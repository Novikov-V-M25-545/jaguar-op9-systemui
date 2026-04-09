package com.android.systemui.controls.dagger;

import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import dagger.Lazy;
import java.util.Optional;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsComponent.kt */
/* loaded from: classes.dex */
public final class ControlsComponent {
    private final boolean featureEnabled;
    private final Lazy<ControlsController> lazyControlsController;
    private final Lazy<ControlsListingController> lazyControlsListingController;
    private final Lazy<ControlsUiController> lazyControlsUiController;

    public ControlsComponent(boolean z, @NotNull Lazy<ControlsController> lazyControlsController, @NotNull Lazy<ControlsUiController> lazyControlsUiController, @NotNull Lazy<ControlsListingController> lazyControlsListingController) {
        Intrinsics.checkParameterIsNotNull(lazyControlsController, "lazyControlsController");
        Intrinsics.checkParameterIsNotNull(lazyControlsUiController, "lazyControlsUiController");
        Intrinsics.checkParameterIsNotNull(lazyControlsListingController, "lazyControlsListingController");
        this.featureEnabled = z;
        this.lazyControlsController = lazyControlsController;
        this.lazyControlsUiController = lazyControlsUiController;
        this.lazyControlsListingController = lazyControlsListingController;
    }

    @NotNull
    public final Optional<ControlsController> getControlsController() {
        Optional<ControlsController> optionalEmpty;
        String str;
        if (this.featureEnabled) {
            optionalEmpty = Optional.of(this.lazyControlsController.get());
            str = "Optional.of(lazyControlsController.get())";
        } else {
            optionalEmpty = Optional.empty();
            str = "Optional.empty()";
        }
        Intrinsics.checkExpressionValueIsNotNull(optionalEmpty, str);
        return optionalEmpty;
    }

    @NotNull
    public final Optional<ControlsUiController> getControlsUiController() {
        Optional<ControlsUiController> optionalEmpty;
        String str;
        if (this.featureEnabled) {
            optionalEmpty = Optional.of(this.lazyControlsUiController.get());
            str = "Optional.of(lazyControlsUiController.get())";
        } else {
            optionalEmpty = Optional.empty();
            str = "Optional.empty()";
        }
        Intrinsics.checkExpressionValueIsNotNull(optionalEmpty, str);
        return optionalEmpty;
    }

    @NotNull
    public final Optional<ControlsListingController> getControlsListingController() {
        if (this.featureEnabled) {
            Optional<ControlsListingController> optionalOf = Optional.of(this.lazyControlsListingController.get());
            Intrinsics.checkExpressionValueIsNotNull(optionalOf, "Optional.of(lazyControlsListingController.get())");
            return optionalOf;
        }
        Optional<ControlsListingController> optionalEmpty = Optional.empty();
        Intrinsics.checkExpressionValueIsNotNull(optionalEmpty, "Optional.empty()");
        return optionalEmpty;
    }
}
