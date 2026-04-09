package com.android.systemui.dagger;

import android.content.Context;
import android.net.NetworkScoreManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideNetworkScoreManagerFactory implements Factory<NetworkScoreManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideNetworkScoreManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public NetworkScoreManager get() {
        return provideInstance(this.contextProvider);
    }

    public static NetworkScoreManager provideInstance(Provider<Context> provider) {
        return proxyProvideNetworkScoreManager(provider.get());
    }

    public static SystemServicesModule_ProvideNetworkScoreManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideNetworkScoreManagerFactory(provider);
    }

    public static NetworkScoreManager proxyProvideNetworkScoreManager(Context context) {
        return (NetworkScoreManager) Preconditions.checkNotNull(SystemServicesModule.provideNetworkScoreManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
