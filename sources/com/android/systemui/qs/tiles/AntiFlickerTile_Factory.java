package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AntiFlickerTile_Factory implements Factory<AntiFlickerTile> {
    private final Provider<QSHost> hostProvider;

    public AntiFlickerTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public AntiFlickerTile get() {
        return provideInstance(this.hostProvider);
    }

    public static AntiFlickerTile provideInstance(Provider<QSHost> provider) {
        return new AntiFlickerTile(provider.get());
    }

    public static AntiFlickerTile_Factory create(Provider<QSHost> provider) {
        return new AntiFlickerTile_Factory(provider);
    }
}
