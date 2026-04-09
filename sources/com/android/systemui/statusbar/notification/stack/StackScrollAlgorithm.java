package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.FooterView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* loaded from: classes.dex */
public class StackScrollAlgorithm {
    private boolean mClipNotificationScrollToTop;
    private int mCollapsedSize;
    private int mGapHeight;
    private float mHeadsUpInset;
    private final ViewGroup mHostView;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsExpanded;
    private int mPaddingBetweenElements;
    private int mPinnedZTranslationExtra;
    private int mStatusBarHeight;
    private StackScrollAlgorithmState mTempAlgorithmState = new StackScrollAlgorithmState();

    public interface SectionProvider {
        boolean beginsSection(View view, View view2);
    }

    public StackScrollAlgorithm(Context context, ViewGroup viewGroup) {
        this.mHostView = viewGroup;
        initView(context);
    }

    public void initView(Context context) {
        initConstants(context);
    }

    private void initConstants(Context context) {
        Resources resources = context.getResources();
        this.mPaddingBetweenElements = resources.getDimensionPixelSize(R.dimen.notification_divider_height);
        this.mIncreasedPaddingBetweenElements = resources.getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mCollapsedSize = resources.getDimensionPixelSize(R.dimen.notification_min_height);
        this.mStatusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
        this.mClipNotificationScrollToTop = resources.getBoolean(R.bool.config_clipNotificationScrollToTop);
        this.mHeadsUpInset = this.mStatusBarHeight + resources.getDimensionPixelSize(R.dimen.heads_up_status_bar_padding);
        this.mPinnedZTranslationExtra = resources.getDimensionPixelSize(R.dimen.heads_up_pinned_elevation);
        this.mGapHeight = resources.getDimensionPixelSize(R.dimen.notification_section_divider_height);
    }

    public void resetViewStates(AmbientState ambientState) {
        StackScrollAlgorithmState stackScrollAlgorithmState = this.mTempAlgorithmState;
        resetChildViewStates();
        initAlgorithmState(this.mHostView, stackScrollAlgorithmState, ambientState);
        updatePositionsForState(stackScrollAlgorithmState, ambientState);
        updateZValuesForState(stackScrollAlgorithmState, ambientState);
        updateHeadsUpStates(stackScrollAlgorithmState, ambientState);
        updatePulsingStates(stackScrollAlgorithmState, ambientState);
        updateDimmedActivatedHideSensitive(ambientState, stackScrollAlgorithmState);
        updateClipping(stackScrollAlgorithmState, ambientState);
        updateSpeedBumpState(stackScrollAlgorithmState, ambientState);
        updateShelfState(ambientState);
        getNotificationChildrenStates(stackScrollAlgorithmState, ambientState);
    }

