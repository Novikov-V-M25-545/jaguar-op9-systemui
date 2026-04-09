package kotlinx.coroutines;

import org.jetbrains.annotations.Nullable;

/* compiled from: TimeSource.kt */
/* loaded from: classes2.dex */
public final class TimeSourceKt {

    @Nullable
    private static TimeSource timeSource;

    @Nullable
    public static final TimeSource getTimeSource() {
        return timeSource;
    }
}
