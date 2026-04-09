package androidx.slice.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.lifecycle.Observer;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceMetadata;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R$attr;
import androidx.slice.view.R$dimen;
import androidx.slice.view.R$style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* loaded from: classes.dex */
public class SliceView extends ViewGroup implements Observer<Slice>, View.OnClickListener {
    public static final Comparator<SliceAction> SLICE_ACTION_PRIORITY_COMPARATOR = new Comparator<SliceAction>() { // from class: androidx.slice.widget.SliceView.3
        @Override // java.util.Comparator
        public int compare(SliceAction sliceAction, SliceAction sliceAction2) {
            int priority = sliceAction.getPriority();
            int priority2 = sliceAction2.getPriority();
            if (priority < 0 && priority2 < 0) {
                return 0;
            }
            if (priority < 0) {
                return 1;
            }
            if (priority2 < 0) {
                return -1;
            }
            if (priority2 < priority) {
                return 1;
            }
            return priority2 > priority ? -1 : 0;
        }
    };
    private ActionRow mActionRow;
    private int mActionRowHeight;
    private List<SliceAction> mActions;
    int[] mClickInfo;
    private Slice mCurrentSlice;
    private boolean mCurrentSliceLoggedVisible;
    private SliceMetrics mCurrentSliceMetrics;
    SliceChildView mCurrentView;
    private int mDownX;
    private int mDownY;
    Handler mHandler;
    boolean mInLongpress;
    private int mLargeHeight;
    ListContent mListContent;
    View.OnLongClickListener mLongClickListener;
    Runnable mLongpressCheck;
    private int mMinTemplateHeight;
    private View.OnClickListener mOnClickListener;
    boolean mPressing;
    Runnable mRefreshLastUpdated;
    private int mShortcutSize;
    private boolean mShowActionDividers;
    private boolean mShowActions;
    private boolean mShowHeaderDivider;
    private boolean mShowLastUpdated;
    private boolean mShowTitleItems;
    SliceMetadata mSliceMetadata;
    private OnSliceActionListener mSliceObserver;
    private SliceStyle mSliceStyle;
    private int mThemeTintColor;
    private int mTouchSlopSquared;
    private SliceViewPolicy mViewPolicy;

    public interface OnSliceActionListener {
        void onSliceAction(EventInfo eventInfo, SliceItem sliceItem);
    }

