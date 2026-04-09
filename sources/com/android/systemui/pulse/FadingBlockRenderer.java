package com.android.systemui.pulse;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.TypedValue;

/* loaded from: classes.dex */
public class FadingBlockRenderer extends Renderer {
    private Canvas mCanvas;
    private Bitmap mCanvasBitmap;
    private boolean mCenterMirrored;
    private int mDbFuzzFactor;
    private int mDivisions;
    private FFTAverage[] mFFTAverage;
    private float[] mFFTPoints;
    private final Paint mFadePaint;
    private int mGravity;
    private int mHeight;
    private boolean mLeftInLandscape;
    private final Matrix mMatrix;
    private LegacySettingsObserver mObserver;
    private final Paint mPaint;
    private boolean mSmoothingEnabled;
    private boolean mVertical;
    private boolean mVerticalMirror;
    private int mWidth;

    public FadingBlockRenderer(Context context, Handler handler, PulseView pulseView, ColorController colorController) {
        super(context, handler, pulseView, colorController);
        this.mObserver = new LegacySettingsObserver(handler);
        Paint paint = new Paint();
        this.mPaint = paint;
        Paint paint2 = new Paint();
        this.mFadePaint = paint2;
        paint2.setColor(Color.argb(200, 255, 255, 255));
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        this.mMatrix = new Matrix();
        this.mObserver.updateSettings();
        paint.setAntiAlias(true);
        onSizeChanged(0, 0, 0, 0);
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
        char c;
        int i = this.mKeyguardShowing ? this.mDbFuzzFactor * 4 : this.mDbFuzzFactor;
        if (bArr != null) {
            float[] fArr = this.mFFTPoints;
            if (fArr == null || fArr.length < bArr.length * 4) {
                this.mFFTPoints = new float[bArr.length * 4];
            }
            int length = bArr.length / this.mDivisions;
            if (this.mSmoothingEnabled) {
                FFTAverage[] fFTAverageArr = this.mFFTAverage;
                if (fFTAverageArr == null || fFTAverageArr.length != length) {
                    setupFFTAverage(length);
                }
            } else {
                this.mFFTAverage = null;
            }
            int i2 = 0;
            while (true) {
                z = this.mCenterMirrored;
                if (i2 >= (z ? length / 2 : length)) {
                    break;
                }
                if (this.mVertical) {
                    float[] fArr2 = this.mFFTPoints;
                    int i3 = i2 * 4;
                    int i4 = this.mDivisions;
                    fArr2[i3 + 1] = i3 * i4;
                    fArr2[i3 + 3] = i3 * i4;
                } else {
                    float[] fArr3 = this.mFFTPoints;
                    int i5 = i2 * 4;
                    int i6 = this.mDivisions;
                    fArr3[i5] = i5 * i6;
                    fArr3[i5 + 2] = i5 * i6;
                }
                int i7 = this.mDivisions;
                byte b = bArr[i7 * i2];
                byte b2 = bArr[(i7 * i2) + 1];
                float f = (b * b) + (b2 * b2);
                int iLog10 = f > 0.0f ? (int) (Math.log10(f) * 10.0d) : 0;
                if (this.mSmoothingEnabled) {
                    iLog10 = this.mFFTAverage[i2].average(iLog10);
                }
                if (this.mVertical) {
                    int i8 = this.mWidth;
                    float f2 = i8;
                    int i9 = this.mGravity;
                    if (i9 == 0) {
                        f2 = i8;
                    } else if (i9 == 1) {
                        f2 = 0.0f;
                    } else if (i9 == 2) {
                        f2 = i8 / 2.0f;
                    }
                    float[] fArr4 = this.mFFTPoints;
                    int i10 = i2 * 4;
                    boolean z2 = this.mLeftInLandscape;
                    fArr4[i10] = z2 ? 0.0f : f2;
                    fArr4[i10 + 2] = z2 ? (iLog10 * i) + 2 : f2 - ((iLog10 * i) + 2);
                } else {
                    int i11 = this.mHeight;
                    float f3 = i11;
                    int i12 = this.mGravity;
                    if (i12 == 0) {
                        f = i11;
                    } else if (i12 != 1) {
                        f = i12 == 2 ? i11 / 2.0f : f3;
                    }
                    float[] fArr5 = this.mFFTPoints;
                    int i13 = i2 * 4;
                    fArr5[i13 + 1] = f;
                    fArr5[i13 + 3] = f - ((iLog10 * i) + 2);
                }
                i2++;
            }
            if (z) {
                while (i2 < length) {
                    int i14 = i2 + 1;
                    if (this.mVertical) {
                        float[] fArr6 = this.mFFTPoints;
                        int i15 = i2 * 4;
                        int i16 = this.mDivisions;
                        fArr6[i15 + 1] = i15 * i16;
                        fArr6[i15 + 3] = i15 * i16;
                    } else {
                        float[] fArr7 = this.mFFTPoints;
                        int i17 = i2 * 4;
                        int i18 = this.mDivisions;
                        fArr7[i17] = i17 * i18;
                        fArr7[i17 + 2] = i17 * i18;
                    }
                    int i19 = this.mDivisions;
                    byte b3 = bArr[i19 * i2];
                    byte b4 = bArr[(i19 * i2) + 1];
                    float f4 = (b3 * b3) + (b4 * b4);
                    int iLog102 = f4 > 0.0f ? (int) (Math.log10(f4) * 10.0d) : 0;
                    if (this.mSmoothingEnabled) {
                        iLog102 = this.mFFTAverage[i2].average(iLog102);
                    }
                    if (this.mVertical) {
                        int i20 = this.mWidth;
                        float f5 = i20;
                        int i21 = this.mGravity;
                        if (i21 == 0) {
                            f5 = i20;
                        } else if (i21 == 1) {
                            f5 = 0.0f;
                        } else if (i21 == 2) {
                            f5 = i20 / 2.0f;
                        }
                        float[] fArr8 = this.mFFTPoints;
                        int i22 = i2 * 4;
                        boolean z3 = this.mLeftInLandscape;
                        fArr8[i22] = z3 ? 0.0f : f5;
                        fArr8[i22 + 2] = z3 ? (iLog102 * i) + 2 : f5 - ((iLog102 * i) + 2);
                        c = 0;
                    } else {
                        int i23 = this.mHeight;
                        float f6 = i23;
                        int i24 = this.mGravity;
                        if (i24 == 0) {
                            f6 = i23;
                        } else if (i24 == 1) {
                            f6 = 0.0f;
                        } else {
                            if (i24 == 2) {
                                c = 0;
                                f6 = i23 / 2.0f;
                            }
                            float[] fArr9 = this.mFFTPoints;
                            int i25 = i2 * 4;
                            fArr9[i25 + 1] = f6;
                            fArr9[i25 + 3] = f6 - ((iLog102 * i) + 2);
                        }
                        c = 0;
                        float[] fArr92 = this.mFFTPoints;
                        int i252 = i2 * 4;
                        fArr92[i252 + 1] = f6;
                        fArr92[i252 + 3] = f6 - ((iLog102 * i) + 2);
                    }
                    i2 = i14;
                }
            }
        }
        Canvas canvas = this.mCanvas;
        if (canvas != null) {
            canvas.drawLines(this.mFFTPoints, this.mPaint);
            this.mCanvas.drawPaint(this.mFadePaint);
        }
        postInvalidate();
    }

