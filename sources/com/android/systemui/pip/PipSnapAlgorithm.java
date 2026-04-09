package com.android.systemui.pip;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.Size;

/* loaded from: classes.dex */
public class PipSnapAlgorithm {
    private final Context mContext;
    private final float mDefaultSizePercent;
    private final float mMaxAspectRatioForMinSize;
    private final float mMinAspectRatioForMinSize;

    public PipSnapAlgorithm(Context context) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mDefaultSizePercent = resources.getFloat(R.dimen.chooser_preview_image_max_dimen);
        float f = resources.getFloat(R.dimen.chooser_preview_image_border);
        this.mMaxAspectRatioForMinSize = f;
        this.mMinAspectRatioForMinSize = 1.0f / f;
    }

    public float getSnapFraction(Rect rect, Rect rect2) {
        Rect rect3 = new Rect();
        snapRectToClosestEdge(rect, rect2, rect3);
        float fWidth = (rect3.left - rect2.left) / rect2.width();
        float fHeight = (rect3.top - rect2.top) / rect2.height();
        int i = rect3.top;
        return i == rect2.top ? fWidth : rect3.left == rect2.right ? fHeight + 1.0f : i == rect2.bottom ? (1.0f - fWidth) + 2.0f : (1.0f - fHeight) + 3.0f;
    }

    public void applySnapFraction(Rect rect, Rect rect2, float f) {
        if (f < 1.0f) {
            rect.offsetTo(rect2.left + ((int) (f * rect2.width())), rect2.top);
            return;
        }
        if (f < 2.0f) {
            rect.offsetTo(rect2.right, rect2.top + ((int) ((f - 1.0f) * rect2.height())));
        } else if (f < 3.0f) {
            rect.offsetTo(rect2.left + ((int) ((1.0f - (f - 2.0f)) * rect2.width())), rect2.bottom);
        } else {
            rect.offsetTo(rect2.left, rect2.top + ((int) ((1.0f - (f - 3.0f)) * rect2.height())));
        }
    }

    public void getMovementBounds(Rect rect, Rect rect2, Rect rect3, int i) {
        rect3.set(rect2);
        rect3.right = Math.max(rect2.left, rect2.right - rect.width());
        int iMax = Math.max(rect2.top, rect2.bottom - rect.height());
        rect3.bottom = iMax;
        rect3.bottom = iMax - i;
    }

    /* JADX WARN: Removed duplicated region for block: B:8:0x0038  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public android.util.Size getSizeForAspectRatio(float r2, float r3, int r4, int r5) {
        /*
            r1 = this;
            int r4 = java.lang.Math.min(r4, r5)
            float r4 = (float) r4
            float r5 = r1.mDefaultSizePercent
            float r4 = r4 * r5
            float r3 = java.lang.Math.max(r3, r4)
            int r3 = (int) r3
            float r4 = r1.mMinAspectRatioForMinSize
            int r4 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            r5 = 1065353216(0x3f800000, float:1.0)
            if (r4 <= 0) goto L38
            float r1 = r1.mMaxAspectRatioForMinSize
            int r4 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r4 <= 0) goto L1c
            goto L38
        L1c:
            float r3 = (float) r3
            float r1 = r1 * r3
            float r1 = android.graphics.PointF.length(r1, r3)
            float r1 = r1 * r1
            float r3 = r2 * r2
            float r3 = r3 + r5
            float r1 = r1 / r3
            double r3 = (double) r1
            double r3 = java.lang.Math.sqrt(r3)
            long r3 = java.lang.Math.round(r3)
            int r3 = (int) r3
            float r1 = (float) r3
            float r1 = r1 * r2
            int r1 = java.lang.Math.round(r1)
            goto L49
        L38:
            int r1 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
            if (r1 > 0) goto L43
            float r1 = (float) r3
            float r1 = r1 / r2
            int r1 = java.lang.Math.round(r1)
            goto L4c
        L43:
            float r1 = (float) r3
            float r1 = r1 * r2
            int r1 = java.lang.Math.round(r1)
        L49:
            r0 = r3
            r3 = r1
            r1 = r0
        L4c:
            android.util.Size r2 = new android.util.Size
            r2.<init>(r3, r1)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.pip.PipSnapAlgorithm.getSizeForAspectRatio(float, float, int, int):android.util.Size");
    }

    public Size getSizeForAspectRatio(Size size, float f, float f2) {
        int iRound;
        int iMax = (int) Math.max(f2, Math.min(size.getWidth(), size.getHeight()));
        if (f <= 1.0f) {
            iRound = Math.round(iMax / f);
        } else {
            iRound = iMax;
            iMax = Math.round(iMax * f);
        }
        return new Size(iMax, iRound);
    }

    public void snapRectToClosestEdge(Rect rect, Rect rect2, Rect rect3) {
        int iMax = Math.max(rect2.left, Math.min(rect2.right, rect.left));
        int iMax2 = Math.max(rect2.top, Math.min(rect2.bottom, rect.top));
        rect3.set(rect);
        int iAbs = Math.abs(rect.left - rect2.left);
        int iAbs2 = Math.abs(rect.top - rect2.top);
        int iAbs3 = Math.abs(rect2.right - rect.left);
        int iMin = Math.min(Math.min(iAbs, iAbs3), Math.min(iAbs2, Math.abs(rect2.bottom - rect.top)));
        if (iMin == iAbs) {
            rect3.offsetTo(rect2.left, iMax2);
            return;
        }
        if (iMin == iAbs2) {
            rect3.offsetTo(iMax, rect2.top);
        } else if (iMin == iAbs3) {
            rect3.offsetTo(rect2.right, iMax2);
        } else {
            rect3.offsetTo(iMax, rect2.bottom);
        }
    }
}
