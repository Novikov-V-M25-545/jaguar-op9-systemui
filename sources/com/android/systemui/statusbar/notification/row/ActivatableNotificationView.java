package com.android.systemui.statusbar.notification.row;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import com.android.systemui.Dependency;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.notification.FakeShadowView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.tuner.TunerService;

/* loaded from: classes.dex */
public abstract class ActivatableNotificationView extends ExpandableOutlineView implements TunerService.Tunable {
    private AccessibilityManager mAccessibilityManager;
    private boolean mActivated;
    private float mAnimationTranslationY;
    private float mAppearAnimationFraction;
    private RectF mAppearAnimationRect;
    private float mAppearAnimationTranslation;
    private ValueAnimator mAppearAnimator;
    private ObjectAnimator mBackgroundAnimator;
    private ValueAnimator mBackgroundColorAnimator;
    public NotificationBackgroundView mBackgroundDimmed;
    NotificationBackgroundView mBackgroundNormal;
    private ValueAnimator.AnimatorUpdateListener mBackgroundVisibilityUpdater;
    int mBgTint;
    private Interpolator mCurrentAlphaInterpolator;
    private Interpolator mCurrentAppearInterpolator;
    private int mCurrentBackgroundTint;
    private boolean mDimmed;
    private int mDimmedAlpha;
    private float mDimmedBackgroundFadeInAmount;
    private boolean mDismissed;
    private boolean mDrawingAppearAnimation;
    private FakeShadowView mFakeShadow;
    private int mHeadsUpAddStartLocation;
    private float mHeadsUpLocation;
    private boolean mIsAppearing;
    private boolean mIsBelowSpeedBump;
    private boolean mIsHeadsUpAnimation;
    private boolean mNeedsDimming;
    private float mNormalBackgroundVisibilityAmount;
    private int mNormalColor;
    private int mNormalRippleColor;
    private float mNotificationBackgroundAlpha;
    private OnActivatedListener mOnActivatedListener;
    private OnDimmedListener mOnDimmedListener;
    private float mOverrideAmount;
    private int mOverrideTint;
    private boolean mRefocusOnDismiss;
    private boolean mShadowHidden;
    private final Interpolator mSlowOutFastInInterpolator;
    private final Interpolator mSlowOutLinearInInterpolator;
    private int mStartTint;
    private int mTargetTint;
    private int mTintedRippleColor;
    private Gefingerpoken mTouchHandler;
    private static final Interpolator ACTIVATE_INVERSE_INTERPOLATOR = new PathInterpolator(0.6f, 0.0f, 0.5f, 1.0f);
    private static final Interpolator ACTIVATE_INVERSE_ALPHA_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);

    public interface OnActivatedListener {
        void onActivated(ActivatableNotificationView activatableNotificationView);

        void onActivationReset(ActivatableNotificationView activatableNotificationView);
    }

    interface OnDimmedListener {
        void onSetDimmed(boolean z);
    }

    protected boolean disallowSingleClick(MotionEvent motionEvent) {
        return false;
    }

    protected abstract View getContentView();

    protected boolean handleSlideBack() {
        return false;
    }

    public boolean isDimmable() {
        return true;
    }

    protected boolean isInteractive() {
        return true;
    }

    protected void onAppearAnimationFinished(boolean z) {
    }

    protected void onBelowSpeedBumpChanged() {
    }

    protected boolean shouldHideBackground() {
        return false;
    }

    public ActivatableNotificationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBgTint = 0;
        this.mAppearAnimationRect = new RectF();
        this.mAppearAnimationFraction = -1.0f;
        this.mDimmedBackgroundFadeInAmount = -1.0f;
        this.mBackgroundVisibilityUpdater = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ActivatableNotificationView activatableNotificationView = ActivatableNotificationView.this;
                activatableNotificationView.setNormalBackgroundVisibilityAmount(activatableNotificationView.mBackgroundNormal.getAlpha());
                ActivatableNotificationView activatableNotificationView2 = ActivatableNotificationView.this;
                activatableNotificationView2.mDimmedBackgroundFadeInAmount = activatableNotificationView2.mBackgroundDimmed.getAlpha();
            }
        };
        this.mNotificationBackgroundAlpha = 1.0f;
        this.mSlowOutFastInInterpolator = new PathInterpolator(0.8f, 0.0f, 0.6f, 1.0f);
        this.mSlowOutLinearInInterpolator = new PathInterpolator(0.8f, 0.0f, 1.0f, 1.0f);
        setClipChildren(false);
        setClipToPadding(false);
        updateColors();
        initDimens();
    }

    private void updateColors() {
        this.mNormalColor = ((FrameLayout) this).mContext.getColor(R.color.notification_material_background_color);
        this.mTintedRippleColor = ((FrameLayout) this).mContext.getColor(R.color.notification_ripple_tinted_color);
        this.mNormalRippleColor = ((FrameLayout) this).mContext.getColor(R.color.notification_ripple_untinted_color);
        this.mDimmedAlpha = Color.alpha(((FrameLayout) this).mContext.getColor(R.color.notification_material_background_dimmed_color));
    }

    private void initDimens() {
        this.mHeadsUpAddStartLocation = getResources().getDimensionPixelSize(android.R.dimen.indeterminate_progress_alpha_60);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView
    public void onDensityOrFontScaleChanged() throws Resources.NotFoundException {
        super.onDensityOrFontScaleChanged();
        initDimens();
    }

    protected void updateBackgroundColors() {
        updateColors();
        initBackground();
        updateBackgroundTint();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(R.id.backgroundNormal);
        FakeShadowView fakeShadowView = (FakeShadowView) findViewById(R.id.fake_shadow);
        this.mFakeShadow = fakeShadowView;
        this.mShadowHidden = fakeShadowView.getVisibility() != 0;
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(R.id.backgroundDimmed);
        initBackground();
        updateBackground();
        updateBackgroundTint();
        updateOutlineAlpha();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:notification_bg_alpha");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        if (str.equals("system:notification_bg_alpha")) {
            this.mNotificationBackgroundAlpha = TunerService.parseInteger(str2, 255) / 255.0f;
            resetBackgroundAlpha();
            updateBackground();
            updateOutlineAlpha();
        }
    }

    protected void initBackground() {
        this.mBackgroundNormal.setCustomBackground(R.drawable.notification_material_bg);
        this.mBackgroundDimmed.setCustomBackground(R.drawable.notification_material_bg_dim);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        Gefingerpoken gefingerpoken = this.mTouchHandler;
        if (gefingerpoken == null || !gefingerpoken.onInterceptTouchEvent(motionEvent)) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float f, float f2) {
        if (this.mDimmed) {
            return;
        }
        this.mBackgroundNormal.drawableHotspotChanged(f, f2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mDimmed) {
            this.mBackgroundDimmed.setState(getDrawableState());
        } else {
            this.mBackgroundNormal.setState(getDrawableState());
        }
    }

    void setRippleAllowed(boolean z) {
        this.mBackgroundNormal.setPressedAllowed(z);
    }

    void makeActive() {
        startActivateAnimation(false);
        this.mActivated = true;
        OnActivatedListener onActivatedListener = this.mOnActivatedListener;
        if (onActivatedListener != null) {
            onActivatedListener.onActivated(this);
        }
    }

    public boolean isActive() {
        return this.mActivated;
    }

    private void startActivateAnimation(final boolean z) {
        Animator animatorCreateCircularReveal;
        Interpolator interpolator;
        Interpolator interpolator2;
        if (isAttachedToWindow() && isDimmable()) {
            int width = this.mBackgroundNormal.getWidth() / 2;
            int actualHeight = this.mBackgroundNormal.getActualHeight() / 2;
            float fSqrt = (float) Math.sqrt((width * width) + (actualHeight * actualHeight));
            if (z) {
                animatorCreateCircularReveal = ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, width, actualHeight, fSqrt, 0.0f);
            } else {
                animatorCreateCircularReveal = ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, width, actualHeight, 0.0f, fSqrt);
            }
            this.mBackgroundNormal.setVisibility(0);
            if (!z) {
                interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
                interpolator2 = interpolator;
            } else {
                interpolator = ACTIVATE_INVERSE_INTERPOLATOR;
                interpolator2 = ACTIVATE_INVERSE_ALPHA_INTERPOLATOR;
            }
            animatorCreateCircularReveal.setInterpolator(interpolator);
            animatorCreateCircularReveal.setDuration(220L);
            if (z) {
                this.mBackgroundNormal.setAlpha(this.mNotificationBackgroundAlpha);
                animatorCreateCircularReveal.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView.2
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        ActivatableNotificationView.this.updateBackground();
                    }
                });
                animatorCreateCircularReveal.start();
            } else {
                this.mBackgroundNormal.setAlpha(Math.min(this.mNotificationBackgroundAlpha, 0.4f));
                animatorCreateCircularReveal.start();
            }
            this.mBackgroundNormal.animate().alpha(z ? 0.0f : this.mNotificationBackgroundAlpha).setInterpolator(interpolator2).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView$$ExternalSyntheticLambda2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.lambda$startActivateAnimation$0(z, valueAnimator);
                }
            }).setDuration(220L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startActivateAnimation$0(boolean z, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (z) {
            animatedFraction = 1.0f - animatedFraction;
        }
        setNormalBackgroundVisibilityAmount(animatedFraction);
    }

    public void makeInactive(boolean z) {
        if (this.mActivated) {
            this.mActivated = false;
            if (this.mDimmed) {
                if (z) {
                    startActivateAnimation(true);
                } else {
                    updateBackground();
                }
            }
        }
        OnActivatedListener onActivatedListener = this.mOnActivatedListener;
        if (onActivatedListener != null) {
            onActivatedListener.onActivationReset(this);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setDimmed(boolean z, boolean z2) {
        this.mNeedsDimming = z;
        OnDimmedListener onDimmedListener = this.mOnDimmedListener;
        if (onDimmedListener != null) {
            onDimmedListener.onSetDimmed(z);
        }
        boolean zIsDimmable = z & isDimmable();
        if (this.mDimmed != zIsDimmable) {
            this.mDimmed = zIsDimmable;
            resetBackgroundAlpha();
            if (z2) {
                fadeDimmedBackground();
            } else {
                updateBackground();
            }
        }
    }

    public boolean isDimmed() {
        return this.mDimmed;
    }

    private void updateOutlineAlpha() {
        setOutlineAlpha(Math.min((0.3f * this.mNormalBackgroundVisibilityAmount) + 0.7f, this.mNotificationBackgroundAlpha));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setNormalBackgroundVisibilityAmount(float f) {
        this.mNormalBackgroundVisibilityAmount = f;
        updateOutlineAlpha();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setBelowSpeedBump(boolean z) {
        super.setBelowSpeedBump(z);
        if (z != this.mIsBelowSpeedBump) {
            this.mIsBelowSpeedBump = z;
            updateBackgroundTint();
            onBelowSpeedBumpChanged();
        }
    }

    protected void setTintColor(int i) {
        setTintColor(i, false);
    }

    void setTintColor(int i, boolean z) {
        if (i != this.mBgTint) {
            this.mBgTint = i;
            updateBackgroundTint(z);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setDistanceToTopRoundness(float f) {
        super.setDistanceToTopRoundness(f);
        this.mBackgroundNormal.setDistanceToTopRoundness(f);
        this.mBackgroundDimmed.setDistanceToTopRoundness(f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setLastInSection(boolean z) {
        if (z != this.mLastInSection) {
            super.setLastInSection(z);
            this.mBackgroundNormal.setLastInSection(z);
            this.mBackgroundDimmed.setLastInSection(z);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setFirstInSection(boolean z) {
        if (z != this.mFirstInSection) {
            super.setFirstInSection(z);
            this.mBackgroundNormal.setFirstInSection(z);
            this.mBackgroundDimmed.setFirstInSection(z);
        }
    }

    public void setOverrideTintColor(int i, float f) {
        this.mOverrideTint = i;
        this.mOverrideAmount = f;
        setBackgroundTintColor(calculateBgColor());
        if (!isDimmable() && this.mNeedsDimming) {
            this.mBackgroundNormal.setDrawableAlpha((int) NotificationUtils.interpolate(255.0f, this.mDimmedAlpha, f));
        } else {
            this.mBackgroundNormal.setDrawableAlpha(255);
        }
    }

    protected void updateBackgroundTint() {
        updateBackgroundTint(false);
    }

    private void updateBackgroundTint(boolean z) {
        ValueAnimator valueAnimator = this.mBackgroundColorAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        int rippleColor = getRippleColor();
        this.mBackgroundDimmed.setRippleColor(rippleColor);
        this.mBackgroundNormal.setRippleColor(rippleColor);
        int iCalculateBgColor = calculateBgColor();
        if (!z) {
            setBackgroundTintColor(iCalculateBgColor);
            return;
        }
        int i = this.mCurrentBackgroundTint;
        if (iCalculateBgColor != i) {
            this.mStartTint = i;
            this.mTargetTint = iCalculateBgColor;
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, this.mNotificationBackgroundAlpha);
            this.mBackgroundColorAnimator = valueAnimatorOfFloat;
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView$$ExternalSyntheticLambda1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    this.f$0.lambda$updateBackgroundTint$1(valueAnimator2);
                }
            });
            this.mBackgroundColorAnimator.setDuration(360L);
            this.mBackgroundColorAnimator.setInterpolator(Interpolators.LINEAR);
            this.mBackgroundColorAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView.3
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    ActivatableNotificationView.this.mBackgroundColorAnimator = null;
                }
            });
            this.mBackgroundColorAnimator.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateBackgroundTint$1(ValueAnimator valueAnimator) {
        setBackgroundTintColor(NotificationUtils.interpolateColors(this.mStartTint, this.mTargetTint, valueAnimator.getAnimatedFraction()));
    }

    protected void setBackgroundTintColor(int i) {
        if (i != this.mCurrentBackgroundTint) {
            this.mCurrentBackgroundTint = i;
            this.mBackgroundDimmed.setTint(i);
            this.mBackgroundNormal.setTint(i);
        }
    }

    private void fadeDimmedBackground() {
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
        if (this.mActivated) {
            updateBackground();
            return;
        }
        if (!shouldHideBackground()) {
            if (this.mDimmed) {
                this.mBackgroundDimmed.setVisibility(0);
            } else {
                this.mBackgroundNormal.setVisibility(0);
            }
        }
        boolean z = this.mDimmed;
        float fFloatValue = z ? this.mNotificationBackgroundAlpha : 0.0f;
        float f = z ? 0.0f : this.mNotificationBackgroundAlpha;
        int currentPlayTime = 220;
        ObjectAnimator objectAnimator = this.mBackgroundAnimator;
        if (objectAnimator != null) {
            fFloatValue = ((Float) objectAnimator.getAnimatedValue()).floatValue();
            currentPlayTime = (int) this.mBackgroundAnimator.getCurrentPlayTime();
            this.mBackgroundAnimator.removeAllListeners();
            this.mBackgroundAnimator.cancel();
            if (currentPlayTime <= 0) {
                updateBackground();
                return;
            }
        }
        this.mBackgroundNormal.setAlpha(fFloatValue);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mBackgroundNormal, (Property<NotificationBackgroundView, Float>) View.ALPHA, fFloatValue, f);
        this.mBackgroundAnimator = objectAnimatorOfFloat;
        objectAnimatorOfFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBackgroundAnimator.setDuration(currentPlayTime);
        this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                ActivatableNotificationView.this.updateBackground();
                ActivatableNotificationView.this.mBackgroundAnimator = null;
                ActivatableNotificationView.this.mDimmedBackgroundFadeInAmount = -1.0f;
            }
        });
        this.mBackgroundAnimator.addUpdateListener(this.mBackgroundVisibilityUpdater);
        this.mBackgroundAnimator.start();
    }

    protected void updateBackgroundAlpha(float f) {
        if (!isChildInGroup() || !this.mDimmed) {
            f = this.mNotificationBackgroundAlpha;
        }
        float f2 = this.mDimmedBackgroundFadeInAmount;
        if (f2 != -1.0f) {
            f *= f2;
        }
        this.mBackgroundDimmed.setAlpha(f);
    }

    protected void resetBackgroundAlpha() {
        updateBackgroundAlpha(0.0f);
    }

    protected void updateBackground() {
        cancelFadeAnimations();
        if (shouldHideBackground()) {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(this.mActivated ? 0 : 4);
        } else if (this.mDimmed) {
            boolean z = isGroupExpansionChanging() && isChildInGroup();
            this.mBackgroundDimmed.setVisibility(z ? 4 : 0);
            this.mBackgroundNormal.setVisibility((this.mActivated || z) ? 0 : 4);
        } else {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(0);
            this.mBackgroundNormal.setAlpha(this.mNotificationBackgroundAlpha);
            makeInactive(false);
        }
        setNormalBackgroundVisibilityAmount(this.mBackgroundNormal.getVisibility() == 0 ? 1.0f : 0.0f);
    }

    protected void updateBackgroundClipping() {
        this.mBackgroundNormal.setBottomAmountClips(!isChildInGroup());
        this.mBackgroundDimmed.setBottomAmountClips(!isChildInGroup());
    }

    private void cancelFadeAnimations() {
        ObjectAnimator objectAnimator = this.mBackgroundAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setPivotX(getWidth() / 2);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        setPivotY(i / 2);
        this.mBackgroundNormal.setActualHeight(i);
        this.mBackgroundDimmed.setActualHeight(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        this.mBackgroundNormal.setClipTopAmount(i);
        this.mBackgroundDimmed.setClipTopAmount(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setClipBottomAmount(int i) {
        super.setClipBottomAmount(i);
        this.mBackgroundNormal.setClipBottomAmount(i);
        this.mBackgroundDimmed.setClipBottomAmount(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public long performRemoveAnimation(long j, long j2, float f, boolean z, float f2, Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter) {
        enableAppearDrawing(true);
        this.mIsHeadsUpAnimation = z;
        this.mHeadsUpLocation = f2;
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(false, f, j2, j, runnable, animatorListenerAdapter);
            return 0L;
        }
        if (runnable == null) {
            return 0L;
        }
        runnable.run();
        return 0L;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void performAddAnimation(long j, long j2, boolean z) {
        enableAppearDrawing(true);
        this.mIsHeadsUpAnimation = z;
        this.mHeadsUpLocation = this.mHeadsUpAddStartLocation;
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(true, z ? 0.0f : -1.0f, j, j2, null, null);
        }
    }

    private void startAppearAnimation(final boolean z, float f, long j, long j2, final Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter) {
        cancelAppearAnimation();
        float actualHeight = f * getActualHeight();
        this.mAnimationTranslationY = actualHeight;
        float f2 = 1.0f;
        if (this.mAppearAnimationFraction == -1.0f) {
            if (z) {
                this.mAppearAnimationFraction = 0.0f;
                this.mAppearAnimationTranslation = actualHeight;
            } else {
                this.mAppearAnimationFraction = 1.0f;
                this.mAppearAnimationTranslation = 0.0f;
            }
        }
        this.mIsAppearing = z;
        if (z) {
            this.mCurrentAppearInterpolator = this.mSlowOutFastInInterpolator;
            this.mCurrentAlphaInterpolator = Interpolators.LINEAR_OUT_SLOW_IN;
        } else {
            this.mCurrentAppearInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            this.mCurrentAlphaInterpolator = this.mSlowOutLinearInInterpolator;
            f2 = 0.0f;
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(this.mAppearAnimationFraction, f2);
        this.mAppearAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setInterpolator(Interpolators.LINEAR);
        this.mAppearAnimator.setDuration((long) (j2 * Math.abs(this.mAppearAnimationFraction - f2)));
        this.mAppearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$startAppearAnimation$2(valueAnimator);
            }
        });
        if (animatorListenerAdapter != null) {
            this.mAppearAnimator.addListener(animatorListenerAdapter);
        }
        if (j > 0) {
            updateAppearAnimationAlpha();
            updateAppearRect();
            this.mAppearAnimator.setStartDelay(j);
        }
        this.mAppearAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.row.ActivatableNotificationView.5
            private boolean mWasCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
                if (this.mWasCancelled) {
                    return;
                }
                ActivatableNotificationView.this.enableAppearDrawing(false);
                ActivatableNotificationView.this.onAppearAnimationFinished(z);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }
        });
        this.mAppearAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startAppearAnimation$2(ValueAnimator valueAnimator) {
        this.mAppearAnimationFraction = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        updateAppearAnimationAlpha();
        updateAppearRect();
        invalidate();
    }

    private void cancelAppearAnimation() {
        ValueAnimator valueAnimator = this.mAppearAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mAppearAnimator = null;
        }
    }

    public void cancelAppearDrawing() {
        cancelAppearAnimation();
        enableAppearDrawing(false);
    }

    private void updateAppearRect() {
        float width;
        float width2;
        float f;
        float f2;
        float f3 = 1.0f - this.mAppearAnimationFraction;
        float interpolation = this.mCurrentAppearInterpolator.getInterpolation(f3) * this.mAnimationTranslationY;
        this.mAppearAnimationTranslation = interpolation;
        float f4 = f3 - 0.0f;
        float interpolation2 = 1.0f - this.mCurrentAppearInterpolator.getInterpolation(Math.min(1.0f, Math.max(0.0f, f4 / 0.8f)));
        float fLerp = MathUtils.lerp((!this.mIsHeadsUpAnimation || this.mIsAppearing) ? 0.05f : 0.0f, 1.0f, interpolation2) * getWidth();
        if (this.mIsHeadsUpAnimation) {
            width = MathUtils.lerp(this.mHeadsUpLocation, 0.0f, interpolation2);
            width2 = fLerp + width;
        } else {
            width = (getWidth() * 0.5f) - (fLerp / 2.0f);
            width2 = getWidth() - width;
        }
        float interpolation3 = this.mCurrentAppearInterpolator.getInterpolation(Math.max(0.0f, f4 / 1.0f));
        int actualHeight = getActualHeight();
        float f5 = this.mAnimationTranslationY;
        if (f5 > 0.0f) {
            f = (actualHeight - ((f5 * interpolation3) * 0.1f)) - interpolation;
            f2 = interpolation3 * f;
        } else {
            float f6 = actualHeight;
            float f7 = (((f5 + f6) * interpolation3) * 0.1f) - interpolation;
            f = (f6 * (1.0f - interpolation3)) + (interpolation3 * f7);
            f2 = f7;
        }
        this.mAppearAnimationRect.set(width, f2, width2, f);
        float f8 = this.mAppearAnimationTranslation;
        setOutlineRect(width, f2 + f8, width2, f + f8);
    }

    private void updateAppearAnimationAlpha() {
        setContentAlpha(this.mCurrentAlphaInterpolator.getInterpolation(Math.min(1.0f, this.mAppearAnimationFraction / 1.0f)));
    }

    private void setContentAlpha(float f) {
        View contentView = getContentView();
        if (contentView.hasOverlappingRendering()) {
            int i = (f == 0.0f || f == 1.0f) ? 0 : 2;
            if (contentView.getLayerType() != i) {
                contentView.setLayerType(i, null);
            }
        }
        contentView.setAlpha(f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView
    protected void applyRoundness() {
        super.applyRoundness();
        applyBackgroundRoundness(getCurrentBackgroundRadiusTop(), getCurrentBackgroundRadiusBottom());
    }

    private void applyBackgroundRoundness(float f, float f2) {
        this.mBackgroundDimmed.setRoundness(f, f2);
        this.mBackgroundNormal.setRoundness(f, f2);
    }

    protected void setBackgroundTop(int i) {
        this.mBackgroundDimmed.setBackgroundTop(i);
        this.mBackgroundNormal.setBackgroundTop(i);
    }

    public int calculateBgColor() {
        return calculateBgColor(true, true);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView
    protected boolean childNeedsClipping(View view) {
        if ((view instanceof NotificationBackgroundView) && isClippingNeeded()) {
            return true;
        }
        return super.childNeedsClipping(view);
    }

    private int calculateBgColor(boolean z, boolean z2) {
        int i;
        if (!z2 || this.mOverrideTint == 0) {
            return (!z || (i = this.mBgTint) == 0) ? this.mNormalColor : i;
        }
        return NotificationUtils.interpolateColors(calculateBgColor(z, false), this.mOverrideTint, this.mOverrideAmount);
    }

    private int getRippleColor() {
        if (this.mBgTint != 0) {
            return this.mTintedRippleColor;
        }
        return this.mNormalRippleColor;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableAppearDrawing(boolean z) {
        if (z != this.mDrawingAppearAnimation) {
            this.mDrawingAppearAnimation = z;
            if (!z) {
                setContentAlpha(1.0f);
                this.mAppearAnimationFraction = -1.0f;
                setOutlineRect(null);
            }
            invalidate();
        }
    }

    public boolean isDrawingAppearAnimation() {
        return this.mDrawingAppearAnimation;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        if (this.mDrawingAppearAnimation) {
            canvas.save();
            canvas.translate(0.0f, this.mAppearAnimationTranslation);
        }
        super.dispatchDraw(canvas);
        if (this.mDrawingAppearAnimation) {
            canvas.restore();
        }
    }

    public void setOnActivatedListener(OnActivatedListener onActivatedListener) {
        this.mOnActivatedListener = onActivatedListener;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
        boolean z = this.mShadowHidden;
        boolean z2 = f == 0.0f;
        this.mShadowHidden = z2;
        if (z2 && z) {
            return;
        }
        this.mFakeShadow.setFakeShadowTranslationZ(f * (getTranslationZ() + 0.1f), f2, i, i2);
    }

    public int getBackgroundColorWithoutTint() {
        return calculateBgColor(false, false);
    }

    public int getCurrentBackgroundTint() {
        return this.mCurrentBackgroundTint;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public int getHeadsUpHeightWithoutHeader() {
        return getHeight();
    }

    public void dismiss(boolean z) {
        this.mDismissed = true;
        this.mRefocusOnDismiss = z;
    }

    public void unDismiss() {
        this.mDismissed = false;
    }

    public boolean isDismissed() {
        return this.mDismissed;
    }

    public boolean shouldRefocusOnDismiss() {
        return this.mRefocusOnDismiss || isAccessibilityFocused();
    }

    void setTouchHandler(Gefingerpoken gefingerpoken) {
        this.mTouchHandler = gefingerpoken;
    }

    void setOnDimmedListener(OnDimmedListener onDimmedListener) {
        this.mOnDimmedListener = onDimmedListener;
    }

    public void setAccessibilityManager(AccessibilityManager accessibilityManager) {
        this.mAccessibilityManager = accessibilityManager;
    }
}
