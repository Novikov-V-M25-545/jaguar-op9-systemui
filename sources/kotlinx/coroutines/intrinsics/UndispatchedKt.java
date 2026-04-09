package kotlinx.coroutines.intrinsics;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: Undispatched.kt */
/* loaded from: classes2.dex */
public final class UndispatchedKt {
    public static final <R, T> void startCoroutineUndispatched(@NotNull Function2<? super R, ? super Continuation<? super T>, ? extends Object> startCoroutineUndispatched, R r, @NotNull Continuation<? super T> completion) {
        Intrinsics.checkParameterIsNotNull(startCoroutineUndispatched, "$this$startCoroutineUndispatched");
        Intrinsics.checkParameterIsNotNull(completion, "completion");
        Continuation continuationProbeCoroutineCreated = DebugProbesKt.probeCoroutineCreated(completion);
        try {
            CoroutineContext context = completion.getContext();
            Object objUpdateThreadContext = ThreadContextKt.updateThreadContext(context, null);
            try {
                Object objInvoke = ((Function2) TypeIntrinsics.beforeCheckcastToFunctionOfArity(startCoroutineUndispatched, 2)).invoke(r, continuationProbeCoroutineCreated);
                if (objInvoke != IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    Result.Companion companion = Result.Companion;
                    continuationProbeCoroutineCreated.resumeWith(Result.m407constructorimpl(objInvoke));
                }
            } finally {
                ThreadContextKt.restoreThreadContext(context, objUpdateThreadContext);
            }
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            continuationProbeCoroutineCreated.resumeWith(Result.m407constructorimpl(ResultKt.createFailure(th)));
        }
    }
}
