package com.google.android.systemui.assist;

import android.content.Context;
import android.os.Handler;
import com.android.internal.app.AssistUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.PhoneStateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.google.android.systemui.assist.uihints.AssistantPresenceHandler;
import com.google.android.systemui.assist.uihints.GoogleDefaultUiController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AssistManagerGoogle_Factory implements Factory<AssistManagerGoogle> {
    private final Provider<AssistLogger> assistLoggerProvider;
    private final Provider<AssistUtils> assistUtilsProvider;
    private final Provider<AssistantPresenceHandler> assistantPresenceHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceProvisionedController> controllerProvider;
    private final Provider<GoogleDefaultUiController> defaultUiControllerProvider;
    private final Provider<AssistHandleBehaviorController> handleControllerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<NavigationModeController> navigationModeControllerProvider;
    private final Provider<OpaEnabledDispatcher> opaEnabledDispatcherProvider;
    private final Provider<OverviewProxyService> overviewProxyServiceProvider;
    private final Provider<PhoneStateMonitor> phoneStateMonitorProvider;
    private final Provider<SysUiState> sysUiStateProvider;

    public AssistManagerGoogle_Factory(Provider<DeviceProvisionedController> provider, Provider<Context> provider2, Provider<AssistUtils> provider3, Provider<AssistHandleBehaviorController> provider4, Provider<CommandQueue> provider5, Provider<PhoneStateMonitor> provider6, Provider<OverviewProxyService> provider7, Provider<ConfigurationController> provider8, Provider<SysUiState> provider9, Provider<GoogleDefaultUiController> provider10, Provider<AssistLogger> provider11, Provider<BroadcastDispatcher> provider12, Provider<OpaEnabledDispatcher> provider13, Provider<KeyguardUpdateMonitor> provider14, Provider<NavigationModeController> provider15, Provider<AssistantPresenceHandler> provider16, Provider<Handler> provider17) {
        this.controllerProvider = provider;
        this.contextProvider = provider2;
        this.assistUtilsProvider = provider3;
        this.handleControllerProvider = provider4;
        this.commandQueueProvider = provider5;
        this.phoneStateMonitorProvider = provider6;
        this.overviewProxyServiceProvider = provider7;
        this.configurationControllerProvider = provider8;
        this.sysUiStateProvider = provider9;
        this.defaultUiControllerProvider = provider10;
        this.assistLoggerProvider = provider11;
        this.broadcastDispatcherProvider = provider12;
        this.opaEnabledDispatcherProvider = provider13;
        this.keyguardUpdateMonitorProvider = provider14;
        this.navigationModeControllerProvider = provider15;
        this.assistantPresenceHandlerProvider = provider16;
        this.handlerProvider = provider17;
    }

    @Override // javax.inject.Provider
    public AssistManagerGoogle get() {
        return provideInstance(this.controllerProvider, this.contextProvider, this.assistUtilsProvider, this.handleControllerProvider, this.commandQueueProvider, this.phoneStateMonitorProvider, this.overviewProxyServiceProvider, this.configurationControllerProvider, this.sysUiStateProvider, this.defaultUiControllerProvider, this.assistLoggerProvider, this.broadcastDispatcherProvider, this.opaEnabledDispatcherProvider, this.keyguardUpdateMonitorProvider, this.navigationModeControllerProvider, this.assistantPresenceHandlerProvider, this.handlerProvider);
    }

    public static AssistManagerGoogle provideInstance(Provider<DeviceProvisionedController> provider, Provider<Context> provider2, Provider<AssistUtils> provider3, Provider<AssistHandleBehaviorController> provider4, Provider<CommandQueue> provider5, Provider<PhoneStateMonitor> provider6, Provider<OverviewProxyService> provider7, Provider<ConfigurationController> provider8, Provider<SysUiState> provider9, Provider<GoogleDefaultUiController> provider10, Provider<AssistLogger> provider11, Provider<BroadcastDispatcher> provider12, Provider<OpaEnabledDispatcher> provider13, Provider<KeyguardUpdateMonitor> provider14, Provider<NavigationModeController> provider15, Provider<AssistantPresenceHandler> provider16, Provider<Handler> provider17) {
        return new AssistManagerGoogle(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), DoubleCheck.lazy(provider9), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get());
    }

    public static AssistManagerGoogle_Factory create(Provider<DeviceProvisionedController> provider, Provider<Context> provider2, Provider<AssistUtils> provider3, Provider<AssistHandleBehaviorController> provider4, Provider<CommandQueue> provider5, Provider<PhoneStateMonitor> provider6, Provider<OverviewProxyService> provider7, Provider<ConfigurationController> provider8, Provider<SysUiState> provider9, Provider<GoogleDefaultUiController> provider10, Provider<AssistLogger> provider11, Provider<BroadcastDispatcher> provider12, Provider<OpaEnabledDispatcher> provider13, Provider<KeyguardUpdateMonitor> provider14, Provider<NavigationModeController> provider15, Provider<AssistantPresenceHandler> provider16, Provider<Handler> provider17) {
        return new AssistManagerGoogle_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17);
    }
}
