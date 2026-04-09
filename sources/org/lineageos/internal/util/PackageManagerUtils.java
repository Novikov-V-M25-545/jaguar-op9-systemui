package org.lineageos.internal.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/* loaded from: classes2.dex */
public final class PackageManagerUtils {
    public static boolean isAppEnabled(Context context, String str, int i) {
        ApplicationInfo applicationInfo = getApplicationInfo(context, str, i);
        return applicationInfo != null && applicationInfo.enabled;
    }

    public static boolean isAppEnabled(Context context, String str) {
        return isAppEnabled(context, str, 0);
    }

    public static ApplicationInfo getApplicationInfo(Context context, String str, int i) {
        try {
            return context.getPackageManager().getApplicationInfo(str, i);
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }
}
