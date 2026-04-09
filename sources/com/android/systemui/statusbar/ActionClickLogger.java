package com.android.systemui.statusbar;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.service.notification.NotificationListenerService;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ActionClickLogger.kt */
/* loaded from: classes.dex */
public final class ActionClickLogger {
    private final LogBuffer buffer;

    public ActionClickLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logInitialClick(@Nullable NotificationEntry notificationEntry, @NotNull PendingIntent pendingIntent) {
        NotificationListenerService.Ranking ranking;
        NotificationChannel channel;
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.ActionClickLogger.logInitialClick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "ACTION CLICK " + receiver.getStr1() + " (channel=" + receiver.getStr2() + ") for pending intent " + receiver.getStr3();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ActionClickLogger", logLevel, anonymousClass2);
        String id = null;
        logMessageImplObtain.setStr1(notificationEntry != null ? notificationEntry.getKey() : null);
        if (notificationEntry != null && (ranking = notificationEntry.getRanking()) != null && (channel = ranking.getChannel()) != null) {
            id = channel.getId();
        }
        logMessageImplObtain.setStr2(id);
        logMessageImplObtain.setStr3(pendingIntent.getIntent().toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logRemoteInputWasHandled(@Nullable NotificationEntry notificationEntry) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01162 c01162 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.ActionClickLogger.logRemoteInputWasHandled.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "  [Action click] Triggered remote input (for " + receiver.getStr1() + "))";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ActionClickLogger", logLevel, c01162);
        logMessageImplObtain.setStr1(notificationEntry != null ? notificationEntry.getKey() : null);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logStartingIntentWithDefaultHandler(@Nullable NotificationEntry notificationEntry, @NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01172 c01172 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.ActionClickLogger.logStartingIntentWithDefaultHandler.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "  [Action click] Launching intent " + receiver.getStr2() + " via default handler (for " + receiver.getStr1() + ')';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ActionClickLogger", logLevel, c01172);
        logMessageImplObtain.setStr1(notificationEntry != null ? notificationEntry.getKey() : null);
        logMessageImplObtain.setStr2(pendingIntent.getIntent().toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logWaitingToCloseKeyguard(@NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01182 c01182 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.ActionClickLogger.logWaitingToCloseKeyguard.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "  [Action click] Intent " + receiver.getStr1() + " launches an activity, dismissing keyguard first...";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ActionClickLogger", logLevel, c01182);
        logMessageImplObtain.setStr1(pendingIntent.getIntent().toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logKeyguardGone(@NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01152 c01152 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.ActionClickLogger.logKeyguardGone.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "  [Action click] Keyguard dismissed, calling default handler for intent " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("ActionClickLogger", logLevel, c01152);
        logMessageImplObtain.setStr1(pendingIntent.getIntent().toString());
        logBuffer.push(logMessageImplObtain);
    }
}
