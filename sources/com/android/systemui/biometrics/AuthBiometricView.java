package com.android.systemui.biometrics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;
import java.util.ArrayList;

/* loaded from: classes.dex */
public abstract class AuthBiometricView extends LinearLayout {
    private final AccessibilityManager mAccessibilityManager;
    protected ImageView mAppIcon;
    private final View.OnClickListener mBackgroundClickListener;
    private Bundle mBiometricPromptBundle;
    private Callback mCallback;
    private TextView mDescriptionView;
    protected boolean mDialogSizeAnimating;
    private int mEffectiveUserId;
    private final Handler mHandler;
    private float mIconOriginalY;
    protected ImageView mIconView;
    protected TextView mIndicatorView;
    private final Injector mInjector;
    private int mMediumHeight;
    private int mMediumWidth;

    @VisibleForTesting
    Button mNegativeButton;
    protected final PackageManager mPackageManager;
    private AuthPanelController mPanelController;

    @VisibleForTesting
    Button mPositiveButton;
    private boolean mRequireConfirmation;
    private final Runnable mResetErrorRunnable;
    private final Runnable mResetHelpRunnable;
    protected Bundle mSavedState;
    int mSize;
    protected int mState;
    private TextView mSubtitleView;
    protected final int mTextColorError;
    protected final int mTextColorHint;
    private TextView mTitleView;

    @VisibleForTesting
    Button mTryAgainButton;
    private int mUserId;

    interface Callback {
        void onAction(int i);
    }

    protected abstract int getDelayAfterAuthenticatedDurationMs();

    protected abstract int getDescriptionTextId();

    protected abstract int getStateForAfterError();

    protected abstract void handleResetAfterError();

    protected abstract void handleResetAfterHelp();

    protected abstract boolean supportsSmallDialog();

    @VisibleForTesting
    static class Injector {
        AuthBiometricView mBiometricView;

        public int getDelayAfterError() {
            return 2000;
        }

        public int getMediumToLargeAnimationDurationMs() {
            return 450;
        }

        Injector() {
        }

        public Button getNegativeButton() {
            return (Button) this.mBiometricView.findViewById(R.id.button_negative);
        }

        public Button getPositiveButton() {
            return (Button) this.mBiometricView.findViewById(R.id.button_positive);
        }

        public Button getTryAgainButton() {
            return (Button) this.mBiometricView.findViewById(R.id.button_try_again);
        }

        public TextView getTitleView() {
            return (TextView) this.mBiometricView.findViewById(R.id.title);
        }

        public TextView getSubtitleView() {
            return (TextView) this.mBiometricView.findViewById(R.id.subtitle);
        }

        public TextView getDescriptionView() {
            return (TextView) this.mBiometricView.findViewById(R.id.description);
        }

        public TextView getIndicatorView() {
            return (TextView) this.mBiometricView.findViewById(R.id.indicator);
        }

        public ImageView getIconView() {
            return (ImageView) this.mBiometricView.findViewById(R.id.biometric_icon);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        if (this.mState == 6) {
            Log.w("BiometricPrompt/AuthBiometricView", "Ignoring background click after authenticated");
            return;
        }
        int i = this.mSize;
        if (i == 1) {
            Log.w("BiometricPrompt/AuthBiometricView", "Ignoring background click during small dialog");
        } else if (i == 3) {
            Log.w("BiometricPrompt/AuthBiometricView", "Ignoring background click during large dialog");
        } else {
            this.mCallback.onAction(2);
        }
    }

    public AuthBiometricView(Context context) {
        this(context, null);
    }