    private void setupFFTAverage(int i) {
        this.mFFTAverage = new FFTAverage[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.mFFTAverage[i2] = new FFTAverage();
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
        this.mCanvasBitmap = Bitmap.createBitmap(this.mWidth, height, Bitmap.Config.ARGB_8888);
        this.mCanvas = new Canvas(this.mCanvasBitmap);
    }

    @Override // com.android.systemui.pulse.Renderer
    public void setLeftInLandscape(boolean z) {
        if (this.mLeftInLandscape != z) {
            this.mLeftInLandscape = z;
            onSizeChanged(0, 0, 0, 0);
        }
    }

    @Override // com.android.systemui.pulse.Renderer
    public void destroy() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        this.mColorController.stopLavaLamp();
        this.mCanvasBitmap = null;
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
        this.mPaint.setColor(i);
    }

    @Override // com.android.systemui.pulse.Renderer
    public void draw(Canvas canvas) {
        canvas.scale(1.0f, 1.0f, this.mWidth / 2.0f, this.mHeight / 2.0f);
        canvas.drawBitmap(this.mCanvasBitmap, this.mMatrix, null);
        if (this.mVerticalMirror) {
            if (this.mVertical) {
                canvas.scale(-1.0f, 1.0f, this.mWidth / 2.0f, this.mHeight / 2.0f);
            } else {
                canvas.scale(1.0f, -1.0f, this.mWidth / 2.0f, this.mHeight / 2.0f);
            }
            canvas.drawBitmap(this.mCanvasBitmap, this.mMatrix, null);
        }
    }

