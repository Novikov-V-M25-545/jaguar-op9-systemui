package kotlinx.coroutines.internal;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;

/* compiled from: Scopes.kt */
/* loaded from: classes2.dex */
public final class ContextScope implements CoroutineScope {

    @NotNull
    private final CoroutineContext coroutineContext;

    public ContextScope(@NotNull CoroutineContext context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.coroutineContext = context;
    }

    @Override // kotlinx.coroutines.CoroutineScope
    @NotNull
    public CoroutineContext getCoroutineContext() {
        return this.coroutineContext;
    }
}
