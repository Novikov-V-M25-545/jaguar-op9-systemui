package com.android.systemui.broadcast.logging;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import java.util.Iterator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequencesKt__SequencesKt;
import kotlin.sequences.SequencesKt___SequencesKt;
import kotlin.text.StringsKt__IndentKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BroadcastDispatcherLogger.kt */
/* loaded from: classes.dex */
public final class BroadcastDispatcherLogger {
    private final LogBuffer buffer;

    public BroadcastDispatcherLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logBroadcastReceived(int i, int i2, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        String string = intent.toString();
        Intrinsics.checkExpressionValueIsNotNull(string, "intent.toString()");
        LogLevel logLevel = LogLevel.INFO;
        C00162 c00162 = new Function1<LogMessage, String>() { // from class: com.android.systemui.broadcast.logging.BroadcastDispatcherLogger.logBroadcastReceived.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getInt1() + "] Broadcast received for user " + receiver.getInt2() + ": " + receiver.getStr1();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("BroadcastDispatcherLog", logLevel, c00162);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setInt2(i2);
        logMessageImplObtain.setStr1(string);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logBroadcastDispatched(int i, @Nullable String str, @NotNull BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        String string = receiver.toString();
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.broadcast.logging.BroadcastDispatcherLogger.logBroadcastDispatched.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver2) {
                Intrinsics.checkParameterIsNotNull(receiver2, "$receiver");
                return "Broadcast " + receiver2.getInt1() + " (" + receiver2.getStr1() + ") dispatched to " + receiver2.getStr2();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("BroadcastDispatcherLog", logLevel, anonymousClass2);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(str);
        logMessageImplObtain.setStr2(string);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logReceiverRegistered(int i, @NotNull BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        String string = receiver.toString();
        LogLevel logLevel = LogLevel.INFO;
        C00192 c00192 = new Function1<LogMessage, String>() { // from class: com.android.systemui.broadcast.logging.BroadcastDispatcherLogger.logReceiverRegistered.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver2) {
                Intrinsics.checkParameterIsNotNull(receiver2, "$receiver");
                return "Receiver " + receiver2.getStr1() + " registered for user " + receiver2.getInt1();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("BroadcastDispatcherLog", logLevel, c00192);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(string);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logReceiverUnregistered(int i, @NotNull BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        String string = receiver.toString();
        LogLevel logLevel = LogLevel.INFO;
        C00202 c00202 = new Function1<LogMessage, String>() { // from class: com.android.systemui.broadcast.logging.BroadcastDispatcherLogger.logReceiverUnregistered.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver2) {
                Intrinsics.checkParameterIsNotNull(receiver2, "$receiver");
                return "Receiver " + receiver2.getStr1() + " unregistered for user " + receiver2.getInt1();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("BroadcastDispatcherLog", logLevel, c00202);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(string);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logContextReceiverRegistered(int i, @NotNull IntentFilter filter) {
        String strJoinToString$default;
        Intrinsics.checkParameterIsNotNull(filter, "filter");
        Iterator<String> itActionsIterator = filter.actionsIterator();
        Intrinsics.checkExpressionValueIsNotNull(itActionsIterator, "filter.actionsIterator()");
        String strJoinToString$default2 = SequencesKt___SequencesKt.joinToString$default(SequencesKt__SequencesKt.asSequence(itActionsIterator), ",", "Actions(", ")", 0, null, null, 56, null);
        if (filter.countCategories() != 0) {
            Iterator<String> itCategoriesIterator = filter.categoriesIterator();
            Intrinsics.checkExpressionValueIsNotNull(itCategoriesIterator, "filter.categoriesIterator()");
            strJoinToString$default = SequencesKt___SequencesKt.joinToString$default(SequencesKt__SequencesKt.asSequence(itCategoriesIterator), ",", "Categories(", ")", 0, null, null, 56, null);
        } else {
            strJoinToString$default = "";
        }
        LogLevel logLevel = LogLevel.INFO;
        C00172 c00172 = new Function1<LogMessage, String>() { // from class: com.android.systemui.broadcast.logging.BroadcastDispatcherLogger.logContextReceiverRegistered.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return StringsKt__IndentKt.trimIndent("\n                Receiver registered with Context for user " + receiver.getInt1() + ".\n                " + receiver.getStr1() + "\n            ");
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("BroadcastDispatcherLog", logLevel, c00172);
        logMessageImplObtain.setInt1(i);
        if (!Intrinsics.areEqual(strJoinToString$default, "")) {
            strJoinToString$default2 = strJoinToString$default2 + '\n' + strJoinToString$default;
        }
        logMessageImplObtain.setStr1(strJoinToString$default2);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logContextReceiverUnregistered(int i, @NotNull String action) {
        Intrinsics.checkParameterIsNotNull(action, "action");
        LogLevel logLevel = LogLevel.INFO;
        C00182 c00182 = new Function1<LogMessage, String>() { // from class: com.android.systemui.broadcast.logging.BroadcastDispatcherLogger.logContextReceiverUnregistered.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Receiver unregistered with Context for user " + receiver.getInt1() + ", action " + receiver.getStr1();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("BroadcastDispatcherLog", logLevel, c00182);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr1(action);
        logBuffer.push(logMessageImplObtain);
    }
}
