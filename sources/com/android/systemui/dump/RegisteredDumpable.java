package com.android.systemui.dump;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DumpManager.kt */
/* loaded from: classes.dex */
final class RegisteredDumpable<T> {
    private final T dumpable;

    @NotNull
    private final String name;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegisteredDumpable)) {
            return false;
        }
        RegisteredDumpable registeredDumpable = (RegisteredDumpable) obj;
        return Intrinsics.areEqual(this.name, registeredDumpable.name) && Intrinsics.areEqual(this.dumpable, registeredDumpable.dumpable);
    }

    public int hashCode() {
        String str = this.name;
        int iHashCode = (str != null ? str.hashCode() : 0) * 31;
        T t = this.dumpable;
        return iHashCode + (t != null ? t.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "RegisteredDumpable(name=" + this.name + ", dumpable=" + this.dumpable + ")";
    }

    public RegisteredDumpable(@NotNull String name, T t) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        this.name = name;
        this.dumpable = t;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final T getDumpable() {
        return this.dumpable;
    }
}
