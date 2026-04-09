package com.android.systemui.statusbar.notification.people;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleNotificationIdentifier.kt */
/* loaded from: classes.dex */
public interface PeopleNotificationIdentifier {
    int compareTo(int i, int i2);

    int getPeopleNotificationType(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.Ranking ranking);
}
