package com.android.systemui.qs.tiles;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.graph.BluetoothDeviceLayerDrawable;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* loaded from: classes.dex */
public class BluetoothTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent BLUETOOTH_SETTINGS = new Intent("android.settings.panel.action.BLUETOOTH");
    private final ActivityStarter mActivityStarter;
    private final BluetoothController.Callback mCallback;
    private final BluetoothController mController;
    private final BluetoothDetailAdapter mDetailAdapter;
    private final KeyguardStateController mKeyguard;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 113;
    }

    public BluetoothTile(QSHost qSHost, BluetoothController bluetoothController, ActivityStarter activityStarter, KeyguardStateController keyguardStateController) {
        super(qSHost);
        BluetoothController.Callback callback = new BluetoothController.Callback() { // from class: com.android.systemui.qs.tiles.BluetoothTile.2
            @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
            public void onBluetoothStateChange(boolean z) {
                BluetoothTile.this.refreshState();
                if (BluetoothTile.this.isShowingDetail()) {
                    BluetoothTile.this.mDetailAdapter.updateItems();
                    BluetoothTile.this.mDetailAdapter.setItemsVisible(true);
                    BluetoothTile bluetoothTile = BluetoothTile.this;
                    bluetoothTile.fireToggleStateChanged(bluetoothTile.mDetailAdapter.getToggleState().booleanValue());
                }
            }

            @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
            public void onBluetoothDevicesChanged() {
                BluetoothTile.this.refreshState();
                if (BluetoothTile.this.isShowingDetail()) {
                    BluetoothTile.this.mDetailAdapter.updateItems();
                }
            }
        };
        this.mCallback = callback;
        this.mController = bluetoothController;
        this.mActivityStarter = activityStarter;
        this.mDetailAdapter = (BluetoothDetailAdapter) createDetailAdapter();
        bluetoothController.observe(getLifecycle(), (Lifecycle) callback);
        this.mKeyguard = keyguardStateController;
        keyguardStateController.observe((LifecycleOwner) this, (BluetoothTile) new KeyguardStateController.Callback() { // from class: com.android.systemui.qs.tiles.BluetoothTile.1
            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardShowingChanged() {
                BluetoothTile.this.refreshState();
            }
        });
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    protected void handleClickInner() {
        boolean z = ((QSTile.BooleanState) this.mState).value;
        refreshState(z ? null : QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING);
        this.mController.setBluetoothEnabled(!z);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (this.mKeyguard.isMethodSecure() && this.mKeyguard.isShowing()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.BluetoothTile$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleClick$0();
                }
            });
        } else {
            handleClickInner();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleClick$0() {
        this.mHost.openPanels();
        handleClickInner();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return BLUETOOTH_SETTINGS;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSecondaryClick() {
        if (this.mKeyguard.isMethodSecure() && this.mKeyguard.isShowing()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.BluetoothTile$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleSecondaryClick$1();
                }
            });
            return;
        }
        if (!this.mController.canConfigBluetooth()) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.BLUETOOTH_SETTINGS"), 0);
            return;
        }
        showDetail(true);
        if (((QSTile.BooleanState) this.mState).value) {
            return;
        }
        this.mController.setBluetoothEnabled(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleSecondaryClick$1() {
        this.mHost.openPanels();
        showDetail(true);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_bluetooth_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        boolean z2 = z || this.mController.isBluetoothEnabled();
        boolean zIsBluetoothConnected = this.mController.isBluetoothConnected();
        boolean zIsBluetoothConnecting = this.mController.isBluetoothConnecting();
        booleanState.isTransient = z || zIsBluetoothConnecting || this.mController.getBluetoothState() == 11;
        booleanState.dualTarget = true;
        booleanState.value = z2;
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.slash.isSlashed = !z2;
        booleanState.label = this.mContext.getString(R.string.quick_settings_bluetooth_label);
        booleanState.secondaryLabel = TextUtils.emptyIfNull(getSecondaryLabel(z2, zIsBluetoothConnecting, zIsBluetoothConnected, booleanState.isTransient));
        booleanState.contentDescription = booleanState.label;
        booleanState.stateDescription = "";
        if (z2) {
            if (zIsBluetoothConnected) {
                booleanState.icon = new BluetoothConnectedTileIcon();
                if (!TextUtils.isEmpty(this.mController.getConnectedDeviceName())) {
                    booleanState.label = this.mController.getConnectedDeviceName();
                }
                booleanState.stateDescription = this.mContext.getString(R.string.accessibility_bluetooth_name, booleanState.label) + ", " + ((Object) booleanState.secondaryLabel);
            } else if (booleanState.isTransient) {
                booleanState.icon = QSTileImpl.ResourceIcon.get(android.R.drawable.fastscroll_label_right_material);
                booleanState.stateDescription = booleanState.secondaryLabel;
            } else {
                booleanState.icon = QSTileImpl.ResourceIcon.get(android.R.drawable.ic_menu_cut_material);
                booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_bluetooth);
                booleanState.stateDescription = this.mContext.getString(R.string.accessibility_not_connected);
            }
            booleanState.state = 2;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(android.R.drawable.ic_menu_cut_material);
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_bluetooth);
            booleanState.state = 1;
        }
        booleanState.dualLabelContentDescription = this.mContext.getResources().getString(R.string.accessibility_quick_settings_open_settings, getTileLabel());
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private String getSecondaryLabel(boolean z, boolean z2, boolean z3, boolean z4) {
        if (z2) {
            return this.mContext.getString(R.string.quick_settings_connecting);
        }
        if (z4) {
            return this.mContext.getString(R.string.quick_settings_bluetooth_secondary_label_transient);
        }
        List<CachedBluetoothDevice> connectedDevices = this.mController.getConnectedDevices();
        if (!z || !z3 || connectedDevices.isEmpty()) {
            return null;
        }
        if (connectedDevices.size() > 1) {
            return this.mContext.getResources().getQuantityString(R.plurals.quick_settings_hotspot_secondary_label_num_devices, connectedDevices.size(), Integer.valueOf(connectedDevices.size()));
        }
        CachedBluetoothDevice cachedBluetoothDevice = connectedDevices.get(0);
        int batteryLevel = cachedBluetoothDevice.getBatteryLevel();
        if (batteryLevel > -1) {
            return this.mContext.getString(R.string.quick_settings_bluetooth_secondary_label_battery_level, Utils.formatPercentage(batteryLevel));
        }
        BluetoothClass btClass = cachedBluetoothDevice.getBtClass();
        if (btClass == null) {
            return null;
        }
        if (cachedBluetoothDevice.isHearingAidDevice()) {
            return this.mContext.getString(R.string.quick_settings_bluetooth_secondary_label_hearing_aids);
        }
        if (btClass.doesClassMatch(1)) {
            return this.mContext.getString(R.string.quick_settings_bluetooth_secondary_label_audio);
        }
        if (btClass.doesClassMatch(0)) {
            return this.mContext.getString(R.string.quick_settings_bluetooth_secondary_label_headset);
        }
        if (btClass.doesClassMatch(3)) {
            return this.mContext.getString(R.string.quick_settings_bluetooth_secondary_label_input);
        }
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mController.isBluetoothSupported();
    }

    protected DetailAdapter createDetailAdapter() {
        return new BluetoothDetailAdapter();
    }

    private class BluetoothBatteryTileIcon extends QSTile.Icon {
        private int mBatteryLevel;
        private float mIconScale;

        BluetoothBatteryTileIcon(int i, float f) {
            this.mBatteryLevel = i;
            this.mIconScale = f;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return BluetoothDeviceLayerDrawable.createLayerDrawable(context, R.drawable.ic_qs_bluetooth_connected, this.mBatteryLevel, this.mIconScale);
        }
    }

    private class BluetoothConnectedTileIcon extends QSTile.Icon {
        BluetoothConnectedTileIcon() {
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return context.getDrawable(R.drawable.ic_qs_bluetooth_connected);
        }
    }

    protected class BluetoothDetailAdapter implements DetailAdapter, QSDetailItems.Callback {
        private QSDetailItems mItems;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 150;
        }

        protected BluetoothDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) BluetoothTile.this).mContext.getString(R.string.quick_settings_bluetooth_label);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) ((QSTileImpl) BluetoothTile.this).mState).value);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public boolean getToggleEnabled() {
            return BluetoothTile.this.mController.getBluetoothState() == 10 || BluetoothTile.this.mController.getBluetoothState() == 12;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return BluetoothTile.BLUETOOTH_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(((QSTileImpl) BluetoothTile.this).mContext, 154, z);
            BluetoothTile.this.mController.setBluetoothEnabled(z);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            QSDetailItems qSDetailItemsConvertOrInflate = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = qSDetailItemsConvertOrInflate;
            qSDetailItemsConvertOrInflate.setTagSuffix("Bluetooth");
            this.mItems.setCallback(this);
            updateItems();
            setItemsVisible(((QSTile.BooleanState) ((QSTileImpl) BluetoothTile.this).mState).value);
            return this.mItems;
        }

        public void setItemsVisible(boolean z) {
            QSDetailItems qSDetailItems = this.mItems;
            if (qSDetailItems == null) {
                return;
            }
            qSDetailItems.setItemsVisible(z);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateItems() {
            if (this.mItems == null) {
                return;
            }
            if (BluetoothTile.this.mController.isBluetoothEnabled()) {
                this.mItems.setEmptyState(R.drawable.ic_qs_bluetooth_detail_empty, R.string.quick_settings_bluetooth_detail_empty_text);
            } else {
                this.mItems.setEmptyState(R.drawable.ic_qs_bluetooth_detail_empty, R.string.bt_is_off);
            }
            ArrayList arrayList = new ArrayList();
            Collection<CachedBluetoothDevice> devices = BluetoothTile.this.mController.getDevices();
            if (devices != null) {
                int i = 0;
                int i2 = 0;
                for (CachedBluetoothDevice cachedBluetoothDevice : devices) {
                    if (BluetoothTile.this.mController.getBondState(cachedBluetoothDevice) != 10) {
                        QSDetailItems.Item item = new QSDetailItems.Item();
                        item.iconResId = android.R.drawable.ic_menu_cut_material;
                        item.line1 = cachedBluetoothDevice.getName();
                        item.tag = cachedBluetoothDevice;
                        int maxConnectionState = cachedBluetoothDevice.getMaxConnectionState();
                        if (maxConnectionState == 2) {
                            item.iconResId = R.drawable.ic_qs_bluetooth_connected;
                            int batteryLevel = cachedBluetoothDevice.getBatteryLevel();
                            if (batteryLevel <= -1) {
                                item.line2 = ((QSTileImpl) BluetoothTile.this).mContext.getString(R.string.quick_settings_connected);
                            } else {
                                item.icon = BluetoothTile.this.new BluetoothBatteryTileIcon(batteryLevel, 1.0f);
                                item.line2 = ((QSTileImpl) BluetoothTile.this).mContext.getString(R.string.quick_settings_connected_battery_level, Utils.formatPercentage(batteryLevel));
                            }
                            item.canDisconnect = true;
                            arrayList.add(i, item);
                            i++;
                        } else if (maxConnectionState == 1) {
                            item.iconResId = R.drawable.ic_qs_bluetooth_connecting;
                            item.line2 = ((QSTileImpl) BluetoothTile.this).mContext.getString(R.string.quick_settings_connecting);
                            arrayList.add(i, item);
                        } else {
                            arrayList.add(item);
                        }
                        i2++;
                        if (i2 == 20) {
                            break;
                        }
                    }
                }
            }
            this.mItems.setItems((QSDetailItems.Item[]) arrayList.toArray(new QSDetailItems.Item[arrayList.size()]));
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemClick(QSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item == null || (obj = item.tag) == null || (cachedBluetoothDevice = (CachedBluetoothDevice) obj) == null || cachedBluetoothDevice.getMaxConnectionState() != 0) {
                return;
            }
            BluetoothTile.this.mController.connect(cachedBluetoothDevice);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemDisconnect(QSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item == null || (obj = item.tag) == null || (cachedBluetoothDevice = (CachedBluetoothDevice) obj) == null) {
                return;
            }
            BluetoothTile.this.mController.disconnect(cachedBluetoothDevice);
        }
    }
}
