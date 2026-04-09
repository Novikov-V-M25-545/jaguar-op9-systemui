package com.android.settingslib.graph;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.PathParser;
import com.android.settingslib.R$array;
import com.android.settingslib.R$color;
import com.android.settingslib.Utils;

/* loaded from: classes.dex */
public class ThemedBatteryDrawable extends Drawable {
    private boolean charging;
    private int[] colorLevels;
    private int criticalLevel;
    private boolean dualTone;
    private final Paint dualToneBackgroundFill;
    private final Paint errorPaint;
    private final Paint fillColorStrokePaint;
    private final Paint fillColorStrokeProtection;
    private final Paint fillPaint;
    private int intrinsicHeight;
    private int intrinsicWidth;
    private boolean invertFillIcon;
    private int level;
    private final Context mContext;
    private boolean powerSaveEnabled;
    private boolean showPercent;
    private final Paint textPaint;
    private final Path boltPath = new Path();
    private int fillColor = -65281;
    private final Path fillMask = new Path();
    private final RectF fillRect = new RectF();
    private int levelColor = -65281;
    private final Path levelPath = new Path();
    private final RectF levelRect = new RectF();
    private final Rect padding = new Rect();
    private final Path errorPerimeterPath = new Path();
    private final Path perimeterPath = new Path();
    private final Matrix scaleMatrix = new Matrix();
    private final Path scaledBolt = new Path();
    private final Path scaledFill = new Path();
    private final Path scaledErrorPerimeter = new Path();
    private final Path scaledPerimeter = new Path();
    private final Path plusPath = new Path();
    private final Path scaledPlus = new Path();
    private final Path unifiedPath = new Path();
    private final Path textPath = new Path();
    private final RectF iconRect = new RectF();
    private final float mWidthDp = 12.0f;
    private final float mHeightDp = 20.0f;

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -1;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    public ThemedBatteryDrawable(Context context, int i) throws Resources.NotFoundException {
        this.mContext = context;
        float f = context.getResources().getDisplayMetrics().density;
        this.intrinsicHeight = (int) (20.0f * f);
        this.intrinsicWidth = (int) (f * 12.0f);
        Resources resources = context.getResources();
        if (Settings.System.getIntForUser(context.getContentResolver(), "battery_level_colors", 0, -2) == 1) {
            TypedArray typedArrayObtainTypedArray = resources.obtainTypedArray(R$array.corvus_batterymeter_color_levels);
            TypedArray typedArrayObtainTypedArray2 = resources.obtainTypedArray(R$array.corvus_batterymeter_color_values);
            int length = typedArrayObtainTypedArray.length();
            this.colorLevels = new int[length * 7];
            for (int i2 = 0; i2 < length; i2++) {
                int i3 = i2 * 7;
                this.colorLevels[i3] = typedArrayObtainTypedArray.getInt(i2, 0);
                if (typedArrayObtainTypedArray2.getType(i2) == 2) {
                    this.colorLevels[i3 + 1] = Utils.getColorAttrDefaultColor(this.mContext, typedArrayObtainTypedArray2.getThemeAttributeId(i2, 0));
                } else {
                    this.colorLevels[i3 + 1] = typedArrayObtainTypedArray2.getColor(i2, 0);
                }
            }
            typedArrayObtainTypedArray.recycle();
            typedArrayObtainTypedArray2.recycle();
        } else {
            TypedArray typedArrayObtainTypedArray3 = resources.obtainTypedArray(R$array.batterymeter_color_levels);
            TypedArray typedArrayObtainTypedArray4 = resources.obtainTypedArray(R$array.batterymeter_color_values);
            int length2 = typedArrayObtainTypedArray3.length();
            this.colorLevels = new int[length2 * 2];
            for (int i4 = 0; i4 < length2; i4++) {
                int i5 = i4 * 2;
                this.colorLevels[i5] = typedArrayObtainTypedArray3.getInt(i4, 0);
                if (typedArrayObtainTypedArray4.getType(i4) == 2) {
                    this.colorLevels[i5 + 1] = Utils.getColorAttrDefaultColor(this.mContext, typedArrayObtainTypedArray4.getThemeAttributeId(i4, 0));
                } else {
                    this.colorLevels[i5 + 1] = typedArrayObtainTypedArray4.getColor(i4, 0);
                }
            }
            typedArrayObtainTypedArray3.recycle();
            typedArrayObtainTypedArray4.recycle();
        }
        setCriticalLevel(resources.getInteger(R.integer.config_bg_current_drain_types_to_restricted_bucket));
        Paint paint = new Paint(1);
        this.dualToneBackgroundFill = paint;
        paint.setColor(i);
        paint.setAlpha(255);
        paint.setDither(true);
        paint.setStrokeWidth(0.0f);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        Paint paint2 = new Paint(1);
        this.fillColorStrokePaint = paint2;
        paint2.setColor(i);
        paint2.setDither(true);
        paint2.setStrokeWidth(5.0f);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint2.setStrokeMiter(5.0f);
        paint2.setStrokeJoin(Paint.Join.ROUND);
        Paint paint3 = new Paint(1);
        this.fillColorStrokeProtection = paint3;
        paint3.setDither(true);
        paint3.setStrokeWidth(5.0f);
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint3.setStrokeMiter(5.0f);
        paint3.setStrokeJoin(Paint.Join.ROUND);
        Paint paint4 = new Paint(1);
        this.fillPaint = paint4;
        paint4.setColor(i);
        paint4.setAlpha(255);
        paint4.setDither(true);
        paint4.setStrokeWidth(0.0f);
        paint4.setStyle(Paint.Style.FILL_AND_STROKE);
        Paint paint5 = new Paint(1);
        this.textPaint = paint5;
        paint5.setTextAlign(Paint.Align.CENTER);
        paint5.setTypeface(Typeface.create("Roboto", 1));
        Paint paint6 = new Paint(1);
        this.errorPaint = paint6;
        paint6.setColor(Utils.getColorStateListDefaultColor(this.mContext, R$color.batterymeter_plus_color));
        paint6.setAlpha(255);
        paint6.setAlpha(255);
        paint6.setDither(true);
        paint6.setStrokeWidth(0.0f);
        paint6.setStyle(Paint.Style.FILL_AND_STROKE);
        paint6.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        loadPaths();
    }

