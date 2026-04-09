package androidx.leanback.widget;

import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public abstract class ParallaxEffect {
    final List<?> mMarkerValues = new ArrayList(2);
    final List<Float> mWeights = new ArrayList(2);
    final List<Float> mTotalWeights = new ArrayList(2);
    final List<ParallaxTarget> mTargets = new ArrayList(4);

    abstract Number calculateDirectValue(Parallax parallax);

    abstract float calculateFraction(Parallax parallax);

    ParallaxEffect() {
    }

    public final void performMapping(Parallax parallax) throws IllegalStateException {
        if (this.mMarkerValues.size() < 2) {
            return;
        }
        parallax.verifyFloatProperties();
        float fCalculateFraction = 0.0f;
        Number numberCalculateDirectValue = null;
        boolean z = false;
        for (int i = 0; i < this.mTargets.size(); i++) {
            ParallaxTarget parallaxTarget = this.mTargets.get(i);
            if (parallaxTarget.isDirectMapping()) {
                if (numberCalculateDirectValue == null) {
                    numberCalculateDirectValue = calculateDirectValue(parallax);
                }
                parallaxTarget.directUpdate(numberCalculateDirectValue);
            } else {
                if (!z) {
                    fCalculateFraction = calculateFraction(parallax);
                    z = true;
                }
                parallaxTarget.update(fCalculateFraction);
            }
        }
    }
}
