package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleRangeTemplate;
import android.util.Log;
import android.util.MathUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import java.util.Arrays;
import java.util.IllegalFormatException;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ToggleRangeBehavior.kt */
/* loaded from: classes.dex */
public final class ToggleRangeBehavior implements Behavior {
    public static final Companion Companion = new Companion(null);

    @NotNull
    public Drawable clipLayer;
    private int colorOffset;

    @NotNull
    public Context context;

    @NotNull
    public Control control;

    @NotNull
    public ControlViewHolder cvh;
    private boolean isChecked;
    private boolean isToggleable;
    private ValueAnimator rangeAnimator;

    @NotNull
    public RangeTemplate rangeTemplate;

    @NotNull
    public String templateId;

    @NotNull
    private CharSequence currentStatusText = "";

    @NotNull
    private String currentRangeValue = "";

    @NotNull
    public final Drawable getClipLayer() {
        Drawable drawable = this.clipLayer;
        if (drawable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
        }
        return drawable;
    }

    @NotNull
    public final String getTemplateId() {
        String str = this.templateId;
        if (str == null) {
            Intrinsics.throwUninitializedPropertyAccessException("templateId");
        }
        return str;
    }

    @NotNull
    public final ControlViewHolder getCvh() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        return controlViewHolder;
    }

    @NotNull
    public final RangeTemplate getRangeTemplate() {
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        return rangeTemplate;
    }

    public final boolean isChecked() {
        return this.isChecked;
    }

    public final boolean isToggleable() {
        return this.isToggleable;
    }

    /* compiled from: ToggleRangeBehavior.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder cvh) {
        Intrinsics.checkParameterIsNotNull(cvh, "cvh");
        this.cvh = cvh;
        this.context = cvh.getContext();
        final ToggleRangeGestureListener toggleRangeGestureListener = new ToggleRangeGestureListener(this, cvh.getLayout());
        Context context = this.context;
        if (context == null) {
            Intrinsics.throwUninitializedPropertyAccessException("context");
        }
        final GestureDetector gestureDetector = new GestureDetector(context, toggleRangeGestureListener);
        cvh.getLayout().setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior.initialize.1
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(@NotNull View v, @NotNull MotionEvent e) {
                Intrinsics.checkParameterIsNotNull(v, "v");
                Intrinsics.checkParameterIsNotNull(e, "e");
                if (!gestureDetector.onTouchEvent(e) && e.getAction() == 1 && toggleRangeGestureListener.isDragging()) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    toggleRangeGestureListener.setDragging(false);
                    ToggleRangeBehavior.this.endUpdateRange();
                }
                return false;
            }
        });
    }

    private final void setup(ToggleRangeTemplate toggleRangeTemplate) {
        RangeTemplate range = toggleRangeTemplate.getRange();
        Intrinsics.checkExpressionValueIsNotNull(range, "template.getRange()");
        this.rangeTemplate = range;
        this.isToggleable = true;
        this.isChecked = toggleRangeTemplate.isChecked();
    }

    private final void setup(RangeTemplate rangeTemplate) {
        this.rangeTemplate = rangeTemplate;
        if (rangeTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        float currentValue = rangeTemplate.getCurrentValue();
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        this.isChecked = currentValue != rangeTemplate2.getMinValue();
    }

    private final boolean setupTemplate(ControlTemplate controlTemplate) {
        if (controlTemplate instanceof ToggleRangeTemplate) {
            setup((ToggleRangeTemplate) controlTemplate);
            return true;
        }
        if (controlTemplate instanceof RangeTemplate) {
            setup((RangeTemplate) controlTemplate);
            return true;
        }
        if (controlTemplate instanceof TemperatureControlTemplate) {
            ControlTemplate template = ((TemperatureControlTemplate) controlTemplate).getTemplate();
            Intrinsics.checkExpressionValueIsNotNull(template, "template.getTemplate()");
            return setupTemplate(template);
        }
        Log.e("ControlsUiController", "Unsupported template type: " + controlTemplate);
        return false;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState cws, int i) throws Resources.NotFoundException {
        Intrinsics.checkParameterIsNotNull(cws, "cws");
        Control control = cws.getControl();
        if (control == null) {
            Intrinsics.throwNpe();
        }
        this.control = control;
        this.colorOffset = i;
        if (control == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        CharSequence statusText = control.getStatusText();
        Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
        this.currentStatusText = statusText;
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        controlViewHolder.getLayout().setOnLongClickListener(null);
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
        Control control2 = this.control;
        if (control2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("control");
        }
        ControlTemplate template = control2.getControlTemplate();
        Intrinsics.checkExpressionValueIsNotNull(template, "template");
        if (setupTemplate(template)) {
            String templateId = template.getTemplateId();
            Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
            this.templateId = templateId;
            RangeTemplate rangeTemplate = this.rangeTemplate;
            if (rangeTemplate == null) {
                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            }
            updateRange(rangeToLevelValue(rangeTemplate.getCurrentValue()), this.isChecked, false);
            ControlViewHolder controlViewHolder3 = this.cvh;
            if (controlViewHolder3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            ControlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(controlViewHolder3, this.isChecked, i, false, 4, null);
            ControlViewHolder controlViewHolder4 = this.cvh;
            if (controlViewHolder4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            controlViewHolder4.getLayout().setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior.bind.1
                @Override // android.view.View.AccessibilityDelegate
                public boolean onRequestSendAccessibilityEvent(@NotNull ViewGroup host, @NotNull View child, @NotNull AccessibilityEvent event) {
                    Intrinsics.checkParameterIsNotNull(host, "host");
                    Intrinsics.checkParameterIsNotNull(child, "child");
                    Intrinsics.checkParameterIsNotNull(event, "event");
                    return true;
                }

                @Override // android.view.View.AccessibilityDelegate
                public void onInitializeAccessibilityNodeInfo(@NotNull View host, @NotNull AccessibilityNodeInfo info) {
                    Intrinsics.checkParameterIsNotNull(host, "host");
                    Intrinsics.checkParameterIsNotNull(info, "info");
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    float fLevelToRangeValue = ToggleRangeBehavior.this.levelToRangeValue(0);
                    ToggleRangeBehavior toggleRangeBehavior = ToggleRangeBehavior.this;
                    float fLevelToRangeValue2 = toggleRangeBehavior.levelToRangeValue(toggleRangeBehavior.getClipLayer().getLevel());
                    float fLevelToRangeValue3 = ToggleRangeBehavior.this.levelToRangeValue(10000);
                    double stepValue = ToggleRangeBehavior.this.getRangeTemplate().getStepValue();
                    int i2 = stepValue != Math.floor(stepValue) ? 1 : 0;
                    if (ToggleRangeBehavior.this.isChecked()) {
                        info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(i2, fLevelToRangeValue, fLevelToRangeValue3, fLevelToRangeValue2));
                    }
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
                }

                @Override // android.view.View.AccessibilityDelegate
                public boolean performAccessibilityAction(@NotNull View host, int i2, @Nullable Bundle bundle) throws Resources.NotFoundException {
                    boolean z;
                    Intrinsics.checkParameterIsNotNull(host, "host");
                    if (i2 == 16) {
                        if (ToggleRangeBehavior.this.isToggleable()) {
                            ToggleRangeBehavior.this.getCvh().getControlActionCoordinator().toggle(ToggleRangeBehavior.this.getCvh(), ToggleRangeBehavior.this.getTemplateId(), ToggleRangeBehavior.this.isChecked());
                            z = true;
                        }
                        z = false;
                    } else {
                        if (i2 == 32) {
                            ToggleRangeBehavior.this.getCvh().getControlActionCoordinator().longPress(ToggleRangeBehavior.this.getCvh());
                        } else {
                            if (i2 == AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.getId() && bundle != null && bundle.containsKey("android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE")) {
                                int iRangeToLevelValue = ToggleRangeBehavior.this.rangeToLevelValue(bundle.getFloat("android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE"));
                                ToggleRangeBehavior toggleRangeBehavior = ToggleRangeBehavior.this;
                                toggleRangeBehavior.updateRange(iRangeToLevelValue, toggleRangeBehavior.isChecked(), true);
                                ToggleRangeBehavior.this.endUpdateRange();
                            }
                            z = false;
                        }
                        z = true;
                    }
                    return z || super.performAccessibilityAction(host, i2, bundle);
                }
            });
        }
    }

    public final void beginUpdateRange() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        controlViewHolder.setUserInteractionInProgress(true);
        ControlViewHolder controlViewHolder2 = this.cvh;
        if (controlViewHolder2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        if (this.context == null) {
            Intrinsics.throwUninitializedPropertyAccessException("context");
        }
        controlViewHolder2.setStatusTextSize(r3.getResources().getDimensionPixelSize(R.dimen.control_status_expanded));
    }

    public final void updateRange(int i, boolean z, boolean z2) throws Resources.NotFoundException {
        int iMax = Math.max(0, Math.min(10000, i));
        Drawable drawable = this.clipLayer;
        if (drawable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
        }
        if (drawable.getLevel() == 0 && iMax > 0) {
            ControlViewHolder controlViewHolder = this.cvh;
            if (controlViewHolder == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            controlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core(z, this.colorOffset, false);
        }
        ValueAnimator valueAnimator = this.rangeAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (z2) {
            boolean z3 = iMax == 0 || iMax == 10000;
            Drawable drawable2 = this.clipLayer;
            if (drawable2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
            }
            if (drawable2.getLevel() != iMax) {
                ControlViewHolder controlViewHolder2 = this.cvh;
                if (controlViewHolder2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                }
                controlViewHolder2.getControlActionCoordinator().drag(z3);
                Drawable drawable3 = this.clipLayer;
                if (drawable3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                }
                drawable3.setLevel(iMax);
            }
        } else {
            Drawable drawable4 = this.clipLayer;
            if (drawable4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
            }
            if (iMax != drawable4.getLevel()) {
                int[] iArr = new int[2];
                ControlViewHolder controlViewHolder3 = this.cvh;
                if (controlViewHolder3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                }
                iArr[0] = controlViewHolder3.getClipLayer().getLevel();
                iArr[1] = iMax;
                ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(iArr);
                valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior$updateRange$$inlined$apply$lambda$1
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator it) {
                        ClipDrawable clipLayer = this.this$0.getCvh().getClipLayer();
                        Intrinsics.checkExpressionValueIsNotNull(it, "it");
                        Object animatedValue = it.getAnimatedValue();
                        if (animatedValue == null) {
                            throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                        }
                        clipLayer.setLevel(((Integer) animatedValue).intValue());
                    }
                });
                valueAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior$updateRange$$inlined$apply$lambda$2
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(@Nullable Animator animator) {
                        this.this$0.rangeAnimator = null;
                    }
                });
                valueAnimatorOfInt.setDuration(700L);
                valueAnimatorOfInt.setInterpolator(Interpolators.CONTROL_STATE);
                valueAnimatorOfInt.start();
                this.rangeAnimator = valueAnimatorOfInt;
            }
        }
        if (z) {
            float fLevelToRangeValue = levelToRangeValue(iMax);
            RangeTemplate rangeTemplate = this.rangeTemplate;
            if (rangeTemplate == null) {
                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            }
            this.currentRangeValue = format(rangeTemplate.getFormatString().toString(), "%.1f", fLevelToRangeValue);
            if (z2) {
                ControlViewHolder controlViewHolder4 = this.cvh;
                if (controlViewHolder4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                }
                controlViewHolder4.setStatusText(this.currentRangeValue, true);
                return;
            }
            ControlViewHolder controlViewHolder5 = this.cvh;
            if (controlViewHolder5 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
            }
            ControlViewHolder.setStatusText$default(controlViewHolder5, this.currentStatusText + ' ' + this.currentRangeValue, false, 2, null);
            return;
        }
        ControlViewHolder controlViewHolder6 = this.cvh;
        if (controlViewHolder6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlViewHolder.setStatusText$default(controlViewHolder6, this.currentStatusText, false, 2, null);
    }

    private final String format(String str, String str2, float f) {
        try {
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String str3 = String.format(str, Arrays.copyOf(new Object[]{Float.valueOf(findNearestStep(f))}, 1));
            Intrinsics.checkExpressionValueIsNotNull(str3, "java.lang.String.format(format, *args)");
            return str3;
        } catch (IllegalFormatException e) {
            Log.w("ControlsUiController", "Illegal format in range template", e);
            return Intrinsics.areEqual(str2, "") ? "" : format(str2, "", f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final float levelToRangeValue(int i) {
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        float minValue = rangeTemplate.getMinValue();
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        return MathUtils.constrainedMap(minValue, rangeTemplate2.getMaxValue(), 0, 10000, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int rangeToLevelValue(float f) {
        float f2 = 0;
        float f3 = 10000;
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        float minValue = rangeTemplate.getMinValue();
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        return (int) MathUtils.constrainedMap(f2, f3, minValue, rangeTemplate2.getMaxValue(), f);
    }

    public final void endUpdateRange() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        if (this.context == null) {
            Intrinsics.throwUninitializedPropertyAccessException("context");
        }
        controlViewHolder.setStatusTextSize(r2.getResources().getDimensionPixelSize(R.dimen.control_status_normal));
        ControlViewHolder controlViewHolder2 = this.cvh;
        if (controlViewHolder2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        controlViewHolder2.setStatusText(this.currentStatusText + ' ' + this.currentRangeValue, true);
        ControlViewHolder controlViewHolder3 = this.cvh;
        if (controlViewHolder3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        ControlActionCoordinator controlActionCoordinator = controlViewHolder3.getControlActionCoordinator();
        ControlViewHolder controlViewHolder4 = this.cvh;
        if (controlViewHolder4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        String templateId = rangeTemplate.getTemplateId();
        Intrinsics.checkExpressionValueIsNotNull(templateId, "rangeTemplate.getTemplateId()");
        Drawable drawable = this.clipLayer;
        if (drawable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
        }
        controlActionCoordinator.setValue(controlViewHolder4, templateId, findNearestStep(levelToRangeValue(drawable.getLevel())));
        ControlViewHolder controlViewHolder5 = this.cvh;
        if (controlViewHolder5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
        }
        controlViewHolder5.setUserInteractionInProgress(false);
    }

    public final float findNearestStep(float f) {
        float max_value = FloatCompanionObject.INSTANCE.getMAX_VALUE();
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate == null) {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        }
        float minValue = rangeTemplate.getMinValue();
        while (true) {
            RangeTemplate rangeTemplate2 = this.rangeTemplate;
            if (rangeTemplate2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            }
            if (minValue <= rangeTemplate2.getMaxValue()) {
                float fAbs = Math.abs(f - minValue);
                if (fAbs >= max_value) {
                    RangeTemplate rangeTemplate3 = this.rangeTemplate;
                    if (rangeTemplate3 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    }
                    return minValue - rangeTemplate3.getStepValue();
                }
                RangeTemplate rangeTemplate4 = this.rangeTemplate;
                if (rangeTemplate4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                }
                minValue += rangeTemplate4.getStepValue();
                max_value = fAbs;
            } else {
                RangeTemplate rangeTemplate5 = this.rangeTemplate;
                if (rangeTemplate5 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                }
                return rangeTemplate5.getMaxValue();
            }
        }
    }

    /* compiled from: ToggleRangeBehavior.kt */
    public final class ToggleRangeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean isDragging;
        final /* synthetic */ ToggleRangeBehavior this$0;

        @NotNull
        private final View v;

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onDown(@NotNull MotionEvent e) {
            Intrinsics.checkParameterIsNotNull(e, "e");
            return true;
        }

        public ToggleRangeGestureListener(@NotNull ToggleRangeBehavior toggleRangeBehavior, View v) {
            Intrinsics.checkParameterIsNotNull(v, "v");
            this.this$0 = toggleRangeBehavior;
            this.v = v;
        }

        public final boolean isDragging() {
            return this.isDragging;
        }

        public final void setDragging(boolean z) {
            this.isDragging = z;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public void onLongPress(@NotNull MotionEvent e) {
            Intrinsics.checkParameterIsNotNull(e, "e");
            if (this.isDragging) {
                return;
            }
            this.this$0.getCvh().getControlActionCoordinator().longPress(this.this$0.getCvh());
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(@NotNull MotionEvent e1, @NotNull MotionEvent e2, float f, float f2) throws Resources.NotFoundException {
            Intrinsics.checkParameterIsNotNull(e1, "e1");
            Intrinsics.checkParameterIsNotNull(e2, "e2");
            if (!this.isDragging) {
                this.v.getParent().requestDisallowInterceptTouchEvent(true);
                this.this$0.beginUpdateRange();
                this.isDragging = true;
            }
            int width = (int) (10000 * ((-f) / this.v.getWidth()));
            ToggleRangeBehavior toggleRangeBehavior = this.this$0;
            toggleRangeBehavior.updateRange(toggleRangeBehavior.getClipLayer().getLevel() + width, true, true);
            return true;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(@NotNull MotionEvent e) {
            Intrinsics.checkParameterIsNotNull(e, "e");
            if (!this.this$0.isToggleable()) {
                return false;
            }
            this.this$0.getCvh().getControlActionCoordinator().toggle(this.this$0.getCvh(), this.this$0.getTemplateId(), this.this$0.isChecked());
            return true;
        }
    }
}
