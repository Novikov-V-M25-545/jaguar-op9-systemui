package com.android.systemui.qs.tiles;

import android.hardware.SensorPrivacyManager;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class SensorPrivacyTile_Factory implements Factory<SensorPrivacyTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<SensorPrivacyManager> sensorPrivacyManagerProvider;

    public SensorPrivacyTile_Factory(Provider<QSHost> provider, Provider<SensorPrivacyManager> provider2, Provider<ActivityStarter> provider3) {
        this.hostProvider = provider;
        this.sensorPrivacyManagerProvider = provider2;
        this.activityStarterProvider = provider3;
    }

    @Override // javax.inject.Provider
    public SensorPrivacyTile get() {
        return provideInstance(this.hostProvider, this.sensorPrivacyManagerProvider, this.activityStarterProvider);
    }

    public static SensorPrivacyTile provideInstance(Provider<QSHost> provider, Provider<SensorPrivacyManager> provider2, Provider<ActivityStarter> provider3) {
        return new SensorPrivacyTile(provider.get(), provider2.get(), provider3.get());
    }

    public static SensorPrivacyTile_Factory create(Provider<QSHost> provider, Provider<SensorPrivacyManager> provider2, Provider<ActivityStarter> provider3) {
        return new SensorPrivacyTile_Factory(provider, provider2, provider3);
    }
}
