package com.android.systemui.bubbles.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.R;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/* loaded from: classes.dex */
public class PhysicsAnimationLayout extends FrameLayout {
    protected PhysicsAnimationController mController;
    protected final HashMap<DynamicAnimation.ViewProperty, Runnable> mEndActionForProperty;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static abstract class PhysicsAnimationController {
        protected PhysicsAnimationLayout mLayout;

        interface ChildAnimationConfigurator {
            void configureAnimationForChildAtIndex(int i, PhysicsPropertyAnimator physicsPropertyAnimator);
        }

        interface MultiAnimationStarter {
            void startAll(Runnable... runnableArr);
        }

        abstract Set<DynamicAnimation.ViewProperty> getAnimatedProperties();

        abstract int getNextAnimationInChain(DynamicAnimation.ViewProperty viewProperty, int i);

        abstract float getOffsetForChainedPropertyAnimation(DynamicAnimation.ViewProperty viewProperty);

        abstract SpringForce getSpringForce(DynamicAnimation.ViewProperty viewProperty, View view);

        abstract void onActiveControllerForLayout(PhysicsAnimationLayout physicsAnimationLayout);

        abstract void onChildAdded(View view, int i);

        abstract void onChildRemoved(View view, int i, Runnable runnable);

        abstract void onChildReordered(View view, int i, int i2);

        PhysicsAnimationController() {
        }

        protected boolean isActiveController() {
            PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
            return physicsAnimationLayout != null && this == physicsAnimationLayout.mController;
        }

        protected void setLayout(PhysicsAnimationLayout physicsAnimationLayout) {
            this.mLayout = physicsAnimationLayout;
            onActiveControllerForLayout(physicsAnimationLayout);
        }

        protected PhysicsPropertyAnimator animationForChild(View view) {
            int i = R.id.physics_animator_tag;
            PhysicsPropertyAnimator physicsPropertyAnimator = (PhysicsPropertyAnimator) view.getTag(i);
            if (physicsPropertyAnimator == null) {
                PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
                Objects.requireNonNull(physicsAnimationLayout);
                physicsPropertyAnimator = physicsAnimationLayout.new PhysicsPropertyAnimator(view);
                view.setTag(i, physicsPropertyAnimator);
            }
            physicsPropertyAnimator.clearAnimator();
            physicsPropertyAnimator.setAssociatedController(this);
            return physicsPropertyAnimator;
        }

        protected PhysicsPropertyAnimator animationForChildAtIndex(int i) {
            return animationForChild(this.mLayout.getChildAt(i));
        }

        protected MultiAnimationStarter animationsForChildrenFromIndex(int i, ChildAnimationConfigurator childAnimationConfigurator) {
            final HashSet hashSet = new HashSet();
            final ArrayList arrayList = new ArrayList();
            while (i < this.mLayout.getChildCount()) {
                PhysicsPropertyAnimator physicsPropertyAnimatorAnimationForChildAtIndex = animationForChildAtIndex(i);
                childAnimationConfigurator.configureAnimationForChildAtIndex(i, physicsPropertyAnimatorAnimationForChildAtIndex);
                hashSet.addAll(physicsPropertyAnimatorAnimationForChildAtIndex.getAnimatedProperties());
                arrayList.add(physicsPropertyAnimatorAnimationForChildAtIndex);
                i++;
            }
            return new MultiAnimationStarter() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsAnimationController$$ExternalSyntheticLambda0
                @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.MultiAnimationStarter
                public final void startAll(Runnable[] runnableArr) {
                    this.f$0.lambda$animationsForChildrenFromIndex$1(hashSet, arrayList, runnableArr);
                }
            };
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$animationsForChildrenFromIndex$1(Set set, List list, final Runnable[] runnableArr) {
            Runnable runnable = new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsAnimationController$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    PhysicsAnimationLayout.PhysicsAnimationController.lambda$animationsForChildrenFromIndex$0(runnableArr);
                }
            };
            if (this.mLayout.getChildCount() == 0) {
                runnable.run();
                return;
            }
            if (runnableArr != null) {
                setEndActionForMultipleProperties(runnable, (DynamicAnimation.ViewProperty[]) set.toArray(new DynamicAnimation.ViewProperty[0]));
            }
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ((PhysicsPropertyAnimator) it.next()).start(new Runnable[0]);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$animationsForChildrenFromIndex$0(Runnable[] runnableArr) {
            for (Runnable runnable : runnableArr) {
                runnable.run();
            }
        }

