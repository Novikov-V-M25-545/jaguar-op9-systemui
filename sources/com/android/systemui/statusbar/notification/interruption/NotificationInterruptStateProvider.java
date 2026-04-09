package com.android.systemui.statusbar.notification.interruption;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

/* loaded from: classes.dex */
public interface NotificationInterruptStateProvider {
    void addSuppressor(NotificationInterruptSuppressor notificationInterruptSuppressor);

    void setGamingPeekMode(boolean z);

    void setUseLessBoringHeadsUp(boolean z);

    boolean shouldBubbleUp(NotificationEntry notificationEntry);

    boolean shouldHeadsUp(NotificationEntry notificationEntry);

    boolean shouldLaunchFullScreenIntentWhenAdded(NotificationEntry notificationEntry);
}
