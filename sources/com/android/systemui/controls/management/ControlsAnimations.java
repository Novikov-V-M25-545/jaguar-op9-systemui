package com.android.systemui.controls.management;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsAnimations.kt */
/* loaded from: classes.dex */
public final class ControlsAnimations {
    public static final ControlsAnimations INSTANCE = new ControlsAnimations();
    private static float translationY = -1.0f;

    private ControlsAnimations() {
    }

    public static final /* synthetic */ float access$getTranslationY$p(ControlsAnimations controlsAnimations) {
        return translationY;
    }

    @NotNull
    public final LifecycleObserver observerForAnimations(@NotNull ViewGroup view, @NotNull Window window, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(window, "window");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        return new LifecycleObserver(window, view, intent) { // from class: com.android.systemui.controls.management.ControlsAnimations.observerForAnimations.1
            final /* synthetic */ Intent $intent;
            final /* synthetic */ ViewGroup $view;
            final /* synthetic */ Window $window;
            private boolean showAnimation;

            {
                this.$view = view;
                this.$intent = intent;
                this.showAnimation = intent.getBooleanExtra("extra_animate", false);
                view.setTransitionGroup(true);
                view.setTransitionAlpha(0.0f);
                if (ControlsAnimations.access$getTranslationY$p(ControlsAnimations.INSTANCE) == -1.0f) {
                    Intrinsics.checkExpressionValueIsNotNull(view.getContext(), "view.context");
                    ControlsAnimations.translationY = r1.getResources().getDimensionPixelSize(R.dimen.global_actions_controls_y_translation);
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public final void setup() {
                Window window2 = this.$window;
                window2.setAllowEnterTransitionOverlap(true);
                ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
                window2.setEnterTransition(controlsAnimations.enterWindowTransition(this.$view.getId()));
                window2.setExitTransition(controlsAnimations.exitWindowTransition(this.$view.getId()));
                window2.setReenterTransition(controlsAnimations.enterWindowTransition(this.$view.getId()));
                window2.setReturnTransition(controlsAnimations.exitWindowTransition(this.$view.getId()));
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public final void enterAnimation() {
                if (this.showAnimation) {
                    ControlsAnimations.INSTANCE.enterAnimation(this.$view).start();
                    this.showAnimation = false;
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public final void resetAnimation() {
                this.$view.setTranslationY(0.0f);
            }
        };
    }

    @NotNull
    public final Animator enterAnimation(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Log.d("ControlsUiController", "Enter animation for " + view);
        view.setTransitionAlpha(0.0f);
        view.setAlpha(1.0f);
        view.setTranslationY(translationY);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, "transitionAlpha", 0.0f, 1.0f);
        Interpolator interpolator = Interpolators.DECELERATE_QUINT;
        objectAnimatorOfFloat.setInterpolator(interpolator);
        objectAnimatorOfFloat.setStartDelay(167L);
        objectAnimatorOfFloat.setDuration(183L);
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(view, "translationY", 0.0f);
        objectAnimatorOfFloat2.setInterpolator(interpolator);
        objectAnimatorOfFloat2.setStartDelay(217L);
        objectAnimatorOfFloat2.setDuration(217L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2);
        return animatorSet;
    }

    public static /* synthetic */ Animator exitAnimation$default(View view, Runnable runnable, int i, Object obj) {
        if ((i & 2) != 0) {
            runnable = null;
        }
        return exitAnimation(view, runnable);
    }

    @NotNull
    public static final Animator exitAnimation(@NotNull View view, @Nullable final Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Log.d("ControlsUiController", "Exit animation for " + view);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, "transitionAlpha", 0.0f);
        Interpolator interpolator = Interpolators.ACCELERATE;
        objectAnimatorOfFloat.setInterpolator(interpolator);
        objectAnimatorOfFloat.setDuration(167L);
        view.setTranslationY(0.0f);
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(view, "translationY", -translationY);
        objectAnimatorOfFloat2.setInterpolator(interpolator);
        objectAnimatorOfFloat2.setDuration(183L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2);
        if (runnable != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.management.ControlsAnimations$exitAnimation$1$1$1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(@NotNull Animator animation) {
                    Intrinsics.checkParameterIsNotNull(animation, "animation");
                    runnable.run();
                }
            });
        }
        return animatorSet;
    }

    @NotNull
    public final WindowTransition enterWindowTransition(int i) {
        WindowTransition windowTransition = new WindowTransition(new Function1<View, Animator>() { // from class: com.android.systemui.controls.management.ControlsAnimations.enterWindowTransition.1
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final Animator invoke(@NotNull View view) {
                Intrinsics.checkParameterIsNotNull(view, "view");
                return ControlsAnimations.INSTANCE.enterAnimation(view);
            }
        });
        windowTransition.addTarget(i);
        return windowTransition;
    }

    @NotNull
    public final WindowTransition exitWindowTransition(int i) {
        WindowTransition windowTransition = new WindowTransition(new Function1<View, Animator>() { // from class: com.android.systemui.controls.management.ControlsAnimations.exitWindowTransition.1
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final Animator invoke(@NotNull View view) {
                Intrinsics.checkParameterIsNotNull(view, "view");
                return ControlsAnimations.exitAnimation$default(view, null, 2, null);
            }
        });
        windowTransition.addTarget(i);
        return windowTransition;
    }
}
