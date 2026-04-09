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
public class OctaviDigitalClockController implements ClockPlugin {
    private final SysuiColorExtractor mColorExtractor;
    private TextClock mDate;
    private TextClock mDay;
    private final LayoutInflater mLayoutInflater;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private TextClock mTimeClock;
    private ClockLayout mView;

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "octavi";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return "OctaviDigitalClock";
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
        return false;
    }

    public OctaviDigitalClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.digital_clock_octavi, (ViewGroup) null);
        this.mView = clockLayout;
        setViews(clockLayout);
    }

    private void setViews(View view) {
        this.mTimeClock = (TextClock) view.findViewById(R.id.time_clock);
        this.mDay = (TextClock) view.findViewById(R.id.clock_day);
        this.mDate = (TextClock) view.findViewById(R.id.date);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mTimeClock = null;
        this.mDay = null;
        this.mDate = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.octavi_digital_preview);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.digital_clock_octavi_preview, (ViewGroup) null);
        setViews(viewInflate);
        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();
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
        this.mTimeClock.setTextColor(i);
        this.mDay.setTextColor(i);
        this.mDate.setTextColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        ClockLayout clockLayout = this.mView;
        if (clockLayout != null) {
            clockLayout.onTimeChanged();
        }
        this.mTimeClock.refreshTime();
        this.mDay.refreshTime();
        this.mDate.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        ClockLayout clockLayout = this.mView;
        if (clockLayout != null) {
            clockLayout.setDarkAmount(f);
        }
    }
}
