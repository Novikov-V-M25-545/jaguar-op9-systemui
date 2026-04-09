package lineageos.preference;

import android.content.Context;
import android.util.AttributeSet;
import lineageos.providers.LineageSettings;

/* loaded from: classes2.dex */
public class LineageSecureSettingSwitchPreference extends SelfRemovingSwitchPreference {
    public LineageSecureSettingSwitchPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected boolean isPersisted() {
        return LineageSettings.Secure.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected void putBoolean(String str, boolean z) {
        LineageSettings.Secure.putInt(getContext().getContentResolver(), str, z ? 1 : 0);
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected boolean getBoolean(String str, boolean z) {
        return LineageSettings.Secure.getInt(getContext().getContentResolver(), str, z ? 1 : 0) != 0;
    }
}