    public SliceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.sliceViewStyle);
    }

    public SliceView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShowActions = false;
        this.mShowLastUpdated = true;
        this.mCurrentSliceLoggedVisible = false;
        this.mShowTitleItems = false;
        this.mShowHeaderDivider = false;
        this.mShowActionDividers = false;
        this.mThemeTintColor = -1;
        this.mLongpressCheck = new Runnable() { // from class: androidx.slice.widget.SliceView.1
            @Override // java.lang.Runnable
            public void run() {
                View.OnLongClickListener onLongClickListener;
                SliceView sliceView = SliceView.this;
                if (!sliceView.mPressing || (onLongClickListener = sliceView.mLongClickListener) == null) {
                    return;
                }
                sliceView.mInLongpress = true;
                onLongClickListener.onLongClick(sliceView);
                SliceView.this.performHapticFeedback(0);
            }
        };
        this.mRefreshLastUpdated = new Runnable() { // from class: androidx.slice.widget.SliceView.2
            @Override // java.lang.Runnable
            public void run() {
                SliceMetadata sliceMetadata = SliceView.this.mSliceMetadata;
                if (sliceMetadata != null && sliceMetadata.isExpired()) {
                    SliceView.this.mCurrentView.setShowLastUpdated(true);
                    SliceView sliceView = SliceView.this;
                    sliceView.mCurrentView.setSliceContent(sliceView.mListContent);
                }
                SliceView.this.mHandler.postDelayed(this, 60000L);
            }
        };
        init(context, attributeSet, i, R$style.Widget_SliceView);
    }

    private void init(Context context, AttributeSet attributeSet, int i, int i2) {
        SliceStyle sliceStyle = new SliceStyle(context, attributeSet, i, i2);
        this.mSliceStyle = sliceStyle;
        this.mThemeTintColor = sliceStyle.getTintColor();
        this.mShortcutSize = getContext().getResources().getDimensionPixelSize(R$dimen.abc_slice_shortcut_size);
        this.mMinTemplateHeight = getContext().getResources().getDimensionPixelSize(R$dimen.abc_slice_row_min_height);
        this.mLargeHeight = getResources().getDimensionPixelSize(R$dimen.abc_slice_large_height);
        this.mActionRowHeight = getResources().getDimensionPixelSize(R$dimen.abc_slice_action_row_height);
        this.mViewPolicy = new SliceViewPolicy();
        TemplateView templateView = new TemplateView(getContext());
        this.mCurrentView = templateView;
        templateView.setPolicy(this.mViewPolicy);
        SliceChildView sliceChildView = this.mCurrentView;
        addView(sliceChildView, getChildLp(sliceChildView));
        applyConfigurations();
        ActionRow actionRow = new ActionRow(getContext(), true);
        this.mActionRow = actionRow;
        actionRow.setBackground(new ColorDrawable(-1118482));
        ActionRow actionRow2 = this.mActionRow;
        addView(actionRow2, getChildLp(actionRow2));
        updateActions();
        int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mTouchSlopSquared = scaledTouchSlop * scaledTouchSlop;
        this.mHandler = new Handler();
        setClipToPadding(false);
        super.setOnClickListener(this);
    }

    void setSliceViewPolicy(SliceViewPolicy sliceViewPolicy) {
        this.mViewPolicy = sliceViewPolicy;
    }

    public boolean isSliceViewClickable() {
        ListContent listContent;
        return (this.mOnClickListener == null && ((listContent = this.mListContent) == null || listContent.getShortcut(getContext()) == null)) ? false : true;
    }

    public void setClickInfo(int[] iArr) {
        this.mClickInfo = iArr;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int[] iArr;
        ListContent listContent = this.mListContent;
        if (listContent != null && listContent.getShortcut(getContext()) != null) {
            try {
                SliceActionImpl sliceActionImpl = (SliceActionImpl) this.mListContent.getShortcut(getContext());
                SliceItem actionItem = sliceActionImpl.getActionItem();
                if (actionItem != null && actionItem.fireActionInternal(getContext(), null)) {
                    this.mCurrentView.setActionLoading(sliceActionImpl.getSliceItem());
                }
                if (actionItem == null || this.mSliceObserver == null || (iArr = this.mClickInfo) == null || iArr.length <= 1) {
                    return;
                }
                int mode = getMode();
                int[] iArr2 = this.mClickInfo;
                EventInfo eventInfo = new EventInfo(mode, 3, iArr2[0], iArr2[1]);
                this.mSliceObserver.onSliceAction(eventInfo, sliceActionImpl.getSliceItem());
                logSliceMetricsOnTouch(sliceActionImpl.getSliceItem(), eventInfo);
                return;
            } catch (PendingIntent.CanceledException e) {
                Log.e("SliceView", "PendingIntent for slice cannot be sent", e);
                return;
            }
        }
        View.OnClickListener onClickListener = this.mOnClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @Override // android.view.View
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        super.setOnLongClickListener(onLongClickListener);
        this.mLongClickListener = onLongClickListener;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return (this.mLongClickListener != null && handleTouchForLongpress(motionEvent)) || super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return (this.mLongClickListener != null && handleTouchForLongpress(motionEvent)) || super.onTouchEvent(motionEvent);
    }

    private boolean handleTouchForLongpress(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mHandler.removeCallbacks(this.mLongpressCheck);
            this.mDownX = (int) motionEvent.getRawX();
            this.mDownY = (int) motionEvent.getRawY();
            this.mPressing = true;
            this.mInLongpress = false;
            this.mHandler.postDelayed(this.mLongpressCheck, ViewConfiguration.getLongPressTimeout());
            return false;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                int rawX = ((int) motionEvent.getRawX()) - this.mDownX;
                int rawY = ((int) motionEvent.getRawY()) - this.mDownY;
                if ((rawX * rawX) + (rawY * rawY) > this.mTouchSlopSquared) {
                    this.mPressing = false;
                    this.mHandler.removeCallbacks(this.mLongpressCheck);
                }
                return this.mInLongpress;
            }
            if (actionMasked != 3) {
                return false;
            }
        }
        boolean z = this.mInLongpress;
        this.mPressing = false;
        this.mInLongpress = false;
        this.mHandler.removeCallbacks(this.mLongpressCheck);
        return z;
    }

    private void configureViewPolicy(int i) {
        ListContent listContent = this.mListContent;
        if (listContent == null || !listContent.isValid() || getMode() == 3) {
            return;
        }
        if (i > 0 && i < this.mSliceStyle.getRowMaxHeight()) {
            int i2 = this.mMinTemplateHeight;
            if (i <= i2) {
                i = i2;
            }
            this.mViewPolicy.setMaxSmallHeight(i);
        } else {
            this.mViewPolicy.setMaxSmallHeight(0);
        }
        this.mViewPolicy.setMaxHeight(i);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int size = View.MeasureSpec.getSize(i);
        if (3 == getMode()) {
            size = this.mShortcutSize + getPaddingLeft() + getPaddingRight();
        }
        int i4 = this.mActionRow.getVisibility() != 8 ? this.mActionRowHeight : 0;
        int size2 = View.MeasureSpec.getSize(i2);
        int mode = View.MeasureSpec.getMode(i2);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        configureViewPolicy(((layoutParams == null || layoutParams.height != -2) && mode != 0) ? size2 : -1);
        int paddingTop = (size2 - getPaddingTop()) - getPaddingBottom();
        if (mode != 1073741824) {
            ListContent listContent = this.mListContent;
            if (listContent == null || !listContent.isValid()) {
                paddingTop = i4;
            } else {
                if (getMode() == 3) {
                    i3 = this.mShortcutSize;
                } else {
                    int height = this.mListContent.getHeight(this.mSliceStyle, this.mViewPolicy) + i4;
                    if (paddingTop > height || mode == 0) {
                        paddingTop = height;
                    } else {
                        if (getMode() == 2) {
                            i3 = this.mLargeHeight;
                            if (paddingTop >= i3 + i4) {
                            }
                        }
                        int i5 = this.mMinTemplateHeight;
                        if (paddingTop <= i5) {
                            paddingTop = i5;
                        }
                    }
                }
                paddingTop = i3 + i4;
            }
        }
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, 1073741824);
        this.mActionRow.measure(iMakeMeasureSpec, View.MeasureSpec.makeMeasureSpec(i4 > 0 ? getPaddingBottom() + i4 : 0, 1073741824));
        this.mCurrentView.measure(iMakeMeasureSpec, View.MeasureSpec.makeMeasureSpec(paddingTop + getPaddingTop() + (i4 <= 0 ? getPaddingBottom() : 0), 1073741824));
        setMeasuredDimension(size, this.mCurrentView.getMeasuredHeight() + this.mActionRow.getMeasuredHeight());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        SliceChildView sliceChildView = this.mCurrentView;
        sliceChildView.layout(0, 0, sliceChildView.getMeasuredWidth(), sliceChildView.getMeasuredHeight());
        if (this.mActionRow.getVisibility() != 8) {
            int measuredHeight = sliceChildView.getMeasuredHeight();
            ActionRow actionRow = this.mActionRow;
            actionRow.layout(0, measuredHeight, actionRow.getMeasuredWidth(), this.mActionRow.getMeasuredHeight() + measuredHeight);
        }
    }

    @Override // androidx.lifecycle.Observer
    public void onChanged(Slice slice) {
        setSlice(slice);
    }

    public void setSlice(Slice slice) {
        LocationBasedViewTracker.trackInputFocused(this);
        LocationBasedViewTracker.trackA11yFocus(this);
        initSliceMetrics(slice);
        boolean z = false;
        boolean z2 = (slice == null || this.mCurrentSlice == null || !slice.getUri().equals(this.mCurrentSlice.getUri())) ? false : true;
        SliceMetadata sliceMetadata = this.mSliceMetadata;
        this.mCurrentSlice = slice;
        SliceMetadata sliceMetadataFrom = slice != null ? SliceMetadata.from(getContext(), this.mCurrentSlice) : null;
        this.mSliceMetadata = sliceMetadataFrom;
        if (z2) {
            if (sliceMetadata.getLoadingState() == 2 && sliceMetadataFrom.getLoadingState() == 0) {
                return;
            }
        } else {
            this.mCurrentView.resetView();
        }
        SliceMetadata sliceMetadata2 = this.mSliceMetadata;
        this.mListContent = sliceMetadata2 != null ? sliceMetadata2.getListContent() : null;
        if (this.mShowTitleItems) {
            showTitleItems(true);
        }
        if (this.mShowHeaderDivider) {
            showHeaderDivider(true);
        }
        if (this.mShowActionDividers) {
            showActionDividers(true);
        }
        ListContent listContent = this.mListContent;
        if (listContent == null || !listContent.isValid()) {
            this.mActions = null;
            this.mCurrentView.resetView();
            updateActions();
            return;
        }
        this.mCurrentView.setLoadingActions(null);
        this.mActions = this.mSliceMetadata.getSliceActions();
        this.mCurrentView.setLastUpdated(this.mSliceMetadata.getLastUpdatedTime());
        SliceChildView sliceChildView = this.mCurrentView;
        if (this.mShowLastUpdated && this.mSliceMetadata.isExpired()) {
            z = true;
        }
        sliceChildView.setShowLastUpdated(z);
        this.mCurrentView.setAllowTwoLines(this.mSliceMetadata.isPermissionSlice());
        this.mCurrentView.setTint(getTintColor());
        if (this.mListContent.getLayoutDir() != -1) {
            this.mCurrentView.setLayoutDirection(this.mListContent.getLayoutDir());
        } else {
            this.mCurrentView.setLayoutDirection(2);
        }
        this.mCurrentView.setSliceContent(this.mListContent);
        updateActions();
        logSliceMetricsVisibilityChange(true);
        refreshLastUpdatedLabel(true);
    }

    public int getMode() {
        return this.mViewPolicy.getMode();
    }

    public void setShowTitleItems(boolean z) {
        this.mShowTitleItems = z;
        ListContent listContent = this.mListContent;
        if (listContent != null) {
            listContent.showTitleItems(z);
        }
    }

    @Deprecated
    public void showTitleItems(boolean z) {
        setShowTitleItems(z);
    }

    public void setShowHeaderDivider(boolean z) {
        this.mShowHeaderDivider = z;
        ListContent listContent = this.mListContent;
        if (listContent != null) {
            listContent.showHeaderDivider(z);
        }
    }

    @Deprecated
    public void showHeaderDivider(boolean z) {
        setShowHeaderDivider(z);
    }

    public void setShowActionDividers(boolean z) {
        this.mShowActionDividers = z;
        ListContent listContent = this.mListContent;
        if (listContent != null) {
            listContent.showActionDividers(z);
        }
    }

    @Deprecated
    public void showActionDividers(boolean z) {
        setShowActionDividers(z);
    }

    private void applyConfigurations() {
        this.mCurrentView.setSliceActionListener(this.mSliceObserver);
        this.mCurrentView.setStyle(this.mSliceStyle);
        this.mCurrentView.setTint(getTintColor());
        ListContent listContent = this.mListContent;
        if (listContent != null && listContent.getLayoutDir() != -1) {
            this.mCurrentView.setLayoutDirection(this.mListContent.getLayoutDir());
        } else {
            this.mCurrentView.setLayoutDirection(2);
        }
    }

    private void updateActions() {
        if (this.mActions == null) {
            this.mActionRow.setVisibility(8);
            this.mCurrentView.setSliceActions(null);
            this.mCurrentView.setInsets(getPaddingStart(), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
            return;
        }
        ArrayList arrayList = new ArrayList(this.mActions);
        Collections.sort(arrayList, SLICE_ACTION_PRIORITY_COMPARATOR);
        if (this.mShowActions && getMode() != 3 && this.mActions.size() >= 2) {
            this.mActionRow.setActions(arrayList, getTintColor());
            this.mActionRow.setVisibility(0);
            this.mCurrentView.setSliceActions(null);
            this.mCurrentView.setInsets(getPaddingStart(), getPaddingTop(), getPaddingEnd(), 0);
            this.mActionRow.setPaddingRelative(getPaddingStart(), 0, getPaddingEnd(), getPaddingBottom());
            return;
        }
        this.mCurrentView.setSliceActions(arrayList);
        this.mCurrentView.setInsets(getPaddingStart(), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        this.mActionRow.setVisibility(8);
    }

    private int getTintColor() {
        int i = this.mThemeTintColor;
        if (i != -1) {
            return i;
        }
        SliceItem sliceItemFindSubtype = SliceQuery.findSubtype(this.mCurrentSlice, "int", "color");
        if (sliceItemFindSubtype != null) {
            return sliceItemFindSubtype.getInt();
        }
        return SliceViewUtil.getColorAccent(getContext());
    }

    private ViewGroup.LayoutParams getChildLp(View view) {
        return new ViewGroup.LayoutParams(-1, -1);
    }

    public static String modeToString(int i) {
        if (i == 1) {
            return "MODE SMALL";
        }
        if (i == 2) {
            return "MODE LARGE";
        }
        if (i == 3) {
            return "MODE SHORTCUT";
        }
        return "unknown mode: " + i;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isShown()) {
            logSliceMetricsVisibilityChange(true);
            refreshLastUpdatedLabel(true);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        logSliceMetricsVisibilityChange(false);
        refreshLastUpdatedLabel(false);
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (isAttachedToWindow()) {
            logSliceMetricsVisibilityChange(i == 0);
            refreshLastUpdatedLabel(i == 0);
        }
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        logSliceMetricsVisibilityChange(i == 0);
        refreshLastUpdatedLabel(i == 0);
    }

    private void initSliceMetrics(Slice slice) {
        if (slice == null || slice.getUri() == null) {
            logSliceMetricsVisibilityChange(false);
            this.mCurrentSliceMetrics = null;
            return;
        }
        Slice slice2 = this.mCurrentSlice;
        if (slice2 == null || !slice2.getUri().equals(slice.getUri())) {
            logSliceMetricsVisibilityChange(false);
            this.mCurrentSliceMetrics = SliceMetrics.getInstance(getContext(), slice.getUri());
        }
    }

    private void logSliceMetricsVisibilityChange(boolean z) {
        SliceMetrics sliceMetrics = this.mCurrentSliceMetrics;
        if (sliceMetrics != null) {
            if (z && !this.mCurrentSliceLoggedVisible) {
                sliceMetrics.logVisible();
                this.mCurrentSliceLoggedVisible = true;
            }
            if (z || !this.mCurrentSliceLoggedVisible) {
                return;
            }
            this.mCurrentSliceMetrics.logHidden();
            this.mCurrentSliceLoggedVisible = false;
        }
    }

    private void logSliceMetricsOnTouch(SliceItem sliceItem, EventInfo eventInfo) {
        if (this.mCurrentSliceMetrics == null || sliceItem.getSlice() == null || sliceItem.getSlice().getUri() == null) {
            return;
        }
        this.mCurrentSliceMetrics.logTouch(eventInfo.actionType, sliceItem.getSlice().getUri());
    }

    private void refreshLastUpdatedLabel(boolean z) {
        SliceMetadata sliceMetadata;
        if (!this.mShowLastUpdated || (sliceMetadata = this.mSliceMetadata) == null || sliceMetadata.neverExpires()) {
            return;
        }
        if (z) {
            this.mHandler.postDelayed(this.mRefreshLastUpdated, this.mSliceMetadata.isExpired() ? 60000L : 60000 + this.mSliceMetadata.getTimeToExpiry());
        } else {
            this.mHandler.removeCallbacks(this.mRefreshLastUpdated);
        }
    }
}
