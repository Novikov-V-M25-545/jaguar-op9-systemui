package com.android.systemui.biometrics;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

/* loaded from: classes.dex */
public abstract class AuthCredentialView extends LinearLayout {
    private final AccessibilityManager mAccessibilityManager;
    private Bundle mBiometricPromptBundle;
    protected Callback mCallback;
    protected final Runnable mClearErrorRunnable;
    protected AuthContainerView mContainerView;
    protected int mCredentialType;
    private TextView mDescriptionView;
    private final DevicePolicyManager mDevicePolicyManager;
    protected int mEffectiveUserId;
    protected ErrorTimer mErrorTimer;
    protected TextView mErrorView;
    protected final Handler mHandler;
    private ImageView mIconView;
    protected final LockPatternUtils mLockPatternUtils;
    protected long mOperationId;
    private AuthPanelController mPanelController;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    private boolean mShouldAnimateContents;
    private boolean mShouldAnimatePanel;
    private TextView mSubtitleView;
    private TextView mTitleView;
    protected int mUserId;
    private final UserManager mUserManager;

    interface Callback {
        void onCredentialMatched(byte[] bArr);
    }

    protected void onErrorTimeoutFinish() {
    }

    protected static class ErrorTimer extends CountDownTimer {
        private final Context mContext;
        private final TextView mErrorView;

        public ErrorTimer(Context context, long j, long j2, TextView textView) {
            super(j, j2);
            this.mErrorView = textView;
            this.mContext = context;
        }

        @Override // android.os.CountDownTimer
        public void onTick(long j) {
            this.mErrorView.setText(this.mContext.getString(R.string.biometric_dialog_credential_too_many_attempts, Integer.valueOf((int) (j / 1000))));
        }
    }

