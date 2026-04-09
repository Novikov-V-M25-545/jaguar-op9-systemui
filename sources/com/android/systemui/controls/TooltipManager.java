package com.android.systemui.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.recents.TriangleShape;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: TooltipManager.kt */
/* loaded from: classes.dex */
public final class TooltipManager {
    public static final Companion Companion = new Companion(null);
    private final View arrowView;
    private final boolean below;
    private final View dismissView;

    @NotNull
    private final ViewGroup layout;
    private final int maxTimesShown;
    private final String preferenceName;

    @NotNull
    private final Function1<Integer, Unit> preferenceStorer;
    private int shown;
    private final TextView textView;

    public TooltipManager(@NotNull final Context context, @NotNull String preferenceName, int i, boolean z) throws Resources.NotFoundException {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(preferenceName, "preferenceName");
        this.preferenceName = preferenceName;
        this.maxTimesShown = i;
        this.below = z;
        this.shown = Prefs.getInt(context, preferenceName, 0);
        View viewInflate = LayoutInflater.from(context).inflate(R.layout.controls_onboarding, (ViewGroup) null);
        if (viewInflate == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
        }
        ViewGroup viewGroup = (ViewGroup) viewInflate;
        this.layout = viewGroup;
        this.preferenceStorer = new Function1<Integer, Unit>() { // from class: com.android.systemui.controls.TooltipManager$preferenceStorer$1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
                invoke(num.intValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int i2) {
                Prefs.putInt(context, this.this$0.preferenceName, i2);
            }
        };
        viewGroup.setAlpha(0.0f);
        this.textView = (TextView) viewGroup.requireViewById(R.id.onboarding_text);
        View viewRequireViewById = viewGroup.requireViewById(R.id.dismiss);
        viewRequireViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.controls.TooltipManager$$special$$inlined$apply$lambda$1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.hide(true);
            }
        });
        this.dismissView = viewRequireViewById;
        View arrowView = viewGroup.requireViewById(R.id.arrow);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        int color = context.getResources().getColor(typedValue.resourceId, context.getTheme());
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.recents_onboarding_toast_arrow_corner_radius);
        ViewGroup.LayoutParams layoutParams = arrowView.getLayoutParams();
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.create(layoutParams.width, layoutParams.height, z));
        Paint arrowPaint = shapeDrawable.getPaint();
        Intrinsics.checkExpressionValueIsNotNull(arrowPaint, "arrowPaint");
        arrowPaint.setColor(color);
        arrowPaint.setPathEffect(new CornerPathEffect(dimensionPixelSize));
        arrowView.setBackground(shapeDrawable);
        this.arrowView = arrowView;
        if (z) {
            return;
        }
        viewGroup.removeView(arrowView);
        viewGroup.addView(arrowView);
        Intrinsics.checkExpressionValueIsNotNull(arrowView, "arrowView");
        ViewGroup.LayoutParams layoutParams2 = arrowView.getLayoutParams();
        if (layoutParams2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams2;
        marginLayoutParams.bottomMargin = marginLayoutParams.topMargin;
        marginLayoutParams.topMargin = 0;
    }

    public /* synthetic */ TooltipManager(Context context, String str, int i, boolean z, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, str, (i2 & 4) != 0 ? 2 : i, (i2 & 8) != 0 ? true : z);
    }

    /* compiled from: TooltipManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @NotNull
    public final ViewGroup getLayout() {
        return this.layout;
    }

    public final void show(int i, final int i2, final int i3) {
        if (shouldShow()) {
            this.textView.setText(i);
            int i4 = this.shown + 1;
            this.shown = i4;
            this.preferenceStorer.invoke(Integer.valueOf(i4));
            this.layout.post(new Runnable() { // from class: com.android.systemui.controls.TooltipManager.show.1
                @Override // java.lang.Runnable
                public final void run() {
                    TooltipManager.this.getLayout().getLocationOnScreen(new int[2]);
                    TooltipManager.this.getLayout().setTranslationX((i2 - r1[0]) - (TooltipManager.this.getLayout().getWidth() / 2));
                    TooltipManager.this.getLayout().setTranslationY((i3 - r1[1]) - (TooltipManager.this.below ? 0 : TooltipManager.this.getLayout().getHeight()));
                    if (TooltipManager.this.getLayout().getAlpha() == 0.0f) {
                        TooltipManager.this.getLayout().animate().alpha(1.0f).withLayer().setStartDelay(500L).setDuration(300L).setInterpolator(new DecelerateInterpolator()).start();
                    }
                }
            });
        }
    }

    public final void hide(final boolean z) {
        if (this.layout.getAlpha() == 0.0f) {
            return;
        }
        this.layout.post(new Runnable() { // from class: com.android.systemui.controls.TooltipManager.hide.1
            @Override // java.lang.Runnable
            public final void run() {
                if (z) {
                    TooltipManager.this.getLayout().animate().alpha(0.0f).withLayer().setStartDelay(0L).setDuration(100L).setInterpolator(new AccelerateInterpolator()).start();
                } else {
                    TooltipManager.this.getLayout().animate().cancel();
                    TooltipManager.this.getLayout().setAlpha(0.0f);
                }
            }
        });
    }

    private final boolean shouldShow() {
        return this.shown < this.maxTimesShown;
    }
}
