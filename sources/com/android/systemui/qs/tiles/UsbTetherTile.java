package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class UsbTetherTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent TETHER_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
    private final ConnectivityManager mConnectivityManager;
    private final QSTile.Icon mIcon;
    private boolean mListening;
    private final BroadcastReceiver mReceiver;
    private boolean mUsbConnected;
    private boolean mUsbTetherEnabled;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483610;
    }

    public UsbTetherTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_usb_tether);
        this.mUsbConnected = false;
        this.mUsbTetherEnabled = false;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.UsbTetherTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                UsbTetherTile.this.mUsbConnected = intent.getBooleanExtra("connected", false);
                if (!UsbTetherTile.this.mUsbConnected || !UsbTetherTile.this.mConnectivityManager.isTetheringSupported()) {
                    UsbTetherTile.this.mUsbTetherEnabled = false;
                } else {
                    UsbTetherTile.this.mUsbTetherEnabled = intent.getBooleanExtra("rndis", false);
                }
                UsbTetherTile.this.refreshState();
            }
        };
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.hardware.usb.action.USB_STATE");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            return;
        }
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (this.mUsbConnected) {
            this.mConnectivityManager.setUsbTethering(!this.mUsbTetherEnabled);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent(TETHER_SETTINGS);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        booleanState.value = this.mUsbTetherEnabled;
        booleanState.label = this.mContext.getString(R.string.quick_settings_usb_tether_label);
        booleanState.icon = this.mIcon;
        if (this.mUsbConnected) {
            i = this.mUsbTetherEnabled ? 2 : 1;
        } else {
            i = 0;
        }
        booleanState.state = i;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_usb_tether_label);
    }
}
