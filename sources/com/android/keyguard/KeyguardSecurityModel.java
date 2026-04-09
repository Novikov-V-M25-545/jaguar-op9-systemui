package com.android.keyguard;

import android.R;
import android.content.Context;
import android.telephony.SubscriptionManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class KeyguardSecurityModel {
    private final Context mContext;
    private final boolean mIsPukScreenAvailable;
    private LockPatternUtils mLockPatternUtils;

    public enum SecurityMode {
        Invalid,
        None,
        Pattern,
        Password,
        PIN,
        SimPin,
        SimPuk
    }

    KeyguardSecurityModel(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mIsPukScreenAvailable = context.getResources().getBoolean(R.bool.config_cecPowerStateChangeOnActiveSourceLostStandbyNow_default);
    }

    void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    public SecurityMode getSecurityMode(final int i) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if (this.mIsPukScreenAvailable && SubscriptionManager.isValidSubscriptionId(keyguardUpdateMonitor.getNextSubIdForState(3))) {
            return SecurityMode.SimPuk;
        }
        if (SubscriptionManager.isValidSubscriptionId(keyguardUpdateMonitor.getNextSubIdForState(2))) {
            return SecurityMode.SimPin;
        }
        int iIntValue = ((Integer) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.keyguard.KeyguardSecurityModel$$ExternalSyntheticLambda0
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$getSecurityMode$0(i);
            }
        })).intValue();
        if (iIntValue == 0) {
            return SecurityMode.None;
        }
        if (iIntValue == 65536) {
            return SecurityMode.Pattern;
        }
        if (iIntValue == 131072 || iIntValue == 196608) {
            return SecurityMode.PIN;
        }
        if (iIntValue == 262144 || iIntValue == 327680 || iIntValue == 393216 || iIntValue == 524288) {
            return SecurityMode.Password;
        }
        throw new IllegalStateException("Unknown security quality:" + iIntValue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Integer lambda$getSecurityMode$0(int i) {
        return Integer.valueOf(this.mLockPatternUtils.getActivePasswordQuality(i));
    }
}
