package com.android.systemui.pulse;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import androidx.core.graphics.ColorUtils;

/* loaded from: classes.dex */
public class SolidLineRenderer extends Renderer {
    private boolean mCenterMirrored;
    private int mColor;
    private int mDbFuzzFactor;
    private FFTAverage[] mFFTAverage;
    private float[] mFFTPoints;
    private int mGravity;
    private int mHeight;
    private boolean mLeftInLandscape;
    private final CMRendererObserver mObserver;
    private final Paint mPaint;
    private boolean mSmoothingEnabled;
    private int mUnits;
    private int mUnitsOpacity;
    private ValueAnimator[] mValueAnimators;
    private boolean mVertical;
    private boolean mVerticalMirror;
    private int mWidth;

    public SolidLineRenderer(Context context, Handler handler, PulseView pulseView, ColorController colorController) {
        super(context, handler, pulseView, colorController);
        this.mUnitsOpacity = 255;
        this.mColor = -1;
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        this.mDbFuzzFactor = 5;
        CMRendererObserver cMRendererObserver = new CMRendererObserver(handler);
        this.mObserver = cMRendererObserver;
        cMRendererObserver.updateSettings();
        loadValueAnimators();
    }

    @Override // com.android.systemui.pulse.Renderer
    public void setLeftInLandscape(boolean z) {
        if (this.mLeftInLandscape != z) {
            this.mLeftInLandscape = z;
            onSizeChanged(0, 0, 0, 0);
        }
    }

