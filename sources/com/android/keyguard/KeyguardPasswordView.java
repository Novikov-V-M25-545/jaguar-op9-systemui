package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.systemui.R;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class KeyguardPasswordView extends KeyguardAbsKeyInputView implements TextView.OnEditorActionListener, TextWatcher {
    private final int mDisappearYTranslation;
    private Interpolator mFastOutLinearInInterpolator;
    InputMethodManager mImm;
    private Interpolator mLinearOutSlowInInterpolator;
    private TextView mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryDisabler;
    private final boolean mShowImeAtScreenOn;
    private View mSwitchImeButton;
    private final boolean quickUnlock;
    private final int userId;

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return true;
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.quickUnlock = Settings.System.getIntForUser(getContext().getContentResolver(), "lockscreen_quick_unlock_control", 0, -2) == 1;
        this.userId = KeyguardUpdateMonitor.getCurrentUser();
        this.mShowImeAtScreenOn = context.getResources().getBoolean(R.bool.kg_show_ime_at_screen_on);
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R.dimen.disappear_y_translation);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, android.R.interpolator.linear_out_slow_in);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_linear_in);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetState() {
        this.mPasswordEntry.setTextOperationUser(UserHandle.of(KeyguardUpdateMonitor.getCurrentUser()));
        SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
        if (securityMessageDisplay != null) {
            securityMessageDisplay.setMessage("");
        }
        boolean zIsEnabled = this.mPasswordEntry.isEnabled();
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        if (this.mResumed && this.mPasswordEntry.isVisibleToUser() && zIsEnabled) {
            this.mImm.showSoftInput(this.mPasswordEntry, 1);
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPasswordTextViewId() {
        return R.id.passwordEntry;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(final int i) {
        super.onResume(i);
        post(new Runnable() { // from class: com.android.keyguard.KeyguardPasswordView.1
            @Override // java.lang.Runnable
            public void run() {
                if (KeyguardPasswordView.this.isShown() && KeyguardPasswordView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPasswordView.this.mPasswordEntry.requestFocus();
                    if (i != 1 || KeyguardPasswordView.this.mShowImeAtScreenOn) {
                        KeyguardPasswordView keyguardPasswordView = KeyguardPasswordView.this;
                        keyguardPasswordView.mImm.showSoftInput(keyguardPasswordView.mPasswordEntry, 1);
                    }
                }
            }
        });
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected int getPromptReasonStringRes(int i) {
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return R.string.kg_prompt_reason_restart_password;
        }
        if (i == 2) {
            return R.string.kg_prompt_reason_timeout_password;
        }
        if (i == 3) {
            return R.string.kg_prompt_reason_device_admin;
        }
        if (i == 4) {
            return R.string.kg_prompt_reason_user_request;
        }
        if (i == 6) {
            return R.string.kg_prompt_reason_timeout_password;
        }
        return R.string.kg_prompt_reason_timeout_password;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        super.onPause();
        this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onStartingToHide() {
        this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSwitchImeButton() {
        boolean z = this.mSwitchImeButton.getVisibility() == 0;
        boolean zHasMultipleEnabledIMEsOrSubtypes = hasMultipleEnabledIMEsOrSubtypes(this.mImm, false);
        if (z != zHasMultipleEnabledIMEsOrSubtypes) {
            this.mSwitchImeButton.setVisibility(zHasMultipleEnabledIMEsOrSubtypes ? 0 : 8);
        }
        if (this.mSwitchImeButton.getVisibility() != 0) {
            ViewGroup.LayoutParams layoutParams = this.mPasswordEntry.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(0);
                this.mPasswordEntry.setLayoutParams(layoutParams);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mImm = (InputMethodManager) getContext().getSystemService("input_method");
        TextView textView = (TextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntry = textView;
        textView.setTextOperationUser(UserHandle.of(KeyguardUpdateMonitor.getCurrentUser()));
        this.mPasswordEntryDisabler = new TextViewInputDisabler(this.mPasswordEntry);
        this.mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        this.mPasswordEntry.setInputType(129);
        this.mPasswordEntry.setOnEditorActionListener(this);
        this.mPasswordEntry.addTextChangedListener(this);
        this.mPasswordEntry.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
            }
        });
        this.mPasswordEntry.setSelected(true);
        View viewFindViewById = findViewById(R.id.switch_ime_button);
        this.mSwitchImeButton = viewFindViewById;
        viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
                KeyguardPasswordView keyguardPasswordView = KeyguardPasswordView.this;
                keyguardPasswordView.mImm.showInputMethodPickerFromSystem(false, keyguardPasswordView.getContext().getDisplayId());
            }
        });
        View viewFindViewById2 = findViewById(R.id.cancel_button);
        if (viewFindViewById2 != null) {
            viewFindViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$onFinishInflate$0(view);
                }
            });
        }
        updateSwitchImeButton();
        postDelayed(new Runnable() { // from class: com.android.keyguard.KeyguardPasswordView.4
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPasswordView.this.updateSwitchImeButton();
            }
        }, 500L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onFinishInflate$0(View view) {
        this.mCallback.reset();
        this.mCallback.onCancelClicked();
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.setText("");
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected LockscreenCredential getEnteredCredential() {
        return LockscreenCredential.createPasswordOrNone(this.mPasswordEntry.getText());
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    protected void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntryDisabler.setInputEnabled(z);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager inputMethodManager, boolean z) {
        int i = 0;
        for (InputMethodInfo inputMethodInfo : inputMethodManager.getEnabledInputMethodListAsUser(KeyguardUpdateMonitor.getCurrentUser())) {
            if (i > 1) {
                return true;
            }
            List<InputMethodSubtype> enabledInputMethodSubtypeList = inputMethodManager.getEnabledInputMethodSubtypeList(inputMethodInfo, true);
            if (!enabledInputMethodSubtypeList.isEmpty()) {
                Iterator<InputMethodSubtype> it = enabledInputMethodSubtypeList.iterator();
                int i2 = 0;
                while (it.hasNext()) {
                    if (it.next().isAuxiliary()) {
                        i2++;
                    }
                }
                if (enabledInputMethodSubtypeList.size() - i2 > 0 || (z && i2 > 1)) {
                }
            }
            i++;
        }
        return i > 1 || inputMethodManager.getEnabledInputMethodSubtypeList(null, false).size() > 1;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_password;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        setAlpha(0.0f);
        setTranslationY(0.0f);
        animate().alpha(1.0f).withLayer().setDuration(300L).setInterpolator(this.mLinearOutSlowInInterpolator);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        animate().alpha(0.0f).translationY(this.mDisappearYTranslation).setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100L).withEndAction(runnable);
        return true;
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.userActivity();
        }
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(editable)) {
            return;
        }
        onUserInput();
        if (this.quickUnlock) {
            LockscreenCredential enteredCredential = getEnteredCredential();
            if (enteredCredential.size() == keyguardPinPasswordLength()) {
                validateQuickUnlock(this.mLockPatternUtils, enteredCredential, this.userId);
            }
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        boolean z = keyEvent == null && (i == 0 || i == 6 || i == 5);
        boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
        if (!z && !z2) {
            return false;
        }
        verifyPasswordAndUnlock();
        return true;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(android.R.string.fingerprint_authenticated);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return KeyguardSecurityModel.SecurityMode.Password;
    }

    private AsyncTask<?, ?, ?> validateQuickUnlock(final LockPatternUtils lockPatternUtils, final LockscreenCredential lockscreenCredential, final int i) {
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() { // from class: com.android.keyguard.KeyguardPasswordView.5
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Boolean doInBackground(Void... voidArr) {
                try {
                    return Boolean.valueOf(lockPatternUtils.checkCredential(lockscreenCredential, i, (LockPatternUtils.CheckCredentialProgressCallback) null));
                } catch (LockPatternUtils.RequestThrottledException unused) {
                    return Boolean.FALSE;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Boolean bool) {
                KeyguardPasswordView.this.runQuickUnlock(bool);
            }
        };
        asyncTask.execute(new Void[0]);
        return asyncTask;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runQuickUnlock(Boolean bool) {
        if (bool.booleanValue()) {
            this.mCallback.reportUnlockAttempt(this.userId, true, 0);
            this.mCallback.dismiss(true, this.userId, getSecurityMode());
            resetPasswordText(true, true);
        }
    }

    private int keyguardPinPasswordLength() {
        int i;
        try {
            i = (int) this.mLockPatternUtils.getLockSettings().getLong("lockscreen.pin_password_length", 0L, this.userId);
        } catch (Exception unused) {
            i = -1;
        }
        if (i >= 4) {
            return i;
        }
        return -1;
    }
}
