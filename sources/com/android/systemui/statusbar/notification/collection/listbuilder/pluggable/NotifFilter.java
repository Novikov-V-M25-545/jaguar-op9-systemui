package com.android.systemui.statusbar.notification.collection.listbuilder.pluggable;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

/* loaded from: classes.dex */
public abstract class NotifFilter extends Pluggable<NotifFilter> {
    public abstract boolean shouldFilterOut(NotificationEntry notificationEntry, long j);

    protected NotifFilter(String str) {
        super(str);
    }
}
