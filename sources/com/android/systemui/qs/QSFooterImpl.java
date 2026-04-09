package com.android.systemui.qs;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.Utils;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.tuner.TunerService;

/* loaded from: classes.dex */
public class QSFooterImpl extends FrameLayout implements QSFooter, View.OnClickListener, UserInfoController.OnUserInfoChangedListener {
    private View mActionsContainer;
    private final ActivityStarter mActivityStarter;
    private TextView mBuildText;
    private final DeviceProvisionedController mDeviceProvisionedController;
    protected View mEdit;
    protected View mEditContainer;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpanded;
    private float mExpansionAmount;
    protected TouchAnimator mFooterAnimator;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private PageIndicator mPageIndicator;
    private boolean mQsDisabled;
    private QSPanel mQsPanel;
    private QuickQSPanel mQuickQsPanel;
    private View mRunningServicesButton;
    private SettingsButton mSettingsButton;
    private TouchAnimator mSettingsCogAnimator;
    protected View mSettingsContainer;
    private boolean mShouldShowBuildText;
    private final UserInfoController mUserInfoController;

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$onClick$4() {
    }

    public QSFooterImpl(Context context, AttributeSet attributeSet, ActivityStarter activityStarter, UserInfoController userInfoController, DeviceProvisionedController deviceProvisionedController) {
        super(context, attributeSet);
        this.mActivityStarter = activityStarter;
        this.mUserInfoController = userInfoController;
        this.mDeviceProvisionedController = deviceProvisionedController;
    }

