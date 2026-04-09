package com.android.systemui.util.sensors;

import com.android.systemui.util.sensors.ThresholdSensorImpl;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SensorModule_ProvideSecondaryProxSensorFactory implements Factory<ThresholdSensor> {
    private final Provider<ThresholdSensorImpl.Builder> thresholdSensorBuilderProvider;

    public SensorModule_ProvideSecondaryProxSensorFactory(Provider<ThresholdSensorImpl.Builder> provider) {
        this.thresholdSensorBuilderProvider = provider;
    }

    @Override // javax.inject.Provider
    public ThresholdSensor get() {
        return provideInstance(this.thresholdSensorBuilderProvider);
    }

    public static ThresholdSensor provideInstance(Provider<ThresholdSensorImpl.Builder> provider) {
        return proxyProvideSecondaryProxSensor(provider.get());
    }

    public static SensorModule_ProvideSecondaryProxSensorFactory create(Provider<ThresholdSensorImpl.Builder> provider) {
        return new SensorModule_ProvideSecondaryProxSensorFactory(provider);
    }

    public static ThresholdSensor proxyProvideSecondaryProxSensor(Object obj) {
        return (ThresholdSensor) Preconditions.checkNotNull(SensorModule.provideSecondaryProxSensor((ThresholdSensorImpl.Builder) obj), "Cannot return null from a non-@Nullable @Provides method");
    }
}
