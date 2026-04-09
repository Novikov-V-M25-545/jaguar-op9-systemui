package com.android.systemui.controls.controller;

import android.content.ComponentName;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: StructureInfo.kt */
/* loaded from: classes.dex */
public final class StructureInfo {

    @NotNull
    private final ComponentName componentName;

    @NotNull
    private final List<ControlInfo> controls;

    @NotNull
    private final CharSequence structure;

    /* JADX WARN: Multi-variable type inference failed */
    public static /* synthetic */ StructureInfo copy$default(StructureInfo structureInfo, ComponentName componentName, CharSequence charSequence, List list, int i, Object obj) {
        if ((i & 1) != 0) {
            componentName = structureInfo.componentName;
        }
        if ((i & 2) != 0) {
            charSequence = structureInfo.structure;
        }
        if ((i & 4) != 0) {
            list = structureInfo.controls;
        }
        return structureInfo.copy(componentName, charSequence, list);
    }

    @NotNull
    public final StructureInfo copy(@NotNull ComponentName componentName, @NotNull CharSequence structure, @NotNull List<ControlInfo> controls) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(structure, "structure");
        Intrinsics.checkParameterIsNotNull(controls, "controls");
        return new StructureInfo(componentName, structure, controls);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StructureInfo)) {
            return false;
        }
        StructureInfo structureInfo = (StructureInfo) obj;
        return Intrinsics.areEqual(this.componentName, structureInfo.componentName) && Intrinsics.areEqual(this.structure, structureInfo.structure) && Intrinsics.areEqual(this.controls, structureInfo.controls);
    }

    public int hashCode() {
        ComponentName componentName = this.componentName;
        int iHashCode = (componentName != null ? componentName.hashCode() : 0) * 31;
        CharSequence charSequence = this.structure;
        int iHashCode2 = (iHashCode + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        List<ControlInfo> list = this.controls;
        return iHashCode2 + (list != null ? list.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "StructureInfo(componentName=" + this.componentName + ", structure=" + this.structure + ", controls=" + this.controls + ")";
    }

    public StructureInfo(@NotNull ComponentName componentName, @NotNull CharSequence structure, @NotNull List<ControlInfo> controls) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(structure, "structure");
        Intrinsics.checkParameterIsNotNull(controls, "controls");
        this.componentName = componentName;
        this.structure = structure;
        this.controls = controls;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final CharSequence getStructure() {
        return this.structure;
    }

    @NotNull
    public final List<ControlInfo> getControls() {
        return this.controls;
    }
}
