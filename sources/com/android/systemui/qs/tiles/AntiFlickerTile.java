package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import lineageos.hardware.LiveDisplayManager;
import lineageos.providers.LineageSettings;

/* loaded from: classes.dex */
public class AntiFlickerTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent DISPLAY_SETTINGS = new Intent("android.settings.DISPLAY_SETTINGS");
    private boolean mAntiFlickerEnabled;
    private final QSTile.Icon mIcon;
    private final LiveDisplayManager mLiveDisplay;
    private final BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483599;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    public AntiFlickerTile(QSHost qSHost) {
        super(qSHost);
        this.mAntiFlickerEnabled = true;
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_anti_flicker);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.AntiFlickerTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                AntiFlickerTile.this.updateConfig();
                AntiFlickerTile.this.refreshState();
                AntiFlickerTile.this.unregisterReceiver();
            }
        };
        this.mReceiver = broadcastReceiver;
        this.mLiveDisplay = LiveDisplayManager.getInstance(this.mContext);
        if (updateConfig()) {
            return;
        }
        this.mContext.registerReceiver(broadcastReceiver, new IntentFilter("lineageos.intent.action.INITIALIZE_LIVEDISPLAY"));
        this.mReceiverRegistered = true;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        super.handleDestroy();
        unregisterReceiver();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterReceiver() {
        if (this.mReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateConfig() {
        if (this.mLiveDisplay.getConfig() == null) {
            return false;
        }
        this.mAntiFlickerEnabled = this.mLiveDisplay.getConfig().hasFeature(19);
        if (isAvailable()) {
            return true;
        }
        this.mHost.removeTile(getTileSpec());
        return true;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        setEnabled(!this.mLiveDisplay.isAntiFlickerEnabled());
        refreshState();
    }

    private void setEnabled(boolean z) {
        LineageSettings.System.putInt(this.mContext.getContentResolver(), "display_anti_flicker", z ? 1 : 0);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return DISPLAY_SETTINGS;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mAntiFlickerEnabled;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        try {
            booleanState.value = this.mLiveDisplay.isAntiFlickerEnabled();
        } catch (NullPointerException unused) {
            booleanState.value = false;
        }
        booleanState.icon = this.mIcon;
        booleanState.contentDescription = this.mContext.getString(R.string.quick_settings_anti_flicker);
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.label = getTileLabel();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_anti_flicker);
    }
}