    public AuthBiometricView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, new Injector());
    }

    @VisibleForTesting
    AuthBiometricView(Context context, AttributeSet attributeSet, Injector injector) {
        super(context, attributeSet);
        this.mSize = 0;
        this.mBackgroundClickListener = new View.OnClickListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mTextColorError = getResources().getColor(R.color.biometric_dialog_error, context.getTheme());
        this.mTextColorHint = getResources().getColor(R.color.biometric_dialog_gray, context.getTheme());
        this.mInjector = injector;
        injector.mBiometricView = this;
        this.mPackageManager = context.getPackageManager();
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mResetErrorRunnable = new Runnable() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.lambda$new$1();
            }
        };
        this.mResetHelpRunnable = new Runnable() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.lambda$new$2();
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1() throws Resources.NotFoundException {
        updateState(getStateForAfterError());
        handleResetAfterError();
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2() throws Resources.NotFoundException {
        updateState(2);
        handleResetAfterHelp();
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    public void setPanelController(AuthPanelController authPanelController) {
        this.mPanelController = authPanelController;
    }

    public void setBiometricPromptBundle(Bundle bundle) {
        this.mBiometricPromptBundle = bundle;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setBackgroundView(View view) {
        view.setOnClickListener(this.mBackgroundClickListener);
    }

    public void setUserId(int i) {
        this.mUserId = i;
    }

    public void setEffectiveUserId(int i) {
        this.mEffectiveUserId = i;
    }

    public void setRequireConfirmation(boolean z) {
        this.mRequireConfirmation = z;
    }

    @VisibleForTesting
    void updateSize(final int i) throws Resources.NotFoundException {
        Log.v("BiometricPrompt/AuthBiometricView", "Current size: " + this.mSize + " New size: " + i);
        if (i == 1) {
            this.mTitleView.setVisibility(8);
            this.mSubtitleView.setVisibility(8);
            this.mDescriptionView.setVisibility(8);
            this.mIndicatorView.setVisibility(8);
            this.mNegativeButton.setVisibility(8);
            float dimension = getResources().getDimension(R.dimen.biometric_dialog_icon_padding);
            this.mIconView.setY((getHeight() - this.mIconView.getHeight()) - dimension);
            this.mPanelController.updateForContentDimensions(this.mMediumWidth, ((this.mIconView.getHeight() + (((int) dimension) * 2)) - this.mIconView.getPaddingTop()) - this.mIconView.getPaddingBottom(), 0);
            this.mSize = i;
        } else if (this.mSize == 1 && i == 2) {
            if (this.mDialogSizeAnimating) {
                return;
            }
            this.mDialogSizeAnimating = true;
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mIconView.getY(), this.mIconOriginalY);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$updateSize$3(valueAnimator);
                }
            });
            ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$updateSize$4(valueAnimator);
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(150L);
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.biometrics.AuthBiometricView.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    AuthBiometricView.this.mTitleView.setVisibility(0);
                    AuthBiometricView.this.mIndicatorView.setVisibility(0);
                    AuthBiometricView.this.mNegativeButton.setVisibility(0);
                    AuthBiometricView.this.mTryAgainButton.setVisibility(0);
                    if (!TextUtils.isEmpty(AuthBiometricView.this.mSubtitleView.getText())) {
                        AuthBiometricView.this.mSubtitleView.setVisibility(0);
                    }
                    if (TextUtils.isEmpty(AuthBiometricView.this.mDescriptionView.getText())) {
                        return;
                    }
                    AuthBiometricView.this.mDescriptionView.setVisibility(0);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    AuthBiometricView authBiometricView = AuthBiometricView.this;
                    authBiometricView.mSize = i;
                    authBiometricView.mDialogSizeAnimating = false;
                    Utils.notifyAccessibilityContentChanged(authBiometricView.mAccessibilityManager, AuthBiometricView.this);
                }
            });
            animatorSet.play(valueAnimatorOfFloat).with(valueAnimatorOfFloat2);
            animatorSet.start();
            this.mPanelController.updateForContentDimensions(this.mMediumWidth, this.mMediumHeight, 150);
        } else if (i == 2) {
            this.mPanelController.updateForContentDimensions(this.mMediumWidth, this.mMediumHeight, 0);
            this.mSize = i;
        } else if (i == 3) {
            ValueAnimator valueAnimatorOfFloat3 = ValueAnimator.ofFloat(getY(), getY() - getResources().getDimension(R.dimen.biometric_dialog_medium_to_large_translation_offset));
            valueAnimatorOfFloat3.setDuration(this.mInjector.getMediumToLargeAnimationDurationMs());
            valueAnimatorOfFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AuthBiometricView.lambda$updateSize$5(this.f$0, valueAnimator);
                }
            });
            valueAnimatorOfFloat3.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.biometrics.AuthBiometricView.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (this.getParent() != null) {
                        ((ViewGroup) this.getParent()).removeView(this);
                    }
                    AuthBiometricView.this.mSize = i;
                }
            });
            ValueAnimator valueAnimatorOfFloat4 = ValueAnimator.ofFloat(1.0f, 0.0f);
            valueAnimatorOfFloat4.setDuration(this.mInjector.getMediumToLargeAnimationDurationMs() / 2);
            valueAnimatorOfFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AuthBiometricView.lambda$updateSize$6(this.f$0, valueAnimator);
                }
            });
            this.mPanelController.setUseFullScreen(true);
            AuthPanelController authPanelController = this.mPanelController;
            authPanelController.updateForContentDimensions(authPanelController.getContainerWidth(), this.mPanelController.getContainerHeight(), this.mInjector.getMediumToLargeAnimationDurationMs());
            AnimatorSet animatorSet2 = new AnimatorSet();
            ArrayList arrayList = new ArrayList();
            arrayList.add(valueAnimatorOfFloat3);
            arrayList.add(valueAnimatorOfFloat4);
            animatorSet2.playTogether(arrayList);
            animatorSet2.setDuration((this.mInjector.getMediumToLargeAnimationDurationMs() * 2) / 3);
            animatorSet2.start();
        } else {
            Log.e("BiometricPrompt/AuthBiometricView", "Unknown transition from: " + this.mSize + " to: " + i);
        }
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateSize$3(ValueAnimator valueAnimator) {
        this.mIconView.setY(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateSize$4(ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mTitleView.setAlpha(fFloatValue);
        this.mIndicatorView.setAlpha(fFloatValue);
        this.mNegativeButton.setAlpha(fFloatValue);
        this.mTryAgainButton.setAlpha(fFloatValue);
        if (!TextUtils.isEmpty(this.mSubtitleView.getText())) {
            this.mSubtitleView.setAlpha(fFloatValue);
        }
        if (TextUtils.isEmpty(this.mDescriptionView.getText())) {
            return;
        }
        this.mDescriptionView.setAlpha(fFloatValue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateSize$5(AuthBiometricView authBiometricView, ValueAnimator valueAnimator) {
        authBiometricView.setTranslationY(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateSize$6(AuthBiometricView authBiometricView, ValueAnimator valueAnimator) {
        authBiometricView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    void updateState(int i) throws Resources.NotFoundException {
        Log.v("BiometricPrompt/AuthBiometricView", "newState: " + i);
        if (i == 1 || i == 2) {
            removePendingAnimations();
            if (this.mRequireConfirmation) {
                this.mPositiveButton.setEnabled(false);
                this.mPositiveButton.setVisibility(0);
            }
        } else if (i != 4) {
            if (i == 5) {
                removePendingAnimations();
                Button button = this.mNegativeButton;
                int i2 = R.string.cancel;
                button.setText(i2);
                this.mNegativeButton.setContentDescription(getResources().getString(i2));
                this.mPositiveButton.setEnabled(true);
                this.mPositiveButton.setVisibility(0);
                this.mIndicatorView.setTextColor(this.mTextColorHint);
                this.mIndicatorView.setText(R.string.biometric_dialog_tap_confirm);
                this.mIndicatorView.setVisibility(0);
            } else if (i == 6) {
                if (this.mSize != 1) {
                    this.mPositiveButton.setVisibility(8);
                    this.mNegativeButton.setVisibility(8);
                    this.mIndicatorView.setVisibility(4);
                }
                announceForAccessibility(getResources().getString(R.string.biometric_dialog_authenticated));
                this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda8
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$updateState$7();
                    }
                }, getDelayAfterAuthenticatedDurationMs());
            } else {
                Log.w("BiometricPrompt/AuthBiometricView", "Unhandled state: " + i);
            }
        } else if (this.mSize == 1) {
            updateSize(2);
        }
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
        this.mState = i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateState$7() {
        Log.d("BiometricPrompt/AuthBiometricView", "Sending ACTION_AUTHENTICATED");
        this.mCallback.onAction(1);
    }

    public void onDialogAnimatedIn() throws Resources.NotFoundException {
        updateState(2);
    }

    public void onAuthenticationSucceeded() throws Resources.NotFoundException {
        removePendingAnimations();
        if (this.mRequireConfirmation) {
            updateState(5);
        } else {
            updateState(6);
        }
    }

    public void onAuthenticationFailed(String str) throws Resources.NotFoundException {
        showTemporaryMessage(str, this.mResetErrorRunnable);
        updateState(4);
    }

    public void onError(String str) throws Resources.NotFoundException {
        showTemporaryMessage(str, this.mResetErrorRunnable);
        updateState(4);
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onError$8();
            }
        }, this.mInjector.getDelayAfterError());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onError$8() {
        this.mCallback.onAction(5);
    }

    public void onHelp(String str) throws Resources.NotFoundException {
        if (this.mSize != 2) {
            Log.w("BiometricPrompt/AuthBiometricView", "Help received in size: " + this.mSize);
            return;
        }
        showTemporaryMessage(str, this.mResetHelpRunnable);
        updateState(3);
    }

    public void onSaveState(Bundle bundle) {
        bundle.putInt("try_agian_visibility", this.mTryAgainButton.getVisibility());
        bundle.putInt("state", this.mState);
        bundle.putString("indicator_string", this.mIndicatorView.getText().toString());
        bundle.putBoolean("error_is_temporary", this.mHandler.hasCallbacks(this.mResetErrorRunnable));
        bundle.putBoolean("hint_is_temporary", this.mHandler.hasCallbacks(this.mResetHelpRunnable));
        bundle.putInt("size", this.mSize);
    }

    public void restoreState(Bundle bundle) {
        this.mSavedState = bundle;
    }

    private void setTextOrHide(TextView textView, String str) {
        if (TextUtils.isEmpty(str)) {
            textView.setVisibility(8);
        } else {
            textView.setText(str);
        }
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    private void setText(TextView textView, String str) {
        textView.setText(str);
    }

    private void removePendingAnimations() {
        this.mHandler.removeCallbacks(this.mResetHelpRunnable);
        this.mHandler.removeCallbacks(this.mResetErrorRunnable);
    }

    private void showTemporaryMessage(String str, Runnable runnable) {
        removePendingAnimations();
        this.mIndicatorView.setText(str);
        this.mIndicatorView.setTextColor(this.mTextColorError);
        this.mIndicatorView.setVisibility(0);
        this.mHandler.postDelayed(runnable, 2000L);
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        onFinishInflateInternal();
    }

    @VisibleForTesting
    void onFinishInflateInternal() throws Resources.NotFoundException {
        this.mTitleView = this.mInjector.getTitleView();
        this.mSubtitleView = this.mInjector.getSubtitleView();
        this.mDescriptionView = this.mInjector.getDescriptionView();
        this.mIconView = this.mInjector.getIconView();
        this.mIndicatorView = this.mInjector.getIndicatorView();
        this.mNegativeButton = this.mInjector.getNegativeButton();
        this.mPositiveButton = this.mInjector.getPositiveButton();
        this.mTryAgainButton = this.mInjector.getTryAgainButton();
        this.mAppIcon = new ImageView(((LinearLayout) this).mContext);
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.applock_icon_dimension);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 17;
        layoutParams.topMargin = (-dimensionPixelSize) / 2;
        this.mAppIcon.setLayoutParams(layoutParams);
        this.mAppIcon.setVisibility(8);
        addView(this.mAppIcon, 0);
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws Resources.NotFoundException {
                this.f$0.lambda$onFinishInflateInternal$9(view);
            }
        });
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws Resources.NotFoundException {
                this.f$0.lambda$onFinishInflateInternal$10(view);
            }
        });
        this.mTryAgainButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.biometrics.AuthBiometricView$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws Resources.NotFoundException {
                this.f$0.lambda$onFinishInflateInternal$11(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflateInternal$9(View view) throws Resources.NotFoundException {
        if (this.mState == 5) {
            this.mCallback.onAction(2);
        } else if (isDeviceCredentialAllowed()) {
            startTransitionToCredentialUI();
        } else {
            this.mCallback.onAction(3);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflateInternal$10(View view) throws Resources.NotFoundException {
        updateState(6);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflateInternal$11(View view) throws Resources.NotFoundException {
        updateState(2);
        this.mCallback.onAction(4);
        this.mTryAgainButton.setVisibility(8);
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    void startTransitionToCredentialUI() throws Resources.NotFoundException {
        updateSize(3);
        this.mCallback.onAction(6);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() throws Resources.NotFoundException {
        super.onAttachedToWindow();
        onAttachedToWindowInternal();
    }

    @VisibleForTesting
    void onAttachedToWindowInternal() throws Resources.NotFoundException {
        String string;
        ApplicationInfo applicationInfoAsUser;
        if (isDeviceCredentialAllowed()) {
            int credentialType = Utils.getCredentialType(((LinearLayout) this).mContext, this.mEffectiveUserId);
            if (credentialType == 1) {
                string = getResources().getString(R.string.biometric_dialog_use_pin);
            } else if (credentialType == 2) {
                string = getResources().getString(R.string.biometric_dialog_use_pattern);
            } else if (credentialType == 3) {
                string = getResources().getString(R.string.biometric_dialog_use_password);
            } else {
                string = getResources().getString(R.string.biometric_dialog_use_password);
            }
        } else {
            string = this.mBiometricPromptBundle.getString("negative_text");
        }
        setText(this.mNegativeButton, string);
        setTextOrHide(this.mSubtitleView, this.mBiometricPromptBundle.getString("subtitle"));
        CharSequence charSequence = this.mBiometricPromptBundle.getCharSequence("applock_package_name");
        if (TextUtils.isEmpty(charSequence)) {
            setText(this.mTitleView, this.mBiometricPromptBundle.getString("title"));
            setTextOrHide(this.mDescriptionView, this.mBiometricPromptBundle.getString("description"));
        } else {
            try {
                applicationInfoAsUser = this.mPackageManager.getApplicationInfoAsUser(charSequence.toString(), 0, this.mUserId);
            } catch (PackageManager.NameNotFoundException unused) {
                applicationInfoAsUser = null;
            }
            Drawable applicationIcon = applicationInfoAsUser != null ? this.mPackageManager.getApplicationIcon(applicationInfoAsUser) : null;
            if (applicationIcon == null) {
                this.mTitleView.setVisibility(0);
                setText(this.mTitleView, getResources().getString(R.string.applock_unlock) + " " + this.mBiometricPromptBundle.getString("title"));
            } else {
                this.mTitleView.setVisibility(8);
                this.mAppIcon.setVisibility(0);
                this.mAppIcon.setImageDrawable(applicationIcon);
            }
            setTextOrHide(this.mDescriptionView, this.mBiometricPromptBundle.getString("description") + getResources().getString(R.string.applock_locked) + "\n" + string + getResources().getString(getDescriptionTextId()));
            this.mDescriptionView.setGravity(17);
        }
        Bundle bundle = this.mSavedState;
        if (bundle == null) {
            updateState(1);
        } else {
            updateState(bundle.getInt("state"));
            this.mTryAgainButton.setVisibility(this.mSavedState.getInt("try_agian_visibility"));
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        int iMin = Math.min(size, size2);
        int childCount = getChildCount();
        int measuredHeight = 0;
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getId() == R.id.biometric_icon) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(iMin, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE));
            } else if (childAt.getId() == R.id.button_bar) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(iMin, 1073741824), View.MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().height, 1073741824));
            } else if (childAt.equals(this.mAppIcon)) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().width, 1073741824), View.MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().height, 1073741824));
            } else {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(iMin, 1073741824), View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE));
            }
            if (childAt.getVisibility() != 8) {
                measuredHeight += childAt.equals(this.mAppIcon) ? childAt.getMeasuredHeight() / 2 : childAt.getMeasuredHeight();
            }
        }
        setMeasuredDimension(iMin, measuredHeight);
        this.mMediumHeight = measuredHeight;
        this.mMediumWidth = getMeasuredWidth();
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) throws Resources.NotFoundException {
        super.onLayout(z, i, i2, i3, i4);
        onLayoutInternal();
    }

    @VisibleForTesting
    void onLayoutInternal() throws Resources.NotFoundException {
        if (this.mIconOriginalY == 0.0f) {
            this.mIconOriginalY = this.mIconView.getY();
            Bundle bundle = this.mSavedState;
            if (bundle == null) {
                updateSize((this.mRequireConfirmation || !supportsSmallDialog()) ? 2 : 1);
                return;
            }
            updateSize(bundle.getInt("size"));
            String string = this.mSavedState.getString("indicator_string");
            if (this.mSavedState.getBoolean("hint_is_temporary")) {
                onHelp(string);
            } else if (this.mSavedState.getBoolean("error_is_temporary")) {
                onAuthenticationFailed(string);
            }
        }
    }

    private boolean isDeviceCredentialAllowed() {
        return Utils.isDeviceCredentialAllowed(this.mBiometricPromptBundle);
    }
}
