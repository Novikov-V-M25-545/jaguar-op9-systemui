package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.animation.Interpolator;
import androidx.annotation.Keep;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;

/* compiled from: LightSourceDrawable.kt */
@Keep
/* loaded from: classes.dex */
public final class LightSourceDrawable extends Drawable {
    private boolean active;
    private boolean pressed;
    private Animator rippleAnimation;
    private int[] themeAttrs;
    private final RippleData rippleData = new RippleData(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private Paint paint = new Paint();
    private int highlightColor = -1;

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -2;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(@NotNull Outline outline) {
        Intrinsics.checkParameterIsNotNull(outline, "outline");
    }

    @Override // android.graphics.drawable.Drawable
    public boolean hasFocusStateSpecified() {
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isProjected() {
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return true;
    }

    public final int getHighlightColor() {
        return this.highlightColor;
    }

    public final void setHighlightColor(int i) {
        if (this.highlightColor == i) {
            return;
        }
        this.highlightColor = i;
        invalidateSelf();
    }

    private final void setActive(boolean z) {
        if (z == this.active) {
            return;
        }
        this.active = z;
        if (z) {
            Animator animator = this.rippleAnimation;
            if (animator != null) {
                animator.cancel();
            }
            this.rippleData.setAlpha(1.0f);
            this.rippleData.setProgress(0.05f);
        } else {
            Animator animator2 = this.rippleAnimation;
            if (animator2 != null) {
                animator2.cancel();
            }
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.rippleData.getAlpha(), 0.0f);
            valueAnimatorOfFloat.setDuration(200L);
            valueAnimatorOfFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.media.LightSourceDrawable$active$$inlined$apply$lambda$1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator it) {
                    RippleData rippleData = this.this$0.rippleData;
                    Intrinsics.checkExpressionValueIsNotNull(it, "it");
                    Object animatedValue = it.getAnimatedValue();
                    if (animatedValue == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
                    }
                    rippleData.setAlpha(((Float) animatedValue).floatValue());
                    this.this$0.invalidateSelf();
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.LightSourceDrawable$active$$inlined$apply$lambda$2
                private boolean cancelled;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(@Nullable Animator animator3) {
                    this.cancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(@Nullable Animator animator3) {
                    if (this.cancelled) {
                        return;
                    }
                    this.this$0.rippleData.setProgress(0.0f);
                    this.this$0.rippleData.setAlpha(0.0f);
                    this.this$0.rippleAnimation = null;
                    this.this$0.invalidateSelf();
                }
            });
            valueAnimatorOfFloat.start();
            this.rippleAnimation = valueAnimatorOfFloat;
        }
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        float fLerp = MathUtils.lerp(this.rippleData.getMinSize(), this.rippleData.getMaxSize(), this.rippleData.getProgress());
        this.paint.setShader(new RadialGradient(this.rippleData.getX(), this.rippleData.getY(), fLerp, new int[]{ColorUtils.setAlphaComponent(this.highlightColor, (int) (this.rippleData.getAlpha() * 255)), 0}, LightSourceDrawableKt.GRADIENT_STOPS, Shader.TileMode.CLAMP));
        canvas.drawCircle(this.rippleData.getX(), this.rippleData.getY(), fLerp, this.paint);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(@NotNull Resources r, @NotNull XmlPullParser parser, @NotNull AttributeSet attrs, @Nullable Resources.Theme theme) {
        Intrinsics.checkParameterIsNotNull(r, "r");
        Intrinsics.checkParameterIsNotNull(parser, "parser");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.IlluminationDrawable);
        this.themeAttrs = a.extractThemeAttrs();
        Intrinsics.checkExpressionValueIsNotNull(a, "a");
        updateStateFromTypedArray(a);
        a.recycle();
    }

