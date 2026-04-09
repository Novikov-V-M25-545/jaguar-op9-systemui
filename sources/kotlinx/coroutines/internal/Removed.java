package kotlinx.coroutines.internal;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LockFreeLinkedList.kt */
/* loaded from: classes2.dex */
final class Removed {

    @NotNull
    public final LockFreeLinkedListNode ref;

    public Removed(@NotNull LockFreeLinkedListNode ref) {
        Intrinsics.checkParameterIsNotNull(ref, "ref");
        this.ref = ref;
    }

    @NotNull
    public String toString() {
        return "Removed[" + this.ref + ']';
    }
}
