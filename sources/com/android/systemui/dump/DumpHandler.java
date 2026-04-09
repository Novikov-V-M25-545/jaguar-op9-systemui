package com.android.systemui.dump;

import android.content.Context;
import android.content.res.Resources;
import com.android.systemui.R;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DumpHandler.kt */
/* loaded from: classes.dex */
public final class DumpHandler {
    public static final Companion Companion = new Companion(null);
    private final Context context;
    private final DumpManager dumpManager;
    private final LogBufferEulogizer logBufferEulogizer;

    public DumpHandler(@NotNull Context context, @NotNull DumpManager dumpManager, @NotNull LogBufferEulogizer logBufferEulogizer) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(logBufferEulogizer, "logBufferEulogizer");
        this.context = context;
        this.dumpManager = dumpManager;
        this.logBufferEulogizer = logBufferEulogizer;
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x004a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final void dump(@org.jetbrains.annotations.NotNull java.io.FileDescriptor r6, @org.jetbrains.annotations.NotNull java.io.PrintWriter r7, @org.jetbrains.annotations.NotNull java.lang.String[] r8) throws java.lang.Exception {
        /*
            r5 = this;
            java.lang.String r0 = "fd"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r6, r0)
            java.lang.String r0 = "pw"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r7, r0)
            java.lang.String r0 = "args"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r8, r0)
            java.lang.String r0 = "DumpManager#dump()"
            android.os.Trace.beginSection(r0)
            long r0 = android.os.SystemClock.uptimeMillis()
            com.android.systemui.dump.ParsedArgs r8 = r5.parseArgs(r8)     // Catch: com.android.systemui.dump.ArgParseException -> L72
            java.lang.String r2 = r8.getDumpPriority()
            if (r2 != 0) goto L23
            goto L4a
        L23:
            int r3 = r2.hashCode()
            r4 = -1986416409(0xffffffff8999b0e7, float:-3.699977E-33)
            if (r3 == r4) goto L3e
            r4 = -1560189025(0xffffffffa301679f, float:-7.015047E-18)
            if (r3 == r4) goto L32
            goto L4a
        L32:
            java.lang.String r3 = "CRITICAL"
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L4a
            r5.dumpCritical(r6, r7, r8)
            goto L4d
        L3e:
            java.lang.String r3 = "NORMAL"
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L4a
            r5.dumpNormal(r7, r8)
            goto L4d
        L4a:
            r5.dumpParameterized(r6, r7, r8)
        L4d:
            r7.println()
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Dump took "
            r5.append(r6)
            long r2 = android.os.SystemClock.uptimeMillis()
            long r2 = r2 - r0
            r5.append(r2)
            java.lang.String r6 = "ms"
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            r7.println(r5)
            android.os.Trace.endSection()
            return
        L72:
            r5 = move-exception
            java.lang.String r5 = r5.getMessage()
            r7.println(r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.DumpHandler.dump(java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue
    java.lang.NullPointerException: Cannot invoke "java.util.List.iterator()" because the return value of "jadx.core.dex.visitors.regions.SwitchOverStringVisitor$SwitchData.getNewCases()" is null
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.restoreSwitchOverString(SwitchOverStringVisitor.java:109)
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.visitRegion(SwitchOverStringVisitor.java:66)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:77)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:82)
     */
    private final void dumpParameterized(FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) throws Exception {
        String command = parsedArgs.getCommand();
        if (command != null) {
            switch (command.hashCode()) {
                case -1354792126:
                    if (command.equals("config")) {
                        dumpConfig(printWriter);
                        return;
                    }
                    break;
                case -1353714459:
                    if (command.equals("dumpables")) {
                        dumpDumpables(fileDescriptor, printWriter, parsedArgs);
                        return;
                    }
                    break;
                case -1045369428:
                    if (command.equals("bugreport-normal")) {
                        dumpNormal(printWriter, parsedArgs);
                        return;
                    }
                    break;
                case 3198785:
                    if (command.equals("help")) {
                        dumpHelp(printWriter);
                        return;
                    }
                    break;
                case 227996723:
                    if (command.equals("buffers")) {
                        dumpBuffers(printWriter, parsedArgs);
                        return;
                    }
                    break;
                case 842828580:
                    if (command.equals("bugreport-critical")) {
                        dumpCritical(fileDescriptor, printWriter, parsedArgs);
                        return;
                    }
                    break;
            }
        }
        dumpTargets(parsedArgs.getNonFlagArgs(), fileDescriptor, printWriter, parsedArgs);
    }

    private final void dumpCritical(FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) throws Resources.NotFoundException {
        this.dumpManager.dumpDumpables(fileDescriptor, printWriter, parsedArgs.getRawArgs());
        dumpConfig(printWriter);
    }

    private final void dumpNormal(PrintWriter printWriter, ParsedArgs parsedArgs) throws Exception {
        this.dumpManager.dumpBuffers(printWriter, parsedArgs.getTailLength());
        this.logBufferEulogizer.readEulogyIfPresent(printWriter);
    }

    private final void dumpDumpables(FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) {
        if (parsedArgs.getListOnly()) {
            this.dumpManager.listDumpables(printWriter);
        } else {
            this.dumpManager.dumpDumpables(fileDescriptor, printWriter, parsedArgs.getRawArgs());
        }
    }

    private final void dumpBuffers(PrintWriter printWriter, ParsedArgs parsedArgs) {
        if (parsedArgs.getListOnly()) {
            this.dumpManager.listBuffers(printWriter);
        } else {
            this.dumpManager.dumpBuffers(printWriter, parsedArgs.getTailLength());
        }
    }

    private final void dumpTargets(List<String> list, FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) {
        if (!list.isEmpty()) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                this.dumpManager.dumpTarget(it.next(), fileDescriptor, printWriter, parsedArgs.getRawArgs(), parsedArgs.getTailLength());
            }
            return;
        }
        if (parsedArgs.getListOnly()) {
            printWriter.println("Dumpables:");
            this.dumpManager.listDumpables(printWriter);
            printWriter.println();
            printWriter.println("Buffers:");
            this.dumpManager.listBuffers(printWriter);
            return;
        }
        printWriter.println("Nothing to dump :(");
    }

    private final void dumpConfig(PrintWriter printWriter) throws Resources.NotFoundException {
        printWriter.println("SystemUiServiceComponents configuration:");
        printWriter.print("vendor component: ");
        printWriter.println(this.context.getResources().getString(R.string.config_systemUIVendorServiceComponent));
        dumpServiceList(printWriter, "global", R.array.config_systemUIServiceComponents);
        dumpServiceList(printWriter, "per-user", R.array.config_systemUIServiceComponentsPerUser);
    }

    private final void dumpServiceList(PrintWriter printWriter, String str, int i) throws Resources.NotFoundException {
        String[] stringArray = this.context.getResources().getStringArray(i);
        printWriter.print(str);
        printWriter.print(": ");
        if (stringArray == null) {
            printWriter.println("N/A");
            return;
        }
        printWriter.print(stringArray.length);
        printWriter.println(" services");
        int length = stringArray.length;
        for (int i2 = 0; i2 < length; i2++) {
            printWriter.print("  ");
            printWriter.print(i2);
            printWriter.print(": ");
            printWriter.println(stringArray[i2]);
        }
    }

    private final void dumpHelp(PrintWriter printWriter) {
        printWriter.println("Let <invocation> be:");
        printWriter.println("$ adb shell dumpsys activity service com.android.systemui/.SystemUIService");
        printWriter.println();
        printWriter.println("Most common usage:");
        printWriter.println("$ <invocation> <targets>");
        printWriter.println("$ <invocation> NotifLog");
        printWriter.println("$ <invocation> StatusBar FalsingManager BootCompleteCacheImpl");
        printWriter.println("etc.");
        printWriter.println();
        printWriter.println("Special commands:");
        printWriter.println("$ <invocation> dumpables");
        printWriter.println("$ <invocation> buffers");
        printWriter.println("$ <invocation> bugreport-critical");
        printWriter.println("$ <invocation> bugreport-normal");
        printWriter.println();
        printWriter.println("Targets can be listed:");
        printWriter.println("$ <invocation> --list");
        printWriter.println("$ <invocation> dumpables --list");
        printWriter.println("$ <invocation> buffers --list");
        printWriter.println();
        printWriter.println("Show only the most recent N lines of buffers");
        printWriter.println("$ <invocation> NotifLog --tail 30");
    }

    private final ParsedArgs parseArgs(String[] strArr) throws ArgParseException {
        List mutableList = ArraysKt___ArraysKt.toMutableList(strArr);
        ParsedArgs parsedArgs = new ParsedArgs(strArr, mutableList);
        Iterator<String> it = mutableList.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (StringsKt__StringsJVMKt.startsWith$default(next, "-", false, 2, null)) {
                it.remove();
                switch (next.hashCode()) {
                    case 1499:
                        if (!next.equals("-h")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setCommand("help");
                        break;
                    case 1503:
                        if (!next.equals("-l")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setListOnly(true);
                        break;
                    case 1511:
                        if (!next.equals("-t")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setTailLength(((Number) readArgument(it, next, new Function1<String, Integer>() { // from class: com.android.systemui.dump.DumpHandler.parseArgs.2
                            @Override // kotlin.jvm.functions.Function1
                            public /* bridge */ /* synthetic */ Integer invoke(String str) {
                                return Integer.valueOf(invoke2(str));
                            }

                            /* renamed from: invoke, reason: avoid collision after fix types in other method */
                            public final int invoke2(@NotNull String it2) {
                                Intrinsics.checkParameterIsNotNull(it2, "it");
                                return Integer.parseInt(it2);
                            }
                        })).intValue());
                        break;
                    case 1056887741:
                        if (next.equals("--dump-priority")) {
                            parsedArgs.setDumpPriority((String) readArgument(it, "--dump-priority", new Function1<String, String>() { // from class: com.android.systemui.dump.DumpHandler.parseArgs.1
                                @Override // kotlin.jvm.functions.Function1
                                @Nullable
                                public final String invoke(@NotNull String it2) {
                                    Intrinsics.checkParameterIsNotNull(it2, "it");
                                    if (ArraysKt___ArraysKt.contains(DumpHandlerKt.PRIORITY_OPTIONS, it2)) {
                                        return it2;
                                    }
                                    throw new IllegalArgumentException();
                                }
                            }));
                            break;
                        } else {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                    case 1333069025:
                        if (!next.equals("--help")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setCommand("help");
                        break;
                    case 1333192254:
                        if (!next.equals("--list")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setListOnly(true);
                        break;
                    case 1333422576:
                        if (!next.equals("--tail")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setTailLength(((Number) readArgument(it, next, new Function1<String, Integer>() { // from class: com.android.systemui.dump.DumpHandler.parseArgs.2
                            @Override // kotlin.jvm.functions.Function1
                            public /* bridge */ /* synthetic */ Integer invoke(String str) {
                                return Integer.valueOf(invoke2(str));
                            }

                            /* renamed from: invoke, reason: avoid collision after fix types in other method */
                            public final int invoke2(@NotNull String it2) {
                                Intrinsics.checkParameterIsNotNull(it2, "it");
                                return Integer.parseInt(it2);
                            }
                        })).intValue());
                        break;
                    default:
                        throw new ArgParseException("Unknown flag: " + next);
                }
            }
        }
        if (parsedArgs.getCommand() == null && (!mutableList.isEmpty()) && ArraysKt___ArraysKt.contains(DumpHandlerKt.COMMANDS, mutableList.get(0))) {
            parsedArgs.setCommand((String) mutableList.remove(0));
        }
        return parsedArgs;
    }

    private final <T> T readArgument(Iterator<String> it, String str, Function1<? super String, ? extends T> function1) throws ArgParseException {
        if (!it.hasNext()) {
            throw new ArgParseException("Missing argument for " + str);
        }
        String next = it.next();
        try {
            T tInvoke = function1.invoke(next);
            it.remove();
            return tInvoke;
        } catch (Exception unused) {
            throw new ArgParseException("Invalid argument '" + next + "' for flag " + str);
        }
    }

    /* compiled from: DumpHandler.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
