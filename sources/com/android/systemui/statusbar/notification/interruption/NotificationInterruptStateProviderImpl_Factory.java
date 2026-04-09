package com.android.systemui.statusbar.notification.interruption;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import android.os.PowerManager;
import android.service.dreams.IDreamManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class NotificationInterruptStateProviderImpl_Factory implements Factory<NotificationInterruptStateProviderImpl> {
    private final Provider<AmbientDisplayConfiguration> ambientDisplayConfigurationProvider;
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<ContentResolver> contentResolverProvider;
    private final Provider<Context> contextProvider;
    private final Provider<IDreamManager> dreamManagerProvider;
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<NotificationFilter> notificationFilterProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public NotificationInterruptStateProviderImpl_Factory(Provider<Context> provider, Provider<ContentResolver> provider2, Provider<PowerManager> provider3, Provider<IDreamManager> provider4, Provider<AmbientDisplayConfiguration> provider5, Provider<NotificationFilter> provider6, Provider<BatteryController> provider7, Provider<StatusBarStateController> provider8, Provider<HeadsUpManager> provider9, Provider<Handler> provider10) {
        this.contextProvider = provider;
        this.contentResolverProvider = provider2;
        this.powerManagerProvider = provider3;
        this.dreamManagerProvider = provider4;
        this.ambientDisplayConfigurationProvider = provider5;
        this.notificationFilterProvider = provider6;
        this.batteryControllerProvider = provider7;
        this.statusBarStateControllerProvider = provider8;
        this.headsUpManagerProvider = provider9;
        this.mainHandlerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public NotificationInterruptStateProviderImpl get() {
        return provideInstance(this.contextProvider, this.contentResolverProvider, this.powerManagerProvider, this.dreamManagerProvider, this.ambientDisplayConfigurationProvider, this.notificationFilterProvider, this.batteryControllerProvider, this.statusBarStateControllerProvider, this.headsUpManagerProvider, this.mainHandlerProvider);
    }

    public static NotificationInterruptStateProviderImpl provideInstance(Provider<Context> provider, Provider<ContentResolver> provider2, Provider<PowerManager> provider3, Provider<IDreamManager> provider4, Provider<AmbientDisplayConfiguration> provider5, Provider<NotificationFilter> provider6, Provider<BatteryController> provider7, Provider<StatusBarStateController> provider8, Provider<HeadsUpManager> provider9, Provider<Handler> provider10) {
        return new NotificationInterruptStateProviderImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static NotificationInterruptStateProviderImpl_Factory create(Provider<Context> provider, Provider<ContentResolver> provider2, Provider<PowerManager> provider3, Provider<IDreamManager> provider4, Provider<AmbientDisplayConfiguration> provider5, Provider<NotificationFilter> provider6, Provider<BatteryController> provider7, Provider<StatusBarStateController> provider8, Provider<HeadsUpManager> provider9, Provider<Handler> provider10) {
        return new NotificationInterruptStateProviderImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
