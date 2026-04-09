package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.util.function.TriConsumer;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.AlarmTimeout;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class ScrimController implements ViewTreeObserver.OnPreDrawListener, ColorExtractor.OnColorsChangedListener, Dumpable {
    private boolean mAnimateChange;
    private long mAnimationDelay;
    private Animator.AnimatorListener mAnimatorListener;
    private int mBehindTint;
    private boolean mBlankScreen;
    private Runnable mBlankingTransitionRunnable;
    private int mBubbleTint;
    private Callback mCallback;
    private final SysuiColorExtractor mColorExtractor;
    private ColorExtractor.GradientColors mColors;
    private boolean mDarkenWhileDragging;
    private final float mDefaultScrimAlpha;
    private final DockManager mDockManager;
    private final DozeParameters mDozeParameters;
    private final Handler mHandler;
    private int mInFrontTint;
    private boolean mKeyguardOccluded;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final KeyguardVisibilityCallback mKeyguardVisibilityCallback;
    private boolean mNeedsDrawableColorUpdate;
    private Runnable mPendingFrameCallback;
    private boolean mScreenBlankingCallbackCalled;
    private boolean mScreenOn;
    private ScrimView mScrimBehind;
    private ScrimView mScrimForBubble;
    private ScrimView mScrimInFront;
    private final TriConsumer<ScrimState, Float, ColorExtractor.GradientColors> mScrimStateListener;
    private Consumer<Integer> mScrimVisibleListener;
    private int mScrimsVisibility;
    private final AlarmTimeout mTimeTicker;
    private boolean mTracking;
    private boolean mUpdatePending;
    private final WakeLock mWakeLock;
    private boolean mWakeLockHeld;
    private boolean mWallpaperSupportsAmbientMode;
    private boolean mWallpaperVisibilityTimedOut;
    private static final boolean DEBUG = Log.isLoggable("ScrimController", 3);
    static final int TAG_KEY_ANIM = R.id.scrim;
    private static final int TAG_START_ALPHA = R.id.scrim_alpha_start;
    private static final int TAG_END_ALPHA = R.id.scrim_alpha_end;
    private ScrimState mState = ScrimState.UNINITIALIZED;
    private float mScrimBehindAlphaKeyguard = 0.2f;
    private float mExpansionFraction = 1.0f;
    private boolean mExpansionAffectsAlpha = true;
    private long mAnimationDuration = -1;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private float mInFrontAlpha = -1.0f;
    private float mBehindAlpha = -1.0f;
    private float mBubbleAlpha = -1.0f;

    public interface Callback {
        default void onCancelled() {
        }

        default void onDisplayBlanked() {
        }

        default void onFinished() {
        }

        default void onStart() {
        }
    }

    public void setCurrentUser(int i) {
    }

    public ScrimController(final LightBarController lightBarController, DozeParameters dozeParameters, AlarmManager alarmManager, final KeyguardStateController keyguardStateController, DelayedWakeLock.Builder builder, Handler handler, KeyguardUpdateMonitor keyguardUpdateMonitor, SysuiColorExtractor sysuiColorExtractor, DockManager dockManager, BlurUtils blurUtils) {
        Objects.requireNonNull(lightBarController);
        this.mScrimStateListener = new TriConsumer() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda2
            public final void accept(Object obj, Object obj2, Object obj3) {
                lightBarController.setScrimState((ScrimState) obj, ((Float) obj2).floatValue(), (ColorExtractor.GradientColors) obj3);
            }
        };
        this.mDefaultScrimAlpha = blurUtils.supportsBlursOnWindows() ? 0.54f : 0.85f;
        this.mKeyguardStateController = keyguardStateController;
        this.mDarkenWhileDragging = !keyguardStateController.canDismissLockScreen();
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mKeyguardVisibilityCallback = new KeyguardVisibilityCallback();
        this.mHandler = handler;
        this.mTimeTicker = new AlarmTimeout(alarmManager, new AlarmManager.OnAlarmListener() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda1
            @Override // android.app.AlarmManager.OnAlarmListener
            public final void onAlarm() {
                this.f$0.onHideWallpaperTimeout();
            }
        }, "hide_aod_wallpaper", handler);
        this.mWakeLock = builder.setHandler(handler).setTag("Scrims").build();
        this.mDozeParameters = dozeParameters;
        this.mDockManager = dockManager;
        keyguardStateController.addCallback(new KeyguardStateController.Callback() { // from class: com.android.systemui.statusbar.phone.ScrimController.1
            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardFadingAwayChanged() {
                ScrimController.this.setKeyguardFadingAway(keyguardStateController.isKeyguardFadingAway(), keyguardStateController.getKeyguardFadingAwayDuration());
            }
        });
        this.mColorExtractor = sysuiColorExtractor;
        sysuiColorExtractor.addOnColorsChangedListener(this);
        this.mColors = sysuiColorExtractor.getNeutralColors();
        this.mNeedsDrawableColorUpdate = true;
    }

    public void attachViews(ScrimView scrimView, ScrimView scrimView2, ScrimView scrimView3) {
        this.mScrimBehind = scrimView;
        this.mScrimInFront = scrimView2;
        this.mScrimForBubble = scrimView3;
        ScrimState[] scrimStateArrValues = ScrimState.values();
        for (int i = 0; i < scrimStateArrValues.length; i++) {
            scrimStateArrValues[i].init(this.mScrimInFront, this.mScrimBehind, this.mScrimForBubble, this.mDozeParameters, this.mDockManager);
            scrimStateArrValues[i].setScrimBehindAlphaKeyguard(this.mScrimBehindAlphaKeyguard);
            scrimStateArrValues[i].setDefaultScrimAlpha(this.mDefaultScrimAlpha);
        }
        this.mScrimBehind.setDefaultFocusHighlightEnabled(false);
        this.mScrimInFront.setDefaultFocusHighlightEnabled(false);
        this.mScrimForBubble.setDefaultFocusHighlightEnabled(false);
        updateScrims();
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardVisibilityCallback);
    }

    void setScrimVisibleListener(Consumer<Integer> consumer) {
        this.mScrimVisibleListener = consumer;
    }

    public void transitionTo(ScrimState scrimState) {
        transitionTo(scrimState, null);
    }

    public void transitionTo(ScrimState scrimState, Callback callback) {
        if (scrimState == this.mState) {
            if (callback == null || this.mCallback == callback) {
                return;
            }
            callback.onFinished();
            return;
        }
        if (DEBUG) {
            Log.d("ScrimController", "State changed to: " + scrimState);
        }
        if (scrimState == ScrimState.UNINITIALIZED) {
            throw new IllegalArgumentException("Cannot change to UNINITIALIZED.");
        }
        ScrimState scrimState2 = this.mState;
        this.mState = scrimState;
        Trace.traceCounter(4096L, "scrim_state", scrimState.ordinal());
        Callback callback2 = this.mCallback;
        if (callback2 != null) {
            callback2.onCancelled();
        }
        this.mCallback = callback;
        scrimState.prepare(scrimState2);
        this.mScreenBlankingCallbackCalled = false;
        this.mAnimationDelay = 0L;
        this.mBlankScreen = scrimState.getBlanksScreen();
        this.mAnimateChange = scrimState.getAnimateChange();
        this.mAnimationDuration = scrimState.getAnimationDuration();
        this.mInFrontTint = scrimState.getFrontTint();
        this.mBehindTint = scrimState.getBehindTint();
        this.mBubbleTint = scrimState.getBubbleTint();
        this.mInFrontAlpha = scrimState.getFrontAlpha();
        this.mBehindAlpha = scrimState.getBehindAlpha();
        this.mBubbleAlpha = scrimState.getBubbleAlpha();
        if (Float.isNaN(this.mBehindAlpha) || Float.isNaN(this.mInFrontAlpha)) {
            throw new IllegalStateException("Scrim opacity is NaN for state: " + scrimState + ", front: " + this.mInFrontAlpha + ", back: " + this.mBehindAlpha);
        }
        applyExpansionToAlpha();
        this.mScrimInFront.setFocusable(!scrimState.isLowPowerState());
        this.mScrimBehind.setFocusable(!scrimState.isLowPowerState());
        Runnable runnable = this.mPendingFrameCallback;
        if (runnable != null) {
            this.mScrimBehind.removeCallbacks(runnable);
            this.mPendingFrameCallback = null;
        }
        if (this.mHandler.hasCallbacks(this.mBlankingTransitionRunnable)) {
            this.mHandler.removeCallbacks(this.mBlankingTransitionRunnable);
            this.mBlankingTransitionRunnable = null;
        }
        this.mNeedsDrawableColorUpdate = scrimState != ScrimState.BRIGHTNESS_MIRROR;
        if (this.mState.isLowPowerState()) {
            holdWakeLock();
        }
        this.mWallpaperVisibilityTimedOut = false;
        if (shouldFadeAwayWallpaper()) {
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$transitionTo$0();
                }
            });
        } else {
            final AlarmTimeout alarmTimeout = this.mTimeTicker;
            Objects.requireNonNull(alarmTimeout);
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    alarmTimeout.cancel();
                }
            });
        }
        if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition() && this.mState == ScrimState.UNLOCKED) {
            this.mAnimationDelay = 10L;
            scheduleUpdate();
        } else if ((!this.mDozeParameters.getAlwaysOn() && scrimState2 == ScrimState.AOD) || (this.mState == ScrimState.AOD && !this.mDozeParameters.getDisplayNeedsBlanking())) {
            onPreDraw();
        } else {
            scheduleUpdate();
        }
        dispatchScrimState(this.mScrimBehind.getViewAlpha());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$transitionTo$0() {
        this.mTimeTicker.schedule(this.mDozeParameters.getWallpaperAodDuration(), 1);
    }

    private boolean shouldFadeAwayWallpaper() {
        return this.mWallpaperSupportsAmbientMode && this.mState == ScrimState.AOD && (this.mDozeParameters.getAlwaysOn() || this.mDockManager.isDocked());
    }

    public ScrimState getState() {
        return this.mState;
    }

    public void onTrackingStarted() {
        this.mTracking = true;
        this.mDarkenWhileDragging = true ^ this.mKeyguardStateController.canDismissLockScreen();
    }

    public void onExpandingFinished() {
        this.mTracking = false;
    }

    @VisibleForTesting
    protected void onHideWallpaperTimeout() {
        ScrimState scrimState = this.mState;
        if (scrimState == ScrimState.AOD || scrimState == ScrimState.PULSING) {
            holdWakeLock();
            this.mWallpaperVisibilityTimedOut = true;
            this.mAnimateChange = true;
            this.mAnimationDuration = this.mDozeParameters.getWallpaperFadeOutDuration();
            scheduleUpdate();
        }
    }

    private void holdWakeLock() {
        if (this.mWakeLockHeld) {
            return;
        }
        WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            this.mWakeLockHeld = true;
            wakeLock.acquire("ScrimController");
        } else {
            Log.w("ScrimController", "Cannot hold wake lock, it has not been set yet");
        }
    }

    public void setPanelExpansion(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("Fraction should not be NaN");
        }
        if (this.mExpansionFraction != f) {
            this.mExpansionFraction = f;
            ScrimState scrimState = this.mState;
            if ((scrimState == ScrimState.UNLOCKED || scrimState == ScrimState.KEYGUARD || scrimState == ScrimState.PULSING || scrimState == ScrimState.BUBBLE_EXPANDED) && this.mExpansionAffectsAlpha) {
                applyAndDispatchExpansion();
            }
        }
    }

    private void setOrAdaptCurrentAnimation(View view) {
        float currentScrimAlpha = getCurrentScrimAlpha(view);
        if (isAnimating(view)) {
            ValueAnimator valueAnimator = (ValueAnimator) view.getTag(TAG_KEY_ANIM);
            int i = TAG_END_ALPHA;
            float fFloatValue = ((Float) view.getTag(i)).floatValue();
            int i2 = TAG_START_ALPHA;
            view.setTag(i2, Float.valueOf(((Float) view.getTag(i2)).floatValue() + (currentScrimAlpha - fFloatValue)));
            view.setTag(i, Float.valueOf(currentScrimAlpha));
            valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            return;
        }
        updateScrimColor(view, currentScrimAlpha, getCurrentScrimTint(view));
    }

    private void applyExpansionToAlpha() {
        if (this.mExpansionAffectsAlpha) {
            ScrimState scrimState = this.mState;
            if (scrimState == ScrimState.UNLOCKED || scrimState == ScrimState.BUBBLE_EXPANDED) {
                this.mBehindAlpha = ((float) Math.pow(getInterpolatedFraction(), 0.800000011920929d)) * this.mDefaultScrimAlpha;
                this.mInFrontAlpha = 0.0f;
            } else if (scrimState == ScrimState.KEYGUARD || scrimState == ScrimState.PULSING) {
                float interpolatedFraction = getInterpolatedFraction();
                float behindAlpha = this.mState.getBehindAlpha();
                if (this.mDarkenWhileDragging) {
                    this.mBehindAlpha = MathUtils.lerp(this.mDefaultScrimAlpha, behindAlpha, interpolatedFraction);
                    this.mInFrontAlpha = this.mState.getFrontAlpha();
                } else {
                    this.mBehindAlpha = MathUtils.lerp(0.0f, behindAlpha, interpolatedFraction);
                    this.mInFrontAlpha = this.mState.getFrontAlpha();
                }
                this.mBehindTint = ColorUtils.blendARGB(ScrimState.BOUNCER.getBehindTint(), this.mState.getBehindTint(), interpolatedFraction);
            }
            if (Float.isNaN(this.mBehindAlpha) || Float.isNaN(this.mInFrontAlpha)) {
                throw new IllegalStateException("Scrim opacity is NaN for state: " + this.mState + ", front: " + this.mInFrontAlpha + ", back: " + this.mBehindAlpha);
            }
        }
    }

    private void applyAndDispatchExpansion() {
        applyExpansionToAlpha();
        if (this.mUpdatePending) {
            return;
        }
        setOrAdaptCurrentAnimation(this.mScrimBehind);
        setOrAdaptCurrentAnimation(this.mScrimInFront);
        setOrAdaptCurrentAnimation(this.mScrimForBubble);
        dispatchScrimState(this.mScrimBehind.getViewAlpha());
        if (this.mWallpaperVisibilityTimedOut) {
            this.mWallpaperVisibilityTimedOut = false;
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$applyAndDispatchExpansion$1();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$applyAndDispatchExpansion$1() {
        this.mTimeTicker.schedule(this.mDozeParameters.getWallpaperAodDuration(), 1);
    }

    public void setAodFrontScrimAlpha(float f) {
        if (this.mInFrontAlpha != f && shouldUpdateFrontScrimAlpha()) {
            this.mInFrontAlpha = f;
            updateScrims();
        }
        ScrimState.AOD.setAodFrontScrimAlpha(f);
        ScrimState.PULSING.setAodFrontScrimAlpha(f);
    }

    private boolean shouldUpdateFrontScrimAlpha() {
        return (this.mState == ScrimState.AOD && (this.mDozeParameters.getAlwaysOn() || this.mDockManager.isDocked())) || this.mState == ScrimState.PULSING;
    }

    public void setWakeLockScreenSensorActive(boolean z) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setWakeLockScreenSensorActive(z);
        }
        ScrimState scrimState2 = this.mState;
        if (scrimState2 == ScrimState.PULSING) {
            float behindAlpha = scrimState2.getBehindAlpha();
            if (this.mBehindAlpha != behindAlpha) {
                this.mBehindAlpha = behindAlpha;
                if (Float.isNaN(behindAlpha)) {
                    throw new IllegalStateException("Scrim opacity is NaN for state: " + this.mState + ", back: " + this.mBehindAlpha);
                }
                updateScrims();
            }
        }
    }

    protected void scheduleUpdate() {
        ScrimView scrimView;
        if (this.mUpdatePending || (scrimView = this.mScrimBehind) == null) {
            return;
        }
        scrimView.invalidate();
        this.mScrimBehind.getViewTreeObserver().addOnPreDrawListener(this);
        this.mUpdatePending = true;
    }

    protected void updateScrims() {
        if (this.mNeedsDrawableColorUpdate) {
            this.mNeedsDrawableColorUpdate = false;
            boolean z = (this.mScrimInFront.getViewAlpha() == 0.0f || this.mBlankScreen) ? false : true;
            boolean z2 = (this.mScrimBehind.getViewAlpha() == 0.0f || this.mBlankScreen) ? false : true;
            boolean z3 = (this.mScrimForBubble.getViewAlpha() == 0.0f || this.mBlankScreen) ? false : true;
            this.mScrimInFront.setColors(this.mColors, z);
            this.mScrimBehind.setColors(this.mColors, z2);
            this.mScrimForBubble.setColors(this.mColors, z3);
            ColorUtils.calculateMinimumBackgroundAlpha(this.mColors.supportsDarkText() ? -16777216 : -1, this.mColors.getMainColor(), 4.5f);
            dispatchScrimState(this.mScrimBehind.getViewAlpha());
        }
        ScrimState scrimState = this.mState;
        ScrimState scrimState2 = ScrimState.AOD;
        boolean z4 = (scrimState == scrimState2 || scrimState == ScrimState.PULSING) && this.mWallpaperVisibilityTimedOut;
        boolean z5 = (scrimState == ScrimState.PULSING || scrimState == scrimState2) && this.mKeyguardOccluded;
        if (z4 || z5) {
            this.mBehindAlpha = 1.0f;
        }
        setScrimAlpha(this.mScrimInFront, this.mInFrontAlpha);
        setScrimAlpha(this.mScrimBehind, this.mBehindAlpha);
        setScrimAlpha(this.mScrimForBubble, this.mBubbleAlpha);
        onFinished();
        dispatchScrimsVisible();
    }

    private void dispatchScrimState(float f) {
        this.mScrimStateListener.accept(this.mState, Float.valueOf(f), this.mScrimInFront.getColors());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchScrimsVisible() {
        int i = (this.mScrimInFront.getViewAlpha() == 1.0f || this.mScrimBehind.getViewAlpha() == 1.0f) ? 2 : (this.mScrimInFront.getViewAlpha() == 0.0f && this.mScrimBehind.getViewAlpha() == 0.0f) ? 0 : 1;
        if (this.mScrimsVisibility != i) {
            this.mScrimsVisibility = i;
            this.mScrimVisibleListener.accept(Integer.valueOf(i));
        }
    }

    private float getInterpolatedFraction() {
        if ((this.mExpansionFraction * 1.2f) - 0.2f <= 0.0f) {
            return 0.0f;
        }
        return (float) (1.0d - ((1.0d - Math.cos(Math.pow(1.0f - r8, 2.0d) * 3.141590118408203d)) * 0.5d));
    }

    private void setScrimAlpha(ScrimView scrimView, float f) {
        if (f == 0.0f) {
            scrimView.setClickable(false);
        } else {
            scrimView.setClickable(this.mState != ScrimState.AOD);
        }
        updateScrim(scrimView, f);
    }

    private String getScrimName(ScrimView scrimView) {
        return scrimView == this.mScrimInFront ? "front_scrim" : scrimView == this.mScrimBehind ? "back_scrim" : scrimView == this.mScrimForBubble ? "bubble_scrim" : "unknown_scrim";
    }

    private void updateScrimColor(View view, float f, int i) {
        float fMax = Math.max(0.0f, Math.min(1.0f, f));
        if (view instanceof ScrimView) {
            ScrimView scrimView = (ScrimView) view;
            Trace.traceCounter(4096L, getScrimName(scrimView) + "_alpha", (int) (255.0f * fMax));
            Trace.traceCounter(4096L, getScrimName(scrimView) + "_tint", Color.alpha(i));
            scrimView.setTint(i);
            scrimView.setViewAlpha(fMax);
        } else {
            view.setAlpha(fMax);
        }
        dispatchScrimsVisible();
    }

    private void startScrimAnimation(final View view, float f) {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        Animator.AnimatorListener animatorListener = this.mAnimatorListener;
        if (animatorListener != null) {
            valueAnimatorOfFloat.addListener(animatorListener);
        }
        final int tint = view instanceof ScrimView ? ((ScrimView) view).getTint() : 0;
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.lambda$startScrimAnimation$2(view, tint, valueAnimator);
            }
        });
        valueAnimatorOfFloat.setInterpolator(this.mInterpolator);
        valueAnimatorOfFloat.setStartDelay(this.mAnimationDelay);
        valueAnimatorOfFloat.setDuration(this.mAnimationDuration);
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.ScrimController.2
            private Callback lastCallback;

            {
                this.lastCallback = ScrimController.this.mCallback;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setTag(ScrimController.TAG_KEY_ANIM, null);
                ScrimController.this.onFinished(this.lastCallback);
                ScrimController.this.dispatchScrimsVisible();
            }
        });
        view.setTag(TAG_START_ALPHA, Float.valueOf(f));
        view.setTag(TAG_END_ALPHA, Float.valueOf(getCurrentScrimAlpha(view)));
        view.setTag(TAG_KEY_ANIM, valueAnimatorOfFloat);
        valueAnimatorOfFloat.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startScrimAnimation$2(View view, int i, ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) view.getTag(TAG_START_ALPHA)).floatValue();
        float fFloatValue2 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        updateScrimColor(view, MathUtils.constrain(MathUtils.lerp(fFloatValue, getCurrentScrimAlpha(view), fFloatValue2), 0.0f, 1.0f), ColorUtils.blendARGB(i, getCurrentScrimTint(view), fFloatValue2));
        dispatchScrimsVisible();
    }

    private float getCurrentScrimAlpha(View view) {
        if (view == this.mScrimInFront) {
            return this.mInFrontAlpha;
        }
        if (view == this.mScrimBehind) {
            return this.mBehindAlpha;
        }
        if (view == this.mScrimForBubble) {
            return this.mBubbleAlpha;
        }
        throw new IllegalArgumentException("Unknown scrim view");
    }

    private int getCurrentScrimTint(View view) {
        if (view == this.mScrimInFront) {
            return this.mInFrontTint;
        }
        if (view == this.mScrimBehind) {
            return this.mBehindTint;
        }
        if (view == this.mScrimForBubble) {
            return this.mBubbleTint;
        }
        throw new IllegalArgumentException("Unknown scrim view");
    }

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        this.mScrimBehind.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mUpdatePending = false;
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onStart();
        }
        updateScrims();
        return true;
    }

    private void onFinished() {
        onFinished(this.mCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinished(Callback callback) {
        if (this.mPendingFrameCallback != null) {
            return;
        }
        if (isAnimating(this.mScrimBehind) || isAnimating(this.mScrimInFront) || isAnimating(this.mScrimForBubble)) {
            if (callback == null || callback == this.mCallback) {
                return;
            }
            callback.onFinished();
            return;
        }
        if (this.mWakeLockHeld) {
            this.mWakeLock.release("ScrimController");
            this.mWakeLockHeld = false;
        }
        if (callback != null) {
            callback.onFinished();
            if (callback == this.mCallback) {
                this.mCallback = null;
            }
        }
        if (this.mState == ScrimState.UNLOCKED) {
            this.mInFrontTint = 0;
            this.mBehindTint = 0;
            this.mBubbleTint = 0;
            updateScrimColor(this.mScrimInFront, this.mInFrontAlpha, 0);
            updateScrimColor(this.mScrimBehind, this.mBehindAlpha, this.mBehindTint);
            updateScrimColor(this.mScrimForBubble, this.mBubbleAlpha, this.mBubbleTint);
        }
    }

    private boolean isAnimating(View view) {
        return view.getTag(TAG_KEY_ANIM) != null;
    }

    @VisibleForTesting
    void setAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.mAnimatorListener = animatorListener;
    }

    private void updateScrim(ScrimView scrimView, float f) {
        Callback callback;
        float viewAlpha = scrimView.getViewAlpha();
        ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(scrimView, TAG_KEY_ANIM);
        if (valueAnimator != null) {
            cancelAnimator(valueAnimator);
        }
        if (this.mPendingFrameCallback != null) {
            return;
        }
        if (this.mBlankScreen) {
            blankDisplay();
            return;
        }
        if (!this.mScreenBlankingCallbackCalled && (callback = this.mCallback) != null) {
            callback.onDisplayBlanked();
            this.mScreenBlankingCallbackCalled = true;
        }
        if (scrimView == this.mScrimBehind) {
            dispatchScrimState(f);
        }
        boolean z = f != viewAlpha;
        boolean z2 = scrimView.getTint() != getCurrentScrimTint(scrimView);
        if (z || z2) {
            if (this.mAnimateChange) {
                startScrimAnimation(scrimView, viewAlpha);
            } else {
                updateScrimColor(scrimView, f, getCurrentScrimTint(scrimView));
            }
        }
    }

    private void cancelAnimator(ValueAnimator valueAnimator) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private void blankDisplay() {
        updateScrimColor(this.mScrimInFront, 1.0f, -16777216);
        Runnable runnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$blankDisplay$4();
            }
        };
        this.mPendingFrameCallback = runnable;
        doOnTheNextFrame(runnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$blankDisplay$4() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onDisplayBlanked();
            this.mScreenBlankingCallbackCalled = true;
        }
        this.mBlankingTransitionRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.ScrimController$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$blankDisplay$3();
            }
        };
        int i = this.mScreenOn ? 32 : 500;
        if (DEBUG) {
            Log.d("ScrimController", "Fading out scrims with delay: " + i);
        }
        this.mHandler.postDelayed(this.mBlankingTransitionRunnable, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$blankDisplay$3() {
        this.mBlankingTransitionRunnable = null;
        this.mPendingFrameCallback = null;
        this.mBlankScreen = false;
        updateScrims();
    }

    @VisibleForTesting
    protected void doOnTheNextFrame(Runnable runnable) {
        this.mScrimBehind.postOnAnimationDelayed(runnable, 32L);
    }

    public void setScrimBehindChangeRunnable(Runnable runnable) {
        this.mScrimBehind.setChangeRunnable(runnable);
    }

    public void onColorsChanged(ColorExtractor colorExtractor, int i) {
        this.mColors = this.mColorExtractor.getNeutralColors();
        this.mNeedsDrawableColorUpdate = true;
        scheduleUpdate();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(" ScrimController: ");
        printWriter.print("  state: ");
        printWriter.println(this.mState);
        printWriter.print("  frontScrim:");
        printWriter.print(" viewAlpha=");
        printWriter.print(this.mScrimInFront.getViewAlpha());
        printWriter.print(" alpha=");
        printWriter.print(this.mInFrontAlpha);
        printWriter.print(" tint=0x");
        printWriter.println(Integer.toHexString(this.mScrimInFront.getTint()));
        printWriter.print("  backScrim:");
        printWriter.print(" viewAlpha=");
        printWriter.print(this.mScrimBehind.getViewAlpha());
        printWriter.print(" alpha=");
        printWriter.print(this.mBehindAlpha);
        printWriter.print(" tint=0x");
        printWriter.println(Integer.toHexString(this.mScrimBehind.getTint()));
        printWriter.print("  bubbleScrim:");
        printWriter.print(" viewAlpha=");
        printWriter.print(this.mScrimForBubble.getViewAlpha());
        printWriter.print(" alpha=");
        printWriter.print(this.mBubbleAlpha);
        printWriter.print(" tint=0x");
        printWriter.println(Integer.toHexString(this.mScrimForBubble.getTint()));
        printWriter.print("  mTracking=");
        printWriter.println(this.mTracking);
        printWriter.print("  mDefaultScrimAlpha=");
        printWriter.println(this.mDefaultScrimAlpha);
        printWriter.print("  mExpansionFraction=");
        printWriter.println(this.mExpansionFraction);
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        this.mWallpaperSupportsAmbientMode = z;
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setWallpaperSupportsAmbientMode(z);
        }
    }

    public void onScreenTurnedOn() {
        this.mScreenOn = true;
        if (this.mHandler.hasCallbacks(this.mBlankingTransitionRunnable)) {
            if (DEBUG) {
                Log.d("ScrimController", "Shorter blanking because screen turned on. All good.");
            }
            this.mHandler.removeCallbacks(this.mBlankingTransitionRunnable);
            this.mBlankingTransitionRunnable.run();
        }
    }

    public void onScreenTurnedOff() {
        this.mScreenOn = false;
    }

    public void setExpansionAffectsAlpha(boolean z) {
        this.mExpansionAffectsAlpha = z;
        if (z) {
            applyAndDispatchExpansion();
        }
    }

    public void setKeyguardOccluded(boolean z) {
        this.mKeyguardOccluded = z;
        updateScrims();
    }

    public void setHasBackdrop(boolean z) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setHasBackdrop(z);
        }
        ScrimState scrimState2 = this.mState;
        if (scrimState2 == ScrimState.AOD || scrimState2 == ScrimState.PULSING) {
            float behindAlpha = scrimState2.getBehindAlpha();
            if (!Float.isNaN(behindAlpha)) {
                if (this.mBehindAlpha != behindAlpha) {
                    this.mBehindAlpha = behindAlpha;
                    updateScrims();
                    return;
                }
                return;
            }
            throw new IllegalStateException("Scrim opacity is NaN for state: " + this.mState + ", back: " + this.mBehindAlpha);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setKeyguardFadingAway(boolean z, long j) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setKeyguardFadingAway(z, j);
        }
    }

    public void setLaunchingAffordanceWithPreview(boolean z) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setLaunchingAffordanceWithPreview(z);
        }
    }

    private class KeyguardVisibilityCallback extends KeyguardUpdateMonitorCallback {
        private KeyguardVisibilityCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            ScrimController.this.mNeedsDrawableColorUpdate = true;
            ScrimController.this.scheduleUpdate();
        }
    }
}
