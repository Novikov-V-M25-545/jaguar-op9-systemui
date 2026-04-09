package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.UserSwitchObserver;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.biometrics.CryptoObject;
import android.hardware.biometrics.IBiometricEnabledOnKeyguardCallback;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.pocket.IPocketCallback;
import android.pocket.PocketManager;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.custom.faceunlock.FaceUnlockUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.fuelgauge.BatteryStatus;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.util.Assert;
import com.android.systemui.util.RingerModeTracker;
import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class KeyguardUpdateMonitor implements TrustManager.TrustListener, Dumpable {
    public static final boolean CORE_APPS_ONLY;
    private static final boolean DEBUG_FACE = Build.IS_DEBUGGABLE;
    private static final ComponentName FALLBACK_HOME_COMPONENT = new ComponentName("com.android.settings", "com.android.settings.FallbackHome");
    private static final boolean mCustomFaceUnlockSupported;
    private static int sCurrentUser;
    private boolean mAssistantVisible;
    private boolean mAuthInterruptActive;
    private final Executor mBackgroundExecutor;

    @VisibleForTesting
    BatteryStatus mBatteryStatus;
    private BiometricManager mBiometricManager;
    private boolean mBouncer;
    private boolean mBouncerFullyShown;

    @VisibleForTesting
    protected final BroadcastReceiver mBroadcastAllReceiver;
    private final BroadcastDispatcher mBroadcastDispatcher;

    @VisibleForTesting
    protected final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private boolean mCredentialAttempted;
    private boolean mDeviceInteractive;
    private final DevicePolicyManager mDevicePolicyManager;
    private boolean mDeviceProvisioned;
    private ContentObserver mDeviceProvisionedObserver;
    private final IDreamManager mDreamManager;
    private final boolean mFaceAuthOnlyOnSecurityView;

    @VisibleForTesting
    FaceManager.AuthenticationCallback mFaceAuthenticationCallback;
    private CancellationSignal mFaceCancelSignal;
    private ArrayDeque<KeyguardFaceListenModel> mFaceListenModels;
    private final FaceManager.LockoutResetCallback mFaceLockoutResetCallback;
    private FaceManager mFaceManager;
    private FingerprintManager.AuthenticationCallback mFingerprintAuthenticationCallback;
    private CancellationSignal mFingerprintCancelSignal;
    private final FingerprintManager.FingerprintDetectionCallback mFingerprintDetectionCallback;
    private boolean mFingerprintLockedOut;
    private final FingerprintManager.LockoutResetCallback mFingerprintLockoutResetCallback;
    private FingerprintManager mFpm;
    private boolean mGoingToSleep;
    private final Handler mHandler;
    private boolean mHasFod;
    private boolean mHasLockscreenWallpaper;
    private final boolean mIsAutomotive;
    private boolean mIsDeviceInPocket;
    private boolean mIsDreaming;
    private final boolean mIsPrimaryUser;
    private KeyguardBypassController mKeyguardBypassController;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardIsVisible;
    private boolean mKeyguardOccluded;
    Date mLastBatteryUpdate;
    private boolean mLockIconPressed;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLogoutEnabled;
    private boolean mNeedsSlowUnlockTransition;
    private int mPhoneState;
    private PocketManager mPocketManager;
    private int mRingMode;
    private RingerModeTracker mRingerModeTracker;
    private boolean mScreenOn;
    private boolean mSecureCameraLaunched;
    SettingsObserver mSettingsObserver;
    private final StatusBarStateController mStatusBarStateController;
    private StrongAuthTracker mStrongAuthTracker;
    private List<SubscriptionInfo> mSubscriptionInfo;
    private SubscriptionManager mSubscriptionManager;
    private boolean mSwitchingUser;
    private final TaskStackChangeListener mTaskStackListener;

    @VisibleForTesting
    protected boolean mTelephonyCapable;
    private TelephonyManager mTelephonyManager;
    private TrustManager mTrustManager;
    private UserManager mUserManager;
    private final UserSwitchObserver mUserSwitchObserver;
    HashMap<Integer, SimData> mSimDatas = new HashMap<>();
    HashMap<Integer, ServiceState> mServiceStates = new HashMap<>();
    HashMap<Integer, ServiceState> mServiceStatesWithKeySlot = new HashMap<>();
    private final ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mCallbacks = Lists.newArrayList();
    private int mFingerprintRunningState = 0;
    private int mFaceRunningState = 0;
    private int mActiveMobileDataSubscription = -1;
    private int mFaceUnlockBehavior = 0;
    private int mHardwareFingerprintUnavailableRetryCount = 0;
    private int mHardwareFaceUnavailableRetryCount = 0;
    private boolean mKeyguardReset = false;
    private final Runnable mFpCancelNotReceived = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda5
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };
    private final Runnable mFaceCancelNotReceived = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda2
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$1();
        }
    };
    private final Observer<Integer> mRingerModeObserver = new Observer<Integer>() { // from class: com.android.keyguard.KeyguardUpdateMonitor.1
        @Override // androidx.lifecycle.Observer
        public void onChanged(Integer num) {
            KeyguardUpdateMonitor.this.mHandler.obtainMessage(305, num.intValue(), 0).sendToTarget();
        }
    };
    private final IPocketCallback mPocketCallback = new IPocketCallback.Stub() { // from class: com.android.keyguard.KeyguardUpdateMonitor.2
        public void onStateChanged(boolean z, int i) {
            boolean z2 = KeyguardUpdateMonitor.this.mIsDeviceInPocket;
            if (i == 0) {
                KeyguardUpdateMonitor.this.mIsDeviceInPocket = z;
            } else {
                KeyguardUpdateMonitor.this.mIsDeviceInPocket = false;
            }
            if (z2 != KeyguardUpdateMonitor.this.mIsDeviceInPocket) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(600);
            }
        }
    };
    private SparseBooleanArray mFingerprintSettingEnabledForUser = new SparseBooleanArray();
    private SparseBooleanArray mFaceSettingEnabledForUser = new SparseBooleanArray();
    private IBiometricEnabledOnKeyguardCallback mBiometricEnabledCallback = new IBiometricEnabledOnKeyguardCallback.Stub() { // from class: com.android.keyguard.KeyguardUpdateMonitor.3
        public void onChanged(BiometricSourceType biometricSourceType, boolean z, int i) throws RemoteException {
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                KeyguardUpdateMonitor.this.mFingerprintSettingEnabledForUser.put(i, z);
                KeyguardUpdateMonitor.this.updateFingerprintListeningState();
                KeyguardUpdateMonitor.this.mFaceSettingEnabledForUser.put(i, z);
                KeyguardUpdateMonitor.this.updateFaceListeningState();
            }
        }
    };

    @VisibleForTesting
    public PhoneStateListener mPhoneStateListener = new PhoneStateListener() { // from class: com.android.keyguard.KeyguardUpdateMonitor.4
        @Override // android.telephony.PhoneStateListener
        public void onActiveDataSubscriptionIdChanged(int i) {
            KeyguardUpdateMonitor.this.mActiveMobileDataSubscription = i;
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = new SubscriptionManager.OnSubscriptionsChangedListener() { // from class: com.android.keyguard.KeyguardUpdateMonitor.5
        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private SparseBooleanArray mUserIsUnlocked = new SparseBooleanArray();
    private SparseBooleanArray mUserHasTrust = new SparseBooleanArray();
    private SparseBooleanArray mUserTrustIsManaged = new SparseBooleanArray();
    private SparseBooleanArray mUserTrustIsUsuallyManaged = new SparseBooleanArray();
    private SparseBooleanArray mUserFaceUnlockRunning = new SparseBooleanArray();
    private Map<Integer, Intent> mSecondaryLockscreenRequirement = new HashMap();

    @VisibleForTesting
    SparseArray<BiometricAuthenticated> mUserFingerprintAuthenticated = new SparseArray<>();

    @VisibleForTesting
    SparseArray<BiometricAuthenticated> mUserFaceAuthenticated = new SparseArray<>();
    private Runnable mUpdateBiometricListeningState = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda4
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.updateBiometricListeningState();
        }
    };
    private Runnable mRetryFingerprintAuthentication = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.7
        @Override // java.lang.Runnable
        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying fingerprint after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareFingerprintUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFingerprintListeningState();
        }
    };
    private Runnable mRetryFaceAuthentication = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.8
        @Override // java.lang.Runnable
        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying face after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareFaceUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFaceListeningState();
        }
    };
    private DisplayClientState mDisplayClientState = new DisplayClientState();

    private boolean containsFlag(int i, int i2) {
        return (i & i2) != 0;
    }

    public static boolean isSimPinSecure(int i) {
        return i == 2 || i == 3 || i == 7;
    }

    static {
        try {
            CORE_APPS_ONLY = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
            mCustomFaceUnlockSupported = FaceUnlockUtils.isFaceUnlockSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        Log.e("KeyguardUpdateMonitor", "Fp cancellation not received, transitioning to STOPPED");
        this.mFingerprintRunningState = 0;
        updateFingerprintListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1() {
        Log.e("KeyguardUpdateMonitor", "Face cancellation not received, transitioning to STOPPED");
        this.mFaceRunningState = 0;
        updateFaceListeningState();
    }

    @VisibleForTesting
    static class BiometricAuthenticated {
        private final boolean mAuthenticated;
        private final boolean mIsStrongBiometric;

        BiometricAuthenticated(boolean z, boolean z2) {
            this.mAuthenticated = z;
            this.mIsStrongBiometric = z2;
        }
    }

    public static synchronized void setCurrentUser(int i) {
        sCurrentUser = i;
    }

    public static synchronized int getCurrentUser() {
        return sCurrentUser;
    }

    public void onTrustChanged(boolean z, int i, int i2, List<String> list) {
        Assert.isMainThread();
        this.mUserHasTrust.put(i, z);
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustChanged(i);
                if (z && i2 != 0) {
                    keyguardUpdateMonitorCallback.onTrustGrantedWithFlags(i2, i);
                }
            }
        }
        if (getCurrentUser() == i && getUserHasTrust(i)) {
            String str = null;
            if (list != null && list.size() > 0) {
                str = list.get(0);
            }
            for (int i4 = 0; i4 < this.mCallbacks.size(); i4++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback2 = this.mCallbacks.get(i4).get();
                if (keyguardUpdateMonitorCallback2 != null) {
                    keyguardUpdateMonitorCallback2.showTrustGrantedMessage(str);
                }
            }
        }
    }

    public void onTrustError(CharSequence charSequence) {
        dispatchErrorMessage(charSequence);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSimSubscriptionInfoChanged() {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged()");
        List<SubscriptionInfo> completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        if (completeActiveSubscriptionInfoList != null) {
            Iterator<SubscriptionInfo> it = completeActiveSubscriptionInfoList.iterator();
            while (it.hasNext()) {
                Log.v("KeyguardUpdateMonitor", "SubInfo:" + it.next());
            }
        } else {
            Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged: list is null");
        }
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(true);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < subscriptionInfo.size(); i++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i);
            if (refreshSimState(subscriptionInfo2.getSubscriptionId(), subscriptionInfo2.getSimSlotIndex())) {
                arrayList.add(subscriptionInfo2);
            }
        }
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            SimData simData = this.mSimDatas.get(Integer.valueOf(((SubscriptionInfo) arrayList.get(i2)).getSimSlotIndex()));
            for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSimStateChanged(simData.subId, simData.slotId, simData.simState);
                }
            }
        }
        callbacksRefreshCarrierInfo();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAirplaneModeChanged() {
        callbacksRefreshCarrierInfo();
    }

    private void callbacksRefreshCarrierInfo() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    public List<SubscriptionInfo> getSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> completeActiveSubscriptionInfoList = this.mSubscriptionInfo;
        if (completeActiveSubscriptionInfoList == null || z) {
            completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        }
        if (completeActiveSubscriptionInfoList == null) {
            this.mSubscriptionInfo = new ArrayList();
        } else {
            this.mSubscriptionInfo = completeActiveSubscriptionInfoList;
        }
        return new ArrayList(this.mSubscriptionInfo);
    }

    public List<SubscriptionInfo> getFilteredSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        if (subscriptionInfo.size() == 2) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(0);
            SubscriptionInfo subscriptionInfo3 = subscriptionInfo.get(1);
            if (subscriptionInfo2.getGroupUuid() == null || !subscriptionInfo2.getGroupUuid().equals(subscriptionInfo3.getGroupUuid()) || (!subscriptionInfo2.isOpportunistic() && !subscriptionInfo3.isOpportunistic())) {
                return subscriptionInfo;
            }
            if (CarrierConfigManager.getDefaultConfig().getBoolean("always_show_primary_signal_bar_in_opportunistic_network_boolean")) {
                if (!subscriptionInfo2.isOpportunistic()) {
                    subscriptionInfo2 = subscriptionInfo3;
                }
                subscriptionInfo.remove(subscriptionInfo2);
            } else {
                if (subscriptionInfo2.getSubscriptionId() == this.mActiveMobileDataSubscription) {
                    subscriptionInfo2 = subscriptionInfo3;
                }
                subscriptionInfo.remove(subscriptionInfo2);
            }
        }
        return subscriptionInfo;
    }

    boolean isEmergencyOnly() {
        ServiceState serviceState;
        boolean z = false;
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            int[] subId = SubscriptionManager.getSubId(i);
            if (subId != null && subId.length > 0) {
                serviceState = this.mServiceStates.get(Integer.valueOf(subId[0]));
            } else {
                serviceState = this.mServiceStatesWithKeySlot.get(Integer.valueOf(i));
            }
            if (serviceState != null) {
                if (serviceState.getVoiceRegState() == 0) {
                    return false;
                }
                if (serviceState.isEmergencyOnly()) {
                    z = true;
                }
            }
        }
        return z;
    }

    int getPresentSubId() {
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            int[] subId = SubscriptionManager.getSubId(i);
            if (subId != null && subId.length > 0 && getSimState(subId[0]) != 1) {
                return subId[0];
            }
        }
        return -1;
    }

    public void onTrustManagedChanged(boolean z, int i) {
        Assert.isMainThread();
        this.mUserTrustIsManaged.put(i, z);
        this.mUserTrustIsUsuallyManaged.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustManagedChanged(i);
            }
        }
    }

    public void setCredentialAttempted() {
        this.mCredentialAttempted = true;
        updateBiometricListeningState();
    }

    public void setKeyguardGoingAway(boolean z) {
        this.mKeyguardGoingAway = z;
        updateBiometricListeningState();
    }

    public void setKeyguardOccluded(boolean z) {
        this.mKeyguardOccluded = z;
        updateBiometricListeningState();
    }

    public void onCameraLaunched() {
        this.mSecureCameraLaunched = true;
        updateBiometricListeningState();
    }

    public boolean isDreaming() {
        return this.mIsDreaming;
    }

    public void awakenFromDream() {
        IDreamManager iDreamManager;
        if (!this.mIsDreaming || (iDreamManager = this.mDreamManager) == null) {
            return;
        }
        try {
            iDreamManager.awaken();
        } catch (RemoteException unused) {
            Log.e("KeyguardUpdateMonitor", "Unable to awaken from dream");
        }
    }

    @VisibleForTesting
    protected void onFingerprintAuthenticated(int i, boolean z) {
        Assert.isMainThread();
        Trace.beginSection("KeyGuardUpdateMonitor#onFingerPrintAuthenticated");
        this.mUserFingerprintAuthenticated.put(i, new BiometricAuthenticated(true, z));
        if (getUserCanSkipBouncer(i)) {
            this.mTrustManager.unlockedByBiometricForUser(i, BiometricSourceType.FINGERPRINT);
        }
        this.mFingerprintCancelSignal = null;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthenticated(i, BiometricSourceType.FINGERPRINT, z);
            }
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(336), 500L);
        this.mAssistantVisible = false;
        reportSuccessfulBiometricUnlock(z, i);
        Trace.endSection();
    }

    private void reportSuccessfulBiometricUnlock(final boolean z, final int i) {
        this.mBackgroundExecutor.execute(new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.6
            @Override // java.lang.Runnable
            public void run() {
                KeyguardUpdateMonitor.this.mLockPatternUtils.reportSuccessfulBiometricUnlock(z, i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintAuthFailed() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthFailed(BiometricSourceType.FINGERPRINT);
            }
        }
        handleFingerprintHelp(-1, this.mContext.getString(R.string.kg_fingerprint_not_recognized));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintAcquired(int i) {
        Assert.isMainThread();
        if (i != 0) {
            return;
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAcquired(BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFingerPrintAuthenticated");
        try {
            int i2 = ActivityManager.getService().getCurrentUser().id;
            if (i2 != i) {
                Log.d("KeyguardUpdateMonitor", "Fingerprint authenticated for wrong user: " + i);
                return;
            }
            if (!isFingerprintDisabled(i2)) {
                onFingerprintAuthenticated(i2, z);
                setFingerprintRunningState(0);
                Trace.endSection();
            } else {
                Log.d("KeyguardUpdateMonitor", "Fingerprint disabled by DPM for userId: " + i2);
            }
        } catch (RemoteException e) {
            Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
        } finally {
            setFingerprintRunningState(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintHelp(int i, String str) {
        Assert.isMainThread();
        if (this.mIsDeviceInPocket && this.mHasFod) {
            return;
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricHelp(i, str, BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintError(int i, String str) {
        int i2;
        Assert.isMainThread();
        if (this.mHandler.hasCallbacks(this.mFpCancelNotReceived)) {
            this.mHandler.removeCallbacks(this.mFpCancelNotReceived);
        }
        this.mFingerprintCancelSignal = null;
        if (i == 5 && this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(0);
            updateFingerprintListeningState();
        } else {
            setFingerprintRunningState(0);
        }
        if (i == 1 && (i2 = this.mHardwareFingerprintUnavailableRetryCount) < 20) {
            this.mHardwareFingerprintUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
            this.mHandler.postDelayed(this.mRetryFingerprintAuthentication, 500L);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
        }
        if (i == 7 || i == 9) {
            this.mFingerprintLockedOut = true;
        }
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricError(i, str, BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFingerprintLockoutReset() {
        this.mFingerprintLockedOut = false;
        updateFingerprintListeningState();
    }

    private void setFingerprintRunningState(int i) {
        boolean z = this.mFingerprintRunningState == 1;
        boolean z2 = i == 1;
        this.mFingerprintRunningState = i;
        Log.d("KeyguardUpdateMonitor", "fingerprintRunningState: " + this.mFingerprintRunningState);
        if (z != z2) {
            notifyFingerprintRunningStateChanged();
        }
    }

    private void notifyFingerprintRunningStateChanged() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricRunningStateChanged(isFingerprintDetectionRunning(), BiometricSourceType.FINGERPRINT);
            }
        }
    }

    @VisibleForTesting
    protected void onFaceAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#onFaceAuthenticated");
        Assert.isMainThread();
        this.mUserFaceAuthenticated.put(i, new BiometricAuthenticated(true, z));
        if (getUserCanSkipBouncer(i)) {
            this.mTrustManager.unlockedByBiometricForUser(i, BiometricSourceType.FACE);
        }
        this.mFaceCancelSignal = null;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthenticated(i, BiometricSourceType.FACE, z);
            }
        }
        this.mAssistantVisible = false;
        reportSuccessfulBiometricUnlock(z, i);
        Trace.endSection();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceAuthFailed() {
        Assert.isMainThread();
        this.mFaceCancelSignal = null;
        setFaceRunningState(0);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthFailed(BiometricSourceType.FACE);
            }
        }
        handleFaceHelp(-2, this.mContext.getString(R.string.kg_face_not_recognized));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceAcquired(int i) {
        Assert.isMainThread();
        if (i != 0) {
            return;
        }
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face acquired");
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAcquired(BiometricSourceType.FACE);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFaceAuthenticated");
        try {
            if (this.mGoingToSleep) {
                Log.d("KeyguardUpdateMonitor", "Aborted successful auth because device is going to sleep.");
                return;
            }
            int i2 = ActivityManager.getService().getCurrentUser().id;
            if (i2 != i) {
                Log.d("KeyguardUpdateMonitor", "Face authenticated for wrong user: " + i);
                return;
            }
            if (isFaceDisabled(i2)) {
                Log.d("KeyguardUpdateMonitor", "Face authentication disabled by DPM for userId: " + i2);
                return;
            }
            if (DEBUG_FACE) {
                Log.d("KeyguardUpdateMonitor", "Face auth succeeded for user " + i2);
            }
            onFaceAuthenticated(i2, z);
            setFaceRunningState(0);
            Trace.endSection();
        } catch (RemoteException e) {
            Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
        } finally {
            setFaceRunningState(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceHelp(int i, String str) {
        Assert.isMainThread();
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face help received: " + str);
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricHelp(i, str, BiometricSourceType.FACE);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceError(int i, String str) {
        int i2;
        Assert.isMainThread();
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face error received: " + str);
        }
        if (this.mHandler.hasCallbacks(this.mFaceCancelNotReceived)) {
            this.mHandler.removeCallbacks(this.mFaceCancelNotReceived);
        }
        this.mFaceCancelSignal = null;
        if (i == 5 && this.mFaceRunningState == 3) {
            setFaceRunningState(0);
            updateFaceListeningState();
        } else {
            setFaceRunningState(0);
        }
        if ((i == 1 || i == 2) && (i2 = this.mHardwareFaceUnavailableRetryCount) < 20) {
            this.mHardwareFaceUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFaceAuthentication);
            this.mHandler.postDelayed(this.mRetryFaceAuthentication, 500L);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
        }
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricError(i, str, BiometricSourceType.FACE);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceLockoutReset() {
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateFaceListeningState();
            }
        }, 1000L);
    }

    private void setFaceRunningState(int i) {
        boolean z = this.mFaceRunningState == 1;
        boolean z2 = i == 1;
        this.mFaceRunningState = i;
        Log.d("KeyguardUpdateMonitor", "faceRunningState: " + this.mFaceRunningState);
        if (z != z2) {
            notifyFaceRunningStateChanged();
        }
    }

    private void notifyFaceRunningStateChanged() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricRunningStateChanged(isFaceDetectionRunning(), BiometricSourceType.FACE);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFaceUnlockStateChanged(boolean z, int i) {
        Assert.isMainThread();
        this.mUserFaceUnlockRunning.put(i, z);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFaceUnlockStateChanged(z, i);
            }
        }
    }

    public boolean isFingerprintDetectionRunning() {
        return this.mFingerprintRunningState == 1;
    }

    public boolean isFaceDetectionRunning() {
        return this.mFaceRunningState == 1;
    }

    private boolean isTrustDisabled(int i) {
        return isSimPinSecure();
    }

    private boolean isFingerprintDisabled(int i) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        return !(devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 32) == 0) || isSimPinSecure();
    }

    private boolean isFaceDisabled(final int i) {
        final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if (mCustomFaceUnlockSupported && devicePolicyManager != null) {
            try {
                if (devicePolicyManager.getPasswordQuality(null, i) > 32768) {
                    return true;
                }
            } catch (SecurityException e) {
                Log.e("KeyguardUpdateMonitor", "isFaceDisabled error:", e);
            }
        }
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda10
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$isFaceDisabled$2(devicePolicyManager, i);
            }
        })).booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$isFaceDisabled$2(DevicePolicyManager devicePolicyManager, int i) {
        return Boolean.valueOf(!(devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 128) == 0) || isSimPinSecure());
    }

    public boolean getUserCanSkipBouncer(int i) {
        return getUserHasTrust(i) || getUserUnlockedWithBiometric(i);
    }

    public boolean getUserHasTrust(int i) {
        return !isTrustDisabled(i) && this.mUserHasTrust.get(i);
    }

    public boolean getUserUnlockedWithBiometric(int i) {
        BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(i);
        BiometricAuthenticated biometricAuthenticated2 = this.mUserFaceAuthenticated.get(i);
        return (biometricAuthenticated != null && biometricAuthenticated.mAuthenticated && isUnlockingWithBiometricAllowed(biometricAuthenticated.mIsStrongBiometric)) || (biometricAuthenticated2 != null && biometricAuthenticated2.mAuthenticated && isUnlockingWithBiometricAllowed(biometricAuthenticated2.mIsStrongBiometric));
    }

    public boolean getUserTrustIsManaged(int i) {
        return this.mUserTrustIsManaged.get(i) && !isTrustDisabled(i);
    }

    private void updateSecondaryLockscreenRequirement(int i) {
        Intent intent = this.mSecondaryLockscreenRequirement.get(Integer.valueOf(i));
        boolean zIsSecondaryLockscreenEnabled = this.mDevicePolicyManager.isSecondaryLockscreenEnabled(UserHandle.of(i));
        boolean z = true;
        if (zIsSecondaryLockscreenEnabled && intent == null) {
            ComponentName profileOwnerOrDeviceOwnerSupervisionComponent = this.mDevicePolicyManager.getProfileOwnerOrDeviceOwnerSupervisionComponent(UserHandle.of(i));
            if (profileOwnerOrDeviceOwnerSupervisionComponent == null) {
                Log.e("KeyguardUpdateMonitor", "No Profile Owner or Device Owner supervision app found for User " + i);
            } else {
                ResolveInfo resolveInfoResolveService = this.mContext.getPackageManager().resolveService(new Intent("android.app.action.BIND_SECONDARY_LOCKSCREEN_SERVICE").setPackage(profileOwnerOrDeviceOwnerSupervisionComponent.getPackageName()), 0);
                if (resolveInfoResolveService != null && resolveInfoResolveService.serviceInfo != null) {
                    this.mSecondaryLockscreenRequirement.put(Integer.valueOf(i), new Intent().setComponent(resolveInfoResolveService.serviceInfo.getComponentName()));
                }
            }
            z = false;
        } else if (zIsSecondaryLockscreenEnabled || intent == null) {
            z = false;
        } else {
            this.mSecondaryLockscreenRequirement.put(Integer.valueOf(i), null);
        }
        if (z) {
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSecondaryLockscreenRequirementChanged(i);
                }
            }
        }
    }

    public Intent getSecondaryLockscreenRequirement(int i) {
        return this.mSecondaryLockscreenRequirement.get(Integer.valueOf(i));
    }

    public boolean isTrustUsuallyManaged(int i) {
        Assert.isMainThread();
        return this.mUserTrustIsUsuallyManaged.get(i);
    }

    public boolean isUnlockingWithBiometricAllowed(boolean z) {
        return this.mStrongAuthTracker.isUnlockingWithBiometricAllowed(z);
    }

    public boolean isUserInLockdown(int i) {
        return containsFlag(this.mStrongAuthTracker.getStrongAuthForUser(i), 32);
    }

    private boolean isEncryptedOrLockdown(int i) {
        int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(i);
        return containsFlag(strongAuthForUser, 1) || (containsFlag(strongAuthForUser, 2) || containsFlag(strongAuthForUser, 32));
    }

    public boolean userNeedsStrongAuth() {
        return this.mStrongAuthTracker.getStrongAuthForUser(getCurrentUser()) != 0;
    }

    public boolean needsSlowUnlockTransition() {
        return this.mNeedsSlowUnlockTransition;
    }

    public StrongAuthTracker getStrongAuthTracker() {
        return this.mStrongAuthTracker;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyStrongAuthStateChanged(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStrongAuthStateChanged(i);
            }
        }
    }

    public boolean isScreenOn() {
        return this.mScreenOn;
    }

    private void dispatchErrorMessage(CharSequence charSequence) {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustAgentErrorMessage(charSequence);
            }
        }
    }

    @VisibleForTesting
    void setAssistantVisible(boolean z) {
        this.mAssistantVisible = z;
        updateBiometricListeningState();
    }

    static class DisplayClientState {
        DisplayClientState() {
        }
    }

    private static class SimData {
        public int simState;
        public int slotId;
        public int subId;

        SimData(int i, int i2, int i3) {
            this.simState = i;
            this.slotId = i2;
            this.subId = i3;
        }

        /* JADX WARN: Removed duplicated region for block: B:13:0x0044  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        static com.android.keyguard.KeyguardUpdateMonitor.SimData fromIntent(android.content.Intent r7) {
            /*
                java.lang.String r0 = r7.getAction()
                java.lang.String r1 = "android.intent.action.SIM_STATE_CHANGED"
                boolean r0 = r1.equals(r0)
                if (r0 == 0) goto L92
                java.lang.String r0 = "ss"
                java.lang.String r0 = r7.getStringExtra(r0)
                java.lang.String r1 = "android.telephony.extra.SLOT_INDEX"
                r2 = 0
                int r1 = r7.getIntExtra(r1, r2)
                r3 = -1
                java.lang.String r4 = "android.telephony.extra.SUBSCRIPTION_INDEX"
                int r3 = r7.getIntExtra(r4, r3)
                java.lang.String r4 = "ABSENT"
                boolean r4 = r4.equals(r0)
                r5 = 5
                java.lang.String r6 = "reason"
                if (r4 == 0) goto L3c
                java.lang.String r7 = r7.getStringExtra(r6)
                java.lang.String r0 = "PERM_DISABLED"
                boolean r7 = r0.equals(r7)
                if (r7 == 0) goto L39
                r7 = 7
                goto L3a
            L39:
                r7 = 1
            L3a:
                r2 = r7
                goto L8c
            L3c:
                java.lang.String r4 = "READY"
                boolean r4 = r4.equals(r0)
                if (r4 == 0) goto L46
            L44:
                r2 = r5
                goto L8c
            L46:
                java.lang.String r4 = "LOCKED"
                boolean r4 = r4.equals(r0)
                if (r4 == 0) goto L66
                java.lang.String r7 = r7.getStringExtra(r6)
                java.lang.String r0 = "PIN"
                boolean r0 = r0.equals(r7)
                if (r0 == 0) goto L5c
                r2 = 2
                goto L8c
            L5c:
                java.lang.String r0 = "PUK"
                boolean r7 = r0.equals(r7)
                if (r7 == 0) goto L8c
                r2 = 3
                goto L8c
            L66:
                java.lang.String r7 = "NETWORK"
                boolean r7 = r7.equals(r0)
                if (r7 == 0) goto L70
                r2 = 4
                goto L8c
            L70:
                java.lang.String r7 = "CARD_IO_ERROR"
                boolean r7 = r7.equals(r0)
                if (r7 == 0) goto L7b
                r2 = 8
                goto L8c
            L7b:
                java.lang.String r7 = "LOADED"
                boolean r7 = r7.equals(r0)
                if (r7 != 0) goto L44
                java.lang.String r7 = "IMSI"
                boolean r7 = r7.equals(r0)
                if (r7 == 0) goto L8c
                goto L44
            L8c:
                com.android.keyguard.KeyguardUpdateMonitor$SimData r7 = new com.android.keyguard.KeyguardUpdateMonitor$SimData
                r7.<init>(r2, r1, r3)
                return r7
            L92:
                java.lang.IllegalArgumentException r7 = new java.lang.IllegalArgumentException
                java.lang.String r0 = "only handles intent ACTION_SIM_STATE_CHANGED"
                r7.<init>(r0)
                throw r7
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.SimData.fromIntent(android.content.Intent):com.android.keyguard.KeyguardUpdateMonitor$SimData");
        }

        public String toString() {
            return "SimData{state=" + this.simState + ",slotId=" + this.slotId + ",subId=" + this.subId + "}";
        }
    }

    public static class StrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        private final Consumer<Integer> mStrongAuthRequiredChangedCallback;

        public StrongAuthTracker(Context context, Consumer<Integer> consumer) {
            super(context);
            this.mStrongAuthRequiredChangedCallback = consumer;
        }

        public boolean isUnlockingWithBiometricAllowed(boolean z) {
            return isBiometricAllowedForUser(z, KeyguardUpdateMonitor.getCurrentUser());
        }

        public boolean hasUserAuthenticatedSinceBoot() {
            return (getStrongAuthForUser(KeyguardUpdateMonitor.getCurrentUser()) & 1) == 0;
        }

        public void onStrongAuthRequiredChanged(int i) {
            this.mStrongAuthRequiredChangedCallback.accept(Integer.valueOf(i));
        }
    }

    protected void handleStartedWakingUp() {
        Trace.beginSection("KeyguardUpdateMonitor#handleStartedWakingUp");
        Assert.isMainThread();
        updateBiometricListeningState();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedWakingUp();
            }
        }
        Trace.endSection();
    }

    protected void handleStartedGoingToSleep(int i) {
        Assert.isMainThread();
        this.mLockIconPressed = false;
        clearBiometricRecognized();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedGoingToSleep(i);
            }
        }
        this.mGoingToSleep = true;
        updateBiometricListeningState();
    }

    protected void handleFinishedGoingToSleep(int i) {
        Assert.isMainThread();
        this.mGoingToSleep = false;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFinishedGoingToSleep(i);
            }
        }
        updateBiometricListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScreenTurnedOn() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOn();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScreenTurnedOff() {
        DejankUtils.startDetectingBlockingIpcs("KeyguardUpdateMonitor#handleScreenTurnedOff");
        Assert.isMainThread();
        this.mHardwareFingerprintUnavailableRetryCount = 0;
        this.mHardwareFaceUnavailableRetryCount = 0;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOff();
            }
        }
        DejankUtils.stopDetectingBlockingIpcs("KeyguardUpdateMonitor#handleScreenTurnedOff");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDreamingStateChanged(int i) {
        Assert.isMainThread();
        this.mIsDreaming = i == 1;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDreamingStateChanged(this.mIsDreaming);
            }
        }
        updateBiometricListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUserInfoChanged(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserInfoChanged(i);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUserUnlocked(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.put(i, true);
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserUnlocked();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUserStopped(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.put(i, this.mUserManager.isUserUnlocked(i));
    }

    @VisibleForTesting
    void handleUserRemoved(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.delete(i);
        this.mUserTrustIsUsuallyManaged.delete(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardGoingAway(boolean z) {
        Assert.isMainThread();
        setKeyguardGoingAway(z);
    }

    @VisibleForTesting
    protected void setStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        StrongAuthTracker strongAuthTracker2 = this.mStrongAuthTracker;
        if (strongAuthTracker2 != null) {
            this.mLockPatternUtils.unregisterStrongAuthTracker(strongAuthTracker2);
        }
        this.mStrongAuthTracker = strongAuthTracker;
        this.mLockPatternUtils.registerStrongAuthTracker(strongAuthTracker);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerRingerTracker() {
        this.mRingerModeTracker.getRingerMode().observeForever(this.mRingerModeObserver);
    }

    @VisibleForTesting
    protected KeyguardUpdateMonitor(Context context, Looper looper, BroadcastDispatcher broadcastDispatcher, DumpManager dumpManager, RingerModeTracker ringerModeTracker, Executor executor, StatusBarStateController statusBarStateController, LockPatternUtils lockPatternUtils) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.keyguard.KeyguardUpdateMonitor.9
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
                    return;
                }
                if ("android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(339, intent.getStringExtra("time-zone")));
                    return;
                }
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(302, new BatteryStatus(intent)));
                    return;
                }
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    SimData simDataFromIntent = SimData.fromIntent(intent);
                    if (intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                        if (simDataFromIntent.simState == 1) {
                            KeyguardUpdateMonitor.this.mHandler.obtainMessage(338, Boolean.TRUE).sendToTarget();
                            return;
                        }
                        return;
                    }
                    Log.v("KeyguardUpdateMonitor", "action " + action + " state: " + intent.getStringExtra("ss") + " slotId: " + simDataFromIntent.slotId + " subid: " + simDataFromIntent.subId);
                    KeyguardUpdateMonitor.this.mHandler.obtainMessage(304, simDataFromIntent.subId, simDataFromIntent.slotId, Integer.valueOf(simDataFromIntent.simState)).sendToTarget();
                    return;
                }
                if ("android.intent.action.PHONE_STATE".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(306, intent.getStringExtra("state")));
                    return;
                }
                if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(329);
                    return;
                }
                if ("android.intent.action.SERVICE_STATE".equals(action)) {
                    ServiceState serviceStateNewFromBundle = ServiceState.newFromBundle(intent.getExtras());
                    int intExtra = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                    int intExtra2 = intent.getIntExtra("slot", -1);
                    if (!SubscriptionManager.isUsableSubIdValue(intExtra) && SubscriptionManager.isValidPhoneId(intExtra2)) {
                        KeyguardUpdateMonitor.this.mServiceStatesWithKeySlot.put(Integer.valueOf(intExtra2), serviceStateNewFromBundle);
                    }
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(330, intExtra, 0, serviceStateNewFromBundle));
                    return;
                }
                if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
                } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(337);
                }
            }
        };
        this.mBroadcastReceiver = broadcastReceiver;
        BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() { // from class: com.android.keyguard.KeyguardUpdateMonitor.10
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
                    return;
                }
                if ("android.intent.action.USER_INFO_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(317, intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()), 0));
                    return;
                }
                if ("com.android.facelock.FACE_UNLOCK_STARTED".equals(action)) {
                    Trace.beginSection("KeyguardUpdateMonitor.mBroadcastAllReceiver#onReceive ACTION_FACE_UNLOCK_STARTED");
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 1, getSendingUserId()));
                    Trace.endSection();
                    return;
                }
                if ("com.android.facelock.FACE_UNLOCK_STOPPED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 0, getSendingUserId()));
                    return;
                }
                if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(309, Integer.valueOf(getSendingUserId())));
                    return;
                }
                if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(334, getSendingUserId(), 0));
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(340, intent.getIntExtra("android.intent.extra.user_handle", -1), 0));
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(341, intent.getIntExtra("android.intent.extra.user_handle", -1), 0));
                }
            }
        };
        this.mBroadcastAllReceiver = broadcastReceiver2;
        this.mFingerprintLockoutResetCallback = new FingerprintManager.LockoutResetCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.11
            public void onLockoutReset() {
                KeyguardUpdateMonitor.this.handleFingerprintLockoutReset();
            }
        };
        this.mFaceLockoutResetCallback = new FaceManager.LockoutResetCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.12
            public void onLockoutReset() {
                KeyguardUpdateMonitor.this.handleFaceLockoutReset();
            }
        };
        this.mFingerprintDetectionCallback = new FingerprintManager.FingerprintDetectionCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda0
            public final void onFingerprintDetected(int i, boolean z) {
                this.f$0.handleFingerprintAuthenticated(i, z);
            }
        };
        this.mFingerprintAuthenticationCallback = new FingerprintManager.AuthenticationCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.13
            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationFailed() {
                KeyguardUpdateMonitor.this.handleFingerprintAuthFailed();
            }

            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
                Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
                KeyguardUpdateMonitor.this.handleFingerprintAuthenticated(authenticationResult.getUserId(), authenticationResult.isStrongBiometric());
                Trace.endSection();
            }

            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationHelp(int i, CharSequence charSequence) {
                KeyguardUpdateMonitor.this.handleFingerprintHelp(i, charSequence.toString());
            }

            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationError(int i, CharSequence charSequence) {
                KeyguardUpdateMonitor.this.handleFingerprintError(i, charSequence != null ? charSequence.toString() : "");
            }

            public void onAuthenticationAcquired(int i) {
                KeyguardUpdateMonitor.this.handleFingerprintAcquired(i);
            }
        };
        this.mFaceAuthenticationCallback = new FaceManager.AuthenticationCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.14
            public void onAuthenticationFailed() {
                KeyguardUpdateMonitor.this.handleFaceAuthFailed();
            }

            public void onAuthenticationSucceeded(FaceManager.AuthenticationResult authenticationResult) {
                Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
                KeyguardUpdateMonitor.this.handleFaceAuthenticated(authenticationResult.getUserId(), authenticationResult.isStrongBiometric());
                Trace.endSection();
            }

            public void onAuthenticationHelp(int i, CharSequence charSequence) {
                KeyguardUpdateMonitor.this.handleFaceHelp(i, charSequence.toString());
            }

            public void onAuthenticationError(int i, CharSequence charSequence) {
                KeyguardUpdateMonitor.this.handleFaceError(i, charSequence.toString());
            }

            public void onAuthenticationAcquired(int i) {
                KeyguardUpdateMonitor.this.handleFaceAcquired(i);
            }
        };
        UserSwitchObserver userSwitchObserver = new UserSwitchObserver() { // from class: com.android.keyguard.KeyguardUpdateMonitor.16
            public void onUserSwitching(int i, IRemoteCallback iRemoteCallback) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(310, i, 0, iRemoteCallback));
            }

            public void onUserSwitchComplete(int i) throws RemoteException {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(314, i, 0));
            }
        };
        this.mUserSwitchObserver = userSwitchObserver;
        this.mTaskStackListener = new TaskStackChangeListener() { // from class: com.android.keyguard.KeyguardUpdateMonitor.18
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskStackChangedBackground() {
                try {
                    ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(0, 4);
                    if (stackInfo == null) {
                        return;
                    }
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(335, Boolean.valueOf(stackInfo.visible)));
                } catch (RemoteException e) {
                    Log.e("KeyguardUpdateMonitor", "unable to check task stack", e);
                }
            }
        };
        this.mContext = context;
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb();
        this.mStrongAuthTracker = new StrongAuthTracker(context, new Consumer() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda7
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.notifyStrongAuthStateChanged(((Integer) obj).intValue());
            }
        });
        this.mFaceAuthOnlyOnSecurityView = context.getResources().getBoolean(android.R.bool.config_cecQuerySadAacDisabled_default);
        this.mBackgroundExecutor = executor;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mRingerModeTracker = ringerModeTracker;
        this.mStatusBarStateController = statusBarStateController;
        this.mLockPatternUtils = lockPatternUtils;
        dumpManager.registerDumpable(KeyguardUpdateMonitor.class.getName(), this);
        Handler handler = new Handler(looper) { // from class: com.android.keyguard.KeyguardUpdateMonitor.15
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 301) {
                    KeyguardUpdateMonitor.this.handleTimeUpdate();
                }
                if (i == 302) {
                    KeyguardUpdateMonitor.this.handleBatteryUpdate((BatteryStatus) message.obj);
                    return;
                }
                if (i == 312) {
                    KeyguardUpdateMonitor.this.handleKeyguardReset();
                    return;
                }
                if (i == 314) {
                    KeyguardUpdateMonitor.this.handleUserSwitchComplete(message.arg1);
                    return;
                }
                if (i == 600) {
                    KeyguardUpdateMonitor.this.updateBiometricListeningState();
                    return;
                }
                switch (i) {
                    case 304:
                        KeyguardUpdateMonitor.this.handleSimStateChange(message.arg1, message.arg2, ((Integer) message.obj).intValue());
                        break;
                    case 305:
                        KeyguardUpdateMonitor.this.handleRingerModeChange(message.arg1);
                        break;
                    case 306:
                        KeyguardUpdateMonitor.this.handlePhoneStateChanged((String) message.obj);
                        break;
                    default:
                        switch (i) {
                            case 308:
                                KeyguardUpdateMonitor.this.handleDeviceProvisioned();
                                break;
                            case 309:
                                KeyguardUpdateMonitor.this.handleDevicePolicyManagerStateChanged(message.arg1);
                                break;
                            case 310:
                                KeyguardUpdateMonitor.this.handleUserSwitching(message.arg1, (IRemoteCallback) message.obj);
                                break;
                            default:
                                switch (i) {
                                    case 317:
                                        KeyguardUpdateMonitor.this.handleUserInfoChanged(message.arg1);
                                        break;
                                    case 318:
                                        KeyguardUpdateMonitor.this.handleReportEmergencyCallAction();
                                        break;
                                    case 319:
                                        Trace.beginSection("KeyguardUpdateMonitor#handler MSG_STARTED_WAKING_UP");
                                        KeyguardUpdateMonitor.this.handleStartedWakingUp();
                                        Trace.endSection();
                                        break;
                                    case 320:
                                        KeyguardUpdateMonitor.this.handleFinishedGoingToSleep(message.arg1);
                                        break;
                                    case 321:
                                        KeyguardUpdateMonitor.this.handleStartedGoingToSleep(message.arg1);
                                        break;
                                    case 322:
                                        KeyguardUpdateMonitor.this.handleKeyguardBouncerChanged(message.arg1);
                                        break;
                                    default:
                                        switch (i) {
                                            case 327:
                                                Trace.beginSection("KeyguardUpdateMonitor#handler MSG_FACE_UNLOCK_STATE_CHANGED");
                                                KeyguardUpdateMonitor.this.handleFaceUnlockStateChanged(message.arg1 != 0, message.arg2);
                                                Trace.endSection();
                                                break;
                                            case 328:
                                                KeyguardUpdateMonitor.this.handleSimSubscriptionInfoChanged();
                                                break;
                                            case 329:
                                                KeyguardUpdateMonitor.this.handleAirplaneModeChanged();
                                                break;
                                            case 330:
                                                KeyguardUpdateMonitor.this.handleServiceStateChange(message.arg1, (ServiceState) message.obj);
                                                break;
                                            case 331:
                                                KeyguardUpdateMonitor.this.handleScreenTurnedOn();
                                                break;
                                            case 332:
                                                Trace.beginSection("KeyguardUpdateMonitor#handler MSG_SCREEN_TURNED_ON");
                                                KeyguardUpdateMonitor.this.handleScreenTurnedOff();
                                                Trace.endSection();
                                                break;
                                            case 333:
                                                KeyguardUpdateMonitor.this.handleDreamingStateChanged(message.arg1);
                                                break;
                                            case 334:
                                                KeyguardUpdateMonitor.this.handleUserUnlocked(message.arg1);
                                                break;
                                            case 335:
                                                KeyguardUpdateMonitor.this.setAssistantVisible(((Boolean) message.obj).booleanValue());
                                                break;
                                            case 336:
                                                KeyguardUpdateMonitor.this.updateBiometricListeningState();
                                                break;
                                            case 337:
                                                KeyguardUpdateMonitor.this.updateLogoutEnabled();
                                                break;
                                            case 338:
                                                KeyguardUpdateMonitor.this.updateTelephonyCapable(((Boolean) message.obj).booleanValue());
                                                break;
                                            case 339:
                                                KeyguardUpdateMonitor.this.handleTimeZoneUpdate((String) message.obj);
                                                break;
                                            case 340:
                                                KeyguardUpdateMonitor.this.handleUserStopped(message.arg1);
                                                break;
                                            case 341:
                                                KeyguardUpdateMonitor.this.handleUserRemoved(message.arg1);
                                                break;
                                            case 342:
                                                KeyguardUpdateMonitor.this.handleKeyguardGoingAway(((Boolean) message.obj).booleanValue());
                                                break;
                                            default:
                                                super.handleMessage(message);
                                                break;
                                        }
                                }
                        }
                }
            }
        };
        this.mHandler = handler;
        this.mHasFod = context.getPackageManager().hasSystemFeature("vendor.lineage.biometrics.fingerprint.inscreen");
        if (!this.mDeviceProvisioned) {
            watchForDeviceProvisioning();
        }
        this.mBatteryStatus = new BatteryStatus(1, 100, 0, 0, 0, 0, 0, 0.0f, false, false, false, false, false, false, false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        broadcastDispatcher.registerReceiverWithHandler(broadcastReceiver, intentFilter, handler);
        executor.execute(new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$3();
            }
        });
        handler.post(new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.registerRingerTracker();
            }
        });
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter2.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter2.addAction("com.android.facelock.FACE_UNLOCK_STARTED");
        intentFilter2.addAction("com.android.facelock.FACE_UNLOCK_STOPPED");
        intentFilter2.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter2.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter2.addAction("android.intent.action.USER_STOPPED");
        intentFilter2.addAction("android.intent.action.USER_REMOVED");
        broadcastDispatcher.registerReceiverWithHandler(broadcastReceiver2, intentFilter2, handler, UserHandle.ALL);
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        try {
            ActivityManager.getService().registerUserSwitchObserver(userSwitchObserver, "KeyguardUpdateMonitor");
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
        TrustManager trustManager = (TrustManager) context.getSystemService(TrustManager.class);
        this.mTrustManager = trustManager;
        trustManager.registerTrustListener(this);
        setStrongAuthTracker(this.mStrongAuthTracker);
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
        PocketManager pocketManager = (PocketManager) context.getSystemService("pocket");
        this.mPocketManager = pocketManager;
        if (pocketManager != null) {
            pocketManager.addCallback(this.mPocketCallback);
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.biometrics.face")) {
            this.mFaceManager = (FaceManager) context.getSystemService("face");
        }
        if (this.mFpm != null || this.mFaceManager != null) {
            BiometricManager biometricManager = (BiometricManager) context.getSystemService(BiometricManager.class);
            this.mBiometricManager = biometricManager;
            biometricManager.registerEnabledOnKeyguardCallback(this.mBiometricEnabledCallback);
        }
        updateBiometricListeningState();
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null) {
            fingerprintManager.addLockoutResetCallback(this.mFingerprintLockoutResetCallback);
        }
        FaceManager faceManager = this.mFaceManager;
        if (faceManager != null) {
            faceManager.addLockoutResetCallback(this.mFaceLockoutResetCallback);
        }
        this.mIsAutomotive = isAutomotive();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        this.mUserManager = userManager;
        this.mIsPrimaryUser = userManager.isPrimaryUser();
        int currentUser = ActivityManager.getCurrentUser();
        this.mUserIsUnlocked.put(currentUser, this.mUserManager.isUserUnlocked(currentUser));
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        this.mDevicePolicyManager = devicePolicyManager;
        this.mLogoutEnabled = devicePolicyManager.isLogoutEnabled();
        updateSecondaryLockscreenRequirement(currentUser);
        for (UserInfo userInfo : this.mUserManager.getUsers()) {
            SparseBooleanArray sparseBooleanArray = this.mUserTrustIsUsuallyManaged;
            int i = userInfo.id;
            sparseBooleanArray.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        }
        updateAirplaneModeState();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mTelephonyManager = telephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 4194304);
            for (int i2 = 0; i2 < this.mTelephonyManager.getActiveModemCount(); i2++) {
                int simState = this.mTelephonyManager.getSimState(i2);
                int[] subscriptionIds = this.mSubscriptionManager.getSubscriptionIds(i2);
                if (subscriptionIds != null) {
                    for (int i3 : subscriptionIds) {
                        this.mHandler.obtainMessage(304, i3, i2, Integer.valueOf(simState)).sendToTarget();
                    }
                }
            }
        }
        SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver = settingsObserver;
        settingsObserver.observe();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3() {
        Intent intentRegisterReceiver;
        int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
        ServiceState serviceStateForSubscriber = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getServiceStateForSubscriber(defaultSubscriptionId);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(330, defaultSubscriptionId, 0, serviceStateForSubscriber));
        if (this.mBatteryStatus == null && (intentRegisterReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null && this.mBatteryStatus == null) {
            this.mBroadcastReceiver.onReceive(this.mContext, intentRegisterReceiver);
        }
    }

    private void updateAirplaneModeState() {
        if (!WirelessUtils.isAirplaneModeOn(this.mContext) || this.mHandler.hasMessages(329)) {
            return;
        }
        this.mHandler.sendEmptyMessage(329);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBiometricListeningState() {
        updateFingerprintListeningState();
        updateFaceListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFingerprintListeningState() {
        if (this.mHandler.hasMessages(336)) {
            return;
        }
        this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
        boolean z = !(this.mHasFod && userNeedsStrongAuth()) && shouldListenForFingerprint();
        int i = this.mFingerprintRunningState;
        boolean z2 = i == 1 || i == 3;
        if (z2 && !z) {
            stopListeningForFingerprint();
        } else {
            if (z2 || !z) {
                return;
            }
            startListeningForFingerprint();
        }
    }

    public boolean isUserUnlocked(int i) {
        return this.mUserIsUnlocked.get(i);
    }

    public void onAuthInterruptDetected(boolean z) {
        if (this.mAuthInterruptActive == z) {
            return;
        }
        this.mAuthInterruptActive = z;
        updateFaceListeningState();
    }

    public void requestFaceAuth() {
        updateFaceListeningState();
    }

    public void cancelFaceAuth() {
        stopListeningForFace();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFaceListeningState() {
        if (this.mHandler.hasMessages(336)) {
            return;
        }
        this.mHandler.removeCallbacks(this.mRetryFaceAuthentication);
        boolean zShouldListenForFace = shouldListenForFace();
        int i = this.mFaceRunningState;
        if (i == 1 && !zShouldListenForFace) {
            stopListeningForFace();
        } else {
            if (i == 1 || !zShouldListenForFace) {
                return;
            }
            startListeningForFace();
        }
    }

    private boolean shouldListenForFingerprintAssistant() {
        BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(getCurrentUser());
        if (this.mAssistantVisible && this.mKeyguardOccluded) {
            return (biometricAuthenticated == null || !biometricAuthenticated.mAuthenticated) && !this.mUserHasTrust.get(getCurrentUser(), false);
        }
        return false;
    }

    private boolean shouldListenForFaceAssistant() {
        BiometricAuthenticated biometricAuthenticated = this.mUserFaceAuthenticated.get(getCurrentUser());
        if (this.mAssistantVisible && this.mKeyguardOccluded) {
            return (biometricAuthenticated == null || !biometricAuthenticated.mAuthenticated) && !this.mUserHasTrust.get(getCurrentUser(), false);
        }
        return false;
    }

    private boolean shouldListenForFingerprint() {
        boolean z = (this.mFingerprintLockedOut && this.mBouncer && this.mCredentialAttempted) ? false : true;
        if ((!this.mKeyguardIsVisible && this.mDeviceInteractive && ((!this.mBouncer || this.mKeyguardGoingAway) && !this.mGoingToSleep && !shouldListenForFingerprintAssistant() && (!this.mKeyguardOccluded || !this.mIsDreaming))) || this.mSwitchingUser || isFingerprintDisabled(getCurrentUser())) {
            return false;
        }
        return !(this.mKeyguardGoingAway && this.mDeviceInteractive) && this.mIsPrimaryUser && z && !this.mIsDeviceInPocket && this.mFingerprintSettingEnabledForUser.get(getCurrentUser());
    }

    public boolean shouldListenForFace() {
        if (this.mFaceAuthOnlyOnSecurityView && this.mKeyguardReset) {
            this.mKeyguardReset = false;
            return false;
        }
        boolean z = this.mKeyguardIsVisible && this.mDeviceInteractive && !this.mGoingToSleep && !(this.mStatusBarStateController.getState() == 2);
        int currentUser = getCurrentUser();
        int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
        boolean z2 = containsFlag(strongAuthForUser, 2) || containsFlag(strongAuthForUser, 32);
        boolean z3 = containsFlag(strongAuthForUser, 1) || containsFlag(strongAuthForUser, 16);
        KeyguardBypassController keyguardBypassController = this.mKeyguardBypassController;
        boolean z4 = keyguardBypassController != null && keyguardBypassController.canBypass();
        boolean z5 = !getUserCanSkipBouncer(currentUser) || z4;
        boolean z6 = (!z3 || (z4 && !this.mBouncer)) && !z2;
        boolean z7 = this.mBouncer;
        boolean z8 = (z7 || this.mAuthInterruptActive || z || shouldListenForFaceAssistant()) && !this.mSwitchingUser && !isFaceDisabled(currentUser) && z5 && !this.mKeyguardGoingAway && this.mFaceSettingEnabledForUser.get(currentUser) && !this.mLockIconPressed && z6 && this.mIsPrimaryUser && !this.mSecureCameraLaunched && ((z7 && z) || !this.mFaceAuthOnlyOnSecurityView) && !this.mIsDeviceInPocket;
        boolean z9 = (z8 && this.mFaceUnlockBehavior == 1 && !this.mBouncerFullyShown) ? false : z8;
        if (DEBUG_FACE) {
            maybeLogFaceListenerModelData(new KeyguardFaceListenModel(System.currentTimeMillis(), currentUser, z9, this.mBouncer, this.mAuthInterruptActive, z, shouldListenForFaceAssistant(), this.mSwitchingUser, isFaceDisabled(currentUser), z5, this.mKeyguardGoingAway, this.mFaceSettingEnabledForUser.get(currentUser), this.mLockIconPressed, z6, this.mIsPrimaryUser, this.mSecureCameraLaunched));
        }
        return z9;
    }

    public void onKeyguardBouncerFullyShown(boolean z) {
        if (this.mBouncerFullyShown != z) {
            this.mBouncerFullyShown = z;
            if (this.mFaceUnlockBehavior == 1) {
                updateFaceListeningState();
            }
        }
    }

    private void maybeLogFaceListenerModelData(KeyguardFaceListenModel keyguardFaceListenModel) {
        if (DEBUG_FACE && this.mFaceRunningState != 1 && keyguardFaceListenModel.isListeningForFace()) {
            if (this.mFaceListenModels == null) {
                this.mFaceListenModels = new ArrayDeque<>(20);
            }
            if (this.mFaceListenModels.size() >= 20) {
                this.mFaceListenModels.remove();
            }
            this.mFaceListenModels.add(keyguardFaceListenModel);
        }
    }

    public void onLockIconPressed() {
        this.mLockIconPressed = true;
        int currentUser = getCurrentUser();
        this.mUserFaceAuthenticated.put(currentUser, null);
        updateFaceListeningState();
        this.mStrongAuthTracker.onStrongAuthRequiredChanged(currentUser);
    }

    private void startListeningForFingerprint() {
        int currentUser = getCurrentUser();
        boolean zIsUnlockWithFingerprintPossible = isUnlockWithFingerprintPossible(currentUser);
        if (this.mFingerprintCancelSignal != null) {
            Log.e("KeyguardUpdateMonitor", "Cancellation signal is not null, high chance of bug in fp auth lifecycle management. FP state: " + this.mFingerprintRunningState + ", unlockPossible: " + zIsUnlockWithFingerprintPossible);
        }
        int i = this.mFingerprintRunningState;
        if (i == 2) {
            setFingerprintRunningState(3);
            return;
        }
        if (i != 3 && zIsUnlockWithFingerprintPossible) {
            this.mFingerprintCancelSignal = new CancellationSignal();
            if (isEncryptedOrLockdown(currentUser)) {
                this.mFpm.detectFingerprint(this.mFingerprintCancelSignal, this.mFingerprintDetectionCallback, currentUser);
            } else {
                this.mFpm.authenticate(null, this.mFingerprintCancelSignal, 0, this.mFingerprintAuthenticationCallback, null, currentUser);
            }
            setFingerprintRunningState(1);
        }
    }

    private void startListeningForFace() {
        int currentUser = getCurrentUser();
        boolean zIsUnlockWithFacePossible = isUnlockWithFacePossible(currentUser);
        if (this.mFaceCancelSignal != null) {
            Log.e("KeyguardUpdateMonitor", "Cancellation signal is not null, high chance of bug in face auth lifecycle management. Face state: " + this.mFaceRunningState + ", unlockPossible: " + zIsUnlockWithFacePossible);
        }
        if (this.mFaceRunningState == 2) {
            setFaceRunningState(3);
        } else if (zIsUnlockWithFacePossible) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            this.mFaceCancelSignal = cancellationSignal;
            this.mFaceManager.authenticate((CryptoObject) null, cancellationSignal, 0, this.mFaceAuthenticationCallback, (Handler) null, currentUser);
            setFaceRunningState(1);
        }
    }

    public boolean isUnlockingWithBiometricsPossible(int i) {
        return isUnlockWithFacePossible(i) || isUnlockWithFingerprintPossible(i);
    }

    private boolean isUnlockWithFingerprintPossible(int i) {
        FingerprintManager fingerprintManager = this.mFpm;
        return fingerprintManager != null && fingerprintManager.isHardwareDetected() && !isFingerprintDisabled(i) && this.mFpm.hasEnrolledTemplates(i) && this.mFingerprintSettingEnabledForUser.get(i);
    }

    private boolean isUnlockWithFacePossible(int i) {
        return isFaceAuthEnabledForUser(i) && !isFaceDisabled(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$isFaceAuthEnabledForUser$4(int i) {
        FaceManager faceManager = this.mFaceManager;
        return Boolean.valueOf(faceManager != null && faceManager.isHardwareDetected() && this.mFaceManager.hasEnrolledTemplates(i) && this.mFaceSettingEnabledForUser.get(i));
    }

    public boolean isFaceAuthEnabledForUser(final int i) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda9
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$isFaceAuthEnabledForUser$4(i);
            }
        })).booleanValue();
    }

    private void stopListeningForFingerprint() {
        if (this.mFingerprintRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFingerprintCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                this.mFingerprintCancelSignal = null;
                this.mHandler.removeCallbacks(this.mFpCancelNotReceived);
                this.mHandler.postDelayed(this.mFpCancelNotReceived, 3000L);
            }
            setFingerprintRunningState(2);
        }
        if (this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(2);
        }
    }

    private void stopListeningForFace() {
        if (this.mFaceRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFaceCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                this.mFaceCancelSignal = null;
                this.mHandler.removeCallbacks(this.mFaceCancelNotReceived);
                this.mHandler.postDelayed(this.mFaceCancelNotReceived, 3000L);
            }
            setFaceRunningState(2);
        }
        if (this.mFaceRunningState == 3) {
            setFaceRunningState(2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDeviceProvisionedInSettingsDb() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    private void watchForDeviceProvisioning() {
        this.mDeviceProvisionedObserver = new ContentObserver(this.mHandler) { // from class: com.android.keyguard.KeyguardUpdateMonitor.17
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                keyguardUpdateMonitor.mDeviceProvisioned = keyguardUpdateMonitor.isDeviceProvisionedInSettingsDb();
                if (KeyguardUpdateMonitor.this.mDeviceProvisioned) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(308);
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        boolean zIsDeviceProvisionedInSettingsDb = isDeviceProvisionedInSettingsDb();
        if (zIsDeviceProvisionedInSettingsDb != this.mDeviceProvisioned) {
            this.mDeviceProvisioned = zIsDeviceProvisionedInSettingsDb;
            if (zIsDeviceProvisionedInSettingsDb) {
                this.mHandler.sendEmptyMessage(308);
            }
        }
    }

    public void setHasLockscreenWallpaper(boolean z) {
        Assert.isMainThread();
        if (z != this.mHasLockscreenWallpaper) {
            this.mHasLockscreenWallpaper = z;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onHasLockscreenWallpaperChanged(z);
                }
            }
        }
    }

    public boolean hasLockscreenWallpaper() {
        return this.mHasLockscreenWallpaper;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDevicePolicyManagerStateChanged(int i) {
        Assert.isMainThread();
        updateFingerprintListeningState();
        updateSecondaryLockscreenRequirement(i);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDevicePolicyManagerStateChanged();
            }
        }
    }

    @VisibleForTesting
    void handleUserSwitching(int i, IRemoteCallback iRemoteCallback) {
        Assert.isMainThread();
        clearBiometricRecognized();
        this.mUserTrustIsUsuallyManaged.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitching(i);
            }
        }
        try {
            iRemoteCallback.sendResult((Bundle) null);
        } catch (RemoteException unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUserSwitchComplete(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitchComplete(i);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDeviceProvisioned() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDeviceProvisioned();
            }
        }
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
            this.mDeviceProvisionedObserver = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePhoneStateChanged(String str) {
        Assert.isMainThread();
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(str)) {
            this.mPhoneState = 0;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(str)) {
            this.mPhoneState = 2;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(str)) {
            this.mPhoneState = 1;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRingerModeChange(int i) {
        Assert.isMainThread();
        this.mRingMode = i;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRingerModeChanged(i);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTimeUpdate() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTimeZoneUpdate(String str) {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeZoneChanged(TimeZone.getTimeZone(str));
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleBatteryUpdate(BatteryStatus batteryStatus) {
        Assert.isMainThread();
        if (this.mLastBatteryUpdate == null) {
            this.mLastBatteryUpdate = new Date();
        } else {
            Date date = new Date();
            if (this.mLastBatteryUpdate.getTime() + 2000 > date.getTime()) {
                return;
            } else {
                this.mLastBatteryUpdate = date;
            }
        }
        boolean zIsBatteryUpdateInteresting = isBatteryUpdateInteresting(this.mBatteryStatus, batteryStatus);
        this.mBatteryStatus = batteryStatus;
        if (zIsBatteryUpdateInteresting) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onRefreshBatteryInfo(batteryStatus);
                }
            }
        }
    }

    @VisibleForTesting
    void updateTelephonyCapable(boolean z) {
        Assert.isMainThread();
        if (z == this.mTelephonyCapable) {
            return;
        }
        this.mTelephonyCapable = z;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTelephonyCapable(this.mTelephonyCapable);
            }
        }
    }

    @VisibleForTesting
    void handleSimStateChange(int i, int i2, int i3) {
        boolean z;
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleSimStateChange(subId=" + i + ", slotId=" + i2 + ", state=" + i3 + ")");
        boolean z2 = true;
        if (SubscriptionManager.isValidSubscriptionId(i)) {
            z = false;
        } else {
            Log.w("KeyguardUpdateMonitor", "invalid subId in handleSimStateChange()");
            if (i3 == 1) {
                updateTelephonyCapable(true);
                for (SimData simData : this.mSimDatas.values()) {
                    if (simData.slotId == i2) {
                        simData.simState = 1;
                    }
                }
                z = true;
            } else {
                if (i3 != 8) {
                    return;
                }
                updateTelephonyCapable(true);
                z = false;
            }
        }
        SimData simData2 = this.mSimDatas.get(Integer.valueOf(i2));
        if (simData2 == null) {
            this.mSimDatas.put(Integer.valueOf(i2), new SimData(i3, i2, i));
        } else {
            if (simData2.simState == i3 && simData2.subId == i && simData2.slotId == i2) {
                z2 = false;
            }
            simData2.simState = i3;
            simData2.subId = i;
            simData2.slotId = i2;
        }
        if ((z2 || z) && i3 != 0) {
            for (int i4 = 0; i4 < this.mCallbacks.size(); i4++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i4).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSimStateChanged(i, i2, i3);
                }
            }
        }
    }

    @VisibleForTesting
    void handleServiceStateChange(int i, ServiceState serviceState) {
        if (!SubscriptionManager.isValidSubscriptionId(i)) {
            Log.w("KeyguardUpdateMonitor", "invalid subId in handleServiceStateChange()");
            return;
        }
        updateTelephonyCapable(true);
        this.mServiceStates.put(Integer.valueOf(i), serviceState);
        callbacksRefreshCarrierInfo();
    }

    public boolean isKeyguardVisible() {
        return this.mKeyguardIsVisible;
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "onKeyguardVisibilityChanged(" + z + ")");
        this.mKeyguardIsVisible = z;
        this.mBouncerFullyShown = false;
        if (z) {
            this.mSecureCameraLaunched = false;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(z);
            }
        }
        updateBiometricListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardReset() {
        this.mBouncerFullyShown = false;
        updateBiometricListeningState();
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
        if (this.mFaceAuthOnlyOnSecurityView) {
            this.mKeyguardReset = true;
        }
    }

    private boolean resolveNeedsSlowUnlockTransition() {
        if (isUserUnlocked(getCurrentUser())) {
            return false;
        }
        ResolveInfo resolveInfoResolveActivity = this.mContext.getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 0);
        if (resolveInfoResolveActivity == null && this.mIsAutomotive) {
            Log.w("KeyguardUpdateMonitor", "resolveNeedsSlowUnlockTransition: returning false since activity could not be resolved.");
            return false;
        }
        return FALLBACK_HOME_COMPONENT.equals(resolveInfoResolveActivity.getComponentInfo().getComponentName());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleKeyguardBouncerChanged(int i) {
        Assert.isMainThread();
        boolean z = i == 1;
        this.mBouncer = z;
        if (z) {
            this.mSecureCameraLaunched = false;
        } else {
            this.mCredentialAttempted = false;
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardBouncerChanged(z);
            }
        }
        updateBiometricListeningState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleReportEmergencyCallAction() {
        this.mBouncerFullyShown = false;
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onEmergencyCallAction();
            }
        }
    }

    private boolean isBatteryUpdateInteresting(BatteryStatus batteryStatus, BatteryStatus batteryStatus2) {
        boolean zIsPluggedIn = batteryStatus2.isPluggedIn();
        boolean zIsPluggedIn2 = batteryStatus.isPluggedIn();
        boolean z = zIsPluggedIn2 && zIsPluggedIn && batteryStatus.status != batteryStatus2.status;
        if (zIsPluggedIn2 != zIsPluggedIn || z || batteryStatus.level != batteryStatus2.level || batteryStatus.temperature != batteryStatus2.temperature) {
            return true;
        }
        if (zIsPluggedIn && (batteryStatus2.maxChargingWattage != batteryStatus.maxChargingWattage || batteryStatus2.maxChargingCurrent != batteryStatus.maxChargingCurrent || batteryStatus2.maxChargingVoltage != batteryStatus.maxChargingVoltage)) {
            return true;
        }
        if (zIsPluggedIn && batteryStatus2.dashChargeStatus != batteryStatus.dashChargeStatus) {
            return true;
        }
        if (zIsPluggedIn && batteryStatus2.smartChargeStatus != batteryStatus.smartChargeStatus) {
            return true;
        }
        if (zIsPluggedIn && batteryStatus2.warpChargeStatus != batteryStatus.warpChargeStatus) {
            return true;
        }
        if (zIsPluggedIn && batteryStatus2.swarpChargeStatus != batteryStatus.swarpChargeStatus) {
            return true;
        }
        if (zIsPluggedIn && batteryStatus2.voocChargeStatus != batteryStatus.voocChargeStatus) {
            return true;
        }
        if (!zIsPluggedIn || batteryStatus2.turboPowerStatus == batteryStatus.turboPowerStatus) {
            return (zIsPluggedIn && batteryStatus2.oemFastChargeStatus != batteryStatus.oemFastChargeStatus) || batteryStatus2.health != batteryStatus.health;
        }
        return true;
    }

    private boolean isAutomotive() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    public void removeCallback(final KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Assert.isMainThread();
        this.mCallbacks.removeIf(new Predicate() { // from class: com.android.keyguard.KeyguardUpdateMonitor$$ExternalSyntheticLambda8
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return KeyguardUpdateMonitor.lambda$removeCallback$5(keyguardUpdateMonitorCallback, (WeakReference) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$removeCallback$5(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback, WeakReference weakReference) {
        return weakReference.get() == keyguardUpdateMonitorCallback;
    }

    public void registerCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == keyguardUpdateMonitorCallback) {
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(keyguardUpdateMonitorCallback));
        removeCallback(null);
        sendUpdates(keyguardUpdateMonitorCallback);
    }

    public void setKeyguardBypassController(KeyguardBypassController keyguardBypassController) {
        this.mKeyguardBypassController = keyguardBypassController;
    }

    public boolean isSwitchingUser() {
        return this.mSwitchingUser;
    }

    public void setSwitchingUser(boolean z) {
        this.mSwitchingUser = z;
        this.mHandler.post(this.mUpdateBiometricListeningState);
    }

    private void sendUpdates(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        keyguardUpdateMonitorCallback.onRefreshBatteryInfo(this.mBatteryStatus);
        keyguardUpdateMonitorCallback.onTimeChanged();
        keyguardUpdateMonitorCallback.onRingerModeChanged(this.mRingMode);
        keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
        keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
        keyguardUpdateMonitorCallback.onClockVisibilityChanged();
        keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(this.mKeyguardIsVisible);
        keyguardUpdateMonitorCallback.onTelephonyCapable(this.mTelephonyCapable);
        Iterator<Map.Entry<Integer, SimData>> it = this.mSimDatas.entrySet().iterator();
        while (it.hasNext()) {
            SimData value = it.next().getValue();
            keyguardUpdateMonitorCallback.onSimStateChanged(value.subId, value.slotId, value.simState);
        }
    }

    public void sendKeyguardReset() {
        this.mHandler.obtainMessage(312).sendToTarget();
    }

    public void sendKeyguardBouncerChanged(boolean z) {
        Message messageObtainMessage = this.mHandler.obtainMessage(322);
        messageObtainMessage.arg1 = z ? 1 : 0;
        messageObtainMessage.sendToTarget();
    }

    public void reportSimUnlocked(int i) {
        Log.v("KeyguardUpdateMonitor", "reportSimUnlocked(subId=" + i + ")");
        handleSimStateChange(i, getSlotId(i), 5);
    }

    public void reportEmergencyCallAction(boolean z) {
        if (!z) {
            this.mHandler.obtainMessage(318).sendToTarget();
        } else {
            Assert.isMainThread();
            handleReportEmergencyCallAction();
        }
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public ServiceState getServiceState(int i) {
        return this.mServiceStates.get(Integer.valueOf(i));
    }

    public void clearBiometricRecognized() {
        clearBiometricRecognized(-10000);
    }

    public void clearBiometricRecognizedWhenKeyguardDone(int i) {
        clearBiometricRecognized(i);
    }

    private void clearBiometricRecognized(int i) {
        Assert.isMainThread();
        this.mUserFingerprintAuthenticated.clear();
        this.mUserFaceAuthenticated.clear();
        this.mTrustManager.clearAllBiometricRecognized(BiometricSourceType.FINGERPRINT, i);
        this.mTrustManager.clearAllBiometricRecognized(BiometricSourceType.FACE, i);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricsCleared();
            }
        }
    }

    public boolean isSimPinVoiceSecure() {
        return isSimPinSecure();
    }

    public boolean isSimPinSecure() {
        Iterator<SubscriptionInfo> it = getSubscriptionInfo(false).iterator();
        while (it.hasNext()) {
            if (isSimPinSecure(getSimState(it.next().getSubscriptionId()))) {
                return true;
            }
        }
        return false;
    }

    public int getSimState(int i) {
        int slotIndex = SubscriptionManager.getSlotIndex(i);
        if (this.mSimDatas.containsKey(Integer.valueOf(slotIndex))) {
            return this.mSimDatas.get(Integer.valueOf(slotIndex)).simState;
        }
        return 0;
    }

    private int getSlotId(int i) {
        int slotIndex = SubscriptionManager.getSlotIndex(i);
        if (!this.mSimDatas.containsKey(Integer.valueOf(slotIndex))) {
            refreshSimState(i, slotIndex);
        }
        return this.mSimDatas.get(Integer.valueOf(slotIndex)).slotId;
    }

    private boolean refreshSimState(int i, int i2) {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        int simState = telephonyManager != null ? telephonyManager.getSimState(i2) : 0;
        SimData simData = this.mSimDatas.get(Integer.valueOf(i2));
        if (simData == null) {
            this.mSimDatas.put(Integer.valueOf(i2), new SimData(simState, i2, i));
            return true;
        }
        boolean z = simData.simState != simState;
        simData.simState = simState;
        return z;
    }

    public void dispatchStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
        }
        this.mHandler.sendEmptyMessage(319);
    }

    public void dispatchStartedGoingToSleep(int i) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(321, i, 0));
    }

    public void dispatchFinishedGoingToSleep(int i) {
        synchronized (this) {
            this.mDeviceInteractive = false;
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(320, i, 0));
    }

    public void dispatchScreenTurnedOn() {
        synchronized (this) {
            this.mScreenOn = true;
        }
        this.mHandler.sendEmptyMessage(331);
    }

    public void dispatchScreenTurnedOff() {
        synchronized (this) {
            this.mScreenOn = false;
        }
        this.mHandler.sendEmptyMessage(332);
    }

    public void dispatchDreamingStarted() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(333, 1, 0));
    }

    public void dispatchDreamingStopped() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(333, 0, 0));
    }

    public void dispatchKeyguardGoingAway(boolean z) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(342, Boolean.valueOf(z)));
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public boolean isGoingToSleep() {
        return this.mGoingToSleep;
    }

    public int getNextSubIdForState(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        int i2 = -1;
        int i3 = Integer.MAX_VALUE;
        for (int i4 = 0; i4 < subscriptionInfo.size(); i4++) {
            int subscriptionId = subscriptionInfo.get(i4).getSubscriptionId();
            int slotId = getSlotId(subscriptionId);
            if (i == getSimState(subscriptionId) && i3 > slotId) {
                i2 = subscriptionId;
                i3 = slotId;
            }
        }
        return i2;
    }

    public SubscriptionInfo getSubscriptionInfoForSubId(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        for (int i2 = 0; i2 < subscriptionInfo.size(); i2++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i2);
            if (i == subscriptionInfo2.getSubscriptionId()) {
                return subscriptionInfo2;
            }
        }
        return null;
    }

    public boolean isLogoutEnabled() {
        return this.mLogoutEnabled;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLogoutEnabled() {
        Assert.isMainThread();
        boolean zIsLogoutEnabled = this.mDevicePolicyManager.isLogoutEnabled();
        if (this.mLogoutEnabled != zIsLogoutEnabled) {
            this.mLogoutEnabled = zIsLogoutEnabled;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onLogoutEnabledChanged();
                }
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            KeyguardUpdateMonitor.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_method"), false, this, -1);
            KeyguardUpdateMonitor.this.updateSettings();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            KeyguardUpdateMonitor.this.updateSettings();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSettings() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (this.mFaceAuthOnlyOnSecurityView) {
            this.mFaceUnlockBehavior = 1;
        } else {
            this.mFaceUnlockBehavior = Settings.Secure.getIntForUser(contentResolver, "face_unlock_method", 0, -2);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardUpdateMonitor state:");
        printWriter.println("  SIM States:");
        Iterator<SimData> it = this.mSimDatas.values().iterator();
        while (it.hasNext()) {
            printWriter.println("    " + it.next().toString());
        }
        printWriter.println("  Subs:");
        if (this.mSubscriptionInfo != null) {
            for (int i = 0; i < this.mSubscriptionInfo.size(); i++) {
                printWriter.println("    " + this.mSubscriptionInfo.get(i));
            }
        }
        printWriter.println("  Current active data subId=" + this.mActiveMobileDataSubscription);
        printWriter.println("  Service states:");
        Iterator<Integer> it2 = this.mServiceStates.keySet().iterator();
        while (it2.hasNext()) {
            int iIntValue = it2.next().intValue();
            printWriter.println("    " + iIntValue + "=" + this.mServiceStates.get(Integer.valueOf(iIntValue)));
        }
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
            int currentUser = ActivityManager.getCurrentUser();
            int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
            BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(currentUser);
            printWriter.println("  Fingerprint state (user=" + currentUser + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("    allowed=");
            sb.append(biometricAuthenticated != null && isUnlockingWithBiometricAllowed(biometricAuthenticated.mIsStrongBiometric));
            printWriter.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("    auth'd=");
            sb2.append(biometricAuthenticated != null && biometricAuthenticated.mAuthenticated);
            printWriter.println(sb2.toString());
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFingerprintDisabled(currentUser));
            printWriter.println("    possible=" + isUnlockWithFingerprintPossible(currentUser));
            printWriter.println("    listening: actual=" + this.mFingerprintRunningState + " expected=" + (shouldListenForFingerprint() ? 1 : 0));
            StringBuilder sb3 = new StringBuilder();
            sb3.append("    strongAuthFlags=");
            sb3.append(Integer.toHexString(strongAuthForUser));
            printWriter.println(sb3.toString());
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser));
        }
        FaceManager faceManager = this.mFaceManager;
        if (faceManager != null && faceManager.isHardwareDetected()) {
            int currentUser2 = ActivityManager.getCurrentUser();
            int strongAuthForUser2 = this.mStrongAuthTracker.getStrongAuthForUser(currentUser2);
            BiometricAuthenticated biometricAuthenticated2 = this.mUserFaceAuthenticated.get(currentUser2);
            printWriter.println("  Face authentication state (user=" + currentUser2 + ")");
            StringBuilder sb4 = new StringBuilder();
            sb4.append("    allowed=");
            sb4.append(biometricAuthenticated2 != null && isUnlockingWithBiometricAllowed(biometricAuthenticated2.mIsStrongBiometric));
            printWriter.println(sb4.toString());
            StringBuilder sb5 = new StringBuilder();
            sb5.append("    auth'd=");
            sb5.append(biometricAuthenticated2 != null && biometricAuthenticated2.mAuthenticated);
            printWriter.println(sb5.toString());
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFaceDisabled(currentUser2));
            printWriter.println("    possible=" + isUnlockWithFacePossible(currentUser2));
            printWriter.println("    strongAuthFlags=" + Integer.toHexString(strongAuthForUser2));
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser2));
            printWriter.println("    enabledByUser=" + this.mFaceSettingEnabledForUser.get(currentUser2));
            printWriter.println("    mSecureCameraLaunched=" + this.mSecureCameraLaunched);
        }
        ArrayDeque<KeyguardFaceListenModel> arrayDeque = this.mFaceListenModels;
        if (arrayDeque != null && !arrayDeque.isEmpty()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            printWriter.println("  Face listen results (last 20 calls):");
            Iterator<KeyguardFaceListenModel> it3 = this.mFaceListenModels.iterator();
            while (it3.hasNext()) {
                KeyguardFaceListenModel next = it3.next();
                printWriter.println("    " + simpleDateFormat.format(new Date(next.getTimeMillis())) + " " + next.toString());
            }
        }
        if (this.mIsAutomotive) {
            printWriter.println("  Running on Automotive build");
        }
    }
}
