package com.android.systemui.crdroid.batterybar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.tuner.TunerService;

/* loaded from: classes.dex */
public class BatteryBarController extends LinearLayout implements TunerService.Tunable {
    boolean isVertical;
    private boolean mAttached;
    private boolean mBatteryCharging;
    private int mBatteryLevel;
    private final BroadcastReceiver mIntentReceiver;
    int mLocation;
    int mLocationToLookFor;
    int mStyle;
    int mThickness;

    public BatteryBarController(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLocationToLookFor = 0;
        this.mAttached = false;
        this.mBatteryLevel = 0;
        this.mBatteryCharging = false;
        this.isVertical = false;
        this.mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.crdroid.batterybar.BatteryBarController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    BatteryBarController.this.mBatteryLevel = intent.getIntExtra("level", 0);
                    BatteryBarController.this.mBatteryCharging = intent.getIntExtra("status", 0) == 2;
                    Prefs.setLastBatteryLevel(context2, BatteryBarController.this.mBatteryLevel);
                }
            }
        };
        if (attributeSet != null) {
            this.mLocationToLookFor = attributeSet.getAttributeIntValue("http://schemas.android.com/apk/res/com.android.systemui", "viewLocation", 0);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mAttached) {
            return;
        }
        this.mAttached = true;
        this.isVertical = getLayoutParams().height == -1;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        getContext().registerReceiver(this.mIntentReceiver, intentFilter);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:statusbar_battery_bar", "system:statusbar_battery_bar_style", "system:statusbar_battery_bar_thickness");
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

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mAttached) {
            getHandler().postDelayed(new Runnable() { // from class: com.android.systemui.crdroid.batterybar.BatteryBarController.2
                @Override // java.lang.Runnable
                public void run() {
                    BatteryBarController.this.addBars();
                }
            }, 500L);
        }
    }

    public void configThickness() {
        int i = (int) ((getContext().getResources().getDisplayMetrics().density * this.mThickness) + 0.5d);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (this.isVertical) {
            layoutParams.width = i;
        } else {
            layoutParams.height = i;
        }
        setLayoutParams(layoutParams);
        if (this.isVertical) {
            layoutParams.width = i;
        } else {
            layoutParams.height = i;
        }
        setLayoutParams(layoutParams);
    }

    public void addBars() {
        removeAllViews();
        int i = this.mLocation;
        if (i == 0 || !isLocationValid(i)) {
            return;
        }
        configThickness();
        this.mBatteryLevel = Prefs.getLastBatteryLevel(getContext());
        int i2 = this.mStyle;
        if (i2 == 0) {
            addView(new BatteryBar(((LinearLayout) this).mContext, this.mBatteryCharging, this.mBatteryLevel, this.isVertical), new LinearLayout.LayoutParams(-1, -1, 1.0f));
            return;
        }
        if (i2 != 1) {
            if (i2 == 2) {
                BatteryBar batteryBar = new BatteryBar(((LinearLayout) this).mContext, this.mBatteryCharging, this.mBatteryLevel, this.isVertical);
                batteryBar.setRotation(180.0f);
                addView(batteryBar, new LinearLayout.LayoutParams(-1, -1, 1.0f));
                return;
            }
            return;
        }
        BatteryBar batteryBar2 = new BatteryBar(((LinearLayout) this).mContext, this.mBatteryCharging, this.mBatteryLevel, this.isVertical);
        BatteryBar batteryBar3 = new BatteryBar(((LinearLayout) this).mContext, this.mBatteryCharging, this.mBatteryLevel, this.isVertical);
        if (this.isVertical) {
            batteryBar3.setRotation(180.0f);
            addView(batteryBar3, new LinearLayout.LayoutParams(-1, -1, 1.0f));
            addView(batteryBar2, new LinearLayout.LayoutParams(-1, -1, 1.0f));
        } else {
            batteryBar2.setRotation(180.0f);
            addView(batteryBar2, new LinearLayout.LayoutParams(-1, -1, 1.0f));
            addView(batteryBar3, new LinearLayout.LayoutParams(-1, -1, 1.0f));
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "system:statusbar_battery_bar_style":
                this.mStyle = str2 != null ? Integer.parseInt(str2) : 1;
                addBars();
                break;
            case "system:statusbar_battery_bar":
                this.mLocation = str2 != null ? Integer.parseInt(str2) : 0;
                addBars();
                break;
            case "system:statusbar_battery_bar_thickness":
                this.mThickness = str2 != null ? Integer.parseInt(str2) : 2;
                configThickness();
                break;
        }
    }

    protected boolean isLocationValid(int i) {
        return this.mLocationToLookFor == i;
    }
}
