package androidx.slice.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import androidx.collection.ArraySet;
import androidx.core.util.ObjectsCompat;
import androidx.slice.SliceSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/* loaded from: classes.dex */
public class CompatPinnedList {
    private final Context mContext;
    private final String mPrefsName;

    public CompatPinnedList(Context context, String str) {
        this.mContext = context;
        this.mPrefsName = str;
    }

    private SharedPreferences getPrefs() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(this.mPrefsName, 0);
        long j = sharedPreferences.getLong("last_boot", 0L);
        long bootTime = getBootTime();
        if (Math.abs(j - bootTime) > 2000) {
            sharedPreferences.edit().clear().putLong("last_boot", bootTime).apply();
        }
        return sharedPreferences;
    }

    public List<Uri> getPinnedSlices() {
        ArrayList arrayList = new ArrayList();
        for (String str : getPrefs().getAll().keySet()) {
            if (str.startsWith("pinned_")) {
                Uri uri = Uri.parse(str.substring(7));
                if (!getPins(uri).isEmpty()) {
                    arrayList.add(uri);
                }
            }
        }
        return arrayList;
    }

    private Set<String> getPins(Uri uri) {
        return getPrefs().getStringSet("pinned_" + uri.toString(), new ArraySet());
    }

    public synchronized ArraySet<SliceSpec> getSpecs(Uri uri) {
        ArraySet<SliceSpec> arraySet = new ArraySet<>();
        SharedPreferences prefs = getPrefs();
        String string = prefs.getString("spec_names_" + uri.toString(), null);
        String string2 = prefs.getString("spec_revs_" + uri.toString(), null);
        if (!TextUtils.isEmpty(string) && !TextUtils.isEmpty(string2)) {
            String[] strArrSplit = string.split(",", -1);
            String[] strArrSplit2 = string2.split(",", -1);
            if (strArrSplit.length != strArrSplit2.length) {
                return new ArraySet<>();
            }
            for (int i = 0; i < strArrSplit.length; i++) {
                arraySet.add(new SliceSpec(strArrSplit[i], Integer.parseInt(strArrSplit2[i])));
            }
            return arraySet;
        }
        return new ArraySet<>();
    }

    private void setPins(Uri uri, Set<String> set) {
        getPrefs().edit().putStringSet("pinned_" + uri.toString(), set).apply();
    }

    private void setSpecs(Uri uri, ArraySet<SliceSpec> arraySet) {
        String[] strArr = new String[arraySet.size()];
        String[] strArr2 = new String[arraySet.size()];
        for (int i = 0; i < arraySet.size(); i++) {
            strArr[i] = arraySet.valueAt(i).getType();
            strArr2[i] = String.valueOf(arraySet.valueAt(i).getRevision());
        }
        getPrefs().edit().putString("spec_names_" + uri.toString(), TextUtils.join(",", strArr)).putString("spec_revs_" + uri.toString(), TextUtils.join(",", strArr2)).apply();
    }

    protected long getBootTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    public synchronized boolean addPin(Uri uri, String str, Set<SliceSpec> set) {
        boolean zIsEmpty;
        Set<String> pins = getPins(uri);
        zIsEmpty = pins.isEmpty();
        pins.add(str);
        setPins(uri, pins);
        if (zIsEmpty) {
            setSpecs(uri, new ArraySet<>(set));
        } else {
            setSpecs(uri, mergeSpecs(getSpecs(uri), set));
        }
        return zIsEmpty;
    }

    public synchronized boolean removePin(Uri uri, String str) {
        Set<String> pins = getPins(uri);
        if (!pins.isEmpty() && pins.contains(str)) {
            pins.remove(str);
            setPins(uri, pins);
            setSpecs(uri, new ArraySet<>());
            return pins.size() == 0;
        }
        return false;
    }

    private static ArraySet<SliceSpec> mergeSpecs(ArraySet<SliceSpec> arraySet, Set<SliceSpec> set) {
        int i;
        int i2 = 0;
        while (i2 < arraySet.size()) {
            SliceSpec sliceSpecValueAt = arraySet.valueAt(i2);
            SliceSpec sliceSpecFindSpec = findSpec(set, sliceSpecValueAt.getType());
            if (sliceSpecFindSpec == null) {
                i = i2 - 1;
                arraySet.removeAt(i2);
            } else if (sliceSpecFindSpec.getRevision() < sliceSpecValueAt.getRevision()) {
                i = i2 - 1;
                arraySet.removeAt(i2);
                arraySet.add(sliceSpecFindSpec);
            } else {
                i2++;
            }
            i2 = i;
            i2++;
        }
        return arraySet;
    }

    private static SliceSpec findSpec(Set<SliceSpec> set, String str) {
        for (SliceSpec sliceSpec : set) {
            if (ObjectsCompat.equals(sliceSpec.getType(), str)) {
                return sliceSpec;
            }
        }
        return null;
    }
}
