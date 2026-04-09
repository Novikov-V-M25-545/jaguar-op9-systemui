package com.android.systemui.screenrecord;

import android.app.NotificationManager;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class RecordingService_Factory implements Factory<RecordingService> {
    private final Provider<RecordingController> controllerProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<KeyguardDismissUtil> keyguardDismissUtilProvider;
    private final Provider<NotificationManager> notificationManagerProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;
    private final Provider<CurrentUserContextTracker> userContextTrackerProvider;

    public RecordingService_Factory(Provider<RecordingController> provider, Provider<Executor> provider2, Provider<UiEventLogger> provider3, Provider<NotificationManager> provider4, Provider<CurrentUserContextTracker> provider5, Provider<KeyguardDismissUtil> provider6) {
        this.controllerProvider = provider;
        this.executorProvider = provider2;
        this.uiEventLoggerProvider = provider3;
        this.notificationManagerProvider = provider4;
        this.userContextTrackerProvider = provider5;
        this.keyguardDismissUtilProvider = provider6;
    }

    @Override // javax.inject.Provider
    public RecordingService get() {
        return provideInstance(this.controllerProvider, this.executorProvider, this.uiEventLoggerProvider, this.notificationManagerProvider, this.userContextTrackerProvider, this.keyguardDismissUtilProvider);
    }

    public static RecordingService provideInstance(Provider<RecordingController> provider, Provider<Executor> provider2, Provider<UiEventLogger> provider3, Provider<NotificationManager> provider4, Provider<CurrentUserContextTracker> provider5, Provider<KeyguardDismissUtil> provider6) {
        return new RecordingService(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static RecordingService_Factory create(Provider<RecordingController> provider, Provider<Executor> provider2, Provider<UiEventLogger> provider3, Provider<NotificationManager> provider4, Provider<CurrentUserContextTracker> provider5, Provider<KeyguardDismissUtil> provider6) {
        return new RecordingService_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
