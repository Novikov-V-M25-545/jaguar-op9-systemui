package lineageos.preference;

import android.content.Context;
import android.util.AttributeSet;
import lineageos.providers.LineageSettings;

/* loaded from: classes2.dex */
public class LineageGlobalSettingSwitchPreference extends SelfRemovingSwitchPreference {
    public LineageGlobalSettingSwitchPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected boolean isPersisted() {
        return LineageSettings.Global.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected void putBoolean(String str, boolean z) {
        LineageSettings.Global.putInt(getContext().getContentResolver(), str, z ? 1 : 0);
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected boolean getBoolean(String str, boolean z) {
        return LineageSettings.Global.getInt(getContext().getContentResolver(), str, z ? 1 : 0) != 0;
    }
}
