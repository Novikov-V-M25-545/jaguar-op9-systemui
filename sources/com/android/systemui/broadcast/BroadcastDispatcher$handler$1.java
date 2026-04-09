package com.android.systemui.broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BroadcastDispatcher.kt */
/* loaded from: classes.dex */
public final class BroadcastDispatcher$handler$1 extends Handler {
    private int currentUser;
    final /* synthetic */ BroadcastDispatcher this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    BroadcastDispatcher$handler$1(BroadcastDispatcher broadcastDispatcher, Looper looper) {
        super(looper);
        this.this$0 = broadcastDispatcher;
    }

    public final int getCurrentUser() {
        return this.currentUser;
    }

    @Override // android.os.Handler
    public void handleMessage(@NotNull Message msg) {
        int identifier;
        Intrinsics.checkParameterIsNotNull(msg, "msg");
        int i = msg.what;
        if (i == 0) {
            Object obj = msg.obj;
            if (obj == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.broadcast.ReceiverData");
            }
            ReceiverData receiverData = (ReceiverData) obj;
            if (receiverData.getUser().getIdentifier() == -2) {
                identifier = this.currentUser;
            } else {
                identifier = receiverData.getUser().getIdentifier();
            }
            if (identifier >= -1) {
                UserBroadcastDispatcher userBroadcastDispatcher = (UserBroadcastDispatcher) this.this$0.receiversByUser.get(identifier, this.this$0.createUBRForUser(identifier));
                this.this$0.receiversByUser.put(identifier, userBroadcastDispatcher);
                userBroadcastDispatcher.registerReceiver(receiverData);
                return;
            }
            throw new IllegalStateException("Attempting to register receiver for invalid user {" + identifier + '}');
        }
        if (i == 1) {
            int size = this.this$0.receiversByUser.size();
            for (int i2 = 0; i2 < size; i2++) {
                UserBroadcastDispatcher userBroadcastDispatcher2 = (UserBroadcastDispatcher) this.this$0.receiversByUser.valueAt(i2);
                Object obj2 = msg.obj;
                if (obj2 == null) {
                    throw new TypeCastException("null cannot be cast to non-null type android.content.BroadcastReceiver");
                }
                userBroadcastDispatcher2.unregisterReceiver((BroadcastReceiver) obj2);
            }
            return;
        }
        if (i != 2) {
            if (i == 3) {
                this.currentUser = msg.arg1;
                return;
            } else if (i == 99) {
                this.currentUser = ActivityManager.getCurrentUser();
                return;
            } else {
                super.handleMessage(msg);
                return;
            }
        }
        UserBroadcastDispatcher userBroadcastDispatcher3 = (UserBroadcastDispatcher) this.this$0.receiversByUser.get(msg.arg1);
        if (userBroadcastDispatcher3 != null) {
            Object obj3 = msg.obj;
            if (obj3 == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.content.BroadcastReceiver");
            }
            userBroadcastDispatcher3.unregisterReceiver((BroadcastReceiver) obj3);
        }
    }
}
