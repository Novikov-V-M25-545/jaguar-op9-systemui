package com.android.systemui.dagger;

import android.view.IWindowManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideIWindowManagerFactory implements Factory<IWindowManager> {
    private static final SystemServicesModule_ProvideIWindowManagerFactory INSTANCE = new SystemServicesModule_ProvideIWindowManagerFactory();

    @Override // javax.inject.Provider
    public IWindowManager get() {
        return provideInstance();
    }

    public static IWindowManager provideInstance() {
        return proxyProvideIWindowManager();
    }

    public static SystemServicesModule_ProvideIWindowManagerFactory create() {
        return INSTANCE;
    }

    public static IWindowManager proxyProvideIWindowManager() {
        return (IWindowManager) Preconditions.checkNotNull(SystemServicesModule.provideIWindowManager(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
