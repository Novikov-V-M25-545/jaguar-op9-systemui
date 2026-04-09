package com.android.systemui.pulse;

import java.util.ArrayDeque;

/* loaded from: classes.dex */
class FFTAverage {
    private float average;
    private final ArrayDeque<Float> window = new ArrayDeque<>(2);

    FFTAverage() {
    }

    int average(int i) {
        Float fPollFirst;
        if (this.window.size() >= 2 && (fPollFirst = this.window.pollFirst()) != null) {
            this.average -= fPollFirst.floatValue();
        }
        float f = i / 2.0f;
        this.average += f;
        this.window.offerLast(Float.valueOf(f));
        return Math.round(this.average);
    }
}
