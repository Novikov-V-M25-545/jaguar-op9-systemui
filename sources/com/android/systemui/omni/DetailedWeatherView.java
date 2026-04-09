package com.android.systemui.omni;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.util.crdroid.OmniJawsClient;
import com.android.systemui.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/* loaded from: classes.dex */
public class DetailedWeatherView extends FrameLayout {
    private ImageView mCurrentImage;
    private TextView mCurrentTemp;
    private TextView mCurrentText;
    private View mCurrentView;
    private View mEmptyView;
    private ImageView mEmptyViewImage;
    private ImageView mForecastImage0;
    private ImageView mForecastImage1;
    private ImageView mForecastImage2;
    private ImageView mForecastImage3;
    private ImageView mForecastImage4;
    private TextView mForecastTemp0;
    private TextView mForecastTemp1;
    private TextView mForecastTemp2;
    private TextView mForecastTemp3;
    private TextView mForecastTemp4;
    private TextView mForecastText0;
    private TextView mForecastText1;
    private TextView mForecastText2;
    private TextView mForecastText3;
    private TextView mForecastText4;
    private View mProgressContainer;
    private TextView mProviderName;
    private TextView mStatusMsg;
    private TextView mWeatherCity;
    private OmniJawsClient mWeatherClient;
    private TextView mWeatherData;
    private View mWeatherLine;
    private TextView mWeatherTimestamp;

    public DetailedWeatherView(Context context) {
        this(context, null);
    }

