package com.android.systemui.qs.tiles;

import android.R;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/* loaded from: classes.dex */
public class SleepModeTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent SLEEP_MODE_SETTINGS;
    private static final ComponentName SLEEP_MODE_SETTING_COMPONENT;
    private final QSTile.Icon mIcon;
    private boolean mIsTurningOn;
    private final SecureSetting mSetting;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSetListening(boolean z) {
    }

    static {
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$SleepModeActivity");
        SLEEP_MODE_SETTING_COMPONENT = componentName;
        SLEEP_MODE_SETTINGS = new Intent().setComponent(componentName);
    }

    public SleepModeTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_menu_selectall_holo_light);
        this.mIsTurningOn = false;
        this.mSetting = new SecureSetting(this.mContext, this.mHandler, "sleep_mode_enabled") { // from class: com.android.systemui.qs.tiles.SleepModeTile.1
            @Override // com.android.systemui.qs.SecureSetting
            protected void handleValueChanged(int i, boolean z) {
                SleepModeTile.this.handleRefreshState(Integer.valueOf(i));
            }
        };
        new SettingsObserver(new Handler()).observe();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if (this.mIsTurningOn) {
            return;
        }
        this.mIsTurningOn = true;
        setEnabled(true ^ ((QSTile.BooleanState) this.mState).value);
        refreshState();
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.qs.tiles.SleepModeTile.2
            @Override // java.lang.Runnable
            public void run() {
                SleepModeTile.this.mIsTurningOn = false;
            }
        }, 1500L);
    }

    private void setEnabled(boolean z) {
        Settings.Secure.putInt(this.mContext.getContentResolver(), "sleep_mode_enabled", z ? 1 : 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0;
        int intForUser = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "sleep_mode_auto_mode", 0, -2);
        boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "sleep_mode_enabled", 0, -2) == 1;
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sleep_mode_auto_time", -2);
        if (stringForUser == null || stringForUser.equals("")) {
            stringForUser = "20:00,07:00";
        }
        String[] strArrSplit = stringForUser.split(",", 0);
        DateTimeFormatter dateTimeFormatterOfPattern = DateTimeFormatter.ofPattern(DateFormat.is24HourFormat(this.mContext) ? "HH:mm" : "h:mm a");
        DateTimeFormatter dateTimeFormatterOfPattern2 = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(strArrSplit[0], dateTimeFormatterOfPattern2);
        LocalTime localTime2 = LocalTime.parse(strArrSplit[1], dateTimeFormatterOfPattern2);
        booleanState.value = z;
        booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_sleep_mode_label);
        booleanState.icon = this.mIcon;
        booleanState.contentDescription = TextUtils.isEmpty(booleanState.secondaryLabel) ? booleanState.label : TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
        if (intForUser == 1) {
            Resources resources = this.mContext.getResources();
            if (z2) {
                i = com.android.systemui.R.string.quick_settings_night_secondary_label_until_sunrise;
            } else {
                i = com.android.systemui.R.string.quick_settings_night_secondary_label_on_at_sunset;
            }
            booleanState.secondaryLabel = resources.getString(i);
        } else if (intForUser != 2) {
            if (intForUser != 3) {
                if (intForUser != 4) {
                    booleanState.secondaryLabel = null;
                } else if (z2) {
                    booleanState.secondaryLabel = this.mContext.getResources().getString(com.android.systemui.R.string.quick_settings_night_secondary_label_until_sunrise);
                } else {
                    booleanState.secondaryLabel = this.mContext.getResources().getString(com.android.systemui.R.string.quick_settings_night_secondary_label_on_at, localTime.format(dateTimeFormatterOfPattern));
                }
            } else if (z2) {
                booleanState.secondaryLabel = this.mContext.getResources().getString(com.android.systemui.R.string.quick_settings_secondary_label_until, localTime2.format(dateTimeFormatterOfPattern));
            } else {
                booleanState.secondaryLabel = this.mContext.getResources().getString(com.android.systemui.R.string.quick_settings_night_secondary_label_on_at_sunset);
            }
        } else if (z2) {
            booleanState.secondaryLabel = this.mContext.getResources().getString(com.android.systemui.R.string.quick_settings_secondary_label_until, localTime2.format(dateTimeFormatterOfPattern));
        } else {
            booleanState.secondaryLabel = this.mContext.getResources().getString(com.android.systemui.R.string.quick_settings_night_secondary_label_on_at, localTime.format(dateTimeFormatterOfPattern));
        }
        booleanState.state = booleanState.value ? 2 : 1;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return SLEEP_MODE_SETTINGS;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver contentResolver = ((QSTileImpl) SleepModeTile.this).mContext.getContentResolver();
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("sleep_mode_enabled"), false, this, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("sleep_mode_auto_mode"), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            SleepModeTile.this.refreshState();
        }
    }
}
