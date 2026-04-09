package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Insets;
import android.graphics.Rect;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.WindowInsetsAnimationControlListener;
import android.view.WindowInsetsAnimationController;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.util.crdroid.Utils;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.InjectionInflationController;
import java.util.List;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class KeyguardSecurityContainer extends FrameLayout implements KeyguardSecurityView {
    private static final UiEventLogger sUiEventLogger = new UiEventLoggerImpl();
    private int mActivePointerId;
    private AlertDialog mAlertDialog;
    private KeyguardSecurityCallback mCallback;
    private KeyguardSecurityModel.SecurityMode mCurrentSecuritySelection;
    private KeyguardSecurityView mCurrentSecurityView;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private boolean mDisappearAnimRunning;
    private boolean mHasFod;
    private InjectionInflationController mInjectionInflationController;
    private boolean mIsDragging;
    private final KeyguardStateController mKeyguardStateController;
    private float mLastTouchY;
    private LockPatternUtils mLockPatternUtils;
    private final MetricsLogger mMetricsLogger;
    private KeyguardSecurityCallback mNullCallback;
    private AdminSecondaryLockScreenController mSecondaryLockScreenController;
    private SecurityCallback mSecurityCallback;
    private KeyguardSecurityModel mSecurityModel;
    KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private final SpringAnimation mSpringAnimation;
    private float mStartTouchY;
    private boolean mSwipeUpToRetry;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final VelocityTracker mVelocityTracker;
    private final ViewConfiguration mViewConfiguration;
    private final WindowInsetsAnimation.Callback mWindowInsetsAnimationCallback;

    public interface SecurityCallback {
        boolean dismiss(boolean z, int i, boolean z2, KeyguardSecurityModel.SecurityMode securityMode);

        void finish(boolean z, int i);

        void onCancelClicked();

        void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z);

        void reset();

        void userActivity();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public enum BouncerUiEvent implements UiEventLogger.UiEventEnum {
        UNKNOWN(0),
        BOUNCER_DISMISS_EXTENDED_ACCESS(413),
        BOUNCER_DISMISS_BIOMETRIC(414),
        BOUNCER_DISMISS_NONE_SECURITY(415),
        BOUNCER_DISMISS_PASSWORD(416),
        BOUNCER_DISMISS_SIM(417),
        BOUNCER_PASSWORD_SUCCESS(418),
        BOUNCER_PASSWORD_FAILURE(419);

        private final int mId;

        BouncerUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCurrentSecuritySelection = KeyguardSecurityModel.SecurityMode.Invalid;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mLastTouchY = -1.0f;
        this.mActivePointerId = -1;
        this.mStartTouchY = -1.0f;
        this.mWindowInsetsAnimationCallback = new WindowInsetsAnimation.Callback(0) { // from class: com.android.keyguard.KeyguardSecurityContainer.1
            private final Rect mInitialBounds = new Rect();
            private final Rect mFinalBounds = new Rect();

            @Override // android.view.WindowInsetsAnimation.Callback
            public void onPrepare(WindowInsetsAnimation windowInsetsAnimation) {
                KeyguardSecurityContainer.this.mSecurityViewFlipper.getBoundsOnScreen(this.mInitialBounds);
            }

            @Override // android.view.WindowInsetsAnimation.Callback
            public WindowInsetsAnimation.Bounds onStart(WindowInsetsAnimation windowInsetsAnimation, WindowInsetsAnimation.Bounds bounds) {
                KeyguardSecurityContainer.this.mSecurityViewFlipper.getBoundsOnScreen(this.mFinalBounds);
                return bounds;
            }

            @Override // android.view.WindowInsetsAnimation.Callback
            public WindowInsets onProgress(WindowInsets windowInsets, List<WindowInsetsAnimation> list) {
                if (KeyguardSecurityContainer.this.mDisappearAnimRunning) {
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY(this.mInitialBounds.bottom - this.mFinalBounds.bottom);
                } else {
                    int iLerp = 0;
                    for (WindowInsetsAnimation windowInsetsAnimation : list) {
                        if ((windowInsetsAnimation.getTypeMask() & WindowInsets.Type.ime()) != 0) {
                            iLerp += (int) MathUtils.lerp(this.mInitialBounds.bottom - this.mFinalBounds.bottom, 0.0f, windowInsetsAnimation.getInterpolatedFraction());
                        }
                    }
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY(iLerp);
                }
                return windowInsets;
            }

            @Override // android.view.WindowInsetsAnimation.Callback
            public void onEnd(WindowInsetsAnimation windowInsetsAnimation) {
                if (KeyguardSecurityContainer.this.mDisappearAnimRunning) {
                    return;
                }
                KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY(0.0f);
            }
        };
        this.mCallback = new AnonymousClass3();
        this.mNullCallback = new KeyguardSecurityCallback() { // from class: com.android.keyguard.KeyguardSecurityContainer.4
            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i2, KeyguardSecurityModel.SecurityMode securityMode) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i2, boolean z2, KeyguardSecurityModel.SecurityMode securityMode) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void onUserInput() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i2, boolean z, int i3) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
            }
        };
        this.mSecurityModel = (KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class);
        this.mLockPatternUtils = new LockPatternUtils(context);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mSpringAnimation = new SpringAnimation(this, DynamicAnimation.Y);
        this.mInjectionInflationController = new InjectionInflationController(SystemUIFactory.getInstance().getRootComponent());
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mSecondaryLockScreenController = new AdminSecondaryLockScreenController(context, this, keyguardUpdateMonitor, this.mCallback, new Handler(Looper.myLooper()));
        this.mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
        ((FrameLayout) this).mContext.getPackageManager();
        this.mHasFod = Utils.deviceHasFOD(context);
    }

    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.mSecurityCallback = securityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onResume(i);
        }
        this.mSecurityViewFlipper.setWindowInsetsAnimationCallback(this.mWindowInsetsAnimationCallback);
        updateBiometricRetry();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mAlertDialog = null;
        }
        this.mSecondaryLockScreenController.hide();
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onPause();
        }
        this.mSecurityViewFlipper.setWindowInsetsAnimationCallback(null);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onStartingToHide() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onStartingToHide();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:28:0x0049  */
    @Override // android.view.ViewGroup
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onInterceptTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            int r0 = r6.getActionMasked()
            r1 = 0
            if (r0 == 0) goto L4c
            r2 = 1
            if (r0 == r2) goto L49
            r3 = 2
            if (r0 == r3) goto L11
            r6 = 3
            if (r0 == r6) goto L49
            goto L61
        L11:
            boolean r0 = r5.mIsDragging
            if (r0 == 0) goto L16
            return r2
        L16:
            boolean r0 = r5.mSwipeUpToRetry
            if (r0 != 0) goto L1b
            return r1
        L1b:
            com.android.keyguard.KeyguardSecurityView r0 = r5.mCurrentSecurityView
            boolean r0 = r0.disallowInterceptTouch(r6)
            if (r0 == 0) goto L24
            return r1
        L24:
            int r0 = r5.mActivePointerId
            int r0 = r6.findPointerIndex(r0)
            android.view.ViewConfiguration r3 = r5.mViewConfiguration
            int r3 = r3.getScaledTouchSlop()
            float r3 = (float) r3
            r4 = 1082130432(0x40800000, float:4.0)
            float r3 = r3 * r4
            com.android.keyguard.KeyguardSecurityView r4 = r5.mCurrentSecurityView
            if (r4 == 0) goto L61
            r4 = -1
            if (r0 == r4) goto L61
            float r4 = r5.mStartTouchY
            float r6 = r6.getY(r0)
            float r4 = r4 - r6
            int r6 = (r4 > r3 ? 1 : (r4 == r3 ? 0 : -1))
            if (r6 <= 0) goto L61
            r5.mIsDragging = r2
            return r2
        L49:
            r5.mIsDragging = r1
            goto L61
        L4c:
            int r0 = r6.getActionIndex()
            float r2 = r6.getY(r0)
            r5.mStartTouchY = r2
            int r6 = r6.getPointerId(r0)
            r5.mActivePointerId = r6
            android.view.VelocityTracker r5 = r5.mVelocityTracker
            r5.clear()
        L61:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0055  */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onTouchEvent(android.view.MotionEvent r7) {
        /*
            r6 = this;
            int r0 = r7.getActionMasked()
            r1 = 0
            r2 = -1082130432(0xffffffffbf800000, float:-1.0)
            r3 = 1
            if (r0 == r3) goto L55
            r4 = 2
            if (r0 == r4) goto L30
            r4 = 3
            if (r0 == r4) goto L55
            r2 = 6
            if (r0 == r2) goto L14
            goto L65
        L14:
            int r2 = r7.getActionIndex()
            int r4 = r7.getPointerId(r2)
            int r5 = r6.mActivePointerId
            if (r4 != r5) goto L65
            if (r2 != 0) goto L23
            r1 = r3
        L23:
            float r2 = r7.getY(r1)
            r6.mLastTouchY = r2
            int r7 = r7.getPointerId(r1)
            r6.mActivePointerId = r7
            goto L65
        L30:
            android.view.VelocityTracker r1 = r6.mVelocityTracker
            r1.addMovement(r7)
            int r1 = r6.mActivePointerId
            int r1 = r7.findPointerIndex(r1)
            float r7 = r7.getY(r1)
            float r1 = r6.mLastTouchY
            int r2 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r2 == 0) goto L52
            float r1 = r7 - r1
            float r2 = r6.getTranslationY()
            r4 = 1048576000(0x3e800000, float:0.25)
            float r1 = r1 * r4
            float r2 = r2 + r1
            r6.setTranslationY(r2)
        L52:
            r6.mLastTouchY = r7
            goto L65
        L55:
            r7 = -1
            r6.mActivePointerId = r7
            r6.mLastTouchY = r2
            r6.mIsDragging = r1
            android.view.VelocityTracker r7 = r6.mVelocityTracker
            float r7 = r7.getYVelocity()
            r6.startSpringAnimation(r7)
        L65:
            if (r0 != r3) goto L94
            float r7 = r6.getTranslationY()
            float r7 = -r7
            r0 = 1092616192(0x41200000, float:10.0)
            android.content.res.Resources r1 = r6.getResources()
            android.util.DisplayMetrics r1 = r1.getDisplayMetrics()
            float r0 = android.util.TypedValue.applyDimension(r3, r0, r1)
            int r7 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r7 <= 0) goto L94
            com.android.keyguard.KeyguardUpdateMonitor r7 = r6.mUpdateMonitor
            boolean r7 = r7.isFaceDetectionRunning()
            if (r7 != 0) goto L94
            com.android.keyguard.KeyguardUpdateMonitor r7 = r6.mUpdateMonitor
            r7.requestFaceAuth()
            com.android.keyguard.KeyguardSecurityCallback r7 = r6.mCallback
            r7.userActivity()
            r7 = 0
            r6.showMessage(r7, r7)
        L94:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void startSpringAnimation(float f) {
        this.mSpringAnimation.setStartVelocity(f).animateToFinalPosition(0.0f);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).startAppearAnimation();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        this.mDisappearAnimRunning = true;
        if (this.mCurrentSecuritySelection == KeyguardSecurityModel.SecurityMode.Password) {
            this.mSecurityViewFlipper.getWindowInsetsController().controlWindowInsetsAnimation(WindowInsets.Type.ime(), 125L, Interpolators.LINEAR, null, new AnonymousClass2());
        }
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            return getSecurityView(securityMode).startDisappearAnimation(runnable);
        }
        return false;
    }

    /* renamed from: com.android.keyguard.KeyguardSecurityContainer$2, reason: invalid class name */
    class AnonymousClass2 implements WindowInsetsAnimationControlListener {
        @Override // android.view.WindowInsetsAnimationControlListener
        public void onCancelled(WindowInsetsAnimationController windowInsetsAnimationController) {
        }

        AnonymousClass2() {
        }

        @Override // android.view.WindowInsetsAnimationControlListener
        public void onReady(final WindowInsetsAnimationController windowInsetsAnimationController, int i) {
            final ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.keyguard.KeyguardSecurityContainer$2$$ExternalSyntheticLambda0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    KeyguardSecurityContainer.AnonymousClass2.lambda$onReady$0(windowInsetsAnimationController, valueAnimatorOfFloat, valueAnimator);
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.keyguard.KeyguardSecurityContainer.2.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    windowInsetsAnimationController.finish(false);
                }
            });
            valueAnimatorOfFloat.setDuration(125L);
            valueAnimatorOfFloat.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            valueAnimatorOfFloat.start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$onReady$0(WindowInsetsAnimationController windowInsetsAnimationController, ValueAnimator valueAnimator, ValueAnimator valueAnimator2) {
            if (windowInsetsAnimationController.isCancelled()) {
                return;
            }
            windowInsetsAnimationController.setInsetsAndAlpha(Insets.add(windowInsetsAnimationController.getShownStateInsets(), Insets.of(0, 0, 0, (int) (((-r0.bottom) / 4) * valueAnimator.getAnimatedFraction()))), ((Float) valueAnimator2.getAnimatedValue()).floatValue(), valueAnimator.getAnimatedFraction());
        }

        @Override // android.view.WindowInsetsAnimationControlListener
        public void onFinished(WindowInsetsAnimationController windowInsetsAnimationController) {
            KeyguardSecurityContainer.this.mDisappearAnimRunning = false;
        }
    }

    private void updateBiometricRetry() {
        KeyguardSecurityModel.SecurityMode securityMode = getSecurityMode();
        this.mSwipeUpToRetry = (!this.mKeyguardStateController.isFaceAuthEnabled() || securityMode == KeyguardSecurityModel.SecurityMode.SimPin || securityMode == KeyguardSecurityModel.SecurityMode.SimPuk || securityMode == KeyguardSecurityModel.SecurityMode.None) ? false : true;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return this.mSecurityViewFlipper.getTitle();
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected KeyguardSecurityView getSecurityView(KeyguardSecurityModel.SecurityMode securityMode) {
        KeyguardSecurityView keyguardSecurityView;
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        int childCount = this.mSecurityViewFlipper.getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                keyguardSecurityView = null;
                break;
            }
            if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                keyguardSecurityView = (KeyguardSecurityView) this.mSecurityViewFlipper.getChildAt(i);
                break;
            }
            i++;
        }
        int layoutIdFor = getLayoutIdFor(securityMode);
        if (keyguardSecurityView != null || layoutIdFor == 0) {
            return keyguardSecurityView;
        }
        View viewInflate = this.mInjectionInflationController.injectable(LayoutInflater.from(((FrameLayout) this).mContext)).inflate(layoutIdFor, (ViewGroup) this.mSecurityViewFlipper, false);
        this.mSecurityViewFlipper.addView(viewInflate);
        updateSecurityView(viewInflate);
        KeyguardSecurityView keyguardSecurityView2 = (KeyguardSecurityView) viewInflate;
        keyguardSecurityView2.reset();
        return keyguardSecurityView2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void updateSecurityView(View view) {
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView keyguardSecurityView = (KeyguardSecurityView) view;
            keyguardSecurityView.setKeyguardCallback(this.mCallback);
            keyguardSecurityView.setLockPatternUtils(this.mLockPatternUtils);
        } else {
            Log.w("KeyguardSecurityView", "View " + view + " is not a KeyguardSecurityView");
        }
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        KeyguardSecurityViewFlipper keyguardSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(R.id.view_flipper);
        this.mSecurityViewFlipper = keyguardSecurityViewFlipper;
        keyguardSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityModel.setLockPatternUtils(lockPatternUtils);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int systemWindowInsetBottom;
        if (ViewRootImpl.sNewInsetsMode == 2) {
            systemWindowInsetBottom = Integer.max(windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom, windowInsets.getInsets(WindowInsets.Type.ime()).bottom);
        } else {
            systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom();
        }
        if (this.mUpdateMonitor.isUnlockingWithBiometricsPossible(KeyguardUpdateMonitor.getCurrentUser())) {
            systemWindowInsetBottom = Integer.max(com.android.systemui.util.Utils.getFODHeight(((FrameLayout) this).mContext, false), systemWindowInsetBottom);
        }
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), systemWindowInsetBottom);
        return windowInsets.inset(0, 0, 0, systemWindowInsetBottom);
    }

    private void showDialog(String str, String str2) {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        AlertDialog alertDialogCreate = new AlertDialog.Builder(((FrameLayout) this).mContext).setTitle(str).setMessage(str2).setCancelable(false).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).create();
        this.mAlertDialog = alertDialogCreate;
        if (!(((FrameLayout) this).mContext instanceof Activity)) {
            alertDialogCreate.getWindow().setType(2009);
        }
        this.mAlertDialog.show();
    }

    private void showTimeoutDialog(int i, int i2) {
        int i3;
        int i4 = i2 / 1000;
        int i5 = AnonymousClass5.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[this.mSecurityModel.getSecurityMode(i).ordinal()];
        if (i5 == 1) {
            i3 = R.string.kg_too_many_failed_pattern_attempts_dialog_message;
        } else if (i5 == 2) {
            i3 = R.string.kg_too_many_failed_pin_attempts_dialog_message;
        } else {
            i3 = i5 != 3 ? 0 : R.string.kg_too_many_failed_password_attempts_dialog_message;
        }
        if (i3 != 0) {
            showDialog(null, ((FrameLayout) this).mContext.getString(i3, Integer.valueOf(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(i)), Integer.valueOf(i4)));
        }
    }

    /* renamed from: com.android.keyguard.KeyguardSecurityContainer$5, reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode;

        static {
            int[] iArr = new int[KeyguardSecurityModel.SecurityMode.values().length];
            $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = iArr;
            try {
                iArr[KeyguardSecurityModel.SecurityMode.Pattern.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.PIN.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.Password.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.Invalid.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.None.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.SimPin.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.SimPuk.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    private void showAlmostAtWipeDialog(int i, int i2, int i3) {
        String string;
        if (i3 == 1) {
            string = ((FrameLayout) this).mContext.getString(R.string.kg_failed_attempts_almost_at_wipe, Integer.valueOf(i), Integer.valueOf(i2));
        } else if (i3 != 2) {
            string = i3 != 3 ? null : ((FrameLayout) this).mContext.getString(R.string.kg_failed_attempts_almost_at_erase_user, Integer.valueOf(i), Integer.valueOf(i2));
        } else {
            string = ((FrameLayout) this).mContext.getString(R.string.kg_failed_attempts_almost_at_erase_profile, Integer.valueOf(i), Integer.valueOf(i2));
        }
        showDialog(null, string);
    }

    private void showWipeDialog(int i, int i2) {
        String string;
        if (i2 == 1) {
            string = ((FrameLayout) this).mContext.getString(R.string.kg_failed_attempts_now_wiping, Integer.valueOf(i));
        } else if (i2 != 2) {
            string = i2 != 3 ? null : ((FrameLayout) this).mContext.getString(R.string.kg_failed_attempts_now_erasing_user, Integer.valueOf(i));
        } else {
            string = ((FrameLayout) this).mContext.getString(R.string.kg_failed_attempts_now_erasing_profile, Integer.valueOf(i));
        }
        showDialog(null, string);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportFailedUnlockAttempt(int i, int i2) {
        int i3 = 1;
        int currentFailedPasswordAttempts = this.mLockPatternUtils.getCurrentFailedPasswordAttempts(i) + 1;
        DevicePolicyManager devicePolicyManager = this.mLockPatternUtils.getDevicePolicyManager();
        int maximumFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(null, i);
        int i4 = maximumFailedPasswordsForWipe > 0 ? maximumFailedPasswordsForWipe - currentFailedPasswordAttempts : Integer.MAX_VALUE;
        if (i4 < 5) {
            int profileWithMinimumFailedPasswordsForWipe = devicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(i);
            if (profileWithMinimumFailedPasswordsForWipe == i) {
                if (profileWithMinimumFailedPasswordsForWipe != 0) {
                    i3 = 3;
                }
            } else if (profileWithMinimumFailedPasswordsForWipe != -10000) {
                i3 = 2;
            }
            if (i4 > 0) {
                showAlmostAtWipeDialog(currentFailedPasswordAttempts, i4, i3);
            } else {
                Slog.i("KeyguardSecurityView", "Too many unlock attempts; user " + profileWithMinimumFailedPasswordsForWipe + " will be wiped!");
                showWipeDialog(currentFailedPasswordAttempts, i3);
            }
        }
        this.mLockPatternUtils.reportFailedPasswordAttempt(i);
        if (i2 > 0) {
            this.mLockPatternUtils.reportPasswordLockout(i2, i);
            showTimeoutDialog(i, i2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ KeyguardSecurityModel.SecurityMode lambda$showPrimarySecurityScreen$0() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    void showPrimarySecurityScreen(boolean z) {
        showSecurityScreen((KeyguardSecurityModel.SecurityMode) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.keyguard.KeyguardSecurityContainer$$ExternalSyntheticLambda0
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$showPrimarySecurityScreen$0();
            }
        }));
    }

    boolean showNextSecurityScreenOrFinish(boolean z, int i, boolean z2, KeyguardSecurityModel.SecurityMode securityMode) {
        BouncerUiEvent bouncerUiEvent;
        boolean z3;
        Intent secondaryLockscreenRequirement;
        if (securityMode != KeyguardSecurityModel.SecurityMode.Invalid && securityMode != getCurrentSecurityMode()) {
            Log.w("KeyguardSecurityView", "Attempted to invoke showNextSecurityScreenOrFinish with securityMode " + securityMode + ", but current mode is " + getCurrentSecurityMode());
            return false;
        }
        BouncerUiEvent bouncerUiEvent2 = BouncerUiEvent.UNKNOWN;
        int i2 = 2;
        boolean z4 = true;
        if (this.mUpdateMonitor.getUserHasTrust(i)) {
            bouncerUiEvent = BouncerUiEvent.BOUNCER_DISMISS_EXTENDED_ACCESS;
            z3 = false;
            i2 = 3;
        } else {
            if (this.mUpdateMonitor.getUserUnlockedWithBiometric(i)) {
                bouncerUiEvent = BouncerUiEvent.BOUNCER_DISMISS_BIOMETRIC;
            } else {
                KeyguardSecurityModel.SecurityMode securityMode2 = KeyguardSecurityModel.SecurityMode.None;
                KeyguardSecurityModel.SecurityMode securityMode3 = this.mCurrentSecuritySelection;
                if (securityMode2 == securityMode3) {
                    KeyguardSecurityModel.SecurityMode securityMode4 = this.mSecurityModel.getSecurityMode(i);
                    if (securityMode2 == securityMode4) {
                        bouncerUiEvent = BouncerUiEvent.BOUNCER_DISMISS_NONE_SECURITY;
                        i2 = 0;
                    } else {
                        showSecurityScreen(securityMode4);
                        bouncerUiEvent = bouncerUiEvent2;
                        z4 = false;
                        i2 = -1;
                    }
                } else if (z) {
                    int i3 = AnonymousClass5.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode3.ordinal()];
                    if (i3 == 1 || i3 == 2 || i3 == 3) {
                        bouncerUiEvent = BouncerUiEvent.BOUNCER_DISMISS_PASSWORD;
                        z3 = true;
                        i2 = 1;
                    } else {
                        if (i3 == 6 || i3 == 7) {
                            KeyguardSecurityModel.SecurityMode securityMode5 = this.mSecurityModel.getSecurityMode(i);
                            boolean z5 = this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) || !this.mDeviceProvisionedController.isUserSetup(i);
                            if (securityMode5 == securityMode2 && z5) {
                                i2 = 4;
                                bouncerUiEvent = BouncerUiEvent.BOUNCER_DISMISS_SIM;
                            } else {
                                showSecurityScreen(securityMode5);
                            }
                        } else {
                            Log.v("KeyguardSecurityView", "Bad security screen " + this.mCurrentSecuritySelection + ", fail safe");
                            showPrimarySecurityScreen(false);
                        }
                        bouncerUiEvent = bouncerUiEvent2;
                        z3 = false;
                        z4 = false;
                        i2 = -1;
                    }
                } else {
                    bouncerUiEvent = bouncerUiEvent2;
                    z3 = false;
                    z4 = false;
                    i2 = -1;
                }
            }
            z3 = false;
        }
        if (z4 && !z2 && (secondaryLockscreenRequirement = this.mUpdateMonitor.getSecondaryLockscreenRequirement(i)) != null) {
            this.mSecondaryLockScreenController.show(secondaryLockscreenRequirement);
            return false;
        }
        if (i2 != -1) {
            this.mMetricsLogger.write(new LogMaker(197).setType(5).setSubtype(i2));
        }
        if (bouncerUiEvent != bouncerUiEvent2) {
            sUiEventLogger.log(bouncerUiEvent);
        }
        if (z4) {
            this.mSecurityCallback.finish(z3, i);
        }
        return z4;
    }

    private void showSecurityScreen(KeyguardSecurityModel.SecurityMode securityMode) {
        KeyguardSecurityModel.SecurityMode securityMode2 = this.mCurrentSecuritySelection;
        if (securityMode == securityMode2) {
            return;
        }
        KeyguardSecurityView securityView = getSecurityView(securityMode2);
        KeyguardSecurityView securityView2 = getSecurityView(securityMode);
        if (securityView != null) {
            securityView.onPause();
            securityView.setKeyguardCallback(this.mNullCallback);
        }
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            securityView2.onResume(2);
            securityView2.setKeyguardCallback(this.mCallback);
        }
        int childCount = this.mSecurityViewFlipper.getChildCount();
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= childCount) {
                break;
            }
            if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                this.mSecurityViewFlipper.setDisplayedChild(i);
                break;
            }
            i++;
        }
        this.mCurrentSecuritySelection = securityMode;
        this.mCurrentSecurityView = securityView2;
        SecurityCallback securityCallback = this.mSecurityCallback;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None && securityView2.needsInput()) {
            z = true;
        }
        securityCallback.onSecurityModeChanged(securityMode, z);
    }

    /* renamed from: com.android.keyguard.KeyguardSecurityContainer$3, reason: invalid class name */
    class AnonymousClass3 implements KeyguardSecurityCallback {
        AnonymousClass3() {
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void userActivity() {
            if (KeyguardSecurityContainer.this.mSecurityCallback != null) {
                KeyguardSecurityContainer.this.mSecurityCallback.userActivity();
            }
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void onUserInput() {
            KeyguardSecurityContainer.this.mUpdateMonitor.cancelFaceAuth();
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void dismiss(boolean z, int i, KeyguardSecurityModel.SecurityMode securityMode) {
            dismiss(z, i, false, securityMode);
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void dismiss(boolean z, int i, boolean z2, KeyguardSecurityModel.SecurityMode securityMode) {
            KeyguardSecurityContainer.this.mSecurityCallback.dismiss(z, i, z2, securityMode);
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void reportUnlockAttempt(int i, boolean z, int i2) {
            if (z) {
                SysUiStatsLog.write(64, 2);
                KeyguardSecurityContainer.this.mLockPatternUtils.reportSuccessfulPasswordAttempt(i);
                ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.keyguard.KeyguardSecurityContainer$3$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() throws InterruptedException {
                        KeyguardSecurityContainer.AnonymousClass3.lambda$reportUnlockAttempt$0();
                    }
                });
            } else {
                SysUiStatsLog.write(64, 1);
                KeyguardSecurityContainer.this.reportFailedUnlockAttempt(i, i2);
            }
            KeyguardSecurityContainer.this.mMetricsLogger.write(new LogMaker(197).setType(z ? 10 : 11));
            KeyguardSecurityContainer.sUiEventLogger.log(z ? BouncerUiEvent.BOUNCER_PASSWORD_SUCCESS : BouncerUiEvent.BOUNCER_PASSWORD_FAILURE);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$reportUnlockAttempt$0() throws InterruptedException {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException unused) {
            }
            Runtime.getRuntime().gc();
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void reset() {
            KeyguardSecurityContainer.this.mSecurityCallback.reset();
        }

        @Override // com.android.keyguard.KeyguardSecurityCallback
        public void onCancelClicked() {
            KeyguardSecurityContainer.this.mSecurityCallback.onCancelClicked();
        }
    }

    private int getSecurityViewIdForMode(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass5.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return R.id.keyguard_pattern_view;
        }
        if (i == 2) {
            return R.id.keyguard_pin_view;
        }
        if (i == 3) {
            return R.id.keyguard_password_view;
        }
        if (i == 6) {
            return R.id.keyguard_sim_pin_view;
        }
        if (i != 7) {
            return 0;
        }
        return R.id.keyguard_sim_puk_view;
    }

    public int getLayoutIdFor(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass5.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return R.layout.keyguard_pattern_view;
        }
        if (i == 2) {
            return R.layout.keyguard_pin_view;
        }
        if (i == 3) {
            return R.layout.keyguard_password_view;
        }
        if (i == 6) {
            return R.layout.keyguard_sim_pin_view;
        }
        if (i != 7) {
            return 0;
        }
        return R.layout.keyguard_sim_puk_view;
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mCurrentSecuritySelection;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return this.mSecurityViewFlipper.needsInput();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mSecurityViewFlipper.setKeyguardCallback(keyguardSecurityCallback);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void reset() {
        this.mSecurityViewFlipper.reset();
        this.mDisappearAnimRunning = false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            if (i != 0) {
                Log.i("KeyguardSecurityView", "Strong auth required, reason: " + i);
            }
            getSecurityView(this.mCurrentSecuritySelection).showPromptReason(i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(CharSequence charSequence, ColorStateList colorStateList) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).showMessage(charSequence, colorStateList);
        }
    }
}
