package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.view.SurfaceControl;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.systemui.TransactionPool;
import com.android.systemui.wm.DisplayImeController;

/* loaded from: classes.dex */
class DividerImeController implements DisplayImeController.ImePositionProcessor {
    private final Handler mHandler;
    private final SplitScreenTaskOrganizer mSplits;
    private final TransactionPool mTransactionPool;
    private int mHiddenTop = 0;
    private int mShownTop = 0;
    private boolean mTargetAdjusted = false;
    private boolean mTargetShown = false;
    private float mTargetPrimaryDim = 0.0f;
    private float mTargetSecondaryDim = 0.0f;
    private boolean mSecondaryHasFocus = false;
    private float mLastPrimaryDim = 0.0f;
    private float mLastSecondaryDim = 0.0f;
    private int mLastAdjustTop = -1;
    private boolean mImeWasShown = false;
    private boolean mAdjusted = false;
    private ValueAnimator mAnimation = null;
    private boolean mPaused = true;
    private boolean mPausedTargetAdjusted = false;
    private boolean mAdjustedWhileHidden = false;

    DividerImeController(SplitScreenTaskOrganizer splitScreenTaskOrganizer, TransactionPool transactionPool, Handler handler) {
        this.mSplits = splitScreenTaskOrganizer;
        this.mTransactionPool = transactionPool;
        this.mHandler = handler;
    }

    private DividerView getView() {
        return this.mSplits.mDivider.getView();
    }

    private SplitDisplayLayout getLayout() {
        return this.mSplits.mDivider.getSplitLayout();
    }

    private boolean isDividerVisible() {
        return this.mSplits.mDivider.isDividerVisible();
    }

    private boolean getSecondaryHasFocus(int i) {
        WindowContainerToken imeTarget = TaskOrganizer.getImeTarget(i);
        return imeTarget != null && imeTarget.asBinder() == this.mSplits.mSecondary.token.asBinder();
    }

    void reset() {
        this.mPaused = true;
        this.mPausedTargetAdjusted = false;
        this.mAdjustedWhileHidden = false;
        this.mAnimation = null;
        this.mTargetAdjusted = false;
        this.mAdjusted = false;
        this.mTargetShown = false;
        this.mImeWasShown = false;
        this.mLastSecondaryDim = 0.0f;
        this.mLastPrimaryDim = 0.0f;
        this.mTargetSecondaryDim = 0.0f;
        this.mTargetPrimaryDim = 0.0f;
        this.mSecondaryHasFocus = false;
        this.mLastAdjustTop = -1;
    }

    private void updateDimTargets() {
        boolean z = !getView().isHidden();
        boolean z2 = this.mSecondaryHasFocus;
        this.mTargetPrimaryDim = (z2 && this.mTargetShown && z) ? 0.3f : 0.0f;
        this.mTargetSecondaryDim = (!z2 && this.mTargetShown && z) ? 0.3f : 0.0f;
    }

    @Override // com.android.systemui.wm.DisplayImeController.ImePositionProcessor
    public int onImeStartPositioning(int i, int i2, int i3, boolean z, boolean z2, SurfaceControl.Transaction transaction) {
        this.mHiddenTop = i2;
        this.mShownTop = i3;
        this.mTargetShown = z;
        if (!isDividerVisible()) {
            return 0;
        }
        boolean z3 = !getView().isHidden();
        boolean secondaryHasFocus = getSecondaryHasFocus(i);
        this.mSecondaryHasFocus = secondaryHasFocus;
        boolean z4 = z3 && z && secondaryHasFocus && !z2 && !getLayout().mDisplayLayout.isLandscape() && !this.mSplits.mDivider.isMinimized();
        int i4 = this.mLastAdjustTop;
        if (i4 < 0) {
            if (!z) {
                i2 = i3;
            }
            this.mLastAdjustTop = i2;
        } else {
            if (i4 != (z ? this.mShownTop : this.mHiddenTop)) {
                boolean z5 = this.mTargetAdjusted;
                if (z5 != z4 && z4 == this.mAdjusted) {
                    this.mAdjusted = z5;
                } else if (z4 && z5 && this.mAdjusted) {
                    this.mAdjusted = false;
                }
            }
        }
        if (this.mPaused) {
            this.mPausedTargetAdjusted = z4;
            return (z4 || this.mAdjusted) ? 1 : 0;
        }
        this.mTargetAdjusted = z4;
        updateDimTargets();
        if (this.mAnimation != null || (this.mImeWasShown && z && this.mTargetAdjusted != this.mAdjusted)) {
            startAsyncAnimation();
        }
        if (z3) {
            updateImeAdjustState();
        } else {
            this.mAdjustedWhileHidden = true;
        }
        return (this.mTargetAdjusted || this.mAdjusted) ? 1 : 0;
    }

