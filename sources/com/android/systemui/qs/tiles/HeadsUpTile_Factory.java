package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class HeadsUpTile_Factory implements Factory<HeadsUpTile> {
    private final Provider<QSHost> hostProvider;

    public HeadsUpTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public HeadsUpTile get() {
        return provideInstance(this.hostProvider);
    }

    public static HeadsUpTile provideInstance(Provider<QSHost> provider) {
        return new HeadsUpTile(provider.get());
    }

    public static HeadsUpTile_Factory create(Provider<QSHost> provider) {
        return new HeadsUpTile_Factory(provider);
    }
}
