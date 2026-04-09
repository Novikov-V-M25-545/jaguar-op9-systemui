package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: EventLoop.kt */
/* loaded from: classes2.dex */
public final class EventLoopKt {
    @NotNull
    public static final EventLoop createEventLoop() {
        Thread threadCurrentThread = Thread.currentThread();
        Intrinsics.checkExpressionValueIsNotNull(threadCurrentThread, "Thread.currentThread()");
        return new BlockingEventLoop(threadCurrentThread);
    }
}
