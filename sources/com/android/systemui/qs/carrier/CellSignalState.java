package com.android.systemui.qs.carrier;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CellSignalState.kt */
/* loaded from: classes.dex */
public final class CellSignalState {

    @Nullable
    public final String contentDescription;
    public final int mobileSignalIconId;
    public final boolean roaming;

    @Nullable
    public final String typeContentDescription;
    public final boolean visible;

    public CellSignalState() {
        this(false, 0, null, null, false, 31, null);
    }

    public static /* synthetic */ CellSignalState copy$default(CellSignalState cellSignalState, boolean z, int i, String str, String str2, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z = cellSignalState.visible;
        }
        if ((i2 & 2) != 0) {
            i = cellSignalState.mobileSignalIconId;
        }
        int i3 = i;
        if ((i2 & 4) != 0) {
            str = cellSignalState.contentDescription;
        }
        String str3 = str;
        if ((i2 & 8) != 0) {
            str2 = cellSignalState.typeContentDescription;
        }
        String str4 = str2;
        if ((i2 & 16) != 0) {
            z2 = cellSignalState.roaming;
        }
        return cellSignalState.copy(z, i3, str3, str4, z2);
    }

    @NotNull
    public final CellSignalState copy(boolean z, int i, @Nullable String str, @Nullable String str2, boolean z2) {
        return new CellSignalState(z, i, str, str2, z2);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CellSignalState)) {
            return false;
        }
        CellSignalState cellSignalState = (CellSignalState) obj;
        return this.visible == cellSignalState.visible && this.mobileSignalIconId == cellSignalState.mobileSignalIconId && Intrinsics.areEqual(this.contentDescription, cellSignalState.contentDescription) && Intrinsics.areEqual(this.typeContentDescription, cellSignalState.typeContentDescription) && this.roaming == cellSignalState.roaming;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [int] */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v11 */
    public int hashCode() {
        boolean z = this.visible;
        ?? r0 = z;
        if (z) {
            r0 = 1;
        }
        int iHashCode = ((r0 * 31) + Integer.hashCode(this.mobileSignalIconId)) * 31;
        String str = this.contentDescription;
        int iHashCode2 = (iHashCode + (str != null ? str.hashCode() : 0)) * 31;
        String str2 = this.typeContentDescription;
        int iHashCode3 = (iHashCode2 + (str2 != null ? str2.hashCode() : 0)) * 31;
        boolean z2 = this.roaming;
        return iHashCode3 + (z2 ? 1 : z2 ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "CellSignalState(visible=" + this.visible + ", mobileSignalIconId=" + this.mobileSignalIconId + ", contentDescription=" + this.contentDescription + ", typeContentDescription=" + this.typeContentDescription + ", roaming=" + this.roaming + ")";
    }

    public CellSignalState(boolean z, int i, @Nullable String str, @Nullable String str2, boolean z2) {
        this.visible = z;
        this.mobileSignalIconId = i;
        this.contentDescription = str;
        this.typeContentDescription = str2;
        this.roaming = z2;
    }

    public /* synthetic */ CellSignalState(boolean z, int i, String str, String str2, boolean z2, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this((i2 & 1) != 0 ? false : z, (i2 & 2) != 0 ? 0 : i, (i2 & 4) != 0 ? null : str, (i2 & 8) != 0 ? null : str2, (i2 & 16) != 0 ? false : z2);
    }

    @NotNull
    public final CellSignalState changeVisibility(boolean z) {
        return this.visible == z ? this : copy$default(this, z, 0, null, null, false, 30, null);
    }
}
