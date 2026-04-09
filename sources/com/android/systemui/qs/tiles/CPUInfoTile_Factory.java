package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class CPUInfoTile_Factory implements Factory<CPUInfoTile> {
    private final Provider<QSHost> hostProvider;

    public CPUInfoTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public CPUInfoTile get() {
        return provideInstance(this.hostProvider);
    }

    public static CPUInfoTile provideInstance(Provider<QSHost> provider) {
        return new CPUInfoTile(provider.get());
    }

    public static CPUInfoTile_Factory create(Provider<QSHost> provider) {
        return new CPUInfoTile_Factory(provider);
    }
}
