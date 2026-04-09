package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.crdroid.OmniJawsClient;
import com.android.internal.util.crdroid.Utils;
import com.android.systemui.R;
import com.android.systemui.omni.DetailedWeatherView;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class WeatherTile extends QSTileImpl<QSTile.BooleanState> implements OmniJawsClient.OmniJawsObserver {
    private static final String[] ALTERNATIVE_WEATHER_APPS = {"cz.martykan.forecastie", "com.accuweather.android", "com.wunderground.android.weather", "com.samruston.weather", "jp.miyavi.androiod.gnws"};
    private final ActivityStarter mActivityStarter;
    private WeatherDetailAdapter mDetailAdapter;
    private DetailedWeatherView mDetailedView;
    private boolean mEnabled;
    private final QSTile.Icon mIcon;
    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherData;
    private Drawable mWeatherImage;
    private String mWeatherLabel;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    public WeatherTile(QSHost qSHost, ActivityStarter activityStarter) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_weather_default_on);
        OmniJawsClient omniJawsClient = new OmniJawsClient(this.mContext);
        this.mWeatherClient = omniJawsClient;
        this.mEnabled = omniJawsClient.isOmniJawsEnabled();
        this.mActivityStarter = activityStarter;
        this.mDetailAdapter = (WeatherDetailAdapter) createDetailAdapter();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        OmniJawsClient omniJawsClient = this.mWeatherClient;
        if (omniJawsClient == null) {
            return;
        }
        this.mEnabled = omniJawsClient.isOmniJawsEnabled();
        if (z) {
            this.mWeatherClient.addObserver(this);
            queryAndUpdateWeather();
        } else {
            this.mWeatherClient.removeObserver(this);
        }
    }

    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    public void weatherError(int i) {
        if (i != 2) {
            this.mWeatherLabel = this.mContext.getResources().getString(R.string.omnijaws_service_error);
            refreshState();
            if (isShowingDetail()) {
                this.mDetailedView.weatherError(i);
            }
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        this.mWeatherClient.removeObserver(this);
        this.mWeatherClient.cleanupObserver();
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSecondaryClick() {
        showDetail(true);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mWeatherClient.isOmniJawsServiceInstalled();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (!((QSTile.BooleanState) this.mState).value) {
            if (!this.mWeatherClient.isOmniJawsSetupDone()) {
                this.mActivityStarter.postStartActivityDismissingKeyguard(this.mWeatherClient.getSettingsIntent(), 0);
            } else {
                this.mEnabled = true;
                this.mWeatherData = null;
                this.mWeatherClient.setOmniJawsEnabled(true);
            }
        } else {
            this.mEnabled = false;
            this.mWeatherData = null;
            this.mWeatherClient.setOmniJawsEnabled(false);
        }
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent launchIntentForPackage;
        PackageManager packageManager = this.mContext.getPackageManager();
        for (String str : ALTERNATIVE_WEATHER_APPS) {
            if (Utils.isPackageInstalled(this.mContext, str) && (launchIntentForPackage = packageManager.getLaunchIntentForPackage(str)) != null) {
                return launchIntentForPackage;
            }
        }
        if (Utils.isPackageInstalled(this.mContext, "com.google.android.googlequicksearchbox")) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse("dynact://velour/weather/ProxyActivity"));
            intent.setComponent(new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.velour.DynamicActivityTrampoline"));
            return intent;
        }
        return this.mWeatherClient.getSettingsIntent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.dualTarget = true;
        boolean z = this.mEnabled;
        booleanState.value = z;
        booleanState.state = z ? 2 : 1;
        booleanState.icon = this.mIcon;
        booleanState.label = this.mContext.getResources().getString(R.string.omnijaws_label_default);
        if (this.mEnabled) {
            Drawable drawable = this.mWeatherImage;
            if (drawable == null) {
                booleanState.secondaryLabel = this.mContext.getResources().getString(R.string.omnijaws_service_error_long);
                return;
            } else {
                booleanState.icon = new QSTileImpl.DrawableIcon(drawable);
                booleanState.secondaryLabel = this.mWeatherLabel;
                return;
            }
        }
        this.mWeatherImage = null;
        booleanState.secondaryLabel = this.mContext.getResources().getString(R.string.omnijaws_service_unkown);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getResources().getString(R.string.omnijaws_label_default);
    }

    private void queryAndUpdateWeather() {
        try {
            this.mWeatherImage = null;
            Resources resources = this.mContext.getResources();
            int i = R.string.omnijaws_label_default;
            this.mWeatherLabel = resources.getString(i);
            if (this.mEnabled) {
                this.mWeatherClient.queryWeather();
                OmniJawsClient.WeatherInfo weatherInfo = this.mWeatherClient.getWeatherInfo();
                this.mWeatherData = weatherInfo;
                if (weatherInfo != null) {
                    Drawable weatherConditionImagefromPack = this.mWeatherClient.getWeatherConditionImagefromPack(weatherInfo.conditionCode, true);
                    this.mWeatherImage = weatherConditionImagefromPack;
                    this.mWeatherImage = weatherConditionImagefromPack.mutate();
                    this.mWeatherLabel = this.mWeatherData.temp + this.mWeatherData.tempUnits;
                } else {
                    this.mWeatherLabel = this.mContext.getResources().getString(R.string.omnijaws_service_unkown);
                }
            } else {
                this.mWeatherLabel = this.mContext.getResources().getString(i);
            }
        } catch (Exception unused) {
            this.mWeatherLabel = this.mContext.getResources().getString(R.string.omnijaws_label_default);
        }
        refreshState();
        if (isShowingDetail()) {
            this.mDetailedView.updateWeatherData(this.mWeatherData);
        }
    }

    protected DetailAdapter createDetailAdapter() {
        return new WeatherDetailAdapter();
    }

    /* JADX INFO: Access modifiers changed from: private */
    class WeatherDetailAdapter implements DetailAdapter {
        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 4000;
        }

        private WeatherDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) WeatherTile.this).mContext.getString(R.string.omnijaws_detail_header);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(WeatherTile.this.mEnabled);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(((QSTileImpl) WeatherTile.this).mContext, getMetricsCategory());
            WeatherTile.this.mWeatherData = null;
            WeatherTile.this.mEnabled = z;
            WeatherTile.this.mWeatherClient.setOmniJawsEnabled(z);
            if (z) {
                WeatherTile.this.mDetailedView.startProgress();
            } else {
                WeatherTile.this.mDetailedView.stopProgress();
                WeatherTile.this.mDetailedView.post(new Runnable() { // from class: com.android.systemui.qs.tiles.WeatherTile$WeatherDetailAdapter$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$setToggleState$0();
                    }
                });
            }
            WeatherTile.this.refreshState();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$setToggleState$0() {
            WeatherTile.this.mDetailedView.updateWeatherData(null);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return WeatherTile.this.mWeatherClient.getSettingsIntent();
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            WeatherTile.this.mDetailedView = (DetailedWeatherView) LayoutInflater.from(context).inflate(R.layout.detailed_weather_view, viewGroup, false);
            WeatherTile.this.mDetailedView.setWeatherClient(WeatherTile.this.mWeatherClient);
            WeatherTile.this.mDetailedView.post(new Runnable() { // from class: com.android.systemui.qs.tiles.WeatherTile$WeatherDetailAdapter$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$createDetailView$1();
                }
            });
            return WeatherTile.this.mDetailedView;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$createDetailView$1() {
            try {
                WeatherTile.this.mDetailedView.updateWeatherData(WeatherTile.this.mWeatherData);
            } catch (Exception unused) {
            }
        }
    }
}
