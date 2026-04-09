package com.android.systemui.media.dialog;

import android.content.Context;
import android.media.MediaRouter2Manager;
import android.media.session.MediaSessionManager;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.ShadeController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class MediaOutputDialogFactory_Factory implements Factory<MediaOutputDialogFactory> {
    private final Provider<Context> contextProvider;
    private final Provider<LocalBluetoothManager> lbmProvider;
    private final Provider<MediaSessionManager> mediaSessionManagerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<MediaRouter2Manager> routerManagerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<ActivityStarter> starterProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public MediaOutputDialogFactory_Factory(Provider<Context> provider, Provider<MediaSessionManager> provider2, Provider<LocalBluetoothManager> provider3, Provider<ShadeController> provider4, Provider<ActivityStarter> provider5, Provider<NotificationEntryManager> provider6, Provider<UiEventLogger> provider7, Provider<MediaRouter2Manager> provider8) {
        this.contextProvider = provider;
        this.mediaSessionManagerProvider = provider2;
        this.lbmProvider = provider3;
        this.shadeControllerProvider = provider4;
        this.starterProvider = provider5;
        this.notificationEntryManagerProvider = provider6;
        this.uiEventLoggerProvider = provider7;
        this.routerManagerProvider = provider8;
    }

    @Override // javax.inject.Provider
    public MediaOutputDialogFactory get() {
        return provideInstance(this.contextProvider, this.mediaSessionManagerProvider, this.lbmProvider, this.shadeControllerProvider, this.starterProvider, this.notificationEntryManagerProvider, this.uiEventLoggerProvider, this.routerManagerProvider);
    }

    public static MediaOutputDialogFactory provideInstance(Provider<Context> provider, Provider<MediaSessionManager> provider2, Provider<LocalBluetoothManager> provider3, Provider<ShadeController> provider4, Provider<ActivityStarter> provider5, Provider<NotificationEntryManager> provider6, Provider<UiEventLogger> provider7, Provider<MediaRouter2Manager> provider8) {
        return new MediaOutputDialogFactory(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static MediaOutputDialogFactory_Factory create(Provider<Context> provider, Provider<MediaSessionManager> provider2, Provider<LocalBluetoothManager> provider3, Provider<ShadeController> provider4, Provider<ActivityStarter> provider5, Provider<NotificationEntryManager> provider6, Provider<UiEventLogger> provider7, Provider<MediaRouter2Manager> provider8) {
        return new MediaOutputDialogFactory_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
