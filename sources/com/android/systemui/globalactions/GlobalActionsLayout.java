package com.android.systemui.globalactions;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.HardwareBgDrawable;
import com.android.systemui.MultiListLayout;
import com.android.systemui.R;
import com.android.systemui.util.leak.RotationUtils;
import java.util.Locale;

/* loaded from: classes.dex */
public abstract class GlobalActionsLayout extends MultiListLayout {
    boolean mBackgroundsSet;

    protected abstract boolean shouldReverseListItems();

    public GlobalActionsLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void setBackgrounds() {
        HardwareBgDrawable backgroundDrawable;
        ViewGroup listView = getListView();
        HardwareBgDrawable backgroundDrawable2 = getBackgroundDrawable(getResources().getColor(R.color.global_actions_grid_background, null));
        if (backgroundDrawable2 != null) {
            listView.setBackground(backgroundDrawable2);
        }
        if (getSeparatedView() == null || (backgroundDrawable = getBackgroundDrawable(getResources().getColor(R.color.global_actions_separated_background, null))) == null) {
            return;
        }
        getSeparatedView().setBackground(backgroundDrawable);
    }

    protected HardwareBgDrawable getBackgroundDrawable(int i) {
        HardwareBgDrawable hardwareBgDrawable = new HardwareBgDrawable(true, true, getContext());
        hardwareBgDrawable.setTint(i);
        return hardwareBgDrawable;
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (getListView() == null || this.mBackgroundsSet) {
            return;
        }
        setBackgrounds();
        this.mBackgroundsSet = true;
    }

    protected void addToListView(View view, boolean z) {
        if (z) {
            getListView().addView(view, 0);
        } else {
            getListView().addView(view);
        }
    }

    protected void addToSeparatedView(View view, boolean z) {
        ViewGroup separatedView = getSeparatedView();
        if (separatedView == null) {
            addToListView(view, z);
        } else if (z) {
            separatedView.addView(view, 0);
        } else {
            separatedView.addView(view);
        }
    }

    @VisibleForTesting
    protected int getCurrentLayoutDirection() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
    }

    @VisibleForTesting
    protected int getCurrentRotation() {
        return RotationUtils.getRotation(((LinearLayout) this).mContext);
    }

    @Override // com.android.systemui.MultiListLayout
    public void onUpdateList() {
        View view;
        super.onUpdateList();
        ViewGroup separatedView = getSeparatedView();
        ViewGroup listView = getListView();
        for (int i = 0; i < this.mAdapter.getCount(); i++) {
            boolean zShouldBeSeparated = this.mAdapter.shouldBeSeparated(i);
            if (zShouldBeSeparated) {
                view = this.mAdapter.getView(i, null, separatedView);
            } else {
                view = this.mAdapter.getView(i, null, listView);
            }
            if (zShouldBeSeparated) {
                addToSeparatedView(view, false);
            } else {
                addToListView(view, shouldReverseListItems());
            }
        }
    }

    @Override // com.android.systemui.MultiListLayout
    protected ViewGroup getSeparatedView() {
        return (ViewGroup) findViewById(R.id.separated_button);
    }

    @Override // com.android.systemui.MultiListLayout
    protected ViewGroup getListView() {
        return (ViewGroup) findViewById(android.R.id.list);
    }

    protected View getWrapper() {
        return getChildAt(0);
    }
}
