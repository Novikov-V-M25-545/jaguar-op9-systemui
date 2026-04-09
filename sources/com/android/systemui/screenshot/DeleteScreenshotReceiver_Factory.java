package com.android.systemui.screenshot;

import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class DeleteScreenshotReceiver_Factory implements Factory<DeleteScreenshotReceiver> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<ScreenshotSmartActions> screenshotSmartActionsProvider;

    public DeleteScreenshotReceiver_Factory(Provider<ScreenshotSmartActions> provider, Provider<Executor> provider2) {
        this.screenshotSmartActionsProvider = provider;
        this.backgroundExecutorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DeleteScreenshotReceiver get() {
        return provideInstance(this.screenshotSmartActionsProvider, this.backgroundExecutorProvider);
    }

    public static DeleteScreenshotReceiver provideInstance(Provider<ScreenshotSmartActions> provider, Provider<Executor> provider2) {
        return new DeleteScreenshotReceiver(provider.get(), provider2.get());
    }

    public static DeleteScreenshotReceiver_Factory create(Provider<ScreenshotSmartActions> provider, Provider<Executor> provider2) {
        return new DeleteScreenshotReceiver_Factory(provider, provider2);
    }
}
