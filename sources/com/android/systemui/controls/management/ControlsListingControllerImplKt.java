package com.android.systemui.controls.management;

import android.content.Context;
import com.android.settingslib.applications.ServiceListing;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsListingControllerImpl.kt */
/* loaded from: classes.dex */
public final class ControlsListingControllerImplKt {
    /* JADX INFO: Access modifiers changed from: private */
    public static final ServiceListing createServiceListing(Context context) {
        ServiceListing.Builder builder = new ServiceListing.Builder(context);
        builder.setIntentAction("android.service.controls.ControlsProviderService");
        builder.setPermission("android.permission.BIND_CONTROLS");
        builder.setNoun("Controls Provider");
        builder.setSetting("controls_providers");
        builder.setTag("controls_providers");
        builder.setAddDeviceLockedFlags(true);
        ServiceListing serviceListingBuild = builder.build();
        Intrinsics.checkExpressionValueIsNotNull(serviceListingBuild, "ServiceListing.Builder(c…Flags(true)\n    }.build()");
        return serviceListingBuild;
    }
}