    private void loadValueAnimators() {
        ValueAnimator[] valueAnimatorArr = this.mValueAnimators;
        if (valueAnimatorArr != null) {
            stopAnimation(valueAnimatorArr.length);
        }
        this.mValueAnimators = new ValueAnimator[this.mUnits];
        boolean z = this.mVertical;
        for (int i = 0; i < this.mUnits; i++) {
            final int i2 = z ? i * 4 : (i * 4) + 1;
            this.mValueAnimators[i] = new ValueAnimator();
            this.mValueAnimators[i].setDuration(128L);
            this.mValueAnimators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.pulse.SolidLineRenderer$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$loadValueAnimators$0(i2, valueAnimator);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadValueAnimators$0(int i, ValueAnimator valueAnimator) {
        this.mFFTPoints[i] = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        postInvalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopAnimation(int i) {
        if (this.mValueAnimators == null) {
            return;
        }
        for (int i2 = 0; i2 < i; i2++) {
            this.mValueAnimators[i2].removeAllUpdateListeners();
            this.mValueAnimators[i2].cancel();
        }
    }

    private void setPortraitPoints() {
        float f = this.mUnits;
        float f2 = this.mWidth / f;
        float f3 = (8.0f * f2) / 9.0f;
        int i = this.mHeight;
        float f4 = i;
        int i2 = this.mGravity;
        if (i2 == 0) {
            f4 = i;
        } else if (i2 == 1) {
            f4 = 0.0f;
        } else if (i2 == 2) {
            f4 = i / 2.0f;
        }
        float f5 = (((f2 - f3) * f) / (f - 1.0f)) + f3;
        this.mPaint.setStrokeWidth(f3);
        for (int i3 = 0; i3 < this.mUnits; i3++) {
            float[] fArr = this.mFFTPoints;
            int i4 = i3 * 4;
            float f6 = (i3 * f5) + (f3 / 2.0f);
            fArr[i4 + 2] = f6;
            fArr[i4] = f6;
            fArr[i4 + 1] = f4;
            fArr[i4 + 3] = f4;
        }
    }

    private void setVerticalPoints() {
        float f = this.mUnits;
        float f2 = this.mHeight / f;
        float f3 = (8.0f * f2) / 9.0f;
        int i = this.mWidth;
        float f4 = i;
        int i2 = this.mGravity;
        if (i2 == 0) {
            f4 = i;
        } else if (i2 == 1) {
            f4 = 0.0f;
        } else if (i2 == 2) {
            f4 = i / 2.0f;
        }
        float f5 = (((f2 - f3) * f) / (f - 1.0f)) + f3;
        this.mPaint.setStrokeWidth(f3);
        for (int i3 = 0; i3 < this.mUnits; i3++) {
            float[] fArr = this.mFFTPoints;
            int i4 = i3 * 4;
            float f6 = (i3 * f5) + (f3 / 2.0f);
            fArr[i4 + 3] = f6;
            fArr[i4 + 1] = f6;
            boolean z = this.mLeftInLandscape;
            fArr[i4] = z ? 0.0f : f4;
            fArr[i4 + 2] = z ? 0.0f : f4;
        }
    }

    @Override // com.android.systemui.pulse.Renderer
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        if (this.mView.getWidth() <= 0 || this.mView.getHeight() <= 0) {
            return;
        }
        this.mWidth = this.mView.getWidth();
        int height = this.mView.getHeight();
        this.mHeight = height;
        boolean z = true;
        if (!this.mKeyguardShowing ? height <= this.mWidth : height >= this.mWidth) {
            z = false;
        }
        this.mVertical = z;
        loadValueAnimators();
        if (this.mVertical) {
            setVerticalPoints();
        } else {
            setPortraitPoints();
        }
    }

    @Override // com.android.systemui.pulse.VisualizerStreamHandler.Listener
    public void onStreamAnalyzed(boolean z) {
        this.mIsValidStream = z;
        if (z) {
            onSizeChanged(0, 0, 0, 0);
            this.mColorController.startLavaLamp();
        }
    }

    @Override // com.android.systemui.pulse.Renderer, com.android.systemui.pulse.VisualizerStreamHandler.Listener
    public void onFFTUpdate(byte[] bArr) {
        boolean z;
        int i;
        int i2;
        int i3 = this.mKeyguardShowing ? this.mDbFuzzFactor * 4 : this.mDbFuzzFactor;
        int i4 = 0;
        while (true) {
            z = this.mCenterMirrored;
            int i5 = this.mUnits;
            if (z) {
                i5 /= 4;
            }
            if (i4 >= i5) {
                break;
            }
            ValueAnimator[] valueAnimatorArr = this.mValueAnimators;
            if (valueAnimatorArr[i4] != null) {
                valueAnimatorArr[i4].cancel();
                int i6 = i4 * 2;
                byte b = bArr[i6 + 2];
                byte b2 = bArr[i6 + 3];
                float f = (b * b) + (b2 * b2);
                int iLog10 = f > 0.0f ? (int) (Math.log10(f) * 10.0d) : 0;
                if (this.mSmoothingEnabled) {
                    if (this.mFFTAverage == null) {
                        setupFFTAverage();
                    }
                    iLog10 = this.mFFTAverage[i4].average(iLog10);
                }
                if (this.mVertical) {
                    if (this.mLeftInLandscape || (i2 = this.mGravity) == 1) {
                        this.mValueAnimators[i4].setFloatValues(this.mFFTPoints[i4 * 4], iLog10 * i3);
                    } else if (i2 == 0 || i2 == 2) {
                        ValueAnimator valueAnimator = this.mValueAnimators[i4];
                        float[] fArr = this.mFFTPoints;
                        valueAnimator.setFloatValues(fArr[i4 * 4], fArr[2] - (iLog10 * i3));
                    }
                } else {
                    int i7 = this.mGravity;
                    if (i7 == 0 || i7 == 2) {
                        ValueAnimator valueAnimator2 = this.mValueAnimators[i4];
                        float[] fArr2 = this.mFFTPoints;
                        valueAnimator2.setFloatValues(fArr2[(i4 * 4) + 1], fArr2[3] - (iLog10 * i3));
                    } else if (i7 == 1) {
                        ValueAnimator valueAnimator3 = this.mValueAnimators[i4];
                        float[] fArr3 = this.mFFTPoints;
                        valueAnimator3.setFloatValues(fArr3[(i4 * 4) + 1], fArr3[3] + (iLog10 * i3));
                    }
                }
                this.mValueAnimators[i4].start();
            }
            i4++;
        }
        if (!z) {
            return;
        }
        while (true) {
            int i8 = this.mUnits;
            if (i4 >= i8) {
                return;
            }
            int i9 = i4 + 1;
            int i10 = i8 - i9;
            ValueAnimator[] valueAnimatorArr2 = this.mValueAnimators;
            if (valueAnimatorArr2[i4] != null) {
                valueAnimatorArr2[i4].cancel();
                int i11 = i10 * 2;
                byte b3 = bArr[i11 + 2];
                byte b4 = bArr[i11 + 3];
                float f2 = (b3 * b3) + (b4 * b4);
                int iLog102 = f2 > 0.0f ? (int) (Math.log10(f2) * 10.0d) : 0;
                if (this.mSmoothingEnabled) {
                    if (this.mFFTAverage == null) {
                        setupFFTAverage();
                    }
                    iLog102 = this.mFFTAverage[i4].average(iLog102);
                }
                if (this.mVertical) {
                    if (this.mLeftInLandscape || (i = this.mGravity) == 1) {
                        this.mValueAnimators[i4].setFloatValues(this.mFFTPoints[i4 * 4], iLog102 * i3);
                    } else if (i == 0 || i == 2) {
                        ValueAnimator valueAnimator4 = this.mValueAnimators[i4];
                        float[] fArr4 = this.mFFTPoints;
                        valueAnimator4.setFloatValues(fArr4[i4 * 4], fArr4[2] - (iLog102 * i3));
                    }
                } else {
                    int i12 = this.mGravity;
                    if (i12 == 0 || i12 == 2) {
                        ValueAnimator valueAnimator5 = this.mValueAnimators[i4];
                        float[] fArr5 = this.mFFTPoints;
                        valueAnimator5.setFloatValues(fArr5[(i4 * 4) + 1], fArr5[3] - (iLog102 * i3));
                    } else if (i12 == 1) {
                        ValueAnimator valueAnimator6 = this.mValueAnimators[i4];
                        float[] fArr6 = this.mFFTPoints;
                        valueAnimator6.setFloatValues(fArr6[(i4 * 4) + 1], fArr6[3] + (iLog102 * i3));
                    }
                }
                this.mValueAnimators[i4].start();
            }
            i4 = i9;
        }
    }

    @Override // com.android.systemui.pulse.Renderer
    public void draw(Canvas canvas) {
        canvas.scale(1.0f, 1.0f, this.mWidth / 2.0f, this.mHeight / 2.0f);
        canvas.drawLines(this.mFFTPoints, this.mPaint);
        if (this.mVerticalMirror) {
            if (this.mVertical) {
                canvas.scale(-1.0f, 1.0f, this.mWidth / 2.0f, this.mHeight / 2.0f);
            } else {
                canvas.scale(1.0f, -1.0f, this.mWidth / 2.0f, this.mHeight / 2.0f);
            }
            canvas.drawLines(this.mFFTPoints, this.mPaint);
        }
    }

    @Override // com.android.systemui.pulse.Renderer
    public void destroy() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        this.mColorController.stopLavaLamp();
    }

    @Override // com.android.systemui.pulse.Renderer
    public void onVisualizerLinkChanged(boolean z) {
        if (z) {
            return;
        }
        this.mColorController.stopLavaLamp();
    }

    @Override // com.android.systemui.pulse.Renderer
    public void onUpdateColor(int i) {
        this.mColor = i;
        this.mPaint.setColor(ColorUtils.setAlphaComponent(i, this.mUnitsOpacity));
    }

    private class CMRendererObserver extends ContentObserver {
        public CMRendererObserver(Handler handler) {
            super(handler);
            register();
        }

        void register() {
            ContentResolver contentResolver = SolidLineRenderer.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_solid_fudge_factor"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_solid_units_count"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_solid_units_opacity"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_smoothing_enabled"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("visualizer_center_mirrored"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_custom_gravity"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_vertical_mirror"), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            updateSettings();
        }

        public void updateSettings() {
            ContentResolver contentResolver = SolidLineRenderer.this.mContext.getContentResolver();
            SolidLineRenderer.this.mDbFuzzFactor = Settings.Secure.getIntForUser(contentResolver, "pulse_solid_fudge_factor", 4, -2);
            SolidLineRenderer.this.mSmoothingEnabled = Settings.Secure.getIntForUser(contentResolver, "pulse_smoothing_enabled", 0, -2) == 1;
            SolidLineRenderer.this.mCenterMirrored = Settings.Secure.getIntForUser(contentResolver, "visualizer_center_mirrored", 0, -2) == 1;
            SolidLineRenderer.this.mVerticalMirror = Settings.Secure.getIntForUser(contentResolver, "pulse_vertical_mirror", 0, -2) == 1;
            SolidLineRenderer.this.mGravity = Settings.Secure.getIntForUser(contentResolver, "pulse_custom_gravity", 0, -2);
            int intForUser = Settings.Secure.getIntForUser(contentResolver, "pulse_solid_units_count", 32, -2);
            if (intForUser != SolidLineRenderer.this.mUnits) {
                SolidLineRenderer solidLineRenderer = SolidLineRenderer.this;
                solidLineRenderer.stopAnimation(solidLineRenderer.mUnits);
                SolidLineRenderer.this.mUnits = intForUser;
                SolidLineRenderer solidLineRenderer2 = SolidLineRenderer.this;
                solidLineRenderer2.mFFTPoints = new float[solidLineRenderer2.mUnits * 4];
                if (SolidLineRenderer.this.mSmoothingEnabled) {
                    SolidLineRenderer.this.setupFFTAverage();
                }
                SolidLineRenderer.this.onSizeChanged(0, 0, 0, 0);
            }
            if (SolidLineRenderer.this.mSmoothingEnabled) {
                if (SolidLineRenderer.this.mFFTAverage == null) {
                    SolidLineRenderer.this.setupFFTAverage();
                }
            } else {
                SolidLineRenderer.this.mFFTAverage = null;
            }
            SolidLineRenderer.this.mUnitsOpacity = Settings.Secure.getIntForUser(contentResolver, "pulse_solid_units_opacity", 200, -2);
            SolidLineRenderer.this.mPaint.setColor(ColorUtils.setAlphaComponent(SolidLineRenderer.this.mColor, SolidLineRenderer.this.mUnitsOpacity));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupFFTAverage() {
        this.mFFTAverage = new FFTAverage[this.mUnits];
        for (int i = 0; i < this.mUnits; i++) {
            this.mFFTAverage[i] = new FFTAverage();
        }
    }
}
