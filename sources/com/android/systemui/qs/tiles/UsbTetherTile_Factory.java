package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class UsbTetherTile_Factory implements Factory<UsbTetherTile> {
    private final Provider<QSHost> hostProvider;

    public UsbTetherTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public UsbTetherTile get() {
        return provideInstance(this.hostProvider);
    }

    public static UsbTetherTile provideInstance(Provider<QSHost> provider) {
        return new UsbTetherTile(provider.get());
    }

    public static UsbTetherTile_Factory create(Provider<QSHost> provider) {
        return new UsbTetherTile_Factory(provider);
    }
}
