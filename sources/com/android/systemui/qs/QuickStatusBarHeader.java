package com.android.systemui.qs;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.ZenModeConfig;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.Utils;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.DualToneHandler;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.privacy.OngoingPrivacyChip;
import com.android.systemui.privacy.PrivacyChipEvent;
import com.android.systemui.privacy.PrivacyItem;
import com.android.systemui.privacy.PrivacyItemController;
import com.android.systemui.qs.QSDetail;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.qs.carrier.QSCarrierGroup;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.phone.StatusIconContainer;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DateView;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.RingerModeTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/* loaded from: classes.dex */
public class QuickStatusBarHeader extends RelativeLayout implements View.OnClickListener, NextAlarmController.NextAlarmChangeCallback, ZenModeController.Callback, LifecycleOwner, TunerService.Tunable {
    private final ActivityStarter mActivityStarter;
    private final NextAlarmController mAlarmController;
    private boolean mAllIndicatorsEnabled;
    private BatteryMeterView mBatteryIcon;
    private BatteryMeterView mBatteryRemainingIcon;
    private final BlurUtils mBlurUtils;
    private BrightnessController mBrightnessController;
    private BroadcastDispatcher mBroadcastDispatcher;
    private QSCarrierGroup mCarrierGroup;
    private Clock mClockView;
    private final CommandQueue mCommandQueue;
    private int mContentMarginEnd;
    private int mContentMarginStart;
    private int mCutOutPaddingLeft;
    private int mCutOutPaddingRight;
    private DateView mDateView;
    private DualToneHandler mDualToneHandler;
    private boolean mExpanded;
    private float mExpandedHeaderAlpha;
    private boolean mHasTopCutout;
    private boolean mHeaderImageEnabled;
    protected QuickQSPanel mHeaderQsPanel;
    private TouchAnimator mHeaderTextContainerAlphaAnimator;
    private View mHeaderTextContainerView;
    protected QSTileHost mHost;
    private StatusBarIconController.TintedIconManager mIconManager;
    private boolean mIsQsAutoBrightnessEnabled;
    private boolean mIsQuickQsBrightnessEnabled;
    private float mKeyguardExpansionFraction;
    private boolean mLandscape;
    private final LifecycleRegistry mLifecycle;
    private boolean mListening;
    private boolean mMicCameraIndicatorsEnabled;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private View mNextAlarmContainer;
    private ImageView mNextAlarmIcon;
    private TextView mNextAlarmTextView;
    private PrivacyItemController.Callback mPICCallback;
    private OngoingPrivacyChip mPrivacyChip;
    private TouchAnimator mPrivacyChipAlphaAnimator;
    private boolean mPrivacyChipLogged;
    private PrivacyItemController mPrivacyItemController;
    private int mQSBatteryStyle;
    private boolean mQsDisabled;
    private QSPanel mQsPanel;
    private View mQuickQsBrightness;
    private View mQuickQsStatusIcons;
    private View mRingerContainer;
    private int mRingerMode;
    private ImageView mRingerModeIcon;
    private TextView mRingerModeTextView;
    private RingerModeTracker mRingerModeTracker;
    private int mRoundedCornerPadding;
    private Space mSpace;
    private int mStatusBarBatteryStyle;
    private final StatusBarIconController mStatusBarIconController;
    private int mStatusBarPaddingTop;
    private TouchAnimator mStatusIconsAlphaAnimator;
    private View mStatusSeparator;
    private View mSystemIconsView;
    private final UiEventLogger mUiEventLogger;
    private int mWaterfallTopInset;
    private final ZenModeController mZenController;

    public static float getColorIntensity(int i) {
        return i == -1 ? 0.0f : 1.0f;
    }

