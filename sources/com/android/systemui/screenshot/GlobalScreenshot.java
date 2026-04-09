package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MathUtils;
import android.view.Choreographer;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.R;
import com.android.systemui.screenshot.ScreenshotSelectorView;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.TaskStackChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class GlobalScreenshot implements ViewTreeObserver.OnComputeInternalInsetsListener {
    private HorizontalScrollView mActionsContainer;
    private ImageView mActionsContainerBackground;
    private LinearLayout mActionsView;
    private AudioManager mAudioManager;
    private ImageView mBackgroundProtection;
    private MediaActionSound mCameraSound;
    private View mCancelButton;
    private View mCaptureButton;
    private final Context mContext;
    private float mCornerSizeX;
    private boolean mDirectionLTR;
    private Animator mDismissAnimation;
    private FrameLayout mDismissButton;
    private float mDismissDeltaY;
    private final Display mDisplay;
    private final DisplayMetrics mDisplayMetrics;
    private final Interpolator mFastOutSlowIn;
    private SavedImageData mImageData;
    private boolean mInDarkMode;
    private int mLeftInset;
    private int mNavMode;
    private final ScreenshotNotificationsController mNotificationsController;
    private Runnable mOnCompleteRunnable;
    private boolean mOrientationPortrait;
    private PackageManager mPm;
    private int mRightInset;
    private SaveImageInBackgroundTask mSaveInBgTask;
    private Bitmap mScreenBitmap;
    private ImageView mScreenshotAnimatedView;
    private Animator mScreenshotAnimation;
    private LinearLayout mScreenshotButtonsLayout;
    private ImageView mScreenshotFlash;
    private View mScreenshotLayout;
    private ImageView mScreenshotPreview;
    private ScreenshotSelectorView mScreenshotSelectorView;
    private final ScreenshotSmartActions mScreenshotSmartActions;
    private ComponentName mTaskComponentName;
    private final TaskStackChangeListener mTaskListener;
    private final Executor mUiBgExecutor;
    private final UiEventLogger mUiEventLogger;
    private Vibrator mVibrator;
    private final WindowManager.LayoutParams mWindowLayoutParams;
    private final WindowManager mWindowManager;
    private final Interpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private final Handler mScreenshotHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.systemui.screenshot.GlobalScreenshot.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what != 2) {
                return;
            }
            GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_INTERACTION_TIMEOUT);
            if (GlobalScreenshot.this.mImageData != null) {
                GlobalScreenshot.this.mNotificationsController.showSilentScreenshotNotification(GlobalScreenshot.this.mImageData);
            }
            GlobalScreenshot.this.dismissScreenshot("timeout", false);
            GlobalScreenshot.this.mOnCompleteRunnable.run();
        }
    };

    static class SaveImageInBackgroundData {
        public String appLabel;
        public int errorMsgResId;
        public Consumer<Uri> finisher;
        public Bitmap image;
        public ActionsReadyListener mActionsReadyListener;

        SaveImageInBackgroundData() {
        }

        void clearImage() {
            this.image = null;
        }
    }

    static class SavedImageData {
        public Notification.Action deleteAction;
        public Notification.Action editAction;
        public Notification.Action shareAction;
        public List<Notification.Action> smartActions;
        public Uri uri;

        SavedImageData() {
        }

        public void reset() {
            this.uri = null;
            this.shareAction = null;
            this.editAction = null;
            this.deleteAction = null;
            this.smartActions = null;
        }
    }

    static abstract class ActionsReadyListener {
        abstract void onActionsReady(SavedImageData savedImageData);

        ActionsReadyListener() {
        }
    }

    /* renamed from: com.android.systemui.screenshot.GlobalScreenshot$2, reason: invalid class name */
    class AnonymousClass2 extends TaskStackChangeListener {
        AnonymousClass2() {
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            GlobalScreenshot.this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$2$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onTaskStackChanged$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onTaskStackChanged$0() {
            ComponentName componentName;
            try {
                ActivityManager.StackInfo focusedStackInfo = ActivityTaskManager.getService().getFocusedStackInfo();
                if (focusedStackInfo == null || (componentName = focusedStackInfo.topActivity) == null) {
                    return;
                }
                GlobalScreenshot.this.mTaskComponentName = componentName;
            } catch (Exception unused) {
            }
        }
    }

    private String getForegroundAppLabel() {
        try {
            return this.mPm.getActivityInfo(this.mTaskComponentName, 0).applicationInfo.loadLabel(this.mPm).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    public GlobalScreenshot(Context context, Resources resources, ScreenshotSmartActions screenshotSmartActions, ScreenshotNotificationsController screenshotNotificationsController, UiEventLogger uiEventLogger, Executor executor) {
        AnonymousClass2 anonymousClass2 = new AnonymousClass2();
        this.mTaskListener = anonymousClass2;
        this.mContext = context;
        this.mScreenshotSmartActions = screenshotSmartActions;
        this.mNotificationsController = screenshotNotificationsController;
        this.mUiEventLogger = uiEventLogger;
        reloadAssets();
        Configuration configuration = context.getResources().getConfiguration();
        this.mInDarkMode = configuration.isNightModeActive();
        this.mDirectionLTR = configuration.getLayoutDirection() == 0;
        this.mOrientationPortrait = configuration.orientation == 1;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2036, 918816, -3);
        this.mWindowLayoutParams = layoutParams;
        layoutParams.setTitle("ScreenshotAnimation");
        layoutParams.layoutInDisplayCutoutMode = 3;
        layoutParams.setFitInsetsTypes(0);
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        this.mWindowManager = windowManager;
        Display defaultDisplay = windowManager.getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mDisplayMetrics = displayMetrics;
        defaultDisplay.getRealMetrics(displayMetrics);
        this.mCornerSizeX = resources.getDimensionPixelSize(R.dimen.global_screenshot_x_scale);
        this.mDismissDeltaY = resources.getDimensionPixelSize(R.dimen.screenshot_dismissal_height_delta);
        this.mFastOutSlowIn = AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in);
        MediaActionSound mediaActionSound = new MediaActionSound();
        this.mCameraSound = mediaActionSound;
        mediaActionSound.load(0);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mUiBgExecutor = executor;
        this.mPm = context.getPackageManager();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(anonymousClass2);
        anonymousClass2.onTaskStackChanged();
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        Region region = new Region();
        Rect rect = new Rect();
        this.mScreenshotPreview.getBoundsOnScreen(rect);
        region.op(rect, Region.Op.UNION);
        Rect rect2 = new Rect();
        this.mActionsContainer.getBoundsOnScreen(rect2);
        region.op(rect2, Region.Op.UNION);
        Rect rect3 = new Rect();
        this.mDismissButton.getBoundsOnScreen(rect3);
        region.op(rect3, Region.Op.UNION);
        if (QuickStepContract.isGesturalMode(this.mNavMode)) {
            Rect rect4 = new Rect(0, 0, this.mLeftInset, this.mDisplayMetrics.heightPixels);
            region.op(rect4, Region.Op.UNION);
            DisplayMetrics displayMetrics = this.mDisplayMetrics;
            int i = displayMetrics.widthPixels;
            rect4.set(i - this.mRightInset, 0, i, displayMetrics.heightPixels);
            region.op(rect4, Region.Op.UNION);
        }
        internalInsetsInfo.touchableRegion.set(region);
    }

    void takeScreenshotFullscreen(Consumer<Uri> consumer, Runnable runnable) {
        this.mOnCompleteRunnable = runnable;
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        DisplayMetrics displayMetrics = this.mDisplayMetrics;
        takeScreenshotInternal(consumer, new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels));
    }

    void handleImageAsScreenshot(Bitmap bitmap, Rect rect, Insets insets, int i, int i2, ComponentName componentName, Consumer<Uri> consumer, Runnable runnable) throws Resources.NotFoundException {
        this.mOnCompleteRunnable = runnable;
        if (aspectRatiosMatch(bitmap, insets, rect)) {
            saveScreenshot(bitmap, consumer, rect, insets, false);
        } else {
            saveScreenshot(bitmap, consumer, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), Insets.NONE, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: hideScreenshotSelector, reason: merged with bridge method [inline-methods] */
    public void lambda$takeScreenshotPartial$4() {
        setLockedScreenOrientation(false);
        if (this.mScreenshotLayout.getWindowToken() != null) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        this.mScreenshotSelectorView.stopSelection();
        this.mScreenshotSelectorView.setVisibility(8);
        this.mCaptureButton.setVisibility(8);
        setBlockedGesturalNavigation(false);
    }

    void setBlockedGesturalNavigation(boolean z) {
        IStatusBarService iStatusBarServiceAsInterface = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        if (iStatusBarServiceAsInterface != null) {
            try {
                iStatusBarServiceAsInterface.setBlockedGesturalNavigation(z);
            } catch (RemoteException unused) {
            }
        }
    }

    void setLockedScreenOrientation(boolean z) {
        this.mWindowLayoutParams.screenOrientation = z ? 14 : -1;
    }

    Rect getRotationAdjustedRect(Rect rect) {
        Display defaultDisplay = this.mWindowManager.getDefaultDisplay();
        Rect rect2 = new Rect(rect);
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        int rotation = defaultDisplay.getRotation();
        if (rotation != 0) {
            if (rotation == 1) {
                int i = this.mDisplayMetrics.heightPixels;
                rect2.top = i - rect.bottom;
                rect2.bottom = i - rect.top;
            } else if (rotation == 2) {
                DisplayMetrics displayMetrics = this.mDisplayMetrics;
                int i2 = displayMetrics.widthPixels;
                rect2.left = i2 - rect.right;
                int i3 = displayMetrics.heightPixels;
                rect2.top = i3 - rect.bottom;
                rect2.right = i2 - rect.left;
                rect2.bottom = i3 - rect.top;
            } else if (rotation == 3) {
                int i4 = this.mDisplayMetrics.widthPixels;
                rect2.left = i4 - rect.right;
                rect2.right = i4 - rect.left;
            } else {
                throw new IllegalArgumentException("Unknown rotation: " + rotation);
            }
        }
        return rect2;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    void takeScreenshotPartial(final Consumer<Uri> consumer, Runnable runnable) {
        if (this.mScreenshotLayout.getParent() != null) {
            consumer.accept(null);
            return;
        }
        dismissScreenshot("new screenshot requested", true);
        this.mOnCompleteRunnable = runnable;
        setBlockedGesturalNavigation(true);
        setLockedScreenOrientation(true);
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        this.mScreenshotSelectorView.setSelectionListener(new ScreenshotSelectorView.OnSelectionListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda15
            @Override // com.android.systemui.screenshot.ScreenshotSelectorView.OnSelectionListener
            public final void onSelectionChanged(Rect rect, boolean z) {
                this.f$0.lambda$takeScreenshotPartial$1(rect, z);
            }
        });
        this.mCancelButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$takeScreenshotPartial$3(consumer, view);
            }
        });
        this.mCaptureButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$takeScreenshotPartial$5(consumer, view);
            }
        });
        this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda22
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$takeScreenshotPartial$6();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotPartial$0() {
        this.mCaptureButton.setVisibility(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotPartial$1(Rect rect, boolean z) {
        if (z) {
            this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda21
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$takeScreenshotPartial$0();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotPartial$3(final Consumer consumer, View view) {
        this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda27
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$takeScreenshotPartial$2(consumer);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotPartial$2(Consumer consumer) {
        consumer.accept(null);
        lambda$takeScreenshotPartial$4();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotPartial$5(final Consumer consumer, View view) {
        final Rect rotationAdjustedRect = getRotationAdjustedRect(this.mScreenshotSelectorView.getSelectionRect());
        this.mScreenshotButtonsLayout.getLayoutTransition().addTransitionListener(new LayoutTransition.TransitionListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.3
            @Override // android.animation.LayoutTransition.TransitionListener
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view2, int i) {
            }

            @Override // android.animation.LayoutTransition.TransitionListener
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view2, int i) {
                GlobalScreenshot.this.takeScreenshotInternal(consumer, rotationAdjustedRect);
                layoutTransition.removeTransitionListener(this);
            }
        });
        this.mScreenshotLayout.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda18
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$takeScreenshotPartial$4();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotPartial$6() {
        this.mScreenshotSelectorView.setVisibility(0);
        this.mScreenshotSelectorView.requestFocus();
    }

    void stopScreenshot() {
        if (this.mScreenshotLayout.getParent() != null) {
            lambda$takeScreenshotPartial$4();
        }
    }

    void dismissScreenshot(String str, boolean z) {
        Log.v("GlobalScreenshot", "clearing screenshot: " + str);
        this.mScreenshotHandler.removeMessages(2);
        this.mScreenshotLayout.getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        if (!z) {
            AnimatorSet animatorSetCreateScreenshotDismissAnimation = createScreenshotDismissAnimation();
            this.mDismissAnimation = animatorSetCreateScreenshotDismissAnimation;
            animatorSetCreateScreenshotDismissAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    GlobalScreenshot.this.clearScreenshot();
                }
            });
            this.mDismissAnimation.start();
            return;
        }
        clearScreenshot();
    }

    /* JADX WARN: Removed duplicated region for block: B:34:0x0045  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void onConfigChanged(android.content.res.Configuration r5) {
        /*
            r4 = this;
            boolean r0 = r5.isNightModeActive()
            r1 = 0
            r2 = 1
            if (r0 == 0) goto Lf
            boolean r0 = r4.mInDarkMode
            if (r0 != 0) goto L17
            r4.mInDarkMode = r2
            goto L15
        Lf:
            boolean r0 = r4.mInDarkMode
            if (r0 == 0) goto L17
            r4.mInDarkMode = r1
        L15:
            r0 = r2
            goto L18
        L17:
            r0 = r1
        L18:
            int r3 = r5.getLayoutDirection()
            if (r3 == 0) goto L28
            if (r3 == r2) goto L21
            goto L2f
        L21:
            boolean r3 = r4.mDirectionLTR
            if (r3 == 0) goto L2f
            r4.mDirectionLTR = r1
            goto L2e
        L28:
            boolean r3 = r4.mDirectionLTR
            if (r3 != 0) goto L2f
            r4.mDirectionLTR = r2
        L2e:
            r0 = r2
        L2f:
            int r5 = r5.orientation
            if (r5 == r2) goto L3e
            r3 = 2
            if (r5 == r3) goto L37
            goto L45
        L37:
            boolean r5 = r4.mOrientationPortrait
            if (r5 == 0) goto L45
            r4.mOrientationPortrait = r1
            goto L46
        L3e:
            boolean r5 = r4.mOrientationPortrait
            if (r5 != 0) goto L45
            r4.mOrientationPortrait = r2
            goto L46
        L45:
            r2 = r0
        L46:
            if (r2 == 0) goto L4b
            r4.reloadAssets()
        L4b:
            android.content.Context r5 = r4.mContext
            android.content.res.Resources r5 = r5.getResources()
            r0 = 17694850(0x10e0082, float:2.6081645E-38)
            int r5 = r5.getInteger(r0)
            r4.mNavMode = r5
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenshot.GlobalScreenshot.onConfigChanged(android.content.res.Configuration):void");
    }

    private void reloadAssets() {
        View view = this.mScreenshotLayout;
        boolean z = view != null && view.isAttachedToWindow();
        if (z) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        View viewInflate = LayoutInflater.from(this.mContext).inflate(R.layout.global_screenshot, (ViewGroup) null);
        this.mScreenshotLayout = viewInflate;
        this.mScreenshotButtonsLayout = (LinearLayout) viewInflate.findViewById(R.id.global_screenshot_buttons);
        this.mScreenshotLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda14
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return this.f$0.lambda$reloadAssets$7(view2, motionEvent);
            }
        });
        this.mScreenshotLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda8
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                return this.f$0.lambda$reloadAssets$8(view2, windowInsets);
            }
        });
        this.mScreenshotLayout.setOnKeyListener(new View.OnKeyListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda13
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view2, int i, KeyEvent keyEvent) {
                return this.f$0.lambda$reloadAssets$9(view2, i, keyEvent);
            }
        });
        this.mScreenshotLayout.setFocusableInTouchMode(true);
        this.mScreenshotLayout.requestFocus();
        ImageView imageView = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_animated_view);
        this.mScreenshotAnimatedView = imageView;
        imageView.setClipToOutline(true);
        this.mScreenshotAnimatedView.setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.screenshot.GlobalScreenshot.5
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view2, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view2.getWidth(), view2.getHeight()), view2.getWidth() * 0.05f);
            }
        });
        ImageView imageView2 = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_preview);
        this.mScreenshotPreview = imageView2;
        imageView2.setClipToOutline(true);
        this.mScreenshotPreview.setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.screenshot.GlobalScreenshot.6
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view2, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view2.getWidth(), view2.getHeight()), view2.getWidth() * 0.05f);
            }
        });
        this.mActionsContainerBackground = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_actions_container_background);
        this.mActionsContainer = (HorizontalScrollView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_actions_container);
        this.mActionsView = (LinearLayout) this.mScreenshotLayout.findViewById(R.id.global_screenshot_actions);
        this.mBackgroundProtection = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_actions_background);
        this.mCaptureButton = this.mScreenshotLayout.findViewById(R.id.global_screenshot_selector_capture);
        this.mCancelButton = this.mScreenshotLayout.findViewById(R.id.global_screenshot_selector_cancel);
        FrameLayout frameLayout = (FrameLayout) this.mScreenshotLayout.findViewById(R.id.global_screenshot_dismiss_button);
        this.mDismissButton = frameLayout;
        frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.lambda$reloadAssets$10(view2);
            }
        });
        this.mScreenshotFlash = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_flash);
        this.mScreenshotSelectorView = (ScreenshotSelectorView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_selector);
        this.mScreenshotLayout.setFocusable(true);
        this.mScreenshotSelectorView.setFocusable(true);
        this.mScreenshotSelectorView.setFocusableInTouchMode(true);
        this.mScreenshotAnimatedView.setPivotX(0.0f);
        this.mScreenshotAnimatedView.setPivotY(0.0f);
        this.mActionsContainer.setScrollX(0);
        if (z) {
            this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$reloadAssets$7(View view, MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 4) {
            setWindowFocusable(false);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ WindowInsets lambda$reloadAssets$8(View view, WindowInsets windowInsets) {
        if (QuickStepContract.isGesturalMode(this.mNavMode)) {
            Insets insets = windowInsets.getInsets(WindowInsets.Type.systemGestures());
            this.mLeftInset = insets.left;
            this.mRightInset = insets.right;
        } else {
            this.mRightInset = 0;
            this.mLeftInset = 0;
        }
        return this.mScreenshotLayout.onApplyWindowInsets(windowInsets);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$reloadAssets$9(View view, int i, KeyEvent keyEvent) {
        if (i != 4) {
            return false;
        }
        dismissScreenshot("back pressed", false);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$reloadAssets$10(View view) {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_EXPLICIT_DISMISSAL);
        SavedImageData savedImageData = this.mImageData;
        if (savedImageData != null) {
            this.mNotificationsController.showSilentScreenshotNotification(savedImageData);
        }
        dismissScreenshot("dismiss_button", false);
        this.mOnCompleteRunnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void takeScreenshotInternal(final Consumer<Uri> consumer, final Rect rect) {
        if (this.mScreenshotLayout.getParent() != null) {
            consumer.accept(null);
            return;
        }
        dismissScreenshot("new screenshot requested", true);
        this.mScreenshotLayout.getRootView().invalidate();
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda6
            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j) {
                this.f$0.lambda$takeScreenshotInternal$12(rect, consumer, j);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotInternal$12(final Rect rect, final Consumer consumer, long j) {
        this.mScreenshotLayout.getRootView().invalidate();
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda7
            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j2) throws Resources.NotFoundException {
                this.f$0.lambda$takeScreenshotInternal$11(rect, consumer, j2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$takeScreenshotInternal$11(Rect rect, Consumer consumer, long j) throws Resources.NotFoundException {
        saveScreenshot(SurfaceControl.screenshot(rect, rect.width(), rect.height(), this.mDisplay.getRotation()), consumer, new Rect(rect), Insets.NONE, true);
    }

    private void saveScreenshot(Bitmap bitmap, Consumer<Uri> consumer, Rect rect, Insets insets, boolean z) throws Resources.NotFoundException {
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            Animator animator = this.mDismissAnimation;
            if (animator == null || !animator.isRunning()) {
                this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_REENTERED);
            }
            dismissScreenshot("new screenshot requested", true);
        }
        this.mScreenBitmap = bitmap;
        if (bitmap == null) {
            this.mNotificationsController.notifyScreenshotError(R.string.screenshot_failed_to_capture_text);
            consumer.accept(null);
            this.mOnCompleteRunnable.run();
        } else {
            if (!isUserSetupComplete()) {
                saveScreenshotAndToast(consumer);
                return;
            }
            this.mScreenBitmap.setHasAlpha(false);
            this.mScreenBitmap.prepareToDraw();
            onConfigChanged(this.mContext.getResources().getConfiguration());
            Animator animator2 = this.mDismissAnimation;
            if (animator2 != null && animator2.isRunning()) {
                this.mDismissAnimation.cancel();
            }
            setWindowFocusable(true);
            startAnimation(consumer, rect, insets, z);
        }
    }

    private void saveScreenshotAndToast(Consumer<Uri> consumer) {
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screenshot_sound", 1, -2) != 0) {
            this.mScreenshotHandler.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda19
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$saveScreenshotAndToast$13();
                }
            });
        }
        saveScreenshotInWorkerThread(consumer, new AnonymousClass7(consumer));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$saveScreenshotAndToast$13() {
        int ringerMode = this.mAudioManager.getRingerMode();
        if (ringerMode != 1) {
            if (ringerMode != 2) {
                return;
            }
            this.mCameraSound.play(0);
        } else {
            Vibrator vibrator = this.mVibrator;
            if (vibrator == null || !vibrator.hasVibrator()) {
                return;
            }
            this.mVibrator.vibrate(VibrationEffect.createOneShot(50L, -1));
        }
    }

    /* renamed from: com.android.systemui.screenshot.GlobalScreenshot$7, reason: invalid class name */
    class AnonymousClass7 extends ActionsReadyListener {
        final /* synthetic */ Consumer val$finisher;

        AnonymousClass7(Consumer consumer) {
            this.val$finisher = consumer;
        }

        @Override // com.android.systemui.screenshot.GlobalScreenshot.ActionsReadyListener
        void onActionsReady(SavedImageData savedImageData) throws Resources.NotFoundException {
            this.val$finisher.accept(savedImageData.uri);
            if (savedImageData.uri == null) {
                GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_NOT_SAVED);
                GlobalScreenshot.this.mNotificationsController.notifyScreenshotError(R.string.screenshot_failed_to_capture_text);
            } else {
                GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SAVED);
                GlobalScreenshot.this.mScreenshotHandler.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$7$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onActionsReady$0();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActionsReady$0() {
            Toast.makeText(GlobalScreenshot.this.mContext, R.string.screenshot_saved_title, 0).show();
        }
    }

    private void startAnimation(final Consumer<Uri> consumer, final Rect rect, final Insets insets, final boolean z) {
        this.mScreenshotHandler.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda24
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startAnimation$15(insets, rect, z, consumer);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startAnimation$15(Insets insets, final Rect rect, final boolean z, final Consumer consumer) {
        if (!this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
        this.mScreenshotAnimatedView.setImageDrawable(createScreenDrawable(this.mScreenBitmap, insets));
        setAnimatedViewSize(rect.width(), rect.height());
        this.mScreenshotAnimatedView.setVisibility(8);
        this.mScreenshotPreview.setImageDrawable(createScreenDrawable(this.mScreenBitmap, insets));
        this.mScreenshotPreview.setVisibility(4);
        this.mScreenshotHandler.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda25
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startAnimation$14(rect, z, consumer);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startAnimation$14(Rect rect, boolean z, Consumer consumer) {
        this.mScreenshotLayout.getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        this.mScreenshotAnimation = createScreenshotDropInAnimation(rect, z);
        saveScreenshotInWorkerThread(consumer, new ActionsReadyListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.8
            @Override // com.android.systemui.screenshot.GlobalScreenshot.ActionsReadyListener
            void onActionsReady(SavedImageData savedImageData) throws Resources.NotFoundException {
                GlobalScreenshot.this.showUiOnActionsReady(savedImageData);
            }
        });
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screenshot_sound", 1, -2) != 0) {
            int ringerMode = this.mAudioManager.getRingerMode();
            if (ringerMode == 1) {
                Vibrator vibrator = this.mVibrator;
                if (vibrator != null && vibrator.hasVibrator()) {
                    this.mVibrator.vibrate(VibrationEffect.createOneShot(50L, -1));
                }
            } else if (ringerMode == 2) {
                this.mCameraSound.play(0);
            }
        }
        this.mScreenshotPreview.setLayerType(2, null);
        this.mScreenshotPreview.buildLayer();
        this.mScreenshotAnimation.start();
    }

    private void saveScreenshotInWorkerThread(Consumer<Uri> consumer, ActionsReadyListener actionsReadyListener) {
        SaveImageInBackgroundData saveImageInBackgroundData = new SaveImageInBackgroundData();
        saveImageInBackgroundData.image = this.mScreenBitmap;
        saveImageInBackgroundData.finisher = consumer;
        saveImageInBackgroundData.mActionsReadyListener = actionsReadyListener;
        saveImageInBackgroundData.appLabel = getForegroundAppLabel();
        SaveImageInBackgroundTask saveImageInBackgroundTask = this.mSaveInBgTask;
        if (saveImageInBackgroundTask != null) {
            saveImageInBackgroundTask.setActionsReadyListener(new ActionsReadyListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.9
                @Override // com.android.systemui.screenshot.GlobalScreenshot.ActionsReadyListener
                void onActionsReady(SavedImageData savedImageData) throws Resources.NotFoundException {
                    GlobalScreenshot.this.logSuccessOnActionsReady(savedImageData);
                }
            });
        }
        this.mImageData = null;
        this.mNotificationsController.reset();
        this.mNotificationsController.setImage(this.mScreenBitmap);
        SaveImageInBackgroundTask saveImageInBackgroundTask2 = new SaveImageInBackgroundTask(this.mContext, this.mScreenshotSmartActions, saveImageInBackgroundData);
        this.mSaveInBgTask = saveImageInBackgroundTask2;
        saveImageInBackgroundTask2.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showUiOnActionsReady(final SavedImageData savedImageData) throws Resources.NotFoundException {
        logSuccessOnActionsReady(savedImageData);
        this.mImageData = savedImageData;
        long recommendedTimeoutMillis = ((AccessibilityManager) this.mContext.getSystemService("accessibility")).getRecommendedTimeoutMillis(3000, 4);
        this.mScreenshotHandler.removeMessages(2);
        Handler handler = this.mScreenshotHandler;
        handler.sendMessageDelayed(handler.obtainMessage(2), recommendedTimeoutMillis);
        if (savedImageData.uri != null) {
            this.mScreenshotHandler.post(new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda26
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showUiOnActionsReady$16(savedImageData);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showUiOnActionsReady$16(final SavedImageData savedImageData) {
        Animator animator = this.mScreenshotAnimation;
        if (animator != null && animator.isRunning()) {
            this.mScreenshotAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.10
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator2) {
                    super.onAnimationEnd(animator2);
                    GlobalScreenshot.this.createScreenshotActionsShadeAnimation(savedImageData).start();
                }
            });
        } else {
            createScreenshotActionsShadeAnimation(savedImageData).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logSuccessOnActionsReady(SavedImageData savedImageData) throws Resources.NotFoundException {
        if (savedImageData.uri == null) {
            this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_NOT_SAVED);
            this.mNotificationsController.notifyScreenshotError(R.string.screenshot_failed_to_capture_text);
        } else {
            this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SAVED);
        }
    }

    private AnimatorSet createScreenshotDropInAnimation(final Rect rect, boolean z) {
        this.mScreenshotPreview.getBoundsOnScreen(new Rect());
        final float fWidth = this.mCornerSizeX / (this.mOrientationPortrait ? rect.width() : rect.height());
        this.mScreenshotAnimatedView.setScaleX(1.0f);
        this.mScreenshotAnimatedView.setScaleY(1.0f);
        this.mDismissButton.setAlpha(0.0f);
        this.mDismissButton.setVisibility(0);
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(133L);
        valueAnimatorOfFloat.setInterpolator(this.mFastOutSlowIn);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$createScreenshotDropInAnimation$17(valueAnimator);
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat2.setDuration(217L);
        valueAnimatorOfFloat2.setInterpolator(this.mFastOutSlowIn);
        valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$createScreenshotDropInAnimation$18(valueAnimator);
            }
        });
        final PointF pointF = new PointF(rect.centerX(), rect.centerY());
        final PointF pointF2 = new PointF(r0.centerX(), r0.centerY());
        ValueAnimator valueAnimatorOfFloat3 = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat3.setDuration(500L);
        final float f = 0.468f;
        final float f2 = 0.4f;
        final float f3 = 0.468f;
        valueAnimatorOfFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$createScreenshotDropInAnimation$19(f3, fWidth, f, pointF, pointF2, rect, f2, valueAnimator);
            }
        });
        valueAnimatorOfFloat3.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.11
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                GlobalScreenshot.this.mScreenshotAnimatedView.setVisibility(0);
            }
        });
        this.mScreenshotFlash.setAlpha(0.0f);
        this.mScreenshotFlash.setVisibility(0);
        if (z) {
            animatorSet.play(valueAnimatorOfFloat2).after(valueAnimatorOfFloat);
            animatorSet.play(valueAnimatorOfFloat2).with(valueAnimatorOfFloat3);
        } else {
            animatorSet.play(valueAnimatorOfFloat3);
        }
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.12
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                float fWidth2;
                super.onAnimationEnd(animator);
                GlobalScreenshot.this.mDismissButton.setAlpha(1.0f);
                float width = GlobalScreenshot.this.mDismissButton.getWidth() / 2.0f;
                if (GlobalScreenshot.this.mDirectionLTR) {
                    fWidth2 = (pointF2.x - width) + ((rect.width() * fWidth) / 2.0f);
                } else {
                    fWidth2 = (pointF2.x - width) - ((rect.width() * fWidth) / 2.0f);
                }
                GlobalScreenshot.this.mDismissButton.setX(fWidth2);
                GlobalScreenshot.this.mDismissButton.setY((pointF2.y - width) - ((rect.height() * fWidth) / 2.0f));
                GlobalScreenshot.this.mScreenshotAnimatedView.setScaleX(1.0f);
                GlobalScreenshot.this.mScreenshotAnimatedView.setScaleY(1.0f);
                GlobalScreenshot.this.mScreenshotAnimatedView.setX(pointF2.x - ((rect.width() * fWidth) / 2.0f));
                GlobalScreenshot.this.mScreenshotAnimatedView.setY(pointF2.y - ((rect.height() * fWidth) / 2.0f));
                GlobalScreenshot.this.mScreenshotAnimatedView.setVisibility(8);
                GlobalScreenshot.this.mScreenshotPreview.setVisibility(0);
                GlobalScreenshot.this.mScreenshotLayout.forceLayout();
            }
        });
        return animatorSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotDropInAnimation$17(ValueAnimator valueAnimator) {
        this.mScreenshotFlash.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotDropInAnimation$18(ValueAnimator valueAnimator) {
        this.mScreenshotFlash.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotDropInAnimation$19(float f, float f2, float f3, PointF pointF, PointF pointF2, Rect rect, float f4, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (animatedFraction < f) {
            float fLerp = MathUtils.lerp(1.0f, f2, this.mFastOutSlowIn.getInterpolation(animatedFraction / f));
            this.mScreenshotAnimatedView.setScaleX(fLerp);
            this.mScreenshotAnimatedView.setScaleY(fLerp);
        } else {
            this.mScreenshotAnimatedView.setScaleX(f2);
            this.mScreenshotAnimatedView.setScaleY(f2);
        }
        float scaleX = this.mScreenshotAnimatedView.getScaleX();
        float scaleY = this.mScreenshotAnimatedView.getScaleY();
        if (animatedFraction < f3) {
            this.mScreenshotAnimatedView.setX(MathUtils.lerp(pointF.x, pointF2.x, this.mFastOutSlowIn.getInterpolation(animatedFraction / f3)) - ((rect.width() * scaleX) / 2.0f));
        } else {
            this.mScreenshotAnimatedView.setX(pointF2.x - ((rect.width() * scaleX) / 2.0f));
        }
        this.mScreenshotAnimatedView.setY(MathUtils.lerp(pointF.y, pointF2.y, this.mFastOutSlowIn.getInterpolation(animatedFraction)) - ((rect.height() * scaleY) / 2.0f));
        if (animatedFraction >= f4) {
            this.mDismissButton.setAlpha((animatedFraction - f4) / (1.0f - f4));
            float x = this.mScreenshotAnimatedView.getX();
            float y = this.mScreenshotAnimatedView.getY();
            this.mDismissButton.setY(y - (r9.getHeight() / 2.0f));
            if (this.mDirectionLTR) {
                this.mDismissButton.setX((x + (rect.width() * scaleX)) - (this.mDismissButton.getWidth() / 2.0f));
            } else {
                this.mDismissButton.setX(x - (r5.getWidth() / 2.0f));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ValueAnimator createScreenshotActionsShadeAnimation(final SavedImageData savedImageData) {
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(this.mContext);
        this.mActionsView.removeAllViews();
        this.mScreenshotLayout.invalidate();
        this.mScreenshotLayout.requestLayout();
        this.mScreenshotLayout.getViewTreeObserver().dispatchOnGlobalLayout();
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        final ArrayList arrayList = new ArrayList();
        for (Notification.Action action : savedImageData.smartActions) {
            ScreenshotActionChip screenshotActionChip = (ScreenshotActionChip) layoutInflaterFrom.inflate(R.layout.global_screenshot_action_chip, (ViewGroup) this.mActionsView, false);
            screenshotActionChip.setText(action.title);
            screenshotActionChip.setIcon(action.getIcon(), false);
            screenshotActionChip.setPendingIntent(action.actionIntent, new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda16
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$createScreenshotActionsShadeAnimation$20();
                }
            });
            this.mActionsView.addView(screenshotActionChip);
            arrayList.add(screenshotActionChip);
        }
        int i = R.layout.global_screenshot_action_chip;
        ScreenshotActionChip screenshotActionChip2 = (ScreenshotActionChip) layoutInflaterFrom.inflate(i, (ViewGroup) this.mActionsView, false);
        screenshotActionChip2.setText(savedImageData.shareAction.title);
        screenshotActionChip2.setIcon(savedImageData.shareAction.getIcon(), true);
        screenshotActionChip2.setPendingIntent(savedImageData.shareAction.actionIntent, new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda23
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$createScreenshotActionsShadeAnimation$21();
            }
        });
        this.mActionsView.addView(screenshotActionChip2);
        arrayList.add(screenshotActionChip2);
        ScreenshotActionChip screenshotActionChip3 = (ScreenshotActionChip) layoutInflaterFrom.inflate(i, (ViewGroup) this.mActionsView, false);
        screenshotActionChip3.setText(savedImageData.editAction.title);
        screenshotActionChip3.setIcon(savedImageData.editAction.getIcon(), true);
        screenshotActionChip3.setPendingIntent(savedImageData.editAction.actionIntent, new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$createScreenshotActionsShadeAnimation$22();
            }
        });
        this.mActionsView.addView(screenshotActionChip3);
        arrayList.add(screenshotActionChip3);
        ScreenshotActionChip screenshotActionChip4 = (ScreenshotActionChip) layoutInflaterFrom.inflate(i, (ViewGroup) this.mActionsView, false);
        screenshotActionChip4.setText(savedImageData.deleteAction.title);
        screenshotActionChip4.setIcon(savedImageData.deleteAction.getIcon(), true);
        screenshotActionChip4.setPendingIntent(savedImageData.deleteAction.actionIntent, new Runnable() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$createScreenshotActionsShadeAnimation$23();
            }
        });
        this.mActionsView.addView(screenshotActionChip4);
        arrayList.add(screenshotActionChip4);
        this.mScreenshotPreview.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws PendingIntent.CanceledException {
                this.f$0.lambda$createScreenshotActionsShadeAnimation$24(savedImageData, view);
            }
        });
        this.mScreenshotPreview.setContentDescription(savedImageData.editAction.title);
        LinearLayout linearLayout = this.mActionsView;
        ((LinearLayout.LayoutParams) linearLayout.getChildAt(linearLayout.getChildCount() - 1).getLayoutParams()).setMarginEnd(0);
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(200L);
        final float f = 0.1f;
        this.mActionsContainer.setAlpha(0.0f);
        this.mActionsContainerBackground.setAlpha(0.0f);
        this.mActionsContainer.setVisibility(0);
        this.mActionsContainerBackground.setVisibility(0);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$createScreenshotActionsShadeAnimation$25(f, arrayList, valueAnimator);
            }
        });
        return valueAnimatorOfFloat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$20() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SMART_ACTION_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$21() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SHARE_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$22() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_EDIT_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$23() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_DELETE_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$24(SavedImageData savedImageData, View view) throws PendingIntent.CanceledException {
        try {
            savedImageData.editAction.actionIntent.send();
            this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_PREVIEW_TAPPED);
            dismissScreenshot("screenshot preview tapped", false);
            this.mOnCompleteRunnable.run();
        } catch (PendingIntent.CanceledException e) {
            Log.e("GlobalScreenshot", "Intent cancelled", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$25(float f, ArrayList arrayList, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mBackgroundProtection.setAlpha(animatedFraction);
        float f2 = animatedFraction < f ? animatedFraction / f : 1.0f;
        this.mActionsContainer.setAlpha(f2);
        this.mActionsContainerBackground.setAlpha(f2);
        float f3 = (0.3f * animatedFraction) + 0.7f;
        this.mActionsContainer.setScaleX(f3);
        this.mActionsContainerBackground.setScaleX(f3);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ScreenshotActionChip screenshotActionChip = (ScreenshotActionChip) it.next();
            screenshotActionChip.setAlpha(animatedFraction);
            screenshotActionChip.setScaleX(1.0f / f3);
        }
        HorizontalScrollView horizontalScrollView = this.mActionsContainer;
        horizontalScrollView.setScrollX(this.mDirectionLTR ? 0 : horizontalScrollView.getWidth());
        this.mActionsContainer.setPivotX(this.mDirectionLTR ? 0.0f : r4.getWidth());
        this.mActionsContainerBackground.setPivotX(this.mDirectionLTR ? 0.0f : r4.getWidth());
    }

    private AnimatorSet createScreenshotDismissAnimation() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setStartDelay(50L);
        valueAnimatorOfFloat.setDuration(183L);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$createScreenshotDismissAnimation$26(valueAnimator);
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat2.setInterpolator(this.mAccelerateInterpolator);
        valueAnimatorOfFloat2.setDuration(350L);
        final float translationY = this.mScreenshotPreview.getTranslationY();
        final float translationY2 = this.mDismissButton.getTranslationY();
        valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot$$ExternalSyntheticLambda3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$createScreenshotDismissAnimation$27(translationY, translationY2, valueAnimator);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(valueAnimatorOfFloat2).with(valueAnimatorOfFloat);
        return animatorSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotDismissAnimation$26(ValueAnimator valueAnimator) {
        this.mScreenshotLayout.setAlpha(1.0f - valueAnimator.getAnimatedFraction());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$createScreenshotDismissAnimation$27(float f, float f2, ValueAnimator valueAnimator) {
        float fLerp = MathUtils.lerp(0.0f, this.mDismissDeltaY, valueAnimator.getAnimatedFraction());
        this.mScreenshotPreview.setTranslationY(f + fLerp);
        this.mDismissButton.setTranslationY(f2 + fLerp);
        this.mActionsContainer.setTranslationY(fLerp);
        this.mActionsContainerBackground.setTranslationY(fLerp);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearScreenshot() {
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        this.mScreenshotPreview.setImageDrawable(null);
        this.mScreenshotAnimatedView.setImageDrawable(null);
        this.mScreenshotAnimatedView.setVisibility(8);
        this.mActionsContainerBackground.setVisibility(8);
        this.mActionsContainer.setVisibility(8);
        this.mBackgroundProtection.setAlpha(0.0f);
        this.mDismissButton.setVisibility(8);
        this.mScreenshotPreview.setVisibility(8);
        this.mScreenshotPreview.setLayerType(0, null);
        this.mScreenshotPreview.setContentDescription(this.mContext.getResources().getString(R.string.screenshot_preview_description));
        this.mScreenshotPreview.setOnClickListener(null);
        this.mScreenshotLayout.setAlpha(1.0f);
        this.mDismissButton.setTranslationY(0.0f);
        this.mActionsContainer.setTranslationY(0.0f);
        this.mActionsContainerBackground.setTranslationY(0.0f);
        this.mScreenshotPreview.setTranslationY(0.0f);
        lambda$takeScreenshotPartial$4();
    }

    private void setAnimatedViewSize(int i, int i2) {
        ViewGroup.LayoutParams layoutParams = this.mScreenshotAnimatedView.getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i2;
        this.mScreenshotAnimatedView.setLayoutParams(layoutParams);
    }

    private void setWindowFocusable(boolean z) {
        if (z) {
            this.mWindowLayoutParams.flags &= -9;
        } else {
            this.mWindowLayoutParams.flags |= 8;
        }
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
    }

    private boolean isUserSetupComplete() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1;
    }

    private boolean aspectRatiosMatch(Bitmap bitmap, Insets insets, Rect rect) {
        int width = (bitmap.getWidth() - insets.left) - insets.right;
        int height = (bitmap.getHeight() - insets.top) - insets.bottom;
        if (height == 0 || width == 0 || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            Log.e("GlobalScreenshot", String.format("Provided bitmap and insets create degenerate region: %dx%d %s", Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight()), insets));
            return false;
        }
        float f = width / height;
        float fWidth = rect.width() / rect.height();
        boolean z = Math.abs(f - fWidth) < 0.1f;
        if (!z) {
            Log.d("GlobalScreenshot", String.format("aspectRatiosMatch: don't match bitmap: %f, bounds: %f", Float.valueOf(f), Float.valueOf(fWidth)));
        }
        return z;
    }

    private Drawable createScreenDrawable(Bitmap bitmap, Insets insets) {
        int width = (bitmap.getWidth() - insets.left) - insets.right;
        int height = (bitmap.getHeight() - insets.top) - insets.bottom;
        BitmapDrawable bitmapDrawable = new BitmapDrawable(this.mContext.getResources(), bitmap);
        if (height == 0 || width == 0 || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            Log.e("GlobalScreenshot", String.format("Can't create insetted drawable, using 0 insets bitmap and insets create degenerate region: %dx%d %s", Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight()), insets));
            return bitmapDrawable;
        }
        float f = width;
        float f2 = height;
        InsetDrawable insetDrawable = new InsetDrawable(bitmapDrawable, (insets.left * (-1.0f)) / f, (insets.top * (-1.0f)) / f2, (insets.right * (-1.0f)) / f, (insets.bottom * (-1.0f)) / f2);
        return (insets.left < 0 || insets.top < 0 || insets.right < 0 || insets.bottom < 0) ? new LayerDrawable(new Drawable[]{new ColorDrawable(-16777216), insetDrawable}) : insetDrawable;
    }
}
