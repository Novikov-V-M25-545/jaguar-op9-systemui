package com.android.systemui.qs;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.systemui.R;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
public class PseudoGridView extends ViewGroup {
    private int mHorizontalSpacing;
    private int mNumColumns;
    private int mVerticalSpacing;

    public PseudoGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNumColumns = 3;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.PseudoGridView);
        int indexCount = typedArrayObtainStyledAttributes.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArrayObtainStyledAttributes.getIndex(i);
            if (index == R.styleable.PseudoGridView_numColumns) {
                this.mNumColumns = typedArrayObtainStyledAttributes.getInt(index, 3);
            } else if (index == R.styleable.PseudoGridView_verticalSpacing) {
                this.mVerticalSpacing = typedArrayObtainStyledAttributes.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.PseudoGridView_horizontalSpacing) {
                this.mHorizontalSpacing = typedArrayObtainStyledAttributes.getDimensionPixelSize(index, 0);
            }
        }
        typedArrayObtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (View.MeasureSpec.getMode(i) == 0) {
            throw new UnsupportedOperationException("Needs a maximum width");
        }
        int size = View.MeasureSpec.getSize(i);
        int i3 = this.mNumColumns;
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec((size - ((i3 - 1) * this.mHorizontalSpacing)) / i3, 1073741824);
        int childCount = getChildCount();
        int i4 = ((childCount + r3) - 1) / this.mNumColumns;
        int i5 = 0;
        for (int i6 = 0; i6 < i4; i6++) {
            int i7 = this.mNumColumns;
            int i8 = i6 * i7;
            int iMin = Math.min(i7 + i8, childCount);
            int iMax = 0;
            for (int i9 = i8; i9 < iMin; i9++) {
                View childAt = getChildAt(i9);
                childAt.measure(iMakeMeasureSpec, 0);
                iMax = Math.max(iMax, childAt.getMeasuredHeight());
            }
            int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(iMax, 1073741824);
            while (i8 < iMin) {
                View childAt2 = getChildAt(i8);
                if (childAt2.getMeasuredHeight() != iMax) {
                    childAt2.measure(iMakeMeasureSpec, iMakeMeasureSpec2);
                }
                i8++;
            }
            i5 += iMax;
            if (i6 > 0) {
                i5 += this.mVerticalSpacing;
            }
        }
        setMeasuredDimension(size, ViewGroup.resolveSizeAndState(i5, i2, 0));
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean zIsLayoutRtl = isLayoutRtl();
        int childCount = getChildCount();
        int i5 = ((childCount + r13) - 1) / this.mNumColumns;
        int i6 = 0;
        for (int i7 = 0; i7 < i5; i7++) {
            int width = zIsLayoutRtl ? getWidth() : 0;
            int i8 = this.mNumColumns;
            int i9 = i7 * i8;
            int iMin = Math.min(i8 + i9, childCount);
            int iMax = 0;
            while (i9 < iMin) {
                View childAt = getChildAt(i9);
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                if (zIsLayoutRtl) {
                    width -= measuredWidth;
                }
                childAt.layout(width, i6, width + measuredWidth, i6 + measuredHeight);
                iMax = Math.max(iMax, measuredHeight);
                if (zIsLayoutRtl) {
                    width -= this.mHorizontalSpacing;
                } else {
                    width += measuredWidth + this.mHorizontalSpacing;
                }
                i9++;
            }
            i6 += iMax;
            if (i7 > 0) {
                i6 += this.mVerticalSpacing;
            }
        }
    }

    public static class ViewGroupAdapterBridge extends DataSetObserver {
        private final BaseAdapter mAdapter;
        private boolean mReleased = false;
        private final WeakReference<ViewGroup> mViewGroup;

        public static void link(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            new ViewGroupAdapterBridge(viewGroup, baseAdapter);
        }

        private ViewGroupAdapterBridge(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            this.mViewGroup = new WeakReference<>(viewGroup);
            this.mAdapter = baseAdapter;
            baseAdapter.registerDataSetObserver(this);
            refresh();
        }

        private void refresh() {
            if (this.mReleased) {
                return;
            }
            ViewGroup viewGroup = this.mViewGroup.get();
            if (viewGroup == null) {
                release();
                return;
            }
            int childCount = viewGroup.getChildCount();
            int count = this.mAdapter.getCount();
            int iMax = Math.max(childCount, count);
            int i = 0;
            while (i < iMax) {
                if (i < count) {
                    View childAt = i < childCount ? viewGroup.getChildAt(i) : null;
                    View view = this.mAdapter.getView(i, childAt, viewGroup);
                    if (childAt == null) {
                        viewGroup.addView(view);
                    } else if (childAt != view) {
                        viewGroup.removeViewAt(i);
                        viewGroup.addView(view, i);
                    }
                } else {
                    viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
                }
                i++;
            }
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            refresh();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            release();
        }

        private void release() {
            if (this.mReleased) {
                return;
            }
            this.mReleased = true;
            this.mAdapter.unregisterDataSetObserver(this);
        }
    }
}
