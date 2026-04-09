package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: UserBroadcastDispatcher.kt */
/* loaded from: classes.dex */
public class UserBroadcastDispatcher implements Dumpable {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static final AtomicInteger index = new AtomicInteger(0);

    @NotNull
    private final ArrayMap<String, ActionReceiver> actionsToActionsReceivers;
    private final Executor bgExecutor;
    private final UserBroadcastDispatcher$bgHandler$1 bgHandler;
    private final Looper bgLooper;
    private final Context context;
    private final BroadcastDispatcherLogger logger;
    private final ArrayMap<BroadcastReceiver, Set<String>> receiverToActions;
    private final int userId;

    public static /* synthetic */ void actionsToActionsReceivers$annotations() {
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        boolean z = pw instanceof IndentingPrintWriter;
        if (z) {
            ((IndentingPrintWriter) pw).increaseIndent();
        }
        for (Map.Entry<String, ActionReceiver> entry : this.actionsToActionsReceivers.entrySet()) {
            String key = entry.getKey();
            ActionReceiver value = entry.getValue();
            pw.println(key + ':');
            value.dump(fd, pw, args);
        }
        if (z) {
            ((IndentingPrintWriter) pw).decreaseIndent();
        }
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [com.android.systemui.broadcast.UserBroadcastDispatcher$bgHandler$1] */
    public UserBroadcastDispatcher(@NotNull Context context, int i, @NotNull final Looper bgLooper, @NotNull Executor bgExecutor, @NotNull BroadcastDispatcherLogger logger) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(bgLooper, "bgLooper");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(logger, "logger");
        this.context = context;
        this.userId = i;
        this.bgLooper = bgLooper;
        this.bgExecutor = bgExecutor;
        this.logger = logger;
        this.bgHandler = new Handler(bgLooper) { // from class: com.android.systemui.broadcast.UserBroadcastDispatcher$bgHandler$1
            @Override // android.os.Handler
            public void handleMessage(@NotNull Message msg) throws IllegalArgumentException {
                Intrinsics.checkParameterIsNotNull(msg, "msg");
                int i2 = msg.what;
                if (i2 == 0) {
                    UserBroadcastDispatcher userBroadcastDispatcher = this.this$0;
                    Object obj = msg.obj;
                    if (obj == null) {
                        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.broadcast.ReceiverData");
                    }
                    userBroadcastDispatcher.handleRegisterReceiver((ReceiverData) obj);
                    return;
                }
                if (i2 != 1) {
                    return;
                }
                UserBroadcastDispatcher userBroadcastDispatcher2 = this.this$0;
                Object obj2 = msg.obj;
                if (obj2 == null) {
                    throw new TypeCastException("null cannot be cast to non-null type android.content.BroadcastReceiver");
                }
                userBroadcastDispatcher2.handleUnregisterReceiver((BroadcastReceiver) obj2);
            }
        };
        this.actionsToActionsReceivers = new ArrayMap<>();
        this.receiverToActions = new ArrayMap<>();
    }

