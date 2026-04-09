package com.android.internal.util.custom.faceunlock;

import android.os.SystemProperties;

/* loaded from: classes.dex */
public class FaceUnlockUtils {
    public static boolean isFaceUnlockSupported() {
        return SystemProperties.getBoolean("ro.face_unlock_service.enabled", false);
    }
}
