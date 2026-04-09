package com.android.systemui.biometrics;

import android.R;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.TaskStackListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.systemui.SystemUI;
import com.android.systemui.biometrics.AuthContainerView;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.statusbar.CommandQueue;
import java.util.List;

/* loaded from: classes.dex */
public class AuthController extends SystemUI implements CommandQueue.Callbacks, AuthDialogCallback {

    @VisibleForTesting
    IActivityTaskManager mActivityTaskManager;

    @VisibleForTesting
    final BroadcastReceiver mBroadcastReceiver;
    private final CommandQueue mCommandQueue;

    @VisibleForTesting
    AuthDialog mCurrentDialog;
    private SomeArgs mCurrentDialogArgs;
    private Handler mHandler;
    private final Injector mInjector;

    @VisibleForTesting
    IBiometricServiceReceiverInternal mReceiver;
    private final Runnable mTaskStackChangedRunnable;

    @VisibleForTesting
    BiometricTaskStackListener mTaskStackListener;
    private WindowManager mWindowManager;

    public class BiometricTaskStackListener extends TaskStackListener {
        public BiometricTaskStackListener() {
        }

        public void onTaskStackChanged() {
            AuthController.this.mHandler.post(AuthController.this.mTaskStackChangedRunnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        AuthDialog authDialog = this.mCurrentDialog;
        if (authDialog != null) {
            try {
                String opPackageName = authDialog.getOpPackageName();
                Log.w("BiometricPrompt/AuthController", "Task stack changed, current client: " + opPackageName);
                List tasks = this.mActivityTaskManager.getTasks(1);
                if (tasks.isEmpty()) {
                    return;
                }
                String packageName = ((ActivityManager.RunningTaskInfo) tasks.get(0)).topActivity.getPackageName();
                if (packageName.contentEquals(opPackageName)) {
                    return;
                }
                Log.w("BiometricPrompt/AuthController", "Evicting client due to: " + packageName);
                this.mCurrentDialog.dismissWithoutCallback(true);
                this.mCurrentDialog = null;
                IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
                if (iBiometricServiceReceiverInternal != null) {
                    iBiometricServiceReceiverInternal.onDialogDismissed(3, (byte[]) null);
                    this.mReceiver = null;
                }
            } catch (RemoteException e) {
                Log.e("BiometricPrompt/AuthController", "Remote exception", e);
            }
        }
    }

    @Override // com.android.systemui.biometrics.AuthDialogCallback
    public void onTryAgainPressed() {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/AuthController", "onTryAgainPressed: Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onTryAgainPressed();
        } catch (RemoteException e) {
            Log.e("BiometricPrompt/AuthController", "RemoteException when handling try again", e);
        }
    }

    @Override // com.android.systemui.biometrics.AuthDialogCallback
    public void onDeviceCredentialPressed() {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/AuthController", "onDeviceCredentialPressed: Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onDeviceCredentialPressed();
        } catch (RemoteException e) {
            Log.e("BiometricPrompt/AuthController", "RemoteException when handling credential button", e);
        }
    }

