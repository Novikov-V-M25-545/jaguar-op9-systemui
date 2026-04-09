package com.android.systemui.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.broadcast.BroadcastDispatcher;
import java.util.concurrent.Executor;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RingerModeTrackerImpl.kt */
/* loaded from: classes.dex */
public final class RingerModeLiveData extends MutableLiveData<Integer> {
    private final BroadcastDispatcher broadcastDispatcher;
    private final Executor executor;
    private final IntentFilter filter;
    private final Function0<Integer> getter;
    private boolean initialSticky;
    private final RingerModeLiveData$receiver$1 receiver;

    /* JADX WARN: Type inference failed for: r2v2, types: [com.android.systemui.util.RingerModeLiveData$receiver$1] */
    public RingerModeLiveData(@NotNull BroadcastDispatcher broadcastDispatcher, @NotNull Executor executor, @NotNull String intent, @NotNull Function0<Integer> getter) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        Intrinsics.checkParameterIsNotNull(getter, "getter");
        this.broadcastDispatcher = broadcastDispatcher;
        this.executor = executor;
        this.getter = getter;
        this.filter = new IntentFilter(intent);
        this.receiver = new BroadcastReceiver() { // from class: com.android.systemui.util.RingerModeLiveData$receiver$1
            @Override // android.content.BroadcastReceiver
            public void onReceive(@NotNull Context context, @NotNull Intent intent2) {
                Intrinsics.checkParameterIsNotNull(context, "context");
                Intrinsics.checkParameterIsNotNull(intent2, "intent");
                this.this$0.initialSticky = isInitialStickyBroadcast();
                this.this$0.postValue(Integer.valueOf(intent2.getIntExtra("android.media.EXTRA_RINGER_MODE", -1)));
            }
        };
    }

    public final boolean getInitialSticky() {
        return this.initialSticky;
    }

    @Override // androidx.lifecycle.LiveData
    @NotNull
    public Integer getValue() {
        Integer num = (Integer) super.getValue();
        return Integer.valueOf(num != null ? num.intValue() : -1);
    }

    @Override // androidx.lifecycle.LiveData
    protected void onActive() {
        super.onActive();
        BroadcastDispatcher broadcastDispatcher = this.broadcastDispatcher;
        RingerModeLiveData$receiver$1 ringerModeLiveData$receiver$1 = this.receiver;
        IntentFilter intentFilter = this.filter;
        Executor executor = this.executor;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(ringerModeLiveData$receiver$1, intentFilter, executor, userHandle);
        this.executor.execute(new Runnable() { // from class: com.android.systemui.util.RingerModeLiveData.onActive.1
            @Override // java.lang.Runnable
            public final void run() {
                RingerModeLiveData ringerModeLiveData = RingerModeLiveData.this;
                ringerModeLiveData.postValue(ringerModeLiveData.getter.invoke());
            }
        });
    }

    @Override // androidx.lifecycle.LiveData
    protected void onInactive() {
        super.onInactive();
        this.broadcastDispatcher.unregisterReceiver(this.receiver);
    }
}
