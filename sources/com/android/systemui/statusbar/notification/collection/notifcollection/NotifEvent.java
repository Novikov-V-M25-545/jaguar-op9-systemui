package com.android.systemui.statusbar.notification.collection.notifcollection;

import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifEvent.kt */
/* loaded from: classes.dex */
public abstract class NotifEvent {
    public abstract void dispatchToListener(@NotNull NotifCollectionListener notifCollectionListener);

    private NotifEvent() {
    }

    public /* synthetic */ NotifEvent(DefaultConstructorMarker defaultConstructorMarker) {
        this();
    }

    public final void dispatchTo(@NotNull List<? extends NotifCollectionListener> listeners) {
        Intrinsics.checkParameterIsNotNull(listeners, "listeners");
        int size = listeners.size();
        for (int i = 0; i < size; i++) {
            dispatchToListener(listeners.get(i));
        }
    }
}
