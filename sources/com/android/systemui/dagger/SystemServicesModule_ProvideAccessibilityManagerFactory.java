package com.android.systemui.dagger;

import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideAccessibilityManagerFactory implements Factory<AccessibilityManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideAccessibilityManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AccessibilityManager get() {
        return provideInstance(this.contextProvider);
    }

    public static AccessibilityManager provideInstance(Provider<Context> provider) {
        return proxyProvideAccessibilityManager(provider.get());
    }

    public static SystemServicesModule_ProvideAccessibilityManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideAccessibilityManagerFactory(provider);
    }

    public static AccessibilityManager proxyProvideAccessibilityManager(Context context) {
        return (AccessibilityManager) Preconditions.checkNotNull(SystemServicesModule.provideAccessibilityManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
