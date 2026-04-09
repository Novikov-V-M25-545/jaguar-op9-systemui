package com.android.systemui.charging;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import java.text.NumberFormat;

/* loaded from: classes.dex */
public class WirelessChargingLayout extends FrameLayout {
    public WirelessChargingLayout(Context context, int i, int i2, boolean z) {
        super(context);
        init(context, null, i, i2, z);
    }

    public WirelessChargingLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet, false);
    }

    private void init(Context context, AttributeSet attributeSet, boolean z) {
        init(context, attributeSet, -1, -1, false);
    }

    private void init(Context context, AttributeSet attributeSet, int i, int i2, boolean z) {
        boolean z2 = i != -1;
        FrameLayout.inflate(new ContextThemeWrapper(context, R.style.ChargingAnim_Background), R.layout.wireless_charging_layout, this);
        Animatable animatable = (Animatable) ((ImageView) findViewById(R.id.wireless_charging_view)).getDrawable();
        TextView textView = (TextView) findViewById(R.id.wireless_charging_percentage);
        if (i2 != -1) {
            textView.setText(NumberFormat.getPercentInstance().format(i2 / 100.0f));
            textView.setAlpha(0.0f);
        }
        long integer = context.getResources().getInteger(R.integer.wireless_charging_fade_offset);
        long integer2 = context.getResources().getInteger(R.integer.wireless_charging_fade_duration);
        float f = context.getResources().getFloat(R.dimen.wireless_charging_anim_battery_level_text_size_start);
        float f2 = context.getResources().getFloat(R.dimen.wireless_charging_anim_battery_level_text_size_end) * (z2 ? 0.75f : 1.0f);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(textView, "textSize", f, f2);
        objectAnimatorOfFloat.setInterpolator(new PathInterpolator(0.0f, 0.0f, 0.0f, 1.0f));
        Resources resources = context.getResources();
        int i3 = R.integer.wireless_charging_battery_level_text_scale_animation_duration;
        objectAnimatorOfFloat.setDuration(resources.getInteger(i3));
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(textView, "alpha", 0.0f, 1.0f);
        Interpolator interpolator = Interpolators.LINEAR;
        objectAnimatorOfFloat2.setInterpolator(interpolator);
        Resources resources2 = context.getResources();
        int i4 = R.integer.wireless_charging_battery_level_text_opacity_duration;
        objectAnimatorOfFloat2.setDuration(resources2.getInteger(i4));
        Resources resources3 = context.getResources();
        int i5 = R.integer.wireless_charging_anim_opacity_offset;
        objectAnimatorOfFloat2.setStartDelay(resources3.getInteger(i5));
        ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(textView, "alpha", 1.0f, 0.0f);
        objectAnimatorOfFloat3.setDuration(integer2);
        objectAnimatorOfFloat3.setInterpolator(interpolator);
        objectAnimatorOfFloat3.setStartDelay(integer);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2, objectAnimatorOfFloat3);
        if (!z2) {
            animatable.start();
            animatorSet.start();
            return;
        }
        TextView textView2 = (TextView) findViewById(R.id.reverse_wireless_charging_percentage);
        textView2.setVisibility(0);
        textView2.setText(NumberFormat.getPercentInstance().format(i / 100.0f));
        textView2.setAlpha(0.0f);
        ObjectAnimator objectAnimatorOfFloat4 = ObjectAnimator.ofFloat(textView2, "textSize", f, f2);
        objectAnimatorOfFloat4.setInterpolator(new PathInterpolator(0.0f, 0.0f, 0.0f, 1.0f));
        objectAnimatorOfFloat4.setDuration(context.getResources().getInteger(i3));
        ObjectAnimator objectAnimatorOfFloat5 = ObjectAnimator.ofFloat(textView2, "alpha", 0.0f, 1.0f);
        objectAnimatorOfFloat5.setInterpolator(interpolator);
        objectAnimatorOfFloat5.setDuration(context.getResources().getInteger(i4));
        objectAnimatorOfFloat5.setStartDelay(context.getResources().getInteger(i5));
        ObjectAnimator objectAnimatorOfFloat6 = ObjectAnimator.ofFloat(textView2, "alpha", 1.0f, 0.0f);
        objectAnimatorOfFloat6.setDuration(integer2);
        objectAnimatorOfFloat6.setInterpolator(interpolator);
        objectAnimatorOfFloat6.setStartDelay(integer);
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.playTogether(objectAnimatorOfFloat4, objectAnimatorOfFloat5, objectAnimatorOfFloat6);
        ImageView imageView = (ImageView) findViewById(R.id.reverse_wireless_charging_icon);
        imageView.setVisibility(0);
        int iRound = Math.round(TypedValue.applyDimension(1, f2, getResources().getDisplayMetrics()));
        imageView.setPadding(iRound, 0, iRound, 0);
        ObjectAnimator objectAnimatorOfFloat7 = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f);
        objectAnimatorOfFloat7.setInterpolator(interpolator);
        objectAnimatorOfFloat7.setDuration(context.getResources().getInteger(i4));
        objectAnimatorOfFloat7.setStartDelay(context.getResources().getInteger(i5));
        ObjectAnimator objectAnimatorOfFloat8 = ObjectAnimator.ofFloat(imageView, "alpha", 1.0f, 0.0f);
        objectAnimatorOfFloat8.setDuration(integer2);
        objectAnimatorOfFloat8.setInterpolator(interpolator);
        objectAnimatorOfFloat8.setStartDelay(integer);
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.playTogether(objectAnimatorOfFloat7, objectAnimatorOfFloat8);
        animatable.start();
        animatorSet.start();
        animatorSet2.start();
        animatorSet3.start();
    }
}
