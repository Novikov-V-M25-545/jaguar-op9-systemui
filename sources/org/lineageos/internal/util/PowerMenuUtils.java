package org.lineageos.internal.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.UserHandle;
import lineageos.providers.LineageSettings;

/* loaded from: classes2.dex */
public final class PowerMenuUtils {
    public static boolean isAdvancedRestartPossible(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        boolean z = keyguardManager.inKeyguardRestrictedInputMode() && keyguardManager.isKeyguardSecure();
        boolean z2 = LineageSettings.Secure.getInt(context.getContentResolver(), "advanced_reboot", 1) == 1;
        boolean z3 = LineageSettings.Secure.getInt(context.getContentResolver(), "advanced_reboot_secured", 1) == 1;
        boolean z4 = UserHandle.getCallingUserId() == 0;
        if (z2) {
            return (!z || z3) && z4;
        }
        return false;
    }
}
