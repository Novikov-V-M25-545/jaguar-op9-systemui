package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: SharedCoordinatorLogger.kt */
/* loaded from: classes.dex */
public final class SharedCoordinatorLogger {
    private final LogBuffer buffer;

    public SharedCoordinatorLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logUserOrProfileChanged(int i, @NotNull String profiles) {
        Intrinsics.checkParameterIsNotNull(profiles, "profiles");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.SharedCoordinatorLogger.logUserOrProfileChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Current user or profiles changed. Current user is " + receiver.getInt1() + "; profiles are " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("NotCurrentUserFilter", logLevel, anonymousClass2);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(profiles);
        logBuffer.push(logMessageImplObtain);
    }
}
