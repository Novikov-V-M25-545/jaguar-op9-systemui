package com.android.systemui.statusbar.phone;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.InsetsState;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import androidx.lifecycle.Lifecycle;
import androidx.mediarouter.media.MediaRoute2Provider$$ExternalSyntheticLambda0;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.LatencyTracker;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.R;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.AutoHideUiElement;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.util.LifecycleFragment;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class NavigationBarFragment extends LifecycleFragment implements CommandQueue.Callbacks, NavigationModeController.ModeChangedListener {
    private AccessibilityManager mAccessibilityManager;
    private final AccessibilityManagerWrapper mAccessibilityManagerWrapper;
    private int mAppearance;
    private AssistHandleViewController mAssistHandlerViewController;
    protected final AssistManager mAssistManager;
    private boolean mAssistantAvailable;
    private AutoHideController mAutoHideController;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CommandQueue mCommandQueue;
    private ContentResolver mContentResolver;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private int mDisabledFlags1;
    private int mDisabledFlags2;
    public int mDisplayId;
    private final Divider mDivider;
    private boolean mForceNavBarHandleOpaque;
    private final Handler mHandler;
    public boolean mHomeBlockedThisTouch;
    private boolean mIsCurrentUserSetup;
    private boolean mIsOnDefaultDisplay;
    private long mLastLockToAppLongPress;
    private int mLayoutDirection;
    private LightBarController mLightBarController;
    private Locale mLocale;
    private final MetricsLogger mMetricsLogger;
    private int mNavBarMode;
    private int mNavigationBarMode;
    private final NavigationModeController mNavigationModeController;
    private final NotificationRemoteInputManager mNotificationRemoteInputManager;
    private OverviewProxyService mOverviewProxyService;
    private final Optional<Recents> mRecentsOptional;
    private final ShadeController mShadeController;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController;
    private SysUiState mSysUiFlagsContainer;
    private final SystemActions mSystemActions;
    private boolean mTransientShown;
    private UiEventLogger mUiEventLogger;
    private WindowManager mWindowManager;
    protected NavigationBarView mNavigationBarView = null;
    private int mNavigationBarWindowState = 0;
    private int mNavigationIconHints = 0;
    private final AutoHideUiElement mAutoHideUiElement = new AutoHideUiElement() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.1
        @Override // com.android.systemui.statusbar.AutoHideUiElement
        public void synchronizeState() {
            NavigationBarFragment.this.checkNavBarModes();
        }

        @Override // com.android.systemui.statusbar.AutoHideUiElement
        public boolean shouldHideOnTouch() {
            return !NavigationBarFragment.this.mNotificationRemoteInputManager.getController().isRemoteInputActive();
        }

        @Override // com.android.systemui.statusbar.AutoHideUiElement
        public boolean isVisible() {
            return NavigationBarFragment.this.isTransientShown();
        }

        @Override // com.android.systemui.statusbar.AutoHideUiElement
        public void hide() {
            NavigationBarFragment.this.clearTransient();
        }
    };
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.2
        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onConnectionChanged(boolean z) {
            NavigationBarFragment.this.mNavigationBarView.updateStates();
            NavigationBarFragment.this.updateScreenPinningGestures();
            if (z) {
                NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                navigationBarFragment.sendAssistantAvailability(navigationBarFragment.mAssistantAvailable);
            }
        }

        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void startAssistant(Bundle bundle) {
            NavigationBarFragment.this.mAssistManager.startAssist(bundle);
        }

        /* JADX WARN: Removed duplicated region for block: B:18:0x0050  */
        /* JADX WARN: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void onNavBarButtonAlphaChanged(float r7, boolean r8) {
            /*
                r6 = this;
                com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                boolean r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$700(r0)
                if (r0 != 0) goto L9
                return
            L9:
                r0 = 0
                com.android.systemui.statusbar.phone.NavigationBarFragment r1 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                int r1 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$800(r1)
                boolean r1 = com.android.systemui.shared.system.QuickStepContract.isGesturalMode(r1)
                r2 = 0
                if (r1 == 0) goto L23
                com.android.systemui.statusbar.phone.NavigationBarFragment r3 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                com.android.systemui.statusbar.phone.NavigationBarView r3 = r3.mNavigationBarView
                boolean r3 = r3.isHomeHandleForceHidden()
                if (r3 == 0) goto L23
                r3 = 1
                goto L24
            L23:
                r3 = r2
            L24:
                com.android.systemui.statusbar.phone.NavigationBarFragment r4 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                int r4 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$800(r4)
                boolean r4 = com.android.systemui.shared.system.QuickStepContract.isSwipeUpMode(r4)
                if (r4 == 0) goto L39
                com.android.systemui.statusbar.phone.NavigationBarFragment r6 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                com.android.systemui.statusbar.phone.NavigationBarView r6 = r6.mNavigationBarView
                com.android.systemui.statusbar.phone.ButtonDispatcher r0 = r6.getBackButton()
                goto L4d
            L39:
                if (r1 == 0) goto L4d
                com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                boolean r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$900(r0)
                com.android.systemui.statusbar.phone.NavigationBarFragment r6 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                com.android.systemui.statusbar.phone.NavigationBarView r6 = r6.mNavigationBarView
                com.android.systemui.statusbar.phone.ButtonDispatcher r6 = r6.getHomeHandle()
                r5 = r0
                r0 = r6
                r6 = r5
                goto L4e
            L4d:
                r6 = r2
            L4e:
                if (r0 == 0) goto L69
                if (r3 != 0) goto L5b
                if (r6 != 0) goto L59
                r1 = 0
                int r1 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1))
                if (r1 <= 0) goto L5b
            L59:
                r1 = r2
                goto L5c
            L5b:
                r1 = 4
            L5c:
                r0.setVisibility(r1)
                if (r6 == 0) goto L63
                r7 = 1065353216(0x3f800000, float:1.0)
            L63:
                if (r3 == 0) goto L66
                r8 = r2
            L66:
                r0.setAlpha(r7, r8)
            L69:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass2.onNavBarButtonAlphaChanged(float, boolean):void");
        }

        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onOverviewShown(boolean z) {
            NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
        }

        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onToggleRecentApps() {
            NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
        }
    };
    private final Runnable mAutoDim = new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda13
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };
    private final ContentObserver mAssistContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.3
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
            if (navigationBarFragment.mNavigationBarView == null) {
                return;
            }
            boolean z2 = navigationBarFragment.mAssistManager.getAssistInfoForUser(-2) != null;
            if (NavigationBarFragment.this.mAssistantAvailable != z2) {
                NavigationBarFragment.this.sendAssistantAvailability(z2);
                NavigationBarFragment.this.mAssistantAvailable = z2;
            }
        }
    };
    private final DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.4
        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            if (properties.getKeyset().contains("nav_bar_handle_force_opaque")) {
                NavigationBarFragment.this.mForceNavBarHandleOpaque = properties.getBoolean("nav_bar_handle_force_opaque", true);
            }
        }
    };
    private final DeviceProvisionedController.DeviceProvisionedListener mUserSetupListener = new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.5
        @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
        public void onUserSetupChanged() {
            NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
            navigationBarFragment.mIsCurrentUserSetup = navigationBarFragment.mDeviceProvisionedController.isCurrentUserSetup();
        }
    };
    private final AccessibilityManager.AccessibilityServicesStateChangeListener mAccessibilityListener = new AccessibilityManager.AccessibilityServicesStateChangeListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda9
        @Override // android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener
        public final void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
            this.f$0.updateAccessibilityServicesState(accessibilityManager);
        }
    };
    private final Consumer<Integer> mRotationWatcher = new Consumer() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda14
        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            this.f$0.lambda$new$3((Integer) obj);
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                NavigationBarFragment.this.notifyNavigationBarScreenOn();
                NavigationBarFragment.this.mNavigationBarView.onScreenStateChanged("android.intent.action.SCREEN_ON".equals(action));
            }
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                navigationBarFragment.updateAccessibilityServicesState(navigationBarFragment.mAccessibilityManager);
            }
        }
    };

    private static int barMode(boolean z, int i) {
        if (z) {
            return 1;
        }
        if ((i & 6) == 6) {
            return 3;
        }
        if ((i & 4) != 0) {
            return 6;
        }
        return (i & 2) != 0 ? 4 : 0;
    }

    @VisibleForTesting
    public enum NavBarActionEvent implements UiEventLogger.UiEventEnum {
        NAVBAR_ASSIST_LONGPRESS(550);

        private final int mId;

        NavBarActionEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        getBarTransitions().setAutoDim(true);
    }

    public NavigationBarFragment(AccessibilityManagerWrapper accessibilityManagerWrapper, DeviceProvisionedController deviceProvisionedController, MetricsLogger metricsLogger, AssistManager assistManager, OverviewProxyService overviewProxyService, NavigationModeController navigationModeController, StatusBarStateController statusBarStateController, SysUiState sysUiState, BroadcastDispatcher broadcastDispatcher, CommandQueue commandQueue, Divider divider, Optional<Recents> optional, Lazy<StatusBar> lazy, ShadeController shadeController, NotificationRemoteInputManager notificationRemoteInputManager, SystemActions systemActions, Handler handler, UiEventLogger uiEventLogger) {
        this.mNavBarMode = 0;
        this.mAccessibilityManagerWrapper = accessibilityManagerWrapper;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mStatusBarStateController = statusBarStateController;
        this.mMetricsLogger = metricsLogger;
        this.mAssistManager = assistManager;
        this.mSysUiFlagsContainer = sysUiState;
        this.mStatusBarLazy = lazy;
        this.mShadeController = shadeController;
        this.mNotificationRemoteInputManager = notificationRemoteInputManager;
        this.mAssistantAvailable = assistManager.getAssistInfoForUser(-2) != null;
        this.mOverviewProxyService = overviewProxyService;
        this.mNavigationModeController = navigationModeController;
        this.mNavBarMode = navigationModeController.addListener(this);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mCommandQueue = commandQueue;
        this.mDivider = divider;
        this.mRecentsOptional = optional;
        this.mSystemActions = systemActions;
        this.mHandler = handler;
        this.mUiEventLogger = uiEventLogger;
    }

    @Override // com.android.systemui.util.LifecycleFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mCommandQueue.observe(getLifecycle(), (Lifecycle) this);
        this.mWindowManager = (WindowManager) getContext().getSystemService(WindowManager.class);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(AccessibilityManager.class);
        ContentResolver contentResolver = getContext().getContentResolver();
        this.mContentResolver = contentResolver;
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("assistant"), false, this.mAssistContentObserver, -1);
        if (bundle != null) {
            this.mDisabledFlags1 = bundle.getInt("disabled_state", 0);
            this.mDisabledFlags2 = bundle.getInt("disabled2_state", 0);
            this.mAppearance = bundle.getInt("appearance", 0);
            this.mTransientShown = bundle.getBoolean("transient_state", false);
        }
        this.mAccessibilityManagerWrapper.addCallback(this.mAccessibilityListener);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, false);
        this.mForceNavBarHandleOpaque = DeviceConfig.getBoolean("systemui", "nav_bar_handle_force_opaque", true);
        Handler handler = this.mHandler;
        Objects.requireNonNull(handler);
        DeviceConfig.addOnPropertiesChangedListener("systemui", new MediaRoute2Provider$$ExternalSyntheticLambda0(handler), this.mOnPropertiesChangedListener);
        this.mIsCurrentUserSetup = this.mDeviceProvisionedController.isCurrentUserSetup();
        this.mDeviceProvisionedController.addCallback(this.mUserSetupListener);
    }

    @Override // com.android.systemui.util.LifecycleFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mNavigationModeController.removeListener(this);
        this.mAccessibilityManagerWrapper.removeCallback(this.mAccessibilityListener);
        this.mContentResolver.unregisterContentObserver(this.mAssistContentObserver);
        this.mDeviceProvisionedController.removeCallback(this.mUserSetupListener);
        DeviceConfig.removeOnPropertiesChangedListener(this.mOnPropertiesChangedListener);
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.navigation_bar, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mNavigationBarView = (NavigationBarView) view;
        Display display = view.getDisplay();
        if (display != null) {
            int displayId = display.getDisplayId();
            this.mDisplayId = displayId;
            this.mIsOnDefaultDisplay = displayId == 0;
        }
        this.mNavigationBarView.setComponents(this.mStatusBarLazy.get().getPanelController());
        this.mNavigationBarView.setDisabledFlags(this.mDisabledFlags1);
        this.mNavigationBarView.setOnVerticalChangedListener(new NavigationBarView.OnVerticalChangedListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda10
            @Override // com.android.systemui.statusbar.phone.NavigationBarView.OnVerticalChangedListener
            public final void onVerticalChanged(boolean z) {
                this.f$0.onVerticalChanged(z);
            }
        });
        this.mNavigationBarView.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda8
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return this.f$0.onNavigationTouch(view2, motionEvent);
            }
        });
        if (bundle != null) {
            this.mNavigationBarView.getLightTransitionsController().restoreState(bundle);
        }
        this.mNavigationBarView.setNavigationIconHints(this.mNavigationIconHints);
        this.mNavigationBarView.setWindowVisible(isNavBarWindowVisible());
        prepareNavigationBarView();
        checkNavBarModes();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastReceiver, intentFilter, Handler.getMain(), UserHandle.ALL);
        notifyNavigationBarScreenOn();
        this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
        updateSystemUiStateFlags(-1);
        if (this.mIsOnDefaultDisplay) {
            RotationButtonController rotationButtonController = this.mNavigationBarView.getRotationButtonController();
            rotationButtonController.setRotationCallback(this.mRotationWatcher);
            if (display != null && rotationButtonController.isRotationLocked()) {
                rotationButtonController.setRotationLockedAtAngle(display.getRotation());
            }
        } else {
            this.mDisabledFlags2 |= 16;
        }
        setDisabled2Flags(this.mDisabledFlags2);
        if (this.mIsOnDefaultDisplay) {
            this.mAssistHandlerViewController = new AssistHandleViewController(this.mHandler, this.mNavigationBarView);
            getBarTransitions().addDarkIntensityListener(this.mAssistHandlerViewController);
        }
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            if (this.mIsOnDefaultDisplay) {
                navigationBarView.getRotationButtonController().setRotationCallback(null);
                this.mNavigationBarView.getBarTransitions().removeDarkIntensityListener(this.mAssistHandlerViewController);
                this.mAssistHandlerViewController = null;
            }
            this.mNavigationBarView.getBarTransitions().destroy();
            this.mNavigationBarView.getLightTransitionsController().destroy(getContext());
        }
        this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
        this.mBroadcastDispatcher.unregisterReceiver(this.mBroadcastReceiver);
        this.mHandler.removeCallbacks(this.mAutoDim);
        this.mNavigationBarView = null;
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("disabled_state", this.mDisabledFlags1);
        bundle.putInt("disabled2_state", this.mDisabledFlags2);
        bundle.putInt("appearance", this.mAppearance);
        bundle.putBoolean("transient_state", this.mTransientShown);
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getLightTransitionsController().saveState(bundle);
        }
    }

    @Override // android.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Locale locale = getContext().getResources().getConfiguration().locale;
        int layoutDirectionFromLocale = TextUtils.getLayoutDirectionFromLocale(locale);
        if (!locale.equals(this.mLocale) || layoutDirectionFromLocale != this.mLayoutDirection) {
            this.mLocale = locale;
            this.mLayoutDirection = layoutDirectionFromLocale;
            refreshLayout(layoutDirectionFromLocale);
        }
        repositionNavigationBar();
    }

    @Override // android.app.Fragment
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (this.mNavigationBarView != null) {
            printWriter.print("  mNavigationBarWindowState=");
            printWriter.println(StatusBarManager.windowStateToString(this.mNavigationBarWindowState));
            printWriter.print("  mNavigationBarMode=");
            printWriter.println(BarTransitions.modeToString(this.mNavigationBarMode));
            StatusBar.dumpBarTransitions(printWriter, "mNavigationBarView", this.mNavigationBarView.getBarTransitions());
        }
        printWriter.print("  mNavigationBarView=");
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            printWriter.println("null");
        } else {
            navigationBarView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x001b  */
    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void setImeWindowStatus(int r2, android.os.IBinder r3, int r4, int r5, boolean r6) {
        /*
            r1 = this;
            int r3 = r1.mDisplayId
            if (r2 == r3) goto L5
            return
        L5:
            r2 = 2
            r3 = r4 & 2
            r4 = 1
            if (r3 == 0) goto Ld
            r3 = r4
            goto Le
        Ld:
            r3 = 0
        Le:
            int r0 = r1.mNavigationIconHints
            if (r5 == 0) goto L1e
            if (r5 == r4) goto L1e
            if (r5 == r2) goto L1e
            r3 = 3
            if (r5 == r3) goto L1b
            r3 = r0
            goto L22
        L1b:
            r3 = r0 & (-2)
            goto L22
        L1e:
            if (r3 == 0) goto L1b
            r3 = r0 | 1
        L22:
            if (r6 == 0) goto L26
            r2 = r2 | r3
            goto L28
        L26:
            r2 = r3 & (-3)
        L28:
            if (r2 != r0) goto L2b
            return
        L2b:
            r1.mNavigationIconHints = r2
            com.android.systemui.statusbar.phone.NavigationBarView r3 = r1.mNavigationBarView
            if (r3 == 0) goto L34
            r3.setNavigationIconHints(r2)
        L34:
            r1.checkBarModes()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.setImeWindowStatus(int, android.os.IBinder, int, int, boolean):void");
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setWindowState(int i, int i2, int i3) {
        if (i == this.mDisplayId && i2 == 2 && this.mNavigationBarWindowState != i3) {
            this.mNavigationBarWindowState = i3;
            updateSystemUiStateFlags(-1);
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                navigationBarView.setWindowVisible(isNavBarWindowVisible());
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onRotationProposal(int i, boolean z) {
        int rotation = this.mNavigationBarView.getDisplay().getRotation();
        boolean zHasDisable2RotateSuggestionFlag = RotationButtonController.hasDisable2RotateSuggestionFlag(this.mDisabledFlags2);
        RotationButtonController rotationButtonController = this.mNavigationBarView.getRotationButtonController();
        rotationButtonController.getRotationButton();
        if (zHasDisable2RotateSuggestionFlag) {
            return;
        }
        rotationButtonController.onRotationProposal(i, rotation, z);
    }

    public void restoreAppearanceAndTransientState() {
        int iBarMode = barMode(this.mTransientShown, this.mAppearance);
        this.mNavigationBarMode = iBarMode;
        checkNavBarModes();
        this.mAutoHideController.touchAutoHide();
        this.mLightBarController.onNavigationBarAppearanceChanged(this.mAppearance, true, iBarMode, false);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        if (i != this.mDisplayId) {
            return;
        }
        boolean zUpdateBarMode = false;
        if (this.mAppearance != i2) {
            this.mAppearance = i2;
            if (getView() == null) {
                return;
            } else {
                zUpdateBarMode = updateBarMode(barMode(this.mTransientShown, i2));
            }
        }
        this.mLightBarController.onNavigationBarAppearanceChanged(i2, zUpdateBarMode, this.mNavigationBarMode, z);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1) && !this.mTransientShown) {
            this.mTransientShown = true;
            handleTransientChanged();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void abortTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1)) {
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
        if (getView() == null) {
            return;
        }
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.onTransientStateChanged(this.mTransientShown);
        }
        int iBarMode = barMode(this.mTransientShown, this.mAppearance);
        if (updateBarMode(iBarMode)) {
            this.mLightBarController.onNavigationBarModeChanged(iBarMode);
        }
    }

    private boolean updateBarMode(int i) {
        int i2 = this.mNavigationBarMode;
        if (i2 == i) {
            return false;
        }
        if (i2 == 0 || i2 == 6) {
            this.mNavigationBarView.hideRecentsOnboarding();
        }
        this.mNavigationBarMode = i;
        checkNavBarModes();
        this.mAutoHideController.touchAutoHide();
        return true;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        int i4;
        if (i != this.mDisplayId) {
            return;
        }
        int i5 = 56623104 & i2;
        if (i5 != this.mDisabledFlags1) {
            this.mDisabledFlags1 = i5;
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                navigationBarView.setDisabledFlags(i2);
            }
            updateScreenPinningGestures();
        }
        if (!this.mIsOnDefaultDisplay || (i4 = i3 & 16) == this.mDisabledFlags2) {
            return;
        }
        this.mDisabledFlags2 = i4;
        setDisabled2Flags(i4);
    }

    private void setDisabled2Flags(int i) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getRotationButtonController().onDisable2FlagChanged(i);
        }
    }

    private void refreshLayout(int i) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setLayoutDirection(i);
        }
    }

    private void repositionNavigationBar() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null || !navigationBarView.isAttachedToWindow()) {
            return;
        }
        prepareNavigationBarView();
        this.mWindowManager.updateViewLayout((View) this.mNavigationBarView.getParent(), ((View) this.mNavigationBarView.getParent()).getLayoutParams());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateScreenPinningGestures() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            return;
        }
        boolean zIsRecentsButtonVisible = navigationBarView.isRecentsButtonVisible();
        ButtonDispatcher backButton = this.mNavigationBarView.getBackButton();
        if (zIsRecentsButtonVisible) {
            backButton.setOnLongClickListener(new NavigationBarFragment$$ExternalSyntheticLambda4(this));
        } else {
            backButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda5
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return this.f$0.onLongPressBackHome(view);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyNavigationBarScreenOn() {
        this.mNavigationBarView.updateNavButtonIcons();
    }

    private void prepareNavigationBarView() {
        this.mNavigationBarView.reorient();
        ButtonDispatcher recentsButton = this.mNavigationBarView.getRecentsButton();
        recentsButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.onRecentsClick(view);
            }
        });
        recentsButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda7
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.onRecentsTouch(view, motionEvent);
            }
        });
        recentsButton.setLongClickable(true);
        recentsButton.setOnLongClickListener(new NavigationBarFragment$$ExternalSyntheticLambda4(this));
        this.mNavigationBarView.getBackButton().setLongClickable(true);
        ButtonDispatcher homeButton = this.mNavigationBarView.getHomeButton();
        homeButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda6
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.onHomeTouch(view, motionEvent);
            }
        });
        homeButton.setLongClickable(true);
        homeButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda2
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return this.f$0.onHomeLongClick(view);
            }
        });
        ButtonDispatcher accessibilityButton = this.mNavigationBarView.getAccessibilityButton();
        accessibilityButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.onAccessibilityClick(view);
            }
        });
        accessibilityButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda3
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return this.f$0.onAccessibilityLongClick(view);
            }
        });
        updateAccessibilityServicesState(this.mAccessibilityManager);
        updateScreenPinningGestures();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onHomeTouch(View view, MotionEvent motionEvent) {
        if (this.mHomeBlockedThisTouch && motionEvent.getActionMasked() != 0) {
            return true;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mHomeBlockedThisTouch = false;
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(TelecomManager.class);
            if (telecomManager != null && telecomManager.isRinging() && this.mStatusBarLazy.get().isKeyguardShowing()) {
                Log.i("NavigationBar", "Ignoring HOME; there's a ringing incoming call. No heads up");
                this.mHomeBlockedThisTouch = true;
                return true;
            }
        } else if (action == 1 || action == 3) {
            this.mStatusBarLazy.get().awakenDreams();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onVerticalChanged(boolean z) {
        this.mStatusBarLazy.get().setQsScrimEnabled(!z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onNavigationTouch(View view, MotionEvent motionEvent) {
        this.mAutoHideController.checkUserAutoHide(motionEvent);
        return false;
    }

    boolean onHomeLongClick(View view) {
        if (!this.mNavigationBarView.isRecentsButtonVisible() && ActivityManagerWrapper.getInstance().isScreenPinningActive()) {
            return onLongPressBackHome(view);
        }
        KeyButtonView keyButtonView = (KeyButtonView) view;
        keyButtonView.sendEvent(0, 128);
        keyButtonView.sendAccessibilityEvent(2);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onRecentsTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            this.mCommandQueue.preloadRecentApps();
            return false;
        }
        if (action == 3) {
            this.mCommandQueue.cancelPreloadRecentApps();
            return false;
        }
        if (action != 1 || view.isPressed()) {
            return false;
        }
        this.mCommandQueue.cancelPreloadRecentApps();
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRecentsClick(View view) {
        if (LatencyTracker.isEnabled(getContext())) {
            LatencyTracker.getInstance(getContext()).onActionStart(1);
        }
        this.mStatusBarLazy.get().awakenDreams();
        this.mCommandQueue.toggleRecentApps();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onLongPressBackHome(View view) {
        return onLongPressNavigationButtons(view, R.id.back, R.id.home);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onLongPressBackRecents(View view) {
        return onLongPressNavigationButtons(view, R.id.back, R.id.recent_apps);
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0051  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean onLongPressNavigationButtons(android.view.View r12, int r13, int r14) {
        /*
            r11 = this;
            r0 = 0
            android.app.IActivityTaskManager r1 = android.app.ActivityTaskManager.getService()     // Catch: android.os.RemoteException -> L96
            android.view.accessibility.AccessibilityManager r2 = r11.mAccessibilityManager     // Catch: android.os.RemoteException -> L96
            boolean r2 = r2.isTouchExplorationEnabled()     // Catch: android.os.RemoteException -> L96
            boolean r3 = r1.isInLockTaskMode()     // Catch: android.os.RemoteException -> L96
            r4 = 2
            r5 = 128(0x80, float:1.8E-43)
            r6 = 1
            if (r3 == 0) goto L55
            if (r2 != 0) goto L55
            long r2 = java.lang.System.currentTimeMillis()     // Catch: java.lang.Throwable -> L94
            long r7 = r11.mLastLockToAppLongPress     // Catch: java.lang.Throwable -> L94
            long r7 = r2 - r7
            r9 = 200(0xc8, double:9.9E-322)
            int r7 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r7 >= 0) goto L2e
            r1.stopSystemLockTaskMode()     // Catch: android.os.RemoteException -> L96
            com.android.systemui.statusbar.phone.NavigationBarView r11 = r11.mNavigationBarView     // Catch: android.os.RemoteException -> L96
            r11.updateNavButtonIcons()     // Catch: android.os.RemoteException -> L96
            return r6
        L2e:
            int r1 = r12.getId()     // Catch: java.lang.Throwable -> L94
            if (r1 != r13) goto L51
            int r13 = com.android.systemui.R.id.recent_apps     // Catch: java.lang.Throwable -> L94
            if (r14 != r13) goto L3f
            com.android.systemui.statusbar.phone.NavigationBarView r13 = r11.mNavigationBarView     // Catch: java.lang.Throwable -> L94
            com.android.systemui.statusbar.phone.ButtonDispatcher r13 = r13.getRecentsButton()     // Catch: java.lang.Throwable -> L94
            goto L45
        L3f:
            com.android.systemui.statusbar.phone.NavigationBarView r13 = r11.mNavigationBarView     // Catch: java.lang.Throwable -> L94
            com.android.systemui.statusbar.phone.ButtonDispatcher r13 = r13.getHomeButton()     // Catch: java.lang.Throwable -> L94
        L45:
            android.view.View r13 = r13.getCurrentView()     // Catch: java.lang.Throwable -> L94
            boolean r13 = r13.isPressed()     // Catch: java.lang.Throwable -> L94
            if (r13 != 0) goto L51
            r13 = r6
            goto L52
        L51:
            r13 = r0
        L52:
            r11.mLastLockToAppLongPress = r2     // Catch: java.lang.Throwable -> L94
            goto L89
        L55:
            int r14 = r12.getId()     // Catch: java.lang.Throwable -> L94
            if (r14 != r13) goto L5d
            r13 = r6
            goto L89
        L5d:
            if (r2 == 0) goto L6a
            if (r3 == 0) goto L6a
            r1.stopSystemLockTaskMode()     // Catch: android.os.RemoteException -> L96
            com.android.systemui.statusbar.phone.NavigationBarView r11 = r11.mNavigationBarView     // Catch: android.os.RemoteException -> L96
            r11.updateNavButtonIcons()     // Catch: android.os.RemoteException -> L96
            return r6
        L6a:
            int r13 = r12.getId()     // Catch: java.lang.Throwable -> L94
            int r14 = com.android.systemui.R.id.recent_apps     // Catch: java.lang.Throwable -> L94
            if (r13 != r14) goto L7b
            com.android.systemui.statusbar.policy.KeyButtonView r12 = (com.android.systemui.statusbar.policy.KeyButtonView) r12     // Catch: java.lang.Throwable -> L94
            r12.sendEvent(r0, r5)     // Catch: java.lang.Throwable -> L94
            r12.sendAccessibilityEvent(r4)     // Catch: java.lang.Throwable -> L94
            return r6
        L7b:
            com.android.systemui.statusbar.phone.NavigationBarView r13 = r11.mNavigationBarView     // Catch: java.lang.Throwable -> L94
            com.android.systemui.statusbar.phone.ButtonDispatcher r13 = r13.getHomeButton()     // Catch: java.lang.Throwable -> L94
            android.view.View r13 = r13.getCurrentView()     // Catch: java.lang.Throwable -> L94
            r11.onHomeLongClick(r13)     // Catch: java.lang.Throwable -> L94
            r13 = r0
        L89:
            if (r13 == 0) goto L9e
            com.android.systemui.statusbar.policy.KeyButtonView r12 = (com.android.systemui.statusbar.policy.KeyButtonView) r12     // Catch: android.os.RemoteException -> L96
            r12.sendEvent(r0, r5)     // Catch: android.os.RemoteException -> L96
            r12.sendAccessibilityEvent(r4)     // Catch: android.os.RemoteException -> L96
            return r6
        L94:
            r11 = move-exception
            throw r11     // Catch: android.os.RemoteException -> L96
        L96:
            r11 = move-exception
            java.lang.String r12 = "NavigationBar"
            java.lang.String r13 = "Unable to reach activity manager"
            android.util.Log.d(r12, r13, r11)
        L9e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.onLongPressNavigationButtons(android.view.View, int, int):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAccessibilityClick(View view) {
        Display display = view.getDisplay();
        this.mAccessibilityManager.notifyAccessibilityButtonClicked(display != null ? display.getDisplayId() : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onAccessibilityLongClick(View view) {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
        view.getContext().startActivityAsUser(intent, UserHandle.CURRENT);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAccessibilityServicesState(AccessibilityManager accessibilityManager) {
        int a11yButtonState = getA11yButtonState(new boolean[1]);
        this.mNavigationBarView.setAccessibilityButtonState((a11yButtonState & 16) != 0, (a11yButtonState & 32) != 0);
        updateSystemUiStateFlags(a11yButtonState);
    }

    public void updateSystemUiStateFlags(int i) {
        if (i < 0) {
            i = getA11yButtonState(null);
        }
        boolean z = (i & 16) != 0;
        boolean z2 = (i & 32) != 0;
        this.mSysUiFlagsContainer.setFlag(16, z).setFlag(32, z2).setFlag(2, true ^ isNavBarWindowVisible()).commitUpdate(this.mDisplayId);
        registerAction(z, 11);
        registerAction(z2, 12);
    }

    private void registerAction(boolean z, int i) {
        if (z) {
            this.mSystemActions.register(i);
        } else {
            this.mSystemActions.unregister(i);
        }
    }

    public int getA11yButtonState(boolean[] zArr) {
        List<AccessibilityServiceInfo> enabledAccessibilityServiceList = this.mAccessibilityManager.getEnabledAccessibilityServiceList(-1);
        int size = this.mAccessibilityManager.getAccessibilityShortcutTargets(0).size();
        int size2 = enabledAccessibilityServiceList.size() - 1;
        boolean z = false;
        while (true) {
            if (size2 < 0) {
                break;
            }
            int i = enabledAccessibilityServiceList.get(size2).feedbackType;
            if (i != 0 && i != 16) {
                z = true;
            }
            size2--;
        }
        if (zArr != null) {
            zArr[0] = z;
        }
        return (size < 1 ? 0 : 16) | (size >= 2 ? 32 : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAssistantAvailability(boolean z) {
        if (this.mOverviewProxyService.getProxy() != null) {
            try {
                this.mOverviewProxyService.getProxy().onAssistantAvailable(z && QuickStepContract.isGesturalMode(this.mNavBarMode));
            } catch (RemoteException unused) {
                Log.w("NavigationBar", "Unable to send assistant availability data to launcher");
            }
        }
    }

    public void touchAutoDim() {
        getBarTransitions().setAutoDim(false);
        this.mHandler.removeCallbacks(this.mAutoDim);
        int state = this.mStatusBarStateController.getState();
        if (state == 1 || state == 2) {
            return;
        }
        this.mHandler.postDelayed(this.mAutoDim, 2250L);
    }

    public void setLightBarController(LightBarController lightBarController) {
        this.mLightBarController = lightBarController;
        lightBarController.setNavigationBar(this.mNavigationBarView.getLightTransitionsController());
    }

    public void setAutoHideController(AutoHideController autoHideController) {
        this.mAutoHideController = autoHideController;
        if (autoHideController != null) {
            autoHideController.setNavigationBar(this.mAutoHideUiElement);
        }
        this.mNavigationBarView.setAutoHideController(autoHideController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isTransientShown() {
        return this.mTransientShown;
    }

    private void checkBarModes() {
        if (this.mIsOnDefaultDisplay) {
            this.mStatusBarLazy.get().checkBarModes();
        } else {
            checkNavBarModes();
        }
    }

    public boolean isNavBarWindowVisible() {
        return this.mNavigationBarWindowState == 0;
    }

    public void checkNavBarModes() {
        this.mNavigationBarView.getBarTransitions().transitionTo(this.mNavigationBarMode, this.mStatusBarLazy.get().isDeviceInteractive() && this.mNavigationBarWindowState != 2);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
        updateScreenPinningGestures();
        if (ActivityManagerWrapper.getInstance().getCurrentUserId() != 0) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onNavigationModeChanged$1();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onNavigationModeChanged$1() {
        FragmentHostManager.get(this.mNavigationBarView).reloadFragments();
    }

    public void disableAnimationsDuringHide(long j) {
        this.mNavigationBarView.setLayoutTransitionsEnabled(false);
        this.mNavigationBarView.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$disableAnimationsDuringHide$2();
            }
        }, j + 448);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$disableAnimationsDuringHide$2() {
        this.mNavigationBarView.setLayoutTransitionsEnabled(true);
    }

    public AssistHandleViewController getAssistHandlerViewController() {
        return this.mAssistHandlerViewController;
    }

    public void transitionTo(int i, boolean z) {
        getBarTransitions().transitionTo(i, z);
    }

    public NavigationBarTransitions getBarTransitions() {
        return this.mNavigationBarView.getBarTransitions();
    }

    public void finishBarAnimations() {
        this.mNavigationBarView.getBarTransitions().finishAnimations();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(Integer num) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null || !navigationBarView.needsReorient(num.intValue())) {
            return;
        }
        repositionNavigationBar();
    }

    public static View create(Context context, final FragmentHostManager.FragmentListener fragmentListener) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 545521768, -3);
        layoutParams.token = new Binder();
        layoutParams.setTitle("NavigationBar" + context.getDisplayId());
        layoutParams.accessibilityTitle = context.getString(R.string.nav_bar);
        layoutParams.windowAnimations = 0;
        layoutParams.privateFlags = layoutParams.privateFlags | 16777216;
        final View viewInflate = LayoutInflater.from(context).inflate(R.layout.navigation_bar_window, (ViewGroup) null);
        if (viewInflate == null) {
            return null;
        }
        viewInflate.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.7
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
                NavigationBarFragment navigationBarFragment = (NavigationBarFragment) FragmentHostManager.get(view).create(NavigationBarFragment.class);
                FragmentHostManager fragmentHostManager = FragmentHostManager.get(view);
                fragmentHostManager.getFragmentManager().beginTransaction().replace(R.id.navigation_bar_frame, navigationBarFragment, "NavigationBar").commit();
                fragmentHostManager.addTagListener("NavigationBar", fragmentListener);
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
                FragmentHostManager.get(view).removeTagListener("NavigationBar", fragmentListener);
                FragmentHostManager.removeAndDestroy(view);
                viewInflate.removeOnAttachStateChangeListener(this);
            }
        });
        ((WindowManager) context.getSystemService(WindowManager.class)).addView(viewInflate, layoutParams);
        return viewInflate;
    }

    int getNavigationIconHints() {
        return this.mNavigationIconHints;
    }
}
