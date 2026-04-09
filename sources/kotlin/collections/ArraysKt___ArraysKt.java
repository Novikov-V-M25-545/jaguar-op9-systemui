package kotlin.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import kotlin.jvm.internal.ArrayIteratorKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: _Arrays.kt */
/* loaded from: classes.dex */
public class ArraysKt___ArraysKt extends ArraysKt___ArraysJvmKt {
    public static <T> boolean contains(@NotNull T[] contains, T t) {
        Intrinsics.checkParameterIsNotNull(contains, "$this$contains");
        return indexOf(contains, t) >= 0;
    }

    public static boolean contains(@NotNull int[] contains, int i) {
        Intrinsics.checkParameterIsNotNull(contains, "$this$contains");
        return indexOf(contains, i) >= 0;
    }

    public static final <T> int indexOf(@NotNull T[] indexOf, T t) {
        Intrinsics.checkParameterIsNotNull(indexOf, "$this$indexOf");
        int i = 0;
        if (t == null) {
            int length = indexOf.length;
            while (i < length) {
                if (indexOf[i] == null) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        int length2 = indexOf.length;
        while (i < length2) {
            if (Intrinsics.areEqual(t, indexOf[i])) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static final int indexOf(@NotNull int[] indexOf, int i) {
        Intrinsics.checkParameterIsNotNull(indexOf, "$this$indexOf");
        int length = indexOf.length;
        for (int i2 = 0; i2 < length; i2++) {
            if (i == indexOf[i2]) {
                return i2;
            }
        }
        return -1;
    }

    public static char single(@NotNull char[] single) {
        Intrinsics.checkParameterIsNotNull(single, "$this$single");
        int length = single.length;
        if (length == 0) {
            throw new NoSuchElementException("Array is empty.");
        }
        if (length == 1) {
            return single[0];
        }
        throw new IllegalArgumentException("Array has more than one element.");
    }

    @Nullable
    public static <T> T singleOrNull(@NotNull T[] singleOrNull) {
        Intrinsics.checkParameterIsNotNull(singleOrNull, "$this$singleOrNull");
        if (singleOrNull.length == 1) {
            return singleOrNull[0];
        }
        return null;
    }

    @NotNull
    public static <T> List<T> filterNotNull(@NotNull T[] filterNotNull) {
        Intrinsics.checkParameterIsNotNull(filterNotNull, "$this$filterNotNull");
        return (List) filterNotNullTo(filterNotNull, new ArrayList());
    }

    @NotNull
    public static final <C extends Collection<? super T>, T> C filterNotNullTo(@NotNull T[] filterNotNullTo, @NotNull C destination) {
        Intrinsics.checkParameterIsNotNull(filterNotNullTo, "$this$filterNotNullTo");
        Intrinsics.checkParameterIsNotNull(destination, "destination");
        for (T t : filterNotNullTo) {
            if (t != null) {
                destination.add(t);
            }
        }
        return destination;
    }

    @NotNull
    public static final <T> T[] sortedArrayWith(@NotNull T[] sortedArrayWith, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(sortedArrayWith, "$this$sortedArrayWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        if (sortedArrayWith.length == 0) {
            return sortedArrayWith;
        }
        T[] tArr = (T[]) Arrays.copyOf(sortedArrayWith, sortedArrayWith.length);
        Intrinsics.checkExpressionValueIsNotNull(tArr, "java.util.Arrays.copyOf(this, size)");
        ArraysKt___ArraysJvmKt.sortWith(tArr, comparator);
        return tArr;
    }

    @NotNull
    public static <T> List<T> sortedWith(@NotNull T[] sortedWith, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(sortedWith, "$this$sortedWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        return ArraysKt___ArraysJvmKt.asList(sortedArrayWith(sortedWith, comparator));
    }

    @NotNull
    public static final <T, C extends Collection<? super T>> C toCollection(@NotNull T[] toCollection, @NotNull C destination) {
        Intrinsics.checkParameterIsNotNull(toCollection, "$this$toCollection");
        Intrinsics.checkParameterIsNotNull(destination, "destination");
        for (T t : toCollection) {
            destination.add(t);
        }
        return destination;
    }

    @NotNull
    public static <T> List<T> toMutableList(@NotNull T[] toMutableList) {
        Intrinsics.checkParameterIsNotNull(toMutableList, "$this$toMutableList");
        return new ArrayList(CollectionsKt__CollectionsKt.asCollection(toMutableList));
    }

    @NotNull
    public static List<Integer> toMutableList(@NotNull int[] toMutableList) {
        Intrinsics.checkParameterIsNotNull(toMutableList, "$this$toMutableList");
        ArrayList arrayList = new ArrayList(toMutableList.length);
        for (int i : toMutableList) {
            arrayList.add(Integer.valueOf(i));
        }
        return arrayList;
    }

    @NotNull
    public static <T> Set<T> toSet(@NotNull T[] toSet) {
        Intrinsics.checkParameterIsNotNull(toSet, "$this$toSet");
        int length = toSet.length;
        if (length == 0) {
            return SetsKt__SetsKt.emptySet();
        }
        if (length == 1) {
            return SetsKt__SetsJVMKt.setOf(toSet[0]);
        }
        return (Set) toCollection(toSet, new LinkedHashSet(MapsKt__MapsKt.mapCapacity(toSet.length)));
    }

    @NotNull
    public static <T> Sequence<T> asSequence(@NotNull final T[] asSequence) {
        Intrinsics.checkParameterIsNotNull(asSequence, "$this$asSequence");
        return asSequence.length == 0 ? SequencesKt__SequencesKt.emptySequence() : new Sequence<T>() { // from class: kotlin.collections.ArraysKt___ArraysKt$asSequence$$inlined$Sequence$1
            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                return ArrayIteratorKt.iterator(asSequence);
            }
        };
    }
}
