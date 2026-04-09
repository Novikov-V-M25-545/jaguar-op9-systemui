package com.android.systemui;

import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.util.time.SystemClock;

/* loaded from: classes.dex */
public class ForegroundServiceLifetimeExtender implements NotificationLifetimeExtender {

    @VisibleForTesting
    static final int MIN_FGS_TIME_MS = 5000;
    private final NotificationInteractionTracker mInteractionTracker;
    private NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationSafeToRemoveCallback;
    private final SystemClock mSystemClock;
    private ArraySet<NotificationEntry> mManagedEntries = new ArraySet<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ForegroundServiceLifetimeExtender(NotificationInteractionTracker notificationInteractionTracker, SystemClock systemClock) {
        this.mSystemClock = systemClock;
        this.mInteractionTracker = notificationInteractionTracker;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
        this.mNotificationSafeToRemoveCallback = notificationSafeToRemoveCallback;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        if ((notificationEntry.getSbn().getNotification().flags & 64) == 0 || notificationEntry.hasInterrupted()) {
            return false;
        }
        return this.mSystemClock.uptimeMillis() - notificationEntry.getCreationTime() < 5000 && !this.mInteractionTracker.hasUserInteractedWith(notificationEntry.getKey());
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetimeForPendingNotification(NotificationEntry notificationEntry) {
        return shouldExtendLifetime(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setShouldManageLifetime(final NotificationEntry notificationEntry, boolean z) {
        if (!z) {
            this.mManagedEntries.remove(notificationEntry);
            return;
        }
        this.mManagedEntries.add(notificationEntry);
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.ForegroundServiceLifetimeExtender$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setShouldManageLifetime$0(notificationEntry);
            }
        }, 5000 - (this.mSystemClock.uptimeMillis() - notificationEntry.getCreationTime()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setShouldManageLifetime$0(NotificationEntry notificationEntry) {
        if (this.mManagedEntries.contains(notificationEntry)) {
            this.mManagedEntries.remove(notificationEntry);
            NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback = this.mNotificationSafeToRemoveCallback;
            if (notificationSafeToRemoveCallback != null) {
                notificationSafeToRemoveCallback.onSafeToRemove(notificationEntry.getKey());
            }
        }
    }
}
