package com.android.systemui.statusbar.notification;

import android.app.AppLockManager;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dumpable;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationRemoveInterceptor;
import com.android.systemui.statusbar.NotificationUiAdjustment;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.util.Assert;
import com.android.systemui.util.leak.LeakDetector;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class NotificationEntryManager implements CommonNotifCollection, Dumpable, VisualStabilityManager.Callback {
    private static final boolean DEBUG = Log.isLoggable("NotificationEntryMgr", 3);
    private final ArrayMap<String, NotificationEntry> mActiveNotifications;
    private final Set<NotificationEntry> mAllNotifications;
    private final AppLockManager.AppLockCallback mAppLockCallback;
    private final AppLockManager mAppLockManager;
    private final Lazy<BubbleController> mBubbleControllerLazy;
    private final FeatureFlags mFeatureFlags;
    private final ForegroundServiceDismissalFeatureController mFgsFeatureController;
    private final NotificationGroupManager mGroupManager;
    private final NotificationRowContentBinder.InflationCallback mInflationCallback;
    private final KeyguardEnvironment mKeyguardEnvironment;
    private NotificationListenerService.RankingMap mLatestRankingMap;
    private final LeakDetector mLeakDetector;
    private final NotificationEntryManagerLogger mLogger;
    private final List<NotifCollectionListener> mNotifCollectionListeners;
    private final NotificationListener.NotificationHandler mNotifListener;
    private final List<NotificationEntryListener> mNotificationEntryListeners;

    @VisibleForTesting
    final ArrayList<NotificationLifetimeExtender> mNotificationLifetimeExtenders;
    private final Lazy<NotificationRowBinder> mNotificationRowBinderLazy;

    @VisibleForTesting
    protected final HashMap<String, NotificationEntry> mPendingNotifications;
    private NotificationPresenter mPresenter;
    private final NotificationRankingManager mRankingManager;
    private final Set<NotificationEntry> mReadOnlyAllNotifications;
    private final List<NotificationEntry> mReadOnlyNotifications;
    private final Lazy<NotificationRemoteInputManager> mRemoteInputManagerLazy;
    private final List<NotificationRemoveInterceptor> mRemoveInterceptors;
    private final Map<NotificationEntry, NotificationLifetimeExtender> mRetainedNotifications;

    @VisibleForTesting
    protected final ArrayList<NotificationEntry> mSortedAndFiltered;

    public interface KeyguardEnvironment {
        boolean isDeviceProvisioned();

        boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAppLockNotification(String str, NotificationEntry notificationEntry) {
        if (str.equals(notificationEntry.getSbn().getPackageName())) {
            notificationEntry.setAppLocked(this.mAppLockManager.isAppLocked(str));
            notificationEntry.onAppStateChanged(!this.mAppLockManager.getAppNotificationHide(str) || this.mAppLockManager.isAppOpen(str));
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationEntryManager state:");
        printWriter.println("  mAllNotifications=");
        if (this.mAllNotifications.size() == 0) {
            printWriter.println("null");
        } else {
            int i = 0;
            Iterator<NotificationEntry> it = this.mAllNotifications.iterator();
            while (it.hasNext()) {
                dumpEntry(printWriter, "  ", i, it.next());
                i++;
            }
        }
        printWriter.print("  mPendingNotifications=");
        if (this.mPendingNotifications.size() == 0) {
            printWriter.println("null");
        } else {
            Iterator<NotificationEntry> it2 = this.mPendingNotifications.values().iterator();
            while (it2.hasNext()) {
                printWriter.println(it2.next().getSbn());
            }
        }
        printWriter.println("  Remove interceptors registered:");
        Iterator<NotificationRemoveInterceptor> it3 = this.mRemoveInterceptors.iterator();
        while (it3.hasNext()) {
            printWriter.println("    " + it3.next().getClass().getSimpleName());
        }
        printWriter.println("  Lifetime extenders registered:");
        Iterator<NotificationLifetimeExtender> it4 = this.mNotificationLifetimeExtenders.iterator();
        while (it4.hasNext()) {
            printWriter.println("    " + it4.next().getClass().getSimpleName());
        }
        printWriter.println("  Lifetime-extended notifications:");
        if (this.mRetainedNotifications.isEmpty()) {
            printWriter.println("    None");
            return;
        }
        for (Map.Entry<NotificationEntry, NotificationLifetimeExtender> entry : this.mRetainedNotifications.entrySet()) {
            printWriter.println("    " + entry.getKey().getSbn() + " retained by " + entry.getValue().getClass().getName());
        }
    }

    public NotificationEntryManager(AppLockManager appLockManager, NotificationEntryManagerLogger notificationEntryManagerLogger, NotificationGroupManager notificationGroupManager, NotificationRankingManager notificationRankingManager, KeyguardEnvironment keyguardEnvironment, FeatureFlags featureFlags, Lazy<NotificationRowBinder> lazy, Lazy<NotificationRemoteInputManager> lazy2, LeakDetector leakDetector, Lazy<BubbleController> lazy3, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        ArraySet arraySet = new ArraySet();
        this.mAllNotifications = arraySet;
        this.mReadOnlyAllNotifications = Collections.unmodifiableSet(arraySet);
        this.mPendingNotifications = new HashMap<>();
        this.mActiveNotifications = new ArrayMap<>();
        ArrayList<NotificationEntry> arrayList = new ArrayList<>();
        this.mSortedAndFiltered = arrayList;
        this.mReadOnlyNotifications = Collections.unmodifiableList(arrayList);
        this.mRetainedNotifications = new ArrayMap();
        this.mNotifCollectionListeners = new ArrayList();
        this.mNotificationLifetimeExtenders = new ArrayList<>();
        this.mNotificationEntryListeners = new ArrayList();
        this.mRemoveInterceptors = new ArrayList();
        AppLockManager.AppLockCallback appLockCallback = new AppLockManager.AppLockCallback() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManager.1
            public void onAppStateChanged(String str) {
                Iterator it = NotificationEntryManager.this.mAllNotifications.iterator();
                while (it.hasNext()) {
                    NotificationEntryManager.this.updateAppLockNotification(str, (NotificationEntry) it.next());
                }
            }
        };
        this.mAppLockCallback = appLockCallback;
        this.mInflationCallback = new NotificationRowContentBinder.InflationCallback() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManager.2
            @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
            public void handleInflationException(NotificationEntry notificationEntry, Exception exc) {
                NotificationEntryManager.this.handleInflationException(notificationEntry.getSbn(), exc);
            }

            @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
            public void onAsyncInflationFinished(NotificationEntry notificationEntry) {
                NotificationEntryManager.this.mPendingNotifications.remove(notificationEntry.getKey());
                if (notificationEntry.isRowRemoved()) {
                    return;
                }
                boolean z = NotificationEntryManager.this.getActiveNotificationUnfiltered(notificationEntry.getKey()) == null;
                NotificationEntryManager.this.mLogger.logNotifInflated(notificationEntry.getKey(), z);
                if (z) {
                    Iterator it = NotificationEntryManager.this.mNotificationEntryListeners.iterator();
                    while (it.hasNext()) {
                        ((NotificationEntryListener) it.next()).onEntryInflated(notificationEntry);
                    }
                    NotificationEntryManager.this.addActiveNotification(notificationEntry);
                    NotificationEntryManager.this.updateNotifications("onAsyncInflationFinished");
                    Iterator it2 = NotificationEntryManager.this.mNotificationEntryListeners.iterator();
                    while (it2.hasNext()) {
                        ((NotificationEntryListener) it2.next()).onNotificationAdded(notificationEntry);
                    }
                    return;
                }
                Iterator it3 = NotificationEntryManager.this.mNotificationEntryListeners.iterator();
                while (it3.hasNext()) {
                    ((NotificationEntryListener) it3.next()).onEntryReinflated(notificationEntry);
                }
            }
        };
        this.mNotifListener = new NotificationListener.NotificationHandler() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManager.3
            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationsInitialized() {
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
                if (NotificationEntryManager.this.mActiveNotifications.containsKey(statusBarNotification.getKey())) {
                    NotificationEntryManager.this.updateNotification(statusBarNotification, rankingMap);
                } else {
                    NotificationEntryManager.this.addNotification(statusBarNotification, rankingMap);
                }
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
                NotificationEntryManager.this.removeNotification(statusBarNotification.getKey(), rankingMap, i);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
                NotificationEntryManager.this.updateNotificationRanking(rankingMap);
            }
        };
        this.mLogger = notificationEntryManagerLogger;
        this.mGroupManager = notificationGroupManager;
        this.mRankingManager = notificationRankingManager;
        this.mKeyguardEnvironment = keyguardEnvironment;
        this.mFeatureFlags = featureFlags;
        this.mNotificationRowBinderLazy = lazy;
        this.mRemoteInputManagerLazy = lazy2;
        this.mLeakDetector = leakDetector;
        this.mFgsFeatureController = foregroundServiceDismissalFeatureController;
        this.mBubbleControllerLazy = lazy3;
        this.mAppLockManager = appLockManager;
        appLockManager.addAppLockCallback(appLockCallback);
    }

    public void attach(NotificationListener notificationListener) {
        notificationListener.addNotificationHandler(this.mNotifListener);
    }

    public void addNotificationEntryListener(NotificationEntryListener notificationEntryListener) {
        this.mNotificationEntryListeners.add(notificationEntryListener);
    }

    public void removeNotificationEntryListener(NotificationEntryListener notificationEntryListener) {
        this.mNotificationEntryListeners.remove(notificationEntryListener);
    }

    public void addNotificationRemoveInterceptor(NotificationRemoveInterceptor notificationRemoveInterceptor) {
        this.mRemoveInterceptors.add(notificationRemoveInterceptor);
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter) {
        this.mPresenter = notificationPresenter;
    }

    public void addNotificationLifetimeExtenders(List<NotificationLifetimeExtender> list) {
        Iterator<NotificationLifetimeExtender> it = list.iterator();
        while (it.hasNext()) {
            addNotificationLifetimeExtender(it.next());
        }
    }

    public void addNotificationLifetimeExtender(NotificationLifetimeExtender notificationLifetimeExtender) {
        this.mNotificationLifetimeExtenders.add(notificationLifetimeExtender);
        notificationLifetimeExtender.setCallback(new NotificationLifetimeExtender.NotificationSafeToRemoveCallback() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManager$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.NotificationLifetimeExtender.NotificationSafeToRemoveCallback
            public final void onSafeToRemove(String str) {
                this.f$0.lambda$addNotificationLifetimeExtender$0(str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addNotificationLifetimeExtender$0(String str) {
        removeNotification(str, this.mLatestRankingMap, 0);
    }

    @Override // com.android.systemui.statusbar.notification.VisualStabilityManager.Callback
    public void onChangeAllowed() {
        updateNotifications("reordering is now allowed");
    }

    public void performRemoveNotification(StatusBarNotification statusBarNotification, int i) {
        removeNotificationInternal(statusBarNotification.getKey(), null, obtainVisibility(statusBarNotification.getKey()), false, true, i);
    }

    private NotificationVisibility obtainVisibility(String str) {
        NotificationEntry notificationEntry = this.mActiveNotifications.get(str);
        return NotificationVisibility.obtain(str, notificationEntry != null ? notificationEntry.getRanking().getRank() : 0, this.mActiveNotifications.size(), true, NotificationLogger.getNotificationLocation(getActiveNotificationUnfiltered(str)));
    }

    private void abortExistingInflation(String str, String str2) {
        if (this.mPendingNotifications.containsKey(str)) {
            NotificationEntry notificationEntry = this.mPendingNotifications.get(str);
            notificationEntry.abortTask();
            this.mPendingNotifications.remove(str);
            Iterator<NotifCollectionListener> it = this.mNotifCollectionListeners.iterator();
            while (it.hasNext()) {
                it.next().onEntryCleanUp(notificationEntry);
            }
            this.mLogger.logInflationAborted(str, "pending", str2);
        }
        NotificationEntry activeNotificationUnfiltered = getActiveNotificationUnfiltered(str);
        if (activeNotificationUnfiltered != null) {
            activeNotificationUnfiltered.abortTask();
            this.mLogger.logInflationAborted(str, "active", str2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleInflationException(StatusBarNotification statusBarNotification, Exception exc) {
        removeNotificationInternal(statusBarNotification.getKey(), null, null, true, false, 4);
        Iterator<NotificationEntryListener> it = this.mNotificationEntryListeners.iterator();
        while (it.hasNext()) {
            it.next().onInflationError(statusBarNotification, exc);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addActiveNotification(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.mActiveNotifications.put(notificationEntry.getKey(), notificationEntry);
        this.mGroupManager.onEntryAdded(notificationEntry);
        updateRankingAndSort(this.mRankingManager.getRankingMap(), "addEntryInternalInternal");
    }

    @VisibleForTesting
    public void addActiveNotificationForTest(NotificationEntry notificationEntry) {
        this.mActiveNotifications.put(notificationEntry.getKey(), notificationEntry);
        this.mGroupManager.onEntryAdded(notificationEntry);
        reapplyFilterAndSort("addVisibleNotification");
    }

    public void removeNotification(String str, NotificationListenerService.RankingMap rankingMap, int i) {
        removeNotificationInternal(str, rankingMap, obtainVisibility(str), false, false, i);
    }

    private void removeNotificationInternal(String str, NotificationListenerService.RankingMap rankingMap, NotificationVisibility notificationVisibility, boolean z, boolean z2, int i) {
        NotificationEntry activeNotificationUnfiltered = getActiveNotificationUnfiltered(str);
        Iterator<NotificationRemoveInterceptor> it = this.mRemoveInterceptors.iterator();
        while (it.hasNext()) {
            if (it.next().onNotificationRemoveRequested(str, activeNotificationUnfiltered, i)) {
                this.mLogger.logRemovalIntercepted(str);
                return;
            }
        }
        boolean z3 = true;
        boolean z4 = false;
        if (activeNotificationUnfiltered == null) {
            NotificationEntry notificationEntry = this.mPendingNotifications.get(str);
            if (notificationEntry != null) {
                Iterator<NotificationLifetimeExtender> it2 = this.mNotificationLifetimeExtenders.iterator();
                while (it2.hasNext()) {
                    NotificationLifetimeExtender next = it2.next();
                    if (next.shouldExtendLifetimeForPendingNotification(notificationEntry)) {
                        extendLifetime(notificationEntry, next);
                        this.mLogger.logLifetimeExtended(str, next.getClass().getName(), "pending");
                        z4 = true;
                    }
                }
                if (z4) {
                    return;
                }
                abortExistingInflation(str, "removeNotification");
                this.mAllNotifications.remove(notificationEntry);
                this.mLeakDetector.trackGarbage(notificationEntry);
                return;
            }
            return;
        }
        boolean zIsRowDismissed = activeNotificationUnfiltered.isRowDismissed();
        if (z || zIsRowDismissed) {
            z3 = false;
        } else {
            Iterator<NotificationLifetimeExtender> it3 = this.mNotificationLifetimeExtenders.iterator();
            while (it3.hasNext()) {
                NotificationLifetimeExtender next2 = it3.next();
                if (next2.shouldExtendLifetime(activeNotificationUnfiltered)) {
                    this.mLatestRankingMap = rankingMap;
                    extendLifetime(activeNotificationUnfiltered, next2);
                    this.mLogger.logLifetimeExtended(str, next2.getClass().getName(), "active");
                    break;
                }
            }
            z3 = false;
        }
        if (z3) {
            return;
        }
        abortExistingInflation(str, "removeNotification");
        this.mAllNotifications.remove(activeNotificationUnfiltered);
        cancelLifetimeExtension(activeNotificationUnfiltered);
        if (activeNotificationUnfiltered.rowExists()) {
            activeNotificationUnfiltered.removeRow();
        }
        handleGroupSummaryRemoved(str);
        removeVisibleNotification(str);
        updateNotifications("removeNotificationInternal");
        boolean z5 = z2 | zIsRowDismissed;
        this.mLogger.logNotifRemoved(activeNotificationUnfiltered.getKey(), z5);
        Iterator<NotificationEntryListener> it4 = this.mNotificationEntryListeners.iterator();
        while (it4.hasNext()) {
            it4.next().onEntryRemoved(activeNotificationUnfiltered, notificationVisibility, z5, i);
        }
        Iterator<NotifCollectionListener> it5 = this.mNotifCollectionListeners.iterator();
        while (it5.hasNext()) {
            it5.next().onEntryRemoved(activeNotificationUnfiltered, 0);
        }
        Iterator<NotifCollectionListener> it6 = this.mNotifCollectionListeners.iterator();
        while (it6.hasNext()) {
            it6.next().onEntryCleanUp(activeNotificationUnfiltered);
        }
        this.mLeakDetector.trackGarbage(activeNotificationUnfiltered);
    }

    private void handleGroupSummaryRemoved(String str) {
        List<NotificationEntry> attachedNotifChildren;
        NotificationEntry activeNotificationUnfiltered = getActiveNotificationUnfiltered(str);
        if (activeNotificationUnfiltered != null && activeNotificationUnfiltered.rowExists() && activeNotificationUnfiltered.isSummaryWithChildren()) {
            if ((activeNotificationUnfiltered.getSbn().getOverrideGroupKey() == null || activeNotificationUnfiltered.isRowDismissed()) && (attachedNotifChildren = activeNotificationUnfiltered.getAttachedNotifChildren()) != null) {
                for (int i = 0; i < attachedNotifChildren.size(); i++) {
                    NotificationEntry notificationEntry = attachedNotifChildren.get(i);
                    boolean z = (activeNotificationUnfiltered.getSbn().getNotification().flags & 64) != 0;
                    boolean z2 = this.mRemoteInputManagerLazy.get().shouldKeepForRemoteInputHistory(notificationEntry) || this.mRemoteInputManagerLazy.get().shouldKeepForSmartReplyHistory(notificationEntry);
                    if (!z && !z2) {
                        notificationEntry.setKeepInParent(true);
                        notificationEntry.removeRow();
                    }
                }
            }
        }
    }

    private void addNotificationInternal(final StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) throws InflationException {
        String key = statusBarNotification.getKey();
        if (DEBUG) {
            Log.d("NotificationEntryMgr", "addNotification key=" + key);
        }
        updateRankingAndSort(rankingMap, "addNotificationInternal");
        NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
        rankingMap.getRanking(key, ranking);
        NotificationEntry notificationEntry = this.mPendingNotifications.get(key);
        if (notificationEntry != null) {
            notificationEntry.setSbn(statusBarNotification);
            notificationEntry.setRanking(ranking);
        } else {
            notificationEntry = new NotificationEntry(statusBarNotification, ranking, this.mFgsFeatureController.isForegroundServiceDismissalEnabled(), SystemClock.uptimeMillis());
            this.mAllNotifications.add(notificationEntry);
            this.mLeakDetector.trackInstance(notificationEntry);
            Iterator<NotifCollectionListener> it = this.mNotifCollectionListeners.iterator();
            while (it.hasNext()) {
                it.next().onEntryInit(notificationEntry);
            }
        }
        updateAppLockNotification(statusBarNotification.getPackageName(), notificationEntry);
        Iterator<NotifCollectionListener> it2 = this.mNotifCollectionListeners.iterator();
        while (it2.hasNext()) {
            it2.next().onEntryBind(notificationEntry, statusBarNotification);
        }
        if (!this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mNotificationRowBinderLazy.get().inflateViews(notificationEntry, new Runnable() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManager$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$addNotificationInternal$1(statusBarNotification);
                }
            }, this.mInflationCallback);
        }
        this.mPendingNotifications.put(key, notificationEntry);
        this.mLogger.logNotifAdded(notificationEntry.getKey());
        Iterator<NotificationEntryListener> it3 = this.mNotificationEntryListeners.iterator();
        while (it3.hasNext()) {
            it3.next().onPendingEntryAdded(notificationEntry);
        }
        Iterator<NotifCollectionListener> it4 = this.mNotifCollectionListeners.iterator();
        while (it4.hasNext()) {
            it4.next().onEntryAdded(notificationEntry);
        }
        Iterator<NotifCollectionListener> it5 = this.mNotifCollectionListeners.iterator();
        while (it5.hasNext()) {
            it5.next().onRankingApplied();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addNotificationInternal$1(StatusBarNotification statusBarNotification) {
        performRemoveNotification(statusBarNotification, 2);
    }

    public void addNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        try {
            addNotificationInternal(statusBarNotification, rankingMap);
        } catch (InflationException e) {
            handleInflationException(statusBarNotification, e);
        }
    }

    private void updateNotificationInternal(final StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) throws InflationException {
        if (DEBUG) {
            Log.d("NotificationEntryMgr", "updateNotification(" + statusBarNotification + ")");
        }
        String key = statusBarNotification.getKey();
        abortExistingInflation(key, "updateNotification");
        NotificationEntry activeNotificationUnfiltered = getActiveNotificationUnfiltered(key);
        if (activeNotificationUnfiltered == null) {
            return;
        }
        cancelLifetimeExtension(activeNotificationUnfiltered);
        updateRankingAndSort(rankingMap, "updateNotificationInternal");
        StatusBarNotification sbn = activeNotificationUnfiltered.getSbn();
        activeNotificationUnfiltered.setSbn(statusBarNotification);
        Iterator<NotifCollectionListener> it = this.mNotifCollectionListeners.iterator();
        while (it.hasNext()) {
            it.next().onEntryBind(activeNotificationUnfiltered, statusBarNotification);
        }
        this.mGroupManager.onEntryUpdated(activeNotificationUnfiltered, sbn);
        this.mLogger.logNotifUpdated(activeNotificationUnfiltered.getKey());
        Iterator<NotificationEntryListener> it2 = this.mNotificationEntryListeners.iterator();
        while (it2.hasNext()) {
            it2.next().onPreEntryUpdated(activeNotificationUnfiltered);
        }
        Iterator<NotifCollectionListener> it3 = this.mNotifCollectionListeners.iterator();
        while (it3.hasNext()) {
            it3.next().onEntryUpdated(activeNotificationUnfiltered);
        }
        if (!this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mNotificationRowBinderLazy.get().inflateViews(activeNotificationUnfiltered, new Runnable() { // from class: com.android.systemui.statusbar.notification.NotificationEntryManager$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$updateNotificationInternal$2(statusBarNotification);
                }
            }, this.mInflationCallback);
        }
        updateNotifications("updateNotificationInternal");
        if (DEBUG) {
            boolean zIsNotificationForCurrentProfiles = this.mKeyguardEnvironment.isNotificationForCurrentProfiles(statusBarNotification);
            StringBuilder sb = new StringBuilder();
            sb.append("notification is ");
            sb.append(zIsNotificationForCurrentProfiles ? "" : "not ");
            sb.append("for you");
            Log.d("NotificationEntryMgr", sb.toString());
        }
        Iterator<NotificationEntryListener> it4 = this.mNotificationEntryListeners.iterator();
        while (it4.hasNext()) {
            it4.next().onPostEntryUpdated(activeNotificationUnfiltered);
        }
        Iterator<NotifCollectionListener> it5 = this.mNotifCollectionListeners.iterator();
        while (it5.hasNext()) {
            it5.next().onRankingApplied();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateNotificationInternal$2(StatusBarNotification statusBarNotification) {
        performRemoveNotification(statusBarNotification, 2);
    }

    public void updateNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        try {
            updateNotificationInternal(statusBarNotification, rankingMap);
        } catch (InflationException e) {
            handleInflationException(statusBarNotification, e);
        }
    }

    public void updateNotifications(String str) {
        reapplyFilterAndSort(str);
        if (this.mPresenter == null || this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return;
        }
        this.mPresenter.updateNotificationViews(str);
    }

    public void updateNotificationRanking(NotificationListenerService.RankingMap rankingMap) {
        ArrayList<NotificationEntry> arrayList = new ArrayList();
        arrayList.addAll(getVisibleNotifications());
        arrayList.addAll(this.mPendingNotifications.values());
        ArrayMap arrayMap = new ArrayMap();
        ArrayMap arrayMap2 = new ArrayMap();
        for (NotificationEntry notificationEntry : arrayList) {
            arrayMap.put(notificationEntry.getKey(), NotificationUiAdjustment.extractFromNotificationEntry(notificationEntry));
            arrayMap2.put(notificationEntry.getKey(), Integer.valueOf(notificationEntry.getImportance()));
        }
        updateRankingAndSort(rankingMap, "updateNotificationRanking");
        updateRankingOfPendingNotifications(rankingMap);
        for (NotificationEntry notificationEntry2 : arrayList) {
            this.mNotificationRowBinderLazy.get().onNotificationRankingUpdated(notificationEntry2, (Integer) arrayMap2.get(notificationEntry2.getKey()), (NotificationUiAdjustment) arrayMap.get(notificationEntry2.getKey()), NotificationUiAdjustment.extractFromNotificationEntry(notificationEntry2), this.mInflationCallback);
        }
        updateNotifications("updateNotificationRanking");
        Iterator<NotificationEntryListener> it = this.mNotificationEntryListeners.iterator();
        while (it.hasNext()) {
            it.next().onNotificationRankingUpdated(rankingMap);
        }
        Iterator<NotifCollectionListener> it2 = this.mNotifCollectionListeners.iterator();
        while (it2.hasNext()) {
            it2.next().onRankingUpdate(rankingMap);
        }
        Iterator<NotifCollectionListener> it3 = this.mNotifCollectionListeners.iterator();
        while (it3.hasNext()) {
            it3.next().onRankingApplied();
        }
    }

    private void updateRankingOfPendingNotifications(NotificationListenerService.RankingMap rankingMap) {
        if (rankingMap == null) {
            return;
        }
        for (NotificationEntry notificationEntry : this.mPendingNotifications.values()) {
            NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
            if (rankingMap.getRanking(notificationEntry.getKey(), ranking)) {
                notificationEntry.setRanking(ranking);
            }
        }
    }

    public Iterable<NotificationEntry> getPendingNotificationsIterator() {
        return this.mPendingNotifications.values();
    }

    public NotificationEntry getActiveNotificationUnfiltered(String str) {
        return this.mActiveNotifications.get(str);
    }

    public boolean hasActiveVisibleNotifications() {
        Iterator<NotificationEntry> it = this.mSortedAndFiltered.iterator();
        while (it.hasNext()) {
            if (it.next().getContentView() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveOngoingNotifications() {
        Iterator<NotificationEntry> it = this.mSortedAndFiltered.iterator();
        while (it.hasNext()) {
            NotificationEntry next = it.next();
            if (next.getContentView() != null && next.getSbn().isOngoing()) {
                return true;
            }
        }
        return false;
    }

    public NotificationEntry getPendingOrActiveNotif(String str) {
        if (this.mPendingNotifications.containsKey(str)) {
            return this.mPendingNotifications.get(str);
        }
        return this.mActiveNotifications.get(str);
    }

    private void extendLifetime(NotificationEntry notificationEntry, NotificationLifetimeExtender notificationLifetimeExtender) {
        NotificationLifetimeExtender notificationLifetimeExtender2 = this.mRetainedNotifications.get(notificationEntry);
        if (notificationLifetimeExtender2 != null && notificationLifetimeExtender2 != notificationLifetimeExtender) {
            notificationLifetimeExtender2.setShouldManageLifetime(notificationEntry, false);
        }
        this.mRetainedNotifications.put(notificationEntry, notificationLifetimeExtender);
        notificationLifetimeExtender.setShouldManageLifetime(notificationEntry, true);
    }

    private void cancelLifetimeExtension(NotificationEntry notificationEntry) {
        NotificationLifetimeExtender notificationLifetimeExtenderRemove = this.mRetainedNotifications.remove(notificationEntry);
        if (notificationLifetimeExtenderRemove != null) {
            notificationLifetimeExtenderRemove.setShouldManageLifetime(notificationEntry, false);
        }
    }

    private void removeVisibleNotification(String str) {
        Assert.isMainThread();
        NotificationEntry notificationEntryRemove = this.mActiveNotifications.remove(str);
        if (notificationEntryRemove == null) {
            return;
        }
        this.mGroupManager.onEntryRemoved(notificationEntryRemove);
    }

    public List<NotificationEntry> getActiveNotificationsForCurrentUser() {
        Assert.isMainThread();
        ArrayList arrayList = new ArrayList();
        int size = this.mActiveNotifications.size();
        for (int i = 0; i < size; i++) {
            NotificationEntry notificationEntryValueAt = this.mActiveNotifications.valueAt(i);
            if (this.mKeyguardEnvironment.isNotificationForCurrentProfiles(notificationEntryValueAt.getSbn())) {
                arrayList.add(notificationEntryValueAt);
            }
        }
        return arrayList;
    }

    public void reapplyFilterAndSort(String str) {
        updateRankingAndSort(this.mRankingManager.getRankingMap(), str);
    }

    private void updateRankingAndSort(NotificationListenerService.RankingMap rankingMap, String str) {
        this.mSortedAndFiltered.clear();
        this.mSortedAndFiltered.addAll(this.mRankingManager.updateRanking(rankingMap, this.mActiveNotifications.values(), str));
    }

    public void dump(PrintWriter printWriter, String str) {
        printWriter.println("NotificationEntryManager");
        int size = this.mSortedAndFiltered.size();
        printWriter.print(str);
        printWriter.println("active notifications: " + size);
        int i = 0;
        while (i < size) {
            dumpEntry(printWriter, str, i, this.mSortedAndFiltered.get(i));
            i++;
        }
        synchronized (this.mActiveNotifications) {
            int size2 = this.mActiveNotifications.size();
            printWriter.print(str);
            printWriter.println("inactive notifications: " + (size2 - i));
            int i2 = 0;
            for (int i3 = 0; i3 < size2; i3++) {
                NotificationEntry notificationEntryValueAt = this.mActiveNotifications.valueAt(i3);
                if (!this.mSortedAndFiltered.contains(notificationEntryValueAt)) {
                    dumpEntry(printWriter, str, i2, notificationEntryValueAt);
                    i2++;
                }
            }
        }
    }

    private void dumpEntry(PrintWriter printWriter, String str, int i, NotificationEntry notificationEntry) {
        printWriter.print(str);
        printWriter.println("  [" + i + "] key=" + notificationEntry.getKey() + " icon=" + notificationEntry.getIcons().getStatusBarIcon());
        StatusBarNotification sbn = notificationEntry.getSbn();
        printWriter.print(str);
        printWriter.println("      pkg=" + sbn.getPackageName() + " id=" + sbn.getId() + " importance=" + notificationEntry.getRanking().getImportance());
        printWriter.print(str);
        StringBuilder sb = new StringBuilder();
        sb.append("      notification=");
        sb.append(sbn.getNotification());
        printWriter.println(sb.toString());
    }

    public List<NotificationEntry> getVisibleNotifications() {
        return this.mReadOnlyNotifications;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection
    public Collection<NotificationEntry> getAllNotifs() {
        return this.mReadOnlyAllNotifications;
    }

    public int getActiveNotificationsCount() {
        return this.mReadOnlyNotifications.size();
    }

    public boolean hasVisibleNotifications() {
        if (this.mReadOnlyNotifications.size() == 0) {
            return false;
        }
        Iterator<NotificationEntry> it = this.mSortedAndFiltered.iterator();
        while (it.hasNext()) {
            if (!this.mBubbleControllerLazy.get().isBubbleNotificationSuppressedFromShade(it.next())) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection
    public void addCollectionListener(NotifCollectionListener notifCollectionListener) {
        this.mNotifCollectionListeners.add(notifCollectionListener);
    }
}
