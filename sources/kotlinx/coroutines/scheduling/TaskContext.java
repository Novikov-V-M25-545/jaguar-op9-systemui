package kotlinx.coroutines.scheduling;

import org.jetbrains.annotations.NotNull;

/* compiled from: Tasks.kt */
/* loaded from: classes2.dex */
public interface TaskContext {
    void afterTask();

    @NotNull
    TaskMode getTaskMode();
}
