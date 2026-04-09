package com.android.systemui.statusbar.notification.people;

import android.app.NotificationChannel;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleNotificationIdentifier.kt */
/* loaded from: classes.dex */
public final class PeopleNotificationIdentifierImpl implements PeopleNotificationIdentifier {
    private final NotificationGroupManager groupManager;
    private final NotificationPersonExtractor personExtractor;

    public PeopleNotificationIdentifierImpl(@NotNull NotificationPersonExtractor personExtractor, @NotNull NotificationGroupManager groupManager) {
        Intrinsics.checkParameterIsNotNull(personExtractor, "personExtractor");
        Intrinsics.checkParameterIsNotNull(groupManager, "groupManager");
        this.personExtractor = personExtractor;
        this.groupManager = groupManager;
    }

    @Override // com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
    public int getPeopleNotificationType(@NotNull StatusBarNotification sbn, @NotNull NotificationListenerService.Ranking ranking) {
        int iUpperBound;
        Intrinsics.checkParameterIsNotNull(sbn, "sbn");
        Intrinsics.checkParameterIsNotNull(ranking, "ranking");
        int personTypeInfo = getPersonTypeInfo(ranking);
        if (personTypeInfo == 3 || (iUpperBound = upperBound(personTypeInfo, extractPersonTypeInfo(sbn))) == 3) {
            return 3;
        }
        return upperBound(iUpperBound, getPeopleTypeOfSummary(sbn));
    }

    @Override // com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
    public int compareTo(int i, int i2) {
        return Intrinsics.compare(i2, i);
    }

    private final int upperBound(int i, int i2) {
        return Math.max(i, i2);
    }

    private final int getPersonTypeInfo(@NotNull NotificationListenerService.Ranking ranking) {
        if (!ranking.isConversation()) {
            return 0;
        }
        if (ranking.getShortcutInfo() == null) {
            return 1;
        }
        NotificationChannel channel = ranking.getChannel();
        return (channel == null || !channel.isImportantConversation()) ? 2 : 3;
    }

    private final int extractPersonTypeInfo(StatusBarNotification statusBarNotification) {
        return this.personExtractor.isPersonNotification(statusBarNotification) ? 1 : 0;
    }

    private final int getPeopleTypeOfSummary(StatusBarNotification statusBarNotification) {
        Sequence sequenceAsSequence;
        Sequence map;
        int iUpperBound = 0;
        if (!this.groupManager.isSummaryOfGroup(statusBarNotification)) {
            return 0;
        }
        ArrayList<NotificationEntry> children = this.groupManager.getChildren(statusBarNotification);
        if (children != null && (sequenceAsSequence = CollectionsKt___CollectionsKt.asSequence(children)) != null && (map = SequencesKt___SequencesKt.map(sequenceAsSequence, new Function1<NotificationEntry, Integer>() { // from class: com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl$getPeopleTypeOfSummary$childTypes$1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Integer invoke(NotificationEntry notificationEntry) {
                return Integer.valueOf(invoke2(notificationEntry));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final int invoke2(NotificationEntry it) {
                PeopleNotificationIdentifierImpl peopleNotificationIdentifierImpl = this.this$0;
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                StatusBarNotification sbn = it.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
                NotificationListenerService.Ranking ranking = it.getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking, "it.ranking");
                return peopleNotificationIdentifierImpl.getPeopleNotificationType(sbn, ranking);
            }
        })) != null) {
            Iterator it = map.iterator();
            while (it.hasNext() && (iUpperBound = upperBound(iUpperBound, ((Number) it.next()).intValue())) != 3) {
            }
        }
        return iUpperBound;
    }
}
