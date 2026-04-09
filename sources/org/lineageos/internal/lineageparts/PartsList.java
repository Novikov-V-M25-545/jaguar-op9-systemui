package org.lineageos.internal.lineageparts;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.util.Log;
import java.util.Map;

/* loaded from: classes2.dex */
public class PartsList {
    private static PartsList sInstance;
    private final Context mContext;
    private final Map<String, PartInfo> mParts = new ArrayMap();
    private static final boolean DEBUG = Log.isLoggable(PartsList.class.getSimpleName(), 2);
    public static final ComponentName LINEAGEPARTS_ACTIVITY = new ComponentName("org.lineageos.lineageparts", "org.lineageos.lineageparts.PartsActivity");
    private static final Object sInstanceLock = new Object();

    private PartsList(Context context) {
        this.mContext = context;
        loadParts();
    }

    public static PartsList get(Context context) {
        PartsList partsList;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new PartsList(context);
            }
            partsList = sInstance;
        }
        return partsList;
    }

    private void loadParts() {
        Resources resourcesForApplication;
        synchronized (this.mParts) {
            try {
                resourcesForApplication = this.mContext.getPackageManager().getResourcesForApplication("org.lineageos.lineageparts");
            } catch (PackageManager.NameNotFoundException unused) {
            }
            if (resourcesForApplication == null) {
                return;
            }
            int identifier = resourcesForApplication.getIdentifier("parts_catalog", "xml", "org.lineageos.lineageparts");
            if (identifier > 0) {
                loadPartsFromResourceLocked(resourcesForApplication, identifier, this.mParts);
            }
        }
    }

    public PartInfo getPartInfo(String str) {
        PartInfo partInfo;
        synchronized (this.mParts) {
            partInfo = this.mParts.get(str);
        }
        return partInfo;
    }

    /* JADX WARN: Code restructure failed: missing block: B:56:0x00e2, code lost:
    
        r12.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x00e5, code lost:
    
        return;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void loadPartsFromResourceLocked(android.content.res.Resources r11, int r12, java.util.Map<java.lang.String, org.lineageos.internal.lineageparts.PartInfo> r13) throws java.lang.Throwable {
        /*
            Method dump skipped, instructions count: 296
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: org.lineageos.internal.lineageparts.PartsList.loadPartsFromResourceLocked(android.content.res.Resources, int, java.util.Map):void");
    }
}
