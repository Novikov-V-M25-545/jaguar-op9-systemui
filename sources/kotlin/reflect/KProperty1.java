package kotlin.reflect;

import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

/* compiled from: KProperty.kt */
/* loaded from: classes.dex */
public interface KProperty1<T, R> extends KProperty<R>, Function1<T, R> {

    /* compiled from: KProperty.kt */
    public interface Getter<T, R> extends Function1<T, R>, Function1 {
    }

    R get(T t);

    @NotNull
    Getter<T, R> getGetter();
}
