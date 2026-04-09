package kotlinx.coroutines.internal;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ThreadContext.kt */
/* loaded from: classes2.dex */
final class ThreadState {
    private Object[] a;

    @NotNull
    private final CoroutineContext context;
    private int i;

    public ThreadState(@NotNull CoroutineContext context, int i) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
        this.a = new Object[i];
    }

    @NotNull
    public final CoroutineContext getContext() {
        return this.context;
    }

    public final void append(@Nullable Object obj) {
        Object[] objArr = this.a;
        int i = this.i;
        this.i = i + 1;
        objArr[i] = obj;
    }

    @Nullable
    public final Object take() {
        Object[] objArr = this.a;
        int i = this.i;
        this.i = i + 1;
        return objArr[i];
    }

    public final void start() {
        this.i = 0;
    }
}
