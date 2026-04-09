package com.android.systemui.controls;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.service.controls.Control;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlStatus.kt */
/* loaded from: classes.dex */
public final class ControlStatus implements ControlInterface {

    @NotNull
    private final ComponentName component;

    @NotNull
    private final Control control;
    private boolean favorite;
    private final boolean removed;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ControlStatus)) {
            return false;
        }
        ControlStatus controlStatus = (ControlStatus) obj;
        return Intrinsics.areEqual(this.control, controlStatus.control) && Intrinsics.areEqual(getComponent(), controlStatus.getComponent()) && getFavorite() == controlStatus.getFavorite() && getRemoved() == controlStatus.getRemoved();
    }

    public int hashCode() {
        Control control = this.control;
        int iHashCode = (control != null ? control.hashCode() : 0) * 31;
        ComponentName component = getComponent();
        int iHashCode2 = (iHashCode + (component != null ? component.hashCode() : 0)) * 31;
        boolean favorite = getFavorite();
        int i = favorite;
        if (favorite) {
            i = 1;
        }
        int i2 = (iHashCode2 + i) * 31;
        boolean removed = getRemoved();
        return i2 + (removed ? 1 : removed);
    }

    @NotNull
    public String toString() {
        return "ControlStatus(control=" + this.control + ", component=" + getComponent() + ", favorite=" + getFavorite() + ", removed=" + getRemoved() + ")";
    }

    public ControlStatus(@NotNull Control control, @NotNull ComponentName component, boolean z, boolean z2) {
        Intrinsics.checkParameterIsNotNull(control, "control");
        Intrinsics.checkParameterIsNotNull(component, "component");
        this.control = control;
        this.component = component;
        this.favorite = z;
        this.removed = z2;
    }

    @NotNull
    public final Control getControl() {
        return this.control;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public ComponentName getComponent() {
        return this.component;
    }

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getFavorite() {
        return this.favorite;
    }

    public void setFavorite(boolean z) {
        this.favorite = z;
    }

    public /* synthetic */ ControlStatus(Control control, ComponentName componentName, boolean z, boolean z2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(control, componentName, z, (i & 8) != 0 ? false : z2);
    }

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getRemoved() {
        return this.removed;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public String getControlId() {
        String controlId = this.control.getControlId();
        Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
        return controlId;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getTitle() {
        CharSequence title = this.control.getTitle();
        Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
        return title;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getSubtitle() {
        CharSequence subtitle = this.control.getSubtitle();
        Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
        return subtitle;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @Nullable
    public Icon getCustomIcon() {
        return this.control.getCustomIcon();
    }

    @Override // com.android.systemui.controls.ControlInterface
    public int getDeviceType() {
        return this.control.getDeviceType();
    }
}
