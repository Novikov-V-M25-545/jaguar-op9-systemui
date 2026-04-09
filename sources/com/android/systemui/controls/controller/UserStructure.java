package com.android.systemui.controls.controller;

import android.content.Context;
import android.os.Environment;
import android.os.UserHandle;
import java.io.File;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
/* loaded from: classes.dex */
public final class UserStructure {
    private final File auxiliaryFile;
    private final File file;
    private final Context userContext;

    public UserStructure(@NotNull Context context, @NotNull UserHandle user) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(user, "user");
        Context userContext = context.createContextAsUser(user, 0);
        this.userContext = userContext;
        Intrinsics.checkExpressionValueIsNotNull(userContext, "userContext");
        this.file = Environment.buildPath(userContext.getFilesDir(), new String[]{"controls_favorites.xml"});
        Intrinsics.checkExpressionValueIsNotNull(userContext, "userContext");
        this.auxiliaryFile = Environment.buildPath(userContext.getFilesDir(), new String[]{"aux_controls_favorites.xml"});
    }

    public final Context getUserContext() {
        return this.userContext;
    }

    public final File getFile() {
        return this.file;
    }

    public final File getAuxiliaryFile() {
        return this.auxiliaryFile;
    }
}
