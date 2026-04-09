package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class RebootTile extends QSTileImpl<QSTile.BooleanState> {
    private int mRebootToRecovery;

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

    public RebootTile(QSHost qSHost) {
        super(qSHost);
        this.mRebootToRecovery = 0;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        int i = this.mRebootToRecovery;
        if (i == 0) {
            this.mRebootToRecovery = 1;
        } else if (i == 1) {
            this.mRebootToRecovery = 2;
        } else {
            this.mRebootToRecovery = 0;
        }
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleLongClick() {
        this.mHost.collapsePanels();
        new Handler().postDelayed(new Runnable() { // from class: com.android.systemui.qs.tiles.RebootTile.1
            @Override // java.lang.Runnable
            public void run() {
                PowerManager powerManager = (PowerManager) ((QSTileImpl) RebootTile.this).mContext.getSystemService("power");
                if (RebootTile.this.mRebootToRecovery != 1) {
                    if (RebootTile.this.mRebootToRecovery == 2) {
                        powerManager.shutdown(false, "userrequested", false);
                        return;
                    } else {
                        powerManager.reboot("");
                        return;
                    }
                }
                powerManager.rebootCustom("recovery");
            }
        }, 500L);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_reboot_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i = this.mRebootToRecovery;
        if (i == 1) {
            booleanState.label = this.mContext.getString(R.string.quick_settings_reboot_recovery_label);
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_reboot_recovery);
        } else if (i == 2) {
            booleanState.label = this.mContext.getString(R.string.quick_settings_poweroff_label);
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_poweroff);
        } else {
            booleanState.label = this.mContext.getString(R.string.quick_settings_reboot_label);
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_reboot);
        }
        booleanState.state = 1;
    }
}
