package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationSectionsLogger.kt */
/* loaded from: classes.dex */
public final class NotificationSectionsLogger {
    private final LogBuffer logBuffer;

    public NotificationSectionsLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "logBuffer");
        this.logBuffer = logBuffer;
    }

    public final void logStartSectionUpdate(@NotNull final String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        LogBuffer logBuffer = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        Function1<LogMessage, String> function1 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger.logStartSectionUpdate.2
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Updating section boundaries: " + reason;
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifSections", logLevel, function1);
        logMessageImplObtain.setStr1(reason);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logIncomingHeader(int i) {
        logPosition(i, "INCOMING HEADER");
    }

    public final void logMediaControls(int i) {
        logPosition(i, "MEDIA CONTROLS");
    }

    public final void logConversationsHeader(int i) {
        logPosition(i, "CONVERSATIONS HEADER");
    }

    public final void logAlertingHeader(int i) {
        logPosition(i, "ALERTING HEADER");
    }

    public final void logSilentHeader(int i) {
        logPosition(i, "SILENT HEADER");
    }

    public final void logOther(int i, @NotNull Class<?> clazz) {
        Intrinsics.checkParameterIsNotNull(clazz, "clazz");
        LogBuffer logBuffer = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger.logOther.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return receiver.getInt1() + ": other (" + receiver.getStr1() + ')';
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifSections", logLevel, anonymousClass2);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(clazz.getName());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logHeadsUp(int i, boolean z) {
        logPosition(i, "Heads Up", z);
    }

    public final void logConversation(int i, boolean z) {
        logPosition(i, "Conversation", z);
    }

    public final void logAlerting(int i, boolean z) {
        logPosition(i, "Alerting", z);
    }

    public final void logSilent(int i, boolean z) {
        logPosition(i, "Silent", z);
    }

    public final void logStr(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "str");
        LogBuffer logBuffer = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01842 c01842 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger.logStr.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return String.valueOf(receiver.getStr1());
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifSections", logLevel, c01842);
        logMessageImplObtain.setStr1(str);
        logBuffer.push(logMessageImplObtain);
    }

    private final void logPosition(int i, String str, boolean z) {
        String str2 = z ? " (HUN)" : "";
        LogBuffer logBuffer = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C01822 c01822 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger.logPosition.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return receiver.getInt1() + ": " + receiver.getStr1() + receiver.getStr2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifSections", logLevel, c01822);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(str);
        logMessageImplObtain.setStr2(str2);
        logBuffer.push(logMessageImplObtain);
    }

    private final void logPosition(int i, String str) {
        LogBuffer logBuffer = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass4 anonymousClass4 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger.logPosition.4
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return receiver.getInt1() + ": " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotifSections", logLevel, anonymousClass4);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(str);
        logBuffer.push(logMessageImplObtain);
    }
}
