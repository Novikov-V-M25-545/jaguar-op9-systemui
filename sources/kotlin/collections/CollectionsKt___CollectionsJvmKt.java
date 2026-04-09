package kotlin.collections;

import java.util.Collections;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: _CollectionsJvm.kt */
/* loaded from: classes.dex */
class CollectionsKt___CollectionsJvmKt extends CollectionsKt__ReversedViewsKt {
    public static final <T> void reverse(@NotNull List<T> reverse) {
        Intrinsics.checkParameterIsNotNull(reverse, "$this$reverse");
        Collections.reverse(reverse);
    }
}
