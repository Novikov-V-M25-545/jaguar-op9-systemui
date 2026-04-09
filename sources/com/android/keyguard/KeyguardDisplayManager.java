package com.android.keyguard;

import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.util.InjectionInflationController;

/* loaded from: classes.dex */
public class KeyguardDisplayManager {
    private static boolean DEBUG = false;
    private final Context mContext;
    private final DisplayManager.DisplayListener mDisplayListener;
    private final DisplayManager mDisplayService;
    private final InjectionInflationController mInjectableInflater;
    private final MediaRouter mMediaRouter;
    private final MediaRouter.SimpleCallback mMediaRouterCallback;
    private boolean mShowing;
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final SparseArray<Presentation> mPresentations = new SparseArray<>();
    private final NavigationBarController mNavBarController = (NavigationBarController) Dependency.get(NavigationBarController.class);

    public KeyguardDisplayManager(Context context, InjectionInflationController injectionInflationController) {
        DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() { // from class: com.android.keyguard.KeyguardDisplayManager.1
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int i) {
                Display display = KeyguardDisplayManager.this.mDisplayService.getDisplay(i);
                if (KeyguardDisplayManager.this.mShowing) {
                    KeyguardDisplayManager.this.updateNavigationBarVisibility(i, false);
                    KeyguardDisplayManager.this.showPresentation(display);
                }
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int i) {
                if (i == 0 || ((Presentation) KeyguardDisplayManager.this.mPresentations.get(i)) == null || !KeyguardDisplayManager.this.mShowing) {
                    return;
                }
                KeyguardDisplayManager.this.hidePresentation(i);
                Display display = KeyguardDisplayManager.this.mDisplayService.getDisplay(i);
                if (display != null) {
                    KeyguardDisplayManager.this.showPresentation(display);
                }
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int i) {
                KeyguardDisplayManager.this.hidePresentation(i);
            }
        };
        this.mDisplayListener = displayListener;
        this.mMediaRouterCallback = new MediaRouter.SimpleCallback() { // from class: com.android.keyguard.KeyguardDisplayManager.2
            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
                if (KeyguardDisplayManager.DEBUG) {
                    Log.d("KeyguardDisplayManager", "onRouteSelected: type=" + i + ", info=" + routeInfo);
                }
                KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
                keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
            }

