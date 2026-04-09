package com.android.systemui.statusbar.notification;

import android.app.NotificationChannel;
import android.service.notification.NotificationListenerService;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationClickerLogger.kt */
/* loaded from: classes.dex */
public final class NotificationClickerLogger {
    private final LogBuffer buffer;

    public NotificationClickerLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logOnClick(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01282 c01282 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationClickerLogger.logOnClick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "CLICK " + receiver.getStr1() + " (channel=" + receiver.getStr2() + ')';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationClicker", logLevel, c01282);
        logMessageImplObtain.setStr1(entry.getKey());
        NotificationListenerService.Ranking ranking = entry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
        NotificationChannel channel = ranking.getChannel();
        Intrinsics.checkExpressionValueIsNotNull(channel, "entry.ranking.channel");
        logMessageImplObtain.setStr2(channel.getId());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logMenuVisible(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01272 c01272 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationClickerLogger.logMenuVisible.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Ignoring click on " + receiver.getStr1() + "; menu is visible";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationClicker", logLevel, c01272);
        logMessageImplObtain.setStr1(entry.getKey());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logParentMenuVisible(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01292 c01292 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationClickerLogger.logParentMenuVisible.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Ignoring click on " + receiver.getStr1() + "; parent menu is visible";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationClicker", logLevel, c01292);
        logMessageImplObtain.setStr1(entry.getKey());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logChildrenExpanded(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationClickerLogger.logChildrenExpanded.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Ignoring click on " + receiver.getStr1() + "; children are expanded";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationClicker", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(entry.getKey());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logGutsExposed(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01262 c01262 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.NotificationClickerLogger.logGutsExposed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Ignoring click on " + receiver.getStr1() + "; guts are exposed";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotificationClicker", logLevel, c01262);
        logMessageImplObtain.setStr1(entry.getKey());
        logBuffer.push(logMessageImplObtain);
    }
}
