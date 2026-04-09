package com.android.systemui.globalactions;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.sysprop.TelephonyProperties;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.ArraySet;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.colorextraction.drawable.ScrimDrawable;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.util.ScreenRecordHelper;
import com.android.internal.util.ScreenshotHelper;
import com.android.internal.view.RotationPolicy;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.MultiListLayout;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.SeedResponse;
import com.android.systemui.controls.dagger.ControlsComponent;
import com.android.systemui.controls.management.ControlsAnimations;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import com.android.systemui.globalactions.GlobalActionsDialog;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.plugins.GlobalActionsPanelPlugin;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.leak.RotationUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.inject.Provider;
import lineageos.hardware.LineageHardwareManager;
import org.lineageos.internal.util.PowerMenuUtils;

/* loaded from: classes.dex */
public class GlobalActionsDialog implements DialogInterface.OnDismissListener, DialogInterface.OnShowListener, ConfigurationController.ConfigurationListener, GlobalActionsPanelPlugin.Callbacks, LifecycleOwner, TunerService.Tunable {

    @VisibleForTesting
    static final String GLOBAL_ACTION_KEY_POWER = "power";
    private static String PANIC_PACKAGE;
    private static final String[] PANIC_PACKAGES = {"info.guardianproject.ripple", "org.calyxos.ripple"};
    private final ActivityStarter mActivityStarter;
    private MyAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver;
    private ToggleAction mAirplaneModeOn;
    private final AudioManager mAudioManager;
    private final Executor mBackgroundExecutor;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final ConfigurationController mConfigurationController;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private Optional<ControlsController> mControlsControllerOptional;
    private Optional<ControlsUiController> mControlsUiControllerOptional;
    private CurrentUserContextTracker mCurrentUserContextTracker;
    private final NotificationShadeDepthController mDepthController;
    private final DevicePolicyManager mDevicePolicyManager;

