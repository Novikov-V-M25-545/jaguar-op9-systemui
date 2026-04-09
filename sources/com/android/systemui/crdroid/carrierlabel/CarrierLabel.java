package com.android.systemui.crdroid.carrierlabel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.internal.util.crdroid.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.DarkIconDispatcher;

/* loaded from: classes.dex */
public class CarrierLabel extends TextView implements DarkIconDispatcher.DarkReceiver {
    private static boolean isCN;
    private boolean mAttached;
    private Context mContext;
    private final BroadcastReceiver mIntentReceiver;

    public CarrierLabel(Context context) {
        this(context, null);
    }

    public CarrierLabel(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CarrierLabel(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.crdroid.carrierlabel.CarrierLabel.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.telephony.action.SERVICE_PROVIDERS_UPDATED".equals(action) || "android.intent.action.CUSTOM_CARRIER_LABEL".equals(action)) {
                    CarrierLabel.this.updateNetworkName(intent.getBooleanExtra("android.telephony.extra.SHOW_SPN", true), intent.getStringExtra("android.telephony.extra.SPN"), intent.getBooleanExtra("android.telephony.extra.SHOW_PLMN", false), intent.getStringExtra("android.telephony.extra.PLMN"));
                    boolean unused = CarrierLabel.isCN = Utils.isChineseLanguage();
                }
            }
        };
        this.mContext = context;
        updateNetworkName(true, null, false, null);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this);
        if (this.mAttached) {
            return;
        }
        this.mAttached = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.SERVICE_PROVIDERS_UPDATED");
        intentFilter.addAction("android.intent.action.CUSTOM_CARRIER_LABEL");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, null, getHandler());
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this);
        if (this.mAttached) {
            this.mContext.unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
        }
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        setTextColor(DarkIconDispatcher.getTint(rect, this, i));
    }

    void updateNetworkName(boolean z, String str, boolean z2, String str2) {
        boolean z3 = z2 && !TextUtils.isEmpty(str2);
        if (!(z && !TextUtils.isEmpty(str))) {
            str = z3 ? str2 : "";
        }
        String stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "custom_carrier_label", -2);
        if (!TextUtils.isEmpty(stringForUser)) {
            setText(stringForUser);
            return;
        }
        if (TextUtils.isEmpty(str)) {
            str = getOperatorName();
        }
        setText(str);
    }

    private String getOperatorName() {
        String networkOperatorName;
        getContext().getString(R.string.quick_settings_wifi_no_network);
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService("phone");
        if (isCN) {
            String networkOperator = telephonyManager.getNetworkOperator();
            if (TextUtils.isEmpty(networkOperator)) {
                networkOperator = telephonyManager.getSimOperator();
            }
            networkOperatorName = new SpnOverride().getSpn(networkOperator);
        } else {
            networkOperatorName = telephonyManager.getNetworkOperatorName();
        }
        return TextUtils.isEmpty(networkOperatorName) ? telephonyManager.getSimOperatorName() : networkOperatorName;
    }
}
