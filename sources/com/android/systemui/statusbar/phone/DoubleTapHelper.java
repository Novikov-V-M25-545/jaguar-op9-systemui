package com.android.systemui.statusbar.phone;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class DoubleTapHelper {
    private boolean mActivated;
    private final ActivationListener mActivationListener;
    private float mActivationX;
    private float mActivationY;
    private final DoubleTapListener mDoubleTapListener;
    private final DoubleTapLogListener mDoubleTapLogListener;
    private float mDoubleTapSlop;
    private float mDownX;
    private float mDownY;
    private final SlideBackListener mSlideBackListener;
    private Runnable mTapTimeoutRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.DoubleTapHelper$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.makeInactive();
        }
    };
    private float mTouchSlop;
    private boolean mTrackTouch;
    private final View mView;

    @FunctionalInterface
    public interface ActivationListener {
        void onActiveChanged(boolean z);
    }

    @FunctionalInterface
    public interface DoubleTapListener {
        boolean onDoubleTap();
    }

    @FunctionalInterface
    public interface DoubleTapLogListener {
        void onDoubleTapLog(boolean z, float f, float f2);
    }

    @FunctionalInterface
    public interface SlideBackListener {
        boolean onSlideBack();
    }

    public DoubleTapHelper(View view, ActivationListener activationListener, DoubleTapListener doubleTapListener, SlideBackListener slideBackListener, DoubleTapLogListener doubleTapLogListener) {
        this.mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
        this.mDoubleTapSlop = view.getResources().getDimension(R.dimen.double_tap_slop);
        this.mView = view;
        this.mActivationListener = activationListener;
        this.mDoubleTapListener = doubleTapListener;
        this.mSlideBackListener = slideBackListener;
        this.mDoubleTapLogListener = doubleTapLogListener;
    }

    public boolean onTouchEvent(MotionEvent motionEvent, int i) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDownX = motionEvent.getX();
            float y = motionEvent.getY();
            this.mDownY = y;
            this.mTrackTouch = true;
            if (y > i) {
                this.mTrackTouch = false;
            }
        } else if (actionMasked != 1) {
            if (actionMasked != 2) {
                if (actionMasked == 3) {
                    makeInactive();
                    this.mTrackTouch = false;
                }
            } else if (!isWithinTouchSlop(motionEvent)) {
                makeInactive();
                this.mTrackTouch = false;
            }
        } else if (isWithinTouchSlop(motionEvent)) {
            SlideBackListener slideBackListener = this.mSlideBackListener;
            if (slideBackListener != null && slideBackListener.onSlideBack()) {
                return true;
            }
            if (!this.mActivated) {
                makeActive();
                this.mView.postDelayed(this.mTapTimeoutRunnable, 1200L);
                this.mActivationX = motionEvent.getX();
                this.mActivationY = motionEvent.getY();
            } else {
                boolean zIsWithinDoubleTapSlop = isWithinDoubleTapSlop(motionEvent);
                DoubleTapLogListener doubleTapLogListener = this.mDoubleTapLogListener;
                if (doubleTapLogListener != null) {
                    doubleTapLogListener.onDoubleTapLog(zIsWithinDoubleTapSlop, motionEvent.getX() - this.mActivationX, motionEvent.getY() - this.mActivationY);
                }
                if (zIsWithinDoubleTapSlop) {
                    makeInactive();
                    if (!this.mDoubleTapListener.onDoubleTap()) {
                        return false;
                    }
                } else {
                    makeInactive();
                    this.mTrackTouch = false;
                }
            }
        } else {
            makeInactive();
            this.mTrackTouch = false;
        }
        return this.mTrackTouch;
    }

    private void makeActive() {
        if (this.mActivated) {
            return;
        }
        this.mActivated = true;
        this.mActivationListener.onActiveChanged(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void makeInactive() {
        if (this.mActivated) {
            this.mActivated = false;
            this.mActivationListener.onActiveChanged(false);
            this.mView.removeCallbacks(this.mTapTimeoutRunnable);
        }
    }

    private boolean isWithinTouchSlop(MotionEvent motionEvent) {
        return Math.abs(motionEvent.getX() - this.mDownX) < this.mTouchSlop && Math.abs(motionEvent.getY() - this.mDownY) < this.mTouchSlop;
    }

    public boolean isWithinDoubleTapSlop(MotionEvent motionEvent) {
        if (this.mActivated) {
            return Math.abs(motionEvent.getX() - this.mActivationX) < this.mDoubleTapSlop && Math.abs(motionEvent.getY() - this.mActivationY) < this.mDoubleTapSlop;
        }
        return true;
    }
}
