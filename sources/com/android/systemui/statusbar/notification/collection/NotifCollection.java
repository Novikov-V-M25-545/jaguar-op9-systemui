package com.android.systemui.statusbar.notification.collection;

import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Pair;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.dump.LogBufferEulogizer;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.coalescer.CoalescedEvent;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer;
import com.android.systemui.statusbar.notification.collection.notifcollection.BindEntryEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.CleanUpEntryEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.CollectionReadyForBuildListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.EntryAddedEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.EntryRemovedEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.EntryUpdatedEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.InitEntryEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.statusbar.notification.collection.notifcollection.RankingAppliedEvent;
import com.android.systemui.statusbar.notification.collection.notifcollection.RankingUpdatedEvent;
import com.android.systemui.util.Assert;
import com.android.systemui.util.time.SystemClock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class NotifCollection implements Dumpable {
    private static final long INITIALIZATION_FORGIVENESS_WINDOW = TimeUnit.SECONDS.toMillis(5);
    private boolean mAmDispatchingToOtherCode;
    private boolean mAttached;
    private CollectionReadyForBuildListener mBuildListener;
    private final SystemClock mClock;
    private final List<NotifDismissInterceptor> mDismissInterceptors;
    private final LogBufferEulogizer mEulogizer;
    private Queue<NotifEvent> mEventQueue;
    private final FeatureFlags mFeatureFlags;
    private long mInitializedTimestamp;
    private final List<NotifLifetimeExtender> mLifetimeExtenders;
    private final NotifCollectionLogger mLogger;
    private final List<NotifCollectionListener> mNotifCollectionListeners;
    private final GroupCoalescer.BatchableNotificationHandler mNotifHandler;
    private final Map<String, NotificationEntry> mNotificationSet;
    private final Collection<NotificationEntry> mReadOnlyNotificationSet;
    private final IStatusBarService mStatusBarService;

    public NotifCollection(IStatusBarService iStatusBarService, SystemClock systemClock, FeatureFlags featureFlags, NotifCollectionLogger notifCollectionLogger, LogBufferEulogizer logBufferEulogizer, DumpManager dumpManager) {
        ArrayMap arrayMap = new ArrayMap();
        this.mNotificationSet = arrayMap;
        this.mReadOnlyNotificationSet = Collections.unmodifiableCollection(arrayMap.values());
        this.mNotifCollectionListeners = new ArrayList();
        this.mLifetimeExtenders = new ArrayList();
        this.mDismissInterceptors = new ArrayList();
        this.mEventQueue = new ArrayDeque();
        this.mAttached = false;
        this.mInitializedTimestamp = 0L;
        this.mNotifHandler = new GroupCoalescer.BatchableNotificationHandler() { // from class: com.android.systemui.statusbar.notification.collection.NotifCollection.1
            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
                NotifCollection.this.onNotificationPosted(statusBarNotification, rankingMap);
            }

            @Override // com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer.BatchableNotificationHandler
            public void onNotificationBatchPosted(List<CoalescedEvent> list) {
                NotifCollection.this.onNotificationGroupPosted(list);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
                NotifCollection.this.onNotificationRemoved(statusBarNotification, rankingMap, i);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
                NotifCollection.this.onNotificationRankingUpdate(rankingMap);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationsInitialized() {
                NotifCollection.this.onNotificationsInitialized();
            }
        };
        Assert.isMainThread();
        this.mStatusBarService = iStatusBarService;
        this.mClock = systemClock;
        this.mFeatureFlags = featureFlags;
        this.mLogger = notifCollectionLogger;
        this.mEulogizer = logBufferEulogizer;
        dumpManager.registerDumpable("NotifCollection", this);
    }

    public void attach(GroupCoalescer groupCoalescer) {
        Assert.isMainThread();
        if (this.mAttached) {
            throw new RuntimeException("attach() called twice");
        }
        this.mAttached = true;
        groupCoalescer.setNotificationHandler(this.mNotifHandler);
    }

    void setBuildListener(CollectionReadyForBuildListener collectionReadyForBuildListener) {
        Assert.isMainThread();
        this.mBuildListener = collectionReadyForBuildListener;
    }

    Collection<NotificationEntry> getAllNotifs() {
        Assert.isMainThread();
        return this.mReadOnlyNotificationSet;
    }

    void addCollectionListener(NotifCollectionListener notifCollectionListener) {
        Assert.isMainThread();
        this.mNotifCollectionListeners.add(notifCollectionListener);
    }

    void addNotificationLifetimeExtender(NotifLifetimeExtender notifLifetimeExtender) {
        Assert.isMainThread();
        checkForReentrantCall();
        if (this.mLifetimeExtenders.contains(notifLifetimeExtender)) {
            throw new IllegalArgumentException("Extender " + notifLifetimeExtender + " already added.");
        }
        this.mLifetimeExtenders.add(notifLifetimeExtender);
        notifLifetimeExtender.setCallback(new NotifLifetimeExtender.OnEndLifetimeExtensionCallback() { // from class: com.android.systemui.statusbar.notification.collection.NotifCollection$$ExternalSyntheticLambda1
            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender.OnEndLifetimeExtensionCallback
            public final void onEndLifetimeExtension(NotifLifetimeExtender notifLifetimeExtender2, NotificationEntry notificationEntry) {
                this.f$0.onEndLifetimeExtension(notifLifetimeExtender2, notificationEntry);
            }
        });
    }

    void addNotificationDismissInterceptor(NotifDismissInterceptor notifDismissInterceptor) {
        Assert.isMainThread();
        checkForReentrantCall();
        if (this.mDismissInterceptors.contains(notifDismissInterceptor)) {
            throw new IllegalArgumentException("Interceptor " + notifDismissInterceptor + " already added.");
        }
        this.mDismissInterceptors.add(notifDismissInterceptor);
        notifDismissInterceptor.setCallback(new NotifDismissInterceptor.OnEndDismissInterception() { // from class: com.android.systemui.statusbar.notification.collection.NotifCollection$$ExternalSyntheticLambda0
            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor.OnEndDismissInterception
            public final void onEndDismissInterception(NotifDismissInterceptor notifDismissInterceptor2, NotificationEntry notificationEntry, DismissedByUserStats dismissedByUserStats) {
                this.f$0.onEndDismissInterception(notifDismissInterceptor2, notificationEntry, dismissedByUserStats);
            }
        });
    }

    public void dismissNotifications(List<Pair<NotificationEntry, DismissedByUserStats>> list) {
        Assert.isMainThread();
        checkForReentrantCall();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            NotificationEntry notificationEntry = (NotificationEntry) list.get(i).first;
            DismissedByUserStats dismissedByUserStats = (DismissedByUserStats) list.get(i).second;
            Objects.requireNonNull(dismissedByUserStats);
            if (notificationEntry != this.mNotificationSet.get(notificationEntry.getKey())) {
                throw ((IllegalStateException) this.mEulogizer.record(new IllegalStateException("Invalid entry: " + notificationEntry.getKey())));
            }
            if (notificationEntry.getDismissState() != NotificationEntry.DismissState.DISMISSED) {
                updateDismissInterceptors(notificationEntry);
                if (isDismissIntercepted(notificationEntry)) {
                    this.mLogger.logNotifDismissedIntercepted(notificationEntry.getKey());
                } else {
                    arrayList.add(notificationEntry);
                    if (!isCanceled(notificationEntry)) {
                        try {
                            this.mStatusBarService.onNotificationClear(notificationEntry.getSbn().getPackageName(), notificationEntry.getSbn().getTag(), notificationEntry.getSbn().getId(), notificationEntry.getSbn().getUser().getIdentifier(), notificationEntry.getSbn().getKey(), dismissedByUserStats.dismissalSurface, dismissedByUserStats.dismissalSentiment, dismissedByUserStats.notificationVisibility);
                        } catch (RemoteException e) {
                            this.mLogger.logRemoteExceptionOnNotificationClear(notificationEntry.getKey(), e);
                        }
                    }
                }
            }
        }
        locallyDismissNotifications(arrayList);
        dispatchEventsAndRebuildList();
    }

    public void dismissNotification(NotificationEntry notificationEntry, DismissedByUserStats dismissedByUserStats) {
        dismissNotifications(List.of(new Pair(notificationEntry, dismissedByUserStats)));
    }

    public void dismissAllNotifications(int i) {
        Assert.isMainThread();
        checkForReentrantCall();
        this.mLogger.logDismissAll(i);
        try {
            this.mStatusBarService.onClearAllNotifications(i);
        } catch (RemoteException e) {
            this.mLogger.logRemoteExceptionOnClearAllNotifications(e);
        }
        ArrayList arrayList = new ArrayList(getAllNotifs());
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            NotificationEntry notificationEntry = arrayList.get(size);
            if (!shouldDismissOnClearAll(notificationEntry, i)) {
                updateDismissInterceptors(notificationEntry);
                if (isDismissIntercepted(notificationEntry)) {
                    this.mLogger.logNotifClearAllDismissalIntercepted(notificationEntry.getKey());
                }
                arrayList.remove(size);
            }
        }
        locallyDismissNotifications(arrayList);
        dispatchEventsAndRebuildList();
    }

    private void locallyDismissNotifications(List<NotificationEntry> list) {
        ArrayList<NotificationEntry> arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            NotificationEntry notificationEntry = list.get(i);
            notificationEntry.setDismissState(NotificationEntry.DismissState.DISMISSED);
            this.mLogger.logNotifDismissed(notificationEntry.getKey());
            if (isCanceled(notificationEntry)) {
                arrayList.add(notificationEntry);
            } else if (notificationEntry.getSbn().getNotification().isGroupSummary()) {
                for (NotificationEntry notificationEntry2 : this.mNotificationSet.values()) {
                    if (shouldAutoDismissChildren(notificationEntry2, notificationEntry.getSbn().getGroupKey())) {
                        notificationEntry2.setDismissState(NotificationEntry.DismissState.PARENT_DISMISSED);
                        this.mLogger.logChildDismissed(notificationEntry2);
                        if (isCanceled(notificationEntry2)) {
                            arrayList.add(notificationEntry2);
                        }
                    }
                }
            }
        }
        for (NotificationEntry notificationEntry3 : arrayList) {
            this.mLogger.logDismissOnAlreadyCanceledEntry(notificationEntry3);
            tryRemoveNotification(notificationEntry3);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        Assert.isMainThread();
        postNotification(statusBarNotification, requireRanking(rankingMap, statusBarNotification.getKey()));
        applyRanking(rankingMap);
        dispatchEventsAndRebuildList();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotificationGroupPosted(List<CoalescedEvent> list) {
        Assert.isMainThread();
        this.mLogger.logNotifGroupPosted(list.get(0).getSbn().getGroupKey(), list.size());
        for (CoalescedEvent coalescedEvent : list) {
            postNotification(coalescedEvent.getSbn(), coalescedEvent.getRanking());
        }
        dispatchEventsAndRebuildList();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
        Assert.isMainThread();
        this.mLogger.logNotifRemoved(statusBarNotification.getKey(), i);
        NotificationEntry notificationEntry = this.mNotificationSet.get(statusBarNotification.getKey());
        if (notificationEntry == null) {
            this.mLogger.logNoNotificationToRemoveWithKey(statusBarNotification.getKey());
            return;
        }
        notificationEntry.mCancellationReason = i;
        tryRemoveNotification(notificationEntry);
        applyRanking(rankingMap);
        dispatchEventsAndRebuildList();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
        Assert.isMainThread();
        this.mEventQueue.add(new RankingUpdatedEvent(rankingMap));
        applyRanking(rankingMap);
        dispatchEventsAndRebuildList();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNotificationsInitialized() {
        this.mInitializedTimestamp = this.mClock.uptimeMillis();
    }

    private void postNotification(StatusBarNotification statusBarNotification, NotificationListenerService.Ranking ranking) {
        NotificationEntry notificationEntry = this.mNotificationSet.get(statusBarNotification.getKey());
        if (notificationEntry == null) {
            NotificationEntry notificationEntry2 = new NotificationEntry(statusBarNotification, ranking, this.mClock.uptimeMillis());
            this.mEventQueue.add(new InitEntryEvent(notificationEntry2));
            this.mEventQueue.add(new BindEntryEvent(notificationEntry2, statusBarNotification));
            this.mNotificationSet.put(statusBarNotification.getKey(), notificationEntry2);
            this.mLogger.logNotifPosted(statusBarNotification.getKey());
            this.mEventQueue.add(new EntryAddedEvent(notificationEntry2));
            return;
        }
        cancelLocalDismissal(notificationEntry);
        cancelLifetimeExtension(notificationEntry);
        cancelDismissInterception(notificationEntry);
        notificationEntry.mCancellationReason = -1;
        notificationEntry.setSbn(statusBarNotification);
        this.mEventQueue.add(new BindEntryEvent(notificationEntry, statusBarNotification));
        this.mLogger.logNotifUpdated(statusBarNotification.getKey());
        this.mEventQueue.add(new EntryUpdatedEvent(notificationEntry));
    }

    private boolean tryRemoveNotification(NotificationEntry notificationEntry) {
        if (this.mNotificationSet.get(notificationEntry.getKey()) != notificationEntry) {
            throw ((IllegalStateException) this.mEulogizer.record(new IllegalStateException("No notification to remove with key " + notificationEntry.getKey())));
        }
        if (!isCanceled(notificationEntry)) {
            throw ((IllegalStateException) this.mEulogizer.record(new IllegalStateException("Cannot remove notification " + notificationEntry.getKey() + ": has not been marked for removal")));
        }
        if (isDismissedByUser(notificationEntry)) {
            cancelLifetimeExtension(notificationEntry);
        } else {
            updateLifetimeExtension(notificationEntry);
        }
        if (isLifetimeExtended(notificationEntry)) {
            return false;
        }
        this.mLogger.logNotifReleased(notificationEntry.getKey());
        this.mNotificationSet.remove(notificationEntry.getKey());
        cancelDismissInterception(notificationEntry);
        this.mEventQueue.add(new EntryRemovedEvent(notificationEntry, notificationEntry.mCancellationReason));
        this.mEventQueue.add(new CleanUpEntryEvent(notificationEntry));
        return true;
    }

    private void applyRanking(NotificationListenerService.RankingMap rankingMap) {
        for (NotificationEntry notificationEntry : this.mNotificationSet.values()) {
            if (!isCanceled(notificationEntry)) {
                NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                if (rankingMap.getRanking(notificationEntry.getKey(), ranking)) {
                    notificationEntry.setRanking(ranking);
                    if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
                        String overrideGroupKey = ranking.getOverrideGroupKey();
                        if (!Objects.equals(notificationEntry.getSbn().getOverrideGroupKey(), overrideGroupKey)) {
                            notificationEntry.getSbn().setOverrideGroupKey(overrideGroupKey);
                        }
                    }
                } else {
                    this.mLogger.logRankingMissing(notificationEntry.getKey(), rankingMap);
                }
            }
        }
        this.mEventQueue.add(new RankingAppliedEvent());
    }

    private void dispatchEventsAndRebuildList() {
        this.mAmDispatchingToOtherCode = true;
        while (!this.mEventQueue.isEmpty()) {
            this.mEventQueue.remove().dispatchTo(this.mNotifCollectionListeners);
        }
        this.mAmDispatchingToOtherCode = false;
        CollectionReadyForBuildListener collectionReadyForBuildListener = this.mBuildListener;
        if (collectionReadyForBuildListener != null) {
            collectionReadyForBuildListener.onBuildList(this.mReadOnlyNotificationSet);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEndLifetimeExtension(NotifLifetimeExtender notifLifetimeExtender, NotificationEntry notificationEntry) {
        Assert.isMainThread();
        if (this.mAttached) {
            checkForReentrantCall();
            if (!notificationEntry.mLifetimeExtenders.remove(notifLifetimeExtender)) {
                throw ((IllegalStateException) this.mEulogizer.record(new IllegalStateException(String.format("Cannot end lifetime extension for extender \"%s\" (%s)", notifLifetimeExtender.getName(), notifLifetimeExtender))));
            }
            this.mLogger.logLifetimeExtensionEnded(notificationEntry.getKey(), notifLifetimeExtender, notificationEntry.mLifetimeExtenders.size());
            if (isLifetimeExtended(notificationEntry) || !tryRemoveNotification(notificationEntry)) {
                return;
            }
            dispatchEventsAndRebuildList();
        }
    }

    private void cancelLifetimeExtension(NotificationEntry notificationEntry) {
        this.mAmDispatchingToOtherCode = true;
        Iterator<NotifLifetimeExtender> it = notificationEntry.mLifetimeExtenders.iterator();
        while (it.hasNext()) {
            it.next().cancelLifetimeExtension(notificationEntry);
        }
        this.mAmDispatchingToOtherCode = false;
        notificationEntry.mLifetimeExtenders.clear();
    }

    private boolean isLifetimeExtended(NotificationEntry notificationEntry) {
        return notificationEntry.mLifetimeExtenders.size() > 0;
    }

    private void updateLifetimeExtension(NotificationEntry notificationEntry) {
        notificationEntry.mLifetimeExtenders.clear();
        this.mAmDispatchingToOtherCode = true;
        for (NotifLifetimeExtender notifLifetimeExtender : this.mLifetimeExtenders) {
            if (notifLifetimeExtender.shouldExtendLifetime(notificationEntry, notificationEntry.mCancellationReason)) {
                this.mLogger.logLifetimeExtended(notificationEntry.getKey(), notifLifetimeExtender);
                notificationEntry.mLifetimeExtenders.add(notifLifetimeExtender);
            }
        }
        this.mAmDispatchingToOtherCode = false;
    }

    private void updateDismissInterceptors(NotificationEntry notificationEntry) {
        notificationEntry.mDismissInterceptors.clear();
        this.mAmDispatchingToOtherCode = true;
        for (NotifDismissInterceptor notifDismissInterceptor : this.mDismissInterceptors) {
            if (notifDismissInterceptor.shouldInterceptDismissal(notificationEntry)) {
                notificationEntry.mDismissInterceptors.add(notifDismissInterceptor);
            }
        }
        this.mAmDispatchingToOtherCode = false;
    }

    private void cancelLocalDismissal(NotificationEntry notificationEntry) {
        if (isDismissedByUser(notificationEntry)) {
            notificationEntry.setDismissState(NotificationEntry.DismissState.NOT_DISMISSED);
            if (notificationEntry.getSbn().getNotification().isGroupSummary()) {
                for (NotificationEntry notificationEntry2 : this.mNotificationSet.values()) {
                    if (notificationEntry2.getSbn().getGroupKey().equals(notificationEntry.getSbn().getGroupKey()) && notificationEntry2.getDismissState() == NotificationEntry.DismissState.PARENT_DISMISSED) {
                        notificationEntry2.setDismissState(NotificationEntry.DismissState.NOT_DISMISSED);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEndDismissInterception(NotifDismissInterceptor notifDismissInterceptor, NotificationEntry notificationEntry, DismissedByUserStats dismissedByUserStats) {
        Assert.isMainThread();
        if (this.mAttached) {
            checkForReentrantCall();
            if (!notificationEntry.mDismissInterceptors.remove(notifDismissInterceptor)) {
                throw ((IllegalStateException) this.mEulogizer.record(new IllegalStateException(String.format("Cannot end dismiss interceptor for interceptor \"%s\" (%s)", notifDismissInterceptor.getName(), notifDismissInterceptor))));
            }
            if (isDismissIntercepted(notificationEntry)) {
                return;
            }
            dismissNotification(notificationEntry, dismissedByUserStats);
        }
    }

    private void cancelDismissInterception(NotificationEntry notificationEntry) {
        this.mAmDispatchingToOtherCode = true;
        Iterator<NotifDismissInterceptor> it = notificationEntry.mDismissInterceptors.iterator();
        while (it.hasNext()) {
            it.next().cancelDismissInterception(notificationEntry);
        }
        this.mAmDispatchingToOtherCode = false;
        notificationEntry.mDismissInterceptors.clear();
    }

    private boolean isDismissIntercepted(NotificationEntry notificationEntry) {
        return notificationEntry.mDismissInterceptors.size() > 0;
    }

    private void checkForReentrantCall() {
        if (this.mAmDispatchingToOtherCode) {
            throw ((IllegalStateException) this.mEulogizer.record(new IllegalStateException("Reentrant call detected")));
        }
    }

    private static NotificationListenerService.Ranking requireRanking(NotificationListenerService.RankingMap rankingMap, String str) {
        NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
        if (rankingMap.getRanking(str, ranking)) {
            return ranking;
        }
        throw new IllegalArgumentException("Ranking map doesn't contain key: " + str);
    }

    private static boolean isCanceled(NotificationEntry notificationEntry) {
        return notificationEntry.mCancellationReason != -1;
    }

    private static boolean isDismissedByUser(NotificationEntry notificationEntry) {
        return notificationEntry.getDismissState() != NotificationEntry.DismissState.NOT_DISMISSED;
    }

    private static boolean shouldAutoDismissChildren(NotificationEntry notificationEntry, String str) {
        return (!notificationEntry.getSbn().getGroupKey().equals(str) || notificationEntry.getSbn().getNotification().isGroupSummary() || hasFlag(notificationEntry, 64) || hasFlag(notificationEntry, LineageHardwareManager.FEATURE_AUTO_CONTRAST) || notificationEntry.getDismissState() == NotificationEntry.DismissState.DISMISSED) ? false : true;
    }

    private static boolean shouldDismissOnClearAll(NotificationEntry notificationEntry, int i) {
        return userIdMatches(notificationEntry, i) && notificationEntry.isClearable() && !hasFlag(notificationEntry, LineageHardwareManager.FEATURE_AUTO_CONTRAST) && notificationEntry.getDismissState() != NotificationEntry.DismissState.DISMISSED;
    }

    private static boolean hasFlag(NotificationEntry notificationEntry, int i) {
        return (notificationEntry.getSbn().getNotification().flags & i) != 0;
    }

    private static boolean userIdMatches(NotificationEntry notificationEntry, int i) {
        return i == -1 || notificationEntry.getSbn().getUser().getIdentifier() == -1 || notificationEntry.getSbn().getUser().getIdentifier() == i;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        ArrayList arrayList = new ArrayList(getAllNotifs());
        printWriter.println("\tNotifCollection unsorted/unfiltered notifications:");
        if (arrayList.size() == 0) {
            printWriter.println("\t\t None");
        }
        printWriter.println(ListDumper.dumpList(arrayList, true, "\t\t"));
    }
}
