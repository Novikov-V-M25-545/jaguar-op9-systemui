package com.android.systemui.dagger;

import android.content.ContentResolver;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideContentResolverFactory implements Factory<ContentResolver> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideContentResolverFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ContentResolver get() {
        return provideInstance(this.contextProvider);
    }

    public static ContentResolver provideInstance(Provider<Context> provider) {
        return proxyProvideContentResolver(provider.get());
    }

    public static SystemServicesModule_ProvideContentResolverFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideContentResolverFactory(provider);
    }

    public static ContentResolver proxyProvideContentResolver(Context context) {
        return (ContentResolver) Preconditions.checkNotNull(SystemServicesModule.provideContentResolver(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
