package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RowContentBindStageLogger.kt */
/* loaded from: classes.dex */
public final class RowContentBindStageLogger {
    private final LogBuffer buffer;

    public RowContentBindStageLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logStageParams(@NotNull String notifKey, @NotNull String stageParams) {
        Intrinsics.checkParameterIsNotNull(notifKey, "notifKey");
        Intrinsics.checkParameterIsNotNull(stageParams, "stageParams");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.RowContentBindStageLogger.logStageParams.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Invalidated notif " + receiver.getStr1() + " with params: \n" + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("RowContentBindStage", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(notifKey);
        logMessageImplObtain.setStr2(stageParams);
        logBuffer.push(logMessageImplObtain);
    }
}
