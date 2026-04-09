package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class AssistOrbView extends FrameLayout {
    private final Paint mBackgroundPaint;
    private final int mBaseMargin;
    private float mCircleAnimationEndValue;
    private ValueAnimator mCircleAnimator;
    private final int mCircleMinSize;
    private final Rect mCircleRect;
    private float mCircleSize;
    private ValueAnimator.AnimatorUpdateListener mCircleUpdateListener;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mClipToOutline;
    private ImageView mLogo;
    private final int mMaxElevation;
    private float mOffset;
    private ValueAnimator mOffsetAnimator;
    private ValueAnimator.AnimatorUpdateListener mOffsetUpdateListener;
    private float mOutlineAlpha;
    private final Interpolator mOvershootInterpolator;
    private final int mStaticOffset;
    private final Rect mStaticRect;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AssistOrbView(Context context) {
        this(context, null);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        Paint paint = new Paint();
        this.mBackgroundPaint = paint;
        this.mCircleRect = new Rect();
        this.mStaticRect = new Rect();
        this.mOvershootInterpolator = new OvershootInterpolator();
        this.mCircleUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.assist.AssistOrbView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AssistOrbView.this.applyCircleSize(((Float) valueAnimator.getAnimatedValue()).floatValue());
                AssistOrbView.this.updateElevation();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.assist.AssistOrbView.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AssistOrbView.this.mCircleAnimator = null;
            }
        };
        this.mOffsetUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.assist.AssistOrbView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AssistOrbView.this.mOffset = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                AssistOrbView.this.updateLayout();
            }
        };
        setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.assist.AssistOrbView.4
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (AssistOrbView.this.mCircleSize > 0.0f) {
                    outline.setOval(AssistOrbView.this.mCircleRect);
                } else {
                    outline.setEmpty();
                }
                outline.setAlpha(AssistOrbView.this.mOutlineAlpha);
            }
        });
        setWillNotDraw(false);
        this.mCircleMinSize = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_size);
        this.mBaseMargin = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_base_margin);
        this.mStaticOffset = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_travel_distance);
        this.mMaxElevation = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_elevation);
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.assist_orb_color));
    }

    public ImageView getLogo() {
        return this.mLogo;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle(this.mCircleRect.centerX(), this.mCircleRect.centerY(), this.mCircleSize / 2.0f, this.mBackgroundPaint);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLogo = (ImageView) findViewById(R.id.search_logo);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        ImageView imageView = this.mLogo;
        imageView.layout(0, 0, imageView.getMeasuredWidth(), this.mLogo.getMeasuredHeight());
        if (z) {
            updateCircleRect(this.mStaticRect, this.mStaticOffset, true);
        }
    }

    public void animateCircleSize(float f, long j, long j2, Interpolator interpolator) {
        if (f == this.mCircleAnimationEndValue) {
            return;
        }
        ValueAnimator valueAnimator = this.mCircleAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mCircleSize, f);
        this.mCircleAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.addUpdateListener(this.mCircleUpdateListener);
        this.mCircleAnimator.addListener(this.mClearAnimatorListener);
        this.mCircleAnimator.setInterpolator(interpolator);
        this.mCircleAnimator.setDuration(j);
        this.mCircleAnimator.setStartDelay(j2);
        this.mCircleAnimator.start();
        this.mCircleAnimationEndValue = f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyCircleSize(float f) {
        this.mCircleSize = f;
        updateLayout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateElevation() {
        int i = this.mStaticOffset;
        setElevation((1.0f - Math.max((i - this.mOffset) / i, 0.0f)) * this.mMaxElevation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateOffset(float f, long j, long j2, Interpolator interpolator) {
        ValueAnimator valueAnimator = this.mOffsetAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            this.mOffsetAnimator.cancel();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mOffset, f);
        this.mOffsetAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.addUpdateListener(this.mOffsetUpdateListener);
        this.mOffsetAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.assist.AssistOrbView.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AssistOrbView.this.mOffsetAnimator = null;
            }
        });
        this.mOffsetAnimator.setInterpolator(interpolator);
        this.mOffsetAnimator.setStartDelay(j2);
        this.mOffsetAnimator.setDuration(j);
        this.mOffsetAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateLayout() {
        updateCircleRect();
        updateLogo();
        invalidateOutline();
        invalidate();
        updateClipping();
    }

    private void updateClipping() {
        boolean z = this.mCircleSize < ((float) this.mCircleMinSize);
        if (z != this.mClipToOutline) {
            setClipToOutline(z);
            this.mClipToOutline = z;
        }
    }

    private void updateLogo() {
        Rect rect = this.mCircleRect;
        Rect rect2 = this.mCircleRect;
        int i = this.mStaticOffset;
        float f = (i - this.mOffset) / i;
        this.mLogo.setImageAlpha((int) (Math.max(((1.0f - f) - 0.5f) * 2.0f, 0.0f) * 255.0f));
        this.mLogo.setTranslationX(((rect.left + rect.right) / 2.0f) - (this.mLogo.getWidth() / 2.0f));
        this.mLogo.setTranslationY(((((rect2.top + rect2.bottom) / 2.0f) - (this.mLogo.getHeight() / 2.0f)) - (this.mCircleMinSize / 7.0f)) + (i * f * 0.1f));
    }

    private void updateCircleRect() {
        updateCircleRect(this.mCircleRect, this.mOffset, false);
    }

    private void updateCircleRect(Rect rect, float f, boolean z) {
        float f2 = z ? this.mCircleMinSize : this.mCircleSize;
        int width = ((int) (getWidth() - f2)) / 2;
        int height = (int) (((getHeight() - (f2 / 2.0f)) - this.mBaseMargin) - f);
        rect.set(width, height, (int) (width + f2), (int) (height + f2));
    }

    public void startExitAnimation(long j) {
        Interpolator interpolator = Interpolators.FAST_OUT_LINEAR_IN;
        animateCircleSize(0.0f, 200L, j, interpolator);
        animateOffset(0.0f, 200L, j, interpolator);
    }

    public void startEnterAnimation() {
        applyCircleSize(0.0f);
        post(new Runnable() { // from class: com.android.systemui.assist.AssistOrbView.6
            @Override // java.lang.Runnable
            public void run() {
                AssistOrbView.this.animateCircleSize(r0.mCircleMinSize, 300L, 0L, AssistOrbView.this.mOvershootInterpolator);
                AssistOrbView.this.animateOffset(r7.mStaticOffset, 400L, 0L, Interpolators.LINEAR_OUT_SLOW_IN);
            }
        });
    }

    public void reset() {
        this.mClipToOutline = false;
        this.mBackgroundPaint.setAlpha(255);
        this.mOutlineAlpha = 1.0f;
    }
}
