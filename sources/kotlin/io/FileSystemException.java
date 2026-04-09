package kotlin.io;

import java.io.File;
import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Exceptions.kt */
/* loaded from: classes.dex */
public class FileSystemException extends IOException {

    @NotNull
    private final File file;

    @Nullable
    private final File other;

    @Nullable
    private final String reason;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public FileSystemException(@NotNull File file, @Nullable File file2, @Nullable String str) {
        super(ExceptionsKt.constructMessage(file, file2, str));
        Intrinsics.checkParameterIsNotNull(file, "file");
        this.file = file;
        this.other = file2;
        this.reason = str;
    }
}
