package com.android.systemui.pip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceControl;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.ConfigurationController;

/* loaded from: classes.dex */
public class PipSurfaceTransactionHelper implements ConfigurationController.ConfigurationListener {
    private final Context mContext;
    private int mCornerRadius;
    private final boolean mEnableCornerRadius;
    private final Matrix mTmpTransform = new Matrix();
    private final float[] mTmpFloat9 = new float[9];
    private final RectF mTmpSourceRectF = new RectF();
    private final RectF mTmpDestinationRectF = new RectF();
    private final Rect mTmpDestinationRect = new Rect();

    interface SurfaceControlTransactionFactory {
        SurfaceControl.Transaction getTransaction();
    }

    public PipSurfaceTransactionHelper(Context context, ConfigurationController configurationController) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mEnableCornerRadius = resources.getBoolean(R.bool.config_pipEnableRoundCorner);
        configurationController.addCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        this.mCornerRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.pip_corner_radius);
    }

    PipSurfaceTransactionHelper alpha(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, float f) {
        transaction.setAlpha(surfaceControl, f);
        return this;
    }

    PipSurfaceTransactionHelper crop(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect) {
        transaction.setWindowCrop(surfaceControl, rect.width(), rect.height()).setPosition(surfaceControl, rect.left, rect.top);
        return this;
    }

    PipSurfaceTransactionHelper scale(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect, Rect rect2) {
        this.mTmpSourceRectF.set(rect);
        this.mTmpDestinationRectF.set(rect2);
        this.mTmpTransform.setRectToRect(this.mTmpSourceRectF, this.mTmpDestinationRectF, Matrix.ScaleToFit.FILL);
        SurfaceControl.Transaction matrix = transaction.setMatrix(surfaceControl, this.mTmpTransform, this.mTmpFloat9);
        RectF rectF = this.mTmpDestinationRectF;
        matrix.setPosition(surfaceControl, rectF.left, rectF.top);
        return this;
    }

    PipSurfaceTransactionHelper scaleAndCrop(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect, Rect rect2, Rect rect3) {
        float fHeight;
        int iHeight;
        this.mTmpSourceRectF.set(rect);
        this.mTmpDestinationRect.set(rect);
        this.mTmpDestinationRect.inset(rect3);
        if (rect.width() <= rect.height()) {
            fHeight = rect2.width();
            iHeight = rect.width();
        } else {
            fHeight = rect2.height();
            iHeight = rect.height();
        }
        float f = fHeight / iHeight;
        this.mTmpTransform.setScale(f, f);
        transaction.setMatrix(surfaceControl, this.mTmpTransform, this.mTmpFloat9).setWindowCrop(surfaceControl, this.mTmpDestinationRect).setPosition(surfaceControl, rect2.left - (rect3.left * f), rect2.top - (rect3.top * f));
        return this;
    }

    PipSurfaceTransactionHelper resetScale(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect) {
        transaction.setMatrix(surfaceControl, Matrix.IDENTITY_MATRIX, this.mTmpFloat9).setPosition(surfaceControl, rect.left, rect.top);
        return this;
    }

    PipSurfaceTransactionHelper round(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, boolean z) {
        if (this.mEnableCornerRadius) {
            transaction.setCornerRadius(surfaceControl, z ? this.mCornerRadius : 0.0f);
        }
        return this;
    }
}
