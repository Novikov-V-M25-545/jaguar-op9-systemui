package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
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
public class MNMLMinimalClockController implements ClockPlugin {
    private int mAccentColor;
    private TextClock mClock;
    private final SysuiColorExtractor mColorExtractor;
    private TextClock mDate;
    private final LayoutInflater mLayoutInflater;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private ClockLayout mView;

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "mnml_mnml";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setStyle(Paint.Style style) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public boolean shouldShowStatusArea() {
        return false;
    }

    public MNMLMinimalClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.digital_mnml_minimal, (ViewGroup) null);
        this.mView = clockLayout;
        this.mClock = (TextClock) clockLayout.findViewById(R.id.clock);
        this.mDate = (TextClock) this.mView.findViewById(R.id.date);
        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        GradientDrawable gradientDrawable = (GradientDrawable) this.mDate.getBackground();
        gradientDrawable.setColor(this.mAccentColor);
        gradientDrawable.setStroke(0, 0);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mClock = null;
        this.mDate = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_mnml_minimal);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.mnmlminimal_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.digital_mnml_minimal_preview, (ViewGroup) null);
        TextClock textClock = (TextClock) viewInflate.findViewById(R.id.clock);
        TextClock textClock2 = (TextClock) viewInflate.findViewById(R.id.date);
        textClock.setTextColor(-1);
        textClock2.setTextColor(-16777216);
        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        GradientDrawable gradientDrawable = (GradientDrawable) textClock2.getBackground();
        gradientDrawable.setColor(this.mAccentColor);
        gradientDrawable.setStroke(0, 0);
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
        this.mClock.setTextColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setColorPalette(boolean z, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            return;
        }
        this.mAccentColor = iArr[Math.max(0, iArr.length - 5)];
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mView.onTimeChanged();
        this.mClock.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mView.setDarkAmount(f);
    }
}
