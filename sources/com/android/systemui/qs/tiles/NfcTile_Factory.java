package com.android.systemui.qs.tiles;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class NfcTile_Factory implements Factory<NfcTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;

    public NfcTile_Factory(Provider<QSHost> provider, Provider<BroadcastDispatcher> provider2, Provider<ActivityStarter> provider3, Provider<KeyguardStateController> provider4) {
        this.hostProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.activityStarterProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public NfcTile get() {
        return provideInstance(this.hostProvider, this.broadcastDispatcherProvider, this.activityStarterProvider, this.keyguardStateControllerProvider);
    }

    public static NfcTile provideInstance(Provider<QSHost> provider, Provider<BroadcastDispatcher> provider2, Provider<ActivityStarter> provider3, Provider<KeyguardStateController> provider4) {
        return new NfcTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static NfcTile_Factory create(Provider<QSHost> provider, Provider<BroadcastDispatcher> provider2, Provider<ActivityStarter> provider3, Provider<KeyguardStateController> provider4) {
        return new NfcTile_Factory(provider, provider2, provider3, provider4);
    }
}
