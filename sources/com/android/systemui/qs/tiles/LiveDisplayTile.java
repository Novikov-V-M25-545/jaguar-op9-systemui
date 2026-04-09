package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.hardware.display.ColorDisplayManager;
import android.os.Handler;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import lineageos.hardware.LiveDisplayManager;
import lineageos.providers.LineageSettings;

/* loaded from: classes.dex */
public class LiveDisplayTile extends QSTileImpl<QSTile.LiveDisplayState> {
    private static final Intent DISPLAY_SETTINGS = new Intent("android.settings.DISPLAY_SETTINGS");
    private String[] mAnnouncementEntries;
    private int mDayTemperature;
    private String[] mDescriptionEntries;
    private String[] mEntries;
    private final int[] mEntryIconRes;
    private boolean mListening;
    private final LiveDisplayManager mLiveDisplay;
    private final boolean mNightDisplayAvailable;
    private final LiveDisplayObserver mObserver;
    private boolean mOutdoorModeAvailable;
    private final BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;
    private String mTitle;
    private String[] mValues;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return -2147483620;
    }

    public LiveDisplayTile(QSHost qSHost) throws Resources.NotFoundException {
        super(qSHost);
        this.mDayTemperature = -1;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.LiveDisplayTile.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                LiveDisplayTile.this.updateConfig();
                LiveDisplayTile.this.refreshState();
                LiveDisplayTile.this.unregisterReceiver();
            }
        };
        this.mNightDisplayAvailable = ColorDisplayManager.isNightDisplayAvailable(this.mContext);
        TypedArray typedArrayObtainTypedArray = this.mContext.getResources().obtainTypedArray(1057161226);
        this.mEntryIconRes = new int[typedArrayObtainTypedArray.length()];
        int i = 0;
        while (true) {
            int[] iArr = this.mEntryIconRes;
            if (i >= iArr.length) {
                break;
            }
            iArr[i] = typedArrayObtainTypedArray.getResourceId(i, 0);
            i++;
        }
        typedArrayObtainTypedArray.recycle();
        updateEntries();
        this.mLiveDisplay = LiveDisplayManager.getInstance(this.mContext);
        if (!updateConfig()) {
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("lineageos.intent.action.INITIALIZE_LIVEDISPLAY"));
            this.mReceiverRegistered = true;
        }
        LiveDisplayObserver liveDisplayObserver = new LiveDisplayObserver(this.mHandler);
        this.mObserver = liveDisplayObserver;
        liveDisplayObserver.startObserving();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        super.handleDestroy();
        unregisterReceiver();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterReceiver() {
        if (this.mReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateConfig() {
        boolean z = false;
        if (this.mLiveDisplay.getConfig() == null) {
            return false;
        }
        if (this.mLiveDisplay.getConfig().hasFeature(3) && !this.mLiveDisplay.getConfig().hasFeature(14)) {
            z = true;
        }
        this.mOutdoorModeAvailable = z;
        this.mDayTemperature = this.mLiveDisplay.getDayColorTemperature();
        if (this.mNightDisplayAvailable && !this.mOutdoorModeAvailable) {
            this.mHost.removeTile(getTileSpec());
        }
        return true;
    }

    private void updateEntries() {
        Resources resources = this.mContext.getResources();
        this.mTitle = resources.getString(1057554463);
        this.mEntries = resources.getStringArray(1057161227);
        this.mDescriptionEntries = resources.getStringArray(1057161225);
        this.mAnnouncementEntries = resources.getStringArray(1057161224);
        this.mValues = resources.getStringArray(1057161229);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.LiveDisplayState newTileState() {
        return new QSTile.LiveDisplayState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mObserver.startObserving();
        } else {
            this.mObserver.endObserving();
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        changeToNextMode();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.LiveDisplayState liveDisplayState, Object obj) {
        int i;
        updateEntries();
        int currentModeIndex = obj == null ? getCurrentModeIndex() : ((Integer) obj).intValue();
        liveDisplayState.mode = currentModeIndex;
        liveDisplayState.label = this.mTitle;
        liveDisplayState.secondaryLabel = this.mEntries[currentModeIndex];
        liveDisplayState.icon = QSTileImpl.ResourceIcon.get(this.mEntryIconRes[currentModeIndex]);
        liveDisplayState.contentDescription = this.mDescriptionEntries[liveDisplayState.mode];
        if (!this.mNightDisplayAvailable || this.mOutdoorModeAvailable) {
            i = this.mLiveDisplay.getMode() != 0 ? 2 : 1;
        } else {
            i = 0;
        }
        liveDisplayState.state = i;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(1057554463);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return DISPLAY_SETTINGS;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        return this.mAnnouncementEntries[getCurrentModeIndex()];
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getCurrentModeIndex() {
        try {
            try {
                return ArrayUtils.indexOf(this.mValues, String.valueOf(this.mLiveDisplay.getMode()));
            } catch (NullPointerException unused) {
                return ArrayUtils.indexOf(this.mValues, String.valueOf(2));
            }
        } catch (Throwable unused2) {
            return ArrayUtils.indexOf(this.mValues, (Object) null);
        }
    }

    private void changeToNextMode() {
        int i;
        int iIntValue;
        if (getCurrentModeIndex() + 1 < this.mValues.length) {
            i = 0;
            while (true) {
                iIntValue = Integer.valueOf(this.mValues[i]).intValue();
                if ((this.mOutdoorModeAvailable || iIntValue != 3) && !((this.mDayTemperature == 6500 && iIntValue == 4) || (this.mNightDisplayAvailable && (iIntValue == 4 || iIntValue == 1)))) {
                    break;
                }
                i++;
                if (i >= this.mValues.length) {
                }
            }
            LineageSettings.System.putIntForUser(this.mContext.getContentResolver(), "display_temperature_mode", iIntValue, -2);
            return;
        }
        i = 0;
    }

    private class LiveDisplayObserver extends ContentObserver {
        public LiveDisplayObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            LiveDisplayTile liveDisplayTile = LiveDisplayTile.this;
            liveDisplayTile.mDayTemperature = liveDisplayTile.mLiveDisplay.getDayColorTemperature();
            LiveDisplayTile liveDisplayTile2 = LiveDisplayTile.this;
            liveDisplayTile2.refreshState(Integer.valueOf(liveDisplayTile2.getCurrentModeIndex()));
        }

        public void startObserving() {
            ((QSTileImpl) LiveDisplayTile.this).mContext.getContentResolver().registerContentObserver(LineageSettings.System.getUriFor("display_temperature_mode"), false, this, -1);
            ((QSTileImpl) LiveDisplayTile.this).mContext.getContentResolver().registerContentObserver(LineageSettings.System.getUriFor("display_temperature_day"), false, this, -1);
        }

        public void endObserving() {
            ((QSTileImpl) LiveDisplayTile.this).mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}
