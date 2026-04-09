package com.android.systemui.log;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: LogMessageImpl.kt */
/* loaded from: classes.dex */
public final class LogMessageImpl implements LogMessage {
    public static final Factory Factory = new Factory(null);
    private boolean bool1;
    private boolean bool2;
    private boolean bool3;
    private boolean bool4;
    private double double1;
    private int int1;
    private int int2;

    @NotNull
    private LogLevel level;
    private long long1;
    private long long2;

    @NotNull
    private Function1<? super LogMessage, String> printer;

    @Nullable
    private String str1;

    @Nullable
    private String str2;

    @Nullable
    private String str3;

    @NotNull
    private String tag;
    private long timestamp;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LogMessageImpl)) {
            return false;
        }
        LogMessageImpl logMessageImpl = (LogMessageImpl) obj;
        return Intrinsics.areEqual(getLevel(), logMessageImpl.getLevel()) && Intrinsics.areEqual(getTag(), logMessageImpl.getTag()) && getTimestamp() == logMessageImpl.getTimestamp() && Intrinsics.areEqual(getPrinter(), logMessageImpl.getPrinter()) && Intrinsics.areEqual(getStr1(), logMessageImpl.getStr1()) && Intrinsics.areEqual(getStr2(), logMessageImpl.getStr2()) && Intrinsics.areEqual(getStr3(), logMessageImpl.getStr3()) && getInt1() == logMessageImpl.getInt1() && getInt2() == logMessageImpl.getInt2() && getLong1() == logMessageImpl.getLong1() && getLong2() == logMessageImpl.getLong2() && Double.compare(getDouble1(), logMessageImpl.getDouble1()) == 0 && getBool1() == logMessageImpl.getBool1() && getBool2() == logMessageImpl.getBool2() && getBool3() == logMessageImpl.getBool3() && getBool4() == logMessageImpl.getBool4();
    }

    public int hashCode() {
        LogLevel level = getLevel();
        int iHashCode = (level != null ? level.hashCode() : 0) * 31;
        String tag = getTag();
        int iHashCode2 = (((iHashCode + (tag != null ? tag.hashCode() : 0)) * 31) + Long.hashCode(getTimestamp())) * 31;
        Function1<LogMessage, String> printer = getPrinter();
        int iHashCode3 = (iHashCode2 + (printer != null ? printer.hashCode() : 0)) * 31;
        String str1 = getStr1();
        int iHashCode4 = (iHashCode3 + (str1 != null ? str1.hashCode() : 0)) * 31;
        String str2 = getStr2();
        int iHashCode5 = (iHashCode4 + (str2 != null ? str2.hashCode() : 0)) * 31;
        String str3 = getStr3();
        int iHashCode6 = (((((((((((iHashCode5 + (str3 != null ? str3.hashCode() : 0)) * 31) + Integer.hashCode(getInt1())) * 31) + Integer.hashCode(getInt2())) * 31) + Long.hashCode(getLong1())) * 31) + Long.hashCode(getLong2())) * 31) + Double.hashCode(getDouble1())) * 31;
        boolean bool1 = getBool1();
        int i = bool1;
        if (bool1) {
            i = 1;
        }
        int i2 = (iHashCode6 + i) * 31;
        boolean bool2 = getBool2();
        int i3 = bool2;
        if (bool2) {
            i3 = 1;
        }
        int i4 = (i2 + i3) * 31;
        boolean bool3 = getBool3();
        int i5 = bool3;
        if (bool3) {
            i5 = 1;
        }
        int i6 = (i4 + i5) * 31;
        boolean bool4 = getBool4();
        return i6 + (bool4 ? 1 : bool4);
    }

    @NotNull
    public String toString() {
        return "LogMessageImpl(level=" + getLevel() + ", tag=" + getTag() + ", timestamp=" + getTimestamp() + ", printer=" + getPrinter() + ", str1=" + getStr1() + ", str2=" + getStr2() + ", str3=" + getStr3() + ", int1=" + getInt1() + ", int2=" + getInt2() + ", long1=" + getLong1() + ", long2=" + getLong2() + ", double1=" + getDouble1() + ", bool1=" + getBool1() + ", bool2=" + getBool2() + ", bool3=" + getBool3() + ", bool4=" + getBool4() + ")";
    }

    public LogMessageImpl(@NotNull LogLevel level, @NotNull String tag, long j, @NotNull Function1<? super LogMessage, String> printer, @Nullable String str, @Nullable String str2, @Nullable String str3, int i, int i2, long j2, long j3, double d, boolean z, boolean z2, boolean z3, boolean z4) {
        Intrinsics.checkParameterIsNotNull(level, "level");
        Intrinsics.checkParameterIsNotNull(tag, "tag");
        Intrinsics.checkParameterIsNotNull(printer, "printer");
        this.level = level;
        this.tag = tag;
        this.timestamp = j;
        this.printer = printer;
        this.str1 = str;
        this.str2 = str2;
        this.str3 = str3;
        this.int1 = i;
        this.int2 = i2;
        this.long1 = j2;
        this.long2 = j3;
        this.double1 = d;
        this.bool1 = z;
        this.bool2 = z2;
        this.bool3 = z3;
        this.bool4 = z4;
    }

    @Override // com.android.systemui.log.LogMessage
    @NotNull
    public LogLevel getLevel() {
        return this.level;
    }

    public void setLevel(@NotNull LogLevel logLevel) {
        Intrinsics.checkParameterIsNotNull(logLevel, "<set-?>");
        this.level = logLevel;
    }

    @Override // com.android.systemui.log.LogMessage
    @NotNull
    public String getTag() {
        return this.tag;
    }

    public void setTag(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "<set-?>");
        this.tag = str;
    }

    @Override // com.android.systemui.log.LogMessage
    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long j) {
        this.timestamp = j;
    }

    @Override // com.android.systemui.log.LogMessage
    @NotNull
    public Function1<LogMessage, String> getPrinter() {
        return this.printer;
    }

    public void setPrinter(@NotNull Function1<? super LogMessage, String> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "<set-?>");
        this.printer = function1;
    }

    @Override // com.android.systemui.log.LogMessage
    @Nullable
    public String getStr1() {
        return this.str1;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setStr1(@Nullable String str) {
        this.str1 = str;
    }

    @Override // com.android.systemui.log.LogMessage
    @Nullable
    public String getStr2() {
        return this.str2;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setStr2(@Nullable String str) {
        this.str2 = str;
    }

    @Override // com.android.systemui.log.LogMessage
    @Nullable
    public String getStr3() {
        return this.str3;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setStr3(@Nullable String str) {
        this.str3 = str;
    }

    @Override // com.android.systemui.log.LogMessage
    public int getInt1() {
        return this.int1;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setInt1(int i) {
        this.int1 = i;
    }

    @Override // com.android.systemui.log.LogMessage
    public int getInt2() {
        return this.int2;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setInt2(int i) {
        this.int2 = i;
    }

    @Override // com.android.systemui.log.LogMessage
    public long getLong1() {
        return this.long1;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setLong1(long j) {
        this.long1 = j;
    }

    @Override // com.android.systemui.log.LogMessage
    public long getLong2() {
        return this.long2;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setLong2(long j) {
        this.long2 = j;
    }

    public double getDouble1() {
        return this.double1;
    }

    public void setDouble1(double d) {
        this.double1 = d;
    }

    @Override // com.android.systemui.log.LogMessage
    public boolean getBool1() {
        return this.bool1;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setBool1(boolean z) {
        this.bool1 = z;
    }

    @Override // com.android.systemui.log.LogMessage
    public boolean getBool2() {
        return this.bool2;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setBool2(boolean z) {
        this.bool2 = z;
    }

    @Override // com.android.systemui.log.LogMessage
    public boolean getBool3() {
        return this.bool3;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setBool3(boolean z) {
        this.bool3 = z;
    }

    @Override // com.android.systemui.log.LogMessage
    public boolean getBool4() {
        return this.bool4;
    }

    @Override // com.android.systemui.log.LogMessage
    public void setBool4(boolean z) {
        this.bool4 = z;
    }

    public final void reset(@NotNull String tag, @NotNull LogLevel level, long j, @NotNull Function1<? super LogMessage, String> renderer) {
        Intrinsics.checkParameterIsNotNull(tag, "tag");
        Intrinsics.checkParameterIsNotNull(level, "level");
        Intrinsics.checkParameterIsNotNull(renderer, "renderer");
        setLevel(level);
        setTag(tag);
        setTimestamp(j);
        setPrinter(renderer);
        setStr1(null);
        setStr2(null);
        setStr3(null);
        setInt1(0);
        setInt2(0);
        setLong1(0L);
        setLong2(0L);
        setDouble1(0.0d);
        setBool1(false);
        setBool2(false);
        setBool3(false);
        setBool4(false);
    }

    /* compiled from: LogMessageImpl.kt */
    public static final class Factory {
        private Factory() {
        }

        public /* synthetic */ Factory(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final LogMessageImpl create() {
            return new LogMessageImpl(LogLevel.DEBUG, "UnknownTag", 0L, LogMessageImplKt.DEFAULT_RENDERER, null, null, null, 0, 0, 0L, 0L, 0.0d, false, false, false, false);
        }
    }
}
