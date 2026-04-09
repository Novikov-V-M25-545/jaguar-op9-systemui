package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.UserHandle;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BroadcastDispatcher.kt */
/* loaded from: classes.dex */
public final class ReceiverData {

    @NotNull
    private final Executor executor;

    @NotNull
    private final IntentFilter filter;

    @NotNull
    private final BroadcastReceiver receiver;

    @NotNull
    private final UserHandle user;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReceiverData)) {
            return false;
        }
        ReceiverData receiverData = (ReceiverData) obj;
        return Intrinsics.areEqual(this.receiver, receiverData.receiver) && Intrinsics.areEqual(this.filter, receiverData.filter) && Intrinsics.areEqual(this.executor, receiverData.executor) && Intrinsics.areEqual(this.user, receiverData.user);
    }

    public int hashCode() {
        BroadcastReceiver broadcastReceiver = this.receiver;
        int iHashCode = (broadcastReceiver != null ? broadcastReceiver.hashCode() : 0) * 31;
        IntentFilter intentFilter = this.filter;
        int iHashCode2 = (iHashCode + (intentFilter != null ? intentFilter.hashCode() : 0)) * 31;
        Executor executor = this.executor;
        int iHashCode3 = (iHashCode2 + (executor != null ? executor.hashCode() : 0)) * 31;
        UserHandle userHandle = this.user;
        return iHashCode3 + (userHandle != null ? userHandle.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "ReceiverData(receiver=" + this.receiver + ", filter=" + this.filter + ", executor=" + this.executor + ", user=" + this.user + ")";
    }

    public ReceiverData(@NotNull BroadcastReceiver receiver, @NotNull IntentFilter filter, @NotNull Executor executor, @NotNull UserHandle user) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        Intrinsics.checkParameterIsNotNull(filter, "filter");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(user, "user");
        this.receiver = receiver;
        this.filter = filter;
        this.executor = executor;
        this.user = user;
    }

    @NotNull
    public final BroadcastReceiver getReceiver() {
        return this.receiver;
    }

    @NotNull
    public final IntentFilter getFilter() {
        return this.filter;
    }

    @NotNull
    public final Executor getExecutor() {
        return this.executor;
    }

    @NotNull
    public final UserHandle getUser() {
        return this.user;
    }
}
