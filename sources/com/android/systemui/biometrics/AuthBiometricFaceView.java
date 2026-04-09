package com.android.systemui.biometrics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class AuthBiometricFaceView extends AuthBiometricView {

    @VisibleForTesting
    IconController mIconController;

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected int getDelayAfterAuthenticatedDurationMs() {
        return 500;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected int getStateForAfterError() {
        return 0;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected boolean supportsSmallDialog() {
        return true;
    }

    public static class IconController extends Animatable2.AnimationCallback {
        Context mContext;
        Handler mHandler = new Handler(Looper.getMainLooper());
        ImageView mIconView;
        boolean mLastPulseLightToDark;
        int mState;
        TextView mTextView;

        IconController(Context context, ImageView imageView, TextView textView) {
            this.mContext = context;
            this.mIconView = imageView;
            this.mTextView = textView;
            showStaticDrawable(R.drawable.face_dialog_pulse_dark_to_light);
        }

        void animateOnce(int i) {
            animateIcon(i, false);
        }

        public void showStaticDrawable(int i) {
            this.mIconView.setImageDrawable(this.mContext.getDrawable(i));
        }

        void animateIcon(int i, boolean z) {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) this.mContext.getDrawable(i);
            this.mIconView.setImageDrawable(animatedVectorDrawable);
            animatedVectorDrawable.forceAnimationOnUI();
            if (z) {
                animatedVectorDrawable.registerAnimationCallback(this);
            }
            animatedVectorDrawable.start();
        }

        void startPulsing() {
            this.mLastPulseLightToDark = false;
            animateIcon(R.drawable.face_dialog_pulse_dark_to_light, true);
        }

        void pulseInNextDirection() {
            animateIcon(this.mLastPulseLightToDark ? R.drawable.face_dialog_pulse_dark_to_light : R.drawable.face_dialog_pulse_light_to_dark, true);
            this.mLastPulseLightToDark = !this.mLastPulseLightToDark;
        }

        @Override // android.graphics.drawable.Animatable2.AnimationCallback
        public void onAnimationEnd(Drawable drawable) {
            super.onAnimationEnd(drawable);
            int i = this.mState;
            if (i == 2 || i == 3) {
                pulseInNextDirection();
            }
        }

        public void updateState(int i, int i2) {
            boolean z = i == 4 || i == 3;
            if (i2 == 1) {
                showStaticDrawable(R.drawable.face_dialog_pulse_dark_to_light);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_authenticating));
            } else if (i2 == 2) {
                startPulsing();
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_authenticating));
            } else if (i == 5 && i2 == 6) {
                animateOnce(R.drawable.face_dialog_dark_to_checkmark);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_confirmed));
            } else if (z && i2 == 0) {
                animateOnce(R.drawable.face_dialog_error_to_idle);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_idle));
            } else if (z && i2 == 6) {
                animateOnce(R.drawable.face_dialog_dark_to_checkmark);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_authenticated));
            } else if (i2 == 4 && i != 4) {
                animateOnce(R.drawable.face_dialog_dark_to_error);
            } else if (i == 2 && i2 == 6) {
                animateOnce(R.drawable.face_dialog_dark_to_checkmark);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_authenticated));
            } else if (i2 == 5) {
                animateOnce(R.drawable.face_dialog_wink_from_dark);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_authenticated));
            } else if (i2 == 0) {
                showStaticDrawable(R.drawable.face_dialog_idle_static);
                this.mIconView.setContentDescription(this.mContext.getString(R.string.biometric_dialog_face_icon_description_idle));
            } else {
                Log.w("BiometricPrompt/AuthBiometricFaceView", "Unhandled state: " + i2);
            }
            this.mState = i2;
        }
    }

    public AuthBiometricFaceView(Context context) {
        this(context, null);
    }

    public AuthBiometricFaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected void handleResetAfterError() {
        resetErrorView();
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected void handleResetAfterHelp() {
        resetErrorView();
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    protected int getDescriptionTextId() {
        return R.string.applock_face;
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView, android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        this.mIconView.setVisibility(0);
        this.mIconController = new IconController(((LinearLayout) this).mContext, this.mIconView, this.mIndicatorView);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void updateState(int i) throws Resources.NotFoundException {
        this.mIconController.updateState(this.mState, i);
        this.mIconView.setVisibility(0);
        if (i == 1 || (i == 2 && this.mSize == 2)) {
            resetErrorView();
        }
        super.updateState(i);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onAuthenticationFailed(String str) throws Resources.NotFoundException {
        if (this.mSize == 2) {
            this.mTryAgainButton.setVisibility(0);
            this.mPositiveButton.setVisibility(8);
        }
        super.onAuthenticationFailed(str);
    }

    private void resetErrorView() {
        this.mIndicatorView.setTextColor(this.mTextColorHint);
        this.mIndicatorView.setVisibility(4);
    }
}