    private void resetChildViewStates() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((ExpandableView) this.mHostView.getChildAt(i)).resetViewState();
        }
    }

    private void getNotificationChildrenStates(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).updateChildrenStates(ambientState);
            }
        }
    }

    private void updateSpeedBumpState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int speedBumpIndex = ambientState.getSpeedBumpIndex();
        int i = 0;
        while (i < size) {
            stackScrollAlgorithmState.visibleChildren.get(i).getViewState().belowSpeedBump = i >= speedBumpIndex;
            i++;
        }
    }

    private void updateShelfState(AmbientState ambientState) {
        NotificationShelf shelf = ambientState.getShelf();
        if (shelf != null) {
            shelf.updateState(ambientState);
        }
    }

    private void updateClipping(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float fMax = 0.0f;
        float topPadding = !ambientState.isOnKeyguard() ? ambientState.getTopPadding() + ambientState.getStackTranslation() + ambientState.getExpandAnimationTopChange() : 0.0f;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        boolean z = true;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewState = expandableView.getViewState();
            if (!expandableView.mustStayOnScreen() || viewState.headsUpIsVisible) {
                fMax = Math.max(topPadding, fMax);
            }
            float f = viewState.yTranslation;
            float f2 = viewState.height + f;
            boolean z2 = (expandableView instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) expandableView).isPinned();
            if (this.mClipNotificationScrollToTop && ((!viewState.inShelf || (z2 && !z)) && f < fMax)) {
                viewState.clipTopAmount = (int) (fMax - f);
            } else {
                viewState.clipTopAmount = 0;
            }
            if (z2) {
                z = false;
            }
            if (!expandableView.isTransparent()) {
                if (!z2) {
                    f = f2;
                }
                fMax = Math.max(fMax, f);
            }
        }
    }

    private void updateDimmedActivatedHideSensitive(AmbientState ambientState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        boolean zIsDimmed = ambientState.isDimmed();
        boolean zIsHideSensitive = ambientState.isHideSensitive();
        ActivatableNotificationView activatedChild = ambientState.getActivatedChild();
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewState = expandableView.getViewState();
            viewState.dimmed = zIsDimmed;
            viewState.hideSensitive = zIsHideSensitive;
            boolean z = activatedChild == expandableView;
            if (zIsDimmed && z) {
                viewState.zTranslation += ambientState.getZDistanceBetweenElements() * 2.0f;
            }
        }
    }

    private void initAlgorithmState(ViewGroup viewGroup, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int i;
        int iIndexOf;
        stackScrollAlgorithmState.scrollY = (int) (Math.max(0, ambientState.getScrollY()) + ambientState.getOverScrollAmount(false));
        int childCount = viewGroup.getChildCount();
        stackScrollAlgorithmState.visibleChildren.clear();
        stackScrollAlgorithmState.visibleChildren.ensureCapacity(childCount);
        stackScrollAlgorithmState.paddingMap.clear();
        if (ambientState.isDozing()) {
            i = ambientState.hasPulsingNotifications() ? 1 : 0;
        } else {
            i = childCount;
        }
        int iUpdateNotGoneIndex = 0;
        ExpandableView expandableView = null;
        for (int i2 = 0; i2 < childCount; i2++) {
            ExpandableView expandableView2 = (ExpandableView) viewGroup.getChildAt(i2);
            if (expandableView2.getVisibility() != 8 && expandableView2 != ambientState.getShelf()) {
                if (i2 >= i) {
                    expandableView = null;
                }
                iUpdateNotGoneIndex = updateNotGoneIndex(stackScrollAlgorithmState, iUpdateNotGoneIndex, expandableView2);
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                if (increasedPaddingAmount != 0.0f) {
                    stackScrollAlgorithmState.paddingMap.put(expandableView2, Float.valueOf(increasedPaddingAmount));
                    if (expandableView != null) {
                        Float f = stackScrollAlgorithmState.paddingMap.get(expandableView);
                        float paddingForValue = getPaddingForValue(Float.valueOf(increasedPaddingAmount));
                        if (f != null) {
                            float paddingForValue2 = getPaddingForValue(f);
                            if (increasedPaddingAmount > 0.0f) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue2, paddingForValue, increasedPaddingAmount);
                            } else if (f.floatValue() > 0.0f) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue, paddingForValue2, f.floatValue());
                            }
                        }
                        stackScrollAlgorithmState.paddingMap.put(expandableView, Float.valueOf(paddingForValue));
                    }
                } else if (expandableView != null) {
                    stackScrollAlgorithmState.paddingMap.put(expandableView, Float.valueOf(getPaddingForValue(stackScrollAlgorithmState.paddingMap.get(expandableView))));
                }
                if (expandableView2 instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
                    List<ExpandableNotificationRow> attachedChildren = expandableNotificationRow.getAttachedChildren();
                    if (expandableNotificationRow.isSummaryWithChildren() && attachedChildren != null) {
                        for (ExpandableNotificationRow expandableNotificationRow2 : attachedChildren) {
                            if (expandableNotificationRow2.getVisibility() != 8) {
                                expandableNotificationRow2.getViewState().notGoneIndex = iUpdateNotGoneIndex;
                                iUpdateNotGoneIndex++;
                            }
                        }
                    }
                }
                expandableView = expandableView2;
            }
        }
        ExpandableNotificationRow expandingNotification = ambientState.getExpandingNotification();
        if (expandingNotification == null) {
            iIndexOf = -1;
        } else if (expandingNotification.isChildInGroup()) {
            iIndexOf = stackScrollAlgorithmState.visibleChildren.indexOf(expandingNotification.getNotificationParent());
        } else {
            iIndexOf = stackScrollAlgorithmState.visibleChildren.indexOf(expandingNotification);
        }
        stackScrollAlgorithmState.indexOfExpandingNotification = iIndexOf;
    }

    private float getPaddingForValue(Float f) {
        if (f == null) {
            return this.mPaddingBetweenElements;
        }
        if (f.floatValue() >= 0.0f) {
            return NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, f.floatValue());
        }
        return NotificationUtils.interpolate(0.0f, this.mPaddingBetweenElements, f.floatValue() + 1.0f);
    }

    private int updateNotGoneIndex(StackScrollAlgorithmState stackScrollAlgorithmState, int i, ExpandableView expandableView) {
        expandableView.getViewState().notGoneIndex = i;
        stackScrollAlgorithmState.visibleChildren.add(expandableView);
        return i + 1;
    }

    private void updatePositionsForState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float f = -stackScrollAlgorithmState.scrollY;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        float fUpdateChild = f;
        for (int i = 0; i < size; i++) {
            fUpdateChild = updateChild(i, stackScrollAlgorithmState, ambientState, fUpdateChild, false);
        }
    }

    protected float updateChild(int i, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState, float f, boolean z) {
        float f2;
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        boolean zChildNeedsGapHeight = childNeedsGapHeight(ambientState.getSectionProvider(), stackScrollAlgorithmState.anchorViewIndex, i, expandableView, i > 0 ? stackScrollAlgorithmState.visibleChildren.get(i - 1) : null);
        ExpandableViewState viewState = expandableView.getViewState();
        viewState.location = 0;
        float f3 = (!zChildNeedsGapHeight || z) ? f : f + this.mGapHeight;
        int paddingAfterChild = getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
        int maxAllowedChildHeight = getMaxAllowedChildHeight(expandableView);
        if (z) {
            viewState.yTranslation = f3 - (maxAllowedChildHeight + paddingAfterChild);
            if (f3 <= 0.0f) {
                viewState.location = 2;
            }
        } else {
            viewState.yTranslation = f3;
        }
        boolean z2 = expandableView instanceof FooterView;
        boolean z3 = expandableView instanceof EmptyShadeView;
        viewState.location = 4;
        float topPadding = ambientState.getTopPadding() + ambientState.getStackTranslation();
        if (i <= stackScrollAlgorithmState.getIndexOfExpandingNotification()) {
            topPadding += ambientState.getExpandAnimationTopChange();
        }
        if (expandableView.mustStayOnScreen()) {
            float f4 = viewState.yTranslation;
            if (f4 >= 0.0f) {
                viewState.headsUpIsVisible = (f4 + ((float) viewState.height)) + topPadding < ambientState.getMaxHeadsUpTranslation();
            }
        }
        if (z2) {
            viewState.yTranslation = Math.min(viewState.yTranslation, ambientState.getInnerHeight() - maxAllowedChildHeight);
        } else if (z3) {
            viewState.yTranslation = (ambientState.getInnerHeight() - maxAllowedChildHeight) + (ambientState.getStackTranslation() * 0.25f);
        } else if (expandableView != ambientState.getTrackedHeadsUpRow()) {
            clampPositionToShelf(expandableView, viewState, ambientState);
        }
        if (z) {
            f2 = viewState.yTranslation;
            if (zChildNeedsGapHeight) {
                f2 -= this.mGapHeight;
            }
        } else {
            f2 = paddingAfterChild + viewState.yTranslation + maxAllowedChildHeight;
            if (f2 <= 0.0f) {
                viewState.location = 2;
            }
        }
        if (viewState.location == 0) {
            Log.wtf("StackScrollAlgorithm", "Failed to assign location for child " + i);
        }
        viewState.yTranslation += topPadding;
        return f2;
    }

    public float getGapHeightForChild(SectionProvider sectionProvider, int i, int i2, View view, View view2) {
        if (childNeedsGapHeight(sectionProvider, i, i2, view, view2)) {
            return this.mGapHeight;
        }
        return 0.0f;
    }

    private boolean childNeedsGapHeight(SectionProvider sectionProvider, int i, int i2, View view, View view2) {
        return sectionProvider.beginsSection(view, view2) && i2 > 0;
    }

    protected int getPaddingAfterChild(StackScrollAlgorithmState stackScrollAlgorithmState, ExpandableView expandableView) {
        return stackScrollAlgorithmState.getPaddingAfterChild(expandableView);
    }

    private void updatePulsingStates(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                if (expandableNotificationRow.showingPulsing() && (i != 0 || !ambientState.isPulseExpanding())) {
                    expandableNotificationRow.getViewState().hidden = false;
                }
            }
        }
    }

    private void updateHeadsUpStates(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        ExpandableViewState viewState;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        ExpandableNotificationRow trackedHeadsUpRow = ambientState.getTrackedHeadsUpRow();
        if (trackedHeadsUpRow != null && (viewState = trackedHeadsUpRow.getViewState()) != null) {
            viewState.yTranslation = MathUtils.lerp(this.mHeadsUpInset, viewState.yTranslation - ambientState.getStackTranslation(), ambientState.getAppearFraction());
        }
        ExpandableNotificationRow expandableNotificationRow = null;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) expandableView;
                if (expandableNotificationRow2.isHeadsUp()) {
                    ExpandableViewState viewState2 = expandableNotificationRow2.getViewState();
                    if (expandableNotificationRow == null && expandableNotificationRow2.mustStayOnScreen() && !viewState2.headsUpIsVisible) {
                        viewState2.location = 1;
                        expandableNotificationRow = expandableNotificationRow2;
                    }
                    boolean z = expandableNotificationRow == expandableNotificationRow2;
                    float f = viewState2.yTranslation + viewState2.height;
                    if (this.mIsExpanded && expandableNotificationRow2.mustStayOnScreen() && !viewState2.headsUpIsVisible && !expandableNotificationRow2.showingPulsing()) {
                        clampHunToTop(ambientState, expandableNotificationRow2, viewState2);
                        if (z && expandableNotificationRow2.isAboveShelf()) {
                            clampHunToMaxTranslation(ambientState, expandableNotificationRow2, viewState2);
                            viewState2.hidden = false;
                        }
                    }
                    if (expandableNotificationRow2.isPinned()) {
                        viewState2.yTranslation = Math.max(viewState2.yTranslation, this.mHeadsUpInset);
                        viewState2.height = Math.max(expandableNotificationRow2.getIntrinsicHeight(), viewState2.height);
                        viewState2.hidden = false;
                        ExpandableViewState viewState3 = expandableNotificationRow == null ? null : expandableNotificationRow.getViewState();
                        if (viewState3 != null && !z && (!this.mIsExpanded || f > viewState3.yTranslation + viewState3.height)) {
                            int intrinsicHeight = expandableNotificationRow2.getIntrinsicHeight();
                            viewState2.height = intrinsicHeight;
                            viewState2.yTranslation = Math.min((viewState3.yTranslation + viewState3.height) - intrinsicHeight, viewState2.yTranslation);
                        }
                        if (!this.mIsExpanded && z && ambientState.getScrollY() > 0) {
                            viewState2.yTranslation -= ambientState.getScrollY();
                        }
                    }
                    if (expandableNotificationRow2.isHeadsUpAnimatingAway()) {
                        viewState2.hidden = false;
                    }
                }
            }
        }
    }

    private void clampHunToTop(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, ExpandableViewState expandableViewState) {
        float fMax = Math.max(ambientState.getTopPadding() + ambientState.getStackTranslation(), expandableViewState.yTranslation);
        expandableViewState.height = (int) Math.max(expandableViewState.height - (fMax - expandableViewState.yTranslation), expandableNotificationRow.getCollapsedHeight());
        expandableViewState.yTranslation = fMax;
    }

    private void clampHunToMaxTranslation(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, ExpandableViewState expandableViewState) {
        float fMin = Math.min(ambientState.getMaxHeadsUpTranslation(), ambientState.getInnerHeight() + ambientState.getTopPadding() + ambientState.getStackTranslation());
        float fMin2 = Math.min(expandableViewState.yTranslation, fMin - expandableNotificationRow.getCollapsedHeight());
        expandableViewState.height = (int) Math.min(expandableViewState.height, fMin - fMin2);
        expandableViewState.yTranslation = fMin2;
    }

    private void clampPositionToShelf(ExpandableView expandableView, ExpandableViewState expandableViewState, AmbientState ambientState) {
        if (ambientState.getShelf() == null) {
            return;
        }
        ExpandableNotificationRow trackedHeadsUpRow = ambientState.getTrackedHeadsUpRow();
        boolean z = trackedHeadsUpRow != null && this.mHostView.indexOfChild(expandableView) < this.mHostView.indexOfChild(trackedHeadsUpRow);
        int innerHeight = ambientState.getInnerHeight() - ambientState.getShelf().getIntrinsicHeight();
        if (ambientState.isAppearing() && !expandableView.isAboveShelf() && !z) {
            expandableViewState.yTranslation = Math.max(expandableViewState.yTranslation, innerHeight);
        }
        float f = innerHeight;
        float fMin = Math.min(expandableViewState.yTranslation, f);
        expandableViewState.yTranslation = fMin;
        if (fMin >= f) {
            expandableViewState.hidden = (expandableView.isExpandAnimationRunning() || expandableView.hasExpandingChild()) ? false : true;
            expandableViewState.inShelf = true;
            expandableViewState.headsUpIsVisible = false;
        }
    }

    protected int getMaxAllowedChildHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view == null ? this.mCollapsedSize : view.getHeight();
    }

    private void updateZValuesForState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int i = 0;
        while (true) {
            if (i >= size) {
                i = -1;
                break;
            }
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if ((expandableView instanceof ActivatableNotificationView) && (expandableView.isAboveShelf() || expandableView.showingPulsing())) {
                break;
            } else {
                i++;
            }
        }
        int i2 = size - 1;
        float fUpdateChildZValue = 0.0f;
        while (i2 >= 0) {
            fUpdateChildZValue = updateChildZValue(i2, fUpdateChildZValue, stackScrollAlgorithmState, ambientState, i2 == i);
            i2--;
        }
    }

    protected float updateChildZValue(int i, float f, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState, boolean z) {
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        ExpandableViewState viewState = expandableView.getViewState();
        int zDistanceBetweenElements = ambientState.getZDistanceBetweenElements();
        float baseZHeight = ambientState.getBaseZHeight();
        if (expandableView.mustStayOnScreen() && !viewState.headsUpIsVisible && !ambientState.isDozingAndNotPulsing(expandableView) && viewState.yTranslation < ambientState.getTopPadding() + ambientState.getStackTranslation()) {
            f = f != 0.0f ? f + 1.0f : f + Math.min(1.0f, ((ambientState.getTopPadding() + ambientState.getStackTranslation()) - viewState.yTranslation) / viewState.height);
            viewState.zTranslation = baseZHeight + (zDistanceBetweenElements * f);
        } else if (z) {
            int intrinsicHeight = ambientState.getShelf() == null ? 0 : ambientState.getShelf().getIntrinsicHeight();
            float innerHeight = (ambientState.getInnerHeight() - intrinsicHeight) + ambientState.getTopPadding() + ambientState.getStackTranslation();
            float intrinsicHeight2 = viewState.yTranslation + expandableView.getIntrinsicHeight() + this.mPaddingBetweenElements;
            if (innerHeight > intrinsicHeight2) {
                viewState.zTranslation = baseZHeight;
            } else {
                viewState.zTranslation = baseZHeight + (Math.min((intrinsicHeight2 - innerHeight) / intrinsicHeight, 1.0f) * zDistanceBetweenElements);
            }
        } else {
            viewState.zTranslation = baseZHeight;
        }
        viewState.zTranslation += (1.0f - expandableView.getHeaderVisibleAmount()) * this.mPinnedZTranslationExtra;
        return f;
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }

    public class StackScrollAlgorithmState {
        public int anchorViewIndex;
        private int indexOfExpandingNotification;
        public int scrollY;
        public final ArrayList<ExpandableView> visibleChildren = new ArrayList<>();
        public final HashMap<ExpandableView, Float> paddingMap = new HashMap<>();

        public StackScrollAlgorithmState() {
        }

        public int getPaddingAfterChild(ExpandableView expandableView) {
            Float f = this.paddingMap.get(expandableView);
            if (f == null) {
                return StackScrollAlgorithm.this.mPaddingBetweenElements;
            }
            return (int) f.floatValue();
        }

        public int getIndexOfExpandingNotification() {
            return this.indexOfExpandingNotification;
        }
    }
}
