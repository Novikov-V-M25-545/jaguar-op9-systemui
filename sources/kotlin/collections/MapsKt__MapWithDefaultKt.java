package kotlin.collections;

import java.util.Map;
import java.util.NoSuchElementException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MapWithDefault.kt */
/* loaded from: classes.dex */
class MapsKt__MapWithDefaultKt {
    public static final <K, V> V getOrImplicitDefaultNullable(@NotNull Map<K, ? extends V> getOrImplicitDefault, K k) {
        Intrinsics.checkParameterIsNotNull(getOrImplicitDefault, "$this$getOrImplicitDefault");
        if (getOrImplicitDefault instanceof MapWithDefault) {
            return (V) ((MapWithDefault) getOrImplicitDefault).getOrImplicitDefault(k);
        }
        V v = getOrImplicitDefault.get(k);
        if (v != null || getOrImplicitDefault.containsKey(k)) {
            return v;
        }
        throw new NoSuchElementException("Key " + k + " is missing in the map.");
    }

    @NotNull
    public static <K, V> Map<K, V> withDefault(@NotNull Map<K, ? extends V> withDefault, @NotNull Function1<? super K, ? extends V> defaultValue) {
        Intrinsics.checkParameterIsNotNull(withDefault, "$this$withDefault");
        Intrinsics.checkParameterIsNotNull(defaultValue, "defaultValue");
        return withDefault instanceof MapWithDefault ? withDefault(((MapWithDefault) withDefault).getMap(), defaultValue) : new MapWithDefaultImpl(withDefault, defaultValue);
    }
}
