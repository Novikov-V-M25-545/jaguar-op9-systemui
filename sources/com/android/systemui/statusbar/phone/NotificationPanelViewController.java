package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.MathUtils;
import android.util.Property;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.phone.NotificationLightsView;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import com.android.systemui.statusbar.notification.ViewGroupFadeHelper;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.KeyguardAffordanceHelper;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.PanelViewController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.InjectionInflationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/* loaded from: classes.dex */
public class NotificationPanelViewController extends PanelViewController {
    private final AnimatableProperty KEYGUARD_HEADS_UP_SHOWING_AMOUNT;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    private final ActivityManager mActivityManager;
    private boolean mAffordanceHasPreview;
    private KeyguardAffordanceHelper mAffordanceHelper;
    private Consumer<Boolean> mAffordanceLaunchListener;
    private boolean mAllowExpandForSmallExpansion;
    private int mAmbientIndicationBottomPadding;
    private boolean mAmbientPulseRanOnce;
    private final Runnable mAnimateKeyguardBottomAreaInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewGoneEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable;
    private boolean mAnimateNextPositionUpdate;
    private boolean mAnimatingQS;
    private int mBarState;
    private ViewGroup mBigClockContainer;
    private final BiometricUnlockController mBiometricUnlockController;
    private boolean mBlockTouches;
    private boolean mBlockingExpansionForCurrentTouch;
    private float mBottomAreaShadeAlpha;
    private final ValueAnimator mBottomAreaShadeAlphaAnimator;
    private final KeyguardClockPositionAlgorithm mClockPositionAlgorithm;
    private final KeyguardClockPositionAlgorithm.Result mClockPositionResult;
    private boolean mClosingWithAlphaFadeOut;
    private boolean mCollapsedOnDown;
    private final CommandQueue mCommandQueue;
    private final ConfigurationController mConfigurationController;
    private final ConfigurationListener mConfigurationListener;
    private boolean mConflictingQsExpansionGesture;
    private final ConversationNotificationManager mConversationNotificationManager;
    private int mDarkIconSize;
    private boolean mDelayShowingKeyguardStatusBar;
    private int mDisplayId;
    private GestureDetector mDoubleTapGesture;
    private boolean mDoubleTapToSleepEnabled;
    private float mDownX;
    private float mDownY;
    private final DozeParameters mDozeParameters;
    private boolean mDozing;
    private boolean mDozingOnDown;
    private float mEmptyDragAmount;
    private final NotificationEntryManager mEntryManager;
    private Runnable mExpandAfterLayoutRunnable;
    private float mExpandOffset;
    private boolean mExpandingFromHeadsUp;
    private final ExpansionCallback mExpansionCallback;
    private boolean mExpectingSynthesizedDown;
    private FalsingManager mFalsingManager;
    private boolean mFirstBypassAttempt;
    private FlingAnimationUtils mFlingAnimationUtils;
    private final FlingAnimationUtils.Builder mFlingAnimationUtilsBuilder;
    private final FragmentHostManager.FragmentListener mFragmentListener;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private HeadsUpAppearanceController mHeadsUpAppearanceController;
    private Runnable mHeadsUpExistenceChangedRunnable;
    private int mHeadsUpInset;
    private boolean mHeadsUpPinnedMode;
    private HeadsUpTouchHelper mHeadsUpTouchHelper;
    private final HeightListener mHeightListener;
    private boolean mHideIconsDuringNotificationLaunch;
    private int mIndicationBottomPadding;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private final InjectionInflationController mInjectionInflationController;
    private float mInterpolatedDarkAmount;
    private boolean mIsExpanding;
    private boolean mIsFullWidth;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private boolean mIsLockscreenDoubleTapEnabled;
    private boolean mIsPanelCollapseOnQQS;
    private final KeyguardAffordanceHelperCallback mKeyguardAffordanceHelperCallback;
    private final KeyguardBypassController mKeyguardBypassController;
    private float mKeyguardHeadsUpShowingAmount;
    private KeyguardIndicationController mKeyguardIndicationController;
    private boolean mKeyguardShowing;
    private KeyguardStatusBarView mKeyguardStatusBar;
    private float mKeyguardStatusBarAnimateAlpha;
    private KeyguardStatusView mKeyguardStatusView;
    private boolean mKeyguardStatusViewAnimating;

