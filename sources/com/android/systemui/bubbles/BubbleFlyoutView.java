package com.android.systemui.bubbles;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.bubbles.Bubble;
import com.android.systemui.recents.TriangleShape;

/* loaded from: classes.dex */
public class BubbleFlyoutView extends FrameLayout {
    private final ArgbEvaluator mArgbEvaluator;
    private boolean mArrowPointingLeft;
    private final Paint mBgPaint;
    private final RectF mBgRect;
    private float mBgTranslationX;
    private float mBgTranslationY;
    private final int mBubbleBitmapSize;
    private final int mBubbleElevation;
    private final float mBubbleIconTopPadding;
    private final int mBubbleSize;
    private final float mCornerRadius;
    private float[] mDotCenter;
    private int mDotColor;
    private final int mFloatingBackgroundColor;
    private final int mFlyoutElevation;
    private final int mFlyoutPadding;
    private final int mFlyoutSpaceFromBubble;
    private final ViewGroup mFlyoutTextContainer;
    private float mFlyoutToDotHeightDelta;
    private float mFlyoutToDotWidthDelta;
    private final ShapeDrawable mLeftTriangleShape;
    private final TextView mMessageText;
    private final float mNewDotRadius;
    private final float mNewDotSize;
    private Runnable mOnHide;
    private final float mOriginalDotSize;
    private float mPercentStillFlyout;
    private float mPercentTransitionedToDot;
    private final int mPointerSize;
    private float mRestingTranslationX;
    private final ShapeDrawable mRightTriangleShape;
    private final ImageView mSenderAvatar;
    private final TextView mSenderText;
    private float mTranslationXWhenDot;
    private float mTranslationYWhenDot;
    private final Outline mTriangleOutline;

