package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class CaffeineTile_Factory implements Factory<CaffeineTile> {
    private final Provider<QSHost> hostProvider;

    public CaffeineTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public CaffeineTile get() {
        return provideInstance(this.hostProvider);
    }

    public static CaffeineTile provideInstance(Provider<QSHost> provider) {
        return new CaffeineTile(provider.get());
    }

    public static CaffeineTile_Factory create(Provider<QSHost> provider) {
        return new CaffeineTile_Factory(provider);
    }
}
