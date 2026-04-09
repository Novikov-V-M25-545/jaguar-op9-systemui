package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;

/* loaded from: classes.dex */
public class ScreenshotSelectorView extends FrameLayout implements View.OnTouchListener {
    private final int mBorderWidth;
    private final int mCornerWidth;
    private Rect mDrawingRect;
    private boolean mIsFirstSelection;
    private boolean mIsMoving;
    private OnSelectionListener mListener;
    private int mMovingOffsetX;
    private int mMovingOffsetY;
    private final Paint mPaintBackground;
    private final Paint mPaintSelection;
    private final Paint mPaintSelectionBorder;
    private final Paint mPaintSelectionCorner;
    private ResizingHandle mResizingHandle;
    private int mResizingOffsetX;
    private int mResizingOffsetY;
    private Rect mSelectionRect;
    private final int mTouchWidth;

    public interface OnSelectionListener {
        void onSelectionChanged(Rect rect, boolean z);
    }

    private enum ResizingHandle {
        INVALID,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        TOP_RIGHT,
        TOP_LEFT,
        RIGHT,
        BOTTOM,
        LEFT,
        TOP;

        public boolean isValid() {
            return this != INVALID;
        }

        public boolean isLeft() {
            return this == LEFT || this == TOP_LEFT || this == BOTTOM_LEFT;
        }

        public boolean isTop() {
            return this == TOP || this == TOP_LEFT || this == TOP_RIGHT;
        }

        public boolean isRight() {
            return this == RIGHT || this == TOP_RIGHT || this == BOTTOM_RIGHT;
        }

        public boolean isBottom() {
            return this == BOTTOM || this == BOTTOM_RIGHT || this == BOTTOM_LEFT;
        }
    }

    public ScreenshotSelectorView(Context context) {
        this(context, null);
    }

