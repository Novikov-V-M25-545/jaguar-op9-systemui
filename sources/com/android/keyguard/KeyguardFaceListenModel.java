package com.android.keyguard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardFaceListenModel.kt */
/* loaded from: classes.dex */
public final class KeyguardFaceListenModel {
    private final boolean isAuthInterruptActive;
    private final boolean isBecauseCannotSkipBouncer;
    private final boolean isBouncer;
    private final boolean isFaceDisabled;
    private final boolean isFaceSettingEnabledForUser;
    private final boolean isKeyguardAwake;
    private final boolean isKeyguardGoingAway;
    private final boolean isListeningForFace;
    private final boolean isListeningForFaceAssistant;
    private final boolean isLockIconPressed;
    private final boolean isPrimaryUser;
    private final boolean isScanningAllowedByStrongAuth;
    private final boolean isSecureCameraLaunched;
    private final boolean isSwitchingUser;
    private final long timeMillis;
    private final int userId;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyguardFaceListenModel)) {
            return false;
        }
        KeyguardFaceListenModel keyguardFaceListenModel = (KeyguardFaceListenModel) obj;
        return this.timeMillis == keyguardFaceListenModel.timeMillis && this.userId == keyguardFaceListenModel.userId && this.isListeningForFace == keyguardFaceListenModel.isListeningForFace && this.isBouncer == keyguardFaceListenModel.isBouncer && this.isAuthInterruptActive == keyguardFaceListenModel.isAuthInterruptActive && this.isKeyguardAwake == keyguardFaceListenModel.isKeyguardAwake && this.isListeningForFaceAssistant == keyguardFaceListenModel.isListeningForFaceAssistant && this.isSwitchingUser == keyguardFaceListenModel.isSwitchingUser && this.isFaceDisabled == keyguardFaceListenModel.isFaceDisabled && this.isBecauseCannotSkipBouncer == keyguardFaceListenModel.isBecauseCannotSkipBouncer && this.isKeyguardGoingAway == keyguardFaceListenModel.isKeyguardGoingAway && this.isFaceSettingEnabledForUser == keyguardFaceListenModel.isFaceSettingEnabledForUser && this.isLockIconPressed == keyguardFaceListenModel.isLockIconPressed && this.isScanningAllowedByStrongAuth == keyguardFaceListenModel.isScanningAllowedByStrongAuth && this.isPrimaryUser == keyguardFaceListenModel.isPrimaryUser && this.isSecureCameraLaunched == keyguardFaceListenModel.isSecureCameraLaunched;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public int hashCode() {
        int iHashCode = ((Long.hashCode(this.timeMillis) * 31) + Integer.hashCode(this.userId)) * 31;
        boolean z = this.isListeningForFace;
        int i = z;
        if (z != 0) {
            i = 1;
        }
        int i2 = (iHashCode + i) * 31;
        boolean z2 = this.isBouncer;
        int i3 = z2;
        if (z2 != 0) {
            i3 = 1;
        }
        int i4 = (i2 + i3) * 31;
        boolean z3 = this.isAuthInterruptActive;
        int i5 = z3;
        if (z3 != 0) {
            i5 = 1;
        }
        int i6 = (i4 + i5) * 31;
        boolean z4 = this.isKeyguardAwake;
        int i7 = z4;
        if (z4 != 0) {
            i7 = 1;
        }
        int i8 = (i6 + i7) * 31;
        boolean z5 = this.isListeningForFaceAssistant;
        int i9 = z5;
        if (z5 != 0) {
            i9 = 1;
        }
        int i10 = (i8 + i9) * 31;
        boolean z6 = this.isSwitchingUser;
        int i11 = z6;
        if (z6 != 0) {
            i11 = 1;
        }
        int i12 = (i10 + i11) * 31;
        boolean z7 = this.isFaceDisabled;
        int i13 = z7;
        if (z7 != 0) {
            i13 = 1;
        }
        int i14 = (i12 + i13) * 31;
        boolean z8 = this.isBecauseCannotSkipBouncer;
        int i15 = z8;
        if (z8 != 0) {
            i15 = 1;
        }
        int i16 = (i14 + i15) * 31;
        boolean z9 = this.isKeyguardGoingAway;
        int i17 = z9;
        if (z9 != 0) {
            i17 = 1;
        }
        int i18 = (i16 + i17) * 31;
        boolean z10 = this.isFaceSettingEnabledForUser;
        int i19 = z10;
        if (z10 != 0) {
            i19 = 1;
        }
        int i20 = (i18 + i19) * 31;
        boolean z11 = this.isLockIconPressed;
        int i21 = z11;
        if (z11 != 0) {
            i21 = 1;
        }
        int i22 = (i20 + i21) * 31;
        boolean z12 = this.isScanningAllowedByStrongAuth;
        int i23 = z12;
        if (z12 != 0) {
            i23 = 1;
        }
        int i24 = (i22 + i23) * 31;
        boolean z13 = this.isPrimaryUser;
        int i25 = z13;
        if (z13 != 0) {
            i25 = 1;
        }
        int i26 = (i24 + i25) * 31;
        boolean z14 = this.isSecureCameraLaunched;
        return i26 + (z14 ? 1 : z14 ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "KeyguardFaceListenModel(timeMillis=" + this.timeMillis + ", userId=" + this.userId + ", isListeningForFace=" + this.isListeningForFace + ", isBouncer=" + this.isBouncer + ", isAuthInterruptActive=" + this.isAuthInterruptActive + ", isKeyguardAwake=" + this.isKeyguardAwake + ", isListeningForFaceAssistant=" + this.isListeningForFaceAssistant + ", isSwitchingUser=" + this.isSwitchingUser + ", isFaceDisabled=" + this.isFaceDisabled + ", isBecauseCannotSkipBouncer=" + this.isBecauseCannotSkipBouncer + ", isKeyguardGoingAway=" + this.isKeyguardGoingAway + ", isFaceSettingEnabledForUser=" + this.isFaceSettingEnabledForUser + ", isLockIconPressed=" + this.isLockIconPressed + ", isScanningAllowedByStrongAuth=" + this.isScanningAllowedByStrongAuth + ", isPrimaryUser=" + this.isPrimaryUser + ", isSecureCameraLaunched=" + this.isSecureCameraLaunched + ")";
    }

    public KeyguardFaceListenModel(long j, int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, boolean z9, boolean z10, boolean z11, boolean z12, boolean z13, boolean z14) {
        this.timeMillis = j;
        this.userId = i;
        this.isListeningForFace = z;
        this.isBouncer = z2;
        this.isAuthInterruptActive = z3;
        this.isKeyguardAwake = z4;
        this.isListeningForFaceAssistant = z5;
        this.isSwitchingUser = z6;
        this.isFaceDisabled = z7;
        this.isBecauseCannotSkipBouncer = z8;
        this.isKeyguardGoingAway = z9;
        this.isFaceSettingEnabledForUser = z10;
        this.isLockIconPressed = z11;
        this.isScanningAllowedByStrongAuth = z12;
        this.isPrimaryUser = z13;
        this.isSecureCameraLaunched = z14;
    }

    public final long getTimeMillis() {
        return this.timeMillis;
    }

    public final boolean isListeningForFace() {
        return this.isListeningForFace;
    }
}
