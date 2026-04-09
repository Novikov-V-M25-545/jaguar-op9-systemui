package com.android.systemui.biometrics;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class AuthPanelController extends ViewOutlineProvider {
    private int mContainerHeight;
    private int mContainerWidth;
    private int mContentHeight;
    private int mContentWidth;
    private final Context mContext;
    private float mCornerRadius;
    private int mMargin;
    private final View mPanelView;
    private boolean mUseFullScreen;

    @Override // android.view.ViewOutlineProvider
    public void getOutline(View view, Outline outline) {
        int i;
        int i2 = this.mContainerWidth;
        int i3 = (i2 - this.mContentWidth) / 2;
        int i4 = i2 - i3;
        int i5 = this.mContentHeight;
        int i6 = this.mContainerHeight;
        if (i5 < i6) {
            i = (i6 - i5) - this.mMargin;
        } else {
            i = this.mMargin;
        }
        outline.setRoundRect(i3, i, i4, (i6 - this.mMargin) + 1, this.mCornerRadius);
    }

    public void setContainerDimensions(int i, int i2) {
        this.mContainerWidth = i;
        this.mContainerHeight = i2;
    }

    public void setUseFullScreen(boolean z) {
        this.mUseFullScreen = z;
    }

    public void updateForContentDimensions(int i, int i2, int i3) {
        if (this.mContainerWidth == 0 || this.mContainerHeight == 0) {
            Log.w("BiometricPrompt/AuthPanelController", "Not done measuring yet");
            return;
        }
        int dimension = this.mUseFullScreen ? 0 : (int) this.mContext.getResources().getDimension(R.dimen.biometric_dialog_border_padding);
        float dimension2 = this.mUseFullScreen ? 0.0f : this.mContext.getResources().getDimension(R.dimen.biometric_dialog_corner_size);
        if (i3 > 0) {
            ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(this.mMargin, dimension);
            valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthPanelController$$ExternalSyntheticLambda2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$updateForContentDimensions$2(valueAnimator);
                }
            });
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mCornerRadius, dimension2);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthPanelController$$ExternalSyntheticLambda3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$updateForContentDimensions$3(valueAnimator);
                }
            });
            ValueAnimator valueAnimatorOfInt2 = ValueAnimator.ofInt(this.mContentHeight, i2);
            valueAnimatorOfInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthPanelController$$ExternalSyntheticLambda1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$updateForContentDimensions$4(valueAnimator);
                }
            });
            ValueAnimator valueAnimatorOfInt3 = ValueAnimator.ofInt(this.mContentWidth, i);
            valueAnimatorOfInt3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthPanelController$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$updateForContentDimensions$5(valueAnimator);
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(i3);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(valueAnimatorOfFloat, valueAnimatorOfInt2, valueAnimatorOfInt3, valueAnimatorOfInt);
            animatorSet.start();
            return;
        }
        this.mMargin = dimension;
        this.mCornerRadius = dimension2;
        this.mContentWidth = i;
        this.mContentHeight = i2;
        this.mPanelView.invalidateOutline();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateForContentDimensions$2(ValueAnimator valueAnimator) {
        this.mMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateForContentDimensions$3(ValueAnimator valueAnimator) {
        this.mCornerRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateForContentDimensions$4(ValueAnimator valueAnimator) {
        this.mContentHeight = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        this.mPanelView.invalidateOutline();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateForContentDimensions$5(ValueAnimator valueAnimator) {
        this.mContentWidth = ((Integer) valueAnimator.getAnimatedValue()).intValue();
    }

    int getContainerWidth() {
        return this.mContainerWidth;
    }

    int getContainerHeight() {
        return this.mContainerHeight;
    }

    AuthPanelController(Context context, View view) {
        this.mContext = context;
        this.mPanelView = view;
        this.mCornerRadius = context.getResources().getDimension(R.dimen.biometric_dialog_corner_size);
        this.mMargin = (int) context.getResources().getDimension(R.dimen.biometric_dialog_border_padding);
        view.setOutlineProvider(this);
        view.setClipToOutline(true);
    }
}
