package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class NonOverlappingLinearLayout extends LinearLayout {
    boolean mDeferFocusableViewAvailableInLayout;
    boolean mFocusableViewAvailableFixEnabled;
    final ArrayList<ArrayList<View>> mSortedAvailableViews;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public NonOverlappingLinearLayout(Context context) {
        this(context, null);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFocusableViewAvailableFixEnabled = false;
        this.mSortedAvailableViews = new ArrayList<>();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v1, types: [int] */
    /* JADX WARN: Type inference failed for: r0v6 */
    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2;
        int size;
        ?? r0 = 0;
        int i5 = 0;
        try {
            boolean z3 = this.mFocusableViewAvailableFixEnabled && getOrientation() == 0 && getLayoutDirection() == 1;
            this.mDeferFocusableViewAvailableInLayout = z3;
            if (z3) {
                while (this.mSortedAvailableViews.size() > getChildCount()) {
                    ArrayList<ArrayList<View>> arrayList = this.mSortedAvailableViews;
                    arrayList.remove(arrayList.size() - 1);
                }
                while (this.mSortedAvailableViews.size() < getChildCount()) {
                    this.mSortedAvailableViews.add(new ArrayList<>());
                }
            }
            super.onLayout(z, i, i2, i3, i4);
            if (this.mDeferFocusableViewAvailableInLayout) {
                for (int i6 = 0; i6 < this.mSortedAvailableViews.size(); i6++) {
                    for (int i7 = 0; i7 < this.mSortedAvailableViews.get(i6).size(); i7++) {
                        super.focusableViewAvailable(this.mSortedAvailableViews.get(i6).get(i7));
                    }
                }
            }
            if (z2) {
                while (true) {
                    if (i5 >= size) {
                        return;
                    }
                }
            }
        } finally {
            if (this.mDeferFocusableViewAvailableInLayout) {
                this.mDeferFocusableViewAvailableInLayout = false;
                while (r0 < this.mSortedAvailableViews.size()) {
                    this.mSortedAvailableViews.get(r0).clear();
                    r0++;
                }
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void focusableViewAvailable(View view) {
        int iIndexOfChild;
        if (!this.mDeferFocusableViewAvailableInLayout) {
            super.focusableViewAvailable(view);
            return;
        }
        for (View view2 = view; view2 != this && view2 != null; view2 = (View) view2.getParent()) {
            if (view2.getParent() == this) {
                iIndexOfChild = indexOfChild(view2);
                break;
            }
        }
        iIndexOfChild = -1;
        if (iIndexOfChild != -1) {
            this.mSortedAvailableViews.get(iIndexOfChild).add(view);
        }
    }
}
