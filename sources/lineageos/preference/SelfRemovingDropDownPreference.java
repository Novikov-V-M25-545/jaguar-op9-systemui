package lineageos.preference;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceViewHolder;

/* loaded from: classes2.dex */
public abstract class SelfRemovingDropDownPreference extends DropDownPreference {
    private final ConstraintsHelper mConstraints;

    protected abstract String getString(String str, String str2);

    protected abstract boolean isPersisted();

    protected abstract void putString(String str, String str2);

    public SelfRemovingDropDownPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mConstraints = new ConstraintsHelper(context, attributeSet, this);
        setPreferenceDataStore(new DataStore());
    }

    @Override // androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mConstraints.onAttached();
    }

    @Override // androidx.preference.DropDownPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        this.mConstraints.onBindViewHolder(preferenceViewHolder);
    }

    @Override // androidx.preference.Preference
    protected void onSetInitialValue(boolean z, Object obj) {
        String string;
        if (z && isPersisted()) {
            string = getString(getKey(), null);
        } else {
            if (obj == null) {
                return;
            }
            string = (String) obj;
            if (shouldPersist()) {
                persistString(string);
            }
        }
        setValue(string);
    }

    private class DataStore extends PreferenceDataStore {
        private DataStore() {
        }

        @Override // androidx.preference.PreferenceDataStore
        public void putString(String str, String str2) {
            SelfRemovingDropDownPreference.this.putString(str, str2);
        }

        @Override // androidx.preference.PreferenceDataStore
        public String getString(String str, String str2) {
            return SelfRemovingDropDownPreference.this.getString(str, str2);
        }
    }
}
