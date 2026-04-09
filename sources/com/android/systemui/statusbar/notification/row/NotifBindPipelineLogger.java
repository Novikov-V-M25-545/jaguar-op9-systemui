package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifBindPipelineLogger.kt */
/* loaded from: classes.dex */
public final class NotifBindPipelineLogger {
    private final LogBuffer buffer;

    public NotifBindPipelineLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logStageSet(@NotNull String stageName) {
        Intrinsics.checkParameterIsNotNull(stageName, "stageName");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01802 c01802 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger.logStageSet.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Stage set: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifBindPipeline", logLevel, c01802);
        logMessageImplObtain.setStr1(stageName);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logManagedRow(@NotNull String notifKey) {
        Intrinsics.checkParameterIsNotNull(notifKey, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01772 c01772 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger.logManagedRow.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Row set for notif: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifBindPipeline", logLevel, c01772);
        logMessageImplObtain.setStr1(notifKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logRequestPipelineRun(@NotNull String notifKey) {
        Intrinsics.checkParameterIsNotNull(notifKey, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01792 c01792 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger.logRequestPipelineRun.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Request pipeline run for notif: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifBindPipeline", logLevel, c01792);
        logMessageImplObtain.setStr1(notifKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logRequestPipelineRowNotSet(@NotNull String notifKey) {
        Intrinsics.checkParameterIsNotNull(notifKey, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        C01782 c01782 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger.logRequestPipelineRowNotSet.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Row is not set so pipeline will not run. notif = " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifBindPipeline", logLevel, c01782);
        logMessageImplObtain.setStr1(notifKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logStartPipeline(@NotNull String notifKey) {
        Intrinsics.checkParameterIsNotNull(notifKey, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01812 c01812 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger.logStartPipeline.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Start pipeline for notif: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifBindPipeline", logLevel, c01812);
        logMessageImplObtain.setStr1(notifKey);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFinishedPipeline(@NotNull String notifKey, int i) {
        Intrinsics.checkParameterIsNotNull(notifKey, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger.logFinishedPipeline.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Finished pipeline for notif " + receiver.getStr1() + " with " + receiver.getInt1() + " callbacks";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifBindPipeline", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(notifKey);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }
}
