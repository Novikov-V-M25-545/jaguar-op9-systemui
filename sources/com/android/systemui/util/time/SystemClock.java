package com.android.systemui.util.time;

/* loaded from: classes.dex */
public interface SystemClock {
    long currentTimeMillis();

    long elapsedRealtime();

    long uptimeMillis();
}