    /* compiled from: UserBroadcastDispatcher.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public final boolean isReceiverReferenceHeld$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull BroadcastReceiver receiver) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        Collection<ActionReceiver> collectionValues = this.actionsToActionsReceivers.values();
        Intrinsics.checkExpressionValueIsNotNull(collectionValues, "actionsToActionsReceivers.values");
        if ((collectionValues instanceof Collection) && collectionValues.isEmpty()) {
            z = false;
        } else {
            Iterator<T> it = collectionValues.iterator();
            while (it.hasNext()) {
                if (((ActionReceiver) it.next()).hasReceiver(receiver)) {
                    z = true;
                    break;
                }
            }
            z = false;
        }
        return z || this.receiverToActions.containsKey(receiver);
    }

    public final void registerReceiver(@NotNull ReceiverData receiverData) {
        Intrinsics.checkParameterIsNotNull(receiverData, "receiverData");
        obtainMessage(0, receiverData).sendToTarget();
    }

    public final void unregisterReceiver(@NotNull BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        obtainMessage(1, receiver).sendToTarget();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handleRegisterReceiver(ReceiverData receiverData) throws IllegalArgumentException {
        Sequence sequenceEmptySequence;
        Looper looper = getLooper();
        Intrinsics.checkExpressionValueIsNotNull(looper, "bgHandler.looper");
        Preconditions.checkState(looper.isCurrentThread(), "This method should only be called from BG thread");
        ArrayMap<BroadcastReceiver, Set<String>> arrayMap = this.receiverToActions;
        BroadcastReceiver receiver = receiverData.getReceiver();
        Set<String> arraySet = arrayMap.get(receiver);
        if (arraySet == null) {
            arraySet = new ArraySet<>();
            arrayMap.put(receiver, arraySet);
        }
        Set<String> set = arraySet;
        Iterator<String> itActionsIterator = receiverData.getFilter().actionsIterator();
        if (itActionsIterator == null || (sequenceEmptySequence = SequencesKt__SequencesKt.asSequence(itActionsIterator)) == null) {
            sequenceEmptySequence = SequencesKt__SequencesKt.emptySequence();
        }
        CollectionsKt__MutableCollectionsKt.addAll(set, sequenceEmptySequence);
        Iterator<String> itActionsIterator2 = receiverData.getFilter().actionsIterator();
        Intrinsics.checkExpressionValueIsNotNull(itActionsIterator2, "receiverData.filter.actionsIterator()");
        while (itActionsIterator2.hasNext()) {
            String it = itActionsIterator2.next();
            ArrayMap<String, ActionReceiver> arrayMap2 = this.actionsToActionsReceivers;
            ActionReceiver actionReceiverCreateActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core = arrayMap2.get(it);
            if (actionReceiverCreateActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core == null) {
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                actionReceiverCreateActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core = createActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core(it);
                arrayMap2.put(it, actionReceiverCreateActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core);
            }
            actionReceiverCreateActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core.addReceiverData(receiverData);
        }
        this.logger.logReceiverRegistered(this.userId, receiverData.getReceiver());
    }

    @NotNull
    public ActionReceiver createActionReceiver$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull final String action) {
        Intrinsics.checkParameterIsNotNull(action, "action");
        return new ActionReceiver(action, this.userId, new Function2<BroadcastReceiver, IntentFilter, Unit>() { // from class: com.android.systemui.broadcast.UserBroadcastDispatcher$createActionReceiver$1
            {
                super(2);
            }

            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
                invoke2(broadcastReceiver, intentFilter);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(@NotNull BroadcastReceiver receiver, @NotNull IntentFilter it) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                Intrinsics.checkParameterIsNotNull(it, "it");
                this.this$0.context.registerReceiverAsUser(receiver, UserHandle.of(this.this$0.userId), it, null, this.this$0.bgHandler);
                this.this$0.logger.logContextReceiverRegistered(this.this$0.userId, it);
            }
        }, new Function1<BroadcastReceiver, Unit>() { // from class: com.android.systemui.broadcast.UserBroadcastDispatcher$createActionReceiver$2
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(BroadcastReceiver broadcastReceiver) {
                invoke2(broadcastReceiver);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(@NotNull BroadcastReceiver receiver) {
                Intrinsics.checkParameterIsNotNull(receiver, "$receiver");
                try {
                    this.this$0.context.unregisterReceiver(receiver);
                    this.this$0.logger.logContextReceiverUnregistered(this.this$0.userId, action);
                } catch (IllegalArgumentException e) {
                    Log.e("UserBroadcastDispatcher", "Trying to unregister unregistered receiver for user " + this.this$0.userId + ", action " + action, new IllegalStateException(e));
                }
            }
        }, this.bgExecutor, this.logger);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handleUnregisterReceiver(BroadcastReceiver broadcastReceiver) {
        Looper looper = getLooper();
        Intrinsics.checkExpressionValueIsNotNull(looper, "bgHandler.looper");
        Preconditions.checkState(looper.isCurrentThread(), "This method should only be called from BG thread");
        Set<String> orDefault = this.receiverToActions.getOrDefault(broadcastReceiver, new LinkedHashSet());
        Intrinsics.checkExpressionValueIsNotNull(orDefault, "receiverToActions.getOrD…receiver, mutableSetOf())");
        Iterator<T> it = orDefault.iterator();
        while (it.hasNext()) {
            ActionReceiver actionReceiver = this.actionsToActionsReceivers.get((String) it.next());
            if (actionReceiver != null) {
                actionReceiver.removeReceiver(broadcastReceiver);
            }
        }
        this.receiverToActions.remove(broadcastReceiver);
        this.logger.logReceiverUnregistered(this.userId, broadcastReceiver);
    }
}
