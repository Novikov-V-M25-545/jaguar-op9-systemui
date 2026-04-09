package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.util.MathUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.doze.util.BurnInHelperKt;
import com.android.systemui.statusbar.notification.NotificationUtils;

/* loaded from: classes.dex */
public class KeyguardClockPositionAlgorithm {
    private static float CLOCK_HEIGHT_WEIGHT = 0.7f;
    public static int CLOCK_USE_DEFAULT_Y = -1;
    private int mBurnInPreventionOffsetX;
    private int mBurnInPreventionOffsetY;
    private boolean mBypassEnabled;
    private int mClockNotificationsMargin;
    private int mClockPreferredY;
    private int mContainerTopPadding;
    private float mDarkAmount;
    private float mEmptyDragAmount;
    private boolean mHasCustomClock;
    private boolean mHasVisibleNotifs;
    private int mHeight;
    private int mKeyguardStatusHeight;
    private int mMaxShadeBottom;
    private int mMinTopMargin;
    private int mNotificationStackHeight;
    private float mPanelExpansion;
    private int mUnlockedStackScrollerPadding;

    public static class Result {
        public float clockAlpha;
        public int clockX;
        public int clockY;
        public int stackScrollerPadding;
        public int stackScrollerPaddingExpanded;
    }

    public void loadDimens(Resources resources) {
        this.mClockNotificationsMargin = resources.getDimensionPixelSize(R.dimen.keyguard_clock_notifications_margin);
        this.mContainerTopPadding = Math.max(resources.getDimensionPixelSize(R.dimen.keyguard_clock_top_margin), resources.getDimensionPixelSize(R.dimen.keyguard_lock_height) + resources.getDimensionPixelSize(R.dimen.keyguard_lock_padding) + resources.getDimensionPixelSize(R.dimen.keyguard_clock_lock_margin));
        this.mBurnInPreventionOffsetX = resources.getDimensionPixelSize(R.dimen.burn_in_prevention_offset_x);
        this.mBurnInPreventionOffsetY = resources.getDimensionPixelSize(R.dimen.burn_in_prevention_offset_y);
    }

    public void setup(int i, int i2, int i3, float f, int i4, int i5, int i6, boolean z, boolean z2, float f2, float f3, boolean z3, int i7) {
        this.mMinTopMargin = i + this.mContainerTopPadding;
        this.mMaxShadeBottom = i2;
        this.mNotificationStackHeight = i3;
        this.mPanelExpansion = f;
        this.mHeight = i4;
        this.mKeyguardStatusHeight = i5;
        this.mClockPreferredY = i6;
        this.mHasCustomClock = z;
        this.mHasVisibleNotifs = z2;
        this.mDarkAmount = f2;
        this.mEmptyDragAmount = f3;
        this.mBypassEnabled = z3;
        this.mUnlockedStackScrollerPadding = i7;
    }

    public void run(Result result) {
        int clockY;
        int clockY2 = getClockY(this.mPanelExpansion);
        result.clockY = clockY2;
        result.clockAlpha = getClockAlpha(clockY2);
        boolean z = this.mBypassEnabled;
        result.stackScrollerPadding = z ? this.mUnlockedStackScrollerPadding : clockY2 + this.mKeyguardStatusHeight;
        if (z) {
            clockY = this.mUnlockedStackScrollerPadding;
        } else {
            clockY = getClockY(1.0f) + this.mKeyguardStatusHeight;
        }
        result.stackScrollerPaddingExpanded = clockY;
        result.clockX = (int) NotificationUtils.interpolate(0.0f, burnInPreventionOffsetX(), this.mDarkAmount);
    }

    public float getMinStackScrollerPadding() {
        return this.mBypassEnabled ? this.mUnlockedStackScrollerPadding : this.mMinTopMargin + this.mKeyguardStatusHeight + this.mClockNotificationsMargin;
    }

    private int getMaxClockY() {
        return ((this.mHeight / 2) - this.mKeyguardStatusHeight) - this.mClockNotificationsMargin;
    }

    private int getPreferredAlternativeClockY(int i) {
        int i2 = this.mClockPreferredY;
        return i2 != CLOCK_USE_DEFAULT_Y ? i2 : i;
    }

    private int getExpandedPreferredClockY() {
        if (this.mHasCustomClock && (!this.mHasVisibleNotifs || this.mBypassEnabled)) {
            return getPreferredAlternativeClockY(getExpandedClockPosition());
        }
        return getExpandedClockPosition();
    }

    public int getExpandedClockPosition() {
        int i = this.mMaxShadeBottom;
        int i2 = this.mMinTopMargin;
        float f = (((((i - i2) / 2) + i2) - (this.mKeyguardStatusHeight * CLOCK_HEIGHT_WEIGHT)) - this.mClockNotificationsMargin) - (this.mNotificationStackHeight / 2);
        if (f < i2) {
            f = i2;
        }
        float maxClockY = getMaxClockY();
        if (f > maxClockY) {
            f = maxClockY;
        }
        return (int) f;
    }

    private int getClockY(float f) {
        float fMax = MathUtils.max(0.0f, (this.mHasCustomClock ? getPreferredAlternativeClockY(getMaxClockY()) : getMaxClockY()) + burnInPreventionOffsetY());
        float expandedPreferredClockY = getExpandedPreferredClockY();
        float f2 = -this.mKeyguardStatusHeight;
        float interpolation = Interpolators.FAST_OUT_LINEAR_IN.getInterpolation(f);
        return (int) (MathUtils.lerp(MathUtils.lerp(f2, expandedPreferredClockY, interpolation), MathUtils.lerp(f2, fMax, interpolation), (!this.mBypassEnabled || this.mHasCustomClock) ? this.mDarkAmount : 1.0f) + this.mEmptyDragAmount);
    }

    private float getClockAlpha(int i) {
        return MathUtils.lerp(Interpolators.ACCELERATE.getInterpolation(Math.max(0.0f, i / Math.max(1.0f, getClockY(1.0f)))), 1.0f, this.mDarkAmount);
    }

    private float burnInPreventionOffsetY() {
        return BurnInHelperKt.getBurnInOffset(this.mBurnInPreventionOffsetY * 2, false) - this.mBurnInPreventionOffsetY;
    }

    private float burnInPreventionOffsetX() {
        return BurnInHelperKt.getBurnInOffset(this.mBurnInPreventionOffsetX * 2, true) - this.mBurnInPreventionOffsetX;
    }
}
