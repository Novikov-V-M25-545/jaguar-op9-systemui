package com.android.systemui.statusbar.notification.collection.inflation;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.GroupEntry;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.phone.NotificationGroupManager;

/* loaded from: classes.dex */
public class LowPriorityInflationHelper {
    private final FeatureFlags mFeatureFlags;
    private final NotificationGroupManager mGroupManager;
    private final RowContentBindStage mRowContentBindStage;

    LowPriorityInflationHelper(FeatureFlags featureFlags, NotificationGroupManager notificationGroupManager, RowContentBindStage rowContentBindStage) {
        this.mFeatureFlags = featureFlags;
        this.mGroupManager = notificationGroupManager;
        this.mRowContentBindStage = rowContentBindStage;
    }

    public void recheckLowPriorityViewAndInflate(NotificationEntry notificationEntry, final ExpandableNotificationRow expandableNotificationRow) {
        RowContentBindParams stageParams = this.mRowContentBindStage.getStageParams(notificationEntry);
        final boolean zShouldUseLowPriorityView = shouldUseLowPriorityView(notificationEntry);
        if (expandableNotificationRow.isRemoved() || expandableNotificationRow.isLowPriority() == zShouldUseLowPriorityView) {
            return;
        }
        stageParams.setUseLowPriority(zShouldUseLowPriorityView);
        this.mRowContentBindStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
            public final void onBindFinished(NotificationEntry notificationEntry2) {
                expandableNotificationRow.setIsLowPriority(zShouldUseLowPriorityView);
            }
        });
    }

    public boolean shouldUseLowPriorityView(NotificationEntry notificationEntry) {
        boolean zIsChildInGroupWithSummary;
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            zIsChildInGroupWithSummary = notificationEntry.getParent() != GroupEntry.ROOT_ENTRY;
        } else {
            zIsChildInGroupWithSummary = this.mGroupManager.isChildInGroupWithSummary(notificationEntry.getSbn());
        }
        return notificationEntry.isAmbient() && !zIsChildInGroupWithSummary;
    }
}
