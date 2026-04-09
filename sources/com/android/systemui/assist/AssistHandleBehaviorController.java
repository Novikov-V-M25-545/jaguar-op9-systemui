package com.android.systemui.assist;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import androidx.mediarouter.media.MediaRoute2Provider$$ExternalSyntheticLambda0;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.AssistUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AssistHandleBehaviorController implements AssistHandleCallbacks, Dumpable {
    private final Lazy<AccessibilityManager> mA11yManager;
    private final Provider<AssistHandleViewController> mAssistHandleViewController;
    private final AssistUtils mAssistUtils;
    private final Map<AssistHandleBehavior, BehaviorController> mBehaviorMap;
    private final Context mContext;
    private final DeviceConfigHelper mDeviceConfigHelper;
    private final Handler mHandler;
    private long mHandlesLastHiddenAt;
    private boolean mInGesturalMode;
    private long mShowAndGoEndsAt;
    private static final long DEFAULT_SHOW_AND_GO_DURATION_MS = TimeUnit.SECONDS.toMillis(3);
    private static final AssistHandleBehavior DEFAULT_BEHAVIOR = AssistHandleBehavior.REMINDER_EXP;
    private final Runnable mHideHandles = new Runnable() { // from class: com.android.systemui.assist.AssistHandleBehaviorController$$ExternalSyntheticLambda2
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.hideHandles();
        }
    };
    private final Runnable mShowAndGo = new Runnable() { // from class: com.android.systemui.assist.AssistHandleBehaviorController$$ExternalSyntheticLambda3
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.showAndGoInternal();
        }
    };
    private boolean mHandlesShowing = false;
    private AssistHandleBehavior mCurrentBehavior = AssistHandleBehavior.OFF;

    interface BehaviorController {
        default void dump(PrintWriter printWriter, String str) {
        }

        default void onAssistHandlesRequested() {
        }

        default void onAssistantGesturePerformed() {
        }

        void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks);

        default void onModeDeactivated() {
        }
    }

    AssistHandleBehaviorController(Context context, AssistUtils assistUtils, Handler handler, Provider<AssistHandleViewController> provider, DeviceConfigHelper deviceConfigHelper, Map<AssistHandleBehavior, BehaviorController> map, NavigationModeController navigationModeController, Lazy<AccessibilityManager> lazy, DumpManager dumpManager) {
        this.mContext = context;
        this.mAssistUtils = assistUtils;
        this.mHandler = handler;
        this.mAssistHandleViewController = provider;
        this.mDeviceConfigHelper = deviceConfigHelper;
        this.mBehaviorMap = map;
        this.mA11yManager = lazy;
        this.mInGesturalMode = QuickStepContract.isGesturalMode(navigationModeController.addListener(new NavigationModeController.ModeChangedListener() { // from class: com.android.systemui.assist.AssistHandleBehaviorController$$ExternalSyntheticLambda1
            @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
            public final void onNavigationModeChanged(int i) {
                this.f$0.handleNavigationModeChange(i);
            }
        }));
        setBehavior(getBehaviorMode());
        Objects.requireNonNull(handler);
        deviceConfigHelper.addOnPropertiesChangedListener(new MediaRoute2Provider$$ExternalSyntheticLambda0(handler), new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.assist.AssistHandleBehaviorController$$ExternalSyntheticLambda0
            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                this.f$0.lambda$new$0(properties);
            }
        });
        dumpManager.registerDumpable("AssistHandleBehavior", this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(DeviceConfig.Properties properties) {
        if (properties.getKeyset().contains("assist_handles_behavior_mode")) {
            setBehavior(properties.getString("assist_handles_behavior_mode", (String) null));
        }
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void hide() {
        clearPendingCommands();
        this.mHandler.post(this.mHideHandles);
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void showAndGo() {
        clearPendingCommands();
        this.mHandler.post(this.mShowAndGo);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAndGoInternal() {
        maybeShowHandles(false);
        long showAndGoDuration = getShowAndGoDuration();
        this.mShowAndGoEndsAt = SystemClock.elapsedRealtime() + showAndGoDuration;
        this.mHandler.postDelayed(this.mHideHandles, showAndGoDuration);
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void showAndGoDelayed(long j, boolean z) {
        clearPendingCommands();
        if (z) {
            this.mHandler.post(this.mHideHandles);
        }
        this.mHandler.postDelayed(this.mShowAndGo, j);
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void showAndStay() {
        clearPendingCommands();
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.assist.AssistHandleBehaviorController$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$showAndStay$1();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showAndStay$1() {
        maybeShowHandles(true);
    }

    public long getShowAndGoRemainingTimeMs() {
        return Long.max(this.mShowAndGoEndsAt - SystemClock.elapsedRealtime(), 0L);
    }

    public boolean areHandlesShowing() {
        return this.mHandlesShowing;
    }

    void onAssistantGesturePerformed() {
        this.mBehaviorMap.get(this.mCurrentBehavior).onAssistantGesturePerformed();
    }

    void onAssistHandlesRequested() {
        if (this.mInGesturalMode) {
            this.mBehaviorMap.get(this.mCurrentBehavior).onAssistHandlesRequested();
        }
    }

    void setBehavior(AssistHandleBehavior assistHandleBehavior) {
        if (this.mCurrentBehavior == assistHandleBehavior) {
            return;
        }
        if (!this.mBehaviorMap.containsKey(assistHandleBehavior)) {
            Log.e("AssistHandleBehavior", "Unsupported behavior requested: " + assistHandleBehavior.toString());
            return;
        }
        if (this.mInGesturalMode) {
            this.mBehaviorMap.get(this.mCurrentBehavior).onModeDeactivated();
            this.mBehaviorMap.get(assistHandleBehavior).onModeActivated(this.mContext, this);
        }
        this.mCurrentBehavior = assistHandleBehavior;
    }

    private void setBehavior(String str) {
        try {
            setBehavior(AssistHandleBehavior.valueOf(str));
        } catch (IllegalArgumentException | NullPointerException unused) {
            Log.e("AssistHandleBehavior", "Invalid behavior: " + str);
        }
    }

    private boolean handlesUnblocked(boolean z) {
        if (isUserSetupComplete()) {
            return (z || ((SystemClock.elapsedRealtime() - this.mHandlesLastHiddenAt) > getShownFrequencyThreshold() ? 1 : ((SystemClock.elapsedRealtime() - this.mHandlesLastHiddenAt) == getShownFrequencyThreshold() ? 0 : -1)) >= 0) && this.mAssistUtils.getAssistComponentForUser(KeyguardUpdateMonitor.getCurrentUser()) != null;
        }
        return false;
    }

    private long getShownFrequencyThreshold() {
        return this.mDeviceConfigHelper.getLong("assist_handles_shown_frequency_threshold_ms", 0L);
    }

    private long getShowAndGoDuration() {
        return this.mA11yManager.get().getRecommendedTimeoutMillis((int) this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_duration_ms", DEFAULT_SHOW_AND_GO_DURATION_MS), 1);
    }

    private String getBehaviorMode() {
        return this.mDeviceConfigHelper.getString("assist_handles_behavior_mode", DEFAULT_BEHAVIOR.toString());
    }

    private void maybeShowHandles(boolean z) {
        if (!this.mHandlesShowing && handlesUnblocked(z)) {
            this.mHandlesShowing = true;
            AssistHandleViewController assistHandleViewController = this.mAssistHandleViewController.get();
            if (assistHandleViewController == null) {
                Log.w("AssistHandleBehavior", "Couldn't show handles, AssistHandleViewController unavailable");
            } else {
                assistHandleViewController.lambda$setAssistHintVisible$0(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideHandles() {
        if (this.mHandlesShowing) {
            this.mHandlesShowing = false;
            this.mHandlesLastHiddenAt = SystemClock.elapsedRealtime();
            AssistHandleViewController assistHandleViewController = this.mAssistHandleViewController.get();
            if (assistHandleViewController == null) {
                Log.w("AssistHandleBehavior", "Couldn't show handles, AssistHandleViewController unavailable");
            } else {
                assistHandleViewController.lambda$setAssistHintVisible$0(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNavigationModeChange(int i) {
        boolean zIsGesturalMode = QuickStepContract.isGesturalMode(i);
        if (this.mInGesturalMode == zIsGesturalMode) {
            return;
        }
        this.mInGesturalMode = zIsGesturalMode;
        if (zIsGesturalMode) {
            this.mBehaviorMap.get(this.mCurrentBehavior).onModeActivated(this.mContext, this);
        } else {
            this.mBehaviorMap.get(this.mCurrentBehavior).onModeDeactivated();
            hide();
        }
    }

    private void clearPendingCommands() {
        this.mHandler.removeCallbacks(this.mHideHandles);
        this.mHandler.removeCallbacks(this.mShowAndGo);
        this.mShowAndGoEndsAt = 0L;
    }

    private boolean isUserSetupComplete() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1;
    }

    @VisibleForTesting
    void setInGesturalModeForTest(boolean z) {
        this.mInGesturalMode = z;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Current AssistHandleBehaviorController State:");
        printWriter.println("   mHandlesShowing=" + this.mHandlesShowing);
        printWriter.println("   mHandlesLastHiddenAt=" + this.mHandlesLastHiddenAt);
        printWriter.println("   mInGesturalMode=" + this.mInGesturalMode);
        printWriter.println("   Phenotype Flags:");
        printWriter.println("      assist_handles_show_and_go_duration_ms(a11y modded)=" + getShowAndGoDuration());
        printWriter.println("      assist_handles_shown_frequency_threshold_ms=" + getShownFrequencyThreshold());
        printWriter.println("      assist_handles_behavior_mode=" + getBehaviorMode());
        printWriter.println("   mCurrentBehavior=" + this.mCurrentBehavior.toString());
        this.mBehaviorMap.get(this.mCurrentBehavior).dump(printWriter, "   ");
    }
}
