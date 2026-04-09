package com.android.settingslib;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

/* loaded from: classes.dex */
public class TwoTargetPreference extends Preference {
    private int mIconSize;
    private int mMediumIconSize;
    private int mSmallIconSize;

    protected int getSecondTargetResId() {
        return 0;
    }

    public TwoTargetPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init(context);
    }

    public TwoTargetPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        setLayoutResource(R$layout.preference_two_target);
        this.mSmallIconSize = context.getResources().getDimensionPixelSize(R$dimen.two_target_pref_small_icon_size);
        this.mMediumIconSize = context.getResources().getDimensionPixelSize(R$dimen.two_target_pref_medium_icon_size);
        int secondTargetResId = getSecondTargetResId();
        if (secondTargetResId != 0) {
            setWidgetLayoutResource(secondTargetResId);
        }
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        ImageView imageView = (ImageView) preferenceViewHolder.itemView.findViewById(R.id.icon);
        int i = this.mIconSize;
        if (i == 1) {
            int i2 = this.mMediumIconSize;
            imageView.setLayoutParams(new LinearLayout.LayoutParams(i2, i2));
        } else if (i == 2) {
            int i3 = this.mSmallIconSize;
            imageView.setLayoutParams(new LinearLayout.LayoutParams(i3, i3));
        }
        View viewFindViewById = preferenceViewHolder.findViewById(R$id.two_target_divider);
        View viewFindViewById2 = preferenceViewHolder.findViewById(R.id.widget_frame);
        boolean zShouldHideSecondTarget = shouldHideSecondTarget();
        if (viewFindViewById != null) {
            viewFindViewById.setVisibility(zShouldHideSecondTarget ? 8 : 0);
        }
        if (viewFindViewById2 != null) {
            viewFindViewById2.setVisibility(zShouldHideSecondTarget ? 8 : 0);
        }
    }

    protected boolean shouldHideSecondTarget() {
        return getSecondTargetResId() == 0;
    }
}
