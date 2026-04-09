package com.android.systemui.bubbles;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.ActivityView;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.display.VirtualDisplay;
import android.os.Binder;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.recents.TriangleShape;
import com.android.systemui.statusbar.AlphaOptimizedButton;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class BubbleExpandedView extends LinearLayout {
    private ActivityManager mActivityManager;
    private SurfaceView mActivitySurface;
    private ActivityView mActivityView;
    private FrameLayout mActivityViewContainer;
    private ActivityViewStatus mActivityViewStatus;
    private Bubble mBubble;
    private BubbleController mBubbleController;
    private float mCornerRadius;
    private Point mDisplaySize;
    private int[] mExpandedViewContainerLocation;
    private int mExpandedViewPadding;
    private boolean mImeShowing;
    private boolean mIsOverflow;
    private boolean mKeyboardVisible;
    private int mMinHeight;
    private boolean mNeedsNewHeight;
    private int mOverflowHeight;
    private PendingIntent mPendingIntent;
    private ShapeDrawable mPointerDrawable;
    private int mPointerHeight;
    private int mPointerMargin;
    private View mPointerView;
    private int mPointerWidth;
    private AlphaOptimizedButton mSettingsIcon;
    private int mSettingsIconHeight;
    private BubbleStackView mStackView;
    private ActivityView.StateCallback mStateCallback;
    private int mTaskId;
    private WindowManager mVirtualDisplayWindowManager;
    private View mVirtualImeView;
    private WindowManager mWindowManager;

    private enum ActivityViewStatus {
        INITIALIZING,
        INITIALIZED,
        ACTIVITY_STARTED,
        RELEASED
    }

    /* renamed from: com.android.systemui.bubbles.BubbleExpandedView$1, reason: invalid class name */
    class AnonymousClass1 extends ActivityView.StateCallback {
        AnonymousClass1() {
        }

        public void onActivityViewReady(ActivityView activityView) {
            int i = AnonymousClass4.$SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus[BubbleExpandedView.this.mActivityViewStatus.ordinal()];
            if (i != 1 && i != 2) {
                if (i != 3) {
                    return;
                }
                BubbleExpandedView.this.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleExpandedView$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onActivityViewReady$1();
                    }
                });
            } else {
                final ActivityOptions activityOptionsMakeCustomAnimation = ActivityOptions.makeCustomAnimation(BubbleExpandedView.this.getContext(), 0, 0);
                activityOptionsMakeCustomAnimation.setTaskAlwaysOnTop(true);
                activityOptionsMakeCustomAnimation.setLaunchWindowingMode(6);
                BubbleExpandedView.this.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleExpandedView$1$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onActivityViewReady$0(activityOptionsMakeCustomAnimation);
                    }
                });
                BubbleExpandedView.this.mActivityViewStatus = ActivityViewStatus.ACTIVITY_STARTED;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActivityViewReady$0(ActivityOptions activityOptions) {
            if (BubbleExpandedView.this.mActivityView == null) {
                BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.getBubbleKey(), 10);
                return;
            }
            try {
                if (!BubbleExpandedView.this.mIsOverflow && BubbleExpandedView.this.mBubble.hasMetadataShortcutId() && BubbleExpandedView.this.mBubble.getShortcutInfo() != null) {
                    activityOptions.setApplyActivityFlagsForBubbles(true);
                    BubbleExpandedView.this.mActivityView.startShortcutActivity(BubbleExpandedView.this.mBubble.getShortcutInfo(), activityOptions, (Rect) null);
                    return;
                }
                Intent intent = new Intent();
                intent.addFlags(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES);
                intent.addFlags(134217728);
                if (BubbleExpandedView.this.mBubble != null) {
                    BubbleExpandedView.this.mBubble.setIntentActive();
                }
                BubbleExpandedView.this.mActivityView.startActivity(BubbleExpandedView.this.mPendingIntent, intent, activityOptions);
            } catch (RuntimeException e) {
                Log.w("Bubbles", "Exception while displaying bubble: " + BubbleExpandedView.this.getBubbleKey() + ", " + e.getMessage() + "; removing bubble");
                BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.getBubbleKey(), 10);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActivityViewReady$1() {
            BubbleExpandedView.this.mActivityManager.moveTaskToFront(BubbleExpandedView.this.mTaskId, 0);
        }

        public void onActivityViewDestroyed(ActivityView activityView) {
            BubbleExpandedView.this.mActivityViewStatus = ActivityViewStatus.RELEASED;
        }

        public void onTaskCreated(int i, ComponentName componentName) {
            BubbleExpandedView.this.mTaskId = i;
        }

        public void onTaskRemovalStarted(int i) {
            if (BubbleExpandedView.this.mBubble != null) {
                BubbleExpandedView.this.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleExpandedView$1$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onTaskRemovalStarted$2();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onTaskRemovalStarted$2() {
            BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.mBubble.getKey(), 3);
        }
    }

    /* renamed from: com.android.systemui.bubbles.BubbleExpandedView$4, reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus;

        static {
            int[] iArr = new int[ActivityViewStatus.values().length];
            $SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus = iArr;
            try {
                iArr[ActivityViewStatus.INITIALIZING.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus[ActivityViewStatus.INITIALIZED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus[ActivityViewStatus.ACTIVITY_STARTED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public BubbleExpandedView(Context context) {
        this(context, null);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mActivityViewStatus = ActivityViewStatus.INITIALIZING;
        this.mTaskId = -1;
        this.mBubbleController = (BubbleController) Dependency.get(BubbleController.class);
        this.mImeShowing = false;
        this.mCornerRadius = 0.0f;
        this.mActivityViewContainer = new FrameLayout(getContext());
        this.mStateCallback = new AnonymousClass1();
        updateDimensions();
        this.mActivityManager = (ActivityManager) ((LinearLayout) this).mContext.getSystemService("activity");
    }

    void updateDimensions() {
        this.mDisplaySize = new Point();
        WindowManager windowManager = (WindowManager) ((LinearLayout) this).mContext.getSystemService("window");
        this.mWindowManager = windowManager;
        windowManager.getDefaultDisplay().getRealSize(this.mDisplaySize);
        Resources resources = getResources();
        this.mMinHeight = resources.getDimensionPixelSize(R.dimen.bubble_expanded_default_height);
        this.mOverflowHeight = resources.getDimensionPixelSize(R.dimen.bubble_overflow_height);
        this.mPointerMargin = resources.getDimensionPixelSize(R.dimen.bubble_pointer_margin);
    }

    @Override // android.view.View
    @SuppressLint({"ClickableViewAccessibility"})
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        Resources resources = getResources();
        this.mPointerView = findViewById(R.id.pointer_view);
        this.mPointerWidth = resources.getDimensionPixelSize(R.dimen.bubble_pointer_width);
        this.mPointerHeight = resources.getDimensionPixelSize(R.dimen.bubble_pointer_height);
        this.mPointerDrawable = new ShapeDrawable(TriangleShape.create(this.mPointerWidth, this.mPointerHeight, true));
        this.mPointerView.setVisibility(4);
        this.mSettingsIconHeight = getContext().getResources().getDimensionPixelSize(R.dimen.bubble_manage_button_height);
        this.mSettingsIcon = (AlphaOptimizedButton) findViewById(R.id.settings_button);
        this.mActivityView = new ActivityView(((LinearLayout) this).mContext, (AttributeSet) null, 0, true, false, true, true);
        setContentVisibility(false);
        this.mActivityViewContainer.setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.bubbles.BubbleExpandedView.2
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), BubbleExpandedView.this.mCornerRadius);
            }
        });
        this.mActivityViewContainer.setClipToOutline(true);
        this.mActivityViewContainer.addView(this.mActivityView);
        this.mActivityViewContainer.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        addView(this.mActivityViewContainer);
        ActivityView activityView = this.mActivityView;
        if (activityView != null && activityView.getChildCount() > 0 && (this.mActivityView.getChildAt(0) instanceof SurfaceView)) {
            this.mActivitySurface = (SurfaceView) this.mActivityView.getChildAt(0);
        }
        bringChildToFront(this.mActivityView);
        bringChildToFront(this.mSettingsIcon);
        applyThemeAttrs();
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.systemui.bubbles.BubbleExpandedView$$ExternalSyntheticLambda0
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return this.f$0.lambda$onFinishInflate$0(view, windowInsets);
            }
        });
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.bubble_expanded_view_padding);
        this.mExpandedViewPadding = dimensionPixelSize;
        setPadding(dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize);
        setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.bubbles.BubbleExpandedView$$ExternalSyntheticLambda1
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.lambda$onFinishInflate$1(view, motionEvent);
            }
        });
        setLayoutDirection(3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ WindowInsets lambda$onFinishInflate$0(View view, WindowInsets windowInsets) {
        boolean z = windowInsets.getSystemWindowInsetBottom() - windowInsets.getStableInsetBottom() != 0;
        this.mKeyboardVisible = z;
        if (!z && this.mNeedsNewHeight) {
            updateHeight();
        }
        return view.onApplyWindowInsets(windowInsets);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$onFinishInflate$1(View view, MotionEvent motionEvent) {
        if (!usingActivityView()) {
            return false;
        }
        Rect rect = new Rect();
        this.mActivityView.getBoundsOnScreen(rect);
        return motionEvent.getRawY() >= ((float) rect.top) && motionEvent.getRawY() <= ((float) rect.bottom) && (motionEvent.getRawX() < ((float) rect.left) || motionEvent.getRawX() > ((float) rect.right));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getBubbleKey() {
        Bubble bubble = this.mBubble;
        return bubble != null ? bubble.getKey() : "null";
    }

    void setSurfaceZOrderedOnTop(boolean z) {
        SurfaceView surfaceView = this.mActivitySurface;
        if (surfaceView == null) {
            return;
        }
        surfaceView.setZOrderedOnTop(z, true);
    }

    SurfaceControl.ScreenshotGraphicBuffer snapshotActivitySurface() {
        SurfaceView surfaceView = this.mActivitySurface;
        if (surfaceView == null) {
            return null;
        }
        return SurfaceControl.captureLayers(surfaceView.getSurfaceControl(), new Rect(0, 0, this.mActivityView.getWidth(), this.mActivityView.getHeight()), 1.0f);
    }

    int[] getActivityViewLocationOnScreen() {
        ActivityView activityView = this.mActivityView;
        return activityView != null ? activityView.getLocationOnScreen() : new int[]{0, 0};
    }

    void setManageClickListener(View.OnClickListener onClickListener) {
        findViewById(R.id.settings_button).setOnClickListener(onClickListener);
    }

    void updateObscuredTouchableRegion() {
        ActivityView activityView = this.mActivityView;
        if (activityView != null) {
            activityView.onLocationChanged();
        }
    }

    void applyThemeAttrs() {
        TypedArray typedArrayObtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.dialogCornerRadius, android.R.attr.colorBackgroundFloating});
        this.mCornerRadius = typedArrayObtainStyledAttributes.getDimensionPixelSize(0, 0);
        this.mActivityViewContainer.setBackgroundColor(typedArrayObtainStyledAttributes.getColor(1, -1));
        typedArrayObtainStyledAttributes.recycle();
        if (this.mActivityView != null && ScreenDecorationsUtils.supportsRoundedCornersOnWindows(((LinearLayout) this).mContext.getResources())) {
            this.mActivityView.setCornerRadius(this.mCornerRadius);
        }
        int i = getResources().getConfiguration().uiMode & 48;
        if (i == 16) {
            this.mPointerDrawable.setTint(getResources().getColor(R.color.bubbles_light));
        } else if (i == 32) {
            this.mPointerDrawable.setTint(getResources().getColor(R.color.bubbles_dark));
        }
        this.mPointerView.setBackground(this.mPointerDrawable);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyboardVisible = false;
        this.mNeedsNewHeight = false;
        ActivityView activityView = this.mActivityView;
        if (activityView != null) {
            if (ViewRootImpl.sNewInsetsMode == 2) {
                setImeWindowToDisplay(0, 0);
            } else {
                activityView.setForwardedInsets(Insets.of(0, 0, 0, 0));
            }
        }
    }

    void setContentVisibility(boolean z) {
        float f = z ? 1.0f : 0.0f;
        this.mPointerView.setAlpha(f);
        ActivityView activityView = this.mActivityView;
        if (activityView == null || f == activityView.getAlpha()) {
            return;
        }
        this.mActivityView.setAlpha(f);
        this.mActivityView.bringToFront();
    }

    ActivityView getActivityView() {
        return this.mActivityView;
    }

    int getTaskId() {
        return this.mTaskId;
    }

    void updateInsets(WindowInsets windowInsets) {
        if (usingActivityView()) {
            int iMax = Math.max((this.mActivityView.getLocationOnScreen()[1] + this.mActivityView.getHeight()) - (this.mDisplaySize.y - Math.max(windowInsets.getSystemWindowInsetBottom(), windowInsets.getDisplayCutout() != null ? windowInsets.getDisplayCutout().getSafeInsetBottom() : 0)), 0);
            if (ViewRootImpl.sNewInsetsMode == 2) {
                setImeWindowToDisplay(getWidth(), iMax);
            } else {
                this.mActivityView.setForwardedInsets(Insets.of(0, 0, 0, iMax));
            }
        }
    }

    private void setImeWindowToDisplay(int i, int i2) {
        if (getVirtualDisplayId() == -1) {
            return;
        }
        if (i2 == 0 || i == 0) {
            if (this.mImeShowing) {
                this.mVirtualImeView.setVisibility(8);
                this.mImeShowing = false;
                return;
            }
            return;
        }
        Context contextCreateDisplayContext = ((LinearLayout) this).mContext.createDisplayContext(getVirtualDisplay().getDisplay());
        if (this.mVirtualDisplayWindowManager == null) {
            this.mVirtualDisplayWindowManager = (WindowManager) contextCreateDisplayContext.getSystemService("window");
        }
        View view = this.mVirtualImeView;
        if (view == null) {
            View view2 = new View(contextCreateDisplayContext);
            this.mVirtualImeView = view2;
            view2.setVisibility(0);
            this.mVirtualDisplayWindowManager.addView(this.mVirtualImeView, getVirtualImeViewAttrs(i, i2));
        } else {
            this.mVirtualDisplayWindowManager.updateViewLayout(view, getVirtualImeViewAttrs(i, i2));
            this.mVirtualImeView.setVisibility(0);
        }
        this.mImeShowing = true;
    }

    private WindowManager.LayoutParams getVirtualImeViewAttrs(int i, int i2) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i, i2, 2024, 536, -2);
        layoutParams.gravity = 80;
        layoutParams.setTitle("ImeInsetsWindowWithoutContent");
        layoutParams.token = new Binder();
        layoutParams.providesInsetsTypes = new int[]{17};
        layoutParams.alpha = 0.0f;
        return layoutParams;
    }

    void setStackView(BubbleStackView bubbleStackView) {
        this.mStackView = bubbleStackView;
    }

    public void setOverflow(boolean z) {
        this.mIsOverflow = z;
        this.mPendingIntent = PendingIntent.getActivity(((LinearLayout) this).mContext, 0, new Intent(((LinearLayout) this).mContext, (Class<?>) BubbleOverflowActivity.class), 134217728);
        this.mSettingsIcon.setVisibility(8);
    }

    void update(Bubble bubble) {
        boolean z = this.mBubble == null || didBackingContentChange(bubble);
        if (z || (bubble != null && bubble.getKey().equals(this.mBubble.getKey()))) {
            this.mBubble = bubble;
            this.mSettingsIcon.setContentDescription(getResources().getString(R.string.bubbles_settings_button_description, bubble.getAppName()));
            this.mSettingsIcon.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.bubbles.BubbleExpandedView.3
                @Override // android.view.View.AccessibilityDelegate
                public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                    BubbleExpandedView.this.mStackView.setupLocalMenu(accessibilityNodeInfo);
                }
            });
            if (z) {
                PendingIntent bubbleIntent = this.mBubble.getBubbleIntent();
                this.mPendingIntent = bubbleIntent;
                if (bubbleIntent != null || this.mBubble.hasMetadataShortcutId()) {
                    setContentVisibility(false);
                    this.mActivityView.setVisibility(0);
                }
            }
            applyThemeAttrs();
            return;
        }
        Log.w("Bubbles", "Trying to update entry with different key, new bubble: " + bubble.getKey() + " old bubble: " + bubble.getKey());
    }

    private boolean didBackingContentChange(Bubble bubble) {
        return (this.mBubble != null && this.mPendingIntent != null) != (bubble.getBubbleIntent() != null);
    }

    void populateExpandedView() {
        if (usingActivityView()) {
            this.mActivityView.setCallback(this.mStateCallback);
        } else {
            Log.e("Bubbles", "Cannot populate expanded view.");
        }
    }

    boolean performBackPressIfNeeded() {
        if (!usingActivityView()) {
            return false;
        }
        this.mActivityView.performBackPress();
        return true;
    }

    void updateHeight() {
        if (this.mExpandedViewContainerLocation != null && usingActivityView()) {
            float fMax = this.mOverflowHeight;
            if (!this.mIsOverflow) {
                fMax = Math.max(this.mBubble.getDesiredHeight(((LinearLayout) this).mContext), this.mMinHeight);
            }
            float fMax2 = Math.max(Math.min(fMax, getMaxExpandedHeight()), this.mMinHeight);
            ViewGroup.LayoutParams layoutParams = this.mActivityView.getLayoutParams();
            this.mNeedsNewHeight = ((float) layoutParams.height) != fMax2;
            if (this.mKeyboardVisible) {
                return;
            }
            layoutParams.height = (int) fMax2;
            this.mActivityView.setLayoutParams(layoutParams);
            this.mNeedsNewHeight = false;
        }
    }

    private int getMaxExpandedHeight() {
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mDisplaySize);
        return ((((((this.mDisplaySize.y - this.mExpandedViewContainerLocation[1]) - getPaddingTop()) - getPaddingBottom()) - this.mSettingsIconHeight) - this.mPointerHeight) - this.mPointerMargin) - (getRootWindowInsets() != null ? getRootWindowInsets().getStableInsetBottom() : 0);
    }

    public void updateView(int[] iArr) {
        this.mExpandedViewContainerLocation = iArr;
        if (usingActivityView() && this.mActivityView.getVisibility() == 0 && this.mActivityView.isAttachedToWindow()) {
            this.mActivityView.onLocationChanged();
            updateHeight();
        }
    }

    public void setPointerPosition(float f) {
        this.mPointerView.setTranslationX((f - (this.mPointerWidth / 2.0f)) - this.mExpandedViewPadding);
        this.mPointerView.setVisibility(0);
    }

    public void getManageButtonBoundsOnScreen(Rect rect) {
        this.mSettingsIcon.getBoundsOnScreen(rect);
    }

    public void cleanUpExpandedState() {
        ActivityView activityView = this.mActivityView;
        if (activityView == null) {
            return;
        }
        activityView.release();
        if (this.mTaskId != -1) {
            try {
                ActivityTaskManager.getService().removeTask(this.mTaskId);
            } catch (RemoteException unused) {
                Log.w("Bubbles", "Failed to remove taskId " + this.mTaskId);
            }
            this.mTaskId = -1;
        }
        removeView(this.mActivityView);
        this.mActivityView = null;
    }

    void notifyDisplayEmpty() {
        if (this.mActivityViewStatus == ActivityViewStatus.ACTIVITY_STARTED) {
            this.mActivityViewStatus = ActivityViewStatus.INITIALIZED;
        }
    }

    private boolean usingActivityView() {
        return (this.mPendingIntent != null || this.mBubble.hasMetadataShortcutId()) && this.mActivityView != null;
    }

    public int getVirtualDisplayId() {
        if (usingActivityView()) {
            return this.mActivityView.getVirtualDisplayId();
        }
        return -1;
    }

    private VirtualDisplay getVirtualDisplay() {
        if (usingActivityView()) {
            return this.mActivityView.getVirtualDisplay();
        }
        return null;
    }
}
