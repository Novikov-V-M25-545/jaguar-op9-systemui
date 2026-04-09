package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class BluetoothTile_Factory implements Factory<BluetoothTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<BluetoothController> bluetoothControllerProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;

    public BluetoothTile_Factory(Provider<QSHost> provider, Provider<BluetoothController> provider2, Provider<ActivityStarter> provider3, Provider<KeyguardStateController> provider4) {
        this.hostProvider = provider;
        this.bluetoothControllerProvider = provider2;
        this.activityStarterProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public BluetoothTile get() {
        return provideInstance(this.hostProvider, this.bluetoothControllerProvider, this.activityStarterProvider, this.keyguardStateControllerProvider);
    }

    public static BluetoothTile provideInstance(Provider<QSHost> provider, Provider<BluetoothController> provider2, Provider<ActivityStarter> provider3, Provider<KeyguardStateController> provider4) {
        return new BluetoothTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static BluetoothTile_Factory create(Provider<QSHost> provider, Provider<BluetoothController> provider2, Provider<ActivityStarter> provider3, Provider<KeyguardStateController> provider4) {
        return new BluetoothTile_Factory(provider, provider2, provider3, provider4);
    }
}
