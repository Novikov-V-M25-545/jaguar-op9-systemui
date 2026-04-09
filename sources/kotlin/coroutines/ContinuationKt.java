package kotlin.coroutines;

import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Continuation.kt */
/* loaded from: classes.dex */
public final class ContinuationKt {
    public static final <R, T> void startCoroutine(@NotNull Function2<? super R, ? super Continuation<? super T>, ? extends Object> startCoroutine, R r, @NotNull Continuation<? super T> completion) {
        Intrinsics.checkParameterIsNotNull(startCoroutine, "$this$startCoroutine");
        Intrinsics.checkParameterIsNotNull(completion, "completion");
        Continuation continuationIntercepted = IntrinsicsKt__IntrinsicsJvmKt.intercepted(IntrinsicsKt__IntrinsicsJvmKt.createCoroutineUnintercepted(startCoroutine, r, completion));
        Unit unit = Unit.INSTANCE;
        Result.Companion companion = Result.Companion;
        continuationIntercepted.resumeWith(Result.m407constructorimpl(unit));
    }
}
