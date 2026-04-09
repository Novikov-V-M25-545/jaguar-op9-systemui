package com.android.systemui.biometrics;

import android.R;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/* loaded from: classes.dex */
public class AuthBiometricFingerprintView extends AuthBiometricView {
    private final boolean mHasFod;

    private boolean shouldAnimateForTransition(int i, int i2) {
        return (i2 == 1 || i2 == 2) ? i == 4 || i == 3 : i2 == 3 || i2 == 4;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected int getDelayAfterAuthenticatedDurationMs() {
        return 0;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected int getStateForAfterError() {
        return 2;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected boolean supportsSmallDialog() {
        return false;
    }

    public AuthBiometricFingerprintView(Context context) {
        this(context, null);
    }

    public AuthBiometricFingerprintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        PackageManager packageManager = ((LinearLayout) this).mContext.getPackageManager();
        this.mHasFod = packageManager.hasSystemFeature("android.hardware.fingerprint") && packageManager.hasSystemFeature("vendor.lineage.biometrics.fingerprint.inscreen");
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected void handleResetAfterError() {
        showTouchSensorString();
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected void handleResetAfterHelp() {
        showTouchSensorString();
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView, android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        if (this.mHasFod) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.indeterminate_progress_alpha_40);
            int dimensionPixelSize2 = getResources().getDimensionPixelSize(com.android.systemui.R.dimen.biometric_dialog_fod_margin);
            this.mIconView.setVisibility(4);
            this.mIconView.setPadding(0, 0, 0, dimensionPixelSize2 - dimensionPixelSize);
            removeView(this.mIndicatorView);
            addView(this.mIndicatorView, indexOfChild(this.mIconView));
            return;
        }
        this.mIconView.setVisibility(0);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected int getDescriptionTextId() {
        return com.android.systemui.R.string.applock_fingerprint;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void updateState(int i) throws Resources.NotFoundException {
        updateIcon(this.mState, i);
        super.updateState(i);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    void onAttachedToWindowInternal() throws Resources.NotFoundException {
        super.onAttachedToWindowInternal();
        showTouchSensorString();
    }

    private void showTouchSensorString() {
        this.mIndicatorView.setText(com.android.systemui.R.string.fingerprint_dialog_touch_sensor);
        this.mIndicatorView.setTextColor(this.mTextColorHint);
    }

    private void updateIcon(int i, int i2) {
        Drawable animationForTransition = getAnimationForTransition(i, i2);
        if (animationForTransition == null) {
            Log.e("BiometricPrompt/AuthBiometricFingerprintView", "Animation not found, " + i + " -> " + i2);
            return;
        }
        AnimatedVectorDrawable animatedVectorDrawable = animationForTransition instanceof AnimatedVectorDrawable ? (AnimatedVectorDrawable) animationForTransition : null;
        this.mIconView.setImageDrawable(animationForTransition);
        if (animatedVectorDrawable == null || !shouldAnimateForTransition(i, i2)) {
            return;
        }
        animatedVectorDrawable.forceAnimationOnUI();
        animatedVectorDrawable.start();
    }

    private Drawable getAnimationForTransition(int i, int i2) {
        int i3;
        if (i2 == 1 || i2 == 2) {
            if (i == 4 || i == 3) {
                i3 = com.android.systemui.R.drawable.fingerprint_dialog_error_to_fp;
            } else {
                i3 = com.android.systemui.R.drawable.fingerprint_dialog_fp_to_error;
            }
        } else {
            if (i2 != 3 && i2 != 4 && i2 != 6) {
                return null;
            }
            i3 = com.android.systemui.R.drawable.fingerprint_dialog_fp_to_error;
        }
        return ((LinearLayout) this).mContext.getDrawable(i3);
    }
}
