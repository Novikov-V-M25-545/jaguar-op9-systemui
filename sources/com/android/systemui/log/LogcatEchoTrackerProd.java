package com.android.systemui.log;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogcatEchoTrackerProd.kt */
/* loaded from: classes.dex */
public final class LogcatEchoTrackerProd implements LogcatEchoTracker {
    @Override // com.android.systemui.log.LogcatEchoTracker
    public boolean isBufferLoggable(@NotNull String bufferName, @NotNull LogLevel level) {
        Intrinsics.checkParameterIsNotNull(bufferName, "bufferName");
        Intrinsics.checkParameterIsNotNull(level, "level");
        return level.compareTo(LogLevel.WARNING) >= 0;
    }

    @Override // com.android.systemui.log.LogcatEchoTracker
    public boolean isTagLoggable(@NotNull String tagName, @NotNull LogLevel level) {
        Intrinsics.checkParameterIsNotNull(tagName, "tagName");
        Intrinsics.checkParameterIsNotNull(level, "level");
        return level.compareTo(LogLevel.WARNING) >= 0;
    }
}
