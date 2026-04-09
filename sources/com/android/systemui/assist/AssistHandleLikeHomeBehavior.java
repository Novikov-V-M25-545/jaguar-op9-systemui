package com.android.systemui.assist;

import android.content.Context;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import dagger.Lazy;
import java.io.PrintWriter;

/* loaded from: classes.dex */
final class AssistHandleLikeHomeBehavior implements AssistHandleBehaviorController.BehaviorController {
    private AssistHandleCallbacks mAssistHandleCallbacks;
    private boolean mIsAwake;
    private boolean mIsDozing;
    private boolean mIsHomeHandleHiding;
    private final Lazy<StatusBarStateController> mStatusBarStateController;
    private final Lazy<SysUiState> mSysUiFlagContainer;
    private final Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    private final StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() { // from class: com.android.systemui.assist.AssistHandleLikeHomeBehavior.1
        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozingChanged(boolean z) {
            AssistHandleLikeHomeBehavior.this.handleDozingChanged(z);
        }
    };
    private final WakefulnessLifecycle.Observer mWakefulnessLifecycleObserver = new WakefulnessLifecycle.Observer() { // from class: com.android.systemui.assist.AssistHandleLikeHomeBehavior.2
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(true);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }
    };
    private final SysUiState.SysUiStateCallback mSysUiStateCallback = new SysUiState.SysUiStateCallback() { // from class: com.android.systemui.assist.AssistHandleLikeHomeBehavior$$ExternalSyntheticLambda0
        @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
        public final void onSystemUiStateChanged(int i) {
            this.f$0.handleSystemUiStateChange(i);
        }
    };

    private static boolean isHomeHandleHiding(int i) {
        return (i & 2) != 0;
    }

    AssistHandleLikeHomeBehavior(Lazy<StatusBarStateController> lazy, Lazy<WakefulnessLifecycle> lazy2, Lazy<SysUiState> lazy3) {
        this.mStatusBarStateController = lazy;
        this.mWakefulnessLifecycle = lazy2;
        this.mSysUiFlagContainer = lazy3;
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks) {
        this.mAssistHandleCallbacks = assistHandleCallbacks;
        this.mIsDozing = this.mStatusBarStateController.get().isDozing();
        this.mStatusBarStateController.get().addCallback(this.mStatusBarStateListener);
        this.mIsAwake = this.mWakefulnessLifecycle.get().getWakefulness() == 2;
        this.mWakefulnessLifecycle.get().addObserver(this.mWakefulnessLifecycleObserver);
        this.mSysUiFlagContainer.get().addCallback(this.mSysUiStateCallback);
        callbackForCurrentState();
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeDeactivated() {
        this.mAssistHandleCallbacks = null;
        this.mStatusBarStateController.get().removeCallback(this.mStatusBarStateListener);
        this.mWakefulnessLifecycle.get().removeObserver(this.mWakefulnessLifecycleObserver);
        this.mSysUiFlagContainer.get().removeCallback(this.mSysUiStateCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDozingChanged(boolean z) {
        if (this.mIsDozing == z) {
            return;
        }
        this.mIsDozing = z;
        callbackForCurrentState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleWakefullnessChanged(boolean z) {
        if (this.mIsAwake == z) {
            return;
        }
        this.mIsAwake = z;
        callbackForCurrentState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSystemUiStateChange(int i) {
        boolean zIsHomeHandleHiding = isHomeHandleHiding(i);
        if (this.mIsHomeHandleHiding == zIsHomeHandleHiding) {
            return;
        }
        this.mIsHomeHandleHiding = zIsHomeHandleHiding;
        callbackForCurrentState();
    }

    private void callbackForCurrentState() {
        if (this.mAssistHandleCallbacks == null) {
            return;
        }
        if (this.mIsHomeHandleHiding || !isFullyAwake()) {
            this.mAssistHandleCallbacks.hide();
        } else {
            this.mAssistHandleCallbacks.showAndStay();
        }
    }

    private boolean isFullyAwake() {
        return this.mIsAwake && !this.mIsDozing;
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "Current AssistHandleLikeHomeBehavior State:");
        printWriter.println(str + "   mIsDozing=" + this.mIsDozing);
        printWriter.println(str + "   mIsAwake=" + this.mIsAwake);
        printWriter.println(str + "   mIsHomeHandleHiding=" + this.mIsHomeHandleHiding);
    }
}
