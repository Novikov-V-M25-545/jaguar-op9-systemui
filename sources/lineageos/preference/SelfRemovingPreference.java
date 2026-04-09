package lineageos.preference;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.R$attr;

/* loaded from: classes2.dex */
public class SelfRemovingPreference extends Preference {
    private final ConstraintsHelper mConstraints;

    public SelfRemovingPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mConstraints = new ConstraintsHelper(context, attributeSet, this);
    }

    public SelfRemovingPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SelfRemovingPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, ConstraintsHelper.getAttr(context, R$attr.preferenceStyle, R.attr.preferenceStyle));
    }

    @Override // androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        this.mConstraints.onAttached();
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        this.mConstraints.onBindViewHolder(preferenceViewHolder);
    }

    public void setAvailable(boolean z) {
        this.mConstraints.setAvailable(z);
    }

    public boolean isAvailable() {
        return this.mConstraints.isAvailable();
    }
}
