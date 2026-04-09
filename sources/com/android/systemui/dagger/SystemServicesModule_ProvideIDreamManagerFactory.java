package com.android.systemui.dagger;

import android.service.dreams.IDreamManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideIDreamManagerFactory implements Factory<IDreamManager> {
    private static final SystemServicesModule_ProvideIDreamManagerFactory INSTANCE = new SystemServicesModule_ProvideIDreamManagerFactory();

    @Override // javax.inject.Provider
    public IDreamManager get() {
        return provideInstance();
    }

    public static IDreamManager provideInstance() {
        return proxyProvideIDreamManager();
    }

    public static SystemServicesModule_ProvideIDreamManagerFactory create() {
        return INSTANCE;
    }

    public static IDreamManager proxyProvideIDreamManager() {
        return (IDreamManager) Preconditions.checkNotNull(SystemServicesModule.provideIDreamManager(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