    public DetailedWeatherView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DetailedWeatherView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setWeatherClient(OmniJawsClient omniJawsClient) {
        this.mWeatherClient = omniJawsClient;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressContainer = findViewById(R.id.progress_container);
        this.mWeatherCity = (TextView) findViewById(R.id.current_weather_city);
        this.mWeatherTimestamp = (TextView) findViewById(R.id.current_weather_timestamp);
        this.mWeatherData = (TextView) findViewById(R.id.current_weather_data);
        this.mForecastImage0 = (ImageView) findViewById(R.id.forecast_image_0);
        this.mForecastImage1 = (ImageView) findViewById(R.id.forecast_image_1);
        this.mForecastImage2 = (ImageView) findViewById(R.id.forecast_image_2);
        this.mForecastImage3 = (ImageView) findViewById(R.id.forecast_image_3);
        this.mForecastImage4 = (ImageView) findViewById(R.id.forecast_image_4);
        this.mForecastTemp0 = (TextView) findViewById(R.id.forecast_temp_0);
        this.mForecastTemp1 = (TextView) findViewById(R.id.forecast_temp_1);
        this.mForecastTemp2 = (TextView) findViewById(R.id.forecast_temp_2);
        this.mForecastTemp3 = (TextView) findViewById(R.id.forecast_temp_3);
        this.mForecastTemp4 = (TextView) findViewById(R.id.forecast_temp_4);
        this.mForecastText0 = (TextView) findViewById(R.id.forecast_text_0);
        this.mForecastText1 = (TextView) findViewById(R.id.forecast_text_1);
        this.mForecastText2 = (TextView) findViewById(R.id.forecast_text_2);
        this.mForecastText3 = (TextView) findViewById(R.id.forecast_text_3);
        this.mForecastText4 = (TextView) findViewById(R.id.forecast_text_4);
        this.mCurrentView = findViewById(R.id.current);
        this.mCurrentImage = (ImageView) findViewById(R.id.current_image);
        this.mCurrentTemp = (TextView) findViewById(R.id.current_temp);
        this.mCurrentText = (TextView) findViewById(R.id.current_text);
        this.mStatusMsg = (TextView) findViewById(R.id.status_msg);
        this.mEmptyView = findViewById(android.R.id.empty);
        this.mEmptyViewImage = (ImageView) findViewById(R.id.empty_weather_image);
        this.mWeatherLine = findViewById(R.id.current_weather);
        this.mProviderName = (TextView) findViewById(R.id.current_weather_provider);
        this.mEmptyViewImage.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.omni.DetailedWeatherView.1
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (!DetailedWeatherView.this.mWeatherClient.isOmniJawsEnabled()) {
                    return true;
                }
                DetailedWeatherView.this.startProgress();
                DetailedWeatherView.this.forceRefreshWeatherSettings();
                return true;
            }
        });
        this.mWeatherLine.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.omni.DetailedWeatherView.2
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (!DetailedWeatherView.this.mWeatherClient.isOmniJawsEnabled()) {
                    return true;
                }
                DetailedWeatherView.this.startProgress();
                DetailedWeatherView.this.forceRefreshWeatherSettings();
                return true;
            }
        });
    }

    public void updateWeatherData(OmniJawsClient.WeatherInfo weatherInfo) {
        this.mProgressContainer.setVisibility(8);
        if (weatherInfo == null || !this.mWeatherClient.isOmniJawsEnabled()) {
            setErrorView();
            if (this.mWeatherClient.isOmniJawsEnabled()) {
                this.mEmptyViewImage.setImageResource(R.drawable.ic_qs_weather_default_on);
                this.mStatusMsg.setText(getResources().getString(R.string.omnijaws_service_unkown));
                return;
            } else {
                this.mEmptyViewImage.setImageResource(R.drawable.ic_qs_weather_default_off);
                this.mStatusMsg.setText(getResources().getString(R.string.omnijaws_service_disabled));
                return;
            }
        }
        this.mEmptyView.setVisibility(8);
        this.mWeatherLine.setVisibility(0);
        this.mWeatherCity.setText(weatherInfo.city);
        this.mProviderName.setText(weatherInfo.provider);
        Long l = weatherInfo.timeStamp;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateFormat.is24HourFormat(((FrameLayout) this).mContext) ? "HH:mm" : "hh:mm a");
        this.mWeatherTimestamp.setText(getResources().getString(R.string.omnijaws_service_last_update) + " " + simpleDateFormat.format(l));
        this.mWeatherData.setText(weatherInfo.windSpeed + " " + weatherInfo.windUnits + " " + weatherInfo.pinWheel + " - " + weatherInfo.humidity);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("EE");
        Calendar calendar = Calendar.getInstance();
        String str = simpleDateFormat2.format(new Date(calendar.getTimeInMillis()));
        this.mForecastImage0.setImageDrawable(overlay(((FrameLayout) this).mContext.getResources(), this.mWeatherClient.getWeatherConditionImage(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(0)).conditionCode)));
        this.mForecastTemp0.setText(formatTempString(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(0)).low, ((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(0)).high, weatherInfo.tempUnits));
        this.mForecastText0.setText(str);
        calendar.add(5, 1);
        String str2 = simpleDateFormat2.format(new Date(calendar.getTimeInMillis()));
        this.mForecastImage1.setImageDrawable(overlay(((FrameLayout) this).mContext.getResources(), this.mWeatherClient.getWeatherConditionImage(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(1)).conditionCode)));
        this.mForecastTemp1.setText(formatTempString(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(1)).low, ((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(1)).high, weatherInfo.tempUnits));
        this.mForecastText1.setText(str2);
        calendar.add(5, 1);
        String str3 = simpleDateFormat2.format(new Date(calendar.getTimeInMillis()));
        this.mForecastImage2.setImageDrawable(overlay(((FrameLayout) this).mContext.getResources(), this.mWeatherClient.getWeatherConditionImage(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(2)).conditionCode)));
        this.mForecastTemp2.setText(formatTempString(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(2)).low, ((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(2)).high, weatherInfo.tempUnits));
        this.mForecastText2.setText(str3);
        calendar.add(5, 1);
        String str4 = simpleDateFormat2.format(new Date(calendar.getTimeInMillis()));
        this.mForecastImage3.setImageDrawable(overlay(((FrameLayout) this).mContext.getResources(), this.mWeatherClient.getWeatherConditionImage(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(3)).conditionCode)));
        this.mForecastTemp3.setText(formatTempString(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(3)).low, ((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(3)).high, weatherInfo.tempUnits));
        this.mForecastText3.setText(str4);
        calendar.add(5, 1);
        String str5 = simpleDateFormat2.format(new Date(calendar.getTimeInMillis()));
        this.mForecastImage4.setImageDrawable(overlay(((FrameLayout) this).mContext.getResources(), this.mWeatherClient.getWeatherConditionImage(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(4)).conditionCode)));
        this.mForecastTemp4.setText(formatTempString(((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(4)).low, ((OmniJawsClient.DayForecast) weatherInfo.forecasts.get(4)).high, weatherInfo.tempUnits));
        this.mForecastText4.setText(str5);
        this.mCurrentImage.setImageDrawable(overlay(((FrameLayout) this).mContext.getResources(), this.mWeatherClient.getWeatherConditionImage(weatherInfo.conditionCode)));
        this.mCurrentTemp.setText(formatTempString(weatherInfo.temp, null, weatherInfo.tempUnits));
        this.mCurrentText.setText(((FrameLayout) this).mContext.getResources().getText(R.string.omnijaws_current_text));
    }

    private Drawable overlay(Resources resources, Drawable drawable) {
        if (drawable instanceof VectorDrawable) {
            drawable = applyTint(drawable);
        }
        Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(1, 2));
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmapCreateBitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        return new BitmapDrawable(resources, bitmapCreateBitmap);
    }

    private Drawable applyTint(Drawable drawable) {
        Drawable drawableMutate = drawable.mutate();
        drawableMutate.setTint(getTintColor());
        return drawableMutate;
    }

    private int getTintColor() {
        TypedArray typedArrayObtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.colorControlNormal});
        int color = typedArrayObtainStyledAttributes.getColor(0, 0);
        typedArrayObtainStyledAttributes.recycle();
        return color;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void forceRefreshWeatherSettings() {
        this.mWeatherClient.updateWeather();
    }

    private void setErrorView() {
        this.mEmptyView.setVisibility(0);
        this.mWeatherLine.setVisibility(8);
    }

    public void weatherError(int i) {
        this.mProgressContainer.setVisibility(8);
        setErrorView();
        if (i == 2) {
            this.mEmptyViewImage.setImageResource(R.drawable.ic_qs_weather_default_off);
            this.mStatusMsg.setText(getResources().getString(R.string.omnijaws_service_disabled));
        } else {
            this.mEmptyViewImage.setImageResource(R.drawable.ic_qs_weather_default_on);
            this.mStatusMsg.setText(getResources().getString(R.string.omnijaws_service_error_long));
        }
    }

    public void startProgress() {
        this.mEmptyView.setVisibility(8);
        this.mWeatherLine.setVisibility(8);
        this.mProgressContainer.setVisibility(0);
    }

    public void stopProgress() {
        this.mProgressContainer.setVisibility(8);
    }

    private static String formatTempString(String str, String str2, String str3) {
        if (str2 != null) {
            return str + "/" + str2 + str3;
        }
        return str + str3;
    }
}
