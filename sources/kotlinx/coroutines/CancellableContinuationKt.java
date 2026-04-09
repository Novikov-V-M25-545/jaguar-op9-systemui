package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CancellableContinuation.kt */
/* loaded from: classes2.dex */
public final class CancellableContinuationKt {
    public static final void disposeOnCancellation(@NotNull CancellableContinuation<?> disposeOnCancellation, @NotNull DisposableHandle handle) {
        Intrinsics.checkParameterIsNotNull(disposeOnCancellation, "$this$disposeOnCancellation");
        Intrinsics.checkParameterIsNotNull(handle, "handle");
        disposeOnCancellation.invokeOnCancellation(new DisposeOnCancel(handle));
    }
}
