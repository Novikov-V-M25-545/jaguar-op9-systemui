package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.AmbientState;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationIconContainer;

/* loaded from: classes.dex */
public class NotificationShelf extends ActivatableNotificationView implements View.OnLayoutChangeListener, StatusBarStateController.StateListener {
    private AmbientState mAmbientState;
    private boolean mAnimationsEnabled;
    private final KeyguardBypassController mBypassController;
    private Rect mClipRect;
    private NotificationIconContainer mCollapsedIcons;
    private int mCutoutHeight;
    private float mFirstElementRoundness;
    private int mGapHeight;
    private boolean mHasItemsInStableShelf;
    private float mHiddenShelfIconSize;
    private boolean mHideBackground;
    private NotificationStackScrollLayout mHostLayout;
    private int mIconAppearTopPadding;
    private int mIconSize;
    private boolean mInteractive;
    private int mMaxLayoutHeight;
    private float mMaxShelfEnd;
    private boolean mNoAnimationsInThisFrame;
    private int mNotGoneIndex;
    private float mOpenedAmount;
    private int mPaddingBetweenElements;
    private int mRelativeOffset;
    private int mScrollFastThreshold;
    private NotificationIconContainer mShelfIcons;
    private boolean mShowNotificationShelf;
    private int mStatusBarHeight;
    private int mStatusBarPaddingStart;
    private int mStatusBarState;
    private int[] mTmp;
    private static final boolean USE_ANIMATIONS_WHEN_OPENING = SystemProperties.getBoolean("debug.icon_opening_animations", true);
    private static final boolean ICON_ANMATIONS_WHILE_SCROLLING = SystemProperties.getBoolean("debug.icon_scroll_animations", true);
    private static final int TAG_CONTINUOUS_CLIPPING = R.id.continuous_clipping_tag;

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean hasNoContentHeight() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean needsClippingToShelf() {
        return false;
    }

