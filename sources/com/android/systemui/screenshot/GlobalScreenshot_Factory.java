package com.android.systemui.screenshot;

import android.content.Context;
import android.content.res.Resources;
import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class GlobalScreenshot_Factory implements Factory<GlobalScreenshot> {
    private final Provider<Context> contextProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<ScreenshotNotificationsController> screenshotNotificationsControllerProvider;
    private final Provider<ScreenshotSmartActions> screenshotSmartActionsProvider;
    private final Provider<Executor> uiBgExecutorProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public GlobalScreenshot_Factory(Provider<Context> provider, Provider<Resources> provider2, Provider<ScreenshotSmartActions> provider3, Provider<ScreenshotNotificationsController> provider4, Provider<UiEventLogger> provider5, Provider<Executor> provider6) {
        this.contextProvider = provider;
        this.resourcesProvider = provider2;
        this.screenshotSmartActionsProvider = provider3;
        this.screenshotNotificationsControllerProvider = provider4;
        this.uiEventLoggerProvider = provider5;
        this.uiBgExecutorProvider = provider6;
    }

    @Override // javax.inject.Provider
    public GlobalScreenshot get() {
        return provideInstance(this.contextProvider, this.resourcesProvider, this.screenshotSmartActionsProvider, this.screenshotNotificationsControllerProvider, this.uiEventLoggerProvider, this.uiBgExecutorProvider);
    }

    public static GlobalScreenshot provideInstance(Provider<Context> provider, Provider<Resources> provider2, Provider<ScreenshotSmartActions> provider3, Provider<ScreenshotNotificationsController> provider4, Provider<UiEventLogger> provider5, Provider<Executor> provider6) {
        return new GlobalScreenshot(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static GlobalScreenshot_Factory create(Provider<Context> provider, Provider<Resources> provider2, Provider<ScreenshotSmartActions> provider3, Provider<ScreenshotNotificationsController> provider4, Provider<UiEventLogger> provider5, Provider<Executor> provider6) {
        return new GlobalScreenshot_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
