package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;

/* compiled from: CancellableContinuationImpl.kt */
/* loaded from: classes2.dex */
final class Active implements NotCompleted {
    public static final Active INSTANCE = new Active();

    @NotNull
    public String toString() {
        return "Active";
    }

    private Active() {
    }
}
