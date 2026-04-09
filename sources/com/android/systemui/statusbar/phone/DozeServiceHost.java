package com.android.systemui.statusbar.phone;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.doze.DozeReceiver;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.tuner.TunerService;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public final class DozeServiceHost implements DozeHost, TunerService.Tunable {
    private View mAmbientIndicationContainer;
    private boolean mAnimateScreenOff;
    private boolean mAnimateWakeup;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final BatteryController mBatteryController;
    private final Lazy<BiometricUnlockController> mBiometricUnlockControllerLazy;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private final DozeLog mDozeLog;
    private final DozeScrimController mDozeScrimController;
    private boolean mDozingRequested;
    private final HeadsUpManagerPhone mHeadsUpManagerPhone;
    private boolean mIgnoreTouchWhilePulsing;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private final LockscreenLockIconController mLockscreenLockIconController;
    private NotificationIconAreaController mNotificationIconAreaController;
    private NotificationPanelViewController mNotificationPanel;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    private final NotificationWakeUpCoordinator mNotificationWakeUpCoordinator;
    private Runnable mPendingScreenOffCallback;
    private final PowerManager mPowerManager;
    private final PulseExpansionHandler mPulseExpansionHandler;
    private boolean mPulsing;
    private final ScrimController mScrimController;
    private StatusBar mStatusBar;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private boolean mSuppressed;
    private final TunerService mTunerService;
    private final VisualStabilityManager mVisualStabilityManager;
    private WakefulnessLifecycle mWakefulnessLifecycle;
    private final ArrayList<DozeHost.Callback> mCallbacks = new ArrayList<>();

    @VisibleForTesting
    boolean mWakeLockScreenPerformsAuth = SystemProperties.getBoolean("persist.sysui.wake_performs_auth", true);
    private boolean mIsAmbientSwipeEnabled = true;

    public DozeServiceHost(DozeLog dozeLog, PowerManager powerManager, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, DeviceProvisionedController deviceProvisionedController, HeadsUpManagerPhone headsUpManagerPhone, BatteryController batteryController, ScrimController scrimController, Lazy<BiometricUnlockController> lazy, KeyguardViewMediator keyguardViewMediator, Lazy<AssistManager> lazy2, DozeScrimController dozeScrimController, KeyguardUpdateMonitor keyguardUpdateMonitor, VisualStabilityManager visualStabilityManager, PulseExpansionHandler pulseExpansionHandler, NotificationShadeWindowController notificationShadeWindowController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, LockscreenLockIconController lockscreenLockIconController, TunerService tunerService) {
        this.mDozeLog = dozeLog;
        this.mPowerManager = powerManager;
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mHeadsUpManagerPhone = headsUpManagerPhone;
        this.mBatteryController = batteryController;
        this.mScrimController = scrimController;
        this.mBiometricUnlockControllerLazy = lazy;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mAssistManagerLazy = lazy2;
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mNotificationWakeUpCoordinator = notificationWakeUpCoordinator;
        this.mLockscreenLockIconController = lockscreenLockIconController;
        this.mTunerService = tunerService;
    }

    public void initialize(StatusBar statusBar, NotificationIconAreaController notificationIconAreaController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, NotificationShadeWindowViewController notificationShadeWindowViewController, NotificationPanelViewController notificationPanelViewController, View view) {
        this.mStatusBar = statusBar;
        this.mNotificationIconAreaController = notificationIconAreaController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mNotificationPanel = notificationPanelViewController;
        this.mNotificationShadeWindowViewController = notificationShadeWindowViewController;
        this.mAmbientIndicationContainer = view;
        this.mTunerService.addTunable(this, "system:ambient_swipe");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:ambient_swipe")) {
            this.mIsAmbientSwipeEnabled = TunerService.parseIntegerSwitch(str2, true);
        }
    }

    public String toString() {
        return "PSB.DozeServiceHost[mCallbacks=" + this.mCallbacks.size() + "]";
    }

    void firePowerSaveChanged(boolean z) {
        Iterator<DozeHost.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onPowerSaveChanged(z);
        }
    }

    void fireNotificationPulse(final NotificationEntry notificationEntry) {
        Runnable runnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.DozeServiceHost$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$fireNotificationPulse$0(notificationEntry);
            }
        };
        Iterator<DozeHost.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onNotificationAlerted(runnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fireNotificationPulse$0(NotificationEntry notificationEntry) {
        notificationEntry.setPulseSuppressed(true);
        this.mNotificationIconAreaController.updateAodNotificationIcons();
    }

    boolean getDozingRequested() {
        return this.mDozingRequested;
    }

    boolean isPulsing() {
        return this.mPulsing;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void addCallback(DozeHost.Callback callback) {
        this.mCallbacks.add(callback);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void removeCallback(DozeHost.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void startDozing() {
        if (this.mDozingRequested) {
            return;
        }
        this.mDozingRequested = true;
        updateDozing();
        this.mDozeLog.traceDozing(this.mStatusBarStateController.isDozing());
        this.mStatusBar.updateIsKeyguard();
    }

    void updateDozing() {
        this.mStatusBarStateController.setIsDozing(this.mBiometricUnlockControllerLazy.get().getMode() != 1 ? (this.mDozingRequested && this.mStatusBarStateController.getState() == 1) || this.mBiometricUnlockControllerLazy.get().getMode() == 2 : false);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void pulseWhileDozing(final DozeHost.PulseCallback pulseCallback, int i) {
        if (i == 5) {
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 4, "com.android.systemui:LONG_PRESS");
            this.mAssistManagerLazy.get().startAssist(new Bundle());
            return;
        }
        if (i == 8) {
            this.mScrimController.setWakeLockScreenSensorActive(true);
        }
        final boolean z = i == 8 && this.mWakeLockScreenPerformsAuth;
        this.mPulsing = true;
        this.mDozeScrimController.pulse(new DozeHost.PulseCallback() { // from class: com.android.systemui.statusbar.phone.DozeServiceHost.1
            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseStarted() {
                pulseCallback.onPulseStarted();
                if (DozeServiceHost.this.mIsAmbientSwipeEnabled) {
                    DozeServiceHost.this.mStatusBar.updateNotificationPanelTouchState();
                }
                setPulsing(true);
            }

            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseFinished() {
                DozeServiceHost.this.mPulsing = false;
                pulseCallback.onPulseFinished();
                DozeServiceHost.this.mStatusBar.updateNotificationPanelTouchState();
                DozeServiceHost.this.mScrimController.setWakeLockScreenSensorActive(false);
                setPulsing(false);
            }

            private void setPulsing(boolean z2) {
                DozeServiceHost.this.mStatusBarStateController.setPulsing(z2);
                DozeServiceHost.this.mStatusBarKeyguardViewManager.setPulsing(z2);
                DozeServiceHost.this.mKeyguardViewMediator.setPulsing(z2);
                DozeServiceHost.this.mNotificationPanel.setPulsing(z2);
                DozeServiceHost.this.mVisualStabilityManager.setPulsing(z2);
                DozeServiceHost.this.mIgnoreTouchWhilePulsing = false;
                if (DozeServiceHost.this.mKeyguardUpdateMonitor != null && z) {
                    DozeServiceHost.this.mKeyguardUpdateMonitor.onAuthInterruptDetected(z2);
                }
                DozeServiceHost.this.mStatusBar.updateScrimController();
                DozeServiceHost.this.mPulseExpansionHandler.setPulsing(z2);
                if (DozeServiceHost.this.mIsAmbientSwipeEnabled) {
                    DozeServiceHost.this.mNotificationWakeUpCoordinator.setPulsing(z2);
                }
            }
        }, i);
        this.mStatusBar.updateScrimController();
    }

    @Override // com.android.systemui.doze.DozeHost
    public void stopDozing() {
        if (this.mDozingRequested) {
            this.mDozingRequested = false;
            updateDozing();
            this.mDozeLog.traceDozing(this.mStatusBarStateController.isDozing());
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void onIgnoreTouchWhilePulsing(boolean z) {
        if (z != this.mIgnoreTouchWhilePulsing) {
            this.mDozeLog.tracePulseTouchDisabledByProx(z);
        }
        this.mIgnoreTouchWhilePulsing = z;
        if (this.mStatusBarStateController.isDozing() && z) {
            this.mNotificationShadeWindowViewController.cancelCurrentTouch();
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void dozeTimeTick() {
        this.mNotificationPanel.dozeTimeTick();
        KeyEvent.Callback callback = this.mAmbientIndicationContainer;
        if (callback instanceof DozeReceiver) {
            ((DozeReceiver) callback).dozeTimeTick();
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isPowerSaveActive() {
        return this.mBatteryController.isAodPowerSave();
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isPulsingBlocked() {
        return this.mBiometricUnlockControllerLazy.get().getMode() == 1;
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isProvisioned() {
        return this.mDeviceProvisionedController.isDeviceProvisioned() && this.mDeviceProvisionedController.isCurrentUserSetup();
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isBlockingDoze() {
        if (!this.mBiometricUnlockControllerLazy.get().hasPendingAuthentication()) {
            return false;
        }
        Log.i("StatusBar", "Blocking AOD because fingerprint has authenticated");
        return true;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void extendPulse(int i) {
        if (i == 8) {
            this.mScrimController.setWakeLockScreenSensorActive(true);
        }
        if (this.mDozeScrimController.isPulsing() && this.mHeadsUpManagerPhone.hasNotifications()) {
            this.mHeadsUpManagerPhone.extendHeadsUp();
        } else {
            this.mDozeScrimController.extendPulse();
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setAnimateWakeup(boolean z) {
        if (this.mWakefulnessLifecycle.getWakefulness() == 2 || this.mWakefulnessLifecycle.getWakefulness() == 1) {
            return;
        }
        this.mAnimateWakeup = z;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setAnimateScreenOff(boolean z) {
        this.mAnimateScreenOff = z;
    }

    @Override // com.android.systemui.doze.DozeHost
    public void onSlpiTap(float f, float f2) {
        View view;
        if (f <= 0.0f || f2 <= 0.0f || (view = this.mAmbientIndicationContainer) == null || view.getVisibility() != 0) {
            return;
        }
        this.mAmbientIndicationContainer.getLocationOnScreen(new int[2]);
        float f3 = f - r1[0];
        float f4 = f2 - r1[1];
        if (0.0f > f3 || f3 > this.mAmbientIndicationContainer.getWidth() || 0.0f > f4 || f4 > this.mAmbientIndicationContainer.getHeight()) {
            return;
        }
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        MotionEvent motionEventObtain = MotionEvent.obtain(jElapsedRealtime, jElapsedRealtime, 0, f, f2, 0);
        this.mAmbientIndicationContainer.dispatchTouchEvent(motionEventObtain);
        motionEventObtain.recycle();
        MotionEvent motionEventObtain2 = MotionEvent.obtain(jElapsedRealtime, jElapsedRealtime, 1, f, f2, 0);
        this.mAmbientIndicationContainer.dispatchTouchEvent(motionEventObtain2);
        motionEventObtain2.recycle();
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setDozeScreenBrightness(int i) {
        this.mNotificationShadeWindowController.setDozeScreenBrightness(i);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void setAodDimmingScrim(float f) {
        this.mScrimController.setAodFrontScrimAlpha(f);
    }

    @Override // com.android.systemui.doze.DozeHost
    public void prepareForGentleSleep(Runnable runnable) {
        if (this.mPendingScreenOffCallback != null) {
            Log.w("DozeServiceHost", "Overlapping onDisplayOffCallback. Ignoring previous one.");
        }
        this.mPendingScreenOffCallback = runnable;
        this.mStatusBar.updateScrimController();
    }

    @Override // com.android.systemui.doze.DozeHost
    public void cancelGentleSleep() {
        this.mPendingScreenOffCallback = null;
        if (this.mScrimController.getState() == ScrimState.OFF) {
            this.mStatusBar.updateScrimController();
        }
    }

    boolean hasPendingScreenOffCallback() {
        return this.mPendingScreenOffCallback != null;
    }

    void executePendingScreenOffCallback() {
        Runnable runnable = this.mPendingScreenOffCallback;
        if (runnable == null) {
            return;
        }
        runnable.run();
        this.mPendingScreenOffCallback = null;
    }

    boolean shouldAnimateWakeup() {
        return this.mAnimateWakeup;
    }

    boolean shouldAnimateScreenOff() {
        return this.mAnimateScreenOff;
    }

    boolean getIgnoreTouchWhilePulsing() {
        return this.mIgnoreTouchWhilePulsing;
    }

    void setDozeSuppressed(boolean z) {
        if (z == this.mSuppressed) {
            return;
        }
        this.mSuppressed = z;
        Iterator<DozeHost.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onDozeSuppressedChanged(z);
        }
    }

    @Override // com.android.systemui.doze.DozeHost
    public boolean isDozeSuppressed() {
        return this.mSuppressed;
    }
}
