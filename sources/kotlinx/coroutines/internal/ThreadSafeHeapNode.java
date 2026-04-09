package kotlinx.coroutines.internal;

import org.jetbrains.annotations.Nullable;

/* compiled from: ThreadSafeHeap.common.kt */
/* loaded from: classes2.dex */
public interface ThreadSafeHeapNode {
    @Nullable
    ThreadSafeHeap<?> getHeap();

    int getIndex();

    void setHeap(@Nullable ThreadSafeHeap<?> threadSafeHeap);

    void setIndex(int i);
}
