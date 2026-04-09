package com.android.settingslib.graph;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import com.android.settingslib.R$array;
import com.android.settingslib.R$color;
import com.android.settingslib.R$dimen;
import com.android.settingslib.R$fraction;
import com.android.settingslib.R$string;
import com.android.settingslib.Utils;

/* loaded from: classes.dex */
public class BatteryMeterDrawableBase extends Drawable {
    public static final String TAG = BatteryMeterDrawableBase.class.getSimpleName();
    private boolean mAnimateCharging;
    private int mBatteryAlpha;
    protected final Paint mBatteryPaint;
    private final RectF mBoltFrame;
    protected final Paint mBoltPaint;
    private final Path mBoltPath;
    private final float[] mBoltPoints;
    private final RectF mButtonFrame;
    protected float mButtonHeightFraction;
    private int mChargeColor;
    private boolean mCharging;
    private ValueAnimator mChargingAnimator;
    private final int[] mColors;
    protected final Context mContext;
    private final int mCriticalLevel;
    private final RectF mFrame;
    protected final Paint mFramePaint;
    private int mHeight;
    private int mIconTint;
    private int mIntrinsicHeight;
    private int mIntrinsicWidth;
    private int mLevel;
    private int mMeterStyle;
    private float mOldDarkIntensity;
    private final Path mOutlinePath;
    private final Rect mPadding;
    private final DashPathEffect mPathEffect;
    private final RectF mPlusFrame;
    protected final Paint mPlusPaint;
    private final Path mPlusPath;
    private final float[] mPlusPoints;
    protected boolean mPowerSaveAsColorError;
    private boolean mPowerSaveEnabled;
    protected final Paint mPowersavePaint;
    private final Path mShapePath;
    private boolean mShowPercent;
    private float mSubpixelSmoothingLeft;
    private float mSubpixelSmoothingRight;
    private float mTextHeight;
    protected final Paint mTextPaint;
    private final Path mTextPath;
    private String mWarningString;
    private float mWarningTextHeight;
    protected final Paint mWarningTextPaint;
    private int mWidth;

    protected float getAspectRatio() {
        return 0.58f;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return 0;
    }

