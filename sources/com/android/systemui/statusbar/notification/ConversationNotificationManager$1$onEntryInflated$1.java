package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* compiled from: ConversationNotifications.kt */
/* loaded from: classes.dex */
final class ConversationNotificationManager$1$onEntryInflated$1 extends Lambda implements Function1<Boolean, Unit> {
    final /* synthetic */ NotificationEntry $entry;
    final /* synthetic */ ConversationNotificationManager.AnonymousClass1 this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    ConversationNotificationManager$1$onEntryInflated$1(ConversationNotificationManager.AnonymousClass1 anonymousClass1, NotificationEntry notificationEntry) {
        super(1);
        this.this$0 = anonymousClass1;
        this.$entry = notificationEntry;
    }

    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
        invoke(bool.booleanValue());
        return Unit.INSTANCE;
    }

    public final void invoke(boolean z) {
        if (z) {
            if (!ConversationNotificationManager.this.notifPanelCollapsed || this.$entry.isPinnedAndExpanded()) {
                ConversationNotificationManager conversationNotificationManager = ConversationNotificationManager.this;
                String key = this.$entry.getKey();
                Intrinsics.checkExpressionValueIsNotNull(key, "entry.key");
                conversationNotificationManager.resetCount(key);
                ExpandableNotificationRow row = this.$entry.getRow();
                if (row != null) {
                    ConversationNotificationManager.this.resetBadgeUi(row);
                }
            }
        }
    }
}
