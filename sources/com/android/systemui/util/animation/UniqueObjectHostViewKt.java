package com.android.systemui.util.animation;

import android.view.View;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: UniqueObjectHostView.kt */
/* loaded from: classes.dex */
public final class UniqueObjectHostViewKt {
    public static final boolean getRequiresRemeasuring(@NotNull View requiresRemeasuring) {
        Intrinsics.checkParameterIsNotNull(requiresRemeasuring, "$this$requiresRemeasuring");
        Object tag = requiresRemeasuring.getTag(R.id.requires_remeasuring);
        if (tag != null) {
            return tag.equals(Boolean.TRUE);
        }
        return false;
    }

    public static final void setRequiresRemeasuring(@NotNull View requiresRemeasuring, boolean z) {
        Intrinsics.checkParameterIsNotNull(requiresRemeasuring, "$this$requiresRemeasuring");
        requiresRemeasuring.setTag(R.id.requires_remeasuring, Boolean.valueOf(z));
    }
}
