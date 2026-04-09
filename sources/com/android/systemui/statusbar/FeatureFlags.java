package com.android.systemui.statusbar;

import android.provider.DeviceConfig;
import android.util.ArrayMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class FeatureFlags {
    private final Map<String, Boolean> mCachedDeviceConfigFlags = new ArrayMap();

    public FeatureFlags(Executor executor) {
        DeviceConfig.addOnPropertiesChangedListener("systemui", executor, new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.statusbar.FeatureFlags$$ExternalSyntheticLambda0
            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                this.f$0.onPropertiesChanged(properties);
            }
        });
    }

    public boolean isNewNotifPipelineEnabled() {
        return getDeviceConfigFlag("notification.newpipeline.enabled", true);
    }

    public boolean isNewNotifPipelineRenderingEnabled() {
        return isNewNotifPipelineEnabled() && getDeviceConfigFlag("notification.newpipeline.rendering", false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPropertiesChanged(DeviceConfig.Properties properties) {
        synchronized (this.mCachedDeviceConfigFlags) {
            Iterator it = properties.getKeyset().iterator();
            while (it.hasNext()) {
                this.mCachedDeviceConfigFlags.remove((String) it.next());
            }
        }
    }

    private boolean getDeviceConfigFlag(String str, boolean z) {
        boolean zBooleanValue;
        synchronized (this.mCachedDeviceConfigFlags) {
            Boolean boolValueOf = this.mCachedDeviceConfigFlags.get(str);
            if (boolValueOf == null) {
                boolValueOf = Boolean.valueOf(DeviceConfig.getBoolean("systemui", str, z));
                this.mCachedDeviceConfigFlags.put(str, boolValueOf);
            }
            zBooleanValue = boolValueOf.booleanValue();
        }
        return zBooleanValue;
    }
}
