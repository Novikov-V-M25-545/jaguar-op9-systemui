package com.android.systemui.qs.tiles;

import android.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class SmartPixelsTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent SMART_PIXELS_SETTINGS = new Intent("android.settings.SMART_PIXELS_SETTINGS");
    private boolean mListening;
    private boolean mLowPowerMode;
    private boolean mSmartPixelsEnable;
    private boolean mSmartPixelsOnPowerSave;
    private BroadcastReceiver mSmartPixelsReceiver;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    public SmartPixelsTile(QSHost qSHost) {
        super(qSHost);
        this.mSmartPixelsReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.SmartPixelsTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                SmartPixelsTile.this.refreshState();
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        this.mListening = z;
        if (z) {
            this.mContext.registerReceiver(this.mSmartPixelsReceiver, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGED"));
        } else {
            this.mContext.unregisterReceiver(this.mSmartPixelsReceiver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_cecRcProfileSourceTopMenuNotHandled_default);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        this.mSmartPixelsEnable = Settings.System.getIntForUser(this.mContext.getContentResolver(), "smart_pixels_enable", 0, -2) == 1;
        this.mSmartPixelsOnPowerSave = Settings.System.getIntForUser(this.mContext.getContentResolver(), "smart_pixels_on_power_save", 0, -2) == 1;
        boolean z = Settings.Global.getInt(this.mContext.getContentResolver(), "low_power", 0) == 1;
        this.mLowPowerMode = z;
        if (z && this.mSmartPixelsOnPowerSave) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "smart_pixels_on_power_save", 0, -2);
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "smart_pixels_enable", 0, -2);
        } else if (!this.mSmartPixelsEnable) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "smart_pixels_enable", 1, -2);
        } else {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "smart_pixels_enable", 0, -2);
        }
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return SMART_PIXELS_SETTINGS;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        this.mSmartPixelsEnable = Settings.System.getIntForUser(this.mContext.getContentResolver(), "smart_pixels_enable", 0, -2) == 1;
        this.mSmartPixelsOnPowerSave = Settings.System.getIntForUser(this.mContext.getContentResolver(), "smart_pixels_on_power_save", 0, -2) == 1;
        this.mLowPowerMode = Settings.Global.getInt(this.mContext.getContentResolver(), "low_power", 0) == 1;
        booleanState.icon = QSTileImpl.ResourceIcon.get(com.android.systemui.R.drawable.ic_qs_smart_pixels);
        if (this.mLowPowerMode && this.mSmartPixelsOnPowerSave) {
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_smart_pixels_on_power_save);
            booleanState.value = true;
        } else if (this.mSmartPixelsEnable) {
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_smart_pixels);
            booleanState.value = true;
        } else {
            booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_smart_pixels);
            booleanState.value = false;
        }
        booleanState.state = booleanState.value ? 2 : 1;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(com.android.systemui.R.string.quick_settings_smart_pixels);
    }
}
