package com.android.systemui.log;

import android.util.Log;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.SensorManagerPlugin;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogBuffer.kt */
/* loaded from: classes.dex */
public final class LogBuffer {
    private final ArrayDeque<LogMessageImpl> buffer;
    private boolean frozen;
    private final LogcatEchoTracker logcatEchoTracker;
    private final int maxLogs;
    private final String name;
    private final int poolSize;

    public final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[LogLevel.values().length];
            $EnumSwitchMapping$0 = iArr;
            iArr[LogLevel.VERBOSE.ordinal()] = 1;
            iArr[LogLevel.DEBUG.ordinal()] = 2;
            iArr[LogLevel.INFO.ordinal()] = 3;
            iArr[LogLevel.WARNING.ordinal()] = 4;
            iArr[LogLevel.ERROR.ordinal()] = 5;
            iArr[LogLevel.WTF.ordinal()] = 6;
        }
    }

    public LogBuffer(@NotNull String name, int i, int i2, @NotNull LogcatEchoTracker logcatEchoTracker) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        Intrinsics.checkParameterIsNotNull(logcatEchoTracker, "logcatEchoTracker");
        this.name = name;
        this.maxLogs = i;
        this.poolSize = i2;
        this.logcatEchoTracker = logcatEchoTracker;
        this.buffer = new ArrayDeque<>();
    }

    public final boolean getFrozen() {
        return this.frozen;
    }

    public final void attach(@NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        dumpManager.registerBuffer(this.name, this);
    }

    @NotNull
    public final synchronized LogMessageImpl obtain(@NotNull String tag, @NotNull LogLevel level, @NotNull Function1<? super LogMessage, String> printer) {
        LogMessageImpl message;
        Intrinsics.checkParameterIsNotNull(tag, "tag");
        Intrinsics.checkParameterIsNotNull(level, "level");
        Intrinsics.checkParameterIsNotNull(printer, "printer");
        message = (!this.frozen && this.buffer.size() > this.maxLogs - this.poolSize) ? this.buffer.removeFirst() : LogMessageImpl.Factory.create();
        message.reset(tag, level, System.currentTimeMillis(), printer);
        Intrinsics.checkExpressionValueIsNotNull(message, "message");
        return message;
    }

    public final synchronized void push(@NotNull LogMessage message) {
        Intrinsics.checkParameterIsNotNull(message, "message");
        if (this.frozen) {
            return;
        }
        if (this.buffer.size() == this.maxLogs) {
            Log.e("LogBuffer", "LogBuffer " + this.name + " has exceeded its pool size");
            this.buffer.removeFirst();
        }
        this.buffer.add((LogMessageImpl) message);
        if (this.logcatEchoTracker.isBufferLoggable(this.name, ((LogMessageImpl) message).getLevel()) || this.logcatEchoTracker.isTagLoggable(((LogMessageImpl) message).getTag(), ((LogMessageImpl) message).getLevel())) {
            echoToLogcat(message);
        }
    }

    public final synchronized void dump(@NotNull PrintWriter pw, int i) {
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        int i2 = 0;
        int size = i <= 0 ? 0 : this.buffer.size() - i;
        for (LogMessageImpl message : this.buffer) {
            if (i2 >= size) {
                Intrinsics.checkExpressionValueIsNotNull(message, "message");
                dumpMessage(message, pw);
            }
            i2++;
        }
    }

    public final synchronized void freeze() {
        if (!this.frozen) {
            LogLevel logLevel = LogLevel.DEBUG;
            AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.log.LogBuffer.freeze.2
                @Override // kotlin.jvm.functions.Function1
                @NotNull
                public final String invoke(@NotNull LogMessage receiver) {
                    Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                    return receiver.getStr1() + " frozen";
                }
            };
            if (!getFrozen()) {
                LogMessageImpl logMessageImplObtain = obtain("LogBuffer", logLevel, anonymousClass2);
                logMessageImplObtain.setStr1(this.name);
                push(logMessageImplObtain);
            }
            this.frozen = true;
        }
    }

    public final synchronized void unfreeze() {
        if (this.frozen) {
            LogLevel logLevel = LogLevel.DEBUG;
            C00782 c00782 = new Function1<LogMessage, String>() { // from class: com.android.systemui.log.LogBuffer.unfreeze.2
                @Override // kotlin.jvm.functions.Function1
                @NotNull
                public final String invoke(@NotNull LogMessage receiver) {
                    Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                    return receiver.getStr1() + " unfrozen";
                }
            };
            if (!getFrozen()) {
                LogMessageImpl logMessageImplObtain = obtain("LogBuffer", logLevel, c00782);
                logMessageImplObtain.setStr1(this.name);
                push(logMessageImplObtain);
            }
            this.frozen = false;
        }
    }

    private final void dumpMessage(LogMessage logMessage, PrintWriter printWriter) {
        printWriter.print(LogBufferKt.DATE_FORMAT.format(Long.valueOf(logMessage.getTimestamp())));
        printWriter.print(" ");
        printWriter.print(logMessage.getLevel());
        printWriter.print(" ");
        printWriter.print(logMessage.getTag());
        printWriter.print(" ");
        printWriter.println(logMessage.getPrinter().invoke(logMessage));
    }

    private final void echoToLogcat(LogMessage logMessage) {
        String strInvoke = logMessage.getPrinter().invoke(logMessage);
        switch (WhenMappings.$EnumSwitchMapping$0[logMessage.getLevel().ordinal()]) {
            case 1:
                Log.v(logMessage.getTag(), strInvoke);
                break;
            case 2:
                Log.d(logMessage.getTag(), strInvoke);
                break;
            case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                Log.i(logMessage.getTag(), strInvoke);
                break;
            case 4:
                Log.w(logMessage.getTag(), strInvoke);
                break;
            case 5:
                Log.e(logMessage.getTag(), strInvoke);
                break;
            case 6:
                Log.wtf(logMessage.getTag(), strInvoke);
                break;
        }
    }
}
