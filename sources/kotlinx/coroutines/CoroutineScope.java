package kotlinx.coroutines;

import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;

/* compiled from: CoroutineScope.kt */
/* loaded from: classes2.dex */
public interface CoroutineScope {
    @NotNull
    CoroutineContext getCoroutineContext();
}
