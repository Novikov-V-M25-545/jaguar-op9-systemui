package com.android.systemui.util;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RelativeTouchListener.kt */
/* loaded from: classes.dex */
public abstract class RelativeTouchListener implements View.OnTouchListener {
    private final Handler handler;
    private boolean movedEnough;
    private boolean performedLongClick;
    private final PointF touchDown = new PointF();
    private final PointF viewPositionOnTouchDown = new PointF();
    private final VelocityTracker velocityTracker = VelocityTracker.obtain();
    private int touchSlop = -1;

    public abstract boolean onDown(@NotNull View view, @NotNull MotionEvent motionEvent);

    public abstract void onMove(@NotNull View view, @NotNull MotionEvent motionEvent, float f, float f2, float f3, float f4);

    public abstract void onUp(@NotNull View view, @NotNull MotionEvent motionEvent, float f, float f2, float f3, float f4, float f5, float f6);

    public RelativeTouchListener() {
        Looper looperMyLooper = Looper.myLooper();
        if (looperMyLooper == null) {
            Intrinsics.throwNpe();
        }
        this.handler = new Handler(looperMyLooper);
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(@NotNull final View v, @NotNull MotionEvent ev) {
        Intrinsics.checkParameterIsNotNull(v, "v");
        Intrinsics.checkParameterIsNotNull(ev, "ev");
        addMovement(ev);
        float rawX = ev.getRawX() - this.touchDown.x;
        float rawY = ev.getRawY() - this.touchDown.y;
        int action = ev.getAction();
        if (action != 0) {
            if (action == 1) {
                if (this.movedEnough) {
                    this.velocityTracker.computeCurrentVelocity(1000);
                    PointF pointF = this.viewPositionOnTouchDown;
                    float f = pointF.x;
                    float f2 = pointF.y;
                    VelocityTracker velocityTracker = this.velocityTracker;
                    Intrinsics.checkExpressionValueIsNotNull(velocityTracker, "velocityTracker");
                    float xVelocity = velocityTracker.getXVelocity();
                    VelocityTracker velocityTracker2 = this.velocityTracker;
                    Intrinsics.checkExpressionValueIsNotNull(velocityTracker2, "velocityTracker");
                    onUp(v, ev, f, f2, rawX, rawY, xVelocity, velocityTracker2.getYVelocity());
                } else if (!this.performedLongClick) {
                    v.performClick();
                } else {
                    this.handler.removeCallbacksAndMessages(null);
                }
                this.velocityTracker.clear();
                this.movedEnough = false;
            } else if (action == 2) {
                if (!this.movedEnough && ((float) Math.hypot(rawX, rawY)) > this.touchSlop && !this.performedLongClick) {
                    this.movedEnough = true;
                    this.handler.removeCallbacksAndMessages(null);
                }
                if (this.movedEnough) {
                    PointF pointF2 = this.viewPositionOnTouchDown;
                    onMove(v, ev, pointF2.x, pointF2.y, rawX, rawY);
                }
            }
        } else {
            if (!onDown(v, ev)) {
                return false;
            }
            ViewConfiguration viewConfiguration = ViewConfiguration.get(v.getContext());
            Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(v.context)");
            this.touchSlop = viewConfiguration.getScaledTouchSlop();
            this.touchDown.set(ev.getRawX(), ev.getRawY());
            this.viewPositionOnTouchDown.set(v.getTranslationX(), v.getTranslationY());
            this.performedLongClick = false;
            this.handler.postDelayed(new Runnable() { // from class: com.android.systemui.util.RelativeTouchListener.onTouch.1
                @Override // java.lang.Runnable
                public final void run() {
                    if (v.isLongClickable()) {
                        RelativeTouchListener.this.performedLongClick = v.performLongClick();
                    }
                }
            }, ViewConfiguration.getLongPressTimeout());
        }
        return true;
    }

    private final void addMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.velocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }
}
