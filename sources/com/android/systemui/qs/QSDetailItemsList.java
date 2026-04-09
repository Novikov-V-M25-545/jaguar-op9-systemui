package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class QSDetailItemsList extends FrameLayout {
    private View mEmpty;
    private ImageView mEmptyIcon;
    private TextView mEmptyText;
    private ListView mListView;

    public QSDetailItemsList(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static QSDetailItemsList convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        if (view instanceof QSDetailItemsList) {
            return (QSDetailItemsList) view;
        }
        return (QSDetailItemsList) LayoutInflater.from(context).inflate(R.layout.qs_detail_items_list, viewGroup, false);
    }

    public void setAdapter(ListAdapter listAdapter) {
        this.mListView.setAdapter(listAdapter);
    }

    public ListView getListView() {
        return this.mListView;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        ListView listView = (ListView) findViewById(android.R.id.list);
        this.mListView = listView;
        listView.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.qs.QSDetailItemsList.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        View viewFindViewById = findViewById(android.R.id.empty);
        this.mEmpty = viewFindViewById;
        viewFindViewById.setVisibility(8);
        this.mEmptyText = (TextView) this.mEmpty.findViewById(android.R.id.title);
        this.mEmptyIcon = (ImageView) this.mEmpty.findViewById(android.R.id.icon);
        this.mListView.setEmptyView(this.mEmpty);
    }
}
