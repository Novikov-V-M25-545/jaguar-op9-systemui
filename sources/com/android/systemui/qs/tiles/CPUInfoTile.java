package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SystemSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class CPUInfoTile extends QSTileImpl<QSTile.BooleanState> {
    private final QSTile.Icon mIcon;
    private final SystemSetting mSetting;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    public CPUInfoTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_cpuinfo);
        this.mSetting = new SystemSetting(this.mContext, this.mHandler, "show_cpu_overlay") { // from class: com.android.systemui.qs.tiles.CPUInfoTile.1
            @Override // com.android.systemui.qs.SystemSetting
            protected void handleValueChanged(int i, boolean z) {
                CPUInfoTile.this.handleRefreshState(Integer.valueOf(i));
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        this.mSetting.setValue(!((QSTile.BooleanState) this.mState).value ? 1 : 0);
        refreshState();
        toggleState();
    }

    protected void toggleState() {
        Intent className = new Intent().setClassName("com.android.systemui", "com.android.systemui.CPUInfoService");
        if (this.mSetting.getValue() == 0) {
            this.mContext.stopService(className);
        } else {
            this.mContext.startService(className);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        SystemSetting systemSetting = this.mSetting;
        if (systemSetting == null) {
            return;
        }
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : systemSetting.getValue()) != 0;
        booleanState.value = z;
        Context context = this.mContext;
        int i = R.string.quick_settings_cpuinfo_label;
        booleanState.label = context.getString(i);
        booleanState.icon = this.mIcon;
        booleanState.contentDescription = this.mContext.getString(i);
        if (z) {
            booleanState.state = 2;
        } else {
            booleanState.state = 1;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_cpuinfo_label);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        return this.mContext.getString(R.string.quick_settings_cpuinfo_label);
    }
}
