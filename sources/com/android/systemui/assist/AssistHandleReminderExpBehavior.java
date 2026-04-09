package com.android.systemui.assist;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import androidx.slice.Clock;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import dagger.Lazy;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
final class AssistHandleReminderExpBehavior implements AssistHandleBehaviorController.BehaviorController {
    private static final String[] DEFAULT_HOME_CHANGE_ACTIONS;
    private static final long DEFAULT_SHOW_AND_GO_DELAYED_LONG_DELAY_MS;
    private static final long DEFAULT_SHOW_AND_GO_DELAY_RESET_TIMEOUT_MS;
    private final Lazy<ActivityManagerWrapper> mActivityManagerWrapper;
    private AssistHandleCallbacks mAssistHandleCallbacks;
    private final Lazy<BootCompleteCache> mBootCompleteCache;
    private final Lazy<BroadcastDispatcher> mBroadcastDispatcher;
    private final Clock mClock;
    private int mConsecutiveTaskSwitches;
    private Context mContext;
    private ComponentName mDefaultHome;
    private final DeviceConfigHelper mDeviceConfigHelper;
    private final Handler mHandler;
    private boolean mIsAwake;
    private boolean mIsDozing;
    private boolean mIsLauncherShowing;
    private boolean mIsLearned;
    private boolean mIsNavBarHidden;
    private long mLastLearningTimestamp;
    private long mLearnedHintLastShownEpochDay;
    private int mLearningCount;
    private long mLearningTimeElapsed;
    private boolean mOnLockscreen;
    private final Lazy<OverviewProxyService> mOverviewProxyService;
    private final Lazy<PackageManagerWrapper> mPackageManagerWrapper;
    private int mRunningTaskId;
    private ContentObserver mSettingObserver;
    private final Lazy<StatusBarStateController> mStatusBarStateController;
    private final Lazy<SysUiState> mSysUiFlagContainer;
    private final Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    private static final Uri LEARNING_TIME_ELAPSED_URI = Settings.Secure.getUriFor("reminder_exp_learning_time_elapsed");
    private static final Uri LEARNING_EVENT_COUNT_URI = Settings.Secure.getUriFor("reminder_exp_learning_event_count");
    private static final long DEFAULT_LEARNING_TIME_MS = TimeUnit.DAYS.toMillis(10);
    private final StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior.1
        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            AssistHandleReminderExpBehavior.this.handleStatusBarStateChanged(i);
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozingChanged(boolean z) {
            AssistHandleReminderExpBehavior.this.handleDozingChanged(z);
        }
    };
    private final TaskStackChangeListener mTaskStackChangeListener = new TaskStackChangeListener() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior.2
        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            AssistHandleReminderExpBehavior.this.handleTaskStackTopChanged(runningTaskInfo.taskId, runningTaskInfo.topActivity);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskCreated(int i, ComponentName componentName) {
            AssistHandleReminderExpBehavior.this.handleTaskStackTopChanged(i, componentName);
        }
    };
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior.3
        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onOverviewShown(boolean z) {
            AssistHandleReminderExpBehavior.this.handleOverviewShown();
        }
    };
    private final SysUiState.SysUiStateCallback mSysUiStateCallback = new SysUiState.SysUiStateCallback() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior$$ExternalSyntheticLambda0
        @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
        public final void onSystemUiStateChanged(int i) {
            this.f$0.handleSystemUiStateChanged(i);
        }
    };
    private final WakefulnessLifecycle.Observer mWakefulnessLifecycleObserver = new WakefulnessLifecycle.Observer() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior.4
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(false);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(true);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(false);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(false);
        }
    };
    private final BroadcastReceiver mDefaultHomeBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            AssistHandleReminderExpBehavior assistHandleReminderExpBehavior = AssistHandleReminderExpBehavior.this;
            assistHandleReminderExpBehavior.mDefaultHome = assistHandleReminderExpBehavior.getCurrentDefaultHome();
        }
    };
    private final BootCompleteCache.BootCompleteListener mBootCompleteListener = new BootCompleteCache.BootCompleteListener() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior.6
        @Override // com.android.systemui.BootCompleteCache.BootCompleteListener
        public void onBootComplete() {
            AssistHandleReminderExpBehavior assistHandleReminderExpBehavior = AssistHandleReminderExpBehavior.this;
            assistHandleReminderExpBehavior.mDefaultHome = assistHandleReminderExpBehavior.getCurrentDefaultHome();
        }
    };
    private final Runnable mResetConsecutiveTaskSwitches = new Runnable() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior$$ExternalSyntheticLambda1
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.resetConsecutiveTaskSwitches();
        }
    };
    private final IntentFilter mDefaultHomeIntentFilter = new IntentFilter();

    private boolean onLockscreen(int i) {
        return i == 1 || i == 2;
    }

    static {
        TimeUnit timeUnit = TimeUnit.SECONDS;
        DEFAULT_SHOW_AND_GO_DELAYED_LONG_DELAY_MS = timeUnit.toMillis(1L);
        DEFAULT_SHOW_AND_GO_DELAY_RESET_TIMEOUT_MS = timeUnit.toMillis(3L);
        DEFAULT_HOME_CHANGE_ACTIONS = new String[]{"android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED", "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED"};
    }

    AssistHandleReminderExpBehavior(Clock clock, Handler handler, DeviceConfigHelper deviceConfigHelper, Lazy<StatusBarStateController> lazy, Lazy<ActivityManagerWrapper> lazy2, Lazy<OverviewProxyService> lazy3, Lazy<SysUiState> lazy4, Lazy<WakefulnessLifecycle> lazy5, Lazy<PackageManagerWrapper> lazy6, Lazy<BroadcastDispatcher> lazy7, Lazy<BootCompleteCache> lazy8) {
        this.mClock = clock;
        this.mHandler = handler;
        this.mDeviceConfigHelper = deviceConfigHelper;
        this.mStatusBarStateController = lazy;
        this.mActivityManagerWrapper = lazy2;
        this.mOverviewProxyService = lazy3;
        this.mSysUiFlagContainer = lazy4;
        this.mWakefulnessLifecycle = lazy5;
        this.mPackageManagerWrapper = lazy6;
        for (String str : DEFAULT_HOME_CHANGE_ACTIONS) {
            this.mDefaultHomeIntentFilter.addAction(str);
        }
        this.mBroadcastDispatcher = lazy7;
        this.mBootCompleteCache = lazy8;
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks) {
        this.mContext = context;
        this.mAssistHandleCallbacks = assistHandleCallbacks;
        this.mConsecutiveTaskSwitches = 0;
        this.mBootCompleteCache.get().addListener(this.mBootCompleteListener);
        this.mDefaultHome = getCurrentDefaultHome();
        this.mBroadcastDispatcher.get().registerReceiver(this.mDefaultHomeBroadcastReceiver, this.mDefaultHomeIntentFilter);
        this.mOnLockscreen = onLockscreen(this.mStatusBarStateController.get().getState());
        this.mIsDozing = this.mStatusBarStateController.get().isDozing();
        this.mStatusBarStateController.get().addCallback(this.mStatusBarStateListener);
        ActivityManager.RunningTaskInfo runningTask = this.mActivityManagerWrapper.get().getRunningTask();
        this.mRunningTaskId = runningTask == null ? 0 : runningTask.taskId;
        this.mActivityManagerWrapper.get().registerTaskStackListener(this.mTaskStackChangeListener);
        this.mOverviewProxyService.get().addCallback(this.mOverviewProxyListener);
        this.mSysUiFlagContainer.get().addCallback(this.mSysUiStateCallback);
        this.mIsAwake = this.mWakefulnessLifecycle.get().getWakefulness() == 2;
        this.mWakefulnessLifecycle.get().addObserver(this.mWakefulnessLifecycleObserver);
        this.mLearningTimeElapsed = Settings.Secure.getLong(context.getContentResolver(), "reminder_exp_learning_time_elapsed", 0L);
        this.mLearningCount = Settings.Secure.getInt(context.getContentResolver(), "reminder_exp_learning_event_count", 0);
        this.mSettingObserver = new SettingsObserver(context, this.mHandler);
        context.getContentResolver().registerContentObserver(LEARNING_TIME_ELAPSED_URI, true, this.mSettingObserver);
        context.getContentResolver().registerContentObserver(LEARNING_EVENT_COUNT_URI, true, this.mSettingObserver);
        this.mLearnedHintLastShownEpochDay = Settings.Secure.getLong(context.getContentResolver(), "reminder_exp_learned_hint_last_shown", 0L);
        this.mLastLearningTimestamp = this.mClock.currentTimeMillis();
        callbackForCurrentState(false);
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeDeactivated() {
        this.mAssistHandleCallbacks = null;
        if (this.mContext != null) {
            this.mBroadcastDispatcher.get().unregisterReceiver(this.mDefaultHomeBroadcastReceiver);
            this.mBootCompleteCache.get().removeListener(this.mBootCompleteListener);
            this.mContext.getContentResolver().unregisterContentObserver(this.mSettingObserver);
            this.mSettingObserver = null;
            Settings.Secure.putString(this.mContext.getContentResolver(), "reminder_exp_learning_time_elapsed", Long.toString(0L), true);
            Settings.Secure.putString(this.mContext.getContentResolver(), "reminder_exp_learning_event_count", Integer.toString(0), true);
            Settings.Secure.putLong(this.mContext.getContentResolver(), "reminder_exp_learned_hint_last_shown", 0L);
            this.mContext = null;
        }
        this.mStatusBarStateController.get().removeCallback(this.mStatusBarStateListener);
        this.mActivityManagerWrapper.get().unregisterTaskStackListener(this.mTaskStackChangeListener);
        this.mOverviewProxyService.get().removeCallback(this.mOverviewProxyListener);
        this.mSysUiFlagContainer.get().removeCallback(this.mSysUiStateCallback);
        this.mWakefulnessLifecycle.get().removeObserver(this.mWakefulnessLifecycleObserver);
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onAssistantGesturePerformed() {
        Context context = this.mContext;
        if (context == null) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        int i = this.mLearningCount + 1;
        this.mLearningCount = i;
        Settings.Secure.putString(contentResolver, "reminder_exp_learning_event_count", Integer.toString(i), true);
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onAssistHandlesRequested() {
        if (this.mAssistHandleCallbacks == null || !isFullyAwake() || this.mIsNavBarHidden || this.mOnLockscreen) {
            return;
        }
        this.mAssistHandleCallbacks.showAndGo();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ComponentName getCurrentDefaultHome() {
        ArrayList<ResolveInfo> arrayList = new ArrayList();
        ComponentName homeActivities = this.mPackageManagerWrapper.get().getHomeActivities(arrayList);
        if (homeActivities != null) {
            return homeActivities;
        }
        int i = Integer.MIN_VALUE;
        while (true) {
            ComponentName componentName = null;
            for (ResolveInfo resolveInfo : arrayList) {
                int i2 = resolveInfo.priority;
                if (i2 <= i) {
                    if (i2 == i) {
                        break;
                    }
                } else {
                    componentName = resolveInfo.activityInfo.getComponentName();
                    i = resolveInfo.priority;
                }
            }
            return componentName;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleStatusBarStateChanged(int i) {
        boolean zOnLockscreen = onLockscreen(i);
        if (this.mOnLockscreen == zOnLockscreen) {
            return;
        }
        resetConsecutiveTaskSwitches();
        this.mOnLockscreen = zOnLockscreen;
        callbackForCurrentState(!zOnLockscreen);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDozingChanged(boolean z) {
        if (this.mIsDozing == z) {
            return;
        }
        resetConsecutiveTaskSwitches();
        this.mIsDozing = z;
        callbackForCurrentState(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleWakefullnessChanged(boolean z) {
        if (this.mIsAwake == z) {
            return;
        }
        resetConsecutiveTaskSwitches();
        this.mIsAwake = z;
        callbackForCurrentState(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTaskStackTopChanged(int i, ComponentName componentName) {
        if (this.mRunningTaskId == i || componentName == null) {
            return;
        }
        this.mRunningTaskId = i;
        boolean zEquals = componentName.equals(this.mDefaultHome);
        this.mIsLauncherShowing = zEquals;
        if (zEquals) {
            resetConsecutiveTaskSwitches();
        } else {
            rescheduleConsecutiveTaskSwitchesReset();
            this.mConsecutiveTaskSwitches++;
        }
        callbackForCurrentState(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSystemUiStateChanged(int i) {
        boolean z = (i & 2) != 0;
        if (this.mIsNavBarHidden == z) {
            return;
        }
        resetConsecutiveTaskSwitches();
        this.mIsNavBarHidden = z;
        callbackForCurrentState(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOverviewShown() {
        resetConsecutiveTaskSwitches();
        callbackForCurrentState(false);
    }

    private void callbackForCurrentState(boolean z) {
        updateLearningStatus();
        if (this.mIsLearned) {
            callbackForLearnedState(z);
        } else {
            callbackForUnlearnedState();
        }
    }

    private void callbackForLearnedState(boolean z) {
        if (this.mAssistHandleCallbacks == null) {
            return;
        }
        if (!isFullyAwake() || this.mIsNavBarHidden || this.mOnLockscreen || !getShowWhenTaught()) {
            this.mAssistHandleCallbacks.hide();
            return;
        }
        if (z) {
            long epochDay = LocalDate.now().toEpochDay();
            if (this.mLearnedHintLastShownEpochDay < epochDay) {
                Context context = this.mContext;
                if (context != null) {
                    Settings.Secure.putLong(context.getContentResolver(), "reminder_exp_learned_hint_last_shown", epochDay);
                }
                this.mLearnedHintLastShownEpochDay = epochDay;
                this.mAssistHandleCallbacks.showAndGo();
            }
        }
    }

    private void callbackForUnlearnedState() {
        if (this.mAssistHandleCallbacks == null) {
            return;
        }
        if (!isFullyAwake() || this.mIsNavBarHidden || isSuppressed()) {
            this.mAssistHandleCallbacks.hide();
            return;
        }
        if (this.mOnLockscreen) {
            this.mAssistHandleCallbacks.showAndStay();
            return;
        }
        if (this.mIsLauncherShowing) {
            this.mAssistHandleCallbacks.showAndGo();
        } else if (this.mConsecutiveTaskSwitches == 1) {
            this.mAssistHandleCallbacks.showAndGoDelayed(getShowAndGoDelayedShortDelayMs(), false);
        } else {
            this.mAssistHandleCallbacks.showAndGoDelayed(getShowAndGoDelayedLongDelayMs(), true);
        }
    }

    private boolean isSuppressed() {
        if (this.mOnLockscreen) {
            return getSuppressOnLockscreen();
        }
        if (this.mIsLauncherShowing) {
            return getSuppressOnLauncher();
        }
        return getSuppressOnApps();
    }

    private void updateLearningStatus() {
        if (this.mContext == null) {
            return;
        }
        long jCurrentTimeMillis = this.mClock.currentTimeMillis();
        this.mLearningTimeElapsed += jCurrentTimeMillis - this.mLastLearningTimestamp;
        this.mLastLearningTimestamp = jCurrentTimeMillis;
        this.mIsLearned = this.mLearningCount >= getLearningCount() || this.mLearningTimeElapsed >= getLearningTimeMs();
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.assist.AssistHandleReminderExpBehavior$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateLearningStatus$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateLearningStatus$0() {
        Settings.Secure.putString(this.mContext.getContentResolver(), "reminder_exp_learning_time_elapsed", Long.toString(this.mLearningTimeElapsed), true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetConsecutiveTaskSwitches() {
        this.mHandler.removeCallbacks(this.mResetConsecutiveTaskSwitches);
        this.mConsecutiveTaskSwitches = 0;
    }

    private void rescheduleConsecutiveTaskSwitchesReset() {
        this.mHandler.removeCallbacks(this.mResetConsecutiveTaskSwitches);
        this.mHandler.postDelayed(this.mResetConsecutiveTaskSwitches, getShowAndGoDelayResetTimeoutMs());
    }

    private boolean isFullyAwake() {
        return this.mIsAwake && !this.mIsDozing;
    }

    private long getLearningTimeMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_learn_time_ms", DEFAULT_LEARNING_TIME_MS);
    }

    private int getLearningCount() {
        return this.mDeviceConfigHelper.getInt("assist_handles_learn_count", 10);
    }

    private long getShowAndGoDelayedShortDelayMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_delayed_short_delay_ms", 150L);
    }

    private long getShowAndGoDelayedLongDelayMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_delayed_long_delay_ms", DEFAULT_SHOW_AND_GO_DELAYED_LONG_DELAY_MS);
    }

    private long getShowAndGoDelayResetTimeoutMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_delay_reset_timeout_ms", DEFAULT_SHOW_AND_GO_DELAY_RESET_TIMEOUT_MS);
    }

    private boolean getSuppressOnLockscreen() {
        return this.mDeviceConfigHelper.getBoolean("assist_handles_suppress_on_lockscreen", false);
    }

    private boolean getSuppressOnLauncher() {
        return this.mDeviceConfigHelper.getBoolean("assist_handles_suppress_on_launcher", false);
    }

    private boolean getSuppressOnApps() {
        return this.mDeviceConfigHelper.getBoolean("assist_handles_suppress_on_apps", true);
    }

    private boolean getShowWhenTaught() {
        return this.mDeviceConfigHelper.getBoolean("assist_handles_show_when_taught", false);
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "Current AssistHandleReminderExpBehavior State:");
        printWriter.println(str + "   mOnLockscreen=" + this.mOnLockscreen);
        printWriter.println(str + "   mIsDozing=" + this.mIsDozing);
        printWriter.println(str + "   mIsAwake=" + this.mIsAwake);
        printWriter.println(str + "   mRunningTaskId=" + this.mRunningTaskId);
        printWriter.println(str + "   mDefaultHome=" + this.mDefaultHome);
        printWriter.println(str + "   mIsNavBarHidden=" + this.mIsNavBarHidden);
        printWriter.println(str + "   mIsLauncherShowing=" + this.mIsLauncherShowing);
        printWriter.println(str + "   mConsecutiveTaskSwitches=" + this.mConsecutiveTaskSwitches);
        printWriter.println(str + "   mIsLearned=" + this.mIsLearned);
        printWriter.println(str + "   mLastLearningTimestamp=" + this.mLastLearningTimestamp);
        printWriter.println(str + "   mLearningTimeElapsed=" + this.mLearningTimeElapsed);
        printWriter.println(str + "   mLearningCount=" + this.mLearningCount);
        printWriter.println(str + "   mLearnedHintLastShownEpochDay=" + this.mLearnedHintLastShownEpochDay);
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("   mAssistHandleCallbacks present: ");
        sb.append(this.mAssistHandleCallbacks != null);
        printWriter.println(sb.toString());
        printWriter.println(str + "   Phenotype Flags:");
        printWriter.println(str + "      assist_handles_learn_time_ms=" + getLearningTimeMs());
        printWriter.println(str + "      assist_handles_learn_count=" + getLearningCount());
        printWriter.println(str + "      assist_handles_show_and_go_delayed_short_delay_ms=" + getShowAndGoDelayedShortDelayMs());
        printWriter.println(str + "      assist_handles_show_and_go_delayed_long_delay_ms=" + getShowAndGoDelayedLongDelayMs());
        printWriter.println(str + "      assist_handles_show_and_go_delay_reset_timeout_ms=" + getShowAndGoDelayResetTimeoutMs());
        printWriter.println(str + "      assist_handles_suppress_on_lockscreen=" + getSuppressOnLockscreen());
        printWriter.println(str + "      assist_handles_suppress_on_launcher=" + getSuppressOnLauncher());
        printWriter.println(str + "      assist_handles_suppress_on_apps=" + getSuppressOnApps());
        printWriter.println(str + "      assist_handles_show_when_taught=" + getShowWhenTaught());
    }

    private final class SettingsObserver extends ContentObserver {
        private final Context mContext;

        SettingsObserver(Context context, Handler handler) {
            super(handler);
            this.mContext = context;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (!AssistHandleReminderExpBehavior.LEARNING_TIME_ELAPSED_URI.equals(uri)) {
                if (AssistHandleReminderExpBehavior.LEARNING_EVENT_COUNT_URI.equals(uri)) {
                    AssistHandleReminderExpBehavior.this.mLearningCount = Settings.Secure.getInt(this.mContext.getContentResolver(), "reminder_exp_learning_event_count", 0);
                }
            } else {
                AssistHandleReminderExpBehavior assistHandleReminderExpBehavior = AssistHandleReminderExpBehavior.this;
                assistHandleReminderExpBehavior.mLastLearningTimestamp = assistHandleReminderExpBehavior.mClock.currentTimeMillis();
                AssistHandleReminderExpBehavior.this.mLearningTimeElapsed = Settings.Secure.getLong(this.mContext.getContentResolver(), "reminder_exp_learning_time_elapsed", 0L);
            }
            super.onChange(z, uri);
        }
    }
}
