package com.android.systemui.statusbar;

import android.app.ITransientNotificationCallback;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import com.android.systemui.tracing.ProtoTracer;
import java.util.ArrayList;
import java.util.Iterator;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class CommandQueue extends IStatusBar.Stub implements CallbackController<Callbacks>, DisplayManager.DisplayListener {
    private ProtoTracer mProtoTracer;
    private final Object mLock = new Object();
    private ArrayList<Callbacks> mCallbacks = new ArrayList<>();
    private Handler mHandler = new H(Looper.getMainLooper());
    private SparseArray<Pair<Integer, Integer>> mDisplayDisabled = new SparseArray<>();
    private int mLastUpdatedImeDisplayId = -1;

    public interface Callbacks {
        default void abortTransient(int i, int[] iArr) {
        }

        default void addQsTile(ComponentName componentName) {
        }

        default void animateCollapsePanels(int i, boolean z) {
        }

        default void animateExpandNotificationsPanel() {
        }

        default void animateExpandSettingsPanel(String str) {
        }

        default void appTransitionCancelled(int i) {
        }

        default void appTransitionFinished(int i) {
        }

        default void appTransitionPending(int i, boolean z) {
        }

        default void appTransitionStarting(int i, long j, long j2, boolean z) {
        }

        default void cancelPreloadRecentApps() {
        }

        default void clickTile(ComponentName componentName) {
        }

        default void disable(int i, int i2, int i3, boolean z) {
        }

        default void dismissInattentiveSleepWarning(boolean z) {
        }

        default void dismissKeyboardShortcutsMenu() {
        }

        default void handleShowGlobalActionsMenu() {
        }

        default void handleShowShutdownUi(boolean z, String str, boolean z2) {
        }

        default void handleSystemKey(int i) {
        }

        default void hideAuthenticationDialog() {
        }

        default void hideInDisplayFingerprintView() {
        }

        default void hideRecentApps(boolean z, boolean z2) {
        }

        default void hideToast(String str, IBinder iBinder) {
        }

        default void leftInLandscapeChanged(boolean z) {
        }

        default void onBiometricAuthenticated() {
        }

        default void onBiometricError(int i, int i2, int i3) {
        }

        default void onBiometricHelp(String str) {
        }

        default void onCameraLaunchGestureDetected(int i) {
        }

        default void onDisplayReady(int i) {
        }

        default void onDisplayRemoved(int i) {
        }

        default void onRecentsAnimationStateChanged(boolean z) {
        }

        default void onRotationProposal(int i, boolean z) {
        }

        default void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        }

        default void onTracingStateChanged(boolean z) {
        }

        default void preloadRecentApps() {
        }

        default void remQsTile(ComponentName componentName) {
        }

        default void removeIcon(String str) {
        }

        default void screenPinningStateChanged(boolean z) {
        }

        default void setBlockedGesturalNavigation(boolean z) {
        }

        default void setIcon(String str, StatusBarIcon statusBarIcon) {
        }

        default void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        }

        default void setTopAppHidesStatusBar(boolean z) {
        }

        default void setWindowState(int i, int i2, int i3) {
        }

        default void showAssistDisclosure() {
        }

        default void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        }

        default void showInDisplayFingerprintView() {
        }

        default void showInattentiveSleepWarning() {
        }

        default void showPictureInPictureMenu() {
        }

        default void showPinningEnterExitToast(boolean z) {
        }

        default void showPinningEscapeToast() {
        }

        default void showRecentApps(boolean z) {
        }

        default void showScreenPinningRequest(int i) {
        }

        default void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        }

        default void showTransient(int i, int[] iArr) {
        }

        default void showWirelessChargingAnimation(int i) {
        }

        default void startAssist(Bundle bundle) {
        }

        default void suppressAmbientDisplay(boolean z) {
        }

        default void toggleCameraFlash() {
        }

        default void toggleKeyboardShortcutsMenu(int i) {
        }

        default void togglePanel() {
        }

        default void toggleRecentApps() {
        }

        default void toggleSplitScreen() {
        }

        default void topAppWindowChanged(int i, boolean z, boolean z2) {
        }
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int i) {
    }

    public CommandQueue(Context context, ProtoTracer protoTracer) {
        this.mProtoTracer = protoTracer;
        ((DisplayManager) context.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mHandler);
        setDisabled(0, 0, 0);
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayRemoved(int i) {
        synchronized (this.mLock) {
            this.mDisplayDisabled.remove(i);
        }
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            this.mCallbacks.get(size).onDisplayRemoved(i);
        }
    }

    public boolean panelsEnabled() {
        return (getDisabled1(0) & 65536) == 0 && (getDisabled2(0) & 4) == 0 && !StatusBar.ONLY_CORE_APPS;
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(Callbacks callbacks) {
        this.mCallbacks.add(callbacks);
        for (int i = 0; i < this.mDisplayDisabled.size(); i++) {
            int iKeyAt = this.mDisplayDisabled.keyAt(i);
            callbacks.disable(iKeyAt, getDisabled1(iKeyAt), getDisabled2(iKeyAt), false);
        }
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(Callbacks callbacks) {
        this.mCallbacks.remove(callbacks);
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 1, 0, new Pair(str, statusBarIcon)).sendToTarget();
        }
    }

    public void removeIcon(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 2, 0, str).sendToTarget();
        }
    }

    public void disable(int i, int i2, int i3, boolean z) {
        synchronized (this.mLock) {
            setDisabled(i, i2, i3);
            this.mHandler.removeMessages(LineageHardwareManager.FEATURE_COLOR_BALANCE);
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = i2;
            someArgsObtain.argi3 = i3;
            someArgsObtain.argi4 = z ? 1 : 0;
            Message messageObtainMessage = this.mHandler.obtainMessage(LineageHardwareManager.FEATURE_COLOR_BALANCE, someArgsObtain);
            if (Looper.myLooper() == this.mHandler.getLooper()) {
                this.mHandler.handleMessage(messageObtainMessage);
                messageObtainMessage.recycle();
            } else {
                messageObtainMessage.sendToTarget();
            }
        }
    }

    public void disable(int i, int i2, int i3) {
        disable(i, i2, i3, true);
    }

    public void recomputeDisableFlags(int i, boolean z) {
        disable(i, getDisabled1(i), getDisabled2(i), z);
    }

    private void setDisabled(int i, int i2, int i3) {
        this.mDisplayDisabled.put(i, new Pair<>(Integer.valueOf(i2), Integer.valueOf(i3)));
    }

    private int getDisabled1(int i) {
        return ((Integer) getDisabled(i).first).intValue();
    }

    private int getDisabled2(int i) {
        return ((Integer) getDisabled(i).second).intValue();
    }

    private Pair<Integer, Integer> getDisabled(int i) {
        Pair<Integer, Integer> pair = this.mDisplayDisabled.get(i);
        if (pair != null) {
            return pair;
        }
        Pair<Integer, Integer> pair2 = new Pair<>(0, 0);
        this.mDisplayDisabled.put(i, pair2);
        return pair2;
    }

    public void animateExpandNotificationsPanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(196608);
            this.mHandler.sendEmptyMessage(196608);
        }
    }

    public void animateCollapsePanels() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT);
            this.mHandler.obtainMessage(LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT, 0, 0).sendToTarget();
        }
    }

    public void animateCollapsePanels(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT);
            this.mHandler.obtainMessage(LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT, i, z ? 1 : 0).sendToTarget();
        }
    }

    public void togglePanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2293760);
            this.mHandler.obtainMessage(2293760, 0, 0).sendToTarget();
        }
    }

    public void animateExpandSettingsPanel(String str) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(327680);
            this.mHandler.obtainMessage(327680, str).sendToTarget();
        }
    }

    public void topAppWindowChanged(int i, boolean z, boolean z2) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.argi1 = i;
            int i2 = 1;
            someArgsObtain.argi2 = z ? 1 : 0;
            if (!z2) {
                i2 = 0;
            }
            someArgsObtain.argi3 = i2;
            this.mHandler.obtainMessage(3276800, someArgsObtain).sendToTarget();
        }
    }

    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES);
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = i2;
            someArgsObtain.argi3 = i3;
            int i4 = 1;
            someArgsObtain.argi4 = z ? 1 : 0;
            if (!z2) {
                i4 = 0;
            }
            someArgsObtain.argi5 = i4;
            someArgsObtain.arg1 = iBinder;
            this.mHandler.obtainMessage(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES, someArgsObtain).sendToTarget();
        }
    }

    public void showRecentApps(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(851968);
            this.mHandler.obtainMessage(851968, z ? 1 : 0, 0, null).sendToTarget();
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(917504);
            this.mHandler.obtainMessage(917504, z ? 1 : 0, z2 ? 1 : 0, null).sendToTarget();
        }
    }

    public void toggleSplitScreen() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1966080);
            this.mHandler.obtainMessage(1966080, 0, 0, null).sendToTarget();
        }
    }

    public void toggleRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(589824);
            Message messageObtainMessage = this.mHandler.obtainMessage(589824, 0, 0, null);
            messageObtainMessage.setAsynchronous(true);
            messageObtainMessage.sendToTarget();
        }
    }

    public void preloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(655360);
            this.mHandler.obtainMessage(655360, 0, 0, null).sendToTarget();
        }
    }

    public void cancelPreloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(720896);
            this.mHandler.obtainMessage(720896, 0, 0, null).sendToTarget();
        }
    }

    public void dismissKeyboardShortcutsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(LineageHardwareManager.FEATURE_ANTI_FLICKER);
            this.mHandler.obtainMessage(LineageHardwareManager.FEATURE_ANTI_FLICKER).sendToTarget();
        }
    }

    public void toggleKeyboardShortcutsMenu(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1638400);
            this.mHandler.obtainMessage(1638400, i, 0).sendToTarget();
        }
    }

    public void showPictureInPictureMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1703936);
            this.mHandler.obtainMessage(1703936).sendToTarget();
        }
    }

    public void setWindowState(int i, int i2, int i3) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(786432, i, i2, Integer.valueOf(i3)).sendToTarget();
        }
    }

    public void showScreenPinningRequest(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1179648, i, 0, null).sendToTarget();
        }
    }

    public void appTransitionPending(int i) {
        appTransitionPending(i, false);
    }

    public void appTransitionPending(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1245184, i, z ? 1 : 0).sendToTarget();
        }
    }

    public void appTransitionCancelled(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1310720, i, 0).sendToTarget();
        }
    }

    public void appTransitionStarting(int i, long j, long j2) {
        appTransitionStarting(i, j, j2, false);
    }

    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = z ? 1 : 0;
            someArgsObtain.arg1 = Long.valueOf(j);
            someArgsObtain.arg2 = Long.valueOf(j2);
            this.mHandler.obtainMessage(1376256, someArgsObtain).sendToTarget();
        }
    }

    public void appTransitionFinished(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2031616, i, 0).sendToTarget();
        }
    }

    public void showAssistDisclosure() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1441792);
            this.mHandler.obtainMessage(1441792).sendToTarget();
        }
    }

    public void startAssist(Bundle bundle) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1507328);
            this.mHandler.obtainMessage(1507328, bundle).sendToTarget();
        }
    }

    public void onCameraLaunchGestureDetected(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1572864);
            this.mHandler.obtainMessage(1572864, i, 0).sendToTarget();
        }
    }

    public void addQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1769472, componentName).sendToTarget();
        }
    }

    public void remQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1835008, componentName).sendToTarget();
        }
    }

    public void clickQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1900544, componentName).sendToTarget();
        }
    }

    public void handleSystemKey(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2162688, i, 0).sendToTarget();
        }
    }

    public void showPinningEnterExitToast(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2949120, Boolean.valueOf(z)).sendToTarget();
        }
    }

    public void showPinningEscapeToast() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3014656).sendToTarget();
        }
    }

    public void showGlobalActionsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2228224);
            this.mHandler.obtainMessage(2228224).sendToTarget();
        }
    }

    public void setTopAppHidesStatusBar(boolean z) {
        this.mHandler.removeMessages(2424832);
        this.mHandler.obtainMessage(2424832, z ? 1 : 0, 0).sendToTarget();
    }

    public void showShutdownUi(boolean z, String str, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2359296);
            this.mHandler.obtainMessage(2359296, z ? 1 : 0, z2 ? 1 : 0, str).sendToTarget();
        }
    }

    public void showWirelessChargingAnimation(int i) {
        this.mHandler.removeMessages(2883584);
        this.mHandler.obtainMessage(2883584, i, 0).sendToTarget();
    }

    public void onProposedRotationChanged(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2490368);
            this.mHandler.obtainMessage(2490368, i, z ? 1 : 0, null).sendToTarget();
        }
    }

    public void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.arg1 = bundle;
            someArgsObtain.arg2 = iBiometricServiceReceiverInternal;
            someArgsObtain.argi1 = i;
            someArgsObtain.arg3 = Boolean.valueOf(z);
            someArgsObtain.argi2 = i2;
            someArgsObtain.arg4 = str;
            someArgsObtain.arg5 = Long.valueOf(j);
            someArgsObtain.argi3 = i3;
            this.mHandler.obtainMessage(2555904, someArgsObtain).sendToTarget();
        }
    }

    public void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.arg1 = str;
            someArgsObtain.arg2 = iBinder;
            someArgsObtain.arg3 = charSequence;
            someArgsObtain.arg4 = iBinder2;
            someArgsObtain.arg5 = iTransientNotificationCallback;
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = i2;
            this.mHandler.obtainMessage(3473408, someArgsObtain).sendToTarget();
        }
    }

    public void hideToast(String str, IBinder iBinder) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.arg1 = str;
            someArgsObtain.arg2 = iBinder;
            this.mHandler.obtainMessage(3538944, someArgsObtain).sendToTarget();
        }
    }

    public void onBiometricAuthenticated() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2621440).sendToTarget();
        }
    }

    public void onBiometricHelp(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2686976, str).sendToTarget();
        }
    }

    public void onBiometricError(int i, int i2, int i3) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = i2;
            someArgsObtain.argi3 = i3;
            this.mHandler.obtainMessage(2752512, someArgsObtain).sendToTarget();
        }
    }

    public void hideAuthenticationDialog() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2818048).sendToTarget();
        }
    }

    public void showInDisplayFingerprintView() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3735552).sendToTarget();
        }
    }

    public void hideInDisplayFingerprintView() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3801088).sendToTarget();
        }
    }

    public void setBlockedGesturalNavigation(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(3866624);
            this.mHandler.obtainMessage(3866624, Boolean.valueOf(z)).sendToTarget();
        }
    }

    public void toggleCameraFlash() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(3932160);
            this.mHandler.sendEmptyMessage(3932160);
        }
    }

    public void onDisplayReady(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(458752, i, 0).sendToTarget();
        }
    }

    public void onRecentsAnimationStateChanged(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3080192, z ? 1 : 0, 0).sendToTarget();
        }
    }

    public void showInattentiveSleepWarning() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3342336).sendToTarget();
        }
    }

    public void dismissInattentiveSleepWarning(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3407872, Boolean.valueOf(z)).sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleShowImeButton(int i, IBinder iBinder, int i2, int i3, boolean z, boolean z2) {
        int i4;
        if (i == -1) {
            return;
        }
        if (!z2 && (i4 = this.mLastUpdatedImeDisplayId) != i && i4 != -1) {
            sendImeInvisibleStatusForPrevNavBar();
        }
        for (int i5 = 0; i5 < this.mCallbacks.size(); i5++) {
            this.mCallbacks.get(i5).setImeWindowStatus(i, iBinder, i2, i3, z);
        }
        this.mLastUpdatedImeDisplayId = i;
    }

    private void sendImeInvisibleStatusForPrevNavBar() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).setImeWindowStatus(this.mLastUpdatedImeDisplayId, null, 4, 0, false);
        }
    }

    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        synchronized (this.mLock) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = i2;
            someArgsObtain.argi3 = z ? 1 : 0;
            someArgsObtain.arg1 = appearanceRegionArr;
            this.mHandler.obtainMessage(393216, someArgsObtain).sendToTarget();
        }
    }

    public void showTransient(int i, int[] iArr) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3145728, i, 0, iArr).sendToTarget();
        }
    }

    public void abortTransient(int i, int[] iArr) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3211264, i, 0, iArr).sendToTarget();
        }
    }

    public void startTracing() {
        synchronized (this.mLock) {
            ProtoTracer protoTracer = this.mProtoTracer;
            if (protoTracer != null) {
                protoTracer.start();
            }
            this.mHandler.obtainMessage(3604480, Boolean.TRUE).sendToTarget();
        }
    }

    public void stopTracing() {
        synchronized (this.mLock) {
            ProtoTracer protoTracer = this.mProtoTracer;
            if (protoTracer != null) {
                protoTracer.stop();
            }
            this.mHandler.obtainMessage(3604480, Boolean.FALSE).sendToTarget();
        }
    }

    public void suppressAmbientDisplay(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3670016, Boolean.valueOf(z)).sendToTarget();
        }
    }

    public void screenPinningStateChanged(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(3997696);
            this.mHandler.obtainMessage(3997696, z ? 1 : 0, 0, null).sendToTarget();
        }
    }

    public void leftInLandscapeChanged(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(4063232);
            this.mHandler.obtainMessage(4063232, z ? 1 : 0, 0, null).sendToTarget();
        }
    }

    private final class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = 0;
            switch (message.what & (-65536)) {
                case 65536:
                    int i2 = message.arg1;
                    if (i2 == 1) {
                        Pair pair = (Pair) message.obj;
                        while (i < CommandQueue.this.mCallbacks.size()) {
                            ((Callbacks) CommandQueue.this.mCallbacks.get(i)).setIcon((String) pair.first, (StatusBarIcon) pair.second);
                            i++;
                        }
                        break;
                    } else if (i2 == 2) {
                        while (i < CommandQueue.this.mCallbacks.size()) {
                            ((Callbacks) CommandQueue.this.mCallbacks.get(i)).removeIcon((String) message.obj);
                            i++;
                        }
                        break;
                    }
                    break;
                case LineageHardwareManager.FEATURE_COLOR_BALANCE /* 131072 */:
                    SomeArgs someArgs = (SomeArgs) message.obj;
                    for (int i3 = 0; i3 < CommandQueue.this.mCallbacks.size(); i3++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i3)).disable(someArgs.argi1, someArgs.argi2, someArgs.argi3, someArgs.argi4 != 0);
                    }
                    break;
                case 196608:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).animateExpandNotificationsPanel();
                        i++;
                    }
                    break;
                case LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT /* 262144 */:
                    for (int i4 = 0; i4 < CommandQueue.this.mCallbacks.size(); i4++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i4)).animateCollapsePanels(message.arg1, message.arg2 != 0);
                    }
                    break;
                case 327680:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).animateExpandSettingsPanel((String) message.obj);
                        i++;
                    }
                    break;
                case 393216:
                    SomeArgs someArgs2 = (SomeArgs) message.obj;
                    for (int i5 = 0; i5 < CommandQueue.this.mCallbacks.size(); i5++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i5)).onSystemBarAppearanceChanged(someArgs2.argi1, someArgs2.argi2, (AppearanceRegion[]) someArgs2.arg1, someArgs2.argi3 == 1);
                    }
                    someArgs2.recycle();
                    break;
                case 458752:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).onDisplayReady(message.arg1);
                        i++;
                    }
                    break;
                case LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES /* 524288 */:
                    SomeArgs someArgs3 = (SomeArgs) message.obj;
                    CommandQueue.this.handleShowImeButton(someArgs3.argi1, (IBinder) someArgs3.arg1, someArgs3.argi2, someArgs3.argi3, someArgs3.argi4 != 0, someArgs3.argi5 != 0);
                    break;
                case 589824:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).toggleRecentApps();
                        i++;
                    }
                    break;
                case 655360:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).preloadRecentApps();
                        i++;
                    }
                    break;
                case 720896:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).cancelPreloadRecentApps();
                        i++;
                    }
                    break;
                case 786432:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).setWindowState(message.arg1, message.arg2, ((Integer) message.obj).intValue());
                        i++;
                    }
                    break;
                case 851968:
                    for (int i6 = 0; i6 < CommandQueue.this.mCallbacks.size(); i6++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i6)).showRecentApps(message.arg1 != 0);
                    }
                    break;
                case 917504:
                    for (int i7 = 0; i7 < CommandQueue.this.mCallbacks.size(); i7++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i7)).hideRecentApps(message.arg1 != 0, message.arg2 != 0);
                    }
                    break;
                case 1179648:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showScreenPinningRequest(message.arg1);
                        i++;
                    }
                    break;
                case 1245184:
                    for (int i8 = 0; i8 < CommandQueue.this.mCallbacks.size(); i8++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i8)).appTransitionPending(message.arg1, message.arg2 != 0);
                    }
                    break;
                case 1310720:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).appTransitionCancelled(message.arg1);
                        i++;
                    }
                    break;
                case 1376256:
                    SomeArgs someArgs4 = (SomeArgs) message.obj;
                    for (int i9 = 0; i9 < CommandQueue.this.mCallbacks.size(); i9++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i9)).appTransitionStarting(someArgs4.argi1, ((Long) someArgs4.arg1).longValue(), ((Long) someArgs4.arg2).longValue(), someArgs4.argi2 != 0);
                    }
                    break;
                case 1441792:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showAssistDisclosure();
                        i++;
                    }
                    break;
                case 1507328:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).startAssist((Bundle) message.obj);
                        i++;
                    }
                    break;
                case 1572864:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).onCameraLaunchGestureDetected(message.arg1);
                        i++;
                    }
                    break;
                case 1638400:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).toggleKeyboardShortcutsMenu(message.arg1);
                        i++;
                    }
                    break;
                case 1703936:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showPictureInPictureMenu();
                        i++;
                    }
                    break;
                case 1769472:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).addQsTile((ComponentName) message.obj);
                        i++;
                    }
                    break;
                case 1835008:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).remQsTile((ComponentName) message.obj);
                        i++;
                    }
                    break;
                case 1900544:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).clickTile((ComponentName) message.obj);
                        i++;
                    }
                    break;
                case 1966080:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).toggleSplitScreen();
                        i++;
                    }
                    break;
                case 2031616:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).appTransitionFinished(message.arg1);
                        i++;
                    }
                    break;
                case LineageHardwareManager.FEATURE_ANTI_FLICKER /* 2097152 */:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).dismissKeyboardShortcutsMenu();
                        i++;
                    }
                    break;
                case 2162688:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).handleSystemKey(message.arg1);
                        i++;
                    }
                    break;
                case 2228224:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).handleShowGlobalActionsMenu();
                        i++;
                    }
                    break;
                case 2293760:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).togglePanel();
                        i++;
                    }
                    break;
                case 2359296:
                    for (int i10 = 0; i10 < CommandQueue.this.mCallbacks.size(); i10++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i10)).handleShowShutdownUi(message.arg1 != 0, (String) message.obj, message.arg2 != 0);
                    }
                    break;
                case 2424832:
                    for (int i11 = 0; i11 < CommandQueue.this.mCallbacks.size(); i11++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i11)).setTopAppHidesStatusBar(message.arg1 != 0);
                    }
                    break;
                case 2490368:
                    for (int i12 = 0; i12 < CommandQueue.this.mCallbacks.size(); i12++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i12)).onRotationProposal(message.arg1, message.arg2 != 0);
                    }
                    break;
                case 2555904:
                    CommandQueue.this.mHandler.removeMessages(2752512);
                    CommandQueue.this.mHandler.removeMessages(2686976);
                    CommandQueue.this.mHandler.removeMessages(2621440);
                    SomeArgs someArgs5 = (SomeArgs) message.obj;
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showAuthenticationDialog((Bundle) someArgs5.arg1, (IBiometricServiceReceiverInternal) someArgs5.arg2, someArgs5.argi1, ((Boolean) someArgs5.arg3).booleanValue(), someArgs5.argi2, (String) someArgs5.arg4, ((Long) someArgs5.arg5).longValue(), someArgs5.argi3);
                        i++;
                    }
                    someArgs5.recycle();
                    break;
                case 2621440:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).onBiometricAuthenticated();
                        i++;
                    }
                    break;
                case 2686976:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).onBiometricHelp((String) message.obj);
                        i++;
                    }
                    break;
                case 2752512:
                    SomeArgs someArgs6 = (SomeArgs) message.obj;
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).onBiometricError(someArgs6.argi1, someArgs6.argi2, someArgs6.argi3);
                        i++;
                    }
                    someArgs6.recycle();
                    break;
                case 2818048:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).hideAuthenticationDialog();
                        i++;
                    }
                    break;
                case 2883584:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showWirelessChargingAnimation(message.arg1);
                        i++;
                    }
                    break;
                case 2949120:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showPinningEnterExitToast(((Boolean) message.obj).booleanValue());
                        i++;
                    }
                    break;
                case 3014656:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showPinningEscapeToast();
                        i++;
                    }
                    break;
                case 3080192:
                    for (int i13 = 0; i13 < CommandQueue.this.mCallbacks.size(); i13++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i13)).onRecentsAnimationStateChanged(message.arg1 > 0);
                    }
                    break;
                case 3145728:
                    int i14 = message.arg1;
                    int[] iArr = (int[]) message.obj;
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showTransient(i14, iArr);
                        i++;
                    }
                    break;
                case 3211264:
                    int i15 = message.arg1;
                    int[] iArr2 = (int[]) message.obj;
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).abortTransient(i15, iArr2);
                        i++;
                    }
                    break;
                case 3276800:
                    SomeArgs someArgs7 = (SomeArgs) message.obj;
                    for (int i16 = 0; i16 < CommandQueue.this.mCallbacks.size(); i16++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i16)).topAppWindowChanged(someArgs7.argi1, someArgs7.argi2 != 0, someArgs7.argi3 != 0);
                    }
                    someArgs7.recycle();
                    break;
                case 3342336:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showInattentiveSleepWarning();
                        i++;
                    }
                    break;
                case 3407872:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).dismissInattentiveSleepWarning(((Boolean) message.obj).booleanValue());
                        i++;
                    }
                    break;
                case 3473408:
                    SomeArgs someArgs8 = (SomeArgs) message.obj;
                    String str = (String) someArgs8.arg1;
                    IBinder iBinder = (IBinder) someArgs8.arg2;
                    CharSequence charSequence = (CharSequence) someArgs8.arg3;
                    IBinder iBinder2 = (IBinder) someArgs8.arg4;
                    ITransientNotificationCallback iTransientNotificationCallback = (ITransientNotificationCallback) someArgs8.arg5;
                    int i17 = someArgs8.argi1;
                    int i18 = someArgs8.argi2;
                    Iterator it = CommandQueue.this.mCallbacks.iterator();
                    while (it.hasNext()) {
                        ((Callbacks) it.next()).showToast(i17, str, iBinder, charSequence, iBinder2, i18, iTransientNotificationCallback);
                    }
                    break;
                case 3538944:
                    SomeArgs someArgs9 = (SomeArgs) message.obj;
                    String str2 = (String) someArgs9.arg1;
                    IBinder iBinder3 = (IBinder) someArgs9.arg2;
                    Iterator it2 = CommandQueue.this.mCallbacks.iterator();
                    while (it2.hasNext()) {
                        ((Callbacks) it2.next()).hideToast(str2, iBinder3);
                    }
                    break;
                case 3604480:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).onTracingStateChanged(((Boolean) message.obj).booleanValue());
                        i++;
                    }
                    break;
                case 3670016:
                    Iterator it3 = CommandQueue.this.mCallbacks.iterator();
                    while (it3.hasNext()) {
                        ((Callbacks) it3.next()).suppressAmbientDisplay(((Boolean) message.obj).booleanValue());
                    }
                    break;
                case 3735552:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).showInDisplayFingerprintView();
                        i++;
                    }
                    break;
                case 3801088:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).hideInDisplayFingerprintView();
                        i++;
                    }
                    break;
                case 3866624:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).setBlockedGesturalNavigation(((Boolean) message.obj).booleanValue());
                        i++;
                    }
                    break;
                case 3932160:
                    while (i < CommandQueue.this.mCallbacks.size()) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i)).toggleCameraFlash();
                        i++;
                    }
                    break;
                case 3997696:
                    for (int i19 = 0; i19 < CommandQueue.this.mCallbacks.size(); i19++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i19)).screenPinningStateChanged(message.arg1 != 0);
                    }
                    break;
                case 4063232:
                    for (int i20 = 0; i20 < CommandQueue.this.mCallbacks.size(); i20++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i20)).leftInLandscapeChanged(message.arg1 != 0);
                    }
                    break;
            }
        }
    }
}
