package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class RebootTile_Factory implements Factory<RebootTile> {
    private final Provider<QSHost> hostProvider;

    public RebootTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public RebootTile get() {
        return provideInstance(this.hostProvider);
    }

    public static RebootTile provideInstance(Provider<QSHost> provider) {
        return new RebootTile(provider.get());
    }

    public static RebootTile_Factory create(Provider<QSHost> provider) {
        return new RebootTile_Factory(provider);
    }
}
