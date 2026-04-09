package com.android.systemui.dagger;

import android.content.Context;
import android.os.UserManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideUserManagerFactory implements Factory<UserManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideUserManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public UserManager get() {
        return provideInstance(this.contextProvider);
    }

    public static UserManager provideInstance(Provider<Context> provider) {
        return proxyProvideUserManager(provider.get());
    }

    public static SystemServicesModule_ProvideUserManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideUserManagerFactory(provider);
    }

    public static UserManager proxyProvideUserManager(Context context) {
        return (UserManager) Preconditions.checkNotNull(SystemServicesModule.provideUserManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
