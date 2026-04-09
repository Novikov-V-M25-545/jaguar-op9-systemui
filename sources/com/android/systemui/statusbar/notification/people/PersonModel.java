package com.android.systemui.statusbar.notification.people;

import android.graphics.drawable.Drawable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHub.kt */
/* loaded from: classes.dex */
public final class PersonModel {

    @NotNull
    private final Drawable avatar;

    @NotNull
    private final Runnable clickRunnable;

    @NotNull
    private final String key;

    @NotNull
    private final CharSequence name;
    private final int userId;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PersonModel)) {
            return false;
        }
        PersonModel personModel = (PersonModel) obj;
        return Intrinsics.areEqual(this.key, personModel.key) && this.userId == personModel.userId && Intrinsics.areEqual(this.name, personModel.name) && Intrinsics.areEqual(this.avatar, personModel.avatar) && Intrinsics.areEqual(this.clickRunnable, personModel.clickRunnable);
    }

    public int hashCode() {
        String str = this.key;
        int iHashCode = (((str != null ? str.hashCode() : 0) * 31) + Integer.hashCode(this.userId)) * 31;
        CharSequence charSequence = this.name;
        int iHashCode2 = (iHashCode + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        Drawable drawable = this.avatar;
        int iHashCode3 = (iHashCode2 + (drawable != null ? drawable.hashCode() : 0)) * 31;
        Runnable runnable = this.clickRunnable;
        return iHashCode3 + (runnable != null ? runnable.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "PersonModel(key=" + this.key + ", userId=" + this.userId + ", name=" + this.name + ", avatar=" + this.avatar + ", clickRunnable=" + this.clickRunnable + ")";
    }

    public PersonModel(@NotNull String key, int i, @NotNull CharSequence name, @NotNull Drawable avatar, @NotNull Runnable clickRunnable) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(name, "name");
        Intrinsics.checkParameterIsNotNull(avatar, "avatar");
        Intrinsics.checkParameterIsNotNull(clickRunnable, "clickRunnable");
        this.key = key;
        this.userId = i;
        this.name = name;
        this.avatar = avatar;
        this.clickRunnable = clickRunnable;
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    public final int getUserId() {
        return this.userId;
    }
}
