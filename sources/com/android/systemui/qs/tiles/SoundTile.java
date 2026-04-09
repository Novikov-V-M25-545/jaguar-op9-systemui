package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ZenModeController;

/* loaded from: classes.dex */
public class SoundTile extends QSTileImpl<QSTile.BooleanState> {
    private final AudioManager mAudioManager;
    private boolean mListening;
    private BroadcastReceiver mReceiver;
    private final ZenModeController mZenController;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    public SoundTile(QSHost qSHost) {
        super(qSHost);
        this.mListening = false;
        this.mZenController = (ZenModeController) Dependency.get(ZenModeController.class);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.SoundTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                SoundTile.this.refreshState();
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            return;
        }
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        updateState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleLongClick() {
        this.mAudioManager.adjustVolume(0, 1);
    }

    private void updateState() {
        int ringerModeInternal = this.mAudioManager.getRingerModeInternal();
        if (ringerModeInternal == 0) {
            this.mZenController.setZen(0, null, this.TAG);
            this.mAudioManager.setRingerModeInternal(2);
        } else if (ringerModeInternal == 1) {
            this.mZenController.setZen(2, null, this.TAG);
        } else {
            if (ringerModeInternal != 2) {
                return;
            }
            this.mAudioManager.setRingerModeInternal(1);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_sound_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int ringerModeInternal = this.mAudioManager.getRingerModeInternal();
        if (ringerModeInternal == 0) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_ringer_silent);
            Context context = this.mContext;
            int i = R.string.quick_settings_sound_dnd;
            booleanState.label = context.getString(i);
            booleanState.contentDescription = this.mContext.getString(i);
        } else if (ringerModeInternal == 1) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_ringer_vibrate);
            Context context2 = this.mContext;
            int i2 = R.string.quick_settings_sound_vibrate;
            booleanState.label = context2.getString(i2);
            booleanState.contentDescription = this.mContext.getString(i2);
        } else if (ringerModeInternal == 2) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_ringer_audible);
            Context context3 = this.mContext;
            int i3 = R.string.quick_settings_sound_ring;
            booleanState.label = context3.getString(i3);
            booleanState.contentDescription = this.mContext.getString(i3);
        }
        booleanState.state = 1;
    }
}