    private class LegacySettingsObserver extends ContentObserver {
        public LegacySettingsObserver(Handler handler) {
            super(handler);
            register();
        }

        void register() {
            ContentResolver contentResolver = FadingBlockRenderer.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_custom_dimen"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_custom_div"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_filled_block_size"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_empty_block_size"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("pulse_custom_fudge_factor"), false, this, -1);
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
            ContentResolver contentResolver = FadingBlockRenderer.this.mContext.getContentResolver();
            Resources resources = FadingBlockRenderer.this.mContext.getResources();
            int intForUser = Settings.Secure.getIntForUser(contentResolver, "pulse_empty_block_size", 1, -2);
            int intForUser2 = Settings.Secure.getIntForUser(contentResolver, "pulse_custom_dimen", 14, -2);
            int intForUser3 = Settings.Secure.getIntForUser(contentResolver, "pulse_custom_div", 16, -2);
            int intForUser4 = Settings.Secure.getIntForUser(contentResolver, "pulse_custom_fudge_factor", 5, -2);
            int limitedDimenValue = FadingBlockRenderer.getLimitedDimenValue(Settings.Secure.getIntForUser(contentResolver, "pulse_filled_block_size", 4, -2), 4, 8, resources);
            int limitedDimenValue2 = FadingBlockRenderer.getLimitedDimenValue(intForUser, 0, 4, resources);
            FadingBlockRenderer.this.mPaint.setPathEffect(null);
            FadingBlockRenderer.this.mPaint.setPathEffect(new DashPathEffect(new float[]{limitedDimenValue, limitedDimenValue2}, 0.0f));
            FadingBlockRenderer.this.mPaint.setStrokeWidth(FadingBlockRenderer.getLimitedDimenValue(intForUser2, 1, 30, resources));
            FadingBlockRenderer.this.mDivisions = FadingBlockRenderer.validateDivision(intForUser3);
            FadingBlockRenderer.this.mDbFuzzFactor = Math.max(2, Math.min(6, intForUser4));
            FadingBlockRenderer.this.mSmoothingEnabled = Settings.Secure.getIntForUser(contentResolver, "pulse_smoothing_enabled", 0, -2) == 1;
            FadingBlockRenderer.this.mCenterMirrored = Settings.Secure.getIntForUser(contentResolver, "visualizer_center_mirrored", 0, -2) == 1;
            FadingBlockRenderer.this.mVerticalMirror = Settings.Secure.getIntForUser(contentResolver, "pulse_vertical_mirror", 0, -2) == 1;
            FadingBlockRenderer.this.mGravity = Settings.Secure.getIntForUser(contentResolver, "pulse_custom_gravity", 0, -2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getLimitedDimenValue(int i, int i2, int i3, Resources resources) {
        return (int) TypedValue.applyDimension(1, Math.max(i2, Math.min(i3, i)), resources.getDisplayMetrics());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int validateDivision(int i) {
        if (i % 2 != 0) {
            i = 16;
        }
        return Math.max(2, Math.min(44, i));
    }
}
