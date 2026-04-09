package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class LiveDisplayTile_Factory implements Factory<LiveDisplayTile> {
    private final Provider<QSHost> hostProvider;

    public LiveDisplayTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public LiveDisplayTile get() {
        return provideInstance(this.hostProvider);
    }

    public static LiveDisplayTile provideInstance(Provider<QSHost> provider) {
        return new LiveDisplayTile(provider.get());
    }

    public static LiveDisplayTile_Factory create(Provider<QSHost> provider) {
        return new LiveDisplayTile_Factory(provider);
    }
}
