package kotlinx.coroutines;

import kotlin.ExceptionsKt__ExceptionsKt;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CoroutineExceptionHandler.kt */
/* loaded from: classes2.dex */
public final class CoroutineExceptionHandlerKt {
    public static final void handleCoroutineException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(exception, "exception");
        try {
            CoroutineExceptionHandler coroutineExceptionHandler = (CoroutineExceptionHandler) context.get(CoroutineExceptionHandler.Key);
            if (coroutineExceptionHandler != null) {
                coroutineExceptionHandler.handleException(context, exception);
            } else {
                CoroutineExceptionHandlerImplKt.handleCoroutineExceptionImpl(context, exception);
            }
        } catch (Throwable th) {
            CoroutineExceptionHandlerImplKt.handleCoroutineExceptionImpl(context, handlerException(exception, th));
        }
    }

    @NotNull
    public static final Throwable handlerException(@NotNull Throwable originalException, @NotNull Throwable thrownException) {
        Intrinsics.checkParameterIsNotNull(originalException, "originalException");
        Intrinsics.checkParameterIsNotNull(thrownException, "thrownException");
        if (originalException == thrownException) {
            return originalException;
        }
        RuntimeException runtimeException = new RuntimeException("Exception while trying to handle coroutine exception", thrownException);
        ExceptionsKt__ExceptionsKt.addSuppressed(runtimeException, originalException);
        return runtimeException;
    }
}
