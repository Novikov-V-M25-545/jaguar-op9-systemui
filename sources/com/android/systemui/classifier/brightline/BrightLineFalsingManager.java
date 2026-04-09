package com.android.systemui.classifier.brightline;

import android.app.ActivityManager;
import android.hardware.biometrics.BiometricSourceType;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.IndentingPrintWriter;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.sensors.ThresholdSensor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/* loaded from: classes.dex */
public class BrightLineFalsingManager implements FalsingManager {
    static final boolean DEBUG = Log.isLoggable("FalsingManager", 3);
    private static final Queue<String> RECENT_INFO_LOG = new ArrayDeque(41);
    private static final Queue<DebugSwipeRecord> RECENT_SWIPES = new ArrayDeque(21);
    private final List<FalsingClassifier> mClassifiers;
    private final FalsingDataProvider mDataProvider;
    private final DockManager mDockManager;
    private int mIsFalseTouchCalls;
    private boolean mJustUnlockedWithFace;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateCallback;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MetricsLogger mMetricsLogger;
    private boolean mPreviousResult;
    private final ProximitySensor mProximitySensor;
    private boolean mScreenOn;
    private ThresholdSensor.Listener mSensorEventListener = new ThresholdSensor.Listener() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda0
        @Override // com.android.systemui.util.sensors.ThresholdSensor.Listener
        public final void onThresholdCrossed(ThresholdSensor.ThresholdSensorEvent thresholdSensorEvent) {
            this.f$0.onProximityEvent(thresholdSensorEvent);
        }
    };
    private boolean mSessionStarted;
    private boolean mShowingAod;
    private int mState;
    private final StatusBarStateController mStatusBarStateController;
    private StatusBarStateController.StateListener mStatusBarStateListener;

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isClassifierEnabled() {
        return true;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isReportingEnabled() {
        return false;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isUnlockingDisabled() {
        return false;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingAborted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraHintStarted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraOn() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onExpansionFromPulseStopped() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceHintStarted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceOn() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationActive() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDismissed() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDoubleTap(boolean z, float f, float f2) {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStopDismissing() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStopDraggingDown() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStopped() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onUnlockHintStarted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public Uri reportRejectedTouch() {
        return null;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setNotificationExpanded() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean shouldEnforceBouncer() {
        return false;
    }

    public BrightLineFalsingManager(FalsingDataProvider falsingDataProvider, KeyguardUpdateMonitor keyguardUpdateMonitor, ProximitySensor proximitySensor, DeviceConfigProxy deviceConfigProxy, DockManager dockManager, StatusBarStateController statusBarStateController) {
        KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
                if (i == KeyguardUpdateMonitor.getCurrentUser() && biometricSourceType == BiometricSourceType.FACE) {
                    BrightLineFalsingManager.this.mJustUnlockedWithFace = true;
                }
            }
        };
        this.mKeyguardUpdateCallback = keyguardUpdateMonitorCallback;
        this.mPreviousResult = false;
        this.mStatusBarStateListener = new StatusBarStateController.StateListener() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager.2
            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                BrightLineFalsingManager.logDebug("StatusBarState=" + StatusBarState.toShortString(i));
                BrightLineFalsingManager.this.mState = i;
                BrightLineFalsingManager.this.updateSessionActive();
            }
        };
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mDataProvider = falsingDataProvider;
        this.mProximitySensor = proximitySensor;
        this.mDockManager = dockManager;
        this.mStatusBarStateController = statusBarStateController;
        keyguardUpdateMonitor.registerCallback(keyguardUpdateMonitorCallback);
        statusBarStateController.addCallback(this.mStatusBarStateListener);
        this.mState = statusBarStateController.getState();
        this.mMetricsLogger = new MetricsLogger();
        ArrayList arrayList = new ArrayList();
        this.mClassifiers = arrayList;
        DistanceClassifier distanceClassifier = new DistanceClassifier(falsingDataProvider, deviceConfigProxy);
        ProximityClassifier proximityClassifier = new ProximityClassifier(distanceClassifier, falsingDataProvider, deviceConfigProxy);
        arrayList.add(new PointerCountClassifier(falsingDataProvider));
        arrayList.add(new TypeClassifier(falsingDataProvider));
        arrayList.add(new DiagonalClassifier(falsingDataProvider, deviceConfigProxy));
        arrayList.add(distanceClassifier);
        arrayList.add(proximityClassifier);
        arrayList.add(new ZigZagClassifier(falsingDataProvider, deviceConfigProxy));
    }

    private void registerSensors() {
        if (this.mDataProvider.isWirelessCharging()) {
            return;
        }
        this.mProximitySensor.register(this.mSensorEventListener);
    }

    private void unregisterSensors() {
        this.mProximitySensor.unregister(this.mSensorEventListener);
    }

    private void sessionStart() {
        if (this.mSessionStarted || !shouldSessionBeActive()) {
            return;
        }
        logDebug("Starting Session");
        this.mSessionStarted = true;
        this.mJustUnlockedWithFace = false;
        registerSensors();
        this.mClassifiers.forEach(new Consumer() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda4
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onSessionStarted();
            }
        });
    }

    private void sessionEnd() {
        if (this.mSessionStarted) {
            logDebug("Ending Session");
            this.mSessionStarted = false;
            unregisterSensors();
            this.mDataProvider.onSessionEnd();
            this.mClassifiers.forEach(new Consumer() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda3
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((FalsingClassifier) obj).onSessionEnded();
                }
            });
            int i = this.mIsFalseTouchCalls;
            if (i != 0) {
                this.mMetricsLogger.histogram("falsing_failure_after_attempts", i);
                this.mIsFalseTouchCalls = 0;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSessionActive() {
        if (shouldSessionBeActive()) {
            sessionStart();
        } else {
            sessionEnd();
        }
    }

    private boolean shouldSessionBeActive() {
        return this.mScreenOn && this.mState == 1 && !this.mShowingAod;
    }

    private void updateInteractionType(int i) {
        logDebug("InteractionType: " + i);
        this.mDataProvider.setInteractionType(i);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isFalseTouch() {
        if (!this.mDataProvider.isDirty()) {
            return this.mPreviousResult;
        }
        this.mPreviousResult = (ActivityManager.isRunningInUserTestHarness() || this.mJustUnlockedWithFace || this.mDockManager.isDocked() || !this.mClassifiers.stream().anyMatch(new Predicate() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda6
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.lambda$isFalseTouch$0((FalsingClassifier) obj);
            }
        })) ? false : true;
        logDebug("Is false touch? " + this.mPreviousResult);
        if (Build.IS_ENG || Build.IS_USERDEBUG) {
            RECENT_SWIPES.add(new DebugSwipeRecord(this.mPreviousResult, this.mDataProvider.getInteractionType(), (List) this.mDataProvider.getRecentMotionEvents().stream().map(new Function() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda5
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return BrightLineFalsingManager.lambda$isFalseTouch$1((MotionEvent) obj);
                }
            }).collect(Collectors.toList())));
            while (true) {
                Queue<DebugSwipeRecord> queue = RECENT_SWIPES;
                if (queue.size() <= 40) {
                    break;
                }
                queue.remove();
            }
        }
        return this.mPreviousResult;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$isFalseTouch$0(FalsingClassifier falsingClassifier) {
        boolean zIsFalseTouch = falsingClassifier.isFalseTouch();
        if (zIsFalseTouch) {
            logInfo(String.format(null, "{classifier=%s, interactionType=%d}", falsingClassifier.getClass().getName(), Integer.valueOf(this.mDataProvider.getInteractionType())));
            String reason = falsingClassifier.getReason();
            if (reason != null) {
                logInfo(reason);
            }
        } else {
            logDebug(falsingClassifier.getClass().getName() + ": false");
        }
        return zIsFalseTouch;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ XYDt lambda$isFalseTouch$1(MotionEvent motionEvent) {
        return new XYDt((int) motionEvent.getX(), (int) motionEvent.getY(), (int) (motionEvent.getEventTime() - motionEvent.getDownTime()));
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTouchEvent(final MotionEvent motionEvent, int i, int i2) {
        this.mDataProvider.onMotionEvent(motionEvent);
        this.mClassifiers.forEach(new Consumer() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onTouchEvent(motionEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onProximityEvent(final ThresholdSensor.ThresholdSensorEvent thresholdSensorEvent) {
        this.mClassifiers.forEach(new Consumer() { // from class: com.android.systemui.classifier.brightline.BrightLineFalsingManager$$ExternalSyntheticLambda2
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onProximityEvent(thresholdSensorEvent);
            }
        });
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onSuccessfulUnlock() {
        int i = this.mIsFalseTouchCalls;
        if (i != 0) {
            this.mMetricsLogger.histogram("falsing_success_after_attempts", i);
            this.mIsFalseTouchCalls = 0;
        }
        sessionEnd();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setShowingAod(boolean z) {
        this.mShowingAod = z;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStartDraggingDown() {
        updateInteractionType(2);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onQsDown() {
        updateInteractionType(0);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setQsExpanded(boolean z) {
        if (z) {
            unregisterSensors();
        } else if (this.mSessionStarted) {
            registerSensors();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStarted(boolean z) {
        updateInteractionType(z ? 8 : 4);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingStarted(boolean z) {
        updateInteractionType(z ? 6 : 5);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onStartExpandingFromPulse() {
        updateInteractionType(9);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOnFromTouch() {
        onScreenTurningOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenTurningOn() {
        this.mScreenOn = true;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOff() {
        this.mScreenOn = false;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStartDismissing() {
        updateInteractionType(1);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerShown() {
        unregisterSensors();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerHidden() {
        if (this.mSessionStarted) {
            registerSensors();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void dump(PrintWriter printWriter) {
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.println("BRIGHTLINE FALSING MANAGER");
        indentingPrintWriter.print("classifierEnabled=");
        indentingPrintWriter.println(isClassifierEnabled() ? 1 : 0);
        indentingPrintWriter.print("mJustUnlockedWithFace=");
        indentingPrintWriter.println(this.mJustUnlockedWithFace ? 1 : 0);
        indentingPrintWriter.print("isDocked=");
        indentingPrintWriter.println(this.mDockManager.isDocked() ? 1 : 0);
        indentingPrintWriter.print("width=");
        indentingPrintWriter.println(this.mDataProvider.getWidthPixels());
        indentingPrintWriter.print("height=");
        indentingPrintWriter.println(this.mDataProvider.getHeightPixels());
        indentingPrintWriter.println();
        Queue<DebugSwipeRecord> queue = RECENT_SWIPES;
        if (queue.size() != 0) {
            indentingPrintWriter.println("Recent swipes:");
            indentingPrintWriter.increaseIndent();
            Iterator<DebugSwipeRecord> it = queue.iterator();
            while (it.hasNext()) {
                indentingPrintWriter.println(it.next().getString());
                indentingPrintWriter.println();
            }
            indentingPrintWriter.decreaseIndent();
        } else {
            indentingPrintWriter.println("No recent swipes");
        }
        indentingPrintWriter.println();
        indentingPrintWriter.println("Recent falsing info:");
        indentingPrintWriter.increaseIndent();
        Iterator<String> it2 = RECENT_INFO_LOG.iterator();
        while (it2.hasNext()) {
            indentingPrintWriter.println(it2.next());
        }
        indentingPrintWriter.println();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void cleanup() {
        unregisterSensors();
        this.mKeyguardUpdateMonitor.removeCallback(this.mKeyguardUpdateCallback);
        this.mStatusBarStateController.removeCallback(this.mStatusBarStateListener);
    }

    static void logDebug(String str) {
        logDebug(str, null);
    }

    static void logDebug(String str, Throwable th) {
        if (DEBUG) {
            Log.d("FalsingManager", str, th);
        }
    }

    static void logInfo(String str) {
        Log.i("FalsingManager", str);
        RECENT_INFO_LOG.add(str);
        while (true) {
            Queue<String> queue = RECENT_INFO_LOG;
            if (queue.size() <= 40) {
                return;
            } else {
                queue.remove();
            }
        }
    }

    private static class DebugSwipeRecord {
        private final int mInteractionType;
        private final boolean mIsFalse;
        private final List<XYDt> mRecentMotionEvents;

        DebugSwipeRecord(boolean z, int i, List<XYDt> list) {
            this.mIsFalse = z;
            this.mInteractionType = i;
            this.mRecentMotionEvents = list;
        }

        String getString() {
            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner.add(Integer.toString(1)).add(this.mIsFalse ? "1" : "0").add(Integer.toString(this.mInteractionType));
            Iterator<XYDt> it = this.mRecentMotionEvents.iterator();
            while (it.hasNext()) {
                stringJoiner.add(it.next().toString());
            }
            return stringJoiner.toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class XYDt {
        private final int mDT;
        private final int mX;
        private final int mY;

        XYDt(int i, int i2, int i3) {
            this.mX = i;
            this.mY = i2;
            this.mDT = i3;
        }

        public String toString() {
            return this.mX + "," + this.mY + "," + this.mDT;
        }
    }
}
