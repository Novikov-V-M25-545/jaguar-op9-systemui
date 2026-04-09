package com.android.systemui.controls.ui;

import android.content.res.Resources;
import android.service.controls.Control;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DefaultBehavior.kt */
/* loaded from: classes.dex */
public final class DefaultBehavior implements Behavior {

    @NotNull
    public ControlViewHolder cvh;

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        this.cvh = cvh;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState cws, int i) throws Resources.NotFoundException {
        CharSequence statusText;
        Intrinsics.checkParameterIsNotNull(cws, "cws");
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        Control control = cws.getControl();
        if (control == null || (statusText = control.getStatusText()) == null) {
            statusText = "";
        }
        ControlViewHolder.setStatusText$default(controlViewHolder, statusText, false, 2, null);
        ControlViewHolder controlViewHolder2 = this.cvh;
        if (controlViewHolder2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(controlViewHolder2, false, i, false, 4, null);
    }
}
