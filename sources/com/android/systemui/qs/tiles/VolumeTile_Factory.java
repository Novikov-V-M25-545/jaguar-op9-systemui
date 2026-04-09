package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class VolumeTile_Factory implements Factory<VolumeTile> {
    private final Provider<QSHost> hostProvider;

    public VolumeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public VolumeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static VolumeTile provideInstance(Provider<QSHost> provider) {
        return new VolumeTile(provider.get());
    }

    public static VolumeTile_Factory create(Provider<QSHost> provider) {
        return new VolumeTile_Factory(provider);
    }
}
