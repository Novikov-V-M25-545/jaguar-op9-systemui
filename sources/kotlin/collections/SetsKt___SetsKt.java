package kotlin.collections;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: _Sets.kt */
/* loaded from: classes.dex */
public class SetsKt___SetsKt extends SetsKt__SetsKt {
    @NotNull
    public static <T> Set<T> minus(@NotNull Set<? extends T> minus, @NotNull Iterable<? extends T> elements) {
        Intrinsics.checkParameterIsNotNull(minus, "$this$minus");
        Intrinsics.checkParameterIsNotNull(elements, "elements");
        Collection<?> collectionConvertToSetForSetOperationWith = CollectionsKt__IterablesKt.convertToSetForSetOperationWith(elements, minus);
        if (collectionConvertToSetForSetOperationWith.isEmpty()) {
            return CollectionsKt___CollectionsKt.toSet(minus);
        }
        if (collectionConvertToSetForSetOperationWith instanceof Set) {
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            for (T t : minus) {
                if (!collectionConvertToSetForSetOperationWith.contains(t)) {
                    linkedHashSet.add(t);
                }
            }
            return linkedHashSet;
        }
        LinkedHashSet linkedHashSet2 = new LinkedHashSet(minus);
        linkedHashSet2.removeAll(collectionConvertToSetForSetOperationWith);
        return linkedHashSet2;
    }
}
