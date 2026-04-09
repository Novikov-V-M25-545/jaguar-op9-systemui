package com.android.systemui.util.sensors;

import android.hardware.SensorManager;
import com.android.systemui.shared.plugins.PluginManager;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AsyncSensorManager_Factory implements Factory<AsyncSensorManager> {
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<SensorManager> sensorManagerProvider;

    public AsyncSensorManager_Factory(Provider<SensorManager> provider, Provider<PluginManager> provider2) {
        this.sensorManagerProvider = provider;
        this.pluginManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public AsyncSensorManager get() {
        return provideInstance(this.sensorManagerProvider, this.pluginManagerProvider);
    }

    public static AsyncSensorManager provideInstance(Provider<SensorManager> provider, Provider<PluginManager> provider2) {
        return new AsyncSensorManager(provider.get(), provider2.get());
    }

    public static AsyncSensorManager_Factory create(Provider<SensorManager> provider, Provider<PluginManager> provider2) {
        return new AsyncSensorManager_Factory(provider, provider2);
    }
}
