package com.android.systemui.util.sensors;

import android.content.Context;
import android.hardware.HardwareBuffer;
import android.hardware.Sensor;
import android.hardware.SensorAdditionalInfo;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.MemoryFile;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.shared.plugins.PluginManager;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class AsyncSensorManager extends SensorManager implements PluginListener<SensorManagerPlugin> {
    private final Handler mHandler;
    private final SensorManager mInner;
    private final List<SensorManagerPlugin> mPlugins;
    private final List<Sensor> mSensorCache;

    public AsyncSensorManager(SensorManager sensorManager, PluginManager pluginManager) {
        this(sensorManager, pluginManager, null);
    }

    @VisibleForTesting
    public AsyncSensorManager(SensorManager sensorManager, PluginManager pluginManager, Handler handler) {
        this.mInner = sensorManager;
        if (handler == null) {
            HandlerThread handlerThread = new HandlerThread("async_sensor");
            handlerThread.start();
            this.mHandler = new Handler(handlerThread.getLooper());
        } else {
            this.mHandler = handler;
        }
        this.mSensorCache = sensorManager.getSensorList(-1);
        this.mPlugins = new ArrayList();
        if (pluginManager != null) {
            pluginManager.addPluginListener((PluginListener) this, SensorManagerPlugin.class, true);
        }
    }

    protected List<Sensor> getFullSensorList() {
        return this.mSensorCache;
    }

    protected List<Sensor> getFullDynamicSensorList() {
        return this.mInner.getSensorList(-1);
    }

    protected boolean registerListenerImpl(final SensorEventListener sensorEventListener, final Sensor sensor, final int i, final Handler handler, final int i2, int i3) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$registerListenerImpl$0(sensorEventListener, sensor, i, i2, handler);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$registerListenerImpl$0(SensorEventListener sensorEventListener, Sensor sensor, int i, int i2, Handler handler) {
        if (this.mInner.registerListener(sensorEventListener, sensor, i, i2, handler)) {
            return;
        }
        Log.e("AsyncSensorManager", "Registering " + sensorEventListener + " for " + sensor + " failed.");
    }

    protected boolean flushImpl(SensorEventListener sensorEventListener) {
        return this.mInner.flush(sensorEventListener);
    }

    protected SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile, HardwareBuffer hardwareBuffer) {
        throw new UnsupportedOperationException("not implemented");
    }

    protected void destroyDirectChannelImpl(SensorDirectChannel sensorDirectChannel) {
        throw new UnsupportedOperationException("not implemented");
    }

    protected int configureDirectChannelImpl(SensorDirectChannel sensorDirectChannel, Sensor sensor, int i) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$registerDynamicSensorCallbackImpl$1(SensorManager.DynamicSensorCallback dynamicSensorCallback, Handler handler) {
        this.mInner.registerDynamicSensorCallback(dynamicSensorCallback, handler);
    }

    protected void registerDynamicSensorCallbackImpl(final SensorManager.DynamicSensorCallback dynamicSensorCallback, final Handler handler) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$registerDynamicSensorCallbackImpl$1(dynamicSensorCallback, handler);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$unregisterDynamicSensorCallbackImpl$2(SensorManager.DynamicSensorCallback dynamicSensorCallback) {
        this.mInner.unregisterDynamicSensorCallback(dynamicSensorCallback);
    }

    protected void unregisterDynamicSensorCallbackImpl(final SensorManager.DynamicSensorCallback dynamicSensorCallback) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$unregisterDynamicSensorCallbackImpl$2(dynamicSensorCallback);
            }
        });
    }

    protected boolean requestTriggerSensorImpl(final TriggerEventListener triggerEventListener, final Sensor sensor) {
        if (triggerEventListener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        }
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$requestTriggerSensorImpl$3(triggerEventListener, sensor);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$requestTriggerSensorImpl$3(TriggerEventListener triggerEventListener, Sensor sensor) {
        if (this.mInner.requestTriggerSensor(triggerEventListener, sensor)) {
            return;
        }
        Log.e("AsyncSensorManager", "Requesting " + triggerEventListener + " for " + sensor + " failed.");
    }

    protected boolean cancelTriggerSensorImpl(final TriggerEventListener triggerEventListener, final Sensor sensor, boolean z) {
        Preconditions.checkArgument(z);
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$cancelTriggerSensorImpl$4(triggerEventListener, sensor);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$cancelTriggerSensorImpl$4(TriggerEventListener triggerEventListener, Sensor sensor) {
        if (this.mInner.cancelTriggerSensor(triggerEventListener, sensor)) {
            return;
        }
        Log.e("AsyncSensorManager", "Canceling " + triggerEventListener + " for " + sensor + " failed.");
    }

    public boolean registerPluginListener(final SensorManagerPlugin.Sensor sensor, final SensorManagerPlugin.SensorEventListener sensorEventListener) {
        if (this.mPlugins.isEmpty()) {
            Log.w("AsyncSensorManager", "No plugins registered");
            return false;
        }
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$registerPluginListener$5(sensor, sensorEventListener);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$registerPluginListener$5(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        for (int i = 0; i < this.mPlugins.size(); i++) {
            this.mPlugins.get(i).registerListener(sensor, sensorEventListener);
        }
    }

    public void unregisterPluginListener(final SensorManagerPlugin.Sensor sensor, final SensorManagerPlugin.SensorEventListener sensorEventListener) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$unregisterPluginListener$6(sensor, sensorEventListener);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$unregisterPluginListener$6(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        for (int i = 0; i < this.mPlugins.size(); i++) {
            this.mPlugins.get(i).unregisterListener(sensor, sensorEventListener);
        }
    }

    protected boolean initDataInjectionImpl(boolean z) {
        throw new UnsupportedOperationException("not implemented");
    }

    protected boolean injectSensorDataImpl(Sensor sensor, float[] fArr, int i, long j) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setOperationParameterImpl$7(SensorAdditionalInfo sensorAdditionalInfo) {
        this.mInner.setOperationParameter(sensorAdditionalInfo);
    }

    protected boolean setOperationParameterImpl(final SensorAdditionalInfo sensorAdditionalInfo) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setOperationParameterImpl$7(sensorAdditionalInfo);
            }
        });
        return true;
    }

    protected void unregisterListenerImpl(final SensorEventListener sensorEventListener, final Sensor sensor) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.util.sensors.AsyncSensorManager$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$unregisterListenerImpl$8(sensor, sensorEventListener);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$unregisterListenerImpl$8(Sensor sensor, SensorEventListener sensorEventListener) {
        if (sensor == null) {
            this.mInner.unregisterListener(sensorEventListener);
        } else {
            this.mInner.unregisterListener(sensorEventListener, sensor);
        }
    }

    @Override // com.android.systemui.plugins.PluginListener
    public void onPluginConnected(SensorManagerPlugin sensorManagerPlugin, Context context) {
        this.mPlugins.add(sensorManagerPlugin);
    }

    @Override // com.android.systemui.plugins.PluginListener
    public void onPluginDisconnected(SensorManagerPlugin sensorManagerPlugin) {
        this.mPlugins.remove(sensorManagerPlugin);
    }
}
