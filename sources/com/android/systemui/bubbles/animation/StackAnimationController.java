package com.android.systemui.bubbles.animation;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.R;
import com.android.systemui.bubbles.BubbleStackView;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import com.google.android.collect.Sets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.function.IntSupplier;

/* loaded from: classes.dex */
public class StackAnimationController extends PhysicsAnimationLayout.PhysicsAnimationController {
    private int mBubbleBitmapSize;
    private IntSupplier mBubbleCountSupplier;
    private int mBubbleOffscreen;
    private int mBubblePaddingTop;
    private int mBubbleSize;
    private FloatingContentCoordinator mFloatingContentCoordinator;
    private MagnetizedObject<StackAnimationController> mMagnetizedStack;
    private Runnable mOnBubbleAnimatedOutAction;
    private PointF mRestingStackPosition;
    private float mStackOffset;
    private BubbleStackView.RelativeStackPosition mStackStartPosition;
    private int mStackStartingVerticalOffset;
    private float mStatusBarHeight;
    private final PhysicsAnimator.SpringConfig mAnimateOutSpringConfig = new PhysicsAnimator.SpringConfig(1000.0f, 1.0f);
    private PointF mStackPosition = new PointF(-1.0f, -1.0f);
    private Rect mAnimatingToBounds = new Rect();
    private boolean mStackMovedToStartPosition = false;
    private float mImeHeight = 0.0f;
    private float mPreImeY = -1.4E-45f;
    private HashMap<DynamicAnimation.ViewProperty, DynamicAnimation> mStackPositionAnimations = new HashMap<>();
    private boolean mIsMovingFromFlinging = false;
    private boolean mFirstBubbleSpringingToTouch = false;
    private boolean mSpringToTouchOnNextMotionEvent = false;
    private final FloatingContentCoordinator.FloatingContent mStackFloatingContent = new FloatingContentCoordinator.FloatingContent() { // from class: com.android.systemui.bubbles.animation.StackAnimationController.1
        private final Rect mFloatingBoundsOnScreen = new Rect();

        @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
        public void moveToBounds(Rect rect) {
            StackAnimationController.this.springStack(rect.left, rect.top, 200.0f);
        }

        @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
        public Rect getAllowedFloatingBoundsRegion() {
            Rect floatingBoundsOnScreen = getFloatingBoundsOnScreen();
            Rect rect = new Rect();
            StackAnimationController.this.getAllowableStackPositionRegion().roundOut(rect);
            rect.right += floatingBoundsOnScreen.width();
            rect.bottom += floatingBoundsOnScreen.height();
            return rect;
        }

        @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
        public Rect getFloatingBoundsOnScreen() {
            if (!StackAnimationController.this.mAnimatingToBounds.isEmpty()) {
                return StackAnimationController.this.mAnimatingToBounds;
            }
            if (StackAnimationController.this.mLayout.getChildCount() > 0) {
                this.mFloatingBoundsOnScreen.set((int) StackAnimationController.this.mStackPosition.x, (int) StackAnimationController.this.mStackPosition.y, ((int) StackAnimationController.this.mStackPosition.x) + StackAnimationController.this.mBubbleSize, ((int) StackAnimationController.this.mStackPosition.y) + StackAnimationController.this.mBubbleSize + StackAnimationController.this.mBubblePaddingTop);
            } else {
                this.mFloatingBoundsOnScreen.setEmpty();
            }
            return this.mFloatingBoundsOnScreen;
        }
    };

    public StackAnimationController(FloatingContentCoordinator floatingContentCoordinator, IntSupplier intSupplier, Runnable runnable) {
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        this.mBubbleCountSupplier = intSupplier;
        this.mOnBubbleAnimatedOutAction = runnable;
    }

