package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotifEvent.kt */
/* loaded from: classes.dex */
public final class EntryAddedEvent extends NotifEvent {

    @NotNull
    private final NotificationEntry entry;

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof EntryAddedEvent) && Intrinsics.areEqual(this.entry, ((EntryAddedEvent) obj).entry);
        }
        return true;
    }

    public int hashCode() {
        NotificationEntry notificationEntry = this.entry;
        if (notificationEntry != null) {
            return notificationEntry.hashCode();
        }
        return 0;
    }

    @NotNull
    public String toString() {
        return "EntryAddedEvent(entry=" + this.entry + ")";
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public EntryAddedEvent(@NotNull NotificationEntry entry) {
        super(null);
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        this.entry = entry;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        listener.onEntryAdded(this.entry);
    }
}
