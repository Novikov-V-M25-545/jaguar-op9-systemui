package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.jvm.internal.CoroutineStackFrame;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Dispatched.kt */
/* loaded from: classes2.dex */
public final class DispatchedContinuation<T> extends DispatchedTask<T> implements CoroutineStackFrame, Continuation<T> {

    @Nullable
    public Object _state;

    @Nullable
    private final CoroutineStackFrame callerFrame;

    @NotNull
    public final Continuation<T> continuation;

    @NotNull
    public final Object countOrElement;

    @NotNull
    public final CoroutineDispatcher dispatcher;

    @Override // kotlin.coroutines.Continuation
    @NotNull
    public CoroutineContext getContext() {
        return this.continuation.getContext();
    }

    @Override // kotlinx.coroutines.DispatchedTask
    @NotNull
    public Continuation<T> getDelegate$kotlinx_coroutines_core() {
        return this;
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    @Nullable
    public StackTraceElement getStackTraceElement() {
        return null;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    public DispatchedContinuation(@NotNull CoroutineDispatcher dispatcher, @NotNull Continuation<? super T> continuation) {
        super(0);
        Intrinsics.checkParameterIsNotNull(dispatcher, "dispatcher");
        Intrinsics.checkParameterIsNotNull(continuation, "continuation");
        this.dispatcher = dispatcher;
        this.continuation = continuation;
        this._state = DispatchedKt.UNDEFINED;
        this.callerFrame = continuation instanceof CoroutineStackFrame ? continuation : (Continuation<? super T>) null;
        this.countOrElement = ThreadContextKt.threadContextElements(getContext());
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    @Nullable
    public CoroutineStackFrame getCallerFrame() {
        return this.callerFrame;
    }

    @Override // kotlinx.coroutines.DispatchedTask
    @Nullable
    public Object takeState$kotlinx_coroutines_core() {
        Object obj = this._state;
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(obj != DispatchedKt.UNDEFINED)) {
                throw new AssertionError();
            }
        }
        this._state = DispatchedKt.UNDEFINED;
        return obj;
    }

    @Override // kotlin.coroutines.Continuation
    public void resumeWith(@NotNull Object obj) {
        CoroutineContext context = this.continuation.getContext();
        Object state = CompletedExceptionallyKt.toState(obj);
        if (!this.dispatcher.isDispatchNeeded(context)) {
            EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
            if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
                this._state = state;
                this.resumeMode = 0;
                eventLoop$kotlinx_coroutines_core.dispatchUnconfined(this);
                return;
            }
            eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
            try {
                CoroutineContext context2 = getContext();
                Object objUpdateThreadContext = ThreadContextKt.updateThreadContext(context2, this.countOrElement);
                try {
                    this.continuation.resumeWith(obj);
                    Unit unit = Unit.INSTANCE;
                    while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent()) {
                    }
                } finally {
                    ThreadContextKt.restoreThreadContext(context2, objUpdateThreadContext);
                }
            } finally {
                try {
                } finally {
                }
            }
            return;
        }
        this._state = state;
        this.resumeMode = 0;
        this.dispatcher.mo415dispatch(context, this);
    }

    public final void dispatchYield$kotlinx_coroutines_core(T t) {
        CoroutineContext context = this.continuation.getContext();
        this._state = t;
        this.resumeMode = 1;
        this.dispatcher.dispatchYield(context, this);
    }

    @NotNull
    public String toString() {
        return "DispatchedContinuation[" + this.dispatcher + ", " + DebugStringsKt.toDebugString(this.continuation) + ']';
    }
}
