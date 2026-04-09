package kotlinx.coroutines;

import org.jetbrains.annotations.Nullable;

/* compiled from: JobSupport.kt */
/* loaded from: classes2.dex */
public class JobImpl extends JobSupport implements CompletableJob {
    private final boolean handlesException;

    @Override // kotlinx.coroutines.JobSupport
    public boolean getOnCancelComplete$kotlinx_coroutines_core() {
        return true;
    }

    public JobImpl(@Nullable Job job) {
        super(true);
        initParentJobInternal$kotlinx_coroutines_core(job);
        this.handlesException = handlesException();
    }

    @Override // kotlinx.coroutines.JobSupport
    public boolean getHandlesException$kotlinx_coroutines_core() {
        return this.handlesException;
    }

    private final boolean handlesException() {
        JobSupport jobSupport;
        ChildHandle childHandle = this.parentHandle;
        if (!(childHandle instanceof ChildHandleNode)) {
            childHandle = null;
        }
        ChildHandleNode childHandleNode = (ChildHandleNode) childHandle;
        if (childHandleNode != null && (jobSupport = (JobSupport) childHandleNode.job) != null) {
            while (!jobSupport.getHandlesException$kotlinx_coroutines_core()) {
                ChildHandle childHandle2 = jobSupport.parentHandle;
                if (!(childHandle2 instanceof ChildHandleNode)) {
                    childHandle2 = null;
                }
                ChildHandleNode childHandleNode2 = (ChildHandleNode) childHandle2;
                if (childHandleNode2 == null || (jobSupport = (JobSupport) childHandleNode2.job) == null) {
                }
            }
            return true;
        }
        return false;
    }
}
