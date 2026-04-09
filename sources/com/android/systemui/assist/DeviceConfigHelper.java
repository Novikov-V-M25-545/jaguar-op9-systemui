package com.android.systemui.assist;

import android.provider.DeviceConfig;
import com.android.systemui.DejankUtils;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class DeviceConfigHelper {
    public long getLong(final String str, final long j) {
        return ((Long) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.assist.DeviceConfigHelper$$ExternalSyntheticLambda1
            @Override // java.util.function.Supplier
            public final Object get() {
                return DeviceConfigHelper.lambda$getLong$0(str, j);
            }
        })).longValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Long lambda$getLong$0(String str, long j) {
        return Long.valueOf(DeviceConfig.getLong("systemui", str, j));
    }

    public int getInt(final String str, final int i) {
        return ((Integer) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.assist.DeviceConfigHelper$$ExternalSyntheticLambda0
            @Override // java.util.function.Supplier
            public final Object get() {
                return DeviceConfigHelper.lambda$getInt$1(str, i);
            }
        })).intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Integer lambda$getInt$1(String str, int i) {
        return Integer.valueOf(DeviceConfig.getInt("systemui", str, i));
    }

    public String getString(final String str, final String str2) {
        return (String) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.assist.DeviceConfigHelper$$ExternalSyntheticLambda2
            @Override // java.util.function.Supplier
            public final Object get() {
                return DeviceConfigHelper.lambda$getString$2(str, str2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ String lambda$getString$2(String str, String str2) {
        return DeviceConfig.getString("systemui", str, str2);
    }

    public boolean getBoolean(final String str, final boolean z) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.assist.DeviceConfigHelper$$ExternalSyntheticLambda3
            @Override // java.util.function.Supplier
            public final Object get() {
                return DeviceConfigHelper.lambda$getBoolean$3(str, z);
            }
        })).booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Boolean lambda$getBoolean$3(String str, boolean z) {
        return Boolean.valueOf(DeviceConfig.getBoolean("systemui", str, z));
    }

    public void addOnPropertiesChangedListener(Executor executor, DeviceConfig.OnPropertiesChangedListener onPropertiesChangedListener) {
        DeviceConfig.addOnPropertiesChangedListener("systemui", executor, onPropertiesChangedListener);
    }
}
