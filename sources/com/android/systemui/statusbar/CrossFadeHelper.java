package com.android.systemui.statusbar;

import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class CrossFadeHelper {
    public static void fadeOut(View view) {
        fadeOut(view, (Runnable) null);
    }

    public static void fadeOut(View view, Runnable runnable) {
        fadeOut(view, 210L, 0, runnable);
    }

    public static void fadeOut(final View view, long j, int i, final Runnable runnable) {
        view.animate().cancel();
        view.animate().alpha(0.0f).setDuration(j).setInterpolator(Interpolators.ALPHA_OUT).setStartDelay(i).withEndAction(new Runnable() { // from class: com.android.systemui.statusbar.CrossFadeHelper.1
            @Override // java.lang.Runnable
            public void run() {
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
                if (view.getVisibility() != 8) {
                    view.setVisibility(4);
                }
            }
        });
        if (view.hasOverlappingRendering()) {
            view.animate().withLayer();
        }
    }

    public static void fadeOut(View view, float f) {
        fadeOut(view, f, true);
    }

    public static void fadeOut(View view, float f, boolean z) {
        view.animate().cancel();
        if (f == 1.0f && view.getVisibility() != 8) {
            view.setVisibility(4);
        } else if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        if (z) {
            f = mapToFadeDuration(f);
        }
        float interpolation = Interpolators.ALPHA_OUT.getInterpolation(1.0f - f);
        view.setAlpha(interpolation);
        updateLayerType(view, interpolation);
    }

    private static float mapToFadeDuration(float f) {
        return Math.min(f / 0.5833333f, 1.0f);
    }

    private static void updateLayerType(View view, float f) {
        if (view.hasOverlappingRendering() && f > 0.0f && f < 1.0f) {
            if (view.getLayerType() != 2) {
                view.setLayerType(2, null);
                view.setTag(R.id.cross_fade_layer_type_changed_tag, Boolean.TRUE);
                return;
            }
            return;
        }
        if (view.getLayerType() == 2) {
            int i = R.id.cross_fade_layer_type_changed_tag;
            if (view.getTag(i) == null || view.getTag(i) == null) {
                return;
            }
            view.setLayerType(0, null);
        }
    }

    public static void fadeIn(View view) {
        fadeIn(view, 210L, 0);
    }

    public static void fadeIn(View view, long j, int i) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setAlpha(0.0f);
            view.setVisibility(0);
        }
        view.animate().alpha(1.0f).setDuration(j).setStartDelay(i).setInterpolator(Interpolators.ALPHA_IN).withEndAction(null);
        if (!view.hasOverlappingRendering() || view.getLayerType() == 2) {
            return;
        }
        view.animate().withLayer();
    }

    public static void fadeIn(View view, float f) {
        fadeIn(view, f, false);
    }

    public static void fadeIn(View view, float f, boolean z) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        if (z) {
            f = mapToFadeDuration(f);
        }
        float interpolation = Interpolators.ALPHA_IN.getInterpolation(f);
        view.setAlpha(interpolation);
        updateLayerType(view, interpolation);
    }
}
