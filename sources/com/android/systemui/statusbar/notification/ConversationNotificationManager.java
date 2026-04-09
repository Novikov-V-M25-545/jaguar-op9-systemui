package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.collections.MapsKt___MapsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ConversationNotifications.kt */
/* loaded from: classes.dex */
public final class ConversationNotificationManager {
    public static final Companion Companion = new Companion(null);
    private final Context context;
    private final Handler mainHandler;
    private boolean notifPanelCollapsed;
    private final NotificationEntryManager notificationEntryManager;
    private final NotificationGroupManager notificationGroupManager;
    private final ConcurrentHashMap<String, ConversationState> states;

    public ConversationNotificationManager(@NotNull NotificationEntryManager notificationEntryManager, @NotNull NotificationGroupManager notificationGroupManager, @NotNull Context context, @NotNull Handler mainHandler) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "notificationGroupManager");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(mainHandler, "mainHandler");
        this.notificationEntryManager = notificationEntryManager;
        this.notificationGroupManager = notificationGroupManager;
        this.context = context;
        this.mainHandler = mainHandler;
        this.states = new ConcurrentHashMap<>();
        this.notifPanelCollapsed = true;
        notificationEntryManager.addNotificationEntryListener(new AnonymousClass1());
    }

    /* compiled from: ConversationNotifications.kt */
    /* renamed from: com.android.systemui.statusbar.notification.ConversationNotificationManager$1, reason: invalid class name */
    public static final class AnonymousClass1 implements NotificationEntryListener {
        AnonymousClass1() {
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onNotificationRankingUpdated(@NotNull NotificationListenerService.RankingMap rankingMap) {
            Sequence<ConversationLayout> sequenceEmptySequence;
            NotificationContentView[] layouts;
            Sequence sequenceAsSequence;
            Sequence sequenceFlatMap;
            Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
            NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
            Set setKeySet = ConversationNotificationManager.this.states.keySet();
            Intrinsics.checkExpressionValueIsNotNull(setKeySet, "states.keys");
            for (NotificationEntry notificationEntry : SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(setKeySet), new Function1<String, NotificationEntry>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$1$onNotificationRankingUpdated$activeConversationEntries$1
                {
                    super(1);
                }

                @Override // kotlin.jvm.functions.Function1
                public final NotificationEntry invoke(@NotNull String it) {
                    Intrinsics.checkParameterIsNotNull(it, "it");
                    return ConversationNotificationManager.this.notificationEntryManager.getActiveNotificationUnfiltered(it);
                }
            })) {
                StatusBarNotification sbn = notificationEntry.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                if (rankingMap.getRanking(sbn.getKey(), ranking) && ranking.isConversation()) {
                    NotificationChannel channel = ranking.getChannel();
                    Intrinsics.checkExpressionValueIsNotNull(channel, "ranking.channel");
                    final boolean zIsImportantConversation = channel.isImportantConversation();
                    ExpandableNotificationRow row = notificationEntry.getRow();
                    if (row == null || (layouts = row.getLayouts()) == null || (sequenceAsSequence = ArraysKt___ArraysKt.asSequence(layouts)) == null || (sequenceFlatMap = SequencesKt___SequencesKt.flatMap(sequenceAsSequence, ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1.INSTANCE)) == null || (sequenceEmptySequence = SequencesKt___SequencesKt.mapNotNull(sequenceFlatMap, new Function1<View, ConversationLayout>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2
                        @Override // kotlin.jvm.functions.Function1
                        @Nullable
                        public final ConversationLayout invoke(View view) {
                            if (!(view instanceof ConversationLayout)) {
                                view = null;
                            }
                            return (ConversationLayout) view;
                        }
                    })) == null) {
                        sequenceEmptySequence = SequencesKt__SequencesKt.emptySequence();
                    }
                    boolean z = false;
                    for (final ConversationLayout conversationLayout : sequenceEmptySequence) {
                        if (zIsImportantConversation != conversationLayout.isImportantConversation()) {
                            z = true;
                            if (zIsImportantConversation && notificationEntry.isMarkedForUserTriggeredMovement()) {
                                ConversationNotificationManager.this.mainHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$1$onNotificationRankingUpdated$2
                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        conversationLayout.setIsImportantConversation(zIsImportantConversation, true);
                                    }
                                }, 960);
                            } else {
                                conversationLayout.setIsImportantConversation(zIsImportantConversation);
                            }
                        }
                    }
                    if (z) {
                        ConversationNotificationManager.this.notificationGroupManager.updateIsolation(notificationEntry);
                    }
                }
            }
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryInflated(@NotNull final NotificationEntry entry) {
            Intrinsics.checkParameterIsNotNull(entry, "entry");
            NotificationListenerService.Ranking ranking = entry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
            if (ranking.isConversation()) {
                final ConversationNotificationManager$1$onEntryInflated$1 conversationNotificationManager$1$onEntryInflated$1 = new ConversationNotificationManager$1$onEntryInflated$1(this, entry);
                ExpandableNotificationRow row = entry.getRow();
                if (row != null) {
                    row.setOnExpansionChangedListener(new ExpandableNotificationRow.OnExpansionChangedListener() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$1$onEntryInflated$2
                        @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.OnExpansionChangedListener
                        public final void onExpansionChanged(final boolean z) {
                            ExpandableNotificationRow row2 = entry.getRow();
                            if (row2 != null && row2.isShown() && z) {
                                entry.getRow().performOnIntrinsicHeightReached(new Runnable() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$1$onEntryInflated$2.1
                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        conversationNotificationManager$1$onEntryInflated$1.invoke(z);
                                    }
                                });
                            } else {
                                conversationNotificationManager$1$onEntryInflated$1.invoke(z);
                            }
                        }
                    });
                }
                ExpandableNotificationRow row2 = entry.getRow();
                conversationNotificationManager$1$onEntryInflated$1.invoke(row2 != null && row2.isExpanded());
            }
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryReinflated(@NotNull NotificationEntry entry) {
            Intrinsics.checkParameterIsNotNull(entry, "entry");
            onEntryInflated(entry);
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryRemoved(@NotNull NotificationEntry entry, @Nullable NotificationVisibility notificationVisibility, boolean z, int i) {
            Intrinsics.checkParameterIsNotNull(entry, "entry");
            ConversationNotificationManager.this.removeTrackedEntry(entry);
        }
    }

    public final int getUnreadCount(@NotNull final NotificationEntry entry, @NotNull final Notification.Builder recoveredBuilder) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        Intrinsics.checkParameterIsNotNull(recoveredBuilder, "recoveredBuilder");
        ConversationState conversationStateCompute = this.states.compute(entry.getKey(), new BiFunction<String, ConversationState, ConversationState>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.getUnreadCount.1
            @Override // java.util.function.BiFunction
            @NotNull
            public final ConversationState apply(@NotNull String str, @Nullable ConversationState conversationState) {
                Intrinsics.checkParameterIsNotNull(str, "<anonymous parameter 0>");
                int unreadCount = 1;
                if (conversationState != null) {
                    unreadCount = Notification.areStyledNotificationsVisiblyDifferent(Notification.Builder.recoverBuilder(ConversationNotificationManager.this.context, conversationState.getNotification()), recoveredBuilder) ? conversationState.getUnreadCount() + 1 : conversationState.getUnreadCount();
                }
                StatusBarNotification sbn = entry.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                Notification notification = sbn.getNotification();
                Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
                return new ConversationState(unreadCount, notification);
            }
        });
        if (conversationStateCompute == null) {
            Intrinsics.throwNpe();
        }
        return conversationStateCompute.getUnreadCount();
    }

    public final void onNotificationPanelExpandStateChanged(boolean z) {
        this.notifPanelCollapsed = z;
        if (z) {
            return;
        }
        final Map map = MapsKt__MapsKt.toMap(SequencesKt___SequencesKt.mapNotNull(MapsKt___MapsKt.asSequence(this.states), new Function1<Map.Entry<? extends String, ? extends ConversationState>, Pair<? extends String, ? extends NotificationEntry>>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$onNotificationPanelExpandStateChanged$expanded$1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Pair<? extends String, ? extends NotificationEntry> invoke(Map.Entry<? extends String, ? extends ConversationNotificationManager.ConversationState> entry) {
                return invoke2((Map.Entry<String, ConversationNotificationManager.ConversationState>) entry);
            }

            @Nullable
            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final Pair<String, NotificationEntry> invoke2(@NotNull Map.Entry<String, ConversationNotificationManager.ConversationState> entry) {
                ExpandableNotificationRow row;
                Intrinsics.checkParameterIsNotNull(entry, "<name for destructuring parameter 0>");
                String key = entry.getKey();
                NotificationEntry activeNotificationUnfiltered = this.this$0.notificationEntryManager.getActiveNotificationUnfiltered(key);
                if (activeNotificationUnfiltered == null || (row = activeNotificationUnfiltered.getRow()) == null || !row.isExpanded()) {
                    return null;
                }
                return TuplesKt.to(key, activeNotificationUnfiltered);
            }
        }));
        this.states.replaceAll(new BiFunction<String, ConversationState, ConversationState>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.onNotificationPanelExpandStateChanged.1
            @Override // java.util.function.BiFunction
            @NotNull
            public final ConversationState apply(@NotNull String key, @NotNull ConversationState state) {
                Intrinsics.checkParameterIsNotNull(key, "key");
                Intrinsics.checkParameterIsNotNull(state, "state");
                return map.containsKey(key) ? ConversationState.copy$default(state, 0, null, 2, null) : state;
            }
        });
        Iterator it = SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(map.values()), new Function1<NotificationEntry, ExpandableNotificationRow>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.onNotificationPanelExpandStateChanged.2
            @Override // kotlin.jvm.functions.Function1
            public final ExpandableNotificationRow invoke(@NotNull NotificationEntry it2) {
                Intrinsics.checkParameterIsNotNull(it2, "it");
                return it2.getRow();
            }
        }).iterator();
        while (it.hasNext()) {
            resetBadgeUi((ExpandableNotificationRow) it.next());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void resetCount(String str) {
        this.states.compute(str, new BiFunction<String, ConversationState, ConversationState>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.resetCount.1
            @Override // java.util.function.BiFunction
            @Nullable
            public final ConversationState apply(@NotNull String str2, @Nullable ConversationState conversationState) {
                Intrinsics.checkParameterIsNotNull(str2, "<anonymous parameter 0>");
                if (conversationState != null) {
                    return ConversationState.copy$default(conversationState, 0, null, 2, null);
                }
                return null;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void removeTrackedEntry(NotificationEntry notificationEntry) {
        this.states.remove(notificationEntry.getKey());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void resetBadgeUi(ExpandableNotificationRow expandableNotificationRow) {
        Sequence sequenceEmptySequence;
        NotificationContentView[] layouts = expandableNotificationRow.getLayouts();
        if (layouts == null || (sequenceEmptySequence = ArraysKt___ArraysKt.asSequence(layouts)) == null) {
            sequenceEmptySequence = SequencesKt__SequencesKt.emptySequence();
        }
        Iterator it = SequencesKt___SequencesKt.mapNotNull(SequencesKt___SequencesKt.flatMap(sequenceEmptySequence, new Function1<NotificationContentView, Sequence<? extends View>>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.resetBadgeUi.1
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final Sequence<View> invoke(NotificationContentView layout) {
                Intrinsics.checkExpressionValueIsNotNull(layout, "layout");
                View[] allViews = layout.getAllViews();
                Intrinsics.checkExpressionValueIsNotNull(allViews, "layout.allViews");
                return ArraysKt___ArraysKt.asSequence(allViews);
            }
        }), new Function1<View, ConversationLayout>() { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.resetBadgeUi.2
            @Override // kotlin.jvm.functions.Function1
            @Nullable
            public final ConversationLayout invoke(View view) {
                if (!(view instanceof ConversationLayout)) {
                    view = null;
                }
                return (ConversationLayout) view;
            }
        }).iterator();
        while (it.hasNext()) {
            ((ConversationLayout) it.next()).setUnreadCount(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* compiled from: ConversationNotifications.kt */
    static final class ConversationState {

        @NotNull
        private final Notification notification;
        private final int unreadCount;

        public static /* synthetic */ ConversationState copy$default(ConversationState conversationState, int i, Notification notification, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = conversationState.unreadCount;
            }
            if ((i2 & 2) != 0) {
                notification = conversationState.notification;
            }
            return conversationState.copy(i, notification);
        }

        @NotNull
        public final ConversationState copy(int i, @NotNull Notification notification) {
            Intrinsics.checkParameterIsNotNull(notification, "notification");
            return new ConversationState(i, notification);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConversationState)) {
                return false;
            }
            ConversationState conversationState = (ConversationState) obj;
            return this.unreadCount == conversationState.unreadCount && Intrinsics.areEqual(this.notification, conversationState.notification);
        }

        public int hashCode() {
            int iHashCode = Integer.hashCode(this.unreadCount) * 31;
            Notification notification = this.notification;
            return iHashCode + (notification != null ? notification.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "ConversationState(unreadCount=" + this.unreadCount + ", notification=" + this.notification + ")";
        }

        public ConversationState(int i, @NotNull Notification notification) {
            Intrinsics.checkParameterIsNotNull(notification, "notification");
            this.unreadCount = i;
            this.notification = notification;
        }

        @NotNull
        public final Notification getNotification() {
            return this.notification;
        }

        public final int getUnreadCount() {
            return this.unreadCount;
        }
    }

    /* compiled from: ConversationNotifications.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
