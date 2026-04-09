package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

/* loaded from: classes.dex */
public class PipUtils {
    public static Pair<ComponentName, Integer> getTopPipActivity(Context context, IActivityManager iActivityManager) {
        int[] iArr;
        try {
            String packageName = context.getPackageName();
            ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
            if (stackInfo != null && (iArr = stackInfo.taskIds) != null && iArr.length > 0) {
                for (int length = stackInfo.taskNames.length - 1; length >= 0; length--) {
                    ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(stackInfo.taskNames[length]);
                    if (componentNameUnflattenFromString != null && !componentNameUnflattenFromString.getPackageName().equals(packageName)) {
                        return new Pair<>(componentNameUnflattenFromString, Integer.valueOf(stackInfo.taskUserIds[length]));
                    }
                }
            }
        } catch (RemoteException unused) {
            Log.w("PipUtils", "Unable to get pinned stack.");
        }
        return new Pair<>(null, 0);
    }
}
