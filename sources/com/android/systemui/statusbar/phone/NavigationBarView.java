package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsOnboarding;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.RegionSamplingHelper;
import com.android.systemui.statusbar.policy.DeadZone;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.Consumer;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class NavigationBarView extends FrameLayout implements NavigationModeController.ModeChangedListener, TunerService.Tunable {
    private AutoHideController mAutoHideController;
    private KeyButtonDrawable mBackIcon;
    private final NavigationBarTransitions mBarTransitions;
    private boolean mBlockedGesturalNavigation;
    private final SparseArray<ButtonDispatcher> mButtonDispatchers;
    private Configuration mConfiguration;
    private final ContextualButtonGroup mContextualButtonGroup;
    private int mCurrentRotation;
    View mCurrentView;
    private CustomSettingsObserver mCustomSettingsObserver;
    private final DeadZone mDeadZone;
    private boolean mDeadZoneConsuming;
    int mDisabledFlags;
    private KeyButtonDrawable mDockedIcon;
    private final Consumer<Boolean> mDockedListener;
    private boolean mDockedStackExists;
    private EdgeBackGestureHandler mEdgeBackGestureHandler;
    private FloatingRotationButton mFloatingRotationButton;
    private KeyButtonDrawable mHomeDefaultIcon;
    private boolean mHomeHandleForceHidden;
    private View mHorizontal;
    private final View.OnClickListener mImeSwitcherClickListener;
    private boolean mImeVisible;
    private boolean mInCarMode;
    private boolean mIsUserEnabled;
    private boolean mIsVertical;
    private boolean mLayoutTransitionsEnabled;
    boolean mLongClickableAccessibilityButton;
    private int mNavBarMode;
    private final int mNavColorSampleMargin;
    int mNavigationIconHints;
    private NavigationBarInflaterView mNavigationInflaterView;
    private final ViewTreeObserver.OnComputeInternalInsetsListener mOnComputeInternalInsetsListener;
    private OnVerticalChangedListener mOnVerticalChangedListener;
    private Rect mOrientedHandleSamplingRegion;
    private final OverviewProxyService mOverviewProxyService;
    private NotificationPanelViewController mPanelView;
    private final PluginManager mPluginManager;
    private final View.AccessibilityDelegate mQuickStepAccessibilityDelegate;
    private KeyButtonDrawable mRecentIcon;
    private RecentsOnboarding mRecentsOnboarding;
    private final RegionSamplingHelper mRegionSamplingHelper;
    private RotationButtonController mRotationButtonController;
    private final Consumer<Boolean> mRotationButtonListener;
    private Rect mSamplingBounds;
    private boolean mScreenOn;
    private ScreenPinningNotify mScreenPinningNotify;
    private boolean mShowCursorKeys;
    private final SysUiState mSysUiFlagContainer;
    private Rect mTmpBounds;
    private Configuration mTmpLastConfiguration;
    private final int[] mTmpPosition;
    private final Region mTmpRegion;
    private final NavTransitionListener mTransitionListener;
    private boolean mUseCarModeUi;
    private View mVertical;
    private boolean mWakeAndUnlocking;

    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean z);
    }

    private static String visibilityToString(int i) {
        return i != 4 ? i != 8 ? "VISIBLE" : "GONE" : "INVISIBLE";
    }

    private class NavTransitionListener implements LayoutTransition.TransitionListener {
        private boolean mBackTransitioning;
        private long mDuration;
        private boolean mHomeAppearing;
        private TimeInterpolator mInterpolator;
        private long mStartDelay;

        private NavTransitionListener() {
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == R.id.back) {
                this.mBackTransitioning = true;
                return;
            }
            if (view.getId() == R.id.home && i == 2) {
                this.mHomeAppearing = true;
                this.mStartDelay = layoutTransition.getStartDelay(i);
                this.mDuration = layoutTransition.getDuration(i);
                this.mInterpolator = layoutTransition.getInterpolator(i);
            }
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == R.id.back) {
                this.mBackTransitioning = false;
            } else if (view.getId() == R.id.home && i == 2) {
                this.mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            ButtonDispatcher backButton = NavigationBarView.this.getBackButton();
            if (!this.mBackTransitioning && backButton.getVisibility() == 0 && this.mHomeAppearing && NavigationBarView.this.getHomeButton().getAlpha() == 0.0f) {
                NavigationBarView.this.getBackButton().setAlpha(0.0f);
                ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(backButton, "alpha", 0.0f, 1.0f);
                objectAnimatorOfFloat.setStartDelay(this.mStartDelay);
                objectAnimatorOfFloat.setDuration(this.mDuration);
                objectAnimatorOfFloat.setInterpolator(this.mInterpolator);
                objectAnimatorOfFloat.start();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        if (!this.mEdgeBackGestureHandler.isHandlingGestures()) {
            internalInsetsInfo.setTouchableInsets(0);
        } else {
            internalInsetsInfo.setTouchableInsets(3);
            internalInsetsInfo.touchableRegion.set(getButtonLocations(false, false));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1(Boolean bool) {
        AutoHideController autoHideController;
        if (bool.booleanValue() && (autoHideController = this.mAutoHideController) != null) {
            autoHideController.touchAutoHide();
        }
        notifyActiveTouchRegions();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public NavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCurrentView = null;
        this.mCurrentRotation = -1;
        this.mDisabledFlags = 0;
        this.mNavigationIconHints = 0;
        this.mTmpRegion = new Region();
        this.mTmpPosition = new int[2];
        this.mTmpBounds = new Rect();
        this.mDeadZoneConsuming = false;
        this.mTransitionListener = new NavTransitionListener();
        this.mLayoutTransitionsEnabled = true;
        this.mUseCarModeUi = false;
        this.mInCarMode = false;
        this.mScreenOn = true;
        this.mIsUserEnabled = true;
        SparseArray<ButtonDispatcher> sparseArray = new SparseArray<>();
        this.mButtonDispatchers = sparseArray;
        this.mSamplingBounds = new Rect();
        this.mImeSwitcherClickListener = new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((InputMethodManager) ((FrameLayout) NavigationBarView.this).mContext.getSystemService(InputMethodManager.class)).showInputMethodPickerFromSystem(true, NavigationBarView.this.getContext().getDisplayId());
            }
        };
        this.mQuickStepAccessibilityDelegate = new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.phone.NavigationBarView.2
            private AccessibilityNodeInfo.AccessibilityAction mToggleOverviewAction;

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (this.mToggleOverviewAction == null) {
                    this.mToggleOverviewAction = new AccessibilityNodeInfo.AccessibilityAction(R.id.action_toggle_overview, NavigationBarView.this.getContext().getString(R.string.quick_step_accessibility_toggle_overview));
                }
                accessibilityNodeInfo.addAction(this.mToggleOverviewAction);
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                if (i == R.id.action_toggle_overview) {
                    ((Recents) Dependency.get(Recents.class)).toggleRecentApps();
                    return true;
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        };
        this.mOnComputeInternalInsetsListener = new ViewTreeObserver.OnComputeInternalInsetsListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarView$$ExternalSyntheticLambda0
            public final void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
                this.f$0.lambda$new$0(internalInsetsInfo);
            }
        };
        Consumer<Boolean> consumer = new Consumer() { // from class: com.android.systemui.statusbar.phone.NavigationBarView$$ExternalSyntheticLambda3
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$new$1((Boolean) obj);
            }
        };
        this.mRotationButtonListener = consumer;
        this.mDockedListener = new Consumer() { // from class: com.android.systemui.statusbar.phone.NavigationBarView$$ExternalSyntheticLambda4
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$new$3((Boolean) obj);
            }
        };
        this.mCustomSettingsObserver = new CustomSettingsObserver();
        this.mIsVertical = false;
        this.mLongClickableAccessibilityButton = false;
        int iAddListener = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
        this.mNavBarMode = iAddListener;
        boolean zIsGesturalMode = QuickStepContract.isGesturalMode(iAddListener);
        SysUiState sysUiState = (SysUiState) Dependency.get(SysUiState.class);
        this.mSysUiFlagContainer = sysUiState;
        PluginManager pluginManager = (PluginManager) Dependency.get(PluginManager.class);
        this.mPluginManager = pluginManager;
        int i = R.id.menu_container;
        ContextualButtonGroup contextualButtonGroup = new ContextualButtonGroup(i);
        this.mContextualButtonGroup = contextualButtonGroup;
        int i2 = R.id.ime_switcher;
        ContextualButton contextualButton = new ContextualButton(i2, R.drawable.ic_ime_switcher_default);
        int i3 = R.id.rotate_suggestion;
        RotationContextButton rotationContextButton = new RotationContextButton(i3, R.drawable.ic_sysbar_rotate_button);
        int i4 = R.id.accessibility_button;
        ContextualButton contextualButton2 = new ContextualButton(i4, R.drawable.ic_sysbar_accessibility_button);
        contextualButtonGroup.addButton(contextualButton);
        if (!zIsGesturalMode) {
            contextualButtonGroup.addButton(rotationContextButton);
        }
        contextualButtonGroup.addButton(contextualButton2);
        int i5 = R.id.dpad_left;
        ContextualButton contextualButton3 = new ContextualButton(i5, R.drawable.ic_chevron_start);
        int i6 = R.id.dpad_right;
        ContextualButton contextualButton4 = new ContextualButton(i6, R.drawable.ic_chevron_end);
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mOverviewProxyService = overviewProxyService;
        this.mRecentsOnboarding = new RecentsOnboarding(context, overviewProxyService);
        FloatingRotationButton floatingRotationButton = new FloatingRotationButton(context);
        this.mFloatingRotationButton = floatingRotationButton;
        this.mRotationButtonController = new RotationButtonController(context, R.style.RotateButtonCCWStart90, zIsGesturalMode ? floatingRotationButton : rotationContextButton, consumer);
        this.mConfiguration = new Configuration();
        this.mTmpLastConfiguration = new Configuration();
        this.mConfiguration.updateFrom(context.getResources().getConfiguration());
        this.mScreenPinningNotify = new ScreenPinningNotify(((FrameLayout) this).mContext);
        this.mBarTransitions = new NavigationBarTransitions(this, (CommandQueue) Dependency.get(CommandQueue.class));
        int i7 = R.id.back;
        sparseArray.put(i7, new ButtonDispatcher(i7));
        int i8 = R.id.home;
        sparseArray.put(i8, new ButtonDispatcher(i8));
        int i9 = R.id.home_handle;
        sparseArray.put(i9, new ButtonDispatcher(i9));
        int i10 = R.id.recent_apps;
        sparseArray.put(i10, new ButtonDispatcher(i10));
        sparseArray.put(i2, contextualButton);
        sparseArray.put(i4, contextualButton2);
        sparseArray.put(i3, rotationContextButton);
        sparseArray.put(i5, contextualButton3);
        sparseArray.put(i6, contextualButton4);
        sparseArray.put(i, contextualButtonGroup);
        this.mDeadZone = new DeadZone(this);
        this.mNavColorSampleMargin = getResources().getDimensionPixelSize(R.dimen.navigation_handle_sample_horizontal_margin);
        this.mEdgeBackGestureHandler = new EdgeBackGestureHandler(context, overviewProxyService, sysUiState, pluginManager, new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarView$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateStates();
            }
        });
        this.mRegionSamplingHelper = new RegionSamplingHelper(this, new RegionSamplingHelper.SamplingCallback() { // from class: com.android.systemui.statusbar.phone.NavigationBarView.3
            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public void onRegionDarknessChanged(boolean z) {
                NavigationBarView.this.getLightTransitionsController().setIconsDark(!z, true);
            }

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public Rect getSampledRegion(View view) {
                if (NavigationBarView.this.mOrientedHandleSamplingRegion != null) {
                    return NavigationBarView.this.mOrientedHandleSamplingRegion;
                }
                NavigationBarView.this.updateSamplingRect();
                return NavigationBarView.this.mSamplingBounds;
            }

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public boolean isSamplingEnabled() {
                return Utils.isGesturalModeOnDefaultDisplay(NavigationBarView.this.getContext(), NavigationBarView.this.mNavBarMode);
            }
        });
    }

    public void setAutoHideController(AutoHideController autoHideController) {
        this.mAutoHideController = autoHideController;
    }

    public NavigationBarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public LightBarTransitionsController getLightTransitionsController() {
        return this.mBarTransitions.getLightTransitionsController();
    }

    public void setComponents(NotificationPanelViewController notificationPanelViewController) {
        this.mPanelView = notificationPanelViewController;
        updatePanelSystemUiStateFlags();
    }

    public void setOnVerticalChangedListener(OnVerticalChangedListener onVerticalChangedListener) {
        this.mOnVerticalChangedListener = onVerticalChangedListener;
        notifyVerticalChangedListener(this.mIsVertical);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (QuickStepContract.isGesturalMode(this.mNavBarMode) && this.mImeVisible && motionEvent.getAction() == 0) {
            SysUiStatsLog.write(304, (int) motionEvent.getX(), (int) motionEvent.getY());
        }
        return shouldDeadZoneConsumeTouchEvents(motionEvent) || super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        shouldDeadZoneConsumeTouchEvents(motionEvent);
        return super.onTouchEvent(motionEvent);
    }

    void onTransientStateChanged(boolean z) {
        this.mEdgeBackGestureHandler.onNavBarTransientStateChanged(z);
    }

    void onBarTransition(int i) {
        if (i == 4) {
            this.mRegionSamplingHelper.stop();
            getLightTransitionsController().setIconsDark(false, true);
        } else {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        }
    }

    private boolean shouldDeadZoneConsumeTouchEvents(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDeadZoneConsuming = false;
        }
        if (!this.mDeadZone.onTouchEvent(motionEvent) && !this.mDeadZoneConsuming) {
            return false;
        }
        if (actionMasked == 0) {
            setSlippery(true);
            this.mDeadZoneConsuming = true;
        } else if (actionMasked == 1 || actionMasked == 3) {
            updateSlippery();
            this.mDeadZoneConsuming = false;
        }
        return true;
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public RotationButtonController getRotationButtonController() {
        return this.mRotationButtonController;
    }

    public ButtonDispatcher getRecentsButton() {
        return this.mButtonDispatchers.get(R.id.recent_apps);
    }

    public ButtonDispatcher getBackButton() {
        return this.mButtonDispatchers.get(R.id.back);
    }

    public ButtonDispatcher getHomeButton() {
        return this.mButtonDispatchers.get(R.id.home);
    }

    public ButtonDispatcher getImeSwitchButton() {
        return this.mButtonDispatchers.get(R.id.ime_switcher);
    }

    public ButtonDispatcher getAccessibilityButton() {
        return this.mButtonDispatchers.get(R.id.accessibility_button);
    }

    public RotationContextButton getRotateSuggestionButton() {
        return (RotationContextButton) this.mButtonDispatchers.get(R.id.rotate_suggestion);
    }

    public ButtonDispatcher getHomeHandle() {
        return this.mButtonDispatchers.get(R.id.home_handle);
    }

    private ContextualButton getCursorLeftButton() {
        return (ContextualButton) this.mButtonDispatchers.get(R.id.dpad_left);
    }

    private ContextualButton getCursorRightButton() {
        return (ContextualButton) this.mButtonDispatchers.get(R.id.dpad_right);
    }

    public SparseArray<ButtonDispatcher> getButtonDispatchers() {
        return this.mButtonDispatchers;
    }

    public boolean isRecentsButtonVisible() {
        return getRecentsButton().getVisibility() == 0;
    }

    public boolean isOverviewEnabled() {
        return (this.mDisabledFlags & 16777216) == 0;
    }

    public boolean isQuickStepSwipeUpEnabled() {
        return this.mOverviewProxyService.shouldShowSwipeUpUI() && isOverviewEnabled();
    }

    private void reloadNavIcons() {
        updateIcons(Configuration.EMPTY);
    }

    private void updateIcons(Configuration configuration) {
        int i = configuration.orientation;
        Configuration configuration2 = this.mConfiguration;
        boolean z = i != configuration2.orientation;
        boolean z2 = configuration.densityDpi != configuration2.densityDpi;
        boolean z3 = configuration.getLayoutDirection() != this.mConfiguration.getLayoutDirection();
        if (z || z2) {
            this.mDockedIcon = getDrawable(R.drawable.ic_sysbar_docked);
            this.mHomeDefaultIcon = getHomeDrawable();
        }
        if (z2 || z3) {
            this.mRecentIcon = getDrawable(R.drawable.ic_sysbar_recent);
            getCursorLeftButton().updateIcon();
            getCursorRightButton().updateIcon();
            this.mContextualButtonGroup.updateIcons();
        }
        if (z || z2 || z3) {
            this.mBackIcon = getBackDrawable();
        }
    }

    public KeyButtonDrawable getBackDrawable() {
        KeyButtonDrawable drawable = getDrawable(getBackDrawableRes());
        orientBackButton(drawable);
        return drawable;
    }

    public int getBackDrawableRes() {
        return chooseNavigationIconDrawableRes(R.drawable.ic_sysbar_back, R.drawable.ic_sysbar_back_quick_step);
    }

    public KeyButtonDrawable getHomeDrawable() {
        KeyButtonDrawable drawable;
        if (this.mOverviewProxyService.shouldShowSwipeUpUI()) {
            drawable = getDrawable(R.drawable.ic_sysbar_home_quick_step);
        } else {
            drawable = getDrawable(R.drawable.ic_sysbar_home);
        }
        orientHomeButton(drawable);
        return drawable;
    }

    public KeyButtonDrawable getRecentsDrawable() {
        return getDrawable(R.drawable.ic_sysbar_recent);
    }

    private void orientBackButton(KeyButtonDrawable keyButtonDrawable) {
        float f;
        boolean z = (this.mNavigationIconHints & 1) != 0;
        boolean z2 = this.mConfiguration.getLayoutDirection() == 1;
        float f2 = 0.0f;
        if (z) {
            f = z2 ? 90 : -90;
        } else {
            f = 0.0f;
        }
        if (keyButtonDrawable.getRotation() == f) {
            return;
        }
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            keyButtonDrawable.setRotation(f);
            return;
        }
        if (!this.mOverviewProxyService.shouldShowSwipeUpUI() && !this.mIsVertical && z) {
            f2 = -getResources().getDimension(R.dimen.navbar_back_button_ime_offset);
        }
        ObjectAnimator objectAnimatorOfPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(keyButtonDrawable, PropertyValuesHolder.ofFloat(KeyButtonDrawable.KEY_DRAWABLE_ROTATE, f), PropertyValuesHolder.ofFloat(KeyButtonDrawable.KEY_DRAWABLE_TRANSLATE_Y, f2));
        objectAnimatorOfPropertyValuesHolder.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        objectAnimatorOfPropertyValuesHolder.setDuration(200L);
        objectAnimatorOfPropertyValuesHolder.start();
    }

    private void orientHomeButton(KeyButtonDrawable keyButtonDrawable) {
        keyButtonDrawable.setRotation(this.mIsVertical ? 90.0f : 0.0f);
    }

    private int chooseNavigationIconDrawableRes(int i, int i2) {
        return this.mOverviewProxyService.shouldShowSwipeUpUI() ? i2 : i;
    }

    private KeyButtonDrawable getDrawable(int i) {
        return KeyButtonDrawable.create(((FrameLayout) this).mContext, i, true);
    }

    public void onScreenStateChanged(boolean z) {
        this.mScreenOn = z;
        if (z) {
            if (Utils.isGesturalModeOnDefaultDisplay(getContext(), this.mNavBarMode)) {
                this.mRegionSamplingHelper.start(this.mSamplingBounds);
                return;
            }
            return;
        }
        this.mRegionSamplingHelper.stop();
    }

    public void setWindowVisible(boolean z) {
        this.mRegionSamplingHelper.setWindowVisible(z);
        this.mRotationButtonController.onNavigationBarWindowVisibilityChange(z);
    }

    @Override // android.view.View
    public void setLayoutDirection(int i) {
        reloadNavIcons();
        super.setLayoutDirection(i);
    }

    public void setNavigationIconHints(int i) {
        int i2 = this.mNavigationIconHints;
        if (i == i2) {
            return;
        }
        boolean z = (i & 1) != 0;
        if (z != ((i2 & 1) != 0)) {
            onImeVisibilityChanged(z);
        }
        this.mNavigationIconHints = i;
        updateNavButtonIcons();
    }

    private void onImeVisibilityChanged(boolean z) {
        if (!z) {
            this.mTransitionListener.onBackAltCleared();
        }
        this.mImeVisible = z;
        this.mRotationButtonController.getRotationButton().setCanShowRotationButton(!z && this.mIsUserEnabled);
    }

    public void setDisabledFlags(int i) {
        if (this.mDisabledFlags == i) {
            return;
        }
        boolean zIsOverviewEnabled = isOverviewEnabled();
        this.mDisabledFlags = i;
        if (!zIsOverviewEnabled && isOverviewEnabled()) {
            reloadNavIcons();
        }
        updateNavButtonIcons();
        updateSlippery();
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        updateDisabledSystemUiStateFlags();
    }

    public void updateNavButtonIcons() {
        LayoutTransition layoutTransition;
        boolean z = (this.mNavigationIconHints & 1) != 0;
        KeyButtonDrawable keyButtonDrawable = this.mBackIcon;
        orientBackButton(keyButtonDrawable);
        KeyButtonDrawable keyButtonDrawable2 = this.mHomeDefaultIcon;
        if (!this.mUseCarModeUi) {
            orientHomeButton(keyButtonDrawable2);
        }
        getHomeButton().setImageDrawable(keyButtonDrawable2);
        getBackButton().setImageDrawable(keyButtonDrawable);
        updateRecentsIcon();
        boolean z2 = this.mShowCursorKeys && (this.mNavigationIconHints & 1) != 0;
        this.mContextualButtonGroup.setButtonVisibility(R.id.ime_switcher, this.mImeVisible && !(!QuickStepContract.isGesturalMode(this.mNavBarMode) && QuickStepContract.isLegacyMode(this.mNavBarMode) && z2));
        this.mBarTransitions.reapplyDarkIntensity();
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            z2 &= !this.mImeVisible;
        }
        int i = z2 ? 0 : 4;
        getCursorLeftButton().setVisibility(i);
        getCursorRightButton().setVisibility(i);
        boolean z3 = QuickStepContract.isGesturalMode(this.mNavBarMode) || (this.mDisabledFlags & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0;
        boolean zIsRecentsButtonDisabled = isRecentsButtonDisabled();
        boolean z4 = zIsRecentsButtonDisabled && (2097152 & this.mDisabledFlags) != 0;
        boolean z5 = !z && (this.mEdgeBackGestureHandler.isHandlingGestures() || (this.mDisabledFlags & 4194304) != 0);
        boolean zIsScreenPinningActive = ActivityManagerWrapper.getInstance().isScreenPinningActive();
        if (this.mOverviewProxyService.isEnabled()) {
            zIsRecentsButtonDisabled |= !QuickStepContract.isLegacyMode(this.mNavBarMode);
            if (zIsScreenPinningActive && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                z5 = false;
                z3 = false;
            }
        } else if (zIsScreenPinningActive) {
            z5 = false;
            zIsRecentsButtonDisabled = false;
        }
        boolean z6 = zIsScreenPinningActive ? true : z5;
        ViewGroup viewGroup = (ViewGroup) getCurrentView().findViewById(R.id.nav_buttons);
        if (viewGroup != null && (layoutTransition = viewGroup.getLayoutTransition()) != null && !layoutTransition.getTransitionListeners().contains(this.mTransitionListener)) {
            layoutTransition.addTransitionListener(this.mTransitionListener);
        }
        getBackButton().setVisibility(z6 ? 4 : 0);
        getHomeButton().setVisibility(z3 ? 4 : 0);
        getRecentsButton().setVisibility(zIsRecentsButtonDisabled ? 4 : 0);
        getHomeHandle().setVisibility((z4 || this.mHomeHandleForceHidden) ? 4 : 0);
        notifyActiveTouchRegions();
    }

    public boolean isHomeHandleForceHidden() {
        return this.mHomeHandleForceHidden;
    }

    @VisibleForTesting
    boolean isRecentsButtonDisabled() {
        return (!this.mUseCarModeUi && isOverviewEnabled() && getContext().getDisplayId() == 0) ? false : true;
    }

    private Display getContextDisplay() {
        return getContext().getDisplay();
    }

    public void setLayoutTransitionsEnabled(boolean z) {
        this.mLayoutTransitionsEnabled = z;
        updateLayoutTransitionsEnabled();
    }

    public void setWakeAndUnlocking(boolean z) {
        setUseFadingAnimations(z);
        this.mWakeAndUnlocking = z;
        updateLayoutTransitionsEnabled();
    }

    private void updateLayoutTransitionsEnabled() {
        boolean z = !this.mWakeAndUnlocking && this.mLayoutTransitionsEnabled;
        LayoutTransition layoutTransition = ((ViewGroup) getCurrentView().findViewById(R.id.nav_buttons)).getLayoutTransition();
        if (layoutTransition != null) {
            if (z) {
                layoutTransition.enableTransitionType(2);
                layoutTransition.enableTransitionType(3);
                layoutTransition.enableTransitionType(0);
                layoutTransition.enableTransitionType(1);
                return;
            }
            layoutTransition.disableTransitionType(2);
            layoutTransition.disableTransitionType(3);
            layoutTransition.disableTransitionType(0);
            layoutTransition.disableTransitionType(1);
        }
    }

    private void setUseFadingAnimations(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) ((ViewGroup) getParent()).getLayoutParams();
        if (layoutParams != null) {
            boolean z2 = layoutParams.windowAnimations != 0;
            if (!z2 && z) {
                layoutParams.windowAnimations = R.style.Animation_NavigationBarFadeIn;
            } else if (!z2 || z) {
                return;
            } else {
                layoutParams.windowAnimations = 0;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout((View) getParent(), layoutParams);
        }
    }

    public void onStatusBarPanelStateChanged() {
        updateSlippery();
        updatePanelSystemUiStateFlags();
    }

    public void updateDisabledSystemUiStateFlags() {
        int displayId = ((FrameLayout) this).mContext.getDisplayId();
        boolean z = true;
        SysUiState flag = this.mSysUiFlagContainer.setFlag(1, ActivityManagerWrapper.getInstance().isScreenPinningActive()).setFlag(128, this.mBlockedGesturalNavigation || (this.mDisabledFlags & 16777216) != 0).setFlag(LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT, this.mBlockedGesturalNavigation || (this.mDisabledFlags & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0);
        if (!this.mBlockedGesturalNavigation && (this.mDisabledFlags & 33554432) == 0) {
            z = false;
        }
        flag.setFlag(LineageHardwareManager.FEATURE_VIBRATOR, z).commitUpdate(displayId);
    }

    public void updatePanelSystemUiStateFlags() {
        int displayId = ((FrameLayout) this).mContext.getDisplayId();
        NotificationPanelViewController notificationPanelViewController = this.mPanelView;
        if (notificationPanelViewController != null) {
            boolean z = true;
            SysUiState flag = this.mSysUiFlagContainer.setFlag(4, notificationPanelViewController.isFullyExpanded() && !this.mPanelView.isInSettings());
            if (!this.mBlockedGesturalNavigation && !this.mPanelView.isInSettings()) {
                z = false;
            }
            flag.setFlag(LineageHardwareManager.FEATURE_TOUCH_HOVERING, z).commitUpdate(displayId);
        }
    }

    public void updateStates() {
        boolean zShouldShowSwipeUpUI = this.mOverviewProxyService.shouldShowSwipeUpUI();
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.onLikelyDefaultLayoutChange();
        }
        updateSlippery();
        reloadNavIcons();
        updateNavButtonIcons();
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        WindowManagerWrapper.getInstance().setNavBarVirtualKeyHapticFeedbackEnabled(!zShouldShowSwipeUpUI);
        getHomeButton().setAccessibilityDelegate(zShouldShowSwipeUpUI ? this.mQuickStepAccessibilityDelegate : null);
    }

    public void setBlockedGesturalNavigation(boolean z) {
        this.mBlockedGesturalNavigation = z;
        this.mEdgeBackGestureHandler.setBlockedGesturalNavigation(z);
        updateDisabledSystemUiStateFlags();
        updatePanelSystemUiStateFlags();
    }

    public void updateSlippery() {
        NotificationPanelViewController notificationPanelViewController;
        setSlippery((isQuickStepSwipeUpEnabled() && ((notificationPanelViewController = this.mPanelView) == null || !notificationPanelViewController.isFullyExpanded() || this.mPanelView.isCollapsing())) ? false : true);
    }

    private void setSlippery(boolean z) {
        setWindowFlag(536870912, z);
    }

    private void setWindowFlag(int i, boolean z) {
        WindowManager.LayoutParams layoutParams;
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup == null || (layoutParams = (WindowManager.LayoutParams) viewGroup.getLayoutParams()) == null) {
            return;
        }
        int i2 = layoutParams.flags;
        if (z == ((i2 & i) != 0)) {
            return;
        }
        if (z) {
            layoutParams.flags = i | i2;
        } else {
            layoutParams.flags = (~i) & i2;
        }
        ((WindowManager) getContext().getSystemService("window")).updateViewLayout(viewGroup, layoutParams);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
        this.mBarTransitions.onNavigationModeChanged(i);
        this.mEdgeBackGestureHandler.onNavigationModeChanged(this.mNavBarMode);
        this.mRecentsOnboarding.onNavigationModeChanged(this.mNavBarMode);
        getRotateSuggestionButton().onNavigationModeChanged(this.mNavBarMode);
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        } else {
            this.mRegionSamplingHelper.stop();
        }
    }

    public void setAccessibilityButtonState(boolean z, boolean z2) {
        this.mLongClickableAccessibilityButton = z2;
        getAccessibilityButton().setLongClickable(z2);
        this.mContextualButtonGroup.setButtonVisibility(R.id.accessibility_button, z);
    }

    public void shiftNavigationBarItems(int i, int i2) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.nav_buttons);
        if (viewGroup == null) {
            return;
        }
        viewGroup.setPaddingRelative(viewGroup.getPaddingStart() + i, viewGroup.getPaddingTop() + i2, viewGroup.getPaddingEnd() + i, viewGroup.getPaddingBottom() - i2);
        invalidate();
    }

    void hideRecentsOnboarding() {
        this.mRecentsOnboarding.hide(true);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        NavigationBarInflaterView navigationBarInflaterView = (NavigationBarInflaterView) findViewById(R.id.navigation_inflater);
        this.mNavigationInflaterView = navigationBarInflaterView;
        navigationBarInflaterView.setButtonDispatchers(this.mButtonDispatchers);
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        ((Divider) Dependency.get(Divider.class)).registerInSplitScreenListener(this.mDockedListener);
        updateOrientationViews();
        reloadNavIcons();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        this.mDeadZone.onDraw(canvas);
        super.onDraw(canvas);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSamplingRect() {
        this.mSamplingBounds.setEmpty();
        View currentView = getHomeHandle().getCurrentView();
        if (currentView != null) {
            int[] iArr = new int[2];
            currentView.getLocationOnScreen(iArr);
            Point point = new Point();
            currentView.getContext().getDisplay().getRealSize(point);
            this.mSamplingBounds.set(new Rect(iArr[0] - this.mNavColorSampleMargin, point.y - getNavBarHeight(), iArr[0] + currentView.getWidth() + this.mNavColorSampleMargin, point.y));
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        notifyActiveTouchRegions();
        this.mRecentsOnboarding.setNavBarHeight(getMeasuredHeight());
    }

    public void notifyActiveTouchRegions() {
        this.mOverviewProxyService.onActiveNavBarRegionChanges(getButtonLocations(true, true));
    }

    private Region getButtonLocations(boolean z, boolean z2) {
        this.mTmpRegion.setEmpty();
        updateButtonLocation(getBackButton(), z2);
        updateButtonLocation(getHomeButton(), z2);
        updateButtonLocation(getRecentsButton(), z2);
        updateButtonLocation(getImeSwitchButton(), z2);
        updateButtonLocation(getAccessibilityButton(), z2);
        if (z && this.mFloatingRotationButton.isVisible()) {
            updateButtonLocation(this.mFloatingRotationButton.getCurrentView(), z2);
        } else {
            updateButtonLocation(getRotateSuggestionButton(), z2);
        }
        return this.mTmpRegion;
    }

    private void updateButtonLocation(ButtonDispatcher buttonDispatcher, boolean z) {
        View currentView = buttonDispatcher.getCurrentView();
        if (currentView == null || !buttonDispatcher.isVisible()) {
            return;
        }
        updateButtonLocation(currentView, z);
    }

    private void updateButtonLocation(View view, boolean z) {
        if (z) {
            view.getBoundsOnScreen(this.mTmpBounds);
        } else {
            view.getLocationInWindow(this.mTmpPosition);
            Rect rect = this.mTmpBounds;
            int[] iArr = this.mTmpPosition;
            rect.set(iArr[0], iArr[1], iArr[0] + view.getWidth(), this.mTmpPosition[1] + view.getHeight());
        }
        this.mTmpRegion.op(this.mTmpBounds, Region.Op.UNION);
    }

    private void updateOrientationViews() {
        this.mHorizontal = findViewById(R.id.horizontal);
        this.mVertical = findViewById(R.id.vertical);
        updateCurrentView();
    }

    boolean needsReorient(int i) {
        return this.mCurrentRotation != i;
    }

    private void updateCurrentView() {
        resetViews();
        View view = this.mIsVertical ? this.mVertical : this.mHorizontal;
        this.mCurrentView = view;
        view.setVisibility(0);
        this.mNavigationInflaterView.setVertical(this.mIsVertical);
        int rotation = getContextDisplay().getRotation();
        this.mCurrentRotation = rotation;
        this.mNavigationInflaterView.setAlternativeOrder(rotation == 1);
        this.mNavigationInflaterView.updateButtonDispatchersCurrentView();
        updateLayoutTransitionsEnabled();
    }

    private void resetViews() {
        this.mHorizontal.setVisibility(8);
        this.mVertical.setVisibility(8);
    }

    private void updateRecentsIcon() {
        this.mDockedIcon.setRotation((this.mDockedStackExists && this.mIsVertical) ? 90.0f : 0.0f);
        getRecentsButton().setImageDrawable(this.mDockedStackExists ? this.mDockedIcon : this.mRecentIcon);
        this.mBarTransitions.reapplyDarkIntensity();
    }

    public void showPinningEnterExitToast(boolean z) {
        if (z) {
            this.mScreenPinningNotify.showPinningStartToast();
        } else {
            this.mScreenPinningNotify.showPinningExitToast();
        }
    }

    public void showPinningEscapeToast() {
        this.mScreenPinningNotify.showEscapeToast(this.mNavBarMode == 2, isRecentsButtonVisible());
    }

    public NavigationBarFrame getNavbarFrame() {
        return (NavigationBarFrame) getRootView();
    }

    public void reorient() {
        updateCurrentView();
        ((NavigationBarFrame) getRootView()).setDeadZone(this.mDeadZone);
        this.mDeadZone.onConfigurationChanged(this.mCurrentRotation);
        this.mBarTransitions.init();
        if (!isLayoutDirectionResolved()) {
            resolveLayoutDirection();
        }
        updateNavButtonIcons();
        getHomeButton().setVertical(this.mIsVertical);
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) throws Resources.NotFoundException {
        int dimensionPixelSize;
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        boolean z = size > 0 && size2 > size && !QuickStepContract.isGesturalMode(this.mNavBarMode);
        if (z != this.mIsVertical) {
            this.mIsVertical = z;
            reorient();
            notifyVerticalChangedListener(z);
        }
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            if (this.mIsVertical) {
                dimensionPixelSize = getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_42);
            } else {
                dimensionPixelSize = getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_40);
            }
            this.mBarTransitions.setBackgroundFrame(new Rect(0, getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_37) - dimensionPixelSize, size, size2));
        }
        super.onMeasure(i, i2);
    }

    private int getNavBarHeight() {
        if (this.mIsVertical) {
            return getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_42);
        }
        return getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_40);
    }

    private void notifyVerticalChangedListener(boolean z) {
        OnVerticalChangedListener onVerticalChangedListener = this.mOnVerticalChangedListener;
        if (onVerticalChangedListener != null) {
            onVerticalChangedListener.onVerticalChanged(z);
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTmpLastConfiguration.updateFrom(this.mConfiguration);
        this.mConfiguration.updateFrom(configuration);
        boolean zUpdateCarMode = updateCarMode();
        updateIcons(this.mTmpLastConfiguration);
        updateRecentsIcon();
        this.mRecentsOnboarding.onConfigurationChanged(this.mConfiguration);
        if (!zUpdateCarMode) {
            Configuration configuration2 = this.mTmpLastConfiguration;
            if (configuration2.densityDpi == this.mConfiguration.densityDpi && configuration2.getLayoutDirection() == this.mConfiguration.getLayoutDirection()) {
                return;
            }
        }
        updateNavButtonIcons();
    }

    private boolean updateCarMode() {
        Configuration configuration = this.mConfiguration;
        if (configuration != null) {
            boolean z = (configuration.uiMode & 15) == 3;
            if (z != this.mInCarMode) {
                this.mInCarMode = z;
                this.mUseCarModeUi = false;
            }
        }
        return false;
    }

    private String getResourceName(int i) {
        if (i == 0) {
            return "(null)";
        }
        try {
            return getContext().getResources().getResourceName(i);
        } catch (Resources.NotFoundException unused) {
            return "(unknown)";
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
        reorient();
        onNavigationModeChanged(this.mNavBarMode);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "lineagesystem:navigation_bar_menu_arrow_keys");
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.registerListeners();
        }
        this.mEdgeBackGestureHandler.onNavBarAttached();
        getViewTreeObserver().addOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
        this.mCustomSettingsObserver.observe();
        this.mCustomSettingsObserver.update();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((NavigationModeController) Dependency.get(NavigationModeController.class)).removeListener(this);
        setUpSwipeUpOnboarding(false);
        for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
            this.mButtonDispatchers.valueAt(i).onDestroy();
        }
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.unregisterListeners();
        }
        this.mEdgeBackGestureHandler.onNavBarDetached();
        getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
        this.mCustomSettingsObserver.stop();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("lineagesystem:navigation_bar_menu_arrow_keys".equals(str)) {
            this.mShowCursorKeys = TunerService.parseIntegerSwitch(str2, false);
            setNavigationIconHints(this.mNavigationIconHints);
        }
    }

    private void setUpSwipeUpOnboarding(boolean z) {
        if (z) {
            this.mRecentsOnboarding.onConnectedToLauncher();
        } else {
            this.mRecentsOnboarding.onDisconnectedFromLauncher();
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NavigationBarView {");
        Rect rect = new Rect();
        Point point = new Point();
        getContextDisplay().getRealSize(point);
        printWriter.println(String.format("      this: " + StatusBar.viewInfo(this) + " " + visibilityToString(getVisibility()), new Object[0]));
        getWindowVisibleDisplayFrame(rect);
        boolean z = rect.right > point.x || rect.bottom > point.y;
        StringBuilder sb = new StringBuilder();
        sb.append("      window: ");
        sb.append(rect.toShortString());
        sb.append(" ");
        sb.append(visibilityToString(getWindowVisibility()));
        sb.append(z ? " OFFSCREEN!" : "");
        printWriter.println(sb.toString());
        printWriter.println(String.format("      mCurrentView: id=%s (%dx%d) %s %f", getResourceName(getCurrentView().getId()), Integer.valueOf(getCurrentView().getWidth()), Integer.valueOf(getCurrentView().getHeight()), visibilityToString(getCurrentView().getVisibility()), Float.valueOf(getCurrentView().getAlpha())));
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mDisabledFlags);
        objArr[1] = this.mIsVertical ? "true" : "false";
        objArr[2] = Float.valueOf(getLightTransitionsController().getCurrentDarkIntensity());
        printWriter.println(String.format("      disabled=0x%08x vertical=%s darkIntensity=%.2f", objArr));
        printWriter.println("      mOrientedHandleSamplingRegion: " + this.mOrientedHandleSamplingRegion);
        dumpButton(printWriter, "back", getBackButton());
        dumpButton(printWriter, "home", getHomeButton());
        dumpButton(printWriter, "rcnt", getRecentsButton());
        dumpButton(printWriter, "rota", getRotateSuggestionButton());
        dumpButton(printWriter, "a11y", getAccessibilityButton());
        dumpButton(printWriter, "ime", getImeSwitchButton());
        dumpButton(printWriter, "curl", getCursorLeftButton());
        dumpButton(printWriter, "curr", getCursorRightButton());
        printWriter.println("    }");
        printWriter.println("    mScreenOn: " + this.mScreenOn);
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.dump(printWriter);
        }
        this.mContextualButtonGroup.dump(printWriter);
        this.mRecentsOnboarding.dump(printWriter);
        this.mRegionSamplingHelper.dump(printWriter);
        this.mEdgeBackGestureHandler.dump(printWriter);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int systemWindowInsetLeft = windowInsets.getSystemWindowInsetLeft();
        int systemWindowInsetRight = windowInsets.getSystemWindowInsetRight();
        setPadding(systemWindowInsetLeft, windowInsets.getSystemWindowInsetTop(), systemWindowInsetRight, windowInsets.getSystemWindowInsetBottom());
        this.mEdgeBackGestureHandler.setInsets(systemWindowInsetLeft, systemWindowInsetRight);
        boolean z = !QuickStepContract.isGesturalMode(this.mNavBarMode) || windowInsets.getSystemWindowInsetBottom() == 0;
        setClipChildren(z);
        setClipToPadding(z);
        NavigationBarController navigationBarController = (NavigationBarController) Dependency.get(NavigationBarController.class);
        AssistHandleViewController assistHandlerViewController = navigationBarController == null ? null : navigationBarController.getAssistHandlerViewController();
        if (assistHandlerViewController != null) {
            assistHandlerViewController.setBottomOffset(windowInsets.getSystemWindowInsetBottom());
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private static void dumpButton(PrintWriter printWriter, String str, ButtonDispatcher buttonDispatcher) {
        printWriter.print("      " + str + ": ");
        if (buttonDispatcher == null) {
            printWriter.print("null");
        } else {
            printWriter.print(visibilityToString(buttonDispatcher.getVisibility()) + " alpha=" + buttonDispatcher.getAlpha());
        }
        printWriter.println();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(final Boolean bool) {
        post(new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarView$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$2(bool);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2(Boolean bool) {
        this.mDockedStackExists = bool.booleanValue();
        updateRecentsIcon();
    }

    private class CustomSettingsObserver extends ContentObserver {
        CustomSettingsObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        void observe() {
            ((FrameLayout) NavigationBarView.this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("enable_floating_rotation_button"), false, this, -1);
        }

        void stop() {
            ((FrameLayout) NavigationBarView.this).mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update();
        }

        void update() {
            boolean z = Settings.System.getInt(NavigationBarView.this.getContext().getContentResolver(), "enable_floating_rotation_button", 1) == 1;
            if (NavigationBarView.this.mIsUserEnabled != z) {
                NavigationBarView.this.mIsUserEnabled = z;
                if (NavigationBarView.this.mRotationButtonController == null) {
                    return;
                }
                NavigationBarView.this.mRotationButtonController.getRotationButton().setCanShowRotationButton(NavigationBarView.this.mIsUserEnabled);
            }
        }
    }
}
