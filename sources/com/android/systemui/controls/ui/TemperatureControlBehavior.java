package com.android.systemui.controls.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.controls.ui.ControlViewHolder;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TemperatureControlBehavior.kt */
/* loaded from: classes.dex */
public final class TemperatureControlBehavior implements Behavior {

    @NotNull
    public Drawable clipLayer;

    @NotNull
    public Control control;

    @NotNull
    public ControlViewHolder cvh;

    @Nullable
    private Behavior subBehavior;

    @NotNull
    public final Control getControl() {
        Control control = this.control;
        if (control == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        return control;
    }

    @NotNull
    public final ControlViewHolder getCvh() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        return controlViewHolder;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        this.cvh = cvh;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState cws, int i) throws Resources.NotFoundException {
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
        Control control3 = this.control;
        if (control3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        ControlTemplate controlTemplate = control3.getControlTemplate();
        if (controlTemplate == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.service.controls.templates.TemperatureControlTemplate");
        }
        final TemperatureControlTemplate temperatureControlTemplate = (TemperatureControlTemplate) controlTemplate;
        int currentActiveMode = temperatureControlTemplate.getCurrentActiveMode();
        ControlTemplate subTemplate = temperatureControlTemplate.getTemplate();
        if (Intrinsics.areEqual(subTemplate, ControlTemplate.getNoTemplateObject()) || Intrinsics.areEqual(subTemplate, ControlTemplate.getErrorTemplate())) {
            boolean z = (currentActiveMode == 0 || currentActiveMode == 1) ? false : true;
            Drawable drawable = this.clipLayer;
            if (drawable == null) {
                Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
            }
            drawable.setLevel(z ? 10000 : 0);
            ControlViewHolder controlViewHolder3 = this.cvh;
            if (controlViewHolder3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(controlViewHolder3, z, currentActiveMode, false, 4, null);
            ControlViewHolder controlViewHolder4 = this.cvh;
            if (controlViewHolder4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            controlViewHolder4.getLayout().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.TemperatureControlBehavior.bind.1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ControlActionCoordinator controlActionCoordinator = TemperatureControlBehavior.this.getCvh().getControlActionCoordinator();
                    ControlViewHolder cvh = TemperatureControlBehavior.this.getCvh();
                    String templateId = temperatureControlTemplate.getTemplateId();
                    Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
                    controlActionCoordinator.touch(cvh, templateId, TemperatureControlBehavior.this.getControl());
                }
            });
            return;
        }
        ControlViewHolder controlViewHolder5 = this.cvh;
        if (controlViewHolder5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        Behavior behavior = this.subBehavior;
        ControlViewHolder.Companion companion = ControlViewHolder.Companion;
        Control control4 = this.control;
        if (control4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        int status = control4.getStatus();
        Intrinsics.checkExpressionValueIsNotNull(subTemplate, "subTemplate");
        Control control5 = this.control;
        if (control5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        this.subBehavior = controlViewHolder5.bindBehavior(behavior, companion.findBehaviorClass(status, subTemplate, control5.getDeviceType()), currentActiveMode);
    }
}
