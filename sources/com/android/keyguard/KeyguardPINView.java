package com.android.keyguard;

import android.R;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.PasswordTextView;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.Dependency;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lineageos.providers.LineageSettings;

/* loaded from: classes.dex */
public class KeyguardPINView extends KeyguardPinBasedInputView {
    private static List<Integer> sNumbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
    private final AppearAnimationUtils mAppearAnimationUtils;
    private ViewGroup mContainer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;
    private int mDisappearYTranslation;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    private View[][] mViews;
    private final int userId;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardPINView(Context context) {
        this(context, null);
    }

    public KeyguardPINView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.userId = KeyguardUpdateMonitor.getCurrentUser();
        this.mAppearAnimationUtils = new AppearAnimationUtils(context);
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125L, 0.6f, 0.45f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, R.interpolator.fast_out_linear_in));
        this.mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context, 187L, 0.6f, 0.45f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, R.interpolator.fast_out_linear_in));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(com.android.systemui.R.dimen.disappear_y_translation);
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetState() {
        super.resetState();
        SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
        if (securityMessageDisplay != null) {
            securityMessageDisplay.setMessage("");
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPasswordTextViewId() {
        return com.android.systemui.R.id.pinEntry;
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        int i = com.android.systemui.R.id.container;
        this.mContainer = (ViewGroup) findViewById(i);
        this.mRow0 = (ViewGroup) findViewById(com.android.systemui.R.id.row0);
        this.mRow1 = (ViewGroup) findViewById(com.android.systemui.R.id.row1);
        this.mRow2 = (ViewGroup) findViewById(com.android.systemui.R.id.row2);
        this.mRow3 = (ViewGroup) findViewById(com.android.systemui.R.id.row3);
        this.mViews = new View[][]{new View[]{this.mRow0, null, null}, new View[]{findViewById(com.android.systemui.R.id.key1), findViewById(com.android.systemui.R.id.key2), findViewById(com.android.systemui.R.id.key3)}, new View[]{findViewById(com.android.systemui.R.id.key4), findViewById(com.android.systemui.R.id.key5), findViewById(com.android.systemui.R.id.key6)}, new View[]{findViewById(com.android.systemui.R.id.key7), findViewById(com.android.systemui.R.id.key8), findViewById(com.android.systemui.R.id.key9)}, new View[]{findViewById(com.android.systemui.R.id.delete_button), findViewById(com.android.systemui.R.id.key0), findViewById(com.android.systemui.R.id.key_enter)}, new View[]{null, this.mEcaView, null}};
        View viewFindViewById = findViewById(com.android.systemui.R.id.cancel_button);
        if (viewFindViewById != null) {
            viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPINView$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$onFinishInflate$0(view);
                }
            });
        }
        if (LineageSettings.System.getIntForUser(((LinearLayout) this).mContext.getContentResolver(), "lockscreen_scramble_pin_layout", 0, -2) == 1) {
            Collections.shuffle(sNumbers);
            LinearLayout linearLayout = (LinearLayout) findViewById(i);
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < linearLayout.getChildCount(); i2++) {
                if (linearLayout.getChildAt(i2) instanceof LinearLayout) {
                    LinearLayout linearLayout2 = (LinearLayout) linearLayout.getChildAt(i2);
                    for (int i3 = 0; i3 < linearLayout2.getChildCount(); i3++) {
                        View childAt = linearLayout2.getChildAt(i3);
                        if (childAt.getClass() == NumPadKey.class) {
                            arrayList.add((NumPadKey) childAt);
                        }
                    }
                }
            }
            for (int i4 = 0; i4 < sNumbers.size(); i4++) {
                ((NumPadKey) arrayList.get(i4)).setDigit(sNumbers.get(i4).intValue());
            }
        }
        if (Settings.System.getIntForUser(getContext().getContentResolver(), "lockscreen_quick_unlock_control", 0, -2) == 1) {
            this.mPasswordEntry.setQuickUnlockListener(new PasswordTextView.QuickUnlockListener() { // from class: com.android.keyguard.KeyguardPINView.1
                @Override // com.android.keyguard.PasswordTextView.QuickUnlockListener
                public void onValidateQuickUnlock(String str) {
                    if (str == null || str.length() != KeyguardPINView.this.keyguardPinPasswordLength()) {
                        return;
                    }
                    KeyguardPINView keyguardPINView = KeyguardPINView.this;
                    keyguardPINView.validateQuickUnlock(keyguardPINView.mLockPatternUtils, str, keyguardPINView.userId);
                }
            });
        } else {
            this.mPasswordEntry.setQuickUnlockListener(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$0(View view) {
        this.mCallback.reset();
        this.mCallback.onCancelClicked();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        return com.android.systemui.R.string.kg_wrong_pin;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        enableClipping(false);
        setAlpha(1.0f);
        setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0L, 500L, 0.0f, this.mAppearAnimationUtils.getInterpolator());
        this.mAppearAnimationUtils.startAnimation2d(this.mViews, new Runnable() { // from class: com.android.keyguard.KeyguardPINView.2
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPINView.this.enableClipping(true);
            }
        });
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(final Runnable runnable) {
        DisappearAnimationUtils disappearAnimationUtils;
        enableClipping(false);
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0L, 280L, this.mDisappearYTranslation, this.mDisappearAnimationUtils.getInterpolator());
        if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            disappearAnimationUtils = this.mDisappearAnimationUtilsLocked;
        } else {
            disappearAnimationUtils = this.mDisappearAnimationUtils;
        }
        disappearAnimationUtils.startAnimation2d(this.mViews, new Runnable() { // from class: com.android.keyguard.KeyguardPINView.3
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPINView.this.enableClipping(true);
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void enableClipping(boolean z) {
        this.mContainer.setClipToPadding(z);
        this.mContainer.setClipChildren(z);
        this.mRow1.setClipToPadding(z);
        this.mRow2.setClipToPadding(z);
        this.mRow3.setClipToPadding(z);
        setClipChildren(z);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return KeyguardSecurityModel.SecurityMode.PIN;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AsyncTask<?, ?, ?> validateQuickUnlock(final LockPatternUtils lockPatternUtils, final String str, final int i) {
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() { // from class: com.android.keyguard.KeyguardPINView.4
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Boolean doInBackground(Void... voidArr) {
                try {
                    return Boolean.valueOf(lockPatternUtils.checkCredential(LockscreenCredential.createPinOrNone(str), i, (LockPatternUtils.CheckCredentialProgressCallback) null));
                } catch (LockPatternUtils.RequestThrottledException unused) {
                    return Boolean.FALSE;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Boolean bool) {
                KeyguardPINView.this.runQuickUnlock(bool);
            }
        };
        asyncTask.execute(new Void[0]);
        return asyncTask;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runQuickUnlock(Boolean bool) {
        if (bool.booleanValue()) {
            this.mPasswordEntry.setEnabled(false);
            this.mCallback.reportUnlockAttempt(this.userId, true, 0);
            this.mCallback.dismiss(true, this.userId, getSecurityMode());
            resetPasswordText(true, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int keyguardPinPasswordLength() {
        int i;
        try {
            i = (int) this.mLockPatternUtils.getLockSettings().getLong("lockscreen.pin_password_length", -1L, this.userId);
        } catch (Exception unused) {
            i = -1;
        }
        if (i >= 4) {
            return i;
        }
        return -1;
    }
}