    public QuickStatusBarHeader(Context context, AttributeSet attributeSet, NextAlarmController nextAlarmController, ZenModeController zenModeController, StatusBarIconController statusBarIconController, ActivityStarter activityStarter, PrivacyItemController privacyItemController, CommandQueue commandQueue, RingerModeTracker ringerModeTracker, UiEventLogger uiEventLogger, BroadcastDispatcher broadcastDispatcher) {
        super(context, attributeSet);
        this.mRingerMode = 2;
        this.mLifecycle = new LifecycleRegistry(this);
        this.mHasTopCutout = false;
        this.mStatusBarPaddingTop = 0;
        this.mRoundedCornerPadding = 0;
        this.mExpandedHeaderAlpha = 1.0f;
        this.mPrivacyChipLogged = false;
        this.mPICCallback = new PrivacyItemController.Callback() { // from class: com.android.systemui.qs.QuickStatusBarHeader.1
            @Override // com.android.systemui.privacy.PrivacyItemController.Callback
            public void onPrivacyItemsChanged(List<PrivacyItem> list) {
                QuickStatusBarHeader.this.mPrivacyChip.setPrivacyList(list);
                QuickStatusBarHeader.this.setChipVisibility(!list.isEmpty());
            }

            @Override // com.android.systemui.privacy.PrivacyItemController.Callback
            public void onFlagAllChanged(boolean z) {
                if (QuickStatusBarHeader.this.mAllIndicatorsEnabled != z) {
                    QuickStatusBarHeader.this.mAllIndicatorsEnabled = z;
                    update();
                }
            }

            @Override // com.android.systemui.privacy.PrivacyItemController.Callback
            public void onFlagMicCameraChanged(boolean z) {
                if (QuickStatusBarHeader.this.mMicCameraIndicatorsEnabled != z) {
                    QuickStatusBarHeader.this.mMicCameraIndicatorsEnabled = z;
                    update();
                }
            }

            private void update() {
                ((StatusIconContainer) QuickStatusBarHeader.this.requireViewById(R.id.statusIcons)).setIgnoredSlots(QuickStatusBarHeader.this.getIgnoredIconSlots());
                QuickStatusBarHeader.this.setChipVisibility(!r2.mPrivacyChip.getPrivacyList().isEmpty());
            }
        };
        this.mAlarmController = nextAlarmController;
        this.mZenController = zenModeController;
        this.mStatusBarIconController = statusBarIconController;
        this.mActivityStarter = activityStarter;
        this.mPrivacyItemController = privacyItemController;
        this.mDualToneHandler = new DualToneHandler(new ContextThemeWrapper(context, R.style.QSHeaderTheme));
        this.mCommandQueue = commandQueue;
        this.mRingerModeTracker = ringerModeTracker;
        this.mUiEventLogger = uiEventLogger;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mBlurUtils = new BlurUtils(((RelativeLayout) this).mContext.getResources(), new DumpManager());
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        this.mHeaderQsPanel = (QuickQSPanel) findViewById(R.id.quick_qs_panel);
        this.mSystemIconsView = findViewById(R.id.quick_status_bar_system_icons);
        this.mQuickQsStatusIcons = findViewById(R.id.quick_qs_status_icons);
        StatusIconContainer statusIconContainer = (StatusIconContainer) findViewById(R.id.statusIcons);
        statusIconContainer.addIgnoredSlots(getIgnoredIconSlots());
        statusIconContainer.setShouldRestrictIcons(false);
        this.mIconManager = new StatusBarIconController.TintedIconManager(statusIconContainer, this.mCommandQueue);
        this.mQuickQsBrightness = findViewById(R.id.quick_qs_brightness_bar);
        this.mBrightnessController = new BrightnessController(getContext(), (ImageView) this.mQuickQsBrightness.findViewById(R.id.brightness_icon), (ToggleSlider) this.mQuickQsBrightness.findViewById(R.id.brightness_slider), this.mBroadcastDispatcher);
        this.mHeaderTextContainerView = findViewById(R.id.header_text_container);
        this.mStatusSeparator = findViewById(R.id.status_separator);
        this.mNextAlarmIcon = (ImageView) findViewById(R.id.next_alarm_icon);
        this.mNextAlarmTextView = (TextView) findViewById(R.id.next_alarm_text);
        View viewFindViewById = findViewById(R.id.alarm_container);
        this.mNextAlarmContainer = viewFindViewById;
        viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QuickStatusBarHeader$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.onClick(view);
            }
        });
        this.mRingerModeIcon = (ImageView) findViewById(R.id.ringer_mode_icon);
        this.mRingerModeTextView = (TextView) findViewById(R.id.ringer_mode_text);
        View viewFindViewById2 = findViewById(R.id.ringer_container);
        this.mRingerContainer = viewFindViewById2;
        viewFindViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QuickStatusBarHeader$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.onClick(view);
            }
        });
        OngoingPrivacyChip ongoingPrivacyChip = (OngoingPrivacyChip) findViewById(R.id.privacy_chip);
        this.mPrivacyChip = ongoingPrivacyChip;
        ongoingPrivacyChip.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QuickStatusBarHeader$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.onClick(view);
            }
        });
        this.mCarrierGroup = (QSCarrierGroup) findViewById(R.id.carrier_group);
        Rect rect = new Rect(0, 0, 0, 0);
        int singleColor = this.mDualToneHandler.getSingleColor(getColorIntensity(Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorForeground)));
        int i = R.id.clock;
        applyDarkness(i, rect, 0.0f, -1);
        this.mIconManager.setTint(singleColor);
        this.mNextAlarmIcon.setImageTintList(ColorStateList.valueOf(singleColor));
        this.mRingerModeIcon.setImageTintList(ColorStateList.valueOf(singleColor));
        Clock clock = (Clock) findViewById(i);
        this.mClockView = clock;
        clock.setOnClickListener(this);
        this.mClockView.setQsHeader();
        this.mDateView = (DateView) findViewById(R.id.date);
        this.mSpace = (Space) findViewById(R.id.space);
        this.mBatteryRemainingIcon = (BatteryMeterView) findViewById(R.id.batteryRemainingIcon);
        this.mBatteryIcon = (BatteryMeterView) findViewById(R.id.batteryIcon);
        this.mBatteryRemainingIcon.setIgnoreTunerUpdates(true);
        this.mBatteryIcon.setIgnoreTunerUpdates(true);
        this.mBatteryRemainingIcon.setOnClickListener(this);
        this.mBatteryIcon.setOnClickListener(this);
        this.mRingerModeTextView.setSelected(true);
        this.mNextAlarmTextView.setSelected(true);
        this.mAllIndicatorsEnabled = this.mPrivacyItemController.getAllIndicatorsAvailable();
        this.mMicCameraIndicatorsEnabled = this.mPrivacyItemController.getMicCameraAvailable();
        updateResources();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "lineagesecure:qs_show_auto_brightness", "lineagesecure:qs_show_brightness_slider", "system:show_qs_clock", "system:qs_show_battery_percent", "system:qs_show_battery_estimate", "system:status_bar_battery_style", "system:qs_battery_style", "system:qs_battery_location", "system:status_bar_custom_header");
    }

    public QuickQSPanel getHeaderQsPanel() {
        return this.mHeaderQsPanel;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<String> getIgnoredIconSlots() {
        ArrayList arrayList = new ArrayList();
        if (getChipEnabled()) {
            arrayList.add(((RelativeLayout) this).mContext.getResources().getString(android.R.string.permlab_foregroundServicePhoneCall));
            arrayList.add(((RelativeLayout) this).mContext.getResources().getString(android.R.string.permlab_invokeCarrierSetup));
            if (this.mAllIndicatorsEnabled) {
                arrayList.add(((RelativeLayout) this).mContext.getResources().getString(android.R.string.permlab_imagesWrite));
            }
        }
        return arrayList;
    }

    private void updateStatusText() {
        if (updateRingerStatus() || updateAlarmStatus()) {
            this.mStatusSeparator.setVisibility(((this.mNextAlarmTextView.getVisibility() == 0) && (this.mRingerModeTextView.getVisibility() == 0)) ? 0 : 8);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setChipVisibility(boolean z) {
        if (z && getChipEnabled()) {
            this.mPrivacyChip.setVisibility(0);
            if (this.mPrivacyChipLogged || !this.mListening) {
                return;
            }
            this.mPrivacyChipLogged = true;
            this.mUiEventLogger.log(PrivacyChipEvent.ONGOING_INDICATORS_CHIP_VIEW);
            return;
        }
        this.mPrivacyChip.setVisibility(8);
    }

    private boolean updateRingerStatus() {
        boolean z;
        boolean z2 = this.mRingerModeTextView.getVisibility() == 0;
        CharSequence text = this.mRingerModeTextView.getText();
        if (ZenModeConfig.isZenOverridingRinger(this.mZenController.getZen(), this.mZenController.getConsolidatedPolicy())) {
            z = false;
        } else {
            int i = this.mRingerMode;
            if (i == 1) {
                this.mRingerModeIcon.setImageResource(R.drawable.ic_volume_ringer_vibrate);
                this.mRingerModeTextView.setText(R.string.qs_status_phone_vibrate);
            } else {
                if (i == 0) {
                    this.mRingerModeIcon.setImageResource(R.drawable.ic_volume_ringer_mute);
                    this.mRingerModeTextView.setText(R.string.qs_status_phone_muted);
                }
                z = false;
            }
            z = true;
        }
        this.mRingerModeIcon.setVisibility(z ? 0 : 8);
        this.mRingerModeTextView.setVisibility(z ? 0 : 8);
        this.mRingerContainer.setVisibility(z ? 0 : 8);
        return (z2 == z && Objects.equals(text, this.mRingerModeTextView.getText())) ? false : true;
    }

    private boolean updateAlarmStatus() {
        boolean z;
        boolean z2 = this.mNextAlarmTextView.getVisibility() == 0;
        CharSequence text = this.mNextAlarmTextView.getText();
        AlarmManager.AlarmClockInfo alarmClockInfo = this.mNextAlarm;
        if (alarmClockInfo != null) {
            this.mNextAlarmTextView.setText(formatNextAlarm(alarmClockInfo));
            z = true;
        } else {
            z = false;
        }
        this.mNextAlarmIcon.setVisibility(z ? 0 : 8);
        this.mNextAlarmTextView.setVisibility(z ? 0 : 8);
        this.mNextAlarmContainer.setVisibility(z ? 0 : 8);
        return (z2 == z && Objects.equals(text, this.mNextAlarmTextView.getText())) ? false : true;
    }

    private void applyDarkness(int i, Rect rect, float f, int i2) {
        KeyEvent.Callback callbackFindViewById = findViewById(i);
        if (callbackFindViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) callbackFindViewById).onDarkChanged(rect, f, i2);
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        this.mLandscape = configuration.orientation == 2;
        updateResources();
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) throws Resources.NotFoundException {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateMinimumHeight() throws Resources.NotFoundException {
        int dimensionPixelSize = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_height);
        int dimensionPixelSize2 = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_header_panel_height);
        if (this.mIsQuickQsBrightnessEnabled) {
            dimensionPixelSize2 += ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.brightness_mirror_height) + ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_tile_margin_top);
        }
        if (this.mHeaderImageEnabled) {
            dimensionPixelSize2 += ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_header_image_offset);
        }
        setMinimumHeight(dimensionPixelSize + dimensionPixelSize2);
    }

    private void updateResources() throws Resources.NotFoundException {
        Resources resources = ((RelativeLayout) this).mContext.getResources();
        updateMinimumHeight();
        this.mRoundedCornerPadding = resources.getDimensionPixelSize(R.dimen.rounded_corner_content_padding);
        this.mStatusBarPaddingTop = resources.getDimensionPixelSize(R.dimen.status_bar_padding_top);
        this.mHeaderTextContainerView.getLayoutParams().height = resources.getDimensionPixelSize(R.dimen.qs_header_tooltip_height);
        View view = this.mHeaderTextContainerView;
        view.setLayoutParams(view.getLayoutParams());
        boolean z = false;
        int dimensionPixelSize = resources.getDimensionPixelSize(android.R.dimen.message_progress_dialog_end_padding) + (this.mHeaderImageEnabled ? resources.getDimensionPixelSize(R.dimen.qs_header_image_offset) : 0);
        this.mSystemIconsView.getLayoutParams().height = dimensionPixelSize;
        View view2 = this.mSystemIconsView;
        view2.setLayoutParams(view2.getLayoutParams());
        if (this.mIsQuickQsBrightnessEnabled) {
            if (this.mIsQsAutoBrightnessEnabled && resources.getBoolean(android.R.bool.config_allow_pin_storage_for_unattended_reboot)) {
                this.mQuickQsBrightness.findViewById(R.id.brightness_icon).setVisibility(0);
            } else {
                QSPanel qSPanel = this.mQsPanel;
                if (qSPanel != null && qSPanel.isHorizontalLayout()) {
                    this.mQuickQsBrightness.findViewById(R.id.brightness_icon).setVisibility(4);
                } else {
                    this.mQuickQsBrightness.findViewById(R.id.brightness_icon).setVisibility(8);
                }
            }
            this.mQuickQsBrightness.setVisibility(0);
        } else {
            QSPanel qSPanel2 = this.mQsPanel;
            if (qSPanel2 != null && qSPanel2.isHorizontalLayout()) {
                this.mQuickQsBrightness.setVisibility(4);
            } else {
                this.mQuickQsBrightness.setVisibility(8);
            }
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (this.mQsDisabled) {
            layoutParams.height = dimensionPixelSize;
        } else {
            layoutParams.height = -2;
        }
        setLayoutParams(layoutParams);
        updateStatusIconAlphaAnimator();
        updateHeaderTextContainerAlphaAnimator();
        updatePrivacyChipAlphaAnimator();
        if (this.mLandscape && !this.mHeaderImageEnabled) {
            z = true;
        }
        this.mClockView.useWallpaperTextColor(z);
    }

    private void updateStatusIconAlphaAnimator() {
        this.mStatusIconsAlphaAnimator = new TouchAnimator.Builder().addFloat(this.mQuickQsStatusIcons, "alpha", 1.0f, 0.0f, 0.0f).build();
    }

    private void updateHeaderTextContainerAlphaAnimator() {
        this.mHeaderTextContainerAlphaAnimator = new TouchAnimator.Builder().addFloat(this.mHeaderTextContainerView, "alpha", 0.0f, 0.0f, this.mExpandedHeaderAlpha).build();
    }

    private void updatePrivacyChipAlphaAnimator() {
        this.mPrivacyChipAlphaAnimator = new TouchAnimator.Builder().addFloat(this.mPrivacyChip, "alpha", 1.0f, 0.0f, 1.0f).build();
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        this.mExpanded = z;
        this.mHeaderQsPanel.setExpanded(z);
        updateEverything();
    }

    public void setExpansion(boolean z, float f, float f2) throws Resources.NotFoundException {
        float f3 = z ? 1.0f : f;
        TouchAnimator touchAnimator = this.mStatusIconsAlphaAnimator;
        if (touchAnimator != null) {
            touchAnimator.setPosition(f3);
        }
        if (z) {
            if (this.mBlurUtils.supportsBlursOnWindows()) {
                this.mBlurUtils.applyBlur(getViewRootImpl(), this.mBlurUtils.blurRadiusOfRatio(f));
            }
            this.mHeaderTextContainerView.setTranslationY(f2);
        } else {
            this.mHeaderTextContainerView.setTranslationY(0.0f);
        }
        TouchAnimator touchAnimator2 = this.mHeaderTextContainerAlphaAnimator;
        if (touchAnimator2 != null) {
            touchAnimator2.setPosition(f3);
            if (f3 > 0.0f) {
                this.mHeaderTextContainerView.setVisibility(0);
            } else {
                this.mHeaderTextContainerView.setVisibility(4);
            }
        }
        if (this.mPrivacyChipAlphaAnimator != null) {
            this.mPrivacyChip.setExpanded(((double) f) > 0.5d);
            this.mPrivacyChipAlphaAnimator.setPosition(f3);
        }
        if (this.mIsQuickQsBrightnessEnabled) {
            if (f3 > 0.0f) {
                this.mQuickQsBrightness.setVisibility(4);
            } else {
                this.mQuickQsBrightness.setVisibility(0);
            }
        }
        if (f < 1.0f && f > 0.99d && this.mHeaderQsPanel.switchTileLayout()) {
            updateResources();
        }
        this.mKeyguardExpansionFraction = f3;
    }

    public void disable(int i, int i2, boolean z) throws Resources.NotFoundException {
        boolean z2 = (i2 & 1) != 0;
        if (z2 == this.mQsDisabled) {
            return;
        }
        this.mQsDisabled = z2;
        this.mHeaderQsPanel.setDisabledByPolicy(z2);
        this.mHeaderTextContainerView.setVisibility(this.mQsDisabled ? 8 : 0);
        this.mQuickQsStatusIcons.setVisibility(this.mQsDisabled ? 8 : 0);
        this.mQuickQsBrightness.setVisibility(this.mQsDisabled ? 8 : 0);
        updateResources();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mRingerModeTracker.getRingerModeInternal().observe(this, new Observer() { // from class: com.android.systemui.qs.QuickStatusBarHeader$$ExternalSyntheticLambda1
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                this.f$0.lambda$onAttachedToWindow$0((Integer) obj);
            }
        });
        this.mStatusBarIconController.addIconGroup(this.mIconManager);
        requestApplyInsets();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onAttachedToWindow$0(Integer num) {
        this.mRingerMode = num.intValue();
        updateStatusText();
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        Pair<Integer, Integer> pairCornerCutoutMargins = StatusBarWindowView.cornerCutoutMargins(displayCutout, getDisplay());
        Pair<Integer, Integer> pairPaddingNeededForCutoutAndRoundedCorner = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(displayCutout, pairCornerCutoutMargins, -1);
        if (pairPaddingNeededForCutoutAndRoundedCorner == null) {
            this.mSystemIconsView.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.status_bar_padding_start), 0, getResources().getDimensionPixelSize(R.dimen.status_bar_padding_end), 0);
        } else {
            this.mSystemIconsView.setPadding(((Integer) pairPaddingNeededForCutoutAndRoundedCorner.first).intValue(), 0, ((Integer) pairPaddingNeededForCutoutAndRoundedCorner.second).intValue(), 0);
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mSpace.getLayoutParams();
        boolean z = pairCornerCutoutMargins != null && (((Integer) pairCornerCutoutMargins.first).intValue() == 0 || ((Integer) pairCornerCutoutMargins.second).intValue() == 0);
        if (displayCutout != null) {
            Rect boundingRectTop = displayCutout.getBoundingRectTop();
            if (boundingRectTop.isEmpty() || z) {
                this.mHasTopCutout = false;
                layoutParams.width = 0;
                this.mSpace.setVisibility(8);
            } else {
                this.mHasTopCutout = true;
                layoutParams.width = boundingRectTop.width();
                this.mSpace.setVisibility(0);
            }
        }
        this.mSpace.setLayoutParams(layoutParams);
        setChipVisibility(this.mPrivacyChip.getVisibility() == 0);
        this.mCutOutPaddingLeft = ((Integer) pairPaddingNeededForCutoutAndRoundedCorner.first).intValue();
        this.mCutOutPaddingRight = ((Integer) pairPaddingNeededForCutoutAndRoundedCorner.second).intValue();
        this.mWaterfallTopInset = displayCutout != null ? displayCutout.getWaterfallInsets().top : 0;
        updateClockPadding();
        return super.onApplyWindowInsets(windowInsets);
    }

    private void updateClockPadding() {
        int iMax;
        int iMax2;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        int i = layoutParams.leftMargin;
        int i2 = layoutParams.rightMargin;
        int i3 = this.mCutOutPaddingLeft;
        if (i3 > 0) {
            iMax = Math.max((Math.max(i3, this.mRoundedCornerPadding) - (isLayoutRtl() ? this.mContentMarginEnd : this.mContentMarginStart)) - i, 0);
        } else {
            iMax = 0;
        }
        int i4 = this.mCutOutPaddingRight;
        if (i4 > 0) {
            iMax2 = Math.max((Math.max(i4, this.mRoundedCornerPadding) - (isLayoutRtl() ? this.mContentMarginStart : this.mContentMarginEnd)) - i2, 0);
        } else {
            iMax2 = 0;
        }
        this.mSystemIconsView.setPadding(iMax, this.mWaterfallTopInset + this.mStatusBarPaddingTop, iMax2, 0);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() throws Resources.NotFoundException {
        setListening(false);
        this.mRingerModeTracker.getRingerModeInternal().removeObservers(this);
        this.mStatusBarIconController.removeIconGroup(this.mIconManager);
        super.onDetachedFromWindow();
    }

    public void setListening(boolean z) throws Resources.NotFoundException {
        if (z == this.mListening) {
            return;
        }
        this.mHeaderQsPanel.setListening(z);
        if (this.mHeaderQsPanel.switchTileLayout()) {
            updateResources();
        }
        this.mListening = z;
        if (z) {
            this.mZenController.addCallback(this);
            this.mAlarmController.addCallback(this);
            this.mBrightnessController.registerCallbacks();
            this.mLifecycle.setCurrentState(Lifecycle.State.RESUMED);
            this.mAllIndicatorsEnabled = this.mPrivacyItemController.getAllIndicatorsAvailable();
            this.mMicCameraIndicatorsEnabled = this.mPrivacyItemController.getMicCameraAvailable();
            this.mPrivacyItemController.addCallback(this.mPICCallback);
            return;
        }
        this.mZenController.removeCallback(this);
        this.mAlarmController.removeCallback(this);
        this.mBrightnessController.unregisterCallbacks();
        this.mLifecycle.setCurrentState(Lifecycle.State.CREATED);
        this.mPrivacyItemController.removeCallback(this.mPICCallback);
        this.mPrivacyChipLogged = false;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mClockView) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
            return;
        }
        View view2 = this.mNextAlarmContainer;
        if (view == view2 && view2.isVisibleToUser()) {
            if (this.mNextAlarm.getShowIntent() != null) {
                this.mActivityStarter.postStartActivityDismissingKeyguard(this.mNextAlarm.getShowIntent());
                return;
            } else {
                Log.d("QuickStatusBarHeader", "No PendingIntent for next alarm. Using default intent");
                this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
                return;
            }
        }
        OngoingPrivacyChip ongoingPrivacyChip = this.mPrivacyChip;
        if (view == ongoingPrivacyChip) {
            if (ongoingPrivacyChip.getBuilder().getAppsAndTypes().size() == 0) {
                return;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            this.mUiEventLogger.log(PrivacyChipEvent.ONGOING_INDICATORS_CHIP_CLICK);
            handler.post(new Runnable() { // from class: com.android.systemui.qs.QuickStatusBarHeader$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onClick$1();
                }
            });
            return;
        }
        View view3 = this.mRingerContainer;
        if (view == view3 && view3.isVisibleToUser()) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.SOUND_SETTINGS"), 0);
        } else if (view == this.mBatteryRemainingIcon) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.POWER_USAGE_SUMMARY"), 0);
        } else if (view == this.mBatteryIcon) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.POWER_USAGE_SUMMARY"), 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onClick$1() {
        this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.REVIEW_ONGOING_PERMISSION_USAGE"), 0);
        this.mHost.collapsePanels();
    }

    @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
        this.mNextAlarm = alarmClockInfo;
        updateStatusText();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenChanged(int i) {
        updateStatusText();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        updateStatusText();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateEverything$2() {
        setClickable(!this.mExpanded);
    }

    public void updateEverything() {
        post(new Runnable() { // from class: com.android.systemui.qs.QuickStatusBarHeader$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateEverything$2();
            }
        });
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        setupHost(qSPanel.getHost());
    }

    public void setupHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mHeaderQsPanel.setQSPanelAndHeader(this.mQsPanel, this);
        this.mHeaderQsPanel.setHost(qSTileHost, null);
        Rect rect = new Rect(0, 0, 0, 0);
        float colorIntensity = getColorIntensity(Utils.getColorAttrDefaultColor(getContext(), android.R.attr.colorForeground));
        this.mBatteryRemainingIcon.onDarkChanged(rect, colorIntensity, this.mDualToneHandler.getSingleColor(colorIntensity));
        this.mBatteryIcon.setColorsFromContext(this.mHost.getContext());
        this.mBatteryIcon.onDarkChanged(new Rect(), 0.0f, -1);
    }

    public void setCallback(QSDetail.Callback callback) {
        this.mHeaderQsPanel.setCallback(callback);
    }

    private String formatNextAlarm(AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo == null) {
            return "";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(((RelativeLayout) this).mContext, ActivityManager.getCurrentUser()) ? "EHm" : "Ehma"), alarmClockInfo.getTriggerTime()).toString();
    }

    @Override // androidx.lifecycle.LifecycleOwner
    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }

    public void setContentMargins(int i, int i2) {
        this.mContentMarginStart = i;
        this.mContentMarginEnd = i2;
        for (int i3 = 0; i3 < getChildCount(); i3++) {
            View childAt = getChildAt(i3);
            QuickQSPanel quickQSPanel = this.mHeaderQsPanel;
            if (childAt == quickQSPanel) {
                quickQSPanel.setContentMargins(i, i2);
            } else if (childAt != this.mQuickQsBrightness) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                marginLayoutParams.setMarginStart(i);
                marginLayoutParams.setMarginEnd(i2);
                childAt.setLayoutParams(marginLayoutParams);
            }
        }
        updateClockPadding();
    }

    public void setExpandedScrollAmount(int i) {
        float interpolation = 1.0f;
        if (this.mHeaderTextContainerView.getHeight() > 0) {
            interpolation = Interpolators.ALPHA_OUT.getInterpolation(MathUtils.map(0.0f, this.mHeaderTextContainerView.getHeight() / 2.0f, 1.0f, 0.0f, i));
        }
        this.mHeaderTextContainerView.setScrollY(i);
        if (interpolation != this.mExpandedHeaderAlpha) {
            this.mExpandedHeaderAlpha = interpolation;
            this.mHeaderTextContainerView.setAlpha(MathUtils.lerp(0.0f, interpolation, this.mKeyguardExpansionFraction));
            updateHeaderTextContainerAlphaAnimator();
        }
    }

    private boolean getChipEnabled() {
        return this.mMicCameraIndicatorsEnabled || this.mAllIndicatorsEnabled;
    }

    private void updateBatteryStyle() {
        int i = this.mQSBatteryStyle;
        if (i == -1) {
            i = this.mStatusBarBatteryStyle;
        }
        BatteryMeterView batteryMeterView = this.mBatteryRemainingIcon;
        batteryMeterView.mBatteryStyle = i;
        this.mBatteryIcon.mBatteryStyle = i;
        batteryMeterView.updateBatteryStyle();
        this.mBatteryRemainingIcon.updatePercentView();
        this.mBatteryRemainingIcon.updateVisibility();
        this.mBatteryIcon.updateBatteryStyle();
        this.mBatteryIcon.updatePercentView();
        this.mBatteryIcon.updateVisibility();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:4:0x0010  */
    @Override // com.android.systemui.tuner.TunerService.Tunable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void onTuningChanged(java.lang.String r7, java.lang.String r8) throws android.content.res.Resources.NotFoundException {
        /*
            Method dump skipped, instructions count: 354
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QuickStatusBarHeader.onTuningChanged(java.lang.String, java.lang.String):void");
    }
}
