package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AmbientDisplayTile_Factory implements Factory<AmbientDisplayTile> {
    private final Provider<QSHost> hostProvider;

    public AmbientDisplayTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public AmbientDisplayTile get() {
        return provideInstance(this.hostProvider);
    }

    public static AmbientDisplayTile provideInstance(Provider<QSHost> provider) {
        return new AmbientDisplayTile(provider.get());
    }

    public static AmbientDisplayTile_Factory create(Provider<QSHost> provider) {
        return new AmbientDisplayTile_Factory(provider);
    }
}