    protected float getRadiusRatio() {
        return 0.05882353f;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    public BatteryMeterDrawableBase(Context context, int i) {
        this(context, i, 0);
    }

    public BatteryMeterDrawableBase(Context context, int i, int i2) throws Resources.NotFoundException {
        this.mLevel = -1;
        this.mPowerSaveAsColorError = true;
        this.mIconTint = -1;
        this.mOldDarkIntensity = -1.0f;
        this.mBoltPath = new Path();
        this.mPlusPath = new Path();
        this.mPadding = new Rect();
        this.mFrame = new RectF();
        this.mButtonFrame = new RectF();
        this.mBoltFrame = new RectF();
        this.mPlusFrame = new RectF();
        this.mShapePath = new Path();
        this.mOutlinePath = new Path();
        this.mTextPath = new Path();
        this.mPathEffect = new DashPathEffect(new float[]{3.0f, 2.0f}, 0.0f);
        this.mContext = context;
        this.mMeterStyle = i2;
        Resources resources = context.getResources();
        if (Settings.System.getIntForUser(context.getContentResolver(), "battery_level_colors", 0, -2) == 1) {
            TypedArray typedArrayObtainTypedArray = resources.obtainTypedArray(R$array.corvus_batterymeter_color_levels);
            TypedArray typedArrayObtainTypedArray2 = resources.obtainTypedArray(R$array.corvus_batterymeter_color_values);
            int length = typedArrayObtainTypedArray.length();
            this.mColors = new int[length * 7];
            for (int i3 = 0; i3 < length; i3++) {
                int i4 = i3 * 7;
                this.mColors[i4] = typedArrayObtainTypedArray.getInt(i3, 0);
                if (typedArrayObtainTypedArray2.getType(i3) == 2) {
                    this.mColors[i4 + 1] = Utils.getColorAttrDefaultColor(this.mContext, typedArrayObtainTypedArray2.getThemeAttributeId(i3, 0));
                } else {
                    this.mColors[i4 + 1] = typedArrayObtainTypedArray2.getColor(i3, 0);
                }
            }
            typedArrayObtainTypedArray.recycle();
            typedArrayObtainTypedArray2.recycle();
        } else {
            TypedArray typedArrayObtainTypedArray3 = resources.obtainTypedArray(R$array.batterymeter_color_levels);
            TypedArray typedArrayObtainTypedArray4 = resources.obtainTypedArray(R$array.batterymeter_color_values);
            int length2 = typedArrayObtainTypedArray3.length();
            this.mColors = new int[length2 * 2];
            for (int i5 = 0; i5 < length2; i5++) {
                int i6 = i5 * 2;
                this.mColors[i6] = typedArrayObtainTypedArray3.getInt(i5, 0);
                if (typedArrayObtainTypedArray4.getType(i5) == 2) {
                    this.mColors[i6 + 1] = Utils.getColorAttrDefaultColor(context, typedArrayObtainTypedArray4.getThemeAttributeId(i5, 0));
                } else {
                    this.mColors[i6 + 1] = typedArrayObtainTypedArray4.getColor(i5, 0);
                }
            }
            typedArrayObtainTypedArray3.recycle();
            typedArrayObtainTypedArray4.recycle();
        }
        this.mWarningString = context.getString(R$string.battery_meter_very_low_overlay_symbol);
        this.mCriticalLevel = this.mContext.getResources().getInteger(R.integer.config_bg_current_drain_types_to_restricted_bucket);
        this.mButtonHeightFraction = context.getResources().getFraction(R$fraction.battery_button_height_fraction, 1, 1);
        this.mSubpixelSmoothingLeft = context.getResources().getFraction(R$fraction.battery_subpixel_smoothing_left, 1, 1);
        this.mSubpixelSmoothingRight = context.getResources().getFraction(R$fraction.battery_subpixel_smoothing_right, 1, 1);
        Paint paint = new Paint(1);
        this.mFramePaint = paint;
        paint.setColor(i);
        paint.setDither(true);
        Paint paint2 = new Paint(1);
        this.mBatteryPaint = paint2;
        paint2.setDither(true);
        Paint paint3 = new Paint(1);
        this.mTextPaint = paint3;
        paint3.setTypeface(Typeface.create("sans-serif-condensed", 1));
        paint3.setTextAlign(Paint.Align.CENTER);
        Paint paint4 = new Paint(1);
        this.mWarningTextPaint = paint4;
        paint4.setTypeface(Typeface.create("sans-serif", 1));
        paint4.setTextAlign(Paint.Align.CENTER);
        int[] iArr = this.mColors;
        if (iArr.length > 1) {
            paint4.setColor(iArr[1]);
        }
        this.mChargeColor = Utils.getColorStateListDefaultColor(this.mContext, R$color.meter_consumed_color);
        Paint paint5 = new Paint(1);
        this.mBoltPaint = paint5;
        paint5.setColor(Utils.getColorStateListDefaultColor(this.mContext, R$color.batterymeter_bolt_color));
        this.mBoltPoints = loadPoints(resources, R$array.batterymeter_bolt_points);
        Paint paint6 = new Paint(1);
        this.mPlusPaint = paint6;
        paint6.setColor(Utils.getColorStateListDefaultColor(this.mContext, R$color.batterymeter_plus_color));
        this.mPlusPoints = loadPoints(resources, R$array.batterymeter_plus_points);
        Paint paint7 = new Paint(1);
        this.mPowersavePaint = paint7;
        paint7.setColor(paint6.getColor());
        paint7.setStyle(Paint.Style.STROKE);
        this.mIntrinsicWidth = context.getResources().getDimensionPixelSize(R$dimen.battery_width);
        this.mIntrinsicHeight = context.getResources().getDimensionPixelSize(R$dimen.battery_height);
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mIntrinsicHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mIntrinsicWidth;
    }

    public void setShowPercent(boolean z) {
        if (this.mShowPercent != z) {
            this.mShowPercent = z;
            postInvalidate();
        }
    }

    private boolean canAnimate() {
        int i = this.mMeterStyle;
        return i == 6 || i == 2 || i == 0 || i == 1;
    }

    private boolean checkChargingAnimation() {
        if (canAnimate() && this.mCharging) {
            if (!this.mAnimateCharging) {
                this.mAnimateCharging = true;
                startChargingAnimation();
            }
            return true;
        }
        cancelChargingAnimation();
        this.mAnimateCharging = false;
        return false;
    }

    public void setCharging(boolean z) {
        if (this.mCharging != z) {
            this.mCharging = z;
            postInvalidate();
        }
    }

    public void setBatteryLevel(int i) {
        if (this.mLevel != i) {
            this.mLevel = i;
            postInvalidate();
        }
    }

    public void setPowerSave(boolean z) {
        if (this.mPowerSaveEnabled != z) {
            this.mPowerSaveEnabled = z;
            postInvalidate();
        }
    }

    public void setMeterStyle(int i) throws Resources.NotFoundException {
        this.mMeterStyle = i;
        updateSize();
        postInvalidate();
    }

    protected void postInvalidate() {
        if (checkChargingAnimation()) {
            return;
        }
        unscheduleSelf(new Runnable() { // from class: com.android.settingslib.graph.BatteryMeterDrawableBase$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.invalidateSelf();
            }
        });
        scheduleSelf(new Runnable() { // from class: com.android.settingslib.graph.BatteryMeterDrawableBase$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.invalidateSelf();
            }
        }, 0L);
    }

    private void startChargingAnimation() {
        cancelChargingAnimation();
        final int alpha = this.mBatteryPaint.getAlpha();
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(alpha, 0, alpha);
        this.mChargingAnimator = valueAnimatorOfInt;
        valueAnimatorOfInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.settingslib.graph.BatteryMeterDrawableBase.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                BatteryMeterDrawableBase.this.mBatteryAlpha = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                BatteryMeterDrawableBase.this.invalidateSelf();
            }
        });
        this.mChargingAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.settingslib.graph.BatteryMeterDrawableBase.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                BatteryMeterDrawableBase.this.mBatteryAlpha = alpha;
                BatteryMeterDrawableBase.this.invalidateSelf();
                onAnimationEnd(animator);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                BatteryMeterDrawableBase.this.mChargingAnimator = null;
            }
        });
        this.mChargingAnimator.setRepeatCount(-1);
        this.mChargingAnimator.setDuration(2000L);
        this.mChargingAnimator.setStartDelay(500L);
        this.mChargingAnimator.start();
    }

    private void cancelChargingAnimation() {
        ValueAnimator valueAnimator = this.mChargingAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private static float[] loadPoints(Resources resources, int i) throws Resources.NotFoundException {
        int[] intArray = resources.getIntArray(i);
        int iMax = 0;
        int iMax2 = 0;
        for (int i2 = 0; i2 < intArray.length; i2 += 2) {
            iMax = Math.max(iMax, intArray[i2]);
            iMax2 = Math.max(iMax2, intArray[i2 + 1]);
        }
        float[] fArr = new float[intArray.length];
        for (int i3 = 0; i3 < intArray.length; i3 += 2) {
            fArr[i3] = intArray[i3] / iMax;
            fArr[i3 + 1] = intArray[r3] / iMax2;
        }
        return fArr;
    }

    @Override // android.graphics.drawable.Drawable
    public void setBounds(int i, int i2, int i3, int i4) throws Resources.NotFoundException {
        super.setBounds(i, i2, i3, i4);
        updateSize();
    }

    private void updateSize() throws Resources.NotFoundException {
        int dimensionPixelSize;
        Rect bounds = getBounds();
        int i = bounds.bottom;
        Rect rect = this.mPadding;
        int i2 = (i - rect.bottom) - (bounds.top + rect.top);
        this.mHeight = i2;
        this.mWidth = (bounds.right - rect.right) - (bounds.left + rect.left);
        this.mWarningTextPaint.setTextSize(i2 * 0.75f);
        this.mWarningTextHeight = -this.mWarningTextPaint.getFontMetrics().ascent;
        Resources resources = this.mContext.getResources();
        int i3 = R$dimen.battery_height;
        this.mIntrinsicHeight = resources.getDimensionPixelSize(i3);
        if (this.mMeterStyle == 0) {
            dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R$dimen.battery_width);
        } else {
            dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(i3);
        }
        this.mIntrinsicWidth = dimensionPixelSize;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect rect) {
        Rect rect2 = this.mPadding;
        if (rect2.left == 0 && rect2.top == 0 && rect2.right == 0 && rect2.bottom == 0) {
            return super.getPadding(rect);
        }
        rect.set(rect2);
        return true;
    }

    public void setPadding(int i, int i2, int i3, int i4) throws Resources.NotFoundException {
        Rect rect = this.mPadding;
        rect.left = i;
        rect.top = i2;
        rect.right = i3;
        rect.bottom = i4;
        updateSize();
    }

    private int getColorForLevel(int i) {
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = this.mColors;
            if (i2 >= iArr.length) {
                return i3;
            }
            int i4 = iArr[i2];
            int i5 = iArr[i2 + 1];
            if (i <= i4) {
                return i2 == iArr.length + (-2) ? this.mIconTint : i5;
            }
            i2 += 2;
            i3 = i5;
        }
    }

    public void setColors(int i, int i2) {
        this.mIconTint = i;
        this.mFramePaint.setColor(i2);
        this.mBoltPaint.setColor(i);
        this.mChargeColor = i;
        invalidateSelf();
    }

    protected int batteryColorForLevel(int i) {
        if (this.mCharging || (this.mPowerSaveEnabled && this.mPowerSaveAsColorError)) {
            return this.mChargeColor;
        }
        return getColorForLevel(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        int i = this.mMeterStyle;
        if (i == 0) {
            drawRectangle(canvas);
        } else if (i != 6) {
            drawCircle(canvas);
        } else {
            drawSolid(canvas);
        }
    }

    private void drawCircle(Canvas canvas) {
        float[] fArr;
        float[] fArr2;
        int i = this.mLevel;
        if (i == -1) {
            return;
        }
        float fMin = Math.min(this.mWidth, this.mHeight);
        float f = fMin / 6.5f;
        this.mFramePaint.setStrokeWidth(f);
        this.mFramePaint.setStyle(Paint.Style.STROKE);
        this.mBatteryPaint.setStrokeWidth(f);
        this.mBatteryPaint.setStyle(Paint.Style.STROKE);
        this.mPowersavePaint.setStrokeWidth(f);
        int i2 = 2;
        if (this.mMeterStyle == 2) {
            this.mBatteryPaint.setPathEffect(this.mPathEffect);
        } else {
            this.mBatteryPaint.setPathEffect(null);
        }
        RectF rectF = this.mFrame;
        float f2 = f / 2.0f;
        int i3 = this.mPadding.left;
        float f3 = fMin - f2;
        rectF.set(i3 + f2, f2, i3 + f3, f3);
        this.mBatteryPaint.setColor(batteryColorForLevel(i));
        if (this.mChargingAnimator != null) {
            this.mBoltPaint.setAlpha(this.mBatteryAlpha);
        }
        if (this.mCharging) {
            RectF rectF2 = this.mFrame;
            float fWidth = rectF2.left + (rectF2.width() / 3.0f);
            RectF rectF3 = this.mFrame;
            float fHeight = rectF3.top + (rectF3.height() / 3.4f);
            RectF rectF4 = this.mFrame;
            float fWidth2 = rectF4.right - (rectF4.width() / 4.0f);
            RectF rectF5 = this.mFrame;
            float fHeight2 = rectF5.bottom - (rectF5.height() / 5.6f);
            RectF rectF6 = this.mBoltFrame;
            if (rectF6.left != fWidth || rectF6.top != fHeight || rectF6.right != fWidth2 || rectF6.bottom != fHeight2) {
                rectF6.set(fWidth, fHeight, fWidth2, fHeight2);
                this.mBoltPath.reset();
                Path path = this.mBoltPath;
                RectF rectF7 = this.mBoltFrame;
                float fWidth3 = rectF7.left + (this.mBoltPoints[0] * rectF7.width());
                RectF rectF8 = this.mBoltFrame;
                path.moveTo(fWidth3, rectF8.top + (this.mBoltPoints[1] * rectF8.height()));
                int i4 = 2;
                while (true) {
                    fArr2 = this.mBoltPoints;
                    if (i4 >= fArr2.length) {
                        break;
                    }
                    Path path2 = this.mBoltPath;
                    RectF rectF9 = this.mBoltFrame;
                    float fWidth4 = rectF9.left + (fArr2[i4] * rectF9.width());
                    RectF rectF10 = this.mBoltFrame;
                    path2.lineTo(fWidth4, rectF10.top + (this.mBoltPoints[i4 + 1] * rectF10.height()));
                    i4 += 2;
                }
                Path path3 = this.mBoltPath;
                RectF rectF11 = this.mBoltFrame;
                float fWidth5 = rectF11.left + (fArr2[0] * rectF11.width());
                RectF rectF12 = this.mBoltFrame;
                path3.lineTo(fWidth5, rectF12.top + (this.mBoltPoints[1] * rectF12.height()));
            }
            canvas.drawPath(this.mBoltPath, this.mBoltPaint);
        }
        canvas.drawArc(this.mFrame, 270.0f, 360.0f, false, this.mFramePaint);
        if (i > 0) {
            if (!this.mCharging && this.mPowerSaveEnabled && this.mPowerSaveAsColorError) {
                canvas.drawArc(this.mFrame, 270.0f, i * 3.6f, false, this.mPowersavePaint);
            } else {
                canvas.drawArc(this.mFrame, 270.0f, i * 3.6f, false, this.mBatteryPaint);
            }
        }
        if (!this.mCharging && i != 100 && this.mShowPercent && !this.mPowerSaveEnabled) {
            this.mTextPaint.setColor(getColorForLevel(i));
            this.mTextPaint.setTextSize(this.mHeight * 0.52f);
            this.mTextHeight = -this.mTextPaint.getFontMetrics().ascent;
            canvas.drawText(i > this.mCriticalLevel ? String.valueOf(i) : this.mWarningString, this.mWidth * 0.5f, (this.mHeight + this.mTextHeight) * 0.47f, this.mTextPaint);
            return;
        }
        if (this.mPowerSaveEnabled) {
            float fWidth6 = this.mFrame.width() / 2.0f;
            RectF rectF13 = this.mFrame;
            float fWidth7 = rectF13.left + ((rectF13.width() - fWidth6) / 2.0f);
            RectF rectF14 = this.mFrame;
            float fHeight3 = rectF14.top + ((rectF14.height() - fWidth6) / 2.0f);
            RectF rectF15 = this.mFrame;
            float fWidth8 = rectF15.right - ((rectF15.width() - fWidth6) / 2.0f);
            RectF rectF16 = this.mFrame;
            float fHeight4 = rectF16.bottom - ((rectF16.height() - fWidth6) / 2.0f);
            RectF rectF17 = this.mPlusFrame;
            if (rectF17.left != fWidth7 || rectF17.top != fHeight3 || rectF17.right != fWidth8 || rectF17.bottom != fHeight4) {
                rectF17.set(fWidth7, fHeight3, fWidth8, fHeight4);
                this.mPlusPath.reset();
                Path path4 = this.mPlusPath;
                RectF rectF18 = this.mPlusFrame;
                float fWidth9 = rectF18.left + (this.mPlusPoints[0] * rectF18.width());
                RectF rectF19 = this.mPlusFrame;
                path4.moveTo(fWidth9, rectF19.top + (this.mPlusPoints[1] * rectF19.height()));
                while (true) {
                    fArr = this.mPlusPoints;
                    if (i2 >= fArr.length) {
                        break;
                    }
                    Path path5 = this.mPlusPath;
                    RectF rectF20 = this.mPlusFrame;
                    float fWidth10 = rectF20.left + (fArr[i2] * rectF20.width());
                    RectF rectF21 = this.mPlusFrame;
                    path5.lineTo(fWidth10, rectF21.top + (this.mPlusPoints[i2 + 1] * rectF21.height()));
                    i2 += 2;
                }
                Path path6 = this.mPlusPath;
                RectF rectF22 = this.mPlusFrame;
                float fWidth11 = rectF22.left + (fArr[0] * rectF22.width());
                RectF rectF23 = this.mPlusFrame;
                path6.lineTo(fWidth11, rectF23.top + (this.mPlusPoints[1] * rectF23.height()));
            }
            this.mShapePath.op(this.mPlusPath, Path.Op.DIFFERENCE);
            if (this.mPowerSaveAsColorError) {
                canvas.drawPath(this.mPlusPath, this.mPlusPaint);
            }
        }
    }

    private void drawRectangle(Canvas canvas) {
        float fHeight;
        float[] fArr;
        float f;
        float f2;
        String strValueOf;
        float[] fArr2;
        int i = this.mLevel;
        Rect bounds = getBounds();
        if (i == -1) {
            return;
        }
        float f3 = i / 100.0f;
        int i2 = this.mHeight;
        int aspectRatio = (int) (getAspectRatio() * this.mHeight);
        int i3 = (this.mWidth - aspectRatio) / 2;
        float f4 = i2;
        int iRound = Math.round(this.mButtonHeightFraction * f4);
        Rect rect = this.mPadding;
        int i4 = rect.left + bounds.left;
        int i5 = (bounds.bottom - rect.bottom) - i2;
        this.mFramePaint.setStrokeWidth(0.0f);
        this.mFramePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mBatteryPaint.setStrokeWidth(0.0f);
        this.mBatteryPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mPowersavePaint.setStrokeWidth(this.mContext.getResources().getDimensionPixelSize(R$dimen.battery_powersave_outline_thickness));
        if (this.mMeterStyle == 0) {
            this.mBatteryPaint.setPathEffect(this.mPathEffect);
        } else {
            this.mBatteryPaint.setPathEffect(null);
        }
        float f5 = i4;
        float f6 = i5;
        this.mFrame.set(f5, f6, i4 + aspectRatio, i2 + i5);
        this.mFrame.offset(i3, 0.0f);
        RectF rectF = this.mButtonFrame;
        float f7 = aspectRatio * 0.28f;
        float fRound = this.mFrame.left + Math.round(f7);
        RectF rectF2 = this.mFrame;
        float f8 = iRound;
        rectF.set(fRound, rectF2.top, rectF2.right - Math.round(f7), this.mFrame.top + f8);
        this.mFrame.top += f8;
        this.mBatteryPaint.setColor(batteryColorForLevel(i));
        if (this.mChargingAnimator != null) {
            this.mBatteryPaint.setAlpha(this.mBatteryAlpha);
        }
        if (i >= 96) {
            f3 = 1.0f;
        } else if (i <= this.mCriticalLevel) {
            f3 = 0.0f;
        }
        if (f3 == 1.0f) {
            fHeight = this.mButtonFrame.top;
        } else {
            RectF rectF3 = this.mFrame;
            fHeight = (rectF3.height() * (1.0f - f3)) + rectF3.top;
        }
        this.mShapePath.reset();
        this.mOutlinePath.reset();
        float radiusRatio = getRadiusRatio() * (this.mFrame.height() + f8);
        this.mShapePath.setFillType(Path.FillType.WINDING);
        this.mShapePath.addRoundRect(this.mFrame, radiusRatio, radiusRatio, Path.Direction.CW);
        this.mShapePath.addRect(this.mButtonFrame, Path.Direction.CW);
        this.mOutlinePath.addRoundRect(this.mFrame, radiusRatio, radiusRatio, Path.Direction.CW);
        Path path = new Path();
        path.addRect(this.mButtonFrame, Path.Direction.CW);
        this.mOutlinePath.op(path, Path.Op.XOR);
        if (this.mCharging) {
            RectF rectF4 = this.mFrame;
            float fWidth = rectF4.left + (rectF4.width() / 4.0f) + 1.0f;
            RectF rectF5 = this.mFrame;
            float fHeight2 = rectF5.top + (rectF5.height() / 6.0f);
            RectF rectF6 = this.mFrame;
            float fWidth2 = (rectF6.right - (rectF6.width() / 4.0f)) + 1.0f;
            RectF rectF7 = this.mFrame;
            float fHeight3 = rectF7.bottom - (rectF7.height() / 10.0f);
            RectF rectF8 = this.mBoltFrame;
            if (rectF8.left != fWidth || rectF8.top != fHeight2 || rectF8.right != fWidth2 || rectF8.bottom != fHeight3) {
                rectF8.set(fWidth, fHeight2, fWidth2, fHeight3);
                this.mBoltPath.reset();
                Path path2 = this.mBoltPath;
                RectF rectF9 = this.mBoltFrame;
                float fWidth3 = rectF9.left + (this.mBoltPoints[0] * rectF9.width());
                RectF rectF10 = this.mBoltFrame;
                path2.moveTo(fWidth3, rectF10.top + (this.mBoltPoints[1] * rectF10.height()));
                int i6 = 2;
                while (true) {
                    fArr2 = this.mBoltPoints;
                    if (i6 >= fArr2.length) {
                        break;
                    }
                    Path path3 = this.mBoltPath;
                    RectF rectF11 = this.mBoltFrame;
                    float fWidth4 = rectF11.left + (fArr2[i6] * rectF11.width());
                    RectF rectF12 = this.mBoltFrame;
                    path3.lineTo(fWidth4, rectF12.top + (this.mBoltPoints[i6 + 1] * rectF12.height()));
                    i6 += 2;
                }
                Path path4 = this.mBoltPath;
                RectF rectF13 = this.mBoltFrame;
                float fWidth5 = rectF13.left + (fArr2[0] * rectF13.width());
                RectF rectF14 = this.mBoltFrame;
                path4.lineTo(fWidth5, rectF14.top + (this.mBoltPoints[1] * rectF14.height()));
            }
            RectF rectF15 = this.mBoltFrame;
            float f9 = rectF15.bottom;
            if (Math.min(Math.max((f9 - fHeight) / (f9 - rectF15.top), 0.0f), 1.0f) <= 0.3f) {
                canvas.drawPath(this.mBoltPath, this.mBoltPaint);
            } else {
                this.mShapePath.op(this.mBoltPath, Path.Op.DIFFERENCE);
            }
        } else if (this.mPowerSaveEnabled) {
            float fWidth6 = (this.mFrame.width() * 2.0f) / 3.0f;
            RectF rectF16 = this.mFrame;
            float fWidth7 = rectF16.left + ((rectF16.width() - fWidth6) / 2.0f);
            RectF rectF17 = this.mFrame;
            float fHeight4 = rectF17.top + ((rectF17.height() - fWidth6) / 2.0f);
            RectF rectF18 = this.mFrame;
            float fWidth8 = rectF18.right - ((rectF18.width() - fWidth6) / 2.0f);
            RectF rectF19 = this.mFrame;
            float fHeight5 = rectF19.bottom - ((rectF19.height() - fWidth6) / 2.0f);
            RectF rectF20 = this.mPlusFrame;
            if (rectF20.left != fWidth7 || rectF20.top != fHeight4 || rectF20.right != fWidth8 || rectF20.bottom != fHeight5) {
                rectF20.set(fWidth7, fHeight4, fWidth8, fHeight5);
                this.mPlusPath.reset();
                Path path5 = this.mPlusPath;
                RectF rectF21 = this.mPlusFrame;
                float fWidth9 = rectF21.left + (this.mPlusPoints[0] * rectF21.width());
                RectF rectF22 = this.mPlusFrame;
                path5.moveTo(fWidth9, rectF22.top + (this.mPlusPoints[1] * rectF22.height()));
                int i7 = 2;
                while (true) {
                    fArr = this.mPlusPoints;
                    if (i7 >= fArr.length) {
                        break;
                    }
                    Path path6 = this.mPlusPath;
                    RectF rectF23 = this.mPlusFrame;
                    float fWidth10 = rectF23.left + (fArr[i7] * rectF23.width());
                    RectF rectF24 = this.mPlusFrame;
                    path6.lineTo(fWidth10, rectF24.top + (this.mPlusPoints[i7 + 1] * rectF24.height()));
                    i7 += 2;
                }
                Path path7 = this.mPlusPath;
                RectF rectF25 = this.mPlusFrame;
                float fWidth11 = rectF25.left + (fArr[0] * rectF25.width());
                RectF rectF26 = this.mPlusFrame;
                path7.lineTo(fWidth11, rectF26.top + (this.mPlusPoints[1] * rectF26.height()));
            }
            this.mShapePath.op(this.mPlusPath, Path.Op.DIFFERENCE);
            if (this.mPowerSaveAsColorError) {
                canvas.drawPath(this.mPlusPath, this.mPlusPaint);
            }
        }
        if (this.mCharging || this.mPowerSaveEnabled || i <= this.mCriticalLevel || !this.mShowPercent) {
            f = 0.0f;
            f2 = 0.0f;
            strValueOf = null;
        } else {
            this.mTextPaint.setColor(getColorForLevel(i));
            this.mTextPaint.setTextSize(f4 * (this.mLevel == 100 ? 0.38f : 0.5f));
            this.mTextHeight = -this.mTextPaint.getFontMetrics().ascent;
            strValueOf = String.valueOf(i);
            f2 = (this.mWidth * 0.5f) + f5;
            f = ((this.mHeight + this.mTextHeight) * 0.47f) + f6;
            z = fHeight > f;
            if (!z) {
                this.mTextPath.reset();
                this.mTextPaint.getTextPath(strValueOf, 0, strValueOf.length(), f2, f, this.mTextPath);
                this.mShapePath.op(this.mTextPath, Path.Op.DIFFERENCE);
            }
        }
        canvas.drawPath(this.mShapePath, this.mFramePaint);
        this.mFrame.top = fHeight;
        canvas.save();
        canvas.clipRect(this.mFrame);
        canvas.drawPath(this.mShapePath, this.mBatteryPaint);
        canvas.restore();
        if (i > 0 && !this.mCharging && this.mPowerSaveEnabled && this.mPowerSaveAsColorError) {
            canvas.drawPath(this.mShapePath, this.mBatteryPaint);
        }
        if (!this.mCharging && !this.mPowerSaveEnabled) {
            if (i <= this.mCriticalLevel) {
                float f10 = ((this.mHeight + this.mWarningTextHeight) * 0.48f) + f6;
                this.mWarningTextPaint.setColor(this.mIconTint);
                canvas.drawText(this.mWarningString, (this.mWidth * 0.5f) + f5, f10, this.mWarningTextPaint);
            } else if (z) {
                canvas.drawText(strValueOf, f2, f, this.mTextPaint);
            }
        }
        if (!this.mCharging && this.mPowerSaveEnabled && this.mPowerSaveAsColorError) {
            canvas.drawPath(this.mOutlinePath, this.mPowersavePaint);
        }
    }

    private void drawSolid(Canvas canvas) {
        float[] fArr;
        int i = this.mLevel;
        if (i == -1) {
            return;
        }
        int iMin = Math.min(this.mWidth, this.mHeight);
        int i2 = iMin / 2;
        float strokeWidth = this.mPowersavePaint.getStrokeWidth();
        float f = i / 100.0f;
        this.mFramePaint.setStrokeWidth(0.0f);
        this.mFramePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mBatteryPaint.setStrokeWidth(0.0f);
        this.mBatteryPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF rectF = this.mFrame;
        float f2 = strokeWidth / 2.0f;
        int i3 = this.mPadding.left;
        float f3 = iMin - f2;
        rectF.set(i3 + f2, f2, i3 + f3, f3);
        this.mBatteryPaint.setColor(batteryColorForLevel(i));
        if (this.mChargingAnimator != null) {
            this.mBatteryPaint.setAlpha(this.mBatteryAlpha);
        }
        boolean z = this.mCharging;
        if (!z && i != 100 && !this.mPowerSaveEnabled && this.mShowPercent) {
            this.mTextPaint.setColor(getColorForLevel(i));
            this.mTextPaint.setTextSize(this.mHeight * 0.62f);
            this.mTextHeight = -this.mTextPaint.getFontMetrics().ascent;
            String strValueOf = i > this.mCriticalLevel ? String.valueOf(i) : this.mWarningString;
            float f4 = (this.mHeight + this.mTextHeight) * 0.47f;
            this.mTextPath.reset();
            this.mTextPaint.getTextPath(strValueOf, 0, strValueOf.length(), this.mWidth * 0.5f, f4, this.mTextPath);
            canvas.clipOutPath(this.mTextPath);
            canvas.drawCircle(this.mFrame.centerX(), this.mFrame.centerY(), i2, this.mFramePaint);
        } else if (!z && this.mPowerSaveEnabled) {
            float fWidth = this.mFrame.width() / 2.0f;
            RectF rectF2 = this.mFrame;
            float fWidth2 = rectF2.left + ((rectF2.width() - fWidth) / 2.0f);
            RectF rectF3 = this.mFrame;
            float fHeight = rectF3.top + ((rectF3.height() - fWidth) / 2.0f);
            RectF rectF4 = this.mFrame;
            float fWidth3 = rectF4.right - ((rectF4.width() - fWidth) / 2.0f);
            RectF rectF5 = this.mFrame;
            float fHeight2 = rectF5.bottom - ((rectF5.height() - fWidth) / 2.0f);
            RectF rectF6 = this.mPlusFrame;
            if (rectF6.left != fWidth2 || rectF6.top != fHeight || rectF6.right != fWidth3 || rectF6.bottom != fHeight2) {
                rectF6.set(fWidth2, fHeight, fWidth3, fHeight2);
                this.mPlusPath.reset();
                Path path = this.mPlusPath;
                RectF rectF7 = this.mPlusFrame;
                float fWidth4 = rectF7.left + (this.mPlusPoints[0] * rectF7.width());
                RectF rectF8 = this.mPlusFrame;
                path.moveTo(fWidth4, rectF8.top + (this.mPlusPoints[1] * rectF8.height()));
                int i4 = 2;
                while (true) {
                    fArr = this.mPlusPoints;
                    if (i4 >= fArr.length) {
                        break;
                    }
                    Path path2 = this.mPlusPath;
                    RectF rectF9 = this.mPlusFrame;
                    float fWidth5 = rectF9.left + (fArr[i4] * rectF9.width());
                    RectF rectF10 = this.mPlusFrame;
                    path2.lineTo(fWidth5, rectF10.top + (this.mPlusPoints[i4 + 1] * rectF10.height()));
                    i4 += 2;
                }
                Path path3 = this.mPlusPath;
                RectF rectF11 = this.mPlusFrame;
                float fWidth6 = rectF11.left + (fArr[0] * rectF11.width());
                RectF rectF12 = this.mPlusFrame;
                path3.lineTo(fWidth6, rectF12.top + (this.mPlusPoints[1] * rectF12.height()));
            }
            canvas.clipOutPath(this.mPlusPath);
            canvas.drawPath(this.mPlusPath, this.mPlusPaint);
            canvas.drawArc(this.mFrame, 270.0f, 360.0f, false, this.mPowersavePaint);
        }
        float f5 = i2;
        canvas.drawCircle(this.mFrame.centerX(), this.mFrame.centerY(), f5, this.mFramePaint);
        if (i > 0) {
            canvas.drawCircle(this.mFrame.centerX(), this.mFrame.centerY(), f5 * f, this.mBatteryPaint);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mFramePaint.setColorFilter(colorFilter);
        this.mBatteryPaint.setColorFilter(colorFilter);
        this.mWarningTextPaint.setColorFilter(colorFilter);
        this.mBoltPaint.setColorFilter(colorFilter);
        this.mPlusPaint.setColorFilter(colorFilter);
    }
}
