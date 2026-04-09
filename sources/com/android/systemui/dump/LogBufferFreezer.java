package com.android.systemui.dump;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: LogBufferFreezer.kt */
/* loaded from: classes.dex */
public final class LogBufferFreezer {
    private final DumpManager dumpManager;
    private final DelayableExecutor executor;
    private final long freezeDuration;
    private Runnable pendingToken;

    public LogBufferFreezer(@NotNull DumpManager dumpManager, @NotNull DelayableExecutor executor, long j) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.dumpManager = dumpManager;
        this.executor = executor;
        this.freezeDuration = j;
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public LogBufferFreezer(@NotNull DumpManager dumpManager, @NotNull DelayableExecutor executor) {
        this(dumpManager, executor, TimeUnit.MINUTES.toMillis(5L));
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
    }

    public final void attach(@NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.dump.LogBufferFreezer.attach.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(@Nullable Context context, @Nullable Intent intent) {
                LogBufferFreezer.this.onBugreportStarted();
            }
        };
        IntentFilter intentFilter = new IntentFilter("com.android.internal.intent.action.BUGREPORT_STARTED");
        DelayableExecutor delayableExecutor = this.executor;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter, delayableExecutor, userHandle);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onBugreportStarted() {
        Runnable runnable = this.pendingToken;
        if (runnable != null) {
            runnable.run();
        }
        Log.i("LogBufferFreezer", "Freezing log buffers");
        this.dumpManager.freezeBuffers();
        this.pendingToken = this.executor.executeDelayed(new Runnable() { // from class: com.android.systemui.dump.LogBufferFreezer.onBugreportStarted.1
            @Override // java.lang.Runnable
            public final void run() {
                Log.i("LogBufferFreezer", "Unfreezing log buffers");
                LogBufferFreezer.this.pendingToken = null;
                LogBufferFreezer.this.dumpManager.unfreezeBuffers();
            }
        }, this.freezeDuration);
    }
}
