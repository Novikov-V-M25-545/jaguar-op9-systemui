package com.android.systemui.controls.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleTemplate;
import android.util.Log;
import android.view.View;
import com.android.systemui.R;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ToggleBehavior.kt */
/* loaded from: classes.dex */
public final class ToggleBehavior implements Behavior {

    @NotNull
    public Drawable clipLayer;

    @NotNull
    public Control control;

    @NotNull
    public ControlViewHolder cvh;

    @NotNull
    public ToggleTemplate template;

    @NotNull
    public final ToggleTemplate getTemplate() {
        ToggleTemplate toggleTemplate = this.template;
        if (toggleTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("template");
        }
        return toggleTemplate;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull final ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        this.cvh = cvh;
        cvh.getLayout().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.ToggleBehavior.initialize.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ControlActionCoordinator controlActionCoordinator = cvh.getControlActionCoordinator();
                ControlViewHolder controlViewHolder = cvh;
                String templateId = ToggleBehavior.this.getTemplate().getTemplateId();
                Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
                controlActionCoordinator.toggle(controlViewHolder, templateId, ToggleBehavior.this.getTemplate().isChecked());
            }
        });
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState cws, int i) throws Resources.NotFoundException {
        ToggleTemplate toggleTemplate;
        Intrinsics.checkParameterIsNotNull(cws, "cws");
        Control control = cws.getControl();
        if (control == null) {
            Intrinsics.throwNpe();
        }
        this.control = control;
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        Control control2 = this.control;
        if (control2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        CharSequence statusText = control2.getStatusText();
        Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
        ControlViewHolder.setStatusText$default(controlViewHolder, statusText, false, 2, null);
        Control control3 = this.control;
        if (control3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        ControlTemplate controlTemplate = control3.getControlTemplate();
        if (controlTemplate instanceof ToggleTemplate) {
            toggleTemplate = (ToggleTemplate) controlTemplate;
        } else {
            if (!(controlTemplate instanceof TemperatureControlTemplate)) {
                Log.e("ControlsUiController", "Unsupported template type: " + controlTemplate);
                return;
            }
            ControlTemplate template = ((TemperatureControlTemplate) controlTemplate).getTemplate();
            if (template == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.service.controls.templates.ToggleTemplate");
            }
            toggleTemplate = (ToggleTemplate) template;
        }
        this.template = toggleTemplate;
        ControlViewHolder controlViewHolder2 = this.cvh;
        if (controlViewHolder2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        Drawable background = controlViewHolder2.getLayout().getBackground();
        if (background == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
        }
        Drawable drawableFindDrawableByLayerId = ((LayerDrawable) background).findDrawableByLayerId(R.id.clip_layer);
        Intrinsics.checkExpressionValueIsNotNull(drawableFindDrawableByLayerId, "ld.findDrawableByLayerId(R.id.clip_layer)");
        this.clipLayer = drawableFindDrawableByLayerId;
        if (drawableFindDrawableByLayerId == null) {
            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
        }
        drawableFindDrawableByLayerId.setLevel(10000);
        ToggleTemplate toggleTemplate2 = this.template;
        if (toggleTemplate2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("template");
        }
        boolean zIsChecked = toggleTemplate2.isChecked();
        ControlViewHolder controlViewHolder3 = this.cvh;
        if (controlViewHolder3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(controlViewHolder3, zIsChecked, i, false, 4, null);
    }
}
