package com.android.systemui.qs.dagger;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import com.android.systemui.qs.AutoAddTracker;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.AutoTileManager;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;

/* loaded from: classes.dex */
public interface QSModule {
    static AutoTileManager provideAutoTileManager(Context context, AutoAddTracker.Builder builder, QSTileHost qSTileHost, Handler handler, HotspotController hotspotController, DataSaverController dataSaverController, ManagedProfileController managedProfileController, NightDisplayListener nightDisplayListener, CastController castController) throws Resources.NotFoundException {
        AutoTileManager autoTileManager = new AutoTileManager(context, builder, qSTileHost, handler, hotspotController, dataSaverController, managedProfileController, nightDisplayListener, castController);
        autoTileManager.init();
        return autoTileManager;
    }
}
