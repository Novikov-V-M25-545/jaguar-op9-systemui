package com.android.keyguard;

import com.android.keyguard.KeyguardSecurityModel;

/* loaded from: classes.dex */
public interface KeyguardSecurityCallback {
    void dismiss(boolean z, int i, KeyguardSecurityModel.SecurityMode securityMode);

    void dismiss(boolean z, int i, boolean z2, KeyguardSecurityModel.SecurityMode securityMode);

    default void onCancelClicked() {
    }

    void onUserInput();

    void reportUnlockAttempt(int i, boolean z, int i2);

    void reset();

    void userActivity();
}