    public BubbleFlyoutView(Context context) throws Resources.NotFoundException {
        super(context);
        Paint paint = new Paint(3);
        this.mBgPaint = paint;
        this.mArgbEvaluator = new ArgbEvaluator();
        this.mArrowPointingLeft = true;
        this.mTriangleOutline = new Outline();
        this.mBgRect = new RectF();
        this.mPercentTransitionedToDot = 1.0f;
        this.mPercentStillFlyout = 0.0f;
        this.mFlyoutToDotWidthDelta = 0.0f;
        this.mFlyoutToDotHeightDelta = 0.0f;
        this.mTranslationXWhenDot = 0.0f;
        this.mTranslationYWhenDot = 0.0f;
        this.mRestingTranslationX = 0.0f;
        LayoutInflater.from(context).inflate(R.layout.bubble_flyout, (ViewGroup) this, true);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.bubble_flyout_text_container);
        this.mFlyoutTextContainer = viewGroup;
        this.mSenderText = (TextView) findViewById(R.id.bubble_flyout_name);
        this.mSenderAvatar = (ImageView) findViewById(R.id.bubble_flyout_avatar);
        this.mMessageText = (TextView) viewGroup.findViewById(R.id.bubble_flyout_text);
        Resources resources = getResources();
        this.mFlyoutPadding = resources.getDimensionPixelSize(R.dimen.bubble_flyout_padding_x);
        this.mFlyoutSpaceFromBubble = resources.getDimensionPixelSize(R.dimen.bubble_flyout_space_from_bubble);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.bubble_flyout_pointer_size);
        this.mPointerSize = dimensionPixelSize;
        this.mBubbleSize = resources.getDimensionPixelSize(R.dimen.individual_bubble_size);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.bubble_bitmap_size);
        this.mBubbleBitmapSize = dimensionPixelSize2;
        this.mBubbleIconTopPadding = (r5 - dimensionPixelSize2) / 2.0f;
        this.mBubbleElevation = resources.getDimensionPixelSize(R.dimen.bubble_elevation);
        int dimensionPixelSize3 = resources.getDimensionPixelSize(R.dimen.bubble_flyout_elevation);
        this.mFlyoutElevation = dimensionPixelSize3;
        float f = dimensionPixelSize2 * 0.228f;
        this.mOriginalDotSize = f;
        float f2 = (f * 1.0f) / 2.0f;
        this.mNewDotRadius = f2;
        this.mNewDotSize = f2 * 2.0f;
        TypedArray typedArrayObtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.colorBackgroundFloating, android.R.attr.dialogCornerRadius});
        int color = typedArrayObtainStyledAttributes.getColor(0, -1);
        this.mFloatingBackgroundColor = color;
        this.mCornerRadius = typedArrayObtainStyledAttributes.getDimensionPixelSize(1, 0);
        typedArrayObtainStyledAttributes.recycle();
        setPadding(dimensionPixelSize, 0, dimensionPixelSize, 0);
        setWillNotDraw(false);
        setClipChildren(false);
        setTranslationZ(dimensionPixelSize3);
        setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.bubbles.BubbleFlyoutView.1
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                BubbleFlyoutView.this.getOutline(outline);
            }
        });
        setLayoutDirection(3);
        paint.setColor(color);
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.createHorizontal(dimensionPixelSize, dimensionPixelSize, true));
        this.mLeftTriangleShape = shapeDrawable;
        shapeDrawable.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
        shapeDrawable.getPaint().setColor(color);
        ShapeDrawable shapeDrawable2 = new ShapeDrawable(TriangleShape.createHorizontal(dimensionPixelSize, dimensionPixelSize, false));
        this.mRightTriangleShape = shapeDrawable2;
        shapeDrawable2.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
        shapeDrawable2.getPaint().setColor(color);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        renderBackground(canvas);
        invalidateOutline();
        super.onDraw(canvas);
    }

    void setupFlyoutStartingAsDot(Bubble.FlyoutMessage flyoutMessage, final PointF pointF, float f, boolean z, int i, final Runnable runnable, Runnable runnable2, float[] fArr, final boolean z2) {
        Drawable drawable = flyoutMessage.senderAvatar;
        if (drawable != null && flyoutMessage.isGroupChat) {
            this.mSenderAvatar.setVisibility(0);
            this.mSenderAvatar.setImageDrawable(drawable);
        } else {
            this.mSenderAvatar.setVisibility(8);
            this.mSenderAvatar.setTranslationX(0.0f);
            this.mMessageText.setTranslationX(0.0f);
            this.mSenderText.setTranslationX(0.0f);
        }
        int i2 = ((int) (f * 0.6f)) - (this.mFlyoutPadding * 2);
        if (!TextUtils.isEmpty(flyoutMessage.senderName)) {
            this.mSenderText.setMaxWidth(i2);
            this.mSenderText.setText(flyoutMessage.senderName);
            this.mSenderText.setVisibility(0);
        } else {
            this.mSenderText.setVisibility(8);
        }
        this.mArrowPointingLeft = z;
        this.mDotColor = i;
        this.mOnHide = runnable2;
        this.mDotCenter = fArr;
        setCollapsePercent(1.0f);
        this.mMessageText.setMaxWidth(i2);
        this.mMessageText.setText(flyoutMessage.message);
        post(new Runnable() { // from class: com.android.systemui.bubbles.BubbleFlyoutView$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setupFlyoutStartingAsDot$0(pointF, z2, runnable);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setupFlyoutStartingAsDot$0(PointF pointF, boolean z, Runnable runnable) {
        float f;
        float height;
        float width;
        if (this.mMessageText.getLineCount() > 1) {
            f = pointF.y;
            height = this.mBubbleIconTopPadding;
        } else {
            f = pointF.y;
            height = (this.mBubbleSize - this.mFlyoutTextContainer.getHeight()) / 2.0f;
        }
        float f2 = f + height;
        setTranslationY(f2);
        if (this.mArrowPointingLeft) {
            width = pointF.x + this.mBubbleSize + this.mFlyoutSpaceFromBubble;
        } else {
            width = (pointF.x - getWidth()) - this.mFlyoutSpaceFromBubble;
        }
        this.mRestingTranslationX = width;
        float f3 = z ? 0.0f : this.mNewDotSize;
        this.mFlyoutToDotWidthDelta = getWidth() - f3;
        this.mFlyoutToDotHeightDelta = getHeight() - f3;
        float f4 = z ? 0.0f : this.mOriginalDotSize / 2.0f;
        float f5 = pointF.x;
        float[] fArr = this.mDotCenter;
        float f6 = (f5 + fArr[0]) - f4;
        float f7 = f2 - ((pointF.y + fArr[1]) - f4);
        this.mTranslationXWhenDot = -(this.mRestingTranslationX - f6);
        this.mTranslationYWhenDot = -f7;
        if (runnable != null) {
            runnable.run();
        }
    }

    void hideFlyout() {
        Runnable runnable = this.mOnHide;
        if (runnable != null) {
            runnable.run();
            this.mOnHide = null;
        }
        setVisibility(8);
    }

    void setCollapsePercent(float f) {
        if (Float.isNaN(f)) {
            return;
        }
        float fMax = Math.max(0.0f, Math.min(f, 1.0f));
        this.mPercentTransitionedToDot = fMax;
        this.mPercentStillFlyout = 1.0f - fMax;
        float width = fMax * (this.mArrowPointingLeft ? -getWidth() : getWidth());
        float fClampPercentage = clampPercentage((this.mPercentStillFlyout - 0.75f) / 0.25f);
        this.mMessageText.setTranslationX(width);
        this.mMessageText.setAlpha(fClampPercentage);
        this.mSenderText.setTranslationX(width);
        this.mSenderText.setAlpha(fClampPercentage);
        this.mSenderAvatar.setTranslationX(width);
        this.mSenderAvatar.setAlpha(fClampPercentage);
        setTranslationZ(this.mFlyoutElevation - ((r3 - this.mBubbleElevation) * this.mPercentTransitionedToDot));
        invalidate();
    }

    float getRestingTranslationX() {
        return this.mRestingTranslationX;
    }

    private float clampPercentage(float f) {
        return Math.min(1.0f, Math.max(0.0f, f));
    }

    private void renderBackground(Canvas canvas) {
        float width = getWidth() - (this.mFlyoutToDotWidthDelta * this.mPercentTransitionedToDot);
        float height = getHeight() - (this.mFlyoutToDotHeightDelta * this.mPercentTransitionedToDot);
        float interpolatedRadius = getInterpolatedRadius();
        float f = this.mTranslationXWhenDot;
        float f2 = this.mPercentTransitionedToDot;
        this.mBgTranslationX = f * f2;
        this.mBgTranslationY = this.mTranslationYWhenDot * f2;
        RectF rectF = this.mBgRect;
        int i = this.mPointerSize;
        float f3 = this.mPercentStillFlyout;
        rectF.set(i * f3, 0.0f, width - (i * f3), height);
        this.mBgPaint.setColor(((Integer) this.mArgbEvaluator.evaluate(this.mPercentTransitionedToDot, Integer.valueOf(this.mFloatingBackgroundColor), Integer.valueOf(this.mDotColor))).intValue());
        canvas.save();
        canvas.translate(this.mBgTranslationX, this.mBgTranslationY);
        renderPointerTriangle(canvas, width, height);
        canvas.drawRoundRect(this.mBgRect, interpolatedRadius, interpolatedRadius, this.mBgPaint);
        canvas.restore();
    }

    private void renderPointerTriangle(Canvas canvas, float f, float f2) {
        canvas.save();
        boolean z = this.mArrowPointingLeft;
        int i = z ? 1 : -1;
        float f3 = this.mPercentTransitionedToDot;
        int i2 = this.mPointerSize;
        float f4 = i * f3 * i2 * 2.0f;
        if (!z) {
            f4 += f - i2;
        }
        float f5 = (f2 / 2.0f) - (i2 / 2.0f);
        ShapeDrawable shapeDrawable = z ? this.mLeftTriangleShape : this.mRightTriangleShape;
        canvas.translate(f4, f5);
        shapeDrawable.setAlpha((int) (this.mPercentStillFlyout * 255.0f));
        shapeDrawable.draw(canvas);
        shapeDrawable.getOutline(this.mTriangleOutline);
        this.mTriangleOutline.offset((int) f4, (int) f5);
        canvas.restore();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getOutline(Outline outline) {
        if (this.mTriangleOutline.isEmpty()) {
            return;
        }
        Path path = new Path();
        float interpolatedRadius = getInterpolatedRadius();
        path.addRoundRect(this.mBgRect, interpolatedRadius, interpolatedRadius, Path.Direction.CW);
        outline.setPath(path);
        if (this.mPercentStillFlyout > 0.5f) {
            outline.mPath.addPath(this.mTriangleOutline.mPath);
        }
        Matrix matrix = new Matrix();
        matrix.postTranslate(getLeft() + this.mBgTranslationX, getTop() + this.mBgTranslationY);
        float f = this.mPercentTransitionedToDot;
        if (f > 0.98f) {
            float f2 = (f - 0.98f) / 0.02f;
            float f3 = 1.0f - f2;
            float f4 = this.mNewDotRadius;
            matrix.postTranslate(f4 * f2, f4 * f2);
            matrix.preScale(f3, f3);
        }
        outline.mPath.transform(matrix);
    }

    private float getInterpolatedRadius() {
        float f = this.mNewDotRadius;
        float f2 = this.mPercentTransitionedToDot;
        return (f * f2) + (this.mCornerRadius * (1.0f - f2));
    }
}
