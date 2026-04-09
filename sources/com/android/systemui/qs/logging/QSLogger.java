package com.android.systemui.qs.logging;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessage;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.statusbar.StatusBarState;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: QSLogger.kt */
/* loaded from: classes.dex */
public final class QSLogger {
    private final LogBuffer buffer;

    private final String toStateString(int i) {
        return i != 0 ? i != 1 ? i != 2 ? "wrong state" : "active" : "inactive" : "unavailable";
    }

    public QSLogger(@NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    public final void logTileAdded(@NotNull String tileSpec) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        LogLevel logLevel = LogLevel.DEBUG;
        C01082 c01082 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileAdded.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getStr1() + "] Tile added";
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01082);
        logMessageImplObtain.setStr1(tileSpec);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTileDestroyed(@NotNull String tileSpec, @NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        LogLevel logLevel = LogLevel.DEBUG;
        C01112 c01112 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileDestroyed.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getStr1() + "] Tile destroyed. Reason: " + receiver.getStr2();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01112);
        logMessageImplObtain.setStr1(tileSpec);
        logMessageImplObtain.setStr2(reason);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTileChangeListening(@NotNull String tileSpec, boolean z) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        LogLevel logLevel = LogLevel.VERBOSE;
        C01092 c01092 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileChangeListening.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getStr1() + "] Tile listening=" + receiver.getBool1();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01092);
        logMessageImplObtain.setBool1(z);
        logMessageImplObtain.setStr1(tileSpec);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logAllTilesChangeListening(boolean z, @NotNull String containerName, @NotNull String allSpecs) {
        Intrinsics.checkParameterIsNotNull(containerName, "containerName");
        Intrinsics.checkParameterIsNotNull(allSpecs, "allSpecs");
        LogLevel logLevel = LogLevel.DEBUG;
        AnonymousClass2 anonymousClass2 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logAllTilesChangeListening.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return "Tiles listening=" + receiver.getBool1() + " in " + receiver.getStr1() + ". " + receiver.getStr2();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, anonymousClass2);
        logMessageImplObtain.setBool1(z);
        logMessageImplObtain.setStr1(containerName);
        logMessageImplObtain.setStr2(allSpecs);
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTileClick(@NotNull String tileSpec, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        LogLevel logLevel = LogLevel.DEBUG;
        C01102 c01102 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileClick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getStr1() + "] Tile clicked. StatusBarState=" + receiver.getStr2() + ". TileState=" + receiver.getStr3();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01102);
        logMessageImplObtain.setStr1(tileSpec);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr2(StatusBarState.toShortString(i));
        logMessageImplObtain.setStr3(toStateString(i2));
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTileSecondaryClick(@NotNull String tileSpec, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        LogLevel logLevel = LogLevel.DEBUG;
        C01132 c01132 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileSecondaryClick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getStr1() + "] Tile long clicked. StatusBarState=" + receiver.getStr2() + ". TileState=" + receiver.getStr3();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01132);
        logMessageImplObtain.setStr1(tileSpec);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr2(StatusBarState.toShortString(i));
        logMessageImplObtain.setStr3(toStateString(i2));
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTileLongClick(@NotNull String tileSpec, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        LogLevel logLevel = LogLevel.DEBUG;
        C01122 c01122 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileLongClick.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return '[' + receiver.getStr1() + "] Tile long clicked. StatusBarState=" + receiver.getStr2() + ". TileState=" + receiver.getStr3();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01122);
        logMessageImplObtain.setStr1(tileSpec);
        logMessageImplObtain.setInt1(i);
        logMessageImplObtain.setStr2(StatusBarState.toShortString(i));
        logMessageImplObtain.setStr3(toStateString(i2));
        logBuffer.push(logMessageImplObtain);
    }

    public final void logTileUpdated(@NotNull String tileSpec, @NotNull QSTile.State state) {
        Intrinsics.checkParameterIsNotNull(tileSpec, "tileSpec");
        Intrinsics.checkParameterIsNotNull(state, "state");
        LogLevel logLevel = LogLevel.VERBOSE;
        C01142 c01142 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logTileUpdated.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                String str;
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                sb.append(receiver.getStr1());
                sb.append("] Tile updated. Label=");
                sb.append(receiver.getStr2());
                sb.append(". State=");
                sb.append(receiver.getInt1());
                sb.append(". Icon=");
                sb.append(receiver.getStr3());
                sb.append('.');
                if (receiver.getBool1()) {
                    str = " Activity in/out=" + receiver.getBool2() + '/' + receiver.getBool3();
                } else {
                    str = "";
                }
                sb.append(str);
                return sb.toString();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01142);
        logMessageImplObtain.setStr1(tileSpec);
        CharSequence charSequence = state.label;
        logMessageImplObtain.setStr2(charSequence != null ? charSequence.toString() : null);
        QSTile.Icon icon = state.icon;
        logMessageImplObtain.setStr3(icon != null ? icon.toString() : null);
        logMessageImplObtain.setInt1(state.state);
        if (state instanceof QSTile.SignalState) {
            logMessageImplObtain.setBool1(true);
            QSTile.SignalState signalState = (QSTile.SignalState) state;
            logMessageImplObtain.setBool2(signalState.activityIn);
            logMessageImplObtain.setBool3(signalState.activityOut);
        }
        logBuffer.push(logMessageImplObtain);
    }

    public final void logPanelExpanded(boolean z, @NotNull String containerName) {
        Intrinsics.checkParameterIsNotNull(containerName, "containerName");
        LogLevel logLevel = LogLevel.DEBUG;
        C01072 c01072 = new Function1<LogMessage, String>() { // from class: com.android.systemui.qs.logging.QSLogger.logPanelExpanded.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final String invoke(@NotNull LogMessage receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                return receiver.getStr1() + " expanded=" + receiver.getBool1();
            }
        };
        LogBuffer logBuffer = this.buffer;
        if (logBuffer.getFrozen()) {
            return;
        }
        LogMessageImpl logMessageImplObtain = logBuffer.obtain("QSLog", logLevel, c01072);
        logMessageImplObtain.setStr1(containerName);
        logMessageImplObtain.setBool1(z);
        logBuffer.push(logMessageImplObtain);
    }
}
