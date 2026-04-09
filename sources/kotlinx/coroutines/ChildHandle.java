package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;

/* compiled from: Job.kt */
/* loaded from: classes2.dex */
public interface ChildHandle extends DisposableHandle {
    boolean childCancelled(@NotNull Throwable th);
}
