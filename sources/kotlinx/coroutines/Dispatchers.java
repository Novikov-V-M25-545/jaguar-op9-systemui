package kotlinx.coroutines;

import kotlinx.coroutines.internal.MainDispatcherLoader;
import kotlinx.coroutines.scheduling.DefaultScheduler;
import org.jetbrains.annotations.NotNull;

/* compiled from: Dispatchers.kt */
/* loaded from: classes2.dex */
public final class Dispatchers {
    public static final Dispatchers INSTANCE = new Dispatchers();

    @NotNull
    private static final CoroutineDispatcher Default = CoroutineContextKt.createDefaultDispatcher();

    @NotNull
    private static final CoroutineDispatcher Unconfined = Unconfined.INSTANCE;

    @NotNull
    private static final CoroutineDispatcher IO = DefaultScheduler.INSTANCE.getIO();

    private Dispatchers() {
    }

    @NotNull
    public static final CoroutineDispatcher getDefault() {
        return Default;
    }

    @NotNull
    public static final MainCoroutineDispatcher getMain() {
        return MainDispatcherLoader.dispatcher;
    }

    @NotNull
    public static final CoroutineDispatcher getIO() {
        return IO;
    }
}
