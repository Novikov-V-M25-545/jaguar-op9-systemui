package com.android.systemui.biometrics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.biometrics.AuthBiometricView;
import com.android.systemui.biometrics.AuthCredentialView;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.SensorManagerPlugin;

/* loaded from: classes.dex */
public class AuthContainerView extends LinearLayout implements AuthDialog, WakefulnessLifecycle.Observer {

    @VisibleForTesting
    final ImageView mBackgroundView;

    @VisibleForTesting
    final BiometricCallback mBiometricCallback;

    @VisibleForTesting
    final ScrollView mBiometricScrollView;

    @VisibleForTesting
    AuthBiometricView mBiometricView;
    final Config mConfig;

    @VisibleForTesting
    int mContainerState;
    byte[] mCredentialAttestation;
    private final CredentialCallback mCredentialCallback;

    @VisibleForTesting
    AuthCredentialView mCredentialView;
    final int mEffectiveUserId;

    @VisibleForTesting
    final FrameLayout mFrameLayout;
    private final Handler mHandler;
    private final Injector mInjector;
    private final Interpolator mLinearOutSlowIn;
    private final AuthPanelController mPanelController;
    private final View mPanelView;
    Integer mPendingCallbackReason;
    private final float mTranslationY;

    @VisibleForTesting
    final WakefulnessLifecycle mWakefulnessLifecycle;
    private final WindowManager mWindowManager;
    private final IBinder mWindowToken;

    static class Config {
        Bundle mBiometricPromptBundle;
        AuthDialogCallback mCallback;
        Context mContext;
        int mModalityMask;
        String mOpPackageName;
        long mOperationId;
        boolean mRequireConfirmation;
        boolean mSkipIntro;
        int mSysUiSessionId;
        int mUserId;

        Config() {
        }
    }

    public static class Builder {
        Config mConfig;

        public Builder(Context context) {
            Config config = new Config();
            this.mConfig = config;
            config.mContext = context;
        }

        public Builder setCallback(AuthDialogCallback authDialogCallback) {
            this.mConfig.mCallback = authDialogCallback;
            return this;
        }

        public Builder setBiometricPromptBundle(Bundle bundle) {
            this.mConfig.mBiometricPromptBundle = bundle;
            return this;
        }

        public Builder setRequireConfirmation(boolean z) {
            this.mConfig.mRequireConfirmation = z;
            return this;
        }

        public Builder setUserId(int i) {
            this.mConfig.mUserId = i;
            return this;
        }

        public Builder setOpPackageName(String str) {
            this.mConfig.mOpPackageName = str;
            return this;
        }

        public Builder setSkipIntro(boolean z) {
            this.mConfig.mSkipIntro = z;
            return this;
        }

        public Builder setOperationId(long j) {
            this.mConfig.mOperationId = j;
            return this;
        }

        public Builder setSysUiSessionId(int i) {
            this.mConfig.mSysUiSessionId = i;
            return this;
        }

        public AuthContainerView build(int i) {
            this.mConfig.mModalityMask = i;
            return new AuthContainerView(this.mConfig, new Injector());
        }
    }

    public static class Injector {
        int getAnimateCredentialStartDelayMs() {
            return 300;
        }

        ScrollView getBiometricScrollView(FrameLayout frameLayout) {
            return (ScrollView) frameLayout.findViewById(R.id.biometric_scrollview);
        }

        FrameLayout inflateContainerView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
            return (FrameLayout) layoutInflater.inflate(R.layout.auth_container_view, viewGroup, false);
        }

        AuthPanelController getPanelController(Context context, View view) {
            return new AuthPanelController(context, view);
        }

        ImageView getBackgroundView(FrameLayout frameLayout) {
            return (ImageView) frameLayout.findViewById(R.id.background);
        }

        View getPanelView(FrameLayout frameLayout) {
            return frameLayout.findViewById(R.id.panel);
        }

        UserManager getUserManager(Context context) {
            return UserManager.get(context);
        }

