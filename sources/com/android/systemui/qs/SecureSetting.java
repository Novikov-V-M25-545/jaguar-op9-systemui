package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

/* loaded from: classes.dex */
public abstract class SecureSetting extends ContentObserver {
    private final Context mContext;
    private final int mDefaultValue;
    private boolean mListening;
    private int mObservedValue;
    private final String mSettingName;
    private int mUserId;

    protected abstract void handleValueChanged(int i, boolean z);

    protected SecureSetting(Context context, Handler handler, String str) {
        this(context, handler, str, ActivityManager.getCurrentUser());
    }

    protected SecureSetting(Context context, Handler handler, String str, int i) {
        this(context, handler, str, i, 0);
    }

    public SecureSetting(Context context, Handler handler, String str, int i, int i2) {
        super(handler);
        this.mContext = context;
        this.mSettingName = str;
        this.mDefaultValue = i2;
        this.mObservedValue = i2;
        this.mUserId = i;
    }

    public int getValue() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), this.mSettingName, this.mDefaultValue, this.mUserId);
    }

    public void setValue(int i) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), this.mSettingName, i, this.mUserId);
    }

    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        if (z) {
            this.mObservedValue = getValue();
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(this.mSettingName), false, this, this.mUserId);
        } else {
            this.mContext.getContentResolver().unregisterContentObserver(this);
            this.mObservedValue = this.mDefaultValue;
        }
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z) {
        int value = getValue();
        handleValueChanged(value, value != this.mObservedValue);
        this.mObservedValue = value;
    }

    public void setUserId(int i) {
        this.mUserId = i;
        if (this.mListening) {
            setListening(false);
            setListening(true);
        }
    }

    public String getKey() {
        return this.mSettingName;
    }
}
