package androidx.slice.widget;

import android.content.Context;
import android.net.Uri;

/* loaded from: classes.dex */
class SliceMetricsWrapper extends SliceMetrics {
    private final android.app.slice.SliceMetrics mSliceMetrics;

    SliceMetricsWrapper(Context context, Uri uri) {
        this.mSliceMetrics = new android.app.slice.SliceMetrics(context, uri);
    }

    @Override // androidx.slice.widget.SliceMetrics
    protected void logVisible() {
        this.mSliceMetrics.logVisible();
    }

    @Override // androidx.slice.widget.SliceMetrics
    protected void logHidden() {
        this.mSliceMetrics.logHidden();
    }

    @Override // androidx.slice.widget.SliceMetrics
    protected void logTouch(int i, Uri uri) {
        this.mSliceMetrics.logTouch(i, uri);
    }
}
