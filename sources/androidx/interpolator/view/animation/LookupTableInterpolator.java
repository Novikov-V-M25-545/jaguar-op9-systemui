package androidx.interpolator.view.animation;

/* loaded from: classes.dex */
final class LookupTableInterpolator {
    static float interpolate(float[] fArr, float f, float f2) {
        if (f2 >= 1.0f) {
            return 1.0f;
        }
        if (f2 <= 0.0f) {
            return 0.0f;
        }
        int iMin = Math.min((int) ((fArr.length - 1) * f2), fArr.length - 2);
        return fArr[iMin] + (((f2 - (iMin * f)) / f) * (fArr[iMin + 1] - fArr[iMin]));
    }
}
