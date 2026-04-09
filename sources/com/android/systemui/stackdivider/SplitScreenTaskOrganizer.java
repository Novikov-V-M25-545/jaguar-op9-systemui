package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.window.TaskOrganizer;

/* loaded from: classes.dex */
class SplitScreenTaskOrganizer extends TaskOrganizer {
    final Divider mDivider;
    ActivityManager.RunningTaskInfo mPrimary;
    SurfaceControl mPrimaryDim;
    SurfaceControl mPrimarySurface;
    ActivityManager.RunningTaskInfo mSecondary;
    SurfaceControl mSecondaryDim;
    SurfaceControl mSecondarySurface;
    Rect mHomeBounds = new Rect();
    private boolean mSplitScreenSupported = false;
    final SurfaceSession mSurfaceSession = new SurfaceSession();

    SplitScreenTaskOrganizer(Divider divider) {
        this.mDivider = divider;
    }

    void init() throws RemoteException {
        registerOrganizer(3);
        registerOrganizer(4);
        synchronized (this) {
            try {
                try {
                    this.mPrimary = TaskOrganizer.createRootTask(0, 3);
                    this.mSecondary = TaskOrganizer.createRootTask(0, 4);
                } catch (Exception e) {
                    unregisterOrganizer();
                    throw e;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    boolean isSplitScreenSupported() {
        return this.mSplitScreenSupported;
    }

    SurfaceControl.Transaction getTransaction() {
        return this.mDivider.mTransactionPool.acquire();
    }

    void releaseTransaction(SurfaceControl.Transaction transaction) {
        this.mDivider.mTransactionPool.release(transaction);
    }

    public void onTaskAppeared(ActivityManager.RunningTaskInfo runningTaskInfo, SurfaceControl surfaceControl) {
        synchronized (this) {
            ActivityManager.RunningTaskInfo runningTaskInfo2 = this.mPrimary;
            if (runningTaskInfo2 != null && this.mSecondary != null) {
                if (runningTaskInfo.token.equals(runningTaskInfo2.token)) {
                    this.mPrimarySurface = surfaceControl;
                } else if (runningTaskInfo.token.equals(this.mSecondary.token)) {
                    this.mSecondarySurface = surfaceControl;
                }
                if (!this.mSplitScreenSupported && this.mPrimarySurface != null && this.mSecondarySurface != null) {
                    this.mSplitScreenSupported = true;
                    this.mPrimaryDim = new SurfaceControl.Builder(this.mSurfaceSession).setParent(this.mPrimarySurface).setColorLayer().setName("Primary Divider Dim").setCallsite("SplitScreenTaskOrganizer.onTaskAppeared").build();
                    this.mSecondaryDim = new SurfaceControl.Builder(this.mSurfaceSession).setParent(this.mSecondarySurface).setColorLayer().setName("Secondary Divider Dim").setCallsite("SplitScreenTaskOrganizer.onTaskAppeared").build();
                    SurfaceControl.Transaction transaction = getTransaction();
                    transaction.setLayer(this.mPrimaryDim, Integer.MAX_VALUE);
                    transaction.setColor(this.mPrimaryDim, new float[]{0.0f, 0.0f, 0.0f});
                    transaction.setLayer(this.mSecondaryDim, Integer.MAX_VALUE);
                    transaction.setColor(this.mSecondaryDim, new float[]{0.0f, 0.0f, 0.0f});
                    transaction.apply();
                    releaseTransaction(transaction);
                }
                return;
            }
            Log.w("SplitScreenTaskOrg", "Received onTaskAppeared before creating root tasks " + runningTaskInfo);
        }
    }

    public void onTaskVanished(ActivityManager.RunningTaskInfo runningTaskInfo) {
        synchronized (this) {
            ActivityManager.RunningTaskInfo runningTaskInfo2 = this.mPrimary;
            boolean z = true;
            boolean z2 = runningTaskInfo2 != null && runningTaskInfo.token.equals(runningTaskInfo2.token);
            ActivityManager.RunningTaskInfo runningTaskInfo3 = this.mSecondary;
            if (runningTaskInfo3 == null || !runningTaskInfo.token.equals(runningTaskInfo3.token)) {
                z = false;
            }
            if (this.mSplitScreenSupported && (z2 || z)) {
                this.mSplitScreenSupported = false;
                SurfaceControl.Transaction transaction = getTransaction();
                transaction.remove(this.mPrimaryDim);
                transaction.remove(this.mSecondaryDim);
                transaction.remove(this.mPrimarySurface);
                transaction.remove(this.mSecondarySurface);
                transaction.apply();
                releaseTransaction(transaction);
                this.mDivider.onTaskVanished();
            }
        }
    }

    public void onTaskInfoChanged(final ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo.displayId != 0) {
            return;
        }
        this.mDivider.getHandler().post(new Runnable() { // from class: com.android.systemui.stackdivider.SplitScreenTaskOrganizer$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.lambda$onTaskInfoChanged$0(runningTaskInfo);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: handleTaskInfoChanged, reason: merged with bridge method [inline-methods] */
    public void lambda$onTaskInfoChanged$0(ActivityManager.RunningTaskInfo runningTaskInfo) throws Resources.NotFoundException {
        if (!this.mSplitScreenSupported) {
            Log.e("SplitScreenTaskOrg", "Got handleTaskInfoChanged when not initialized: " + runningTaskInfo);
            return;
        }
        int i = this.mSecondary.topActivityType;
        boolean z = false;
        boolean z2 = i == 2 || (i == 3 && this.mDivider.isHomeStackResizable());
        boolean z3 = this.mPrimary.topActivityType == 0;
        boolean z4 = this.mSecondary.topActivityType == 0;
        if (runningTaskInfo.token.asBinder() == this.mPrimary.token.asBinder()) {
            this.mPrimary = runningTaskInfo;
        } else if (runningTaskInfo.token.asBinder() == this.mSecondary.token.asBinder()) {
            this.mSecondary = runningTaskInfo;
        }
        boolean z5 = this.mPrimary.topActivityType == 0;
        int i2 = this.mSecondary.topActivityType;
        boolean z6 = i2 == 0;
        if (i2 == 2 || (i2 == 3 && this.mDivider.isHomeStackResizable())) {
            z = true;
        }
        if (z5 == z3 && z4 == z6 && z2 == z) {
            return;
        }
        if (!z5 && !z6) {
            if (z) {
                this.mDivider.ensureMinimizedSplit();
                return;
            } else {
                this.mDivider.ensureNormalSplit();
                return;
            }
        }
        if (this.mDivider.isDividerVisible()) {
            this.mDivider.startDismissSplit();
        } else if (!z5 && z3 && z4) {
            this.mDivider.startEnterSplit();
        }
    }
}
