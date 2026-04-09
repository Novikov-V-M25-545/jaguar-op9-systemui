package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsUiControllerImpl.kt */
/* loaded from: classes.dex */
final class SelectionItem {

    @NotNull
    private final CharSequence appName;

    @NotNull
    private final ComponentName componentName;

    @NotNull
    private final Drawable icon;

    @NotNull
    private final CharSequence structure;

    public static /* synthetic */ SelectionItem copy$default(SelectionItem selectionItem, CharSequence charSequence, CharSequence charSequence2, Drawable drawable, ComponentName componentName, int i, Object obj) {
        if ((i & 1) != 0) {
            charSequence = selectionItem.appName;
        }
        if ((i & 2) != 0) {
            charSequence2 = selectionItem.structure;
        }
        if ((i & 4) != 0) {
            drawable = selectionItem.icon;
        }
        if ((i & 8) != 0) {
            componentName = selectionItem.componentName;
        }
        return selectionItem.copy(charSequence, charSequence2, drawable, componentName);
    }

    @NotNull
    public final SelectionItem copy(@NotNull CharSequence appName, @NotNull CharSequence structure, @NotNull Drawable icon, @NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(appName, "appName");
        Intrinsics.checkParameterIsNotNull(structure, "structure");
        Intrinsics.checkParameterIsNotNull(icon, "icon");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        return new SelectionItem(appName, structure, icon, componentName);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SelectionItem)) {
            return false;
        }
        SelectionItem selectionItem = (SelectionItem) obj;
        return Intrinsics.areEqual(this.appName, selectionItem.appName) && Intrinsics.areEqual(this.structure, selectionItem.structure) && Intrinsics.areEqual(this.icon, selectionItem.icon) && Intrinsics.areEqual(this.componentName, selectionItem.componentName);
    }

    public int hashCode() {
        CharSequence charSequence = this.appName;
        int iHashCode = (charSequence != null ? charSequence.hashCode() : 0) * 31;
        CharSequence charSequence2 = this.structure;
        int iHashCode2 = (iHashCode + (charSequence2 != null ? charSequence2.hashCode() : 0)) * 31;
        Drawable drawable = this.icon;
        int iHashCode3 = (iHashCode2 + (drawable != null ? drawable.hashCode() : 0)) * 31;
        ComponentName componentName = this.componentName;
        return iHashCode3 + (componentName != null ? componentName.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "SelectionItem(appName=" + this.appName + ", structure=" + this.structure + ", icon=" + this.icon + ", componentName=" + this.componentName + ")";
    }

    public SelectionItem(@NotNull CharSequence appName, @NotNull CharSequence structure, @NotNull Drawable icon, @NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(appName, "appName");
        Intrinsics.checkParameterIsNotNull(structure, "structure");
        Intrinsics.checkParameterIsNotNull(icon, "icon");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        this.appName = appName;
        this.structure = structure;
        this.icon = icon;
        this.componentName = componentName;
    }

    @NotNull
    public final CharSequence getStructure() {
        return this.structure;
    }

    @NotNull
    public final Drawable getIcon() {
        return this.icon;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final CharSequence getTitle() {
        return this.structure.length() == 0 ? this.appName : this.structure;
    }
}
