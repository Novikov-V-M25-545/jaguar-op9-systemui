package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.util.MathUtils;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.CornerHandleView;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.NavigationBarTransitions;

/* loaded from: classes.dex */
public class AssistHandleViewController implements NavigationBarTransitions.DarkIntensityListener {

    @VisibleForTesting
    boolean mAssistHintBlocked = false;
    private CornerHandleView mAssistHintLeft;
    private CornerHandleView mAssistHintRight;

    @VisibleForTesting
    boolean mAssistHintVisible;
    private int mBottomOffset;
    private Handler mHandler;

    public AssistHandleViewController(Handler handler, View view) {
        this.mHandler = handler;
        this.mAssistHintLeft = (CornerHandleView) view.findViewById(R.id.assist_hint_left);
        this.mAssistHintRight = (CornerHandleView) view.findViewById(R.id.assist_hint_right);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarTransitions.DarkIntensityListener
    public void onDarkIntensity(float f) {
        this.mAssistHintLeft.updateDarkness(f);
        this.mAssistHintRight.updateDarkness(f);
    }

    public void setBottomOffset(int i) {
        if (this.mBottomOffset != i) {
            this.mBottomOffset = i;
            if (this.mAssistHintVisible) {
                hideAssistHandles();
                lambda$setAssistHintVisible$0(true);
            }
        }
    }

    /* renamed from: setAssistHintVisible, reason: merged with bridge method [inline-methods] */
    public void lambda$setAssistHintVisible$0(final boolean z) {
        if (!this.mHandler.getLooper().isCurrentThread()) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.assist.AssistHandleViewController$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$setAssistHintVisible$0(z);
                }
            });
            return;
        }
        if ((this.mAssistHintBlocked && z) || this.mAssistHintVisible == z) {
            return;
        }
        this.mAssistHintVisible = z;
        fade(this.mAssistHintLeft, z, true);
        fade(this.mAssistHintRight, this.mAssistHintVisible, false);
    }

    /* renamed from: setAssistHintBlocked, reason: merged with bridge method [inline-methods] */
    public void lambda$setAssistHintBlocked$1(final boolean z) {
        if (!this.mHandler.getLooper().isCurrentThread()) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.assist.AssistHandleViewController$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$setAssistHintBlocked$1(z);
                }
            });
            return;
        }
        this.mAssistHintBlocked = z;
        if (this.mAssistHintVisible && z) {
            hideAssistHandles();
        }
    }

    private void hideAssistHandles() {
        this.mAssistHintLeft.setVisibility(8);
        this.mAssistHintRight.setVisibility(8);
        this.mAssistHintVisible = false;
    }

    Animator getHandleAnimator(View view, float f, float f2, boolean z, long j, Interpolator interpolator) {
        float fLerp = MathUtils.lerp(2.0f, 1.0f, f);
        float fLerp2 = MathUtils.lerp(2.0f, 1.0f, f2);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.SCALE_X, fLerp, fLerp2);
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.SCALE_Y, fLerp, fLerp2);
        float fLerp3 = MathUtils.lerp(0.2f, 0.0f, f);
        float fLerp4 = MathUtils.lerp(0.2f, 0.0f, f2);
        float f3 = z ? -1 : 1;
        ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.TRANSLATION_X, f3 * fLerp3 * view.getWidth(), f3 * fLerp4 * view.getWidth());
        ObjectAnimator objectAnimatorOfFloat4 = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.TRANSLATION_Y, (fLerp3 * view.getHeight()) + this.mBottomOffset, (fLerp4 * view.getHeight()) + this.mBottomOffset);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimatorOfFloat).with(objectAnimatorOfFloat2);
        animatorSet.play(objectAnimatorOfFloat).with(objectAnimatorOfFloat3);
        animatorSet.play(objectAnimatorOfFloat).with(objectAnimatorOfFloat4);
        animatorSet.setDuration(j);
        animatorSet.setInterpolator(interpolator);
        return animatorSet;
    }

    private void fade(View view, boolean z, boolean z2) {
        if (z) {
            view.animate().cancel();
            view.setAlpha(1.0f);
            view.setVisibility(0);
            AnimatorSet animatorSet = new AnimatorSet();
            Animator handleAnimator = getHandleAnimator(view, 0.0f, 1.1f, z2, 750L, new PathInterpolator(0.0f, 0.45f, 0.67f, 1.0f));
            PathInterpolator pathInterpolator = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);
            Animator handleAnimator2 = getHandleAnimator(view, 1.1f, 0.97f, z2, 400L, pathInterpolator);
            Animator handleAnimator3 = getHandleAnimator(view, 0.97f, 1.02f, z2, 400L, pathInterpolator);
            Animator handleAnimator4 = getHandleAnimator(view, 1.02f, 1.0f, z2, 400L, pathInterpolator);
            animatorSet.play(handleAnimator).before(handleAnimator2);
            animatorSet.play(handleAnimator2).before(handleAnimator3);
            animatorSet.play(handleAnimator3).before(handleAnimator4);
            animatorSet.start();
            return;
        }
        view.animate().cancel();
        view.animate().setInterpolator(new AccelerateInterpolator(1.5f)).setDuration(250L).alpha(0.0f);
    }
}
