package com.android.systemui.statusbar.phone;

import android.R;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ISystemGestureExclusionListener;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.GestureNavigationSettingsObserver;
import com.android.internal.util.crdroid.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.NavigationEdgeBackPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.tracing.ProtoTraceable;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tracing.nano.EdgeBackGestureHandlerProto;
import com.android.systemui.tracing.nano.SystemUiTraceProto;
import com.android.systemui.tuner.TunerService;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import lineageos.hardware.LineageHardwareManager;
import org.lineageos.internal.util.DeviceKeysConstants$Action;

/* loaded from: classes.dex */
public class EdgeBackGestureHandler extends CurrentUserTracker implements DisplayManager.DisplayListener, PluginListener<NavigationEdgeBackPlugin>, ProtoTraceable<SystemUiTraceProto>, TunerService.Tunable {
    private static final int MAX_LONG_PRESS_TIMEOUT = SystemProperties.getInt("gestures.back_timeout", 250);
    private boolean mAllowGesture;
    private final NavigationEdgeBackPlugin.BackCallback mBackCallback;
    private BackGestureTfClassifierProvider mBackGestureTfClassifierProvider;
    private boolean mBlockedGesturalNavigation;
    private float mBottomGestureHeight;
    private final Context mContext;
    private boolean mDisabledForQuickstep;
    private final int mDisplayId;
    private final Point mDisplaySize;
    private final PointF mDownPoint;
    private NavigationEdgeBackPlugin mEdgeBackPlugin;
    private boolean mEdgeHapticEnabled;
    private int mEdgeHeight;
    private int mEdgeWidthLeft;
    private int mEdgeWidthRight;
    private final PointF mEndPoint;
    private final Region mExcludeRegion;
    private final List<ComponentName> mGestureBlockingActivities;
    private boolean mGestureBlockingActivityRunning;
    private ISystemGestureExclusionListener mGestureExclusionListener;
    private final GestureNavigationSettingsObserver mGestureNavigationSettingsObserver;
    private boolean mInRejectedExclusion;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsAttached;
    private boolean mIsBackGestureAllowed;
    private boolean mIsBackGestureArrowEnabled;
    private boolean mIsEnabled;
    private boolean mIsGesturalModeEnabled;
    private boolean mIsLongSwipeEnabled;
    private boolean mIsNavBarShownTransiently;
    private boolean mIsOnLeftEdge;
    private int mLeftInset;
    private boolean mLogGesture;
    private final int mLongPressTimeout;
    private int mMLEnableWidth;
    private float mMLModelThreshold;
    private float mMLResults;
    private final Executor mMainExecutor;
    private final int mNavBarHeight;
    private boolean mNavbarVisible;
    private DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener;
    private final OverviewProxyService mOverviewProxyService;
    private String mPackageName;
    private final PluginManager mPluginManager;
    private ArrayDeque<String> mPredictionLog;
    private OverviewProxyService.OverviewProxyListener mQuickSwitchListener;
    private int mRightInset;
    private int mStartingQuickstepRotation;
    private final Runnable mStateChangeCallback;
    private int mSysUiFlags;
    private final SysUiState mSysUiState;
    private final SysUiState.SysUiStateCallback mSysUiStateCallback;
    private TaskStackChangeListener mTaskStackListener;
    private boolean mThresholdCrossed;
    private float mTouchSlop;
    private final Region mUnrestrictedExcludeRegion;
    private boolean mUseMLModel;
    private final Vibrator mVibrator;
    private Map<String, Integer> mVocab;

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayRemoved(int i) {
    }

    /* renamed from: com.android.systemui.statusbar.phone.EdgeBackGestureHandler$1, reason: invalid class name */
    class AnonymousClass1 extends ISystemGestureExclusionListener.Stub {
        AnonymousClass1() {
        }

