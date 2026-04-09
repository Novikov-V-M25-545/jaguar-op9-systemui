package com.android.systemui;

import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Trace;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.glwallpaper.EglHelper;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.glwallpaper.ImageWallpaperRenderer;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class ImageWallpaper extends WallpaperService {
    private static final String TAG = ImageWallpaper.class.getSimpleName();
    private HandlerThread mWorker;

    @Override // android.service.wallpaper.WallpaperService, android.app.Service
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread(TAG);
        this.mWorker = handlerThread;
        handlerThread.start();
    }

    @Override // android.service.wallpaper.WallpaperService
    public WallpaperService.Engine onCreateEngine() {
        return new GLEngine();
    }

    @Override // android.service.wallpaper.WallpaperService, android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.mWorker.quitSafely();
        this.mWorker = null;
    }

    class GLEngine extends WallpaperService.Engine {

        @VisibleForTesting
        static final int MIN_SURFACE_HEIGHT = 64;

        @VisibleForTesting
        static final int MIN_SURFACE_WIDTH = 64;
        private EglHelper mEglHelper;
        private final Runnable mFinishRenderingTask;
        private GLWallpaperRenderer mRenderer;

        public boolean shouldZoomOutWallpaper() {
            return true;
        }

        GLEngine() {
            super(ImageWallpaper.this);
            this.mFinishRenderingTask = new Runnable() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.finishRendering();
                }
            };
        }

        @VisibleForTesting
        GLEngine(Handler handler) {
            super(ImageWallpaper.this, new Supplier() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda5
                @Override // java.util.function.Supplier
                public final Object get() {
                    return Long.valueOf(SystemClock.elapsedRealtime());
                }
            }, handler);
            this.mFinishRenderingTask = new Runnable() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.finishRendering();
                }
            };
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onCreate(SurfaceHolder surfaceHolder) {
            this.mEglHelper = getEglHelperInstance();
            this.mRenderer = getRendererInstance();
            setFixedSizeAllowed(true);
            setOffsetNotificationsEnabled(false);
            updateSurfaceSize();
        }

        EglHelper getEglHelperInstance() {
            return new EglHelper();
        }

        ImageWallpaperRenderer getRendererInstance() {
            return new ImageWallpaperRenderer(getDisplayContext());
        }

        private void updateSurfaceSize() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Size sizeReportSurfaceSize = this.mRenderer.reportSurfaceSize();
            surfaceHolder.setFixedSize(Math.max(64, sizeReportSurfaceSize.getWidth()), Math.max(64, sizeReportSurfaceSize.getHeight()));
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onDestroy() {
            ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onDestroy$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDestroy$0() {
            this.mRenderer.finish();
            this.mRenderer = null;
            this.mEglHelper.finish();
            this.mEglHelper = null;
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceCreated(final SurfaceHolder surfaceHolder) {
            if (ImageWallpaper.this.mWorker == null) {
                return;
            }
            ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onSurfaceCreated$1(surfaceHolder);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onSurfaceCreated$1(SurfaceHolder surfaceHolder) {
            this.mEglHelper.init(surfaceHolder, needSupportWideColorGamut());
            this.mRenderer.onSurfaceCreated();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, final int i2, final int i3) {
            if (ImageWallpaper.this.mWorker == null) {
                return;
            }
            ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onSurfaceChanged$2(i2, i3);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onSurfaceChanged$2(int i, int i2) {
            this.mRenderer.onSurfaceChanged(i, i2);
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
            if (ImageWallpaper.this.mWorker == null) {
                return;
            }
            ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() { // from class: com.android.systemui.ImageWallpaper$GLEngine$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.drawFrame();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void drawFrame() {
            preRender();
            requestRender();
            postRender();
        }

        public void preRender() {
            Trace.beginSection("ImageWallpaper#preRender");
            preRenderInternal();
            Trace.endSection();
        }

        private void preRenderInternal() {
            boolean z;
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            cancelFinishRenderingTask();
            if (this.mEglHelper.hasEglContext()) {
                z = false;
            } else {
                this.mEglHelper.destroyEglSurface();
                if (this.mEglHelper.createEglContext()) {
                    z = true;
                } else {
                    Log.w(ImageWallpaper.TAG, "recreate egl context failed!");
                    z = false;
                }
            }
            if (this.mEglHelper.hasEglContext() && !this.mEglHelper.hasEglSurface() && !this.mEglHelper.createEglSurface(getSurfaceHolder(), needSupportWideColorGamut())) {
                Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
            }
            if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && z) {
                this.mRenderer.onSurfaceCreated();
                this.mRenderer.onSurfaceChanged(surfaceFrame.width(), surfaceFrame.height());
            }
        }

        public void requestRender() {
            Trace.beginSection("ImageWallpaper#requestRender");
            requestRenderInternal();
            Trace.endSection();
        }

        private void requestRenderInternal() {
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            if (!(this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && surfaceFrame.width() > 0 && surfaceFrame.height() > 0)) {
                Log.e(ImageWallpaper.TAG, "requestRender: not ready, has context=" + this.mEglHelper.hasEglContext() + ", has surface=" + this.mEglHelper.hasEglSurface() + ", frame=" + surfaceFrame);
                return;
            }
            this.mRenderer.onDrawFrame();
            if (this.mEglHelper.swapBuffer()) {
                return;
            }
            Log.e(ImageWallpaper.TAG, "drawFrame failed!");
        }

        public void postRender() {
            Trace.beginSection("ImageWallpaper#postRender");
            scheduleFinishRendering();
            Trace.endSection();
        }

        private void cancelFinishRenderingTask() {
            if (ImageWallpaper.this.mWorker == null) {
                return;
            }
            ImageWallpaper.this.mWorker.getThreadHandler().removeCallbacks(this.mFinishRenderingTask);
        }

        private void scheduleFinishRendering() {
            if (ImageWallpaper.this.mWorker == null) {
                return;
            }
            cancelFinishRenderingTask();
            ImageWallpaper.this.mWorker.getThreadHandler().postDelayed(this.mFinishRenderingTask, 1000L);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void finishRendering() {
            Trace.beginSection("ImageWallpaper#finishRendering");
            EglHelper eglHelper = this.mEglHelper;
            if (eglHelper != null) {
                eglHelper.destroyEglSurface();
                this.mEglHelper.destroyEglContext();
            }
            Trace.endSection();
        }

        private boolean needSupportWideColorGamut() {
            return this.mRenderer.isWcgContent();
        }

        @Override // android.service.wallpaper.WallpaperService.Engine
        protected void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            super.dump(str, fileDescriptor, printWriter, strArr);
            printWriter.print(str);
            printWriter.print("Engine=");
            printWriter.println(this);
            printWriter.print(str);
            printWriter.print("valid surface=");
            printWriter.println((getSurfaceHolder() == null || getSurfaceHolder().getSurface() == null) ? "null" : Boolean.valueOf(getSurfaceHolder().getSurface().isValid()));
            printWriter.print(str);
            printWriter.print("surface frame=");
            printWriter.println(getSurfaceHolder() != null ? getSurfaceHolder().getSurfaceFrame() : "null");
            this.mEglHelper.dump(str, fileDescriptor, printWriter, strArr);
            this.mRenderer.dump(str, fileDescriptor, printWriter, strArr);
        }
    }
}
