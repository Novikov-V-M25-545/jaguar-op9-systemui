package com.android.keyguard;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.provider.Settings;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.clock.ClockManager;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.util.wakelock.KeepAwakeAnimationListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class KeyguardClockSwitch extends RelativeLayout {
    private ViewGroup mBigClockContainer;
    private final ClockVisibilityTransition mBoldClockTransition;
    private ClockManager.ClockChangedListener mClockChangedListener;
    private final ClockManager mClockManager;
    private ClockPlugin mClockPlugin;
    private final ClockVisibilityTransition mClockTransition;
    private TextClock mClockView;
    private TextClock mClockViewBold;
    private int[] mColorPalette;
    private final ColorExtractor.OnColorsChangedListener mColorsListener;
    private float mDarkAmount;
    private boolean mHasVisibleNotifications;
    private View mKeyguardStatusArea;
    private boolean mShowingHeader;
    private FrameLayout mSmallClockFrame;
    private final StatusBarStateController.StateListener mStateListener;
    private int mStatusBarState;
    private final StatusBarStateController mStatusBarStateController;
    private boolean mSupportsDarkText;
    private final SysuiColorExtractor mSysuiColorExtractor;
    private final Transition mTransition;

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(ColorExtractor colorExtractor, int i) {
        if ((i & 2) != 0) {
            updateColors();
            updateClockColor();
        }
    }

    public KeyguardClockSwitch(Context context, AttributeSet attributeSet, StatusBarStateController statusBarStateController, SysuiColorExtractor sysuiColorExtractor, ClockManager clockManager) {
        super(context, attributeSet);
        this.mStateListener = new StatusBarStateController.StateListener() { // from class: com.android.keyguard.KeyguardClockSwitch.1
            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                KeyguardClockSwitch.this.mStatusBarState = i;
                KeyguardClockSwitch.this.updateBigClockVisibility();
            }
        };
        this.mClockChangedListener = new ClockManager.ClockChangedListener() { // from class: com.android.keyguard.KeyguardClockSwitch$$ExternalSyntheticLambda1
            @Override // com.android.keyguard.clock.ClockManager.ClockChangedListener
            public final void onClockChanged(ClockPlugin clockPlugin) {
                this.f$0.setClockPlugin(clockPlugin);
            }
        };
        this.mColorsListener = new ColorExtractor.OnColorsChangedListener() { // from class: com.android.keyguard.KeyguardClockSwitch$$ExternalSyntheticLambda0
            public final void onColorsChanged(ColorExtractor colorExtractor, int i) {
                this.f$0.lambda$new$0(colorExtractor, i);
            }
        };
        this.mStatusBarStateController = statusBarStateController;
        this.mStatusBarState = statusBarStateController.getState();
        this.mSysuiColorExtractor = sysuiColorExtractor;
        this.mClockManager = clockManager;
        ClockVisibilityTransition cutoff = new ClockVisibilityTransition().setCutoff(0.3f);
        this.mClockTransition = cutoff;
        cutoff.addTarget(R.id.default_clock_view);
        ClockVisibilityTransition cutoff2 = new ClockVisibilityTransition().setCutoff(0.7f);
        this.mBoldClockTransition = cutoff2;
        cutoff2.addTarget(R.id.default_clock_view_bold);
        this.mTransition = new TransitionSet().setOrdering(0).addTransition(cutoff).addTransition(cutoff2).setDuration(275L).setInterpolator((TimeInterpolator) Interpolators.LINEAR_OUT_SLOW_IN);
    }

    public boolean hasCustomClock() {
        return this.mClockPlugin != null;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mClockView = (TextClock) findViewById(R.id.default_clock_view);
        this.mClockViewBold = (TextClock) findViewById(R.id.default_clock_view_bold);
        this.mSmallClockFrame = (FrameLayout) findViewById(R.id.clock_view);
        this.mKeyguardStatusArea = findViewById(R.id.keyguard_status_area);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mClockManager.addOnClockChangedListener(this.mClockChangedListener);
        this.mStatusBarStateController.addCallback(this.mStateListener);
        this.mSysuiColorExtractor.addOnColorsChangedListener(this.mColorsListener);
        updateColors();
        updateClockColor();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mClockManager.removeOnClockChangedListener(this.mClockChangedListener);
        this.mStatusBarStateController.removeCallback(this.mStateListener);
        this.mSysuiColorExtractor.removeOnColorsChangedListener(this.mColorsListener);
        setClockPlugin(null);
    }

    public void updateClockColor() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "lockscreen_clock_color", -1);
        TextClock textClock = this.mClockView;
        if (textClock != null) {
            textClock.setTextColor(i);
        }
        TextClock textClock2 = this.mClockViewBold;
        if (textClock2 != null) {
            textClock2.setTextColor(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setClockPlugin(ClockPlugin clockPlugin) {
        ViewGroup viewGroup;
        ClockPlugin clockPlugin2 = this.mClockPlugin;
        if (clockPlugin2 != null) {
            View view = clockPlugin2.getView();
            if (view != null) {
                ViewParent parent = view.getParent();
                FrameLayout frameLayout = this.mSmallClockFrame;
                if (parent == frameLayout) {
                    frameLayout.removeView(view);
                }
            }
            ViewGroup viewGroup2 = this.mBigClockContainer;
            if (viewGroup2 != null) {
                viewGroup2.removeAllViews();
                updateBigClockVisibility();
            }
            this.mClockPlugin.onDestroyView();
            this.mClockPlugin = null;
        }
        if (clockPlugin == null) {
            if (this.mShowingHeader) {
                this.mClockView.setVisibility(8);
                this.mClockViewBold.setVisibility(0);
            } else {
                this.mClockView.setVisibility(0);
                this.mClockViewBold.setVisibility(4);
            }
            this.mKeyguardStatusArea.setVisibility(0);
            return;
        }
        View view2 = clockPlugin.getView();
        if (view2 != null) {
            this.mSmallClockFrame.addView(view2, -1, new ViewGroup.LayoutParams(-1, -2));
            this.mClockView.setVisibility(8);
            this.mClockViewBold.setVisibility(8);
        }
        View bigClockView = clockPlugin.getBigClockView();
        if (bigClockView != null && (viewGroup = this.mBigClockContainer) != null) {
            viewGroup.addView(bigClockView);
            updateBigClockVisibility();
        }
        this.mKeyguardStatusArea.setVisibility(clockPlugin.shouldShowStatusArea() ? 0 : 8);
        this.mClockPlugin = clockPlugin;
        clockPlugin.setStyle(getPaint().getStyle());
        this.mClockPlugin.setTextColor(getCurrentTextColor());
        this.mClockPlugin.setDarkAmount(this.mDarkAmount);
        this.mClockPlugin.setHasVisibleNotifications(this.mHasVisibleNotifications);
        int[] iArr = this.mColorPalette;
        if (iArr != null) {
            this.mClockPlugin.setColorPalette(this.mSupportsDarkText, iArr);
        }
    }

    public void setBigClockContainer(ViewGroup viewGroup) {
        View bigClockView;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null && viewGroup != null && (bigClockView = clockPlugin.getBigClockView()) != null) {
            viewGroup.addView(bigClockView);
        }
        this.mBigClockContainer = viewGroup;
        updateBigClockVisibility();
    }

    public void setShowCurrentUserTime(boolean z) {
        this.mClockView.setShowCurrentUserTime(z);
        this.mClockViewBold.setShowCurrentUserTime(z);
    }

    public void setTextSize(int i, float f) {
        this.mClockView.setTextSize(i, f);
    }

    public void setFormat12Hour(CharSequence charSequence) {
        this.mClockView.setFormat12Hour(charSequence);
        this.mClockViewBold.setFormat12Hour(charSequence);
    }

    public void setFormat24Hour(CharSequence charSequence) {
        this.mClockView.setFormat24Hour(charSequence);
        this.mClockViewBold.setFormat24Hour(charSequence);
    }

    public void setDarkAmount(float f) {
        this.mDarkAmount = f;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setDarkAmount(f);
        }
        updateBigClockAlpha();
    }

    void setHasVisibleNotifications(boolean z) {
        ViewGroup viewGroup;
        if (z == this.mHasVisibleNotifications) {
            return;
        }
        this.mHasVisibleNotifications = z;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setHasVisibleNotifications(z);
        }
        if (this.mDarkAmount == 0.0f && (viewGroup = this.mBigClockContainer) != null) {
            TransitionManager.beginDelayedTransition(viewGroup, new Fade().setDuration(275L).addTarget(this.mBigClockContainer));
        }
        updateBigClockAlpha();
    }

    public Paint getPaint() {
        return this.mClockView.getPaint();
    }

    public int getCurrentTextColor() {
        return this.mClockView.getCurrentTextColor();
    }

    public float getTextSize() {
        return this.mClockView.getTextSize();
    }

    int getPreferredY(int i) {
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            return clockPlugin.getPreferredY(i);
        }
        return i / 2;
    }

    public void refresh() {
        this.mClockView.refreshTime();
        this.mClockViewBold.refreshTime();
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.onTimeTick();
        }
        if (Build.IS_DEBUGGABLE) {
            Log.d("KeyguardClockSwitch", "Updating clock: " + this.mClockView.getText().toString().replaceAll("[^\\x00-\\x7F]", ":"));
        }
    }

    public void onTimeZoneChanged(TimeZone timeZone) {
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.onTimeZoneChanged(timeZone);
        }
    }

    private void updateColors() {
        ColorExtractor.GradientColors colors = this.mSysuiColorExtractor.getColors(2);
        this.mSupportsDarkText = colors.supportsDarkText();
        int[] colorPalette = colors.getColorPalette();
        this.mColorPalette = colorPalette;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setColorPalette(this.mSupportsDarkText, colorPalette);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBigClockVisibility() {
        ViewGroup viewGroup = this.mBigClockContainer;
        if (viewGroup == null) {
            return;
        }
        int i = this.mStatusBarState;
        boolean z = true;
        if (i != 1 && i != 2) {
            z = false;
        }
        int i2 = (!z || viewGroup.getChildCount() == 0) ? 8 : 0;
        if (this.mBigClockContainer.getVisibility() != i2) {
            this.mBigClockContainer.setVisibility(i2);
        }
    }

    private void updateBigClockAlpha() {
        ViewGroup viewGroup = this.mBigClockContainer;
        if (viewGroup != null) {
            float f = this.mHasVisibleNotifications ? this.mDarkAmount : 1.0f;
            viewGroup.setAlpha(f);
            if (f == 0.0f) {
                this.mBigClockContainer.setVisibility(4);
            } else if (this.mBigClockContainer.getVisibility() == 4) {
                this.mBigClockContainer.setVisibility(0);
            }
        }
    }

    void setKeyguardShowingHeader(boolean z) throws Resources.NotFoundException {
        if (this.mShowingHeader == z) {
            return;
        }
        this.mShowingHeader = z;
        if (hasCustomClock()) {
            return;
        }
        float dimensionPixelSize = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.widget_small_font_size);
        float dimensionPixelSize2 = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.widget_big_font_size);
        this.mClockTransition.setScale(dimensionPixelSize / dimensionPixelSize2);
        this.mBoldClockTransition.setScale(dimensionPixelSize2 / dimensionPixelSize);
        TransitionManager.endTransitions((ViewGroup) this.mClockView.getParent());
        if (z) {
            this.mTransition.addListener(new TransitionListenerAdapter() { // from class: com.android.keyguard.KeyguardClockSwitch.2
                @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    if (KeyguardClockSwitch.this.mShowingHeader) {
                        KeyguardClockSwitch.this.mClockView.setVisibility(8);
                    }
                    transition.removeListener(this);
                }
            });
        }
        TransitionManager.beginDelayedTransition((ViewGroup) this.mClockView.getParent(), this.mTransition);
        this.mClockView.setVisibility(z ? 4 : 0);
        this.mClockViewBold.setVisibility(z ? 0 : 4);
        int dimensionPixelSize3 = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(z ? R.dimen.widget_vertical_padding_clock : R.dimen.title_clock_padding);
        TextClock textClock = this.mClockView;
        textClock.setPadding(textClock.getPaddingLeft(), this.mClockView.getPaddingTop(), this.mClockView.getPaddingRight(), dimensionPixelSize3);
        TextClock textClock2 = this.mClockViewBold;
        textClock2.setPadding(textClock2.getPaddingLeft(), this.mClockViewBold.getPaddingTop(), this.mClockViewBold.getPaddingRight(), dimensionPixelSize3);
    }

    ClockManager.ClockChangedListener getClockChangedListener() {
        return this.mClockChangedListener;
    }

    StatusBarStateController.StateListener getStateListener() {
        return this.mStateListener;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardClockSwitch:");
        printWriter.println("  mClockPlugin: " + this.mClockPlugin);
        printWriter.println("  mClockView: " + this.mClockView);
        printWriter.println("  mClockViewBold: " + this.mClockViewBold);
        printWriter.println("  mSmallClockFrame: " + this.mSmallClockFrame);
        printWriter.println("  mBigClockContainer: " + this.mBigClockContainer);
        printWriter.println("  mKeyguardStatusArea: " + this.mKeyguardStatusArea);
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mShowingHeader: " + this.mShowingHeader);
        printWriter.println("  mSupportsDarkText: " + this.mSupportsDarkText);
        printWriter.println("  mColorPalette: " + Arrays.toString(this.mColorPalette));
    }

    /* JADX INFO: Access modifiers changed from: private */
    class ClockVisibilityTransition extends Visibility {
        private float mCutoff;
        private float mScale;

        ClockVisibilityTransition() {
            setCutoff(1.0f);
            setScale(1.0f);
        }

        public ClockVisibilityTransition setCutoff(float f) {
            this.mCutoff = f;
            return this;
        }

        public ClockVisibilityTransition setScale(float f) {
            this.mScale = f;
            return this;
        }

        @Override // android.transition.Visibility, android.transition.Transition
        public void captureStartValues(TransitionValues transitionValues) {
            super.captureStartValues(transitionValues);
            captureVisibility(transitionValues);
        }

        @Override // android.transition.Visibility, android.transition.Transition
        public void captureEndValues(TransitionValues transitionValues) {
            super.captureStartValues(transitionValues);
            captureVisibility(transitionValues);
        }

        private void captureVisibility(TransitionValues transitionValues) {
            transitionValues.values.put("systemui:keyguard:visibility", Integer.valueOf(transitionValues.view.getVisibility()));
        }

        @Override // android.transition.Visibility
        public Animator onAppear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
            if (viewGroup.isShown()) {
                return createAnimator(view, this.mCutoff, 4, ((Integer) transitionValues2.values.get("systemui:keyguard:visibility")).intValue(), this.mScale, 1.0f);
            }
            return null;
        }

        @Override // android.transition.Visibility
        public Animator onDisappear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
            if (viewGroup.isShown()) {
                return createAnimator(view, 1.0f - this.mCutoff, 0, ((Integer) transitionValues2.values.get("systemui:keyguard:visibility")).intValue(), 1.0f, this.mScale);
            }
            return null;
        }

        private Animator createAnimator(final View view, final float f, final int i, final int i2, final float f2, final float f3) {
            view.setPivotY(view.getHeight() - view.getPaddingBottom());
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.keyguard.KeyguardClockSwitch$ClockVisibilityTransition$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    KeyguardClockSwitch.ClockVisibilityTransition.lambda$createAnimator$0(f, view, i2, f2, f3, valueAnimator);
                }
            });
            valueAnimatorOfFloat.addListener(new KeepAwakeAnimationListener(KeyguardClockSwitch.this.getContext()) { // from class: com.android.keyguard.KeyguardClockSwitch.ClockVisibilityTransition.1
                @Override // com.android.systemui.util.wakelock.KeepAwakeAnimationListener, android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    view.setVisibility(i);
                }

                @Override // com.android.systemui.util.wakelock.KeepAwakeAnimationListener, android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    animator.removeListener(this);
                }
            });
            addListener(new TransitionListenerAdapter() { // from class: com.android.keyguard.KeyguardClockSwitch.ClockVisibilityTransition.2
                @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                public void onTransitionEnd(Transition transition) {
                    view.setVisibility(i2);
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                    transition.removeListener(this);
                }
            });
            return valueAnimatorOfFloat;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$createAnimator$0(float f, View view, int i, float f2, float f3, ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();
            if (animatedFraction > f) {
                view.setVisibility(i);
            }
            float fLerp = MathUtils.lerp(f2, f3, animatedFraction);
            view.setScaleX(fLerp);
            view.setScaleY(fLerp);
        }
    }
}
