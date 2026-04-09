package com.android.systemui.controls.ui;

import android.content.ContentProvider;
import android.graphics.drawable.Icon;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CanUseIconPredicate.kt */
/* loaded from: classes.dex */
public final class CanUseIconPredicate implements Function1<Icon, Boolean> {
    private final int currentUserId;

    public CanUseIconPredicate(int i) {
        this.currentUserId = i;
    }

    @Override // kotlin.jvm.functions.Function1
    @NotNull
    public Boolean invoke(@NotNull Icon icon) {
        Intrinsics.checkParameterIsNotNull(icon, "icon");
        boolean z = true;
        if ((icon.getType() == 4 || icon.getType() == 6) && ContentProvider.getUserIdFromUri(icon.getUri(), this.currentUserId) != this.currentUserId) {
            z = false;
        }
        return Boolean.valueOf(z);
    }
}
