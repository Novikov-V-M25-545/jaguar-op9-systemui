package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothCodecStatus;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.settingslib.R$string;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/* loaded from: classes.dex */
public class BluetoothEventManager {
    private final Context mContext;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final android.os.Handler mReceiverHandler;
    private final UserHandle mUserHandle;
    private final BroadcastReceiver mBroadcastReceiver = new BluetoothBroadcastReceiver();
    private final BroadcastReceiver mProfileBroadcastReceiver = new BluetoothBroadcastReceiver();
    private final Collection<BluetoothCallback> mCallbacks = new CopyOnWriteArrayList();
    private final String ACT_BROADCAST_SOURCE_INFO = "android.bluetooth.BroadcastAudioSAManager.action.BROADCAST_SOURCE_INFO";
    private final IntentFilter mAdapterIntentFilter = new IntentFilter();
    private final IntentFilter mProfileIntentFilter = new IntentFilter();
    private final Map<String, Handler> mHandlerMap = new HashMap();

    interface Handler {
        void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice);
    }

    BluetoothEventManager(LocalBluetoothAdapter localBluetoothAdapter, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, final Context context, android.os.Handler handler, UserHandle userHandle) throws IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException {
        Object objNewInstance = null;
        this.mLocalAdapter = localBluetoothAdapter;
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mContext = context;
        this.mUserHandle = userHandle;
        this.mReceiverHandler = handler;
        addHandler("android.bluetooth.adapter.action.STATE_CHANGED", new AdapterStateChangedHandler());
        addHandler("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED", new ConnectionStateChangedHandler());
        addHandler("android.bluetooth.adapter.action.DISCOVERY_STARTED", new ScanningStateChangedHandler(true));
        addHandler("android.bluetooth.adapter.action.DISCOVERY_FINISHED", new ScanningStateChangedHandler(false));
        addHandler("android.bluetooth.device.action.FOUND", new DeviceFoundHandler());
        addHandler("android.bluetooth.device.action.NAME_CHANGED", new NameChangedHandler());
        addHandler("android.bluetooth.device.action.ALIAS_CHANGED", new NameChangedHandler());
        addHandler("android.bluetooth.device.action.BOND_STATE_CHANGED", new BondStateChangedHandler());
        addHandler("android.bluetooth.device.action.CLASS_CHANGED", new ClassChangedHandler());
        addHandler("android.bluetooth.device.action.UUID", new UuidChangedHandler());
        addHandler("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED", new BatteryLevelChangedHandler());
        addHandler("android.bluetooth.headset.action.HF_TWSP_BATTERY_STATE_CHANGED", new TwspBatteryLevelChangedHandler());
        addHandler("android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED", new ActiveDeviceChangedHandler());
        addHandler("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED", new ActiveDeviceChangedHandler());
        addHandler("android.bluetooth.hearingaid.profile.action.ACTIVE_DEVICE_CHANGED", new ActiveDeviceChangedHandler());
        addHandler("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED", new AudioModeChangedHandler());
        addHandler("android.intent.action.PHONE_STATE", new AudioModeChangedHandler());
        addHandler("android.bluetooth.device.action.ACL_CONNECTED", new AclStateChangedHandler());
        addHandler("android.bluetooth.device.action.ACL_DISCONNECTED", new AclStateChangedHandler());
        addHandler("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED", new A2dpCodecConfigChangedHandler());
        try {
            objNewInstance = Class.forName("com.android.settingslib.bluetooth.BroadcastSourceInfoHandler").getDeclaredConstructor(CachedBluetoothDeviceManager.class).newInstance(cachedBluetoothDeviceManager);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (objNewInstance != null) {
            Log.d("BluetoothEventManager", "adding SourceInfo Handler");
            addHandler("android.bluetooth.BroadcastAudioSAManager.action.BROADCAST_SOURCE_INFO", (Handler) objNewInstance);
        }
        registerAdapterIntentReceiver();
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("bluetooth_off_timeout"), false, new ContentObserver(handler) { // from class: com.android.settingslib.bluetooth.BluetoothEventManager.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                BluetoothTimeoutReceiver.setTimeoutAlarm(BluetoothEventManager.this.mContext, Settings.Global.getLong(context.getContentResolver(), "bluetooth_off_timeout", 0L));
            }
        });
    }

    public void registerCallback(BluetoothCallback bluetoothCallback) {
        this.mCallbacks.add(bluetoothCallback);
    }

    void registerProfileIntentReceiver() {
        registerIntentReceiver(this.mProfileBroadcastReceiver, this.mProfileIntentFilter);
    }

    void registerAdapterIntentReceiver() {
        registerIntentReceiver(this.mBroadcastReceiver, this.mAdapterIntentFilter);
    }

    private void registerIntentReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        UserHandle userHandle = this.mUserHandle;
        if (userHandle == null) {
            this.mContext.registerReceiver(broadcastReceiver, intentFilter, null, this.mReceiverHandler);
        } else {
            this.mContext.registerReceiverAsUser(broadcastReceiver, userHandle, intentFilter, null, this.mReceiverHandler);
        }
    }

    void addProfileHandler(String str, Handler handler) {
        this.mHandlerMap.put(str, handler);
        this.mProfileIntentFilter.addAction(str);
    }

    boolean readPairedDevices() {
        Set<BluetoothDevice> bondedDevices = this.mLocalAdapter.getBondedDevices();
        boolean z = false;
        if (bondedDevices == null) {
            return false;
        }
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (this.mDeviceManager.findDevice(bluetoothDevice) == null) {
                this.mDeviceManager.addDevice(bluetoothDevice);
                z = true;
            }
        }
        return z;
    }

    void dispatchDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onDeviceAdded(cachedBluetoothDevice);
        }
    }

    void dispatchDeviceRemoved(CachedBluetoothDevice cachedBluetoothDevice) {
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onDeviceDeleted(cachedBluetoothDevice);
        }
    }

    void dispatchProfileConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i, int i2) {
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onProfileConnectionStateChanged(cachedBluetoothDevice, i, i2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onConnectionStateChanged(cachedBluetoothDevice, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchAudioModeChanged() {
        Iterator<CachedBluetoothDevice> it = this.mDeviceManager.getCachedDevicesCopy().iterator();
        while (it.hasNext()) {
            it.next().onAudioModeChanged();
        }
        Iterator<BluetoothCallback> it2 = this.mCallbacks.iterator();
        while (it2.hasNext()) {
            it2.next().onAudioModeChanged();
        }
    }

    void dispatchActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        for (CachedBluetoothDevice cachedBluetoothDevice2 : this.mDeviceManager.getCachedDevicesCopy()) {
            cachedBluetoothDevice2.onActiveDeviceChanged(Objects.equals(cachedBluetoothDevice2, cachedBluetoothDevice), i);
        }
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onActiveDeviceChanged(cachedBluetoothDevice, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchAclStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onAclConnectionStateChanged(cachedBluetoothDevice, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchA2dpCodecConfigChanged(CachedBluetoothDevice cachedBluetoothDevice, BluetoothCodecStatus bluetoothCodecStatus) {
        Iterator<BluetoothCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onA2dpCodecConfigChanged(cachedBluetoothDevice, bluetoothCodecStatus);
        }
    }

    void addHandler(String str, Handler handler) {
        this.mHandlerMap.put(str, handler);
        this.mAdapterIntentFilter.addAction(str);
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        private BluetoothBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Handler handler = (Handler) BluetoothEventManager.this.mHandlerMap.get(action);
            if (handler != null) {
                handler.onReceive(context, intent, bluetoothDevice);
            }
        }
    }

    private class AdapterStateChangedHandler implements Handler {
        private AdapterStateChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
            BluetoothEventManager.this.mLocalAdapter.setBluetoothStateInt(intExtra);
            Iterator it = BluetoothEventManager.this.mCallbacks.iterator();
            while (it.hasNext()) {
                ((BluetoothCallback) it.next()).onBluetoothStateChanged(intExtra);
            }
            BluetoothEventManager.this.mDeviceManager.onBluetoothStateChanged(intExtra);
            if (intExtra == 12) {
                BluetoothTimeoutReceiver.setTimeoutAlarm(context, Settings.Global.getLong(context.getContentResolver(), "bluetooth_off_timeout", 0L));
            }
        }
    }

    private class ScanningStateChangedHandler implements Handler {
        private final boolean mStarted;

        ScanningStateChangedHandler(boolean z) {
            this.mStarted = z;
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            Iterator it = BluetoothEventManager.this.mCallbacks.iterator();
            while (it.hasNext()) {
                ((BluetoothCallback) it.next()).onScanningStateChanged(this.mStarted);
            }
            BluetoothEventManager.this.mDeviceManager.onScanningStateChanged(this.mStarted);
            BluetoothTimeoutReceiver.setTimeoutAlarm(context, this.mStarted ? 0L : Settings.Global.getLong(context.getContentResolver(), "bluetooth_off_timeout", 0L));
        }
    }

    private class DeviceFoundHandler implements Handler {
        private DeviceFoundHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            short shortExtra = intent.getShortExtra("android.bluetooth.device.extra.RSSI", Short.MIN_VALUE);
            intent.getStringExtra("android.bluetooth.device.extra.NAME");
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            if (cachedBluetoothDeviceFindDevice == null) {
                cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.addDevice(bluetoothDevice);
                Log.d("BluetoothEventManager", "DeviceFoundHandler created new CachedBluetoothDevice: " + cachedBluetoothDeviceFindDevice);
            } else if (cachedBluetoothDeviceFindDevice.getBondState() == 12 && !cachedBluetoothDeviceFindDevice.getDevice().isConnected()) {
                BluetoothEventManager.this.dispatchDeviceAdded(cachedBluetoothDeviceFindDevice);
                Log.d("BluetoothEventManager", "DeviceFoundHandler found bonded and not connected device:" + cachedBluetoothDeviceFindDevice);
            } else {
                Log.d("BluetoothEventManager", "DeviceFoundHandler found existing CachedBluetoothDevice:" + cachedBluetoothDeviceFindDevice);
            }
            cachedBluetoothDeviceFindDevice.setRssi(shortExtra);
            cachedBluetoothDeviceFindDevice.setJustDiscovered(true);
        }
    }

    private class ConnectionStateChangedHandler implements Handler {
        private ConnectionStateChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", Integer.MIN_VALUE);
            BluetoothEventManager.this.dispatchConnectionStateChanged(cachedBluetoothDeviceFindDevice, intExtra);
            if (intExtra == 0) {
                BluetoothTimeoutReceiver.setTimeoutAlarm(context, Settings.Global.getLong(context.getContentResolver(), "bluetooth_off_timeout", 0L));
            }
        }
    }

    private class NameChangedHandler implements Handler {
        private NameChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            BluetoothEventManager.this.mDeviceManager.onDeviceNameUpdated(bluetoothDevice);
        }
    }

    private class BondStateChangedHandler implements Handler {
        private BondStateChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            if (bluetoothDevice == null) {
                Log.e("BluetoothEventManager", "ACTION_BOND_STATE_CHANGED with no EXTRA_DEVICE");
                return;
            }
            int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            if (cachedBluetoothDeviceFindDevice == null) {
                Log.w("BluetoothEventManager", "Got bonding state changed for " + bluetoothDevice + ", but we have no record of that device.");
                cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.addDevice(bluetoothDevice);
            }
            Iterator it = BluetoothEventManager.this.mCallbacks.iterator();
            while (it.hasNext()) {
                ((BluetoothCallback) it.next()).onDeviceBondStateChanged(cachedBluetoothDeviceFindDevice, intExtra);
            }
            cachedBluetoothDeviceFindDevice.onBondingStateChanged(intExtra);
            if (intExtra == 10) {
                if (cachedBluetoothDeviceFindDevice.getHiSyncId() != 0) {
                    BluetoothEventManager.this.mDeviceManager.onDeviceUnpaired(cachedBluetoothDeviceFindDevice);
                }
                showUnbondMessage(context, cachedBluetoothDeviceFindDevice.getName(), intent.getIntExtra("android.bluetooth.device.extra.REASON", Integer.MIN_VALUE));
            }
        }

        private void showUnbondMessage(Context context, String str, int i) {
            int i2;
            switch (i) {
                case 1:
                    i2 = R$string.bluetooth_pairing_pin_error_message;
                    break;
                case 2:
                    i2 = R$string.bluetooth_pairing_rejected_error_message;
                    break;
                case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                default:
                    Log.w("BluetoothEventManager", "showUnbondMessage: Not displaying any message for reason: " + i);
                    return;
                case 4:
                    i2 = R$string.bluetooth_pairing_device_down_error_message;
                    break;
                case 5:
                case 6:
                case 7:
                case QS.VERSION /* 8 */:
                    i2 = R$string.bluetooth_pairing_error_message;
                    break;
            }
            BluetoothUtils.showError(context, str, i2);
        }
    }

    private class ClassChangedHandler implements Handler {
        private ClassChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            if (cachedBluetoothDeviceFindDevice != null) {
                cachedBluetoothDeviceFindDevice.refresh();
            }
        }
    }

    private class UuidChangedHandler implements Handler {
        private UuidChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            if (cachedBluetoothDeviceFindDevice != null) {
                cachedBluetoothDeviceFindDevice.onUuidChanged();
            }
        }
    }

    private class BatteryLevelChangedHandler implements Handler {
        private BatteryLevelChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            if (cachedBluetoothDeviceFindDevice != null) {
                cachedBluetoothDeviceFindDevice.refresh();
            }
        }
    }

    private class TwspBatteryLevelChangedHandler implements Handler {
        private TwspBatteryLevelChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
            if (cachedBluetoothDeviceFindDevice != null) {
                cachedBluetoothDeviceFindDevice.mTwspBatteryState = intent.getIntExtra("android.bluetooth.headset.extra.HF_TWSP_BATTERY_STATE", -1);
                cachedBluetoothDeviceFindDevice.mTwspBatteryLevel = intent.getIntExtra("android.bluetooth.headset.extra.HF_TWSP_BATTERY_LEVEL", -1);
                Log.i("BluetoothEventManager", cachedBluetoothDeviceFindDevice + ": mTwspBatteryState: " + cachedBluetoothDeviceFindDevice.mTwspBatteryState + "mTwspBatteryLevel: " + cachedBluetoothDeviceFindDevice.mTwspBatteryLevel);
                cachedBluetoothDeviceFindDevice.refresh();
            }
        }
    }

    private class ActiveDeviceChangedHandler implements Handler {
        private ActiveDeviceChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            int i;
            String action = intent.getAction();
            if (action != null) {
                CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
                if (Objects.equals(action, "android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED")) {
                    i = 2;
                } else if (Objects.equals(action, "android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED")) {
                    i = 1;
                } else {
                    if (!Objects.equals(action, "android.bluetooth.hearingaid.profile.action.ACTIVE_DEVICE_CHANGED")) {
                        Log.w("BluetoothEventManager", "ActiveDeviceChangedHandler: unknown action " + action);
                        return;
                    }
                    i = 21;
                }
                BluetoothEventManager.this.dispatchActiveDeviceChanged(cachedBluetoothDeviceFindDevice, i);
                return;
            }
            Log.w("BluetoothEventManager", "ActiveDeviceChangedHandler: action is null");
        }
    }

    private class AclStateChangedHandler implements Handler {
        private AclStateChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            int i;
            if (bluetoothDevice != null) {
                if (BluetoothEventManager.this.mDeviceManager.isSubDevice(bluetoothDevice)) {
                    return;
                }
                String action = intent.getAction();
                if (action != null) {
                    CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
                    if (cachedBluetoothDeviceFindDevice == null) {
                        Log.w("BluetoothEventManager", "AclStateChangedHandler: activeDevice is null");
                        return;
                    }
                    if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
                        i = 2;
                    } else {
                        if (!action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                            Log.w("BluetoothEventManager", "ActiveDeviceChangedHandler: unknown action " + action);
                            return;
                        }
                        i = 0;
                    }
                    BluetoothEventManager.this.dispatchAclStateChanged(cachedBluetoothDeviceFindDevice, i);
                    return;
                }
                Log.w("BluetoothEventManager", "AclStateChangedHandler: action is null");
                return;
            }
            Log.w("BluetoothEventManager", "AclStateChangedHandler: device is null");
        }
    }

    private class AudioModeChangedHandler implements Handler {
        private AudioModeChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            if (intent.getAction() != null) {
                BluetoothEventManager.this.dispatchAudioModeChanged();
            } else {
                Log.w("BluetoothEventManager", "AudioModeChangedHandler() action is null");
            }
        }
    }

    private class A2dpCodecConfigChangedHandler implements Handler {
        private A2dpCodecConfigChangedHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothEventManager.Handler
        public void onReceive(Context context, Intent intent, BluetoothDevice bluetoothDevice) {
            if (intent.getAction() != null) {
                CachedBluetoothDevice cachedBluetoothDeviceFindDevice = BluetoothEventManager.this.mDeviceManager.findDevice(bluetoothDevice);
                if (cachedBluetoothDeviceFindDevice == null) {
                    Log.w("BluetoothEventManager", "A2dpCodecConfigChangedHandler: device is null");
                    return;
                }
                BluetoothCodecStatus bluetoothCodecStatus = (BluetoothCodecStatus) intent.getParcelableExtra("android.bluetooth.extra.CODEC_STATUS");
                Log.d("BluetoothEventManager", "A2dpCodecConfigChangedHandler: device=" + bluetoothDevice + ", codecStatus=" + bluetoothCodecStatus);
                BluetoothEventManager.this.dispatchA2dpCodecConfigChanged(cachedBluetoothDeviceFindDevice, bluetoothCodecStatus);
                return;
            }
            Log.w("BluetoothEventManager", "A2dpCodecConfigChangedHandler: action is null");
        }
    }
}
