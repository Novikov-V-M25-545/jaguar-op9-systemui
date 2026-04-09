package kotlin.io;

import java.io.Closeable;
import java.io.IOException;
import kotlin.ExceptionsKt__ExceptionsKt;
import org.jetbrains.annotations.Nullable;

/* compiled from: Closeable.kt */
/* loaded from: classes.dex */
public final class CloseableKt {
    public static final void closeFinally(@Nullable Closeable closeable, @Nullable Throwable th) throws IOException {
        if (closeable == null) {
            return;
        }
        if (th == null) {
            closeable.close();
            return;
        }
        try {
            closeable.close();
        } catch (Throwable th2) {
            ExceptionsKt__ExceptionsKt.addSuppressed(th, th2);
        }
    }
}
