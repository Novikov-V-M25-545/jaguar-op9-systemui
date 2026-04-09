package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class GamingModeTile_Factory implements Factory<GamingModeTile> {
    private final Provider<QSHost> hostProvider;

    public GamingModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public GamingModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static GamingModeTile provideInstance(Provider<QSHost> provider) {
        return new GamingModeTile(provider.get());
    }

    public static GamingModeTile_Factory create(Provider<QSHost> provider) {
        return new GamingModeTile_Factory(provider);
    }
}
