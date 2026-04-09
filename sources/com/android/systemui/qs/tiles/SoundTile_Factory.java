package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SoundTile_Factory implements Factory<SoundTile> {
    private final Provider<QSHost> hostProvider;

    public SoundTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public SoundTile get() {
        return provideInstance(this.hostProvider);
    }

    public static SoundTile provideInstance(Provider<QSHost> provider) {
        return new SoundTile(provider.get());
    }

    public static SoundTile_Factory create(Provider<QSHost> provider) {
        return new SoundTile_Factory(provider);
    }
}
