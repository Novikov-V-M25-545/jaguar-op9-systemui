package com.android.systemui.classifier;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.classifier.brightline.FalsingDataProvider;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.sensors.ProximitySensor;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class FalsingManagerProxy_Factory implements Factory<FalsingManagerProxy> {
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> deviceConfigProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<FalsingDataProvider> falsingDataProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<ProximitySensor> proximitySensorProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<Executor> uiBgExecutorProvider;

    public FalsingManagerProxy_Factory(Provider<Context> provider, Provider<PluginManager> provider2, Provider<Executor> provider3, Provider<ProximitySensor> provider4, Provider<DeviceConfigProxy> provider5, Provider<DockManager> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<DumpManager> provider8, Provider<Executor> provider9, Provider<StatusBarStateController> provider10, Provider<FalsingDataProvider> provider11) {
        this.contextProvider = provider;
        this.pluginManagerProvider = provider2;
        this.executorProvider = provider3;
        this.proximitySensorProvider = provider4;
        this.deviceConfigProvider = provider5;
        this.dockManagerProvider = provider6;
        this.keyguardUpdateMonitorProvider = provider7;
        this.dumpManagerProvider = provider8;
        this.uiBgExecutorProvider = provider9;
        this.statusBarStateControllerProvider = provider10;
        this.falsingDataProvider = provider11;
    }

    @Override // javax.inject.Provider
    public FalsingManagerProxy get() {
        return provideInstance(this.contextProvider, this.pluginManagerProvider, this.executorProvider, this.proximitySensorProvider, this.deviceConfigProvider, this.dockManagerProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.uiBgExecutorProvider, this.statusBarStateControllerProvider, this.falsingDataProvider);
    }

    public static FalsingManagerProxy provideInstance(Provider<Context> provider, Provider<PluginManager> provider2, Provider<Executor> provider3, Provider<ProximitySensor> provider4, Provider<DeviceConfigProxy> provider5, Provider<DockManager> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<DumpManager> provider8, Provider<Executor> provider9, Provider<StatusBarStateController> provider10, Provider<FalsingDataProvider> provider11) {
        return new FalsingManagerProxy(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get());
    }

    public static FalsingManagerProxy_Factory create(Provider<Context> provider, Provider<PluginManager> provider2, Provider<Executor> provider3, Provider<ProximitySensor> provider4, Provider<DeviceConfigProxy> provider5, Provider<DockManager> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<DumpManager> provider8, Provider<Executor> provider9, Provider<StatusBarStateController> provider10, Provider<FalsingDataProvider> provider11) {
        return new FalsingManagerProxy_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }
}
