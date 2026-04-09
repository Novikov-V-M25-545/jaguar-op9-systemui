package com.android.systemui.globalactions;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class GlobalActionsPopupMenu extends ListPopupWindow {
    private ListAdapter mAdapter;
    private Context mContext;
    private int mGlobalActionsSidePadding;
    private boolean mIsDropDownMode;
    private int mMenuVerticalPadding;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener;

    public GlobalActionsPopupMenu(Context context, boolean z) {
        super(context);
        this.mMenuVerticalPadding = 0;
        this.mGlobalActionsSidePadding = 0;
        this.mContext = context;
        Resources resources = context.getResources();
        setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_bg_full, context.getTheme()));
        this.mIsDropDownMode = z;
        setWindowLayoutType(2020);
        setInputMethodMode(2);
        setModal(true);
        this.mGlobalActionsSidePadding = resources.getDimensionPixelSize(R.dimen.global_actions_side_margin);
        if (z) {
            return;
        }
        this.mMenuVerticalPadding = resources.getDimensionPixelSize(R.dimen.control_menu_vertical_padding);
    }

    @Override // android.widget.ListPopupWindow
    public void setAdapter(ListAdapter listAdapter) {
        this.mAdapter = listAdapter;
        super.setAdapter(listAdapter);
    }

    @Override // android.widget.ListPopupWindow
    public void show() {
        super.show();
        if (this.mOnItemLongClickListener != null) {
            getListView().setOnItemLongClickListener(this.mOnItemLongClickListener);
        }
        ListView listView = getListView();
        Resources resources = this.mContext.getResources();
        setVerticalOffset((-getAnchorView().getHeight()) / 2);
        if (this.mIsDropDownMode) {
            listView.setDividerHeight(resources.getDimensionPixelSize(R.dimen.control_list_divider));
            listView.setDivider(resources.getDrawable(R.drawable.controls_list_divider_inset));
        } else {
            if (this.mAdapter == null) {
                return;
            }
            double d = Resources.getSystem().getDisplayMetrics().widthPixels;
            int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) (0.9d * d), Integer.MIN_VALUE);
            int iMax = 0;
            for (int i = 0; i < this.mAdapter.getCount(); i++) {
                View view = this.mAdapter.getView(i, null, listView);
                view.measure(iMakeMeasureSpec, 0);
                iMax = Math.max(view.getMeasuredWidth(), iMax);
            }
            int iMax2 = Math.max(iMax, (int) (d * 0.5d));
            int i2 = this.mMenuVerticalPadding;
            listView.setPadding(0, i2, 0, i2);
            setWidth(iMax2);
            if (getAnchorView().getLayoutDirection() == 0) {
                setHorizontalOffset((getAnchorView().getWidth() - this.mGlobalActionsSidePadding) - iMax2);
            } else {
                setHorizontalOffset(this.mGlobalActionsSidePadding);
            }
        }
        super.show();
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }
}
