package kotlinx.coroutines;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ResumeMode.kt */
/* loaded from: classes2.dex */
public final class ResumeModeKt {
    public static final boolean isCancellableMode(int i) {
        return i == 1;
    }

    public static final boolean isDispatchedMode(int i) {
        return i == 0 || i == 1;
    }

    public static final <T> void resumeMode(@NotNull Continuation<? super T> resumeMode, T t, int i) {
        Intrinsics.checkParameterIsNotNull(resumeMode, "$this$resumeMode");
        if (i == 0) {
            Result.Companion companion = Result.Companion;
            resumeMode.resumeWith(Result.m407constructorimpl(t));
            return;
        }
        if (i == 1) {
            DispatchedKt.resumeCancellable(resumeMode, t);
            return;
        }
        if (i == 2) {
            DispatchedKt.resumeDirect(resumeMode, t);
            return;
        }
        if (i != 3) {
            if (i == 4) {
                return;
            }
            throw new IllegalStateException(("Invalid mode " + i).toString());
        }
        DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) resumeMode;
        CoroutineContext context = dispatchedContinuation.getContext();
        Object objUpdateThreadContext = ThreadContextKt.updateThreadContext(context, dispatchedContinuation.countOrElement);
        try {
            Continuation<T> continuation = dispatchedContinuation.continuation;
            Result.Companion companion2 = Result.Companion;
            continuation.resumeWith(Result.m407constructorimpl(t));
            Unit unit = Unit.INSTANCE;
        } finally {
            ThreadContextKt.restoreThreadContext(context, objUpdateThreadContext);
        }
    }

    public static final <T> void resumeWithExceptionMode(@NotNull Continuation<? super T> resumeWithExceptionMode, @NotNull Throwable exception, int i) {
        Intrinsics.checkParameterIsNotNull(resumeWithExceptionMode, "$this$resumeWithExceptionMode");
        Intrinsics.checkParameterIsNotNull(exception, "exception");
        if (i == 0) {
            Result.Companion companion = Result.Companion;
            resumeWithExceptionMode.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(exception)));
            return;
        }
        if (i == 1) {
            DispatchedKt.resumeCancellableWithException(resumeWithExceptionMode, exception);
            return;
        }
        if (i == 2) {
            DispatchedKt.resumeDirectWithException(resumeWithExceptionMode, exception);
            return;
        }
        if (i != 3) {
            if (i == 4) {
                return;
            }
            throw new IllegalStateException(("Invalid mode " + i).toString());
        }
        DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) resumeWithExceptionMode;
        CoroutineContext context = dispatchedContinuation.getContext();
        Object objUpdateThreadContext = ThreadContextKt.updateThreadContext(context, dispatchedContinuation.countOrElement);
        try {
            Continuation<T> continuation = dispatchedContinuation.continuation;
            Result.Companion companion2 = Result.Companion;
            continuation.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(exception, continuation))));
            Unit unit = Unit.INSTANCE;
        } finally {
            ThreadContextKt.restoreThreadContext(context, objUpdateThreadContext);
        }
    }
}
