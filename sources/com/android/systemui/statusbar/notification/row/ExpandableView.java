package com.android.systemui.statusbar.notification.row;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public abstract class ExpandableView extends FrameLayout implements Dumpable {
    private static Rect mClipRect = new Rect();
    private int mActualHeight;
    private boolean mChangingPosition;
    protected int mClipBottomAmount;
    private boolean mClipToActualHeight;
    protected int mClipTopAmount;
    protected int mContentShift;
    protected float mContentTransformationAmount;
    private float mContentTranslation;
    protected float mExtraWidthForClipping;
    protected boolean mFirstInSection;
    private boolean mInShelf;
    protected boolean mIsLastChild;
    protected boolean mLastInSection;
    private ArrayList<View> mMatchParentViews;
    private int mMinClipTopAmount;
    protected int mMinimumHeightForClipping;
    protected OnHeightChangedListener mOnHeightChangedListener;
    private boolean mTransformingInShelf;
    private ViewGroup mTransientContainer;
    private final ExpandableViewState mViewState;
    private boolean mWillBeGone;

    public interface OnHeightChangedListener {
        void onHeightChanged(ExpandableView expandableView, boolean z);

        void onReset(ExpandableView expandableView);
    }

    protected void applyContentTransformation(float f, float f2) {
    }

    public boolean areChildrenExpanded() {
        return false;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public int getExtraBottomPadding() {
        return 0;
    }

    public float getHeaderVisibleAmount() {
        return 1.0f;
    }

    public float getIncreasedPaddingAmount() {
        return 0.0f;
    }

    public float getOutlineAlpha() {
        return 0.0f;
    }

    public int getOutlineTranslation() {
        return 0;
    }

    public StatusBarIconView getShelfIcon() {
        return null;
    }

    public View getShelfTransformationTarget() {
        return null;
    }

    public boolean hasExpandingChild() {
        return false;
    }

    public boolean hasNoContentHeight() {
        return false;
    }

    public boolean isAboveShelf() {
        return false;
    }

    public boolean isChildInGroup() {
        return false;
    }

    public boolean isContentExpandable() {
        return false;
    }

    public boolean isExpandAnimationRunning() {
        return false;
    }

    public boolean isGroupExpanded() {
        return false;
    }

    public boolean isGroupExpansionChanging() {
        return false;
    }

    public boolean isHeadsUpAnimatingAway() {
        return false;
    }

    public boolean isPinned() {
        return false;
    }

    public boolean isRemoved() {
        return false;
    }

    public boolean isSummaryWithChildren() {
        return false;
    }

    public boolean isTransparent() {
        return false;
    }

    public boolean mustStayOnScreen() {
        return false;
    }

    public boolean needsClippingToShelf() {
        return true;
    }

    public abstract void performAddAnimation(long j, long j2, boolean z);

    public abstract long performRemoveAnimation(long j, long j2, float f, boolean z, float f2, Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter);

    public void setActualHeightAnimating(boolean z) {
    }

    public void setBelowSpeedBump(boolean z) {
    }

    public boolean setBottomRoundness(float f, boolean z) {
        return false;
    }

    public void setDimmed(boolean z, boolean z2) {
    }

    public void setDistanceToTopRoundness(float f) {
    }

    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
    }

    public void setHeadsUpIsVisible() {
    }

    public void setHideSensitive(boolean z, boolean z2, long j, long j2) {
    }

    public void setHideSensitiveForIntrinsicHeight(boolean z) {
    }

    public boolean setTopRoundness(float f, boolean z) {
        return false;
    }

    protected boolean shouldClipToActualHeight() {
        return true;
    }

    public boolean showingPulsing() {
        return false;
    }

    public ExpandableView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMinimumHeightForClipping = 0;
        this.mExtraWidthForClipping = 0.0f;
        this.mMatchParentViews = new ArrayList<>();
        this.mMinClipTopAmount = 0;
        this.mClipToActualHeight = true;
        this.mChangingPosition = false;
        this.mViewState = createExpandableViewState();
        initDimens();
    }

    private void initDimens() {
        this.mContentShift = getResources().getDimensionPixelSize(R.dimen.shelf_transform_content_shift);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i2);
        int paddingStart = getPaddingStart() + getPaddingEnd();
        int mode = View.MeasureSpec.getMode(i2);
        int iMin = Integer.MAX_VALUE;
        if (mode != 0 && size != 0) {
            iMin = Math.min(size, Integer.MAX_VALUE);
        }
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(iMin, Integer.MIN_VALUE);
        int childCount = getChildCount();
        int iMax = 0;
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                int i4 = layoutParams.height;
                if (i4 != -1) {
                    childAt.measure(FrameLayout.getChildMeasureSpec(i, paddingStart, layoutParams.width), i4 >= 0 ? View.MeasureSpec.makeMeasureSpec(Math.min(i4, iMin), 1073741824) : iMakeMeasureSpec);
                    iMax = Math.max(iMax, childAt.getMeasuredHeight());
                } else {
                    this.mMatchParentViews.add(childAt);
                }
            }
        }
        if (mode != 1073741824) {
            size = Math.min(iMin, iMax);
        }
        int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(size, 1073741824);
        Iterator<View> it = this.mMatchParentViews.iterator();
        while (it.hasNext()) {
            View next = it.next();
            next.measure(FrameLayout.getChildMeasureSpec(i, paddingStart, next.getLayoutParams().width), iMakeMeasureSpec2);
        }
        this.mMatchParentViews.clear();
        setMeasuredDimension(View.MeasureSpec.getSize(i), size);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateClipping();
    }

    public boolean pointInView(float f, float f2, float f3) {
        return f >= (-f3) && f2 >= ((float) this.mClipTopAmount) - f3 && f < ((float) (((FrameLayout) this).mRight - ((FrameLayout) this).mLeft)) + f3 && f2 < ((float) this.mActualHeight) + f3;
    }

    public void setActualHeight(int i, boolean z) {
        this.mActualHeight = i;
        updateClipping();
        if (z) {
            notifyHeightChanged(false);
        }
    }

    public void setActualHeight(int i) {
        setActualHeight(i, true);
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public int getMaxContentHeight() {
        return getHeight();
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean z) {
        return getHeight();
    }

    public int getCollapsedHeight() {
        return getHeight();
    }

    public int getIntrinsicHeight() {
        return getHeight();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateClipping();
    }

    public int getClipTopAmount() {
        return this.mClipTopAmount;
    }

    public int getClipBottomAmount() {
        return this.mClipBottomAmount;
    }

    public void setOnHeightChangedListener(OnHeightChangedListener onHeightChangedListener) {
        this.mOnHeightChangedListener = onHeightChangedListener;
    }

    public void notifyHeightChanged(boolean z) {
        OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onHeightChanged(this, z);
        }
    }

    public int getPinnedHeadsUpHeight() {
        return getIntrinsicHeight();
    }

    public void setTranslation(float f) {
        setTranslationX(f);
    }

    public float getTranslation() {
        return getTranslationX();
    }

    public void onHeightReset() {
        OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onReset(this);
        }
    }

    @Override // android.view.View
    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        rect.left = (int) (rect.left + getTranslationX());
        rect.right = (int) (rect.right + getTranslationX());
        rect.bottom = (int) (rect.top + getTranslationY() + getActualHeight());
        rect.top = (int) (rect.top + getTranslationY() + getClipTopAmount());
    }

    public void getBoundsOnScreen(Rect rect, boolean z) {
        super.getBoundsOnScreen(rect, z);
        if (getTop() + getTranslationY() < 0.0f) {
            rect.top = (int) (rect.top + getTop() + getTranslationY());
        }
        rect.bottom = rect.top + getActualHeight();
        rect.top += getClipTopAmount();
    }

    protected void updateClipping() {
        if (this.mClipToActualHeight && shouldClipToActualHeight()) {
            int clipTopAmount = getClipTopAmount();
            int iMax = Math.max(Math.max((getActualHeight() + getExtraBottomPadding()) - this.mClipBottomAmount, clipTopAmount), this.mMinimumHeightForClipping);
            int i = (int) (this.mExtraWidthForClipping / 2.0f);
            mClipRect.set(-i, clipTopAmount, getWidth() + i, iMax);
            setClipBounds(mClipRect);
            return;
        }
        setClipBounds(null);
    }

    public void setMinimumHeightForClipping(int i) {
        this.mMinimumHeightForClipping = i;
        updateClipping();
    }

    public void setExtraWidthForClipping(float f) {
        this.mExtraWidthForClipping = f;
        updateClipping();
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        updateClipping();
    }

    public boolean willBeGone() {
        return this.mWillBeGone;
    }

    public void setWillBeGone(boolean z) {
        this.mWillBeGone = z;
    }

    public void setMinClipTopAmount(int i) {
        this.mMinClipTopAmount = i;
    }

    @Override // android.view.View
    public void setLayerType(int i, Paint paint) {
        if (hasOverlappingRendering()) {
            super.setLayerType(i, paint);
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return super.hasOverlappingRendering() && getActualHeight() <= getHeight();
    }

    public void setChangingPosition(boolean z) {
        this.mChangingPosition = z;
    }

    public boolean isChangingPosition() {
        return this.mChangingPosition;
    }

    public void setTransientContainer(ViewGroup viewGroup) {
        this.mTransientContainer = viewGroup;
    }

    public ViewGroup getTransientContainer() {
        return this.mTransientContainer;
    }

    protected ExpandableViewState createExpandableViewState() {
        return new ExpandableViewState();
    }

    public ExpandableViewState resetViewState() {
        this.mViewState.height = getIntrinsicHeight();
        this.mViewState.gone = getVisibility() == 8;
        ExpandableViewState expandableViewState = this.mViewState;
        expandableViewState.alpha = 1.0f;
        expandableViewState.notGoneIndex = -1;
        expandableViewState.xTranslation = getTranslationX();
        ExpandableViewState expandableViewState2 = this.mViewState;
        expandableViewState2.hidden = false;
        expandableViewState2.scaleX = getScaleX();
        this.mViewState.scaleY = getScaleY();
        ExpandableViewState expandableViewState3 = this.mViewState;
        expandableViewState3.inShelf = false;
        expandableViewState3.headsUpIsVisible = false;
        if (this instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) this;
            List<ExpandableNotificationRow> attachedChildren = expandableNotificationRow.getAttachedChildren();
            if (expandableNotificationRow.isSummaryWithChildren() && attachedChildren != null) {
                Iterator<ExpandableNotificationRow> it = attachedChildren.iterator();
                while (it.hasNext()) {
                    it.next().resetViewState();
                }
            }
        }
        return this.mViewState;
    }

    public ExpandableViewState getViewState() {
        return this.mViewState;
    }

    public void applyViewState() {
        ExpandableViewState expandableViewState = this.mViewState;
        if (expandableViewState.gone) {
            return;
        }
        expandableViewState.applyToView(this);
    }

    public void setInShelf(boolean z) {
        this.mInShelf = z;
    }

    public boolean isInShelf() {
        return this.mInShelf;
    }

    public int getRelativeTopPadding(View view) {
        int top = 0;
        while (view.getParent() instanceof ViewGroup) {
            top += view.getTop();
            view = (View) view.getParent();
            if (view == this) {
                break;
            }
        }
        return top;
    }

    public int getRelativeStartPadding(View view) {
        boolean zIsLayoutRtl = isLayoutRtl();
        int width = 0;
        while (view.getParent() instanceof ViewGroup) {
            View view2 = (View) view.getParent();
            width += zIsLayoutRtl ? view2.getWidth() - view.getRight() : view.getLeft();
            if (view2 == this) {
                return width;
            }
            view = view2;
        }
        return width;
    }

    public void setContentTransformationAmount(float f, boolean z) {
        boolean z2 = (z != this.mIsLastChild) | (this.mContentTransformationAmount != f);
        this.mIsLastChild = z;
        this.mContentTransformationAmount = f;
        if (z2) {
            updateContentTransformation();
        }
    }

    protected void updateContentTransformation() {
        float contentTransformationShift = (-this.mContentTransformationAmount) * getContentTransformationShift();
        float interpolation = Interpolators.ALPHA_OUT.getInterpolation(Math.min((1.0f - this.mContentTransformationAmount) / 0.5f, 1.0f));
        if (this.mIsLastChild) {
            contentTransformationShift *= 0.4f;
        }
        this.mContentTranslation = contentTransformationShift;
        applyContentTransformation(interpolation, contentTransformationShift);
    }

    protected float getContentTransformationShift() {
        return this.mContentShift;
    }

    public void setTransformingInShelf(boolean z) {
        this.mTransformingInShelf = z;
    }

    public boolean isTransformingIntoShelf() {
        return this.mTransformingInShelf;
    }

    public float getContentTranslation() {
        return this.mContentTranslation;
    }

    public void setFirstInSection(boolean z) {
        this.mFirstInSection = z;
    }

    public void setLastInSection(boolean z) {
        this.mLastInSection = z;
    }

    public boolean isLastInSection() {
        return this.mLastInSection;
    }

    public boolean isFirstInSection() {
        return this.mFirstInSection;
    }

    public int getHeadsUpHeightWithoutHeader() {
        return getHeight();
    }
}