    @Override // com.android.systemui.biometrics.AuthDialogCallback
    public void onSystemEvent(int i) {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/AuthController", "onSystemEvent(" + i + "): Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onSystemEvent(i);
        } catch (RemoteException e) {
            Log.e("BiometricPrompt/AuthController", "RemoteException when sending system event", e);
        }
    }

    @Override // com.android.systemui.biometrics.AuthDialogCallback
    public void onDismissed(int i, byte[] bArr) {
        switch (i) {
            case 1:
                sendResultAndCleanUp(3, bArr);
                break;
            case 2:
                sendResultAndCleanUp(2, bArr);
                break;
            case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                sendResultAndCleanUp(1, bArr);
                break;
            case 4:
                sendResultAndCleanUp(4, bArr);
                break;
            case 5:
                sendResultAndCleanUp(5, bArr);
                break;
            case 6:
                sendResultAndCleanUp(6, bArr);
                break;
            case 7:
                sendResultAndCleanUp(7, bArr);
                break;
            default:
                Log.e("BiometricPrompt/AuthController", "Unhandled reason: " + i);
                break;
        }
    }

    private void sendResultAndCleanUp(int i, byte[] bArr) {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/AuthController", "sendResultAndCleanUp: Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onDialogDismissed(i, bArr);
        } catch (RemoteException e) {
            Log.w("BiometricPrompt/AuthController", "Remote exception", e);
        }
        onDialogDismissed(i);
    }

    public static class Injector {
        IActivityTaskManager getActivityTaskManager() {
            return ActivityTaskManager.getService();
        }
    }

    public AuthController(Context context, CommandQueue commandQueue) {
        this(context, commandQueue, new Injector());
    }

    @VisibleForTesting
    AuthController(Context context, CommandQueue commandQueue, Injector injector) {
        super(context);
        this.mHandler = new Handler(Looper.getMainLooper());
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.biometrics.AuthController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if (AuthController.this.mCurrentDialog == null || !"android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                    return;
                }
                Log.w("BiometricPrompt/AuthController", "ACTION_CLOSE_SYSTEM_DIALOGS received");
                AuthController.this.mCurrentDialog.dismissWithoutCallback(true);
                AuthController authController = AuthController.this;
                authController.mCurrentDialog = null;
                try {
                    IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = authController.mReceiver;
                    if (iBiometricServiceReceiverInternal != null) {
                        iBiometricServiceReceiverInternal.onDialogDismissed(3, (byte[]) null);
                        AuthController.this.mReceiver = null;
                    }
                } catch (RemoteException e) {
                    Log.e("BiometricPrompt/AuthController", "Remote exception", e);
                }
            }
        };
        this.mBroadcastReceiver = broadcastReceiver;
        this.mTaskStackChangedRunnable = new Runnable() { // from class: com.android.systemui.biometrics.AuthController$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$0();
            }
        };
        this.mCommandQueue = commandQueue;
        this.mInjector = injector;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mActivityTaskManager = this.mInjector.getActivityTaskManager();
        try {
            BiometricTaskStackListener biometricTaskStackListener = new BiometricTaskStackListener();
            this.mTaskStackListener = biometricTaskStackListener;
            this.mActivityTaskManager.registerTaskStackListener(biometricTaskStackListener);
        } catch (RemoteException e) {
            Log.w("BiometricPrompt/AuthController", "Unable to register task stack listener", e);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        boolean z2;
        Log.d("BiometricPrompt/AuthController", "showAuthenticationDialog, authenticators: " + Utils.getAuthenticators(bundle) + ", biometricModality: " + i + ", requireConfirmation: " + z + ", operationId: " + j + ", sysUiSessionId: " + i3);
        SomeArgs someArgsObtain = SomeArgs.obtain();
        someArgsObtain.arg1 = bundle;
        someArgsObtain.arg2 = iBiometricServiceReceiverInternal;
        someArgsObtain.argi1 = i;
        someArgsObtain.arg3 = Boolean.valueOf(z);
        someArgsObtain.argi2 = i2;
        someArgsObtain.arg4 = str;
        someArgsObtain.arg5 = Long.valueOf(j);
        someArgsObtain.argi3 = i3;
        if (this.mCurrentDialog != null) {
            Log.w("BiometricPrompt/AuthController", "mCurrentDialog: " + this.mCurrentDialog);
            z2 = true;
        } else {
            z2 = false;
        }
        showDialog(someArgsObtain, z2, null);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onBiometricAuthenticated() {
        this.mCurrentDialog.onAuthenticationSucceeded();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onBiometricHelp(String str) {
        Log.d("BiometricPrompt/AuthController", "onBiometricHelp: " + str);
        this.mCurrentDialog.onHelp(str);
    }

    private String getErrorString(int i, int i2, int i3) {
        if (i != 2) {
            return i != 8 ? "" : FaceManager.getErrorString(this.mContext, i2, i3);
        }
        return FingerprintManager.getErrorString(this.mContext, i2, i3);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onBiometricError(int i, int i2, int i3) {
        String errorString;
        Log.d("BiometricPrompt/AuthController", String.format("onBiometricError(%d, %d, %d)", Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)));
        boolean z = i2 == 7 || i2 == 9;
        boolean z2 = i2 == 100 || i2 == 3;
        AuthDialog authDialog = this.mCurrentDialog;
        if (authDialog != null) {
            if (authDialog.isAllowDeviceCredentials() && z) {
                Log.d("BiometricPrompt/AuthController", "onBiometricError, lockout");
                this.mCurrentDialog.animateToCredentialUI();
                return;
            }
            if (z2) {
                if (i2 == 100) {
                    errorString = this.mContext.getString(R.string.biometric_error_canceled);
                } else {
                    errorString = getErrorString(i, i2, i3);
                }
                Log.d("BiometricPrompt/AuthController", "onBiometricError, soft error: " + errorString);
                this.mCurrentDialog.onAuthenticationFailed(errorString);
                return;
            }
            String errorString2 = getErrorString(i, i2, i3);
            Log.d("BiometricPrompt/AuthController", "onBiometricError, hard error: " + errorString2);
            this.mCurrentDialog.onError(errorString2);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void hideAuthenticationDialog() {
        Log.d("BiometricPrompt/AuthController", "hideAuthenticationDialog: " + this.mCurrentDialog);
        AuthDialog authDialog = this.mCurrentDialog;
        if (authDialog == null) {
            return;
        }
        authDialog.dismissFromSystemServer();
        this.mCurrentDialog = null;
    }

    private void showDialog(SomeArgs someArgs, boolean z, Bundle bundle) {
        this.mCurrentDialogArgs = someArgs;
        int i = someArgs.argi1;
        Bundle bundle2 = (Bundle) someArgs.arg1;
        boolean zBooleanValue = ((Boolean) someArgs.arg3).booleanValue();
        int i2 = someArgs.argi2;
        String str = (String) someArgs.arg4;
        long jLongValue = ((Long) someArgs.arg5).longValue();
        int i3 = someArgs.argi3;
        AuthDialog authDialogBuildDialog = buildDialog(bundle2, zBooleanValue, i2, i, str, z, jLongValue, i3);
        if (authDialogBuildDialog == null) {
            Log.e("BiometricPrompt/AuthController", "Unsupported type: " + i);
            return;
        }
        Log.d("BiometricPrompt/AuthController", "userId: " + i2 + " savedState: " + bundle + " mCurrentDialog: " + this.mCurrentDialog + " newDialog: " + authDialogBuildDialog + " type: " + i + " sysUiSessionId: " + i3);
        AuthDialog authDialog = this.mCurrentDialog;
        if (authDialog != null) {
            authDialog.dismissWithoutCallback(false);
        }
        this.mReceiver = (IBiometricServiceReceiverInternal) someArgs.arg2;
        this.mCurrentDialog = authDialogBuildDialog;
        authDialogBuildDialog.show(this.mWindowManager, bundle);
    }

    private void onDialogDismissed(int i) {
        Log.d("BiometricPrompt/AuthController", "onDialogDismissed: " + i);
        if (this.mCurrentDialog == null) {
            Log.w("BiometricPrompt/AuthController", "Dialog already dismissed");
        }
        this.mReceiver = null;
        this.mCurrentDialog = null;
    }

    @Override // com.android.systemui.SystemUI
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mCurrentDialog != null) {
            Bundle bundle = new Bundle();
            this.mCurrentDialog.onSaveState(bundle);
            this.mCurrentDialog.dismissWithoutCallback(false);
            this.mCurrentDialog = null;
            if (bundle.getInt("container_state") != 4) {
                if (bundle.getBoolean("credential_showing")) {
                    ((Bundle) this.mCurrentDialogArgs.arg1).putInt("authenticators_allowed", 32768);
                }
                showDialog(this.mCurrentDialogArgs, true, bundle);
            }
        }
    }

    protected AuthDialog buildDialog(Bundle bundle, boolean z, int i, int i2, String str, boolean z2, long j, int i3) {
        return new AuthContainerView.Builder(this.mContext).setCallback(this).setBiometricPromptBundle(bundle).setRequireConfirmation(z).setUserId(i).setOpPackageName(str).setSkipIntro(z2).setOperationId(j).setSysUiSessionId(i3).build(i2);
    }
}
