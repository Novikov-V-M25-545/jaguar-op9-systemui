package com.android.systemui.pulse;

import android.animation.ValueAnimator;
import android.graphics.Color;

/* loaded from: classes.dex */
public class ColorAnimator implements ValueAnimator.AnimatorUpdateListener {
    protected final float[] from;
    protected final float[] hsv;
    protected long mAnimTime;
    protected ValueAnimator mColorAnim;
    protected int mFromColor;
    protected boolean mIsRunning;
    protected int mLastColor;
    protected ColorAnimationListener mListener;
    protected int mToColor;
    protected final float[] to;

    public interface ColorAnimationListener {
        default void onColorChanged(ColorAnimator colorAnimator, int i) {
        }

        default void onStartAnimation(ColorAnimator colorAnimator, int i) {
        }

        default void onStopAnimation(ColorAnimator colorAnimator, int i) {
        }
    }

    public ColorAnimator() {
        this(ValueAnimator.ofFloat(0.0f, 1.0f));
    }

    public ColorAnimator(ValueAnimator valueAnimator) {
        this(valueAnimator, 10000L);
    }

    public ColorAnimator(ValueAnimator valueAnimator, long j) {
        this(valueAnimator, j, Color.parseColor("#ffff8080"), Color.parseColor("#ff8080ff"));
    }

    public ColorAnimator(ValueAnimator valueAnimator, long j, int i, int i2) {
        this.from = new float[3];
        this.to = new float[3];
        this.hsv = new float[3];
        this.mLastColor = Color.parseColor("#ffff8080");
        this.mAnimTime = j;
        this.mFromColor = i;
        this.mToColor = i2;
        this.mColorAnim = valueAnimator;
        valueAnimator.addUpdateListener(this);
    }

    public void start() {
        stop();
        Color.colorToHSV(this.mFromColor, this.from);
        Color.colorToHSV(this.mToColor, this.to);
        this.mColorAnim.setDuration(this.mAnimTime);
        this.mColorAnim.setRepeatMode(2);
        this.mColorAnim.setRepeatCount(-1);
        ColorAnimationListener colorAnimationListener = this.mListener;
        if (colorAnimationListener != null) {
            colorAnimationListener.onStartAnimation(this, this.mFromColor);
        }
        this.mColorAnim.start();
        this.mIsRunning = true;
    }

    public void stop() {
        if (this.mColorAnim.isStarted()) {
            this.mColorAnim.end();
            this.mIsRunning = false;
            ColorAnimationListener colorAnimationListener = this.mListener;
            if (colorAnimationListener != null) {
                colorAnimationListener.onStopAnimation(this, this.mLastColor);
            }
        }
    }

    public void setAnimationTime(long j) {
        if (this.mAnimTime != j) {
            this.mAnimTime = j;
            if (this.mColorAnim.isRunning()) {
                start();
            }
        }
    }

    public void setColorAnimatorListener(ColorAnimationListener colorAnimationListener) {
        this.mListener = colorAnimationListener;
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float[] fArr = this.hsv;
        float[] fArr2 = this.from;
        fArr[0] = fArr2[0] + ((this.to[0] - fArr2[0]) * valueAnimator.getAnimatedFraction());
        float[] fArr3 = this.hsv;
        float[] fArr4 = this.from;
        fArr3[1] = fArr4[1] + ((this.to[1] - fArr4[1]) * valueAnimator.getAnimatedFraction());
        float[] fArr5 = this.hsv;
        float[] fArr6 = this.from;
        fArr5[2] = fArr6[2] + ((this.to[2] - fArr6[2]) * valueAnimator.getAnimatedFraction());
        int iHSVToColor = Color.HSVToColor(this.hsv);
        this.mLastColor = iHSVToColor;
        ColorAnimationListener colorAnimationListener = this.mListener;
        if (colorAnimationListener != null) {
            colorAnimationListener.onColorChanged(this, iHSVToColor);
        }
    }
}
