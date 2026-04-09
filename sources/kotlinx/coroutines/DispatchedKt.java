package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import kotlinx.coroutines.internal.Symbol;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: Dispatched.kt */
/* loaded from: classes2.dex */
public final class DispatchedKt {
    private static final Symbol UNDEFINED = new Symbol("UNDEFINED");

    private static final void resumeUnconfined(@NotNull DispatchedTask<?> dispatchedTask) {
        EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
            eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedTask);
            return;
        }
        eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
        try {
            resume(dispatchedTask, dispatchedTask.getDelegate$kotlinx_coroutines_core(), 3);
            do {
            } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
        } finally {
            try {
            } finally {
            }
        }
    }

    public static final <T> void resumeCancellable(@NotNull Continuation<? super T> resumeCancellable, T t) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(resumeCancellable, "$this$resumeCancellable");
        if (resumeCancellable instanceof DispatchedContinuation) {
            DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) resumeCancellable;
            if (!dispatchedContinuation.dispatcher.isDispatchNeeded(dispatchedContinuation.getContext())) {
                EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
                if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
                    dispatchedContinuation._state = t;
                    dispatchedContinuation.resumeMode = 1;
                    eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedContinuation);
                    return;
                }
                eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
                try {
                    Job job = (Job) dispatchedContinuation.getContext().get(Job.Key);
                    if (job == null || job.isActive()) {
                        z = false;
                    } else {
                        CancellationException cancellationException = job.getCancellationException();
                        Result.Companion companion = Result.Companion;
                        dispatchedContinuation.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(cancellationException)));
                        z = true;
                    }
                    if (!z) {
                        CoroutineContext context = dispatchedContinuation.getContext();
                        Object objUpdateThreadContext = ThreadContextKt.updateThreadContext(context, dispatchedContinuation.countOrElement);
                        try {
                            Continuation<T> continuation = dispatchedContinuation.continuation;
                            Result.Companion companion2 = Result.Companion;
                            continuation.resumeWith(Result.m407constructorimpl(t));
                            Unit unit = Unit.INSTANCE;
                            ThreadContextKt.restoreThreadContext(context, objUpdateThreadContext);
                        } catch (Throwable th) {
                            ThreadContextKt.restoreThreadContext(context, objUpdateThreadContext);
                            throw th;
                        }
                    }
                    while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent()) {
                    }
                } finally {
                    try {
                        return;
                    } finally {
                    }
                }
                return;
            }
            dispatchedContinuation._state = t;
            dispatchedContinuation.resumeMode = 1;
            dispatchedContinuation.dispatcher.mo415dispatch(dispatchedContinuation.getContext(), dispatchedContinuation);
            return;
        }
        Result.Companion companion3 = Result.Companion;
        resumeCancellable.resumeWith(Result.m407constructorimpl(t));
    }

    public static final <T> void resumeCancellableWithException(@NotNull Continuation<? super T> resumeCancellableWithException, @NotNull Throwable exception) {
        Intrinsics.checkParameterIsNotNull(resumeCancellableWithException, "$this$resumeCancellableWithException");
        Intrinsics.checkParameterIsNotNull(exception, "exception");
        if (resumeCancellableWithException instanceof DispatchedContinuation) {
            DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) resumeCancellableWithException;
            CoroutineContext context = dispatchedContinuation.continuation.getContext();
            boolean z = false;
            int i = 2;
            Throwable th = null;
            byte b = 0;
            CompletedExceptionally completedExceptionally = new CompletedExceptionally(exception, z, i, 0 == true ? 1 : 0);
            if (!dispatchedContinuation.dispatcher.isDispatchNeeded(context)) {
                EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
                if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
                    dispatchedContinuation._state = completedExceptionally;
                    dispatchedContinuation.resumeMode = 1;
                    eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedContinuation);
                    return;
                }
                eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
                try {
                    Job job = (Job) dispatchedContinuation.getContext().get(Job.Key);
                    if (job != null && !job.isActive()) {
                        CancellationException cancellationException = job.getCancellationException();
                        Result.Companion companion = Result.Companion;
                        dispatchedContinuation.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(cancellationException)));
                        z = true;
                    }
                    if (!z) {
                        CoroutineContext context2 = dispatchedContinuation.getContext();
                        Object objUpdateThreadContext = ThreadContextKt.updateThreadContext(context2, dispatchedContinuation.countOrElement);
                        try {
                            Continuation<T> continuation = dispatchedContinuation.continuation;
                            Result.Companion companion2 = Result.Companion;
                            continuation.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(exception, continuation))));
                            Unit unit = Unit.INSTANCE;
                            ThreadContextKt.restoreThreadContext(context2, objUpdateThreadContext);
                        } catch (Throwable th2) {
                            ThreadContextKt.restoreThreadContext(context2, objUpdateThreadContext);
                            throw th2;
                        }
                    }
                    while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent()) {
                    }
                } finally {
                    try {
                        return;
                    } finally {
                    }
                }
                return;
            }
            dispatchedContinuation._state = new CompletedExceptionally(exception, z, i, b == true ? 1 : 0);
            dispatchedContinuation.resumeMode = 1;
            dispatchedContinuation.dispatcher.mo415dispatch(context, dispatchedContinuation);
            return;
        }
        Result.Companion companion3 = Result.Companion;
        resumeCancellableWithException.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(exception, resumeCancellableWithException))));
    }

    public static final <T> void resumeDirect(@NotNull Continuation<? super T> resumeDirect, T t) {
        Intrinsics.checkParameterIsNotNull(resumeDirect, "$this$resumeDirect");
        if (!(resumeDirect instanceof DispatchedContinuation)) {
            Result.Companion companion = Result.Companion;
            resumeDirect.resumeWith(Result.m407constructorimpl(t));
        } else {
            Continuation<T> continuation = ((DispatchedContinuation) resumeDirect).continuation;
            Result.Companion companion2 = Result.Companion;
            continuation.resumeWith(Result.m407constructorimpl(t));
        }
    }

    public static final <T> void resumeDirectWithException(@NotNull Continuation<? super T> resumeDirectWithException, @NotNull Throwable exception) {
        Intrinsics.checkParameterIsNotNull(resumeDirectWithException, "$this$resumeDirectWithException");
        Intrinsics.checkParameterIsNotNull(exception, "exception");
        if (resumeDirectWithException instanceof DispatchedContinuation) {
            Continuation<T> continuation = ((DispatchedContinuation) resumeDirectWithException).continuation;
            Result.Companion companion = Result.Companion;
            continuation.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(exception, continuation))));
        } else {
            Result.Companion companion2 = Result.Companion;
            resumeDirectWithException.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(exception, resumeDirectWithException))));
        }
    }

    public static final boolean yieldUndispatched(@NotNull DispatchedContinuation<? super Unit> yieldUndispatched) {
        Intrinsics.checkParameterIsNotNull(yieldUndispatched, "$this$yieldUndispatched");
        Unit unit = Unit.INSTANCE;
        EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedQueueEmpty()) {
            return false;
        }
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
            yieldUndispatched._state = unit;
            yieldUndispatched.resumeMode = 1;
            eventLoop$kotlinx_coroutines_core.dispatchUnconfined(yieldUndispatched);
            return true;
        }
        eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
        try {
            yieldUndispatched.run();
            do {
            } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
        } finally {
            try {
                return false;
            } finally {
            }
        }
        return false;
    }

    public static final <T> void dispatch(@NotNull DispatchedTask<? super T> dispatch, int i) {
        Intrinsics.checkParameterIsNotNull(dispatch, "$this$dispatch");
        Continuation<? super T> delegate$kotlinx_coroutines_core = dispatch.getDelegate$kotlinx_coroutines_core();
        if (ResumeModeKt.isDispatchedMode(i) && (delegate$kotlinx_coroutines_core instanceof DispatchedContinuation) && ResumeModeKt.isCancellableMode(i) == ResumeModeKt.isCancellableMode(dispatch.resumeMode)) {
            CoroutineDispatcher coroutineDispatcher = ((DispatchedContinuation) delegate$kotlinx_coroutines_core).dispatcher;
            CoroutineContext context = delegate$kotlinx_coroutines_core.getContext();
            if (coroutineDispatcher.isDispatchNeeded(context)) {
                coroutineDispatcher.mo415dispatch(context, dispatch);
                return;
            } else {
                resumeUnconfined(dispatch);
                return;
            }
        }
        resume(dispatch, delegate$kotlinx_coroutines_core, i);
    }

    public static final <T> void resume(@NotNull DispatchedTask<? super T> resume, @NotNull Continuation<? super T> delegate, int i) {
        Intrinsics.checkParameterIsNotNull(resume, "$this$resume");
        Intrinsics.checkParameterIsNotNull(delegate, "delegate");
        Object objTakeState$kotlinx_coroutines_core = resume.takeState$kotlinx_coroutines_core();
        Throwable exceptionalResult$kotlinx_coroutines_core = resume.getExceptionalResult$kotlinx_coroutines_core(objTakeState$kotlinx_coroutines_core);
        if (exceptionalResult$kotlinx_coroutines_core != null) {
            if (!(delegate instanceof DispatchedTask)) {
                exceptionalResult$kotlinx_coroutines_core = StackTraceRecoveryKt.recoverStackTrace(exceptionalResult$kotlinx_coroutines_core, delegate);
            }
            ResumeModeKt.resumeWithExceptionMode(delegate, exceptionalResult$kotlinx_coroutines_core, i);
            return;
        }
        ResumeModeKt.resumeMode(delegate, resume.getSuccessfulResult$kotlinx_coroutines_core(objTakeState$kotlinx_coroutines_core), i);
    }
}
