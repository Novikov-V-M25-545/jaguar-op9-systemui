package com.android.systemui.dagger;

import android.content.Context;
import android.media.session.MediaSessionManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideMediaSessionManagerFactory implements Factory<MediaSessionManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideMediaSessionManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MediaSessionManager get() {
        return provideInstance(this.contextProvider);
    }

    public static MediaSessionManager provideInstance(Provider<Context> provider) {
        return proxyProvideMediaSessionManager(provider.get());
    }

    public static SystemServicesModule_ProvideMediaSessionManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideMediaSessionManagerFactory(provider);
    }

    public static MediaSessionManager proxyProvideMediaSessionManager(Context context) {
        return (MediaSessionManager) Preconditions.checkNotNull(SystemServicesModule.provideMediaSessionManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
