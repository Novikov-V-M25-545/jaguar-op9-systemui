package com.android.systemui.util.sensors;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.android.systemui.R;
import com.android.systemui.util.sensors.ThresholdSensorImpl;

/* loaded from: classes.dex */
public class SensorModule {
    static ThresholdSensor providePrimaryProxSensor(SensorManager sensorManager, ThresholdSensorImpl.Builder builder) {
        try {
            return builder.setSensorDelay(3).setSensorResourceId(R.string.proximity_sensor_type).setThresholdResourceId(R.dimen.proximity_sensor_threshold).setThresholdLatchResourceId(R.dimen.proximity_sensor_threshold_latch).build();
        } catch (IllegalStateException unused) {
            Sensor defaultSensor = sensorManager.getDefaultSensor(8);
            return builder.setSensor(defaultSensor).setThresholdValue(defaultSensor != null ? defaultSensor.getMaximumRange() : 0.0f).build();
        }
    }

    static ThresholdSensor provideSecondaryProxSensor(ThresholdSensorImpl.Builder builder) {
        try {
            return builder.setSensorResourceId(R.string.proximity_sensor_secondary_type).setThresholdResourceId(R.dimen.proximity_sensor_secondary_threshold).setThresholdLatchResourceId(R.dimen.proximity_sensor_secondary_threshold_latch).build();
        } catch (IllegalStateException unused) {
            return builder.setSensor(null).setThresholdValue(0.0f).build();
        }
    }
}
