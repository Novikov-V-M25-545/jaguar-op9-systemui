package com.android.systemui.statusbar.notification.row;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.ArraySet;
import androidx.core.os.CancellationSignal;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.row.BindRequester;
import com.android.systemui.statusbar.notification.row.BindStage;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public final class NotifBindPipeline {
    private final NotifCollectionListener mCollectionListener;
    private final NotifBindPipelineLogger mLogger;
    private final Handler mMainHandler;
    private BindStage mStage;
    private final Map<NotificationEntry, BindEntry> mBindEntries = new ArrayMap();
    private final List<BindCallback> mScratchCallbacksList = new ArrayList();

    public interface BindCallback {
        void onBindFinished(NotificationEntry notificationEntry);
    }

    NotifBindPipeline(CommonNotifCollection commonNotifCollection, NotifBindPipelineLogger notifBindPipelineLogger, Looper looper) {
        NotifCollectionListener notifCollectionListener = new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipeline.1
            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryInit(NotificationEntry notificationEntry) {
                NotifBindPipeline.this.mBindEntries.put(notificationEntry, new BindEntry());
                NotifBindPipeline.this.mStage.createStageParams(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryCleanUp(NotificationEntry notificationEntry) {
                ExpandableNotificationRow expandableNotificationRow = ((BindEntry) NotifBindPipeline.this.mBindEntries.remove(notificationEntry)).row;
                if (expandableNotificationRow != null) {
                    NotifBindPipeline.this.mStage.abortStage(notificationEntry, expandableNotificationRow);
                }
                NotifBindPipeline.this.mStage.deleteStageParams(notificationEntry);
                NotifBindPipeline.this.mMainHandler.removeMessages(1, notificationEntry);
            }
        };
        this.mCollectionListener = notifCollectionListener;
        commonNotifCollection.addCollectionListener(notifCollectionListener);
        this.mLogger = notifBindPipelineLogger;
        this.mMainHandler = new NotifBindPipelineHandler(looper);
    }

    public void setStage(BindStage bindStage) {
        this.mLogger.logStageSet(bindStage.getClass().getName());
        this.mStage = bindStage;
        bindStage.setBindRequestListener(new BindRequester.BindRequestListener() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipeline$$ExternalSyntheticLambda1
            @Override // com.android.systemui.statusbar.notification.row.BindRequester.BindRequestListener
            public final void onBindRequest(NotificationEntry notificationEntry, CancellationSignal cancellationSignal, NotifBindPipeline.BindCallback bindCallback) {
                this.f$0.onBindRequested(notificationEntry, cancellationSignal, bindCallback);
            }
        });
    }

    public void manageRow(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        this.mLogger.logManagedRow(notificationEntry.getKey());
        BindEntry bindEntry = getBindEntry(notificationEntry);
        if (bindEntry == null) {
            return;
        }
        bindEntry.row = expandableNotificationRow;
        if (bindEntry.invalidated) {
            requestPipelineRun(notificationEntry);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBindRequested(NotificationEntry notificationEntry, CancellationSignal cancellationSignal, final BindCallback bindCallback) {
        BindEntry bindEntry = getBindEntry(notificationEntry);
        if (bindEntry == null) {
            return;
        }
        bindEntry.invalidated = true;
        if (bindCallback != null) {
            final Set<BindCallback> set = bindEntry.callbacks;
            set.add(bindCallback);
            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipeline$$ExternalSyntheticLambda0
                @Override // androidx.core.os.CancellationSignal.OnCancelListener
                public final void onCancel() {
                    set.remove(bindCallback);
                }
            });
        }
        requestPipelineRun(notificationEntry);
    }

    private void requestPipelineRun(NotificationEntry notificationEntry) {
        this.mLogger.logRequestPipelineRun(notificationEntry.getKey());
        ExpandableNotificationRow expandableNotificationRow = getBindEntry(notificationEntry).row;
        if (expandableNotificationRow == null) {
            this.mLogger.logRequestPipelineRowNotSet(notificationEntry.getKey());
            return;
        }
        this.mStage.abortStage(notificationEntry, expandableNotificationRow);
        if (this.mMainHandler.hasMessages(1, notificationEntry)) {
            return;
        }
        this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 1, notificationEntry));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startPipeline(NotificationEntry notificationEntry) {
        this.mLogger.logStartPipeline(notificationEntry.getKey());
        if (this.mStage == null) {
            throw new IllegalStateException("No stage was ever set on the pipeline");
        }
        this.mStage.executeStage(notificationEntry, this.mBindEntries.get(notificationEntry).row, new BindStage.StageCallback() { // from class: com.android.systemui.statusbar.notification.row.NotifBindPipeline$$ExternalSyntheticLambda2
            @Override // com.android.systemui.statusbar.notification.row.BindStage.StageCallback
            public final void onStageFinished(NotificationEntry notificationEntry2) {
                this.f$0.lambda$startPipeline$1(notificationEntry2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: onPipelineComplete, reason: merged with bridge method [inline-methods] */
    public void lambda$startPipeline$1(NotificationEntry notificationEntry) {
        BindEntry bindEntry = getBindEntry(notificationEntry);
        Set<BindCallback> set = bindEntry.callbacks;
        this.mLogger.logFinishedPipeline(notificationEntry.getKey(), set.size());
        bindEntry.invalidated = false;
        this.mScratchCallbacksList.addAll(set);
        set.clear();
        for (int i = 0; i < this.mScratchCallbacksList.size(); i++) {
            this.mScratchCallbacksList.get(i).onBindFinished(notificationEntry);
        }
        this.mScratchCallbacksList.clear();
    }

    private BindEntry getBindEntry(NotificationEntry notificationEntry) {
        return this.mBindEntries.get(notificationEntry);
    }

    private class BindEntry {
        public final Set<BindCallback> callbacks;
        public boolean invalidated;
        public ExpandableNotificationRow row;

        private BindEntry() {
            this.callbacks = new ArraySet();
        }
    }

    private class NotifBindPipelineHandler extends Handler {
        NotifBindPipelineHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                NotifBindPipeline.this.startPipeline((NotificationEntry) message.obj);
            } else {
                throw new IllegalArgumentException("Unknown message type: " + message.what);
            }
        }
    }
}
