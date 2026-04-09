package com.android.systemui.appops;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PermissionFlagsCache.kt */
/* loaded from: classes.dex */
final class PermissionFlagKey {

    @NotNull
    private final String packageName;

    @NotNull
    private final String permission;
    private final int uid;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PermissionFlagKey)) {
            return false;
        }
        PermissionFlagKey permissionFlagKey = (PermissionFlagKey) obj;
        return Intrinsics.areEqual(this.permission, permissionFlagKey.permission) && Intrinsics.areEqual(this.packageName, permissionFlagKey.packageName) && this.uid == permissionFlagKey.uid;
    }

    public int hashCode() {
        String str = this.permission;
        int iHashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.packageName;
        return ((iHashCode + (str2 != null ? str2.hashCode() : 0)) * 31) + Integer.hashCode(this.uid);
    }

    @NotNull
    public String toString() {
        return "PermissionFlagKey(permission=" + this.permission + ", packageName=" + this.packageName + ", uid=" + this.uid + ")";
    }

    public PermissionFlagKey(@NotNull String permission, @NotNull String packageName, int i) {
        Intrinsics.checkParameterIsNotNull(permission, "permission");
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        this.permission = permission;
        this.packageName = packageName;
        this.uid = i;
    }

    @NotNull
    public final String getPermission() {
        return this.permission;
    }

    @NotNull
    public final String getPackageName() {
        return this.packageName;
    }

    public final int getUid() {
        return this.uid;
    }
}
