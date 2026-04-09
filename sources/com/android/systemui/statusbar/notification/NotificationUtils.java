package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class NotificationUtils {
    private static final int[] sLocationBase = new int[2];
    private static final int[] sLocationOffset = new int[2];
    private static Boolean sUseNewInterruptionModel;

    public static float interpolate(float f, float f2, float f3) {
        return (f * (1.0f - f3)) + (f2 * f3);
    }

    public static boolean isGrayscale(ImageView imageView, ContrastColorUtil contrastColorUtil) {
        int i = R.id.icon_is_grayscale;
        Object tag = imageView.getTag(i);
        if (tag != null) {
            return Boolean.TRUE.equals(tag);
        }
        boolean zIsGrayscaleIcon = contrastColorUtil.isGrayscaleIcon(imageView.getDrawable());
        imageView.setTag(i, Boolean.valueOf(zIsGrayscaleIcon));
        return zIsGrayscaleIcon;
    }

    public static int interpolateColors(int i, int i2, float f) {
        return Color.argb((int) interpolate(Color.alpha(i), Color.alpha(i2), f), (int) interpolate(Color.red(i), Color.red(i2), f), (int) interpolate(Color.green(i), Color.green(i2), f), (int) interpolate(Color.blue(i), Color.blue(i2), f));
    }

    public static float getRelativeYOffset(View view, View view2) {
        view2.getLocationOnScreen(sLocationBase);
        view.getLocationOnScreen(sLocationOffset);
        return r2[1] - r0[1];
    }

    public static int getFontScaledHeight(Context context, int i) throws Resources.NotFoundException {
        return (int) (context.getResources().getDimensionPixelSize(i) * Math.max(1.0f, context.getResources().getDisplayMetrics().scaledDensity / context.getResources().getDisplayMetrics().density));
    }

    public static boolean useNewInterruptionModel(Context context) {
        if (sUseNewInterruptionModel == null) {
            sUseNewInterruptionModel = Boolean.valueOf(Settings.Secure.getInt(context.getContentResolver(), "new_interruption_model", 1) != 0);
        }
        return sUseNewInterruptionModel.booleanValue();
    }
}
