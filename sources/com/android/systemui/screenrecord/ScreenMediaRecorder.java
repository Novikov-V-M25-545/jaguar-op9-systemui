package com.android.systemui.screenrecord;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;
import com.android.systemui.R;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/* loaded from: classes.dex */
public class ScreenMediaRecorder {
    private ScreenInternalAudioRecorder mAudio;
    private ScreenRecordingAudioSource mAudioSource;
    private String mAvcProfileLevel;
    private Context mContext;
    private Surface mInputSurface;
    MediaRecorder.OnInfoListener mListener;
    private int mMaxRefreshRate;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private ScreenRecordingMuxer mMuxer;
    private File mTempAudioFile;
    private File mTempVideoFile;
    private int mUser;
    private VirtualDisplay mVirtualDisplay;

    public ScreenMediaRecorder(Context context, int i, ScreenRecordingAudioSource screenRecordingAudioSource, MediaRecorder.OnInfoListener onInfoListener) {
        this.mContext = context;
        this.mUser = i;
        this.mListener = onInfoListener;
        this.mAudioSource = screenRecordingAudioSource;
        this.mMaxRefreshRate = context.getResources().getInteger(R.integer.config_screenRecorderMaxFramerate);
        this.mAvcProfileLevel = this.mContext.getResources().getString(R.string.config_screenRecorderAVCProfileLevel);
    }

