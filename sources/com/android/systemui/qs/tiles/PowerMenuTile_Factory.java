package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class PowerMenuTile_Factory implements Factory<PowerMenuTile> {
    private final Provider<QSHost> hostProvider;

    public PowerMenuTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public PowerMenuTile get() {
        return provideInstance(this.hostProvider);
    }

    public static PowerMenuTile provideInstance(Provider<QSHost> provider) {
        return new PowerMenuTile(provider.get());
    }

    public static PowerMenuTile_Factory create(Provider<QSHost> provider) {
        return new PowerMenuTile_Factory(provider);
    }
}
