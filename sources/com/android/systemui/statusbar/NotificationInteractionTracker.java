package com.android.systemui.statusbar;

import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationInteractionTracker.kt */
/* loaded from: classes.dex */
public final class NotificationInteractionTracker implements NotifCollectionListener, NotificationInteractionListener {
    private final NotificationClickNotifier clicker;
    private final NotificationEntryManager entryManager;
    private final Map<String, Boolean> interactions;

    public NotificationInteractionTracker(@NotNull NotificationClickNotifier clicker, @NotNull NotificationEntryManager entryManager) {
        Intrinsics.checkParameterIsNotNull(clicker, "clicker");
        Intrinsics.checkParameterIsNotNull(entryManager, "entryManager");
        this.clicker = clicker;
        this.entryManager = entryManager;
        this.interactions = new LinkedHashMap();
        clicker.addNotificationInteractionListener(this);
        entryManager.addCollectionListener(this);
    }

    public final boolean hasUserInteractedWith(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Boolean bool = this.interactions.get(key);
        if (bool != null) {
            return bool.booleanValue();
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryAdded(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        Map<String, Boolean> map = this.interactions;
        String key = entry.getKey();
        Intrinsics.checkExpressionValueIsNotNull(key, "entry.key");
        map.put(key, Boolean.FALSE);
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryCleanUp(@NotNull NotificationEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        this.interactions.remove(entry.getKey());
    }

    @Override // com.android.systemui.statusbar.NotificationInteractionListener
    public void onNotificationInteraction(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        this.interactions.put(key, Boolean.TRUE);
    }
}
