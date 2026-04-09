package com.android.systemui;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class JaguarIdleManager {
    static List<ActivityManager.RunningAppProcessInfo> RunningServices = null;
    static String TAG = "JaguarIdleManager";
    static Context imContext;
    static List<String> killablePackages;
    static ActivityManager localActivityManager;
    static ContentResolver mContentResolver;
    static Runnable rStateThree;
    static Runnable rStateTwo;
    static Handler h = new Handler();
    static final String[] LOG_MSGS = {"just ran ", "rStateTwo Immediate!", "rStateTwo", "rStateThree", "alarmTime ", "realTime "};

    public static void initManager(Context context) {
        imContext = context;
        killablePackages = new ArrayList();
        localActivityManager = (ActivityManager) context.getSystemService("activity");
        mContentResolver = context.getContentResolver();
        rStateTwo = new Runnable() { // from class: com.android.systemui.JaguarIdleManager.1
            @Override // java.lang.Runnable
            public void run() {
                JaguarIdleManager.servicesKiller();
            }
        };
        rStateThree = new Runnable() { // from class: com.android.systemui.JaguarIdleManager.2
            @Override // java.lang.Runnable
            public void run() {
                JaguarIdleManager.haltManager();
            }
        };
    }

    public static void executeManager() {
        RunningServices = localActivityManager.getRunningAppProcesses();
        if (4000000 > msTillAlarm(imContext) && msTillAlarm(imContext) != 0) {
            IdleManLog("executeManager " + LOG_MSGS[1]);
            h.postDelayed(rStateTwo, 100L);
        } else {
            IdleManLog("executeManager " + LOG_MSGS[2]);
            h.postDelayed(rStateTwo, 4000000L);
        }
        if (msTillAlarm(imContext) != 0) {
            IdleManLog("executeManager " + LOG_MSGS[3]);
            h.postDelayed(rStateThree, msTillAlarm(imContext) - 900000);
        }
    }

    public static void haltManager() {
        IdleManLog(LOG_MSGS[0] + "haltManager");
        h.removeCallbacks(rStateTwo);
        theAwakening();
    }

    public static void theAwakening() {
        IdleManLog(LOG_MSGS[0] + "theAwakening");
        h.removeCallbacks(rStateThree);
    }

    public static long msTillAlarm(Context context) {
        StringBuilder sb = new StringBuilder();
        String[] strArr = LOG_MSGS;
        sb.append(strArr[0]);
        sb.append("msTillAlarm");
        IdleManLog(sb.toString());
        AlarmManager.AlarmClockInfo nextAlarmClock = ((AlarmManager) context.getSystemService("alarm")).getNextAlarmClock();
        if (nextAlarmClock == null) {
            return 0L;
        }
        long triggerTime = nextAlarmClock.getTriggerTime();
        IdleManLog("msTillAlarm" + strArr[4] + Long.toString(triggerTime));
        long jCurrentTimeMillis = triggerTime - System.currentTimeMillis();
        IdleManLog("msTillAlarm" + strArr[5] + Long.toString(jCurrentTimeMillis));
        return jCurrentTimeMillis;
    }

    public static void servicesKiller() {
        IdleManLog(LOG_MSGS[0] + "servicesKiller");
        ActivityManager activityManager = (ActivityManager) imContext.getSystemService("activity");
        localActivityManager = activityManager;
        RunningServices = activityManager.getRunningAppProcesses();
        for (int i = 0; i < RunningServices.size(); i++) {
            if (!RunningServices.get(i).pkgList[0].toString().contains("com.android.") && !RunningServices.get(i).pkgList[0].toString().equals("android") && !RunningServices.get(i).pkgList[0].toString().contains("google") && !RunningServices.get(i).pkgList[0].toString().equals("google") && !RunningServices.get(i).pkgList[0].toString().contains("launcher") && !RunningServices.get(i).pkgList[0].toString().contains("settings") && !RunningServices.get(i).pkgList[0].toString().contains("ims")) {
                localActivityManager.killBackgroundProcesses(RunningServices.get(i).pkgList[0].toString());
            }
        }
    }

    private static void IdleManLog(String str) {
        if (SystemProperties.getBoolean("jaguar.debug", false)) {
            Log.d(TAG, str);
        }
    }
}
