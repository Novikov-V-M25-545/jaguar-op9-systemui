package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: Builders.common.kt */
/* loaded from: classes2.dex */
public final /* synthetic */ class BuildersKt__Builders_commonKt {
    public static /* synthetic */ Job launch$default(CoroutineScope coroutineScope, CoroutineContext coroutineContext, CoroutineStart coroutineStart, Function2 function2, int i, Object obj) {
        if ((i & 1) != 0) {
            coroutineContext = EmptyCoroutineContext.INSTANCE;
        }
        if ((i & 2) != 0) {
            coroutineStart = CoroutineStart.DEFAULT;
        }
        return BuildersKt.launch(coroutineScope, coroutineContext, coroutineStart, function2);
    }

    @NotNull
    public static final Job launch(@NotNull CoroutineScope launch, @NotNull CoroutineContext context, @NotNull CoroutineStart start, @NotNull Function2<? super CoroutineScope, ? super Continuation<? super Unit>, ? extends Object> block) {
        AbstractCoroutine standaloneCoroutine;
        Intrinsics.checkParameterIsNotNull(launch, "$this$launch");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(start, "start");
        Intrinsics.checkParameterIsNotNull(block, "block");
        CoroutineContext coroutineContextNewCoroutineContext = CoroutineContextKt.newCoroutineContext(launch, context);
        if (start.isLazy()) {
            standaloneCoroutine = new LazyStandaloneCoroutine(coroutineContextNewCoroutineContext, block);
        } else {
            standaloneCoroutine = new StandaloneCoroutine(coroutineContextNewCoroutineContext, true);
        }
        standaloneCoroutine.start(start, standaloneCoroutine, block);
        return standaloneCoroutine;
    }
}
