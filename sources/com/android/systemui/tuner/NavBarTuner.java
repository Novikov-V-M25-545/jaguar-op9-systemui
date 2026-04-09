package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class NavBarTuner extends TunerPreferenceFragment {
    private static final int[][] ICONS = {new int[]{R.drawable.ic_qs_circle, R.string.tuner_circle}, new int[]{R.drawable.ic_add, R.string.tuner_plus}, new int[]{R.drawable.ic_remove, R.string.tuner_minus}, new int[]{R.drawable.ic_left, R.string.tuner_left}, new int[]{R.drawable.ic_right, R.string.tuner_right}, new int[]{R.drawable.ic_menu, R.string.tuner_menu}};
    private Handler mHandler;
    private final ArrayList<TunerService.Tunable> mTunables = new ArrayList<>();

    @Override // androidx.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        this.mHandler = new Handler();
        super.onCreate(bundle);
    }

    @Override // android.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override // androidx.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(R.xml.nav_bar_tuner);
        bindLayout((ListPreference) findPreference("layout"));
        bindButton("sysui_nav_bar_left", "space", "left");
        bindButton("sysui_nav_bar_right", "menu_ime", "right");
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mTunables.forEach(new Consumer() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda9
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NavBarTuner.lambda$onDestroy$0((TunerService.Tunable) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$onDestroy$0(TunerService.Tunable tunable) {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(tunable);
    }

    private void addTunable(TunerService.Tunable tunable, String... strArr) {
        this.mTunables.add(tunable);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(tunable, strArr);
    }

    private void bindLayout(final ListPreference listPreference) {
        addTunable(new TunerService.Tunable() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda4
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                this.f$0.lambda$bindLayout$2(listPreference, str, str2);
            }
        }, "navbar_layout_views");
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda2
            @Override // androidx.preference.Preference.OnPreferenceChangeListener
            public final boolean onPreferenceChange(Preference preference, Object obj) {
                return NavBarTuner.lambda$bindLayout$3(preference, obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindLayout$2(final ListPreference listPreference, String str, final String str2) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                NavBarTuner.lambda$bindLayout$1(str2, listPreference);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$bindLayout$1(String str, ListPreference listPreference) {
        if (str == null) {
            str = "default";
        }
        listPreference.setValue(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$bindLayout$3(Preference preference, Object obj) {
        String str = (String) obj;
        if ("default".equals(str)) {
            str = null;
        }
        ((TunerService) Dependency.get(TunerService.class)).setValue("navbar_layout_views", str);
        return true;
    }

    private void bindButton(final String str, final String str2, String str3) {
        final ListPreference listPreference = (ListPreference) findPreference("type_" + str3);
        final Preference preferenceFindPreference = findPreference("keycode_" + str3);
        final ListPreference listPreference2 = (ListPreference) findPreference("icon_" + str3);
        setupIcons(listPreference2);
        addTunable(new TunerService.Tunable() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda5
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str4, String str5) {
                this.f$0.lambda$bindButton$5(str2, listPreference, listPreference2, preferenceFindPreference, str4, str5);
            }
        }, str);
        Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda1
            @Override // androidx.preference.Preference.OnPreferenceChangeListener
            public final boolean onPreferenceChange(Preference preference, Object obj) {
                return this.f$0.lambda$bindButton$7(str, listPreference, preferenceFindPreference, listPreference2, preference, obj);
            }
        };
        listPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        listPreference2.setOnPreferenceChangeListener(onPreferenceChangeListener);
        preferenceFindPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda3
            @Override // androidx.preference.Preference.OnPreferenceClickListener
            public final boolean onPreferenceClick(Preference preference) {
                return this.f$0.lambda$bindButton$9(preferenceFindPreference, str, listPreference, listPreference2, preference);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindButton$5(final String str, final ListPreference listPreference, final ListPreference listPreference2, final Preference preference, String str2, final String str3) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() throws NumberFormatException {
                this.f$0.lambda$bindButton$4(str3, str, listPreference, listPreference2, preference);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindButton$4(String str, String str2, ListPreference listPreference, ListPreference listPreference2, Preference preference) throws NumberFormatException {
        if (str == null) {
            str = str2;
        }
        String strExtractButton = NavigationBarInflaterView.extractButton(str);
        if (strExtractButton.startsWith("key")) {
            listPreference.setValue("key");
            String strExtractImage = NavigationBarInflaterView.extractImage(strExtractButton);
            int iExtractKeycode = NavigationBarInflaterView.extractKeycode(strExtractButton);
            listPreference2.setValue(strExtractImage);
            updateSummary(listPreference2);
            preference.setSummary(iExtractKeycode + "");
            preference.setVisible(true);
            listPreference2.setVisible(true);
            return;
        }
        listPreference.setValue(strExtractButton);
        preference.setVisible(false);
        listPreference2.setVisible(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$bindButton$7(final String str, final ListPreference listPreference, final Preference preference, final ListPreference listPreference2, Preference preference2, Object obj) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() throws NumberFormatException {
                this.f$0.lambda$bindButton$6(str, listPreference, preference, listPreference2);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindButton$6(String str, ListPreference listPreference, Preference preference, ListPreference listPreference2) throws NumberFormatException {
        setValue(str, listPreference, preference, listPreference2);
        updateSummary(listPreference2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$bindButton$9(final Preference preference, final String str, final ListPreference listPreference, final ListPreference listPreference2, Preference preference2) {
        final EditText editText = new EditText(getContext());
        new AlertDialog.Builder(getContext()).setTitle(preference2.getTitle()).setView(editText).setNegativeButton(android.R.string.cancel, (DialogInterface.OnClickListener) null).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.systemui.tuner.NavBarTuner$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) throws NumberFormatException {
                this.f$0.lambda$bindButton$8(editText, preference, str, listPreference, listPreference2, dialogInterface, i);
            }
        }).show();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindButton$8(EditText editText, Preference preference, String str, ListPreference listPreference, ListPreference listPreference2, DialogInterface dialogInterface, int i) throws NumberFormatException {
        int i2;
        try {
            i2 = Integer.parseInt(editText.getText().toString());
        } catch (Exception unused) {
            i2 = 66;
        }
        preference.setSummary(i2 + "");
        setValue(str, listPreference, preference, listPreference2);
    }

    private void updateSummary(ListPreference listPreference) throws NumberFormatException {
        try {
            int iApplyDimension = (int) TypedValue.applyDimension(1, 14.0f, getContext().getResources().getDisplayMetrics());
            String str = listPreference.getValue().split("/")[0];
            int i = Integer.parseInt(listPreference.getValue().split("/")[1]);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            Drawable drawableLoadDrawable = Icon.createWithResource(str, i).loadDrawable(getContext());
            drawableLoadDrawable.setTint(-16777216);
            drawableLoadDrawable.setBounds(0, 0, iApplyDimension, iApplyDimension);
            spannableStringBuilder.append("  ", new ImageSpan(drawableLoadDrawable, 1), 0);
            spannableStringBuilder.append((CharSequence) " ");
            int i2 = 0;
            while (true) {
                int[][] iArr = ICONS;
                if (i2 < iArr.length) {
                    if (iArr[i2][0] == i) {
                        spannableStringBuilder.append((CharSequence) getString(iArr[i2][1]));
                    }
                    i2++;
                } else {
                    listPreference.setSummary(spannableStringBuilder);
                    return;
                }
            }
        } catch (Exception e) {
            Log.d("NavButton", "Problem with summary", e);
            listPreference.setSummary((CharSequence) null);
        }
    }

    private void setValue(String str, ListPreference listPreference, Preference preference, ListPreference listPreference2) throws NumberFormatException {
        String value = listPreference.getValue();
        if ("key".equals(value)) {
            String value2 = listPreference2.getValue();
            int i = 66;
            try {
                i = Integer.parseInt(preference.getSummary().toString());
            } catch (Exception unused) {
            }
            value = value + "(" + i + ":" + value2 + ")";
        }
        ((TunerService) Dependency.get(TunerService.class)).setValue(str, value);
    }

    private void setupIcons(ListPreference listPreference) {
        int[][] iArr = ICONS;
        CharSequence[] charSequenceArr = new CharSequence[iArr.length];
        CharSequence[] charSequenceArr2 = new CharSequence[iArr.length];
        int iApplyDimension = (int) TypedValue.applyDimension(1, 14.0f, getContext().getResources().getDisplayMetrics());
        int i = 0;
        while (true) {
            int[][] iArr2 = ICONS;
            if (i < iArr2.length) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                Drawable drawableLoadDrawable = Icon.createWithResource(getContext().getPackageName(), iArr2[i][0]).loadDrawable(getContext());
                drawableLoadDrawable.setTint(-16777216);
                drawableLoadDrawable.setBounds(0, 0, iApplyDimension, iApplyDimension);
                spannableStringBuilder.append("  ", new ImageSpan(drawableLoadDrawable, 1), 0);
                spannableStringBuilder.append((CharSequence) " ");
                spannableStringBuilder.append((CharSequence) getString(iArr2[i][1]));
                charSequenceArr[i] = spannableStringBuilder;
                charSequenceArr2[i] = getContext().getPackageName() + "/" + iArr2[i][0];
                i++;
            } else {
                listPreference.setEntries(charSequenceArr);
                listPreference.setEntryValues(charSequenceArr2);
                return;
            }
        }
    }
}
