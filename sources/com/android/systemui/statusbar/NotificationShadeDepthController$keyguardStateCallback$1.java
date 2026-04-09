package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationShadeDepthController.kt */
/* loaded from: classes.dex */
public final class NotificationShadeDepthController$keyguardStateCallback$1 implements KeyguardStateController.Callback {
    final /* synthetic */ NotificationShadeDepthController this$0;

    NotificationShadeDepthController$keyguardStateCallback$1(NotificationShadeDepthController notificationShadeDepthController) {
        this.this$0 = notificationShadeDepthController;
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardFadingAwayChanged() {
        if (this.this$0.keyguardStateController.isKeyguardFadingAway() && this.this$0.biometricUnlockController.getMode() == 1) {
            Animator animator = this.this$0.keyguardAnimator;
            if (animator != null) {
                animator.cancel();
            }
            NotificationShadeDepthController notificationShadeDepthController = this.this$0;
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
            valueAnimatorOfFloat.setDuration(this.this$0.dozeParameters.getWallpaperFadeOutDuration());
            valueAnimatorOfFloat.setStartDelay(this.this$0.keyguardStateController.getKeyguardFadingAwayDelay());
            valueAnimatorOfFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.NotificationShadeDepthController$keyguardStateCallback$1$onKeyguardFadingAwayChanged$$inlined$apply$lambda$1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(@NotNull ValueAnimator animation) {
                    Intrinsics.checkParameterIsNotNull(animation, "animation");
                    NotificationShadeDepthController notificationShadeDepthController2 = this.this$0.this$0;
                    BlurUtils blurUtils = notificationShadeDepthController2.blurUtils;
                    Object animatedValue = animation.getAnimatedValue();
                    if (animatedValue == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
                    }
                    notificationShadeDepthController2.setWakeAndUnlockBlurRadius(blurUtils.blurRadiusOfRatio(((Float) animatedValue).floatValue()));
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.NotificationShadeDepthController$keyguardStateCallback$1$onKeyguardFadingAwayChanged$$inlined$apply$lambda$2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(@Nullable Animator animator2) {
                    this.this$0.this$0.keyguardAnimator = null;
                    NotificationShadeDepthController.scheduleUpdate$default(this.this$0.this$0, null, 1, null);
                }
            });
            valueAnimatorOfFloat.start();
            notificationShadeDepthController.keyguardAnimator = valueAnimatorOfFloat;
        }
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        if (this.this$0.keyguardStateController.isShowing()) {
            Animator animator = this.this$0.keyguardAnimator;
            if (animator != null) {
                animator.cancel();
            }
            Animator animator2 = this.this$0.notificationAnimator;
            if (animator2 != null) {
                animator2.cancel();
            }
        }
    }
}
