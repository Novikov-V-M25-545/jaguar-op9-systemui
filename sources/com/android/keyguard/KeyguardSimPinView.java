package com.android.keyguard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.telephony.PinResult;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.systemui.Dependency;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class KeyguardSimPinView extends KeyguardPinBasedInputView {
    private CheckSimPin mCheckSimPinThread;
    private int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    private boolean mShowDefaultMessage;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromptReasonStringRes(int i) {
        return 0;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected boolean shouldLockout(long j) {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    public KeyguardSimPinView(Context context) {
        this(context, null);
    }

    public KeyguardSimPinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mSubId = -1;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardSimPinView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) throws Resources.NotFoundException {
                Log.v("KeyguardSimPinView", "onSimStateChanged(subId=" + i + ",state=" + i3 + ")");
                if (i3 == 5) {
                    KeyguardSimPinView.this.mRemainingAttempts = -1;
                    KeyguardSimPinView.this.resetState();
                } else {
                    KeyguardSimPinView.this.resetState();
                }
            }
        };
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() throws Resources.NotFoundException {
        super.resetState();
        Log.v("KeyguardSimPinView", "Resetting state");
        handleSubInfoChangeIfNeeded();
        if (this.mShowDefaultMessage) {
            showDefaultMessage();
        }
        ((KeyguardEsimArea) findViewById(R.id.keyguard_esim_area)).setVisibility(KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId) ? 0 : 8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setLockedSimMessage() throws Resources.NotFoundException {
        String string;
        boolean zIsEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId);
        TelephonyManager telephonyManager = (TelephonyManager) ((LinearLayout) this).mContext.getSystemService("phone");
        int activeModemCount = telephonyManager != null ? telephonyManager.getActiveModemCount() : 1;
        Resources resources = getResources();
        TypedArray typedArrayObtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{R.attr.wallpaperTextColor});
        int color = typedArrayObtainStyledAttributes.getColor(0, -1);
        typedArrayObtainStyledAttributes.recycle();
        if (activeModemCount < 2) {
            string = resources.getString(R.string.kg_sim_pin_instructions);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
            String string2 = resources.getString(R.string.kg_sim_pin_instructions_multi, subscriptionInfoForSubId != null ? subscriptionInfoForSubId.getDisplayName() : "");
            if (subscriptionInfoForSubId != null) {
                color = subscriptionInfoForSubId.getIconTint();
            }
            string = string2;
        }
        if (zIsEsimLocked) {
            string = resources.getString(R.string.kg_sim_lock_esim_instructions, string);
        }
        if (this.mSecurityMessageDisplay != null && getVisibility() == 0) {
            this.mSecurityMessageDisplay.setMessage(string);
        }
        this.mSimImageView.setImageTintList(ColorStateList.valueOf(color));
    }

    private void showDefaultMessage() throws Resources.NotFoundException {
        setLockedSimMessage();
        if (this.mRemainingAttempts >= 0) {
            return;
        }
        new CheckSimPin("", this.mSubId) { // from class: com.android.keyguard.KeyguardSimPinView.2
            @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
            void onSimCheckResponse(PinResult pinResult) throws Resources.NotFoundException {
                Log.d("KeyguardSimPinView", "onSimCheckResponse  dummy One result " + pinResult.toString());
                if (pinResult.getAttemptsRemaining() >= 0) {
                    KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                    KeyguardSimPinView.this.setLockedSimMessage();
                }
            }
        }.start();
    }

    private void handleSubInfoChangeIfNeeded() {
        int nextSubIdForState = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getNextSubIdForState(2);
        if (nextSubIdForState == this.mSubId || !SubscriptionManager.isValidSubscriptionId(nextSubIdForState)) {
            return;
        }
        this.mSubId = nextSubIdForState;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        resetState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPinPasswordErrorMessage(int i, boolean z) throws Resources.NotFoundException {
        String string;
        int i2;
        if (i == 0) {
            string = getContext().getString(R.string.kg_password_wrong_pin_code_pukked);
        } else if (i > 0) {
            if (z) {
                i2 = R.plurals.kg_password_default_pin_message;
            } else {
                i2 = R.plurals.kg_password_wrong_pin_code;
            }
            string = getContext().getResources().getQuantityString(i2, i, Integer.valueOf(i));
        } else {
            string = getContext().getString(z ? R.string.kg_sim_pin_instructions : R.string.kg_password_pin_failed);
        }
        if (KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId)) {
            string = getResources().getString(R.string.kg_sim_lock_esim_instructions, string);
        }
        Log.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + string);
        return string;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPasswordTextViewId() {
        return R.id.simPinEntry;
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = this.mEcaView;
        if (view instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) view).setCarrierTextVisible(true);
        }
        this.mSimImageView = (ImageView) findViewById(R.id.keyguard_sim);
        this.mPasswordEntry.setQuickUnlockListener(null);
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) throws Resources.NotFoundException {
        super.onResume(i);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        resetState();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    private abstract class CheckSimPin extends Thread {
        private final String mPin;
        private int mSubId;

        abstract void onSimCheckResponse(PinResult pinResult);

        protected CheckSimPin(String str, int i) {
            this.mPin = str;
            this.mSubId = i;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Log.v("KeyguardSimPinView", "call supplyPinReportResultForSubscriber(subid=" + this.mSubId + ")");
            final PinResult pinResultSupplyPinReportPinResult = ((TelephonyManager) ((LinearLayout) KeyguardSimPinView.this).mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPinReportPinResult(this.mPin);
            if (pinResultSupplyPinReportPinResult == null) {
                Log.e("KeyguardSimPinView", "Error result for supplyPinReportResult.");
                KeyguardSimPinView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.CheckSimPin.1
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(PinResult.getDefaultFailedResult());
                    }
                });
                return;
            }
            Log.v("KeyguardSimPinView", "supplyPinReportResult returned: " + pinResultSupplyPinReportPinResult.toString());
            KeyguardSimPinView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.CheckSimPin.2
                @Override // java.lang.Runnable
                public void run() {
                    CheckSimPin.this.onSimCheckResponse(pinResultSupplyPinReportPinResult);
                }
            });
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(((LinearLayout) this).mContext);
            this.mSimUnlockProgressDialog = progressDialog;
            progressDialog.setMessage(((LinearLayout) this).mContext.getString(R.string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            this.mSimUnlockProgressDialog.getWindow().setType(2009);
        }
        return this.mSimUnlockProgressDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Dialog getSimRemainingAttemptsDialog(int i) throws Resources.NotFoundException {
        String pinPasswordErrorMessage = getPinPasswordErrorMessage(i, false);
        AlertDialog alertDialog = this.mRemainingAttemptsDialog;
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(((LinearLayout) this).mContext);
            builder.setMessage(pinPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null);
            AlertDialog alertDialogCreate = builder.create();
            this.mRemainingAttemptsDialog = alertDialogCreate;
            alertDialogCreate.getWindow().setType(2009);
        } else {
            alertDialog.setMessage(pinPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void verifyPasswordAndUnlock() {
        String text = this.mPasswordEntry.getText();
        if (text.length() < 4 || text.length() > 8) {
            this.mSecurityMessageDisplay.setMessage(R.string.kg_invalid_sim_pin_hint);
            resetPasswordText(true, true);
            this.mCallback.userActivity();
        } else {
            getSimUnlockProgressDialog().show();
            if (this.mCheckSimPinThread == null) {
                CheckSimPin checkSimPin = new CheckSimPin(this.mPasswordEntry.getText(), this.mSubId) { // from class: com.android.keyguard.KeyguardSimPinView.3
                    @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
                    void onSimCheckResponse(final PinResult pinResult) {
                        KeyguardSimPinView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.3.1
                            @Override // java.lang.Runnable
                            public void run() {
                                KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                                if (KeyguardSimPinView.this.mSimUnlockProgressDialog != null) {
                                    KeyguardSimPinView.this.mSimUnlockProgressDialog.hide();
                                }
                                KeyguardSimPinView.this.resetPasswordText(true, pinResult.getType() != 0);
                                if (pinResult.getType() == 0) {
                                    ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPinView.this.mSubId);
                                    KeyguardSimPinView.this.mRemainingAttempts = -1;
                                    KeyguardSimPinView.this.mShowDefaultMessage = true;
                                    KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPinView.this.mCallback;
                                    if (keyguardSecurityCallback != null) {
                                        keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser(), KeyguardSecurityModel.SecurityMode.SimPin);
                                    }
                                } else {
                                    KeyguardSimPinView.this.mShowDefaultMessage = false;
                                    if (pinResult.getType() == 1) {
                                        if (pinResult.getAttemptsRemaining() <= 2) {
                                            KeyguardSimPinView.this.getSimRemainingAttemptsDialog(pinResult.getAttemptsRemaining()).show();
                                        } else {
                                            KeyguardSimPinView keyguardSimPinView = KeyguardSimPinView.this;
                                            keyguardSimPinView.mSecurityMessageDisplay.setMessage(keyguardSimPinView.getPinPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                        }
                                    } else {
                                        KeyguardSimPinView keyguardSimPinView2 = KeyguardSimPinView.this;
                                        keyguardSimPinView2.mSecurityMessageDisplay.setMessage(keyguardSimPinView2.getContext().getString(R.string.kg_password_pin_failed));
                                    }
                                    Log.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + pinResult + " attemptsRemaining=" + pinResult.getAttemptsRemaining());
                                }
                                KeyguardSimPinView.this.mCallback.userActivity();
                                KeyguardSimPinView.this.mCheckSimPinThread = null;
                            }
                        });
                    }
                };
                this.mCheckSimPinThread = checkSimPin;
                checkSimPin.start();
            }
        }
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(android.R.string.fingerprint_dangling_notification_msg_all_deleted_2);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return KeyguardSecurityModel.SecurityMode.SimPin;
    }
}
