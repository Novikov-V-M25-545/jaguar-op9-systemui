package com.android.systemui.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.util.SparseArray;
import android.view.IDisplayWindowInsetsController;
import android.view.InsetsSource;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.SurfaceControl;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.view.IInputMethodManager;
import com.android.systemui.TransactionPool;
import com.android.systemui.wm.DisplayController;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class DisplayImeController implements DisplayController.OnDisplaysChangedListener {
    public static final Interpolator INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    final Handler mHandler;
    final SparseArray<PerDisplay> mImePerDisplay = new SparseArray<>();
    final ArrayList<ImePositionProcessor> mPositionProcessors = new ArrayList<>();
    SystemWindows mSystemWindows;
    final TransactionPool mTransactionPool;

    public interface ImePositionProcessor {
        default void onImeEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
        }

        default void onImePositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        }

        default int onImeStartPositioning(int i, int i2, int i3, boolean z, boolean z2, SurfaceControl.Transaction transaction) {
            return 0;
        }
    }

    public DisplayImeController(SystemWindows systemWindows, DisplayController displayController, Handler handler, TransactionPool transactionPool) {
        this.mHandler = handler;
        this.mSystemWindows = systemWindows;
        this.mTransactionPool = transactionPool;
        displayController.addDisplayWindowListener(this);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayAdded(int i) {
        PerDisplay perDisplay = new PerDisplay(i, this.mSystemWindows.mDisplayController.getDisplayLayout(i).rotation());
        try {
            this.mSystemWindows.mWmService.setDisplayWindowInsetsController(i, perDisplay);
        } catch (RemoteException unused) {
            Slog.w("DisplayImeController", "Unable to set insets controller on display " + i);
        }
        this.mImePerDisplay.put(i, perDisplay);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayConfigurationChanged(int i, Configuration configuration) {
        PerDisplay perDisplay = this.mImePerDisplay.get(i);
        if (perDisplay == null || this.mSystemWindows.mDisplayController.getDisplayLayout(i).rotation() == perDisplay.mRotation || !isImeShowing(i)) {
            return;
        }
        perDisplay.startAnimation(true, false);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayRemoved(int i) {
        try {
            this.mSystemWindows.mWmService.setDisplayWindowInsetsController(i, (IDisplayWindowInsetsController) null);
        } catch (RemoteException unused) {
            Slog.w("DisplayImeController", "Unable to remove insets controller on display " + i);
        }
        this.mImePerDisplay.remove(i);
    }

    private boolean isImeShowing(int i) {
        InsetsSource source;
        PerDisplay perDisplay = this.mImePerDisplay.get(i);
        return (perDisplay == null || (source = perDisplay.mInsetsState.getSource(17)) == null || perDisplay.mImeSourceControl == null || !source.isVisible()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchPositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        synchronized (this.mPositionProcessors) {
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                it.next().onImePositionChanged(i, i2, transaction);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int dispatchStartPositioning(int i, int i2, int i3, boolean z, boolean z2, SurfaceControl.Transaction transaction) {
        int iOnImeStartPositioning;
        synchronized (this.mPositionProcessors) {
            iOnImeStartPositioning = 0;
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                iOnImeStartPositioning |= it.next().onImeStartPositioning(i, i2, i3, z, z2, transaction);
            }
        }
        return iOnImeStartPositioning;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
        synchronized (this.mPositionProcessors) {
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                it.next().onImeEndPositioning(i, z, transaction);
            }
        }
    }

    public void addPositionProcessor(ImePositionProcessor imePositionProcessor) {
        synchronized (this.mPositionProcessors) {
            if (this.mPositionProcessors.contains(imePositionProcessor)) {
                return;
            }
            this.mPositionProcessors.add(imePositionProcessor);
        }
    }

    class PerDisplay extends IDisplayWindowInsetsController.Stub {
        final int mDisplayId;
        int mRotation;
        final InsetsState mInsetsState = new InsetsState();
        InsetsSourceControl mImeSourceControl = null;
        int mAnimationDirection = 0;
        ValueAnimator mAnimation = null;
        boolean mImeShowing = false;
        final Rect mImeFrame = new Rect();
        boolean mAnimateAlpha = true;

        public void topFocusedWindowChanged(String str) {
        }

        PerDisplay(int i, int i2) {
            this.mRotation = 0;
            this.mDisplayId = i;
            this.mRotation = i2;
        }

        public void insetsChanged(final InsetsState insetsState) {
            DisplayImeController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayImeController$PerDisplay$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$insetsChanged$0(insetsState);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$insetsChanged$0(InsetsState insetsState) {
            if (this.mInsetsState.equals(insetsState)) {
                return;
            }
            InsetsSource source = insetsState.getSource(17);
            Rect frame = source.getFrame();
            Rect frame2 = this.mInsetsState.getSource(17).getFrame();
            this.mInsetsState.set(insetsState, true);
            if (this.mImeShowing && !frame.equals(frame2) && source.isVisible()) {
                startAnimation(this.mImeShowing, true);
            }
        }

        public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] insetsSourceControlArr) {
            insetsChanged(insetsState);
            if (insetsSourceControlArr != null) {
                for (final InsetsSourceControl insetsSourceControl : insetsSourceControlArr) {
                    if (insetsSourceControl != null && insetsSourceControl.getType() == 17) {
                        DisplayImeController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayImeController$PerDisplay$$ExternalSyntheticLambda3
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.lambda$insetsControlChanged$1(insetsSourceControl);
                            }
                        });
                    }
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$insetsControlChanged$1(InsetsSourceControl insetsSourceControl) {
            InsetsSourceControl insetsSourceControl2 = this.mImeSourceControl;
            boolean z = !insetsSourceControl.getSurfacePosition().equals(insetsSourceControl2 != null ? insetsSourceControl2.getSurfacePosition() : null);
            boolean z2 = !DisplayImeController.haveSameLeash(this.mImeSourceControl, insetsSourceControl);
            this.mImeSourceControl = insetsSourceControl;
            if (this.mAnimation != null) {
                if (z) {
                    startAnimation(this.mImeShowing, true);
                }
            } else {
                if (z2) {
                    applyVisibilityToLeash();
                }
                if (this.mImeShowing) {
                    return;
                }
                DisplayImeController.this.removeImeSurface();
            }
        }

        private void applyVisibilityToLeash() {
            SurfaceControl leash = this.mImeSourceControl.getLeash();
            if (leash != null) {
                SurfaceControl.Transaction transactionAcquire = DisplayImeController.this.mTransactionPool.acquire();
                if (this.mImeShowing) {
                    transactionAcquire.show(leash);
                } else {
                    transactionAcquire.hide(leash);
                }
                transactionAcquire.apply();
                DisplayImeController.this.mTransactionPool.release(transactionAcquire);
            }
        }

        public void showInsets(int i, boolean z) {
            if ((i & WindowInsets.Type.ime()) == 0) {
                return;
            }
            DisplayImeController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayImeController$PerDisplay$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$showInsets$2();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$showInsets$2() {
            startAnimation(true, false);
        }

        public void hideInsets(int i, boolean z) {
            if ((i & WindowInsets.Type.ime()) == 0) {
                return;
            }
            DisplayImeController.this.mHandler.post(new Runnable() { // from class: com.android.systemui.wm.DisplayImeController$PerDisplay$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$hideInsets$3();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$hideInsets$3() {
            startAnimation(false, false);
        }

        private void setVisibleDirectly(boolean z) {
            this.mInsetsState.getSource(17).setVisible(z);
            try {
                DisplayImeController.this.mSystemWindows.mWmService.modifyDisplayWindowInsets(this.mDisplayId, this.mInsetsState);
            } catch (RemoteException unused) {
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int imeTop(float f) {
            return this.mImeFrame.top + ((int) f);
        }

        private boolean calcIsFloating(InsetsSource insetsSource) {
            Rect frame = insetsSource.getFrame();
            return frame.height() == 0 || frame.height() <= DisplayImeController.this.mSystemWindows.mDisplayController.getDisplayLayout(this.mDisplayId).navBarFrameHeight();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void startAnimation(boolean z, boolean z2) {
            boolean z3;
            InsetsSource source = this.mInsetsState.getSource(17);
            if (source == null || this.mImeSourceControl == null) {
                return;
            }
            Rect frame = source.getFrame();
            final boolean z4 = calcIsFloating(source) && z;
            if (z4) {
                this.mImeFrame.set(frame);
                this.mImeFrame.bottom -= (int) (DisplayImeController.this.mSystemWindows.mDisplayController.getDisplayLayout(this.mDisplayId).density() * (-80.0f));
            } else if (frame.height() != 0) {
                this.mImeFrame.set(frame);
            }
            if (!z2 && this.mAnimationDirection == 1 && z) {
                return;
            }
            if (this.mAnimationDirection != 2 || z) {
                float fFloatValue = 0.0f;
                ValueAnimator valueAnimator = this.mAnimation;
                if (valueAnimator != null) {
                    if (valueAnimator.isRunning()) {
                        fFloatValue = ((Float) this.mAnimation.getAnimatedValue()).floatValue();
                        z3 = true;
                    } else {
                        z3 = false;
                    }
                    this.mAnimation.cancel();
                } else {
                    z3 = false;
                }
                final float f = this.mImeSourceControl.getSurfacePosition().y;
                final float f2 = this.mImeSourceControl.getSurfacePosition().x;
                final float fHeight = f + this.mImeFrame.height();
                float f3 = z ? fHeight : f;
                float f4 = z ? f : fHeight;
                if (this.mAnimationDirection == 0 && this.mImeShowing && z) {
                    fFloatValue = f;
                    z3 = true;
                }
                this.mAnimationDirection = z ? 1 : 2;
                this.mImeShowing = z;
                ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(f3, f4);
                this.mAnimation = valueAnimatorOfFloat;
                valueAnimatorOfFloat.setDuration(z ? 275L : 340L);
                if (z3) {
                    this.mAnimation.setCurrentFraction((fFloatValue - f3) / (f4 - f3));
                }
                final boolean z5 = z4;
                this.mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.wm.DisplayImeController$PerDisplay$$ExternalSyntheticLambda0
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                        this.f$0.lambda$startAnimation$4(f2, z5, fHeight, f, valueAnimator2);
                    }
                });
                this.mAnimation.setInterpolator(DisplayImeController.INTERPOLATOR);
                final float f5 = f3;
                final float f6 = f4;
                this.mAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.wm.DisplayImeController.PerDisplay.1
                    private boolean mCancelled = false;

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationStart(Animator animator) {
                        float f7;
                        SurfaceControl.Transaction transactionAcquire = DisplayImeController.this.mTransactionPool.acquire();
                        transactionAcquire.setPosition(PerDisplay.this.mImeSourceControl.getLeash(), f2, f5);
                        PerDisplay perDisplay = PerDisplay.this;
                        int iDispatchStartPositioning = DisplayImeController.this.dispatchStartPositioning(perDisplay.mDisplayId, perDisplay.imeTop(fHeight), PerDisplay.this.imeTop(f), PerDisplay.this.mAnimationDirection == 1, z4, transactionAcquire);
                        PerDisplay perDisplay2 = PerDisplay.this;
                        boolean z6 = (iDispatchStartPositioning & 1) == 0;
                        perDisplay2.mAnimateAlpha = z6;
                        if (z6 || z4) {
                            float f8 = f5;
                            float f9 = fHeight;
                            f7 = (f8 - f9) / (f - f9);
                        } else {
                            f7 = 1.0f;
                        }
                        transactionAcquire.setAlpha(perDisplay2.mImeSourceControl.getLeash(), f7);
                        PerDisplay perDisplay3 = PerDisplay.this;
                        if (perDisplay3.mAnimationDirection == 1) {
                            transactionAcquire.show(perDisplay3.mImeSourceControl.getLeash());
                        }
                        transactionAcquire.apply();
                        DisplayImeController.this.mTransactionPool.release(transactionAcquire);
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animator) {
                        this.mCancelled = true;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        SurfaceControl.Transaction transactionAcquire = DisplayImeController.this.mTransactionPool.acquire();
                        if (!this.mCancelled) {
                            transactionAcquire.setPosition(PerDisplay.this.mImeSourceControl.getLeash(), f2, f6);
                            transactionAcquire.setAlpha(PerDisplay.this.mImeSourceControl.getLeash(), 1.0f);
                        }
                        PerDisplay perDisplay = PerDisplay.this;
                        DisplayImeController.this.dispatchEndPositioning(perDisplay.mDisplayId, this.mCancelled, transactionAcquire);
                        PerDisplay perDisplay2 = PerDisplay.this;
                        if (perDisplay2.mAnimationDirection == 2 && !this.mCancelled) {
                            transactionAcquire.hide(perDisplay2.mImeSourceControl.getLeash());
                            DisplayImeController.this.removeImeSurface();
                        }
                        transactionAcquire.apply();
                        DisplayImeController.this.mTransactionPool.release(transactionAcquire);
                        PerDisplay perDisplay3 = PerDisplay.this;
                        perDisplay3.mAnimationDirection = 0;
                        perDisplay3.mAnimation = null;
                    }
                });
                if (!z) {
                    setVisibleDirectly(false);
                }
                this.mAnimation.start();
                if (z) {
                    setVisibleDirectly(true);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$startAnimation$4(float f, boolean z, float f2, float f3, ValueAnimator valueAnimator) {
            SurfaceControl.Transaction transactionAcquire = DisplayImeController.this.mTransactionPool.acquire();
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            transactionAcquire.setPosition(this.mImeSourceControl.getLeash(), f, fFloatValue);
            transactionAcquire.setAlpha(this.mImeSourceControl.getLeash(), (this.mAnimateAlpha || z) ? (fFloatValue - f2) / (f3 - f2) : 1.0f);
            DisplayImeController.this.dispatchPositionChanged(this.mDisplayId, imeTop(fFloatValue), transactionAcquire);
            transactionAcquire.apply();
            DisplayImeController.this.mTransactionPool.release(transactionAcquire);
        }
    }

    void removeImeSurface() {
        IInputMethodManager imms = getImms();
        if (imms != null) {
            try {
                imms.removeImeSurface();
            } catch (RemoteException e) {
                Slog.e("DisplayImeController", "Failed to remove IME surface.", e);
            }
        }
    }

    public IInputMethodManager getImms() {
        return IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean haveSameLeash(InsetsSourceControl insetsSourceControl, InsetsSourceControl insetsSourceControl2) {
        if (insetsSourceControl == insetsSourceControl2) {
            return true;
        }
        if (insetsSourceControl != null && insetsSourceControl2 != null) {
            if (insetsSourceControl.getLeash() == insetsSourceControl2.getLeash()) {
                return true;
            }
            if (insetsSourceControl.getLeash() != null && insetsSourceControl2.getLeash() != null) {
                return insetsSourceControl.getLeash().isSameSurface(insetsSourceControl2.getLeash());
            }
        }
        return false;
    }
}
