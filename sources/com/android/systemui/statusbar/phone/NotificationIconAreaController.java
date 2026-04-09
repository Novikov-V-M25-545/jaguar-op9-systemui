package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.util.ContrastColorUtil;
import com.android.settingslib.Utils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.Objects;
import java.util.function.Function;

/* loaded from: classes.dex */
public class NotificationIconAreaController implements DarkIconDispatcher.DarkReceiver, StatusBarStateController.StateListener, NotificationWakeUpCoordinator.WakeUpListener {
    private boolean mAnimationsEnabled;
    private int mAodIconAppearTranslation;
    private int mAodIconTint;
    private NotificationIconContainer mAodIcons;
    private boolean mAodIconsVisible;
    private final BubbleController mBubbleController;
    private final KeyguardBypassController mBypassController;
    private NotificationIconContainer mCenteredIcon;
    protected View mCenteredIconArea;
    private StatusBarIconView mCenteredIconView;
    private Context mContext;
    private final ContrastColorUtil mContrastColorUtil;
    private final DozeParameters mDozeParameters;
    private int mIconHPadding;
    private int mIconSize;
    private final NotificationMediaManager mMediaManager;
    protected View mNotificationIconArea;
    private NotificationIconContainer mNotificationIcons;
    private ViewGroup mNotificationScrollLayout;
    final NotificationListener.NotificationSettingsListener mSettingsListener;
    private NotificationIconContainer mShelfIcons;
    private StatusBar mStatusBar;
    private final StatusBarStateController mStatusBarStateController;
    private final NotificationWakeUpCoordinator mWakeUpCoordinator;
    private final Runnable mUpdateStatusBarIcons = new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.updateStatusBarIcons();
        }
    };
    private int mIconTint = -1;
    private int mCenteredIconTint = -1;
    private final Rect mTintArea = new Rect();
    private boolean mShowLowPriority = true;

    public NotificationIconAreaController(Context context, StatusBar statusBar, StatusBarStateController statusBarStateController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, NotificationMediaManager notificationMediaManager, NotificationListener notificationListener, DozeParameters dozeParameters, BubbleController bubbleController) {
        NotificationListener.NotificationSettingsListener notificationSettingsListener = new NotificationListener.NotificationSettingsListener() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController.1
            @Override // com.android.systemui.statusbar.NotificationListener.NotificationSettingsListener
            public void onStatusBarIconsBehaviorChanged(boolean z) {
                NotificationIconAreaController.this.mShowLowPriority = !z;
                if (NotificationIconAreaController.this.mNotificationScrollLayout != null) {
                    NotificationIconAreaController.this.updateStatusBarIcons();
                }
            }
        };
        this.mSettingsListener = notificationSettingsListener;
        this.mStatusBar = statusBar;
        this.mContrastColorUtil = ContrastColorUtil.getInstance(context);
        this.mContext = context;
        this.mStatusBarStateController = statusBarStateController;
        statusBarStateController.addCallback(this);
        this.mMediaManager = notificationMediaManager;
        this.mDozeParameters = dozeParameters;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        notificationWakeUpCoordinator.addListener(this);
        this.mBypassController = keyguardBypassController;
        this.mBubbleController = bubbleController;
        notificationListener.addNotificationSettingsListener(notificationSettingsListener);
        initializeNotificationAreaViews(context);
        reloadAodColor();
    }

    protected View inflateIconArea(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.notification_icon_area, (ViewGroup) null);
    }

    protected void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(context);
        View viewInflateIconArea = inflateIconArea(layoutInflaterFrom);
        this.mNotificationIconArea = viewInflateIconArea;
        this.mNotificationIcons = (NotificationIconContainer) viewInflateIconArea.findViewById(R.id.notificationIcons);
        this.mNotificationScrollLayout = this.mStatusBar.getNotificationScrollLayout();
        View viewInflate = layoutInflaterFrom.inflate(R.layout.center_icon_area, (ViewGroup) null);
        this.mCenteredIconArea = viewInflate;
        this.mCenteredIcon = (NotificationIconContainer) viewInflate.findViewById(R.id.centeredIcon);
        initAodIcons();
    }

    public void initAodIcons() {
        NotificationIconContainer notificationIconContainer = this.mAodIcons;
        boolean z = notificationIconContainer != null;
        if (z) {
            notificationIconContainer.setAnimationsEnabled(false);
            this.mAodIcons.removeAllViews();
        }
        NotificationIconContainer notificationIconContainer2 = (NotificationIconContainer) this.mStatusBar.getNotificationShadeWindowView().findViewById(R.id.clock_notification_icon_container);
        this.mAodIcons = notificationIconContainer2;
        notificationIconContainer2.setOnLockScreen(true);
        updateAodIconsVisibility(false);
        updateAnimations();
        if (z) {
            updateAodNotificationIcons();
        }
    }

    public void setupShelf(NotificationShelf notificationShelf) {
        this.mShelfIcons = notificationShelf.getShelfIcons();
        notificationShelf.setCollapsedIcons(this.mNotificationIcons);
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        FrameLayout.LayoutParams layoutParamsGenerateIconLayoutParams = generateIconLayoutParams();
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            this.mNotificationIcons.getChildAt(i).setLayoutParams(layoutParamsGenerateIconLayoutParams);
        }
        for (int i2 = 0; i2 < this.mShelfIcons.getChildCount(); i2++) {
            this.mShelfIcons.getChildAt(i2).setLayoutParams(layoutParamsGenerateIconLayoutParams);
        }
        for (int i3 = 0; i3 < this.mCenteredIcon.getChildCount(); i3++) {
            this.mCenteredIcon.getChildAt(i3).setLayoutParams(layoutParamsGenerateIconLayoutParams);
        }
        for (int i4 = 0; i4 < this.mAodIcons.getChildCount(); i4++) {
            this.mAodIcons.getChildAt(i4).setLayoutParams(layoutParamsGenerateIconLayoutParams);
        }
    }

    private FrameLayout.LayoutParams generateIconLayoutParams() {
        return new FrameLayout.LayoutParams(this.mIconSize + (this.mIconHPadding * 2), getHeight());
    }

    private void reloadDimens(Context context) {
        Resources resources = context.getResources();
        this.mIconSize = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_width_low_ram);
        this.mIconHPadding = resources.getDimensionPixelSize(R.dimen.status_bar_icon_padding);
        this.mAodIconAppearTranslation = resources.getDimensionPixelSize(R.dimen.shelf_appear_translation);
    }

    public View getNotificationInnerAreaView() {
        return this.mNotificationIconArea;
    }

    public View getCenteredNotificationAreaView() {
        return this.mCenteredIconArea;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        if (rect == null) {
            this.mTintArea.setEmpty();
        } else {
            this.mTintArea.set(rect);
        }
        View view = this.mNotificationIconArea;
        if (view == null || DarkIconDispatcher.isInArea(rect, view)) {
            this.mIconTint = i;
        }
        View view2 = this.mCenteredIconArea;
        if (view2 == null || DarkIconDispatcher.isInArea(rect, view2)) {
            this.mCenteredIconTint = i;
        }
        applyNotificationIconsTint();
    }

    protected int getHeight() {
        return this.mStatusBar.getStatusBarHeight();
    }

    protected boolean shouldShowNotificationIcon(NotificationEntry notificationEntry, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8) {
        boolean z9 = (this.mCenteredIconView == null || notificationEntry.getIcons().getCenteredIcon() == null || !Objects.equals(notificationEntry.getIcons().getCenteredIcon(), this.mCenteredIconView)) ? false : true;
        if (z8) {
            return z9;
        }
        if (z6 && z9 && !notificationEntry.isRowHeadsUp()) {
            return false;
        }
        if (notificationEntry.getRanking().isAmbient() && !z) {
            return false;
        }
        if (z5 && notificationEntry.getKey().equals(this.mMediaManager.getMediaNotificationKey())) {
            return false;
        }
        if ((!z2 && notificationEntry.getImportance() < 3) || !notificationEntry.isTopLevelChild() || notificationEntry.getRow().getVisibility() == 8) {
            return false;
        }
        if (notificationEntry.isRowDismissed() && z3) {
            return false;
        }
        if (z4 && notificationEntry.isLastMessageFromReply()) {
            return false;
        }
        if (z || !notificationEntry.shouldSuppressStatusBar()) {
            return ((z7 && notificationEntry.showingPulsing() && (!this.mWakeUpCoordinator.getNotificationsFullyHidden() || !notificationEntry.isPulseSuppressed())) || this.mBubbleController.isBubbleExpanded(notificationEntry)) ? false : true;
        }
        return false;
    }

    public void updateNotificationIcons() {
        updateStatusBarIcons();
        updateShelfIcons();
        updateCenterIcon();
        updateAodNotificationIcons();
        applyNotificationIconsTint();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ StatusBarIconView lambda$updateShelfIcons$0(NotificationEntry notificationEntry) {
        return notificationEntry.getIcons().getShelfIcon();
    }

    private void updateShelfIcons() {
        updateIconsForLayout(new Function() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda5
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NotificationIconAreaController.lambda$updateShelfIcons$0((NotificationEntry) obj);
            }
        }, this.mShelfIcons, true, true, false, false, false, false, false, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ StatusBarIconView lambda$updateStatusBarIcons$1(NotificationEntry notificationEntry) {
        return notificationEntry.getIcons().getStatusBarIcon();
    }

    public void updateStatusBarIcons() {
        updateIconsForLayout(new Function() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda6
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NotificationIconAreaController.lambda$updateStatusBarIcons$1((NotificationEntry) obj);
            }
        }, this.mNotificationIcons, false, this.mShowLowPriority, true, true, false, true, false, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ StatusBarIconView lambda$updateCenterIcon$2(NotificationEntry notificationEntry) {
        return notificationEntry.getIcons().getCenteredIcon();
    }

    private void updateCenterIcon() {
        updateIconsForLayout(new Function() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda7
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NotificationIconAreaController.lambda$updateCenterIcon$2((NotificationEntry) obj);
            }
        }, this.mCenteredIcon, false, true, false, false, false, false, false, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ StatusBarIconView lambda$updateAodNotificationIcons$3(NotificationEntry notificationEntry) {
        return notificationEntry.getIcons().getAodIcon();
    }

    public void updateAodNotificationIcons() {
        updateIconsForLayout(new Function() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda4
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NotificationIconAreaController.lambda$updateAodNotificationIcons$3((NotificationEntry) obj);
            }
        }, this.mAodIcons, false, true, true, true, true, true, this.mBypassController.getBypassEnabled(), false);
    }

    boolean shouldShouldLowPriorityIcons() {
        return this.mShowLowPriority;
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x005f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void updateIconsForLayout(java.util.function.Function<com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.statusbar.StatusBarIconView> r17, com.android.systemui.statusbar.phone.NotificationIconContainer r18, boolean r19, boolean r20, boolean r21, boolean r22, boolean r23, boolean r24, boolean r25, boolean r26) {
        /*
            Method dump skipped, instructions count: 370
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationIconAreaController.updateIconsForLayout(java.util.function.Function, com.android.systemui.statusbar.phone.NotificationIconContainer, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$updateIconsForLayout$4(StatusBarIconView statusBarIconView, StatusBarIconView statusBarIconView2) {
        return statusBarIconView2.equalIcons(statusBarIconView2.getSourceIcon(), statusBarIconView.getSourceIcon());
    }

    private void applyNotificationIconsTint() {
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            final StatusBarIconView statusBarIconView = (StatusBarIconView) this.mNotificationIcons.getChildAt(i);
            if (statusBarIconView.getWidth() != 0) {
                updateTintForIcon(statusBarIconView, this.mIconTint);
            } else {
                statusBarIconView.executeOnLayout(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$applyNotificationIconsTint$5(statusBarIconView);
                    }
                });
            }
        }
        for (int i2 = 0; i2 < this.mCenteredIcon.getChildCount(); i2++) {
            final StatusBarIconView statusBarIconView2 = (StatusBarIconView) this.mCenteredIcon.getChildAt(i2);
            if (statusBarIconView2.getWidth() != 0) {
                updateTintForIcon(statusBarIconView2, this.mCenteredIconTint);
            } else {
                statusBarIconView2.executeOnLayout(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$applyNotificationIconsTint$6(statusBarIconView2);
                    }
                });
            }
        }
        updateAodIconColors();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$applyNotificationIconsTint$5(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mIconTint);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$applyNotificationIconsTint$6(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mCenteredIconTint);
    }

    private void updateTintForIcon(StatusBarIconView statusBarIconView, int i) {
        int tint = !Boolean.TRUE.equals(statusBarIconView.getTag(R.id.icon_is_pre_L)) || NotificationUtils.isGrayscale(statusBarIconView, this.mContrastColorUtil) ? DarkIconDispatcher.getTint(this.mTintArea, statusBarIconView, i) : 0;
        boolean z = Settings.System.getIntForUser(this.mContext.getContentResolver(), "statusbar_colored_icons", 0, -2) == 1;
        if (statusBarIconView.getStatusBarIcon().pkg.contains("systemui") || !z) {
            statusBarIconView.setStaticDrawableColor(tint);
            statusBarIconView.setDecorColor(i);
        }
    }

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        this.mNotificationIcons.showIconIsolated(statusBarIconView, z);
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        this.mNotificationIcons.setIsolatedIconLocation(rect, z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        this.mAodIcons.setDozing(z, this.mDozeParameters.getAlwaysOn() && !this.mDozeParameters.getDisplayNeedsBlanking(), 0L);
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateAnimations();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        updateAodIconsVisibility(false);
        updateAnimations();
    }

    private void updateAnimations() {
        boolean z = this.mStatusBarStateController.getState() == 0;
        this.mAodIcons.setAnimationsEnabled(this.mAnimationsEnabled && !z);
        this.mCenteredIcon.setAnimationsEnabled(this.mAnimationsEnabled && z);
        this.mNotificationIcons.setAnimationsEnabled(this.mAnimationsEnabled && z);
    }

    public void onThemeChanged() {
        reloadAodColor();
        updateAodIconColors();
    }

    public void appearAodIcons() {
        if (this.mDozeParameters.shouldControlScreenOff()) {
            this.mAodIcons.setTranslationY(-this.mAodIconAppearTranslation);
            this.mAodIcons.setAlpha(0.0f);
            animateInAodIconTranslation();
            this.mAodIcons.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR).setDuration(200L).start();
            return;
        }
        this.mAodIcons.setAlpha(1.0f);
        this.mAodIcons.setTranslationY(0.0f);
    }

    private void animateInAodIconTranslation() {
        this.mAodIcons.animate().setInterpolator(Interpolators.DECELERATE_QUINT).translationY(0.0f).setDuration(200L).start();
    }

    private void reloadAodColor() {
        this.mAodIconTint = Utils.getColorAttrDefaultColor(this.mContext, R.attr.wallpaperTextColor);
    }

    private void updateAodIconColors() {
        for (int i = 0; i < this.mAodIcons.getChildCount(); i++) {
            final StatusBarIconView statusBarIconView = (StatusBarIconView) this.mAodIcons.getChildAt(i);
            if (statusBarIconView.getWidth() != 0) {
                updateTintForIcon(statusBarIconView, this.mAodIconTint);
            } else {
                statusBarIconView.executeOnLayout(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.lambda$updateAodIconColors$7(statusBarIconView);
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateAodIconColors$7(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mAodIconTint);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onFullyHiddenChanged(boolean z) {
        if (!this.mBypassController.getBypassEnabled()) {
            z = (this.mDozeParameters.getAlwaysOn() && !this.mDozeParameters.getDisplayNeedsBlanking()) & z;
        }
        updateAodIconsVisibility(z);
        updateAodNotificationIcons();
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onPulseExpansionChanged(boolean z) {
        if (z) {
            updateAodIconsVisibility(true);
        }
    }

    private void updateAodIconsVisibility(boolean z) {
        boolean z2 = this.mBypassController.getBypassEnabled() || this.mWakeUpCoordinator.getNotificationsFullyHidden();
        if (this.mStatusBarStateController.getState() != 1) {
            z2 = false;
        }
        if (z2 && this.mWakeUpCoordinator.isPulseExpanding()) {
            z2 = false;
        }
        if (this.mAodIconsVisible != z2) {
            this.mAodIconsVisible = z2;
            this.mAodIcons.animate().cancel();
            if (z) {
                boolean z3 = this.mAodIcons.getVisibility() != 0;
                if (!this.mAodIconsVisible) {
                    animateInAodIconTranslation();
                    CrossFadeHelper.fadeOut(this.mAodIcons);
                    return;
                } else if (z3) {
                    this.mAodIcons.setVisibility(0);
                    this.mAodIcons.setAlpha(1.0f);
                    appearAodIcons();
                    return;
                } else {
                    animateInAodIconTranslation();
                    CrossFadeHelper.fadeIn(this.mAodIcons);
                    return;
                }
            }
            this.mAodIcons.setAlpha(1.0f);
            this.mAodIcons.setTranslationY(0.0f);
            this.mAodIcons.setVisibility(z2 ? 0 : 4);
        }
    }
}
