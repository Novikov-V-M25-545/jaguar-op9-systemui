package com.android.settingslib.bluetooth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Date;

/* loaded from: classes.dex */
public class BluetoothTimeoutReceiver extends BroadcastReceiver {
    public static void setTimeoutAlarm(Context context, long j) {
        Intent intent = new Intent("android.bluetooth.intent.TIMEOUT");
        intent.setClassName("com.android.settings", "com.android.settingslib.bluetooth.BluetoothTimeoutReceiver");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 268435456);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (j != 0) {
            long jCurrentTimeMillis = System.currentTimeMillis() + j;
            Log.d("BluetoothTimeoutReceiver", "setTimeoutAlarm(): alarmTime = " + new Date(jCurrentTimeMillis));
            alarmManager.setExactAndAllowWhileIdle(0, jCurrentTimeMillis, broadcast);
            return;
        }
        alarmManager.cancel(broadcast);
    }
}
