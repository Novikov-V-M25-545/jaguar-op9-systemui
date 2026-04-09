package com.android.systemui.dagger;

import android.app.AppLockManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideAppLockManagerFactory implements Factory<AppLockManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideAppLockManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AppLockManager get() {
        return provideInstance(this.contextProvider);
    }

    public static AppLockManager provideInstance(Provider<Context> provider) {
        return proxyProvideAppLockManager(provider.get());
    }

    public static SystemServicesModule_ProvideAppLockManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideAppLockManagerFactory(provider);
    }

    public static AppLockManager proxyProvideAppLockManager(Context context) {
        return (AppLockManager) Preconditions.checkNotNull(SystemServicesModule.provideAppLockManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
