package com.android.systemui.statusbar.notification;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationEntryManagerLogger.kt */
/* loaded from: classes.dex */
public final class NotificationEntryManagerLogger {
    private final LogBuffer buffer;

    public NotificationEntryManagerLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logNotifAdded(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01322 c01322 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logNotifAdded.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF ADDED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01322);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifUpdated(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01352 c01352 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logNotifUpdated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF UPDATED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01352);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logInflationAborted(@NotNull String key, @NotNull String status, @NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(status, "status");
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01302 c01302 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logInflationAborted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF INFLATION ABORTED " + receiver.getStr1() + " notifStatus=" + receiver.getStr2() + " reason=" + receiver.getStr3();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01302);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(status);
        logMessageImplObtain.setStr3(reason);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifInflated(@NotNull String key, boolean z) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01332 c01332 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logNotifInflated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF INFLATED " + receiver.getStr1() + " isNew=" + receiver.getBool1() + '}';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01332);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logRemovalIntercepted(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01362 c01362 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logRemovalIntercepted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF REMOVE INTERCEPTED for " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01362);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logLifetimeExtended(@NotNull String key, @NotNull String extenderName, @NotNull String status) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(extenderName, "extenderName");
        Intrinsics.checkParameterIsNotNull(status, "status");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01312 c01312 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logLifetimeExtended.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF LIFETIME EXTENDED " + receiver.getStr1() + " extender=" + receiver.getStr2() + " status=" + receiver.getStr3();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01312);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(extenderName);
        logMessageImplObtain.setStr3(status);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNotifRemoved(@NotNull String key, boolean z) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01342 c01342 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logNotifRemoved.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF REMOVED " + receiver.getStr1() + " removedByUser=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, c01342);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFilterAndSort(@NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManagerLogger.logFilterAndSort.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "FILTER AND SORT reason=" + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationEntryMgr", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(reason);
        logBuffer.push(logMessageImplObtain);
    }
}
