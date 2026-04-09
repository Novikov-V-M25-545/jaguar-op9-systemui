package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import java.text.DecimalFormat;

/* loaded from: classes.dex */
public class DataUsageDetailView extends LinearLayout {
    private final DecimalFormat FORMAT;

    public DataUsageDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.FORMAT = new DecimalFormat("#.##");
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = R.dimen.qs_data_usage_text_size;
        FontSizeUtils.updateFontSize(this, android.R.id.title, i);
        FontSizeUtils.updateFontSize(this, R.id.usage_text, R.dimen.qs_data_usage_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_carrier_text, i);
        FontSizeUtils.updateFontSize(this, R.id.usage_info_top_text, i);
        FontSizeUtils.updateFontSize(this, R.id.usage_period_text, i);
        FontSizeUtils.updateFontSize(this, R.id.usage_info_bottom_text, i);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0076  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void bind(com.android.settingslib.net.DataUsageController.DataUsageInfo r24) throws android.content.res.Resources.NotFoundException {
        /*
            Method dump skipped, instructions count: 294
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.DataUsageDetailView.bind(com.android.settingslib.net.DataUsageController$DataUsageInfo):void");
    }

    private CharSequence formatDataUsage(long j) {
        Formatter.BytesResult bytes = Formatter.formatBytes(((LinearLayout) this).mContext.getResources(), j, 8);
        return BidiFormatter.getInstance().unicodeWrap(((LinearLayout) this).mContext.getString(android.R.string.duration_days_relative, bytes.value, bytes.units));
    }
}
