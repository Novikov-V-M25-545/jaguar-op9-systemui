package com.android.systemui.pip.phone;

import android.annotation.SuppressLint;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.IPinnedStackController;
import android.view.InputEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.phone.PipAccessibilityInteractionConnection;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.shared.system.InputConsumerController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.DismissCircleView;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Function;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function5;

/* loaded from: classes.dex */
public class PipTouchHandler {
    private final AccessibilityManager mAccessibilityManager;
    private final IActivityManager mActivityManager;
    private int mBottomOffsetBufferPx;
    private PipAccessibilityInteractionConnection mConnection;
    private final Context mContext;
    private int mDismissAreaHeight;
    private int mDisplayRotation;
    private final boolean mEnableDismissDragToEdge;
    private final boolean mEnableResize;
    private int mExpandedShortestEdgeSize;
    private final FloatingContentCoordinator mFloatingContentCoordinator;
    private PipTouchGesture mGesture;
    private int mImeHeight;
    private int mImeOffset;
    private boolean mIsImeShowing;
    private boolean mIsShelfShowing;
    private MagnetizedObject.MagneticTarget mMagneticTarget;
    private PhysicsAnimator<View> mMagneticTargetAnimator;
    private MagnetizedObject<Rect> mMagnetizedPip;
    private final PipMenuActivityController mMenuController;
    private PipMotionHelper mMotionHelper;
    private int mMovementBoundsExtraOffsets;
    private boolean mMovementWithinDismiss;
    private IPinnedStackController mPinnedStackController;
    private final PipBoundsHandler mPipBoundsHandler;
    private PipResizeGestureHandler mPipResizeGestureHandler;
    private final PipUiEventLogger mPipUiEventLogger;
    private boolean mSendingHoverAccessibilityEvents;
    private int mShelfHeight;
    private final PipSnapAlgorithm mSnapAlgorithm;
    private DismissCircleView mTargetView;
    private ViewGroup mTargetViewContainer;
    private final PipTouchState mTouchState;
    private final WindowManager mWindowManager;
    private boolean mShowPipMenuOnAnimationEnd = false;
    private final PhysicsAnimator.SpringConfig mTargetSpringConfig = new PhysicsAnimator.SpringConfig(200.0f, 0.75f);
    private Rect mMovementBounds = new Rect();
    private Rect mInsetBounds = new Rect();
    private Rect mNormalBounds = new Rect();

    @VisibleForTesting
    Rect mNormalMovementBounds = new Rect();
    private Rect mExpandedBounds = new Rect();

