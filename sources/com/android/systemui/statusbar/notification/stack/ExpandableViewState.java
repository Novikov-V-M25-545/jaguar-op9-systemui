package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;

/* loaded from: classes.dex */
public class ExpandableViewState extends ViewState {
    private static final int TAG_ANIMATOR_HEIGHT = R.id.height_animator_tag;
    private static final int TAG_ANIMATOR_TOP_INSET = R.id.top_inset_animator_tag;
    private static final int TAG_END_HEIGHT = R.id.height_animator_end_value_tag;
    private static final int TAG_END_TOP_INSET = R.id.top_inset_animator_end_value_tag;
    private static final int TAG_START_HEIGHT = R.id.height_animator_start_value_tag;
    private static final int TAG_START_TOP_INSET = R.id.top_inset_animator_start_value_tag;
    public boolean belowSpeedBump;
    public int clipTopAmount;
    public boolean dimmed;
    public boolean headsUpIsVisible;
    public int height;
    public boolean hideSensitive;
    public boolean inShelf;
    public int location;
    public int notGoneIndex;

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void copyFrom(ViewState viewState) {
        super.copyFrom(viewState);
        if (viewState instanceof ExpandableViewState) {
            ExpandableViewState expandableViewState = (ExpandableViewState) viewState;
            this.height = expandableViewState.height;
            this.dimmed = expandableViewState.dimmed;
            this.hideSensitive = expandableViewState.hideSensitive;
            this.belowSpeedBump = expandableViewState.belowSpeedBump;
            this.clipTopAmount = expandableViewState.clipTopAmount;
            this.notGoneIndex = expandableViewState.notGoneIndex;
            this.location = expandableViewState.location;
            this.headsUpIsVisible = expandableViewState.headsUpIsVisible;
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void applyToView(View view) {
        super.applyToView(view);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            int actualHeight = expandableView.getActualHeight();
            int i = this.height;
            if (actualHeight != i) {
                expandableView.setActualHeight(i, false);
            }
            expandableView.setDimmed(this.dimmed, false);
            expandableView.setHideSensitive(this.hideSensitive, false, 0L, 0L);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
            float clipTopAmount = expandableView.getClipTopAmount();
            int i2 = this.clipTopAmount;
            if (clipTopAmount != i2) {
                expandableView.setClipTopAmount(i2);
            }
            expandableView.setTransformingInShelf(false);
            expandableView.setInShelf(this.inShelf);
            if (this.headsUpIsVisible) {
                expandableView.setHeadsUpIsVisible();
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void animateTo(View view, AnimationProperties animationProperties) {
        super.animateTo(view, animationProperties);
        if (view instanceof ExpandableView) {
            ExpandableView expandableView = (ExpandableView) view;
            AnimationFilter animationFilter = animationProperties.getAnimationFilter();
            if (this.height != expandableView.getActualHeight()) {
                startHeightAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, TAG_ANIMATOR_HEIGHT);
            }
            if (this.clipTopAmount != expandableView.getClipTopAmount()) {
                startInsetAnimation(expandableView, animationProperties);
            } else {
                abortAnimation(view, TAG_ANIMATOR_TOP_INSET);
            }
            expandableView.setDimmed(this.dimmed, animationFilter.animateDimmed);
            expandableView.setBelowSpeedBump(this.belowSpeedBump);
            expandableView.setHideSensitive(this.hideSensitive, animationFilter.animateHideSensitive, animationProperties.delay, animationProperties.duration);
            if (animationProperties.wasAdded(view) && !this.hidden) {
                expandableView.performAddAnimation(animationProperties.delay, animationProperties.duration, false);
            }
            if (!expandableView.isInShelf() && this.inShelf) {
                expandableView.setTransformingInShelf(true);
            }
            expandableView.setInShelf(this.inShelf);
            if (this.headsUpIsVisible) {
                expandableView.setHeadsUpIsVisible();
            }
        }
    }

    private void startHeightAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        int i = TAG_START_HEIGHT;
        Integer num = (Integer) ViewState.getChildTag(expandableView, i);
        int i2 = TAG_END_HEIGHT;
        Integer num2 = (Integer) ViewState.getChildTag(expandableView, i2);
        int i3 = this.height;
        if (num2 == null || num2.intValue() != i3) {
            int i4 = TAG_ANIMATOR_HEIGHT;
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, i4);
            if (!animationProperties.getAnimationFilter().animateHeight) {
                if (valueAnimator != null) {
                    PropertyValuesHolder[] values = valueAnimator.getValues();
                    int iIntValue = num.intValue() + (i3 - num2.intValue());
                    values[0].setIntValues(iIntValue, i3);
                    expandableView.setTag(i, Integer.valueOf(iIntValue));
                    expandableView.setTag(i2, Integer.valueOf(i3));
                    valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                    return;
                }
                expandableView.setActualHeight(i3, false);
                return;
            }
            ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(expandableView.getActualHeight(), i3);
            valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.stack.ExpandableViewState.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    expandableView.setActualHeight(((Integer) valueAnimator2.getAnimatedValue()).intValue(), false);
                }
            });
            valueAnimatorOfInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            valueAnimatorOfInt.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
            if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                valueAnimatorOfInt.setStartDelay(animationProperties.delay);
            }
            AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(null);
            if (animationFinishListener != null) {
                valueAnimatorOfInt.addListener(animationFinishListener);
            }
            valueAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.stack.ExpandableViewState.2
                boolean mWasCancelled;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    expandableView.setTag(ExpandableViewState.TAG_ANIMATOR_HEIGHT, null);
                    expandableView.setTag(ExpandableViewState.TAG_START_HEIGHT, null);
                    expandableView.setTag(ExpandableViewState.TAG_END_HEIGHT, null);
                    expandableView.setActualHeightAnimating(false);
                    if (this.mWasCancelled) {
                        return;
                    }
                    ExpandableView expandableView2 = expandableView;
                    if (expandableView2 instanceof ExpandableNotificationRow) {
                        ((ExpandableNotificationRow) expandableView2).setGroupExpansionChanging(false);
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mWasCancelled = false;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.mWasCancelled = true;
                }
            });
            ViewState.startAnimator(valueAnimatorOfInt, animationFinishListener);
            expandableView.setTag(i4, valueAnimatorOfInt);
            expandableView.setTag(i, Integer.valueOf(expandableView.getActualHeight()));
            expandableView.setTag(i2, Integer.valueOf(i3));
            expandableView.setActualHeightAnimating(true);
        }
    }

    private void startInsetAnimation(final ExpandableView expandableView, AnimationProperties animationProperties) {
        int i = TAG_START_TOP_INSET;
        Integer num = (Integer) ViewState.getChildTag(expandableView, i);
        int i2 = TAG_END_TOP_INSET;
        Integer num2 = (Integer) ViewState.getChildTag(expandableView, i2);
        int i3 = this.clipTopAmount;
        if (num2 == null || num2.intValue() != i3) {
            int i4 = TAG_ANIMATOR_TOP_INSET;
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(expandableView, i4);
            if (!animationProperties.getAnimationFilter().animateTopInset) {
                if (valueAnimator != null) {
                    PropertyValuesHolder[] values = valueAnimator.getValues();
                    int iIntValue = num.intValue() + (i3 - num2.intValue());
                    values[0].setIntValues(iIntValue, i3);
                    expandableView.setTag(i, Integer.valueOf(iIntValue));
                    expandableView.setTag(i2, Integer.valueOf(i3));
                    valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                    return;
                }
                expandableView.setClipTopAmount(i3);
                return;
            }
            ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(expandableView.getClipTopAmount(), i3);
            valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.stack.ExpandableViewState.3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    expandableView.setClipTopAmount(((Integer) valueAnimator2.getAnimatedValue()).intValue());
                }
            });
            valueAnimatorOfInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            valueAnimatorOfInt.setDuration(ViewState.cancelAnimatorAndGetNewDuration(animationProperties.duration, valueAnimator));
            if (animationProperties.delay > 0 && (valueAnimator == null || valueAnimator.getAnimatedFraction() == 0.0f)) {
                valueAnimatorOfInt.setStartDelay(animationProperties.delay);
            }
            AnimatorListenerAdapter animationFinishListener = animationProperties.getAnimationFinishListener(null);
            if (animationFinishListener != null) {
                valueAnimatorOfInt.addListener(animationFinishListener);
            }
            valueAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.stack.ExpandableViewState.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    expandableView.setTag(ExpandableViewState.TAG_ANIMATOR_TOP_INSET, null);
                    expandableView.setTag(ExpandableViewState.TAG_START_TOP_INSET, null);
                    expandableView.setTag(ExpandableViewState.TAG_END_TOP_INSET, null);
                }
            });
            ViewState.startAnimator(valueAnimatorOfInt, animationFinishListener);
            expandableView.setTag(i4, valueAnimatorOfInt);
            expandableView.setTag(i, Integer.valueOf(expandableView.getClipTopAmount()));
            expandableView.setTag(i2, Integer.valueOf(i3));
        }
    }

    public static int getFinalActualHeight(ExpandableView expandableView) {
        if (expandableView == null) {
            return 0;
        }
        if (((ValueAnimator) ViewState.getChildTag(expandableView, TAG_ANIMATOR_HEIGHT)) == null) {
            return expandableView.getActualHeight();
        }
        return ((Integer) ViewState.getChildTag(expandableView, TAG_END_HEIGHT)).intValue();
    }

    @Override // com.android.systemui.statusbar.notification.stack.ViewState
    public void cancelAnimations(View view) {
        super.cancelAnimations(view);
        Animator animator = (Animator) ViewState.getChildTag(view, TAG_ANIMATOR_HEIGHT);
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = (Animator) ViewState.getChildTag(view, TAG_ANIMATOR_TOP_INSET);
        if (animator2 != null) {
            animator2.cancel();
        }
    }
}
