package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SleepModeTile_Factory implements Factory<SleepModeTile> {
    private final Provider<QSHost> hostProvider;

    public SleepModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public SleepModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static SleepModeTile provideInstance(Provider<QSHost> provider) {
        return new SleepModeTile(provider.get());
    }

    public static SleepModeTile_Factory create(Provider<QSHost> provider) {
        return new SleepModeTile_Factory(provider);
    }
}
