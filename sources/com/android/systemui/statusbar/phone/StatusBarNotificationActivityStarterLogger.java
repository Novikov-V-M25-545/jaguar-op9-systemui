package com.android.systemui.statusbar.phone;

import android.app.PendingIntent;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StatusBarNotificationActivityStarterLogger.kt */
/* loaded from: classes.dex */
public final class StatusBarNotificationActivityStarterLogger {
    private final LogBuffer buffer;

    public StatusBarNotificationActivityStarterLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logStartingActivityFromClick(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01952 c01952 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logStartingActivityFromClick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(1/4) onNotificationClicked: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01952);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logHandleClickAfterKeyguardDismissed(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01892 c01892 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logHandleClickAfterKeyguardDismissed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(2/4) handleNotificationClickAfterKeyguardDismissed: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01892);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logHandleClickAfterPanelCollapsed(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01902 c01902 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logHandleClickAfterPanelCollapsed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(3/4) handleNotificationClickAfterPanelCollapsed: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01902);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logStartNotificationIntent(@NotNull String key, @NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01942 c01942 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logStartNotificationIntent.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "(4/4) Starting " + receiver.getStr2() + " for notification " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01942);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(pendingIntent.getIntent().toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logExpandingBubble(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logExpandingBubble.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Expanding bubble for " + receiver.getStr1() + " (rather than firing intent)";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logSendingIntentFailed(@NotNull Exception e) {
        Intrinsics.checkParameterIsNotNull(e, "e");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        C01932 c01932 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logSendingIntentFailed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Sending contentIntentFailed: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01932);
        logMessageImplObtain.setStr1(e.toString());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logNonClickableNotification(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.ERROR;
        C01912 c01912 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logNonClickableNotification.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "onNotificationClicked called for non-clickable notification! " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01912);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFullScreenIntentSuppressedByDnD(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01882 c01882 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logFullScreenIntentSuppressedByDnD.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "No Fullscreen intent: suppressed by DND: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01882);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFullScreenIntentNotImportantEnough(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01872 c01872 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logFullScreenIntentNotImportantEnough.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "No Fullscreen intent: not important enough: " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01872);
        logMessageImplObtain.setStr1(key);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logSendingFullScreenIntent(@NotNull String key, @NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C01922 c01922 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger.logSendingFullScreenIntent.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Notification " + receiver.getStr1() + " has fullScreenIntent; sending fullScreenIntent " + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifActivityStarter", logLevel, c01922);
        logMessageImplObtain.setStr1(key);
        logMessageImplObtain.setStr2(pendingIntent.getIntent().toString());
        logBuffer.push(logMessageImplObtain);
    }
}
