package com.android.systemui.log;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogMessageImpl.kt */
/* loaded from: classes.dex */
public final class LogMessageImplKt {
    private static final Function1<LogMessage, String> DEFAULT_RENDERER = new Function1<LogMessage, String>() { // from class: com.android.systemui.log.LogMessageImplKt$DEFAULT_RENDERER$1
        @Override // kotlin.jvm.functions.Function1
        @NotNull
        public final String invoke(@NotNull LogMessage receiver) {
            Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
            return "Unknown message: " + receiver;
        }
    };
}
