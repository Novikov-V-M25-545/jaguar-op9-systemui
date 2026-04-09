package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MathUtils;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.VisibilityLocationProvider;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.FooterView;
import com.android.systemui.statusbar.notification.row.ForegroundServiceDungeonView;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.NotificationSnooze;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper;
import com.android.systemui.statusbar.phone.HeadsUpAppearanceController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.HeadsUpTouchHelper;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import com.android.systemui.statusbar.policy.ScrollAdapter;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Assert;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class NotificationStackScrollLayout extends ViewGroup implements ScrollAdapter, NotificationListContainer, ConfigurationController.ConfigurationListener, Dumpable, DynamicPrivacyController.Listener {
    private boolean mActivateNeedsAnimation;
    private int mActivePointerId;
    private ArrayList<View> mAddedHeadsUpChildren;
    private final boolean mAllowLongPress;
    private final AmbientState mAmbientState;
    private boolean mAnimateBottomOnLayout;
    private boolean mAnimateNextBackgroundBottom;
    private boolean mAnimateNextBackgroundTop;
    private boolean mAnimateNextSectionBoundsChange;
    private ArrayList<AnimationEvent> mAnimationEvents;
    private HashSet<Runnable> mAnimationFinishedRunnables;
    private boolean mAnimationRunning;
    private boolean mAnimationsEnabled;
    private final Rect mBackgroundAnimationRect;
    private final Paint mBackgroundPaint;
    private ViewTreeObserver.OnPreDrawListener mBackgroundUpdater;
    private float mBackgroundXFactor;
    private boolean mBackwardScrollable;
    private final IStatusBarService mBarService;
    private int mBgColor;
    private int mBottomInset;
    private int mBottomMargin;
    private int mCachedBackgroundColor;
    private boolean mChangePositionInProgress;
    boolean mCheckForLeavebehind;
    private boolean mChildTransferInProgress;
    private ArrayList<ExpandableView> mChildrenChangingPositions;
    private HashSet<ExpandableView> mChildrenToAddAnimated;
    private ArrayList<ExpandableView> mChildrenToRemoveAnimated;
    private boolean mChildrenUpdateRequested;
    private ViewTreeObserver.OnPreDrawListener mChildrenUpdater;
    protected boolean mClearAllEnabled;
    private HashSet<ExpandableView> mClearTransientViewsWhenFinished;
    private final Rect mClipRect;
    private int mCollapsedSize;
    private final SysuiColorExtractor mColorExtractor;
    private int mContentHeight;
    private boolean mContinuousBackgroundUpdate;
    private boolean mContinuousShadowUpdate;
    private int mCornerRadius;
    private int mCurrentStackHeight;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private float mDimAmount;
    private ValueAnimator mDimAnimator;
    private final Animator.AnimatorListener mDimEndListener;
    private ValueAnimator.AnimatorUpdateListener mDimUpdateListener;
    private boolean mDimmedNeedsAnimation;
    private boolean mDisallowDismissInThisMotion;
    private boolean mDisallowScrollingInThisMotion;
    private boolean mDismissAllInProgress;
    private boolean mDismissRtl;
    private final DisplayMetrics mDisplayMetrics;
    private boolean mDontClampNextScroll;
    private boolean mDontReportNextOverScroll;
    private int mDownX;
    private final DragDownHelper.DragDownCallback mDragDownCallback;
    private final DynamicPrivacyController mDynamicPrivacyController;
    protected EmptyShadeView mEmptyShadeView;
    private final NotificationEntryManager mEntryManager;
    private boolean mEverythingNeedsAnimation;
    private ExpandHelper mExpandHelper;
    private ExpandHelper.Callback mExpandHelperCallback;
    private ExpandableView mExpandedGroupView;
    private float mExpandedHeight;
    private ArrayList<BiConsumer<Float, Float>> mExpandedHeightListeners;
    private boolean mExpandedInThisMotion;
    private boolean mExpandingNotification;
    private boolean mFadeNotificationsOnDismiss;
    private FalsingManager mFalsingManager;
    private final FeatureFlags mFeatureFlags;
    private final ForegroundServiceSectionController mFgsSectionController;
    private ForegroundServiceDungeonView mFgsSectionView;
    private Runnable mFinishScrollingCallback;
    protected FooterView mFooterView;
    private boolean mForceNoOverlappingRendering;
    private View mForcedScroll;
    private boolean mForwardScrollable;
    private HashSet<View> mFromMoreCardAdditions;
    private int mGapHeight;
    private boolean mGenerateChildOrderChangedEvent;
    private long mGoToFullShadeDelay;
    private boolean mGoToFullShadeNeedsAnimation;
    private boolean mGroupExpandedForMeasure;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private HeadsUpAppearanceController mHeadsUpAppearanceController;
    private final HeadsUpTouchHelper.Callback mHeadsUpCallback;
    private HashSet<Pair<ExpandableNotificationRow, Boolean>> mHeadsUpChangeAnimations;
    private boolean mHeadsUpGoingAwayAnimationsAllowed;
    private int mHeadsUpInset;
    private HeadsUpManagerPhone mHeadsUpManager;
    private boolean mHideSensitiveNeedsAnimation;
    private Interpolator mHideXInterpolator;
    private boolean mHighPriorityBeforeSpeedBump;
    private NotificationIconAreaController mIconAreaController;
    private int mIconColor;
    private boolean mInHeadsUpPinnedMode;
    private int mIncreasedPaddingBetweenElements;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mInterpolatedHideAmount;
    private int mIntrinsicContentHeight;
    private int mIntrinsicPadding;
    private boolean mIsBeingDragged;
    private boolean mIsClipped;
    private boolean mIsCurrentUserSetup;
    private boolean mIsExpanded;
    private boolean mIsExpansionChanging;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardMediaController mKeyguardMediaController;
    private int mLastMotionY;
    private float mLastSentAppear;
    private float mLastSentExpandedHeight;
    private float mLinearHideAmount;
    private NotificationLogger.OnChildLocationsChangedListener mListener;
    private final LockscreenGestureLogger mLockscreenGestureLogger;
    private final NotificationLockscreenUserManager.UserChangedListener mLockscreenUserChangeListener;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private ExpandableNotificationRow.LongPressListener mLongPressListener;
    private int mMaxDisplayedNotifications;
    private int mMaxLayoutHeight;
    private float mMaxOverScroll;
    private int mMaxScrollAfterExpand;
    private int mMaxTopPadding;
    private int mMaximumVelocity;

    @VisibleForTesting
    protected final NotificationMenuRowPlugin.OnMenuEventListener mMenuEventListener;

    @VisibleForTesting
    protected final MetricsLogger mMetricsLogger;
    private int mMinInteractionHeight;
    private float mMinTopOverScrollToEscape;
    private int mMinimumVelocity;
    private boolean mNeedViewResizeAnimation;
    private boolean mNeedsAnimation;
    private boolean mNoAmbient;
    private final NotifCollection mNotifCollection;
    private final NotifPipeline mNotifPipeline;
    private NotificationActivityStarter mNotificationActivityStarter;
    private int mNotificationBackgroundAlpha;
    private final NotificationSwipeHelper.NotificationCallback mNotificationCallback;
    private final NotificationGutsManager mNotificationGutsManager;
    private NotificationPanelViewController mNotificationPanelController;
    private ColorExtractor.OnColorsChangedListener mOnColorsChangedListener;
    private OnEmptySpaceClickListener mOnEmptySpaceClickListener;
    private final NotificationGroupManager.OnGroupChangeListener mOnGroupChangeListener;
    private ExpandableView.OnHeightChangedListener mOnHeightChangedListener;
    private boolean mOnlyScrollingInThisMotion;
    private final ViewOutlineProvider mOutlineProvider;
    private float mOverScrolledBottomPixels;
    private float mOverScrolledTopPixels;
    private int mOverflingDistance;
    private OnOverscrollTopChangedListener mOverscrollTopChangedListener;
    private int mOwnScrollY;
    private int mPaddingBetweenElements;
    private boolean mPanelTracking;
    private boolean mPulsing;
    protected ViewGroup mQsContainer;
    private boolean mQsExpanded;
    private float mQsExpansionFraction;
    private Runnable mReclamp;
    private Runnable mReflingAndAnimateScroll;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private Rect mRequestedClipBounds;
    private final NotificationRoundnessManager mRoundnessManager;
    private ViewTreeObserver.OnPreDrawListener mRunningAnimationUpdater;
    private ScrimController mScrimController;
    private boolean mScrollable;
    private boolean mScrolledToTopOnFirstDown;
    private OverScroller mScroller;
    protected boolean mScrollingEnabled;
    private NotificationSection[] mSections;
    private final NotificationSectionsManager mSectionsManager;
    private ViewTreeObserver.OnPreDrawListener mShadowUpdater;
    private NotificationShelf mShelf;
    private final boolean mShouldDrawNotificationBackground;
    private boolean mShouldShowShelfOnly;
    private boolean mShowDimissButton;
    private int mSidePaddings;
    private float mSlopMultiplier;
    private PorterDuffXfermode mSrcMode;
    protected final StackScrollAlgorithm mStackScrollAlgorithm;
    private float mStackTranslation;
    private final StackStateAnimator mStateAnimator;
    private final StatusBarStateController.StateListener mStateListener;
    private StatusBar mStatusBar;
    private int mStatusBarHeight;
    private int mStatusBarState;
    private final SysuiStatusBarStateController mStatusbarStateController;
    private final NotificationSwipeHelper mSwipeHelper;
    private ArrayList<View> mSwipedOutViews;
    private boolean mSwipingInProgress;
    private int[] mTempInt2;
    private final ArrayList<Pair<ExpandableNotificationRow, Boolean>> mTmpList;
    private final Rect mTmpRect;
    private ArrayList<ExpandableView> mTmpSortedChildren;
    private int mTopPadding;
    private boolean mTopPaddingNeedsAnimation;
    private float mTopPaddingOverflow;
    private boolean mTouchIsClick;
    private int mTouchSlop;
    private boolean mTrackingHeadsUp;
    protected final UiEventLogger mUiEventLogger;
    private boolean mUsingLightTheme;
    private VelocityTracker mVelocityTracker;
    private Comparator<ExpandableView> mViewPositionComparator;
    private final VisualStabilityManager mVisualStabilityManager;
    private int mWaterfallTopInset;
    private boolean mWillExpand;
    private final ZenModeController mZenController;

    public interface OnEmptySpaceClickListener {
        void onEmptySpaceClicked(float f, float f2);
    }

    public interface OnOverscrollTopChangedListener {
        void flingTopOverscroll(float f, boolean z);

        void onOverscrollTopChanged(float f, boolean z);
    }

    @Override // com.android.systemui.statusbar.policy.ScrollAdapter
    public View getHostView() {
        return this;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public ViewGroup getViewParentForNotification(NotificationEntry notificationEntry) {
        return this;
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$new$0() {
        updateBackground();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2(ColorExtractor colorExtractor, int i) {
        updateDecorViews(this.mColorExtractor.getNeutralColors().supportsDarkText());
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet, boolean z, final NotificationRoundnessManager notificationRoundnessManager, DynamicPrivacyController dynamicPrivacyController, SysuiStatusBarStateController sysuiStatusBarStateController, HeadsUpManagerPhone headsUpManagerPhone, KeyguardBypassController keyguardBypassController, final KeyguardMediaController keyguardMediaController, FalsingManager falsingManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGutsManager notificationGutsManager, ZenModeController zenModeController, NotificationSectionsManager notificationSectionsManager, ForegroundServiceSectionController foregroundServiceSectionController, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController, FeatureFlags featureFlags, NotifPipeline notifPipeline, NotificationEntryManager notificationEntryManager, NotifCollection notifCollection, UiEventLogger uiEventLogger) throws Resources.NotFoundException {
        super(context, attributeSet, 0, 0);
        this.mCurrentStackHeight = Integer.MAX_VALUE;
        Paint paint = new Paint();
        this.mBackgroundPaint = paint;
        this.mActivePointerId = -1;
        this.mBottomInset = 0;
        this.mChildrenToAddAnimated = new HashSet<>();
        this.mAddedHeadsUpChildren = new ArrayList<>();
        this.mChildrenToRemoveAnimated = new ArrayList<>();
        this.mChildrenChangingPositions = new ArrayList<>();
        this.mFromMoreCardAdditions = new HashSet<>();
        this.mAnimationEvents = new ArrayList<>();
        this.mSwipedOutViews = new ArrayList<>();
        this.mStateAnimator = new StackStateAnimator(this);
        this.mIsExpanded = true;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.updateForcedScroll();
                NotificationStackScrollLayout.this.updateChildren();
                NotificationStackScrollLayout.this.mChildrenUpdateRequested = false;
                NotificationStackScrollLayout.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        NotificationLockscreenUserManager.UserChangedListener userChangedListener = new NotificationLockscreenUserManager.UserChangedListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.2
            @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
            public void onUserChanged(int i) {
                NotificationStackScrollLayout.this.updateSensitiveness(false);
            }
        };
        this.mLockscreenUserChangeListener = userChangedListener;
        this.mTempInt2 = new int[2];
        this.mAnimationFinishedRunnables = new HashSet<>();
        this.mClearTransientViewsWhenFinished = new HashSet<>();
        this.mHeadsUpChangeAnimations = new HashSet<>();
        this.mTmpList = new ArrayList<>();
        this.mRunningAnimationUpdater = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.3
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.onPreDrawDuringAnimation();
                return true;
            }
        };
        this.mTmpSortedChildren = new ArrayList<>();
        this.mDimEndListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                NotificationStackScrollLayout.this.mDimAnimator = null;
            }
        };
        this.mDimUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationStackScrollLayout.this.setDimAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        };
        this.mShadowUpdater = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.6
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.updateViewShadows();
                return true;
            }
        };
        this.mBackgroundUpdater = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda5
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public final boolean onPreDraw() {
                return this.f$0.lambda$new$0();
            }
        };
        this.mViewPositionComparator = new Comparator<ExpandableView>() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.7
            @Override // java.util.Comparator
            public int compare(ExpandableView expandableView, ExpandableView expandableView2) {
                float translationY = expandableView.getTranslationY() + expandableView.getActualHeight();
                float translationY2 = expandableView2.getTranslationY() + expandableView2.getActualHeight();
                if (translationY < translationY2) {
                    return -1;
                }
                return translationY > translationY2 ? 1 : 0;
            }
        };
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.8
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (NotificationStackScrollLayout.this.mAmbientState.isHiddenAtAll()) {
                    outline.setRoundRect(NotificationStackScrollLayout.this.mBackgroundAnimationRect, MathUtils.lerp(NotificationStackScrollLayout.this.mCornerRadius / 2.0f, NotificationStackScrollLayout.this.mCornerRadius, NotificationStackScrollLayout.this.mHideXInterpolator.getInterpolation((1.0f - NotificationStackScrollLayout.this.mLinearHideAmount) * NotificationStackScrollLayout.this.mBackgroundXFactor)));
                    outline.setAlpha(1.0f - NotificationStackScrollLayout.this.mAmbientState.getHideAmount());
                    return;
                }
                ViewOutlineProvider.BACKGROUND.getOutline(view, outline);
            }
        };
        this.mOutlineProvider = viewOutlineProvider;
        this.mSrcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.mInterpolatedHideAmount = 0.0f;
        this.mLinearHideAmount = 0.0f;
        this.mBackgroundXFactor = 1.0f;
        this.mMaxDisplayedNotifications = -1;
        this.mClipRect = new Rect();
        this.mHeadsUpGoingAwayAnimationsAllowed = true;
        this.mReflingAndAnimateScroll = new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$1();
            }
        };
        this.mBackgroundAnimationRect = new Rect();
        this.mExpandedHeightListeners = new ArrayList<>();
        this.mTmpRect = new Rect();
        DeviceProvisionedController deviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mRemoteInputManager = (NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class);
        SysuiColorExtractor sysuiColorExtractor = (SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class);
        this.mColorExtractor = sysuiColorExtractor;
        this.mDisplayMetrics = (DisplayMetrics) Dependency.get(DisplayMetrics.class);
        this.mLockscreenGestureLogger = (LockscreenGestureLogger) Dependency.get(LockscreenGestureLogger.class);
        this.mVisualStabilityManager = (VisualStabilityManager) Dependency.get(VisualStabilityManager.class);
        this.mHideXInterpolator = Interpolators.FAST_OUT_SLOW_IN;
        this.mOnColorsChangedListener = new ColorExtractor.OnColorsChangedListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda6
            public final void onColorsChanged(ColorExtractor colorExtractor, int i) {
                this.f$0.lambda$new$2(colorExtractor, i);
            }
        };
        this.mReclamp = new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.13
            @Override // java.lang.Runnable
            public void run() {
                NotificationStackScrollLayout.this.mScroller.startScroll(((ViewGroup) NotificationStackScrollLayout.this).mScrollX, NotificationStackScrollLayout.this.mOwnScrollY, 0, NotificationStackScrollLayout.this.getScrollRange() - NotificationStackScrollLayout.this.mOwnScrollY);
                NotificationStackScrollLayout.this.mDontReportNextOverScroll = true;
                NotificationStackScrollLayout.this.mDontClampNextScroll = true;
                NotificationStackScrollLayout.this.lambda$new$1();
            }
        };
        this.mStateListener = new StatusBarStateController.StateListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.14
            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStatePreChange(int i, int i2) {
                if (i == 2 && i2 == 1) {
                    NotificationStackScrollLayout.this.requestAnimateEverything();
                }
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                NotificationStackScrollLayout.this.setStatusBarState(i);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStatePostChange() {
                NotificationStackScrollLayout.this.onStatePostChange();
            }
        };
        NotificationMenuRowPlugin.OnMenuEventListener onMenuEventListener = new NotificationMenuRowPlugin.OnMenuEventListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.15
            @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.OnMenuEventListener
            public void onMenuClicked(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                if (NotificationStackScrollLayout.this.mLongPressListener == null) {
                    return;
                }
                if (view instanceof ExpandableNotificationRow) {
                    NotificationStackScrollLayout.this.mMetricsLogger.write(((ExpandableNotificationRow) view).getEntry().getSbn().getLogMaker().setCategory(333).setType(4));
                }
                NotificationStackScrollLayout.this.mLongPressListener.onLongPress(view, i, i2, menuItem);
            }

            @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.OnMenuEventListener
            public void onMenuReset(View view) {
                View translatingParentView = NotificationStackScrollLayout.this.mSwipeHelper.getTranslatingParentView();
                if (translatingParentView == null || view != translatingParentView) {
                    return;
                }
                NotificationStackScrollLayout.this.mSwipeHelper.clearExposedMenuView();
                NotificationStackScrollLayout.this.mSwipeHelper.clearTranslatingParentView();
                if (view instanceof ExpandableNotificationRow) {
                    NotificationStackScrollLayout.this.mHeadsUpManager.setMenuShown(((ExpandableNotificationRow) view).getEntry(), false);
                }
            }

            @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.OnMenuEventListener
            public void onMenuShown(View view) {
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    NotificationStackScrollLayout.this.mMetricsLogger.write(expandableNotificationRow.getEntry().getSbn().getLogMaker().setCategory(332).setType(4));
                    NotificationStackScrollLayout.this.mHeadsUpManager.setMenuShown(expandableNotificationRow.getEntry(), true);
                    NotificationStackScrollLayout.this.mSwipeHelper.onMenuShown(view);
                    NotificationStackScrollLayout.this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
                    NotificationMenuRowPlugin provider = expandableNotificationRow.getProvider();
                    if (provider.shouldShowGutsOnSnapOpen()) {
                        NotificationMenuRowPlugin.MenuItem menuItemMenuItemToExposeOnSnap = provider.menuItemToExposeOnSnap();
                        if (menuItemMenuItemToExposeOnSnap != null) {
                            Point revealAnimationOrigin = provider.getRevealAnimationOrigin();
                            NotificationStackScrollLayout.this.mNotificationGutsManager.openGuts(view, revealAnimationOrigin.x, revealAnimationOrigin.y, menuItemMenuItemToExposeOnSnap);
                        } else {
                            Log.e("StackScroller", "Provider has shouldShowGutsOnSnapOpen, but provided no menu item in menuItemtoExposeOnSnap. Skipping.");
                        }
                        NotificationStackScrollLayout.this.resetExposedMenuView(false, true);
                    }
                }
            }
        };
        this.mMenuEventListener = onMenuEventListener;
        NotificationSwipeHelper.NotificationCallback notificationCallback = new NotificationSwipeHelper.NotificationCallback() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.16
            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void onDismiss() {
                NotificationStackScrollLayout.this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void onSnooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
                NotificationStackScrollLayout.this.mStatusBar.setNotificationSnoozed(statusBarNotification, snoozeOption);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void onSnooze(StatusBarNotification statusBarNotification, int i) {
                NotificationStackScrollLayout.this.mStatusBar.setNotificationSnoozed(statusBarNotification, i);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public boolean shouldDismissQuickly() {
                return NotificationStackScrollLayout.this.isExpanded() && NotificationStackScrollLayout.this.mAmbientState.isFullyAwake();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onDragCancelled(View view) {
                NotificationStackScrollLayout.this.setSwipingInProgress(false);
                NotificationStackScrollLayout.this.mFalsingManager.onNotificationStopDismissing();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onChildDismissed(View view) {
                if (view instanceof ActivatableNotificationView) {
                    ActivatableNotificationView activatableNotificationView = (ActivatableNotificationView) view;
                    if (!activatableNotificationView.isDismissed()) {
                        handleChildViewDismissed(view);
                    }
                    ViewGroup transientContainer = activatableNotificationView.getTransientContainer();
                    if (transientContainer != null) {
                        transientContainer.removeTransientView(view);
                    }
                }
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void handleChildViewDismissed(View view) {
                boolean zPerformDismissWithBlockingHelper = false;
                NotificationStackScrollLayout.this.setSwipingInProgress(false);
                if (NotificationStackScrollLayout.this.mDismissAllInProgress) {
                    return;
                }
                NotificationStackScrollLayout.this.mAmbientState.onDragFinished(view);
                NotificationStackScrollLayout.this.updateContinuousShadowDrawing();
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    if (expandableNotificationRow.isHeadsUp()) {
                        NotificationStackScrollLayout.this.mHeadsUpManager.addSwipedOutNotification(expandableNotificationRow.getEntry().getSbn().getKey());
                    }
                    zPerformDismissWithBlockingHelper = expandableNotificationRow.performDismissWithBlockingHelper(false);
                }
                if (view instanceof PeopleHubView) {
                    NotificationStackScrollLayout.this.mSectionsManager.hidePeopleRow();
                }
                if (!zPerformDismissWithBlockingHelper) {
                    NotificationStackScrollLayout.this.mSwipedOutViews.add(view);
                }
                NotificationStackScrollLayout.this.mFalsingManager.onNotificationDismissed();
                if (NotificationStackScrollLayout.this.mFalsingManager.shouldEnforceBouncer()) {
                    NotificationStackScrollLayout.this.mStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
                }
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean isAntiFalsingNeeded() {
                return NotificationStackScrollLayout.this.onKeyguard();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public View getChildAtPosition(MotionEvent motionEvent) {
                ExpandableNotificationRow notificationParent;
                ExpandableView childAtPosition = NotificationStackScrollLayout.this.getChildAtPosition(motionEvent.getX(), motionEvent.getY(), true, false);
                return ((childAtPosition instanceof ExpandableNotificationRow) && (notificationParent = ((ExpandableNotificationRow) childAtPosition).getNotificationParent()) != null && notificationParent.areChildrenExpanded()) ? (notificationParent.areGutsExposed() || NotificationStackScrollLayout.this.mSwipeHelper.getExposedMenuView() == notificationParent || (notificationParent.getAttachedChildren().size() == 1 && notificationParent.getEntry().isClearable())) ? notificationParent : childAtPosition : childAtPosition;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onBeginDrag(View view) {
                NotificationStackScrollLayout.this.mFalsingManager.onNotificationStartDismissing();
                NotificationStackScrollLayout.this.setSwipingInProgress(true);
                NotificationStackScrollLayout.this.mAmbientState.onBeginDrag((ExpandableView) view);
                NotificationStackScrollLayout.this.updateContinuousShadowDrawing();
                NotificationStackScrollLayout.this.updateContinuousBackgroundDrawing();
                NotificationStackScrollLayout.this.requestChildrenUpdate();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onChildSnappedBack(View view, float f) {
                NotificationStackScrollLayout.this.mAmbientState.onDragFinished(view);
                NotificationStackScrollLayout.this.updateContinuousShadowDrawing();
                NotificationStackScrollLayout.this.updateContinuousBackgroundDrawing();
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    if (expandableNotificationRow.isPinned() && !canChildBeDismissed(expandableNotificationRow) && expandableNotificationRow.getEntry().getSbn().getNotification().fullScreenIntent == null) {
                        NotificationStackScrollLayout.this.mHeadsUpManager.removeNotification(expandableNotificationRow.getEntry().getSbn().getKey(), true);
                    }
                }
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean updateSwipeProgress(View view, boolean z2, float f) {
                return !NotificationStackScrollLayout.this.mFadeNotificationsOnDismiss;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public float getFalsingThresholdFactor() {
                return NotificationStackScrollLayout.this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public int getConstrainSwipeStartPosition() {
                NotificationMenuRowPlugin currentMenuRow = NotificationStackScrollLayout.this.mSwipeHelper.getCurrentMenuRow();
                if (currentMenuRow != null) {
                    return Math.abs(currentMenuRow.getMenuSnapTarget());
                }
                return 0;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean canChildBeDismissed(View view) {
                return NotificationStackScrollLayout.canChildBeDismissed(view);
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean canChildBeDismissedInDirection(View view, boolean z2) {
                return canChildBeDismissed(view);
            }
        };
        this.mNotificationCallback = notificationCallback;
        this.mDragDownCallback = new AnonymousClass17();
        this.mHeadsUpCallback = new HeadsUpTouchHelper.Callback() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.18
            @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
            public ExpandableView getChildAtRawPosition(float f, float f2) {
                return NotificationStackScrollLayout.this.getChildAtRawPosition(f, f2);
            }

            @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
            public boolean isExpanded() {
                return NotificationStackScrollLayout.this.mIsExpanded;
            }

            @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
            public Context getContext() {
                return ((ViewGroup) NotificationStackScrollLayout.this).mContext;
            }
        };
        this.mOnGroupChangeListener = new NotificationGroupManager.OnGroupChangeListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.19
            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupExpansionChanged(final ExpandableNotificationRow expandableNotificationRow, boolean z2) {
                boolean z3 = !NotificationStackScrollLayout.this.mGroupExpandedForMeasure && NotificationStackScrollLayout.this.mAnimationsEnabled && (NotificationStackScrollLayout.this.mIsExpanded || expandableNotificationRow.isPinned());
                if (z3) {
                    NotificationStackScrollLayout.this.mExpandedGroupView = expandableNotificationRow;
                    NotificationStackScrollLayout.this.mNeedsAnimation = true;
                }
                expandableNotificationRow.setChildrenExpanded(z2, z3);
                if (!NotificationStackScrollLayout.this.mGroupExpandedForMeasure) {
                    NotificationStackScrollLayout.this.onHeightChanged(expandableNotificationRow, false);
                }
                NotificationStackScrollLayout.this.runAfterAnimationFinished(new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.19.1
                    @Override // java.lang.Runnable
                    public void run() {
                        expandableNotificationRow.onFinishedExpansionChange();
                    }
                });
            }

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupCreatedFromChildren(NotificationGroupManager.NotificationGroup notificationGroup) {
                NotificationStackScrollLayout.this.mStatusBar.requestNotificationUpdate("onGroupCreatedFromChildren");
            }

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupsChanged() {
                NotificationStackScrollLayout.this.mStatusBar.requestNotificationUpdate("onGroupsChanged");
            }
        };
        this.mExpandHelperCallback = new ExpandHelper.Callback() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.20
            @Override // com.android.systemui.ExpandHelper.Callback
            public ExpandableView getChildAtPosition(float f, float f2) {
                return NotificationStackScrollLayout.this.getChildAtPosition(f, f2);
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public ExpandableView getChildAtRawPosition(float f, float f2) {
                return NotificationStackScrollLayout.this.getChildAtRawPosition(f, f2);
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public boolean canChildBeExpanded(View view) {
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    if (expandableNotificationRow.isExpandable() && !expandableNotificationRow.areGutsExposed() && (NotificationStackScrollLayout.this.mIsExpanded || !expandableNotificationRow.isPinned())) {
                        return true;
                    }
                }
                return false;
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void setUserExpandedChild(View view, boolean z2) {
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    if (z2 && NotificationStackScrollLayout.this.onKeyguard()) {
                        expandableNotificationRow.setUserLocked(false);
                        NotificationStackScrollLayout.this.updateContentHeight();
                        NotificationStackScrollLayout.this.notifyHeightChangeListener(expandableNotificationRow);
                    } else {
                        expandableNotificationRow.setUserExpanded(z2, true);
                        expandableNotificationRow.onExpandedByGesture(z2);
                    }
                }
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void setExpansionCancelled(View view) {
                if (view instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) view).setGroupExpansionChanging(false);
                }
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void setUserLockedChild(View view, boolean z2) {
                if (view instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) view).setUserLocked(z2);
                }
                NotificationStackScrollLayout.this.cancelLongPress();
                NotificationStackScrollLayout.this.requestDisallowInterceptTouchEvent(true);
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void expansionStateChanged(boolean z2) {
                NotificationStackScrollLayout.this.mExpandingNotification = z2;
                if (NotificationStackScrollLayout.this.mExpandedInThisMotion) {
                    return;
                }
                NotificationStackScrollLayout notificationStackScrollLayout = NotificationStackScrollLayout.this;
                notificationStackScrollLayout.mMaxScrollAfterExpand = notificationStackScrollLayout.mOwnScrollY;
                NotificationStackScrollLayout.this.mExpandedInThisMotion = true;
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public int getMaxExpandHeight(ExpandableView expandableView) {
                return expandableView.getMaxContentHeight();
            }
        };
        Resources resources = getResources();
        this.mAllowLongPress = z;
        this.mRoundnessManager = notificationRoundnessManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mNotificationGutsManager = notificationGutsManager;
        this.mHeadsUpManager = headsUpManagerPhone;
        headsUpManagerPhone.addListener(notificationRoundnessManager);
        this.mHeadsUpManager.setAnimationStateHandler(new HeadsUpManagerPhone.AnimationStateHandler() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda9
            @Override // com.android.systemui.statusbar.phone.HeadsUpManagerPhone.AnimationStateHandler
            public final void setHeadsUpGoingAwayAnimationsAllowed(boolean z2) {
                this.f$0.setHeadsUpGoingAwayAnimationsAllowed(z2);
            }
        });
        this.mKeyguardBypassController = keyguardBypassController;
        this.mFalsingManager = falsingManager;
        this.mZenController = zenModeController;
        this.mFgsSectionController = foregroundServiceSectionController;
        this.mSectionsManager = notificationSectionsManager;
        notificationSectionsManager.initialize(this, LayoutInflater.from(context));
        notificationSectionsManager.setOnClearSilentNotifsClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$3(view);
            }
        });
        this.mSections = notificationSectionsManager.createSectionsForBuckets();
        this.mAmbientState = new AmbientState(context, notificationSectionsManager, this.mHeadsUpManager);
        this.mBgColor = context.getColor(R.color.notification_shade_background_color);
        ExpandHelper expandHelper = new ExpandHelper(getContext(), this.mExpandHelperCallback, resources.getDimensionPixelSize(R.dimen.notification_min_height), resources.getDimensionPixelSize(R.dimen.notification_max_height));
        this.mExpandHelper = expandHelper;
        expandHelper.setEventSource(this);
        this.mExpandHelper.setScrollAdapter(this);
        this.mSwipeHelper = new NotificationSwipeHelper(0, notificationCallback, getContext(), onMenuEventListener, this.mFalsingManager);
        this.mStackScrollAlgorithm = createStackScrollAlgorithm(context);
        initView(context);
        boolean z2 = resources.getBoolean(R.bool.config_drawNotificationBackground);
        this.mShouldDrawNotificationBackground = z2;
        this.mFadeNotificationsOnDismiss = resources.getBoolean(R.bool.config_fadeNotificationsOnDismiss);
        notificationRoundnessManager.setAnimatedChildren(this.mChildrenToAddAnimated);
        notificationRoundnessManager.setOnRoundingChangedCallback(new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.invalidate();
            }
        });
        Objects.requireNonNull(notificationRoundnessManager);
        addOnExpandedHeightChangedListener(new BiConsumer() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda19
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                notificationRoundnessManager.setExpanded(((Float) obj).floatValue(), ((Float) obj2).floatValue());
            }
        });
        notificationLockscreenUserManager.addUserChangedListener(userChangedListener);
        setOutlineProvider(viewOutlineProvider);
        final NotificationBlockingHelperManager notificationBlockingHelperManager = (NotificationBlockingHelperManager) Dependency.get(NotificationBlockingHelperManager.class);
        addOnExpandedHeightChangedListener(new BiConsumer() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda18
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                NotificationStackScrollLayout.lambda$new$4(notificationBlockingHelperManager, (Float) obj, (Float) obj2);
            }
        });
        setWillNotDraw(!(z2));
        paint.setAntiAlias(true);
        this.mClearAllEnabled = resources.getBoolean(R.bool.config_enableNotificationsClearAll);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(new TunerService.Tunable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda10
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                this.f$0.lambda$new$5(str, str2);
            }
        }, "high_priority", "notification_dismiss_rtl", "notification_history_enabled", "system:notification_material_dismiss");
        deviceProvisionedController.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.9
            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onDeviceProvisionedChanged() {
                updateCurrentUserIsSetup();
            }

            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSwitched() {
                updateCurrentUserIsSetup();
            }

            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSetupChanged() {
                updateCurrentUserIsSetup();
            }

            private void updateCurrentUserIsSetup() {
                NotificationStackScrollLayout notificationStackScrollLayout = NotificationStackScrollLayout.this;
                notificationStackScrollLayout.setCurrentUserSetup(notificationStackScrollLayout.mDeviceProvisionedController.isCurrentUserSetup());
            }
        });
        this.mFeatureFlags = featureFlags;
        this.mNotifPipeline = notifPipeline;
        this.mEntryManager = notificationEntryManager;
        this.mNotifCollection = notifCollection;
        if (featureFlags.isNewNotifPipelineRenderingEnabled()) {
            notifPipeline.addCollectionListener(new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.10
                @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
                public void onEntryUpdated(NotificationEntry notificationEntry) {
                    NotificationStackScrollLayout.this.onEntryUpdated(notificationEntry);
                }
            });
        } else {
            notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.11
                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                    NotificationStackScrollLayout.this.onEntryUpdated(notificationEntry);
                }
            });
        }
        dynamicPrivacyController.addListener(this);
        this.mDynamicPrivacyController = dynamicPrivacyController;
        this.mStatusbarStateController = sysuiStatusBarStateController;
        initializeForegroundServiceSection(foregroundServiceDismissalFeatureController);
        this.mUiEventLogger = uiEventLogger;
        sysuiColorExtractor.addOnColorsChangedListener(this.mOnColorsChangedListener);
        this.mKeyguardMediaController = keyguardMediaController;
        keyguardMediaController.setVisibilityChangedListener(new Function1() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda21
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return this.f$0.lambda$new$6(keyguardMediaController, (Boolean) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(View view) {
        clearNotifications(2, true ^ hasActiveClearableNotifications(1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$new$4(NotificationBlockingHelperManager notificationBlockingHelperManager, Float f, Float f2) {
        notificationBlockingHelperManager.setNotificationShadeExpanded(f.floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$5(String str, String str2) {
        if (str.equals("high_priority")) {
            this.mHighPriorityBeforeSpeedBump = "1".equals(str2);
            return;
        }
        if (str.equals("notification_dismiss_rtl")) {
            updateDismissRtlSetting("1".equals(str2));
            return;
        }
        if (str.equals("notification_history_enabled")) {
            updateFooter();
            return;
        }
        if (str.equals("system:notification_bg_alpha")) {
            this.mNotificationBackgroundAlpha = TunerService.parseInteger(str2, 255);
            updateBackgroundDimming();
        } else if (str.equals("system:notification_material_dismiss")) {
            this.mShowDimissButton = TunerService.parseIntegerSwitch(str2, false);
            updateFooter();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Unit lambda$new$6(KeyguardMediaController keyguardMediaController, Boolean bool) {
        if (bool.booleanValue()) {
            generateAddAnimation(keyguardMediaController.getView(), false);
        } else {
            generateRemoveAnimation(keyguardMediaController.getView());
        }
        requestChildrenUpdate();
        return null;
    }

    private void initializeForegroundServiceSection(ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        if (foregroundServiceDismissalFeatureController.isForegroundServiceDismissalEnabled()) {
            ForegroundServiceDungeonView foregroundServiceDungeonView = (ForegroundServiceDungeonView) this.mFgsSectionController.createView(LayoutInflater.from(((ViewGroup) this).mContext));
            this.mFgsSectionView = foregroundServiceDungeonView;
            addView(foregroundServiceDungeonView, -1);
        }
    }

    private void updateDismissRtlSetting(boolean z) {
        this.mDismissRtl = z;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) childAt).setDismissRtl(z);
            }
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflateEmptyShadeView();
        inflateFooterView();
        this.mVisualStabilityManager.setVisibilityLocationProvider(new VisibilityLocationProvider() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda7
            @Override // com.android.systemui.statusbar.notification.VisibilityLocationProvider
            public final boolean isInVisibleLocation(NotificationEntry notificationEntry) {
                return this.f$0.isInVisibleLocation(notificationEntry);
            }
        });
        if (this.mAllowLongPress) {
            final NotificationGutsManager notificationGutsManager = this.mNotificationGutsManager;
            Objects.requireNonNull(notificationGutsManager);
            setLongPressListener(new ExpandableNotificationRow.LongPressListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda8
                @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.LongPressListener
                public final boolean onLongPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                    return notificationGutsManager.openGuts(view, i, i2, menuItem);
                }
            });
        }
    }

    public float getWakeUpHeight() {
        int collapsedHeight;
        ExpandableView firstChildWithBackground = getFirstChildWithBackground();
        if (firstChildWithBackground == null) {
            return 0.0f;
        }
        if (this.mKeyguardBypassController.getBypassEnabled()) {
            collapsedHeight = firstChildWithBackground.getHeadsUpHeightWithoutHeader();
        } else {
            collapsedHeight = firstChildWithBackground.getCollapsedHeight();
        }
        return collapsedHeight;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        reinflateViews();
    }

    private void reinflateViews() {
        inflateFooterView();
        inflateEmptyShadeView();
        updateFooter();
        this.mIconColor = ((ViewGroup) this).mContext.getColor(R.color.dismiss_all_icon_color);
        this.mSectionsManager.reinflateViews(LayoutInflater.from(((ViewGroup) this).mContext));
        this.mStatusBar.updateDismissAllButton(this.mIconColor);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        updateFooter();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() throws Resources.NotFoundException {
        int dimensionPixelSize = ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(Utils.getThemeAttr(((ViewGroup) this).mContext, android.R.attr.dialogCornerRadius));
        if (this.mCornerRadius != dimensionPixelSize) {
            this.mCornerRadius = dimensionPixelSize;
            invalidate();
        }
        reinflateViews();
    }

    @VisibleForTesting
    public void updateFooter() {
        if (this.mFooterView == null) {
            return;
        }
        boolean z = this.mClearAllEnabled && hasActiveClearableNotifications(0);
        this.mStatusBar.setHasClearableNotifs(hasActiveClearableNotifications(0));
        updateFooterView((z || hasActiveNotifications()) && this.mIsCurrentUserSetup && this.mStatusBarState != 1 && !this.mRemoteInputManager.getController().isRemoteInputActive(), z, Settings.Secure.getIntForUser(((ViewGroup) this).mContext.getContentResolver(), "notification_history_enabled", 0, -2) == 1);
    }

    public boolean hasActiveClearableNotifications(int i) {
        if (this.mDynamicPrivacyController.isInLockedDownShade()) {
            return false;
        }
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                if (expandableNotificationRow.getEntry().isClearable() && matchesSelection(expandableNotificationRow, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public RemoteInputController.Delegate createDelegate() {
        return new RemoteInputController.Delegate() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.12
            @Override // com.android.systemui.statusbar.RemoteInputController.Delegate
            public void setRemoteInputActive(NotificationEntry notificationEntry, boolean z) {
                NotificationStackScrollLayout.this.mHeadsUpManager.setRemoteInputActive(notificationEntry, z);
                notificationEntry.notifyHeightChanged(true);
                NotificationStackScrollLayout.this.updateFooter();
            }

            @Override // com.android.systemui.statusbar.RemoteInputController.Delegate
            public void lockScrollTo(NotificationEntry notificationEntry) {
                NotificationStackScrollLayout.this.lockScrollTo(notificationEntry.getRow());
            }

            @Override // com.android.systemui.statusbar.RemoteInputController.Delegate
            public void requestDisallowLongPressAndDismiss() {
                NotificationStackScrollLayout.this.requestDisallowLongPress();
                NotificationStackScrollLayout.this.requestDisallowDismiss();
            }
        };
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this.mStateListener, 2);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback(this.mStateListener);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public NotificationSwipeActionHelper getSwipeActionHelper() {
        return this.mSwipeHelper;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        this.mBgColor = ((ViewGroup) this).mContext.getColor(R.color.notification_shade_background_color);
        this.mIconColor = ((ViewGroup) this).mContext.getColor(R.color.dismiss_all_icon_color);
        updateBackgroundDimming();
        this.mShelf.onUiModeChanged();
        this.mStatusBar.updateDismissAllButton(this.mIconColor);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mShouldDrawNotificationBackground) {
            if (this.mSections[0].getCurrentBounds().top < this.mSections[r1.length - 1].getCurrentBounds().bottom || this.mAmbientState.isDozing()) {
                drawBackground(canvas);
                return;
            }
        }
        if (this.mInHeadsUpPinnedMode || this.mHeadsUpAnimatingAway) {
            drawHeadsUpBackground(canvas);
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    private void drawBackground(Canvas canvas) {
        boolean z;
        boolean zIsPulseExpanding;
        int i = this.mSidePaddings;
        int width = getWidth() - this.mSidePaddings;
        int i2 = this.mSections[0].getCurrentBounds().top;
        NotificationSection[] notificationSectionArr = this.mSections;
        int i3 = notificationSectionArr[notificationSectionArr.length - 1].getCurrentBounds().bottom;
        int width2 = getWidth() / 2;
        int i4 = this.mTopPadding;
        float f = 1.0f - this.mInterpolatedHideAmount;
        float interpolation = this.mHideXInterpolator.getInterpolation((1.0f - this.mLinearHideAmount) * this.mBackgroundXFactor);
        float f2 = width2;
        int iLerp = (int) MathUtils.lerp(f2, i, interpolation);
        int iLerp2 = (int) MathUtils.lerp(f2, width, interpolation);
        float f3 = i4;
        int iLerp3 = (int) MathUtils.lerp(f3, i2, f);
        this.mBackgroundAnimationRect.set(iLerp, iLerp3, iLerp2, (int) MathUtils.lerp(f3, i3, f));
        int i5 = iLerp3 - i2;
        NotificationSection[] notificationSectionArr2 = this.mSections;
        int length = notificationSectionArr2.length;
        int i6 = 0;
        while (true) {
            if (i6 >= length) {
                z = false;
                break;
            } else {
                if (notificationSectionArr2[i6].needsBackground()) {
                    z = true;
                    break;
                }
                i6++;
            }
        }
        if (this.mKeyguardBypassController.getBypassEnabled() && onKeyguard()) {
            zIsPulseExpanding = isPulseExpanding();
        } else {
            zIsPulseExpanding = !this.mAmbientState.isDozing() || z;
        }
        if (zIsPulseExpanding) {
            drawBackgroundRects(canvas, iLerp, iLerp2, iLerp3, i5);
        }
        updateClipping();
    }

    private void drawBackgroundRects(Canvas canvas, int i, int i2, int i3, int i4) {
        int i5 = i2;
        int i6 = this.mSections[0].getCurrentBounds().bottom + i4;
        NotificationSection[] notificationSectionArr = this.mSections;
        int length = notificationSectionArr.length;
        int i7 = 1;
        int i8 = i;
        int i9 = i5;
        int i10 = i6;
        int i11 = 0;
        boolean z = true;
        int i12 = i3;
        while (i11 < length) {
            NotificationSection notificationSection = notificationSectionArr[i11];
            if (notificationSection.needsBackground()) {
                int i13 = notificationSection.getCurrentBounds().top + i4;
                int iMin = Math.min(Math.max(i, notificationSection.getCurrentBounds().left), i5);
                int iMax = Math.max(Math.min(i5, notificationSection.getCurrentBounds().right), iMin);
                if (i13 - i10 > i7 || ((i8 != iMin || i9 != iMax) && !z)) {
                    int i14 = this.mCornerRadius;
                    canvas.drawRoundRect(i8, i12, i9, i10, i14, i14, this.mBackgroundPaint);
                    i12 = i13;
                }
                i10 = notificationSection.getCurrentBounds().bottom + i4;
                i9 = iMax;
                i8 = iMin;
                z = false;
            }
            i11++;
            i5 = i2;
            i7 = 1;
        }
        int i15 = this.mCornerRadius;
        canvas.drawRoundRect(i8, i12, i9, i10, i15, i15, this.mBackgroundPaint);
    }

    private void drawHeadsUpBackground(Canvas canvas) {
        int i = this.mSidePaddings;
        int width = getWidth() - this.mSidePaddings;
        float height = getHeight();
        int childCount = getChildCount();
        float f = height;
        float fMax = 0.0f;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                if ((expandableNotificationRow.isPinned() || expandableNotificationRow.isHeadsUpAnimatingAway()) && expandableNotificationRow.getTranslation() < 0.0f && expandableNotificationRow.getProvider().shouldShowGutsOnSnapOpen()) {
                    float fMin = Math.min(f, expandableNotificationRow.getTranslationY());
                    fMax = Math.max(fMax, expandableNotificationRow.getTranslationY() + expandableNotificationRow.getActualHeight());
                    f = fMin;
                }
            }
        }
        if (f < fMax) {
            int i3 = this.mCornerRadius;
            canvas.drawRoundRect(i, f, width, fMax, i3, i3, this.mBackgroundPaint);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBackgroundDimming() {
        if (this.mShouldDrawNotificationBackground) {
            int iBlendARGB = ColorUtils.blendARGB(this.mBgColor, -1, MathUtils.smoothStep(0.4f, 1.0f, this.mLinearHideAmount));
            if (this.mCachedBackgroundColor == iBlendARGB && this.mBackgroundPaint.getAlpha() == this.mNotificationBackgroundAlpha) {
                return;
            }
            this.mCachedBackgroundColor = iBlendARGB;
            this.mBackgroundPaint.setColor(iBlendARGB);
            this.mBackgroundPaint.setAlpha(this.mNotificationBackgroundAlpha);
            invalidate();
        }
    }

    private void initView(Context context) {
        this.mScroller = new OverScroller(getContext());
        setDescendantFocusability(LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT);
        setClipChildren(false);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mSlopMultiplier = viewConfiguration.getScaledAmbiguousGestureMultiplier();
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mOverflingDistance = viewConfiguration.getScaledOverflingDistance();
        Resources resources = context.getResources();
        this.mCollapsedSize = resources.getDimensionPixelSize(R.dimen.notification_min_height);
        this.mGapHeight = resources.getDimensionPixelSize(R.dimen.notification_section_divider_height);
        this.mStackScrollAlgorithm.initView(context);
        this.mAmbientState.reload(context);
        this.mPaddingBetweenElements = Math.max(1, resources.getDimensionPixelSize(R.dimen.notification_divider_height));
        this.mIncreasedPaddingBetweenElements = resources.getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mMinTopOverScrollToEscape = resources.getDimensionPixelSize(R.dimen.min_top_overscroll_to_qs);
        this.mStatusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
        this.mBottomMargin = resources.getDimensionPixelSize(R.dimen.notification_panel_margin_bottom);
        this.mSidePaddings = resources.getDimensionPixelSize(R.dimen.notification_side_paddings);
        this.mMinInteractionHeight = resources.getDimensionPixelSize(R.dimen.notification_min_interaction_height);
        this.mCornerRadius = resources.getDimensionPixelSize(Utils.getThemeAttr(((ViewGroup) this).mContext, android.R.attr.dialogCornerRadius));
        this.mHeadsUpInset = this.mStatusBarHeight + resources.getDimensionPixelSize(R.dimen.heads_up_status_bar_padding);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyHeightChangeListener(ExpandableView expandableView) {
        notifyHeightChangeListener(expandableView, false);
    }

    private void notifyHeightChangeListener(ExpandableView expandableView, boolean z) {
        ExpandableView.OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onHeightChanged(expandableView, z);
        }
    }

    public boolean isPulseExpanding() {
        return this.mAmbientState.isPulseExpanding();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i) - (this.mSidePaddings * 2), View.MeasureSpec.getMode(i));
        int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i2), 0);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            measureChild(getChildAt(i3), iMakeMeasureSpec, iMakeMeasureSpec2);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float width = getWidth() / 2.0f;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            float measuredWidth = r8.getMeasuredWidth() / 2.0f;
            getChildAt(i5).layout((int) (width - measuredWidth), 0, (int) (measuredWidth + width), r8.getMeasuredHeight());
        }
        setMaxLayoutHeight(getHeight());
        updateContentHeight();
        clampScrollPosition();
        requestChildrenUpdate();
        updateFirstAndLastBackgroundViews();
        updateAlgorithmLayoutMinHeight();
        updateOwnTranslationZ();
    }

    private void requestAnimationOnViewResize(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mAnimationsEnabled) {
            if (this.mIsExpanded || (expandableNotificationRow != null && expandableNotificationRow.isPinned())) {
                this.mNeedViewResizeAnimation = true;
                this.mNeedsAnimation = true;
            }
        }
    }

    public void updateSpeedBumpIndex(int i, boolean z) {
        this.mAmbientState.setSpeedBumpIndex(i);
        this.mNoAmbient = z;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setChildLocationsChangedListener(NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener) {
        this.mListener = onChildLocationsChangedListener;
    }

    @Override // com.android.systemui.statusbar.notification.VisibilityLocationProvider
    public boolean isInVisibleLocation(NotificationEntry notificationEntry) {
        ExpandableNotificationRow row = notificationEntry.getRow();
        ExpandableViewState viewState = row.getViewState();
        return (viewState == null || (viewState.location & 5) == 0 || row.getVisibility() != 0) ? false : true;
    }

    private void setMaxLayoutHeight(int i) {
        this.mMaxLayoutHeight = i;
        this.mShelf.setMaxLayoutHeight(i);
        updateAlgorithmHeightAndPadding();
    }

    private void updateAlgorithmHeightAndPadding() {
        this.mAmbientState.setLayoutHeight(getLayoutHeight());
        updateAlgorithmLayoutMinHeight();
        this.mAmbientState.setTopPadding(this.mTopPadding);
    }

    private void updateAlgorithmLayoutMinHeight() {
        this.mAmbientState.setLayoutMinHeight((this.mQsExpanded || isHeadsUpTransition()) ? getLayoutMinHeight() : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateChildren() {
        updateScrollStateForAddedChildren();
        this.mAmbientState.setCurrentScrollVelocity(this.mScroller.isFinished() ? 0.0f : this.mScroller.getCurrVelocity());
        this.mAmbientState.setScrollY(this.mOwnScrollY);
        this.mStackScrollAlgorithm.resetViewStates(this.mAmbientState);
        if (!isCurrentlyAnimating() && !this.mNeedsAnimation) {
            applyCurrentState();
        } else {
            startAnimationToState();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPreDrawDuringAnimation() {
        this.mShelf.updateAppearance();
        updateClippingToTopRoundedCorner();
        if (this.mNeedsAnimation || this.mChildrenUpdateRequested) {
            return;
        }
        updateBackground();
    }

    private void updateClippingToTopRoundedCorner() {
        Float fValueOf = Float.valueOf(this.mTopPadding + this.mStackTranslation + this.mAmbientState.getExpandAnimationTopChange());
        Float fValueOf2 = Float.valueOf(fValueOf.floatValue() + this.mCornerRadius);
        boolean z = true;
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                float translationY = expandableView.getTranslationY();
                float actualHeight = expandableView.getActualHeight() + translationY;
                expandableView.setDistanceToTopRoundness((!z || !isScrolledToTop()) & (((fValueOf.floatValue() > translationY ? 1 : (fValueOf.floatValue() == translationY ? 0 : -1)) > 0 && (fValueOf.floatValue() > actualHeight ? 1 : (fValueOf.floatValue() == actualHeight ? 0 : -1)) < 0) || ((fValueOf2.floatValue() > translationY ? 1 : (fValueOf2.floatValue() == translationY ? 0 : -1)) >= 0 && (fValueOf2.floatValue() > actualHeight ? 1 : (fValueOf2.floatValue() == actualHeight ? 0 : -1)) <= 0)) ? Math.max(translationY - fValueOf.floatValue(), 0.0f) : -1.0f);
                z = false;
            }
        }
    }

    private void updateScrollStateForAddedChildren() {
        int i;
        if (this.mChildrenToAddAnimated.isEmpty()) {
            return;
        }
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i2);
            if (this.mChildrenToAddAnimated.contains(expandableView)) {
                int positionInLinearLayout = getPositionInLinearLayout(expandableView);
                float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
                if (increasedPaddingAmount == 1.0f) {
                    i = this.mIncreasedPaddingBetweenElements;
                } else {
                    i = increasedPaddingAmount == -1.0f ? 0 : this.mPaddingBetweenElements;
                }
                int intrinsicHeight = getIntrinsicHeight(expandableView) + i;
                int i3 = this.mOwnScrollY;
                if (positionInLinearLayout < i3) {
                    setOwnScrollY(i3 + intrinsicHeight);
                }
            }
        }
        clampScrollPosition();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateForcedScroll() {
        View view = this.mForcedScroll;
        if (view != null && (!view.hasFocus() || !this.mForcedScroll.isAttachedToWindow())) {
            this.mForcedScroll = null;
        }
        View view2 = this.mForcedScroll;
        if (view2 != null) {
            ExpandableView expandableView = (ExpandableView) view2;
            int positionInLinearLayout = getPositionInLinearLayout(expandableView);
            int iTargetScrollForView = targetScrollForView(expandableView, positionInLinearLayout);
            int intrinsicHeight = positionInLinearLayout + expandableView.getIntrinsicHeight();
            int iMax = Math.max(0, Math.min(iTargetScrollForView, getScrollRange()));
            int i = this.mOwnScrollY;
            if (i < iMax || intrinsicHeight < i) {
                setOwnScrollY(iMax);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestChildrenUpdate() {
        if (this.mChildrenUpdateRequested) {
            return;
        }
        getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
        this.mChildrenUpdateRequested = true;
        invalidate();
    }

    public int getVisibleNotificationCount() {
        int i = 0;
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View childAt = getChildAt(i2);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                i++;
            }
        }
        return i;
    }

    public boolean isVisibleNotification() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                return true;
            }
        }
        return this.mKeyguardMediaController.getView().getVisibility() != 8;
    }

    private boolean isCurrentlyAnimating() {
        return this.mStateAnimator.isRunning();
    }

    private void clampScrollPosition() {
        int scrollRange = getScrollRange();
        if (scrollRange < this.mOwnScrollY) {
            setOwnScrollY(scrollRange);
        }
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    private void setTopPadding(int i, boolean z) {
        if (this.mTopPadding != i) {
            this.mTopPadding = i;
            updateAlgorithmHeightAndPadding();
            updateContentHeight();
            if (z && this.mAnimationsEnabled && this.mIsExpanded) {
                this.mTopPaddingNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
            notifyHeightChangeListener(null, z);
        }
    }

    public void setExpandedHeight(float f) {
        float expandTranslationStart;
        int intrinsicHeight;
        this.mExpandedHeight = f;
        float fLerp = 0.0f;
        setIsExpanded(f > 0.0f);
        float minExpansionHeight = getMinExpansionHeight();
        if (f < minExpansionHeight) {
            Rect rect = this.mClipRect;
            rect.left = 0;
            rect.right = getWidth();
            Rect rect2 = this.mClipRect;
            rect2.top = 0;
            rect2.bottom = (int) f;
            setRequestedClipBounds(rect2);
            f = minExpansionHeight;
        } else {
            setRequestedClipBounds(null);
        }
        float appearEndPosition = getAppearEndPosition();
        float appearStartPosition = getAppearStartPosition();
        float fCalculateAppearFraction = 1.0f;
        boolean z = f < appearEndPosition;
        this.mAmbientState.setAppearing(z);
        if (!z) {
            if (this.mShouldShowShelfOnly) {
                intrinsicHeight = this.mTopPadding + this.mShelf.getIntrinsicHeight();
            } else if (this.mQsExpanded) {
                int i = (this.mContentHeight - this.mTopPadding) + this.mIntrinsicPadding;
                int intrinsicHeight2 = this.mMaxTopPadding + this.mShelf.getIntrinsicHeight();
                if (i <= intrinsicHeight2) {
                    intrinsicHeight = intrinsicHeight2;
                } else {
                    f = NotificationUtils.interpolate(i, intrinsicHeight2, this.mQsExpansionFraction);
                    intrinsicHeight = (int) f;
                }
            } else {
                intrinsicHeight = (int) f;
            }
        } else {
            fCalculateAppearFraction = calculateAppearFraction(f);
            if (fCalculateAppearFraction >= 0.0f) {
                expandTranslationStart = NotificationUtils.interpolate(getExpandTranslationStart(), 0.0f, fCalculateAppearFraction);
            } else {
                expandTranslationStart = (f - appearStartPosition) + getExpandTranslationStart();
            }
            intrinsicHeight = (int) (f - expandTranslationStart);
            fLerp = isHeadsUpTransition() ? MathUtils.lerp(this.mHeadsUpInset - this.mTopPadding, 0.0f, fCalculateAppearFraction) : expandTranslationStart;
        }
        this.mAmbientState.setAppearFraction(fCalculateAppearFraction);
        if (intrinsicHeight != this.mCurrentStackHeight) {
            this.mCurrentStackHeight = intrinsicHeight;
            updateAlgorithmHeightAndPadding();
            requestChildrenUpdate();
        }
        setStackTranslation(fLerp);
        notifyAppearChangedListeners();
    }

    private void notifyAppearChangedListeners() {
        float fSaturate;
        float pulseHeight;
        if (this.mKeyguardBypassController.getBypassEnabled() && onKeyguard()) {
            fSaturate = calculateAppearFractionBypass();
            pulseHeight = getPulseHeight();
        } else {
            fSaturate = MathUtils.saturate(calculateAppearFraction(this.mExpandedHeight));
            pulseHeight = this.mExpandedHeight;
        }
        if (fSaturate == this.mLastSentAppear && pulseHeight == this.mLastSentExpandedHeight) {
            return;
        }
        this.mLastSentAppear = fSaturate;
        this.mLastSentExpandedHeight = pulseHeight;
        for (int i = 0; i < this.mExpandedHeightListeners.size(); i++) {
            this.mExpandedHeightListeners.get(i).accept(Float.valueOf(pulseHeight), Float.valueOf(fSaturate));
        }
    }

    private void setRequestedClipBounds(Rect rect) {
        this.mRequestedClipBounds = rect;
        updateClipping();
    }

    public int getIntrinsicContentHeight() {
        return this.mIntrinsicContentHeight;
    }

    public void updateClipping() {
        boolean z = true;
        boolean z2 = (this.mRequestedClipBounds == null || this.mInHeadsUpPinnedMode || this.mHeadsUpAnimatingAway) ? false : true;
        if (this.mIsClipped != z2) {
            this.mIsClipped = z2;
        }
        if (this.mAmbientState.isHiddenAtAll()) {
            invalidateOutline();
            if (isFullyHidden()) {
                setClipBounds(null);
            }
        } else {
            if (z2) {
                setClipBounds(this.mRequestedClipBounds);
            } else {
                setClipBounds(null);
            }
            z = false;
        }
        setClipToOutline(z);
    }

    private float getExpandTranslationStart() {
        return ((-this.mTopPadding) + getMinExpansionHeight()) - this.mShelf.getIntrinsicHeight();
    }

    private float getAppearStartPosition() {
        if (isHeadsUpTransition()) {
            return this.mHeadsUpInset + getFirstVisibleSection().getFirstVisibleChild().getPinnedHeadsUpHeight();
        }
        return getMinExpansionHeight();
    }

    private int getTopHeadsUpPinnedHeight() {
        NotificationEntry groupSummary;
        NotificationEntry topEntry = this.mHeadsUpManager.getTopEntry();
        if (topEntry == null) {
            return 0;
        }
        ExpandableNotificationRow row = topEntry.getRow();
        if (row.isChildInGroup() && (groupSummary = this.mGroupManager.getGroupSummary(row.getEntry().getSbn())) != null) {
            row = groupSummary.getRow();
        }
        return row.getPinnedHeadsUpHeight();
    }

    private float getAppearEndPosition() {
        int topHeadsUpPinnedHeight;
        int visibleNotificationCount = getVisibleNotificationCount();
        int height = 0;
        if (this.mEmptyShadeView.getVisibility() == 8 && visibleNotificationCount > 0) {
            if (isHeadsUpTransition() || (this.mHeadsUpManager.hasPinnedHeadsUp() && !this.mAmbientState.isDozing())) {
                if (this.mShelf.getVisibility() != 8 && visibleNotificationCount > 1) {
                    height = 0 + this.mShelf.getIntrinsicHeight() + this.mPaddingBetweenElements;
                }
                topHeadsUpPinnedHeight = getTopHeadsUpPinnedHeight() + getPositionInLinearLayout(this.mAmbientState.getTrackedHeadsUpRow());
            } else if (this.mShelf.getVisibility() != 8) {
                topHeadsUpPinnedHeight = this.mShelf.getIntrinsicHeight();
            }
            height += topHeadsUpPinnedHeight;
        } else {
            height = this.mEmptyShadeView.getHeight();
        }
        return height + (onKeyguard() ? this.mTopPadding : this.mIntrinsicPadding);
    }

    private boolean isHeadsUpTransition() {
        return this.mAmbientState.getTrackedHeadsUpRow() != null;
    }

    public float calculateAppearFraction(float f) {
        float appearEndPosition = getAppearEndPosition();
        float appearStartPosition = getAppearStartPosition();
        return (f - appearStartPosition) / (appearEndPosition - appearStartPosition);
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    private void setStackTranslation(float f) {
        if (f != this.mStackTranslation) {
            this.mStackTranslation = f;
            this.mAmbientState.setStackTranslation(f);
            requestChildrenUpdate();
        }
    }

    private int getLayoutHeight() {
        return Math.min(this.mMaxLayoutHeight, this.mCurrentStackHeight);
    }

    public void setQsContainer(ViewGroup viewGroup) {
        this.mQsContainer = viewGroup;
    }

    public static boolean isPinnedHeadsUp(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        return expandableNotificationRow.isHeadsUp() && expandableNotificationRow.isPinned();
    }

    private boolean isHeadsUp(View view) {
        if (view instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) view).isHeadsUp();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ExpandableView getChildAtPosition(float f, float f2) {
        return getChildAtPosition(f, f2, true, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ExpandableView getChildAtPosition(float f, float f2, boolean z, boolean z2) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() == 0 && (!z2 || !(expandableView instanceof StackScrollerDecorView))) {
                float translationY = expandableView.getTranslationY();
                float clipTopAmount = expandableView.getClipTopAmount() + translationY;
                float actualHeight = (expandableView.getActualHeight() + translationY) - expandableView.getClipBottomAmount();
                int width = getWidth();
                if ((actualHeight - clipTopAmount >= this.mMinInteractionHeight || !z) && f2 >= clipTopAmount && f2 <= actualHeight && f >= 0 && f <= width) {
                    if (!(expandableView instanceof ExpandableNotificationRow)) {
                        return expandableView;
                    }
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                    NotificationEntry entry = expandableNotificationRow.getEntry();
                    if (this.mIsExpanded || !expandableNotificationRow.isHeadsUp() || !expandableNotificationRow.isPinned() || this.mHeadsUpManager.getTopEntry().getRow() == expandableNotificationRow || this.mGroupManager.getGroupSummary(this.mHeadsUpManager.getTopEntry().getSbn()) == entry) {
                        return expandableNotificationRow.getViewAtPosition(f2 - translationY);
                    }
                }
            }
        }
        return null;
    }

    public ExpandableView getChildAtRawPosition(float f, float f2) {
        getLocationOnScreen(this.mTempInt2);
        int[] iArr = this.mTempInt2;
        return getChildAtPosition(f - iArr[0], f2 - iArr[1]);
    }

    public void setScrollingEnabled(boolean z) {
        this.mScrollingEnabled = z;
    }

    public void lockScrollTo(View view) {
        if (this.mForcedScroll == view) {
            return;
        }
        this.mForcedScroll = view;
        scrollTo(view);
    }

    public boolean scrollTo(View view) {
        ExpandableView expandableView = (ExpandableView) view;
        int positionInLinearLayout = getPositionInLinearLayout(view);
        int iTargetScrollForView = targetScrollForView(expandableView, positionInLinearLayout);
        int intrinsicHeight = positionInLinearLayout + expandableView.getIntrinsicHeight();
        int i = this.mOwnScrollY;
        if (i >= iTargetScrollForView && intrinsicHeight >= i) {
            return false;
        }
        this.mScroller.startScroll(((ViewGroup) this).mScrollX, i, 0, iTargetScrollForView - i);
        this.mDontReportNextOverScroll = true;
        lambda$new$1();
        return true;
    }

    private int targetScrollForView(ExpandableView expandableView, int i) {
        return (((i + expandableView.getIntrinsicHeight()) + getImeInset()) - getHeight()) + ((isExpanded() || !isPinnedHeadsUp(expandableView)) ? getTopPadding() : this.mHeadsUpInset);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mBottomInset = windowInsets.getSystemWindowInsetBottom();
        this.mWaterfallTopInset = 0;
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if (displayCutout != null) {
            this.mWaterfallTopInset = displayCutout.getWaterfallInsets().top;
        }
        if (this.mOwnScrollY > getScrollRange()) {
            removeCallbacks(this.mReclamp);
            postDelayed(this.mReclamp, 50L);
        } else {
            View view = this.mForcedScroll;
            if (view != null) {
                scrollTo(view);
            }
        }
        return windowInsets;
    }

    public void setExpandingEnabled(boolean z) {
        this.mExpandHelper.setEnabled(z);
    }

    private boolean isScrollingEnabled() {
        return this.mScrollingEnabled;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onKeyguard() {
        return this.mStatusBarState == 1;
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mStatusBarHeight = getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        this.mSwipeHelper.setDensityScale(getResources().getDisplayMetrics().density);
        this.mSwipeHelper.setPagingTouchSlop(ViewConfiguration.get(getContext()).getScaledPagingTouchSlop());
        initView(getContext());
    }

    public void dismissViewAnimated(View view, Runnable runnable, int i, long j) {
        this.mSwipeHelper.dismissChild(view, 0.0f, runnable, i, true, j, true);
    }

    private void snapViewIfNeeded(NotificationEntry notificationEntry) {
        ExpandableNotificationRow row = notificationEntry.getRow();
        boolean z = this.mIsExpanded || isPinnedHeadsUp(row);
        if (row.getProvider() != null) {
            this.mSwipeHelper.snapChildIfNeeded(row, z, row.getProvider().isMenuVisible() ? row.getTranslation() : 0.0f);
        }
    }

    private float overScrollUp(int i, int i2) {
        int iMax = Math.max(i, 0);
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        float f = currentOverScrollAmount - iMax;
        if (currentOverScrollAmount > 0.0f) {
            setOverScrollAmount(f, true, false);
        }
        float f2 = f < 0.0f ? -f : 0.0f;
        float f3 = this.mOwnScrollY + f2;
        float f4 = i2;
        if (f3 <= f4) {
            return f2;
        }
        if (!this.mExpandedInThisMotion) {
            setOverScrolledPixels((getCurrentOverScrolledPixels(false) + f3) - f4, false, false);
        }
        setOwnScrollY(i2);
        return 0.0f;
    }

    private float overScrollDown(int i) {
        int iMin = Math.min(i, 0);
        float currentOverScrollAmount = getCurrentOverScrollAmount(false);
        float f = iMin + currentOverScrollAmount;
        if (currentOverScrollAmount > 0.0f) {
            setOverScrollAmount(f, false, false);
        }
        if (f >= 0.0f) {
            f = 0.0f;
        }
        float f2 = this.mOwnScrollY + f;
        if (f2 >= 0.0f) {
            return f;
        }
        setOverScrolledPixels(getCurrentOverScrolledPixels(true) - f2, true, false);
        setOwnScrollY(0);
        return 0.0f;
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    public void setFinishScrollingCallback(Runnable runnable) {
        this.mFinishScrollingCallback = runnable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: animateScroll, reason: merged with bridge method [inline-methods] */
    public void lambda$new$1() {
        if (this.mScroller.computeScrollOffset()) {
            int i = this.mOwnScrollY;
            int currY = this.mScroller.getCurrY();
            if (i != currY) {
                int scrollRange = getScrollRange();
                if ((currY < 0 && i >= 0) || (currY > scrollRange && i <= scrollRange)) {
                    setMaxOverScrollFromCurrentVelocity();
                }
                if (this.mDontClampNextScroll) {
                    scrollRange = Math.max(scrollRange, i);
                }
                customOverScrollBy(currY - i, i, scrollRange, (int) this.mMaxOverScroll);
            }
            postOnAnimation(this.mReflingAndAnimateScroll);
            return;
        }
        this.mDontClampNextScroll = false;
        Runnable runnable = this.mFinishScrollingCallback;
        if (runnable != null) {
            runnable.run();
        }
    }

    private void setMaxOverScrollFromCurrentVelocity() {
        float currVelocity = this.mScroller.getCurrVelocity();
        if (currVelocity >= this.mMinimumVelocity) {
            this.mMaxOverScroll = (Math.abs(currVelocity) / 1000.0f) * this.mOverflingDistance;
        }
    }

    private void customOverScrollBy(int i, int i2, int i3, int i4) {
        int i5 = i2 + i;
        int i6 = -i4;
        int i7 = i4 + i3;
        boolean z = true;
        if (i5 > i7) {
            i5 = i7;
        } else if (i5 < i6) {
            i5 = i6;
        } else {
            z = false;
        }
        onCustomOverScrolled(i5, z);
    }

    public void setOverScrolledPixels(float f, boolean z, boolean z2) {
        setOverScrollAmount(f * getRubberBandFactor(z), z, z2, true);
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2) {
        setOverScrollAmount(f, z, z2, true);
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2, boolean z3) {
        setOverScrollAmount(f, z, z2, z3, isRubberbanded(z));
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2, boolean z3, boolean z4) {
        if (z3) {
            this.mStateAnimator.cancelOverScrollAnimators(z);
        }
        setOverScrollAmountInternal(f, z, z2, z4);
    }

    private void setOverScrollAmountInternal(float f, boolean z, boolean z2, boolean z3) {
        float fMax = Math.max(0.0f, f);
        if (z2) {
            this.mStateAnimator.animateOverScrollToAmount(fMax, z, z3);
            return;
        }
        setOverScrolledPixels(fMax / getRubberBandFactor(z), z);
        this.mAmbientState.setOverScrollAmount(fMax, z);
        if (z) {
            notifyOverscrollTopListener(fMax, z3);
        }
        requestChildrenUpdate();
    }

    private void notifyOverscrollTopListener(float f, boolean z) {
        this.mExpandHelper.onlyObserveMovements(f > 1.0f);
        if (this.mDontReportNextOverScroll) {
            this.mDontReportNextOverScroll = false;
            return;
        }
        OnOverscrollTopChangedListener onOverscrollTopChangedListener = this.mOverscrollTopChangedListener;
        if (onOverscrollTopChangedListener != null) {
            onOverscrollTopChangedListener.onOverscrollTopChanged(f, z);
        }
    }

    public void setOverscrollTopChangedListener(OnOverscrollTopChangedListener onOverscrollTopChangedListener) {
        this.mOverscrollTopChangedListener = onOverscrollTopChangedListener;
    }

    public float getCurrentOverScrollAmount(boolean z) {
        return this.mAmbientState.getOverScrollAmount(z);
    }

    public float getCurrentOverScrolledPixels(boolean z) {
        return z ? this.mOverScrolledTopPixels : this.mOverScrolledBottomPixels;
    }

    private void setOverScrolledPixels(float f, boolean z) {
        if (z) {
            this.mOverScrolledTopPixels = f;
        } else {
            this.mOverScrolledBottomPixels = f;
        }
    }

    private void onCustomOverScrolled(int i, boolean z) {
        if (!this.mScroller.isFinished()) {
            setOwnScrollY(i);
            if (z) {
                springBack();
                return;
            }
            float currentOverScrollAmount = getCurrentOverScrollAmount(true);
            if (this.mOwnScrollY < 0) {
                notifyOverscrollTopListener(-r0, isRubberbanded(true));
                return;
            } else {
                notifyOverscrollTopListener(currentOverScrollAmount, isRubberbanded(true));
                return;
            }
        }
        setOwnScrollY(i);
    }

    private void springBack() {
        float f;
        boolean z;
        int scrollRange = getScrollRange();
        int i = this.mOwnScrollY;
        boolean z2 = i <= 0;
        boolean z3 = i >= scrollRange;
        if (z2 || z3) {
            if (z2) {
                f = -i;
                setOwnScrollY(0);
                this.mDontReportNextOverScroll = true;
                z = true;
            } else {
                setOwnScrollY(scrollRange);
                f = i - scrollRange;
                z = false;
            }
            setOverScrollAmount(f, z, false);
            setOverScrollAmount(0.0f, z, true);
            this.mScroller.forceFinished(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getScrollRange() {
        int topHeadsUpPinnedHeight = this.mContentHeight;
        if (!isExpanded() && this.mHeadsUpManager.hasPinnedHeadsUp()) {
            topHeadsUpPinnedHeight = this.mHeadsUpInset + getTopHeadsUpPinnedHeight();
        }
        int iMax = Math.max(0, topHeadsUpPinnedHeight - this.mMaxLayoutHeight);
        int imeInset = getImeInset();
        return iMax + Math.min(imeInset, Math.max(0, topHeadsUpPinnedHeight - (getHeight() - imeInset)));
    }

    private int getImeInset() {
        return Math.max(0, this.mBottomInset - (getRootView().getHeight() - getHeight()));
    }

    public ExpandableView getFirstChildNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && childAt != this.mShelf) {
                return (ExpandableView) childAt;
            }
        }
        return null;
    }

    private View getFirstChildBelowTranlsationY(float f, boolean z) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8) {
                float translationY = childAt.getTranslationY();
                if (translationY >= f) {
                    return childAt;
                }
                if (!z && (childAt instanceof ExpandableNotificationRow)) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                    if (expandableNotificationRow.isSummaryWithChildren() && expandableNotificationRow.areChildrenExpanded()) {
                        List<ExpandableNotificationRow> attachedChildren = expandableNotificationRow.getAttachedChildren();
                        for (int i2 = 0; i2 < attachedChildren.size(); i2++) {
                            ExpandableNotificationRow expandableNotificationRow2 = attachedChildren.get(i2);
                            if (expandableNotificationRow2.getTranslationY() + translationY >= f) {
                                return expandableNotificationRow2;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public ExpandableView getLastChildNotGone() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (childAt.getVisibility() != 8 && childAt != this.mShelf) {
                return (ExpandableView) childAt;
            }
        }
        return null;
    }

    public int getNotGoneChildCount() {
        int childCount = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i2);
            if (expandableView.getVisibility() != 8 && !expandableView.willBeGone() && expandableView != this.mShelf) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateContentHeight() {
        float intrinsicHeight;
        float fInterpolate;
        float fInterpolate2;
        float f = this.mPaddingBetweenElements;
        int i = this.mMaxDisplayedNotifications;
        ExpandableView expandableView = null;
        float f2 = 0.0f;
        int i2 = 0;
        int iCalculateGapHeight = 0;
        boolean z = false;
        for (int i3 = 0; i3 < getChildCount(); i3++) {
            ExpandableView expandableView2 = (ExpandableView) getChildAt(i3);
            boolean z2 = expandableView2 == this.mFooterView && onKeyguard();
            if (expandableView2.getVisibility() != 8 && !expandableView2.hasNoContentHeight() && !z2) {
                if (i != -1 && i2 >= i) {
                    intrinsicHeight = this.mShelf.getIntrinsicHeight();
                    z = true;
                } else {
                    intrinsicHeight = expandableView2.getIntrinsicHeight();
                }
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                if (increasedPaddingAmount >= 0.0f) {
                    fInterpolate2 = (int) NotificationUtils.interpolate(f, this.mIncreasedPaddingBetweenElements, increasedPaddingAmount);
                    fInterpolate = (int) NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, increasedPaddingAmount);
                } else {
                    int iInterpolate = (int) NotificationUtils.interpolate(0.0f, this.mPaddingBetweenElements, 1.0f + increasedPaddingAmount);
                    float fInterpolate3 = f2 > 0.0f ? (int) NotificationUtils.interpolate(iInterpolate, this.mIncreasedPaddingBetweenElements, f2) : iInterpolate;
                    fInterpolate = iInterpolate;
                    fInterpolate2 = fInterpolate3;
                }
                if (iCalculateGapHeight != 0) {
                    iCalculateGapHeight = (int) (iCalculateGapHeight + fInterpolate2);
                }
                iCalculateGapHeight = (int) (((int) (iCalculateGapHeight + calculateGapHeight(expandableView, expandableView2, i2))) + intrinsicHeight);
                i2++;
                if (z) {
                    break;
                }
                f = fInterpolate;
                expandableView = expandableView2;
                f2 = increasedPaddingAmount;
            }
        }
        this.mIntrinsicContentHeight = iCalculateGapHeight;
        this.mContentHeight = iCalculateGapHeight + Math.max(this.mIntrinsicPadding, this.mTopPadding) + this.mBottomMargin;
        updateScrollability();
        clampScrollPosition();
        this.mAmbientState.setLayoutMaxHeight(this.mContentHeight);
    }

    public float calculateGapHeight(ExpandableView expandableView, ExpandableView expandableView2, int i) {
        return this.mStackScrollAlgorithm.getGapHeightForChild(this.mSectionsManager, this.mAmbientState.getAnchorViewIndex(), i, expandableView2, expandableView);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public boolean hasPulsingNotifications() {
        return this.mPulsing;
    }

    private void updateScrollability() {
        boolean z = !this.mQsExpanded && getScrollRange() > 0;
        if (z != this.mScrollable) {
            this.mScrollable = z;
            setFocusable(z);
            updateForwardAndBackwardScrollability();
        }
    }

    private void updateForwardAndBackwardScrollability() {
        boolean z = true;
        boolean z2 = this.mScrollable && !isScrolledToBottom();
        boolean z3 = this.mScrollable && !isScrolledToTop();
        if (z2 == this.mForwardScrollable && z3 == this.mBackwardScrollable) {
            z = false;
        }
        this.mForwardScrollable = z2;
        this.mBackwardScrollable = z3;
        if (z) {
            sendAccessibilityEvent(LineageHardwareManager.FEATURE_TOUCH_HOVERING);
        }
    }

    private void updateBackground() {
        if (this.mShouldDrawNotificationBackground) {
            updateBackgroundBounds();
            if (didSectionBoundsChange()) {
                boolean z = this.mAnimateNextSectionBoundsChange || this.mAnimateNextBackgroundTop || this.mAnimateNextBackgroundBottom || areSectionBoundsAnimating();
                if (!isExpanded()) {
                    abortBackgroundAnimators();
                    z = false;
                }
                if (z) {
                    startBackgroundAnimation();
                } else {
                    for (NotificationSection notificationSection : this.mSections) {
                        notificationSection.resetCurrentBounds();
                    }
                    invalidate();
                }
            } else {
                abortBackgroundAnimators();
            }
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
            this.mAnimateNextSectionBoundsChange = false;
        }
    }

    private void abortBackgroundAnimators() {
        for (NotificationSection notificationSection : this.mSections) {
            notificationSection.cancelAnimators();
        }
    }

    private boolean didSectionBoundsChange() {
        for (NotificationSection notificationSection : this.mSections) {
            if (notificationSection.didBoundsChange()) {
                return true;
            }
        }
        return false;
    }

    private boolean areSectionBoundsAnimating() {
        for (NotificationSection notificationSection : this.mSections) {
            if (notificationSection.areBoundsAnimating()) {
                return true;
            }
        }
        return false;
    }

    private void startBackgroundAnimation() {
        boolean z;
        boolean z2;
        NotificationSection firstVisibleSection = getFirstVisibleSection();
        NotificationSection lastVisibleSection = getLastVisibleSection();
        for (NotificationSection notificationSection : this.mSections) {
            if (notificationSection == firstVisibleSection) {
                z = this.mAnimateNextBackgroundTop;
            } else {
                z = this.mAnimateNextSectionBoundsChange;
            }
            if (notificationSection == lastVisibleSection) {
                z2 = this.mAnimateNextBackgroundBottom;
            } else {
                z2 = this.mAnimateNextSectionBoundsChange;
            }
            notificationSection.startBackgroundAnimation(z, z2);
        }
    }

    private void updateBackgroundBounds() {
        int iUpdateBounds;
        int i = this.mSidePaddings;
        int width = getWidth() - this.mSidePaddings;
        for (NotificationSection notificationSection : this.mSections) {
            notificationSection.getBounds().left = i;
            notificationSection.getBounds().right = width;
        }
        if (!this.mIsExpanded) {
            for (NotificationSection notificationSection2 : this.mSections) {
                notificationSection2.getBounds().top = 0;
                notificationSection2.getBounds().bottom = 0;
            }
            return;
        }
        NotificationSection lastVisibleSection = getLastVisibleSection();
        boolean z = true;
        boolean z2 = this.mStatusBarState == 1;
        if (!z2) {
            iUpdateBounds = (int) (this.mTopPadding + this.mStackTranslation);
        } else if (lastVisibleSection == null) {
            iUpdateBounds = this.mTopPadding;
        } else {
            NotificationSection firstVisibleSection = getFirstVisibleSection();
            firstVisibleSection.updateBounds(0, 0, false);
            iUpdateBounds = firstVisibleSection.getBounds().top;
        }
        if (this.mHeadsUpManager.getAllEntries().count() > 1 || (!this.mAmbientState.isDozing() && (!this.mKeyguardBypassController.getBypassEnabled() || !z2))) {
            z = false;
        }
        NotificationSection[] notificationSectionArr = this.mSections;
        int length = notificationSectionArr.length;
        int i2 = 0;
        while (i2 < length) {
            NotificationSection notificationSection3 = notificationSectionArr[i2];
            iUpdateBounds = notificationSection3.updateBounds(iUpdateBounds, notificationSection3 == lastVisibleSection ? (int) (ViewState.getFinalTranslationY(this.mShelf) + this.mShelf.getIntrinsicHeight()) : iUpdateBounds, z);
            i2++;
            z = false;
        }
    }

    private NotificationSection getFirstVisibleSection() {
        for (NotificationSection notificationSection : this.mSections) {
            if (notificationSection.getFirstVisibleChild() != null) {
                return notificationSection;
            }
        }
        return null;
    }

    private NotificationSection getLastVisibleSection() {
        for (int length = this.mSections.length - 1; length >= 0; length--) {
            NotificationSection notificationSection = this.mSections[length];
            if (notificationSection.getLastVisibleChild() != null) {
                return notificationSection;
            }
        }
        return null;
    }

    private ExpandableView getLastChildWithBackground() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) getChildAt(childCount);
            if (expandableView.getVisibility() != 8 && !(expandableView instanceof StackScrollerDecorView) && expandableView != this.mShelf) {
                return expandableView;
            }
        }
        return null;
    }

    private ExpandableView getFirstChildWithBackground() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8 && !(expandableView instanceof StackScrollerDecorView) && expandableView != this.mShelf) {
                return expandableView;
            }
        }
        return null;
    }

    private List<ExpandableView> getChildrenWithBackground() {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8 && !(expandableView instanceof StackScrollerDecorView) && expandableView != this.mShelf) {
                arrayList.add(expandableView);
            }
        }
        return arrayList;
    }

    protected void fling(int i) {
        if (getChildCount() > 0) {
            float currentOverScrollAmount = getCurrentOverScrollAmount(true);
            float currentOverScrollAmount2 = getCurrentOverScrollAmount(false);
            if (i < 0 && currentOverScrollAmount > 0.0f) {
                setOwnScrollY(this.mOwnScrollY - ((int) currentOverScrollAmount));
                this.mDontReportNextOverScroll = true;
                setOverScrollAmount(0.0f, true, false);
                this.mMaxOverScroll = ((Math.abs(i) / 1000.0f) * getRubberBandFactor(true) * this.mOverflingDistance) + currentOverScrollAmount;
            } else if (i > 0 && currentOverScrollAmount2 > 0.0f) {
                setOwnScrollY((int) (this.mOwnScrollY + currentOverScrollAmount2));
                setOverScrollAmount(0.0f, false, false);
                this.mMaxOverScroll = ((Math.abs(i) / 1000.0f) * getRubberBandFactor(false) * this.mOverflingDistance) + currentOverScrollAmount2;
            } else {
                this.mMaxOverScroll = 0.0f;
            }
            int iMax = Math.max(0, getScrollRange());
            if (this.mExpandedInThisMotion) {
                iMax = Math.min(iMax, this.mMaxScrollAfterExpand);
            }
            int i2 = iMax;
            OverScroller overScroller = this.mScroller;
            int i3 = ((ViewGroup) this).mScrollX;
            int i4 = this.mOwnScrollY;
            overScroller.fling(i3, i4, 1, i, 0, 0, 0, i2, 0, (!this.mExpandedInThisMotion || i4 < 0) ? 1073741823 : 0);
            lambda$new$1();
        }
    }

    private boolean shouldOverScrollFling(int i) {
        return this.mScrolledToTopOnFirstDown && !this.mExpandedInThisMotion && getCurrentOverScrollAmount(true) > this.mMinTopOverScrollToEscape && i > 0;
    }

    public void updateTopPadding(float f, boolean z) {
        int i = (int) f;
        if (getLayoutMinHeight() + i > getHeight()) {
            this.mTopPaddingOverflow = r0 - getHeight();
        } else {
            this.mTopPaddingOverflow = 0.0f;
        }
        setTopPadding(i, z && !this.mKeyguardBypassController.getBypassEnabled());
        setExpandedHeight(this.mExpandedHeight);
    }

    public void setMaxTopPadding(int i) {
        this.mMaxTopPadding = i;
    }

    public int getLayoutMinHeight() {
        if (isHeadsUpTransition()) {
            if (this.mAmbientState.getTrackedHeadsUpRow().isAboveShelf()) {
                return getTopHeadsUpPinnedHeight() + ((int) MathUtils.lerp(0.0f, getPositionInLinearLayout(r0), this.mAmbientState.getAppearFraction()));
            }
            return getTopHeadsUpPinnedHeight();
        }
        if (this.mShelf.getVisibility() == 8) {
            return 0;
        }
        return this.mShelf.getIntrinsicHeight();
    }

    public float getTopPaddingOverflow() {
        return this.mTopPaddingOverflow;
    }

    public int getPeekHeight() {
        int collapsedHeight;
        ExpandableView firstChildNotGone = getFirstChildNotGone();
        if (firstChildNotGone != null) {
            collapsedHeight = firstChildNotGone.getCollapsedHeight();
        } else {
            collapsedHeight = this.mCollapsedSize;
        }
        int intrinsicHeight = 0;
        if (getLastVisibleSection() != null && this.mShelf.getVisibility() != 8) {
            intrinsicHeight = this.mShelf.getIntrinsicHeight();
        }
        return this.mIntrinsicPadding + collapsedHeight + intrinsicHeight;
    }

    private float getRubberBandFactor(boolean z) {
        if (!z) {
            return 0.35f;
        }
        if (this.mExpandedInThisMotion) {
            return 0.15f;
        }
        if (this.mIsExpansionChanging || this.mPanelTracking) {
            return 0.21f;
        }
        return this.mScrolledToTopOnFirstDown ? 1.0f : 0.35f;
    }

    private boolean isRubberbanded(boolean z) {
        return !z || this.mExpandedInThisMotion || this.mIsExpansionChanging || this.mPanelTracking || !this.mScrolledToTopOnFirstDown;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void setChildTransferInProgress(boolean z) {
        Assert.isMainThread();
        this.mChildTransferInProgress = z;
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (this.mChildTransferInProgress) {
            return;
        }
        onViewRemovedInternal((ExpandableView) view, this);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void cleanUpViewStateForEntry(NotificationEntry notificationEntry) {
        if (notificationEntry.getRow() == this.mSwipeHelper.getTranslatingParentView()) {
            this.mSwipeHelper.clearTranslatingParentView();
        }
    }

    private void onViewRemovedInternal(ExpandableView expandableView, ViewGroup viewGroup) {
        if (this.mChangePositionInProgress) {
            return;
        }
        expandableView.setOnHeightChangedListener(null);
        updateScrollStateForRemovedChild(expandableView);
        if (generateRemoveAnimation(expandableView)) {
            if (!this.mSwipedOutViews.contains(expandableView) || Math.abs(expandableView.getTranslation()) != expandableView.getWidth()) {
                viewGroup.addTransientView(expandableView, 0);
                expandableView.setTransientContainer(viewGroup);
            }
        } else {
            this.mSwipedOutViews.remove(expandableView);
        }
        updateAnimationState(false, expandableView);
        focusNextViewIfFocused(expandableView);
    }

    private void focusNextViewIfFocused(View view) {
        float translationY;
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.shouldRefocusOnDismiss()) {
                View childAfterViewWhenDismissed = expandableNotificationRow.getChildAfterViewWhenDismissed();
                if (childAfterViewWhenDismissed == null) {
                    View groupParentWhenDismissed = expandableNotificationRow.getGroupParentWhenDismissed();
                    if (groupParentWhenDismissed != null) {
                        translationY = groupParentWhenDismissed.getTranslationY();
                    } else {
                        translationY = view.getTranslationY();
                    }
                    childAfterViewWhenDismissed = getFirstChildBelowTranlsationY(translationY, true);
                }
                if (childAfterViewWhenDismissed != null) {
                    childAfterViewWhenDismissed.requestAccessibilityFocus();
                }
            }
        }
    }

    private boolean isChildInGroup(View view) {
        return (view instanceof ExpandableNotificationRow) && this.mGroupManager.isChildInGroupWithSummary(((ExpandableNotificationRow) view).getEntry().getSbn());
    }

    private boolean generateRemoveAnimation(ExpandableView expandableView) {
        if (removeRemovedChildFromHeadsUpChangeAnimations(expandableView)) {
            this.mAddedHeadsUpChildren.remove(expandableView);
            return false;
        }
        if (isClickedHeadsUp(expandableView)) {
            this.mClearTransientViewsWhenFinished.add(expandableView);
            return true;
        }
        if (this.mIsExpanded && this.mAnimationsEnabled && !isChildInInvisibleGroup(expandableView)) {
            if (!this.mChildrenToAddAnimated.contains(expandableView)) {
                this.mChildrenToRemoveAnimated.add(expandableView);
                this.mNeedsAnimation = true;
                return true;
            }
            this.mChildrenToAddAnimated.remove(expandableView);
            this.mFromMoreCardAdditions.remove(expandableView);
        }
        return false;
    }

    private boolean isClickedHeadsUp(View view) {
        return HeadsUpUtil.isClickedHeadsUpNotification(view);
    }

    private boolean removeRemovedChildFromHeadsUpChangeAnimations(View view) {
        Iterator<Pair<ExpandableNotificationRow, Boolean>> it = this.mHeadsUpChangeAnimations.iterator();
        boolean z = false;
        while (it.hasNext()) {
            Pair<ExpandableNotificationRow, Boolean> next = it.next();
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next.first;
            boolean zBooleanValue = ((Boolean) next.second).booleanValue();
            if (view == expandableNotificationRow) {
                this.mTmpList.add(next);
                z |= zBooleanValue;
            }
        }
        if (z) {
            this.mHeadsUpChangeAnimations.removeAll(this.mTmpList);
            ((ExpandableNotificationRow) view).setHeadsUpAnimatingAway(false);
        }
        this.mTmpList.clear();
        return z;
    }

    private boolean isChildInInvisibleGroup(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        NotificationEntry groupSummary = this.mGroupManager.getGroupSummary(expandableNotificationRow.getEntry().getSbn());
        return (groupSummary == null || groupSummary.getRow() == expandableNotificationRow || expandableNotificationRow.getVisibility() != 4) ? false : true;
    }

    private void updateScrollStateForRemovedChild(ExpandableView expandableView) {
        float fInterpolate;
        int positionInLinearLayout = getPositionInLinearLayout(expandableView);
        float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
        if (increasedPaddingAmount >= 0.0f) {
            fInterpolate = NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, increasedPaddingAmount);
        } else {
            fInterpolate = NotificationUtils.interpolate(0.0f, this.mPaddingBetweenElements, increasedPaddingAmount + 1.0f);
        }
        int intrinsicHeight = getIntrinsicHeight(expandableView) + ((int) fInterpolate);
        int i = positionInLinearLayout + intrinsicHeight;
        int i2 = this.mOwnScrollY;
        if (i <= i2) {
            setOwnScrollY(i2 - intrinsicHeight);
        } else if (positionInLinearLayout < i2) {
            setOwnScrollY(positionInLinearLayout);
        }
    }

    private int getIntrinsicHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view.getHeight();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public int getPositionInLinearLayout(View view) {
        ExpandableNotificationRow expandableNotificationRow;
        float fInterpolate;
        float fInterpolate2;
        ExpandableNotificationRow expandableNotificationRow2 = null;
        if (isChildInGroup(view)) {
            expandableNotificationRow2 = (ExpandableNotificationRow) view;
            view = expandableNotificationRow2.getNotificationParent();
            expandableNotificationRow = view;
        } else {
            expandableNotificationRow = 0;
        }
        float f = this.mPaddingBetweenElements;
        float f2 = 0.0f;
        int intrinsicHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            boolean z = expandableView.getVisibility() != 8;
            if (z && !expandableView.hasNoContentHeight()) {
                float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
                if (increasedPaddingAmount >= 0.0f) {
                    fInterpolate2 = (int) NotificationUtils.interpolate(f, this.mIncreasedPaddingBetweenElements, increasedPaddingAmount);
                    fInterpolate = (int) NotificationUtils.interpolate(this.mPaddingBetweenElements, this.mIncreasedPaddingBetweenElements, increasedPaddingAmount);
                } else {
                    int iInterpolate = (int) NotificationUtils.interpolate(0.0f, this.mPaddingBetweenElements, 1.0f + increasedPaddingAmount);
                    float fInterpolate3 = f2 > 0.0f ? (int) NotificationUtils.interpolate(iInterpolate, this.mIncreasedPaddingBetweenElements, f2) : iInterpolate;
                    fInterpolate = iInterpolate;
                    fInterpolate2 = fInterpolate3;
                }
                if (intrinsicHeight != 0) {
                    intrinsicHeight = (int) (intrinsicHeight + fInterpolate2);
                }
                f = fInterpolate;
                f2 = increasedPaddingAmount;
            }
            if (expandableView == view) {
                return expandableNotificationRow != 0 ? intrinsicHeight + expandableNotificationRow.getPositionOfChild(expandableNotificationRow2) : intrinsicHeight;
            }
            if (z) {
                intrinsicHeight += getIntrinsicHeight(expandableView);
            }
        }
        return 0;
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        onViewAddedInternal((ExpandableView) view);
    }

    private void updateFirstAndLastBackgroundViews() {
        NotificationSection firstVisibleSection = getFirstVisibleSection();
        NotificationSection lastVisibleSection = getLastVisibleSection();
        ExpandableView firstVisibleChild = firstVisibleSection == null ? null : firstVisibleSection.getFirstVisibleChild();
        ExpandableView lastVisibleChild = lastVisibleSection != null ? lastVisibleSection.getLastVisibleChild() : null;
        ExpandableView firstChildWithBackground = getFirstChildWithBackground();
        ExpandableView lastChildWithBackground = getLastChildWithBackground();
        boolean zUpdateFirstAndLastViewsForAllSections = this.mSectionsManager.updateFirstAndLastViewsForAllSections(this.mSections, getChildrenWithBackground());
        if (this.mAnimationsEnabled && this.mIsExpanded) {
            boolean z = true;
            this.mAnimateNextBackgroundTop = firstChildWithBackground != firstVisibleChild;
            if (lastChildWithBackground == lastVisibleChild && !this.mAnimateBottomOnLayout) {
                z = false;
            }
            this.mAnimateNextBackgroundBottom = z;
            this.mAnimateNextSectionBoundsChange = zUpdateFirstAndLastViewsForAllSections;
        } else {
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
            this.mAnimateNextSectionBoundsChange = false;
        }
        this.mAmbientState.setLastVisibleBackgroundChild(lastChildWithBackground);
        this.mRoundnessManager.updateRoundedChildren(this.mSections);
        this.mAnimateBottomOnLayout = false;
        invalidate();
    }

    private void onViewAddedInternal(ExpandableView expandableView) {
        updateHideSensitiveForChild(expandableView);
        expandableView.setOnHeightChangedListener(this);
        generateAddAnimation(expandableView, false);
        updateAnimationState(expandableView);
        updateChronometerForChild(expandableView);
        if (expandableView instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) expandableView).setDismissRtl(this.mDismissRtl);
        }
    }

    private void updateHideSensitiveForChild(ExpandableView expandableView) {
        expandableView.setHideSensitiveForIntrinsicHeight(this.mAmbientState.isHideSensitive());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void notifyGroupChildRemoved(ExpandableView expandableView, ViewGroup viewGroup) {
        onViewRemovedInternal(expandableView, viewGroup);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void notifyGroupChildRemoved(View view, ViewGroup viewGroup) {
        notifyGroupChildRemoved((ExpandableView) view, viewGroup);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void notifyGroupChildAdded(ExpandableView expandableView) {
        onViewAddedInternal(expandableView);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void notifyGroupChildAdded(View view) {
        notifyGroupChildAdded((ExpandableView) view);
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateNotificationAnimationStates();
        if (z) {
            return;
        }
        this.mSwipedOutViews.clear();
        this.mChildrenToRemoveAnimated.clear();
        clearTemporaryViewsInGroup(this);
    }

    private void updateNotificationAnimationStates() {
        boolean z = this.mAnimationsEnabled || hasPulsingNotifications();
        this.mShelf.setAnimationsEnabled(z);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            z &= this.mIsExpanded || isPinnedHeadsUp(childAt);
            updateAnimationState(z, childAt);
        }
    }

    private void updateAnimationState(View view) {
        updateAnimationState((this.mAnimationsEnabled || hasPulsingNotifications()) && (this.mIsExpanded || isPinnedHeadsUp(view)), view);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setExpandingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mAmbientState.setExpandingNotification(expandableNotificationRow);
        requestChildrenUpdate();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void bindRow(final ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setHeadsUpAnimatingAwayListener(new Consumer() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda20
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$bindRow$7(expandableNotificationRow, (Boolean) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bindRow$7(ExpandableNotificationRow expandableNotificationRow, Boolean bool) {
        this.mRoundnessManager.onHeadsupAnimatingAwayChanged(expandableNotificationRow, bool.booleanValue());
        this.mHeadsUpAppearanceController.lambda$updateHeadsUpHeaders$4(expandableNotificationRow.getEntry());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public boolean containsView(View view) {
        return view.getParent() == this;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void applyExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.mAmbientState.setExpandAnimationTopChange(expandAnimationParameters == null ? 0 : expandAnimationParameters.getTopChange());
        requestChildrenUpdate();
    }

    private void updateAnimationState(boolean z, View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setIconAnimationRunning(z);
        }
    }

    public boolean isAddOrRemoveAnimationPending() {
        return this.mNeedsAnimation && !(this.mChildrenToAddAnimated.isEmpty() && this.mChildrenToRemoveAnimated.isEmpty());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void generateAddAnimation(ExpandableView expandableView, boolean z) {
        if (this.mIsExpanded && this.mAnimationsEnabled && !this.mChangePositionInProgress && !isFullyHidden()) {
            this.mChildrenToAddAnimated.add(expandableView);
            if (z) {
                this.mFromMoreCardAdditions.add(expandableView);
            }
            this.mNeedsAnimation = true;
        }
        if (!isHeadsUp(expandableView) || !this.mAnimationsEnabled || this.mChangePositionInProgress || isFullyHidden()) {
            return;
        }
        this.mAddedHeadsUpChildren.add(expandableView);
        this.mChildrenToAddAnimated.remove(expandableView);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void changeViewPosition(ExpandableView expandableView, int i) {
        Assert.isMainThread();
        if (this.mChangePositionInProgress) {
            throw new IllegalStateException("Reentrant call to changeViewPosition");
        }
        int iIndexOfChild = indexOfChild(expandableView);
        boolean z = false;
        if (iIndexOfChild == -1) {
            if ((expandableView instanceof ExpandableNotificationRow) && expandableView.getTransientContainer() != null) {
                z = true;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Attempting to re-position ");
            sb.append(z ? "transient" : "");
            sb.append(" view {");
            sb.append(expandableView);
            sb.append("}");
            Log.e("StackScroller", sb.toString());
            return;
        }
        if (expandableView == null || expandableView.getParent() != this || iIndexOfChild == i) {
            return;
        }
        this.mChangePositionInProgress = true;
        expandableView.setChangingPosition(true);
        removeView(expandableView);
        addView(expandableView, i);
        expandableView.setChangingPosition(false);
        this.mChangePositionInProgress = false;
        if (this.mIsExpanded && this.mAnimationsEnabled && expandableView.getVisibility() != 8) {
            this.mChildrenChangingPositions.add(expandableView);
            this.mNeedsAnimation = true;
        }
    }

    private void startAnimationToState() {
        if (this.mNeedsAnimation) {
            generateAllAnimationEvents();
            this.mNeedsAnimation = false;
        }
        if (!this.mAnimationEvents.isEmpty() || isCurrentlyAnimating()) {
            setAnimationRunning(true);
            this.mStateAnimator.startAnimationForEvents(this.mAnimationEvents, this.mGoToFullShadeDelay);
            this.mAnimationEvents.clear();
            updateBackground();
            updateViewShadows();
            updateClippingToTopRoundedCorner();
        } else {
            applyCurrentState();
        }
        this.mGoToFullShadeDelay = 0L;
    }

    private void generateAllAnimationEvents() {
        generateHeadsUpAnimationEvents();
        generateChildRemovalEvents();
        generateChildAdditionEvents();
        generatePositionChangeEvents();
        generateTopPaddingEvent();
        generateActivateEvent();
        generateDimmedEvent();
        generateHideSensitiveEvent();
        generateGoToFullShadeEvent();
        generateViewResizeEvent();
        generateGroupExpansionEvent();
        generateAnimateEverythingEvent();
    }

    private void generateHeadsUpAnimationEvents() {
        Iterator<Pair<ExpandableNotificationRow, Boolean>> it = this.mHeadsUpChangeAnimations.iterator();
        while (it.hasNext()) {
            Pair<ExpandableNotificationRow, Boolean> next = it.next();
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next.first;
            boolean zBooleanValue = ((Boolean) next.second).booleanValue();
            if (zBooleanValue == expandableNotificationRow.isHeadsUp()) {
                int i = 14;
                boolean z = true;
                boolean z2 = false;
                boolean z3 = expandableNotificationRow.isPinned() && !this.mIsExpanded;
                if (this.mIsExpanded && (!this.mKeyguardBypassController.getBypassEnabled() || !onKeyguard() || !this.mHeadsUpManager.hasPinnedHeadsUp())) {
                    z = false;
                }
                if (z && !zBooleanValue) {
                    i = expandableNotificationRow.wasJustClicked() ? 13 : 12;
                    if (expandableNotificationRow.isChildInGroup()) {
                        expandableNotificationRow.setHeadsUpAnimatingAway(false);
                    } else {
                        AnimationEvent animationEvent = new AnimationEvent(expandableNotificationRow, i);
                        animationEvent.headsUpFromBottom = z2;
                        this.mAnimationEvents.add(animationEvent);
                    }
                } else {
                    ExpandableViewState viewState = expandableNotificationRow.getViewState();
                    if (viewState != null) {
                        if (zBooleanValue && (this.mAddedHeadsUpChildren.contains(expandableNotificationRow) || z3)) {
                            i = (z3 || shouldHunAppearFromBottom(viewState)) ? 11 : 0;
                            z2 = !z3;
                        }
                        AnimationEvent animationEvent2 = new AnimationEvent(expandableNotificationRow, i);
                        animationEvent2.headsUpFromBottom = z2;
                        this.mAnimationEvents.add(animationEvent2);
                    }
                }
            }
        }
        this.mHeadsUpChangeAnimations.clear();
        this.mAddedHeadsUpChildren.clear();
    }

    private boolean shouldHunAppearFromBottom(ExpandableViewState expandableViewState) {
        return expandableViewState.yTranslation + ((float) expandableViewState.height) >= this.mAmbientState.getMaxHeadsUpTranslation();
    }

    private void generateGroupExpansionEvent() {
        if (this.mExpandedGroupView != null) {
            this.mAnimationEvents.add(new AnimationEvent(this.mExpandedGroupView, 10));
            this.mExpandedGroupView = null;
        }
    }

    private void generateViewResizeEvent() {
        boolean z;
        if (this.mNeedViewResizeAnimation) {
            Iterator<AnimationEvent> it = this.mAnimationEvents.iterator();
            while (it.hasNext()) {
                int i = it.next().animationType;
                if (i == 13 || i == 12) {
                    z = true;
                    break;
                }
            }
            z = false;
            if (!z) {
                this.mAnimationEvents.add(new AnimationEvent(null, 9));
            }
        }
        this.mNeedViewResizeAnimation = false;
    }

    private void generateChildRemovalEvents() {
        boolean z;
        ViewGroup transientContainer;
        Iterator<ExpandableView> it = this.mChildrenToRemoveAnimated.iterator();
        while (it.hasNext()) {
            ExpandableView next = it.next();
            boolean zContains = this.mSwipedOutViews.contains(next);
            float translationY = next.getTranslationY();
            boolean z2 = false;
            int i = 1;
            if (next instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next;
                if (expandableNotificationRow.isRemoved() && expandableNotificationRow.wasChildInGroupWhenRemoved()) {
                    translationY = expandableNotificationRow.getTranslationWhenRemoved();
                    z = false;
                } else {
                    z = true;
                }
                zContains |= Math.abs(expandableNotificationRow.getTranslation()) == ((float) expandableNotificationRow.getWidth());
            } else if (next instanceof MediaHeaderView) {
                zContains = true;
                z = true;
            } else {
                z = true;
            }
            if (!zContains) {
                Rect clipBounds = next.getClipBounds();
                if (clipBounds != null && clipBounds.height() == 0) {
                    z2 = true;
                }
                if (z2 && (transientContainer = next.getTransientContainer()) != null) {
                    transientContainer.removeTransientView(next);
                }
                zContains = z2;
            }
            if (zContains) {
                i = 2;
            }
            AnimationEvent animationEvent = new AnimationEvent(next, i);
            animationEvent.viewAfterChangingView = getFirstChildBelowTranlsationY(translationY, z);
            this.mAnimationEvents.add(animationEvent);
            this.mSwipedOutViews.remove(next);
        }
        this.mChildrenToRemoveAnimated.clear();
    }

    private void generatePositionChangeEvents() {
        AnimationEvent animationEvent;
        Iterator<ExpandableView> it = this.mChildrenChangingPositions.iterator();
        while (true) {
            Integer num = null;
            if (!it.hasNext()) {
                break;
            }
            ExpandableView next = it.next();
            if (next instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next;
                if (expandableNotificationRow.getEntry().isMarkedForUserTriggeredMovement()) {
                    num = 500;
                    expandableNotificationRow.getEntry().markForUserTriggeredMovement(false);
                }
            }
            if (num == null) {
                animationEvent = new AnimationEvent(next, 6);
            } else {
                animationEvent = new AnimationEvent(next, 6, num.intValue());
            }
            this.mAnimationEvents.add(animationEvent);
        }
        this.mChildrenChangingPositions.clear();
        if (this.mGenerateChildOrderChangedEvent) {
            this.mAnimationEvents.add(new AnimationEvent(null, 6));
            this.mGenerateChildOrderChangedEvent = false;
        }
    }

    private void generateChildAdditionEvents() {
        Iterator<ExpandableView> it = this.mChildrenToAddAnimated.iterator();
        while (it.hasNext()) {
            ExpandableView next = it.next();
            if (this.mFromMoreCardAdditions.contains(next)) {
                this.mAnimationEvents.add(new AnimationEvent(next, 0, 360L));
            } else {
                this.mAnimationEvents.add(new AnimationEvent(next, 0));
            }
        }
        this.mChildrenToAddAnimated.clear();
        this.mFromMoreCardAdditions.clear();
    }

    private void generateTopPaddingEvent() {
        AnimationEvent animationEvent;
        if (this.mTopPaddingNeedsAnimation) {
            if (this.mAmbientState.isDozing()) {
                animationEvent = new AnimationEvent(null, 3, 550L);
            } else {
                animationEvent = new AnimationEvent(null, 3);
            }
            this.mAnimationEvents.add(animationEvent);
        }
        this.mTopPaddingNeedsAnimation = false;
    }

    private void generateActivateEvent() {
        if (this.mActivateNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 4));
        }
        this.mActivateNeedsAnimation = false;
    }

    private void generateAnimateEverythingEvent() {
        if (this.mEverythingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 15));
        }
        this.mEverythingNeedsAnimation = false;
    }

    private void generateDimmedEvent() {
        if (this.mDimmedNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 5));
        }
        this.mDimmedNeedsAnimation = false;
    }

    private void generateHideSensitiveEvent() {
        if (this.mHideSensitiveNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 8));
        }
        this.mHideSensitiveNeedsAnimation = false;
    }

    private void generateGoToFullShadeEvent() {
        if (this.mGoToFullShadeNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 7));
        }
        this.mGoToFullShadeNeedsAnimation = false;
    }

    protected StackScrollAlgorithm createStackScrollAlgorithm(Context context) {
        return new StackScrollAlgorithm(context, this);
    }

    public boolean isInContentBounds(float f) {
        return f < ((float) (getHeight() - getEmptyBottomMargin()));
    }

    public void setLongPressListener(ExpandableNotificationRow.LongPressListener longPressListener) {
        this.mLongPressListener = longPressListener;
    }

    private float getTouchSlop(MotionEvent motionEvent) {
        if (motionEvent.getClassification() == 1) {
            return this.mTouchSlop * this.mSlopMultiplier;
        }
        return this.mTouchSlop;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean zOnTouchEvent;
        NotificationGuts exposedGuts = this.mNotificationGutsManager.getExposedGuts();
        boolean z = motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1;
        handleEmptySpaceClick(motionEvent);
        boolean z2 = this.mSwipingInProgress;
        if (!this.mIsExpanded || z2 || this.mOnlyScrollingInThisMotion || exposedGuts != null) {
            zOnTouchEvent = false;
        } else {
            if (z) {
                this.mExpandHelper.onlyObserveMovements(false);
            }
            boolean z3 = this.mExpandingNotification;
            zOnTouchEvent = this.mExpandHelper.onTouchEvent(motionEvent);
            if (this.mExpandedInThisMotion && !this.mExpandingNotification && z3 && !this.mDisallowScrollingInThisMotion) {
                dispatchDownEventToScroller(motionEvent);
            }
        }
        boolean zOnScrollTouch = (!this.mIsExpanded || z2 || this.mExpandingNotification || this.mDisallowScrollingInThisMotion) ? false : onScrollTouch(motionEvent);
        boolean zOnTouchEvent2 = (this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion) ? false : this.mSwipeHelper.onTouchEvent(motionEvent);
        if (exposedGuts != null && !NotificationSwipeHelper.isTouchInView(motionEvent, exposedGuts) && (exposedGuts.getGutsContent() instanceof NotificationSnooze) && ((((NotificationSnooze) exposedGuts.getGutsContent()).isExpanded() && z) || (!zOnTouchEvent2 && zOnScrollTouch))) {
            checkSnoozeLeavebehind();
        }
        if (motionEvent.getActionMasked() == 1) {
            this.mCheckForLeavebehind = true;
        }
        return zOnTouchEvent2 || zOnScrollTouch || zOnTouchEvent || super.onTouchEvent(motionEvent);
    }

    private void dispatchDownEventToScroller(MotionEvent motionEvent) {
        MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
        motionEventObtain.setAction(0);
        onScrollTouch(motionEventObtain);
        motionEventObtain.recycle();
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (!isScrollingEnabled() || !this.mIsExpanded || this.mSwipingInProgress || this.mExpandingNotification || this.mDisallowScrollingInThisMotion) {
            return false;
        }
        if ((motionEvent.getSource() & 2) != 0 && motionEvent.getAction() == 8 && !this.mIsBeingDragged) {
            float axisValue = motionEvent.getAxisValue(9);
            if (axisValue != 0.0f) {
                int verticalScrollFactor = (int) (axisValue * getVerticalScrollFactor());
                int scrollRange = getScrollRange();
                int i = this.mOwnScrollY;
                int i2 = i - verticalScrollFactor;
                int i3 = i2 >= 0 ? i2 > scrollRange ? scrollRange : i2 : 0;
                if (i3 != i) {
                    setOwnScrollY(i3);
                    return true;
                }
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    private boolean onScrollTouch(MotionEvent motionEvent) {
        float fOverScrollUp;
        if (!isScrollingEnabled()) {
            return false;
        }
        if (isInsideQsContainer(motionEvent) && !this.mIsBeingDragged) {
            return false;
        }
        this.mForcedScroll = null;
        initVelocityTrackerIfNotExists();
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (motionEvent.findPointerIndex(this.mActivePointerId) == -1 && actionMasked != 0) {
            Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent " + MotionEvent.actionToString(motionEvent.getActionMasked()));
            return true;
        }
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int iFindPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (iFindPointerIndex == -1) {
                        Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                    } else {
                        int y = (int) motionEvent.getY(iFindPointerIndex);
                        int x = (int) motionEvent.getX(iFindPointerIndex);
                        int i = this.mLastMotionY - y;
                        int iAbs = Math.abs(x - this.mDownX);
                        int iAbs2 = Math.abs(i);
                        float touchSlop = getTouchSlop(motionEvent);
                        if (!this.mIsBeingDragged && iAbs2 > touchSlop && iAbs2 > iAbs) {
                            setIsBeingDragged(true);
                            i = (int) (i > 0 ? i - touchSlop : i + touchSlop);
                        }
                        if (this.mIsBeingDragged) {
                            this.mLastMotionY = y;
                            int scrollRange = getScrollRange();
                            if (this.mExpandedInThisMotion) {
                                scrollRange = Math.min(scrollRange, this.mMaxScrollAfterExpand);
                            }
                            if (i < 0) {
                                fOverScrollUp = overScrollDown(i);
                            } else {
                                fOverScrollUp = overScrollUp(i, scrollRange);
                            }
                            if (fOverScrollUp != 0.0f) {
                                customOverScrollBy((int) fOverScrollUp, this.mOwnScrollY, scrollRange, getHeight() / 2);
                                checkSnoozeLeavebehind();
                            }
                        }
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 5) {
                        int actionIndex = motionEvent.getActionIndex();
                        this.mLastMotionY = (int) motionEvent.getY(actionIndex);
                        this.mDownX = (int) motionEvent.getX(actionIndex);
                        this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                    } else if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                        this.mLastMotionY = (int) motionEvent.getY(motionEvent.findPointerIndex(this.mActivePointerId));
                        this.mDownX = (int) motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId));
                    }
                } else if (this.mIsBeingDragged && getChildCount() > 0) {
                    if (this.mScroller.springBack(((ViewGroup) this).mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                        lambda$new$1();
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                }
            } else if (this.mIsBeingDragged) {
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                int yVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                if (shouldOverScrollFling(yVelocity)) {
                    onOverScrollFling(true, yVelocity);
                } else if (getChildCount() > 0) {
                    if (Math.abs(yVelocity) > this.mMinimumVelocity) {
                        if (getCurrentOverScrollAmount(true) == 0.0f || yVelocity > 0) {
                            fling(-yVelocity);
                        } else {
                            onOverScrollFling(false, yVelocity);
                        }
                    } else if (this.mScroller.springBack(((ViewGroup) this).mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                        lambda$new$1();
                    }
                }
                this.mActivePointerId = -1;
                endDrag();
            }
        } else {
            if (getChildCount() == 0 || !isInContentBounds(motionEvent)) {
                return false;
            }
            setIsBeingDragged(!this.mScroller.isFinished());
            if (!this.mScroller.isFinished()) {
                this.mScroller.forceFinished(true);
            }
            this.mLastMotionY = (int) motionEvent.getY();
            this.mDownX = (int) motionEvent.getX();
            this.mActivePointerId = motionEvent.getPointerId(0);
        }
        return true;
    }

    protected boolean isInsideQsContainer(MotionEvent motionEvent) {
        return motionEvent.getY() < ((float) this.mQsContainer.getBottom());
    }

    private void onOverScrollFling(boolean z, int i) {
        OnOverscrollTopChangedListener onOverscrollTopChangedListener = this.mOverscrollTopChangedListener;
        if (onOverscrollTopChangedListener != null) {
            onOverscrollTopChangedListener.flingTopOverscroll(i, z);
        }
        this.mDontReportNextOverScroll = true;
        setOverScrollAmount(0.0f, true, false);
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            int i = action == 0 ? 1 : 0;
            this.mLastMotionY = (int) motionEvent.getY(i);
            this.mActivePointerId = motionEvent.getPointerId(i);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        setIsBeingDragged(false);
        recycleVelocityTracker();
        if (getCurrentOverScrollAmount(true) > 0.0f) {
            setOverScrollAmount(0.0f, true, true);
        }
        if (getCurrentOverScrollAmount(false) > 0.0f) {
            setOverScrollAmount(0.0f, false, true);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        initDownStates(motionEvent);
        handleEmptySpaceClick(motionEvent);
        NotificationGuts exposedGuts = this.mNotificationGutsManager.getExposedGuts();
        boolean z = this.mSwipingInProgress;
        boolean zOnInterceptTouchEvent = (z || this.mOnlyScrollingInThisMotion || exposedGuts != null) ? false : this.mExpandHelper.onInterceptTouchEvent(motionEvent);
        boolean zOnInterceptTouchEventScroll = (z || this.mExpandingNotification) ? false : onInterceptTouchEventScroll(motionEvent);
        boolean zOnInterceptTouchEvent2 = (this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion) ? false : this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
        boolean z2 = motionEvent.getActionMasked() == 1;
        if (!NotificationSwipeHelper.isTouchInView(motionEvent, exposedGuts) && z2 && !zOnInterceptTouchEvent2 && !zOnInterceptTouchEvent && !zOnInterceptTouchEventScroll) {
            this.mCheckForLeavebehind = false;
            this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
        }
        if (motionEvent.getActionMasked() == 1) {
            this.mCheckForLeavebehind = true;
        }
        return zOnInterceptTouchEvent2 || zOnInterceptTouchEventScroll || zOnInterceptTouchEvent || super.onInterceptTouchEvent(motionEvent);
    }

    private void handleEmptySpaceClick(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 1) {
            if (this.mStatusBarState != 1 && this.mTouchIsClick && isBelowLastNotification(this.mInitialTouchX, this.mInitialTouchY)) {
                this.mOnEmptySpaceClickListener.onEmptySpaceClicked(this.mInitialTouchX, this.mInitialTouchY);
                return;
            }
            return;
        }
        if (actionMasked != 2) {
            return;
        }
        float touchSlop = getTouchSlop(motionEvent);
        if (this.mTouchIsClick) {
            if (Math.abs(motionEvent.getY() - this.mInitialTouchY) > touchSlop || Math.abs(motionEvent.getX() - this.mInitialTouchX) > touchSlop) {
                this.mTouchIsClick = false;
            }
        }
    }

    private void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mExpandedInThisMotion = false;
            this.mOnlyScrollingInThisMotion = !this.mScroller.isFinished();
            this.mDisallowScrollingInThisMotion = false;
            this.mDisallowDismissInThisMotion = false;
            this.mTouchIsClick = true;
            this.mInitialTouchX = motionEvent.getX();
            this.mInitialTouchY = motionEvent.getY();
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        super.requestDisallowInterceptTouchEvent(z);
        if (z) {
            cancelLongPress();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x0089  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean onInterceptTouchEventScroll(android.view.MotionEvent r9) {
        /*
            Method dump skipped, instructions count: 233
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.onInterceptTouchEventScroll(android.view.MotionEvent):boolean");
    }

    private boolean isInContentBounds(MotionEvent motionEvent) {
        return isInContentBounds(motionEvent.getY());
    }

    @VisibleForTesting
    void setIsBeingDragged(boolean z) {
        this.mIsBeingDragged = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
            cancelLongPress();
            resetExposedMenuView(true, true);
        }
    }

    public void requestDisallowLongPress() {
        cancelLongPress();
    }

    public void requestDisallowDismiss() {
        this.mDisallowDismissInThisMotion = true;
    }

    @Override // android.view.View
    public void cancelLongPress() {
        this.mSwipeHelper.cancelLongPress();
    }

    public void setOnEmptySpaceClickListener(OnEmptySpaceClickListener onEmptySpaceClickListener) {
        this.mOnEmptySpaceClickListener = onEmptySpaceClickListener;
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0021, code lost:
    
        if (r5 != 16908346) goto L22;
     */
    /* JADX WARN: Removed duplicated region for block: B:20:0x004d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean performAccessibilityActionInternal(int r5, android.os.Bundle r6) {
        /*
            r4 = this;
            boolean r6 = super.performAccessibilityActionInternal(r5, r6)
            r0 = 1
            if (r6 == 0) goto L8
            return r0
        L8:
            boolean r6 = r4.isEnabled()
            r1 = 0
            if (r6 != 0) goto L10
            return r1
        L10:
            r6 = -1
            r2 = 4096(0x1000, float:5.74E-42)
            if (r5 == r2) goto L24
            r2 = 8192(0x2000, float:1.148E-41)
            if (r5 == r2) goto L25
            r2 = 16908344(0x1020038, float:2.3877386E-38)
            if (r5 == r2) goto L25
            r6 = 16908346(0x102003a, float:2.3877392E-38)
            if (r5 == r6) goto L24
            goto L59
        L24:
            r6 = r0
        L25:
            int r5 = r4.getHeight()
            int r2 = r4.mPaddingBottom
            int r5 = r5 - r2
            int r2 = r4.mTopPadding
            int r5 = r5 - r2
            int r2 = r4.mPaddingTop
            int r5 = r5 - r2
            com.android.systemui.statusbar.NotificationShelf r2 = r4.mShelf
            int r2 = r2.getIntrinsicHeight()
            int r5 = r5 - r2
            int r2 = r4.mOwnScrollY
            int r6 = r6 * r5
            int r2 = r2 + r6
            int r5 = r4.getScrollRange()
            int r5 = java.lang.Math.min(r2, r5)
            int r5 = java.lang.Math.max(r1, r5)
            int r6 = r4.mOwnScrollY
            if (r5 == r6) goto L59
            android.widget.OverScroller r2 = r4.mScroller
            int r3 = r4.mScrollX
            int r5 = r5 - r6
            r2.startScroll(r3, r6, r1, r5)
            r4.lambda$new$1()
            return r0
        L59:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.performAccessibilityActionInternal(int, android.os.Bundle):boolean");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v1, types: [android.view.View] */
    public void closeControlsIfOutsideTouch(MotionEvent motionEvent) {
        NotificationGuts exposedGuts = this.mNotificationGutsManager.getExposedGuts();
        NotificationMenuRowPlugin currentMenuRow = this.mSwipeHelper.getCurrentMenuRow();
        ?? translatingParentView = this.mSwipeHelper.getTranslatingParentView();
        if (exposedGuts == null || exposedGuts.getGutsContent().isLeavebehind()) {
            exposedGuts = (currentMenuRow == null || !currentMenuRow.isMenuVisible() || translatingParentView == 0) ? null : translatingParentView;
        }
        if (exposedGuts == null || NotificationSwipeHelper.isTouchInView(motionEvent, exposedGuts)) {
            return;
        }
        this.mNotificationGutsManager.closeAndSaveGuts(false, false, true, -1, -1, false);
        resetExposedMenuView(true, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSwipingInProgress(boolean z) {
        this.mSwipingInProgress = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            return;
        }
        cancelLongPress();
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void clearChildFocus(View view) {
        super.clearChildFocus(view);
        if (this.mForcedScroll == view) {
            this.mForcedScroll = null;
        }
    }

    @Override // com.android.systemui.statusbar.policy.ScrollAdapter
    public boolean isScrolledToTop() {
        return this.mOwnScrollY == 0;
    }

    public boolean isScrolledToBottom() {
        return this.mOwnScrollY >= getScrollRange();
    }

    public int getEmptyBottomMargin() {
        return Math.max(this.mMaxLayoutHeight - this.mContentHeight, 0);
    }

    public void checkSnoozeLeavebehind() {
        if (this.mCheckForLeavebehind) {
            this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
            this.mCheckForLeavebehind = false;
        }
    }

    public void resetCheckSnoozeLeavebehind() {
        this.mCheckForLeavebehind = true;
    }

    public void onExpansionStarted() {
        this.mIsExpansionChanging = true;
        this.mAmbientState.setExpansionChanging(true);
        checkSnoozeLeavebehind();
    }

    public void onExpansionStopped() {
        this.mIsExpansionChanging = false;
        resetCheckSnoozeLeavebehind();
        this.mAmbientState.setExpansionChanging(false);
        if (this.mIsExpanded) {
            return;
        }
        resetScrollPosition();
        this.mStatusBar.resetUserExpandedStates();
        clearTemporaryViews();
        clearUserLockedViews();
        ArrayList<ExpandableView> draggedViews = this.mAmbientState.getDraggedViews();
        if (draggedViews.size() > 0) {
            draggedViews.clear();
            updateContinuousShadowDrawing();
        }
    }

    private void clearUserLockedViews() {
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).setUserLocked(false);
            }
        }
    }

    private void clearTemporaryViews() {
        clearTemporaryViewsInGroup(this);
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                clearTemporaryViewsInGroup(((ExpandableNotificationRow) expandableView).getChildrenContainer());
            }
        }
    }

    private void clearTemporaryViewsInGroup(ViewGroup viewGroup) {
        while (viewGroup != null && viewGroup.getTransientViewCount() != 0) {
            viewGroup.removeTransientView(viewGroup.getTransientView(0));
        }
    }

    public void onPanelTrackingStarted() {
        this.mPanelTracking = true;
        this.mAmbientState.setPanelTracking(true);
        resetExposedMenuView(true, true);
    }

    public void onPanelTrackingStopped() {
        this.mPanelTracking = false;
        this.mAmbientState.setPanelTracking(false);
    }

    public void resetScrollPosition() {
        this.mScroller.abortAnimation();
        setOwnScrollY(0);
    }

    private void setIsExpanded(boolean z) {
        boolean z2 = z != this.mIsExpanded;
        this.mIsExpanded = z;
        this.mStackScrollAlgorithm.setIsExpanded(z);
        this.mAmbientState.setShadeExpanded(z);
        this.mStateAnimator.setShadeExpanded(z);
        this.mSwipeHelper.setIsExpanded(z);
        if (z2) {
            this.mWillExpand = false;
            if (!this.mIsExpanded) {
                this.mGroupManager.collapseAllGroups();
                this.mExpandHelper.cancelImmediately();
            }
            updateNotificationAnimationStates();
            updateChronometers();
            requestChildrenUpdate();
        }
    }

    private void updateChronometers() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            updateChronometerForChild(getChildAt(i));
        }
    }

    private void updateChronometerForChild(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setChronometerRunning(this.mIsExpanded);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
    public void onHeightChanged(ExpandableView expandableView, boolean z) {
        updateContentHeight();
        updateScrollPositionOnExpandInBottom(expandableView);
        clampScrollPosition();
        notifyHeightChangeListener(expandableView, z);
        ExpandableNotificationRow expandableNotificationRow = expandableView instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) expandableView : null;
        NotificationSection firstVisibleSection = getFirstVisibleSection();
        ExpandableView firstVisibleChild = firstVisibleSection != null ? firstVisibleSection.getFirstVisibleChild() : null;
        if (expandableNotificationRow != null && (expandableNotificationRow == firstVisibleChild || expandableNotificationRow.getNotificationParent() == firstVisibleChild)) {
            updateAlgorithmLayoutMinHeight();
        }
        if (z) {
            requestAnimationOnViewResize(expandableNotificationRow);
        }
        requestChildrenUpdate();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
    public void onReset(ExpandableView expandableView) {
        updateAnimationState(expandableView);
        updateChronometerForChild(expandableView);
    }

    private void updateScrollPositionOnExpandInBottom(ExpandableView expandableView) {
        if (!(expandableView instanceof ExpandableNotificationRow) || onKeyguard()) {
            return;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
        if (!expandableNotificationRow.isUserLocked() || expandableNotificationRow == getFirstChildNotGone() || expandableNotificationRow.isSummaryWithChildren()) {
            return;
        }
        float translationY = expandableNotificationRow.getTranslationY() + expandableNotificationRow.getActualHeight();
        if (expandableNotificationRow.isChildInGroup()) {
            translationY += expandableNotificationRow.getNotificationParent().getTranslationY();
        }
        int intrinsicHeight = this.mMaxLayoutHeight + ((int) this.mStackTranslation);
        NotificationSection lastVisibleSection = getLastVisibleSection();
        if (expandableNotificationRow != (lastVisibleSection == null ? null : lastVisibleSection.getLastVisibleChild()) && this.mShelf.getVisibility() != 8) {
            intrinsicHeight -= this.mShelf.getIntrinsicHeight() + this.mPaddingBetweenElements;
        }
        float f = intrinsicHeight;
        if (translationY > f) {
            setOwnScrollY((int) ((this.mOwnScrollY + translationY) - f));
            this.mDisallowScrollingInThisMotion = true;
        }
    }

    public void setOnHeightChangedListener(ExpandableView.OnHeightChangedListener onHeightChangedListener) {
        this.mOnHeightChangedListener = onHeightChangedListener;
    }

    public void onChildAnimationFinished() {
        setAnimationRunning(false);
        requestChildrenUpdate();
        runAnimationFinishedRunnables();
        clearTransient();
        clearHeadsUpDisappearRunning();
    }

    private void clearHeadsUpDisappearRunning() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                expandableNotificationRow.setHeadsUpAnimatingAway(false);
                if (expandableNotificationRow.isSummaryWithChildren()) {
                    Iterator<ExpandableNotificationRow> it = expandableNotificationRow.getAttachedChildren().iterator();
                    while (it.hasNext()) {
                        it.next().setHeadsUpAnimatingAway(false);
                    }
                }
            }
        }
    }

    private void clearTransient() {
        Iterator<ExpandableView> it = this.mClearTransientViewsWhenFinished.iterator();
        while (it.hasNext()) {
            StackStateAnimator.removeTransientView(it.next());
        }
        this.mClearTransientViewsWhenFinished.clear();
    }

    private void runAnimationFinishedRunnables() {
        Iterator<Runnable> it = this.mAnimationFinishedRunnables.iterator();
        while (it.hasNext()) {
            it.next().run();
        }
        this.mAnimationFinishedRunnables.clear();
    }

    public void setDimmed(boolean z, boolean z2) {
        boolean zOnKeyguard = z & onKeyguard();
        this.mAmbientState.setDimmed(zOnKeyguard);
        if (z2 && this.mAnimationsEnabled) {
            this.mDimmedNeedsAnimation = true;
            this.mNeedsAnimation = true;
            animateDimmed(zOnKeyguard);
        } else {
            setDimAmount(zOnKeyguard ? 1.0f : 0.0f);
        }
        requestChildrenUpdate();
    }

    @VisibleForTesting
    boolean isDimmed() {
        return this.mAmbientState.isDimmed();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDimAmount(float f) {
        this.mDimAmount = f;
        updateBackgroundDimming();
    }

    private void animateDimmed(boolean z) {
        ValueAnimator valueAnimator = this.mDimAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        float f = z ? 1.0f : 0.0f;
        float f2 = this.mDimAmount;
        if (f == f2) {
            return;
        }
        ValueAnimator valueAnimatorOfFloat = TimeAnimator.ofFloat(f2, f);
        this.mDimAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(220L);
        this.mDimAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mDimAnimator.addListener(this.mDimEndListener);
        this.mDimAnimator.addUpdateListener(this.mDimUpdateListener);
        this.mDimAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSensitiveness(boolean z) {
        boolean zIsAnyProfilePublicMode = this.mLockscreenUserManager.isAnyProfilePublicMode();
        if (zIsAnyProfilePublicMode != this.mAmbientState.isHideSensitive()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((ExpandableView) getChildAt(i)).setHideSensitiveForIntrinsicHeight(zIsAnyProfilePublicMode);
            }
            this.mAmbientState.setHideSensitive(zIsAnyProfilePublicMode);
            if (z && this.mAnimationsEnabled) {
                this.mHideSensitiveNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            updateContentHeight();
            requestChildrenUpdate();
        }
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mAmbientState.setActivatedChild(activatableNotificationView);
        if (this.mAnimationsEnabled) {
            this.mActivateNeedsAnimation = true;
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mAmbientState.getActivatedChild();
    }

    private void applyCurrentState() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((ExpandableView) getChildAt(i)).applyViewState();
        }
        NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener = this.mListener;
        if (onChildLocationsChangedListener != null) {
            onChildLocationsChangedListener.onChildLocationsChanged();
        }
        runAnimationFinishedRunnables();
        setAnimationRunning(false);
        updateBackground();
        updateViewShadows();
        updateClippingToTopRoundedCorner();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateViewShadows() {
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                this.mTmpSortedChildren.add(expandableView);
            }
        }
        Collections.sort(this.mTmpSortedChildren, this.mViewPositionComparator);
        ExpandableView expandableView2 = null;
        int i2 = 0;
        while (i2 < this.mTmpSortedChildren.size()) {
            ExpandableView expandableView3 = this.mTmpSortedChildren.get(i2);
            float translationZ = expandableView3.getTranslationZ();
            float translationZ2 = (expandableView2 == null ? translationZ : expandableView2.getTranslationZ()) - translationZ;
            if (translationZ2 <= 0.0f || translationZ2 >= 0.1f) {
                expandableView3.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            } else {
                expandableView3.setFakeShadowIntensity(translationZ2 / 0.1f, expandableView2.getOutlineAlpha(), (int) (((expandableView2.getTranslationY() + expandableView2.getActualHeight()) - expandableView3.getTranslationY()) - expandableView2.getExtraBottomPadding()), expandableView2.getOutlineTranslation());
            }
            i2++;
            expandableView2 = expandableView3;
        }
        this.mTmpSortedChildren.clear();
    }

    public void updateDecorViews(boolean z) {
        if (z == this.mUsingLightTheme) {
            return;
        }
        this.mUsingLightTheme = z;
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(new ContextThemeWrapper(((ViewGroup) this).mContext, z ? R.style.Theme_SystemUI_Light : R.style.Theme_SystemUI), R.attr.wallpaperTextColor);
        this.mSectionsManager.setHeaderForegroundColor(colorAttrDefaultColor);
        this.mFooterView.setTextColor(colorAttrDefaultColor);
        this.mEmptyShadeView.setTextColor(colorAttrDefaultColor);
    }

    public void goToFullShade(long j) {
        this.mGoToFullShadeNeedsAnimation = true;
        this.mGoToFullShadeDelay = j;
        this.mNeedsAnimation = true;
        requestChildrenUpdate();
    }

    public void cancelExpandHelper() {
        this.mExpandHelper.cancel();
    }

    public void setIntrinsicPadding(int i) {
        this.mIntrinsicPadding = i;
        this.mAmbientState.setIntrinsicPadding(i);
    }

    public int getIntrinsicPadding() {
        return this.mIntrinsicPadding;
    }

    public void setDozing(boolean z, boolean z2, PointF pointF) {
        if (this.mAmbientState.isDozing() == z) {
            return;
        }
        this.mAmbientState.setDozing(z);
        requestChildrenUpdate();
        notifyHeightChangeListener(this.mShelf);
    }

    public void setHideAmount(float f, float f2) {
        this.mLinearHideAmount = f;
        this.mInterpolatedHideAmount = f2;
        boolean zIsFullyHidden = this.mAmbientState.isFullyHidden();
        boolean zIsHiddenAtAll = this.mAmbientState.isHiddenAtAll();
        this.mAmbientState.setHideAmount(f2);
        boolean zIsFullyHidden2 = this.mAmbientState.isFullyHidden();
        boolean zIsHiddenAtAll2 = this.mAmbientState.isHiddenAtAll();
        if (zIsFullyHidden2 != zIsFullyHidden) {
            updateVisibility();
        }
        if (!zIsHiddenAtAll && zIsHiddenAtAll2) {
            resetExposedMenuView(true, true);
        }
        if (zIsFullyHidden2 != zIsFullyHidden || zIsHiddenAtAll != zIsHiddenAtAll2) {
            invalidateOutline();
        }
        updateAlgorithmHeightAndPadding();
        updateBackgroundDimming();
        requestChildrenUpdate();
        updateOwnTranslationZ();
    }

    private void updateOwnTranslationZ() {
        ExpandableView firstChildNotGone;
        setTranslationZ((this.mKeyguardBypassController.getBypassEnabled() && this.mAmbientState.isHiddenAtAll() && (firstChildNotGone = getFirstChildNotGone()) != null && firstChildNotGone.showingPulsing()) ? firstChildNotGone.getTranslationZ() : 0.0f);
    }

    private void updateVisibility() {
        setVisibility(!this.mAmbientState.isFullyHidden() || !onKeyguard() ? 0 : 4);
    }

    public void notifyHideAnimationStart(boolean z) {
        Interpolator interpolator;
        float f = this.mInterpolatedHideAmount;
        if (f == 0.0f || f == 1.0f) {
            this.mBackgroundXFactor = z ? 1.8f : 1.5f;
            if (z) {
                interpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
            } else {
                interpolator = Interpolators.FAST_OUT_SLOW_IN;
            }
            this.mHideXInterpolator = interpolator;
        }
    }

    public void setFooterView(FooterView footerView) {
        int iIndexOfChild;
        FooterView footerView2 = this.mFooterView;
        if (footerView2 != null) {
            iIndexOfChild = indexOfChild(footerView2);
            removeView(this.mFooterView);
        } else {
            iIndexOfChild = -1;
        }
        this.mFooterView = footerView;
        addView(footerView, iIndexOfChild);
    }

    public void setEmptyShadeView(EmptyShadeView emptyShadeView) {
        int iIndexOfChild;
        EmptyShadeView emptyShadeView2 = this.mEmptyShadeView;
        if (emptyShadeView2 != null) {
            iIndexOfChild = indexOfChild(emptyShadeView2);
            removeView(this.mEmptyShadeView);
        } else {
            iIndexOfChild = -1;
        }
        this.mEmptyShadeView = emptyShadeView;
        addView(emptyShadeView, iIndexOfChild);
    }

    public void updateEmptyShadeView(boolean z) {
        this.mEmptyShadeView.setVisible(z, this.mIsExpanded && this.mAnimationsEnabled);
        int textResource = this.mEmptyShadeView.getTextResource();
        int i = this.mZenController.areNotificationsHiddenInShade() ? R.string.dnd_suppressing_shade_text : R.string.empty_shade_text;
        if (textResource != i) {
            this.mEmptyShadeView.setText(i);
        }
    }

    public void updateFooterView(boolean z, boolean z2, boolean z3) {
        FooterView footerView = this.mFooterView;
        if (footerView == null) {
            return;
        }
        boolean z4 = this.mIsExpanded && this.mAnimationsEnabled;
        footerView.setVisible(z, z4);
        this.mFooterView.setSecondaryVisible(!this.mShowDimissButton && z2, z4);
        this.mFooterView.showHistory(z3);
    }

    public void setDismissAllInProgress(boolean z) {
        this.mDismissAllInProgress = z;
        this.mAmbientState.setDismissAllInProgress(z);
        handleDismissAllClipping();
    }

    private void handleDismissAllClipping() {
        int childCount = getChildCount();
        boolean zCanChildBeDismissed = false;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                if (this.mDismissAllInProgress && zCanChildBeDismissed) {
                    expandableView.setMinClipTopAmount(expandableView.getClipTopAmount());
                } else {
                    expandableView.setMinClipTopAmount(0);
                }
                zCanChildBeDismissed = canChildBeDismissed(expandableView);
            }
        }
    }

    public boolean isFooterViewNotGone() {
        FooterView footerView = this.mFooterView;
        return (footerView == null || footerView.getVisibility() == 8 || this.mFooterView.willBeGone()) ? false : true;
    }

    public boolean isFooterViewContentVisible() {
        FooterView footerView = this.mFooterView;
        return footerView != null && footerView.isContentVisible();
    }

    public int getFooterViewHeightWithPadding() {
        FooterView footerView = this.mFooterView;
        if (footerView == null) {
            return 0;
        }
        return this.mGapHeight + footerView.getHeight() + this.mPaddingBetweenElements;
    }

    public int getEmptyShadeViewHeight() {
        return this.mEmptyShadeView.getHeight();
    }

    public float getBottomMostNotificationBottom() {
        int childCount = getChildCount();
        float f = 0.0f;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                float translationY = (expandableView.getTranslationY() + expandableView.getActualHeight()) - expandableView.getClipBottomAmount();
                if (translationY > f) {
                    f = translationY;
                }
            }
        }
        return f + getStackTranslation();
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
        notificationGroupManager.addOnGroupChangeListener(this.mOnGroupChangeListener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestAnimateEverything() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mEverythingNeedsAnimation = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public boolean isBelowLastNotification(float f, float f2) {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) getChildAt(childCount);
            if (expandableView.getVisibility() != 8) {
                float y = expandableView.getY();
                if (y > f2) {
                    return false;
                }
                boolean z = f2 > (((float) expandableView.getActualHeight()) + y) - ((float) expandableView.getClipBottomAmount());
                FooterView footerView = this.mFooterView;
                if (expandableView == footerView) {
                    if (!z && !footerView.isOnEmptySpace(f - footerView.getX(), f2 - y)) {
                        return false;
                    }
                } else {
                    if (expandableView == this.mEmptyShadeView) {
                        return true;
                    }
                    if (!z) {
                        return false;
                    }
                }
            }
        }
        return f2 > ((float) this.mTopPadding) + this.mStackTranslation;
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEventInternal(accessibilityEvent);
        accessibilityEvent.setScrollable(this.mScrollable);
        accessibilityEvent.setMaxScrollX(((ViewGroup) this).mScrollX);
        accessibilityEvent.setScrollY(this.mOwnScrollY);
        accessibilityEvent.setMaxScrollY(getScrollRange());
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        if (this.mScrollable) {
            accessibilityNodeInfo.setScrollable(true);
            if (this.mBackwardScrollable) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
            }
            if (this.mForwardScrollable) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
            }
        }
        accessibilityNodeInfo.setClassName(ScrollView.class.getName());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void generateChildOrderChangedEvent() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mGenerateChildOrderChangedEvent = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public int getContainerChildCount() {
        return getChildCount();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public View getContainerChildAt(int i) {
        return getChildAt(i);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void removeContainerView(View view) {
        Assert.isMainThread();
        removeView(view);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void removeListItem(NotificationListItem notificationListItem) {
        removeContainerView(notificationListItem.getView());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void addContainerView(View view) {
        Assert.isMainThread();
        addView(view);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void addListItem(NotificationListItem notificationListItem) {
        addContainerView(notificationListItem.getView());
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mAnimationFinishedRunnables.add(runnable);
    }

    public void generateHeadsUpAnimation(NotificationEntry notificationEntry, boolean z) {
        generateHeadsUpAnimation(notificationEntry.getHeadsUpAnimationView(), z);
    }

    public void generateHeadsUpAnimation(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        if (this.mAnimationsEnabled) {
            if (z || this.mHeadsUpGoingAwayAnimationsAllowed) {
                this.mHeadsUpChangeAnimations.add(new Pair<>(expandableNotificationRow, Boolean.valueOf(z)));
                this.mNeedsAnimation = true;
                if (!this.mIsExpanded && !this.mWillExpand && !z) {
                    expandableNotificationRow.setHeadsUpAnimatingAway(true);
                }
                requestChildrenUpdate();
            }
        }
    }

    public void setHeadsUpBoundaries(int i, int i2) {
        this.mAmbientState.setMaxHeadsUpTranslation(i - i2);
        this.mStateAnimator.setHeadsUpAppearHeightBottom(i);
        requestChildrenUpdate();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setWillExpand(boolean z) {
        this.mWillExpand = z;
    }

    public void setTrackingHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        this.mAmbientState.setTrackedHeadsUpRow(expandableNotificationRow);
        this.mTrackingHeadsUp = expandableNotificationRow != null;
        this.mRoundnessManager.setTrackingHeadsUp(expandableNotificationRow);
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
        scrimController.setScrimBehindChangeRunnable(new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateBackgroundDimming();
            }
        });
    }

    public void forceNoOverlappingRendering(boolean z) {
        this.mForceNoOverlappingRendering = z;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return !this.mForceNoOverlappingRendering && super.hasOverlappingRendering();
    }

    public void setAnimationRunning(boolean z) {
        if (z != this.mAnimationRunning) {
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mRunningAnimationUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mRunningAnimationUpdater);
            }
            this.mAnimationRunning = z;
            updateContinuousShadowDrawing();
        }
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public void setPulsing(boolean z, boolean z2) {
        if (this.mPulsing || z) {
            this.mPulsing = z;
            this.mAmbientState.setPulsing(z);
            this.mSwipeHelper.setPulsing(z);
            updateNotificationAnimationStates();
            updateAlgorithmHeightAndPadding();
            updateContentHeight();
            requestChildrenUpdate();
            notifyHeightChangeListener(null, z2);
        }
    }

    public void setQsExpanded(boolean z) {
        this.mQsExpanded = z;
        updateAlgorithmLayoutMinHeight();
        updateScrollability();
    }

    public void setQsExpansionFraction(float f) {
        this.mQsExpansionFraction = f;
    }

    private void setOwnScrollY(int i) {
        int i2 = this.mOwnScrollY;
        if (i != i2) {
            int i3 = ((ViewGroup) this).mScrollX;
            onScrollChanged(i3, i, i3, i2);
            this.mOwnScrollY = i;
            updateOnScrollChange();
        }
    }

    private void updateOnScrollChange() {
        updateForwardAndBackwardScrollability();
        requestChildrenUpdate();
    }

    public void setShelf(NotificationShelf notificationShelf) {
        int iIndexOfChild;
        NotificationShelf notificationShelf2 = this.mShelf;
        if (notificationShelf2 != null) {
            iIndexOfChild = indexOfChild(notificationShelf2);
            removeView(this.mShelf);
        } else {
            iIndexOfChild = -1;
        }
        this.mShelf = notificationShelf;
        addView(notificationShelf, iIndexOfChild);
        this.mAmbientState.setShelf(notificationShelf);
        this.mStateAnimator.setShelf(notificationShelf);
        notificationShelf.bind(this.mAmbientState, this);
    }

    public NotificationShelf getNotificationShelf() {
        return this.mShelf;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setMaxDisplayedNotifications(int i) {
        if (this.mMaxDisplayedNotifications != i) {
            this.mMaxDisplayedNotifications = i;
            updateContentHeight();
            notifyHeightChangeListener(this.mShelf);
        }
    }

    public void setShouldShowShelfOnly(boolean z) {
        this.mShouldShowShelfOnly = z;
        updateAlgorithmLayoutMinHeight();
    }

    public int getMinExpansionHeight() {
        int intrinsicHeight = this.mShelf.getIntrinsicHeight();
        int intrinsicHeight2 = this.mShelf.getIntrinsicHeight() - this.mStatusBarHeight;
        int i = this.mWaterfallTopInset;
        return (intrinsicHeight - ((intrinsicHeight2 + i) / 2)) + i;
    }

    public void setInHeadsUpPinnedMode(boolean z) {
        this.mInHeadsUpPinnedMode = z;
        updateClipping();
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        updateClipping();
    }

    @VisibleForTesting
    protected void setStatusBarState(int i) {
        this.mStatusBarState = i;
        this.mAmbientState.setStatusBarState(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onStatePostChange() {
        boolean zOnKeyguard = onKeyguard();
        HeadsUpAppearanceController headsUpAppearanceController = this.mHeadsUpAppearanceController;
        if (headsUpAppearanceController != null) {
            headsUpAppearanceController.onStateChanged();
        }
        SysuiStatusBarStateController sysuiStatusBarStateController = (SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class);
        updateSensitiveness(sysuiStatusBarStateController.goingToFullShade());
        setDimmed(zOnKeyguard, sysuiStatusBarStateController.fromShadeLocked());
        setExpandingEnabled(!zOnKeyguard);
        ActivatableNotificationView activatedChild = getActivatedChild();
        setActivatedChild(null);
        if (activatedChild != null) {
            activatedChild.makeInactive(false);
        }
        updateFooter();
        requestChildrenUpdate();
        onUpdateRowStates();
        this.mEntryManager.updateNotifications("StatusBar state changed");
        updateVisibility();
    }

    public void setExpandingVelocity(float f) {
        this.mAmbientState.setExpandingVelocity(f);
    }

    public float getOpeningHeight() {
        if (this.mEmptyShadeView.getVisibility() == 8) {
            return getMinExpansionHeight();
        }
        return getAppearEndPosition();
    }

    public void setIsFullWidth(boolean z) {
        this.mAmbientState.setPanelFullWidth(z);
    }

    public void setUnlockHintRunning(boolean z) {
        this.mAmbientState.setUnlockHintRunning(z);
    }

    public void setQsCustomizerShowing(boolean z) {
        this.mAmbientState.setQsCustomizerShowing(z);
        requestChildrenUpdate();
    }

    public void setHeadsUpGoingAwayAnimationsAllowed(boolean z) {
        this.mHeadsUpGoingAwayAnimationsAllowed = z;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        Object[] objArr = new Object[9];
        objArr[0] = getClass().getSimpleName();
        objArr[1] = this.mPulsing ? "T" : "f";
        objArr[2] = this.mAmbientState.isQsCustomizerShowing() ? "T" : "f";
        if (getVisibility() == 0) {
            str = "visible";
        } else {
            str = getVisibility() == 8 ? "gone" : "invisible";
        }
        objArr[3] = str;
        objArr[4] = Float.valueOf(getAlpha());
        objArr[5] = Integer.valueOf(this.mAmbientState.getScrollY());
        objArr[6] = Integer.valueOf(this.mMaxTopPadding);
        objArr[7] = this.mShouldShowShelfOnly ? "T" : "f";
        objArr[8] = Float.valueOf(this.mQsExpansionFraction);
        printWriter.println(String.format("[%s: pulsing=%s qsCustomizerShowing=%s visibility=%s alpha:%f scrollY:%d maxTopPadding:%d showShelfOnly=%s qsExpandFraction=%f]", objArr));
        int childCount = getChildCount();
        printWriter.println("  Number of children: " + childCount);
        printWriter.println();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            expandableView.dump(fileDescriptor, printWriter, strArr);
            if (!(expandableView instanceof ExpandableNotificationRow)) {
                printWriter.println("  " + expandableView.getClass().getSimpleName());
                ExpandableViewState viewState = expandableView.getViewState();
                if (viewState == null) {
                    printWriter.println("    no viewState!!!");
                } else {
                    printWriter.print("    ");
                    viewState.dump(fileDescriptor, printWriter, strArr);
                    printWriter.println();
                    printWriter.println();
                }
            }
        }
        int transientViewCount = getTransientViewCount();
        printWriter.println("  Transient Views: " + transientViewCount);
        for (int i2 = 0; i2 < transientViewCount; i2++) {
            ((ExpandableView) getTransientView(i2)).dump(fileDescriptor, printWriter, strArr);
        }
        ArrayList<ExpandableView> draggedViews = this.mAmbientState.getDraggedViews();
        int size = draggedViews.size();
        printWriter.println("  Dragged Views: " + size);
        for (int i3 = 0; i3 < size; i3++) {
            draggedViews.get(i3).dump(fileDescriptor, printWriter, strArr);
        }
    }

    public boolean isFullyHidden() {
        return this.mAmbientState.isFullyHidden();
    }

    public void addOnExpandedHeightChangedListener(BiConsumer<Float, Float> biConsumer) {
        this.mExpandedHeightListeners.add(biConsumer);
    }

    public void removeOnExpandedHeightChangedListener(BiConsumer<Float, Float> biConsumer) {
        this.mExpandedHeightListeners.remove(biConsumer);
    }

    public void setHeadsUpAppearanceController(HeadsUpAppearanceController headsUpAppearanceController) {
        this.mHeadsUpAppearanceController = headsUpAppearanceController;
    }

    public void setIconAreaController(NotificationIconAreaController notificationIconAreaController) {
        this.mIconAreaController = notificationIconAreaController;
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0052  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0094 A[SYNTHETIC] */
    @com.android.internal.annotations.VisibleForTesting
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    void clearNotifications(final int r12, boolean r13) {
        /*
            r11 = this;
            int r0 = r11.getChildCount()
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>(r0)
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>(r0)
            r3 = 0
            r4 = r3
        L10:
            if (r4 >= r0) goto L98
            android.view.View r5 = r11.getChildAt(r4)
            boolean r6 = r5 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r6 == 0) goto L94
            r6 = r5
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r6 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r6
            android.graphics.Rect r7 = r11.mTmpRect
            boolean r7 = r5.getClipBounds(r7)
            boolean r8 = r11.includeChildInDismissAll(r6, r12)
            r9 = 1
            if (r8 == 0) goto L41
            r2.add(r6)
            int r8 = r5.getVisibility()
            if (r8 != 0) goto L52
            if (r7 == 0) goto L3d
            android.graphics.Rect r7 = r11.mTmpRect
            int r7 = r7.height()
            if (r7 <= 0) goto L52
        L3d:
            r1.add(r5)
            goto L53
        L41:
            int r5 = r5.getVisibility()
            if (r5 != 0) goto L52
            if (r7 == 0) goto L53
            android.graphics.Rect r5 = r11.mTmpRect
            int r5 = r5.height()
            if (r5 <= 0) goto L52
            goto L53
        L52:
            r9 = r3
        L53:
            java.util.List r5 = r6.getAttachedChildren()
            if (r5 == 0) goto L94
            java.util.Iterator r5 = r5.iterator()
        L5d:
            boolean r7 = r5.hasNext()
            if (r7 == 0) goto L94
            java.lang.Object r7 = r5.next()
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r7 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r7
            boolean r8 = r11.includeChildInDismissAll(r6, r12)
            if (r8 == 0) goto L5d
            r2.add(r7)
            if (r9 == 0) goto L5d
            boolean r8 = r6.areChildrenExpanded()
            if (r8 == 0) goto L5d
            android.graphics.Rect r8 = r11.mTmpRect
            boolean r8 = r7.getClipBounds(r8)
            int r10 = r7.getVisibility()
            if (r10 != 0) goto L5d
            if (r8 == 0) goto L90
            android.graphics.Rect r8 = r11.mTmpRect
            int r8 = r8.height()
            if (r8 <= 0) goto L5d
        L90:
            r1.add(r7)
            goto L5d
        L94:
            int r4 = r4 + 1
            goto L10
        L98:
            com.android.internal.logging.UiEventLogger r0 = r11.mUiEventLogger
            com.android.internal.logging.UiEventLogger$UiEventEnum r4 = com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.NotificationPanelEvent.fromSelection(r12)
            r0.log(r4)
            boolean r0 = r2.isEmpty()
            if (r0 == 0) goto Lb5
            if (r13 == 0) goto Lb4
            java.lang.Class<com.android.systemui.statusbar.phone.ShadeController> r11 = com.android.systemui.statusbar.phone.ShadeController.class
            java.lang.Object r11 = com.android.systemui.Dependency.get(r11)
            com.android.systemui.statusbar.phone.ShadeController r11 = (com.android.systemui.statusbar.phone.ShadeController) r11
            r11.animateCollapsePanels(r3)
        Lb4:
            return
        Lb5:
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda16 r0 = new com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda16
            r0.<init>()
            r11.performDismissAllAnimations(r1, r13, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.clearNotifications(int, boolean):void");
    }

    private boolean includeChildInDismissAll(ExpandableNotificationRow expandableNotificationRow, int i) {
        return canChildBeDismissed(expandableNotificationRow) && matchesSelection(expandableNotificationRow, i);
    }

    private void performDismissAllAnimations(ArrayList<View> arrayList, final boolean z, final Runnable runnable) {
        Runnable runnable2 = new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$performDismissAllAnimations$10(z, runnable);
            }
        };
        if (arrayList.isEmpty()) {
            runnable2.run();
            return;
        }
        setDismissAllInProgress(true);
        int iMax = 140;
        int i = 180;
        int size = arrayList.size() - 1;
        while (size >= 0) {
            dismissViewAnimated(arrayList.get(size), size == 0 ? runnable2 : null, i, 260L);
            iMax = Math.max(50, iMax - 10);
            i += iMax;
            size--;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performDismissAllAnimations$10(boolean z, final Runnable runnable) {
        if (z) {
            ((ShadeController) Dependency.get(ShadeController.class)).addPostCollapseAction(new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda15
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$performDismissAllAnimations$9(runnable);
                }
            });
            ((ShadeController) Dependency.get(ShadeController.class)).animateCollapsePanels(0);
        } else {
            setDismissAllInProgress(false);
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performDismissAllAnimations$9(Runnable runnable) {
        setDismissAllInProgress(false);
        runnable.run();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setNotificationActivityStarter(NotificationActivityStarter notificationActivityStarter) {
        this.mNotificationActivityStarter = notificationActivityStarter;
    }

    @VisibleForTesting
    protected void inflateFooterView() {
        FooterView footerView = (FooterView) LayoutInflater.from(((ViewGroup) this).mContext).inflate(R.layout.status_bar_notification_footer, (ViewGroup) this, false);
        footerView.setDismissButtonClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$inflateFooterView$11(view);
            }
        });
        footerView.setManageButtonClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$inflateFooterView$12(view);
            }
        });
        StatusBar statusBar = this.mStatusBar;
        if (statusBar != null && statusBar.getDismissAllButton() != null) {
            this.mStatusBar.getDismissAllButton().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda4
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$inflateFooterView$13(view);
                }
            });
        }
        setFooterView(footerView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inflateFooterView$11(View view) {
        if (this.mShowDimissButton) {
            return;
        }
        this.mMetricsLogger.action(148);
        clearNotifications(0, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inflateFooterView$12(View view) {
        this.mNotificationActivityStarter.startHistoryIntent(this.mFooterView.isHistoryShown());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inflateFooterView$13(View view) {
        if (this.mShowDimissButton) {
            this.mMetricsLogger.action(148);
            clearNotifications(0, true);
        }
    }

    private void inflateEmptyShadeView() {
        EmptyShadeView emptyShadeView = (EmptyShadeView) LayoutInflater.from(((ViewGroup) this).mContext).inflate(R.layout.status_bar_no_notifications, (ViewGroup) this, false);
        emptyShadeView.setText(R.string.empty_shade_text);
        emptyShadeView.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$inflateEmptyShadeView$14(view);
            }
        });
        setEmptyShadeView(emptyShadeView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inflateEmptyShadeView$14(View view) {
        Intent intent;
        if (Settings.Secure.getIntForUser(((ViewGroup) this).mContext.getContentResolver(), "notification_history_enabled", 0, -2) == 1) {
            intent = new Intent("android.settings.NOTIFICATION_HISTORY");
        } else {
            intent = new Intent("android.settings.NOTIFICATION_SETTINGS");
        }
        this.mStatusBar.startActivity(intent, true, true, 536870912);
    }

    public void onUpdateRowStates() {
        ForegroundServiceDungeonView foregroundServiceDungeonView = this.mFgsSectionView;
        int i = 1;
        if (foregroundServiceDungeonView != null) {
            changeViewPosition(foregroundServiceDungeonView, getChildCount() - 1);
            i = 2;
        }
        int i2 = i + 1;
        changeViewPosition(this.mFooterView, getChildCount() - i);
        changeViewPosition(this.mEmptyShadeView, getChildCount() - i2);
        changeViewPosition(this.mShelf, getChildCount() - (i2 + 1));
    }

    public void setNotificationPanelController(NotificationPanelViewController notificationPanelViewController) {
        this.mNotificationPanelController = notificationPanelViewController;
    }

    public void updateIconAreaViews() {
        this.mIconAreaController.updateNotificationIcons();
    }

    public float setPulseHeight(float f) {
        this.mAmbientState.setPulseHeight(f);
        if (this.mKeyguardBypassController.getBypassEnabled()) {
            notifyAppearChangedListeners();
        }
        requestChildrenUpdate();
        return Math.max(0.0f, f - this.mAmbientState.getInnerHeight(true));
    }

    public float getPulseHeight() {
        return this.mAmbientState.getPulseHeight();
    }

    public void setDozeAmount(float f) {
        this.mAmbientState.setDozeAmount(f);
        updateContinuousBackgroundDrawing();
        requestChildrenUpdate();
    }

    public void wakeUpFromPulse() {
        setPulseHeight(getWakeUpHeight());
        int childCount = getChildCount();
        float translationY = -1.0f;
        boolean z = true;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                boolean z2 = expandableView == this.mShelf;
                if ((expandableView instanceof ExpandableNotificationRow) || z2) {
                    if (expandableView.getVisibility() != 0 || z2) {
                        if (!z) {
                            expandableView.setTranslationY(translationY);
                        }
                    } else if (z) {
                        translationY = (expandableView.getTranslationY() + expandableView.getActualHeight()) - this.mShelf.getIntrinsicHeight();
                        z = false;
                    }
                }
            }
        }
        this.mDimmedNeedsAnimation = true;
    }

    @Override // com.android.systemui.statusbar.notification.DynamicPrivacyController.Listener
    public void onDynamicPrivacyChanged() {
        if (this.mIsExpanded) {
            this.mAnimateBottomOnLayout = true;
        }
        post(new Runnable() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onDynamicPrivacyChanged$15();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onDynamicPrivacyChanged$15() {
        updateFooter();
        updateSectionBoundaries("dynamic privacy changed");
    }

    public void setOnPulseHeightChangedListener(Runnable runnable) {
        this.mAmbientState.setOnPulseHeightChangedListener(runnable);
    }

    public float calculateAppearFractionBypass() {
        return MathUtils.smoothStep(0.0f, getIntrinsicPadding(), getPulseHeight() - getWakeUpHeight());
    }

    public void setCurrentUserSetup(boolean z) {
        if (this.mIsCurrentUserSetup != z) {
            this.mIsCurrentUserSetup = z;
            updateFooter();
        }
    }

    public void updateSpeedBumpIndex() {
        int childCount = getChildCount();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            boolean zIsAmbient = true;
            if (i >= childCount) {
                break;
            }
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                i3++;
                if (this.mHighPriorityBeforeSpeedBump) {
                    if (expandableNotificationRow.getEntry().getBucket() >= 6) {
                        zIsAmbient = false;
                    }
                } else {
                    zIsAmbient = true ^ expandableNotificationRow.getEntry().isAmbient();
                }
                if (zIsAmbient) {
                    i2 = i3;
                }
            }
            i++;
        }
        updateSpeedBumpIndex(i2, i2 == childCount);
    }

    public void updateSectionBoundaries(String str) {
        this.mSectionsManager.updateSectionBoundaries(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateContinuousBackgroundDrawing() {
        boolean z = (this.mAmbientState.isFullyAwake() || this.mAmbientState.getDraggedViews().isEmpty()) ? false : true;
        if (z != this.mContinuousBackgroundUpdate) {
            this.mContinuousBackgroundUpdate = z;
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mBackgroundUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mBackgroundUpdater);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateContinuousShadowDrawing() {
        boolean z = this.mAnimationRunning || !this.mAmbientState.getDraggedViews().isEmpty();
        if (z != this.mContinuousShadowUpdate) {
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mShadowUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mShadowUpdater);
            }
            this.mContinuousShadowUpdate = z;
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void resetExposedMenuView(boolean z, boolean z2) {
        this.mSwipeHelper.resetExposedMenuView(z, z2);
    }

    private static boolean matchesSelection(ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i == 0) {
            return true;
        }
        if (i == 1) {
            return expandableNotificationRow.getEntry().getBucket() < 6;
        }
        if (i == 2) {
            return expandableNotificationRow.getEntry().getBucket() == 6;
        }
        throw new IllegalArgumentException("Unknown selection: " + i);
    }

    static class AnimationEvent {
        static AnimationFilter[] FILTERS = {new AnimationFilter().animateAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateDimmed().animateZ(), new AnimationFilter().animateZ(), new AnimationFilter().animateDimmed(), new AnimationFilter().animateAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateDimmed().animateZ().hasDelays(), new AnimationFilter().animateHideSensitive(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateAlpha().animateDimmed().animateHideSensitive().animateHeight().animateTopInset().animateY().animateZ()};
        static int[] LENGTHS = {464, 464, 360, 360, 220, 220, 360, 448, 360, 360, 360, 550, 300, 300, 360, 360};
        final int animationType;
        final long eventStartTime;
        final AnimationFilter filter;
        boolean headsUpFromBottom;
        final long length;
        final ExpandableView mChangingView;
        View viewAfterChangingView;

        AnimationEvent(ExpandableView expandableView, int i) {
            this(expandableView, i, LENGTHS[i]);
        }

        AnimationEvent(ExpandableView expandableView, int i, long j) {
            this(expandableView, i, j, FILTERS[i]);
        }

        AnimationEvent(ExpandableView expandableView, int i, long j, AnimationFilter animationFilter) {
            this.eventStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mChangingView = expandableView;
            this.animationType = i;
            this.length = j;
            this.filter = animationFilter;
        }

        static long combineLength(ArrayList<AnimationEvent> arrayList) {
            int size = arrayList.size();
            long jMax = 0;
            for (int i = 0; i < size; i++) {
                AnimationEvent animationEvent = arrayList.get(i);
                jMax = Math.max(jMax, animationEvent.length);
                if (animationEvent.animationType == 7) {
                    return animationEvent.length;
                }
            }
            return jMax;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean canChildBeDismissed(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.isBlockingHelperShowingAndTranslationFinished()) {
                return true;
            }
            if (expandableNotificationRow.areGutsExposed() || !expandableNotificationRow.getEntry().hasFinishedInitialization()) {
                return false;
            }
            return expandableNotificationRow.canViewBeDismissed();
        }
        if (view instanceof PeopleHubView) {
            return ((PeopleHubView) view).getCanSwipe();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEntryUpdated(NotificationEntry notificationEntry) {
        if (!notificationEntry.rowExists() || notificationEntry.getSbn().isClearable()) {
            return;
        }
        snapViewIfNeeded(notificationEntry);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean hasActiveNotifications() {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return !this.mNotifPipeline.getShadeList().isEmpty();
        }
        return this.mEntryManager.hasVisibleNotifications();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: onDismissAllAnimationsEnd, reason: merged with bridge method [inline-methods] */
    public void lambda$clearNotifications$8(List<ExpandableNotificationRow> list, int i) {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            if (i == 0) {
                this.mNotifCollection.dismissAllNotifications(this.mLockscreenUserManager.getCurrentUserId());
                return;
            }
            ArrayList arrayList = new ArrayList();
            new ArrayList();
            int shadeListCount = this.mNotifPipeline.getShadeListCount();
            for (int i2 = 0; i2 < list.size(); i2++) {
                NotificationEntry entry = list.get(i2).getEntry();
                arrayList.add(new Pair(entry, new DismissedByUserStats(3, 1, NotificationVisibility.obtain(entry.getKey(), entry.getRanking().getRank(), shadeListCount, true, NotificationLogger.getNotificationLocation(entry)))));
            }
            this.mNotifCollection.dismissNotifications(arrayList);
            return;
        }
        for (ExpandableNotificationRow expandableNotificationRow : list) {
            if (!canChildBeDismissed(expandableNotificationRow)) {
                expandableNotificationRow.resetTranslation();
            } else if (i == 0) {
                this.mEntryManager.removeNotification(expandableNotificationRow.getEntry().getKey(), null, 3);
            } else {
                this.mEntryManager.performRemoveNotification(expandableNotificationRow.getEntry().getSbn(), 3);
            }
        }
        if (i == 0) {
            try {
                this.mBarService.onClearAllNotifications(this.mLockscreenUserManager.getCurrentUserId());
            } catch (Exception unused) {
            }
        }
    }

    /* renamed from: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$17, reason: invalid class name */
    class AnonymousClass17 implements DragDownHelper.DragDownCallback {
        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ boolean lambda$onDraggedDown$0() {
            return false;
        }

        AnonymousClass17() {
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public boolean onDraggedDown(View view, int i) {
            boolean z = NotificationStackScrollLayout.this.hasActiveNotifications() || NotificationStackScrollLayout.this.mKeyguardMediaController.getView().getVisibility() == 0;
            if (NotificationStackScrollLayout.this.mStatusBarState != 1 || !z) {
                if (!NotificationStackScrollLayout.this.mDynamicPrivacyController.isInLockedDownShade()) {
                    return false;
                }
                NotificationStackScrollLayout.this.mStatusbarStateController.setLeaveOpenOnKeyguardHide(true);
                NotificationStackScrollLayout.this.mStatusBar.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$17$$ExternalSyntheticLambda0
                    @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                    public final boolean onDismiss() {
                        return NotificationStackScrollLayout.AnonymousClass17.lambda$onDraggedDown$0();
                    }
                }, null, false);
                return true;
            }
            NotificationStackScrollLayout.this.mLockscreenGestureLogger.write(187, (int) (i / NotificationStackScrollLayout.this.mDisplayMetrics.density), 0);
            NotificationStackScrollLayout.this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_PULL_SHADE_OPEN);
            if (!NotificationStackScrollLayout.this.mAmbientState.isDozing() || view != null) {
                ((ShadeController) Dependency.get(ShadeController.class)).goToLockedShade(view);
                if (view instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) view).onExpandedByGesture(true);
                }
            }
            return true;
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public void onDragDownReset() {
            NotificationStackScrollLayout.this.setDimmed(true, true);
            NotificationStackScrollLayout.this.resetScrollPosition();
            NotificationStackScrollLayout.this.resetCheckSnoozeLeavebehind();
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public void onCrossedThreshold(boolean z) {
            NotificationStackScrollLayout.this.setDimmed(!z, true);
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public void onTouchSlopExceeded() {
            NotificationStackScrollLayout.this.cancelLongPress();
            NotificationStackScrollLayout.this.checkSnoozeLeavebehind();
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public void setEmptyDragAmount(float f) {
            NotificationStackScrollLayout.this.mNotificationPanelController.setEmptyDragAmount(f);
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public boolean isFalsingCheckNeeded() {
            return NotificationStackScrollLayout.this.mStatusBarState == 1;
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public boolean isDragDownEnabledForView(ExpandableView expandableView) {
            if (isDragDownAnywhereEnabled()) {
                return true;
            }
            if (!NotificationStackScrollLayout.this.mDynamicPrivacyController.isInLockedDownShade()) {
                return false;
            }
            if (expandableView == null) {
                return true;
            }
            if (expandableView instanceof ExpandableNotificationRow) {
                return ((ExpandableNotificationRow) expandableView).getEntry().isSensitive();
            }
            return false;
        }

        @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
        public boolean isDragDownAnywhereEnabled() {
            return NotificationStackScrollLayout.this.mStatusbarStateController.getState() == 1 && !NotificationStackScrollLayout.this.mKeyguardBypassController.getBypassEnabled();
        }
    }

    public DragDownHelper.DragDownCallback getDragDownCallback() {
        return this.mDragDownCallback;
    }

    public HeadsUpTouchHelper.Callback getHeadsUpCallback() {
        return this.mHeadsUpCallback;
    }

    public ExpandHelper.Callback getExpandHelperCallback() {
        return this.mExpandHelperCallback;
    }

    enum NotificationPanelEvent implements UiEventLogger.UiEventEnum {
        INVALID(0),
        DISMISS_ALL_NOTIFICATIONS_PANEL(312),
        DISMISS_SILENT_NOTIFICATIONS_PANEL(314);

        private final int mId;

        NotificationPanelEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        public static UiEventLogger.UiEventEnum fromSelection(int i) {
            if (i == 0) {
                return DISMISS_ALL_NOTIFICATIONS_PANEL;
            }
            if (i == 2) {
                return DISMISS_SILENT_NOTIFICATIONS_PANEL;
            }
            return INVALID;
        }
    }
}
