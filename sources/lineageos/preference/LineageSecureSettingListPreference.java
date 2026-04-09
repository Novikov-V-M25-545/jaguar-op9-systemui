package lineageos.preference;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import lineageos.providers.LineageSettings;

/* loaded from: classes2.dex */
public class LineageSecureSettingListPreference extends SelfRemovingListPreference {
    private boolean mAutoSummary;

    public LineageSecureSettingListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAutoSummary = false;
    }

    @Override // androidx.preference.ListPreference
    public void setValue(String str) {
        super.setValue(str);
        if (this.mAutoSummary || TextUtils.isEmpty(getSummary())) {
            setSummary(getEntry(), true);
        }
    }

    @Override // androidx.preference.ListPreference, androidx.preference.Preference
    public void setSummary(CharSequence charSequence) {
        setSummary(charSequence, false);
    }

    private void setSummary(CharSequence charSequence, boolean z) {
        this.mAutoSummary = z;
        super.setSummary(charSequence);
    }

    @Override // lineageos.preference.SelfRemovingListPreference
    protected boolean isPersisted() {
        return LineageSettings.Secure.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override // lineageos.preference.SelfRemovingListPreference
    protected void putString(String str, String str2) {
        LineageSettings.Secure.putString(getContext().getContentResolver(), str, str2);
    }

    @Override // lineageos.preference.SelfRemovingListPreference
    protected String getString(String str, String str2) {
        return LineageSettings.Secure.getString(getContext().getContentResolver(), str, str2);
    }
}
