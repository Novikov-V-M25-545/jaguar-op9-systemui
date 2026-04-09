package com.android.systemui.stackdivider;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SurfaceControl;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class DividerView extends FrameLayout implements View.OnTouchListener, ViewTreeObserver.OnComputeInternalInsetsListener {
    private boolean mAdjustedForIme;
    private final AnimationHandler mAnimationHandler;
    private View mBackground;
    private boolean mBackgroundLifted;
    private DividerCallbacks mCallback;
    private ValueAnimator mCurrentAnimator;
    private final Display mDefaultDisplay;
    private int mDividerInsets;
    int mDividerPositionX;
    int mDividerPositionY;
    private int mDividerSize;
    private int mDockSide;
    private final Rect mDockedInsetRect;
    private final Rect mDockedRect;
    private boolean mDockedStackMinimized;
    private final Rect mDockedTaskRect;
    private boolean mEntranceAnimationRunning;
    private boolean mExitAnimationRunning;
    private int mExitStartPosition;
    boolean mFirstLayout;
    private FlingAnimationUtils mFlingAnimationUtils;
    private boolean mGrowRecents;
    private DividerHandleView mHandle;
    private final View.AccessibilityDelegate mHandleDelegate;
    private final Handler mHandler;
    private boolean mHomeStackResizable;
    private DividerImeController mImeController;
    private boolean mIsInMinimizeInteraction;
    private final Rect mLastResizeRect;
    private int mLongPressEntraceAnimDuration;
    private MinimizedDockShadow mMinimizedShadow;
    private boolean mMoving;
    private final Rect mOtherInsetRect;
    private final Rect mOtherRect;
    private final Rect mOtherTaskRect;
    private boolean mRemoved;
    private final Runnable mResetBackgroundRunnable;
    DividerSnapAlgorithm.SnapTarget mSnapTargetBeforeMinimized;
    private SplitDisplayLayout mSplitLayout;
    private int mStartPosition;
    private int mStartX;
    private int mStartY;
    private DividerState mState;
    private boolean mSurfaceHidden;
    private SplitScreenTaskOrganizer mTiles;
    private final Matrix mTmpMatrix;
    private final Rect mTmpRect;
    private final float[] mTmpValues;
    private int mTouchElevation;
    private int mTouchSlop;
    private Runnable mUpdateEmbeddedMatrix;
    private VelocityTracker mVelocityTracker;
    private DividerWindowManager mWindowManager;
    private WindowManagerProxy mWindowManagerProxy;
    private static final PathInterpolator SLOWDOWN_INTERPOLATOR = new PathInterpolator(0.5f, 1.0f, 0.5f, 1.0f);
    private static final PathInterpolator DIM_INTERPOLATOR = new PathInterpolator(0.23f, 0.87f, 0.52f, -0.11f);
    private static final Interpolator IME_ADJUST_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.1f, 1.0f);

    public interface DividerCallbacks {
        void growRecents();

        void onDraggingEnd();

        void onDraggingStart();
    }

    private static boolean dockSideBottomRight(int i) {
        return i == 4 || i == 3;
    }

    private static boolean dockSideTopLeft(int i) {
        return i == 2 || i == 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        if (getViewRootImpl() == null) {
            return;
        }
        if (isHorizontalDivision()) {
            this.mTmpMatrix.setTranslate(0.0f, this.mDividerPositionY - this.mDividerInsets);
        } else {
            this.mTmpMatrix.setTranslate(this.mDividerPositionX - this.mDividerInsets, 0.0f);
        }
        this.mTmpMatrix.getValues(this.mTmpValues);
        try {
            getViewRootImpl().getAccessibilityEmbeddedConnection().setScreenMatrix(this.mTmpValues);
        } catch (RemoteException unused) {
        }
    }

    public DividerView(Context context) {
        this(context, null);
    }

    public DividerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DividerView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public DividerView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mTmpRect = new Rect();
        AnimationHandler animationHandler = new AnimationHandler();
        this.mAnimationHandler = animationHandler;
        this.mFirstLayout = true;
        this.mTmpMatrix = new Matrix();
        this.mTmpValues = new float[9];
        this.mSurfaceHidden = false;
        this.mHandler = new Handler();
        this.mHandleDelegate = new View.AccessibilityDelegate() { // from class: com.android.systemui.stackdivider.DividerView.1
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                DividerSnapAlgorithm snapAlgorithm = DividerView.this.getSnapAlgorithm();
                if (DividerView.this.isHorizontalDivision()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_full, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_top_full)));
                    if (snapAlgorithm.isFirstSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_70, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_top_70)));
                    }
                    if (snapAlgorithm.showMiddleSplitTargetForAccessibility()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_50, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_top_50)));
                    }
                    if (snapAlgorithm.isLastSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_30, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_top_30)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_rb_full, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_bottom_full)));
                    return;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_full, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_left_full)));
                if (snapAlgorithm.isFirstSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_70, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_left_70)));
                }
                if (snapAlgorithm.showMiddleSplitTargetForAccessibility()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_50, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_left_50)));
                }
                if (snapAlgorithm.isLastSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_30, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_left_30)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_rb_full, ((FrameLayout) DividerView.this).mContext.getString(R.string.accessibility_action_divider_right_full)));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i3, Bundle bundle) throws Resources.NotFoundException {
                DividerSnapAlgorithm.SnapTarget dismissStartTarget;
                int currentPosition = DividerView.this.getCurrentPosition();
                DividerSnapAlgorithm snapAlgorithm = DividerView.this.mSplitLayout.getSnapAlgorithm();
                if (i3 == R.id.action_move_tl_full) {
                    dismissStartTarget = snapAlgorithm.getDismissEndTarget();
                } else if (i3 == R.id.action_move_tl_70) {
                    dismissStartTarget = snapAlgorithm.getLastSplitTarget();
                } else if (i3 == R.id.action_move_tl_50) {
                    dismissStartTarget = snapAlgorithm.getMiddleTarget();
                } else if (i3 == R.id.action_move_tl_30) {
                    dismissStartTarget = snapAlgorithm.getFirstSplitTarget();
                } else {
                    dismissStartTarget = i3 == R.id.action_move_rb_full ? snapAlgorithm.getDismissStartTarget() : null;
                }
                DividerSnapAlgorithm.SnapTarget snapTarget = dismissStartTarget;
                if (snapTarget != null) {
                    DividerView.this.startDragging(true, false);
                    DividerView.this.stopDragging(currentPosition, snapTarget, 250L, Interpolators.FAST_OUT_SLOW_IN);
                    return true;
                }
                return super.performAccessibilityAction(view, i3, bundle);
            }
        };
        this.mResetBackgroundRunnable = new Runnable() { // from class: com.android.systemui.stackdivider.DividerView.2
            @Override // java.lang.Runnable
            public void run() {
                DividerView.this.resetBackground();
            }
        };
        this.mUpdateEmbeddedMatrix = new Runnable() { // from class: com.android.systemui.stackdivider.DividerView$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$0();
            }
        };
        this.mDefaultDisplay = ((DisplayManager) ((FrameLayout) this).mContext.getSystemService("display")).getDisplay(0);
        animationHandler.setProvider(new SfVsyncFrameCallbackProvider());
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        this.mHandle = (DividerHandleView) findViewById(R.id.docked_divider_handle);
        this.mBackground = findViewById(R.id.docked_divider_background);
        this.mMinimizedShadow = (MinimizedDockShadow) findViewById(R.id.minimized_dock_shadow);
        this.mHandle.setOnTouchListener(this);
        int dimensionPixelSize = getResources().getDimensionPixelSize(android.R.dimen.date_picker_date_label_size);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(android.R.dimen.cross_profile_apps_thumbnail_size);
        this.mDividerInsets = dimensionPixelSize2;
        this.mDividerSize = dimensionPixelSize - (dimensionPixelSize2 * 2);
        this.mTouchElevation = getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_lift_elevation);
        this.mLongPressEntraceAnimDuration = getResources().getInteger(R.integer.long_press_dock_anim_duration);
        this.mGrowRecents = getResources().getBoolean(R.bool.recents_grow_in_multiwindow);
        this.mTouchSlop = ViewConfiguration.get(((FrameLayout) this).mContext).getScaledTouchSlop();
        this.mFlingAnimationUtils = new FlingAnimationUtils(getResources().getDisplayMetrics(), 0.3f);
        this.mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(), getResources().getConfiguration().orientation == 2 ? 1014 : 1015));
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        this.mHandle.setAccessibilityDelegate(this.mHandleDelegate);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDockSide != -1 && !this.mIsInMinimizeInteraction) {
            saveSnapTargetBeforeMinimized(this.mSnapTargetBeforeMinimized);
        }
        this.mFirstLayout = true;
    }

    void onDividerRemoved() {
        this.mRemoved = true;
        this.mCallback = null;
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int top;
        int right;
        super.onLayout(z, i, i2, i3, i4);
        int i5 = 0;
        if (this.mFirstLayout) {
            initializeSurfaceState();
            this.mFirstLayout = false;
        }
        int i6 = this.mDockSide;
        if (i6 == 2) {
            top = this.mBackground.getTop();
        } else {
            if (i6 == 1) {
                right = this.mBackground.getLeft();
            } else if (i6 == 3) {
                right = this.mBackground.getRight() - this.mMinimizedShadow.getWidth();
            } else {
                top = 0;
            }
            i5 = right;
            top = 0;
        }
        MinimizedDockShadow minimizedDockShadow = this.mMinimizedShadow;
        minimizedDockShadow.layout(i5, top, minimizedDockShadow.getMeasuredWidth() + i5, this.mMinimizedShadow.getMeasuredHeight() + top);
        if (z) {
            notifySplitScreenBoundsChanged();
        }
    }

    public void injectDependencies(DividerWindowManager dividerWindowManager, DividerState dividerState, DividerCallbacks dividerCallbacks, SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout, DividerImeController dividerImeController, WindowManagerProxy windowManagerProxy) {
        this.mWindowManager = dividerWindowManager;
        this.mState = dividerState;
        this.mCallback = dividerCallbacks;
        this.mTiles = splitScreenTaskOrganizer;
        this.mSplitLayout = splitDisplayLayout;
        this.mImeController = dividerImeController;
        this.mWindowManagerProxy = windowManagerProxy;
        if (dividerState.mRatioPositionBeforeMinimized == 0.0f) {
            this.mSnapTargetBeforeMinimized = splitDisplayLayout.getSnapAlgorithm().getMiddleTarget();
        } else {
            repositionSnapTargetBeforeMinimized();
        }
    }

    public Rect getNonMinimizedSplitScreenSecondaryBounds() {
        this.mOtherTaskRect.set(this.mSplitLayout.mSecondary);
        return this.mOtherTaskRect;
    }

    private boolean inSplitMode() {
        return getVisibility() == 0;
    }

    void setHidden(final boolean z) {
        if (this.mSurfaceHidden == z) {
            return;
        }
        this.mSurfaceHidden = z;
        post(new Runnable() { // from class: com.android.systemui.stackdivider.DividerView$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setHidden$1(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setHidden$1(boolean z) {
        SurfaceControl windowSurfaceControl = getWindowSurfaceControl();
        if (windowSurfaceControl == null) {
            return;
        }
        SurfaceControl.Transaction transaction = this.mTiles.getTransaction();
        if (z) {
            transaction.hide(windowSurfaceControl);
        } else {
            transaction.show(windowSurfaceControl);
        }
        this.mImeController.setDimsHidden(transaction, z);
        transaction.apply();
        this.mTiles.releaseTransaction(transaction);
    }

    boolean isHidden() {
        return this.mSurfaceHidden;
    }

    public boolean startDragging(boolean z, boolean z2) {
        cancelFlingAnimation();
        if (z2) {
            this.mHandle.setTouching(true, z);
        }
        this.mDockSide = this.mSplitLayout.getPrimarySplitSide();
        this.mWindowManagerProxy.setResizing(true);
        if (z2) {
            this.mWindowManager.setSlippery(false);
            liftBackground();
        }
        DividerCallbacks dividerCallbacks = this.mCallback;
        if (dividerCallbacks != null) {
            dividerCallbacks.onDraggingStart();
        }
        return inSplitMode();
    }

    public void stopDragging(int i, float f, boolean z, boolean z2) throws Resources.NotFoundException {
        this.mHandle.setTouching(false, true);
        fling(i, f, z, z2);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, Interpolator interpolator) throws Resources.NotFoundException {
        stopDragging(i, snapTarget, j, 0L, 0L, interpolator);
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, Interpolator interpolator, long j2) throws Resources.NotFoundException {
        stopDragging(i, snapTarget, j, 0L, j2, interpolator);
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, long j2, long j3, Interpolator interpolator) throws Resources.NotFoundException {
        this.mHandle.setTouching(false, true);
        flingTo(i, snapTarget, j, j2, j3, interpolator);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    private void stopDragging() {
        this.mHandle.setTouching(false, true);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    private void updateDockSide() throws Resources.NotFoundException {
        int primarySplitSide = this.mSplitLayout.getPrimarySplitSide();
        this.mDockSide = primarySplitSide;
        this.mMinimizedShadow.setDockSide(primarySplitSide);
    }

    public DividerSnapAlgorithm getSnapAlgorithm() {
        return this.mDockedStackMinimized ? this.mSplitLayout.getMinimizedSnapAlgorithm(this.mHomeStackResizable) : this.mSplitLayout.getSnapAlgorithm();
    }

    public int getCurrentPosition() {
        return isHorizontalDivision() ? this.mDividerPositionY : this.mDividerPositionX;
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x0078  */
    @Override // android.view.View.OnTouchListener
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onTouch(android.view.View r6, android.view.MotionEvent r7) throws android.content.res.Resources.NotFoundException {
        /*
            r5 = this;
            r5.convertToScreenCoordinates(r7)
            int r6 = r7.getAction()
            r6 = r6 & 255(0xff, float:3.57E-43)
            r0 = 0
            r1 = 1
            if (r6 == 0) goto Lab
            if (r6 == r1) goto L78
            r2 = 2
            if (r6 == r2) goto L17
            r2 = 3
            if (r6 == r2) goto L78
            goto Laa
        L17:
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r6.addMovement(r7)
            float r6 = r7.getX()
            int r6 = (int) r6
            float r7 = r7.getY()
            int r7 = (int) r7
            boolean r2 = r5.isHorizontalDivision()
            if (r2 == 0) goto L38
            int r2 = r5.mStartY
            int r2 = r7 - r2
            int r2 = java.lang.Math.abs(r2)
            int r3 = r5.mTouchSlop
            if (r2 > r3) goto L4a
        L38:
            boolean r2 = r5.isHorizontalDivision()
            if (r2 != 0) goto L4c
            int r2 = r5.mStartX
            int r2 = r6 - r2
            int r2 = java.lang.Math.abs(r2)
            int r3 = r5.mTouchSlop
            if (r2 <= r3) goto L4c
        L4a:
            r2 = r1
            goto L4d
        L4c:
            r2 = r0
        L4d:
            boolean r3 = r5.mMoving
            if (r3 != 0) goto L59
            if (r2 == 0) goto L59
            r5.mStartX = r6
            r5.mStartY = r7
            r5.mMoving = r1
        L59:
            boolean r2 = r5.mMoving
            if (r2 == 0) goto Laa
            int r2 = r5.mDockSide
            r3 = -1
            if (r2 == r3) goto Laa
            com.android.internal.policy.DividerSnapAlgorithm r2 = r5.getSnapAlgorithm()
            int r3 = r5.mStartPosition
            r4 = 0
            com.android.internal.policy.DividerSnapAlgorithm$SnapTarget r0 = r2.calculateSnapTarget(r3, r4, r0)
            int r6 = r5.calculatePosition(r6, r7)
            int r7 = r5.mStartPosition
            r2 = 0
            r5.resizeStackSurfaces(r6, r7, r0, r2)
            goto Laa
        L78:
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r6.addMovement(r7)
            float r6 = r7.getRawX()
            int r6 = (int) r6
            float r7 = r7.getRawY()
            int r7 = (int) r7
            android.view.VelocityTracker r2 = r5.mVelocityTracker
            r3 = 1000(0x3e8, float:1.401E-42)
            r2.computeCurrentVelocity(r3)
            int r6 = r5.calculatePosition(r6, r7)
            boolean r7 = r5.isHorizontalDivision()
            if (r7 == 0) goto L9f
            android.view.VelocityTracker r7 = r5.mVelocityTracker
            float r7 = r7.getYVelocity()
            goto La5
        L9f:
            android.view.VelocityTracker r7 = r5.mVelocityTracker
            float r7 = r7.getXVelocity()
        La5:
            r5.stopDragging(r6, r7, r0, r1)
            r5.mMoving = r0
        Laa:
            return r1
        Lab:
            android.view.VelocityTracker r6 = android.view.VelocityTracker.obtain()
            r5.mVelocityTracker = r6
            r6.addMovement(r7)
            float r6 = r7.getX()
            int r6 = (int) r6
            r5.mStartX = r6
            float r6 = r7.getY()
            int r6 = (int) r6
            r5.mStartY = r6
            boolean r6 = r5.startDragging(r1, r1)
            if (r6 != 0) goto Lcb
            r5.stopDragging()
        Lcb:
            int r7 = r5.getCurrentPosition()
            r5.mStartPosition = r7
            r5.mMoving = r0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.DividerView.onTouch(android.view.View, android.view.MotionEvent):boolean");
    }

    private void logResizeEvent(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget == this.mSplitLayout.getSnapAlgorithm().getDismissStartTarget()) {
            MetricsLogger.action(((FrameLayout) this).mContext, 390, dockSideTopLeft(this.mDockSide) ? 1 : 0);
            return;
        }
        if (snapTarget == this.mSplitLayout.getSnapAlgorithm().getDismissEndTarget()) {
            MetricsLogger.action(((FrameLayout) this).mContext, 390, dockSideBottomRight(this.mDockSide) ? 1 : 0);
            return;
        }
        if (snapTarget == this.mSplitLayout.getSnapAlgorithm().getMiddleTarget()) {
            MetricsLogger.action(((FrameLayout) this).mContext, 389, 0);
            return;
        }
        if (snapTarget == this.mSplitLayout.getSnapAlgorithm().getFirstSplitTarget()) {
            MetricsLogger.action(((FrameLayout) this).mContext, 389, dockSideTopLeft(this.mDockSide) ? 1 : 2);
        } else if (snapTarget == this.mSplitLayout.getSnapAlgorithm().getLastSplitTarget()) {
            MetricsLogger.action(((FrameLayout) this).mContext, 389, dockSideTopLeft(this.mDockSide) ? 2 : 1);
        }
    }

    private void convertToScreenCoordinates(MotionEvent motionEvent) {
        motionEvent.setLocation(motionEvent.getRawX(), motionEvent.getRawY());
    }

    private void fling(int i, float f, boolean z, boolean z2) throws Resources.NotFoundException {
        DividerSnapAlgorithm snapAlgorithm = getSnapAlgorithm();
        DividerSnapAlgorithm.SnapTarget snapTargetCalculateSnapTarget = snapAlgorithm.calculateSnapTarget(i, f);
        if (z && snapTargetCalculateSnapTarget == snapAlgorithm.getDismissStartTarget()) {
            snapTargetCalculateSnapTarget = snapAlgorithm.getFirstSplitTarget();
        }
        if (z2) {
            logResizeEvent(snapTargetCalculateSnapTarget);
        }
        ValueAnimator flingAnimator = getFlingAnimator(i, snapTargetCalculateSnapTarget, 0L);
        this.mFlingAnimationUtils.apply(flingAnimator, i, snapTargetCalculateSnapTarget.position, f);
        flingAnimator.start();
    }

    private void flingTo(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, long j2, long j3, Interpolator interpolator) throws Resources.NotFoundException {
        ValueAnimator flingAnimator = getFlingAnimator(i, snapTarget, j3);
        flingAnimator.setDuration(j);
        flingAnimator.setStartDelay(j2);
        flingAnimator.setInterpolator(interpolator);
        flingAnimator.start();
    }

    private ValueAnimator getFlingAnimator(int i, final DividerSnapAlgorithm.SnapTarget snapTarget, long j) throws Resources.NotFoundException {
        if (this.mCurrentAnimator != null) {
            cancelFlingAnimation();
            updateDockSide();
        }
        final boolean z = snapTarget.flag == 0;
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(i, snapTarget.position);
        valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.stackdivider.DividerView$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$getFlingAnimator$2(z, snapTarget, valueAnimator);
            }
        });
        valueAnimatorOfInt.addListener(new AnonymousClass3(j, new Consumer() { // from class: com.android.systemui.stackdivider.DividerView$$ExternalSyntheticLambda5
            @Override // java.util.function.Consumer
            public final void accept(Object obj) throws Resources.NotFoundException {
                this.f$0.lambda$getFlingAnimator$3(snapTarget, (Boolean) obj);
            }
        }));
        valueAnimatorOfInt.setAnimationHandler(this.mAnimationHandler);
        this.mCurrentAnimator = valueAnimatorOfInt;
        return valueAnimatorOfInt;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getFlingAnimator$2(boolean z, DividerSnapAlgorithm.SnapTarget snapTarget, ValueAnimator valueAnimator) {
        resizeStackSurfaces(((Integer) valueAnimator.getAnimatedValue()).intValue(), (z && valueAnimator.getAnimatedFraction() == 1.0f) ? Integer.MAX_VALUE : snapTarget.taskPosition, snapTarget, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$getFlingAnimator$3(DividerSnapAlgorithm.SnapTarget snapTarget, Boolean bool) throws Resources.NotFoundException {
        boolean z = this.mIsInMinimizeInteraction;
        if (!bool.booleanValue() && !this.mDockedStackMinimized && this.mIsInMinimizeInteraction) {
            this.mIsInMinimizeInteraction = false;
        }
        boolean zCommitSnapFlags = commitSnapFlags(snapTarget);
        this.mWindowManagerProxy.setResizing(false);
        updateDockSide();
        this.mCurrentAnimator = null;
        this.mEntranceAnimationRunning = false;
        this.mExitAnimationRunning = false;
        if (!zCommitSnapFlags && !z) {
            WindowManagerProxy.applyResizeSplits(snapTarget.position, this.mSplitLayout);
        }
        DividerCallbacks dividerCallbacks = this.mCallback;
        if (dividerCallbacks != null) {
            dividerCallbacks.onDraggingEnd();
        }
        if (!this.mIsInMinimizeInteraction) {
            if (snapTarget.position < 0) {
                snapTarget = this.mSplitLayout.getSnapAlgorithm().getMiddleTarget();
            }
            DividerSnapAlgorithm snapAlgorithm = this.mSplitLayout.getSnapAlgorithm();
            if (snapTarget.position != snapAlgorithm.getDismissEndTarget().position && snapTarget.position != snapAlgorithm.getDismissStartTarget().position) {
                saveSnapTargetBeforeMinimized(snapTarget);
            }
        }
        notifySplitScreenBoundsChanged();
    }

    /* renamed from: com.android.systemui.stackdivider.DividerView$3, reason: invalid class name */
    class AnonymousClass3 extends AnimatorListenerAdapter {
        private boolean mCancelled;
        final /* synthetic */ Consumer val$endAction;
        final /* synthetic */ long val$endDelay;

        AnonymousClass3(long j, Consumer consumer) {
            this.val$endDelay = j;
            this.val$endAction = consumer;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            this.mCancelled = true;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            long j = this.val$endDelay;
            if (j == 0) {
                j = 0;
            }
            if (j == 0) {
                this.val$endAction.accept(Boolean.valueOf(this.mCancelled));
                return;
            }
            final Boolean boolValueOf = Boolean.valueOf(this.mCancelled);
            Handler handler = DividerView.this.mHandler;
            final Consumer consumer = this.val$endAction;
            handler.postDelayed(new Runnable() { // from class: com.android.systemui.stackdivider.DividerView$3$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    consumer.accept(boolValueOf);
                }
            }, j);
        }
    }

    private void notifySplitScreenBoundsChanged() {
        Rect rect;
        SplitDisplayLayout splitDisplayLayout = this.mSplitLayout;
        if (splitDisplayLayout.mPrimary == null || (rect = splitDisplayLayout.mSecondary) == null) {
            return;
        }
        this.mOtherTaskRect.set(rect);
        this.mTmpRect.set(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom());
        if (isHorizontalDivision()) {
            this.mTmpRect.offsetTo(0, this.mDividerPositionY);
        } else {
            this.mTmpRect.offsetTo(this.mDividerPositionX, 0);
        }
        this.mWindowManagerProxy.setTouchRegion(this.mTmpRect);
        this.mTmpRect.set(this.mSplitLayout.mDisplayLayout.stableInsets());
        int primarySplitSide = this.mSplitLayout.getPrimarySplitSide();
        if (primarySplitSide == 1) {
            this.mTmpRect.left = 0;
        } else if (primarySplitSide == 2) {
            this.mTmpRect.top = 0;
        } else if (primarySplitSide == 3) {
            this.mTmpRect.right = 0;
        }
        ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).notifySplitScreenBoundsChanged(this.mOtherTaskRect, this.mTmpRect);
    }

    private void cancelFlingAnimation() {
        ValueAnimator valueAnimator = this.mCurrentAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private boolean commitSnapFlags(DividerSnapAlgorithm.SnapTarget snapTarget) {
        int i;
        int i2;
        int i3 = snapTarget.flag;
        if (i3 == 0) {
            return false;
        }
        this.mWindowManagerProxy.dismissOrMaximizeDocked(this.mTiles, this.mSplitLayout, i3 != 1 ? (i = this.mDockSide) == 3 || i == 4 : (i2 = this.mDockSide) == 1 || i2 == 2);
        SurfaceControl.Transaction transaction = this.mTiles.getTransaction();
        setResizeDimLayer(transaction, true, 0.0f);
        setResizeDimLayer(transaction, false, 0.0f);
        transaction.apply();
        this.mTiles.releaseTransaction(transaction);
        return true;
    }

    private void liftBackground() {
        if (this.mBackgroundLifted) {
            return;
        }
        if (isHorizontalDivision()) {
            this.mBackground.animate().scaleY(1.4f);
        } else {
            this.mBackground.animate().scaleX(1.4f);
        }
        ViewPropertyAnimator viewPropertyAnimatorAnimate = this.mBackground.animate();
        Interpolator interpolator = Interpolators.TOUCH_RESPONSE;
        viewPropertyAnimatorAnimate.setInterpolator(interpolator).setDuration(150L).translationZ(this.mTouchElevation).start();
        this.mHandle.animate().setInterpolator(interpolator).setDuration(150L).translationZ(this.mTouchElevation).start();
        this.mBackgroundLifted = true;
    }

    private void releaseBackground() {
        if (this.mBackgroundLifted) {
            ViewPropertyAnimator viewPropertyAnimatorAnimate = this.mBackground.animate();
            Interpolator interpolator = Interpolators.FAST_OUT_SLOW_IN;
            viewPropertyAnimatorAnimate.setInterpolator(interpolator).setDuration(200L).translationZ(0.0f).scaleX(1.0f).scaleY(1.0f).start();
            this.mHandle.animate().setInterpolator(interpolator).setDuration(200L).translationZ(0.0f).start();
            this.mBackgroundLifted = false;
        }
    }

    private void initializeSurfaceState() {
        Rect rect;
        this.mSplitLayout.resizeSplits(this.mSplitLayout.getSnapAlgorithm().getMiddleTarget().position);
        SurfaceControl.Transaction transaction = this.mTiles.getTransaction();
        if (this.mDockedStackMinimized) {
            int i = this.mSplitLayout.getMinimizedSnapAlgorithm(this.mHomeStackResizable).getMiddleTarget().position;
            calculateBoundsForPosition(i, this.mDockSide, this.mDockedRect);
            calculateBoundsForPosition(i, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherRect);
            this.mDividerPositionY = i;
            this.mDividerPositionX = i;
            Rect rect2 = this.mDockedRect;
            SplitDisplayLayout splitDisplayLayout = this.mSplitLayout;
            resizeSplitSurfaces(transaction, rect2, splitDisplayLayout.mPrimary, this.mOtherRect, splitDisplayLayout.mSecondary);
        } else {
            SplitDisplayLayout splitDisplayLayout2 = this.mSplitLayout;
            resizeSplitSurfaces(transaction, splitDisplayLayout2.mPrimary, null, splitDisplayLayout2.mSecondary, null);
        }
        setResizeDimLayer(transaction, true, 0.0f);
        setResizeDimLayer(transaction, false, 0.0f);
        transaction.apply();
        this.mTiles.releaseTransaction(transaction);
        if (isHorizontalDivision()) {
            rect = new Rect(0, this.mDividerInsets, this.mSplitLayout.mDisplayLayout.width(), this.mDividerInsets + this.mDividerSize);
        } else {
            int i2 = this.mDividerInsets;
            rect = new Rect(i2, 0, this.mDividerSize + i2, this.mSplitLayout.mDisplayLayout.height());
        }
        Region region = new Region(rect);
        region.union(new Rect(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom()));
        this.mWindowManager.setTouchRegion(region);
    }

    void setMinimizedDockStack(boolean z, boolean z2, SurfaceControl.Transaction transaction) {
        this.mHomeStackResizable = z2;
        updateDockSide();
        if (!z) {
            resetBackground();
        }
        this.mMinimizedShadow.setAlpha(z ? 1.0f : 0.0f);
        if (this.mDockedStackMinimized != z) {
            this.mDockedStackMinimized = z;
            if (this.mSplitLayout.mDisplayLayout.rotation() != this.mDefaultDisplay.getRotation()) {
                repositionSnapTargetBeforeMinimized();
            }
            if (this.mIsInMinimizeInteraction == z && this.mCurrentAnimator == null) {
                return;
            }
            cancelFlingAnimation();
            if (z) {
                requestLayout();
                this.mIsInMinimizeInteraction = true;
                resizeStackSurfaces(this.mSplitLayout.getMinimizedSnapAlgorithm(this.mHomeStackResizable).getMiddleTarget(), transaction);
            } else {
                resizeStackSurfaces(this.mSnapTargetBeforeMinimized, transaction);
                this.mIsInMinimizeInteraction = false;
            }
        }
    }

    void enterSplitMode(boolean z) {
        post(new Runnable() { // from class: com.android.systemui.stackdivider.DividerView$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$enterSplitMode$4();
            }
        });
        DividerSnapAlgorithm.SnapTarget middleTarget = this.mSplitLayout.getMinimizedSnapAlgorithm(z).getMiddleTarget();
        if (this.mDockedStackMinimized) {
            int i = middleTarget.position;
            this.mDividerPositionX = i;
            this.mDividerPositionY = i;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$enterSplitMode$4() {
        SurfaceControl windowSurfaceControl = getWindowSurfaceControl();
        if (windowSurfaceControl == null) {
            return;
        }
        SurfaceControl.Transaction transaction = this.mTiles.getTransaction();
        transaction.show(windowSurfaceControl).apply();
        this.mTiles.releaseTransaction(transaction);
    }

    private SurfaceControl getWindowSurfaceControl() {
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl == null) {
            return null;
        }
        SurfaceControl surfaceControl = viewRootImpl.getSurfaceControl();
        return (surfaceControl == null || !surfaceControl.isValid()) ? this.mWindowManager.mSystemWindows.getViewSurface(this) : surfaceControl;
    }

    void exitSplitMode() {
        SurfaceControl windowSurfaceControl = getWindowSurfaceControl();
        if (windowSurfaceControl == null) {
            return;
        }
        SurfaceControl.Transaction transaction = this.mTiles.getTransaction();
        transaction.hide(windowSurfaceControl).apply();
        this.mTiles.releaseTransaction(transaction);
        WindowManagerProxy.applyResizeSplits(this.mSplitLayout.getSnapAlgorithm().getMiddleTarget().position, this.mSplitLayout);
    }

    public void setMinimizedDockStack(boolean z, long j, boolean z2) {
        int currentPosition;
        DividerSnapAlgorithm.SnapTarget middleTarget;
        this.mHomeStackResizable = z2;
        updateDockSide();
        if (this.mDockedStackMinimized != z) {
            this.mIsInMinimizeInteraction = true;
            this.mDockedStackMinimized = z;
            if (z) {
                currentPosition = this.mSnapTargetBeforeMinimized.position;
            } else {
                currentPosition = getCurrentPosition();
            }
            int i = currentPosition;
            if (z) {
                middleTarget = this.mSplitLayout.getMinimizedSnapAlgorithm(this.mHomeStackResizable).getMiddleTarget();
            } else {
                middleTarget = this.mSnapTargetBeforeMinimized;
            }
            stopDragging(i, middleTarget, j, Interpolators.FAST_OUT_SLOW_IN, 0L);
            setAdjustedForIme(false, j);
        }
        if (!z) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mBackground.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(j).start();
    }

    void finishAnimations() {
        ValueAnimator valueAnimator = this.mCurrentAnimator;
        if (valueAnimator != null) {
            valueAnimator.end();
        }
    }

    public void setAdjustedForIme(boolean z, long j) {
        if (this.mAdjustedForIme == z) {
            return;
        }
        updateDockSide();
        ViewPropertyAnimator viewPropertyAnimatorAnimate = this.mHandle.animate();
        Interpolator interpolator = IME_ADJUST_INTERPOLATOR;
        viewPropertyAnimatorAnimate.setInterpolator(interpolator).setDuration(j).alpha(z ? 0.0f : 1.0f).start();
        if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            this.mBackground.animate().scaleY(z ? 0.5f : 1.0f);
        }
        if (!z) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mBackground.animate().setInterpolator(interpolator).setDuration(j).start();
        this.mAdjustedForIme = z;
    }

    private void saveSnapTargetBeforeMinimized(DividerSnapAlgorithm.SnapTarget snapTarget) {
        this.mSnapTargetBeforeMinimized = snapTarget;
        this.mState.mRatioPositionBeforeMinimized = snapTarget.position / (isHorizontalDivision() ? this.mSplitLayout.mDisplayLayout.height() : this.mSplitLayout.mDisplayLayout.width());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetBackground() {
        this.mBackground.setPivotX(r0.getWidth() / 2);
        this.mBackground.setPivotY(r0.getHeight() / 2);
        this.mBackground.setScaleX(1.0f);
        this.mBackground.setScaleY(1.0f);
        this.mMinimizedShadow.setAlpha(0.0f);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void repositionSnapTargetBeforeMinimized() {
        this.mSnapTargetBeforeMinimized = this.mSplitLayout.getSnapAlgorithm().calculateNonDismissingSnapTarget((int) (this.mState.mRatioPositionBeforeMinimized * (isHorizontalDivision() ? this.mSplitLayout.mDisplayLayout.height() : this.mSplitLayout.mDisplayLayout.width())));
    }

    private int calculatePosition(int i, int i2) {
        return isHorizontalDivision() ? calculateYPosition(i2) : calculateXPosition(i);
    }

    public boolean isHorizontalDivision() {
        return getResources().getConfiguration().orientation == 1;
    }

    private int calculateXPosition(int i) {
        return (this.mStartPosition + i) - this.mStartX;
    }

    private int calculateYPosition(int i) {
        return (this.mStartPosition + i) - this.mStartY;
    }

    private void alignTopLeft(Rect rect, Rect rect2) {
        int iWidth = rect2.width();
        int iHeight = rect2.height();
        int i = rect.left;
        int i2 = rect.top;
        rect2.set(i, i2, iWidth + i, iHeight + i2);
    }

    private void alignBottomRight(Rect rect, Rect rect2) {
        int iWidth = rect2.width();
        int iHeight = rect2.height();
        int i = rect.right;
        int i2 = rect.bottom;
        rect2.set(i - iWidth, i2 - iHeight, i, i2);
    }

    public void calculateBoundsForPosition(int i, int i2, Rect rect) {
        DockedDividerUtils.calculateBoundsForPosition(i, i2, rect, this.mSplitLayout.mDisplayLayout.width(), this.mSplitLayout.mDisplayLayout.height(), this.mDividerSize);
    }

    private void resizeStackSurfaces(DividerSnapAlgorithm.SnapTarget snapTarget, SurfaceControl.Transaction transaction) {
        int i = snapTarget.position;
        resizeStackSurfaces(i, i, snapTarget, transaction);
    }

    void resizeSplitSurfaces(SurfaceControl.Transaction transaction, Rect rect, Rect rect2) {
        resizeSplitSurfaces(transaction, rect, null, rect2, null);
    }

    private void resizeSplitSurfaces(SurfaceControl.Transaction transaction, Rect rect, Rect rect2, Rect rect3, Rect rect4) {
        if (rect2 == null) {
            rect2 = rect;
        }
        if (rect4 == null) {
            rect4 = rect3;
        }
        this.mDividerPositionX = this.mSplitLayout.getPrimarySplitSide() == 3 ? rect3.right : rect.right;
        this.mDividerPositionY = rect.bottom;
        transaction.setPosition(this.mTiles.mPrimarySurface, rect2.left, rect2.top);
        Rect rect5 = new Rect(rect);
        rect5.offsetTo(-Math.min(rect2.left - rect.left, 0), -Math.min(rect2.top - rect.top, 0));
        transaction.setWindowCrop(this.mTiles.mPrimarySurface, rect5);
        transaction.setPosition(this.mTiles.mSecondarySurface, rect4.left, rect4.top);
        rect5.set(rect3);
        rect5.offsetTo(-(rect4.left - rect3.left), -(rect4.top - rect3.top));
        transaction.setWindowCrop(this.mTiles.mSecondarySurface, rect5);
        SurfaceControl windowSurfaceControl = getWindowSurfaceControl();
        if (windowSurfaceControl != null) {
            if (isHorizontalDivision()) {
                transaction.setPosition(windowSurfaceControl, 0.0f, this.mDividerPositionY - this.mDividerInsets);
            } else {
                transaction.setPosition(windowSurfaceControl, this.mDividerPositionX - this.mDividerInsets, 0.0f);
            }
        }
        if (getViewRootImpl() != null) {
            this.mHandler.removeCallbacks(this.mUpdateEmbeddedMatrix);
            this.mHandler.post(this.mUpdateEmbeddedMatrix);
        }
    }

    void setResizeDimLayer(SurfaceControl.Transaction transaction, boolean z, float f) {
        SplitScreenTaskOrganizer splitScreenTaskOrganizer = this.mTiles;
        SurfaceControl surfaceControl = z ? splitScreenTaskOrganizer.mPrimaryDim : splitScreenTaskOrganizer.mSecondaryDim;
        if (f <= 0.001f) {
            transaction.hide(surfaceControl);
        } else {
            transaction.setAlpha(surfaceControl, f);
            transaction.show(surfaceControl);
        }
    }

    void resizeStackSurfaces(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget, SurfaceControl.Transaction transaction) {
        if (this.mRemoved) {
            return;
        }
        calculateBoundsForPosition(i, this.mDockSide, this.mDockedRect);
        calculateBoundsForPosition(i, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherRect);
        if (!this.mDockedRect.equals(this.mLastResizeRect) || this.mEntranceAnimationRunning) {
            if (this.mBackground.getZ() > 0.0f) {
                this.mBackground.invalidate();
            }
            boolean z = transaction == null;
            SurfaceControl.Transaction transaction2 = z ? this.mTiles.getTransaction() : transaction;
            this.mLastResizeRect.set(this.mDockedRect);
            if (this.mIsInMinimizeInteraction) {
                calculateBoundsForPosition(this.mSnapTargetBeforeMinimized.position, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(this.mSnapTargetBeforeMinimized.position, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                if (this.mDockSide == 3) {
                    this.mDockedTaskRect.offset((Math.max(i, -this.mDividerSize) - this.mDockedTaskRect.left) + this.mDividerSize, 0);
                }
                resizeSplitSurfaces(transaction2, this.mDockedRect, this.mDockedTaskRect, this.mOtherRect, this.mOtherTaskRect);
                if (z) {
                    transaction2.apply();
                    this.mTiles.releaseTransaction(transaction2);
                    return;
                }
                return;
            }
            if (this.mEntranceAnimationRunning && i2 != Integer.MAX_VALUE) {
                calculateBoundsForPosition(i2, this.mDockSide, this.mDockedTaskRect);
                if (this.mDockSide == 3) {
                    this.mDockedTaskRect.offset((Math.max(i, -this.mDividerSize) - this.mDockedTaskRect.left) + this.mDividerSize, 0);
                }
                calculateBoundsForPosition(i2, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                resizeSplitSurfaces(transaction2, this.mDockedRect, this.mDockedTaskRect, this.mOtherRect, this.mOtherTaskRect);
            } else if (this.mExitAnimationRunning && i2 != Integer.MAX_VALUE) {
                calculateBoundsForPosition(i2, this.mDockSide, this.mDockedTaskRect);
                this.mDockedInsetRect.set(this.mDockedTaskRect);
                calculateBoundsForPosition(this.mExitStartPosition, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                this.mOtherInsetRect.set(this.mOtherTaskRect);
                applyExitAnimationParallax(this.mOtherTaskRect, i);
                if (this.mDockSide == 3) {
                    this.mDockedTaskRect.offset(this.mDividerSize + i, 0);
                }
                resizeSplitSurfaces(transaction2, this.mDockedRect, this.mDockedTaskRect, this.mOtherRect, this.mOtherTaskRect);
            } else if (i2 != Integer.MAX_VALUE) {
                calculateBoundsForPosition(i, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherRect);
                int iInvertDockSide = DockedDividerUtils.invertDockSide(this.mDockSide);
                int iRestrictDismissingTaskPosition = restrictDismissingTaskPosition(i2, this.mDockSide, snapTarget);
                int iRestrictDismissingTaskPosition2 = restrictDismissingTaskPosition(i2, iInvertDockSide, snapTarget);
                calculateBoundsForPosition(iRestrictDismissingTaskPosition, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(iRestrictDismissingTaskPosition2, iInvertDockSide, this.mOtherTaskRect);
                this.mTmpRect.set(0, 0, this.mSplitLayout.mDisplayLayout.width(), this.mSplitLayout.mDisplayLayout.height());
                alignTopLeft(this.mDockedRect, this.mDockedTaskRect);
                alignTopLeft(this.mOtherRect, this.mOtherTaskRect);
                this.mDockedInsetRect.set(this.mDockedTaskRect);
                this.mOtherInsetRect.set(this.mOtherTaskRect);
                if (dockSideTopLeft(this.mDockSide)) {
                    alignTopLeft(this.mTmpRect, this.mDockedInsetRect);
                    alignBottomRight(this.mTmpRect, this.mOtherInsetRect);
                } else {
                    alignBottomRight(this.mTmpRect, this.mDockedInsetRect);
                    alignTopLeft(this.mTmpRect, this.mOtherInsetRect);
                }
                applyDismissingParallax(this.mDockedTaskRect, this.mDockSide, snapTarget, i, iRestrictDismissingTaskPosition);
                applyDismissingParallax(this.mOtherTaskRect, iInvertDockSide, snapTarget, i, iRestrictDismissingTaskPosition2);
                resizeSplitSurfaces(transaction2, this.mDockedRect, this.mDockedTaskRect, this.mOtherRect, this.mOtherTaskRect);
            } else {
                resizeSplitSurfaces(transaction2, this.mDockedRect, null, this.mOtherRect, null);
            }
            DividerSnapAlgorithm.SnapTarget closestDismissTarget = getSnapAlgorithm().getClosestDismissTarget(i);
            setResizeDimLayer(transaction2, isDismissTargetPrimary(closestDismissTarget), getDimFraction(i, closestDismissTarget));
            if (z) {
                transaction2.apply();
                this.mTiles.releaseTransaction(transaction2);
            }
        }
    }

    private void applyExitAnimationParallax(Rect rect, int i) {
        int i2 = this.mDockSide;
        if (i2 == 2) {
            rect.offset(0, (int) ((i - this.mExitStartPosition) * 0.25f));
        } else if (i2 == 1) {
            rect.offset((int) ((i - this.mExitStartPosition) * 0.25f), 0);
        } else if (i2 == 3) {
            rect.offset((int) ((this.mExitStartPosition - i) * 0.25f), 0);
        }
    }

    private float getDimFraction(int i, DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (this.mEntranceAnimationRunning) {
            return 0.0f;
        }
        return DIM_INTERPOLATOR.getInterpolation(Math.max(0.0f, Math.min(getSnapAlgorithm().calculateDismissingFraction(i), 1.0f)));
    }

    private int restrictDismissingTaskPosition(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag == 1 && dockSideTopLeft(i2)) {
            return Math.max(this.mSplitLayout.getSnapAlgorithm().getFirstSplitTarget().position, this.mStartPosition);
        }
        return (snapTarget.flag == 2 && dockSideBottomRight(i2)) ? Math.min(this.mSplitLayout.getSnapAlgorithm().getLastSplitTarget().position, this.mStartPosition) : i;
    }

    private void applyDismissingParallax(Rect rect, int i, DividerSnapAlgorithm.SnapTarget snapTarget, int i2, int i3) {
        DividerSnapAlgorithm.SnapTarget firstSplitTarget;
        float fMin = Math.min(1.0f, Math.max(0.0f, this.mSplitLayout.getSnapAlgorithm().calculateDismissingFraction(i2)));
        DividerSnapAlgorithm.SnapTarget dismissEndTarget = null;
        if (i2 <= this.mSplitLayout.getSnapAlgorithm().getLastSplitTarget().position && dockSideTopLeft(i)) {
            dismissEndTarget = this.mSplitLayout.getSnapAlgorithm().getDismissStartTarget();
            firstSplitTarget = this.mSplitLayout.getSnapAlgorithm().getFirstSplitTarget();
        } else if (i2 < this.mSplitLayout.getSnapAlgorithm().getLastSplitTarget().position || !dockSideBottomRight(i)) {
            i3 = 0;
            firstSplitTarget = null;
        } else {
            dismissEndTarget = this.mSplitLayout.getSnapAlgorithm().getDismissEndTarget();
            DividerSnapAlgorithm.SnapTarget lastSplitTarget = this.mSplitLayout.getSnapAlgorithm().getLastSplitTarget();
            firstSplitTarget = lastSplitTarget;
            i3 = lastSplitTarget.position;
        }
        if (dismissEndTarget == null || fMin <= 0.0f || !isDismissing(firstSplitTarget, i2, i)) {
            return;
        }
        int iCalculateParallaxDismissingFraction = (int) (i3 + (calculateParallaxDismissingFraction(fMin, i) * (dismissEndTarget.position - firstSplitTarget.position)));
        int iWidth = rect.width();
        int iHeight = rect.height();
        if (i == 1) {
            rect.left = iCalculateParallaxDismissingFraction - iWidth;
            rect.right = iCalculateParallaxDismissingFraction;
            return;
        }
        if (i == 2) {
            rect.top = iCalculateParallaxDismissingFraction - iHeight;
            rect.bottom = iCalculateParallaxDismissingFraction;
        } else if (i == 3) {
            int i4 = this.mDividerSize;
            rect.left = iCalculateParallaxDismissingFraction + i4;
            rect.right = iCalculateParallaxDismissingFraction + iWidth + i4;
        } else {
            if (i != 4) {
                return;
            }
            int i5 = this.mDividerSize;
            rect.top = iCalculateParallaxDismissingFraction + i5;
            rect.bottom = iCalculateParallaxDismissingFraction + iHeight + i5;
        }
    }

    private static float calculateParallaxDismissingFraction(float f, int i) {
        float interpolation = SLOWDOWN_INTERPOLATOR.getInterpolation(f) / 3.5f;
        return i == 2 ? interpolation / 2.0f : interpolation;
    }

    private static boolean isDismissing(DividerSnapAlgorithm.SnapTarget snapTarget, int i, int i2) {
        return (i2 == 2 || i2 == 1) ? i < snapTarget.position : i > snapTarget.position;
    }

    private boolean isDismissTargetPrimary(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag == 1 && dockSideTopLeft(this.mDockSide)) {
            return true;
        }
        return snapTarget.flag == 2 && dockSideBottomRight(this.mDockSide);
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        internalInsetsInfo.touchableRegion.set(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom());
        internalInsetsInfo.touchableRegion.op(this.mBackground.getLeft(), this.mBackground.getTop(), this.mBackground.getRight(), this.mBackground.getBottom(), Region.Op.UNION);
    }

    void onDockedFirstAnimationFrame() {
        saveSnapTargetBeforeMinimized(this.mSplitLayout.getSnapAlgorithm().getMiddleTarget());
    }

    void onDockedTopTask() {
        DividerState dividerState = this.mState;
        dividerState.growAfterRecentsDrawn = false;
        dividerState.animateAfterRecentsDrawn = true;
        startDragging(false, false);
        updateDockSide();
        this.mEntranceAnimationRunning = true;
        resizeStackSurfaces(calculatePositionForInsetBounds(), this.mSplitLayout.getSnapAlgorithm().getMiddleTarget().position, this.mSplitLayout.getSnapAlgorithm().getMiddleTarget(), null);
    }

    void onRecentsDrawn() {
        updateDockSide();
        final int iCalculatePositionForInsetBounds = calculatePositionForInsetBounds();
        DividerState dividerState = this.mState;
        if (dividerState.animateAfterRecentsDrawn) {
            dividerState.animateAfterRecentsDrawn = false;
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.stackdivider.DividerView$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() throws Resources.NotFoundException {
                    this.f$0.lambda$onRecentsDrawn$5(iCalculatePositionForInsetBounds);
                }
            });
        }
        DividerState dividerState2 = this.mState;
        if (dividerState2.growAfterRecentsDrawn) {
            dividerState2.growAfterRecentsDrawn = false;
            updateDockSide();
            DividerCallbacks dividerCallbacks = this.mCallback;
            if (dividerCallbacks != null) {
                dividerCallbacks.growRecents();
            }
            stopDragging(iCalculatePositionForInsetBounds, getSnapAlgorithm().getMiddleTarget(), 336L, Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onRecentsDrawn$5(int i) throws Resources.NotFoundException {
        stopDragging(i, getSnapAlgorithm().getMiddleTarget(), this.mLongPressEntraceAnimDuration, Interpolators.FAST_OUT_SLOW_IN, 200L);
    }

    void onUndockingTask() {
        DividerSnapAlgorithm.SnapTarget dismissStartTarget;
        int primarySplitSide = this.mSplitLayout.getPrimarySplitSide();
        if (inSplitMode()) {
            startDragging(false, false);
            if (dockSideTopLeft(primarySplitSide)) {
                dismissStartTarget = this.mSplitLayout.getSnapAlgorithm().getDismissEndTarget();
            } else {
                dismissStartTarget = this.mSplitLayout.getSnapAlgorithm().getDismissStartTarget();
            }
            this.mExitAnimationRunning = true;
            int currentPosition = getCurrentPosition();
            this.mExitStartPosition = currentPosition;
            stopDragging(currentPosition, dismissStartTarget, 336L, 100L, 0L, Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    private int calculatePositionForInsetBounds() {
        this.mSplitLayout.mDisplayLayout.getStableBounds(this.mTmpRect);
        return DockedDividerUtils.calculatePositionForBounds(this.mTmpRect, this.mDockSide, this.mDividerSize);
    }
}