    public QSFooterImpl(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, (ActivityStarter) Dependency.get(ActivityStarter.class), (UserInfoController) Dependency.get(UserInfoController.class), (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class));
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        View viewFindViewById = findViewById(R.id.edit);
        this.mEdit = viewFindViewById;
        viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onFinishInflate$1(view);
            }
        });
        this.mPageIndicator = (PageIndicator) findViewById(com.android.systemui.R.id.footer_page_indicator);
        this.mSettingsButton = (SettingsButton) findViewById(com.android.systemui.R.id.settings_button);
        this.mSettingsContainer = findViewById(com.android.systemui.R.id.settings_button_container);
        this.mSettingsButton.setOnClickListener(this);
        View viewFindViewById2 = findViewById(com.android.systemui.R.id.running_services_button);
        this.mRunningServicesButton = viewFindViewById2;
        viewFindViewById2.setOnClickListener(this);
        MultiUserSwitch multiUserSwitch = (MultiUserSwitch) findViewById(com.android.systemui.R.id.multi_user_switch);
        this.mMultiUserSwitch = multiUserSwitch;
        this.mMultiUserAvatar = (ImageView) multiUserSwitch.findViewById(com.android.systemui.R.id.multi_user_avatar);
        this.mActionsContainer = findViewById(com.android.systemui.R.id.qs_footer_actions_container);
        this.mEditContainer = findViewById(com.android.systemui.R.id.qs_footer_actions_edit_container);
        this.mBuildText = (TextView) findViewById(com.android.systemui.R.id.build);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mRunningServicesButton.getBackground()).setForceSoftware(true);
        updateResources();
        addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda1
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) throws Resources.NotFoundException {
                this.f$0.lambda$onFinishInflate$2(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        setImportantForAccessibility(1);
        updateEverything();
        setBuildText();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$1(final View view) {
        this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onFinishInflate$0(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$0(View view) {
        this.mQsPanel.showEdit(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$2(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) throws Resources.NotFoundException {
        updateAnimator(i3 - i);
    }

    private void setBuildText() {
        TextView textView = this.mBuildText;
        if (textView == null) {
            return;
        }
        this.mShouldShowBuildText = false;
        textView.setSelected(false);
    }

    private void updateAnimator(int i) throws Resources.NotFoundException {
        QuickQSPanel quickQSPanel = this.mQuickQsPanel;
        int numQuickTiles = quickQSPanel != null ? quickQSPanel.getNumQuickTiles() : QuickQSPanel.getDefaultMaxTiles();
        int dimensionPixelSize = (i - ((((FrameLayout) this).mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.qs_quick_tile_size) - ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.qs_quick_tile_padding)) * numQuickTiles)) / (numQuickTiles - 1);
        int dimensionPixelOffset = ((FrameLayout) this).mContext.getResources().getDimensionPixelOffset(com.android.systemui.R.dimen.default_gear_space);
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        View view = this.mSettingsContainer;
        float[] fArr = new float[2];
        int i2 = dimensionPixelSize - dimensionPixelOffset;
        if (!isLayoutRtl()) {
            i2 = -i2;
        }
        fArr[0] = i2;
        fArr[1] = 0.0f;
        this.mSettingsCogAnimator = builder.addFloat(view, "translationX", fArr).addFloat(this.mSettingsButton, "rotation", -120.0f, 0.0f).build();
        setExpansion(this.mExpansionAmount);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateResources() {
        updateFooterAnimator();
    }

    private void updateFooterAnimator() {
        this.mFooterAnimator = createFooterAnimator();
    }

    private TouchAnimator createFooterAnimator() {
        return new TouchAnimator.Builder().addFloat(this.mActionsContainer, "alpha", 0.0f, 1.0f).addFloat(this.mEditContainer, "alpha", 0.0f, 1.0f).addFloat(this.mPageIndicator, "alpha", 0.0f, 1.0f).setStartDelay(0.9f).build();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setKeyguardShowing(boolean z) {
        setExpansion(this.mExpansionAmount);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpanded(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        this.mExpanded = z;
        updateEverything();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpansion(float f) {
        this.mExpansionAmount = f;
        TouchAnimator touchAnimator = this.mSettingsCogAnimator;
        if (touchAnimator != null) {
            touchAnimator.setPosition(f);
        }
        TouchAnimator touchAnimator2 = this.mFooterAnimator;
        if (touchAnimator2 != null) {
            touchAnimator2.setPosition(f);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        setListening(false);
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setListening(boolean z) {
        if (z == this.mListening) {
            return;
        }
        this.mListening = z;
        updateListeners();
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        View.OnClickListener onClickListener;
        if (i == 262144 && (onClickListener = this.mExpandClickListener) != null) {
            onClickListener.onClick(null);
            return true;
        }
        return super.performAccessibilityAction(i, bundle);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void disable(int i, int i2, boolean z) {
        boolean z2 = (i2 & 1) != 0;
        if (z2 == this.mQsDisabled) {
            return;
        }
        this.mQsDisabled = z2;
        updateEverything();
    }

    public void updateEverything() {
        post(new Runnable() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateEverything$3();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateEverything$3() {
        updateVisibilities();
        updateClickabilities();
        setClickable(false);
    }

    private void updateClickabilities() {
        MultiUserSwitch multiUserSwitch = this.mMultiUserSwitch;
        multiUserSwitch.setClickable(multiUserSwitch.getVisibility() == 0);
        View view = this.mEdit;
        view.setClickable(view.getVisibility() == 0);
        SettingsButton settingsButton = this.mSettingsButton;
        settingsButton.setClickable(settingsButton.getVisibility() == 0);
    }

    private void updateVisibilities() {
        int i = 8;
        this.mSettingsContainer.setVisibility((!isSettingsEnabled() || this.mQsDisabled) ? 8 : 0);
        int i2 = 4;
        this.mSettingsContainer.findViewById(com.android.systemui.R.id.tuner_icon).setVisibility(TunerService.isTunerEnabled(((FrameLayout) this).mContext) ? 0 : 4);
        boolean zIsDeviceInDemoMode = UserManager.isDeviceInDemoMode(((FrameLayout) this).mContext);
        this.mMultiUserSwitch.setVisibility(isUserEnabled() ? showUserSwitcher() ? 0 : 4 : 8);
        this.mEdit.setVisibility(isEditEnabled() ? (zIsDeviceInDemoMode || !this.mExpanded) ? 4 : 0 : 8);
        this.mEditContainer.setVisibility((zIsDeviceInDemoMode || !this.mExpanded) ? 4 : 0);
        this.mSettingsButton.setVisibility(isSettingsEnabled() ? (zIsDeviceInDemoMode || !this.mExpanded) ? 4 : 0 : 8);
        View view = this.mRunningServicesButton;
        if (!isServicesEnabled()) {
            i2 = 8;
        } else if (!zIsDeviceInDemoMode && this.mExpanded) {
            i2 = 0;
        }
        view.setVisibility(i2);
        TextView textView = this.mBuildText;
        if (this.mExpanded && this.mShouldShowBuildText) {
            i = 0;
        }
        textView.setVisibility(i);
    }

    private boolean showUserSwitcher() {
        return this.mExpanded && this.mMultiUserSwitch.isMultiUserEnabled();
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mUserInfoController.addCallback(this);
        } else {
            this.mUserInfoController.removeCallback(this);
        }
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        if (qSPanel != null) {
            this.mMultiUserSwitch.setQsPanel(qSPanel);
            this.mQsPanel.setFooterPageIndicator(this.mPageIndicator);
        }
    }

    public boolean isSettingsEnabled() {
        return Settings.System.getInt(((FrameLayout) this).mContext.getContentResolver(), "qs_footer_show_settings", 1) == 1;
    }

    public boolean isServicesEnabled() {
        return Settings.System.getInt(((FrameLayout) this).mContext.getContentResolver(), "qs_footer_show_services", 0) == 1;
    }

    public boolean isEditEnabled() {
        return Settings.System.getInt(((FrameLayout) this).mContext.getContentResolver(), "qs_footer_show_edit", 1) == 1;
    }

    public boolean isUserEnabled() {
        return Settings.System.getInt(((FrameLayout) this).mContext.getContentResolver(), "qs_footer_show_user", 1) == 1;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        boolean z = this.mExpanded;
        if (z) {
            if (view == this.mSettingsButton) {
                if (!this.mDeviceProvisionedController.isCurrentUserSetup()) {
                    this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda6
                        @Override // java.lang.Runnable
                        public final void run() {
                            QSFooterImpl.lambda$onClick$4();
                        }
                    });
                    return;
                }
                MetricsLogger.action(((FrameLayout) this).mContext, this.mExpanded ? 406 : 490);
                if (this.mSettingsButton.isTunerClick()) {
                    this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onClick$6();
                        }
                    });
                    return;
                } else {
                    lambda$onClick$5();
                    return;
                }
            }
            if (view == this.mRunningServicesButton) {
                MetricsLogger.action(((FrameLayout) this).mContext, z ? 406 : 490);
                startRunningServicesActivity();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onClick$6() {
        if (TunerService.isTunerEnabled(((FrameLayout) this).mContext)) {
            TunerService.showResetRequest(((FrameLayout) this).mContext, new Runnable() { // from class: com.android.systemui.qs.QSFooterImpl$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onClick$5();
                }
            });
        } else {
            Toast.makeText(getContext(), com.android.systemui.R.string.tuner_toast, 1).show();
            TunerService.setTunerEnabled(((FrameLayout) this).mContext, true);
        }
        lambda$onClick$5();
    }

    private void startRunningServicesActivity() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$DevRunningServicesActivity");
        this.mActivityStarter.startActivity(intent, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: startSettingsActivity, reason: merged with bridge method [inline-methods] */
    public void lambda$onClick$5() {
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        if (drawable != null && UserManager.get(((FrameLayout) this).mContext).isGuestUser(KeyguardUpdateMonitor.getCurrentUser()) && !(drawable instanceof UserIconDrawable)) {
            drawable = drawable.getConstantState().newDrawable(((FrameLayout) this).mContext.getResources()).mutate();
            drawable.setColorFilter(Utils.getColorAttrDefaultColor(((FrameLayout) this).mContext, R.attr.colorForeground), PorterDuff.Mode.SRC_IN);
        }
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }
}
