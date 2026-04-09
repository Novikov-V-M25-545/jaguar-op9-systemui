package com.android.settingslib.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import androidx.preference.Preference;
import com.android.settingslib.R$string;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
public abstract class AbstractBatteryStatusPreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnStart, OnStop {
    static final String KEY_BATTERY_STATUS = "battery_status";
    private Preference mBatteryStatus;
    private Context mContext;
    private Handler mHandler;

    @Override // com.android.settingslib.core.lifecycle.events.OnStart
    public void onStart() {
        getHandler().sendEmptyMessage(700);
    }

    @Override // com.android.settingslib.core.lifecycle.events.OnStop
    public void onStop() {
        getHandler().removeMessages(700);
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new MyHandler(this);
        }
        return this.mHandler;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBattery() {
        Intent intentRegisterReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (this.mBatteryStatus != null) {
            String string = this.mContext.getString(R$string.battery_info_status_unknown);
            String str = Integer.toString(Math.round((intentRegisterReceiver.getIntExtra("level", 100) * 100.0f) / intentRegisterReceiver.getIntExtra("scale", 100))) + "%";
            int intExtra = intentRegisterReceiver.getIntExtra("status", -1);
            if (intExtra == 2) {
                string = this.mContext.getString(R$string.battery_info_status_charging);
            } else if (intExtra == 3) {
                string = this.mContext.getString(R$string.battery_info_status_discharging);
            } else if (intExtra == 4) {
                string = this.mContext.getString(R$string.battery_info_status_not_charging);
            } else if (intExtra == 5) {
                string = this.mContext.getString(R$string.battery_info_status_full);
            }
            this.mBatteryStatus.setSummary(str + " - " + string);
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<AbstractBatteryStatusPreferenceController> mStatus;

        public MyHandler(AbstractBatteryStatusPreferenceController abstractBatteryStatusPreferenceController) {
            this.mStatus = new WeakReference<>(abstractBatteryStatusPreferenceController);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            AbstractBatteryStatusPreferenceController abstractBatteryStatusPreferenceController = this.mStatus.get();
            if (abstractBatteryStatusPreferenceController == null) {
                return;
            }
            if (message.what == 700) {
                abstractBatteryStatusPreferenceController.updateBattery();
                sendEmptyMessageDelayed(700, 1000L);
            } else {
                throw new IllegalStateException("Unknown message " + message.what);
            }
        }
    }
}
