package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.util.leak.RotationUtils;
import java.util.Objects;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class PhoneStatusBarView extends PanelBar implements CommandQueue.Callbacks {
    StatusBar mBar;
    private DarkIconDispatcher.DarkReceiver mBattery;
    private View mCenterIconSpace;
    private DarkIconDispatcher.DarkReceiver mClock;
    private DarkIconDispatcher.DarkReceiver mClockCentre;
    private DarkIconDispatcher.DarkReceiver mClockRight;
    private final CommandQueue mCommandQueue;
    private int mCutoutSideNudge;
    private View mCutoutSpace;
    private DisplayCutout mDisplayCutout;
    private boolean mHeadsUpVisible;
    private Runnable mHideExpandedRunnable;
    boolean mIsFullyOpenedPanel;
    private float mMinFraction;
    private RotationButtonController mRotationButtonController;
    private final Consumer<Boolean> mRotationButtonListener;
    private int mRotationOrientation;
    private int mRoundedCornerPadding;
    private ScrimController mScrimController;
    private int mStatusBarHeight;

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$new$0(Boolean bool) {
    }

    public PhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsFullyOpenedPanel = false;
        this.mHideExpandedRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarView.1
            @Override // java.lang.Runnable
            public void run() {
                PhoneStatusBarView phoneStatusBarView = PhoneStatusBarView.this;
                if (phoneStatusBarView.mPanelFraction == 0.0f) {
                    phoneStatusBarView.mBar.makeExpandedInvisible();
                }
            }
        };
        this.mRotationOrientation = -1;
        this.mCutoutSideNudge = 0;
        this.mRoundedCornerPadding = 0;
        PhoneStatusBarView$$ExternalSyntheticLambda0 phoneStatusBarView$$ExternalSyntheticLambda0 = new Consumer() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarView$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PhoneStatusBarView.lambda$new$0((Boolean) obj);
            }
        };
        this.mRotationButtonListener = phoneStatusBarView$$ExternalSyntheticLambda0;
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        if (hasNavigationBar()) {
            return;
        }
        this.mRotationButtonController = new RotationButtonController(context, R.style.RotateButtonCCWStart90, new FloatingRotationButton(context), phoneStatusBarView$$ExternalSyntheticLambda0);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onRotationProposal(int i, boolean z) {
        if (this.mRotationButtonController == null || hasNavigationBar()) {
            return;
        }
        this.mRotationButtonController.onRotationProposal(i, getDisplay().getRotation(), z);
    }

    private boolean hasNavigationBar() {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(0);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public void setBar(StatusBar statusBar) {
        this.mBar = statusBar;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
    }

    public void shiftStatusBarItems(int i, int i2) {
        View viewFindViewById = findViewById(R.id.status_bar_contents);
        if (viewFindViewById == null) {
            return;
        }
        viewFindViewById.setPaddingRelative(viewFindViewById.getPaddingStart() + i, viewFindViewById.getPaddingTop() + i2, viewFindViewById.getPaddingEnd() + i, viewFindViewById.getPaddingBottom() - i2);
        invalidate();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public void onFinishInflate() {
        this.mBattery = (DarkIconDispatcher.DarkReceiver) findViewById(R.id.battery);
        this.mCutoutSpace = findViewById(R.id.cutout_space_view);
        this.mCenterIconSpace = findViewById(R.id.centered_icon_area);
        this.mClock = (DarkIconDispatcher.DarkReceiver) findViewById(R.id.clock);
        this.mClockCentre = (DarkIconDispatcher.DarkReceiver) findViewById(R.id.clock_center);
        this.mClockRight = (DarkIconDispatcher.DarkReceiver) findViewById(R.id.clock_right);
        updateResources();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() throws Resources.NotFoundException {
        super.onAttachedToWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mBattery);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mClock);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mClockCentre);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mClockRight);
        if (updateOrientationAndCutout()) {
            updateLayoutForCutout();
        }
        if (this.mRotationButtonController == null || hasNavigationBar()) {
            return;
        }
        this.mRotationButtonController.registerListeners();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mBattery);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mClock);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mClockCentre);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mClockRight);
        this.mDisplayCutout = null;
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.unregisterListeners();
            this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) throws Resources.NotFoundException {
        if (updateOrientationAndCutout()) {
            updateLayoutForCutout();
            requestLayout();
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private boolean updateOrientationAndCutout() {
        boolean z;
        int exactRotation = RotationUtils.getExactRotation(((FrameLayout) this).mContext);
        if (exactRotation != this.mRotationOrientation) {
            this.mRotationOrientation = exactRotation;
            z = true;
        } else {
            z = false;
        }
        if (Objects.equals(getRootWindowInsets().getDisplayCutout(), this.mDisplayCutout)) {
            return z;
        }
        this.mDisplayCutout = getRootWindowInsets().getDisplayCutout();
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public boolean panelEnabled() {
        return this.mCommandQueue.panelsEnabled();
    }

    public boolean onRequestSendAccessibilityEventInternal(View view, AccessibilityEvent accessibilityEvent) {
        if (!super.onRequestSendAccessibilityEventInternal(view, accessibilityEvent)) {
            return false;
        }
        AccessibilityEvent accessibilityEventObtain = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(accessibilityEventObtain);
        dispatchPopulateAccessibilityEvent(accessibilityEventObtain);
        accessibilityEvent.appendRecord(accessibilityEventObtain);
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelPeeked() {
        super.onPanelPeeked();
        this.mBar.makeExpandedVisible(false);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelCollapsed() {
        super.onPanelCollapsed();
        post(this.mHideExpandedRunnable);
        this.mIsFullyOpenedPanel = false;
    }

    public void removePendingHideExpandedRunnables() {
        removeCallbacks(this.mHideExpandedRunnable);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelFullyOpened() {
        super.onPanelFullyOpened();
        if (!this.mIsFullyOpenedPanel) {
            this.mPanel.getView().sendAccessibilityEvent(32);
        }
        this.mIsFullyOpenedPanel = true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mBar.interceptTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onTrackingStarted() {
        super.onTrackingStarted();
        this.mBar.onTrackingStarted();
        this.mScrimController.onTrackingStarted();
        removePendingHideExpandedRunnables();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onClosingFinished() {
        super.onClosingFinished();
        this.mBar.onClosingFinished();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onTrackingStopped(boolean z) {
        super.onTrackingStopped(z);
        this.mBar.onTrackingStopped(z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mScrimController.onExpandingFinished();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mBar.interceptTouchEvent(motionEvent) || super.onInterceptTouchEvent(motionEvent);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void panelScrimMinFractionChanged(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("minFraction cannot be NaN");
        }
        if (this.mMinFraction != f) {
            this.mMinFraction = f;
            updateScrimFraction();
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void panelExpansionChanged(float f, boolean z) {
        super.panelExpansionChanged(f, z);
        updateScrimFraction();
        if ((f == 0.0f || f == 1.0f) && this.mBar.getNavigationBarView() != null) {
            this.mBar.getNavigationBarView().onStatusBarPanelStateChanged();
        }
    }

    private void updateScrimFraction() {
        float fMax = this.mPanelFraction;
        float f = this.mMinFraction;
        if (f < 1.0f) {
            fMax = Math.max((fMax - f) / (1.0f - f), 0.0f);
        }
        this.mScrimController.setPanelExpansion(fMax);
    }

    public void updateResources() {
        this.mCutoutSideNudge = getResources().getDimensionPixelSize(R.dimen.display_cutout_margin_consumption);
        this.mRoundedCornerPadding = getResources().getDimensionPixelSize(R.dimen.rounded_corner_content_padding);
        updateStatusBarHeight();
    }

    private void updateStatusBarHeight() throws Resources.NotFoundException {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        this.mStatusBarHeight = dimensionPixelSize;
        layoutParams.height = dimensionPixelSize - i;
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.status_bar_padding_top);
        int dimensionPixelSize3 = getResources().getDimensionPixelSize(R.dimen.status_bar_padding_start);
        findViewById(R.id.status_bar_contents).setPaddingRelative(dimensionPixelSize3, dimensionPixelSize2, getResources().getDimensionPixelSize(R.dimen.status_bar_padding_end), 0);
        findViewById(R.id.notification_lights_out).setPaddingRelative(0, dimensionPixelSize3, 0, 0);
        setLayoutParams(layoutParams);
    }

    private void updateLayoutForCutout() throws Resources.NotFoundException {
        updateStatusBarHeight();
        updateCutoutLocation(StatusBarWindowView.cornerCutoutMargins(this.mDisplayCutout, getDisplay()));
        updateSafeInsets(StatusBarWindowView.statusBarCornerCutoutMargins(this.mDisplayCutout, getDisplay(), this.mRotationOrientation, this.mStatusBarHeight));
    }

    private void updateCutoutLocation(Pair<Integer, Integer> pair) {
        if (this.mCutoutSpace == null) {
            return;
        }
        DisplayCutout displayCutout = this.mDisplayCutout;
        if (displayCutout == null || displayCutout.isEmpty() || pair != null) {
            this.mCenterIconSpace.setVisibility(0);
            this.mCutoutSpace.setVisibility(8);
            return;
        }
        this.mCenterIconSpace.setVisibility(8);
        this.mCutoutSpace.setVisibility(0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mCutoutSpace.getLayoutParams();
        Rect rect = new Rect();
        ScreenDecorations.DisplayCutoutView.boundsFromDirection(this.mDisplayCutout, 48, rect);
        int i = rect.left;
        int i2 = this.mCutoutSideNudge;
        rect.left = i + i2;
        rect.right -= i2;
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
    }

    private void updateSafeInsets(Pair<Integer, Integer> pair) {
        Pair<Integer, Integer> pairPaddingNeededForCutoutAndRoundedCorner = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(this.mDisplayCutout, pair, this.mRoundedCornerPadding);
        setPadding(((Integer) pairPaddingNeededForCutoutAndRoundedCorner.first).intValue(), getPaddingTop(), ((Integer) pairPaddingNeededForCutoutAndRoundedCorner.second).intValue(), getPaddingBottom());
    }

    public void setHeadsUpVisible(boolean z) {
        this.mHeadsUpVisible = z;
        updateVisibility();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    protected boolean shouldPanelBeVisible() {
        return this.mHeadsUpVisible || super.shouldPanelBeVisible();
    }
}
