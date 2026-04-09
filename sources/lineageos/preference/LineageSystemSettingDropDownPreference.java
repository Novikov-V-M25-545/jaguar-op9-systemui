package lineageos.preference;

import android.content.Context;
import android.util.AttributeSet;
import lineageos.providers.LineageSettings;

/* loaded from: classes2.dex */
public class LineageSystemSettingDropDownPreference extends SelfRemovingDropDownPreference {
    public LineageSystemSettingDropDownPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // lineageos.preference.SelfRemovingDropDownPreference
    protected boolean isPersisted() {
        return LineageSettings.System.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override // lineageos.preference.SelfRemovingDropDownPreference
    protected void putString(String str, String str2) {
        LineageSettings.System.putString(getContext().getContentResolver(), str, str2);
    }

    @Override // lineageos.preference.SelfRemovingDropDownPreference
    protected String getString(String str, String str2) {
        return LineageSettings.System.getString(getContext().getContentResolver(), str, str2);
    }
}
