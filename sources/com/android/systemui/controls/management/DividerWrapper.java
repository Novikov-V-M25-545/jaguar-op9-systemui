package com.android.systemui.controls.management;

import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsModel.kt */
/* loaded from: classes.dex */
public final class DividerWrapper extends ElementWrapper {
    private boolean showDivider;
    private boolean showNone;

    /* JADX WARN: Illegal instructions before constructor call */
    public DividerWrapper() {
        boolean z = false;
        this(z, z, 3, null);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DividerWrapper)) {
            return false;
        }
        DividerWrapper dividerWrapper = (DividerWrapper) obj;
        return this.showNone == dividerWrapper.showNone && this.showDivider == dividerWrapper.showDivider;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [int] */
    /* JADX WARN: Type inference failed for: r0v4 */
    /* JADX WARN: Type inference failed for: r0v5 */
    public int hashCode() {
        boolean z = this.showNone;
        ?? r0 = z;
        if (z) {
            r0 = 1;
        }
        int i = r0 * 31;
        boolean z2 = this.showDivider;
        return i + (z2 ? 1 : z2 ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "DividerWrapper(showNone=" + this.showNone + ", showDivider=" + this.showDivider + ")";
    }

    public final boolean getShowNone() {
        return this.showNone;
    }

    public final void setShowNone(boolean z) {
        this.showNone = z;
    }

    public /* synthetic */ DividerWrapper(boolean z, boolean z2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? false : z, (i & 2) != 0 ? false : z2);
    }

    public final boolean getShowDivider() {
        return this.showDivider;
    }

    public final void setShowDivider(boolean z) {
        this.showDivider = z;
    }

    public DividerWrapper(boolean z, boolean z2) {
        super(null);
        this.showNone = z;
        this.showDivider = z2;
    }
}
