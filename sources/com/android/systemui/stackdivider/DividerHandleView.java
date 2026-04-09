package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class DividerHandleView extends View {
    private static final Property<DividerHandleView, Integer> HEIGHT_PROPERTY;
    private static final Property<DividerHandleView, Integer> WIDTH_PROPERTY;
    private AnimatorSet mAnimator;
    private final int mCircleDiameter;
    private int mCurrentHeight;
    private int mCurrentWidth;
    private final int mHeight;
    private final Paint mPaint;
    private boolean mTouching;
    private final int mWidth;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    static {
        Class<Integer> cls = Integer.class;
        WIDTH_PROPERTY = new Property<DividerHandleView, Integer>(cls, "width") { // from class: com.android.systemui.stackdivider.DividerHandleView.1
            @Override // android.util.Property
            public Integer get(DividerHandleView dividerHandleView) {
                return Integer.valueOf(dividerHandleView.mCurrentWidth);
            }

            @Override // android.util.Property
            public void set(DividerHandleView dividerHandleView, Integer num) {
                dividerHandleView.mCurrentWidth = num.intValue();
                dividerHandleView.invalidate();
            }
        };
        HEIGHT_PROPERTY = new Property<DividerHandleView, Integer>(cls, "height") { // from class: com.android.systemui.stackdivider.DividerHandleView.2
            @Override // android.util.Property
            public Integer get(DividerHandleView dividerHandleView) {
                return Integer.valueOf(dividerHandleView.mCurrentHeight);
            }

            @Override // android.util.Property
            public void set(DividerHandleView dividerHandleView, Integer num) {
                dividerHandleView.mCurrentHeight = num.intValue();
                dividerHandleView.invalidate();
            }
        };
    }

    public DividerHandleView(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        super(context, attributeSet);
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setColor(getResources().getColor(R.color.docked_divider_handle, null));
        paint.setAntiAlias(true);
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_width);
        this.mWidth = dimensionPixelSize;
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_height);
        this.mHeight = dimensionPixelSize2;
        this.mCurrentWidth = dimensionPixelSize;
        this.mCurrentHeight = dimensionPixelSize2;
        this.mCircleDiameter = (dimensionPixelSize + dimensionPixelSize2) / 3;
    }

    public void setTouching(boolean z, boolean z2) {
        if (z == this.mTouching) {
            return;
        }
        AnimatorSet animatorSet = this.mAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mAnimator = null;
        }
        if (!z2) {
            if (z) {
                int i = this.mCircleDiameter;
                this.mCurrentWidth = i;
                this.mCurrentHeight = i;
            } else {
                this.mCurrentWidth = this.mWidth;
                this.mCurrentHeight = this.mHeight;
            }
            invalidate();
        } else {
            animateToTarget(z ? this.mCircleDiameter : this.mWidth, z ? this.mCircleDiameter : this.mHeight, z);
        }
        this.mTouching = z;
    }

    private void animateToTarget(int i, int i2, boolean z) {
        Interpolator interpolator;
        ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(this, WIDTH_PROPERTY, this.mCurrentWidth, i);
        ObjectAnimator objectAnimatorOfInt2 = ObjectAnimator.ofInt(this, HEIGHT_PROPERTY, this.mCurrentHeight, i2);
        AnimatorSet animatorSet = new AnimatorSet();
        this.mAnimator = animatorSet;
        animatorSet.playTogether(objectAnimatorOfInt, objectAnimatorOfInt2);
        this.mAnimator.setDuration(z ? 150L : 200L);
        AnimatorSet animatorSet2 = this.mAnimator;
        if (z) {
            interpolator = Interpolators.TOUCH_RESPONSE;
        } else {
            interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        animatorSet2.setInterpolator(interpolator);
        this.mAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.stackdivider.DividerHandleView.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                DividerHandleView.this.mAnimator = null;
            }
        });
        this.mAnimator.start();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int width = (getWidth() / 2) - (this.mCurrentWidth / 2);
        int height = getHeight() / 2;
        int i = this.mCurrentHeight;
        float fMin = Math.min(this.mCurrentWidth, i) / 2;
        canvas.drawRoundRect(width, height - (i / 2), width + this.mCurrentWidth, r1 + this.mCurrentHeight, fMin, fMin, this.mPaint);
    }
}
