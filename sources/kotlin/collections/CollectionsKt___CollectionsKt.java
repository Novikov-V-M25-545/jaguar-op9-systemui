package kotlin.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.text.StringsKt__StringBuilderKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: _Collections.kt */
/* loaded from: classes.dex */
public class CollectionsKt___CollectionsKt extends CollectionsKt___CollectionsJvmKt {
    public static <T> boolean contains(@NotNull Iterable<? extends T> contains, T t) {
        Intrinsics.checkParameterIsNotNull(contains, "$this$contains");
        if (contains instanceof Collection) {
            return ((Collection) contains).contains(t);
        }
        return indexOf(contains, t) >= 0;
    }

    public static final <T> T first(@NotNull Iterable<? extends T> first) {
        Intrinsics.checkParameterIsNotNull(first, "$this$first");
        if (first instanceof List) {
            return (T) first((List) first);
        }
        Iterator<? extends T> it = first.iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("Collection is empty.");
        }
        return it.next();
    }

    public static final <T> T first(@NotNull List<? extends T> first) {
        Intrinsics.checkParameterIsNotNull(first, "$this$first");
        if (first.isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }
        return first.get(0);
    }

    @Nullable
    public static <T> T firstOrNull(@NotNull List<? extends T> firstOrNull) {
        Intrinsics.checkParameterIsNotNull(firstOrNull, "$this$firstOrNull");
        if (firstOrNull.isEmpty()) {
            return null;
        }
        return firstOrNull.get(0);
    }

    public static final <T> int indexOf(@NotNull Iterable<? extends T> indexOf, T t) {
        Intrinsics.checkParameterIsNotNull(indexOf, "$this$indexOf");
        if (indexOf instanceof List) {
            return ((List) indexOf).indexOf(t);
        }
        int i = 0;
        for (T t2 : indexOf) {
            if (i < 0) {
                CollectionsKt__CollectionsKt.throwIndexOverflow();
            }
            if (Intrinsics.areEqual(t, t2)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static final <T> T last(@NotNull Iterable<? extends T> last) {
        Intrinsics.checkParameterIsNotNull(last, "$this$last");
        if (last instanceof List) {
            return (T) CollectionsKt.last((List) last);
        }
        Iterator<? extends T> it = last.iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("Collection is empty.");
        }
        T next = it.next();
        while (it.hasNext()) {
            next = it.next();
        }
        return next;
    }

    public static <T> T last(@NotNull List<? extends T> last) {
        Intrinsics.checkParameterIsNotNull(last, "$this$last");
        if (last.isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }
        return last.get(CollectionsKt__CollectionsKt.getLastIndex(last));
    }

    public static <T> T single(@NotNull Iterable<? extends T> single) {
        Intrinsics.checkParameterIsNotNull(single, "$this$single");
        if (single instanceof List) {
            return (T) single((List) single);
        }
        Iterator<? extends T> it = single.iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("Collection is empty.");
        }
        T next = it.next();
        if (it.hasNext()) {
            throw new IllegalArgumentException("Collection has more than one element.");
        }
        return next;
    }

    public static final <T> T single(@NotNull List<? extends T> single) {
        Intrinsics.checkParameterIsNotNull(single, "$this$single");
        int size = single.size();
        if (size == 0) {
            throw new NoSuchElementException("List is empty.");
        }
        if (size == 1) {
            return single.get(0);
        }
        throw new IllegalArgumentException("List has more than one element.");
    }

    @NotNull
    public static <T> List<T> drop(@NotNull Iterable<? extends T> drop, int i) {
        ArrayList arrayList;
        Intrinsics.checkParameterIsNotNull(drop, "$this$drop");
        int i2 = 0;
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        }
        if (i == 0) {
            return toList(drop);
        }
        if (drop instanceof Collection) {
            Collection collection = (Collection) drop;
            int size = collection.size() - i;
            if (size <= 0) {
                return CollectionsKt__CollectionsKt.emptyList();
            }
            if (size == 1) {
                return CollectionsKt__CollectionsJVMKt.listOf(last(drop));
            }
            arrayList = new ArrayList(size);
            if (drop instanceof List) {
                if (drop instanceof RandomAccess) {
                    int size2 = collection.size();
                    while (i < size2) {
                        arrayList.add(((List) drop).get(i));
                        i++;
                    }
                } else {
                    ListIterator listIterator = ((List) drop).listIterator(i);
                    while (listIterator.hasNext()) {
                        arrayList.add(listIterator.next());
                    }
                }
                return arrayList;
            }
        } else {
            arrayList = new ArrayList();
        }
        for (T t : drop) {
            if (i2 >= i) {
                arrayList.add(t);
            } else {
                i2++;
            }
        }
        return CollectionsKt__CollectionsKt.optimizeReadOnlyList(arrayList);
    }

    @NotNull
    public static <T> List<T> take(@NotNull Iterable<? extends T> take, int i) {
        Intrinsics.checkParameterIsNotNull(take, "$this$take");
        int i2 = 0;
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        }
        if (i == 0) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
        if (take instanceof Collection) {
            if (i >= ((Collection) take).size()) {
                return toList(take);
            }
            if (i == 1) {
                return CollectionsKt__CollectionsJVMKt.listOf(first(take));
            }
        }
        ArrayList arrayList = new ArrayList(i);
        Iterator<? extends T> it = take.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next());
            i2++;
            if (i2 == i) {
                break;
            }
        }
        return CollectionsKt__CollectionsKt.optimizeReadOnlyList(arrayList);
    }

    @NotNull
    public static <T> List<T> takeLast(@NotNull List<? extends T> takeLast, int i) {
        Intrinsics.checkParameterIsNotNull(takeLast, "$this$takeLast");
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        }
        if (i == 0) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
        int size = takeLast.size();
        if (i >= size) {
            return toList(takeLast);
        }
        if (i == 1) {
            return CollectionsKt__CollectionsJVMKt.listOf(CollectionsKt.last((List) takeLast));
        }
        ArrayList arrayList = new ArrayList(i);
        if (takeLast instanceof RandomAccess) {
            for (int i2 = size - i; i2 < size; i2++) {
                arrayList.add(takeLast.get(i2));
            }
        } else {
            ListIterator<? extends T> listIterator = takeLast.listIterator(size - i);
            while (listIterator.hasNext()) {
                arrayList.add(listIterator.next());
            }
        }
        return arrayList;
    }

    @NotNull
    public static <T> List<T> reversed(@NotNull Iterable<? extends T> reversed) {
        Intrinsics.checkParameterIsNotNull(reversed, "$this$reversed");
        if ((reversed instanceof Collection) && ((Collection) reversed).size() <= 1) {
            return toList(reversed);
        }
        List<T> mutableList = toMutableList(reversed);
        CollectionsKt___CollectionsJvmKt.reverse(mutableList);
        return mutableList;
    }

    @NotNull
    public static <T extends Comparable<? super T>> List<T> sorted(@NotNull Iterable<? extends T> sorted) {
        Intrinsics.checkParameterIsNotNull(sorted, "$this$sorted");
        if (sorted instanceof Collection) {
            Collection collection = (Collection) sorted;
            if (collection.size() <= 1) {
                return toList(sorted);
            }
            Object[] array = collection.toArray(new Comparable[0]);
            if (array == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
            }
            Comparable[] comparableArr = (Comparable[]) array;
            ArraysKt___ArraysJvmKt.sort(comparableArr);
            return ArraysKt___ArraysJvmKt.asList(comparableArr);
        }
        List<T> mutableList = toMutableList(sorted);
        CollectionsKt__MutableCollectionsJVMKt.sort(mutableList);
        return mutableList;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static <T> List<T> sortedWith(@NotNull Iterable<? extends T> sortedWith, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(sortedWith, "$this$sortedWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        if (sortedWith instanceof Collection) {
            Collection collection = (Collection) sortedWith;
            if (collection.size() <= 1) {
                return toList(sortedWith);
            }
            Object[] array = collection.toArray(new Object[0]);
            if (array == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
            }
            ArraysKt___ArraysJvmKt.sortWith(array, comparator);
            return ArraysKt___ArraysJvmKt.asList(array);
        }
        List<T> mutableList = toMutableList(sortedWith);
        CollectionsKt__MutableCollectionsJVMKt.sortWith(mutableList, comparator);
        return mutableList;
    }

    @NotNull
    public static final <T, C extends Collection<? super T>> C toCollection(@NotNull Iterable<? extends T> toCollection, @NotNull C destination) {
        Intrinsics.checkParameterIsNotNull(toCollection, "$this$toCollection");
        Intrinsics.checkParameterIsNotNull(destination, "destination");
        Iterator<? extends T> it = toCollection.iterator();
        while (it.hasNext()) {
            destination.add(it.next());
        }
        return destination;
    }

    @NotNull
    public static final <T> HashSet<T> toHashSet(@NotNull Iterable<? extends T> toHashSet) {
        Intrinsics.checkParameterIsNotNull(toHashSet, "$this$toHashSet");
        return (HashSet) toCollection(toHashSet, new HashSet(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(toHashSet, 12))));
    }

    @NotNull
    public static <T> List<T> toList(@NotNull Iterable<? extends T> toList) {
        Intrinsics.checkParameterIsNotNull(toList, "$this$toList");
        if (toList instanceof Collection) {
            Collection collection = (Collection) toList;
            int size = collection.size();
            if (size == 0) {
                return CollectionsKt__CollectionsKt.emptyList();
            }
            if (size != 1) {
                return toMutableList(collection);
            }
            return CollectionsKt__CollectionsJVMKt.listOf(toList instanceof List ? ((List) toList).get(0) : toList.iterator().next());
        }
        return CollectionsKt__CollectionsKt.optimizeReadOnlyList(toMutableList(toList));
    }

    @NotNull
    public static final <T> List<T> toMutableList(@NotNull Iterable<? extends T> toMutableList) {
        Intrinsics.checkParameterIsNotNull(toMutableList, "$this$toMutableList");
        if (toMutableList instanceof Collection) {
            return toMutableList((Collection) toMutableList);
        }
        return (List) toCollection(toMutableList, new ArrayList());
    }

    @NotNull
    public static <T> List<T> toMutableList(@NotNull Collection<? extends T> toMutableList) {
        Intrinsics.checkParameterIsNotNull(toMutableList, "$this$toMutableList");
        return new ArrayList(toMutableList);
    }

    @NotNull
    public static <T> Set<T> toSet(@NotNull Iterable<? extends T> toSet) {
        Intrinsics.checkParameterIsNotNull(toSet, "$this$toSet");
        if (toSet instanceof Collection) {
            Collection collection = (Collection) toSet;
            int size = collection.size();
            if (size == 0) {
                return SetsKt__SetsKt.emptySet();
            }
            if (size != 1) {
                return (Set) toCollection(toSet, new LinkedHashSet(MapsKt__MapsKt.mapCapacity(collection.size())));
            }
            return SetsKt__SetsJVMKt.setOf(toSet instanceof List ? ((List) toSet).get(0) : toSet.iterator().next());
        }
        return SetsKt__SetsKt.optimizeReadOnlySet((Set) toCollection(toSet, new LinkedHashSet()));
    }

    @NotNull
    public static <T> List<T> distinct(@NotNull Iterable<? extends T> distinct) {
        Intrinsics.checkParameterIsNotNull(distinct, "$this$distinct");
        return toList(toMutableSet(distinct));
    }

    @NotNull
    public static <T> Set<T> intersect(@NotNull Iterable<? extends T> intersect, @NotNull Iterable<? extends T> other) {
        Intrinsics.checkParameterIsNotNull(intersect, "$this$intersect");
        Intrinsics.checkParameterIsNotNull(other, "other");
        Set<T> mutableSet = toMutableSet(intersect);
        CollectionsKt__MutableCollectionsKt.retainAll(mutableSet, other);
        return mutableSet;
    }

    @NotNull
    public static <T> Set<T> subtract(@NotNull Iterable<? extends T> subtract, @NotNull Iterable<? extends T> other) {
        Intrinsics.checkParameterIsNotNull(subtract, "$this$subtract");
        Intrinsics.checkParameterIsNotNull(other, "other");
        Set<T> mutableSet = toMutableSet(subtract);
        CollectionsKt__MutableCollectionsKt.removeAll(mutableSet, other);
        return mutableSet;
    }

    @NotNull
    public static <T> Set<T> toMutableSet(@NotNull Iterable<? extends T> toMutableSet) {
        Intrinsics.checkParameterIsNotNull(toMutableSet, "$this$toMutableSet");
        return toMutableSet instanceof Collection ? new LinkedHashSet((Collection) toMutableSet) : (Set) toCollection(toMutableSet, new LinkedHashSet());
    }

    @NotNull
    public static <T> Set<T> union(@NotNull Iterable<? extends T> union, @NotNull Iterable<? extends T> other) {
        Intrinsics.checkParameterIsNotNull(union, "$this$union");
        Intrinsics.checkParameterIsNotNull(other, "other");
        Set<T> mutableSet = toMutableSet(union);
        CollectionsKt__MutableCollectionsKt.addAll(mutableSet, other);
        return mutableSet;
    }

    @Nullable
    public static <T extends Comparable<? super T>> T min(@NotNull Iterable<? extends T> min) {
        Intrinsics.checkParameterIsNotNull(min, "$this$min");
        Iterator<? extends T> it = min.iterator();
        if (!it.hasNext()) {
            return null;
        }
        T next = it.next();
        while (it.hasNext()) {
            T next2 = it.next();
            if (next.compareTo(next2) > 0) {
                next = next2;
            }
        }
        return next;
    }

    @NotNull
    public static <T> List<T> minus(@NotNull Iterable<? extends T> minus, T t) {
        Intrinsics.checkParameterIsNotNull(minus, "$this$minus");
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(minus, 10));
        boolean z = false;
        for (T t2 : minus) {
            boolean z2 = true;
            if (!z && Intrinsics.areEqual(t2, t)) {
                z = true;
                z2 = false;
            }
            if (z2) {
                arrayList.add(t2);
            }
        }
        return arrayList;
    }

    @NotNull
    public static <T> List<T> plus(@NotNull Collection<? extends T> plus, T t) {
        Intrinsics.checkParameterIsNotNull(plus, "$this$plus");
        ArrayList arrayList = new ArrayList(plus.size() + 1);
        arrayList.addAll(plus);
        arrayList.add(t);
        return arrayList;
    }

    @NotNull
    public static <T> List<T> plus(@NotNull Collection<? extends T> plus, @NotNull Iterable<? extends T> elements) {
        Intrinsics.checkParameterIsNotNull(plus, "$this$plus");
        Intrinsics.checkParameterIsNotNull(elements, "elements");
        if (elements instanceof Collection) {
            Collection collection = (Collection) elements;
            ArrayList arrayList = new ArrayList(plus.size() + collection.size());
            arrayList.addAll(plus);
            arrayList.addAll(collection);
            return arrayList;
        }
        ArrayList arrayList2 = new ArrayList(plus);
        CollectionsKt__MutableCollectionsKt.addAll(arrayList2, elements);
        return arrayList2;
    }

    @NotNull
    public static final <T, A extends Appendable> A joinTo(@NotNull Iterable<? extends T> joinTo, @NotNull A buffer, @NotNull CharSequence separator, @NotNull CharSequence prefix, @NotNull CharSequence postfix, int i, @NotNull CharSequence truncated, @Nullable Function1<? super T, ? extends CharSequence> function1) throws IOException {
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

    public static /* synthetic */ String joinToString$default(Iterable iterable, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i, CharSequence charSequence4, Function1 function1, int i2, Object obj) {
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
        return joinToString(iterable, charSequence, charSequence5, charSequence6, i3, charSequence7, function1);
    }

    @NotNull
    public static final <T> String joinToString(@NotNull Iterable<? extends T> joinToString, @NotNull CharSequence separator, @NotNull CharSequence prefix, @NotNull CharSequence postfix, int i, @NotNull CharSequence truncated, @Nullable Function1<? super T, ? extends CharSequence> function1) {
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
    public static <T> Sequence<T> asSequence(@NotNull final Iterable<? extends T> asSequence) {
        Intrinsics.checkParameterIsNotNull(asSequence, "$this$asSequence");
        return new Sequence<T>() { // from class: kotlin.collections.CollectionsKt___CollectionsKt$asSequence$$inlined$Sequence$1
            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                return asSequence.iterator();
            }
        };
    }
}
