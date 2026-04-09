package androidx.slice.widget;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

/* loaded from: classes.dex */
class SliceMetrics {
    protected void logHidden() {
        throw null;
    }

    protected void logTouch(int i, Uri uri) {
        throw null;
    }

    protected void logVisible() {
        throw null;
    }

    SliceMetrics() {
    }

    public static SliceMetrics getInstance(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= 28) {
            return new SliceMetricsWrapper(context, uri);
        }
        return null;
    }
}
