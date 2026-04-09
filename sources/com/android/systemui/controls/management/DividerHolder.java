package com.android.systemui.controls.management;

import android.view.View;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
/* loaded from: classes.dex */
final class DividerHolder extends Holder {
    private final View divider;
    private final View frame;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DividerHolder(@NotNull View view) {
        super(view, null);
        Intrinsics.checkParameterIsNotNull(view, "view");
        View viewRequireViewById = this.itemView.requireViewById(R.id.frame);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "itemView.requireViewById(R.id.frame)");
        this.frame = viewRequireViewById;
        View viewRequireViewById2 = this.itemView.requireViewById(R.id.divider);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "itemView.requireViewById(R.id.divider)");
        this.divider = viewRequireViewById2;
    }

    @Override // com.android.systemui.controls.management.Holder
    public void bindData(@NotNull ElementWrapper wrapper) {
        Intrinsics.checkParameterIsNotNull(wrapper, "wrapper");
        DividerWrapper dividerWrapper = (DividerWrapper) wrapper;
        this.frame.setVisibility(dividerWrapper.getShowNone() ? 0 : 8);
        this.divider.setVisibility(dividerWrapper.getShowDivider() ? 0 : 8);
    }
}
