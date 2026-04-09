package com.android.systemui.statusbar.policy;

import android.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import java.util.List;

/* loaded from: classes.dex */
public class EmergencyCryptkeeperText extends TextView {
    private final KeyguardUpdateMonitorCallback mCallback;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final BroadcastReceiver mReceiver;

    private boolean iccCardExist(int i) {
        return i == 2 || i == 3 || i == 4 || i == 5 || i == 6 || i == 7 || i == 8 || i == 9 || i == 10;
    }

    public EmergencyCryptkeeperText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.policy.EmergencyCryptkeeperText.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onPhoneStateChanged(int i) {
                EmergencyCryptkeeperText.this.update();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onRefreshCarrierInfo() {
                EmergencyCryptkeeperText.this.update();
            }
        };
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.EmergencyCryptkeeperText.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                    EmergencyCryptkeeperText.this.update();
                }
            }
        };
        setVisibility(8);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mCallback);
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        update();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateMonitor;
        if (keyguardUpdateMonitor != null) {
            keyguardUpdateMonitor.removeCallback(this.mCallback);
        }
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void update() {
        boolean zIsNetworkSupported = ConnectivityManager.from(((TextView) this).mContext).isNetworkSupported(0);
        boolean z = true;
        boolean z2 = Settings.Global.getInt(((TextView) this).mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        if (!zIsNetworkSupported || z2) {
            setText((CharSequence) null);
            setVisibility(8);
            return;
        }
        List<SubscriptionInfo> filteredSubscriptionInfo = this.mKeyguardUpdateMonitor.getFilteredSubscriptionInfo(false);
        int size = filteredSubscriptionInfo.size();
        CharSequence text = null;
        for (int i = 0; i < size; i++) {
            int simState = this.mKeyguardUpdateMonitor.getSimState(filteredSubscriptionInfo.get(i).getSubscriptionId());
            CharSequence carrierName = filteredSubscriptionInfo.get(i).getCarrierName();
            if (iccCardExist(simState) && !TextUtils.isEmpty(carrierName)) {
                z = false;
                text = carrierName;
            }
        }
        if (z) {
            if (size != 0) {
                text = filteredSubscriptionInfo.get(0).getCarrierName();
            } else {
                text = getContext().getText(R.string.console_running_notification_title);
                Intent intentRegisterReceiver = getContext().registerReceiver(null, new IntentFilter("android.telephony.action.SERVICE_PROVIDERS_UPDATED"));
                if (intentRegisterReceiver != null) {
                    text = intentRegisterReceiver.getStringExtra("android.telephony.extra.PLMN");
                }
            }
        }
        setText(text);
        setVisibility(TextUtils.isEmpty(text) ? 8 : 0);
    }
}
