package com.android.keyguard;

import android.app.admin.IKeyguardCallback;
import android.app.admin.IKeyguardClient;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.SurfaceControlViewHost;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardSecurityModel;
import java.util.NoSuchElementException;

/* loaded from: classes.dex */
public class AdminSecondaryLockScreenController {
    private IKeyguardClient mClient;
    private final Context mContext;
    private Handler mHandler;
    private KeyguardSecurityCallback mKeyguardCallback;
    private final ViewGroup mParent;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private AdminSecurityView mView;
    private final ServiceConnection mConnection = new ServiceConnection() { // from class: com.android.keyguard.AdminSecondaryLockScreenController.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) throws RemoteException {
            AdminSecondaryLockScreenController.this.mClient = IKeyguardClient.Stub.asInterface(iBinder);
            if (!AdminSecondaryLockScreenController.this.mView.isAttachedToWindow() || AdminSecondaryLockScreenController.this.mClient == null) {
                return;
            }
            AdminSecondaryLockScreenController.this.onSurfaceReady();
            try {
                iBinder.linkToDeath(AdminSecondaryLockScreenController.this.mKeyguardClientDeathRecipient, 0);
            } catch (RemoteException e) {
                Log.e("AdminSecondaryLockScreenController", "Lost connection to secondary lockscreen service", e);
                AdminSecondaryLockScreenController.this.dismiss(KeyguardUpdateMonitor.getCurrentUser());
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            AdminSecondaryLockScreenController.this.mClient = null;
        }
    };
    private final IBinder.DeathRecipient mKeyguardClientDeathRecipient = new IBinder.DeathRecipient() { // from class: com.android.keyguard.AdminSecondaryLockScreenController$$ExternalSyntheticLambda0
        @Override // android.os.IBinder.DeathRecipient
        public final void binderDied() {
            this.f$0.lambda$new$0();
        }
    };
    private final IKeyguardCallback mCallback = new AnonymousClass2();
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.AdminSecondaryLockScreenController.3
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSecondaryLockscreenRequirementChanged(int i) {
            if (AdminSecondaryLockScreenController.this.mUpdateMonitor.getSecondaryLockscreenRequirement(i) == null) {
                AdminSecondaryLockScreenController.this.dismiss(i);
            }
        }
    };

    @VisibleForTesting
    protected SurfaceHolder.Callback mSurfaceHolderCallback = new AnonymousClass4();

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        hide();
        Log.d("AdminSecondaryLockScreenController", "KeyguardClient service died");
    }

    /* renamed from: com.android.keyguard.AdminSecondaryLockScreenController$2, reason: invalid class name */
    class AnonymousClass2 extends IKeyguardCallback.Stub {
        AnonymousClass2() {
        }

        public void onDismiss() {
            AdminSecondaryLockScreenController.this.mHandler.post(new Runnable() { // from class: com.android.keyguard.AdminSecondaryLockScreenController$2$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onDismiss$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDismiss$0() {
            AdminSecondaryLockScreenController.this.dismiss(UserHandle.getCallingUserId());
        }

        public void onRemoteContentReady(SurfaceControlViewHost.SurfacePackage surfacePackage) {
            if (AdminSecondaryLockScreenController.this.mHandler != null) {
                AdminSecondaryLockScreenController.this.mHandler.removeCallbacksAndMessages(null);
            }
            if (surfacePackage != null) {
                AdminSecondaryLockScreenController.this.mView.setChildSurfacePackage(surfacePackage);
            } else {
                AdminSecondaryLockScreenController.this.mHandler.post(new Runnable() { // from class: com.android.keyguard.AdminSecondaryLockScreenController$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onRemoteContentReady$1();
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onRemoteContentReady$1() {
            AdminSecondaryLockScreenController.this.dismiss(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    /* renamed from: com.android.keyguard.AdminSecondaryLockScreenController$4, reason: invalid class name */
    class AnonymousClass4 implements SurfaceHolder.Callback {
        @Override // android.view.SurfaceHolder.Callback
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        }

        AnonymousClass4() {
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            AdminSecondaryLockScreenController.this.mUpdateMonitor.registerCallback(AdminSecondaryLockScreenController.this.mUpdateCallback);
            if (AdminSecondaryLockScreenController.this.mClient != null) {
                AdminSecondaryLockScreenController.this.onSurfaceReady();
            }
            AdminSecondaryLockScreenController.this.mHandler.postDelayed(new Runnable() { // from class: com.android.keyguard.AdminSecondaryLockScreenController$4$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$surfaceCreated$0(currentUser);
                }
            }, 500L);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$surfaceCreated$0(int i) {
            AdminSecondaryLockScreenController.this.dismiss(i);
            Log.w("AdminSecondaryLockScreenController", "Timed out waiting for secondary lockscreen content.");
        }

        @Override // android.view.SurfaceHolder.Callback
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            AdminSecondaryLockScreenController.this.mUpdateMonitor.removeCallback(AdminSecondaryLockScreenController.this.mUpdateCallback);
        }
    }

    public AdminSecondaryLockScreenController(Context context, ViewGroup viewGroup, KeyguardUpdateMonitor keyguardUpdateMonitor, KeyguardSecurityCallback keyguardSecurityCallback, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mParent = viewGroup;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mKeyguardCallback = keyguardSecurityCallback;
        this.mView = new AdminSecurityView(context, this.mSurfaceHolderCallback);
    }

    public void show(Intent intent) {
        if (this.mClient == null) {
            this.mContext.bindService(intent, this.mConnection, 1);
        }
        if (this.mView.isAttachedToWindow()) {
            return;
        }
        this.mParent.addView(this.mView);
    }

    public void hide() {
        if (this.mView.isAttachedToWindow()) {
            this.mParent.removeView(this.mView);
        }
        IKeyguardClient iKeyguardClient = this.mClient;
        if (iKeyguardClient != null) {
            try {
                iKeyguardClient.asBinder().unlinkToDeath(this.mKeyguardClientDeathRecipient, 0);
            } catch (NoSuchElementException unused) {
                Log.w("AdminSecondaryLockScreenController", "IKeyguardClient death recipient already released");
            }
            this.mContext.unbindService(this.mConnection);
            this.mClient = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSurfaceReady() {
        try {
            IBinder hostToken = this.mView.getHostToken();
            if (hostToken != null) {
                this.mClient.onCreateKeyguardSurface(hostToken, this.mCallback);
            } else {
                hide();
            }
        } catch (RemoteException e) {
            Log.e("AdminSecondaryLockScreenController", "Error in onCreateKeyguardSurface", e);
            dismiss(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismiss(int i) {
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mView.isAttachedToWindow() && i == KeyguardUpdateMonitor.getCurrentUser()) {
            hide();
            KeyguardSecurityCallback keyguardSecurityCallback = this.mKeyguardCallback;
            if (keyguardSecurityCallback != null) {
                keyguardSecurityCallback.dismiss(true, i, true, KeyguardSecurityModel.SecurityMode.Invalid);
            }
        }
    }

    private class AdminSecurityView extends SurfaceView {
        private SurfaceHolder.Callback mSurfaceHolderCallback;

        AdminSecurityView(Context context, SurfaceHolder.Callback callback) {
            super(context);
            this.mSurfaceHolderCallback = callback;
            setZOrderOnTop(true);
        }

        @Override // android.view.SurfaceView, android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getHolder().addCallback(this.mSurfaceHolderCallback);
        }

        @Override // android.view.SurfaceView, android.view.View
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            getHolder().removeCallback(this.mSurfaceHolderCallback);
        }
    }
}
