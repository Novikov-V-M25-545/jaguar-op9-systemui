package com.android.systemui.statusbar.notification.row;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.MediaTransferManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationCustomViewWrapper;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.InflatedSmartReplies;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.SmartReplyView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class NotificationContentView extends FrameLayout {
    private static final boolean DEBUG = Log.isLoggable("NotificationContentView", 3);
    private boolean mAnimate;
    private int mAnimationStartVisibleType;
    private boolean mBeforeN;
    private RemoteInputView mCachedExpandedRemoteInput;
    private RemoteInputView mCachedHeadsUpRemoteInput;
    private int mClipBottomAmount;
    private final Rect mClipBounds;
    private boolean mClipToActualHeight;
    private int mClipTopAmount;
    private ExpandableNotificationRow mContainingNotification;
    private int mContentHeight;
    private int mContentHeightAtAnimationStart;
    private View mContractedChild;
    private NotificationViewWrapper mContractedWrapper;
    private InflatedSmartReplies.SmartRepliesAndActions mCurrentSmartRepliesAndActions;
    private final ViewTreeObserver.OnPreDrawListener mEnableAnimationPredrawListener;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpandable;
    private View mExpandedChild;
    private InflatedSmartReplies mExpandedInflatedSmartReplies;
    private RemoteInputView mExpandedRemoteInput;
    private SmartReplyView mExpandedSmartReplyView;
    private Runnable mExpandedVisibleListener;
    private NotificationViewWrapper mExpandedWrapper;
    private boolean mFocusOnVisibilityChange;
    private boolean mForceSelectNextLayout;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private View mHeadsUpChild;
    private int mHeadsUpHeight;
    private InflatedSmartReplies mHeadsUpInflatedSmartReplies;
    private RemoteInputView mHeadsUpRemoteInput;
    private SmartReplyView mHeadsUpSmartReplyView;
    private NotificationViewWrapper mHeadsUpWrapper;
    private HybridGroupManager mHybridGroupManager;
    private boolean mIsChildInGroup;
    private boolean mIsContentExpandable;
    private boolean mIsHeadsUp;
    private boolean mIsLowPriority;
    private boolean mLegacy;
    private MediaTransferManager mMediaTransferManager;
    private int mMinContractedHeight;
    private int mNotificationContentMarginEnd;
    private int mNotificationMaxHeight;
    private final ArrayMap<View, Runnable> mOnContentViewInactiveListeners;
    private PeopleNotificationIdentifier mPeopleIdentifier;
    private PendingIntent mPreviousExpandedRemoteInputIntent;
    private PendingIntent mPreviousHeadsUpRemoteInputIntent;
    private RemoteInputController mRemoteInputController;
    private boolean mRemoteInputVisible;
    private boolean mShelfIconVisible;
    private HybridNotificationView mSingleLineView;
    private int mSingleLineWidthIndention;
    private int mSmallHeight;
    private SmartReplyConstants mSmartReplyConstants;
    private SmartReplyController mSmartReplyController;
    private StatusBarNotification mStatusBarNotification;
    private IStatusBarService mStatusBarService;
    private int mTransformationStartVisibleType;
    private int mUnrestrictedContentHeight;
    private boolean mUserExpanding;
    private int mVisibleType;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public NotificationContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClipBounds = new Rect();
        this.mVisibleType = -1;
        this.mOnContentViewInactiveListeners = new ArrayMap<>();
        this.mEnableAnimationPredrawListener = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentView.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                NotificationContentView.this.post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentView.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        NotificationContentView.this.mAnimate = true;
                    }
                });
                NotificationContentView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mClipToActualHeight = true;
        this.mAnimationStartVisibleType = -1;
        this.mForceSelectNextLayout = true;
        this.mContentHeightAtAnimationStart = -1;
        this.mHybridGroupManager = new HybridGroupManager(getContext());
        this.mMediaTransferManager = new MediaTransferManager(getContext());
        this.mSmartReplyConstants = (SmartReplyConstants) Dependency.get(SmartReplyConstants.class);
        this.mSmartReplyController = (SmartReplyController) Dependency.get(SmartReplyController.class);
        this.mStatusBarService = (IStatusBarService) Dependency.get(IStatusBarService.class);
        initView();
    }

    public void initView() {
        this.mMinContractedHeight = getResources().getDimensionPixelSize(R.dimen.min_notification_layout_height);
        this.mNotificationContentMarginEnd = getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_59);
    }

    public void setHeights(int i, int i2, int i3) {
        this.mSmallHeight = i;
        this.mHeadsUpHeight = i2;
        this.mNotificationMaxHeight = i3;
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        int iMax;
        boolean z;
        int iMakeMeasureSpec;
        int iMakeMeasureSpec2;
        boolean z2;
        int mode = View.MeasureSpec.getMode(i2);
        boolean z3 = true;
        boolean z4 = mode == 1073741824;
        boolean z5 = mode == Integer.MIN_VALUE;
        int size = View.MeasureSpec.getSize(i);
        int size2 = (z4 || z5) ? View.MeasureSpec.getSize(i2) : 1073741823;
        if (this.mExpandedChild != null) {
            int heightUpperLimit = this.mNotificationMaxHeight;
            SmartReplyView smartReplyView = this.mExpandedSmartReplyView;
            if (smartReplyView != null) {
                heightUpperLimit += smartReplyView.getHeightUpperLimit();
            }
            int extraMeasureHeight = heightUpperLimit + this.mExpandedWrapper.getExtraMeasureHeight();
            int i3 = this.mExpandedChild.getLayoutParams().height;
            if (i3 >= 0) {
                extraMeasureHeight = Math.min(extraMeasureHeight, i3);
                z2 = true;
            } else {
                z2 = false;
            }
            measureChildWithMargins(this.mExpandedChild, i, 0, View.MeasureSpec.makeMeasureSpec(extraMeasureHeight, z2 ? 1073741824 : Integer.MIN_VALUE), 0);
            iMax = Math.max(0, this.mExpandedChild.getMeasuredHeight());
        } else {
            iMax = 0;
        }
        View view = this.mContractedChild;
        if (view != null) {
            int iMin = this.mSmallHeight;
            int i4 = view.getLayoutParams().height;
            if (i4 >= 0) {
                iMin = Math.min(iMin, i4);
                z = true;
            } else {
                z = false;
            }
            if (shouldContractedBeFixedSize() || z) {
                iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(iMin, 1073741824);
            } else {
                iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(iMin, Integer.MIN_VALUE);
            }
            int i5 = iMakeMeasureSpec;
            measureChildWithMargins(this.mContractedChild, i, 0, i5, 0);
            int measuredHeight = this.mContractedChild.getMeasuredHeight();
            int i6 = this.mMinContractedHeight;
            if (measuredHeight < i6) {
                iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(i6, 1073741824);
                measureChildWithMargins(this.mContractedChild, i, 0, iMakeMeasureSpec2, 0);
            } else {
                iMakeMeasureSpec2 = i5;
            }
            iMax = Math.max(iMax, measuredHeight);
            if (updateContractedHeaderWidth()) {
                measureChildWithMargins(this.mContractedChild, i, 0, iMakeMeasureSpec2, 0);
            }
            if (this.mExpandedChild != null && this.mContractedChild.getMeasuredHeight() > this.mExpandedChild.getMeasuredHeight()) {
                measureChildWithMargins(this.mExpandedChild, i, 0, View.MeasureSpec.makeMeasureSpec(this.mContractedChild.getMeasuredHeight(), 1073741824), 0);
            }
        }
        if (this.mHeadsUpChild != null) {
            int heightUpperLimit2 = this.mHeadsUpHeight;
            SmartReplyView smartReplyView2 = this.mHeadsUpSmartReplyView;
            if (smartReplyView2 != null) {
                heightUpperLimit2 += smartReplyView2.getHeightUpperLimit();
            }
            int extraMeasureHeight2 = heightUpperLimit2 + this.mHeadsUpWrapper.getExtraMeasureHeight();
            int i7 = this.mHeadsUpChild.getLayoutParams().height;
            if (i7 >= 0) {
                extraMeasureHeight2 = Math.min(extraMeasureHeight2, i7);
            } else {
                z3 = false;
            }
            measureChildWithMargins(this.mHeadsUpChild, i, 0, View.MeasureSpec.makeMeasureSpec(extraMeasureHeight2, z3 ? 1073741824 : Integer.MIN_VALUE), 0);
            iMax = Math.max(iMax, this.mHeadsUpChild.getMeasuredHeight());
        }
        if (this.mSingleLineView != null) {
            this.mSingleLineView.measure((this.mSingleLineWidthIndention == 0 || View.MeasureSpec.getMode(i) == 0) ? i : View.MeasureSpec.makeMeasureSpec((size - this.mSingleLineWidthIndention) + this.mSingleLineView.getPaddingEnd(), 1073741824), View.MeasureSpec.makeMeasureSpec(this.mNotificationMaxHeight, Integer.MIN_VALUE));
            iMax = Math.max(iMax, this.mSingleLineView.getMeasuredHeight());
        }
        setMeasuredDimension(size, Math.min(iMax, size2));
    }

    private int getExtraRemoteInputHeight(RemoteInputView remoteInputView) {
        if (remoteInputView == null) {
            return 0;
        }
        if (remoteInputView.isActive() || remoteInputView.isSending()) {
            return getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_58);
        }
        return 0;
    }

    private boolean updateContractedHeaderWidth() {
        NotificationHeaderView notificationHeader = this.mContractedWrapper.getNotificationHeader();
        if (notificationHeader != null) {
            if (this.mExpandedChild != null && this.mExpandedWrapper.getNotificationHeader() != null) {
                int headerTextMarginEnd = this.mExpandedWrapper.getNotificationHeader().getHeaderTextMarginEnd();
                if (headerTextMarginEnd != notificationHeader.getHeaderTextMarginEnd()) {
                    notificationHeader.setHeaderTextMarginEnd(headerTextMarginEnd);
                    return true;
                }
            } else {
                int paddingLeft = this.mNotificationContentMarginEnd;
                if (notificationHeader.getPaddingEnd() != paddingLeft) {
                    int paddingLeft2 = notificationHeader.isLayoutRtl() ? paddingLeft : notificationHeader.getPaddingLeft();
                    int paddingTop = notificationHeader.getPaddingTop();
                    if (notificationHeader.isLayoutRtl()) {
                        paddingLeft = notificationHeader.getPaddingLeft();
                    }
                    notificationHeader.setPadding(paddingLeft2, paddingTop, paddingLeft, notificationHeader.getPaddingBottom());
                    notificationHeader.setShowWorkBadgeAtEnd(false);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldContractedBeFixedSize() {
        return this.mBeforeN && (this.mContractedWrapper instanceof NotificationCustomViewWrapper);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        View view = this.mExpandedChild;
        int height = view != null ? view.getHeight() : 0;
        super.onLayout(z, i, i2, i3, i4);
        if (height != 0 && this.mExpandedChild.getHeight() != height) {
            this.mContentHeightAtAnimationStart = height;
        }
        updateClipping();
        invalidateOutline();
        selectLayout(false, this.mForceSelectNextLayout);
        this.mForceSelectNextLayout = false;
        updateExpandButtons(this.mExpandable);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateVisibility();
    }

    public View getContractedChild() {
        return this.mContractedChild;
    }

    public View getExpandedChild() {
        return this.mExpandedChild;
    }

    public View getHeadsUpChild() {
        return this.mHeadsUpChild;
    }

    public void setContractedChild(View view) {
        View view2 = this.mContractedChild;
        if (view2 != null) {
            this.mOnContentViewInactiveListeners.remove(view2);
            this.mContractedChild.animate().cancel();
            removeView(this.mContractedChild);
        }
        if (view == null) {
            this.mContractedChild = null;
            this.mContractedWrapper = null;
            if (this.mTransformationStartVisibleType == 0) {
                this.mTransformationStartVisibleType = -1;
                return;
            }
            return;
        }
        addView(view);
        this.mContractedChild = view;
        this.mContractedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
    }

    private NotificationViewWrapper getWrapperForView(View view) {
        if (view == this.mContractedChild) {
            return this.mContractedWrapper;
        }
        if (view == this.mExpandedChild) {
            return this.mExpandedWrapper;
        }
        if (view == this.mHeadsUpChild) {
            return this.mHeadsUpWrapper;
        }
        return null;
    }

    public void setExpandedChild(View view) throws Resources.NotFoundException {
        if (this.mExpandedChild != null) {
            this.mPreviousExpandedRemoteInputIntent = null;
            RemoteInputView remoteInputView = this.mExpandedRemoteInput;
            if (remoteInputView != null) {
                remoteInputView.onNotificationUpdateOrReset();
                if (this.mExpandedRemoteInput.isActive()) {
                    this.mPreviousExpandedRemoteInputIntent = this.mExpandedRemoteInput.getPendingIntent();
                    RemoteInputView remoteInputView2 = this.mExpandedRemoteInput;
                    this.mCachedExpandedRemoteInput = remoteInputView2;
                    remoteInputView2.dispatchStartTemporaryDetach();
                    ((ViewGroup) this.mExpandedRemoteInput.getParent()).removeView(this.mExpandedRemoteInput);
                }
            }
            this.mOnContentViewInactiveListeners.remove(this.mExpandedChild);
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
            this.mExpandedRemoteInput = null;
        }
        if (view == null) {
            this.mExpandedChild = null;
            this.mExpandedWrapper = null;
            if (this.mTransformationStartVisibleType == 1) {
                this.mTransformationStartVisibleType = -1;
            }
            if (this.mVisibleType == 1) {
                selectLayout(false, true);
                return;
            }
            return;
        }
        addView(view);
        this.mExpandedChild = view;
        this.mExpandedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
        ExpandableNotificationRow expandableNotificationRow = this.mContainingNotification;
        if (expandableNotificationRow != null) {
            applyBubbleAction(this.mExpandedChild, expandableNotificationRow.getEntry());
        }
    }

    public void setHeadsUpChild(View view) throws Resources.NotFoundException {
        if (this.mHeadsUpChild != null) {
            this.mPreviousHeadsUpRemoteInputIntent = null;
            RemoteInputView remoteInputView = this.mHeadsUpRemoteInput;
            if (remoteInputView != null) {
                remoteInputView.onNotificationUpdateOrReset();
                if (this.mHeadsUpRemoteInput.isActive()) {
                    this.mPreviousHeadsUpRemoteInputIntent = this.mHeadsUpRemoteInput.getPendingIntent();
                    RemoteInputView remoteInputView2 = this.mHeadsUpRemoteInput;
                    this.mCachedHeadsUpRemoteInput = remoteInputView2;
                    remoteInputView2.dispatchStartTemporaryDetach();
                    ((ViewGroup) this.mHeadsUpRemoteInput.getParent()).removeView(this.mHeadsUpRemoteInput);
                }
            }
            this.mOnContentViewInactiveListeners.remove(this.mHeadsUpChild);
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
            this.mHeadsUpRemoteInput = null;
        }
        if (view == null) {
            this.mHeadsUpChild = null;
            this.mHeadsUpWrapper = null;
            if (this.mTransformationStartVisibleType == 2) {
                this.mTransformationStartVisibleType = -1;
            }
            if (this.mVisibleType == 2) {
                selectLayout(false, true);
                return;
            }
            return;
        }
        addView(view);
        this.mHeadsUpChild = view;
        this.mHeadsUpWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification);
        ExpandableNotificationRow expandableNotificationRow = this.mContainingNotification;
        if (expandableNotificationRow != null) {
            applyBubbleAction(this.mHeadsUpChild, expandableNotificationRow.getEntry());
        }
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        view.setTag(R.id.row_tag_for_content_view, this.mContainingNotification);
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        updateVisibility();
        if (i == 0 || this.mOnContentViewInactiveListeners.isEmpty()) {
            return;
        }
        Iterator it = new ArrayList(this.mOnContentViewInactiveListeners.values()).iterator();
        while (it.hasNext()) {
            ((Runnable) it.next()).run();
        }
        this.mOnContentViewInactiveListeners.clear();
    }

    private void updateVisibility() {
        setVisible(isShown());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
    }

    private void setVisible(boolean z) {
        if (z) {
            getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
            getViewTreeObserver().addOnPreDrawListener(this.mEnableAnimationPredrawListener);
        } else {
            getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
            this.mAnimate = false;
        }
    }

    private void focusExpandButtonIfNecessary() {
        View expandButton;
        if (this.mFocusOnVisibilityChange) {
            NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
            if (visibleWrapper != null && (expandButton = visibleWrapper.getExpandButton()) != null) {
                expandButton.requestAccessibilityFocus();
            }
            this.mFocusOnVisibilityChange = false;
        }
    }

    public void setContentHeight(int i) {
        this.mUnrestrictedContentHeight = Math.max(i, getMinHeight());
        this.mContentHeight = Math.min(this.mUnrestrictedContentHeight, (this.mContainingNotification.getIntrinsicHeight() - getExtraRemoteInputHeight(this.mExpandedRemoteInput)) - getExtraRemoteInputHeight(this.mHeadsUpRemoteInput));
        selectLayout(this.mAnimate, false);
        if (this.mContractedChild == null) {
            return;
        }
        int minContentHeightHint = getMinContentHeightHint();
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            visibleWrapper.setContentHeight(this.mUnrestrictedContentHeight, minContentHeightHint);
        }
        NotificationViewWrapper visibleWrapper2 = getVisibleWrapper(this.mTransformationStartVisibleType);
        if (visibleWrapper2 != null) {
            visibleWrapper2.setContentHeight(this.mUnrestrictedContentHeight, minContentHeightHint);
        }
        updateClipping();
        invalidateOutline();
    }

    private int getMinContentHeightHint() {
        int minHeight;
        int i;
        if (this.mIsChildInGroup && isVisibleOrTransitioning(3)) {
            return ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_49);
        }
        if (this.mHeadsUpChild != null && this.mExpandedChild != null) {
            boolean z = isTransitioningFromTo(2, 1) || isTransitioningFromTo(1, 2);
            boolean z2 = !isVisibleOrTransitioning(0) && (this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && this.mContainingNotification.canShowHeadsUp();
            if (z || z2) {
                return Math.min(getViewHeight(2), getViewHeight(1));
            }
        }
        if (this.mVisibleType == 1 && (i = this.mContentHeightAtAnimationStart) != -1 && this.mExpandedChild != null) {
            return Math.min(i, getViewHeight(1));
        }
        if (this.mHeadsUpChild != null && isVisibleOrTransitioning(2)) {
            minHeight = getViewHeight(2);
        } else if (this.mExpandedChild != null) {
            minHeight = getViewHeight(1);
        } else if (this.mContractedChild != null) {
            minHeight = getViewHeight(0) + ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_49);
        } else {
            minHeight = getMinHeight();
        }
        return (this.mExpandedChild == null || !isVisibleOrTransitioning(1)) ? minHeight : Math.min(minHeight, getViewHeight(1));
    }

    private boolean isTransitioningFromTo(int i, int i2) {
        return (this.mTransformationStartVisibleType == i || this.mAnimationStartVisibleType == i) && this.mVisibleType == i2;
    }

    private boolean isVisibleOrTransitioning(int i) {
        return this.mVisibleType == i || this.mTransformationStartVisibleType == i || this.mAnimationStartVisibleType == i;
    }

    private void updateContentTransformation() {
        int iCalculateVisibleType = calculateVisibleType();
        if (getTransformableViewForVisibleType(this.mVisibleType) == null) {
            this.mVisibleType = iCalculateVisibleType;
            updateViewVisibilities(iCalculateVisibleType);
            updateBackgroundColor(false);
            return;
        }
        int i = this.mVisibleType;
        if (iCalculateVisibleType != i) {
            this.mTransformationStartVisibleType = i;
            TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(iCalculateVisibleType);
            TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
            transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2, 0.0f);
            getViewForVisibleType(iCalculateVisibleType).setVisibility(0);
            transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, 0.0f);
            this.mVisibleType = iCalculateVisibleType;
            updateBackgroundColor(true);
        }
        if (this.mForceSelectNextLayout) {
            forceUpdateVisibilities();
        }
        int i2 = this.mTransformationStartVisibleType;
        if (i2 != -1 && this.mVisibleType != i2 && getViewForVisibleType(i2) != null) {
            TransformableView transformableViewForVisibleType3 = getTransformableViewForVisibleType(this.mVisibleType);
            TransformableView transformableViewForVisibleType4 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
            float fCalculateTransformationAmount = calculateTransformationAmount();
            transformableViewForVisibleType3.transformFrom(transformableViewForVisibleType4, fCalculateTransformationAmount);
            transformableViewForVisibleType4.transformTo(transformableViewForVisibleType3, fCalculateTransformationAmount);
            updateBackgroundTransformation(fCalculateTransformationAmount);
            return;
        }
        updateViewVisibilities(iCalculateVisibleType);
        updateBackgroundColor(false);
    }

    private void updateBackgroundTransformation(float f) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        int backgroundColor2 = getBackgroundColor(this.mTransformationStartVisibleType);
        if (backgroundColor != backgroundColor2) {
            if (backgroundColor2 == 0) {
                backgroundColor2 = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            if (backgroundColor == 0) {
                backgroundColor = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            backgroundColor = NotificationUtils.interpolateColors(backgroundColor2, backgroundColor, f);
        }
        this.mContainingNotification.updateBackgroundAlpha(f);
        this.mContainingNotification.setContentBackground(backgroundColor, false, this);
    }

    private float calculateTransformationAmount() {
        int viewHeight = getViewHeight(this.mTransformationStartVisibleType);
        int viewHeight2 = getViewHeight(this.mVisibleType);
        int iAbs = Math.abs(this.mContentHeight - viewHeight);
        int iAbs2 = Math.abs(viewHeight2 - viewHeight);
        if (iAbs2 == 0) {
            Log.wtf("NotificationContentView", "the total transformation distance is 0\n StartType: " + this.mTransformationStartVisibleType + " height: " + viewHeight + "\n VisibleType: " + this.mVisibleType + " height: " + viewHeight2 + "\n mContentHeight: " + this.mContentHeight);
            return 1.0f;
        }
        return Math.min(1.0f, iAbs / iAbs2);
    }

    public int getMaxHeight() {
        int viewHeight;
        int extraRemoteInputHeight;
        if (this.mExpandedChild != null) {
            viewHeight = getViewHeight(1);
            extraRemoteInputHeight = getExtraRemoteInputHeight(this.mExpandedRemoteInput);
        } else if (this.mIsHeadsUp && this.mHeadsUpChild != null && this.mContainingNotification.canShowHeadsUp()) {
            viewHeight = getViewHeight(2);
            extraRemoteInputHeight = getExtraRemoteInputHeight(this.mHeadsUpRemoteInput);
        } else {
            if (this.mContractedChild != null) {
                return getViewHeight(0);
            }
            return this.mNotificationMaxHeight;
        }
        return viewHeight + extraRemoteInputHeight;
    }

    private int getViewHeight(int i) {
        return getViewHeight(i, false);
    }

    private int getViewHeight(int i, boolean z) {
        View viewForVisibleType = getViewForVisibleType(i);
        int height = viewForVisibleType.getHeight();
        NotificationViewWrapper wrapperForView = getWrapperForView(viewForVisibleType);
        return wrapperForView != null ? height + wrapperForView.getHeaderTranslation(z) : height;
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean z) {
        if (z || !this.mIsChildInGroup || isGroupExpanded()) {
            return this.mContractedChild != null ? getViewHeight(0) : this.mMinContractedHeight;
        }
        return this.mSingleLineView.getHeight();
    }

    private boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateClipping();
    }

    @Override // android.view.View
    public void setTranslationY(float f) {
        super.setTranslationY(f);
        updateClipping();
    }

    private void updateClipping() {
        if (this.mClipToActualHeight) {
            int translationY = (int) (this.mClipTopAmount - getTranslationY());
            this.mClipBounds.set(0, translationY, getWidth(), Math.max(translationY, (int) ((this.mUnrestrictedContentHeight - this.mClipBottomAmount) - getTranslationY())));
            setClipBounds(this.mClipBounds);
            return;
        }
        setClipBounds(null);
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        updateClipping();
    }

    private void selectLayout(boolean z, boolean z2) {
        if (this.mContractedChild == null) {
            return;
        }
        if (this.mUserExpanding) {
            updateContentTransformation();
            return;
        }
        int iCalculateVisibleType = calculateVisibleType();
        boolean z3 = iCalculateVisibleType != this.mVisibleType;
        if (z3 || z2) {
            View viewForVisibleType = getViewForVisibleType(iCalculateVisibleType);
            if (viewForVisibleType != null) {
                viewForVisibleType.setVisibility(0);
                transferRemoteInputFocus(iCalculateVisibleType);
            }
            if (z && ((iCalculateVisibleType == 1 && this.mExpandedChild != null) || ((iCalculateVisibleType == 2 && this.mHeadsUpChild != null) || ((iCalculateVisibleType == 3 && this.mSingleLineView != null) || iCalculateVisibleType == 0)))) {
                animateToVisibleType(iCalculateVisibleType);
            } else {
                updateViewVisibilities(iCalculateVisibleType);
            }
            this.mVisibleType = iCalculateVisibleType;
            if (z3) {
                focusExpandButtonIfNecessary();
            }
            NotificationViewWrapper visibleWrapper = getVisibleWrapper(iCalculateVisibleType);
            if (visibleWrapper != null) {
                visibleWrapper.setContentHeight(this.mUnrestrictedContentHeight, getMinContentHeightHint());
            }
            updateBackgroundColor(z);
        }
    }

    private void forceUpdateVisibilities() {
        forceUpdateVisibility(0, this.mContractedChild, this.mContractedWrapper);
        forceUpdateVisibility(1, this.mExpandedChild, this.mExpandedWrapper);
        forceUpdateVisibility(2, this.mHeadsUpChild, this.mHeadsUpWrapper);
        HybridNotificationView hybridNotificationView = this.mSingleLineView;
        forceUpdateVisibility(3, hybridNotificationView, hybridNotificationView);
        fireExpandedVisibleListenerIfVisible();
        this.mAnimationStartVisibleType = -1;
    }

    private void fireExpandedVisibleListenerIfVisible() {
        if (this.mExpandedVisibleListener == null || this.mExpandedChild == null || !isShown() || this.mExpandedChild.getVisibility() != 0) {
            return;
        }
        Runnable runnable = this.mExpandedVisibleListener;
        this.mExpandedVisibleListener = null;
        runnable.run();
    }

    private void forceUpdateVisibility(int i, View view, TransformableView transformableView) {
        if (view == null) {
            return;
        }
        if (!(this.mVisibleType == i || this.mTransformationStartVisibleType == i)) {
            view.setVisibility(4);
        } else {
            transformableView.setVisible(true);
        }
    }

    public void updateBackgroundColor(boolean z) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        this.mContainingNotification.resetBackgroundAlpha();
        this.mContainingNotification.setContentBackground(backgroundColor, z, this);
    }

    public void setBackgroundTintColor(int i) {
        SmartReplyView smartReplyView = this.mExpandedSmartReplyView;
        if (smartReplyView != null) {
            smartReplyView.setBackgroundTintColor(i);
        }
        SmartReplyView smartReplyView2 = this.mHeadsUpSmartReplyView;
        if (smartReplyView2 != null) {
            smartReplyView2.setBackgroundTintColor(i);
        }
    }

    public int getVisibleType() {
        return this.mVisibleType;
    }

    public int getBackgroundColorForExpansionState() {
        int iCalculateVisibleType;
        if (this.mContainingNotification.isGroupExpanded() || this.mContainingNotification.isUserLocked()) {
            iCalculateVisibleType = calculateVisibleType();
        } else {
            iCalculateVisibleType = getVisibleType();
        }
        return getBackgroundColor(iCalculateVisibleType);
    }

    public int getBackgroundColor(int i) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(i);
        if (visibleWrapper != null) {
            return visibleWrapper.getCustomBackgroundColor();
        }
        return 0;
    }

    private void updateViewVisibilities(int i) {
        updateViewVisibility(i, 0, this.mContractedChild, this.mContractedWrapper);
        updateViewVisibility(i, 1, this.mExpandedChild, this.mExpandedWrapper);
        updateViewVisibility(i, 2, this.mHeadsUpChild, this.mHeadsUpWrapper);
        HybridNotificationView hybridNotificationView = this.mSingleLineView;
        updateViewVisibility(i, 3, hybridNotificationView, hybridNotificationView);
        fireExpandedVisibleListenerIfVisible();
        this.mAnimationStartVisibleType = -1;
    }

    private void updateViewVisibility(int i, int i2, View view, TransformableView transformableView) {
        if (view != null) {
            transformableView.setVisible(i == i2);
        }
    }

    private void animateToVisibleType(int i) {
        TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(i);
        final TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mVisibleType);
        if (transformableViewForVisibleType == transformableViewForVisibleType2 || transformableViewForVisibleType2 == null) {
            transformableViewForVisibleType.setVisible(true);
            return;
        }
        this.mAnimationStartVisibleType = this.mVisibleType;
        transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2);
        getViewForVisibleType(i).setVisibility(0);
        transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, new Runnable() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentView.2
            @Override // java.lang.Runnable
            public void run() {
                TransformableView transformableView = transformableViewForVisibleType2;
                NotificationContentView notificationContentView = NotificationContentView.this;
                if (transformableView != notificationContentView.getTransformableViewForVisibleType(notificationContentView.mVisibleType)) {
                    transformableViewForVisibleType2.setVisible(false);
                }
                NotificationContentView.this.mAnimationStartVisibleType = -1;
            }
        });
        fireExpandedVisibleListenerIfVisible();
    }

    private void transferRemoteInputFocus(int i) {
        RemoteInputView remoteInputView;
        RemoteInputView remoteInputView2;
        if (i == 2 && this.mHeadsUpRemoteInput != null && (remoteInputView2 = this.mExpandedRemoteInput) != null && remoteInputView2.isActive()) {
            this.mHeadsUpRemoteInput.stealFocusFrom(this.mExpandedRemoteInput);
        }
        if (i != 1 || this.mExpandedRemoteInput == null || (remoteInputView = this.mHeadsUpRemoteInput) == null || !remoteInputView.isActive()) {
            return;
        }
        this.mExpandedRemoteInput.stealFocusFrom(this.mHeadsUpRemoteInput);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public TransformableView getTransformableViewForVisibleType(int i) {
        if (i == 1) {
            return this.mExpandedWrapper;
        }
        if (i == 2) {
            return this.mHeadsUpWrapper;
        }
        if (i == 3) {
            return this.mSingleLineView;
        }
        return this.mContractedWrapper;
    }

    private View getViewForVisibleType(int i) {
        if (i == 1) {
            return this.mExpandedChild;
        }
        if (i == 2) {
            return this.mHeadsUpChild;
        }
        if (i == 3) {
            return this.mSingleLineView;
        }
        return this.mContractedChild;
    }

    public View[] getAllViews() {
        return new View[]{this.mContractedChild, this.mHeadsUpChild, this.mExpandedChild, this.mSingleLineView};
    }

    public NotificationViewWrapper getVisibleWrapper(int i) {
        if (i == 0) {
            return this.mContractedWrapper;
        }
        if (i == 1) {
            return this.mExpandedWrapper;
        }
        if (i != 2) {
            return null;
        }
        return this.mHeadsUpWrapper;
    }

    public int calculateVisibleType() {
        int maxContentHeight;
        if (this.mUserExpanding) {
            if (!this.mIsChildInGroup || isGroupExpanded() || this.mContainingNotification.isExpanded(true)) {
                maxContentHeight = this.mContainingNotification.getMaxContentHeight();
            } else {
                maxContentHeight = this.mContainingNotification.getShowingLayout().getMinHeight();
            }
            if (maxContentHeight == 0) {
                maxContentHeight = this.mContentHeight;
            }
            int visualTypeForHeight = getVisualTypeForHeight(maxContentHeight);
            int visualTypeForHeight2 = (!this.mIsChildInGroup || isGroupExpanded()) ? getVisualTypeForHeight(this.mContainingNotification.getCollapsedHeight()) : 3;
            return this.mTransformationStartVisibleType == visualTypeForHeight2 ? visualTypeForHeight : visualTypeForHeight2;
        }
        int intrinsicHeight = this.mContainingNotification.getIntrinsicHeight();
        int iMin = this.mContentHeight;
        if (intrinsicHeight != 0) {
            iMin = Math.min(iMin, intrinsicHeight);
        }
        return getVisualTypeForHeight(iMin);
    }

    private int getVisualTypeForHeight(float f) {
        boolean z = this.mExpandedChild == null;
        if (!z && f == getViewHeight(1)) {
            return 1;
        }
        if (!this.mUserExpanding && this.mIsChildInGroup && !isGroupExpanded()) {
            return 3;
        }
        if ((this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && this.mHeadsUpChild != null && this.mContainingNotification.canShowHeadsUp()) {
            return (f <= ((float) getViewHeight(2)) || z) ? 2 : 1;
        }
        if (z || !(this.mContractedChild == null || f > getViewHeight(0) || (this.mIsChildInGroup && !isGroupExpanded() && this.mContainingNotification.isExpanded(true)))) {
            return 0;
        }
        return !z ? 1 : -1;
    }

    public boolean isContentExpandable() {
        return this.mIsContentExpandable;
    }

    public void setHeadsUp(boolean z) {
        this.mIsHeadsUp = z;
        selectLayout(false, true);
        updateExpandButtons(this.mExpandable);
    }

    public void setLegacy(boolean z) {
        this.mLegacy = z;
        updateLegacy();
    }

    private void updateLegacy() {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setLegacy(this.mLegacy);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setLegacy(this.mLegacy);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setLegacy(this.mLegacy);
        }
    }

    public void setIsChildInGroup(boolean z) {
        this.mIsChildInGroup = z;
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setIsChildInGroup(z);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        updateAllSingleLineViews();
    }

    public void onNotificationUpdated(NotificationEntry notificationEntry) {
        this.mStatusBarNotification = notificationEntry.getSbn();
        this.mBeforeN = notificationEntry.targetSdk < 24;
        updateAllSingleLineViews();
        ExpandableNotificationRow row = notificationEntry.getRow();
        if (this.mContractedChild != null) {
            this.mContractedWrapper.onContentUpdated(row);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.onContentUpdated(row);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.onContentUpdated(row);
        }
        applyRemoteInputAndSmartReply(notificationEntry);
        applyMediaTransfer(notificationEntry);
        updateLegacy();
        this.mForceSelectNextLayout = true;
        this.mPreviousExpandedRemoteInputIntent = null;
        this.mPreviousHeadsUpRemoteInputIntent = null;
        applyBubbleAction(this.mExpandedChild, notificationEntry);
        applyBubbleAction(this.mHeadsUpChild, notificationEntry);
    }

    private void updateAllSingleLineViews() {
        updateSingleLineView();
    }

    private void updateSingleLineView() {
        if (this.mIsChildInGroup) {
            HybridNotificationView hybridNotificationView = this.mSingleLineView;
            boolean z = hybridNotificationView == null;
            HybridNotificationView hybridNotificationViewBindFromNotification = this.mHybridGroupManager.bindFromNotification(hybridNotificationView, this.mContractedChild, this.mStatusBarNotification, this);
            this.mSingleLineView = hybridNotificationViewBindFromNotification;
            if (z) {
                updateViewVisibility(this.mVisibleType, 3, hybridNotificationViewBindFromNotification, hybridNotificationViewBindFromNotification);
                return;
            }
            return;
        }
        View view = this.mSingleLineView;
        if (view != null) {
            removeView(view);
            this.mSingleLineView = null;
        }
    }

    private void applyMediaTransfer(NotificationEntry notificationEntry) {
        if (notificationEntry.isMediaNotification()) {
            View view = this.mExpandedChild;
            if (view != null && (view instanceof ViewGroup)) {
                this.mMediaTransferManager.applyMediaTransferView((ViewGroup) view, notificationEntry);
            }
            View view2 = this.mContractedChild;
            if (view2 == null || !(view2 instanceof ViewGroup)) {
                return;
            }
            this.mMediaTransferManager.applyMediaTransferView((ViewGroup) view2, notificationEntry);
        }
    }

    private void applyRemoteInputAndSmartReply(NotificationEntry notificationEntry) {
        InflatedSmartReplies.SmartRepliesAndActions smartRepliesAndActions;
        if (this.mRemoteInputController == null) {
            return;
        }
        applyRemoteInput(notificationEntry, InflatedSmartReplies.hasFreeformRemoteInput(notificationEntry));
        InflatedSmartReplies inflatedSmartReplies = this.mExpandedInflatedSmartReplies;
        if (inflatedSmartReplies == null && this.mHeadsUpInflatedSmartReplies == null) {
            if (DEBUG) {
                Log.d("NotificationContentView", "Both expanded, and heads-up InflatedSmartReplies are null, don't add smart replies.");
                return;
            }
            return;
        }
        if (inflatedSmartReplies != null) {
            smartRepliesAndActions = inflatedSmartReplies.getSmartRepliesAndActions();
        } else {
            smartRepliesAndActions = this.mHeadsUpInflatedSmartReplies.getSmartRepliesAndActions();
        }
        this.mCurrentSmartRepliesAndActions = smartRepliesAndActions;
        if (DEBUG) {
            Object[] objArr = new Object[3];
            objArr[0] = notificationEntry.getSbn().getKey();
            SmartReplyView.SmartActions smartActions = this.mCurrentSmartRepliesAndActions.smartActions;
            objArr[1] = Integer.valueOf(smartActions == null ? 0 : smartActions.actions.size());
            SmartReplyView.SmartReplies smartReplies = this.mCurrentSmartRepliesAndActions.smartReplies;
            objArr[2] = Integer.valueOf(smartReplies != null ? smartReplies.choices.size() : 0);
            Log.d("NotificationContentView", String.format("Adding suggestions for %s, %d actions, and %d replies.", objArr));
        }
        applySmartReplyView(this.mCurrentSmartRepliesAndActions, notificationEntry);
    }

    private void applyRemoteInput(NotificationEntry notificationEntry, boolean z) {
        View view = this.mExpandedChild;
        if (view != null) {
            this.mExpandedRemoteInput = applyRemoteInput(view, notificationEntry, z, this.mPreviousExpandedRemoteInputIntent, this.mCachedExpandedRemoteInput, this.mExpandedWrapper);
        } else {
            this.mExpandedRemoteInput = null;
        }
        RemoteInputView remoteInputView = this.mCachedExpandedRemoteInput;
        if (remoteInputView != null && remoteInputView != this.mExpandedRemoteInput) {
            remoteInputView.dispatchFinishTemporaryDetach();
        }
        this.mCachedExpandedRemoteInput = null;
        View view2 = this.mHeadsUpChild;
        if (view2 != null) {
            this.mHeadsUpRemoteInput = applyRemoteInput(view2, notificationEntry, z, this.mPreviousHeadsUpRemoteInputIntent, this.mCachedHeadsUpRemoteInput, this.mHeadsUpWrapper);
        } else {
            this.mHeadsUpRemoteInput = null;
        }
        RemoteInputView remoteInputView2 = this.mCachedHeadsUpRemoteInput;
        if (remoteInputView2 != null && remoteInputView2 != this.mHeadsUpRemoteInput) {
            remoteInputView2.dispatchFinishTemporaryDetach();
        }
        this.mCachedHeadsUpRemoteInput = null;
    }

    private RemoteInputView applyRemoteInput(View view, NotificationEntry notificationEntry, boolean z, PendingIntent pendingIntent, RemoteInputView remoteInputView, NotificationViewWrapper notificationViewWrapper) {
        View viewFindViewById = view.findViewById(android.R.id.account_row_text);
        if (!(viewFindViewById instanceof FrameLayout)) {
            return null;
        }
        RemoteInputView remoteInputView2 = (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
        if (remoteInputView2 != null) {
            remoteInputView2.onNotificationUpdateOrReset();
        }
        if (remoteInputView2 == null && z) {
            FrameLayout frameLayout = (FrameLayout) viewFindViewById;
            if (remoteInputView == null) {
                remoteInputView = RemoteInputView.inflate(((FrameLayout) this).mContext, frameLayout, notificationEntry, this.mRemoteInputController);
                remoteInputView.setVisibility(4);
                frameLayout.addView(remoteInputView, new FrameLayout.LayoutParams(-1, -1));
            } else {
                frameLayout.addView(remoteInputView);
                remoteInputView.dispatchFinishTemporaryDetach();
                remoteInputView.requestFocus();
            }
        } else {
            remoteInputView = remoteInputView2;
        }
        if (z) {
            int color = notificationEntry.getSbn().getNotification().color;
            if (color == 0) {
                color = ((FrameLayout) this).mContext.getColor(R.color.default_remote_input_background);
            }
            remoteInputView.setBackgroundColor(ContrastColorUtil.ensureTextBackgroundColor(color, ((FrameLayout) this).mContext.getColor(R.color.remote_input_text_enabled), ((FrameLayout) this).mContext.getColor(R.color.remote_input_hint)));
            remoteInputView.setWrapper(notificationViewWrapper);
            remoteInputView.setOnVisibilityChangedListener(new Consumer() { // from class: com.android.systemui.statusbar.notification.row.NotificationContentView$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.setRemoteInputVisible(((Boolean) obj).booleanValue());
                }
            });
            if (pendingIntent != null || remoteInputView.isActive()) {
                Notification.Action[] actionArr = notificationEntry.getSbn().getNotification().actions;
                if (pendingIntent != null) {
                    remoteInputView.setPendingIntent(pendingIntent);
                }
                if (remoteInputView.updatePendingIntentFromActions(actionArr)) {
                    if (!remoteInputView.isActive()) {
                        remoteInputView.focus();
                    }
                } else if (remoteInputView.isActive()) {
                    remoteInputView.close();
                }
            }
        }
        return remoteInputView;
    }

    public void updateBubbleButton(NotificationEntry notificationEntry) {
        applyBubbleAction(this.mExpandedChild, notificationEntry);
    }

    private boolean isBubblesEnabled() {
        return Settings.Global.getInt(((FrameLayout) this).mContext.getContentResolver(), "notification_bubbles", 0) == 1;
    }

    private void applyBubbleAction(View view, NotificationEntry notificationEntry) throws Resources.NotFoundException {
        int i;
        int i2;
        if (view == null || this.mContainingNotification == null || this.mPeopleIdentifier == null) {
            return;
        }
        ImageView imageView = (ImageView) view.findViewById(android.R.id.auto);
        View viewFindViewById = view.findViewById(android.R.id.account_row_text);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(android.R.id.account_type);
        if (imageView == null || viewFindViewById == null || linearLayout == null) {
            return;
        }
        if (isBubblesEnabled() && (this.mPeopleIdentifier.getPeopleNotificationType(notificationEntry.getSbn(), notificationEntry.getRanking()) >= 2) && notificationEntry.getBubbleMetadata() != null) {
            Resources resources = ((FrameLayout) this).mContext.getResources();
            if (notificationEntry.isBubble()) {
                i = R.drawable.ic_stop_bubble;
            } else {
                i = R.drawable.ic_create_bubble;
            }
            Drawable drawable = resources.getDrawable(i);
            this.mContainingNotification.updateNotificationColor();
            drawable.setTint(this.mContainingNotification.getNotificationColor());
            Resources resources2 = ((FrameLayout) this).mContext.getResources();
            if (notificationEntry.isBubble()) {
                i2 = R.string.notification_conversation_unbubble;
            } else {
                i2 = R.string.notification_conversation_bubble;
            }
            imageView.setContentDescription(resources2.getString(i2));
            imageView.setImageDrawable(drawable);
            imageView.setOnClickListener(this.mContainingNotification.getBubbleClickListener());
            imageView.setVisibility(0);
            viewFindViewById.setVisibility(0);
            linearLayout.setPaddingRelative(0, 0, getResources().getDimensionPixelSize(android.R.dimen.alert_dialog_button_bar_width), 0);
            return;
        }
        imageView.setVisibility(8);
        linearLayout.setPaddingRelative(0, 0, getResources().getDimensionPixelSize(android.R.dimen.alert_dialog_button_bar_height), 0);
    }

    private void applySmartReplyView(InflatedSmartReplies.SmartRepliesAndActions smartRepliesAndActions, NotificationEntry notificationEntry) {
        SmartReplyView.SmartReplies smartReplies;
        boolean z;
        View view = this.mExpandedChild;
        if (view != null) {
            SmartReplyView smartReplyViewApplySmartReplyView = applySmartReplyView(view, smartRepliesAndActions, notificationEntry, this.mExpandedInflatedSmartReplies);
            this.mExpandedSmartReplyView = smartReplyViewApplySmartReplyView;
            if (smartReplyViewApplySmartReplyView != null && ((smartReplies = smartRepliesAndActions.smartReplies) != null || smartRepliesAndActions.smartActions != null)) {
                boolean z2 = false;
                int size = smartReplies == null ? 0 : smartReplies.choices.size();
                SmartReplyView.SmartActions smartActions = smartRepliesAndActions.smartActions;
                int size2 = smartActions == null ? 0 : smartActions.actions.size();
                SmartReplyView.SmartReplies smartReplies2 = smartRepliesAndActions.smartReplies;
                if (smartReplies2 == null) {
                    z = smartRepliesAndActions.smartActions.fromAssistant;
                } else {
                    z = smartReplies2.fromAssistant;
                }
                boolean z3 = z;
                if (smartReplies2 != null && this.mSmartReplyConstants.getEffectiveEditChoicesBeforeSending(smartReplies2.remoteInput.getEditChoicesBeforeSending())) {
                    z2 = true;
                }
                this.mSmartReplyController.smartSuggestionsAdded(notificationEntry, size, size2, z3, z2);
            }
        }
        if (this.mHeadsUpChild == null || !this.mSmartReplyConstants.getShowInHeadsUp()) {
            return;
        }
        this.mHeadsUpSmartReplyView = applySmartReplyView(this.mHeadsUpChild, smartRepliesAndActions, notificationEntry, this.mHeadsUpInflatedSmartReplies);
    }

    private SmartReplyView applySmartReplyView(View view, InflatedSmartReplies.SmartRepliesAndActions smartRepliesAndActions, NotificationEntry notificationEntry, InflatedSmartReplies inflatedSmartReplies) {
        View viewFindViewById = view.findViewById(android.R.id.resolver_empty_state_container);
        SmartReplyView smartReplyView = null;
        smartReplyView = null;
        smartReplyView = null;
        if (!(viewFindViewById instanceof LinearLayout)) {
            return null;
        }
        LinearLayout linearLayout = (LinearLayout) viewFindViewById;
        if (!InflatedSmartReplies.shouldShowSmartReplyView(notificationEntry, smartRepliesAndActions)) {
            linearLayout.setVisibility(8);
            return null;
        }
        if (linearLayout.getChildCount() == 1 && (linearLayout.getChildAt(0) instanceof SmartReplyView)) {
            linearLayout.removeAllViews();
        }
        if (linearLayout.getChildCount() == 0 && inflatedSmartReplies != null && inflatedSmartReplies.getSmartReplyView() != null) {
            SmartReplyView smartReplyView2 = inflatedSmartReplies.getSmartReplyView();
            linearLayout.addView(smartReplyView2);
            smartReplyView = smartReplyView2;
        }
        if (smartReplyView != null) {
            smartReplyView.resetSmartSuggestions(linearLayout);
            smartReplyView.addPreInflatedButtons(inflatedSmartReplies.getSmartSuggestionButtons());
            smartReplyView.setBackgroundTintColor(notificationEntry.getRow().getCurrentBackgroundTint());
            linearLayout.setVisibility(0);
        }
        return smartReplyView;
    }

    public void setExpandedInflatedSmartReplies(InflatedSmartReplies inflatedSmartReplies) {
        this.mExpandedInflatedSmartReplies = inflatedSmartReplies;
        if (inflatedSmartReplies == null) {
            this.mExpandedSmartReplyView = null;
        }
    }

    public void setHeadsUpInflatedSmartReplies(InflatedSmartReplies inflatedSmartReplies) {
        this.mHeadsUpInflatedSmartReplies = inflatedSmartReplies;
        if (inflatedSmartReplies == null) {
            this.mHeadsUpSmartReplyView = null;
        }
    }

    public InflatedSmartReplies.SmartRepliesAndActions getCurrentSmartRepliesAndActions() {
        return this.mCurrentSmartRepliesAndActions;
    }

    public void closeRemoteInput() {
        RemoteInputView remoteInputView = this.mHeadsUpRemoteInput;
        if (remoteInputView != null) {
            remoteInputView.close();
        }
        RemoteInputView remoteInputView2 = this.mExpandedRemoteInput;
        if (remoteInputView2 != null) {
            remoteInputView2.close();
        }
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public void setRemoteInputController(RemoteInputController remoteInputController) {
        this.mRemoteInputController = remoteInputController;
    }

    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    public void updateExpandButtons(boolean z) {
        this.mExpandable = z;
        View view = this.mExpandedChild;
        if (view != null && view.getHeight() != 0 && ((this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && this.mHeadsUpChild != null && this.mContainingNotification.canShowHeadsUp() ? this.mExpandedChild.getHeight() <= this.mHeadsUpChild.getHeight() : !(this.mContractedChild != null && this.mExpandedChild.getHeight() > this.mContractedChild.getHeight()))) {
            z = false;
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.updateExpandability(z, this.mExpandClickListener);
        }
        if (this.mContractedChild != null) {
            this.mContractedWrapper.updateExpandability(z, this.mExpandClickListener);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.updateExpandability(z, this.mExpandClickListener);
        }
        this.mIsContentExpandable = z;
    }

    public NotificationHeaderView getNotificationHeader() {
        NotificationHeaderView notificationHeader = this.mContractedChild != null ? this.mContractedWrapper.getNotificationHeader() : null;
        if (notificationHeader == null && this.mExpandedChild != null) {
            notificationHeader = this.mExpandedWrapper.getNotificationHeader();
        }
        return (notificationHeader != null || this.mHeadsUpChild == null) ? notificationHeader : this.mHeadsUpWrapper.getNotificationHeader();
    }

    public void showAppOpsIcons(ArraySet<Integer> arraySet) {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.showAppOpsIcons(arraySet);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.showAppOpsIcons(arraySet);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.showAppOpsIcons(arraySet);
        }
    }

    public void setRecentlyAudiblyAlerted(boolean z) {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setRecentlyAudiblyAlerted(z);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setRecentlyAudiblyAlerted(z);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setRecentlyAudiblyAlerted(z);
        }
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper == null) {
            return null;
        }
        return visibleWrapper.getNotificationHeader();
    }

    public void setContainingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mContainingNotification = expandableNotificationRow;
    }

    public void setPeopleNotificationIdentifier(PeopleNotificationIdentifier peopleNotificationIdentifier) {
        this.mPeopleIdentifier = peopleNotificationIdentifier;
    }

    public void requestSelectLayout(boolean z) {
        selectLayout(z, false);
    }

    public void reInflateViews() {
        HybridNotificationView hybridNotificationView;
        if (!this.mIsChildInGroup || (hybridNotificationView = this.mSingleLineView) == null) {
            return;
        }
        removeView(hybridNotificationView);
        this.mSingleLineView = null;
        updateAllSingleLineViews();
    }

    public void setUserExpanding(boolean z) {
        this.mUserExpanding = z;
        if (z) {
            this.mTransformationStartVisibleType = this.mVisibleType;
            return;
        }
        this.mTransformationStartVisibleType = -1;
        int iCalculateVisibleType = calculateVisibleType();
        this.mVisibleType = iCalculateVisibleType;
        updateViewVisibilities(iCalculateVisibleType);
        updateBackgroundColor(false);
    }

    public void setSingleLineWidthIndention(int i) {
        if (i != this.mSingleLineWidthIndention) {
            this.mSingleLineWidthIndention = i;
            this.mContainingNotification.forceLayout();
            forceLayout();
        }
    }

    public HybridNotificationView getSingleLineView() {
        return this.mSingleLineView;
    }

    public void setRemoved() {
        RemoteInputView remoteInputView = this.mExpandedRemoteInput;
        if (remoteInputView != null) {
            remoteInputView.setRemoved();
        }
        RemoteInputView remoteInputView2 = this.mHeadsUpRemoteInput;
        if (remoteInputView2 != null) {
            remoteInputView2.setRemoved();
        }
        NotificationViewWrapper notificationViewWrapper = this.mExpandedWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setRemoved();
            this.mMediaTransferManager.setRemoved(this.mExpandedChild);
        }
        NotificationViewWrapper notificationViewWrapper2 = this.mContractedWrapper;
        if (notificationViewWrapper2 != null) {
            notificationViewWrapper2.setRemoved();
            this.mMediaTransferManager.setRemoved(this.mContractedChild);
        }
        NotificationViewWrapper notificationViewWrapper3 = this.mHeadsUpWrapper;
        if (notificationViewWrapper3 != null) {
            notificationViewWrapper3.setRemoved();
        }
    }

    public void setContentHeightAnimating(boolean z) {
        if (z) {
            return;
        }
        this.mContentHeightAtAnimationStart = -1;
    }

    @VisibleForTesting
    boolean isAnimatingVisibleType() {
        return this.mAnimationStartVisibleType != -1;
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        selectLayout(false, true);
    }

    public void setFocusOnVisibilityChange() {
        this.mFocusOnVisibilityChange = true;
    }

    public void setShelfIconVisible(boolean z) {
        this.mShelfIconVisible = z;
        updateIconVisibilities();
    }

    private void updateIconVisibilities() {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setShelfIconVisible(this.mShelfIconVisible);
        }
        NotificationViewWrapper notificationViewWrapper2 = this.mHeadsUpWrapper;
        if (notificationViewWrapper2 != null) {
            notificationViewWrapper2.setShelfIconVisible(this.mShelfIconVisible);
        }
        NotificationViewWrapper notificationViewWrapper3 = this.mExpandedWrapper;
        if (notificationViewWrapper3 != null) {
            notificationViewWrapper3.setShelfIconVisible(this.mShelfIconVisible);
        }
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z) {
            fireExpandedVisibleListenerIfVisible();
        }
    }

    public void setOnExpandedVisibleListener(Runnable runnable) {
        this.mExpandedVisibleListener = runnable;
        fireExpandedVisibleListenerIfVisible();
    }

    void performWhenContentInactive(int i, Runnable runnable) {
        View viewForVisibleType = getViewForVisibleType(i);
        if (viewForVisibleType == null || isContentViewInactive(i)) {
            runnable.run();
        } else {
            this.mOnContentViewInactiveListeners.put(viewForVisibleType, runnable);
        }
    }

    void removeContentInactiveRunnable(int i) {
        View viewForVisibleType = getViewForVisibleType(i);
        if (viewForVisibleType == null) {
            return;
        }
        this.mOnContentViewInactiveListeners.remove(viewForVisibleType);
    }

    public boolean isContentViewInactive(int i) {
        return isContentViewInactive(getViewForVisibleType(i));
    }

    private boolean isContentViewInactive(View view) {
        if (view != null && isShown()) {
            return (view.getVisibility() == 0 || getViewForVisibleType(this.mVisibleType) == view) ? false : true;
        }
        return true;
    }

    protected void onChildVisibilityChanged(View view, int i, int i2) {
        Runnable runnableRemove;
        super.onChildVisibilityChanged(view, i, i2);
        if (!isContentViewInactive(view) || (runnableRemove = this.mOnContentViewInactiveListeners.remove(view)) == null) {
            return;
        }
        runnableRemove.run();
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
    }

    public boolean isDimmable() {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        return notificationViewWrapper != null && notificationViewWrapper.isDimmable();
    }

    public boolean disallowSingleClick(float f, float f2) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(getVisibleType());
        if (visibleWrapper != null) {
            return visibleWrapper.disallowSingleClick(f, f2);
        }
        return false;
    }

    public boolean shouldClipToRounding(boolean z, boolean z2) {
        boolean zShouldClipToRounding = shouldClipToRounding(getVisibleType(), z, z2);
        return this.mUserExpanding ? zShouldClipToRounding | shouldClipToRounding(this.mTransformationStartVisibleType, z, z2) : zShouldClipToRounding;
    }

    private boolean shouldClipToRounding(int i, boolean z, boolean z2) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(i);
        if (visibleWrapper == null) {
            return false;
        }
        return visibleWrapper.shouldClipToRounding(z, z2);
    }

    public CharSequence getActiveRemoteInputText() {
        RemoteInputView remoteInputView = this.mExpandedRemoteInput;
        if (remoteInputView != null && remoteInputView.isActive()) {
            return this.mExpandedRemoteInput.getText();
        }
        RemoteInputView remoteInputView2 = this.mHeadsUpRemoteInput;
        if (remoteInputView2 == null || !remoteInputView2.isActive()) {
            return null;
        }
        return this.mHeadsUpRemoteInput.getText();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        float y = motionEvent.getY();
        RemoteInputView remoteInputForView = getRemoteInputForView(getViewForVisibleType(this.mVisibleType));
        if (remoteInputForView != null && remoteInputForView.getVisibility() == 0) {
            int height = this.mUnrestrictedContentHeight - remoteInputForView.getHeight();
            if (y <= this.mUnrestrictedContentHeight && y >= height) {
                motionEvent.offsetLocation(0.0f, -height);
                return remoteInputForView.dispatchTouchEvent(motionEvent);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean pointInView(float f, float f2, float f3) {
        return f >= (-f3) && f2 >= ((float) this.mClipTopAmount) - f3 && f < ((float) (((FrameLayout) this).mRight - ((FrameLayout) this).mLeft)) + f3 && f2 < ((float) this.mUnrestrictedContentHeight) + f3;
    }

    private RemoteInputView getRemoteInputForView(View view) {
        if (view == this.mExpandedChild) {
            return this.mExpandedRemoteInput;
        }
        if (view == this.mHeadsUpChild) {
            return this.mHeadsUpRemoteInput;
        }
        return null;
    }

    public int getExpandHeight() {
        int i;
        if (this.mExpandedChild != null) {
            i = 1;
        } else {
            if (this.mContractedChild == null) {
                return getMinHeight();
            }
            i = 0;
        }
        return getViewHeight(i) + getExtraRemoteInputHeight(this.mExpandedRemoteInput);
    }

    public int getHeadsUpHeight(boolean z) {
        int i;
        if (this.mHeadsUpChild != null) {
            i = 2;
        } else {
            if (this.mContractedChild == null) {
                return getMinHeight();
            }
            i = 0;
        }
        return getViewHeight(i, z) + getExtraRemoteInputHeight(this.mHeadsUpRemoteInput) + getExtraRemoteInputHeight(this.mExpandedRemoteInput);
    }

    public void setRemoteInputVisible(boolean z) {
        this.mRemoteInputVisible = z;
        setClipChildren(!z);
    }

    @Override // android.view.ViewGroup
    public void setClipChildren(boolean z) {
        super.setClipChildren(z && !this.mRemoteInputVisible);
    }

    public void setHeaderVisibleAmount(float f) {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setHeaderVisibleAmount(f);
        }
        NotificationViewWrapper notificationViewWrapper2 = this.mHeadsUpWrapper;
        if (notificationViewWrapper2 != null) {
            notificationViewWrapper2.setHeaderVisibleAmount(f);
        }
        NotificationViewWrapper notificationViewWrapper3 = this.mExpandedWrapper;
        if (notificationViewWrapper3 != null) {
            notificationViewWrapper3.setHeaderVisibleAmount(f);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("    ");
        printWriter.print("contentView visibility: " + getVisibility());
        printWriter.print(", alpha: " + getAlpha());
        printWriter.print(", clipBounds: " + getClipBounds());
        printWriter.print(", contentHeight: " + this.mContentHeight);
        printWriter.print(", visibleType: " + this.mVisibleType);
        View viewForVisibleType = getViewForVisibleType(this.mVisibleType);
        printWriter.print(", visibleView ");
        if (viewForVisibleType != null) {
            printWriter.print(" visibility: " + viewForVisibleType.getVisibility());
            printWriter.print(", alpha: " + viewForVisibleType.getAlpha());
            printWriter.print(", clipBounds: " + viewForVisibleType.getClipBounds());
        } else {
            printWriter.print("null");
        }
        printWriter.println();
    }

    public RemoteInputView getExpandedRemoteInput() {
        return this.mExpandedRemoteInput;
    }

    public View getShelfTransformationTarget() {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            return visibleWrapper.getShelfTransformationTarget();
        }
        return null;
    }

    public int getOriginalIconColor() {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            return visibleWrapper.getOriginalIconColor();
        }
        return 1;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            Log.e("NotificationContentView", "Drawing view failed: " + e);
            cancelNotification(e);
        }
    }

    private void cancelNotification(Exception exc) {
        try {
            setVisibility(8);
            IStatusBarService iStatusBarService = this.mStatusBarService;
            if (iStatusBarService != null) {
                iStatusBarService.onNotificationError(this.mStatusBarNotification.getPackageName(), this.mStatusBarNotification.getTag(), this.mStatusBarNotification.getId(), this.mStatusBarNotification.getUid(), this.mStatusBarNotification.getInitialPid(), exc.getMessage(), this.mStatusBarNotification.getUser().getIdentifier());
            }
        } catch (RemoteException e) {
            Log.e("NotificationContentView", "cancelNotification failed: " + e);
        }
    }
}
