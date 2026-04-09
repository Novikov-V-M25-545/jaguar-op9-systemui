package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.List;
import java.util.concurrent.Executors;

/* loaded from: classes.dex */
public class DataSwitchTile extends QSTileImpl<QSTile.BooleanState> {
    private boolean mCanSwitch;
    private final MyCallStateListener mPhoneStateListener;
    private boolean mRegistered;
    private int mSimCount;
    BroadcastReceiver mSimReceiver;
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    class MyCallStateListener extends PhoneStateListener {
        MyCallStateListener() {
        }

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int i, String str) {
            DataSwitchTile dataSwitchTile = DataSwitchTile.this;
            dataSwitchTile.mCanSwitch = dataSwitchTile.mTelephonyManager.getCallState() == 0;
            DataSwitchTile.this.refreshState();
        }
    }

    public DataSwitchTile(QSHost qSHost) {
        super(qSHost);
        this.mCanSwitch = true;
        this.mRegistered = false;
        this.mSimCount = 0;
        this.mSimReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.DataSwitchTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                Log.d(((QSTileImpl) DataSwitchTile.this).TAG, "mSimReceiver:onReceive");
                DataSwitchTile.this.refreshState();
            }
        };
        this.mSubscriptionManager = SubscriptionManager.from(qSHost.getContext());
        this.mTelephonyManager = TelephonyManager.from(qSHost.getContext());
        this.mPhoneStateListener = new MyCallStateListener();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        Log.d(this.TAG, "phoneCount: " + phoneCount);
        return phoneCount >= 2;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            if (!this.mRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
                this.mContext.registerReceiver(this.mSimReceiver, intentFilter);
                this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
                this.mRegistered = true;
            }
            refreshState();
            return;
        }
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mSimReceiver);
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mRegistered = false;
        }
    }

    private void updateSimCount() {
        String str = SystemProperties.get("gsm.sim.state");
        Log.d(this.TAG, "DataSwitchTile:updateSimCount:simState=" + str);
        this.mSimCount = 0;
        try {
            for (String str2 : TextUtils.split(str, ",")) {
                if (!str2.isEmpty() && !str2.equalsIgnoreCase("ABSENT") && !str2.equalsIgnoreCase("NOT_READY")) {
                    this.mSimCount++;
                }
            }
        } catch (Exception unused) {
            Log.e(this.TAG, "Error to parse sim state");
        }
        Log.d(this.TAG, "DataSwitchTile:updateSimCount:mSimCount=" + this.mSimCount);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (!this.mCanSwitch) {
            Log.d(this.TAG, "Call state=" + this.mTelephonyManager.getCallState());
            return;
        }
        int i = this.mSimCount;
        if (i == 0) {
            Log.d(this.TAG, "handleClick:no sim card");
        } else if (i == 1) {
            Log.d(this.TAG, "handleClick:only one sim card");
        } else {
            Executors.newSingleThreadExecutor().execute(new Runnable() { // from class: com.android.systemui.qs.tiles.DataSwitchTile$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleClick$0();
                }
            });
            this.mHost.collapsePanels();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleClick$0() {
        toggleMobileDataEnabled();
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.NETWORK_OPERATOR_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.qs_data_switch_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean zBooleanValue;
        int i;
        int i2;
        int i3;
        if (obj == null) {
            int defaultDataPhoneId = this.mSubscriptionManager.getDefaultDataPhoneId();
            Log.d(this.TAG, "default data phone id=" + defaultDataPhoneId);
            zBooleanValue = defaultDataPhoneId == 0;
        } else {
            zBooleanValue = ((Boolean) obj).booleanValue();
        }
        updateSimCount();
        int i4 = this.mSimCount;
        if (i4 == 0) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_data_switch_0);
            booleanState.value = false;
            booleanState.secondaryLabel = this.mContext.getString(R.string.tile_unavailable);
        } else if (i4 == 1) {
            if (zBooleanValue) {
                i2 = R.drawable.ic_qs_data_switch_2;
            } else {
                i2 = R.drawable.ic_qs_data_switch_1;
            }
            booleanState.icon = QSTileImpl.ResourceIcon.get(i2);
            booleanState.value = false;
            booleanState.secondaryLabel = this.mContext.getString(R.string.tile_unavailable);
        } else if (i4 == 2) {
            if (zBooleanValue) {
                i3 = R.drawable.ic_qs_data_switch_2;
            } else {
                i3 = R.drawable.ic_qs_data_switch_1;
            }
            booleanState.icon = QSTileImpl.ResourceIcon.get(i3);
            booleanState.value = true;
            booleanState.secondaryLabel = getInactiveSlotName();
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_data_switch_1);
            booleanState.value = false;
            booleanState.secondaryLabel = this.mContext.getString(R.string.tile_unavailable);
        }
        if (this.mSimCount < 2) {
            booleanState.state = 0;
        } else if (!this.mCanSwitch) {
            booleanState.state = 0;
            Log.d(this.TAG, "call state isn't idle, set to unavailable.");
        } else {
            booleanState.state = booleanState.value ? 2 : 1;
        }
        Context context = this.mContext;
        if (zBooleanValue) {
            i = R.string.qs_data_switch_changed_1;
        } else {
            i = R.string.qs_data_switch_changed_2;
        }
        booleanState.contentDescription = context.getString(i);
        booleanState.label = this.mContext.getString(R.string.qs_data_switch_label);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        int i;
        Context context = this.mContext;
        if (((QSTile.BooleanState) this.mState).value) {
            i = R.string.qs_data_switch_changed_1;
        } else {
            i = R.string.qs_data_switch_changed_2;
        }
        return context.getString(i);
    }

    private void toggleMobileDataEnabled() {
        List<SubscriptionInfo> activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList(true);
        if (activeSubscriptionInfoList != null) {
            boolean z = false;
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                TelephonyManager telephonyManagerCreateForSubscriptionId = this.mTelephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId());
                boolean dataEnabled = telephonyManagerCreateForSubscriptionId.getDataEnabled();
                if (!subscriptionInfo.isOpportunistic() || !dataEnabled) {
                    telephonyManagerCreateForSubscriptionId.setDataEnabled((dataEnabled || z) ? false : true);
                    if (!z) {
                        z = !dataEnabled;
                    }
                }
                String str = this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Changed subID ");
                sb.append(subscriptionInfo.getSubscriptionId());
                sb.append(" to ");
                sb.append(!dataEnabled);
                Log.d(str, sb.toString());
            }
        }
    }

    private String getInactiveSlotName() {
        String string = this.mContext.getString(R.string.tile_unavailable);
        List<SubscriptionInfo> activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList(true);
        if (activeSubscriptionInfoList != null) {
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                if (!this.mTelephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId()).getDataEnabled()) {
                    return subscriptionInfo.getDisplayName().toString();
                }
            }
        }
        return string;
    }
}
