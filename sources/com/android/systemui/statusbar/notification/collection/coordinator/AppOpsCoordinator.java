package com.android.systemui.statusbar.notification.collection.coordinator;

import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.util.Assert;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class AppOpsCoordinator implements Coordinator {
    private final AppOpsController mAppOpsController;
    private final ForegroundServiceController mForegroundServiceController;
    private final DelayableExecutor mMainExecutor;
    private NotifPipeline mNotifPipeline;
    private final NotifFilter mNotifFilter = new NotifFilter("AppOpsCoordinator") { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.1
        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            String[] stringArray;
            StatusBarNotification sbn = notificationEntry.getSbn();
            if (!AppOpsCoordinator.this.mForegroundServiceController.isDisclosureNotification(sbn) || AppOpsCoordinator.this.mForegroundServiceController.isDisclosureNeededForUser(sbn.getUser().getIdentifier())) {
                return AppOpsCoordinator.this.mForegroundServiceController.isSystemAlertNotification(sbn) && (stringArray = sbn.getNotification().extras.getStringArray("android.foregroundApps")) != null && stringArray.length >= 1 && !AppOpsCoordinator.this.mForegroundServiceController.isSystemAlertWarningNeeded(sbn.getUser().getIdentifier(), stringArray[0]);
            }
            return true;
        }
    };
    private final NotifLifetimeExtender mForegroundLifetimeExtender = new AnonymousClass2();
    private NotifCollectionListener mNotifCollectionListener = new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.3
        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(NotificationEntry notificationEntry) {
            tagAppOps(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(NotificationEntry notificationEntry) {
            tagAppOps(notificationEntry);
        }

        private void tagAppOps(NotificationEntry notificationEntry) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            ArraySet<Integer> appOps = AppOpsCoordinator.this.mForegroundServiceController.getAppOps(sbn.getUser().getIdentifier(), sbn.getPackageName());
            notificationEntry.mActiveAppOps.clear();
            if (appOps != null) {
                notificationEntry.mActiveAppOps.addAll((ArraySet<? extends Integer>) appOps);
            }
        }
    };

    public AppOpsCoordinator(ForegroundServiceController foregroundServiceController, AppOpsController appOpsController, DelayableExecutor delayableExecutor) {
        this.mForegroundServiceController = foregroundServiceController;
        this.mAppOpsController = appOpsController;
        this.mMainExecutor = delayableExecutor;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mNotifPipeline = notifPipeline;
        notifPipeline.addNotificationLifetimeExtender(this.mForegroundLifetimeExtender);
        this.mNotifPipeline.addCollectionListener(this.mNotifCollectionListener);
        this.mNotifPipeline.addPreGroupFilter(this.mNotifFilter);
        this.mAppOpsController.addCallback(ForegroundServiceController.APP_OPS, new AppOpsController.Callback() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator$$ExternalSyntheticLambda0
            @Override // com.android.systemui.appops.AppOpsController.Callback
            public final void onActiveStateChanged(int i, int i2, String str, boolean z) {
                this.f$0.onAppOpsChanged(i, i2, str, z);
            }
        });
    }

    /* renamed from: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator$2, reason: invalid class name */
    class AnonymousClass2 implements NotifLifetimeExtender {
        private NotifLifetimeExtender.OnEndLifetimeExtensionCallback mEndCallback;
        private Map<NotificationEntry, Runnable> mEndRunnables = new HashMap();

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public String getName() {
            return "AppOpsCoordinator";
        }

        AnonymousClass2() {
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void setCallback(NotifLifetimeExtender.OnEndLifetimeExtensionCallback onEndLifetimeExtensionCallback) {
            this.mEndCallback = onEndLifetimeExtensionCallback;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public boolean shouldExtendLifetime(final NotificationEntry notificationEntry, int i) {
            if ((notificationEntry.getSbn().getNotification().flags & 64) == 0) {
                return false;
            }
            long jCurrentTimeMillis = System.currentTimeMillis();
            boolean z = jCurrentTimeMillis - notificationEntry.getSbn().getPostTime() < 5000;
            if (z && !this.mEndRunnables.containsKey(notificationEntry)) {
                this.mEndRunnables.put(notificationEntry, AppOpsCoordinator.this.mMainExecutor.executeDelayed(new Runnable() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$shouldExtendLifetime$0(notificationEntry);
                    }
                }, 5000 - (jCurrentTimeMillis - notificationEntry.getSbn().getPostTime())));
            }
            return z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$shouldExtendLifetime$0(NotificationEntry notificationEntry) {
            this.mEndRunnables.remove(notificationEntry);
            this.mEndCallback.onEndLifetimeExtension(AppOpsCoordinator.this.mForegroundLifetimeExtender, notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void cancelLifetimeExtension(NotificationEntry notificationEntry) {
            Runnable runnableRemove = this.mEndRunnables.remove(notificationEntry);
            if (runnableRemove != null) {
                runnableRemove.run();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAppOpsChanged(final int i, final int i2, final String str, final boolean z) {
        this.mMainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onAppOpsChanged$0(i, i2, str, z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: handleAppOpsChanged, reason: merged with bridge method [inline-methods] */
    public void lambda$onAppOpsChanged$0(int i, int i2, String str, boolean z) {
        boolean zRemove;
        Assert.isMainThread();
        ArraySet<String> standardLayoutKeys = this.mForegroundServiceController.getStandardLayoutKeys(UserHandle.getUserId(i2), str);
        if (standardLayoutKeys != null) {
            boolean z2 = false;
            for (int i3 = 0; i3 < standardLayoutKeys.size(); i3++) {
                NotificationEntry notificationEntryFindNotificationEntryWithKey = findNotificationEntryWithKey(standardLayoutKeys.valueAt(i3));
                if (notificationEntryFindNotificationEntryWithKey != null && i2 == notificationEntryFindNotificationEntryWithKey.getSbn().getUid() && str.equals(notificationEntryFindNotificationEntryWithKey.getSbn().getPackageName())) {
                    if (z) {
                        zRemove = notificationEntryFindNotificationEntryWithKey.mActiveAppOps.add(Integer.valueOf(i));
                    } else {
                        zRemove = notificationEntryFindNotificationEntryWithKey.mActiveAppOps.remove(Integer.valueOf(i));
                    }
                    z2 |= zRemove;
                }
            }
            if (z2) {
                this.mNotifFilter.invalidateList();
            }
        }
    }

    private NotificationEntry findNotificationEntryWithKey(String str) {
        for (NotificationEntry notificationEntry : this.mNotifPipeline.getAllNotifs()) {
            if (notificationEntry.getKey().equals(str)) {
                return notificationEntry;
            }
        }
        return null;
    }
}
