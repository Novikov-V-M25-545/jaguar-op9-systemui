package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.provider.Settings;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class HeadsUpTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent NOTIFICATION_SETTINGS = new Intent("android.settings.NOTIFICATION_SETTINGS");
    private final QSTile.Icon mIcon;
    private final GlobalSetting mSetting;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483608;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    public HeadsUpTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_heads_up);
        this.mSetting = new GlobalSetting(this.mContext, this.mHandler, "heads_up_notifications_enabled") { // from class: com.android.systemui.qs.tiles.HeadsUpTile.1
            @Override // com.android.systemui.qs.GlobalSetting
            protected void handleValueChanged(int i) {
                HeadsUpTile.this.handleRefreshState(Integer.valueOf(i));
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        setEnabled(!((QSTile.BooleanState) this.mState).value);
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return NOTIFICATION_SETTINGS;
    }

    private void setEnabled(boolean z) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "heads_up_notifications_enabled", z ? 1 : 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0;
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R.string.quick_settings_heads_up_label);
        booleanState.icon = this.mIcon;
        if (z) {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_heads_up_on);
            booleanState.state = 2;
        } else {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_heads_up_off);
            booleanState.state = 1;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_heads_up_label);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_heads_up_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_heads_up_changed_off);
    }
}
