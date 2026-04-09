package com.android.systemui.biometrics;

/* loaded from: classes.dex */
public interface AuthDialogCallback {
    void onDeviceCredentialPressed();

    void onDismissed(int i, byte[] bArr);

    void onSystemEvent(int i);

    void onTryAgainPressed();
}
