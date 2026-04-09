package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class TypeClockController implements ClockPlugin {
    private final int mBurnInOffsetY;
    private final SysuiColorExtractor mColorExtractor;
    private float mDarkAmount;
    private CrossFadeDarkController mDarkController;
    private final int mKeyguardLockHeight;
    private final int mKeyguardLockPadding;
    private final LayoutInflater mLayoutInflater;
    private TypographicClock mLockClock;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private final int mStatusBarHeight;
    private TypographicClock mTypeClock;
    private View mView;

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "type";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setStyle(Paint.Style style) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public boolean shouldShowStatusArea() {
        return false;
    }

    TypeClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
        this.mStatusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
        this.mKeyguardLockPadding = resources.getDimensionPixelSize(R.dimen.keyguard_lock_padding);
        this.mKeyguardLockHeight = resources.getDimensionPixelSize(R.dimen.keyguard_lock_height);
        this.mBurnInOffsetY = resources.getDimensionPixelSize(R.dimen.burn_in_prevention_offset_y);
    }

    private void createViews() {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.type_aod_clock, (ViewGroup) null);
        this.mView = viewInflate;
        this.mTypeClock = (TypographicClock) viewInflate.findViewById(R.id.type_clock);
        TypographicClock typographicClock = (TypographicClock) this.mLayoutInflater.inflate(R.layout.typographic_clock, (ViewGroup) null);
        this.mLockClock = typographicClock;
        typographicClock.setVisibility(8);
        this.mDarkController = new CrossFadeDarkController(this.mView, this.mLockClock);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mTypeClock = null;
        this.mLockClock = null;
        this.mDarkController = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_type);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.type_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View bigClockView = getBigClockView();
        setDarkAmount(1.0f);
        setTextColor(-1);
        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();
        return this.mRenderer.createPreview(bigClockView, i, i2);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getView() {
        if (this.mLockClock == null) {
            createViews();
        }
        return this.mLockClock;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        if (this.mView == null) {
            createViews();
        }
        return this.mView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public int getPreferredY(int i) {
        return (int) MathUtils.lerp(this.mStatusBarHeight + this.mKeyguardLockHeight + (this.mKeyguardLockPadding * 2) + (this.mTypeClock.getHeight() / 2), this.mStatusBarHeight + this.mKeyguardLockHeight + (this.mKeyguardLockPadding * 2) + this.mBurnInOffsetY + this.mTypeClock.getHeight() + (this.mTypeClock.getHeight() / 2), this.mDarkAmount);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setTextColor(int i) {
        this.mTypeClock.setTextColor(i);
        this.mLockClock.setTextColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setColorPalette(boolean z, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            return;
        }
        int i = iArr[Math.max(0, iArr.length - 5)];
        this.mTypeClock.setClockColor(i);
        this.mLockClock.setClockColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mTypeClock.onTimeChanged();
        this.mLockClock.onTimeChanged();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mDarkAmount = f;
        CrossFadeDarkController crossFadeDarkController = this.mDarkController;
        if (crossFadeDarkController != null) {
            crossFadeDarkController.setDarkAmount(f);
        }
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
        this.mTypeClock.onTimeZoneChanged(timeZone);
        this.mLockClock.onTimeZoneChanged(timeZone);
    }
}
