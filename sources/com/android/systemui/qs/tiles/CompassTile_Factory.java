package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class CompassTile_Factory implements Factory<CompassTile> {
    private final Provider<QSHost> hostProvider;

    public CompassTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public CompassTile get() {
        return provideInstance(this.hostProvider);
    }

    public static CompassTile provideInstance(Provider<QSHost> provider) {
        return new CompassTile(provider.get());
    }

    public static CompassTile_Factory create(Provider<QSHost> provider) {
        return new CompassTile_Factory(provider);
    }
}
