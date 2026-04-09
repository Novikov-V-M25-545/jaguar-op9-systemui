package com.android.systemui.classifier;

/* loaded from: classes.dex */
public class SpeedRatioEvaluator {
    public static float evaluate(float f) {
        if (f == 0.0f) {
            return 0.0f;
        }
        double d = f;
        float f2 = d <= 1.0d ? 1.0f : 0.0f;
        if (d <= 0.5d) {
            f2 += 1.0f;
        }
        if (d > 9.0d) {
            f2 += 1.0f;
        }
        return d > 18.0d ? f2 + 1.0f : f2;
    }
}
