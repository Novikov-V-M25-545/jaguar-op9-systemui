package kotlin.sequences;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

/* compiled from: Sequence.kt */
/* loaded from: classes.dex */
public interface Sequence<T> {
    @NotNull
    Iterator<T> iterator();
}
