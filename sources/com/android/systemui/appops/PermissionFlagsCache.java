package com.android.systemui.appops;

import android.content.pm.PackageManager;
import android.os.UserHandle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PermissionFlagsCache.kt */
/* loaded from: classes.dex */
public final class PermissionFlagsCache implements PackageManager.OnPermissionsChangedListener {
    private final Executor executor;
    private boolean listening;
    private final PackageManager packageManager;
    private final Map<Integer, Map<PermissionFlagKey, Integer>> permissionFlagsCache;

    public PermissionFlagsCache(@NotNull PackageManager packageManager, @NotNull Executor executor) {
        Intrinsics.checkParameterIsNotNull(packageManager, "packageManager");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.packageManager = packageManager;
        this.executor = executor;
        this.permissionFlagsCache = new LinkedHashMap();
    }

    public void onPermissionsChanged(final int i) {
        this.executor.execute(new Runnable() { // from class: com.android.systemui.appops.PermissionFlagsCache.onPermissionsChanged.1
            @Override // java.lang.Runnable
            public final void run() {
                Map map = (Map) PermissionFlagsCache.this.permissionFlagsCache.get(Integer.valueOf(i));
                if (map != null) {
                    for (Map.Entry entry : map.entrySet()) {
                        map.put(entry.getKey(), Integer.valueOf(PermissionFlagsCache.this.getFlags((PermissionFlagKey) entry.getKey())));
                    }
                }
            }
        });
    }

    public final int getPermissionFlags(@NotNull String permission, @NotNull String packageName, int i) {
        Intrinsics.checkParameterIsNotNull(permission, "permission");
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        if (!this.listening) {
            this.listening = true;
            this.packageManager.addOnPermissionsChangeListener(this);
        }
        PermissionFlagKey permissionFlagKey = new PermissionFlagKey(permission, packageName, i);
        Map<Integer, Map<PermissionFlagKey, Integer>> map = this.permissionFlagsCache;
        Integer numValueOf = Integer.valueOf(i);
        Map<PermissionFlagKey, Integer> linkedHashMap = map.get(numValueOf);
        if (linkedHashMap == null) {
            linkedHashMap = new LinkedHashMap<>();
            map.put(numValueOf, linkedHashMap);
        }
        Integer num = linkedHashMap.get(permissionFlagKey);
        if (num != null) {
            return num.intValue();
        }
        int flags = getFlags(permissionFlagKey);
        Map<PermissionFlagKey, Integer> map2 = this.permissionFlagsCache.get(Integer.valueOf(i));
        if (map2 != null) {
            map2.put(permissionFlagKey, Integer.valueOf(flags));
        }
        return flags;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int getFlags(PermissionFlagKey permissionFlagKey) {
        return this.packageManager.getPermissionFlags(permissionFlagKey.getPermission(), permissionFlagKey.getPackageName(), UserHandle.getUserHandleForUid(permissionFlagKey.getUid()));
    }
}
