package com.android.systemui.dagger;

import android.content.Context;
import android.hardware.SensorPrivacyManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideSensorPrivacyManagerFactory implements Factory<SensorPrivacyManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideSensorPrivacyManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public SensorPrivacyManager get() {
        return provideInstance(this.contextProvider);
    }

    public static SensorPrivacyManager provideInstance(Provider<Context> provider) {
        return proxyProvideSensorPrivacyManager(provider.get());
    }

    public static SystemServicesModule_ProvideSensorPrivacyManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideSensorPrivacyManagerFactory(provider);
    }

    public static SensorPrivacyManager proxyProvideSensorPrivacyManager(Context context) {
        return (SensorPrivacyManager) Preconditions.checkNotNull(SystemServicesModule.provideSensorPrivacyManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
