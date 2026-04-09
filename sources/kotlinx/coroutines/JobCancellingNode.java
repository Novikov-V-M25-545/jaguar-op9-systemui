package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;

/* compiled from: JobSupport.kt */
/* loaded from: classes2.dex */
public abstract class JobCancellingNode<J extends Job> extends JobNode<J> {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public JobCancellingNode(@NotNull J job) {
        super(job);
        Intrinsics.checkParameterIsNotNull(job, "job");
    }
}