    private void prepare() throws IllegalStateException, IOException, RemoteException, IllegalArgumentException {
        this.mMediaProjection = new MediaProjection(this.mContext, IMediaProjection.Stub.asInterface(IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection")).createProjection(this.mUser, this.mContext.getPackageName(), 0, false).asBinder()));
        File cacheDir = this.mContext.getCacheDir();
        cacheDir.mkdirs();
        this.mTempVideoFile = File.createTempFile("temp", ".mp4", cacheDir);
        MediaRecorder mediaRecorder = new MediaRecorder();
        this.mMediaRecorder = mediaRecorder;
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        ScreenRecordingAudioSource screenRecordingAudioSource2 = ScreenRecordingAudioSource.MIC;
        if (screenRecordingAudioSource == screenRecordingAudioSource2) {
            mediaRecorder.setAudioSource(0);
        }
        this.mMediaRecorder.setVideoSource(2);
        this.mMediaRecorder.setOutputFormat(2);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        int refreshRate = (int) windowManager.getDefaultDisplay().getRefreshRate();
        int i3 = this.mMaxRefreshRate;
        if (i3 != 0 && refreshRate > i3) {
            refreshRate = i3;
        }
        this.mMediaRecorder.setVideoEncoder(2);
        this.mMediaRecorder.setVideoEncodingProfileLevel(2, getAvcProfileLevelCodeByName(this.mAvcProfileLevel));
        this.mMediaRecorder.setVideoSize(i, i2);
        this.mMediaRecorder.setVideoFrameRate(refreshRate);
        this.mMediaRecorder.setVideoEncodingBitRate((((i2 * i) * refreshRate) / 30) * 6);
        this.mMediaRecorder.setMaxDuration(3600000);
        this.mMediaRecorder.setMaxFileSize(5000000000L);
        if (this.mAudioSource == screenRecordingAudioSource2) {
            this.mMediaRecorder.setAudioEncoder(4);
            this.mMediaRecorder.setAudioChannels(1);
            this.mMediaRecorder.setAudioEncodingBitRate(196000);
            this.mMediaRecorder.setAudioSamplingRate(44100);
        }
        this.mMediaRecorder.setOutputFile(this.mTempVideoFile);
        this.mMediaRecorder.prepare();
        Surface surface = this.mMediaRecorder.getSurface();
        this.mInputSurface = surface;
        this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay("Recording Display", i, i2, displayMetrics.densityDpi, 16, surface, null, null);
        this.mMediaRecorder.setOnInfoListener(this.mListener);
        ScreenRecordingAudioSource screenRecordingAudioSource3 = this.mAudioSource;
        if (screenRecordingAudioSource3 == ScreenRecordingAudioSource.INTERNAL || screenRecordingAudioSource3 == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
            this.mTempAudioFile = File.createTempFile("temp", ".aac", this.mContext.getCacheDir());
            this.mAudio = new ScreenInternalAudioRecorder(this.mTempAudioFile.getAbsolutePath(), this.mMediaProjection, this.mAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0048  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private int getAvcProfileLevelCodeByName(java.lang.String r5) {
        /*
            r4 = this;
            int r4 = r5.hashCode()
            r0 = 4
            r1 = 3
            r2 = 2
            r3 = 1
            switch(r4) {
                case 51: goto L3e;
                case 52: goto L34;
                case 50486: goto L2a;
                case 50487: goto L20;
                case 51447: goto L16;
                case 51448: goto Lc;
                default: goto Lb;
            }
        Lb:
            goto L48
        Lc:
            java.lang.String r4 = "4.2"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L48
            r4 = 6
            goto L49
        L16:
            java.lang.String r4 = "4.1"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L48
            r4 = r0
            goto L49
        L20:
            java.lang.String r4 = "3.2"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L48
            r4 = r2
            goto L49
        L2a:
            java.lang.String r4 = "3.1"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L48
            r4 = r3
            goto L49
        L34:
            java.lang.String r4 = "4"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L48
            r4 = r1
            goto L49
        L3e:
            java.lang.String r4 = "3"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L48
            r4 = 0
            goto L49
        L48:
            r4 = -1
        L49:
            if (r4 == 0) goto L62
            if (r4 == r3) goto L5f
            if (r4 == r2) goto L5c
            if (r4 == r1) goto L59
            if (r4 == r0) goto L56
            r4 = 8192(0x2000, float:1.148E-41)
            return r4
        L56:
            r4 = 4096(0x1000, float:5.74E-42)
            return r4
        L59:
            r4 = 2048(0x800, float:2.87E-42)
            return r4
        L5c:
            r4 = 1024(0x400, float:1.435E-42)
            return r4
        L5f:
            r4 = 512(0x200, float:7.17E-43)
            return r4
        L62:
            r4 = 256(0x100, float:3.59E-43)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenrecord.ScreenMediaRecorder.getAvcProfileLevelCodeByName(java.lang.String):int");
    }

    void start() throws IllegalStateException, IOException, RemoteException, IllegalArgumentException {
        Log.d("ScreenMediaRecorder", "start recording");
        prepare();
        this.mMediaRecorder.start();
        recordInternalAudio();
    }

    void end() throws IllegalStateException, InterruptedException {
        this.mMediaRecorder.stop();
        this.mMediaProjection.stop();
        this.mMediaRecorder.release();
        this.mMediaRecorder = null;
        this.mMediaProjection = null;
        this.mInputSurface.release();
        this.mVirtualDisplay.release();
        stopInternalAudioRecording();
        Log.d("ScreenMediaRecorder", "end recording");
    }

    private void stopInternalAudioRecording() throws IllegalStateException, InterruptedException {
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
            this.mAudio.end();
            this.mAudio = null;
        }
    }

    private void recordInternalAudio() throws IllegalStateException {
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL) {
            this.mAudio.start();
        }
    }

    protected SavedRecording save() throws IOException {
        String str = new SimpleDateFormat("'screen-'yyyyMMdd-HHmmss'.mp4'").format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", str);
        contentValues.put("mime_type", "video/mp4");
        contentValues.put("date_added", Long.valueOf(System.currentTimeMillis()));
        contentValues.put("datetaken", Long.valueOf(System.currentTimeMillis()));
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri uriInsert = contentResolver.insert(MediaStore.Video.Media.getContentUri("external_primary"), contentValues);
        Log.d("ScreenMediaRecorder", uriInsert.toString());
        ScreenRecordingAudioSource screenRecordingAudioSource = this.mAudioSource;
        if (screenRecordingAudioSource == ScreenRecordingAudioSource.MIC_AND_INTERNAL || screenRecordingAudioSource == ScreenRecordingAudioSource.INTERNAL) {
            try {
                Log.d("ScreenMediaRecorder", "muxing recording");
                File fileCreateTempFile = File.createTempFile("temp", ".mp4", this.mContext.getCacheDir());
                ScreenRecordingMuxer screenRecordingMuxer = new ScreenRecordingMuxer(0, fileCreateTempFile.getAbsolutePath(), this.mTempVideoFile.getAbsolutePath(), this.mTempAudioFile.getAbsolutePath());
                this.mMuxer = screenRecordingMuxer;
                screenRecordingMuxer.mux();
                this.mTempVideoFile.delete();
                this.mTempVideoFile = fileCreateTempFile;
            } catch (IOException e) {
                Log.e("ScreenMediaRecorder", "muxing recording " + e.getMessage());
                e.printStackTrace();
            }
        }
        OutputStream outputStreamOpenOutputStream = contentResolver.openOutputStream(uriInsert, "w");
        Files.copy(this.mTempVideoFile.toPath(), outputStreamOpenOutputStream);
        outputStreamOpenOutputStream.close();
        File file = this.mTempAudioFile;
        if (file != null) {
            file.delete();
        }
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        SavedRecording savedRecording = new SavedRecording(uriInsert, this.mTempVideoFile, new Size(displayMetrics.widthPixels, displayMetrics.heightPixels));
        this.mTempVideoFile.delete();
        return savedRecording;
    }

    public class SavedRecording {
        private Bitmap mThumbnailBitmap;
        private Uri mUri;

        protected SavedRecording(Uri uri, File file, Size size) {
            this.mUri = uri;
            try {
                this.mThumbnailBitmap = ThumbnailUtils.createVideoThumbnail(file, size, null);
            } catch (IOException e) {
                Log.e("ScreenMediaRecorder", "Error creating thumbnail", e);
            }
        }

        public Uri getUri() {
            return this.mUri;
        }

        public Bitmap getThumbnail() {
            return this.mThumbnailBitmap;
        }
    }
}
