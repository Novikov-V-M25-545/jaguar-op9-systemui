package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import lineageos.hardware.LineageHardwareManager;
import org.lineageos.internal.util.PackageManagerUtils;

/* loaded from: classes.dex */
public class ReadingModeTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent DISPLAY_SETTINGS = new Intent("android.settings.DISPLAY_SETTINGS");
    private LineageHardwareManager mHardware;
    private final QSTile.Icon mIcon;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483602;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    public ReadingModeTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_reader);
        this.mHardware = LineageHardwareManager.getInstance(this.mContext);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        this.mHardware.set(LineageHardwareManager.FEATURE_READING_ENHANCEMENT, !isReadingModeEnabled());
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return DISPLAY_SETTINGS;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return !isWellbeingEnabled() && this.mHardware.isSupported(LineageHardwareManager.FEATURE_READING_ENHANCEMENT);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean zIsReadingModeEnabled = isReadingModeEnabled();
        booleanState.value = zIsReadingModeEnabled;
        booleanState.icon = this.mIcon;
        if (zIsReadingModeEnabled) {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_reading_mode_on);
            booleanState.state = 2;
        } else {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_reading_mode_off);
            booleanState.state = 1;
        }
        booleanState.label = getTileLabel();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_reading_mode);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_reading_mode_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_reading_mode_changed_off);
    }

    private boolean isReadingModeEnabled() {
        return this.mHardware.get(LineageHardwareManager.FEATURE_READING_ENHANCEMENT);
    }

    private boolean isWellbeingEnabled() {
        Context context = this.mContext;
        return PackageManagerUtils.isAppEnabled(context, context.getString(android.R.string.config_customAdbWifiNetworkConfirmationComponent));
    }
}
