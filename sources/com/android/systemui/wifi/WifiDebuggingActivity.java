package com.android.systemui.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.debug.IAdbManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.R;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class WifiDebuggingActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private CheckBox mAlwaysAllow;
    private String mBssid;
    private boolean mClicked = false;
    private WifiChangeReceiver mWifiChangeReceiver;
    private WifiManager mWifiManager;

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) throws Resources.NotFoundException {
        Window window = getWindow();
        window.addSystemFlags(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES);
        window.setType(2008);
        super.onCreate(bundle);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mWifiChangeReceiver = new WifiChangeReceiver(this);
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("ssid");
        String stringExtra2 = intent.getStringExtra("bssid");
        this.mBssid = stringExtra2;
        if (stringExtra == null || stringExtra2 == null) {
            finish();
            return;
        }
        AlertController.AlertParams alertParams = ((AlertActivity) this).mAlertParams;
        alertParams.mTitle = getString(R.string.wifi_debugging_title);
        alertParams.mMessage = getString(R.string.wifi_debugging_message, new Object[]{stringExtra, this.mBssid});
        alertParams.mPositiveButtonText = getString(R.string.wifi_debugging_allow);
        alertParams.mNegativeButtonText = getString(android.R.string.cancel);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        View viewInflate = LayoutInflater.from(alertParams.mContext).inflate(android.R.layout.alert_dialog_progress_material, (ViewGroup) null);
        CheckBox checkBox = (CheckBox) viewInflate.findViewById(android.R.id.actions_container_layout);
        this.mAlwaysAllow = checkBox;
        checkBox.setText(getString(R.string.wifi_debugging_always));
        alertParams.mView = viewInflate;
        window.setCloseOnTouchOutside(false);
        setupAlert();
        if (Settings.System.getIntForUser(alertParams.mContext.getContentResolver(), "smart_pixels_enable", 0, -2) == 1) {
            alertParams.mContext.getResources().getBoolean(android.R.bool.config_cecRcProfileSourceTopMenuNotHandled_default);
        }
        ((AlertActivity) this).mAlert.getButton(-1).setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.wifi.WifiDebuggingActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return WifiDebuggingActivity.lambda$onCreate$0(view, motionEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onCreate$0(View view, MotionEvent motionEvent) {
        if ((motionEvent.getFlags() & 1) == 0 && (motionEvent.getFlags() & 2) == 0) {
            return false;
        }
        if (motionEvent.getAction() == 1) {
            EventLog.writeEvent(1397638484, "62187985");
            Toast.makeText(view.getContext(), R.string.touch_filtered_warning, 0).show();
        }
        return true;
    }

    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        super.onWindowAttributesChanged(layoutParams);
    }

    private class WifiChangeReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        WifiChangeReceiver(Activity activity) {
            this.mActivity = activity;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                if (intent.getIntExtra("wifi_state", 1) == 1) {
                    this.mActivity.finish();
                    return;
                }
                return;
            }
            if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo.getType() == 1) {
                    if (networkInfo.isConnected()) {
                        WifiInfo connectionInfo = WifiDebuggingActivity.this.mWifiManager.getConnectionInfo();
                        if (connectionInfo == null || connectionInfo.getNetworkId() == -1) {
                            this.mActivity.finish();
                            return;
                        }
                        String bssid = connectionInfo.getBSSID();
                        if (bssid != null && !bssid.isEmpty()) {
                            if (bssid.equals(WifiDebuggingActivity.this.mBssid)) {
                                return;
                            }
                            this.mActivity.finish();
                            return;
                        }
                        this.mActivity.finish();
                        return;
                    }
                    this.mActivity.finish();
                }
            }
        }
    }

    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(this.mWifiChangeReceiver, intentFilter);
        sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    protected void onStop() {
        WifiChangeReceiver wifiChangeReceiver = this.mWifiChangeReceiver;
        if (wifiChangeReceiver != null) {
            unregisterReceiver(wifiChangeReceiver);
        }
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mClicked) {
            return;
        }
        try {
            IAdbManager.Stub.asInterface(ServiceManager.getService("adb")).denyWirelessDebugging();
        } catch (Exception e) {
            Log.e("WifiDebuggingActivity", "Unable to notify Adb service", e);
        }
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        this.mClicked = true;
        boolean z = i == -1;
        boolean z2 = z && this.mAlwaysAllow.isChecked();
        try {
            IAdbManager iAdbManagerAsInterface = IAdbManager.Stub.asInterface(ServiceManager.getService("adb"));
            if (z) {
                iAdbManagerAsInterface.allowWirelessDebugging(z2, this.mBssid);
            } else {
                iAdbManagerAsInterface.denyWirelessDebugging();
            }
        } catch (Exception e) {
            Log.e("WifiDebuggingActivity", "Unable to notify Adb service", e);
        }
        finish();
    }
}
