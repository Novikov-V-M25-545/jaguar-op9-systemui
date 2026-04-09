package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class LocaleTile_Factory implements Factory<LocaleTile> {
    private final Provider<QSHost> hostProvider;

    public LocaleTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public LocaleTile get() {
        return provideInstance(this.hostProvider);
    }

    public static LocaleTile provideInstance(Provider<QSHost> provider) {
        return new LocaleTile(provider.get());
    }

    public static LocaleTile_Factory create(Provider<QSHost> provider) {
        return new LocaleTile_Factory(provider);
    }
}
