package com.android.systemui.power;

import android.content.Context;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserManager;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.fuelgauge.Estimate;
import com.android.settingslib.utils.PowerUtil;

/* loaded from: classes.dex */
public class EnhancedEstimatesImpl implements EnhancedEstimates {
    BatteryStatsHelper mBatteryStatsHelper;
    UserManager mUserManager;

    @Override // com.android.systemui.power.EnhancedEstimates
    public boolean getLowWarningEnabled() {
        return true;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public long getLowWarningThreshold() {
        return 0L;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public long getSevereWarningThreshold() {
        return 0L;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public boolean isHybridNotificationEnabled() {
        return true;
    }

    public EnhancedEstimatesImpl(Context context) {
        this.mBatteryStatsHelper = new BatteryStatsHelper(context, true);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public Estimate getEstimate() {
        try {
            this.mBatteryStatsHelper.create((Bundle) null);
            this.mBatteryStatsHelper.clearStats();
            this.mBatteryStatsHelper.refreshStats(0, this.mUserManager.getUserProfiles());
            BatteryStats stats = this.mBatteryStatsHelper.getStats();
            if (stats != null) {
                long jComputeBatteryTimeRemaining = stats.computeBatteryTimeRemaining(PowerUtil.convertMsToUs(SystemClock.elapsedRealtime()));
                if (jComputeBatteryTimeRemaining != -1) {
                    return new Estimate(PowerUtil.convertUsToMs(jComputeBatteryTimeRemaining), false, -1L);
                }
            }
        } catch (Exception unused) {
        }
        return null;
    }
}
