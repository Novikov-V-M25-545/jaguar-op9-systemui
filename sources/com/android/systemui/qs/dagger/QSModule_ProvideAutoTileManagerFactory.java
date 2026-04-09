package com.android.systemui.qs.dagger;

import android.content.Context;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import com.android.systemui.qs.AutoAddTracker;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.AutoTileManager;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class QSModule_ProvideAutoTileManagerFactory implements Factory<AutoTileManager> {
    private final Provider<AutoAddTracker.Builder> autoAddTrackerBuilderProvider;
    private final Provider<CastController> castControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DataSaverController> dataSaverControllerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<QSTileHost> hostProvider;
    private final Provider<HotspotController> hotspotControllerProvider;
    private final Provider<ManagedProfileController> managedProfileControllerProvider;
    private final Provider<NightDisplayListener> nightDisplayListenerProvider;

    public QSModule_ProvideAutoTileManagerFactory(Provider<Context> provider, Provider<AutoAddTracker.Builder> provider2, Provider<QSTileHost> provider3, Provider<Handler> provider4, Provider<HotspotController> provider5, Provider<DataSaverController> provider6, Provider<ManagedProfileController> provider7, Provider<NightDisplayListener> provider8, Provider<CastController> provider9) {
        this.contextProvider = provider;
        this.autoAddTrackerBuilderProvider = provider2;
        this.hostProvider = provider3;
        this.handlerProvider = provider4;
        this.hotspotControllerProvider = provider5;
        this.dataSaverControllerProvider = provider6;
        this.managedProfileControllerProvider = provider7;
        this.nightDisplayListenerProvider = provider8;
        this.castControllerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public AutoTileManager get() {
        return provideInstance(this.contextProvider, this.autoAddTrackerBuilderProvider, this.hostProvider, this.handlerProvider, this.hotspotControllerProvider, this.dataSaverControllerProvider, this.managedProfileControllerProvider, this.nightDisplayListenerProvider, this.castControllerProvider);
    }

    public static AutoTileManager provideInstance(Provider<Context> provider, Provider<AutoAddTracker.Builder> provider2, Provider<QSTileHost> provider3, Provider<Handler> provider4, Provider<HotspotController> provider5, Provider<DataSaverController> provider6, Provider<ManagedProfileController> provider7, Provider<NightDisplayListener> provider8, Provider<CastController> provider9) {
        return proxyProvideAutoTileManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static QSModule_ProvideAutoTileManagerFactory create(Provider<Context> provider, Provider<AutoAddTracker.Builder> provider2, Provider<QSTileHost> provider3, Provider<Handler> provider4, Provider<HotspotController> provider5, Provider<DataSaverController> provider6, Provider<ManagedProfileController> provider7, Provider<NightDisplayListener> provider8, Provider<CastController> provider9) {
        return new QSModule_ProvideAutoTileManagerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }

    public static AutoTileManager proxyProvideAutoTileManager(Context context, AutoAddTracker.Builder builder, QSTileHost qSTileHost, Handler handler, HotspotController hotspotController, DataSaverController dataSaverController, ManagedProfileController managedProfileController, NightDisplayListener nightDisplayListener, CastController castController) {
        return (AutoTileManager) Preconditions.checkNotNull(QSModule.provideAutoTileManager(context, builder, qSTileHost, handler, hotspotController, dataSaverController, managedProfileController, nightDisplayListener, castController), "Cannot return null from a non-@Nullable @Provides method");
    }
}
