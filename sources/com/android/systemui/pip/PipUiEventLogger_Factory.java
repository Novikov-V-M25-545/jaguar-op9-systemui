package com.android.systemui.pip;

import android.content.pm.PackageManager;
import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class PipUiEventLogger_Factory implements Factory<PipUiEventLogger> {
    private final Provider<PackageManager> packageManagerProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public PipUiEventLogger_Factory(Provider<UiEventLogger> provider, Provider<PackageManager> provider2) {
        this.uiEventLoggerProvider = provider;
        this.packageManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PipUiEventLogger get() {
        return provideInstance(this.uiEventLoggerProvider, this.packageManagerProvider);
    }

    public static PipUiEventLogger provideInstance(Provider<UiEventLogger> provider, Provider<PackageManager> provider2) {
        return new PipUiEventLogger(provider.get(), provider2.get());
    }

    public static PipUiEventLogger_Factory create(Provider<UiEventLogger> provider, Provider<PackageManager> provider2) {
        return new PipUiEventLogger_Factory(provider, provider2);
    }
}
