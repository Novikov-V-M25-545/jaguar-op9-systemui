package com.android.systemui.biometrics;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.AttributeSet;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockscreenCredential;
import com.android.systemui.R;
import java.util.List;

/* loaded from: classes.dex */
public class AuthCredentialPatternView extends AuthCredentialView {
    private LockPatternView mLockPatternView;

    /* JADX INFO: Access modifiers changed from: private */
    class UnlockPatternListener implements LockPatternView.OnPatternListener {
        public void onPatternCellAdded(List<LockPatternView.Cell> list) {
        }

        public void onPatternCleared() {
        }

        public void onPatternStart() {
        }

        private UnlockPatternListener() {
        }

        public void onPatternDetected(List<LockPatternView.Cell> list) {
            AsyncTask<?, ?, ?> asyncTask = AuthCredentialPatternView.this.mPendingLockCheck;
            if (asyncTask != null) {
                asyncTask.cancel(false);
            }
            AuthCredentialPatternView.this.mLockPatternView.setEnabled(false);
            if (list.size() < 4) {
                onPatternVerified(null, 0);
                return;
            }
            AuthCredentialPatternView authCredentialPatternView = AuthCredentialPatternView.this;
            LockscreenCredential lockscreenCredentialCreatePattern = LockscreenCredential.createPattern(list, authCredentialPatternView.mLockPatternUtils.getLockPatternSize(authCredentialPatternView.mUserId));
            try {
                AuthCredentialPatternView authCredentialPatternView2 = AuthCredentialPatternView.this;
                authCredentialPatternView2.mPendingLockCheck = LockPatternChecker.verifyCredential(authCredentialPatternView2.mLockPatternUtils, lockscreenCredentialCreatePattern, authCredentialPatternView2.mOperationId, authCredentialPatternView2.mEffectiveUserId, new LockPatternChecker.OnVerifyCallback() { // from class: com.android.systemui.biometrics.AuthCredentialPatternView$UnlockPatternListener$$ExternalSyntheticLambda0
                    public final void onVerified(byte[] bArr, int i) {
                        this.f$0.onPatternVerified(bArr, i);
                    }
                });
                if (lockscreenCredentialCreatePattern != null) {
                    lockscreenCredentialCreatePattern.close();
                }
            } catch (Throwable th) {
                if (lockscreenCredentialCreatePattern != null) {
                    try {
                        lockscreenCredentialCreatePattern.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onPatternVerified(byte[] bArr, int i) {
            AuthCredentialPatternView.this.onCredentialVerified(bArr, i);
            if (i > 0) {
                AuthCredentialPatternView.this.mLockPatternView.setEnabled(false);
            } else {
                AuthCredentialPatternView.this.mLockPatternView.setEnabled(true);
            }
        }
    }

    @Override // com.android.systemui.biometrics.AuthCredentialView
    protected void onErrorTimeoutFinish() {
        super.onErrorTimeoutFinish();
        this.mLockPatternView.setEnabled(true);
    }

    public AuthCredentialPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.biometrics.AuthCredentialView, android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() throws Resources.NotFoundException {
        super.onAttachedToWindow();
        LockPatternView lockPatternViewFindViewById = findViewById(R.id.lockPattern);
        this.mLockPatternView = lockPatternViewFindViewById;
        lockPatternViewFindViewById.setLockPatternSize(this.mLockPatternUtils.getLockPatternSize(this.mUserId));
        this.mLockPatternView.setOnPatternListener(new UnlockPatternListener());
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(this.mUserId));
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
    }
}
