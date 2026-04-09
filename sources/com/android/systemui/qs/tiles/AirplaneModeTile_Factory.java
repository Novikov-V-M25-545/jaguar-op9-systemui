package com.android.systemui.qs.tiles;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AirplaneModeTile_Factory implements Factory<AirplaneModeTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;

    public AirplaneModeTile_Factory(Provider<QSHost> provider, Provider<ActivityStarter> provider2, Provider<BroadcastDispatcher> provider3, Provider<KeyguardStateController> provider4) {
        this.hostProvider = provider;
        this.activityStarterProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public AirplaneModeTile get() {
        return provideInstance(this.hostProvider, this.activityStarterProvider, this.broadcastDispatcherProvider, this.keyguardStateControllerProvider);
    }

    public static AirplaneModeTile provideInstance(Provider<QSHost> provider, Provider<ActivityStarter> provider2, Provider<BroadcastDispatcher> provider3, Provider<KeyguardStateController> provider4) {
        return new AirplaneModeTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static AirplaneModeTile_Factory create(Provider<QSHost> provider, Provider<ActivityStarter> provider2, Provider<BroadcastDispatcher> provider3, Provider<KeyguardStateController> provider4) {
        return new AirplaneModeTile_Factory(provider, provider2, provider3, provider4);
    }
}
