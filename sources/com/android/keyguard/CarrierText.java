package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.CarrierTextController;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import java.util.Locale;

/* loaded from: classes.dex */
public class CarrierText extends TextView {
    private static CharSequence mSeparator;
    private CarrierTextController.CarrierTextCallback mCarrierTextCallback;
    private CarrierTextController mCarrierTextController;
    private boolean mShouldMarquee;
    private boolean mShowAirplaneMode;
    private boolean mShowMissingSim;

    public CarrierText(Context context) {
        this(context, null);
    }

    public CarrierText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCarrierTextCallback = new CarrierTextController.CarrierTextCallback() { // from class: com.android.keyguard.CarrierText.1
            @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
            public void updateCarrierInfo(CarrierTextController.CarrierTextCallbackInfo carrierTextCallbackInfo) {
                CarrierText.this.setText(carrierTextCallbackInfo.carrierText);
            }

            @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
            public void startedGoingToSleep() {
                CarrierText.this.setSelected(false);
            }

            @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
            public void finishedWakingUp() {
                CarrierText.this.setSelected(true);
            }
        };
        TypedArray typedArrayObtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.CarrierText, 0, 0);
        try {
            boolean z = typedArrayObtainStyledAttributes.getBoolean(R.styleable.CarrierText_allCaps, false);
            this.mShowAirplaneMode = typedArrayObtainStyledAttributes.getBoolean(R.styleable.CarrierText_showAirplaneMode, false);
            this.mShowMissingSim = typedArrayObtainStyledAttributes.getBoolean(R.styleable.CarrierText_showMissingSim, false);
            typedArrayObtainStyledAttributes.recycle();
            setTransformationMethod(new CarrierTextTransformationMethod(((TextView) this).mContext, z));
        } catch (Throwable th) {
            typedArrayObtainStyledAttributes.recycle();
            throw th;
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        String string = getResources().getString(android.R.string.fp_power_button_bp_negative_button);
        mSeparator = string;
        this.mCarrierTextController = new CarrierTextController(((TextView) this).mContext, string, this.mShowAirplaneMode, this.mShowMissingSim);
        boolean zIsDeviceInteractive = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive();
        this.mShouldMarquee = zIsDeviceInteractive;
        setSelected(zIsDeviceInteractive);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCarrierTextController.setListening(this.mCarrierTextCallback);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCarrierTextController.setListening(null);
    }

    @Override // android.widget.TextView, android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (i == 0) {
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
        } else {
            setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    private class CarrierTextTransformationMethod extends SingleLineTransformationMethod {
        private final boolean mAllCaps;
        private final Locale mLocale;

        public CarrierTextTransformationMethod(Context context, boolean z) {
            this.mLocale = context.getResources().getConfiguration().locale;
            this.mAllCaps = z;
        }

        @Override // android.text.method.ReplacementTransformationMethod, android.text.method.TransformationMethod
        public CharSequence getTransformation(CharSequence charSequence, View view) {
            CharSequence transformation = super.getTransformation(charSequence, view);
            return (!this.mAllCaps || transformation == null) ? transformation : transformation.toString().toUpperCase(this.mLocale);
        }
    }
}
