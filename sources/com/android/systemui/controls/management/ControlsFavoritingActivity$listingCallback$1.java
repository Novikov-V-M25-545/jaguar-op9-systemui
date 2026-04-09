package com.android.systemui.controls.management;

import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsFavoritingActivity.kt */
/* loaded from: classes.dex */
public final class ControlsFavoritingActivity$listingCallback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$listingCallback$1(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> serviceInfos) {
        Intrinsics.checkParameterIsNotNull(serviceInfos, "serviceInfos");
        if (serviceInfos.size() > 1) {
            ControlsFavoritingActivity.access$getOtherAppsButton$p(this.this$0).post(new Runnable() { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$listingCallback$1$onServicesUpdated$1
                @Override // java.lang.Runnable
                public final void run() {
                    ControlsFavoritingActivity.access$getOtherAppsButton$p(this.this$0.this$0).setVisibility(0);
                }
            });
        }
    }
}