    public void moveFirstBubbleWithStackFollowing(float f, float f2) {
        this.mAnimatingToBounds.setEmpty();
        this.mPreImeY = -1.4E-45f;
        moveFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_X, f);
        moveFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_Y, f2);
        this.mIsMovingFromFlinging = false;
    }

    public PointF getStackPosition() {
        return this.mStackPosition;
    }

    public boolean isStackOnLeftSide() {
        return this.mLayout == null || !isStackPositionSet() || this.mStackPosition.x + ((float) (this.mBubbleBitmapSize / 2)) < ((float) (this.mLayout.getWidth() / 2));
    }

    public void springStack(float f, float f2, float f3) {
        notifyFloatingCoordinatorStackAnimatingTo(f, f2);
        springFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_X, new SpringForce().setStiffness(f3).setDampingRatio(0.85f), 0.0f, f, new Runnable[0]);
        springFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_Y, new SpringForce().setStiffness(f3).setDampingRatio(0.85f), 0.0f, f2, new Runnable[0]);
    }

    public void springStackAfterFling(float f, float f2) {
        springStack(f, f2, 750.0f);
    }

    public float flingStackThenSpringToEdge(float f, float f2, float f3) {
        float fMax;
        boolean z = !(((f - ((float) (this.mBubbleBitmapSize / 2))) > ((float) (this.mLayout.getWidth() / 2)) ? 1 : ((f - ((float) (this.mBubbleBitmapSize / 2))) == ((float) (this.mLayout.getWidth() / 2)) ? 0 : -1)) < 0) ? f2 >= -750.0f : f2 >= 750.0f;
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        float f4 = z ? allowableStackPositionRegion.left : allowableStackPositionRegion.right;
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (physicsAnimationLayout != null && physicsAnimationLayout.getChildCount() != 0) {
            ContentResolver contentResolver = this.mLayout.getContext().getContentResolver();
            float f5 = Settings.Secure.getFloat(contentResolver, "bubble_stiffness", 750.0f);
            float f6 = Settings.Secure.getFloat(contentResolver, "bubble_damping", 0.85f);
            float f7 = Settings.Secure.getFloat(contentResolver, "bubble_friction", 2.2f);
            float f8 = (f4 - f) * 4.2f * f7;
            notifyFloatingCoordinatorStackAnimatingTo(f4, PhysicsAnimator.estimateFlingEndValue(this.mStackPosition.y, f3, new PhysicsAnimator.FlingConfig(f7, allowableStackPositionRegion.top, allowableStackPositionRegion.bottom)));
            if (z) {
                fMax = Math.min(f8, f2);
            } else {
                fMax = Math.max(f8, f2);
            }
            flingThenSpringFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_X, fMax, f7, new SpringForce().setStiffness(f5).setDampingRatio(f6), Float.valueOf(f4));
            flingThenSpringFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_Y, f3, f7, new SpringForce().setStiffness(f5).setDampingRatio(f6), null);
            this.mFirstBubbleSpringingToTouch = false;
            this.mIsMovingFromFlinging = true;
        }
        return f4;
    }

    public PointF getStackPositionAlongNearestHorizontalEdge() {
        PointF stackPosition = getStackPosition();
        boolean zIsFirstChildXLeftOfCenter = this.mLayout.isFirstChildXLeftOfCenter(stackPosition.x);
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        stackPosition.x = zIsFirstChildXLeftOfCenter ? allowableStackPositionRegion.left : allowableStackPositionRegion.right;
        return stackPosition;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StackAnimationController state:");
        printWriter.print("  isActive:             ");
        printWriter.println(isActiveController());
        printWriter.print("  restingStackPos:      ");
        PointF pointF = this.mRestingStackPosition;
        printWriter.println(pointF != null ? pointF.toString() : "null");
        printWriter.print("  currentStackPos:      ");
        printWriter.println(this.mStackPosition.toString());
        printWriter.print("  isMovingFromFlinging: ");
        printWriter.println(this.mIsMovingFromFlinging);
        printWriter.print("  withinDismiss:        ");
        printWriter.println(isStackStuckToTarget());
        printWriter.print("  firstBubbleSpringing: ");
        printWriter.println(this.mFirstBubbleSpringingToTouch);
    }

    protected void flingThenSpringFirstBubbleWithStackFollowing(final DynamicAnimation.ViewProperty viewProperty, float f, float f2, final SpringForce springForce, final Float f3) {
        float f4;
        float f5;
        if (isActiveController()) {
            Log.d("Bubbs.StackCtrl", String.format("Flinging %s.", PhysicsAnimationLayout.getReadablePropertyName(viewProperty)));
            StackPositionProperty stackPositionProperty = new StackPositionProperty(viewProperty);
            float value = stackPositionProperty.getValue(this);
            RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
            DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_X;
            if (viewProperty.equals(viewProperty2)) {
                f4 = allowableStackPositionRegion.left;
            } else {
                f4 = allowableStackPositionRegion.top;
            }
            final float f6 = f4;
            if (viewProperty.equals(viewProperty2)) {
                f5 = allowableStackPositionRegion.right;
            } else {
                f5 = allowableStackPositionRegion.bottom;
            }
            final float f7 = f5;
            FlingAnimation flingAnimation = new FlingAnimation(this, stackPositionProperty);
            flingAnimation.setFriction(f2).setStartVelocity(f).setMinValue(Math.min(value, f6)).setMaxValue(Math.max(value, f7)).addEndListener(new DynamicAnimation.OnAnimationEndListener() { // from class: com.android.systemui.bubbles.animation.StackAnimationController$$ExternalSyntheticLambda0
                @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f8, float f9) {
                    this.f$0.lambda$flingThenSpringFirstBubbleWithStackFollowing$0(viewProperty, springForce, f3, f6, f7, dynamicAnimation, z, f8, f9);
                }
            });
            cancelStackPositionAnimation(viewProperty);
            this.mStackPositionAnimations.put(viewProperty, flingAnimation);
            flingAnimation.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$flingThenSpringFirstBubbleWithStackFollowing$0(DynamicAnimation.ViewProperty viewProperty, SpringForce springForce, Float f, float f2, float f3, DynamicAnimation dynamicAnimation, boolean z, float f4, float f5) {
        float fMax;
        if (z) {
            return;
        }
        this.mRestingStackPosition.set(this.mStackPosition);
        if (f != null) {
            fMax = f.floatValue();
        } else {
            fMax = Math.max(f2, Math.min(f3, f4));
        }
        springFirstBubbleWithStackFollowing(viewProperty, springForce, f5, fMax, new Runnable[0]);
    }

    public void cancelStackPositionAnimations() {
        DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
        cancelStackPositionAnimation(viewProperty);
        DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
        cancelStackPositionAnimation(viewProperty2);
        removeEndActionForProperty(viewProperty);
        removeEndActionForProperty(viewProperty2);
    }

    public void setImeHeight(int i) {
        this.mImeHeight = i;
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0025  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public float animateForImeVisibility(boolean r9) {
        /*
            r8 = this;
            android.graphics.RectF r0 = r8.getAllowableStackPositionRegion()
            float r0 = r0.bottom
            r1 = -2147483647(0xffffffff80000001, float:-1.4E-45)
            if (r9 == 0) goto L1c
            android.graphics.PointF r9 = r8.mStackPosition
            float r9 = r9.y
            int r2 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r2 <= 0) goto L25
            float r2 = r8.mPreImeY
            int r2 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r2 != 0) goto L25
            r8.mPreImeY = r9
            goto L26
        L1c:
            float r0 = r8.mPreImeY
            int r9 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r9 == 0) goto L25
            r8.mPreImeY = r1
            goto L26
        L25:
            r0 = r1
        L26:
            int r9 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r9 == 0) goto L47
            androidx.dynamicanimation.animation.DynamicAnimation$ViewProperty r3 = androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Y
            r1 = 0
            androidx.dynamicanimation.animation.SpringForce r1 = r8.getSpringForce(r3, r1)
            r2 = 1128792064(0x43480000, float:200.0)
            androidx.dynamicanimation.animation.SpringForce r4 = r1.setStiffness(r2)
            r5 = 0
            r1 = 0
            java.lang.Runnable[] r7 = new java.lang.Runnable[r1]
            r2 = r8
            r6 = r0
            r2.springFirstBubbleWithStackFollowing(r3, r4, r5, r6, r7)
            android.graphics.PointF r1 = r8.mStackPosition
            float r1 = r1.x
            r8.notifyFloatingCoordinatorStackAnimatingTo(r1, r0)
        L47:
            if (r9 == 0) goto L4a
            goto L4e
        L4a:
            android.graphics.PointF r8 = r8.mStackPosition
            float r0 = r8.y
        L4e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.animation.StackAnimationController.animateForImeVisibility(boolean):float");
    }

    private void notifyFloatingCoordinatorStackAnimatingTo(float f, float f2) {
        Rect floatingBoundsOnScreen = this.mStackFloatingContent.getFloatingBoundsOnScreen();
        floatingBoundsOnScreen.offsetTo((int) f, (int) f2);
        this.mAnimatingToBounds = floatingBoundsOnScreen;
        this.mFloatingContentCoordinator.onContentMoved(this.mStackFloatingContent);
    }

    public RectF getAllowableStackPositionRegion() {
        WindowInsets rootWindowInsets = this.mLayout.getRootWindowInsets();
        RectF rectF = new RectF();
        if (rootWindowInsets != null) {
            rectF.left = (-this.mBubbleOffscreen) + Math.max(rootWindowInsets.getSystemWindowInsetLeft(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetLeft() : 0);
            rectF.right = ((this.mLayout.getWidth() - this.mBubbleSize) + this.mBubbleOffscreen) - Math.max(rootWindowInsets.getSystemWindowInsetRight(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetRight() : 0);
            rectF.top = this.mBubblePaddingTop + Math.max(this.mStatusBarHeight, rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetTop() : 0.0f);
            int height = this.mLayout.getHeight() - this.mBubbleSize;
            int i = this.mBubblePaddingTop;
            float f = height - i;
            float f2 = this.mImeHeight;
            rectF.bottom = (f - (f2 != -1.4E-45f ? f2 + i : 0.0f)) - Math.max(rootWindowInsets.getStableInsetBottom(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetBottom() : 0);
        }
        return rectF;
    }

    public void moveStackFromTouch(float f, float f2) {
        if (this.mSpringToTouchOnNextMotionEvent) {
            springStack(f, f2, 12000.0f);
            this.mSpringToTouchOnNextMotionEvent = false;
            this.mFirstBubbleSpringingToTouch = true;
        } else if (this.mFirstBubbleSpringingToTouch) {
            SpringAnimation springAnimation = (SpringAnimation) this.mStackPositionAnimations.get(DynamicAnimation.TRANSLATION_X);
            SpringAnimation springAnimation2 = (SpringAnimation) this.mStackPositionAnimations.get(DynamicAnimation.TRANSLATION_Y);
            if (springAnimation.isRunning() || springAnimation2.isRunning()) {
                springAnimation.animateToFinalPosition(f);
                springAnimation2.animateToFinalPosition(f2);
            } else {
                this.mFirstBubbleSpringingToTouch = false;
            }
        }
        if (this.mFirstBubbleSpringingToTouch || isStackStuckToTarget()) {
            return;
        }
        moveFirstBubbleWithStackFollowing(f, f2);
    }

    public void onUnstuckFromTarget() {
        this.mSpringToTouchOnNextMotionEvent = true;
    }

    public void animateStackDismissal(final float f, Runnable runnable) {
        animationsForChildrenFromIndex(0, new PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator() { // from class: com.android.systemui.bubbles.animation.StackAnimationController$$ExternalSyntheticLambda2
            @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator
            public final void configureAnimationForChildAtIndex(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
                this.f$0.lambda$animateStackDismissal$1(f, i, physicsPropertyAnimator);
            }
        }).startAll(runnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateStackDismissal$1(float f, int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
        physicsPropertyAnimator.scaleX(0.0f, new Runnable[0]).scaleY(0.0f, new Runnable[0]).alpha(0.0f, new Runnable[0]).translationY(this.mLayout.getChildAt(i).getTranslationY() + f, new Runnable[0]).withStiffness(10000.0f);
    }

    protected void springFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, SpringForce springForce, float f, float f2, final Runnable... runnableArr) {
        if (this.mLayout.getChildCount() == 0 || !isActiveController()) {
            return;
        }
        Log.d("Bubbs.StackCtrl", String.format("Springing %s to final position %f.", PhysicsAnimationLayout.getReadablePropertyName(viewProperty), Float.valueOf(f2)));
        final boolean z = this.mSpringToTouchOnNextMotionEvent;
        SpringAnimation startVelocity = new SpringAnimation(this, new StackPositionProperty(viewProperty)).setSpring(springForce).addEndListener(new DynamicAnimation.OnAnimationEndListener() { // from class: com.android.systemui.bubbles.animation.StackAnimationController$$ExternalSyntheticLambda1
            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
            public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z2, float f3, float f4) {
                this.f$0.lambda$springFirstBubbleWithStackFollowing$2(z, runnableArr, dynamicAnimation, z2, f3, f4);
            }
        }).setStartVelocity(f);
        cancelStackPositionAnimation(viewProperty);
        this.mStackPositionAnimations.put(viewProperty, startVelocity);
        startVelocity.animateToFinalPosition(f2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$springFirstBubbleWithStackFollowing$2(boolean z, Runnable[] runnableArr, DynamicAnimation dynamicAnimation, boolean z2, float f, float f2) {
        if (!z) {
            this.mRestingStackPosition.set(this.mStackPosition);
        }
        if (runnableArr != null) {
            for (Runnable runnable : runnableArr) {
                runnable.run();
            }
        }
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
        return Sets.newHashSet(new DynamicAnimation.ViewProperty[]{DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y, DynamicAnimation.ALPHA, DynamicAnimation.SCALE_X, DynamicAnimation.SCALE_Y});
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    int getNextAnimationInChain(DynamicAnimation.ViewProperty viewProperty, int i) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X) || viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return i + 1;
        }
        return -1;
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    float getOffsetForChainedPropertyAnimation(DynamicAnimation.ViewProperty viewProperty) {
        if (!viewProperty.equals(DynamicAnimation.TRANSLATION_X) || isStackStuckToTarget()) {
            return 0.0f;
        }
        return this.mLayout.isFirstChildXLeftOfCenter(this.mStackPosition.x) ? -this.mStackOffset : this.mStackOffset;
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    SpringForce getSpringForce(DynamicAnimation.ViewProperty viewProperty, View view) {
        ContentResolver contentResolver = this.mLayout.getContext().getContentResolver();
        return new SpringForce().setDampingRatio(Settings.Secure.getFloat(contentResolver, "bubble_damping", 0.9f)).setStiffness(Settings.Secure.getFloat(contentResolver, "bubble_stiffness", this.mIsMovingFromFlinging ? 20000.0f : 12000.0f));
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onChildAdded(View view, int i) {
        if (isStackStuckToTarget()) {
            return;
        }
        if (getBubbleCount() == 1) {
            moveStackToStartPosition();
        } else if (isStackPositionSet() && this.mLayout.indexOfChild(view) == 0) {
            animateInBubble(view, i);
        }
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onChildRemoved(View view, int i, Runnable runnable) {
        PhysicsAnimator.getInstance(view).spring(DynamicAnimation.ALPHA, 0.0f).spring(DynamicAnimation.SCALE_X, 0.0f, this.mAnimateOutSpringConfig).spring(DynamicAnimation.SCALE_Y, 0.0f, this.mAnimateOutSpringConfig).withEndActions(runnable, this.mOnBubbleAnimatedOutAction).start();
        if (getBubbleCount() > 0) {
            animationForChildAtIndex(0).translationX(this.mStackPosition.x, new Runnable[0]).start(new Runnable[0]);
            return;
        }
        PointF startPosition = this.mRestingStackPosition;
        if (startPosition == null) {
            startPosition = getStartPosition();
        }
        setStackPosition(startPosition);
        this.mFloatingContentCoordinator.onContentRemoved(this.mStackFloatingContent);
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onChildReordered(View view, int i, int i2) {
        if (isStackPositionSet()) {
            setStackPosition(this.mStackPosition);
        }
    }

    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    void onActiveControllerForLayout(PhysicsAnimationLayout physicsAnimationLayout) {
        Resources resources = physicsAnimationLayout.getResources();
        this.mStackOffset = resources.getDimensionPixelSize(R.dimen.bubble_stack_offset);
        this.mBubbleSize = resources.getDimensionPixelSize(R.dimen.individual_bubble_size);
        this.mBubbleBitmapSize = resources.getDimensionPixelSize(R.dimen.bubble_bitmap_size);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(R.dimen.bubble_padding_top);
        this.mBubbleOffscreen = resources.getDimensionPixelSize(R.dimen.bubble_stack_offscreen);
        this.mStackStartingVerticalOffset = resources.getDimensionPixelSize(R.dimen.bubble_stack_starting_offset_y);
        this.mStatusBarHeight = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
    }

    public void updateResources(int i) {
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (physicsAnimationLayout != null) {
            this.mBubblePaddingTop = physicsAnimationLayout.getContext().getResources().getDimensionPixelSize(R.dimen.bubble_padding_top);
            this.mStatusBarHeight = r2.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        }
    }

    private boolean isStackStuckToTarget() {
        MagnetizedObject<StackAnimationController> magnetizedObject = this.mMagnetizedStack;
        return magnetizedObject != null && magnetizedObject.getObjectStuckToTarget();
    }

    private void moveStackToStartPosition() {
        this.mLayout.setVisibility(4);
        this.mLayout.post(new Runnable() { // from class: com.android.systemui.bubbles.animation.StackAnimationController$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$moveStackToStartPosition$3();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$moveStackToStartPosition$3() {
        PointF startPosition = this.mRestingStackPosition;
        if (startPosition == null) {
            startPosition = getStartPosition();
        }
        setStackPosition(startPosition);
        this.mStackMovedToStartPosition = true;
        this.mLayout.setVisibility(0);
        if (this.mLayout.getChildCount() > 0) {
            this.mFloatingContentCoordinator.onContentAdded(this.mStackFloatingContent);
            animateInBubble(this.mLayout.getChildAt(0), 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void moveFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, float f) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            this.mStackPosition.x = f;
        } else if (viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            this.mStackPosition.y = f;
        }
        if (this.mLayout.getChildCount() > 0) {
            viewProperty.setValue(this.mLayout.getChildAt(0), f);
            if (this.mLayout.getChildCount() > 1) {
                animationForChildAtIndex(1).property(viewProperty, f + getOffsetForChainedPropertyAnimation(viewProperty), new Runnable[0]).start(new Runnable[0]);
            }
        }
    }

    public void setStackPosition(PointF pointF) {
        Log.d("Bubbs.StackCtrl", String.format("Setting position to (%f, %f).", Float.valueOf(pointF.x), Float.valueOf(pointF.y)));
        this.mStackPosition.set(pointF.x, pointF.y);
        if (this.mRestingStackPosition == null) {
            this.mRestingStackPosition = new PointF();
        }
        this.mRestingStackPosition.set(this.mStackPosition);
        if (isActiveController()) {
            PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
            DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
            DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
            physicsAnimationLayout.cancelAllAnimationsOfProperties(viewProperty, viewProperty2);
            cancelStackPositionAnimations();
            float offsetForChainedPropertyAnimation = getOffsetForChainedPropertyAnimation(viewProperty);
            float offsetForChainedPropertyAnimation2 = getOffsetForChainedPropertyAnimation(viewProperty2);
            for (int i = 0; i < this.mLayout.getChildCount(); i++) {
                float f = i;
                this.mLayout.getChildAt(i).setTranslationX(pointF.x + (f * offsetForChainedPropertyAnimation));
                this.mLayout.getChildAt(i).setTranslationY(pointF.y + (f * offsetForChainedPropertyAnimation2));
            }
        }
    }

    public void setStackPosition(BubbleStackView.RelativeStackPosition relativeStackPosition) {
        setStackPosition(relativeStackPosition.getAbsolutePositionInRegion(getAllowableStackPositionRegion()));
    }

    public BubbleStackView.RelativeStackPosition getRelativeStackPosition() {
        return new BubbleStackView.RelativeStackPosition(this.mStackPosition, getAllowableStackPositionRegion());
    }

    public void setStackStartPosition(BubbleStackView.RelativeStackPosition relativeStackPosition) {
        this.mStackStartPosition = relativeStackPosition;
    }

    public PointF getStartPosition() {
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (physicsAnimationLayout == null) {
            return null;
        }
        if (this.mStackStartPosition == null) {
            this.mStackStartPosition = new BubbleStackView.RelativeStackPosition(physicsAnimationLayout.getResources().getConfiguration().getLayoutDirection() != 1, this.mLayout.getResources().getDimensionPixelOffset(R.dimen.bubble_stack_starting_offset_y) / getAllowableStackPositionRegion().height());
        }
        return this.mStackStartPosition.getAbsolutePositionInRegion(getAllowableStackPositionRegion());
    }

    private boolean isStackPositionSet() {
        return this.mStackMovedToStartPosition;
    }

    private void animateInBubble(View view, int i) {
        if (isActiveController()) {
            float offsetForChainedPropertyAnimation = getOffsetForChainedPropertyAnimation(DynamicAnimation.TRANSLATION_X);
            view.setTranslationX(this.mStackPosition.x + (i * offsetForChainedPropertyAnimation));
            view.setTranslationY(this.mStackPosition.y);
            view.setScaleX(0.0f);
            view.setScaleY(0.0f);
            int i2 = i + 1;
            if (i2 < this.mLayout.getChildCount()) {
                animationForChildAtIndex(i2).translationX(this.mStackPosition.x + (offsetForChainedPropertyAnimation * i2), new Runnable[0]).withStiffness(200.0f).start(new Runnable[0]);
            }
            animationForChild(view).scaleX(1.0f, new Runnable[0]).scaleY(1.0f, new Runnable[0]).withStiffness(1000.0f).withStartDelay(this.mLayout.getChildCount() > 1 ? 25L : 0L).start(new Runnable[0]);
        }
    }

    private void cancelStackPositionAnimation(DynamicAnimation.ViewProperty viewProperty) {
        if (this.mStackPositionAnimations.containsKey(viewProperty)) {
            this.mStackPositionAnimations.get(viewProperty).cancel();
        }
    }

    public MagnetizedObject<StackAnimationController> getMagnetizedStack(MagnetizedObject.MagneticTarget magneticTarget) {
        if (this.mMagnetizedStack == null) {
            MagnetizedObject<StackAnimationController> magnetizedObject = new MagnetizedObject<StackAnimationController>(this.mLayout.getContext(), this, new StackPositionProperty(DynamicAnimation.TRANSLATION_X), new StackPositionProperty(DynamicAnimation.TRANSLATION_Y)) { // from class: com.android.systemui.bubbles.animation.StackAnimationController.2
                @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
                public float getWidth(StackAnimationController stackAnimationController) {
                    return StackAnimationController.this.mBubbleSize;
                }

                @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
                public float getHeight(StackAnimationController stackAnimationController) {
                    return StackAnimationController.this.mBubbleSize;
                }

                @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
                public void getLocationOnScreen(StackAnimationController stackAnimationController, int[] iArr) {
                    iArr[0] = (int) StackAnimationController.this.mStackPosition.x;
                    iArr[1] = (int) StackAnimationController.this.mStackPosition.y;
                }
            };
            this.mMagnetizedStack = magnetizedObject;
            magnetizedObject.addTarget(magneticTarget);
            this.mMagnetizedStack.setHapticsEnabled(true);
            this.mMagnetizedStack.setFlingToTargetMinVelocity(4000.0f);
        }
        ContentResolver contentResolver = this.mLayout.getContext().getContentResolver();
        float f = Settings.Secure.getFloat(contentResolver, "bubble_dismiss_fling_min_velocity", this.mMagnetizedStack.getFlingToTargetMinVelocity());
        float f2 = Settings.Secure.getFloat(contentResolver, "bubble_dismiss_stick_max_velocity", this.mMagnetizedStack.getStickToTargetMaxXVelocity());
        float f3 = Settings.Secure.getFloat(contentResolver, "bubble_dismiss_target_width_percent", this.mMagnetizedStack.getFlingToTargetWidthPercent());
        this.mMagnetizedStack.setFlingToTargetMinVelocity(f);
        this.mMagnetizedStack.setStickToTargetMaxXVelocity(f2);
        this.mMagnetizedStack.setFlingToTargetWidthPercent(f3);
        return this.mMagnetizedStack;
    }

    private int getBubbleCount() {
        return this.mBubbleCountSupplier.getAsInt();
    }

    private class StackPositionProperty extends FloatPropertyCompat<StackAnimationController> {
        private final DynamicAnimation.ViewProperty mProperty;

        private StackPositionProperty(DynamicAnimation.ViewProperty viewProperty) {
            super(viewProperty.toString());
            this.mProperty = viewProperty;
        }

        @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
        public float getValue(StackAnimationController stackAnimationController) {
            if (StackAnimationController.this.mLayout.getChildCount() > 0) {
                return this.mProperty.getValue(StackAnimationController.this.mLayout.getChildAt(0));
            }
            return 0.0f;
        }

        @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
        public void setValue(StackAnimationController stackAnimationController, float f) {
            StackAnimationController.this.moveFirstBubbleWithStackFollowing(this.mProperty, f);
        }
    }
}
