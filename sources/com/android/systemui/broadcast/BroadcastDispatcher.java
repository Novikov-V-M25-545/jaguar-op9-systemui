package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BroadcastDispatcher.kt */
/* loaded from: classes.dex */
public class BroadcastDispatcher extends BroadcastReceiver implements Dumpable {
    private final Executor bgExecutor;
    private final Looper bgLooper;
    private final Context context;
    private final DumpManager dumpManager;
    private final BroadcastDispatcher$handler$1 handler;
    private final BroadcastDispatcherLogger logger;
    private final SparseArray<UserBroadcastDispatcher> receiversByUser;

    public void registerReceiver(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter) {
        registerReceiver$default(this, broadcastReceiver, intentFilter, null, null, 12, null);
    }

    public void registerReceiverWithHandler(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter, @NotNull Handler handler) {
        registerReceiverWithHandler$default(this, broadcastReceiver, intentFilter, handler, null, 8, null);
    }

    public BroadcastDispatcher(@NotNull Context context, @NotNull Looper bgLooper, @NotNull Executor bgExecutor, @NotNull DumpManager dumpManager, @NotNull BroadcastDispatcherLogger logger) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(bgLooper, "bgLooper");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(logger, "logger");
        this.context = context;
        this.bgLooper = bgLooper;
        this.bgExecutor = bgExecutor;
        this.dumpManager = dumpManager;
        this.logger = logger;
        this.receiversByUser = new SparseArray<>(20);
        this.handler = new BroadcastDispatcher$handler$1(this, bgLooper);
    }

    public final void initialize() {
        DumpManager dumpManager = this.dumpManager;
        String name = BroadcastDispatcher.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
        this.handler.sendEmptyMessage(99);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.USER_SWITCHED");
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        registerReceiver(this, intentFilter, null, userHandle);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (Intrinsics.areEqual(intent.getAction(), "android.intent.action.USER_SWITCHED")) {
            this.handler.obtainMessage(3, intent.getIntExtra("android.intent.extra.user_handle", -10000), 0).sendToTarget();
        }
    }

    public static /* synthetic */ void registerReceiverWithHandler$default(BroadcastDispatcher broadcastDispatcher, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Handler handler, UserHandle userHandle, int i, Object obj) {
        if (obj != null) {
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: registerReceiverWithHandler");
        }
        if ((i & 8) != 0) {
            userHandle = broadcastDispatcher.context.getUser();
            Intrinsics.checkExpressionValueIsNotNull(userHandle, "context.user");
        }
        broadcastDispatcher.registerReceiverWithHandler(broadcastReceiver, intentFilter, handler, userHandle);
    }

    public void registerReceiverWithHandler(@NotNull BroadcastReceiver receiver, @NotNull IntentFilter filter, @NotNull Handler handler, @NotNull UserHandle user) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        Intrinsics.checkParameterIsNotNull(filter, "filter");
        Intrinsics.checkParameterIsNotNull(handler, "handler");
        Intrinsics.checkParameterIsNotNull(user, "user");
        registerReceiver(receiver, filter, new HandlerExecutor(handler), user);
    }

    public static /* synthetic */ void registerReceiver$default(BroadcastDispatcher broadcastDispatcher, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Executor executor, UserHandle userHandle, int i, Object obj) {
        if (obj != null) {
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: registerReceiver");
        }
        if ((i & 4) != 0) {
            executor = broadcastDispatcher.context.getMainExecutor();
        }
        if ((i & 8) != 0) {
            userHandle = broadcastDispatcher.context.getUser();
            Intrinsics.checkExpressionValueIsNotNull(userHandle, "context.user");
        }
        broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter, executor, userHandle);
    }

    public void registerReceiver(@NotNull BroadcastReceiver receiver, @NotNull IntentFilter filter, @Nullable Executor executor, @NotNull UserHandle user) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        Intrinsics.checkParameterIsNotNull(filter, "filter");
        Intrinsics.checkParameterIsNotNull(user, "user");
        checkFilter(filter);
        BroadcastDispatcher$handler$1 broadcastDispatcher$handler$1 = this.handler;
        if (executor == null) {
            executor = this.context.getMainExecutor();
            Intrinsics.checkExpressionValueIsNotNull(executor, "context.mainExecutor");
        }
        broadcastDispatcher$handler$1.obtainMessage(0, new ReceiverData(receiver, filter, executor, user)).sendToTarget();
    }

    private final void checkFilter(IntentFilter intentFilter) {
        StringBuilder sb = new StringBuilder();
        if (intentFilter.countActions() == 0) {
            sb.append("Filter must contain at least one action. ");
        }
        if (intentFilter.countDataAuthorities() != 0) {
            sb.append("Filter cannot contain DataAuthorities. ");
        }
        if (intentFilter.countDataPaths() != 0) {
            sb.append("Filter cannot contain DataPaths. ");
        }
        if (intentFilter.countDataSchemes() != 0) {
            sb.append("Filter cannot contain DataSchemes. ");
        }
        if (intentFilter.countDataTypes() != 0) {
            sb.append("Filter cannot contain DataTypes. ");
        }
        if (intentFilter.getPriority() != 0) {
            sb.append("Filter cannot modify priority. ");
        }
        if (!TextUtils.isEmpty(sb)) {
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public void unregisterReceiver(@NotNull BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        this.handler.obtainMessage(1, receiver).sendToTarget();
    }

    @VisibleForTesting
    @NotNull
    protected UserBroadcastDispatcher createUBRForUser(int i) {
        return new UserBroadcastDispatcher(this.context, i, this.bgLooper, this.bgExecutor, this.logger);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("Broadcast dispatcher:");
        PrintWriter indentingPrintWriter = new IndentingPrintWriter(pw, "  ");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("Current user: " + this.handler.getCurrentUser());
        int size = this.receiversByUser.size();
        for (int i = 0; i < size; i++) {
            indentingPrintWriter.println("User " + this.receiversByUser.keyAt(i));
            this.receiversByUser.valueAt(i).dump(fd, indentingPrintWriter, args);
        }
        indentingPrintWriter.decreaseIndent();
    }
}
