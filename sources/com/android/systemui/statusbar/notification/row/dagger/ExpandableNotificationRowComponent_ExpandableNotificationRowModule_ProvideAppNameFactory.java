package com.android.systemui.statusbar.notification.row.dagger;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory implements Factory<String> {
    private final Provider<Context> contextProvider;
    private final Provider<StatusBarNotification> statusBarNotificationProvider;

    public ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory(Provider<Context> provider, Provider<StatusBarNotification> provider2) {
        this.contextProvider = provider;
        this.statusBarNotificationProvider = provider2;
    }

    @Override // javax.inject.Provider
    public String get() {
        return provideInstance(this.contextProvider, this.statusBarNotificationProvider);
    }

    public static String provideInstance(Provider<Context> provider, Provider<StatusBarNotification> provider2) {
        return proxyProvideAppName(provider.get(), provider2.get());
    }

    public static ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory create(Provider<Context> provider, Provider<StatusBarNotification> provider2) {
        return new ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory(provider, provider2);
    }

    public static String proxyProvideAppName(Context context, StatusBarNotification statusBarNotification) {
        return (String) Preconditions.checkNotNull(ExpandableNotificationRowComponent.ExpandableNotificationRowModule.provideAppName(context, statusBarNotification), "Cannot return null from a non-@Nullable @Provides method");
    }
}
