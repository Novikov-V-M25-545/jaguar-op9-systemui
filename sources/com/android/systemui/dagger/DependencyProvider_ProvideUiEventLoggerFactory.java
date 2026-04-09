package com.android.systemui.dagger;

import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class DependencyProvider_ProvideUiEventLoggerFactory implements Factory<UiEventLogger> {
    private static final DependencyProvider_ProvideUiEventLoggerFactory INSTANCE = new DependencyProvider_ProvideUiEventLoggerFactory();

    @Override // javax.inject.Provider
    public UiEventLogger get() {
        return provideInstance();
    }

    public static UiEventLogger provideInstance() {
        return proxyProvideUiEventLogger();
    }

    public static DependencyProvider_ProvideUiEventLoggerFactory create() {
        return INSTANCE;
    }

    public static UiEventLogger proxyProvideUiEventLogger() {
        return (UiEventLogger) Preconditions.checkNotNull(DependencyProvider.provideUiEventLogger(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
