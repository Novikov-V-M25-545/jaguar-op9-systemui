package org.lineageos.internal.preference.deviceinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import lineageos.os.Build;

/* loaded from: classes2.dex */
public class LineageAPIVersionTextView extends TextView {
    public LineageAPIVersionTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int i = Build.LINEAGE_VERSION.SDK_INT;
        StringBuilder sb = new StringBuilder();
        sb.append(Build.getNameForSDKInt(i));
        sb.append(" (" + i + ")");
        setText(sb.toString());
    }
}
