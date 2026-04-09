package com.android.systemui.statusbar.policy;

import android.R;

/* loaded from: classes.dex */
public class WifiIcons {
    public static final int[][] QS_WIFI_SIGNAL_STRENGTH;
    static final int[] WIFI_FULL_ICONS;
    static final int WIFI_LEVEL_COUNT;
    private static final int[] WIFI_NO_INTERNET_ICONS;
    static final int[][] WIFI_SIGNAL_STRENGTH;

    static {
        int[] iArr = {R.drawable.ic_perm_group_bluetooth, R.drawable.ic_perm_group_bookmarks, R.drawable.ic_perm_group_calendar, R.drawable.ic_perm_group_camera, R.drawable.ic_perm_group_device_alarms};
        WIFI_FULL_ICONS = iArr;
        int[] iArr2 = {com.android.systemui.R.drawable.ic_qs_wifi_0, com.android.systemui.R.drawable.ic_qs_wifi_1, com.android.systemui.R.drawable.ic_qs_wifi_2, com.android.systemui.R.drawable.ic_qs_wifi_3, com.android.systemui.R.drawable.ic_qs_wifi_4};
        WIFI_NO_INTERNET_ICONS = iArr2;
        int[][] iArr3 = {iArr2, iArr};
        QS_WIFI_SIGNAL_STRENGTH = iArr3;
        WIFI_SIGNAL_STRENGTH = iArr3;
        WIFI_LEVEL_COUNT = iArr3[0].length;
    }
}
