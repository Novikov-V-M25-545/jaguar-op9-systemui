package com.android.systemui.privacy;

import android.os.UserManager;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class PrivacyItemController_Factory implements Factory<PrivacyItemController> {
    private final Provider<AppOpsController> appOpsControllerProvider;
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<DeviceConfigProxy> deviceConfigProxyProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<DelayableExecutor> uiExecutorProvider;
    private final Provider<UserManager> userManagerProvider;

    public PrivacyItemController_Factory(Provider<AppOpsController> provider, Provider<DelayableExecutor> provider2, Provider<Executor> provider3, Provider<BroadcastDispatcher> provider4, Provider<DeviceConfigProxy> provider5, Provider<UserManager> provider6, Provider<DumpManager> provider7) {
        this.appOpsControllerProvider = provider;
        this.uiExecutorProvider = provider2;
        this.bgExecutorProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.deviceConfigProxyProvider = provider5;
        this.userManagerProvider = provider6;
        this.dumpManagerProvider = provider7;
    }

    @Override // javax.inject.Provider
    public PrivacyItemController get() {
        return provideInstance(this.appOpsControllerProvider, this.uiExecutorProvider, this.bgExecutorProvider, this.broadcastDispatcherProvider, this.deviceConfigProxyProvider, this.userManagerProvider, this.dumpManagerProvider);
    }

    public static PrivacyItemController provideInstance(Provider<AppOpsController> provider, Provider<DelayableExecutor> provider2, Provider<Executor> provider3, Provider<BroadcastDispatcher> provider4, Provider<DeviceConfigProxy> provider5, Provider<UserManager> provider6, Provider<DumpManager> provider7) {
        return new PrivacyItemController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get());
    }

    public static PrivacyItemController_Factory create(Provider<AppOpsController> provider, Provider<DelayableExecutor> provider2, Provider<Executor> provider3, Provider<BroadcastDispatcher> provider4, Provider<DeviceConfigProxy> provider5, Provider<UserManager> provider6, Provider<DumpManager> provider7) {
        return new PrivacyItemController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
