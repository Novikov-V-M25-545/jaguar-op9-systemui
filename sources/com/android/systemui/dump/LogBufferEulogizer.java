package com.android.systemui.dump;

import android.content.Context;
import android.util.Log;
import com.android.systemui.util.io.Files;
import com.android.systemui.util.time.SystemClock;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jdk7.AutoCloseableKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogBufferEulogizer.kt */
/* loaded from: classes.dex */
public final class LogBufferEulogizer {
    private final DumpManager dumpManager;
    private final Files files;
    private final Path logPath;
    private final long maxLogAgeToDump;
    private final long minWriteGap;
    private final SystemClock systemClock;

    public LogBufferEulogizer(@NotNull DumpManager dumpManager, @NotNull SystemClock systemClock, @NotNull Files files, @NotNull Path logPath, long j, long j2) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(systemClock, "systemClock");
        Intrinsics.checkParameterIsNotNull(files, "files");
        Intrinsics.checkParameterIsNotNull(logPath, "logPath");
        this.dumpManager = dumpManager;
        this.systemClock = systemClock;
        this.files = files;
        this.logPath = logPath;
        this.minWriteGap = j;
        this.maxLogAgeToDump = j2;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public LogBufferEulogizer(@NotNull Context context, @NotNull DumpManager dumpManager, @NotNull SystemClock systemClock, @NotNull Files files) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(systemClock, "systemClock");
        Intrinsics.checkParameterIsNotNull(files, "files");
        Path path = Paths.get(context.getFilesDir().toPath().toString(), "log_buffers.txt");
        Intrinsics.checkExpressionValueIsNotNull(path, "Paths.get(context.filesD…ing(), \"log_buffers.txt\")");
        this(dumpManager, systemClock, files, path, LogBufferEulogizerKt.MIN_WRITE_GAP, LogBufferEulogizerKt.MAX_AGE_TO_DUMP);
    }

    @NotNull
    public final <T extends Exception> T record(@NotNull T reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        long jUptimeMillis = this.systemClock.uptimeMillis();
        Log.i("BufferEulogizer", "Performing emergency dump of log buffers");
        long millisSinceLastWrite = getMillisSinceLastWrite(this.logPath);
        if (millisSinceLastWrite < this.minWriteGap) {
            Log.w("BufferEulogizer", "Cannot dump logs, last write was only " + millisSinceLastWrite + " ms ago");
            return reason;
        }
        long jUptimeMillis2 = 0;
        try {
            BufferedWriter bufferedWriterNewBufferedWriter = this.files.newBufferedWriter(this.logPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                PrintWriter printWriter = new PrintWriter(bufferedWriterNewBufferedWriter);
                printWriter.println(LogBufferEulogizerKt.DATE_FORMAT.format(Long.valueOf(this.systemClock.currentTimeMillis())));
                printWriter.println();
                printWriter.println("Dump triggered by exception:");
                reason.printStackTrace(printWriter);
                this.dumpManager.dumpBuffers(printWriter, 0);
                jUptimeMillis2 = this.systemClock.uptimeMillis() - jUptimeMillis;
                printWriter.println();
                printWriter.println("Buffer eulogy took " + jUptimeMillis2 + "ms");
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(bufferedWriterNewBufferedWriter, null);
            } finally {
            }
        } catch (Exception e) {
            Log.e("BufferEulogizer", "Exception while attempting to dump buffers, bailing", e);
        }
        Log.i("BufferEulogizer", "Buffer eulogy took " + jUptimeMillis2 + "ms");
        return reason;
    }

    public final void readEulogyIfPresent(@NotNull final PrintWriter pw) throws Exception {
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        try {
            long millisSinceLastWrite = getMillisSinceLastWrite(this.logPath);
            if (millisSinceLastWrite > this.maxLogAgeToDump) {
                Log.i("BufferEulogizer", "Not eulogizing buffers; they are " + TimeUnit.HOURS.convert(millisSinceLastWrite, TimeUnit.MILLISECONDS) + " hours old");
                return;
            }
            Stream<String> streamLines = this.files.lines(this.logPath);
            try {
                pw.println();
                pw.println();
                pw.println("=============== BUFFERS FROM MOST RECENT CRASH ===============");
                streamLines.forEach(new Consumer<String>() { // from class: com.android.systemui.dump.LogBufferEulogizer$readEulogyIfPresent$$inlined$use$lambda$1
                    @Override // java.util.function.Consumer
                    public final void accept(String str) {
                        pw.println(str);
                    }
                });
                Unit unit = Unit.INSTANCE;
                AutoCloseableKt.closeFinally(streamLines, null);
            } finally {
            }
        } catch (IOException unused) {
        } catch (UncheckedIOException e) {
            Log.e("BufferEulogizer", "UncheckedIOException while dumping the core", e);
        }
    }

    private final long getMillisSinceLastWrite(Path path) {
        BasicFileAttributes attributes;
        FileTime fileTimeLastModifiedTime;
        try {
            attributes = this.files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
        } catch (IOException unused) {
            attributes = null;
        }
        return this.systemClock.currentTimeMillis() - ((attributes == null || (fileTimeLastModifiedTime = attributes.lastModifiedTime()) == null) ? 0L : fileTimeLastModifiedTime.toMillis());
    }
}
