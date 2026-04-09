package com.android.systemui.qs.carrier;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.DualToneHandler;
import com.android.systemui.R;
import com.android.systemui.qs.QuickStatusBarHeader;
import java.util.Objects;

/* loaded from: classes.dex */
public class QSCarrier extends LinearLayout {
    private TextView mCarrierText;
    private float mColorForegroundIntensity;
    private ColorStateList mColorForegroundStateList;
    private DualToneHandler mDualToneHandler;
    private CellSignalState mLastSignalState;
    private View mMobileGroup;
    private ImageView mMobileRoaming;
    private ImageView mMobileSignal;

    public QSCarrier(Context context) {
        super(context);
    }

    public QSCarrier(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public QSCarrier(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public QSCarrier(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDualToneHandler = new DualToneHandler(getContext());
        this.mMobileGroup = findViewById(R.id.mobile_combo);
        this.mMobileSignal = (ImageView) findViewById(R.id.mobile_signal);
        this.mMobileRoaming = (ImageView) findViewById(R.id.mobile_roaming);
        this.mCarrierText = (TextView) findViewById(R.id.qs_carrier_text);
        this.mMobileSignal.setImageDrawable(new SignalDrawable(((LinearLayout) this).mContext));
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(((LinearLayout) this).mContext, android.R.attr.colorForeground);
        this.mColorForegroundStateList = ColorStateList.valueOf(colorAttrDefaultColor);
        this.mColorForegroundIntensity = QuickStatusBarHeader.getColorIntensity(colorAttrDefaultColor);
    }

    public boolean updateState(CellSignalState cellSignalState) {
        if (Objects.equals(cellSignalState, this.mLastSignalState)) {
            return false;
        }
        this.mLastSignalState = cellSignalState;
        this.mMobileGroup.setVisibility(cellSignalState.visible ? 0 : 8);
        if (!cellSignalState.visible) {
            return true;
        }
        this.mMobileRoaming.setVisibility(cellSignalState.roaming ? 0 : 8);
        ColorStateList colorStateListValueOf = ColorStateList.valueOf(this.mDualToneHandler.getSingleColor(this.mColorForegroundIntensity));
        this.mMobileRoaming.setImageTintList(colorStateListValueOf);
        this.mMobileSignal.setImageTintList(colorStateListValueOf);
        this.mMobileSignal.setImageLevel(cellSignalState.mobileSignalIconId);
        StringBuilder sb = new StringBuilder();
        String str = cellSignalState.contentDescription;
        if (str != null) {
            sb.append(str);
            sb.append(", ");
        }
        if (cellSignalState.roaming) {
            sb.append(((LinearLayout) this).mContext.getString(R.string.data_connection_roaming));
            sb.append(", ");
        }
        if (hasValidTypeContentDescription(cellSignalState.typeContentDescription)) {
            sb.append(cellSignalState.typeContentDescription);
        }
        this.mMobileSignal.setContentDescription(sb);
        return true;
    }

    private boolean hasValidTypeContentDescription(String str) {
        return TextUtils.equals(str, ((LinearLayout) this).mContext.getString(R.string.data_connection_no_internet)) || TextUtils.equals(str, ((LinearLayout) this).mContext.getString(R.string.cell_data_off_content_description)) || TextUtils.equals(str, ((LinearLayout) this).mContext.getString(R.string.not_default_data_content_description));
    }

    public void setCarrierText(CharSequence charSequence) {
        this.mCarrierText.setText(charSequence);
    }
}
