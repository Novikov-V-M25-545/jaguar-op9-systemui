package com.android.systemui.dagger;

import android.content.Context;
import android.content.res.Resources;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideResourcesFactory implements Factory<Resources> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideResourcesFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public Resources get() {
        return provideInstance(this.contextProvider);
    }

    public static Resources provideInstance(Provider<Context> provider) {
        return proxyProvideResources(provider.get());
    }

    public static SystemServicesModule_ProvideResourcesFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideResourcesFactory(provider);
    }

    public static Resources proxyProvideResources(Context context) {
        return (Resources) Preconditions.checkNotNull(SystemServicesModule.provideResources(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
