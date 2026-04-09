package com.android.systemui.classifier;

import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes.dex */
public class FalsingLog {
    public static final boolean ENABLED = SystemProperties.getBoolean("debug.falsing_log", Build.IS_DEBUGGABLE);
    private static final boolean LOGCAT = SystemProperties.getBoolean("debug.falsing_logcat", false);
    private static final int MAX_SIZE = SystemProperties.getInt("debug.falsing_log_size", 100);
    private static FalsingLog sInstance;
    private final ArrayDeque<String> mLog = new ArrayDeque<>(MAX_SIZE);
    private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);

    private FalsingLog() {
    }

    public static void i(String str, String str2) {
        if (LOGCAT) {
            Log.i("FalsingLog", str + "\t" + str2);
        }
        log("I", str, str2);
    }

    public static void wLogcat(String str, String str2) {
        Log.w("FalsingLog", str + "\t" + str2);
        log("W", str, str2);
    }

    public static void e(String str, String str2) {
        if (LOGCAT) {
            Log.e("FalsingLog", str + "\t" + str2);
        }
        log("E", str, str2);
    }

    public static synchronized void log(String str, String str2, String str3) {
        if (ENABLED) {
            if (sInstance == null) {
                sInstance = new FalsingLog();
            }
            if (sInstance.mLog.size() >= MAX_SIZE) {
                sInstance.mLog.removeFirst();
            }
            sInstance.mLog.add(sInstance.mFormat.format(new Date()) + " " + str + " " + str2 + " " + str3);
        }
    }

    public static synchronized void dump(PrintWriter printWriter) {
        printWriter.println("FALSING LOG:");
        if (!ENABLED) {
            printWriter.println("Disabled, to enable: setprop debug.falsing_log 1");
            printWriter.println();
            return;
        }
        FalsingLog falsingLog = sInstance;
        if (falsingLog != null && !falsingLog.mLog.isEmpty()) {
            Iterator<String> it = sInstance.mLog.iterator();
            while (it.hasNext()) {
                printWriter.println(it.next());
            }
            printWriter.println();
            return;
        }
        printWriter.println("<empty>");
        printWriter.println();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0086 A[Catch: all -> 0x00b4, TryCatch #2 {, blocks: (B:4:0x0003, B:8:0x0009, B:11:0x0018, B:15:0x0069, B:32:0x0091, B:25:0x007e, B:29:0x0086, B:30:0x0089, B:31:0x008a), top: B:41:0x0003 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static synchronized void wtf(java.lang.String r7, java.lang.String r8, java.lang.Throwable r9) {
        /*
            java.lang.Class<com.android.systemui.classifier.FalsingLog> r0 = com.android.systemui.classifier.FalsingLog.class
            monitor-enter(r0)
            boolean r1 = com.android.systemui.classifier.FalsingLog.ENABLED     // Catch: java.lang.Throwable -> Lb4
            if (r1 != 0) goto L9
            monitor-exit(r0)
            return
        L9:
            e(r7, r8)     // Catch: java.lang.Throwable -> Lb4
            android.app.Application r1 = android.app.ActivityThread.currentApplication()     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r2 = ""
            boolean r3 = android.os.Build.IS_DEBUGGABLE     // Catch: java.lang.Throwable -> Lb4
            if (r3 == 0) goto L8a
            if (r1 == 0) goto L8a
            java.io.File r3 = new java.io.File     // Catch: java.lang.Throwable -> Lb4
            java.io.File r1 = r1.getDataDir()     // Catch: java.lang.Throwable -> Lb4
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Lb4
            r4.<init>()     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r5 = "falsing-"
            r4.append(r5)     // Catch: java.lang.Throwable -> Lb4
            java.text.SimpleDateFormat r5 = new java.text.SimpleDateFormat     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r6 = "yyyy-MM-dd-HH-mm-ss"
            r5.<init>(r6)     // Catch: java.lang.Throwable -> Lb4
            java.util.Date r6 = new java.util.Date     // Catch: java.lang.Throwable -> Lb4
            r6.<init>()     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r5 = r5.format(r6)     // Catch: java.lang.Throwable -> Lb4
            r4.append(r5)     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r5 = ".txt"
            r4.append(r5)     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r4 = r4.toString()     // Catch: java.lang.Throwable -> Lb4
            r3.<init>(r1, r4)     // Catch: java.lang.Throwable -> Lb4
            r1 = 0
            java.io.PrintWriter r4 = new java.io.PrintWriter     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L72
            r4.<init>(r3)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L72
            dump(r4)     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            r4.close()     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            r1.<init>()     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            java.lang.String r5 = "Log written to "
            r1.append(r5)     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            java.lang.String r3 = r3.getAbsolutePath()     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            r1.append(r3)     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            java.lang.String r1 = r1.toString()     // Catch: java.io.IOException -> L6e java.lang.Throwable -> L82
            r4.close()     // Catch: java.lang.Throwable -> Lb4
            r2 = r1
            goto L91
        L6e:
            r1 = move-exception
            goto L75
        L70:
            r7 = move-exception
            goto L84
        L72:
            r3 = move-exception
            r4 = r1
            r1 = r3
        L75:
            java.lang.String r3 = "FalsingLog"
            java.lang.String r5 = "Unable to write falsing log"
            android.util.Log.e(r3, r5, r1)     // Catch: java.lang.Throwable -> L82
            if (r4 == 0) goto L91
            r4.close()     // Catch: java.lang.Throwable -> Lb4
            goto L91
        L82:
            r7 = move-exception
            r1 = r4
        L84:
            if (r1 == 0) goto L89
            r1.close()     // Catch: java.lang.Throwable -> Lb4
        L89:
            throw r7     // Catch: java.lang.Throwable -> Lb4
        L8a:
            java.lang.String r1 = "FalsingLog"
            java.lang.String r3 = "Unable to write log, build must be debuggable."
            android.util.Log.e(r1, r3)     // Catch: java.lang.Throwable -> Lb4
        L91:
            java.lang.String r1 = "FalsingLog"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Lb4
            r3.<init>()     // Catch: java.lang.Throwable -> Lb4
            r3.append(r7)     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r7 = " "
            r3.append(r7)     // Catch: java.lang.Throwable -> Lb4
            r3.append(r8)     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r7 = "; "
            r3.append(r7)     // Catch: java.lang.Throwable -> Lb4
            r3.append(r2)     // Catch: java.lang.Throwable -> Lb4
            java.lang.String r7 = r3.toString()     // Catch: java.lang.Throwable -> Lb4
            android.util.Log.wtf(r1, r7, r9)     // Catch: java.lang.Throwable -> Lb4
            monitor-exit(r0)
            return
        Lb4:
            r7 = move-exception
            monitor-exit(r0)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.classifier.FalsingLog.wtf(java.lang.String, java.lang.String, java.lang.Throwable):void");
    }
}
