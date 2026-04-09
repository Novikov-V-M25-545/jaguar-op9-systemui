package com.android.systemui.statusbar.notification.dagger;

import android.app.AppLockManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.util.leak.LeakDetector;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class NotificationsModule_ProvideNotificationEntryManagerFactory implements Factory<NotificationEntryManager> {
    private final Provider<AppLockManager> appLockManagerProvider;
    private final Provider<BubbleController> bubbleControllerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<ForegroundServiceDismissalFeatureController> fgsFeatureControllerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<NotificationEntryManager.KeyguardEnvironment> keyguardEnvironmentProvider;
    private final Provider<LeakDetector> leakDetectorProvider;
    private final Provider<NotificationEntryManagerLogger> loggerProvider;
    private final Provider<NotificationRemoteInputManager> notificationRemoteInputManagerLazyProvider;
    private final Provider<NotificationRowBinder> notificationRowBinderLazyProvider;
    private final Provider<NotificationRankingManager> rankingManagerProvider;

    public NotificationsModule_ProvideNotificationEntryManagerFactory(Provider<AppLockManager> provider, Provider<NotificationEntryManagerLogger> provider2, Provider<NotificationGroupManager> provider3, Provider<NotificationRankingManager> provider4, Provider<NotificationEntryManager.KeyguardEnvironment> provider5, Provider<FeatureFlags> provider6, Provider<NotificationRowBinder> provider7, Provider<NotificationRemoteInputManager> provider8, Provider<LeakDetector> provider9, Provider<BubbleController> provider10, Provider<ForegroundServiceDismissalFeatureController> provider11) {
        this.appLockManagerProvider = provider;
        this.loggerProvider = provider2;
        this.groupManagerProvider = provider3;
        this.rankingManagerProvider = provider4;
        this.keyguardEnvironmentProvider = provider5;
        this.featureFlagsProvider = provider6;
        this.notificationRowBinderLazyProvider = provider7;
        this.notificationRemoteInputManagerLazyProvider = provider8;
        this.leakDetectorProvider = provider9;
        this.bubbleControllerProvider = provider10;
        this.fgsFeatureControllerProvider = provider11;
    }

    @Override // javax.inject.Provider
    public NotificationEntryManager get() {
        return provideInstance(this.appLockManagerProvider, this.loggerProvider, this.groupManagerProvider, this.rankingManagerProvider, this.keyguardEnvironmentProvider, this.featureFlagsProvider, this.notificationRowBinderLazyProvider, this.notificationRemoteInputManagerLazyProvider, this.leakDetectorProvider, this.bubbleControllerProvider, this.fgsFeatureControllerProvider);
    }

    public static NotificationEntryManager provideInstance(Provider<AppLockManager> provider, Provider<NotificationEntryManagerLogger> provider2, Provider<NotificationGroupManager> provider3, Provider<NotificationRankingManager> provider4, Provider<NotificationEntryManager.KeyguardEnvironment> provider5, Provider<FeatureFlags> provider6, Provider<NotificationRowBinder> provider7, Provider<NotificationRemoteInputManager> provider8, Provider<LeakDetector> provider9, Provider<BubbleController> provider10, Provider<ForegroundServiceDismissalFeatureController> provider11) {
        return proxyProvideNotificationEntryManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), DoubleCheck.lazy(provider7), DoubleCheck.lazy(provider8), provider9.get(), DoubleCheck.lazy(provider10), provider11.get());
    }

    public static NotificationsModule_ProvideNotificationEntryManagerFactory create(Provider<AppLockManager> provider, Provider<NotificationEntryManagerLogger> provider2, Provider<NotificationGroupManager> provider3, Provider<NotificationRankingManager> provider4, Provider<NotificationEntryManager.KeyguardEnvironment> provider5, Provider<FeatureFlags> provider6, Provider<NotificationRowBinder> provider7, Provider<NotificationRemoteInputManager> provider8, Provider<LeakDetector> provider9, Provider<BubbleController> provider10, Provider<ForegroundServiceDismissalFeatureController> provider11) {
        return new NotificationsModule_ProvideNotificationEntryManagerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }

    public static NotificationEntryManager proxyProvideNotificationEntryManager(AppLockManager appLockManager, NotificationEntryManagerLogger notificationEntryManagerLogger, NotificationGroupManager notificationGroupManager, NotificationRankingManager notificationRankingManager, NotificationEntryManager.KeyguardEnvironment keyguardEnvironment, FeatureFlags featureFlags, Lazy<NotificationRowBinder> lazy, Lazy<NotificationRemoteInputManager> lazy2, LeakDetector leakDetector, Lazy<BubbleController> lazy3, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        return (NotificationEntryManager) Preconditions.checkNotNull(NotificationsModule.provideNotificationEntryManager(appLockManager, notificationEntryManagerLogger, notificationGroupManager, notificationRankingManager, keyguardEnvironment, featureFlags, lazy, lazy2, leakDetector, lazy3, foregroundServiceDismissalFeatureController), "Cannot return null from a non-@Nullable @Provides method");
    }
}
