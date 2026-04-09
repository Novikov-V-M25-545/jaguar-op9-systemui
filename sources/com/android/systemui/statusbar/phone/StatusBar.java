package com.android.systemui.statusbar.phone;

import android.R;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.Fragment;
import android.app.IApplicationThread;
import android.app.IWallpaperManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProfilerInfo;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.om.OverlayManager;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.IFingerprintService;
import android.media.AudioAttributes;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.Display;
import android.view.IWindowManager;
import android.view.InsetsState;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.RemoteAnimationAdapter;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.widget.DateTimeView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.RegisterStatusBarResult;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.ThemeAccentUtils;
import com.android.internal.util.crdroid.ThemesUtils;
import com.android.internal.util.crdroid.Utils;
import com.android.internal.view.AppearanceRegion;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.settingslib.display.BrightnessUtils;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.DejankUtils;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.EventLogTags;
import com.android.systemui.InitController;
import com.android.systemui.JaguarIdleManager;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.biometrics.FODCircleViewImpl;
import com.android.systemui.biometrics.FODCircleViewImplCallback;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.charging.WirelessChargingAnimation;
import com.android.systemui.classifier.FalsingLog;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.fragments.ExtensionFragmentListener;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.OverlayPlugin;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.pulse.VisualizerView;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.AutoHideUiElement;
import com.android.systemui.statusbar.BackDropView;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyboardShortcuts;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.phone.dagger.StatusBarComponent;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.BurnInProtectionController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.PulseController;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.RotationUtils;
import com.android.systemui.volume.VolumeComponent;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Provider;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class StatusBar extends SystemUI implements DemoMode, ActivityStarter, KeyguardStateController.Callback, OnHeadsUpChangedListener, CommandQueue.Callbacks, ColorExtractor.OnColorsChangedListener, ConfigurationController.ConfigurationListener, StatusBarStateController.StateListener, ActivityLaunchAnimator.Callback, LifecycleOwner, BatteryController.BatteryStateChangeCallback, TunerService.Tunable {
    public static final boolean ONLY_CORE_APPS;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static final UiEventLogger sUiEventLogger = new UiEventLoggerImpl();
    private boolean isIdleManagerIstantiated;
    private long lastGcTime;
    private final int[] mAbsPos;
    protected AccessibilityManager mAccessibilityManager;
    private ActivityIntentHelper mActivityIntentHelper;
    private ActivityLaunchAnimator mActivityLaunchAnimator;
    private ActivityManager mActivityManager;
    private View mAmbientIndicationContainer;
    private boolean mAppFullscreen;
    private boolean mAppImmersive;
    private int mAppearance;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final AutoHideController mAutoHideController;
    private boolean mAutomaticBrightness;
    private final BroadcastReceiver mBannerActionBroadcastReceiver;
    protected IStatusBarService mBarService;
    protected final BatteryController mBatteryController;
    private BiometricUnlockController mBiometricUnlockController;
    private final Lazy<BiometricUnlockController> mBiometricUnlockControllerLazy;
    protected boolean mBouncerShowing;
    private boolean mBouncerWasShowingWhenHidden;
    private boolean mBrightnessChanged;
    private boolean mBrightnessControl;
    private BrightnessMirrorController mBrightnessMirrorController;
    private boolean mBrightnessMirrorVisible;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final BroadcastReceiver mBroadcastReceiver;
    private final BubbleController mBubbleController;
    private final BubbleController.BubbleExpandListener mBubbleExpandListener;
    private BurnInProtectionController mBurnInProtectionController;
    private final BypassHeadsUpNotifier mBypassHeadsUpNotifier;
    private long[] mCameraLaunchGestureVibePattern;
    private final Runnable mCheckBarModes;
    private boolean mClearableNotifications;
    private final SysuiColorExtractor mColorExtractor;
    protected final CommandQueue mCommandQueue;
    private final ConfigurationController mConfigurationController;
    private final Point mCurrentDisplaySize;
    private CustomSettingsObserver mCustomSettingsObserver;
    private final DarkIconDispatcher mDarkIconDispatcher;
    private boolean mDemoMode;
    private boolean mDemoModeAllowed;
    private final BroadcastReceiver mDemoReceiver;
    protected boolean mDeviceInteractive;
    protected DevicePolicyManager mDevicePolicyManager;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private int mDisabled1;
    private int mDisabled2;
    private ImageButton mDismissAllButton;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    protected Display mDisplay;
    private int mDisplayId;
    private DisplayManager mDisplayManager;
    private final DisplayMetrics mDisplayMetrics;
    private final Optional<Divider> mDividerOptional;
    private final DozeParameters mDozeParameters;
    protected DozeScrimController mDozeScrimController;

    @VisibleForTesting
    DozeServiceHost mDozeServiceHost;
    protected boolean mDozing;
    private NotificationEntry mDraggedDownEntry;
    private IDreamManager mDreamManager;
    private final DynamicPrivacyController mDynamicPrivacyController;
    private boolean mExpandedVisible;
    private final ExtensionController mExtensionController;
    private FODCircleViewImpl mFODCircleViewImpl;
    private FODCircleViewImplCallback mFODCircleViewImplCallback;
    private final FalsingManager mFalsingManager;
    private final IFingerprintService mFingerprintService;
    private FlashlightController mFlashlightController;
    private boolean mFodVisibility;
    private boolean mGamingModeActivated;
    private final GestureRecorder mGestureRec;
    protected PowerManager.WakeLock mGestureWakeLock;
    private final View.OnClickListener mGoToLockedShadeListener;
    private final NotificationGroupManager mGroupManager;
    private final NotificationGutsManager mGutsManager;
    protected final H mHandler;
    private HeadsUpAppearanceController mHeadsUpAppearanceController;
    private boolean mHeadsUpDisabled;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mHideIconsForBouncer;
    private IOverlayManager mIOverlayManager;
    private final StatusBarIconController mIconController;
    private PhoneStatusBarPolicy mIconPolicy;
    private int mImmerseMode;
    private final InitController mInitController;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mInteractingWindows;
    private boolean mIsDreaming;
    protected boolean mIsKeyguard;
    private boolean mIsOccluded;
    private boolean mJustPeeked;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardDismissUtil mKeyguardDismissUtil;
    KeyguardIndicationController mKeyguardIndicationController;
    private final KeyguardLiftController mKeyguardLiftController;
    protected KeyguardManager mKeyguardManager;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    private boolean mKillNotch;
    private int mLastCameraLaunchSource;
    private int mLastLoggedStateFingerprint;
    private boolean mLaunchCameraOnFinishedGoingToSleep;
    private boolean mLaunchCameraWhenFinishedWaking;
    private Runnable mLaunchTransitionEndRunnable;
    private final LifecycleRegistry mLifecycle;
    private final LightBarController mLightBarController;
    private final LightsOutNotifController mLightsOutNotifController;
    private int mLinger;
    private final LockscreenLockIconController mLockscreenLockIconController;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    protected LockscreenWallpaper mLockscreenWallpaper;
    private final Lazy<LockscreenWallpaper> mLockscreenWallpaperLazy;
    private final Runnable mLongPressBrightnessChange;
    private final Handler mMainThreadHandler;
    private float mMaximumBacklight;
    private final NotificationMediaManager mMediaManager;
    private final MetricsLogger mMetricsLogger;
    private float mMinimumBacklight;
    private int mNavbarStyle;
    private final NavigationBarController mNavigationBarController;
    private final NetworkController mNetworkController;
    private boolean mNoAnimationOnNextBarModeChange;
    private NotificationActivityStarter mNotificationActivityStarter;
    protected NotificationIconAreaController mNotificationIconAreaController;
    protected final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationLogger mNotificationLogger;
    protected NotificationPanelViewController mNotificationPanelViewController;
    private Lazy<NotificationShadeDepthController> mNotificationShadeDepthControllerLazy;
    protected NotificationShadeWindowController mNotificationShadeWindowController;
    protected NotificationShadeWindowView mNotificationShadeWindowView;
    protected NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    protected NotificationShelf mNotificationShelf;
    private NotificationsController mNotificationsController;
    private OverlayManager mOverlayManager;
    protected boolean mPanelExpanded;
    private View mPendingRemoteInputView;
    protected StatusBarWindowView mPhoneStatusBarWindow;
    private final PluginDependencyProvider mPluginDependencyProvider;
    private final PluginManager mPluginManager;
    private boolean mPortrait;
    private final PowerManager mPowerManager;
    protected StatusBarNotificationPresenter mPresenter;
    private final PulseExpansionHandler mPulseExpansionHandler;
    private QSPanel mQSPanel;
    private final Object mQueueLock;
    private int mQuickQsTotalHeight;
    private final Optional<Recents> mRecentsOptional;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler;
    private View mReportRejectedTouch;
    private int mRoundedStyle;
    private final ScreenLifecycle mScreenLifecycle;
    final ScreenLifecycle.Observer mScreenObserver;
    private final ScreenPinningRequest mScreenPinningRequest;
    private final ScrimController mScrimController;
    private final ShadeController mShadeController;
    private boolean mShowDimissButton;
    private StatusBarSignalPolicy mSignalPolicy;
    private int mSignalStyle;
    protected ViewGroup mStackScroller;
    final Runnable mStartTracing;
    protected int mState;
    private final Provider<StatusBarComponent.Builder> mStatusBarComponentBuilder;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private int mStatusBarMode;
    private final StatusBarNotificationActivityStarter.Builder mStatusBarNotificationActivityStarterBuilder;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private LogMaker mStatusBarStateLog;
    private final StatusBarTouchableRegionManager mStatusBarTouchableRegionManager;
    protected PhoneStatusBarView mStatusBarView;
    protected StatusBarWindowController mStatusBarWindowController;
    private boolean mStatusBarWindowHidden;
    private int mStatusBarWindowState;
    private boolean mStockStatusBar;
    final Runnable mStopTracing;
    private final SuperStatusBarViewFactory mSuperStatusBarViewFactory;
    private final Runnable mSystemUiGcOpt;
    private boolean mSysuiRoundedFwvals;
    private final int[] mTmpInt2;
    private boolean mTopHidesStatusBar;
    private String mTopPkgClass;
    private boolean mTransientShown;
    private final TunerService mTunerService;
    private final Executor mUiBgExecutor;
    private UiModeManager mUiModeManager;
    private final UiOffloadThread mUiOffloadThread;
    private final ScrimController.Callback mUnlockScrimCallback;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    private final UserInfoControllerImpl mUserInfoControllerImpl;

    @VisibleForTesting
    protected boolean mUserSetup;
    private final DeviceProvisionedController.DeviceProvisionedListener mUserSetupObserver;
    private final UserSwitcherController mUserSwitcherController;
    private boolean mVibrateOnOpening;
    private Vibrator mVibrator;
    private final VibratorHelper mVibratorHelper;
    private final NotificationViewHierarchyManager mViewHierarchyManager;
    protected boolean mVisible;
    private boolean mVisibleToUser;
    private final VisualStabilityManager mVisualStabilityManager;
    private VisualizerView mVisualizerView;
    private final VolumeComponent mVolumeComponent;
    private boolean mWakeUpComingFromTouch;
    private final NotificationWakeUpCoordinator mWakeUpCoordinator;
    private PointF mWakeUpTouchLocation;
    private final WakefulnessLifecycle mWakefulnessLifecycle;

    @VisibleForTesting
    final WakefulnessLifecycle.Observer mWakefulnessObserver;
    private final BroadcastReceiver mWallpaperChangedReceiver;
    private boolean mWallpaperSupported;
    private boolean mWereIconsJustHidden;
    private int mWifiStyle;
    protected WindowManager mWindowManager;
    protected IWindowManager mWindowManagerService;
    private boolean mlessBoringHeadsUp;

    private static int barMode(boolean z, int i) {
        if (z) {
            return 1;
        }
        if ((i & 5) == 5) {
            return 3;
        }
        if ((i & 4) != 0) {
            return 6;
        }
        return (i & 1) != 0 ? 4 : 0;
    }

    private static int getLoggingFingerprint(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return (i & 255) | ((z ? 1 : 0) << 8) | ((z2 ? 1 : 0) << 9) | ((z3 ? 1 : 0) << 10) | ((z4 ? 1 : 0) << 11) | ((z5 ? 1 : 0) << 12);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
    }

    static {
        boolean zIsOnlyCoreApps;
        try {
            zIsOnlyCoreApps = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
        } catch (RemoteException unused) {
            zIsOnlyCoreApps = false;
        }
        ONLY_CORE_APPS = zIsOnlyCoreApps;
    }

    @VisibleForTesting
    public enum StatusBarUiEvent implements UiEventLogger.UiEventEnum {
        LOCKSCREEN_OPEN_SECURE(405),
        LOCKSCREEN_OPEN_INSECURE(406),
        LOCKSCREEN_CLOSE_SECURE(407),
        LOCKSCREEN_CLOSE_INSECURE(408),
        BOUNCER_OPEN_SECURE(409),
        BOUNCER_OPEN_INSECURE(410),
        BOUNCER_CLOSE_SECURE(411),
        BOUNCER_CLOSE_INSECURE(412);

        private final int mId;

        StatusBarUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        if (this.mState == 1) {
            wakeUpIfDozing(SystemClock.uptimeMillis(), view, "SHADE_CLICK");
            goToLockedShade(null);
        }
    }

    public StatusBar(Context context, NotificationsController notificationsController, LightBarController lightBarController, AutoHideController autoHideController, KeyguardUpdateMonitor keyguardUpdateMonitor, StatusBarIconController statusBarIconController, PulseExpansionHandler pulseExpansionHandler, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, KeyguardStateController keyguardStateController, HeadsUpManagerPhone headsUpManagerPhone, DynamicPrivacyController dynamicPrivacyController, BypassHeadsUpNotifier bypassHeadsUpNotifier, FalsingManager falsingManager, BroadcastDispatcher broadcastDispatcher, RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler, NotificationGutsManager notificationGutsManager, NotificationLogger notificationLogger, NotificationInterruptStateProvider notificationInterruptStateProvider, NotificationViewHierarchyManager notificationViewHierarchyManager, KeyguardViewMediator keyguardViewMediator, DisplayMetrics displayMetrics, MetricsLogger metricsLogger, Executor executor, NotificationMediaManager notificationMediaManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationRemoteInputManager notificationRemoteInputManager, UserSwitcherController userSwitcherController, NetworkController networkController, BatteryController batteryController, SysuiColorExtractor sysuiColorExtractor, ScreenLifecycle screenLifecycle, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, VibratorHelper vibratorHelper, BubbleController bubbleController, NotificationGroupManager notificationGroupManager, VisualStabilityManager visualStabilityManager, DeviceProvisionedController deviceProvisionedController, NavigationBarController navigationBarController, Lazy<AssistManager> lazy, ConfigurationController configurationController, NotificationShadeWindowController notificationShadeWindowController, LockscreenLockIconController lockscreenLockIconController, DozeParameters dozeParameters, ScrimController scrimController, KeyguardLiftController keyguardLiftController, Lazy<LockscreenWallpaper> lazy2, Lazy<BiometricUnlockController> lazy3, DozeServiceHost dozeServiceHost, PowerManager powerManager, ScreenPinningRequest screenPinningRequest, DozeScrimController dozeScrimController, VolumeComponent volumeComponent, CommandQueue commandQueue, Optional<Recents> optional, Provider<StatusBarComponent.Builder> provider, PluginManager pluginManager, Optional<Divider> optional2, LightsOutNotifController lightsOutNotifController, StatusBarNotificationActivityStarter.Builder builder, ShadeController shadeController, SuperStatusBarViewFactory superStatusBarViewFactory, StatusBarKeyguardViewManager statusBarKeyguardViewManager, ViewMediatorCallback viewMediatorCallback, InitController initController, DarkIconDispatcher darkIconDispatcher, Handler handler, PluginDependencyProvider pluginDependencyProvider, KeyguardDismissUtil keyguardDismissUtil, ExtensionController extensionController, UserInfoControllerImpl userInfoControllerImpl, PhoneStatusBarPolicy phoneStatusBarPolicy, KeyguardIndicationController keyguardIndicationController, DismissCallbackRegistry dismissCallbackRegistry, Lazy<NotificationShadeDepthController> lazy4, StatusBarTouchableRegionManager statusBarTouchableRegionManager, TunerService tunerService, FODCircleViewImpl fODCircleViewImpl) {
        super(context);
        this.mCurrentDisplaySize = new Point();
        this.mStatusBarWindowState = 0;
        this.mQueueLock = new Object();
        this.isIdleManagerIstantiated = false;
        this.mAbsPos = new int[2];
        this.mDisabled1 = 0;
        this.mDisabled2 = 0;
        this.lastGcTime = 0L;
        this.mClearableNotifications = true;
        this.mLongPressBrightnessChange = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar.1
            @Override // java.lang.Runnable
            public void run() {
                StatusBar.this.mStatusBarView.performHapticFeedback(0);
                StatusBar statusBar = StatusBar.this;
                statusBar.adjustBrightness(statusBar.mInitialTouchX);
                StatusBar.this.mLinger = 21;
            }
        };
        this.mUserSetup = false;
        this.mUserSetupObserver = new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.statusbar.phone.StatusBar.2
            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSetupChanged() {
                boolean zIsUserSetup = StatusBar.this.mDeviceProvisionedController.isUserSetup(StatusBar.this.mDeviceProvisionedController.getCurrentUser());
                Log.d("StatusBar", "mUserSetupObserver - DeviceProvisionedListener called for user " + StatusBar.this.mDeviceProvisionedController.getCurrentUser());
                NotificationPanelViewController notificationPanelViewController = StatusBar.this.mNotificationPanelViewController;
                if (notificationPanelViewController != null) {
                    notificationPanelViewController.setUserSetupComplete(zIsUserSetup);
                }
                StatusBar statusBar = StatusBar.this;
                if (zIsUserSetup != statusBar.mUserSetup) {
                    statusBar.mUserSetup = zIsUserSetup;
                    if (!zIsUserSetup && statusBar.mStatusBarView != null) {
                        statusBar.animateCollapseQuickSettings();
                    }
                    StatusBar statusBar2 = StatusBar.this;
                    NotificationPanelViewController notificationPanelViewController2 = statusBar2.mNotificationPanelViewController;
                    if (notificationPanelViewController2 != null) {
                        notificationPanelViewController2.setUserSetupComplete(statusBar2.mUserSetup);
                    }
                    StatusBar.this.updateQsExpansionEnabled();
                }
            }
        };
        H hCreateHandler = createHandler();
        this.mHandler = hCreateHandler;
        this.mWallpaperChangedReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.StatusBar.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if (!StatusBar.this.mWallpaperSupported) {
                    Log.wtf("StatusBar", "WallpaperManager not supported");
                    return;
                }
                WallpaperInfo wallpaperInfo = ((WallpaperManager) context2.getSystemService(WallpaperManager.class)).getWallpaperInfo(-2);
                boolean z = ((SystemUI) StatusBar.this).mContext.getResources().getBoolean(R.bool.config_camera_autorotate) && wallpaperInfo != null && wallpaperInfo.supportsAmbientMode();
                StatusBar.this.mNotificationShadeWindowController.setWallpaperSupportsAmbientMode(z);
                StatusBar.this.mScrimController.setWallpaperSupportsAmbientMode(z);
            }
        };
        this.mTmpInt2 = new int[2];
        this.mUnlockScrimCallback = new ScrimController.Callback() { // from class: com.android.systemui.statusbar.phone.StatusBar.4
            @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
            public void onFinished() {
                StatusBar statusBar = StatusBar.this;
                if (statusBar.mStatusBarKeyguardViewManager != null) {
                    if (statusBar.mKeyguardStateController.isKeyguardFadingAway()) {
                        StatusBar.this.mStatusBarKeyguardViewManager.onKeyguardFadedAway();
                        return;
                    }
                    return;
                }
                Log.w("StatusBar", "Tried to notify keyguard visibility when mStatusBarKeyguardViewManager was null");
            }

            @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
            public void onCancelled() {
                onFinished();
            }
        };
        this.mFODCircleViewImplCallback = new FODCircleViewImplCallback() { // from class: com.android.systemui.statusbar.phone.StatusBar.5
            @Override // com.android.systemui.biometrics.FODCircleViewImplCallback
            public void onFODStatusChange(boolean z) {
                boolean zIsClientActive;
                try {
                    zIsClientActive = StatusBar.this.mFingerprintService.isClientActive();
                } catch (Exception unused) {
                    zIsClientActive = false;
                }
                StatusBar.this.mFodVisibility = z;
                if (!zIsClientActive) {
                    StatusBar.this.mTopPkgClass = null;
                    return;
                }
                if (z) {
                    StatusBar statusBar = StatusBar.this;
                    if (statusBar.mIsKeyguard || statusBar.mIsDreaming) {
                        return;
                    }
                    StatusBar statusBar2 = StatusBar.this;
                    statusBar2.mTopPkgClass = statusBar2.getForegroundPackageNameAndClass();
                }
            }
        };
        this.mLifecycle = new LifecycleRegistry(this);
        this.mGoToLockedShadeListener = new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        };
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.StatusBar.6
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onDreamingStateChanged(boolean z) {
                StatusBar.this.mIsDreaming = z;
                if (z) {
                    StatusBar.this.maybeEscalateHeadsUp();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStrongAuthStateChanged(int i) {
                super.onStrongAuthStateChanged(i);
                StatusBar.this.mNotificationsController.requestNotificationUpdate("onStrongAuthStateChanged");
            }
        };
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());
        this.mUiOffloadThread = (UiOffloadThread) Dependency.get(UiOffloadThread.class);
        this.mStockStatusBar = true;
        this.mPortrait = true;
        this.mCustomSettingsObserver = new CustomSettingsObserver(hCreateHandler);
        this.mCheckBarModes = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.checkBarModes();
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.StatusBar.13
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                    KeyboardShortcuts.dismiss();
                    if (StatusBar.this.mRemoteInputManager.getController() != null) {
                        StatusBar.this.mRemoteInputManager.getController().closeRemoteInputs();
                    }
                    if (StatusBar.this.mBubbleController.isStackExpanded()) {
                        StatusBar.this.mBubbleController.collapseStack();
                    }
                    if (StatusBar.this.mLockscreenUserManager.isCurrentProfile(getSendingUserId())) {
                        String stringExtra = intent.getStringExtra("reason");
                        if (stringExtra != null && stringExtra.equals("recentapps")) {
                            i = 2;
                        }
                        StatusBar.this.mShadeController.animateCollapsePanels(i);
                        return;
                    }
                    return;
                }
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    NotificationShadeWindowController notificationShadeWindowController2 = StatusBar.this.mNotificationShadeWindowController;
                    if (notificationShadeWindowController2 != null) {
                        notificationShadeWindowController2.setNotTouchable(false);
                    }
                    if (StatusBar.this.mBubbleController.isStackExpanded()) {
                        StatusBar.this.mBubbleController.collapseStack();
                    }
                    StatusBar.this.finishBarAnimations();
                    StatusBar.this.resetUserExpandedStates();
                    return;
                }
                if ("android.app.action.SHOW_DEVICE_MONITORING_DIALOG".equals(action)) {
                    StatusBar.this.mQSPanel.showDeviceMonitoringDialog();
                } else if ("lineageos.intent.action.SCREEN_CAMERA_GESTURE".equals(action)) {
                    if ((Settings.Secure.getInt(((SystemUI) StatusBar.this).mContext.getContentResolver(), "user_setup_complete", 0) != 0 ? 1 : 0) == 0) {
                        return;
                    }
                    StatusBar.this.onCameraLaunchGestureDetected(3);
                }
            }
        };
        this.mDemoReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.StatusBar.14
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("com.android.systemui.demo".equals(action)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String lowerCase = extras.getString("command", "").trim().toLowerCase();
                        if (lowerCase.length() > 0) {
                            try {
                                StatusBar.this.dispatchDemoCommand(lowerCase, extras);
                                return;
                            } catch (Throwable th) {
                                Log.w("StatusBar", "Error running demo command, intent=" + intent, th);
                                return;
                            }
                        }
                        return;
                    }
                    return;
                }
                "fake_artwork".equals(action);
            }
        };
        this.mStartTracing = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar.15
            @Override // java.lang.Runnable
            public void run() {
                StatusBar.this.vibrate();
                SystemClock.sleep(250L);
                Log.d("StatusBar", "startTracing");
                Debug.startMethodTracing("/data/statusbar-traces/trace");
                StatusBar statusBar = StatusBar.this;
                statusBar.mHandler.postDelayed(statusBar.mStopTracing, 10000L);
            }
        };
        this.mStopTracing = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$21();
            }
        };
        this.mWakefulnessObserver = new AnonymousClass16();
        this.mSystemUiGcOpt = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar.17
            @Override // java.lang.Runnable
            public void run() {
                long jCurrentTimeMillis = System.currentTimeMillis();
                if (StatusBar.this.lastGcTime == 0 || jCurrentTimeMillis - StatusBar.this.lastGcTime > 600000) {
                    Log.v("GcOpt", "performing garbage collection for SystemUI");
                    System.gc();
                    System.runFinalization();
                    System.gc();
                    StatusBar.this.lastGcTime = jCurrentTimeMillis;
                }
            }
        };
        this.mScreenObserver = new ScreenLifecycle.Observer() { // from class: com.android.systemui.statusbar.phone.StatusBar.18
            @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
            public void onScreenTurningOn() {
                StatusBar.this.mFalsingManager.onScreenTurningOn();
                StatusBar.this.mNotificationPanelViewController.onScreenTurningOn();
            }

            @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
            public void onScreenTurnedOn() {
                StatusBar.this.mScrimController.onScreenTurnedOn();
            }

            @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
            public void onScreenTurnedOff() {
                StatusBar.this.mFalsingManager.onScreenOff();
                StatusBar.this.mScrimController.onScreenTurnedOff();
                if (StatusBar.this.mNotificationPanelViewController.isQsExpanded()) {
                    StatusBar.this.mNotificationPanelViewController.closeQs();
                }
                StatusBar.this.updateIsKeyguard();
            }
        };
        this.mBannerActionBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.StatusBar.20
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("com.android.systemui.statusbar.banner_action_cancel".equals(action) || "com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                    ((NotificationManager) ((SystemUI) StatusBar.this).mContext.getSystemService("notification")).cancel(5);
                    Settings.Secure.putInt(((SystemUI) StatusBar.this).mContext.getContentResolver(), "show_note_about_notification_hiding", 0);
                    if ("com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                        StatusBar.this.mShadeController.animateCollapsePanels(2, true);
                        ((SystemUI) StatusBar.this).mContext.startActivity(new Intent("android.settings.ACTION_APP_NOTIFICATION_REDACTION").addFlags(268435456));
                    }
                }
            }
        };
        this.mNotificationsController = notificationsController;
        this.mLightBarController = lightBarController;
        this.mAutoHideController = autoHideController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mIconController = statusBarIconController;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mKeyguardStateController = keyguardStateController;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mKeyguardIndicationController = keyguardIndicationController;
        this.mStatusBarTouchableRegionManager = statusBarTouchableRegionManager;
        this.mDynamicPrivacyController = dynamicPrivacyController;
        this.mBypassHeadsUpNotifier = bypassHeadsUpNotifier;
        this.mFalsingManager = falsingManager;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mRemoteInputQuickSettingsDisabler = remoteInputQuickSettingsDisabler;
        this.mGutsManager = notificationGutsManager;
        this.mNotificationLogger = notificationLogger;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mViewHierarchyManager = notificationViewHierarchyManager;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mDisplayMetrics = displayMetrics;
        this.mMetricsLogger = metricsLogger;
        this.mUiBgExecutor = executor;
        this.mMediaManager = notificationMediaManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mUserSwitcherController = userSwitcherController;
        this.mNetworkController = networkController;
        this.mBatteryController = batteryController;
        this.mColorExtractor = sysuiColorExtractor;
        this.mScreenLifecycle = screenLifecycle;
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mVibratorHelper = vibratorHelper;
        this.mBubbleController = bubbleController;
        this.mGroupManager = notificationGroupManager;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mNavigationBarController = navigationBarController;
        this.mAssistManagerLazy = lazy;
        this.mConfigurationController = configurationController;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mLockscreenLockIconController = lockscreenLockIconController;
        this.mDozeServiceHost = dozeServiceHost;
        this.mPowerManager = powerManager;
        this.mDozeParameters = dozeParameters;
        this.mScrimController = scrimController;
        this.mKeyguardLiftController = keyguardLiftController;
        this.mLockscreenWallpaperLazy = lazy2;
        this.mScreenPinningRequest = screenPinningRequest;
        this.mDozeScrimController = dozeScrimController;
        this.mBiometricUnlockControllerLazy = lazy3;
        this.mNotificationShadeDepthControllerLazy = lazy4;
        this.mVolumeComponent = volumeComponent;
        this.mCommandQueue = commandQueue;
        this.mRecentsOptional = optional;
        this.mStatusBarComponentBuilder = provider;
        this.mPluginManager = pluginManager;
        this.mDividerOptional = optional2;
        this.mStatusBarNotificationActivityStarterBuilder = builder;
        this.mShadeController = shadeController;
        this.mSuperStatusBarViewFactory = superStatusBarViewFactory;
        this.mLightsOutNotifController = lightsOutNotifController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mKeyguardViewMediatorCallback = viewMediatorCallback;
        this.mInitController = initController;
        this.mDarkIconDispatcher = darkIconDispatcher;
        this.mPluginDependencyProvider = pluginDependencyProvider;
        this.mKeyguardDismissUtil = keyguardDismissUtil;
        this.mExtensionController = extensionController;
        this.mUserInfoControllerImpl = userInfoControllerImpl;
        this.mIconPolicy = phoneStatusBarPolicy;
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mTunerService = tunerService;
        this.mFODCircleViewImpl = fODCircleViewImpl;
        this.mBubbleExpandListener = new BubbleController.BubbleExpandListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda3
            @Override // com.android.systemui.bubbles.BubbleController.BubbleExpandListener
            public final void onBubbleExpandChanged(boolean z, String str) {
                this.f$0.lambda$new$1(z, str);
            }
        };
        DateTimeView.setReceiverHandler(handler);
        this.mFingerprintService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(boolean z, String str) {
        this.mNotificationsController.requestNotificationUpdate("onBubbleExpandChanged");
        updateScrimController();
    }

    @Override // com.android.systemui.SystemUI
    public void start() throws Resources.NotFoundException {
        RegisterStatusBarResult registerStatusBarResultRegisterStatusBar;
        this.mScreenLifecycle.addObserver(this.mScreenObserver);
        this.mWakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
        this.mOverlayManager = (OverlayManager) this.mContext.getSystemService(OverlayManager.class);
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService(UiModeManager.class);
        this.mBypassHeadsUpNotifier.setUp();
        this.mBubbleController.setExpandListener(this.mBubbleExpandListener);
        this.mActivityIntentHelper = new ActivityIntentHelper(this.mContext);
        this.mColorExtractor.addOnColorsChangedListener(this);
        this.mStatusBarStateController.addCallback(this, 0);
        this.mTunerService.addTunable(this, "system:force_show_navbar");
        this.mTunerService.addTunable(this, "system:screen_brightness_mode");
        this.mTunerService.addTunable(this, "lineagesystem:status_bar_brightness_control");
        this.mTunerService.addTunable(this, "system:qs_rows_portrait");
        this.mTunerService.addTunable(this, "system:qs_rows_landscape");
        this.mTunerService.addTunable(this, "system:qs_columns_portrait");
        this.mTunerService.addTunable(this, "system:qs_columns_landscape");
        this.mTunerService.addTunable(this, "system:qs_tile_title_visibility");
        this.mTunerService.addTunable(this, "system:gaming_mode_active");
        this.mTunerService.addTunable(this, "system:gaming_mode_headsup_toggle");
        this.mTunerService.addTunable(this, "system:less_boring_heads_up");
        this.mTunerService.addTunable(this, "system:navbar_style");
        this.mTunerService.addTunable(this, "system:qs_panel_bg_use_new_tint");
        this.mTunerService.addTunable(this, "pulse_on_new_tracks");
        this.mTunerService.addTunable(this, "system:notification_material_dismiss");
        this.mTunerService.addTunable(this, "system:display_cutout_mode");
        this.mTunerService.addTunable(this, "system:stock_statusbar_in_hide");
        this.mTunerService.addTunable(this, "system:qs_panel_icons_primary_color");
        this.mTunerService.addTunable(this, "system:display_kill_notch");
        this.mTunerService.addTunable(this, "system:berry_rounded_style");
        this.mTunerService.addTunable(this, "system:berry_signal_style");
        this.mTunerService.addTunable(this, "system:berry_wifi_style");
        this.mTunerService.addTunable(this, "sysui_rounded_fwvals");
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
        Display defaultDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        this.mDisplayId = defaultDisplay.getDisplayId();
        updateDisplaySize();
        this.mVibrateOnOpening = this.mContext.getResources().getBoolean(com.android.systemui.R.bool.config_vibrateOnIconAnimation);
        this.mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mIOverlayManager = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
        this.mKeyguardUpdateMonitor.setKeyguardBypassController(this.mKeyguardBypassController);
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mWallpaperSupported = ((WallpaperManager) this.mContext.getSystemService(WallpaperManager.class)).isWallpaperSupported();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mMediaManager.addCallback((NotificationMediaManager.MediaListener) Dependency.get(PulseController.class));
        try {
            registerStatusBarResultRegisterStatusBar = this.mBarService.registerStatusBar(this.mCommandQueue);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            registerStatusBarResultRegisterStatusBar = null;
        }
        createAndAddWindows(registerStatusBarResultRegisterStatusBar);
        if (this.mWallpaperSupported) {
            this.mBroadcastDispatcher.registerReceiver(this.mWallpaperChangedReceiver, new IntentFilter("android.intent.action.WALLPAPER_CHANGED"), null, UserHandle.ALL);
            this.mWallpaperChangedReceiver.onReceive(this.mContext, null);
        }
        setUpPresenter();
        if (InsetsState.containsType(registerStatusBarResultRegisterStatusBar.mTransientBarTypes, 0)) {
            showTransientUnchecked();
        }
        onSystemBarAppearanceChanged(this.mDisplayId, registerStatusBarResultRegisterStatusBar.mAppearance, registerStatusBarResultRegisterStatusBar.mAppearanceRegions, registerStatusBarResultRegisterStatusBar.mNavbarColorManagedByIme);
        this.mAppFullscreen = registerStatusBarResultRegisterStatusBar.mAppFullscreen;
        this.mAppImmersive = registerStatusBarResultRegisterStatusBar.mAppImmersive;
        this.mCustomSettingsObserver.observe();
        setImeWindowStatus(this.mDisplayId, registerStatusBarResultRegisterStatusBar.mImeToken, registerStatusBarResultRegisterStatusBar.mImeWindowVis, registerStatusBarResultRegisterStatusBar.mImeBackDisposition, registerStatusBarResultRegisterStatusBar.mShowImeSwitcher);
        int size = registerStatusBarResultRegisterStatusBar.mIcons.size();
        for (int i = 0; i < size; i++) {
            this.mCommandQueue.setIcon((String) registerStatusBarResultRegisterStatusBar.mIcons.keyAt(i), (StatusBarIcon) registerStatusBarResultRegisterStatusBar.mIcons.valueAt(i));
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.statusbar.banner_action_cancel");
        intentFilter.addAction("com.android.systemui.statusbar.banner_action_setup");
        this.mContext.registerReceiver(this.mBannerActionBroadcastReceiver, intentFilter, "com.android.systemui.permission.SELF", null);
        if (this.mWallpaperSupported) {
            try {
                IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).setInAmbientMode(false, 0L);
            } catch (RemoteException unused) {
            }
        }
        this.mIconPolicy.init();
        this.mSignalPolicy = new StatusBarSignalPolicy(this.mContext, this.mIconController);
        this.mKeyguardStateController.addCallback(this);
        startKeyguard();
        this.mKeyguardUpdateMonitor.registerCallback(this.mUpdateCallback);
        this.mDozeServiceHost.initialize(this, this.mNotificationIconAreaController, this.mStatusBarKeyguardViewManager, this.mNotificationShadeWindowViewController, this.mNotificationPanelViewController, this.mAmbientIndicationContainer);
        this.mConfigurationController.addCallback(this);
        this.mBatteryController.observe((Lifecycle) this.mLifecycle, (LifecycleRegistry) this);
        this.mLifecycle.setCurrentState(Lifecycle.State.RESUMED);
        final int i2 = registerStatusBarResultRegisterStatusBar.mDisabledFlags1;
        final int i3 = registerStatusBarResultRegisterStatusBar.mDisabledFlags2;
        this.mInitController.addPostInitTask(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda28
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$start$2(i2, i3);
            }
        });
        this.mPluginManager.addPluginListener((PluginListener) new AnonymousClass7(), OverlayPlugin.class, true);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService(ActivityManager.class);
        this.mFODCircleViewImpl.registerCallback(this.mFODCircleViewImplCallback);
    }

    /* renamed from: com.android.systemui.statusbar.phone.StatusBar$7, reason: invalid class name */
    class AnonymousClass7 implements PluginListener<OverlayPlugin> {
        private ArraySet<OverlayPlugin> mOverlays = new ArraySet<>();

        AnonymousClass7() {
        }

        @Override // com.android.systemui.plugins.PluginListener
        public void onPluginConnected(final OverlayPlugin overlayPlugin, Context context) {
            StatusBar.this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$7$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onPluginConnected$0(overlayPlugin);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onPluginConnected$0(OverlayPlugin overlayPlugin) {
            overlayPlugin.setup(StatusBar.this.getNotificationShadeWindowView(), StatusBar.this.getNavigationBarView(), new Callback(overlayPlugin), StatusBar.this.mDozeParameters);
        }

        @Override // com.android.systemui.plugins.PluginListener
        public void onPluginDisconnected(final OverlayPlugin overlayPlugin) {
            StatusBar.this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$7$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onPluginDisconnected$1(overlayPlugin);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onPluginDisconnected$1(OverlayPlugin overlayPlugin) {
            this.mOverlays.remove(overlayPlugin);
            StatusBar.this.mNotificationShadeWindowController.setForcePluginOpen(this.mOverlays.size() != 0);
        }

        /* renamed from: com.android.systemui.statusbar.phone.StatusBar$7$Callback */
        class Callback implements OverlayPlugin.Callback {
            private final OverlayPlugin mPlugin;

            Callback(OverlayPlugin overlayPlugin) {
                this.mPlugin = overlayPlugin;
            }

            @Override // com.android.systemui.plugins.OverlayPlugin.Callback
            public void onHoldStatusBarOpenChange() {
                if (this.mPlugin.holdStatusBarOpen()) {
                    AnonymousClass7.this.mOverlays.add(this.mPlugin);
                } else {
                    AnonymousClass7.this.mOverlays.remove(this.mPlugin);
                }
                StatusBar.this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$7$Callback$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onHoldStatusBarOpenChange$2();
                    }
                });
            }

            /* JADX INFO: Access modifiers changed from: private */
            public /* synthetic */ void lambda$onHoldStatusBarOpenChange$2() {
                StatusBar.this.mNotificationShadeWindowController.setStateListener(new NotificationShadeWindowController.OtherwisedCollapsedListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$7$Callback$$ExternalSyntheticLambda0
                    @Override // com.android.systemui.statusbar.phone.NotificationShadeWindowController.OtherwisedCollapsedListener
                    public final void setWouldOtherwiseCollapse(boolean z) {
                        this.f$0.lambda$onHoldStatusBarOpenChange$1(z);
                    }
                });
                AnonymousClass7 anonymousClass7 = AnonymousClass7.this;
                StatusBar.this.mNotificationShadeWindowController.setForcePluginOpen(anonymousClass7.mOverlays.size() != 0);
            }

            /* JADX INFO: Access modifiers changed from: private */
            public /* synthetic */ void lambda$onHoldStatusBarOpenChange$1(final boolean z) {
                AnonymousClass7.this.mOverlays.forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.StatusBar$7$Callback$$ExternalSyntheticLambda2
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((OverlayPlugin) obj).setCollapseDesired(z);
                    }
                });
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void makeStatusBarView(RegisterStatusBarResult registerStatusBarResult) throws Resources.NotFoundException {
        Context context = this.mContext;
        updateDisplaySize();
        updateResources();
        updateTheme();
        inflateStatusBarWindow();
        this.mNotificationShadeWindowViewController.setService(this, this.mNotificationShadeWindowController);
        this.mNotificationShadeWindowView.setOnTouchListener(getStatusBarWindowTouchListener());
        this.mDismissAllButton = (ImageButton) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.clear_notifications);
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mMinimumBacklight = powerManager.getBrightnessConstraint(0);
        this.mMaximumBacklight = powerManager.getBrightnessConstraint(1);
        ViewGroup viewGroup = (ViewGroup) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.notification_stack_scroller);
        this.mStackScroller = viewGroup;
        this.mNotificationLogger.setUpWithContainer((NotificationListContainer) viewGroup);
        NotificationIconAreaController notificationIconAreaControllerCreateNotificationIconAreaController = SystemUIFactory.getInstance().createNotificationIconAreaController(context, this, this.mWakeUpCoordinator, this.mKeyguardBypassController, this.mStatusBarStateController);
        this.mNotificationIconAreaController = notificationIconAreaControllerCreateNotificationIconAreaController;
        this.mWakeUpCoordinator.setIconAreaController(notificationIconAreaControllerCreateNotificationIconAreaController);
        inflateShelf();
        this.mNotificationIconAreaController.setupShelf(this.mNotificationShelf);
        NotificationPanelViewController notificationPanelViewController = this.mNotificationPanelViewController;
        final NotificationIconAreaController notificationIconAreaController = this.mNotificationIconAreaController;
        Objects.requireNonNull(notificationIconAreaController);
        notificationPanelViewController.setOnReinflationListener(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                notificationIconAreaController.initAodIcons();
            }
        });
        this.mNotificationPanelViewController.addExpansionListener(this.mWakeUpCoordinator);
        this.mDarkIconDispatcher.addDarkReceiver(this.mNotificationIconAreaController);
        this.mPluginDependencyProvider.allowPluginDependency(DarkIconDispatcher.class);
        this.mPluginDependencyProvider.allowPluginDependency(StatusBarStateController.class);
        FragmentHostManager.get(this.mPhoneStatusBarWindow).addTagListener("CollapsedStatusBarFragment", new FragmentHostManager.FragmentListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda4
            @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
            public final void onFragmentViewCreated(String str, Fragment fragment) {
                this.f$0.lambda$makeStatusBarView$3(str, fragment);
            }
        }).getFragmentManager().beginTransaction().replace(com.android.systemui.R.id.status_bar_container, new CollapsedStatusBarFragment(), "CollapsedStatusBarFragment").commit();
        this.mHeadsUpManager.setup(this.mVisualStabilityManager);
        this.mStatusBarTouchableRegionManager.setup(this, this.mNotificationShadeWindowView);
        this.mHeadsUpManager.addListener(this);
        this.mHeadsUpManager.addListener(this.mNotificationPanelViewController.getOnHeadsUpChangedListener());
        this.mHeadsUpManager.addListener(this.mVisualStabilityManager);
        this.mNotificationPanelViewController.setHeadsUpManager(this.mHeadsUpManager);
        this.mNotificationLogger.setHeadsUpManager(this.mHeadsUpManager);
        createNavigationBar(registerStatusBarResult);
        if (this.mWallpaperSupported) {
            this.mLockscreenWallpaper = this.mLockscreenWallpaperLazy.get();
        }
        this.mKeyguardIndicationController.setIndicationArea((ViewGroup) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.keyguard_indication_area));
        this.mNotificationPanelViewController.setKeyguardIndicationController(this.mKeyguardIndicationController);
        this.mAmbientIndicationContainer = this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.ambient_indication_container);
        this.mAutoHideController.setStatusBar(new AutoHideUiElement() { // from class: com.android.systemui.statusbar.phone.StatusBar.8
            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void synchronizeState() {
                StatusBar.this.checkBarModes();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean shouldHideOnTouch() {
                return !StatusBar.this.mRemoteInputManager.getController().isRemoteInputActive();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean isVisible() {
                return StatusBar.this.isTransientShown();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void hide() {
                StatusBar.this.clearTransient();
            }
        });
        ScrimView scrimView = (ScrimView) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.scrim_behind);
        ScrimView scrimView2 = (ScrimView) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.scrim_in_front);
        ScrimView scrimForBubble = this.mBubbleController.getScrimForBubble();
        this.mScrimController.setScrimVisibleListener(new Consumer() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda42
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$makeStatusBarView$4((Integer) obj);
            }
        });
        this.mScrimController.attachViews(scrimView, scrimView2, scrimForBubble);
        this.mNotificationPanelViewController.initDependencies(this, this.mGroupManager, this.mNotificationShelf, this.mNotificationIconAreaController, this.mScrimController);
        final BackDropView backDropView = (BackDropView) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.backdrop);
        this.mMediaManager.setup(backDropView, (ImageView) backDropView.findViewById(com.android.systemui.R.id.backdrop_front), (ImageView) backDropView.findViewById(com.android.systemui.R.id.backdrop_back), this.mScrimController, this.mLockscreenWallpaper);
        final float f = this.mContext.getResources().getFloat(R.dimen.config_hapticChannelMaxVibrationAmplitude);
        this.mNotificationShadeDepthControllerLazy.get().addListener(new NotificationShadeDepthController.DepthListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda8
            @Override // com.android.systemui.statusbar.NotificationShadeDepthController.DepthListener
            public final void onWallpaperZoomOutChanged(float f2) {
                StatusBar.lambda$makeStatusBarView$5(f, backDropView, f2);
            }
        });
        this.mNotificationPanelViewController.setUserSetupComplete(this.mUserSetup);
        if (UserManager.get(this.mContext).isUserSwitcherEnabled()) {
            createUserSwitcher();
        }
        NotificationPanelViewController notificationPanelViewController2 = this.mNotificationPanelViewController;
        final LockscreenLockIconController lockscreenLockIconController = this.mLockscreenLockIconController;
        Objects.requireNonNull(lockscreenLockIconController);
        notificationPanelViewController2.setLaunchAffordanceListener(new Consumer() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda39
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                lockscreenLockIconController.onShowingLaunchAffordanceChanged((Boolean) obj);
            }
        });
        NotificationShadeWindowView notificationShadeWindowView = this.mNotificationShadeWindowView;
        int i = com.android.systemui.R.id.qs_frame;
        View viewFindViewById = notificationShadeWindowView.findViewById(i);
        if (viewFindViewById != null) {
            FragmentHostManager fragmentHostManager = FragmentHostManager.get(viewFindViewById);
            ExtensionFragmentListener.attachExtensonToFragment(viewFindViewById, QS.TAG, i, this.mExtensionController.newExtension(QS.class).withPlugin(QS.class).withDefault(new Supplier() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda44
                @Override // java.util.function.Supplier
                public final Object get() {
                    return this.f$0.createDefaultQSFragment();
                }
            }).build());
            this.mBrightnessMirrorController = new BrightnessMirrorController(this.mNotificationShadeWindowView, this.mNotificationPanelViewController, this.mNotificationShadeDepthControllerLazy.get(), new Consumer() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda41
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.lambda$makeStatusBarView$6((Boolean) obj);
                }
            });
            fragmentHostManager.addTagListener(QS.TAG, new FragmentHostManager.FragmentListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda5
                @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
                public final void onFragmentViewCreated(String str, Fragment fragment) {
                    this.f$0.lambda$makeStatusBarView$7(str, fragment);
                }
            });
        }
        this.mVisualizerView = (VisualizerView) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.visualizerview);
        View viewFindViewById2 = this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.report_rejected_touch);
        this.mReportRejectedTouch = viewFindViewById2;
        if (viewFindViewById2 != null) {
            updateReportRejectedTouchVisibility();
            this.mReportRejectedTouch.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$makeStatusBarView$8(view);
                }
            });
        }
        if (!this.mPowerManager.isScreenOn()) {
            this.mBroadcastReceiver.onReceive(this.mContext, new Intent("android.intent.action.SCREEN_OFF"));
        }
        this.mGestureWakeLock = this.mPowerManager.newWakeLock(10, "GestureWakeLock");
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        int[] intArray = this.mContext.getResources().getIntArray(com.android.systemui.R.array.config_cameraLaunchGestureVibePattern);
        this.mCameraLaunchGestureVibePattern = new long[intArray.length];
        for (int i2 = 0; i2 < intArray.length; i2++) {
            this.mCameraLaunchGestureVibePattern[i2] = intArray[i2];
        }
        registerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.demo");
        context.registerReceiverAsUser(this.mDemoReceiver, UserHandle.ALL, intentFilter, "android.permission.DUMP", null);
        this.mDeviceProvisionedController.addCallback(this.mUserSetupObserver);
        this.mUserSetupObserver.onUserSetupChanged();
        ThreadedRenderer.overrideProperty("disableProfileBars", "true");
        ThreadedRenderer.overrideProperty("ambientRatio", String.valueOf(1.5f));
        this.mFlashlightController = (FlashlightController) Dependency.get(FlashlightController.class);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$makeStatusBarView$3(String str, Fragment fragment) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = (CollapsedStatusBarFragment) fragment;
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        PhoneStatusBarView phoneStatusBarView2 = (PhoneStatusBarView) collapsedStatusBarFragment.getView();
        this.mStatusBarView = phoneStatusBarView2;
        phoneStatusBarView2.setBar(this);
        this.mStatusBarView.setPanel(this.mNotificationPanelViewController);
        this.mStatusBarView.setScrimController(this.mScrimController);
        collapsedStatusBarFragment.initNotificationIconArea(this.mNotificationIconAreaController);
        if (this.mHeadsUpManager.hasPinnedHeadsUp()) {
            this.mNotificationPanelViewController.notifyBarPanelExpansionChanged();
        }
        this.mStatusBarView.setBouncerShowing(this.mBouncerShowing);
        if (phoneStatusBarView != null) {
            this.mStatusBarView.panelExpansionChanged(phoneStatusBarView.getExpansionFraction(), phoneStatusBarView.isExpanded());
        }
        HeadsUpAppearanceController headsUpAppearanceController = this.mHeadsUpAppearanceController;
        if (headsUpAppearanceController != null) {
            headsUpAppearanceController.destroy();
        }
        HeadsUpAppearanceController headsUpAppearanceController2 = new HeadsUpAppearanceController(this.mNotificationIconAreaController, this.mHeadsUpManager, this.mNotificationShadeWindowView, this.mStatusBarStateController, this.mKeyguardBypassController, this.mKeyguardStateController, this.mWakeUpCoordinator, this.mCommandQueue, this.mNotificationPanelViewController, this.mStatusBarView);
        this.mHeadsUpAppearanceController = headsUpAppearanceController2;
        headsUpAppearanceController2.readFrom(headsUpAppearanceController);
        this.mLightsOutNotifController.setLightsOutNotifView(this.mStatusBarView.findViewById(com.android.systemui.R.id.notification_lights_out));
        this.mNotificationShadeWindowViewController.setStatusBarView(this.mStatusBarView);
        checkBarModes();
        handleCutout();
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "burnin_protection_enabled", 1, -2) == 1) {
            this.mBurnInProtectionController = new BurnInProtectionController(this.mContext, this, this.mStatusBarView);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$makeStatusBarView$4(Integer num) {
        this.mNotificationShadeWindowController.setScrimsVisibility(num.intValue());
        if (this.mNotificationShadeWindowView != null) {
            this.mLockscreenLockIconController.onScrimVisibilityChanged(num);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$makeStatusBarView$5(float f, BackDropView backDropView, float f2) {
        float fLerp = MathUtils.lerp(f, 1.0f, f2);
        backDropView.setPivotX(backDropView.getWidth() / 2.0f);
        backDropView.setPivotY(backDropView.getHeight() / 2.0f);
        backDropView.setScaleX(fLerp);
        backDropView.setScaleY(fLerp);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$makeStatusBarView$6(Boolean bool) {
        this.mBrightnessMirrorVisible = bool.booleanValue();
        updateScrimController();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$makeStatusBarView$7(String str, Fragment fragment) {
        QS qs = (QS) fragment;
        if (qs instanceof QSFragment) {
            QSPanel qsPanel = ((QSFragment) qs).getQsPanel();
            this.mQSPanel = qsPanel;
            qsPanel.setBrightnessMirror(this.mBrightnessMirrorController);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$makeStatusBarView$8(View view) {
        Uri uriReportRejectedTouch = this.mFalsingManager.reportRejectedTouch();
        if (uriReportRejectedTouch == null) {
            return;
        }
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("Build info: ");
        stringWriter.write(SystemProperties.get("ro.build.description"));
        stringWriter.write("\nSerial number: ");
        stringWriter.write(SystemProperties.get("ro.serialno"));
        stringWriter.write("\n");
        PrintWriter printWriter = new PrintWriter(stringWriter);
        FalsingLog.dump(printWriter);
        printWriter.flush();
        startActivityDismissingKeyguard(Intent.createChooser(new Intent("android.intent.action.SEND").setType("*/*").putExtra("android.intent.extra.SUBJECT", "Rejected touch report").putExtra("android.intent.extra.STREAM", uriReportRejectedTouch).putExtra("android.intent.extra.TEXT", stringWriter.toString()), "Share rejected touch report").addFlags(268435456), true, true);
    }

    @Override // androidx.lifecycle.LifecycleOwner
    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        this.mHandler.post(this.mCheckBarModes);
        DozeServiceHost dozeServiceHost = this.mDozeServiceHost;
        if (dozeServiceHost != null) {
            dozeServiceHost.firePowerSaveChanged(z);
        }
    }

    @VisibleForTesting
    protected void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.app.action.SHOW_DEVICE_MONITORING_DIALOG");
        intentFilter.addAction("lineageos.intent.action.SCREEN_CAMERA_GESTURE");
        intentFilter.addAction("cancel_notification_pulse");
        this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter, null, UserHandle.ALL);
    }

    protected QS createDefaultQSFragment() {
        return (QS) FragmentHostManager.get(this.mNotificationShadeWindowView).create(QSFragment.class);
    }

    private void setUpPresenter() {
        ActivityLaunchAnimator activityLaunchAnimator = new ActivityLaunchAnimator(this.mNotificationShadeWindowViewController, this, this.mNotificationPanelViewController, this.mNotificationShadeDepthControllerLazy.get(), (NotificationListContainer) this.mStackScroller, this.mContext.getMainExecutor(), this.mFODCircleViewImpl);
        this.mActivityLaunchAnimator = activityLaunchAnimator;
        StatusBarNotificationPresenter statusBarNotificationPresenter = new StatusBarNotificationPresenter(this.mContext, this.mNotificationPanelViewController, this.mHeadsUpManager, this.mNotificationShadeWindowView, this.mStackScroller, this.mDozeScrimController, this.mScrimController, activityLaunchAnimator, this.mDynamicPrivacyController, this.mKeyguardStateController, this.mKeyguardIndicationController, this, this.mShadeController, this.mCommandQueue, this.mInitController, this.mNotificationInterruptStateProvider);
        this.mPresenter = statusBarNotificationPresenter;
        this.mNotificationShelf.setOnActivatedListener(statusBarNotificationPresenter);
        this.mRemoteInputManager.getController().addCallback(this.mNotificationShadeWindowController);
        StatusBarNotificationActivityStarter statusBarNotificationActivityStarterBuild = this.mStatusBarNotificationActivityStarterBuilder.setStatusBar(this).setActivityLaunchAnimator(this.mActivityLaunchAnimator).setNotificationPresenter(this.mPresenter).setNotificationPanelViewController(this.mNotificationPanelViewController).build();
        this.mNotificationActivityStarter = statusBarNotificationActivityStarterBuild;
        ((NotificationListContainer) this.mStackScroller).setNotificationActivityStarter(statusBarNotificationActivityStarterBuild);
        this.mGutsManager.setNotificationActivityStarter(this.mNotificationActivityStarter);
        NotificationsController notificationsController = this.mNotificationsController;
        StatusBarNotificationPresenter statusBarNotificationPresenter2 = this.mPresenter;
        notificationsController.initialize(this, statusBarNotificationPresenter2, (NotificationListContainer) this.mStackScroller, this.mNotificationActivityStarter, statusBarNotificationPresenter2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* renamed from: setUpDisableFlags, reason: merged with bridge method [inline-methods] */
    public void lambda$start$2(int i, int i2) {
        this.mCommandQueue.disable(this.mDisplayId, i, i2, false);
    }

    public void wakeUpIfDozing(long j, View view, String str) {
        if (this.mDozing) {
            this.mPowerManager.wakeUp(j, 4, "com.android.systemui:" + str);
            this.mWakeUpComingFromTouch = true;
            view.getLocationInWindow(this.mTmpInt2);
            this.mWakeUpTouchLocation = new PointF((float) (this.mTmpInt2[0] + (view.getWidth() / 2)), (float) (this.mTmpInt2[1] + (view.getHeight() / 2)));
            this.mFalsingManager.onScreenOnFromTouch();
        }
    }

    protected void createNavigationBar(RegisterStatusBarResult registerStatusBarResult) {
        this.mNavigationBarController.createNavigationBars(true, registerStatusBarResult);
    }

    protected View.OnTouchListener getStatusBarWindowTouchListener() {
        return new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda2
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.lambda$getStatusBarWindowTouchListener$9(view, motionEvent);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$getStatusBarWindowTouchListener$9(View view, MotionEvent motionEvent) {
        this.mAutoHideController.checkUserAutoHide(motionEvent);
        this.mRemoteInputManager.checkRemoteInputOutside(motionEvent);
        if (motionEvent.getAction() == 0 && this.mExpandedVisible) {
            this.mShadeController.animateCollapsePanels();
        }
        return this.mNotificationShadeWindowView.onTouchEvent(motionEvent);
    }

    private void inflateShelf() {
        NotificationShelf notificationShelf = this.mSuperStatusBarViewFactory.getNotificationShelf(this.mStackScroller);
        this.mNotificationShelf = notificationShelf;
        notificationShelf.setOnClickListener(this.mGoToLockedShadeListener);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onDensityOrFontScaleChanged();
        }
        this.mUserInfoControllerImpl.onDensityOrFontScaleChanged();
        this.mUserSwitcherController.onDensityOrFontScaleChanged();
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        if (keyguardUserSwitcher != null) {
            keyguardUserSwitcher.onDensityOrFontScaleChanged();
        }
        this.mNotificationIconAreaController.onDensityOrFontScaleChanged(this.mContext);
        this.mHeadsUpManager.onDensityOrFontScaleChanged();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            statusBarKeyguardViewManager.onThemeChanged();
        }
        View view = this.mAmbientIndicationContainer;
        if (view instanceof AutoReinflateContainer) {
            ((AutoReinflateContainer) view).inflateLayout();
        }
        this.mNotificationIconAreaController.onThemeChanged();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onOverlayChanged();
        }
        this.mNotificationPanelViewController.onThemeChanged();
        onThemeChanged();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onUiModeChanged();
        }
    }

    protected void createUserSwitcher() {
        this.mKeyguardUserSwitcher = new KeyguardUserSwitcher(this.mContext, (ViewStub) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.keyguard_user_switcher), (KeyguardStatusBarView) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.keyguard_header), this.mNotificationPanelViewController);
    }

    private void inflateStatusBarWindow() {
        this.mNotificationShadeWindowView = this.mSuperStatusBarViewFactory.getNotificationShadeWindowView();
        StatusBarComponent statusBarComponentBuild = this.mStatusBarComponentBuilder.get().statusBarWindowView(this.mNotificationShadeWindowView).build();
        this.mNotificationShadeWindowViewController = statusBarComponentBuild.getNotificationShadeWindowViewController();
        this.mNotificationShadeWindowController.setNotificationShadeView(this.mNotificationShadeWindowView);
        this.mNotificationShadeWindowViewController.setupExpandedStatusBar();
        this.mStatusBarWindowController = statusBarComponentBuild.getStatusBarWindowController();
        this.mPhoneStatusBarWindow = this.mSuperStatusBarViewFactory.getStatusBarWindowView();
        this.mNotificationPanelViewController = statusBarComponentBuild.getNotificationPanelViewController();
    }

    protected void startKeyguard() {
        Trace.beginSection("StatusBar#startKeyguard");
        this.mBiometricUnlockController = this.mBiometricUnlockControllerLazy.get();
        this.mStatusBarKeyguardViewManager.registerStatusBar(this, getBouncerContainer(), this.mNotificationPanelViewController, this.mBiometricUnlockController, this.mDismissCallbackRegistry, (ViewGroup) this.mNotificationShadeWindowView.findViewById(com.android.systemui.R.id.lock_icon_container), this.mStackScroller, this.mKeyguardBypassController, this.mFalsingManager);
        this.mKeyguardIndicationController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mBiometricUnlockController.setKeyguardViewController(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputManager.getController().addCallback(this.mStatusBarKeyguardViewManager);
        this.mDynamicPrivacyController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mLightBarController.setBiometricUnlockController(this.mBiometricUnlockController);
        this.mMediaManager.setBiometricUnlockController(this.mBiometricUnlockController);
        this.mKeyguardDismissUtil.setDismissHandler(new KeyguardDismissHandler() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda9
            @Override // com.android.systemui.statusbar.phone.KeyguardDismissHandler
            public final void executeWhenUnlocked(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
                this.f$0.executeWhenUnlocked(onDismissAction, z);
            }
        });
        Trace.endSection();
    }

    protected View getStatusBarView() {
        return this.mStatusBarView;
    }

    public NotificationShadeWindowView getNotificationShadeWindowView() {
        return this.mNotificationShadeWindowView;
    }

    public NotificationShadeWindowViewController getNotificationShadeWindowViewController() {
        return this.mNotificationShadeWindowViewController;
    }

    protected ViewGroup getBouncerContainer() {
        return this.mNotificationShadeWindowView;
    }

    public int getStatusBarHeight() {
        return this.mStatusBarWindowController.getStatusBarHeight();
    }

    protected boolean toggleSplitScreenMode(int i, int i2) {
        if (!this.mRecentsOptional.isPresent()) {
            return false;
        }
        Divider divider = this.mDividerOptional.isPresent() ? this.mDividerOptional.get() : null;
        if (divider == null || !divider.isDividerVisible()) {
            int navBarPosition = WindowManagerWrapper.getInstance().getNavBarPosition(this.mDisplayId);
            if (navBarPosition == -1) {
                return false;
            }
            return this.mRecentsOptional.get().splitPrimaryTask(navBarPosition == 1 ? 1 : 0, null, i);
        }
        if (divider.isMinimized() && !divider.isHomeStackResizable()) {
            return false;
        }
        divider.onUndockingTask();
        if (i2 != -1) {
            this.mMetricsLogger.action(i2);
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x0029  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void updateQsExpansionEnabled() {
        /*
            r3 = this;
            com.android.systemui.statusbar.policy.DeviceProvisionedController r0 = r3.mDeviceProvisionedController
            boolean r0 = r0.isDeviceProvisioned()
            r1 = 1
            if (r0 == 0) goto L29
            boolean r0 = r3.mUserSetup
            if (r0 != 0) goto L17
            com.android.systemui.statusbar.policy.UserSwitcherController r0 = r3.mUserSwitcherController
            if (r0 == 0) goto L17
            boolean r0 = r0.isSimpleUserSwitcher()
            if (r0 != 0) goto L29
        L17:
            int r0 = r3.mDisabled2
            r2 = r0 & 4
            if (r2 != 0) goto L29
            r0 = r0 & r1
            if (r0 != 0) goto L29
            boolean r0 = r3.mDozing
            if (r0 != 0) goto L29
            boolean r0 = com.android.systemui.statusbar.phone.StatusBar.ONLY_CORE_APPS
            if (r0 != 0) goto L29
            goto L2a
        L29:
            r1 = 0
        L2a:
            com.android.systemui.statusbar.phone.NotificationPanelViewController r3 = r3.mNotificationPanelViewController
            r3.setQsExpansionEnabled(r1)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r0 = "updateQsExpansionEnabled - QS Expand enabled: "
            r3.append(r0)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            java.lang.String r0 = "StatusBar"
            android.util.Log.d(r0, r3)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.updateQsExpansionEnabled():void");
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void addQsTile(ComponentName componentName) {
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel == null || qSPanel.getHost() == null) {
            return;
        }
        this.mQSPanel.getHost().addTile(componentName);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void remQsTile(ComponentName componentName) {
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel == null || qSPanel.getHost() == null) {
            return;
        }
        this.mQSPanel.getHost().removeTile(componentName);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void clickTile(ComponentName componentName) {
        this.mQSPanel.clickTile(componentName);
    }

    public void requestNotificationUpdate(String str) {
        this.mNotificationsController.requestNotificationUpdate(str);
    }

    public void requestFaceAuth() {
        if (this.mKeyguardStateController.canDismissLockScreen()) {
            return;
        }
        this.mKeyguardUpdateMonitor.requestFaceAuth();
    }

    private void updateReportRejectedTouchVisibility() {
        View view = this.mReportRejectedTouch;
        if (view == null) {
            return;
        }
        view.setVisibility((this.mState == 1 && !this.mDozing && this.mFalsingManager.isReportingEnabled()) ? 0 : 4);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i != this.mDisplayId) {
            return;
        }
        int iAdjustDisableFlags = this.mRemoteInputQuickSettingsDisabler.adjustDisableFlags(i3);
        int i4 = this.mDisabled1 ^ i2;
        this.mDisabled1 = i2;
        int i5 = this.mDisabled2 ^ iAdjustDisableFlags;
        this.mDisabled2 = iAdjustDisableFlags;
        StringBuilder sb = new StringBuilder();
        sb.append("disable<");
        int i6 = i2 & 65536;
        sb.append(i6 != 0 ? 'E' : 'e');
        int i7 = 65536 & i4;
        sb.append(i7 != 0 ? '!' : ' ');
        sb.append((i2 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0 ? 'I' : 'i');
        sb.append((131072 & i4) != 0 ? '!' : ' ');
        sb.append((i2 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0 ? 'A' : 'a');
        int i8 = 262144 & i4;
        sb.append(i8 != 0 ? '!' : ' ');
        sb.append((i2 & 1048576) != 0 ? 'S' : 's');
        sb.append((1048576 & i4) != 0 ? '!' : ' ');
        sb.append((i2 & 4194304) != 0 ? 'B' : 'b');
        sb.append((4194304 & i4) != 0 ? '!' : ' ');
        sb.append((i2 & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0 ? 'H' : 'h');
        sb.append((2097152 & i4) != 0 ? '!' : ' ');
        int i9 = i2 & 16777216;
        sb.append(i9 != 0 ? 'R' : 'r');
        int i10 = i4 & 16777216;
        sb.append(i10 != 0 ? '!' : ' ');
        sb.append((i2 & 8388608) != 0 ? 'C' : 'c');
        sb.append((i4 & 8388608) != 0 ? '!' : ' ');
        sb.append((i2 & 33554432) == 0 ? 's' : 'S');
        sb.append((i4 & 33554432) != 0 ? '!' : ' ');
        sb.append("> disable2<");
        sb.append((iAdjustDisableFlags & 1) != 0 ? 'Q' : 'q');
        int i11 = i5 & 1;
        sb.append(i11 != 0 ? '!' : ' ');
        sb.append((iAdjustDisableFlags & 2) == 0 ? 'i' : 'I');
        sb.append((i5 & 2) != 0 ? '!' : ' ');
        sb.append((iAdjustDisableFlags & 4) != 0 ? 'N' : 'n');
        int i12 = i5 & 4;
        sb.append(i12 != 0 ? '!' : ' ');
        sb.append('>');
        Log.d("StatusBar", sb.toString());
        if (i7 != 0 && i6 != 0) {
            this.mShadeController.animateCollapsePanels();
        }
        if (i10 != 0 && i9 != 0) {
            this.mHandler.removeMessages(1020);
            this.mHandler.sendEmptyMessage(1020);
        }
        if (i8 != 0 && areNotificationAlertsDisabled()) {
            this.mHeadsUpManager.releaseAllImmediately();
        }
        if (i11 != 0) {
            updateQsExpansionEnabled();
        }
        if (i12 != 0) {
            updateQsExpansionEnabled();
            if ((i2 & 4) != 0) {
                this.mShadeController.animateCollapsePanels();
            }
        }
    }

    boolean areNotificationAlertsDisabled() {
        return (this.mDisabled1 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0;
    }

    protected H createHandler() {
        return new H();
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, boolean z2, int i) {
        startActivityDismissingKeyguard(intent, z, z2, i);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, false, z);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        startActivityDismissingKeyguard(intent, false, z, false, callback, 0);
    }

    public void setQsExpanded(boolean z) {
        this.mNotificationShadeWindowController.setQsExpanded(z);
        this.mNotificationPanelViewController.setStatusAccessibilityImportance(z ? 4 : 0);
        if (getNavigationBarView() != null) {
            getNavigationBarView().onStatusBarPanelStateChanged();
        }
    }

    public boolean isWakeUpComingFromTouch() {
        return this.mWakeUpComingFromTouch;
    }

    public boolean isFalsingThresholdNeeded() {
        return this.mStatusBarStateController.getState() == 1;
    }

    public void onKeyguardViewManagerStatesUpdated() {
        logStateToEventlog();
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() {
        updateKeyguardState();
        logStateToEventlog();
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpPinnedModeChanged(boolean z) {
        if (z) {
            this.mNotificationShadeWindowController.setHeadsUpShowing(true);
            this.mStatusBarWindowController.setForceStatusBarVisible(true);
            if (this.mNotificationPanelViewController.isFullyCollapsed()) {
                this.mNotificationPanelViewController.getView().requestLayout();
                this.mNotificationShadeWindowController.setForceWindowCollapsed(true);
                this.mNotificationPanelViewController.getView().post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda18
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onHeadsUpPinnedModeChanged$10();
                    }
                });
                return;
            }
            return;
        }
        boolean z2 = this.mKeyguardBypassController.getBypassEnabled() && this.mState == 1;
        if (!this.mNotificationPanelViewController.isFullyCollapsed() || this.mNotificationPanelViewController.isTracking() || z2) {
            this.mNotificationShadeWindowController.setHeadsUpShowing(false);
            if (z2) {
                this.mStatusBarWindowController.setForceStatusBarVisible(false);
                return;
            }
            return;
        }
        this.mHeadsUpManager.setHeadsUpGoingAway(true);
        this.mNotificationPanelViewController.runAfterAnimationFinished(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onHeadsUpPinnedModeChanged$11();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onHeadsUpPinnedModeChanged$10() {
        this.mNotificationShadeWindowController.setForceWindowCollapsed(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onHeadsUpPinnedModeChanged$11() {
        if (!this.mHeadsUpManager.hasPinnedHeadsUp()) {
            this.mNotificationShadeWindowController.setHeadsUpShowing(false);
            this.mHeadsUpManager.setHeadsUpGoingAway(false);
        }
        this.mRemoteInputManager.onPanelCollapsed();
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        this.mNotificationsController.requestNotificationUpdate("onHeadsUpStateChanged");
        if (this.mStatusBarStateController.isDozing() && z) {
            notificationEntry.setPulseSuppressed(false);
            this.mDozeServiceHost.fireNotificationPulse(notificationEntry);
            if (this.mDozeServiceHost.isPulsing()) {
                this.mDozeScrimController.cancelPendingPulseTimeout();
            }
        }
        if (z || this.mHeadsUpManager.hasNotifications()) {
            return;
        }
        this.mDozeScrimController.pulseOutNow();
    }

    public void setPanelExpanded(boolean z) {
        if (this.mPanelExpanded != z) {
            this.mNotificationLogger.onPanelExpandedChanged(z);
        }
        this.mPanelExpanded = z;
        updateHideIconsForBouncer(false);
        this.mNotificationShadeWindowController.setPanelExpanded(z);
        this.mVisualStabilityManager.setPanelExpanded(z);
        if (z && this.mStatusBarStateController.getState() != 1) {
            clearNotificationEffects();
        }
        if (z && this.mFodVisibility) {
            this.mFODCircleViewImpl.hideInDisplayFingerprintView();
        }
        if (z) {
            return;
        }
        this.mRemoteInputManager.onPanelCollapsed();
    }

    public ViewGroup getNotificationScrollLayout() {
        return this.mStackScroller;
    }

    public boolean isPulsing() {
        return this.mDozeServiceHost.isPulsing();
    }

    public boolean hideStatusBarIconsWhenExpanded() {
        return this.mNotificationPanelViewController.hideStatusBarIconsWhenExpanded();
    }

    public void onColorsChanged(ColorExtractor colorExtractor, int i) {
        updateTheme();
    }

    public View getAmbientIndicationContainer() {
        return this.mAmbientIndicationContainer;
    }

    public boolean isOccluded() {
        return this.mIsOccluded;
    }

    public void setOccluded(boolean z) {
        this.mIsOccluded = z;
        this.mScrimController.setKeyguardOccluded(z);
        updateHideIconsForBouncer(false);
    }

    public boolean hideStatusBarIconsForBouncer() {
        return this.mHideIconsForBouncer || this.mWereIconsJustHidden;
    }

    private void updateHideIconsForBouncer(boolean z) {
        boolean z2 = (this.mTopHidesStatusBar && this.mIsOccluded && (this.mStatusBarWindowHidden || this.mBouncerShowing)) || (!this.mPanelExpanded && !this.mIsOccluded && this.mBouncerShowing);
        if (this.mHideIconsForBouncer != z2) {
            this.mHideIconsForBouncer = z2;
            if (!z2 && this.mBouncerWasShowingWhenHidden) {
                this.mWereIconsJustHidden = true;
                this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda22
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$updateHideIconsForBouncer$12();
                    }
                }, 500L);
            } else {
                this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, z);
            }
        }
        if (z2) {
            this.mBouncerWasShowingWhenHidden = this.mBouncerShowing;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateHideIconsForBouncer$12() {
        this.mWereIconsJustHidden = false;
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
    }

    public boolean headsUpShouldBeVisible() {
        return this.mHeadsUpAppearanceController.shouldBeVisible();
    }

    @Override // com.android.systemui.statusbar.notification.ActivityLaunchAnimator.Callback
    public void onLaunchAnimationCancelled() {
        if (this.mPresenter.isCollapsing()) {
            return;
        }
        onClosingFinished();
    }

    @Override // com.android.systemui.statusbar.notification.ActivityLaunchAnimator.Callback
    public void onExpandAnimationFinished(boolean z) {
        if (!this.mPresenter.isCollapsing()) {
            onClosingFinished();
        }
        if (z) {
            instantCollapseNotificationPanel();
        }
    }

    @Override // com.android.systemui.statusbar.notification.ActivityLaunchAnimator.Callback
    public void onExpandAnimationTimedOut() {
        ActivityLaunchAnimator activityLaunchAnimator;
        if (this.mPresenter.isPresenterFullyCollapsed() && !this.mPresenter.isCollapsing() && (activityLaunchAnimator = this.mActivityLaunchAnimator) != null && !activityLaunchAnimator.isLaunchForActivity()) {
            onClosingFinished();
        } else {
            this.mShadeController.collapsePanel(true);
        }
    }

    @Override // com.android.systemui.statusbar.notification.ActivityLaunchAnimator.Callback
    public boolean areLaunchAnimationsEnabled() {
        return this.mState == 0;
    }

    public boolean isDeviceInVrMode() {
        return this.mPresenter.isDeviceInVrMode();
    }

    public NotificationPresenter getPresenter() {
        return this.mPresenter;
    }

    @VisibleForTesting
    void setBarStateForTest(int i) {
        this.mState = i;
    }

    @VisibleForTesting
    void setUserSetupForTest(boolean z) {
        this.mUserSetup = z;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setBlockedGesturalNavigation(boolean z) {
        if (getNavigationBarView() != null) {
            getNavigationBarView().setBlockedGesturalNavigation(z);
        }
    }

    private class CustomSettingsObserver extends ContentObserver {
        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ((SystemUI) StatusBar.this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("switch_style"), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (uri.equals(Settings.System.getUriFor("switch_style"))) {
                StatusBar.this.stockSwitchStyle();
                StatusBar.this.updateSwitchStyle();
            }
        }
    }

    public void updateSwitchStyle() {
        ThemesUtils.updateSwitchStyle(this.mIOverlayManager, this.mLockscreenUserManager.getCurrentUserId(), Settings.System.getIntForUser(this.mContext.getContentResolver(), "switch_style", 0, this.mLockscreenUserManager.getCurrentUserId()));
    }

    public void stockSwitchStyle() {
        ThemesUtils.stockSwitchStyle(this.mIOverlayManager, this.mLockscreenUserManager.getCurrentUserId());
    }

    protected class H extends Handler {
        protected H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1026) {
                StatusBar.this.toggleKeyboardShortcuts(message.arg1);
            }
            if (i == 1027) {
                StatusBar.this.dismissKeyboardShortcuts();
                return;
            }
            switch (i) {
                case 1000:
                    StatusBar.this.animateExpandNotificationsPanel();
                    break;
                case 1001:
                    StatusBar.this.mShadeController.animateCollapsePanels();
                    break;
                case 1002:
                    StatusBar.this.animateExpandSettingsPanel((String) message.obj);
                    break;
                case 1003:
                    StatusBar.this.onLaunchTransitionTimeout();
                    break;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void maybeEscalateHeadsUp() {
        this.mHeadsUpManager.getAllEntries().forEach(new Consumer() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda40
            @Override // java.util.function.Consumer
            public final void accept(Object obj) throws PendingIntent.CanceledException {
                this.f$0.lambda$maybeEscalateHeadsUp$13((NotificationEntry) obj);
            }
        });
        this.mHeadsUpManager.releaseAllImmediately();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$maybeEscalateHeadsUp$13(NotificationEntry notificationEntry) throws PendingIntent.CanceledException {
        StatusBarNotification sbn = notificationEntry.getSbn();
        Notification notification = sbn.getNotification();
        if (notification.fullScreenIntent != null) {
            try {
                EventLog.writeEvent(36003, sbn.getKey());
                wakeUpForFullScreenIntent();
                notification.fullScreenIntent.send();
                notificationEntry.notifyFullScreenIntentLaunched();
            } catch (PendingIntent.CanceledException unused) {
            }
        }
    }

    void wakeUpForFullScreenIntent() {
        if (isGoingToSleep() || this.mDozing) {
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 2, "com.android.systemui:full_screen_intent");
            this.mWakeUpComingFromTouch = false;
            this.mWakeUpTouchLocation = null;
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void handleSystemKey(int i) {
        if (this.mCommandQueue.panelsEnabled() && this.mKeyguardUpdateMonitor.isDeviceInteractive()) {
            if ((!this.mKeyguardStateController.isShowing() || this.mKeyguardStateController.isOccluded()) && this.mUserSetup) {
                if (280 == i) {
                    this.mMetricsLogger.action(493);
                    this.mNotificationPanelViewController.collapse(false, 1.0f);
                    return;
                }
                if (281 == i) {
                    this.mMetricsLogger.action(494);
                    if (this.mNotificationPanelViewController.isFullyCollapsed()) {
                        if (this.mVibrateOnOpening) {
                            this.mVibratorHelper.vibrate(2);
                        }
                        this.mNotificationPanelViewController.expand(true);
                        ((NotificationListContainer) this.mStackScroller).setWillExpand(true);
                        this.mHeadsUpManager.unpinAll(true);
                        this.mMetricsLogger.count("panel_open", 1);
                        return;
                    }
                    if (this.mNotificationPanelViewController.isInSettings() || this.mNotificationPanelViewController.isExpanding()) {
                        return;
                    }
                    this.mNotificationPanelViewController.flingSettings(0.0f, 0);
                    this.mMetricsLogger.count("panel_open_qs", 1);
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showPinningEnterExitToast(boolean z) {
        if (getNavigationBarView() != null) {
            getNavigationBarView().showPinningEnterExitToast(z);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showPinningEscapeToast() {
        if (getNavigationBarView() != null) {
            getNavigationBarView().showPinningEscapeToast();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void toggleCameraFlash() {
        FlashlightController flashlightController = this.mFlashlightController;
        if (flashlightController != null) {
            flashlightController.initFlashLight();
            if (this.mFlashlightController.hasFlashlight() && this.mFlashlightController.isAvailable()) {
                this.mFlashlightController.setFlashlight(!r1.isEnabled());
            }
        }
    }

    void makeExpandedVisible(boolean z) {
        if (z || (!this.mExpandedVisible && this.mCommandQueue.panelsEnabled())) {
            this.mExpandedVisible = true;
            this.mNotificationShadeWindowController.setPanelVisible(true);
            visibilityChanged(true);
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, !z);
            setInteracting(1, true);
        }
    }

    public void postAnimateCollapsePanels() {
        H h = this.mHandler;
        final ShadeController shadeController = this.mShadeController;
        Objects.requireNonNull(shadeController);
        h.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() {
                shadeController.animateCollapsePanels();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$postAnimateForceCollapsePanels$14() {
        this.mShadeController.animateCollapsePanels(0, true);
    }

    public void postAnimateForceCollapsePanels() {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda21
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$postAnimateForceCollapsePanels$14();
            }
        });
    }

    public void postAnimateOpenPanels() {
        this.mHandler.sendEmptyMessage(1002);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void togglePanel() {
        if (this.mPanelExpanded) {
            this.mShadeController.animateCollapsePanels();
        } else {
            animateExpandNotificationsPanel();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateCollapsePanels(int i, boolean z) {
        this.mShadeController.animateCollapsePanels(i, z, false, 1.0f);
    }

    void postHideRecentApps() {
        if (this.mHandler.hasMessages(1020)) {
            return;
        }
        this.mHandler.removeMessages(1020);
        this.mHandler.sendEmptyMessage(1020);
    }

    public void onInputFocusTransfer(boolean z, boolean z2, float f) {
        if (this.mCommandQueue.panelsEnabled()) {
            if (z) {
                this.mNotificationPanelViewController.startWaitingForOpenPanelGesture();
            } else {
                this.mNotificationPanelViewController.stopWaitingForOpenPanelGesture(z2, f);
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandNotificationsPanel() {
        if (this.mCommandQueue.panelsEnabled()) {
            this.mNotificationPanelViewController.expandWithoutQs();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandSettingsPanel(String str) {
        if (this.mCommandQueue.panelsEnabled() && this.mUserSetup) {
            if (str != null) {
                this.mQSPanel.openDetails(str);
            }
            this.mNotificationPanelViewController.expandWithQs();
        }
    }

    public void animateCollapseQuickSettings() {
        if (this.mState == 0) {
            this.mStatusBarView.collapsePanel(true, false, 1.0f);
        }
    }

    void makeExpandedInvisible() {
        if (!this.mExpandedVisible || this.mNotificationShadeWindowView == null) {
            return;
        }
        this.mStatusBarView.collapsePanel(false, false, 1.0f);
        this.mNotificationPanelViewController.closeQs();
        this.mExpandedVisible = false;
        visibilityChanged(false);
        this.mNotificationShadeWindowController.setPanelVisible(false);
        this.mStatusBarWindowController.setForceStatusBarVisible(false);
        this.mGutsManager.closeAndSaveGuts(true, true, true, -1, -1, true);
        this.mShadeController.runPostCollapseRunnables();
        setInteracting(1, false);
        if (!this.mNotificationActivityStarter.isCollapsingToShowActivityOverLockscreen()) {
            showBouncerIfKeyguard();
        }
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, this.mNotificationPanelViewController.hideStatusBarIconsWhenExpanded());
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            return;
        }
        WindowManagerGlobal.getInstance().trimMemory(20);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void adjustBrightness(int i) {
        this.mBrightnessChanged = true;
        float fMin = (Math.min(0.85f, Math.max(0.15f, i / getDisplayWidth())) - 0.15f) / 0.7f;
        if (this.mAutomaticBrightness) {
            final float fMin2 = Math.min(Math.max((fMin * 2.0f) - 1.0f, -1.0f), 1.0f);
            this.mDisplayManager.setTemporaryAutoBrightnessAdjustment(fMin2);
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar.9
                @Override // java.lang.Runnable
                public void run() {
                    Settings.System.putFloatForUser(((SystemUI) StatusBar.this).mContext.getContentResolver(), "screen_auto_brightness_adj", fMin2, -2);
                }
            });
        } else {
            final float fConvertGammaToLinearFloat = BrightnessUtils.convertGammaToLinearFloat(Math.round(fMin * 65535.0f), this.mMinimumBacklight, this.mMaximumBacklight);
            this.mDisplayManager.setTemporaryBrightness(fConvertGammaToLinearFloat);
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar.10
                @Override // java.lang.Runnable
                public void run() {
                    Settings.System.putFloatForUser(((SystemUI) StatusBar.this).mContext.getContentResolver(), "screen_brightness_float", fConvertGammaToLinearFloat, -2);
                }
            });
        }
    }

    private void brightnessControl(MotionEvent motionEvent) throws Resources.NotFoundException {
        int action = motionEvent.getAction();
        int rawX = (int) motionEvent.getRawX();
        int rawY = (int) motionEvent.getRawY();
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.message_progress_dialog_letter_spacing);
        this.mQuickQsTotalHeight = dimensionPixelSize;
        if (action == 0) {
            if (rawY < dimensionPixelSize) {
                this.mLinger = 0;
                this.mInitialTouchX = rawX;
                this.mInitialTouchY = rawY;
                this.mJustPeeked = true;
                this.mHandler.removeCallbacks(this.mLongPressBrightnessChange);
                this.mHandler.postDelayed(this.mLongPressBrightnessChange, 750L);
                return;
            }
            return;
        }
        if (action != 2) {
            if (action == 1 || action == 3) {
                this.mHandler.removeCallbacks(this.mLongPressBrightnessChange);
                return;
            }
            return;
        }
        if (rawY < dimensionPixelSize && this.mJustPeeked) {
            if (this.mLinger > 20) {
                adjustBrightness(rawX);
                return;
            }
            int iAbs = Math.abs(rawX - this.mInitialTouchX);
            int iAbs2 = Math.abs(rawY - this.mInitialTouchY);
            int scaledTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
            if (iAbs > iAbs2) {
                this.mLinger++;
            }
            if (iAbs > scaledTouchSlop || iAbs2 > scaledTouchSlop) {
                this.mHandler.removeCallbacks(this.mLongPressBrightnessChange);
                return;
            }
            return;
        }
        if (rawY > dimensionPixelSize) {
            this.mJustPeeked = false;
        }
        this.mHandler.removeCallbacks(this.mLongPressBrightnessChange);
    }

    public void updateDismissAllVisibility(boolean z) {
        ImageButton imageButton = this.mDismissAllButton;
        if (imageButton == null) {
            return;
        }
        if (this.mShowDimissButton && this.mClearableNotifications && this.mState != 1 && z) {
            imageButton.setVisibility(0);
            int iRound = Math.round(this.mNotificationPanelViewController.getExpandedFraction() * 255.0f);
            this.mDismissAllButton.setAlpha(iRound);
            this.mDismissAllButton.getBackground().setAlpha(iRound);
            return;
        }
        imageButton.setAlpha(0);
        this.mDismissAllButton.getBackground().setAlpha(0);
        this.mDismissAllButton.setVisibility(8);
    }

    public void updateDismissAllButton(int i) {
        ImageButton imageButton = this.mDismissAllButton;
        if (imageButton != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageButton.getLayoutParams();
            layoutParams.width = this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.dismiss_all_button_width);
            layoutParams.height = this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.dismiss_all_button_height);
            layoutParams.bottomMargin = this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.dismiss_all_button_margin_bottom);
            this.mDismissAllButton.setElevation(this.mContext.getResources().getDimension(com.android.systemui.R.dimen.dismiss_all_button_elevation));
            this.mDismissAllButton.setColorFilter(i);
            this.mDismissAllButton.setBackground(this.mContext.getResources().getDrawable(com.android.systemui.R.drawable.dismiss_all_background));
        }
    }

    public void setHasClearableNotifs(boolean z) {
        this.mClearableNotifications = z;
    }

    public View getDismissAllButton() {
        return this.mDismissAllButton;
    }

    public boolean interceptTouchEvent(MotionEvent motionEvent) throws Resources.NotFoundException {
        if (this.mBrightnessControl) {
            brightnessControl(motionEvent);
            if ((this.mDisabled1 & 65536) != 0) {
                return true;
            }
        }
        boolean z = motionEvent.getAction() == 1 || motionEvent.getAction() == 3;
        if (this.mStatusBarWindowState == 0) {
            if (z && !this.mExpandedVisible) {
                setInteracting(1, false);
            } else {
                setInteracting(1, true);
            }
        }
        if (this.mBrightnessChanged && z) {
            this.mBrightnessChanged = false;
            if (this.mJustPeeked && this.mExpandedVisible) {
                this.mNotificationPanelViewController.fling(10.0f, false);
            }
        }
        return false;
    }

    boolean isSameStatusBarState(int i) {
        return this.mStatusBarWindowState == i;
    }

    public GestureRecorder getGestureRecorder() {
        return this.mGestureRec;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setWindowState(int i, int i2, int i3) {
        if (i != this.mDisplayId) {
            return;
        }
        boolean z = i3 == 0;
        if (this.mNotificationShadeWindowView != null && i2 == 1 && this.mStatusBarWindowState != i3) {
            this.mStatusBarWindowState = i3;
            if (!z && this.mState == 0) {
                this.mStatusBarView.collapsePanel(false, false, 1.0f);
            }
            if (this.mStatusBarView != null) {
                this.mStatusBarWindowHidden = i3 == 2;
                updateHideIconsForBouncer(false);
            }
        }
        updateBubblesVisibility();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        if (i != this.mDisplayId) {
            return;
        }
        boolean zUpdateBarMode = false;
        if (this.mAppearance != i2) {
            this.mAppearance = i2;
            zUpdateBarMode = updateBarMode(barMode(this.mTransientShown, i2));
        }
        this.mLightBarController.onStatusBarAppearanceChanged(appearanceRegionArr, zUpdateBarMode, this.mStatusBarMode, z);
        updateBubblesVisibility();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 0)) {
            showTransientUnchecked();
        }
    }

    private void showTransientUnchecked() {
        if (this.mTransientShown) {
            return;
        }
        this.mTransientShown = true;
        this.mNoAnimationOnNextBarModeChange = true;
        handleTransientChanged();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void abortTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 0)) {
            clearTransient();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearTransient() {
        if (this.mTransientShown) {
            this.mTransientShown = false;
            handleTransientChanged();
        }
    }

    private void handleTransientChanged() {
        int iBarMode = barMode(this.mTransientShown, this.mAppearance);
        if (updateBarMode(iBarMode)) {
            this.mLightBarController.onStatusBarModeChanged(iBarMode);
            updateBubblesVisibility();
        }
    }

    private boolean updateBarMode(int i) {
        if (this.mStatusBarMode == i) {
            return false;
        }
        this.mStatusBarMode = i;
        checkBarModes();
        this.mAutoHideController.touchAutoHide();
        return true;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void topAppWindowChanged(int i, boolean z, boolean z2) {
        if (i != this.mDisplayId) {
            return;
        }
        this.mAppFullscreen = z;
        this.mAppImmersive = z2;
        this.mStatusBarStateController.setFullscreenState(z, z2);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showWirelessChargingAnimation(int i) {
        showChargingAnimation(i, -1, 0L);
    }

    protected void showChargingAnimation(int i, int i2, long j) {
        if (this.mDozing || this.mKeyguardManager.isKeyguardLocked()) {
            WirelessChargingAnimation.makeWirelessChargingAnimation(this.mContext, null, i2, i, new WirelessChargingAnimation.Callback() { // from class: com.android.systemui.statusbar.phone.StatusBar.11
                @Override // com.android.systemui.charging.WirelessChargingAnimation.Callback
                public void onAnimationStarting() {
                    StatusBar.this.mNotificationShadeWindowController.setRequestTopUi(true, "StatusBar");
                    CrossFadeHelper.fadeOut(StatusBar.this.mNotificationPanelViewController.getView(), 1.0f);
                }

                @Override // com.android.systemui.charging.WirelessChargingAnimation.Callback
                public void onAnimationEnded() {
                    CrossFadeHelper.fadeIn(StatusBar.this.mNotificationPanelViewController.getView());
                    StatusBar.this.mNotificationShadeWindowController.setRequestTopUi(false, "StatusBar");
                }
            }, this.mDozing).show(j);
        } else {
            WirelessChargingAnimation.makeWirelessChargingAnimation(this.mContext, null, i2, i, new WirelessChargingAnimation.Callback() { // from class: com.android.systemui.statusbar.phone.StatusBar.12
                @Override // com.android.systemui.charging.WirelessChargingAnimation.Callback
                public void onAnimationStarting() {
                    StatusBar.this.mNotificationShadeWindowController.setRequestTopUi(true, "StatusBar");
                }

                @Override // com.android.systemui.charging.WirelessChargingAnimation.Callback
                public void onAnimationEnded() {
                    StatusBar.this.mNotificationShadeWindowController.setRequestTopUi(false, "StatusBar");
                }
            }, false).show(j);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onRecentsAnimationStateChanged(boolean z) {
        setInteracting(2, z);
        this.mFODCircleViewImpl.hideInDisplayFingerprintView();
    }

    void checkBarModes() {
        if (this.mDemoMode) {
            return;
        }
        NotificationShadeWindowViewController notificationShadeWindowViewController = this.mNotificationShadeWindowViewController;
        if (notificationShadeWindowViewController != null && notificationShadeWindowViewController.getBarTransitions() != null) {
            checkBarMode(this.mStatusBarMode, this.mStatusBarWindowState, this.mNotificationShadeWindowViewController.getBarTransitions());
        }
        this.mNavigationBarController.checkNavBarModes(this.mDisplayId);
        this.mNoAnimationOnNextBarModeChange = false;
    }

    void setQsScrimEnabled(boolean z) {
        NotificationPanelViewController notificationPanelViewController = this.mNotificationPanelViewController;
        if (notificationPanelViewController != null) {
            notificationPanelViewController.setQsScrimEnabled(z);
        }
    }

    private void updateBubblesVisibility() {
        BubbleController bubbleController = this.mBubbleController;
        int i = this.mStatusBarMode;
        bubbleController.onStatusBarVisibilityChanged((i == 3 || i == 6 || this.mStatusBarWindowHidden) ? false : true);
    }

    void checkBarMode(int i, int i2, BarTransitions barTransitions) {
        barTransitions.transitionTo(i, (this.mNoAnimationOnNextBarModeChange || !this.mDeviceInteractive || i2 == 2) ? false : true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishBarAnimations() {
        if (this.mNotificationShadeWindowController != null && this.mNotificationShadeWindowViewController.getBarTransitions() != null) {
            this.mNotificationShadeWindowViewController.getBarTransitions().finishAnimations();
        }
        this.mNavigationBarController.finishBarAnimations(this.mDisplayId);
    }

    public void setInteracting(int i, boolean z) {
        int i2 = this.mInteractingWindows;
        boolean z2 = ((i2 & i) != 0) != z;
        int i3 = z ? i2 | i : i2 & (~i);
        this.mInteractingWindows = i3;
        if (i3 != 0) {
            this.mAutoHideController.suspendAutoHide();
        } else {
            this.mAutoHideController.resumeSuspendedAutoHide();
        }
        if (z2 && z && i == 2) {
            this.mNavigationBarController.touchAutoDim(this.mDisplayId);
            dismissVolumeDialog();
        }
        checkBarModes();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissVolumeDialog() {
        VolumeComponent volumeComponent = this.mVolumeComponent;
        if (volumeComponent != null) {
            volumeComponent.dismissNow();
        }
    }

    public boolean inFullscreenMode() {
        return this.mAppFullscreen;
    }

    public boolean inImmersiveMode() {
        return this.mAppImmersive;
    }

    public static String viewInfo(View view) {
        return "[(" + view.getLeft() + "," + view.getTop() + ")(" + view.getRight() + "," + view.getBottom() + ") " + view.getWidth() + "x" + view.getHeight() + "]";
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        synchronized (this.mQueueLock) {
            printWriter.println("Current Status Bar state:");
            printWriter.println("  mExpandedVisible=" + this.mExpandedVisible);
            printWriter.println("  mDisplayMetrics=" + this.mDisplayMetrics);
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller));
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller) + " scroll " + this.mStackScroller.getScrollX() + "," + this.mStackScroller.getScrollY());
        }
        printWriter.print("  mInteractingWindows=");
        printWriter.println(this.mInteractingWindows);
        printWriter.print("  mStatusBarWindowState=");
        printWriter.println(StatusBarManager.windowStateToString(this.mStatusBarWindowState));
        printWriter.print("  mStatusBarMode=");
        printWriter.println(BarTransitions.modeToString(this.mStatusBarMode));
        printWriter.print("  mDozing=");
        printWriter.println(this.mDozing);
        printWriter.print("  mWallpaperSupported= ");
        printWriter.println(this.mWallpaperSupported);
        printWriter.println("  StatusBarWindowView: ");
        NotificationShadeWindowViewController notificationShadeWindowViewController = this.mNotificationShadeWindowViewController;
        if (notificationShadeWindowViewController != null) {
            notificationShadeWindowViewController.dump(fileDescriptor, printWriter, strArr);
            dumpBarTransitions(printWriter, "PhoneStatusBarTransitions", this.mNotificationShadeWindowViewController.getBarTransitions());
        }
        printWriter.println("  mMediaManager: ");
        NotificationMediaManager notificationMediaManager = this.mMediaManager;
        if (notificationMediaManager != null) {
            notificationMediaManager.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  Panels: ");
        if (this.mNotificationPanelViewController != null) {
            printWriter.println("    mNotificationPanel=" + this.mNotificationPanelViewController.getView() + " params=" + this.mNotificationPanelViewController.getView().getLayoutParams().debug(""));
            printWriter.print("      ");
            this.mNotificationPanelViewController.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  mStackScroller: ");
        if (this.mStackScroller instanceof Dumpable) {
            printWriter.print("      ");
            ((Dumpable) this.mStackScroller).dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  Theme:");
        printWriter.println("    dark theme: " + (this.mUiModeManager == null ? "null" : this.mUiModeManager.getNightMode() + "") + " (auto: 0, yes: 2, no: 1)");
        printWriter.println("    light wallpaper theme: " + (this.mContext.getThemeResId() == com.android.systemui.R.style.Theme_SystemUI_Light));
        KeyguardIndicationController keyguardIndicationController = this.mKeyguardIndicationController;
        if (keyguardIndicationController != null) {
            keyguardIndicationController.dump(fileDescriptor, printWriter, strArr);
        }
        ScrimController scrimController = this.mScrimController;
        if (scrimController != null) {
            scrimController.dump(fileDescriptor, printWriter, strArr);
        }
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            statusBarKeyguardViewManager.dump(printWriter);
        }
        this.mNotificationsController.dump(fileDescriptor, printWriter, strArr, true);
        HeadsUpManagerPhone headsUpManagerPhone = this.mHeadsUpManager;
        if (headsUpManagerPhone != null) {
            headsUpManagerPhone.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mHeadsUpManager: null");
        }
        StatusBarTouchableRegionManager statusBarTouchableRegionManager = this.mStatusBarTouchableRegionManager;
        if (statusBarTouchableRegionManager != null) {
            statusBarTouchableRegionManager.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mStatusBarTouchableRegionManager: null");
        }
        LightBarController lightBarController = this.mLightBarController;
        if (lightBarController != null) {
            lightBarController.dump(fileDescriptor, printWriter, strArr);
        }
        this.mFalsingManager.dump(printWriter);
        FalsingLog.dump(printWriter);
        printWriter.println("SharedPreferences:");
        for (Map.Entry<String, ?> entry : Prefs.getAll(this.mContext).entrySet()) {
            printWriter.print("  ");
            printWriter.print(entry.getKey());
            printWriter.print("=");
            printWriter.println(entry.getValue());
        }
        FlashlightController flashlightController = this.mFlashlightController;
        if (flashlightController != null) {
            flashlightController.dump(fileDescriptor, printWriter, strArr);
        }
    }

    static void dumpBarTransitions(PrintWriter printWriter, String str, BarTransitions barTransitions) {
        printWriter.print("  ");
        printWriter.print(str);
        printWriter.print(".BarTransitions.mMode=");
        printWriter.println(BarTransitions.modeToString(barTransitions.getMode()));
    }

    public void createAndAddWindows(RegisterStatusBarResult registerStatusBarResult) throws Resources.NotFoundException {
        makeStatusBarView(registerStatusBarResult);
        this.mNotificationShadeWindowController.attach();
        this.mStatusBarWindowController.attach();
    }

    void updateDisplaySize() {
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        this.mDisplay.getSize(this.mCurrentDisplaySize);
    }

    float getDisplayDensity() {
        return this.mDisplayMetrics.density;
    }

    float getDisplayWidth() {
        return this.mDisplayMetrics.widthPixels;
    }

    float getDisplayHeight() {
        return this.mDisplayMetrics.heightPixels;
    }

    int getRotation() {
        return this.mDisplay.getRotation();
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2, int i) {
        startActivityDismissingKeyguard(intent, z, z2, false, null, i);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2, 0);
    }

    public void startActivityDismissingKeyguard(final Intent intent, boolean z, boolean z2, final boolean z3, final ActivityStarter.Callback callback, final int i) {
        if (!z || this.mDeviceProvisionedController.isDeviceProvisioned()) {
            executeRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda32
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$startActivityDismissingKeyguard$16(intent, i, z3, callback);
                }
            }, new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda10
                @Override // java.lang.Runnable
                public final void run() {
                    StatusBar.lambda$startActivityDismissingKeyguard$17(callback);
                }
            }, z2, this.mActivityIntentHelper.wouldLaunchResolverActivity(intent, this.mLockscreenUserManager.getCurrentUserId()), true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startActivityDismissingKeyguard$16(Intent intent, int i, boolean z, ActivityStarter.Callback callback) {
        int iStartActivityAsUser;
        this.mAssistManagerLazy.get().hideAssist();
        intent.setFlags(335544320);
        intent.addFlags(i);
        ActivityOptions activityOptions = new ActivityOptions(getActivityOptions(null));
        activityOptions.setDisallowEnterPictureInPictureWhileLaunching(z);
        if (intent == KeyguardBottomAreaView.INSECURE_CAMERA_INTENT) {
            activityOptions.setRotationAnimationHint(3);
        }
        if (intent.getAction() == "android.settings.panel.action.VOLUME") {
            activityOptions.setDisallowEnterPictureInPictureWhileLaunching(true);
        }
        try {
            iStartActivityAsUser = ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, this.mContext.getBasePackageName(), this.mContext.getAttributionTag(), intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, activityOptions.toBundle(), UserHandle.CURRENT.getIdentifier());
        } catch (RemoteException e) {
            Log.w("StatusBar", "Unable to start activity", e);
            iStartActivityAsUser = -96;
        }
        if (callback != null) {
            callback.onActivityStarted(iStartActivityAsUser);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$startActivityDismissingKeyguard$17(ActivityStarter.Callback callback) {
        if (callback != null) {
            callback.onActivityStarted(-96);
        }
    }

    public void readyForKeyguardDone() {
        this.mStatusBarKeyguardViewManager.readyForKeyguardDone();
    }

    public void executeRunnableDismissingKeyguard(final Runnable runnable, Runnable runnable2, final boolean z, boolean z2, final boolean z3) {
        dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda7
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return this.f$0.lambda$executeRunnableDismissingKeyguard$18(runnable, z, z3);
            }
        }, runnable2, z2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$executeRunnableDismissingKeyguard$18(Runnable runnable, boolean z, boolean z2) {
        if (runnable != null) {
            if (this.mStatusBarKeyguardViewManager.isShowing() && this.mStatusBarKeyguardViewManager.isOccluded()) {
                this.mStatusBarKeyguardViewManager.addAfterKeyguardGoneRunnable(runnable);
            } else {
                AsyncTask.execute(runnable);
            }
        }
        if (z) {
            if (this.mExpandedVisible && !this.mBouncerShowing) {
                this.mShadeController.animateCollapsePanels(2, true, true);
            } else {
                H h = this.mHandler;
                final ShadeController shadeController = this.mShadeController;
                Objects.requireNonNull(shadeController);
                h.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda13
                    @Override // java.lang.Runnable
                    public final void run() {
                        shadeController.runPostCollapseRunnables();
                    }
                });
            }
        } else if (isInLaunchTransition() && this.mNotificationPanelViewController.isLaunchTransitionFinished()) {
            H h2 = this.mHandler;
            final StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
            Objects.requireNonNull(statusBarKeyguardViewManager);
            h2.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda37
                @Override // java.lang.Runnable
                public final void run() {
                    statusBarKeyguardViewManager.readyForKeyguardDone();
                }
            });
        }
        return z2;
    }

    public void resetUserExpandedStates() {
        this.mNotificationsController.resetUserExpandedStates();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeWhenUnlocked(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
        if (this.mStatusBarKeyguardViewManager.isShowing() && z) {
            this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
        }
        dismissKeyguardThenExecute(onDismissAction, null, false);
    }

    protected void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
        dismissKeyguardThenExecute(onDismissAction, null, z);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        if (this.mWakefulnessLifecycle.getWakefulness() == 0 && this.mKeyguardStateController.canDismissLockScreen() && !this.mStatusBarStateController.leaveOpenOnKeyguardHide() && this.mDozeServiceHost.isPulsing()) {
            this.mBiometricUnlockController.startWakeAndUnlock(2);
        }
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            this.mStatusBarKeyguardViewManager.dismissWithAction(onDismissAction, runnable, z);
        } else {
            onDismissAction.onDismiss();
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        updateResources();
        updateDisplaySize();
        this.mPortrait = RotationUtils.getExactRotation(this.mContext) == 0;
        this.mViewHierarchyManager.updateRowStates();
        this.mScreenPinningRequest.onConfigurationChanged();
        if (this.mImmerseMode == 1) {
            setBlackStatusBar(this.mPortrait);
        }
    }

    public void setLockscreenUser(int i) {
        LockscreenWallpaper lockscreenWallpaper = this.mLockscreenWallpaper;
        if (lockscreenWallpaper != null) {
            lockscreenWallpaper.setCurrentUser(i);
        }
        this.mScrimController.setCurrentUser(i);
        if (this.mWallpaperSupported) {
            this.mWallpaperChangedReceiver.onReceive(this.mContext, null);
        }
    }

    void updateResources() {
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel != null) {
            qSPanel.updateResources();
        }
        StatusBarWindowController statusBarWindowController = this.mStatusBarWindowController;
        if (statusBarWindowController != null) {
            statusBarWindowController.refreshStatusBarHeight();
        }
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.updateResources();
        }
        NotificationPanelViewController notificationPanelViewController = this.mNotificationPanelViewController;
        if (notificationPanelViewController != null) {
            notificationPanelViewController.updateResources();
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.updateResources();
        }
    }

    protected void handleVisibleToUserChanged(boolean z) {
        if (z) {
            handleVisibleToUserChangedImpl(z);
            this.mNotificationLogger.startNotificationLogging();
        } else {
            this.mNotificationLogger.stopNotificationLogging();
            handleVisibleToUserChangedImpl(z);
        }
    }

    void handlePeekToExpandTransistion() {
        try {
            this.mBarService.onPanelRevealed(false, this.mNotificationsController.getActiveNotificationsCount());
        } catch (RemoteException unused) {
        }
    }

    void handleVisibleToUserChangedImpl(boolean z) {
        int i;
        if (z) {
            boolean zHasPinnedHeadsUp = this.mHeadsUpManager.hasPinnedHeadsUp();
            final boolean z2 = !this.mPresenter.isPresenterFullyCollapsed() && ((i = this.mState) == 0 || i == 2);
            final int activeNotificationsCount = (zHasPinnedHeadsUp && this.mPresenter.isPresenterFullyCollapsed()) ? 1 : this.mNotificationsController.getActiveNotificationsCount();
            this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda36
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleVisibleToUserChangedImpl$19(z2, activeNotificationsCount);
                }
            });
            return;
        }
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda25
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$handleVisibleToUserChangedImpl$20();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleVisibleToUserChangedImpl$19(boolean z, int i) {
        try {
            this.mBarService.onPanelRevealed(z, i);
        } catch (RemoteException unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleVisibleToUserChangedImpl$20() {
        try {
            this.mBarService.onPanelHidden();
        } catch (RemoteException unused) {
        }
    }

    private void logStateToEventlog() {
        boolean zIsShowing = this.mStatusBarKeyguardViewManager.isShowing();
        boolean zIsOccluded = this.mStatusBarKeyguardViewManager.isOccluded();
        boolean zIsBouncerShowing = this.mStatusBarKeyguardViewManager.isBouncerShowing();
        boolean zIsMethodSecure = this.mKeyguardStateController.isMethodSecure();
        boolean zCanDismissLockScreen = this.mKeyguardStateController.canDismissLockScreen();
        int loggingFingerprint = getLoggingFingerprint(this.mState, zIsShowing, zIsOccluded, zIsBouncerShowing, zIsMethodSecure, zCanDismissLockScreen);
        if (loggingFingerprint != this.mLastLoggedStateFingerprint) {
            if (this.mStatusBarStateLog == null) {
                this.mStatusBarStateLog = new LogMaker(0);
            }
            this.mMetricsLogger.write(this.mStatusBarStateLog.setCategory(zIsBouncerShowing ? 197 : 196).setType(zIsShowing ? 1 : 2).setSubtype(zIsMethodSecure ? 1 : 0));
            EventLogTags.writeSysuiStatusBarState(this.mState, zIsShowing ? 1 : 0, zIsOccluded ? 1 : 0, zIsBouncerShowing ? 1 : 0, zIsMethodSecure ? 1 : 0, zCanDismissLockScreen ? 1 : 0);
            this.mLastLoggedStateFingerprint = loggingFingerprint;
            StringBuilder sb = new StringBuilder();
            sb.append(zIsBouncerShowing ? "BOUNCER" : "LOCKSCREEN");
            sb.append(zIsShowing ? "_OPEN" : "_CLOSE");
            sb.append(zIsMethodSecure ? "_SECURE" : "_INSECURE");
            sUiEventLogger.log(StatusBarUiEvent.valueOf(sb.toString()));
        }
    }

    void vibrate() {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(250L, VIBRATION_ATTRIBUTES);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$21() {
        Debug.stopMethodTracing();
        Log.d("StatusBar", "stopTracing");
        vibrate();
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postQSRunnableDismissingKeyguard(final Runnable runnable) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda33
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$postQSRunnableDismissingKeyguard$23(runnable);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$postQSRunnableDismissingKeyguard$23(final Runnable runnable) {
        this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
        executeRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda35
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$postQSRunnableDismissingKeyguard$22(runnable);
            }
        }, null, false, false, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$postQSRunnableDismissingKeyguard$22(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postStartActivityDismissingKeyguard(final PendingIntent pendingIntent) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda29
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$postStartActivityDismissingKeyguard$24(pendingIntent);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postStartActivityDismissingKeyguard(final Intent intent, int i) {
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda31
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$postStartActivityDismissingKeyguard$25(intent);
            }
        }, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$postStartActivityDismissingKeyguard$25(Intent intent) {
        handleStartActivityDismissingKeyguard(intent, true);
    }

    private void handleStartActivityDismissingKeyguard(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, z, true);
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        VolumeComponent volumeComponent;
        int i = 0;
        if (!this.mDemoModeAllowed) {
            this.mDemoModeAllowed = Settings.Global.getInt(this.mContext.getContentResolver(), "sysui_demo_allowed", 0) != 0;
        }
        if (this.mDemoModeAllowed) {
            if (str.equals("enter")) {
                this.mDemoMode = true;
            } else if (str.equals("exit")) {
                this.mDemoMode = false;
                checkBarModes();
            } else if (!this.mDemoMode) {
                dispatchDemoCommand("enter", new Bundle());
            }
            boolean z = str.equals("enter") || str.equals("exit");
            if ((z || str.equals("volume")) && (volumeComponent = this.mVolumeComponent) != null) {
                volumeComponent.dispatchDemoCommand(str, bundle);
            }
            if (z || str.equals("clock")) {
                dispatchDemoCommandToView(str, bundle, com.android.systemui.R.id.clock);
            }
            if (z || str.equals("battery")) {
                this.mBatteryController.dispatchDemoCommand(str, bundle);
            }
            if (z || str.equals("status")) {
                ((StatusBarIconControllerImpl) this.mIconController).dispatchDemoCommand(str, bundle);
            }
            if (this.mNetworkController != null && (z || str.equals("network"))) {
                this.mNetworkController.dispatchDemoCommand(str, bundle);
            }
            if (z || str.equals("notifications")) {
                PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
                View viewFindViewById = phoneStatusBarView == null ? null : phoneStatusBarView.findViewById(com.android.systemui.R.id.notification_icon_area);
                if (viewFindViewById != null) {
                    viewFindViewById.setVisibility((this.mDemoMode && "false".equals(bundle.getString("visible"))) ? 4 : 0);
                }
            }
            if (str.equals("bars")) {
                String string = bundle.getString("mode");
                if ("opaque".equals(string)) {
                    i = 4;
                } else if ("translucent".equals(string)) {
                    i = 2;
                } else if ("semi-transparent".equals(string)) {
                    i = 1;
                } else if (!"transparent".equals(string)) {
                    i = "warning".equals(string) ? 5 : -1;
                }
                if (i != -1) {
                    if (this.mNotificationShadeWindowController != null && this.mNotificationShadeWindowViewController.getBarTransitions() != null) {
                        this.mNotificationShadeWindowViewController.getBarTransitions().transitionTo(i, true);
                    }
                    this.mNavigationBarController.transitionTo(this.mDisplayId, i, true);
                }
            }
            if (z || str.equals("operator")) {
                dispatchDemoCommandToView(str, bundle, com.android.systemui.R.id.operator_name);
            }
        }
    }

    private void dispatchDemoCommandToView(String str, Bundle bundle, int i) {
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView == null) {
            return;
        }
        KeyEvent.Callback callbackFindViewById = phoneStatusBarView.findViewById(i);
        if (callbackFindViewById instanceof DemoMode) {
            ((DemoMode) callbackFindViewById).dispatchDemoCommand(str, bundle);
        }
    }

    public void showKeyguard() {
        this.mStatusBarStateController.setKeyguardRequested(true);
        this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(false);
        this.mPendingRemoteInputView = null;
        updateIsKeyguard();
        this.mAssistManagerLazy.get().onLockscreenShown();
    }

    public boolean hideKeyguard() {
        this.mStatusBarStateController.setKeyguardRequested(false);
        return updateIsKeyguard();
    }

    public boolean isFullScreenUserSwitcherState() {
        return this.mState == 3;
    }

    boolean updateIsKeyguard() {
        return updateIsKeyguard(false);
    }

    boolean updateIsKeyguard(boolean z) {
        boolean z2 = this.mBiometricUnlockController.getMode() == 1;
        boolean z3 = this.mDozeServiceHost.getDozingRequested() && (!this.mDeviceInteractive || (isGoingToSleep() && (isScreenFullyOff() || this.mIsKeyguard)));
        boolean z4 = (this.mStatusBarStateController.isKeyguardRequested() || z3) && !z2;
        if (z3) {
            updatePanelExpansionForKeyguard();
        }
        if (z4) {
            if (!isGoingToSleep() || this.mScreenLifecycle.getScreenState() != 3) {
                showKeyguardImpl();
            }
            return false;
        }
        return hideKeyguardImpl(z);
    }

    public void showKeyguardImpl() {
        this.mIsKeyguard = true;
        if (this.mKeyguardStateController.isLaunchTransitionFadingAway()) {
            this.mNotificationPanelViewController.cancelAnimation();
            onLaunchTransitionFadingEnded();
        }
        this.mHandler.removeMessages(1003);
        UserSwitcherController userSwitcherController = this.mUserSwitcherController;
        if (userSwitcherController != null && userSwitcherController.useFullscreenUserSwitcher()) {
            this.mStatusBarStateController.setState(3);
        } else if (!this.mPulseExpansionHandler.isWakingToShadeLocked()) {
            this.mStatusBarStateController.setState(1);
        }
        updatePanelExpansionForKeyguard();
        NotificationEntry notificationEntry = this.mDraggedDownEntry;
        if (notificationEntry != null) {
            notificationEntry.setUserLocked(false);
            this.mDraggedDownEntry.notifyHeightChanged(false);
            this.mDraggedDownEntry = null;
        }
    }

    private void updatePanelExpansionForKeyguard() {
        if (this.mState == 1 && this.mBiometricUnlockController.getMode() != 1) {
            this.mShadeController.instantExpandNotificationsPanel();
        } else if (this.mState == 3) {
            instantCollapseNotificationPanel();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLaunchTransitionFadingEnded() {
        this.mNotificationPanelViewController.setAlpha(1.0f);
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        runLaunchTransitionEndRunnable();
        this.mKeyguardStateController.setLaunchTransitionFadingAway(false);
        this.mPresenter.updateMediaMetaData(true, true);
    }

    public boolean isInLaunchTransition() {
        return this.mNotificationPanelViewController.isLaunchTransitionRunning() || this.mNotificationPanelViewController.isLaunchTransitionFinished();
    }

    public void fadeKeyguardAfterLaunchTransition(final Runnable runnable, Runnable runnable2) {
        this.mHandler.removeMessages(1003);
        this.mLaunchTransitionEndRunnable = runnable2;
        Runnable runnable3 = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda34
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$fadeKeyguardAfterLaunchTransition$26(runnable);
            }
        };
        if (this.mNotificationPanelViewController.isLaunchTransitionRunning()) {
            this.mNotificationPanelViewController.setLaunchTransitionEndRunnable(runnable3);
        } else {
            runnable3.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fadeKeyguardAfterLaunchTransition$26(Runnable runnable) {
        this.mKeyguardStateController.setLaunchTransitionFadingAway(true);
        if (runnable != null) {
            runnable.run();
        }
        updateScrimController();
        this.mPresenter.updateMediaMetaData(false, true);
        this.mNotificationPanelViewController.setAlpha(1.0f);
        this.mNotificationPanelViewController.fadeOut(10L, 10L, new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.onLaunchTransitionFadingEnded();
            }
        });
        this.mCommandQueue.appTransitionStarting(this.mDisplayId, SystemClock.uptimeMillis(), 120L, true);
    }

    public void fadeKeyguardWhilePulsing() {
        this.mNotificationPanelViewController.fadeOut(0L, 96L, new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda24
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$fadeKeyguardWhilePulsing$27();
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fadeKeyguardWhilePulsing$27() {
        hideKeyguard();
        this.mStatusBarKeyguardViewManager.onKeyguardFadedAway();
    }

    public void animateKeyguardUnoccluding() {
        this.mNotificationPanelViewController.setExpandedFraction(0.0f);
        animateExpandNotificationsPanel();
    }

    public void startLaunchTransitionTimeout() {
        this.mHandler.sendEmptyMessageDelayed(1003, 4000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLaunchTransitionTimeout() {
        Log.w("StatusBar", "Launch transition: Timeout!");
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mNotificationPanelViewController.resetViews(false);
    }

    private void runLaunchTransitionEndRunnable() {
        Runnable runnable = this.mLaunchTransitionEndRunnable;
        if (runnable != null) {
            this.mLaunchTransitionEndRunnable = null;
            runnable.run();
        }
    }

    public boolean hideKeyguardImpl(boolean z) {
        this.mIsKeyguard = false;
        Trace.beginSection("StatusBar#hideKeyguard");
        boolean zLeaveOpenOnKeyguardHide = this.mStatusBarStateController.leaveOpenOnKeyguardHide();
        if (!this.mStatusBarStateController.setState(0, z)) {
            this.mLockscreenUserManager.updatePublicMode();
        }
        if (this.mStatusBarStateController.leaveOpenOnKeyguardHide()) {
            if (!this.mStatusBarStateController.isKeyguardRequested()) {
                this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(false);
            }
            long jCalculateGoingToFullShadeDelay = this.mKeyguardStateController.calculateGoingToFullShadeDelay();
            this.mNotificationPanelViewController.animateToFullShade(jCalculateGoingToFullShadeDelay);
            NotificationEntry notificationEntry = this.mDraggedDownEntry;
            if (notificationEntry != null) {
                notificationEntry.setUserLocked(false);
                this.mDraggedDownEntry = null;
            }
            this.mNavigationBarController.disableAnimationsDuringHide(this.mDisplayId, jCalculateGoingToFullShadeDelay);
        } else if (!this.mNotificationPanelViewController.isCollapsing()) {
            instantCollapseNotificationPanel();
        }
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel != null) {
            qSPanel.refreshAllTiles();
        }
        this.mHandler.removeMessages(1003);
        releaseGestureWakeLock();
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
        this.mNotificationPanelViewController.cancelAnimation();
        this.mNotificationPanelViewController.setAlpha(1.0f);
        this.mNotificationPanelViewController.resetViewGroupFade();
        updateScrimController();
        Trace.endSection();
        return zLeaveOpenOnKeyguardHide;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void releaseGestureWakeLock() {
        if (this.mGestureWakeLock.isHeld()) {
            this.mGestureWakeLock.release();
        }
    }

    public void keyguardGoingAway() {
        this.mKeyguardStateController.notifyKeyguardGoingAway(true);
        this.mCommandQueue.appTransitionPending(this.mDisplayId, true);
        ((PulseController) Dependency.get(PulseController.class)).notifyKeyguardGoingAway();
    }

    public void setKeyguardFadingAway(long j, long j2, long j3, boolean z) {
        this.mCommandQueue.appTransitionStarting(this.mDisplayId, (j + j3) - 120, 120L, true);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, j3 > 0);
        this.mCommandQueue.appTransitionStarting(this.mDisplayId, j - 120, 120L, true);
        this.mKeyguardStateController.notifyKeyguardFadingAway(j2, j3, z);
    }

    public void finishKeyguardFadingAway() {
        this.mKeyguardStateController.notifyKeyguardDoneFading();
        this.mScrimController.setExpansionAffectsAlpha(true);
    }

    public boolean isCurrentRoundedSameAsFw() {
        int dimension = (int) (((int) this.mContext.getResources().getDimension(R.dimen.navigation_bar_width)) / Resources.getSystem().getDisplayMetrics().density);
        return dimension == Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "sysui_rounded_size", dimension, -2);
    }

    private void updateCorners() {
        if (!this.mSysuiRoundedFwvals || isCurrentRoundedSameAsFw()) {
            return;
        }
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "sysui_rounded_size", (int) (((int) this.mContext.getResources().getDimension(R.dimen.navigation_bar_width)) / Resources.getSystem().getDisplayMetrics().density), -2);
    }

    protected void updateTheme() {
        int i = this.mColorExtractor.getNeutralColors().supportsDarkText() ? com.android.systemui.R.style.Theme_SystemUI_Light : com.android.systemui.R.style.Theme_SystemUI;
        if (this.mContext.getThemeResId() != i) {
            this.mContext.setTheme(i);
            this.mConfigurationController.notifyThemeChanged();
        }
        updateCorners();
    }

    private void updateNavbarStyle() {
        this.mUiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda27
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateNavbarStyle$28();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateNavbarStyle$28() {
        ThemeAccentUtils.setNavbarStyle(this.mOverlayManager, this.mNavbarStyle);
    }

    private void updateRoundedStyle() {
        this.mUiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda26
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateRoundedStyle$29();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateRoundedStyle$29() {
        ThemeAccentUtils.setRoundedStyle(this.mOverlayManager, this.mRoundedStyle);
    }

    private void updateSignalStyle() {
        this.mUiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateSignalStyle$30();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateSignalStyle$30() {
        ThemeAccentUtils.setSignalStyle(this.mOverlayManager, this.mSignalStyle);
    }

    private void updateWifiStyle() {
        this.mUiOffloadThread.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda23
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateWifiStyle$31();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateWifiStyle$31() {
        ThemeAccentUtils.setWifiStyle(this.mOverlayManager, this.mWifiStyle);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void updateDozingState() {
        Trace.traceCounter(4096L, "dozing", this.mDozing ? 1 : 0);
        Trace.beginSection("StatusBar#updateDozingState");
        boolean z = false;
        byte b = this.mStatusBarKeyguardViewManager.isShowing() && !this.mStatusBarKeyguardViewManager.isOccluded();
        byte b2 = this.mBiometricUnlockController.getMode() == 1;
        if ((!this.mDozing && this.mDozeServiceHost.shouldAnimateWakeup() && b2 == false) || (this.mDozing && this.mDozeServiceHost.shouldAnimateScreenOff() && b != false)) {
            z = true;
        }
        this.mNotificationPanelViewController.setDozing(this.mDozing, z, this.mWakeUpTouchLocation);
        ((PulseController) Dependency.get(PulseController.class)).setDozing(this.mDozing);
        updateQsExpansionEnabled();
        Trace.endSection();
    }

    public void userActivity() {
        if (this.mState == 1) {
            this.mKeyguardViewMediatorCallback.userActivity();
        }
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        return this.mState == 1 && this.mStatusBarKeyguardViewManager.interceptMediaKey(keyEvent);
    }

    protected boolean shouldUnlockOnMenuPressed() {
        return this.mDeviceInteractive && this.mState != 0 && this.mStatusBarKeyguardViewManager.shouldDismissOnMenuPressed();
    }

    public boolean onMenuPressed() {
        if (!shouldUnlockOnMenuPressed()) {
            return false;
        }
        this.mShadeController.animateCollapsePanels(2, true);
        return true;
    }

    public void endAffordanceLaunch() {
        releaseGestureWakeLock();
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
    }

    public boolean onBackPressed() {
        boolean z = this.mScrimController.getState() == ScrimState.BOUNCER_SCRIMMED;
        if (this.mStatusBarKeyguardViewManager.onBackPressed(z)) {
            if (!z) {
                this.mNotificationPanelViewController.expandWithoutQs();
            }
            return true;
        }
        if (this.mNotificationPanelViewController.isQsExpanded()) {
            if (this.mNotificationPanelViewController.isQsDetailShowing()) {
                this.mNotificationPanelViewController.closeQsDetail();
            } else {
                this.mNotificationPanelViewController.animateCloseQs(false);
            }
            return true;
        }
        int i = this.mState;
        if (i != 1 && i != 2) {
            if (this.mNotificationPanelViewController.canPanelBeCollapsed()) {
                this.mShadeController.animateCollapsePanels();
            } else {
                this.mBubbleController.performBackPressIfNeeded();
            }
            return true;
        }
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        return keyguardUserSwitcher != null && keyguardUserSwitcher.hideIfNotSimple(true);
    }

    public boolean onSpacePressed() {
        if (!this.mDeviceInteractive || this.mState == 0) {
            return false;
        }
        this.mShadeController.animateCollapsePanels(2, true);
        return true;
    }

    private void showBouncerIfKeyguard() {
        int i = this.mState;
        if ((i == 1 || i == 2) && !this.mKeyguardViewMediator.isHiding()) {
            this.mStatusBarKeyguardViewManager.showBouncer(true);
        }
    }

    void instantCollapseNotificationPanel() {
        this.mNotificationPanelViewController.instantCollapse();
        this.mShadeController.runPostCollapseRunnables();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStatePreChange(int i, int i2) {
        if (this.mVisible && (i2 == 2 || this.mStatusBarStateController.goingToFullShade())) {
            clearNotificationEffects();
        }
        if (i2 == 1) {
            this.mRemoteInputManager.onPanelCollapsed();
            maybeEscalateHeadsUp();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        if (this.mState != i) {
            updateDismissAllVisibility(true);
        }
        this.mState = i;
        updateReportRejectedTouchVisibility();
        this.mDozeServiceHost.updateDozing();
        updateTheme();
        this.mNavigationBarController.touchAutoDim(this.mDisplayId);
        Trace.beginSection("StatusBar#updateKeyguardState");
        if (this.mState == 1) {
            this.mKeyguardIndicationController.setVisible(true);
            KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher != null) {
                keyguardUserSwitcher.setKeyguard(true, this.mStatusBarStateController.fromShadeLocked());
            }
            PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
            if (phoneStatusBarView != null) {
                phoneStatusBarView.removePendingHideExpandedRunnables();
            }
            View view = this.mAmbientIndicationContainer;
            if (view != null) {
                view.setVisibility(0);
            }
        } else {
            this.mKeyguardIndicationController.setVisible(false);
            KeyguardUserSwitcher keyguardUserSwitcher2 = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher2 != null) {
                keyguardUserSwitcher2.setKeyguard(false, this.mStatusBarStateController.goingToFullShade() || this.mState == 2 || this.mStatusBarStateController.fromShadeLocked());
            }
            View view2 = this.mAmbientIndicationContainer;
            if (view2 != null) {
                view2.setVisibility(4);
            }
        }
        updateDozingState();
        checkBarModes();
        updateScrimController();
        this.mPresenter.updateMediaMetaData(false, this.mState != 1);
        ((PulseController) Dependency.get(PulseController.class)).setKeyguardShowing(this.mState == 1);
        updateKeyguardState();
        Trace.endSection();
    }

    public VisualizerView getLsVisualizer() {
        return this.mVisualizerView;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        Trace.beginSection("StatusBar#updateDozing");
        this.mDozing = z;
        this.mNotificationPanelViewController.resetViews(this.mDozeServiceHost.getDozingRequested() && this.mDozeParameters.shouldControlScreenOff());
        updateQsExpansionEnabled();
        this.mKeyguardViewMediator.setDozing(this.mDozing);
        this.mNotificationsController.requestNotificationUpdate("onDozingChanged");
        updateDozingState();
        this.mDozeServiceHost.updateDozing();
        updateScrimController();
        updateReportRejectedTouchVisibility();
        Trace.endSection();
    }

    private void updateKeyguardState() {
        this.mKeyguardStateController.notifyKeyguardState(this.mStatusBarKeyguardViewManager.isShowing(), this.mStatusBarKeyguardViewManager.isOccluded());
    }

    public void onTrackingStarted() {
        this.mShadeController.runPostCollapseRunnables();
    }

    public void onClosingFinished() {
        this.mShadeController.runPostCollapseRunnables();
        if (!this.mPresenter.isPresenterFullyCollapsed()) {
            this.mNotificationShadeWindowController.setNotificationShadeFocusable(true);
        }
        this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(false);
    }

    public void onUnlockHintStarted() {
        this.mFalsingManager.onUnlockHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(com.android.systemui.R.string.keyguard_unlock);
    }

    public void onHintFinished() {
        this.mKeyguardIndicationController.hideTransientIndicationDelayed(1200L);
    }

    public void onCameraHintStarted() {
        this.mFalsingManager.onCameraHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(com.android.systemui.R.string.camera_hint);
    }

    public void onVoiceAssistHintStarted() {
        this.mFalsingManager.onLeftAffordanceHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(com.android.systemui.R.string.voice_hint);
    }

    public void onPhoneHintStarted() {
        this.mFalsingManager.onLeftAffordanceHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(com.android.systemui.R.string.phone_hint);
    }

    public void onTrackingStopped(boolean z) {
        int i = this.mState;
        if ((i != 1 && i != 2) || z || this.mKeyguardStateController.canDismissLockScreen()) {
            return;
        }
        this.mStatusBarKeyguardViewManager.showBouncer(false);
    }

    public NavigationBarView getNavigationBarView() {
        return this.mNavigationBarController.getNavigationBarView(this.mDisplayId);
    }

    void goToLockedShade(View view) {
        NotificationEntry entry;
        if ((this.mDisabled2 & 4) != 0) {
            return;
        }
        int currentUserId = this.mLockscreenUserManager.getCurrentUserId();
        if (view instanceof ExpandableNotificationRow) {
            entry = ((ExpandableNotificationRow) view).getEntry();
            entry.setUserExpanded(true, true);
            entry.setGroupExpansionChanging(true);
            currentUserId = entry.getSbn().getUserId();
        } else {
            entry = null;
        }
        NotificationLockscreenUserManager notificationLockscreenUserManager = this.mLockscreenUserManager;
        boolean z = this.mKeyguardBypassController.getBypassEnabled() ? false : (notificationLockscreenUserManager.userAllowsPrivateNotificationsInPublic(notificationLockscreenUserManager.getCurrentUserId()) && this.mLockscreenUserManager.shouldShowLockscreenNotifications() && !this.mFalsingManager.shouldEnforceBouncer()) ? false : true;
        if (this.mLockscreenUserManager.isLockscreenPublicMode(currentUserId) && z) {
            this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
            showBouncerIfKeyguard();
            this.mDraggedDownEntry = entry;
            this.mPendingRemoteInputView = null;
            return;
        }
        this.mNotificationPanelViewController.animateToFullShade(0L);
        this.mStatusBarStateController.setState(2);
    }

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
        this.mKeyguardBypassController.setBouncerShowing(z);
        this.mPulseExpansionHandler.setBouncerShowing(z);
        this.mLockscreenLockIconController.setBouncerShowingScrimmed(isBouncerShowingScrimmed());
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.setBouncerShowing(z);
        }
        updateHideIconsForBouncer(true);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
        updateScrimController();
        if (this.mBouncerShowing) {
            return;
        }
        updatePanelExpansionForKeyguard();
    }

    /* renamed from: com.android.systemui.statusbar.phone.StatusBar$16, reason: invalid class name */
    class AnonymousClass16 implements WakefulnessLifecycle.Observer {
        AnonymousClass16() {
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            StatusBar.this.mNotificationPanelViewController.onAffordanceLaunchEnded();
            StatusBar.this.releaseGestureWakeLock();
            StatusBar.this.mLaunchCameraWhenFinishedWaking = false;
            StatusBar statusBar = StatusBar.this;
            statusBar.mDeviceInteractive = false;
            statusBar.mWakeUpComingFromTouch = false;
            StatusBar.this.mWakeUpTouchLocation = null;
            StatusBar.this.mVisualStabilityManager.setScreenOn(false);
            StatusBar.this.updateVisibleToUser();
            StatusBar.this.updateNotificationPanelTouchState();
            StatusBar.this.mNotificationShadeWindowViewController.cancelCurrentTouch();
            if (StatusBar.this.mBurnInProtectionController != null) {
                StatusBar.this.mBurnInProtectionController.stopShiftTimer(true);
            }
            if (StatusBar.this.mLaunchCameraOnFinishedGoingToSleep) {
                StatusBar.this.mLaunchCameraOnFinishedGoingToSleep = false;
                StatusBar.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$16$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onFinishedGoingToSleep$0();
                    }
                });
            }
            StatusBar.this.updateIsKeyguard(true);
            StatusBar statusBar2 = StatusBar.this;
            statusBar2.mHandler.postDelayed(statusBar2.mSystemUiGcOpt, 5000L);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFinishedGoingToSleep$0() {
            StatusBar statusBar = StatusBar.this;
            statusBar.onCameraLaunchGestureDetected(statusBar.mLastCameraLaunchSource);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            DejankUtils.startDetectingBlockingIpcs("StatusBar#onStartedGoingToSleep");
            StatusBar.this.updateNotificationPanelTouchState();
            StatusBar.this.maybeEscalateHeadsUp();
            StatusBar.this.dismissVolumeDialog();
            StatusBar.this.mWakeUpCoordinator.setFullyAwake(false);
            StatusBar.this.mBypassHeadsUpNotifier.setFullyAwake(false);
            StatusBar.this.mKeyguardBypassController.onStartedGoingToSleep();
            DejankUtils.stopDetectingBlockingIpcs("StatusBar#onStartedGoingToSleep");
            if (Settings.System.getIntForUser(((SystemUI) StatusBar.this).mContext.getContentResolver(), "jaguar_idle_manager", 1, StatusBar.this.mLockscreenUserManager.getCurrentUserId()) == 1) {
                if (!StatusBar.this.isIdleManagerIstantiated) {
                    JaguarIdleManager.initManager(((SystemUI) StatusBar.this).mContext);
                    StatusBar.this.isIdleManagerIstantiated = true;
                    JaguarIdleManager.executeManager();
                    return;
                }
                JaguarIdleManager.executeManager();
            }
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            DejankUtils.startDetectingBlockingIpcs("StatusBar#onStartedWakingUp");
            StatusBar statusBar = StatusBar.this;
            statusBar.mDeviceInteractive = true;
            statusBar.mWakeUpCoordinator.setWakingUp(true);
            if (!StatusBar.this.mKeyguardBypassController.getBypassEnabled()) {
                StatusBar.this.mHeadsUpManager.releaseAllImmediately();
            }
            StatusBar.this.mVisualStabilityManager.setScreenOn(true);
            StatusBar.this.updateVisibleToUser();
            StatusBar.this.updateIsKeyguard();
            StatusBar.this.mDozeServiceHost.stopDozing();
            StatusBar.this.updateNotificationPanelTouchState();
            StatusBar.this.mPulseExpansionHandler.onStartedWakingUp();
            DejankUtils.stopDetectingBlockingIpcs("StatusBar#onStartedWakingUp");
            if (Settings.System.getIntForUser(((SystemUI) StatusBar.this).mContext.getContentResolver(), "jaguar_idle_manager", 1, StatusBar.this.mLockscreenUserManager.getCurrentUserId()) == 1) {
                JaguarIdleManager.haltManager();
            }
            StatusBar statusBar2 = StatusBar.this;
            statusBar2.mHandler.removeCallbacks(statusBar2.mSystemUiGcOpt);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            StatusBar.this.mWakeUpCoordinator.setFullyAwake(true);
            StatusBar.this.mBypassHeadsUpNotifier.setFullyAwake(true);
            StatusBar.this.mWakeUpCoordinator.setWakingUp(false);
            if (StatusBar.this.mLaunchCameraWhenFinishedWaking) {
                StatusBar statusBar = StatusBar.this;
                statusBar.mNotificationPanelViewController.launchCamera(false, statusBar.mLastCameraLaunchSource);
                StatusBar.this.mLaunchCameraWhenFinishedWaking = false;
            }
            StatusBar.this.updateScrimController();
            if (StatusBar.this.mBurnInProtectionController != null) {
                StatusBar.this.mBurnInProtectionController.startShiftTimer(true);
            }
        }
    }

    void updateNotificationPanelTouchState() {
        boolean z = !(this.mDeviceInteractive || this.mDozeServiceHost.isPulsing()) || (isGoingToSleep() && !this.mDozeParameters.shouldControlScreenOff());
        this.mNotificationPanelViewController.setTouchAndAnimationDisabled(z);
        this.mNotificationIconAreaController.setAnimationsEnabled(!z);
    }

    public int getWakefulnessState() {
        return this.mWakefulnessLifecycle.getWakefulness();
    }

    private void vibrateForCameraGesture() {
        this.mVibrator.vibrate(this.mCameraLaunchGestureVibePattern, -1);
    }

    public boolean isScreenFullyOff() {
        return this.mScreenLifecycle.getScreenState() == 0;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showScreenPinningRequest(int i) {
        if (this.mKeyguardStateController.isShowing()) {
            return;
        }
        showScreenPinningRequest(i, true);
    }

    public void showScreenPinningRequest(int i, boolean z) {
        this.mScreenPinningRequest.showPrompt(i, z);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionCancelled(int i) {
        if (i == this.mDisplayId) {
            this.mDividerOptional.ifPresent(StatusBar$$ExternalSyntheticLambda43.INSTANCE);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionFinished(int i) {
        if (i == this.mDisplayId) {
            this.mDividerOptional.ifPresent(StatusBar$$ExternalSyntheticLambda43.INSTANCE);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onCameraLaunchGestureDetected(int i) {
        this.mLastCameraLaunchSource = i;
        if (isGoingToSleep()) {
            this.mLaunchCameraOnFinishedGoingToSleep = true;
            return;
        }
        if (this.mNotificationPanelViewController.canCameraGestureBeLaunched()) {
            if (!this.mDeviceInteractive) {
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 5, "com.android.systemui:CAMERA_GESTURE");
            }
            if (i != 3) {
                vibrateForCameraGesture();
            }
            if (i == 1) {
                Log.v("StatusBar", "Camera launch");
                this.mKeyguardUpdateMonitor.onCameraLaunched();
            }
            if (!this.mStatusBarKeyguardViewManager.isShowing()) {
                startActivityDismissingKeyguard(KeyguardBottomAreaView.INSECURE_CAMERA_INTENT, false, true, true, null, 0);
                return;
            }
            if (!this.mDeviceInteractive) {
                this.mGestureWakeLock.acquire(5000L);
            }
            if (isWakingUpOrAwake()) {
                if (this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    this.mStatusBarKeyguardViewManager.reset(true);
                }
                this.mNotificationPanelViewController.launchCamera(this.mDeviceInteractive, i);
                updateScrimController();
                return;
            }
            this.mLaunchCameraWhenFinishedWaking = true;
        }
    }

    boolean isCameraAllowedByAdmin() {
        if (this.mDevicePolicyManager.getCameraDisabled(null, this.mLockscreenUserManager.getCurrentUserId())) {
            return false;
        }
        return !(this.mStatusBarKeyguardViewManager == null || (isKeyguardShowing() && isKeyguardSecure())) || (this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mLockscreenUserManager.getCurrentUserId()) & 2) == 0;
    }

    private boolean isGoingToSleep() {
        return this.mWakefulnessLifecycle.getWakefulness() == 3;
    }

    private boolean isWakingUpOrAwake() {
        return this.mWakefulnessLifecycle.getWakefulness() == 2 || this.mWakefulnessLifecycle.getWakefulness() == 1;
    }

    public void notifyBiometricAuthModeChanged() {
        this.mDozeServiceHost.updateDozing();
        updateScrimController();
        this.mLockscreenLockIconController.onBiometricAuthModeChanged(this.mBiometricUnlockController.isWakeAndUnlock(), this.mBiometricUnlockController.isBiometricUnlock(), this.mBiometricUnlockController.getBiometricType());
    }

    @VisibleForTesting
    void updateScrimController() {
        Trace.beginSection("StatusBar#updateScrimController");
        boolean z = this.mBiometricUnlockController.isWakeAndUnlock() || this.mKeyguardStateController.isKeyguardFadingAway();
        this.mScrimController.setExpansionAffectsAlpha(!this.mBiometricUnlockController.isBiometricUnlock());
        boolean zIsLaunchingAffordanceWithPreview = this.mNotificationPanelViewController.isLaunchingAffordanceWithPreview();
        this.mScrimController.setLaunchingAffordanceWithPreview(true);
        if (this.mBouncerShowing) {
            this.mScrimController.transitionTo(this.mStatusBarKeyguardViewManager.bouncerNeedsScrimming() ? ScrimState.BOUNCER_SCRIMMED : ScrimState.BOUNCER);
        } else if (isInLaunchTransition() || this.mLaunchCameraWhenFinishedWaking || zIsLaunchingAffordanceWithPreview) {
            this.mScrimController.transitionTo(ScrimState.UNLOCKED, this.mUnlockScrimCallback);
        } else if (this.mBrightnessMirrorVisible) {
            this.mScrimController.transitionTo(ScrimState.BRIGHTNESS_MIRROR);
        } else if (this.mDozeServiceHost.isPulsing()) {
            this.mScrimController.transitionTo(ScrimState.PULSING, this.mDozeScrimController.getScrimCallback());
        } else if (this.mDozeServiceHost.hasPendingScreenOffCallback()) {
            this.mScrimController.transitionTo(ScrimState.OFF, new ScrimController.Callback() { // from class: com.android.systemui.statusbar.phone.StatusBar.19
                @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
                public void onFinished() {
                    StatusBar.this.mDozeServiceHost.executePendingScreenOffCallback();
                }
            });
        } else if (this.mDozing && !z) {
            this.mScrimController.transitionTo(ScrimState.AOD);
        } else if (this.mIsKeyguard && !z) {
            this.mScrimController.transitionTo(ScrimState.KEYGUARD);
        } else if (this.mBubbleController.isStackExpanded()) {
            this.mScrimController.transitionTo(ScrimState.BUBBLE_EXPANDED, this.mUnlockScrimCallback);
        } else {
            this.mScrimController.transitionTo(ScrimState.UNLOCKED, this.mUnlockScrimCallback);
        }
        Trace.endSection();
    }

    public boolean isKeyguardShowing() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager == null) {
            Slog.i("StatusBar", "isKeyguardShowing() called before startKeyguard(), returning true");
            return true;
        }
        return statusBarKeyguardViewManager.isShowing();
    }

    public boolean shouldIgnoreTouch() {
        return this.mStatusBarStateController.isDozing() && this.mDozeServiceHost.getIgnoreTouchWhilePulsing();
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public void setNotificationSnoozed(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.mNotificationsController.setNotificationSnoozed(statusBarNotification, snoozeOption);
    }

    public void setNotificationSnoozed(StatusBarNotification statusBarNotification, int i) {
        this.mNotificationsController.setNotificationSnoozed(statusBarNotification, i);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void toggleSplitScreen() {
        toggleSplitScreenMode(-1, -1);
    }

    void awakenDreams() {
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$awakenDreams$32();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$awakenDreams$32() {
        try {
            this.mDreamManager.awaken();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void preloadRecentApps() {
        this.mHandler.removeMessages(1022);
        this.mHandler.sendEmptyMessage(1022);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void cancelPreloadRecentApps() {
        this.mHandler.removeMessages(1023);
        this.mHandler.sendEmptyMessage(1023);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void dismissKeyboardShortcutsMenu() {
        this.mHandler.removeMessages(1027);
        this.mHandler.sendEmptyMessage(1027);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void toggleKeyboardShortcutsMenu(int i) {
        this.mHandler.removeMessages(1026);
        this.mHandler.obtainMessage(1026, i, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setTopAppHidesStatusBar(boolean z) {
        this.mTopHidesStatusBar = z;
        if (!z && this.mWereIconsJustHidden) {
            this.mWereIconsJustHidden = false;
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
        }
        updateHideIconsForBouncer(true);
    }

    protected void toggleKeyboardShortcuts(int i) {
        KeyboardShortcuts.toggle(this.mContext, i);
    }

    protected void dismissKeyboardShortcuts() {
        KeyboardShortcuts.dismiss();
    }

    public void onPanelLaidOut() {
        updateKeyguardMaxNotifications();
    }

    public void updateKeyguardMaxNotifications() {
        if (this.mState != 1 || this.mPresenter.getMaxNotificationsWhileLocked(false) == this.mPresenter.getMaxNotificationsWhileLocked(true)) {
            return;
        }
        this.mViewHierarchyManager.updateRowStates();
    }

    public void executeActionDismissingKeyguard(final Runnable runnable, boolean z) {
        if (this.mDeviceProvisionedController.isDeviceProvisioned()) {
            dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda6
                @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                public final boolean onDismiss() {
                    return this.f$0.lambda$executeActionDismissingKeyguard$34(runnable);
                }
            }, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$executeActionDismissingKeyguard$34(final Runnable runnable) {
        new Thread(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda38
            @Override // java.lang.Runnable
            public final void run() {
                StatusBar.lambda$executeActionDismissingKeyguard$33(runnable);
            }
        }).start();
        return this.mShadeController.collapsePanel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$executeActionDismissingKeyguard$33(Runnable runnable) {
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        runnable.run();
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    /* renamed from: startPendingIntentDismissingKeyguard, reason: merged with bridge method [inline-methods] */
    public void lambda$postStartActivityDismissingKeyguard$24(PendingIntent pendingIntent) {
        startPendingIntentDismissingKeyguard(pendingIntent, null);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable) {
        startPendingIntentDismissingKeyguard(pendingIntent, runnable, null);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(final PendingIntent pendingIntent, final Runnable runnable, final View view) {
        executeActionDismissingKeyguard(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBar$$ExternalSyntheticLambda30
            @Override // java.lang.Runnable
            public final void run() throws PendingIntent.CanceledException {
                this.f$0.lambda$startPendingIntentDismissingKeyguard$35(pendingIntent, view, runnable);
            }
        }, pendingIntent.isActivity() && this.mActivityIntentHelper.wouldLaunchResolverActivity(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPendingIntentDismissingKeyguard$35(PendingIntent pendingIntent, View view, Runnable runnable) throws PendingIntent.CanceledException {
        try {
            pendingIntent.send(null, 0, null, null, null, null, getActivityOptions(this.mActivityLaunchAnimator.getLaunchAnimation(view, isOccluded())));
        } catch (PendingIntent.CanceledException e) {
            Log.w("StatusBar", "Sending intent failed: " + e);
        }
        if (pendingIntent.isActivity()) {
            this.mAssistManagerLazy.get().hideAssist();
        }
        if (runnable != null) {
            postOnUiThread(runnable);
        }
    }

    private void postOnUiThread(Runnable runnable) {
        this.mMainThreadHandler.post(runnable);
    }

    public static Bundle getActivityOptions(RemoteAnimationAdapter remoteAnimationAdapter) {
        ActivityOptions activityOptionsMakeBasic;
        if (remoteAnimationAdapter != null) {
            activityOptionsMakeBasic = ActivityOptions.makeRemoteAnimation(remoteAnimationAdapter);
        } else {
            activityOptionsMakeBasic = ActivityOptions.makeBasic();
        }
        activityOptionsMakeBasic.setLaunchWindowingMode(4);
        return activityOptionsMakeBasic.toBundle();
    }

    void visibilityChanged(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            if (!z) {
                this.mGutsManager.closeAndSaveGuts(true, true, true, -1, -1, true);
            }
        }
        updateVisibleToUser();
    }

    protected void updateVisibleToUser() {
        boolean z = this.mVisibleToUser;
        boolean z2 = this.mVisible && this.mDeviceInteractive;
        this.mVisibleToUser = z2;
        if (z != z2) {
            handleVisibleToUserChanged(z2);
        }
    }

    public void clearNotificationEffects() {
        try {
            this.mBarService.clearNotificationEffects();
        } catch (RemoteException unused) {
        }
    }

    public boolean isBouncerShowing() {
        return this.mBouncerShowing;
    }

    public boolean isBouncerShowingScrimmed() {
        return isBouncerShowing() && this.mStatusBarKeyguardViewManager.bouncerNeedsScrimming();
    }

    public void onBouncerPreHideAnimation() {
        this.mNotificationPanelViewController.onBouncerPreHideAnimation();
        this.mLockscreenLockIconController.onBouncerPreHideAnimation();
    }

    public static PackageManager getPackageManagerForUser(Context context, int i) {
        if (i >= 0) {
            try {
                context = context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(i));
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        return context.getPackageManager();
    }

    public boolean isKeyguardSecure() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager == null) {
            Slog.w("StatusBar", "isKeyguardSecure() called before startKeyguard(), returning false", new Throwable());
            return false;
        }
        return statusBarKeyguardViewManager.isSecure();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showAssistDisclosure() {
        this.mAssistManagerLazy.get().showDisclosure();
    }

    public NotificationPanelViewController getPanelController() {
        return this.mNotificationPanelViewController;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void startAssist(Bundle bundle) {
        this.mAssistManagerLazy.get().startAssist(bundle);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "pulse_on_new_tracks":
                boolean integerSwitch = TunerService.parseIntegerSwitch(str2, false);
                KeyguardSliceProvider attachedInstance = KeyguardSliceProvider.getAttachedInstance();
                if (attachedInstance != null) {
                    attachedInstance.setPulseOnNewTracks(integerSwitch);
                    break;
                }
                break;
            case "sysui_rounded_fwvals":
                this.mSysuiRoundedFwvals = TunerService.parseIntegerSwitch(str2, true);
                updateCorners();
                break;
            case "system:qs_rows_landscape":
            case "system:qs_columns_portrait":
            case "system:qs_tile_title_visibility":
            case "system:qs_columns_landscape":
            case "system:qs_rows_portrait":
                QSPanel qSPanel = this.mQSPanel;
                if (qSPanel != null) {
                    qSPanel.updateResources();
                    break;
                }
                break;
            case "system:berry_signal_style":
                int integer = TunerService.parseInteger(str2, 0);
                if (this.mSignalStyle != integer) {
                    this.mSignalStyle = integer;
                    updateSignalStyle();
                    break;
                }
                break;
            case "lineagesystem:status_bar_brightness_control":
                this.mBrightnessControl = TunerService.parseIntegerSwitch(str2, false);
                break;
            case "system:display_kill_notch":
                boolean integerSwitch2 = TunerService.parseIntegerSwitch(str2, false);
                if (this.mKillNotch != integerSwitch2) {
                    this.mKillNotch = integerSwitch2;
                    handleCutout();
                    break;
                }
                break;
            case "system:gaming_mode_headsup_toggle":
                boolean integerSwitch3 = TunerService.parseIntegerSwitch(str2, true);
                this.mHeadsUpDisabled = integerSwitch3;
                this.mNotificationInterruptStateProvider.setGamingPeekMode(this.mGamingModeActivated && integerSwitch3);
                break;
            case "system:berry_rounded_style":
                int integer2 = TunerService.parseInteger(str2, 0);
                if (this.mRoundedStyle != integer2) {
                    this.mRoundedStyle = integer2;
                    updateRoundedStyle();
                    break;
                }
                break;
            case "system:gaming_mode_active":
                boolean integerSwitch4 = TunerService.parseIntegerSwitch(str2, false);
                this.mGamingModeActivated = integerSwitch4;
                this.mNotificationInterruptStateProvider.setGamingPeekMode(integerSwitch4 && this.mHeadsUpDisabled);
                break;
            case "system:qs_panel_icons_primary_color":
                QSPanel qSPanel2 = this.mQSPanel;
                if (qSPanel2 != null) {
                    qSPanel2.getHost().reloadAllTiles();
                    break;
                }
                break;
            case "system:notification_material_dismiss":
                this.mShowDimissButton = TunerService.parseIntegerSwitch(str2, false);
                updateDismissAllVisibility(true);
                break;
            case "system:navbar_style":
                int integer3 = TunerService.parseInteger(str2, 0);
                if (this.mNavbarStyle != integer3) {
                    this.mNavbarStyle = integer3;
                    updateNavbarStyle();
                    break;
                }
                break;
            case "system:screen_brightness_mode":
                this.mAutomaticBrightness = 1 == TunerService.parseInteger(str2, 0);
                break;
            case "system:qs_panel_bg_use_new_tint":
                QSPanel qSPanel3 = this.mQSPanel;
                if (qSPanel3 != null) {
                    qSPanel3.getHost().reloadAllTiles();
                    break;
                }
                break;
            case "system:stock_statusbar_in_hide":
                boolean integerSwitch5 = TunerService.parseIntegerSwitch(str2, true);
                if (this.mStockStatusBar != integerSwitch5) {
                    this.mStockStatusBar = integerSwitch5;
                    handleCutout();
                    break;
                }
                break;
            case "system:berry_wifi_style":
                int integer4 = TunerService.parseInteger(str2, 0);
                if (this.mWifiStyle != integer4) {
                    this.mWifiStyle = integer4;
                    updateWifiStyle();
                    break;
                }
                break;
            case "system:force_show_navbar":
                if (this.mDisplayId == 0 && this.mWindowManagerService != null) {
                    boolean integerSwitch6 = TunerService.parseIntegerSwitch(str2, Utils.hasNavbarByDefault(this.mContext));
                    boolean z = getNavigationBarView() != null;
                    try {
                        if (integerSwitch6) {
                            if (!z) {
                                this.mNavigationBarController.onDisplayReady(this.mDisplayId);
                            }
                        } else if (z) {
                            this.mNavigationBarController.onDisplayRemoved(this.mDisplayId);
                        }
                        break;
                    } catch (Exception unused) {
                        return;
                    }
                }
                break;
            case "system:less_boring_heads_up":
                boolean integerSwitch7 = TunerService.parseIntegerSwitch(str2, false);
                this.mlessBoringHeadsUp = integerSwitch7;
                this.mNotificationInterruptStateProvider.setUseLessBoringHeadsUp(integerSwitch7);
                break;
            case "system:display_cutout_mode":
                int integer5 = TunerService.parseInteger(str2, 0);
                if (this.mImmerseMode != integer5) {
                    this.mImmerseMode = integer5;
                    handleCutout();
                    break;
                }
                break;
        }
    }

    public NotificationGutsManager getGutsManager() {
        return this.mGutsManager;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isTransientShown() {
        return this.mTransientShown;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void suppressAmbientDisplay(boolean z) {
        this.mDozeServiceHost.setDozeSuppressed(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getForegroundPackageNameAndClass() throws SecurityException {
        List<ActivityManager.RunningTaskInfo> runningTasks = this.mActivityManager.getRunningTasks(1);
        if (runningTasks.isEmpty()) {
            return null;
        }
        ComponentName componentName = runningTasks.get(0).topActivity;
        if (componentName.getPackageName() == null) {
            return null;
        }
        return componentName.getPackageName().trim() + componentName.getShortClassName().trim();
    }

    private void setBlackStatusBar(boolean z) {
        NotificationShadeWindowViewController notificationShadeWindowViewController = this.mNotificationShadeWindowViewController;
        if (notificationShadeWindowViewController == null || notificationShadeWindowViewController.getBarTransitions() == null) {
            return;
        }
        if (z) {
            this.mNotificationShadeWindowViewController.getBarTransitions().getBackground().setColorOverride(new Integer(-16777216));
        } else {
            this.mNotificationShadeWindowViewController.getBarTransitions().getBackground().setColorOverride(null);
        }
    }

    private void handleCutout() {
        boolean z = this.mKillNotch;
        boolean z2 = false;
        boolean z3 = !z && this.mImmerseMode == 1;
        boolean z4 = !z && this.mImmerseMode == 2;
        setBlackStatusBar(this.mPortrait && z3);
        ThemeAccentUtils.setImmersiveOverlay(this.mOverlayManager, z3 || z4);
        ThemeAccentUtils.setCutoutOverlay(this.mOverlayManager, z4);
        OverlayManager overlayManager = this.mOverlayManager;
        if (z4 && this.mStockStatusBar) {
            z2 = true;
        }
        ThemeAccentUtils.setStatusBarStockOverlay(overlayManager, z2);
        ThemeAccentUtils.setKillNotchOverlay(this.mOverlayManager, this.mKillNotch);
    }
}