        int getCredentialType(Context context, int i) {
            return Utils.getCredentialType(context, i);
        }
    }

    @VisibleForTesting
    final class BiometricCallback implements AuthBiometricView.Callback {
        BiometricCallback() {
        }

        @Override // com.android.systemui.biometrics.AuthBiometricView.Callback
        public void onAction(int i) {
            Log.d("BiometricPrompt/AuthContainerView", "onAction: " + i + ", sysUiSessionId: " + AuthContainerView.this.mConfig.mSysUiSessionId + ", state: " + AuthContainerView.this.mContainerState);
            switch (i) {
                case 1:
                    AuthContainerView.this.animateAway(4);
                    break;
                case 2:
                    AuthContainerView.this.sendEarlyUserCanceled();
                    AuthContainerView.this.animateAway(1);
                    break;
                case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                    AuthContainerView.this.animateAway(2);
                    break;
                case 4:
                    AuthContainerView.this.mConfig.mCallback.onTryAgainPressed();
                    break;
                case 5:
                    AuthContainerView.this.animateAway(5);
                    break;
                case 6:
                    AuthContainerView.this.mConfig.mCallback.onDeviceCredentialPressed();
                    AuthContainerView.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.biometrics.AuthContainerView$BiometricCallback$$ExternalSyntheticLambda0
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onAction$0();
                        }
                    }, AuthContainerView.this.mInjector.getAnimateCredentialStartDelayMs());
                    break;
                default:
                    Log.e("BiometricPrompt/AuthContainerView", "Unhandled action: " + i);
                    break;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onAction$0() {
            AuthContainerView.this.addCredentialView(false, true);
        }
    }

    final class CredentialCallback implements AuthCredentialView.Callback {
        CredentialCallback() {
        }

        @Override // com.android.systemui.biometrics.AuthCredentialView.Callback
        public void onCredentialMatched(byte[] bArr) {
            AuthContainerView authContainerView = AuthContainerView.this;
            authContainerView.mCredentialAttestation = bArr;
            authContainerView.animateAway(7);
        }
    }

    @VisibleForTesting
    AuthContainerView(Config config, Injector injector) {
        super(config.mContext);
        this.mWindowToken = new Binder();
        this.mContainerState = 0;
        this.mConfig = config;
        this.mInjector = injector;
        this.mEffectiveUserId = injector.getUserManager(((LinearLayout) this).mContext).getCredentialOwnerProfile(config.mUserId);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mWindowManager = (WindowManager) ((LinearLayout) this).mContext.getSystemService(WindowManager.class);
        this.mWakefulnessLifecycle = (WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class);
        this.mTranslationY = getResources().getDimension(R.dimen.biometric_dialog_animation_translation_offset);
        this.mLinearOutSlowIn = Interpolators.LINEAR_OUT_SLOW_IN;
        this.mBiometricCallback = new BiometricCallback();
        this.mCredentialCallback = new CredentialCallback();
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(((LinearLayout) this).mContext);
        FrameLayout frameLayoutInflateContainerView = injector.inflateContainerView(layoutInflaterFrom, this);
        this.mFrameLayout = frameLayoutInflateContainerView;
        View panelView = injector.getPanelView(frameLayoutInflateContainerView);
        this.mPanelView = panelView;
        this.mPanelController = injector.getPanelController(((LinearLayout) this).mContext, panelView);
        if (Utils.isBiometricAllowed(config.mBiometricPromptBundle)) {
            int i = config.mModalityMask;
            if (i == 2) {
                this.mBiometricView = (AuthBiometricFingerprintView) layoutInflaterFrom.inflate(R.layout.auth_biometric_fingerprint_view, (ViewGroup) null, false);
            } else if (i == 8) {
                this.mBiometricView = (AuthBiometricFaceView) layoutInflaterFrom.inflate(R.layout.auth_biometric_face_view, (ViewGroup) null, false);
            } else {
                Log.e("BiometricPrompt/AuthContainerView", "Unsupported biometric modality: " + config.mModalityMask);
                this.mBiometricView = null;
                this.mBackgroundView = null;
                this.mBiometricScrollView = null;
                return;
            }
        }
        ScrollView biometricScrollView = injector.getBiometricScrollView(frameLayoutInflateContainerView);
        this.mBiometricScrollView = biometricScrollView;
        this.mBackgroundView = injector.getBackgroundView(frameLayoutInflateContainerView);
        addView(frameLayoutInflateContainerView);
        biometricScrollView.setClipChildren(false);
        frameLayoutInflateContainerView.setClipChildren(false);
        setOnKeyListener(new View.OnKeyListener() { // from class: com.android.systemui.biometrics.AuthContainerView$$ExternalSyntheticLambda0
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view, int i2, KeyEvent keyEvent) {
                return this.f$0.lambda$new$0(view, i2, keyEvent);
            }
        });
        setImportantForAccessibility(2);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$new$0(View view, int i, KeyEvent keyEvent) {
        if (i != 4) {
            return false;
        }
        if (keyEvent.getAction() == 1) {
            sendEarlyUserCanceled();
            animateAway(1);
        }
        return true;
    }

    void sendEarlyUserCanceled() {
        this.mConfig.mCallback.onSystemEvent(1);
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public boolean isAllowDeviceCredentials() {
        return Utils.isDeviceCredentialAllowed(this.mConfig.mBiometricPromptBundle);
    }

    private void addBiometricView() {
        this.mBiometricView.setRequireConfirmation(this.mConfig.mRequireConfirmation);
        this.mBiometricView.setPanelController(this.mPanelController);
        this.mBiometricView.setBiometricPromptBundle(this.mConfig.mBiometricPromptBundle);
        this.mBiometricView.setCallback(this.mBiometricCallback);
        this.mBiometricView.setBackgroundView(this.mBackgroundView);
        this.mBiometricView.setUserId(this.mConfig.mUserId);
        this.mBiometricView.setEffectiveUserId(this.mEffectiveUserId);
        this.mBiometricScrollView.addView(this.mBiometricView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addCredentialView(boolean z, boolean z2) {
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(((LinearLayout) this).mContext);
        int credentialType = this.mInjector.getCredentialType(((LinearLayout) this).mContext, this.mEffectiveUserId);
        if (credentialType == 1) {
            this.mCredentialView = (AuthCredentialView) layoutInflaterFrom.inflate(R.layout.auth_credential_password_view, (ViewGroup) null, false);
        } else if (credentialType == 2) {
            this.mCredentialView = (AuthCredentialView) layoutInflaterFrom.inflate(R.layout.auth_credential_pattern_view, (ViewGroup) null, false);
        } else {
            if (credentialType != 3) {
                throw new IllegalStateException("Unknown credential type: " + credentialType);
            }
            this.mCredentialView = (AuthCredentialView) layoutInflaterFrom.inflate(R.layout.auth_credential_password_view, (ViewGroup) null, false);
        }
        this.mBackgroundView.setOnClickListener(null);
        this.mBackgroundView.setImportantForAccessibility(2);
        this.mCredentialView.setContainerView(this);
        this.mCredentialView.setUserId(this.mConfig.mUserId);
        this.mCredentialView.setOperationId(this.mConfig.mOperationId);
        this.mCredentialView.setEffectiveUserId(this.mEffectiveUserId);
        this.mCredentialView.setCredentialType(credentialType);
        this.mCredentialView.setCallback(this.mCredentialCallback);
        this.mCredentialView.setBiometricPromptBundle(this.mConfig.mBiometricPromptBundle);
        this.mCredentialView.setPanelController(this.mPanelController, z);
        this.mCredentialView.setShouldAnimateContents(z2);
        this.mFrameLayout.addView(this.mCredentialView);
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mPanelController.setContainerDimensions(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttachedToWindowInternal();
    }

    @VisibleForTesting
    void onAttachedToWindowInternal() {
        this.mWakefulnessLifecycle.addObserver(this);
        if (Utils.isBiometricAllowed(this.mConfig.mBiometricPromptBundle)) {
            addBiometricView();
        } else if (Utils.isDeviceCredentialAllowed(this.mConfig.mBiometricPromptBundle)) {
            addCredentialView(true, false);
        } else {
            throw new IllegalStateException("Unknown configuration: " + Utils.getAuthenticators(this.mConfig.mBiometricPromptBundle));
        }
        if (this.mConfig.mSkipIntro) {
            this.mContainerState = 3;
            return;
        }
        this.mContainerState = 1;
        this.mPanelView.setY(this.mTranslationY);
        this.mBiometricScrollView.setY(this.mTranslationY);
        setAlpha(0.0f);
        postOnAnimation(new Runnable() { // from class: com.android.systemui.biometrics.AuthContainerView$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onAttachedToWindowInternal$1();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onAttachedToWindowInternal$1() {
        this.mPanelView.animate().translationY(0.0f).setDuration(250L).setInterpolator(this.mLinearOutSlowIn).withLayer().withEndAction(new Runnable() { // from class: com.android.systemui.biometrics.AuthContainerView$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() throws Resources.NotFoundException {
                this.f$0.onDialogAnimatedIn();
            }
        }).start();
        this.mBiometricScrollView.animate().translationY(0.0f).setDuration(250L).setInterpolator(this.mLinearOutSlowIn).start();
        AuthCredentialView authCredentialView = this.mCredentialView;
        if (authCredentialView != null && authCredentialView.isAttachedToWindow()) {
            this.mCredentialView.setY(this.mTranslationY);
            this.mCredentialView.animate().translationY(0.0f).setDuration(250L).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
        }
        animate().alpha(1.0f).setDuration(250L).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWakefulnessLifecycle.removeObserver(this);
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedGoingToSleep() {
        animateAway(1);
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void show(WindowManager windowManager, Bundle bundle) {
        AuthBiometricView authBiometricView = this.mBiometricView;
        if (authBiometricView != null) {
            authBiometricView.restoreState(bundle);
        }
        windowManager.addView(this, getLayoutParams(this.mWindowToken));
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void dismissWithoutCallback(boolean z) {
        if (z) {
            animateAway(false, 0);
        } else {
            removeWindowIfAttached(false);
        }
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void dismissFromSystemServer() {
        removeWindowIfAttached(true);
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void onAuthenticationSucceeded() throws Resources.NotFoundException {
        this.mBiometricView.onAuthenticationSucceeded();
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void onAuthenticationFailed(String str) throws Resources.NotFoundException {
        this.mBiometricView.onAuthenticationFailed(str);
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void onHelp(String str) throws Resources.NotFoundException {
        this.mBiometricView.onHelp(str);
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void onError(String str) throws Resources.NotFoundException {
        this.mBiometricView.onError(str);
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void onSaveState(Bundle bundle) {
        bundle.putInt("container_state", this.mContainerState);
        bundle.putBoolean("biometric_showing", this.mBiometricView != null && this.mCredentialView == null);
        bundle.putBoolean("credential_showing", this.mCredentialView != null);
        AuthBiometricView authBiometricView = this.mBiometricView;
        if (authBiometricView != null) {
            authBiometricView.onSaveState(bundle);
        }
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public String getOpPackageName() {
        return this.mConfig.mOpPackageName;
    }

    @Override // com.android.systemui.biometrics.AuthDialog
    public void animateToCredentialUI() throws Resources.NotFoundException {
        this.mBiometricView.startTransitionToCredentialUI();
    }

    @VisibleForTesting
    void animateAway(int i) {
        animateAway(true, i);
    }

    private void animateAway(boolean z, int i) {
        int i2 = this.mContainerState;
        if (i2 == 1) {
            Log.w("BiometricPrompt/AuthContainerView", "startDismiss(): waiting for onDialogAnimatedIn");
            this.mContainerState = 2;
            return;
        }
        if (i2 == 4) {
            Log.w("BiometricPrompt/AuthContainerView", "Already dismissing, sendReason: " + z + " reason: " + i);
            return;
        }
        this.mContainerState = 4;
        if (z) {
            this.mPendingCallbackReason = Integer.valueOf(i);
        } else {
            this.mPendingCallbackReason = null;
        }
        final Runnable runnable = new Runnable() { // from class: com.android.systemui.biometrics.AuthContainerView$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateAway$2();
            }
        };
        postOnAnimation(new Runnable() { // from class: com.android.systemui.biometrics.AuthContainerView$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$animateAway$3(runnable);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateAway$2() {
        setVisibility(4);
        removeWindowIfAttached(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateAway$3(Runnable runnable) {
        this.mPanelView.animate().translationY(this.mTranslationY).setDuration(350L).setInterpolator(this.mLinearOutSlowIn).withLayer().withEndAction(runnable).start();
        this.mBiometricScrollView.animate().translationY(this.mTranslationY).setDuration(350L).setInterpolator(this.mLinearOutSlowIn).start();
        AuthCredentialView authCredentialView = this.mCredentialView;
        if (authCredentialView != null && authCredentialView.isAttachedToWindow()) {
            this.mCredentialView.animate().translationY(this.mTranslationY).setDuration(350L).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
        }
        animate().alpha(0.0f).setDuration(350L).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
    }

    private void sendPendingCallbackIfNotNull() {
        Log.d("BiometricPrompt/AuthContainerView", "pendingCallback: " + this.mPendingCallbackReason + " sysUISessionId: " + this.mConfig.mSysUiSessionId);
        Integer num = this.mPendingCallbackReason;
        if (num != null) {
            this.mConfig.mCallback.onDismissed(num.intValue(), this.mCredentialAttestation);
            this.mPendingCallbackReason = null;
        }
    }

    private void removeWindowIfAttached(boolean z) {
        if (z) {
            sendPendingCallbackIfNotNull();
        }
        if (this.mContainerState == 5) {
            Log.w("BiometricPrompt/AuthContainerView", "Container already STATE_GONE, mSysUiSessionId: " + this.mConfig.mSysUiSessionId);
            return;
        }
        Log.d("BiometricPrompt/AuthContainerView", "Removing container, mSysUiSessionId: " + this.mConfig.mSysUiSessionId);
        this.mContainerState = 5;
        this.mWindowManager.removeView(this);
    }

    @VisibleForTesting
    void onDialogAnimatedIn() throws Resources.NotFoundException {
        if (this.mContainerState == 2) {
            Log.d("BiometricPrompt/AuthContainerView", "onDialogAnimatedIn(): mPendingDismissDialog=true, dismissing now");
            animateAway(1);
            return;
        }
        this.mContainerState = 3;
        AuthBiometricView authBiometricView = this.mBiometricView;
        if (authBiometricView != null) {
            authBiometricView.onDialogAnimatedIn();
        }
    }

    public static WindowManager.LayoutParams getLayoutParams(IBinder iBinder) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2017, 16785408, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setFitInsetsTypes(layoutParams.getFitInsetsTypes() & (~WindowInsets.Type.ime()));
        layoutParams.setTitle("BiometricPrompt");
        layoutParams.token = iBinder;
        return layoutParams;
    }
}
