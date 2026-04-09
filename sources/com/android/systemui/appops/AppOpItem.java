package com.android.systemui.appops;

/* loaded from: classes.dex */
public class AppOpItem {
    private int mCode;
    private String mPackageName;
    private boolean mSilenced;
    private String mState;
    private long mTimeStarted;
    private int mUid;

    public AppOpItem(int i, int i2, String str, long j) {
        this.mCode = i;
        this.mUid = i2;
        this.mPackageName = str;
        this.mTimeStarted = j;
        this.mState = "AppOpItem(Op code=" + i + ", UID=" + i2 + ", Package name=" + str + ", Paused=";
    }

    public int getCode() {
        return this.mCode;
    }

    public int getUid() {
        return this.mUid;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setSilenced(boolean z) {
        this.mSilenced = z;
    }

    public boolean isSilenced() {
        return this.mSilenced;
    }

    public String toString() {
        return this.mState + this.mSilenced + ")";
    }
}
