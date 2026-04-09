package kotlinx.coroutines;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.ContextScope;
import org.jetbrains.annotations.NotNull;

/* compiled from: CoroutineScope.kt */
/* loaded from: classes2.dex */
public final class CoroutineScopeKt {
    @NotNull
    public static final CoroutineScope CoroutineScope(@NotNull CoroutineContext context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (context.get(Job.Key) == null) {
            context = context.plus(JobKt__JobKt.Job$default(null, 1, null));
        }
        return new ContextScope(context);
    }
}
