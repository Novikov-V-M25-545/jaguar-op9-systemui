package com.android.systemui.screenshot;

import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class ActionProxyReceiver_Factory implements Factory<ActionProxyReceiver> {
    private final Provider<ActivityManagerWrapper> activityManagerWrapperProvider;
    private final Provider<ScreenshotSmartActions> screenshotSmartActionsProvider;
    private final Provider<Optional<StatusBar>> statusBarProvider;

    public ActionProxyReceiver_Factory(Provider<Optional<StatusBar>> provider, Provider<ActivityManagerWrapper> provider2, Provider<ScreenshotSmartActions> provider3) {
        this.statusBarProvider = provider;
        this.activityManagerWrapperProvider = provider2;
        this.screenshotSmartActionsProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ActionProxyReceiver get() {
        return provideInstance(this.statusBarProvider, this.activityManagerWrapperProvider, this.screenshotSmartActionsProvider);
    }

    public static ActionProxyReceiver provideInstance(Provider<Optional<StatusBar>> provider, Provider<ActivityManagerWrapper> provider2, Provider<ScreenshotSmartActions> provider3) {
        return new ActionProxyReceiver(provider.get(), provider2.get(), provider3.get());
    }

    public static ActionProxyReceiver_Factory create(Provider<Optional<StatusBar>> provider, Provider<ActivityManagerWrapper> provider2, Provider<ScreenshotSmartActions> provider3) {
        return new ActionProxyReceiver_Factory(provider, provider2, provider3);
    }
}
