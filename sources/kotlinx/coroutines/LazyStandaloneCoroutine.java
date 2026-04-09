package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.intrinsics.CancellableKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: Builders.common.kt */
/* loaded from: classes2.dex */
final class LazyStandaloneCoroutine extends StandaloneCoroutine {
    private Function2<? super CoroutineScope, ? super Continuation<? super Unit>, ? extends Object> block;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public LazyStandaloneCoroutine(@NotNull CoroutineContext parentContext, @NotNull Function2<? super CoroutineScope, ? super Continuation<? super Unit>, ? extends Object> block) {
        super(parentContext, false);
        Intrinsics.checkParameterIsNotNull(parentContext, "parentContext");
        Intrinsics.checkParameterIsNotNull(block, "block");
        this.block = block;
    }

    @Override // kotlinx.coroutines.AbstractCoroutine
    protected void onStart() {
        Function2<? super CoroutineScope, ? super Continuation<? super Unit>, ? extends Object> function2 = this.block;
        if (function2 == null) {
            throw new IllegalStateException("Already started".toString());
        }
        this.block = null;
        CancellableKt.startCoroutineCancellable(function2, this, this);
    }
}
