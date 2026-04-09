package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class BinaryClockController implements ClockPlugin {
    private ClockLayout mBigClockView;
    private BinaryClock mBinaryClock;
    private final SmallClockPosition mClockPosition;
    private final SysuiColorExtractor mColorExtractor;
    private final LayoutInflater mLayoutInflater;
    private TextClock mLockClock;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private View mView;

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "binary";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setColorPalette(boolean z, int[] iArr) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setStyle(Paint.Style style) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public boolean shouldShowStatusArea() {
        return true;
    }

    public BinaryClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
        this.mClockPosition = new SmallClockPosition(resources);
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.binary_clock, (ViewGroup) null);
        this.mBigClockView = clockLayout;
        this.mBinaryClock = (BinaryClock) clockLayout.findViewById(R.id.analog_clock);
        View viewInflate = this.mLayoutInflater.inflate(R.layout.digital_clock, (ViewGroup) null);
        this.mView = viewInflate;
        this.mLockClock = (TextClock) viewInflate.findViewById(R.id.lock_screen_clock);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mBigClockView = null;
        this.mBinaryClock = null;
        this.mView = null;
        this.mLockClock = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_binary);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.binary_thumbnail);
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
        if (this.mView == null) {
            createViews();
        }
        return this.mView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        if (this.mBigClockView == null) {
            createViews();
        }
        return this.mBigClockView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public int getPreferredY(int i) {
        return this.mClockPosition.getPreferredY();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setTextColor(int i) {
        this.mBinaryClock.setTintColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mBinaryClock.onTimeChanged();
        this.mBigClockView.onTimeChanged();
        this.mLockClock.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mClockPosition.setDarkAmount(f);
        this.mBigClockView.setDarkAmount(f);
        this.mBinaryClock.setDark(f == 1.0f);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
        this.mBinaryClock.onTimeZoneChanged(timeZone);
    }
}
