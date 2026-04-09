package com.android.systemui.statusbar.phone;

import android.R;
import android.content.Context;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/* loaded from: classes.dex */
public class StatusBarSignalPolicy implements NetworkController.SignalCallback, SecurityController.SecurityControllerCallback, TunerService.Tunable {
    private boolean mActivityEnabled;
    private boolean mBlockAirplane;
    private boolean mBlockEthernet;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    private final Context mContext;
    private boolean mForceBlockWifi;
    private final StatusBarIconController mIconController;
    private final NetworkController mNetworkController;
    private final SecurityController mSecurityController;
    private final String mSlotAirplane;
    private final String mSlotEthernet;
    private final String mSlotMobile;
    private final String mSlotVpn;
    private final String mSlotWifi;
    private final Handler mHandler = Handler.getMain();
    private boolean mIsAirplaneMode = false;
    private boolean mWifiVisible = false;
    private ArrayList<MobileIconState> mMobileStates = new ArrayList<>();
    private WifiIconState mWifiIconState = new WifiIconState();

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
    }

    public StatusBarSignalPolicy(Context context, StatusBarIconController statusBarIconController) {
        this.mContext = context;
        this.mSlotAirplane = context.getString(R.string.permlab_foregroundServiceMediaPlayback);
        this.mSlotMobile = context.getString(R.string.permlab_killBackgroundProcesses);
        this.mSlotWifi = context.getString(R.string.permlab_preferredPaymentInfo);
        this.mSlotEthernet = context.getString(R.string.permlab_getPackageSize);
        this.mSlotVpn = context.getString(R.string.permlab_postNotification);
        this.mActivityEnabled = context.getResources().getBoolean(com.android.systemui.R.bool.config_showActivity);
        this.mIconController = statusBarIconController;
        NetworkController networkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mNetworkController = networkController;
        SecurityController securityController = (SecurityController) Dependency.get(SecurityController.class);
        this.mSecurityController = securityController;
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
        networkController.addCallback((NetworkController.SignalCallback) this);
        securityController.addCallback(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateVpn() {
        boolean zIsVpnEnabled = this.mSecurityController.isVpnEnabled();
        this.mIconController.setIcon(this.mSlotVpn, currentVpnIconId(this.mSecurityController.isVpnBranded()), this.mContext.getResources().getString(com.android.systemui.R.string.accessibility_vpn_on));
        this.mIconController.setIconVisibility(this.mSlotVpn, zIsVpnEnabled);
    }

    private int currentVpnIconId(boolean z) {
        return z ? com.android.systemui.R.drawable.stat_sys_branded_vpn : com.android.systemui.R.drawable.stat_sys_vpn_ic;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
    public void onStateChanged() {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarSignalPolicy$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateVpn();
            }
        });
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, str2);
            boolean zContains = iconBlacklist.contains(this.mSlotAirplane);
            boolean zContains2 = iconBlacklist.contains(this.mSlotMobile);
            boolean zContains3 = iconBlacklist.contains(this.mSlotWifi);
            boolean zContains4 = iconBlacklist.contains(this.mSlotEthernet);
            if (zContains == this.mBlockAirplane && zContains2 == this.mBlockMobile && zContains4 == this.mBlockEthernet && zContains3 == this.mBlockWifi) {
                return;
            }
            this.mBlockAirplane = zContains;
            this.mBlockMobile = zContains2;
            this.mBlockEthernet = zContains4;
            this.mBlockWifi = zContains3 || this.mForceBlockWifi;
            this.mNetworkController.removeCallback((NetworkController.SignalCallback) this);
            this.mNetworkController.addCallback((NetworkController.SignalCallback) this);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        boolean z5 = iconState.visible && !this.mBlockWifi;
        boolean z6 = z2 && this.mActivityEnabled && z5;
        boolean z7 = z3 && this.mActivityEnabled && z5;
        WifiIconState wifiIconStateCopy = this.mWifiIconState.copy();
        wifiIconStateCopy.visible = z5;
        wifiIconStateCopy.resId = iconState.icon;
        wifiIconStateCopy.activityIn = z6;
        wifiIconStateCopy.activityOut = z7;
        wifiIconStateCopy.slot = this.mSlotWifi;
        wifiIconStateCopy.airplaneSpacerVisible = this.mIsAirplaneMode;
        wifiIconStateCopy.contentDescription = iconState.contentDescription;
        MobileIconState firstMobileState = getFirstMobileState();
        wifiIconStateCopy.signalSpacerVisible = (firstMobileState == null || firstMobileState.typeId == 0) ? false : true;
        updateWifiIconWithState(wifiIconStateCopy);
        this.mWifiIconState = wifiIconStateCopy;
    }

    private void updateShowWifiSignalSpacer(WifiIconState wifiIconState) {
        MobileIconState firstMobileState = getFirstMobileState();
        wifiIconState.signalSpacerVisible = (firstMobileState == null || firstMobileState.typeId == 0) ? false : true;
    }

    private void updateWifiIconWithState(WifiIconState wifiIconState) {
        if (wifiIconState.visible && wifiIconState.resId > 0) {
            this.mIconController.setSignalIcon(this.mSlotWifi, wifiIconState);
            this.mIconController.setIconVisibility(this.mSlotWifi, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotWifi, false);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
        MobileIconState state = getState(i4);
        if (state == null) {
            return;
        }
        int i5 = state.typeId;
        boolean z5 = i != i5 && (i == 0 || i5 == 0);
        state.visible = iconState.visible && !this.mBlockMobile;
        state.strengthId = iconState.icon;
        state.typeId = i;
        state.contentDescription = iconState.contentDescription;
        state.typeContentDescription = charSequence;
        state.roaming = z4;
        state.activityIn = z && this.mActivityEnabled;
        state.activityOut = z2 && this.mActivityEnabled;
        state.volteId = i3;
        this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
        if (z5) {
            WifiIconState wifiIconStateCopy = this.mWifiIconState.copy();
            updateShowWifiSignalSpacer(wifiIconStateCopy);
            if (Objects.equals(wifiIconStateCopy, this.mWifiIconState)) {
                return;
            }
            updateWifiIconWithState(wifiIconStateCopy);
            this.mWifiIconState = wifiIconStateCopy;
        }
    }

    private MobileIconState getState(int i) {
        Iterator<MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            MobileIconState next = it.next();
            if (next.subId == i) {
                return next;
            }
        }
        Log.e("StatusBarSignalPolicy", "Unexpected subscription " + i);
        return null;
    }

    private MobileIconState getFirstMobileState() {
        if (this.mMobileStates.size() > 0) {
            return this.mMobileStates.get(0);
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        if (hasCorrectSubs(list)) {
            return;
        }
        this.mIconController.removeAllIconsForSlot(this.mSlotMobile);
        this.mMobileStates.clear();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            this.mMobileStates.add(new MobileIconState(list.get(i).getSubscriptionId()));
        }
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> list) {
        int size = list.size();
        if (size != this.mMobileStates.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.mMobileStates.get(i).subId != list.get(i).getSubscriptionId()) {
                return false;
            }
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        if (iconState.visible) {
            boolean z = this.mBlockEthernet;
        }
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (i > 0) {
            this.mIconController.setIcon(this.mSlotEthernet, i, str);
            this.mIconController.setIconVisibility(this.mSlotEthernet, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotEthernet, false);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        boolean z = iconState.visible && !this.mBlockAirplane;
        this.mIsAirplaneMode = z;
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (z && i > 0) {
            this.mIconController.setIcon(this.mSlotAirplane, i, str);
            this.mIconController.setIconVisibility(this.mSlotAirplane, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotAirplane, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static abstract class SignalIconState {
        public boolean activityIn;
        public boolean activityOut;
        public String contentDescription;
        public String slot;
        public boolean visible;

        private SignalIconState() {
        }

        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SignalIconState signalIconState = (SignalIconState) obj;
            return this.visible == signalIconState.visible && this.activityOut == signalIconState.activityOut && this.activityIn == signalIconState.activityIn && Objects.equals(this.contentDescription, signalIconState.contentDescription) && Objects.equals(this.slot, signalIconState.slot);
        }

        public int hashCode() {
            return Objects.hash(Boolean.valueOf(this.visible), Boolean.valueOf(this.activityOut), this.slot);
        }

        protected void copyTo(SignalIconState signalIconState) {
            signalIconState.visible = this.visible;
            signalIconState.activityIn = this.activityIn;
            signalIconState.activityOut = this.activityOut;
            signalIconState.slot = this.slot;
            signalIconState.contentDescription = this.contentDescription;
        }
    }

    public static class WifiIconState extends SignalIconState {
        public boolean airplaneSpacerVisible;
        public int resId;
        public boolean signalSpacerVisible;

        public WifiIconState() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public boolean equals(Object obj) {
            if (obj == null || WifiIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            WifiIconState wifiIconState = (WifiIconState) obj;
            return this.resId == wifiIconState.resId && this.airplaneSpacerVisible == wifiIconState.airplaneSpacerVisible && this.signalSpacerVisible == wifiIconState.signalSpacerVisible;
        }

        public void copyTo(WifiIconState wifiIconState) {
            super.copyTo((SignalIconState) wifiIconState);
            wifiIconState.resId = this.resId;
            wifiIconState.airplaneSpacerVisible = this.airplaneSpacerVisible;
            wifiIconState.signalSpacerVisible = this.signalSpacerVisible;
        }

        public WifiIconState copy() {
            WifiIconState wifiIconState = new WifiIconState();
            copyTo(wifiIconState);
            return wifiIconState;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.resId), Boolean.valueOf(this.airplaneSpacerVisible), Boolean.valueOf(this.signalSpacerVisible));
        }

        public String toString() {
            return "WifiIconState(resId=" + this.resId + ", visible=" + this.visible + ")";
        }
    }

    public static class MobileIconState extends SignalIconState {
        public boolean needsLeadingPadding;
        public boolean roaming;
        public int strengthId;
        public int subId;
        public CharSequence typeContentDescription;
        public int typeId;
        public int volteId;

        private MobileIconState(int i) {
            super();
            this.subId = i;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public boolean equals(Object obj) {
            if (obj == null || MobileIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            MobileIconState mobileIconState = (MobileIconState) obj;
            return this.subId == mobileIconState.subId && this.strengthId == mobileIconState.strengthId && this.typeId == mobileIconState.typeId && this.roaming == mobileIconState.roaming && this.needsLeadingPadding == mobileIconState.needsLeadingPadding && this.volteId == mobileIconState.volteId && Objects.equals(this.typeContentDescription, mobileIconState.typeContentDescription);
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.subId), Integer.valueOf(this.strengthId), Integer.valueOf(this.typeId), Boolean.valueOf(this.roaming), Boolean.valueOf(this.needsLeadingPadding), this.typeContentDescription);
        }

        public MobileIconState copy() {
            MobileIconState mobileIconState = new MobileIconState(this.subId);
            copyTo(mobileIconState);
            return mobileIconState;
        }

        public void copyTo(MobileIconState mobileIconState) {
            super.copyTo((SignalIconState) mobileIconState);
            mobileIconState.subId = this.subId;
            mobileIconState.strengthId = this.strengthId;
            mobileIconState.typeId = this.typeId;
            mobileIconState.roaming = this.roaming;
            mobileIconState.needsLeadingPadding = this.needsLeadingPadding;
            mobileIconState.typeContentDescription = this.typeContentDescription;
            mobileIconState.volteId = this.volteId;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static List<MobileIconState> copyStates(List<MobileIconState> list) {
            ArrayList arrayList = new ArrayList();
            for (MobileIconState mobileIconState : list) {
                MobileIconState mobileIconState2 = new MobileIconState(mobileIconState.subId);
                mobileIconState.copyTo(mobileIconState2);
                arrayList.add(mobileIconState2);
            }
            return arrayList;
        }

        public String toString() {
            return "MobileIconState(subId=" + this.subId + ", strengthId=" + this.strengthId + ", roaming=" + this.roaming + ", typeId=" + this.typeId + ", volteId=" + this.volteId + ", visible=" + this.visible + ")";
        }
    }
}
