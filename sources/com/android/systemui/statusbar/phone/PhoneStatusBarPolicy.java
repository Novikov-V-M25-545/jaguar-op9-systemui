package com.android.systemui.statusbar.phone;

import android.R;
import android.app.ActivityTaskManager;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.INetworkPolicyManager;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.text.format.DateFormat;
import android.util.Log;
import androidx.lifecycle.Observer;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.privacy.PrivacyItem;
import com.android.systemui.privacy.PrivacyItemController;
import com.android.systemui.privacy.PrivacyType;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.time.DateFormatUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class PhoneStatusBarPolicy implements BluetoothController.Callback, CommandQueue.Callbacks, RotationLockController.RotationLockControllerCallback, DataSaverController.Listener, ZenModeController.Callback, DeviceProvisionedController.DeviceProvisionedListener, KeyguardStateController.Callback, PrivacyItemController.Callback, LocationController.LocationChangeCallback, TunerService.Tunable, RecordingController.RecordingStateChangeCallback {
    private static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    static final int LOCATION_STATUS_ICON_ID = PrivacyType.TYPE_LOCATION.getIconId();
    private NfcAdapter mAdapter;
    private final AlarmManager mAlarmManager;
    private BluetoothController mBluetooth;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CastController mCast;
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private boolean mCurrentUserSetup;
    private final DataSaverController mDataSaver;
    private final DateFormatUtil mDateFormatUtil;
    private final int mDisplayId;
    private final HotspotController mHotspot;
    private final IActivityManager mIActivityManager;
    private final StatusBarIconController mIconController;
    private final KeyguardStateController mKeyguardStateController;
    private final LocationController mLocationController;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private final NextAlarmController mNextAlarmController;
    private boolean mNfcVisible;
    private final PrivacyItemController mPrivacyItemController;
    private final DeviceProvisionedController mProvisionedController;
    private final RecordingController mRecordingController;
    private final Resources mResources;
    private final RingerModeTracker mRingerModeTracker;
    private final RotationLockController mRotationLockController;
    private final SensorPrivacyController mSensorPrivacyController;
    private final SharedPreferences mSharedPreferences;
    private boolean mShowBluetoothBattery;
    private final String mSlotAlarmClock;
    private final String mSlotBluetooth;
    private final String mSlotCamera;
    private final String mSlotCast;
    private final String mSlotDataSaver;
    private final String mSlotFirewall;
    private final String mSlotHeadset;
    private final String mSlotHotspot;
    private final String mSlotLocation;
    private final String mSlotManagedProfile;
    private final String mSlotMicrophone;
    private final String mSlotNfc;
    private final String mSlotRotate;
    private final String mSlotScreenRecord;
    private final String mSlotSensorsOff;
    private final String mSlotTty;
    private final String mSlotVolume;
    private final String mSlotZen;
    private final TelecomManager mTelecomManager;
    private final Executor mUiBgExecutor;
    private final UserInfoController mUserInfoController;
    private final UserManager mUserManager;
    private boolean mVolumeVisible;
    private final ZenModeController mZenController;
    private boolean mZenVisible;
    private final Handler mHandler = new Handler();
    private boolean mFirewallVisible = false;
    private boolean mManagedProfileIconVisible = false;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new AnonymousClass1();
    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.2
        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotHotspot, z);
        }
    };
    private final CastController.Callback mCastCallback = new CastController.Callback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.3
        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onCastDevicesChanged() {
            PhoneStatusBarPolicy.this.updateCast();
        }
    };
    private final NextAlarmController.NextAlarmChangeCallback mNextAlarmCallback = new NextAlarmController.NextAlarmChangeCallback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.4
        @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
        public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
            PhoneStatusBarPolicy.this.mNextAlarm = alarmClockInfo;
            PhoneStatusBarPolicy.this.updateAlarm();
        }
    };
    private final SensorPrivacyController.OnSensorPrivacyChangedListener mSensorPrivacyListener = new AnonymousClass5();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) throws Resources.NotFoundException {
            String action = intent.getAction();
            action.hashCode();
            switch (action) {
                case "android.intent.action.HEADSET_PLUG":
                    PhoneStatusBarPolicy.this.updateHeadsetPlug(intent);
                    break;
                case "android.intent.action.MANAGED_PROFILE_UNAVAILABLE":
                case "android.intent.action.MANAGED_PROFILE_AVAILABLE":
                case "android.intent.action.MANAGED_PROFILE_REMOVED":
                    PhoneStatusBarPolicy.this.updateManagedProfile();
                    break;
                case "android.intent.action.SIM_STATE_CHANGED":
                    intent.getBooleanExtra("rebroadcastOnUnlock", false);
                    break;
                case "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED":
                    PhoneStatusBarPolicy.this.updateBluetooth();
                    break;
                case "android.telecom.action.CURRENT_TTY_MODE_CHANGED":
                    PhoneStatusBarPolicy.this.updateTTY(intent.getIntExtra("android.telecom.extra.CURRENT_TTY_MODE", 0));
                    break;
                case "android.nfc.action.ADAPTER_STATE_CHANGED":
                    PhoneStatusBarPolicy.this.updateNfc();
                    break;
            }
        }
    };
    private Runnable mRemoveCastIconRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.7
        @Override // java.lang.Runnable
        public void run() {
            if (PhoneStatusBarPolicy.DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
            }
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotCast, false);
        }
    };

    public PhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController, CommandQueue commandQueue, BroadcastDispatcher broadcastDispatcher, Executor executor, Resources resources, CastController castController, HotspotController hotspotController, BluetoothController bluetoothController, NextAlarmController nextAlarmController, UserInfoController userInfoController, RotationLockController rotationLockController, DataSaverController dataSaverController, ZenModeController zenModeController, DeviceProvisionedController deviceProvisionedController, KeyguardStateController keyguardStateController, LocationController locationController, SensorPrivacyController sensorPrivacyController, IActivityManager iActivityManager, AlarmManager alarmManager, UserManager userManager, RecordingController recordingController, TelecomManager telecomManager, int i, SharedPreferences sharedPreferences, DateFormatUtil dateFormatUtil, RingerModeTracker ringerModeTracker, PrivacyItemController privacyItemController) {
        this.mContext = context;
        this.mIconController = statusBarIconController;
        this.mCommandQueue = commandQueue;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mResources = resources;
        this.mCast = castController;
        this.mHotspot = hotspotController;
        this.mBluetooth = bluetoothController;
        this.mNextAlarmController = nextAlarmController;
        this.mAlarmManager = alarmManager;
        this.mUserInfoController = userInfoController;
        this.mIActivityManager = iActivityManager;
        this.mUserManager = userManager;
        this.mRotationLockController = rotationLockController;
        this.mDataSaver = dataSaverController;
        this.mZenController = zenModeController;
        this.mProvisionedController = deviceProvisionedController;
        this.mKeyguardStateController = keyguardStateController;
        this.mLocationController = locationController;
        this.mPrivacyItemController = privacyItemController;
        this.mSensorPrivacyController = sensorPrivacyController;
        this.mRecordingController = recordingController;
        this.mUiBgExecutor = executor;
        this.mTelecomManager = telecomManager;
        this.mRingerModeTracker = ringerModeTracker;
        this.mSlotCast = resources.getString(R.string.permlab_foregroundServiceRemoteMessaging);
        this.mSlotHotspot = resources.getString(R.string.permlab_hideOverlayWindows);
        this.mSlotBluetooth = resources.getString(R.string.permlab_foregroundServiceMicrophone);
        this.mSlotTty = resources.getString(R.string.permlab_observeCompanionDevicePresence);
        this.mSlotZen = resources.getString(R.string.permlab_processOutgoingCalls);
        this.mSlotVolume = resources.getString(R.string.permlab_persistentActivity);
        this.mSlotAlarmClock = resources.getString(R.string.permlab_foregroundServiceMediaProcessing);
        this.mSlotManagedProfile = resources.getString(R.string.permlab_install_shortcut);
        this.mSlotRotate = resources.getString(R.string.permlab_manageProfileAndDeviceOwners);
        this.mSlotHeadset = resources.getString(R.string.permlab_handoverStatus);
        this.mSlotDataSaver = resources.getString(R.string.permlab_getAccounts);
        this.mSlotLocation = resources.getString(R.string.permlab_imagesWrite);
        this.mSlotMicrophone = resources.getString(R.string.permlab_invokeCarrierSetup);
        this.mSlotCamera = resources.getString(R.string.permlab_foregroundServicePhoneCall);
        this.mSlotSensorsOff = resources.getString(R.string.permlab_modifyNetworkAccounting);
        this.mSlotScreenRecord = resources.getString(R.string.permlab_mediaLocation);
        this.mSlotNfc = resources.getString(R.string.permlab_manageNetworkPolicy);
        this.mSlotFirewall = resources.getString(R.string.permlab_getTasks);
        this.mCurrentUserSetup = deviceProvisionedController.isDeviceProvisioned();
        this.mDisplayId = i;
        this.mSharedPreferences = sharedPreferences;
        this.mDateFormatUtil = dateFormatUtil;
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:bluetooth_show_battery");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) throws Resources.NotFoundException {
        str.hashCode();
        if (str.equals("system:bluetooth_show_battery")) {
            this.mShowBluetoothBattery = TunerService.parseIntegerSwitch(str2, true);
            updateBluetooth();
        }
    }

    public void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        intentFilter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, this.mHandler);
        Observer<? super Integer> observer = new Observer() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda0
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                this.f$0.lambda$init$0((Integer) obj);
            }
        };
        this.mRingerModeTracker.getRingerMode().observeForever(observer);
        this.mRingerModeTracker.getRingerModeInternal().observeForever(observer);
        try {
            this.mIActivityManager.registerUserSwitchObserver(this.mUserSwitchListener, "PhoneStatusBarPolicy");
        } catch (RemoteException unused) {
        }
        updateTTY();
        updateBluetooth();
        this.mIconController.setIcon(this.mSlotAlarmClock, com.android.systemui.R.drawable.stat_sys_alarm, null);
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotZen, com.android.systemui.R.drawable.stat_sys_dnd, null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotVolume, com.android.systemui.R.drawable.stat_sys_ringer_vibrate, null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        updateVolumeZen();
        this.mIconController.setIcon(this.mSlotCast, com.android.systemui.R.drawable.stat_sys_cast, null);
        this.mIconController.setIconVisibility(this.mSlotCast, false);
        this.mIconController.setIcon(this.mSlotHotspot, com.android.systemui.R.drawable.stat_sys_hotspot, this.mResources.getString(com.android.systemui.R.string.accessibility_status_bar_hotspot));
        this.mIconController.setIconVisibility(this.mSlotHotspot, this.mHotspot.isHotspotEnabled());
        this.mIconController.setIcon(this.mSlotManagedProfile, com.android.systemui.R.drawable.stat_sys_managed_profile_status, this.mResources.getString(com.android.systemui.R.string.accessibility_managed_profile));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, com.android.systemui.R.drawable.stat_sys_data_saver, this.mResources.getString(com.android.systemui.R.string.accessibility_data_saver_on));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        Resources resources = this.mResources;
        PrivacyType privacyType = PrivacyType.TYPE_MICROPHONE;
        String string = resources.getString(privacyType.getNameId());
        Resources resources2 = this.mResources;
        int i = com.android.systemui.R.string.ongoing_privacy_chip_content_multiple_apps;
        this.mIconController.setIcon(this.mSlotMicrophone, privacyType.getIconId(), resources2.getString(i, string));
        this.mIconController.setIconVisibility(this.mSlotMicrophone, false);
        Resources resources3 = this.mResources;
        PrivacyType privacyType2 = PrivacyType.TYPE_CAMERA;
        this.mIconController.setIcon(this.mSlotCamera, privacyType2.getIconId(), this.mResources.getString(i, resources3.getString(privacyType2.getNameId())));
        this.mIconController.setIconVisibility(this.mSlotCamera, false);
        this.mIconController.setIcon(this.mSlotLocation, LOCATION_STATUS_ICON_ID, this.mResources.getString(com.android.systemui.R.string.accessibility_location_active));
        this.mIconController.setIconVisibility(this.mSlotLocation, false);
        this.mIconController.setIcon(this.mSlotSensorsOff, com.android.systemui.R.drawable.stat_sys_sensors_off, this.mResources.getString(com.android.systemui.R.string.accessibility_sensors_off_active));
        this.mIconController.setIconVisibility(this.mSlotSensorsOff, this.mSensorPrivacyController.isSensorPrivacyEnabled());
        this.mIconController.setIcon(this.mSlotScreenRecord, com.android.systemui.R.drawable.stat_sys_screen_record, null);
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
        this.mIconController.setIcon(this.mSlotNfc, com.android.systemui.R.drawable.stat_sys_nfc, this.mResources.getString(com.android.systemui.R.string.accessibility_status_bar_nfc));
        this.mIconController.setIconVisibility(this.mSlotNfc, false);
        updateNfc();
        this.mIconController.setIcon(this.mSlotFirewall, com.android.systemui.R.drawable.stat_sys_firewall, null);
        this.mIconController.setIconVisibility(this.mSlotFirewall, this.mFirewallVisible);
        this.mRotationLockController.addCallback(this);
        this.mBluetooth.addCallback(this);
        this.mProvisionedController.addCallback(this);
        this.mZenController.addCallback(this);
        this.mCast.addCallback(this.mCastCallback);
        this.mHotspot.addCallback(this.mHotspotCallback);
        this.mNextAlarmController.addCallback(this.mNextAlarmCallback);
        this.mDataSaver.addCallback(this);
        this.mKeyguardStateController.addCallback(this);
        this.mPrivacyItemController.addCallback(this);
        this.mSensorPrivacyController.addCallback(this.mSensorPrivacyListener);
        this.mLocationController.addCallback(this);
        this.mRecordingController.addCallback((RecordingController.RecordingStateChangeCallback) this);
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$init$0(Integer num) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.updateVolumeZen();
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenChanged(int i) throws Resources.NotFoundException {
        updateVolumeZen();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(ZenModeConfig zenModeConfig) throws Resources.NotFoundException {
        updateVolumeZen();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAlarm() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(-2);
        boolean z = nextAlarmClock != null && nextAlarmClock.getTriggerTime() > 0;
        this.mIconController.setIcon(this.mSlotAlarmClock, this.mZenController.getZen() == 2 ? com.android.systemui.R.drawable.stat_sys_alarm_dim : com.android.systemui.R.drawable.stat_sys_alarm, buildAlarmContentDescription());
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, this.mCurrentUserSetup && z);
    }

    private String buildAlarmContentDescription() {
        if (this.mNextAlarm == null) {
            return this.mResources.getString(com.android.systemui.R.string.status_bar_alarm);
        }
        return this.mResources.getString(com.android.systemui.R.string.accessibility_quick_settings_alarm, DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), this.mDateFormatUtil.is24HourFormat() ? "EHm" : "Ehma"), this.mNextAlarm.getTriggerTime()).toString());
    }

    private NfcAdapter getAdapter() {
        if (this.mAdapter == null) {
            try {
                this.mAdapter = NfcAdapter.getNfcAdapter(this.mContext);
            } catch (UnsupportedOperationException unused) {
                this.mAdapter = null;
            }
        }
        return this.mAdapter;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateNfc() {
        boolean z = getAdapter() != null && getAdapter().isEnabled();
        this.mNfcVisible = z;
        if (z) {
            this.mIconController.setIconVisibility(this.mSlotNfc, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotNfc, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateVolumeZen() throws Resources.NotFoundException {
        boolean z;
        int i;
        String string;
        int i2;
        Integer value;
        int i3;
        int i4;
        String string2;
        int zen = this.mZenController.getZen();
        String string3 = null;
        boolean z2 = false;
        if (DndTile.isVisible(this.mSharedPreferences) || DndTile.isCombinedIcon(this.mSharedPreferences)) {
            z = zen != 0;
            if (zen == 1) {
                i = com.android.systemui.R.drawable.stat_sys_dnd_priority;
            } else if (zen == 2) {
                i = com.android.systemui.R.drawable.stat_sys_dnd_total_silence;
            } else {
                i = com.android.systemui.R.drawable.stat_sys_dnd;
            }
            string = this.mResources.getString(com.android.systemui.R.string.quick_settings_dnd_label);
        } else {
            if (zen == 2) {
                i4 = com.android.systemui.R.drawable.stat_sys_dnd;
                string2 = this.mResources.getString(com.android.systemui.R.string.interruption_level_none);
            } else if (zen == 1) {
                i4 = com.android.systemui.R.drawable.stat_sys_dnd;
                string2 = this.mResources.getString(com.android.systemui.R.string.interruption_level_priority);
            } else {
                string = null;
                z = false;
                i = 0;
            }
            string = string2;
            i = i4;
            z = true;
        }
        if (ZenModeConfig.isZenOverridingRinger(zen, this.mZenController.getConsolidatedPolicy()) || (value = this.mRingerModeTracker.getRingerModeInternal().getValue()) == null) {
            i2 = 0;
        } else {
            if (value.intValue() == 1) {
                i3 = com.android.systemui.R.drawable.stat_sys_ringer_vibrate;
                string3 = this.mResources.getString(com.android.systemui.R.string.accessibility_ringer_vibrate);
            } else {
                if (value.intValue() == 0) {
                    i3 = com.android.systemui.R.drawable.stat_sys_ringer_silent;
                    string3 = this.mResources.getString(com.android.systemui.R.string.accessibility_ringer_silent);
                }
                i2 = 0;
            }
            i2 = i3;
            z2 = true;
        }
        if (z) {
            this.mIconController.setIcon(this.mSlotZen, i, string);
        }
        if (z != this.mZenVisible) {
            this.mIconController.setIconVisibility(this.mSlotZen, z);
            this.mZenVisible = z;
        }
        if (z2) {
            this.mIconController.setIcon(this.mSlotVolume, i2, string3);
        }
        if (z2 != this.mVolumeVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, z2);
            this.mVolumeVisible = z2;
        }
        updateAlarm();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothDevicesChanged() throws Resources.NotFoundException {
        updateBluetooth();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothStateChange(boolean z) throws Resources.NotFoundException {
        updateBluetooth();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateBluetooth() throws Resources.NotFoundException {
        int i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected;
        String string = this.mResources.getString(com.android.systemui.R.string.accessibility_quick_settings_bluetooth_on);
        BluetoothController bluetoothController = this.mBluetooth;
        boolean zIsBluetoothEnabled = false;
        if (bluetoothController != null && bluetoothController.isBluetoothConnected() && (this.mBluetooth.isBluetoothAudioActive() || !this.mBluetooth.isBluetoothAudioProfileOnly())) {
            List<CachedBluetoothDevice> connectedDevices = this.mBluetooth.getConnectedDevices();
            int batteryLevel = (connectedDevices.isEmpty() || !this.mShowBluetoothBattery) ? -1 : connectedDevices.get(0).getBatteryLevel();
            if (batteryLevel == 100) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_9;
            } else if (batteryLevel >= 90) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_8;
            } else if (batteryLevel >= 80) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_7;
            } else if (batteryLevel >= 70) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_6;
            } else if (batteryLevel >= 60) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_5;
            } else if (batteryLevel >= 50) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_4;
            } else if (batteryLevel >= 40) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_3;
            } else if (batteryLevel >= 30) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_2;
            } else if (batteryLevel >= 20) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_1;
            } else if (batteryLevel >= 10) {
                i = com.android.systemui.R.drawable.stat_sys_data_bluetooth_connected_battery_0;
            }
            string = this.mResources.getString(com.android.systemui.R.string.accessibility_bluetooth_connected);
            zIsBluetoothEnabled = this.mBluetooth.isBluetoothEnabled();
        }
        this.mIconController.setIcon(this.mSlotBluetooth, i, string);
        this.mIconController.setIconVisibility(this.mSlotBluetooth, zIsBluetoothEnabled);
    }

    private final void updateTTY() {
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager == null) {
            updateTTY(0);
        } else {
            updateTTY(telecomManager.getCurrentTtyMode());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateTTY(int i) {
        boolean z = i != 0;
        boolean z2 = DEBUG;
        if (z2) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + z);
        }
        if (z) {
            if (z2) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
            }
            this.mIconController.setIcon(this.mSlotTty, com.android.systemui.R.drawable.stat_sys_tty_mode, this.mResources.getString(com.android.systemui.R.string.accessibility_tty_enabled));
            this.mIconController.setIconVisibility(this.mSlotTty, true);
            return;
        }
        if (z2) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
        }
        this.mIconController.setIconVisibility(this.mSlotTty, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCast() {
        boolean z;
        Iterator<CastController.CastDevice> it = this.mCast.getCastDevices().iterator();
        while (it.hasNext()) {
            int i = it.next().state;
            if (i == 1 || i == 2) {
                z = true;
                break;
            }
        }
        z = false;
        boolean z2 = DEBUG;
        if (z2) {
            Log.v("PhoneStatusBarPolicy", "updateCast: isCasting: " + z);
        }
        this.mHandler.removeCallbacks(this.mRemoveCastIconRunnable);
        if (z && !this.mRecordingController.isRecording()) {
            this.mIconController.setIcon(this.mSlotCast, com.android.systemui.R.drawable.stat_sys_cast, this.mResources.getString(com.android.systemui.R.string.accessibility_casting));
            this.mIconController.setIconVisibility(this.mSlotCast, true);
        } else {
            if (z2) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon in 3 sec...");
            }
            this.mHandler.postDelayed(this.mRemoveCastIconRunnable, 3000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateManagedProfile() {
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateManagedProfile$2();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateManagedProfile$2() {
        try {
            final boolean zIsManagedProfile = this.mUserManager.isManagedProfile(ActivityTaskManager.getService().getLastResumedActivityUserId());
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda8
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$updateManagedProfile$1(zIsManagedProfile);
                }
            });
        } catch (RemoteException e) {
            Log.w("PhoneStatusBarPolicy", "updateManagedProfile: ", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateManagedProfile$1(boolean z) {
        boolean z2;
        if (!z || (this.mKeyguardStateController.isShowing() && !this.mKeyguardStateController.isOccluded())) {
            z2 = false;
        } else {
            z2 = true;
            this.mIconController.setIcon(this.mSlotManagedProfile, com.android.systemui.R.drawable.stat_sys_managed_profile_status, this.mResources.getString(com.android.systemui.R.string.accessibility_managed_profile));
        }
        if (this.mManagedProfileIconVisible != z2) {
            this.mIconController.setIconVisibility(this.mSlotManagedProfile, z2);
            this.mManagedProfileIconVisible = z2;
        }
    }

    private void updateFirewall() {
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateFirewall$4();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateFirewall$4() {
        try {
            int lastResumedActivityUid = ActivityTaskManager.getService().getLastResumedActivityUid();
            final boolean z = false;
            final boolean zIsUidNetworkingBlocked = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy")).isUidNetworkingBlocked(lastResumedActivityUid, false);
            ArrayList arrayList = new ArrayList();
            AppGlobals.getPackageManager().getHomeActivities(arrayList);
            Iterator it = arrayList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (lastResumedActivityUid == ((ResolveInfo) it.next()).activityInfo.applicationInfo.uid) {
                    z = true;
                    break;
                }
            }
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$updateFirewall$3(z, zIsUidNetworkingBlocked);
                }
            });
        } catch (RemoteException e) {
            Log.w("PhoneStatusBarPolicy", "updateFirewall: ", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateFirewall$3(boolean z, boolean z2) {
        boolean z3;
        if (z || !z2 || (this.mKeyguardStateController.isShowing() && !this.mKeyguardStateController.isOccluded())) {
            z3 = false;
        } else {
            z3 = true;
            this.mIconController.setIcon(this.mSlotFirewall, com.android.systemui.R.drawable.stat_sys_firewall, null);
        }
        if (this.mFirewallVisible != z3) {
            this.mIconController.setIconVisibility(this.mSlotFirewall, z3);
            this.mFirewallVisible = z3;
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$1, reason: invalid class name */
    class AnonymousClass1 extends SynchronousUserSwitchObserver {
        AnonymousClass1() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onUserSwitching$0() {
            PhoneStatusBarPolicy.this.mUserInfoController.reloadUserInfo();
        }

        public void onUserSwitching(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$1$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onUserSwitching$0();
                }
            });
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onUserSwitchComplete$1();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onUserSwitchComplete$1() {
            PhoneStatusBarPolicy.this.updateAlarm();
            PhoneStatusBarPolicy.this.updateManagedProfile();
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$5, reason: invalid class name */
    class AnonymousClass5 implements SensorPrivacyController.OnSensorPrivacyChangedListener {
        AnonymousClass5() {
        }

        @Override // com.android.systemui.statusbar.policy.SensorPrivacyController.OnSensorPrivacyChangedListener
        public void onSensorPrivacyChanged(final boolean z) {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$5$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onSensorPrivacyChanged$0(z);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onSensorPrivacyChanged$0(boolean z) {
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotSensorsOff, z);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        if (this.mDisplayId == i) {
            updateManagedProfile();
            updateFirewall();
        }
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        updateManagedProfile();
        updateFirewall();
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
    public void onUserSetupChanged() {
        DeviceProvisionedController deviceProvisionedController = this.mProvisionedController;
        boolean zIsUserSetup = deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser());
        if (this.mCurrentUserSetup == zIsUserSetup) {
            return;
        }
        this.mCurrentUserSetup = zIsUserSetup;
        updateAlarm();
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
    public void onRotationLockStateChanged(boolean z, boolean z2) {
        boolean zIsCurrentOrientationLockPortrait = RotationLockTile.isCurrentOrientationLockPortrait(this.mRotationLockController, this.mResources);
        if (z) {
            if (zIsCurrentOrientationLockPortrait) {
                this.mIconController.setIcon(this.mSlotRotate, com.android.systemui.R.drawable.stat_sys_rotate_portrait, this.mResources.getString(com.android.systemui.R.string.accessibility_rotation_lock_on_portrait));
            } else {
                this.mIconController.setIcon(this.mSlotRotate, com.android.systemui.R.drawable.stat_sys_rotate_landscape, this.mResources.getString(com.android.systemui.R.string.accessibility_rotation_lock_on_landscape));
            }
            this.mIconController.setIconVisibility(this.mSlotRotate, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeadsetPlug(Intent intent) throws Resources.NotFoundException {
        int i;
        boolean z = intent.getIntExtra("state", 0) != 0;
        boolean z2 = intent.getIntExtra("microphone", 0) != 0;
        if (z) {
            Resources resources = this.mResources;
            if (z2) {
                i = com.android.systemui.R.string.accessibility_status_bar_headset;
            } else {
                i = com.android.systemui.R.string.accessibility_status_bar_headphones;
            }
            this.mIconController.setIcon(this.mSlotHeadset, z2 ? com.android.systemui.R.drawable.stat_sys_headset_mic : com.android.systemui.R.drawable.stat_sys_headset, resources.getString(i));
            this.mIconController.setIconVisibility(this.mSlotHeadset, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, z);
    }

    @Override // com.android.systemui.privacy.PrivacyItemController.Callback
    public void onPrivacyItemsChanged(List<PrivacyItem> list) {
        updatePrivacyItems(list);
    }

    private void updatePrivacyItems(List<PrivacyItem> list) {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        for (PrivacyItem privacyItem : list) {
            if (privacyItem == null) {
                if (DEBUG) {
                    Log.e("PhoneStatusBarPolicy", "updatePrivacyItems - null item found");
                    StringWriter stringWriter = new StringWriter();
                    this.mPrivacyItemController.dump(null, new PrintWriter(stringWriter), null);
                    Log.e("PhoneStatusBarPolicy", stringWriter.toString());
                }
            } else {
                int i = AnonymousClass8.$SwitchMap$com$android$systemui$privacy$PrivacyType[privacyItem.getPrivacyType().ordinal()];
                if (i == 1) {
                    z = true;
                } else if (i == 2) {
                    z3 = true;
                } else if (i == 3) {
                    z2 = true;
                }
            }
        }
        this.mIconController.setIconVisibility(this.mSlotCamera, z);
        this.mIconController.setIconVisibility(this.mSlotMicrophone, z2);
        if (this.mPrivacyItemController.getAllIndicatorsAvailable()) {
            this.mIconController.setIconVisibility(this.mSlotLocation, z3);
        }
    }

    /* renamed from: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$8, reason: invalid class name */
    static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$privacy$PrivacyType;

        static {
            int[] iArr = new int[PrivacyType.values().length];
            $SwitchMap$com$android$systemui$privacy$PrivacyType = iArr;
            try {
                iArr[PrivacyType.TYPE_CAMERA.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$privacy$PrivacyType[PrivacyType.TYPE_LOCATION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$systemui$privacy$PrivacyType[PrivacyType.TYPE_MICROPHONE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.LocationController.LocationChangeCallback
    public void onLocationActiveChanged(boolean z) {
        if (this.mPrivacyItemController.getAllIndicatorsAvailable()) {
            return;
        }
        updateLocationFromController();
    }

    private void updateLocationFromController() {
        if (this.mLocationController.isLocationActive()) {
            this.mIconController.setIconVisibility(this.mSlotLocation, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotLocation, false);
        }
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onCountdown(long j) {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: countdown " + j);
        }
        int iFloorDiv = (int) Math.floorDiv(j + 500, 1000L);
        int i = com.android.systemui.R.drawable.stat_sys_screen_record;
        String string = Integer.toString(iFloorDiv);
        if (iFloorDiv == 1) {
            i = com.android.systemui.R.drawable.stat_sys_screen_record_1;
        } else if (iFloorDiv == 2) {
            i = com.android.systemui.R.drawable.stat_sys_screen_record_2;
        } else if (iFloorDiv == 3) {
            i = com.android.systemui.R.drawable.stat_sys_screen_record_3;
        }
        this.mIconController.setIcon(this.mSlotScreenRecord, i, string);
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, true);
        this.mIconController.setIconAccessibilityLiveRegion(this.mSlotScreenRecord, 2);
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onCountdownEnd() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: hiding icon during countdown");
        }
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onCountdownEnd$5();
            }
        });
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onCountdownEnd$6();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCountdownEnd$5() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCountdownEnd$6() {
        this.mIconController.setIconAccessibilityLiveRegion(this.mSlotScreenRecord, 0);
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onRecordingStart() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: showing icon");
        }
        this.mIconController.setIcon(this.mSlotScreenRecord, com.android.systemui.R.drawable.stat_sys_screen_record, this.mResources.getString(com.android.systemui.R.string.screenrecord_ongoing_screen_only));
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onRecordingStart$7();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onRecordingStart$7() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, true);
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onRecordingEnd() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: hiding icon");
        }
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onRecordingEnd$8();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onRecordingEnd$8() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
    }
}
