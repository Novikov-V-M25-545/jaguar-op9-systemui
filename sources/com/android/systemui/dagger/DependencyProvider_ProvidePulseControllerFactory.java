package com.android.systemui.dagger;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.statusbar.policy.PulseController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class DependencyProvider_ProvidePulseControllerFactory implements Factory<PulseController> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvidePulseControllerFactory(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2, Provider<Executor> provider3) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.backgroundExecutorProvider = provider3;
    }

    @Override // javax.inject.Provider
    public PulseController get() {
        return provideInstance(this.module, this.contextProvider, this.mainHandlerProvider, this.backgroundExecutorProvider);
    }

    public static PulseController provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2, Provider<Executor> provider3) {
        return proxyProvidePulseController(dependencyProvider, provider.get(), provider2.get(), provider3.get());
    }

    public static DependencyProvider_ProvidePulseControllerFactory create(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2, Provider<Executor> provider3) {
        return new DependencyProvider_ProvidePulseControllerFactory(dependencyProvider, provider, provider2, provider3);
    }

    public static PulseController proxyProvidePulseController(DependencyProvider dependencyProvider, Context context, Handler handler, Executor executor) {
        return (PulseController) Preconditions.checkNotNull(dependencyProvider.providePulseController(context, handler, executor), "Cannot return null from a non-@Nullable @Provides method");
    }
}
