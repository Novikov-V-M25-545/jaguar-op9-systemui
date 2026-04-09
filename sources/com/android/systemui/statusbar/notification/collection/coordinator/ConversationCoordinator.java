package com.android.systemui.statusbar.notification.collection.coordinator;

import android.app.NotificationChannel;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationCoordinator.kt */
/* loaded from: classes.dex */
public final class ConversationCoordinator implements Coordinator {
    public static final Companion Companion = new Companion(null);
    private final ConversationCoordinator$notificationPromoter$1 notificationPromoter;

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator$notificationPromoter$1] */
    public ConversationCoordinator() {
        final String str = "ConversationCoordinator";
        this.notificationPromoter = new NotifPromoter(str) { // from class: com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator$notificationPromoter$1
            @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter
            public boolean shouldPromoteToTopLevel(@NotNull NotificationEntry entry) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                NotificationChannel channel = entry.getChannel();
                return channel != null && channel.isImportantConversation();
            }
        };
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(@NotNull NotifPipeline pipeline) {
        Intrinsics.checkParameterIsNotNull(pipeline, "pipeline");
        pipeline.addPromoter(this.notificationPromoter);
    }

    /* compiled from: ConversationCoordinator.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
