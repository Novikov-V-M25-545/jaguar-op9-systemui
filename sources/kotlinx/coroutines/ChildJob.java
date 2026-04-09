package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;

/* compiled from: Job.kt */
/* loaded from: classes2.dex */
public interface ChildJob extends Job {
    void parentCancelled(@NotNull ParentJob parentJob);
}
