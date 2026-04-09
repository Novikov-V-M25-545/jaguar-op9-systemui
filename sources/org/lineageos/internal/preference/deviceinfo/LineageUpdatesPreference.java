package org.lineageos.internal.preference.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import androidx.preference.Preference;
import lineageos.preference.SelfRemovingPreference;

/* loaded from: classes2.dex */
public class LineageUpdatesPreference extends SelfRemovingPreference implements Preference.OnPreferenceClickListener {
    public LineageUpdatesPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // lineageos.preference.SelfRemovingPreference, androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        setOnPreferenceClickListener(this);
        setTitle(1057554448);
    }

    @Override // androidx.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        Intent className = new Intent("android.intent.action.MAIN").setClassName("org.lineageos.updater", "org.lineageos.updater.UpdatesActivity");
        try {
            getContext().startActivity(className);
            return true;
        } catch (Exception unused) {
            Log.e("LineageUpdatesPreference", "Unable to start activity " + className.toString());
            return true;
        }
    }
}
