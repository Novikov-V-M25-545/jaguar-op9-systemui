package com.android.systemui.util.concurrency;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory implements Factory<RepeatableExecutor> {
    private final Provider<DelayableExecutor> execProvider;

    public ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory(Provider<DelayableExecutor> provider) {
        this.execProvider = provider;
    }

    @Override // javax.inject.Provider
    public RepeatableExecutor get() {
        return provideInstance(this.execProvider);
    }

    public static RepeatableExecutor provideInstance(Provider<DelayableExecutor> provider) {
        return proxyProvideBackgroundRepeatableExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory create(Provider<DelayableExecutor> provider) {
        return new ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory(provider);
    }

    public static RepeatableExecutor proxyProvideBackgroundRepeatableExecutor(DelayableExecutor delayableExecutor) {
        return (RepeatableExecutor) Preconditions.checkNotNull(ConcurrencyModule.provideBackgroundRepeatableExecutor(delayableExecutor), "Cannot return null from a non-@Nullable @Provides method");
    }
}
