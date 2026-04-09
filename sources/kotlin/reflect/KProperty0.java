package kotlin.reflect;

import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;

/* compiled from: KProperty.kt */
/* loaded from: classes.dex */
public interface KProperty0<R> extends KProperty<R>, Function0<R> {

    /* compiled from: KProperty.kt */
    public interface Getter<R> extends Function0<R>, Function0 {
    }

    R get();

    @NotNull
    Getter<R> getGetter();
}
