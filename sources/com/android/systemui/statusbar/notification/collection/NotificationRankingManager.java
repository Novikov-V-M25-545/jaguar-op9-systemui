package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationRankingManager.kt */
/* loaded from: classes.dex */
public class NotificationRankingManager {
    static final /* synthetic */ KProperty[] $$delegatedProperties = {Reflection.property1(new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(NotificationRankingManager.class), "mediaManager", "getMediaManager()Lcom/android/systemui/statusbar/NotificationMediaManager;"))};
    private final NotificationGroupManager groupManager;
    private final HeadsUpManager headsUpManager;
    private final HighPriorityProvider highPriorityProvider;
    private final NotificationEntryManagerLogger logger;
    private final Lazy mediaManager$delegate;
    private final dagger.Lazy<NotificationMediaManager> mediaManagerLazy;
    private final NotificationFilter notifFilter;
    private final PeopleNotificationIdentifier peopleNotificationIdentifier;
    private final Comparator<NotificationEntry> rankingComparator;

    @Nullable
    private NotificationListenerService.RankingMap rankingMap;
    private final NotificationSectionsFeatureManager sectionsFeatureManager;

    private final NotificationMediaManager getMediaManager() {
        Lazy lazy = this.mediaManager$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (NotificationMediaManager) lazy.getValue();
    }

    public NotificationRankingManager(@NotNull dagger.Lazy<NotificationMediaManager> mediaManagerLazy, @NotNull NotificationGroupManager groupManager, @NotNull HeadsUpManager headsUpManager, @NotNull NotificationFilter notifFilter, @NotNull NotificationEntryManagerLogger logger, @NotNull NotificationSectionsFeatureManager sectionsFeatureManager, @NotNull PeopleNotificationIdentifier peopleNotificationIdentifier, @NotNull HighPriorityProvider highPriorityProvider) {
        Intrinsics.checkParameterIsNotNull(mediaManagerLazy, "mediaManagerLazy");
        Intrinsics.checkParameterIsNotNull(groupManager, "groupManager");
        Intrinsics.checkParameterIsNotNull(headsUpManager, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(notifFilter, "notifFilter");
        Intrinsics.checkParameterIsNotNull(logger, "logger");
        Intrinsics.checkParameterIsNotNull(sectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(peopleNotificationIdentifier, "peopleNotificationIdentifier");
        Intrinsics.checkParameterIsNotNull(highPriorityProvider, "highPriorityProvider");
        this.mediaManagerLazy = mediaManagerLazy;
        this.groupManager = groupManager;
        this.headsUpManager = headsUpManager;
        this.notifFilter = notifFilter;
        this.logger = logger;
        this.sectionsFeatureManager = sectionsFeatureManager;
        this.peopleNotificationIdentifier = peopleNotificationIdentifier;
        this.highPriorityProvider = highPriorityProvider;
        this.mediaManager$delegate = LazyKt__LazyJVMKt.lazy(new Function0<NotificationMediaManager>() { // from class: com.android.systemui.statusbar.notification.collection.NotificationRankingManager$mediaManager$2
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            public final NotificationMediaManager invoke() {
                return (NotificationMediaManager) this.this$0.mediaManagerLazy.get();
            }
        });
        this.rankingComparator = new Comparator<NotificationEntry>() { // from class: com.android.systemui.statusbar.notification.collection.NotificationRankingManager$rankingComparator$1
            @Override // java.util.Comparator
            public final int compare(NotificationEntry a, NotificationEntry b) {
                Intrinsics.checkExpressionValueIsNotNull(a, "a");
                StatusBarNotification sbn = a.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "a.sbn");
                Intrinsics.checkExpressionValueIsNotNull(b, "b");
                StatusBarNotification sbn2 = b.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn2, "b.sbn");
                NotificationListenerService.Ranking ranking = a.getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking, "a.ranking");
                int rank = ranking.getRank();
                NotificationListenerService.Ranking ranking2 = b.getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking2, "b.ranking");
                int rank2 = ranking2.getRank();
                boolean zIsColorizedForegroundService = NotificationRankingManagerKt.isColorizedForegroundService(a);
                boolean zIsColorizedForegroundService2 = NotificationRankingManagerKt.isColorizedForegroundService(b);
                int peopleNotificationType = this.this$0.getPeopleNotificationType(a);
                int peopleNotificationType2 = this.this$0.getPeopleNotificationType(b);
                boolean zIsImportantMedia = this.this$0.isImportantMedia(a);
                boolean zIsImportantMedia2 = this.this$0.isImportantMedia(b);
                boolean zIsSystemMax = NotificationRankingManagerKt.isSystemMax(a);
                boolean zIsSystemMax2 = NotificationRankingManagerKt.isSystemMax(b);
                boolean zIsRowHeadsUp = a.isRowHeadsUp();
                boolean zIsRowHeadsUp2 = b.isRowHeadsUp();
                boolean zIsHighPriority = this.this$0.isHighPriority(a);
                boolean zIsHighPriority2 = this.this$0.isHighPriority(b);
                if (zIsRowHeadsUp != zIsRowHeadsUp2) {
                    if (!zIsRowHeadsUp) {
                        return 1;
                    }
                } else {
                    if (zIsRowHeadsUp) {
                        return this.this$0.headsUpManager.compare(a, b);
                    }
                    if (zIsColorizedForegroundService != zIsColorizedForegroundService2) {
                        if (!zIsColorizedForegroundService) {
                            return 1;
                        }
                    } else {
                        if (this.this$0.getUsePeopleFiltering() && peopleNotificationType != peopleNotificationType2) {
                            return this.this$0.peopleNotificationIdentifier.compareTo(peopleNotificationType, peopleNotificationType2);
                        }
                        if (zIsImportantMedia != zIsImportantMedia2) {
                            if (!zIsImportantMedia) {
                                return 1;
                            }
                        } else {
                            if (zIsSystemMax == zIsSystemMax2) {
                                if (zIsHighPriority != zIsHighPriority2) {
                                    return Intrinsics.compare(zIsHighPriority ? 1 : 0, zIsHighPriority2 ? 1 : 0) * (-1);
                                }
                                return rank != rank2 ? rank - rank2 : (sbn2.getNotification().when > sbn.getNotification().when ? 1 : (sbn2.getNotification().when == sbn.getNotification().when ? 0 : -1));
                            }
                            if (!zIsSystemMax) {
                                return 1;
                            }
                        }
                    }
                }
                return -1;
            }
        };
    }

    @Nullable
    public final NotificationListenerService.RankingMap getRankingMap() {
        return this.rankingMap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean getUsePeopleFiltering() {
        return this.sectionsFeatureManager.isFilteringEnabled();
    }

    @NotNull
    public final List<NotificationEntry> updateRanking(@Nullable NotificationListenerService.RankingMap rankingMap, @NotNull Collection<NotificationEntry> entries, @NotNull String reason) {
        List<NotificationEntry> listFilterAndSortLocked;
        Intrinsics.checkParameterIsNotNull(entries, "entries");
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        if (rankingMap != null) {
            this.rankingMap = rankingMap;
            updateRankingForEntries(entries);
        }
        synchronized (this) {
            listFilterAndSortLocked = filterAndSortLocked(entries, reason);
        }
        return listFilterAndSortLocked;
    }

    private final List<NotificationEntry> filterAndSortLocked(Collection<NotificationEntry> collection, String str) {
        this.logger.logFilterAndSort(str);
        List<NotificationEntry> list = SequencesKt___SequencesKt.toList(SequencesKt___SequencesKt.sortedWith(SequencesKt___SequencesKt.filterNot(CollectionsKt___CollectionsKt.asSequence(collection), new NotificationRankingManager$filterAndSortLocked$filtered$1(this)), this.rankingComparator));
        for (NotificationEntry notificationEntry : collection) {
            notificationEntry.setBucket(getBucketForEntry(notificationEntry));
        }
        return list;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean filter(NotificationEntry notificationEntry) {
        boolean zShouldFilterOut = this.notifFilter.shouldFilterOut(notificationEntry);
        if (zShouldFilterOut) {
            notificationEntry.resetInitializationTime();
        }
        return zShouldFilterOut;
    }

    private final int getBucketForEntry(NotificationEntry notificationEntry) {
        boolean zIsRowHeadsUp = notificationEntry.isRowHeadsUp();
        boolean zIsImportantMedia = isImportantMedia(notificationEntry);
        boolean zIsSystemMax = NotificationRankingManagerKt.isSystemMax(notificationEntry);
        if (NotificationRankingManagerKt.isColorizedForegroundService(notificationEntry)) {
            return 3;
        }
        if (getUsePeopleFiltering() && isConversation(notificationEntry)) {
            return 4;
        }
        return (zIsRowHeadsUp || zIsImportantMedia || zIsSystemMax || isHighPriority(notificationEntry)) ? 5 : 6;
    }

    private final void updateRankingForEntries(Iterable<NotificationEntry> iterable) {
        NotificationListenerService.RankingMap rankingMap = this.rankingMap;
        if (rankingMap != null) {
            synchronized (iterable) {
                for (NotificationEntry notificationEntry : iterable) {
                    NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                    if (rankingMap.getRanking(notificationEntry.getKey(), ranking)) {
                        notificationEntry.setRanking(ranking);
                        String overrideGroupKey = ranking.getOverrideGroupKey();
                        StatusBarNotification sbn = notificationEntry.getSbn();
                        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                        if (!Objects.equals(sbn.getOverrideGroupKey(), overrideGroupKey)) {
                            StatusBarNotification sbn2 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
                            String groupKey = sbn2.getGroupKey();
                            StatusBarNotification sbn3 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
                            boolean zIsGroup = sbn3.isGroup();
                            StatusBarNotification sbn4 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
                            Notification notification = sbn4.getNotification();
                            Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
                            boolean zIsGroupSummary = notification.isGroupSummary();
                            StatusBarNotification sbn5 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn5, "entry.sbn");
                            sbn5.setOverrideGroupKey(overrideGroupKey);
                            this.groupManager.onEntryUpdated(notificationEntry, groupKey, zIsGroup, zIsGroupSummary);
                        }
                    }
                }
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isImportantMedia(@NotNull NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        NotificationMediaManager mediaManager = getMediaManager();
        Intrinsics.checkExpressionValueIsNotNull(mediaManager, "mediaManager");
        if (Intrinsics.areEqual(key, mediaManager.getMediaNotificationKey())) {
            NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
            if (ranking.getImportance() > 1) {
                return true;
            }
        }
        return false;
    }

    private final boolean isConversation(@NotNull NotificationEntry notificationEntry) {
        return getPeopleNotificationType(notificationEntry) != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int getPeopleNotificationType(@NotNull NotificationEntry notificationEntry) {
        PeopleNotificationIdentifier peopleNotificationIdentifier = this.peopleNotificationIdentifier;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
        return peopleNotificationIdentifier.getPeopleNotificationType(sbn, ranking);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isHighPriority(@NotNull NotificationEntry notificationEntry) {
        return this.highPriorityProvider.isHighPriority(notificationEntry);
    }
}
