package kotlinx.coroutines.internal;

import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.MainCoroutineDispatcher;
import org.jetbrains.annotations.NotNull;

/* compiled from: MainDispatchers.kt */
/* loaded from: classes2.dex */
public final class MainDispatchersKt {
    @NotNull
    public static final MainCoroutineDispatcher tryCreateDispatcher(@NotNull MainDispatcherFactory tryCreateDispatcher, @NotNull List<? extends MainDispatcherFactory> factories) {
        Intrinsics.checkParameterIsNotNull(tryCreateDispatcher, "$this$tryCreateDispatcher");
        Intrinsics.checkParameterIsNotNull(factories, "factories");
        try {
            return tryCreateDispatcher.createDispatcher(factories);
        } catch (Throwable th) {
            return new MissingMainCoroutineDispatcher(th, tryCreateDispatcher.hintOnError());
        }
    }
}