        protected void setEndActionForProperty(Runnable runnable, DynamicAnimation.ViewProperty viewProperty) {
            this.mLayout.mEndActionForProperty.put(viewProperty, runnable);
        }

        protected void setEndActionForMultipleProperties(final Runnable runnable, final DynamicAnimation.ViewProperty... viewPropertyArr) {
            Runnable runnable2 = new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsAnimationController$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$setEndActionForMultipleProperties$2(viewPropertyArr, runnable);
                }
            };
            for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
                setEndActionForProperty(runnable2, viewProperty);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$setEndActionForMultipleProperties$2(DynamicAnimation.ViewProperty[] viewPropertyArr, Runnable runnable) {
            if (this.mLayout.arePropertiesAnimating(viewPropertyArr)) {
                return;
            }
            runnable.run();
            for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
                removeEndActionForProperty(viewProperty);
            }
        }

        protected void removeEndActionForProperty(DynamicAnimation.ViewProperty viewProperty) {
            this.mLayout.mEndActionForProperty.remove(viewProperty);
        }
    }

    public PhysicsAnimationLayout(Context context) {
        super(context);
        this.mEndActionForProperty = new HashMap<>();
    }

    public void setActiveController(PhysicsAnimationController physicsAnimationController) {
        cancelAllAnimations();
        this.mEndActionForProperty.clear();
        this.mController = physicsAnimationController;
        physicsAnimationController.setLayout(this);
        Iterator<DynamicAnimation.ViewProperty> it = this.mController.getAnimatedProperties().iterator();
        while (it.hasNext()) {
            setUpAnimationsForProperty(it.next());
        }
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view, i, layoutParams, false);
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(final View view) {
        if (this.mController != null) {
            int iIndexOfChild = indexOfChild(view);
            super.removeView(view);
            addTransientView(view, iIndexOfChild);
            this.mController.onChildRemoved(view, iIndexOfChild, new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$removeView$0(view);
                }
            });
            return;
        }
        super.removeView(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$removeView$0(View view) {
        cancelAnimationsOnView(view);
        removeTransientView(view);
    }

    @Override // android.view.ViewGroup
    public void removeViewAt(int i) {
        removeView(getChildAt(i));
    }

    public void reorderView(View view, int i) {
        if (view == null) {
            return;
        }
        int iIndexOfChild = indexOfChild(view);
        super.removeView(view);
        addViewInternal(view, i, view.getLayoutParams(), true);
        PhysicsAnimationController physicsAnimationController = this.mController;
        if (physicsAnimationController != null) {
            physicsAnimationController.onChildReordered(view, iIndexOfChild, i);
        }
    }

    public boolean arePropertiesAnimating(DynamicAnimation.ViewProperty... viewPropertyArr) {
        for (int i = 0; i < getChildCount(); i++) {
            if (arePropertiesAnimatingOnView(getChildAt(i), viewPropertyArr)) {
                return true;
            }
        }
        return false;
    }

    public boolean arePropertiesAnimatingOnView(View view, DynamicAnimation.ViewProperty... viewPropertyArr) {
        ObjectAnimator targetAnimatorFromView = getTargetAnimatorFromView(view);
        for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
            SpringAnimation animationFromView = getAnimationFromView(viewProperty, view);
            if (animationFromView != null && animationFromView.isRunning()) {
                return true;
            }
            if ((viewProperty.equals(DynamicAnimation.TRANSLATION_X) || viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) && targetAnimatorFromView != null && targetAnimatorFromView.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public void cancelAllAnimations() {
        PhysicsAnimationController physicsAnimationController = this.mController;
        if (physicsAnimationController == null) {
            return;
        }
        cancelAllAnimationsOfProperties((DynamicAnimation.ViewProperty[]) physicsAnimationController.getAnimatedProperties().toArray(new DynamicAnimation.ViewProperty[0]));
    }

    public void cancelAllAnimationsOfProperties(DynamicAnimation.ViewProperty... viewPropertyArr) {
        if (this.mController == null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
                SpringAnimation animationAtIndex = getAnimationAtIndex(viewProperty, i);
                if (animationAtIndex != null) {
                    animationAtIndex.cancel();
                }
            }
        }
    }

    public void cancelAnimationsOnView(View view) {
        ObjectAnimator targetAnimatorFromView = getTargetAnimatorFromView(view);
        if (targetAnimatorFromView != null) {
            targetAnimatorFromView.cancel();
        }
        Iterator<DynamicAnimation.ViewProperty> it = this.mController.getAnimatedProperties().iterator();
        while (it.hasNext()) {
            SpringAnimation animationFromView = getAnimationFromView(it.next(), view);
            if (animationFromView != null) {
                animationFromView.cancel();
            }
        }
    }

    protected boolean isActiveController(PhysicsAnimationController physicsAnimationController) {
        return this.mController == physicsAnimationController;
    }

    protected boolean isFirstChildXLeftOfCenter(float f) {
        return getChildCount() > 0 && f + ((float) (getChildAt(0).getWidth() / 2)) < ((float) (getWidth() / 2));
    }

    protected static String getReadablePropertyName(DynamicAnimation.ViewProperty viewProperty) {
        return viewProperty.equals(DynamicAnimation.TRANSLATION_X) ? "TRANSLATION_X" : viewProperty.equals(DynamicAnimation.TRANSLATION_Y) ? "TRANSLATION_Y" : viewProperty.equals(DynamicAnimation.SCALE_X) ? "SCALE_X" : viewProperty.equals(DynamicAnimation.SCALE_Y) ? "SCALE_Y" : viewProperty.equals(DynamicAnimation.ALPHA) ? "ALPHA" : "Unknown animation property.";
    }

    private void addViewInternal(View view, int i, ViewGroup.LayoutParams layoutParams, boolean z) {
        super.addView(view, i, layoutParams);
        PhysicsAnimationController physicsAnimationController = this.mController;
        if (physicsAnimationController == null || z) {
            return;
        }
        Iterator<DynamicAnimation.ViewProperty> it = physicsAnimationController.getAnimatedProperties().iterator();
        while (it.hasNext()) {
            setUpAnimationForChild(it.next(), view, i);
        }
        this.mController.onChildAdded(view, i);
    }

    private SpringAnimation getAnimationAtIndex(DynamicAnimation.ViewProperty viewProperty, int i) {
        return getAnimationFromView(viewProperty, getChildAt(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SpringAnimation getAnimationFromView(DynamicAnimation.ViewProperty viewProperty, View view) {
        return (SpringAnimation) view.getTag(getTagIdForProperty(viewProperty));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ObjectAnimator getTargetAnimatorFromView(View view) {
        return (ObjectAnimator) view.getTag(R.id.target_animator_tag);
    }

    private void setUpAnimationsForProperty(DynamicAnimation.ViewProperty viewProperty) {
        for (int i = 0; i < getChildCount(); i++) {
            setUpAnimationForChild(viewProperty, getChildAt(i), i);
        }
    }

    private void setUpAnimationForChild(final DynamicAnimation.ViewProperty viewProperty, final View view, int i) {
        SpringAnimation springAnimation = new SpringAnimation(view, viewProperty);
        springAnimation.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$$ExternalSyntheticLambda0
            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
            public final void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2) {
                this.f$0.lambda$setUpAnimationForChild$1(view, viewProperty, dynamicAnimation, f, f2);
            }
        });
        springAnimation.setSpring(this.mController.getSpringForce(viewProperty, view));
        springAnimation.addEndListener(new AllAnimationsForPropertyFinishedEndListener(viewProperty));
        view.setTag(getTagIdForProperty(viewProperty), springAnimation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setUpAnimationForChild$1(View view, DynamicAnimation.ViewProperty viewProperty, DynamicAnimation dynamicAnimation, float f, float f2) {
        SpringAnimation animationAtIndex;
        int iIndexOfChild = indexOfChild(view);
        int nextAnimationInChain = this.mController.getNextAnimationInChain(viewProperty, iIndexOfChild);
        if (nextAnimationInChain == -1 || iIndexOfChild < 0) {
            return;
        }
        float offsetForChainedPropertyAnimation = this.mController.getOffsetForChainedPropertyAnimation(viewProperty);
        if (nextAnimationInChain >= getChildCount() || (animationAtIndex = getAnimationAtIndex(viewProperty, nextAnimationInChain)) == null) {
            return;
        }
        animationAtIndex.animateToFinalPosition(f + offsetForChainedPropertyAnimation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getTagIdForProperty(DynamicAnimation.ViewProperty viewProperty) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            return R.id.translation_x_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return R.id.translation_y_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_X)) {
            return R.id.scale_x_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_Y)) {
            return R.id.scale_y_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.ALPHA)) {
            return R.id.alpha_dynamicanimation_tag;
        }
        return -1;
    }

    protected class AllAnimationsForPropertyFinishedEndListener implements DynamicAnimation.OnAnimationEndListener {
        private DynamicAnimation.ViewProperty mProperty;

        AllAnimationsForPropertyFinishedEndListener(DynamicAnimation.ViewProperty viewProperty) {
            this.mProperty = viewProperty;
        }

        @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
            Runnable runnable;
            if (PhysicsAnimationLayout.this.arePropertiesAnimating(this.mProperty) || !PhysicsAnimationLayout.this.mEndActionForProperty.containsKey(this.mProperty) || (runnable = PhysicsAnimationLayout.this.mEndActionForProperty.get(this.mProperty)) == null) {
                return;
            }
            runnable.run();
        }
    }

    protected class PhysicsPropertyAnimator {
        private PhysicsAnimationController mAssociatedController;
        private ObjectAnimator mPathAnimator;
        private Runnable[] mPositionEndActions;
        private View mView;
        private float mDefaultStartVelocity = -3.4028235E38f;
        private long mStartDelay = 0;
        private float mDampingRatio = -1.0f;
        private float mStiffness = -1.0f;
        private Map<DynamicAnimation.ViewProperty, Runnable[]> mEndActionsForProperty = new HashMap();
        private Map<DynamicAnimation.ViewProperty, Float> mPositionStartVelocities = new HashMap();
        private Map<DynamicAnimation.ViewProperty, Float> mAnimatedProperties = new HashMap();
        private Map<DynamicAnimation.ViewProperty, Float> mInitialPropertyValues = new HashMap();
        private PointF mCurrentPointOnPath = new PointF();
        private final FloatProperty<PhysicsPropertyAnimator> mCurrentPointOnPathXProperty = new FloatProperty<PhysicsPropertyAnimator>("PathX") { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.1
            @Override // android.util.FloatProperty
            public void setValue(PhysicsPropertyAnimator physicsPropertyAnimator, float f) {
                PhysicsPropertyAnimator.this.mCurrentPointOnPath.x = f;
            }

            @Override // android.util.Property
            public Float get(PhysicsPropertyAnimator physicsPropertyAnimator) {
                return Float.valueOf(PhysicsPropertyAnimator.this.mCurrentPointOnPath.x);
            }
        };
        private final FloatProperty<PhysicsPropertyAnimator> mCurrentPointOnPathYProperty = new FloatProperty<PhysicsPropertyAnimator>("PathY") { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.2
            @Override // android.util.FloatProperty
            public void setValue(PhysicsPropertyAnimator physicsPropertyAnimator, float f) {
                PhysicsPropertyAnimator.this.mCurrentPointOnPath.y = f;
            }

            @Override // android.util.Property
            public Float get(PhysicsPropertyAnimator physicsPropertyAnimator) {
                return Float.valueOf(PhysicsPropertyAnimator.this.mCurrentPointOnPath.y);
            }
        };

        protected PhysicsPropertyAnimator(View view) {
            this.mView = view;
        }

        public PhysicsPropertyAnimator property(DynamicAnimation.ViewProperty viewProperty, float f, Runnable... runnableArr) {
            this.mAnimatedProperties.put(viewProperty, Float.valueOf(f));
            this.mEndActionsForProperty.put(viewProperty, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator alpha(float f, Runnable... runnableArr) {
            return property(DynamicAnimation.ALPHA, f, runnableArr);
        }

        public PhysicsPropertyAnimator translationX(float f, Runnable... runnableArr) {
            this.mPathAnimator = null;
            return property(DynamicAnimation.TRANSLATION_X, f, runnableArr);
        }

        public PhysicsPropertyAnimator translationY(float f, Runnable... runnableArr) {
            this.mPathAnimator = null;
            return property(DynamicAnimation.TRANSLATION_Y, f, runnableArr);
        }

        public PhysicsPropertyAnimator translationY(float f, float f2, Runnable... runnableArr) {
            this.mInitialPropertyValues.put(DynamicAnimation.TRANSLATION_Y, Float.valueOf(f));
            return translationY(f2, runnableArr);
        }

        public PhysicsPropertyAnimator position(float f, float f2, Runnable... runnableArr) {
            this.mPositionEndActions = runnableArr;
            translationX(f, new Runnable[0]);
            return translationY(f2, new Runnable[0]);
        }

        public PhysicsPropertyAnimator followAnimatedTargetAlongPath(Path path, int i, TimeInterpolator timeInterpolator, final Runnable... runnableArr) {
            ObjectAnimator objectAnimator = this.mPathAnimator;
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this, this.mCurrentPointOnPathXProperty, this.mCurrentPointOnPathYProperty, path);
            this.mPathAnimator = objectAnimatorOfFloat;
            if (runnableArr != null) {
                objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.3
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        for (Runnable runnable : runnableArr) {
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    }
                });
            }
            this.mPathAnimator.setDuration(i);
            this.mPathAnimator.setInterpolator(timeInterpolator);
            clearTranslationValues();
            return this;
        }

        private void clearTranslationValues() {
            Map<DynamicAnimation.ViewProperty, Float> map = this.mAnimatedProperties;
            DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
            map.remove(viewProperty);
            Map<DynamicAnimation.ViewProperty, Float> map2 = this.mAnimatedProperties;
            DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
            map2.remove(viewProperty2);
            this.mInitialPropertyValues.remove(viewProperty);
            this.mInitialPropertyValues.remove(viewProperty2);
            PhysicsAnimationLayout.this.mEndActionForProperty.remove(viewProperty);
            PhysicsAnimationLayout.this.mEndActionForProperty.remove(viewProperty2);
        }

        public PhysicsPropertyAnimator scaleX(float f, Runnable... runnableArr) {
            return property(DynamicAnimation.SCALE_X, f, runnableArr);
        }

        public PhysicsPropertyAnimator scaleY(float f, Runnable... runnableArr) {
            return property(DynamicAnimation.SCALE_Y, f, runnableArr);
        }

        public PhysicsPropertyAnimator withStiffness(float f) {
            this.mStiffness = f;
            return this;
        }

        public PhysicsPropertyAnimator withPositionStartVelocities(float f, float f2) {
            this.mPositionStartVelocities.put(DynamicAnimation.TRANSLATION_X, Float.valueOf(f));
            this.mPositionStartVelocities.put(DynamicAnimation.TRANSLATION_Y, Float.valueOf(f2));
            return this;
        }

        public PhysicsPropertyAnimator withStartDelay(long j) {
            this.mStartDelay = j;
            return this;
        }

        public void start(final Runnable... runnableArr) {
            if (!PhysicsAnimationLayout.this.isActiveController(this.mAssociatedController)) {
                Log.w("Bubbs.PAL", "Only the active animation controller is allowed to start animations. Use PhysicsAnimationLayout#setActiveController to set the active animation controller.");
                return;
            }
            Set<DynamicAnimation.ViewProperty> animatedProperties = getAnimatedProperties();
            if (runnableArr != null && runnableArr.length > 0) {
                this.mAssociatedController.setEndActionForMultipleProperties(new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsPropertyAnimator$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() {
                        PhysicsAnimationLayout.PhysicsPropertyAnimator.lambda$start$0(runnableArr);
                    }
                }, (DynamicAnimation.ViewProperty[]) animatedProperties.toArray(new DynamicAnimation.ViewProperty[0]));
            }
            if (this.mPositionEndActions != null) {
                PhysicsAnimationLayout physicsAnimationLayout = PhysicsAnimationLayout.this;
                DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
                final SpringAnimation animationFromView = physicsAnimationLayout.getAnimationFromView(viewProperty, this.mView);
                PhysicsAnimationLayout physicsAnimationLayout2 = PhysicsAnimationLayout.this;
                DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
                final SpringAnimation animationFromView2 = physicsAnimationLayout2.getAnimationFromView(viewProperty2, this.mView);
                Runnable runnable = new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsPropertyAnimator$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$start$1(animationFromView, animationFromView2);
                    }
                };
                this.mEndActionsForProperty.put(viewProperty, new Runnable[]{runnable});
                this.mEndActionsForProperty.put(viewProperty2, new Runnable[]{runnable});
            }
            if (this.mPathAnimator != null) {
                startPathAnimation();
            }
            for (DynamicAnimation.ViewProperty viewProperty3 : animatedProperties) {
                if (this.mPathAnimator != null && (viewProperty3.equals(DynamicAnimation.TRANSLATION_X) || viewProperty3.equals(DynamicAnimation.TRANSLATION_Y))) {
                    return;
                }
                if (this.mInitialPropertyValues.containsKey(viewProperty3)) {
                    viewProperty3.setValue(this.mView, this.mInitialPropertyValues.get(viewProperty3).floatValue());
                }
                SpringForce springForce = PhysicsAnimationLayout.this.mController.getSpringForce(viewProperty3, this.mView);
                View view = this.mView;
                float fFloatValue = this.mAnimatedProperties.get(viewProperty3).floatValue();
                float fFloatValue2 = this.mPositionStartVelocities.getOrDefault(viewProperty3, Float.valueOf(this.mDefaultStartVelocity)).floatValue();
                long j = this.mStartDelay;
                float stiffness = this.mStiffness;
                if (stiffness < 0.0f) {
                    stiffness = springForce.getStiffness();
                }
                float f = stiffness;
                float f2 = this.mDampingRatio;
                animateValueForChild(viewProperty3, view, fFloatValue, fFloatValue2, j, f, f2 >= 0.0f ? f2 : springForce.getDampingRatio(), this.mEndActionsForProperty.get(viewProperty3));
            }
            clearAnimator();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$start$0(Runnable[] runnableArr) {
            for (Runnable runnable : runnableArr) {
                runnable.run();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$start$1(SpringAnimation springAnimation, SpringAnimation springAnimation2) {
            if (springAnimation.isRunning() || springAnimation2.isRunning()) {
                return;
            }
            Runnable[] runnableArr = this.mPositionEndActions;
            if (runnableArr != null) {
                for (Runnable runnable : runnableArr) {
                    runnable.run();
                }
            }
            this.mPositionEndActions = null;
        }

        protected Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
            HashSet hashSet = new HashSet(this.mAnimatedProperties.keySet());
            if (this.mPathAnimator != null) {
                hashSet.add(DynamicAnimation.TRANSLATION_X);
                hashSet.add(DynamicAnimation.TRANSLATION_Y);
            }
            return hashSet;
        }

        protected void animateValueForChild(DynamicAnimation.ViewProperty viewProperty, View view, final float f, final float f2, long j, final float f3, final float f4, final Runnable... runnableArr) {
            final SpringAnimation springAnimation;
            if (view == null || (springAnimation = (SpringAnimation) view.getTag(PhysicsAnimationLayout.this.getTagIdForProperty(viewProperty))) == null) {
                return;
            }
            if (runnableArr != null) {
                springAnimation.addEndListener(new OneTimeEndListener() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.4
                    @Override // com.android.systemui.bubbles.animation.OneTimeEndListener, androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                    public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f5, float f6) {
                        super.onAnimationEnd(dynamicAnimation, z, f5, f6);
                        for (Runnable runnable : runnableArr) {
                            runnable.run();
                        }
                    }
                });
            }
            final SpringForce spring = springAnimation.getSpring();
            if (spring == null) {
                return;
            }
            Runnable runnable = new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsPropertyAnimator$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    PhysicsAnimationLayout.PhysicsPropertyAnimator.lambda$animateValueForChild$2(spring, f3, f4, f2, springAnimation, f);
                }
            };
            if (j > 0) {
                PhysicsAnimationLayout.this.postDelayed(runnable, j);
            } else {
                runnable.run();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$animateValueForChild$2(SpringForce springForce, float f, float f2, float f3, SpringAnimation springAnimation, float f4) {
            springForce.setStiffness(f);
            springForce.setDampingRatio(f2);
            if (f3 > -3.4028235E38f) {
                springAnimation.setStartVelocity(f3);
            }
            springForce.setFinalPosition(f4);
            springAnimation.start();
        }

        private void updateValueForChild(DynamicAnimation.ViewProperty viewProperty, View view, float f) {
            SpringAnimation springAnimation;
            SpringForce spring;
            if (view == null || (springAnimation = (SpringAnimation) view.getTag(PhysicsAnimationLayout.this.getTagIdForProperty(viewProperty))) == null || (spring = springAnimation.getSpring()) == null) {
                return;
            }
            spring.setFinalPosition(f);
            springAnimation.start();
        }

        protected void startPathAnimation() {
            final SpringForce springForce = PhysicsAnimationLayout.this.mController.getSpringForce(DynamicAnimation.TRANSLATION_X, this.mView);
            final SpringForce springForce2 = PhysicsAnimationLayout.this.mController.getSpringForce(DynamicAnimation.TRANSLATION_Y, this.mView);
            long j = this.mStartDelay;
            if (j > 0) {
                this.mPathAnimator.setStartDelay(j);
            }
            final Runnable runnable = new Runnable() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsPropertyAnimator$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$startPathAnimation$3();
                }
            };
            this.mPathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout$PhysicsPropertyAnimator$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    runnable.run();
                }
            });
            this.mPathAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.5
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    float dampingRatio;
                    float dampingRatio2;
                    PhysicsPropertyAnimator physicsPropertyAnimator = PhysicsPropertyAnimator.this;
                    DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
                    View view = physicsPropertyAnimator.mView;
                    float f = PhysicsPropertyAnimator.this.mCurrentPointOnPath.x;
                    float f2 = PhysicsPropertyAnimator.this.mDefaultStartVelocity;
                    float stiffness = PhysicsPropertyAnimator.this.mStiffness >= 0.0f ? PhysicsPropertyAnimator.this.mStiffness : springForce.getStiffness();
                    if (PhysicsPropertyAnimator.this.mDampingRatio >= 0.0f) {
                        dampingRatio = PhysicsPropertyAnimator.this.mDampingRatio;
                    } else {
                        dampingRatio = springForce.getDampingRatio();
                    }
                    physicsPropertyAnimator.animateValueForChild(viewProperty, view, f, f2, 0L, stiffness, dampingRatio, new Runnable[0]);
                    PhysicsPropertyAnimator physicsPropertyAnimator2 = PhysicsPropertyAnimator.this;
                    DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
                    View view2 = physicsPropertyAnimator2.mView;
                    float f3 = PhysicsPropertyAnimator.this.mCurrentPointOnPath.y;
                    float f4 = PhysicsPropertyAnimator.this.mDefaultStartVelocity;
                    float stiffness2 = PhysicsPropertyAnimator.this.mStiffness >= 0.0f ? PhysicsPropertyAnimator.this.mStiffness : springForce2.getStiffness();
                    if (PhysicsPropertyAnimator.this.mDampingRatio >= 0.0f) {
                        dampingRatio2 = PhysicsPropertyAnimator.this.mDampingRatio;
                    } else {
                        dampingRatio2 = springForce2.getDampingRatio();
                    }
                    physicsPropertyAnimator2.animateValueForChild(viewProperty2, view2, f3, f4, 0L, stiffness2, dampingRatio2, new Runnable[0]);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    runnable.run();
                }
            });
            ObjectAnimator targetAnimatorFromView = PhysicsAnimationLayout.this.getTargetAnimatorFromView(this.mView);
            if (targetAnimatorFromView != null) {
                targetAnimatorFromView.cancel();
            }
            this.mView.setTag(R.id.target_animator_tag, this.mPathAnimator);
            this.mPathAnimator.start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$startPathAnimation$3() {
            updateValueForChild(DynamicAnimation.TRANSLATION_X, this.mView, this.mCurrentPointOnPath.x);
            updateValueForChild(DynamicAnimation.TRANSLATION_Y, this.mView, this.mCurrentPointOnPath.y);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void clearAnimator() {
            this.mInitialPropertyValues.clear();
            this.mAnimatedProperties.clear();
            this.mPositionStartVelocities.clear();
            this.mDefaultStartVelocity = -3.4028235E38f;
            this.mStartDelay = 0L;
            this.mStiffness = -1.0f;
            this.mDampingRatio = -1.0f;
            this.mEndActionsForProperty.clear();
            this.mPathAnimator = null;
            this.mPositionEndActions = null;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setAssociatedController(PhysicsAnimationController physicsAnimationController) {
            this.mAssociatedController = physicsAnimationController;
        }
    }
}
