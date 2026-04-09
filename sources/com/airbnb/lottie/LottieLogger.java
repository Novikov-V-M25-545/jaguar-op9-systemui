package com.airbnb.lottie;

/* loaded from: classes.dex */
public interface LottieLogger {
    void debug(String str);

    void warning(String str);

    void warning(String str, Throwable th);
}
