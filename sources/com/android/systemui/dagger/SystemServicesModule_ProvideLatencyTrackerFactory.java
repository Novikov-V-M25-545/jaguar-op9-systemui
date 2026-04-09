package com.android.systemui.dagger;

import android.content.Context;
import com.android.internal.util.LatencyTracker;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SystemServicesModule_ProvideLatencyTrackerFactory implements Factory<LatencyTracker> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideLatencyTrackerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public LatencyTracker get() {
        return provideInstance(this.contextProvider);
    }

    public static LatencyTracker provideInstance(Provider<Context> provider) {
        return proxyProvideLatencyTracker(provider.get());
    }

    public static SystemServicesModule_ProvideLatencyTrackerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideLatencyTrackerFactory(provider);
    }

    public static LatencyTracker proxyProvideLatencyTracker(Context context) {
        return (LatencyTracker) Preconditions.checkNotNull(SystemServicesModule.provideLatencyTracker(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
