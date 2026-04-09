package com.android.systemui.util.concurrency;

import android.os.Handler;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class ConcurrencyModule_ProvideHandlerFactory implements Factory<Handler> {
    private static final ConcurrencyModule_ProvideHandlerFactory INSTANCE = new ConcurrencyModule_ProvideHandlerFactory();

    @Override // javax.inject.Provider
    public Handler get() {
        return provideInstance();
    }

    public static Handler provideInstance() {
        return proxyProvideHandler();
    }

    public static ConcurrencyModule_ProvideHandlerFactory create() {
        return INSTANCE;
    }

    public static Handler proxyProvideHandler() {
        return (Handler) Preconditions.checkNotNull(ConcurrencyModule.provideHandler(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
