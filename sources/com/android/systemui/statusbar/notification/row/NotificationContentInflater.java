package com.android.systemui.statusbar.notification.row;

import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ImageMessageConsumer;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.media.MediaFeatureFlag;
import com.android.systemui.statusbar.InflationTask;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.ConversationNotificationProcessor;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.MediaNotificationProcessor;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.InflatedSmartReplies;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.util.Assert;
import dagger.Lazy;
import java.util.HashMap;
import java.util.concurrent.Executor;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
/* loaded from: classes.dex */
public class NotificationContentInflater implements NotificationRowContentBinder {
    private final Executor mBgExecutor;
    private final ConversationNotificationProcessor mConversationProcessor;
    private boolean mInflateSynchronously = false;
    private final boolean mIsMediaInQS;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final NotifRemoteViewCache mRemoteViewCache;
    private final Lazy<SmartReplyConstants> mSmartReplyConstants;
    private final Lazy<SmartReplyController> mSmartReplyController;

    NotificationContentInflater(NotifRemoteViewCache notifRemoteViewCache, NotificationRemoteInputManager notificationRemoteInputManager, Lazy<SmartReplyConstants> lazy, Lazy<SmartReplyController> lazy2, ConversationNotificationProcessor conversationNotificationProcessor, MediaFeatureFlag mediaFeatureFlag, Executor executor) {
        this.mRemoteViewCache = notifRemoteViewCache;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mSmartReplyConstants = lazy;
        this.mSmartReplyController = lazy2;
        this.mConversationProcessor = conversationNotificationProcessor;
        this.mIsMediaInQS = mediaFeatureFlag.getEnabled();
        this.mBgExecutor = executor;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder
    public void bindContent(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i, NotificationRowContentBinder.BindParams bindParams, boolean z, NotificationRowContentBinder.InflationCallback inflationCallback) {
        if (expandableNotificationRow.isRemoved()) {
            return;
        }
        expandableNotificationRow.getImageResolver().preloadImages(notificationEntry.getSbn().getNotification());
        if (z) {
            this.mRemoteViewCache.clearCache(notificationEntry);
        }
        cancelContentViewFrees(expandableNotificationRow, i);
        AsyncInflationTask asyncInflationTask = new AsyncInflationTask(this.mBgExecutor, this.mInflateSynchronously, i, this.mRemoteViewCache, notificationEntry, this.mSmartReplyConstants.get(), this.mSmartReplyController.get(), this.mConversationProcessor, expandableNotificationRow, bindParams.isLowPriority, bindParams.usesIncreasedHeight, bindParams.usesIncreasedHeadsUpHeight, inflationCallback, this.mRemoteInputManager.getRemoteViewsOnClickHandler(), this.mIsMediaInQS);
        if (this.mInflateSynchronously) {
            asyncInflationTask.onPostExecute(asyncInflationTask.doInBackground(new Void[0]));
        } else {
            asyncInflationTask.executeOnExecutor(this.mBgExecutor, new Void[0]);
        }
    }

    @VisibleForTesting
    InflationProgress inflateNotificationViews(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, NotificationRowContentBinder.BindParams bindParams, boolean z, int i, Notification.Builder builder, Context context) throws Resources.NotFoundException {
        InflationProgress inflationProgressInflateSmartReplyViews = inflateSmartReplyViews(createRemoteViews(i, builder, bindParams.isLowPriority, bindParams.usesIncreasedHeight, bindParams.usesIncreasedHeadsUpHeight, context), i, notificationEntry, expandableNotificationRow.getContext(), context, expandableNotificationRow.getHeadsUpManager(), this.mSmartReplyConstants.get(), this.mSmartReplyController.get(), expandableNotificationRow.getExistingSmartRepliesAndActions());
        apply(this.mBgExecutor, z, inflationProgressInflateSmartReplyViews, i, this.mRemoteViewCache, notificationEntry, expandableNotificationRow, this.mRemoteInputManager.getRemoteViewsOnClickHandler(), null);
        return inflationProgressInflateSmartReplyViews;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder
    public void cancelBind(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        notificationEntry.abortTask();
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder
    public void unbindContent(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i) {
        int i2 = 1;
        while (i != 0) {
            if ((i & i2) != 0) {
                freeNotificationView(notificationEntry, expandableNotificationRow, i2);
            }
            i &= ~i2;
            i2 <<= 1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$freeNotificationView$0(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPrivateLayout().setContractedChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$freeNotificationView$1(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) throws Resources.NotFoundException {
        expandableNotificationRow.getPrivateLayout().setExpandedChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$freeNotificationView$2(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) throws Resources.NotFoundException {
        expandableNotificationRow.getPrivateLayout().setHeadsUpChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 4);
        expandableNotificationRow.getPrivateLayout().setHeadsUpInflatedSmartReplies(null);
    }

    private void freeNotificationView(final NotificationEntry notificationEntry, final ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i == 1) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(0, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$freeNotificationView$0(expandableNotificationRow, notificationEntry);
                }
            });
            return;
        }
        if (i == 2) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(1, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() throws Resources.NotFoundException {
                    this.f$0.lambda$freeNotificationView$1(expandableNotificationRow, notificationEntry);
                }
            });
        } else if (i == 4) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(2, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() throws Resources.NotFoundException {
                    this.f$0.lambda$freeNotificationView$2(expandableNotificationRow, notificationEntry);
                }
            });
        } else {
            if (i != 8) {
                return;
            }
            expandableNotificationRow.getPublicLayout().performWhenContentInactive(0, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$freeNotificationView$3(expandableNotificationRow, notificationEntry);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$freeNotificationView$3(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPublicLayout().setContractedChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 8);
    }

    private void cancelContentViewFrees(ExpandableNotificationRow expandableNotificationRow, int i) {
        if ((i & 1) != 0) {
            expandableNotificationRow.getPrivateLayout().removeContentInactiveRunnable(0);
        }
        if ((i & 2) != 0) {
            expandableNotificationRow.getPrivateLayout().removeContentInactiveRunnable(1);
        }
        if ((i & 4) != 0) {
            expandableNotificationRow.getPrivateLayout().removeContentInactiveRunnable(2);
        }
        if ((i & 8) != 0) {
            expandableNotificationRow.getPublicLayout().removeContentInactiveRunnable(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static InflationProgress inflateSmartReplyViews(InflationProgress inflationProgress, int i, NotificationEntry notificationEntry, Context context, Context context2, HeadsUpManager headsUpManager, SmartReplyConstants smartReplyConstants, SmartReplyController smartReplyController, InflatedSmartReplies.SmartRepliesAndActions smartRepliesAndActions) {
        if ((i & 2) != 0 && inflationProgress.newExpandedView != null) {
            inflationProgress.expandedInflatedSmartReplies = InflatedSmartReplies.inflate(context, context2, notificationEntry, smartReplyConstants, smartReplyController, headsUpManager, smartRepliesAndActions);
        }
        if ((i & 4) != 0 && inflationProgress.newHeadsUpView != null) {
            inflationProgress.headsUpInflatedSmartReplies = InflatedSmartReplies.inflate(context, context2, notificationEntry, smartReplyConstants, smartReplyController, headsUpManager, smartRepliesAndActions);
        }
        return inflationProgress;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static InflationProgress createRemoteViews(int i, Notification.Builder builder, boolean z, boolean z2, boolean z3, Context context) {
        InflationProgress inflationProgress = new InflationProgress();
        if ((i & 1) != 0) {
            inflationProgress.newContentView = createContentView(builder, z, z2);
        }
        if ((i & 2) != 0) {
            inflationProgress.newExpandedView = createExpandedView(builder, z);
        }
        if ((i & 4) != 0) {
            inflationProgress.newHeadsUpView = builder.createHeadsUpContentView(z3);
        }
        if ((i & 8) != 0) {
            inflationProgress.newPublicView = builder.makePublicContentView(z);
        }
        inflationProgress.packageContext = context;
        inflationProgress.headsUpStatusBarText = builder.getHeadsUpStatusBarText(false);
        inflationProgress.headsUpStatusBarTextPublic = builder.getHeadsUpStatusBarText(true);
        return inflationProgress;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CancellationSignal apply(Executor executor, boolean z, final InflationProgress inflationProgress, int i, NotifRemoteViewCache notifRemoteViewCache, NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, RemoteViews.OnClickHandler onClickHandler, NotificationRowContentBinder.InflationCallback inflationCallback) throws Resources.NotFoundException {
        HashMap map;
        NotificationContentView notificationContentView;
        NotificationContentView notificationContentView2;
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        NotificationContentView publicLayout = expandableNotificationRow.getPublicLayout();
        HashMap map2 = new HashMap();
        if ((i & 1) != 0) {
            map = map2;
            notificationContentView = publicLayout;
            notificationContentView2 = privateLayout;
            applyRemoteView(executor, z, inflationProgress, i, 1, notifRemoteViewCache, notificationEntry, expandableNotificationRow, !canReapplyRemoteView(inflationProgress.newContentView, notifRemoteViewCache.getCachedView(notificationEntry, 1)), onClickHandler, inflationCallback, privateLayout, privateLayout.getContractedChild(), privateLayout.getVisibleWrapper(0), map, new ApplyCallback() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater.1
                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public void setResultView(View view) {
                    inflationProgress.inflatedContentView = view;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public RemoteViews getRemoteView() {
                    return inflationProgress.newContentView;
                }
            });
        } else {
            map = map2;
            notificationContentView = publicLayout;
            notificationContentView2 = privateLayout;
        }
        if ((i & 2) != 0 && inflationProgress.newExpandedView != null) {
            applyRemoteView(executor, z, inflationProgress, i, 2, notifRemoteViewCache, notificationEntry, expandableNotificationRow, !canReapplyRemoteView(inflationProgress.newExpandedView, notifRemoteViewCache.getCachedView(notificationEntry, 2)), onClickHandler, inflationCallback, notificationContentView2, notificationContentView2.getExpandedChild(), notificationContentView2.getVisibleWrapper(1), map, new ApplyCallback() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater.2
                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public void setResultView(View view) {
                    inflationProgress.inflatedExpandedView = view;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public RemoteViews getRemoteView() {
                    return inflationProgress.newExpandedView;
                }
            });
        }
        if ((i & 4) != 0 && inflationProgress.newHeadsUpView != null) {
            NotificationContentView notificationContentView3 = notificationContentView2;
            applyRemoteView(executor, z, inflationProgress, i, 4, notifRemoteViewCache, notificationEntry, expandableNotificationRow, !canReapplyRemoteView(inflationProgress.newHeadsUpView, notifRemoteViewCache.getCachedView(notificationEntry, 4)), onClickHandler, inflationCallback, notificationContentView3, notificationContentView2.getHeadsUpChild(), notificationContentView3.getVisibleWrapper(2), map, new ApplyCallback() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater.3
                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public void setResultView(View view) {
                    inflationProgress.inflatedHeadsUpView = view;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public RemoteViews getRemoteView() {
                    return inflationProgress.newHeadsUpView;
                }
            });
        }
        if ((i & 8) != 0) {
            NotificationContentView notificationContentView4 = notificationContentView;
            applyRemoteView(executor, z, inflationProgress, i, 8, notifRemoteViewCache, notificationEntry, expandableNotificationRow, !canReapplyRemoteView(inflationProgress.newPublicView, notifRemoteViewCache.getCachedView(notificationEntry, 8)), onClickHandler, inflationCallback, notificationContentView4, notificationContentView.getContractedChild(), notificationContentView4.getVisibleWrapper(0), map, new ApplyCallback() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater.4
                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public void setResultView(View view) {
                    inflationProgress.inflatedPublicView = view;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationContentInflater.ApplyCallback
                public RemoteViews getRemoteView() {
                    return inflationProgress.newPublicView;
                }
            });
        }
        finishIfDone(inflationProgress, i, notifRemoteViewCache, map, inflationCallback, notificationEntry, expandableNotificationRow);
        CancellationSignal cancellationSignal = new CancellationSignal();
        final HashMap map3 = map;
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater$$ExternalSyntheticLambda0
            @Override // android.os.CancellationSignal.OnCancelListener
            public final void onCancel() {
                NotificationContentInflater.lambda$apply$4(map3);
            }
        });
        return cancellationSignal;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$apply$4(HashMap map) {
        map.values().forEach(NotificationContentInflater$$ExternalSyntheticLambda5.INSTANCE);
    }

    @VisibleForTesting
    static void applyRemoteView(Executor executor, boolean z, final InflationProgress inflationProgress, final int i, final int i2, final NotifRemoteViewCache notifRemoteViewCache, final NotificationEntry notificationEntry, final ExpandableNotificationRow expandableNotificationRow, final boolean z2, final RemoteViews.OnClickHandler onClickHandler, final NotificationRowContentBinder.InflationCallback inflationCallback, final NotificationContentView notificationContentView, final View view, final NotificationViewWrapper notificationViewWrapper, final HashMap<Integer, CancellationSignal> map, final ApplyCallback applyCallback) {
        CancellationSignal cancellationSignalReapplyAsync;
        final RemoteViews remoteView = applyCallback.getRemoteView();
        if (z) {
            try {
                if (z2) {
                    View viewApply = remoteView.apply(inflationProgress.packageContext, notificationContentView, onClickHandler);
                    viewApply.setIsRootNamespace(true);
                    applyCallback.setResultView(viewApply);
                } else {
                    remoteView.reapply(inflationProgress.packageContext, view, onClickHandler);
                    notificationViewWrapper.onReinflated();
                }
                return;
            } catch (Exception e) {
                handleInflationError(map, e, expandableNotificationRow.getEntry(), inflationCallback);
                map.put(Integer.valueOf(i2), new CancellationSignal());
                return;
            }
        }
        RemoteViews.OnViewAppliedListener onViewAppliedListener = new RemoteViews.OnViewAppliedListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentInflater.5
            public void onViewInflated(View view2) {
                if (view2 instanceof ImageMessageConsumer) {
                    ((ImageMessageConsumer) view2).setImageResolver(expandableNotificationRow.getImageResolver());
                }
            }

            public void onViewApplied(View view2) throws Resources.NotFoundException {
                if (z2) {
                    view2.setIsRootNamespace(true);
                    applyCallback.setResultView(view2);
                } else {
                    NotificationViewWrapper notificationViewWrapper2 = notificationViewWrapper;
                    if (notificationViewWrapper2 != null) {
                        notificationViewWrapper2.onReinflated();
                    }
                }
                map.remove(Integer.valueOf(i2));
                NotificationContentInflater.finishIfDone(inflationProgress, i, notifRemoteViewCache, map, inflationCallback, notificationEntry, expandableNotificationRow);
            }

            public void onError(Exception exc) {
                try {
                    View viewApply2 = view;
                    if (z2) {
                        viewApply2 = remoteView.apply(inflationProgress.packageContext, notificationContentView, onClickHandler);
                    } else {
                        remoteView.reapply(inflationProgress.packageContext, viewApply2, onClickHandler);
                    }
                    Log.wtf("NotifContentInflater", "Async Inflation failed but normal inflation finished normally.", exc);
                    onViewApplied(viewApply2);
                } catch (Exception unused) {
                    map.remove(Integer.valueOf(i2));
                    NotificationContentInflater.handleInflationError(map, exc, expandableNotificationRow.getEntry(), inflationCallback);
                }
            }
        };
        if (z2) {
            cancellationSignalReapplyAsync = remoteView.applyAsync(inflationProgress.packageContext, notificationContentView, executor, onViewAppliedListener, onClickHandler);
        } else {
            cancellationSignalReapplyAsync = remoteView.reapplyAsync(inflationProgress.packageContext, view, executor, onViewAppliedListener, onClickHandler);
        }
        map.put(Integer.valueOf(i2), cancellationSignalReapplyAsync);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void handleInflationError(HashMap<Integer, CancellationSignal> map, Exception exc, NotificationEntry notificationEntry, NotificationRowContentBinder.InflationCallback inflationCallback) {
        Assert.isMainThread();
        map.values().forEach(NotificationContentInflater$$ExternalSyntheticLambda5.INSTANCE);
        if (inflationCallback != null) {
            inflationCallback.handleInflationException(notificationEntry, exc);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean finishIfDone(InflationProgress inflationProgress, int i, NotifRemoteViewCache notifRemoteViewCache, HashMap<Integer, CancellationSignal> map, NotificationRowContentBinder.InflationCallback inflationCallback, NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) throws Resources.NotFoundException {
        Assert.isMainThread();
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        NotificationContentView publicLayout = expandableNotificationRow.getPublicLayout();
        if (!map.isEmpty()) {
            return false;
        }
        if ((i & 1) != 0) {
            if (inflationProgress.inflatedContentView == null) {
                if (notifRemoteViewCache.hasCachedView(notificationEntry, 1)) {
                    notifRemoteViewCache.putCachedView(notificationEntry, 1, inflationProgress.newContentView);
                }
            } else {
                privateLayout.setContractedChild(inflationProgress.inflatedContentView);
                notifRemoteViewCache.putCachedView(notificationEntry, 1, inflationProgress.newContentView);
            }
        }
        if ((i & 2) != 0) {
            if (inflationProgress.inflatedExpandedView != null) {
                privateLayout.setExpandedChild(inflationProgress.inflatedExpandedView);
                notifRemoteViewCache.putCachedView(notificationEntry, 2, inflationProgress.newExpandedView);
            } else if (inflationProgress.newExpandedView == null) {
                privateLayout.setExpandedChild(null);
                notifRemoteViewCache.removeCachedView(notificationEntry, 2);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 2)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 2, inflationProgress.newExpandedView);
            }
            if (inflationProgress.newExpandedView != null) {
                privateLayout.setExpandedInflatedSmartReplies(inflationProgress.expandedInflatedSmartReplies);
            } else {
                privateLayout.setExpandedInflatedSmartReplies(null);
            }
            expandableNotificationRow.setExpandable(inflationProgress.newExpandedView != null);
        }
        if ((i & 4) != 0) {
            if (inflationProgress.inflatedHeadsUpView != null) {
                privateLayout.setHeadsUpChild(inflationProgress.inflatedHeadsUpView);
                notifRemoteViewCache.putCachedView(notificationEntry, 4, inflationProgress.newHeadsUpView);
            } else if (inflationProgress.newHeadsUpView == null) {
                privateLayout.setHeadsUpChild(null);
                notifRemoteViewCache.removeCachedView(notificationEntry, 4);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 4)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 4, inflationProgress.newHeadsUpView);
            }
            if (inflationProgress.newHeadsUpView != null) {
                privateLayout.setHeadsUpInflatedSmartReplies(inflationProgress.headsUpInflatedSmartReplies);
            } else {
                privateLayout.setHeadsUpInflatedSmartReplies(null);
            }
        }
        if ((i & 8) != 0) {
            if (inflationProgress.inflatedPublicView == null) {
                if (notifRemoteViewCache.hasCachedView(notificationEntry, 8)) {
                    notifRemoteViewCache.putCachedView(notificationEntry, 8, inflationProgress.newPublicView);
                }
            } else {
                publicLayout.setContractedChild(inflationProgress.inflatedPublicView);
                notifRemoteViewCache.putCachedView(notificationEntry, 8, inflationProgress.newPublicView);
            }
        }
        notificationEntry.headsUpStatusBarText = inflationProgress.headsUpStatusBarText;
        notificationEntry.headsUpStatusBarTextPublic = inflationProgress.headsUpStatusBarTextPublic;
        if (inflationCallback != null) {
            inflationCallback.onAsyncInflationFinished(notificationEntry);
        }
        return true;
    }

    private static RemoteViews createExpandedView(Notification.Builder builder, boolean z) {
        RemoteViews remoteViewsCreateBigContentView = builder.createBigContentView();
        if (remoteViewsCreateBigContentView != null) {
            return remoteViewsCreateBigContentView;
        }
        if (!z) {
            return null;
        }
        RemoteViews remoteViewsCreateContentView = builder.createContentView();
        Notification.Builder.makeHeaderExpanded(remoteViewsCreateContentView);
        return remoteViewsCreateContentView;
    }

    private static RemoteViews createContentView(Notification.Builder builder, boolean z, boolean z2) {
        if (z) {
            return builder.makeLowPriorityContentView(false);
        }
        return builder.createContentView(z2);
    }

    @VisibleForTesting
    static boolean canReapplyRemoteView(RemoteViews remoteViews, RemoteViews remoteViews2) {
        if (remoteViews == null && remoteViews2 == null) {
            return true;
        }
        return (remoteViews == null || remoteViews2 == null || remoteViews2.getPackage() == null || remoteViews.getPackage() == null || !remoteViews.getPackage().equals(remoteViews2.getPackage()) || remoteViews.getLayoutId() != remoteViews2.getLayoutId() || remoteViews2.hasFlags(1)) ? false : true;
    }

    @VisibleForTesting
    public void setInflateSynchronously(boolean z) {
        this.mInflateSynchronously = z;
    }

    public static class AsyncInflationTask extends AsyncTask<Void, Void, InflationProgress> implements NotificationRowContentBinder.InflationCallback, InflationTask {
        private final Executor mBgExecutor;
        private final NotificationRowContentBinder.InflationCallback mCallback;
        private CancellationSignal mCancellationSignal;
        private final Context mContext;
        private final ConversationNotificationProcessor mConversationProcessor;
        private final NotificationEntry mEntry;
        private Exception mError;
        private final boolean mInflateSynchronously;
        private final boolean mIsLowPriority;
        private final boolean mIsMediaInQS;
        private final int mReInflateFlags;
        private final NotifRemoteViewCache mRemoteViewCache;
        private RemoteViews.OnClickHandler mRemoteViewClickHandler;
        private ExpandableNotificationRow mRow;
        private final SmartReplyConstants mSmartReplyConstants;
        private final SmartReplyController mSmartReplyController;
        private final boolean mUsesIncreasedHeadsUpHeight;
        private final boolean mUsesIncreasedHeight;

        private AsyncInflationTask(Executor executor, boolean z, int i, NotifRemoteViewCache notifRemoteViewCache, NotificationEntry notificationEntry, SmartReplyConstants smartReplyConstants, SmartReplyController smartReplyController, ConversationNotificationProcessor conversationNotificationProcessor, ExpandableNotificationRow expandableNotificationRow, boolean z2, boolean z3, boolean z4, NotificationRowContentBinder.InflationCallback inflationCallback, RemoteViews.OnClickHandler onClickHandler, boolean z5) {
            this.mEntry = notificationEntry;
            this.mRow = expandableNotificationRow;
            this.mSmartReplyConstants = smartReplyConstants;
            this.mSmartReplyController = smartReplyController;
            this.mBgExecutor = executor;
            this.mInflateSynchronously = z;
            this.mReInflateFlags = i;
            this.mRemoteViewCache = notifRemoteViewCache;
            this.mContext = expandableNotificationRow.getContext();
            this.mIsLowPriority = z2;
            this.mUsesIncreasedHeight = z3;
            this.mUsesIncreasedHeadsUpHeight = z4;
            this.mRemoteViewClickHandler = onClickHandler;
            this.mCallback = inflationCallback;
            this.mConversationProcessor = conversationNotificationProcessor;
            this.mIsMediaInQS = z5;
            notificationEntry.setInflationTask(this);
        }

        @VisibleForTesting
        public int getReInflateFlags() {
            return this.mReInflateFlags;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public InflationProgress doInBackground(Void... voidArr) {
            try {
                StatusBarNotification sbn = this.mEntry.getSbn();
                Notification.Builder builderRecoverBuilder = Notification.Builder.recoverBuilder(this.mContext, sbn.getNotification());
                Context packageContext = sbn.getPackageContext(this.mContext);
                Context rtlEnabledContext = builderRecoverBuilder.usesTemplate() ? new RtlEnabledContext(packageContext) : packageContext;
                Notification notification = sbn.getNotification();
                if (notification.isMediaNotification() && (!this.mIsMediaInQS || !MediaDataManagerKt.isMediaNotification(sbn))) {
                    new MediaNotificationProcessor(this.mContext, rtlEnabledContext).processNotification(notification, builderRecoverBuilder);
                }
                if (this.mEntry.getRanking().isConversation()) {
                    this.mConversationProcessor.processNotification(this.mEntry, builderRecoverBuilder);
                }
                InflationProgress inflationProgressInflateSmartReplyViews = NotificationContentInflater.inflateSmartReplyViews(NotificationContentInflater.createRemoteViews(this.mReInflateFlags, builderRecoverBuilder, this.mIsLowPriority, this.mUsesIncreasedHeight, this.mUsesIncreasedHeadsUpHeight, rtlEnabledContext), this.mReInflateFlags, this.mEntry, this.mRow.getContext(), rtlEnabledContext, this.mRow.getHeadsUpManager(), this.mSmartReplyConstants, this.mSmartReplyController, this.mRow.getExistingSmartRepliesAndActions());
                this.mRow.getImageResolver().waitForPreloadedImages(1000L);
                return inflationProgressInflateSmartReplyViews;
            } catch (Exception e) {
                this.mError = e;
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(InflationProgress inflationProgress) {
            Exception exc = this.mError;
            if (exc == null) {
                this.mCancellationSignal = NotificationContentInflater.apply(this.mBgExecutor, this.mInflateSynchronously, inflationProgress, this.mReInflateFlags, this.mRemoteViewCache, this.mEntry, this.mRow, this.mRemoteViewClickHandler, this);
            } else {
                handleError(exc);
            }
        }

        private void handleError(Exception exc) {
            this.mEntry.onInflationTaskFinished();
            StatusBarNotification sbn = this.mEntry.getSbn();
            Log.e("StatusBar", "couldn't inflate view for notification " + (sbn.getPackageName() + "/0x" + Integer.toHexString(sbn.getId())), exc);
            NotificationRowContentBinder.InflationCallback inflationCallback = this.mCallback;
            if (inflationCallback != null) {
                inflationCallback.handleInflationException(this.mRow.getEntry(), new InflationException("Couldn't inflate contentViews" + exc));
            }
            this.mRow.getImageResolver().cancelRunningTasks();
        }

        @Override // com.android.systemui.statusbar.InflationTask
        public void abort() {
            cancel(true);
            CancellationSignal cancellationSignal = this.mCancellationSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }

        @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
        public void handleInflationException(NotificationEntry notificationEntry, Exception exc) {
            handleError(exc);
        }

        @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
        public void onAsyncInflationFinished(NotificationEntry notificationEntry) {
            this.mEntry.onInflationTaskFinished();
            this.mRow.onNotificationUpdated();
            NotificationRowContentBinder.InflationCallback inflationCallback = this.mCallback;
            if (inflationCallback != null) {
                inflationCallback.onAsyncInflationFinished(this.mEntry);
            }
            this.mRow.getImageResolver().purgeCache();
            this.mRow.getImageResolver().cancelRunningTasks();
        }

        private class RtlEnabledContext extends ContextWrapper {
            private RtlEnabledContext(Context context) {
                super(context);
            }

            @Override // android.content.ContextWrapper, android.content.Context
            public ApplicationInfo getApplicationInfo() {
                ApplicationInfo applicationInfo = super.getApplicationInfo();
                applicationInfo.flags |= 4194304;
                return applicationInfo;
            }
        }
    }

    @VisibleForTesting
    static class InflationProgress {
        private InflatedSmartReplies expandedInflatedSmartReplies;
        private InflatedSmartReplies headsUpInflatedSmartReplies;
        private CharSequence headsUpStatusBarText;
        private CharSequence headsUpStatusBarTextPublic;
        private View inflatedContentView;
        private View inflatedExpandedView;
        private View inflatedHeadsUpView;
        private View inflatedPublicView;
        private RemoteViews newContentView;
        private RemoteViews newExpandedView;
        private RemoteViews newHeadsUpView;
        private RemoteViews newPublicView;

        @VisibleForTesting
        Context packageContext;

        InflationProgress() {
        }
    }

    @VisibleForTesting
    static abstract class ApplyCallback {
        public abstract RemoteViews getRemoteView();

        public abstract void setResultView(View view);

        ApplyCallback() {
        }
    }
}