    @VisibleForTesting
    final KeyguardUpdateMonitorCallback mKeyguardUpdateCallback;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private String mLastCameraLaunchSource;
    private boolean mLastEventSynthesizedDown;
    private int mLastOrientation;
    private float mLastOverscroll;
    private Runnable mLaunchAnimationEndRunnable;
    private boolean mLaunchingAffordance;
    private float mLinearDarkAmount;
    private boolean mListenForHeadsUp;
    private LockscreenGestureLogger mLockscreenGestureLogger;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private final MediaHierarchyManager mMediaHierarchyManager;
    private final MetricsLogger mMetricsLogger;
    private int mNavigationBarBottomHeight;
    private NotificationsQuickSettingsContainer mNotificationContainerParent;
    private NotificationStackScrollLayout mNotificationStackScroller;
    private int mNotificationsHeaderCollideDistance;
    private int mOldLayoutDirection;
    private final OnClickListener mOnClickListener;
    private final OnEmptySpaceClickListener mOnEmptySpaceClickListener;
    private final MyOnHeadsUpChangedListener mOnHeadsUpChangedListener;
    private final OnHeightChangedListener mOnHeightChangedListener;
    private final OnOverscrollTopChangedListener mOnOverscrollTopChangedListener;
    private Runnable mOnReinflationListener;
    private int mOneFingerQuickSettingsIntercept;
    private boolean mOnlyAffordanceInThisMotion;
    private int mPanelAlpha;
    private final AnimatableProperty mPanelAlphaAnimator;
    private Runnable mPanelAlphaEndAction;
    private final AnimationProperties mPanelAlphaInPropertiesAnimator;
    private final AnimationProperties mPanelAlphaOutPropertiesAnimator;
    private boolean mPanelExpanded;
    private int mPositionMinSideMargin;
    private final PowerManager mPowerManager;
    private final PulseExpansionHandler mPulseExpansionHandler;
    private NotificationLightsView mPulseLightsView;
    private boolean mPulsing;
    private QS mQs;
    private boolean mQsAnimatorExpand;
    private boolean mQsExpandImmediate;
    private boolean mQsExpanded;
    private boolean mQsExpandedWhenExpandingStarted;
    private ValueAnimator mQsExpansionAnimator;
    private boolean mQsExpansionEnabled;
    private boolean mQsExpansionFromOverscroll;
    private float mQsExpansionHeight;
    private int mQsFalsingThreshold;
    private FrameLayout mQsFrame;
    private boolean mQsFullyExpanded;
    private int mQsMaxExpansionHeight;
    private int mQsMinExpansionHeight;
    private View mQsNavbarScrim;
    private int mQsNotificationTopPadding;
    private int mQsPeekHeight;
    private boolean mQsScrimEnabled;
    private ValueAnimator mQsSizeChangeAnimator;
    private int mQsSmartPullDown;
    private boolean mQsTouchAboveFalsingThreshold;
    private boolean mQsTracking;
    private VelocityTracker mQsVelocityTracker;
    private ScreenDecorations mScreenDecorations;
    private final ShadeController mShadeController;
    private int mShelfHeight;
    private boolean mShowEmptyShadeView;
    private boolean mShowIconsWhenExpanded;
    private boolean mShowLockscreenStatusBar;
    private boolean mShowingKeyguardHeadsUp;
    private int mStackScrollerMeasuringPass;
    private boolean mStackScrollerOverscrolling;
    private final ValueAnimator.AnimatorUpdateListener mStatusBarAnimateAlphaListener;
    private int mStatusBarHeaderHeight;
    private int mStatusBarHeight;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private int mStatusBarMinHeight;
    private boolean mStatusBarShownOnSecureKeyguard;
    private final StatusBarStateListener mStatusBarStateListener;
    private int mThemeResId;
    private ArrayList<Consumer<ExpandableNotificationRow>> mTrackingHeadsUpListeners;
    private int mTrackingPointer;
    private final TunerService mTunerService;
    private boolean mTwoFingerQsExpandPossible;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mUserSetupComplete;
    private ArrayList<Runnable> mVerticalTranslationListener;
    private final NotificationPanelView mView;
    private final NotificationWakeUpCoordinator mWakeUpCoordinator;
    private final ZenModeController mZenModeController;
    private final ZenModeControllerCallback mZenModeControllerCallback;
    private static final Rect M_DUMMY_DIRTY_RECT = new Rect(0, 0, 1, 1);
    private static final Rect EMPTY_RECT = new Rect();
    private static final AnimationProperties CLOCK_ANIMATION_PROPERTIES = new AnimationProperties().setDuration(360);
    private static final AnimationProperties KEYGUARD_HUN_PROPERTIES = new AnimationProperties().setDuration(360);

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(NotificationPanelView notificationPanelView, Float f) {
        setKeyguardHeadsUpShowingAmount(f.floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Float lambda$new$1(NotificationPanelView notificationPanelView) {
        return Float.valueOf(getKeyguardHeadsUpShowingAmount());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2() {
        setHeadsUpAnimatingAway(false);
        notifyBarPanelExpansionChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(Property property) {
        Runnable runnable = this.mPanelAlphaEndAction;
        if (runnable != null) {
            runnable.run();
        }
    }

    public NotificationPanelViewController(NotificationPanelView notificationPanelView, InjectionInflationController injectionInflationController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, PulseExpansionHandler pulseExpansionHandler, DynamicPrivacyController dynamicPrivacyController, KeyguardBypassController keyguardBypassController, FalsingManager falsingManager, ShadeController shadeController, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationEntryManager notificationEntryManager, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, DozeLog dozeLog, DozeParameters dozeParameters, CommandQueue commandQueue, VibratorHelper vibratorHelper, LatencyTracker latencyTracker, PowerManager powerManager, AccessibilityManager accessibilityManager, int i, KeyguardUpdateMonitor keyguardUpdateMonitor, MetricsLogger metricsLogger, ActivityManager activityManager, ZenModeController zenModeController, ConfigurationController configurationController, FlingAnimationUtils.Builder builder, StatusBarTouchableRegionManager statusBarTouchableRegionManager, ConversationNotificationManager conversationNotificationManager, MediaHierarchyManager mediaHierarchyManager, BiometricUnlockController biometricUnlockController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, TunerService tunerService) {
        super(notificationPanelView, falsingManager, dozeLog, keyguardStateController, (SysuiStatusBarStateController) statusBarStateController, vibratorHelper, latencyTracker, builder, statusBarTouchableRegionManager);
        this.mOnHeightChangedListener = new OnHeightChangedListener();
        this.mOnClickListener = new OnClickListener();
        this.mOnOverscrollTopChangedListener = new OnOverscrollTopChangedListener();
        this.mKeyguardAffordanceHelperCallback = new KeyguardAffordanceHelperCallback();
        this.mOnEmptySpaceClickListener = new OnEmptySpaceClickListener();
        this.mOnHeadsUpChangedListener = new MyOnHeadsUpChangedListener();
        this.mHeightListener = new HeightListener();
        this.mZenModeControllerCallback = new ZenModeControllerCallback();
        this.mConfigurationListener = new ConfigurationListener();
        this.mStatusBarStateListener = new StatusBarStateListener();
        this.mExpansionCallback = new ExpansionCallback();
        this.KEYGUARD_HEADS_UP_SHOWING_AMOUNT = AnimatableProperty.from("KEYGUARD_HEADS_UP_SHOWING_AMOUNT", new BiConsumer() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda5
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                this.f$0.lambda$new$0((NotificationPanelView) obj, (Float) obj2);
            }
        }, new Function() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda9
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return this.f$0.lambda$new$1((NotificationPanelView) obj);
            }
        }, R.id.keyguard_hun_animator_tag, R.id.keyguard_hun_animator_end_tag, R.id.keyguard_hun_animator_start_tag);
        this.mKeyguardUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricAuthenticated(int i2, BiometricSourceType biometricSourceType, boolean z) {
                if (NotificationPanelViewController.this.mFirstBypassAttempt && NotificationPanelViewController.this.mUpdateMonitor.isUnlockingWithBiometricAllowed(z)) {
                    NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar = true;
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
                boolean z2 = true;
                if (NotificationPanelViewController.this.mBarState != 1 && NotificationPanelViewController.this.mBarState != 2) {
                    z2 = false;
                }
                if (z || !NotificationPanelViewController.this.mFirstBypassAttempt || !z2 || NotificationPanelViewController.this.mDozing || NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar || NotificationPanelViewController.this.mBiometricUnlockController.isBiometricUnlock()) {
                    return;
                }
                NotificationPanelViewController.this.mFirstBypassAttempt = false;
                NotificationPanelViewController.this.animateKeyguardStatusBarIn(360L);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i2) {
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                notificationPanelViewController.mFirstBypassAttempt = notificationPanelViewController.mKeyguardBypassController.getBypassEnabled();
                NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar = false;
            }
        };
        this.mQsExpansionEnabled = true;
        this.mClockPositionAlgorithm = new KeyguardClockPositionAlgorithm();
        this.mClockPositionResult = new KeyguardClockPositionAlgorithm.Result();
        this.mQsScrimEnabled = true;
        this.mKeyguardStatusBarAnimateAlpha = 1.0f;
        this.mLastOrientation = -1;
        this.mLastCameraLaunchSource = "lockscreen_affordance";
        this.mHeadsUpExistenceChangedRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$2();
            }
        };
        this.mLockscreenGestureLogger = new LockscreenGestureLogger();
        this.mHideIconsDuringNotificationLaunch = true;
        this.mTrackingHeadsUpListeners = new ArrayList<>();
        this.mVerticalTranslationListener = new ArrayList<>();
        AnimatableProperty animatablePropertyFrom = AnimatableProperty.from("panelAlpha", new BiConsumer() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda6
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                ((NotificationPanelView) obj).setPanelAlphaInternal(((Float) obj2).floatValue());
            }
        }, new Function() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda10
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return Float.valueOf(((NotificationPanelView) obj).getCurrentPanelAlpha());
            }
        }, R.id.panel_alpha_animator_tag, R.id.panel_alpha_animator_start_tag, R.id.panel_alpha_animator_end_tag);
        this.mPanelAlphaAnimator = animatablePropertyFrom;
        AnimationProperties duration = new AnimationProperties().setDuration(150L);
        Property property = animatablePropertyFrom.getProperty();
        Interpolator interpolator = Interpolators.ALPHA_OUT;
        this.mPanelAlphaOutPropertiesAnimator = duration.setCustomInterpolator(property, interpolator);
        this.mPanelAlphaInPropertiesAnimator = new AnimationProperties().setDuration(200L).setAnimationEndAction(new Consumer() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda8
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$new$3((Property) obj);
            }
        }).setCustomInterpolator(animatablePropertyFrom.getProperty(), Interpolators.ALPHA_IN);
        this.mKeyguardHeadsUpShowingAmount = 0.0f;
        this.mAmbientPulseRanOnce = false;
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.2
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i2, Bundle bundle) {
                if (i2 == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId() || i2 == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.getId()) {
                    NotificationPanelViewController.this.mStatusBarKeyguardViewManager.showBouncer(true);
                    return true;
                }
                return super.performAccessibilityAction(view, i2, bundle);
            }
        };
        this.mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.8
            @Override // java.lang.Runnable
            public void run() {
                NotificationPanelViewController.this.mKeyguardStatusViewAnimating = false;
                NotificationPanelViewController.this.mKeyguardStatusView.setVisibility(4);
            }
        };
        this.mAnimateKeyguardStatusViewGoneEndRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.9
            @Override // java.lang.Runnable
            public void run() {
                NotificationPanelViewController.this.mKeyguardStatusViewAnimating = false;
                NotificationPanelViewController.this.mKeyguardStatusView.setVisibility(8);
            }
        };
        this.mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.10
            @Override // java.lang.Runnable
            public void run() {
                NotificationPanelViewController.this.mKeyguardStatusViewAnimating = false;
            }
        };
        this.mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.11
            @Override // java.lang.Runnable
            public void run() {
                NotificationPanelViewController.this.mKeyguardStatusBar.setVisibility(4);
                NotificationPanelViewController.this.mKeyguardStatusBar.setAlpha(1.0f);
                NotificationPanelViewController.this.mKeyguardStatusBarAnimateAlpha = 1.0f;
            }
        };
        this.mStatusBarAnimateAlphaListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.13
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelViewController.this.mKeyguardStatusBarAnimateAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                NotificationPanelViewController.this.updateHeaderKeyguardAlpha();
            }
        };
        this.mAnimateKeyguardBottomAreaInvisibleEndRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.14
            @Override // java.lang.Runnable
            public void run() {
                NotificationPanelViewController.this.mKeyguardBottomArea.setVisibility(8);
            }
        };
        this.mFragmentListener = new AnonymousClass18();
        this.mView = notificationPanelView;
        this.mMetricsLogger = metricsLogger;
        this.mActivityManager = activityManager;
        this.mZenModeController = zenModeController;
        this.mConfigurationController = configurationController;
        this.mFlingAnimationUtilsBuilder = builder;
        this.mMediaHierarchyManager = mediaHierarchyManager;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mTunerService = tunerService;
        notificationPanelView.setWillNotDraw(true);
        this.mInjectionInflationController = injectionInflationController;
        this.mFalsingManager = falsingManager;
        this.mPowerManager = powerManager;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        this.mAccessibilityManager = accessibilityManager;
        notificationPanelView.setAccessibilityPaneTitle(determineAccessibilityPaneTitle());
        setPanelAlpha(255, false);
        this.mCommandQueue = commandQueue;
        this.mDisplayId = i;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mDozeParameters = dozeParameters;
        this.mBiometricUnlockController = biometricUnlockController;
        pulseExpansionHandler.setPulseExpandAbortListener(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$4();
            }
        });
        this.mThemeResId = notificationPanelView.getContext().getThemeResId();
        this.mKeyguardBypassController = keyguardBypassController;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mFirstBypassAttempt = keyguardBypassController.getBypassEnabled();
        this.mKeyguardStateController.addCallback(new KeyguardStateController.Callback() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.3
            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardFadingAwayChanged() {
                if (NotificationPanelViewController.this.mKeyguardStateController.isKeyguardFadingAway()) {
                    return;
                }
                NotificationPanelViewController.this.mFirstBypassAttempt = false;
                NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar = false;
            }

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardShowingChanged() {
                NotificationPanelViewController.this.mStatusBar.updateQsExpansionEnabled();
            }
        });
        dynamicPrivacyController.addListener(new DynamicPrivacyControlListener());
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        this.mBottomAreaShadeAlphaAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$new$5(valueAnimator);
            }
        });
        valueAnimatorOfFloat.setDuration(160L);
        valueAnimatorOfFloat.setInterpolator(interpolator);
        this.mDoubleTapGesture = new GestureDetector(notificationPanelView.getContext(), new GestureDetector.SimpleOnGestureListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.4
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (NotificationPanelViewController.this.mPowerManager != null) {
                    NotificationPanelViewController.this.mPowerManager.goToSleep(motionEvent.getEventTime());
                }
                NotificationPanelViewController.this.mQsExpandImmediate = false;
                NotificationPanelViewController.this.requestPanelHeightUpdate();
                NotificationPanelViewController.this.setListening(false);
                return true;
            }
        });
        this.mShadeController = shadeController;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mEntryManager = notificationEntryManager;
        this.mConversationNotificationManager = conversationNotificationManager;
        notificationPanelView.setBackgroundColor(0);
        OnAttachStateChangeListener onAttachStateChangeListener = new OnAttachStateChangeListener();
        notificationPanelView.addOnAttachStateChangeListener(onAttachStateChangeListener);
        if (notificationPanelView.isAttachedToWindow()) {
            onAttachStateChangeListener.onViewAttachedToWindow(notificationPanelView);
        }
        notificationPanelView.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener());
        this.mScreenDecorations = (ScreenDecorations) Dependency.get(ScreenDecorations.class);
        onFinishInflate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$4() {
        QS qs = this.mQs;
        if (qs != null) {
            qs.animateHeaderSlidingOut();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$5(ValueAnimator valueAnimator) {
        this.mBottomAreaShadeAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        updateKeyguardBottomAreaAlpha();
    }

    private void onFinishInflate() {
        loadDimens();
        this.mKeyguardStatusBar = (KeyguardStatusBarView) this.mView.findViewById(R.id.keyguard_header);
        this.mKeyguardStatusView = (KeyguardStatusView) this.mView.findViewById(R.id.keyguard_status_view);
        KeyguardClockSwitch keyguardClockSwitch = (KeyguardClockSwitch) this.mView.findViewById(R.id.keyguard_clock_container);
        ViewGroup viewGroup = (ViewGroup) this.mView.findViewById(R.id.big_clock_container);
        this.mBigClockContainer = viewGroup;
        keyguardClockSwitch.setBigClockContainer(viewGroup);
        this.mNotificationContainerParent = (NotificationsQuickSettingsContainer) this.mView.findViewById(R.id.notification_container_parent);
        NotificationStackScrollLayout notificationStackScrollLayout = (NotificationStackScrollLayout) this.mView.findViewById(R.id.notification_stack_scroller);
        this.mNotificationStackScroller = notificationStackScrollLayout;
        notificationStackScrollLayout.setOnHeightChangedListener(this.mOnHeightChangedListener);
        this.mNotificationStackScroller.setOverscrollTopChangedListener(this.mOnOverscrollTopChangedListener);
        this.mNotificationStackScroller.setOnEmptySpaceClickListener(this.mOnEmptySpaceClickListener);
        final NotificationStackScrollLayout notificationStackScrollLayout2 = this.mNotificationStackScroller;
        Objects.requireNonNull(notificationStackScrollLayout2);
        addTrackingHeadsUpListener(new Consumer() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda7
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                notificationStackScrollLayout2.setTrackingHeadsUp((ExpandableNotificationRow) obj);
            }
        });
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) this.mView.findViewById(R.id.keyguard_bottom_area);
        this.mQsNavbarScrim = this.mView.findViewById(R.id.qs_navbar_scrim);
        this.mLastOrientation = this.mResources.getConfiguration().orientation;
        this.mPulseLightsView = (NotificationLightsView) this.mView.findViewById(R.id.lights_container);
        initBottomArea();
        this.mWakeUpCoordinator.setStackScroller(this.mNotificationStackScroller);
        this.mQsFrame = (FrameLayout) this.mView.findViewById(R.id.qs_frame);
        this.mPulseExpansionHandler.setUp(this.mNotificationStackScroller, this.mExpansionCallback, this.mShadeController);
        this.mWakeUpCoordinator.addListener(new NotificationWakeUpCoordinator.WakeUpListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.5
            @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
            public void onFullyHiddenChanged(boolean z) {
                NotificationPanelViewController.this.updateKeyguardStatusBarForHeadsUp();
            }

            @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
            public void onPulseExpansionChanged(boolean z) {
                if (NotificationPanelViewController.this.mKeyguardBypassController.getBypassEnabled()) {
                    NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                    NotificationPanelViewController.this.updateQSPulseExpansion();
                }
            }
        });
        this.mView.setRtlChangeListener(new NotificationPanelView.RtlChangeListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda2
            @Override // com.android.systemui.statusbar.phone.NotificationPanelView.RtlChangeListener
            public final void onRtlPropertielsChanged(int i) {
                this.f$0.lambda$onFinishInflate$6(i);
            }
        });
        this.mView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$6(int i) {
        if (i != this.mOldLayoutDirection) {
            this.mAffordanceHelper.onRtlPropertiesChanged();
            this.mOldLayoutDirection = i;
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void loadDimens() {
        super.loadDimens();
        this.mFlingAnimationUtils = this.mFlingAnimationUtilsBuilder.reset().setMaxLengthSeconds(0.4f).build();
        this.mStatusBarMinHeight = this.mResources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        this.mQsPeekHeight = this.mResources.getDimensionPixelSize(R.dimen.qs_peek_height);
        this.mNotificationsHeaderCollideDistance = this.mResources.getDimensionPixelSize(R.dimen.header_notifications_collide_distance);
        this.mClockPositionAlgorithm.loadDimens(this.mResources);
        this.mQsFalsingThreshold = this.mResources.getDimensionPixelSize(R.dimen.qs_falsing_threshold);
        this.mPositionMinSideMargin = this.mResources.getDimensionPixelSize(R.dimen.notification_panel_min_side_margin);
        this.mIndicationBottomPadding = this.mResources.getDimensionPixelSize(R.dimen.keyguard_indication_bottom_padding);
        this.mQsNotificationTopPadding = this.mResources.getDimensionPixelSize(R.dimen.qs_notification_padding);
        this.mStatusBarHeaderHeight = this.mResources.getDimensionPixelSize(R.dimen.status_bar_header_height_keyguard);
        this.mStatusBarHeight = this.mResources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_width);
        this.mShelfHeight = this.mResources.getDimensionPixelSize(R.dimen.notification_shelf_height);
        this.mDarkIconSize = this.mResources.getDimensionPixelSize(R.dimen.status_bar_icon_drawing_size_dark);
        this.mHeadsUpInset = this.mResources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height) + this.mResources.getDimensionPixelSize(R.dimen.heads_up_status_bar_padding);
    }

    public boolean hasCustomClock() {
        return this.mKeyguardStatusView.hasCustomClock();
    }

    private void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        this.mKeyguardBottomArea.setStatusBar(statusBar);
    }

    public void setLaunchAffordanceListener(Consumer<Boolean> consumer) {
        this.mAffordanceLaunchListener = consumer;
    }

    public void updateResources() {
        int dimensionPixelSize = this.mResources.getDimensionPixelSize(R.dimen.qs_panel_width);
        int integer = this.mResources.getInteger(R.integer.notification_panel_layout_gravity);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQsFrame.getLayoutParams();
        if (layoutParams.width != dimensionPixelSize || layoutParams.gravity != integer) {
            layoutParams.width = dimensionPixelSize;
            layoutParams.gravity = integer;
            this.mQsFrame.setLayoutParams(layoutParams);
        }
        int dimensionPixelSize2 = this.mResources.getDimensionPixelSize(R.dimen.notification_panel_width);
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mNotificationStackScroller.getLayoutParams();
        if (layoutParams2.width == dimensionPixelSize2 && layoutParams2.gravity == integer) {
            return;
        }
        layoutParams2.width = dimensionPixelSize2;
        layoutParams2.gravity = integer;
        this.mNotificationStackScroller.setLayoutParams(layoutParams2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reInflateViews() {
        updateShowEmptyShadeView();
        int iIndexOfChild = this.mView.indexOfChild(this.mKeyguardStatusView);
        this.mView.removeView(this.mKeyguardStatusView);
        KeyguardStatusView keyguardStatusView = (KeyguardStatusView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mView.getContext())).inflate(R.layout.keyguard_status_view, (ViewGroup) this.mView, false);
        this.mKeyguardStatusView = keyguardStatusView;
        this.mView.addView(keyguardStatusView, iIndexOfChild);
        int iIndexOfChild2 = this.mView.indexOfChild(this.mKeyguardStatusBar);
        this.mView.removeView(this.mKeyguardStatusBar);
        KeyguardStatusBarView keyguardStatusBarView = (KeyguardStatusBarView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mView.getContext())).inflate(R.layout.keyguard_status_bar, (ViewGroup) this.mView, false);
        this.mKeyguardStatusBar = keyguardStatusBarView;
        this.mView.addView(keyguardStatusBarView, iIndexOfChild2);
        QS qs = this.mQs;
        if (qs != null && (qs instanceof QSFragment)) {
            this.mKeyguardStatusBar.setQSPanel(((QSFragment) qs).getQsPanel());
        }
        this.mKeyguardStatusBar.setAlpha(this.mBarState == 1 ? 0.0f : 1.0f);
        this.mKeyguardStatusBar.setVisibility(this.mBarState == 1 ? 0 : 4);
        this.mBigClockContainer.removeAllViews();
        ((KeyguardClockSwitch) this.mView.findViewById(R.id.keyguard_clock_container)).setBigClockContainer(this.mBigClockContainer);
        int iIndexOfChild3 = this.mView.indexOfChild(this.mKeyguardBottomArea);
        this.mView.removeView(this.mKeyguardBottomArea);
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
        KeyguardBottomAreaView keyguardBottomAreaView2 = (KeyguardBottomAreaView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mView.getContext())).inflate(R.layout.keyguard_bottom_area, (ViewGroup) this.mView, false);
        this.mKeyguardBottomArea = keyguardBottomAreaView2;
        keyguardBottomAreaView2.initFrom(keyguardBottomAreaView);
        this.mView.addView(this.mKeyguardBottomArea, iIndexOfChild3);
        initBottomArea();
        this.mKeyguardIndicationController.setIndicationArea(this.mKeyguardBottomArea);
        this.mStatusBarStateListener.onDozeAmountChanged(this.mStatusBarStateController.getDozeAmount(), this.mStatusBarStateController.getInterpolatedDozeAmount());
        setKeyguardStatusViewVisibility(this.mBarState, false, false);
        setKeyguardBottomAreaVisibility(this.mBarState, false);
        Runnable runnable = this.mOnReinflationListener;
        if (runnable != null) {
            runnable.run();
        }
    }

    private void initBottomArea() {
        KeyguardAffordanceHelper keyguardAffordanceHelper = new KeyguardAffordanceHelper(this.mKeyguardAffordanceHelperCallback, this.mView.getContext(), this.mFalsingManager);
        this.mAffordanceHelper = keyguardAffordanceHelper;
        this.mKeyguardBottomArea.setAffordanceHelper(keyguardAffordanceHelper);
        this.mKeyguardBottomArea.setStatusBar(this.mStatusBar);
        this.mKeyguardBottomArea.setUserSetupComplete(this.mUserSetupComplete);
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mKeyguardIndicationController = keyguardIndicationController;
        keyguardIndicationController.setIndicationArea(this.mKeyguardBottomArea);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateGestureExclusionRect() {
        Rect rectCalculateGestureExclusionRect = calculateGestureExclusionRect();
        this.mView.setSystemGestureExclusionRects(rectCalculateGestureExclusionRect.isEmpty() ? Collections.EMPTY_LIST : Collections.singletonList(rectCalculateGestureExclusionRect));
    }

    private Rect calculateGestureExclusionRect() {
        Region regionCalculateTouchableRegion = this.mStatusBarTouchableRegionManager.calculateTouchableRegion();
        Rect bounds = (!isFullyCollapsed() || regionCalculateTouchableRegion == null) ? null : regionCalculateTouchableRegion.getBounds();
        return bounds != null ? bounds : EMPTY_RECT;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setIsFullWidth(boolean z) {
        this.mIsFullWidth = z;
        this.mNotificationStackScroller.setIsFullWidth(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startQsSizeChangeAnimation(int i, int i2) {
        ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
        if (valueAnimator != null) {
            i = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            this.mQsSizeChangeAnimator.cancel();
        }
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(i, i2);
        this.mQsSizeChangeAnimator = valueAnimatorOfInt;
        valueAnimatorOfInt.setDuration(300L);
        this.mQsSizeChangeAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mQsSizeChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelViewController.this.requestPanelHeightUpdate();
                NotificationPanelViewController.this.mQs.setHeightOverride(((Integer) NotificationPanelViewController.this.mQsSizeChangeAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mQsSizeChangeAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.7
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                NotificationPanelViewController.this.mQsSizeChangeAnimator = null;
            }
        });
        this.mQsSizeChangeAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void positionClockAndNotifications() {
        int unlockedStackScrollerPadding;
        boolean zIsAddOrRemoveAnimationPending = this.mNotificationStackScroller.isAddOrRemoveAnimationPending();
        boolean z = zIsAddOrRemoveAnimationPending || this.mAnimateNextPositionUpdate;
        if (this.mBarState != 1) {
            unlockedStackScrollerPadding = getUnlockedStackScrollerPadding();
        } else {
            int height = this.mView.getHeight();
            int iMax = Math.max(this.mIndicationBottomPadding, this.mAmbientIndicationBottomPadding);
            int clockPreferredY = this.mKeyguardStatusView.getClockPreferredY(height);
            boolean bypassEnabled = this.mKeyguardBypassController.getBypassEnabled();
            boolean z2 = !bypassEnabled && this.mNotificationStackScroller.isVisibleNotification();
            this.mKeyguardStatusView.setHasVisibleNotifications(z2);
            this.mClockPositionAlgorithm.setup(this.mStatusBarMinHeight, height - iMax, this.mNotificationStackScroller.getIntrinsicContentHeight(), getExpandedFraction(), height, (int) ((this.mKeyguardStatusView.getHeight() - (this.mShelfHeight / 2.0f)) - (this.mDarkIconSize / 2.0f)), clockPreferredY, hasCustomClock(), z2, this.mInterpolatedDarkAmount, this.mEmptyDragAmount, bypassEnabled, getUnlockedStackScrollerPadding());
            this.mClockPositionAlgorithm.run(this.mClockPositionResult);
            KeyguardStatusView keyguardStatusView = this.mKeyguardStatusView;
            AnimatableProperty animatableProperty = AnimatableProperty.X;
            float f = this.mClockPositionResult.clockX;
            AnimationProperties animationProperties = CLOCK_ANIMATION_PROPERTIES;
            PropertyAnimator.setProperty(keyguardStatusView, animatableProperty, f, animationProperties, z);
            PropertyAnimator.setProperty(this.mKeyguardStatusView, AnimatableProperty.Y, this.mClockPositionResult.clockY, animationProperties, z);
            updateNotificationTranslucency();
            updateClock();
            unlockedStackScrollerPadding = this.mClockPositionResult.stackScrollerPaddingExpanded;
        }
        this.mNotificationStackScroller.setIntrinsicPadding(unlockedStackScrollerPadding);
        this.mKeyguardBottomArea.setAntiBurnInOffsetX(this.mClockPositionResult.clockX);
        this.mStackScrollerMeasuringPass++;
        requestScrollerTopPaddingUpdate(zIsAddOrRemoveAnimationPending);
        this.mStackScrollerMeasuringPass = 0;
        this.mAnimateNextPositionUpdate = false;
    }

    private int getUnlockedStackScrollerPadding() {
        QS qs = this.mQs;
        return (qs != null ? qs.getHeader().getHeight() : 0) + this.mQsPeekHeight + this.mQsNotificationTopPadding;
    }

    public int computeMaxKeyguardNotifications(int i) {
        float minStackScrollerPadding = this.mClockPositionAlgorithm.getMinStackScrollerPadding();
        int iMax = Math.max(1, this.mResources.getDimensionPixelSize(R.dimen.notification_divider_height));
        float intrinsicHeight = this.mNotificationStackScroller.getNotificationShelf().getVisibility() == 8 ? 0.0f : r3.getIntrinsicHeight() + iMax;
        float height = (((this.mNotificationStackScroller.getHeight() - minStackScrollerPadding) - intrinsicHeight) - Math.max(this.mIndicationBottomPadding, this.mAmbientIndicationBottomPadding)) - this.mKeyguardStatusView.getLogoutButtonHeight();
        ExpandableView expandableView = null;
        int i2 = 0;
        for (int i3 = 0; i3 < this.mNotificationStackScroller.getChildCount(); i3++) {
            ExpandableView expandableView2 = (ExpandableView) this.mNotificationStackScroller.getChildAt(i3);
            if (canShowViewOnLockscreen(expandableView2)) {
                height = ((height - expandableView2.getMinHeight(true)) - (i2 == 0 ? 0.0f : iMax)) - this.mNotificationStackScroller.calculateGapHeight(expandableView, expandableView2, i2);
                if (height < 0.0f || i2 >= i) {
                    if (height <= (-intrinsicHeight)) {
                        return i2;
                    }
                    for (int i4 = i3 + 1; i4 < this.mNotificationStackScroller.getChildCount(); i4++) {
                        ExpandableView expandableView3 = (ExpandableView) this.mNotificationStackScroller.getChildAt(i4);
                        if ((expandableView3 instanceof ExpandableNotificationRow) && canShowViewOnLockscreen(expandableView3)) {
                            return i2;
                        }
                    }
                    return i2 + 1;
                }
                i2++;
                expandableView = expandableView2;
            }
        }
        return i2;
    }

    private boolean canShowViewOnLockscreen(ExpandableView expandableView) {
        if (expandableView.hasNoContentHeight()) {
            return false;
        }
        return (!(expandableView instanceof ExpandableNotificationRow) || canShowRowOnLockscreen((ExpandableNotificationRow) expandableView)) && expandableView.getVisibility() != 8;
    }

    private boolean canShowRowOnLockscreen(ExpandableNotificationRow expandableNotificationRow) {
        NotificationGroupManager notificationGroupManager = this.mGroupManager;
        return ((notificationGroupManager != null && notificationGroupManager.isSummaryOfSuppressedGroup(expandableNotificationRow.getEntry().getSbn())) || !this.mLockscreenUserManager.shouldShowOnKeyguard(expandableNotificationRow.getEntry()) || expandableNotificationRow.isRemoved()) ? false : true;
    }

    private void updateClock() {
        if (this.mKeyguardStatusViewAnimating) {
            return;
        }
        this.mKeyguardStatusView.setAlpha(this.mClockPositionResult.clockAlpha);
    }

    public void animateToFullShade(long j) {
        this.mNotificationStackScroller.goToFullShade(j);
        this.mView.requestLayout();
        this.mAnimateNextPositionUpdate = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isQSEventBlocked() {
        if (this.mKeyguardStateController.isShowing() && this.mKeyguardStateController.isMethodSecure()) {
            return !this.mStatusBarShownOnSecureKeyguard;
        }
        return false;
    }

    public void setQsExpansionEnabled(boolean z) {
        this.mQsExpansionEnabled = z;
        QS qs = this.mQs;
        if (qs == null) {
            return;
        }
        qs.setHeaderClickable(z);
        this.mQs.setSecureExpandDisabled(!this.mQsExpansionEnabled);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void resetViews(boolean z) {
        this.mIsLaunchTransitionFinished = false;
        this.mBlockTouches = false;
        if (!this.mLaunchingAffordance) {
            this.mAffordanceHelper.reset(false);
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        this.mStatusBar.getGutsManager().closeAndSaveGuts(true, true, true, -1, -1, true);
        if (z) {
            animateCloseQs(true);
        } else {
            closeQs();
        }
        this.mNotificationStackScroller.setOverScrollAmount(0.0f, true, z, !z);
        this.mNotificationStackScroller.resetScrollPosition();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void collapse(boolean z, float f) {
        if (canPanelBeCollapsed()) {
            if (this.mQsExpanded) {
                this.mQsExpandImmediate = true;
                this.mNotificationStackScroller.setShouldShowShelfOnly(true);
            }
            super.collapse(z, f);
        }
    }

    public void closeQs() {
        cancelQsAnimation();
        setQsExpansion(this.mQsMinExpansionHeight);
    }

    public void cancelAnimation() {
        this.mView.animate().cancel();
    }

    public void animateCloseQs(boolean z) {
        ValueAnimator valueAnimator = this.mQsExpansionAnimator;
        if (valueAnimator != null) {
            if (!this.mQsAnimatorExpand) {
                return;
            }
            float f = this.mQsExpansionHeight;
            valueAnimator.cancel();
            setQsExpansion(f);
        }
        flingSettings(0.0f, z ? 2 : 1);
    }

    public void expandWithQs() {
        if (this.mQsExpansionEnabled && !isQSEventBlocked()) {
            this.mQsExpandImmediate = true;
            this.mNotificationStackScroller.setShouldShowShelfOnly(true);
        }
        if (isFullyCollapsed()) {
            expand(true);
        } else {
            flingSettings(0.0f, 0);
        }
    }

    public void expandWithoutQs() {
        if (isQsExpanded()) {
            flingSettings(0.0f, 1);
        } else {
            expand(true);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void fling(float f, boolean z) {
        ((PhoneStatusBarView) this.mBar).mBar.getGestureRecorder();
        super.fling(f, z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        this.mHeadsUpTouchHelper.notifyFling(!z);
        setClosingWithAlphaFadeout((z || isOnKeyguard() || getFadeoutAlpha() != 1.0f) ? false : true);
        super.flingToHeight(f, z, f2, f3, z2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00b6  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onQsIntercept(android.view.MotionEvent r8) {
        /*
            Method dump skipped, instructions count: 256
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelViewController.onQsIntercept(android.view.MotionEvent):boolean");
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean isInContentBounds(float f, float f2) {
        float x = this.mNotificationStackScroller.getX();
        return !this.mNotificationStackScroller.isBelowLastNotification(f - x, f2) && x < f && f < x + ((float) this.mNotificationStackScroller.getWidth());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mOnlyAffordanceInThisMotion = false;
            this.mQsTouchAboveFalsingThreshold = this.mQsFullyExpanded;
            this.mDozingOnDown = isDozing();
            this.mDownX = motionEvent.getX();
            this.mDownY = motionEvent.getY();
            this.mCollapsedOnDown = isFullyCollapsed();
            this.mIsPanelCollapseOnQQS = canPanelCollapseOnQQS(this.mDownX, this.mDownY);
            this.mListenForHeadsUp = this.mCollapsedOnDown && this.mHeadsUpManager.hasPinnedHeadsUp();
            boolean z = this.mExpectingSynthesizedDown;
            this.mAllowExpandForSmallExpansion = z;
            this.mTouchSlopExceededBeforeDown = z;
            if (z) {
                this.mLastEventSynthesizedDown = true;
                return;
            } else {
                this.mLastEventSynthesizedDown = false;
                return;
            }
        }
        this.mLastEventSynthesizedDown = false;
    }

    private boolean canPanelCollapseOnQQS(float f, float f2) {
        if (this.mCollapsedOnDown || this.mKeyguardShowing || this.mQsExpanded) {
            return false;
        }
        QS qs = this.mQs;
        return f >= this.mQsFrame.getX() && f <= this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth()) && f2 <= ((float) (qs == null ? this.mKeyguardStatusBar : qs.getHeader()).getBottom());
    }

    private void flingQsWithCurrentVelocity(float f, boolean z) {
        float currentQSVelocity = getCurrentQSVelocity();
        boolean zFlingExpandsQs = flingExpandsQs(currentQSVelocity);
        if (zFlingExpandsQs) {
            logQsSwipeDown(f);
        }
        flingSettings(currentQSVelocity, (!zFlingExpandsQs || z) ? 1 : 0);
    }

    private void logQsSwipeDown(float f) {
        this.mLockscreenGestureLogger.write(this.mBarState == 1 ? 193 : 194, (int) ((f - this.mInitialTouchY) / this.mStatusBar.getDisplayDensity()), (int) (getCurrentQSVelocity() / this.mStatusBar.getDisplayDensity()));
    }

    private boolean flingExpandsQs(float f) {
        if (this.mFalsingManager.isUnlockingDisabled() || isFalseTouch()) {
            return false;
        }
        return Math.abs(f) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond() ? getQsExpansionFraction() > 0.5f : f > 0.0f;
    }

    private boolean isFalseTouch() {
        if (!this.mKeyguardAffordanceHelperCallback.needsAntiFalsing()) {
            return false;
        }
        if (this.mFalsingManager.isClassifierEnabled()) {
            return this.mFalsingManager.isFalseTouch();
        }
        return !this.mQsTouchAboveFalsingThreshold;
    }

    private float getQsExpansionFraction() {
        return Math.min(1.0f, (this.mQsExpansionHeight - this.mQsMinExpansionHeight) / (this.mQsMaxExpansionHeight - r1));
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean shouldExpandWhenNotFlinging() {
        if (super.shouldExpandWhenNotFlinging()) {
            return true;
        }
        return this.mAllowExpandForSmallExpansion && SystemClock.uptimeMillis() - this.mDownTime <= 300;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected float getOpeningHeight() {
        return this.mNotificationStackScroller.getOpeningHeight();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean handleQsTouch(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0 && getExpandedFraction() == 1.0f && this.mBarState != 1 && !this.mQsExpanded && this.mQsExpansionEnabled && !isQSEventBlocked()) {
            this.mQsTracking = true;
            this.mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
        }
        if (!isFullyCollapsed()) {
            handleQsDown(motionEvent);
        }
        if (!this.mQsExpandImmediate && this.mQsTracking) {
            onQsTouch(motionEvent);
            if (!this.mConflictingQsExpansionGesture) {
                return true;
            }
        }
        if (actionMasked == 3 || actionMasked == 1) {
            this.mConflictingQsExpansionGesture = false;
        }
        if (actionMasked == 0 && isFullyCollapsed() && this.mQsExpansionEnabled && !isQSEventBlocked()) {
            this.mTwoFingerQsExpandPossible = true;
        }
        if (this.mTwoFingerQsExpandPossible && isOpenQsEvent(motionEvent) && motionEvent.getY(motionEvent.getActionIndex()) < this.mStatusBarMinHeight) {
            this.mMetricsLogger.count("panel_open_qs", 1);
            this.mQsExpandImmediate = true;
            this.mNotificationStackScroller.setShouldShowShelfOnly(true);
            requestPanelHeightUpdate();
            setListening(true);
        }
        return false;
    }

    private boolean isInQsArea(float f, float f2) {
        return f >= this.mQsFrame.getX() && f <= this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth()) && (f2 <= this.mNotificationStackScroller.getBottomMostNotificationBottom() || f2 <= this.mQs.getView().getY() + ((float) this.mQs.getView().getHeight()));
    }

    private boolean isOpenQsEvent(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        int actionMasked = motionEvent.getActionMasked();
        boolean z = actionMasked == 5 && pointerCount == 2;
        boolean z2 = actionMasked == 0 && (motionEvent.isButtonPressed(32) || motionEvent.isButtonPressed(64));
        boolean z3 = actionMasked == 0 && (motionEvent.isButtonPressed(2) || motionEvent.isButtonPressed(4));
        float measuredWidth = this.mView.getMeasuredWidth();
        float x = motionEvent.getX();
        float f = (1.0f * measuredWidth) / 4.0f;
        int i = this.mOneFingerQuickSettingsIntercept;
        boolean z4 = i == 1 ? !(!this.mView.isLayoutRtl() ? measuredWidth - f < x : x < f) : !(i == 2 ? !this.mView.isLayoutRtl() ? x < f : measuredWidth - f < x : i != 3);
        if ((this.mQsSmartPullDown == 1 && !hasActiveClearableNotifications()) || ((this.mQsSmartPullDown == 2 && !this.mEntryManager.hasActiveOngoingNotifications()) || (this.mQsSmartPullDown == 3 && !this.mEntryManager.hasActiveVisibleNotifications()))) {
            z4 = true;
        }
        boolean z5 = z4 & (this.mBarState == 0);
        if (isQSEventBlocked()) {
            return false;
        }
        return z || z5 || z2 || z3;
    }

    private void handleQsDown(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && shouldQuickSettingsIntercept(motionEvent.getX(), motionEvent.getY(), -1.0f)) {
            this.mFalsingManager.onQsDown();
            this.mQsTracking = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
            notifyExpandingFinished();
        }
    }

    public void startWaitingForOpenPanelGesture() {
        if (isFullyCollapsed()) {
            this.mExpectingSynthesizedDown = true;
            onTrackingStarted();
            updatePanelExpanded();
        }
    }

    public void stopWaitingForOpenPanelGesture(boolean z, float f) {
        if (this.mExpectingSynthesizedDown) {
            this.mExpectingSynthesizedDown = false;
            if (z) {
                collapse(false, 1.0f);
            } else {
                maybeVibrateOnOpening();
                fling(f > 1.0f ? f * 1000.0f : 0.0f, true);
            }
            onTrackingStopped(false);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean flingExpands(float f, float f2, float f3, float f4) {
        boolean zFlingExpands = super.flingExpands(f, f2, f3, f4);
        if (this.mQsExpansionAnimator != null) {
            return true;
        }
        return zFlingExpands;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean shouldGestureWaitForTouchSlop() {
        if (!this.mExpectingSynthesizedDown) {
            return isFullyCollapsed() || this.mBarState != 0;
        }
        this.mExpectingSynthesizedDown = false;
        return false;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean shouldGestureIgnoreXTouchSlop(float f, float f2) {
        return !this.mAffordanceHelper.isOnAffordanceIcon(f, f2);
    }

    private void onQsTouch(MotionEvent motionEvent) {
        int pointerId;
        int iFindPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (iFindPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            iFindPointerIndex = 0;
        }
        float y = motionEvent.getY(iFindPointerIndex);
        float x = motionEvent.getX(iFindPointerIndex);
        float f = y - this.mInitialTouchY;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mQsTracking = true;
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            initVelocityTracker();
            trackMovement(motionEvent);
            return;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                setQsExpansion(this.mInitialHeightOnTouch + f);
                if (f >= getFalsingThreshold()) {
                    this.mQsTouchAboveFalsingThreshold = true;
                }
                trackMovement(motionEvent);
                return;
            }
            if (actionMasked != 3) {
                if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                    int i = motionEvent.getPointerId(0) == pointerId ? 1 : 0;
                    float y2 = motionEvent.getY(i);
                    float x2 = motionEvent.getX(i);
                    this.mTrackingPointer = motionEvent.getPointerId(i);
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mInitialTouchY = y2;
                    this.mInitialTouchX = x2;
                    return;
                }
                return;
            }
        }
        this.mQsTracking = false;
        this.mTrackingPointer = -1;
        trackMovement(motionEvent);
        if (getQsExpansionFraction() != 0.0f || y >= this.mInitialTouchY) {
            flingQsWithCurrentVelocity(y, motionEvent.getActionMasked() == 3);
        }
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mQsVelocityTracker = null;
        }
    }

    private int getFalsingThreshold() {
        return (int) (this.mQsFalsingThreshold * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setOverScrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        QS qs = this.mQs;
        if (qs == null) {
            return;
        }
        qs.setOverscrolling(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    protected void onQsExpansionStarted(int i) {
        cancelQsAnimation();
        cancelHeightAnimator();
        float f = this.mQsExpansionHeight - i;
        setQsExpansion(f);
        requestPanelHeightUpdate();
        this.mNotificationStackScroller.checkSnoozeLeavebehind();
        if (f == 0.0f) {
            this.mStatusBar.requestFaceAuth();
        }
    }

    private void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            updateQsState();
            requestPanelHeightUpdate();
            this.mFalsingManager.setQsExpanded(z);
            this.mStatusBar.setQsExpanded(z);
            this.mNotificationContainerParent.setQsExpanded(z);
            this.mPulseExpansionHandler.setQsExpanded(z);
            this.mKeyguardBypassController.setQSExpanded(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void maybeAnimateBottomAreaAlpha() {
        this.mBottomAreaShadeAlphaAnimator.cancel();
        if (this.mBarState == 2) {
            this.mBottomAreaShadeAlphaAnimator.start();
        } else {
            this.mBottomAreaShadeAlpha = 1.0f;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateKeyguardStatusBarOut() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mKeyguardStatusBar.getAlpha(), 0.0f);
        valueAnimatorOfFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        valueAnimatorOfFloat.setStartDelay(this.mKeyguardStateController.isKeyguardFadingAway() ? this.mKeyguardStateController.getKeyguardFadingAwayDelay() : 0L);
        valueAnimatorOfFloat.setDuration(this.mKeyguardStateController.isKeyguardFadingAway() ? this.mKeyguardStateController.getShortenedFadingAwayDuration() : 360L);
        valueAnimatorOfFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.12
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                NotificationPanelViewController.this.mAnimateKeyguardStatusBarInvisibleEndRunnable.run();
            }
        });
        valueAnimatorOfFloat.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateKeyguardStatusBarIn(long j) {
        if (this.mShowLockscreenStatusBar) {
            this.mKeyguardStatusBar.setVisibility(0);
            this.mKeyguardStatusBar.setAlpha(0.0f);
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
            valueAnimatorOfFloat.setDuration(j);
            valueAnimatorOfFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            valueAnimatorOfFloat.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setKeyguardBottomAreaVisibility(int i, boolean z) {
        this.mKeyguardBottomArea.animate().cancel();
        if (z) {
            this.mKeyguardBottomArea.animate().alpha(0.0f).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).setDuration(this.mKeyguardStateController.getShortenedFadingAwayDuration()).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardBottomAreaInvisibleEndRunnable).start();
        } else if (i == 1 || i == 2) {
            this.mKeyguardBottomArea.setVisibility(0);
            this.mKeyguardBottomArea.setAlpha(1.0f);
        } else {
            this.mKeyguardBottomArea.setVisibility(8);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setKeyguardStatusViewVisibility(int i, boolean z, boolean z2) {
        this.mKeyguardStatusView.animate().cancel();
        this.mKeyguardStatusViewAnimating = false;
        if ((!z && this.mBarState == 1 && i != 1) || z2) {
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.animate().alpha(0.0f).setStartDelay(0L).setDuration(160L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardStatusViewGoneEndRunnable);
            if (z) {
                this.mKeyguardStatusView.animate().setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).setDuration(this.mKeyguardStateController.getShortenedFadingAwayDuration()).start();
                return;
            }
            return;
        }
        if (this.mBarState == 2 && i == 1) {
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.setAlpha(0.0f);
            this.mKeyguardStatusView.animate().alpha(1.0f).setStartDelay(0L).setDuration(320L).setInterpolator(Interpolators.ALPHA_IN).withEndAction(this.mAnimateKeyguardStatusViewVisibleEndRunnable);
            return;
        }
        if (i != 1) {
            this.mKeyguardStatusView.setVisibility(8);
            this.mKeyguardStatusView.setAlpha(1.0f);
        } else if (z) {
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.animate().alpha(0.0f).translationYBy((-getHeight()) * 0.05f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration(125L).setStartDelay(0L).withEndAction(this.mAnimateKeyguardStatusViewInvisibleEndRunnable).start();
        } else {
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusView.setAlpha(1.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQsState() {
        this.mNotificationStackScroller.setQsExpanded(this.mQsExpanded);
        this.mNotificationStackScroller.setScrollingEnabled(this.mBarState != 1 && (!this.mQsExpanded || this.mQsExpansionFromOverscroll));
        updateEmptyShadeView();
        this.mQsNavbarScrim.setVisibility((this.mBarState == 0 && this.mQsExpanded && !this.mStackScrollerOverscrolling && this.mQsScrimEnabled) ? 0 : 4);
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        if (keyguardUserSwitcher != null && this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            keyguardUserSwitcher.hideIfNotSimple(true);
        }
        QS qs = this.mQs;
        if (qs == null) {
            return;
        }
        qs.setExpanded(this.mQsExpanded);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setQsExpansion(float f) {
        float fMin = Math.min(Math.max(f, this.mQsMinExpansionHeight), this.mQsMaxExpansionHeight);
        int i = this.mQsMaxExpansionHeight;
        this.mQsFullyExpanded = fMin == ((float) i) && i != 0;
        int i2 = this.mQsMinExpansionHeight;
        if (fMin > i2 && !this.mQsExpanded && !this.mStackScrollerOverscrolling && !this.mDozing) {
            setQsExpanded(true);
        } else if (fMin <= i2 && this.mQsExpanded) {
            setQsExpanded(false);
        }
        if (this.mKeyguardShowing) {
            if (fMin > this.mStatusBarHeight) {
                setTopCorners(false);
            } else {
                setTopCorners(true);
            }
        }
        this.mQsExpansionHeight = fMin;
        updateQsExpansion();
        requestScrollerTopPaddingUpdate(false);
        updateHeaderKeyguardAlpha();
        int i3 = this.mBarState;
        if (i3 == 2 || i3 == 1) {
            updateKeyguardBottomAreaAlpha();
            updateBigClockAlpha();
        }
        if (this.mBarState == 0 && this.mQsExpanded && !this.mStackScrollerOverscrolling && this.mQsScrimEnabled) {
            this.mQsNavbarScrim.setAlpha(getQsExpansionFraction());
        }
        if (this.mAccessibilityManager.isEnabled()) {
            this.mView.setAccessibilityPaneTitle(determineAccessibilityPaneTitle());
        }
        if (!this.mFalsingManager.isUnlockingDisabled() && this.mQsFullyExpanded && this.mFalsingManager.shouldEnforceBouncer()) {
            this.mStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
        }
        for (int i4 = 0; i4 < this.mExpansionListeners.size(); i4++) {
            PanelExpansionListener panelExpansionListener = this.mExpansionListeners.get(i4);
            int i5 = this.mQsMaxExpansionHeight;
            panelExpansionListener.onQsExpansionChanged(i5 != 0 ? this.mQsExpansionHeight / i5 : 0.0f);
        }
    }

    protected void updateQsExpansion() {
        if (this.mQs == null) {
            return;
        }
        float qsExpansionFraction = getQsExpansionFraction();
        this.mQs.setQsExpansion(qsExpansionFraction, getHeaderTranslation());
        this.mMediaHierarchyManager.setQsExpansion(qsExpansionFraction);
        this.mNotificationStackScroller.setQsExpansionFraction(qsExpansionFraction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String determineAccessibilityPaneTitle() {
        QS qs = this.mQs;
        if (qs != null && qs.isCustomizing()) {
            return this.mResources.getString(R.string.accessibility_desc_quick_settings_edit);
        }
        if (this.mQsExpansionHeight != 0.0f && this.mQsFullyExpanded) {
            return this.mResources.getString(R.string.accessibility_desc_quick_settings);
        }
        if (this.mBarState == 1) {
            return this.mResources.getString(R.string.accessibility_desc_lock_screen);
        }
        return this.mResources.getString(R.string.accessibility_desc_notification_shade);
    }

    private float calculateQsTopPadding() {
        boolean z = this.mKeyguardShowing;
        if (z && (this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted))) {
            int keyguardNotificationStaticPadding = getKeyguardNotificationStaticPadding();
            int iMax = this.mQsMaxExpansionHeight + this.mQsNotificationTopPadding;
            if (this.mBarState == 1) {
                iMax = Math.max(keyguardNotificationStaticPadding, iMax);
            }
            return (int) MathUtils.lerp(this.mQsMinExpansionHeight, iMax, getExpandedFraction());
        }
        if (this.mQsSizeChangeAnimator != null) {
            return Math.max(((Integer) r1.getAnimatedValue()).intValue(), getKeyguardNotificationStaticPadding());
        }
        if (z) {
            return MathUtils.lerp(getKeyguardNotificationStaticPadding(), this.mQsMaxExpansionHeight + this.mQsNotificationTopPadding, getQsExpansionFraction());
        }
        return this.mQsExpansionHeight + this.mQsNotificationTopPadding;
    }

    private int getKeyguardNotificationStaticPadding() {
        if (!this.mKeyguardShowing) {
            return 0;
        }
        if (!this.mKeyguardBypassController.getBypassEnabled()) {
            return this.mClockPositionResult.stackScrollerPadding;
        }
        int i = this.mHeadsUpInset;
        return !this.mNotificationStackScroller.isPulseExpanding() ? i : (int) MathUtils.lerp(i, this.mClockPositionResult.stackScrollerPadding, this.mNotificationStackScroller.calculateAppearFractionBypass());
    }

    protected void requestScrollerTopPaddingUpdate(boolean z) {
        this.mNotificationStackScroller.updateTopPadding(calculateQsTopPadding(), z);
        if (this.mKeyguardShowing && this.mKeyguardBypassController.getBypassEnabled()) {
            updateQsExpansion();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQSPulseExpansion() {
        QS qs = this.mQs;
        if (qs != null) {
            qs.setShowCollapsedOnKeyguard(this.mKeyguardShowing && this.mKeyguardBypassController.getBypassEnabled() && this.mNotificationStackScroller.isPulseExpanding());
        }
    }

    private void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }

    private void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mQsVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentQSVelocity() {
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker == null) {
            return 0.0f;
        }
        velocityTracker.computeCurrentVelocity(1000);
        return this.mQsVelocityTracker.getYVelocity();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelQsAnimation() {
        ValueAnimator valueAnimator = this.mQsExpansionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public void flingSettings(float f, int i) {
        flingSettings(f, i, null, false);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0014  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x001a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void flingSettings(float r7, int r8, final java.lang.Runnable r9, boolean r10) {
        /*
            r6 = this;
            r0 = 0
            r1 = 1
            if (r8 == 0) goto Lb
            if (r8 == r1) goto L8
            r2 = r0
            goto Le
        L8:
            int r2 = r6.mQsMinExpansionHeight
            goto Ld
        Lb:
            int r2 = r6.mQsMaxExpansionHeight
        Ld:
            float r2 = (float) r2
        Le:
            float r3 = r6.mQsExpansionHeight
            int r4 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r4 != 0) goto L1a
            if (r9 == 0) goto L19
            r9.run()
        L19:
            return
        L1a:
            r4 = 0
            if (r8 != 0) goto L1f
            r8 = r1
            goto L20
        L1f:
            r8 = r4
        L20:
            int r5 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r5 <= 0) goto L26
            if (r8 == 0) goto L2c
        L26:
            int r5 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r5 >= 0) goto L2f
            if (r8 == 0) goto L2f
        L2c:
            r7 = r0
            r0 = r1
            goto L30
        L2f:
            r0 = r4
        L30:
            r5 = 2
            float[] r5 = new float[r5]
            r5[r4] = r3
            r5[r1] = r2
            android.animation.ValueAnimator r3 = android.animation.ValueAnimator.ofFloat(r5)
            if (r10 == 0) goto L48
            android.view.animation.Interpolator r7 = com.android.systemui.Interpolators.TOUCH_RESPONSE
            r3.setInterpolator(r7)
            r4 = 368(0x170, double:1.82E-321)
            r3.setDuration(r4)
            goto L4f
        L48:
            com.android.systemui.statusbar.FlingAnimationUtils r10 = r6.mFlingAnimationUtils
            float r4 = r6.mQsExpansionHeight
            r10.apply(r3, r4, r2, r7)
        L4f:
            if (r0 == 0) goto L56
            r4 = 350(0x15e, double:1.73E-321)
            r3.setDuration(r4)
        L56:
            com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda1 r7 = new com.android.systemui.statusbar.phone.NotificationPanelViewController$$ExternalSyntheticLambda1
            r7.<init>()
            r3.addUpdateListener(r7)
            com.android.systemui.statusbar.phone.NotificationPanelViewController$15 r7 = new com.android.systemui.statusbar.phone.NotificationPanelViewController$15
            r7.<init>()
            r3.addListener(r7)
            r6.mAnimatingQS = r1
            r3.start()
            r6.mQsExpansionAnimator = r3
            r6.mQsAnimatorExpand = r8
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelViewController.flingSettings(float, int, java.lang.Runnable, boolean):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$flingSettings$7(ValueAnimator valueAnimator) {
        setQsExpansion(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldQuickSettingsIntercept(float f, float f2, float f3) {
        QS qs;
        if (!this.mQsExpansionEnabled || this.mCollapsedOnDown || isQSEventBlocked() || (this.mKeyguardShowing && this.mKeyguardBypassController.getBypassEnabled())) {
            return false;
        }
        View header = (this.mKeyguardShowing || (qs = this.mQs) == null) ? this.mKeyguardStatusBar : qs.getHeader();
        boolean z = f >= this.mQsFrame.getX() && f <= this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth()) && f2 >= ((float) header.getTop()) && f2 <= ((float) header.getBottom());
        return this.mQsExpanded ? z || (f3 < 0.0f && isInQsArea(f, f2)) : z;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean canCollapsePanelOnTouch() {
        return isInSettings() || this.mBarState == 1 || this.mNotificationStackScroller.isScrolledToBottom() || this.mIsPanelCollapseOnQQS;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected int getMaxPanelHeight() {
        if (this.mKeyguardBypassController.getBypassEnabled() && this.mBarState == 1) {
            return getMaxPanelHeightBypass();
        }
        return getMaxPanelHeightNonBypass();
    }

    private int getMaxPanelHeightNonBypass() {
        int iCalculatePanelHeightQsExpanded;
        int iMax = this.mStatusBarMinHeight;
        if (this.mBarState != 1 && this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            iMax = Math.max(iMax, (int) (this.mQsMinExpansionHeight + getOverExpansionAmount()));
        }
        if (this.mQsExpandImmediate || this.mQsExpanded || ((this.mIsExpanding && this.mQsExpandedWhenExpandingStarted) || this.mPulsing)) {
            iCalculatePanelHeightQsExpanded = calculatePanelHeightQsExpanded();
        } else {
            iCalculatePanelHeightQsExpanded = calculatePanelHeightShade();
        }
        int iMax2 = Math.max(iMax, iCalculatePanelHeightQsExpanded);
        if (iMax2 == 0) {
            Log.wtf(PanelViewController.TAG, "maxPanelHeight is 0. getOverExpansionAmount(): " + getOverExpansionAmount() + ", calculatePanelHeightQsExpanded: " + calculatePanelHeightQsExpanded() + ", calculatePanelHeightShade: " + calculatePanelHeightShade() + ", mStatusBarMinHeight = " + this.mStatusBarMinHeight + ", mQsMinExpansionHeight = " + this.mQsMinExpansionHeight);
        }
        return iMax2;
    }

    private int getMaxPanelHeightBypass() {
        int expandedClockPosition = this.mClockPositionAlgorithm.getExpandedClockPosition() + this.mKeyguardStatusView.getHeight();
        return this.mNotificationStackScroller.getVisibleNotificationCount() != 0 ? (int) (expandedClockPosition + (this.mShelfHeight / 2.0f) + (this.mDarkIconSize / 2.0f)) : expandedClockPosition;
    }

    public boolean isInSettings() {
        return this.mQsExpanded;
    }

    public boolean isExpanding() {
        return this.mIsExpanding;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onHeightUpdated(float f) {
        float fCalculatePanelHeightQsExpanded;
        if ((!this.mQsExpanded || this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) && this.mStackScrollerMeasuringPass <= 2) {
            positionClockAndNotifications();
        }
        if (this.mQsExpandImmediate || (this.mQsExpanded && !this.mQsTracking && this.mQsExpansionAnimator == null && !this.mQsExpansionFromOverscroll)) {
            if (this.mKeyguardShowing) {
                fCalculatePanelHeightQsExpanded = f / getMaxPanelHeight();
            } else {
                float intrinsicPadding = this.mNotificationStackScroller.getIntrinsicPadding() + this.mNotificationStackScroller.getLayoutMinHeight();
                fCalculatePanelHeightQsExpanded = (f - intrinsicPadding) / (calculatePanelHeightQsExpanded() - intrinsicPadding);
            }
            setQsExpansion(this.mQsMinExpansionHeight + (fCalculatePanelHeightQsExpanded * (this.mQsMaxExpansionHeight - r1)));
        }
        if (!this.mKeyguardShowing) {
            if (f > this.mStatusBarHeight) {
                setTopCorners(false);
            } else {
                setTopCorners(true);
            }
        }
        updateExpandedHeight(f);
        updateHeader();
        updateNotificationTranslucency();
        updatePanelExpanded();
        updateGestureExclusionRect();
    }

    private void updatePanelExpanded() {
        boolean z = !isFullyCollapsed() || this.mExpectingSynthesizedDown;
        if (this.mPanelExpanded != z) {
            this.mHeadsUpManager.setIsPanelExpanded(z);
            this.mStatusBarTouchableRegionManager.setPanelExpanded(z);
            this.mStatusBar.setPanelExpanded(z);
            this.mPanelExpanded = z;
        }
    }

    private int calculatePanelHeightShade() {
        int height = (int) ((this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) + this.mNotificationStackScroller.getTopPaddingOverflow());
        return this.mBarState == 1 ? Math.max(height, this.mClockPositionAlgorithm.getExpandedClockPosition() + this.mKeyguardStatusView.getHeight() + this.mNotificationStackScroller.getIntrinsicContentHeight()) : height;
    }

    private int calculatePanelHeightQsExpanded() {
        float height = (this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mNotificationStackScroller.getTopPadding();
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0 && this.mShowEmptyShadeView) {
            height = this.mNotificationStackScroller.getEmptyShadeViewHeight();
        }
        int iIntValue = this.mQsMaxExpansionHeight;
        if (this.mKeyguardShowing) {
            iIntValue += this.mQsNotificationTopPadding;
        }
        ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
        if (valueAnimator != null) {
            iIntValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        }
        float fMax = Math.max(iIntValue, this.mBarState == 1 ? this.mClockPositionResult.stackScrollerPadding : 0) + height + this.mNotificationStackScroller.getTopPaddingOverflow();
        if (fMax > this.mNotificationStackScroller.getHeight()) {
            fMax = Math.max(iIntValue + this.mNotificationStackScroller.getLayoutMinHeight(), this.mNotificationStackScroller.getHeight());
        }
        return (int) fMax;
    }

    public void updateNotificationTranslucency() {
        float fadeoutAlpha = (!this.mClosingWithAlphaFadeOut || this.mExpandingFromHeadsUp || this.mHeadsUpManager.hasPinnedHeadsUp()) ? 1.0f : getFadeoutAlpha();
        if (this.mBarState == 1 && !this.mHintAnimationRunning && !this.mKeyguardBypassController.getBypassEnabled()) {
            fadeoutAlpha *= this.mClockPositionResult.clockAlpha;
        }
        this.mNotificationStackScroller.setAlpha(fadeoutAlpha);
        this.mStatusBar.updateDismissAllVisibility(true);
    }

    private float getFadeoutAlpha() {
        if (this.mQsMinExpansionHeight == 0) {
            return 1.0f;
        }
        return (float) Math.pow(Math.max(0.0f, Math.min(getExpandedHeight() / this.mQsMinExpansionHeight, 1.0f)), 0.75d);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected float getOverExpansionAmount() {
        float currentOverScrollAmount = this.mNotificationStackScroller.getCurrentOverScrollAmount(true);
        if (Float.isNaN(currentOverScrollAmount)) {
            Log.wtf(PanelViewController.TAG, "OverExpansionAmount is NaN!");
        }
        return currentOverScrollAmount;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected float getOverExpansionPixels() {
        return this.mNotificationStackScroller.getCurrentOverScrolledPixels(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeader() {
        if (this.mBarState == 1) {
            updateHeaderKeyguardAlpha();
        }
        updateQsExpansion();
    }

    protected float getHeaderTranslation() {
        if (this.mBarState == 1 && !this.mKeyguardBypassController.getBypassEnabled()) {
            return -this.mQs.getQsMinExpansionHeight();
        }
        float fCalculateAppearFraction = this.mNotificationStackScroller.calculateAppearFraction(this.mExpandedHeight);
        float f = -this.mQsExpansionHeight;
        if (this.mKeyguardBypassController.getBypassEnabled() && isOnKeyguard() && this.mNotificationStackScroller.isPulseExpanding()) {
            fCalculateAppearFraction = (this.mPulseExpansionHandler.isExpanding() || this.mPulseExpansionHandler.getLeavingLockscreen()) ? this.mNotificationStackScroller.calculateAppearFractionBypass() : 0.0f;
            f = -this.mQs.getQsMinExpansionHeight();
        }
        return Math.min(0.0f, MathUtils.lerp(f, 0.0f, Math.min(1.0f, fCalculateAppearFraction)) + this.mExpandOffset);
    }

    private float getKeyguardContentsAlpha() {
        float expandedHeight;
        float height;
        if (this.mBarState == 1) {
            expandedHeight = getExpandedHeight();
            height = this.mKeyguardStatusBar.getHeight() + this.mNotificationsHeaderCollideDistance;
        } else {
            expandedHeight = getExpandedHeight();
            height = this.mKeyguardStatusBar.getHeight();
        }
        return (float) Math.pow(MathUtils.saturate(expandedHeight / height), 0.75d);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeaderKeyguardAlpha() {
        if (this.mKeyguardShowing) {
            float fMin = Math.min(getKeyguardContentsAlpha(), 1.0f - Math.min(1.0f, getQsExpansionFraction() * 2.0f)) * this.mKeyguardStatusBarAnimateAlpha * (1.0f - this.mKeyguardHeadsUpShowingAmount);
            this.mKeyguardStatusBar.setAlpha(fMin);
            this.mKeyguardStatusBar.setVisibility((fMin == 0.0f || this.mDozing || ((this.mFirstBypassAttempt && this.mUpdateMonitor.shouldListenForFace()) || this.mDelayShowingKeyguardStatusBar) || !this.mShowLockscreenStatusBar) ? 4 : 0);
        }
    }

    private void updateKeyguardBottomAreaAlpha() {
        float fMin = Math.min(MathUtils.map(isUnlockHintRunning() ? 0.0f : 0.95f, 1.0f, 0.0f, 1.0f, getExpandedFraction()), 1.0f - getQsExpansionFraction()) * this.mBottomAreaShadeAlpha;
        this.mKeyguardBottomArea.setAffordanceAlpha(fMin);
        this.mKeyguardBottomArea.setImportantForAccessibility(fMin == 0.0f ? 4 : 0);
        View ambientIndicationContainer = this.mStatusBar.getAmbientIndicationContainer();
        if (ambientIndicationContainer != null) {
            ambientIndicationContainer.setAlpha(fMin);
        }
    }

    private void updateBigClockAlpha() {
        this.mBigClockContainer.setAlpha(Math.min(MathUtils.map(isUnlockHintRunning() ? 0.0f : 0.95f, 1.0f, 0.0f, 1.0f, getExpandedFraction()), 1.0f - getQsExpansionFraction()));
    }

    private void setTopCorners(boolean z) {
        if (this.mScreenDecorations == null) {
            this.mScreenDecorations = (ScreenDecorations) Dependency.get(ScreenDecorations.class);
        }
        this.mScreenDecorations.setTopCorners(z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onExpandingStarted() {
        super.onExpandingStarted();
        this.mNotificationStackScroller.onExpansionStarted();
        this.mIsExpanding = true;
        boolean z = this.mQsFullyExpanded;
        this.mQsExpandedWhenExpandingStarted = z;
        this.mMediaHierarchyManager.setCollapsingShadeFromQS(z && !this.mAnimatingQS);
        if (this.mQsExpanded) {
            onQsExpansionStarted();
        }
        QS qs = this.mQs;
        if (qs == null) {
            return;
        }
        qs.setHeaderListening(true);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onExpandingFinished() {
        super.onExpandingFinished();
        this.mNotificationStackScroller.onExpansionStopped();
        this.mHeadsUpManager.onExpandingFinished();
        this.mConversationNotificationManager.onNotificationPanelExpandStateChanged(isFullyCollapsed());
        this.mIsExpanding = false;
        this.mMediaHierarchyManager.setCollapsingShadeFromQS(false);
        if (isFullyCollapsed()) {
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.16
                @Override // java.lang.Runnable
                public void run() {
                    NotificationPanelViewController.this.setListening(false);
                }
            });
            this.mView.postOnAnimation(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.17
                @Override // java.lang.Runnable
                public void run() {
                    NotificationPanelViewController.this.mView.getParent().invalidateChild(NotificationPanelViewController.this.mView, NotificationPanelViewController.M_DUMMY_DIRTY_RECT);
                }
            });
        } else {
            setListening(true);
        }
        this.mQsExpandImmediate = false;
        this.mNotificationStackScroller.setShouldShowShelfOnly(false);
        this.mTwoFingerQsExpandPossible = false;
        notifyListenersTrackingHeadsUp(null);
        this.mExpandingFromHeadsUp = false;
        setPanelScrimMinFraction(0.0f);
    }

    private void notifyListenersTrackingHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        for (int i = 0; i < this.mTrackingHeadsUpListeners.size(); i++) {
            this.mTrackingHeadsUpListeners.get(i).accept(expandableNotificationRow);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setListening(boolean z) {
        QS qs = this.mQs;
        if (qs == null) {
            return;
        }
        qs.setListening(z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void expand(boolean z) {
        super.expand(z);
        setListening(true);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void setOverExpansion(float f, boolean z) {
        if (this.mConflictingQsExpansionGesture || this.mQsExpandImmediate || this.mBarState == 1) {
            return;
        }
        this.mNotificationStackScroller.setOnHeightChangedListener(null);
        if (z) {
            this.mNotificationStackScroller.setOverScrolledPixels(f, true, false);
        } else {
            this.mNotificationStackScroller.setOverScrollAmount(f, true, false);
        }
        this.mNotificationStackScroller.setOnHeightChangedListener(this.mOnHeightChangedListener);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onTrackingStarted() {
        this.mFalsingManager.onTrackingStarted(!this.mKeyguardStateController.canDismissLockScreen());
        super.onTrackingStarted();
        if (this.mQsFullyExpanded) {
            this.mQsExpandImmediate = true;
            this.mNotificationStackScroller.setShouldShowShelfOnly(true);
        }
        int i = this.mBarState;
        if (i == 1 || i == 2) {
            this.mAffordanceHelper.animateHideLeftRightIcon();
        }
        this.mNotificationStackScroller.onPanelTrackingStarted();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onTrackingStopped(boolean z) {
        this.mFalsingManager.onTrackingStopped();
        super.onTrackingStopped(z);
        if (z) {
            this.mNotificationStackScroller.setOverScrolledPixels(0.0f, true, true);
        }
        this.mNotificationStackScroller.onPanelTrackingStopped();
        if (z) {
            int i = this.mBarState;
            if ((i == 1 || i == 2) && !this.mHintAnimationRunning) {
                this.mAffordanceHelper.reset(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMaxHeadsUpTranslation() {
        this.mNotificationStackScroller.setHeadsUpBoundaries(getHeight(), this.mNavigationBarBottomHeight);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void startUnlockHintAnimation() {
        if (this.mPowerManager.isPowerSaveMode()) {
            onUnlockHintStarted();
            onUnlockHintFinished();
        } else {
            super.startUnlockHintAnimation();
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onUnlockHintFinished() {
        super.onUnlockHintFinished();
        this.mNotificationStackScroller.setUnlockHintRunning(false);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onUnlockHintStarted() {
        super.onUnlockHintStarted();
        this.mNotificationStackScroller.setUnlockHintRunning(true);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected float getPeekHeight() {
        int peekHeight;
        if (this.mNotificationStackScroller.getNotGoneChildCount() > 0) {
            peekHeight = this.mNotificationStackScroller.getPeekHeight();
        } else {
            peekHeight = this.mQsMinExpansionHeight;
        }
        return peekHeight;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean shouldExpandToTopOfClearAll(float f) {
        return super.shouldExpandToTopOfClearAll(f) && this.mNotificationStackScroller.calculateAppearFraction(f) >= 1.0f;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean shouldUseDismissingAnimation() {
        return this.mBarState != 0 && (this.mKeyguardStateController.canDismissLockScreen() || !isTracking());
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean fullyExpandedClearAllVisible() {
        return this.mNotificationStackScroller.isFooterViewNotGone() && this.mNotificationStackScroller.isScrolledToBottom() && !this.mQsExpandImmediate;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean isClearAllVisible() {
        return this.mNotificationStackScroller.isFooterViewContentVisible();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected int getClearAllHeightWithPadding() {
        return this.mNotificationStackScroller.getFooterViewHeightWithPadding();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean isTrackingBlocked() {
        return (this.mConflictingQsExpansionGesture && this.mQsExpanded) || this.mBlockingExpansionForCurrentTouch;
    }

    public boolean isQsExpanded() {
        return this.mQsExpanded;
    }

    public boolean isQsDetailShowing() {
        return this.mQs.isShowingDetail();
    }

    public void closeQsDetail() {
        this.mQs.closeDetail();
    }

    public boolean isLaunchTransitionFinished() {
        return this.mIsLaunchTransitionFinished;
    }

    public boolean isLaunchTransitionRunning() {
        return this.mIsLaunchTransitionRunning;
    }

    public void setLaunchTransitionEndRunnable(Runnable runnable) {
        this.mLaunchAnimationEndRunnable = runnable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDozingVisibilities(boolean z) {
        this.mKeyguardBottomArea.setDozing(this.mDozing, z);
        if (this.mDozing || !z) {
            return;
        }
        animateKeyguardStatusBarIn(360L);
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public void showEmptyShadeView(boolean z) {
        this.mShowEmptyShadeView = z;
        updateEmptyShadeView();
    }

    private void updateEmptyShadeView() {
        this.mNotificationStackScroller.updateEmptyShadeView(this.mShowEmptyShadeView && !this.mQsExpanded);
    }

    public void setQsScrimEnabled(boolean z) {
        boolean z2 = this.mQsScrimEnabled != z;
        this.mQsScrimEnabled = z;
        if (z2) {
            updateQsState();
        }
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void onScreenTurningOn() {
        this.mKeyguardStatusView.dozeTimeTick();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean onMiddleClicked() {
        int i = this.mBarState;
        if (i == 0) {
            this.mView.post(this.mPostCollapseRunnable);
            return false;
        }
        if (i == 1) {
            if (!this.mDozingOnDown) {
                if (this.mKeyguardBypassController.getBypassEnabled()) {
                    this.mUpdateMonitor.requestFaceAuth();
                } else {
                    this.mLockscreenGestureLogger.write(188, 0, 0);
                    this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_LOCK_SHOW_HINT);
                    startUnlockHintAnimation();
                }
            }
            return true;
        }
        if (i == 2 && !this.mQsExpanded) {
            this.mStatusBarStateController.setState(1);
        }
        return true;
    }

    public void setPanelAlpha(int i, boolean z) {
        if (this.mPanelAlpha != i) {
            this.mPanelAlpha = i;
            PropertyAnimator.setProperty(this.mView, this.mPanelAlphaAnimator, i, i == 255 ? this.mPanelAlphaInPropertiesAnimator : this.mPanelAlphaOutPropertiesAnimator, z);
        }
    }

    public void setPanelAlphaEndAction(Runnable runnable) {
        this.mPanelAlphaEndAction = runnable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateKeyguardStatusBarForHeadsUp() {
        boolean z = this.mKeyguardShowing && this.mHeadsUpAppearanceController.shouldBeVisible();
        if (this.mShowingKeyguardHeadsUp != z) {
            this.mShowingKeyguardHeadsUp = z;
            if (this.mKeyguardShowing) {
                PropertyAnimator.setProperty(this.mView, this.KEYGUARD_HEADS_UP_SHOWING_AMOUNT, z ? 1.0f : 0.0f, KEYGUARD_HUN_PROPERTIES, true);
            } else {
                PropertyAnimator.applyImmediately(this.mView, this.KEYGUARD_HEADS_UP_SHOWING_AMOUNT, 0.0f);
            }
        }
    }

    private void setKeyguardHeadsUpShowingAmount(float f) {
        this.mKeyguardHeadsUpShowingAmount = f;
        updateHeaderKeyguardAlpha();
    }

    private float getKeyguardHeadsUpShowingAmount() {
        return this.mKeyguardHeadsUpShowingAmount;
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        this.mNotificationStackScroller.setHeadsUpAnimatingAway(z);
        updateHeadsUpVisibility();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateHeadsUpVisibility() {
        ((PhoneStatusBarView) this.mBar).setHeadsUpVisible(this.mHeadsUpAnimatingAway || this.mHeadsUpPinnedMode);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void setHeadsUpManager(HeadsUpManagerPhone headsUpManagerPhone) {
        super.setHeadsUpManager(headsUpManagerPhone);
        this.mHeadsUpTouchHelper = new HeadsUpTouchHelper(headsUpManagerPhone, this.mNotificationStackScroller.getHeadsUpCallback(), this);
    }

    public void setTrackedHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow != null) {
            notifyListenersTrackingHeadsUp(expandableNotificationRow);
            this.mExpandingFromHeadsUp = true;
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected void onClosingFinished() {
        super.onClosingFinished();
        resetHorizontalPanelPosition();
        setClosingWithAlphaFadeout(false);
        this.mMediaHierarchyManager.closeGuts();
    }

    private void setClosingWithAlphaFadeout(boolean z) {
        this.mClosingWithAlphaFadeOut = z;
        this.mNotificationStackScroller.forceNoOverlappingRendering(z);
    }

    protected void updateVerticalPanelPosition(float f) {
        if (this.mKeyguardShowing || this.mNotificationStackScroller.getWidth() * 1.75f > this.mView.getWidth()) {
            resetHorizontalPanelPosition();
            return;
        }
        float width = this.mPositionMinSideMargin + (this.mNotificationStackScroller.getWidth() / 2);
        float width2 = (this.mView.getWidth() - this.mPositionMinSideMargin) - (this.mNotificationStackScroller.getWidth() / 2);
        if (Math.abs(f - (this.mView.getWidth() / 2)) < this.mNotificationStackScroller.getWidth() / 4) {
            f = this.mView.getWidth() / 2;
        }
        setHorizontalPanelTranslation(Math.min(width2, Math.max(width, f)) - (this.mNotificationStackScroller.getLeft() + (this.mNotificationStackScroller.getWidth() / 2)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetHorizontalPanelPosition() {
        setHorizontalPanelTranslation(0.0f);
    }

    protected void setHorizontalPanelTranslation(float f) {
        this.mNotificationStackScroller.setTranslationX(f);
        this.mQsFrame.setTranslationX(f);
        int size = this.mVerticalTranslationListener.size();
        for (int i = 0; i < size; i++) {
            this.mVerticalTranslationListener.get(i).run();
        }
    }

    protected void updateExpandedHeight(float f) {
        if (this.mTracking) {
            this.mNotificationStackScroller.setExpandingVelocity(getCurrentExpandVelocity());
        }
        if (this.mKeyguardBypassController.getBypassEnabled() && isOnKeyguard()) {
            f = getMaxPanelHeightNonBypass();
        }
        this.mNotificationStackScroller.setExpandedHeight(f);
        updateKeyguardBottomAreaAlpha();
        updateBigClockAlpha();
        updateStatusBarIcons();
    }

    public boolean isFullWidth() {
        return this.mIsFullWidth;
    }

    private void updateStatusBarIcons() {
        boolean z = (isPanelVisibleBecauseOfHeadsUp() || isFullWidth()) && getExpandedHeight() < getOpeningHeight();
        if (z && isOnKeyguard()) {
            z = false;
        }
        if (z != this.mShowIconsWhenExpanded) {
            this.mShowIconsWhenExpanded = z;
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isOnKeyguard() {
        return this.mBarState == 1;
    }

    public void setPanelScrimMinFraction(float f) {
        this.mBar.panelScrimMinFractionChanged(f);
    }

    public void clearNotificationEffects() {
        this.mStatusBar.clearNotificationEffects();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected boolean isPanelVisibleBecauseOfHeadsUp() {
        return (this.mHeadsUpManager.hasPinnedHeadsUp() || this.mHeadsUpAnimatingAway) && this.mBarState == 0;
    }

    public void launchCamera(boolean z, int i) {
        if (i == 1) {
            this.mLastCameraLaunchSource = "power_double_tap";
        } else if (i == 0) {
            this.mLastCameraLaunchSource = "wiggle_gesture";
        } else if (i == 2) {
            this.mLastCameraLaunchSource = "lift_to_launch_ml";
        } else {
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        if (isFullyCollapsed()) {
            z = false;
        } else {
            setLaunchingAffordance(true);
        }
        this.mAffordanceHasPreview = this.mKeyguardBottomArea.getRightPreview() != null;
        this.mAffordanceHelper.launchAffordance(z, this.mView.getLayoutDirection() == 1);
    }

    public void onAffordanceLaunchEnded() {
        setLaunchingAffordance(false);
    }

    private void setLaunchingAffordance(boolean z) {
        this.mLaunchingAffordance = z;
        this.mKeyguardAffordanceHelperCallback.getLeftIcon().setLaunchingAffordance(z);
        this.mKeyguardAffordanceHelperCallback.getRightIcon().setLaunchingAffordance(z);
        this.mKeyguardBypassController.setLaunchingAffordance(z);
        Consumer<Boolean> consumer = this.mAffordanceLaunchListener;
        if (consumer != null) {
            consumer.accept(Boolean.valueOf(z));
        }
    }

    public boolean isLaunchingAffordanceWithPreview() {
        return this.mLaunchingAffordance && this.mAffordanceHasPreview;
    }

    public boolean canCameraGestureBeLaunched() {
        ActivityInfo activityInfo;
        if (!this.mStatusBar.isCameraAllowedByAdmin()) {
            return false;
        }
        ResolveInfo resolveInfoResolveCameraIntent = this.mKeyguardBottomArea.resolveCameraIntent();
        String str = (resolveInfoResolveCameraIntent == null || (activityInfo = resolveInfoResolveCameraIntent.activityInfo) == null) ? null : activityInfo.packageName;
        if (str != null) {
            return ((this.mBarState == 0 && isForegroundApp(str)) || this.mAffordanceHelper.isSwipingInProgress()) ? false : true;
        }
        return false;
    }

    private boolean isForegroundApp(String str) throws SecurityException {
        List<ActivityManager.RunningTaskInfo> runningTasks = this.mActivityManager.getRunningTasks(1);
        return !runningTasks.isEmpty() && str.equals(runningTasks.get(0).topActivity.getPackageName());
    }

    private void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public boolean hideStatusBarIconsWhenExpanded() {
        if (this.mLaunchingNotification) {
            return this.mHideIconsDuringNotificationLaunch;
        }
        HeadsUpAppearanceController headsUpAppearanceController = this.mHeadsUpAppearanceController;
        if (headsUpAppearanceController == null || !headsUpAppearanceController.shouldBeVisible()) {
            return (isFullWidth() && this.mShowIconsWhenExpanded) ? false : true;
        }
        return false;
    }

    /* renamed from: com.android.systemui.statusbar.phone.NotificationPanelViewController$18, reason: invalid class name */
    class AnonymousClass18 implements FragmentHostManager.FragmentListener {
        AnonymousClass18() {
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
        public void onFragmentViewCreated(String str, Fragment fragment) {
            NotificationPanelViewController.this.mQs = (QS) fragment;
            NotificationPanelViewController.this.mQs.setPanelView(NotificationPanelViewController.this.mHeightListener);
            NotificationPanelViewController.this.mQs.setExpandClickListener(NotificationPanelViewController.this.mOnClickListener);
            NotificationPanelViewController.this.mQs.setHeaderClickable(NotificationPanelViewController.this.mQsExpansionEnabled);
            NotificationPanelViewController.this.updateQSPulseExpansion();
            NotificationPanelViewController.this.mQs.setOverscrolling(NotificationPanelViewController.this.mStackScrollerOverscrolling);
            NotificationPanelViewController.this.mQs.setSecureExpandDisabled(NotificationPanelViewController.this.isQSEventBlocked());
            NotificationPanelViewController.this.mQs.getView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$18$$ExternalSyntheticLambda0
                @Override // android.view.View.OnLayoutChangeListener
                public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    this.f$0.lambda$onFragmentViewCreated$0(view, i, i2, i3, i4, i5, i6, i7, i8);
                }
            });
            NotificationPanelViewController.this.mNotificationStackScroller.setQsContainer((ViewGroup) NotificationPanelViewController.this.mQs.getView());
            if (NotificationPanelViewController.this.mQs instanceof QSFragment) {
                NotificationPanelViewController.this.mKeyguardStatusBar.setQSPanel(((QSFragment) NotificationPanelViewController.this.mQs).getQsPanel());
            }
            NotificationPanelViewController.this.updateQsExpansion();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onFragmentViewCreated$0(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            if (i4 - i2 != i8 - i6) {
                NotificationPanelViewController.this.mHeightListener.onQsHeightChanged();
            }
        }

        @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
        public void onFragmentViewDestroyed(String str, Fragment fragment) {
            if (fragment == NotificationPanelViewController.this.mQs) {
                NotificationPanelViewController.this.mQs = null;
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void setTouchAndAnimationDisabled(boolean z) {
        super.setTouchAndAnimationDisabled(z);
        if (z && this.mAffordanceHelper.isSwipingInProgress() && !this.mIsLaunchTransitionRunning) {
            this.mAffordanceHelper.reset(false);
        }
        this.mNotificationStackScroller.setAnimationsEnabled(!z);
    }

    public void setDozing(boolean z, boolean z2, PointF pointF) {
        if (z == this.mDozing) {
            return;
        }
        this.mView.setDozing(z);
        this.mDozing = z;
        this.mNotificationStackScroller.setDozing(z, z2, pointF);
        this.mKeyguardBottomArea.setDozing(this.mDozing, z2);
        if (z) {
            this.mBottomAreaShadeAlphaAnimator.cancel();
        }
        int i = this.mBarState;
        if (i == 1 || i == 2) {
            updateDozingVisibilities(z2);
        }
        this.mStatusBarStateController.setDozeAmount(z ? 1.0f : 0.0f, z2);
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
        boolean z2 = !this.mDozeParameters.getDisplayNeedsBlanking();
        boolean z3 = Settings.Secure.getIntForUser(this.mView.getContext().getContentResolver(), "pulse_ambient_light", 0, -2) == 1;
        boolean z4 = Settings.Secure.getIntForUser(this.mView.getContext().getContentResolver(), "ambient_light_pulse_for_all", 0, -2) == 1;
        if (z2) {
            this.mAnimateNextPositionUpdate = true;
        }
        boolean z5 = this.mPulsing;
        if (!z5 && !this.mDozing) {
            this.mAnimateNextPositionUpdate = false;
        }
        NotificationLightsView notificationLightsView = this.mPulseLightsView;
        if (notificationLightsView != null && z3) {
            notificationLightsView.setVisibility(z5 ? 0 : 8);
            if (this.mPulsing || z4) {
                this.mPulseLightsView.animateNotification();
                this.mPulseLightsView.setPulsing(z);
            }
        }
        this.mNotificationStackScroller.setPulsing(z, z2);
        this.mKeyguardStatusView.setPulsing(z);
    }

    public void dozeTimeTick() {
        this.mKeyguardBottomArea.dozeTimeTick();
        this.mKeyguardStatusView.dozeTimeTick();
        if (this.mInterpolatedDarkAmount > 0.0f) {
            positionClockAndNotifications();
        }
    }

    public void setStatusAccessibilityImportance(int i) {
        this.mKeyguardStatusView.setImportantForAccessibility(i);
    }

    public void setUserSetupComplete(boolean z) {
        this.mUserSetupComplete = z;
        this.mKeyguardBottomArea.setUserSetupComplete(z);
    }

    public void applyExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.mExpandOffset = expandAnimationParameters != null ? expandAnimationParameters.getTopChange() : 0.0f;
        updateQsExpansion();
        if (expandAnimationParameters != null) {
            boolean z = expandAnimationParameters.getProgress(14L, 100L) == 0.0f;
            if (z != this.mHideIconsDuringNotificationLaunch) {
                this.mHideIconsDuringNotificationLaunch = z;
                if (z) {
                    return;
                }
                this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
            }
        }
    }

    public void addTrackingHeadsUpListener(Consumer<ExpandableNotificationRow> consumer) {
        this.mTrackingHeadsUpListeners.add(consumer);
    }

    public void removeTrackingHeadsUpListener(Consumer<ExpandableNotificationRow> consumer) {
        this.mTrackingHeadsUpListeners.remove(consumer);
    }

    public void addVerticalTranslationListener(Runnable runnable) {
        this.mVerticalTranslationListener.add(runnable);
    }

    public void removeVerticalTranslationListener(Runnable runnable) {
        this.mVerticalTranslationListener.remove(runnable);
    }

    public void setHeadsUpAppearanceController(HeadsUpAppearanceController headsUpAppearanceController) {
        this.mHeadsUpAppearanceController = headsUpAppearanceController;
    }

    public void onBouncerPreHideAnimation() {
        setKeyguardStatusViewVisibility(this.mBarState, true, false);
    }

    public void blockExpansionForCurrentTouch() {
        this.mBlockingExpansionForCurrentTouch = this.mTracking;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
        printWriter.println("    gestureExclusionRect: " + calculateGestureExclusionRect());
        KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
        if (keyguardStatusBarView != null) {
            keyguardStatusBarView.dump(fileDescriptor, printWriter, strArr);
        }
        KeyguardStatusView keyguardStatusView = this.mKeyguardStatusView;
        if (keyguardStatusView != null) {
            keyguardStatusView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    public boolean hasActiveClearableNotifications() {
        return this.mNotificationStackScroller.hasActiveClearableNotifications(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateShowEmptyShadeView() {
        showEmptyShadeView((this.mBarState == 1 || this.mEntryManager.hasVisibleNotifications()) ? false : true);
    }

    public RemoteInputController.Delegate createRemoteInputDelegate() {
        return this.mNotificationStackScroller.createDelegate();
    }

    void updateNotificationViews(String str) {
        this.mNotificationStackScroller.updateSectionBoundaries(str);
        this.mNotificationStackScroller.updateSpeedBumpIndex();
        this.mNotificationStackScroller.updateFooter();
        updateShowEmptyShadeView();
        this.mNotificationStackScroller.updateIconAreaViews();
    }

    public void onUpdateRowStates() {
        this.mNotificationStackScroller.onUpdateRowStates();
    }

    public boolean hasPulsingNotifications() {
        return this.mNotificationStackScroller.hasPulsingNotifications();
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mNotificationStackScroller.getActivatedChild();
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mNotificationStackScroller.setActivatedChild(activatableNotificationView);
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mNotificationStackScroller.runAfterAnimationFinished(runnable);
    }

    public void initDependencies(StatusBar statusBar, NotificationGroupManager notificationGroupManager, NotificationShelf notificationShelf, NotificationIconAreaController notificationIconAreaController, ScrimController scrimController) {
        setStatusBar(statusBar);
        setGroupManager(this.mGroupManager);
        this.mNotificationStackScroller.setNotificationPanelController(this);
        this.mNotificationStackScroller.setIconAreaController(notificationIconAreaController);
        this.mNotificationStackScroller.setStatusBar(statusBar);
        this.mNotificationStackScroller.setGroupManager(notificationGroupManager);
        this.mNotificationStackScroller.setShelf(notificationShelf);
        this.mNotificationStackScroller.setScrimController(scrimController);
        updateShowEmptyShadeView();
    }

    public void showTransientIndication(int i) {
        this.mKeyguardIndicationController.showTransientIndication(i);
    }

    public void setOnReinflationListener(Runnable runnable) {
        this.mOnReinflationListener = runnable;
    }

    public void setAlpha(float f) {
        this.mView.setAlpha(f);
    }

    public ViewPropertyAnimator fadeOut(long j, long j2, Runnable runnable) {
        return this.mView.animate().alpha(0.0f).setStartDelay(j).setDuration(j2).setInterpolator(Interpolators.ALPHA_OUT).withLayer().withEndAction(runnable);
    }

    public void resetViewGroupFade() {
        ViewGroupFadeHelper.reset(this.mView);
    }

    public void addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        this.mView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void removeOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        this.mView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public MyOnHeadsUpChangedListener getOnHeadsUpChangedListener() {
        return this.mOnHeadsUpChangedListener;
    }

    public int getHeight() {
        return this.mView.getHeight();
    }

    public void onThemeChanged() {
        this.mConfigurationListener.onThemeChanged();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public OnLayoutChangeListener createLayoutChangeListener() {
        return new OnLayoutChangeListener();
    }

    public void setEmptyDragAmount(float f) {
        this.mExpansionCallback.setEmptyDragAmount(f);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected PanelViewController.TouchHandler createTouchHandler() {
        return new PanelViewController.TouchHandler() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController.19
            @Override // com.android.systemui.statusbar.phone.PanelViewController.TouchHandler
            public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                if (NotificationPanelViewController.this.mBlockTouches) {
                    return false;
                }
                if (NotificationPanelViewController.this.mQsFullyExpanded && NotificationPanelViewController.this.mQs.disallowPanelTouches()) {
                    return false;
                }
                NotificationPanelViewController.this.initDownStates(motionEvent);
                if (NotificationPanelViewController.this.mStatusBar.isBouncerShowing()) {
                    return true;
                }
                if (NotificationPanelViewController.this.mBar.panelEnabled() && NotificationPanelViewController.this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
                    NotificationPanelViewController.this.mMetricsLogger.count("panel_open", 1);
                    NotificationPanelViewController.this.mMetricsLogger.count("panel_open_peek", 1);
                    return true;
                }
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                if (!notificationPanelViewController.shouldQuickSettingsIntercept(notificationPanelViewController.mDownX, NotificationPanelViewController.this.mDownY, 0.0f) && NotificationPanelViewController.this.mPulseExpansionHandler.onInterceptTouchEvent(motionEvent)) {
                    return true;
                }
                if (NotificationPanelViewController.this.isFullyCollapsed() || !NotificationPanelViewController.this.onQsIntercept(motionEvent)) {
                    return super.onInterceptTouchEvent(motionEvent);
                }
                return true;
            }

            @Override // com.android.systemui.statusbar.phone.PanelViewController.TouchHandler, android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (NotificationPanelViewController.this.mBlockTouches) {
                    return false;
                }
                if ((NotificationPanelViewController.this.mQsFullyExpanded && NotificationPanelViewController.this.mQs != null && NotificationPanelViewController.this.mQs.disallowPanelTouches()) || NotificationPanelViewController.this.mStatusBar.isBouncerShowingScrimmed()) {
                    return false;
                }
                if (((NotificationPanelViewController.this.mIsLockscreenDoubleTapEnabled && NotificationPanelViewController.this.mBarState == 1) || (!NotificationPanelViewController.this.mQsExpanded && NotificationPanelViewController.this.mDoubleTapToSleepEnabled && motionEvent.getY() < NotificationPanelViewController.this.mStatusBarHeaderHeight)) && !NotificationPanelViewController.this.mPulsing && !NotificationPanelViewController.this.mDozing) {
                    NotificationPanelViewController.this.mDoubleTapGesture.onTouchEvent(motionEvent);
                }
                if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                    NotificationPanelViewController.this.mBlockingExpansionForCurrentTouch = false;
                }
                if (NotificationPanelViewController.this.mLastEventSynthesizedDown && motionEvent.getAction() == 1) {
                    NotificationPanelViewController.this.expand(true);
                }
                NotificationPanelViewController.this.initDownStates(motionEvent);
                if (!NotificationPanelViewController.this.mIsExpanding) {
                    NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                    if (!notificationPanelViewController.shouldQuickSettingsIntercept(notificationPanelViewController.mDownX, NotificationPanelViewController.this.mDownY, 0.0f) && NotificationPanelViewController.this.mPulseExpansionHandler.onTouchEvent(motionEvent)) {
                        return true;
                    }
                }
                if (NotificationPanelViewController.this.mListenForHeadsUp && !NotificationPanelViewController.this.mHeadsUpTouchHelper.isTrackingHeadsUp() && NotificationPanelViewController.this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
                    NotificationPanelViewController.this.mMetricsLogger.count("panel_open_peek", 1);
                }
                boolean zOnTouchEvent = ((NotificationPanelViewController.this.mIsExpanding && !NotificationPanelViewController.this.mHintAnimationRunning) || NotificationPanelViewController.this.mQsExpanded || NotificationPanelViewController.this.mBarState == 0 || NotificationPanelViewController.this.mDozing) ? false : NotificationPanelViewController.this.mAffordanceHelper.onTouchEvent(motionEvent) | false;
                if (NotificationPanelViewController.this.mOnlyAffordanceInThisMotion) {
                    return true;
                }
                boolean zOnTouchEvent2 = zOnTouchEvent | NotificationPanelViewController.this.mHeadsUpTouchHelper.onTouchEvent(motionEvent);
                if (!NotificationPanelViewController.this.mHeadsUpTouchHelper.isTrackingHeadsUp() && NotificationPanelViewController.this.handleQsTouch(motionEvent)) {
                    return true;
                }
                if (motionEvent.getActionMasked() == 0 && NotificationPanelViewController.this.isFullyCollapsed()) {
                    NotificationPanelViewController.this.mMetricsLogger.count("panel_open", 1);
                    NotificationPanelViewController.this.updateVerticalPanelPosition(motionEvent.getX());
                    zOnTouchEvent2 = true;
                }
                return !NotificationPanelViewController.this.mDozing || NotificationPanelViewController.this.mPulsing || super.onTouch(view, motionEvent) || zOnTouchEvent2;
            }
        };
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    protected PanelViewController.OnConfigurationChangedListener createOnConfigurationChangedListener() {
        return new OnConfigurationChangedListener();
    }

    private class OnHeightChangedListener implements ExpandableView.OnHeightChangedListener {
        @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
        public void onReset(ExpandableView expandableView) {
        }

        private OnHeightChangedListener() {
        }

        @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
        public void onHeightChanged(ExpandableView expandableView, boolean z) {
            if (expandableView == null && NotificationPanelViewController.this.mQsExpanded) {
                return;
            }
            if (z && NotificationPanelViewController.this.mInterpolatedDarkAmount == 0.0f) {
                NotificationPanelViewController.this.mAnimateNextPositionUpdate = true;
            }
            ExpandableView firstChildNotGone = NotificationPanelViewController.this.mNotificationStackScroller.getFirstChildNotGone();
            ExpandableNotificationRow expandableNotificationRow = firstChildNotGone instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) firstChildNotGone : null;
            if (expandableNotificationRow != null && (expandableView == expandableNotificationRow || expandableNotificationRow.getNotificationParent() == expandableNotificationRow)) {
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
            }
            NotificationPanelViewController.this.requestPanelHeightUpdate();
        }
    }

    private class OnClickListener implements View.OnClickListener {
        private OnClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            NotificationPanelViewController.this.onQsExpansionStarted();
            if (!NotificationPanelViewController.this.mQsExpanded) {
                if (!NotificationPanelViewController.this.mQsExpansionEnabled || NotificationPanelViewController.this.isQSEventBlocked()) {
                    return;
                }
                NotificationPanelViewController.this.mLockscreenGestureLogger.write(195, 0, 0);
                NotificationPanelViewController.this.flingSettings(0.0f, 0, null, true);
                return;
            }
            NotificationPanelViewController.this.flingSettings(0.0f, 1, null, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class OnOverscrollTopChangedListener implements NotificationStackScrollLayout.OnOverscrollTopChangedListener {
        private OnOverscrollTopChangedListener() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener
        public void onOverscrollTopChanged(float f, boolean z) {
            NotificationPanelViewController.this.cancelQsAnimation();
            if (!NotificationPanelViewController.this.mQsExpansionEnabled || NotificationPanelViewController.this.isQSEventBlocked()) {
                f = 0.0f;
            }
            if (f < 1.0f) {
                f = 0.0f;
            }
            NotificationPanelViewController.this.setOverScrolling(f != 0.0f && z);
            NotificationPanelViewController.this.mQsExpansionFromOverscroll = f != 0.0f;
            NotificationPanelViewController.this.mLastOverscroll = f;
            NotificationPanelViewController.this.updateQsState();
            NotificationPanelViewController.this.setQsExpansion(r4.mQsMinExpansionHeight + f);
        }

        @Override // com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener
        public void flingTopOverscroll(float f, boolean z) {
            NotificationPanelViewController.this.mLastOverscroll = 0.0f;
            NotificationPanelViewController.this.mQsExpansionFromOverscroll = false;
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.setQsExpansion(notificationPanelViewController.mQsExpansionHeight);
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            if ((!notificationPanelViewController2.mQsExpansionEnabled || NotificationPanelViewController.this.isQSEventBlocked()) && z) {
                f = 0.0f;
            }
            notificationPanelViewController2.flingSettings(f, (z && NotificationPanelViewController.this.mQsExpansionEnabled && !NotificationPanelViewController.this.isQSEventBlocked()) ? 0 : 1, new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$OnOverscrollTopChangedListener$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$flingTopOverscroll$0();
                }
            }, false);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$flingTopOverscroll$0() {
            NotificationPanelViewController.this.mStackScrollerOverscrolling = false;
            NotificationPanelViewController.this.setOverScrolling(false);
            NotificationPanelViewController.this.updateQsState();
        }
    }

    private class DynamicPrivacyControlListener implements DynamicPrivacyController.Listener {
        private DynamicPrivacyControlListener() {
        }

        @Override // com.android.systemui.statusbar.notification.DynamicPrivacyController.Listener
        public void onDynamicPrivacyChanged() {
            if (NotificationPanelViewController.this.mLinearDarkAmount != 0.0f) {
                return;
            }
            NotificationPanelViewController.this.mAnimateNextPositionUpdate = true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class KeyguardAffordanceHelperCallback implements KeyguardAffordanceHelper.Callback {
        private KeyguardAffordanceHelperCallback() {
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public void onAnimationToSideStarted(boolean z, float f, float f2) {
            if (NotificationPanelViewController.this.mView.getLayoutDirection() != 1) {
                z = !z;
            }
            NotificationPanelViewController.this.mIsLaunchTransitionRunning = true;
            NotificationPanelViewController.this.mLaunchAnimationEndRunnable = null;
            float displayDensity = NotificationPanelViewController.this.mStatusBar.getDisplayDensity();
            int iAbs = Math.abs((int) (f / displayDensity));
            int iAbs2 = Math.abs((int) (f2 / displayDensity));
            if (z) {
                NotificationPanelViewController.this.mLockscreenGestureLogger.write(190, iAbs, iAbs2);
                NotificationPanelViewController.this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_DIALER);
                NotificationPanelViewController.this.mFalsingManager.onLeftAffordanceOn();
                if (NotificationPanelViewController.this.mFalsingManager.shouldEnforceBouncer()) {
                    NotificationPanelViewController.this.mStatusBar.executeRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$KeyguardAffordanceHelperCallback$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onAnimationToSideStarted$0();
                        }
                    }, null, true, false, true);
                } else {
                    NotificationPanelViewController.this.mKeyguardBottomArea.launchLeftAffordance();
                }
            } else {
                if ("lockscreen_affordance".equals(NotificationPanelViewController.this.mLastCameraLaunchSource)) {
                    NotificationPanelViewController.this.mLockscreenGestureLogger.write(189, iAbs, iAbs2);
                    NotificationPanelViewController.this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_CAMERA);
                }
                NotificationPanelViewController.this.mFalsingManager.onCameraOn();
                if (NotificationPanelViewController.this.mFalsingManager.shouldEnforceBouncer()) {
                    NotificationPanelViewController.this.mStatusBar.executeRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$KeyguardAffordanceHelperCallback$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onAnimationToSideStarted$1();
                        }
                    }, null, true, false, true);
                } else {
                    NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                    notificationPanelViewController.mKeyguardBottomArea.launchCamera(notificationPanelViewController.mLastCameraLaunchSource);
                }
            }
            NotificationPanelViewController.this.mStatusBar.startLaunchTransitionTimeout();
            NotificationPanelViewController.this.mBlockTouches = true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onAnimationToSideStarted$0() {
            NotificationPanelViewController.this.mKeyguardBottomArea.launchLeftAffordance();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onAnimationToSideStarted$1() {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mKeyguardBottomArea.launchCamera(notificationPanelViewController.mLastCameraLaunchSource);
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public void onAnimationToSideEnded() {
            NotificationPanelViewController.this.mIsLaunchTransitionRunning = false;
            NotificationPanelViewController.this.mIsLaunchTransitionFinished = true;
            if (NotificationPanelViewController.this.mLaunchAnimationEndRunnable != null) {
                NotificationPanelViewController.this.mLaunchAnimationEndRunnable.run();
                NotificationPanelViewController.this.mLaunchAnimationEndRunnable = null;
            }
            NotificationPanelViewController.this.mStatusBar.readyForKeyguardDone();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public float getMaxTranslationDistance() {
            return (float) Math.hypot(NotificationPanelViewController.this.mView.getWidth(), NotificationPanelViewController.this.getHeight());
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public void onSwipingStarted(boolean z) {
            NotificationPanelViewController.this.mFalsingManager.onAffordanceSwipingStarted(z);
            if (NotificationPanelViewController.this.mView.getLayoutDirection() == 1) {
                z = !z;
            }
            if (z) {
                NotificationPanelViewController.this.mKeyguardBottomArea.bindCameraPrewarmService();
            }
            NotificationPanelViewController.this.mView.requestDisallowInterceptTouchEvent(true);
            NotificationPanelViewController.this.mOnlyAffordanceInThisMotion = true;
            NotificationPanelViewController.this.mQsTracking = false;
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public void onSwipingAborted() {
            NotificationPanelViewController.this.mFalsingManager.onAffordanceSwipingAborted();
            NotificationPanelViewController.this.mKeyguardBottomArea.unbindCameraPrewarmService(false);
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public void onIconClicked(boolean z) {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            if (notificationPanelViewController.mHintAnimationRunning) {
                return;
            }
            notificationPanelViewController.mHintAnimationRunning = true;
            notificationPanelViewController.mAffordanceHelper.startHintAnimation(z, new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationPanelViewController$KeyguardAffordanceHelperCallback$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onIconClicked$2();
                }
            });
            if (NotificationPanelViewController.this.mView.getLayoutDirection() == 1) {
                z = !z;
            }
            if (z) {
                NotificationPanelViewController.this.mStatusBar.onCameraHintStarted();
            } else if (NotificationPanelViewController.this.mKeyguardBottomArea.isLeftVoiceAssist()) {
                NotificationPanelViewController.this.mStatusBar.onVoiceAssistHintStarted();
            } else {
                NotificationPanelViewController.this.mStatusBar.onPhoneHintStarted();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onIconClicked$2() {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mHintAnimationRunning = false;
            notificationPanelViewController.mStatusBar.onHintFinished();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public KeyguardAffordanceView getLeftIcon() {
            return NotificationPanelViewController.this.mView.getLayoutDirection() == 1 ? NotificationPanelViewController.this.mKeyguardBottomArea.getRightView() : NotificationPanelViewController.this.mKeyguardBottomArea.getLeftView();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public KeyguardAffordanceView getRightIcon() {
            return NotificationPanelViewController.this.mView.getLayoutDirection() == 1 ? NotificationPanelViewController.this.mKeyguardBottomArea.getLeftView() : NotificationPanelViewController.this.mKeyguardBottomArea.getRightView();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public View getLeftPreview() {
            return NotificationPanelViewController.this.mView.getLayoutDirection() == 1 ? NotificationPanelViewController.this.mKeyguardBottomArea.getRightPreview() : NotificationPanelViewController.this.mKeyguardBottomArea.getLeftPreview();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public View getRightPreview() {
            return NotificationPanelViewController.this.mView.getLayoutDirection() == 1 ? NotificationPanelViewController.this.mKeyguardBottomArea.getLeftPreview() : NotificationPanelViewController.this.mKeyguardBottomArea.getRightPreview();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public float getAffordanceFalsingFactor() {
            return NotificationPanelViewController.this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public boolean needsAntiFalsing() {
            return NotificationPanelViewController.this.mBarState == 1;
        }
    }

    private class OnEmptySpaceClickListener implements NotificationStackScrollLayout.OnEmptySpaceClickListener {
        private OnEmptySpaceClickListener() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.OnEmptySpaceClickListener
        public void onEmptySpaceClicked(float f, float f2) {
            NotificationPanelViewController.this.onEmptySpaceClick(f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class MyOnHeadsUpChangedListener implements OnHeadsUpChangedListener {
        private MyOnHeadsUpChangedListener() {
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpPinnedModeChanged(boolean z) {
            NotificationPanelViewController.this.mNotificationStackScroller.setInHeadsUpPinnedMode(z);
            if (z) {
                NotificationPanelViewController.this.mHeadsUpExistenceChangedRunnable.run();
                NotificationPanelViewController.this.updateNotificationTranslucency();
            } else {
                NotificationPanelViewController.this.setHeadsUpAnimatingAway(true);
                NotificationPanelViewController.this.mNotificationStackScroller.runAfterAnimationFinished(NotificationPanelViewController.this.mHeadsUpExistenceChangedRunnable);
            }
            NotificationPanelViewController.this.updateGestureExclusionRect();
            NotificationPanelViewController.this.mHeadsUpPinnedMode = z;
            NotificationPanelViewController.this.updateHeadsUpVisibility();
            NotificationPanelViewController.this.updateKeyguardStatusBarForHeadsUp();
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpPinned(NotificationEntry notificationEntry) {
            if (NotificationPanelViewController.this.isOnKeyguard()) {
                return;
            }
            NotificationPanelViewController.this.mNotificationStackScroller.generateHeadsUpAnimation(notificationEntry.getHeadsUpAnimationView(), true);
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpUnPinned(NotificationEntry notificationEntry) {
            if (NotificationPanelViewController.this.isFullyCollapsed() && notificationEntry.isRowHeadsUp() && !NotificationPanelViewController.this.isOnKeyguard()) {
                NotificationPanelViewController.this.mNotificationStackScroller.generateHeadsUpAnimation(notificationEntry.getHeadsUpAnimationView(), false);
                notificationEntry.setHeadsUpIsVisible();
            }
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
            NotificationPanelViewController.this.mNotificationStackScroller.generateHeadsUpAnimation(notificationEntry, z);
        }
    }

    private class HeightListener implements QS.HeightListener {
        private HeightListener() {
        }

        @Override // com.android.systemui.plugins.qs.QS.HeightListener
        public void onQsHeightChanged() {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mQsMaxExpansionHeight = notificationPanelViewController.mQs != null ? NotificationPanelViewController.this.mQs.getDesiredHeight() : 0;
            if (NotificationPanelViewController.this.mQsExpanded && NotificationPanelViewController.this.mQsFullyExpanded) {
                NotificationPanelViewController.this.mQsExpansionHeight = r0.mQsMaxExpansionHeight;
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelViewController.this.requestPanelHeightUpdate();
            }
            if (NotificationPanelViewController.this.mAccessibilityManager.isEnabled()) {
                NotificationPanelViewController.this.mView.setAccessibilityPaneTitle(NotificationPanelViewController.this.determineAccessibilityPaneTitle());
            }
            NotificationPanelViewController.this.mNotificationStackScroller.setMaxTopPadding(NotificationPanelViewController.this.mQsMaxExpansionHeight + NotificationPanelViewController.this.mQsNotificationTopPadding);
        }
    }

    private class ZenModeControllerCallback implements ZenModeController.Callback {
        private ZenModeControllerCallback() {
        }

        @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
        public void onZenChanged(int i) {
            NotificationPanelViewController.this.updateShowEmptyShadeView();
        }
    }

    private class ConfigurationListener implements ConfigurationController.ConfigurationListener {
        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onUiModeChanged() {
        }

        private ConfigurationListener() {
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            NotificationPanelViewController.this.updateShowEmptyShadeView();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onThemeChanged() {
            int themeResId = NotificationPanelViewController.this.mView.getContext().getThemeResId();
            if (NotificationPanelViewController.this.mThemeResId == themeResId) {
                return;
            }
            NotificationPanelViewController.this.mThemeResId = themeResId;
            NotificationPanelViewController.this.reInflateViews();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onOverlayChanged() {
            NotificationPanelViewController.this.reInflateViews();
        }
    }

    private class StatusBarStateListener implements StatusBarStateController.StateListener {
        private StatusBarStateListener() {
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            boolean zGoingToFullShade = NotificationPanelViewController.this.mStatusBarStateController.goingToFullShade();
            boolean zIsKeyguardFadingAway = NotificationPanelViewController.this.mKeyguardStateController.isKeyguardFadingAway();
            int i2 = NotificationPanelViewController.this.mBarState;
            boolean z = i == 1;
            NotificationPanelViewController.this.setKeyguardStatusViewVisibility(i, zIsKeyguardFadingAway, zGoingToFullShade);
            NotificationPanelViewController.this.setKeyguardBottomAreaVisibility(i, zGoingToFullShade);
            NotificationPanelViewController.this.mBarState = i;
            NotificationPanelViewController.this.mKeyguardShowing = z;
            if (NotificationPanelViewController.this.mQs != null) {
                NotificationPanelViewController.this.mQs.setSecureExpandDisabled(NotificationPanelViewController.this.isQSEventBlocked());
            }
            if (i2 == 1 && (zGoingToFullShade || i == 2)) {
                NotificationPanelViewController.this.animateKeyguardStatusBarOut();
                NotificationPanelViewController.this.mQs.animateHeaderSlidingIn(NotificationPanelViewController.this.mBarState == 2 ? 0L : NotificationPanelViewController.this.mKeyguardStateController.calculateGoingToFullShadeDelay());
            } else if (i2 != 2 || i != 1) {
                NotificationPanelViewController.this.mKeyguardStatusBar.setAlpha(1.0f);
                NotificationPanelViewController.this.mKeyguardStatusBar.setVisibility((z && NotificationPanelViewController.this.mShowLockscreenStatusBar) ? 0 : 4);
                if (z && i2 != NotificationPanelViewController.this.mBarState && NotificationPanelViewController.this.mQs != null) {
                    NotificationPanelViewController.this.mQs.hideImmediately();
                }
            } else {
                NotificationPanelViewController.this.animateKeyguardStatusBarIn(360L);
                NotificationPanelViewController.this.mNotificationStackScroller.resetScrollPosition();
                if (!NotificationPanelViewController.this.mQsExpanded) {
                    NotificationPanelViewController.this.mQs.animateHeaderSlidingOut();
                }
            }
            NotificationPanelViewController.this.updateKeyguardStatusBarForHeadsUp();
            if (z) {
                NotificationPanelViewController.this.updateDozingVisibilities(false);
            }
            NotificationPanelViewController.this.updateQSPulseExpansion();
            NotificationPanelViewController.this.maybeAnimateBottomAreaAlpha();
            NotificationPanelViewController.this.resetHorizontalPanelPosition();
            NotificationPanelViewController.this.updateQsState();
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozeAmountChanged(float f, float f2) {
            NotificationPanelViewController.this.mInterpolatedDarkAmount = f2;
            NotificationPanelViewController.this.mLinearDarkAmount = f;
            NotificationPanelViewController.this.mKeyguardStatusView.setDarkAmount(NotificationPanelViewController.this.mInterpolatedDarkAmount);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mKeyguardBottomArea.setDarkAmount(notificationPanelViewController.mInterpolatedDarkAmount);
            NotificationPanelViewController.this.positionClockAndNotifications();
        }
    }

    private class ExpansionCallback implements PulseExpansionHandler.ExpansionCallback {
        private ExpansionCallback() {
        }

        @Override // com.android.systemui.statusbar.PulseExpansionHandler.ExpansionCallback
        public void setEmptyDragAmount(float f) {
            NotificationPanelViewController.this.mEmptyDragAmount = f * 0.2f;
            NotificationPanelViewController.this.positionClockAndNotifications();
        }
    }

    private class OnAttachStateChangeListener implements View.OnAttachStateChangeListener, TunerService.Tunable {
        private OnAttachStateChangeListener() {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            FragmentHostManager.get(NotificationPanelViewController.this.mView).addTagListener(QS.TAG, NotificationPanelViewController.this.mFragmentListener);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mStatusBarStateController.addCallback(notificationPanelViewController.mStatusBarStateListener);
            NotificationPanelViewController.this.mZenModeController.addCallback(NotificationPanelViewController.this.mZenModeControllerCallback);
            NotificationPanelViewController.this.mConfigurationController.addCallback(NotificationPanelViewController.this.mConfigurationListener);
            NotificationPanelViewController.this.mTunerService.addTunable(this, "lineagesystem:qs_quick_pulldown");
            NotificationPanelViewController.this.mTunerService.addTunable(this, "lineagesystem:double_tap_sleep_gesture");
            NotificationPanelViewController.this.mTunerService.addTunable(this, "system:double_tap_sleep_lockscreen");
            NotificationPanelViewController.this.mTunerService.addTunable(this, "system:lockscreen_status_bar");
            NotificationPanelViewController.this.mTunerService.addTunable(this, "system:lockscreen_enable_qs");
            NotificationPanelViewController.this.mTunerService.addTunable(this, "system:qs_smart_pulldown");
            NotificationPanelViewController.this.mUpdateMonitor.registerCallback(NotificationPanelViewController.this.mKeyguardUpdateCallback);
            NotificationPanelViewController.this.mConfigurationListener.onThemeChanged();
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            FragmentHostManager.get(NotificationPanelViewController.this.mView).removeTagListener(QS.TAG, NotificationPanelViewController.this.mFragmentListener);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mStatusBarStateController.removeCallback(notificationPanelViewController.mStatusBarStateListener);
            NotificationPanelViewController.this.mZenModeController.removeCallback(NotificationPanelViewController.this.mZenModeControllerCallback);
            NotificationPanelViewController.this.mConfigurationController.removeCallback(NotificationPanelViewController.this.mConfigurationListener);
            NotificationPanelViewController.this.mTunerService.removeTunable(this);
            NotificationPanelViewController.this.mUpdateMonitor.removeCallback(NotificationPanelViewController.this.mKeyguardUpdateCallback);
        }

        @Override // com.android.systemui.tuner.TunerService.Tunable
        public void onTuningChanged(String str, String str2) {
            str.hashCode();
            switch (str) {
                case "system:qs_smart_pulldown":
                    NotificationPanelViewController.this.mQsSmartPullDown = TunerService.parseInteger(str2, 0);
                    break;
                case "system:lockscreen_enable_qs":
                    NotificationPanelViewController.this.mStatusBarShownOnSecureKeyguard = TunerService.parseIntegerSwitch(str2, true);
                    NotificationPanelViewController.this.mStatusBar.updateQsExpansionEnabled();
                    break;
                case "lineagesystem:qs_quick_pulldown":
                    NotificationPanelViewController.this.mOneFingerQuickSettingsIntercept = TunerService.parseInteger(str2, 0);
                    break;
                case "lineagesystem:double_tap_sleep_gesture":
                    NotificationPanelViewController.this.mDoubleTapToSleepEnabled = TunerService.parseIntegerSwitch(str2, true);
                    break;
                case "system:lockscreen_status_bar":
                    NotificationPanelViewController.this.mShowLockscreenStatusBar = TunerService.parseIntegerSwitch(str2, true);
                    break;
                case "system:double_tap_sleep_lockscreen":
                    NotificationPanelViewController.this.mIsLockscreenDoubleTapEnabled = TunerService.parseIntegerSwitch(str2, true);
                    break;
            }
        }
    }

    private class OnLayoutChangeListener extends PanelViewController.OnLayoutChangeListener {
        private OnLayoutChangeListener() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.PanelViewController.OnLayoutChangeListener, android.view.View.OnLayoutChangeListener
        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            DejankUtils.startDetectingBlockingIpcs("NVP#onLayout");
            super.onLayoutChange(view, i, i2, i3, i4, i5, i6, i7, i8);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.setIsFullWidth(notificationPanelViewController.mNotificationStackScroller.getWidth() == NotificationPanelViewController.this.mView.getWidth());
            NotificationPanelViewController.this.mKeyguardStatusView.setPivotX(NotificationPanelViewController.this.mView.getWidth() / 2);
            NotificationPanelViewController.this.mKeyguardStatusView.setPivotY(NotificationPanelViewController.this.mKeyguardStatusView.getClockTextSize() * 0.34521484f);
            int i9 = NotificationPanelViewController.this.mQsMaxExpansionHeight;
            if (NotificationPanelViewController.this.mQs != null) {
                float f = NotificationPanelViewController.this.mQsMinExpansionHeight;
                NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
                notificationPanelViewController2.mQsMinExpansionHeight = notificationPanelViewController2.mKeyguardShowing ? 0 : NotificationPanelViewController.this.mQs.getQsMinExpansionHeight();
                if (NotificationPanelViewController.this.mQsExpansionHeight == f) {
                    NotificationPanelViewController.this.mQsExpansionHeight = r3.mQsMinExpansionHeight;
                }
                NotificationPanelViewController notificationPanelViewController3 = NotificationPanelViewController.this;
                notificationPanelViewController3.mQsMaxExpansionHeight = notificationPanelViewController3.mQs.getDesiredHeight();
                NotificationPanelViewController.this.mNotificationStackScroller.setMaxTopPadding(NotificationPanelViewController.this.mQsMaxExpansionHeight + NotificationPanelViewController.this.mQsNotificationTopPadding);
            }
            NotificationPanelViewController.this.positionClockAndNotifications();
            if (!NotificationPanelViewController.this.mQsExpanded || !NotificationPanelViewController.this.mQsFullyExpanded) {
                if (!NotificationPanelViewController.this.mQsExpanded) {
                    NotificationPanelViewController.this.setQsExpansion(r2.mQsMinExpansionHeight + NotificationPanelViewController.this.mLastOverscroll);
                }
            } else {
                NotificationPanelViewController.this.mQsExpansionHeight = r3.mQsMaxExpansionHeight;
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelViewController.this.requestPanelHeightUpdate();
                if (NotificationPanelViewController.this.mQsMaxExpansionHeight != i9) {
                    NotificationPanelViewController notificationPanelViewController4 = NotificationPanelViewController.this;
                    notificationPanelViewController4.startQsSizeChangeAnimation(i9, notificationPanelViewController4.mQsMaxExpansionHeight);
                }
            }
            NotificationPanelViewController notificationPanelViewController5 = NotificationPanelViewController.this;
            notificationPanelViewController5.updateExpandedHeight(notificationPanelViewController5.getExpandedHeight());
            NotificationPanelViewController.this.updateHeader();
            if (NotificationPanelViewController.this.mQsSizeChangeAnimator == null && NotificationPanelViewController.this.mQs != null) {
                NotificationPanelViewController.this.mQs.setHeightOverride(NotificationPanelViewController.this.mQs.getDesiredHeight());
            }
            NotificationPanelViewController.this.updateMaxHeadsUpTranslation();
            NotificationPanelViewController.this.updateGestureExclusionRect();
            if (NotificationPanelViewController.this.mExpandAfterLayoutRunnable != null) {
                NotificationPanelViewController.this.mExpandAfterLayoutRunnable.run();
                NotificationPanelViewController.this.mExpandAfterLayoutRunnable = null;
            }
            DejankUtils.stopDetectingBlockingIpcs("NVP#onLayout");
        }
    }

    private class OnConfigurationChangedListener extends PanelViewController.OnConfigurationChangedListener {
        private OnConfigurationChangedListener() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.PanelViewController.OnConfigurationChangedListener, com.android.systemui.statusbar.phone.PanelView.OnConfigurationChangedListener
        public void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            NotificationPanelViewController.this.mAffordanceHelper.onConfigurationChanged();
            if (configuration.orientation != NotificationPanelViewController.this.mLastOrientation) {
                NotificationPanelViewController.this.resetHorizontalPanelPosition();
            }
            NotificationPanelViewController.this.mLastOrientation = configuration.orientation;
        }
    }

    private class OnApplyWindowInsetsListener implements View.OnApplyWindowInsetsListener {
        private OnApplyWindowInsetsListener() {
        }

        @Override // android.view.View.OnApplyWindowInsetsListener
        public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
            NotificationPanelViewController.this.mNavigationBarBottomHeight = windowInsets.getStableInsetBottom();
            NotificationPanelViewController.this.updateMaxHeadsUpTranslation();
            return windowInsets;
        }
    }
}
