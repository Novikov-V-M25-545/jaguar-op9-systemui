package com.android.systemui.dagger;

import com.android.internal.statusbar.IStatusBarService;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideIStatusBarServiceFactory implements Factory<IStatusBarService> {
    private static final SystemServicesModule_ProvideIStatusBarServiceFactory INSTANCE = new SystemServicesModule_ProvideIStatusBarServiceFactory();

    @Override // javax.inject.Provider
    public IStatusBarService get() {
        return provideInstance();
    }

    public static IStatusBarService provideInstance() {
        return proxyProvideIStatusBarService();
    }

    public static SystemServicesModule_ProvideIStatusBarServiceFactory create() {
        return INSTANCE;
    }

    public static IStatusBarService proxyProvideIStatusBarService() {
        return (IStatusBarService) Preconditions.checkNotNull(SystemServicesModule.provideIStatusBarService(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
