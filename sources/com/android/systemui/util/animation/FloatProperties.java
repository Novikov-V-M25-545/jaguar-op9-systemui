package com.android.systemui.util.animation;

import android.graphics.Rect;
import android.graphics.RectF;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: FloatProperties.kt */
/* loaded from: classes.dex */
public final class FloatProperties {
    public static final Companion Companion = new Companion(null);

    @NotNull
    public static final FloatPropertyCompat<RectF> RECTF_X;

    @NotNull
    public static final FloatPropertyCompat<RectF> RECTF_Y;

    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_HEIGHT;

    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_WIDTH;

    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_X;

    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_Y;

    /* compiled from: FloatProperties.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    static {
        final String str = "RectX";
        RECT_X = new FloatPropertyCompat<Rect>(str) { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_X$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@Nullable Rect rect, float f) {
                if (rect != null) {
                    rect.offsetTo((int) f, rect.top);
                }
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@Nullable Rect rect) {
                return rect != null ? rect.left : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
            }
        };
        final String str2 = "RectY";
        RECT_Y = new FloatPropertyCompat<Rect>(str2) { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_Y$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@Nullable Rect rect, float f) {
                if (rect != null) {
                    rect.offsetTo(rect.left, (int) f);
                }
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@Nullable Rect rect) {
                return rect != null ? rect.top : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
            }
        };
        final String str3 = "RectWidth";
        RECT_WIDTH = new FloatPropertyCompat<Rect>(str3) { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_WIDTH$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@NotNull Rect rect) {
                Intrinsics.checkParameterIsNotNull(rect, "rect");
                return rect.width();
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@NotNull Rect rect, float f) {
                Intrinsics.checkParameterIsNotNull(rect, "rect");
                rect.right = rect.left + ((int) f);
            }
        };
        final String str4 = "RectHeight";
        RECT_HEIGHT = new FloatPropertyCompat<Rect>(str4) { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_HEIGHT$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@NotNull Rect rect) {
                Intrinsics.checkParameterIsNotNull(rect, "rect");
                return rect.height();
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@NotNull Rect rect, float f) {
                Intrinsics.checkParameterIsNotNull(rect, "rect");
                rect.bottom = rect.top + ((int) f);
            }
        };
        final String str5 = "RectFX";
        RECTF_X = new FloatPropertyCompat<RectF>(str5) { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECTF_X$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@Nullable RectF rectF, float f) {
                if (rectF != null) {
                    rectF.offsetTo(f, rectF.top);
                }
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@Nullable RectF rectF) {
                return rectF != null ? rectF.left : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
            }
        };
        final String str6 = "RectFY";
        RECTF_Y = new FloatPropertyCompat<RectF>(str6) { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECTF_Y$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@Nullable RectF rectF, float f) {
                if (rectF != null) {
                    rectF.offsetTo(rectF.left, f);
                }
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@Nullable RectF rectF) {
                return rectF != null ? rectF.top : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
            }
        };
    }
}
