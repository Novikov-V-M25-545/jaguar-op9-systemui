package com.android.systemui.appops;

import android.content.pm.PackageManager;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class PermissionFlagsCache_Factory implements Factory<PermissionFlagsCache> {
    private final Provider<Executor> executorProvider;
    private final Provider<PackageManager> packageManagerProvider;

    public PermissionFlagsCache_Factory(Provider<PackageManager> provider, Provider<Executor> provider2) {
        this.packageManagerProvider = provider;
        this.executorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PermissionFlagsCache get() {
        return provideInstance(this.packageManagerProvider, this.executorProvider);
    }

    public static PermissionFlagsCache provideInstance(Provider<PackageManager> provider, Provider<Executor> provider2) {
        return new PermissionFlagsCache(provider.get(), provider2.get());
    }

    public static PermissionFlagsCache_Factory create(Provider<PackageManager> provider, Provider<Executor> provider2) {
        return new PermissionFlagsCache_Factory(provider, provider2);
    }
}
