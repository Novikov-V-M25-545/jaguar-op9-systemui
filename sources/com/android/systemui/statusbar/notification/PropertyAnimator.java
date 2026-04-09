package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;

/* loaded from: classes.dex */
public class PropertyAnimator {
    public static <T extends View> void setProperty(T t, AnimatableProperty animatableProperty, float f, AnimationProperties animationProperties, boolean z) {
        if (((ValueAnimator) ViewState.getChildTag(t, animatableProperty.getAnimatorTag())) != null || z) {
            if (!z) {
                animationProperties = null;
            }
            startAnimation(t, animatableProperty, f, animationProperties);
            return;
        }
        animatableProperty.getProperty().set(t, Float.valueOf(f));
    }

    public static <T extends View> void startAnimation(final T t, AnimatableProperty animatableProperty, float f, AnimationProperties animationProperties) {
        final Property property = animatableProperty.getProperty();
        final int animationStartTag = animatableProperty.getAnimationStartTag();
        final int animationEndTag = animatableProperty.getAnimationEndTag();
        Float f2 = (Float) ViewState.getChildTag(t, animationStartTag);
        Float f3 = (Float) ViewState.getChildTag(t, animationEndTag);
        if (f3 == null || f3.floatValue() != f) {
            final int animatorTag = animatableProperty.getAnimatorTag();
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(t, animatorTag);
            AnimationFilter animationFilter = animationProperties != null ? animationProperties.getAnimationFilter() : null;
            if (animationFilter == null || !animationFilter.shouldAnimateProperty(property)) {
                if (valueAnimator != null) {
                    PropertyValuesHolder[] values = valueAnimator.getValues();
                    float fFloatValue = f2.floatValue() + (f - f3.floatValue());
                    values[0].setFloatValues(fFloatValue, f);
                    t.setTag(animationStartTag, Float.valueOf(fFloatValue));
                    t.setTag(animationEndTag, Float.valueOf(f));
                    valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                    return;
                }
                property.set(t, Float.valueOf(f));
                return;
            }
            Float f4 = (Float) property.get(t);
            AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(property);
            if (f4.equals(Float.valueOf(f))) {
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                if (animationFinishListener != null) {
                    animationFinishListener.onAnimationEnd(null);
                    return;
                }
                return;
            }
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(f4.floatValue(), f);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.PropertyAnimator$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    PropertyAnimator.lambda$startAnimation$0(property, t, valueAnimator2);
                }
            });
            Interpolator customInterpolator = animationProperties.getCustomInterpolator(t, property);
            if (customInterpolator == null) {
                customInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            }
            valueAnimatorOfFloat.setInterpolator(customInterpolator);
            valueAnimatorOfFloat.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
            if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                valueAnimatorOfFloat.setStartDelay(animationProperties.delay);
            }
            if (animationFinishListener != null) {
                valueAnimatorOfFloat.addListener(animationFinishListener);
            }
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.PropertyAnimator.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    t.setTag(animatorTag, null);
                    t.setTag(animationStartTag, null);
                    t.setTag(animationEndTag, null);
                }
            });
            ViewState.startAnimator(valueAnimatorOfFloat, animationFinishListener);
            t.setTag(animatorTag, valueAnimatorOfFloat);
            t.setTag(animationStartTag, f4);
            t.setTag(animationEndTag, Float.valueOf(f));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$startAnimation$0(Property property, View view, ValueAnimator valueAnimator) {
        property.set(view, (Float) valueAnimator.getAnimatedValue());
    }

    public static <T extends View> void applyImmediately(T t, AnimatableProperty animatableProperty, float f) {
        cancelAnimation(t, animatableProperty);
        animatableProperty.getProperty().set(t, Float.valueOf(f));
    }

    public static void cancelAnimation(View view, AnimatableProperty animatableProperty) {
        ValueAnimator valueAnimator = (ValueAnimator) view.getTag(animatableProperty.getAnimatorTag());
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}
