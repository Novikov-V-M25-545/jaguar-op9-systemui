package com.android.systemui.qs.tiles;

import android.content.Intent;
import androidx.lifecycle.Lifecycle;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BatteryController;

/* loaded from: classes.dex */
public class AODTile extends QSTileImpl<QSTile.BooleanState> implements BatteryController.BatteryStateChangeCallback {
    private final BatteryController mBatteryController;
    private final QSTile.Icon mIcon;
    private final SecureSetting mSetting;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483601;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return true;
    }

    public AODTile(QSHost qSHost, BatteryController batteryController) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_aod);
        this.mSetting = new SecureSetting(this.mContext, this.mHandler, "doze_always_on") { // from class: com.android.systemui.qs.tiles.AODTile.1
            @Override // com.android.systemui.qs.SecureSetting
            protected void handleValueChanged(int i, boolean z) {
                AODTile.this.handleRefreshState(Integer.valueOf(i));
            }
        };
        this.mBatteryController = batteryController;
        batteryController.observe(getLifecycle(), (Lifecycle) this);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        this.mSetting.setValue(!((QSTile.BooleanState) this.mState).value ? 1 : 0);
        refreshState();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_always_on_display_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0;
        booleanState.icon = this.mIcon;
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R.string.quick_settings_always_on_display_label);
        booleanState.secondaryLabel = null;
        if (this.mBatteryController.isAodPowerSave()) {
            booleanState.state = 0;
            booleanState.secondaryLabel = this.mContext.getString(R.string.quick_settings_dark_mode_secondary_label_battery_saver);
        } else if (booleanState.value) {
            booleanState.state = 2;
        } else {
            booleanState.state = 1;
        }
    }
}
