package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ConcurrencyModule_ProvideBackgroundExecutorFactory implements Factory<Executor> {
    private final Provider<Looper> looperProvider;

    public ConcurrencyModule_ProvideBackgroundExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public Executor get() {
        return provideInstance(this.looperProvider);
    }

    public static Executor provideInstance(Provider<Looper> provider) {
        return proxyProvideBackgroundExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideBackgroundExecutorFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideBackgroundExecutorFactory(provider);
    }

    public static Executor proxyProvideBackgroundExecutor(Looper looper) {
        return (Executor) Preconditions.checkNotNull(ConcurrencyModule.provideBackgroundExecutor(looper), "Cannot return null from a non-@Nullable @Provides method");
    }
}
