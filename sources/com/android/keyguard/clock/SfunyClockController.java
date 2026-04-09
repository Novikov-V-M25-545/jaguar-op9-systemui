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
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class SfunyClockController implements ClockPlugin {
    private final SysuiColorExtractor mColorExtractor;
    private TextClock mHourClock;
    private final LayoutInflater mLayoutInflater;
    private TextClock mMinuteClock;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private ClockLayout mView;

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "sfuny";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return "SFUNY";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
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

    public SfunyClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.digital_clock_sfuny, (ViewGroup) null);
        this.mView = clockLayout;
        this.mHourClock = (TextClock) clockLayout.findViewById(R.id.clockHour);
        this.mMinuteClock = (TextClock) this.mView.findViewById(R.id.clockMinute);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mHourClock = null;
        this.mMinuteClock = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.sfuny_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.digital_sfuny_preview, (ViewGroup) null);
        TextClock textClock = (TextClock) viewInflate.findViewById(R.id.clockHour);
        TextClock textClock2 = (TextClock) viewInflate.findViewById(R.id.clockMinute);
        TextClock textClock3 = (TextClock) viewInflate.findViewById(R.id.date);
        textClock.setTextColor(-1);
        textClock2.setTextColor(-1);
        textClock3.setTextColor(-1);
        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        return this.mRenderer.createPreview(viewInflate, i, i2);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getView() {
        if (this.mView == null) {
            createViews();
        }
        return this.mView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public int getPreferredY(int i) {
        return KeyguardClockPositionAlgorithm.CLOCK_USE_DEFAULT_Y;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setTextColor(int i) {
        this.mHourClock.setTextColor(i);
        this.mMinuteClock.setTextColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mView.onTimeChanged();
        this.mHourClock.refreshTime();
        this.mMinuteClock.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mView.setDarkAmount(f);
    }
}
