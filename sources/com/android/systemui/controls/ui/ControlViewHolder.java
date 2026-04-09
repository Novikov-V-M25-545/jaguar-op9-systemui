package com.android.systemui.controls.ui;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.service.controls.Control;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.StatelessTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleRangeTemplate;
import android.service.controls.templates.ToggleTemplate;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.ui.RenderInfo;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.List;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlViewHolder.kt */
/* loaded from: classes.dex */
public final class ControlViewHolder {
    private final GradientDrawable baseLayer;

    @Nullable
    private Behavior behavior;

    @NotNull
    private final DelayableExecutor bgExecutor;
    private final CanUseIconPredicate canUseIconPredicate;

    @NotNull
    private final ClipDrawable clipLayer;

    @NotNull
    private final Context context;

    @NotNull
    private final ControlActionCoordinator controlActionCoordinator;

    @NotNull
    private final ControlsController controlsController;
    private final int currentUserId;

    @NotNull
    public ControlWithState cws;

    @NotNull
    private final ImageView icon;
    private boolean isLoading;

    @Nullable
    private ControlAction lastAction;
    private Dialog lastChallengeDialog;

    @NotNull
    private final ViewGroup layout;
    private CharSequence nextStatusText;
    private final Function0<Unit> onDialogCancel;
    private ValueAnimator stateAnimator;
    private final TextView status;
    private Animator statusAnimator;

    @NotNull
    private final TextView subtitle;

    @NotNull
    private final TextView title;
    private final float toggleBackgroundIntensity;

    @NotNull
    private final DelayableExecutor uiExecutor;
    private boolean userInteractionInProgress;

    @Nullable
    private Dialog visibleDialog;
    public static final Companion Companion = new Companion(null);
    private static final Set<Integer> FORCE_PANEL_DEVICES = SetsKt__SetsKt.setOf((Object[]) new Integer[]{49, 50});
    private static final int[] ATTR_ENABLED = {R.attr.state_enabled};
    private static final int[] ATTR_DISABLED = {-16842910};

