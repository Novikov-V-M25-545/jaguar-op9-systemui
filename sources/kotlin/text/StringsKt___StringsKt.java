package kotlin.text;

import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: _Strings.kt */
/* loaded from: classes2.dex */
class StringsKt___StringsKt extends StringsKt___StringsJvmKt {
    @NotNull
    public static final String drop(@NotNull String drop, int i) {
        Intrinsics.checkParameterIsNotNull(drop, "$this$drop");
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested character count " + i + " is less than zero.").toString());
        }
        String strSubstring = drop.substring(RangesKt___RangesKt.coerceAtMost(i, drop.length()));
        Intrinsics.checkExpressionValueIsNotNull(strSubstring, "(this as java.lang.String).substring(startIndex)");
        return strSubstring;
    }
}
