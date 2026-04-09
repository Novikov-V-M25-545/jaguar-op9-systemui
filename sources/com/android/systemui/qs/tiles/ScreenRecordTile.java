package com.android.systemui.qs.tiles;

import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import androidx.lifecycle.LifecycleOwner;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;

/* loaded from: classes.dex */
public class ScreenRecordTile extends QSTileImpl<QSTile.BooleanState> implements RecordingController.RecordingStateChangeCallback {
    private Callback mCallback;
    private RecordingController mController;
    private KeyguardDismissUtil mKeyguardDismissUtil;
    private long mMillisUntilFinished;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 0;
    }

    public ScreenRecordTile(QSHost qSHost, RecordingController recordingController, KeyguardDismissUtil keyguardDismissUtil) {
        super(qSHost);
        this.mMillisUntilFinished = 0L;
        Callback callback = new Callback();
        this.mCallback = callback;
        this.mController = recordingController;
        recordingController.observe((LifecycleOwner) this, (ScreenRecordTile) callback);
        this.mKeyguardDismissUtil = keyguardDismissUtil;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.label = this.mContext.getString(R.string.quick_settings_screen_record_label);
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() throws PendingIntent.CanceledException {
        if (this.mController.isStarting()) {
            cancelCountdown();
        } else if (this.mController.isRecording()) {
            stopRecording();
        } else {
            this.mUiHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.ScreenRecordTile$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleClick$0();
                }
            });
        }
        refreshState();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean zIsStarting = this.mController.isStarting();
        boolean zIsRecording = this.mController.isRecording();
        booleanState.value = zIsRecording || zIsStarting;
        booleanState.state = (zIsRecording || zIsStarting) ? 2 : 1;
        booleanState.label = this.mContext.getString(R.string.quick_settings_screen_record_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_screenrecord);
        if (zIsRecording) {
            booleanState.secondaryLabel = this.mContext.getString(R.string.quick_settings_screen_record_stop);
        } else if (zIsStarting) {
            booleanState.secondaryLabel = String.format("%d...", Integer.valueOf((int) Math.floorDiv(this.mMillisUntilFinished + 500, 1000L)));
        } else {
            booleanState.secondaryLabel = this.mContext.getString(R.string.quick_settings_screen_record_start);
        }
        booleanState.contentDescription = TextUtils.isEmpty(booleanState.secondaryLabel) ? booleanState.label : TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_screen_record_label);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: showPrompt, reason: merged with bridge method [inline-methods] */
    public void lambda$handleClick$0() {
        getHost().collapsePanels();
        final Intent promptIntent = this.mController.getPromptIntent();
        this.mKeyguardDismissUtil.executeWhenUnlocked(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.qs.tiles.ScreenRecordTile$$ExternalSyntheticLambda0
            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return this.f$0.lambda$showPrompt$1(promptIntent);
            }
        }, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$showPrompt$1(Intent intent) {
        this.mContext.startActivity(intent);
        return false;
    }

    private void cancelCountdown() {
        Log.d("ScreenRecordTile", "Cancelling countdown");
        this.mController.cancelCountdown();
    }

    private void stopRecording() throws PendingIntent.CanceledException {
        this.mController.stopRecording();
    }

    private final class Callback implements RecordingController.RecordingStateChangeCallback {
        private Callback() {
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onCountdown(long j) {
            ScreenRecordTile.this.mMillisUntilFinished = j;
            ScreenRecordTile.this.refreshState();
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onCountdownEnd() {
            ScreenRecordTile.this.refreshState();
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onRecordingStart() {
            ScreenRecordTile.this.refreshState();
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onRecordingEnd() {
            ScreenRecordTile.this.refreshState();
        }
    }
}
