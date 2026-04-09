package com.android.systemui.bubbles;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.PathParser;
import android.widget.ImageView;
import com.android.launcher3.icons.DotRenderer;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import java.util.EnumSet;

/* loaded from: classes.dex */
public class BadgedImageView extends ImageView {
    private float mAnimatingToDotScale;
    private BubbleViewProvider mBubble;
    private int mBubbleBitmapSize;
    private int mDotColor;
    private boolean mDotIsAnimating;
    private DotRenderer mDotRenderer;
    private float mDotScale;
    private final EnumSet<SuppressionFlag> mDotSuppressionFlags;
    private DotRenderer.DrawParams mDrawParams;
    private boolean mOnLeft;
    private Rect mTempBounds;

    enum SuppressionFlag {
        FLYOUT_VISIBLE,
        BEHIND_STACK
    }

    public BadgedImageView(Context context) {
        this(context, null);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDotSuppressionFlags = EnumSet.of(SuppressionFlag.FLYOUT_VISIBLE);
        this.mDotScale = 0.0f;
        this.mAnimatingToDotScale = 0.0f;
        this.mDotIsAnimating = false;
        this.mTempBounds = new Rect();
        this.mBubbleBitmapSize = getResources().getDimensionPixelSize(R.dimen.bubble_bitmap_size);
        this.mDrawParams = new DotRenderer.DrawParams();
        this.mDotRenderer = new DotRenderer(this.mBubbleBitmapSize, PathParser.createPathFromPathData(getResources().getString(android.R.string.config_defaultDndDeniedPackages)), 100);
        setFocusable(true);
        setClickable(true);
    }

    public void setRenderedBubble(BubbleViewProvider bubbleViewProvider) {
        this.mBubble = bubbleViewProvider;
        setImageBitmap(bubbleViewProvider.getBadgedImage());
        this.mDotColor = bubbleViewProvider.getDotColor();
        drawDot(bubbleViewProvider.getDotPath());
    }

    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (shouldDrawDot()) {
            getDrawingRect(this.mTempBounds);
            DotRenderer.DrawParams drawParams = this.mDrawParams;
            drawParams.color = this.mDotColor;
            drawParams.iconBounds = this.mTempBounds;
            drawParams.leftAlign = this.mOnLeft;
            drawParams.scale = this.mDotScale;
            this.mDotRenderer.draw(canvas, drawParams);
        }
    }

    void addDotSuppressionFlag(SuppressionFlag suppressionFlag) {
        if (this.mDotSuppressionFlags.add(suppressionFlag)) {
            updateDotVisibility(suppressionFlag == SuppressionFlag.BEHIND_STACK);
        }
    }

    void removeDotSuppressionFlag(SuppressionFlag suppressionFlag) {
        if (this.mDotSuppressionFlags.remove(suppressionFlag)) {
            updateDotVisibility(suppressionFlag == SuppressionFlag.BEHIND_STACK);
        }
    }

    void updateDotVisibility(boolean z) {
        float f = shouldDrawDot() ? 1.0f : 0.0f;
        if (z) {
            animateDotScale(f, null);
            return;
        }
        this.mDotScale = f;
        this.mAnimatingToDotScale = f;
        invalidate();
    }

    void setDotOnLeft(boolean z) {
        this.mOnLeft = z;
        invalidate();
    }

    void drawDot(Path path) {
        this.mDotRenderer = new DotRenderer(this.mBubbleBitmapSize, path, 100);
        invalidate();
    }

    void setDotScale(float f) {
        this.mDotScale = f;
        invalidate();
    }

    boolean getDotOnLeft() {
        return this.mOnLeft;
    }

    float[] getDotCenter() {
        float[] rightDotPosition;
        if (this.mOnLeft) {
            rightDotPosition = this.mDotRenderer.getLeftDotPosition();
        } else {
            rightDotPosition = this.mDotRenderer.getRightDotPosition();
        }
        getDrawingRect(this.mTempBounds);
        return new float[]{this.mTempBounds.width() * rightDotPosition[0], this.mTempBounds.height() * rightDotPosition[1]};
    }

    public String getKey() {
        BubbleViewProvider bubbleViewProvider = this.mBubble;
        if (bubbleViewProvider != null) {
            return bubbleViewProvider.getKey();
        }
        return null;
    }

    int getDotColor() {
        return this.mDotColor;
    }

    void setDotPositionOnLeft(final boolean z, boolean z2) {
        if (z2 && z != getDotOnLeft() && shouldDrawDot()) {
            animateDotScale(0.0f, new Runnable() { // from class: com.android.systemui.bubbles.BadgedImageView$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$setDotPositionOnLeft$0(z);
                }
            });
        } else {
            setDotOnLeft(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setDotPositionOnLeft$0(boolean z) {
        setDotOnLeft(z);
        animateDotScale(1.0f, null);
    }

    boolean getDotPositionOnLeft() {
        return getDotOnLeft();
    }

    private boolean shouldDrawDot() {
        return this.mDotIsAnimating || (this.mBubble.showDot() && this.mDotSuppressionFlags.isEmpty());
    }

    private void animateDotScale(float f, final Runnable runnable) {
        this.mDotIsAnimating = true;
        if (this.mAnimatingToDotScale == f || !shouldDrawDot()) {
            this.mDotIsAnimating = false;
            return;
        }
        this.mAnimatingToDotScale = f;
        final boolean z = f > 0.0f;
        clearAnimation();
        animate().setDuration(200L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.bubbles.BadgedImageView$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$animateDotScale$1(z, valueAnimator);
            }
        }).withEndAction(new Runnable() { // from class: com.android.systemui.bubbles.BadgedImageView$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateDotScale$2(z, runnable);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateDotScale$1(boolean z, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (!z) {
            animatedFraction = 1.0f - animatedFraction;
        }
        setDotScale(animatedFraction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateDotScale$2(boolean z, Runnable runnable) {
        setDotScale(z ? 1.0f : 0.0f);
        this.mDotIsAnimating = false;
        if (runnable != null) {
            runnable.run();
        }
    }
}
