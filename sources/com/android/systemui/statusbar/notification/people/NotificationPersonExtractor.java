package com.android.systemui.statusbar.notification.people;

import android.service.notification.StatusBarNotification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHubNotificationListener.kt */
/* loaded from: classes.dex */
public interface NotificationPersonExtractor {
    @Nullable
    String extractPersonKey(@NotNull StatusBarNotification statusBarNotification);

    boolean isPersonNotification(@NotNull StatusBarNotification statusBarNotification);
}
