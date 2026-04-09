package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

/* loaded from: classes.dex */
public abstract class SystemSetting extends ContentObserver {
    private final Context mContext;
    private int mObservedValue;
    private final String mSettingName;
    private int mUserId;

    protected abstract void handleValueChanged(int i, boolean z);

    public SystemSetting(Context context, Handler handler, String str) {
        super(handler);
        this.mObservedValue = 0;
        this.mContext = context;
        this.mSettingName = str;
        this.mUserId = ActivityManager.getCurrentUser();
    }

    public int getValue() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), this.mSettingName, 0, this.mUserId);
    }

    public void setValue(int i) {
        Settings.System.putIntForUser(this.mContext.getContentResolver(), this.mSettingName, i, this.mUserId);
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z) {
        int value = getValue();
        handleValueChanged(value, value != this.mObservedValue);
        this.mObservedValue = value;
    }
}
