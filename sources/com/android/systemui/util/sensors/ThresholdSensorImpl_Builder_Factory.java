package com.android.systemui.util.sensors;

import android.content.res.Resources;
import com.android.systemui.util.concurrency.Execution;
import com.android.systemui.util.sensors.ThresholdSensorImpl;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ThresholdSensorImpl_Builder_Factory implements Factory<ThresholdSensorImpl.Builder> {
    private final Provider<Execution> executionProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<AsyncSensorManager> sensorManagerProvider;

    public ThresholdSensorImpl_Builder_Factory(Provider<Resources> provider, Provider<AsyncSensorManager> provider2, Provider<Execution> provider3) {
        this.resourcesProvider = provider;
        this.sensorManagerProvider = provider2;
        this.executionProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ThresholdSensorImpl.Builder get() {
        return provideInstance(this.resourcesProvider, this.sensorManagerProvider, this.executionProvider);
    }

    public static ThresholdSensorImpl.Builder provideInstance(Provider<Resources> provider, Provider<AsyncSensorManager> provider2, Provider<Execution> provider3) {
        return new ThresholdSensorImpl.Builder(provider.get(), provider2.get(), provider3.get());
    }

    public static ThresholdSensorImpl_Builder_Factory create(Provider<Resources> provider, Provider<AsyncSensorManager> provider2, Provider<Execution> provider3) {
        return new ThresholdSensorImpl_Builder_Factory(provider, provider2, provider3);
    }
}