    @VisibleForTesting
    protected ActionsDialog mDialog;
    private final IDreamManager mDreamManager;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private final IActivityManager mIActivityManager;
    private final IWindowManager mIWindowManager;
    private final KeyguardStateController mKeyguardStateController;
    private final LockPatternUtils mLockPatternUtils;
    private Handler mMainHandler;
    private final MetricsLogger mMetricsLogger;
    private int mNotificationBackgroundAlpha;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private MyOverflowAdapter mOverflowAdapter;
    private MyPowerOptionsAdapter mPowerAdapter;
    private final Resources mResources;
    private MyRestartOptionsAdapter mRestartAdapter;
    private final RingerModeTracker mRingerModeTracker;
    private final ScreenRecordHelper mScreenRecordHelper;
    private final ScreenshotHelper mScreenshotHelper;
    private final boolean mShowSilentToggle;
    private Action mSilentModeAction;
    private final IStatusBarService mStatusBarService;
    private final SysUiState mSysUiState;
    private final SysuiColorExtractor mSysuiColorExtractor;
    private final TelecomManager mTelecomManager;
    private final TrustManager mTrustManager;
    private final UiEventLogger mUiEventLogger;
    private final UserManager mUserManager;
    private GlobalActionsPanelPlugin mWalletPlugin;
    private final GlobalActions.GlobalActionsManager mWindowManagerFuncs;
    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);

    @VisibleForTesting
    protected final ArrayList<Action> mItems = new ArrayList<>();

    @VisibleForTesting
    protected final ArrayList<Action> mOverflowItems = new ArrayList<>();

    @VisibleForTesting
    protected final ArrayList<Action> mPowerItems = new ArrayList<>();

    @VisibleForTesting
    protected final ArrayList<Action> mRestartItems = new ArrayList<>();
    private boolean mKeyguardShowing = false;
    private boolean mDeviceProvisioned = false;
    private ToggleState mAirplaneState = ToggleState.Off;
    private boolean mIsWaitingForEcmExit = false;
    private List<ControlsServiceInfo> mControlsServiceInfos = new ArrayList();
    private int mDialogPressDelay = 850;

    @VisibleForTesting
    boolean mShowLockScreenCardsAndControls = false;
    private boolean mPowerMenuSecure = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.8
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                String stringExtra = intent.getStringExtra("reason");
                if ("globalactions".equals(stringExtra)) {
                    return;
                }
                GlobalActionsDialog.this.mHandler.sendMessage(GlobalActionsDialog.this.mHandler.obtainMessage(0, stringExtra));
                return;
            }
            if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("android.telephony.extra.PHONE_IN_ECM_STATE", false) && GlobalActionsDialog.this.mIsWaitingForEcmExit) {
                GlobalActionsDialog.this.mIsWaitingForEcmExit = false;
                GlobalActionsDialog.this.changeAirplaneModeSystemSetting(true);
            }
        }
    };
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.9
        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            if (GlobalActionsDialog.this.mHasTelephony) {
                boolean z = serviceState.getState() == 3;
                GlobalActionsDialog.this.mAirplaneState = z ? ToggleState.On : ToggleState.Off;
                GlobalActionsDialog.this.mAirplaneModeOn.updateState(GlobalActionsDialog.this.mAirplaneState);
                GlobalActionsDialog.this.mAdapter.notifyDataSetChanged();
                GlobalActionsDialog.this.mOverflowAdapter.notifyDataSetChanged();
                GlobalActionsDialog.this.mPowerAdapter.notifyDataSetChanged();
                GlobalActionsDialog.this.mRestartAdapter.notifyDataSetChanged();
            }
        }
    };
    private Handler mHandler = new Handler() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.11
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 0) {
                if (i != 1) {
                    return;
                }
                GlobalActionsDialog.this.refreshSilentMode();
                GlobalActionsDialog.this.mAdapter.notifyDataSetChanged();
                return;
            }
            if (GlobalActionsDialog.this.mDialog != null) {
                if (!"dream".equals(message.obj)) {
                    GlobalActionsDialog.this.mDialog.dismiss();
                } else {
                    GlobalActionsDialog.this.mDialog.completeDismiss();
                }
                GlobalActionsDialog.this.mDialog = null;
            }
        }
    };

    public interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        Drawable getIcon(Context context);

        CharSequence getMessage();

        int getMessageResId();

        boolean isEnabled();

        void onPress();

        default boolean shouldBeSeparated() {
            return false;
        }

        default boolean shouldShow() {
            return true;
        }

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    private interface LongPressAction extends Action {
        boolean onLongPress();
    }

    @VisibleForTesting
    public enum GlobalActionsEvent implements UiEventLogger.UiEventEnum {
        GA_POWER_MENU_OPEN(337),
        GA_POWER_MENU_CLOSE(471),
        GA_BUGREPORT_PRESS(344),
        GA_BUGREPORT_LONG_PRESS(345),
        GA_EMERGENCY_DIALER_PRESS(346),
        GA_SCREENSHOT_PRESS(347),
        GA_SCREENSHOT_LONG_PRESS(348);

        private final int mId;

        GlobalActionsEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public GlobalActionsDialog(Context context, GlobalActions.GlobalActionsManager globalActionsManager, AudioManager audioManager, IDreamManager iDreamManager, DevicePolicyManager devicePolicyManager, LockPatternUtils lockPatternUtils, BroadcastDispatcher broadcastDispatcher, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, ContentResolver contentResolver, Vibrator vibrator, Resources resources, ConfigurationController configurationController, ActivityStarter activityStarter, KeyguardStateController keyguardStateController, UserManager userManager, TrustManager trustManager, IActivityManager iActivityManager, TelecomManager telecomManager, MetricsLogger metricsLogger, NotificationShadeDepthController notificationShadeDepthController, SysuiColorExtractor sysuiColorExtractor, IStatusBarService iStatusBarService, NotificationShadeWindowController notificationShadeWindowController, IWindowManager iWindowManager, Executor executor, UiEventLogger uiEventLogger, RingerModeTracker ringerModeTracker, SysUiState sysUiState, Handler handler, ControlsComponent controlsComponent, CurrentUserContextTracker currentUserContextTracker) {
        this.mAirplaneModeObserver = new ContentObserver(this.mMainHandler) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.10
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                GlobalActionsDialog.this.onAirplaneModeChanged();
            }
        };
        this.mContext = context;
        this.mWindowManagerFuncs = globalActionsManager;
        this.mAudioManager = audioManager;
        this.mDreamManager = iDreamManager;
        this.mDevicePolicyManager = devicePolicyManager;
        this.mLockPatternUtils = lockPatternUtils;
        this.mKeyguardStateController = keyguardStateController;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mContentResolver = contentResolver;
        this.mResources = resources;
        this.mConfigurationController = configurationController;
        this.mUserManager = userManager;
        this.mTrustManager = trustManager;
        this.mIActivityManager = iActivityManager;
        this.mTelecomManager = telecomManager;
        this.mMetricsLogger = metricsLogger;
        this.mUiEventLogger = uiEventLogger;
        this.mDepthController = notificationShadeDepthController;
        this.mSysuiColorExtractor = sysuiColorExtractor;
        this.mStatusBarService = iStatusBarService;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mControlsUiControllerOptional = controlsComponent.getControlsUiController();
        this.mIWindowManager = iWindowManager;
        this.mBackgroundExecutor = executor;
        this.mRingerModeTracker = ringerModeTracker;
        this.mControlsControllerOptional = controlsComponent.getControlsController();
        this.mSysUiState = sysUiState;
        this.mMainHandler = handler;
        this.mCurrentUserContextTracker = currentUserContextTracker;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        broadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mHasTelephony = connectivityManager.isNetworkSupported(0);
        telephonyManager.listen(this.mPhoneStateListener, 1);
        contentResolver.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        this.mHasVibrator = vibrator != null && vibrator.hasVibrator();
        boolean z = !resources.getBoolean(R.bool.config_cecSoundbarModeEnabled_allowed);
        this.mShowSilentToggle = z;
        if (z) {
            ringerModeTracker.getRingerMode().observe(this, new Observer() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$$ExternalSyntheticLambda0
                @Override // androidx.lifecycle.Observer
                public final void onChanged(Object obj) {
                    this.f$0.lambda$new$0((Integer) obj);
                }
            });
        }
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
        this.mScreenshotHelper = new ScreenshotHelper(context);
        this.mScreenRecordHelper = new ScreenRecordHelper(context);
        configurationController.addCallback(this);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:notification_bg_alpha");
        this.mActivityStarter = activityStarter;
        keyguardStateController.addCallback(new KeyguardStateController.Callback() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.1
            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onUnlockedChanged() {
                GlobalActionsDialog globalActionsDialog = GlobalActionsDialog.this;
                if (globalActionsDialog.mDialog != null) {
                    boolean zIsUnlocked = globalActionsDialog.mKeyguardStateController.isUnlocked();
                    if (GlobalActionsDialog.this.mDialog.mWalletViewController != null) {
                        GlobalActionsDialog.this.mDialog.mWalletViewController.onDeviceLockStateChanged(!zIsUnlocked);
                    }
                    if (!GlobalActionsDialog.this.mDialog.isShowingControls() && GlobalActionsDialog.this.shouldShowControls()) {
                        GlobalActionsDialog globalActionsDialog2 = GlobalActionsDialog.this;
                        globalActionsDialog2.mDialog.showControls((ControlsUiController) globalActionsDialog2.mControlsUiControllerOptional.get());
                    }
                    if (zIsUnlocked) {
                        GlobalActionsDialog.this.mDialog.hideLockMessage();
                    }
                }
            }
        });
        if (controlsComponent.getControlsListingController().isPresent()) {
            controlsComponent.getControlsListingController().get().addCallback(new ControlsListingController.ControlsListingCallback() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$$ExternalSyntheticLambda1
                @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
                public final void onServicesUpdated(List list) {
                    this.f$0.lambda$new$1(list);
                }
            });
        }
        onPowerMenuLockScreenSettingsChanged();
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("power_menu_locked_show_content"), false, new ContentObserver(this.mMainHandler) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z2) {
                GlobalActionsDialog.this.onPowerMenuLockScreenSettingsChanged();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(Integer num) {
        this.mHandler.sendEmptyMessage(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(List list) {
        this.mControlsServiceInfos = list;
        ActionsDialog actionsDialog = this.mDialog;
        if (actionsDialog != null) {
            if (actionsDialog.isShowingControls() || !shouldShowControls()) {
                if (shouldShowLockMessage(this.mDialog)) {
                    this.mDialog.showLockMessage();
                    return;
                }
                return;
            }
            this.mDialog.showControls(this.mControlsUiControllerOptional.get());
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:notification_bg_alpha")) {
            int integer = TunerService.parseInteger(str2, 255);
            this.mNotificationBackgroundAlpha = integer;
            GlobalActionsPowerDialog.mNotificationBackgroundAlpha = integer;
        }
    }

    private void seedFavorites() throws Resources.NotFoundException {
        if (!this.mControlsControllerOptional.isPresent() || this.mControlsServiceInfos.isEmpty()) {
            return;
        }
        String[] stringArray = this.mContext.getResources().getStringArray(com.android.systemui.R.array.config_controlsPreferredPackages);
        final SharedPreferences sharedPreferences = this.mCurrentUserContextTracker.getCurrentUserContext().getSharedPreferences("controls_prefs", 0);
        Set<String> stringSet = sharedPreferences.getStringSet("SeedingCompleted", Collections.emptySet());
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < Math.min(2, stringArray.length); i++) {
            String str = stringArray[i];
            Iterator<ControlsServiceInfo> it = this.mControlsServiceInfos.iterator();
            while (true) {
                if (it.hasNext()) {
                    ControlsServiceInfo next = it.next();
                    if (str.equals(next.componentName.getPackageName())) {
                        if (!stringSet.contains(str)) {
                            if (this.mControlsControllerOptional.get().countFavoritesForComponent(next.componentName) > 0) {
                                addPackageToSeededSet(sharedPreferences, str);
                            } else {
                                arrayList.add(next.componentName);
                            }
                        }
                    }
                }
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        this.mControlsControllerOptional.get().seedFavoritesForComponents(arrayList, new Consumer() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$$ExternalSyntheticLambda4
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$seedFavorites$2(sharedPreferences, (SeedResponse) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$seedFavorites$2(SharedPreferences sharedPreferences, SeedResponse seedResponse) {
        Log.d("GlobalActionsDialog", "Controls seeded: " + seedResponse);
        if (seedResponse.getAccepted()) {
            addPackageToSeededSet(sharedPreferences, seedResponse.getPackageName());
        }
    }

    private void addPackageToSeededSet(SharedPreferences sharedPreferences, String str) {
        HashSet hashSet = new HashSet(sharedPreferences.getStringSet("SeedingCompleted", Collections.emptySet()));
        hashSet.add(str);
        sharedPreferences.edit().putStringSet("SeedingCompleted", hashSet).apply();
    }

    public void showOrHideDialog(boolean z, boolean z2, GlobalActionsPanelPlugin globalActionsPanelPlugin) throws Resources.NotFoundException {
        this.mKeyguardShowing = z;
        this.mDeviceProvisioned = z2;
        this.mWalletPlugin = globalActionsPanelPlugin;
        ActionsDialog actionsDialog = this.mDialog;
        if (actionsDialog != null && actionsDialog.isShowing()) {
            this.mWindowManagerFuncs.onGlobalActionsShown();
            this.mDialog.dismiss();
            this.mDialog = null;
            return;
        }
        handleShow();
    }

    public void dismissDialog() {
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(0);
    }

    private void awakenIfNecessary() {
        IDreamManager iDreamManager = this.mDreamManager;
        if (iDreamManager != null) {
            try {
                if (iDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException unused) {
            }
        }
    }

    private void handleShow() throws Resources.NotFoundException {
        awakenIfNecessary();
        this.mDialog = createDialog();
        prepareDialog();
        seedFavorites();
        WindowManager.LayoutParams attributes = this.mDialog.getWindow().getAttributes();
        attributes.setTitle("ActionsDialog");
        attributes.layoutInDisplayCutoutMode = 3;
        this.mDialog.getWindow().setAttributes(attributes);
        this.mDialog.getWindow().setFlags(LineageHardwareManager.FEATURE_COLOR_BALANCE, LineageHardwareManager.FEATURE_COLOR_BALANCE);
        this.mDialog.show();
        this.mWindowManagerFuncs.onGlobalActionsShown();
    }

    @VisibleForTesting
    protected boolean shouldShowAction(Action action) {
        if (this.mKeyguardShowing && !action.showDuringKeyguard()) {
            return false;
        }
        if (this.mDeviceProvisioned || action.showBeforeProvisioning()) {
            return action.shouldShow();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldShowRestartSubmenu() {
        return PowerMenuUtils.isAdvancedRestartPossible(this.mContext);
    }

    @VisibleForTesting
    protected int getMaxShownPowerItems() {
        return this.mResources.getInteger(com.android.systemui.R.integer.power_menu_max_columns);
    }

    private void addActionItem(Action action) {
        if (this.mItems.size() < getMaxShownPowerItems()) {
            this.mItems.add(action);
        } else {
            this.mOverflowItems.add(action);
        }
    }

    @VisibleForTesting
    protected String[] getDefaultActions() {
        return this.mResources.getStringArray(R.array.config_displayCutoutApproximationRectArray);
    }

    @VisibleForTesting
    protected String[] getRestartActions() {
        return this.mResources.getStringArray(1057161220);
    }

    private void addIfShouldShowAction(List<Action> list, Action action) {
        if (shouldShowAction(action)) {
            list.add(action);
        }
    }

    @VisibleForTesting
    protected void createActionItems() {
        RestartDownloadAction restartDownloadAction;
        RestartDownloadAction restartDownloadAction2;
        RestartFastbootAction restartFastbootAction;
        RestartSystemUIAction restartSystemUIAction;
        String[] strArr;
        String str;
        CurrentUserProvider currentUserProvider;
        if (!this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeToggleAction();
        } else {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mAudioManager, this.mHandler);
        }
        this.mAirplaneModeOn = new AirplaneModeAction();
        onAirplaneModeChanged();
        this.mItems.clear();
        this.mOverflowItems.clear();
        this.mPowerItems.clear();
        this.mRestartItems.clear();
        String[] defaultActions = getDefaultActions();
        String[] restartActions = getRestartActions();
        ShutDownAction shutDownAction = new ShutDownAction();
        RestartAction restartAction = new RestartAction();
        RestartSystemAction restartSystemAction = new RestartSystemAction();
        RestartRecoveryAction restartRecoveryAction = new RestartRecoveryAction();
        RestartBootloaderAction restartBootloaderAction = new RestartBootloaderAction();
        RestartDownloadAction restartDownloadAction3 = new RestartDownloadAction();
        RestartFastbootAction restartFastbootAction2 = new RestartFastbootAction();
        RestartSystemUIAction restartSystemUIAction2 = new RestartSystemUIAction();
        ArraySet arraySet = new ArraySet();
        ArraySet arraySet2 = new ArraySet();
        ArrayList arrayList = new ArrayList();
        CurrentUserProvider currentUserProvider2 = new CurrentUserProvider();
        RestartSystemUIAction restartSystemUIAction3 = restartSystemUIAction2;
        String str2 = "emergency";
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            addIfShouldShowAction(arrayList, new EmergencyAffordanceAction());
            arraySet.add("emergency");
        }
        RestartFastbootAction restartFastbootAction3 = restartFastbootAction2;
        int i = 0;
        while (true) {
            restartDownloadAction = restartDownloadAction3;
            if (i >= defaultActions.length) {
                break;
            }
            String str3 = defaultActions[i];
            if (arraySet.contains(str3)) {
                strArr = defaultActions;
                str = str2;
                currentUserProvider = currentUserProvider2;
            } else {
                strArr = defaultActions;
                if (GLOBAL_ACTION_KEY_POWER.equals(str3)) {
                    addIfShouldShowAction(arrayList, shutDownAction);
                } else if ("airplane".equals(str3)) {
                    addIfShouldShowAction(arrayList, this.mAirplaneModeOn);
                } else if ("bugreport".equals(str3)) {
                    if (shouldDisplayBugReport(currentUserProvider2.get())) {
                        addIfShouldShowAction(arrayList, new BugReportAction());
                    }
                } else if ("silent".equals(str3)) {
                    if (this.mShowSilentToggle) {
                        addIfShouldShowAction(arrayList, this.mSilentModeAction);
                    }
                } else if ("users".equals(str3)) {
                    if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                        addUserActions(arrayList, currentUserProvider2.get());
                    }
                } else if ("settings".equals(str3)) {
                    addIfShouldShowAction(arrayList, getSettingsAction());
                } else if ("lockdown".equals(str3)) {
                    if (shouldDisplayLockdown(currentUserProvider2.get())) {
                        addIfShouldShowAction(arrayList, new LockDownAction());
                    }
                } else if ("voiceassist".equals(str3)) {
                    addIfShouldShowAction(arrayList, getVoiceAssistAction());
                } else if ("assist".equals(str3)) {
                    addIfShouldShowAction(arrayList, getAssistAction());
                } else if ("restart".equals(str3)) {
                    addIfShouldShowAction(arrayList, restartAction);
                } else if ("screenshot".equals(str3)) {
                    addIfShouldShowAction(arrayList, new ScreenshotAction());
                } else if ("users_choice".equals(str3)) {
                    if (OnTheGoUserEnabled(this.mContext)) {
                        addIfShouldShowAction(arrayList, getOnTheGoAction());
                    }
                } else if ("logout".equals(str3)) {
                    if (this.mDevicePolicyManager.isLogoutEnabled() && currentUserProvider2.get() != null && currentUserProvider2.get().id != 0) {
                        addIfShouldShowAction(arrayList, new LogoutAction());
                    }
                } else {
                    if (str2.equals(str3)) {
                        addIfShouldShowAction(arrayList, new EmergencyDialerAction());
                    } else {
                        if ("panic".equals(str3)) {
                            str = str2;
                            currentUserProvider = currentUserProvider2;
                            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "panic_in_power_menu", 0, getCurrentUser().id) != 0 && isPanicAvailable()) {
                                addIfShouldShowAction(arrayList, new PanicAction());
                            }
                        } else {
                            str = str2;
                            currentUserProvider = currentUserProvider2;
                            Log.e("GlobalActionsDialog", "Invalid global action key " + str3);
                        }
                        arraySet.add(str3);
                    }
                }
                str = str2;
                currentUserProvider = currentUserProvider2;
                arraySet.add(str3);
            }
            i++;
            restartDownloadAction3 = restartDownloadAction;
            defaultActions = strArr;
            str2 = str;
            currentUserProvider2 = currentUserProvider;
        }
        int i2 = 0;
        while (i2 < restartActions.length) {
            String str4 = restartActions[i2];
            if (arraySet2.contains(str4)) {
                restartSystemUIAction = restartSystemUIAction3;
                restartFastbootAction = restartFastbootAction3;
                restartDownloadAction2 = restartDownloadAction;
            } else {
                if ("restart".equals(str4)) {
                    addIfShouldShowAction(this.mRestartItems, restartSystemAction);
                } else if ("restart_recovery".equals(str4)) {
                    addIfShouldShowAction(this.mRestartItems, restartRecoveryAction);
                } else if ("restart_bootloader".equals(str4)) {
                    addIfShouldShowAction(this.mRestartItems, restartBootloaderAction);
                } else {
                    if ("restart_download".equals(str4)) {
                        restartDownloadAction2 = restartDownloadAction;
                        addIfShouldShowAction(this.mRestartItems, restartDownloadAction2);
                        restartSystemUIAction = restartSystemUIAction3;
                        restartFastbootAction = restartFastbootAction3;
                    } else {
                        restartDownloadAction2 = restartDownloadAction;
                        if ("restart_fastboot".equals(str4)) {
                            restartFastbootAction = restartFastbootAction3;
                            addIfShouldShowAction(this.mRestartItems, restartFastbootAction);
                        } else {
                            restartFastbootAction = restartFastbootAction3;
                            if ("restart_systemui".equals(str4)) {
                                restartSystemUIAction = restartSystemUIAction3;
                                addIfShouldShowAction(this.mRestartItems, restartSystemUIAction);
                            }
                        }
                        restartSystemUIAction = restartSystemUIAction3;
                    }
                    arraySet2.add(str4);
                }
                restartSystemUIAction = restartSystemUIAction3;
                restartFastbootAction = restartFastbootAction3;
                restartDownloadAction2 = restartDownloadAction;
                arraySet2.add(str4);
            }
            i2++;
            restartDownloadAction = restartDownloadAction2;
            restartFastbootAction3 = restartFastbootAction;
            restartSystemUIAction3 = restartSystemUIAction;
        }
        if (arrayList.contains(shutDownAction) && arrayList.contains(restartAction) && arrayList.size() > getMaxShownPowerItems()) {
            int iMin = Math.min(arrayList.indexOf(restartAction), arrayList.indexOf(shutDownAction));
            arrayList.remove(shutDownAction);
            arrayList.remove(restartAction);
            this.mPowerItems.add(shutDownAction);
            this.mPowerItems.add(restartAction);
            arrayList.add(iMin, new PowerOptionsAction());
        }
        Iterator<Action> it = arrayList.iterator();
        while (it.hasNext()) {
            addActionItem(it.next());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRotate() {
        createActionItems();
    }

    private ActionsDialog createDialog() {
        createActionItems();
        this.mAdapter = new MyAdapter();
        this.mOverflowAdapter = new MyOverflowAdapter();
        this.mPowerAdapter = new MyPowerOptionsAdapter();
        this.mRestartAdapter = new MyRestartOptionsAdapter();
        this.mDepthController.setShowingHomeControls(true);
        ActionsDialog actionsDialog = new ActionsDialog(this.mContext, this.mAdapter, this.mOverflowAdapter, new Provider() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$$ExternalSyntheticLambda5
            @Override // javax.inject.Provider
            public final Object get() {
                return this.f$0.getWalletViewController();
            }
        }, this.mDepthController, this.mSysuiColorExtractor, this.mStatusBarService, this.mNotificationShadeWindowController, controlsAvailable(), (this.mControlsUiControllerOptional.isPresent() && shouldShowControls()) ? this.mControlsUiControllerOptional.get() : null, this.mSysUiState, new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.onRotate();
            }
        }, this.mKeyguardShowing, this.mPowerAdapter, this.mRestartAdapter);
        if (shouldShowLockMessage(actionsDialog)) {
            actionsDialog.showLockMessage();
        }
        actionsDialog.setCanceledOnTouchOutside(false);
        actionsDialog.setOnDismissListener(this);
        actionsDialog.setOnShowListener(this);
        this.mPowerMenuSecure = Settings.System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_powermenu_secure", 0, -2) != 0;
        return actionsDialog;
    }

    @VisibleForTesting
    boolean shouldDisplayLockdown(UserInfo userInfo) {
        if (userInfo == null) {
            return false;
        }
        int i = userInfo.id;
        if (Settings.Secure.getIntForUser(this.mContentResolver, "lockdown_in_power_menu", 0, i) == 0 || !this.mKeyguardStateController.isMethodSecure()) {
            return false;
        }
        int strongAuthForUser = this.mLockPatternUtils.getStrongAuthForUser(i);
        return strongAuthForUser == 0 || strongAuthForUser == 4;
    }

    private boolean OnTheGoUserEnabled(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(), "global_actions_users_choice", 0, -2) == 1;
    }

    @VisibleForTesting
    boolean shouldDisplayBugReport(UserInfo userInfo) {
        if (Settings.Global.getInt(this.mContentResolver, "bugreport_in_power_menu", 0) != 0) {
            return userInfo == null || userInfo.isPrimary();
        }
        return false;
    }

    private boolean isPanicAvailable() {
        for (String str : PANIC_PACKAGES) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(str, "org.calyxos.ripple.CountDownActivity"));
            intent.addFlags(335544320);
            if (this.mContext.getPackageManager().resolveActivity(intent, 0) != null) {
                PANIC_PACKAGE = str;
                return true;
            }
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        this.mContext.getTheme().applyStyle(this.mContext.getThemeResId(), true);
        ActionsDialog actionsDialog = this.mDialog;
        if (actionsDialog == null || !actionsDialog.isShowing()) {
            return;
        }
        this.mDialog.refreshDialog();
    }

    public void destroy() {
        this.mConfigurationController.removeCallback(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public GlobalActionsPanelPlugin.PanelViewController getWalletViewController() {
        GlobalActionsPanelPlugin globalActionsPanelPlugin = this.mWalletPlugin;
        if (globalActionsPanelPlugin == null) {
            return null;
        }
        return globalActionsPanelPlugin.onPanelShown(this, !this.mKeyguardStateController.isUnlocked());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean rebootAction(final boolean z, final String str) {
        if (this.mPowerMenuSecure && this.mKeyguardStateController.isMethodSecure() && this.mKeyguardStateController.isShowing()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$rebootAction$3(z, str);
                }
            });
            return true;
        }
        this.mWindowManagerFuncs.reboot(z, str);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$rebootAction$3(boolean z, String str) {
        this.mWindowManagerFuncs.reboot(z, str);
    }

    @Override // com.android.systemui.plugins.GlobalActionsPanelPlugin.Callbacks
    public void dismissGlobalActionsMenu() {
        dismissDialog();
    }

    @Override // com.android.systemui.plugins.GlobalActionsPanelPlugin.Callbacks
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActivityStarter.lambda$postStartActivityDismissingKeyguard$24(pendingIntent);
    }

    @VisibleForTesting
    protected final class PowerOptionsAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private PowerOptionsAction() {
            super(com.android.systemui.R.drawable.ic_settings_power, R.string.ext_media_move_specific_title);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.showPowerOptionsMenu();
            }
        }
    }

    @VisibleForTesting
    final class ShutDownAction extends SinglePressAction implements LongPressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private ShutDownAction() {
            super(R.drawable.ic_lock_power_off, R.string.ext_media_move_failure_title);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.LongPressAction
        public boolean onLongPress() {
            if (GlobalActionsDialog.this.mUserManager.hasUserRestriction("no_safe_boot")) {
                return false;
            }
            return GlobalActionsDialog.this.rebootAction(true, null);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mWindowManagerFuncs.shutdown();
        }
    }

    @VisibleForTesting
    protected abstract class EmergencyAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean shouldBeSeparated() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        EmergencyAction(int i, int i2) {
            super(i, i2);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.SinglePressAction, com.android.systemui.globalactions.GlobalActionsDialog.Action
        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) throws Resources.NotFoundException {
            View viewCreate = super.create(context, view, viewGroup, layoutInflater);
            int color = viewCreate.getResources().getColor(com.android.systemui.R.color.global_actions_emergency_text);
            TextView textView = (TextView) viewCreate.findViewById(R.id.message);
            textView.setTextColor(color);
            textView.setSelected(true);
            ((ImageView) viewCreate.findViewById(R.id.icon)).getDrawable().setTint(viewCreate.getResources().getColor(com.android.systemui.R.color.global_actions_emergency_background));
            return viewCreate;
        }
    }

    private class EmergencyAffordanceAction extends EmergencyAction {
        EmergencyAffordanceAction() {
            super(R.drawable.decor_caption_title_unfocused, R.string.ext_media_init_action);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mEmergencyAffordanceManager.performEmergencyCall();
        }
    }

    @VisibleForTesting
    class EmergencyDialerAction extends EmergencyAction {
        private EmergencyDialerAction() {
            super(com.android.systemui.R.drawable.ic_emergency_star, R.string.ext_media_init_action);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mMetricsLogger.action(1569);
            GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_EMERGENCY_DIALER_PRESS);
            if (GlobalActionsDialog.this.mTelecomManager != null) {
                Intent intentCreateLaunchEmergencyDialerIntent = GlobalActionsDialog.this.mTelecomManager.createLaunchEmergencyDialerIntent(null);
                intentCreateLaunchEmergencyDialerIntent.addFlags(343932928);
                intentCreateLaunchEmergencyDialerIntent.putExtra("com.android.phone.EmergencyDialer.extra.ENTRY_TYPE", 2);
                GlobalActionsDialog.this.mContext.startActivityAsUser(intentCreateLaunchEmergencyDialerIntent, UserHandle.CURRENT);
            }
        }
    }

    @VisibleForTesting
    EmergencyDialerAction makeEmergencyDialerActionForTesting() {
        return new EmergencyDialerAction();
    }

    @VisibleForTesting
    final class RestartAction extends SinglePressAction implements LongPressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartAction() {
            super(R.drawable.ic_menu_find_mtrl_alpha, R.string.ext_media_move_success_message);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.LongPressAction
        public boolean onLongPress() {
            if (GlobalActionsDialog.this.mUserManager.hasUserRestriction("no_safe_boot")) {
                return false;
            }
            return GlobalActionsDialog.this.rebootAction(true, null);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog globalActionsDialog = GlobalActionsDialog.this;
            if (globalActionsDialog.mDialog == null || !globalActionsDialog.shouldShowRestartSubmenu()) {
                GlobalActionsDialog.this.rebootAction(false, null);
            } else {
                GlobalActionsDialog.this.mDialog.showRestartOptionsMenu();
            }
        }
    }

    private final class RestartSystemAction extends SinglePressAction implements LongPressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        public RestartSystemAction() {
            super(R.drawable.ic_menu_find_mtrl_alpha, com.android.systemui.R.string.global_action_restart_system);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.LongPressAction
        public boolean onLongPress() {
            if (GlobalActionsDialog.this.mUserManager.hasUserRestriction("no_safe_boot")) {
                return false;
            }
            return GlobalActionsDialog.this.rebootAction(true, null);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.rebootAction(false, null);
        }
    }

    private final class RestartRecoveryAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartRecoveryAction() {
            super(com.android.systemui.R.drawable.ic_lock_restart_recovery, com.android.systemui.R.string.global_action_restart_recovery);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.rebootAction(false, "recovery");
        }
    }

    private final class RestartBootloaderAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartBootloaderAction() {
            super(com.android.systemui.R.drawable.ic_lock_restart_bootloader, com.android.systemui.R.string.global_action_restart_bootloader);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.rebootAction(false, "bootloader");
        }
    }

    private final class RestartFastbootAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartFastbootAction() {
            super(com.android.systemui.R.drawable.ic_lock_restart_fastboot, com.android.systemui.R.string.global_action_restart_fastboot);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.rebootAction(false, "fastboot");
        }
    }

    private final class RestartDownloadAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartDownloadAction() {
            super(com.android.systemui.R.drawable.ic_lock_restart_bootloader, com.android.systemui.R.string.global_action_restart_download);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.rebootAction(false, "download");
        }
    }

    private final class RestartSystemUIAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartSystemUIAction() {
            super(com.android.systemui.R.drawable.ic_restart_systemui, com.android.systemui.R.string.global_action_restart_systemui);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mWindowManagerFuncs.onGlobalActionsHidden();
            Process.killProcess(Process.myPid());
        }
    }

    @VisibleForTesting
    class ScreenshotAction extends SinglePressAction implements LongPressAction {
        final String KEY_SYSTEM_NAV_2BUTTONS;

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        public ScreenshotAction() {
            super(R.drawable.ic_menu_friendslist, R.string.ext_media_move_success_title);
            this.KEY_SYSTEM_NAV_2BUTTONS = "system_nav_2buttons";
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.ScreenshotAction.1
                @Override // java.lang.Runnable
                public void run() {
                    GlobalActionsDialog.this.mScreenshotHelper.takeScreenshot(1, true, true, 0, GlobalActionsDialog.this.mHandler, (Consumer) null);
                    GlobalActionsDialog.this.mMetricsLogger.action(1282);
                    GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_SCREENSHOT_PRESS);
                }
            }, GlobalActionsDialog.this.mDialogPressDelay);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean shouldShow() {
            return is2ButtonNavigationEnabled();
        }

        boolean is2ButtonNavigationEnabled() {
            return 1 == GlobalActionsDialog.this.mContext.getResources().getInteger(R.integer.config_dreamOverlayMaxReconnectAttempts);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.LongPressAction
        public boolean onLongPress() {
            if (FeatureFlagUtils.isEnabled(GlobalActionsDialog.this.mContext, "settings_screenrecord_long_press")) {
                GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_SCREENSHOT_LONG_PRESS);
                GlobalActionsDialog.this.mScreenRecordHelper.launchRecordPrompt();
                return true;
            }
            GlobalActionsDialog.this.mScreenshotHelper.takeScreenshot(2, true, true, GlobalActionsDialog.this.mHandler, (Consumer) null);
            return true;
        }
    }

    @VisibleForTesting
    ScreenshotAction makeScreenshotActionForTesting() {
        return new ScreenshotAction();
    }

    @VisibleForTesting
    class BugReportAction extends SinglePressAction implements LongPressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        public BugReportAction() {
            super(R.drawable.ic_contact_picture_2, R.string.bluetooth_airplane_mode_toast);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            if (ActivityManager.isUserAMonkey()) {
                return;
            }
            GlobalActionsDialog.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.BugReportAction.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        GlobalActionsDialog.this.mMetricsLogger.action(292);
                        GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_BUGREPORT_PRESS);
                        if (GlobalActionsDialog.this.mIActivityManager.launchBugReportHandlerApp()) {
                            return;
                        }
                        Log.w("GlobalActionsDialog", "Bugreport handler could not be launched");
                        GlobalActionsDialog.this.mIActivityManager.requestInteractiveBugReport();
                    } catch (RemoteException unused) {
                    }
                }
            }, GlobalActionsDialog.this.mDialogPressDelay);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.LongPressAction
        public boolean onLongPress() {
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                GlobalActionsDialog.this.mMetricsLogger.action(293);
                GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_BUGREPORT_LONG_PRESS);
                GlobalActionsDialog.this.mIActivityManager.requestFullBugReport();
            } catch (RemoteException unused) {
            }
            return false;
        }
    }

    @VisibleForTesting
    BugReportAction makeBugReportActionForTesting() {
        return new BugReportAction();
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class LogoutAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        private LogoutAction() {
            super(R.drawable.ic_expand_more_48dp, R.string.ext_media_move_failure_message);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$LogoutAction$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onPress$0();
                }
            }, GlobalActionsDialog.this.mDialogPressDelay);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onPress$0() {
            try {
                int i = GlobalActionsDialog.this.getCurrentUser().id;
                GlobalActionsDialog.this.mIActivityManager.switchUser(0);
                GlobalActionsDialog.this.mIActivityManager.stopUser(i, true, (IStopUserCallback) null);
            } catch (RemoteException e) {
                Log.e("GlobalActionsDialog", "Couldn't logout user " + e);
            }
        }
    }

    private final class PanicAction extends SinglePressAction implements LongPressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return false;
        }

        private PanicAction() {
            super(com.android.systemui.R.drawable.ic_panic, com.android.systemui.R.string.global_action_panic);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(GlobalActionsDialog.PANIC_PACKAGE, "org.calyxos.ripple.CountDownActivity"));
            intent.addFlags(335544320);
            if (GlobalActionsDialog.this.mContext.getPackageManager().resolveActivity(intent, 0) != null) {
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.LongPressAction
        public boolean onLongPress() {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(GlobalActionsDialog.PANIC_PACKAGE, "org.calyxos.ripple.SettingsActivityLink"));
            intent.addFlags(335544320);
            if (GlobalActionsDialog.this.mContext.getPackageManager().resolveActivity(intent, 0) == null) {
                return false;
            }
            GlobalActionsDialog.this.mContext.startActivity(intent);
            return true;
        }
    }

    private Action getSettingsAction() {
        return new SinglePressAction(R.drawable.ic_menu_mark, R.string.ext_media_move_title) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.3
            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showBeforeProvisioning() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public void onPress() {
                Intent intent = new Intent("android.settings.SETTINGS");
                intent.addFlags(335544320);
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        };
    }

    private Action getAssistAction() {
        return new SinglePressAction(R.drawable.emo_im_winking, R.string.ext_media_checking_notification_message) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.4
            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showBeforeProvisioning() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public void onPress() {
                Intent intent = new Intent("android.intent.action.ASSIST");
                intent.addFlags(335544320);
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        };
    }

    private Action getVoiceAssistAction() {
        return new SinglePressAction(R.drawable.ic_notification_cast_1, R.string.ext_media_nomedia_notification_title) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.5
            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showBeforeProvisioning() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public void onPress() {
                Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
                intent.addFlags(335544320);
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        };
    }

    private Action getOnTheGoAction() {
        return new SinglePressAction(com.android.systemui.R.drawable.ic_lock_onthego, com.android.systemui.R.string.global_action_onthego) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.6
            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showBeforeProvisioning() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
            public void onPress() {
                ComponentName componentName = new ComponentName("com.android.systemui", "com.android.systemui.crdroid.onthego.OnTheGoService");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setAction("start");
                GlobalActionsDialog.this.mContext.startService(intent);
            }
        };
    }

    @VisibleForTesting
    class LockDownAction extends SinglePressAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        LockDownAction() {
            super(R.drawable.ic_contact_picture_holo_light, R.string.ext_media_missing_title);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
            GlobalActionsDialog.this.mLockPatternUtils.requireStrongAuth(32, -1);
            try {
                GlobalActionsDialog.this.mIWindowManager.lockNow((Bundle) null);
                GlobalActionsDialog.this.mBackgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$LockDownAction$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onPress$0();
                    }
                });
            } catch (RemoteException e) {
                Log.e("GlobalActionsDialog", "Error while trying to lock device.", e);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onPress$0() {
            GlobalActionsDialog.this.lockProfiles();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void lockProfiles() {
        int i = getCurrentUser().id;
        for (int i2 : this.mUserManager.getEnabledProfileIds(i)) {
            if (i2 != i) {
                this.mTrustManager.setDeviceLockedForUser(i2, true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UserInfo getCurrentUser() {
        try {
            return this.mIActivityManager.getCurrentUser();
        } catch (RemoteException unused) {
            return null;
        }
    }

    private class CurrentUserProvider {
        private boolean mFetched;
        private UserInfo mUserInfo;

        private CurrentUserProvider() {
            this.mUserInfo = null;
            this.mFetched = false;
        }

        UserInfo get() {
            if (!this.mFetched) {
                this.mFetched = true;
                this.mUserInfo = GlobalActionsDialog.this.getCurrentUser();
            }
            return this.mUserInfo;
        }
    }

    private void addUserActions(List<Action> list, UserInfo userInfo) {
        if (this.mUserManager.isUserSwitcherEnabled()) {
            for (final UserInfo userInfo2 : this.mUserManager.getUsers()) {
                if (userInfo2.supportsSwitchToByUser()) {
                    boolean z = true;
                    if (userInfo != null ? userInfo.id != userInfo2.id : userInfo2.id != 0) {
                        z = false;
                    }
                    String str = userInfo2.iconPath;
                    Drawable drawableCreateFromPath = str != null ? Drawable.createFromPath(str) : null;
                    int i = R.drawable.ic_media_route_connected_light_27_mtrl;
                    StringBuilder sb = new StringBuilder();
                    String str2 = userInfo2.name;
                    if (str2 == null) {
                        str2 = "Primary";
                    }
                    sb.append(str2);
                    sb.append(z ? " ✔" : "");
                    addIfShouldShowAction(list, new SinglePressAction(i, drawableCreateFromPath, sb.toString()) { // from class: com.android.systemui.globalactions.GlobalActionsDialog.7
                        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
                        public boolean showBeforeProvisioning() {
                            return false;
                        }

                        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
                        public boolean showDuringKeyguard() {
                            return true;
                        }

                        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
                        public void onPress() {
                            try {
                                GlobalActionsDialog.this.mIActivityManager.switchUser(userInfo2.id);
                            } catch (RemoteException e) {
                                Log.e("GlobalActionsDialog", "Couldn't switch user " + e);
                            }
                        }
                    });
                }
            }
        }
    }

    private void prepareDialog() {
        refreshSilentMode();
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mLifecycle.setCurrentState(Lifecycle.State.RESUMED);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshSilentMode() {
        if (this.mHasVibrator) {
            return;
        }
        Integer value = this.mRingerModeTracker.getRingerMode().getValue();
        ((ToggleAction) this.mSilentModeAction).updateState(value != null && value.intValue() != 2 ? ToggleState.On : ToggleState.Off);
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        if (this.mDialog == dialogInterface) {
            this.mDialog = null;
        }
        this.mUiEventLogger.log(GlobalActionsEvent.GA_POWER_MENU_CLOSE);
        this.mWindowManagerFuncs.onGlobalActionsHidden();
        this.mLifecycle.setCurrentState(Lifecycle.State.CREATED);
    }

    @Override // android.content.DialogInterface.OnShowListener
    public void onShow(DialogInterface dialogInterface) {
        this.mMetricsLogger.visible(1568);
        this.mUiEventLogger.log(GlobalActionsEvent.GA_POWER_MENU_OPEN);
    }

    public class MyAdapter extends MultiListLayout.MultiListAdapter {
        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public MyAdapter() {
        }

        private int countItems(boolean z) {
            int i = 0;
            for (int i2 = 0; i2 < GlobalActionsDialog.this.mItems.size(); i2++) {
                if (GlobalActionsDialog.this.mItems.get(i2).shouldBeSeparated() == z) {
                    i++;
                }
            }
            return i;
        }

        @Override // com.android.systemui.MultiListLayout.MultiListAdapter
        public int countSeparatedItems() {
            return countItems(true);
        }

        @Override // com.android.systemui.MultiListLayout.MultiListAdapter
        public int countListItems() {
            return countItems(false);
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return countSeparatedItems() + countListItems();
        }

        @Override // android.widget.BaseAdapter, android.widget.ListAdapter
        public boolean isEnabled(int i) {
            return getItem(i).isEnabled();
        }

        @Override // android.widget.Adapter
        public Action getItem(int i) {
            int i2 = 0;
            for (int i3 = 0; i3 < GlobalActionsDialog.this.mItems.size(); i3++) {
                Action action = GlobalActionsDialog.this.mItems.get(i3);
                if (GlobalActionsDialog.this.shouldShowAction(action)) {
                    if (i2 == i) {
                        return action;
                    }
                    i2++;
                }
            }
            throw new IllegalArgumentException("position " + i + " out of range of showable actions, filtered count=" + getCount() + ", keyguardshowing=" + GlobalActionsDialog.this.mKeyguardShowing + ", provisioned=" + GlobalActionsDialog.this.mDeviceProvisioned);
        }

        @Override // android.widget.Adapter
        public View getView(final int i, View view, ViewGroup viewGroup) {
            Action item = getItem(i);
            View viewCreate = item.create(GlobalActionsDialog.this.mContext, view, viewGroup, LayoutInflater.from(GlobalActionsDialog.this.mContext));
            viewCreate.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$MyAdapter$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    this.f$0.lambda$getView$0(i, view2);
                }
            });
            if (item instanceof LongPressAction) {
                viewCreate.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$MyAdapter$$ExternalSyntheticLambda1
                    @Override // android.view.View.OnLongClickListener
                    public final boolean onLongClick(View view2) {
                        return this.f$0.lambda$getView$1(i, view2);
                    }
                });
            }
            return viewCreate;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$getView$0(int i, View view) {
            onClickItem(i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ boolean lambda$getView$1(int i, View view) {
            return onLongClickItem(i);
        }

        public boolean onLongClickItem(int i) {
            Action item = GlobalActionsDialog.this.mAdapter.getItem(i);
            if (!(item instanceof LongPressAction)) {
                return false;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action long-clicked while mDialog is null.");
            }
            return ((LongPressAction) item).onLongPress();
        }

        public void onClickItem(int i) {
            Action item = GlobalActionsDialog.this.mAdapter.getItem(i);
            if (item instanceof SilentModeTriStateAction) {
                return;
            }
            GlobalActionsDialog globalActionsDialog = GlobalActionsDialog.this;
            if (globalActionsDialog.mDialog != null) {
                if (!(item instanceof PowerOptionsAction) && (!(item instanceof RestartAction) || !globalActionsDialog.shouldShowRestartSubmenu())) {
                    GlobalActionsDialog.this.mDialog.dismiss();
                }
            } else {
                Log.w("GlobalActionsDialog", "Action clicked while mDialog is null.");
            }
            item.onPress();
        }

        @Override // com.android.systemui.MultiListLayout.MultiListAdapter
        public boolean shouldBeSeparated(int i) {
            return getItem(i).shouldBeSeparated();
        }
    }

    public class MyPowerOptionsAdapter extends BaseAdapter {
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public MyPowerOptionsAdapter() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return GlobalActionsDialog.this.mPowerItems.size();
        }

        @Override // android.widget.Adapter
        public Action getItem(int i) {
            return GlobalActionsDialog.this.mPowerItems.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(final int i, View view, ViewGroup viewGroup) {
            Action item = getItem(i);
            if (item == null) {
                Log.w("GlobalActionsDialog", "No power options action found at position: " + i);
                return null;
            }
            int i2 = com.android.systemui.R.layout.global_actions_power_item;
            if (view == null) {
                view = LayoutInflater.from(GlobalActionsDialog.this.mContext).inflate(i2, viewGroup, false);
            }
            view.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$MyPowerOptionsAdapter$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    this.f$0.lambda$getView$0(i, view2);
                }
            });
            if (item instanceof LongPressAction) {
                view.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$MyPowerOptionsAdapter$$ExternalSyntheticLambda1
                    @Override // android.view.View.OnLongClickListener
                    public final boolean onLongClick(View view2) {
                        return this.f$0.lambda$getView$1(i, view2);
                    }
                });
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            TextView textView = (TextView) view.findViewById(R.id.message);
            textView.setSelected(true);
            imageView.setImageDrawable(item.getIcon(GlobalActionsDialog.this.mContext));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (item.getMessage() != null) {
                textView.setText(item.getMessage());
            } else {
                textView.setText(item.getMessageResId());
            }
            return view;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$getView$0(int i, View view) {
            onClickItem(i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ boolean lambda$getView$1(int i, View view) {
            return onLongClickItem(i);
        }

        private boolean onLongClickItem(int i) {
            Action item = getItem(i);
            if (!(item instanceof LongPressAction)) {
                return false;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action long-clicked while mDialog is null.");
            }
            return ((LongPressAction) item).onLongPress();
        }

        private void onClickItem(int i) {
            Action item = getItem(i);
            if (item instanceof SilentModeTriStateAction) {
                return;
            }
            GlobalActionsDialog globalActionsDialog = GlobalActionsDialog.this;
            if (globalActionsDialog.mDialog != null && (!(item instanceof RestartAction) || !globalActionsDialog.shouldShowRestartSubmenu())) {
                GlobalActionsDialog.this.mDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action clicked while mDialog is null.");
            }
            item.onPress();
        }
    }

    public class MyRestartOptionsAdapter extends MyPowerOptionsAdapter {
        public MyRestartOptionsAdapter() {
            super();
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.MyPowerOptionsAdapter, android.widget.Adapter
        public int getCount() {
            return GlobalActionsDialog.this.mRestartItems.size();
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.MyPowerOptionsAdapter, android.widget.Adapter
        public Action getItem(int i) {
            return GlobalActionsDialog.this.mRestartItems.get(i);
        }
    }

    public class MyOverflowAdapter extends BaseAdapter {
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        public MyOverflowAdapter() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return GlobalActionsDialog.this.mOverflowItems.size();
        }

        @Override // android.widget.Adapter
        public Action getItem(int i) {
            return GlobalActionsDialog.this.mOverflowItems.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            Action item = getItem(i);
            if (item == null) {
                Log.w("GlobalActionsDialog", "No overflow action found at position: " + i);
                return null;
            }
            int i2 = com.android.systemui.R.layout.controls_more_item;
            if (view == null) {
                view = LayoutInflater.from(GlobalActionsDialog.this.mContext).inflate(i2, viewGroup, false);
            }
            TextView textView = (TextView) view;
            if (item.getMessageResId() != 0) {
                textView.setText(item.getMessageResId());
            } else {
                textView.setText(item.getMessage());
            }
            return textView;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean onLongClickItem(int i) {
            Action item = getItem(i);
            if (!(item instanceof LongPressAction)) {
                return false;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action long-clicked while mDialog is null.");
            }
            return ((LongPressAction) item).onLongPress();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onClickItem(int i) {
            Action item = getItem(i);
            if (item instanceof SilentModeTriStateAction) {
                return;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action clicked while mDialog is null.");
            }
            item.onPress();
        }
    }

    private abstract class SinglePressAction implements Action {
        private final Drawable mIcon;
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean isEnabled() {
            return true;
        }

        protected SinglePressAction(int i, int i2) {
            this.mIconResId = i;
            this.mMessageResId = i2;
            this.mMessage = null;
            this.mIcon = null;
        }

        protected SinglePressAction(int i, Drawable drawable, CharSequence charSequence) {
            this.mIconResId = i;
            this.mMessageResId = 0;
            this.mMessage = charSequence;
            this.mIcon = drawable;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public int getMessageResId() {
            return this.mMessageResId;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public CharSequence getMessage() {
            return this.mMessage;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public Drawable getIcon(Context context) {
            Drawable drawable = this.mIcon;
            return drawable != null ? drawable : context.getDrawable(this.mIconResId);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            View viewInflate = layoutInflater.inflate(com.android.systemui.R.layout.global_actions_grid_item_v2, viewGroup, false);
            viewInflate.getBackground().setAlpha(GlobalActionsDialog.this.mNotificationBackgroundAlpha);
            ImageView imageView = (ImageView) viewInflate.findViewById(R.id.icon);
            TextView textView = (TextView) viewInflate.findViewById(R.id.message);
            textView.setSelected(true);
            imageView.setImageDrawable(getIcon(context));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            CharSequence charSequence = this.mMessage;
            if (charSequence != null) {
                textView.setText(charSequence);
            } else {
                textView.setText(this.mMessageResId);
            }
            return viewInflate;
        }
    }

    private enum ToggleState {
        Off(false),
        TurningOn(true),
        TurningOff(true),
        On(false);

        private final boolean mInTransition;

        ToggleState(boolean z) {
            this.mInTransition = z;
        }

        public boolean inTransition() {
            return this.mInTransition;
        }
    }

    private abstract class ToggleAction implements Action {
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected ToggleState mState = ToggleState.Off;

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public CharSequence getMessage() {
            return null;
        }

        abstract void onToggle(boolean z);

        void willCreate() {
        }

        public ToggleAction(int i, int i2, int i3, int i4, int i5) {
            this.mEnabledIconResId = i;
            this.mDisabledIconResid = i2;
            this.mMessageResId = i3;
            this.mEnabledStatusMessageResId = i4;
            this.mDisabledStatusMessageResId = i5;
        }

        private boolean isOn() {
            ToggleState toggleState = this.mState;
            return toggleState == ToggleState.On || toggleState == ToggleState.TurningOn;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public int getMessageResId() {
            return isOn() ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId;
        }

        private int getIconResId() {
            return isOn() ? this.mEnabledIconResId : this.mDisabledIconResid;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public Drawable getIcon(Context context) {
            return context.getDrawable(getIconResId());
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            willCreate();
            View viewInflate = layoutInflater.inflate(com.android.systemui.R.layout.global_actions_grid_item_v2, viewGroup, false);
            ImageView imageView = (ImageView) viewInflate.findViewById(R.id.icon);
            TextView textView = (TextView) viewInflate.findViewById(R.id.message);
            boolean zIsEnabled = isEnabled();
            if (textView != null) {
                textView.setText(getMessageResId());
                textView.setEnabled(zIsEnabled);
                textView.setSelected(true);
            }
            if (imageView != null) {
                imageView.setImageDrawable(context.getDrawable(getIconResId()));
                imageView.setEnabled(zIsEnabled);
            }
            viewInflate.setEnabled(zIsEnabled);
            return viewInflate;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public final void onPress() {
            if (this.mState.inTransition()) {
                Log.w("GlobalActionsDialog", "shouldn't be able to toggle when in transition");
                return;
            }
            boolean z = this.mState != ToggleState.On;
            onToggle(z);
            changeStateFromPress(z);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean isEnabled() {
            return !this.mState.inTransition();
        }

        protected void changeStateFromPress(boolean z) {
            this.mState = z ? ToggleState.On : ToggleState.Off;
        }

        public void updateState(ToggleState toggleState) {
            this.mState = toggleState;
        }
    }

    private class AirplaneModeAction extends ToggleAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        AirplaneModeAction() {
            super(R.drawable.ic_commit_search_api_mtrl_alpha, R.drawable.ic_contact_picture_180_holo_dark, R.string.ext_media_status_checking, R.string.ext_media_status_bad_removal, R.string.ext_media_seamless_action);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.ToggleAction
        void onToggle(boolean z) {
            if (!GlobalActionsDialog.this.mHasTelephony || !((Boolean) TelephonyProperties.in_ecm_mode().orElse(Boolean.FALSE)).booleanValue()) {
                GlobalActionsDialog.this.changeAirplaneModeSystemSetting(z);
                return;
            }
            GlobalActionsDialog.this.mIsWaitingForEcmExit = true;
            Intent intent = new Intent("android.telephony.action.SHOW_NOTICE_ECM_BLOCK_OTHERS", (Uri) null);
            intent.addFlags(268435456);
            GlobalActionsDialog.this.mContext.startActivity(intent);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.ToggleAction
        protected void changeStateFromPress(boolean z) {
            if (GlobalActionsDialog.this.mHasTelephony && !((Boolean) TelephonyProperties.in_ecm_mode().orElse(Boolean.FALSE)).booleanValue()) {
                ToggleState toggleState = z ? ToggleState.TurningOn : ToggleState.TurningOff;
                this.mState = toggleState;
                GlobalActionsDialog.this.mAirplaneState = toggleState;
            }
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        public SilentModeToggleAction() {
            super(R.drawable.fastscroll_label_left_holo_light, R.drawable.fastscroll_label_left_holo_dark, R.string.ext_media_nomedia_notification_message, R.string.ext_media_new_notification_title, R.string.ext_media_new_notification_message);
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.ToggleAction
        void onToggle(boolean z) {
            if (z) {
                GlobalActionsDialog.this.mAudioManager.setRingerMode(0);
            } else {
                GlobalActionsDialog.this.mAudioManager.setRingerMode(2);
            }
        }
    }

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {
        private final int[] ITEM_IDS = {R.id.messaging_group_sending_progress_container, R.id.mic, R.id.micro};
        private final AudioManager mAudioManager;
        private final Handler mHandler;

        private int indexToRingerMode(int i) {
            return i;
        }

        private int ringerModeToIndex(int i) {
            return i;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public CharSequence getMessage() {
            return null;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public int getMessageResId() {
            return 0;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean isEnabled() {
            return true;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public void onPress() {
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        SilentModeTriStateAction(AudioManager audioManager, Handler handler) {
            this.mAudioManager = audioManager;
            this.mHandler = handler;
        }

        @Override // com.android.systemui.globalactions.GlobalActionsDialog.Action
        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            View viewInflate = layoutInflater.inflate(R.layout.floating_popup_open_overflow_button, viewGroup, false);
            int iRingerModeToIndex = ringerModeToIndex(this.mAudioManager.getRingerMode());
            int i = 0;
            while (i < 3) {
                View viewFindViewById = viewInflate.findViewById(this.ITEM_IDS[i]);
                viewFindViewById.setSelected(iRingerModeToIndex == i);
                viewFindViewById.setTag(Integer.valueOf(i));
                viewFindViewById.setOnClickListener(this);
                i++;
            }
            return viewInflate;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view.getTag() instanceof Integer) {
                this.mAudioManager.setRingerMode(indexToRingerMode(((Integer) view.getTag()).intValue()));
                this.mHandler.sendEmptyMessageDelayed(0, 300L);
            }
        }
    }

    @VisibleForTesting
    void setZeroDialogPressDelayForTesting() {
        this.mDialogPressDelay = 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAirplaneModeChanged() {
        if (this.mHasTelephony) {
            return;
        }
        ToggleState toggleState = Settings.Global.getInt(this.mContentResolver, "airplane_mode_on", 0) == 1 ? ToggleState.On : ToggleState.Off;
        this.mAirplaneState = toggleState;
        this.mAirplaneModeOn.updateState(toggleState);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeAirplaneModeSystemSetting(boolean z) {
        Settings.Global.putInt(this.mContentResolver, "airplane_mode_on", z ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra("state", z);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (this.mHasTelephony) {
            return;
        }
        this.mAirplaneState = z ? ToggleState.On : ToggleState.Off;
    }

    @Override // androidx.lifecycle.LifecycleOwner
    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }

    @VisibleForTesting
    static final class ActionsDialog extends Dialog implements ColorExtractor.OnColorsChangedListener {
        private final MyAdapter mAdapter;
        private Drawable mBackgroundDrawable;
        private final SysuiColorExtractor mColorExtractor;
        private ViewGroup mContainer;
        private final Context mContext;
        private final boolean mControlsAvailable;
        private ControlsUiController mControlsUiController;
        private ViewGroup mControlsView;
        private final NotificationShadeDepthController mDepthController;
        private MultiListLayout mGlobalActionsLayout;
        private boolean mKeyguardShowing;
        private TextView mLockMessage;

        @VisibleForTesting
        ViewGroup mLockMessageContainer;
        private final NotificationShadeWindowController mNotificationShadeWindowController;
        private final Runnable mOnRotateCallback;
        private final MyOverflowAdapter mOverflowAdapter;
        private ListPopupWindow mOverflowPopup;
        private final MyPowerOptionsAdapter mPowerOptionsAdapter;
        private Dialog mPowerOptionsDialog;
        private ResetOrientationData mResetOrientationData;
        private final MyRestartOptionsAdapter mRestartOptionsAdapter;
        private Dialog mRestartOptionsDialog;
        private float mScrimAlpha;
        private boolean mShowing;
        private final IStatusBarService mStatusBarService;
        private final SysUiState mSysUiState;
        private final IBinder mToken;
        private final Provider<GlobalActionsPanelPlugin.PanelViewController> mWalletFactory;
        private GlobalActionsPanelPlugin.PanelViewController mWalletViewController;

        ActionsDialog(Context context, MyAdapter myAdapter, MyOverflowAdapter myOverflowAdapter, Provider<GlobalActionsPanelPlugin.PanelViewController> provider, NotificationShadeDepthController notificationShadeDepthController, SysuiColorExtractor sysuiColorExtractor, IStatusBarService iStatusBarService, NotificationShadeWindowController notificationShadeWindowController, boolean z, ControlsUiController controlsUiController, SysUiState sysUiState, Runnable runnable, boolean z2, MyPowerOptionsAdapter myPowerOptionsAdapter, MyRestartOptionsAdapter myRestartOptionsAdapter) {
            super(context, com.android.systemui.R.style.Theme_SystemUI_Dialog_GlobalActions);
            this.mToken = new Binder();
            this.mContext = context;
            this.mAdapter = myAdapter;
            this.mOverflowAdapter = myOverflowAdapter;
            this.mPowerOptionsAdapter = myPowerOptionsAdapter;
            this.mRestartOptionsAdapter = myRestartOptionsAdapter;
            this.mDepthController = notificationShadeDepthController;
            this.mColorExtractor = sysuiColorExtractor;
            this.mStatusBarService = iStatusBarService;
            this.mNotificationShadeWindowController = notificationShadeWindowController;
            this.mControlsAvailable = z;
            this.mControlsUiController = controlsUiController;
            this.mSysUiState = sysUiState;
            this.mOnRotateCallback = runnable;
            this.mKeyguardShowing = z2;
            this.mWalletFactory = provider;
            Window window = getWindow();
            window.requestFeature(1);
            window.getDecorView();
            window.getAttributes().systemUiVisibility |= 1792;
            window.setLayout(-1, -1);
            window.clearFlags(2);
            window.addFlags(17629472);
            window.setType(2020);
            window.getAttributes().setFitInsetsTypes(0);
            setTitle(R.string.ext_media_ready_notification_message);
            initializeLayout();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isShowingControls() {
            return this.mControlsUiController != null;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showControls(ControlsUiController controlsUiController) {
            this.mControlsUiController = controlsUiController;
            controlsUiController.show(this.mControlsView, new GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda11(this));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isWalletViewAvailable() {
            GlobalActionsPanelPlugin.PanelViewController panelViewController = this.mWalletViewController;
            return (panelViewController == null || panelViewController.getPanelContent() == null) ? false : true;
        }

        private void initializeWalletView() {
            this.mWalletViewController = this.mWalletFactory.get();
            if (isWalletViewAvailable()) {
                int rotation = RotationUtils.getRotation(this.mContext);
                boolean zIsRotationLocked = RotationPolicy.isRotationLocked(this.mContext);
                if (rotation != 0) {
                    if (zIsRotationLocked) {
                        if (this.mResetOrientationData == null) {
                            ResetOrientationData resetOrientationData = new ResetOrientationData();
                            this.mResetOrientationData = resetOrientationData;
                            resetOrientationData.locked = true;
                            resetOrientationData.rotation = rotation;
                        }
                        this.mGlobalActionsLayout.post(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda14
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.lambda$initializeWalletView$0();
                            }
                        });
                        return;
                    }
                    return;
                }
                if (!zIsRotationLocked) {
                    if (this.mResetOrientationData == null) {
                        ResetOrientationData resetOrientationData2 = new ResetOrientationData();
                        this.mResetOrientationData = resetOrientationData2;
                        resetOrientationData2.locked = false;
                    }
                    this.mGlobalActionsLayout.post(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda15
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$initializeWalletView$1();
                        }
                    });
                }
                setRotationSuggestionsEnabled(false);
                FrameLayout frameLayout = (FrameLayout) findViewById(com.android.systemui.R.id.global_actions_wallet);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
                if (!this.mControlsAvailable) {
                    layoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.global_actions_wallet_top_margin);
                }
                View panelContent = this.mWalletViewController.getPanelContent();
                frameLayout.addView(panelContent, layoutParams);
                final ViewGroup viewGroup = (ViewGroup) findViewById(com.android.systemui.R.id.global_actions_grid_root);
                if (viewGroup != null) {
                    panelContent.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda7
                        @Override // android.view.View.OnLayoutChangeListener
                        public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                            GlobalActionsDialog.ActionsDialog.lambda$initializeWalletView$2(viewGroup, view, i, i2, i3, i4, i5, i6, i7, i8);
                        }
                    });
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$initializeWalletView$0() {
            RotationPolicy.setRotationLockAtAngle(this.mContext, false, 0);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$initializeWalletView$1() {
            RotationPolicy.setRotationLockAtAngle(this.mContext, true, 0);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$initializeWalletView$2(ViewGroup viewGroup, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            int i9 = i8 - i6;
            int i10 = i4 - i2;
            if (i9 <= 0 || i9 == i10) {
                return;
            }
            TransitionManager.beginDelayedTransition(viewGroup, new AutoTransition().setDuration(250L).setOrdering(0));
        }

        private ListPopupWindow createPowerOverflowPopup() {
            GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(new ContextThemeWrapper(this.mContext, com.android.systemui.R.style.Control_ListPopupWindow), false);
            globalActionsPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda8
                @Override // android.widget.AdapterView.OnItemClickListener
                public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                    this.f$0.lambda$createPowerOverflowPopup$3(adapterView, view, i, j);
                }
            });
            globalActionsPopupMenu.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda9
                @Override // android.widget.AdapterView.OnItemLongClickListener
                public final boolean onItemLongClick(AdapterView adapterView, View view, int i, long j) {
                    return this.f$0.lambda$createPowerOverflowPopup$4(adapterView, view, i, j);
                }
            });
            globalActionsPopupMenu.setAnchorView(findViewById(com.android.systemui.R.id.global_actions_overflow_button));
            globalActionsPopupMenu.setAdapter(this.mOverflowAdapter);
            return globalActionsPopupMenu;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$createPowerOverflowPopup$3(AdapterView adapterView, View view, int i, long j) {
            this.mOverflowAdapter.onClickItem(i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ boolean lambda$createPowerOverflowPopup$4(AdapterView adapterView, View view, int i, long j) {
            return this.mOverflowAdapter.onLongClickItem(i);
        }

        public void showPowerOptionsMenu() {
            Dialog dialogCreate = GlobalActionsPowerDialog.create(this.mContext, this.mPowerOptionsAdapter);
            this.mPowerOptionsDialog = dialogCreate;
            dialogCreate.show();
        }

        public void showRestartOptionsMenu() {
            Dialog dialogCreate = GlobalActionsPowerDialog.create(this.mContext, this.mRestartOptionsAdapter);
            this.mRestartOptionsDialog = dialogCreate;
            dialogCreate.show();
        }

        private void showPowerOverflowMenu() {
            ListPopupWindow listPopupWindowCreatePowerOverflowPopup = createPowerOverflowPopup();
            this.mOverflowPopup = listPopupWindowCreatePowerOverflowPopup;
            listPopupWindowCreatePowerOverflowPopup.show();
        }

        private void initializeLayout() {
            setContentView(com.android.systemui.R.layout.global_actions_grid_v2);
            fixNavBarClipping();
            this.mControlsView = (ViewGroup) findViewById(com.android.systemui.R.id.global_actions_controls);
            MultiListLayout multiListLayout = (MultiListLayout) findViewById(com.android.systemui.R.id.global_actions_view);
            this.mGlobalActionsLayout = multiListLayout;
            multiListLayout.setOutsideTouchListener(new View.OnClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$initializeLayout$5(view);
                }
            });
            this.mGlobalActionsLayout.setListViewAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.ActionsDialog.1
                @Override // android.view.View.AccessibilityDelegate
                public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
                    accessibilityEvent.getText().add(ActionsDialog.this.mContext.getString(R.string.ext_media_ready_notification_message));
                    return true;
                }
            });
            this.mGlobalActionsLayout.setRotationListener(new MultiListLayout.RotationListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda10
                @Override // com.android.systemui.MultiListLayout.RotationListener
                public final void onRotate(int i, int i2) {
                    this.f$0.onRotate(i, i2);
                }
            });
            this.mGlobalActionsLayout.setAdapter(this.mAdapter);
            this.mContainer = (ViewGroup) findViewById(com.android.systemui.R.id.global_actions_container);
            this.mLockMessageContainer = (ViewGroup) requireViewById(com.android.systemui.R.id.global_actions_lock_message_container);
            this.mLockMessage = (TextView) requireViewById(com.android.systemui.R.id.global_actions_lock_message);
            View viewFindViewById = findViewById(com.android.systemui.R.id.global_actions_overflow_button);
            if (viewFindViewById != null) {
                if (this.mOverflowAdapter.getCount() > 0) {
                    viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda5
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            this.f$0.lambda$initializeLayout$6(view);
                        }
                    });
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mGlobalActionsLayout.getLayoutParams();
                    layoutParams.setMarginEnd(0);
                    this.mGlobalActionsLayout.setLayoutParams(layoutParams);
                } else {
                    viewFindViewById.setVisibility(8);
                    LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mGlobalActionsLayout.getLayoutParams();
                    layoutParams2.setMarginEnd(this.mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.global_actions_side_margin));
                    this.mGlobalActionsLayout.setLayoutParams(layoutParams2);
                }
            }
            initializeWalletView();
            ((View) this.mGlobalActionsLayout.getParent()).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$initializeLayout$7(view);
                }
            });
            View viewFindViewById2 = findViewById(com.android.systemui.R.id.global_actions_grid_root);
            if (viewFindViewById2 != null) {
                viewFindViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda4
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.f$0.lambda$initializeLayout$8(view);
                    }
                });
            }
            if (this.mBackgroundDrawable == null) {
                this.mBackgroundDrawable = new ScrimDrawable();
                this.mScrimAlpha = 0.54f;
            }
            getWindow().setBackgroundDrawable(this.mBackgroundDrawable);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$initializeLayout$5(View view) {
            dismiss();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$initializeLayout$6(View view) {
            showPowerOverflowMenu();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$initializeLayout$7(View view) {
            dismiss();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$initializeLayout$8(View view) {
            dismiss();
        }

        private void fixNavBarClipping() {
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.content);
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
            ViewGroup viewGroup2 = (ViewGroup) viewGroup.getParent();
            viewGroup2.setClipChildren(false);
            viewGroup2.setClipToPadding(false);
        }

        @Override // android.app.Dialog
        protected void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
            this.mGlobalActionsLayout.updateList();
            if (this.mBackgroundDrawable instanceof ScrimDrawable) {
                this.mColorExtractor.addOnColorsChangedListener(this);
                updateColors(this.mColorExtractor.getNeutralColors(), false);
            }
        }

        private void updateColors(ColorExtractor.GradientColors gradientColors, boolean z) {
            if (this.mBackgroundDrawable instanceof ScrimDrawable) {
                View decorView = getWindow().getDecorView();
                if (gradientColors.supportsDarkText()) {
                    decorView.setSystemUiVisibility(8208);
                } else {
                    decorView.setSystemUiVisibility(0);
                }
            }
        }

        @Override // android.app.Dialog
        protected void onStop() {
            super.onStop();
            this.mColorExtractor.removeOnColorsChangedListener(this);
        }

        @Override // android.app.Dialog
        public void show() {
            super.show();
            this.mShowing = true;
            this.mNotificationShadeWindowController.setRequestTopUi(true, "GlobalActionsDialog");
            this.mSysUiState.setFlag(32768, true).commitUpdate(this.mContext.getDisplayId());
            final ViewGroup viewGroup = (ViewGroup) this.mGlobalActionsLayout.getRootView();
            viewGroup.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda2
                @Override // android.view.View.OnApplyWindowInsetsListener
                public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    return GlobalActionsDialog.ActionsDialog.lambda$show$9(viewGroup, view, windowInsets);
                }
            });
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.show(this.mControlsView, new GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda11(this));
            }
            this.mBackgroundDrawable.setAlpha(0);
            float animationOffsetX = this.mGlobalActionsLayout.getAnimationOffsetX();
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mContainer, "alpha", 0.0f, 1.0f);
            Interpolator interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            objectAnimatorOfFloat.setInterpolator(interpolator);
            objectAnimatorOfFloat.setDuration(183L);
            objectAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$show$10(valueAnimator);
                }
            });
            ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(this.mContainer, "translationX", animationOffsetX, 0.0f);
            objectAnimatorOfFloat2.setInterpolator(interpolator);
            objectAnimatorOfFloat2.setDuration(350L);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2);
            animatorSet.start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ WindowInsets lambda$show$9(ViewGroup viewGroup, View view, WindowInsets windowInsets) {
            viewGroup.setPadding(windowInsets.getStableInsetLeft(), windowInsets.getStableInsetTop(), windowInsets.getStableInsetRight(), windowInsets.getStableInsetBottom());
            return WindowInsets.CONSUMED;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$show$10(ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();
            this.mBackgroundDrawable.setAlpha((int) (this.mScrimAlpha * animatedFraction * 255.0f));
            this.mDepthController.updateGlobalDialogVisibility(animatedFraction, this.mGlobalActionsLayout);
        }

        @Override // android.app.Dialog, android.content.DialogInterface
        public void dismiss() {
            dismissWithAnimation(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda13
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$dismiss$12();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$dismiss$12() {
            this.mContainer.setTranslationX(0.0f);
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mContainer, "alpha", 1.0f, 0.0f);
            Interpolator interpolator = Interpolators.FAST_OUT_LINEAR_IN;
            objectAnimatorOfFloat.setInterpolator(interpolator);
            objectAnimatorOfFloat.setDuration(233L);
            objectAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$dismiss$11(valueAnimator);
                }
            });
            ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(this.mContainer, "translationX", 0.0f, this.mGlobalActionsLayout.getAnimationOffsetX());
            objectAnimatorOfFloat2.setInterpolator(interpolator);
            objectAnimatorOfFloat2.setDuration(350L);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2);
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.ActionsDialog.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    ActionsDialog.this.completeDismiss();
                }
            });
            animatorSet.start();
            dismissOverflow(false);
            dismissPowerOptions(false);
            dismissRestartOptions(false);
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.closeDialogs(false);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$dismiss$11(ValueAnimator valueAnimator) {
            float animatedFraction = 1.0f - valueAnimator.getAnimatedFraction();
            this.mBackgroundDrawable.setAlpha((int) (this.mScrimAlpha * animatedFraction * 255.0f));
            this.mDepthController.updateGlobalDialogVisibility(animatedFraction, this.mGlobalActionsLayout);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void dismissForControlsActivity() {
            dismissWithAnimation(new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda16
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$dismissForControlsActivity$13();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$dismissForControlsActivity$13() {
            ControlsAnimations.exitAnimation((ViewGroup) this.mGlobalActionsLayout.getParent(), new Runnable() { // from class: com.android.systemui.globalactions.GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.completeDismiss();
                }
            }).start();
        }

        void dismissWithAnimation(Runnable runnable) {
            if (this.mShowing) {
                this.mShowing = false;
                runnable.run();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void completeDismiss() {
            this.mShowing = false;
            resetOrientation();
            dismissWallet();
            dismissOverflow(true);
            dismissPowerOptions(true);
            dismissRestartOptions(true);
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.hide();
            }
            this.mNotificationShadeWindowController.setRequestTopUi(false, "GlobalActionsDialog");
            this.mDepthController.updateGlobalDialogVisibility(0.0f, null);
            this.mSysUiState.setFlag(32768, false).commitUpdate(this.mContext.getDisplayId());
            super.dismiss();
        }

        private void dismissWallet() {
            GlobalActionsPanelPlugin.PanelViewController panelViewController = this.mWalletViewController;
            if (panelViewController != null) {
                panelViewController.onDismissed();
                this.mWalletViewController = null;
            }
        }

        private void dismissOverflow(boolean z) {
            ListPopupWindow listPopupWindow = this.mOverflowPopup;
            if (listPopupWindow != null) {
                if (z) {
                    listPopupWindow.dismissImmediate();
                } else {
                    listPopupWindow.dismiss();
                }
            }
        }

        private void dismissPowerOptions(boolean z) {
            Dialog dialog = this.mPowerOptionsDialog;
            if (dialog != null) {
                if (z) {
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }
        }

        private void dismissRestartOptions(boolean z) {
            Dialog dialog = this.mRestartOptionsDialog;
            if (dialog != null) {
                if (z) {
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }
        }

        private void setRotationSuggestionsEnabled(boolean z) {
            try {
                this.mStatusBarService.disable2ForUser(z ? 0 : 16, this.mToken, this.mContext.getPackageName(), Binder.getCallingUserHandle().getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        private void resetOrientation() {
            ResetOrientationData resetOrientationData = this.mResetOrientationData;
            if (resetOrientationData != null) {
                RotationPolicy.setRotationLockAtAngle(this.mContext, resetOrientationData.locked, resetOrientationData.rotation);
            }
            setRotationSuggestionsEnabled(true);
        }

        public void onColorsChanged(ColorExtractor colorExtractor, int i) {
            if (this.mKeyguardShowing) {
                if ((i & 2) != 0) {
                    updateColors(colorExtractor.getColors(2), true);
                }
            } else if ((i & 1) != 0) {
                updateColors(colorExtractor.getColors(1), true);
            }
        }

        public void refreshDialog() {
            dismissWallet();
            dismissOverflow(true);
            dismissPowerOptions(true);
            dismissRestartOptions(true);
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.hide();
            }
            initializeLayout();
            this.mGlobalActionsLayout.updateList();
            ControlsUiController controlsUiController2 = this.mControlsUiController;
            if (controlsUiController2 != null) {
                controlsUiController2.show(this.mControlsView, new GlobalActionsDialog$ActionsDialog$$ExternalSyntheticLambda11(this));
            }
        }

        public void onRotate(int i, int i2) {
            if (this.mShowing) {
                this.mOnRotateCallback.run();
                refreshDialog();
            }
        }

        void hideLockMessage() {
            if (this.mLockMessageContainer.getVisibility() == 0) {
                this.mLockMessageContainer.animate().alpha(0.0f).setDuration(150L).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.globalactions.GlobalActionsDialog.ActionsDialog.3
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        ActionsDialog.this.mLockMessageContainer.setVisibility(8);
                    }
                }).start();
            }
        }

        void showLockMessage() {
            Drawable drawable = this.mContext.getDrawable(R.drawable.ic_commit_search_api_material);
            drawable.setTint(this.mContext.getColor(com.android.systemui.R.color.control_primary_text));
            this.mLockMessage.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, drawable, (Drawable) null, (Drawable) null);
            this.mLockMessageContainer.setVisibility(0);
        }

        private static class ResetOrientationData {
            public boolean locked;
            public int rotation;

            private ResetOrientationData() {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldShowControls() {
        boolean z = this.mShowLockScreenCardsAndControls && this.mLockPatternUtils.getStrongAuthForUser(getCurrentUser().id) != 1;
        if (controlsAvailable()) {
            return this.mKeyguardStateController.isUnlocked() || z;
        }
        return false;
    }

    private boolean controlsAvailable() {
        return this.mDeviceProvisioned && this.mControlsUiControllerOptional.isPresent() && this.mControlsUiControllerOptional.get().getAvailable() && !this.mControlsServiceInfos.isEmpty();
    }

    private boolean shouldShowLockMessage(ActionsDialog actionsDialog) {
        boolean z = this.mLockPatternUtils.getStrongAuthForUser(getCurrentUser().id) == 1;
        if (this.mKeyguardStateController.isUnlocked()) {
            return false;
        }
        if (!this.mShowLockScreenCardsAndControls || z) {
            return controlsAvailable() || actionsDialog.isWalletViewAvailable();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPowerMenuLockScreenSettingsChanged() {
        this.mShowLockScreenCardsAndControls = Settings.Secure.getInt(this.mContentResolver, "power_menu_locked_show_content", 0) != 0;
    }
}
