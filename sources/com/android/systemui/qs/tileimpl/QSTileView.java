package com.android.systemui.qs.tileimpl;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import java.util.Objects;

/* loaded from: classes.dex */
public class QSTileView extends QSTileBaseView {
    private ColorStateList mColorLabelActive;
    private ColorStateList mColorLabelDefault;
    private ColorStateList mColorLabelUnavailable;
    private View mDivider;
    private View mExpandIndicator;
    private View mExpandSpace;
    protected TextView mLabel;
    private ViewGroup mLabelContainer;
    private ImageView mPadLock;
    protected TextView mSecondLine;
    private int mState;

    public QSTileView(Context context, QSIconView qSIconView) {
        this(context, qSIconView, false);
    }

    public QSTileView(Context context, QSIconView qSIconView, boolean z) {
        super(context, qSIconView, z);
        setClipChildren(false);
        setClipToPadding(false);
        setClickable(true);
        setId(View.generateViewId());
        createLabel();
        setOrientation(1);
        setGravity(49);
        this.mColorLabelDefault = Utils.getColorAttr(getContext(), R.attr.textColorPrimary);
        this.mColorLabelActive = Utils.getColorAttr(getContext(), R.attr.colorAccent);
        this.mColorLabelUnavailable = Utils.getColorAttr(getContext(), R.attr.textColorSecondary);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        TextView textView = this.mLabel;
        int i = com.android.systemui.R.dimen.qs_tile_text_size;
        FontSizeUtils.updateFontSize(textView, i);
        FontSizeUtils.updateFontSize(this.mSecondLine, i);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView, com.android.systemui.plugins.qs.QSTileView
    public int getDetailY() {
        return getTop() + this.mLabelContainer.getTop() + (this.mLabelContainer.getHeight() / 2);
    }

    protected void createLabel() {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(com.android.systemui.R.layout.qs_tile_label, (ViewGroup) this, false);
        this.mLabelContainer = viewGroup;
        viewGroup.setClipChildren(false);
        this.mLabelContainer.setClipToPadding(false);
        this.mLabel = (TextView) this.mLabelContainer.findViewById(com.android.systemui.R.id.tile_label);
        this.mPadLock = (ImageView) this.mLabelContainer.findViewById(com.android.systemui.R.id.restricted_padlock);
        this.mDivider = this.mLabelContainer.findViewById(com.android.systemui.R.id.underline);
        this.mExpandIndicator = this.mLabelContainer.findViewById(com.android.systemui.R.id.expand_indicator);
        this.mExpandSpace = this.mLabelContainer.findViewById(com.android.systemui.R.id.expand_space);
        this.mSecondLine = (TextView) this.mLabelContainer.findViewById(com.android.systemui.R.id.app_label);
        addView(this.mLabelContainer);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mLabel.getLineCount() > 2 || (!TextUtils.isEmpty(this.mSecondLine.getText()) && this.mSecondLine.getLineHeight() > this.mSecondLine.getHeight())) {
            this.mLabel.setSingleLine();
            super.onMeasure(i, i2);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView
    protected void handleStateChanged(QSTile.State state) throws Resources.NotFoundException {
        super.handleStateChanged(state);
        if (!Objects.equals(this.mLabel.getText(), state.label) || this.mState != state.state) {
            this.mLabel.setTextColor(state.state == 0 ? this.mColorLabelUnavailable : this.mColorLabelDefault);
            this.mState = state.state;
            this.mLabel.setText(state.label);
        }
        if (!Objects.equals(this.mSecondLine.getText(), state.secondaryLabel)) {
            this.mSecondLine.setText(state.secondaryLabel);
            this.mSecondLine.setVisibility(TextUtils.isEmpty(state.secondaryLabel) ? 8 : 0);
        }
        boolean z = Settings.System.getIntForUser(getContext().getContentResolver(), "qs_panel_bg_use_new_tint", 0, -2) == 1;
        boolean z2 = getContext().getResources().getBoolean(com.android.systemui.R.bool.config_enable_qs_tile_tinting);
        if (z || z2) {
            int i = state.state;
            if (i == 2) {
                this.mLabel.setTextColor(this.mColorLabelActive);
            } else if (i == 1) {
                this.mLabel.setTextColor(this.mColorLabelDefault);
            }
        }
        boolean z3 = state.dualTarget;
        this.mExpandIndicator.setVisibility(z3 ? 0 : 8);
        this.mExpandSpace.setVisibility(z3 ? 0 : 8);
        this.mLabelContainer.setContentDescription(z3 ? state.dualLabelContentDescription : null);
        if (z3 != this.mLabelContainer.isClickable()) {
            this.mLabelContainer.setClickable(z3);
            this.mLabelContainer.setLongClickable(z3);
            this.mLabelContainer.setBackground(z3 ? newTileBackground() : null);
        }
        this.mLabel.setEnabled(true ^ state.disabledByPolicy);
        this.mPadLock.setVisibility(state.disabledByPolicy ? 0 : 8);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView
    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        super.init(onClickListener, onClickListener2, onLongClickListener);
        this.mLabelContainer.setOnClickListener(onClickListener2);
        this.mLabelContainer.setOnLongClickListener(onLongClickListener);
        this.mLabelContainer.setClickable(false);
        this.mLabelContainer.setLongClickable(false);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView, com.android.systemui.plugins.qs.QSTileView
    public void textVisibility() {
        if (Settings.System.getIntForUser(((LinearLayout) this).mContext.getContentResolver(), "qs_tile_title_visibility", 1, -2) == 1) {
            this.mLabelContainer.setVisibility(0);
        } else {
            this.mLabelContainer.setVisibility(8);
        }
    }
}
