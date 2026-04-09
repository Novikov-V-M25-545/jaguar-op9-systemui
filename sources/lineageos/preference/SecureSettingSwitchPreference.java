package lineageos.preference;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;

/* loaded from: classes2.dex */
public class SecureSettingSwitchPreference extends SelfRemovingSwitchPreference {
    public SecureSettingSwitchPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected boolean isPersisted() {
        return Settings.Secure.getString(getContext().getContentResolver(), getKey()) != null;
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected void putBoolean(String str, boolean z) {
        Settings.Secure.putInt(getContext().getContentResolver(), str, z ? 1 : 0);
    }

    @Override // lineageos.preference.SelfRemovingSwitchPreference
    protected boolean getBoolean(String str, boolean z) {
        return Settings.Secure.getInt(getContext().getContentResolver(), str, z ? 1 : 0) != 0;
    }
}
