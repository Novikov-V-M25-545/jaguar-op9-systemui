package com.android.systemui.dagger;

import com.android.systemui.shared.system.PackageManagerWrapper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvidePackageManagerWrapperFactory implements Factory<PackageManagerWrapper> {
    private static final SystemServicesModule_ProvidePackageManagerWrapperFactory INSTANCE = new SystemServicesModule_ProvidePackageManagerWrapperFactory();

    @Override // javax.inject.Provider
    public PackageManagerWrapper get() {
        return provideInstance();
    }

    public static PackageManagerWrapper provideInstance() {
        return proxyProvidePackageManagerWrapper();
    }

    public static SystemServicesModule_ProvidePackageManagerWrapperFactory create() {
        return INSTANCE;
    }

    public static PackageManagerWrapper proxyProvidePackageManagerWrapper() {
        return (PackageManagerWrapper) Preconditions.checkNotNull(SystemServicesModule.providePackageManagerWrapper(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
