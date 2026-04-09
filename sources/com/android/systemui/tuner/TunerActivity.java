package com.android.systemui.tuner;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.fragments.FragmentService;
import java.util.ArrayDeque;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class TunerActivity extends Activity implements PreferenceFragment.OnPreferenceStartFragmentCallback, PreferenceFragment.OnPreferenceStartScreenCallback {
    private final ArrayDeque<String> titleStack = new ArrayDeque<>();

    TunerActivity() {
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        Fragment tunerFragment;
        super.onCreate(bundle);
        getWindow().addFlags(Integer.MIN_VALUE);
        requestWindowFeature(1);
        setContentView(R.layout.tuner_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        if (toolbar != null) {
            setActionBar(toolbar);
        }
        if (getFragmentManager().findFragmentByTag("tuner") == null) {
            String action = getIntent().getAction();
            if ("com.android.settings.action.DEMO_MODE".equals(action)) {
                tunerFragment = new DemoModeFragment();
            } else if ("com.android.settings.action.NAV_BAR_TUNER".equals(action)) {
                tunerFragment = new NavBarTuner();
            } else if ("com.android.settings.action.POWER_NOTIF_CONTROLS".equals(action)) {
                tunerFragment = new PowerNotificationControlsFragment();
            } else if ("com.android.settings.action.STATUS_BAR_TUNER".equals(action)) {
                tunerFragment = new StatusBarTuner();
            } else {
                tunerFragment = new TunerFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.content_frame, tunerFragment, "tuner").commit();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Dependency.destroy(FragmentService.class, new Consumer() { // from class: com.android.systemui.tuner.TunerActivity$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((FragmentService) obj).destroyAll();
            }
        });
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onMenuItemSelected(int i, MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
            return true;
        }
        return super.onMenuItemSelected(i, menuItem);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (getFragmentManager().popBackStackImmediate()) {
            String strPoll = this.titleStack.poll();
            if (strPoll != null) {
                setTitle(strPoll);
            }
            try {
                FragmentManager fragmentManager = getFragmentManager();
                int i = R.id.content_frame;
                Fragment fragmentFindFragmentById = fragmentManager.findFragmentById(i);
                Fragment fragment = (Fragment) fragmentFindFragmentById.getClass().newInstance();
                fragment.setArguments(fragmentFindFragmentById.getArguments());
                FragmentTransaction fragmentTransactionBeginTransaction = getFragmentManager().beginTransaction();
                fragmentTransactionBeginTransaction.replace(i, fragment);
                fragmentTransactionBeginTransaction.commit();
                return;
            } catch (IllegalAccessException | InstantiationException e) {
                Log.d("TunerActivity", "Problem launching fragment", e);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override // androidx.preference.PreferenceFragment.OnPreferenceStartFragmentCallback
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        try {
            Fragment fragment = (Fragment) Class.forName(preference.getFragment()).newInstance();
            Bundle bundle = new Bundle(1);
            bundle.putString("androidx.preference.PreferenceFragmentCompat.PREFERENCE_ROOT", preference.getKey());
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransactionBeginTransaction = getFragmentManager().beginTransaction();
            this.titleStack.push(getTitle().toString());
            setTitle(preference.getTitle());
            fragmentTransactionBeginTransaction.replace(R.id.content_frame, fragment);
            fragmentTransactionBeginTransaction.addToBackStack("PreferenceFragment");
            fragmentTransactionBeginTransaction.commit();
            return true;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            Log.d("TunerActivity", "Problem launching fragment", e);
            return false;
        }
    }

    @Override // androidx.preference.PreferenceFragment.OnPreferenceStartScreenCallback
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        FragmentTransaction fragmentTransactionBeginTransaction = getFragmentManager().beginTransaction();
        SubSettingsFragment subSettingsFragment = new SubSettingsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("androidx.preference.PreferenceFragmentCompat.PREFERENCE_ROOT", preferenceScreen.getKey());
        subSettingsFragment.setArguments(bundle);
        subSettingsFragment.setTargetFragment(preferenceFragment, 0);
        fragmentTransactionBeginTransaction.replace(R.id.content_frame, subSettingsFragment);
        fragmentTransactionBeginTransaction.addToBackStack("PreferenceFragment");
        fragmentTransactionBeginTransaction.commit();
        return true;
    }

    public static class SubSettingsFragment extends PreferenceFragment {
        private PreferenceScreen mParentScreen;

        @Override // androidx.preference.PreferenceFragment
        public void onCreatePreferences(Bundle bundle, String str) {
            this.mParentScreen = (PreferenceScreen) ((PreferenceFragment) getTargetFragment()).getPreferenceScreen().findPreference(str);
            PreferenceScreen preferenceScreenCreatePreferenceScreen = getPreferenceManager().createPreferenceScreen(getPreferenceManager().getContext());
            setPreferenceScreen(preferenceScreenCreatePreferenceScreen);
            while (this.mParentScreen.getPreferenceCount() > 0) {
                Preference preference = this.mParentScreen.getPreference(0);
                this.mParentScreen.removePreference(preference);
                preferenceScreenCreatePreferenceScreen.addPreference(preference);
            }
        }

        @Override // android.app.Fragment
        public void onDestroy() {
            super.onDestroy();
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            while (preferenceScreen.getPreferenceCount() > 0) {
                Preference preference = preferenceScreen.getPreference(0);
                preferenceScreen.removePreference(preference);
                this.mParentScreen.addPreference(preference);
            }
        }
    }
}
