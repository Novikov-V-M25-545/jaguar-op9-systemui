package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.airbnb.lottie.LottieAnimationView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.widget.ViewClippingUtil;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.Utils;
import com.android.settingslib.fuelgauge.BatteryStatus;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.IllegalFormatConversionException;
import java.util.function.Supplier;
import vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen;

/* loaded from: classes.dex */
public class KeyguardIndicationController implements StatusBarStateController.StateListener, KeyguardStateController.Callback, TunerService.Tunable {
    private String mAlignmentIndication;
    private final IBatteryStats mBatteryInfo;
    private int mBatteryLevel;
    private boolean mBatteryOverheated;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private BroadcastReceiver mBroadcastReceiver;
    private int mChargingCurrent;
    private LottieAnimationView mChargingIndicationView;
    private int mChargingSpeed;
    private long mChargingTimeRemaining;
    private double mChargingVoltage;
    private double mChargingWattage;
    private final Context mContext;
    private int mCurrentDivider;
    private final DevicePolicyManager mDevicePolicyManager;
    private KeyguardIndicationTextView mDisclosure;
    private float mDisclosureMaxAlpha;
    private final DockManager mDockManager;
    private boolean mDozing;
    private boolean mEnableBatteryDefender;
    private final Handler mHandler;
    private boolean mHideTransientMessageOnScreenOff;
    private ViewGroup mIndicationArea;
    private ColorStateList mInitialTextColorState;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private LockscreenLockIconController mLockIconController;
    private String mMessageToShowOnScreenOn;
    private boolean mPowerCharged;
    private boolean mPowerPluggedIn;
    private boolean mPowerPluggedInWired;
    private String mRestingIndication;
    private boolean mShowBatteryInfo;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarStateController mStatusBarStateController;
    private float mTemperature;
    private KeyguardIndicationTextView mTextView;
    private final KeyguardUpdateMonitorCallback mTickReceiver;
    private CharSequence mTransientIndication;
    private boolean mTransientTextIsError;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private final UserManager mUserManager;
    private boolean mVisible;
    private final SettableWakeLock mWakeLock;
    private int mChargingIndication = 1;
    private int mFODPositionY = 0;
    private final ViewClippingUtil.ClippingParameters mClippingParams = new ViewClippingUtil.ClippingParameters() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.1
        public boolean shouldFinish(View view) {
            return view == KeyguardIndicationController.this.mIndicationArea;
        }
    };

    private String getTrustManagedIndication() {
        return null;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    public KeyguardIndicationController(Context context, WakeLock.Builder builder, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, KeyguardUpdateMonitor keyguardUpdateMonitor, DockManager dockManager, BroadcastDispatcher broadcastDispatcher, DevicePolicyManager devicePolicyManager, IBatteryStats iBatteryStats, UserManager userManager) {
        KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.4
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() throws Resources.NotFoundException {
                if (KeyguardIndicationController.this.mVisible) {
                    KeyguardIndicationController.this.updateIndication(false);
                }
            }
        };
        this.mTickReceiver = keyguardUpdateMonitorCallback;
        this.mHandler = new Handler() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.5
            @Override // android.os.Handler
            public void handleMessage(Message message) throws Resources.NotFoundException {
                int i = message.what;
                if (i == 1) {
                    KeyguardIndicationController.this.hideTransientIndication();
                    return;
                }
                if (i == 2) {
                    if (KeyguardIndicationController.this.mLockIconController != null) {
                        KeyguardIndicationController.this.mLockIconController.setTransientBiometricsError(false);
                    }
                } else if (i == 3) {
                    KeyguardIndicationController.this.showSwipeUpToUnlock();
                }
            }
        };
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mDevicePolicyManager = devicePolicyManager;
        this.mKeyguardStateController = keyguardStateController;
        this.mStatusBarStateController = statusBarStateController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mDockManager = dockManager;
        dockManager.addAlignmentStateListener(new DockManager.AlignmentStateListener(this) { // from class: com.android.systemui.statusbar.KeyguardIndicationController$$ExternalSyntheticLambda0
        });
        this.mWakeLock = new SettableWakeLock(builder.setTag("Doze:KeyguardIndication").build(), "KeyguardIndication");
        this.mBatteryInfo = iBatteryStats;
        this.mUserManager = userManager;
        keyguardUpdateMonitor.registerCallback(getKeyguardCallback());
        keyguardUpdateMonitor.registerCallback(keyguardUpdateMonitorCallback);
        statusBarStateController.addCallback(this);
        keyguardStateController.addCallback(this);
        TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
        tunerService.addTunable(this, "system:lockscreen_charging_animation_style");
        tunerService.addTunable(this, "system:lockscreen_battery_info");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (!str.equals("system:lockscreen_charging_animation_style")) {
            if (str.equals("system:lockscreen_battery_info")) {
                this.mShowBatteryInfo = TunerService.parseIntegerSwitch(str2, true);
            }
        } else {
            this.mChargingIndication = TunerService.parseInteger(str2, 1);
            if (this.mChargingIndicationView != null) {
                updateChargingIndicationStyle();
            }
        }
    }

    public void setIndicationArea(ViewGroup viewGroup) {
        this.mIndicationArea = viewGroup;
        KeyguardIndicationTextView keyguardIndicationTextView = (KeyguardIndicationTextView) viewGroup.findViewById(R.id.keyguard_indication_text);
        this.mTextView = keyguardIndicationTextView;
        this.mInitialTextColorState = keyguardIndicationTextView != null ? keyguardIndicationTextView.getTextColors() : ColorStateList.valueOf(-1);
        this.mChargingIndicationView = (LottieAnimationView) viewGroup.findViewById(R.id.charging_indication);
        updateChargingIndicationStyle();
        if (hasActiveInDisplayFp()) {
            try {
                this.mFODPositionY = IFingerprintInscreen.getService().getPositionY();
            } catch (RemoteException unused) {
            }
            if (this.mFODPositionY <= 0) {
                this.mFODPositionY = 0;
            }
        }
        KeyguardIndicationTextView keyguardIndicationTextView2 = (KeyguardIndicationTextView) viewGroup.findViewById(R.id.keyguard_indication_enterprise_disclosure);
        this.mDisclosure = keyguardIndicationTextView2;
        this.mDisclosureMaxAlpha = keyguardIndicationTextView2.getAlpha();
        updateIndication(false);
        updateDisclosure();
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.2
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    KeyguardIndicationController.this.updateDisclosure();
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
            intentFilter.addAction("android.intent.action.USER_REMOVED");
            this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        }
    }

    public void setLockIconController(LockscreenLockIconController lockscreenLockIconController) {
        this.mLockIconController = lockscreenLockIconController;
    }

    protected KeyguardUpdateMonitorCallback getKeyguardCallback() {
        if (this.mUpdateMonitorCallback == null) {
            this.mUpdateMonitorCallback = new BaseKeyguardCallback();
        }
        return this.mUpdateMonitorCallback;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDisclosure() {
        if (((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.KeyguardIndicationController$$ExternalSyntheticLambda1
            @Override // java.util.function.Supplier
            public final Object get() {
                return Boolean.valueOf(this.f$0.isOrganizationOwnedDevice());
            }
        })).booleanValue()) {
            CharSequence organizationOwnedDeviceOrganizationName = getOrganizationOwnedDeviceOrganizationName();
            if (organizationOwnedDeviceOrganizationName != null) {
                this.mDisclosure.switchIndication(this.mContext.getResources().getString(R.string.do_disclosure_with_name, organizationOwnedDeviceOrganizationName));
            } else {
                this.mDisclosure.switchIndication(R.string.do_disclosure_generic);
            }
            this.mDisclosure.setVisibility(0);
            return;
        }
        this.mDisclosure.setVisibility(8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isOrganizationOwnedDevice() {
        return this.mDevicePolicyManager.isDeviceManaged() || this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile();
    }

    private CharSequence getOrganizationOwnedDeviceOrganizationName() {
        if (this.mDevicePolicyManager.isDeviceManaged()) {
            return this.mDevicePolicyManager.getDeviceOwnerOrganizationName();
        }
        if (this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile()) {
            return getWorkProfileOrganizationName();
        }
        return null;
    }

    private CharSequence getWorkProfileOrganizationName() {
        int workProfileUserId = getWorkProfileUserId(UserHandle.myUserId());
        if (workProfileUserId == -10000) {
            return null;
        }
        return this.mDevicePolicyManager.getOrganizationNameForUser(workProfileUserId);
    }

    private int getWorkProfileUserId(int i) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        this.mIndicationArea.setVisibility(z ? 0 : 8);
        if (z) {
            if (!this.mHandler.hasMessages(1)) {
                hideTransientIndication();
            }
            updateIndication(false);
        } else {
            if (z) {
                return;
            }
            hideTransientIndication();
        }
    }

    @VisibleForTesting
    String getTrustGrantedIndication() {
        return this.mContext.getString(R.string.keyguard_indication_trust_unlocked);
    }

    @VisibleForTesting
    void setPowerPluggedIn(boolean z) {
        this.mPowerPluggedIn = z;
    }

    public void hideTransientIndicationDelayed(long j) {
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), j);
    }

    public void showTransientIndication(int i) {
        showTransientIndication(this.mContext.getResources().getString(i));
    }

    public void showTransientIndication(CharSequence charSequence) throws Resources.NotFoundException {
        showTransientIndication(charSequence, false, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTransientIndication(CharSequence charSequence, boolean z, boolean z2) throws Resources.NotFoundException {
        this.mTransientIndication = charSequence;
        this.mHideTransientMessageOnScreenOff = z2 && charSequence != null;
        this.mTransientTextIsError = z;
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        if (this.mDozing && !TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(true);
            hideTransientIndicationDelayed(5000L);
        }
        updateIndication(false);
    }

    public void hideTransientIndication() throws Resources.NotFoundException {
        if (this.mTransientIndication != null) {
            this.mTransientIndication = null;
            this.mHideTransientMessageOnScreenOff = false;
            this.mHandler.removeMessages(1);
            updateIndication(false);
        }
    }

    protected final void updateIndication(boolean z) throws Resources.NotFoundException {
        boolean z2 = false;
        if (TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(false);
        }
        if (this.mVisible) {
            if (this.mDozing) {
                this.mTextView.setTextColor(-1);
                if (!TextUtils.isEmpty(this.mTransientIndication)) {
                    this.mTextView.switchIndication(this.mTransientIndication);
                } else if (!TextUtils.isEmpty(this.mAlignmentIndication)) {
                    this.mTextView.switchIndication(this.mAlignmentIndication);
                    this.mTextView.setTextColor(this.mContext.getColor(R.color.misalignment_text_color));
                } else if (this.mPowerPluggedIn || this.mEnableBatteryDefender) {
                    String strComputePowerIndication = computePowerIndication();
                    if (z) {
                        animateText(this.mTextView, strComputePowerIndication);
                    } else {
                        this.mTextView.switchIndication(strComputePowerIndication);
                    }
                } else {
                    this.mTextView.switchIndication(NumberFormat.getPercentInstance().format(this.mBatteryLevel / 100.0f));
                }
                updateChargingIndication();
                return;
            }
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            String trustGrantedIndication = getTrustGrantedIndication();
            String trustManagedIndication = getTrustManagedIndication();
            String strComputePowerIndication2 = (this.mPowerPluggedIn || this.mEnableBatteryDefender) ? computePowerIndication() : null;
            if (!this.mKeyguardUpdateMonitor.isUserUnlocked(currentUser)) {
                this.mTextView.switchIndication(android.R.string.icu_abbrev_wday_month_day_no_year);
            } else if (!TextUtils.isEmpty(this.mTransientIndication)) {
                this.mTextView.switchIndication(this.mTransientIndication);
                z2 = this.mTransientTextIsError;
            } else if (TextUtils.isEmpty(trustGrantedIndication) || !this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                if (!TextUtils.isEmpty(this.mAlignmentIndication)) {
                    this.mTextView.switchIndication(this.mAlignmentIndication);
                    z2 = true;
                } else if (this.mPowerPluggedIn || this.mEnableBatteryDefender) {
                    if (z) {
                        animateText(this.mTextView, strComputePowerIndication2);
                    } else {
                        this.mTextView.switchIndication(strComputePowerIndication2);
                    }
                } else if (!TextUtils.isEmpty(trustManagedIndication) && this.mKeyguardUpdateMonitor.getUserTrustIsManaged(currentUser) && !this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                    this.mTextView.switchIndication(trustManagedIndication);
                } else {
                    this.mTextView.switchIndication(this.mRestingIndication);
                }
            } else if (strComputePowerIndication2 != null) {
                this.mTextView.switchIndication(this.mContext.getResources().getString(R.string.keyguard_indication_trust_unlocked_plugged_in, trustGrantedIndication, strComputePowerIndication2));
            } else {
                this.mTextView.switchIndication(trustGrantedIndication);
            }
            this.mTextView.setTextColor(z2 ? Utils.getColorError(this.mContext) : this.mInitialTextColorState);
            updateChargingIndication();
        }
    }

    public void updateChargingIndicationStyle() {
        int i = this.mChargingIndication;
        if (i == 2) {
            this.mChargingIndicationView.setFileName("keyguard_charge_battery.json");
            this.mChargingIndicationView.getLayoutParams().height = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_charging_indication_width);
            this.mChargingIndicationView.getLayoutParams().width = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_charging_indication_height);
            return;
        }
        if (i == 3) {
            this.mChargingIndicationView.setFileName("keyguard_charge_drop.json");
            ViewGroup.LayoutParams layoutParams = this.mChargingIndicationView.getLayoutParams();
            Resources resources = this.mContext.getResources();
            int i2 = R.dimen.keyguard_charging_indication_height;
            layoutParams.height = resources.getDimensionPixelSize(i2);
            this.mChargingIndicationView.getLayoutParams().width = this.mContext.getResources().getDimensionPixelSize(i2);
            return;
        }
        if (i == 4) {
            this.mChargingIndicationView.setFileName("keyguard_charge_explosion.json");
            ViewGroup.LayoutParams layoutParams2 = this.mChargingIndicationView.getLayoutParams();
            Resources resources2 = this.mContext.getResources();
            int i3 = R.dimen.keyguard_charging_indication_height;
            layoutParams2.height = resources2.getDimensionPixelSize(i3);
            this.mChargingIndicationView.getLayoutParams().width = this.mContext.getResources().getDimensionPixelSize(i3);
            return;
        }
        if (i != 5) {
            this.mChargingIndicationView.setFileName("keyguard_charging_indication.json");
            this.mChargingIndicationView.getLayoutParams().height = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_charging_indication_height);
            this.mChargingIndicationView.getLayoutParams().width = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_charging_indication_width);
            return;
        }
        this.mChargingIndicationView.setFileName("keyguard_charge_water.json");
        ViewGroup.LayoutParams layoutParams3 = this.mChargingIndicationView.getLayoutParams();
        Resources resources3 = this.mContext.getResources();
        int i4 = R.dimen.keyguard_charging_indication_height;
        layoutParams3.height = resources3.getDimensionPixelSize(i4);
        this.mChargingIndicationView.getLayoutParams().width = this.mContext.getResources().getDimensionPixelSize(i4);
    }

    private void updateChargingIndication() throws Resources.NotFoundException {
        LottieAnimationView lottieAnimationView = this.mChargingIndicationView;
        if (lottieAnimationView == null) {
            return;
        }
        if (this.mChargingIndication > 0 && !this.mDozing && !this.mPowerCharged && this.mPowerPluggedIn) {
            if (hasActiveInDisplayFp() && this.mFODPositionY != 0) {
                Display defaultDisplay = ((WindowManager) this.mContext.getSystemService(WindowManager.class)).getDefaultDisplay();
                Point point = new Point();
                defaultDisplay.getRealSize(point);
                int i = point.y;
                int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_width);
                boolean z = this.mContext.getResources().getBoolean(android.R.bool.config_cecQuerySadDtshdDisabled_allowed);
                int i2 = this.mFODPositionY;
                if (z) {
                    i2 -= dimensionPixelSize;
                }
                int measuredHeight = ((i - i2) - (this.mTextView.getMeasuredHeight() + this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_indication_margin_bottom_fingerprint_in_display))) + 10;
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mChargingIndicationView.getLayoutParams();
                marginLayoutParams.setMargins(0, 0, 0, measuredHeight);
                this.mChargingIndicationView.setLayoutParams(marginLayoutParams);
            }
            this.mChargingIndicationView.setVisibility(0);
            this.mChargingIndicationView.playAnimation();
            return;
        }
        lottieAnimationView.setVisibility(8);
    }

    private boolean hasActiveInDisplayFp() {
        return this.mContext.getPackageManager().hasSystemFeature("vendor.lineage.biometrics.fingerprint.inscreen") && ((FingerprintManager) this.mContext.getSystemService("fingerprint")).getEnrolledFingerprints(KeyguardUpdateMonitor.getCurrentUser()).size() > 0;
    }

    private void animateText(final KeyguardIndicationTextView keyguardIndicationTextView, final String str) throws Resources.NotFoundException {
        int integer = this.mContext.getResources().getInteger(R.integer.wired_charging_keyguard_text_animation_distance);
        int integer2 = this.mContext.getResources().getInteger(R.integer.wired_charging_keyguard_text_animation_duration_up);
        final int integer3 = this.mContext.getResources().getInteger(R.integer.wired_charging_keyguard_text_animation_duration_down);
        keyguardIndicationTextView.animate().cancel();
        ViewClippingUtil.setClippingDeactivated(keyguardIndicationTextView, true, this.mClippingParams);
        keyguardIndicationTextView.animate().translationYBy(integer).setInterpolator(Interpolators.LINEAR).setDuration(integer2).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.3
            private boolean mCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                keyguardIndicationTextView.switchIndication(str);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                keyguardIndicationTextView.setTranslationY(0.0f);
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    ViewClippingUtil.setClippingDeactivated(keyguardIndicationTextView, false, KeyguardIndicationController.this.mClippingParams);
                } else {
                    keyguardIndicationTextView.animate().setDuration(integer3).setInterpolator(Interpolators.BOUNCE).translationY(0.0f).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.3.1
                        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                        public void onAnimationEnd(Animator animator2) {
                            keyguardIndicationTextView.setTranslationY(0.0f);
                            AnonymousClass3 anonymousClass3 = AnonymousClass3.this;
                            ViewClippingUtil.setClippingDeactivated(keyguardIndicationTextView, false, KeyguardIndicationController.this.mClippingParams);
                        }
                    });
                }
            }
        });
    }

    protected String computePowerIndication() throws Resources.NotFoundException {
        int i;
        int i2;
        int i3;
        String string;
        String str;
        String str2;
        String str3;
        this.mCurrentDivider = this.mContext.getResources().getInteger(R.integer.config_currentInfoDivider);
        if (this.mPowerCharged) {
            return this.mContext.getResources().getString(R.string.keyguard_charged);
        }
        String str4 = NumberFormat.getPercentInstance().format(this.mBatteryLevel / 100.0f);
        if (this.mBatteryOverheated) {
            return this.mContext.getResources().getString(R.string.keyguard_plugged_in_charging_limited, str4);
        }
        boolean z = this.mChargingTimeRemaining > 0;
        if (this.mPowerPluggedInWired) {
            int i4 = this.mChargingSpeed;
            if (i4 != 0) {
                switch (i4) {
                    case 2:
                        if (z) {
                            i = R.string.keyguard_indication_charging_time_fast;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_charging_fast;
                            break;
                        }
                    case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                        if (z) {
                            i = R.string.keyguard_indication_dash_charging_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_dash_charging;
                            break;
                        }
                    case 4:
                        if (z) {
                            i = R.string.keyguard_indication_warp_charging_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_warp_charging;
                            break;
                        }
                    case 5:
                        if (z) {
                            i = R.string.keyguard_indication_vooc_charging_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_vooc_charging;
                            break;
                        }
                    case 6:
                        if (z) {
                            i = R.string.keyguard_indication_turbo_power_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_turbo_charging;
                            break;
                        }
                    case 7:
                        if (z) {
                            i = R.string.keyguard_indication_smart_charging_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_smart_charging;
                            break;
                        }
                    case QS.VERSION /* 8 */:
                        if (z) {
                            i = R.string.keyguard_indication_swarp_charging_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in_swarp_charging;
                            break;
                        }
                    default:
                        if (z) {
                            i = R.string.keyguard_indication_charging_time;
                            break;
                        } else {
                            i = R.string.keyguard_plugged_in;
                            break;
                        }
                }
            } else if (z) {
                i = R.string.keyguard_indication_charging_time_slowly;
            } else {
                i = R.string.keyguard_plugged_in_charging_slowly;
            }
        } else if (z) {
            i = R.string.keyguard_indication_charging_time_wireless;
        } else {
            i = R.string.keyguard_plugged_in_wireless;
        }
        String str5 = "";
        if (this.mShowBatteryInfo) {
            if (this.mChargingCurrent > 0) {
                string = "" + (this.mChargingCurrent / this.mCurrentDivider) + "mA";
            } else {
                string = "";
            }
            if (this.mChargingWattage > 0.0d) {
                StringBuilder sb = new StringBuilder();
                if (string == "") {
                    str3 = "";
                } else {
                    str3 = string + " · ";
                }
                sb.append(str3);
                i2 = i;
                sb.append(String.format("%.1f", Double.valueOf((this.mChargingWattage / this.mCurrentDivider) / 1000.0d)));
                sb.append("W");
                string = sb.toString();
            } else {
                i2 = i;
            }
            if (this.mChargingVoltage > 0.0d) {
                StringBuilder sb2 = new StringBuilder();
                if (string == "") {
                    str2 = "";
                } else {
                    str2 = string + " · ";
                }
                sb2.append(str2);
                sb2.append(String.format("%.1f", Double.valueOf((this.mChargingVoltage / 1000.0d) / 1000.0d)));
                sb2.append("V");
                string = sb2.toString();
            }
            if (this.mTemperature > 0.0f) {
                StringBuilder sb3 = new StringBuilder();
                if (string == "") {
                    str = "";
                } else {
                    str = string + " · ";
                }
                sb3.append(str);
                sb3.append(this.mTemperature / 10.0f);
                sb3.append("°C");
                string = sb3.toString();
            }
            if (string != "") {
                str5 = "\n" + string;
            } else {
                str5 = string;
            }
        } else {
            i2 = i;
        }
        if (z) {
            String shortElapsedTimeRoundingUpToMinutes = Formatter.formatShortElapsedTimeRoundingUpToMinutes(this.mContext, this.mChargingTimeRemaining);
            try {
                i3 = i2;
            } catch (IllegalFormatConversionException unused) {
                i3 = i2;
            }
            try {
                return this.mContext.getResources().getString(i3, shortElapsedTimeRoundingUpToMinutes, str4) + str5;
            } catch (IllegalFormatConversionException unused2) {
                return this.mContext.getResources().getString(i3, shortElapsedTimeRoundingUpToMinutes) + str5;
            }
        }
        int i5 = i2;
        try {
            return this.mContext.getResources().getString(i5, str4) + str5;
        } catch (IllegalFormatConversionException unused3) {
            return this.mContext.getResources().getString(i5) + str5;
        }
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showSwipeUpToUnlock() throws Resources.NotFoundException {
        if (this.mDozing) {
            return;
        }
        if (this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
            this.mStatusBarKeyguardViewManager.showBouncerMessage(this.mContext.getString(R.string.keyguard_retry), this.mInitialTextColorState);
        } else if (this.mKeyguardUpdateMonitor.isScreenOn()) {
            showTransientIndication(this.mContext.getString(R.string.keyguard_unlock), false, true);
            hideTransientIndicationDelayed(5000L);
        }
    }

    public void setDozing(boolean z) throws Resources.NotFoundException {
        if (this.mDozing == z) {
            return;
        }
        this.mDozing = z;
        if (this.mHideTransientMessageOnScreenOff && z) {
            hideTransientIndication();
        } else {
            updateIndication(false);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardIndicationController:");
        printWriter.println("  mTransientTextIsError: " + this.mTransientTextIsError);
        printWriter.println("  mInitialTextColorState: " + this.mInitialTextColorState);
        printWriter.println("  mPowerPluggedInWired: " + this.mPowerPluggedInWired);
        printWriter.println("  mPowerPluggedIn: " + this.mPowerPluggedIn);
        printWriter.println("  mPowerCharged: " + this.mPowerCharged);
        printWriter.println("  mChargingSpeed: " + this.mChargingSpeed);
        printWriter.println("  mChargingWattage: " + this.mChargingWattage);
        printWriter.println("  mMessageToShowOnScreenOn: " + this.mMessageToShowOnScreenOn);
        printWriter.println("  mDozing: " + this.mDozing);
        printWriter.println("  mBatteryLevel: " + this.mBatteryLevel);
        StringBuilder sb = new StringBuilder();
        sb.append("  mTextView.getText(): ");
        KeyguardIndicationTextView keyguardIndicationTextView = this.mTextView;
        sb.append((Object) (keyguardIndicationTextView == null ? null : keyguardIndicationTextView.getText()));
        printWriter.println(sb.toString());
        printWriter.println("  computePowerIndication(): " + computePowerIndication());
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) throws Resources.NotFoundException {
        setDozing(z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozeAmountChanged(float f, float f2) {
        this.mDisclosure.setAlpha((1.0f - f) * this.mDisclosureMaxAlpha);
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() throws Resources.NotFoundException {
        updateIndication(!this.mDozing);
    }

    protected class BaseKeyguardCallback extends KeyguardUpdateMonitorCallback {
        protected BaseKeyguardCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(BatteryStatus batteryStatus) throws Resources.NotFoundException {
            int i = batteryStatus.status;
            boolean z = false;
            boolean z2 = i == 2 || i == 5;
            boolean z3 = KeyguardIndicationController.this.mPowerPluggedIn;
            KeyguardIndicationController.this.mPowerPluggedInWired = batteryStatus.isPluggedInWired() && z2;
            KeyguardIndicationController.this.mPowerPluggedIn = batteryStatus.isPluggedIn() && z2;
            KeyguardIndicationController.this.mPowerCharged = batteryStatus.isCharged();
            KeyguardIndicationController.this.mChargingCurrent = batteryStatus.maxChargingCurrent;
            KeyguardIndicationController.this.mChargingVoltage = batteryStatus.maxChargingVoltage;
            KeyguardIndicationController.this.mChargingWattage = batteryStatus.maxChargingWattage;
            KeyguardIndicationController.this.mBatteryOverheated = batteryStatus.isOverheated();
            KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
            keyguardIndicationController.mEnableBatteryDefender = keyguardIndicationController.mBatteryOverheated && batteryStatus.isPluggedIn();
            KeyguardIndicationController keyguardIndicationController2 = KeyguardIndicationController.this;
            keyguardIndicationController2.mChargingSpeed = batteryStatus.getChargingSpeed(keyguardIndicationController2.mContext);
            KeyguardIndicationController.this.mTemperature = batteryStatus.temperature;
            KeyguardIndicationController.this.mBatteryLevel = batteryStatus.level;
            try {
                KeyguardIndicationController keyguardIndicationController3 = KeyguardIndicationController.this;
                keyguardIndicationController3.mChargingTimeRemaining = keyguardIndicationController3.mPowerPluggedIn ? KeyguardIndicationController.this.mBatteryInfo.computeChargeTimeRemaining() : -1L;
            } catch (RemoteException e) {
                Log.e("KeyguardIndication", "Error calling IBatteryStats: ", e);
                KeyguardIndicationController.this.mChargingTimeRemaining = -1L;
            }
            KeyguardIndicationController keyguardIndicationController4 = KeyguardIndicationController.this;
            if (!z3 && keyguardIndicationController4.mPowerPluggedInWired) {
                z = true;
            }
            keyguardIndicationController4.updateIndication(z);
            if (KeyguardIndicationController.this.mDozing) {
                if (z3 || !KeyguardIndicationController.this.mPowerPluggedIn) {
                    if (!z3 || KeyguardIndicationController.this.mPowerPluggedIn) {
                        return;
                    }
                    KeyguardIndicationController.this.hideTransientIndication();
                    return;
                }
                KeyguardIndicationController keyguardIndicationController5 = KeyguardIndicationController.this;
                keyguardIndicationController5.showTransientIndication(keyguardIndicationController5.computePowerIndication());
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000L);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) throws Resources.NotFoundException {
            if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true)) {
                boolean z = i == -2;
                if (!KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isScreenOn()) {
                        KeyguardIndicationController.this.showTransientIndication(str, false, z);
                        if (!z) {
                            KeyguardIndicationController.this.hideTransientIndicationDelayed(1300L);
                        }
                    }
                } else {
                    KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, KeyguardIndicationController.this.mInitialTextColorState);
                }
                if (z) {
                    KeyguardIndicationController.this.mHandler.sendMessageDelayed(KeyguardIndicationController.this.mHandler.obtainMessage(3), 1300L);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) throws Resources.NotFoundException {
            if (shouldSuppressBiometricError(i, biometricSourceType, KeyguardIndicationController.this.mKeyguardUpdateMonitor)) {
                return;
            }
            animatePadlockError();
            if (i == 3) {
                KeyguardIndicationController.this.showSwipeUpToUnlock();
                return;
            }
            if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, KeyguardIndicationController.this.mInitialTextColorState);
            } else if (!KeyguardIndicationController.this.mKeyguardUpdateMonitor.isScreenOn()) {
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = str;
            } else {
                KeyguardIndicationController.this.showTransientIndication(str);
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000L);
            }
        }

        private void animatePadlockError() {
            if (KeyguardIndicationController.this.mLockIconController != null) {
                KeyguardIndicationController.this.mLockIconController.setTransientBiometricsError(true);
            }
            KeyguardIndicationController.this.mHandler.removeMessages(2);
            KeyguardIndicationController.this.mHandler.sendMessageDelayed(KeyguardIndicationController.this.mHandler.obtainMessage(2), 1300L);
        }

        private boolean shouldSuppressBiometricError(int i, BiometricSourceType biometricSourceType, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                return shouldSuppressFingerprintError(i, keyguardUpdateMonitor);
            }
            if (biometricSourceType == BiometricSourceType.FACE) {
                return shouldSuppressFaceError(i, keyguardUpdateMonitor);
            }
            return false;
        }

        private boolean shouldSuppressFingerprintError(int i, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            return !(keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) || i == 9) || i == 5;
        }

        private boolean shouldSuppressFaceError(int i, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            return !(keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) || i == 9) || i == 5;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustAgentErrorMessage(CharSequence charSequence) throws Resources.NotFoundException {
            KeyguardIndicationController.this.showTransientIndication(charSequence, true, false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() throws Resources.NotFoundException {
            if (KeyguardIndicationController.this.mMessageToShowOnScreenOn != null) {
                KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                keyguardIndicationController.showTransientIndication(keyguardIndicationController.mMessageToShowOnScreenOn, true, false);
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000L);
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) throws Resources.NotFoundException {
            if (z) {
                KeyguardIndicationController.this.hideTransientIndication();
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            super.onBiometricAuthenticated(i, biometricSourceType, z);
            KeyguardIndicationController.this.mHandler.sendEmptyMessage(1);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) throws Resources.NotFoundException {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() throws Resources.NotFoundException {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }
    }
}
