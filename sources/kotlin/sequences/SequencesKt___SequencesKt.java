package kotlin.sequences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__MutableCollectionsJVMKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringBuilderKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: _Sequences.kt */
/* loaded from: classes.dex */
public class SequencesKt___SequencesKt extends SequencesKt___SequencesJvmKt {
    @Nullable
    public static <T> T firstOrNull(@NotNull Sequence<? extends T> firstOrNull) {
        Intrinsics.checkParameterIsNotNull(firstOrNull, "$this$firstOrNull");
        Iterator<? extends T> it = firstOrNull.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Nullable
    public static <T> T lastOrNull(@NotNull Sequence<? extends T> lastOrNull) {
        Intrinsics.checkParameterIsNotNull(lastOrNull, "$this$lastOrNull");
        Iterator<? extends T> it = lastOrNull.iterator();
        if (!it.hasNext()) {
            return null;
        }
        T next = it.next();
        while (it.hasNext()) {
            next = it.next();
        }
        return next;
    }

    @NotNull
    public static <T> Sequence<T> filter(@NotNull Sequence<? extends T> filter, @NotNull Function1<? super T, Boolean> predicate) {
        Intrinsics.checkParameterIsNotNull(filter, "$this$filter");
        Intrinsics.checkParameterIsNotNull(predicate, "predicate");
        return new FilteringSequence(filter, true, predicate);
    }

    @NotNull
    public static <T> Sequence<T> filterNot(@NotNull Sequence<? extends T> filterNot, @NotNull Function1<? super T, Boolean> predicate) {
        Intrinsics.checkParameterIsNotNull(filterNot, "$this$filterNot");
        Intrinsics.checkParameterIsNotNull(predicate, "predicate");
        return new FilteringSequence(filterNot, false, predicate);
    }

    @NotNull
    public static <T> Sequence<T> filterNotNull(@NotNull Sequence<? extends T> filterNotNull) {
        Intrinsics.checkParameterIsNotNull(filterNotNull, "$this$filterNotNull");
        Sequence<T> sequenceFilterNot = filterNot(filterNotNull, new Function1<T, Boolean>() { // from class: kotlin.sequences.SequencesKt___SequencesKt.filterNotNull.1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(Object obj) {
                return Boolean.valueOf(invoke2((C01971<T>) obj));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final boolean invoke2(@Nullable T t) {
                return t == null;
            }
        });
        if (sequenceFilterNot != null) {
            return sequenceFilterNot;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.sequences.Sequence<T>");
    }

    @NotNull
    public static <T> Sequence<T> take(@NotNull Sequence<? extends T> take, int i) {
        Intrinsics.checkParameterIsNotNull(take, "$this$take");
        if (i >= 0) {
            if (i == 0) {
                return SequencesKt__SequencesKt.emptySequence();
            }
            return take instanceof DropTakeSequence ? ((DropTakeSequence) take).take(i) : new TakeSequence(take, i);
        }
        throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
    }

    @NotNull
    public static <T> Sequence<T> sortedWith(@NotNull final Sequence<? extends T> sortedWith, @NotNull final Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(sortedWith, "$this$sortedWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        return new Sequence<T>() { // from class: kotlin.sequences.SequencesKt___SequencesKt.sortedWith.1
            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                List mutableList = SequencesKt___SequencesKt.toMutableList(sortedWith);
                CollectionsKt__MutableCollectionsJVMKt.sortWith(mutableList, comparator);
                return mutableList.iterator();
            }
        };
    }

    @NotNull
    public static final <T, C extends Collection<? super T>> C toCollection(@NotNull Sequence<? extends T> toCollection, @NotNull C destination) {
        Intrinsics.checkParameterIsNotNull(toCollection, "$this$toCollection");
        Intrinsics.checkParameterIsNotNull(destination, "destination");
        Iterator<? extends T> it = toCollection.iterator();
        while (it.hasNext()) {
            destination.add(it.next());
        }
        return destination;
    }

    @NotNull
    public static <T> List<T> toList(@NotNull Sequence<? extends T> toList) {
        Intrinsics.checkParameterIsNotNull(toList, "$this$toList");
        return CollectionsKt__CollectionsKt.optimizeReadOnlyList(toMutableList(toList));
    }

    @NotNull
    public static final <T> List<T> toMutableList(@NotNull Sequence<? extends T> toMutableList) {
        Intrinsics.checkParameterIsNotNull(toMutableList, "$this$toMutableList");
        return (List) toCollection(toMutableList, new ArrayList());
    }

    @NotNull
    public static <T, R> Sequence<R> flatMap(@NotNull Sequence<? extends T> flatMap, @NotNull Function1<? super T, ? extends Sequence<? extends R>> transform) {
        Intrinsics.checkParameterIsNotNull(flatMap, "$this$flatMap");
        Intrinsics.checkParameterIsNotNull(transform, "transform");
        return new FlatteningSequence(flatMap, transform, new Function1<Sequence<? extends R>, Iterator<? extends R>>() { // from class: kotlin.sequences.SequencesKt___SequencesKt.flatMap.1
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final Iterator<R> invoke(@NotNull Sequence<? extends R> it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return it.iterator();
            }
        });
    }

    @NotNull
    public static <T, R> Sequence<R> map(@NotNull Sequence<? extends T> map, @NotNull Function1<? super T, ? extends R> transform) {
        Intrinsics.checkParameterIsNotNull(map, "$this$map");
        Intrinsics.checkParameterIsNotNull(transform, "transform");
        return new TransformingSequence(map, transform);
    }

    @NotNull
    public static <T, R> Sequence<R> mapNotNull(@NotNull Sequence<? extends T> mapNotNull, @NotNull Function1<? super T, ? extends R> transform) {
        Intrinsics.checkParameterIsNotNull(mapNotNull, "$this$mapNotNull");
        Intrinsics.checkParameterIsNotNull(transform, "transform");
        return filterNotNull(new TransformingSequence(mapNotNull, transform));
    }

    @NotNull
    public static <T> Sequence<T> distinct(@NotNull Sequence<? extends T> distinct) {
        Intrinsics.checkParameterIsNotNull(distinct, "$this$distinct");
        return distinctBy(distinct, new Function1<T, T>() { // from class: kotlin.sequences.SequencesKt___SequencesKt.distinct.1
            @Override // kotlin.jvm.functions.Function1
            public final T invoke(T t) {
                return t;
            }
        });
    }

    @NotNull
    public static final <T, K> Sequence<T> distinctBy(@NotNull Sequence<? extends T> distinctBy, @NotNull Function1<? super T, ? extends K> selector) {
        Intrinsics.checkParameterIsNotNull(distinctBy, "$this$distinctBy");
        Intrinsics.checkParameterIsNotNull(selector, "selector");
        return new DistinctSequence(distinctBy, selector);
    }

    @NotNull
    public static final <T, A extends Appendable> A joinTo(@NotNull Sequence<? extends T> joinTo, @NotNull A buffer, @NotNull CharSequence separator, @NotNull CharSequence prefix, @NotNull CharSequence postfix, int i, @NotNull CharSequence truncated, @Nullable Function1<? super T, ? extends CharSequence> function1) throws IOException {
        Intrinsics.checkParameterIsNotNull(joinTo, "$this$joinTo");
        Intrinsics.checkParameterIsNotNull(buffer, "buffer");
        Intrinsics.checkParameterIsNotNull(separator, "separator");
        Intrinsics.checkParameterIsNotNull(prefix, "prefix");
        Intrinsics.checkParameterIsNotNull(postfix, "postfix");
        Intrinsics.checkParameterIsNotNull(truncated, "truncated");
        buffer.append(prefix);
        int i2 = 0;
        for (T t : joinTo) {
            i2++;
            if (i2 > 1) {
                buffer.append(separator);
            }
            if (i >= 0 && i2 > i) {
                break;
            }
            StringsKt__StringBuilderKt.appendElement(buffer, t, function1);
        }
        if (i >= 0 && i2 > i) {
            buffer.append(truncated);
        }
        buffer.append(postfix);
        return buffer;
    }

    public static /* synthetic */ String joinToString$default(Sequence sequence, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i, CharSequence charSequence4, Function1 function1, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            charSequence = ", ";
        }
        CharSequence charSequence5 = (i2 & 2) != 0 ? "" : charSequence2;
        CharSequence charSequence6 = (i2 & 4) == 0 ? charSequence3 : "";
        if ((i2 & 8) != 0) {
            i = -1;
        }
        int i3 = i;
        if ((i2 & 16) != 0) {
            charSequence4 = "...";
        }
        CharSequence charSequence7 = charSequence4;
        if ((i2 & 32) != 0) {
            function1 = null;
        }
        return joinToString(sequence, charSequence, charSequence5, charSequence6, i3, charSequence7, function1);
    }

