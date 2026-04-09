package com.android.settingslib.net;

import android.R;
import android.content.Context;
import android.telephony.SubscriptionManager;

/* loaded from: classes.dex */
public class SignalStrengthUtil {
    public static boolean shouldInflateSignalStrength(Context context, int i) {
        return SubscriptionManager.getResourcesForSubId(context, i).getBoolean(R.bool.config_cecQuerySadDdp_userConfigurable);
    }
}
