package com.android.systemui.statusbar.phone;

import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.AlertingNotificationManager;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/* loaded from: classes.dex */
public class NotificationGroupAlertTransferHelper implements OnHeadsUpChangedListener, StatusBarStateController.StateListener {
    private NotificationEntryManager mEntryManager;
    private HeadsUpManager mHeadsUpManager;
    private boolean mIsDozing;
    private final RowContentBindStage mRowContentBindStage;
    private final ArrayMap<String, GroupAlertEntry> mGroupAlertEntries = new ArrayMap<>();
    private final ArrayMap<String, PendingAlertInfo> mPendingAlerts = new ArrayMap<>();
    private final NotificationGroupManager mGroupManager = (NotificationGroupManager) Dependency.get(NotificationGroupManager.class);
    private final NotificationGroupManager.OnGroupChangeListener mOnGroupChangeListener = new NotificationGroupManager.OnGroupChangeListener() { // from class: com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper.1
        @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
        public void onGroupCreated(NotificationGroupManager.NotificationGroup notificationGroup, String str) {
            NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.put(str, new GroupAlertEntry(notificationGroup));
        }

        @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
        public void onGroupRemoved(NotificationGroupManager.NotificationGroup notificationGroup, String str) {
            NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.remove(str);
        }

        @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
        public void onGroupSuppressionChanged(NotificationGroupManager.NotificationGroup notificationGroup, boolean z) {
            if (z) {
                if (NotificationGroupAlertTransferHelper.this.mHeadsUpManager.isAlerting(notificationGroup.summary.getKey())) {
                    NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper = NotificationGroupAlertTransferHelper.this;
                    notificationGroupAlertTransferHelper.handleSuppressedSummaryAlerted(notificationGroup.summary, notificationGroupAlertTransferHelper.mHeadsUpManager);
                    return;
                }
                return;
            }
            if (notificationGroup.summary == null) {
                return;
            }
            GroupAlertEntry groupAlertEntry = (GroupAlertEntry) NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.get(NotificationGroupAlertTransferHelper.this.mGroupManager.getGroupKey(notificationGroup.summary.getSbn()));
            if (groupAlertEntry.mAlertSummaryOnNextAddition) {
                if (!NotificationGroupAlertTransferHelper.this.mHeadsUpManager.isAlerting(notificationGroup.summary.getKey())) {
                    NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper2 = NotificationGroupAlertTransferHelper.this;
                    notificationGroupAlertTransferHelper2.alertNotificationWhenPossible(notificationGroup.summary, notificationGroupAlertTransferHelper2.mHeadsUpManager);
                }
                groupAlertEntry.mAlertSummaryOnNextAddition = false;
                return;
            }
            NotificationGroupAlertTransferHelper.this.checkShouldTransferBack(groupAlertEntry);
        }
    };
    private final NotificationEntryListener mNotificationEntryListener = new NotificationEntryListener() { // from class: com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper.2
        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onPendingEntryAdded(NotificationEntry notificationEntry) {
            GroupAlertEntry groupAlertEntry = (GroupAlertEntry) NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.get(NotificationGroupAlertTransferHelper.this.mGroupManager.getGroupKey(notificationEntry.getSbn()));
            if (groupAlertEntry != null) {
                NotificationGroupAlertTransferHelper.this.checkShouldTransferBack(groupAlertEntry);
            }
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
            NotificationGroupAlertTransferHelper.this.mPendingAlerts.remove(notificationEntry.getKey());
        }
    };

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    public NotificationGroupAlertTransferHelper(RowContentBindStage rowContentBindStage) {
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this);
        this.mRowContentBindStage = rowContentBindStage;
    }

    public void bind(NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager) {
        if (this.mEntryManager != null) {
            throw new IllegalStateException("Already bound.");
        }
        this.mEntryManager = notificationEntryManager;
        notificationEntryManager.addNotificationEntryListener(this.mNotificationEntryListener);
        notificationGroupManager.addOnGroupChangeListener(this.mOnGroupChangeListener);
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        if (this.mIsDozing != z) {
            for (GroupAlertEntry groupAlertEntry : this.mGroupAlertEntries.values()) {
                groupAlertEntry.mLastAlertTransferTime = 0L;
                groupAlertEntry.mAlertSummaryOnNextAddition = false;
            }
        }
        this.mIsDozing = z;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        onAlertStateChanged(notificationEntry, z, this.mHeadsUpManager);
    }

    private void onAlertStateChanged(NotificationEntry notificationEntry, boolean z, AlertingNotificationManager alertingNotificationManager) {
        if (z && this.mGroupManager.isSummaryOfSuppressedGroup(notificationEntry.getSbn())) {
            handleSuppressedSummaryAlerted(notificationEntry, alertingNotificationManager);
        }
    }

    private int getPendingChildrenNotAlerting(NotificationGroupManager.NotificationGroup notificationGroup) {
        NotificationEntryManager notificationEntryManager = this.mEntryManager;
        int i = 0;
        if (notificationEntryManager == null) {
            return 0;
        }
        for (NotificationEntry notificationEntry : notificationEntryManager.getPendingNotificationsIterator()) {
            if (isPendingNotificationInGroup(notificationEntry, notificationGroup) && onlySummaryAlerts(notificationEntry)) {
                i++;
            }
        }
        return i;
    }

    private boolean pendingInflationsWillAddChildren(NotificationGroupManager.NotificationGroup notificationGroup) {
        NotificationEntryManager notificationEntryManager = this.mEntryManager;
        if (notificationEntryManager == null) {
            return false;
        }
        Iterator<NotificationEntry> it = notificationEntryManager.getPendingNotificationsIterator().iterator();
        while (it.hasNext()) {
            if (isPendingNotificationInGroup(it.next(), notificationGroup)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPendingNotificationInGroup(NotificationEntry notificationEntry, NotificationGroupManager.NotificationGroup notificationGroup) {
        return this.mGroupManager.isGroupChild(notificationEntry.getSbn()) && Objects.equals(this.mGroupManager.getGroupKey(notificationEntry.getSbn()), this.mGroupManager.getGroupKey(notificationGroup.summary.getSbn())) && !notificationGroup.children.containsKey(notificationEntry.getKey());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSuppressedSummaryAlerted(NotificationEntry notificationEntry, AlertingNotificationManager alertingNotificationManager) {
        NotificationEntry next;
        StatusBarNotification sbn = notificationEntry.getSbn();
        GroupAlertEntry groupAlertEntry = this.mGroupAlertEntries.get(this.mGroupManager.getGroupKey(sbn));
        if (!this.mGroupManager.isSummaryOfSuppressedGroup(notificationEntry.getSbn()) || !alertingNotificationManager.isAlerting(sbn.getKey()) || groupAlertEntry == null || pendingInflationsWillAddChildren(groupAlertEntry.mGroup) || (next = this.mGroupManager.getLogicalChildren(notificationEntry.getSbn()).iterator().next()) == null || next.getRow().keepInParent() || next.isRowRemoved() || next.isRowDismissed()) {
            return;
        }
        if (!alertingNotificationManager.isAlerting(next.getKey()) && onlySummaryAlerts(notificationEntry)) {
            groupAlertEntry.mLastAlertTransferTime = SystemClock.elapsedRealtime();
        }
        transferAlertState(notificationEntry, next, alertingNotificationManager);
    }

    private void transferAlertState(NotificationEntry notificationEntry, NotificationEntry notificationEntry2, AlertingNotificationManager alertingNotificationManager) {
        alertingNotificationManager.removeNotification(notificationEntry.getKey(), true);
        alertNotificationWhenPossible(notificationEntry2, alertingNotificationManager);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkShouldTransferBack(GroupAlertEntry groupAlertEntry) {
        if (SystemClock.elapsedRealtime() - groupAlertEntry.mLastAlertTransferTime < 300) {
            NotificationEntry notificationEntry = groupAlertEntry.mGroup.summary;
            if (onlySummaryAlerts(notificationEntry)) {
                ArrayList<NotificationEntry> logicalChildren = this.mGroupManager.getLogicalChildren(notificationEntry.getSbn());
                int size = logicalChildren.size();
                int pendingChildrenNotAlerting = getPendingChildrenNotAlerting(groupAlertEntry.mGroup);
                int i = size + pendingChildrenNotAlerting;
                if (i <= 1) {
                    return;
                }
                boolean z = false;
                for (int i2 = 0; i2 < logicalChildren.size(); i2++) {
                    NotificationEntry notificationEntry2 = logicalChildren.get(i2);
                    if (onlySummaryAlerts(notificationEntry2) && this.mHeadsUpManager.isAlerting(notificationEntry2.getKey())) {
                        this.mHeadsUpManager.removeNotification(notificationEntry2.getKey(), true);
                        z = true;
                    }
                    if (this.mPendingAlerts.containsKey(notificationEntry2.getKey())) {
                        this.mPendingAlerts.get(notificationEntry2.getKey()).mAbortOnInflation = true;
                        z = true;
                    }
                }
                if (!z || this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                    return;
                }
                if (i - pendingChildrenNotAlerting > 1) {
                    alertNotificationWhenPossible(notificationEntry, this.mHeadsUpManager);
                } else {
                    groupAlertEntry.mAlertSummaryOnNextAddition = true;
                }
                groupAlertEntry.mLastAlertTransferTime = 0L;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void alertNotificationWhenPossible(final NotificationEntry notificationEntry, AlertingNotificationManager alertingNotificationManager) {
        final int contentFlag = alertingNotificationManager.getContentFlag();
        RowContentBindParams stageParams = this.mRowContentBindStage.getStageParams(notificationEntry);
        if ((stageParams.getContentViews() & contentFlag) == 0) {
            this.mPendingAlerts.put(notificationEntry.getKey(), new PendingAlertInfo(notificationEntry));
            stageParams.requireContentViews(contentFlag);
            this.mRowContentBindStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper$$ExternalSyntheticLambda0
                @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                public final void onBindFinished(NotificationEntry notificationEntry2) {
                    this.f$0.lambda$alertNotificationWhenPossible$0(notificationEntry, contentFlag, notificationEntry2);
                }
            });
        } else if (alertingNotificationManager.isAlerting(notificationEntry.getKey())) {
            alertingNotificationManager.updateNotification(notificationEntry.getKey(), true);
        } else {
            alertingNotificationManager.showNotification(notificationEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$alertNotificationWhenPossible$0(NotificationEntry notificationEntry, int i, NotificationEntry notificationEntry2) {
        PendingAlertInfo pendingAlertInfoRemove = this.mPendingAlerts.remove(notificationEntry.getKey());
        if (pendingAlertInfoRemove != null) {
            if (pendingAlertInfoRemove.isStillValid()) {
                alertNotificationWhenPossible(notificationEntry, this.mHeadsUpManager);
            } else {
                this.mRowContentBindStage.getStageParams(notificationEntry).markContentViewsFreeable(i);
                this.mRowContentBindStage.requestRebind(notificationEntry, null);
            }
        }
    }

    private boolean onlySummaryAlerts(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getNotification().getGroupAlertBehavior() == 1;
    }

    private class PendingAlertInfo {
        boolean mAbortOnInflation;
        final NotificationEntry mEntry;
        final StatusBarNotification mOriginalNotification;

        PendingAlertInfo(NotificationEntry notificationEntry) {
            this.mOriginalNotification = notificationEntry.getSbn();
            this.mEntry = notificationEntry;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isStillValid() {
            return !this.mAbortOnInflation && this.mEntry.getSbn().getGroupKey() == this.mOriginalNotification.getGroupKey() && this.mEntry.getSbn().getNotification().isGroupSummary() == this.mOriginalNotification.getNotification().isGroupSummary();
        }
    }

    private static class GroupAlertEntry {
        boolean mAlertSummaryOnNextAddition;
        final NotificationGroupManager.NotificationGroup mGroup;
        long mLastAlertTransferTime;

        GroupAlertEntry(NotificationGroupManager.NotificationGroup notificationGroup) {
            this.mGroup = notificationGroup;
        }
    }
}
