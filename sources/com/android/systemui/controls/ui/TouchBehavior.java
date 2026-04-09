package com.android.systemui.controls.ui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.StatelessTemplate;
import android.view.View;
import com.android.systemui.R;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: TouchBehavior.kt */
/* loaded from: classes.dex */
public final class TouchBehavior implements Behavior {
    public static final Companion Companion = new Companion(null);

    @NotNull
    public Drawable clipLayer;

    @NotNull
    public Control control;

    @NotNull
    public ControlViewHolder cvh;
    private int lastColorOffset;
    private boolean statelessTouch;

    @NotNull
    public ControlTemplate template;

    @NotNull
    public final ControlTemplate getTemplate() {
        ControlTemplate controlTemplate = this.template;
        if (controlTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("template");
        }
        return controlTemplate;
    }

    @NotNull
    public final Control getControl() {
        Control control = this.control;
        if (control == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        return control;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean getEnabled() {
        return this.lastColorOffset > 0 || this.statelessTouch;
    }

    /* compiled from: TouchBehavior.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull final ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        this.cvh = cvh;
        cvh.getLayout().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.ui.TouchBehavior.initialize.1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws Resources.NotFoundException {
                ControlActionCoordinator controlActionCoordinator = cvh.getControlActionCoordinator();
                ControlViewHolder controlViewHolder = cvh;
                String templateId = TouchBehavior.this.getTemplate().getTemplateId();
                Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
                controlActionCoordinator.touch(controlViewHolder, templateId, TouchBehavior.this.getControl());
                if (TouchBehavior.this.getTemplate() instanceof StatelessTemplate) {
                    TouchBehavior.this.statelessTouch = true;
                    ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(cvh, TouchBehavior.this.getEnabled(), TouchBehavior.this.lastColorOffset, false, 4, null);
                    cvh.getUiExecutor().executeDelayed(new Runnable() { // from class: com.android.systemui.controls.ui.TouchBehavior.initialize.1.1
                        @Override // java.lang.Runnable
                        public final void run() throws Resources.NotFoundException {
                            TouchBehavior.this.statelessTouch = false;
                            AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                            ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(cvh, TouchBehavior.this.getEnabled(), TouchBehavior.this.lastColorOffset, false, 4, null);
                        }
                    }, 3000L);
                }
            }
        });
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState cws, int i) throws Resources.NotFoundException {
        Intrinsics.checkParameterIsNotNull(cws, "cws");
        Control control = cws.getControl();
        if (control == null) {
            Intrinsics.throwNpe();
        }
        this.control = control;
        this.lastColorOffset = i;
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
        Intrinsics.checkExpressionValueIsNotNull(controlTemplate, "control.getControlTemplate()");
        this.template = controlTemplate;
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
        drawableFindDrawableByLayerId.setLevel(getEnabled() ? 10000 : 0);
        ControlViewHolder controlViewHolder3 = this.cvh;
        if (controlViewHolder3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(controlViewHolder3, getEnabled(), i, false, 4, null);
    }
}
