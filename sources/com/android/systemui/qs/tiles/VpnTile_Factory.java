package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.SecurityController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class VpnTile_Factory implements Factory<VpnTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<SecurityController> securityControllerProvider;

    public VpnTile_Factory(Provider<QSHost> provider, Provider<SecurityController> provider2, Provider<KeyguardStateController> provider3, Provider<ActivityStarter> provider4) {
        this.hostProvider = provider;
        this.securityControllerProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
        this.activityStarterProvider = provider4;
    }

    @Override // javax.inject.Provider
    public VpnTile get() {
        return provideInstance(this.hostProvider, this.securityControllerProvider, this.keyguardStateControllerProvider, this.activityStarterProvider);
    }

    public static VpnTile provideInstance(Provider<QSHost> provider, Provider<SecurityController> provider2, Provider<KeyguardStateController> provider3, Provider<ActivityStarter> provider4) {
        return new VpnTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static VpnTile_Factory create(Provider<QSHost> provider, Provider<SecurityController> provider2, Provider<KeyguardStateController> provider3, Provider<ActivityStarter> provider4) {
        return new VpnTile_Factory(provider, provider2, provider3, provider4);
    }
}
