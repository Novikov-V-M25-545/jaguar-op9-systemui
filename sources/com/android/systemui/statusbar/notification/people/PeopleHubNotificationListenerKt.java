package com.android.systemui.statusbar.notification.people;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.internal.widget.MessagingGroup;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import kotlin.sequences.SequencesKt__SequenceBuilderKt;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHubNotificationListener.kt */
/* loaded from: classes.dex */
public final class PeopleHubNotificationListenerKt {
    /* JADX INFO: Access modifiers changed from: private */
    public static final Sequence<View> getChildren(@NotNull ViewGroup viewGroup) {
        return SequencesKt__SequenceBuilderKt.sequence(new PeopleHubNotificationListenerKt$children$1(viewGroup, null));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Sequence<View> childrenWithId(@NotNull ViewGroup viewGroup, final int i) {
        return SequencesKt___SequencesKt.filter(getChildren(viewGroup), new Function1<View, Boolean>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubNotificationListenerKt.childrenWithId.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(View view) {
                return Boolean.valueOf(invoke2(view));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final boolean invoke2(@NotNull View it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return it.getId() == i;
            }
        });
    }

    @Nullable
    public static final Drawable extractAvatarFromRow(@NotNull NotificationEntry entry) {
        Sequence<View> sequenceChildrenWithId;
        Sequence sequenceMapNotNull;
        Sequence sequenceFlatMap;
        Sequence sequenceMapNotNull2;
        Sequence sequenceMapNotNull3;
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        ExpandableNotificationRow row = entry.getRow();
        if (row == null || (sequenceChildrenWithId = childrenWithId(row, R.id.expanded)) == null || (sequenceMapNotNull = SequencesKt___SequencesKt.mapNotNull(sequenceChildrenWithId, new Function1<View, ViewGroup>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubNotificationListenerKt.extractAvatarFromRow.1
            @Override // kotlin.jvm.functions.Function1
            @Nullable
            public final ViewGroup invoke(@NotNull View it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                if (!(it instanceof ViewGroup)) {
                    it = null;
                }
                return (ViewGroup) it;
            }
        })) == null || (sequenceFlatMap = SequencesKt___SequencesKt.flatMap(sequenceMapNotNull, new Function1<ViewGroup, Sequence<? extends View>>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubNotificationListenerKt.extractAvatarFromRow.2
            @Override // kotlin.jvm.functions.Function1
            @NotNull
            public final Sequence<View> invoke(@NotNull ViewGroup it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return PeopleHubNotificationListenerKt.childrenWithId(it, android.R.id.screenSize);
            }
        })) == null || (sequenceMapNotNull2 = SequencesKt___SequencesKt.mapNotNull(sequenceFlatMap, new Function1<View, ViewGroup>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubNotificationListenerKt.extractAvatarFromRow.3
            @Override // kotlin.jvm.functions.Function1
            public final ViewGroup invoke(@NotNull View it) {
                Intrinsics.checkParameterIsNotNull(it, "it");
                return (ViewGroup) it.findViewById(android.R.id.mediaProjection);
            }
        })) == null || (sequenceMapNotNull3 = SequencesKt___SequencesKt.mapNotNull(sequenceMapNotNull2, new Function1<ViewGroup, Drawable>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubNotificationListenerKt.extractAvatarFromRow.4
            @Override // kotlin.jvm.functions.Function1
            @Nullable
            public final Drawable invoke(@NotNull ViewGroup messagesView) {
                ImageView imageView;
                Intrinsics.checkParameterIsNotNull(messagesView, "messagesView");
                MessagingGroup messagingGroup = (MessagingGroup) SequencesKt.lastOrNull(SequencesKt___SequencesKt.mapNotNull(PeopleHubNotificationListenerKt.getChildren(messagesView), new Function1<View, MessagingGroup>() { // from class: com.android.systemui.statusbar.notification.people.PeopleHubNotificationListenerKt.extractAvatarFromRow.4.1
                    @Override // kotlin.jvm.functions.Function1
                    @Nullable
                    public final MessagingGroup invoke(@NotNull View it) {
                        Intrinsics.checkParameterIsNotNull(it, "it");
                        if (!(it instanceof MessagingGroup)) {
                            it = null;
                        }
                        return (MessagingGroup) it;
                    }
                }));
                if (messagingGroup == null || (imageView = (ImageView) messagingGroup.findViewById(android.R.id.insideOverlay)) == null) {
                    return null;
                }
                return imageView.getDrawable();
            }
        })) == null) {
            return null;
        }
        return (Drawable) SequencesKt.firstOrNull(sequenceMapNotNull3);
    }
}
