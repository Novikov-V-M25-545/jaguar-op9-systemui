package kotlin.jdk7;

import org.jetbrains.annotations.Nullable;

/* compiled from: AutoCloseable.kt */
/* loaded from: classes.dex */
public final class AutoCloseableKt {
    public static final void closeFinally(@Nullable AutoCloseable autoCloseable, @Nullable Throwable th) throws Exception {
        if (autoCloseable == null) {
            return;
        }
        if (th == null) {
            autoCloseable.close();
            return;
        }
        try {
            autoCloseable.close();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }
}
