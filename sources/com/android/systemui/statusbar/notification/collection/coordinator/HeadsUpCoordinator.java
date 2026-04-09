package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.collection.ListEntry;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.Objects;

/* loaded from: classes.dex */
public class HeadsUpCoordinator implements Coordinator {
    private NotificationEntry mCurrentHun;
    private NotifLifetimeExtender.OnEndLifetimeExtensionCallback mEndLifetimeExtension;
    private final HeadsUpManager mHeadsUpManager;
    private final HeadsUpViewBinder mHeadsUpViewBinder;
    private NotificationEntry mNotifExtendingLifetime;
    private final NotifPromoter mNotifPromoter;
    private final NotifSection mNotifSection;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final NotifCollectionListener mNotifCollectionListener = new AnonymousClass1();
    private final NotifLifetimeExtender mLifetimeExtender = new NotifLifetimeExtender() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.2
        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public String getName() {
            return "HeadsUpCoordinator";
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void setCallback(NotifLifetimeExtender.OnEndLifetimeExtensionCallback onEndLifetimeExtensionCallback) {
            HeadsUpCoordinator.this.mEndLifetimeExtension = onEndLifetimeExtensionCallback;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry, int i) {
            boolean zIsCurrentlyShowingHun = HeadsUpCoordinator.this.isCurrentlyShowingHun(notificationEntry);
            if (zIsCurrentlyShowingHun) {
                HeadsUpCoordinator.this.mNotifExtendingLifetime = notificationEntry;
            }
            return zIsCurrentlyShowingHun;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void cancelLifetimeExtension(NotificationEntry notificationEntry) {
            if (Objects.equals(HeadsUpCoordinator.this.mNotifExtendingLifetime, notificationEntry)) {
                HeadsUpCoordinator.this.mNotifExtendingLifetime = null;
            }
        }
    };
    private final OnHeadsUpChangedListener mOnHeadsUpChangedListener = new OnHeadsUpChangedListener() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.5
        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
            NotificationEntry topEntry = HeadsUpCoordinator.this.mHeadsUpManager.getTopEntry();
            if (!Objects.equals(HeadsUpCoordinator.this.mCurrentHun, topEntry)) {
                HeadsUpCoordinator.this.endNotifLifetimeExtension();
                HeadsUpCoordinator.this.mCurrentHun = topEntry;
                HeadsUpCoordinator.this.mNotifPromoter.invalidateList();
                HeadsUpCoordinator.this.mNotifSection.invalidateList();
            }
            if (z) {
                return;
            }
            HeadsUpCoordinator.this.mHeadsUpViewBinder.unbindHeadsUpView(notificationEntry);
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$400(HeadsUpCoordinator headsUpCoordinator, NotificationEntry notificationEntry) {
        headsUpCoordinator.onHeadsUpViewBound(notificationEntry);
    }

    public HeadsUpCoordinator(HeadsUpManager headsUpManager, HeadsUpViewBinder headsUpViewBinder, NotificationInterruptStateProvider notificationInterruptStateProvider, NotificationRemoteInputManager notificationRemoteInputManager) {
        String str = "HeadsUpCoordinator";
        this.mNotifPromoter = new NotifPromoter(str) { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.3
            @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter
            public boolean shouldPromoteToTopLevel(NotificationEntry notificationEntry) {
                return HeadsUpCoordinator.this.isCurrentlyShowingHun(notificationEntry);
            }
        };
        this.mNotifSection = new NotifSection(str) { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.4
            @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection
            public boolean isInSection(ListEntry listEntry) {
                return HeadsUpCoordinator.this.isCurrentlyShowingHun(listEntry);
            }
        };
        this.mHeadsUpManager = headsUpManager;
        this.mHeadsUpViewBinder = headsUpViewBinder;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mRemoteInputManager = notificationRemoteInputManager;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mHeadsUpManager.addListener(this.mOnHeadsUpChangedListener);
        notifPipeline.addCollectionListener(this.mNotifCollectionListener);
        notifPipeline.addPromoter(this.mNotifPromoter);
        notifPipeline.addNotificationLifetimeExtender(this.mLifetimeExtender);
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public NotifSection getSection() {
        return this.mNotifSection;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onHeadsUpViewBound(NotificationEntry notificationEntry) {
        this.mHeadsUpManager.showNotification(notificationEntry);
    }

    /* renamed from: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator$1, reason: invalid class name */
    class AnonymousClass1 implements NotifCollectionListener {
        AnonymousClass1() {
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(NotificationEntry notificationEntry) {
            if (HeadsUpCoordinator.this.mNotificationInterruptStateProvider.shouldHeadsUp(notificationEntry)) {
                HeadsUpViewBinder headsUpViewBinder = HeadsUpCoordinator.this.mHeadsUpViewBinder;
                final HeadsUpCoordinator headsUpCoordinator = HeadsUpCoordinator.this;
                headsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator$1$$ExternalSyntheticLambda1
                    @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                    public final void onBindFinished(NotificationEntry notificationEntry2) {
                        HeadsUpCoordinator.access$400(headsUpCoordinator, notificationEntry2);
                    }
                });
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(NotificationEntry notificationEntry) {
            boolean zAlertAgain = HeadsUpController.alertAgain(notificationEntry, notificationEntry.getSbn().getNotification());
            boolean zShouldHeadsUp = HeadsUpCoordinator.this.mNotificationInterruptStateProvider.shouldHeadsUp(notificationEntry);
            if (HeadsUpCoordinator.this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                if (zShouldHeadsUp) {
                    HeadsUpCoordinator.this.mHeadsUpManager.updateNotification(notificationEntry.getKey(), zAlertAgain);
                    return;
                } else {
                    if (HeadsUpCoordinator.this.mHeadsUpManager.isEntryAutoHeadsUpped(notificationEntry.getKey())) {
                        return;
                    }
                    HeadsUpCoordinator.this.mHeadsUpManager.removeNotification(notificationEntry.getKey(), false);
                    return;
                }
            }
            if (zShouldHeadsUp && zAlertAgain) {
                HeadsUpViewBinder headsUpViewBinder = HeadsUpCoordinator.this.mHeadsUpViewBinder;
                final HeadsUpCoordinator headsUpCoordinator = HeadsUpCoordinator.this;
                headsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator$1$$ExternalSyntheticLambda0
                    @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                    public final void onBindFinished(NotificationEntry notificationEntry2) {
                        HeadsUpCoordinator.access$400(headsUpCoordinator, notificationEntry2);
                    }
                });
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
            String key = notificationEntry.getKey();
            if (HeadsUpCoordinator.this.mHeadsUpManager.isAlerting(key)) {
                HeadsUpCoordinator.this.mHeadsUpManager.removeNotification(notificationEntry.getKey(), HeadsUpCoordinator.this.mRemoteInputManager.getController().isSpinning(key) && !NotificationRemoteInputManager.FORCE_REMOTE_INPUT_HISTORY);
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryCleanUp(NotificationEntry notificationEntry) {
            HeadsUpCoordinator.this.mHeadsUpViewBinder.abortBindCallback(notificationEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isCurrentlyShowingHun(ListEntry listEntry) {
        return this.mCurrentHun == listEntry.getRepresentativeEntry();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void endNotifLifetimeExtension() {
        NotificationEntry notificationEntry = this.mNotifExtendingLifetime;
        if (notificationEntry != null) {
            this.mEndLifetimeExtension.onEndLifetimeExtension(this.mLifetimeExtender, notificationEntry);
            this.mNotifExtendingLifetime = null;
        }
    }
}
