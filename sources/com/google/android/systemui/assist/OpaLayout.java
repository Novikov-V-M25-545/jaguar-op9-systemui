package com.google.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Trace;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.ButtonInterface;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.KeyButtonView;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class OpaLayout extends FrameLayout implements ButtonInterface {
    private final Interpolator HOME_DISAPPEAR_INTERPOLATOR;
    private boolean mAllowAnimation;
    private final ArrayList<View> mAnimatedViews;
    private int mAnimationState;
    private View mBlue;
    private View mBottom;
    private Context mContext;
    private final ArraySet<Animator> mCurrentAnimators;
    private boolean mDelayTouchFeedback;
    private final Runnable mDiamondAnimation;
    private boolean mDiamondAnimationDelayed;
    private final Interpolator mDiamondInterpolator;
    private AnimatorSet mGestureAnimatorSet;
    private int mGestureState;
    private View mGreen;
    private Handler mHandler;
    private KeyButtonView mHome;
    private int mHomeDiameter;
    private boolean mIsPressed;
    private boolean mIsVertical;
    private View mLeft;
    private boolean mOpaEnabled;
    private boolean mOpaEnabledNeedsUpdate;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener;
    private OverviewProxyService mOverviewProxyService;
    private View mRed;
    private Resources mResources;
    private final Runnable mRetract;
    private View mRight;
    private SettingsObserver mSettingsObserver;
    private long mStartTime;
    private View mTop;
    private int mTouchDownX;
    private int mTouchDownY;
    private ImageView mWhite;
    private boolean mWindowVisible;
    private View mYellow;

    public OpaLayout(Context context) {
        this(context, null);
    }

    public OpaLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpaLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mAllowAnimation = true;
        this.HOME_DISAPPEAR_INTERPOLATOR = new PathInterpolator(0.65f, 0.0f, 1.0f, 1.0f);
        this.mDiamondInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
        this.mCurrentAnimators = new ArraySet<>();
        this.mAnimatedViews = new ArrayList<>();
        this.mAnimationState = 0;
        this.mGestureState = 0;
        this.mRetract = new Runnable() { // from class: com.google.android.systemui.assist.OpaLayout.1
            @Override // java.lang.Runnable
            public void run() {
                OpaLayout.this.cancelCurrentAnimation("retract");
                OpaLayout.this.startRetractAnimation();
                OpaLayout.this.hideAllOpa();
            }
        };
        this.mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.google.android.systemui.assist.OpaLayout.2
            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onConnectionChanged(boolean z) {
                OpaLayout.this.updateOpaLayout();
            }
        };
        this.mDiamondAnimation = new Runnable() { // from class: com.google.android.systemui.assist.OpaLayout.3
            @Override // java.lang.Runnable
            public final void run() {
                if (OpaLayout.this.mCurrentAnimators.isEmpty()) {
                    OpaLayout.this.startDiamondAnimation();
                }
            }
        };
        this.mContext = context;
        this.mHandler = new Handler();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
    }

    public OpaLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mResources = getResources();
        this.mBlue = findViewById(R.id.blue);
        this.mRed = findViewById(R.id.red);
        this.mYellow = findViewById(R.id.yellow);
        this.mGreen = findViewById(R.id.green);
        this.mWhite = (ImageView) findViewById(R.id.white);
        this.mHome = (KeyButtonView) findViewById(R.id.home_button);
        this.mHomeDiameter = this.mResources.getDimensionPixelSize(R.dimen.opa_disabled_home_diameter);
        this.mAnimatedViews.add(this.mBlue);
        this.mAnimatedViews.add(this.mRed);
        this.mAnimatedViews.add(this.mYellow);
        this.mAnimatedViews.add(this.mGreen);
        this.mAnimatedViews.add(this.mWhite);
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mSettingsObserver.observe();
        hideAllOpa();
    }

    @Override // android.view.View
    public void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        this.mWindowVisible = i == 0;
        if (i == 0) {
            updateOpaLayout();
            return;
        }
        cancelCurrentAnimation("winVis=" + i);
        skipToStartingValue();
    }

    @Override // android.view.View
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.mHome.setOnLongClickListener(onLongClickListener);
    }

    @Override // android.view.View
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mHome.setOnTouchListener(onTouchListener);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (getOpaEnabled() && ValueAnimator.areAnimatorsEnabled() && this.mGestureState == 0) {
            int action = motionEvent.getAction();
            if (action != 0) {
                if (action != 1) {
                    if (action == 2) {
                        float quickStepTouchSlopPx = QuickStepContract.getQuickStepTouchSlopPx(getContext());
                        if (Math.abs(motionEvent.getRawX() - this.mTouchDownX) <= quickStepTouchSlopPx && Math.abs(motionEvent.getRawY() - this.mTouchDownY) <= quickStepTouchSlopPx) {
                            return false;
                        }
                        abortCurrentGesture();
                        return false;
                    }
                    if (action != 3) {
                        return false;
                    }
                }
                if (this.mDiamondAnimationDelayed) {
                    if (this.mIsPressed) {
                        postDelayed(this.mRetract, 200L);
                    }
                } else {
                    if (this.mAnimationState == 1) {
                        removeCallbacks(this.mRetract);
                        postDelayed(this.mRetract, 100 - (SystemClock.elapsedRealtime() - this.mStartTime));
                        removeCallbacks(this.mDiamondAnimation);
                        cancelLongPress();
                        return false;
                    }
                    if (this.mIsPressed) {
                        this.mRetract.run();
                    }
                }
                this.mIsPressed = false;
            } else {
                this.mTouchDownX = (int) motionEvent.getRawX();
                this.mTouchDownY = (int) motionEvent.getRawY();
                if (this.mCurrentAnimators.isEmpty()) {
                    z = false;
                } else {
                    if (this.mAnimationState != 2) {
                        return false;
                    }
                    endCurrentAnimation("touchDown");
                    z = true;
                }
                this.mStartTime = SystemClock.elapsedRealtime();
                this.mIsPressed = true;
                removeCallbacks(this.mDiamondAnimation);
                removeCallbacks(this.mRetract);
                if (!this.mDelayTouchFeedback || z) {
                    this.mDiamondAnimationDelayed = false;
                    startDiamondAnimation();
                } else {
                    this.mDiamondAnimationDelayed = true;
                    postDelayed(this.mDiamondAnimation, ViewConfiguration.getTapTimeout());
                }
            }
        }
        return false;
    }

    @Override // android.view.View
    public void setAccessibilityDelegate(View.AccessibilityDelegate accessibilityDelegate) {
        super.setAccessibilityDelegate(accessibilityDelegate);
        this.mHome.setAccessibilityDelegate(accessibilityDelegate);
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setImageDrawable(Drawable drawable) {
        this.mWhite.setImageDrawable(drawable);
    }

    public void abortCurrentGesture() {
        Trace.beginSection("OpaLayout.abortCurrentGesture: animState=" + this.mAnimationState);
        Trace.endSection();
        this.mHome.abortCurrentGesture();
        this.mIsPressed = false;
        this.mDiamondAnimationDelayed = false;
        removeCallbacks(this.mDiamondAnimation);
        cancelLongPress();
        int i = this.mAnimationState;
        if (i == 3 || i == 1) {
            this.mRetract.run();
        }
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateOpaLayout();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
        this.mOpaEnabledNeedsUpdate = true;
        post(new Runnable() { // from class: com.google.android.systemui.assist.OpaLayout.4
            @Override // java.lang.Runnable
            public final void run() {
                OpaLayout.this.getOpaEnabled();
            }
        });
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            OpaLayout.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("pixel_nav_animation"), false, this, -1);
            update();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update();
        }

        public void update() {
            OpaLayout opaLayout = OpaLayout.this;
            opaLayout.mAllowAnimation = Settings.System.getIntForUser(opaLayout.mContext.getContentResolver(), "pixel_nav_animation", 0, -2) == 1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startDiamondAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            setDotsVisible();
            this.mCurrentAnimators.addAll((ArraySet<? extends Animator>) getDiamondAnimatorSet());
            this.mAnimationState = 1;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startRetractAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet<? extends Animator>) getRetractAnimatorSet());
            this.mAnimationState = 2;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startLineAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet<? extends Animator>) getLineAnimatorSet());
            this.mAnimationState = 3;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startCollapseAnimation() {
        if (allowAnimations()) {
            this.mCurrentAnimators.clear();
            this.mCurrentAnimators.addAll((ArraySet<? extends Animator>) getCollapseAnimatorSet());
            this.mAnimationState = 3;
            startAll(this.mCurrentAnimators);
            return;
        }
        skipToStartingValue();
    }

    private void startAll(ArraySet<Animator> arraySet) {
        showAllOpa();
        for (int size = arraySet.size() - 1; size >= 0; size--) {
            arraySet.valueAt(size).start();
        }
        for (int size2 = this.mAnimatedViews.size() - 1; size2 >= 0; size2--) {
            this.mAnimatedViews.get(size2).invalidate();
        }
    }

    private boolean allowAnimations() {
        return this.mAllowAnimation && isAttachedToWindow() && this.mWindowVisible;
    }

    private ArraySet<Animator> getDiamondAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        View view = this.mTop;
        Property<View, Float> property = View.Y;
        Resources resources = this.mResources;
        int i = R.dimen.opa_diamond_translation;
        arraySet.add(getPropertyAnimator(view, property, view.getY() + (-OpaUtils.getPxVal(resources, i)), 200, this.mDiamondInterpolator));
        View view2 = this.mTop;
        Property<View, Float> property2 = FrameLayout.SCALE_X;
        Interpolator interpolator = Interpolators.FAST_OUT_SLOW_IN;
        arraySet.add(getPropertyAnimator(view2, property2, 0.8f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mTop, FrameLayout.SCALE_Y, 0.8f, 200, interpolator));
        View view3 = this.mBottom;
        arraySet.add(getPropertyAnimator(view3, View.Y, view3.getY() + OpaUtils.getPxVal(this.mResources, i), 200, this.mDiamondInterpolator));
        arraySet.add(getPropertyAnimator(this.mBottom, FrameLayout.SCALE_X, 0.8f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mBottom, FrameLayout.SCALE_Y, 0.8f, 200, interpolator));
        View view4 = this.mLeft;
        arraySet.add(getPropertyAnimator(view4, View.X, view4.getX() + (-OpaUtils.getPxVal(this.mResources, i)), 200, this.mDiamondInterpolator));
        arraySet.add(getPropertyAnimator(this.mLeft, FrameLayout.SCALE_X, 0.8f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mLeft, FrameLayout.SCALE_Y, 0.8f, 200, interpolator));
        View view5 = this.mRight;
        arraySet.add(getPropertyAnimator(view5, View.X, view5.getX() + OpaUtils.getPxVal(this.mResources, i), 200, this.mDiamondInterpolator));
        arraySet.add(getPropertyAnimator(this.mRight, FrameLayout.SCALE_X, 0.8f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mRight, FrameLayout.SCALE_Y, 0.8f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mWhite, FrameLayout.SCALE_X, 0.625f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mWhite, FrameLayout.SCALE_Y, 0.625f, 200, interpolator));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() { // from class: com.google.android.systemui.assist.OpaLayout.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                Trace.beginSection("OpaLayout.start.diamond");
                Trace.endSection();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                Trace.beginSection("OpaLayout.cancel.diamond");
                Trace.endSection();
                OpaLayout.this.mCurrentAnimators.clear();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Trace.beginSection("OpaLayout.end.diamond");
                Trace.endSection();
                OpaLayout.this.startLineAnimation();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getRetractAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        View view = this.mRed;
        Property<View, Float> property = FrameLayout.TRANSLATION_X;
        Interpolator interpolator = OpaUtils.INTERPOLATOR_40_OUT;
        arraySet.add(getPropertyAnimator(view, property, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mRed, FrameLayout.TRANSLATION_Y, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mRed, FrameLayout.SCALE_X, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mRed, FrameLayout.SCALE_Y, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mBlue, FrameLayout.TRANSLATION_X, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mBlue, FrameLayout.TRANSLATION_Y, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mBlue, FrameLayout.SCALE_X, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mBlue, FrameLayout.SCALE_Y, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mGreen, FrameLayout.TRANSLATION_X, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mGreen, FrameLayout.TRANSLATION_Y, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mGreen, FrameLayout.SCALE_X, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mGreen, FrameLayout.SCALE_Y, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mYellow, FrameLayout.TRANSLATION_X, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mYellow, FrameLayout.TRANSLATION_Y, 0.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mYellow, FrameLayout.SCALE_X, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mYellow, FrameLayout.SCALE_Y, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mWhite, FrameLayout.SCALE_X, 1.0f, 190, interpolator));
        arraySet.add(getPropertyAnimator(this.mWhite, FrameLayout.SCALE_Y, 1.0f, 190, interpolator));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() { // from class: com.google.android.systemui.assist.OpaLayout.6
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                Trace.beginSection("OpaLayout.start.retract");
                Trace.endSection();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                Trace.beginSection("OpaLayout.cancel.retract");
                Trace.endSection();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Trace.beginSection("OpaLayout.end.retract");
                Trace.endSection();
                OpaLayout.this.mCurrentAnimators.clear();
                OpaLayout.this.skipToStartingValue();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getCollapseAnimatorSet() {
        Animator propertyAnimator;
        Animator propertyAnimator2;
        Animator propertyAnimator3;
        Animator propertyAnimator4;
        ArraySet<Animator> arraySet = new ArraySet<>();
        if (this.mIsVertical) {
            propertyAnimator = getPropertyAnimator(this.mRed, FrameLayout.TRANSLATION_Y, 0.0f, 133, OpaUtils.INTERPOLATOR_40_OUT);
        } else {
            propertyAnimator = getPropertyAnimator(this.mRed, FrameLayout.TRANSLATION_X, 0.0f, 133, OpaUtils.INTERPOLATOR_40_OUT);
        }
        arraySet.add(propertyAnimator);
        View view = this.mRed;
        Property<View, Float> property = FrameLayout.SCALE_X;
        Interpolator interpolator = OpaUtils.INTERPOLATOR_40_OUT;
        arraySet.add(getPropertyAnimator(view, property, 1.0f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mRed, FrameLayout.SCALE_Y, 1.0f, 200, interpolator));
        if (this.mIsVertical) {
            propertyAnimator2 = getPropertyAnimator(this.mBlue, FrameLayout.TRANSLATION_Y, 0.0f, 150, interpolator);
        } else {
            propertyAnimator2 = getPropertyAnimator(this.mBlue, FrameLayout.TRANSLATION_X, 0.0f, 150, interpolator);
        }
        arraySet.add(propertyAnimator2);
        arraySet.add(getPropertyAnimator(this.mBlue, FrameLayout.SCALE_X, 1.0f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mBlue, FrameLayout.SCALE_Y, 1.0f, 200, interpolator));
        if (this.mIsVertical) {
            propertyAnimator3 = getPropertyAnimator(this.mYellow, FrameLayout.TRANSLATION_Y, 0.0f, 133, interpolator);
        } else {
            propertyAnimator3 = getPropertyAnimator(this.mYellow, FrameLayout.TRANSLATION_X, 0.0f, 133, interpolator);
        }
        arraySet.add(propertyAnimator3);
        arraySet.add(getPropertyAnimator(this.mYellow, FrameLayout.SCALE_X, 1.0f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mYellow, FrameLayout.SCALE_Y, 1.0f, 200, interpolator));
        if (this.mIsVertical) {
            propertyAnimator4 = getPropertyAnimator(this.mGreen, FrameLayout.TRANSLATION_Y, 0.0f, 150, interpolator);
        } else {
            propertyAnimator4 = getPropertyAnimator(this.mGreen, FrameLayout.TRANSLATION_X, 0.0f, 150, interpolator);
        }
        arraySet.add(propertyAnimator4);
        arraySet.add(getPropertyAnimator(this.mGreen, FrameLayout.SCALE_X, 1.0f, 200, interpolator));
        arraySet.add(getPropertyAnimator(this.mGreen, FrameLayout.SCALE_Y, 1.0f, 200, interpolator));
        ImageView imageView = this.mWhite;
        Property<View, Float> property2 = FrameLayout.SCALE_X;
        Interpolator interpolator2 = Interpolators.FAST_OUT_SLOW_IN;
        Animator propertyAnimator5 = getPropertyAnimator(imageView, property2, 1.0f, 150, interpolator2);
        Animator propertyAnimator6 = getPropertyAnimator(this.mWhite, FrameLayout.SCALE_Y, 1.0f, 150, interpolator2);
        propertyAnimator5.setStartDelay(33L);
        propertyAnimator6.setStartDelay(33L);
        arraySet.add(propertyAnimator5);
        arraySet.add(propertyAnimator6);
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() { // from class: com.google.android.systemui.assist.OpaLayout.7
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                Trace.beginSection("OpaLayout.start.collapse");
                Trace.endSection();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                Trace.beginSection("OpaLayout.cancel.collapse");
                Trace.endSection();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Trace.beginSection("OpaLayout.end.collapse");
                Trace.endSection();
                OpaLayout.this.mCurrentAnimators.clear();
                OpaLayout.this.skipToStartingValue();
            }
        });
        return arraySet;
    }

    private ArraySet<Animator> getLineAnimatorSet() {
        ArraySet<Animator> arraySet = new ArraySet<>();
        if (this.mIsVertical) {
            View view = this.mRed;
            Property<View, Float> property = View.Y;
            float y = view.getY();
            Resources resources = this.mResources;
            int i = R.dimen.opa_line_x_trans_ry;
            float pxVal = y + OpaUtils.getPxVal(resources, i);
            Interpolator interpolator = Interpolators.FAST_OUT_SLOW_IN;
            arraySet.add(getPropertyAnimator(view, property, pxVal, 225, interpolator));
            View view2 = this.mRed;
            Property<View, Float> property2 = View.X;
            float x = view2.getX();
            Resources resources2 = this.mResources;
            int i2 = R.dimen.opa_line_y_translation;
            arraySet.add(getPropertyAnimator(view2, property2, x + OpaUtils.getPxVal(resources2, i2), 133, interpolator));
            View view3 = this.mBlue;
            Property<View, Float> property3 = View.Y;
            float y2 = view3.getY();
            Resources resources3 = this.mResources;
            int i3 = R.dimen.opa_line_x_trans_bg;
            arraySet.add(getPropertyAnimator(view3, property3, y2 + OpaUtils.getPxVal(resources3, i3), 225, interpolator));
            View view4 = this.mYellow;
            arraySet.add(getPropertyAnimator(view4, View.Y, view4.getY() + (-OpaUtils.getPxVal(this.mResources, i)), 225, interpolator));
            View view5 = this.mYellow;
            arraySet.add(getPropertyAnimator(view5, View.X, view5.getX() + (-OpaUtils.getPxVal(this.mResources, i2)), 133, interpolator));
            View view6 = this.mGreen;
            arraySet.add(getPropertyAnimator(view6, View.Y, view6.getY() + (-OpaUtils.getPxVal(this.mResources, i3)), 225, interpolator));
        } else {
            View view7 = this.mRed;
            Property<View, Float> property4 = View.X;
            float x2 = view7.getX();
            Resources resources4 = this.mResources;
            int i4 = R.dimen.opa_line_x_trans_ry;
            float f = x2 + (-OpaUtils.getPxVal(resources4, i4));
            Interpolator interpolator2 = Interpolators.FAST_OUT_SLOW_IN;
            arraySet.add(getPropertyAnimator(view7, property4, f, 225, interpolator2));
            View view8 = this.mRed;
            Property<View, Float> property5 = View.Y;
            float y3 = view8.getY();
            Resources resources5 = this.mResources;
            int i5 = R.dimen.opa_line_y_translation;
            arraySet.add(getPropertyAnimator(view8, property5, y3 + OpaUtils.getPxVal(resources5, i5), 133, interpolator2));
            View view9 = this.mBlue;
            Property<View, Float> property6 = View.X;
            float x3 = view9.getX();
            Resources resources6 = this.mResources;
            int i6 = R.dimen.opa_line_x_trans_bg;
            arraySet.add(getPropertyAnimator(view9, property6, x3 + (-OpaUtils.getPxVal(resources6, i6)), 225, interpolator2));
            View view10 = this.mYellow;
            arraySet.add(getPropertyAnimator(view10, View.X, view10.getX() + OpaUtils.getPxVal(this.mResources, i4), 225, interpolator2));
            View view11 = this.mYellow;
            arraySet.add(getPropertyAnimator(view11, View.Y, view11.getY() + (-OpaUtils.getPxVal(this.mResources, i5)), 133, interpolator2));
            View view12 = this.mGreen;
            arraySet.add(getPropertyAnimator(view12, View.X, view12.getX() + OpaUtils.getPxVal(this.mResources, i6), 225, interpolator2));
        }
        arraySet.add(getPropertyAnimator(this.mWhite, FrameLayout.SCALE_X, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        arraySet.add(getPropertyAnimator(this.mWhite, FrameLayout.SCALE_Y, 0.0f, 83, this.HOME_DISAPPEAR_INTERPOLATOR));
        getLongestAnim(arraySet).addListener(new AnimatorListenerAdapter() { // from class: com.google.android.systemui.assist.OpaLayout.8
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                Trace.beginSection("OpaLayout.start.line");
                Trace.endSection();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                Trace.beginSection("OpaLayout.cancel.line");
                Trace.endSection();
                OpaLayout.this.mCurrentAnimators.clear();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Trace.beginSection("OpaLayout.end.line");
                Trace.endSection();
                OpaLayout.this.startCollapseAnimation();
            }
        });
        return arraySet;
    }

    public boolean getOpaEnabled() {
        if (this.mOpaEnabledNeedsUpdate) {
            ((AssistManagerGoogle) Dependency.get(AssistManager.class)).dispatchOpaEnabledState();
            if (this.mOpaEnabledNeedsUpdate) {
                Log.w("OpaLayout", "mOpaEnabledNeedsUpdate not cleared by AssistManagerGoogle!");
            }
        }
        return this.mOpaEnabled;
    }

    public void setOpaEnabled(boolean z) {
        Log.i("OpaLayout", "Setting opa enabled to " + z);
        this.mOpaEnabled = z;
        this.mOpaEnabledNeedsUpdate = false;
        updateOpaLayout();
    }

    public void updateOpaLayout() {
        boolean zShouldShowSwipeUpUI = this.mOverviewProxyService.shouldShowSwipeUpUI();
        boolean z = false;
        boolean z2 = this.mOpaEnabled && !zShouldShowSwipeUpUI;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mWhite.getLayoutParams();
        if (!z2 && !zShouldShowSwipeUpUI) {
            z = true;
        }
        int i = z ? this.mHomeDiameter : -1;
        layoutParams.width = i;
        layoutParams.height = i;
        this.mWhite.setLayoutParams(layoutParams);
        this.mWhite.setScaleType(z ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelCurrentAnimation(String str) {
        Trace.beginSection("OpaLayout.cancelCurrentAnimation: reason=" + str);
        Trace.endSection();
        if (!this.mCurrentAnimators.isEmpty()) {
            for (int size = this.mCurrentAnimators.size() - 1; size >= 0; size--) {
                Animator animatorValueAt = this.mCurrentAnimators.valueAt(size);
                animatorValueAt.removeAllListeners();
                animatorValueAt.cancel();
            }
            this.mCurrentAnimators.clear();
            this.mAnimationState = 0;
        }
        AnimatorSet animatorSet = this.mGestureAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mGestureState = 0;
        }
    }

    private void endCurrentAnimation(String str) {
        Trace.beginSection("OpaLayout.endCurrentAnimation: reason=" + str);
        if (!this.mCurrentAnimators.isEmpty()) {
            for (int size = this.mCurrentAnimators.size() - 1; size >= 0; size--) {
                Animator animatorValueAt = this.mCurrentAnimators.valueAt(size);
                animatorValueAt.removeAllListeners();
                animatorValueAt.end();
            }
            this.mCurrentAnimators.clear();
        }
        this.mAnimationState = 0;
    }

    private Animator getLongestAnim(ArraySet<Animator> arraySet) {
        long totalDuration = Long.MIN_VALUE;
        Animator animator = null;
        for (int size = arraySet.size() - 1; size >= 0; size--) {
            Animator animatorValueAt = arraySet.valueAt(size);
            if (animatorValueAt.getTotalDuration() > totalDuration) {
                totalDuration = animatorValueAt.getTotalDuration();
                animator = animatorValueAt;
            }
        }
        return animator;
    }

    private void setDotsVisible() {
        int size = this.mAnimatedViews.size();
        for (int i = 0; i < size; i++) {
            this.mAnimatedViews.get(i).setAlpha(1.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void skipToStartingValue() {
        int size = this.mAnimatedViews.size();
        for (int i = 0; i < size; i++) {
            View view = this.mAnimatedViews.get(i);
            view.setScaleY(1.0f);
            view.setScaleX(1.0f);
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            view.setAlpha(0.0f);
        }
        this.mWhite.setAlpha(1.0f);
        this.mAnimationState = 0;
        this.mGestureState = 0;
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setVertical(boolean z) {
        AnimatorSet animatorSet;
        if (this.mIsVertical != z && (animatorSet = this.mGestureAnimatorSet) != null) {
            animatorSet.cancel();
            this.mGestureAnimatorSet = null;
            skipToStartingValue();
        }
        this.mIsVertical = z;
        this.mHome.setVertical(z);
        if (this.mIsVertical) {
            this.mTop = this.mGreen;
            this.mBottom = this.mBlue;
            this.mRight = this.mYellow;
            this.mLeft = this.mRed;
            return;
        }
        this.mTop = this.mRed;
        this.mBottom = this.mYellow;
        this.mLeft = this.mBlue;
        this.mRight = this.mGreen;
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setDarkIntensity(float f) {
        if (this.mWhite.getDrawable() instanceof KeyButtonDrawable) {
            ((KeyButtonDrawable) this.mWhite.getDrawable()).setDarkIntensity(f);
        }
        this.mWhite.invalidate();
        this.mHome.setDarkIntensity(f);
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setDelayTouchFeedback(boolean z) {
        this.mHome.setDelayTouchFeedback(z);
        this.mDelayTouchFeedback = z;
    }

    private Animator getPropertyAnimator(View view, Property<View, Float> property, float f, int i, Interpolator interpolator) {
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, property, f);
        objectAnimatorOfFloat.setDuration(i);
        objectAnimatorOfFloat.setInterpolator(interpolator);
        return objectAnimatorOfFloat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideAllOpa() {
        fadeOutButton(this.mBlue);
        fadeOutButton(this.mRed);
        fadeOutButton(this.mYellow);
        fadeOutButton(this.mGreen);
    }

    private void showAllOpa() {
        fadeInButton(this.mBlue);
        fadeInButton(this.mRed);
        fadeInButton(this.mYellow);
        fadeInButton(this.mGreen);
    }

    private void fadeInButton(final View view) {
        if (view == null) {
            return;
        }
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.ALPHA, 0.0f, 1.0f);
        objectAnimatorOfFloat.setDuration(50L);
        objectAnimatorOfFloat.start();
        objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.google.android.systemui.assist.OpaLayout.11
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(0);
            }
        });
    }

    private void fadeOutButton(final View view) {
        if (view == null) {
            return;
        }
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.ALPHA, 1.0f, 0.0f);
        objectAnimatorOfFloat.setDuration(250L);
        objectAnimatorOfFloat.start();
        objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.google.android.systemui.assist.OpaLayout.12
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(4);
            }
        });
    }
}