    private void updateImeAdjustState() {
        updateImeAdjustState(false);
    }

    private void updateImeAdjustState(boolean z) {
        DividerView view;
        boolean z2 = false;
        if (this.mAdjusted != this.mTargetAdjusted || z) {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            SplitDisplayLayout layout = getLayout();
            if (this.mTargetAdjusted) {
                int i = this.mShownTop;
                layout.updateAdjustedBounds(i, this.mHiddenTop, i);
                windowContainerTransaction.setBounds(this.mSplits.mSecondary.token, layout.mAdjustedSecondary);
                Rect rect = new Rect(this.mSplits.mSecondary.configuration.windowConfiguration.getAppBounds());
                rect.offset(0, layout.mAdjustedSecondary.top - layout.mSecondary.top);
                windowContainerTransaction.setAppBounds(this.mSplits.mSecondary.token, rect);
                ActivityManager.RunningTaskInfo runningTaskInfo = this.mSplits.mSecondary;
                WindowContainerToken windowContainerToken = runningTaskInfo.token;
                Configuration configuration = runningTaskInfo.configuration;
                windowContainerTransaction.setScreenSizeDp(windowContainerToken, configuration.screenWidthDp, configuration.screenHeightDp);
                windowContainerTransaction.setBounds(this.mSplits.mPrimary.token, layout.mAdjustedPrimary);
                Rect rect2 = new Rect(this.mSplits.mPrimary.configuration.windowConfiguration.getAppBounds());
                rect2.offset(0, layout.mAdjustedPrimary.top - layout.mPrimary.top);
                windowContainerTransaction.setAppBounds(this.mSplits.mPrimary.token, rect2);
                ActivityManager.RunningTaskInfo runningTaskInfo2 = this.mSplits.mPrimary;
                WindowContainerToken windowContainerToken2 = runningTaskInfo2.token;
                Configuration configuration2 = runningTaskInfo2.configuration;
                windowContainerTransaction.setScreenSizeDp(windowContainerToken2, configuration2.screenWidthDp, configuration2.screenHeightDp);
            } else {
                windowContainerTransaction.setBounds(this.mSplits.mSecondary.token, layout.mSecondary);
                windowContainerTransaction.setAppBounds(this.mSplits.mSecondary.token, (Rect) null);
                windowContainerTransaction.setScreenSizeDp(this.mSplits.mSecondary.token, 0, 0);
                windowContainerTransaction.setBounds(this.mSplits.mPrimary.token, layout.mPrimary);
                windowContainerTransaction.setAppBounds(this.mSplits.mPrimary.token, (Rect) null);
                windowContainerTransaction.setScreenSizeDp(this.mSplits.mPrimary.token, 0, 0);
            }
            if (!this.mSplits.mDivider.getWmProxy().queueSyncTransactionIfWaiting(windowContainerTransaction)) {
                WindowOrganizer.applyTransaction(windowContainerTransaction);
            }
        }
        if (!this.mPaused && (view = getView()) != null) {
            boolean z3 = this.mTargetShown;
            view.setAdjustedForIme(z3, z3 ? 275L : 340L);
        }
        Divider divider = this.mSplits.mDivider;
        if (this.mTargetShown && !this.mPaused) {
            z2 = true;
        }
        divider.setAdjustedForIme(z2);
    }

    public void updateAdjustForIme() {
        updateImeAdjustState(this.mAdjustedWhileHidden);
        this.mAdjustedWhileHidden = false;
    }

