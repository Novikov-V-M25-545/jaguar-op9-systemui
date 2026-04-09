package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

/* loaded from: classes.dex */
public class BootReceiver extends BroadcastReceiver {
    private Context mContext;
    private Handler mHandler = new Handler();
    private SettingsObserver mSettingsObserver;

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            BootReceiver.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_cpu_overlay"), false, this);
            BootReceiver.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_fps_overlay"), false, this);
            update();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            update();
        }

        public void update() {
            Intent intent = new Intent(BootReceiver.this.mContext, (Class<?>) CPUInfoService.class);
            Intent intent2 = new Intent(BootReceiver.this.mContext, (Class<?>) FPSInfoService.class);
            if (Settings.System.getInt(BootReceiver.this.mContext.getContentResolver(), "show_cpu_overlay", 0) != 0) {
                BootReceiver.this.mContext.startService(intent);
            } else {
                BootReceiver.this.mContext.stopService(intent);
            }
            if (Settings.System.getInt(BootReceiver.this.mContext.getContentResolver(), "show_fps_overlay", 0) != 0) {
                BootReceiver.this.mContext.startService(intent2);
            } else {
                BootReceiver.this.mContext.stopService(intent2);
            }
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            this.mContext = context;
            if (this.mSettingsObserver == null) {
                SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
                this.mSettingsObserver = settingsObserver;
                settingsObserver.observe();
            }
            if (Settings.System.getInt(this.mContext.getContentResolver(), "show_cpu_overlay", 0) != 0) {
                this.mContext.startService(new Intent(this.mContext, (Class<?>) CPUInfoService.class));
            }
            if (Settings.System.getInt(this.mContext.getContentResolver(), "show_fps_overlay", 0) != 0) {
                this.mContext.startService(new Intent(this.mContext, (Class<?>) FPSInfoService.class));
            }
        } catch (Exception e) {
            Log.e("SystemUIBootReceiver", "Can't start load average service", e);
        }
    }
}
