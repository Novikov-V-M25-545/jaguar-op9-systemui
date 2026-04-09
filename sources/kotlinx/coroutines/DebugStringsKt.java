package kotlinx.coroutines;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DebugStrings.kt */
/* loaded from: classes2.dex */
public final class DebugStringsKt {
    @NotNull
    public static final String getHexAddress(@NotNull Object hexAddress) {
        Intrinsics.checkParameterIsNotNull(hexAddress, "$this$hexAddress");
        String hexString = Integer.toHexString(System.identityHashCode(hexAddress));
        Intrinsics.checkExpressionValueIsNotNull(hexString, "Integer.toHexString(System.identityHashCode(this))");
        return hexString;
    }

    @NotNull
    public static final String toDebugString(@NotNull Continuation<?> toDebugString) {
        Object objM407constructorimpl;
        Intrinsics.checkParameterIsNotNull(toDebugString, "$this$toDebugString");
        if (toDebugString instanceof DispatchedContinuation) {
            return toDebugString.toString();
        }
        try {
            Result.Companion companion = Result.Companion;
            objM407constructorimpl = Result.m407constructorimpl(toDebugString + '@' + getHexAddress(toDebugString));
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            objM407constructorimpl = Result.m407constructorimpl(ResultKt.createFailure(th));
        }
        if (Result.m409exceptionOrNullimpl(objM407constructorimpl) != null) {
            objM407constructorimpl = toDebugString.getClass().getName() + '@' + getHexAddress(toDebugString);
        }
        return (String) objM407constructorimpl;
    }

    @NotNull
    public static final String getClassSimpleName(@NotNull Object classSimpleName) {
        Intrinsics.checkParameterIsNotNull(classSimpleName, "$this$classSimpleName");
        String simpleName = classSimpleName.getClass().getSimpleName();
        Intrinsics.checkExpressionValueIsNotNull(simpleName, "this::class.java.simpleName");
        return simpleName;
    }
}
