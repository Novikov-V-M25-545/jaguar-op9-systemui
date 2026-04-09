package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationNotifications.kt */
/* loaded from: classes.dex */
public final class ConversationNotificationProcessor {
    private final ConversationNotificationManager conversationNotificationManager;
    private final LauncherApps launcherApps;

    public ConversationNotificationProcessor(@NotNull LauncherApps launcherApps, @NotNull ConversationNotificationManager conversationNotificationManager) {
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        Intrinsics.checkParameterIsNotNull(conversationNotificationManager, "conversationNotificationManager");
        this.launcherApps = launcherApps;
        this.conversationNotificationManager = conversationNotificationManager;
    }

    public final void processNotification(@NotNull NotificationEntry entry, @NotNull Notification.Builder recoveredBuilder) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        Intrinsics.checkParameterIsNotNull(recoveredBuilder, "recoveredBuilder");
        Notification.Style style = recoveredBuilder.getStyle();
        if (!(style instanceof Notification.MessagingStyle)) {
            style = null;
        }
        Notification.MessagingStyle messagingStyle = (Notification.MessagingStyle) style;
        if (messagingStyle != null) {
            NotificationListenerService.Ranking ranking = entry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
            NotificationChannel channel = ranking.getChannel();
            Intrinsics.checkExpressionValueIsNotNull(channel, "entry.ranking.channel");
            messagingStyle.setConversationType(channel.isImportantConversation() ? 2 : 1);
            NotificationListenerService.Ranking ranking2 = entry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking2, "entry.ranking");
            ShortcutInfo shortcutInfo = ranking2.getShortcutInfo();
            if (shortcutInfo != null) {
                messagingStyle.setShortcutIcon(this.launcherApps.getShortcutIcon(shortcutInfo));
                CharSequence label = shortcutInfo.getLabel();
                if (label != null) {
                    messagingStyle.setConversationTitle(label);
                }
            }
            messagingStyle.setUnreadMessageCount(this.conversationNotificationManager.getUnreadCount(entry, recoveredBuilder));
        }
    }
}
