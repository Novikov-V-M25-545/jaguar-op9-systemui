package com.android.keyguard.clock;

import android.content.Context;
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
public class SamsungClockController implements ClockPlugin {
    private TextClock mClock;
    private final SysuiColorExtractor mColorExtractor;
    private Context mContext;
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
        return "samsung";
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

    public SamsungClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mContext = layoutInflater.getContext();
        this.mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.digital_clock_custom, (ViewGroup) null);
        this.mView = clockLayout;
        TextClock textClock = (TextClock) clockLayout.findViewById(R.id.clock);
        this.mClock = textClock;
        textClock.setTextSize(this.mContext.getResources().getDimensionPixelSize(R.dimen.widget_label_font_size) * 2.0f);
        this.mClock.setLineSpacing(0.0f, 0.8f);
        this.mClock.setFormat12Hour("hh\nmm");
        this.mClock.setFormat24Hour("kk\nmm");
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mClock = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_samsung);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.samsung_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.default_clock_preview, (ViewGroup) null);
        TextClock textClock = (TextClock) viewInflate.findViewById(R.id.time);
        textClock.setLineSpacing(0.0f, 0.8f);
        textClock.setFormat12Hour("hh\nmm");
        textClock.setFormat24Hour("kk\nmm");
        TextClock textClock2 = (TextClock) viewInflate.findViewById(R.id.date);
        textClock.setTextColor(-1);
        textClock2.setTextColor(-1);
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
        this.mClock.setTextColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        ClockLayout clockLayout = this.mView;
        if (clockLayout == null || this.mClock == null) {
            return;
        }
        clockLayout.onTimeChanged();
        this.mClock.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mView.setDarkAmount(f);
    }
}
