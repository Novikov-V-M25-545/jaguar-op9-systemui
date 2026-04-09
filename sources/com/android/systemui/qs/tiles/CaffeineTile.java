package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class CaffeineTile extends QSTileImpl<QSTile.BooleanState> {
    private static int[] DURATIONS = {300, 600, 1800, -1};
    private static final int INFINITE_DURATION_INDEX;
    private CountDownTimer mCountdownTimer;
    private int mDuration;
    private final QSTile.Icon mIcon;
    public long mLastClickTime;
    private final Receiver mReceiver;
    private int mSecondsRemaining;
    private final PowerManager.WakeLock mWakeLock;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483606;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    static {
        INFINITE_DURATION_INDEX = r0.length - 1;
    }

    public CaffeineTile(QSHost qSHost) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_caffeine);
        this.mCountdownTimer = null;
        this.mLastClickTime = -1L;
        Receiver receiver = new Receiver();
        this.mReceiver = receiver;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService(PowerManager.class)).newWakeLock(26, "CaffeineTile");
        receiver.init();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        super.handleDestroy();
        stopCountDown();
        this.mReceiver.destroy();
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (this.mWakeLock.isHeld() && this.mLastClickTime != -1 && SystemClock.elapsedRealtime() - this.mLastClickTime < 5000) {
            int i = this.mDuration + 1;
            this.mDuration = i;
            if (i >= DURATIONS.length) {
                this.mDuration = -1;
                stopCountDown();
                if (this.mWakeLock.isHeld()) {
                    this.mWakeLock.release();
                }
            } else {
                startCountDown(r1[i]);
                if (!this.mWakeLock.isHeld()) {
                    this.mWakeLock.acquire();
                }
            }
        } else if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            stopCountDown();
        } else {
            this.mWakeLock.acquire();
            this.mDuration = 0;
            startCountDown(DURATIONS[0]);
        }
        this.mLastClickTime = SystemClock.elapsedRealtime();
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleLongClick() {
        if (this.mWakeLock.isHeld()) {
            if (this.mDuration == INFINITE_DURATION_INDEX) {
                return;
            }
        } else {
            this.mWakeLock.acquire();
        }
        this.mDuration = INFINITE_DURATION_INDEX;
        startCountDown(DURATIONS[r0]);
        refreshState();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_caffeine_label);
    }

    private void startCountDown(long j) {
        stopCountDown();
        this.mSecondsRemaining = (int) j;
        if (j == -1) {
            return;
        }
        this.mCountdownTimer = new CountDownTimer(j * 1000, 1000L) { // from class: com.android.systemui.qs.tiles.CaffeineTile.1
            @Override // android.os.CountDownTimer
            public void onTick(long j2) {
                CaffeineTile.this.mSecondsRemaining = (int) (j2 / 1000);
                CaffeineTile.this.refreshState();
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                if (CaffeineTile.this.mWakeLock.isHeld()) {
                    CaffeineTile.this.mWakeLock.release();
                }
                CaffeineTile.this.refreshState();
            }
        }.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopCountDown() {
        CountDownTimer countDownTimer = this.mCountdownTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            this.mCountdownTimer = null;
        }
    }

    private String formatValueWithRemainingTime() {
        int i = this.mSecondsRemaining;
        return i == -1 ? "∞" : String.format("%02d:%02d", Integer.valueOf((i / 60) % 60), Integer.valueOf(this.mSecondsRemaining % 60));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mWakeLock.isHeld();
        booleanState.icon = this.mIcon;
        booleanState.label = this.mContext.getString(R.string.quick_settings_caffeine_label);
        if (booleanState.value) {
            booleanState.secondaryLabel = formatValueWithRemainingTime();
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_caffeine_on);
            booleanState.state = 2;
        } else {
            booleanState.secondaryLabel = null;
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_caffeine_off);
            booleanState.state = 1;
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            ((QSTileImpl) CaffeineTile.this).mContext.registerReceiver(this, intentFilter, null, ((QSTileImpl) CaffeineTile.this).mHandler);
        }

        public void destroy() {
            ((QSTileImpl) CaffeineTile.this).mContext.unregisterReceiver(this);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                CaffeineTile.this.stopCountDown();
                if (CaffeineTile.this.mWakeLock.isHeld()) {
                    CaffeineTile.this.mWakeLock.release();
                }
                CaffeineTile.this.refreshState();
            }
        }
    }
}