    @VisibleForTesting
    Rect mExpandedMovementBounds = new Rect();
    private int mDeferResizeToNormalBoundsUntilRotation = -1;
    private Runnable mShowTargetAction = new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda4
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.showDismissTargetMaybe();
        }
    };
    private Handler mHandler = new Handler();
    private int mMenuState = 0;
    private float mSavedSnapFraction = -1.0f;
    private final Rect mTmpBounds = new Rect();

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$2000(PipTouchHandler pipTouchHandler) throws RemoteException {
        pipTouchHandler.updateDismissFraction();
    }

    private class PipMenuListener implements PipMenuActivityController.Listener {
        private PipMenuListener() {
        }

        /* synthetic */ PipMenuListener(PipTouchHandler pipTouchHandler, AnonymousClass1 anonymousClass1) {
            this();
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipMenuStateChanged(int i, boolean z, Runnable runnable) {
            PipTouchHandler.this.setMenuState(i, z, runnable);
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipExpand() {
            PipTouchHandler.this.mMotionHelper.expandPipToFullscreen();
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipDismiss() {
            PipTouchHandler.this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_TAP_TO_REMOVE);
            PipTouchHandler.this.mTouchState.removeDoubleTapTimeoutCallback();
            PipTouchHandler.this.mMotionHelper.dismissPip();
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipShowMenu() throws RemoteException {
            PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
        }
    }

    @SuppressLint({"InflateParams"})
    public PipTouchHandler(Context context, IActivityManager iActivityManager, final PipMenuActivityController pipMenuActivityController, InputConsumerController inputConsumerController, PipBoundsHandler pipBoundsHandler, PipTaskOrganizer pipTaskOrganizer, FloatingContentCoordinator floatingContentCoordinator, DeviceConfigProxy deviceConfigProxy, PipSnapAlgorithm pipSnapAlgorithm, SysUiState sysUiState, PipUiEventLogger pipUiEventLogger) throws Resources.NotFoundException {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mMenuController = pipMenuActivityController;
        AnonymousClass1 anonymousClass1 = null;
        pipMenuActivityController.addListener(new PipMenuListener(this, anonymousClass1));
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mGesture = new DefaultPipTouchGesture(this, anonymousClass1);
        PipMotionHelper pipMotionHelper = new PipMotionHelper(context, pipTaskOrganizer, pipMenuActivityController, pipSnapAlgorithm, floatingContentCoordinator);
        this.mMotionHelper = pipMotionHelper;
        this.mPipResizeGestureHandler = new PipResizeGestureHandler(context, pipBoundsHandler, pipMotionHelper, deviceConfigProxy, pipTaskOrganizer, pipMenuActivityController, new Function() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda8
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return this.f$0.getMovementBounds((Rect) obj);
            }
        }, new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateMovementBounds();
            }
        }, sysUiState, pipUiEventLogger);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        Handler handler = this.mHandler;
        Runnable runnable = new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() throws RemoteException {
                this.f$0.lambda$new$0();
            }
        };
        Objects.requireNonNull(pipMenuActivityController);
        this.mTouchState = new PipTouchState(viewConfiguration, handler, runnable, new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() throws RemoteException {
                pipMenuActivityController.hideMenu();
            }
        });
        Resources resources = context.getResources();
        this.mEnableDismissDragToEdge = resources.getBoolean(R.bool.config_pipEnableDismissDragToEdge);
        this.mEnableResize = resources.getBoolean(R.bool.config_pipEnableResizeForMenu);
        reloadResources();
        inputConsumerController.setInputListener(new InputConsumerController.InputListener() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda1
            @Override // com.android.systemui.shared.system.InputConsumerController.InputListener
            public final boolean onInputEvent(InputEvent inputEvent) {
                return this.f$0.handleTouchEvent(inputEvent);
            }
        });
        inputConsumerController.setRegistrationListener(new InputConsumerController.RegistrationListener() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda2
            @Override // com.android.systemui.shared.system.InputConsumerController.RegistrationListener
            public final void onRegistrationChanged(boolean z) {
                this.f$0.onRegistrationChanged(z);
            }
        });
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        this.mConnection = new PipAccessibilityInteractionConnection(context, this.mMotionHelper, pipTaskOrganizer, pipSnapAlgorithm, new PipAccessibilityInteractionConnection.AccessibilityCallbacks() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda0
            @Override // com.android.systemui.pip.phone.PipAccessibilityInteractionConnection.AccessibilityCallbacks
            public final void onAccessibilityShowMenu() throws RemoteException {
                this.f$0.onAccessibilityShowMenu();
            }
        }, new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateMovementBounds();
            }
        }, this.mHandler);
        this.mPipUiEventLogger = pipUiEventLogger;
        this.mTargetView = new DismissCircleView(context);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mTargetViewContainer = frameLayout;
        frameLayout.setBackgroundDrawable(context.getDrawable(R.drawable.floating_dismiss_gradient_transition));
        this.mTargetViewContainer.setClipChildren(false);
        this.mTargetViewContainer.addView(this.mTargetView);
        MagnetizedObject<Rect> magnetizedPip = this.mMotionHelper.getMagnetizedPip();
        this.mMagnetizedPip = magnetizedPip;
        this.mMagneticTarget = magnetizedPip.addTarget(this.mTargetView, 0);
        updateMagneticTargetSize();
        this.mMagnetizedPip.setAnimateStuckToTarget(new Function5() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda9
            @Override // kotlin.jvm.functions.Function5
            public final Object invoke(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
                return this.f$0.lambda$new$1((MagnetizedObject.MagneticTarget) obj, (Float) obj2, (Float) obj3, (Boolean) obj4, (Function0) obj5);
            }
        });
        this.mMagnetizedPip.setMagnetListener(new AnonymousClass1());
        this.mMagneticTargetAnimator = PhysicsAnimator.getInstance(this.mTargetView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() throws RemoteException {
        this.mMenuController.showMenuWithDelay(2, this.mMotionHelper.getBounds(), true, willResizeMenu(), shouldShowResizeHandle());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Unit lambda$new$1(MagnetizedObject.MagneticTarget magneticTarget, Float f, Float f2, Boolean bool, Function0 function0) {
        this.mMotionHelper.animateIntoDismissTarget(magneticTarget, f.floatValue(), f2.floatValue(), bool.booleanValue(), function0);
        return Unit.INSTANCE;
    }

    /* renamed from: com.android.systemui.pip.phone.PipTouchHandler$1, reason: invalid class name */
    class AnonymousClass1 implements MagnetizedObject.MagnetListener {
        AnonymousClass1() {
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            PipTouchHandler.this.showDismissTargetMaybe();
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
            if (z) {
                PipTouchHandler.this.mMotionHelper.flingToSnapTarget(f, f2, null, null);
                PipTouchHandler.this.hideDismissTarget();
            } else {
                PipTouchHandler.this.mMotionHelper.setSpringingToTouch(true);
            }
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            PipTouchHandler.this.mMotionHelper.notifyDismissalPending();
            PipTouchHandler.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onReleasedInTarget$0();
                }
            });
            PipTouchHandler.this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_DRAG_TO_REMOVE);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReleasedInTarget$0() {
            PipTouchHandler.this.mMotionHelper.animateDismiss();
            PipTouchHandler.this.hideDismissTarget();
        }
    }

    private void reloadResources() throws Resources.NotFoundException {
        Resources resources = this.mContext.getResources();
        this.mBottomOffsetBufferPx = resources.getDimensionPixelSize(R.dimen.pip_bottom_offset_buffer);
        this.mExpandedShortestEdgeSize = resources.getDimensionPixelSize(R.dimen.pip_expanded_shortest_edge_size);
        this.mImeOffset = resources.getDimensionPixelSize(R.dimen.pip_ime_offset);
        this.mDismissAreaHeight = resources.getDimensionPixelSize(R.dimen.floating_dismiss_gradient_height);
        updateMagneticTargetSize();
    }

    private void updateMagneticTargetSize() throws Resources.NotFoundException {
        if (this.mTargetView == null) {
            return;
        }
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.dismiss_circle_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 81;
        layoutParams.bottomMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.floating_dismiss_bottom_margin);
        this.mTargetView.setLayoutParams(layoutParams);
        this.mMagneticTarget.setMagneticFieldRadiusPx((int) (dimensionPixelSize * 1.25f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldShowResizeHandle() {
        return !this.mPipBoundsHandler.hasSaveReentryBounds();
    }

    public void setTouchEnabled(boolean z) {
        this.mTouchState.setAllowTouches(z);
    }

    public void showPictureInPictureMenu() throws RemoteException {
        if (this.mTouchState.isUserInteracting()) {
            return;
        }
        this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), false, willResizeMenu(), shouldShowResizeHandle());
    }

    public void onActivityPinned() {
        createOrUpdateDismissTarget();
        this.mShowPipMenuOnAnimationEnd = true;
        this.mPipResizeGestureHandler.onActivityPinned();
        this.mFloatingContentCoordinator.onContentAdded(this.mMotionHelper);
    }

    public void onActivityUnpinned(ComponentName componentName) {
        if (componentName == null) {
            cleanUpDismissTarget();
            this.mFloatingContentCoordinator.onContentRemoved(this.mMotionHelper);
        }
        this.mPipResizeGestureHandler.onActivityUnpinned();
    }

    public void onPinnedStackAnimationEnded(int i) throws RemoteException {
        this.mMotionHelper.synchronizePinnedStackBounds();
        updateMovementBounds();
        if (i == 2) {
            this.mPipResizeGestureHandler.setUserResizeBounds(this.mMotionHelper.getBounds());
        }
        if (this.mShowPipMenuOnAnimationEnd) {
            this.mMenuController.showMenu(1, this.mMotionHelper.getBounds(), true, false, shouldShowResizeHandle());
            this.mShowPipMenuOnAnimationEnd = false;
        }
    }

    public void onConfigurationChanged() throws Resources.NotFoundException {
        this.mPipResizeGestureHandler.onConfigurationChanged();
        this.mMotionHelper.synchronizePinnedStackBounds();
        reloadResources();
        createOrUpdateDismissTarget();
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mIsImeShowing = z;
        this.mImeHeight = i;
    }

    public void onShelfVisibilityChanged(boolean z, int i) {
        this.mIsShelfShowing = z;
        this.mShelfHeight = i;
    }

    public void adjustBoundsForRotation(Rect rect, Rect rect2, Rect rect3) {
        Rect rect4 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(rect, rect3, rect4, 0);
        if ((this.mMovementBounds.bottom - this.mMovementBoundsExtraOffsets) - this.mBottomOffsetBufferPx <= rect2.top) {
            rect.offsetTo(rect.left, rect4.bottom);
        }
    }

    public void onAspectRatioChanged() {
        this.mPipResizeGestureHandler.invalidateUserResizeBounds();
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) {
        int i2;
        if (this.mPipResizeGestureHandler.getUserResizeBounds().isEmpty()) {
            this.mPipResizeGestureHandler.setUserResizeBounds(rect2);
        }
        int i3 = this.mIsImeShowing ? this.mImeHeight : 0;
        if (this.mDisplayRotation != i) {
            this.mTouchState.reset();
        }
        this.mNormalBounds.set(rect2);
        Rect rect4 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mNormalBounds, rect, rect4, i3);
        if (this.mMovementBounds.isEmpty()) {
            this.mSnapAlgorithm.getMovementBounds(rect3, rect, this.mMovementBounds, 0);
        }
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        Size sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(rect2.width() / rect2.height(), this.mExpandedShortestEdgeSize, point.x, point.y);
        this.mExpandedBounds.set(0, 0, sizeForAspectRatio.getWidth(), sizeForAspectRatio.getHeight());
        Rect rect5 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mExpandedBounds, rect, rect5, i3);
        this.mPipResizeGestureHandler.updateMinSize(this.mNormalBounds.width(), this.mNormalBounds.height());
        this.mPipResizeGestureHandler.updateMaxSize(this.mExpandedBounds.width(), this.mExpandedBounds.height());
        boolean z3 = this.mIsImeShowing;
        int iMax = Math.max(z3 ? this.mImeOffset : 0, (z3 || !this.mIsShelfShowing) ? 0 : this.mShelfHeight);
        if ((z || z2) && !this.mTouchState.isUserInteracting()) {
            boolean z4 = this.mMenuState == 2 && willResizeMenu();
            Rect rect6 = new Rect();
            this.mSnapAlgorithm.getMovementBounds(rect3, rect, rect6, this.mIsImeShowing ? this.mImeHeight : 0);
            int i4 = this.mMovementBounds.bottom - this.mMovementBoundsExtraOffsets;
            int i5 = rect6.bottom;
            if (i5 >= rect6.top) {
                i5 -= iMax;
            }
            if (z4) {
                rect3.set(this.mExpandedBounds);
                this.mSnapAlgorithm.applySnapFraction(rect3, rect6, this.mSavedSnapFraction);
            }
            if (i4 < i5) {
                int i6 = rect3.top;
                if (i6 > i4 - this.mBottomOffsetBufferPx) {
                    this.mMotionHelper.animateToOffset(rect3, i5 - i6);
                }
            } else if (i4 > i5 && (i2 = rect3.top) > i5 - this.mBottomOffsetBufferPx) {
                this.mMotionHelper.animateToOffset(rect3, i5 - i2);
            }
        }
        this.mNormalMovementBounds.set(rect4);
        this.mExpandedMovementBounds.set(rect5);
        this.mDisplayRotation = i;
        this.mInsetBounds.set(rect);
        updateMovementBounds();
        this.mMovementBoundsExtraOffsets = iMax;
        this.mConnection.onMovementBoundsChanged(this.mNormalBounds, this.mExpandedBounds, this.mNormalMovementBounds, this.mExpandedMovementBounds);
        if (this.mDeferResizeToNormalBoundsUntilRotation == i) {
            this.mMotionHelper.animateToUnexpandedState(rect2, this.mSavedSnapFraction, this.mNormalMovementBounds, this.mMovementBounds, true);
            this.mSavedSnapFraction = -1.0f;
            this.mDeferResizeToNormalBoundsUntilRotation = -1;
        }
    }

    private void createOrUpdateDismissTarget() {
        if (!this.mTargetViewContainer.isAttachedToWindow()) {
            this.mHandler.removeCallbacks(this.mShowTargetAction);
            this.mMagneticTargetAnimator.cancel();
            this.mTargetViewContainer.setVisibility(4);
            try {
                this.mWindowManager.addView(this.mTargetViewContainer, getDismissTargetLayoutParams());
                return;
            } catch (IllegalStateException unused) {
                this.mWindowManager.updateViewLayout(this.mTargetViewContainer, getDismissTargetLayoutParams());
                return;
            }
        }
        this.mWindowManager.updateViewLayout(this.mTargetViewContainer, getDismissTargetLayoutParams());
    }

    private WindowManager.LayoutParams getDismissTargetLayoutParams() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        int i = this.mDismissAreaHeight;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, i, 0, point.y - i, 2024, 280, -3);
        layoutParams.setTitle("pip-dismiss-overlay");
        layoutParams.privateFlags |= 16;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDismissTargetMaybe() {
        createOrUpdateDismissTarget();
        if (this.mTargetViewContainer.getVisibility() != 0) {
            this.mTargetView.setTranslationY(this.mTargetViewContainer.getHeight());
            this.mTargetViewContainer.setVisibility(0);
            this.mMagneticTargetAnimator.cancel();
            this.mMagneticTargetAnimator.spring(DynamicAnimation.TRANSLATION_Y, 0.0f, this.mTargetSpringConfig).start();
            ((TransitionDrawable) this.mTargetViewContainer.getBackground()).startTransition(200);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowTargetAction);
        this.mMagneticTargetAnimator.spring(DynamicAnimation.TRANSLATION_Y, this.mTargetViewContainer.getHeight(), this.mTargetSpringConfig).withEndActions(new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$hideDismissTarget$2();
            }
        }).start();
        ((TransitionDrawable) this.mTargetViewContainer.getBackground()).reverseTransition(200);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hideDismissTarget$2() {
        this.mTargetViewContainer.setVisibility(8);
    }

    private void cleanUpDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowTargetAction);
        if (this.mTargetViewContainer.isAttachedToWindow()) {
            this.mWindowManager.removeViewImmediate(this.mTargetViewContainer);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRegistrationChanged(boolean z) {
        this.mAccessibilityManager.setPictureInPictureActionReplacingConnection(z ? this.mConnection : null);
        if (z || !this.mTouchState.isUserInteracting()) {
            return;
        }
        cleanUpDismissTarget();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAccessibilityShowMenu() throws RemoteException {
        this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), true, willResizeMenu(), shouldShowResizeHandle());
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:63:0x00df  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean handleTouchEvent(android.view.InputEvent r12) throws android.os.RemoteException {
        /*
            Method dump skipped, instructions count: 282
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.pip.phone.PipTouchHandler.handleTouchEvent(android.view.InputEvent):boolean");
    }

    private void sendAccessibilityHoverEvent(int i) {
        if (this.mAccessibilityManager.isEnabled()) {
            AccessibilityEvent accessibilityEventObtain = AccessibilityEvent.obtain(i);
            accessibilityEventObtain.setImportantForAccessibility(true);
            accessibilityEventObtain.setSourceNodeId(AccessibilityNodeInfo.ROOT_NODE_ID);
            accessibilityEventObtain.setWindowId(-3);
            this.mAccessibilityManager.sendAccessibilityEvent(accessibilityEventObtain);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDismissFraction() throws RemoteException {
        if (this.mMenuController == null || this.mIsImeShowing) {
            return;
        }
        Rect bounds = this.mMotionHelper.getBounds();
        float f = this.mInsetBounds.bottom;
        int i = bounds.bottom;
        float fMin = ((float) i) > f ? Math.min((i - f) / bounds.height(), 1.0f) : 0.0f;
        if (Float.compare(fMin, 0.0f) != 0 || this.mMenuController.isMenuActivityVisible()) {
            this.mMenuController.setDismissFraction(fMin);
        }
    }

    void setPinnedStackController(IPinnedStackController iPinnedStackController) {
        this.mPinnedStackController = iPinnedStackController;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setMenuState(int i, boolean z, Runnable runnable) {
        int i2 = this.mMenuState;
        if (i2 != i || z) {
            if (i != 2 || i2 == 2) {
                if (i == 0 && i2 == 2) {
                    if (z) {
                        if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                            try {
                                int displayRotation = this.mPinnedStackController.getDisplayRotation();
                                if (this.mDisplayRotation != displayRotation) {
                                    this.mDeferResizeToNormalBoundsUntilRotation = displayRotation;
                                }
                            } catch (RemoteException unused) {
                                Log.e("PipTouchHandler", "Could not get display rotation from controller");
                            }
                        }
                        if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                            Rect rect = new Rect(getUserResizeBounds());
                            Rect rect2 = new Rect();
                            this.mSnapAlgorithm.getMovementBounds(rect, this.mInsetBounds, rect2, this.mIsImeShowing ? this.mImeHeight : 0);
                            this.mMotionHelper.animateToUnexpandedState(rect, this.mSavedSnapFraction, rect2, this.mMovementBounds, false);
                            this.mSavedSnapFraction = -1.0f;
                        }
                    } else {
                        this.mSavedSnapFraction = -1.0f;
                    }
                }
            } else if (z) {
                this.mSavedSnapFraction = this.mMotionHelper.animateToExpandedState(new Rect(this.mExpandedBounds), this.mMovementBounds, this.mExpandedMovementBounds, runnable);
            }
            this.mMenuState = i;
            updateMovementBounds();
            onRegistrationChanged(i == 0);
            if (i == 0) {
                this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_HIDE_MENU);
            } else if (i == 2) {
                this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_SHOW_MENU);
            }
        }
    }

    public PipMotionHelper getMotionHelper() {
        return this.mMotionHelper;
    }

    @VisibleForTesting
    PipResizeGestureHandler getPipResizeGestureHandler() {
        return this.mPipResizeGestureHandler;
    }

    @VisibleForTesting
    void setPipResizeGestureHandler(PipResizeGestureHandler pipResizeGestureHandler) {
        this.mPipResizeGestureHandler = pipResizeGestureHandler;
    }

    @VisibleForTesting
    void setPipMotionHelper(PipMotionHelper pipMotionHelper) {
        this.mMotionHelper = pipMotionHelper;
    }

    Rect getUserResizeBounds() {
        return this.mPipResizeGestureHandler.getUserResizeBounds();
    }

    /* JADX INFO: Access modifiers changed from: private */
    class DefaultPipTouchGesture extends PipTouchGesture {
        private final PointF mDelta;
        private boolean mShouldHideMenuAfterFling;
        private final Point mStartPosition;

        private DefaultPipTouchGesture() {
            this.mStartPosition = new Point();
            this.mDelta = new PointF();
        }

        /* synthetic */ DefaultPipTouchGesture(PipTouchHandler pipTouchHandler, AnonymousClass1 anonymousClass1) {
            this();
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public void onDown(PipTouchState pipTouchState) throws RemoteException {
            if (pipTouchState.isUserInteracting()) {
                Rect possiblyAnimatingBounds = PipTouchHandler.this.mMotionHelper.getPossiblyAnimatingBounds();
                this.mDelta.set(0.0f, 0.0f);
                this.mStartPosition.set(possiblyAnimatingBounds.left, possiblyAnimatingBounds.top);
                PipTouchHandler.this.mMovementWithinDismiss = pipTouchState.getDownTouchPosition().y >= ((float) PipTouchHandler.this.mMovementBounds.bottom);
                PipTouchHandler.this.mMotionHelper.setSpringingToTouch(false);
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.pokeMenu();
                }
            }
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public boolean onMove(PipTouchState pipTouchState) {
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            if (pipTouchState.startedDragging()) {
                PipTouchHandler.this.mSavedSnapFraction = -1.0f;
                if (PipTouchHandler.this.mEnableDismissDragToEdge && PipTouchHandler.this.mTargetViewContainer.getVisibility() != 0) {
                    PipTouchHandler.this.mHandler.removeCallbacks(PipTouchHandler.this.mShowTargetAction);
                    PipTouchHandler.this.showDismissTargetMaybe();
                }
            }
            if (!pipTouchState.isDragging()) {
                return false;
            }
            PointF lastTouchDelta = pipTouchState.getLastTouchDelta();
            Point point = this.mStartPosition;
            float f = point.x;
            PointF pointF = this.mDelta;
            float f2 = pointF.x;
            float f3 = f + f2;
            float f4 = point.y;
            float f5 = pointF.y;
            float f6 = f4 + f5;
            float f7 = lastTouchDelta.x + f3;
            float f8 = lastTouchDelta.y + f6;
            pointF.x = f2 + (f7 - f3);
            pointF.y = f5 + (f8 - f6);
            PipTouchHandler.this.mTmpBounds.set(PipTouchHandler.this.mMotionHelper.getPossiblyAnimatingBounds());
            PipTouchHandler.this.mTmpBounds.offsetTo((int) f7, (int) f8);
            PipTouchHandler.this.mMotionHelper.movePip(PipTouchHandler.this.mTmpBounds, true);
            PointF lastTouchPosition = pipTouchState.getLastTouchPosition();
            if (PipTouchHandler.this.mMovementWithinDismiss) {
                PipTouchHandler pipTouchHandler = PipTouchHandler.this;
                pipTouchHandler.mMovementWithinDismiss = lastTouchPosition.y >= ((float) pipTouchHandler.mMovementBounds.bottom);
            }
            return true;
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public boolean onUp(PipTouchState pipTouchState) throws RemoteException {
            if (PipTouchHandler.this.mEnableDismissDragToEdge) {
                PipTouchHandler.this.hideDismissTarget();
            }
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            PointF velocity = pipTouchState.getVelocity();
            if (pipTouchState.isDragging()) {
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.showMenu(PipTouchHandler.this.mMenuState, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
                }
                this.mShouldHideMenuAfterFling = PipTouchHandler.this.mMenuState == 0;
                PipTouchHandler.this.mTouchState.reset();
                PipMotionHelper pipMotionHelper = PipTouchHandler.this.mMotionHelper;
                float f = velocity.x;
                float f2 = velocity.y;
                final PipTouchHandler pipTouchHandler = PipTouchHandler.this;
                pipMotionHelper.flingToSnapTarget(f, f2, new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$DefaultPipTouchGesture$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() throws RemoteException {
                        PipTouchHandler.access$2000(pipTouchHandler);
                    }
                }, new Runnable() { // from class: com.android.systemui.pip.phone.PipTouchHandler$DefaultPipTouchGesture$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() throws RemoteException {
                        this.f$0.flingEndAction();
                    }
                });
            } else if (!PipTouchHandler.this.mTouchState.isDoubleTap()) {
                if (PipTouchHandler.this.mMenuState != 2) {
                    if (PipTouchHandler.this.mTouchState.isWaitingForDoubleTap()) {
                        PipTouchHandler.this.mTouchState.scheduleDoubleTapTimeoutCallback();
                    } else {
                        PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
                    }
                }
            } else {
                PipTouchHandler.this.setTouchEnabled(false);
                PipTouchHandler.this.mMotionHelper.expandPipToFullscreen();
            }
            return true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void flingEndAction() throws RemoteException {
            if (this.mShouldHideMenuAfterFling) {
                PipTouchHandler.this.mMenuController.hideMenu();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMovementBounds() {
        int i = 0;
        this.mSnapAlgorithm.getMovementBounds(this.mMotionHelper.getBounds(), this.mInsetBounds, this.mMovementBounds, this.mIsImeShowing ? this.mImeHeight : 0);
        this.mMotionHelper.setCurrentMovementBounds(this.mMovementBounds);
        boolean z = this.mMenuState == 2;
        PipBoundsHandler pipBoundsHandler = this.mPipBoundsHandler;
        if (z && willResizeMenu()) {
            i = this.mExpandedShortestEdgeSize;
        }
        pipBoundsHandler.setMinEdgeSize(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Rect getMovementBounds(Rect rect) {
        Rect rect2 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(rect, this.mInsetBounds, rect2, this.mIsImeShowing ? this.mImeHeight : 0);
        return rect2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean willResizeMenu() {
        if (this.mEnableResize) {
            return (this.mExpandedBounds.width() == this.mNormalBounds.width() && this.mExpandedBounds.height() == this.mNormalBounds.height()) ? false : true;
        }
        return false;
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipTouchHandler");
        printWriter.println(str2 + "mMovementBounds=" + this.mMovementBounds);
        printWriter.println(str2 + "mNormalBounds=" + this.mNormalBounds);
        printWriter.println(str2 + "mNormalMovementBounds=" + this.mNormalMovementBounds);
        printWriter.println(str2 + "mExpandedBounds=" + this.mExpandedBounds);
        printWriter.println(str2 + "mExpandedMovementBounds=" + this.mExpandedMovementBounds);
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mIsImeShowing=" + this.mIsImeShowing);
        printWriter.println(str2 + "mImeHeight=" + this.mImeHeight);
        printWriter.println(str2 + "mIsShelfShowing=" + this.mIsShelfShowing);
        printWriter.println(str2 + "mShelfHeight=" + this.mShelfHeight);
        printWriter.println(str2 + "mSavedSnapFraction=" + this.mSavedSnapFraction);
        printWriter.println(str2 + "mEnableDragToEdgeDismiss=" + this.mEnableDismissDragToEdge);
        printWriter.println(str2 + "mMovementBoundsExtraOffsets=" + this.mMovementBoundsExtraOffsets);
        this.mTouchState.dump(printWriter, str2);
        this.mMotionHelper.dump(printWriter, str2);
        PipResizeGestureHandler pipResizeGestureHandler = this.mPipResizeGestureHandler;
        if (pipResizeGestureHandler != null) {
            pipResizeGestureHandler.dump(printWriter, str2);
        }
    }
}
