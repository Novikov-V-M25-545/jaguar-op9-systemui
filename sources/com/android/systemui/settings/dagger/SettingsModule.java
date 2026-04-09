package com.android.systemui.settings.dagger;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserContextTracker;

/* loaded from: classes.dex */
public abstract class SettingsModule {
    static CurrentUserContextTracker provideCurrentUserContextTracker(Context context, BroadcastDispatcher broadcastDispatcher) {
        CurrentUserContextTracker currentUserContextTracker = new CurrentUserContextTracker(context, broadcastDispatcher);
        currentUserContextTracker.initialize();
        return currentUserContextTracker;
    }
}
