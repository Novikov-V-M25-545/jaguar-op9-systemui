package com.android.systemui.controls.ui;

import android.service.controls.Control;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlActionCoordinator.kt */
/* loaded from: classes.dex */
public interface ControlActionCoordinator {
    void closeDialogs();

    void drag(boolean z);

    void enableActionOnTouch(@NotNull String str);

    void longPress(@NotNull ControlViewHolder controlViewHolder);

    void setValue(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, float f);

    void toggle(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, boolean z);

    void touch(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, @NotNull Control control);
}
