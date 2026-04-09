package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NetworkController;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class CollapsedStatusBarFragment extends Fragment implements CommandQueue.Callbacks, StatusBarStateController.StateListener {
    private View mBatteryBar;
    private BatteryMeterView mBatteryMeterView;
    private BatteryMeterView.BatteryMeterViewCallbacks mBatteryMeterViewCallback;
    private LinearLayout mCenterClockLayout;
    private View mCenteredIconArea;
    private ClockController mClockController;
    private CommandQueue mCommandQueue;
    private ContentResolver mContentResolver;
    private View mCustomCarrierLabel;
    private LinearLayout mCustomIconArea;
    private StatusBarIconController.DarkIconManager mDarkIconManager;
    private int mDisabled1;
    private final Handler mHandler;
    private KeyguardStateController mKeyguardStateController;
    private NetworkController mNetworkController;
    private View mNotificationIconAreaInner;
    private View mOperatorNameFrame;
    private SettingsObserver mSettingsObserver;
    private int mShowCarrierLabel;
    private NetworkController.SignalCallback mSignalCallback;
    private int mSignalClusterEndPadding = 0;
    private PhoneStatusBarView mStatusBar;
    private StatusBar mStatusBarComponent;
    private StatusBarStateController mStatusBarStateController;
    private StatusIconContainer mStatusIcons;
    private LinearLayout mSystemIconArea;

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    public CollapsedStatusBarFragment() {
        Handler handler = new Handler();
        this.mHandler = handler;
        this.mSettingsObserver = new SettingsObserver(handler);
        this.mSignalCallback = new NetworkController.SignalCallback() { // from class: com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.1
            @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
            public void setIsAirplaneMode(NetworkController.IconState iconState) {
                CollapsedStatusBarFragment.this.mCommandQueue.recomputeDisableFlags(CollapsedStatusBarFragment.this.getContext().getDisplayId(), true);
            }
        };
        this.mBatteryMeterViewCallback = new BatteryMeterView.BatteryMeterViewCallbacks() { // from class: com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.2
            @Override // com.android.systemui.BatteryMeterView.BatteryMeterViewCallbacks
            public void onHiddenBattery(boolean z) {
                CollapsedStatusBarFragment.this.mStatusIcons.setPadding(CollapsedStatusBarFragment.this.mStatusIcons.getPaddingLeft(), CollapsedStatusBarFragment.this.mStatusIcons.getPaddingTop(), z ? 0 : CollapsedStatusBarFragment.this.mSignalClusterEndPadding, CollapsedStatusBarFragment.this.mStatusIcons.getPaddingBottom());
            }
        };
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            CollapsedStatusBarFragment.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier"), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            CollapsedStatusBarFragment.this.updateSettings(true);
        }
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContentResolver = getContext().getContentResolver();
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mStatusBarStateController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);
        this.mStatusBarComponent = (StatusBar) Dependency.get(StatusBar.class);
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        this.mSettingsObserver.observe();
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.status_bar, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mStatusBar = (PhoneStatusBarView) view;
        if (bundle != null && bundle.containsKey("panel_state")) {
            this.mStatusBar.restoreHierarchyState(bundle.getSparseParcelableArray("panel_state"));
        }
        int i = R.id.statusIcons;
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(i), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDarkIconManager = darkIconManager;
        darkIconManager.setShouldLog(true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
        this.mSystemIconArea = (LinearLayout) this.mStatusBar.findViewById(R.id.system_icon_area);
        this.mCustomIconArea = (LinearLayout) this.mStatusBar.findViewById(R.id.left_icon_area);
        this.mCenterClockLayout = (LinearLayout) this.mStatusBar.findViewById(R.id.center_clock_layout);
        this.mBatteryBar = this.mStatusBar.findViewById(R.id.battery_bar);
        this.mClockController = new ClockController(getContext(), this.mStatusBar);
        this.mSignalClusterEndPadding = getResources().getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
        this.mStatusIcons = (StatusIconContainer) this.mStatusBar.findViewById(i);
        int i2 = Settings.System.getInt(getContext().getContentResolver(), "status_bar_battery_style", 0);
        StatusIconContainer statusIconContainer = this.mStatusIcons;
        statusIconContainer.setPadding(statusIconContainer.getPaddingLeft(), this.mStatusIcons.getPaddingTop(), i2 == 5 ? 0 : this.mSignalClusterEndPadding, this.mStatusIcons.getPaddingBottom());
        BatteryMeterView batteryMeterView = (BatteryMeterView) this.mStatusBar.findViewById(R.id.battery);
        this.mBatteryMeterView = batteryMeterView;
        batteryMeterView.addCallback(this.mBatteryMeterViewCallback);
        showSystemIconArea(false);
        this.mCustomCarrierLabel = this.mStatusBar.findViewById(R.id.statusbar_carrier_text);
        initEmergencyCryptkeeperText();
        initOperatorName();
        this.mSettingsObserver.observe();
        updateSettings(false);
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        SparseArray<? extends Parcelable> sparseArray = new SparseArray<>();
        this.mStatusBar.saveHierarchyState(sparseArray);
        bundle.putSparseParcelableArray("panel_state", sparseArray);
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mStatusBarStateController.addCallback(this);
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
        this.mStatusBarStateController.removeCallback(this);
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDarkIconManager);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            this.mNetworkController.removeCallback(this.mSignalCallback);
        }
        BatteryMeterView batteryMeterView = this.mBatteryMeterView;
        if (batteryMeterView != null) {
            batteryMeterView.removeCallback(this.mBatteryMeterViewCallback);
        }
    }

    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(R.id.notification_icon_area);
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        if (notificationInnerAreaView.getParent() != null) {
            ((ViewGroup) this.mNotificationIconAreaInner.getParent()).removeView(this.mNotificationIconAreaInner);
        }
        viewGroup.addView(this.mNotificationIconAreaInner);
        ViewGroup viewGroup2 = (ViewGroup) this.mStatusBar.findViewById(R.id.centered_icon_area);
        View centeredNotificationAreaView = notificationIconAreaController.getCenteredNotificationAreaView();
        this.mCenteredIconArea = centeredNotificationAreaView;
        if (centeredNotificationAreaView.getParent() != null) {
            ((ViewGroup) this.mCenteredIconArea.getParent()).removeView(this.mCenteredIconArea);
        }
        viewGroup2.addView(this.mCenteredIconArea);
        showNotificationIconArea(false);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i != getContext().getDisplayId()) {
            return;
        }
        int iAdjustDisableFlags = adjustDisableFlags(i2);
        int i4 = this.mDisabled1 ^ iAdjustDisableFlags;
        this.mDisabled1 = iAdjustDisableFlags;
        if ((i4 & 1048576) != 0) {
            if ((1048576 & iAdjustDisableFlags) != 0) {
                hideSystemIconArea(z);
                hideOperatorName(z);
            } else {
                showSystemIconArea(z);
                showOperatorName(z);
            }
        }
        if ((i4 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0) {
            if ((iAdjustDisableFlags & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0) {
                hideNotificationIconArea(z);
                hideCarrierName(z);
            } else {
                showNotificationIconArea(z);
                showCarrierName(z);
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:6:0x0013  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected int adjustDisableFlags(int r7) {
        /*
            r6 = this;
            com.android.systemui.statusbar.phone.StatusBar r0 = r6.mStatusBarComponent
            boolean r0 = r0.headsUpShouldBeVisible()
            r1 = 1
            r2 = 8388608(0x800000, float:1.1754944E-38)
            if (r0 == 0) goto L23
            com.android.systemui.statusbar.phone.ClockController r3 = r6.mClockController
            com.android.systemui.statusbar.policy.Clock r3 = r3.getClock()
            if (r3 != 0) goto L15
        L13:
            r7 = r7 | r2
            goto L23
        L15:
            int r3 = r3.getId()
            int r4 = com.android.systemui.R.id.clock_right
            if (r3 != r4) goto L1f
            r3 = r1
            goto L20
        L1f:
            r3 = 0
        L20:
            if (r3 != 0) goto L23
            goto L13
        L23:
            com.android.systemui.statusbar.policy.KeyguardStateController r3 = r6.mKeyguardStateController
            boolean r3 = r3.isLaunchTransitionFadingAway()
            r4 = 1048576(0x100000, float:1.469368E-39)
            r5 = 131072(0x20000, float:1.83671E-40)
            if (r3 != 0) goto L4a
            com.android.systemui.statusbar.policy.KeyguardStateController r3 = r6.mKeyguardStateController
            boolean r3 = r3.isKeyguardFadingAway()
            if (r3 != 0) goto L4a
            boolean r3 = r6.shouldHideNotificationIcons()
            if (r3 == 0) goto L4a
            com.android.systemui.plugins.statusbar.StatusBarStateController r3 = r6.mStatusBarStateController
            int r3 = r3.getState()
            if (r3 != r1) goto L47
            if (r0 != 0) goto L4a
        L47:
            r7 = r7 | r5
            r7 = r7 | r4
            r7 = r7 | r2
        L4a:
            com.android.systemui.statusbar.policy.NetworkController r0 = r6.mNetworkController
            if (r0 == 0) goto L62
            boolean r1 = com.android.systemui.statusbar.policy.EncryptionHelper.IS_DATA_ENCRYPTED
            if (r1 == 0) goto L62
            boolean r0 = r0.hasEmergencyCryptKeeperText()
            if (r0 == 0) goto L59
            r7 = r7 | r5
        L59:
            com.android.systemui.statusbar.policy.NetworkController r0 = r6.mNetworkController
            boolean r0 = r0.isRadioOn()
            if (r0 != 0) goto L62
            r7 = r7 | r4
        L62:
            com.android.systemui.plugins.statusbar.StatusBarStateController r0 = r6.mStatusBarStateController
            boolean r0 = r0.isDozing()
            if (r0 == 0) goto L79
            com.android.systemui.statusbar.phone.StatusBar r6 = r6.mStatusBarComponent
            com.android.systemui.statusbar.phone.NotificationPanelViewController r6 = r6.getPanelController()
            boolean r6 = r6.hasCustomClock()
            if (r6 == 0) goto L79
            r6 = 9437184(0x900000, float:1.3224311E-38)
            r7 = r7 | r6
        L79:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.adjustDisableFlags(int):int");
    }

    private boolean shouldHideNotificationIcons() {
        return (!this.mStatusBar.isClosed() && this.mStatusBarComponent.hideStatusBarIconsWhenExpanded()) || this.mStatusBarComponent.hideStatusBarIconsForBouncer();
    }

    public void hideSystemIconArea(boolean z) {
        animateHide(this.mBatteryBar, z);
        animateHide(this.mSystemIconArea, z);
    }

    public void showSystemIconArea(boolean z) {
        animateShow(this.mBatteryBar, z);
        animateShow(this.mSystemIconArea, z);
    }

    public void hideNotificationIconArea(boolean z) {
        animateHide(this.mNotificationIconAreaInner, z);
        animateHide(this.mCenteredIconArea, z);
        animateHide(this.mCustomIconArea, z);
        animateHide(this.mCenterClockLayout, z);
    }

    public void showNotificationIconArea(boolean z) {
        animateShow(this.mNotificationIconAreaInner, z);
        animateShow(this.mCenteredIconArea, z);
        animateShow(this.mCustomIconArea, z);
        animateShow(this.mCenterClockLayout, z);
    }

    public void hideOperatorName(boolean z) {
        View view = this.mOperatorNameFrame;
        if (view != null) {
            animateHide(view, z);
        }
    }

    public void showOperatorName(boolean z) {
        View view = this.mOperatorNameFrame;
        if (view != null) {
            animateShow(view, z);
        }
    }

    private void animateHiddenState(final View view, final int i, boolean z) {
        view.animate().cancel();
        if (!z) {
            view.setAlpha(0.0f);
            view.setVisibility(i);
        } else {
            view.animate().alpha(0.0f).setDuration(160L).setStartDelay(0L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() { // from class: com.android.systemui.statusbar.phone.CollapsedStatusBarFragment$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    view.setVisibility(i);
                }
            });
        }
    }

    public void hideCarrierName(boolean z) {
        View view = this.mCustomCarrierLabel;
        if (view != null) {
            animateHide(view, z);
        }
    }

    public void showCarrierName(boolean z) {
        if (this.mCustomCarrierLabel != null) {
            setCarrierLabel(z);
        }
    }

    private void animateHide(View view, boolean z) {
        if (view.getVisibility() == 8) {
            return;
        }
        animateHiddenState(view, 4, z);
    }

    private void animateShow(View view, boolean z) {
        if (view.getVisibility() == 8) {
            return;
        }
        view.animate().cancel();
        view.setVisibility(0);
        if (!z) {
            view.setAlpha(1.0f);
            return;
        }
        view.animate().alpha(1.0f).setDuration(320L).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50L).withEndAction(null);
        if (this.mKeyguardStateController.isKeyguardFadingAway()) {
            view.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
        }
    }

    private void initEmergencyCryptkeeperText() {
        View viewFindViewById = this.mStatusBar.findViewById(R.id.emergency_cryptkeeper_text);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            if (viewFindViewById != null) {
                ((ViewStub) viewFindViewById).inflate();
            }
            this.mNetworkController.addCallback(this.mSignalCallback);
        } else if (viewFindViewById != null) {
            ((ViewGroup) viewFindViewById.getParent()).removeView(viewFindViewById);
        }
    }

    private void initOperatorName() {
        if (getResources().getBoolean(R.bool.config_showOperatorNameInStatusBar)) {
            this.mOperatorNameFrame = ((ViewStub) this.mStatusBar.findViewById(R.id.operator_name)).inflate();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        int displayId = getContext().getDisplayId();
        int i = this.mDisabled1;
        disable(displayId, i, i, false);
    }

    public void updateSettings(boolean z) {
        this.mShowCarrierLabel = Settings.System.getIntForUser(this.mContentResolver, "status_bar_show_carrier", 0, -2);
        setCarrierLabel(z);
    }

    private void setCarrierLabel(boolean z) {
        int i = this.mShowCarrierLabel;
        if (i == 2 || i == 3) {
            animateShow(this.mCustomCarrierLabel, z);
        } else {
            animateHide(this.mCustomCarrierLabel, z);
        }
    }
}
