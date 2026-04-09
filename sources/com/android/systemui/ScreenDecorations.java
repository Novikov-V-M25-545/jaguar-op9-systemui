package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.mediarouter.media.MediaRoute2Provider$$ExternalSyntheticLambda0;
import com.android.internal.util.Preconditions;
import com.android.systemui.CameraAvailabilityListener;
import com.android.systemui.RegionInterceptingFrameLayout;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class ScreenDecorations extends SystemUI implements TunerService.Tunable, StatusBarStateController.StateListener {
    private static final boolean DEBUG_COLOR;
    private static final boolean DEBUG_SCREENSHOT_ROUNDED_CORNERS;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private CameraAvailabilityListener mCameraListener;
    private CameraAvailabilityListener.CameraTransitionCallback mCameraTransitionCallback;
    private SecureSetting mColorInversionSetting;
    private CustomSettingsObserver mCustomSettingsObserver;
    private DisplayCutoutView[] mCutoutViews;
    private float mDensity;
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private boolean mFullscreenMode;
    private Handler mHandler;
    private boolean mImmerseMode;
    private int mImmerseModeSetting;
    protected boolean mIsRegistered;
    private boolean mIsRoundedCornerMultipleRadius;
    private final Handler mMainHandler;
    protected View[] mOverlays;
    private boolean mPendingRotationChange;
    private int mRotation;
    protected Point mRoundedDefault;
    protected Point mRoundedDefaultBottom;
    protected Point mRoundedDefaultTop;
    private int mRoundedSize;
    private StatusBar mStatusBar;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private boolean mTopEnabled;
    private final TunerService mTunerService;
    private final BroadcastReceiver mUserSwitchIntentReceiver;
    private WindowManager mWindowManager;
    private Point mZeroPoint;

    /* JADX INFO: Access modifiers changed from: private */
    public static int getBoundPositionFromRotation(int i, int i2) {
        int i3 = i - i2;
        return i3 < 0 ? i3 + 4 : i3;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozeAmountChanged(float f, float f2) {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onPulsingChanged(boolean z) {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStatePostChange() {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStatePreChange(int i, int i2) {
    }

    static {
        boolean z = SystemProperties.getBoolean("debug.screenshot_rounded_corners", false);
        DEBUG_SCREENSHOT_ROUNDED_CORNERS = z;
        DEBUG_COLOR = z;
    }

    public static Region rectsToRegion(List<Rect> list) {
        Region regionObtain = Region.obtain();
        if (list != null) {
            for (Rect rect : list) {
                if (rect != null && !rect.isEmpty()) {
                    regionObtain.op(rect, Region.Op.UNION);
                }
            }
        }
        return regionObtain;
    }

    public ScreenDecorations(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher, TunerService tunerService) {
        super(context);
        this.mRoundedDefault = new Point(0, 0);
        this.mRoundedDefaultTop = new Point(0, 0);
        this.mRoundedDefaultBottom = new Point(0, 0);
        this.mImmerseModeSetting = 0;
        this.mTopEnabled = true;
        this.mZeroPoint = new Point(0, 0);
        this.mRoundedSize = -1;
        this.mStatusBarStateController = (SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class);
        this.mFullscreenMode = false;
        this.mImmerseMode = false;
        this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
        this.mCameraTransitionCallback = new CameraAvailabilityListener.CameraTransitionCallback() { // from class: com.android.systemui.ScreenDecorations.1
            @Override // com.android.systemui.CameraAvailabilityListener.CameraTransitionCallback
            public void onApplyCameraProtection(Path path, Rect rect) {
                if (ScreenDecorations.this.mCutoutViews != null) {
                    for (DisplayCutoutView displayCutoutView : ScreenDecorations.this.mCutoutViews) {
                        if (displayCutoutView != null) {
                            displayCutoutView.setProtection(path, rect);
                            displayCutoutView.setShowProtection(true);
                        }
                    }
                    return;
                }
                Log.w("ScreenDecorations", "DisplayCutoutView do not initialized");
            }

            @Override // com.android.systemui.CameraAvailabilityListener.CameraTransitionCallback
            public void onHideCameraProtection() {
                if (ScreenDecorations.this.mCutoutViews != null) {
                    for (DisplayCutoutView displayCutoutView : ScreenDecorations.this.mCutoutViews) {
                        if (displayCutoutView != null) {
                            displayCutoutView.setShowProtection(false);
                        }
                    }
                    return;
                }
                Log.w("ScreenDecorations", "DisplayCutoutView do not initialized");
            }
        };
        this.mUserSwitchIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.ScreenDecorations.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                ScreenDecorations.this.mColorInversionSetting.setUserId(ActivityManager.getCurrentUser());
                ScreenDecorations screenDecorations = ScreenDecorations.this;
                screenDecorations.updateColorInversion(screenDecorations.mColorInversionSetting.getValue());
            }
        };
        this.mCustomSettingsObserver = new CustomSettingsObserver(this.mHandler);
        this.mMainHandler = handler;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mTunerService = tunerService;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        Handler handlerStartHandlerThread = startHandlerThread();
        this.mHandler = handlerStartHandlerThread;
        handlerStartHandlerThread.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.startOnScreenDecorationsThread();
            }
        });
        this.mStatusBarStateController.addCallback(this);
        this.mCustomSettingsObserver.observe();
        this.mCustomSettingsObserver.update();
    }

    Handler startHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("ScreenDecorations");
        handlerThread.start();
        return handlerThread.getThreadHandler();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startOnScreenDecorationsThread() throws Resources.NotFoundException {
        this.mRotation = this.mContext.getDisplay().getRotation();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(WindowManager.class);
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mIsRoundedCornerMultipleRadius = this.mContext.getResources().getBoolean(R.bool.config_roundedCornerMultipleRadius);
        updateRoundedCornerRadii();
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startOnScreenDecorationsThread$0();
            }
        });
        setupDecorations();
        setupCameraListener();
        DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() { // from class: com.android.systemui.ScreenDecorations.2
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int i) {
                int rotation = ScreenDecorations.this.mContext.getDisplay().getRotation();
                ScreenDecorations screenDecorations = ScreenDecorations.this;
                if (screenDecorations.mOverlays != null && screenDecorations.mRotation != rotation) {
                    ScreenDecorations.this.mPendingRotationChange = true;
                    for (int i2 = 0; i2 < 4; i2++) {
                        View[] viewArr = ScreenDecorations.this.mOverlays;
                        if (viewArr[i2] != null) {
                            ViewTreeObserver viewTreeObserver = viewArr[i2].getViewTreeObserver();
                            ScreenDecorations screenDecorations2 = ScreenDecorations.this;
                            viewTreeObserver.addOnPreDrawListener(new RestartingPreDrawListener(screenDecorations2.mOverlays[i2], i2, rotation));
                        }
                    }
                }
                ScreenDecorations.this.updateOrientation();
            }
        };
        this.mDisplayListener = displayListener;
        this.mDisplayManager.registerDisplayListener(displayListener, this.mHandler);
        updateOrientation();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startOnScreenDecorationsThread$0() {
        this.mTunerService.addTunable(this, "sysui_rounded_size");
    }

    private void setupDecorations() {
        if (hasRoundedCorners() || shouldDrawCutout()) {
            DisplayCutout cutout = getCutout();
            Rect[] boundingRectsAll = cutout == null ? null : cutout.getBoundingRectsAll();
            for (int i = 0; i < 4; i++) {
                int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
                if ((boundingRectsAll != null && !boundingRectsAll[boundPositionFromRotation].isEmpty()) || shouldShowRoundedCorner(i)) {
                    createOverlay(i);
                } else {
                    removeOverlay(i);
                }
            }
        } else {
            removeAllOverlays();
        }
        if (hasOverlays()) {
            if (this.mIsRegistered) {
                return;
            }
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.mDisplayManager.getDisplay(0).getMetrics(displayMetrics);
            this.mDensity = displayMetrics.density;
            if (this.mColorInversionSetting == null) {
                SecureSetting secureSetting = new SecureSetting(this.mContext, this.mHandler, "accessibility_display_inversion_enabled") { // from class: com.android.systemui.ScreenDecorations.3
                    @Override // com.android.systemui.qs.SecureSetting
                    protected void handleValueChanged(int i2, boolean z) {
                        ScreenDecorations.this.updateColorInversion(i2);
                    }
                };
                this.mColorInversionSetting = secureSetting;
                secureSetting.setListening(true);
                this.mColorInversionSetting.onChange(false);
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            this.mBroadcastDispatcher.registerReceiver(this.mUserSwitchIntentReceiver, intentFilter, new HandlerExecutor(this.mHandler), UserHandle.ALL);
            this.mIsRegistered = true;
            return;
        }
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setupDecorations$1();
            }
        });
        SecureSetting secureSetting2 = this.mColorInversionSetting;
        if (secureSetting2 != null) {
            secureSetting2.setListening(false);
        }
        this.mBroadcastDispatcher.unregisterReceiver(this.mUserSwitchIntentReceiver);
        this.mIsRegistered = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupDecorations$1() {
        this.mTunerService.removeTunable(this);
    }

    DisplayCutout getCutout() {
        return this.mContext.getDisplay().getCutout();
    }

    boolean hasOverlays() {
        if (this.mOverlays == null) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (this.mOverlays[i] != null) {
                return true;
            }
        }
        this.mOverlays = null;
        return false;
    }

    private void removeAllOverlays() {
        if (this.mOverlays == null) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (this.mOverlays[i] != null) {
                removeOverlay(i);
            }
        }
        this.mOverlays = null;
    }

    private void removeOverlay(int i) {
        View[] viewArr = this.mOverlays;
        if (viewArr == null || viewArr[i] == null) {
            return;
        }
        this.mWindowManager.removeViewImmediate(viewArr[i]);
        this.mOverlays[i] = null;
    }

    private void createOverlay(final int i) {
        if (this.mOverlays == null) {
            this.mOverlays = new View[4];
        }
        if (this.mCutoutViews == null) {
            this.mCutoutViews = new DisplayCutoutView[4];
        }
        View[] viewArr = this.mOverlays;
        if (viewArr[i] != null) {
            return;
        }
        viewArr[i] = overlayForPosition(i);
        this.mCutoutViews[i] = new DisplayCutoutView(this.mContext, i, this);
        ((ViewGroup) this.mOverlays[i]).addView(this.mCutoutViews[i]);
        this.mOverlays[i].setSystemUiVisibility(LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT);
        this.mOverlays[i].setAlpha(0.0f);
        this.mOverlays[i].setForceDarkAllowed(false);
        updateView(i);
        this.mWindowManager.addView(this.mOverlays[i], getWindowLayoutParams(i));
        this.mOverlays[i].addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.ScreenDecorations.4
            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                ScreenDecorations.this.mOverlays[i].removeOnLayoutChangeListener(this);
                ScreenDecorations.this.mOverlays[i].animate().alpha(1.0f).setDuration(1000L).start();
            }
        });
        this.mOverlays[i].getViewTreeObserver().addOnPreDrawListener(new ValidatingPreDrawListener(this.mOverlays[i]));
    }

    private View overlayForPosition(int i) {
        if (i == 1) {
            return LayoutInflater.from(this.mContext).inflate(R.layout.rounded_corners_top, (ViewGroup) null);
        }
        if (i == 3) {
            return LayoutInflater.from(this.mContext).inflate(R.layout.rounded_corners_bottom, (ViewGroup) null);
        }
        return LayoutInflater.from(this.mContext).inflate(R.layout.rounded_corners, (ViewGroup) null);
    }

    private void updateView(int i) {
        View[] viewArr = this.mOverlays;
        if (viewArr == null || viewArr[i] == null) {
            return;
        }
        updateRoundedCornerView(i, R.id.left);
        updateRoundedCornerView(i, R.id.right);
        updateRoundedCornerSize(this.mRoundedDefault, this.mRoundedDefaultTop, this.mRoundedDefaultBottom);
        DisplayCutoutView[] displayCutoutViewArr = this.mCutoutViews;
        if (displayCutoutViewArr == null || displayCutoutViewArr[i] == null) {
            return;
        }
        displayCutoutViewArr[i].setRotation(this.mRotation);
    }

    WindowManager.LayoutParams getWindowLayoutParams(int i) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(getWidthLayoutParamByPos(i), getHeightLayoutParamByPos(i), 2024, 545259816, -3);
        int i2 = layoutParams.privateFlags | 80;
        layoutParams.privateFlags = i2;
        if (!DEBUG_SCREENSHOT_ROUNDED_CORNERS) {
            layoutParams.privateFlags = i2 | 1048576;
        }
        layoutParams.setTitle(getWindowTitleByPos(i));
        layoutParams.gravity = getOverlayWindowGravity(i);
        if (this.mImmerseMode && (!this.mFullscreenMode || this.mStatusBar.isKeyguardShowing())) {
            layoutParams.layoutInDisplayCutoutMode = 2;
        } else {
            layoutParams.layoutInDisplayCutoutMode = 3;
        }
        layoutParams.setFitInsetsTypes(0);
        layoutParams.privateFlags |= 16777216;
        return layoutParams;
    }

    private int getWidthLayoutParamByPos(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        return (boundPositionFromRotation == 1 || boundPositionFromRotation == 3) ? -1 : -2;
    }

    private int getHeightLayoutParamByPos(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        return (boundPositionFromRotation == 1 || boundPositionFromRotation == 3) ? -2 : -1;
    }

    private static String getWindowTitleByPos(int i) {
        if (i == 0) {
            return "ScreenDecorOverlayLeft";
        }
        if (i == 1) {
            return "ScreenDecorOverlay";
        }
        if (i == 2) {
            return "ScreenDecorOverlayRight";
        }
        if (i == 3) {
            return "ScreenDecorOverlayBottom";
        }
        throw new IllegalArgumentException("unknown bound position: " + i);
    }

    private int getOverlayWindowGravity(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        if (boundPositionFromRotation == 0) {
            return 3;
        }
        if (boundPositionFromRotation == 1) {
            return 48;
        }
        if (boundPositionFromRotation == 2) {
            return 5;
        }
        if (boundPositionFromRotation == 3) {
            return 80;
        }
        throw new IllegalArgumentException("unknown bound position: " + i);
    }

    private void setupCameraListener() {
        if (this.mContext.getResources().getBoolean(R.bool.config_enableDisplayCutoutProtection)) {
            CameraAvailabilityListener.Factory factory = CameraAvailabilityListener.Factory;
            Context context = this.mContext;
            Handler handler = this.mHandler;
            Objects.requireNonNull(handler);
            CameraAvailabilityListener cameraAvailabilityListenerBuild = factory.build(context, new MediaRoute2Provider$$ExternalSyntheticLambda0(handler));
            this.mCameraListener = cameraAvailabilityListenerBuild;
            cameraAvailabilityListenerBuild.addTransitionCallback(this.mCameraTransitionCallback);
            this.mCameraListener.startListening();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateColorInversion(int i) {
        int i2 = i != 0 ? -1 : -16777216;
        ColorStateList colorStateListValueOf = ColorStateList.valueOf(i2);
        if (this.mOverlays == null) {
            return;
        }
        for (int i3 = 0; i3 < 4; i3++) {
            View[] viewArr = this.mOverlays;
            if (viewArr[i3] != null) {
                int childCount = ((ViewGroup) viewArr[i3]).getChildCount();
                for (int i4 = 0; i4 < childCount; i4++) {
                    View childAt = ((ViewGroup) this.mOverlays[i3]).getChildAt(i4);
                    if (childAt instanceof ImageView) {
                        ((ImageView) childAt).setImageTintList(colorStateListValueOf);
                    } else if (childAt instanceof DisplayCutoutView) {
                        ((DisplayCutoutView) childAt).setColor(i2);
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    protected void onConfigurationChanged(Configuration configuration) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.lambda$onConfigurationChanged$2();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onConfigurationChanged$2() throws Resources.NotFoundException {
        this.mPendingRotationChange = false;
        updateOrientation();
        updateRoundedCornerRadii();
        setupDecorations();
        if (this.mOverlays != null) {
            lambda$onFullscreenStateChanged$6();
            updateCutoutMode();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateOrientation() {
        int rotation;
        Preconditions.checkState(this.mHandler.getLooper().getThread() == Thread.currentThread(), "must call on " + this.mHandler.getLooper().getThread() + ", but was " + Thread.currentThread());
        if (this.mPendingRotationChange || (rotation = this.mContext.getDisplay().getRotation()) == this.mRotation) {
            return;
        }
        this.mRotation = rotation;
        if (this.mOverlays != null) {
            lambda$onFullscreenStateChanged$6();
            for (int i = 0; i < 4; i++) {
                if (this.mOverlays[i] != null) {
                    updateView(i);
                }
            }
        }
    }

    private void updateRoundedCornerRadii() throws Resources.NotFoundException {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(android.R.dimen.navigation_bar_width);
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_action_disabled_alpha);
        int dimensionPixelSize3 = this.mContext.getResources().getDimensionPixelSize(android.R.dimen.navigation_edge_action_progress_threshold);
        Point point = this.mRoundedDefault;
        if ((point.x == dimensionPixelSize && this.mRoundedDefaultTop.x == dimensionPixelSize && this.mRoundedDefaultBottom.x == dimensionPixelSize) ? false : true) {
            if (this.mIsRoundedCornerMultipleRadius) {
                Drawable drawable = this.mContext.getDrawable(R.drawable.rounded);
                this.mRoundedDefault.set(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                Drawable drawable2 = this.mContext.getDrawable(R.drawable.rounded_corner_top);
                this.mRoundedDefaultTop.set(drawable2.getIntrinsicWidth(), drawable2.getIntrinsicHeight());
                Drawable drawable3 = this.mContext.getDrawable(R.drawable.rounded_corner_bottom);
                this.mRoundedDefaultBottom.set(drawable3.getIntrinsicWidth(), drawable3.getIntrinsicHeight());
            } else {
                point.set(dimensionPixelSize, dimensionPixelSize);
                this.mRoundedDefaultTop.set(dimensionPixelSize2, dimensionPixelSize2);
                this.mRoundedDefaultBottom.set(dimensionPixelSize3, dimensionPixelSize3);
            }
            onTuningChanged("sysui_rounded_size", null);
        }
    }

    private void updateRoundedCornerView(int i, int i2) {
        View viewFindViewById = this.mOverlays[i].findViewById(i2);
        if (viewFindViewById == null) {
            return;
        }
        viewFindViewById.setVisibility(8);
        if (shouldShowRoundedCorner(i)) {
            int roundedCornerGravity = getRoundedCornerGravity(i, i2 == R.id.left);
            ((FrameLayout.LayoutParams) viewFindViewById.getLayoutParams()).gravity = roundedCornerGravity;
            setRoundedCornerOrientation(viewFindViewById, roundedCornerGravity);
            viewFindViewById.setVisibility(0);
        }
    }

    private int getRoundedCornerGravity(int i, boolean z) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        if (boundPositionFromRotation == 0) {
            return z ? 51 : 83;
        }
        if (boundPositionFromRotation == 1) {
            return z ? 51 : 53;
        }
        if (boundPositionFromRotation == 2) {
            return z ? 53 : 85;
        }
        if (boundPositionFromRotation == 3) {
            return z ? 83 : 85;
        }
        throw new IllegalArgumentException("Incorrect position: " + boundPositionFromRotation);
    }

    private void setRoundedCornerOrientation(View view, int i) {
        view.setRotation(0.0f);
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        if (i != 51) {
            if (i == 53) {
                view.setScaleX(-1.0f);
                return;
            }
            if (i == 83) {
                view.setScaleY(-1.0f);
            } else {
                if (i == 85) {
                    view.setRotation(180.0f);
                    return;
                }
                throw new IllegalArgumentException("Unsupported gravity: " + i);
            }
        }
    }

    private boolean hasRoundedCorners() {
        return this.mRoundedDefault.x > 0 || this.mRoundedDefaultBottom.x > 0 || this.mRoundedDefaultTop.x > 0 || this.mIsRoundedCornerMultipleRadius || this.mRoundedSize > 0;
    }

    private boolean shouldShowRoundedCorner(int i) {
        if (!hasRoundedCorners()) {
            return false;
        }
        DisplayCutout cutout = getCutout();
        return (!(cutout == null || cutout.isBoundsEmpty()) && cutout.getBoundingRectsAll()[getBoundPositionFromRotation(1, this.mRotation)].isEmpty() && cutout.getBoundingRectsAll()[getBoundPositionFromRotation(3, this.mRotation)].isEmpty()) ? i == 0 || i == 2 : i == 1 || i == 3;
    }

    private boolean shouldDrawCutout() {
        return shouldDrawCutout(this.mContext);
    }

    static boolean shouldDrawCutout(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return !(contentResolver != null && Settings.System.getIntForUser(contentResolver, "display_cutout_mode", 0, -2) != 0) && context.getResources().getBoolean(android.R.bool.config_cecQuerySadAacEnabled_allowed);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: updateLayoutParams, reason: merged with bridge method [inline-methods] */
    public void lambda$onFullscreenStateChanged$6() {
        if (this.mOverlays == null) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            View[] viewArr = this.mOverlays;
            if (viewArr[i] != null) {
                this.mWindowManager.updateViewLayout(viewArr[i], getWindowLayoutParams(i));
            }
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(final String str, final String str2) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onTuningChanged$3(str, str2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onTuningChanged$3(String str, String str2) {
        if ("sysui_rounded_size".equals(str)) {
            Point point = this.mRoundedDefault;
            if (this.mOverlays == null) {
                setupDecorations();
            }
            Point point2 = this.mRoundedDefaultTop;
            Point point3 = this.mRoundedDefaultBottom;
            if (str2 != null) {
                int integer = TunerService.parseInteger(str2, -1);
                this.mRoundedSize = integer;
                if (integer >= 0) {
                    try {
                        int i = (int) (integer * this.mDensity);
                        point = new Point(i, i);
                    } catch (Exception unused) {
                    }
                }
            }
            updateRoundedCornerSize(point, point2, point3);
        }
    }

    private void updateRoundedCornerSize(Point point, Point point2, Point point3) {
        if (this.mOverlays == null) {
            return;
        }
        if (point.x > 0) {
            point3 = point;
        } else {
            if (!this.mTopEnabled && this.mRotation == 0) {
                point2 = this.mZeroPoint;
            } else if (point2.x == 0) {
                point2 = point;
            }
            if (point3.x == 0) {
                point3 = point;
            }
            point = point2;
        }
        for (int i = 0; i < 4; i++) {
            View[] viewArr = this.mOverlays;
            if (viewArr[i] != null) {
                if (i == 0 || i == 2) {
                    if (this.mRotation == 3) {
                        setSize(viewArr[i].findViewById(R.id.left), point3);
                        setSize(this.mOverlays[i].findViewById(R.id.right), point);
                    } else {
                        setSize(viewArr[i].findViewById(R.id.left), point);
                        setSize(this.mOverlays[i].findViewById(R.id.right), point3);
                    }
                } else if (i == 1) {
                    setSize(viewArr[i].findViewById(R.id.left), point);
                    setSize(this.mOverlays[i].findViewById(R.id.right), point);
                } else if (i == 3) {
                    setSize(viewArr[i].findViewById(R.id.left), point3);
                    setSize(this.mOverlays[i].findViewById(R.id.right), point3);
                }
            }
        }
    }

    public void setTopCorners(boolean z) {
        if (this.mImmerseModeSetting != 1 || this.mTopEnabled == z) {
            return;
        }
        this.mTopEnabled = z;
        if (this.mOverlays != null) {
            if (!this.mHandler.getLooper().isCurrentThread()) {
                this.mHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$setTopCorners$4();
                    }
                });
            } else {
                lambda$updateCutoutMode$5();
            }
        }
    }

    protected void setSize(View view, Point point) {
        if (point.x <= 0 && point.y <= 0) {
            view.setVisibility(4);
            return;
        }
        view.setVisibility(0);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = point.x;
        layoutParams.height = point.y;
        view.setLayoutParams(layoutParams);
    }

    public static class DisplayCutoutView extends View implements DisplayManager.DisplayListener, RegionInterceptingFrameLayout.RegionInterceptableView {
        private final Path mBoundingPath;
        private final Rect mBoundingRect;
        private final List<Rect> mBounds;
        private ValueAnimator mCameraProtectionAnimator;
        private float mCameraProtectionProgress;
        private int mColor;
        private CustomSettingsObserver mCustomSettingsObserver;
        private final ScreenDecorations mDecorations;
        private Display.Mode mDisplayMode;
        private final DisplayInfo mInfo;
        private int mInitialPosition;
        private final int[] mLocation;
        private final Paint mPaint;
        private int mPosition;
        private Path mProtectionPath;
        private Path mProtectionPathOrig;
        private RectF mProtectionRect;
        private RectF mProtectionRectOrig;
        private int mRotation;
        private boolean mShowProtection;
        private Rect mTotalBounds;

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int i) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int i) {
        }

        public DisplayCutoutView(Context context, int i, ScreenDecorations screenDecorations) {
            super(context);
            this.mDisplayMode = null;
            this.mInfo = new DisplayInfo();
            this.mPaint = new Paint();
            this.mBounds = new ArrayList();
            this.mBoundingRect = new Rect();
            this.mBoundingPath = new Path();
            this.mTotalBounds = new Rect();
            this.mShowProtection = false;
            this.mLocation = new int[2];
            this.mColor = -16777216;
            this.mCameraProtectionProgress = 0.5f;
            this.mInitialPosition = i;
            this.mDecorations = screenDecorations;
            setId(R.id.display_cutout);
            CustomSettingsObserver customSettingsObserver = new CustomSettingsObserver(new Handler());
            this.mCustomSettingsObserver = customSettingsObserver;
            customSettingsObserver.observe();
        }

        public void setColor(int i) {
            this.mColor = i;
            invalidate();
        }

        @Override // android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            ((DisplayManager) ((View) this).mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, getHandler());
            update();
        }

        @Override // android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            ((DisplayManager) ((View) this).mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            getLocationOnScreen(this.mLocation);
            int[] iArr = this.mLocation;
            canvas.translate(-iArr[0], -iArr[1]);
            if (!this.mBoundingPath.isEmpty()) {
                this.mPaint.setColor(this.mColor);
                this.mPaint.setStyle(Paint.Style.FILL);
                this.mPaint.setAntiAlias(true);
                canvas.drawPath(this.mBoundingPath, this.mPaint);
            }
            if (this.mCameraProtectionProgress <= 0.5f || this.mProtectionRect.isEmpty()) {
                return;
            }
            float f = this.mCameraProtectionProgress;
            canvas.scale(f, f, this.mProtectionRect.centerX(), this.mProtectionRect.centerY());
            canvas.drawPath(this.mProtectionPath, this.mPaint);
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int i) {
            Display.Mode mode = this.mDisplayMode;
            Display.Mode mode2 = getDisplay().getMode();
            this.mDisplayMode = mode2;
            if (modeChanged(mode, mode2) && i == getDisplay().getDisplayId()) {
                update();
            }
        }

        private boolean modeChanged(Display.Mode mode, Display.Mode mode2) {
            if (mode == null) {
                return true;
            }
            return (mode.getPhysicalWidth() != mode2.getPhysicalWidth()) | (mode.getPhysicalHeight() != mode2.getPhysicalHeight()) | false;
        }

        public void setRotation(int i) {
            this.mRotation = i;
            update();
        }

        void setProtection(Path path, Rect rect) {
            if (this.mProtectionPathOrig == null) {
                this.mProtectionPathOrig = new Path();
                this.mProtectionPath = new Path();
            }
            this.mProtectionPathOrig.set(path);
            if (this.mProtectionRectOrig == null) {
                this.mProtectionRectOrig = new RectF();
                this.mProtectionRect = new RectF();
            }
            this.mProtectionRectOrig.set(rect);
        }

        void setShowProtection(boolean z) {
            if (this.mShowProtection == z) {
                return;
            }
            this.mShowProtection = z;
            updateBoundingPath();
            if (this.mShowProtection) {
                requestLayout();
            }
            ValueAnimator valueAnimator = this.mCameraProtectionAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            float[] fArr = new float[2];
            fArr[0] = this.mCameraProtectionProgress;
            fArr[1] = this.mShowProtection ? 1.0f : 0.5f;
            ValueAnimator duration = ValueAnimator.ofFloat(fArr).setDuration(750L);
            this.mCameraProtectionAnimator = duration;
            duration.setInterpolator(Interpolators.DECELERATE_QUINT);
            this.mCameraProtectionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.ScreenDecorations$DisplayCutoutView$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    this.f$0.lambda$setShowProtection$1(valueAnimator2);
                }
            });
            this.mCameraProtectionAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.ScreenDecorations.DisplayCutoutView.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    DisplayCutoutView.this.mCameraProtectionAnimator = null;
                    if (DisplayCutoutView.this.mShowProtection) {
                        return;
                    }
                    DisplayCutoutView.this.requestLayout();
                }
            });
            this.mCameraProtectionAnimator.start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$setShowProtection$1(ValueAnimator valueAnimator) {
            this.mCameraProtectionProgress = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            invalidate();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void update() {
            int i;
            if (!isAttachedToWindow() || this.mDecorations.mPendingRotationChange) {
                return;
            }
            this.mPosition = ScreenDecorations.getBoundPositionFromRotation(this.mInitialPosition, this.mRotation);
            requestLayout();
            getDisplay().getDisplayInfo(this.mInfo);
            this.mBounds.clear();
            this.mBoundingRect.setEmpty();
            this.mBoundingPath.reset();
            if (ScreenDecorations.shouldDrawCutout(getContext()) && hasCutout()) {
                this.mBounds.addAll(this.mInfo.displayCutout.getBoundingRects());
                localBounds(this.mBoundingRect);
                updateGravity();
                updateBoundingPath();
                invalidate();
                i = 0;
            } else {
                i = 8;
            }
            if (i != getVisibility()) {
                setVisibility(i);
            }
        }

        private void updateBoundingPath() {
            DisplayInfo displayInfo = this.mInfo;
            int i = displayInfo.logicalWidth;
            int i2 = displayInfo.logicalHeight;
            int i3 = displayInfo.rotation;
            boolean z = true;
            if (i3 != 1 && i3 != 3) {
                z = false;
            }
            int i4 = z ? i2 : i;
            if (!z) {
                i = i2;
            }
            Path pathPathFromResources = DisplayCutout.pathFromResources(getResources(), i4, i);
            if (pathPathFromResources != null) {
                this.mBoundingPath.set(pathPathFromResources);
            } else {
                this.mBoundingPath.reset();
            }
            Matrix matrix = new Matrix();
            transformPhysicalToLogicalCoordinates(this.mInfo.rotation, i4, i, matrix);
            this.mBoundingPath.transform(matrix);
            Path path = this.mProtectionPathOrig;
            if (path != null) {
                this.mProtectionPath.set(path);
                this.mProtectionPath.transform(matrix);
                matrix.mapRect(this.mProtectionRect, this.mProtectionRectOrig);
            }
        }

        private static void transformPhysicalToLogicalCoordinates(int i, int i2, int i3, Matrix matrix) {
            if (i == 0) {
                matrix.reset();
                return;
            }
            if (i == 1) {
                matrix.setRotate(270.0f);
                matrix.postTranslate(0.0f, i2);
                return;
            }
            if (i == 2) {
                matrix.setRotate(180.0f);
                matrix.postTranslate(i2, i3);
            } else if (i == 3) {
                matrix.setRotate(90.0f);
                matrix.postTranslate(i3, 0.0f);
            } else {
                throw new IllegalArgumentException("Unknown rotation: " + i);
            }
        }

        private void updateGravity() {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) layoutParams;
                int gravity = getGravity(this.mInfo.displayCutout);
                if (layoutParams2.gravity != gravity) {
                    layoutParams2.gravity = gravity;
                    setLayoutParams(layoutParams2);
                }
            }
        }

        private boolean hasCutout() {
            if (this.mInfo.displayCutout == null) {
                return false;
            }
            int i = this.mPosition;
            if (i == 0) {
                return !r0.getBoundingRectLeft().isEmpty();
            }
            if (i == 1) {
                return !r0.getBoundingRectTop().isEmpty();
            }
            if (i == 3) {
                return !r0.getBoundingRectBottom().isEmpty();
            }
            if (i == 2) {
                return !r0.getBoundingRectRight().isEmpty();
            }
            return false;
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            if (this.mBounds.isEmpty()) {
                super.onMeasure(i, i2);
                return;
            }
            if (this.mShowProtection) {
                this.mTotalBounds.union(this.mBoundingRect);
                Rect rect = this.mTotalBounds;
                RectF rectF = this.mProtectionRect;
                rect.union((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                setMeasuredDimension(View.resolveSizeAndState(this.mTotalBounds.width(), i, 0), View.resolveSizeAndState(this.mTotalBounds.height(), i2, 0));
                return;
            }
            setMeasuredDimension(View.resolveSizeAndState(this.mBoundingRect.width(), i, 0), View.resolveSizeAndState(this.mBoundingRect.height(), i2, 0));
        }

        public static void boundsFromDirection(DisplayCutout displayCutout, int i, Rect rect) {
            if (i == 3) {
                rect.set(displayCutout.getBoundingRectLeft());
                return;
            }
            if (i == 5) {
                rect.set(displayCutout.getBoundingRectRight());
                return;
            }
            if (i == 48) {
                rect.set(displayCutout.getBoundingRectTop());
            } else if (i == 80) {
                rect.set(displayCutout.getBoundingRectBottom());
            } else {
                rect.setEmpty();
            }
        }

        private void localBounds(Rect rect) {
            DisplayCutout displayCutout = this.mInfo.displayCutout;
            boundsFromDirection(displayCutout, getGravity(displayCutout), rect);
        }

        private int getGravity(DisplayCutout displayCutout) {
            int i = this.mPosition;
            return i == 0 ? !displayCutout.getBoundingRectLeft().isEmpty() ? 3 : 0 : i == 1 ? !displayCutout.getBoundingRectTop().isEmpty() ? 48 : 0 : i == 3 ? !displayCutout.getBoundingRectBottom().isEmpty() ? 80 : 0 : (i != 2 || displayCutout.getBoundingRectRight().isEmpty()) ? 0 : 5;
        }

        @Override // com.android.systemui.RegionInterceptingFrameLayout.RegionInterceptableView
        public boolean shouldInterceptTouch() {
            return this.mInfo.displayCutout != null && getVisibility() == 0;
        }

        @Override // com.android.systemui.RegionInterceptingFrameLayout.RegionInterceptableView
        public Region getInterceptRegion() {
            if (this.mInfo.displayCutout == null) {
                return null;
            }
            View rootView = getRootView();
            Region regionRectsToRegion = ScreenDecorations.rectsToRegion(this.mInfo.displayCutout.getBoundingRects());
            rootView.getLocationOnScreen(this.mLocation);
            int[] iArr = this.mLocation;
            regionRectsToRegion.translate(-iArr[0], -iArr[1]);
            regionRectsToRegion.op(rootView.getLeft(), rootView.getTop(), rootView.getRight(), rootView.getBottom(), Region.Op.INTERSECT);
            return regionRectsToRegion;
        }

        private class CustomSettingsObserver extends ContentObserver {
            CustomSettingsObserver(Handler handler) {
                super(handler);
            }

            void observe() {
                ((View) DisplayCutoutView.this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("display_cutout_mode"), false, this);
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri) {
                if (uri.equals(Settings.System.getUriFor("display_cutout_mode"))) {
                    DisplayCutoutView.this.update();
                }
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                DisplayCutoutView.this.update();
            }
        }
    }

    private class RestartingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final int mPosition;
        private final int mTargetRotation;
        private final View mView;

        private RestartingPreDrawListener(View view, int i, int i2) {
            this.mView = view;
            this.mTargetRotation = i2;
            this.mPosition = i;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            this.mView.getViewTreeObserver().removeOnPreDrawListener(this);
            if (this.mTargetRotation == ScreenDecorations.this.mRotation) {
                return true;
            }
            ScreenDecorations.this.mPendingRotationChange = false;
            ScreenDecorations.this.updateOrientation();
            this.mView.invalidate();
            return false;
        }
    }

    private class ValidatingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final View mView;

        public ValidatingPreDrawListener(View view) {
            this.mView = view;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            if (ScreenDecorations.this.mContext.getDisplay().getRotation() == ScreenDecorations.this.mRotation || ScreenDecorations.this.mPendingRotationChange) {
                return true;
            }
            this.mView.invalidate();
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: updateAllForCutout, reason: merged with bridge method [inline-methods] and merged with bridge method [inline-methods] */
    public void lambda$updateCutoutMode$5() {
        onTuningChanged("sysui_rounded_size", null);
        lambda$onFullscreenStateChanged$6();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCutoutMode() {
        boolean z = false;
        if (this.mRotation == 0 && this.mImmerseModeSetting == 1) {
            z = true;
        }
        if (this.mImmerseMode != z) {
            this.mImmerseMode = z;
            if (this.mOverlays != null) {
                if (!this.mHandler.getLooper().isCurrentThread()) {
                    this.mHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$updateCutoutMode$5();
                        }
                    });
                } else {
                    lambda$updateCutoutMode$5();
                }
            }
        }
    }

    private class CustomSettingsObserver extends ContentObserver {
        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ScreenDecorations.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("display_cutout_mode"), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (uri.equals(Settings.System.getUriFor("display_cutout_mode"))) {
                update();
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            update();
        }

        public void update() {
            ScreenDecorations screenDecorations = ScreenDecorations.this;
            screenDecorations.mImmerseModeSetting = Settings.System.getIntForUser(screenDecorations.mContext.getContentResolver(), "display_cutout_mode", 0, -2);
            ScreenDecorations.this.updateCutoutMode();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onFullscreenStateChanged(boolean z, boolean z2) {
        if (z == this.mFullscreenMode) {
            return;
        }
        this.mFullscreenMode = z;
        if (!this.mHandler.getLooper().isCurrentThread()) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.ScreenDecorations$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onFullscreenStateChanged$6();
                }
            });
        } else {
            lambda$onFullscreenStateChanged$6();
        }
    }
}
