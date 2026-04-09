package org.lineageos.internal.preference.deviceinfo;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.widget.TextView;

/* loaded from: classes2.dex */
public class LineageBuildDateTextView extends TextView {
    public LineageBuildDateTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(SystemProperties.get("ro.build.date", getContext().getResources().getString(1057554527)));
    }
}
