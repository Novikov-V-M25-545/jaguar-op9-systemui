package com.android.keyguard.clock;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.doze.util.BurnInHelperKt;

/* loaded from: classes.dex */
public class ClockLayout extends FrameLayout {
    private View mAnalogClock;
    private int mBurnInPreventionOffsetX;
    private int mBurnInPreventionOffsetY;
    private float mDarkAmount;
    private View mTypeClock;

    public ClockLayout(Context context) {
        this(context, null);
    }

    public ClockLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ClockLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAnalogClock = findViewById(R.id.analog_clock);
        this.mTypeClock = findViewById(R.id.type_clock);
        Resources resources = getResources();
        this.mBurnInPreventionOffsetX = resources.getDimensionPixelSize(R.dimen.burn_in_prevention_offset_x);
        this.mBurnInPreventionOffsetY = resources.getDimensionPixelSize(R.dimen.burn_in_prevention_offset_y);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        positionChildren();
    }

    void onTimeChanged() {
        positionChildren();
    }

    void setDarkAmount(float f) {
        this.mDarkAmount = f;
        positionChildren();
    }

    private void positionChildren() {
        float fLerp = MathUtils.lerp(0.0f, BurnInHelperKt.getBurnInOffset(this.mBurnInPreventionOffsetX * 2, true) - this.mBurnInPreventionOffsetX, this.mDarkAmount);
        float fLerp2 = MathUtils.lerp(0.0f, BurnInHelperKt.getBurnInOffset(this.mBurnInPreventionOffsetY * 2, false) - (this.mBurnInPreventionOffsetY * 0.5f), this.mDarkAmount);
        View view = this.mAnalogClock;
        if (view != null) {
            view.setX(Math.max(0.0f, (getWidth() - this.mAnalogClock.getWidth()) * 0.5f) + (fLerp * 3.0f));
            this.mAnalogClock.setY(Math.max(0.0f, (getHeight() - this.mAnalogClock.getHeight()) * 0.5f) + (3.0f * fLerp2));
        }
        View view2 = this.mTypeClock;
        if (view2 != null) {
            view2.setX(fLerp);
            this.mTypeClock.setY((getHeight() * 0.2f) + fLerp2);
        }
    }
}
