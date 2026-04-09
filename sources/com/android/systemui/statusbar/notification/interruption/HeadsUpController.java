package com.android.systemui.statusbar.notification.interruption;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.Objects;

/* loaded from: classes.dex */
public class HeadsUpController {
    private final HeadsUpManager mHeadsUpManager;
    private final HeadsUpViewBinder mHeadsUpViewBinder;
    private final NotificationInterruptStateProvider mInterruptStateProvider;
    private final NotificationListener mNotificationListener;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final StatusBarStateController mStatusBarStateController;
    private final VisualStabilityManager mVisualStabilityManager;
    private NotifCollectionListener mCollectionListener = new AnonymousClass1();
    private OnHeadsUpChangedListener mOnHeadsUpChangedListener = new OnHeadsUpChangedListener() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController.2
        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
            if (z || notificationEntry.getRow().isRemoved()) {
                return;
            }
            HeadsUpController.this.mHeadsUpViewBinder.unbindHeadsUpView(notificationEntry);
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$400(HeadsUpController headsUpController, NotificationEntry notificationEntry) {
        headsUpController.showAlertingView(notificationEntry);
    }

    HeadsUpController(HeadsUpViewBinder headsUpViewBinder, NotificationInterruptStateProvider notificationInterruptStateProvider, HeadsUpManager headsUpManager, NotificationRemoteInputManager notificationRemoteInputManager, StatusBarStateController statusBarStateController, VisualStabilityManager visualStabilityManager, NotificationListener notificationListener) {
        this.mHeadsUpViewBinder = headsUpViewBinder;
        this.mHeadsUpManager = headsUpManager;
        this.mInterruptStateProvider = notificationInterruptStateProvider;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mStatusBarStateController = statusBarStateController;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mNotificationListener = notificationListener;
    }

    public void attach(NotificationEntryManager notificationEntryManager, HeadsUpManager headsUpManager) {
        notificationEntryManager.addCollectionListener(this.mCollectionListener);
        headsUpManager.addListener(this.mOnHeadsUpChangedListener);
    }

    /* renamed from: com.android.systemui.statusbar.notification.interruption.HeadsUpController$1, reason: invalid class name */
    class AnonymousClass1 implements NotifCollectionListener {
        AnonymousClass1() {
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(NotificationEntry notificationEntry) {
            if (HeadsUpController.this.mInterruptStateProvider.shouldHeadsUp(notificationEntry)) {
                HeadsUpViewBinder headsUpViewBinder = HeadsUpController.this.mHeadsUpViewBinder;
                final HeadsUpController headsUpController = HeadsUpController.this;
                headsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController$1$$ExternalSyntheticLambda0
                    @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                    public final void onBindFinished(NotificationEntry notificationEntry2) {
                        HeadsUpController.access$400(headsUpController, notificationEntry2);
                    }
                });
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(NotificationEntry notificationEntry) {
            HeadsUpController.this.updateHunState(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
            HeadsUpController.this.stopAlerting(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryCleanUp(NotificationEntry notificationEntry) {
            HeadsUpController.this.mHeadsUpViewBinder.abortBindCallback(notificationEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAlertingView(NotificationEntry notificationEntry) {
        this.mHeadsUpManager.showNotification(notificationEntry);
        if (this.mStatusBarStateController.isDozing()) {
            return;
        }
        setNotificationShown(notificationEntry.getSbn());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHunState(NotificationEntry notificationEntry) {
        boolean zAlertAgain = alertAgain(notificationEntry, notificationEntry.getSbn().getNotification());
        boolean zShouldHeadsUp = this.mInterruptStateProvider.shouldHeadsUp(notificationEntry);
        if (this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
            if (zShouldHeadsUp) {
                this.mHeadsUpManager.updateNotification(notificationEntry.getKey(), zAlertAgain);
                return;
            } else {
                if (this.mHeadsUpManager.isEntryAutoHeadsUpped(notificationEntry.getKey())) {
                    return;
                }
                this.mHeadsUpManager.removeNotification(notificationEntry.getKey(), false);
                return;
            }
        }
        if (zShouldHeadsUp && zAlertAgain) {
            HeadsUpViewBinder headsUpViewBinder = this.mHeadsUpViewBinder;
            final HeadsUpManager headsUpManager = this.mHeadsUpManager;
            Objects.requireNonNull(headsUpManager);
            headsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController$$ExternalSyntheticLambda0
                @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                public final void onBindFinished(NotificationEntry notificationEntry2) {
                    headsUpManager.showNotification(notificationEntry2);
                }
            });
        }
    }

    private void setNotificationShown(StatusBarNotification statusBarNotification) {
        try {
            this.mNotificationListener.setNotificationsShown(new String[]{statusBarNotification.getKey()});
        } catch (RuntimeException e) {
            Log.d("HeadsUpBindController", "failed setNotificationsShown: ", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopAlerting(NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        if (this.mHeadsUpManager.isAlerting(key)) {
            this.mHeadsUpManager.removeNotification(key, (this.mRemoteInputManager.getController().isSpinning(key) && !NotificationRemoteInputManager.FORCE_REMOTE_INPUT_HISTORY) || !this.mVisualStabilityManager.isReorderingAllowed());
        }
    }

    public static boolean alertAgain(NotificationEntry notificationEntry, Notification notification) {
        return notificationEntry == null || !notificationEntry.hasInterrupted() || (notification.flags & 8) == 0;
    }
}
