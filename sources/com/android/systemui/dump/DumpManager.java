package com.android.systemui.dump;

import android.util.ArrayMap;
import com.android.systemui.Dumpable;
import com.android.systemui.log.LogBuffer;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: DumpManager.kt */
/* loaded from: classes.dex */
public final class DumpManager {
    private final Map<String, RegisteredDumpable<Dumpable>> dumpables = new ArrayMap();
    private final Map<String, RegisteredDumpable<LogBuffer>> buffers = new ArrayMap();

    public final synchronized void registerDumpable(@NotNull String name, @NotNull Dumpable module) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        Intrinsics.checkParameterIsNotNull(module, "module");
        if (!canAssignToNameLocked(name, module)) {
            throw new IllegalArgumentException('\'' + name + "' is already registered");
        }
        this.dumpables.put(name, new RegisteredDumpable<>(name, module));
    }

    public final synchronized void unregisterDumpable(@NotNull String name) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        this.dumpables.remove(name);
    }

    public final synchronized void registerBuffer(@NotNull String name, @NotNull LogBuffer buffer) {
        Intrinsics.checkParameterIsNotNull(name, "name");
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        if (!canAssignToNameLocked(name, buffer)) {
            throw new IllegalArgumentException('\'' + name + "' is already registered");
        }
        this.buffers.put(name, new RegisteredDumpable<>(name, buffer));
    }

    public final synchronized void dumpTarget(@NotNull String target, @NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args, int i) {
        Intrinsics.checkParameterIsNotNull(target, "target");
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        for (RegisteredDumpable<Dumpable> registeredDumpable : this.dumpables.values()) {
            if (StringsKt__StringsJVMKt.endsWith$default(registeredDumpable.getName(), target, false, 2, null)) {
                dumpDumpable(registeredDumpable, fd, pw, args);
                return;
            }
        }
        for (RegisteredDumpable<LogBuffer> registeredDumpable2 : this.buffers.values()) {
            if (StringsKt__StringsJVMKt.endsWith$default(registeredDumpable2.getName(), target, false, 2, null)) {
                dumpBuffer(registeredDumpable2, pw, i);
                return;
            }
        }
    }

    public final synchronized void dumpDumpables(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        Iterator<RegisteredDumpable<Dumpable>> it = this.dumpables.values().iterator();
        while (it.hasNext()) {
            dumpDumpable(it.next(), fd, pw, args);
        }
    }

    public final synchronized void listDumpables(@NotNull PrintWriter pw) {
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Iterator<RegisteredDumpable<Dumpable>> it = this.dumpables.values().iterator();
        while (it.hasNext()) {
            pw.println(it.next().getName());
        }
    }

    public final synchronized void dumpBuffers(@NotNull PrintWriter pw, int i) {
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Iterator<RegisteredDumpable<LogBuffer>> it = this.buffers.values().iterator();
        while (it.hasNext()) {
            dumpBuffer(it.next(), pw, i);
        }
    }

    public final synchronized void listBuffers(@NotNull PrintWriter pw) {
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Iterator<RegisteredDumpable<LogBuffer>> it = this.buffers.values().iterator();
        while (it.hasNext()) {
            pw.println(it.next().getName());
        }
    }

    public final synchronized void freezeBuffers() {
        Iterator<RegisteredDumpable<LogBuffer>> it = this.buffers.values().iterator();
        while (it.hasNext()) {
            it.next().getDumpable().freeze();
        }
    }

    public final synchronized void unfreezeBuffers() {
        Iterator<RegisteredDumpable<LogBuffer>> it = this.buffers.values().iterator();
        while (it.hasNext()) {
            it.next().getDumpable().unfreeze();
        }
    }

    private final void dumpDumpable(RegisteredDumpable<Dumpable> registeredDumpable, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println();
        printWriter.println(registeredDumpable.getName() + ':');
        printWriter.println("----------------------------------------------------------------------------");
        registeredDumpable.getDumpable().dump(fileDescriptor, printWriter, strArr);
    }

    private final void dumpBuffer(RegisteredDumpable<LogBuffer> registeredDumpable, PrintWriter printWriter, int i) {
        printWriter.println();
        printWriter.println();
        printWriter.println("BUFFER " + registeredDumpable.getName() + ':');
        printWriter.println("============================================================================");
        registeredDumpable.getDumpable().dump(printWriter, i);
    }

    private final boolean canAssignToNameLocked(String str, Object obj) {
        LogBuffer dumpable;
        RegisteredDumpable<Dumpable> registeredDumpable = this.dumpables.get(str);
        if (registeredDumpable == null || (dumpable = registeredDumpable.getDumpable()) == null) {
            RegisteredDumpable<LogBuffer> registeredDumpable2 = this.buffers.get(str);
            dumpable = registeredDumpable2 != null ? registeredDumpable2.getDumpable() : null;
        }
        return dumpable == null || Intrinsics.areEqual(obj, dumpable);
    }
}
