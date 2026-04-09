package com.android.systemui.crdroid.batterybar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.systemui.Dependency;
import com.android.systemui.tuner.TunerService;

/* loaded from: classes.dex */
public class BatteryBar extends RelativeLayout implements Animatable, TunerService.Tunable {
    private static final String TAG = BatteryBar.class.getSimpleName();
    private boolean isAnimating;
    private boolean mAttached;
    View mBatteryBar;
    LinearLayout mBatteryBarLayout;
    private boolean mBatteryCharging;
    private int mBatteryLevel;
    private int mBatteryLowColor;
    private boolean mBlendColor;
    private boolean mBlendColorReversed;
    View mCharger;
    LinearLayout mChargerLayout;
    private int mChargingColor;
    private int mChargingLevel;
    private int mColor;
    private final BroadcastReceiver mIntentReceiver;
    int mLocation;
    private boolean mUseChargingColor;
    private boolean shouldAnimateCharging;
    boolean vertical;

    public BatteryBar(Context context, boolean z, int i, boolean z2) {
        this(context, null);
        this.mBatteryLevel = i;
        this.mBatteryCharging = z;
        this.vertical = z2;
    }

    public BatteryBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAttached = false;
        this.mBatteryLevel = 0;
        this.mChargingLevel = -1;
        this.mBatteryCharging = false;
        this.shouldAnimateCharging = true;
        this.isAnimating = false;
        this.vertical = false;
        this.mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.crdroid.batterybar.BatteryBar.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    BatteryBar.this.mBatteryLevel = intent.getIntExtra("level", 0);
                    BatteryBar.this.mBatteryCharging = intent.getIntExtra("status", 0) == 2;
                    if (BatteryBar.this.mBatteryCharging && BatteryBar.this.mBatteryLevel < 100) {
                        BatteryBar.this.start();
                    } else {
                        BatteryBar.this.stop();
                    }
                    BatteryBar batteryBar = BatteryBar.this;
                    batteryBar.setProgress(batteryBar.mBatteryLevel);
                    return;
                }
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    BatteryBar.this.stop();
                } else if ("android.intent.action.SCREEN_ON".equals(action) && BatteryBar.this.mBatteryCharging && BatteryBar.this.mBatteryLevel < 100) {
                    BatteryBar.this.start();
                }
            }
        };
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mAttached) {
            return;
        }
        this.mAttached = true;
        LinearLayout linearLayout = new LinearLayout(((RelativeLayout) this).mContext);
        this.mBatteryBarLayout = linearLayout;
        addView(linearLayout, new RelativeLayout.LayoutParams(-1, -1));
        View view = new View(((RelativeLayout) this).mContext);
        this.mBatteryBar = view;
        this.mBatteryBarLayout.addView(view, new LinearLayout.LayoutParams(-1, -1));
        int i = (int) ((getContext().getResources().getDisplayMetrics().density * 4.0f) + 0.5f);
        LinearLayout linearLayout2 = new LinearLayout(((RelativeLayout) this).mContext);
        this.mChargerLayout = linearLayout2;
        if (this.vertical) {
            addView(linearLayout2, new RelativeLayout.LayoutParams(-1, i));
        } else {
            addView(linearLayout2, new RelativeLayout.LayoutParams(i, -1));
        }
        this.mCharger = new View(((RelativeLayout) this).mContext);
        this.mChargerLayout.setVisibility(8);
        this.mChargerLayout.addView(this.mCharger, new LinearLayout.LayoutParams(-1, -1));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        getContext().registerReceiver(this.mIntentReceiver, intentFilter, null, getHandler());
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:statusbar_battery_bar", "system:statusbar_battery_bar_color", "system:statusbar_battery_bar_charging_color", "system:statusbar_battery_bar_battery_low_color", "system:statusbar_battery_bar_animate", "system:statusbar_battery_bar_enable_charging_color", "system:statusbar_battery_bar_blend_color", "system:statusbar_battery_bar_blend_color_reverse");
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            this.mAttached = false;
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "system:statusbar_battery_bar_enable_charging_color":
                this.mUseChargingColor = TunerService.parseIntegerSwitch(str2, true);
                break;
            case "system:statusbar_battery_bar_color":
                this.mColor = TunerService.parseInteger(str2, -8994524);
                break;
            case "system:statusbar_battery_bar":
                this.mLocation = TunerService.parseInteger(str2, 0);
                break;
            case "system:statusbar_battery_bar_battery_low_color":
                this.mBatteryLowColor = TunerService.parseInteger(str2, -458712);
                break;
            case "system:statusbar_battery_bar_blend_color":
                this.mBlendColor = TunerService.parseIntegerSwitch(str2, true);
                break;
            case "system:statusbar_battery_bar_charging_color":
                this.mChargingColor = TunerService.parseInteger(str2, -14065);
                break;
            case "system:statusbar_battery_bar_blend_color_reverse":
                this.mBlendColorReversed = TunerService.parseIntegerSwitch(str2, false);
                break;
            case "system:statusbar_battery_bar_animate":
                this.shouldAnimateCharging = TunerService.parseIntegerSwitch(str2, true);
                break;
        }
        if (this.mLocation > 0 && this.shouldAnimateCharging && this.mBatteryCharging && this.mBatteryLevel < 100) {
            start();
        } else {
            stop();
        }
        setProgress(this.mBatteryLevel);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setProgress(int i) {
        if (this.vertical) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mBatteryBarLayout.getLayoutParams();
            layoutParams.height = (int) (((getHeight() / 100.0d) * i) + 0.5d);
            this.mBatteryBarLayout.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mBatteryBarLayout.getLayoutParams();
            layoutParams2.width = (int) (((getWidth() / 100.0d) * i) + 0.5d);
            this.mBatteryBarLayout.setLayoutParams(layoutParams2);
        }
        int colorForPercent = getColorForPercent(i);
        this.mBatteryBar.setBackgroundColor(colorForPercent);
        this.mCharger.setBackgroundColor(colorForPercent);
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        if (this.shouldAnimateCharging) {
            if (this.vertical) {
                TranslateAnimation translateAnimation = new TranslateAnimation(getX(), getX(), getHeight(), this.mBatteryBarLayout.getHeight());
                translateAnimation.setInterpolator(new AccelerateInterpolator());
                translateAnimation.setDuration(1000L);
                translateAnimation.setRepeatCount(-1);
                this.mChargerLayout.startAnimation(translateAnimation);
                this.mChargerLayout.setVisibility(0);
            } else {
                TranslateAnimation translateAnimation2 = new TranslateAnimation(getWidth(), this.mBatteryBarLayout.getWidth(), getTop(), getTop());
                translateAnimation2.setInterpolator(new AccelerateInterpolator());
                translateAnimation2.setDuration(1000L);
                translateAnimation2.setRepeatCount(-1);
                this.mChargerLayout.startAnimation(translateAnimation2);
                this.mChargerLayout.setVisibility(0);
            }
            this.isAnimating = true;
        }
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        this.mChargerLayout.clearAnimation();
        this.mChargerLayout.setVisibility(8);
        this.isAnimating = false;
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        return this.isAnimating;
    }

    private static int getBlendColorForPercent(int i, int i2, boolean z, int i3) {
        float[] fArr = new float[3];
        float[] fArr2 = new float[3];
        float[] fArr3 = new float[3];
        Color.colorToHSV(i, fArr3);
        int iAlpha = Color.alpha(i);
        Color.colorToHSV(i2, fArr2);
        int iAlpha2 = Color.alpha(i2);
        float f = i3 / 100.0f;
        if (z) {
            if (fArr2[0] < fArr3[0]) {
                fArr2[0] = fArr2[0] + 360.0f;
            }
            fArr[0] = fArr2[0] - ((fArr2[0] - fArr3[0]) * f);
        } else {
            if (fArr2[0] > fArr3[0]) {
                fArr3[0] = fArr3[0] + 360.0f;
            }
            fArr[0] = fArr2[0] + ((fArr3[0] - fArr2[0]) * f);
        }
        if (fArr[0] > 360.0f) {
            fArr[0] = fArr[0] - 360.0f;
        } else if (fArr[0] < 0.0f) {
            fArr[0] = fArr[0] + 360.0f;
        }
        fArr[1] = fArr2[1] + ((fArr3[1] - fArr2[1]) * f);
        fArr[2] = fArr2[2] + ((fArr3[2] - fArr2[2]) * f);
        return Color.HSVToColor((int) (iAlpha2 + ((iAlpha - iAlpha2) * f)), fArr);
    }

    private int getColorForPercent(int i) {
        if (this.mBatteryCharging && this.mUseChargingColor) {
            return this.mChargingColor;
        }
        if (this.mBlendColor) {
            return getBlendColorForPercent(this.mColor, this.mBatteryLowColor, this.mBlendColorReversed, i);
        }
        return i > 20 ? this.mColor : this.mBatteryLowColor;
    }
}
