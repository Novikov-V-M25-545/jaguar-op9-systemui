package com.android.systemui.controls.management;

import android.view.View;
import android.widget.TextView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
/* loaded from: classes.dex */
final class ZoneHolder extends Holder {
    private final TextView zone;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ZoneHolder(@NotNull View view) {
        super(view, null);
        Intrinsics.checkParameterIsNotNull(view, "view");
        View view2 = this.itemView;
        if (view2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.widget.TextView");
        }
        this.zone = (TextView) view2;
    }

    @Override // com.android.systemui.controls.management.Holder
    public void bindData(@NotNull ElementWrapper wrapper) {
        Intrinsics.checkParameterIsNotNull(wrapper, "wrapper");
        this.zone.setText(((ZoneNameWrapper) wrapper).getZoneName());
    }
}
