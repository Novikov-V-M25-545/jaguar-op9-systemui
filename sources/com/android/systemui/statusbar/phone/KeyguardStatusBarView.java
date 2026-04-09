package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class KeyguardStatusBarView extends RelativeLayout implements UserInfoController.OnUserInfoChangedListener, ConfigurationController.ConfigurationListener, TunerService.Tunable {
    private BatteryMeterView mBatteryView;
    private TextView mCarrierLabel;
    private int mCutoutSideNudge;
    private View mCutoutSpace;
    private DisplayCutout mDisplayCutout;
    private final Rect mEmptyRect;
    private StatusBarIconController.TintedIconManager mIconManager;
    private boolean mImmerseMode;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mKeyguardUserSwitcherShowing;
    private int mLayoutState;
    private ImageView mMultiUserAvatar;
    private MultiUserSwitch mMultiUserSwitch;
    private ContentObserver mObserver;
    private Pair<Integer, Integer> mPadding;
    private int mRoundedCornerPadding;
    private int mShowCarrierLabel;
    private ViewGroup mStatusIconArea;
    private StatusIconContainer mStatusIconContainer;
    private int mSystemIconsBaseMargin;
    private View mSystemIconsContainer;
    private int mSystemIconsSwitcherHiddenExpandedMargin;
    private UserSwitcherController mUserSwitcherController;

    private int calculateMargin(int i, int i2) {
        if (i2 >= i) {
            return 0;
        }
        return i - i2;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mEmptyRect = new Rect(0, 0, 0, 0);
        this.mLayoutState = 0;
        this.mCutoutSideNudge = 0;
        this.mRoundedCornerPadding = 0;
        this.mPadding = new Pair<>(0, 0);
        this.mObserver = new ContentObserver(new Handler()) { // from class: com.android.systemui.statusbar.phone.KeyguardStatusBarView.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri) {
                KeyguardStatusBarView.this.showStatusBarCarrier();
                KeyguardStatusBarView.this.updateVisibilities();
            }
        };
        showStatusBarCarrier();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showStatusBarCarrier() {
        this.mShowCarrierLabel = Settings.System.getIntForUser(getContext().getContentResolver(), "status_bar_show_carrier", 0, -2);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsContainer = findViewById(R.id.system_icons_container);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) findViewById(R.id.multi_user_avatar);
        this.mCarrierLabel = (TextView) findViewById(R.id.keyguard_carrier_text);
        this.mBatteryView = (BatteryMeterView) this.mSystemIconsContainer.findViewById(R.id.battery);
        this.mCutoutSpace = findViewById(R.id.cutout_space_view);
        this.mStatusIconArea = (ViewGroup) findViewById(R.id.status_icon_area);
        this.mStatusIconContainer = (StatusIconContainer) findViewById(R.id.statusIcons);
        loadDimens();
        updateUserSwitcher();
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mMultiUserAvatar.getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.multi_user_avatar_keyguard_size);
        marginLayoutParams.height = dimensionPixelSize;
        marginLayoutParams.width = dimensionPixelSize;
        this.mMultiUserAvatar.setLayoutParams(marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        marginLayoutParams2.width = getResources().getDimensionPixelSize(R.dimen.multi_user_switch_width_keyguard);
        marginLayoutParams2.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.multi_user_switch_keyguard_margin));
        this.mMultiUserSwitch.setLayoutParams(marginLayoutParams2);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mSystemIconsContainer.getLayoutParams();
        marginLayoutParams3.setMarginStart(getResources().getDimensionPixelSize(R.dimen.system_icons_super_container_margin_start));
        this.mSystemIconsContainer.setLayoutParams(marginLayoutParams3);
        View view = this.mSystemIconsContainer;
        view.setPaddingRelative(view.getPaddingStart(), this.mSystemIconsContainer.getPaddingTop(), getResources().getDimensionPixelSize(R.dimen.system_icons_keyguard_padding_end), this.mSystemIconsContainer.getPaddingBottom());
        this.mCarrierLabel.setTextSize(0, getResources().getDimensionPixelSize(android.R.dimen.notification_icon_circle_start));
    }

    private void updateKeyguardStatusBarHeight() {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        if (this.mImmerseMode) {
            marginLayoutParams.height = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        } else {
            marginLayoutParams.height = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height_keyguard) + i;
        }
        setLayoutParams(marginLayoutParams);
    }

    private void loadDimens() {
        Resources resources = getResources();
        this.mSystemIconsSwitcherHiddenExpandedMargin = resources.getDimensionPixelSize(R.dimen.system_icons_switcher_hidden_expanded_margin);
        this.mSystemIconsBaseMargin = resources.getDimensionPixelSize(R.dimen.system_icons_super_container_avatarless_margin_end);
        this.mCutoutSideNudge = getResources().getDimensionPixelSize(R.dimen.display_cutout_margin_consumption);
        this.mRoundedCornerPadding = resources.getDimensionPixelSize(R.dimen.rounded_corner_content_padding);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateVisibilities() {
        if (this.mMultiUserSwitch.getParent() != this.mStatusIconArea && !this.mKeyguardUserSwitcherShowing) {
            if (this.mMultiUserSwitch.getParent() != null) {
                getOverlay().remove(this.mMultiUserSwitch);
            }
            this.mStatusIconArea.addView(this.mMultiUserSwitch, 0);
        } else {
            ViewParent parent = this.mMultiUserSwitch.getParent();
            ViewGroup viewGroup = this.mStatusIconArea;
            if (parent == viewGroup && this.mKeyguardUserSwitcherShowing) {
                viewGroup.removeView(this.mMultiUserSwitch);
            }
        }
        if (this.mKeyguardUserSwitcher == null) {
            if (this.mMultiUserSwitch.isMultiUserEnabled()) {
                this.mMultiUserSwitch.setVisibility(0);
            } else {
                this.mMultiUserSwitch.setVisibility(8);
            }
        }
        TextView textView = this.mCarrierLabel;
        if (textView != null) {
            int i = this.mShowCarrierLabel;
            if (i == 1 || i == 3) {
                textView.setVisibility(0);
            } else {
                textView.setVisibility(8);
            }
        }
    }

    private void updateSystemIconsLayoutParams() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams();
        int i = this.mMultiUserSwitch.getVisibility() == 8 ? this.mSystemIconsBaseMargin : 0;
        if (this.mKeyguardUserSwitcherShowing) {
            i = this.mSystemIconsSwitcherHiddenExpandedMargin;
        }
        int iCalculateMargin = calculateMargin(i, ((Integer) this.mPadding.second).intValue());
        if (iCalculateMargin != layoutParams.getMarginEnd()) {
            layoutParams.setMarginEnd(iCalculateMargin);
            this.mSystemIconsContainer.setLayoutParams(layoutParams);
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mLayoutState = 0;
        if (updateLayoutConsideringCutout()) {
            requestLayout();
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private boolean updateLayoutConsideringCutout() {
        this.mDisplayCutout = getRootWindowInsets().getDisplayCutout();
        updateKeyguardStatusBarHeight();
        Pair<Integer, Integer> pairCornerCutoutMargins = StatusBarWindowView.cornerCutoutMargins(this.mDisplayCutout, getDisplay());
        updatePadding(pairCornerCutoutMargins);
        updateCarrierLabelParams();
        if (this.mDisplayCutout == null || pairCornerCutoutMargins != null) {
            return updateLayoutParamsNoCutout();
        }
        return updateLayoutParamsForCutout();
    }

    private void updatePadding(Pair<Integer, Integer> pair) {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        Pair<Integer, Integer> pairPaddingNeededForCutoutAndRoundedCorner = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(this.mDisplayCutout, pair, this.mRoundedCornerPadding);
        this.mPadding = pairPaddingNeededForCutoutAndRoundedCorner;
        setPadding(((Integer) pairPaddingNeededForCutoutAndRoundedCorner.first).intValue(), i, ((Integer) this.mPadding.second).intValue(), 0);
    }

    private void updateCarrierLabelParams() {
        int iCalculateMargin = calculateMargin(getResources().getDimensionPixelSize(R.dimen.keyguard_carrier_text_margin), ((Integer) this.mPadding.first).intValue());
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mCarrierLabel.getLayoutParams();
        marginLayoutParams.setMarginStart(iCalculateMargin);
        this.mCarrierLabel.setLayoutParams(marginLayoutParams);
    }

    private boolean updateLayoutParamsNoCutout() {
        if (this.mLayoutState == 2) {
            return false;
        }
        this.mLayoutState = 2;
        View view = this.mCutoutSpace;
        if (view != null) {
            view.setVisibility(8);
        }
        ((RelativeLayout.LayoutParams) this.mCarrierLabel.getLayoutParams()).addRule(16, R.id.status_icon_area);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mStatusIconArea.getLayoutParams();
        layoutParams.removeRule(1);
        layoutParams.width = -2;
        ((LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams()).setMarginStart(getResources().getDimensionPixelSize(R.dimen.system_icons_super_container_margin_start));
        return true;
    }

    private boolean updateLayoutParamsForCutout() {
        if (this.mLayoutState == 1) {
            return false;
        }
        this.mLayoutState = 1;
        if (this.mCutoutSpace == null) {
            updateLayoutParamsNoCutout();
        }
        Rect rect = new Rect();
        ScreenDecorations.DisplayCutoutView.boundsFromDirection(this.mDisplayCutout, 48, rect);
        this.mCutoutSpace.setVisibility(0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mCutoutSpace.getLayoutParams();
        int i = rect.left;
        int i2 = this.mCutoutSideNudge;
        rect.left = i + i2;
        rect.right -= i2;
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        layoutParams.addRule(13);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mCarrierLabel.getLayoutParams();
        int i3 = R.id.cutout_space_view;
        layoutParams2.addRule(16, i3);
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mStatusIconArea.getLayoutParams();
        layoutParams3.addRule(1, i3);
        layoutParams3.width = -1;
        ((LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams()).setMarginStart(0);
        return true;
    }

    private void updateUserSwitcher() {
        boolean z = this.mKeyguardUserSwitcher != null;
        this.mMultiUserSwitch.setClickable(z);
        this.mMultiUserSwitch.setFocusable(z);
        this.mMultiUserSwitch.setKeyguardMode(z);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        UserInfoController userInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
        userInfoController.addCallback(this);
        UserSwitcherController userSwitcherController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
        this.mUserSwitcherController = userSwitcherController;
        this.mMultiUserSwitch.setUserSwitcherController(userSwitcherController);
        userInfoController.reloadUserInfo();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mIconManager = new StatusBarIconController.TintedIconManager((ViewGroup) findViewById(R.id.statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier"), false, this.mObserver);
        onThemeChanged();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:display_cutout_mode");
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((UserInfoController) Dependency.get(UserInfoController.class)).removeCallback(this);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:display_cutout_mode")) {
            this.mImmerseMode = TunerService.parseInteger(str2, 0) == 1;
            updateKeyguardStatusBarHeight();
        }
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mMultiUserSwitch.setQsPanel(qSPanel);
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        this.mMultiUserSwitch.setKeyguardUserSwitcher(keyguardUserSwitcher);
        updateUserSwitcher();
    }

    public void setKeyguardUserSwitcherShowing(boolean z, boolean z2) {
        this.mKeyguardUserSwitcherShowing = z;
        if (z2) {
            animateNextLayoutChange();
        }
        updateVisibilities();
        updateLayoutConsideringCutout();
        updateSystemIconsLayoutParams();
    }

    private void animateNextLayoutChange() {
        getViewTreeObserver().addOnPreDrawListener(new AnonymousClass2(this.mMultiUserSwitch.getParent() == this.mStatusIconArea, this.mSystemIconsContainer.getLeft()));
    }

    /* renamed from: com.android.systemui.statusbar.phone.KeyguardStatusBarView$2, reason: invalid class name */
    class AnonymousClass2 implements ViewTreeObserver.OnPreDrawListener {
        final /* synthetic */ int val$systemIconsCurrentX;
        final /* synthetic */ boolean val$userSwitcherVisible;

        AnonymousClass2(boolean z, int i) {
            this.val$userSwitcherVisible = z;
            this.val$systemIconsCurrentX = i;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            KeyguardStatusBarView.this.getViewTreeObserver().removeOnPreDrawListener(this);
            boolean z = this.val$userSwitcherVisible && KeyguardStatusBarView.this.mMultiUserSwitch.getParent() != KeyguardStatusBarView.this.mStatusIconArea;
            KeyguardStatusBarView.this.mSystemIconsContainer.setX(this.val$systemIconsCurrentX);
            KeyguardStatusBarView.this.mSystemIconsContainer.animate().translationX(0.0f).setDuration(400L).setStartDelay(z ? 300L : 0L).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
            if (z) {
                KeyguardStatusBarView.this.getOverlay().add(KeyguardStatusBarView.this.mMultiUserSwitch);
                KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(0.0f).setDuration(300L).setStartDelay(0L).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardStatusBarView$2$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$onPreDraw$0();
                    }
                }).start();
            } else {
                KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(0.0f);
                KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(1.0f).setDuration(300L).setStartDelay(200L).setInterpolator(Interpolators.ALPHA_IN);
            }
            return true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onPreDraw$0() {
            KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(1.0f);
            KeyguardStatusBarView.this.getOverlay().remove(KeyguardStatusBarView.this.mMultiUserSwitch);
        }
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        if (i != 0) {
            this.mSystemIconsContainer.animate().cancel();
            this.mSystemIconsContainer.setTranslationX(0.0f);
            this.mMultiUserSwitch.animate().cancel();
            this.mMultiUserSwitch.setAlpha(1.0f);
            return;
        }
        updateVisibilities();
        updateSystemIconsLayoutParams();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        this.mBatteryView.setColorsFromContext(((RelativeLayout) this).mContext);
        updateIconsAndTextColors();
        ((UserInfoControllerImpl) Dependency.get(UserInfoController.class)).onDensityOrFontScaleChanged();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        loadDimens();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        this.mCarrierLabel.setTextAppearance(Utils.getThemeAttr(((RelativeLayout) this).mContext, android.R.attr.textAppearanceSmall));
        onThemeChanged();
        this.mBatteryView.updatePercentView();
    }

    private void updateIconsAndTextColors() {
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(((RelativeLayout) this).mContext, R.attr.wallpaperTextColor);
        int colorStateListDefaultColor = Utils.getColorStateListDefaultColor(((RelativeLayout) this).mContext, ((double) Color.luminance(colorAttrDefaultColor)) < 0.5d ? R.color.dark_mode_icon_color_single_tone : R.color.light_mode_icon_color_single_tone);
        float f = colorAttrDefaultColor == -1 ? 0.0f : 1.0f;
        this.mCarrierLabel.setTextColor(colorStateListDefaultColor);
        StatusBarIconController.TintedIconManager tintedIconManager = this.mIconManager;
        if (tintedIconManager != null) {
            tintedIconManager.setTint(colorStateListDefaultColor);
        }
        applyDarkness(R.id.battery, this.mEmptyRect, f, colorStateListDefaultColor);
        applyDarkness(R.id.clock, this.mEmptyRect, f, colorStateListDefaultColor);
    }

    private void applyDarkness(int i, Rect rect, float f, int i2) {
        KeyEvent.Callback callbackFindViewById = findViewById(i);
        if (callbackFindViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) callbackFindViewById).onDarkChanged(rect, f, i2);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardStatusBarView:");
        printWriter.println("  mKeyguardUserSwitcherShowing: " + this.mKeyguardUserSwitcherShowing);
        printWriter.println("  mLayoutState: " + this.mLayoutState);
        BatteryMeterView batteryMeterView = this.mBatteryView;
        if (batteryMeterView != null) {
            batteryMeterView.dump(fileDescriptor, printWriter, strArr);
        }
    }
}