    public ScreenshotSelectorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mResizingHandle = ResizingHandle.INVALID;
        int dimension = (int) context.getResources().getDimension(R.dimen.global_screenshot_selector_border_width);
        this.mBorderWidth = dimension;
        int dimension2 = (int) context.getResources().getDimension(R.dimen.global_screenshot_selector_corner_width);
        this.mCornerWidth = dimension2;
        this.mTouchWidth = (int) context.getResources().getDimension(R.dimen.global_screenshot_selector_touch_width);
        Paint paint = new Paint(-16777216);
        this.mPaintBackground = paint;
        paint.setAlpha(160);
        Paint paint2 = new Paint(0);
        this.mPaintSelection = paint2;
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Paint paint3 = new Paint();
        this.mPaintSelectionBorder = paint3;
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(dimension);
        paint3.setColor(-1);
        paint3.setAntiAlias(true);
        Paint paint4 = new Paint();
        this.mPaintSelectionCorner = paint4;
        paint4.setStyle(Paint.Style.STROKE);
        paint4.setStrokeWidth(dimension2);
        paint4.setColor(-1);
        paint4.setAntiAlias(true);
        this.mDrawingRect = new Rect();
        setOnTouchListener(this);
        setWillNotDraw(false);
    }

    public void startSelection(int i, int i2) {
        this.mSelectionRect = new Rect(i, i2, i, i2);
        invalidate();
    }

    public Rect getSelectionRect() {
        return this.mSelectionRect;
    }

    public void sortSelectionRect() {
        this.mSelectionRect.sort();
    }

    public void stopSelection() {
        this.mSelectionRect = null;
        invalidate();
    }

    public void delegateSelection() {
        OnSelectionListener onSelectionListener = this.mListener;
        if (onSelectionListener != null) {
            onSelectionListener.onSelectionChanged(this.mSelectionRect, this.mIsFirstSelection);
        }
    }

    public void setSelectionListener(OnSelectionListener onSelectionListener) {
        this.mListener = onSelectionListener;
    }

    private boolean isTouchingCenteredSquare(int i, int i2, int i3, int i4) {
        int i5 = this.mTouchWidth;
        return i3 >= i - i5 && i3 <= i + i5 && i4 >= i2 - i5 && i4 <= i2 + i5;
    }

    private boolean isTouchingBorder(int i, int i2, int i3, int i4, int i5, boolean z) {
        if (!z) {
            i5 = i4;
            i4 = i5;
        }
        int i6 = this.mTouchWidth;
        return i4 >= i3 - i6 && i4 <= i3 + i6 && i5 >= i && i5 <= i2;
    }

    /* renamed from: com.android.systemui.screenshot.ScreenshotSelectorView$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle;

        static {
            int[] iArr = new int[ResizingHandle.values().length];
            $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle = iArr;
            try {
                iArr[ResizingHandle.LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.TOP_LEFT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.TOP.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.TOP_RIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.RIGHT.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.BOTTOM_RIGHT.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.BOTTOM.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[ResizingHandle.BOTTOM_LEFT.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
        }
    }

    public boolean isTouching(ResizingHandle resizingHandle, int i, int i2) {
        switch (AnonymousClass1.$SwitchMap$com$android$systemui$screenshot$ScreenshotSelectorView$ResizingHandle[resizingHandle.ordinal()]) {
            case 1:
                Rect rect = this.mSelectionRect;
                return isTouchingBorder(rect.top, rect.bottom, rect.left, i, i2, true);
            case 2:
                Rect rect2 = this.mSelectionRect;
                return isTouchingCenteredSquare(rect2.left, rect2.top, i, i2);
            case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                Rect rect3 = this.mSelectionRect;
                return isTouchingBorder(rect3.left, rect3.right, rect3.top, i, i2, false);
            case 4:
                Rect rect4 = this.mSelectionRect;
                return isTouchingCenteredSquare(rect4.right, rect4.top, i, i2);
            case 5:
                Rect rect5 = this.mSelectionRect;
                return isTouchingBorder(rect5.top, rect5.bottom, rect5.right, i, i2, true);
            case 6:
                Rect rect6 = this.mSelectionRect;
                return isTouchingCenteredSquare(rect6.right, rect6.bottom, i, i2);
            case 7:
                Rect rect7 = this.mSelectionRect;
                return isTouchingBorder(rect7.left, rect7.right, rect7.bottom, i, i2, false);
            case QS.VERSION /* 8 */:
                Rect rect8 = this.mSelectionRect;
                return isTouchingCenteredSquare(rect8.left, rect8.bottom, i, i2);
            default:
                return false;
        }
    }

    private ResizingHandle getTouchedResizingHandle(int i, int i2) {
        for (ResizingHandle resizingHandle : ResizingHandle.values()) {
            if (isTouching(resizingHandle, i, i2)) {
                return resizingHandle;
            }
        }
        return ResizingHandle.INVALID;
    }

    public boolean isInsideSelection(int i, int i2) {
        return this.mSelectionRect.contains(i, i2);
    }

    private void resizeSelection(int i, int i2) {
        if (this.mResizingHandle.isLeft()) {
            this.mSelectionRect.left = Math.max(i - this.mResizingOffsetX, 0);
        }
        if (this.mResizingHandle.isTop()) {
            this.mSelectionRect.top = Math.max(i2 - this.mResizingOffsetY, 0);
        }
        if (this.mResizingHandle.isRight()) {
            this.mSelectionRect.right = Math.min(i - this.mResizingOffsetX, getMeasuredWidth());
        }
        if (this.mResizingHandle.isBottom()) {
            this.mSelectionRect.bottom = Math.min(i2 - this.mResizingOffsetY, getMeasuredHeight());
        }
        invalidate();
    }

    private void setMovingOffset(int i, int i2) {
        Rect rect = this.mSelectionRect;
        this.mMovingOffsetX = i - rect.left;
        this.mMovingOffsetY = i2 - rect.top;
    }

    private void setResizingOffset(int i, int i2) {
        this.mResizingOffsetX = 0;
        this.mResizingOffsetY = 0;
        if (this.mResizingHandle.isLeft()) {
            this.mResizingOffsetX = i - this.mSelectionRect.left;
        }
        if (this.mResizingHandle.isTop()) {
            this.mResizingOffsetY = i2 - this.mSelectionRect.top;
        }
        if (this.mResizingHandle.isRight()) {
            this.mResizingOffsetX = i - this.mSelectionRect.right;
        }
        if (this.mResizingHandle.isBottom()) {
            this.mResizingOffsetY = i2 - this.mSelectionRect.bottom;
        }
    }

    private void moveSelection(int i, int i2) {
        int i3 = i - this.mMovingOffsetX;
        int i4 = i2 - this.mMovingOffsetY;
        int iWidth = this.mSelectionRect.width() + i3;
        int iHeight = this.mSelectionRect.height() + i4;
        if (i3 < 0) {
            iWidth += -i3;
            i3 = 0;
        }
        if (i4 < 0) {
            iHeight += -i4;
            i4 = 0;
        }
        int measuredWidth = getMeasuredWidth();
        int i5 = iWidth - measuredWidth;
        if (i5 > 0) {
            i3 -= i5;
            iWidth = measuredWidth;
        }
        int measuredHeight = getMeasuredHeight();
        int i6 = iHeight - measuredHeight;
        if (i6 > 0) {
            i4 -= i6;
            iHeight = measuredHeight;
        }
        this.mSelectionRect.set(i3, i4, iWidth, iHeight);
        invalidate();
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(((FrameLayout) this).mLeft, ((FrameLayout) this).mTop, ((FrameLayout) this).mRight, ((FrameLayout) this).mBottom, this.mPaintBackground);
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            this.mDrawingRect.set(rect);
            this.mDrawingRect.sort();
            canvas.drawRect(this.mDrawingRect, this.mPaintSelectionBorder);
            Rect rect2 = this.mDrawingRect;
            int i = rect2.left;
            float f = rect2.top;
            float fMin = Math.min(i + this.mTouchWidth, rect2.right);
            Rect rect3 = this.mDrawingRect;
            canvas.drawRect(i, f, fMin, Math.min(rect3.top + this.mTouchWidth, rect3.bottom), this.mPaintSelectionCorner);
            Rect rect4 = this.mDrawingRect;
            canvas.drawRect(Math.max(rect4.right - this.mTouchWidth, rect4.left), this.mDrawingRect.top, r0.right, Math.min(r1 + this.mTouchWidth, r0.bottom), this.mPaintSelectionCorner);
            Rect rect5 = this.mDrawingRect;
            float fMax = Math.max(rect5.right - this.mTouchWidth, rect5.left);
            Rect rect6 = this.mDrawingRect;
            float fMax2 = Math.max(rect6.bottom - this.mTouchWidth, rect6.top);
            Rect rect7 = this.mDrawingRect;
            canvas.drawRect(fMax, fMax2, rect7.right, rect7.bottom, this.mPaintSelectionCorner);
            Rect rect8 = this.mDrawingRect;
            float f2 = rect8.left;
            float fMax3 = Math.max(rect8.bottom - this.mTouchWidth, rect8.top);
            Rect rect9 = this.mDrawingRect;
            canvas.drawRect(f2, fMax3, Math.min(rect9.left + this.mTouchWidth, rect9.right), this.mDrawingRect.bottom, this.mPaintSelectionCorner);
            canvas.drawRect(this.mDrawingRect, this.mPaintSelection);
        }
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action == 1) {
                sortSelectionRect();
                delegateSelection();
                this.mResizingHandle = ResizingHandle.INVALID;
                this.mIsFirstSelection = false;
                this.mIsMoving = false;
            } else if (action == 2) {
                if (this.mResizingHandle.isValid()) {
                    resizeSelection(x, y);
                } else if (this.mIsMoving) {
                    moveSelection(x, y);
                }
            }
        } else if (this.mSelectionRect == null) {
            startSelection(x, y);
            this.mIsFirstSelection = true;
            this.mResizingHandle = ResizingHandle.BOTTOM_RIGHT;
        } else {
            ResizingHandle touchedResizingHandle = getTouchedResizingHandle(x, y);
            this.mResizingHandle = touchedResizingHandle;
            if (touchedResizingHandle.isValid()) {
                setResizingOffset(x, y);
            } else if (isInsideSelection(x, y)) {
                this.mIsMoving = true;
                setMovingOffset(x, y);
            }
        }
        return true;
    }
}
