package com.android.systemui.util.sensors;

import android.hardware.SensorManager;
import com.android.systemui.util.sensors.ThresholdSensorImpl;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SensorModule_ProvidePrimaryProxSensorFactory implements Factory<ThresholdSensor> {
    private final Provider<SensorManager> sensorManagerProvider;
    private final Provider<ThresholdSensorImpl.Builder> thresholdSensorBuilderProvider;

    public SensorModule_ProvidePrimaryProxSensorFactory(Provider<SensorManager> provider, Provider<ThresholdSensorImpl.Builder> provider2) {
        this.sensorManagerProvider = provider;
        this.thresholdSensorBuilderProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ThresholdSensor get() {
        return provideInstance(this.sensorManagerProvider, this.thresholdSensorBuilderProvider);
    }

    public static ThresholdSensor provideInstance(Provider<SensorManager> provider, Provider<ThresholdSensorImpl.Builder> provider2) {
        return proxyProvidePrimaryProxSensor(provider.get(), provider2.get());
    }

    public static SensorModule_ProvidePrimaryProxSensorFactory create(Provider<SensorManager> provider, Provider<ThresholdSensorImpl.Builder> provider2) {
        return new SensorModule_ProvidePrimaryProxSensorFactory(provider, provider2);
    }

    public static ThresholdSensor proxyProvidePrimaryProxSensor(SensorManager sensorManager, Object obj) {
        return (ThresholdSensor) Preconditions.checkNotNull(SensorModule.providePrimaryProxSensor(sensorManager, (ThresholdSensorImpl.Builder) obj), "Cannot return null from a non-@Nullable @Provides method");
    }
}
