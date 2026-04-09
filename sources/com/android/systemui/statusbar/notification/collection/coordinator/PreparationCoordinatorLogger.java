package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PreparationCoordinatorLogger.kt */
/* loaded from: classes.dex */
public final class PreparationCoordinatorLogger {
    private final LogBuffer buffer;

    public PreparationCoordinatorLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logNotifInflated(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01442 c01442 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinatorLogger.logNotifInflated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF INFLATED " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("PreparationCoordinator", logLevel, c01442);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logInflationAborted(@NotNull String key, @NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinatorLogger.logInflationAborted.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "NOTIF INFLATION ABORTED " + receiver.getStr1() + " reason=" + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("PreparationCoordinator", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(reason);
        logBuffer.push(logMessageImplObtain);
    }
}
