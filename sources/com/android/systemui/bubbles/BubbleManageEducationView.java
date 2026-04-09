package com.android.systemui.bubbles;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.R;

/* loaded from: classes.dex */
public class BubbleManageEducationView extends LinearLayout {
    private TextView mDescTextView;
    private View mManageView;
    private TextView mTitleTextView;

    public BubbleManageEducationView(Context context) {
        this(context, null);
    }

    public BubbleManageEducationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleManageEducationView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BubbleManageEducationView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mManageView = findViewById(R.id.manage_education_view);
        this.mTitleTextView = (TextView) findViewById(R.id.user_education_title);
        this.mDescTextView = (TextView) findViewById(R.id.user_education_description);
        TypedArray typedArrayObtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{android.R.attr.colorAccent, android.R.attr.textColorPrimaryInverse});
        int color = typedArrayObtainStyledAttributes.getColor(0, -16777216);
        int color2 = typedArrayObtainStyledAttributes.getColor(1, -1);
        typedArrayObtainStyledAttributes.recycle();
        int iEnsureTextContrast = ContrastColorUtil.ensureTextContrast(color2, color, true);
        this.mTitleTextView.setTextColor(iEnsureTextContrast);
        this.mDescTextView.setTextColor(iEnsureTextContrast);
    }

    public void setManageViewPosition(int i, int i2) {
        this.mManageView.setTranslationX(i);
        this.mManageView.setTranslationY(i2);
    }

    public int getManageViewHeight() {
        return this.mManageView.getHeight();
    }

    @Override // android.view.View
    public void setLayoutDirection(int i) {
        super.setLayoutDirection(i);
        if (getResources().getConfiguration().getLayoutDirection() == 1) {
            this.mManageView.setBackgroundResource(R.drawable.bubble_stack_user_education_bg_rtl);
            this.mTitleTextView.setGravity(5);
            this.mDescTextView.setGravity(5);
        } else {
            this.mManageView.setBackgroundResource(R.drawable.bubble_stack_user_education_bg);
            this.mTitleTextView.setGravity(3);
            this.mDescTextView.setGravity(3);
        }
    }
}
