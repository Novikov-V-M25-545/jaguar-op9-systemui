package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SystemSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class GamingModeTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent GAMING_MODE_SETTINGS = new Intent("android.settings.GAMING_MODE_SETTINGS");
    private final SystemSetting mGamingModeActivated;
    private final QSTile.Icon mIcon;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return true;
    }

    public GamingModeTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_gaming_mode);
        this.mGamingModeActivated = new SystemSetting(this.mContext, this.mHandler, "gaming_mode_active") { // from class: com.android.systemui.qs.tiles.GamingModeTile.1
            @Override // com.android.systemui.qs.SystemSetting
            protected void handleValueChanged(int i, boolean z) {
                GamingModeTile.this.handleRefreshState(Integer.valueOf(i));
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        boolean z = Settings.System.getInt(this.mContext.getContentResolver(), "gaming_mode_enabled", 0) == 1;
        this.mHost.collapsePanels();
        if (z) {
            this.mGamingModeActivated.setValue(1 ^ (((QSTile.BooleanState) this.mState).value ? 1 : 0));
        } else {
            Context context = this.mContext;
            SysUIToast.makeText(context, context.getString(R.string.gaming_mode_not_enabled), 1).show();
        }
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return GAMING_MODE_SETTINGS;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_gaming_mode_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mGamingModeActivated.getValue()) == 1;
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R.string.quick_settings_gaming_mode_label);
        booleanState.icon = this.mIcon;
        booleanState.state = z ? 2 : 1;
    }
}