        public void onSystemGestureExclusionChanged(int i, final Region region, final Region region2) {
            if (i == EdgeBackGestureHandler.this.mDisplayId) {
                EdgeBackGestureHandler.this.mMainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onSystemGestureExclusionChanged$0(region, region2);
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onSystemGestureExclusionChanged$0(Region region, Region region2) {
            EdgeBackGestureHandler.this.mExcludeRegion.set(region);
            Region region3 = EdgeBackGestureHandler.this.mUnrestrictedExcludeRegion;
            if (region2 != null) {
                region = region2;
            }
            region3.set(region);
        }
    }

    public EdgeBackGestureHandler(Context context, OverviewProxyService overviewProxyService, SysUiState sysUiState, PluginManager pluginManager, Runnable runnable) throws Resources.NotFoundException, PackageManager.NameNotFoundException {
        super((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));
        this.mGestureExclusionListener = new AnonymousClass1();
        this.mQuickSwitchListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.2
            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onQuickSwitchToNewTask(int i) {
                EdgeBackGestureHandler.this.mStartingQuickstepRotation = i;
                EdgeBackGestureHandler.this.updateDisabledForQuickstep();
            }
        };
        this.mTaskStackListener = new TaskStackChangeListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.3
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskStackChanged() {
                EdgeBackGestureHandler edgeBackGestureHandler = EdgeBackGestureHandler.this;
                edgeBackGestureHandler.mGestureBlockingActivityRunning = edgeBackGestureHandler.isGestureBlockingActivityRunning();
            }

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskCreated(int i, ComponentName componentName) {
                if (componentName == null) {
                    EdgeBackGestureHandler.this.mPackageName = "_UNKNOWN";
                } else {
                    EdgeBackGestureHandler.this.mPackageName = componentName.getPackageName();
                }
            }
        };
        this.mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.4
            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                if ("systemui".equals(properties.getNamespace())) {
                    if (properties.getKeyset().contains("back_gesture_ml_model_threshold") || properties.getKeyset().contains("use_back_gesture_ml_model") || properties.getKeyset().contains("back_gesture_ml_model_name")) {
                        EdgeBackGestureHandler.this.updateMLModelState();
                    }
                }
            }
        };
        this.mGestureBlockingActivities = new ArrayList();
        this.mDisplaySize = new Point();
        this.mExcludeRegion = new Region();
        this.mUnrestrictedExcludeRegion = new Region();
        this.mStartingQuickstepRotation = -1;
        this.mDownPoint = new PointF();
        this.mEndPoint = new PointF();
        this.mThresholdCrossed = false;
        this.mAllowGesture = false;
        this.mLogGesture = false;
        this.mInRejectedExclusion = false;
        this.mPredictionLog = new ArrayDeque<>();
        this.mBackCallback = new NavigationEdgeBackPlugin.BackCallback() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.5
            @Override // com.android.systemui.plugins.NavigationEdgeBackPlugin.BackCallback
            public void triggerBack(boolean z) {
                if (EdgeBackGestureHandler.this.mEdgeHapticEnabled) {
                    EdgeBackGestureHandler.this.vibrateTick();
                }
                EdgeBackGestureHandler edgeBackGestureHandler = EdgeBackGestureHandler.this;
                int i = LineageHardwareManager.FEATURE_TOUCH_HOVERING;
                edgeBackGestureHandler.sendEvent(0, 4, z ? 2048 : 0);
                EdgeBackGestureHandler edgeBackGestureHandler2 = EdgeBackGestureHandler.this;
                if (!z) {
                    i = 0;
                }
                edgeBackGestureHandler2.sendEvent(1, 4, i);
                EdgeBackGestureHandler.this.mOverviewProxyService.notifyBackAction(true, (int) EdgeBackGestureHandler.this.mDownPoint.x, (int) EdgeBackGestureHandler.this.mDownPoint.y, false, !EdgeBackGestureHandler.this.mIsOnLeftEdge);
                EdgeBackGestureHandler edgeBackGestureHandler3 = EdgeBackGestureHandler.this;
                edgeBackGestureHandler3.logGesture(edgeBackGestureHandler3.mInRejectedExclusion ? 2 : 1);
            }

            @Override // com.android.systemui.plugins.NavigationEdgeBackPlugin.BackCallback
            public void cancelBack() {
                EdgeBackGestureHandler.this.logGesture(4);
                EdgeBackGestureHandler.this.mOverviewProxyService.notifyBackAction(false, (int) EdgeBackGestureHandler.this.mDownPoint.x, (int) EdgeBackGestureHandler.this.mDownPoint.y, false, !EdgeBackGestureHandler.this.mIsOnLeftEdge);
            }
        };
        this.mSysUiStateCallback = new SysUiState.SysUiStateCallback() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.6
            @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
            public void onSystemUiStateChanged(int i) {
                EdgeBackGestureHandler.this.mSysUiFlags = i;
            }
        };
        Resources resources = context.getResources();
        this.mContext = context;
        this.mVibrator = (Vibrator) context.getSystemService(Vibrator.class);
        this.mDisplayId = context.getDisplayId();
        this.mMainExecutor = context.getMainExecutor();
        this.mOverviewProxyService = overviewProxyService;
        this.mSysUiState = sysUiState;
        this.mPluginManager = pluginManager;
        this.mStateChangeCallback = runnable;
        ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(context.getString(R.string.config_display_features));
        if (componentNameUnflattenFromString != null) {
            String packageName = componentNameUnflattenFromString.getPackageName();
            try {
                Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
                int identifier = resourcesForApplication.getIdentifier("gesture_blocking_activities", "array", packageName);
                if (identifier == 0) {
                    Log.e("EdgeBackGestureHandler", "No resource found for gesture-blocking activities");
                } else {
                    for (String str : resourcesForApplication.getStringArray(identifier)) {
                        this.mGestureBlockingActivities.add(ComponentName.unflattenFromString(str));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("EdgeBackGestureHandler", "Failed to add gesture blocking activities", e);
            }
        }
        this.mLongPressTimeout = Math.min(MAX_LONG_PRESS_TIMEOUT, ViewConfiguration.getLongPressTimeout());
        this.mGestureNavigationSettingsObserver = new GestureNavigationSettingsObserver(this.mContext.getMainThreadHandler(), this.mContext, new Runnable() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.onNavigationSettingsChanged();
            }
        });
        this.mNavBarHeight = resources.getDimensionPixelSize(com.android.systemui.R.dimen.navigation_bar_frame_height);
        updateCurrentUserResources();
    }

    public void updateCurrentUserResources() {
        Resources resources = ((NavigationModeController) Dependency.get(NavigationModeController.class)).getCurrentUserContext().getResources();
        this.mEdgeWidthLeft = this.mGestureNavigationSettingsObserver.getLeftSensitivity(resources);
        this.mEdgeWidthRight = this.mGestureNavigationSettingsObserver.getRightSensitivity(resources);
        this.mEdgeHapticEnabled = this.mGestureNavigationSettingsObserver.getEdgeHaptic();
        this.mIsBackGestureAllowed = !this.mGestureNavigationSettingsObserver.areNavigationButtonForcedVisible();
        this.mIsBackGestureArrowEnabled = this.mGestureNavigationSettingsObserver.getBackArrowGesture();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        this.mBottomGestureHeight = TypedValue.applyDimension(1, DeviceConfig.getFloat("systemui", "back_gesture_bottom_height", resources.getDimension(R.dimen.indeterminate_progress_alpha_39) / displayMetrics.density), displayMetrics);
        this.mNavbarVisible = Settings.System.getIntForUser(this.mContext.getContentResolver(), "force_show_navbar", Utils.hasNavbarByDefault(this.mContext) ? 1 : 0, -2) != 0;
        int iApplyDimension = (int) TypedValue.applyDimension(1, 12.0f, displayMetrics);
        this.mMLEnableWidth = iApplyDimension;
        int i = this.mEdgeWidthRight;
        if (iApplyDimension > i) {
            this.mMLEnableWidth = i;
        }
        int i2 = this.mMLEnableWidth;
        int i3 = this.mEdgeWidthLeft;
        if (i2 > i3) {
            this.mMLEnableWidth = i3;
        }
        TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
        tunerService.addTunable(this, "lineagesystem:key_edge_long_swipe_action");
        tunerService.addTunable(this, "system:force_show_navbar");
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop() * DeviceConfig.getFloat("systemui", "back_gesture_slop_multiplier", 0.75f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNavigationSettingsChanged() {
        boolean zIsHandlingGestures = isHandlingGestures();
        updateCurrentUserResources();
        if (zIsHandlingGestures != isHandlingGestures()) {
            this.mStateChangeCallback.run();
        }
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        updateIsEnabled();
        updateCurrentUserResources();
    }

    private void updateEdgeHeightValue() {
        if (this.mDisplaySize == null) {
            return;
        }
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "back_gesture_height", 0, -2);
        if (intForUser == 0) {
            this.mEdgeHeight = this.mDisplaySize.y;
            return;
        }
        if (intForUser == 1) {
            this.mEdgeHeight = (this.mDisplaySize.y * 3) / 4;
        } else if (intForUser == 2) {
            this.mEdgeHeight = this.mDisplaySize.y / 2;
        } else {
            this.mEdgeHeight = this.mDisplaySize.y / 4;
        }
    }

    public void onNavBarAttached() {
        this.mIsAttached = true;
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).add(this);
        this.mOverviewProxyService.addCallback(this.mQuickSwitchListener);
        this.mSysUiState.addCallback(this.mSysUiStateCallback);
        updateIsEnabled();
        startTracking();
    }

    public void onNavBarDetached() {
        this.mIsAttached = false;
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).remove(this);
        this.mOverviewProxyService.removeCallback(this.mQuickSwitchListener);
        this.mSysUiState.removeCallback(this.mSysUiStateCallback);
        updateIsEnabled();
        stopTracking();
    }

    public void onNavigationModeChanged(int i) {
        this.mIsGesturalModeEnabled = QuickStepContract.isGesturalMode(i);
        updateIsEnabled();
        updateCurrentUserResources();
    }

    public void onNavBarTransientStateChanged(boolean z) {
        this.mIsNavBarShownTransiently = z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void vibrateTick() {
        AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$vibrateTick$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$vibrateTick$0() {
        this.mVibrator.vibrate(VibrationEffect.createOneShot(20L, -1));
    }

    private void disposeInputChannel() {
        InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
        if (inputEventReceiver != null) {
            inputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.dispose();
            this.mInputMonitor = null;
        }
    }

    private void updateIsEnabled() {
        boolean z = this.mIsAttached && this.mIsGesturalModeEnabled && this.mNavbarVisible;
        if (z == this.mIsEnabled) {
            return;
        }
        this.mIsEnabled = z;
        disposeInputChannel();
        NavigationEdgeBackPlugin navigationEdgeBackPlugin = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin != null) {
            navigationEdgeBackPlugin.onDestroy();
            this.mEdgeBackPlugin = null;
        }
        if (!this.mIsEnabled) {
            this.mGestureNavigationSettingsObserver.unregister();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
            this.mPluginManager.removePluginListener(this);
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
            DeviceConfig.addOnPropertiesChangedListener("systemui", new Executor() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler$$ExternalSyntheticLambda2
                @Override // java.util.concurrent.Executor
                public final void execute(Runnable runnable) {
                    this.f$0.lambda$updateIsEnabled$1(runnable);
                }
            }, this.mOnPropertiesChangedListener);
            try {
                WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
            } catch (RemoteException | IllegalArgumentException e) {
                Log.e("EdgeBackGestureHandler", "Failed to unregister window manager callbacks", e);
            }
        } else {
            this.mGestureNavigationSettingsObserver.register();
            updateDisplaySize();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mContext.getMainThreadHandler());
            ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
            try {
                WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
            } catch (RemoteException | IllegalArgumentException e2) {
                Log.e("EdgeBackGestureHandler", "Failed to register window manager callbacks", e2);
            }
            this.mInputMonitor = InputManager.getInstance().monitorGestureInput("edge-swipe", this.mDisplayId);
            this.mInputEventReceiver = new SysUiInputEventReceiver(this.mInputMonitor.getInputChannel(), Looper.getMainLooper());
            setEdgeBackPlugin(new NavigationBarEdgePanel(this.mContext));
            this.mPluginManager.addPluginListener((PluginListener) this, NavigationEdgeBackPlugin.class, false);
            updateLongSwipeWidth();
        }
        updateMLModelState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateIsEnabled$1(Runnable runnable) {
        this.mContext.getMainThreadHandler().post(runnable);
    }

    @Override // com.android.systemui.plugins.PluginListener
    public void onPluginConnected(NavigationEdgeBackPlugin navigationEdgeBackPlugin, Context context) {
        setEdgeBackPlugin(navigationEdgeBackPlugin);
    }

    @Override // com.android.systemui.plugins.PluginListener
    public void onPluginDisconnected(NavigationEdgeBackPlugin navigationEdgeBackPlugin) {
        setEdgeBackPlugin(new NavigationBarEdgePanel(this.mContext));
    }

    private void setEdgeBackPlugin(NavigationEdgeBackPlugin navigationEdgeBackPlugin) {
        NavigationEdgeBackPlugin navigationEdgeBackPlugin2 = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin2 != null) {
            navigationEdgeBackPlugin2.onDestroy();
        }
        this.mEdgeBackPlugin = navigationEdgeBackPlugin;
        navigationEdgeBackPlugin.setBackCallback(this.mBackCallback);
        this.mEdgeBackPlugin.setLayoutParams(createLayoutParams());
        updateDisplaySize();
    }

    public boolean isHandlingGestures() {
        return this.mIsEnabled && this.mIsBackGestureAllowed;
    }

    private WindowManager.LayoutParams createLayoutParams() {
        Resources resources = this.mContext.getResources();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(resources.getDimensionPixelSize(com.android.systemui.R.dimen.navigation_edge_panel_width), resources.getDimensionPixelSize(com.android.systemui.R.dimen.navigation_edge_panel_height), 2024, 8388904, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("EdgeBackGestureHandler" + this.mContext.getDisplayId());
        layoutParams.accessibilityTitle = this.mContext.getString(com.android.systemui.R.string.nav_bar_edge_panel);
        layoutParams.windowAnimations = 0;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onInputEvent(InputEvent inputEvent) {
        if (inputEvent instanceof MotionEvent) {
            onMotionEvent((MotionEvent) inputEvent);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMLModelState() {
        boolean z = this.mIsEnabled && DeviceConfig.getBoolean("systemui", "use_back_gesture_ml_model", false);
        if (z == this.mUseMLModel) {
            return;
        }
        if (z) {
            this.mBackGestureTfClassifierProvider = SystemUIFactory.getInstance().createBackGestureTfClassifierProvider(this.mContext.getAssets(), DeviceConfig.getString("systemui", "back_gesture_ml_model_name", "backgesture"));
            this.mMLModelThreshold = DeviceConfig.getFloat("systemui", "back_gesture_ml_model_threshold", 0.9f);
            if (this.mBackGestureTfClassifierProvider.isActive()) {
                this.mVocab = this.mBackGestureTfClassifierProvider.loadVocab(this.mContext.getAssets());
                this.mUseMLModel = true;
                return;
            }
        }
        this.mUseMLModel = false;
        BackGestureTfClassifierProvider backGestureTfClassifierProvider = this.mBackGestureTfClassifierProvider;
        if (backGestureTfClassifierProvider != null) {
            backGestureTfClassifierProvider.release();
            this.mBackGestureTfClassifierProvider = null;
        }
    }

    private int getBackGesturePredictionsCategory(int i, int i2, int i3) {
        int i4;
        if (i3 == -1) {
            return -1;
        }
        double d = i;
        int i5 = this.mDisplaySize.x;
        if (d <= i5 / 2.0d) {
            i4 = 1;
        } else {
            i = i5 - i;
            i4 = 2;
        }
        float fPredict = this.mBackGestureTfClassifierProvider.predict(new Object[]{new long[]{i5}, new long[]{i}, new long[]{i4}, new long[]{i3}, new long[]{i2}});
        this.mMLResults = fPredict;
        if (fPredict == -1.0f) {
            return -1;
        }
        return fPredict >= this.mMLModelThreshold ? 1 : 0;
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x0070, code lost:
    
        r4 = 0;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v10 */
    /* JADX WARN: Type inference failed for: r4v13 */
    /* JADX WARN: Type inference failed for: r4v2 */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v4, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r4v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean isWithinTouchRegion(int r14, int r15) {
        /*
            Method dump skipped, instructions count: 252
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.isWithinTouchRegion(int, int):boolean");
    }

    private void cancelGesture(MotionEvent motionEvent) {
        this.mAllowGesture = false;
        this.mLogGesture = false;
        this.mInRejectedExclusion = false;
        MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
        motionEventObtain.setAction(3);
        this.mEdgeBackPlugin.onMotionEvent(motionEventObtain);
        motionEventObtain.recycle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logGesture(int i) {
        if (this.mLogGesture) {
            this.mLogGesture = false;
            String str = (this.mUseMLModel && this.mVocab.containsKey(this.mPackageName) && this.mVocab.get(this.mPackageName).intValue() < 100) ? this.mPackageName : "";
            PointF pointF = this.mDownPoint;
            float f = pointF.y;
            int i2 = (int) f;
            int i3 = this.mIsOnLeftEdge ? 1 : 2;
            int i4 = (int) pointF.x;
            int i5 = (int) f;
            PointF pointF2 = this.mEndPoint;
            SysUiStatsLog.write(224, i, i2, i3, i4, i5, (int) pointF2.x, (int) pointF2.y, this.mEdgeWidthLeft + this.mLeftInset, this.mDisplaySize.x - (this.mEdgeWidthRight + this.mRightInset), this.mUseMLModel ? this.mMLResults : -2.0f, str);
        }
    }

    private void onMotionEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mIsOnLeftEdge = motionEvent.getX() <= ((float) (this.mEdgeWidthLeft + this.mLeftInset));
            this.mMLResults = 0.0f;
            this.mLogGesture = false;
            this.mInRejectedExclusion = false;
            boolean z = (this.mDisabledForQuickstep || !this.mIsBackGestureAllowed || this.mGestureBlockingActivityRunning || QuickStepContract.isBackGestureDisabled(this.mSysUiFlags) || !isWithinTouchRegion((int) motionEvent.getX(), (int) motionEvent.getY())) ? false : true;
            this.mAllowGesture = z;
            if (z) {
                this.mEdgeBackPlugin.setIsLeftPanel(this.mIsOnLeftEdge);
                this.mEdgeBackPlugin.setBackArrowVisibility(this.mIsBackGestureArrowEnabled);
                this.mEdgeBackPlugin.onMotionEvent(motionEvent);
            }
            if (this.mLogGesture) {
                this.mDownPoint.set(motionEvent.getX(), motionEvent.getY());
                this.mEndPoint.set(-1.0f, -1.0f);
                this.mThresholdCrossed = false;
            }
        } else if (this.mAllowGesture || this.mLogGesture) {
            if (!this.mThresholdCrossed) {
                this.mEndPoint.x = (int) motionEvent.getX();
                this.mEndPoint.y = (int) motionEvent.getY();
                if (actionMasked == 5) {
                    if (this.mAllowGesture) {
                        logGesture(6);
                        cancelGesture(motionEvent);
                    }
                    this.mLogGesture = false;
                    return;
                }
                if (actionMasked == 2) {
                    if (motionEvent.getEventTime() - motionEvent.getDownTime() > this.mLongPressTimeout) {
                        if (this.mAllowGesture) {
                            logGesture(7);
                            cancelGesture(motionEvent);
                        }
                        this.mLogGesture = false;
                        return;
                    }
                    float fAbs = Math.abs(motionEvent.getX() - this.mDownPoint.x);
                    float fAbs2 = Math.abs(motionEvent.getY() - this.mDownPoint.y);
                    if (fAbs2 > fAbs && fAbs2 > this.mTouchSlop) {
                        if (this.mAllowGesture) {
                            logGesture(8);
                            cancelGesture(motionEvent);
                        }
                        this.mLogGesture = false;
                        return;
                    }
                    if (fAbs > fAbs2 && fAbs > this.mTouchSlop) {
                        if (this.mAllowGesture) {
                            this.mThresholdCrossed = true;
                            this.mInputMonitor.pilferPointers();
                        } else {
                            logGesture(5);
                        }
                    }
                }
            }
            if (this.mAllowGesture) {
                this.mEdgeBackPlugin.onMotionEvent(motionEvent);
            }
        }
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).update();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDisabledForQuickstep() {
        int rotation = this.mContext.getResources().getConfiguration().windowConfiguration.getRotation();
        int i = this.mStartingQuickstepRotation;
        this.mDisabledForQuickstep = i > -1 && i != rotation;
    }

    private void updateLongSwipeWidth() {
        NavigationEdgeBackPlugin navigationEdgeBackPlugin;
        if (!this.mIsEnabled || (navigationEdgeBackPlugin = this.mEdgeBackPlugin) == null) {
            return;
        }
        navigationEdgeBackPlugin.setLongSwipeEnabled(this.mIsLongSwipeEnabled);
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int i) {
        if (this.mStartingQuickstepRotation > -1) {
            updateDisabledForQuickstep();
        }
        if (i == this.mDisplayId) {
            updateDisplaySize();
        }
    }

    private void updateDisplaySize() {
        this.mContext.getDisplay().getRealSize(this.mDisplaySize);
        NavigationEdgeBackPlugin navigationEdgeBackPlugin = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin != null) {
            navigationEdgeBackPlugin.setDisplaySize(this.mDisplaySize);
        }
        updateLongSwipeWidth();
        updateEdgeHeightValue();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("lineagesystem:key_edge_long_swipe_action".equals(str)) {
            this.mIsLongSwipeEnabled = DeviceKeysConstants$Action.fromIntSafe(TunerService.parseInteger(str2, 0)) != DeviceKeysConstants$Action.NOTHING;
            updateLongSwipeWidth();
        } else if ("system:force_show_navbar".equals(str)) {
            this.mNavbarVisible = TunerService.parseIntegerSwitch(str2, Utils.hasNavbarByDefault(this.mContext));
            updateIsEnabled();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendEvent(int i, int i2, int i3) {
        long jUptimeMillis = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(jUptimeMillis, jUptimeMillis, i, i2, 0, 0, -1, 0, i3 | 8 | 64, 67108865);
        int expandedDisplayId = ((BubbleController) Dependency.get(BubbleController.class)).getExpandedDisplayId(this.mContext);
        if (i2 == 4 && expandedDisplayId != -1) {
            keyEvent.setDisplayId(expandedDisplayId);
        }
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
    }

    public void setInsets(int i, int i2) {
        this.mLeftInset = i;
        this.mRightInset = i2;
        NavigationEdgeBackPlugin navigationEdgeBackPlugin = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin != null) {
            navigationEdgeBackPlugin.setInsets(i, i2);
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("EdgeBackGestureHandler:");
        printWriter.println("  mIsEnabled=" + this.mIsEnabled);
        printWriter.println("  mIsBackGestureAllowed=" + this.mIsBackGestureAllowed);
        printWriter.println("  mAllowGesture=" + this.mAllowGesture);
        printWriter.println("  mDisabledForQuickstep=" + this.mDisabledForQuickstep);
        printWriter.println("  mStartingQuickstepRotation=" + this.mStartingQuickstepRotation);
        printWriter.println("  mInRejectedExclusion" + this.mInRejectedExclusion);
        printWriter.println("  mExcludeRegion=" + this.mExcludeRegion);
        printWriter.println("  mUnrestrictedExcludeRegion=" + this.mUnrestrictedExcludeRegion);
        printWriter.println("  mIsAttached=" + this.mIsAttached);
        printWriter.println("  mEdgeWidthLeft=" + this.mEdgeWidthLeft);
        printWriter.println("  mEdgeWidthRight=" + this.mEdgeWidthRight);
        printWriter.println("  mIsNavBarShownTransiently=" + this.mIsNavBarShownTransiently);
        printWriter.println("  mPredictionLog=" + String.join(";", this.mPredictionLog));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isGestureBlockingActivityRunning() {
        ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
        ComponentName componentName = runningTask == null ? null : runningTask.topActivity;
        if (componentName != null) {
            this.mPackageName = componentName.getPackageName();
        } else {
            this.mPackageName = "_UNKNOWN";
        }
        return componentName != null && this.mGestureBlockingActivities.contains(componentName);
    }

    public void setBlockedGesturalNavigation(boolean z) {
        this.mBlockedGesturalNavigation = z;
    }

    @Override // com.android.systemui.shared.tracing.ProtoTraceable
    public void writeToProto(SystemUiTraceProto systemUiTraceProto) {
        if (systemUiTraceProto.edgeBackGestureHandler == null) {
            systemUiTraceProto.edgeBackGestureHandler = new EdgeBackGestureHandlerProto();
        }
        systemUiTraceProto.edgeBackGestureHandler.allowGesture = this.mAllowGesture;
    }

    class SysUiInputEventReceiver extends InputEventReceiver {
        SysUiInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent inputEvent) {
            if (!EdgeBackGestureHandler.this.mBlockedGesturalNavigation) {
                EdgeBackGestureHandler.this.onInputEvent(inputEvent);
            }
            finishInputEvent(inputEvent, true);
        }
    }
}
