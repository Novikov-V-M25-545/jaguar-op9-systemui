package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.hardware.SensorPrivacyManager;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class SensorPrivacyTile extends QSTileImpl<QSTile.BooleanState> implements SensorPrivacyManager.OnSensorPrivacyChangedListener {
    private final ActivityStarter mActivityStarter;
    private final QSTile.Icon mIcon;
    private final SensorPrivacyManager mSensorPrivacyManager;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 1598;
    }

    public SensorPrivacyTile(QSHost qSHost, SensorPrivacyManager sensorPrivacyManager, ActivityStarter activityStarter) {
        super(qSHost);
        this.mIcon = QSTileImpl.ResourceIcon.get(R.drawable.ic_signal_sensors);
        this.mSensorPrivacyManager = sensorPrivacyManager;
        this.mActivityStarter = activityStarter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        final boolean z = ((QSTile.BooleanState) this.mState).value;
        if (z) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.SensorPrivacyTile$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$handleClick$0(z);
                }
            });
        } else {
            MetricsLogger.action(this.mContext, getMetricsCategory(), !z);
            setEnabled(!z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleClick$0(boolean z) {
        MetricsLogger.action(this.mContext, getMetricsCategory(), !z);
        setEnabled(!z);
    }

    private void setEnabled(boolean z) {
        this.mSensorPrivacyManager.setSensorPrivacy(z);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.sensor_privacy_mode);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean zBooleanValue = obj instanceof Boolean ? ((Boolean) obj).booleanValue() : this.mSensorPrivacyManager.isSensorPrivacyEnabled();
        booleanState.value = zBooleanValue;
        String string = this.mContext.getString(R.string.sensor_privacy_mode);
        booleanState.label = string;
        booleanState.icon = this.mIcon;
        booleanState.state = zBooleanValue ? 2 : 1;
        booleanState.contentDescription = string;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_sensor_privacy_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_sensor_privacy_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleSetListening(boolean z) {
        if (z) {
            this.mSensorPrivacyManager.addSensorPrivacyListener(this);
        } else {
            this.mSensorPrivacyManager.removeSensorPrivacyListener(this);
        }
    }

    public void onSensorPrivacyChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }
}
