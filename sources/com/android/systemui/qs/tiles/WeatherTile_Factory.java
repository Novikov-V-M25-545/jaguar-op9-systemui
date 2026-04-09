package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class WeatherTile_Factory implements Factory<WeatherTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;

    public WeatherTile_Factory(Provider<QSHost> provider, Provider<ActivityStarter> provider2) {
        this.hostProvider = provider;
        this.activityStarterProvider = provider2;
    }

    @Override // javax.inject.Provider
    public WeatherTile get() {
        return provideInstance(this.hostProvider, this.activityStarterProvider);
    }

    public static WeatherTile provideInstance(Provider<QSHost> provider, Provider<ActivityStarter> provider2) {
        return new WeatherTile(provider.get(), provider2.get());
    }

    public static WeatherTile_Factory create(Provider<QSHost> provider, Provider<ActivityStarter> provider2) {
        return new WeatherTile_Factory(provider, provider2);
    }
}
