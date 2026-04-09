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
import java.util.Calendar;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class MNMLBoxClockController implements ClockPlugin {
    private TextClock mClock;
    private final SysuiColorExtractor mColorExtractor;
    private TextClock mDate;
    private TextClock mDateDay;
    private final LayoutInflater mLayoutInflater;
    private final Resources mResources;
    private ClockLayout mView;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Calendar mTime = Calendar.getInstance(TimeZone.getDefault());

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "mnml_box";
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

    public MNMLBoxClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.digital_mnml_box, (ViewGroup) null);
        this.mView = clockLayout;
        this.mClock = (TextClock) clockLayout.findViewById(R.id.clock);
        this.mDate = (TextClock) this.mView.findViewById(R.id.bigDate);
        this.mDateDay = (TextClock) this.mView.findViewById(R.id.bigDateDay);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mClock = null;
        this.mDate = null;
        this.mDateDay = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_mnml_box);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.mnmlbox_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View viewInflate = this.mLayoutInflater.inflate(R.layout.digital_mnml_box_preview, (ViewGroup) null);
        TextClock textClock = (TextClock) viewInflate.findViewById(R.id.clock);
        TextClock textClock2 = (TextClock) viewInflate.findViewById(R.id.bigDate);
        TextClock textClock3 = (TextClock) viewInflate.findViewById(R.id.bigDateDay);
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
        this.mClock.setTextColor(i);
        this.mDate.setTextColor(i);
        this.mDateDay.setTextColor(i);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mView.onTimeChanged();
        this.mClock.refreshTime();
        this.mDate.refreshTime();
        this.mDateDay.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mView.setDarkAmount(f);
    }
}
