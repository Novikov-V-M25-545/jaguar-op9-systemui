package com.android.systemui.statusbar.notification.collection;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationRankingManager.kt */
/* loaded from: classes.dex */
final /* synthetic */ class NotificationRankingManager$filterAndSortLocked$filtered$1 extends FunctionReference implements Function1<NotificationEntry, Boolean> {
    NotificationRankingManager$filterAndSortLocked$filtered$1(NotificationRankingManager notificationRankingManager) {
        super(1, notificationRankingManager);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "filter";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(NotificationRankingManager.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "filter(Lcom/android/systemui/statusbar/notification/collection/NotificationEntry;)Z";
    }

    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke2(notificationEntry));
    }

    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final boolean invoke2(@NotNull NotificationEntry p1) {
        Intrinsics.checkParameterIsNotNull(p1, "p1");
        return ((NotificationRankingManager) this.receiver).filter(p1);
    }
}
