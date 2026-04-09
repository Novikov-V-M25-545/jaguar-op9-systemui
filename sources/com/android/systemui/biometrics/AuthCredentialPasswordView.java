package com.android.systemui.biometrics;

import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImeAwareEditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockscreenCredential;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class AuthCredentialPasswordView extends AuthCredentialView implements TextView.OnEditorActionListener {
    private final InputMethodManager mImm;
    private ImeAwareEditText mPasswordField;

    public AuthCredentialPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mImm = (InputMethodManager) ((LinearLayout) this).mContext.getSystemService(InputMethodManager.class);
    }

    @Override // com.android.systemui.biometrics.AuthCredentialView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        ImeAwareEditText imeAwareEditTextFindViewById = findViewById(R.id.lockPassword);
        this.mPasswordField = imeAwareEditTextFindViewById;
        imeAwareEditTextFindViewById.setOnEditorActionListener(this);
        this.mPasswordField.setOnKeyListener(new View.OnKeyListener() { // from class: com.android.systemui.biometrics.AuthCredentialPasswordView$$ExternalSyntheticLambda0
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view, int i, KeyEvent keyEvent) {
                return this.f$0.lambda$onFinishInflate$0(view, i, keyEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$onFinishInflate$0(View view, int i, KeyEvent keyEvent) {
        if (i != 4) {
            return false;
        }
        if (keyEvent.getAction() == 1) {
            this.mContainerView.sendEarlyUserCanceled();
            this.mContainerView.animateAway(1);
        }
        return true;
    }

    @Override // com.android.systemui.biometrics.AuthCredentialView, android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() throws Resources.NotFoundException {
        super.onAttachedToWindow();
        this.mPasswordField.setTextOperationUser(UserHandle.of(this.mUserId));
        if (this.mCredentialType == 1) {
            this.mPasswordField.setInputType(18);
        }
        this.mPasswordField.requestFocus();
        this.mPasswordField.scheduleShowSoftInput();
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        boolean z = keyEvent == null && (i == 0 || i == 6 || i == 5);
        boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
        if (!z && !z2) {
            return false;
        }
        checkPasswordAndUnlock();
        return true;
    }

    private void checkPasswordAndUnlock() {
        LockscreenCredential lockscreenCredentialCreatePasswordOrNone;
        if (this.mCredentialType == 1) {
            lockscreenCredentialCreatePasswordOrNone = LockscreenCredential.createPinOrNone(this.mPasswordField.getText());
        } else {
            lockscreenCredentialCreatePasswordOrNone = LockscreenCredential.createPasswordOrNone(this.mPasswordField.getText());
        }
        try {
            if (!lockscreenCredentialCreatePasswordOrNone.isNone()) {
                this.mPendingLockCheck = LockPatternChecker.verifyCredential(this.mLockPatternUtils, lockscreenCredentialCreatePasswordOrNone, this.mOperationId, this.mEffectiveUserId, new LockPatternChecker.OnVerifyCallback() { // from class: com.android.systemui.biometrics.AuthCredentialPasswordView$$ExternalSyntheticLambda1
                    public final void onVerified(byte[] bArr, int i) {
                        this.f$0.onCredentialVerified(bArr, i);
                    }
                });
                lockscreenCredentialCreatePasswordOrNone.close();
                return;
            }
            lockscreenCredentialCreatePasswordOrNone.close();
        } catch (Throwable th) {
            if (lockscreenCredentialCreatePasswordOrNone != null) {
                try {
                    lockscreenCredentialCreatePasswordOrNone.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    @Override // com.android.systemui.biometrics.AuthCredentialView
    protected void onCredentialVerified(byte[] bArr, int i) {
        super.onCredentialVerified(bArr, i);
        if (bArr != null) {
            this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
        } else {
            this.mPasswordField.setText("");
        }
    }
}