    @Override // com.android.systemui.wm.DisplayImeController.ImePositionProcessor
    public void onImePositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        if (this.mAnimation == null && isDividerVisible() && !this.mPaused) {
            float f = (i2 - this.mHiddenTop) / (this.mShownTop - r3);
            if (!this.mTargetShown) {
                f = 1.0f - f;
            }
            onProgress(f, transaction);
        }
    }

    @Override // com.android.systemui.wm.DisplayImeController.ImePositionProcessor
    public void onImeEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
        if (this.mAnimation == null && isDividerVisible() && !this.mPaused) {
            onEnd(z, transaction);
        }
    }

    private void onProgress(float f, SurfaceControl.Transaction transaction) {
        DividerView view = getView();
        if (this.mTargetAdjusted != this.mAdjusted && !this.mPaused) {
            SplitDisplayLayout layout = getLayout();
            float f2 = this.mTargetAdjusted ? f : 1.0f - f;
            int i = this.mShownTop;
            int i2 = this.mHiddenTop;
            int i3 = (int) ((i * f2) + ((1.0f - f2) * i2));
            this.mLastAdjustTop = i3;
            layout.updateAdjustedBounds(i3, i2, i);
            view.resizeSplitSurfaces(transaction, layout.mAdjustedPrimary, layout.mAdjustedSecondary);
        }
        float f3 = 1.0f - f;
        view.setResizeDimLayer(transaction, true, (this.mLastPrimaryDim * f3) + (this.mTargetPrimaryDim * f));
        view.setResizeDimLayer(transaction, false, (this.mLastSecondaryDim * f3) + (f * this.mTargetSecondaryDim));
    }

    void setDimsHidden(SurfaceControl.Transaction transaction, boolean z) {
        DividerView view = getView();
        if (z) {
            view.setResizeDimLayer(transaction, true, 0.0f);
            view.setResizeDimLayer(transaction, false, 0.0f);
        } else {
            updateDimTargets();
            view.setResizeDimLayer(transaction, true, this.mTargetPrimaryDim);
            view.setResizeDimLayer(transaction, false, this.mTargetSecondaryDim);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEnd(boolean z, SurfaceControl.Transaction transaction) {
        if (z) {
            return;
        }
        onProgress(1.0f, transaction);
        boolean z2 = this.mTargetAdjusted;
        this.mAdjusted = z2;
        this.mImeWasShown = this.mTargetShown;
        this.mLastAdjustTop = z2 ? this.mShownTop : this.mHiddenTop;
        this.mLastPrimaryDim = this.mTargetPrimaryDim;
        this.mLastSecondaryDim = this.mTargetSecondaryDim;
    }

    private void startAsyncAnimation() {
        ValueAnimator valueAnimator = this.mAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mAnimation = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(275L);
        boolean z = this.mTargetAdjusted;
        if (z != this.mAdjusted) {
            float f = (this.mLastAdjustTop - this.mHiddenTop) / (this.mShownTop - r2);
            if (!z) {
                f = 1.0f - f;
            }
            this.mAnimation.setCurrentFraction(f);
        }
        this.mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.stackdivider.DividerImeController$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                this.f$0.lambda$startAsyncAnimation$0(valueAnimator2);
            }
        });
        this.mAnimation.setInterpolator(DisplayImeController.INTERPOLATOR);
        this.mAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.stackdivider.DividerImeController.1
            private boolean mCancel = false;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancel = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                SurfaceControl.Transaction transactionAcquire = DividerImeController.this.mTransactionPool.acquire();
                DividerImeController.this.onEnd(this.mCancel, transactionAcquire);
                transactionAcquire.apply();
                DividerImeController.this.mTransactionPool.release(transactionAcquire);
                DividerImeController.this.mAnimation = null;
            }
        });
        this.mAnimation.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startAsyncAnimation$0(ValueAnimator valueAnimator) {
        SurfaceControl.Transaction transactionAcquire = this.mTransactionPool.acquire();
        onProgress(((Float) valueAnimator.getAnimatedValue()).floatValue(), transactionAcquire);
        transactionAcquire.apply();
        this.mTransactionPool.release(transactionAcquire);
    }

    public void pause(int i) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.stackdivider.DividerImeController$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$pause$1();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$pause$1() {
        if (this.mPaused) {
            return;
        }
        this.mPaused = true;
        this.mPausedTargetAdjusted = this.mTargetAdjusted;
        this.mTargetAdjusted = false;
        this.mTargetSecondaryDim = 0.0f;
        this.mTargetPrimaryDim = 0.0f;
        updateImeAdjustState();
        startAsyncAnimation();
        ValueAnimator valueAnimator = this.mAnimation;
        if (valueAnimator != null) {
            valueAnimator.end();
        }
    }

    public void resume(int i) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.stackdivider.DividerImeController$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$resume$2();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resume$2() {
        if (this.mPaused) {
            this.mPaused = false;
            this.mTargetAdjusted = this.mPausedTargetAdjusted;
            updateDimTargets();
            DividerView view = getView();
            if (this.mTargetAdjusted != this.mAdjusted && !this.mSplits.mDivider.isMinimized() && view != null) {
                view.finishAnimations();
            }
            updateImeAdjustState();
            startAsyncAnimation();
        }
    }
}
