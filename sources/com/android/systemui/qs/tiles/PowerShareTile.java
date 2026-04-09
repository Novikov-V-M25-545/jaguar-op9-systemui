package com.android.systemui.qs.tiles;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.RemoteException;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BatteryController;
import java.util.NoSuchElementException;
import vendor.lineage.powershare.V1_0.IPowerShare;

/* loaded from: classes.dex */
public class PowerShareTile extends QSTileImpl<QSTile.BooleanState> implements BatteryController.BatteryStateChangeCallback {
    private BatteryController mBatteryController;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private IPowerShare mPowerShare;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483600;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    public PowerShareTile(QSHost qSHost, BatteryController batteryController) {
        super(qSHost);
        IPowerShare powerShare = getPowerShare();
        this.mPowerShare = powerShare;
        if (powerShare == null) {
            return;
        }
        this.mBatteryController = batteryController;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        this.mNotificationManager.createNotificationChannel(new NotificationChannel("powershare", this.mContext.getString(R.string.quick_settings_powershare_label), 3));
        Notification.Builder builder = new Notification.Builder(this.mContext, "powershare");
        builder.setContentTitle(this.mContext.getString(R.string.quick_settings_powershare_enabled_label));
        builder.setSmallIcon(R.drawable.ic_qs_powershare);
        builder.setOnlyAlertOnce(true);
        Notification notificationBuild = builder.build();
        this.mNotification = notificationBuild;
        notificationBuild.flags |= 34;
        notificationBuild.visibility = 1;
        batteryController.addCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public void refreshState() {
        updatePowerShareState();
        super.refreshState();
    }

    private void updatePowerShareState() {
        if (isAvailable()) {
            if (this.mBatteryController.isPowerSave()) {
                try {
                    this.mPowerShare.setEnabled(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (this.mPowerShare.isEnabled()) {
                    this.mNotificationManager.notify(273298, this.mNotification);
                } else {
                    this.mNotificationManager.cancel(273298);
                }
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mPowerShare != null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        try {
            boolean zIsEnabled = this.mPowerShare.isEnabled();
            if (this.mPowerShare.setEnabled(!zIsEnabled) != zIsEnabled) {
                refreshState();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        if (this.mBatteryController.isPowerSave()) {
            return this.mContext.getString(R.string.quick_settings_powershare_off_powersave_label);
        }
        if (getBatteryLevel() < getMinBatteryLevel()) {
            return this.mContext.getString(R.string.quick_settings_powershare_off_low_battery_label);
        }
        return this.mContext.getString(R.string.quick_settings_powershare_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (isAvailable()) {
            if (booleanState.slash == null) {
                booleanState.slash = new QSTile.SlashState();
            }
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_powershare);
            try {
                booleanState.value = this.mPowerShare.isEnabled();
            } catch (RemoteException e) {
                booleanState.value = false;
                e.printStackTrace();
            }
            booleanState.slash.isSlashed = booleanState.value;
            booleanState.label = this.mContext.getString(R.string.quick_settings_powershare_label);
            if (this.mBatteryController.isPowerSave() || getBatteryLevel() < getMinBatteryLevel()) {
                booleanState.state = 0;
            } else if (!booleanState.value) {
                booleanState.state = 1;
            } else {
                booleanState.state = 2;
            }
        }
    }

    private synchronized IPowerShare getPowerShare() {
        try {
            return IPowerShare.getService();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchElementException unused) {
            return null;
        }
    }

    private int getMinBatteryLevel() {
        try {
            return this.mPowerShare.getMinBattery();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getBatteryLevel() {
        return ((BatteryManager) this.mContext.getSystemService(BatteryManager.class)).getIntProperty(4);
    }
}
