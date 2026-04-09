package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class FPSInfoTile_Factory implements Factory<FPSInfoTile> {
    private final Provider<QSHost> hostProvider;

    public FPSInfoTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public FPSInfoTile get() {
        return provideInstance(this.hostProvider);
    }

    public static FPSInfoTile provideInstance(Provider<QSHost> provider) {
        return new FPSInfoTile(provider.get());
    }

    public static FPSInfoTile_Factory create(Provider<QSHost> provider) {
        return new FPSInfoTile_Factory(provider);
    }
}
