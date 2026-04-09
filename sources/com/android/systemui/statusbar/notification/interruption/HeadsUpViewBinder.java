package com.android.systemui.statusbar.notification.interruption;

import android.util.ArrayMap;
import androidx.core.os.CancellationSignal;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import java.util.Map;

/* loaded from: classes.dex */
public class HeadsUpViewBinder {
    private final NotificationMessagingUtil mNotificationMessagingUtil;
    private NotificationPresenter mNotificationPresenter;
    private final Map<NotificationEntry, CancellationSignal> mOngoingBindCallbacks = new ArrayMap();
    private final RowContentBindStage mStage;

    HeadsUpViewBinder(NotificationMessagingUtil notificationMessagingUtil, RowContentBindStage rowContentBindStage) {
        this.mNotificationMessagingUtil = notificationMessagingUtil;
        this.mStage = rowContentBindStage;
    }

    public void setPresenter(NotificationPresenter notificationPresenter) {
        this.mNotificationPresenter = notificationPresenter;
    }

    public void bindHeadsUpView(NotificationEntry notificationEntry, final NotifBindPipeline.BindCallback bindCallback) {
        final RowContentBindParams stageParams = this.mStage.getStageParams(notificationEntry);
        stageParams.setUseIncreasedHeadsUpHeight(this.mNotificationMessagingUtil.isImportantMessaging(notificationEntry.getSbn(), notificationEntry.getImportance()) && !this.mNotificationPresenter.isPresenterFullyCollapsed());
        stageParams.requireContentViews(4);
        CancellationSignal cancellationSignalRequestRebind = this.mStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
            public final void onBindFinished(NotificationEntry notificationEntry2) {
                HeadsUpViewBinder.lambda$bindHeadsUpView$0(stageParams, bindCallback, notificationEntry2);
            }
        });
        abortBindCallback(notificationEntry);
        this.mOngoingBindCallbacks.put(notificationEntry, cancellationSignalRequestRebind);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$bindHeadsUpView$0(RowContentBindParams rowContentBindParams, NotifBindPipeline.BindCallback bindCallback, NotificationEntry notificationEntry) {
        notificationEntry.getRow().setUsesIncreasedHeadsUpHeight(rowContentBindParams.useIncreasedHeadsUpHeight());
        if (bindCallback != null) {
            bindCallback.onBindFinished(notificationEntry);
        }
    }

    public void abortBindCallback(NotificationEntry notificationEntry) {
        CancellationSignal cancellationSignalRemove = this.mOngoingBindCallbacks.remove(notificationEntry);
        if (cancellationSignalRemove != null) {
            cancellationSignalRemove.cancel();
        }
    }

    public void unbindHeadsUpView(NotificationEntry notificationEntry) {
        abortBindCallback(notificationEntry);
        this.mStage.getStageParams(notificationEntry).markContentViewsFreeable(4);
        this.mStage.requestRebind(notificationEntry, null);
    }
}
