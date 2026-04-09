package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class OnTheGoTile_Factory implements Factory<OnTheGoTile> {
    private final Provider<QSHost> hostProvider;

    public OnTheGoTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public OnTheGoTile get() {
        return provideInstance(this.hostProvider);
    }

    public static OnTheGoTile provideInstance(Provider<QSHost> provider) {
        return new OnTheGoTile(provider.get());
    }

    public static OnTheGoTile_Factory create(Provider<QSHost> provider) {
        return new OnTheGoTile_Factory(provider);
    }
}
