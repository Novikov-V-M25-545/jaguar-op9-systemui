package com.android.systemui.statusbar.notification.row;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.row.NotificationConversationInfo;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PriorityOnboardingDialogController.kt */
/* loaded from: classes.dex */
public final class PriorityOnboardingDialogController {
    private final long IMPORTANCE_ANIM_DELAY;
    private final long IMPORTANCE_ANIM_GROW_DURATION;
    private final long IMPORTANCE_ANIM_SHRINK_DELAY;
    private final long IMPORTANCE_ANIM_SHRINK_DURATION;
    private final Interpolator OVERSHOOT;

    @NotNull
    private final Drawable badge;

    @NotNull
    private final Context context;
    private Dialog dialog;

    @NotNull
    private final Drawable icon;
    private final boolean ignoresDnd;
    private final NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener;
    private final boolean showsAsBubble;

    @NotNull
    private final View view;
    private final int wmFlags;

    public PriorityOnboardingDialogController(@NotNull View view, @NotNull Context context, boolean z, boolean z2, @NotNull Drawable icon, @NotNull NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener, @NotNull Drawable badge) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(icon, "icon");
        Intrinsics.checkParameterIsNotNull(onConversationSettingsClickListener, "onConversationSettingsClickListener");
        Intrinsics.checkParameterIsNotNull(badge, "badge");
        this.view = view;
        this.context = context;
        this.ignoresDnd = z;
        this.showsAsBubble = z2;
        this.icon = icon;
        this.onConversationSettingsClickListener = onConversationSettingsClickListener;
        this.badge = badge;
        this.OVERSHOOT = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.4f);
        this.IMPORTANCE_ANIM_DELAY = 150L;
        this.IMPORTANCE_ANIM_GROW_DURATION = 250L;
        this.IMPORTANCE_ANIM_SHRINK_DURATION = 200L;
        this.IMPORTANCE_ANIM_SHRINK_DELAY = 25L;
        this.wmFlags = -2130444288;
    }

    public final void init() {
        initDialog();
    }

    public final void show() {
        Dialog dialog = this.dialog;
        if (dialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        dialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void done() {
        Prefs.putBoolean(this.context, "HasUserSeenPriorityOnboarding", true);
        Dialog dialog = this.dialog;
        if (dialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        dialog.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void settings() {
        Prefs.putBoolean(this.context, "HasUserSeenPriorityOnboarding", true);
        Dialog dialog = this.dialog;
        if (dialog == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        dialog.dismiss();
        NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener = this.onConversationSettingsClickListener;
        if (onConversationSettingsClickListener != null) {
            onConversationSettingsClickListener.onClick();
        }
    }

    /* compiled from: PriorityOnboardingDialogController.kt */
    public static final class Builder {
        private Drawable badge;
        private Context context;
        private Drawable icon;
        private boolean ignoresDnd;
        private NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener;
        private boolean showAsBubble;
        private View view;

        @NotNull
        public final Builder setView(@NotNull View v) {
            Intrinsics.checkParameterIsNotNull(v, "v");
            this.view = v;
            return this;
        }

        @NotNull
        public final Builder setContext(@NotNull Context c) {
            Intrinsics.checkParameterIsNotNull(c, "c");
            this.context = c;
            return this;
        }

        @NotNull
        public final Builder setIgnoresDnd(boolean z) {
            this.ignoresDnd = z;
            return this;
        }

        @NotNull
        public final Builder setShowsAsBubble(boolean z) {
            this.showAsBubble = z;
            return this;
        }

        @NotNull
        public final Builder setIcon(@NotNull Drawable draw) {
            Intrinsics.checkParameterIsNotNull(draw, "draw");
            this.icon = draw;
            return this;
        }

        @NotNull
        public final Builder setBadge(@NotNull Drawable badge) {
            Intrinsics.checkParameterIsNotNull(badge, "badge");
            this.badge = badge;
            return this;
        }

        @NotNull
        public final Builder setOnSettingsClick(@NotNull NotificationConversationInfo.OnConversationSettingsClickListener onClick) {
            Intrinsics.checkParameterIsNotNull(onClick, "onClick");
            this.onConversationSettingsClickListener = onClick;
            return this;
        }

        @NotNull
        public final PriorityOnboardingDialogController build() {
            View view = this.view;
            if (view == null) {
                Intrinsics.throwUninitializedPropertyAccessException("view");
            }
            Context context = this.context;
            if (context == null) {
                Intrinsics.throwUninitializedPropertyAccessException("context");
            }
            boolean z = this.ignoresDnd;
            boolean z2 = this.showAsBubble;
            Drawable drawable = this.icon;
            if (drawable == null) {
                Intrinsics.throwUninitializedPropertyAccessException("icon");
            }
            NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener = this.onConversationSettingsClickListener;
            if (onConversationSettingsClickListener == null) {
                Intrinsics.throwUninitializedPropertyAccessException("onConversationSettingsClickListener");
            }
            Drawable drawable2 = this.badge;
            if (drawable2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("badge");
            }
            return new PriorityOnboardingDialogController(view, context, z, z2, drawable, onConversationSettingsClickListener, drawable2);
        }
    }

    private final void initDialog() throws Resources.NotFoundException {
        Dialog dialog = new Dialog(this.context);
        this.dialog = dialog;
        if (dialog.getWindow() == null) {
            throw new IllegalStateException("Need a window for the onboarding dialog to show");
        }
        Dialog dialog2 = this.dialog;
        if (dialog2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        Window window = dialog2.getWindow();
        if (window != null) {
            window.requestFeature(1);
        }
        Dialog dialog3 = this.dialog;
        if (dialog3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        dialog3.setTitle(" ");
        Dialog dialog4 = this.dialog;
        if (dialog4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        dialog4.setContentView(this.view);
        dialog4.setCanceledOnTouchOutside(true);
        TextView textView = (TextView) dialog4.findViewById(R.id.done_button);
        if (textView != null) {
            textView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$$inlined$apply$lambda$1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.done();
                }
            });
        }
        TextView textView2 = (TextView) dialog4.findViewById(R.id.settings_button);
        if (textView2 != null) {
            textView2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$$inlined$apply$lambda$2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.settings();
                }
            });
        }
        ImageView imageView = (ImageView) dialog4.findViewById(R.id.conversation_icon);
        if (imageView != null) {
            imageView.setImageDrawable(this.icon);
        }
        ImageView imageView2 = (ImageView) dialog4.findViewById(R.id.icon);
        if (imageView2 != null) {
            imageView2.setImageDrawable(this.badge);
        }
        final ImageView mImportanceRingView = (ImageView) dialog4.findViewById(R.id.conversation_icon_badge_ring);
        final ImageView conversationIconBadgeBg = (ImageView) dialog4.findViewById(R.id.conversation_icon_badge_bg);
        Intrinsics.checkExpressionValueIsNotNull(mImportanceRingView, "mImportanceRingView");
        Drawable drawable = mImportanceRingView.getDrawable();
        if (drawable == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
        }
        final GradientDrawable gradientDrawable = (GradientDrawable) drawable;
        gradientDrawable.mutate();
        Intrinsics.checkExpressionValueIsNotNull(conversationIconBadgeBg, "conversationIconBadgeBg");
        Drawable drawable2 = conversationIconBadgeBg.getDrawable();
        if (drawable2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
        }
        final GradientDrawable gradientDrawable2 = (GradientDrawable) drawable2;
        gradientDrawable2.mutate();
        final int color = dialog4.getContext().getResources().getColor(android.R.color.car_action1);
        Context context = dialog4.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(android.R.dimen.docked_stack_divider_thickness);
        Context context2 = dialog4.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "context");
        int dimensionPixelSize2 = context2.getResources().getDimensionPixelSize(android.R.dimen.display_cutout_touchable_region_size);
        Context context3 = dialog4.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context3, "context");
        final int dimensionPixelSize3 = context3.getResources().getDimensionPixelSize(android.R.dimen.docked_stack_divider_insets) - (dimensionPixelSize * 2);
        Context context4 = dialog4.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context4, "context");
        final int dimensionPixelSize4 = context4.getResources().getDimensionPixelSize(android.R.dimen.config_minScrollbarTouchTarget);
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator animation) {
                Intrinsics.checkExpressionValueIsNotNull(animation, "animation");
                Object animatedValue = animation.getAnimatedValue();
                if (animatedValue == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                }
                int iIntValue = ((Integer) animatedValue).intValue();
                gradientDrawable.setStroke(iIntValue, color);
                int i = dimensionPixelSize3 + (iIntValue * 2);
                gradientDrawable.setSize(i, i);
                mImportanceRingView.invalidate();
            }
        };
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(0, dimensionPixelSize2);
        Intrinsics.checkExpressionValueIsNotNull(valueAnimatorOfInt, "ValueAnimator.ofInt(0, largeThickness)");
        valueAnimatorOfInt.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        valueAnimatorOfInt.setDuration(this.IMPORTANCE_ANIM_GROW_DURATION);
        valueAnimatorOfInt.addUpdateListener(animatorUpdateListener);
        ValueAnimator valueAnimatorOfInt2 = ValueAnimator.ofInt(dimensionPixelSize2, dimensionPixelSize);
        Intrinsics.checkExpressionValueIsNotNull(valueAnimatorOfInt2, "ValueAnimator.ofInt(larg…kness, standardThickness)");
        valueAnimatorOfInt2.setDuration(this.IMPORTANCE_ANIM_SHRINK_DURATION);
        valueAnimatorOfInt2.setStartDelay(this.IMPORTANCE_ANIM_SHRINK_DELAY);
        valueAnimatorOfInt2.setInterpolator(this.OVERSHOOT);
        valueAnimatorOfInt2.addUpdateListener(animatorUpdateListener);
        valueAnimatorOfInt2.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$1$3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(@Nullable Animator animator) {
                GradientDrawable gradientDrawable3 = gradientDrawable2;
                int i = dimensionPixelSize3;
                gradientDrawable3.setSize(i, i);
                conversationIconBadgeBg.invalidate();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator) {
                GradientDrawable gradientDrawable3 = gradientDrawable2;
                int i = dimensionPixelSize4;
                gradientDrawable3.setSize(i, i);
                conversationIconBadgeBg.invalidate();
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setStartDelay(this.IMPORTANCE_ANIM_DELAY);
        animatorSet.playSequentially(valueAnimatorOfInt, valueAnimatorOfInt2);
        Dialog dialog5 = this.dialog;
        if (dialog5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
        }
        int dimensionPixelSize5 = dialog5.getContext().getResources().getDimensionPixelSize(R.dimen.conversation_onboarding_bullet_gap_width);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(dialog4.getContext().getText(R.string.priority_onboarding_show_at_top_text), new BulletSpan(dimensionPixelSize5), 0);
        spannableStringBuilder.append((CharSequence) System.lineSeparator());
        spannableStringBuilder.append(dialog4.getContext().getText(R.string.priority_onboarding_show_avatar_text), new BulletSpan(dimensionPixelSize5), 0);
        if (this.showsAsBubble) {
            spannableStringBuilder.append((CharSequence) System.lineSeparator());
            spannableStringBuilder.append(dialog4.getContext().getText(R.string.priority_onboarding_appear_as_bubble_text), new BulletSpan(dimensionPixelSize5), 0);
        }
        if (this.ignoresDnd) {
            spannableStringBuilder.append((CharSequence) System.lineSeparator());
            spannableStringBuilder.append(dialog4.getContext().getText(R.string.priority_onboarding_ignores_dnd_text), new BulletSpan(dimensionPixelSize5), 0);
        }
        ((TextView) dialog4.findViewById(R.id.behaviors)).setText(spannableStringBuilder);
        Window window2 = dialog4.getWindow();
        if (window2 != null) {
            window2.setBackgroundDrawable(new ColorDrawable(0));
            window2.addFlags(this.wmFlags);
            window2.setType(2017);
            window2.setWindowAnimations(android.R.style.Animation.InputMethod);
            WindowManager.LayoutParams attributes = window2.getAttributes();
            attributes.format = -3;
            attributes.setTitle(PriorityOnboardingDialogController.class.getSimpleName());
            attributes.gravity = 81;
            WindowManager.LayoutParams attributes2 = window2.getAttributes();
            Intrinsics.checkExpressionValueIsNotNull(attributes2, "attributes");
            attributes.setFitInsetsTypes(attributes2.getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
            attributes.width = -1;
            attributes.height = -2;
            window2.setAttributes(attributes);
        }
        animatorSet.start();
    }
}