    public ControlViewHolder(@NotNull ViewGroup layout, @NotNull ControlsController controlsController, @NotNull DelayableExecutor uiExecutor, @NotNull DelayableExecutor bgExecutor, @NotNull ControlActionCoordinator controlActionCoordinator, int i) {
        Intrinsics.checkParameterIsNotNull(layout, "layout");
        Intrinsics.checkParameterIsNotNull(controlsController, "controlsController");
        Intrinsics.checkParameterIsNotNull(uiExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(controlActionCoordinator, "controlActionCoordinator");
        this.layout = layout;
        this.controlsController = controlsController;
        this.uiExecutor = uiExecutor;
        this.bgExecutor = bgExecutor;
        this.controlActionCoordinator = controlActionCoordinator;
        this.currentUserId = i;
        this.canUseIconPredicate = new CanUseIconPredicate(i);
        Context context = layout.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "layout.context");
        this.toggleBackgroundIntensity = context.getResources().getFraction(com.android.systemui.R.fraction.controls_toggle_bg_intensity, 1, 1);
        View viewRequireViewById = layout.requireViewById(com.android.systemui.R.id.icon);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "layout.requireViewById(R.id.icon)");
        this.icon = (ImageView) viewRequireViewById;
        View viewRequireViewById2 = layout.requireViewById(com.android.systemui.R.id.status);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "layout.requireViewById(R.id.status)");
        TextView textView = (TextView) viewRequireViewById2;
        this.status = textView;
        this.nextStatusText = "";
        View viewRequireViewById3 = layout.requireViewById(com.android.systemui.R.id.title);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "layout.requireViewById(R.id.title)");
        this.title = (TextView) viewRequireViewById3;
        View viewRequireViewById4 = layout.requireViewById(com.android.systemui.R.id.subtitle);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById4, "layout.requireViewById(R.id.subtitle)");
        this.subtitle = (TextView) viewRequireViewById4;
        Context context2 = layout.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "layout.getContext()");
        this.context = context2;
        this.onDialogCancel = new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlViewHolder$onDialogCancel$1
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                this.this$0.lastChallengeDialog = null;
            }
        };
        Drawable background = layout.getBackground();
        if (background == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
        }
        LayerDrawable layerDrawable = (LayerDrawable) background;
        layerDrawable.mutate();
        Drawable drawableFindDrawableByLayerId = layerDrawable.findDrawableByLayerId(com.android.systemui.R.id.clip_layer);
        if (drawableFindDrawableByLayerId == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.ClipDrawable");
        }
        ClipDrawable clipDrawable = (ClipDrawable) drawableFindDrawableByLayerId;
        this.clipLayer = clipDrawable;
        clipDrawable.setAlpha(0);
        Drawable drawableFindDrawableByLayerId2 = layerDrawable.findDrawableByLayerId(com.android.systemui.R.id.background);
        if (drawableFindDrawableByLayerId2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
        }
        this.baseLayer = (GradientDrawable) drawableFindDrawableByLayerId2;
        textView.setSelected(true);
    }

    @NotNull
    public final ViewGroup getLayout() {
        return this.layout;
    }

    @NotNull
    public final DelayableExecutor getUiExecutor() {
        return this.uiExecutor;
    }

    @NotNull
    public final ControlActionCoordinator getControlActionCoordinator() {
        return this.controlActionCoordinator;
    }

    /* compiled from: ControlViewHolder.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final KClass<? extends Behavior> findBehaviorClass(int i, @NotNull ControlTemplate template, int i2) {
            Intrinsics.checkParameterIsNotNull(template, "template");
            if (i != 1) {
                return Reflection.getOrCreateKotlinClass(StatusBehavior.class);
            }
            if (i2 == 50) {
                return Reflection.getOrCreateKotlinClass(TouchBehavior.class);
            }
            if (Intrinsics.areEqual(template, ControlTemplate.NO_TEMPLATE)) {
                return Reflection.getOrCreateKotlinClass(TouchBehavior.class);
            }
            if (template instanceof ToggleTemplate) {
                return Reflection.getOrCreateKotlinClass(ToggleBehavior.class);
            }
            if (template instanceof StatelessTemplate) {
                return Reflection.getOrCreateKotlinClass(TouchBehavior.class);
            }
            if (!(template instanceof ToggleRangeTemplate) && !(template instanceof RangeTemplate)) {
                return template instanceof TemperatureControlTemplate ? Reflection.getOrCreateKotlinClass(TemperatureControlBehavior.class) : Reflection.getOrCreateKotlinClass(DefaultBehavior.class);
            }
            return Reflection.getOrCreateKotlinClass(ToggleRangeBehavior.class);
        }
    }

    @NotNull
    public final TextView getTitle() {
        return this.title;
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }

    @NotNull
    public final ClipDrawable getClipLayer() {
        return this.clipLayer;
    }

    @NotNull
    public final ControlWithState getCws() {
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        return controlWithState;
    }

    @Nullable
    public final ControlAction getLastAction() {
        return this.lastAction;
    }

    public final void setLoading(boolean z) {
        this.isLoading = z;
    }

    public final void setVisibleDialog(@Nullable Dialog dialog) {
        this.visibleDialog = dialog;
    }

    public final int getDeviceType() {
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        Control control = controlWithState.getControl();
        if (control != null) {
            return control.getDeviceType();
        }
        ControlWithState controlWithState2 = this.cws;
        if (controlWithState2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        return controlWithState2.getCi().getDeviceType();
    }

    public final int getControlStatus() {
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        Control control = controlWithState.getControl();
        if (control != null) {
            return control.getStatus();
        }
        return 0;
    }

    @NotNull
    public final ControlTemplate getControlTemplate() {
        ControlTemplate controlTemplate;
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        Control control = controlWithState.getControl();
        if (control != null && (controlTemplate = control.getControlTemplate()) != null) {
            return controlTemplate;
        }
        ControlTemplate controlTemplate2 = ControlTemplate.NO_TEMPLATE;
        Intrinsics.checkExpressionValueIsNotNull(controlTemplate2, "ControlTemplate.NO_TEMPLATE");
        return controlTemplate2;
    }

    public final void setUserInteractionInProgress(boolean z) {
        this.userInteractionInProgress = z;
    }

    public final void bindData(@NotNull ControlWithState cws) {
        Intrinsics.checkParameterIsNotNull(cws, "cws");
        if (this.userInteractionInProgress) {
            return;
        }
        this.cws = cws;
        if (getControlStatus() == 0 || getControlStatus() == 2) {
            this.title.setText(cws.getCi().getControlTitle());
            this.subtitle.setText(cws.getCi().getControlSubtitle());
        } else {
            Control control = cws.getControl();
            if (control != null) {
                this.title.setText(control.getTitle());
                this.subtitle.setText(control.getSubtitle());
            }
        }
        if (cws.getControl() != null) {
            this.layout.setClickable(true);
            this.layout.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.controls.ui.ControlViewHolder$bindData$$inlined$let$lambda$1
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    this.this$0.getControlActionCoordinator().longPress(this.this$0);
                    return true;
                }
            });
        }
        this.isLoading = false;
        this.behavior = bindBehavior$default(this, this.behavior, Companion.findBehaviorClass(getControlStatus(), getControlTemplate(), getDeviceType()), 0, 4, null);
        updateContentDescription();
    }

    public final void actionResponse(int i) throws Resources.NotFoundException {
        ControlActionCoordinator controlActionCoordinator = this.controlActionCoordinator;
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        controlActionCoordinator.enableActionOnTouch(controlWithState.getCi().getControlId());
        boolean z = this.lastChallengeDialog != null;
        if (i == 0) {
            this.lastChallengeDialog = null;
            setErrorStatus();
            return;
        }
        if (i == 1) {
            this.lastChallengeDialog = null;
            return;
        }
        if (i == 2) {
            this.lastChallengeDialog = null;
            setErrorStatus();
            return;
        }
        if (i == 3) {
            Dialog dialogCreateConfirmationDialog = ChallengeDialogs.INSTANCE.createConfirmationDialog(this, this.onDialogCancel);
            this.lastChallengeDialog = dialogCreateConfirmationDialog;
            if (dialogCreateConfirmationDialog != null) {
                dialogCreateConfirmationDialog.show();
                return;
            }
            return;
        }
        if (i == 4) {
            Dialog dialogCreatePinDialog = ChallengeDialogs.INSTANCE.createPinDialog(this, false, z, this.onDialogCancel);
            this.lastChallengeDialog = dialogCreatePinDialog;
            if (dialogCreatePinDialog != null) {
                dialogCreatePinDialog.show();
                return;
            }
            return;
        }
        if (i != 5) {
            return;
        }
        Dialog dialogCreatePinDialog2 = ChallengeDialogs.INSTANCE.createPinDialog(this, true, z, this.onDialogCancel);
        this.lastChallengeDialog = dialogCreatePinDialog2;
        if (dialogCreatePinDialog2 != null) {
            dialogCreatePinDialog2.show();
        }
    }

    public final void dismiss() {
        Dialog dialog = this.lastChallengeDialog;
        if (dialog != null) {
            dialog.dismiss();
        }
        this.lastChallengeDialog = null;
        Dialog dialog2 = this.visibleDialog;
        if (dialog2 != null) {
            dialog2.dismiss();
        }
        this.visibleDialog = null;
    }

    public final void setErrorStatus() throws Resources.NotFoundException {
        final String string = this.context.getResources().getString(com.android.systemui.R.string.controls_error_failed);
        animateStatusChange(true, new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlViewHolder.setErrorStatus.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                ControlViewHolder controlViewHolder = ControlViewHolder.this;
                String text = string;
                Intrinsics.checkExpressionValueIsNotNull(text, "text");
                controlViewHolder.setStatusText(text, true);
            }
        });
    }

    private final void updateContentDescription() {
        ViewGroup viewGroup = this.layout;
        StringBuilder sb = new StringBuilder();
        sb.append(this.title.getText());
        sb.append(' ');
        sb.append(this.subtitle.getText());
        sb.append(' ');
        sb.append(this.status.getText());
        viewGroup.setContentDescription(sb.toString());
    }

    public final void action(@NotNull ControlAction action) {
        Intrinsics.checkParameterIsNotNull(action, "action");
        this.lastAction = action;
        ControlsController controlsController = this.controlsController;
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        ComponentName componentName = controlWithState.getComponentName();
        ControlWithState controlWithState2 = this.cws;
        if (controlWithState2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        controlsController.action(componentName, controlWithState2.getCi(), action);
    }

    public final boolean usePanel() {
        return FORCE_PANEL_DEVICES.contains(Integer.valueOf(getDeviceType())) || Intrinsics.areEqual(getControlTemplate(), ControlTemplate.NO_TEMPLATE);
    }

    public static /* synthetic */ Behavior bindBehavior$default(ControlViewHolder controlViewHolder, Behavior behavior, KClass kClass, int i, int i2, Object obj) {
        if ((i2 & 4) != 0) {
            i = 0;
        }
        return controlViewHolder.bindBehavior(behavior, kClass, i);
    }

    @NotNull
    public final Behavior bindBehavior(@Nullable Behavior behavior, @NotNull KClass<? extends Behavior> clazz, int i) {
        Intrinsics.checkParameterIsNotNull(clazz, "clazz");
        if (behavior == null || (!Intrinsics.areEqual(Reflection.getOrCreateKotlinClass(behavior.getClass()), clazz))) {
            behavior = (Behavior) JvmClassMappingKt.getJavaClass(clazz).newInstance();
            behavior.initialize(this);
            this.layout.setAccessibilityDelegate(null);
        }
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        behavior.bind(controlWithState, i);
        Intrinsics.checkExpressionValueIsNotNull(behavior, "behavior.also {\n        …nd(cws, offset)\n        }");
        return behavior;
    }

    public static /* synthetic */ void applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core$default(ControlViewHolder controlViewHolder, boolean z, int i, boolean z2, int i2, Object obj) throws Resources.NotFoundException {
        if ((i2 & 4) != 0) {
            z2 = true;
        }
        controlViewHolder.applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core(z, i, z2);
    }

    public final void applyRenderInfo$frameworks__base__packages__SystemUI__android_common__SystemUI_core(final boolean z, int i, boolean z2) throws Resources.NotFoundException {
        int deviceType = (getControlStatus() == 1 || getControlStatus() == 0) ? getDeviceType() : -1000;
        RenderInfo.Companion companion = RenderInfo.Companion;
        Context context = this.context;
        ControlWithState controlWithState = this.cws;
        if (controlWithState == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        final RenderInfo renderInfoLookup = companion.lookup(context, controlWithState.getComponentName(), deviceType, i);
        final ColorStateList colorStateList = this.context.getResources().getColorStateList(renderInfoLookup.getForeground(), this.context.getTheme());
        final CharSequence charSequence = this.nextStatusText;
        ControlWithState controlWithState2 = this.cws;
        if (controlWithState2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
        }
        final Control control = controlWithState2.getControl();
        if (Intrinsics.areEqual(charSequence, this.status.getText())) {
            z2 = false;
        }
        animateStatusChange(z2, new Function0<Unit>() { // from class: com.android.systemui.controls.ui.ControlViewHolder$applyRenderInfo$1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public /* bridge */ /* synthetic */ Unit invoke() {
                invoke2();
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2() {
                ControlViewHolder controlViewHolder = this.this$0;
                boolean z3 = z;
                CharSequence charSequence2 = charSequence;
                Drawable icon = renderInfoLookup.getIcon();
                ColorStateList fg = colorStateList;
                Intrinsics.checkExpressionValueIsNotNull(fg, "fg");
                controlViewHolder.updateStatusRow(z3, charSequence2, icon, fg, control);
            }
        });
        animateBackgroundChange(z2, z, renderInfoLookup.getEnabledBackground());
    }

    public final void setStatusTextSize(float f) {
        this.status.setTextSize(0, f);
    }

    public static /* synthetic */ void setStatusText$default(ControlViewHolder controlViewHolder, CharSequence charSequence, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        controlViewHolder.setStatusText(charSequence, z);
    }

    public final void setStatusText(@NotNull CharSequence text, boolean z) {
        Intrinsics.checkParameterIsNotNull(text, "text");
        if (z) {
            this.status.setAlpha(1.0f);
            this.status.setText(text);
            updateContentDescription();
        }
        this.nextStatusText = text;
    }

    private final void animateBackgroundChange(final boolean z, boolean z2, int i) throws Resources.NotFoundException {
        List listListOf;
        int color;
        ColorStateList customColor;
        Resources resources = this.context.getResources();
        int i2 = com.android.systemui.R.color.control_default_background;
        final int color2 = resources.getColor(i2, this.context.getTheme());
        final Ref$IntRef ref$IntRef = new Ref$IntRef();
        final Ref$IntRef ref$IntRef2 = new Ref$IntRef();
        if (z2) {
            ControlWithState controlWithState = this.cws;
            if (controlWithState == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cws");
            }
            Control control = controlWithState.getControl();
            if (control != null && (customColor = control.getCustomColor()) != null) {
                color = customColor.getColorForState(new int[]{R.attr.state_enabled}, customColor.getDefaultColor());
            } else {
                color = this.context.getResources().getColor(i, this.context.getTheme());
            }
            listListOf = CollectionsKt__CollectionsKt.listOf((Object[]) new Integer[]{Integer.valueOf(color), 255});
        } else {
            listListOf = CollectionsKt__CollectionsKt.listOf((Object[]) new Integer[]{Integer.valueOf(this.context.getResources().getColor(i2, this.context.getTheme())), 0});
        }
        ref$IntRef.element = ((Number) listListOf.get(0)).intValue();
        ref$IntRef2.element = ((Number) listListOf.get(1)).intValue();
        Drawable drawable = this.clipLayer.getDrawable();
        if (drawable == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
        }
        final GradientDrawable gradientDrawable = (GradientDrawable) drawable;
        int iBlendARGB = this.behavior instanceof ToggleRangeBehavior ? ColorUtils.blendARGB(color2, ref$IntRef.element, this.toggleBackgroundIntensity) : color2;
        ValueAnimator valueAnimator = this.stateAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (z) {
            ColorStateList color3 = gradientDrawable.getColor();
            final int defaultColor = color3 != null ? color3.getDefaultColor() : ref$IntRef.element;
            ColorStateList color4 = this.baseLayer.getColor();
            int defaultColor2 = color4 != null ? color4.getDefaultColor() : iBlendARGB;
            final float alpha = this.layout.getAlpha();
            ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(this.clipLayer.getAlpha(), ref$IntRef2.element);
            final int i3 = defaultColor2;
            final int i4 = iBlendARGB;
            final int i5 = iBlendARGB;
            valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.controls.ui.ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator it) {
                    GradientDrawable gradientDrawable2 = gradientDrawable;
                    Intrinsics.checkExpressionValueIsNotNull(it, "it");
                    Object animatedValue = it.getAnimatedValue();
                    if (animatedValue == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                    }
                    gradientDrawable2.setAlpha(((Integer) animatedValue).intValue());
                    gradientDrawable.setColor(ColorUtils.blendARGB(defaultColor, ref$IntRef.element, it.getAnimatedFraction()));
                    this.baseLayer.setColor(ColorUtils.blendARGB(i3, i4, it.getAnimatedFraction()));
                    this.getLayout().setAlpha(MathUtils.lerp(alpha, 1.0f, it.getAnimatedFraction()));
                }
            });
            valueAnimatorOfInt.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.ui.ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(@Nullable Animator animator) {
                    this.stateAnimator = null;
                }
            });
            valueAnimatorOfInt.setDuration(700L);
            valueAnimatorOfInt.setInterpolator(Interpolators.CONTROL_STATE);
            valueAnimatorOfInt.start();
            this.stateAnimator = valueAnimatorOfInt;
            return;
        }
        gradientDrawable.setAlpha(ref$IntRef2.element);
        gradientDrawable.setColor(ref$IntRef.element);
        this.baseLayer.setColor(iBlendARGB);
        this.layout.setAlpha(1.0f);
    }

    private final void animateStatusChange(boolean z, final Function0<Unit> function0) {
        Animator animator = this.statusAnimator;
        if (animator != null) {
            animator.cancel();
        }
        if (!z) {
            function0.invoke();
            return;
        }
        if (this.isLoading) {
            function0.invoke();
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.status, "alpha", 0.45f);
            objectAnimatorOfFloat.setRepeatMode(2);
            objectAnimatorOfFloat.setRepeatCount(-1);
            objectAnimatorOfFloat.setDuration(500L);
            objectAnimatorOfFloat.setInterpolator(Interpolators.LINEAR);
            objectAnimatorOfFloat.setStartDelay(900L);
            objectAnimatorOfFloat.start();
            this.statusAnimator = objectAnimatorOfFloat;
            return;
        }
        final ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(this.status, "alpha", 0.0f);
        objectAnimatorOfFloat2.setDuration(200L);
        Interpolator interpolator = Interpolators.LINEAR;
        objectAnimatorOfFloat2.setInterpolator(interpolator);
        objectAnimatorOfFloat2.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.ui.ControlViewHolder$animateStatusChange$$inlined$apply$lambda$1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator2) {
                function0.invoke();
            }
        });
        final ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(this.status, "alpha", 1.0f);
        objectAnimatorOfFloat3.setDuration(200L);
        objectAnimatorOfFloat3.setInterpolator(interpolator);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(objectAnimatorOfFloat2, objectAnimatorOfFloat3);
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.controls.ui.ControlViewHolder$animateStatusChange$$inlined$apply$lambda$2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator2) {
                this.this$0.status.setAlpha(1.0f);
                this.this$0.statusAnimator = null;
            }
        });
        animatorSet.start();
        this.statusAnimator = animatorSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateStatusRow(boolean z, CharSequence charSequence, Drawable drawable, ColorStateList colorStateList, Control control) {
        Icon customIcon;
        setEnabled(z);
        this.status.setText(charSequence);
        updateContentDescription();
        this.status.setTextColor(colorStateList);
        if (control != null && (customIcon = control.getCustomIcon()) != null) {
            if (!this.canUseIconPredicate.invoke((CanUseIconPredicate) customIcon).booleanValue()) {
                customIcon = null;
            }
            if (customIcon != null) {
                if (this.icon.getImageTintList() != null) {
                    this.icon.setImageTintList(null);
                }
                this.icon.setImageIcon(customIcon);
                return;
            }
        }
        if (drawable instanceof StateListDrawable) {
            if (this.icon.getDrawable() == null || !(this.icon.getDrawable() instanceof StateListDrawable)) {
                this.icon.setImageDrawable(drawable);
            }
            this.icon.setImageState(z ? ATTR_ENABLED : ATTR_DISABLED, true);
        } else {
            this.icon.setImageDrawable(drawable);
        }
        if (getDeviceType() != 52) {
            this.icon.setImageTintList(colorStateList);
        }
    }

    private final void setEnabled(boolean z) {
        this.status.setEnabled(z);
        this.icon.setEnabled(z);
    }
}
