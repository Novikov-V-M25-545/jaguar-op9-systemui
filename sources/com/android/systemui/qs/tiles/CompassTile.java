package com.android.systemui.qs.tiles;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageView;
import com.android.internal.util.crdroid.Utils;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

/* loaded from: classes.dex */
public class CompassTile extends QSTileImpl<QSTile.BooleanState> implements SensorEventListener {
    private float[] mAcceleration;
    private Sensor mAccelerationSensor;
    private boolean mActive;
    private float[] mGeomagnetic;
    private Sensor mGeomagneticFieldSensor;
    private ImageView mImage;
    private boolean mListeningSensors;
    private SensorManager mSensorManager;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 4000;
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public CompassTile(QSHost qSHost) {
        super(qSHost);
        this.mActive = false;
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSensorManager = sensorManager;
        this.mAccelerationSensor = sensorManager.getDefaultSensor(1);
        this.mGeomagneticFieldSensor = this.mSensorManager.getDefaultSensor(2);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleDestroy() {
        super.handleDestroy();
        setListeningSensors(false);
        this.mSensorManager = null;
        this.mImage = null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public QSIconView createTileView(Context context) {
        QSIconView qSIconViewCreateTileView = super.createTileView(context);
        this.mImage = (ImageView) qSIconViewCreateTileView.findViewById(R.id.icon);
        return qSIconViewCreateTileView;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected void handleClick() {
        this.mActive = !this.mActive;
        refreshState();
        setListeningSensors(this.mActive);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleLongClick() {
        handleClick();
    }

    private void setListeningSensors(boolean z) {
        if (z == this.mListeningSensors) {
            return;
        }
        this.mListeningSensors = z;
        if (z) {
            this.mSensorManager.registerListener(this, this.mAccelerationSensor, 1);
            this.mSensorManager.registerListener(this, this.mGeomagneticFieldSensor, 1);
        } else {
            this.mSensorManager.unregisterListener(this);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(com.android.systemui.R.string.quick_settings_compass_label);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        Float fValueOf = Float.valueOf(obj == null ? 0.0f : ((Float) obj).floatValue());
        booleanState.value = this.mActive;
        booleanState.icon = QSTileImpl.ResourceIcon.get(com.android.systemui.R.drawable.ic_qs_compass);
        if (booleanState.value) {
            booleanState.state = 2;
            if (obj != null) {
                booleanState.label = formatValueWithCardinalDirection(fValueOf.floatValue());
                float fFloatValue = (360.0f - fValueOf.floatValue()) - this.mImage.getRotation();
                if (fFloatValue > 180.0f) {
                    fFloatValue -= 360.0f;
                }
                ImageView imageView = this.mImage;
                imageView.setRotation(imageView.getRotation() + (fFloatValue / 2.0f));
            } else {
                booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_compass_init);
                this.mImage.setRotation(0.0f);
            }
            booleanState.contentDescription = this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_compass_on);
            return;
        }
        booleanState.label = this.mContext.getString(com.android.systemui.R.string.quick_settings_compass_label);
        booleanState.contentDescription = this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_compass_off);
        booleanState.state = 1;
        ImageView imageView2 = this.mImage;
        if (imageView2 != null) {
            imageView2.setRotation(0.0f);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    protected String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_compass_on);
        }
        return this.mContext.getString(com.android.systemui.R.string.accessibility_quick_settings_compass_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return Utils.deviceHasCompass(this.mContext);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            return;
        }
        setListeningSensors(false);
        this.mActive = false;
    }

    private String formatValueWithCardinalDirection(float f) throws Resources.NotFoundException {
        return this.mContext.getString(com.android.systemui.R.string.quick_settings_compass_value, Float.valueOf(f), this.mContext.getResources().getStringArray(com.android.systemui.R.array.cardinal_directions)[((int) (Math.floor(((f - 22.5d) % 360.0d) / 45.0d) + 1.0d)) % 8]);
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] fArr;
        float[] fArr2;
        float[] fArr3;
        if (sensorEvent.sensor.getType() == 1) {
            if (this.mAcceleration == null) {
                this.mAcceleration = (float[]) sensorEvent.values.clone();
            }
            fArr = this.mAcceleration;
        } else {
            if (this.mGeomagnetic == null) {
                this.mGeomagnetic = (float[]) sensorEvent.values.clone();
            }
            fArr = this.mGeomagnetic;
        }
        for (int i = 0; i < 3; i++) {
            fArr[i] = (fArr[i] * 0.97f) + (sensorEvent.values[i] * 0.029999971f);
        }
        if (!this.mActive || !this.mListeningSensors || (fArr2 = this.mAcceleration) == null || (fArr3 = this.mGeomagnetic) == null) {
            return;
        }
        float[] fArr4 = new float[9];
        if (SensorManager.getRotationMatrix(fArr4, new float[9], fArr2, fArr3)) {
            SensorManager.getOrientation(fArr4, new float[3]);
            refreshState(Float.valueOf((Float.valueOf((float) Math.toDegrees(r7[0])).floatValue() + 360.0f) % 360.0f));
        }
    }
}
