package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ProfilesTile_Factory implements Factory<ProfilesTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;

    public ProfilesTile_Factory(Provider<QSHost> provider, Provider<ActivityStarter> provider2, Provider<KeyguardStateController> provider3) {
        this.hostProvider = provider;
        this.activityStarterProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ProfilesTile get() {
        return provideInstance(this.hostProvider, this.activityStarterProvider, this.keyguardStateControllerProvider);
    }

    public static ProfilesTile provideInstance(Provider<QSHost> provider, Provider<ActivityStarter> provider2, Provider<KeyguardStateController> provider3) {
        return new ProfilesTile(provider.get(), provider2.get(), provider3.get());
    }

    public static ProfilesTile_Factory create(Provider<QSHost> provider, Provider<ActivityStarter> provider2, Provider<KeyguardStateController> provider3) {
        return new ProfilesTile_Factory(provider, provider2, provider3);
    }
}
