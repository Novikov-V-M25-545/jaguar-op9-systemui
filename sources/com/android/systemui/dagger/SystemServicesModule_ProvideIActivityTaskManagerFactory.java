package com.android.systemui.dagger;

import android.app.IActivityTaskManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideIActivityTaskManagerFactory implements Factory<IActivityTaskManager> {
    private static final SystemServicesModule_ProvideIActivityTaskManagerFactory INSTANCE = new SystemServicesModule_ProvideIActivityTaskManagerFactory();

    @Override // javax.inject.Provider
    public IActivityTaskManager get() {
        return provideInstance();
    }

    public static IActivityTaskManager provideInstance() {
        return proxyProvideIActivityTaskManager();
    }

    public static SystemServicesModule_ProvideIActivityTaskManagerFactory create() {
        return INSTANCE;
    }

    public static IActivityTaskManager proxyProvideIActivityTaskManager() {
        return (IActivityTaskManager) Preconditions.checkNotNull(SystemServicesModule.provideIActivityTaskManager(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
