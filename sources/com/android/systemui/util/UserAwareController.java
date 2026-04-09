package com.android.systemui.util;

import android.os.UserHandle;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: UserAwareController.kt */
/* loaded from: classes.dex */
public interface UserAwareController {
    default void changeUser(@NotNull UserHandle newUser) {
        Intrinsics.checkParameterIsNotNull(newUser, "newUser");
    }

    int getCurrentUserId();
}
