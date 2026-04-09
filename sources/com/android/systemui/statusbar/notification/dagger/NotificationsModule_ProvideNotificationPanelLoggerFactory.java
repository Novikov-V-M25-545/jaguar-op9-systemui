package com.android.systemui.statusbar.notification.dagger;

import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class NotificationsModule_ProvideNotificationPanelLoggerFactory implements Factory<NotificationPanelLogger> {
    private static final NotificationsModule_ProvideNotificationPanelLoggerFactory INSTANCE = new NotificationsModule_ProvideNotificationPanelLoggerFactory();

    @Override // javax.inject.Provider
    public NotificationPanelLogger get() {
        return provideInstance();
    }

    public static NotificationPanelLogger provideInstance() {
        return proxyProvideNotificationPanelLogger();
    }

    public static NotificationsModule_ProvideNotificationPanelLoggerFactory create() {
        return INSTANCE;
    }

    public static NotificationPanelLogger proxyProvideNotificationPanelLogger() {
        return (NotificationPanelLogger) Preconditions.checkNotNull(NotificationsModule.provideNotificationPanelLogger(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
