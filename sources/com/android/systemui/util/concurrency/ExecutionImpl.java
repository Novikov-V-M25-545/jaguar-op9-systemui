package com.android.systemui.util.concurrency;

import android.os.Looper;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Execution.kt */
/* loaded from: classes.dex */
public final class ExecutionImpl implements Execution {
    private final Looper mainLooper = Looper.getMainLooper();

    @Override // com.android.systemui.util.concurrency.Execution
    public void assertIsMainThread() {
        Looper mainLooper = this.mainLooper;
        Intrinsics.checkExpressionValueIsNotNull(mainLooper, "mainLooper");
        if (mainLooper.isCurrentThread()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("should be called from the main thread. Main thread name=");
        Looper mainLooper2 = this.mainLooper;
        Intrinsics.checkExpressionValueIsNotNull(mainLooper2, "mainLooper");
        Thread thread = mainLooper2.getThread();
        Intrinsics.checkExpressionValueIsNotNull(thread, "mainLooper.thread");
        sb.append(thread.getName());
        sb.append(" Thread.currentThread()=");
        Thread threadCurrentThread = Thread.currentThread();
        Intrinsics.checkExpressionValueIsNotNull(threadCurrentThread, "Thread.currentThread()");
        sb.append(threadCurrentThread.getName());
        throw new IllegalStateException(sb.toString());
    }
}
