package com.android.systemui.accessibility;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.android.systemui.SystemUI;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class WindowMagnification extends SystemUI {
    private final Handler mHandler;
    private Configuration mLastConfiguration;
    private WindowMagnificationController mWindowMagnificationController;

    public WindowMagnification(Context context, Handler handler) {
        super(context);
        this.mHandler = handler;
        this.mLastConfiguration = new Configuration(context.getResources().getConfiguration());
    }

    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        int iDiff = configuration.diff(this.mLastConfiguration);
        if ((iDiff & LineageHardwareManager.FEATURE_AUTO_CONTRAST) == 0) {
            return;
        }
        this.mLastConfiguration.setTo(configuration);
        WindowMagnificationController windowMagnificationController = this.mWindowMagnificationController;
        if (windowMagnificationController != null) {
            windowMagnificationController.onConfigurationChanged(iDiff);
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("window_magnification"), true, new ContentObserver(this.mHandler) { // from class: com.android.systemui.accessibility.WindowMagnification.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                WindowMagnification.this.updateWindowMagnification();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWindowMagnification() {
        try {
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "window_magnification") != 0) {
                enableMagnification();
            } else {
                disableMagnification();
            }
        } catch (Settings.SettingNotFoundException unused) {
            disableMagnification();
        }
    }

    private void enableMagnification() {
        if (this.mWindowMagnificationController == null) {
            this.mWindowMagnificationController = new WindowMagnificationController(this.mContext, null);
        }
        this.mWindowMagnificationController.createWindowMagnification();
    }

    private void disableMagnification() {
        WindowMagnificationController windowMagnificationController = this.mWindowMagnificationController;
        if (windowMagnificationController != null) {
            windowMagnificationController.deleteWindowMagnification();
        }
        this.mWindowMagnificationController = null;
    }
}
