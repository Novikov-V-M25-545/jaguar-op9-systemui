package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import androidx.mediarouter.media.MediaRoute2Provider$$ExternalSyntheticLambda0;
import com.android.ims.FeatureConnector;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.SignalDrawable;
import com.android.settingslib.net.SignalStrengthUtil;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SignalController;
import com.android.systemui.tuner.TunerService;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class MobileSignalController extends SignalController<MobileState, MobileIconGroup> implements TunerService.Tunable {
    private int mCallState;
    private ImsMmTelManager.CapabilityCallback mCapabilityCallback;
    private NetworkControllerImpl.Config mConfig;
    private boolean mDataDisabledIcon;
    private int mDataState;
    private MobileIconGroup mDefaultIcons;
    private final NetworkControllerImpl.SubscriptionDefaults mDefaults;
    private FeatureConnector<ImsManager> mFeatureConnector;
    private final Handler mHandler;
    private ImsManager mImsManager;
    private final ImsMmTelManager.RegistrationCallback mImsRegistrationCallback;

    @VisibleForTesting
    boolean mInflateSignalStrengths;
    private final String mNetworkNameDefault;
    private final String mNetworkNameSeparator;
    final Map<String, MobileIconGroup> mNetworkToIconLookup;
    private final ContentObserver mObserver;
    private boolean mOverride;
    private final TelephonyManager mPhone;

    @VisibleForTesting
    final PhoneStateListener mPhoneStateListener;
    private boolean mRoamingIconAllowed;
    private ServiceState mServiceState;
    private boolean mShow4gForLte;
    private SignalStrength mSignalStrength;
    final SubscriptionInfo mSubscriptionInfo;
    private TelephonyDisplayInfo mTelephonyDisplayInfo;
    private int mVoLTEicon;
    private int mVoNRicon;
    private int mVoWIFIicon;
    private final BroadcastReceiver mVolteSwitchObserver;

    public MobileSignalController(Context context, NetworkControllerImpl.Config config, boolean z, TelephonyManager telephonyManager, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, SubscriptionInfo subscriptionInfo, NetworkControllerImpl.SubscriptionDefaults subscriptionDefaults, Looper looper) {
        super("MobileSignalController(" + subscriptionInfo.getSubscriptionId() + ")", context, 0, callbackHandler, networkControllerImpl);
        this.mHandler = new Handler();
        this.mDataState = 0;
        this.mTelephonyDisplayInfo = new TelephonyDisplayInfo(0, 0);
        this.mInflateSignalStrengths = false;
        this.mCallState = 0;
        this.mVoLTEicon = 0;
        this.mVoWIFIicon = 0;
        this.mVoNRicon = 0;
        this.mOverride = true;
        this.mCapabilityCallback = new ImsMmTelManager.CapabilityCallback() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.3
            @Override // android.telephony.ims.ImsMmTelManager.CapabilityCallback
            public void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities mmTelCapabilities) {
                ((MobileState) MobileSignalController.this.mCurrentState).voiceCapable = mmTelCapabilities.isCapable(1);
                ((MobileState) MobileSignalController.this.mCurrentState).videoCapable = mmTelCapabilities.isCapable(2);
                Log.d(MobileSignalController.this.mTag, "onCapabilitiesStatusChanged isVoiceCapable=" + ((MobileState) MobileSignalController.this.mCurrentState).voiceCapable + " isVideoCapable=" + ((MobileState) MobileSignalController.this.mCurrentState).videoCapable);
                MobileSignalController.this.notifyListenersIfNecessary();
            }
        };
        this.mImsRegistrationCallback = new ImsMmTelManager.RegistrationCallback() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.4
            public void onRegistered(int i) {
                Log.d(MobileSignalController.this.mTag, "onRegistered imsTransportType=" + i);
                MobileSignalController mobileSignalController = MobileSignalController.this;
                ((MobileState) mobileSignalController.mCurrentState).imsRegistered = true;
                mobileSignalController.notifyListenersIfNecessary();
            }

            public void onRegistering(int i) {
                Log.d(MobileSignalController.this.mTag, "onRegistering imsTransportType=" + i);
                MobileSignalController mobileSignalController = MobileSignalController.this;
                ((MobileState) mobileSignalController.mCurrentState).imsRegistered = false;
                mobileSignalController.notifyListenersIfNecessary();
            }

            public void onUnregistered(ImsReasonInfo imsReasonInfo) {
                Log.d(MobileSignalController.this.mTag, "onDeregistered imsReasonInfo=" + imsReasonInfo);
                MobileSignalController mobileSignalController = MobileSignalController.this;
                ((MobileState) mobileSignalController.mCurrentState).imsRegistered = false;
                mobileSignalController.notifyListenersIfNecessary();
            }
        };
        this.mVolteSwitchObserver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                Log.d(MobileSignalController.this.mTag, "action=" + intent.getAction());
                if (MobileSignalController.this.mConfig.showVolteIcon) {
                    MobileSignalController.this.notifyListeners();
                }
            }
        };
        this.mNetworkToIconLookup = new HashMap();
        this.mConfig = config;
        this.mPhone = telephonyManager;
        this.mDefaults = subscriptionDefaults;
        this.mSubscriptionInfo = subscriptionInfo;
        this.mPhoneStateListener = new MobilePhoneStateListener(new MediaRoute2Provider$$ExternalSyntheticLambda0(new Handler(looper)));
        this.mNetworkNameSeparator = getTextIfExists(R.string.status_bar_network_name_separator).toString();
        String string = getTextIfExists(android.R.string.gpsVerifYes).toString();
        this.mNetworkNameDefault = string;
        mapIconSets();
        string = subscriptionInfo.getCarrierName() != null ? subscriptionInfo.getCarrierName().toString() : string;
        T t = this.mLastState;
        T t2 = this.mCurrentState;
        ((MobileState) t2).networkName = string;
        ((MobileState) t).networkName = string;
        ((MobileState) t2).networkNameData = string;
        ((MobileState) t).networkNameData = string;
        ((MobileState) t2).enabled = z;
        ((MobileState) t).enabled = z;
        MobileIconGroup mobileIconGroup = this.mDefaultIcons;
        ((MobileState) t2).iconGroup = mobileIconGroup;
        ((MobileState) t).iconGroup = mobileIconGroup;
        updateDataSim();
        final int simSlotIndex = subscriptionInfo.getSimSlotIndex();
        this.mFeatureConnector = new FeatureConnector<>(this.mContext, simSlotIndex, new FeatureConnector.Listener<ImsManager>() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.1
            /* renamed from: getFeatureManager, reason: merged with bridge method [inline-methods] */
            public ImsManager m363getFeatureManager() {
                return ImsManager.getInstance(MobileSignalController.this.mContext, simSlotIndex);
            }

            public void connectionReady(ImsManager imsManager) throws ImsException {
                Log.d(MobileSignalController.this.mTag, "ImsManager: connection ready.");
                MobileSignalController.this.mImsManager = imsManager;
                MobileSignalController.this.setListeners();
            }

            public void connectionUnavailable() {
                Log.d(MobileSignalController.this.mTag, "ImsManager: connection unavailable.");
                MobileSignalController.this.removeListeners();
            }
        }, "?");
        this.mObserver = new ContentObserver(new Handler(looper)) { // from class: com.android.systemui.statusbar.policy.MobileSignalController.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z2) {
                MobileSignalController.this.updateTelephony();
            }
        };
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:volte_icon_style");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:vonr_icon_style");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:vowifi_icon_style");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:volte_vowifi_override");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:roaming_indicator_icon");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:show_fourg_icon");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:data_disabled_icon");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "system:vowifi_icon_style":
                this.mVoWIFIicon = TunerService.parseInteger(str2, 0);
                notifyListeners();
                break;
            case "system:roaming_indicator_icon":
                this.mRoamingIconAllowed = TunerService.parseIntegerSwitch(str2, true);
                updateTelephony();
                break;
            case "system:vonr_icon_style":
                this.mVoNRicon = TunerService.parseInteger(str2, 0);
                notifyListeners();
                break;
            case "system:volte_vowifi_override":
                this.mOverride = TunerService.parseIntegerSwitch(str2, true);
                notifyListeners();
                break;
            case "system:volte_icon_style":
                this.mVoLTEicon = TunerService.parseInteger(str2, 0);
                notifyListeners();
                break;
            case "system:data_disabled_icon":
                this.mDataDisabledIcon = TunerService.parseIntegerSwitch(str2, true);
                updateTelephony();
                break;
            case "system:show_fourg_icon":
                this.mShow4gForLte = TunerService.parseIntegerSwitch(str2, false);
                mapIconSets();
                break;
        }
    }

    public void setConfiguration(NetworkControllerImpl.Config config) {
        this.mConfig = config;
        updateInflateSignalStrength();
        mapIconSets();
        updateTelephony();
    }

    public void setAirplaneMode(boolean z) {
        ((MobileState) this.mCurrentState).airplaneMode = z;
        notifyListenersIfNecessary();
    }

    public void setUserSetupComplete(boolean z) {
        ((MobileState) this.mCurrentState).userSetup = z;
        notifyListenersIfNecessary();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        boolean z = bitSet2.get(this.mTransportType);
        ((MobileState) this.mCurrentState).isDefault = bitSet.get(this.mTransportType);
        T t = this.mCurrentState;
        ((MobileState) t).inetCondition = (z || !((MobileState) t).isDefault) ? 1 : 0;
        notifyListenersIfNecessary();
    }

    public void setCarrierNetworkChangeMode(boolean z) {
        ((MobileState) this.mCurrentState).carrierNetworkChangeMode = z;
        updateTelephony();
    }

    public void registerListener() {
        this.mPhone.listen(this.mPhoneStateListener, 5308897);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data" + this.mSubscriptionInfo.getSubscriptionId()), true, this.mObserver);
        this.mContext.registerReceiver(this.mVolteSwitchObserver, new IntentFilter("org.codeaurora.intent.action.ACTION_ENHANCE_4G_SWITCH"));
        this.mFeatureConnector.connect();
    }

    public void unregisterListener() {
        this.mPhone.listen(this.mPhoneStateListener, 0);
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        this.mContext.unregisterReceiver(this.mVolteSwitchObserver);
        this.mFeatureConnector.disconnect();
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x00fd  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0124  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void mapIconSets() {
        /*
            Method dump skipped, instructions count: 388
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.mapIconSets():void");
    }

    private String getIconKey() {
        if (this.mTelephonyDisplayInfo.getOverrideNetworkType() == 0) {
            return toIconKey(this.mTelephonyDisplayInfo.getNetworkType());
        }
        return toDisplayIconKey(this.mTelephonyDisplayInfo.getOverrideNetworkType());
    }

    private String toIconKey(int i) {
        return Integer.toString(i);
    }

    private String toDisplayIconKey(int i) {
        if (i == 1) {
            return toIconKey(13) + "_CA";
        }
        if (i == 2) {
            return toIconKey(13) + "_CA_Plus";
        }
        if (i == 3) {
            return toIconKey(20);
        }
        if (i != 5) {
            return "unsupported";
        }
        return toIconKey(20) + "_Plus";
    }

    private void updateInflateSignalStrength() {
        this.mInflateSignalStrengths = SignalStrengthUtil.shouldInflateSignalStrength(this.mContext, this.mSubscriptionInfo.getSubscriptionId());
    }

    private int getNumLevels() {
        if (this.mInflateSignalStrengths) {
            return CellSignalStrength.getNumSignalStrengthLevels() + 1;
        }
        return CellSignalStrength.getNumSignalStrengthLevels();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public int getCurrentIconId() {
        T t = this.mCurrentState;
        if (((MobileState) t).iconGroup == TelephonyIcons.CARRIER_NETWORK_CHANGE) {
            return SignalDrawable.getCarrierChangeState(getNumLevels());
        }
        if (((MobileState) t).connected) {
            int i = ((MobileState) t).level;
            if (this.mInflateSignalStrengths) {
                i++;
            }
            return SignalDrawable.getState(i, getNumLevels(), (((MobileState) t).userSetup && (((MobileState) t).iconGroup == TelephonyIcons.DATA_DISABLED || (((MobileState) t).iconGroup == TelephonyIcons.NOT_DEFAULT_DATA && ((MobileState) t).defaultDataOff))) || (((MobileState) t).inetCondition == 0));
        }
        if (((MobileState) t).enabled) {
            return SignalDrawable.getEmptyState(getNumLevels());
        }
        return 0;
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public int getQsCurrentIconId() {
        return getCurrentIconId();
    }

    private int getVolteVowifiResId() {
        if (this.mOverride && this.mConfig.showVowifiIcon && this.mVoWIFIicon > 0 && isVowifiAvailable()) {
            if (!isCallIdle()) {
                return R.drawable.ic_vowifi_calling;
            }
            int i = this.mVoWIFIicon;
            if (i == 2) {
                return R.drawable.ic_vowifi_oneplus;
            }
            if (i == 3) {
                return R.drawable.ic_vowifi_moto;
            }
            if (i == 4) {
                return R.drawable.ic_vowifi_asus;
            }
            if (i == 5) {
                return R.drawable.ic_vowifi_emui;
            }
            if (i != 6) {
                return R.drawable.ic_vowifi;
            }
            return R.drawable.ic_vowifi_oneplus_compact;
        }
        if (this.mImsManager != null && this.mConfig.showVolteIcon && this.mVoLTEicon > 0 && isVolteAvailable() && this.mServiceState.getDataNetworkType() != 20) {
            switch (this.mVoLTEicon) {
                case 2:
                    return R.drawable.ic_volte2;
                case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                    return R.drawable.ic_volte6;
                case 4:
                    return R.drawable.ic_volte7;
                case 5:
                    return R.drawable.ic_volte8;
                case 6:
                    return R.drawable.ic_volte9;
                case 7:
                    return R.drawable.ic_volte_ios;
                case QS.VERSION /* 8 */:
                    return R.drawable.ic_volte_aris;
                case 9:
                    return R.drawable.ic_volte_volit;
                case 10:
                    return R.drawable.ic_volte_zirco;
                default:
                    return R.drawable.ic_volte1;
            }
        }
        if (this.mImsManager == null || !this.mConfig.showVolteIcon || this.mVoNRicon <= 0 || !isVolteAvailable() || this.mServiceState.getDataNetworkType() != 20) {
            return 0;
        }
        int i2 = this.mVoNRicon;
        if (i2 == 2) {
            return R.drawable.ic_vonr2;
        }
        if (i2 == 3) {
            return R.drawable.ic_vonr3;
        }
        if (i2 == 4) {
            return R.drawable.ic_vonr4;
        }
        if (i2 != 5) {
            return R.drawable.ic_volte_vcircle;
        }
        return R.drawable.ic_vonr5;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setListeners() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            Log.e(this.mTag, "setListeners mImsManager is null");
            return;
        }
        try {
            imsManager.addCapabilitiesCallback(this.mCapabilityCallback);
            this.mImsManager.addRegistrationCallback(this.mImsRegistrationCallback);
            Log.d(this.mTag, "addCapabilitiesCallback " + this.mCapabilityCallback + " into " + this.mImsManager);
            Log.d(this.mTag, "addRegistrationCallback " + this.mImsRegistrationCallback + " into " + this.mImsManager);
        } catch (ImsException unused) {
            Log.d(this.mTag, "unable to addCapabilitiesCallback callback.");
        }
        queryImsState();
    }

    private void queryImsState() {
        TelephonyManager telephonyManagerCreateForSubscriptionId = this.mPhone.createForSubscriptionId(this.mSubscriptionInfo.getSubscriptionId());
        boolean zIsVolteAvailable = telephonyManagerCreateForSubscriptionId.isVolteAvailable();
        boolean zIsVideoTelephonyAvailable = telephonyManagerCreateForSubscriptionId.isVideoTelephonyAvailable();
        ((MobileState) this.mCurrentState).imsRegistered = this.mPhone.isImsRegistered(this.mSubscriptionInfo.getSubscriptionId());
        if (SignalController.DEBUG) {
            Log.d(this.mTag, "tm=" + telephonyManagerCreateForSubscriptionId + " phone=" + this.mPhone + " isVoiceCapable=" + zIsVolteAvailable + " imsRegitered=" + ((MobileState) this.mCurrentState).imsRegistered + " isVideoCapable=" + zIsVideoTelephonyAvailable);
        }
        notifyListenersIfNecessary();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeListeners() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            Log.e(this.mTag, "removeListeners mImsManager is null");
            return;
        }
        try {
            imsManager.removeCapabilitiesCallback(this.mCapabilityCallback);
            this.mImsManager.removeRegistrationListener(this.mImsRegistrationCallback);
            Log.d(this.mTag, "removeCapabilitiesCallback " + this.mCapabilityCallback + " from " + this.mImsManager);
            Log.d(this.mTag, "removeRegistrationCallback " + this.mImsRegistrationCallback + " from " + this.mImsManager);
        } catch (ImsException unused) {
            Log.d(this.mTag, "unable to remove callback.");
        }
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void notifyListeners(final NetworkController.SignalCallback signalCallback) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.MobileSignalController$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$notifyListeners$0(signalCallback);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r11v13, types: [com.android.systemui.statusbar.policy.NetworkController$IconState] */
    public /* synthetic */ void lambda$notifyListeners$0(NetworkController.SignalCallback signalCallback) {
        int i;
        CharSequence charSequence;
        int i2;
        NetworkController.IconState iconState;
        MobileIconGroup icons = getIcons();
        String string = getTextIfExists(getContentDescription()).toString();
        CharSequence textIfExists = getTextIfExists(icons.mDataContentDescription);
        String string2 = Html.fromHtml(textIfExists.toString(), 0).toString();
        if (((MobileState) this.mCurrentState).inetCondition == 0) {
            string2 = this.mContext.getString(R.string.data_connection_no_internet);
        }
        String str = string2;
        Object obj = this.mCurrentState;
        boolean z = (((MobileState) obj).iconGroup == TelephonyIcons.DATA_DISABLED || ((MobileState) obj).iconGroup == TelephonyIcons.NOT_DEFAULT_DATA) && ((MobileState) obj).userSetup;
        boolean z2 = ((MobileState) obj).dataConnected || z;
        NetworkController.IconState iconState2 = new NetworkController.IconState(((MobileState) obj).enabled, getCurrentIconId(), string);
        Object obj2 = this.mCurrentState;
        if (((MobileState) obj2).dataSim) {
            i = (z2 || this.mConfig.alwaysShowDataRatIcon) ? icons.mQsDataType : 0;
            ?? iconState3 = new NetworkController.IconState(((MobileState) obj2).enabled && !((MobileState) obj2).isEmergency, getQsCurrentIconId(), string);
            Object obj3 = this.mCurrentState;
            charSequence = ((MobileState) obj3).isEmergency ? null : ((MobileState) obj3).networkName;
            charSequence = iconState3;
        } else {
            i = 0;
            charSequence = null;
        }
        Object obj4 = this.mCurrentState;
        boolean z3 = ((MobileState) obj4).dataConnected && !((MobileState) obj4).carrierNetworkChangeMode && ((MobileState) obj4).activityIn;
        boolean z4 = ((MobileState) obj4).dataConnected && !((MobileState) obj4).carrierNetworkChangeMode && ((MobileState) obj4).activityOut;
        int i3 = (((((MobileState) obj4).isDefault || z) && z2) || this.mConfig.alwaysShowDataRatIcon) ? icons.mDataType : 0;
        int volteVowifiResId = getVolteVowifiResId();
        MobileIconGroup vowifiIconGroup = getVowifiIconGroup();
        if (!this.mConfig.showVowifiIcon || vowifiIconGroup == null) {
            i2 = i3;
            iconState = iconState2;
        } else {
            int i4 = vowifiIconGroup.mDataType;
            iconState = new NetworkController.IconState(true, ((MobileState) this.mCurrentState).enabled ? iconState2.icon : 0, iconState2.contentDescription);
            i2 = i4;
        }
        signalCallback.setMobileDataIndicators(iconState, charSequence, i2, i, z3, z4, volteVowifiResId, str, textIfExists, charSequence, icons.mIsWide, this.mSubscriptionInfo.getSubscriptionId(), ((MobileState) this.mCurrentState).roaming);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public MobileState cleanState() {
        return new MobileState();
    }

    private boolean isCdma() {
        SignalStrength signalStrength = this.mSignalStrength;
        return (signalStrength == null || signalStrength.isGsm()) ? false : true;
    }

    public boolean isEmergencyOnly() {
        ServiceState serviceState = this.mServiceState;
        return serviceState != null && serviceState.isEmergencyOnly();
    }

    public boolean isInService() {
        return Utils.isInService(this.mServiceState);
    }

    private boolean isRoaming() {
        if (isCarrierNetworkChangeActive()) {
            return false;
        }
        if (isCdma() && this.mServiceState != null) {
            int eriIconMode = this.mPhone.getCdmaEriInformation().getEriIconMode();
            if (this.mPhone.getCdmaEriInformation().getEriIconIndex() != 1) {
                return eriIconMode == 0 || eriIconMode == 1;
            }
            return false;
        }
        ServiceState serviceState = this.mServiceState;
        return serviceState != null && serviceState.getRoaming();
    }

    private boolean isCarrierNetworkChangeActive() {
        return ((MobileState) this.mCurrentState).carrierNetworkChangeMode;
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.telephony.action.SERVICE_PROVIDERS_UPDATED")) {
            updateNetworkName(intent.getBooleanExtra("android.telephony.extra.SHOW_SPN", false), intent.getStringExtra("android.telephony.extra.SPN"), intent.getStringExtra("android.telephony.extra.DATA_SPN"), intent.getBooleanExtra("android.telephony.extra.SHOW_PLMN", false), intent.getStringExtra("android.telephony.extra.PLMN"));
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            updateDataSim();
            notifyListenersIfNecessary();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDataSim() {
        int activeDataSubId = this.mDefaults.getActiveDataSubId();
        if (SubscriptionManager.isValidSubscriptionId(activeDataSubId)) {
            ((MobileState) this.mCurrentState).dataSim = activeDataSubId == this.mSubscriptionInfo.getSubscriptionId();
        } else {
            ((MobileState) this.mCurrentState).dataSim = true;
        }
    }

    void updateNetworkName(boolean z, String str, String str2, boolean z2, String str3) {
        if (SignalController.CHATTY) {
            Log.d("CarrierLabel", "updateNetworkName showSpn=" + z + " spn=" + str + " dataSpn=" + str2 + " showPlmn=" + z2 + " plmn=" + str3);
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        if (z2 && str3 != null) {
            sb.append(str3);
            sb2.append(str3);
        }
        if (z && str != null && !sb.toString().equalsIgnoreCase(str)) {
            if (sb.length() != 0) {
                sb.append(this.mNetworkNameSeparator);
            }
            sb.append(str);
        }
        if (sb.length() != 0) {
            ((MobileState) this.mCurrentState).networkName = sb.toString();
        } else {
            ((MobileState) this.mCurrentState).networkName = this.mNetworkNameDefault;
        }
        if (z && str2 != null && !sb2.toString().equalsIgnoreCase(str2)) {
            if (sb2.length() != 0) {
                sb2.append(this.mNetworkNameSeparator);
            }
            sb2.append(str2);
        }
        if (sb2.length() != 0) {
            ((MobileState) this.mCurrentState).networkNameData = sb2.toString();
        } else {
            ((MobileState) this.mCurrentState).networkNameData = this.mNetworkNameDefault;
        }
    }

    private final int getCdmaLevel() {
        List cellSignalStrengths = this.mSignalStrength.getCellSignalStrengths(CellSignalStrengthCdma.class);
        if (cellSignalStrengths.isEmpty()) {
            return 0;
        }
        return ((CellSignalStrengthCdma) cellSignalStrengths.get(0)).getLevel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateTelephony() {
        ServiceState serviceState;
        ServiceState serviceState2;
        if (SignalController.DEBUG) {
            Log.d(this.mTag, "updateTelephonySignalStrength: hasService=" + Utils.isInService(this.mServiceState) + " ss=" + this.mSignalStrength + " displayInfo=" + this.mTelephonyDisplayInfo);
        }
        checkDefaultData();
        ((MobileState) this.mCurrentState).connected = Utils.isInService(this.mServiceState) && this.mSignalStrength != null;
        if (((MobileState) this.mCurrentState).connected) {
            if (!this.mSignalStrength.isGsm() && this.mConfig.alwaysShowCdmaRssi) {
                ((MobileState) this.mCurrentState).level = getCdmaLevel();
            } else {
                ((MobileState) this.mCurrentState).level = this.mSignalStrength.getLevel();
            }
        }
        String iconKey = getIconKey();
        if (this.mNetworkToIconLookup.get(iconKey) != null) {
            ((MobileState) this.mCurrentState).iconGroup = this.mNetworkToIconLookup.get(iconKey);
        } else {
            ((MobileState) this.mCurrentState).iconGroup = this.mDefaultIcons;
        }
        T t = this.mCurrentState;
        ((MobileState) t).dataConnected = ((MobileState) t).connected && this.mDataState == 2;
        ((MobileState) t).roaming = isRoaming() && this.mRoamingIconAllowed;
        if (isCarrierNetworkChangeActive()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        } else if (isDataDisabled() && this.mDataDisabledIcon) {
            if (this.mSubscriptionInfo.getSubscriptionId() != this.mDefaults.getDefaultDataSubId()) {
                ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.NOT_DEFAULT_DATA;
            } else {
                ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.DATA_DISABLED;
            }
        }
        boolean zIsEmergencyOnly = isEmergencyOnly();
        T t2 = this.mCurrentState;
        if (zIsEmergencyOnly != ((MobileState) t2).isEmergency) {
            ((MobileState) t2).isEmergency = isEmergencyOnly();
            this.mNetworkController.recalculateEmergency();
        }
        if (((MobileState) this.mCurrentState).networkName.equals(this.mNetworkNameDefault) && (serviceState2 = this.mServiceState) != null && !TextUtils.isEmpty(serviceState2.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkName = this.mServiceState.getOperatorAlphaShort();
        }
        if (((MobileState) this.mCurrentState).networkNameData.equals(this.mNetworkNameDefault) && (serviceState = this.mServiceState) != null && ((MobileState) this.mCurrentState).dataSim && !TextUtils.isEmpty(serviceState.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkNameData = this.mServiceState.getOperatorAlphaShort();
        }
        notifyListenersIfNecessary();
    }

    private void checkDefaultData() {
        T t = this.mCurrentState;
        if (((MobileState) t).iconGroup != TelephonyIcons.NOT_DEFAULT_DATA) {
            ((MobileState) t).defaultDataOff = false;
        } else {
            ((MobileState) t).defaultDataOff = this.mNetworkController.isDataControllerDisabled();
        }
    }

    void onMobileDataChanged() {
        checkDefaultData();
        notifyListenersIfNecessary();
    }

    boolean isDataDisabled() {
        return !this.mPhone.isDataConnectionAllowed();
    }

    @VisibleForTesting
    void setActivity(int i) {
        T t = this.mCurrentState;
        ((MobileState) t).activityIn = i == 3 || i == 1;
        ((MobileState) t).activityOut = i == 3 || i == 2;
        notifyListenersIfNecessary();
    }

    private boolean isCallIdle() {
        return this.mCallState == 0;
    }

    private boolean isVolteAvailable() {
        T t = this.mCurrentState;
        return (((MobileState) t).voiceCapable || ((MobileState) t).videoCapable) && ((MobileState) t).imsRegistered;
    }

    public boolean isVowifiAvailable() {
        return ((MobileState) this.mCurrentState).imsRegistered && this.mServiceState.getDataNetworkType() == 18;
    }

    private MobileIconGroup getVowifiIconGroup() {
        if (this.mVoWIFIicon != 0 && !this.mOverride) {
            if (isVowifiAvailable() && !isCallIdle()) {
                return TelephonyIcons.VOWIFI_CALLING;
            }
            if (isVowifiAvailable()) {
                int i = this.mVoWIFIicon;
                if (i == 2) {
                    return TelephonyIcons.VOWIFI_ONEPLUS;
                }
                if (i == 3) {
                    return TelephonyIcons.VOWIFI_MOTO;
                }
                if (i == 4) {
                    return TelephonyIcons.VOWIFI_ASUS;
                }
                if (i == 5) {
                    return TelephonyIcons.VOWIFI_EMUI;
                }
                if (i != 6) {
                    return TelephonyIcons.VOWIFI;
                }
                return TelephonyIcons.VOWIFI_ONEPLUS_COMPACT;
            }
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void dump(PrintWriter printWriter) {
        super.dump(printWriter);
        printWriter.println("  mSubscription=" + this.mSubscriptionInfo + ",");
        printWriter.println("  mServiceState=" + this.mServiceState + ",");
        printWriter.println("  mSignalStrength=" + this.mSignalStrength + ",");
        printWriter.println("  mTelephonyDisplayInfo=" + this.mTelephonyDisplayInfo + ",");
        printWriter.println("  mDataState=" + this.mDataState + ",");
        printWriter.println("  mInflateSignalStrengths=" + this.mInflateSignalStrengths + ",");
        printWriter.println("  isDataDisabled=" + isDataDisabled() + ",");
    }

    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(Executor executor) {
            super(executor);
        }

        @Override // android.telephony.PhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            String str;
            if (SignalController.DEBUG) {
                String str2 = MobileSignalController.this.mTag;
                StringBuilder sb = new StringBuilder();
                sb.append("onSignalStrengthsChanged signalStrength=");
                sb.append(signalStrength);
                if (signalStrength == null) {
                    str = "";
                } else {
                    str = " level=" + signalStrength.getLevel();
                }
                sb.append(str);
                Log.d(str2, sb.toString());
            }
            MobileSignalController.this.mSignalStrength = signalStrength;
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            if (SignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onServiceStateChanged voiceState=" + serviceState.getState() + " dataState=" + serviceState.getDataRegistrationState());
            }
            MobileSignalController.this.mServiceState = serviceState;
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataConnectionStateChanged(int i, int i2) {
            if (SignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onDataConnectionStateChanged: state=" + i + " type=" + i2);
            }
            MobileSignalController.this.mDataState = i;
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataActivity(int i) {
            if (SignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onDataActivity: direction=" + i);
            }
            MobileSignalController.this.setActivity(i);
        }

        public void onCarrierNetworkChange(boolean z) {
            if (SignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onCarrierNetworkChange: active=" + z);
            }
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).carrierNetworkChangeMode = z;
            mobileSignalController.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onActiveDataSubscriptionIdChanged(int i) {
            if (SignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onActiveDataSubscriptionIdChanged: subId=" + i);
            }
            MobileSignalController.this.updateDataSim();
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo) {
            if (SignalController.DEBUG) {
                Log.d(MobileSignalController.this.mTag, "onDisplayInfoChanged: telephonyDisplayInfo=" + telephonyDisplayInfo);
            }
            MobileSignalController.this.mTelephonyDisplayInfo = telephonyDisplayInfo;
            MobileSignalController.this.updateTelephony();
        }
    }

    static class MobileIconGroup extends SignalController.IconGroup {
        final int mDataContentDescription;
        final int mDataType;
        final boolean mIsWide;
        final int mQsDataType;

        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z) {
            super(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5);
            this.mDataContentDescription = i6;
            this.mDataType = i7;
            this.mIsWide = z;
            this.mQsDataType = i7;
        }
    }

    static class MobileState extends SignalController.State {
        boolean airplaneMode;
        boolean carrierNetworkChangeMode;
        boolean dataConnected;
        boolean dataSim;
        boolean defaultDataOff;
        boolean imsRegistered;
        boolean isDefault;
        boolean isEmergency;
        String networkName;
        String networkNameData;
        boolean roaming;
        boolean userSetup;
        boolean videoCapable;
        boolean voiceCapable;

        MobileState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            MobileState mobileState = (MobileState) state;
            this.dataSim = mobileState.dataSim;
            this.networkName = mobileState.networkName;
            this.networkNameData = mobileState.networkNameData;
            this.dataConnected = mobileState.dataConnected;
            this.isDefault = mobileState.isDefault;
            this.isEmergency = mobileState.isEmergency;
            this.airplaneMode = mobileState.airplaneMode;
            this.carrierNetworkChangeMode = mobileState.carrierNetworkChangeMode;
            this.userSetup = mobileState.userSetup;
            this.roaming = mobileState.roaming;
            this.defaultDataOff = mobileState.defaultDataOff;
            this.imsRegistered = mobileState.imsRegistered;
            this.voiceCapable = mobileState.voiceCapable;
            this.videoCapable = mobileState.videoCapable;
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        protected void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(',');
            sb.append("dataSim=");
            sb.append(this.dataSim);
            sb.append(',');
            sb.append("networkName=");
            sb.append(this.networkName);
            sb.append(',');
            sb.append("networkNameData=");
            sb.append(this.networkNameData);
            sb.append(',');
            sb.append("dataConnected=");
            sb.append(this.dataConnected);
            sb.append(',');
            sb.append("roaming=");
            sb.append(this.roaming);
            sb.append(',');
            sb.append("isDefault=");
            sb.append(this.isDefault);
            sb.append(',');
            sb.append("isEmergency=");
            sb.append(this.isEmergency);
            sb.append(',');
            sb.append("airplaneMode=");
            sb.append(this.airplaneMode);
            sb.append(',');
            sb.append("carrierNetworkChangeMode=");
            sb.append(this.carrierNetworkChangeMode);
            sb.append(',');
            sb.append("userSetup=");
            sb.append(this.userSetup);
            sb.append(',');
            sb.append("defaultDataOff=");
            sb.append(this.defaultDataOff);
            sb.append("imsRegistered=");
            sb.append(this.imsRegistered);
            sb.append(',');
            sb.append("voiceCapable=");
            sb.append(this.voiceCapable);
            sb.append(',');
            sb.append("videoCapable=");
            sb.append(this.videoCapable);
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                MobileState mobileState = (MobileState) obj;
                if (Objects.equals(mobileState.networkName, this.networkName) && Objects.equals(mobileState.networkNameData, this.networkNameData) && mobileState.dataSim == this.dataSim && mobileState.dataConnected == this.dataConnected && mobileState.isEmergency == this.isEmergency && mobileState.airplaneMode == this.airplaneMode && mobileState.carrierNetworkChangeMode == this.carrierNetworkChangeMode && mobileState.userSetup == this.userSetup && mobileState.isDefault == this.isDefault && mobileState.roaming == this.roaming && mobileState.defaultDataOff == this.defaultDataOff && mobileState.imsRegistered == this.imsRegistered && mobileState.voiceCapable == this.voiceCapable && mobileState.videoCapable == this.videoCapable) {
                    return true;
                }
            }
            return false;
        }
    }
}
