package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ReadingModeTile_Factory implements Factory<ReadingModeTile> {
    private final Provider<QSHost> hostProvider;

    public ReadingModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public ReadingModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static ReadingModeTile provideInstance(Provider<QSHost> provider) {
        return new ReadingModeTile(provider.get());
    }

    public static ReadingModeTile_Factory create(Provider<QSHost> provider) {
        return new ReadingModeTile_Factory(provider);
    }
}
