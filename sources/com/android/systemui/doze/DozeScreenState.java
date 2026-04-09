package com.android.systemui.doze;

import android.os.Handler;
import android.util.Log;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;

/* loaded from: classes.dex */
public class DozeScreenState implements DozeMachine.Part {
    private static final boolean DEBUG = DozeService.DEBUG;
    private final DozeHost mDozeHost;
    private final DozeMachine.Service mDozeService;
    private final Handler mHandler;
    private final DozeParameters mParameters;
    private SettableWakeLock mWakeLock;
    private final Runnable mApplyPendingScreenState = new Runnable() { // from class: com.android.systemui.doze.DozeScreenState$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.applyPendingScreenState();
        }
    };
    private int mPendingScreenState = 0;

    public DozeScreenState(DozeMachine.Service service, Handler handler, DozeHost dozeHost, DozeParameters dozeParameters, WakeLock wakeLock) {
        this.mDozeService = service;
        this.mHandler = handler;
        this.mParameters = dozeParameters;
        this.mDozeHost = dozeHost;
        this.mWakeLock = new SettableWakeLock(wakeLock, "DozeScreenState");
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        final int iScreenState = state2.screenState(this.mParameters);
        this.mDozeHost.cancelGentleSleep();
        boolean z = false;
        if (state2 == DozeMachine.State.FINISH) {
            this.mPendingScreenState = 0;
            this.mHandler.removeCallbacks(this.mApplyPendingScreenState);
            lambda$transitionTo$0(iScreenState);
            this.mWakeLock.setAcquired(false);
            return;
        }
        if (iScreenState == 0) {
            return;
        }
        boolean zHasCallbacks = this.mHandler.hasCallbacks(this.mApplyPendingScreenState);
        boolean z2 = state == DozeMachine.State.DOZE_PULSE_DONE && state2.isAlwaysOn();
        DozeMachine.State state3 = DozeMachine.State.DOZE_AOD_PAUSED;
        boolean z3 = (state == state3 || state == DozeMachine.State.DOZE) && state2.isAlwaysOn();
        boolean z4 = (state.isAlwaysOn() && state2 == DozeMachine.State.DOZE) || (state == DozeMachine.State.DOZE_AOD_PAUSING && state2 == state3);
        boolean z5 = state == DozeMachine.State.INITIALIZED;
        if (!zHasCallbacks && !z5 && !z2 && !z3) {
            if (z4) {
                this.mDozeHost.prepareForGentleSleep(new Runnable() { // from class: com.android.systemui.doze.DozeScreenState$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$transitionTo$0(iScreenState);
                    }
                });
                return;
            } else {
                lambda$transitionTo$0(iScreenState);
                return;
            }
        }
        this.mPendingScreenState = iScreenState;
        if (state2 == DozeMachine.State.DOZE_AOD && this.mParameters.shouldControlScreenOff() && !z3) {
            z = true;
        }
        if (z) {
            this.mWakeLock.setAcquired(true);
        }
        if (!zHasCallbacks) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Display state changed to ");
                sb.append(iScreenState);
                sb.append(" delayed by ");
                sb.append(z ? 4000 : 1);
                Log.d("DozeScreenState", sb.toString());
            }
            if (z) {
                this.mHandler.postDelayed(this.mApplyPendingScreenState, 4000L);
                return;
            } else {
                this.mHandler.post(this.mApplyPendingScreenState);
                return;
            }
        }
        if (DEBUG) {
            Log.d("DozeScreenState", "Pending display state change to " + iScreenState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyPendingScreenState() {
        lambda$transitionTo$0(this.mPendingScreenState);
        this.mPendingScreenState = 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: applyScreenState, reason: merged with bridge method [inline-methods] */
    public void lambda$transitionTo$0(int i) {
        if (i != 0) {
            if (DEBUG) {
                Log.d("DozeScreenState", "setDozeScreenState(" + i + ")");
            }
            this.mDozeService.setDozeScreenState(i);
            this.mPendingScreenState = 0;
            this.mWakeLock.setAcquired(false);
        }
    }
}
