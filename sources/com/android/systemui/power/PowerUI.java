package com.android.systemui.power;

import android.R;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IThermalEventListener;
import android.os.IThermalService;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Temperature;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.fuelgauge.Estimate;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Future;

/* loaded from: classes.dex */
public class PowerUI extends SystemUI implements CommandQueue.Callbacks {
    private static final long SIX_HOURS_MILLIS = Duration.ofHours(6).toMillis();

    @VisibleForTesting
    int mBatteryLevel;

    @VisibleForTesting
    int mBatteryStatus;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CommandQueue mCommandQueue;

    @VisibleForTesting
    BatteryStateSnapshot mCurrentBatteryStateSnapshot;
    private boolean mEnableSkinTemperatureWarning;
    private boolean mEnableUsbTemperatureAlarm;
    private EnhancedEstimates mEnhancedEstimates;
    private final Handler mHandler;
    private int mInvalidCharger;

    @VisibleForTesting
    BatteryStateSnapshot mLastBatteryStateSnapshot;
    private final Configuration mLastConfiguration;
    private Future mLastShowWarningTask;
    private int mLowBatteryAlertCloseLevel;
    private final int[] mLowBatteryReminderLevels;

    @VisibleForTesting
    boolean mLowWarningShownThisChargeCycle;
    private InattentiveSleepWarningView mOverlayView;
    private int mPlugType;
    private PowerManager mPowerManager;

    @VisibleForTesting
    final Receiver mReceiver;
    private long mScreenOffTime;

    @VisibleForTesting
    boolean mSevereWarningShownThisChargeCycle;
    private IThermalEventListener mSkinThermalEventListener;
    private final Lazy<StatusBar> mStatusBarLazy;

    @VisibleForTesting
    IThermalService mThermalService;
    private IThermalEventListener mUsbThermalEventListener;
    private WarningsUI mWarnings;

    public interface WarningsUI {
        void dismissHighTemperatureWarning();

        void dismissInvalidChargerWarning();

        void dismissLowBatteryWarning();

        void dump(PrintWriter printWriter);

        boolean isInvalidChargerWarningShowing();

        void showHighTemperatureWarning();

        void showInvalidChargerWarning();

        void showLowBatteryWarning(boolean z);

        void showThermalShutdownWarning();

        void showUsbHighTemperatureAlarm();

        void update(int i, int i2, long j);

        void updateLowBatteryWarning();

        void updateSnapshot(BatteryStateSnapshot batteryStateSnapshot);

        void userSwitched();
    }

    public PowerUI(Context context, BroadcastDispatcher broadcastDispatcher, CommandQueue commandQueue, Lazy<StatusBar> lazy) {
        super(context);
        this.mHandler = new Handler();
        this.mReceiver = new Receiver();
        this.mLastConfiguration = new Configuration();
        this.mPlugType = 0;
        this.mInvalidCharger = 0;
        this.mLowBatteryReminderLevels = new int[2];
        this.mScreenOffTime = -1L;
        this.mBatteryLevel = 100;
        this.mBatteryStatus = 1;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mCommandQueue = commandQueue;
        this.mStatusBarLazy = lazy;
    }

