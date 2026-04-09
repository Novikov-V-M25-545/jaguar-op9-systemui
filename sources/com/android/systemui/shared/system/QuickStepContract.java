package com.android.systemui.shared.system;

import android.content.Context;
import android.view.ViewConfiguration;
import java.util.StringJoiner;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class QuickStepContract {
    public static boolean isAssistantGestureDisabled(int i) {
        if ((i & 3083) != 0) {
            return true;
        }
        return (i & 4) != 0 && (i & 64) == 0;
    }

    public static boolean isBackGestureDisabled(int i) {
        return (i & 8) == 0 && (32768 & i) == 0 && (i & 70) != 0;
    }

    public static boolean isGesturalMode(int i) {
        return i == 2;
    }

    public static boolean isLegacyMode(int i) {
        return i == 0;
    }

    public static boolean isSwipeUpMode(int i) {
        return i == 1;
    }

    public static String getSystemUiStateString(int i) {
        StringJoiner stringJoiner = new StringJoiner("|");
        stringJoiner.add((i & 1) != 0 ? "screen_pinned" : "");
        stringJoiner.add((i & 128) != 0 ? "overview_disabled" : "");
        stringJoiner.add((i & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) != 0 ? "home_disabled" : "");
        stringJoiner.add((i & LineageHardwareManager.FEATURE_VIBRATOR) != 0 ? "search_disabled" : "");
        stringJoiner.add((i & 2) != 0 ? "navbar_hidden" : "");
        stringJoiner.add((i & 4) != 0 ? "notif_visible" : "");
        stringJoiner.add((i & LineageHardwareManager.FEATURE_TOUCH_HOVERING) != 0 ? "qs_visible" : "");
        stringJoiner.add((i & 64) != 0 ? "keygrd_visible" : "");
        stringJoiner.add((i & 512) != 0 ? "keygrd_occluded" : "");
        stringJoiner.add((i & 8) != 0 ? "bouncer_visible" : "");
        stringJoiner.add((32768 & i) != 0 ? "global_actions" : "");
        stringJoiner.add((i & 16) != 0 ? "a11y_click" : "");
        stringJoiner.add((i & 32) != 0 ? "a11y_long_click" : "");
        stringJoiner.add((i & LineageHardwareManager.FEATURE_AUTO_CONTRAST) != 0 ? "tracing" : "");
        stringJoiner.add((i & LineageHardwareManager.FEATURE_DISPLAY_MODES) != 0 ? "asst_gesture_constrain" : "");
        stringJoiner.add((i & LineageHardwareManager.FEATURE_READING_ENHANCEMENT) != 0 ? "bubbles_expanded" : "");
        return stringJoiner.toString();
    }

    public static final float getQuickStepTouchSlopPx(Context context) {
        return ViewConfiguration.get(context).getScaledTouchSlop() * 3.0f;
    }
}