    @NotNull
    public static final <T> String joinToString(@NotNull Sequence<? extends T> joinToString, @NotNull CharSequence separator, @NotNull CharSequence prefix, @NotNull CharSequence postfix, int i, @NotNull CharSequence truncated, @Nullable Function1<? super T, ? extends CharSequence> function1) {
        Intrinsics.checkParameterIsNotNull(joinToString, "$this$joinToString");
        Intrinsics.checkParameterIsNotNull(separator, "separator");
        Intrinsics.checkParameterIsNotNull(prefix, "prefix");
        Intrinsics.checkParameterIsNotNull(postfix, "postfix");
        Intrinsics.checkParameterIsNotNull(truncated, "truncated");
        String string = ((StringBuilder) joinTo(joinToString, new StringBuilder(), separator, prefix, postfix, i, truncated, function1)).toString();
        Intrinsics.checkExpressionValueIsNotNull(string, "joinTo(StringBuilder(), …ed, transform).toString()");
        return string;
    }

    @NotNull
    public static <T> Iterable<T> asIterable(@NotNull Sequence<? extends T> asIterable) {
        Intrinsics.checkParameterIsNotNull(asIterable, "$this$asIterable");
        return new SequencesKt___SequencesKt$asIterable$$inlined$Iterable$1(asIterable);
    }
}
