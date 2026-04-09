package com.android.keyguard.clock;

import android.graphics.Color;
import android.util.MathUtils;
import org.jetbrains.annotations.Nullable;

/* compiled from: ClockPalette.kt */
/* loaded from: classes.dex */
public final class ClockPalette {
    private float darkAmount;
    private int accentPrimary = -1;
    private int accentSecondaryLight = -1;
    private int accentSecondaryDark = -16777216;
    private final float[] lightHSV = new float[3];
    private final float[] darkHSV = new float[3];
    private final float[] hsv = new float[3];

    public final int getPrimaryColor() {
        return this.accentPrimary;
    }

    public final int getSecondaryColor() {
        Color.colorToHSV(this.accentSecondaryLight, this.lightHSV);
        Color.colorToHSV(this.accentSecondaryDark, this.darkHSV);
        for (int i = 0; i <= 2; i++) {
            this.hsv[i] = MathUtils.lerp(this.darkHSV[i], this.lightHSV[i], this.darkAmount);
        }
        return Color.HSVToColor(this.hsv);
    }

    public final void setColorPalette(boolean z, @Nullable int[] iArr) {
        if (iArr != null) {
            if (!(iArr.length == 0)) {
                int length = iArr.length;
                this.accentPrimary = iArr[Math.max(0, length - 5)];
                this.accentSecondaryLight = iArr[Math.max(0, length - 2)];
                this.accentSecondaryDark = iArr[Math.max(0, length - (z ? 8 : 2))];
                return;
            }
        }
        this.accentPrimary = -1;
        this.accentSecondaryLight = -1;
        this.accentSecondaryDark = z ? -16777216 : -1;
    }

    public final void setDarkAmount(float f) {
        this.darkAmount = f;
    }
}
