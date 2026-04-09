package com.android.systemui.pip.phone;

import android.os.Handler;
import android.os.HandlerThread;

/* loaded from: classes.dex */
public final class PipUpdateThread extends HandlerThread {
    private static Handler sHandler;
    private static PipUpdateThread sInstance;

    private PipUpdateThread() {
        super("pip");
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            PipUpdateThread pipUpdateThread = new PipUpdateThread();
            sInstance = pipUpdateThread;
            pipUpdateThread.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static PipUpdateThread get() {
        PipUpdateThread pipUpdateThread;
        synchronized (PipUpdateThread.class) {
            ensureThreadLocked();
            pipUpdateThread = sInstance;
        }
        return pipUpdateThread;
    }
}
