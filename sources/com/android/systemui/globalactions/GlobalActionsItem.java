package com.android.systemui.globalactions;

import android.R;
import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

/* loaded from: classes.dex */
public class GlobalActionsItem extends LinearLayout {
    public GlobalActionsItem(Context context) {
        super(context);
    }

    public GlobalActionsItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public GlobalActionsItem(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private TextView getTextView() {
        return (TextView) findViewById(R.id.message);
    }

    public void setMarquee(boolean z) {
        TextView textView = getTextView();
        textView.setSingleLine(z);
        textView.setEllipsize(z ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
    }

    public boolean isTruncated() {
        Layout layout;
        TextView textView = getTextView();
        return textView != null && (layout = textView.getLayout()) != null && layout.getLineCount() > 0 && layout.getEllipsisCount(layout.getLineCount() - 1) > 0;
    }
}
