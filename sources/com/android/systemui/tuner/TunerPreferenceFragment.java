package com.android.systemui.tuner;

import androidx.preference.ListPreferenceDialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import com.android.systemui.tuner.CustomListPreference;

/* loaded from: classes.dex */
public abstract class TunerPreferenceFragment extends PreferenceFragment {
    @Override // androidx.preference.PreferenceFragment, androidx.preference.PreferenceManager.OnDisplayPreferenceDialogListener
    public void onDisplayPreferenceDialog(Preference preference) {
        ListPreferenceDialogFragment listPreferenceDialogFragmentNewInstance;
        if (preference instanceof CustomListPreference) {
            listPreferenceDialogFragmentNewInstance = CustomListPreference.CustomListPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            listPreferenceDialogFragmentNewInstance = null;
        }
        listPreferenceDialogFragmentNewInstance.setTargetFragment(this, 0);
        listPreferenceDialogFragmentNewInstance.show(getFragmentManager(), "dialog_preference");
    }
}
