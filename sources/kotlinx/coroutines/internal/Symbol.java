package kotlinx.coroutines.internal;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Symbol.kt */
/* loaded from: classes2.dex */
public final class Symbol {

    @NotNull
    private final String symbol;

    public Symbol(@NotNull String symbol) {
        Intrinsics.checkParameterIsNotNull(symbol, "symbol");
        this.symbol = symbol;
    }

    @NotNull
    public String toString() {
        return this.symbol;
    }
}
