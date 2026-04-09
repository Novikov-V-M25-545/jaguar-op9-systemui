package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import androidx.collection.ArrayMap;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.AlphaOptimizedFrameLayout;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class NotificationIconContainer extends AlphaOptimizedFrameLayout {
    public final int MAX_STATIC_ICONS;
    private final int MAX_VISIBLE_ICONS_ON_LOCK;
    private int[] mAbsolutePosition;
    private int mActualLayoutWidth;
    private float mActualPaddingEnd;
    private float mActualPaddingStart;
    private int mAddAnimationStartIndex;
    private boolean mAnimationsEnabled;
    private int mCannedAnimationStartIndex;
    private boolean mChangingViewPositions;
    private boolean mDisallowNextAnimation;
    private int mDotPadding;
    private boolean mDozing;
    private IconState mFirstVisibleIconState;
    private int mIconSize;
    private final HashMap<View, IconState> mIconStates;
    private boolean mIsStaticLayout;
    private StatusBarIconView mIsolatedIcon;
    private View mIsolatedIconForAnimation;
    private Rect mIsolatedIconLocation;
    private IconState mLastVisibleIconState;
    private int mNumDots;
    private boolean mOnLockScreen;
    private float mOpenedAmount;
    private int mOverflowWidth;
    private ArrayMap<String, ArrayList<StatusBarIcon>> mReplacingIcons;
    private int mSpeedBumpIndex;
    private int mStaticDotDiameter;
    private int mStaticDotRadius;
    private float mVisualOverflowStart;
    private static final AnimationProperties DOT_ANIMATION_PROPERTIES = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.1
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateX();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(200);
    private static final AnimationProperties ICON_ANIMATION_PROPERTIES = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.2
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateX().animateY().animateAlpha().animateScale();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(100);
    private static final AnimationProperties sTempProperties = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.3
        private AnimationFilter mAnimationFilter = new AnimationFilter();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    };
    private static final AnimationProperties ADD_ICON_PROPERTIES = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.4
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateAlpha();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(200).setDelay(50);
    private static final AnimationProperties UNISOLATION_PROPERTY_OTHERS = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.5
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateAlpha();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(110);
    private static final AnimationProperties UNISOLATION_PROPERTY = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.6
        private AnimationFilter mAnimationFilter = new AnimationFilter().animateX();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    }.setDuration(110);

    public NotificationIconContainer(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        super(context, attributeSet);
        this.MAX_VISIBLE_ICONS_ON_LOCK = getResources().getInteger(R.integer.config_maxVisibleNotificationIconsOnLock);
        this.MAX_STATIC_ICONS = getResources().getInteger(R.integer.config_maxVisibleNotificationIcons);
        this.mIsStaticLayout = true;
        this.mIconStates = new HashMap<>();
        this.mActualLayoutWidth = Integer.MIN_VALUE;
        this.mActualPaddingEnd = -2.1474836E9f;
        this.mActualPaddingStart = -2.1474836E9f;
        this.mAddAnimationStartIndex = -1;
        this.mCannedAnimationStartIndex = -1;
        this.mSpeedBumpIndex = -1;
        this.mOpenedAmount = 0.0f;
        this.mAnimationsEnabled = true;
        this.mAbsolutePosition = new int[2];
        initDimens();
        setWillNotDraw(true);
    }

    private void initDimens() throws Resources.NotFoundException {
        this.mDotPadding = getResources().getDimensionPixelSize(R.dimen.overflow_icon_dot_padding);
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.overflow_dot_radius);
        this.mStaticDotRadius = dimensionPixelSize;
        this.mStaticDotDiameter = dimensionPixelSize * 2;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(-65536);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(getActualPaddingStart(), 0.0f, getLayoutEnd(), getHeight(), paint);
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = getHeight() / 2.0f;
        this.mIconSize = 0;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (measuredHeight / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
            if (i5 == 0) {
                setIconSize(childAt.getWidth());
            }
        }
        getLocationOnScreen(this.mAbsolutePosition);
        if (this.mIsStaticLayout) {
            updateState();
        }
    }

    private void setIconSize(int i) {
        this.mIconSize = i;
        this.mOverflowWidth = i + ((this.mStaticDotDiameter + this.mDotPadding) * 0);
    }

    private void updateState() {
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
    }

    public void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            IconState iconState = this.mIconStates.get(childAt);
            if (iconState != null) {
                iconState.applyToView(childAt);
            }
        }
        this.mAddAnimationStartIndex = -1;
        this.mCannedAnimationStartIndex = -1;
        this.mDisallowNextAnimation = false;
        this.mIsolatedIconForAnimation = null;
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        boolean zIsReplacingIcon = isReplacingIcon(view);
        if (!this.mChangingViewPositions) {
            IconState iconState = new IconState(view);
            if (zIsReplacingIcon) {
                iconState.justAdded = false;
                iconState.justReplaced = true;
            }
            this.mIconStates.put(view, iconState);
        }
        int iIndexOfChild = indexOfChild(view);
        if (iIndexOfChild < getChildCount() - 1 && !zIsReplacingIcon && this.mIconStates.get(getChildAt(iIndexOfChild + 1)).iconAppearAmount > 0.0f) {
            int i = this.mAddAnimationStartIndex;
            if (i < 0) {
                this.mAddAnimationStartIndex = iIndexOfChild;
            } else {
                this.mAddAnimationStartIndex = Math.min(i, iIndexOfChild);
            }
        }
        if (view instanceof StatusBarIconView) {
            ((StatusBarIconView) view).setDozing(this.mDozing, false, 0L);
        }
    }

    private boolean isReplacingIcon(View view) {
        if (this.mReplacingIcons == null || !(view instanceof StatusBarIconView)) {
            return false;
        }
        StatusBarIconView statusBarIconView = (StatusBarIconView) view;
        Icon sourceIcon = statusBarIconView.getSourceIcon();
        ArrayList<StatusBarIcon> arrayList = this.mReplacingIcons.get(statusBarIconView.getNotification().getGroupKey());
        return arrayList != null && sourceIcon.sameAs(arrayList.get(0).icon);
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (view instanceof StatusBarIconView) {
            boolean zIsReplacingIcon = isReplacingIcon(view);
            final StatusBarIconView statusBarIconView = (StatusBarIconView) view;
            if (areAnimationsEnabled(statusBarIconView) && statusBarIconView.getVisibleState() != 2 && view.getVisibility() == 0 && zIsReplacingIcon) {
                int iFindFirstViewIndexAfter = findFirstViewIndexAfter(statusBarIconView.getTranslationX());
                int i = this.mAddAnimationStartIndex;
                if (i < 0) {
                    this.mAddAnimationStartIndex = iFindFirstViewIndexAfter;
                } else {
                    this.mAddAnimationStartIndex = Math.min(i, iFindFirstViewIndexAfter);
                }
            }
            if (this.mChangingViewPositions) {
                return;
            }
            this.mIconStates.remove(view);
            if (!areAnimationsEnabled(statusBarIconView) || zIsReplacingIcon) {
                return;
            }
            addTransientView(statusBarIconView, 0);
            statusBarIconView.setVisibleState(2, true, new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onViewRemoved$0(statusBarIconView);
                }
            }, view == this.mIsolatedIcon ? 110L : 0L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onViewRemoved$0(StatusBarIconView statusBarIconView) {
        removeTransientView(statusBarIconView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean areAnimationsEnabled(StatusBarIconView statusBarIconView) {
        return this.mAnimationsEnabled || statusBarIconView == this.mIsolatedIcon;
    }

    private int findFirstViewIndexAfter(float f) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTranslationX() > f) {
                return i;
            }
        }
        return getChildCount();
    }

    public void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            IconState iconState = this.mIconStates.get(childAt);
            iconState.initFrom(childAt);
            StatusBarIconView statusBarIconView = this.mIsolatedIcon;
            iconState.alpha = (statusBarIconView == null || childAt == statusBarIconView) ? 1.0f : 0.0f;
            iconState.hidden = false;
        }
    }

    public void calculateIconTranslations() {
        int i;
        IconState iconState;
        float f;
        boolean z;
        int i2;
        float actualPaddingStart = getActualPaddingStart();
        int childCount = getChildCount();
        if (this.mOnLockScreen) {
            i = this.MAX_VISIBLE_ICONS_ON_LOCK;
        } else {
            i = this.mIsStaticLayout ? this.MAX_STATIC_ICONS : childCount;
        }
        float layoutEnd = getLayoutEnd();
        float maxOverflowStart = getMaxOverflowStart();
        float f2 = 0.0f;
        this.mVisualOverflowStart = 0.0f;
        this.mFirstVisibleIconState = null;
        int i3 = this.mSpeedBumpIndex;
        int i4 = -1;
        boolean z2 = i3 != -1 && i3 < getChildCount();
        int i5 = -1;
        int i6 = 0;
        while (i6 < childCount) {
            View childAt = getChildAt(i6);
            IconState iconState2 = this.mIconStates.get(childAt);
            float f3 = iconState2.iconAppearAmount;
            if (f3 == 1.0f) {
                iconState2.xTranslation = actualPaddingStart;
            }
            if (this.mFirstVisibleIconState == null) {
                this.mFirstVisibleIconState = iconState2;
            }
            int i7 = this.mSpeedBumpIndex;
            boolean z3 = (i7 != i4 && i6 >= i7 && f3 > f2) || i6 >= i;
            boolean z4 = i6 == childCount + (-1);
            float iconScaleIncreased = (this.mOnLockScreen && (childAt instanceof StatusBarIconView)) ? ((StatusBarIconView) childAt).getIconScaleIncreased() : 1.0f;
            if (this.mOpenedAmount != f2) {
                z4 = (!z4 || z2 || z3) ? false : true;
            }
            iconState2.visibleState = 0;
            if (z4) {
                f = layoutEnd - this.mIconSize;
            } else {
                f = maxOverflowStart - this.mIconSize;
            }
            if (actualPaddingStart > f) {
                i2 = -1;
                z = true;
            } else {
                z = false;
                i2 = -1;
            }
            if (i5 == i2 && (z3 || z)) {
                i5 = (!z4 || z3) ? i6 : i6 - 1;
                float f4 = layoutEnd - this.mOverflowWidth;
                this.mVisualOverflowStart = f4;
                if (z3 || this.mIsStaticLayout) {
                    this.mVisualOverflowStart = Math.min(actualPaddingStart, f4);
                }
            }
            actualPaddingStart += iconState2.iconAppearAmount * childAt.getWidth() * iconScaleIncreased;
            i6++;
            f2 = 0.0f;
            i4 = -1;
        }
        this.mNumDots = 0;
        if (i5 != -1) {
            actualPaddingStart = this.mVisualOverflowStart;
            for (int i8 = i5; i8 < childCount; i8++) {
                IconState iconState3 = this.mIconStates.get(getChildAt(i8));
                int i9 = this.mStaticDotDiameter + this.mDotPadding;
                iconState3.xTranslation = actualPaddingStart;
                int i10 = this.mNumDots;
                if (i10 < 1) {
                    if (i10 == 0 && iconState3.iconAppearAmount < 0.8f) {
                        iconState3.visibleState = 0;
                    } else {
                        iconState3.visibleState = 1;
                        this.mNumDots = i10 + 1;
                    }
                    if (this.mNumDots == 1) {
                        i9 *= 1;
                    }
                    actualPaddingStart += i9 * iconState3.iconAppearAmount;
                    this.mLastVisibleIconState = iconState3;
                } else {
                    iconState3.visibleState = 2;
                }
            }
        } else if (childCount > 0) {
            this.mLastVisibleIconState = this.mIconStates.get(getChildAt(childCount - 1));
            this.mFirstVisibleIconState = this.mIconStates.get(getChildAt(0));
        }
        if (this.mOnLockScreen && actualPaddingStart < getLayoutEnd()) {
            IconState iconState4 = this.mFirstVisibleIconState;
            float f5 = iconState4 == null ? 0.0f : iconState4.xTranslation;
            IconState iconState5 = this.mLastVisibleIconState;
            float layoutEnd2 = ((getLayoutEnd() - getActualPaddingStart()) - (iconState5 != null ? Math.min(getWidth(), iconState5.xTranslation + this.mIconSize) - f5 : 0.0f)) / 2.0f;
            if (i5 != -1) {
                layoutEnd2 = (((getLayoutEnd() - this.mVisualOverflowStart) / 2.0f) + layoutEnd2) / 2.0f;
            }
            for (int i11 = 0; i11 < childCount; i11++) {
                this.mIconStates.get(getChildAt(i11)).xTranslation += layoutEnd2;
            }
        }
        if (isLayoutRtl()) {
            for (int i12 = 0; i12 < childCount; i12++) {
                IconState iconState6 = this.mIconStates.get(getChildAt(i12));
                iconState6.xTranslation = (getWidth() - iconState6.xTranslation) - r3.getWidth();
            }
        }
        StatusBarIconView statusBarIconView = this.mIsolatedIcon;
        if (statusBarIconView == null || (iconState = this.mIconStates.get(statusBarIconView)) == null) {
            return;
        }
        iconState.xTranslation = (this.mIsolatedIconLocation.left - this.mAbsolutePosition[0]) - (((1.0f - this.mIsolatedIcon.getIconScale()) * this.mIsolatedIcon.getWidth()) / 2.0f);
        iconState.visibleState = 0;
    }

    private float getLayoutEnd() {
        return getActualWidth() - getActualPaddingEnd();
    }

    private float getActualPaddingEnd() {
        float f = this.mActualPaddingEnd;
        return f == -2.1474836E9f ? getPaddingEnd() : f;
    }

    public float getActualPaddingStart() {
        float f = this.mActualPaddingStart;
        return f == -2.1474836E9f ? getPaddingStart() : f;
    }

    public void setIsStaticLayout(boolean z) {
        this.mIsStaticLayout = z;
    }

    public void setActualLayoutWidth(int i) {
        this.mActualLayoutWidth = i;
    }

    public void setActualPaddingEnd(float f) {
        this.mActualPaddingEnd = f;
    }

    public void setActualPaddingStart(float f) {
        this.mActualPaddingStart = f;
    }

    public int getActualWidth() {
        int i = this.mActualLayoutWidth;
        return i == Integer.MIN_VALUE ? getWidth() : i;
    }

    public int getFinalTranslationX() {
        if (this.mLastVisibleIconState == null) {
            return 0;
        }
        return Math.min(getWidth(), (int) (isLayoutRtl() ? getWidth() - this.mLastVisibleIconState.xTranslation : this.mLastVisibleIconState.xTranslation + this.mIconSize));
    }

    private float getMaxOverflowStart() {
        return getLayoutEnd() - this.mOverflowWidth;
    }

    public void setChangingViewPositions(boolean z) {
        this.mChangingViewPositions = z;
    }

    public void setDozing(boolean z, boolean z2, long j) {
        this.mDozing = z;
        this.mDisallowNextAnimation |= !z2;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof StatusBarIconView) {
                ((StatusBarIconView) childAt).setDozing(z, z2, j);
            }
        }
    }

    public IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mIconStates.get(statusBarIconView);
    }

    public void setSpeedBumpIndex(int i) {
        this.mSpeedBumpIndex = i;
    }

    public void setOpenedAmount(float f) {
        this.mOpenedAmount = f;
    }

    public boolean hasOverflow() {
        return this.mNumDots > 0;
    }

    public boolean hasPartialOverflow() {
        int i = this.mNumDots;
        return i > 0 && i < 1;
    }

    public int getPartialOverflowExtraPadding() {
        if (!hasPartialOverflow()) {
            return 0;
        }
        int i = (1 - this.mNumDots) * (this.mStaticDotDiameter + this.mDotPadding);
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
    }

    public int getNoOverflowExtraPadding() {
        if (this.mNumDots != 0) {
            return 0;
        }
        int i = this.mOverflowWidth;
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
    }

    public void setAnimationsEnabled(boolean z) {
        if (!z && this.mAnimationsEnabled) {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                IconState iconState = this.mIconStates.get(childAt);
                if (iconState != null) {
                    iconState.cancelAnimations(childAt);
                    iconState.applyToView(childAt);
                }
            }
        }
        this.mAnimationsEnabled = z;
    }

    public void setReplacingIcons(ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap) {
        this.mReplacingIcons = arrayMap;
    }

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        if (z) {
            this.mIsolatedIconForAnimation = statusBarIconView != null ? statusBarIconView : this.mIsolatedIcon;
        }
        this.mIsolatedIcon = statusBarIconView;
        updateState();
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        this.mIsolatedIconLocation = rect;
        if (z) {
            updateState();
        }
    }

    public void setOnLockScreen(boolean z) {
        this.mOnLockScreen = z;
    }

    public class IconState extends ViewState {
        public boolean isLastExpandIcon;
        private boolean justReplaced;
        private final View mView;
        public boolean needsCannedAnimation;
        public boolean noAnimations;
        public boolean translateContent;
        public boolean useFullTransitionAmount;
        public boolean useLinearTransitionAmount;
        public int visibleState;
        public float iconAppearAmount = 1.0f;
        public float clampedAppearAmount = 1.0f;
        public boolean justAdded = true;
        public int iconColor = 0;
        public int customTransformHeight = Integer.MIN_VALUE;
        private final Consumer<Property> mCannedAnimationEndListener = new Consumer() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer$IconState$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$new$0((Property) obj);
            }
        };

        public IconState(View view) {
            this.mView = view;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$new$0(Property property) {
            if (property == View.TRANSLATION_Y && this.iconAppearAmount == 0.0f && this.mView.getVisibility() == 0) {
                this.mView.setVisibility(4);
            }
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState
        public void applyToView(View view) {
            boolean z;
            AnimationProperties animationProperties;
            Interpolator interpolator;
            if (view instanceof StatusBarIconView) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) view;
                boolean z2 = (!NotificationIconContainer.this.areAnimationsEnabled(statusBarIconView) || NotificationIconContainer.this.mDisallowNextAnimation || this.noAnimations) ? false : true;
                if (z2) {
                    if (this.justAdded || this.justReplaced) {
                        super.applyToView(statusBarIconView);
                        if (this.justAdded && this.iconAppearAmount != 0.0f) {
                            statusBarIconView.setAlpha(0.0f);
                            statusBarIconView.setVisibleState(2, false);
                            animationProperties = NotificationIconContainer.ADD_ICON_PROPERTIES;
                            z = true;
                        }
                        z = false;
                        animationProperties = null;
                    } else {
                        if (this.visibleState != statusBarIconView.getVisibleState()) {
                            animationProperties = NotificationIconContainer.DOT_ANIMATION_PROPERTIES;
                            z = true;
                        }
                        z = false;
                        animationProperties = null;
                    }
                    if (!z && NotificationIconContainer.this.mAddAnimationStartIndex >= 0 && NotificationIconContainer.this.indexOfChild(view) >= NotificationIconContainer.this.mAddAnimationStartIndex && (statusBarIconView.getVisibleState() != 2 || this.visibleState != 2)) {
                        animationProperties = NotificationIconContainer.DOT_ANIMATION_PROPERTIES;
                        z = true;
                    }
                    if (this.needsCannedAnimation) {
                        AnimationFilter animationFilter = NotificationIconContainer.sTempProperties.getAnimationFilter();
                        animationFilter.reset();
                        animationFilter.combineFilter(NotificationIconContainer.ICON_ANIMATION_PROPERTIES.getAnimationFilter());
                        NotificationIconContainer.sTempProperties.resetCustomInterpolators();
                        NotificationIconContainer.sTempProperties.combineCustomInterpolators(NotificationIconContainer.ICON_ANIMATION_PROPERTIES);
                        if (statusBarIconView.showsConversation()) {
                            interpolator = Interpolators.ICON_OVERSHOT_LESS;
                        } else {
                            interpolator = Interpolators.ICON_OVERSHOT;
                        }
                        NotificationIconContainer.sTempProperties.setCustomInterpolator(View.TRANSLATION_Y, interpolator);
                        NotificationIconContainer.sTempProperties.setAnimationEndAction(this.mCannedAnimationEndListener);
                        if (animationProperties != null) {
                            animationFilter.combineFilter(animationProperties.getAnimationFilter());
                            NotificationIconContainer.sTempProperties.combineCustomInterpolators(animationProperties);
                        }
                        animationProperties = NotificationIconContainer.sTempProperties;
                        animationProperties.setDuration(100L);
                        NotificationIconContainer notificationIconContainer = NotificationIconContainer.this;
                        notificationIconContainer.mCannedAnimationStartIndex = notificationIconContainer.indexOfChild(view);
                        z = true;
                    }
                    if (!z && NotificationIconContainer.this.mCannedAnimationStartIndex >= 0 && NotificationIconContainer.this.indexOfChild(view) > NotificationIconContainer.this.mCannedAnimationStartIndex && (statusBarIconView.getVisibleState() != 2 || this.visibleState != 2)) {
                        AnimationFilter animationFilter2 = NotificationIconContainer.sTempProperties.getAnimationFilter();
                        animationFilter2.reset();
                        animationFilter2.animateX();
                        NotificationIconContainer.sTempProperties.resetCustomInterpolators();
                        animationProperties = NotificationIconContainer.sTempProperties;
                        animationProperties.setDuration(100L);
                        z = true;
                    }
                    if (NotificationIconContainer.this.mIsolatedIconForAnimation != null) {
                        if (view == NotificationIconContainer.this.mIsolatedIconForAnimation) {
                            animationProperties = NotificationIconContainer.UNISOLATION_PROPERTY;
                            animationProperties.setDelay(NotificationIconContainer.this.mIsolatedIcon == null ? 0L : 100L);
                        } else {
                            animationProperties = NotificationIconContainer.UNISOLATION_PROPERTY_OTHERS;
                            animationProperties.setDelay(NotificationIconContainer.this.mIsolatedIcon != null ? 0L : 100L);
                        }
                        z = true;
                    }
                } else {
                    z = false;
                    animationProperties = null;
                }
                statusBarIconView.setVisibleState(this.visibleState, z2);
                boolean z3 = Settings.System.getIntForUser(NotificationIconContainer.this.getContext().getContentResolver(), "statusbar_colored_icons", 0, -2) == 1;
                if (statusBarIconView.getStatusBarIcon().pkg.contains("systemui") || !z3) {
                    statusBarIconView.setIconColor(this.iconColor, this.needsCannedAnimation && z2);
                }
                if (z) {
                    animateTo(statusBarIconView, animationProperties);
                } else {
                    super.applyToView(view);
                }
                NotificationIconContainer.sTempProperties.setAnimationEndAction(null);
                statusBarIconView.setIsInShelf(this.iconAppearAmount == 1.0f);
            }
            this.justAdded = false;
            this.justReplaced = false;
            this.needsCannedAnimation = false;
        }

        public boolean hasCustomTransformHeight() {
            return this.isLastExpandIcon && this.customTransformHeight != Integer.MIN_VALUE;
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState
        public void initFrom(View view) {
            super.initFrom(view);
            if (view instanceof StatusBarIconView) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) view;
                boolean z = Settings.System.getIntForUser(NotificationIconContainer.this.getContext().getContentResolver(), "statusbar_colored_icons", 0, -2) == 1;
                if (statusBarIconView.getStatusBarIcon().pkg.contains("systemui") || !z) {
                    this.iconColor = statusBarIconView.getStaticDrawableColor();
                }
            }
        }
    }
}
