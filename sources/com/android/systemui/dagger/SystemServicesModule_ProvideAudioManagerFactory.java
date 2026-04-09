package com.android.systemui.dagger;

import android.content.Context;
import android.media.AudioManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideAudioManagerFactory implements Factory<AudioManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideAudioManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AudioManager get() {
        return provideInstance(this.contextProvider);
    }

    public static AudioManager provideInstance(Provider<Context> provider) {
        return proxyProvideAudioManager(provider.get());
    }

    public static SystemServicesModule_ProvideAudioManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideAudioManagerFactory(provider);
    }

    public static AudioManager proxyProvideAudioManager(Context context) {
        return (AudioManager) Preconditions.checkNotNull(SystemServicesModule.provideAudioManager(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
