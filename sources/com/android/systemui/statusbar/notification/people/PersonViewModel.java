package com.android.systemui.statusbar.notification.people;

import android.graphics.drawable.Drawable;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHub.kt */
/* loaded from: classes.dex */
public final class PersonViewModel {

    @NotNull
    private final Drawable icon;

    @NotNull
    private final CharSequence name;

    @NotNull
    private final Function0<Unit> onClick;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PersonViewModel)) {
            return false;
        }
        PersonViewModel personViewModel = (PersonViewModel) obj;
        return Intrinsics.areEqual(this.name, personViewModel.name) && Intrinsics.areEqual(this.icon, personViewModel.icon) && Intrinsics.areEqual(this.onClick, personViewModel.onClick);
    }

    public int hashCode() {
        CharSequence charSequence = this.name;
        int iHashCode = (charSequence != null ? charSequence.hashCode() : 0) * 31;
        Drawable drawable = this.icon;
        int iHashCode2 = (iHashCode + (drawable != null ? drawable.hashCode() : 0)) * 31;
        Function0<Unit> function0 = this.onClick;
        return iHashCode2 + (function0 != null ? function0.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "PersonViewModel(name=" + this.name + ", icon=" + this.icon + ", onClick=" + this.onClick + ")";
    }

    @NotNull
    public final Drawable getIcon() {
        return this.icon;
    }

    @NotNull
    public final Function0<Unit> getOnClick() {
        return this.onClick;
    }
}
