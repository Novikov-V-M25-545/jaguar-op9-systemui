package com.android.systemui.dagger;

import android.content.Context;
import android.media.MediaRouter2Manager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideMediaRouter2ManagerFactory implements Factory<MediaRouter2Manager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideMediaRouter2ManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MediaRouter2Manager get() {
        return provideInstance(this.contextProvider);
    }

    public static MediaRouter2Manager provideInstance(Provider<Context> provider) {
        return proxyProvideMediaRouter2Manager(provider.get());
    }

    public static SystemServicesModule_ProvideMediaRouter2ManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideMediaRouter2ManagerFactory(provider);
    }

    public static MediaRouter2Manager proxyProvideMediaRouter2Manager(Context context) {
        return (MediaRouter2Manager) Preconditions.checkNotNull(SystemServicesModule.provideMediaRouter2Manager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
