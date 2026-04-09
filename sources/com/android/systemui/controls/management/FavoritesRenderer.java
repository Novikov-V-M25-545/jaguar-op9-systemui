package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.res.Resources;
import com.android.systemui.R;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppAdapter.kt */
/* loaded from: classes.dex */
public final class FavoritesRenderer {
    private final Function1<ComponentName, Integer> favoriteFunction;
    private final Resources resources;

    /* JADX WARN: Multi-variable type inference failed */
    public FavoritesRenderer(@NotNull Resources resources, @NotNull Function1<? super ComponentName, Integer> favoriteFunction) {
        Intrinsics.checkParameterIsNotNull(resources, "resources");
        Intrinsics.checkParameterIsNotNull(favoriteFunction, "favoriteFunction");
        this.resources = resources;
        this.favoriteFunction = favoriteFunction;
    }

    @Nullable
    public final String renderFavoritesForComponent(@NotNull ComponentName component) {
        Intrinsics.checkParameterIsNotNull(component, "component");
        int iIntValue = this.favoriteFunction.invoke(component).intValue();
        if (iIntValue != 0) {
            return this.resources.getQuantityString(R.plurals.controls_number_of_favorites, iIntValue, Integer.valueOf(iIntValue));
        }
        return null;
    }
}
