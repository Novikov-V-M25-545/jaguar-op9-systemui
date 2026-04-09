package com.android.systemui.bubbles;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityView;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Choreographer;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.bubbles.BadgedImageView;
import com.android.systemui.bubbles.Bubble;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.animation.AnimatableScaleMatrix;
import com.android.systemui.bubbles.animation.ExpandedAnimationController;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.bubbles.animation.StackAnimationController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.util.DismissCircleView;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.RelativeTouchListener;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class BubbleStackView extends FrameLayout implements ViewTreeObserver.OnComputeInternalInsetsListener {

    @VisibleForTesting
    static final int FLYOUT_HIDE_AFTER = 5000;
    private Runnable mAfterFlyoutHidden;
    private final DynamicAnimation.OnAnimationEndListener mAfterFlyoutTransitionSpring;
    private Runnable mAnimateInFlyout;
    private final Runnable mAnimateTemporarilyInvisibleImmediate;
    private boolean mAnimatingEducationAway;
    private boolean mAnimatingManageEducationAway;
    private SurfaceControl.ScreenshotGraphicBuffer mAnimatingOutBubbleBuffer;
    private FrameLayout mAnimatingOutSurfaceContainer;
    private SurfaceView mAnimatingOutSurfaceView;
    private View.OnClickListener mBubbleClickListener;
    private PhysicsAnimationLayout mBubbleContainer;
    private final BubbleData mBubbleData;
    private int mBubbleElevation;
    private BubbleOverflow mBubbleOverflow;
    private int mBubblePaddingTop;
    private int mBubbleSize;
    private Bubble mBubbleToExpandAfterFlyoutCollapse;
    private RelativeTouchListener mBubbleTouchListener;
    private int mBubbleTouchPadding;
    private int mCornerRadius;
    private final Handler mDelayedAnimationHandler;
    private final ValueAnimator mDesaturateAndDarkenAnimator;
    private final Paint mDesaturateAndDarkenPaint;
    private View mDesaturateAndDarkenTargetView;
    private PhysicsAnimator<View> mDismissTargetAnimator;
    private View mDismissTargetCircle;
    private ViewGroup mDismissTargetContainer;
    private PhysicsAnimator.SpringConfig mDismissTargetSpring;
    private Point mDisplaySize;
    private BubbleController.BubbleExpandListener mExpandListener;
    private ExpandedAnimationController mExpandedAnimationController;
    private BubbleViewProvider mExpandedBubble;
    private FrameLayout mExpandedViewContainer;
    private final AnimatableScaleMatrix mExpandedViewContainerMatrix;
    private int mExpandedViewPadding;
    private BubbleFlyoutView mFlyout;
    private View.OnClickListener mFlyoutClickListener;
    private final FloatPropertyCompat mFlyoutCollapseProperty;
    private float mFlyoutDragDeltaX;
    private RelativeTouchListener mFlyoutTouchListener;
    private final SpringAnimation mFlyoutTransitionSpring;
    private final Runnable mHideCurrentInputMethodCallback;
    private Runnable mHideFlyout;
    private int mImeOffset;
    private final MagnetizedObject.MagnetListener mIndividualBubbleMagnetListener;
    private LayoutInflater mInflater;
    private boolean mIsBubbleSwitchAnimating;
    private boolean mIsDraggingStack;
    private boolean mIsExpanded;
    private boolean mIsExpansionAnimating;
    private boolean mIsGestureInProgress;
    private MagnetizedObject.MagneticTarget mMagneticTarget;
    private MagnetizedObject<?> mMagnetizedObject;
    private BubbleManageEducationView mManageEducationView;
    private ViewGroup mManageMenu;
    private ImageView mManageSettingsIcon;
    private TextView mManageSettingsText;
    private PhysicsAnimator.SpringConfig mManageSpringConfig;
    private int mMaxBubbles;
    public final Consumer<Boolean> mOnImeVisibilityChanged;
    private int mOrientation;
    private View.OnLayoutChangeListener mOrientationChangedListener;
    private int mPointerIndexDown;
    private RelativeStackPosition mRelativeStackPositionBeforeRotation;
    private final PhysicsAnimator.SpringConfig mScaleInSpringConfig;
    private final PhysicsAnimator.SpringConfig mScaleOutSpringConfig;
    private boolean mShouldShowManageEducation;
    private boolean mShouldShowUserEducation;
    private boolean mShowingDismiss;
    private boolean mShowingManage;
    private StackAnimationController mStackAnimationController;
    private final MagnetizedObject.MagnetListener mStackMagnetListener;
    private boolean mStackOnLeftOrWillBe;
    private int mStatusBarHeight;
    private final SurfaceSynchronizer mSurfaceSynchronizer;
    private SysUiState mSysUiState;
    private ViewTreeObserver.OnDrawListener mSystemGestureExcludeUpdater;
    private final List<Rect> mSystemGestureExclusionRects;
    private Rect mTempRect;
    private boolean mTemporarilyInvisible;
    private final PhysicsAnimator.SpringConfig mTranslateSpringConfig;
    private Consumer<String> mUnbubbleConversationCallback;
    private View mUserEducationView;
    private boolean mViewUpdatedRequested;
    private ViewTreeObserver.OnPreDrawListener mViewUpdater;
    private static final PhysicsAnimator.SpringConfig FLYOUT_IME_ANIMATION_SPRING_CONFIG = new PhysicsAnimator.SpringConfig(200.0f, 0.9f);
    private static final SurfaceSynchronizer DEFAULT_SURFACE_SYNCHRONIZER = new SurfaceSynchronizer() { // from class: com.android.systemui.bubbles.BubbleStackView.1
        @Override // com.android.systemui.bubbles.BubbleStackView.SurfaceSynchronizer
        public void syncSurfaceAndRun(final Runnable runnable) {
            Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() { // from class: com.android.systemui.bubbles.BubbleStackView.1.1
                private int mFrameWait = 2;

                @Override // android.view.Choreographer.FrameCallback
                public void doFrame(long j) {
                    int i = this.mFrameWait - 1;
                    this.mFrameWait = i;
                    if (i > 0) {
                        Choreographer.getInstance().postFrameCallback(this);
                    } else {
                        runnable.run();
                    }
                }
            });
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public interface SurfaceSynchronizer {
        void syncSurfaceAndRun(Runnable runnable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$800(BubbleStackView bubbleStackView) {
        bubbleStackView.dismissMagnetizedObject();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        animateFlyoutCollapsed(true, 0.0f);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Stack view state:");
        printWriter.print("  gestureInProgress:       ");
        printWriter.println(this.mIsGestureInProgress);
        printWriter.print("  showingDismiss:          ");
        printWriter.println(this.mShowingDismiss);
        printWriter.print("  isExpansionAnimating:    ");
        printWriter.println(this.mIsExpansionAnimating);
        printWriter.print("  expandedContainerVis:    ");
        printWriter.println(this.mExpandedViewContainer.getVisibility());
        printWriter.print("  expandedContainerAlpha:  ");
        printWriter.println(this.mExpandedViewContainer.getAlpha());
        printWriter.print("  expandedContainerMatrix: ");
        printWriter.println(this.mExpandedViewContainer.getAnimationMatrix());
        this.mStackAnimationController.dump(fileDescriptor, printWriter, strArr);
        this.mExpandedAnimationController.dump(fileDescriptor, printWriter, strArr);
        if (this.mExpandedBubble != null) {
            printWriter.println("Expanded bubble state:");
            printWriter.println("  expandedBubbleKey: " + this.mExpandedBubble.getKey());
            BubbleExpandedView expandedView = this.mExpandedBubble.getExpandedView();
            if (expandedView != null) {
                printWriter.println("  expandedViewVis:    " + expandedView.getVisibility());
                printWriter.println("  expandedViewAlpha:  " + expandedView.getAlpha());
                printWriter.println("  expandedViewTaskId: " + expandedView.getTaskId());
                ActivityView activityView = expandedView.getActivityView();
                if (activityView != null) {
                    printWriter.println("  activityViewVis:    " + activityView.getVisibility());
                    printWriter.println("  activityViewAlpha:  " + activityView.getAlpha());
                    return;
                }
                printWriter.println("  activityView is null");
                return;
            }
            printWriter.println("Expanded bubble view state: expanded bubble view is null");
            return;
        }
        printWriter.println("Expanded bubble state: expanded bubble is null");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
        if (this.mFlyoutDragDeltaX == 0.0f) {
            this.mFlyout.postDelayed(this.mHideFlyout, 5000L);
        } else {
            this.mFlyout.hideFlyout();
        }
    }

    /* renamed from: com.android.systemui.bubbles.BubbleStackView$4, reason: invalid class name */
    class AnonymousClass4 implements MagnetizedObject.MagnetListener {
        AnonymousClass4() {
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            if (BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble() == null) {
                return;
            }
            BubbleStackView bubbleStackView = BubbleStackView.this;
            bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mExpandedAnimationController.getDraggedOutBubble(), true);
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
            if (BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble() == null) {
                return;
            }
            BubbleStackView bubbleStackView = BubbleStackView.this;
            bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mExpandedAnimationController.getDraggedOutBubble(), false);
            if (!z) {
                BubbleStackView.this.mExpandedAnimationController.onUnstuckFromTarget();
            } else {
                BubbleStackView.this.mExpandedAnimationController.snapBubbleBack(BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble(), f, f2);
                BubbleStackView.this.hideDismissTarget();
            }
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            if (BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble() == null) {
                return;
            }
            ExpandedAnimationController expandedAnimationController = BubbleStackView.this.mExpandedAnimationController;
            View draggedOutBubble = BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble();
            float height = BubbleStackView.this.mDismissTargetContainer.getHeight();
            final BubbleStackView bubbleStackView = BubbleStackView.this;
            expandedAnimationController.dismissDraggedOutBubble(draggedOutBubble, height, new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$4$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    BubbleStackView.access$800(bubbleStackView);
                }
            });
            BubbleStackView.this.hideDismissTarget();
        }
    }

    /* renamed from: com.android.systemui.bubbles.BubbleStackView$5, reason: invalid class name */
    class AnonymousClass5 implements MagnetizedObject.MagnetListener {
        AnonymousClass5() {
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            BubbleStackView bubbleStackView = BubbleStackView.this;
            bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mBubbleContainer, true);
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
            BubbleStackView bubbleStackView = BubbleStackView.this;
            bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mBubbleContainer, false);
            if (!z) {
                BubbleStackView.this.mStackAnimationController.onUnstuckFromTarget();
            } else {
                BubbleStackView.this.mStackAnimationController.flingStackThenSpringToEdge(BubbleStackView.this.mStackAnimationController.getStackPosition().x, f, f2);
                BubbleStackView.this.hideDismissTarget();
            }
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            BubbleStackView.this.mStackAnimationController.animateStackDismissal(BubbleStackView.this.mDismissTargetContainer.getHeight(), new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$5$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onReleasedInTarget$0();
                }
            });
            BubbleStackView.this.hideDismissTarget();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReleasedInTarget$0() {
            BubbleStackView.this.resetDesaturationAndDarken();
            BubbleStackView.this.dismissMagnetizedObject();
        }
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public BubbleStackView(Context context, BubbleData bubbleData, SurfaceSynchronizer surfaceSynchronizer, FloatingContentCoordinator floatingContentCoordinator, SysUiState sysUiState, final Runnable runnable, final Consumer<Boolean> consumer, Runnable runnable2) throws Resources.NotFoundException {
        super(context);
        this.mScaleInSpringConfig = new PhysicsAnimator.SpringConfig(300.0f, 0.9f);
        this.mScaleOutSpringConfig = new PhysicsAnimator.SpringConfig(900.0f, 1.0f);
        this.mTranslateSpringConfig = new PhysicsAnimator.SpringConfig(200.0f, 1.0f);
        this.mDelayedAnimationHandler = new Handler();
        this.mDesaturateAndDarkenPaint = new Paint();
        this.mExpandedViewContainerMatrix = new AnimatableScaleMatrix();
        this.mHideFlyout = new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda22
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$0();
            }
        };
        this.mBubbleToExpandAfterFlyoutCollapse = null;
        this.mStackOnLeftOrWillBe = true;
        this.mIsGestureInProgress = false;
        this.mTemporarilyInvisible = false;
        this.mIsDraggingStack = false;
        this.mPointerIndexDown = -1;
        this.mViewUpdatedRequested = false;
        this.mIsExpansionAnimating = false;
        this.mIsBubbleSwitchAnimating = false;
        this.mShowingDismiss = false;
        this.mTempRect = new Rect();
        this.mSystemGestureExclusionRects = Collections.singletonList(new Rect());
        this.mViewUpdater = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.bubbles.BubbleStackView.2
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                BubbleStackView.this.getViewTreeObserver().removeOnPreDrawListener(BubbleStackView.this.mViewUpdater);
                BubbleStackView.this.updateExpandedView();
                BubbleStackView.this.mViewUpdatedRequested = false;
                return true;
            }
        };
        this.mSystemGestureExcludeUpdater = new ViewTreeObserver.OnDrawListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda12
            @Override // android.view.ViewTreeObserver.OnDrawListener
            public final void onDraw() {
                this.f$0.updateSystemGestureExcludeRects();
            }
        };
        FloatPropertyCompat floatPropertyCompat = new FloatPropertyCompat("FlyoutCollapseSpring") { // from class: com.android.systemui.bubbles.BubbleStackView.3
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(Object obj) {
                return BubbleStackView.this.mFlyoutDragDeltaX;
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(Object obj, float f) {
                BubbleStackView.this.setFlyoutStateForDragLength(f);
            }
        };
        this.mFlyoutCollapseProperty = floatPropertyCompat;
        SpringAnimation springAnimation = new SpringAnimation(this, floatPropertyCompat);
        this.mFlyoutTransitionSpring = springAnimation;
        this.mFlyoutDragDeltaX = 0.0f;
        DynamicAnimation.OnAnimationEndListener onAnimationEndListener = new DynamicAnimation.OnAnimationEndListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda13
            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
            public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                this.f$0.lambda$new$1(dynamicAnimation, z, f, f2);
            }
        };
        this.mAfterFlyoutTransitionSpring = onAnimationEndListener;
        this.mIndividualBubbleMagnetListener = new AnonymousClass4();
        this.mStackMagnetListener = new AnonymousClass5();
        this.mBubbleClickListener = new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Bubble bubbleWithView;
                BubbleStackView.this.mIsDraggingStack = false;
                if (BubbleStackView.this.mIsExpansionAnimating || BubbleStackView.this.mIsBubbleSwitchAnimating || (bubbleWithView = BubbleStackView.this.mBubbleData.getBubbleWithView(view)) == null) {
                    return;
                }
                boolean zEquals = bubbleWithView.getKey().equals(BubbleStackView.this.mExpandedBubble.getKey());
                if (BubbleStackView.this.isExpanded()) {
                    BubbleStackView.this.mExpandedAnimationController.onGestureFinished();
                }
                if (!BubbleStackView.this.isExpanded() || zEquals) {
                    if (BubbleStackView.this.maybeShowStackUserEducation()) {
                        return;
                    }
                    BubbleStackView.this.mBubbleData.setExpanded(!BubbleStackView.this.mBubbleData.isExpanded());
                } else if (bubbleWithView != BubbleStackView.this.mBubbleData.getSelectedBubble()) {
                    BubbleStackView.this.mBubbleData.setSelectedBubble(bubbleWithView);
                } else {
                    BubbleStackView.this.setSelectedBubble(bubbleWithView);
                }
            }
        };
        this.mBubbleTouchListener = new RelativeTouchListener() { // from class: com.android.systemui.bubbles.BubbleStackView.7
            @Override // com.android.systemui.util.RelativeTouchListener
            public boolean onDown(View view, MotionEvent motionEvent) {
                if (BubbleStackView.this.mIsExpansionAnimating) {
                    return true;
                }
                if (BubbleStackView.this.mShowingManage) {
                    BubbleStackView.this.showManageMenu(false);
                }
                if (!BubbleStackView.this.mBubbleData.isExpanded()) {
                    BubbleStackView.this.mStackAnimationController.cancelStackPositionAnimations();
                    BubbleStackView.this.mBubbleContainer.setActiveController(BubbleStackView.this.mStackAnimationController);
                    BubbleStackView.this.hideFlyoutImmediate();
                    BubbleStackView bubbleStackView = BubbleStackView.this;
                    bubbleStackView.mMagnetizedObject = bubbleStackView.mStackAnimationController.getMagnetizedStack(BubbleStackView.this.mMagneticTarget);
                    BubbleStackView.this.mMagnetizedObject.setMagnetListener(BubbleStackView.this.mStackMagnetListener);
                    BubbleStackView.this.mIsDraggingStack = true;
                    BubbleStackView.this.updateTemporarilyInvisibleAnimation(false);
                } else {
                    BubbleStackView.this.maybeShowManageEducation(false);
                    BubbleStackView.this.mExpandedAnimationController.prepareForBubbleDrag(view, BubbleStackView.this.mMagneticTarget, BubbleStackView.this.mIndividualBubbleMagnetListener);
                    BubbleStackView.this.hideCurrentInputMethod();
                    BubbleStackView bubbleStackView2 = BubbleStackView.this;
                    bubbleStackView2.mMagnetizedObject = bubbleStackView2.mExpandedAnimationController.getMagnetizedBubbleDraggingOut();
                }
                BubbleStackView.this.passEventToMagnetizedObject(motionEvent);
                return true;
            }

            @Override // com.android.systemui.util.RelativeTouchListener
            public void onMove(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4) {
                if (BubbleStackView.this.mIsExpansionAnimating) {
                    return;
                }
                BubbleStackView.this.springInDismissTargetMaybe();
                if (BubbleStackView.this.passEventToMagnetizedObject(motionEvent)) {
                    return;
                }
                if (BubbleStackView.this.mBubbleData.isExpanded()) {
                    BubbleStackView.this.mExpandedAnimationController.dragBubbleOut(view, f + f3, f2 + f4);
                } else {
                    BubbleStackView.this.hideStackUserEducation(false);
                    BubbleStackView.this.mStackAnimationController.moveStackFromTouch(f + f3, f2 + f4);
                }
            }

            @Override // com.android.systemui.util.RelativeTouchListener
            public void onUp(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4, float f5, float f6) {
                if (BubbleStackView.this.mIsExpansionAnimating) {
                    return;
                }
                if (!BubbleStackView.this.passEventToMagnetizedObject(motionEvent)) {
                    if (BubbleStackView.this.mBubbleData.isExpanded()) {
                        BubbleStackView.this.mExpandedAnimationController.snapBubbleBack(view, f5, f6);
                    } else {
                        BubbleStackView bubbleStackView = BubbleStackView.this;
                        bubbleStackView.mStackOnLeftOrWillBe = bubbleStackView.mStackAnimationController.flingStackThenSpringToEdge(f + f3, f5, f6) <= 0.0f;
                        BubbleStackView.this.updateBubbleZOrdersAndDotPosition(true);
                        BubbleStackView.this.logBubbleEvent(null, 7);
                    }
                    BubbleStackView.this.hideDismissTarget();
                }
                BubbleStackView.this.mIsDraggingStack = false;
                BubbleStackView.this.updateTemporarilyInvisibleAnimation(false);
            }
        };
        this.mFlyoutClickListener = new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (BubbleStackView.this.maybeShowStackUserEducation()) {
                    BubbleStackView.this.mBubbleToExpandAfterFlyoutCollapse = null;
                } else {
                    BubbleStackView bubbleStackView = BubbleStackView.this;
                    bubbleStackView.mBubbleToExpandAfterFlyoutCollapse = bubbleStackView.mBubbleData.getSelectedBubble();
                }
                BubbleStackView.this.mFlyout.removeCallbacks(BubbleStackView.this.mHideFlyout);
                BubbleStackView.this.mHideFlyout.run();
            }
        };
        this.mFlyoutTouchListener = new RelativeTouchListener() { // from class: com.android.systemui.bubbles.BubbleStackView.9
            @Override // com.android.systemui.util.RelativeTouchListener
            public boolean onDown(View view, MotionEvent motionEvent) {
                BubbleStackView.this.mFlyout.removeCallbacks(BubbleStackView.this.mHideFlyout);
                return true;
            }

            @Override // com.android.systemui.util.RelativeTouchListener
            public void onMove(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4) {
                BubbleStackView.this.setFlyoutStateForDragLength(f3);
            }

            @Override // com.android.systemui.util.RelativeTouchListener
            public void onUp(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4, float f5, float f6) {
                boolean zIsStackOnLeftSide = BubbleStackView.this.mStackAnimationController.isStackOnLeftSide();
                boolean z = true;
                boolean z2 = !zIsStackOnLeftSide ? f5 <= 2000.0f : f5 >= -2000.0f;
                boolean z3 = !zIsStackOnLeftSide ? f3 <= ((float) BubbleStackView.this.mFlyout.getWidth()) * 0.25f : f3 >= ((float) (-BubbleStackView.this.mFlyout.getWidth())) * 0.25f;
                boolean z4 = !zIsStackOnLeftSide ? f5 >= 0.0f : f5 <= 0.0f;
                if (!z2 && (!z3 || z4)) {
                    z = false;
                }
                BubbleStackView.this.mFlyout.removeCallbacks(BubbleStackView.this.mHideFlyout);
                BubbleStackView.this.animateFlyoutCollapsed(z, f5);
                BubbleStackView.this.maybeShowStackUserEducation();
            }
        };
        this.mDismissTargetSpring = new PhysicsAnimator.SpringConfig(200.0f, 0.75f);
        this.mOrientation = 0;
        this.mShowingManage = false;
        this.mManageSpringConfig = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
        this.mAnimateTemporarilyInvisibleImmediate = new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda38
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$9();
            }
        };
        this.mBubbleData = bubbleData;
        this.mInflater = LayoutInflater.from(context);
        this.mSysUiState = sysUiState;
        Resources resources = getResources();
        this.mMaxBubbles = resources.getInteger(R.integer.bubbles_max_rendered);
        this.mBubbleSize = resources.getDimensionPixelSize(R.dimen.individual_bubble_size);
        int i = R.dimen.bubble_elevation;
        this.mBubbleElevation = resources.getDimensionPixelSize(i);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(R.dimen.bubble_padding_top);
        this.mBubbleTouchPadding = resources.getDimensionPixelSize(R.dimen.bubble_touch_padding);
        this.mStatusBarHeight = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        this.mImeOffset = resources.getDimensionPixelSize(R.dimen.pip_ime_offset);
        this.mDisplaySize = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(this.mDisplaySize);
        this.mExpandedViewPadding = resources.getDimensionPixelSize(R.dimen.bubble_expanded_view_padding);
        int dimensionPixelSize = resources.getDimensionPixelSize(i);
        TypedArray typedArrayObtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.dialogCornerRadius});
        this.mCornerRadius = typedArrayObtainStyledAttributes.getDimensionPixelSize(0, 0);
        typedArrayObtainStyledAttributes.recycle();
        Runnable runnable3 = new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda45
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$2(runnable);
            }
        };
        this.mStackAnimationController = new StackAnimationController(floatingContentCoordinator, new IntSupplier() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda52
            @Override // java.util.function.IntSupplier
            public final int getAsInt() {
                return this.f$0.getBubbleCount();
            }
        }, runnable3);
        this.mExpandedAnimationController = new ExpandedAnimationController(this.mDisplaySize, this.mExpandedViewPadding, resources.getConfiguration().orientation, runnable3);
        this.mSurfaceSynchronizer = surfaceSynchronizer != null ? surfaceSynchronizer : DEFAULT_SURFACE_SYNCHRONIZER;
        setUpUserEducation();
        setLayoutDirection(0);
        PhysicsAnimationLayout physicsAnimationLayout = new PhysicsAnimationLayout(context);
        this.mBubbleContainer = physicsAnimationLayout;
        physicsAnimationLayout.setActiveController(this.mStackAnimationController);
        float f = dimensionPixelSize;
        this.mBubbleContainer.setElevation(f);
        this.mBubbleContainer.setClipChildren(false);
        addView(this.mBubbleContainer, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout frameLayout = new FrameLayout(context);
        this.mExpandedViewContainer = frameLayout;
        frameLayout.setElevation(f);
        this.mExpandedViewContainer.setClipChildren(false);
        addView(this.mExpandedViewContainer);
        FrameLayout frameLayout2 = new FrameLayout(getContext());
        this.mAnimatingOutSurfaceContainer = frameLayout2;
        frameLayout2.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        addView(this.mAnimatingOutSurfaceContainer);
        SurfaceView surfaceView = new SurfaceView(getContext());
        this.mAnimatingOutSurfaceView = surfaceView;
        surfaceView.setUseAlpha();
        this.mAnimatingOutSurfaceView.setZOrderOnTop(true);
        this.mAnimatingOutSurfaceView.setCornerRadius(this.mCornerRadius);
        this.mAnimatingOutSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        this.mAnimatingOutSurfaceContainer.addView(this.mAnimatingOutSurfaceView);
        FrameLayout frameLayout3 = this.mAnimatingOutSurfaceContainer;
        int i2 = this.mExpandedViewPadding;
        frameLayout3.setPadding(i2, i2, i2, i2);
        setUpManageMenu();
        setUpFlyout();
        springAnimation.setSpring(new SpringForce().setStiffness(200.0f).setDampingRatio(0.75f));
        springAnimation.addEndListener(onAnimationEndListener);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.dismiss_circle_size);
        this.mDismissTargetCircle = new DismissCircleView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize2, dimensionPixelSize2);
        layoutParams.gravity = 81;
        this.mDismissTargetCircle.setLayoutParams(layoutParams);
        this.mDismissTargetAnimator = PhysicsAnimator.getInstance(this.mDismissTargetCircle);
        FrameLayout frameLayout4 = new FrameLayout(context);
        this.mDismissTargetContainer = frameLayout4;
        frameLayout4.setLayoutParams(new FrameLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.floating_dismiss_gradient_height), 80));
        this.mDismissTargetContainer.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.floating_dismiss_bottom_margin));
        this.mDismissTargetContainer.setClipToPadding(false);
        this.mDismissTargetContainer.setClipChildren(false);
        this.mDismissTargetContainer.addView(this.mDismissTargetCircle);
        this.mDismissTargetContainer.setVisibility(4);
        this.mDismissTargetContainer.setBackgroundResource(R.drawable.floating_dismiss_gradient_transition);
        addView(this.mDismissTargetContainer);
        this.mDismissTargetCircle.setTranslationY(getResources().getDimensionPixelSize(r8));
        this.mMagneticTarget = new MagnetizedObject.MagneticTarget(this.mDismissTargetCircle, Settings.Secure.getInt(getContext().getContentResolver(), "bubble_dismiss_radius", this.mBubbleSize * 2));
        setClipChildren(false);
        setFocusable(true);
        this.mBubbleContainer.bringToFront();
        setUpOverflow();
        this.mOnImeVisibilityChanged = consumer;
        this.mHideCurrentInputMethodCallback = runnable2;
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda1
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return this.f$0.lambda$new$4(consumer, view, windowInsets);
            }
        });
        this.mOrientationChangedListener = new View.OnLayoutChangeListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda10
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
                this.f$0.lambda$new$6(view, i3, i4, i5, i6, i7, i8, i9, i10);
            }
        };
        getViewTreeObserver().addOnDrawListener(this.mSystemGestureExcludeUpdater);
        final ColorMatrix colorMatrix = new ColorMatrix();
        final ColorMatrix colorMatrix2 = new ColorMatrix();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        this.mDesaturateAndDarkenAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$new$7(colorMatrix, colorMatrix2, valueAnimator);
            }
        });
        setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda11
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.lambda$new$8(view, motionEvent);
            }
        });
        animate().setInterpolator(Interpolators.PANEL_CLOSE_ACCELERATED).setDuration(320L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2(Runnable runnable) {
        if (getBubbleCount() == 0) {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ WindowInsets lambda$new$4(Consumer consumer, View view, final WindowInsets windowInsets) {
        consumer.accept(Boolean.valueOf(windowInsets.getInsets(WindowInsets.Type.ime()).bottom > 0));
        if (!this.mIsExpanded || this.mIsExpansionAnimating) {
            return view.onApplyWindowInsets(windowInsets);
        }
        this.mExpandedAnimationController.updateYPosition(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda41
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$3(windowInsets);
            }
        });
        return view.onApplyWindowInsets(windowInsets);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(WindowInsets windowInsets) {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null) {
            return;
        }
        this.mExpandedBubble.getExpandedView().updateInsets(windowInsets);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$6(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int iMax;
        int safeInsetRight;
        this.mExpandedAnimationController.updateResources(this.mOrientation, this.mDisplaySize);
        this.mStackAnimationController.updateResources(this.mOrientation);
        this.mBubbleOverflow.updateDimensions();
        WindowInsets rootWindowInsets = getRootWindowInsets();
        int i9 = this.mExpandedViewPadding;
        if (rootWindowInsets != null) {
            DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
            int safeInsetLeft = 0;
            if (displayCutout != null) {
                safeInsetLeft = displayCutout.getSafeInsetLeft();
                safeInsetRight = displayCutout.getSafeInsetRight();
            } else {
                safeInsetRight = 0;
            }
            int iMax2 = Math.max(safeInsetLeft, rootWindowInsets.getStableInsetLeft()) + i9;
            iMax = i9 + Math.max(safeInsetRight, rootWindowInsets.getStableInsetRight());
            i9 = iMax2;
        } else {
            iMax = i9;
        }
        FrameLayout frameLayout = this.mExpandedViewContainer;
        int i10 = this.mExpandedViewPadding;
        frameLayout.setPadding(i9, i10, iMax, i10);
        if (this.mIsExpanded) {
            beforeExpandedViewAnimation();
            updateOverflowVisibility();
            updatePointerPosition();
            this.mExpandedAnimationController.expandFromStack(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda25
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$new$5();
                }
            });
            this.mExpandedViewContainer.setTranslationX(0.0f);
            this.mExpandedViewContainer.setTranslationY(getExpandedViewY());
            this.mExpandedViewContainer.setAlpha(1.0f);
        }
        RelativeStackPosition relativeStackPosition = this.mRelativeStackPositionBeforeRotation;
        if (relativeStackPosition != null) {
            this.mStackAnimationController.setStackPosition(relativeStackPosition);
            this.mRelativeStackPositionBeforeRotation = null;
        }
        removeOnLayoutChangeListener(this.mOrientationChangedListener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$7(ColorMatrix colorMatrix, ColorMatrix colorMatrix2, ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        colorMatrix.setSaturation(fFloatValue);
        float f = 1.0f - ((1.0f - fFloatValue) * 0.3f);
        colorMatrix2.setScale(f, f, f, 1.0f);
        colorMatrix.postConcat(colorMatrix2);
        this.mDesaturateAndDarkenPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        View view = this.mDesaturateAndDarkenTargetView;
        if (view != null) {
            view.setLayerPaint(this.mDesaturateAndDarkenPaint);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$new$8(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() != 0) {
            return true;
        }
        if (this.mShowingManage) {
            showManageMenu(false);
            return true;
        }
        if (!this.mBubbleData.isExpanded()) {
            return true;
        }
        this.mBubbleData.setExpanded(false);
        return true;
    }

    public void setTemporarilyInvisible(boolean z) {
        this.mTemporarilyInvisible = z;
        updateTemporarilyInvisibleAnimation(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTemporarilyInvisibleAnimation(boolean z) {
        removeCallbacks(this.mAnimateTemporarilyInvisibleImmediate);
        if (this.mIsDraggingStack) {
            return;
        }
        postDelayed(this.mAnimateTemporarilyInvisibleImmediate, (!(this.mTemporarilyInvisible && this.mFlyout.getVisibility() != 0) || z) ? 0L : 1000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$9() {
        if (this.mTemporarilyInvisible && this.mFlyout.getVisibility() != 0) {
            if (this.mStackAnimationController.isStackOnLeftSide()) {
                animate().translationX(-this.mBubbleSize).start();
                return;
            } else {
                animate().translationX(this.mBubbleSize).start();
                return;
            }
        }
        animate().translationX(0.0f).start();
    }

    private void setUpManageMenu() {
        ViewGroup viewGroup = this.mManageMenu;
        if (viewGroup != null) {
            removeView(viewGroup);
        }
        ViewGroup viewGroup2 = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.bubble_manage_menu, (ViewGroup) this, false);
        this.mManageMenu = viewGroup2;
        viewGroup2.setVisibility(4);
        PhysicsAnimator.getInstance(this.mManageMenu).setDefaultSpringConfig(this.mManageSpringConfig);
        this.mManageMenu.setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.bubbles.BubbleStackView.10
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), BubbleStackView.this.mCornerRadius);
            }
        });
        this.mManageMenu.setClipToOutline(true);
        this.mManageMenu.findViewById(R.id.bubble_manage_menu_dismiss_container).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$setUpManageMenu$10(view);
            }
        });
        this.mManageMenu.findViewById(R.id.bubble_manage_menu_dont_bubble_container).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$setUpManageMenu$11(view);
            }
        });
        this.mManageMenu.findViewById(R.id.bubble_manage_menu_settings_container).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$setUpManageMenu$13(view);
            }
        });
        this.mManageSettingsIcon = (ImageView) this.mManageMenu.findViewById(R.id.bubble_manage_menu_settings_icon);
        this.mManageSettingsText = (TextView) this.mManageMenu.findViewById(R.id.bubble_manage_menu_settings_name);
        this.mManageMenu.setLayoutDirection(3);
        addView(this.mManageMenu);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setUpManageMenu$10(View view) {
        showManageMenu(false);
        dismissBubbleIfExists(this.mBubbleData.getSelectedBubble());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setUpManageMenu$11(View view) {
        showManageMenu(false);
        this.mUnbubbleConversationCallback.accept(this.mBubbleData.getSelectedBubble().getKey());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setUpManageMenu$13(View view) {
        showManageMenu(false);
        final Bubble selectedBubble = this.mBubbleData.getSelectedBubble();
        if (selectedBubble == null || !this.mBubbleData.hasBubbleInStackWithKey(selectedBubble.getKey())) {
            return;
        }
        final Intent settingsIntent = selectedBubble.getSettingsIntent(((FrameLayout) this).mContext);
        collapseStack(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda40
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setUpManageMenu$12(settingsIntent, selectedBubble);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setUpManageMenu$12(Intent intent, Bubble bubble) {
        ((FrameLayout) this).mContext.startActivityAsUser(intent, bubble.getUser());
        logBubbleEvent(bubble, 9);
    }

    private void setUpUserEducation() {
        View view = this.mUserEducationView;
        if (view != null) {
            removeView(view);
        }
        boolean zShouldShowBubblesEducation = shouldShowBubblesEducation();
        this.mShouldShowUserEducation = zShouldShowBubblesEducation;
        if (zShouldShowBubblesEducation) {
            View viewInflate = this.mInflater.inflate(R.layout.bubble_stack_user_education, (ViewGroup) this, false);
            this.mUserEducationView = viewInflate;
            viewInflate.setVisibility(8);
            TypedArray typedArrayObtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.colorAccent, android.R.attr.textColorPrimaryInverse});
            int color = typedArrayObtainStyledAttributes.getColor(0, -16777216);
            int color2 = typedArrayObtainStyledAttributes.getColor(1, -1);
            typedArrayObtainStyledAttributes.recycle();
            int iEnsureTextContrast = ContrastColorUtil.ensureTextContrast(color2, color, true);
            TextView textView = (TextView) this.mUserEducationView.findViewById(R.id.user_education_title);
            TextView textView2 = (TextView) this.mUserEducationView.findViewById(R.id.user_education_description);
            textView.setTextColor(iEnsureTextContrast);
            textView2.setTextColor(iEnsureTextContrast);
            updateUserEducationForLayoutDirection();
            addView(this.mUserEducationView);
        }
        BubbleManageEducationView bubbleManageEducationView = this.mManageEducationView;
        if (bubbleManageEducationView != null) {
            removeView(bubbleManageEducationView);
        }
        boolean zShouldShowManageEducation = shouldShowManageEducation();
        this.mShouldShowManageEducation = zShouldShowManageEducation;
        if (zShouldShowManageEducation) {
            BubbleManageEducationView bubbleManageEducationView2 = (BubbleManageEducationView) this.mInflater.inflate(R.layout.bubbles_manage_button_education, (ViewGroup) this, false);
            this.mManageEducationView = bubbleManageEducationView2;
            bubbleManageEducationView2.setVisibility(8);
            this.mManageEducationView.setElevation(this.mBubbleElevation);
            this.mManageEducationView.setLayoutDirection(3);
            addView(this.mManageEducationView);
        }
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void setUpFlyout() {
        BubbleFlyoutView bubbleFlyoutView = this.mFlyout;
        if (bubbleFlyoutView != null) {
            removeView(bubbleFlyoutView);
        }
        BubbleFlyoutView bubbleFlyoutView2 = new BubbleFlyoutView(getContext());
        this.mFlyout = bubbleFlyoutView2;
        bubbleFlyoutView2.setVisibility(8);
        this.mFlyout.animate().setDuration(100L).setInterpolator(new AccelerateDecelerateInterpolator());
        this.mFlyout.setOnClickListener(this.mFlyoutClickListener);
        this.mFlyout.setOnTouchListener(this.mFlyoutTouchListener);
        addView(this.mFlyout, new FrameLayout.LayoutParams(-2, -2));
    }

    private void setUpOverflow() {
        int childCount;
        BubbleOverflow bubbleOverflow = this.mBubbleOverflow;
        if (bubbleOverflow == null) {
            BubbleOverflow bubbleOverflow2 = new BubbleOverflow(getContext());
            this.mBubbleOverflow = bubbleOverflow2;
            bubbleOverflow2.setUpOverflow(this.mBubbleContainer, this);
            childCount = 0;
        } else {
            this.mBubbleContainer.removeView(bubbleOverflow.getIconView());
            this.mBubbleOverflow.setUpOverflow(this.mBubbleContainer, this);
            childCount = this.mBubbleContainer.getChildCount();
        }
        this.mBubbleContainer.addView(this.mBubbleOverflow.getIconView(), childCount, new FrameLayout.LayoutParams(-2, -2));
        this.mBubbleOverflow.getIconView().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$setUpOverflow$14(view);
            }
        });
        updateOverflowVisibility();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setUpOverflow$14(View view) {
        setSelectedBubble(this.mBubbleOverflow);
        showManageMenu(false);
    }

    public void onThemeChanged() {
        setUpFlyout();
        setUpOverflow();
        setUpUserEducation();
        setUpManageMenu();
        updateExpandedViewTheme();
    }

    public void onOrientationChanged(int i) {
        this.mOrientation = i;
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealSize(this.mDisplaySize);
        Resources resources = getContext().getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(R.dimen.bubble_padding_top);
        this.mRelativeStackPositionBeforeRotation = this.mStackAnimationController.getRelativeStackPosition();
        addOnLayoutChangeListener(this.mOrientationChangedListener);
        hideFlyoutImmediate();
        this.mManageMenu.setVisibility(4);
        this.mShowingManage = false;
    }

    public void onLayoutDirectionChanged(int i) {
        this.mManageMenu.setLayoutDirection(i);
        this.mFlyout.setLayoutDirection(i);
        View view = this.mUserEducationView;
        if (view != null) {
            view.setLayoutDirection(i);
            updateUserEducationForLayoutDirection();
        }
        BubbleManageEducationView bubbleManageEducationView = this.mManageEducationView;
        if (bubbleManageEducationView != null) {
            bubbleManageEducationView.setLayoutDirection(i);
        }
        updateExpandedViewDirection(i);
    }

    public void onDisplaySizeChanged() {
        setUpOverflow();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealSize(this.mDisplaySize);
        Resources resources = getContext().getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(R.dimen.bubble_padding_top);
        this.mBubbleSize = getResources().getDimensionPixelSize(R.dimen.individual_bubble_size);
        for (Bubble bubble : this.mBubbleData.getBubbles()) {
            if (bubble.getIconView() == null) {
                Log.d("Bubbles", "Display size changed. Icon null: " + bubble);
            } else {
                BadgedImageView iconView = bubble.getIconView();
                int i = this.mBubbleSize;
                iconView.setLayoutParams(new FrameLayout.LayoutParams(i, i));
            }
        }
        this.mExpandedAnimationController.updateResources(this.mOrientation, this.mDisplaySize);
        this.mStackAnimationController.updateResources(this.mOrientation);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.dismiss_circle_size);
        this.mDismissTargetCircle.getLayoutParams().width = dimensionPixelSize;
        this.mDismissTargetCircle.getLayoutParams().height = dimensionPixelSize;
        this.mDismissTargetCircle.requestLayout();
        this.mMagneticTarget.setMagneticFieldRadiusPx(this.mBubbleSize * 2);
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        this.mTempRect.setEmpty();
        getTouchableRegion(this.mTempRect);
        internalInsetsInfo.touchableRegion.set(this.mTempRect);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mViewUpdater);
        getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        BubbleOverflow bubbleOverflow = this.mBubbleOverflow;
        if (bubbleOverflow == null || bubbleOverflow.getExpandedView() == null) {
            return;
        }
        this.mBubbleOverflow.getExpandedView().cleanUpExpandedState();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        setupLocalMenu(accessibilityNodeInfo);
    }

    void updateExpandedViewTheme() {
        List<Bubble> bubbles = this.mBubbleData.getBubbles();
        if (bubbles.isEmpty()) {
            return;
        }
        bubbles.forEach(new Consumer() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda51
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BubbleStackView.lambda$updateExpandedViewTheme$15((Bubble) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateExpandedViewTheme$15(Bubble bubble) {
        if (bubble.getExpandedView() != null) {
            bubble.getExpandedView().applyThemeAttrs();
        }
    }

    void updateExpandedViewDirection(final int i) {
        List<Bubble> bubbles = this.mBubbleData.getBubbles();
        if (bubbles.isEmpty()) {
            return;
        }
        bubbles.forEach(new Consumer() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda49
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BubbleStackView.lambda$updateExpandedViewDirection$16(i, (Bubble) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateExpandedViewDirection$16(int i, Bubble bubble) {
        if (bubble.getExpandedView() != null) {
            bubble.getExpandedView().setLayoutDirection(i);
        }
    }

    void setupLocalMenu(AccessibilityNodeInfo accessibilityNodeInfo) {
        Resources resources = ((FrameLayout) this).mContext.getResources();
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_top_left, resources.getString(R.string.bubble_accessibility_action_move_top_left)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_top_right, resources.getString(R.string.bubble_accessibility_action_move_top_right)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_bottom_left, resources.getString(R.string.bubble_accessibility_action_move_bottom_left)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_bottom_right, resources.getString(R.string.bubble_accessibility_action_move_bottom_right)));
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS);
        if (this.mIsExpanded) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE);
        } else {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (super.performAccessibilityActionInternal(i, bundle)) {
            return true;
        }
        RectF allowableStackPositionRegion = this.mStackAnimationController.getAllowableStackPositionRegion();
        if (i == 1048576) {
            this.mBubbleData.dismissAll(6);
            announceForAccessibility(getResources().getString(R.string.accessibility_bubble_dismissed));
            return true;
        }
        if (i == 524288) {
            this.mBubbleData.setExpanded(false);
            return true;
        }
        if (i == 262144) {
            this.mBubbleData.setExpanded(true);
            return true;
        }
        if (i == R.id.action_move_top_left) {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.left, allowableStackPositionRegion.top);
            return true;
        }
        if (i == R.id.action_move_top_right) {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.right, allowableStackPositionRegion.top);
            return true;
        }
        if (i == R.id.action_move_bottom_left) {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.left, allowableStackPositionRegion.bottom);
            return true;
        }
        if (i != R.id.action_move_bottom_right) {
            return false;
        }
        this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.right, allowableStackPositionRegion.bottom);
        return true;
    }

    public void updateContentDescription() {
        if (this.mBubbleData.getBubbles().isEmpty()) {
            return;
        }
        for (int i = 0; i < this.mBubbleData.getBubbles().size(); i++) {
            Bubble bubble = this.mBubbleData.getBubbles().get(i);
            String appName = bubble.getAppName();
            String title = bubble.getTitle();
            if (title == null) {
                title = getResources().getString(R.string.notification_bubble_title);
            }
            if (bubble.getIconView() != null) {
                if (this.mIsExpanded || i > 0) {
                    bubble.getIconView().setContentDescription(getResources().getString(R.string.bubble_content_description_single, title, appName));
                } else {
                    bubble.getIconView().setContentDescription(getResources().getString(R.string.bubble_content_description_stack, title, appName, Integer.valueOf(this.mBubbleContainer.getChildCount() - 1)));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSystemGestureExcludeRects() {
        Rect rect = this.mSystemGestureExclusionRects.get(0);
        if (getBubbleCount() > 0) {
            View childAt = this.mBubbleContainer.getChildAt(0);
            rect.set(childAt.getLeft(), childAt.getTop(), childAt.getRight(), childAt.getBottom());
            rect.offset((int) (childAt.getTranslationX() + 0.5f), (int) (childAt.getTranslationY() + 0.5f));
            this.mBubbleContainer.setSystemGestureExclusionRects(this.mSystemGestureExclusionRects);
            return;
        }
        rect.setEmpty();
        this.mBubbleContainer.setSystemGestureExclusionRects(Collections.emptyList());
    }

    public void setExpandListener(BubbleController.BubbleExpandListener bubbleExpandListener) {
        this.mExpandListener = bubbleExpandListener;
    }

    public void setUnbubbleConversationCallback(Consumer<String> consumer) {
        this.mUnbubbleConversationCallback = consumer;
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public boolean isExpansionAnimating() {
        return this.mIsExpansionAnimating;
    }

    BubbleViewProvider getExpandedBubble() {
        return this.mExpandedBubble;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    void addBubble(Bubble bubble) {
        if (getBubbleCount() == 0 && this.mShouldShowUserEducation) {
            StackAnimationController stackAnimationController = this.mStackAnimationController;
            stackAnimationController.setStackPosition(stackAnimationController.getStartPosition());
        }
        if (getBubbleCount() == 0) {
            this.mStackOnLeftOrWillBe = this.mStackAnimationController.isStackOnLeftSide();
        }
        if (bubble.getIconView() == null) {
            return;
        }
        bubble.getIconView().setDotPositionOnLeft(!this.mStackOnLeftOrWillBe, false);
        bubble.getIconView().setOnClickListener(this.mBubbleClickListener);
        bubble.getIconView().setOnTouchListener(this.mBubbleTouchListener);
        this.mBubbleContainer.addView(bubble.getIconView(), 0, new FrameLayout.LayoutParams(-2, -2));
        animateInFlyoutForBubble(bubble);
        requestUpdate();
        logBubbleEvent(bubble, 1);
    }

    void removeBubble(Bubble bubble) {
        for (int i = 0; i < getBubbleCount(); i++) {
            View childAt = this.mBubbleContainer.getChildAt(i);
            if ((childAt instanceof BadgedImageView) && ((BadgedImageView) childAt).getKey().equals(bubble.getKey())) {
                this.mBubbleContainer.removeViewAt(i);
                bubble.cleanupViews();
                updatePointerPosition();
                logBubbleEvent(bubble, 5);
                return;
            }
        }
        Log.d("Bubbles", "was asked to remove Bubble, but didn't find the view! " + bubble);
    }

    private void updateOverflowVisibility() {
        BubbleOverflow bubbleOverflow = this.mBubbleOverflow;
        if (bubbleOverflow == null) {
            return;
        }
        bubbleOverflow.setVisible(this.mIsExpanded ? 0 : 8);
    }

    void updateBubble(Bubble bubble) {
        animateInFlyoutForBubble(bubble);
        requestUpdate();
        logBubbleEvent(bubble, 2);
    }

    public void updateBubbleOrder(List<Bubble> list) {
        for (int i = 0; i < list.size(); i++) {
            this.mBubbleContainer.reorderView(list.get(i).getIconView(), i);
        }
        updateBubbleZOrdersAndDotPosition(false);
        updatePointerPosition();
    }

    public void setSelectedBubble(final BubbleViewProvider bubbleViewProvider) {
        BubbleViewProvider bubbleViewProvider2;
        if (bubbleViewProvider == null) {
            this.mBubbleData.setShowingOverflow(false);
            return;
        }
        if (this.mExpandedBubble == bubbleViewProvider) {
            return;
        }
        if (bubbleViewProvider.getKey() == "Overflow") {
            this.mBubbleData.setShowingOverflow(true);
        } else {
            this.mBubbleData.setShowingOverflow(false);
        }
        if (this.mIsExpanded && this.mIsExpansionAnimating) {
            cancelAllExpandCollapseSwitchAnimations();
        }
        if (this.mIsExpanded && (bubbleViewProvider2 = this.mExpandedBubble) != null && bubbleViewProvider2.getExpandedView() != null) {
            BubbleViewProvider bubbleViewProvider3 = this.mExpandedBubble;
            if (bubbleViewProvider3 != null && bubbleViewProvider3.getExpandedView() != null) {
                this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(true);
            }
            try {
                screenshotAnimatingOutBubbleIntoSurface(new Consumer() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda50
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.lambda$setSelectedBubble$17(bubbleViewProvider, (Boolean) obj);
                    }
                });
                return;
            } catch (Exception e) {
                showNewlySelectedBubble(bubbleViewProvider);
                e.printStackTrace();
                return;
            }
        }
        showNewlySelectedBubble(bubbleViewProvider);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setSelectedBubble$17(BubbleViewProvider bubbleViewProvider, Boolean bool) {
        this.mAnimatingOutSurfaceContainer.setVisibility(bool.booleanValue() ? 0 : 4);
        showNewlySelectedBubble(bubbleViewProvider);
    }

    private void showNewlySelectedBubble(final BubbleViewProvider bubbleViewProvider) {
        final BubbleViewProvider bubbleViewProvider2 = this.mExpandedBubble;
        this.mExpandedBubble = bubbleViewProvider;
        updatePointerPosition();
        if (this.mIsExpanded) {
            hideCurrentInputMethod();
            this.mExpandedViewContainer.setAlpha(0.0f);
            this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda44
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showNewlySelectedBubble$18(bubbleViewProvider2, bubbleViewProvider);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showNewlySelectedBubble$18(BubbleViewProvider bubbleViewProvider, BubbleViewProvider bubbleViewProvider2) {
        if (bubbleViewProvider != null) {
            bubbleViewProvider.setContentVisibility(false);
        }
        updateExpandedBubble();
        requestUpdate();
        logBubbleEvent(bubbleViewProvider, 4);
        logBubbleEvent(bubbleViewProvider2, 3);
        notifyExpansionChanged(bubbleViewProvider, false);
        notifyExpansionChanged(bubbleViewProvider2, true);
    }

    public void setExpanded(boolean z) {
        if (!z) {
            releaseAnimatingOutBubbleBuffer();
        }
        if (z == this.mIsExpanded) {
            return;
        }
        hideCurrentInputMethod();
        this.mSysUiState.setFlag(LineageHardwareManager.FEATURE_READING_ENHANCEMENT, z).commitUpdate(((FrameLayout) this).mContext.getDisplayId());
        if (this.mIsExpanded) {
            animateCollapse();
            logBubbleEvent(this.mExpandedBubble, 4);
        } else {
            animateExpansion();
            logBubbleEvent(this.mExpandedBubble, 3);
            logBubbleEvent(this.mExpandedBubble, 15);
        }
        notifyExpansionChanged(this.mExpandedBubble, this.mIsExpanded);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean maybeShowStackUserEducation() {
        if (!this.mShouldShowUserEducation || this.mUserEducationView.getVisibility() == 0) {
            return false;
        }
        this.mUserEducationView.setAlpha(0.0f);
        this.mUserEducationView.setVisibility(0);
        updateUserEducationForLayoutDirection();
        this.mUserEducationView.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda27
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$maybeShowStackUserEducation$19();
            }
        });
        Prefs.putBoolean(getContext(), "HasSeenBubblesOnboarding", true);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeShowStackUserEducation$19() {
        this.mUserEducationView.setTranslationY((this.mStackAnimationController.getStartPosition().y + (this.mBubbleSize / 2)) - (this.mUserEducationView.getHeight() / 2));
        this.mUserEducationView.animate().setDuration(200L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f);
    }

    private void updateUserEducationForLayoutDirection() {
        View view = this.mUserEducationView;
        if (view == null) {
            return;
        }
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.user_education_view);
        TextView textView = (TextView) this.mUserEducationView.findViewById(R.id.user_education_title);
        TextView textView2 = (TextView) this.mUserEducationView.findViewById(R.id.user_education_description);
        if (getResources().getConfiguration().getLayoutDirection() == 0) {
            this.mUserEducationView.setLayoutDirection(0);
            linearLayout.setBackgroundResource(R.drawable.bubble_stack_user_education_bg);
            textView.setGravity(3);
            textView2.setGravity(3);
            return;
        }
        this.mUserEducationView.setLayoutDirection(1);
        linearLayout.setBackgroundResource(R.drawable.bubble_stack_user_education_bg_rtl);
        textView.setGravity(5);
        textView2.setGravity(5);
    }

    void hideStackUserEducation(boolean z) {
        if (this.mShouldShowUserEducation && this.mUserEducationView.getVisibility() == 0 && !this.mAnimatingEducationAway) {
            this.mAnimatingEducationAway = true;
            this.mUserEducationView.animate().alpha(0.0f).setDuration(z ? 40L : 200L).withEndAction(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda37
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$hideStackUserEducation$20();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hideStackUserEducation$20() {
        this.mAnimatingEducationAway = false;
        this.mShouldShowUserEducation = shouldShowBubblesEducation();
        this.mUserEducationView.setVisibility(8);
    }

    void maybeShowManageEducation(boolean z) {
        BubbleManageEducationView bubbleManageEducationView = this.mManageEducationView;
        if (bubbleManageEducationView == null) {
            return;
        }
        if (z && this.mShouldShowManageEducation && bubbleManageEducationView.getVisibility() != 0 && this.mIsExpanded && this.mExpandedBubble.getExpandedView() != null) {
            this.mManageEducationView.setAlpha(0.0f);
            this.mManageEducationView.setVisibility(0);
            this.mManageEducationView.post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda28
                @Override // java.lang.Runnable
                public final void run() throws Resources.NotFoundException {
                    this.f$0.lambda$maybeShowManageEducation$24();
                }
            });
            Prefs.putBoolean(getContext(), "HasSeenBubblesManageOnboarding", true);
            return;
        }
        if (z || this.mManageEducationView.getVisibility() != 0 || this.mAnimatingManageEducationAway) {
            return;
        }
        this.mManageEducationView.animate().alpha(0.0f).setDuration(this.mIsExpansionAnimating ? 40L : 200L).withEndAction(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda36
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$maybeShowManageEducation$25();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeShowManageEducation$24() throws Resources.NotFoundException {
        this.mExpandedBubble.getExpandedView().getManageButtonBoundsOnScreen(this.mTempRect);
        int manageViewHeight = this.mManageEducationView.getManageViewHeight();
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.bubbles_manage_education_top_inset);
        this.mManageEducationView.bringToFront();
        this.mManageEducationView.setManageViewPosition(0, (this.mTempRect.top - manageViewHeight) + dimensionPixelSize);
        this.mManageEducationView.animate().setDuration(200L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f);
        this.mManageEducationView.findViewById(R.id.manage).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$maybeShowManageEducation$21(view);
            }
        });
        this.mManageEducationView.findViewById(R.id.got_it).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$maybeShowManageEducation$22(view);
            }
        });
        this.mManageEducationView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$maybeShowManageEducation$23(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeShowManageEducation$21(View view) {
        this.mExpandedBubble.getExpandedView().findViewById(R.id.settings_button).performClick();
        maybeShowManageEducation(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeShowManageEducation$22(View view) {
        maybeShowManageEducation(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeShowManageEducation$23(View view) {
        maybeShowManageEducation(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeShowManageEducation$25() {
        this.mAnimatingManageEducationAway = false;
        this.mShouldShowManageEducation = shouldShowManageEducation();
        this.mManageEducationView.setVisibility(8);
    }

    @Deprecated
    void collapseStack(Runnable runnable) {
        this.mBubbleData.setExpanded(false);
        runnable.run();
    }

    void showExpandedViewContents(int i) {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null || this.mExpandedBubble.getExpandedView().getVirtualDisplayId() != i) {
            return;
        }
        this.mExpandedBubble.setContentVisibility(true);
    }

    void hideCurrentInputMethod() {
        this.mHideCurrentInputMethodCallback.run();
    }

    private void beforeExpandedViewAnimation() {
        this.mIsExpansionAnimating = true;
        hideFlyoutImmediate();
        updateExpandedBubble();
        updateExpandedView();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: afterExpandedViewAnimation, reason: merged with bridge method [inline-methods] */
    public void lambda$new$5() {
        this.mIsExpansionAnimating = false;
        updateExpandedView();
        requestUpdate();
    }

    private void animateExpansion() {
        cancelDelayedExpandCollapseSwitchAnimations();
        this.mIsExpanded = true;
        hideStackUserEducation(true);
        beforeExpandedViewAnimation();
        this.mBubbleContainer.setActiveController(this.mExpandedAnimationController);
        updateOverflowVisibility();
        updatePointerPosition();
        this.mExpandedAnimationController.expandFromStack(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateExpansion$26();
            }
        });
        this.mExpandedViewContainer.setTranslationX(0.0f);
        this.mExpandedViewContainer.setTranslationY(getExpandedViewY());
        this.mExpandedViewContainer.setAlpha(1.0f);
        final float bubbleLeft = this.mExpandedAnimationController.getBubbleLeft(this.mBubbleData.getBubbles().indexOf(this.mExpandedBubble));
        long jAbs = getWidth() > 0 ? (long) (((Math.abs(bubbleLeft - this.mStackAnimationController.getStackPosition().x) / getWidth()) * 30.0f) + 175.0f) : 0L;
        this.mExpandedViewContainerMatrix.setScale(0.0f, 0.0f, (this.mBubbleSize / 2.0f) + bubbleLeft, getExpandedViewY());
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null) {
            this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(false);
        }
        this.mDelayedAnimationHandler.postDelayed(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda39
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateExpansion$29(bubbleLeft);
            }
        }, jAbs);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateExpansion$26() {
        lambda$new$5();
        maybeShowManageEducation(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateExpansion$29(final float f) {
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).spring(AnimatableScaleMatrix.SCALE_X, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig).spring(AnimatableScaleMatrix.SCALE_Y, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig).addUpdateListener(new PhysicsAnimator.UpdateListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda15
            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                this.f$0.lambda$animateExpansion$27(f, (AnimatableScaleMatrix) obj, arrayMap);
            }
        }).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda34
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateExpansion$28();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateExpansion$27(float f, AnimatableScaleMatrix animatableScaleMatrix, ArrayMap arrayMap) {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getIconView() == null) {
            return;
        }
        this.mExpandedViewContainerMatrix.postTranslate(this.mExpandedBubble.getIconView().getTranslationX() - f, 0.0f);
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateExpansion$28() {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null) {
            return;
        }
        this.mExpandedBubble.getExpandedView().setContentVisibility(true);
        this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(false);
    }

    private void animateCollapse() {
        cancelDelayedExpandCollapseSwitchAnimations();
        showManageMenu(false);
        this.mIsExpanded = false;
        this.mIsExpansionAnimating = true;
        this.mBubbleContainer.cancelAllAnimations();
        PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).cancel();
        this.mAnimatingOutSurfaceContainer.setScaleX(0.0f);
        this.mAnimatingOutSurfaceContainer.setScaleY(0.0f);
        this.mExpandedAnimationController.notifyPreparingToCollapse();
        this.mDelayedAnimationHandler.postDelayed(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda32
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateCollapse$31();
            }
        }, 105L);
        final View iconView = this.mExpandedBubble.getIconView();
        final float bubbleLeft = this.mExpandedAnimationController.getBubbleLeft(this.mBubbleData.getBubbles().indexOf(this.mExpandedBubble));
        this.mExpandedViewContainerMatrix.setScale(1.0f, 1.0f, (this.mBubbleSize / 2.0f) + bubbleLeft, getExpandedViewY());
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).spring(AnimatableScaleMatrix.SCALE_X, 0.0f, this.mScaleOutSpringConfig).spring(AnimatableScaleMatrix.SCALE_Y, 0.0f, this.mScaleOutSpringConfig).addUpdateListener(new PhysicsAnimator.UpdateListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda16
            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                this.f$0.lambda$animateCollapse$32(iconView, bubbleLeft, (AnimatableScaleMatrix) obj, arrayMap);
            }
        }).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateCollapse$33();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateCollapse$31() {
        this.mExpandedAnimationController.collapseBackToStack(this.mStackAnimationController.getStackPositionAlongNearestHorizontalEdge(), new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda26
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateCollapse$30();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateCollapse$30() {
        this.mBubbleContainer.setActiveController(this.mStackAnimationController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateCollapse$32(View view, float f, AnimatableScaleMatrix animatableScaleMatrix, ArrayMap arrayMap) {
        if (view != null) {
            this.mExpandedViewContainerMatrix.postTranslate(view.getTranslationX() - f, 0.0f);
        }
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
        if (this.mExpandedViewContainerMatrix.getScaleX() < 0.05f) {
            this.mExpandedViewContainer.setVisibility(4);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateCollapse$33() {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        beforeExpandedViewAnimation();
        maybeShowManageEducation(false);
        updateOverflowVisibility();
        lambda$new$5();
        if (bubbleViewProvider != null) {
            bubbleViewProvider.setContentVisibility(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateSwitchBubbles() {
        int iIndexOf;
        if (this.mIsExpanded) {
            this.mIsBubbleSwitchAnimating = true;
            PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).cancel();
            PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).spring(DynamicAnimation.SCALE_X, 0.0f, this.mScaleOutSpringConfig).spring(DynamicAnimation.SCALE_Y, 0.0f, this.mScaleOutSpringConfig).spring(DynamicAnimation.TRANSLATION_Y, this.mAnimatingOutSurfaceContainer.getTranslationY() - (this.mBubbleSize * 2), this.mTranslateSpringConfig).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda23
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.releaseAnimatingOutBubbleBuffer();
                }
            }).start();
            BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
            boolean z = bubbleViewProvider != null && bubbleViewProvider.getKey().equals("Overflow");
            ExpandedAnimationController expandedAnimationController = this.mExpandedAnimationController;
            if (z) {
                iIndexOf = getBubbleCount();
            } else {
                iIndexOf = this.mBubbleData.getBubbles().indexOf(this.mExpandedBubble);
            }
            float bubbleLeft = expandedAnimationController.getBubbleLeft(iIndexOf);
            this.mExpandedViewContainer.setAlpha(1.0f);
            this.mExpandedViewContainer.setVisibility(0);
            this.mExpandedViewContainerMatrix.setScale(0.0f, 0.0f, bubbleLeft + (this.mBubbleSize / 2.0f), getExpandedViewY());
            this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
            this.mDelayedAnimationHandler.postDelayed(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda18
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$animateSwitchBubbles$36();
                }
            }, 25L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateSwitchBubbles$36() {
        if (!this.mIsExpanded) {
            this.mIsBubbleSwitchAnimating = false;
        } else {
            PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
            PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).spring(AnimatableScaleMatrix.SCALE_X, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig).spring(AnimatableScaleMatrix.SCALE_Y, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig).addUpdateListener(new PhysicsAnimator.UpdateListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda14
                @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
                public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                    this.f$0.lambda$animateSwitchBubbles$34((AnimatableScaleMatrix) obj, arrayMap);
                }
            }).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda17
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$animateSwitchBubbles$35();
                }
            }).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateSwitchBubbles$34(AnimatableScaleMatrix animatableScaleMatrix, ArrayMap arrayMap) {
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateSwitchBubbles$35() {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null) {
            this.mExpandedBubble.getExpandedView().setContentVisibility(true);
            this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(false);
        }
        this.mIsBubbleSwitchAnimating = false;
    }

    private void cancelDelayedExpandCollapseSwitchAnimations() {
        this.mDelayedAnimationHandler.removeCallbacksAndMessages(null);
        this.mIsExpansionAnimating = false;
        this.mIsBubbleSwitchAnimating = false;
    }

    private void cancelAllExpandCollapseSwitchAnimations() {
        cancelDelayedExpandCollapseSwitchAnimations();
        PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceView).cancel();
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        this.mExpandedViewContainer.setAnimationMatrix(null);
    }

    private void notifyExpansionChanged(BubbleViewProvider bubbleViewProvider, boolean z) {
        BubbleController.BubbleExpandListener bubbleExpandListener = this.mExpandListener;
        if (bubbleExpandListener == null || bubbleViewProvider == null) {
            return;
        }
        bubbleExpandListener.onBubbleExpandChanged(z, bubbleViewProvider.getKey());
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mStackAnimationController.setImeHeight(z ? i + this.mImeOffset : 0);
        if (this.mIsExpanded || getBubbleCount() <= 0) {
            return;
        }
        float fAnimateForImeVisibility = this.mStackAnimationController.animateForImeVisibility(z) - this.mStackAnimationController.getStackPosition().y;
        if (this.mFlyout.getVisibility() == 0) {
            PhysicsAnimator.getInstance(this.mFlyout).spring(DynamicAnimation.TRANSLATION_Y, this.mFlyout.getTranslationY() + fAnimateForImeVisibility, FLYOUT_IME_ANIMATION_SPRING_CONFIG).start();
        }
    }

    public void subtractObscuredTouchableRegion(Region region, View view) {
        BubbleManageEducationView bubbleManageEducationView;
        if (!this.mIsExpanded || this.mShowingManage || ((bubbleManageEducationView = this.mManageEducationView) != null && bubbleManageEducationView.getVisibility() == 0)) {
            region.setEmpty();
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (motionEvent.getAction() != 0 && motionEvent.getActionIndex() != this.mPointerIndexDown) {
            return false;
        }
        if (motionEvent.getAction() == 0) {
            this.mPointerIndexDown = motionEvent.getActionIndex();
        } else if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
            this.mPointerIndexDown = -1;
        }
        boolean zDispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
        if (!zDispatchTouchEvent && !this.mIsExpanded && this.mIsGestureInProgress) {
            zDispatchTouchEvent = this.mBubbleTouchListener.onTouch(this, motionEvent);
        }
        if (motionEvent.getAction() != 1 && motionEvent.getAction() != 3) {
            z = true;
        }
        this.mIsGestureInProgress = z;
        return zDispatchTouchEvent;
    }

    void setFlyoutStateForDragLength(float f) {
        if (this.mFlyout.getWidth() <= 0) {
            return;
        }
        boolean zIsStackOnLeftSide = this.mStackAnimationController.isStackOnLeftSide();
        this.mFlyoutDragDeltaX = f;
        if (zIsStackOnLeftSide) {
            f = -f;
        }
        float width = f / this.mFlyout.getWidth();
        float width2 = 0.0f;
        this.mFlyout.setCollapsePercent(Math.min(1.0f, Math.max(0.0f, width)));
        if (width < 0.0f || width > 1.0f) {
            boolean z = false;
            boolean z2 = width > 1.0f;
            if ((zIsStackOnLeftSide && width > 1.0f) || (!zIsStackOnLeftSide && width < 0.0f)) {
                z = true;
            }
            width2 = (z2 ? width - 1.0f : width * (-1.0f)) * (z ? -1 : 1) * (this.mFlyout.getWidth() / (8.0f / (z2 ? 2 : 1)));
        }
        BubbleFlyoutView bubbleFlyoutView = this.mFlyout;
        bubbleFlyoutView.setTranslationX(bubbleFlyoutView.getRestingTranslationX() + width2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean passEventToMagnetizedObject(MotionEvent motionEvent) {
        MagnetizedObject<?> magnetizedObject = this.mMagnetizedObject;
        return magnetizedObject != null && magnetizedObject.maybeConsumeMotionEvent(motionEvent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissMagnetizedObject() {
        if (this.mIsExpanded) {
            dismissBubbleIfExists(this.mBubbleData.getBubbleWithView((View) this.mMagnetizedObject.getUnderlyingObject()));
        } else {
            this.mBubbleData.dismissAll(1);
        }
    }

    private void dismissBubbleIfExists(Bubble bubble) {
        if (bubble == null || !this.mBubbleData.hasBubbleInStackWithKey(bubble.getKey())) {
            return;
        }
        this.mBubbleData.dismissBubbleWithKey(bubble.getKey(), 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateDesaturateAndDarken(View view, boolean z) {
        this.mDesaturateAndDarkenTargetView = view;
        if (view == null) {
            return;
        }
        if (z) {
            view.setLayerType(2, this.mDesaturateAndDarkenPaint);
            this.mDesaturateAndDarkenAnimator.removeAllListeners();
            this.mDesaturateAndDarkenAnimator.start();
        } else {
            this.mDesaturateAndDarkenAnimator.removeAllListeners();
            this.mDesaturateAndDarkenAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.bubbles.BubbleStackView.11
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    BubbleStackView.this.resetDesaturationAndDarken();
                }
            });
            this.mDesaturateAndDarkenAnimator.reverse();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetDesaturationAndDarken() {
        this.mDesaturateAndDarkenAnimator.removeAllListeners();
        this.mDesaturateAndDarkenAnimator.cancel();
        View view = this.mDesaturateAndDarkenTargetView;
        if (view != null) {
            view.setLayerType(0, null);
            this.mDesaturateAndDarkenTargetView = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void springInDismissTargetMaybe() {
        if (this.mShowingDismiss) {
            return;
        }
        this.mShowingDismiss = true;
        this.mDismissTargetContainer.bringToFront();
        this.mDismissTargetContainer.setZ(32766.0f);
        this.mDismissTargetContainer.setVisibility(0);
        ((TransitionDrawable) this.mDismissTargetContainer.getBackground()).startTransition(200);
        this.mDismissTargetAnimator.cancel();
        this.mDismissTargetAnimator.spring(DynamicAnimation.TRANSLATION_Y, 0.0f, this.mDismissTargetSpring).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideDismissTarget() {
        if (this.mShowingDismiss) {
            this.mShowingDismiss = false;
            ((TransitionDrawable) this.mDismissTargetContainer.getBackground()).reverseTransition(200);
            this.mDismissTargetAnimator.spring(DynamicAnimation.TRANSLATION_Y, this.mDismissTargetContainer.getHeight(), this.mDismissTargetSpring).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda31
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$hideDismissTarget$37();
                }
            }).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hideDismissTarget$37() {
        this.mDismissTargetContainer.setVisibility(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateFlyoutCollapsed(boolean z, float f) {
        float f2;
        boolean zIsStackOnLeftSide = this.mStackAnimationController.isStackOnLeftSide();
        this.mFlyoutTransitionSpring.getSpring().setStiffness(this.mBubbleToExpandAfterFlyoutCollapse != null ? 1500.0f : 200.0f);
        SpringAnimation startVelocity = this.mFlyoutTransitionSpring.setStartValue(this.mFlyoutDragDeltaX).setStartVelocity(f);
        if (z) {
            int width = this.mFlyout.getWidth();
            if (zIsStackOnLeftSide) {
                width = -width;
            }
            f2 = width;
        } else {
            f2 = 0.0f;
        }
        startVelocity.animateToFinalPosition(f2);
    }

    float getExpandedViewY() {
        return getStatusBarHeight() + this.mBubbleSize + this.mBubblePaddingTop;
    }

    @VisibleForTesting
    void animateInFlyoutForBubble(final Bubble bubble) {
        View view;
        final Bubble.FlyoutMessage flyoutMessage = bubble.getFlyoutMessage();
        final BadgedImageView iconView = bubble.getIconView();
        if (flyoutMessage == null || flyoutMessage.message == null || !bubble.showFlyout() || (((view = this.mUserEducationView) != null && view.getVisibility() == 0) || isExpanded() || this.mIsExpansionAnimating || this.mIsGestureInProgress || this.mBubbleToExpandAfterFlyoutCollapse != null || iconView == null)) {
            if (iconView != null) {
                iconView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
                return;
            }
            return;
        }
        this.mFlyoutDragDeltaX = 0.0f;
        clearFlyoutOnHide();
        this.mAfterFlyoutHidden = new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda42
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateInFlyoutForBubble$38(iconView);
            }
        };
        this.mFlyout.setVisibility(4);
        iconView.addDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
        post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda43
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateInFlyoutForBubble$41(bubble, flyoutMessage);
            }
        });
        this.mFlyout.removeCallbacks(this.mHideFlyout);
        this.mFlyout.postDelayed(this.mHideFlyout, 5000L);
        logBubbleEvent(bubble, 16);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$38(BadgedImageView badgedImageView) {
        this.mAfterFlyoutHidden = null;
        Bubble bubble = this.mBubbleToExpandAfterFlyoutCollapse;
        if (bubble != null) {
            this.mBubbleData.setSelectedBubble(bubble);
            this.mBubbleData.setExpanded(true);
            this.mBubbleToExpandAfterFlyoutCollapse = null;
        }
        badgedImageView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
        this.mFlyout.setVisibility(4);
        updateTemporarilyInvisibleAnimation(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$41(Bubble bubble, Bubble.FlyoutMessage flyoutMessage) {
        if (isExpanded()) {
            return;
        }
        Runnable runnable = new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda24
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateInFlyoutForBubble$40();
            }
        };
        if (bubble.getIconView() == null) {
            return;
        }
        this.mFlyout.setupFlyoutStartingAsDot(flyoutMessage, this.mStackAnimationController.getStackPosition(), getWidth(), this.mStackAnimationController.isStackOnLeftSide(), bubble.getIconView().getDotColor(), runnable, this.mAfterFlyoutHidden, bubble.getIconView().getDotCenter(), !bubble.showDot());
        this.mFlyout.bringToFront();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$40() {
        Runnable runnable = new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda30
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateInFlyoutForBubble$39();
            }
        };
        this.mAnimateInFlyout = runnable;
        this.mFlyout.postDelayed(runnable, 200L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$39() {
        int width;
        this.mFlyout.setVisibility(0);
        updateTemporarilyInvisibleAnimation(false);
        if (this.mStackAnimationController.isStackOnLeftSide()) {
            width = -this.mFlyout.getWidth();
        } else {
            width = this.mFlyout.getWidth();
        }
        this.mFlyoutDragDeltaX = width;
        animateFlyoutCollapsed(false, 0.0f);
        this.mFlyout.postDelayed(this.mHideFlyout, 5000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideFlyoutImmediate() {
        clearFlyoutOnHide();
        this.mFlyout.removeCallbacks(this.mAnimateInFlyout);
        this.mFlyout.removeCallbacks(this.mHideFlyout);
        this.mFlyout.hideFlyout();
    }

    private void clearFlyoutOnHide() {
        this.mFlyout.removeCallbacks(this.mAnimateInFlyout);
        Runnable runnable = this.mAfterFlyoutHidden;
        if (runnable == null) {
            return;
        }
        runnable.run();
        this.mAfterFlyoutHidden = null;
    }

    public void getTouchableRegion(Rect rect) {
        View view = this.mUserEducationView;
        if (view != null && view.getVisibility() == 0) {
            rect.set(0, 0, getWidth(), getHeight());
            return;
        }
        if (!this.mIsExpanded) {
            if (getBubbleCount() > 0) {
                this.mBubbleContainer.getChildAt(0).getBoundsOnScreen(rect);
                int i = rect.top;
                int i2 = this.mBubbleTouchPadding;
                rect.top = i - i2;
                rect.left -= i2;
                rect.right += i2;
                rect.bottom += i2;
            }
        } else {
            this.mBubbleContainer.getBoundsOnScreen(rect);
        }
        if (this.mFlyout.getVisibility() == 0) {
            Rect rect2 = new Rect();
            this.mFlyout.getBoundsOnScreen(rect2);
            rect.union(rect2);
        }
    }

    private int getStatusBarHeight() {
        if (getRootWindowInsets() == null) {
            return 0;
        }
        WindowInsets rootWindowInsets = getRootWindowInsets();
        return Math.max(this.mStatusBarHeight, rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetTop() : 0);
    }

    private void requestUpdate() {
        if (this.mViewUpdatedRequested || this.mIsExpansionAnimating) {
            return;
        }
        this.mViewUpdatedRequested = true;
        getViewTreeObserver().addOnPreDrawListener(this.mViewUpdater);
        invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showManageMenu(boolean z) {
        this.mShowingManage = z;
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null) {
            this.mManageMenu.setVisibility(4);
            return;
        }
        if (z && this.mBubbleData.hasBubbleInStackWithKey(this.mExpandedBubble.getKey())) {
            Bubble bubbleInStackWithKey = this.mBubbleData.getBubbleInStackWithKey(this.mExpandedBubble.getKey());
            this.mManageSettingsIcon.setImageDrawable(bubbleInStackWithKey.getBadgedAppIcon());
            this.mManageSettingsText.setText(getResources().getString(R.string.bubbles_app_settings, bubbleInStackWithKey.getAppName()));
        }
        this.mExpandedBubble.getExpandedView().getManageButtonBoundsOnScreen(this.mTempRect);
        boolean z2 = getResources().getConfiguration().getLayoutDirection() == 0;
        Rect rect = this.mTempRect;
        float width = z2 ? rect.left : rect.right - this.mManageMenu.getWidth();
        float height = this.mTempRect.bottom - this.mManageMenu.getHeight();
        float width2 = ((z2 ? 1 : -1) * this.mManageMenu.getWidth()) / 4.0f;
        if (z) {
            this.mManageMenu.setScaleX(0.5f);
            this.mManageMenu.setScaleY(0.5f);
            this.mManageMenu.setTranslationX(width - width2);
            this.mManageMenu.setTranslationY((r10.getHeight() / 4.0f) + height);
            this.mManageMenu.setAlpha(0.0f);
            PhysicsAnimator.getInstance(this.mManageMenu).spring(DynamicAnimation.ALPHA, 1.0f).spring(DynamicAnimation.SCALE_X, 1.0f).spring(DynamicAnimation.SCALE_Y, 1.0f).spring(DynamicAnimation.TRANSLATION_X, width).spring(DynamicAnimation.TRANSLATION_Y, height).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda29
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showManageMenu$42();
                }
            }).start();
            this.mManageMenu.setVisibility(0);
        } else {
            PhysicsAnimator.getInstance(this.mManageMenu).spring(DynamicAnimation.ALPHA, 0.0f).spring(DynamicAnimation.SCALE_X, 0.5f).spring(DynamicAnimation.SCALE_Y, 0.5f).spring(DynamicAnimation.TRANSLATION_X, width - width2).spring(DynamicAnimation.TRANSLATION_Y, height + (this.mManageMenu.getHeight() / 4.0f)).withEndActions(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda33
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showManageMenu$43();
                }
            }).start();
        }
        this.mExpandedBubble.getExpandedView().updateObscuredTouchableRegion();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showManageMenu$42() {
        this.mManageMenu.getChildAt(0).requestAccessibilityFocus();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showManageMenu$43() {
        this.mManageMenu.setVisibility(4);
    }

    private void updateExpandedBubble() {
        BubbleViewProvider bubbleViewProvider;
        this.mExpandedViewContainer.removeAllViews();
        if (!this.mIsExpanded || (bubbleViewProvider = this.mExpandedBubble) == null || bubbleViewProvider.getExpandedView() == null) {
            return;
        }
        BubbleExpandedView expandedView = this.mExpandedBubble.getExpandedView();
        expandedView.setContentVisibility(false);
        this.mExpandedViewContainerMatrix.setScaleX(0.0f);
        this.mExpandedViewContainerMatrix.setScaleY(0.0f);
        this.mExpandedViewContainerMatrix.setTranslate(0.0f, 0.0f);
        this.mExpandedViewContainer.setVisibility(4);
        this.mExpandedViewContainer.setAlpha(0.0f);
        this.mExpandedViewContainer.addView(expandedView);
        expandedView.setManageClickListener(new View.OnClickListener() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$updateExpandedBubble$44(view);
            }
        });
        expandedView.populateExpandedView();
        if (this.mIsExpansionAnimating) {
            return;
        }
        this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda35
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateExpandedBubble$45();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateExpandedBubble$44(View view) {
        showManageMenu(!this.mShowingManage);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateExpandedBubble$45() {
        post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda21
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.animateSwitchBubbles();
            }
        });
    }

    private void screenshotAnimatingOutBubbleIntoSurface(final Consumer<Boolean> consumer) {
        BubbleViewProvider bubbleViewProvider;
        if (!this.mIsExpanded || (bubbleViewProvider = this.mExpandedBubble) == null || bubbleViewProvider.getExpandedView() == null) {
            consumer.accept(Boolean.FALSE);
            return;
        }
        BubbleExpandedView expandedView = this.mExpandedBubble.getExpandedView();
        if (this.mAnimatingOutBubbleBuffer != null) {
            releaseAnimatingOutBubbleBuffer();
        }
        try {
            this.mAnimatingOutBubbleBuffer = expandedView.snapshotActivitySurface();
        } catch (Exception e) {
            Log.wtf("Bubbles", e);
            consumer.accept(Boolean.FALSE);
        }
        SurfaceControl.ScreenshotGraphicBuffer screenshotGraphicBuffer = this.mAnimatingOutBubbleBuffer;
        if (screenshotGraphicBuffer == null || screenshotGraphicBuffer.getGraphicBuffer() == null) {
            consumer.accept(Boolean.FALSE);
            return;
        }
        PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).cancel();
        this.mAnimatingOutSurfaceContainer.setScaleX(1.0f);
        this.mAnimatingOutSurfaceContainer.setScaleY(1.0f);
        this.mAnimatingOutSurfaceContainer.setTranslationX(0.0f);
        this.mAnimatingOutSurfaceContainer.setTranslationY(0.0f);
        this.mAnimatingOutSurfaceContainer.setTranslationY(this.mExpandedBubble.getExpandedView().getActivityViewLocationOnScreen()[1] - this.mAnimatingOutSurfaceView.getLocationOnScreen()[1]);
        this.mAnimatingOutSurfaceView.getLayoutParams().width = this.mAnimatingOutBubbleBuffer.getGraphicBuffer().getWidth();
        this.mAnimatingOutSurfaceView.getLayoutParams().height = this.mAnimatingOutBubbleBuffer.getGraphicBuffer().getHeight();
        this.mAnimatingOutSurfaceView.requestLayout();
        post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda46
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$screenshotAnimatingOutBubbleIntoSurface$48(consumer);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$screenshotAnimatingOutBubbleIntoSurface$48(final Consumer consumer) {
        if (this.mAnimatingOutBubbleBuffer.getGraphicBuffer().isDestroyed()) {
            consumer.accept(Boolean.FALSE);
        } else if (!this.mIsExpanded) {
            consumer.accept(Boolean.FALSE);
        } else {
            this.mAnimatingOutSurfaceView.getHolder().getSurface().attachAndQueueBufferWithColorSpace(this.mAnimatingOutBubbleBuffer.getGraphicBuffer(), this.mAnimatingOutBubbleBuffer.getColorSpace());
            this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda47
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$screenshotAnimatingOutBubbleIntoSurface$47(consumer);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$screenshotAnimatingOutBubbleIntoSurface$46(Consumer consumer) {
        consumer.accept(Boolean.TRUE);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$screenshotAnimatingOutBubbleIntoSurface$47(final Consumer consumer) {
        post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleStackView$$ExternalSyntheticLambda48
            @Override // java.lang.Runnable
            public final void run() {
                BubbleStackView.lambda$screenshotAnimatingOutBubbleIntoSurface$46(consumer);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void releaseAnimatingOutBubbleBuffer() {
        SurfaceControl.ScreenshotGraphicBuffer screenshotGraphicBuffer = this.mAnimatingOutBubbleBuffer;
        if (screenshotGraphicBuffer == null || screenshotGraphicBuffer.getGraphicBuffer().isDestroyed()) {
            return;
        }
        this.mAnimatingOutBubbleBuffer.getGraphicBuffer().destroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateExpandedView() {
        this.mExpandedViewContainer.setVisibility(this.mIsExpanded ? 0 : 8);
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null) {
            this.mExpandedViewContainer.setTranslationY(getExpandedViewY());
            this.mExpandedBubble.getExpandedView().updateView(this.mExpandedViewContainer.getLocationOnScreen());
        }
        this.mStackOnLeftOrWillBe = this.mStackAnimationController.isStackOnLeftSide();
        updateBubbleZOrdersAndDotPosition(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBubbleZOrdersAndDotPosition(boolean z) {
        int bubbleCount = getBubbleCount();
        for (int i = 0; i < bubbleCount; i++) {
            BadgedImageView badgedImageView = (BadgedImageView) this.mBubbleContainer.getChildAt(i);
            badgedImageView.setZ((this.mMaxBubbles * this.mBubbleElevation) - i);
            boolean dotPositionOnLeft = badgedImageView.getDotPositionOnLeft();
            boolean z2 = this.mStackOnLeftOrWillBe;
            if (dotPositionOnLeft == z2) {
                badgedImageView.setDotPositionOnLeft(!z2, z);
            }
            if (!this.mIsExpanded && i > 0) {
                badgedImageView.addDotSuppressionFlag(BadgedImageView.SuppressionFlag.BEHIND_STACK);
            } else {
                badgedImageView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.BEHIND_STACK);
            }
        }
    }

    private void updatePointerPosition() {
        int bubbleIndex;
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null || (bubbleIndex = getBubbleIndex(this.mExpandedBubble)) == -1) {
            return;
        }
        this.mExpandedBubble.getExpandedView().setPointerPosition((this.mExpandedAnimationController.getBubbleLeft(bubbleIndex) + (this.mBubbleSize / 2.0f)) - this.mExpandedViewContainer.getPaddingLeft());
    }

    public int getBubbleCount() {
        return this.mBubbleContainer.getChildCount() - 1;
    }

    int getBubbleIndex(BubbleViewProvider bubbleViewProvider) {
        if (bubbleViewProvider == null) {
            return 0;
        }
        return this.mBubbleContainer.indexOfChild(bubbleViewProvider.getIconView());
    }

    public float getNormalizedXPosition() {
        BigDecimal bigDecimal = new BigDecimal(getStackPosition().x / this.mDisplaySize.x);
        RoundingMode roundingMode = RoundingMode.CEILING;
        return bigDecimal.setScale(4, RoundingMode.HALF_UP).floatValue();
    }

    public float getNormalizedYPosition() {
        BigDecimal bigDecimal = new BigDecimal(getStackPosition().y / this.mDisplaySize.y);
        RoundingMode roundingMode = RoundingMode.CEILING;
        return bigDecimal.setScale(4, RoundingMode.HALF_UP).floatValue();
    }

    public void setStackStartPosition(RelativeStackPosition relativeStackPosition) {
        this.mStackAnimationController.setStackStartPosition(relativeStackPosition);
    }

    public PointF getStackPosition() {
        return this.mStackAnimationController.getStackPosition();
    }

    public RelativeStackPosition getRelativeStackPosition() {
        return this.mStackAnimationController.getRelativeStackPosition();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logBubbleEvent(BubbleViewProvider bubbleViewProvider, int i) {
        if (bubbleViewProvider == null || bubbleViewProvider.getKey().equals("Overflow")) {
            SysUiStatsLog.write(149, ((FrameLayout) this).mContext.getApplicationInfo().packageName, bubbleViewProvider == null ? null : "Overflow", 0, 0, getBubbleCount(), i, getNormalizedXPosition(), getNormalizedYPosition(), false, false, false);
        } else {
            bubbleViewProvider.logUIEvent(getBubbleCount(), i, getNormalizedXPosition(), getNormalizedYPosition(), getBubbleIndex(bubbleViewProvider));
        }
    }

    boolean performBackPressIfNeeded() {
        BubbleViewProvider bubbleViewProvider;
        if (!isExpanded() || (bubbleViewProvider = this.mExpandedBubble) == null || bubbleViewProvider.getExpandedView() == null) {
            return false;
        }
        return this.mExpandedBubble.getExpandedView().performBackPressIfNeeded();
    }

    private boolean shouldShowBubblesEducation() {
        return BubbleDebugConfig.forceShowUserEducation(getContext()) || !Prefs.getBoolean(getContext(), "HasSeenBubblesOnboarding", false);
    }

    private boolean shouldShowManageEducation() {
        return BubbleDebugConfig.forceShowUserEducation(getContext()) || !Prefs.getBoolean(getContext(), "HasSeenBubblesManageOnboarding", false);
    }

    public static class RelativeStackPosition {
        private boolean mOnLeft;
        private float mVerticalOffsetPercent;

        public RelativeStackPosition(boolean z, float f) {
            this.mOnLeft = z;
            this.mVerticalOffsetPercent = clampVerticalOffsetPercent(f);
        }

        public RelativeStackPosition(PointF pointF, RectF rectF) {
            this.mOnLeft = pointF.x < rectF.width() / 2.0f;
            this.mVerticalOffsetPercent = clampVerticalOffsetPercent((pointF.y - rectF.top) / rectF.height());
        }

        private float clampVerticalOffsetPercent(float f) {
            return Math.max(0.0f, Math.min(1.0f, f));
        }

        public PointF getAbsolutePositionInRegion(RectF rectF) {
            return new PointF(this.mOnLeft ? rectF.left : rectF.right, rectF.top + (this.mVerticalOffsetPercent * rectF.height()));
        }
    }
}
