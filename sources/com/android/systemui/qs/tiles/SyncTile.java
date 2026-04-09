package com.android.systemui.qs.tiles;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class SyncTile extends QSTileImpl<QSTile.BooleanState> {
    private final QSTile.Icon mIcon;
    private boolean mListening;
    private SyncStatusObserver mSyncObserver;
    private Object mSyncObserverHandle;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483611;
    }

    public SyncTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_sync);
        this.mSyncObserverHandle = null;
        this.mSyncObserver = new SyncStatusObserver() { // from class: com.android.systemui.qs.tiles.SyncTile.1
            @Override // android.content.SyncStatusObserver
            public void onStatusChanged(int i) {
                ((QSTileImpl) SyncTile.this).mHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.SyncTile.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        SyncTile.this.refreshState();
                    }
                });
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        ContentResolver.setMasterSyncAutomatically(!((QSTile.BooleanState) this.mState).value);
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.settings.SYNC_SETTINGS");
        intent.addCategory("android.intent.category.DEFAULT");
        return intent;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = ContentResolver.getMasterSyncAutomatically();
        booleanState.label = this.mContext.getString(R.string.quick_settings_sync_label);
        booleanState.icon = this.mIcon;
        if (booleanState.value) {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_sync_on);
            booleanState.state = 2;
        } else {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_sync_off);
            booleanState.state = 1;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_sync_label);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_sync_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_sync_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mSyncObserverHandle = ContentResolver.addStatusChangeListener(1, this.mSyncObserver);
        } else {
            ContentResolver.removeStatusChangeListener(this.mSyncObserverHandle);
            this.mSyncObserverHandle = null;
        }
    }
}
