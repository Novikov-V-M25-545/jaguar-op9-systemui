package lineageos.preference;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

/* loaded from: classes2.dex */
public abstract class SelfRemovingSwitchPreference extends SwitchPreference {
    private final ConstraintsHelper mConstraints;

    protected abstract boolean getBoolean(String str, boolean z);

    protected abstract boolean isPersisted();

    protected abstract void putBoolean(String str, boolean z);

    public SelfRemovingSwitchPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mConstraints = new ConstraintsHelper(context, attributeSet, this);
        setPreferenceDataStore(new DataStore());
    }

    @Override // androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mConstraints.onAttached();
    }

    @Override // androidx.preference.SwitchPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        this.mConstraints.onBindViewHolder(preferenceViewHolder);
    }

    @Override // androidx.preference.Preference
    protected void onSetInitialValue(boolean z, Object obj) {
        boolean z2;
        if (z && isPersisted()) {
            z2 = getBoolean(getKey(), false);
        } else {
            if (obj == null) {
                return;
            }
            z2 = getBoolean(getKey(), ((Boolean) obj).booleanValue());
            if (shouldPersist()) {
                persistBoolean(z2);
            }
        }
        setChecked(z2);
    }

    private class DataStore extends PreferenceDataStore {
        private DataStore() {
        }

        @Override // androidx.preference.PreferenceDataStore
        public void putBoolean(String str, boolean z) {
            SelfRemovingSwitchPreference.this.putBoolean(str, z);
        }

        @Override // androidx.preference.PreferenceDataStore
        public boolean getBoolean(String str, boolean z) {
            return SelfRemovingSwitchPreference.this.getBoolean(str, z);
        }
    }
}
