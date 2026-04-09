package com.android.systemui.settings;

import android.content.Context;
import android.os.UserHandle;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.util.Assert;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CurrentUserContextTracker.kt */
/* loaded from: classes.dex */
public final class CurrentUserContextTracker {
    private Context _curUserContext;
    private boolean initialized;
    private final Context sysuiContext;
    private final CurrentUserTracker userTracker;

    public CurrentUserContextTracker(@NotNull Context sysuiContext, @NotNull final BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(sysuiContext, "sysuiContext");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        this.sysuiContext = sysuiContext;
        this.userTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.settings.CurrentUserContextTracker.1
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                CurrentUserContextTracker.this.handleUserSwitched(i);
            }
        };
    }

    @NotNull
    public final Context getCurrentUserContext() {
        if (!this.initialized) {
            throw new IllegalStateException("Must initialize before getting context");
        }
        Context context = this._curUserContext;
        if (context == null) {
            Intrinsics.throwNpe();
        }
        return context;
    }

    public final void initialize() {
        this.initialized = true;
        this.userTracker.startTracking();
        this._curUserContext = makeUserContext(this.userTracker.getCurrentUserId());
    }

    public final void handleUserSwitched(int i) {
        this._curUserContext = makeUserContext(i);
    }

    private final Context makeUserContext(int i) {
        Assert.isMainThread();
        Context contextCreateContextAsUser = this.sysuiContext.createContextAsUser(UserHandle.of(i), 0);
        Intrinsics.checkExpressionValueIsNotNull(contextCreateContextAsUser, "sysuiContext.createConte…er(UserHandle.of(uid), 0)");
        return contextCreateContextAsUser;
    }
}
