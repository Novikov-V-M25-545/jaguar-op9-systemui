package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import com.android.settingslib.Utils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class BarTransitions {
    private boolean mAlwaysOpaque = false;
    protected final BarBackgroundDrawable mBarBackground;
    private int mMode;
    private final String mTag;
    private final View mView;

    protected boolean isLightsOut(int i) {
        return i == 3 || i == 6;
    }

    public BarTransitions(View view, int i) {
        this.mTag = "BarTransitions." + view.getClass().getSimpleName();
        this.mView = view;
        BarBackgroundDrawable barBackgroundDrawable = new BarBackgroundDrawable(view.getContext(), i);
        this.mBarBackground = barBackgroundDrawable;
        view.setBackground(barBackgroundDrawable);
    }

    public int getMode() {
        return this.mMode;
    }

    public BarBackgroundDrawable getBackground() {
        return this.mBarBackground;
    }

    public boolean isAlwaysOpaque() {
        return this.mAlwaysOpaque;
    }

    public void transitionTo(int i, boolean z) {
        if (isAlwaysOpaque() && (i == 1 || i == 2 || i == 0)) {
            i = 4;
        }
        if (isAlwaysOpaque() && i == 6) {
            i = 3;
        }
        int i2 = this.mMode;
        if (i2 == i) {
            return;
        }
        this.mMode = i;
        onTransition(i2, i, z);
    }

    protected void onTransition(int i, int i2, boolean z) {
        applyModeBackground(i, i2, z);
    }

    protected void applyModeBackground(int i, int i2, boolean z) {
        this.mBarBackground.applyModeBackground(i, i2, z);
    }

    public static String modeToString(int i) {
        if (i == 4) {
            return "MODE_OPAQUE";
        }
        if (i == 1) {
            return "MODE_SEMI_TRANSPARENT";
        }
        if (i == 2) {
            return "MODE_TRANSLUCENT";
        }
        if (i == 3) {
            return "MODE_LIGHTS_OUT";
        }
        if (i == 0) {
            return "MODE_TRANSPARENT";
        }
        if (i == 5) {
            return "MODE_WARNING";
        }
        if (i == 6) {
            return "MODE_LIGHTS_OUT_TRANSPARENT";
        }
        throw new IllegalArgumentException("Unknown mode " + i);
    }

    public void finishAnimations() {
        this.mBarBackground.finishAnimation();
    }

    protected static class BarBackgroundDrawable extends Drawable {
        private boolean mAnimating;
        private int mColor;
        private Integer mColorOverride;
        private int mColorStart;
        private long mEndTime;
        private Rect mFrame;
        private final Drawable mGradient;
        private int mGradientAlpha;
        private int mGradientAlphaStart;
        private final int mOpaque;
        private final int mSemiTransparent;
        private long mStartTime;
        private PorterDuffColorFilter mTintFilter;
        private final int mTransparent;
        private final int mWarning;
        private int mMode = -1;
        private Paint mPaint = new Paint();

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        public BarBackgroundDrawable(Context context, int i) {
            context.getResources();
            this.mOpaque = context.getColor(R.color.system_bar_background_opaque);
            this.mSemiTransparent = context.getColor(android.R.color.keyguard_avatar_frame_shadow_color);
            this.mTransparent = context.getColor(R.color.system_bar_background_transparent);
            this.mWarning = Utils.getColorAttrDefaultColor(context, android.R.attr.colorError);
            this.mGradient = context.getDrawable(i);
        }

        public void setFrame(Rect rect) {
            this.mFrame = rect;
        }

        @Override // android.graphics.drawable.Drawable
        public void setTint(int i) {
            PorterDuffColorFilter porterDuffColorFilter = this.mTintFilter;
            PorterDuff.Mode mode = porterDuffColorFilter == null ? PorterDuff.Mode.SRC_IN : porterDuffColorFilter.getMode();
            PorterDuffColorFilter porterDuffColorFilter2 = this.mTintFilter;
            if (porterDuffColorFilter2 == null || porterDuffColorFilter2.getColor() != i) {
                this.mTintFilter = new PorterDuffColorFilter(i, mode);
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintMode(PorterDuff.Mode mode) {
            PorterDuffColorFilter porterDuffColorFilter = this.mTintFilter;
            int color = porterDuffColorFilter == null ? 0 : porterDuffColorFilter.getColor();
            PorterDuffColorFilter porterDuffColorFilter2 = this.mTintFilter;
            if (porterDuffColorFilter2 == null || porterDuffColorFilter2.getMode() != mode) {
                this.mTintFilter = new PorterDuffColorFilter(color, mode);
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            super.onBoundsChange(rect);
            this.mGradient.setBounds(rect);
        }

        public void setColorOverride(Integer num) {
            if (this.mColorOverride == num) {
                return;
            }
            this.mColorOverride = num;
            invalidateSelf();
        }

        public void applyModeBackground(int i, int i2, boolean z) {
            if (this.mMode == i2) {
                return;
            }
            this.mMode = i2;
            this.mAnimating = z;
            if (z) {
                long jElapsedRealtime = SystemClock.elapsedRealtime();
                this.mStartTime = jElapsedRealtime;
                this.mEndTime = jElapsedRealtime + 200;
                this.mGradientAlphaStart = this.mGradientAlpha;
                this.mColorStart = this.mColor;
            }
            invalidateSelf();
        }

        public void finishAnimation() {
            if (this.mAnimating) {
                this.mAnimating = false;
                invalidateSelf();
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int iIntValue;
            int i = this.mMode;
            if (i == 5) {
                iIntValue = this.mWarning;
            } else if (i == 2 || i == 1) {
                iIntValue = this.mSemiTransparent;
            } else if (i == 0 || i == 6) {
                iIntValue = this.mTransparent;
            } else {
                iIntValue = this.mOpaque;
            }
            Integer num = this.mColorOverride;
            if (num != null) {
                iIntValue = num.intValue();
            }
            if (!this.mAnimating) {
                this.mColor = iIntValue;
                this.mGradientAlpha = 0;
            } else {
                if (SystemClock.elapsedRealtime() >= this.mEndTime) {
                    this.mAnimating = false;
                    this.mColor = iIntValue;
                    this.mGradientAlpha = 0;
                } else {
                    long j = this.mStartTime;
                    float fMax = Math.max(0.0f, Math.min(Interpolators.LINEAR.getInterpolation((r3 - j) / (r5 - j)), 1.0f));
                    float f = 1.0f - fMax;
                    this.mGradientAlpha = (int) ((0 * fMax) + (this.mGradientAlphaStart * f));
                    this.mColor = Color.argb((int) ((Color.alpha(iIntValue) * fMax) + (Color.alpha(this.mColorStart) * f)), (int) ((Color.red(iIntValue) * fMax) + (Color.red(this.mColorStart) * f)), (int) ((Color.green(iIntValue) * fMax) + (Color.green(this.mColorStart) * f)), (int) ((fMax * Color.blue(iIntValue)) + (Color.blue(this.mColorStart) * f)));
                }
            }
            int i2 = this.mGradientAlpha;
            if (i2 > 0) {
                this.mGradient.setAlpha(i2);
                this.mGradient.draw(canvas);
            }
            if (Color.alpha(this.mColor) > 0) {
                this.mPaint.setColor(this.mColor);
                PorterDuffColorFilter porterDuffColorFilter = this.mTintFilter;
                if (porterDuffColorFilter != null) {
                    this.mPaint.setColorFilter(porterDuffColorFilter);
                }
                Rect rect = this.mFrame;
                if (rect != null) {
                    canvas.drawRect(rect, this.mPaint);
                } else {
                    canvas.drawPaint(this.mPaint);
                }
            }
            if (this.mAnimating) {
                invalidateSelf();
            }
        }
    }
}
