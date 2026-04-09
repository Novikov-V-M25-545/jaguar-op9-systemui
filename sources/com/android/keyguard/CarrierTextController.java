package com.android.keyguard;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.WirelessUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/* loaded from: classes.dex */
public class CarrierTextController {
    private final Handler mBgHandler;
    private CarrierTextCallback mCarrierTextCallback;
    private Context mContext;
    protected KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final Handler mMainHandler;
    private CharSequence mSeparator;
    private boolean mShowAirplaneMode;
    private boolean mShowMissingSim;
    private boolean[] mSimErrorState;
    private final int mSimSlotsNumber;
    private boolean mTelephonyCapable;
    private WifiManager mWifiManager;
    private final AtomicBoolean mNetworkSupported = new AtomicBoolean();
    private final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() { // from class: com.android.keyguard.CarrierTextController.1
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            CarrierTextCallback carrierTextCallback = CarrierTextController.this.mCarrierTextCallback;
            if (carrierTextCallback != null) {
                carrierTextCallback.finishedWakingUp();
            }
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            CarrierTextCallback carrierTextCallback = CarrierTextController.this.mCarrierTextCallback;
            if (carrierTextCallback != null) {
                carrierTextCallback.startedGoingToSleep();
            }
        }
    };
    protected final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.CarrierTextController.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshCarrierInfo() {
            CarrierTextController.this.updateCarrierText();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTelephonyCapable(boolean z) {
            CarrierTextController.this.mTelephonyCapable = z;
            CarrierTextController.this.updateCarrierText();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSimStateChanged(int i, int i2, int i3) {
            if (i2 >= 0 && i2 < CarrierTextController.this.mSimSlotsNumber) {
                if (CarrierTextController.this.getStatusForIccState(i3) == StatusMode.SimIoError) {
                    CarrierTextController.this.mSimErrorState[i2] = true;
                    CarrierTextController.this.updateCarrierText();
                    return;
                } else {
                    if (CarrierTextController.this.mSimErrorState[i2]) {
                        CarrierTextController.this.mSimErrorState[i2] = false;
                        CarrierTextController.this.updateCarrierText();
                        return;
                    }
                    return;
                }
            }
            Log.d("CarrierTextController", "onSimStateChanged() - slotId invalid: " + i2 + " mTelephonyCapable: " + Boolean.toString(CarrierTextController.this.mTelephonyCapable));
        }
    };
    private int mActiveMobileDataSubscription = -1;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() { // from class: com.android.keyguard.CarrierTextController.3
        @Override // android.telephony.PhoneStateListener
        public void onActiveDataSubscriptionIdChanged(int i) {
            CarrierTextController.this.mActiveMobileDataSubscription = i;
            if (!CarrierTextController.this.mNetworkSupported.get() || CarrierTextController.this.mCarrierTextCallback == null) {
                return;
            }
            CarrierTextController.this.updateCarrierText();
        }
    };
    private final boolean mIsEmergencyCallCapable = getTelephonyManager().isVoiceCapable();
    private WakefulnessLifecycle mWakefulnessLifecycle = (WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class);

    public interface CarrierTextCallback {
        default void finishedWakingUp() {
        }

        default void startedGoingToSleep() {
        }

        default void updateCarrierInfo(CarrierTextCallbackInfo carrierTextCallbackInfo) {
        }
    }

    private enum StatusMode {
        Normal,
        NetworkLocked,
        SimMissing,
        SimMissingLocked,
        SimPukLocked,
        SimLocked,
        SimPermDisabled,
        SimNotReady,
        SimIoError,
        SimUnknown
    }

    public CarrierTextController(Context context, CharSequence charSequence, boolean z, boolean z2) {
        this.mContext = context;
        this.mShowAirplaneMode = z;
        this.mShowMissingSim = z2;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mSeparator = charSequence;
        int supportedModemCount = getTelephonyManager().getSupportedModemCount();
        this.mSimSlotsNumber = supportedModemCount;
        this.mSimErrorState = new boolean[supportedModemCount];
        this.mMainHandler = (Handler) Dependency.get(Dependency.MAIN_HANDLER);
        Handler handler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mBgHandler = handler;
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        handler.post(new Runnable() { // from class: com.android.keyguard.CarrierTextController$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        boolean zIsNetworkSupported = ConnectivityManager.from(this.mContext).isNetworkSupported(0);
        if (zIsNetworkSupported && this.mNetworkSupported.compareAndSet(false, zIsNetworkSupported)) {
            lambda$setListening$4(this.mCarrierTextCallback);
        }
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService("phone");
    }

    private CharSequence updateCarrierTextWithSimIoError(CharSequence charSequence, CharSequence[] charSequenceArr, int[] iArr, boolean z) {
        CharSequence carrierTextForSimState = getCarrierTextForSimState(8, "");
        for (int i = 0; i < getTelephonyManager().getActiveModemCount(); i++) {
            if (this.mSimErrorState[i]) {
                if (z) {
                    return concatenate(carrierTextForSimState, getContext().getText(R.string.console_running_notification_title), this.mSeparator);
                }
                if (iArr[i] != -1) {
                    int i2 = iArr[i];
                    charSequenceArr[i2] = concatenate(carrierTextForSimState, charSequenceArr[i2], this.mSeparator);
                } else {
                    charSequence = concatenate(charSequence, carrierTextForSimState, this.mSeparator);
                }
            }
        }
        return charSequence;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: handleSetListening, reason: merged with bridge method [inline-methods] */
    public void lambda$setListening$4(final CarrierTextCallback carrierTextCallback) {
        TelephonyManager telephonyManager = getTelephonyManager();
        if (carrierTextCallback != null) {
            this.mCarrierTextCallback = carrierTextCallback;
            if (this.mNetworkSupported.get()) {
                this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.CarrierTextController$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$handleSetListening$1();
                    }
                });
                this.mWakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
                telephonyManager.listen(this.mPhoneStateListener, 4194304);
                return;
            }
            this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.CarrierTextController$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    CarrierTextController.lambda$handleSetListening$2(carrierTextCallback);
                }
            });
            return;
        }
        this.mCarrierTextCallback = null;
        this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.CarrierTextController$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$handleSetListening$3();
            }
        });
        this.mWakefulnessLifecycle.removeObserver(this.mWakefulnessObserver);
        telephonyManager.listen(this.mPhoneStateListener, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleSetListening$1() {
        this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$handleSetListening$2(CarrierTextCallback carrierTextCallback) {
        carrierTextCallback.updateCarrierInfo(new CarrierTextCallbackInfo("", null, false, null));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleSetListening$3() {
        this.mKeyguardUpdateMonitor.removeCallback(this.mCallback);
    }

    public void setListening(final CarrierTextCallback carrierTextCallback) {
        this.mBgHandler.post(new Runnable() { // from class: com.android.keyguard.CarrierTextController$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setListening$4(carrierTextCallback);
            }
        });
    }

    protected List<SubscriptionInfo> getSubscriptionInfo() {
        return this.mKeyguardUpdateMonitor.getFilteredSubscriptionInfo(false);
    }

    protected void updateCarrierText() {
        boolean z;
        CharSequence airplaneModeMessage;
        ServiceState serviceState;
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo();
        int size = subscriptionInfo.size();
        int[] iArr = new int[size];
        int[] iArr2 = new int[this.mSimSlotsNumber];
        for (int i = 0; i < this.mSimSlotsNumber; i++) {
            iArr2[i] = -1;
        }
        CharSequence[] charSequenceArr = new CharSequence[size];
        int i2 = 0;
        boolean z2 = false;
        boolean z3 = true;
        while (true) {
            if (i2 >= size) {
                break;
            }
            int subscriptionId = subscriptionInfo.get(i2).getSubscriptionId();
            charSequenceArr[i2] = "";
            iArr[i2] = subscriptionId;
            iArr2[subscriptionInfo.get(i2).getSimSlotIndex()] = i2;
            int simState = this.mKeyguardUpdateMonitor.getSimState(subscriptionId);
            CharSequence carrierTextForSimState = getCarrierTextForSimState(simState, subscriptionInfo.get(i2).getCarrierName());
            if (carrierTextForSimState != null) {
                charSequenceArr[i2] = carrierTextForSimState;
                z3 = false;
            }
            if (simState == 5 && (serviceState = this.mKeyguardUpdateMonitor.mServiceStates.get(Integer.valueOf(subscriptionId))) != null && serviceState.getDataRegistrationState() == 0 && (serviceState.getRilDataRadioTechnology() != 18 || (this.mWifiManager.isWifiEnabled() && this.mWifiManager.getConnectionInfo() != null && this.mWifiManager.getConnectionInfo().getBSSID() != null))) {
                z2 = true;
            }
            i2++;
        }
        int presentSubId = this.mKeyguardUpdateMonitor.getPresentSubId();
        CharSequence carrierTextForSimState2 = (size >= TelephonyManager.getDefault().getPhoneCount() || !this.mKeyguardUpdateMonitor.isEmergencyOnly() || presentSubId == -1) ? null : getCarrierTextForSimState(this.mKeyguardUpdateMonitor.getSimState(presentSubId), getContext().getText(R.string.console_running_notification_title));
        if (z3 && !z2) {
            if (size != 0) {
                carrierTextForSimState2 = makeCarrierStringOnEmergencyCapable(getMissingSimMessage(), subscriptionInfo.get(0).getCarrierName());
            } else {
                CharSequence text = getContext().getText(R.string.console_running_notification_title);
                Intent intentRegisterReceiver = getContext().registerReceiver(null, new IntentFilter("android.telephony.action.SERVICE_PROVIDERS_UPDATED"));
                if (intentRegisterReceiver != null) {
                    String stringExtra = intentRegisterReceiver.getBooleanExtra("android.telephony.extra.SHOW_SPN", false) ? intentRegisterReceiver.getStringExtra("android.telephony.extra.SPN") : "";
                    String stringExtra2 = intentRegisterReceiver.getBooleanExtra("android.telephony.extra.SHOW_PLMN", false) ? intentRegisterReceiver.getStringExtra("android.telephony.extra.PLMN") : "";
                    text = Objects.equals(stringExtra2, stringExtra) ? stringExtra2 : concatenate(stringExtra2, stringExtra, this.mSeparator);
                }
                carrierTextForSimState2 = makeCarrierStringOnEmergencyCapable(getMissingSimMessage(), text);
            }
        }
        if (TextUtils.isEmpty(carrierTextForSimState2)) {
            carrierTextForSimState2 = joinNotEmpty(this.mSeparator, charSequenceArr);
        }
        CharSequence charSequenceUpdateCarrierTextWithSimIoError = updateCarrierTextWithSimIoError(carrierTextForSimState2, charSequenceArr, iArr2, z3);
        if (z2 || !WirelessUtils.isAirplaneModeOn(this.mContext)) {
            z = false;
            airplaneModeMessage = charSequenceUpdateCarrierTextWithSimIoError;
        } else {
            airplaneModeMessage = getAirplaneModeMessage();
            z = true;
        }
        postToCallback(new CarrierTextCallbackInfo(airplaneModeMessage, charSequenceArr, true ^ z3, iArr, z));
    }

    protected void postToCallback(final CarrierTextCallbackInfo carrierTextCallbackInfo) {
        final CarrierTextCallback carrierTextCallback = this.mCarrierTextCallback;
        if (carrierTextCallback != null) {
            this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.CarrierTextController$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    carrierTextCallback.updateCarrierInfo(carrierTextCallbackInfo);
                }
            });
        }
    }

    private Context getContext() {
        return this.mContext;
    }

    private String getMissingSimMessage() {
        return (this.mShowMissingSim && this.mTelephonyCapable) ? getContext().getString(com.android.systemui.R.string.keyguard_missing_sim_message_short) : "";
    }

    private String getAirplaneModeMessage() {
        return this.mShowAirplaneMode ? getContext().getString(com.android.systemui.R.string.airplane_mode) : "";
    }

    /* renamed from: com.android.keyguard.CarrierTextController$4, reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode;

        static {
            int[] iArr = new int[StatusMode.values().length];
            $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode = iArr;
            try {
                iArr[StatusMode.Normal.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimNotReady.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.NetworkLocked.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimMissing.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimPermDisabled.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimMissingLocked.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimLocked.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimPukLocked.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimIoError.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimUnknown.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
        }
    }

    private CharSequence getCarrierTextForSimState(int i, CharSequence charSequence) {
        switch (AnonymousClass4.$SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[getStatusForIccState(i).ordinal()]) {
            case 1:
                return charSequence;
            case 2:
                return "";
            case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                return makeCarrierStringOnEmergencyCapable(this.mContext.getText(com.android.systemui.R.string.keyguard_network_locked_message), charSequence);
            case 4:
            case 6:
            case 10:
            default:
                return null;
            case 5:
                return makeCarrierStringOnEmergencyCapable(getContext().getText(com.android.systemui.R.string.keyguard_permanent_disabled_sim_message_short), charSequence);
            case 7:
                return makeCarrierStringOnLocked(getContext().getText(com.android.systemui.R.string.keyguard_sim_locked_message), charSequence);
            case QS.VERSION /* 8 */:
                return makeCarrierStringOnLocked(getContext().getText(com.android.systemui.R.string.keyguard_sim_puk_locked_message), charSequence);
            case 9:
                return makeCarrierStringOnEmergencyCapable(getContext().getText(com.android.systemui.R.string.keyguard_sim_error_message_short), charSequence);
        }
    }

    private CharSequence makeCarrierStringOnEmergencyCapable(CharSequence charSequence, CharSequence charSequence2) {
        return this.mIsEmergencyCallCapable ? concatenate(charSequence, charSequence2, this.mSeparator) : charSequence;
    }

    private CharSequence makeCarrierStringOnLocked(CharSequence charSequence, CharSequence charSequence2) {
        boolean z = !TextUtils.isEmpty(charSequence);
        boolean z2 = !TextUtils.isEmpty(charSequence2);
        return (z && z2) ? this.mContext.getString(com.android.systemui.R.string.keyguard_carrier_name_with_sim_locked_template, charSequence2, charSequence) : z ? charSequence : z2 ? charSequence2 : "";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public StatusMode getStatusForIccState(int i) {
        boolean z = true;
        if (this.mKeyguardUpdateMonitor.isDeviceProvisioned() || (i != 1 && i != 7)) {
            z = false;
        }
        if (z) {
            i = 4;
        }
        switch (i) {
            case 0:
                return StatusMode.SimUnknown;
            case 1:
                return StatusMode.SimMissing;
            case 2:
                return StatusMode.SimLocked;
            case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                return StatusMode.SimPukLocked;
            case 4:
                return StatusMode.SimMissingLocked;
            case 5:
                return StatusMode.Normal;
            case 6:
                return StatusMode.SimNotReady;
            case 7:
                return StatusMode.SimPermDisabled;
            case QS.VERSION /* 8 */:
                return StatusMode.SimIoError;
            default:
                return StatusMode.SimUnknown;
        }
    }

    private static CharSequence concatenate(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        boolean z = !TextUtils.isEmpty(charSequence);
        boolean z2 = !TextUtils.isEmpty(charSequence2);
        if (!z || !z2) {
            return z ? charSequence : z2 ? charSequence2 : "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(charSequence);
        sb.append(charSequence3);
        sb.append(charSequence2);
        return sb.toString();
    }

    private static CharSequence joinNotEmpty(CharSequence charSequence, CharSequence[] charSequenceArr) {
        int length = charSequenceArr.length;
        if (length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (!TextUtils.isEmpty(charSequenceArr[i])) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(charSequence);
                }
                sb.append(charSequenceArr[i]);
            }
        }
        return sb.toString();
    }

    public static class Builder {
        private final Context mContext;
        private final String mSeparator;
        private boolean mShowAirplaneMode;
        private boolean mShowMissingSim;

        public Builder(Context context, Resources resources) {
            this.mContext = context;
            this.mSeparator = resources.getString(R.string.fp_power_button_bp_negative_button);
        }

        public Builder setShowAirplaneMode(boolean z) {
            this.mShowAirplaneMode = z;
            return this;
        }

        public Builder setShowMissingSim(boolean z) {
            this.mShowMissingSim = z;
            return this;
        }

        public CarrierTextController build() {
            return new CarrierTextController(this.mContext, this.mSeparator, this.mShowAirplaneMode, this.mShowMissingSim);
        }
    }

    public static final class CarrierTextCallbackInfo {
        public boolean airplaneMode;
        public final boolean anySimReady;
        public final CharSequence carrierText;
        public final CharSequence[] listOfCarriers;
        public final int[] subscriptionIds;

        public CarrierTextCallbackInfo(CharSequence charSequence, CharSequence[] charSequenceArr, boolean z, int[] iArr) {
            this(charSequence, charSequenceArr, z, iArr, false);
        }

        public CarrierTextCallbackInfo(CharSequence charSequence, CharSequence[] charSequenceArr, boolean z, int[] iArr, boolean z2) {
            this.carrierText = charSequence;
            this.listOfCarriers = charSequenceArr;
            this.anySimReady = z;
            this.subscriptionIds = iArr;
            this.airplaneMode = z2;
        }
    }
}
