package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class TuxClockController implements ClockPlugin {
    private TextClock mClock;
    private final LayoutInflater mLayoutInflater;
    private ImageView mLogo;
    private final Resources mResources;
    private ClockLayout mView;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final ClockPalette mPalette = new ClockPalette();

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "tux";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setStyle(Paint.Style style) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public boolean shouldShowStatusArea() {
        return true;
    }

    public TuxClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
    }

    private void createViews() {
        ClockLayout clockLayout = (ClockLayout) this.mLayoutInflater.inflate(R.layout.tux_clock, (ViewGroup) null);
        this.mView = clockLayout;
        TextClock textClock = (TextClock) clockLayout.findViewById(R.id.clock);
        this.mClock = textClock;
        textClock.setFormat12Hour("hh\nmm");
        this.mClock.setFormat24Hour("kk\nmm");
        this.mLogo = (ImageView) this.mView.findViewById(R.id.logo);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mClock = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(R.string.clock_title_tux);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public int getPreferredY(int i) {
        return i / 4;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, R.drawable.tux_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View view = getView();
        TextClock textClock = (TextClock) view.findViewById(R.id.clock);
        textClock.setFormat12Hour("hh\nmm");
        textClock.setFormat24Hour("kk\nmm");
        onTimeTick();
        return this.mRenderer.createPreview(view, i, i2);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getView() {
        if (this.mView == null) {
            createViews();
        }
        return this.mView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setTextColor(int i) {
        updateColor();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setColorPalette(boolean z, int[] iArr) {
        this.mPalette.setColorPalette(z, iArr);
        updateColor();
    }

    private void updateColor() {
        int primaryColor = this.mPalette.getPrimaryColor();
        this.mClock.setTextColor(this.mPalette.getSecondaryColor());
        this.mLogo.setColorFilter(primaryColor);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
        this.mView.onTimeChanged();
        this.mClock.refreshTime();
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
        this.mPalette.setDarkAmount(f);
        this.mView.setDarkAmount(f);
    }
}
