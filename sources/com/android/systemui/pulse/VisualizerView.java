package com.android.systemui.pulse;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/* loaded from: classes.dex */
public class VisualizerView extends FrameLayout {
    private boolean mAttached;

    public VisualizerView(Context context) {
        super(context);
    }

    public VisualizerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public VisualizerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        this.mAttached = true;
        super.onAttachedToWindow();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        this.mAttached = false;
        super.onDetachedFromWindow();
    }

    public boolean isAttached() {
        return this.mAttached;
    }
}
