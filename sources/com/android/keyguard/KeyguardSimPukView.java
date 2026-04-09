package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
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
public class KeyguardSimPukView extends KeyguardPinBasedInputView {
    private CheckSimPuk mCheckSimPukThread;
    private String mPinText;
    private String mPukText;
    private int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    private boolean mShowDefaultMessage;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private StateMachine mStateMachine;
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

    public KeyguardSimPukView(Context context) {
        this(context, null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mStateMachine = new StateMachine();
        this.mSubId = -1;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardSimPukView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) throws Resources.NotFoundException {
                if (i3 == 5) {
                    KeyguardSimPukView.this.mRemainingAttempts = -1;
                    KeyguardSimPukView.this.mShowDefaultMessage = true;
                    KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPukView.this.mCallback;
                    if (keyguardSecurityCallback != null) {
                        keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser(), KeyguardSecurityModel.SecurityMode.SimPuk);
                        return;
                    }
                    return;
                }
                KeyguardSimPukView.this.resetState();
            }
        };
    }

    private class StateMachine {
        final int CONFIRM_PIN;
        final int DONE;
        final int ENTER_PIN;
        final int ENTER_PUK;
        private int state;

        private StateMachine() {
            this.ENTER_PUK = 0;
            this.ENTER_PIN = 1;
            this.CONFIRM_PIN = 2;
            this.DONE = 3;
            this.state = 0;
        }

        public void next() {
            int i;
            int i2 = this.state;
            if (i2 == 0) {
                if (KeyguardSimPukView.this.checkPuk()) {
                    this.state = 1;
                    i = R.string.kg_puk_enter_pin_hint;
                } else {
                    i = R.string.kg_invalid_sim_puk_hint;
                }
            } else if (i2 == 1) {
                if (KeyguardSimPukView.this.checkPin()) {
                    this.state = 2;
                    i = R.string.kg_enter_confirm_pin_hint;
                } else {
                    i = R.string.kg_invalid_sim_pin_hint;
                }
            } else if (i2 != 2) {
                i = 0;
            } else if (KeyguardSimPukView.this.confirmPin()) {
                this.state = 3;
                i = R.string.keyguard_sim_unlock_progress_dialog_message;
                KeyguardSimPukView.this.updateSim();
            } else {
                this.state = 1;
                i = R.string.kg_invalid_confirm_pin_hint;
            }
            KeyguardSimPukView.this.resetPasswordText(true, true);
            if (i != 0) {
                KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(i);
            }
        }

        void reset() throws Resources.NotFoundException {
            KeyguardSimPukView.this.mPinText = "";
            KeyguardSimPukView.this.mPukText = "";
            this.state = 0;
            KeyguardSimPukView.this.handleSubInfoChangeIfNeeded();
            if (KeyguardSimPukView.this.mShowDefaultMessage) {
                KeyguardSimPukView.this.showDefaultMessage();
            }
            ((KeyguardEsimArea) KeyguardSimPukView.this.findViewById(R.id.keyguard_esim_area)).setVisibility(KeyguardEsimArea.isEsimLocked(((LinearLayout) KeyguardSimPukView.this).mContext, KeyguardSimPukView.this.mSubId) ? 0 : 8);
            KeyguardSimPukView.this.mPasswordEntry.requestFocus();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDefaultMessage() throws Resources.NotFoundException {
        String string;
        int i = this.mRemainingAttempts;
        if (i >= 0) {
            this.mSecurityMessageDisplay.setMessage(getPukPasswordErrorMessage(i, true));
            return;
        }
        boolean zIsEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId);
        TelephonyManager telephonyManager = (TelephonyManager) ((LinearLayout) this).mContext.getSystemService("phone");
        int activeModemCount = telephonyManager != null ? telephonyManager.getActiveModemCount() : 1;
        Resources resources = getResources();
        TypedArray typedArrayObtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{R.attr.wallpaperTextColor});
        int color = typedArrayObtainStyledAttributes.getColor(0, -1);
        typedArrayObtainStyledAttributes.recycle();
        String str = "";
        if (activeModemCount < 2) {
            string = resources.getString(R.string.kg_puk_enter_puk_hint);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
            String string2 = resources.getString(R.string.kg_puk_enter_puk_hint_multi, subscriptionInfoForSubId != null ? subscriptionInfoForSubId.getDisplayName() : "");
            if (subscriptionInfoForSubId != null) {
                color = subscriptionInfoForSubId.getIconTint();
            }
            string = string2;
        }
        if (zIsEsimLocked) {
            string = resources.getString(R.string.kg_sim_lock_esim_instructions, string);
        }
        SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
        if (securityMessageDisplay != null) {
            securityMessageDisplay.setMessage(string);
        }
        this.mSimImageView.setImageTintList(ColorStateList.valueOf(color));
        new CheckSimPuk(str, str, this.mSubId) { // from class: com.android.keyguard.KeyguardSimPukView.2
            @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
            void onSimLockChangedResponse(PinResult pinResult) {
                if (pinResult == null) {
                    Log.e("KeyguardSimPukView", "onSimCheckResponse, pin result is NULL");
                    return;
                }
                Log.d("KeyguardSimPukView", "onSimCheckResponse  dummy One result " + pinResult.toString());
                if (pinResult.getAttemptsRemaining() >= 0) {
                    KeyguardSimPukView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                    keyguardSimPukView.mSecurityMessageDisplay.setMessage(keyguardSimPukView.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), true));
                }
            }
        }.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSubInfoChangeIfNeeded() {
        int nextSubIdForState = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getNextSubIdForState(3);
        if (nextSubIdForState == this.mSubId || !SubscriptionManager.isValidSubscriptionId(nextSubIdForState)) {
            return;
        }
        this.mSubId = nextSubIdForState;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPukPasswordErrorMessage(int i, boolean z) throws Resources.NotFoundException {
        int i2;
        String string;
        int i3;
        if (i == 0) {
            string = getContext().getString(R.string.kg_password_wrong_puk_code_dead);
        } else if (i > 0) {
            if (z) {
                i3 = R.plurals.kg_password_default_puk_message;
            } else {
                i3 = R.plurals.kg_password_wrong_puk_code;
            }
            string = getContext().getResources().getQuantityString(i3, i, Integer.valueOf(i));
        } else {
            if (z) {
                i2 = R.string.kg_puk_enter_puk_hint;
            } else {
                i2 = R.string.kg_password_puk_failed;
            }
            string = getContext().getString(i2);
        }
        return KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId) ? getResources().getString(R.string.kg_sim_lock_esim_instructions, string) : string;
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() throws Resources.NotFoundException {
        super.resetState();
        this.mStateMachine.reset();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPasswordTextViewId() {
        return R.id.pukEntry;
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

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() throws Resources.NotFoundException {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        resetState();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    private abstract class CheckSimPuk extends Thread {
        private final String mPin;
        private final String mPuk;
        private final int mSubId;

        abstract void onSimLockChangedResponse(PinResult pinResult);

        protected CheckSimPuk(String str, String str2, int i) {
            this.mPuk = str;
            this.mPin = str2;
            this.mSubId = i;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            final PinResult pinResultSupplyPukReportPinResult = ((TelephonyManager) ((LinearLayout) KeyguardSimPukView.this).mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPukReportPinResult(this.mPuk, this.mPin);
            if (pinResultSupplyPukReportPinResult == null) {
                Log.e("KeyguardSimPukView", "Error result for supplyPukReportResult.");
                KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.1
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(PinResult.getDefaultFailedResult());
                    }
                });
            } else {
                KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.2
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(pinResultSupplyPukReportPinResult);
                    }
                });
            }
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(((LinearLayout) this).mContext);
            this.mSimUnlockProgressDialog = progressDialog;
            progressDialog.setMessage(((LinearLayout) this).mContext.getString(R.string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            if (!(((LinearLayout) this).mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Dialog getPukRemainingAttemptsDialog(int i) throws Resources.NotFoundException {
        String pukPasswordErrorMessage = getPukPasswordErrorMessage(i, false);
        AlertDialog alertDialog = this.mRemainingAttemptsDialog;
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(((LinearLayout) this).mContext);
            builder.setMessage(pukPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null);
            AlertDialog alertDialogCreate = builder.create();
            this.mRemainingAttemptsDialog = alertDialogCreate;
            alertDialogCreate.getWindow().setType(2009);
        } else {
            alertDialog.setMessage(pukPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() != 8) {
            return false;
        }
        this.mPukText = this.mPasswordEntry.getText();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPin() {
        int length = this.mPasswordEntry.getText().length();
        if (length < 4 || length > 8) {
            return false;
        }
        this.mPinText = this.mPasswordEntry.getText();
        return true;
    }

    public boolean confirmPin() {
        return this.mPinText.equals(this.mPasswordEntry.getText());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSim() {
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPukThread == null) {
            CheckSimPuk checkSimPuk = new CheckSimPuk(this.mPukText, this.mPinText, this.mSubId) { // from class: com.android.keyguard.KeyguardSimPukView.3
                @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
                void onSimLockChangedResponse(final PinResult pinResult) {
                    KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.3.1
                        @Override // java.lang.Runnable
                        public void run() throws Resources.NotFoundException {
                            if (KeyguardSimPukView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPukView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPukView.this.resetPasswordText(true, pinResult.getType() != 0);
                            if (pinResult.getType() == 0) {
                                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPukView.this.mSubId);
                                KeyguardSimPukView.this.mRemainingAttempts = -1;
                                KeyguardSimPukView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPukView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser(), KeyguardSecurityModel.SecurityMode.SimPuk);
                                }
                            } else {
                                KeyguardSimPukView.this.mShowDefaultMessage = false;
                                if (pinResult.getType() == 1) {
                                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                                    keyguardSimPukView.mSecurityMessageDisplay.setMessage(keyguardSimPukView.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                    if (pinResult.getAttemptsRemaining() <= 2) {
                                        KeyguardSimPukView.this.getPukRemainingAttemptsDialog(pinResult.getAttemptsRemaining()).show();
                                    } else {
                                        KeyguardSimPukView keyguardSimPukView2 = KeyguardSimPukView.this;
                                        keyguardSimPukView2.mSecurityMessageDisplay.setMessage(keyguardSimPukView2.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                    }
                                } else {
                                    KeyguardSimPukView keyguardSimPukView3 = KeyguardSimPukView.this;
                                    keyguardSimPukView3.mSecurityMessageDisplay.setMessage(keyguardSimPukView3.getContext().getString(R.string.kg_password_puk_failed));
                                }
                            }
                            KeyguardSimPukView.this.mStateMachine.reset();
                            KeyguardSimPukView.this.mCheckSimPukThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPukThread = checkSimPuk;
            checkSimPuk.start();
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void verifyPasswordAndUnlock() {
        this.mStateMachine.next();
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(android.R.string.fingerprint_dangling_notification_title);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return KeyguardSecurityModel.SecurityMode.SimPuk;
    }
}
