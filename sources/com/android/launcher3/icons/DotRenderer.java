package com.android.launcher3.icons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.util.Log;
import android.view.ViewDebug;
import com.android.launcher3.icons.ShadowGenerator;

/* loaded from: classes.dex */
public class DotRenderer {
    private final Bitmap mBackgroundWithShadow;
    private final float mBitmapOffset;
    private final Paint mCirclePaint = new Paint(3);
    private final float mCircleRadius;
    private final float[] mLeftDotPosition;
    private final float[] mRightDotPosition;

    public static class DrawParams {

        @ViewDebug.ExportedProperty(category = "notification dot", formatToHexString = true)
        public int color;

        @ViewDebug.ExportedProperty(category = "notification dot")
        public Rect iconBounds = new Rect();

        @ViewDebug.ExportedProperty(category = "notification dot")
        public boolean leftAlign;

        @ViewDebug.ExportedProperty(category = "notification dot")
        public float scale;
    }

    public DotRenderer(int i, Path path, int i2) {
        int iRound = Math.round(i * 0.228f);
        ShadowGenerator.Builder builder = new ShadowGenerator.Builder(0);
        builder.ambientShadowAlpha = 88;
        this.mBackgroundWithShadow = builder.setupBlurForSize(iRound).createPill(iRound, iRound);
        this.mCircleRadius = builder.radius;
        this.mBitmapOffset = (-r3.getHeight()) * 0.5f;
        float f = i2;
        this.mLeftDotPosition = getPathPoint(path, f, -1.0f);
        this.mRightDotPosition = getPathPoint(path, f, 1.0f);
    }

    private static float[] getPathPoint(Path path, float f, float f2) {
        float f3 = f / 2.0f;
        float f4 = (f2 * f3) + f3;
        Path path2 = new Path();
        path2.moveTo(f3, f3);
        path2.lineTo((f2 * 1.0f) + f4, 0.0f);
        path2.lineTo(f4, -1.0f);
        path2.close();
        path2.op(path, Path.Op.INTERSECT);
        float[] fArr = new float[2];
        new PathMeasure(path2, false).getPosTan(0.0f, fArr, null);
        fArr[0] = fArr[0] / f;
        fArr[1] = fArr[1] / f;
        return fArr;
    }

    public float[] getLeftDotPosition() {
        return this.mLeftDotPosition;
    }

    public float[] getRightDotPosition() {
        return this.mRightDotPosition;
    }

    public void draw(Canvas canvas, DrawParams drawParams) {
        float fMin;
        if (drawParams == null) {
            Log.e("DotRenderer", "Invalid null argument(s) passed in call to draw.");
            return;
        }
        canvas.save();
        Rect rect = drawParams.iconBounds;
        float[] fArr = drawParams.leftAlign ? this.mLeftDotPosition : this.mRightDotPosition;
        float fWidth = rect.left + (rect.width() * fArr[0]);
        float fHeight = rect.top + (rect.height() * fArr[1]);
        Rect clipBounds = canvas.getClipBounds();
        if (drawParams.leftAlign) {
            fMin = Math.max(0.0f, clipBounds.left - (this.mBitmapOffset + fWidth));
        } else {
            fMin = Math.min(0.0f, clipBounds.right - (fWidth - this.mBitmapOffset));
        }
        canvas.translate(fWidth + fMin, fHeight + Math.max(0.0f, clipBounds.top - (this.mBitmapOffset + fHeight)));
        float f = drawParams.scale;
        canvas.scale(f, f);
        this.mCirclePaint.setColor(-16777216);
        Bitmap bitmap = this.mBackgroundWithShadow;
        float f2 = this.mBitmapOffset;
        canvas.drawBitmap(bitmap, f2, f2, this.mCirclePaint);
        this.mCirclePaint.setColor(drawParams.color);
        canvas.drawCircle(0.0f, 0.0f, this.mCircleRadius, this.mCirclePaint);
        canvas.restore();
    }
}
