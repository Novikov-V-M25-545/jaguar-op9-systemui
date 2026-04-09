package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class MediaDataManager_Factory implements Factory<MediaDataManager> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<DelayableExecutor> foregroundExecutorProvider;
    private final Provider<MediaControllerFactory> mediaControllerFactoryProvider;
    private final Provider<MediaDataCombineLatest> mediaDataCombineLatestProvider;
    private final Provider<MediaDataFilter> mediaDataFilterProvider;
    private final Provider<MediaDeviceManager> mediaDeviceManagerProvider;
    private final Provider<MediaResumeListener> mediaResumeListenerProvider;
    private final Provider<MediaSessionBasedFilter> mediaSessionBasedFilterProvider;
    private final Provider<MediaTimeoutListener> mediaTimeoutListenerProvider;

    public MediaDataManager_Factory(Provider<Context> provider, Provider<Executor> provider2, Provider<DelayableExecutor> provider3, Provider<MediaControllerFactory> provider4, Provider<DumpManager> provider5, Provider<BroadcastDispatcher> provider6, Provider<MediaTimeoutListener> provider7, Provider<MediaResumeListener> provider8, Provider<MediaSessionBasedFilter> provider9, Provider<MediaDeviceManager> provider10, Provider<MediaDataCombineLatest> provider11, Provider<MediaDataFilter> provider12) {
        this.contextProvider = provider;
        this.backgroundExecutorProvider = provider2;
        this.foregroundExecutorProvider = provider3;
        this.mediaControllerFactoryProvider = provider4;
        this.dumpManagerProvider = provider5;
        this.broadcastDispatcherProvider = provider6;
        this.mediaTimeoutListenerProvider = provider7;
        this.mediaResumeListenerProvider = provider8;
        this.mediaSessionBasedFilterProvider = provider9;
        this.mediaDeviceManagerProvider = provider10;
        this.mediaDataCombineLatestProvider = provider11;
        this.mediaDataFilterProvider = provider12;
    }

    @Override // javax.inject.Provider
    public MediaDataManager get() {
        return provideInstance(this.contextProvider, this.backgroundExecutorProvider, this.foregroundExecutorProvider, this.mediaControllerFactoryProvider, this.dumpManagerProvider, this.broadcastDispatcherProvider, this.mediaTimeoutListenerProvider, this.mediaResumeListenerProvider, this.mediaSessionBasedFilterProvider, this.mediaDeviceManagerProvider, this.mediaDataCombineLatestProvider, this.mediaDataFilterProvider);
    }

    public static MediaDataManager provideInstance(Provider<Context> provider, Provider<Executor> provider2, Provider<DelayableExecutor> provider3, Provider<MediaControllerFactory> provider4, Provider<DumpManager> provider5, Provider<BroadcastDispatcher> provider6, Provider<MediaTimeoutListener> provider7, Provider<MediaResumeListener> provider8, Provider<MediaSessionBasedFilter> provider9, Provider<MediaDeviceManager> provider10, Provider<MediaDataCombineLatest> provider11, Provider<MediaDataFilter> provider12) {
        return new MediaDataManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get());
    }

    public static MediaDataManager_Factory create(Provider<Context> provider, Provider<Executor> provider2, Provider<DelayableExecutor> provider3, Provider<MediaControllerFactory> provider4, Provider<DumpManager> provider5, Provider<BroadcastDispatcher> provider6, Provider<MediaTimeoutListener> provider7, Provider<MediaResumeListener> provider8, Provider<MediaSessionBasedFilter> provider9, Provider<MediaDeviceManager> provider10, Provider<MediaDataCombineLatest> provider11, Provider<MediaDataFilter> provider12) {
        return new MediaDataManager_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12);
    }
}
