package org.lineageos.internal.preference;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import androidx.preference.EditTextPreference;
import lineageos.providers.LineageSettings;

/* loaded from: classes2.dex */
public class HostnamePreference extends EditTextPreference {
    public HostnamePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setSummary(getText());
    }

    @Override // androidx.preference.EditTextPreference
    public void setText(String str) {
        if (str == null) {
            Log.e("HostnamePreference", "tried to set null hostname, request ignored");
            return;
        }
        String strReplaceAll = str.replaceAll("[^-.a-zA-Z0-9]", "");
        if (TextUtils.isEmpty(strReplaceAll)) {
            Log.w("HostnamePreference", "setting empty hostname");
        } else {
            Log.i("HostnamePreference", "hostname has been set: " + strReplaceAll);
        }
        SystemProperties.set("net.hostname", strReplaceAll);
        persistHostname(strReplaceAll);
        setSummary(strReplaceAll);
    }

    @Override // androidx.preference.EditTextPreference
    public String getText() {
        return SystemProperties.get("net.hostname");
    }

    @Override // androidx.preference.Preference
    public void onSetInitialValue(boolean z, Object obj) {
        persistHostname(getText());
    }

    public void persistHostname(String str) {
        LineageSettings.Secure.putString(getContext().getContentResolver(), "device_hostname", str);
    }
}
