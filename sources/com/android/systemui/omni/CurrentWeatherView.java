package com.android.systemui.omni;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.util.crdroid.OmniJawsClient;
import com.android.settingslib.Utils;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class CurrentWeatherView extends FrameLayout implements OmniJawsClient.OmniJawsObserver {
    private ImageView mCurrentImage;
    private float mDarkAmount;
    private TextView mLeftText;
    private TextView mRightText;
    private int mTextColor;
    private boolean mUpdatesEnabled;
    private OmniJawsClient mWeatherClient;

    public CurrentWeatherView(Context context) {
        this(context, null);
    }

    public CurrentWeatherView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CurrentWeatherView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void enableUpdates() {
        if (this.mUpdatesEnabled) {
            return;
        }
        setVisibility(0);
        OmniJawsClient omniJawsClient = new OmniJawsClient(getContext(), false);
        this.mWeatherClient = omniJawsClient;
        if (omniJawsClient.isOmniJawsEnabled()) {
            this.mWeatherClient.addSettingsObserver();
            this.mWeatherClient.addObserver(this);
            queryAndUpdateWeather();
            this.mUpdatesEnabled = true;
        }
    }

    public void disableUpdates() {
        if (this.mUpdatesEnabled) {
            setVisibility(8);
            OmniJawsClient omniJawsClient = this.mWeatherClient;
            if (omniJawsClient != null) {
                omniJawsClient.removeObserver(this);
                this.mWeatherClient.cleanupObserver();
                this.mUpdatesEnabled = false;
            }
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCurrentImage = (ImageView) findViewById(R.id.current_image);
        this.mLeftText = (TextView) findViewById(R.id.left_text);
        this.mRightText = (TextView) findViewById(R.id.right_text);
        this.mTextColor = this.mLeftText.getCurrentTextColor();
    }

    private void updateWeatherData(OmniJawsClient.WeatherInfo weatherInfo) throws Resources.NotFoundException {
        if (!this.mWeatherClient.isOmniJawsEnabled()) {
            setErrorView();
            return;
        }
        if (weatherInfo == null) {
            setBlankView();
            return;
        }
        Drawable drawableMutate = this.mWeatherClient.getWeatherConditionImage(weatherInfo.conditionCode).mutate();
        updateTint(drawableMutate);
        this.mCurrentImage.setImageDrawable(drawableMutate);
        this.mRightText.setText(weatherInfo.temp + " " + weatherInfo.tempUnits);
        this.mLeftText.setText(weatherInfo.city);
    }

    private int getTintColor() {
        return Utils.getColorAttrDefaultColor(((FrameLayout) this).mContext, R.attr.wallpaperTextColor);
    }

    private void setErrorView() throws Resources.NotFoundException {
        Drawable drawable = ((FrameLayout) this).mContext.getResources().getDrawable(R.drawable.ic_qs_weather_default_off_white);
        updateTint(drawable);
        this.mCurrentImage.setImageDrawable(drawable);
        this.mLeftText.setText("");
        this.mRightText.setText("");
    }

    private void setBlankView() {
        this.mCurrentImage.setImageDrawable(null);
        this.mLeftText.setText("");
        this.mRightText.setText("");
    }

    public void weatherError(int i) throws Resources.NotFoundException {
        if (i == 2) {
            setErrorView();
        }
    }

    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    public void updateSettings() throws Resources.NotFoundException {
        updateWeatherData(this.mWeatherClient.getWeatherInfo());
    }

    private void queryAndUpdateWeather() {
        OmniJawsClient omniJawsClient = this.mWeatherClient;
        if (omniJawsClient == null) {
            return;
        }
        try {
            omniJawsClient.queryWeather();
            updateWeatherData(this.mWeatherClient.getWeatherInfo());
        } catch (Exception unused) {
        }
    }

    private void updateTint(Drawable drawable) {
        if (this.mDarkAmount == 1.0f) {
            this.mCurrentImage.setImageTintList(ColorStateList.valueOf(-1));
        } else {
            this.mCurrentImage.setImageTintList(drawable instanceof VectorDrawable ? ColorStateList.valueOf(getTintColor()) : null);
        }
    }

    public void onDensityOrFontScaleChanged() {
        TextView textView = this.mLeftText;
        Resources resources = getResources();
        int i = R.dimen.widget_label_font_size;
        textView.setTextSize(0, resources.getDimensionPixelSize(i));
        this.mRightText.setTextSize(0, getResources().getDimensionPixelSize(i));
        ViewGroup.LayoutParams layoutParams = this.mCurrentImage.getLayoutParams();
        Resources resources2 = getResources();
        int i2 = R.dimen.current_weather_image_size;
        layoutParams.height = resources2.getDimensionPixelSize(i2);
        this.mCurrentImage.getLayoutParams().width = getResources().getDimensionPixelSize(i2);
    }
}