    @Override // com.android.systemui.SystemUI
    public void start() throws Resources.NotFoundException, Settings.SettingNotFoundException {
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mPowerManager = powerManager;
        this.mScreenOffTime = powerManager.isScreenOn() ? -1L : SystemClock.elapsedRealtime();
        this.mWarnings = (WarningsUI) Dependency.get(WarningsUI.class);
        this.mEnhancedEstimates = (EnhancedEstimates) Dependency.get(EnhancedEstimates.class);
        this.mLastConfiguration.setTo(this.mContext.getResources().getConfiguration());
        ContentObserver contentObserver = new ContentObserver(this.mHandler) { // from class: com.android.systemui.power.PowerUI.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) throws Resources.NotFoundException {
                PowerUI.this.updateBatteryWarningLevels();
            }
        };
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, contentObserver, -1);
        updateBatteryWarningLevels();
        this.mReceiver.init();
        showWarnOnThermalShutdown();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("show_temperature_warning"), false, new ContentObserver(this.mHandler) { // from class: com.android.systemui.power.PowerUI.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                PowerUI.this.doSkinThermalEventListenerRegistration();
            }
        });
        contentResolver.registerContentObserver(Settings.Global.getUriFor("show_usb_temperature_alarm"), false, new ContentObserver(this.mHandler) { // from class: com.android.systemui.power.PowerUI.3
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                PowerUI.this.doUsbThermalEventListenerRegistration();
            }
        });
        initThermalEventListeners();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.SystemUI
    protected void onConfigurationChanged(Configuration configuration) {
        if ((this.mLastConfiguration.updateFrom(configuration) & 3) != 0) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.power.PowerUI$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.initThermalEventListeners();
                }
            });
        }
    }

    void updateBatteryWarningLevels() throws Resources.NotFoundException {
        int integer = this.mContext.getResources().getInteger(R.integer.config_bg_current_drain_types_to_restricted_bucket);
        int integer2 = this.mContext.getResources().getInteger(R.integer.config_displayWhiteBalanceColorTemperatureMax);
        if (integer2 < integer) {
            integer2 = integer;
        }
        int[] iArr = this.mLowBatteryReminderLevels;
        iArr[0] = integer2;
        iArr[1] = integer;
        this.mLowBatteryAlertCloseLevel = iArr[0] + this.mContext.getResources().getInteger(R.integer.config_displayWhiteBalanceColorTemperatureFilterHorizon);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int findBatteryLevelBucket(int i) {
        if (i >= this.mLowBatteryAlertCloseLevel) {
            return 1;
        }
        int[] iArr = this.mLowBatteryReminderLevels;
        if (i > iArr[0]) {
            return 0;
        }
        for (int length = iArr.length - 1; length >= 0; length--) {
            if (i <= this.mLowBatteryReminderLevels[length]) {
                return (-1) - length;
            }
        }
        throw new RuntimeException("not possible!");
    }

    @VisibleForTesting
    final class Receiver extends BroadcastReceiver {
        private boolean mHasReceivedBattery = false;

        Receiver() {
        }

        public void init() {
            Intent intentRegisterReceiver;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            PowerUI.this.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, PowerUI.this.mHandler);
            if (this.mHasReceivedBattery || (intentRegisterReceiver = ((SystemUI) PowerUI.this).mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) == null) {
                return;
            }
            onReceive(((SystemUI) PowerUI.this).mContext, intentRegisterReceiver);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(action)) {
                ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.systemui.power.PowerUI$Receiver$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onReceive$0();
                    }
                });
                return;
            }
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                this.mHasReceivedBattery = true;
                PowerUI powerUI = PowerUI.this;
                int i = powerUI.mBatteryLevel;
                powerUI.mBatteryLevel = intent.getIntExtra("level", 100);
                PowerUI powerUI2 = PowerUI.this;
                int i2 = powerUI2.mBatteryStatus;
                powerUI2.mBatteryStatus = intent.getIntExtra("status", 1);
                int unused = PowerUI.this.mPlugType;
                PowerUI.this.mPlugType = intent.getIntExtra("plugged", 1);
                int i3 = PowerUI.this.mInvalidCharger;
                PowerUI.this.mInvalidCharger = intent.getIntExtra("invalid_charger", 0);
                PowerUI powerUI3 = PowerUI.this;
                powerUI3.mLastBatteryStateSnapshot = powerUI3.mCurrentBatteryStateSnapshot;
                final boolean z = powerUI3.mPlugType != 0;
                PowerUI.this.findBatteryLevelBucket(i);
                PowerUI powerUI4 = PowerUI.this;
                final int iFindBatteryLevelBucket = powerUI4.findBatteryLevelBucket(powerUI4.mBatteryLevel);
                WarningsUI warningsUI = PowerUI.this.mWarnings;
                PowerUI powerUI5 = PowerUI.this;
                warningsUI.update(powerUI5.mBatteryLevel, iFindBatteryLevelBucket, powerUI5.mScreenOffTime);
                if (i3 != 0 || PowerUI.this.mInvalidCharger == 0) {
                    if (i3 == 0 || PowerUI.this.mInvalidCharger != 0) {
                        if (PowerUI.this.mWarnings.isInvalidChargerWarningShowing()) {
                            return;
                        }
                    } else {
                        PowerUI.this.mWarnings.dismissInvalidChargerWarning();
                    }
                    if (PowerUI.this.mLastShowWarningTask != null) {
                        PowerUI.this.mLastShowWarningTask.cancel(true);
                    }
                    PowerUI.this.mLastShowWarningTask = ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.systemui.power.PowerUI$Receiver$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onReceive$1(z, iFindBatteryLevelBucket);
                        }
                    });
                    return;
                }
                Slog.d("PowerUI", "showing invalid charger warning");
                PowerUI.this.mWarnings.showInvalidChargerWarning();
                return;
            }
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                PowerUI.this.mScreenOffTime = SystemClock.elapsedRealtime();
                return;
            }
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                PowerUI.this.mScreenOffTime = -1L;
                return;
            }
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                PowerUI.this.mWarnings.userSwitched();
                return;
            }
            Slog.w("PowerUI", "unknown intent: " + intent);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$0() {
            if (PowerUI.this.mPowerManager.isPowerSaveMode()) {
                PowerUI.this.mWarnings.dismissLowBatteryWarning();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$1(boolean z, int i) {
            PowerUI.this.maybeShowBatteryWarningV2(z, i);
        }
    }

    protected void maybeShowBatteryWarningV2(boolean z, int i) {
        boolean zIsHybridNotificationEnabled = this.mEnhancedEstimates.isHybridNotificationEnabled();
        boolean zIsPowerSaveMode = this.mPowerManager.isPowerSaveMode();
        if (zIsHybridNotificationEnabled) {
            Estimate estimateRefreshEstimateIfNeeded = refreshEstimateIfNeeded();
            int i2 = this.mBatteryLevel;
            int i3 = this.mBatteryStatus;
            int[] iArr = this.mLowBatteryReminderLevels;
            this.mCurrentBatteryStateSnapshot = new BatteryStateSnapshot(i2, zIsPowerSaveMode, z, i, i3, iArr[1], iArr[0], estimateRefreshEstimateIfNeeded.getEstimateMillis(), estimateRefreshEstimateIfNeeded.getAverageDischargeTime(), this.mEnhancedEstimates.getSevereWarningThreshold(), this.mEnhancedEstimates.getLowWarningThreshold(), estimateRefreshEstimateIfNeeded.isBasedOnUsage(), this.mEnhancedEstimates.getLowWarningEnabled());
        } else {
            int i4 = this.mBatteryLevel;
            int i5 = this.mBatteryStatus;
            int[] iArr2 = this.mLowBatteryReminderLevels;
            this.mCurrentBatteryStateSnapshot = new BatteryStateSnapshot(i4, zIsPowerSaveMode, z, i, i5, iArr2[1], iArr2[0]);
        }
        this.mWarnings.updateSnapshot(this.mCurrentBatteryStateSnapshot);
        if (this.mCurrentBatteryStateSnapshot.isHybrid()) {
            maybeShowHybridWarning(this.mCurrentBatteryStateSnapshot, this.mLastBatteryStateSnapshot);
        } else {
            maybeShowBatteryWarning(this.mCurrentBatteryStateSnapshot, this.mLastBatteryStateSnapshot);
        }
    }

    @VisibleForTesting
    Estimate refreshEstimateIfNeeded() {
        BatteryStateSnapshot batteryStateSnapshot = this.mLastBatteryStateSnapshot;
        if (batteryStateSnapshot == null || batteryStateSnapshot.getTimeRemainingMillis() == -1 || this.mBatteryLevel != this.mLastBatteryStateSnapshot.getBatteryLevel()) {
            return this.mEnhancedEstimates.getEstimate();
        }
        return new Estimate(this.mLastBatteryStateSnapshot.getTimeRemainingMillis(), this.mLastBatteryStateSnapshot.isBasedOnUsage(), this.mLastBatteryStateSnapshot.getAverageTimeToDischargeMillis());
    }

    @VisibleForTesting
    void maybeShowHybridWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        long timeRemainingMillis = batteryStateSnapshot.getTimeRemainingMillis();
        if (batteryStateSnapshot.getBatteryLevel() >= 45 && (timeRemainingMillis > SIX_HOURS_MILLIS || timeRemainingMillis == -1)) {
            this.mLowWarningShownThisChargeCycle = false;
            this.mSevereWarningShownThisChargeCycle = false;
        }
        boolean z = batteryStateSnapshot.getBucket() != batteryStateSnapshot2.getBucket() || batteryStateSnapshot2.getPlugged();
        if (shouldShowHybridWarning(batteryStateSnapshot)) {
            this.mWarnings.showLowBatteryWarning(z);
            if ((timeRemainingMillis != -1 && timeRemainingMillis <= batteryStateSnapshot.getSevereThresholdMillis()) || batteryStateSnapshot.getBatteryLevel() <= batteryStateSnapshot.getSevereLevelThreshold()) {
                this.mSevereWarningShownThisChargeCycle = true;
                this.mLowWarningShownThisChargeCycle = true;
                return;
            } else {
                Slog.d("PowerUI", "Low warning marked as shown this cycle");
                this.mLowWarningShownThisChargeCycle = true;
                return;
            }
        }
        if (shouldDismissHybridWarning(batteryStateSnapshot)) {
            this.mWarnings.dismissLowBatteryWarning();
        } else {
            this.mWarnings.updateLowBatteryWarning();
        }
    }

    @VisibleForTesting
    boolean shouldShowHybridWarning(BatteryStateSnapshot batteryStateSnapshot) {
        if (batteryStateSnapshot.getBatteryStatus() == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("can't show warning due to - plugged: ");
            sb.append(batteryStateSnapshot.getPlugged());
            sb.append(" status unknown: ");
            sb.append(batteryStateSnapshot.getBatteryStatus() == 1);
            Slog.d("PowerUI", sb.toString());
            return false;
        }
        long timeRemainingMillis = batteryStateSnapshot.getTimeRemainingMillis();
        return (batteryStateSnapshot.isLowWarningEnabled() && !this.mLowWarningShownThisChargeCycle && !batteryStateSnapshot.isPowerSaver() && (((timeRemainingMillis > (-1L) ? 1 : (timeRemainingMillis == (-1L) ? 0 : -1)) != 0 && (timeRemainingMillis > batteryStateSnapshot.getLowThresholdMillis() ? 1 : (timeRemainingMillis == batteryStateSnapshot.getLowThresholdMillis() ? 0 : -1)) < 0) || batteryStateSnapshot.getBatteryLevel() <= batteryStateSnapshot.getLowLevelThreshold())) || (!this.mSevereWarningShownThisChargeCycle && (((timeRemainingMillis > (-1L) ? 1 : (timeRemainingMillis == (-1L) ? 0 : -1)) != 0 && (timeRemainingMillis > batteryStateSnapshot.getSevereThresholdMillis() ? 1 : (timeRemainingMillis == batteryStateSnapshot.getSevereThresholdMillis() ? 0 : -1)) < 0) || batteryStateSnapshot.getBatteryLevel() <= batteryStateSnapshot.getSevereLevelThreshold()));
    }

    @VisibleForTesting
    boolean shouldDismissHybridWarning(BatteryStateSnapshot batteryStateSnapshot) {
        return batteryStateSnapshot.getPlugged() || batteryStateSnapshot.getTimeRemainingMillis() > batteryStateSnapshot.getLowThresholdMillis();
    }

    protected void maybeShowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        boolean z = batteryStateSnapshot.getBucket() != batteryStateSnapshot2.getBucket() || batteryStateSnapshot2.getPlugged();
        if (shouldShowLowBatteryWarning(batteryStateSnapshot, batteryStateSnapshot2)) {
            this.mWarnings.showLowBatteryWarning(z);
        } else if (shouldDismissLowBatteryWarning(batteryStateSnapshot, batteryStateSnapshot2)) {
            this.mWarnings.dismissLowBatteryWarning();
        } else {
            this.mWarnings.updateLowBatteryWarning();
        }
    }

    @VisibleForTesting
    boolean shouldShowLowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        return (batteryStateSnapshot.getPlugged() || batteryStateSnapshot.isPowerSaver() || (batteryStateSnapshot.getBucket() >= batteryStateSnapshot2.getBucket() && !batteryStateSnapshot2.getPlugged()) || batteryStateSnapshot.getBucket() >= 0 || batteryStateSnapshot.getBatteryStatus() == 1) ? false : true;
    }

    @VisibleForTesting
    boolean shouldDismissLowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        return batteryStateSnapshot.isPowerSaver() || batteryStateSnapshot.getPlugged() || (batteryStateSnapshot.getBucket() > batteryStateSnapshot2.getBucket() && batteryStateSnapshot.getBucket() > 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initThermalEventListeners() {
        doSkinThermalEventListenerRegistration();
        doUsbThermalEventListenerRegistration();
    }

    @VisibleForTesting
    synchronized void doSkinThermalEventListenerRegistration() {
        boolean zUnregisterThermalEventListener;
        boolean z = this.mEnableSkinTemperatureWarning;
        boolean z2 = true;
        boolean z3 = Settings.Global.getInt(this.mContext.getContentResolver(), "show_temperature_warning", this.mContext.getResources().getInteger(com.android.systemui.R.integer.config_showTemperatureWarning)) != 0;
        this.mEnableSkinTemperatureWarning = z3;
        if (z3 != z) {
            try {
                if (this.mSkinThermalEventListener == null) {
                    this.mSkinThermalEventListener = new SkinThermalEventListener();
                }
                if (this.mThermalService == null) {
                    this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService("thermalservice"));
                }
                if (this.mEnableSkinTemperatureWarning) {
                    zUnregisterThermalEventListener = this.mThermalService.registerThermalEventListenerWithType(this.mSkinThermalEventListener, 3);
                } else {
                    zUnregisterThermalEventListener = this.mThermalService.unregisterThermalEventListener(this.mSkinThermalEventListener);
                }
            } catch (RemoteException e) {
                Slog.e("PowerUI", "Exception while (un)registering skin thermal event listener.", e);
                zUnregisterThermalEventListener = false;
            }
            if (!zUnregisterThermalEventListener) {
                if (this.mEnableSkinTemperatureWarning) {
                    z2 = false;
                }
                this.mEnableSkinTemperatureWarning = z2;
                Slog.e("PowerUI", "Failed to register or unregister skin thermal event listener.");
            }
        }
    }

    @VisibleForTesting
    synchronized void doUsbThermalEventListenerRegistration() {
        boolean zUnregisterThermalEventListener;
        boolean z = this.mEnableUsbTemperatureAlarm;
        boolean z2 = true;
        boolean z3 = Settings.Global.getInt(this.mContext.getContentResolver(), "show_usb_temperature_alarm", this.mContext.getResources().getInteger(com.android.systemui.R.integer.config_showUsbPortAlarm)) != 0;
        this.mEnableUsbTemperatureAlarm = z3;
        if (z3 != z) {
            try {
                if (this.mUsbThermalEventListener == null) {
                    this.mUsbThermalEventListener = new UsbThermalEventListener();
                }
                if (this.mThermalService == null) {
                    this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService("thermalservice"));
                }
                if (this.mEnableUsbTemperatureAlarm) {
                    zUnregisterThermalEventListener = this.mThermalService.registerThermalEventListenerWithType(this.mUsbThermalEventListener, 4);
                } else {
                    zUnregisterThermalEventListener = this.mThermalService.unregisterThermalEventListener(this.mUsbThermalEventListener);
                }
            } catch (RemoteException e) {
                Slog.e("PowerUI", "Exception while (un)registering usb thermal event listener.", e);
                zUnregisterThermalEventListener = false;
            }
            if (!zUnregisterThermalEventListener) {
                if (this.mEnableUsbTemperatureAlarm) {
                    z2 = false;
                }
                this.mEnableUsbTemperatureAlarm = z2;
                Slog.e("PowerUI", "Failed to register or unregister usb thermal event listener.");
            }
        }
    }

    private void showWarnOnThermalShutdown() throws Settings.SettingNotFoundException {
        int i = -1;
        int i2 = this.mContext.getSharedPreferences("powerui_prefs", 0).getInt("boot_count", -1);
        try {
            i = Settings.Global.getInt(this.mContext.getContentResolver(), "boot_count");
        } catch (Settings.SettingNotFoundException unused) {
            Slog.e("PowerUI", "Failed to read system boot count from Settings.Global.BOOT_COUNT");
        }
        if (i > i2) {
            this.mContext.getSharedPreferences("powerui_prefs", 0).edit().putInt("boot_count", i).apply();
            if (this.mPowerManager.getLastShutdownReason() == 4) {
                this.mWarnings.showThermalShutdownWarning();
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showInattentiveSleepWarning() {
        if (this.mOverlayView == null) {
            this.mOverlayView = new InattentiveSleepWarningView(this.mContext);
        }
        this.mOverlayView.show();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void dismissInattentiveSleepWarning(boolean z) {
        InattentiveSleepWarningView inattentiveSleepWarningView = this.mOverlayView;
        if (inattentiveSleepWarningView != null) {
            inattentiveSleepWarningView.dismiss(z);
        }
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("mLowBatteryAlertCloseLevel=");
        printWriter.println(this.mLowBatteryAlertCloseLevel);
        printWriter.print("mLowBatteryReminderLevels=");
        printWriter.println(Arrays.toString(this.mLowBatteryReminderLevels));
        printWriter.print("mBatteryLevel=");
        printWriter.println(Integer.toString(this.mBatteryLevel));
        printWriter.print("mBatteryStatus=");
        printWriter.println(Integer.toString(this.mBatteryStatus));
        printWriter.print("mPlugType=");
        printWriter.println(Integer.toString(this.mPlugType));
        printWriter.print("mInvalidCharger=");
        printWriter.println(Integer.toString(this.mInvalidCharger));
        printWriter.print("mScreenOffTime=");
        printWriter.print(this.mScreenOffTime);
        if (this.mScreenOffTime >= 0) {
            printWriter.print(" (");
            printWriter.print(SystemClock.elapsedRealtime() - this.mScreenOffTime);
            printWriter.print(" ago)");
        }
        printWriter.println();
        printWriter.print("soundTimeout=");
        printWriter.println(Settings.Global.getInt(this.mContext.getContentResolver(), "low_battery_sound_timeout", 0));
        printWriter.print("bucket: ");
        printWriter.println(Integer.toString(findBatteryLevelBucket(this.mBatteryLevel)));
        printWriter.print("mEnableSkinTemperatureWarning=");
        printWriter.println(this.mEnableSkinTemperatureWarning);
        printWriter.print("mEnableUsbTemperatureAlarm=");
        printWriter.println(this.mEnableUsbTemperatureAlarm);
        this.mWarnings.dump(printWriter);
    }

    @VisibleForTesting
    final class SkinThermalEventListener extends IThermalEventListener.Stub {
        SkinThermalEventListener() {
        }

        public void notifyThrottling(Temperature temperature) {
            int status = temperature.getStatus();
            if (status >= 5) {
                if (((StatusBar) PowerUI.this.mStatusBarLazy.get()).isDeviceInVrMode()) {
                    return;
                }
                PowerUI.this.mWarnings.showHighTemperatureWarning();
                Slog.d("PowerUI", "SkinThermalEventListener: notifyThrottling was called , current skin status = " + status + ", temperature = " + temperature.getValue());
                return;
            }
            PowerUI.this.mWarnings.dismissHighTemperatureWarning();
        }
    }

    @VisibleForTesting
    final class UsbThermalEventListener extends IThermalEventListener.Stub {
        UsbThermalEventListener() {
        }

        public void notifyThrottling(Temperature temperature) {
            int status = temperature.getStatus();
            if (status >= 5) {
                PowerUI.this.mWarnings.showUsbHighTemperatureAlarm();
                Slog.d("PowerUI", "UsbThermalEventListener: notifyThrottling was called , current usb port status = " + status + ", temperature = " + temperature.getValue());
            }
        }
    }
}
