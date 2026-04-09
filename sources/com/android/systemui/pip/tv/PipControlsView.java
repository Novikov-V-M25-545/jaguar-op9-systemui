package com.android.systemui.pip.tv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class PipControlsView extends LinearLayout {
    public PipControlsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.tv_pip_controls, this);
        setOrientation(0);
        setGravity(49);
    }

    PipControlButtonView getFullButtonView() {
        return (PipControlButtonView) findViewById(R.id.full_button);
    }

    PipControlButtonView getCloseButtonView() {
        return (PipControlButtonView) findViewById(R.id.close_button);
    }

    PipControlButtonView getPlayPauseButtonView() {
        return (PipControlButtonView) findViewById(R.id.play_pause_button);
    }
}
