package com.android.keyguard.clock;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.R;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class BinaryClock extends View {
    private Paint mAmbienEmptyDotPaint;
    private Paint mAmbientDotPaint;
    private final Calendar mCalendar;
    private String mDescFormat;
    private Paint mDotPaint;
    private int mDotSize;
    private int[][] mDots;
    private Paint mEmptyDotPaint;
    private int mHour;
    private boolean mIsAmbientDisplay;
    private int mMinutes;
    private TimeZone mTimeZone;

    public BinaryClock(Context context) {
        this(context, null);
    }

    public BinaryClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BinaryClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCalendar = Calendar.getInstance(TimeZone.getDefault());
        this.mDots = (int[][]) Array.newInstance((Class<?>) int.class, 4, 4);
        Resources resources = context.getResources();
        Paint paint = new Paint();
        this.mDotPaint = paint;
        paint.setAntiAlias(true);
        this.mDotPaint.setStyle(Paint.Style.FILL);
        this.mDotPaint.setColor(resources.getColor(R.color.binary_clock_dot_color));
        Paint paint2 = new Paint();
        this.mEmptyDotPaint = paint2;
        paint2.setAntiAlias(true);
        this.mEmptyDotPaint.setStyle(Paint.Style.STROKE);
        this.mEmptyDotPaint.setColor(resources.getColor(R.color.binary_clock_empty_dot_color));
        Paint paint3 = new Paint();
        this.mAmbientDotPaint = paint3;
        paint3.setAntiAlias(true);
        this.mAmbientDotPaint.setStyle(Paint.Style.FILL);
        this.mAmbientDotPaint.setColor(resources.getColor(R.color.binary_clock_ambient_dot_color));
        Paint paint4 = new Paint();
        this.mAmbienEmptyDotPaint = paint4;
        paint4.setAntiAlias(true);
        this.mAmbienEmptyDotPaint.setStyle(Paint.Style.STROKE);
        this.mAmbienEmptyDotPaint.setColor(resources.getColor(R.color.binary_clock_ambient_empty_dot_color));
        this.mDescFormat = ((SimpleDateFormat) DateFormat.getTimeFormat(context)).toLocalizedPattern();
        onDensityOrFontScaleChanged();
    }

    public void onDensityOrFontScaleChanged() {
        this.mDotSize = getContext().getResources().getDimensionPixelSize(R.dimen.binary_clock_dot_size);
        Paint paint = this.mDotPaint;
        int i = R.dimen.binary_clock_stroke_width;
        paint.setStrokeWidth(r0.getDimensionPixelSize(i));
        this.mEmptyDotPaint.setStrokeWidth(r0.getDimensionPixelSize(i));
        this.mAmbientDotPaint.setStrokeWidth(r0.getDimensionPixelSize(i));
        this.mAmbienEmptyDotPaint.setStrokeWidth(r0.getDimensionPixelSize(i));
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Calendar calendar = this.mCalendar;
        TimeZone timeZone = this.mTimeZone;
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        calendar.setTimeZone(timeZone);
        onTimeChanged();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() / 4;
        int height = getHeight() / 4;
        int i = height / 2;
        for (int i2 = 3; i2 >= 0; i2--) {
            int i3 = width / 2;
            for (int i4 = 0; i4 < 4; i4++) {
                if (i2 < 2 || i4 != 0) {
                    if (this.mDots[i4][i2] == 1) {
                        canvas.drawCircle(i3, i, this.mDotSize, this.mIsAmbientDisplay ? this.mAmbientDotPaint : this.mDotPaint);
                    } else {
                        canvas.drawCircle(i3, i, this.mDotSize, this.mIsAmbientDisplay ? this.mAmbienEmptyDotPaint : this.mEmptyDotPaint);
                    }
                }
                i3 += width;
            }
            i += height;
        }
    }

    private void calculateDotMatrix() {
        int i = this.mHour;
        int i2 = i >= 10 ? i / 10 : 0;
        int i3 = i - (i2 * 10);
        int i4 = this.mMinutes;
        int i5 = i4 >= 10 ? i4 / 10 : 0;
        int i6 = i4 - (i5 * 10);
        this.mDots = (int[][]) Array.newInstance((Class<?>) int.class, 4, 4);
        if (i2 != 0) {
            String binaryString = Integer.toBinaryString(i2);
            for (int i7 = 0; i7 < binaryString.length(); i7++) {
                this.mDots[0][(binaryString.length() - 1) - i7] = binaryString.charAt(i7) == '1' ? 1 : 0;
            }
        }
        if (i3 != 0) {
            String binaryString2 = Integer.toBinaryString(i3);
            for (int i8 = 0; i8 < binaryString2.length(); i8++) {
                this.mDots[1][(binaryString2.length() - 1) - i8] = binaryString2.charAt(i8) == '1' ? 1 : 0;
            }
        }
        if (i5 != 0) {
            String binaryString3 = Integer.toBinaryString(i5);
            for (int i9 = 0; i9 < binaryString3.length(); i9++) {
                this.mDots[2][(binaryString3.length() - 1) - i9] = binaryString3.charAt(i9) == '1' ? 1 : 0;
            }
        }
        if (i6 != 0) {
            String binaryString4 = Integer.toBinaryString(i6);
            for (int i10 = 0; i10 < binaryString4.length(); i10++) {
                this.mDots[3][(binaryString4.length() - 1) - i10] = binaryString4.charAt(i10) == '1' ? 1 : 0;
            }
        }
    }

    public void onTimeChanged() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        this.mHour = this.mCalendar.get(DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser()) ? 11 : 10);
        this.mMinutes = this.mCalendar.get(12);
        setContentDescription(DateFormat.format(this.mDescFormat, this.mCalendar));
        calculateDotMatrix();
        invalidate();
    }

    public void onTimeZoneChanged(TimeZone timeZone) {
        this.mTimeZone = timeZone;
        this.mCalendar.setTimeZone(timeZone);
    }

    public void setDark(boolean z) {
        if (this.mIsAmbientDisplay != z) {
            this.mIsAmbientDisplay = z;
            invalidate();
        }
    }

    public void setTintColor(int i) {
        this.mDotPaint.setColor(i);
        this.mEmptyDotPaint.setColor(i);
        invalidate();
    }
}
