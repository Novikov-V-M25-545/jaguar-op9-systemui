package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import androidx.annotation.Keep;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;

/* compiled from: IlluminationDrawable.kt */
@Keep
/* loaded from: classes.dex */
public final class IlluminationDrawable extends Drawable {
    private ValueAnimator backgroundAnimation;
    private int backgroundColor;
    private float cornerRadius;
    private float highlight;
    private int highlightColor;
    private int[] themeAttrs;
    private float[] tmpHsl = {0.0f, 0.0f, 0.0f};
    private Paint paint = new Paint();
    private final ArrayList<LightSourceDrawable> lightSources = new ArrayList<>();

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setBackgroundColor(int i) {
        if (i == this.backgroundColor) {
            return;
        }
        this.backgroundColor = i;
        animateBackground();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        float fWidth = getBounds().width();
        float fHeight = getBounds().height();
        float f = this.cornerRadius;
        canvas.drawRoundRect(0.0f, 0.0f, fWidth, fHeight, f, f, this.paint);
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(@NotNull Outline outline) {
        Intrinsics.checkParameterIsNotNull(outline, "outline");
        outline.setRoundRect(getBounds(), this.cornerRadius);
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
        int i = R.styleable.IlluminationDrawable_cornerRadius;
        if (typedArray.hasValue(i)) {
            this.cornerRadius = typedArray.getDimension(i, this.cornerRadius);
        }
        if (typedArray.hasValue(R.styleable.IlluminationDrawable_highlight)) {
            this.highlight = typedArray.getInteger(r0, 0) / 100.0f;
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.IlluminationDrawable.canApplyTheme():boolean");
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

    private final void animateBackground() {
        ColorUtils.colorToHSL(this.backgroundColor, this.tmpHsl);
        float[] fArr = this.tmpHsl;
        float f = fArr[2];
        float f2 = this.highlight;
        fArr[2] = MathUtils.constrain(f < 1.0f - f2 ? f + f2 : f - f2, 0.0f, 1.0f);
        final int color = this.paint.getColor();
        final int i = this.highlightColor;
        final int iHSLToColor = ColorUtils.HSLToColor(this.tmpHsl);
        ValueAnimator valueAnimator = this.backgroundAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(370L);
        valueAnimatorOfFloat.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.media.IlluminationDrawable$animateBackground$$inlined$apply$lambda$1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator it) {
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                Object animatedValue = it.getAnimatedValue();
                if (animatedValue != null) {
                    float fFloatValue = ((Float) animatedValue).floatValue();
                    this.this$0.paint.setColor(ColorUtils.blendARGB(color, this.this$0.backgroundColor, fFloatValue));
                    this.this$0.highlightColor = ColorUtils.blendARGB(i, iHSLToColor, fFloatValue);
                    Iterator it2 = this.this$0.lightSources.iterator();
                    while (it2.hasNext()) {
                        ((LightSourceDrawable) it2.next()).setHighlightColor(this.this$0.highlightColor);
                    }
                    this.this$0.invalidateSelf();
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.IlluminationDrawable$animateBackground$$inlined$apply$lambda$2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator) {
                this.this$0.backgroundAnimation = null;
            }
        });
        valueAnimatorOfFloat.start();
        this.backgroundAnimation = valueAnimatorOfFloat;
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(@Nullable ColorStateList colorStateList) {
        super.setTintList(colorStateList);
        if (colorStateList == null) {
            Intrinsics.throwNpe();
        }
        setBackgroundColor(colorStateList.getDefaultColor());
    }

    public final void registerLightSource(@NotNull View lightSource) {
        Intrinsics.checkParameterIsNotNull(lightSource, "lightSource");
        if (lightSource.getBackground() instanceof LightSourceDrawable) {
            ArrayList<LightSourceDrawable> arrayList = this.lightSources;
            Drawable background = lightSource.getBackground();
            if (background == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.LightSourceDrawable");
            }
            arrayList.add((LightSourceDrawable) background);
            return;
        }
        if (lightSource.getForeground() instanceof LightSourceDrawable) {
            ArrayList<LightSourceDrawable> arrayList2 = this.lightSources;
            Drawable foreground = lightSource.getForeground();
            if (foreground == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.LightSourceDrawable");
            }
            arrayList2.add((LightSourceDrawable) foreground);
        }
    }
}
