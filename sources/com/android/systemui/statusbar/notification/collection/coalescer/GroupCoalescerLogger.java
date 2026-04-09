package com.android.systemui.statusbar.notification.collection.coalescer;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: GroupCoalescerLogger.kt */
/* loaded from: classes.dex */
public final class GroupCoalescerLogger {
    private final LogBuffer buffer;

    public GroupCoalescerLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logEventCoalesced(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01412 c01412 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger.logEventCoalesced.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "COALESCED: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("GroupCoalescer", logLevel, c01412);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logEmitBatch(@NotNull String groupKey) {
        Intrinsics.checkParameterIsNotNull(groupKey, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01402 c01402 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger.logEmitBatch.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Emitting event batch for group " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("GroupCoalescer", logLevel, c01402);
        logMessageImplObtain.setStr1(groupKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logEarlyEmit(@NotNull String modifiedKey, @NotNull String groupKey) {
        Intrinsics.checkParameterIsNotNull(modifiedKey, "modifiedKey");
        Intrinsics.checkParameterIsNotNull(groupKey, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger.logEarlyEmit.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Modification of notif " + receiver.getStr1() + " triggered early emit of batched group " + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("GroupCoalescer", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(modifiedKey);
        logMessageImplObtain.setStr2(groupKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logMaxBatchTimeout(@NotNull String modifiedKey, @NotNull String groupKey) {
        Intrinsics.checkParameterIsNotNull(modifiedKey, "modifiedKey");
        Intrinsics.checkParameterIsNotNull(groupKey, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01422 c01422 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger.logMaxBatchTimeout.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Modification of notif " + receiver.getStr1() + " triggered TIMEOUT emit of batched group " + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("GroupCoalescer", logLevel, c01422);
        logMessageImplObtain.setStr1(modifiedKey);
        logMessageImplObtain.setStr2(groupKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logMissingRanking(@NotNull String forKey) {
        Intrinsics.checkParameterIsNotNull(forKey, "forKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        C01432 c01432 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger.logMissingRanking.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "RankingMap is missing an entry for coalesced notification " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("GroupCoalescer", logLevel, c01432);
        logMessageImplObtain.setStr1(forKey);
        logBuffer.push(logMessageImplObtain);
    }
}
