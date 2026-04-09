package com.android.settingslib.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

/* loaded from: classes.dex */
public class BarChartPreference extends Preference {
    private static final int[] BAR_VIEWS = {R$id.bar_view1, R$id.bar_view2, R$id.bar_view3, R$id.bar_view4};
    private boolean mIsLoading;
    private int mMaxBarHeight;

    public BarChartPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        preferenceViewHolder.setDividerAllowedAbove(true);
        preferenceViewHolder.setDividerAllowedBelow(true);
        bindChartTitleView(preferenceViewHolder);
        bindChartDetailsView(preferenceViewHolder);
        if (this.mIsLoading) {
            preferenceViewHolder.itemView.setVisibility(4);
        } else {
            preferenceViewHolder.itemView.setVisibility(0);
            throw null;
        }
    }

    private void init() {
        setSelectable(false);
        setLayoutResource(R$layout.settings_bar_chart);
        this.mMaxBarHeight = getContext().getResources().getDimensionPixelSize(R$dimen.settings_bar_view_max_height);
    }

    private void bindChartTitleView(PreferenceViewHolder preferenceViewHolder) {
        throw null;
    }

    private void bindChartDetailsView(PreferenceViewHolder preferenceViewHolder) {
        throw null;
    }
}
