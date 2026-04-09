package org.lineageos.internal.preference.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/* loaded from: classes2.dex */
public class LineageVersionTextView extends TextView implements View.OnClickListener {
    private long[] mHits;

    public LineageVersionTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHits = new long[3];
        setText(SystemProperties.get("ro.modversion", getContext().getResources().getString(1057554527)));
        setOnClickListener(this);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        long[] jArr = this.mHits;
        System.arraycopy(jArr, 1, jArr, 0, jArr.length - 1);
        long[] jArr2 = this.mHits;
        jArr2[jArr2.length - 1] = SystemClock.uptimeMillis();
        if (this.mHits[0] >= SystemClock.uptimeMillis() - 500) {
            launchUrl("https://www.crdroid.net");
        }
    }

    private void launchUrl(String str) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str));
        try {
            getContext().startActivity(intent);
        } catch (Exception unused) {
            Log.e("LineageVersionTextView", "Unable to start activity " + intent.toString());
        }
    }
}
