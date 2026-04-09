package com.android.systemui.dagger;

import android.content.Context;
import android.hardware.SensorManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvidesSensorManagerFactory implements Factory<SensorManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvidesSensorManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public SensorManager get() {
        return provideInstance(this.contextProvider);
    }

    public static SensorManager provideInstance(Provider<Context> provider) {
        return proxyProvidesSensorManager(provider.get());
    }

    public static SystemServicesModule_ProvidesSensorManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvidesSensorManagerFactory(provider);
    }

    public static SensorManager proxyProvidesSensorManager(Context context) {
        return (SensorManager) Preconditions.checkNotNull(SystemServicesModule.providesSensorManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
