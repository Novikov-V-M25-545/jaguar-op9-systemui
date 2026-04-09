package com.android.systemui.doze;

import com.android.systemui.doze.DozeMachine;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import java.util.Date;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DozeLogger.kt */
/* loaded from: classes.dex */
public final class DozeLogger {
    private final LogBuffer buffer;

    public DozeLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logPickupWakeup(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00662 c00662 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logPickupWakeup.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "PickupWakeup withinVibrationThreshold=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00662);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPulseStart(int i) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00702 c00702 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logPulseStart.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Pulse start, reason=" + DozeLog.reasonToString(receiver.getInt1());
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00702);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPulseFinish() {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00692 c00692 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logPulseFinish.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Pulse finish";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        logBuffer.push(logBuffer.obtain("DozeLog", logLevel, c00692));
    }

    public final void logNotificationPulse() {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00652 c00652 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logNotificationPulse.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Notification pulse";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        logBuffer.push(logBuffer.obtain("DozeLog", logLevel, c00652));
    }

    public final void logDozing(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00592 c00592 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logDozing.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Dozing=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00592);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logFling(boolean z, boolean z2, boolean z3, boolean z4) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00612 c00612 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logFling.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Fling expand=" + receiver.getBool1() + " aboveThreshold=" + receiver.getBool2() + " thresholdNeeded=" + receiver.getBool3() + " screenOnFromTouch=" + receiver.getBool4();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00612);
        logMessageImplObtain.setBool1(z);
        logMessageImplObtain.setBool2(z2);
        logMessageImplObtain.setBool3(z3);
        logMessageImplObtain.setBool4(z4);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logEmergencyCall() {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00602 c00602 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logEmergencyCall.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Emergency call";
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        logBuffer.push(logBuffer.obtain("DozeLog", logLevel, c00602));
    }

    public final void logKeyguardBouncerChanged(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00622 c00622 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logKeyguardBouncerChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Keyguard bouncer changed, showing=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00622);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logScreenOn(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00732 c00732 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logScreenOn.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Screen on, pulsing=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00732);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logScreenOff(int i) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00722 c00722 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logScreenOff.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Screen off, why=" + receiver.getInt1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00722);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logMissedTick(@NotNull String delay) {
        Intrinsics.checkParameterIsNotNull(delay, "delay");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.ERROR;
        C00642 c00642 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logMissedTick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Missed AOD time tick by " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00642);
        logMessageImplObtain.setStr1(delay);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTimeTickScheduled(long j, long j2) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00752 c00752 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logTimeTickScheduled.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Time tick scheduledAt=" + DozeLoggerKt.getDATE_FORMAT().format(new Date(receiver.getLong1())) + " triggerAt=" + DozeLoggerKt.getDATE_FORMAT().format(new Date(receiver.getLong2()));
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00752);
        logMessageImplObtain.setLong1(j);
        logMessageImplObtain.setLong2(j2);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logKeyguardVisibilityChange(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00632 c00632 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logKeyguardVisibilityChange.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Keyguard visibility change, isShowing=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00632);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logDozeStateChanged(@NotNull DozeMachine.State state) {
        Intrinsics.checkParameterIsNotNull(state, "state");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logDozeStateChanged.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Doze state changed to " + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, anonymousClass2);
        logMessageImplObtain.setStr1(state.name());
        logBuffer.push(logMessageImplObtain);
    }

    public final void logWakeDisplay(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00762 c00762 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logWakeDisplay.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Display wakefulness changed, isAwake=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00762);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logProximityResult(boolean z, long j, int i) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00672 c00672 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logProximityResult.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Proximity result reason=" + DozeLog.reasonToString(receiver.getInt1()) + " near=" + receiver.getBool1() + " millis=" + receiver.getLong1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00672);
        logMessageImplObtain.setBool1(z);
        logMessageImplObtain.setLong1(j);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPulseDropped(boolean z, @NotNull DozeMachine.State state, boolean z2) {
        Intrinsics.checkParameterIsNotNull(state, "state");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00682 c00682 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logPulseDropped.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Pulse dropped, pulsePending=" + receiver.getBool1() + " state=" + receiver.getStr1() + " blocked=" + receiver.getBool2();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00682);
        logMessageImplObtain.setBool1(z);
        logMessageImplObtain.setStr1(state.name());
        logMessageImplObtain.setBool2(z2);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPulseDropped(@NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        AnonymousClass4 anonymousClass4 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logPulseDropped.4
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Pulse dropped, why=" + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, anonymousClass4);
        logMessageImplObtain.setStr1(reason);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPulseTouchDisabledByProx(boolean z) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00712 c00712 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logPulseTouchDisabledByProx.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Pulse touch modified by prox, disabled=" + receiver.getBool1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00712);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logSensorTriggered(int i) {
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        C00742 c00742 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logSensorTriggered.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Sensor triggered, type=" + DozeLog.reasonToString(receiver.getInt1());
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00742);
        logMessageImplObtain.setInt1(i);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logDozeSuppressed(@NotNull DozeMachine.State state) {
        Intrinsics.checkParameterIsNotNull(state, "state");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        C00582 c00582 = new Function1<LogMessage, String>() { // from class: com.android.systemui.doze.DozeLogger.logDozeSuppressed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Doze state suppressed, state=" + receiver.getStr1();
            }
        };
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("DozeLog", logLevel, c00582);
        logMessageImplObtain.setStr1(state.name());
        logBuffer.push(logMessageImplObtain);
    }
}
