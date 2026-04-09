package com.android.systemui.controls.management;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.Log;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsRequestReceiver.kt */
/* loaded from: classes.dex */
public final class ControlsRequestReceiver extends BroadcastReceiver {
    public static final Companion Companion = new Companion(null);

    /* compiled from: ControlsRequestReceiver.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final boolean isPackageInForeground(@NotNull Context context, @NotNull String packageName) throws PackageManager.NameNotFoundException {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(packageName, "packageName");
            try {
                int packageUid = context.getPackageManager().getPackageUid(packageName, 0);
                ActivityManager activityManager = (ActivityManager) context.getSystemService(ActivityManager.class);
                if ((activityManager != null ? activityManager.getUidImportance(packageUid) : 1000) == 100) {
                    return true;
                }
                Log.w("ControlsRequestReceiver", "Uid " + packageUid + " not in foreground");
                return false;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.w("ControlsRequestReceiver", "Package " + packageName + " not found");
                return false;
            }
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (context.getPackageManager().hasSystemFeature("android.software.controls")) {
            ComponentName componentName = (ComponentName) intent.getParcelableExtra("android.intent.extra.COMPONENT_NAME");
            String packageName = componentName != null ? componentName.getPackageName() : null;
            if (packageName == null || !Companion.isPackageInForeground(context, packageName)) {
                return;
            }
            Intent intent2 = new Intent(context, (Class<?>) ControlsRequestDialog.class);
            intent2.putExtra("android.intent.extra.COMPONENT_NAME", intent.getParcelableExtra("android.intent.extra.COMPONENT_NAME"));
            intent2.putExtra("android.service.controls.extra.CONTROL", intent.getParcelableExtra("android.service.controls.extra.CONTROL"));
            intent2.addFlags(268566528);
            intent2.putExtra("android.intent.extra.USER_ID", context.getUserId());
            context.startActivityAsUser(intent2, UserHandle.SYSTEM);
        }
    }
}
