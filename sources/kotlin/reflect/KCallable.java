package kotlin.reflect;

import org.jetbrains.annotations.NotNull;

/* compiled from: KCallable.kt */
/* loaded from: classes.dex */
public interface KCallable<R> {
    R call(@NotNull Object... objArr);
}
