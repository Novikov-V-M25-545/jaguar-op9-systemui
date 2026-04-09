package com.android.systemui.dagger;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardLiftController;
import com.android.systemui.util.sensors.AsyncSensorManager;

/* loaded from: classes.dex */
public abstract class SystemUIModule {
    static KeyguardLiftController provideKeyguardLiftController(Context context, StatusBarStateController statusBarStateController, AsyncSensorManager asyncSensorManager, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.biometrics.face")) {
            return new KeyguardLiftController(statusBarStateController, asyncSensorManager, keyguardUpdateMonitor, dumpManager);
        }
        return null;
    }

    static SysUiState provideSysUiState() {
        return new SysUiState();
    }
}
