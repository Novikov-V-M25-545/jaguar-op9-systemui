package com.android.systemui.qs.carrier;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class QSCarrierGroup extends LinearLayout {
    public QSCarrierGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    TextView getNoSimTextView() {
        return (TextView) findViewById(R.id.no_carrier_text);
    }

    QSCarrier getCarrier1View() {
        return (QSCarrier) findViewById(R.id.carrier1);
    }

    QSCarrier getCarrier2View() {
        return (QSCarrier) findViewById(R.id.carrier2);
    }

    QSCarrier getCarrier3View() {
        return (QSCarrier) findViewById(R.id.carrier3);
    }

    View getCarrierDivider1() {
        return findViewById(R.id.qs_carrier_divider1);
    }

    View getCarrierDivider2() {
        return findViewById(R.id.qs_carrier_divider2);
    }
}
