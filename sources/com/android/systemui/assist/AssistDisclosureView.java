package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.assist.AssistDisclosureView;
import com.android.systemui.util.leak.RotationUtils;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AssistDisclosureView.kt */
/* loaded from: classes.dex */
public final class AssistDisclosureView extends View implements ValueAnimator.AnimatorUpdateListener {
    static final /* synthetic */ KProperty[] $$delegatedProperties = {Reflection.property1(new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(AssistDisclosureView.class), "path", "getPath()Landroid/graphics/Path;")), Reflection.property1(new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(AssistDisclosureView.class), "shadowPath", "getShadowPath()Landroid/graphics/Path;"))};
    public static final Companion Companion = new Companion(null);
    private final ValueAnimator alphaInAnimator;
    private final ValueAnimator alphaOutAnimator;
    private final AnimatorSet animator;

    @Nullable
    private OnFinishedListener onFinishedListener;
    private final Paint paint;
    private int paintAlpha;
    private final Lazy path$delegate;
    private final float radius;
    private final float radiusBottom;
    private final float radiusTop;
    private final Paint shadowPaint;
    private final Lazy shadowPath$delegate;
    private final float shadowThickness;
    private final PorterDuffXfermode srcMode;
    private final float thickness;

    /* compiled from: AssistDisclosureView.kt */
    public interface OnFinishedListener {
        void onFinished();
    }

    private final Path getPath() {
        Lazy lazy = this.path$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (Path) lazy.getValue();
    }

    private final Path getShadowPath() {
        Lazy lazy = this.shadowPath$delegate;
        KProperty kProperty = $$delegatedProperties[1];
        return (Path) lazy.getValue();
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AssistDisclosureView(@NotNull Context context) throws Resources.NotFoundException {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
        float dimension = getResources().getDimension(R.dimen.assist_disclosure_thickness);
        this.thickness = dimension;
        float dimension2 = getResources().getDimension(R.dimen.assist_disclosure_shadow_thickness);
        this.shadowThickness = dimension2;
        this.radius = getResources().getDimension(android.R.dimen.navigation_bar_width);
        this.radiusTop = getResources().getDimension(android.R.dimen.notification_action_disabled_alpha);
        this.radiusBottom = getResources().getDimension(android.R.dimen.navigation_edge_action_progress_threshold);
        PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.srcMode = porterDuffXfermode;
        Paint paint = new Paint();
        paint.setColor(-1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dimension);
        paint.setXfermode(porterDuffXfermode);
        paint.setAntiAlias(true);
        this.paint = paint;
        Paint paint2 = new Paint();
        paint2.setColor(-12303292);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(dimension2);
        paint2.setXfermode(porterDuffXfermode);
        paint2.setAntiAlias(true);
        this.shadowPaint = paint2;
        this.path$delegate = LazyKt__LazyJVMKt.lazy(new Function0<Path>() { // from class: com.android.systemui.assist.AssistDisclosureView$path$2
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            @NotNull
            public final Path invoke() {
                AssistDisclosureView assistDisclosureView = this.this$0;
                return assistDisclosureView.createPath(assistDisclosureView.thickness);
            }
        });
        this.shadowPath$delegate = LazyKt__LazyJVMKt.lazy(new Function0<Path>() { // from class: com.android.systemui.assist.AssistDisclosureView$shadowPath$2
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            @NotNull
            public final Path invoke() {
                AssistDisclosureView assistDisclosureView = this.this$0;
                return assistDisclosureView.createPath(assistDisclosureView.thickness + this.this$0.shadowThickness);
            }
        });
        ValueAnimator duration = ValueAnimator.ofInt(0, 222).setDuration(400L);
        duration.addUpdateListener(this);
        Interpolator interpolator = Interpolators.CUSTOM_40_40;
        duration.setInterpolator(interpolator);
        Intrinsics.checkExpressionValueIsNotNull(duration, "ValueAnimator\n        .o…s.CUSTOM_40_40)\n        }");
        this.alphaInAnimator = duration;
        ValueAnimator duration2 = ValueAnimator.ofInt(222, 0).setDuration(300L);
        duration2.addUpdateListener(this);
        duration2.setInterpolator(interpolator);
        Intrinsics.checkExpressionValueIsNotNull(duration2, "ValueAnimator\n        .o…s.CUSTOM_40_40)\n        }");
        this.alphaOutAnimator = duration2;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(duration).before(duration2);
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.assist.AssistDisclosureView$$special$$inlined$apply$lambda$1
            private boolean cancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(@NotNull Animator animation) {
                Intrinsics.checkParameterIsNotNull(animation, "animation");
                this.cancelled = false;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(@NotNull Animator animation) {
                Intrinsics.checkParameterIsNotNull(animation, "animation");
                this.cancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@NotNull Animator animation) {
                AssistDisclosureView.OnFinishedListener onFinishedListener;
                Intrinsics.checkParameterIsNotNull(animation, "animation");
                if (this.cancelled || (onFinishedListener = this.this$0.getOnFinishedListener()) == null) {
                    return;
                }
                onFinishedListener.onFinished();
            }
        });
        this.animator = animatorSet;
    }

    @Nullable
    public final OnFinishedListener getOnFinishedListener() {
        return this.onFinishedListener;
    }

    public final void setOnFinishedListener(@Nullable OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
        sendAccessibilityEvent(16777216);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.animator.cancel();
        this.paintAlpha = 0;
    }

    @Override // android.view.View
    protected void onDraw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        this.shadowPaint.setAlpha(this.paintAlpha / 4);
        this.paint.setAlpha(this.paintAlpha);
        canvas.drawPath(getShadowPath(), this.shadowPaint);
        canvas.drawPath(getPath(), this.paint);
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(@NotNull ValueAnimator animation) {
        Intrinsics.checkParameterIsNotNull(animation, "animation");
        ValueAnimator valueAnimator = this.alphaOutAnimator;
        if (animation == valueAnimator) {
            Object animatedValue = valueAnimator.getAnimatedValue();
            if (animatedValue == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
            }
            this.paintAlpha = ((Integer) animatedValue).intValue();
        } else {
            ValueAnimator valueAnimator2 = this.alphaInAnimator;
            if (animation == valueAnimator2) {
                Object animatedValue2 = valueAnimator2.getAnimatedValue();
                if (animatedValue2 == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                }
                this.paintAlpha = ((Integer) animatedValue2).intValue();
            }
        }
        invalidate();
    }

    private final void startAnimation() {
        this.animator.cancel();
        this.animator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Path createPath(float f) {
        float f2 = f / 2.0f;
        RectF rectF = new RectF(f2, f2, getWidth() - f2, getHeight() - f2);
        Path path = new Path();
        path.addRoundRect(rectF, resolveCorners(), Path.Direction.CW);
        return path;
    }

    private final float[] resolveCorners() {
        Companion companion = Companion;
        float fFallbackTo = companion.fallbackTo(this.radiusTop, this.radius);
        float fFallbackTo2 = companion.fallbackTo(this.radiusTop, this.radius);
        float fFallbackTo3 = companion.fallbackTo(this.radiusBottom, this.radius);
        float fFallbackTo4 = companion.fallbackTo(this.radiusBottom, this.radius);
        int exactRotation = RotationUtils.getExactRotation(getContext());
        if (exactRotation == 1) {
            fFallbackTo2 = companion.fallbackTo(this.radiusBottom, this.radius);
            fFallbackTo3 = companion.fallbackTo(this.radiusTop, this.radius);
        } else if (exactRotation == 2) {
            fFallbackTo = companion.fallbackTo(this.radiusBottom, this.radius);
            fFallbackTo4 = companion.fallbackTo(this.radiusTop, this.radius);
        } else if (exactRotation == 3) {
            fFallbackTo = companion.fallbackTo(this.radiusBottom, this.radius);
            fFallbackTo2 = companion.fallbackTo(this.radiusBottom, this.radius);
            fFallbackTo3 = companion.fallbackTo(this.radiusTop, this.radius);
            fFallbackTo4 = companion.fallbackTo(this.radiusTop, this.radius);
        }
        return new float[]{fFallbackTo, fFallbackTo, fFallbackTo2, fFallbackTo2, fFallbackTo4, fFallbackTo4, fFallbackTo3, fFallbackTo3};
    }

    /* compiled from: AssistDisclosureView.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final float fallbackTo(float f, float f2) {
            Float fValueOf = Float.valueOf(f);
            if (fValueOf.floatValue() == 0.0f) {
                fValueOf = null;
            }
            return fValueOf != null ? fValueOf.floatValue() : f2;
        }
    }
}
