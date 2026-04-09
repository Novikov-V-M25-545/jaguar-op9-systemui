package com.android.settingslib.display;

import android.util.MathUtils;

/* loaded from: classes.dex */
public class BrightnessUtils {
    public static final float convertGammaToLinearFloat(int i, float f, float f2) {
        float fExp;
        float fNorm = MathUtils.norm(0.0f, 65535.0f, i);
        if (fNorm <= 0.5f) {
            fExp = MathUtils.sq(fNorm / 0.5f);
        } else {
            fExp = MathUtils.exp((fNorm - 0.5599107f) / 0.17883277f) + 0.28466892f;
        }
        return MathUtils.lerp(f, f2, MathUtils.constrain(fExp, 0.0f, 12.0f) / 12.0f);
    }

    public static final int convertLinearToGammaFloat(float f, float f2, float f3) {
        float fLog;
        float fNorm = MathUtils.norm(f2, f3, f) * 12.0f;
        if (fNorm <= 1.0f) {
            fLog = MathUtils.sqrt(fNorm) * 0.5f;
        } else {
            fLog = (MathUtils.log(fNorm - 0.28466892f) * 0.17883277f) + 0.5599107f;
        }
        return Math.round(MathUtils.lerp(0.0f, 65535.0f, fLog));
    }
}
