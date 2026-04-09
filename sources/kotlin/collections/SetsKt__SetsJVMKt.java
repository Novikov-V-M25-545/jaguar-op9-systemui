package kotlin.collections;

import java.util.Collections;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: SetsJVM.kt */
/* loaded from: classes.dex */
class SetsKt__SetsJVMKt {
    @NotNull
    public static final <T> Set<T> setOf(T t) {
        Set<T> setSingleton = Collections.singleton(t);
        Intrinsics.checkExpressionValueIsNotNull(setSingleton, "java.util.Collections.singleton(element)");
        return setSingleton;
    }
}
