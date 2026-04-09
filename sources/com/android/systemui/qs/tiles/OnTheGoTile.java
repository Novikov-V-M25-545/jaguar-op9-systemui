package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import com.android.internal.util.crdroid.OnTheGoUtils;
import com.android.systemui.R;
import com.android.systemui.crdroid.onthego.OnTheGoService;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class OnTheGoTile extends QSTileImpl<QSTile.BooleanState> {
    private final QSTile.Icon mIcon;
    private boolean mIsEnabled;

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

    public OnTheGoTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_onthego);
        this.mIsEnabled = isOnTheGoEnabled();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        toggleService();
    }

    protected void toggleService() {
        ComponentName componentName = new ComponentName("com.android.systemui", "com.android.systemui.crdroid.onthego.OnTheGoService");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        if (isOnTheGoEnabled()) {
            intent.setAction("stop");
            this.mIsEnabled = false;
        } else {
            intent.setAction("start");
            this.mIsEnabled = true;
        }
        this.mContext.startService(intent);
        refreshState();
    }

    protected boolean isOnTheGoEnabled() {
        return OnTheGoUtils.isServiceRunning(this.mContext, OnTheGoService.class.getName());
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_onthego_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = this.mIsEnabled;
        booleanState.value = z;
        booleanState.state = z ? 2 : 1;
        String string = this.mContext.getString(R.string.quick_settings_onthego_label);
        booleanState.label = string;
        booleanState.icon = this.mIcon;
        booleanState.contentDescription = string;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_onthego_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_onthego_off);
    }
}
