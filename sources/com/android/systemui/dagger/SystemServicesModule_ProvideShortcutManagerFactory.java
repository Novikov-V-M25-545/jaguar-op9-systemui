package com.android.systemui.dagger;

import android.content.Context;
import android.content.pm.ShortcutManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideShortcutManagerFactory implements Factory<ShortcutManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideShortcutManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ShortcutManager get() {
        return provideInstance(this.contextProvider);
    }

    public static ShortcutManager provideInstance(Provider<Context> provider) {
        return proxyProvideShortcutManager(provider.get());
    }

    public static SystemServicesModule_ProvideShortcutManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideShortcutManagerFactory(provider);
    }

    public static ShortcutManager proxyProvideShortcutManager(Context context) {
        return (ShortcutManager) Preconditions.checkNotNull(SystemServicesModule.provideShortcutManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
