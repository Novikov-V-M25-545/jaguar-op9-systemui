package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: EventLoop.kt */
/* loaded from: classes2.dex */
public final class BlockingEventLoop extends EventLoopImplBase {

    @NotNull
    private final Thread thread;

    @Override // kotlinx.coroutines.EventLoopImplPlatform
    @NotNull
    protected Thread getThread() {
        return this.thread;
    }

    public BlockingEventLoop(@NotNull Thread thread) {
        Intrinsics.checkParameterIsNotNull(thread, "thread");
        this.thread = thread;
    }
}
