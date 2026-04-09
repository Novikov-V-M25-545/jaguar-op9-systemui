package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.ArraySet;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.ActionReceiver;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ActionReceiver.kt */
/* loaded from: classes.dex */
public final class ActionReceiver extends BroadcastReceiver implements Dumpable {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static final AtomicInteger index = new AtomicInteger(0);
    private final String action;
    private final ArraySet<String> activeCategories;
    private final Executor bgExecutor;
    private final BroadcastDispatcherLogger logger;
    private final ArraySet<ReceiverData> receiverDatas;
    private final Function2<BroadcastReceiver, IntentFilter, Unit> registerAction;
    private boolean registered;
    private final Function1<BroadcastReceiver, Unit> unregisterAction;
    private final int userId;

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        if (pw instanceof IndentingPrintWriter) {
            ((IndentingPrintWriter) pw).increaseIndent();
        }
        pw.println("Registered: " + this.registered);
        pw.println("Receivers:");
        boolean z = pw instanceof IndentingPrintWriter;
        if (z) {
            ((IndentingPrintWriter) pw).increaseIndent();
        }
        Iterator<T> it = this.receiverDatas.iterator();
        while (it.hasNext()) {
            pw.println(((ReceiverData) it.next()).getReceiver());
        }
        if (z) {
            ((IndentingPrintWriter) pw).decreaseIndent();
        }
        pw.println("Categories: " + CollectionsKt___CollectionsKt.joinToString$default(this.activeCategories, ", ", null, null, 0, null, null, 62, null));
        if (z) {
            ((IndentingPrintWriter) pw).decreaseIndent();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public ActionReceiver(@NotNull String action, int i, @NotNull Function2<? super BroadcastReceiver, ? super IntentFilter, Unit> registerAction, @NotNull Function1<? super BroadcastReceiver, Unit> unregisterAction, @NotNull Executor bgExecutor, @NotNull BroadcastDispatcherLogger logger) {
        Intrinsics.checkParameterIsNotNull(action, "action");
        Intrinsics.checkParameterIsNotNull(registerAction, "registerAction");
        Intrinsics.checkParameterIsNotNull(unregisterAction, "unregisterAction");
        Intrinsics.checkParameterIsNotNull(bgExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(logger, "logger");
        this.action = action;
        this.userId = i;
        this.registerAction = registerAction;
        this.unregisterAction = unregisterAction;
        this.bgExecutor = bgExecutor;
        this.logger = logger;
        this.receiverDatas = new ArraySet<>();
        this.activeCategories = new ArraySet<>();
    }

    /* compiled from: ActionReceiver.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public final void addReceiverData(@NotNull ReceiverData receiverData) throws IllegalArgumentException {
        Sequence sequenceEmptySequence;
        Intrinsics.checkParameterIsNotNull(receiverData, "receiverData");
        if (!receiverData.getFilter().hasAction(this.action)) {
            throw new IllegalArgumentException("Trying to attach to " + this.action + " without correct action,receiver: " + receiverData.getReceiver());
        }
        ArraySet<String> arraySet = this.activeCategories;
        Iterator<String> itCategoriesIterator = receiverData.getFilter().categoriesIterator();
        if (itCategoriesIterator == null || (sequenceEmptySequence = SequencesKt__SequencesKt.asSequence(itCategoriesIterator)) == null) {
            sequenceEmptySequence = SequencesKt__SequencesKt.emptySequence();
        }
        boolean zAddAll = CollectionsKt__MutableCollectionsKt.addAll(arraySet, sequenceEmptySequence);
        if (this.receiverDatas.add(receiverData) && this.receiverDatas.size() == 1) {
            this.registerAction.invoke(this, createFilter());
            this.registered = true;
        } else if (zAddAll) {
            this.unregisterAction.invoke(this);
            this.registerAction.invoke(this, createFilter());
        }
    }

    public final boolean hasReceiver(@NotNull BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        ArraySet<ReceiverData> arraySet = this.receiverDatas;
        if ((arraySet instanceof Collection) && arraySet.isEmpty()) {
            return false;
        }
        Iterator<T> it = arraySet.iterator();
        while (it.hasNext()) {
            if (Intrinsics.areEqual(((ReceiverData) it.next()).getReceiver(), receiver)) {
                return true;
            }
        }
        return false;
    }

    private final IntentFilter createFilter() {
        IntentFilter intentFilter = new IntentFilter(this.action);
        Iterator<T> it = this.activeCategories.iterator();
        while (it.hasNext()) {
            intentFilter.addCategory((String) it.next());
        }
        return intentFilter;
    }

    public final void removeReceiver(@NotNull final BroadcastReceiver receiver) {
        Intrinsics.checkParameterIsNotNull(receiver, "receiver");
        if (CollectionsKt__MutableCollectionsKt.removeAll(this.receiverDatas, new Function1<ReceiverData, Boolean>() { // from class: com.android.systemui.broadcast.ActionReceiver.removeReceiver.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(ReceiverData receiverData) {
                return Boolean.valueOf(invoke2(receiverData));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final boolean invoke2(ReceiverData receiverData) {
                return Intrinsics.areEqual(receiverData.getReceiver(), receiver);
            }
        }) && this.receiverDatas.isEmpty() && this.registered) {
            this.unregisterAction.invoke(this);
            this.registered = false;
            this.activeCategories.clear();
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(@NotNull Context context, @NotNull Intent intent) throws IllegalStateException {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (!Intrinsics.areEqual(intent.getAction(), this.action)) {
            throw new IllegalStateException("Received intent for " + intent.getAction() + " in receiver for " + this.action + '}');
        }
        int andIncrement = index.getAndIncrement();
        this.logger.logBroadcastReceived(andIncrement, this.userId, intent);
        this.bgExecutor.execute(new AnonymousClass1(intent, context, andIncrement));
    }

    /* compiled from: ActionReceiver.kt */
    /* renamed from: com.android.systemui.broadcast.ActionReceiver$onReceive$1, reason: invalid class name */
    static final class AnonymousClass1 implements Runnable {
        final /* synthetic */ Context $context;
        final /* synthetic */ int $id;
        final /* synthetic */ Intent $intent;

        AnonymousClass1(Intent intent, Context context, int i) {
            this.$intent = intent;
            this.$context = context;
            this.$id = i;
        }

        @Override // java.lang.Runnable
        public final void run() {
            for (final ReceiverData receiverData : ActionReceiver.this.receiverDatas) {
                if (receiverData.getFilter().matchCategories(this.$intent.getCategories()) == null) {
                    receiverData.getExecutor().execute(new Runnable() { // from class: com.android.systemui.broadcast.ActionReceiver$onReceive$1$$special$$inlined$forEach$lambda$1
                        @Override // java.lang.Runnable
                        public final void run() {
                            receiverData.getReceiver().setPendingResult(ActionReceiver.this.getPendingResult());
                            BroadcastReceiver receiver = receiverData.getReceiver();
                            ActionReceiver.AnonymousClass1 anonymousClass1 = this;
                            receiver.onReceive(anonymousClass1.$context, anonymousClass1.$intent);
                            BroadcastDispatcherLogger broadcastDispatcherLogger = ActionReceiver.this.logger;
                            ActionReceiver.AnonymousClass1 anonymousClass12 = this;
                            broadcastDispatcherLogger.logBroadcastDispatched(anonymousClass12.$id, ActionReceiver.this.action, receiverData.getReceiver());
                        }
                    });
                }
            }
        }
    }
}
