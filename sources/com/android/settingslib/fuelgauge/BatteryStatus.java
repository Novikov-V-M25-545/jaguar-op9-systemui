package com.android.settingslib.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.android.settingslib.R$integer;

/* loaded from: classes.dex */
public class BatteryStatus {
    public final boolean dashChargeStatus;
    public final int health;
    public final int level;
    public final int maxChargingCurrent;
    public final int maxChargingVoltage;
    public final int maxChargingWattage;
    public final boolean oemFastChargeStatus;
    public final int plugged;
    public final boolean smartChargeStatus;
    public final int status;
    public final boolean swarpChargeStatus;
    public final float temperature;
    public final boolean turboPowerStatus;
    public final boolean voocChargeStatus;
    public final boolean warpChargeStatus;

    public BatteryStatus(int i, int i2, int i3, int i4, int i5, int i6, int i7, float f, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7) {
        this.status = i;
        this.level = i2;
        this.plugged = i3;
        this.health = i4;
        this.maxChargingCurrent = i5;
        this.maxChargingVoltage = i6;
        this.maxChargingWattage = i7;
        this.temperature = f;
        this.dashChargeStatus = z;
        this.warpChargeStatus = z2;
        this.swarpChargeStatus = z6;
        this.voocChargeStatus = z3;
        this.turboPowerStatus = z4;
        this.smartChargeStatus = z5;
        this.oemFastChargeStatus = z7;
    }

    public BatteryStatus(Intent intent) {
        this.status = intent.getIntExtra("status", 1);
        this.plugged = intent.getIntExtra("plugged", 0);
        this.level = intent.getIntExtra("level", 0);
        this.health = intent.getIntExtra("health", 1);
        this.temperature = intent.getIntExtra("temperature", -1);
        this.dashChargeStatus = intent.getBooleanExtra("dash_charger", false);
        this.warpChargeStatus = intent.getBooleanExtra("warp_charger", false);
        this.swarpChargeStatus = intent.getBooleanExtra("swarp_charger", false);
        this.voocChargeStatus = intent.getBooleanExtra("vooc_charger", false);
        this.turboPowerStatus = intent.getBooleanExtra("turbo_power", false);
        this.smartChargeStatus = intent.getBooleanExtra("smart_charger", false);
        this.oemFastChargeStatus = intent.getBooleanExtra("oem_fast_charger", false);
        int intExtra = intent.getIntExtra("max_charging_current", -1);
        int intExtra2 = intent.getIntExtra("max_charging_voltage", -1);
        intExtra2 = intExtra2 <= 0 ? 5000000 : intExtra2;
        if (intExtra > 0) {
            this.maxChargingWattage = (intExtra / 1000) * (intExtra2 / 1000);
            this.maxChargingCurrent = intExtra;
            this.maxChargingVoltage = intExtra2;
        } else {
            this.maxChargingWattage = -1;
            this.maxChargingCurrent = -1;
            this.maxChargingVoltage = -1;
        }
    }

    public boolean isPluggedIn() {
        int i = this.plugged;
        return i == 1 || i == 2 || i == 4;
    }

    public boolean isPluggedInWired() {
        int i = this.plugged;
        return i == 1 || i == 2;
    }

    public boolean isCharged() {
        return this.status == 5 || this.level >= 100;
    }

    public boolean isOverheated() {
        return this.health == 3;
    }

    public final int getChargingSpeed(Context context) throws Resources.NotFoundException {
        if (this.oemFastChargeStatus) {
            return 2;
        }
        int integer = context.getResources().getInteger(R$integer.config_chargingSlowlyThreshold);
        int integer2 = context.getResources().getInteger(R$integer.config_chargingFastThreshold);
        if (this.dashChargeStatus) {
            return 3;
        }
        if (this.warpChargeStatus) {
            return 4;
        }
        if (this.swarpChargeStatus) {
            return 8;
        }
        if (this.voocChargeStatus) {
            return 5;
        }
        if (this.turboPowerStatus) {
            return 6;
        }
        if (this.smartChargeStatus) {
            return 7;
        }
        int i = this.maxChargingWattage;
        if (i <= 0) {
            return -1;
        }
        if (i < integer) {
            return 0;
        }
        return i > integer2 ? 2 : 1;
    }

    public String toString() {
        return "BatteryStatus{status=" + this.status + ",level=" + this.level + ",plugged=" + this.plugged + ",health=" + this.health + ",maxChargingWattage=" + this.maxChargingWattage + "}";
    }
}
