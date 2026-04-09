package lineageos.os;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.SparseArray;

/* loaded from: classes2.dex */
public class Build {
    private static final SparseArray<String> sdkMap;
    public static final String CRDROID_VERSION = getString("ro.jaguar.version");
    public static final String CRDROID_DISPLAY_VERSION = getString("ro.jaguar.display.version");

    public static class LINEAGE_VERSION {
        public static final int SDK_INT = SystemProperties.getInt("ro.lineage.build.version.plat.sdk", 0);
    }

    static {
        SparseArray<String> sparseArray = new SparseArray<>();
        sdkMap = sparseArray;
        sparseArray.put(1, "Apricot");
        sparseArray.put(2, "Boysenberry");
        sparseArray.put(3, "Cantaloupe");
        sparseArray.put(4, "Dragon Fruit");
        sparseArray.put(5, "Elderberry");
        sparseArray.put(6, "Fig");
        sparseArray.put(7, "Guava");
        sparseArray.put(8, "Hackberry");
        sparseArray.put(9, "Ilama");
    }

    public static String getNameForSDKInt(int i) {
        String str = sdkMap.get(i);
        return TextUtils.isEmpty(str) ? "unknown" : str;
    }

    private static String getString(String str) {
        return SystemProperties.get(str, "unknown");
    }
}
