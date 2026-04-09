package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotifEvent.kt */
/* loaded from: classes.dex */
public final class EntryRemovedEvent extends NotifEvent {

    @NotNull
    private final NotificationEntry entry;
    private final int reason;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntryRemovedEvent)) {
            return false;
        }
        EntryRemovedEvent entryRemovedEvent = (EntryRemovedEvent) obj;
        return Intrinsics.areEqual(this.entry, entryRemovedEvent.entry) && this.reason == entryRemovedEvent.reason;
    }

    public int hashCode() {
        NotificationEntry notificationEntry = this.entry;
        return ((notificationEntry != null ? notificationEntry.hashCode() : 0) * 31) + Integer.hashCode(this.reason);
    }

    @NotNull
    public String toString() {
        return "EntryRemovedEvent(entry=" + this.entry + ", reason=" + this.reason + ")";
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public EntryRemovedEvent(@NotNull NotificationEntry entry, int i) {
        super(null);
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        this.entry = entry;
        this.reason = i;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        listener.onEntryRemoved(this.entry, this.reason);
    }
}