    public AuthCredentialView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClearErrorRunnable = new Runnable() { // from class: com.android.systemui.biometrics.AuthCredentialView.1
            @Override // java.lang.Runnable
            public void run() {
                TextView textView = AuthCredentialView.this.mErrorView;
                if (textView != null) {
                    textView.setText("");
                }
            }
        };
        this.mLockPatternUtils = new LockPatternUtils(((LinearLayout) this).mContext);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mAccessibilityManager = (AccessibilityManager) ((LinearLayout) this).mContext.getSystemService(AccessibilityManager.class);
        this.mUserManager = (UserManager) ((LinearLayout) this).mContext.getSystemService(UserManager.class);
        this.mDevicePolicyManager = (DevicePolicyManager) ((LinearLayout) this).mContext.getSystemService(DevicePolicyManager.class);
    }

    protected void showError(String str) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeCallbacks(this.mClearErrorRunnable);
            this.mHandler.postDelayed(this.mClearErrorRunnable, 3000L);
        }
        TextView textView = this.mErrorView;
        if (textView != null) {
            textView.setText(str);
        }
    }

    private void setTextOrHide(TextView textView, CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            textView.setVisibility(8);
        } else {
            textView.setText(charSequence);
        }
        Utils.notifyAccessibilityContentChanged(this.mAccessibilityManager, this);
    }

    private void setText(TextView textView, CharSequence charSequence) {
        textView.setText(charSequence);
    }

    void setUserId(int i) {
        this.mUserId = i;
    }

    void setOperationId(long j) {
        this.mOperationId = j;
    }

    void setEffectiveUserId(int i) {
        this.mEffectiveUserId = i;
    }

    void setCredentialType(int i) {
        this.mCredentialType = i;
    }

    void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    void setBiometricPromptBundle(Bundle bundle) {
        this.mBiometricPromptBundle = bundle;
    }

    void setPanelController(AuthPanelController authPanelController, boolean z) {
        this.mPanelController = authPanelController;
        this.mShouldAnimatePanel = z;
    }

    void setShouldAnimateContents(boolean z) {
        this.mShouldAnimateContents = z;
    }

    void setContainerView(AuthContainerView authContainerView) {
        this.mContainerView = authContainerView;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() throws Resources.NotFoundException {
        String string;
        Drawable drawable;
        super.onAttachedToWindow();
        CharSequence title = getTitle(this.mBiometricPromptBundle);
        setTextOrHide(this.mSubtitleView, getSubtitle(this.mBiometricPromptBundle));
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
        if (TextUtils.isEmpty(this.mBiometricPromptBundle.getCharSequence("applock_package_name"))) {
            setText(this.mTitleView, title);
            setTextOrHide(this.mDescriptionView, getDescription(this.mBiometricPromptBundle));
        } else {
            setText(this.mTitleView, ((Object) title) + getResources().getString(R.string.applock_locked));
            setTextOrHide(this.mDescriptionView, string + getResources().getString(R.string.applock_credential));
        }
        announceForAccessibility(title);
        if (this.mIconView != null) {
            if (Utils.isManagedProfile(((LinearLayout) this).mContext, this.mEffectiveUserId)) {
                drawable = getResources().getDrawable(R.drawable.auth_dialog_enterprise, ((LinearLayout) this).mContext.getTheme());
            } else {
                drawable = getResources().getDrawable(R.drawable.auth_dialog_lock, ((LinearLayout) this).mContext.getTheme());
            }
            this.mIconView.setImageDrawable(drawable);
        }
        if (this.mShouldAnimateContents) {
            setTranslationY(getResources().getDimension(R.dimen.biometric_dialog_credential_translation_offset));
            setAlpha(0.0f);
            postOnAnimation(new Runnable() { // from class: com.android.systemui.biometrics.AuthCredentialView$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onAttachedToWindow$0();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onAttachedToWindow$0() {
        animate().translationY(0.0f).setDuration(150L).alpha(1.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).withLayer().start();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ErrorTimer errorTimer = this.mErrorTimer;
        if (errorTimer != null) {
            errorTimer.cancel();
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleView = (TextView) findViewById(R.id.title);
        this.mSubtitleView = (TextView) findViewById(R.id.subtitle);
        this.mDescriptionView = (TextView) findViewById(R.id.description);
        this.mIconView = (ImageView) findViewById(R.id.icon);
        this.mErrorView = (TextView) findViewById(R.id.error);
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mShouldAnimatePanel) {
            this.mPanelController.setUseFullScreen(true);
            AuthPanelController authPanelController = this.mPanelController;
            authPanelController.updateForContentDimensions(authPanelController.getContainerWidth(), this.mPanelController.getContainerHeight(), 0);
            this.mShouldAnimatePanel = false;
        }
    }

    protected void onCredentialVerified(byte[] bArr, int i) {
        int i2;
        if (bArr != null) {
            this.mClearErrorRunnable.run();
            this.mLockPatternUtils.userPresent(this.mEffectiveUserId);
            this.mCallback.onCredentialMatched(bArr);
            return;
        }
        if (i > 0) {
            this.mHandler.removeCallbacks(this.mClearErrorRunnable);
            ErrorTimer errorTimer = new ErrorTimer(((LinearLayout) this).mContext, this.mLockPatternUtils.setLockoutAttemptDeadline(this.mEffectiveUserId, i) - SystemClock.elapsedRealtime(), 1000L, this.mErrorView) { // from class: com.android.systemui.biometrics.AuthCredentialView.2
                @Override // android.os.CountDownTimer
                public void onFinish() {
                    AuthCredentialView.this.onErrorTimeoutFinish();
                    AuthCredentialView.this.mClearErrorRunnable.run();
                }
            };
            this.mErrorTimer = errorTimer;
            errorTimer.start();
            return;
        }
        if (reportFailedAttempt()) {
            return;
        }
        int i3 = this.mCredentialType;
        if (i3 == 1) {
            i2 = R.string.biometric_dialog_wrong_pin;
        } else if (i3 == 2) {
            i2 = R.string.biometric_dialog_wrong_pattern;
        } else {
            i2 = R.string.biometric_dialog_wrong_password;
        }
        showError(getResources().getString(i2));
    }

    private boolean reportFailedAttempt() {
        boolean zUpdateErrorMessage = updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId) + 1);
        this.mLockPatternUtils.reportFailedPasswordAttempt(this.mEffectiveUserId);
        return zUpdateErrorMessage;
    }

    private boolean updateErrorMessage(int i) {
        int maximumFailedPasswordsForWipe = this.mLockPatternUtils.getMaximumFailedPasswordsForWipe(this.mEffectiveUserId);
        if (maximumFailedPasswordsForWipe <= 0 || i <= 0) {
            return false;
        }
        if (this.mErrorView != null) {
            showError(getResources().getString(R.string.biometric_dialog_credential_attempts_before_wipe, Integer.valueOf(i), Integer.valueOf(maximumFailedPasswordsForWipe)));
        }
        int i2 = maximumFailedPasswordsForWipe - i;
        if (i2 == 1) {
            showLastAttemptBeforeWipeDialog();
        } else if (i2 <= 0) {
            showNowWipingDialog();
        }
        return true;
    }

    private void showLastAttemptBeforeWipeDialog() {
        AlertDialog alertDialogCreate = new AlertDialog.Builder(((LinearLayout) this).mContext).setTitle(R.string.biometric_dialog_last_attempt_before_wipe_dialog_title).setMessage(getLastAttemptBeforeWipeMessageRes(getUserTypeForWipe(), this.mCredentialType)).setPositiveButton(android.R.string.ok, (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.getWindow().setType(2017);
        alertDialogCreate.show();
    }

    private void showNowWipingDialog() {
        AlertDialog alertDialogCreate = new AlertDialog.Builder(((LinearLayout) this).mContext).setMessage(getNowWipingMessageRes(getUserTypeForWipe())).setPositiveButton(R.string.biometric_dialog_now_wiping_dialog_dismiss, (DialogInterface.OnClickListener) null).setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.biometrics.AuthCredentialView$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                this.f$0.lambda$showNowWipingDialog$1(dialogInterface);
            }
        }).create();
        alertDialogCreate.getWindow().setType(2017);
        alertDialogCreate.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showNowWipingDialog$1(DialogInterface dialogInterface) {
        this.mContainerView.animateAway(5);
    }

    private int getUserTypeForWipe() {
        UserInfo userInfo = this.mUserManager.getUserInfo(this.mDevicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(this.mEffectiveUserId));
        if (userInfo == null || userInfo.isPrimary()) {
            return 1;
        }
        return userInfo.isManagedProfile() ? 2 : 3;
    }

    private static int getLastAttemptBeforeWipeMessageRes(int i, int i2) {
        if (i == 1) {
            return getLastAttemptBeforeWipeDeviceMessageRes(i2);
        }
        if (i == 2) {
            return getLastAttemptBeforeWipeProfileMessageRes(i2);
        }
        if (i == 3) {
            return getLastAttemptBeforeWipeUserMessageRes(i2);
        }
        throw new IllegalArgumentException("Unrecognized user type:" + i);
    }

    private static int getLastAttemptBeforeWipeDeviceMessageRes(int i) {
        if (i == 1) {
            return R.string.biometric_dialog_last_pin_attempt_before_wipe_device;
        }
        if (i == 2) {
            return R.string.biometric_dialog_last_pattern_attempt_before_wipe_device;
        }
        return R.string.biometric_dialog_last_password_attempt_before_wipe_device;
    }

    private static int getLastAttemptBeforeWipeProfileMessageRes(int i) {
        if (i == 1) {
            return R.string.biometric_dialog_last_pin_attempt_before_wipe_profile;
        }
        if (i == 2) {
            return R.string.biometric_dialog_last_pattern_attempt_before_wipe_profile;
        }
        return R.string.biometric_dialog_last_password_attempt_before_wipe_profile;
    }

    private static int getLastAttemptBeforeWipeUserMessageRes(int i) {
        if (i == 1) {
            return R.string.biometric_dialog_last_pin_attempt_before_wipe_user;
        }
        if (i == 2) {
            return R.string.biometric_dialog_last_pattern_attempt_before_wipe_user;
        }
        return R.string.biometric_dialog_last_password_attempt_before_wipe_user;
    }

    private static int getNowWipingMessageRes(int i) {
        if (i == 1) {
            return R.string.biometric_dialog_failed_attempts_now_wiping_device;
        }
        if (i == 2) {
            return R.string.biometric_dialog_failed_attempts_now_wiping_profile;
        }
        if (i == 3) {
            return R.string.biometric_dialog_failed_attempts_now_wiping_user;
        }
        throw new IllegalArgumentException("Unrecognized user type:" + i);
    }

    private static CharSequence getTitle(Bundle bundle) {
        CharSequence charSequence = bundle.getCharSequence("device_credential_title");
        return charSequence != null ? charSequence : bundle.getCharSequence("title");
    }

    private static CharSequence getSubtitle(Bundle bundle) {
        CharSequence charSequence = bundle.getCharSequence("device_credential_subtitle");
        return charSequence != null ? charSequence : bundle.getCharSequence("subtitle");
    }

    private static CharSequence getDescription(Bundle bundle) {
        CharSequence charSequence = bundle.getCharSequence("device_credential_description");
        return charSequence != null ? charSequence : bundle.getCharSequence("description");
    }
}
