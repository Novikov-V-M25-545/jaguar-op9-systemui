package com.android.systemui.statusbar.phone;

import android.R;
import android.content.Context;
import android.hardware.biometrics.BiometricSourceType;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardBypassController.kt */
/* loaded from: classes.dex */
public class KeyguardBypassController implements Dumpable {
    public static final Companion Companion = new Companion(null);
    private boolean bouncerShowing;
    private boolean bypassEnabled;
    private boolean bypassEnabledBiometric;
    private int faceUnlockMethod;
    private boolean hasFaceFeature;
    private boolean isPulseExpanding;
    private boolean launchingAffordance;
    private final KeyguardStateController mKeyguardStateController;
    private PendingUnlock pendingUnlock;
    private boolean qSExpanded;
    private final StatusBarStateController statusBarStateController;

    @NotNull
    public BiometricUnlockController unlockController;

    /* compiled from: KeyguardBypassController.kt */
    private static final class PendingUnlock {
        private final boolean isStrongBiometric;

        @NotNull
        private final BiometricSourceType pendingUnlockType;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PendingUnlock)) {
                return false;
            }
            PendingUnlock pendingUnlock = (PendingUnlock) obj;
            return Intrinsics.areEqual(this.pendingUnlockType, pendingUnlock.pendingUnlockType) && this.isStrongBiometric == pendingUnlock.isStrongBiometric;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public int hashCode() {
            BiometricSourceType biometricSourceType = this.pendingUnlockType;
            int iHashCode = (biometricSourceType != null ? biometricSourceType.hashCode() : 0) * 31;
            boolean z = this.isStrongBiometric;
            int i = z;
            if (z != 0) {
                i = 1;
            }
            return iHashCode + i;
        }

        @NotNull
        public String toString() {
            return "PendingUnlock(pendingUnlockType=" + this.pendingUnlockType + ", isStrongBiometric=" + this.isStrongBiometric + ")";
        }

        public PendingUnlock(@NotNull BiometricSourceType pendingUnlockType, boolean z) {
            Intrinsics.checkParameterIsNotNull(pendingUnlockType, "pendingUnlockType");
            this.pendingUnlockType = pendingUnlockType;
            this.isStrongBiometric = z;
        }

        @NotNull
        public final BiometricSourceType getPendingUnlockType() {
            return this.pendingUnlockType;
        }

        public final boolean isStrongBiometric() {
            return this.isStrongBiometric;
        }
    }

    public final void setUnlockController(@NotNull BiometricUnlockController biometricUnlockController) {
        Intrinsics.checkParameterIsNotNull(biometricUnlockController, "<set-?>");
        this.unlockController = biometricUnlockController;
    }

    public final void setPulseExpanding(boolean z) {
        this.isPulseExpanding = z;
    }

    public final boolean getBypassEnabled() {
        return this.bypassEnabled && this.mKeyguardStateController.isFaceAuthEnabled();
    }

    public final boolean getBypassEnabledBiometric() {
        return this.bypassEnabledBiometric;
    }

    public final void setBypassEnabledBiometric(boolean z) {
        this.bypassEnabledBiometric = z;
    }

    public final int getFaceUnlockMethod() {
        return this.faceUnlockMethod;
    }

    public final void setFaceUnlockMethod(int i) {
        this.faceUnlockMethod = i;
    }

    public final void setBouncerShowing(boolean z) {
        this.bouncerShowing = z;
    }

    public final void setLaunchingAffordance(boolean z) {
        this.launchingAffordance = z;
    }

    public final void setQSExpanded(boolean z) {
        boolean z2 = this.qSExpanded != z;
        this.qSExpanded = z;
        if (!z2 || z) {
            return;
        }
        maybePerformPendingUnlock();
    }

    public KeyguardBypassController(@NotNull Context context, @NotNull final TunerService tunerService, @NotNull StatusBarStateController statusBarStateController, @NotNull NotificationLockscreenUserManager lockscreenUserManager, @NotNull KeyguardStateController keyguardStateController, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(tunerService, "tunerService");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(lockscreenUserManager, "lockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.mKeyguardStateController = keyguardStateController;
        this.statusBarStateController = statusBarStateController;
        boolean zHasSystemFeature = context.getPackageManager().hasSystemFeature("android.hardware.biometrics.face");
        this.hasFaceFeature = zHasSystemFeature;
        if (zHasSystemFeature) {
            dumpManager.registerDumpable("KeyguardBypassController", this);
            statusBarStateController.addCallback(new StatusBarStateController.StateListener() { // from class: com.android.systemui.statusbar.phone.KeyguardBypassController.1
                @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
                public void onStateChanged(int i) {
                    if (i != 1) {
                        KeyguardBypassController.this.pendingUnlock = null;
                    }
                }
            });
            if (context.getResources().getBoolean(R.bool.config_cecQuerySadAacDisabled_default)) {
                this.bypassEnabledBiometric = false;
            } else {
                tunerService.addTunable(new TunerService.Tunable() { // from class: com.android.systemui.statusbar.phone.KeyguardBypassController.2
                    @Override // com.android.systemui.tuner.TunerService.Tunable
                    public void onTuningChanged(@Nullable String str, @Nullable String str2) {
                        KeyguardBypassController.this.setFaceUnlockMethod(tunerService.getValue(str, 0));
                    }
                }, "face_unlock_method");
                final int i = context.getResources().getBoolean(R.bool.config_cecQuerySadAacDisabled_allowed) ? 1 : 0;
                tunerService.addTunable(new TunerService.Tunable() { // from class: com.android.systemui.statusbar.phone.KeyguardBypassController.3
                    @Override // com.android.systemui.tuner.TunerService.Tunable
                    public void onTuningChanged(@Nullable String str, @Nullable String str2) {
                        KeyguardBypassController keyguardBypassController = KeyguardBypassController.this;
                        keyguardBypassController.setBypassEnabledBiometric(keyguardBypassController.getFaceUnlockMethod() == 0 && tunerService.getValue(str, i) != 0);
                    }
                }, "face_unlock_dismisses_keyguard");
            }
            lockscreenUserManager.addUserChangedListener(new NotificationLockscreenUserManager.UserChangedListener() { // from class: com.android.systemui.statusbar.phone.KeyguardBypassController.4
                @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
                public void onUserChanged(int i2) {
                    KeyguardBypassController.this.pendingUnlock = null;
                }
            });
        }
    }

    public final boolean onBiometricAuthenticated(@NotNull BiometricSourceType biometricSourceType, boolean z) {
        Intrinsics.checkParameterIsNotNull(biometricSourceType, "biometricSourceType");
        boolean z2 = true;
        if (this.bypassEnabledBiometric) {
            if (biometricSourceType == BiometricSourceType.FACE && !canBypass()) {
                z2 = false;
            }
            if (!z2 && (this.isPulseExpanding || this.qSExpanded)) {
                this.pendingUnlock = new PendingUnlock(biometricSourceType, z);
            }
        }
        return z2;
    }

    public final void maybePerformPendingUnlock() {
        PendingUnlock pendingUnlock = this.pendingUnlock;
        if (pendingUnlock != null) {
            if (pendingUnlock == null) {
                Intrinsics.throwNpe();
            }
            BiometricSourceType pendingUnlockType = pendingUnlock.getPendingUnlockType();
            PendingUnlock pendingUnlock2 = this.pendingUnlock;
            if (pendingUnlock2 == null) {
                Intrinsics.throwNpe();
            }
            if (onBiometricAuthenticated(pendingUnlockType, pendingUnlock2.isStrongBiometric())) {
                BiometricUnlockController biometricUnlockController = this.unlockController;
                if (biometricUnlockController == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("unlockController");
                }
                PendingUnlock pendingUnlock3 = this.pendingUnlock;
                if (pendingUnlock3 == null) {
                    Intrinsics.throwNpe();
                }
                BiometricSourceType pendingUnlockType2 = pendingUnlock3.getPendingUnlockType();
                PendingUnlock pendingUnlock4 = this.pendingUnlock;
                if (pendingUnlock4 == null) {
                    Intrinsics.throwNpe();
                }
                biometricUnlockController.startWakeAndUnlock(pendingUnlockType2, pendingUnlock4.isStrongBiometric());
                this.pendingUnlock = null;
            }
        }
    }

    public final boolean canBypass() {
        if (this.bypassEnabledBiometric) {
            return this.bouncerShowing || !(this.statusBarStateController.getState() != 1 || this.launchingAffordance || this.isPulseExpanding || this.qSExpanded);
        }
        return false;
    }

    public final boolean canPlaySubtleWindowAnimations() {
        return this.bypassEnabledBiometric && this.statusBarStateController.getState() == 1 && !this.qSExpanded;
    }

    public final void onStartedGoingToSleep() {
        this.pendingUnlock = null;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("KeyguardBypassController:");
        if (this.pendingUnlock != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("  mPendingUnlock.pendingUnlockType: ");
            PendingUnlock pendingUnlock = this.pendingUnlock;
            if (pendingUnlock == null) {
                Intrinsics.throwNpe();
            }
            sb.append(pendingUnlock.getPendingUnlockType());
            pw.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  mPendingUnlock.isStrongBiometric: ");
            PendingUnlock pendingUnlock2 = this.pendingUnlock;
            if (pendingUnlock2 == null) {
                Intrinsics.throwNpe();
            }
            sb2.append(pendingUnlock2.isStrongBiometric());
            pw.println(sb2.toString());
        } else {
            pw.println("  mPendingUnlock: " + this.pendingUnlock);
        }
        pw.println("  bypassEnabled: " + getBypassEnabled());
        pw.println("  bypassEnabledBiometric: " + this.bypassEnabledBiometric);
        pw.println("  canBypass: " + canBypass());
        pw.println("  bouncerShowing: " + this.bouncerShowing);
        pw.println("  isPulseExpanding: " + this.isPulseExpanding);
        pw.println("  launchingAffordance: " + this.launchingAffordance);
        pw.println("  qSExpanded: " + this.qSExpanded);
        pw.println("  hasFaceFeature: " + this.hasFaceFeature);
    }

    /* compiled from: KeyguardBypassController.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
