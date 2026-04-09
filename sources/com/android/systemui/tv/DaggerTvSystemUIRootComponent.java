package com.android.systemui.tv;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppLockManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.hardware.SensorPrivacyManager;
import android.hardware.display.NightDisplayListener;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.service.dreams.IDreamManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import androidx.slice.Clock;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.CarrierTextController;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.KeyguardMessageArea;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityModel_Factory;
import com.android.keyguard.KeyguardSliceView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor_Factory;
import com.android.keyguard.clock.ClockManager;
import com.android.keyguard.clock.ClockManager_Factory;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.ActivityIntentHelper_Factory;
import com.android.systemui.ActivityStarterDelegate;
import com.android.systemui.ActivityStarterDelegate_Factory;
import com.android.systemui.BootCompleteCacheImpl;
import com.android.systemui.BootCompleteCacheImpl_Factory;
import com.android.systemui.Dependency;
import com.android.systemui.Dependency_MembersInjector;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.ForegroundServiceController_Factory;
import com.android.systemui.ForegroundServiceLifetimeExtender_Factory;
import com.android.systemui.ForegroundServiceNotificationListener;
import com.android.systemui.ForegroundServiceNotificationListener_Factory;
import com.android.systemui.ForegroundServicesDialog;
import com.android.systemui.ForegroundServicesDialog_Factory;
import com.android.systemui.ImageWallpaper;
import com.android.systemui.ImageWallpaper_Factory;
import com.android.systemui.InitController;
import com.android.systemui.InitController_Factory;
import com.android.systemui.LatencyTester;
import com.android.systemui.LatencyTester_Factory;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.ScreenDecorations_Factory;
import com.android.systemui.SizeCompatModeActivityController;
import com.android.systemui.SizeCompatModeActivityController_Factory;
import com.android.systemui.SliceBroadcastRelayHandler;
import com.android.systemui.SliceBroadcastRelayHandler_Factory;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.SystemUIAppComponentFactory_MembersInjector;
import com.android.systemui.SystemUIService;
import com.android.systemui.SystemUIService_Factory;
import com.android.systemui.TransactionPool;
import com.android.systemui.TransactionPool_Factory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.UiOffloadThread_Factory;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.accessibility.SystemActions_Factory;
import com.android.systemui.accessibility.WindowMagnification;
import com.android.systemui.accessibility.WindowMagnification_Factory;
import com.android.systemui.appops.AppOpsControllerImpl;
import com.android.systemui.appops.AppOpsControllerImpl_Factory;
import com.android.systemui.appops.PermissionFlagsCache;
import com.android.systemui.appops.PermissionFlagsCache_Factory;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.assist.AssistHandleBehaviorController_Factory;
import com.android.systemui.assist.AssistHandleLikeHomeBehavior_Factory;
import com.android.systemui.assist.AssistHandleOffBehavior_Factory;
import com.android.systemui.assist.AssistHandleReminderExpBehavior_Factory;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.AssistLogger_Factory;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.assist.AssistManager_Factory;
import com.android.systemui.assist.AssistModule_ProvideAssistHandleBehaviorControllerMapFactory;
import com.android.systemui.assist.AssistModule_ProvideAssistHandleViewControllerFactory;
import com.android.systemui.assist.AssistModule_ProvideAssistUtilsFactory;
import com.android.systemui.assist.AssistModule_ProvideBackgroundHandlerFactory;
import com.android.systemui.assist.AssistModule_ProvideSystemClockFactory;
import com.android.systemui.assist.DeviceConfigHelper;
import com.android.systemui.assist.DeviceConfigHelper_Factory;
import com.android.systemui.assist.PhoneStateMonitor;
import com.android.systemui.assist.PhoneStateMonitor_Factory;
import com.android.systemui.biometrics.AuthController;
import com.android.systemui.biometrics.AuthController_Factory;
import com.android.systemui.biometrics.FODCircleViewImpl;
import com.android.systemui.biometrics.FODCircleViewImpl_Factory;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger_Factory;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleDataRepository;
import com.android.systemui.bubbles.BubbleDataRepository_Factory;
import com.android.systemui.bubbles.BubbleData_Factory;
import com.android.systemui.bubbles.BubbleOverflowActivity;
import com.android.systemui.bubbles.BubbleOverflowActivity_Factory;
import com.android.systemui.bubbles.dagger.BubbleModule_NewBubbleControllerFactory;
import com.android.systemui.bubbles.storage.BubblePersistentRepository;
import com.android.systemui.bubbles.storage.BubblePersistentRepository_Factory;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository_Factory;
import com.android.systemui.classifier.FalsingManagerProxy;
import com.android.systemui.classifier.FalsingManagerProxy_Factory;
import com.android.systemui.classifier.brightline.FalsingDataProvider_Factory;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.colorextraction.SysuiColorExtractor_Factory;
import com.android.systemui.controls.CustomIconCache;
import com.android.systemui.controls.CustomIconCache_Factory;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl_Factory;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.ControlsControllerImpl_Factory;
import com.android.systemui.controls.controller.ControlsFavoritePersistenceWrapper;
import com.android.systemui.controls.dagger.ControlsComponent;
import com.android.systemui.controls.dagger.ControlsComponent_Factory;
import com.android.systemui.controls.dagger.ControlsModule_ProvidesControlsFeatureEnabledFactory;
import com.android.systemui.controls.management.ControlsEditingActivity;
import com.android.systemui.controls.management.ControlsEditingActivity_Factory;
import com.android.systemui.controls.management.ControlsFavoritingActivity;
import com.android.systemui.controls.management.ControlsFavoritingActivity_Factory;
import com.android.systemui.controls.management.ControlsListingControllerImpl;
import com.android.systemui.controls.management.ControlsListingControllerImpl_Factory;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity_Factory;
import com.android.systemui.controls.management.ControlsRequestDialog;
import com.android.systemui.controls.management.ControlsRequestDialog_Factory;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl_Factory;
import com.android.systemui.controls.ui.ControlsUiControllerImpl;
import com.android.systemui.controls.ui.ControlsUiControllerImpl_Factory;
import com.android.systemui.dagger.ContextComponentHelper;
import com.android.systemui.dagger.ContextComponentResolver;
import com.android.systemui.dagger.ContextComponentResolver_Factory;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.DependencyProvider_ProvideActivityManagerWrapperFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideAmbientDisplayConfigurationFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideAutoHideControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideConfigurationControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideDataSaverControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideDevicePolicyManagerWrapperFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideDisplayMetricsFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideINotificationManagerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideLeakDetectorFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideLockPatternUtilsFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideMetricsLoggerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideNavigationBarControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideNightDisplayListenerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideNotificationMessagingUtilFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidePluginManagerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidePulseControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideSharePreferencesFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideTimeTickHandlerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideUiEventLoggerFactory;
import com.android.systemui.dagger.DependencyProvider_ProviderLayoutInflaterFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidesBroadcastDispatcherFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidesChoreographerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidesViewMediatorCallbackFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAccessibilityManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideActivityManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAlarmManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAppLockManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAudioManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideConnectivityManagagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideContentResolverFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideDevicePolicyManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideDisplayIdFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIActivityManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIActivityTaskManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIBatteryStatsFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIDreamManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIPackageManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIStatusBarServiceFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIWallPaperManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIWindowManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideKeyguardManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideLatencyTrackerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideLauncherAppsFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideLocalBluetoothControllerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideMediaRouter2ManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideMediaSessionManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideNetworkScoreManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideNotificationManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidePackageManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidePackageManagerWrapperFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidePowerManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideResourcesFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideSensorPrivacyManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideShortcutManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideTelecomManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideTelephonyManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideTrustManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideUserManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideVibratorFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideWallpaperManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideWifiManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideWindowManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidesSensorManagerFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideBatteryControllerFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideLeakReportEmailFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideRecentsFactory;
import com.android.systemui.dagger.SystemUIModule_ProvideKeyguardLiftControllerFactory;
import com.android.systemui.dagger.SystemUIModule_ProvideSysUiStateFactory;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dock.DockManagerImpl;
import com.android.systemui.dock.DockManagerImpl_Factory;
import com.android.systemui.doze.DozeFactory_Factory;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.doze.DozeLog_Factory;
import com.android.systemui.doze.DozeLogger_Factory;
import com.android.systemui.doze.DozeService;
import com.android.systemui.doze.DozeService_Factory;
import com.android.systemui.dump.DumpHandler_Factory;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.dump.DumpManager_Factory;
import com.android.systemui.dump.LogBufferEulogizer;
import com.android.systemui.dump.LogBufferEulogizer_Factory;
import com.android.systemui.dump.LogBufferFreezer_Factory;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService_Factory;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.fragments.FragmentService_Factory;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.globalactions.GlobalActionsComponent_Factory;
import com.android.systemui.globalactions.GlobalActionsDialog_Factory;
import com.android.systemui.globalactions.GlobalActionsImpl_Factory;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.DismissCallbackRegistry_Factory;
import com.android.systemui.keyguard.KeyguardLifecyclesDispatcher;
import com.android.systemui.keyguard.KeyguardLifecyclesDispatcher_Factory;
import com.android.systemui.keyguard.KeyguardService;
import com.android.systemui.keyguard.KeyguardService_Factory;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.keyguard.KeyguardSliceProvider_MembersInjector;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.ScreenLifecycle_Factory;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle_Factory;
import com.android.systemui.keyguard.WorkLockActivity;
import com.android.systemui.keyguard.WorkLockActivity_Factory;
import com.android.systemui.keyguard.dagger.KeyguardModule_NewKeyguardViewMediatorFactory;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import com.android.systemui.log.dagger.LogModule_ProvideBroadcastDispatcherLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideDozeLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideLogcatEchoTrackerFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotifInteractionLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotificationSectionLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotificationsLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideQuickSettingsLogBufferFactory;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.media.KeyguardMediaController_Factory;
import com.android.systemui.media.LocalMediaManagerFactory_Factory;
import com.android.systemui.media.MediaBrowserFactory_Factory;
import com.android.systemui.media.MediaCarouselController;
import com.android.systemui.media.MediaCarouselController_Factory;
import com.android.systemui.media.MediaControlPanel_Factory;
import com.android.systemui.media.MediaControllerFactory_Factory;
import com.android.systemui.media.MediaDataCombineLatest_Factory;
import com.android.systemui.media.MediaDataFilter_Factory;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDataManager_Factory;
import com.android.systemui.media.MediaDeviceManager_Factory;
import com.android.systemui.media.MediaFeatureFlag_Factory;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.media.MediaHierarchyManager_Factory;
import com.android.systemui.media.MediaHost;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.media.MediaHostStatesManager_Factory;
import com.android.systemui.media.MediaHost_Factory;
import com.android.systemui.media.MediaHost_MediaHostStateHolder_Factory;
import com.android.systemui.media.MediaResumeListener;
import com.android.systemui.media.MediaResumeListener_Factory;
import com.android.systemui.media.MediaSessionBasedFilter_Factory;
import com.android.systemui.media.MediaTimeoutListener;
import com.android.systemui.media.MediaTimeoutListener_Factory;
import com.android.systemui.media.MediaViewController_Factory;
import com.android.systemui.media.ResumeMediaBrowserFactory_Factory;
import com.android.systemui.media.SeekBarViewModel_Factory;
import com.android.systemui.media.dialog.MediaOutputDialogFactory_Factory;
import com.android.systemui.media.dialog.MediaOutputDialogReceiver;
import com.android.systemui.media.dialog.MediaOutputDialogReceiver_Factory;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipAnimationController_Factory;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipBoundsHandler_Factory;
import com.android.systemui.pip.PipSnapAlgorithm_Factory;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipSurfaceTransactionHelper_Factory;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipTaskOrganizer_Factory;
import com.android.systemui.pip.PipUI;
import com.android.systemui.pip.PipUI_Factory;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.PipUiEventLogger_Factory;
import com.android.systemui.pip.tv.PipControlsView;
import com.android.systemui.pip.tv.PipControlsViewController;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.pip.tv.PipManager_Factory;
import com.android.systemui.pip.tv.PipMenuActivity;
import com.android.systemui.pip.tv.PipMenuActivity_Factory;
import com.android.systemui.pip.tv.dagger.TvPipComponent;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.PluginDependencyProvider_Factory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimatesImpl;
import com.android.systemui.power.EnhancedEstimatesImpl_Factory;
import com.android.systemui.power.PowerNotificationWarnings;
import com.android.systemui.power.PowerNotificationWarnings_Factory;
import com.android.systemui.power.PowerUI;
import com.android.systemui.power.PowerUI_Factory;
import com.android.systemui.privacy.PrivacyItemController;
import com.android.systemui.privacy.PrivacyItemController_Factory;
import com.android.systemui.qs.AutoAddTracker_Builder_Factory;
import com.android.systemui.qs.QSContainerImplController;
import com.android.systemui.qs.QSFooterImpl;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.QSTileHost_Factory;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.QuickStatusBarHeader;
import com.android.systemui.qs.QuickStatusBarHeaderController;
import com.android.systemui.qs.carrier.QSCarrierGroupController;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.dagger.QSModule_ProvideAutoTileManagerFactory;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.logging.QSLogger_Factory;
import com.android.systemui.qs.tileimpl.QSFactoryImpl;
import com.android.systemui.qs.tileimpl.QSFactoryImpl_Factory;
import com.android.systemui.qs.tiles.AODTile_Factory;
import com.android.systemui.qs.tiles.AirplaneModeTile_Factory;
import com.android.systemui.qs.tiles.AmbientDisplayTile_Factory;
import com.android.systemui.qs.tiles.AntiFlickerTile_Factory;
import com.android.systemui.qs.tiles.BatterySaverTile_Factory;
import com.android.systemui.qs.tiles.BluetoothTile_Factory;
import com.android.systemui.qs.tiles.CPUInfoTile_Factory;
import com.android.systemui.qs.tiles.CaffeineTile_Factory;
import com.android.systemui.qs.tiles.CastTile_Factory;
import com.android.systemui.qs.tiles.CellularTile_Factory;
import com.android.systemui.qs.tiles.ColorInversionTile_Factory;
import com.android.systemui.qs.tiles.CompassTile_Factory;
import com.android.systemui.qs.tiles.DataSaverTile_Factory;
import com.android.systemui.qs.tiles.DataSwitchTile_Factory;
import com.android.systemui.qs.tiles.DndTile_Factory;
import com.android.systemui.qs.tiles.FPSInfoTile_Factory;
import com.android.systemui.qs.tiles.FlashlightTile_Factory;
import com.android.systemui.qs.tiles.GamingModeTile_Factory;
import com.android.systemui.qs.tiles.HeadsUpTile_Factory;
import com.android.systemui.qs.tiles.HotspotTile_Factory;
import com.android.systemui.qs.tiles.LiveDisplayTile_Factory;
import com.android.systemui.qs.tiles.LocaleTile_Factory;
import com.android.systemui.qs.tiles.LocationTile_Factory;
import com.android.systemui.qs.tiles.NfcTile_Factory;
import com.android.systemui.qs.tiles.NightDisplayTile_Factory;
import com.android.systemui.qs.tiles.OnTheGoTile_Factory;
import com.android.systemui.qs.tiles.PowerMenuTile_Factory;
import com.android.systemui.qs.tiles.PowerShareTile_Factory;
import com.android.systemui.qs.tiles.ProfilesTile_Factory;
import com.android.systemui.qs.tiles.ReadingModeTile_Factory;
import com.android.systemui.qs.tiles.RebootTile_Factory;
import com.android.systemui.qs.tiles.RotationLockTile_Factory;
import com.android.systemui.qs.tiles.ScreenRecordTile_Factory;
import com.android.systemui.qs.tiles.SensorPrivacyTile_Factory;
import com.android.systemui.qs.tiles.SleepModeTile_Factory;
import com.android.systemui.qs.tiles.SmartPixelsTile_Factory;
import com.android.systemui.qs.tiles.SoundTile_Factory;
import com.android.systemui.qs.tiles.SyncTile_Factory;
import com.android.systemui.qs.tiles.UiModeNightTile_Factory;
import com.android.systemui.qs.tiles.UsbTetherTile_Factory;
import com.android.systemui.qs.tiles.UserTile_Factory;
import com.android.systemui.qs.tiles.VolumeTile_Factory;
import com.android.systemui.qs.tiles.VpnTile_Factory;
import com.android.systemui.qs.tiles.WeatherTile_Factory;
import com.android.systemui.qs.tiles.WifiTile_Factory;
import com.android.systemui.qs.tiles.WorkModeTile_Factory;
import com.android.systemui.recents.OverviewProxyRecentsImpl;
import com.android.systemui.recents.OverviewProxyRecentsImpl_Factory;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.OverviewProxyService_Factory;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsImplementation;
import com.android.systemui.recents.RecentsModule_ProvideRecentsImplFactory;
import com.android.systemui.recents.ScreenPinningRequest_Factory;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.screenrecord.RecordingController_Factory;
import com.android.systemui.screenrecord.RecordingService;
import com.android.systemui.screenrecord.RecordingService_Factory;
import com.android.systemui.screenrecord.ScreenRecordDialog;
import com.android.systemui.screenrecord.ScreenRecordDialog_Factory;
import com.android.systemui.screenshot.ActionProxyReceiver;
import com.android.systemui.screenshot.ActionProxyReceiver_Factory;
import com.android.systemui.screenshot.DeleteScreenshotReceiver;
import com.android.systemui.screenshot.DeleteScreenshotReceiver_Factory;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.screenshot.GlobalScreenshot_Factory;
import com.android.systemui.screenshot.ScreenshotNotificationsController_Factory;
import com.android.systemui.screenshot.ScreenshotSmartActions;
import com.android.systemui.screenshot.ScreenshotSmartActions_Factory;
import com.android.systemui.screenshot.SmartActionsReceiver;
import com.android.systemui.screenshot.SmartActionsReceiver_Factory;
import com.android.systemui.screenshot.TakeScreenshotService;
import com.android.systemui.screenshot.TakeScreenshotService_Factory;
import com.android.systemui.settings.BrightnessDialog;
import com.android.systemui.settings.BrightnessDialog_Factory;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.settings.dagger.SettingsModule_ProvideCurrentUserContextTrackerFactory;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shortcut.ShortcutKeyDispatcher;
import com.android.systemui.shortcut.ShortcutKeyDispatcher_Factory;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerModule_ProvideDividerFactory;
import com.android.systemui.statusbar.ActionClickLogger_Factory;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.BlurUtils_Factory;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.FeatureFlags_Factory;
import com.android.systemui.statusbar.FlingAnimationUtils_Builder_Factory;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.KeyguardIndicationController_Factory;
import com.android.systemui.statusbar.MediaArtworkProcessor;
import com.android.systemui.statusbar.MediaArtworkProcessor_Factory;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationClickNotifier_Factory;
import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.NotificationInteractionTracker_Factory;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl_Factory;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.NotificationShadeDepthController_Factory;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.PulseExpansionHandler_Factory;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.StatusBarStateControllerImpl;
import com.android.systemui.statusbar.StatusBarStateControllerImpl_Factory;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SuperStatusBarViewFactory_Factory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.VibratorHelper_Factory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideCommandQueueFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationListenerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationViewHierarchyManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideSmartReplyControllerFactory;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.ConversationNotificationManager_Factory;
import com.android.systemui.statusbar.notification.ConversationNotificationProcessor_Factory;
import com.android.systemui.statusbar.notification.DynamicChildBindController_Factory;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.DynamicPrivacyController_Factory;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController_Factory;
import com.android.systemui.statusbar.notification.InstantAppNotifier;
import com.android.systemui.statusbar.notification.InstantAppNotifier_Factory;
import com.android.systemui.statusbar.notification.NotificationClickerLogger_Factory;
import com.android.systemui.statusbar.notification.NotificationClicker_Builder_Factory;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger_Factory;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationFilter_Factory;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager_Factory;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator_Factory;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifCollection_Factory;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl_Factory;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotifPipeline_Factory;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn_Factory;
import com.android.systemui.statusbar.notification.collection.NotifViewManager;
import com.android.systemui.statusbar.notification.collection.NotifViewManager_Factory;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager_Factory;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder_Factory;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver_Factory;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger_Factory;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.DeviceProvisionedCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.DeviceProvisionedCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.HideNotifsForOtherUsersCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.KeyguardCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.KeyguardCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.MediaCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinatorLogger_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.SharedCoordinatorLogger_Factory;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper_Factory;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl_Factory;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer_Factory;
import com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger_Factory;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger_Factory;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider_Factory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideCommonNotifCollectionFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationBlockingHelperManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationEntryManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationGutsManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationLoggerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationPanelLoggerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationsControllerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideVisualStabilityManagerFactory;
import com.android.systemui.statusbar.notification.icon.IconBuilder_Factory;
import com.android.systemui.statusbar.notification.icon.IconManager_Factory;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl_Factory;
import com.android.systemui.statusbar.notification.init.NotificationsControllerStub_Factory;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier_Factory;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController_Factory;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder_Factory;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl_Factory;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.logging.NotificationLogger_ExpansionStateLogger_Factory;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary;
import com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapterImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapterImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubViewModelFactoryDataSourceImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubViewModelFactoryDataSourceImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl_Factory;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController_Factory;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController_Factory;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialog_Builder_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableOutlineViewController;
import com.android.systemui.statusbar.notification.row.ExpandableOutlineViewController_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableViewController;
import com.android.systemui.statusbar.notification.row.ExpandableViewController_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline_Factory;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager_Factory;
import com.android.systemui.statusbar.notification.row.NotifRemoteViewCache;
import com.android.systemui.statusbar.notification.row.NotifRemoteViewCacheImpl_Factory;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater_Factory;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController_Builder_Factory;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.notification.row.RowContentBindStageLogger_Factory;
import com.android.systemui.statusbar.notification.row.RowContentBindStage_Factory;
import com.android.systemui.statusbar.notification.row.RowInflaterTask_Factory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.BiometricUnlockController_Factory;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl_Factory;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.DozeParameters_Factory;
import com.android.systemui.statusbar.phone.DozeScrimController;
import com.android.systemui.statusbar.phone.DozeScrimController_Factory;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.DozeServiceHost_Factory;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.KeyguardBypassController_Factory;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil_Factory;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl_Factory;
import com.android.systemui.statusbar.phone.KeyguardLiftController;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LightBarController_Factory;
import com.android.systemui.statusbar.phone.LightsOutNotifController;
import com.android.systemui.statusbar.phone.LightsOutNotifController_Factory;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger_Factory;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.LockscreenLockIconController_Factory;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.LockscreenWallpaper_Factory;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl_Factory;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NavigationModeController_Factory;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager_Factory;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController_Factory;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController_Factory;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy_Factory;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimController_Factory;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.ShadeControllerImpl;
import com.android.systemui.statusbar.phone.ShadeControllerImpl_Factory;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl_Factory;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager_Factory;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger_Factory;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter_Builder_Factory;
import com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback;
import com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback_Factory;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager_Factory;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.android.systemui.statusbar.phone.StatusBarWindowController_Factory;
import com.android.systemui.statusbar.phone.dagger.StatusBarComponent;
import com.android.systemui.statusbar.phone.dagger.StatusBarPhoneDependenciesModule_ProvideNotificationGroupAlertTransferHelperFactory;
import com.android.systemui.statusbar.phone.dagger.StatusBarPhoneModule_ProvideStatusBarFactory;
import com.android.systemui.statusbar.phone.dagger.StatusBarViewModule_GetNotificationPanelViewFactory;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.AccessibilityController_Factory;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper_Factory;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl_Factory;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.CastControllerImpl_Factory;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl_Factory;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl_Factory;
import com.android.systemui.statusbar.policy.FlashlightControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightControllerImpl_Factory;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.HotspotControllerImpl_Factory;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardStateControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardStateControllerImpl_Factory;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.LocationControllerImpl_Factory;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl_Factory;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl_Factory;
import com.android.systemui.statusbar.policy.PulseController;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler_Factory;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import com.android.systemui.statusbar.policy.RemoteInputUriController_Factory;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.SmartReplyConstants_Factory;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl_Factory;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.UserSwitcherController_Factory;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl_Factory;
import com.android.systemui.statusbar.tv.TvStatusBar;
import com.android.systemui.statusbar.tv.TvStatusBar_Factory;
import com.android.systemui.theme.ThemeOverlayController;
import com.android.systemui.theme.ThemeOverlayController_Factory;
import com.android.systemui.toast.ToastUI;
import com.android.systemui.toast.ToastUI_Factory;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tracing.ProtoTracer_Factory;
import com.android.systemui.tuner.TunablePadding;
import com.android.systemui.tuner.TunablePadding_TunablePaddingService_Factory;
import com.android.systemui.tuner.TunerActivity;
import com.android.systemui.tuner.TunerActivity_Factory;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerServiceImpl;
import com.android.systemui.tuner.TunerServiceImpl_Factory;
import com.android.systemui.tv.TvSystemUIRootComponent;
import com.android.systemui.usb.UsbDebuggingActivity;
import com.android.systemui.usb.UsbDebuggingActivity_Factory;
import com.android.systemui.usb.UsbDebuggingSecondaryUserActivity;
import com.android.systemui.usb.UsbDebuggingSecondaryUserActivity_Factory;
import com.android.systemui.user.CreateUserActivity;
import com.android.systemui.user.CreateUserActivity_Factory;
import com.android.systemui.user.UserCreator_Factory;
import com.android.systemui.user.UserModule;
import com.android.systemui.user.UserModule_ProvideEditUserInfoControllerFactory;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.DeviceConfigProxy_Factory;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.FloatingContentCoordinator_Factory;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.InjectionInflationController_Factory;
import com.android.systemui.util.InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory;
import com.android.systemui.util.InjectionInflationController_ViewAttributeProvider_ProvideContextFactory;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.RingerModeTrackerImpl;
import com.android.systemui.util.RingerModeTrackerImpl_Factory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBgHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBgLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideLongRunningExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideLongRunningLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideUiBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.concurrency.Execution;
import com.android.systemui.util.concurrency.ExecutionImpl_Factory;
import com.android.systemui.util.concurrency.RepeatableExecutor;
import com.android.systemui.util.io.Files;
import com.android.systemui.util.io.Files_Factory;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.util.leak.GarbageMonitor_Factory;
import com.android.systemui.util.leak.GarbageMonitor_MemoryTile_Factory;
import com.android.systemui.util.leak.GarbageMonitor_Service_Factory;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.util.leak.LeakReporter;
import com.android.systemui.util.leak.LeakReporter_Factory;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.AsyncSensorManager_Factory;
import com.android.systemui.util.sensors.ProximitySensor_Factory;
import com.android.systemui.util.sensors.ProximitySensor_ProximityCheck_Factory;
import com.android.systemui.util.sensors.SensorModule_ProvidePrimaryProxSensorFactory;
import com.android.systemui.util.sensors.SensorModule_ProvideSecondaryProxSensorFactory;
import com.android.systemui.util.sensors.ThresholdSensorImpl_Builder_Factory;
import com.android.systemui.util.time.DateFormatUtil_Factory;
import com.android.systemui.util.time.SystemClock;
import com.android.systemui.util.time.SystemClockImpl_Factory;
import com.android.systemui.util.wakelock.DelayedWakeLock_Builder_Factory;
import com.android.systemui.util.wakelock.WakeLock_Builder_Factory;
import com.android.systemui.volume.VolumeDialogComponent;
import com.android.systemui.volume.VolumeDialogComponent_Factory;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import com.android.systemui.volume.VolumeDialogControllerImpl_Factory;
import com.android.systemui.volume.VolumeUI;
import com.android.systemui.volume.VolumeUI_Factory;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayController_Factory;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.DisplayImeController_Factory;
import com.android.systemui.wm.SystemWindows;
import com.android.systemui.wm.SystemWindows_Factory;
import com.google.android.systemui.assist.AssistManagerGoogle;
import com.google.android.systemui.assist.AssistManagerGoogle_Factory;
import com.google.android.systemui.assist.OpaEnabledDispatcher;
import com.google.android.systemui.assist.OpaEnabledDispatcher_Factory;
import com.google.android.systemui.assist.uihints.AssistantPresenceHandler;
import com.google.android.systemui.assist.uihints.AssistantPresenceHandler_Factory;
import com.google.android.systemui.assist.uihints.GoogleDefaultUiController;
import com.google.android.systemui.assist.uihints.GoogleDefaultUiController_Factory;
import dagger.Lazy;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.InstanceFactory;
import dagger.internal.MapProviderFactory;
import dagger.internal.Preconditions;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class DaggerTvSystemUIRootComponent implements TvSystemUIRootComponent {
    private static final Provider ABSENT_JDK_OPTIONAL_PROVIDER = InstanceFactory.create(Optional.empty());
    private AODTile_Factory aODTileProvider;
    private Provider<AccessibilityController> accessibilityControllerProvider;
    private Provider<AccessibilityManagerWrapper> accessibilityManagerWrapperProvider;
    private ActionClickLogger_Factory actionClickLoggerProvider;
    private ActionProxyReceiver_Factory actionProxyReceiverProvider;
    private Provider<ActivityIntentHelper> activityIntentHelperProvider;
    private Provider<ActivityStarterDelegate> activityStarterDelegateProvider;
    private AirplaneModeTile_Factory airplaneModeTileProvider;
    private AmbientDisplayTile_Factory ambientDisplayTileProvider;
    private AntiFlickerTile_Factory antiFlickerTileProvider;
    private Provider<AppOpsControllerImpl> appOpsControllerImplProvider;
    private Provider<AppOpsCoordinator> appOpsCoordinatorProvider;
    private Provider<AssistHandleBehaviorController> assistHandleBehaviorControllerProvider;
    private Provider assistHandleLikeHomeBehaviorProvider;
    private Provider assistHandleOffBehaviorProvider;
    private Provider assistHandleReminderExpBehaviorProvider;
    private Provider<AssistLogger> assistLoggerProvider;
    private Provider<AssistManagerGoogle> assistManagerGoogleProvider;
    private Provider<AssistManager> assistManagerProvider;
    private Provider<AssistantPresenceHandler> assistantPresenceHandlerProvider;
    private Provider<AsyncSensorManager> asyncSensorManagerProvider;
    private Provider<AuthController> authControllerProvider;
    private BatterySaverTile_Factory batterySaverTileProvider;
    private Provider<SystemClock> bindSystemClockProvider;
    private Provider<BiometricUnlockController> biometricUnlockControllerProvider;
    private Provider<BluetoothControllerImpl> bluetoothControllerImplProvider;
    private BluetoothTile_Factory bluetoothTileProvider;
    private Provider<BlurUtils> blurUtilsProvider;
    private Provider<BootCompleteCacheImpl> bootCompleteCacheImplProvider;
    private BrightnessDialog_Factory brightnessDialogProvider;
    private BroadcastDispatcherLogger_Factory broadcastDispatcherLoggerProvider;
    private Provider<BubbleCoordinator> bubbleCoordinatorProvider;
    private Provider<BubbleData> bubbleDataProvider;
    private Provider<BubbleDataRepository> bubbleDataRepositoryProvider;
    private BubbleOverflowActivity_Factory bubbleOverflowActivityProvider;
    private Provider<BubblePersistentRepository> bubblePersistentRepositoryProvider;
    private Provider<BubbleVolatileRepository> bubbleVolatileRepositoryProvider;
    private ThresholdSensorImpl_Builder_Factory builderProvider;
    private NotificationClicker_Builder_Factory builderProvider2;
    private WakeLock_Builder_Factory builderProvider3;
    private DelayedWakeLock_Builder_Factory builderProvider4;
    private Provider<StatusBarNotificationActivityStarter.Builder> builderProvider5;
    private AutoAddTracker_Builder_Factory builderProvider6;
    private Provider<BypassHeadsUpNotifier> bypassHeadsUpNotifierProvider;
    private CPUInfoTile_Factory cPUInfoTileProvider;
    private CaffeineTile_Factory caffeineTileProvider;
    private Provider<CastControllerImpl> castControllerImplProvider;
    private CastTile_Factory castTileProvider;
    private CellularTile_Factory cellularTileProvider;
    private Provider<ChannelEditorDialogController> channelEditorDialogControllerProvider;
    private Provider<ClockManager> clockManagerProvider;
    private ColorInversionTile_Factory colorInversionTileProvider;
    private CompassTile_Factory compassTileProvider;
    private Context context;
    private Provider<ContextComponentResolver> contextComponentResolverProvider;
    private Provider<Context> contextProvider;
    private Provider<ControlActionCoordinatorImpl> controlActionCoordinatorImplProvider;
    private Provider<ControlsBindingControllerImpl> controlsBindingControllerImplProvider;
    private Provider<ControlsComponent> controlsComponentProvider;
    private Provider<ControlsControllerImpl> controlsControllerImplProvider;
    private ControlsEditingActivity_Factory controlsEditingActivityProvider;
    private ControlsFavoritingActivity_Factory controlsFavoritingActivityProvider;
    private Provider<ControlsListingControllerImpl> controlsListingControllerImplProvider;
    private ControlsProviderSelectorActivity_Factory controlsProviderSelectorActivityProvider;
    private ControlsRequestDialog_Factory controlsRequestDialogProvider;
    private Provider<ControlsUiControllerImpl> controlsUiControllerImplProvider;
    private Provider<ConversationCoordinator> conversationCoordinatorProvider;
    private Provider<ConversationNotificationManager> conversationNotificationManagerProvider;
    private ConversationNotificationProcessor_Factory conversationNotificationProcessorProvider;
    private CreateUserActivity_Factory createUserActivityProvider;
    private Provider<CustomIconCache> customIconCacheProvider;
    private Provider<DarkIconDispatcherImpl> darkIconDispatcherImplProvider;
    private DataSaverTile_Factory dataSaverTileProvider;
    private DataSwitchTile_Factory dataSwitchTileProvider;
    private DateFormatUtil_Factory dateFormatUtilProvider;
    private DeleteScreenshotReceiver_Factory deleteScreenshotReceiverProvider;
    private Provider<DeviceConfigHelper> deviceConfigHelperProvider;
    private Provider<DeviceProvisionedControllerImpl> deviceProvisionedControllerImplProvider;
    private Provider<DeviceProvisionedCoordinator> deviceProvisionedCoordinatorProvider;
    private Provider<DismissCallbackRegistry> dismissCallbackRegistryProvider;
    private Provider<DisplayController> displayControllerProvider;
    private Provider<DisplayImeController> displayImeControllerProvider;
    private DndTile_Factory dndTileProvider;
    private Provider<DockManagerImpl> dockManagerImplProvider;
    private DozeFactory_Factory dozeFactoryProvider;
    private Provider<DozeLog> dozeLogProvider;
    private DozeLogger_Factory dozeLoggerProvider;
    private Provider<DozeParameters> dozeParametersProvider;
    private Provider<DozeScrimController> dozeScrimControllerProvider;
    private Provider<DozeServiceHost> dozeServiceHostProvider;
    private DozeService_Factory dozeServiceProvider;
    private DumpHandler_Factory dumpHandlerProvider;
    private Provider<DumpManager> dumpManagerProvider;
    private DynamicChildBindController_Factory dynamicChildBindControllerProvider;
    private Provider<DynamicPrivacyController> dynamicPrivacyControllerProvider;
    private Provider<EnhancedEstimatesImpl> enhancedEstimatesImplProvider;
    private Provider<ExpandableNotificationRowComponent.Builder> expandableNotificationRowComponentBuilderProvider;
    private NotificationLogger_ExpansionStateLogger_Factory expansionStateLoggerProvider;
    private Provider<ExtensionControllerImpl> extensionControllerImplProvider;
    private Provider<FODCircleViewImpl> fODCircleViewImplProvider;
    private FPSInfoTile_Factory fPSInfoTileProvider;
    private FalsingDataProvider_Factory falsingDataProvider;
    private Provider<FalsingManagerProxy> falsingManagerProxyProvider;
    private Provider<FeatureFlags> featureFlagsProvider;
    private Provider<Files> filesProvider;
    private Provider<FlashlightControllerImpl> flashlightControllerImplProvider;
    private FlashlightTile_Factory flashlightTileProvider;
    private Provider<FloatingContentCoordinator> floatingContentCoordinatorProvider;
    private Provider<ForegroundServiceController> foregroundServiceControllerProvider;
    private Provider<ForegroundServiceDismissalFeatureController> foregroundServiceDismissalFeatureControllerProvider;
    private ForegroundServiceLifetimeExtender_Factory foregroundServiceLifetimeExtenderProvider;
    private Provider<ForegroundServiceNotificationListener> foregroundServiceNotificationListenerProvider;
    private Provider<ForegroundServiceSectionController> foregroundServiceSectionControllerProvider;
    private Provider<FragmentService> fragmentServiceProvider;
    private GamingModeTile_Factory gamingModeTileProvider;
    private Provider<GarbageMonitor> garbageMonitorProvider;
    private Provider<GlobalActionsComponent> globalActionsComponentProvider;
    private GlobalActionsDialog_Factory globalActionsDialogProvider;
    private GlobalActionsImpl_Factory globalActionsImplProvider;
    private Provider<GlobalScreenshot> globalScreenshotProvider;
    private Provider<GoogleDefaultUiController> googleDefaultUiControllerProvider;
    private GroupCoalescerLogger_Factory groupCoalescerLoggerProvider;
    private GroupCoalescer_Factory groupCoalescerProvider;
    private Provider<HeadsUpController> headsUpControllerProvider;
    private Provider<HeadsUpCoordinator> headsUpCoordinatorProvider;
    private HeadsUpTile_Factory headsUpTileProvider;
    private Provider<HeadsUpViewBinder> headsUpViewBinderProvider;
    private HideNotifsForOtherUsersCoordinator_Factory hideNotifsForOtherUsersCoordinatorProvider;
    private Provider<HighPriorityProvider> highPriorityProvider;
    private Provider<HotspotControllerImpl> hotspotControllerImplProvider;
    private HotspotTile_Factory hotspotTileProvider;
    private IconBuilder_Factory iconBuilderProvider;
    private IconManager_Factory iconManagerProvider;
    private Provider<InitController> initControllerProvider;
    private Provider<InjectionInflationController> injectionInflationControllerProvider;
    private Provider<InstantAppNotifier> instantAppNotifierProvider;
    private Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private Provider<KeyguardCoordinator> keyguardCoordinatorProvider;
    private Provider<KeyguardDismissUtil> keyguardDismissUtilProvider;
    private Provider<KeyguardEnvironmentImpl> keyguardEnvironmentImplProvider;
    private Provider<KeyguardIndicationController> keyguardIndicationControllerProvider;
    private Provider<KeyguardLifecyclesDispatcher> keyguardLifecyclesDispatcherProvider;
    private Provider<KeyguardMediaController> keyguardMediaControllerProvider;
    private Provider<KeyguardSecurityModel> keyguardSecurityModelProvider;
    private KeyguardService_Factory keyguardServiceProvider;
    private Provider<KeyguardStateControllerImpl> keyguardStateControllerImplProvider;
    private Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private Provider<LatencyTester> latencyTesterProvider;
    private Provider<LeakReporter> leakReporterProvider;
    private Provider<LightBarController> lightBarControllerProvider;
    private Provider<LightsOutNotifController> lightsOutNotifControllerProvider;
    private LiveDisplayTile_Factory liveDisplayTileProvider;
    private LocalMediaManagerFactory_Factory localMediaManagerFactoryProvider;
    private LocaleTile_Factory localeTileProvider;
    private Provider<LocationControllerImpl> locationControllerImplProvider;
    private LocationTile_Factory locationTileProvider;
    private Provider<LockscreenGestureLogger> lockscreenGestureLoggerProvider;
    private Provider<LockscreenLockIconController> lockscreenLockIconControllerProvider;
    private Provider<LockscreenWallpaper> lockscreenWallpaperProvider;
    private Provider<LogBufferEulogizer> logBufferEulogizerProvider;
    private LogBufferFreezer_Factory logBufferFreezerProvider;
    private Provider<LowPriorityInflationHelper> lowPriorityInflationHelperProvider;
    private Provider<ManagedProfileControllerImpl> managedProfileControllerImplProvider;
    private Provider<Map<Class<?>, Provider<Activity>>> mapOfClassOfAndProviderOfActivityProvider;
    private Provider<Map<Class<?>, Provider<BroadcastReceiver>>> mapOfClassOfAndProviderOfBroadcastReceiverProvider;
    private Provider<Map<Class<?>, Provider<RecentsImplementation>>> mapOfClassOfAndProviderOfRecentsImplementationProvider;
    private Provider<Map<Class<?>, Provider<Service>>> mapOfClassOfAndProviderOfServiceProvider;
    private Provider<Map<Class<?>, Provider<SystemUI>>> mapOfClassOfAndProviderOfSystemUIProvider;
    private Provider<MediaArtworkProcessor> mediaArtworkProcessorProvider;
    private MediaBrowserFactory_Factory mediaBrowserFactoryProvider;
    private Provider<MediaCarouselController> mediaCarouselControllerProvider;
    private MediaControlPanel_Factory mediaControlPanelProvider;
    private MediaControllerFactory_Factory mediaControllerFactoryProvider;
    private MediaCoordinator_Factory mediaCoordinatorProvider;
    private MediaDataFilter_Factory mediaDataFilterProvider;
    private Provider<MediaDataManager> mediaDataManagerProvider;
    private MediaDeviceManager_Factory mediaDeviceManagerProvider;
    private MediaFeatureFlag_Factory mediaFeatureFlagProvider;
    private Provider<MediaHierarchyManager> mediaHierarchyManagerProvider;
    private MediaHost_Factory mediaHostProvider;
    private Provider<MediaHostStatesManager> mediaHostStatesManagerProvider;
    private MediaOutputDialogFactory_Factory mediaOutputDialogFactoryProvider;
    private MediaOutputDialogReceiver_Factory mediaOutputDialogReceiverProvider;
    private Provider<MediaResumeListener> mediaResumeListenerProvider;
    private MediaSessionBasedFilter_Factory mediaSessionBasedFilterProvider;
    private Provider<MediaTimeoutListener> mediaTimeoutListenerProvider;
    private MediaViewController_Factory mediaViewControllerProvider;
    private GarbageMonitor_MemoryTile_Factory memoryTileProvider;
    private Provider<NavigationModeController> navigationModeControllerProvider;
    private Provider<NetworkControllerImpl> networkControllerImplProvider;
    private Provider<BubbleController> newBubbleControllerProvider;
    private Provider<KeyguardViewMediator> newKeyguardViewMediatorProvider;
    private Provider<NextAlarmControllerImpl> nextAlarmControllerImplProvider;
    private NfcTile_Factory nfcTileProvider;
    private NightDisplayTile_Factory nightDisplayTileProvider;
    private NotifBindPipelineInitializer_Factory notifBindPipelineInitializerProvider;
    private NotifBindPipelineLogger_Factory notifBindPipelineLoggerProvider;
    private Provider<NotifBindPipeline> notifBindPipelineProvider;
    private NotifCollectionLogger_Factory notifCollectionLoggerProvider;
    private Provider<NotifCollection> notifCollectionProvider;
    private Provider<NotifCoordinators> notifCoordinatorsProvider;
    private Provider<NotifInflaterImpl> notifInflaterImplProvider;
    private Provider<NotifInflationErrorManager> notifInflationErrorManagerProvider;
    private Provider<NotifPipelineInitializer> notifPipelineInitializerProvider;
    private Provider<NotifPipeline> notifPipelineProvider;
    private NotifRemoteViewCacheImpl_Factory notifRemoteViewCacheImplProvider;
    private Provider<NotifViewBarn> notifViewBarnProvider;
    private Provider<NotifViewManager> notifViewManagerProvider;
    private Provider<NotificationClickNotifier> notificationClickNotifierProvider;
    private NotificationClickerLogger_Factory notificationClickerLoggerProvider;
    private Provider<NotificationContentInflater> notificationContentInflaterProvider;
    private NotificationEntryManagerLogger_Factory notificationEntryManagerLoggerProvider;
    private Provider<NotificationFilter> notificationFilterProvider;
    private Provider<NotificationGroupManager> notificationGroupManagerProvider;
    private Provider<NotificationInteractionTracker> notificationInteractionTrackerProvider;
    private Provider<NotificationInterruptStateProviderImpl> notificationInterruptStateProviderImplProvider;
    private Provider<NotificationLockscreenUserManagerImpl> notificationLockscreenUserManagerImplProvider;
    private Provider<NotificationPersonExtractorPluginBoundary> notificationPersonExtractorPluginBoundaryProvider;
    private NotificationRankingManager_Factory notificationRankingManagerProvider;
    private Provider<NotificationRoundnessManager> notificationRoundnessManagerProvider;
    private Provider<NotificationRowBinderImpl> notificationRowBinderImplProvider;
    private Provider<NotificationRowComponent.Builder> notificationRowComponentBuilderProvider;
    private NotificationSectionsFeatureManager_Factory notificationSectionsFeatureManagerProvider;
    private Provider<NotificationSectionsLogger> notificationSectionsLoggerProvider;
    private Provider<NotificationShadeDepthController> notificationShadeDepthControllerProvider;
    private Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private Provider<NotificationWakeUpCoordinator> notificationWakeUpCoordinatorProvider;
    private Provider<NotificationsControllerImpl> notificationsControllerImplProvider;
    private NotificationsControllerStub_Factory notificationsControllerStubProvider;
    private OnTheGoTile_Factory onTheGoTileProvider;
    private Provider<OpaEnabledDispatcher> opaEnabledDispatcherProvider;
    private Provider<Optional<ControlsFavoritePersistenceWrapper>> optionalOfControlsFavoritePersistenceWrapperProvider;
    private Provider<Optional<Divider>> optionalOfDividerProvider;
    private Provider<Optional<Lazy<Recents>>> optionalOfLazyOfRecentsProvider;
    private Provider<Optional<Lazy<StatusBar>>> optionalOfLazyOfStatusBarProvider;
    private Provider<Optional<Recents>> optionalOfRecentsProvider;
    private Provider<Optional<StatusBar>> optionalOfStatusBarProvider;
    private Provider<OverviewProxyRecentsImpl> overviewProxyRecentsImplProvider;
    private Provider<OverviewProxyService> overviewProxyServiceProvider;
    private Provider<PeopleHubDataSourceImpl> peopleHubDataSourceImplProvider;
    private Provider<PeopleHubViewAdapterImpl> peopleHubViewAdapterImplProvider;
    private Provider<PeopleHubViewModelFactoryDataSourceImpl> peopleHubViewModelFactoryDataSourceImplProvider;
    private Provider<PeopleNotificationIdentifierImpl> peopleNotificationIdentifierImplProvider;
    private Provider<PermissionFlagsCache> permissionFlagsCacheProvider;
    private Provider<PhoneStateMonitor> phoneStateMonitorProvider;
    private PhoneStatusBarPolicy_Factory phoneStatusBarPolicyProvider;
    private PipAnimationController_Factory pipAnimationControllerProvider;
    private Provider<PipBoundsHandler> pipBoundsHandlerProvider;
    private Provider<PipManager> pipManagerProvider;
    private PipMenuActivity_Factory pipMenuActivityProvider;
    private PipSnapAlgorithm_Factory pipSnapAlgorithmProvider;
    private Provider<PipSurfaceTransactionHelper> pipSurfaceTransactionHelperProvider;
    private Provider<PipTaskOrganizer> pipTaskOrganizerProvider;
    private Provider<PipUI> pipUIProvider;
    private Provider<PipUiEventLogger> pipUiEventLoggerProvider;
    private Provider<PluginDependencyProvider> pluginDependencyProvider;
    private PowerMenuTile_Factory powerMenuTileProvider;
    private Provider<PowerNotificationWarnings> powerNotificationWarningsProvider;
    private PowerShareTile_Factory powerShareTileProvider;
    private Provider<PowerUI> powerUIProvider;
    private PreparationCoordinatorLogger_Factory preparationCoordinatorLoggerProvider;
    private Provider<PreparationCoordinator> preparationCoordinatorProvider;
    private Provider<PrivacyItemController> privacyItemControllerProvider;
    private ProfilesTile_Factory profilesTileProvider;
    private Provider<ProtoTracer> protoTracerProvider;
    private Provider<AccessibilityManager> provideAccessibilityManagerProvider;
    private Provider<ActivityManager> provideActivityManagerProvider;
    private Provider<ActivityManagerWrapper> provideActivityManagerWrapperProvider;
    private Provider<AlarmManager> provideAlarmManagerProvider;
    private Provider<Boolean> provideAllowNotificationLongPressProvider;
    private DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory provideAlwaysOnDisplayPolicyProvider;
    private DependencyProvider_ProvideAmbientDisplayConfigurationFactory provideAmbientDisplayConfigurationProvider;
    private Provider<AppLockManager> provideAppLockManagerProvider;
    private Provider provideAssistHandleBehaviorControllerMapProvider;
    private AssistModule_ProvideAssistHandleViewControllerFactory provideAssistHandleViewControllerProvider;
    private Provider<AssistUtils> provideAssistUtilsProvider;
    private Provider<AudioManager> provideAudioManagerProvider;
    private Provider<AutoHideController> provideAutoHideControllerProvider;
    private QSModule_ProvideAutoTileManagerFactory provideAutoTileManagerProvider;
    private Provider<DelayableExecutor> provideBackgroundDelayableExecutorProvider;
    private Provider<Executor> provideBackgroundExecutorProvider;
    private Provider<Handler> provideBackgroundHandlerProvider;
    private Provider<RepeatableExecutor> provideBackgroundRepeatableExecutorProvider;
    private Provider<BatteryController> provideBatteryControllerProvider;
    private ConcurrencyModule_ProvideBgHandlerFactory provideBgHandlerProvider;
    private Provider<Looper> provideBgLooperProvider;
    private Provider<LogBuffer> provideBroadcastDispatcherLogBufferProvider;
    private Provider<CommandQueue> provideCommandQueueProvider;
    private Provider<CommonNotifCollection> provideCommonNotifCollectionProvider;
    private Provider<ConfigurationController> provideConfigurationControllerProvider;
    private Provider<ConnectivityManager> provideConnectivityManagagerProvider;
    private Provider<ContentResolver> provideContentResolverProvider;
    private Provider<CurrentUserContextTracker> provideCurrentUserContextTrackerProvider;
    private Provider<DataSaverController> provideDataSaverControllerProvider;
    private Provider<DelayableExecutor> provideDelayableExecutorProvider;
    private Provider<DevicePolicyManager> provideDevicePolicyManagerProvider;
    private Provider<DevicePolicyManagerWrapper> provideDevicePolicyManagerWrapperProvider;
    private SystemServicesModule_ProvideDisplayIdFactory provideDisplayIdProvider;
    private Provider<DisplayMetrics> provideDisplayMetricsProvider;
    private Provider<Divider> provideDividerProvider;
    private Provider<LogBuffer> provideDozeLogBufferProvider;
    private UserModule_ProvideEditUserInfoControllerFactory provideEditUserInfoControllerProvider;
    private Provider<Execution> provideExecutionProvider;
    private Provider<Executor> provideExecutorProvider;
    private Provider<HeadsUpManagerPhone> provideHeadsUpManagerPhoneProvider;
    private Provider<IActivityManager> provideIActivityManagerProvider;
    private Provider<IActivityTaskManager> provideIActivityTaskManagerProvider;
    private Provider<IBatteryStats> provideIBatteryStatsProvider;
    private Provider<IDreamManager> provideIDreamManagerProvider;
    private Provider<INotificationManager> provideINotificationManagerProvider;
    private Provider<IPackageManager> provideIPackageManagerProvider;
    private Provider<IStatusBarService> provideIStatusBarServiceProvider;
    private Provider<IWindowManager> provideIWindowManagerProvider;
    private Provider<KeyguardLiftController> provideKeyguardLiftControllerProvider;
    private Provider<KeyguardManager> provideKeyguardManagerProvider;
    private Provider<LatencyTracker> provideLatencyTrackerProvider;
    private Provider<LauncherApps> provideLauncherAppsProvider;
    private Provider<LeakDetector> provideLeakDetectorProvider;
    private Provider<String> provideLeakReportEmailProvider;
    private Provider<LocalBluetoothManager> provideLocalBluetoothControllerProvider;
    private DependencyProvider_ProvideLockPatternUtilsFactory provideLockPatternUtilsProvider;
    private Provider<LogcatEchoTracker> provideLogcatEchoTrackerProvider;
    private Provider<Executor> provideLongRunningExecutorProvider;
    private Provider<Looper> provideLongRunningLooperProvider;
    private Provider<DelayableExecutor> provideMainDelayableExecutorProvider;
    private ConcurrencyModule_ProvideMainExecutorFactory provideMainExecutorProvider;
    private ConcurrencyModule_ProvideMainHandlerFactory provideMainHandlerProvider;
    private SystemServicesModule_ProvideMediaRouter2ManagerFactory provideMediaRouter2ManagerProvider;
    private SystemServicesModule_ProvideMediaSessionManagerFactory provideMediaSessionManagerProvider;
    private Provider<MetricsLogger> provideMetricsLoggerProvider;
    private Provider<NavigationBarController> provideNavigationBarControllerProvider;
    private Provider<NetworkScoreManager> provideNetworkScoreManagerProvider;
    private Provider<NightDisplayListener> provideNightDisplayListenerProvider;
    private Provider<LogBuffer> provideNotifInteractionLogBufferProvider;
    private Provider<NotifRemoteViewCache> provideNotifRemoteViewCacheProvider;
    private Provider<NotificationBlockingHelperManager> provideNotificationBlockingHelperManagerProvider;
    private Provider<NotificationEntryManager> provideNotificationEntryManagerProvider;
    private Provider<NotificationGroupAlertTransferHelper> provideNotificationGroupAlertTransferHelperProvider;
    private Provider<NotificationGutsManager> provideNotificationGutsManagerProvider;
    private Provider<NotificationListener> provideNotificationListenerProvider;
    private Provider<NotificationLogger> provideNotificationLoggerProvider;
    private Provider<NotificationManager> provideNotificationManagerProvider;
    private Provider<NotificationMediaManager> provideNotificationMediaManagerProvider;
    private DependencyProvider_ProvideNotificationMessagingUtilFactory provideNotificationMessagingUtilProvider;
    private Provider<NotificationPanelLogger> provideNotificationPanelLoggerProvider;
    private Provider<NotificationRemoteInputManager> provideNotificationRemoteInputManagerProvider;
    private Provider<LogBuffer> provideNotificationSectionLogBufferProvider;
    private Provider<NotificationViewHierarchyManager> provideNotificationViewHierarchyManagerProvider;
    private Provider<NotificationsController> provideNotificationsControllerProvider;
    private Provider<LogBuffer> provideNotificationsLogBufferProvider;
    private Provider<PackageManager> providePackageManagerProvider;
    private Provider<PackageManagerWrapper> providePackageManagerWrapperProvider;
    private Provider<PluginManager> providePluginManagerProvider;
    private Provider<PowerManager> providePowerManagerProvider;
    private SensorModule_ProvidePrimaryProxSensorFactory providePrimaryProxSensorProvider;
    private Provider<PulseController> providePulseControllerProvider;
    private Provider<LogBuffer> provideQuickSettingsLogBufferProvider;
    private RecentsModule_ProvideRecentsImplFactory provideRecentsImplProvider;
    private Provider<Recents> provideRecentsProvider;
    private SystemServicesModule_ProvideResourcesFactory provideResourcesProvider;
    private SensorModule_ProvideSecondaryProxSensorFactory provideSecondaryProxSensorProvider;
    private Provider<SensorPrivacyManager> provideSensorPrivacyManagerProvider;
    private DependencyProvider_ProvideSharePreferencesFactory provideSharePreferencesProvider;
    private Provider<ShortcutManager> provideShortcutManagerProvider;
    private Provider<SmartReplyController> provideSmartReplyControllerProvider;
    private Provider<StatusBar> provideStatusBarProvider;
    private Provider<SysUiState> provideSysUiStateProvider;
    private Provider<Clock> provideSystemClockProvider;
    private Provider<TelecomManager> provideTelecomManagerProvider;
    private Provider<TelephonyManager> provideTelephonyManagerProvider;
    private Provider<Handler> provideTimeTickHandlerProvider;
    private Provider<TrustManager> provideTrustManagerProvider;
    private Provider<Executor> provideUiBackgroundExecutorProvider;
    private Provider<UiEventLogger> provideUiEventLoggerProvider;
    private Provider<UserManager> provideUserManagerProvider;
    private Provider<Vibrator> provideVibratorProvider;
    private Provider<VisualStabilityManager> provideVisualStabilityManagerProvider;
    private SystemServicesModule_ProvideWallpaperManagerFactory provideWallpaperManagerProvider;
    private Provider<WifiManager> provideWifiManagerProvider;
    private Provider<WindowManager> provideWindowManagerProvider;
    private Provider<LayoutInflater> providerLayoutInflaterProvider;
    private Provider<BroadcastDispatcher> providesBroadcastDispatcherProvider;
    private Provider<Choreographer> providesChoreographerProvider;
    private Provider<Boolean> providesControlsFeatureEnabledProvider;
    private Provider<SensorManager> providesSensorManagerProvider;
    private DependencyProvider_ProvidesViewMediatorCallbackFactory providesViewMediatorCallbackProvider;
    private ProximitySensor_ProximityCheck_Factory proximityCheckProvider;
    private ProximitySensor_Factory proximitySensorProvider;
    private Provider<PulseExpansionHandler> pulseExpansionHandlerProvider;
    private Provider<QSFactoryImpl> qSFactoryImplProvider;
    private QSLogger_Factory qSLoggerProvider;
    private Provider<QSTileHost> qSTileHostProvider;
    private Provider<RankingCoordinator> rankingCoordinatorProvider;
    private ReadingModeTile_Factory readingModeTileProvider;
    private RebootTile_Factory rebootTileProvider;
    private Provider<RecordingController> recordingControllerProvider;
    private RecordingService_Factory recordingServiceProvider;
    private Provider<RemoteInputQuickSettingsDisabler> remoteInputQuickSettingsDisablerProvider;
    private Provider<RemoteInputUriController> remoteInputUriControllerProvider;
    private ResumeMediaBrowserFactory_Factory resumeMediaBrowserFactoryProvider;
    private Provider<RingerModeTrackerImpl> ringerModeTrackerImplProvider;
    private Provider<RotationLockControllerImpl> rotationLockControllerImplProvider;
    private RotationLockTile_Factory rotationLockTileProvider;
    private RowContentBindStageLogger_Factory rowContentBindStageLoggerProvider;
    private Provider<RowContentBindStage> rowContentBindStageProvider;
    private Provider<ScreenDecorations> screenDecorationsProvider;
    private Provider<ScreenLifecycle> screenLifecycleProvider;
    private ScreenPinningRequest_Factory screenPinningRequestProvider;
    private ScreenRecordDialog_Factory screenRecordDialogProvider;
    private ScreenRecordTile_Factory screenRecordTileProvider;
    private ScreenshotNotificationsController_Factory screenshotNotificationsControllerProvider;
    private Provider<ScreenshotSmartActions> screenshotSmartActionsProvider;
    private Provider<ScrimController> scrimControllerProvider;
    private Provider<SecurityControllerImpl> securityControllerImplProvider;
    private SeekBarViewModel_Factory seekBarViewModelProvider;
    private Provider<SensorPrivacyControllerImpl> sensorPrivacyControllerImplProvider;
    private SensorPrivacyTile_Factory sensorPrivacyTileProvider;
    private Provider<GarbageMonitor.Service> serviceProvider;
    private Provider<ShadeControllerImpl> shadeControllerImplProvider;
    private ShadeListBuilderLogger_Factory shadeListBuilderLoggerProvider;
    private Provider<ShadeListBuilder> shadeListBuilderProvider;
    private SharedCoordinatorLogger_Factory sharedCoordinatorLoggerProvider;
    private Provider<ShortcutKeyDispatcher> shortcutKeyDispatcherProvider;
    private Provider<SizeCompatModeActivityController> sizeCompatModeActivityControllerProvider;
    private SleepModeTile_Factory sleepModeTileProvider;
    private Provider<SliceBroadcastRelayHandler> sliceBroadcastRelayHandlerProvider;
    private SmartActionsReceiver_Factory smartActionsReceiverProvider;
    private SmartPixelsTile_Factory smartPixelsTileProvider;
    private Provider<SmartReplyConstants> smartReplyConstantsProvider;
    private SoundTile_Factory soundTileProvider;
    private Provider<StatusBarComponent.Builder> statusBarComponentBuilderProvider;
    private Provider<StatusBarIconControllerImpl> statusBarIconControllerImplProvider;
    private Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private StatusBarNotificationActivityStarterLogger_Factory statusBarNotificationActivityStarterLoggerProvider;
    private Provider<StatusBarRemoteInputCallback> statusBarRemoteInputCallbackProvider;
    private Provider<StatusBarStateControllerImpl> statusBarStateControllerImplProvider;
    private Provider<StatusBarTouchableRegionManager> statusBarTouchableRegionManagerProvider;
    private Provider<StatusBarWindowController> statusBarWindowControllerProvider;
    private Provider<SuperStatusBarViewFactory> superStatusBarViewFactoryProvider;
    private SyncTile_Factory syncTileProvider;
    private Provider<SystemActions> systemActionsProvider;
    private SystemUIAuxiliaryDumpService_Factory systemUIAuxiliaryDumpServiceProvider;
    private SystemUIService_Factory systemUIServiceProvider;
    private Provider<SystemWindows> systemWindowsProvider;
    private Provider<SysuiColorExtractor> sysuiColorExtractorProvider;
    private TakeScreenshotService_Factory takeScreenshotServiceProvider;
    private Provider<TargetSdkResolver> targetSdkResolverProvider;
    private Provider<ThemeOverlayController> themeOverlayControllerProvider;
    private Provider<ToastUI> toastUIProvider;
    private Provider<TransactionPool> transactionPoolProvider;
    private Provider<TunablePadding.TunablePaddingService> tunablePaddingServiceProvider;
    private Provider<TunerServiceImpl> tunerServiceImplProvider;
    private Provider<TvPipComponent.Builder> tvPipComponentBuilderProvider;
    private Provider<TvStatusBar> tvStatusBarProvider;
    private Provider<TvSystemUIRootComponent> tvSystemUIRootComponentProvider;
    private UiModeNightTile_Factory uiModeNightTileProvider;
    private Provider<UiOffloadThread> uiOffloadThreadProvider;
    private UsbDebuggingActivity_Factory usbDebuggingActivityProvider;
    private UsbDebuggingSecondaryUserActivity_Factory usbDebuggingSecondaryUserActivityProvider;
    private UsbTetherTile_Factory usbTetherTileProvider;
    private UserCreator_Factory userCreatorProvider;
    private Provider<UserInfoControllerImpl> userInfoControllerImplProvider;
    private Provider<UserSwitcherController> userSwitcherControllerProvider;
    private UserTile_Factory userTileProvider;
    private Provider<VibratorHelper> vibratorHelperProvider;
    private Provider<VolumeDialogComponent> volumeDialogComponentProvider;
    private Provider<VolumeDialogControllerImpl> volumeDialogControllerImplProvider;
    private VolumeTile_Factory volumeTileProvider;
    private Provider<VolumeUI> volumeUIProvider;
    private VpnTile_Factory vpnTileProvider;
    private Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;
    private WeatherTile_Factory weatherTileProvider;
    private WifiTile_Factory wifiTileProvider;
    private Provider<WindowMagnification> windowMagnificationProvider;
    private WorkLockActivity_Factory workLockActivityProvider;
    private WorkModeTile_Factory workModeTileProvider;
    private Provider<ZenModeControllerImpl> zenModeControllerImplProvider;

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(ContentProvider contentProvider) {
    }

    private DaggerTvSystemUIRootComponent(Builder builder) {
        initialize(builder);
        initialize2(builder);
        initialize3(builder);
        initialize4(builder);
        initialize5(builder);
    }

    public static TvSystemUIRootComponent.Builder builder() {
        return new Builder();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Handler getMainHandler() {
        return ConcurrencyModule_ProvideMainHandlerFactory.proxyProvideMainHandler(ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Handler getBackgroundHandler() {
        return ConcurrencyModule_ProvideBgHandlerFactory.proxyProvideBgHandler(this.provideBgLooperProvider.get());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Resources getMainResources() {
        return SystemServicesModule_ProvideResourcesFactory.proxyProvideResources(this.context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public NotificationSectionsFeatureManager getNotificationSectionsFeatureManager() {
        return new NotificationSectionsFeatureManager(new DeviceConfigProxy(), this.context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public QSLogger getQSLogger() {
        return new QSLogger(this.provideQuickSettingsLogBufferProvider.get());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public MediaHost getMediaHost() {
        return new MediaHost(new MediaHost.MediaHostStateHolder(), this.mediaHierarchyManagerProvider.get(), this.mediaDataManagerProvider.get(), this.mediaHostStatesManagerProvider.get());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Executor getMainExecutor() {
        return ConcurrencyModule_ProvideMainExecutorFactory.proxyProvideMainExecutor(this.context);
    }

    private void initialize(Builder builder) {
        Provider<DumpManager> provider = DoubleCheck.provider(DumpManager_Factory.create());
        this.dumpManagerProvider = provider;
        this.bootCompleteCacheImplProvider = DoubleCheck.provider(BootCompleteCacheImpl_Factory.create(provider));
        this.contextProvider = InstanceFactory.create(builder.context);
        this.provideConfigurationControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideConfigurationControllerFactory.create(builder.dependencyProvider, this.contextProvider));
        Provider<Looper> provider2 = DoubleCheck.provider(ConcurrencyModule_ProvideBgLooperFactory.create());
        this.provideBgLooperProvider = provider2;
        this.provideBackgroundExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundExecutorFactory.create(provider2));
        Provider<ContentResolver> provider3 = DoubleCheck.provider(SystemServicesModule_ProvideContentResolverFactory.create(this.contextProvider));
        this.provideContentResolverProvider = provider3;
        Provider<LogcatEchoTracker> provider4 = DoubleCheck.provider(LogModule_ProvideLogcatEchoTrackerFactory.create(provider3, ConcurrencyModule_ProvideMainLooperFactory.create()));
        this.provideLogcatEchoTrackerProvider = provider4;
        Provider<LogBuffer> provider5 = DoubleCheck.provider(LogModule_ProvideBroadcastDispatcherLogBufferFactory.create(provider4, this.dumpManagerProvider));
        this.provideBroadcastDispatcherLogBufferProvider = provider5;
        this.broadcastDispatcherLoggerProvider = BroadcastDispatcherLogger_Factory.create(provider5);
        Provider<BroadcastDispatcher> provider6 = DoubleCheck.provider(DependencyProvider_ProvidesBroadcastDispatcherFactory.create(builder.dependencyProvider, this.contextProvider, this.provideBgLooperProvider, this.provideBackgroundExecutorProvider, this.dumpManagerProvider, this.broadcastDispatcherLoggerProvider));
        this.providesBroadcastDispatcherProvider = provider6;
        this.workLockActivityProvider = WorkLockActivity_Factory.create(provider6);
        this.brightnessDialogProvider = BrightnessDialog_Factory.create(this.providesBroadcastDispatcherProvider);
        this.recordingControllerProvider = DoubleCheck.provider(RecordingController_Factory.create(this.providesBroadcastDispatcherProvider));
        Provider<CurrentUserContextTracker> provider7 = DoubleCheck.provider(SettingsModule_ProvideCurrentUserContextTrackerFactory.create(this.contextProvider, this.providesBroadcastDispatcherProvider));
        this.provideCurrentUserContextTrackerProvider = provider7;
        this.screenRecordDialogProvider = ScreenRecordDialog_Factory.create(this.recordingControllerProvider, provider7);
        this.provideWindowManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideWindowManagerFactory.create(this.contextProvider));
        this.provideIActivityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIActivityManagerFactory.create());
        this.provideResourcesProvider = SystemServicesModule_ProvideResourcesFactory.create(this.contextProvider);
        this.provideAmbientDisplayConfigurationProvider = DependencyProvider_ProvideAmbientDisplayConfigurationFactory.create(builder.dependencyProvider, this.contextProvider);
        this.provideAlwaysOnDisplayPolicyProvider = DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory.create(builder.dependencyProvider, this.contextProvider);
        this.providePowerManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvidePowerManagerFactory.create(this.contextProvider));
        this.provideMainHandlerProvider = ConcurrencyModule_ProvideMainHandlerFactory.create(ConcurrencyModule_ProvideMainLooperFactory.create());
        Provider<LeakDetector> provider8 = DoubleCheck.provider(DependencyProvider_ProvideLeakDetectorFactory.create(builder.dependencyProvider));
        this.provideLeakDetectorProvider = provider8;
        Provider<TunerServiceImpl> provider9 = DoubleCheck.provider(TunerServiceImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, provider8, this.providesBroadcastDispatcherProvider));
        this.tunerServiceImplProvider = provider9;
        this.dozeParametersProvider = DoubleCheck.provider(DozeParameters_Factory.create(this.provideResourcesProvider, this.provideAmbientDisplayConfigurationProvider, this.provideAlwaysOnDisplayPolicyProvider, this.providePowerManagerProvider, provider9));
        Provider<UiEventLogger> provider10 = DoubleCheck.provider(DependencyProvider_ProvideUiEventLoggerFactory.create());
        this.provideUiEventLoggerProvider = provider10;
        this.statusBarStateControllerImplProvider = DoubleCheck.provider(StatusBarStateControllerImpl_Factory.create(provider10));
        this.providePluginManagerProvider = DoubleCheck.provider(DependencyProvider_ProvidePluginManagerFactory.create(builder.dependencyProvider, this.contextProvider));
        this.provideMainExecutorProvider = ConcurrencyModule_ProvideMainExecutorFactory.create(this.contextProvider);
        Provider<SensorManager> provider11 = DoubleCheck.provider(SystemServicesModule_ProvidesSensorManagerFactory.create(this.contextProvider));
        this.providesSensorManagerProvider = provider11;
        this.asyncSensorManagerProvider = DoubleCheck.provider(AsyncSensorManager_Factory.create(provider11, this.providePluginManagerProvider));
        Provider<Execution> provider12 = DoubleCheck.provider(ExecutionImpl_Factory.create());
        this.provideExecutionProvider = provider12;
        ThresholdSensorImpl_Builder_Factory thresholdSensorImpl_Builder_FactoryCreate = ThresholdSensorImpl_Builder_Factory.create(this.provideResourcesProvider, this.asyncSensorManagerProvider, provider12);
        this.builderProvider = thresholdSensorImpl_Builder_FactoryCreate;
        this.providePrimaryProxSensorProvider = SensorModule_ProvidePrimaryProxSensorFactory.create(this.providesSensorManagerProvider, thresholdSensorImpl_Builder_FactoryCreate);
        this.provideSecondaryProxSensorProvider = SensorModule_ProvideSecondaryProxSensorFactory.create(this.builderProvider);
        Provider<DelayableExecutor> provider13 = DoubleCheck.provider(ConcurrencyModule_ProvideMainDelayableExecutorFactory.create(ConcurrencyModule_ProvideMainLooperFactory.create()));
        this.provideMainDelayableExecutorProvider = provider13;
        this.proximitySensorProvider = ProximitySensor_Factory.create(this.providePrimaryProxSensorProvider, this.provideSecondaryProxSensorProvider, provider13, this.provideExecutionProvider);
        this.dockManagerImplProvider = DoubleCheck.provider(DockManagerImpl_Factory.create());
        Provider<AudioManager> provider14 = DoubleCheck.provider(SystemServicesModule_ProvideAudioManagerFactory.create(this.contextProvider));
        this.provideAudioManagerProvider = provider14;
        this.ringerModeTrackerImplProvider = DoubleCheck.provider(RingerModeTrackerImpl_Factory.create(provider14, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider));
        this.provideLockPatternUtilsProvider = DependencyProvider_ProvideLockPatternUtilsFactory.create(builder.dependencyProvider, this.contextProvider);
        this.keyguardUpdateMonitorProvider = DoubleCheck.provider(KeyguardUpdateMonitor_Factory.create(this.contextProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.providesBroadcastDispatcherProvider, this.dumpManagerProvider, this.ringerModeTrackerImplProvider, this.provideBackgroundExecutorProvider, this.statusBarStateControllerImplProvider, this.provideLockPatternUtilsProvider));
        this.provideUiBackgroundExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideUiBackgroundExecutorFactory.create());
        this.provideDisplayMetricsProvider = DoubleCheck.provider(DependencyProvider_ProvideDisplayMetricsFactory.create(builder.dependencyProvider, this.contextProvider, this.provideWindowManagerProvider));
        this.enhancedEstimatesImplProvider = DoubleCheck.provider(EnhancedEstimatesImpl_Factory.create(this.contextProvider));
        ConcurrencyModule_ProvideBgHandlerFactory concurrencyModule_ProvideBgHandlerFactoryCreate = ConcurrencyModule_ProvideBgHandlerFactory.create(this.provideBgLooperProvider);
        this.provideBgHandlerProvider = concurrencyModule_ProvideBgHandlerFactoryCreate;
        Provider<BatteryController> provider15 = DoubleCheck.provider(SystemUIDefaultModule_ProvideBatteryControllerFactory.create(this.contextProvider, this.enhancedEstimatesImplProvider, this.providePowerManagerProvider, this.providesBroadcastDispatcherProvider, this.provideMainHandlerProvider, concurrencyModule_ProvideBgHandlerFactoryCreate));
        this.provideBatteryControllerProvider = provider15;
        this.falsingDataProvider = FalsingDataProvider_Factory.create(this.provideDisplayMetricsProvider, provider15);
        this.falsingManagerProxyProvider = DoubleCheck.provider(FalsingManagerProxy_Factory.create(this.contextProvider, this.providePluginManagerProvider, this.provideMainExecutorProvider, this.proximitySensorProvider, DeviceConfigProxy_Factory.create(), this.dockManagerImplProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.provideUiBackgroundExecutorProvider, this.statusBarStateControllerImplProvider, this.falsingDataProvider));
        this.newKeyguardViewMediatorProvider = new DelegateFactory();
        this.providesViewMediatorCallbackProvider = DependencyProvider_ProvidesViewMediatorCallbackFactory.create(builder.dependencyProvider, this.newKeyguardViewMediatorProvider);
        Provider<DeviceProvisionedControllerImpl> provider16 = DoubleCheck.provider(DeviceProvisionedControllerImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.deviceProvisionedControllerImplProvider = provider16;
        this.navigationModeControllerProvider = DoubleCheck.provider(NavigationModeController_Factory.create(this.contextProvider, provider16, this.provideConfigurationControllerProvider, this.provideUiBackgroundExecutorProvider));
        this.notificationShadeWindowControllerProvider = new DelegateFactory();
        this.keyguardStateControllerImplProvider = DoubleCheck.provider(KeyguardStateControllerImpl_Factory.create(this.contextProvider, this.keyguardUpdateMonitorProvider, this.provideLockPatternUtilsProvider));
        this.featureFlagsProvider = DoubleCheck.provider(FeatureFlags_Factory.create(this.provideBackgroundExecutorProvider));
        Provider<NotificationManager> provider17 = DoubleCheck.provider(SystemServicesModule_ProvideNotificationManagerFactory.create(this.contextProvider));
        this.provideNotificationManagerProvider = provider17;
        this.provideNotificationListenerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationListenerFactory.create(this.contextProvider, provider17, this.provideMainHandlerProvider));
        this.provideAppLockManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAppLockManagerFactory.create(this.contextProvider));
        Provider<LogBuffer> provider18 = DoubleCheck.provider(LogModule_ProvideNotificationsLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotificationsLogBufferProvider = provider18;
        this.notificationEntryManagerLoggerProvider = NotificationEntryManagerLogger_Factory.create(provider18);
        Provider<ExtensionControllerImpl> provider19 = DoubleCheck.provider(ExtensionControllerImpl_Factory.create(this.contextProvider, this.provideLeakDetectorProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.provideConfigurationControllerProvider));
        this.extensionControllerImplProvider = provider19;
        this.notificationPersonExtractorPluginBoundaryProvider = DoubleCheck.provider(NotificationPersonExtractorPluginBoundary_Factory.create(provider19));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.notificationGroupManagerProvider = delegateFactory;
        Provider<PeopleNotificationIdentifierImpl> provider20 = DoubleCheck.provider(PeopleNotificationIdentifierImpl_Factory.create(this.notificationPersonExtractorPluginBoundaryProvider, delegateFactory));
        this.peopleNotificationIdentifierImplProvider = provider20;
        DelegateFactory delegateFactory2 = (DelegateFactory) this.notificationGroupManagerProvider;
        Provider<NotificationGroupManager> provider21 = DoubleCheck.provider(NotificationGroupManager_Factory.create(this.statusBarStateControllerImplProvider, provider20));
        this.notificationGroupManagerProvider = provider21;
        delegateFactory2.setDelegatedProvider(provider21);
        this.provideNotificationMediaManagerProvider = new DelegateFactory();
        this.provideDevicePolicyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideDevicePolicyManagerFactory.create(this.contextProvider));
        this.provideUserManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideUserManagerFactory.create(this.contextProvider));
        Provider<IStatusBarService> provider22 = DoubleCheck.provider(SystemServicesModule_ProvideIStatusBarServiceFactory.create());
        this.provideIStatusBarServiceProvider = provider22;
        this.notificationClickNotifierProvider = DoubleCheck.provider(NotificationClickNotifier_Factory.create(provider22, this.provideMainExecutorProvider));
        Provider<KeyguardManager> provider23 = DoubleCheck.provider(SystemServicesModule_ProvideKeyguardManagerFactory.create(this.contextProvider));
        this.provideKeyguardManagerProvider = provider23;
        Provider<NotificationLockscreenUserManagerImpl> provider24 = DoubleCheck.provider(NotificationLockscreenUserManagerImpl_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideDevicePolicyManagerProvider, this.provideUserManagerProvider, this.notificationClickNotifierProvider, provider23, this.statusBarStateControllerImplProvider, this.provideMainHandlerProvider, this.deviceProvisionedControllerImplProvider, this.keyguardStateControllerImplProvider));
        this.notificationLockscreenUserManagerImplProvider = provider24;
        Provider<KeyguardBypassController> provider25 = DoubleCheck.provider(KeyguardBypassController_Factory.create(this.contextProvider, this.tunerServiceImplProvider, this.statusBarStateControllerImplProvider, provider24, this.keyguardStateControllerImplProvider, this.dumpManagerProvider));
        this.keyguardBypassControllerProvider = provider25;
        this.provideHeadsUpManagerPhoneProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory.create(this.contextProvider, this.statusBarStateControllerImplProvider, provider25, this.notificationGroupManagerProvider, this.provideConfigurationControllerProvider));
        MediaFeatureFlag_Factory mediaFeatureFlag_FactoryCreate = MediaFeatureFlag_Factory.create(this.contextProvider);
        this.mediaFeatureFlagProvider = mediaFeatureFlag_FactoryCreate;
        this.notificationFilterProvider = DoubleCheck.provider(NotificationFilter_Factory.create(this.statusBarStateControllerImplProvider, mediaFeatureFlag_FactoryCreate));
        this.notificationSectionsFeatureManagerProvider = NotificationSectionsFeatureManager_Factory.create(DeviceConfigProxy_Factory.create(), this.contextProvider);
        Provider<HighPriorityProvider> provider26 = DoubleCheck.provider(HighPriorityProvider_Factory.create(this.peopleNotificationIdentifierImplProvider, this.notificationGroupManagerProvider));
        this.highPriorityProvider = provider26;
        this.notificationRankingManagerProvider = NotificationRankingManager_Factory.create(this.provideNotificationMediaManagerProvider, this.notificationGroupManagerProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationFilterProvider, this.notificationEntryManagerLoggerProvider, this.notificationSectionsFeatureManagerProvider, this.peopleNotificationIdentifierImplProvider, provider26);
        this.keyguardEnvironmentImplProvider = DoubleCheck.provider(KeyguardEnvironmentImpl_Factory.create());
        this.provideNotificationMessagingUtilProvider = DependencyProvider_ProvideNotificationMessagingUtilFactory.create(builder.dependencyProvider, this.contextProvider);
        DelegateFactory delegateFactory3 = new DelegateFactory();
        this.provideNotificationEntryManagerProvider = delegateFactory3;
        this.provideSmartReplyControllerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideSmartReplyControllerFactory.create(delegateFactory3, this.provideIStatusBarServiceProvider, this.notificationClickNotifierProvider));
        this.provideStatusBarProvider = new DelegateFactory();
        this.remoteInputUriControllerProvider = DoubleCheck.provider(RemoteInputUriController_Factory.create(this.provideIStatusBarServiceProvider));
        Provider<LogBuffer> provider27 = DoubleCheck.provider(LogModule_ProvideNotifInteractionLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotifInteractionLogBufferProvider = provider27;
        this.actionClickLoggerProvider = ActionClickLogger_Factory.create(provider27);
        this.provideNotificationRemoteInputManagerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory.create(this.contextProvider, this.notificationLockscreenUserManagerImplProvider, this.provideSmartReplyControllerProvider, this.provideNotificationEntryManagerProvider, this.provideStatusBarProvider, this.statusBarStateControllerImplProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.remoteInputUriControllerProvider, this.notificationClickNotifierProvider, this.actionClickLoggerProvider));
        this.bindSystemClockProvider = DoubleCheck.provider(SystemClockImpl_Factory.create());
        this.notifCollectionLoggerProvider = NotifCollectionLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        Provider<Files> provider28 = DoubleCheck.provider(Files_Factory.create());
        this.filesProvider = provider28;
        Provider<LogBufferEulogizer> provider29 = DoubleCheck.provider(LogBufferEulogizer_Factory.create(this.contextProvider, this.dumpManagerProvider, this.bindSystemClockProvider, provider28));
        this.logBufferEulogizerProvider = provider29;
        this.notifCollectionProvider = DoubleCheck.provider(NotifCollection_Factory.create(this.provideIStatusBarServiceProvider, this.bindSystemClockProvider, this.featureFlagsProvider, this.notifCollectionLoggerProvider, provider29, this.dumpManagerProvider));
        this.shadeListBuilderLoggerProvider = ShadeListBuilderLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        Provider<NotificationInteractionTracker> provider30 = DoubleCheck.provider(NotificationInteractionTracker_Factory.create(this.notificationClickNotifierProvider, this.provideNotificationEntryManagerProvider));
        this.notificationInteractionTrackerProvider = provider30;
        Provider<ShadeListBuilder> provider31 = DoubleCheck.provider(ShadeListBuilder_Factory.create(this.bindSystemClockProvider, this.shadeListBuilderLoggerProvider, this.dumpManagerProvider, provider30));
        this.shadeListBuilderProvider = provider31;
        Provider<NotifPipeline> provider32 = DoubleCheck.provider(NotifPipeline_Factory.create(this.notifCollectionProvider, provider31));
        this.notifPipelineProvider = provider32;
        this.provideCommonNotifCollectionProvider = DoubleCheck.provider(NotificationsModule_ProvideCommonNotifCollectionFactory.create(this.featureFlagsProvider, provider32, this.provideNotificationEntryManagerProvider));
    }

    private void initialize2(Builder builder) {
        NotifBindPipelineLogger_Factory notifBindPipelineLogger_FactoryCreate = NotifBindPipelineLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.notifBindPipelineLoggerProvider = notifBindPipelineLogger_FactoryCreate;
        this.notifBindPipelineProvider = DoubleCheck.provider(NotifBindPipeline_Factory.create(this.provideCommonNotifCollectionProvider, notifBindPipelineLogger_FactoryCreate, ConcurrencyModule_ProvideMainLooperFactory.create()));
        NotifRemoteViewCacheImpl_Factory notifRemoteViewCacheImpl_FactoryCreate = NotifRemoteViewCacheImpl_Factory.create(this.provideCommonNotifCollectionProvider);
        this.notifRemoteViewCacheImplProvider = notifRemoteViewCacheImpl_FactoryCreate;
        this.provideNotifRemoteViewCacheProvider = DoubleCheck.provider(notifRemoteViewCacheImpl_FactoryCreate);
        this.smartReplyConstantsProvider = DoubleCheck.provider(SmartReplyConstants_Factory.create(this.provideMainHandlerProvider, this.contextProvider, DeviceConfigProxy_Factory.create()));
        this.provideLauncherAppsProvider = DoubleCheck.provider(SystemServicesModule_ProvideLauncherAppsFactory.create(this.contextProvider));
        Provider<ConversationNotificationManager> provider = DoubleCheck.provider(ConversationNotificationManager_Factory.create(this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.contextProvider, this.provideMainHandlerProvider));
        this.conversationNotificationManagerProvider = provider;
        ConversationNotificationProcessor_Factory conversationNotificationProcessor_FactoryCreate = ConversationNotificationProcessor_Factory.create(this.provideLauncherAppsProvider, provider);
        this.conversationNotificationProcessorProvider = conversationNotificationProcessor_FactoryCreate;
        this.notificationContentInflaterProvider = DoubleCheck.provider(NotificationContentInflater_Factory.create(this.provideNotifRemoteViewCacheProvider, this.provideNotificationRemoteInputManagerProvider, this.smartReplyConstantsProvider, this.provideSmartReplyControllerProvider, conversationNotificationProcessor_FactoryCreate, this.mediaFeatureFlagProvider, this.provideBackgroundExecutorProvider));
        this.notifInflationErrorManagerProvider = DoubleCheck.provider(NotifInflationErrorManager_Factory.create());
        RowContentBindStageLogger_Factory rowContentBindStageLogger_FactoryCreate = RowContentBindStageLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.rowContentBindStageLoggerProvider = rowContentBindStageLogger_FactoryCreate;
        this.rowContentBindStageProvider = DoubleCheck.provider(RowContentBindStage_Factory.create(this.notificationContentInflaterProvider, this.notifInflationErrorManagerProvider, rowContentBindStageLogger_FactoryCreate));
        Provider<IDreamManager> provider2 = DoubleCheck.provider(SystemServicesModule_ProvideIDreamManagerFactory.create());
        this.provideIDreamManagerProvider = provider2;
        this.notificationInterruptStateProviderImplProvider = DoubleCheck.provider(NotificationInterruptStateProviderImpl_Factory.create(this.contextProvider, this.provideContentResolverProvider, this.providePowerManagerProvider, provider2, this.provideAmbientDisplayConfigurationProvider, this.notificationFilterProvider, this.provideBatteryControllerProvider, this.statusBarStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideMainHandlerProvider));
        this.expandableNotificationRowComponentBuilderProvider = new Provider<ExpandableNotificationRowComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // javax.inject.Provider
            public ExpandableNotificationRowComponent.Builder get() {
                return new ExpandableNotificationRowComponentBuilder();
            }
        };
        IconBuilder_Factory iconBuilder_FactoryCreate = IconBuilder_Factory.create(this.contextProvider);
        this.iconBuilderProvider = iconBuilder_FactoryCreate;
        this.iconManagerProvider = IconManager_Factory.create(this.provideCommonNotifCollectionProvider, this.provideLauncherAppsProvider, iconBuilder_FactoryCreate);
        this.lowPriorityInflationHelperProvider = DoubleCheck.provider(LowPriorityInflationHelper_Factory.create(this.featureFlagsProvider, this.notificationGroupManagerProvider, this.rowContentBindStageProvider));
        this.notificationRowBinderImplProvider = DoubleCheck.provider(NotificationRowBinderImpl_Factory.create(this.contextProvider, this.provideNotificationMessagingUtilProvider, this.provideNotificationRemoteInputManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.notifBindPipelineProvider, this.rowContentBindStageProvider, this.notificationInterruptStateProviderImplProvider, RowInflaterTask_Factory.create(), this.expandableNotificationRowComponentBuilderProvider, this.iconManagerProvider, this.lowPriorityInflationHelperProvider));
        this.newBubbleControllerProvider = new DelegateFactory();
        Provider<ForegroundServiceDismissalFeatureController> provider3 = DoubleCheck.provider(ForegroundServiceDismissalFeatureController_Factory.create(DeviceConfigProxy_Factory.create(), this.contextProvider));
        this.foregroundServiceDismissalFeatureControllerProvider = provider3;
        DelegateFactory delegateFactory = (DelegateFactory) this.provideNotificationEntryManagerProvider;
        Provider<NotificationEntryManager> provider4 = DoubleCheck.provider(NotificationsModule_ProvideNotificationEntryManagerFactory.create(this.provideAppLockManagerProvider, this.notificationEntryManagerLoggerProvider, this.notificationGroupManagerProvider, this.notificationRankingManagerProvider, this.keyguardEnvironmentImplProvider, this.featureFlagsProvider, this.notificationRowBinderImplProvider, this.provideNotificationRemoteInputManagerProvider, this.provideLeakDetectorProvider, this.newBubbleControllerProvider, provider3));
        this.provideNotificationEntryManagerProvider = provider4;
        delegateFactory.setDelegatedProvider(provider4);
        this.targetSdkResolverProvider = DoubleCheck.provider(TargetSdkResolver_Factory.create(this.contextProvider));
        GroupCoalescerLogger_Factory groupCoalescerLogger_FactoryCreate = GroupCoalescerLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.groupCoalescerLoggerProvider = groupCoalescerLogger_FactoryCreate;
        this.groupCoalescerProvider = GroupCoalescer_Factory.create(this.provideMainDelayableExecutorProvider, this.bindSystemClockProvider, groupCoalescerLogger_FactoryCreate);
        SharedCoordinatorLogger_Factory sharedCoordinatorLogger_FactoryCreate = SharedCoordinatorLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.sharedCoordinatorLoggerProvider = sharedCoordinatorLogger_FactoryCreate;
        this.hideNotifsForOtherUsersCoordinatorProvider = HideNotifsForOtherUsersCoordinator_Factory.create(this.notificationLockscreenUserManagerImplProvider, sharedCoordinatorLogger_FactoryCreate);
        this.keyguardCoordinatorProvider = DoubleCheck.provider(KeyguardCoordinator_Factory.create(this.contextProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.providesBroadcastDispatcherProvider, this.statusBarStateControllerImplProvider, this.keyguardUpdateMonitorProvider, this.highPriorityProvider));
        this.rankingCoordinatorProvider = DoubleCheck.provider(RankingCoordinator_Factory.create(this.statusBarStateControllerImplProvider));
        Provider<PackageManager> provider5 = DoubleCheck.provider(SystemServicesModule_ProvidePackageManagerFactory.create(this.contextProvider));
        this.providePackageManagerProvider = provider5;
        Provider<PermissionFlagsCache> provider6 = DoubleCheck.provider(PermissionFlagsCache_Factory.create(provider5, this.provideBackgroundExecutorProvider));
        this.permissionFlagsCacheProvider = provider6;
        Provider<AppOpsControllerImpl> provider7 = DoubleCheck.provider(AppOpsControllerImpl_Factory.create(this.contextProvider, this.provideBgLooperProvider, this.dumpManagerProvider, provider6, this.provideAudioManagerProvider, this.providesBroadcastDispatcherProvider));
        this.appOpsControllerImplProvider = provider7;
        Provider<ForegroundServiceController> provider8 = DoubleCheck.provider(ForegroundServiceController_Factory.create(this.provideNotificationEntryManagerProvider, provider7, this.provideMainHandlerProvider));
        this.foregroundServiceControllerProvider = provider8;
        this.appOpsCoordinatorProvider = DoubleCheck.provider(AppOpsCoordinator_Factory.create(provider8, this.appOpsControllerImplProvider, this.provideMainDelayableExecutorProvider));
        Provider<IPackageManager> provider9 = DoubleCheck.provider(SystemServicesModule_ProvideIPackageManagerFactory.create());
        this.provideIPackageManagerProvider = provider9;
        this.deviceProvisionedCoordinatorProvider = DoubleCheck.provider(DeviceProvisionedCoordinator_Factory.create(this.deviceProvisionedControllerImplProvider, provider9));
        this.bubbleCoordinatorProvider = DoubleCheck.provider(BubbleCoordinator_Factory.create(this.newBubbleControllerProvider, this.notifCollectionProvider));
        Provider<HeadsUpViewBinder> provider10 = DoubleCheck.provider(HeadsUpViewBinder_Factory.create(this.provideNotificationMessagingUtilProvider, this.rowContentBindStageProvider));
        this.headsUpViewBinderProvider = provider10;
        this.headsUpCoordinatorProvider = DoubleCheck.provider(HeadsUpCoordinator_Factory.create(this.provideHeadsUpManagerPhoneProvider, provider10, this.notificationInterruptStateProviderImplProvider, this.provideNotificationRemoteInputManagerProvider));
        this.conversationCoordinatorProvider = DoubleCheck.provider(ConversationCoordinator_Factory.create());
        this.preparationCoordinatorLoggerProvider = PreparationCoordinatorLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.notifInflaterImplProvider = DoubleCheck.provider(NotifInflaterImpl_Factory.create(this.provideIStatusBarServiceProvider, this.notifCollectionProvider, this.notifInflationErrorManagerProvider, this.notifPipelineProvider));
        Provider<NotifViewBarn> provider11 = DoubleCheck.provider(NotifViewBarn_Factory.create());
        this.notifViewBarnProvider = provider11;
        this.preparationCoordinatorProvider = DoubleCheck.provider(PreparationCoordinator_Factory.create(this.preparationCoordinatorLoggerProvider, this.notifInflaterImplProvider, this.notifInflationErrorManagerProvider, provider11, this.provideIStatusBarServiceProvider));
        MediaCoordinator_Factory mediaCoordinator_FactoryCreate = MediaCoordinator_Factory.create(this.mediaFeatureFlagProvider);
        this.mediaCoordinatorProvider = mediaCoordinator_FactoryCreate;
        this.notifCoordinatorsProvider = DoubleCheck.provider(NotifCoordinators_Factory.create(this.dumpManagerProvider, this.featureFlagsProvider, this.hideNotifsForOtherUsersCoordinatorProvider, this.keyguardCoordinatorProvider, this.rankingCoordinatorProvider, this.appOpsCoordinatorProvider, this.deviceProvisionedCoordinatorProvider, this.bubbleCoordinatorProvider, this.headsUpCoordinatorProvider, this.conversationCoordinatorProvider, this.preparationCoordinatorProvider, mediaCoordinator_FactoryCreate));
        Provider<VisualStabilityManager> provider12 = DoubleCheck.provider(NotificationsModule_ProvideVisualStabilityManagerFactory.create(this.provideNotificationEntryManagerProvider, ConcurrencyModule_ProvideHandlerFactory.create()));
        this.provideVisualStabilityManagerProvider = provider12;
        Provider<NotifViewManager> provider13 = DoubleCheck.provider(NotifViewManager_Factory.create(this.notifViewBarnProvider, provider12, this.featureFlagsProvider));
        this.notifViewManagerProvider = provider13;
        this.notifPipelineInitializerProvider = DoubleCheck.provider(NotifPipelineInitializer_Factory.create(this.notifPipelineProvider, this.groupCoalescerProvider, this.notifCollectionProvider, this.shadeListBuilderProvider, this.notifCoordinatorsProvider, this.notifInflaterImplProvider, this.dumpManagerProvider, this.featureFlagsProvider, provider13));
        this.notifBindPipelineInitializerProvider = NotifBindPipelineInitializer_Factory.create(this.notifBindPipelineProvider, this.rowContentBindStageProvider);
        this.provideNotificationGroupAlertTransferHelperProvider = DoubleCheck.provider(StatusBarPhoneDependenciesModule_ProvideNotificationGroupAlertTransferHelperFactory.create(this.rowContentBindStageProvider));
        this.headsUpControllerProvider = DoubleCheck.provider(HeadsUpController_Factory.create(this.headsUpViewBinderProvider, this.notificationInterruptStateProviderImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideNotificationRemoteInputManagerProvider, this.statusBarStateControllerImplProvider, this.provideVisualStabilityManagerProvider, this.provideNotificationListenerProvider));
        NotificationClickerLogger_Factory notificationClickerLogger_FactoryCreate = NotificationClickerLogger_Factory.create(this.provideNotifInteractionLogBufferProvider);
        this.notificationClickerLoggerProvider = notificationClickerLogger_FactoryCreate;
        NotificationClicker_Builder_Factory notificationClicker_Builder_FactoryCreate = NotificationClicker_Builder_Factory.create(this.newBubbleControllerProvider, notificationClickerLogger_FactoryCreate);
        this.builderProvider2 = notificationClicker_Builder_FactoryCreate;
        this.notificationsControllerImplProvider = DoubleCheck.provider(NotificationsControllerImpl_Factory.create(this.featureFlagsProvider, this.provideNotificationListenerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.targetSdkResolverProvider, this.notifPipelineInitializerProvider, this.notifBindPipelineInitializerProvider, this.deviceProvisionedControllerImplProvider, this.notificationRowBinderImplProvider, this.remoteInputUriControllerProvider, this.notificationGroupManagerProvider, this.provideNotificationGroupAlertTransferHelperProvider, this.provideHeadsUpManagerPhoneProvider, this.headsUpControllerProvider, this.headsUpViewBinderProvider, notificationClicker_Builder_FactoryCreate));
        NotificationsControllerStub_Factory notificationsControllerStub_FactoryCreate = NotificationsControllerStub_Factory.create(this.provideNotificationListenerProvider);
        this.notificationsControllerStubProvider = notificationsControllerStub_FactoryCreate;
        this.provideNotificationsControllerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationsControllerFactory.create(this.contextProvider, this.notificationsControllerImplProvider, notificationsControllerStub_FactoryCreate));
        Provider<ProtoTracer> provider14 = DoubleCheck.provider(ProtoTracer_Factory.create(this.contextProvider, this.dumpManagerProvider));
        this.protoTracerProvider = provider14;
        Provider<CommandQueue> provider15 = DoubleCheck.provider(StatusBarDependenciesModule_ProvideCommandQueueFactory.create(this.contextProvider, provider14));
        this.provideCommandQueueProvider = provider15;
        Provider<DarkIconDispatcherImpl> provider16 = DoubleCheck.provider(DarkIconDispatcherImpl_Factory.create(this.contextProvider, provider15));
        this.darkIconDispatcherImplProvider = provider16;
        this.lightBarControllerProvider = DoubleCheck.provider(LightBarController_Factory.create(this.contextProvider, provider16, this.provideBatteryControllerProvider, this.navigationModeControllerProvider));
        this.provideIWindowManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIWindowManagerFactory.create());
        this.provideAutoHideControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideAutoHideControllerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideMainHandlerProvider, this.provideIWindowManagerProvider));
        this.statusBarIconControllerImplProvider = DoubleCheck.provider(StatusBarIconControllerImpl_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        this.notificationWakeUpCoordinatorProvider = DoubleCheck.provider(NotificationWakeUpCoordinator_Factory.create(this.provideHeadsUpManagerPhoneProvider, this.statusBarStateControllerImplProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider));
        Provider<NotificationRoundnessManager> provider17 = DoubleCheck.provider(NotificationRoundnessManager_Factory.create(this.keyguardBypassControllerProvider, this.notificationSectionsFeatureManagerProvider));
        this.notificationRoundnessManagerProvider = provider17;
        this.pulseExpansionHandlerProvider = DoubleCheck.provider(PulseExpansionHandler_Factory.create(this.contextProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.provideHeadsUpManagerPhoneProvider, provider17, this.statusBarStateControllerImplProvider, this.falsingManagerProxyProvider));
        this.dynamicPrivacyControllerProvider = DoubleCheck.provider(DynamicPrivacyController_Factory.create(this.notificationLockscreenUserManagerImplProvider, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider));
        this.bypassHeadsUpNotifierProvider = DoubleCheck.provider(BypassHeadsUpNotifier_Factory.create(this.contextProvider, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationMediaManagerProvider, this.provideNotificationEntryManagerProvider, this.tunerServiceImplProvider));
        this.remoteInputQuickSettingsDisablerProvider = DoubleCheck.provider(RemoteInputQuickSettingsDisabler_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider, this.provideCommandQueueProvider));
        this.provideAccessibilityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAccessibilityManagerFactory.create(this.contextProvider));
        this.provideINotificationManagerProvider = DoubleCheck.provider(DependencyProvider_ProvideINotificationManagerFactory.create(builder.dependencyProvider));
        this.provideShortcutManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideShortcutManagerFactory.create(this.contextProvider));
        Provider<ChannelEditorDialogController> provider18 = DoubleCheck.provider(ChannelEditorDialogController_Factory.create(this.contextProvider, this.provideINotificationManagerProvider, ChannelEditorDialog_Builder_Factory.create()));
        this.channelEditorDialogControllerProvider = provider18;
        this.provideNotificationGutsManagerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationGutsManagerFactory.create(this.contextProvider, this.provideVisualStabilityManagerProvider, this.provideStatusBarProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider, this.provideAccessibilityManagerProvider, this.highPriorityProvider, this.provideINotificationManagerProvider, this.provideLauncherAppsProvider, this.provideShortcutManagerProvider, provider18, this.provideCurrentUserContextTrackerProvider, PriorityOnboardingDialogController_Builder_Factory.create(), this.newBubbleControllerProvider, this.provideUiEventLoggerProvider));
        this.expansionStateLoggerProvider = NotificationLogger_ExpansionStateLogger_Factory.create(this.provideUiBackgroundExecutorProvider);
        Provider<NotificationPanelLogger> provider19 = DoubleCheck.provider(NotificationsModule_ProvideNotificationPanelLoggerFactory.create());
        this.provideNotificationPanelLoggerProvider = provider19;
        this.provideNotificationLoggerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationLoggerFactory.create(this.provideNotificationListenerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationEntryManagerProvider, this.statusBarStateControllerImplProvider, this.expansionStateLoggerProvider, provider19));
        this.foregroundServiceSectionControllerProvider = DoubleCheck.provider(ForegroundServiceSectionController_Factory.create(this.provideNotificationEntryManagerProvider, this.foregroundServiceDismissalFeatureControllerProvider));
        DynamicChildBindController_Factory dynamicChildBindController_FactoryCreate = DynamicChildBindController_Factory.create(this.rowContentBindStageProvider);
        this.dynamicChildBindControllerProvider = dynamicChildBindController_FactoryCreate;
        this.provideNotificationViewHierarchyManagerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationViewHierarchyManagerFactory.create(this.contextProvider, this.provideMainHandlerProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationGroupManagerProvider, this.provideVisualStabilityManagerProvider, this.statusBarStateControllerImplProvider, this.provideNotificationEntryManagerProvider, this.keyguardBypassControllerProvider, this.newBubbleControllerProvider, this.dynamicPrivacyControllerProvider, this.foregroundServiceSectionControllerProvider, dynamicChildBindController_FactoryCreate, this.lowPriorityInflationHelperProvider));
        this.provideMetricsLoggerProvider = DoubleCheck.provider(DependencyProvider_ProvideMetricsLoggerFactory.create(builder.dependencyProvider));
        Provider<Optional<Lazy<StatusBar>>> providerOf = PresentJdkOptionalLazyProvider.of(this.provideStatusBarProvider);
        this.optionalOfLazyOfStatusBarProvider = providerOf;
        this.activityStarterDelegateProvider = DoubleCheck.provider(ActivityStarterDelegate_Factory.create(providerOf));
        Provider<IActivityTaskManager> provider20 = DoubleCheck.provider(SystemServicesModule_ProvideIActivityTaskManagerFactory.create());
        this.provideIActivityTaskManagerProvider = provider20;
        this.userSwitcherControllerProvider = DoubleCheck.provider(UserSwitcherController_Factory.create(this.contextProvider, this.keyguardStateControllerImplProvider, this.provideMainHandlerProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider, this.provideUiEventLoggerProvider, provider20));
        this.provideConnectivityManagagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideConnectivityManagagerFactory.create(this.contextProvider));
        this.provideTelephonyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideTelephonyManagerFactory.create(this.contextProvider));
        this.provideWifiManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideWifiManagerFactory.create(this.contextProvider));
        Provider<NetworkScoreManager> provider21 = DoubleCheck.provider(SystemServicesModule_ProvideNetworkScoreManagerFactory.create(this.contextProvider));
        this.provideNetworkScoreManagerProvider = provider21;
        this.networkControllerImplProvider = DoubleCheck.provider(NetworkControllerImpl_Factory.create(this.contextProvider, this.provideBgLooperProvider, this.deviceProvisionedControllerImplProvider, this.providesBroadcastDispatcherProvider, this.provideConnectivityManagagerProvider, this.provideTelephonyManagerProvider, this.provideWifiManagerProvider, provider21));
        this.sysuiColorExtractorProvider = DoubleCheck.provider(SysuiColorExtractor_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider));
        this.screenLifecycleProvider = DoubleCheck.provider(ScreenLifecycle_Factory.create());
        this.wakefulnessLifecycleProvider = DoubleCheck.provider(WakefulnessLifecycle_Factory.create());
        this.vibratorHelperProvider = DoubleCheck.provider(VibratorHelper_Factory.create(this.contextProvider));
        this.provideNavigationBarControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideNavigationBarControllerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideMainHandlerProvider, this.provideCommandQueueProvider));
        this.provideAssistUtilsProvider = DoubleCheck.provider(AssistModule_ProvideAssistUtilsFactory.create(this.contextProvider));
        this.provideBackgroundHandlerProvider = DoubleCheck.provider(AssistModule_ProvideBackgroundHandlerFactory.create());
        this.provideAssistHandleViewControllerProvider = AssistModule_ProvideAssistHandleViewControllerFactory.create(this.provideNavigationBarControllerProvider);
        this.deviceConfigHelperProvider = DoubleCheck.provider(DeviceConfigHelper_Factory.create());
    }

    private void initialize3(Builder builder) {
        this.assistHandleOffBehaviorProvider = DoubleCheck.provider(AssistHandleOffBehavior_Factory.create());
        Provider<SysUiState> provider = DoubleCheck.provider(SystemUIModule_ProvideSysUiStateFactory.create());
        this.provideSysUiStateProvider = provider;
        this.assistHandleLikeHomeBehaviorProvider = DoubleCheck.provider(AssistHandleLikeHomeBehavior_Factory.create(this.statusBarStateControllerImplProvider, this.wakefulnessLifecycleProvider, provider));
        this.provideSystemClockProvider = DoubleCheck.provider(AssistModule_ProvideSystemClockFactory.create());
        this.provideActivityManagerWrapperProvider = DoubleCheck.provider(DependencyProvider_ProvideActivityManagerWrapperFactory.create(builder.dependencyProvider));
        this.pipSnapAlgorithmProvider = PipSnapAlgorithm_Factory.create(this.contextProvider);
        Provider<DisplayController> provider2 = DoubleCheck.provider(DisplayController_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.provideIWindowManagerProvider));
        this.displayControllerProvider = provider2;
        this.pipBoundsHandlerProvider = DoubleCheck.provider(PipBoundsHandler_Factory.create(this.contextProvider, this.pipSnapAlgorithmProvider, provider2));
        this.pipSurfaceTransactionHelperProvider = DoubleCheck.provider(PipSurfaceTransactionHelper_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.contextComponentResolverProvider = delegateFactory;
        RecentsModule_ProvideRecentsImplFactory recentsModule_ProvideRecentsImplFactoryCreate = RecentsModule_ProvideRecentsImplFactory.create(this.contextProvider, delegateFactory);
        this.provideRecentsImplProvider = recentsModule_ProvideRecentsImplFactoryCreate;
        Provider<Recents> provider3 = DoubleCheck.provider(SystemUIDefaultModule_ProvideRecentsFactory.create(this.contextProvider, recentsModule_ProvideRecentsImplFactoryCreate, this.provideCommandQueueProvider));
        this.provideRecentsProvider = provider3;
        this.optionalOfLazyOfRecentsProvider = PresentJdkOptionalLazyProvider.of(provider3);
        this.systemWindowsProvider = DoubleCheck.provider(SystemWindows_Factory.create(this.contextProvider, this.displayControllerProvider, this.provideIWindowManagerProvider));
        Provider<TransactionPool> provider4 = DoubleCheck.provider(TransactionPool_Factory.create());
        this.transactionPoolProvider = provider4;
        Provider<DisplayImeController> provider5 = DoubleCheck.provider(DisplayImeController_Factory.create(this.systemWindowsProvider, this.displayControllerProvider, this.provideMainHandlerProvider, provider4));
        this.displayImeControllerProvider = provider5;
        this.provideDividerProvider = DoubleCheck.provider(DividerModule_ProvideDividerFactory.create(this.contextProvider, this.optionalOfLazyOfRecentsProvider, this.displayControllerProvider, this.systemWindowsProvider, provider5, this.provideMainHandlerProvider, this.keyguardStateControllerImplProvider, this.transactionPoolProvider));
        this.pipAnimationControllerProvider = PipAnimationController_Factory.create(this.pipSurfaceTransactionHelperProvider);
        Provider<PipUiEventLogger> provider6 = DoubleCheck.provider(PipUiEventLogger_Factory.create(this.provideUiEventLoggerProvider, this.providePackageManagerProvider));
        this.pipUiEventLoggerProvider = provider6;
        Provider<PipTaskOrganizer> provider7 = DoubleCheck.provider(PipTaskOrganizer_Factory.create(this.contextProvider, this.pipBoundsHandlerProvider, this.pipSurfaceTransactionHelperProvider, this.provideDividerProvider, this.displayControllerProvider, this.pipAnimationControllerProvider, provider6));
        this.pipTaskOrganizerProvider = provider7;
        Provider<PipManager> provider8 = DoubleCheck.provider(PipManager_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.pipBoundsHandlerProvider, provider7, this.pipSurfaceTransactionHelperProvider, this.provideDividerProvider));
        this.pipManagerProvider = provider8;
        this.pipUIProvider = DoubleCheck.provider(PipUI_Factory.create(this.contextProvider, this.provideCommandQueueProvider, provider8));
        Provider<Optional<Divider>> providerOf = PresentJdkOptionalInstanceProvider.of(this.provideDividerProvider);
        this.optionalOfDividerProvider = providerOf;
        this.overviewProxyServiceProvider = DoubleCheck.provider(OverviewProxyService_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.provideNavigationBarControllerProvider, this.navigationModeControllerProvider, this.notificationShadeWindowControllerProvider, this.provideSysUiStateProvider, this.pipUIProvider, providerOf, this.optionalOfLazyOfStatusBarProvider, this.providesBroadcastDispatcherProvider));
        Provider<PackageManagerWrapper> provider9 = DoubleCheck.provider(SystemServicesModule_ProvidePackageManagerWrapperFactory.create());
        this.providePackageManagerWrapperProvider = provider9;
        Provider provider10 = DoubleCheck.provider(AssistHandleReminderExpBehavior_Factory.create(this.provideSystemClockProvider, this.provideBackgroundHandlerProvider, this.deviceConfigHelperProvider, this.statusBarStateControllerImplProvider, this.provideActivityManagerWrapperProvider, this.overviewProxyServiceProvider, this.provideSysUiStateProvider, this.wakefulnessLifecycleProvider, provider9, this.providesBroadcastDispatcherProvider, this.bootCompleteCacheImplProvider));
        this.assistHandleReminderExpBehaviorProvider = provider10;
        Provider provider11 = DoubleCheck.provider(AssistModule_ProvideAssistHandleBehaviorControllerMapFactory.create(this.assistHandleOffBehaviorProvider, this.assistHandleLikeHomeBehaviorProvider, provider10));
        this.provideAssistHandleBehaviorControllerMapProvider = provider11;
        this.assistHandleBehaviorControllerProvider = DoubleCheck.provider(AssistHandleBehaviorController_Factory.create(this.contextProvider, this.provideAssistUtilsProvider, this.provideBackgroundHandlerProvider, this.provideAssistHandleViewControllerProvider, this.deviceConfigHelperProvider, provider11, this.navigationModeControllerProvider, this.provideAccessibilityManagerProvider, this.dumpManagerProvider));
        Provider<PhoneStateMonitor> provider12 = DoubleCheck.provider(PhoneStateMonitor_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.optionalOfLazyOfStatusBarProvider, this.bootCompleteCacheImplProvider));
        this.phoneStateMonitorProvider = provider12;
        Provider<AssistLogger> provider13 = DoubleCheck.provider(AssistLogger_Factory.create(this.contextProvider, this.provideUiEventLoggerProvider, this.provideAssistUtilsProvider, provider12, this.assistHandleBehaviorControllerProvider));
        this.assistLoggerProvider = provider13;
        Provider<GoogleDefaultUiController> provider14 = DoubleCheck.provider(GoogleDefaultUiController_Factory.create(this.contextProvider, provider13));
        this.googleDefaultUiControllerProvider = provider14;
        this.assistManagerProvider = DoubleCheck.provider(AssistManager_Factory.create(this.deviceProvisionedControllerImplProvider, this.contextProvider, this.provideAssistUtilsProvider, this.assistHandleBehaviorControllerProvider, this.provideCommandQueueProvider, this.phoneStateMonitorProvider, this.overviewProxyServiceProvider, this.provideConfigurationControllerProvider, this.provideSysUiStateProvider, provider14, this.assistLoggerProvider));
        this.lockscreenGestureLoggerProvider = DoubleCheck.provider(LockscreenGestureLogger_Factory.create());
        DelegateFactory delegateFactory2 = new DelegateFactory();
        this.statusBarKeyguardViewManagerProvider = delegateFactory2;
        this.shadeControllerImplProvider = DoubleCheck.provider(ShadeControllerImpl_Factory.create(this.provideCommandQueueProvider, this.statusBarStateControllerImplProvider, this.notificationShadeWindowControllerProvider, delegateFactory2, this.provideWindowManagerProvider, this.provideStatusBarProvider, this.assistManagerProvider, this.newBubbleControllerProvider));
        this.accessibilityControllerProvider = DoubleCheck.provider(AccessibilityController_Factory.create(this.contextProvider));
        this.builderProvider3 = WakeLock_Builder_Factory.create(this.contextProvider);
        Provider<IBatteryStats> provider15 = DoubleCheck.provider(SystemServicesModule_ProvideIBatteryStatsFactory.create());
        this.provideIBatteryStatsProvider = provider15;
        Provider<KeyguardIndicationController> provider16 = DoubleCheck.provider(KeyguardIndicationController_Factory.create(this.contextProvider, this.builderProvider3, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider, this.keyguardUpdateMonitorProvider, this.dockManagerImplProvider, this.providesBroadcastDispatcherProvider, this.provideDevicePolicyManagerProvider, provider15, this.provideUserManagerProvider));
        this.keyguardIndicationControllerProvider = provider16;
        this.lockscreenLockIconControllerProvider = DoubleCheck.provider(LockscreenLockIconController_Factory.create(this.lockscreenGestureLoggerProvider, this.keyguardUpdateMonitorProvider, this.provideLockPatternUtilsProvider, this.shadeControllerImplProvider, this.accessibilityControllerProvider, provider16, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.dockManagerImplProvider, this.keyguardStateControllerImplProvider, this.provideResourcesProvider, this.provideHeadsUpManagerPhoneProvider));
        this.provideAlarmManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAlarmManagerFactory.create(this.contextProvider));
        this.builderProvider4 = DelayedWakeLock_Builder_Factory.create(this.contextProvider);
        this.blurUtilsProvider = DoubleCheck.provider(BlurUtils_Factory.create(this.provideResourcesProvider, this.dumpManagerProvider));
        this.scrimControllerProvider = DoubleCheck.provider(ScrimController_Factory.create(this.lightBarControllerProvider, this.dozeParametersProvider, this.provideAlarmManagerProvider, this.keyguardStateControllerImplProvider, this.builderProvider4, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardUpdateMonitorProvider, this.sysuiColorExtractorProvider, this.dockManagerImplProvider, this.blurUtilsProvider));
        this.provideKeyguardLiftControllerProvider = DoubleCheck.provider(SystemUIModule_ProvideKeyguardLiftControllerFactory.create(this.contextProvider, this.statusBarStateControllerImplProvider, this.asyncSensorManagerProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider));
        SystemServicesModule_ProvideWallpaperManagerFactory systemServicesModule_ProvideWallpaperManagerFactoryCreate = SystemServicesModule_ProvideWallpaperManagerFactory.create(this.contextProvider);
        this.provideWallpaperManagerProvider = systemServicesModule_ProvideWallpaperManagerFactoryCreate;
        this.lockscreenWallpaperProvider = DoubleCheck.provider(LockscreenWallpaper_Factory.create(systemServicesModule_ProvideWallpaperManagerFactoryCreate, SystemServicesModule_ProvideIWallPaperManagerFactory.create(), this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.provideNotificationMediaManagerProvider, this.provideMainHandlerProvider));
        Provider<LogBuffer> provider17 = DoubleCheck.provider(LogModule_ProvideDozeLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideDozeLogBufferProvider = provider17;
        DozeLogger_Factory dozeLogger_FactoryCreate = DozeLogger_Factory.create(provider17);
        this.dozeLoggerProvider = dozeLogger_FactoryCreate;
        Provider<DozeLog> provider18 = DoubleCheck.provider(DozeLog_Factory.create(this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, dozeLogger_FactoryCreate));
        this.dozeLogProvider = provider18;
        Provider<DozeScrimController> provider19 = DoubleCheck.provider(DozeScrimController_Factory.create(this.dozeParametersProvider, provider18));
        this.dozeScrimControllerProvider = provider19;
        Provider<BiometricUnlockController> provider20 = DoubleCheck.provider(BiometricUnlockController_Factory.create(this.contextProvider, provider19, this.newKeyguardViewMediatorProvider, this.scrimControllerProvider, this.provideStatusBarProvider, this.shadeControllerImplProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerImplProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardUpdateMonitorProvider, this.provideResourcesProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider, this.provideMetricsLoggerProvider, this.dumpManagerProvider));
        this.biometricUnlockControllerProvider = provider20;
        this.dozeServiceHostProvider = DoubleCheck.provider(DozeServiceHost_Factory.create(this.dozeLogProvider, this.providePowerManagerProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideBatteryControllerProvider, this.scrimControllerProvider, provider20, this.newKeyguardViewMediatorProvider, this.assistManagerProvider, this.dozeScrimControllerProvider, this.keyguardUpdateMonitorProvider, this.provideVisualStabilityManagerProvider, this.pulseExpansionHandlerProvider, this.notificationShadeWindowControllerProvider, this.notificationWakeUpCoordinatorProvider, this.lockscreenLockIconControllerProvider, this.tunerServiceImplProvider));
        this.screenPinningRequestProvider = ScreenPinningRequest_Factory.create(this.contextProvider, this.optionalOfLazyOfStatusBarProvider);
        Provider<VolumeDialogControllerImpl> provider21 = DoubleCheck.provider(VolumeDialogControllerImpl_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.optionalOfLazyOfStatusBarProvider, this.ringerModeTrackerImplProvider));
        this.volumeDialogControllerImplProvider = provider21;
        this.volumeDialogComponentProvider = DoubleCheck.provider(VolumeDialogComponent_Factory.create(this.contextProvider, this.newKeyguardViewMediatorProvider, provider21));
        this.optionalOfRecentsProvider = PresentJdkOptionalInstanceProvider.of(this.provideRecentsProvider);
        this.statusBarComponentBuilderProvider = new Provider<StatusBarComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // javax.inject.Provider
            public StatusBarComponent.Builder get() {
                return new StatusBarComponentBuilder();
            }
        };
        this.lightsOutNotifControllerProvider = DoubleCheck.provider(LightsOutNotifController_Factory.create(this.provideWindowManagerProvider, this.provideNotificationEntryManagerProvider, this.provideCommandQueueProvider));
        this.statusBarRemoteInputCallbackProvider = DoubleCheck.provider(StatusBarRemoteInputCallback_Factory.create(this.contextProvider, this.notificationGroupManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider, this.statusBarKeyguardViewManagerProvider, this.activityStarterDelegateProvider, this.shadeControllerImplProvider, this.provideCommandQueueProvider, this.actionClickLoggerProvider));
        this.activityIntentHelperProvider = DoubleCheck.provider(ActivityIntentHelper_Factory.create(this.contextProvider));
        StatusBarNotificationActivityStarterLogger_Factory statusBarNotificationActivityStarterLogger_FactoryCreate = StatusBarNotificationActivityStarterLogger_Factory.create(this.provideNotifInteractionLogBufferProvider);
        this.statusBarNotificationActivityStarterLoggerProvider = statusBarNotificationActivityStarterLogger_FactoryCreate;
        this.builderProvider5 = DoubleCheck.provider(StatusBarNotificationActivityStarter_Builder_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.notifCollectionProvider, this.provideHeadsUpManagerPhoneProvider, this.activityStarterDelegateProvider, this.notificationClickNotifierProvider, this.statusBarStateControllerImplProvider, this.statusBarKeyguardViewManagerProvider, this.provideKeyguardManagerProvider, this.provideIDreamManagerProvider, this.newBubbleControllerProvider, this.assistManagerProvider, this.provideNotificationRemoteInputManagerProvider, this.notificationGroupManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.shadeControllerImplProvider, this.keyguardStateControllerImplProvider, this.notificationInterruptStateProviderImplProvider, this.provideLockPatternUtilsProvider, this.statusBarRemoteInputCallbackProvider, this.activityIntentHelperProvider, this.featureFlagsProvider, this.provideMetricsLoggerProvider, statusBarNotificationActivityStarterLogger_FactoryCreate));
        Factory factoryCreate = InstanceFactory.create(this);
        this.tvSystemUIRootComponentProvider = factoryCreate;
        this.injectionInflationControllerProvider = DoubleCheck.provider(InjectionInflationController_Factory.create(factoryCreate));
        Provider<NotificationRowComponent.Builder> provider22 = new Provider<NotificationRowComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.3
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // javax.inject.Provider
            public NotificationRowComponent.Builder get() {
                return new NotificationRowComponentBuilder();
            }
        };
        this.notificationRowComponentBuilderProvider = provider22;
        this.superStatusBarViewFactoryProvider = DoubleCheck.provider(SuperStatusBarViewFactory_Factory.create(this.contextProvider, this.injectionInflationControllerProvider, provider22, this.lockscreenLockIconControllerProvider));
        this.initControllerProvider = DoubleCheck.provider(InitController_Factory.create());
        this.provideTimeTickHandlerProvider = DoubleCheck.provider(DependencyProvider_ProvideTimeTickHandlerFactory.create(builder.dependencyProvider));
        this.pluginDependencyProvider = DoubleCheck.provider(PluginDependencyProvider_Factory.create(this.providePluginManagerProvider));
        this.keyguardDismissUtilProvider = DoubleCheck.provider(KeyguardDismissUtil_Factory.create());
        this.userInfoControllerImplProvider = DoubleCheck.provider(UserInfoControllerImpl_Factory.create(this.contextProvider));
        this.castControllerImplProvider = DoubleCheck.provider(CastControllerImpl_Factory.create(this.contextProvider));
        this.hotspotControllerImplProvider = DoubleCheck.provider(HotspotControllerImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.provideLocalBluetoothControllerProvider = DoubleCheck.provider(SystemServicesModule_ProvideLocalBluetoothControllerFactory.create(this.contextProvider, this.provideBgHandlerProvider));
        this.bluetoothControllerImplProvider = DoubleCheck.provider(BluetoothControllerImpl_Factory.create(this.contextProvider, this.provideBgLooperProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.provideLocalBluetoothControllerProvider));
        this.nextAlarmControllerImplProvider = DoubleCheck.provider(NextAlarmControllerImpl_Factory.create(this.contextProvider));
        this.rotationLockControllerImplProvider = DoubleCheck.provider(RotationLockControllerImpl_Factory.create(this.contextProvider));
        this.provideDataSaverControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideDataSaverControllerFactory.create(builder.dependencyProvider, this.networkControllerImplProvider));
        this.zenModeControllerImplProvider = DoubleCheck.provider(ZenModeControllerImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.locationControllerImplProvider = DoubleCheck.provider(LocationControllerImpl_Factory.create(this.contextProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.provideBgLooperProvider, this.providesBroadcastDispatcherProvider, this.bootCompleteCacheImplProvider));
        this.sensorPrivacyControllerImplProvider = DoubleCheck.provider(SensorPrivacyControllerImpl_Factory.create(this.contextProvider));
        this.provideTelecomManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideTelecomManagerFactory.create(this.contextProvider));
        this.provideDisplayIdProvider = SystemServicesModule_ProvideDisplayIdFactory.create(this.contextProvider);
        this.provideSharePreferencesProvider = DependencyProvider_ProvideSharePreferencesFactory.create(builder.dependencyProvider, this.contextProvider);
        this.dateFormatUtilProvider = DateFormatUtil_Factory.create(this.contextProvider);
        Provider<PrivacyItemController> provider23 = DoubleCheck.provider(PrivacyItemController_Factory.create(this.appOpsControllerImplProvider, this.provideMainDelayableExecutorProvider, this.provideBackgroundExecutorProvider, this.providesBroadcastDispatcherProvider, DeviceConfigProxy_Factory.create(), this.provideUserManagerProvider, this.dumpManagerProvider));
        this.privacyItemControllerProvider = provider23;
        this.phoneStatusBarPolicyProvider = PhoneStatusBarPolicy_Factory.create(this.contextProvider, this.statusBarIconControllerImplProvider, this.provideCommandQueueProvider, this.providesBroadcastDispatcherProvider, this.provideUiBackgroundExecutorProvider, this.provideResourcesProvider, this.castControllerImplProvider, this.hotspotControllerImplProvider, this.bluetoothControllerImplProvider, this.nextAlarmControllerImplProvider, this.userInfoControllerImplProvider, this.rotationLockControllerImplProvider, this.provideDataSaverControllerProvider, this.zenModeControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.keyguardStateControllerImplProvider, this.locationControllerImplProvider, this.sensorPrivacyControllerImplProvider, this.provideIActivityManagerProvider, this.provideAlarmManagerProvider, this.provideUserManagerProvider, this.recordingControllerProvider, this.provideTelecomManagerProvider, this.provideDisplayIdProvider, this.provideSharePreferencesProvider, this.dateFormatUtilProvider, this.ringerModeTrackerImplProvider, provider23);
        Provider<Choreographer> provider24 = DoubleCheck.provider(DependencyProvider_ProvidesChoreographerFactory.create(builder.dependencyProvider));
        this.providesChoreographerProvider = provider24;
        this.notificationShadeDepthControllerProvider = DoubleCheck.provider(NotificationShadeDepthController_Factory.create(this.statusBarStateControllerImplProvider, this.blurUtilsProvider, this.biometricUnlockControllerProvider, this.keyguardStateControllerImplProvider, provider24, this.provideWallpaperManagerProvider, this.notificationShadeWindowControllerProvider, this.dozeParametersProvider, this.dumpManagerProvider));
        this.dismissCallbackRegistryProvider = DoubleCheck.provider(DismissCallbackRegistry_Factory.create(this.provideUiBackgroundExecutorProvider));
        this.statusBarTouchableRegionManagerProvider = DoubleCheck.provider(StatusBarTouchableRegionManager_Factory.create(this.contextProvider, this.notificationShadeWindowControllerProvider, this.provideConfigurationControllerProvider, this.provideHeadsUpManagerPhoneProvider));
        Provider<FODCircleViewImpl> provider25 = DoubleCheck.provider(FODCircleViewImpl_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        this.fODCircleViewImplProvider = provider25;
        DelegateFactory delegateFactory3 = (DelegateFactory) this.provideStatusBarProvider;
        Provider<StatusBar> provider26 = DoubleCheck.provider(StatusBarPhoneModule_ProvideStatusBarFactory.create(this.contextProvider, this.provideNotificationsControllerProvider, this.lightBarControllerProvider, this.provideAutoHideControllerProvider, this.keyguardUpdateMonitorProvider, this.statusBarIconControllerImplProvider, this.pulseExpansionHandlerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.keyguardStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.dynamicPrivacyControllerProvider, this.bypassHeadsUpNotifierProvider, this.falsingManagerProxyProvider, this.providesBroadcastDispatcherProvider, this.remoteInputQuickSettingsDisablerProvider, this.provideNotificationGutsManagerProvider, this.provideNotificationLoggerProvider, this.notificationInterruptStateProviderImplProvider, this.provideNotificationViewHierarchyManagerProvider, this.newKeyguardViewMediatorProvider, this.provideDisplayMetricsProvider, this.provideMetricsLoggerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationMediaManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationRemoteInputManagerProvider, this.userSwitcherControllerProvider, this.networkControllerImplProvider, this.provideBatteryControllerProvider, this.sysuiColorExtractorProvider, this.screenLifecycleProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.vibratorHelperProvider, this.newBubbleControllerProvider, this.notificationGroupManagerProvider, this.provideVisualStabilityManagerProvider, this.deviceProvisionedControllerImplProvider, this.provideNavigationBarControllerProvider, this.assistManagerProvider, this.provideConfigurationControllerProvider, this.notificationShadeWindowControllerProvider, this.lockscreenLockIconControllerProvider, this.dozeParametersProvider, this.scrimControllerProvider, this.provideKeyguardLiftControllerProvider, this.lockscreenWallpaperProvider, this.biometricUnlockControllerProvider, this.dozeServiceHostProvider, this.providePowerManagerProvider, this.screenPinningRequestProvider, this.dozeScrimControllerProvider, this.volumeDialogComponentProvider, this.provideCommandQueueProvider, this.optionalOfRecentsProvider, this.statusBarComponentBuilderProvider, this.providePluginManagerProvider, this.optionalOfDividerProvider, this.lightsOutNotifControllerProvider, this.builderProvider5, this.shadeControllerImplProvider, this.superStatusBarViewFactoryProvider, this.statusBarKeyguardViewManagerProvider, this.providesViewMediatorCallbackProvider, this.initControllerProvider, this.darkIconDispatcherImplProvider, this.provideTimeTickHandlerProvider, this.pluginDependencyProvider, this.keyguardDismissUtilProvider, this.extensionControllerImplProvider, this.userInfoControllerImplProvider, this.phoneStatusBarPolicyProvider, this.keyguardIndicationControllerProvider, this.notificationShadeDepthControllerProvider, this.dismissCallbackRegistryProvider, this.statusBarTouchableRegionManagerProvider, this.tunerServiceImplProvider, provider25));
        this.provideStatusBarProvider = provider26;
        delegateFactory3.setDelegatedProvider(provider26);
        this.mediaArtworkProcessorProvider = DoubleCheck.provider(MediaArtworkProcessor_Factory.create());
        MediaControllerFactory_Factory mediaControllerFactory_FactoryCreate = MediaControllerFactory_Factory.create(this.contextProvider);
        this.mediaControllerFactoryProvider = mediaControllerFactory_FactoryCreate;
        this.mediaTimeoutListenerProvider = DoubleCheck.provider(MediaTimeoutListener_Factory.create(mediaControllerFactory_FactoryCreate, this.provideMainDelayableExecutorProvider));
        MediaBrowserFactory_Factory mediaBrowserFactory_FactoryCreate = MediaBrowserFactory_Factory.create(this.contextProvider);
        this.mediaBrowserFactoryProvider = mediaBrowserFactory_FactoryCreate;
        ResumeMediaBrowserFactory_Factory resumeMediaBrowserFactory_FactoryCreate = ResumeMediaBrowserFactory_Factory.create(this.contextProvider, mediaBrowserFactory_FactoryCreate);
        this.resumeMediaBrowserFactoryProvider = resumeMediaBrowserFactory_FactoryCreate;
        this.mediaResumeListenerProvider = DoubleCheck.provider(MediaResumeListener_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider, this.tunerServiceImplProvider, resumeMediaBrowserFactory_FactoryCreate));
    }

    private void initialize4(Builder builder) {
        SystemServicesModule_ProvideMediaSessionManagerFactory systemServicesModule_ProvideMediaSessionManagerFactoryCreate = SystemServicesModule_ProvideMediaSessionManagerFactory.create(this.contextProvider);
        this.provideMediaSessionManagerProvider = systemServicesModule_ProvideMediaSessionManagerFactoryCreate;
        this.mediaSessionBasedFilterProvider = MediaSessionBasedFilter_Factory.create(this.contextProvider, systemServicesModule_ProvideMediaSessionManagerFactoryCreate, this.provideMainExecutorProvider, this.provideBackgroundExecutorProvider);
        this.localMediaManagerFactoryProvider = LocalMediaManagerFactory_Factory.create(this.contextProvider, this.provideLocalBluetoothControllerProvider);
        SystemServicesModule_ProvideMediaRouter2ManagerFactory systemServicesModule_ProvideMediaRouter2ManagerFactoryCreate = SystemServicesModule_ProvideMediaRouter2ManagerFactory.create(this.contextProvider);
        this.provideMediaRouter2ManagerProvider = systemServicesModule_ProvideMediaRouter2ManagerFactoryCreate;
        this.mediaDeviceManagerProvider = MediaDeviceManager_Factory.create(this.mediaControllerFactoryProvider, this.localMediaManagerFactoryProvider, systemServicesModule_ProvideMediaRouter2ManagerFactoryCreate, this.provideMainExecutorProvider, this.provideBackgroundExecutorProvider, this.dumpManagerProvider);
        this.mediaDataFilterProvider = MediaDataFilter_Factory.create(this.providesBroadcastDispatcherProvider, this.mediaResumeListenerProvider, this.notificationLockscreenUserManagerImplProvider, this.provideMainExecutorProvider);
        this.mediaDataManagerProvider = DoubleCheck.provider(MediaDataManager_Factory.create(this.contextProvider, this.provideBackgroundExecutorProvider, this.provideMainDelayableExecutorProvider, this.mediaControllerFactoryProvider, this.dumpManagerProvider, this.providesBroadcastDispatcherProvider, this.mediaTimeoutListenerProvider, this.mediaResumeListenerProvider, this.mediaSessionBasedFilterProvider, this.mediaDeviceManagerProvider, MediaDataCombineLatest_Factory.create(), this.mediaDataFilterProvider));
        DelegateFactory delegateFactory = (DelegateFactory) this.provideNotificationMediaManagerProvider;
        Provider<NotificationMediaManager> provider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory.create(this.contextProvider, this.provideStatusBarProvider, this.notificationShadeWindowControllerProvider, this.provideNotificationEntryManagerProvider, this.mediaArtworkProcessorProvider, this.keyguardBypassControllerProvider, this.provideMainDelayableExecutorProvider, DeviceConfigProxy_Factory.create(), this.mediaDataManagerProvider, this.mediaFeatureFlagProvider));
        this.provideNotificationMediaManagerProvider = provider;
        delegateFactory.setDelegatedProvider(provider);
        DelegateFactory delegateFactory2 = (DelegateFactory) this.statusBarKeyguardViewManagerProvider;
        Provider<StatusBarKeyguardViewManager> provider2 = DoubleCheck.provider(StatusBarKeyguardViewManager_Factory.create(this.contextProvider, this.providesViewMediatorCallbackProvider, this.provideLockPatternUtilsProvider, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.keyguardUpdateMonitorProvider, this.navigationModeControllerProvider, this.dockManagerImplProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerImplProvider, this.provideNotificationMediaManagerProvider));
        this.statusBarKeyguardViewManagerProvider = provider2;
        delegateFactory2.setDelegatedProvider(provider2);
        Provider<TrustManager> provider3 = DoubleCheck.provider(SystemServicesModule_ProvideTrustManagerFactory.create(this.contextProvider));
        this.provideTrustManagerProvider = provider3;
        DelegateFactory delegateFactory3 = (DelegateFactory) this.newKeyguardViewMediatorProvider;
        Provider<KeyguardViewMediator> provider4 = DoubleCheck.provider(KeyguardModule_NewKeyguardViewMediatorFactory.create(this.contextProvider, this.falsingManagerProxyProvider, this.provideLockPatternUtilsProvider, this.providesBroadcastDispatcherProvider, this.statusBarKeyguardViewManagerProvider, this.dismissCallbackRegistryProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.providePowerManagerProvider, provider3, this.provideUiBackgroundExecutorProvider, DeviceConfigProxy_Factory.create(), this.navigationModeControllerProvider));
        this.newKeyguardViewMediatorProvider = provider4;
        delegateFactory3.setDelegatedProvider(provider4);
        DelegateFactory delegateFactory4 = (DelegateFactory) this.notificationShadeWindowControllerProvider;
        Provider<NotificationShadeWindowController> provider5 = DoubleCheck.provider(NotificationShadeWindowController_Factory.create(this.contextProvider, this.provideWindowManagerProvider, this.provideIActivityManagerProvider, this.dozeParametersProvider, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.newKeyguardViewMediatorProvider, this.keyguardBypassControllerProvider, this.sysuiColorExtractorProvider, this.dumpManagerProvider));
        this.notificationShadeWindowControllerProvider = provider5;
        delegateFactory4.setDelegatedProvider(provider5);
        this.bubbleDataProvider = DoubleCheck.provider(BubbleData_Factory.create(this.contextProvider));
        this.floatingContentCoordinatorProvider = DoubleCheck.provider(FloatingContentCoordinator_Factory.create());
        this.bubbleVolatileRepositoryProvider = DoubleCheck.provider(BubbleVolatileRepository_Factory.create(this.provideLauncherAppsProvider));
        Provider<BubblePersistentRepository> provider6 = DoubleCheck.provider(BubblePersistentRepository_Factory.create(this.contextProvider));
        this.bubblePersistentRepositoryProvider = provider6;
        Provider<BubbleDataRepository> provider7 = DoubleCheck.provider(BubbleDataRepository_Factory.create(this.bubbleVolatileRepositoryProvider, provider6, this.provideLauncherAppsProvider));
        this.bubbleDataRepositoryProvider = provider7;
        DelegateFactory delegateFactory5 = (DelegateFactory) this.newBubbleControllerProvider;
        Provider<BubbleController> provider8 = DoubleCheck.provider(BubbleModule_NewBubbleControllerFactory.create(this.contextProvider, this.notificationShadeWindowControllerProvider, this.statusBarStateControllerImplProvider, this.shadeControllerImplProvider, this.bubbleDataProvider, this.provideConfigurationControllerProvider, this.notificationInterruptStateProviderImplProvider, this.zenModeControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationGroupManagerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.featureFlagsProvider, this.dumpManagerProvider, this.floatingContentCoordinatorProvider, provider7, this.provideSysUiStateProvider, this.provideINotificationManagerProvider, this.provideIStatusBarServiceProvider, this.provideWindowManagerProvider, this.provideLauncherAppsProvider));
        this.newBubbleControllerProvider = provider8;
        delegateFactory5.setDelegatedProvider(provider8);
        this.bubbleOverflowActivityProvider = BubbleOverflowActivity_Factory.create(this.newBubbleControllerProvider);
        this.usbDebuggingActivityProvider = UsbDebuggingActivity_Factory.create(this.providesBroadcastDispatcherProvider);
        this.usbDebuggingSecondaryUserActivityProvider = UsbDebuggingSecondaryUserActivity_Factory.create(this.providesBroadcastDispatcherProvider);
        this.userCreatorProvider = UserCreator_Factory.create(this.contextProvider, this.provideUserManagerProvider);
        UserModule_ProvideEditUserInfoControllerFactory userModule_ProvideEditUserInfoControllerFactoryCreate = UserModule_ProvideEditUserInfoControllerFactory.create(builder.userModule);
        this.provideEditUserInfoControllerProvider = userModule_ProvideEditUserInfoControllerFactoryCreate;
        this.createUserActivityProvider = CreateUserActivity_Factory.create(this.userCreatorProvider, userModule_ProvideEditUserInfoControllerFactoryCreate, this.provideIActivityManagerProvider);
        Provider<Executor> provider9 = DoubleCheck.provider(ConcurrencyModule_ProvideExecutorFactory.create(this.provideBgLooperProvider));
        this.provideExecutorProvider = provider9;
        this.controlsListingControllerImplProvider = DoubleCheck.provider(ControlsListingControllerImpl_Factory.create(this.contextProvider, provider9));
        this.provideBackgroundDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory.create(this.provideBgLooperProvider));
        this.controlsControllerImplProvider = new DelegateFactory();
        this.provideDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideDelayableExecutorFactory.create(this.provideBgLooperProvider));
        this.globalActionsComponentProvider = new DelegateFactory();
        this.provideVibratorProvider = DoubleCheck.provider(SystemServicesModule_ProvideVibratorFactory.create(this.contextProvider));
        this.providesControlsFeatureEnabledProvider = DoubleCheck.provider(ControlsModule_ProvidesControlsFeatureEnabledFactory.create(this.providePackageManagerProvider));
        DelegateFactory delegateFactory6 = new DelegateFactory();
        this.controlsUiControllerImplProvider = delegateFactory6;
        Provider<ControlsComponent> provider10 = DoubleCheck.provider(ControlsComponent_Factory.create(this.providesControlsFeatureEnabledProvider, this.controlsControllerImplProvider, delegateFactory6, this.controlsListingControllerImplProvider));
        this.controlsComponentProvider = provider10;
        GlobalActionsDialog_Factory globalActionsDialog_FactoryCreate = GlobalActionsDialog_Factory.create(this.contextProvider, this.globalActionsComponentProvider, this.provideAudioManagerProvider, this.provideIDreamManagerProvider, this.provideDevicePolicyManagerProvider, this.provideLockPatternUtilsProvider, this.providesBroadcastDispatcherProvider, this.provideConnectivityManagagerProvider, this.provideTelephonyManagerProvider, this.provideContentResolverProvider, this.provideVibratorProvider, this.provideResourcesProvider, this.provideConfigurationControllerProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider, this.provideUserManagerProvider, this.provideTrustManagerProvider, this.provideIActivityManagerProvider, this.provideTelecomManagerProvider, this.provideMetricsLoggerProvider, this.notificationShadeDepthControllerProvider, this.sysuiColorExtractorProvider, this.provideIStatusBarServiceProvider, this.notificationShadeWindowControllerProvider, this.provideIWindowManagerProvider, this.provideBackgroundExecutorProvider, this.provideUiEventLoggerProvider, this.ringerModeTrackerImplProvider, this.provideSysUiStateProvider, this.provideMainHandlerProvider, provider10, this.provideCurrentUserContextTrackerProvider);
        this.globalActionsDialogProvider = globalActionsDialog_FactoryCreate;
        GlobalActionsImpl_Factory globalActionsImpl_FactoryCreate = GlobalActionsImpl_Factory.create(this.contextProvider, this.provideCommandQueueProvider, globalActionsDialog_FactoryCreate, this.blurUtilsProvider);
        this.globalActionsImplProvider = globalActionsImpl_FactoryCreate;
        DelegateFactory delegateFactory7 = (DelegateFactory) this.globalActionsComponentProvider;
        Provider<GlobalActionsComponent> provider11 = DoubleCheck.provider(GlobalActionsComponent_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.extensionControllerImplProvider, globalActionsImpl_FactoryCreate, this.statusBarKeyguardViewManagerProvider));
        this.globalActionsComponentProvider = provider11;
        delegateFactory7.setDelegatedProvider(provider11);
        this.controlActionCoordinatorImplProvider = DoubleCheck.provider(ControlActionCoordinatorImpl_Factory.create(this.contextProvider, this.provideDelayableExecutorProvider, this.provideMainDelayableExecutorProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider, this.globalActionsComponentProvider));
        Provider<CustomIconCache> provider12 = DoubleCheck.provider(CustomIconCache_Factory.create());
        this.customIconCacheProvider = provider12;
        DelegateFactory delegateFactory8 = (DelegateFactory) this.controlsUiControllerImplProvider;
        Provider<ControlsUiControllerImpl> provider13 = DoubleCheck.provider(ControlsUiControllerImpl_Factory.create(this.controlsControllerImplProvider, this.contextProvider, this.provideMainDelayableExecutorProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsListingControllerImplProvider, this.provideSharePreferencesProvider, this.controlActionCoordinatorImplProvider, this.activityStarterDelegateProvider, this.shadeControllerImplProvider, provider12));
        this.controlsUiControllerImplProvider = provider13;
        delegateFactory8.setDelegatedProvider(provider13);
        this.controlsBindingControllerImplProvider = DoubleCheck.provider(ControlsBindingControllerImpl_Factory.create(this.contextProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsControllerImplProvider));
        Provider<Optional<ControlsFavoritePersistenceWrapper>> providerAbsentJdkOptionalProvider = absentJdkOptionalProvider();
        this.optionalOfControlsFavoritePersistenceWrapperProvider = providerAbsentJdkOptionalProvider;
        DelegateFactory delegateFactory9 = (DelegateFactory) this.controlsControllerImplProvider;
        Provider<ControlsControllerImpl> provider14 = DoubleCheck.provider(ControlsControllerImpl_Factory.create(this.contextProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsUiControllerImplProvider, this.controlsBindingControllerImplProvider, this.controlsListingControllerImplProvider, this.providesBroadcastDispatcherProvider, providerAbsentJdkOptionalProvider, this.dumpManagerProvider));
        this.controlsControllerImplProvider = provider14;
        delegateFactory9.setDelegatedProvider(provider14);
        this.controlsProviderSelectorActivityProvider = ControlsProviderSelectorActivity_Factory.create(this.provideMainExecutorProvider, this.provideBackgroundExecutorProvider, this.controlsListingControllerImplProvider, this.controlsControllerImplProvider, this.globalActionsComponentProvider, this.providesBroadcastDispatcherProvider);
        this.controlsFavoritingActivityProvider = ControlsFavoritingActivity_Factory.create(this.provideMainExecutorProvider, this.controlsControllerImplProvider, this.controlsListingControllerImplProvider, this.providesBroadcastDispatcherProvider, this.globalActionsComponentProvider);
        this.controlsEditingActivityProvider = ControlsEditingActivity_Factory.create(this.controlsControllerImplProvider, this.providesBroadcastDispatcherProvider, this.globalActionsComponentProvider, this.customIconCacheProvider);
        this.controlsRequestDialogProvider = ControlsRequestDialog_Factory.create(this.controlsControllerImplProvider, this.providesBroadcastDispatcherProvider, this.controlsListingControllerImplProvider);
        Provider<TvPipComponent.Builder> provider15 = new Provider<TvPipComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.4
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // javax.inject.Provider
            public TvPipComponent.Builder get() {
                return new TvPipComponentBuilder();
            }
        };
        this.tvPipComponentBuilderProvider = provider15;
        this.pipMenuActivityProvider = PipMenuActivity_Factory.create(provider15, this.pipManagerProvider);
        this.mapOfClassOfAndProviderOfActivityProvider = MapProviderFactory.builder(14).put(TunerActivity.class, TunerActivity_Factory.create()).put(ForegroundServicesDialog.class, ForegroundServicesDialog_Factory.create()).put(WorkLockActivity.class, this.workLockActivityProvider).put(BrightnessDialog.class, this.brightnessDialogProvider).put(ScreenRecordDialog.class, this.screenRecordDialogProvider).put(BubbleOverflowActivity.class, this.bubbleOverflowActivityProvider).put(UsbDebuggingActivity.class, this.usbDebuggingActivityProvider).put(UsbDebuggingSecondaryUserActivity.class, this.usbDebuggingSecondaryUserActivityProvider).put(CreateUserActivity.class, this.createUserActivityProvider).put(ControlsProviderSelectorActivity.class, this.controlsProviderSelectorActivityProvider).put(ControlsFavoritingActivity.class, this.controlsFavoritingActivityProvider).put(ControlsEditingActivity.class, this.controlsEditingActivityProvider).put(ControlsRequestDialog.class, this.controlsRequestDialogProvider).put(PipMenuActivity.class, this.pipMenuActivityProvider).build();
        this.proximityCheckProvider = ProximitySensor_ProximityCheck_Factory.create(this.proximitySensorProvider, this.provideMainDelayableExecutorProvider);
        DozeFactory_Factory dozeFactory_FactoryCreate = DozeFactory_Factory.create(this.falsingManagerProxyProvider, this.dozeLogProvider, this.dozeParametersProvider, this.provideBatteryControllerProvider, this.asyncSensorManagerProvider, this.provideAlarmManagerProvider, this.wakefulnessLifecycleProvider, this.keyguardUpdateMonitorProvider, this.dockManagerImplProvider, SystemServicesModule_ProvideIWallPaperManagerFactory.create(), this.proximitySensorProvider, this.proximityCheckProvider, this.builderProvider4, this.provideMainHandlerProvider, this.biometricUnlockControllerProvider, this.providesBroadcastDispatcherProvider, this.dozeServiceHostProvider);
        this.dozeFactoryProvider = dozeFactory_FactoryCreate;
        this.dozeServiceProvider = DozeService_Factory.create(dozeFactory_FactoryCreate, this.providePluginManagerProvider);
        Provider<KeyguardLifecyclesDispatcher> provider16 = DoubleCheck.provider(KeyguardLifecyclesDispatcher_Factory.create(this.screenLifecycleProvider, this.wakefulnessLifecycleProvider));
        this.keyguardLifecyclesDispatcherProvider = provider16;
        this.keyguardServiceProvider = KeyguardService_Factory.create(this.newKeyguardViewMediatorProvider, provider16);
        this.dumpHandlerProvider = DumpHandler_Factory.create(this.contextProvider, this.dumpManagerProvider, this.logBufferEulogizerProvider);
        LogBufferFreezer_Factory logBufferFreezer_FactoryCreate = LogBufferFreezer_Factory.create(this.dumpManagerProvider, this.provideMainDelayableExecutorProvider);
        this.logBufferFreezerProvider = logBufferFreezer_FactoryCreate;
        this.systemUIServiceProvider = SystemUIService_Factory.create(this.provideMainHandlerProvider, this.dumpHandlerProvider, this.providesBroadcastDispatcherProvider, logBufferFreezer_FactoryCreate);
        this.systemUIAuxiliaryDumpServiceProvider = SystemUIAuxiliaryDumpService_Factory.create(this.dumpHandlerProvider);
        this.screenshotSmartActionsProvider = DoubleCheck.provider(ScreenshotSmartActions_Factory.create());
        ScreenshotNotificationsController_Factory screenshotNotificationsController_FactoryCreate = ScreenshotNotificationsController_Factory.create(this.contextProvider, this.provideWindowManagerProvider);
        this.screenshotNotificationsControllerProvider = screenshotNotificationsController_FactoryCreate;
        Provider<GlobalScreenshot> provider17 = DoubleCheck.provider(GlobalScreenshot_Factory.create(this.contextProvider, this.provideResourcesProvider, this.screenshotSmartActionsProvider, screenshotNotificationsController_FactoryCreate, this.provideUiEventLoggerProvider, this.provideUiBackgroundExecutorProvider));
        this.globalScreenshotProvider = provider17;
        this.takeScreenshotServiceProvider = TakeScreenshotService_Factory.create(provider17, this.provideUserManagerProvider, this.provideUiEventLoggerProvider);
        Provider<Looper> provider18 = DoubleCheck.provider(ConcurrencyModule_ProvideLongRunningLooperFactory.create());
        this.provideLongRunningLooperProvider = provider18;
        Provider<Executor> provider19 = DoubleCheck.provider(ConcurrencyModule_ProvideLongRunningExecutorFactory.create(provider18));
        this.provideLongRunningExecutorProvider = provider19;
        this.recordingServiceProvider = RecordingService_Factory.create(this.recordingControllerProvider, provider19, this.provideUiEventLoggerProvider, this.provideNotificationManagerProvider, this.provideCurrentUserContextTrackerProvider, this.keyguardDismissUtilProvider);
        this.mapOfClassOfAndProviderOfServiceProvider = MapProviderFactory.builder(7).put(DozeService.class, this.dozeServiceProvider).put(ImageWallpaper.class, ImageWallpaper_Factory.create()).put(KeyguardService.class, this.keyguardServiceProvider).put(SystemUIService.class, this.systemUIServiceProvider).put(SystemUIAuxiliaryDumpService.class, this.systemUIAuxiliaryDumpServiceProvider).put(TakeScreenshotService.class, this.takeScreenshotServiceProvider).put(RecordingService.class, this.recordingServiceProvider).build();
        this.authControllerProvider = DoubleCheck.provider(AuthController_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        Provider<String> provider20 = DoubleCheck.provider(SystemUIDefaultModule_ProvideLeakReportEmailFactory.create());
        this.provideLeakReportEmailProvider = provider20;
        Provider<LeakReporter> provider21 = DoubleCheck.provider(LeakReporter_Factory.create(this.contextProvider, this.provideLeakDetectorProvider, provider20));
        this.leakReporterProvider = provider21;
        Provider<GarbageMonitor> provider22 = DoubleCheck.provider(GarbageMonitor_Factory.create(this.contextProvider, this.provideBgLooperProvider, this.provideLeakDetectorProvider, provider21));
        this.garbageMonitorProvider = provider22;
        this.serviceProvider = DoubleCheck.provider(GarbageMonitor_Service_Factory.create(this.contextProvider, provider22));
        this.instantAppNotifierProvider = DoubleCheck.provider(InstantAppNotifier_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.provideUiBackgroundExecutorProvider, this.provideDividerProvider));
        this.latencyTesterProvider = DoubleCheck.provider(LatencyTester_Factory.create(this.contextProvider, this.biometricUnlockControllerProvider, this.providePowerManagerProvider, this.providesBroadcastDispatcherProvider));
        this.powerUIProvider = DoubleCheck.provider(PowerUI_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideCommandQueueProvider, this.provideStatusBarProvider));
        this.screenDecorationsProvider = DoubleCheck.provider(ScreenDecorations_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider, this.tunerServiceImplProvider));
        this.shortcutKeyDispatcherProvider = DoubleCheck.provider(ShortcutKeyDispatcher_Factory.create(this.contextProvider, this.provideDividerProvider, this.provideRecentsProvider));
        this.sizeCompatModeActivityControllerProvider = DoubleCheck.provider(SizeCompatModeActivityController_Factory.create(this.contextProvider, this.provideActivityManagerWrapperProvider, this.provideCommandQueueProvider));
        this.sliceBroadcastRelayHandlerProvider = DoubleCheck.provider(SliceBroadcastRelayHandler_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider));
        this.systemActionsProvider = DoubleCheck.provider(SystemActions_Factory.create(this.contextProvider));
        this.themeOverlayControllerProvider = DoubleCheck.provider(ThemeOverlayController_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideBgHandlerProvider));
        this.toastUIProvider = DoubleCheck.provider(ToastUI_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        this.tvStatusBarProvider = DoubleCheck.provider(TvStatusBar_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.assistManagerProvider));
        this.volumeUIProvider = DoubleCheck.provider(VolumeUI_Factory.create(this.contextProvider, this.volumeDialogComponentProvider));
        this.windowMagnificationProvider = DoubleCheck.provider(WindowMagnification_Factory.create(this.contextProvider, this.provideMainHandlerProvider));
        this.mapOfClassOfAndProviderOfSystemUIProvider = MapProviderFactory.builder(22).put(AuthController.class, this.authControllerProvider).put(Divider.class, this.provideDividerProvider).put(FODCircleViewImpl.class, this.fODCircleViewImplProvider).put(GarbageMonitor.Service.class, this.serviceProvider).put(GlobalActionsComponent.class, this.globalActionsComponentProvider).put(InstantAppNotifier.class, this.instantAppNotifierProvider).put(KeyguardViewMediator.class, this.newKeyguardViewMediatorProvider).put(LatencyTester.class, this.latencyTesterProvider).put(PipUI.class, this.pipUIProvider).put(PowerUI.class, this.powerUIProvider).put(Recents.class, this.provideRecentsProvider).put(ScreenDecorations.class, this.screenDecorationsProvider).put(ShortcutKeyDispatcher.class, this.shortcutKeyDispatcherProvider).put(SizeCompatModeActivityController.class, this.sizeCompatModeActivityControllerProvider).put(SliceBroadcastRelayHandler.class, this.sliceBroadcastRelayHandlerProvider).put(StatusBar.class, this.provideStatusBarProvider).put(SystemActions.class, this.systemActionsProvider).put(ThemeOverlayController.class, this.themeOverlayControllerProvider).put(ToastUI.class, this.toastUIProvider).put(TvStatusBar.class, this.tvStatusBarProvider).put(VolumeUI.class, this.volumeUIProvider).put(WindowMagnification.class, this.windowMagnificationProvider).build();
        this.overviewProxyRecentsImplProvider = DoubleCheck.provider(OverviewProxyRecentsImpl_Factory.create(this.optionalOfLazyOfStatusBarProvider, this.optionalOfDividerProvider));
        this.mapOfClassOfAndProviderOfRecentsImplementationProvider = MapProviderFactory.builder(1).put(OverviewProxyRecentsImpl.class, this.overviewProxyRecentsImplProvider).build();
        Provider<Optional<StatusBar>> providerOf = PresentJdkOptionalInstanceProvider.of(this.provideStatusBarProvider);
        this.optionalOfStatusBarProvider = providerOf;
        this.actionProxyReceiverProvider = ActionProxyReceiver_Factory.create(providerOf, this.provideActivityManagerWrapperProvider, this.screenshotSmartActionsProvider);
        this.deleteScreenshotReceiverProvider = DeleteScreenshotReceiver_Factory.create(this.screenshotSmartActionsProvider, this.provideBackgroundExecutorProvider);
        this.smartActionsReceiverProvider = SmartActionsReceiver_Factory.create(this.screenshotSmartActionsProvider);
        MediaOutputDialogFactory_Factory mediaOutputDialogFactory_FactoryCreate = MediaOutputDialogFactory_Factory.create(this.contextProvider, this.provideMediaSessionManagerProvider, this.provideLocalBluetoothControllerProvider, this.shadeControllerImplProvider, this.activityStarterDelegateProvider, this.provideNotificationEntryManagerProvider, this.provideUiEventLoggerProvider, this.provideMediaRouter2ManagerProvider);
        this.mediaOutputDialogFactoryProvider = mediaOutputDialogFactory_FactoryCreate;
        this.mediaOutputDialogReceiverProvider = MediaOutputDialogReceiver_Factory.create(mediaOutputDialogFactory_FactoryCreate);
        MapProviderFactory mapProviderFactoryBuild = MapProviderFactory.builder(4).put(ActionProxyReceiver.class, this.actionProxyReceiverProvider).put(DeleteScreenshotReceiver.class, this.deleteScreenshotReceiverProvider).put(SmartActionsReceiver.class, this.smartActionsReceiverProvider).put(MediaOutputDialogReceiver.class, this.mediaOutputDialogReceiverProvider).build();
        this.mapOfClassOfAndProviderOfBroadcastReceiverProvider = mapProviderFactoryBuild;
        DelegateFactory delegateFactory10 = (DelegateFactory) this.contextComponentResolverProvider;
        Provider<ContextComponentResolver> provider23 = DoubleCheck.provider(ContextComponentResolver_Factory.create(this.mapOfClassOfAndProviderOfActivityProvider, this.mapOfClassOfAndProviderOfServiceProvider, this.mapOfClassOfAndProviderOfSystemUIProvider, this.mapOfClassOfAndProviderOfRecentsImplementationProvider, mapProviderFactoryBuild));
        this.contextComponentResolverProvider = provider23;
        delegateFactory10.setDelegatedProvider(provider23);
        this.provideAllowNotificationLongPressProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory.create());
        this.flashlightControllerImplProvider = DoubleCheck.provider(FlashlightControllerImpl_Factory.create(this.contextProvider));
        this.provideNightDisplayListenerProvider = DoubleCheck.provider(DependencyProvider_ProvideNightDisplayListenerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideBgHandlerProvider));
        this.managedProfileControllerImplProvider = DoubleCheck.provider(ManagedProfileControllerImpl_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider));
    }

    private void initialize5(Builder builder) {
        this.opaEnabledDispatcherProvider = DoubleCheck.provider(OpaEnabledDispatcher_Factory.create(this.provideStatusBarProvider));
        Provider<AssistantPresenceHandler> provider = DoubleCheck.provider(AssistantPresenceHandler_Factory.create(this.contextProvider, this.provideAssistUtilsProvider));
        this.assistantPresenceHandlerProvider = provider;
        this.assistManagerGoogleProvider = DoubleCheck.provider(AssistManagerGoogle_Factory.create(this.deviceProvisionedControllerImplProvider, this.contextProvider, this.provideAssistUtilsProvider, this.assistHandleBehaviorControllerProvider, this.provideCommandQueueProvider, this.phoneStateMonitorProvider, this.overviewProxyServiceProvider, this.provideConfigurationControllerProvider, this.provideSysUiStateProvider, this.googleDefaultUiControllerProvider, this.assistLoggerProvider, this.providesBroadcastDispatcherProvider, this.opaEnabledDispatcherProvider, this.keyguardUpdateMonitorProvider, this.navigationModeControllerProvider, provider, this.provideMainHandlerProvider));
        this.securityControllerImplProvider = DoubleCheck.provider(SecurityControllerImpl_Factory.create(this.contextProvider, this.provideBgHandlerProvider, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider));
        this.statusBarWindowControllerProvider = DoubleCheck.provider(StatusBarWindowController_Factory.create(this.contextProvider, this.provideWindowManagerProvider, this.superStatusBarViewFactoryProvider, this.provideResourcesProvider));
        this.fragmentServiceProvider = DoubleCheck.provider(FragmentService_Factory.create(this.tvSystemUIRootComponentProvider, this.provideConfigurationControllerProvider));
        this.accessibilityManagerWrapperProvider = DoubleCheck.provider(AccessibilityManagerWrapper_Factory.create(this.contextProvider));
        this.tunablePaddingServiceProvider = DoubleCheck.provider(TunablePadding_TunablePaddingService_Factory.create(this.tunerServiceImplProvider));
        this.uiOffloadThreadProvider = DoubleCheck.provider(UiOffloadThread_Factory.create());
        this.powerNotificationWarningsProvider = DoubleCheck.provider(PowerNotificationWarnings_Factory.create(this.contextProvider, this.activityStarterDelegateProvider));
        this.provideNotificationBlockingHelperManagerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationBlockingHelperManagerFactory.create(this.contextProvider, this.provideNotificationGutsManagerProvider, this.provideNotificationEntryManagerProvider, this.provideMetricsLoggerProvider));
        this.provideSensorPrivacyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideSensorPrivacyManagerFactory.create(this.contextProvider));
        ForegroundServiceLifetimeExtender_Factory foregroundServiceLifetimeExtender_FactoryCreate = ForegroundServiceLifetimeExtender_Factory.create(this.notificationInteractionTrackerProvider, this.bindSystemClockProvider);
        this.foregroundServiceLifetimeExtenderProvider = foregroundServiceLifetimeExtender_FactoryCreate;
        this.foregroundServiceNotificationListenerProvider = DoubleCheck.provider(ForegroundServiceNotificationListener_Factory.create(this.contextProvider, this.foregroundServiceControllerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, foregroundServiceLifetimeExtender_FactoryCreate, this.bindSystemClockProvider));
        this.clockManagerProvider = DoubleCheck.provider(ClockManager_Factory.create(this.contextProvider, this.injectionInflationControllerProvider, this.providePluginManagerProvider, this.sysuiColorExtractorProvider, this.dockManagerImplProvider, this.providesBroadcastDispatcherProvider));
        this.provideDevicePolicyManagerWrapperProvider = DoubleCheck.provider(DependencyProvider_ProvideDevicePolicyManagerWrapperFactory.create(builder.dependencyProvider));
        this.keyguardSecurityModelProvider = DoubleCheck.provider(KeyguardSecurityModel_Factory.create(this.contextProvider));
        this.providePulseControllerProvider = DoubleCheck.provider(DependencyProvider_ProvidePulseControllerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideMainHandlerProvider, this.provideBackgroundExecutorProvider));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.qSTileHostProvider = delegateFactory;
        this.wifiTileProvider = WifiTile_Factory.create(delegateFactory, this.networkControllerImplProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider);
        this.bluetoothTileProvider = BluetoothTile_Factory.create(this.qSTileHostProvider, this.bluetoothControllerImplProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider);
        this.cellularTileProvider = CellularTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider);
        this.dndTileProvider = DndTile_Factory.create(this.qSTileHostProvider, this.zenModeControllerImplProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider, this.provideSharePreferencesProvider);
        this.colorInversionTileProvider = ColorInversionTile_Factory.create(this.qSTileHostProvider);
        this.airplaneModeTileProvider = AirplaneModeTile_Factory.create(this.qSTileHostProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider, this.keyguardStateControllerImplProvider);
        this.workModeTileProvider = WorkModeTile_Factory.create(this.qSTileHostProvider, this.managedProfileControllerImplProvider);
        this.rotationLockTileProvider = RotationLockTile_Factory.create(this.qSTileHostProvider, this.rotationLockControllerImplProvider);
        this.flashlightTileProvider = FlashlightTile_Factory.create(this.qSTileHostProvider, this.flashlightControllerImplProvider);
        this.locationTileProvider = LocationTile_Factory.create(this.qSTileHostProvider, this.locationControllerImplProvider, this.keyguardStateControllerImplProvider, this.activityStarterDelegateProvider);
        this.castTileProvider = CastTile_Factory.create(this.qSTileHostProvider, this.castControllerImplProvider, this.keyguardStateControllerImplProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider);
        this.hotspotTileProvider = HotspotTile_Factory.create(this.qSTileHostProvider, this.hotspotControllerImplProvider, this.provideDataSaverControllerProvider);
        this.userTileProvider = UserTile_Factory.create(this.qSTileHostProvider, this.userSwitcherControllerProvider, this.userInfoControllerImplProvider);
        this.batterySaverTileProvider = BatterySaverTile_Factory.create(this.qSTileHostProvider, this.provideBatteryControllerProvider);
        this.dataSaverTileProvider = DataSaverTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider);
        this.nightDisplayTileProvider = NightDisplayTile_Factory.create(this.qSTileHostProvider, this.locationControllerImplProvider);
        this.nfcTileProvider = NfcTile_Factory.create(this.qSTileHostProvider, this.providesBroadcastDispatcherProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider);
        this.sensorPrivacyTileProvider = SensorPrivacyTile_Factory.create(this.qSTileHostProvider, this.provideSensorPrivacyManagerProvider, this.activityStarterDelegateProvider);
        this.memoryTileProvider = GarbageMonitor_MemoryTile_Factory.create(this.qSTileHostProvider, this.garbageMonitorProvider, this.activityStarterDelegateProvider);
        this.uiModeNightTileProvider = UiModeNightTile_Factory.create(this.qSTileHostProvider, this.provideConfigurationControllerProvider, this.provideBatteryControllerProvider, this.locationControllerImplProvider);
        this.screenRecordTileProvider = ScreenRecordTile_Factory.create(this.qSTileHostProvider, this.recordingControllerProvider, this.keyguardDismissUtilProvider);
        this.ambientDisplayTileProvider = AmbientDisplayTile_Factory.create(this.qSTileHostProvider);
        this.aODTileProvider = AODTile_Factory.create(this.qSTileHostProvider, this.provideBatteryControllerProvider);
        this.caffeineTileProvider = CaffeineTile_Factory.create(this.qSTileHostProvider);
        this.powerMenuTileProvider = PowerMenuTile_Factory.create(this.qSTileHostProvider);
        this.dataSwitchTileProvider = DataSwitchTile_Factory.create(this.qSTileHostProvider);
        this.headsUpTileProvider = HeadsUpTile_Factory.create(this.qSTileHostProvider);
        this.liveDisplayTileProvider = LiveDisplayTile_Factory.create(this.qSTileHostProvider);
        this.powerShareTileProvider = PowerShareTile_Factory.create(this.qSTileHostProvider, this.provideBatteryControllerProvider);
        this.profilesTileProvider = ProfilesTile_Factory.create(this.qSTileHostProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider);
        this.readingModeTileProvider = ReadingModeTile_Factory.create(this.qSTileHostProvider);
        this.syncTileProvider = SyncTile_Factory.create(this.qSTileHostProvider);
        this.usbTetherTileProvider = UsbTetherTile_Factory.create(this.qSTileHostProvider);
        this.volumeTileProvider = VolumeTile_Factory.create(this.qSTileHostProvider);
        this.vpnTileProvider = VpnTile_Factory.create(this.qSTileHostProvider, this.securityControllerImplProvider, this.keyguardStateControllerImplProvider, this.activityStarterDelegateProvider);
        this.weatherTileProvider = WeatherTile_Factory.create(this.qSTileHostProvider, this.activityStarterDelegateProvider);
        this.cPUInfoTileProvider = CPUInfoTile_Factory.create(this.qSTileHostProvider);
        this.fPSInfoTileProvider = FPSInfoTile_Factory.create(this.qSTileHostProvider);
        this.gamingModeTileProvider = GamingModeTile_Factory.create(this.qSTileHostProvider);
        this.smartPixelsTileProvider = SmartPixelsTile_Factory.create(this.qSTileHostProvider);
        this.rebootTileProvider = RebootTile_Factory.create(this.qSTileHostProvider);
        this.soundTileProvider = SoundTile_Factory.create(this.qSTileHostProvider);
        this.compassTileProvider = CompassTile_Factory.create(this.qSTileHostProvider);
        this.antiFlickerTileProvider = AntiFlickerTile_Factory.create(this.qSTileHostProvider);
        this.localeTileProvider = LocaleTile_Factory.create(this.qSTileHostProvider);
        this.sleepModeTileProvider = SleepModeTile_Factory.create(this.qSTileHostProvider);
        OnTheGoTile_Factory onTheGoTile_FactoryCreate = OnTheGoTile_Factory.create(this.qSTileHostProvider);
        this.onTheGoTileProvider = onTheGoTile_FactoryCreate;
        this.qSFactoryImplProvider = DoubleCheck.provider(QSFactoryImpl_Factory.create(this.qSTileHostProvider, this.wifiTileProvider, this.bluetoothTileProvider, this.cellularTileProvider, this.dndTileProvider, this.colorInversionTileProvider, this.airplaneModeTileProvider, this.workModeTileProvider, this.rotationLockTileProvider, this.flashlightTileProvider, this.locationTileProvider, this.castTileProvider, this.hotspotTileProvider, this.userTileProvider, this.batterySaverTileProvider, this.dataSaverTileProvider, this.nightDisplayTileProvider, this.nfcTileProvider, this.sensorPrivacyTileProvider, this.memoryTileProvider, this.uiModeNightTileProvider, this.screenRecordTileProvider, this.ambientDisplayTileProvider, this.aODTileProvider, this.caffeineTileProvider, this.powerMenuTileProvider, this.dataSwitchTileProvider, this.headsUpTileProvider, this.liveDisplayTileProvider, this.powerShareTileProvider, this.profilesTileProvider, this.readingModeTileProvider, this.syncTileProvider, this.usbTetherTileProvider, this.volumeTileProvider, this.vpnTileProvider, this.weatherTileProvider, this.cPUInfoTileProvider, this.fPSInfoTileProvider, this.gamingModeTileProvider, this.smartPixelsTileProvider, this.rebootTileProvider, this.soundTileProvider, this.compassTileProvider, this.antiFlickerTileProvider, this.localeTileProvider, this.sleepModeTileProvider, onTheGoTile_FactoryCreate));
        AutoAddTracker_Builder_Factory autoAddTracker_Builder_FactoryCreate = AutoAddTracker_Builder_Factory.create(this.contextProvider);
        this.builderProvider6 = autoAddTracker_Builder_FactoryCreate;
        this.provideAutoTileManagerProvider = QSModule_ProvideAutoTileManagerFactory.create(this.contextProvider, autoAddTracker_Builder_FactoryCreate, this.qSTileHostProvider, this.provideBgHandlerProvider, this.hotspotControllerImplProvider, this.provideDataSaverControllerProvider, this.managedProfileControllerImplProvider, this.provideNightDisplayListenerProvider, this.castControllerImplProvider);
        Provider<LogBuffer> provider2 = DoubleCheck.provider(LogModule_ProvideQuickSettingsLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideQuickSettingsLogBufferProvider = provider2;
        QSLogger_Factory qSLogger_FactoryCreate = QSLogger_Factory.create(provider2);
        this.qSLoggerProvider = qSLogger_FactoryCreate;
        DelegateFactory delegateFactory2 = (DelegateFactory) this.qSTileHostProvider;
        Provider<QSTileHost> provider3 = DoubleCheck.provider(QSTileHost_Factory.create(this.contextProvider, this.statusBarIconControllerImplProvider, this.qSFactoryImplProvider, this.provideMainHandlerProvider, this.provideBgLooperProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.provideAutoTileManagerProvider, this.dumpManagerProvider, this.providesBroadcastDispatcherProvider, this.optionalOfStatusBarProvider, qSLogger_FactoryCreate, this.provideUiEventLoggerProvider));
        this.qSTileHostProvider = provider3;
        delegateFactory2.setDelegatedProvider(provider3);
        this.context = builder.context;
        Provider<MediaHostStatesManager> provider4 = DoubleCheck.provider(MediaHostStatesManager_Factory.create());
        this.mediaHostStatesManagerProvider = provider4;
        this.mediaViewControllerProvider = MediaViewController_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider, provider4);
        Provider<RepeatableExecutor> provider5 = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory.create(this.provideBackgroundDelayableExecutorProvider));
        this.provideBackgroundRepeatableExecutorProvider = provider5;
        SeekBarViewModel_Factory seekBarViewModel_FactoryCreate = SeekBarViewModel_Factory.create(provider5);
        this.seekBarViewModelProvider = seekBarViewModel_FactoryCreate;
        MediaControlPanel_Factory mediaControlPanel_FactoryCreate = MediaControlPanel_Factory.create(this.contextProvider, this.provideBackgroundExecutorProvider, this.activityStarterDelegateProvider, this.mediaViewControllerProvider, seekBarViewModel_FactoryCreate, this.mediaDataManagerProvider, this.keyguardDismissUtilProvider, this.mediaOutputDialogFactoryProvider);
        this.mediaControlPanelProvider = mediaControlPanel_FactoryCreate;
        Provider<MediaCarouselController> provider6 = DoubleCheck.provider(MediaCarouselController_Factory.create(this.contextProvider, mediaControlPanel_FactoryCreate, this.provideVisualStabilityManagerProvider, this.mediaHostStatesManagerProvider, this.activityStarterDelegateProvider, this.provideMainDelayableExecutorProvider, this.mediaDataManagerProvider, this.provideConfigurationControllerProvider, this.falsingManagerProxyProvider));
        this.mediaCarouselControllerProvider = provider6;
        this.mediaHierarchyManagerProvider = DoubleCheck.provider(MediaHierarchyManager_Factory.create(this.contextProvider, this.statusBarStateControllerImplProvider, this.keyguardStateControllerImplProvider, this.keyguardBypassControllerProvider, provider6, this.notificationLockscreenUserManagerImplProvider, this.wakefulnessLifecycleProvider));
        MediaHost_Factory mediaHost_FactoryCreate = MediaHost_Factory.create(MediaHost_MediaHostStateHolder_Factory.create(), this.mediaHierarchyManagerProvider, this.mediaDataManagerProvider, this.mediaHostStatesManagerProvider);
        this.mediaHostProvider = mediaHost_FactoryCreate;
        this.keyguardMediaControllerProvider = DoubleCheck.provider(KeyguardMediaController_Factory.create(mediaHost_FactoryCreate, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider));
        Provider<PeopleHubDataSourceImpl> provider7 = DoubleCheck.provider(PeopleHubDataSourceImpl_Factory.create(this.provideNotificationEntryManagerProvider, this.notificationPersonExtractorPluginBoundaryProvider, this.provideUserManagerProvider, this.provideLauncherAppsProvider, this.providePackageManagerProvider, this.contextProvider, this.provideNotificationListenerProvider, this.provideBackgroundExecutorProvider, this.provideMainExecutorProvider, this.notificationLockscreenUserManagerImplProvider, this.peopleNotificationIdentifierImplProvider));
        this.peopleHubDataSourceImplProvider = provider7;
        Provider<PeopleHubViewModelFactoryDataSourceImpl> provider8 = DoubleCheck.provider(PeopleHubViewModelFactoryDataSourceImpl_Factory.create(this.activityStarterDelegateProvider, provider7));
        this.peopleHubViewModelFactoryDataSourceImplProvider = provider8;
        this.peopleHubViewAdapterImplProvider = DoubleCheck.provider(PeopleHubViewAdapterImpl_Factory.create(provider8));
        Provider<LogBuffer> provider9 = DoubleCheck.provider(LogModule_ProvideNotificationSectionLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotificationSectionLogBufferProvider = provider9;
        this.notificationSectionsLoggerProvider = DoubleCheck.provider(NotificationSectionsLogger_Factory.create(provider9));
        this.provideLatencyTrackerProvider = DoubleCheck.provider(SystemServicesModule_ProvideLatencyTrackerFactory.create(this.contextProvider));
        this.provideActivityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideActivityManagerFactory.create(this.contextProvider));
        this.providerLayoutInflaterProvider = DoubleCheck.provider(DependencyProvider_ProviderLayoutInflaterFactory.create(builder.dependencyProvider, this.contextProvider));
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public BootCompleteCacheImpl provideBootCacheImpl() {
        return this.bootCompleteCacheImplProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public ConfigurationController getConfigurationController() {
        return this.provideConfigurationControllerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public ContextComponentHelper getContextComponentHelper() {
        return this.contextComponentResolverProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public DumpManager createDumpManager() {
        return this.dumpManagerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public InitController getInitController() {
        return this.initControllerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(SystemUIAppComponentFactory systemUIAppComponentFactory) {
        injectSystemUIAppComponentFactory(systemUIAppComponentFactory);
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(KeyguardSliceProvider keyguardSliceProvider) {
        injectKeyguardSliceProvider(keyguardSliceProvider);
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public Dependency.DependencyInjector createDependency() {
        return new DependencyInjectorImpl();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public FragmentService.FragmentCreator createFragmentCreator() {
        return new FragmentCreatorImpl();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public InjectionInflationController.ViewCreator createViewCreator() {
        return new ViewCreatorImpl();
    }

    private SystemUIAppComponentFactory injectSystemUIAppComponentFactory(SystemUIAppComponentFactory systemUIAppComponentFactory) {
        SystemUIAppComponentFactory_MembersInjector.injectMComponentHelper(systemUIAppComponentFactory, this.contextComponentResolverProvider.get());
        return systemUIAppComponentFactory;
    }

    private KeyguardSliceProvider injectKeyguardSliceProvider(KeyguardSliceProvider keyguardSliceProvider) {
        KeyguardSliceProvider_MembersInjector.injectMDozeParameters(keyguardSliceProvider, this.dozeParametersProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMZenModeController(keyguardSliceProvider, this.zenModeControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMNextAlarmController(keyguardSliceProvider, this.nextAlarmControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMAlarmManager(keyguardSliceProvider, this.provideAlarmManagerProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMContentResolver(keyguardSliceProvider, this.provideContentResolverProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMMediaManager(keyguardSliceProvider, this.provideNotificationMediaManagerProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMStatusBarStateController(keyguardSliceProvider, this.statusBarStateControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMKeyguardBypassController(keyguardSliceProvider, this.keyguardBypassControllerProvider.get());
        return keyguardSliceProvider;
    }

    private static <T> Provider<Optional<T>> absentJdkOptionalProvider() {
        return ABSENT_JDK_OPTIONAL_PROVIDER;
    }

    private static final class PresentJdkOptionalLazyProvider<T> implements Provider<Optional<Lazy<T>>> {
        private final Provider<T> delegate;

        private PresentJdkOptionalLazyProvider(Provider<T> provider) {
            this.delegate = (Provider) Preconditions.checkNotNull(provider);
        }

        @Override // javax.inject.Provider
        public Optional<Lazy<T>> get() {
            return Optional.of(DoubleCheck.lazy(this.delegate));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static <T> Provider<Optional<Lazy<T>>> of(Provider<T> provider) {
            return new PresentJdkOptionalLazyProvider(provider);
        }
    }

    private static final class PresentJdkOptionalInstanceProvider<T> implements Provider<Optional<T>> {
        private final Provider<T> delegate;

        private PresentJdkOptionalInstanceProvider(Provider<T> provider) {
            this.delegate = (Provider) Preconditions.checkNotNull(provider);
        }

        @Override // javax.inject.Provider
        public Optional<T> get() {
            return Optional.of(this.delegate.get());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static <T> Provider<Optional<T>> of(Provider<T> provider) {
            return new PresentJdkOptionalInstanceProvider(provider);
        }
    }

    private static final class Builder implements TvSystemUIRootComponent.Builder {
        private Context context;
        private DependencyProvider dependencyProvider;
        private UserModule userModule;

        private Builder() {
        }

        @Override // com.android.systemui.tv.TvSystemUIRootComponent.Builder
        public TvSystemUIRootComponent build() {
            if (this.dependencyProvider == null) {
                this.dependencyProvider = new DependencyProvider();
            }
            if (this.userModule == null) {
                this.userModule = new UserModule();
            }
            if (this.context == null) {
                throw new IllegalStateException(Context.class.getCanonicalName() + " must be set");
            }
            return new DaggerTvSystemUIRootComponent(this);
        }

        @Override // com.android.systemui.tv.TvSystemUIRootComponent.Builder
        public Builder context(Context context) {
            this.context = (Context) Preconditions.checkNotNull(context);
            return this;
        }
    }

    private final class DependencyInjectorImpl implements Dependency.DependencyInjector {
        private DependencyInjectorImpl() {
        }

        @Override // com.android.systemui.Dependency.DependencyInjector
        public void createSystemUI(Dependency dependency) {
            injectDependency(dependency);
        }

        private Dependency injectDependency(Dependency dependency) {
            Dependency_MembersInjector.injectMDumpManager(dependency, (DumpManager) DaggerTvSystemUIRootComponent.this.dumpManagerProvider.get());
            Dependency_MembersInjector.injectMActivityStarter(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider));
            Dependency_MembersInjector.injectMBroadcastDispatcher(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider));
            Dependency_MembersInjector.injectMAsyncSensorManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.asyncSensorManagerProvider));
            Dependency_MembersInjector.injectMBluetoothController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.bluetoothControllerImplProvider));
            Dependency_MembersInjector.injectMLocationController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.locationControllerImplProvider));
            Dependency_MembersInjector.injectMRotationLockController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.rotationLockControllerImplProvider));
            Dependency_MembersInjector.injectMNetworkController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.networkControllerImplProvider));
            Dependency_MembersInjector.injectMZenModeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider));
            Dependency_MembersInjector.injectMHotspotController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.hotspotControllerImplProvider));
            Dependency_MembersInjector.injectMCastController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.castControllerImplProvider));
            Dependency_MembersInjector.injectMFlashlightController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.flashlightControllerImplProvider));
            Dependency_MembersInjector.injectMUserSwitcherController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.userSwitcherControllerProvider));
            Dependency_MembersInjector.injectMUserInfoController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.userInfoControllerImplProvider));
            Dependency_MembersInjector.injectMKeyguardMonitor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider));
            Dependency_MembersInjector.injectMKeyguardUpdateMonitor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardUpdateMonitorProvider));
            Dependency_MembersInjector.injectMBatteryController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBatteryControllerProvider));
            Dependency_MembersInjector.injectMNightDisplayListener(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNightDisplayListenerProvider));
            Dependency_MembersInjector.injectMManagedProfileController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.managedProfileControllerImplProvider));
            Dependency_MembersInjector.injectMNextAlarmController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.nextAlarmControllerImplProvider));
            Dependency_MembersInjector.injectMDataSaverController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDataSaverControllerProvider));
            Dependency_MembersInjector.injectMAccessibilityController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.accessibilityControllerProvider));
            Dependency_MembersInjector.injectMDeviceProvisionedController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.deviceProvisionedControllerImplProvider));
            Dependency_MembersInjector.injectMPluginManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providePluginManagerProvider));
            Dependency_MembersInjector.injectMAssistManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.assistManagerGoogleProvider));
            Dependency_MembersInjector.injectMSecurityController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.securityControllerImplProvider));
            Dependency_MembersInjector.injectMLeakDetector(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideLeakDetectorProvider));
            Dependency_MembersInjector.injectMLeakReporter(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.leakReporterProvider));
            Dependency_MembersInjector.injectMGarbageMonitor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.garbageMonitorProvider));
            Dependency_MembersInjector.injectMTunerService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider));
            Dependency_MembersInjector.injectMNotificationShadeWindowController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationShadeWindowControllerProvider));
            Dependency_MembersInjector.injectMTempStatusBarWindowController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarWindowControllerProvider));
            Dependency_MembersInjector.injectMDarkIconDispatcher(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.darkIconDispatcherImplProvider));
            Dependency_MembersInjector.injectMConfigurationController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider));
            Dependency_MembersInjector.injectMStatusBarIconController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarIconControllerImplProvider));
            Dependency_MembersInjector.injectMScreenLifecycle(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.screenLifecycleProvider));
            Dependency_MembersInjector.injectMWakefulnessLifecycle(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.wakefulnessLifecycleProvider));
            Dependency_MembersInjector.injectMFragmentService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.fragmentServiceProvider));
            Dependency_MembersInjector.injectMExtensionController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.extensionControllerImplProvider));
            Dependency_MembersInjector.injectMPluginDependencyProvider(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.pluginDependencyProvider));
            Dependency_MembersInjector.injectMLocalBluetoothManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideLocalBluetoothControllerProvider));
            Dependency_MembersInjector.injectMVolumeDialogController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.volumeDialogControllerImplProvider));
            Dependency_MembersInjector.injectMMetricsLogger(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideMetricsLoggerProvider));
            Dependency_MembersInjector.injectMAccessibilityManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.accessibilityManagerWrapperProvider));
            Dependency_MembersInjector.injectMSysuiColorExtractor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.sysuiColorExtractorProvider));
            Dependency_MembersInjector.injectMTunablePaddingService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.tunablePaddingServiceProvider));
            Dependency_MembersInjector.injectMForegroundServiceController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.foregroundServiceControllerProvider));
            Dependency_MembersInjector.injectMUiOffloadThread(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.uiOffloadThreadProvider));
            Dependency_MembersInjector.injectMWarningsUI(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.powerNotificationWarningsProvider));
            Dependency_MembersInjector.injectMLightBarController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.lightBarControllerProvider));
            Dependency_MembersInjector.injectMIWindowManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideIWindowManagerProvider));
            Dependency_MembersInjector.injectMOverviewProxyService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.overviewProxyServiceProvider));
            Dependency_MembersInjector.injectMNavBarModeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.navigationModeControllerProvider));
            Dependency_MembersInjector.injectMEnhancedEstimates(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.enhancedEstimatesImplProvider));
            Dependency_MembersInjector.injectMVibratorHelper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.vibratorHelperProvider));
            Dependency_MembersInjector.injectMIStatusBarService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideIStatusBarServiceProvider));
            Dependency_MembersInjector.injectMDisplayMetrics(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDisplayMetricsProvider));
            Dependency_MembersInjector.injectMLockscreenGestureLogger(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.lockscreenGestureLoggerProvider));
            Dependency_MembersInjector.injectMKeyguardEnvironment(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardEnvironmentImplProvider));
            Dependency_MembersInjector.injectMShadeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationRemoteInputManagerCallback(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarRemoteInputCallbackProvider));
            Dependency_MembersInjector.injectMAppOpsController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.appOpsControllerImplProvider));
            Dependency_MembersInjector.injectMNavigationBarController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNavigationBarControllerProvider));
            Dependency_MembersInjector.injectMStatusBarStateController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationLockscreenUserManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider));
            Dependency_MembersInjector.injectMNotificationGroupAlertTransferHelper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationGroupAlertTransferHelperProvider));
            Dependency_MembersInjector.injectMNotificationGroupManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationGroupManagerProvider));
            Dependency_MembersInjector.injectMVisualStabilityManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideVisualStabilityManagerProvider));
            Dependency_MembersInjector.injectMNotificationGutsManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationGutsManagerProvider));
            Dependency_MembersInjector.injectMNotificationMediaManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationMediaManagerProvider));
            Dependency_MembersInjector.injectMNotificationBlockingHelperManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationBlockingHelperManagerProvider));
            Dependency_MembersInjector.injectMNotificationRemoteInputManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationRemoteInputManagerProvider));
            Dependency_MembersInjector.injectMSmartReplyConstants(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.smartReplyConstantsProvider));
            Dependency_MembersInjector.injectMNotificationListener(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationListenerProvider));
            Dependency_MembersInjector.injectMNotificationLogger(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationLoggerProvider));
            Dependency_MembersInjector.injectMNotificationViewHierarchyManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationViewHierarchyManagerProvider));
            Dependency_MembersInjector.injectMNotificationFilter(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationFilterProvider));
            Dependency_MembersInjector.injectMKeyguardDismissUtil(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardDismissUtilProvider));
            Dependency_MembersInjector.injectMSmartReplyController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideSmartReplyControllerProvider));
            Dependency_MembersInjector.injectMRemoteInputQuickSettingsDisabler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.remoteInputQuickSettingsDisablerProvider));
            Dependency_MembersInjector.injectMBubbleController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.newBubbleControllerProvider));
            Dependency_MembersInjector.injectMNotificationEntryManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider));
            Dependency_MembersInjector.injectMSensorPrivacyManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideSensorPrivacyManagerProvider));
            Dependency_MembersInjector.injectMAutoHideController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideAutoHideControllerProvider));
            Dependency_MembersInjector.injectMForegroundServiceNotificationListener(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.foregroundServiceNotificationListenerProvider));
            Dependency_MembersInjector.injectMPrivacyItemController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.privacyItemControllerProvider));
            Dependency_MembersInjector.injectMBgLooper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBgLooperProvider));
            Dependency_MembersInjector.injectMBgHandler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBgHandlerProvider));
            Dependency_MembersInjector.injectMMainLooper(dependency, DoubleCheck.lazy(ConcurrencyModule_ProvideMainLooperFactory.create()));
            Dependency_MembersInjector.injectMMainHandler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideMainHandlerProvider));
            Dependency_MembersInjector.injectMTimeTickHandler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideTimeTickHandlerProvider));
            Dependency_MembersInjector.injectMLeakReportEmail(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideLeakReportEmailProvider));
            Dependency_MembersInjector.injectMMainExecutor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideMainExecutorProvider));
            Dependency_MembersInjector.injectMBackgroundExecutor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBackgroundExecutorProvider));
            Dependency_MembersInjector.injectMClockManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.clockManagerProvider));
            Dependency_MembersInjector.injectMActivityManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideActivityManagerWrapperProvider));
            Dependency_MembersInjector.injectMDevicePolicyManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDevicePolicyManagerWrapperProvider));
            Dependency_MembersInjector.injectMPackageManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providePackageManagerWrapperProvider));
            Dependency_MembersInjector.injectMSensorPrivacyController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.sensorPrivacyControllerImplProvider));
            Dependency_MembersInjector.injectMDockManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.dockManagerImplProvider));
            Dependency_MembersInjector.injectMINotificationManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideINotificationManagerProvider));
            Dependency_MembersInjector.injectMSysUiStateFlagsContainer(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideSysUiStateProvider));
            Dependency_MembersInjector.injectMAlarmManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideAlarmManagerProvider));
            Dependency_MembersInjector.injectMKeyguardSecurityModel(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardSecurityModelProvider));
            Dependency_MembersInjector.injectMDozeParameters(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.dozeParametersProvider));
            Dependency_MembersInjector.injectMWallpaperManager(dependency, DoubleCheck.lazy(SystemServicesModule_ProvideIWallPaperManagerFactory.create()));
            Dependency_MembersInjector.injectMCommandQueue(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider));
            Dependency_MembersInjector.injectMRecents(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideRecentsProvider));
            Dependency_MembersInjector.injectMStatusBar(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideStatusBarProvider));
            Dependency_MembersInjector.injectMDisplayController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.displayControllerProvider));
            Dependency_MembersInjector.injectMSystemWindows(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.systemWindowsProvider));
            Dependency_MembersInjector.injectMDisplayImeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.displayImeControllerProvider));
            Dependency_MembersInjector.injectMRecordingController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.recordingControllerProvider));
            Dependency_MembersInjector.injectMProtoTracer(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.protoTracerProvider));
            Dependency_MembersInjector.injectMDivider(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDividerProvider));
            Dependency_MembersInjector.injectMPulseController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providePulseControllerProvider));
            Dependency_MembersInjector.injectMMediaOutputDialogFactory(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.mediaOutputDialogFactoryProvider));
            Dependency_MembersInjector.injectMScreenDecorations(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.screenDecorationsProvider));
            return dependency;
        }
    }

    private final class FragmentCreatorImpl implements FragmentService.FragmentCreator {
        private FragmentCreatorImpl() {
        }

        private CarrierTextController.Builder getBuilder4() {
            return new CarrierTextController.Builder(DaggerTvSystemUIRootComponent.this.context, DaggerTvSystemUIRootComponent.this.getMainResources());
        }

        private QSCarrierGroupController.Builder getBuilder3() {
            return new QSCarrierGroupController.Builder((ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), DaggerTvSystemUIRootComponent.this.getBackgroundHandler(), ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper(), (NetworkController) DaggerTvSystemUIRootComponent.this.networkControllerImplProvider.get(), getBuilder4());
        }

        private QuickStatusBarHeaderController.Builder getBuilder2() {
            return new QuickStatusBarHeaderController.Builder(getBuilder3());
        }

        private QSContainerImplController.Builder getBuilder() {
            return new QSContainerImplController.Builder(getBuilder2());
        }

        @Override // com.android.systemui.fragments.FragmentService.FragmentCreator
        public NavigationBarFragment createNavigationBarFragment() {
            return new NavigationBarFragment((AccessibilityManagerWrapper) DaggerTvSystemUIRootComponent.this.accessibilityManagerWrapperProvider.get(), (DeviceProvisionedController) DaggerTvSystemUIRootComponent.this.deviceProvisionedControllerImplProvider.get(), (MetricsLogger) DaggerTvSystemUIRootComponent.this.provideMetricsLoggerProvider.get(), (AssistManager) DaggerTvSystemUIRootComponent.this.assistManagerProvider.get(), (OverviewProxyService) DaggerTvSystemUIRootComponent.this.overviewProxyServiceProvider.get(), (NavigationModeController) DaggerTvSystemUIRootComponent.this.navigationModeControllerProvider.get(), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (SysUiState) DaggerTvSystemUIRootComponent.this.provideSysUiStateProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), (Divider) DaggerTvSystemUIRootComponent.this.provideDividerProvider.get(), Optional.of((Recents) DaggerTvSystemUIRootComponent.this.provideRecentsProvider.get()), DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideStatusBarProvider), (ShadeController) DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider.get(), (NotificationRemoteInputManager) DaggerTvSystemUIRootComponent.this.provideNotificationRemoteInputManagerProvider.get(), (SystemActions) DaggerTvSystemUIRootComponent.this.systemActionsProvider.get(), DaggerTvSystemUIRootComponent.this.getMainHandler(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
        }

        @Override // com.android.systemui.fragments.FragmentService.FragmentCreator
        public QSFragment createQSFragment() {
            return new QSFragment((RemoteInputQuickSettingsDisabler) DaggerTvSystemUIRootComponent.this.remoteInputQuickSettingsDisablerProvider.get(), (InjectionInflationController) DaggerTvSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (QSTileHost) DaggerTvSystemUIRootComponent.this.qSTileHostProvider.get(), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), getBuilder());
        }
    }

    private final class ViewCreatorImpl implements InjectionInflationController.ViewCreator {
        private ViewCreatorImpl() {
        }

        @Override // com.android.systemui.util.InjectionInflationController.ViewCreator
        public InjectionInflationController.ViewInstanceCreator createInstanceCreator(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
            return new ViewInstanceCreatorImpl(viewAttributeProvider);
        }

        private final class ViewInstanceCreatorImpl implements InjectionInflationController.ViewInstanceCreator {
            private InjectionInflationController.ViewAttributeProvider viewAttributeProvider;

            private ViewInstanceCreatorImpl(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
                initialize(viewAttributeProvider);
            }

            private NotificationSectionsManager getNotificationSectionsManager() {
                return new NotificationSectionsManager((ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (ConfigurationController) DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (PeopleHubViewAdapter) DaggerTvSystemUIRootComponent.this.peopleHubViewAdapterImplProvider.get(), (KeyguardMediaController) DaggerTvSystemUIRootComponent.this.keyguardMediaControllerProvider.get(), DaggerTvSystemUIRootComponent.this.getNotificationSectionsFeatureManager(), (NotificationSectionsLogger) DaggerTvSystemUIRootComponent.this.notificationSectionsLoggerProvider.get());
            }

            private TileQueryHelper getTileQueryHelper() {
                return new TileQueryHelper(DaggerTvSystemUIRootComponent.this.context, DaggerTvSystemUIRootComponent.this.getMainExecutor(), (Executor) DaggerTvSystemUIRootComponent.this.provideBackgroundExecutorProvider.get());
            }

            private void initialize(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
                this.viewAttributeProvider = (InjectionInflationController.ViewAttributeProvider) Preconditions.checkNotNull(viewAttributeProvider);
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QuickStatusBarHeader createQsHeader() {
                return new QuickStatusBarHeader(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (NextAlarmController) DaggerTvSystemUIRootComponent.this.nextAlarmControllerImplProvider.get(), (ZenModeController) DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider.get(), (StatusBarIconController) DaggerTvSystemUIRootComponent.this.statusBarIconControllerImplProvider.get(), (ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (PrivacyItemController) DaggerTvSystemUIRootComponent.this.privacyItemControllerProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), (RingerModeTracker) DaggerTvSystemUIRootComponent.this.ringerModeTrackerImplProvider.get(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSFooterImpl createQsFooter() {
                return new QSFooterImpl(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (UserInfoController) DaggerTvSystemUIRootComponent.this.userInfoControllerImplProvider.get(), (DeviceProvisionedController) DaggerTvSystemUIRootComponent.this.deviceProvisionedControllerImplProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public NotificationStackScrollLayout createNotificationStackScrollLayout() {
                return new NotificationStackScrollLayout(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), ((Boolean) DaggerTvSystemUIRootComponent.this.provideAllowNotificationLongPressProvider.get()).booleanValue(), (NotificationRoundnessManager) DaggerTvSystemUIRootComponent.this.notificationRoundnessManagerProvider.get(), (DynamicPrivacyController) DaggerTvSystemUIRootComponent.this.dynamicPrivacyControllerProvider.get(), (SysuiStatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (HeadsUpManagerPhone) DaggerTvSystemUIRootComponent.this.provideHeadsUpManagerPhoneProvider.get(), (KeyguardBypassController) DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider.get(), (KeyguardMediaController) DaggerTvSystemUIRootComponent.this.keyguardMediaControllerProvider.get(), (FalsingManager) DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider.get(), (NotificationLockscreenUserManager) DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider.get(), (NotificationGutsManager) DaggerTvSystemUIRootComponent.this.provideNotificationGutsManagerProvider.get(), (ZenModeController) DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider.get(), getNotificationSectionsManager(), (ForegroundServiceSectionController) DaggerTvSystemUIRootComponent.this.foregroundServiceSectionControllerProvider.get(), (ForegroundServiceDismissalFeatureController) DaggerTvSystemUIRootComponent.this.foregroundServiceDismissalFeatureControllerProvider.get(), (FeatureFlags) DaggerTvSystemUIRootComponent.this.featureFlagsProvider.get(), (NotifPipeline) DaggerTvSystemUIRootComponent.this.notifPipelineProvider.get(), (NotificationEntryManager) DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider.get(), (NotifCollection) DaggerTvSystemUIRootComponent.this.notifCollectionProvider.get(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public NotificationShelf creatNotificationShelf() {
                return new NotificationShelf(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (KeyguardBypassController) DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardClockSwitch createKeyguardClockSwitch() {
                return new KeyguardClockSwitch(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (SysuiColorExtractor) DaggerTvSystemUIRootComponent.this.sysuiColorExtractorProvider.get(), (ClockManager) DaggerTvSystemUIRootComponent.this.clockManagerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardSliceView createKeyguardSliceView() {
                return new KeyguardSliceView(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (ConfigurationController) DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (TunerService) DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider.get(), DaggerTvSystemUIRootComponent.this.getMainResources());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardMessageArea createKeyguardMessageArea() {
                return new KeyguardMessageArea(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ConfigurationController) DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSPanel createQSPanel() {
                return new QSPanel(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (DumpManager) DaggerTvSystemUIRootComponent.this.dumpManagerProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), DaggerTvSystemUIRootComponent.this.getQSLogger(), DaggerTvSystemUIRootComponent.this.getMediaHost(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QuickQSPanel createQuickQSPanel() {
                return new QuickQSPanel(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (DumpManager) DaggerTvSystemUIRootComponent.this.dumpManagerProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), DaggerTvSystemUIRootComponent.this.getQSLogger(), DaggerTvSystemUIRootComponent.this.getMediaHost(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSCustomizer createQSCustomizer() {
                return new QSCustomizer(DaggerTvSystemUIRootComponent.this.context, InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (LightBarController) DaggerTvSystemUIRootComponent.this.lightBarControllerProvider.get(), (KeyguardStateController) DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider.get(), (ScreenLifecycle) DaggerTvSystemUIRootComponent.this.screenLifecycleProvider.get(), getTileQueryHelper(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }
        }
    }

    private final class ExpandableNotificationRowComponentBuilder implements ExpandableNotificationRowComponent.Builder {
        private ExpandableNotificationRow expandableNotificationRow;
        private NotificationEntry notificationEntry;
        private Runnable onDismissRunnable;
        private ExpandableNotificationRow.OnExpandClickListener onExpandClickListener;
        private RowContentBindStage rowContentBindStage;

        private ExpandableNotificationRowComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponent build() {
            if (this.expandableNotificationRow == null) {
                throw new IllegalStateException(ExpandableNotificationRow.class.getCanonicalName() + " must be set");
            }
            if (this.notificationEntry == null) {
                throw new IllegalStateException(NotificationEntry.class.getCanonicalName() + " must be set");
            }
            if (this.onDismissRunnable == null) {
                throw new IllegalStateException(Runnable.class.getCanonicalName() + " must be set");
            }
            if (this.rowContentBindStage == null) {
                throw new IllegalStateException(RowContentBindStage.class.getCanonicalName() + " must be set");
            }
            if (this.onExpandClickListener == null) {
                throw new IllegalStateException(ExpandableNotificationRow.OnExpandClickListener.class.getCanonicalName() + " must be set");
            }
            return new ExpandableNotificationRowComponentImpl(this);
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder expandableNotificationRow(ExpandableNotificationRow expandableNotificationRow) {
            this.expandableNotificationRow = (ExpandableNotificationRow) Preconditions.checkNotNull(expandableNotificationRow);
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder notificationEntry(NotificationEntry notificationEntry) {
            this.notificationEntry = (NotificationEntry) Preconditions.checkNotNull(notificationEntry);
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder onDismissRunnable(Runnable runnable) {
            this.onDismissRunnable = (Runnable) Preconditions.checkNotNull(runnable);
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder rowContentBindStage(RowContentBindStage rowContentBindStage) {
            this.rowContentBindStage = (RowContentBindStage) Preconditions.checkNotNull(rowContentBindStage);
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder onExpandClickListener(ExpandableNotificationRow.OnExpandClickListener onExpandClickListener) {
            this.onExpandClickListener = (ExpandableNotificationRow.OnExpandClickListener) Preconditions.checkNotNull(onExpandClickListener);
            return this;
        }
    }

    private final class ExpandableNotificationRowComponentImpl implements ExpandableNotificationRowComponent {
        private ActivatableNotificationViewController_Factory activatableNotificationViewControllerProvider;
        private Provider<ExpandableNotificationRowController> expandableNotificationRowControllerProvider;
        private Provider<ExpandableNotificationRow> expandableNotificationRowProvider;
        private ExpandableOutlineViewController_Factory expandableOutlineViewControllerProvider;
        private ExpandableViewController_Factory expandableViewControllerProvider;
        private Provider<NotificationEntry> notificationEntryProvider;
        private Provider<Runnable> onDismissRunnableProvider;
        private Provider<ExpandableNotificationRow.OnExpandClickListener> onExpandClickListenerProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory provideAppNameProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory provideNotificationKeyProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory provideStatusBarNotificationProvider;
        private Provider<RowContentBindStage> rowContentBindStageProvider;

        private ExpandableNotificationRowComponentImpl(ExpandableNotificationRowComponentBuilder expandableNotificationRowComponentBuilder) {
            initialize(expandableNotificationRowComponentBuilder);
        }

        private void initialize(ExpandableNotificationRowComponentBuilder expandableNotificationRowComponentBuilder) {
            Factory factoryCreate = InstanceFactory.create(expandableNotificationRowComponentBuilder.expandableNotificationRow);
            this.expandableNotificationRowProvider = factoryCreate;
            ExpandableViewController_Factory expandableViewController_FactoryCreate = ExpandableViewController_Factory.create(factoryCreate);
            this.expandableViewControllerProvider = expandableViewController_FactoryCreate;
            ExpandableOutlineViewController_Factory expandableOutlineViewController_FactoryCreate = ExpandableOutlineViewController_Factory.create(this.expandableNotificationRowProvider, expandableViewController_FactoryCreate);
            this.expandableOutlineViewControllerProvider = expandableOutlineViewController_FactoryCreate;
            this.activatableNotificationViewControllerProvider = ActivatableNotificationViewController_Factory.create(this.expandableNotificationRowProvider, expandableOutlineViewController_FactoryCreate, DaggerTvSystemUIRootComponent.this.provideAccessibilityManagerProvider, DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider);
            Factory factoryCreate2 = InstanceFactory.create(expandableNotificationRowComponentBuilder.notificationEntry);
            this.notificationEntryProvider = factoryCreate2;
            this.provideStatusBarNotificationProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory.create(factoryCreate2);
            this.provideAppNameProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory.create(DaggerTvSystemUIRootComponent.this.contextProvider, this.provideStatusBarNotificationProvider);
            this.provideNotificationKeyProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory.create(this.provideStatusBarNotificationProvider);
            this.rowContentBindStageProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.rowContentBindStage);
            this.onExpandClickListenerProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.onExpandClickListener);
            this.onDismissRunnableProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.onDismissRunnable);
            this.expandableNotificationRowControllerProvider = DoubleCheck.provider(ExpandableNotificationRowController_Factory.create(this.expandableNotificationRowProvider, this.activatableNotificationViewControllerProvider, DaggerTvSystemUIRootComponent.this.provideNotificationMediaManagerProvider, DaggerTvSystemUIRootComponent.this.providePluginManagerProvider, DaggerTvSystemUIRootComponent.this.bindSystemClockProvider, this.provideAppNameProvider, this.provideNotificationKeyProvider, DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider, DaggerTvSystemUIRootComponent.this.notificationGroupManagerProvider, this.rowContentBindStageProvider, DaggerTvSystemUIRootComponent.this.provideNotificationLoggerProvider, DaggerTvSystemUIRootComponent.this.provideHeadsUpManagerPhoneProvider, this.onExpandClickListenerProvider, DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider, DaggerTvSystemUIRootComponent.this.provideNotificationGutsManagerProvider, DaggerTvSystemUIRootComponent.this.provideAllowNotificationLongPressProvider, this.onDismissRunnableProvider, DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider, DaggerTvSystemUIRootComponent.this.peopleNotificationIdentifierImplProvider, DaggerTvSystemUIRootComponent.this.provideIStatusBarServiceProvider));
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent
        public ExpandableNotificationRowController getExpandableNotificationRowController() {
            return this.expandableNotificationRowControllerProvider.get();
        }
    }

    private final class StatusBarComponentBuilder implements StatusBarComponent.Builder {
        private NotificationShadeWindowView statusBarWindowView;

        private StatusBarComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent.Builder
        public StatusBarComponent build() {
            if (this.statusBarWindowView == null) {
                throw new IllegalStateException(NotificationShadeWindowView.class.getCanonicalName() + " must be set");
            }
            return new StatusBarComponentImpl(this);
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent.Builder
        public StatusBarComponentBuilder statusBarWindowView(NotificationShadeWindowView notificationShadeWindowView) {
            this.statusBarWindowView = (NotificationShadeWindowView) Preconditions.checkNotNull(notificationShadeWindowView);
            return this;
        }
    }

    private final class StatusBarComponentImpl implements StatusBarComponent {
        private FlingAnimationUtils_Builder_Factory builderProvider;
        private Provider<NotificationPanelView> getNotificationPanelViewProvider;
        private Provider<NotificationPanelViewController> notificationPanelViewControllerProvider;
        private NotificationShadeWindowView statusBarWindowView;
        private Provider<NotificationShadeWindowView> statusBarWindowViewProvider;

        private StatusBarComponentImpl(StatusBarComponentBuilder statusBarComponentBuilder) {
            initialize(statusBarComponentBuilder);
        }

        private void initialize(StatusBarComponentBuilder statusBarComponentBuilder) {
            this.statusBarWindowView = statusBarComponentBuilder.statusBarWindowView;
            Factory factoryCreate = InstanceFactory.create(statusBarComponentBuilder.statusBarWindowView);
            this.statusBarWindowViewProvider = factoryCreate;
            this.getNotificationPanelViewProvider = DoubleCheck.provider(StatusBarViewModule_GetNotificationPanelViewFactory.create(factoryCreate));
            this.builderProvider = FlingAnimationUtils_Builder_Factory.create(DaggerTvSystemUIRootComponent.this.provideDisplayMetricsProvider);
            this.notificationPanelViewControllerProvider = DoubleCheck.provider(NotificationPanelViewController_Factory.create(this.getNotificationPanelViewProvider, DaggerTvSystemUIRootComponent.this.injectionInflationControllerProvider, DaggerTvSystemUIRootComponent.this.notificationWakeUpCoordinatorProvider, DaggerTvSystemUIRootComponent.this.pulseExpansionHandlerProvider, DaggerTvSystemUIRootComponent.this.dynamicPrivacyControllerProvider, DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider, DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider, DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider, DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider, DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider, DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider, DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider, DaggerTvSystemUIRootComponent.this.dozeLogProvider, DaggerTvSystemUIRootComponent.this.dozeParametersProvider, DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider, DaggerTvSystemUIRootComponent.this.vibratorHelperProvider, DaggerTvSystemUIRootComponent.this.provideLatencyTrackerProvider, DaggerTvSystemUIRootComponent.this.providePowerManagerProvider, DaggerTvSystemUIRootComponent.this.provideAccessibilityManagerProvider, DaggerTvSystemUIRootComponent.this.provideDisplayIdProvider, DaggerTvSystemUIRootComponent.this.keyguardUpdateMonitorProvider, DaggerTvSystemUIRootComponent.this.provideMetricsLoggerProvider, DaggerTvSystemUIRootComponent.this.provideActivityManagerProvider, DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider, DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider, this.builderProvider, DaggerTvSystemUIRootComponent.this.statusBarTouchableRegionManagerProvider, DaggerTvSystemUIRootComponent.this.conversationNotificationManagerProvider, DaggerTvSystemUIRootComponent.this.mediaHierarchyManagerProvider, DaggerTvSystemUIRootComponent.this.biometricUnlockControllerProvider, DaggerTvSystemUIRootComponent.this.statusBarKeyguardViewManagerProvider, DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider));
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public NotificationShadeWindowViewController getNotificationShadeWindowViewController() {
            return new NotificationShadeWindowViewController((InjectionInflationController) DaggerTvSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (NotificationWakeUpCoordinator) DaggerTvSystemUIRootComponent.this.notificationWakeUpCoordinatorProvider.get(), (PulseExpansionHandler) DaggerTvSystemUIRootComponent.this.pulseExpansionHandlerProvider.get(), (DynamicPrivacyController) DaggerTvSystemUIRootComponent.this.dynamicPrivacyControllerProvider.get(), (KeyguardBypassController) DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider.get(), (FalsingManager) DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider.get(), (PluginManager) DaggerTvSystemUIRootComponent.this.providePluginManagerProvider.get(), (TunerService) DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider.get(), (NotificationLockscreenUserManager) DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider.get(), (NotificationEntryManager) DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider.get(), (KeyguardStateController) DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider.get(), (SysuiStatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (DozeLog) DaggerTvSystemUIRootComponent.this.dozeLogProvider.get(), (DozeParameters) DaggerTvSystemUIRootComponent.this.dozeParametersProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), (ShadeController) DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider.get(), (DockManager) DaggerTvSystemUIRootComponent.this.dockManagerImplProvider.get(), (NotificationShadeDepthController) DaggerTvSystemUIRootComponent.this.notificationShadeDepthControllerProvider.get(), this.statusBarWindowView, this.notificationPanelViewControllerProvider.get(), (SuperStatusBarViewFactory) DaggerTvSystemUIRootComponent.this.superStatusBarViewFactoryProvider.get());
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public StatusBarWindowController getStatusBarWindowController() {
            return (StatusBarWindowController) DaggerTvSystemUIRootComponent.this.statusBarWindowControllerProvider.get();
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public NotificationPanelViewController getNotificationPanelViewController() {
            return this.notificationPanelViewControllerProvider.get();
        }
    }

    private final class NotificationRowComponentBuilder implements NotificationRowComponent.Builder {
        private ActivatableNotificationView activatableNotificationView;

        private NotificationRowComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent.Builder
        public NotificationRowComponent build() {
            if (this.activatableNotificationView == null) {
                throw new IllegalStateException(ActivatableNotificationView.class.getCanonicalName() + " must be set");
            }
            return new NotificationRowComponentImpl(this);
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent.Builder
        public NotificationRowComponentBuilder activatableNotificationView(ActivatableNotificationView activatableNotificationView) {
            this.activatableNotificationView = (ActivatableNotificationView) Preconditions.checkNotNull(activatableNotificationView);
            return this;
        }
    }

    private final class NotificationRowComponentImpl implements NotificationRowComponent {
        private ActivatableNotificationView activatableNotificationView;

        private NotificationRowComponentImpl(NotificationRowComponentBuilder notificationRowComponentBuilder) {
            initialize(notificationRowComponentBuilder);
        }

        private ExpandableViewController getExpandableViewController() {
            return new ExpandableViewController(this.activatableNotificationView);
        }

        private ExpandableOutlineViewController getExpandableOutlineViewController() {
            return new ExpandableOutlineViewController(this.activatableNotificationView, getExpandableViewController());
        }

        private void initialize(NotificationRowComponentBuilder notificationRowComponentBuilder) {
            this.activatableNotificationView = notificationRowComponentBuilder.activatableNotificationView;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent
        public ActivatableNotificationViewController getActivatableNotificationViewController() {
            return new ActivatableNotificationViewController(this.activatableNotificationView, getExpandableOutlineViewController(), (AccessibilityManager) DaggerTvSystemUIRootComponent.this.provideAccessibilityManagerProvider.get(), (FalsingManager) DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider.get());
        }
    }

    private final class TvPipComponentBuilder implements TvPipComponent.Builder {
        private PipControlsView pipControlsView;

        private TvPipComponentBuilder() {
        }

        @Override // com.android.systemui.pip.tv.dagger.TvPipComponent.Builder
        public TvPipComponent build() {
            if (this.pipControlsView == null) {
                throw new IllegalStateException(PipControlsView.class.getCanonicalName() + " must be set");
            }
            return new TvPipComponentImpl(this);
        }

        @Override // com.android.systemui.pip.tv.dagger.TvPipComponent.Builder
        public TvPipComponentBuilder pipControlsView(PipControlsView pipControlsView) {
            this.pipControlsView = (PipControlsView) Preconditions.checkNotNull(pipControlsView);
            return this;
        }
    }

    private final class TvPipComponentImpl implements TvPipComponent {
        private PipControlsView pipControlsView;

        private TvPipComponentImpl(TvPipComponentBuilder tvPipComponentBuilder) {
            initialize(tvPipComponentBuilder);
        }

        private void initialize(TvPipComponentBuilder tvPipComponentBuilder) {
            this.pipControlsView = tvPipComponentBuilder.pipControlsView;
        }

        @Override // com.android.systemui.pip.tv.dagger.TvPipComponent
        public PipControlsViewController getPipControlsViewController() {
            return new PipControlsViewController(this.pipControlsView, (PipManager) DaggerTvSystemUIRootComponent.this.pipManagerProvider.get(), (LayoutInflater) DaggerTvSystemUIRootComponent.this.providerLayoutInflaterProvider.get(), DaggerTvSystemUIRootComponent.this.getMainHandler());
        }
    }
}
