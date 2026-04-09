package com.android.systemui.media;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class MediaDataFilter_Factory implements Factory<MediaDataFilter> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<NotificationLockscreenUserManager> lockscreenUserManagerProvider;
    private final Provider<MediaResumeListener> mediaResumeListenerProvider;

    public MediaDataFilter_Factory(Provider<BroadcastDispatcher> provider, Provider<MediaResumeListener> provider2, Provider<NotificationLockscreenUserManager> provider3, Provider<Executor> provider4) {
        this.broadcastDispatcherProvider = provider;
        this.mediaResumeListenerProvider = provider2;
        this.lockscreenUserManagerProvider = provider3;
        this.executorProvider = provider4;
    }

    @Override // javax.inject.Provider
    public MediaDataFilter get() {
        return provideInstance(this.broadcastDispatcherProvider, this.mediaResumeListenerProvider, this.lockscreenUserManagerProvider, this.executorProvider);
    }

    public static MediaDataFilter provideInstance(Provider<BroadcastDispatcher> provider, Provider<MediaResumeListener> provider2, Provider<NotificationLockscreenUserManager> provider3, Provider<Executor> provider4) {
        return new MediaDataFilter(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static MediaDataFilter_Factory create(Provider<BroadcastDispatcher> provider, Provider<MediaResumeListener> provider2, Provider<NotificationLockscreenUserManager> provider3, Provider<Executor> provider4) {
        return new MediaDataFilter_Factory(provider, provider2, provider3, provider4);
    }
}
