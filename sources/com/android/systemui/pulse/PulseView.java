package com.android.systemui.pulse;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;

/* loaded from: classes.dex */
public class PulseView extends View {
    private final PulseControllerImpl mPulse;

    public PulseView(Context context, PulseControllerImpl pulseControllerImpl) {
        super(context);
        this.mPulse = pulseControllerImpl;
        setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        setWillNotDraw(false);
        setTag("PulseView");
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mPulse.onSizeChanged(i, i2, i3, i4);
        super.onSizeChanged(i, i2, i3, i4);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.mPulse.onDraw(canvas);
        super.onDraw(canvas);
    }
}