    private final void updateStateFromTypedArray(TypedArray typedArray) {
        int i = R.styleable.IlluminationDrawable_rippleMinSize;
        if (typedArray.hasValue(i)) {
            this.rippleData.setMinSize(typedArray.getDimension(i, 0.0f));
        }
        int i2 = R.styleable.IlluminationDrawable_rippleMaxSize;
        if (typedArray.hasValue(i2)) {
            this.rippleData.setMaxSize(typedArray.getDimension(i2, 0.0f));
        }
        if (typedArray.hasValue(R.styleable.IlluminationDrawable_highlight)) {
            this.rippleData.setHighlight(typedArray.getInteger(r0, 0) / 100.0f);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:8:0x000c  */
    @Override // android.graphics.drawable.Drawable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean canApplyTheme() {
        /*
            r1 = this;
            int[] r0 = r1.themeAttrs
            if (r0 == 0) goto Lc
            if (r0 != 0) goto L9
            kotlin.jvm.internal.Intrinsics.throwNpe()
        L9:
            int r0 = r0.length
            if (r0 > 0) goto L12
        Lc:
            boolean r1 = super.canApplyTheme()
            if (r1 == 0) goto L14
        L12:
            r1 = 1
            goto L15
        L14:
            r1 = 0
        L15:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.LightSourceDrawable.canApplyTheme():boolean");
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(@NotNull Resources.Theme t) {
        Intrinsics.checkParameterIsNotNull(t, "t");
        super.applyTheme(t);
        int[] iArr = this.themeAttrs;
        if (iArr != null) {
            TypedArray a = t.resolveAttributes(iArr, R.styleable.IlluminationDrawable);
            Intrinsics.checkExpressionValueIsNotNull(a, "a");
            updateStateFromTypedArray(a);
            a.recycle();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Color filters are not supported");
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        throw new UnsupportedOperationException("Alpha is not supported");
    }

    private final void illuminate() {
        this.rippleData.setAlpha(1.0f);
        invalidateSelf();
        Animator animator = this.rippleAnimation;
        if (animator != null) {
            animator.cancel();
        }
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat.setStartDelay(133L);
        valueAnimatorOfFloat.setDuration(800 - valueAnimatorOfFloat.getStartDelay());
        Interpolator interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
        valueAnimatorOfFloat.setInterpolator(interpolator);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.media.LightSourceDrawable$illuminate$$inlined$apply$lambda$1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator it) {
                RippleData rippleData = this.this$0.rippleData;
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                Object animatedValue = it.getAnimatedValue();
                if (animatedValue == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
                }
                rippleData.setAlpha(((Float) animatedValue).floatValue());
                this.this$0.invalidateSelf();
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(this.rippleData.getProgress(), 1.0f);
        valueAnimatorOfFloat2.setDuration(800L);
        valueAnimatorOfFloat2.setInterpolator(interpolator);
        valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.media.LightSourceDrawable$illuminate$$inlined$apply$lambda$2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator it) {
                RippleData rippleData = this.this$0.rippleData;
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                Object animatedValue = it.getAnimatedValue();
                if (animatedValue == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
                }
                rippleData.setProgress(((Float) animatedValue).floatValue());
                this.this$0.invalidateSelf();
            }
        });
        animatorSet.playTogether(valueAnimatorOfFloat, valueAnimatorOfFloat2);
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.LightSourceDrawable$illuminate$$inlined$apply$lambda$3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator2) {
                this.this$0.rippleData.setProgress(0.0f);
                this.this$0.rippleAnimation = null;
                this.this$0.invalidateSelf();
            }
        });
        animatorSet.start();
        this.rippleAnimation = animatorSet;
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspot(float f, float f2) {
        this.rippleData.setX(f);
        this.rippleData.setY(f2);
        if (this.active) {
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    @NotNull
    public Rect getDirtyBounds() {
        float fLerp = MathUtils.lerp(this.rippleData.getMinSize(), this.rippleData.getMaxSize(), this.rippleData.getProgress());
        Rect rect = new Rect((int) (this.rippleData.getX() - fLerp), (int) (this.rippleData.getY() - fLerp), (int) (this.rippleData.getX() + fLerp), (int) (this.rippleData.getY() + fLerp));
        rect.union(super.getDirtyBounds());
        return rect;
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onStateChange(@Nullable int[] iArr) {
        boolean zOnStateChange = super.onStateChange(iArr);
        if (iArr == null) {
            return zOnStateChange;
        }
        boolean z = this.pressed;
        boolean z2 = false;
        this.pressed = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        for (int i : iArr) {
            switch (i) {
                case android.R.attr.state_focused:
                    z4 = true;
                    break;
                case android.R.attr.state_enabled:
                    z3 = true;
                    break;
                case android.R.attr.state_pressed:
                    this.pressed = true;
                    break;
                case android.R.attr.state_hovered:
                    z5 = true;
                    break;
            }
        }
        if (z3 && (this.pressed || z4 || z5)) {
            z2 = true;
        }
        setActive(z2);
        if (z && !this.pressed) {
            illuminate();
        }
        return zOnStateChange;
    }
}
