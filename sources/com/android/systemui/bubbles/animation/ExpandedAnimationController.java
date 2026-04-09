package com.android.systemui.bubbles.animation;

import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import com.google.android.collect.Sets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Set;

/* loaded from: classes.dex */
public class ExpandedAnimationController extends PhysicsAnimationLayout.PhysicsAnimationController {
    private Runnable mAfterCollapse;
    private Runnable mAfterExpand;
    private float mBubblePaddingTop;
    private float mBubbleSizePx;
    private int mBubblesMaxRendered;
    private PointF mCollapsePoint;
    private Point mDisplaySize;
    private int mExpandedViewPadding;
    private Runnable mLeadBubbleEndAction;
    private MagnetizedObject<View> mMagnetizedBubbleDraggingOut;
    private Runnable mOnBubbleAnimatedOutAction;
    private int mScreenOrientation;
    private float mSpaceBetweenBubbles;
    private float mStackOffsetPx;
    private float mStatusBarHeight;
    private final PhysicsAnimator.SpringConfig mAnimateOutSpringConfig = new PhysicsAnimator.SpringConfig(1000.0f, 1.0f);
    private boolean mAnimatingExpand = false;
    private boolean mPreparingToCollapse = false;
    private boolean mAnimatingCollapse = false;
    private boolean mSpringingBubbleToTouch = false;
    private boolean mSpringToTouchOnNextMotionEvent = false;
    private boolean mBubbleDraggedOutEnough = false;

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    int getNextAnimationInChain(DynamicAnimation.ViewProperty viewProperty, int i) {
        return -1;
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    float getOffsetForChainedPropertyAnimation(DynamicAnimation.ViewProperty viewProperty) {
        return 0.0f;
    }

    public ExpandedAnimationController(Point point, int i, int i2, Runnable runnable) {
        updateResources(i2, point);
        this.mExpandedViewPadding = i;
        this.mOnBubbleAnimatedOutAction = runnable;
    }

    public void expandFromStack(Runnable runnable, Runnable runnable2) {
        this.mPreparingToCollapse = false;
        this.mAnimatingCollapse = false;
        this.mAnimatingExpand = true;
        this.mAfterExpand = runnable;
        this.mLeadBubbleEndAction = runnable2;
        startOrUpdatePathAnimation(true);
    }

    public void expandFromStack(Runnable runnable) {
        expandFromStack(runnable, null);
    }

    public void notifyPreparingToCollapse() {
        this.mPreparingToCollapse = true;
    }

    public void collapseBackToStack(PointF pointF, Runnable runnable) {
        this.mAnimatingExpand = false;
        this.mPreparingToCollapse = false;
        this.mAnimatingCollapse = true;
        this.mAfterCollapse = runnable;
        this.mCollapsePoint = pointF;
        startOrUpdatePathAnimation(false);
    }

    public void updateResources(int i, Point point) {
        this.mScreenOrientation = i;
        this.mDisplaySize = point;
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (physicsAnimationLayout == null) {
            return;
        }
        Resources resources = physicsAnimationLayout.getContext().getResources();
        int i2 = R.dimen.bubble_padding_top;
        this.mBubblePaddingTop = resources.getDimensionPixelSize(i2);
        this.mStatusBarHeight = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        this.mStackOffsetPx = resources.getDimensionPixelSize(R.dimen.bubble_stack_offset);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(i2);
        this.mBubbleSizePx = resources.getDimensionPixelSize(R.dimen.individual_bubble_size);
        this.mBubblesMaxRendered = resources.getInteger(R.integer.bubbles_max_rendered);
        float widthForDisplayingBubbles = getWidthForDisplayingBubbles() - (this.mExpandedViewPadding * 2);
        this.mSpaceBetweenBubbles = (widthForDisplayingBubbles - ((r4 + 1) * this.mBubbleSizePx)) / this.mBubblesMaxRendered;
    }

    private void startOrUpdatePathAnimation(final boolean z) {
        Runnable runnable;
        if (z) {
            runnable = new Runnable() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$startOrUpdatePathAnimation$0();
                }
            };
        } else {
            runnable = new Runnable() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$startOrUpdatePathAnimation$1();
                }
            };
        }
        animationsForChildrenFromIndex(0, new PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda1
            @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator
            public final void configureAnimationForChildAtIndex(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
                this.f$0.lambda$startOrUpdatePathAnimation$3(z, i, physicsPropertyAnimator);
            }
        }).startAll(runnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startOrUpdatePathAnimation$0() {
        this.mAnimatingExpand = false;
        Runnable runnable = this.mAfterExpand;
        if (runnable != null) {
            runnable.run();
        }
        this.mAfterExpand = null;
        updateBubblePositions();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startOrUpdatePathAnimation$1() {
        this.mAnimatingCollapse = false;
        Runnable runnable = this.mAfterCollapse;
        if (runnable != null) {
            runnable.run();
        }
        this.mAfterCollapse = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startOrUpdatePathAnimation$3(boolean z, int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
        View childAt = this.mLayout.getChildAt(i);
        Path path = new Path();
        path.moveTo(childAt.getTranslationX(), childAt.getTranslationY());
        float expandedY = getExpandedY();
        if (z) {
            path.lineTo(childAt.getTranslationX(), expandedY);
            path.lineTo(getBubbleLeft(i), expandedY);
        } else {
            float f = this.mCollapsePoint.x + ((this.mLayout.isFirstChildXLeftOfCenter(this.mCollapsePoint.x) ? -1.0f : 1.0f) * i * this.mStackOffsetPx);
            path.lineTo(f, expandedY);
            path.lineTo(f, this.mCollapsePoint.y);
        }
        boolean z2 = (z && !this.mLayout.isFirstChildXLeftOfCenter(childAt.getTranslationX())) || (!z && this.mLayout.isFirstChildXLeftOfCenter(this.mCollapsePoint.x));
        int childCount = z2 ? i * 10 : (this.mLayout.getChildCount() - i) * 10;
        boolean z3 = (z2 && i == 0) || (!z2 && i == this.mLayout.getChildCount() - 1);
        Interpolator interpolator = Interpolators.LINEAR;
        Runnable[] runnableArr = new Runnable[2];
        runnableArr[0] = z3 ? this.mLeadBubbleEndAction : null;
        runnableArr[1] = new Runnable() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$startOrUpdatePathAnimation$2();
            }
        };
        physicsPropertyAnimator.followAnimatedTargetAlongPath(path, 175, interpolator, runnableArr).withStartDelay(childCount).withStiffness(1000.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startOrUpdatePathAnimation$2() {
        this.mLeadBubbleEndAction = null;
    }

    public void onUnstuckFromTarget() {
        this.mSpringToTouchOnNextMotionEvent = true;
    }

    public void prepareForBubbleDrag(final View view, MagnetizedObject.MagneticTarget magneticTarget, MagnetizedObject.MagnetListener magnetListener) {
        this.mLayout.cancelAnimationsOnView(view);
        view.setTranslationZ(32767.0f);
        MagnetizedObject<View> magnetizedObject = new MagnetizedObject<View>(this.mLayout.getContext(), view, DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y) { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController.1
            @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
            public float getWidth(View view2) {
                return ExpandedAnimationController.this.mBubbleSizePx;
            }

            @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
            public float getHeight(View view2) {
                return ExpandedAnimationController.this.mBubbleSizePx;
            }

            @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
            public void getLocationOnScreen(View view2, int[] iArr) {
                iArr[0] = (int) view.getTranslationX();
                iArr[1] = (int) view.getTranslationY();
            }
        };
        this.mMagnetizedBubbleDraggingOut = magnetizedObject;
        magnetizedObject.addTarget(magneticTarget);
        this.mMagnetizedBubbleDraggingOut.setMagnetListener(magnetListener);
        this.mMagnetizedBubbleDraggingOut.setHapticsEnabled(true);
        this.mMagnetizedBubbleDraggingOut.setFlingToTargetMinVelocity(6000.0f);
    }

    private void springBubbleTo(View view, float f, float f2) {
        animationForChild(view).translationX(f, new Runnable[0]).translationY(f2, new Runnable[0]).withStiffness(10000.0f).start(new Runnable[0]);
    }

    public void dragBubbleOut(View view, float f, float f2) {
        boolean z = true;
        if (this.mSpringToTouchOnNextMotionEvent) {
            springBubbleTo(this.mMagnetizedBubbleDraggingOut.getUnderlyingObject(), f, f2);
            this.mSpringToTouchOnNextMotionEvent = false;
            this.mSpringingBubbleToTouch = true;
        } else if (this.mSpringingBubbleToTouch) {
            if (this.mLayout.arePropertiesAnimatingOnView(view, DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y)) {
                springBubbleTo(this.mMagnetizedBubbleDraggingOut.getUnderlyingObject(), f, f2);
            } else {
                this.mSpringingBubbleToTouch = false;
            }
        }
        if (!this.mSpringingBubbleToTouch && !this.mMagnetizedBubbleDraggingOut.getObjectStuckToTarget()) {
            view.setTranslationX(f);
            view.setTranslationY(f2);
        }
        if (f2 <= getExpandedY() + this.mBubbleSizePx && f2 >= getExpandedY() - this.mBubbleSizePx) {
            z = false;
        }
        if (z != this.mBubbleDraggedOutEnough) {
            updateBubblePositions();
            this.mBubbleDraggedOutEnough = z;
        }
    }

    public void dismissDraggedOutBubble(View view, float f, Runnable runnable) {
        if (view == null) {
            return;
        }
        animationForChild(view).withStiffness(10000.0f).scaleX(0.0f, new Runnable[0]).scaleY(0.0f, new Runnable[0]).translationY(view.getTranslationY() + f, new Runnable[0]).alpha(0.0f, runnable).start(new Runnable[0]);
        updateBubblePositions();
    }

    public View getDraggedOutBubble() {
        MagnetizedObject<View> magnetizedObject = this.mMagnetizedBubbleDraggingOut;
        if (magnetizedObject == null) {
            return null;
        }
        return magnetizedObject.getUnderlyingObject();
    }

    public MagnetizedObject<View> getMagnetizedBubbleDraggingOut() {
        return this.mMagnetizedBubbleDraggingOut;
    }

    public void snapBubbleBack(final View view, float f, float f2) {
        int iIndexOfChild = this.mLayout.indexOfChild(view);
        animationForChildAtIndex(iIndexOfChild).position(getBubbleLeft(iIndexOfChild), getExpandedY(), new Runnable[0]).withPositionStartVelocities(f, f2).start(new Runnable() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                view.setTranslationZ(0.0f);
            }
        });
        this.mMagnetizedBubbleDraggingOut = null;
        updateBubblePositions();
    }

    public void onGestureFinished() {
        this.mBubbleDraggedOutEnough = false;
        this.mMagnetizedBubbleDraggingOut = null;
        updateBubblePositions();
    }

    public void updateYPosition(Runnable runnable) {
        if (this.mLayout == null) {
            return;
        }
        animationsForChildrenFromIndex(0, new PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda0
            @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator
            public final void configureAnimationForChildAtIndex(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
                this.f$0.lambda$updateYPosition$5(i, physicsPropertyAnimator);
            }
        }).startAll(runnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateYPosition$5(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
        physicsPropertyAnimator.translationY(getExpandedY(), new Runnable[0]);
    }

    public float getExpandedY() {
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (physicsAnimationLayout == null || physicsAnimationLayout.getRootWindowInsets() == null) {
            return 0.0f;
        }
        return this.mBubblePaddingTop + Math.max(this.mStatusBarHeight, this.mLayout.getRootWindowInsets().getDisplayCutout() != null ? r0.getDisplayCutout().getSafeInsetTop() : 0.0f);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("ExpandedAnimationController state:");
        printWriter.print("  isActive:          ");
        printWriter.println(isActiveController());
        printWriter.print("  animatingExpand:   ");
        printWriter.println(this.mAnimatingExpand);
        printWriter.print("  animatingCollapse: ");
        printWriter.println(this.mAnimatingCollapse);
        printWriter.print("  springingBubble:   ");
        printWriter.println(this.mSpringingBubbleToTouch);
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onActiveControllerForLayout(PhysicsAnimationLayout physicsAnimationLayout) {
        updateResources(this.mScreenOrientation, this.mDisplaySize);
        this.mLayout.setVisibility(0);
        animationsForChildrenFromIndex(0, new PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator() { // from class: com.android.systemui.bubbles.animation.ExpandedAnimationController$$ExternalSyntheticLambda2
            @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator
            public final void configureAnimationForChildAtIndex(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
                ExpandedAnimationController.lambda$onActiveControllerForLayout$6(i, physicsPropertyAnimator);
            }
        }).startAll(new Runnable[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$onActiveControllerForLayout$6(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
        physicsPropertyAnimator.scaleX(1.0f, new Runnable[0]).scaleY(1.0f, new Runnable[0]).alpha(1.0f, new Runnable[0]);
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
        return Sets.newHashSet(new DynamicAnimation.ViewProperty[]{DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y, DynamicAnimation.SCALE_X, DynamicAnimation.SCALE_Y, DynamicAnimation.ALPHA});
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    SpringForce getSpringForce(DynamicAnimation.ViewProperty viewProperty, View view) {
        return new SpringForce().setDampingRatio(0.75f).setStiffness(200.0f);
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onChildAdded(View view, int i) {
        if (this.mAnimatingExpand) {
            startOrUpdatePathAnimation(true);
            return;
        }
        if (this.mAnimatingCollapse) {
            startOrUpdatePathAnimation(false);
            return;
        }
        view.setTranslationX(getBubbleLeft(i));
        if (this.mPreparingToCollapse) {
            return;
        }
        animationForChild(view).translationY(getExpandedY() - (this.mBubbleSizePx * 4.0f), getExpandedY(), new Runnable[0]).start(new Runnable[0]);
        updateBubblePositions();
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onChildRemoved(View view, int i, Runnable runnable) {
        if (view.equals(getDraggedOutBubble())) {
            this.mMagnetizedBubbleDraggingOut = null;
            runnable.run();
            this.mOnBubbleAnimatedOutAction.run();
        } else {
            PhysicsAnimator.getInstance(view).spring(DynamicAnimation.ALPHA, 0.0f).spring(DynamicAnimation.SCALE_X, 0.0f, this.mAnimateOutSpringConfig).spring(DynamicAnimation.SCALE_Y, 0.0f, this.mAnimateOutSpringConfig).withEndActions(runnable, this.mOnBubbleAnimatedOutAction).start();
        }
        updateBubblePositions();
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onChildReordered(View view, int i, int i2) {
        if (this.mPreparingToCollapse) {
            return;
        }
        if (this.mAnimatingCollapse) {
            startOrUpdatePathAnimation(false);
        } else {
            updateBubblePositions();
        }
    }

    private void updateBubblePositions() {
        if (this.mAnimatingExpand || this.mAnimatingCollapse) {
            return;
        }
        for (int i = 0; i < this.mLayout.getChildCount(); i++) {
            View childAt = this.mLayout.getChildAt(i);
            if (childAt.equals(getDraggedOutBubble())) {
                return;
            }
            animationForChild(childAt).translationX(getBubbleLeft(i), new Runnable[0]).start(new Runnable[0]);
        }
    }

    public float getBubbleLeft(int i) {
        return getRowLeft() + (i * (this.mBubbleSizePx + this.mSpaceBetweenBubbles));
    }

    public float getWidthForDisplayingBubbles() {
        float availableScreenWidth = getAvailableScreenWidth(true);
        return this.mScreenOrientation == 2 ? Math.max(this.mDisplaySize.y, availableScreenWidth * 0.66f) : availableScreenWidth;
    }

    private float getAvailableScreenWidth(boolean z) {
        int safeInsetRight;
        int safeInsetLeft;
        float f = this.mDisplaySize.x;
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        WindowInsets rootWindowInsets = physicsAnimationLayout != null ? physicsAnimationLayout.getRootWindowInsets() : null;
        if (rootWindowInsets == null) {
            return f;
        }
        DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
        if (displayCutout != null) {
            safeInsetLeft = displayCutout.getSafeInsetLeft();
            safeInsetRight = displayCutout.getSafeInsetRight();
        } else {
            safeInsetRight = 0;
            safeInsetLeft = 0;
        }
        return (f - Math.max(z ? rootWindowInsets.getStableInsetLeft() : 0, safeInsetLeft)) - Math.max(z ? rootWindowInsets.getStableInsetRight() : 0, safeInsetRight);
    }

    private float getRowLeft() {
        if (this.mLayout == null) {
            return 0.0f;
        }
        return (getAvailableScreenWidth(false) / 2.0f) - (((r0.getChildCount() * this.mBubbleSizePx) + ((this.mLayout.getChildCount() - 1) * this.mSpaceBetweenBubbles)) / 2.0f);
    }
}
