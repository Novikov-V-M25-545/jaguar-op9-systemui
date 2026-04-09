package com.android.settingslib.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.util.Date;

/* loaded from: classes.dex */
public class WifiTimeoutReceiver extends BroadcastReceiver {
    public static void setTimeoutAlarm(Context context, long j) {
        Intent intent = new Intent("android.net.wifi.intent.TIMEOUT");
        intent.setClass(context, WifiTimeoutReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 268435456);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (j != 0) {
            long jCurrentTimeMillis = System.currentTimeMillis() + j;
            Log.d("WifiTimeoutReceiver", "setTimeoutAlarm(): alarmTime = " + new Date(jCurrentTimeMillis));
            alarmManager.setExactAndAllowWhileIdle(0, jCurrentTimeMillis, broadcast);
            return;
        }
        alarmManager.cancel(broadcast);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null || intent.getAction().equals("android.net.wifi.intent.TIMEOUT")) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(WifiManager.class);
            if (wifiManager != null) {
                if (wifiManager.isWifiEnabled() && wifiManager.getCurrentNetwork() == null) {
                    wifiManager.setWifiEnabled(false);
                    return;
                }
                return;
            }
            Log.e("WifiTimeoutReceiver", "wifiManager is NULL!!");
        }
    }
}
