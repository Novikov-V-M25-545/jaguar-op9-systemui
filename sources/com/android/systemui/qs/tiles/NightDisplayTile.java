package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.NightDisplayListener;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.LocationController;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class NightDisplayTile extends QSTileImpl<QSTile.BooleanState> implements NightDisplayListener.Callback {
    private boolean mIsListening;
    private NightDisplayListener mListener;
    private final LocationController mLocationController;
    private ColorDisplayManager mManager;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 491;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return true;
    }

    public NightDisplayTile(QSHost qSHost, LocationController locationController) {
        super(qSHost);
        this.mLocationController = locationController;
        this.mManager = (ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class);
        this.mListener = new NightDisplayListener(this.mContext, new Handler(Looper.myLooper()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        if ("1".equals(Settings.Global.getString(this.mContext.getContentResolver(), "night_display_forced_auto_mode_available")) && this.mManager.getNightDisplayAutoModeRaw() == -1) {
            this.mManager.setNightDisplayAutoMode(1);
            Log.i("NightDisplayTile", "Enrolled in forced night display auto mode");
        }
        this.mManager.setNightDisplayActivated(!((QSTile.BooleanState) this.mState).value);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleUserSwitch(int i) {
        if (this.mIsListening) {
            this.mListener.setCallback((NightDisplayListener.Callback) null);
        }
        this.mManager = (ColorDisplayManager) getHost().getUserContext().getSystemService(ColorDisplayManager.class);
        NightDisplayListener nightDisplayListener = new NightDisplayListener(this.mContext, i, new Handler(Looper.myLooper()));
        this.mListener = nightDisplayListener;
        if (this.mIsListening) {
            nightDisplayListener.setCallback(this);
        }
        super.handleUserSwitch(i);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mManager.isNightDisplayActivated();
        booleanState.label = this.mContext.getString(R.string.quick_settings_night_display_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(android.R.drawable.ic_menu_find);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        boolean z = booleanState.value;
        booleanState.state = z ? 2 : 1;
        String secondaryLabel = getSecondaryLabel(z);
        booleanState.secondaryLabel = secondaryLabel;
        booleanState.contentDescription = TextUtils.isEmpty(secondaryLabel) ? booleanState.label : TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
    }

    private String getSecondaryLabel(boolean z) {
        LocalTime nightDisplayCustomStartTime;
        int i;
        int nightDisplayAutoMode = this.mManager.getNightDisplayAutoMode();
        if (nightDisplayAutoMode != 1) {
            if (nightDisplayAutoMode != 2 || !this.mLocationController.isLocationEnabled()) {
                return null;
            }
            if (z) {
                return this.mContext.getString(R.string.quick_settings_night_secondary_label_until_sunrise);
            }
            return this.mContext.getString(R.string.quick_settings_night_secondary_label_on_at_sunset);
        }
        if (z) {
            nightDisplayCustomStartTime = this.mManager.getNightDisplayCustomEndTime();
            i = R.string.quick_settings_secondary_label_until;
        } else {
            nightDisplayCustomStartTime = this.mManager.getNightDisplayCustomStartTime();
            i = R.string.quick_settings_night_secondary_label_on_at;
        }
        Calendar calendar = Calendar.getInstance();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this.mContext);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeZone(timeFormat.getTimeZone());
        calendar.set(11, nightDisplayCustomStartTime.getHour());
        calendar.set(12, nightDisplayCustomStartTime.getMinute());
        calendar.set(13, 0);
        calendar.set(14, 0);
        return this.mContext.getString(i, timeFormat.format(calendar.getTime()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).addTaggedData(1311, Integer.valueOf(this.mManager.getNightDisplayAutoModeRaw()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.NIGHT_DISPLAY_SETTINGS");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mIsListening = z;
        if (z) {
            this.mListener.setCallback(this);
            refreshState();
        } else {
            this.mListener.setCallback((NightDisplayListener.Callback) null);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_night_display_label);
    }

    public void onActivated(boolean z) {
        refreshState();
    }
}
