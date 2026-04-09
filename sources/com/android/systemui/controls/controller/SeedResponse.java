package com.android.systemui.controls.controller;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsController.kt */
/* loaded from: classes.dex */
public final class SeedResponse {
    private final boolean accepted;

    @NotNull
    private final String packageName;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SeedResponse)) {
            return false;
        }
        SeedResponse seedResponse = (SeedResponse) obj;
        return Intrinsics.areEqual(this.packageName, seedResponse.packageName) && this.accepted == seedResponse.accepted;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public int hashCode() {
        String str = this.packageName;
        int iHashCode = (str != null ? str.hashCode() : 0) * 31;
        boolean z = this.accepted;
        int i = z;
        if (z != 0) {
            i = 1;
        }
        return iHashCode + i;
    }

    @NotNull
    public String toString() {
        return "SeedResponse(packageName=" + this.packageName + ", accepted=" + this.accepted + ")";
    }

    public SeedResponse(@NotNull String packageName, boolean z) {
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        this.packageName = packageName;
        this.accepted = z;
    }

    public final boolean getAccepted() {
        return this.accepted;
    }

    @NotNull
    public final String getPackageName() {
        return this.packageName;
    }
}