    public void setCriticalLevel(int i) {
        this.criticalLevel = i;
    }

    public final void setCharging(boolean z) {
        if (this.charging != z) {
            this.charging = z;
            postInvalidate();
        }
    }

    public final boolean getPowerSaveEnabled() {
        return this.powerSaveEnabled;
    }

    public final void setPowerSaveEnabled(boolean z) {
        if (this.powerSaveEnabled != z) {
            this.powerSaveEnabled = z;
            postInvalidate();
        }
    }

    public void setShowPercent(boolean z) {
        if (this.showPercent != z) {
            this.showPercent = z;
            postInvalidate();
        }
    }

    protected void postInvalidate() {
        unscheduleSelf(new Runnable() { // from class: com.android.settingslib.graph.ThemedBatteryDrawable$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.invalidateSelf();
            }
        });
        scheduleSelf(new Runnable() { // from class: com.android.settingslib.graph.ThemedBatteryDrawable$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.invalidateSelf();
            }
        }, 0L);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean z;
        float fHeight;
        float fHeight2;
        boolean z2 = this.level <= 30;
        if (this.charging || this.powerSaveEnabled || !this.showPercent) {
            z = false;
            fHeight = 0.0f;
        } else {
            float fHeight3 = (this.dualTone ? this.iconRect : this.fillRect).height();
            this.textPaint.setColor(getColorForLevel(this.level));
            this.textPaint.setTextSize(fHeight3 * (this.level == 100 ? 0.38f : 0.65f));
            float f = -this.textPaint.getFontMetrics().ascent;
            String strValueOf = String.valueOf(this.level);
            float fWidth = this.fillRect.width() * 0.5f;
            RectF rectF = this.fillRect;
            float f2 = fWidth + rectF.left;
            fHeight = ((rectF.height() + f) * 0.47f) + this.fillRect.top;
            this.textPath.reset();
            this.textPaint.getTextPath(strValueOf, 0, strValueOf.length(), f2, fHeight, this.textPath);
            z = true;
        }
        canvas.saveLayer(null, null);
        this.unifiedPath.reset();
        this.levelPath.reset();
        this.levelRect.set(this.fillRect);
        int i = this.level;
        float f3 = i / 100.0f;
        if (i >= 95) {
            fHeight2 = this.fillRect.top;
        } else {
            RectF rectF2 = this.fillRect;
            fHeight2 = rectF2.top + (rectF2.height() * (1.0f - f3));
        }
        boolean z3 = fHeight2 > fHeight;
        this.levelRect.top = (float) Math.floor(this.dualTone ? this.fillRect.top : fHeight2);
        this.levelPath.addRect(this.levelRect, Path.Direction.CCW);
        this.unifiedPath.addPath(this.scaledPerimeter);
        this.unifiedPath.op(this.levelPath, Path.Op.UNION);
        this.fillPaint.setColor(this.levelColor);
        if (this.charging) {
            if (!this.dualTone || !z2) {
                this.unifiedPath.op(this.scaledBolt, Path.Op.DIFFERENCE);
            }
            if (!this.dualTone && !this.invertFillIcon) {
                canvas.drawPath(this.scaledBolt, this.fillPaint);
            }
        } else if (z) {
            if (!this.dualTone || !z3) {
                this.unifiedPath.op(this.textPath, Path.Op.DIFFERENCE);
            }
            if (!this.dualTone && !this.invertFillIcon) {
                canvas.drawPath(this.textPath, this.fillPaint);
            }
        }
        if (this.dualTone) {
            canvas.drawPath(this.unifiedPath, this.dualToneBackgroundFill);
            canvas.save();
            canvas.clipRect(0.0f, getBounds().bottom - (getBounds().height() * f3), getBounds().right, getBounds().bottom);
            canvas.drawPath(this.unifiedPath, this.fillPaint);
            if (this.charging && z2) {
                canvas.drawPath(this.scaledBolt, this.fillPaint);
            } else if (z && z3) {
                canvas.drawPath(this.textPath, this.fillPaint);
            }
            canvas.restore();
        } else {
            this.fillPaint.setColor(this.fillColor);
            canvas.drawPath(this.unifiedPath, this.fillPaint);
            this.fillPaint.setColor(this.levelColor);
            if (this.level <= 15 && !this.charging) {
                canvas.save();
                canvas.clipPath(this.scaledFill);
                canvas.drawPath(this.levelPath, this.fillPaint);
                canvas.restore();
            }
        }
        if (this.charging) {
            canvas.clipOutPath(this.scaledBolt);
            if (this.invertFillIcon) {
                canvas.drawPath(this.scaledBolt, this.fillColorStrokePaint);
            } else {
                canvas.drawPath(this.scaledBolt, this.fillColorStrokeProtection);
            }
        } else if (this.powerSaveEnabled) {
            canvas.drawPath(this.scaledErrorPerimeter, this.errorPaint);
            if (!this.showPercent) {
                canvas.drawPath(this.scaledPlus, this.errorPaint);
            }
        } else if (z) {
            canvas.clipOutPath(this.textPath);
            if (this.invertFillIcon) {
                canvas.drawPath(this.textPath, this.fillColorStrokePaint);
            } else {
                canvas.drawPath(this.textPath, this.fillColorStrokeProtection);
            }
        }
        canvas.restore();
    }

    protected int batteryColorForLevel(int i) {
        if (this.charging || this.powerSaveEnabled) {
            return this.fillColor;
        }
        return getColorForLevel(i);
    }

    private final int getColorForLevel(int i) {
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = this.colorLevels;
            if (i2 >= iArr.length) {
                return i3;
            }
            int i4 = iArr[i2];
            int i5 = iArr[i2 + 1];
            if (i <= i4) {
                return i2 == iArr.length + (-2) ? this.fillColor : i5;
            }
            i2 += 2;
            i3 = i5;
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.fillPaint.setColorFilter(colorFilter);
        this.fillColorStrokePaint.setColorFilter(colorFilter);
        this.dualToneBackgroundFill.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.intrinsicHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.intrinsicWidth;
    }

    public void setBatteryLevel(int i) {
        boolean z;
        if (this.level != i) {
            this.level = i;
            if (i >= 67) {
                z = true;
            } else {
                z = i <= 33 ? false : this.invertFillIcon;
            }
            this.invertFillIcon = z;
            this.levelColor = batteryColorForLevel(i);
            postInvalidate();
        }
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        updateSize();
    }

    public void setColors(int i, int i2, int i3) {
        if (!this.dualTone) {
            i = i3;
        }
        this.fillColor = i;
        this.fillPaint.setColor(i);
        this.fillColorStrokePaint.setColor(this.fillColor);
        this.dualToneBackgroundFill.setColor(i2);
        this.levelColor = batteryColorForLevel(this.level);
        invalidateSelf();
    }

    private final void updateSize() {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            this.scaleMatrix.setScale(1.0f, 1.0f);
        } else {
            this.scaleMatrix.setScale(bounds.right / 12.0f, bounds.bottom / 20.0f);
        }
        this.perimeterPath.transform(this.scaleMatrix, this.scaledPerimeter);
        this.errorPerimeterPath.transform(this.scaleMatrix, this.scaledErrorPerimeter);
        this.fillMask.transform(this.scaleMatrix, this.scaledFill);
        this.scaledFill.computeBounds(this.fillRect, true);
        this.boltPath.transform(this.scaleMatrix, this.scaledBolt);
        this.plusPath.transform(this.scaleMatrix, this.scaledPlus);
        float fMax = Math.max((bounds.right / 12.0f) * 3.0f, 6.0f);
        this.fillColorStrokePaint.setStrokeWidth(fMax);
        this.fillColorStrokeProtection.setStrokeWidth(fMax);
        this.iconRect.set(bounds);
    }

    private final void loadPaths() throws Resources.NotFoundException {
        this.perimeterPath.set(PathParser.createPathFromPathData(this.mContext.getResources().getString(R.string.color_inversion_feature_name)));
        this.perimeterPath.computeBounds(new RectF(), true);
        this.errorPerimeterPath.set(PathParser.createPathFromPathData(this.mContext.getResources().getString(R.string.close_button_text)));
        this.errorPerimeterPath.computeBounds(new RectF(), true);
        this.fillMask.set(PathParser.createPathFromPathData(this.mContext.getResources().getString(R.string.color_correction_feature_name)));
        this.fillMask.computeBounds(this.fillRect, true);
        this.boltPath.set(PathParser.createPathFromPathData(this.mContext.getResources().getString(R.string.clone_profile_label_badge)));
        this.plusPath.set(PathParser.createPathFromPathData(this.mContext.getResources().getString(R.string.common_last_name_prefixes)));
        this.dualTone = this.mContext.getResources().getBoolean(R.bool.config_appCompatUserAppAspectRatioFullscreenIsEnabled);
    }
}
