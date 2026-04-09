package com.android.systemui.statusbar.notification.collection;

import android.view.textclassifier.Log;
import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifViewBarn.kt */
/* loaded from: classes.dex */
public final class NotifViewBarn {
    private final boolean DEBUG;
    private final Map<String, NotificationListItem> rowMap = new LinkedHashMap();

    @NotNull
    public final NotificationListItem requireView(@NotNull ListEntry forEntry) {
        Intrinsics.checkParameterIsNotNull(forEntry, "forEntry");
        if (this.DEBUG) {
            Log.d("NotifViewBarn", "requireView: " + forEntry + ".key");
        }
        NotificationListItem notificationListItem = this.rowMap.get(forEntry.getKey());
        if (notificationListItem != null) {
            return notificationListItem;
        }
        throw new IllegalStateException("No view has been registered for entry: " + forEntry);
    }

    public final void registerViewForEntry(@NotNull ListEntry entry, @NotNull NotificationListItem view) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        Intrinsics.checkParameterIsNotNull(view, "view");
        if (this.DEBUG) {
            Log.d("NotifViewBarn", "registerViewForEntry: " + entry + ".key");
        }
        Map<String, NotificationListItem> map = this.rowMap;
        String key = entry.getKey();
        Intrinsics.checkExpressionValueIsNotNull(key, "entry.key");
        map.put(key, view);
    }

    public final void removeViewForEntry(@NotNull ListEntry entry) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        if (this.DEBUG) {
            Log.d("NotifViewBarn", "removeViewForEntry: " + entry + ".key");
        }
        this.rowMap.remove(entry.getKey());
    }
}
