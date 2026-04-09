package com.android.systemui.statusbar.policy;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController;
import java.util.Objects;

/* loaded from: classes.dex */
public class WifiSignalController extends SignalController<WifiState, SignalController.IconGroup> {
    private final boolean mHasMobileDataFeature;
    private final WifiStatusTracker mWifiTracker;

    public WifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, WifiManager wifiManager, ConnectivityManager connectivityManager, NetworkScoreManager networkScoreManager) {
        super("WifiSignalController", context, 1, callbackHandler, networkControllerImpl);
        WifiStatusTracker wifiStatusTracker = new WifiStatusTracker(this.mContext, wifiManager, networkScoreManager, connectivityManager, new Runnable() { // from class: com.android.systemui.statusbar.policy.WifiSignalController$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.handleStatusUpdated();
            }
        });
        this.mWifiTracker = wifiStatusTracker;
        wifiStatusTracker.setListening(true);
        this.mHasMobileDataFeature = z;
        if (wifiManager != null) {
            wifiManager.registerTrafficStateCallback(context.getMainExecutor(), new WifiTrafficStateCallback());
        }
        WifiState wifiState = (WifiState) this.mCurrentState;
        WifiState wifiState2 = (WifiState) this.mLastState;
        SignalController.IconGroup iconGroup = new SignalController.IconGroup("Wi-Fi Icons", WifiIcons.WIFI_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, R.drawable.ic_perm_group_bluetooth, R.drawable.ic_perm_group_bluetooth, R.drawable.ic_perm_group_bluetooth, R.drawable.ic_perm_group_bluetooth, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        wifiState2.iconGroup = iconGroup;
        wifiState.iconGroup = iconGroup;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public WifiState cleanState() {
        return new WifiState();
    }

    void refreshLocale() {
        this.mWifiTracker.refreshLocale();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        boolean z = this.mContext.getResources().getBoolean(com.android.systemui.R.bool.config_showWifiIndicatorWhenEnabled);
        T t = this.mCurrentState;
        boolean z2 = ((WifiState) t).enabled && ((((WifiState) t).connected && ((WifiState) t).inetCondition == 1) || !this.mHasMobileDataFeature || ((WifiState) t).isDefault || z);
        String str = ((WifiState) t).connected ? ((WifiState) t).ssid : null;
        boolean z3 = z2 && ((WifiState) t).ssid != null;
        String string = getTextIfExists(getContentDescription()).toString();
        if (((WifiState) this.mCurrentState).inetCondition == 0) {
            string = string + "," + this.mContext.getString(com.android.systemui.R.string.data_connection_no_internet);
        }
        NetworkController.IconState iconState = new NetworkController.IconState(z2, getCurrentIconId(), string);
        NetworkController.IconState iconState2 = new NetworkController.IconState(((WifiState) this.mCurrentState).connected, this.mWifiTracker.isCaptivePortal ? com.android.systemui.R.drawable.ic_qs_wifi_disconnected : getQsCurrentIconId(), string);
        T t2 = this.mCurrentState;
        signalCallback.setWifiIndicators(((WifiState) t2).enabled, iconState, iconState2, z3 && ((WifiState) t2).activityIn, z3 && ((WifiState) t2).activityOut, str, ((WifiState) t2).isTransient, ((WifiState) t2).statusLabel);
    }

    public void fetchInitialState() {
        this.mWifiTracker.fetchInitialState();
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).enabled = wifiStatusTracker.enabled;
        ((WifiState) t).isDefault = wifiStatusTracker.isDefaultNetwork;
        ((WifiState) t).connected = wifiStatusTracker.connected;
        ((WifiState) t).ssid = wifiStatusTracker.ssid;
        ((WifiState) t).rssi = wifiStatusTracker.rssi;
        ((WifiState) t).level = wifiStatusTracker.level;
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        notifyListenersIfNecessary();
    }

    public void handleBroadcast(Intent intent) {
        this.mWifiTracker.handleBroadcast(intent);
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).enabled = wifiStatusTracker.enabled;
        ((WifiState) t).isDefault = wifiStatusTracker.isDefaultNetwork;
        ((WifiState) t).connected = wifiStatusTracker.connected;
        ((WifiState) t).ssid = wifiStatusTracker.ssid;
        ((WifiState) t).rssi = wifiStatusTracker.rssi;
        ((WifiState) t).level = wifiStatusTracker.level;
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        notifyListenersIfNecessary();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleStatusUpdated() {
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        ((WifiState) t).isDefault = wifiStatusTracker.isDefaultNetwork;
        notifyListenersIfNecessary();
    }

    @VisibleForTesting
    void setActivity(int i) {
        T t = this.mCurrentState;
        ((WifiState) t).activityIn = i == 3 || i == 1;
        ((WifiState) t).activityOut = i == 3 || i == 2;
        notifyListenersIfNecessary();
    }

    private class WifiTrafficStateCallback implements WifiManager.TrafficStateCallback {
        private WifiTrafficStateCallback() {
        }

        public void onStateChanged(int i) {
            WifiSignalController.this.setActivity(i);
        }
    }

    static class WifiState extends SignalController.State {
        boolean isDefault;
        boolean isTransient;
        String ssid;
        String statusLabel;

        WifiState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            WifiState wifiState = (WifiState) state;
            this.ssid = wifiState.ssid;
            this.isTransient = wifiState.isTransient;
            this.isDefault = wifiState.isDefault;
            this.statusLabel = wifiState.statusLabel;
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        protected void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(",ssid=");
            sb.append(this.ssid);
            sb.append(",isTransient=");
            sb.append(this.isTransient);
            sb.append(",isDefault=");
            sb.append(this.isDefault);
            sb.append(",statusLabel=");
            sb.append(this.statusLabel);
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            WifiState wifiState = (WifiState) obj;
            return Objects.equals(wifiState.ssid, this.ssid) && wifiState.isTransient == this.isTransient && wifiState.isDefault == this.isDefault && TextUtils.equals(wifiState.statusLabel, this.statusLabel);
        }
    }
}
