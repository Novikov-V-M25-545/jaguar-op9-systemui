package com.android.systemui.statusbar.policy;

/* loaded from: classes.dex */
public interface SensorPrivacyController extends CallbackController<OnSensorPrivacyChangedListener> {

    public interface OnSensorPrivacyChangedListener {
        void onSensorPrivacyChanged(boolean z);
    }

    boolean isSensorPrivacyEnabled();
}
