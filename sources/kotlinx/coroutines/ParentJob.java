package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import org.jetbrains.annotations.NotNull;

/* compiled from: Job.kt */
/* loaded from: classes2.dex */
public interface ParentJob extends Job {
    @NotNull
    CancellationException getChildJobCancellationCause();
}
