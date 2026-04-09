package com.android.systemui.log;

/* compiled from: LogLevel.kt */
/* loaded from: classes.dex */
public enum LogLevel {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARNING(5),
    ERROR(6),
    WTF(7);

    private final int nativeLevel;

    LogLevel(int i) {
        this.nativeLevel = i;
    }
}
