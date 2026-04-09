package com.android.systemui.dagger;

import android.content.Context;
import android.view.WindowManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideWindowManagerFactory implements Factory<WindowManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideWindowManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public WindowManager get() {
        return provideInstance(this.contextProvider);
    }

    public static WindowManager provideInstance(Provider<Context> provider) {
        return proxyProvideWindowManager(provider.get());
    }

    public static SystemServicesModule_ProvideWindowManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideWindowManagerFactory(provider);
    }

    public static WindowManager proxyProvideWindowManager(Context context) {
        return (WindowManager) Preconditions.checkNotNull(SystemServicesModule.provideWindowManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
