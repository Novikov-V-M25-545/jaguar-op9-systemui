package com.android.systemui.phone;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class NotificationLightsView extends RelativeLayout {
    private ValueAnimator mLightAnimator;
    private Runnable mLightUpdate;
    private boolean mPulsing;

    public NotificationLightsView(Context context) {
        this(context, null);
    }

    public NotificationLightsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationLightsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NotificationLightsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mLightUpdate = new Runnable() { // from class: com.android.systemui.phone.NotificationLightsView.1
            @Override // java.lang.Runnable
            public void run() {
                NotificationLightsView.this.animateNotification();
            }
        };
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing == z) {
            return;
        }
        this.mPulsing = z;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    public void animateNotification() {
        int intForUser = Settings.Secure.getIntForUser(((RelativeLayout) this).mContext.getContentResolver(), "pulse_ambient_light_color", -13008641, -2);
        int intForUser2 = Settings.Secure.getIntForUser(((RelativeLayout) this).mContext.getContentResolver(), "pulse_ambient_light_duration", 2, -2) * 1000;
        int intForUser3 = Settings.Secure.getIntForUser(((RelativeLayout) this).mContext.getContentResolver(), "pulse_ambient_light_layout", 0, -2);
        int intForUser4 = Settings.Secure.getIntForUser(((RelativeLayout) this).mContext.getContentResolver(), "pulse_ambient_light_width", 125, -2);
        final ImageView imageView = (ImageView) findViewById(R.id.notification_animation_left_solid);
        final ImageView imageView2 = (ImageView) findViewById(R.id.notification_animation_left_faded);
        imageView.setColorFilter(intForUser);
        imageView2.setColorFilter(intForUser);
        imageView.setVisibility(intForUser3 == 0 ? 0 : 8);
        imageView2.setVisibility(intForUser3 == 1 ? 0 : 8);
        final ImageView imageView3 = (ImageView) findViewById(R.id.notification_animation_right_solid);
        final ImageView imageView4 = (ImageView) findViewById(R.id.notification_animation_right_faded);
        imageView3.setColorFilter(intForUser);
        imageView4.setColorFilter(intForUser);
        imageView3.setVisibility(intForUser3 == 0 ? 0 : 8);
        imageView4.setVisibility(intForUser3 != 1 ? 8 : 0);
        imageView.getLayoutParams().width = intForUser4;
        imageView3.getLayoutParams().width = intForUser4;
        imageView2.getLayoutParams().width = intForUser4;
        imageView4.getLayoutParams().width = intForUser4;
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 2.0f);
        this.mLightAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(intForUser2);
        this.mLightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.phone.NotificationLightsView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                imageView.setScaleY(fFloatValue);
                imageView2.setScaleY(fFloatValue);
                imageView3.setScaleY(fFloatValue);
                imageView4.setScaleY(fFloatValue);
                float f = 1.0f;
                if (fFloatValue <= 0.3f) {
                    f = fFloatValue / 0.3f;
                } else if (fFloatValue >= 1.0f) {
                    f = 2.0f - fFloatValue;
                }
                imageView.setAlpha(f);
                imageView2.setAlpha(f);
                imageView3.setAlpha(f);
                imageView4.setAlpha(f);
            }
        });
        this.mLightAnimator.start();
    }
}