    public NotificationShelf(Context context, AttributeSet attributeSet, KeyguardBypassController keyguardBypassController) {
        super(context, attributeSet);
        this.mTmp = new int[2];
        this.mAnimationsEnabled = true;
        this.mClipRect = new Rect();
        this.mBypassController = keyguardBypassController;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, android.view.View
    @VisibleForTesting
    public void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        NotificationIconContainer notificationIconContainer = (NotificationIconContainer) findViewById(R.id.content);
        this.mShelfIcons = notificationIconContainer;
        notificationIconContainer.setClipChildren(false);
        this.mShelfIcons.setClipToPadding(false);
        setClipToActualHeight(false);
        setClipChildren(false);
        setClipToPadding(false);
        this.mShelfIcons.setIsStaticLayout(false);
        setBottomRoundness(1.0f, false);
        setFirstInSection(true);
        initDimens();
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this, 3);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback(this);
    }

    public void bind(AmbientState ambientState, NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mAmbientState = ambientState;
        this.mHostLayout = notificationStackScrollLayout;
    }

    private void initDimens() throws Resources.NotFoundException {
        Resources resources = getResources();
        this.mIconAppearTopPadding = resources.getDimensionPixelSize(R.dimen.notification_icon_appear_padding);
        this.mStatusBarHeight = resources.getDimensionPixelOffset(R.dimen.status_bar_height);
        this.mStatusBarPaddingStart = resources.getDimensionPixelOffset(R.dimen.status_bar_padding_start);
        this.mPaddingBetweenElements = resources.getDimensionPixelSize(R.dimen.notification_divider_height);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = resources.getDimensionPixelOffset(R.dimen.notification_shelf_height);
        setLayoutParams(layoutParams);
        int dimensionPixelOffset = resources.getDimensionPixelOffset(R.dimen.shelf_icon_container_padding);
        this.mShelfIcons.setPadding(dimensionPixelOffset, 0, dimensionPixelOffset, 0);
        this.mScrollFastThreshold = resources.getDimensionPixelOffset(R.dimen.scroll_fast_threshold);
        this.mShowNotificationShelf = resources.getBoolean(R.bool.config_showNotificationShelf);
        this.mIconSize = resources.getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_width_low_ram);
        this.mHiddenShelfIconSize = resources.getDimensionPixelOffset(R.dimen.hidden_shelf_icon_size);
        this.mGapHeight = resources.getDimensionPixelSize(R.dimen.qs_notification_padding);
        if (this.mShowNotificationShelf) {
            return;
        }
        setVisibility(8);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    protected View getContentView() {
        return this.mShelfIcons;
    }

    public NotificationIconContainer getShelfIcons() {
        return this.mShelfIcons;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public ExpandableViewState createExpandableViewState() {
        return new ShelfState();
    }

    public void updateState(AmbientState ambientState) {
        ExpandableView lastVisibleBackgroundChild = ambientState.getLastVisibleBackgroundChild();
        ShelfState shelfState = (ShelfState) getViewState();
        boolean z = true;
        if (this.mShowNotificationShelf && lastVisibleBackgroundChild != null) {
            float innerHeight = ambientState.getInnerHeight() + ambientState.getTopPadding() + ambientState.getStackTranslation();
            ExpandableViewState viewState = lastVisibleBackgroundChild.getViewState();
            float f = viewState.yTranslation + viewState.height;
            shelfState.copyFrom(viewState);
            shelfState.height = getIntrinsicHeight();
            shelfState.yTranslation = Math.max(Math.min(f, innerHeight) - shelfState.height, getFullyClosedTranslation());
            shelfState.zTranslation = ambientState.getBaseZHeight();
            shelfState.openedAmount = Math.min(1.0f, (shelfState.yTranslation - getFullyClosedTranslation()) / ((getIntrinsicHeight() * 2) + this.mCutoutHeight));
            shelfState.clipTopAmount = 0;
            shelfState.alpha = 1.0f;
            shelfState.belowSpeedBump = this.mAmbientState.getSpeedBumpIndex() == 0;
            shelfState.hideSensitive = false;
            shelfState.xTranslation = getTranslationX();
            int i = this.mNotGoneIndex;
            if (i != -1) {
                shelfState.notGoneIndex = Math.min(shelfState.notGoneIndex, i);
            }
            shelfState.hasItemsInStableShelf = viewState.inShelf;
            if (this.mAmbientState.isShadeExpanded() && !this.mAmbientState.isQsCustomizerShowing()) {
                z = false;
            }
            shelfState.hidden = z;
            shelfState.maxShelfEnd = innerHeight;
            return;
        }
        shelfState.hidden = true;
        shelfState.location = 64;
        shelfState.hasItemsInStableShelf = false;
    }

    /* JADX WARN: Removed duplicated region for block: B:80:0x016b  */
    /* JADX WARN: Removed duplicated region for block: B:87:0x017c  */
    /* JADX WARN: Removed duplicated region for block: B:90:0x0186  */
    /* JADX WARN: Removed duplicated region for block: B:97:0x01b4  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void updateAppearance() {
        /*
            Method dump skipped, instructions count: 704
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.NotificationShelf.updateAppearance():void");
    }

    private void clipTransientViews() {
        for (int i = 0; i < this.mHostLayout.getTransientViewCount(); i++) {
            View transientView = this.mHostLayout.getTransientView(i);
            if (transientView instanceof ExpandableView) {
                updateNotificationClipHeight((ExpandableView) transientView, getTranslationY(), -1);
            }
        }
    }

    private void setFirstElementRoundness(float f) {
        if (this.mFirstElementRoundness != f) {
            this.mFirstElementRoundness = f;
            setTopRoundness(f, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconClipAmount(ExpandableNotificationRow expandableNotificationRow) {
        float translationY = expandableNotificationRow.getTranslationY();
        if (getClipTopAmount() != 0) {
            translationY = Math.max(translationY, getTranslationY() + getClipTopAmount());
        }
        StatusBarIconView shelfIcon = expandableNotificationRow.getEntry().getIcons().getShelfIcon();
        float translationY2 = getTranslationY() + shelfIcon.getTop() + shelfIcon.getTranslationY();
        if (translationY2 < translationY && !this.mAmbientState.isFullyHidden()) {
            int i = (int) (translationY - translationY2);
            shelfIcon.setClipBounds(new Rect(0, i, shelfIcon.getWidth(), Math.max(i, shelfIcon.getHeight())));
        } else {
            shelfIcon.setClipBounds(null);
        }
    }

    private void updateContinuousClipping(final ExpandableNotificationRow expandableNotificationRow) {
        final StatusBarIconView shelfIcon = expandableNotificationRow.getEntry().getIcons().getShelfIcon();
        boolean z = ViewState.isAnimatingY(shelfIcon) && !this.mAmbientState.isDozing();
        int i = TAG_CONTINUOUS_CLIPPING;
        boolean z2 = shelfIcon.getTag(i) != null;
        if (!z || z2) {
            return;
        }
        final ViewTreeObserver viewTreeObserver = shelfIcon.getViewTreeObserver();
        final ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.NotificationShelf.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                if (ViewState.isAnimatingY(shelfIcon)) {
                    NotificationShelf.this.updateIconClipAmount(expandableNotificationRow);
                    return true;
                }
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.removeOnPreDrawListener(this);
                }
                shelfIcon.setTag(NotificationShelf.TAG_CONTINUOUS_CLIPPING, null);
                return true;
            }
        };
        viewTreeObserver.addOnPreDrawListener(onPreDrawListener);
        shelfIcon.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.statusbar.NotificationShelf.2
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
                if (view == shelfIcon) {
                    if (viewTreeObserver.isAlive()) {
                        viewTreeObserver.removeOnPreDrawListener(onPreDrawListener);
                    }
                    shelfIcon.setTag(NotificationShelf.TAG_CONTINUOUS_CLIPPING, null);
                }
            }
        });
        shelfIcon.setTag(i, onPreDrawListener);
    }

    private int updateNotificationClipHeight(ExpandableView expandableView, float f, int i) {
        float translationY = expandableView.getTranslationY() + expandableView.getActualHeight();
        boolean zShowingPulsing = true;
        boolean z = (expandableView.isPinned() || expandableView.isHeadsUpAnimatingAway()) && !this.mAmbientState.isDozingAndNotPulsing(expandableView);
        if (!this.mAmbientState.isPulseExpanding()) {
            zShowingPulsing = expandableView.showingPulsing();
        } else if (i != 0) {
            zShowingPulsing = false;
        }
        if (translationY > f && !zShowingPulsing && (this.mAmbientState.isShadeExpanded() || !z)) {
            int iMin = (int) (translationY - f);
            if (z) {
                iMin = Math.min(expandableView.getIntrinsicHeight() - expandableView.getCollapsedHeight(), iMin);
            }
            expandableView.setClipBottomAmount(iMin);
        } else {
            expandableView.setClipBottomAmount(0);
        }
        if (zShowingPulsing) {
            return (int) (translationY - getTranslationY());
        }
        return 0;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
        if (!this.mHasItemsInStableShelf) {
            f = 0.0f;
        }
        super.setFakeShadowIntensity(f, f2, i, i2);
    }

    private float updateShelfTransformation(ExpandableView expandableView, float f, boolean z, boolean z2, boolean z3, boolean z4) {
        float f2;
        float f3;
        float f4;
        float fMin;
        float fInterpolate;
        float fConstrain;
        NotificationIconContainer.IconState iconState = getIconState(expandableView.getShelfIcon());
        float translationY = expandableView.getTranslationY();
        int actualHeight = expandableView.getActualHeight() + this.mPaddingBetweenElements;
        float fCalculateIconTransformationStart = calculateIconTransformationStart(expandableView);
        float f5 = actualHeight;
        float fMin2 = Math.min((f5 + translationY) - fCalculateIconTransformationStart, Math.min(getIntrinsicHeight() * 1.5f * NotificationUtils.interpolate(1.0f, 1.5f, f), f5));
        if (z4) {
            actualHeight = Math.min(actualHeight, expandableView.getMinHeight() - getIntrinsicHeight());
            fMin2 = Math.min(fMin2, expandableView.getMinHeight() - getIntrinsicHeight());
        }
        float f6 = actualHeight + translationY;
        handleCustomTransformHeight(expandableView, z3, iconState);
        float translationY2 = getTranslationY();
        boolean z5 = true;
        float f7 = 0.0f;
        if (f6 < translationY2 || ((this.mAmbientState.isUnlockHintRunning() && !expandableView.isInShelf()) || (!this.mAmbientState.isShadeExpanded() && (expandableView.isPinned() || expandableView.isHeadsUpAnimatingAway())))) {
            f2 = fMin2;
            f3 = 0.0f;
            f4 = 0.0f;
            fMin = 0.0f;
        } else {
            if (translationY < translationY2) {
                if (iconState != null && iconState.hasCustomTransformHeight()) {
                    actualHeight = iconState.customTransformHeight;
                    fMin2 = actualHeight;
                }
                float f8 = translationY2 - translationY;
                float fMin3 = Math.min(1.0f, f8 / actualHeight);
                fInterpolate = 1.0f - NotificationUtils.interpolate(Interpolators.ACCELERATE_DECELERATE.getInterpolation(fMin3), fMin3, f);
                fConstrain = 1.0f - MathUtils.constrain(z4 ? f8 / fMin2 : (translationY2 - fCalculateIconTransformationStart) / fMin2, 0.0f, 1.0f);
                z5 = false;
            } else {
                fInterpolate = 1.0f;
                fConstrain = 1.0f;
            }
            fMin = 1.0f - Math.min(1.0f, (translationY2 - translationY) / fMin2);
            f2 = fMin2;
            f4 = fInterpolate;
            f3 = fConstrain;
        }
        if (iconState != null && z5 && !z3 && iconState.isLastExpandIcon) {
            iconState.isLastExpandIcon = false;
            iconState.customTransformHeight = Integer.MIN_VALUE;
        }
        if (!expandableView.isAboveShelf() && !expandableView.showingPulsing() && (z4 || iconState == null || iconState.translateContent)) {
            f7 = fMin;
        }
        expandableView.setContentTransformationAmount(f7, z4);
        updateIconPositioning(expandableView, f3, f4, f2, z, z2, z3, z4);
        return f4;
    }

    private float calculateIconTransformationStart(ExpandableView expandableView) {
        if (expandableView.getShelfTransformationTarget() == null) {
            return expandableView.getTranslationY();
        }
        return (expandableView.getTranslationY() + expandableView.getRelativeTopPadding(r1)) - expandableView.getShelfIcon().getTop();
    }

    private void handleCustomTransformHeight(ExpandableView expandableView, boolean z, NotificationIconContainer.IconState iconState) {
        if (iconState == null || !z || this.mAmbientState.getScrollY() != 0 || this.mAmbientState.isOnKeyguard() || iconState.isLastExpandIcon) {
            return;
        }
        float intrinsicPadding = this.mAmbientState.getIntrinsicPadding() + this.mHostLayout.getPositionInLinearLayout(expandableView);
        float intrinsicHeight = this.mMaxLayoutHeight - getIntrinsicHeight();
        if (intrinsicPadding >= intrinsicHeight || expandableView.getIntrinsicHeight() + intrinsicPadding < intrinsicHeight || expandableView.getTranslationY() >= intrinsicPadding) {
            return;
        }
        iconState.isLastExpandIcon = true;
        iconState.customTransformHeight = Integer.MIN_VALUE;
        if (((float) (this.mMaxLayoutHeight - getIntrinsicHeight())) - intrinsicPadding < ((float) getIntrinsicHeight())) {
            return;
        }
        iconState.customTransformHeight = (int) ((this.mMaxLayoutHeight - getIntrinsicHeight()) - intrinsicPadding);
    }

    private void updateIconPositioning(ExpandableView expandableView, float f, float f2, float f3, boolean z, boolean z2, boolean z3, boolean z4) {
        StatusBarIconView shelfIcon = expandableView.getShelfIcon();
        NotificationIconContainer.IconState iconState = getIconState(shelfIcon);
        if (iconState == null) {
            return;
        }
        boolean z5 = iconState.isLastExpandIcon && !iconState.hasCustomTransformHeight();
        float f4 = 1.0f;
        float f5 = (f > 0.5f ? 1 : (f == 0.5f ? 0 : -1)) > 0 || isTargetClipped(expandableView) ? 1.0f : 0.0f;
        if (f == f5) {
            boolean z6 = (z2 || z3) && !z5;
            iconState.noAnimations = z6;
            iconState.useFullTransitionAmount = z6 || (!ICON_ANMATIONS_WHILE_SCROLLING && f == 0.0f && z);
            iconState.useLinearTransitionAmount = (ICON_ANMATIONS_WHILE_SCROLLING || f != 0.0f || this.mAmbientState.isExpansionChanging()) ? false : true;
            iconState.translateContent = (((float) this.mMaxLayoutHeight) - getTranslationY()) - ((float) getIntrinsicHeight()) > 0.0f;
        }
        if (!z5 && (z2 || (z3 && iconState.useFullTransitionAmount && !ViewState.isAnimatingY(shelfIcon)))) {
            iconState.cancelAnimations(shelfIcon);
            iconState.useFullTransitionAmount = true;
            iconState.noAnimations = true;
        }
        if (iconState.hasCustomTransformHeight()) {
            iconState.useFullTransitionAmount = true;
        }
        if (iconState.isLastExpandIcon) {
            iconState.translateContent = false;
        }
        if (this.mAmbientState.isHiddenAtAll() && !expandableView.isInShelf()) {
            if (!this.mAmbientState.isFullyHidden()) {
                f4 = 0.0f;
            }
        } else if (z4 || !USE_ANIMATIONS_WHEN_OPENING || iconState.useFullTransitionAmount || iconState.useLinearTransitionAmount) {
            f4 = f;
        } else {
            iconState.needsCannedAnimation = (iconState.clampedAppearAmount == f5 || this.mNoAnimationsInThisFrame) ? false : true;
            f4 = f5;
        }
        iconState.iconAppearAmount = (!USE_ANIMATIONS_WHEN_OPENING || iconState.useFullTransitionAmount) ? f2 : f4;
        iconState.clampedAppearAmount = f5;
        setIconTransformationAmount(expandableView, f4, f3, f5 != f4, z4);
    }

    private boolean isTargetClipped(ExpandableView expandableView) {
        View shelfTransformationTarget = expandableView.getShelfTransformationTarget();
        return shelfTransformationTarget != null && ((expandableView.getTranslationY() + expandableView.getContentTranslation()) + ((float) expandableView.getRelativeTopPadding(shelfTransformationTarget))) + ((float) shelfTransformationTarget.getHeight()) >= getTranslationY() - ((float) this.mPaddingBetweenElements);
    }

    private void setIconTransformationAmount(ExpandableView expandableView, float f, float f2, boolean z, boolean z2) {
        int relativeTopPadding;
        float height;
        int relativeStartPadding;
        float f3;
        if (expandableView instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            StatusBarIconView shelfIcon = expandableNotificationRow.getShelfIcon();
            NotificationIconContainer.IconState iconState = getIconState(shelfIcon);
            View shelfTransformationTarget = expandableNotificationRow.getShelfTransformationTarget();
            if (shelfTransformationTarget != null) {
                relativeTopPadding = expandableNotificationRow.getRelativeTopPadding(shelfTransformationTarget);
                relativeStartPadding = expandableNotificationRow.getRelativeStartPadding(shelfTransformationTarget);
                height = shelfTransformationTarget.getHeight();
            } else {
                relativeTopPadding = this.mIconAppearTopPadding;
                height = 0.0f;
                relativeStartPadding = 0;
            }
            float iconScale = (this.mAmbientState.isFullyHidden() ? this.mHiddenShelfIconSize : this.mIconSize) * shelfIcon.getIconScale();
            float translationY = expandableNotificationRow.getTranslationY() + expandableNotificationRow.getContentTranslation();
            boolean z3 = expandableNotificationRow.isInShelf() && !expandableNotificationRow.isTransformingIntoShelf();
            float fInterpolate = NotificationUtils.interpolate((translationY + relativeTopPadding) - ((getTranslationY() + shelfIcon.getTop()) + ((shelfIcon.getHeight() - iconScale) / 2.0f)), (!z || z3) ? 0.0f : NotificationUtils.interpolate(Math.min((this.mIconAppearTopPadding + translationY) - getTranslationY(), 0.0f), 0.0f, f), f);
            float fInterpolate2 = NotificationUtils.interpolate(relativeStartPadding - (shelfIcon.getLeft() + (((1.0f - shelfIcon.getIconScale()) * shelfIcon.getWidth()) / 2.0f)), this.mShelfIcons.getActualPaddingStart(), f);
            boolean z4 = !expandableNotificationRow.isShowingIcon();
            if (z4) {
                height = iconScale / 2.0f;
                fInterpolate2 = this.mShelfIcons.getActualPaddingStart();
                f3 = f;
            } else {
                f3 = 1.0f;
            }
            float fInterpolate3 = NotificationUtils.interpolate(height, iconScale, f);
            if (iconState != null) {
                float f4 = fInterpolate3 / iconScale;
                iconState.scaleX = f4;
                iconState.scaleY = f4;
                iconState.hidden = f == 0.0f && !iconState.isAnimating(shelfIcon);
                if (expandableNotificationRow.isDrawingAppearAnimation() && !expandableNotificationRow.isInShelf()) {
                    iconState.hidden = true;
                    iconState.iconAppearAmount = 0.0f;
                }
                iconState.alpha = f3;
                iconState.yTranslation = fInterpolate;
                iconState.xTranslation = fInterpolate2;
                if (z3) {
                    iconState.iconAppearAmount = 1.0f;
                    iconState.alpha = 1.0f;
                    iconState.scaleX = 1.0f;
                    iconState.scaleY = 1.0f;
                    iconState.hidden = false;
                }
                if (expandableNotificationRow.isAboveShelf() || expandableNotificationRow.showingPulsing() || (!expandableNotificationRow.isInShelf() && ((z2 && expandableNotificationRow.areGutsExposed()) || expandableNotificationRow.getTranslationZ() > this.mAmbientState.getBaseZHeight()))) {
                    iconState.hidden = true;
                }
                int contrastedStaticDrawableColor = shelfIcon.getContrastedStaticDrawableColor(getBackgroundColorWithoutTint());
                if (!z4 && contrastedStaticDrawableColor != 0) {
                    contrastedStaticDrawableColor = NotificationUtils.interpolateColors(expandableNotificationRow.getOriginalIconColor(), contrastedStaticDrawableColor, iconState.iconAppearAmount);
                }
                iconState.iconColor = contrastedStaticDrawableColor;
            }
        }
    }

    private NotificationIconContainer.IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mShelfIcons.getIconState(statusBarIconView);
    }

    private float getFullyClosedTranslation() {
        return (-(getIntrinsicHeight() - this.mStatusBarHeight)) / 2;
    }

    private void setHideBackground(boolean z) {
        if (this.mHideBackground != z) {
            this.mHideBackground = z;
            updateBackground();
            updateOutline();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView
    protected boolean needsOutline() {
        return !this.mHideBackground && super.needsOutline();
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    protected boolean shouldHideBackground() {
        return super.shouldHideBackground() || this.mHideBackground;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateRelativeOffset();
        int i5 = getResources().getDisplayMetrics().heightPixels;
        this.mClipRect.set(0, -i5, getWidth(), i5);
        this.mShelfIcons.setClipBounds(this.mClipRect);
    }

    private void updateRelativeOffset() {
        this.mCollapsedIcons.getLocationOnScreen(this.mTmp);
        int[] iArr = this.mTmp;
        this.mRelativeOffset = iArr[0];
        getLocationOnScreen(iArr);
        this.mRelativeOffset -= this.mTmp[0];
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        WindowInsets windowInsetsOnApplyWindowInsets = super.onApplyWindowInsets(windowInsets);
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        this.mCutoutHeight = (displayCutout == null || displayCutout.getSafeInsetTop() < 0) ? 0 : displayCutout.getSafeInsetTop();
        return windowInsetsOnApplyWindowInsets;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setOpenedAmount(float f) {
        int partialOverflowExtraPadding;
        this.mNoAnimationsInThisFrame = f == 1.0f && this.mOpenedAmount == 0.0f;
        this.mOpenedAmount = f;
        if (!this.mAmbientState.isPanelFullWidth() || this.mAmbientState.isDozing()) {
            f = 1.0f;
        }
        int width = this.mRelativeOffset;
        if (isLayoutRtl()) {
            width = (getWidth() - width) - this.mCollapsedIcons.getWidth();
        }
        this.mShelfIcons.setActualLayoutWidth((int) NotificationUtils.interpolate(this.mCollapsedIcons.getFinalTranslationX() + width, this.mShelfIcons.getWidth(), Interpolators.FAST_OUT_SLOW_IN_REVERSE.getInterpolation(f)));
        boolean zHasOverflow = this.mCollapsedIcons.hasOverflow();
        int paddingEnd = this.mCollapsedIcons.getPaddingEnd();
        if (!zHasOverflow) {
            partialOverflowExtraPadding = this.mCollapsedIcons.getNoOverflowExtraPadding();
        } else {
            partialOverflowExtraPadding = this.mCollapsedIcons.getPartialOverflowExtraPadding();
        }
        this.mShelfIcons.setActualPaddingEnd(NotificationUtils.interpolate(paddingEnd - partialOverflowExtraPadding, this.mShelfIcons.getPaddingEnd(), f));
        this.mShelfIcons.setActualPaddingStart(NotificationUtils.interpolate(width, this.mShelfIcons.getPaddingStart(), f));
        this.mShelfIcons.setOpenedAmount(f);
    }

    public void setMaxLayoutHeight(int i) {
        this.mMaxLayoutHeight = i;
    }

    public int getNotGoneIndex() {
        return this.mNotGoneIndex;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setHasItemsInStableShelf(boolean z) {
        if (this.mHasItemsInStableShelf != z) {
            this.mHasItemsInStableShelf = z;
            updateInteractiveness();
        }
    }

    public void setCollapsedIcons(NotificationIconContainer notificationIconContainer) {
        this.mCollapsedIcons = notificationIconContainer;
        notificationIconContainer.addOnLayoutChangeListener(this);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.mStatusBarState = i;
        updateInteractiveness();
    }

    private void updateInteractiveness() {
        boolean z = this.mStatusBarState == 1 && this.mHasItemsInStableShelf;
        this.mInteractive = z;
        setClickable(z);
        setFocusable(this.mInteractive);
        setImportantForAccessibility(this.mInteractive ? 1 : 4);
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    protected boolean isInteractive() {
        return this.mInteractive;
    }

    public void setMaxShelfEnd(float f) {
        this.mMaxShelfEnd = f;
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        if (z) {
            return;
        }
        this.mShelfIcons.setAnimationsEnabled(false);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mInteractive) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, getContext().getString(R.string.accessibility_overflow_action)));
        }
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateRelativeOffset();
    }

    public void onUiModeChanged() {
        updateBackgroundColors();
    }

    private class ShelfState extends ExpandableViewState {
        private boolean hasItemsInStableShelf;
        private float maxShelfEnd;
        private float openedAmount;

        private ShelfState() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.ExpandableViewState, com.android.systemui.statusbar.notification.stack.ViewState
        public void applyToView(View view) {
            if (NotificationShelf.this.mShowNotificationShelf) {
                super.applyToView(view);
                NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
                NotificationShelf.this.setOpenedAmount(this.openedAmount);
                NotificationShelf.this.updateAppearance();
                NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
                NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
            }
        }

        @Override // com.android.systemui.statusbar.notification.stack.ExpandableViewState, com.android.systemui.statusbar.notification.stack.ViewState
        public void animateTo(View view, AnimationProperties animationProperties) {
            if (NotificationShelf.this.mShowNotificationShelf) {
                super.animateTo(view, animationProperties);
                NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
                NotificationShelf.this.setOpenedAmount(this.openedAmount);
                NotificationShelf.this.updateAppearance();
                NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
                NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
            }
        }
    }
}
