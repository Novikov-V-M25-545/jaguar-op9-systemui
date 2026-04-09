package com.android.systemui.statusbar.notification.collection;

import android.view.View;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import com.android.systemui.util.Assert;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotifViewManager.kt */
/* loaded from: classes.dex */
public final class NotifViewManager {

    @NotNull
    private List<? extends ListEntry> currentNotifs;
    private final FeatureFlags featureFlags;
    private SimpleNotificationListContainer listContainer;
    private final NotifViewBarn rowRegistry;
    private final VisualStabilityManager stabilityManager;

    public final void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
    }

    public NotifViewManager(@NotNull NotifViewBarn rowRegistry, @NotNull VisualStabilityManager stabilityManager, @NotNull FeatureFlags featureFlags) {
        Intrinsics.checkParameterIsNotNull(rowRegistry, "rowRegistry");
        Intrinsics.checkParameterIsNotNull(stabilityManager, "stabilityManager");
        Intrinsics.checkParameterIsNotNull(featureFlags, "featureFlags");
        this.rowRegistry = rowRegistry;
        this.stabilityManager = stabilityManager;
        this.featureFlags = featureFlags;
        this.currentNotifs = CollectionsKt__CollectionsKt.emptyList();
    }

    public final void attach(@NotNull ShadeListBuilder listBuilder) {
        Intrinsics.checkParameterIsNotNull(listBuilder, "listBuilder");
        if (this.featureFlags.isNewNotifPipelineRenderingEnabled()) {
            listBuilder.setOnRenderListListener(new ShadeListBuilder.OnRenderListListener() { // from class: com.android.systemui.statusbar.notification.collection.NotifViewManager.attach.1
                @Override // com.android.systemui.statusbar.notification.collection.ShadeListBuilder.OnRenderListListener
                public final void onRenderList(@NotNull List<? extends ListEntry> entries) {
                    Intrinsics.checkParameterIsNotNull(entries, "entries");
                    NotifViewManager.this.onNotifTreeBuilt(entries);
                }
            });
        }
    }

    public final void setViewConsumer(@NotNull SimpleNotificationListContainer consumer) {
        Intrinsics.checkParameterIsNotNull(consumer, "consumer");
        this.listContainer = consumer;
    }

    public final void onNotifTreeBuilt(@NotNull List<? extends ListEntry> notifList) {
        Intrinsics.checkParameterIsNotNull(notifList, "notifList");
        Assert.isMainThread();
        detachRows(notifList);
        attachRows(notifList);
        this.currentNotifs = notifList;
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x004a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private final void detachRows(java.util.List<? extends com.android.systemui.statusbar.notification.collection.ListEntry> r10) {
        /*
            r9 = this;
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r0 = r9.listContainer
            java.lang.String r1 = "listContainer"
            if (r0 != 0) goto L9
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L9:
            kotlin.sequences.Sequence r0 = r9.getListItems(r0)
            com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1 r2 = new kotlin.jvm.functions.Function1<com.android.systemui.statusbar.notification.stack.NotificationListItem, java.lang.Boolean>() { // from class: com.android.systemui.statusbar.notification.collection.NotifViewManager.detachRows.1
                static {
                    /*
                        com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1 r0 = new com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1
                        r0.<init>()
                        
                        // error: 0x0005: SPUT (r0 I:com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1) com.android.systemui.statusbar.notification.collection.NotifViewManager.detachRows.1.INSTANCE com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.C01381.<clinit>():void");
                }

                {
                    /*
                        r1 = this;
                        r0 = 1
                        r1.<init>(r0)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.C01381.<init>():void");
                }

                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ java.lang.Boolean invoke(com.android.systemui.statusbar.notification.stack.NotificationListItem r1) {
                    /*
                        r0 = this;
                        com.android.systemui.statusbar.notification.stack.NotificationListItem r1 = (com.android.systemui.statusbar.notification.stack.NotificationListItem) r1
                        boolean r0 = r0.invoke2(r1)
                        java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)
                        return r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.C01381.invoke(java.lang.Object):java.lang.Object");
                }

                /* renamed from: invoke, reason: avoid collision after fix types in other method */
                public final boolean invoke2(@org.jetbrains.annotations.NotNull com.android.systemui.statusbar.notification.stack.NotificationListItem r1) {
                    /*
                        r0 = this;
                        java.lang.String r0 = "it"
                        kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r1, r0)
                        boolean r0 = r1.isBlockingHelperShowing()
                        r0 = r0 ^ 1
                        return r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.C01381.invoke2(com.android.systemui.statusbar.notification.stack.NotificationListItem):boolean");
                }
            }
            kotlin.sequences.Sequence r0 = kotlin.sequences.SequencesKt.filter(r0, r2)
            java.util.Iterator r0 = r0.iterator()
        L17:
            boolean r2 = r0.hasNext()
            if (r2 == 0) goto Lf3
            java.lang.Object r2 = r0.next()
            com.android.systemui.statusbar.notification.stack.NotificationListItem r2 = (com.android.systemui.statusbar.notification.stack.NotificationListItem) r2
            com.android.systemui.statusbar.notification.collection.NotificationEntry r3 = r2.getEntry()
            java.lang.String r4 = "listItem.entry"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r3, r4)
            com.android.systemui.statusbar.notification.collection.GroupEntry r3 = r3.getParent()
            com.android.systemui.statusbar.notification.collection.GroupEntry r5 = com.android.systemui.statusbar.notification.collection.GroupEntry.ROOT_ENTRY
            boolean r3 = kotlin.jvm.internal.Intrinsics.areEqual(r3, r5)
            r5 = 1
            r3 = r3 ^ r5
            r6 = 0
            if (r3 == 0) goto L4a
            com.android.systemui.statusbar.notification.collection.NotificationEntry r7 = r2.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r7, r4)
            com.android.systemui.statusbar.notification.collection.GroupEntry r4 = r7.getParent()
            if (r4 == 0) goto L4a
            r4 = r5
            goto L4b
        L4a:
            r4 = r6
        L4b:
            com.android.systemui.statusbar.notification.collection.NotificationEntry r7 = r2.getEntry()
            int r7 = r10.indexOf(r7)
            if (r3 == 0) goto L7f
            boolean r3 = r2.isSummaryWithChildren()
            if (r3 == 0) goto L5e
            r2.removeAllChildren()
        L5e:
            if (r4 == 0) goto L6a
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r3 = r9.listContainer
            if (r3 != 0) goto L67
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L67:
            r3.setChildTransferInProgress(r5)
        L6a:
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r3 = r9.listContainer
            if (r3 != 0) goto L71
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L71:
            r3.removeListItem(r2)
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r2 = r9.listContainer
            if (r2 != 0) goto L7b
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        L7b:
            r2.setChildTransferInProgress(r6)
            goto L17
        L7f:
            java.lang.Object r3 = r10.get(r7)
            boolean r3 = r3 instanceof com.android.systemui.statusbar.notification.collection.GroupEntry
            if (r3 == 0) goto L17
            java.lang.Object r3 = r10.get(r7)
            if (r3 == 0) goto Leb
            com.android.systemui.statusbar.notification.collection.GroupEntry r3 = (com.android.systemui.statusbar.notification.collection.GroupEntry) r3
            java.util.List r3 = r3.getChildren()
            java.lang.String r4 = "(entries[idx] as GroupEntry).children"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r3, r4)
            java.util.List r4 = r2.getAttachedChildren()
            if (r4 == 0) goto L17
            java.util.Iterator r4 = r4.iterator()
        La2:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L17
            java.lang.Object r5 = r4.next()
            com.android.systemui.statusbar.notification.stack.NotificationListItem r5 = (com.android.systemui.statusbar.notification.stack.NotificationListItem) r5
            java.lang.String r6 = "listChild"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r6)
            com.android.systemui.statusbar.notification.collection.NotificationEntry r6 = r5.getEntry()
            boolean r6 = r3.contains(r6)
            if (r6 != 0) goto La2
            r2.removeChildNotification(r5)
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r6 = r9.listContainer
            if (r6 != 0) goto Lc7
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
        Lc7:
            android.view.View r7 = r5.getView()
            java.lang.String r8 = "listChild.view"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r7, r8)
            android.view.View r5 = r5.getView()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r8)
            android.view.ViewParent r5 = r5.getParent()
            if (r5 == 0) goto Le3
            android.view.ViewGroup r5 = (android.view.ViewGroup) r5
            r6.notifyGroupChildRemoved(r7, r5)
            goto La2
        Le3:
            kotlin.TypeCastException r9 = new kotlin.TypeCastException
            java.lang.String r10 = "null cannot be cast to non-null type android.view.ViewGroup"
            r9.<init>(r10)
            throw r9
        Leb:
            kotlin.TypeCastException r9 = new kotlin.TypeCastException
            java.lang.String r10 = "null cannot be cast to non-null type com.android.systemui.statusbar.notification.collection.GroupEntry"
            r9.<init>(r10)
            throw r9
        Lf3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.detachRows(java.util.List):void");
    }

    private final Sequence<NotificationListItem> getListItems(final SimpleNotificationListContainer simpleNotificationListContainer) {
        Sequence<NotificationListItem> sequenceFilter = SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.map(CollectionsKt___CollectionsKt.asSequence(RangesKt___RangesKt.until(0, simpleNotificationListContainer.getContainerChildCount())), new Function1<Integer, View>() { // from class: com.android.systemui.statusbar.notification.collection.NotifViewManager.getListItems.1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ View invoke(Integer num) {
                return invoke(num.intValue());
            }

            @NotNull
            public final View invoke(int i) {
                return simpleNotificationListContainer.getContainerChildAt(i);
            }
        }), new Function1<Object, Boolean>() { // from class: com.android.systemui.statusbar.notification.collection.NotifViewManager$getListItems$$inlined$filterIsInstance$1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(Object obj) {
                return Boolean.valueOf(invoke2(obj));
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final boolean invoke2(@Nullable Object obj) {
                return obj instanceof NotificationListItem;
            }
        });
        if (sequenceFilter != null) {
            return sequenceFilter;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.sequences.Sequence<R>");
    }

    private final List<NotificationListItem> getChildListFromParent(ListEntry listEntry) {
        if (listEntry instanceof GroupEntry) {
            List<NotificationEntry> children = ((GroupEntry) listEntry).getChildren();
            Intrinsics.checkExpressionValueIsNotNull(children, "parent.children");
            ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(children, 10));
            for (NotificationEntry child : children) {
                NotifViewBarn notifViewBarn = this.rowRegistry;
                Intrinsics.checkExpressionValueIsNotNull(child, "child");
                arrayList.add(notifViewBarn.requireView(child));
            }
            return CollectionsKt___CollectionsKt.toList(arrayList);
        }
        return CollectionsKt__CollectionsKt.emptyList();
    }

    /* JADX WARN: Removed duplicated region for block: B:23:0x008c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private final void attachRows(java.util.List<? extends com.android.systemui.statusbar.notification.collection.ListEntry> r13) {
        /*
            Method dump skipped, instructions count: 285
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.attachRows(java.util.List):void");
    }
}
