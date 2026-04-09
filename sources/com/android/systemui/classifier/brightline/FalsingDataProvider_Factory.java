package com.android.systemui.classifier.brightline;

import android.util.DisplayMetrics;
import com.android.systemui.statusbar.policy.BatteryController;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class FalsingDataProvider_Factory implements Factory<FalsingDataProvider> {
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<DisplayMetrics> displayMetricsProvider;

    public FalsingDataProvider_Factory(Provider<DisplayMetrics> provider, Provider<BatteryController> provider2) {
        this.displayMetricsProvider = provider;
        this.batteryControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public FalsingDataProvider get() {
        return provideInstance(this.displayMetricsProvider, this.batteryControllerProvider);
    }

    public static FalsingDataProvider provideInstance(Provider<DisplayMetrics> provider, Provider<BatteryController> provider2) {
        return new FalsingDataProvider(provider.get(), provider2.get());
    }

    public static FalsingDataProvider_Factory create(Provider<DisplayMetrics> provider, Provider<BatteryController> provider2) {
        return new FalsingDataProvider_Factory(provider, provider2);
    }
}
