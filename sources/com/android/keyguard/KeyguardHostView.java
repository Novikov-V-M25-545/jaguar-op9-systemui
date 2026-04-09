package com.android.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.settingslib.Utils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import java.io.File;

/* loaded from: classes.dex */
public class KeyguardHostView extends FrameLayout implements KeyguardSecurityContainer.SecurityCallback {
    private AudioManager mAudioManager;
    private Runnable mCancelAction;
    private ActivityStarter.OnDismissAction mDismissAction;
    protected LockPatternUtils mLockPatternUtils;
    protected KeyguardSecurityContainer mSecurityContainer;
    private TelephonyManager mTelephonyManager;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    protected ViewMediatorCallback mViewMediatorCallback;

    public KeyguardHostView(Context context) {
        this(context, null);
    }

    public KeyguardHostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTelephonyManager = null;
        KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardHostView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                KeyguardHostView.this.getSecurityContainer().showPrimarySecurityScreen(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTrustGrantedWithFlags(int i, int i2) {
                if (i2 == KeyguardUpdateMonitor.getCurrentUser() && KeyguardHostView.this.isAttachedToWindow()) {
                    boolean zIsVisibleToUser = KeyguardHostView.this.isVisibleToUser();
                    boolean z = (i & 1) != 0;
                    boolean z2 = (i & 2) != 0;
                    if (z || z2) {
                        if (KeyguardHostView.this.mViewMediatorCallback.isScreenOn() && (zIsVisibleToUser || z2)) {
                            if (!zIsVisibleToUser) {
                                Log.i("KeyguardViewBase", "TrustAgent dismissed Keyguard.");
                            }
                            KeyguardHostView.this.dismiss(false, i2, false, KeyguardSecurityModel.SecurityMode.Invalid);
                            return;
                        }
                        KeyguardHostView.this.mViewMediatorCallback.playTrustedSound();
                    }
                }
            }
        };
        this.mUpdateCallback = keyguardUpdateMonitorCallback;
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(keyguardUpdateMonitorCallback);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.keyguardDoneDrawing();
        }
    }

    public void setOnDismissAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable) {
        Runnable runnable2 = this.mCancelAction;
        if (runnable2 != null) {
            runnable2.run();
            this.mCancelAction = null;
        }
        this.mDismissAction = onDismissAction;
        this.mCancelAction = runnable;
    }

    public boolean hasDismissActions() {
        return (this.mDismissAction == null && this.mCancelAction == null) ? false : true;
    }

    public void cancelDismissAction() {
        setOnDismissAction(null, null);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        this.mSecurityContainer = (KeyguardSecurityContainer) findViewById(R.id.keyguard_security_container);
        LockPatternUtils lockPatternUtils = new LockPatternUtils(((FrameLayout) this).mContext);
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
        this.mSecurityContainer.setSecurityCallback(this);
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPrimarySecurityScreen() {
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPromptReason(int i) {
        this.mSecurityContainer.showPromptReason(i);
    }

    public void showMessage(CharSequence charSequence, ColorStateList colorStateList) {
        this.mSecurityContainer.showMessage(charSequence, colorStateList);
    }

    public void showErrorMessage(CharSequence charSequence) {
        showMessage(charSequence, Utils.getColorError(((FrameLayout) this).mContext));
    }

    public boolean dismiss(int i) {
        return dismiss(false, i, false, getCurrentSecurityMode());
    }

    protected KeyguardSecurityContainer getSecurityContainer() {
        return this.mSecurityContainer;
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public boolean dismiss(boolean z, int i, boolean z2, KeyguardSecurityModel.SecurityMode securityMode) {
        return this.mSecurityContainer.showNextSecurityScreenOrFinish(z, i, z2, securityMode);
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void finish(boolean z, int i) {
        boolean zOnDismiss;
        ActivityStarter.OnDismissAction onDismissAction = this.mDismissAction;
        if (onDismissAction != null) {
            zOnDismiss = onDismissAction.onDismiss();
            this.mDismissAction = null;
            this.mCancelAction = null;
        } else {
            zOnDismiss = false;
        }
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            if (zOnDismiss) {
                viewMediatorCallback.keyguardDonePending(z, i);
            } else {
                viewMediatorCallback.keyguardDone(z, i);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void reset() {
        this.mViewMediatorCallback.resetKeyguard();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void onCancelClicked() {
        this.mViewMediatorCallback.onCancelClicked();
    }

    public void resetSecurityContainer() {
        this.mSecurityContainer.reset();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z) {
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.setNeedsInput(z);
        }
    }

    public CharSequence getAccessibilityTitleForCurrentMode() {
        return this.mSecurityContainer.getTitle();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void userActivity() {
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.userActivity();
        }
    }

    public void onPause() {
        this.mSecurityContainer.showPrimarySecurityScreen(true);
        this.mSecurityContainer.onPause();
        clearFocus();
    }

    public void onResume() {
        this.mSecurityContainer.onResume(1);
        requestFocus();
    }

    public void startAppearAnimation() {
        this.mSecurityContainer.startAppearAnimation();
    }

    public void startDisappearAnimation(Runnable runnable) {
        if (this.mSecurityContainer.startDisappearAnimation(runnable) || runnable == null) {
            return;
        }
        runnable.run();
    }

    public void cleanUp() {
        getSecurityContainer().onPause();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (interceptMediaKey(keyEvent)) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0023  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean interceptMediaKey(android.view.KeyEvent r9) {
        /*
            r8 = this;
            int r0 = r9.getKeyCode()
            int r1 = r9.getAction()
            r2 = 127(0x7f, float:1.78E-43)
            r3 = 126(0x7e, float:1.77E-43)
            r4 = 222(0xde, float:3.11E-43)
            r5 = 130(0x82, float:1.82E-43)
            r6 = 79
            r7 = 1
            if (r1 != 0) goto L44
            if (r0 == r6) goto L40
            if (r0 == r5) goto L40
            if (r0 == r4) goto L40
            if (r0 == r3) goto L23
            if (r0 == r2) goto L23
            switch(r0) {
                case 85: goto L23;
                case 86: goto L40;
                case 87: goto L40;
                case 88: goto L40;
                case 89: goto L40;
                case 90: goto L40;
                case 91: goto L40;
                default: goto L22;
            }
        L22:
            goto L5c
        L23:
            android.telephony.TelephonyManager r0 = r8.mTelephonyManager
            if (r0 != 0) goto L35
            android.content.Context r0 = r8.getContext()
            java.lang.String r1 = "phone"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.telephony.TelephonyManager r0 = (android.telephony.TelephonyManager) r0
            r8.mTelephonyManager = r0
        L35:
            android.telephony.TelephonyManager r0 = r8.mTelephonyManager
            if (r0 == 0) goto L40
            int r0 = r0.getCallState()
            if (r0 == 0) goto L40
            return r7
        L40:
            r8.handleMediaKeyEvent(r9)
            return r7
        L44:
            int r1 = r9.getAction()
            if (r1 != r7) goto L5c
            if (r0 == r6) goto L58
            if (r0 == r5) goto L58
            if (r0 == r4) goto L58
            if (r0 == r3) goto L58
            if (r0 == r2) goto L58
            switch(r0) {
                case 85: goto L58;
                case 86: goto L58;
                case 87: goto L58;
                case 88: goto L58;
                case 89: goto L58;
                case 90: goto L58;
                case 91: goto L58;
                default: goto L57;
            }
        L57:
            goto L5c
        L58:
            r8.handleMediaKeyEvent(r9)
            return r7
        L5c:
            r8 = 0
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardHostView.interceptMediaKey(android.view.KeyEvent):boolean");
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        synchronized (this) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            }
        }
        this.mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    public boolean shouldEnableMenuKey() {
        return !getResources().getBoolean(R.bool.config_disableMenuKeyInLockScreen) || ActivityManager.isRunningInTestHarness() || new File("/data/local/enable_menu_key").exists();
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        this.mViewMediatorCallback = viewMediatorCallback;
        viewMediatorCallback.setNeedsInput(this.mSecurityContainer.needsInput());
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityContainer.getSecurityMode();
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mSecurityContainer.getCurrentSecurityMode();
    }

    public void onStartingToHide() {
        this.mSecurityContainer.onStartingToHide();
    }
}
