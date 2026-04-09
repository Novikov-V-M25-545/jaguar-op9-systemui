package com.android.systemui.media;

import android.view.View;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaHierarchyManager.kt */
/* loaded from: classes.dex */
public final class MediaHierarchyManagerKt {
    public static final boolean isShownNotFaded(@NotNull View isShownNotFaded) {
        Object parent;
        Intrinsics.checkParameterIsNotNull(isShownNotFaded, "$this$isShownNotFaded");
        while (isShownNotFaded.getVisibility() == 0 && isShownNotFaded.getAlpha() != 0.0f && (parent = isShownNotFaded.getParent()) != null) {
            if (!(parent instanceof View)) {
                return true;
            }
            isShownNotFaded = (View) parent;
        }
        return false;
    }
}
