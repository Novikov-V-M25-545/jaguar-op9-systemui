package com.android.systemui.pip.phone;

import android.graphics.PointF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class PipTouchState {

    @VisibleForTesting
    static final long DOUBLE_TAP_TIMEOUT = 200;
    private int mActivePointerId;
    private final Runnable mDoubleTapTimeoutCallback;
    private final Handler mHandler;
    private final Runnable mHoverExitTimeoutCallback;
    private VelocityTracker mVelocityTracker;
    private final ViewConfiguration mViewConfig;
    private long mDownTouchTime = 0;
    private long mLastDownTouchTime = 0;
    private long mUpTouchTime = 0;
    private final PointF mDownTouch = new PointF();
    private final PointF mDownDelta = new PointF();
    private final PointF mLastTouch = new PointF();
    private final PointF mLastDelta = new PointF();
    private final PointF mVelocity = new PointF();
    private boolean mAllowTouches = true;
    private boolean mIsUserInteracting = false;
    private boolean mIsDoubleTap = false;
    private boolean mIsWaitingForDoubleTap = false;
    private boolean mIsDragging = false;
    private boolean mPreviouslyDragging = false;
    private boolean mStartedDragging = false;
    private boolean mAllowDraggingOffscreen = false;

    public PipTouchState(ViewConfiguration viewConfiguration, Handler handler, Runnable runnable, Runnable runnable2) {
        this.mViewConfig = viewConfiguration;
        this.mHandler = handler;
        this.mDoubleTapTimeoutCallback = runnable;
        this.mHoverExitTimeoutCallback = runnable2;
    }

    public void reset() {
        this.mAllowDraggingOffscreen = false;
        this.mIsDragging = false;
        this.mStartedDragging = false;
        this.mIsUserInteracting = false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        boolean z = false;
        z = false;
        z = false;
        if (actionMasked == 0) {
            if (this.mAllowTouches) {
                initOrResetVelocityTracker();
                addMovementToVelocityTracker(motionEvent);
                this.mActivePointerId = motionEvent.getPointerId(0);
                this.mLastTouch.set(motionEvent.getRawX(), motionEvent.getRawY());
                this.mDownTouch.set(this.mLastTouch);
                this.mAllowDraggingOffscreen = true;
                this.mIsUserInteracting = true;
                long eventTime = motionEvent.getEventTime();
                this.mDownTouchTime = eventTime;
                this.mIsDoubleTap = !this.mPreviouslyDragging && eventTime - this.mLastDownTouchTime < DOUBLE_TAP_TIMEOUT;
                this.mIsWaitingForDoubleTap = false;
                this.mIsDragging = false;
                this.mLastDownTouchTime = eventTime;
                Runnable runnable = this.mDoubleTapTimeoutCallback;
                if (runnable != null) {
                    this.mHandler.removeCallbacks(runnable);
                    return;
                }
                return;
            }
            return;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (this.mIsUserInteracting) {
                    addMovementToVelocityTracker(motionEvent);
                    int iFindPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (iFindPointerIndex == -1) {
                        Log.e("PipTouchState", "Invalid active pointer id on MOVE: " + this.mActivePointerId);
                        return;
                    }
                    float rawX = motionEvent.getRawX(iFindPointerIndex);
                    float rawY = motionEvent.getRawY(iFindPointerIndex);
                    PointF pointF = this.mLastDelta;
                    PointF pointF2 = this.mLastTouch;
                    pointF.set(rawX - pointF2.x, rawY - pointF2.y);
                    PointF pointF3 = this.mDownDelta;
                    PointF pointF4 = this.mDownTouch;
                    pointF3.set(rawX - pointF4.x, rawY - pointF4.y);
                    byte b = this.mDownDelta.length() > ((float) this.mViewConfig.getScaledTouchSlop());
                    if (this.mIsDragging) {
                        this.mStartedDragging = false;
                    } else if (b != false) {
                        this.mIsDragging = true;
                        this.mStartedDragging = true;
                    }
                    this.mLastTouch.set(rawX, rawY);
                    return;
                }
                return;
            }
            if (actionMasked != 3) {
                if (actionMasked != 6) {
                    if (actionMasked != 11) {
                        return;
                    }
                    removeHoverExitTimeoutCallback();
                    return;
                } else {
                    if (this.mIsUserInteracting) {
                        addMovementToVelocityTracker(motionEvent);
                        int actionIndex = motionEvent.getActionIndex();
                        if (motionEvent.getPointerId(actionIndex) == this.mActivePointerId) {
                            int i = actionIndex == 0 ? 1 : 0;
                            this.mActivePointerId = motionEvent.getPointerId(i);
                            this.mLastTouch.set(motionEvent.getRawX(i), motionEvent.getRawY(i));
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        } else {
            if (!this.mIsUserInteracting) {
                return;
            }
            addMovementToVelocityTracker(motionEvent);
            this.mVelocityTracker.computeCurrentVelocity(1000, this.mViewConfig.getScaledMaximumFlingVelocity());
            this.mVelocity.set(this.mVelocityTracker.getXVelocity(), this.mVelocityTracker.getYVelocity());
            int iFindPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
            if (iFindPointerIndex2 == -1) {
                Log.e("PipTouchState", "Invalid active pointer id on UP: " + this.mActivePointerId);
                return;
            }
            this.mUpTouchTime = motionEvent.getEventTime();
            this.mLastTouch.set(motionEvent.getRawX(iFindPointerIndex2), motionEvent.getRawY(iFindPointerIndex2));
            boolean z2 = this.mIsDragging;
            this.mPreviouslyDragging = z2;
            if (!this.mIsDoubleTap && !z2 && this.mUpTouchTime - this.mDownTouchTime < DOUBLE_TAP_TIMEOUT) {
                z = true;
            }
            this.mIsWaitingForDoubleTap = z;
        }
        recycleVelocityTracker();
    }

    public PointF getVelocity() {
        return this.mVelocity;
    }

    public PointF getLastTouchPosition() {
        return this.mLastTouch;
    }

    public PointF getLastTouchDelta() {
        return this.mLastDelta;
    }

    public PointF getDownTouchPosition() {
        return this.mDownTouch;
    }

    public boolean isDragging() {
        return this.mIsDragging;
    }

    public boolean isUserInteracting() {
        return this.mIsUserInteracting;
    }

    public boolean startedDragging() {
        return this.mStartedDragging;
    }

    public void setAllowTouches(boolean z) {
        this.mAllowTouches = z;
        if (this.mIsUserInteracting) {
            reset();
        }
    }

    public boolean isDoubleTap() {
        return this.mIsDoubleTap;
    }

    public boolean isWaitingForDoubleTap() {
        return this.mIsWaitingForDoubleTap;
    }

    public void scheduleDoubleTapTimeoutCallback() {
        if (this.mIsWaitingForDoubleTap) {
            long doubleTapTimeoutCallbackDelay = getDoubleTapTimeoutCallbackDelay();
            this.mHandler.removeCallbacks(this.mDoubleTapTimeoutCallback);
            this.mHandler.postDelayed(this.mDoubleTapTimeoutCallback, doubleTapTimeoutCallbackDelay);
        }
    }

    @VisibleForTesting
    long getDoubleTapTimeoutCallbackDelay() {
        if (this.mIsWaitingForDoubleTap) {
            return Math.max(0L, DOUBLE_TAP_TIMEOUT - (this.mUpTouchTime - this.mDownTouchTime));
        }
        return -1L;
    }

    public void removeDoubleTapTimeoutCallback() {
        this.mIsWaitingForDoubleTap = false;
        this.mHandler.removeCallbacks(this.mDoubleTapTimeoutCallback);
    }

    void scheduleHoverExitTimeoutCallback() {
        this.mHandler.removeCallbacks(this.mHoverExitTimeoutCallback);
        this.mHandler.postDelayed(this.mHoverExitTimeoutCallback, 50L);
    }

    void removeHoverExitTimeoutCallback() {
        this.mHandler.removeCallbacks(this.mHoverExitTimeoutCallback);
    }

    void addMovementToVelocityTracker(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            return;
        }
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.mVelocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipTouchState");
        printWriter.println(str2 + "mAllowTouches=" + this.mAllowTouches);
        printWriter.println(str2 + "mActivePointerId=" + this.mActivePointerId);
        printWriter.println(str2 + "mDownTouch=" + this.mDownTouch);
        printWriter.println(str2 + "mDownDelta=" + this.mDownDelta);
        printWriter.println(str2 + "mLastTouch=" + this.mLastTouch);
        printWriter.println(str2 + "mLastDelta=" + this.mLastDelta);
        printWriter.println(str2 + "mVelocity=" + this.mVelocity);
        printWriter.println(str2 + "mIsUserInteracting=" + this.mIsUserInteracting);
        printWriter.println(str2 + "mIsDragging=" + this.mIsDragging);
        printWriter.println(str2 + "mStartedDragging=" + this.mStartedDragging);
        printWriter.println(str2 + "mAllowDraggingOffscreen=" + this.mAllowDraggingOffscreen);
    }
}
