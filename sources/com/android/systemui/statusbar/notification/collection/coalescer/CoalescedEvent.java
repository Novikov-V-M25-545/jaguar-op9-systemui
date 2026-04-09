package com.android.systemui.statusbar.notification.collection.coalescer;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CoalescedEvent.kt */
/* loaded from: classes.dex */
public final class CoalescedEvent {

    @Nullable
    private EventBatch batch;

    @NotNull
    private final String key;
    private int position;

    @NotNull
    private NotificationListenerService.Ranking ranking;

    @NotNull
    private StatusBarNotification sbn;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CoalescedEvent)) {
            return false;
        }
        CoalescedEvent coalescedEvent = (CoalescedEvent) obj;
        return Intrinsics.areEqual(this.key, coalescedEvent.key) && this.position == coalescedEvent.position && Intrinsics.areEqual(this.sbn, coalescedEvent.sbn) && Intrinsics.areEqual(this.ranking, coalescedEvent.ranking) && Intrinsics.areEqual(this.batch, coalescedEvent.batch);
    }

    public int hashCode() {
        String str = this.key;
        int iHashCode = (((str != null ? str.hashCode() : 0) * 31) + Integer.hashCode(this.position)) * 31;
        StatusBarNotification statusBarNotification = this.sbn;
        int iHashCode2 = (iHashCode + (statusBarNotification != null ? statusBarNotification.hashCode() : 0)) * 31;
        NotificationListenerService.Ranking ranking = this.ranking;
        int iHashCode3 = (iHashCode2 + (ranking != null ? ranking.hashCode() : 0)) * 31;
        EventBatch eventBatch = this.batch;
        return iHashCode3 + (eventBatch != null ? eventBatch.hashCode() : 0);
    }

    public CoalescedEvent(@NotNull String key, int i, @NotNull StatusBarNotification sbn, @NotNull NotificationListenerService.Ranking ranking, @Nullable EventBatch eventBatch) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(sbn, "sbn");
        Intrinsics.checkParameterIsNotNull(ranking, "ranking");
        this.key = key;
        this.position = i;
        this.sbn = sbn;
        this.ranking = ranking;
        this.batch = eventBatch;
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    public final int getPosition() {
        return this.position;
    }

    @NotNull
    public final StatusBarNotification getSbn() {
        return this.sbn;
    }

    @NotNull
    public final NotificationListenerService.Ranking getRanking() {
        return this.ranking;
    }

    public final void setRanking(@NotNull NotificationListenerService.Ranking ranking) {
        Intrinsics.checkParameterIsNotNull(ranking, "<set-?>");
        this.ranking = ranking;
    }

    @Nullable
    public final EventBatch getBatch() {
        return this.batch;
    }

    public final void setBatch(@Nullable EventBatch eventBatch) {
        this.batch = eventBatch;
    }

    @NotNull
    public String toString() {
        return "CoalescedEvent(key=" + this.key + ')';
    }
}