            @Override // android.media.MediaRouter.SimpleCallback, android.media.MediaRouter.Callback
            public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
                if (KeyguardDisplayManager.DEBUG) {
                    Log.d("KeyguardDisplayManager", "onRouteUnselected: type=" + i + ", info=" + routeInfo);
                }
                KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
                keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
            }

            @Override // android.media.MediaRouter.Callback
            public void onRoutePresentationDisplayChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
                if (KeyguardDisplayManager.DEBUG) {
                    Log.d("KeyguardDisplayManager", "onRoutePresentationDisplayChanged: info=" + routeInfo);
                }
                KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
                keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
            }
        };
        this.mContext = context;
        this.mInjectableInflater = injectionInflationController;
        this.mMediaRouter = (MediaRouter) context.getSystemService(MediaRouter.class);
        DisplayManager displayManager = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.mDisplayService = displayManager;
        displayManager.registerDisplayListener(displayListener, null);
    }

    private boolean isKeyguardShowable(Display display) {
        if (display == null) {
            if (DEBUG) {
                Log.i("KeyguardDisplayManager", "Cannot show Keyguard on null display");
            }
            return false;
        }
        if (display.getDisplayId() == 0) {
            if (DEBUG) {
                Log.i("KeyguardDisplayManager", "Do not show KeyguardPresentation on the default display");
            }
            return false;
        }
        display.getDisplayInfo(this.mTmpDisplayInfo);
        if ((this.mTmpDisplayInfo.flags & 4) == 0) {
            return true;
        }
        if (DEBUG) {
            Log.i("KeyguardDisplayManager", "Do not show KeyguardPresentation on a private display");
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean showPresentation(Display display) {
        if (!isKeyguardShowable(display)) {
            return false;
        }
        if (DEBUG) {
            Log.i("KeyguardDisplayManager", "Keyguard enabled on display: " + display);
        }
        final int displayId = display.getDisplayId();
        if (this.mPresentations.get(displayId) == null) {
            Context context = this.mContext;
            final KeyguardPresentation keyguardPresentation = new KeyguardPresentation(context, display, this.mInjectableInflater.injectable(LayoutInflater.from(context)));
            keyguardPresentation.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.keyguard.KeyguardDisplayManager$$ExternalSyntheticLambda0
                @Override // android.content.DialogInterface.OnDismissListener
                public final void onDismiss(DialogInterface dialogInterface) {
                    this.f$0.lambda$showPresentation$0(keyguardPresentation, displayId, dialogInterface);
                }
            });
            try {
                keyguardPresentation.show();
            } catch (WindowManager.InvalidDisplayException e) {
                Log.w("KeyguardDisplayManager", "Invalid display:", e);
                keyguardPresentation = null;
            }
            if (keyguardPresentation != null) {
                this.mPresentations.append(displayId, keyguardPresentation);
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPresentation$0(Presentation presentation, int i, DialogInterface dialogInterface) {
        if (presentation.equals(this.mPresentations.get(i))) {
            this.mPresentations.remove(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hidePresentation(int i) {
        Presentation presentation = this.mPresentations.get(i);
        if (presentation != null) {
            presentation.dismiss();
            this.mPresentations.remove(i);
        }
    }

    public void show() {
        if (!this.mShowing) {
            if (DEBUG) {
                Log.v("KeyguardDisplayManager", "show");
            }
            this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
            updateDisplays(true);
        }
        this.mShowing = true;
    }

    public void hide() {
        if (this.mShowing) {
            if (DEBUG) {
                Log.v("KeyguardDisplayManager", "hide");
            }
            this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
            updateDisplays(false);
        }
        this.mShowing = false;
    }

    protected boolean updateDisplays(boolean z) {
        if (z) {
            boolean zShowPresentation = false;
            for (Display display : this.mDisplayService.getDisplays()) {
                updateNavigationBarVisibility(display.getDisplayId(), false);
                zShowPresentation |= showPresentation(display);
            }
            return zShowPresentation;
        }
        boolean z2 = this.mPresentations.size() > 0;
        for (int size = this.mPresentations.size() - 1; size >= 0; size--) {
            updateNavigationBarVisibility(this.mPresentations.keyAt(size), true);
            this.mPresentations.valueAt(size).dismiss();
        }
        this.mPresentations.clear();
        return z2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateNavigationBarVisibility(int i, boolean z) {
        NavigationBarView navigationBarView;
        if (i == 0 || (navigationBarView = this.mNavBarController.getNavigationBarView(i)) == null) {
            return;
        }
        if (z) {
            navigationBarView.getRootView().setVisibility(0);
        } else {
            navigationBarView.getRootView().setVisibility(8);
        }
    }

    @VisibleForTesting
    static final class KeyguardPresentation extends Presentation {
        private View mClock;
        private final LayoutInflater mInjectableLayoutInflater;
        private int mMarginLeft;
        private int mMarginTop;
        Runnable mMoveTextRunnable;
        private int mUsableHeight;
        private int mUsableWidth;

        @Override // android.app.Dialog, android.content.DialogInterface
        public void cancel() {
        }

        KeyguardPresentation(Context context, Display display, LayoutInflater layoutInflater) {
            super(context, display, R.style.Theme_SystemUI_KeyguardPresentation);
            this.mMoveTextRunnable = new Runnable() { // from class: com.android.keyguard.KeyguardDisplayManager.KeyguardPresentation.1
                @Override // java.lang.Runnable
                public void run() {
                    int iRandom = KeyguardPresentation.this.mMarginLeft + ((int) (Math.random() * (KeyguardPresentation.this.mUsableWidth - KeyguardPresentation.this.mClock.getWidth())));
                    int iRandom2 = KeyguardPresentation.this.mMarginTop + ((int) (Math.random() * (KeyguardPresentation.this.mUsableHeight - KeyguardPresentation.this.mClock.getHeight())));
                    KeyguardPresentation.this.mClock.setTranslationX(iRandom);
                    KeyguardPresentation.this.mClock.setTranslationY(iRandom2);
                    KeyguardPresentation.this.mClock.postDelayed(KeyguardPresentation.this.mMoveTextRunnable, 10000L);
                }
            };
            this.mInjectableLayoutInflater = layoutInflater;
            getWindow().setType(2009);
            setCancelable(false);
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public void onDetachedFromWindow() {
            this.mClock.removeCallbacks(this.mMoveTextRunnable);
        }

        @Override // android.app.Dialog
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Point point = new Point();
            getDisplay().getSize(point);
            int i = point.x;
            this.mUsableWidth = (i * 80) / 100;
            int i2 = point.y;
            this.mUsableHeight = (i2 * 80) / 100;
            this.mMarginLeft = (i * 20) / 200;
            this.mMarginTop = (i2 * 20) / 200;
            setContentView(this.mInjectableLayoutInflater.inflate(R.layout.keyguard_presentation, (ViewGroup) null));
            getWindow().getDecorView().setSystemUiVisibility(1792);
            getWindow().getAttributes().setFitInsetsTypes(0);
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setNavigationBarColor(0);
            View viewFindViewById = findViewById(R.id.clock);
            this.mClock = viewFindViewById;
            viewFindViewById.post(this.mMoveTextRunnable);
        }
    }
}
