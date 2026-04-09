package com.android.systemui.wm;

import android.os.Handler;
import android.os.RemoteException;
import android.view.IDisplayWindowRotationCallback;
import android.view.IDisplayWindowRotationController;
import android.view.IWindowManager;
import android.window.WindowContainerTransaction;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class DisplayChangeController {
    private final IDisplayWindowRotationController mDisplayRotationController;
    private final Handler mHandler;
    private final ArrayList<OnDisplayChangingListener> mRotationListener = new ArrayList<>();
    private final ArrayList<OnDisplayChangingListener> mTmpListeners = new ArrayList<>();
    private final IWindowManager mWmService;

    public interface OnDisplayChangingListener {
        void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction);
    }

    /* renamed from: com.android.systemui.wm.DisplayChangeController$1, reason: invalid class name */
    class AnonymousClass1 extends IDisplayWindowRotationController.Stub {
        AnonymousClass1() {
        }

        public void onRotateDisplay(final int i, final int i2, final int i3, final IDisplayWindowRotationCallback iDisplayWindowRotationCallback) {
            DisplayChangeController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayChangeController$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onRotateDisplay$0(i, i2, i3, iDisplayWindowRotationCallback);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onRotateDisplay$0(int i, int i2, int i3, IDisplayWindowRotationCallback iDisplayWindowRotationCallback) {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            synchronized (DisplayChangeController.this.mRotationListener) {
                DisplayChangeController.this.mTmpListeners.clear();
                DisplayChangeController.this.mTmpListeners.addAll(DisplayChangeController.this.mRotationListener);
            }
            Iterator it = DisplayChangeController.this.mTmpListeners.iterator();
            while (it.hasNext()) {
                ((OnDisplayChangingListener) it.next()).onRotateDisplay(i, i2, i3, windowContainerTransaction);
            }
            try {
                iDisplayWindowRotationCallback.continueRotateDisplay(i3, windowContainerTransaction);
            } catch (RemoteException unused) {
            }
        }
    }

    public DisplayChangeController(Handler handler, IWindowManager iWindowManager) {
        AnonymousClass1 anonymousClass1 = new AnonymousClass1();
        this.mDisplayRotationController = anonymousClass1;
        this.mHandler = handler;
        this.mWmService = iWindowManager;
        try {
            iWindowManager.setDisplayWindowRotationController(anonymousClass1);
        } catch (RemoteException unused) {
            throw new RuntimeException("Unable to register rotation controller");
        }
    }

    public void addRotationListener(OnDisplayChangingListener onDisplayChangingListener) {
        synchronized (this.mRotationListener) {
            this.mRotationListener.add(onDisplayChangingListener);
        }
    }
}
