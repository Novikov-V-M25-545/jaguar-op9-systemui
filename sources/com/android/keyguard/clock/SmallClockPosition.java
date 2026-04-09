package com.android.keyguard.clock;

import android.content.res.Resources;
import android.util.MathUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.R;

/* loaded from: classes.dex */
class SmallClockPosition {
    private final int mBurnInOffsetY;
    private float mDarkAmount;
    private final int mKeyguardLockHeight;
    private final int mKeyguardLockPadding;
    private final int mStatusBarHeight;

    SmallClockPosition(Resources resources) {
        this(resources.getDimensionPixelSize(R.dimen.status_bar_height), resources.getDimensionPixelSize(R.dimen.keyguard_lock_padding), resources.getDimensionPixelSize(R.dimen.keyguard_lock_height), resources.getDimensionPixelSize(R.dimen.burn_in_prevention_offset_y));
    }

    @VisibleForTesting
    SmallClockPosition(int i, int i2, int i3, int i4) {
        this.mStatusBarHeight = i;
        this.mKeyguardLockPadding = i2;
        this.mKeyguardLockHeight = i3;
        this.mBurnInOffsetY = i4;
    }

    void setDarkAmount(float f) {
        this.mDarkAmount = f;
    }

    int getPreferredY() {
        int i = this.mStatusBarHeight;
        int i2 = this.mKeyguardLockHeight;
        int i3 = this.mKeyguardLockPadding;
        return (int) MathUtils.lerp(i + i2 + (i3 * 2), i + i2 + (i3 * 2) + this.mBurnInOffsetY, this.mDarkAmount);
    }
}
