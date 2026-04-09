package com.android.systemui.statusbar.notification.row;

import com.android.systemui.media.MediaFeatureFlag;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.ConversationNotificationProcessor;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class NotificationContentInflater_Factory implements Factory<NotificationContentInflater> {
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<ConversationNotificationProcessor> conversationProcessorProvider;
    private final Provider<MediaFeatureFlag> mediaFeatureFlagProvider;
    private final Provider<NotificationRemoteInputManager> remoteInputManagerProvider;
    private final Provider<NotifRemoteViewCache> remoteViewCacheProvider;
    private final Provider<SmartReplyConstants> smartReplyConstantsProvider;
    private final Provider<SmartReplyController> smartReplyControllerProvider;

    public NotificationContentInflater_Factory(Provider<NotifRemoteViewCache> provider, Provider<NotificationRemoteInputManager> provider2, Provider<SmartReplyConstants> provider3, Provider<SmartReplyController> provider4, Provider<ConversationNotificationProcessor> provider5, Provider<MediaFeatureFlag> provider6, Provider<Executor> provider7) {
        this.remoteViewCacheProvider = provider;
        this.remoteInputManagerProvider = provider2;
        this.smartReplyConstantsProvider = provider3;
        this.smartReplyControllerProvider = provider4;
        this.conversationProcessorProvider = provider5;
        this.mediaFeatureFlagProvider = provider6;
        this.bgExecutorProvider = provider7;
    }

    @Override // javax.inject.Provider
    public NotificationContentInflater get() {
        return provideInstance(this.remoteViewCacheProvider, this.remoteInputManagerProvider, this.smartReplyConstantsProvider, this.smartReplyControllerProvider, this.conversationProcessorProvider, this.mediaFeatureFlagProvider, this.bgExecutorProvider);
    }

    public static NotificationContentInflater provideInstance(Provider<NotifRemoteViewCache> provider, Provider<NotificationRemoteInputManager> provider2, Provider<SmartReplyConstants> provider3, Provider<SmartReplyController> provider4, Provider<ConversationNotificationProcessor> provider5, Provider<MediaFeatureFlag> provider6, Provider<Executor> provider7) {
        return new NotificationContentInflater(provider.get(), provider2.get(), DoubleCheck.lazy(provider3), DoubleCheck.lazy(provider4), provider5.get(), provider6.get(), provider7.get());
    }

    public static NotificationContentInflater_Factory create(Provider<NotifRemoteViewCache> provider, Provider<NotificationRemoteInputManager> provider2, Provider<SmartReplyConstants> provider3, Provider<SmartReplyController> provider4, Provider<ConversationNotificationProcessor> provider5, Provider<MediaFeatureFlag> provider6, Provider<Executor> provider7) {
        return new NotificationContentInflater_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
