package org.lineageos.internal.preference.deviceinfo;

import android.content.Context;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/* loaded from: classes2.dex */
public class LineageVendorSecurityPatchTextView extends TextView {
    public LineageVendorSecurityPatchTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(getVendorSecurityPatchLevel());
    }

    private String getVendorSecurityPatchLevel() {
        String str = SystemProperties.get("ro.vendor.build.security_patch");
        if (str.isEmpty()) {
            str = SystemProperties.get("ro.lineage.build.vendor_security_patch");
        }
        if (!str.isEmpty()) {
            try {
                return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy"), new SimpleDateFormat("yyyy-MM-dd").parse(str)).toString();
            } catch (ParseException unused) {
                return str;
            }
        }
        return getContext().getResources().getString(1057554527);
    }
}
