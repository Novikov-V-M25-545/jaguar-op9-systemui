package androidx.slice.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.slice.view.R$styleable;

/* loaded from: classes.dex */
public class RowStyle {
    private int mActionDividerHeight;
    private int mBottomDividerEndPadding;
    private int mBottomDividerStartPadding;
    private int mContentEndPadding;
    private int mContentStartPadding;
    private boolean mDisableRecyclerViewItemAnimator;
    private int mEndItemEndPadding;
    private int mEndItemStartPadding;
    private int mIconSize;
    private int mProgressBarEndPadding;
    private int mProgressBarInlineWidth;
    private int mProgressBarStartPadding;
    private int mSeekBarInlineWidth;
    private int mSubContentEndPadding;
    private int mSubContentStartPadding;
    private int mTitleEndPadding;
    private int mTitleItemEndPadding;
    private int mTitleItemStartPadding;
    private int mTitleStartPadding;

    public RowStyle(Context context, int i) throws Resources.NotFoundException {
        TypedArray typedArrayObtainStyledAttributes = context.getTheme().obtainStyledAttributes(i, R$styleable.RowStyle);
        try {
            this.mTitleItemStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_titleItemStartPadding, -1.0f);
            this.mTitleItemEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_titleItemEndPadding, -1.0f);
            this.mContentStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_contentStartPadding, -1.0f);
            this.mContentEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_contentEndPadding, -1.0f);
            this.mTitleStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_titleStartPadding, -1.0f);
            this.mTitleEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_titleEndPadding, -1.0f);
            this.mSubContentStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_subContentStartPadding, -1.0f);
            this.mSubContentEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_subContentEndPadding, -1.0f);
            this.mEndItemStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_endItemStartPadding, -1.0f);
            this.mEndItemEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_endItemEndPadding, -1.0f);
            this.mBottomDividerStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_bottomDividerStartPadding, -1.0f);
            this.mBottomDividerEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_bottomDividerEndPadding, -1.0f);
            this.mActionDividerHeight = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_actionDividerHeight, -1.0f);
            this.mSeekBarInlineWidth = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_seekBarInlineWidth, -1.0f);
            this.mProgressBarInlineWidth = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_progressBarInlineWidth, -1.0f);
            this.mProgressBarStartPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_progressBarStartPadding, -1.0f);
            this.mProgressBarEndPadding = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_progressBarEndPadding, -1.0f);
            this.mIconSize = (int) typedArrayObtainStyledAttributes.getDimension(R$styleable.RowStyle_iconSize, -1.0f);
            this.mDisableRecyclerViewItemAnimator = typedArrayObtainStyledAttributes.getBoolean(R$styleable.RowStyle_disableRecyclerViewItemAnimator, false);
        } finally {
            typedArrayObtainStyledAttributes.recycle();
        }
    }

    public int getTitleItemStartPadding() {
        return this.mTitleItemStartPadding;
    }

    public int getTitleItemEndPadding() {
        return this.mTitleItemEndPadding;
    }

    public int getContentStartPadding() {
        return this.mContentStartPadding;
    }

    public int getContentEndPadding() {
        return this.mContentEndPadding;
    }

    public int getTitleStartPadding() {
        return this.mTitleStartPadding;
    }

    public int getTitleEndPadding() {
        return this.mTitleEndPadding;
    }

    public int getSubContentStartPadding() {
        return this.mSubContentStartPadding;
    }

    public int getSubContentEndPadding() {
        return this.mSubContentEndPadding;
    }

    public int getEndItemStartPadding() {
        return this.mEndItemStartPadding;
    }

    public int getEndItemEndPadding() {
        return this.mEndItemEndPadding;
    }

    public int getBottomDividerStartPadding() {
        return this.mBottomDividerStartPadding;
    }

    public int getBottomDividerEndPadding() {
        return this.mBottomDividerEndPadding;
    }

    public int getActionDividerHeight() {
        return this.mActionDividerHeight;
    }

    public int getSeekBarInlineWidth() {
        return this.mSeekBarInlineWidth;
    }

    public int getProgressBarInlineWidth() {
        return this.mProgressBarInlineWidth;
    }

    public int getProgressBarStartPadding() {
        return this.mProgressBarStartPadding;
    }

    public int getProgressBarEndPadding() {
        return this.mProgressBarEndPadding;
    }

    public int getIconSize() {
        return this.mIconSize;
    }

    public boolean getDisableRecyclerViewItemAnimator() {
        return this.mDisableRecyclerViewItemAnimator;
    }
}
