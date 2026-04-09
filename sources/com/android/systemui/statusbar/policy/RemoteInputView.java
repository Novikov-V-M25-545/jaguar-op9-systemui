package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.Editable;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.LightBarController;
import java.util.HashMap;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class RemoteInputView extends LinearLayout implements View.OnClickListener {
    public static final Object VIEW_TAG = new Object();
    private RemoteInputController mController;
    private RemoteEditText mEditText;
    private final TextView.OnEditorActionListener mEditorActionHandler;
    private NotificationEntry mEntry;
    private Consumer<Boolean> mOnVisibilityChangedListener;
    private PendingIntent mPendingIntent;
    private ProgressBar mProgressBar;
    private RemoteInput mRemoteInput;
    private RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler;
    private RemoteInput[] mRemoteInputs;
    private boolean mRemoved;
    private boolean mResetting;
    private int mRevealCx;
    private int mRevealCy;
    private int mRevealR;
    private ImageButton mSendButton;
    private IStatusBarService mStatusBarManagerService;
    private final SendButtonTextWatcher mTextWatcher;
    public final Object mToken;
    private NotificationViewWrapper mWrapper;

    public RemoteInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mToken = new Object();
        this.mTextWatcher = new SendButtonTextWatcher();
        this.mEditorActionHandler = new EditorActionHandler();
        this.mRemoteInputQuickSettingsDisabler = (RemoteInputQuickSettingsDisabler) Dependency.get(RemoteInputQuickSettingsDisabler.class);
        this.mStatusBarManagerService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(R.id.remote_input_progress);
        ImageButton imageButton = (ImageButton) findViewById(R.id.remote_input_send);
        this.mSendButton = imageButton;
        imageButton.setOnClickListener(this);
        RemoteEditText remoteEditText = (RemoteEditText) getChildAt(0);
        this.mEditText = remoteEditText;
        remoteEditText.setInnerFocusable(false);
    }

    protected Intent prepareRemoteInputFromText() {
        Bundle bundle = new Bundle();
        bundle.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent intentAddFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, intentAddFlags, bundle);
        this.mEntry.remoteInputText = this.mEditText.getText();
        NotificationEntry notificationEntry = this.mEntry;
        notificationEntry.remoteInputUri = null;
        notificationEntry.remoteInputMimeType = null;
        if (notificationEntry.editedSuggestionInfo == null) {
            RemoteInput.setResultsSource(intentAddFlags, 0);
        } else {
            RemoteInput.setResultsSource(intentAddFlags, 1);
        }
        return intentAddFlags;
    }

    protected Intent prepareRemoteInputFromData(String str, Uri uri) {
        HashMap map = new HashMap();
        map.put(str, uri);
        this.mController.grantInlineReplyUriPermission(this.mEntry.getSbn(), uri);
        Intent intentAddFlags = new Intent().addFlags(268435456);
        RemoteInput.addDataResultToIntent(this.mRemoteInput, intentAddFlags, map);
        this.mEntry.remoteInputText = ((LinearLayout) this).mContext.getString(R.string.remote_input_image_insertion_text);
        NotificationEntry notificationEntry = this.mEntry;
        notificationEntry.remoteInputMimeType = str;
        notificationEntry.remoteInputUri = uri;
        return intentAddFlags;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendRemoteInput(Intent intent) throws PendingIntent.CanceledException {
        this.mEditText.setEnabled(false);
        this.mSendButton.setVisibility(4);
        this.mProgressBar.setVisibility(0);
        this.mEntry.lastRemoteInputSent = SystemClock.elapsedRealtime();
        this.mController.addSpinning(this.mEntry.getKey(), this.mToken);
        this.mController.removeRemoteInput(this.mEntry, this.mToken);
        this.mEditText.mShowImeOnInputConnection = false;
        this.mController.remoteInputSent(this.mEntry);
        this.mEntry.setHasSentReply();
        ((ShortcutManager) getContext().getSystemService(ShortcutManager.class)).onApplicationActive(this.mEntry.getSbn().getPackageName(), this.mEntry.getSbn().getUser().getIdentifier());
        MetricsLogger.action(((LinearLayout) this).mContext, 398, this.mEntry.getSbn().getPackageName());
        try {
            this.mPendingIntent.send(((LinearLayout) this).mContext, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.i("RemoteInput", "Unable to send remote input result", e);
            MetricsLogger.action(((LinearLayout) this).mContext, 399, this.mEntry.getSbn().getPackageName());
        }
    }

    public CharSequence getText() {
        return this.mEditText.getText();
    }

    public static RemoteInputView inflate(Context context, ViewGroup viewGroup, NotificationEntry notificationEntry, RemoteInputController remoteInputController) {
        RemoteInputView remoteInputView = (RemoteInputView) LayoutInflater.from(context).inflate(R.layout.remote_input, viewGroup, false);
        remoteInputView.mController = remoteInputController;
        remoteInputView.mEntry = notificationEntry;
        UserHandle userHandleComputeTextOperationUser = computeTextOperationUser(notificationEntry.getSbn().getUser());
        RemoteEditText remoteEditText = remoteInputView.mEditText;
        remoteEditText.mUser = userHandleComputeTextOperationUser;
        remoteEditText.setTextOperationUser(userHandleComputeTextOperationUser);
        remoteInputView.setTag(VIEW_TAG);
        return remoteInputView;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) throws PendingIntent.CanceledException {
        if (view == this.mSendButton) {
            sendRemoteInput(prepareRemoteInputFromText());
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDefocus(boolean z) {
        int i;
        this.mController.removeRemoteInput(this.mEntry, this.mToken);
        this.mEntry.remoteInputText = this.mEditText.getText();
        if (!this.mRemoved) {
            if (z && (i = this.mRevealR) > 0) {
                Animator animatorCreateCircularReveal = ViewAnimationUtils.createCircularReveal(this, this.mRevealCx, this.mRevealCy, i, 0.0f);
                animatorCreateCircularReveal.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
                animatorCreateCircularReveal.setDuration(150L);
                animatorCreateCircularReveal.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.policy.RemoteInputView.1
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        RemoteInputView.this.setVisibility(4);
                        if (RemoteInputView.this.mWrapper != null) {
                            RemoteInputView.this.mWrapper.setRemoteInputVisible(false);
                        }
                    }
                });
                animatorCreateCircularReveal.start();
            } else {
                setVisibility(4);
                NotificationViewWrapper notificationViewWrapper = this.mWrapper;
                if (notificationViewWrapper != null) {
                    notificationViewWrapper.setRemoteInputVisible(false);
                }
            }
        }
        this.mRemoteInputQuickSettingsDisabler.setRemoteInputActive(false);
        MetricsLogger.action(((LinearLayout) this).mContext, 400, this.mEntry.getSbn().getPackageName());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mEditText.mRemoteInputView = this;
        this.mEditText.setOnEditorActionListener(this.mEditorActionHandler);
        this.mEditText.addTextChangedListener(this.mTextWatcher);
        if (this.mEntry.getRow().isChangingPosition() && getVisibility() == 0 && this.mEditText.isFocusable()) {
            this.mEditText.requestFocus();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mEditText.removeTextChangedListener(this.mTextWatcher);
        this.mEditText.setOnEditorActionListener(null);
        this.mEditText.mRemoteInputView = null;
        if (this.mEntry.getRow().isChangingPosition() || isTemporarilyDetached()) {
            return;
        }
        this.mController.removeRemoteInput(this.mEntry, this.mToken);
        this.mController.removeSpinning(this.mEntry.getKey(), this.mToken);
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.mPendingIntent = pendingIntent;
    }

    public void setRemoteInput(RemoteInput[] remoteInputArr, RemoteInput remoteInput, NotificationEntry.EditedSuggestionInfo editedSuggestionInfo) {
        this.mRemoteInputs = remoteInputArr;
        this.mRemoteInput = remoteInput;
        this.mEditText.setHint(remoteInput.getLabel());
        NotificationEntry notificationEntry = this.mEntry;
        notificationEntry.editedSuggestionInfo = editedSuggestionInfo;
        if (editedSuggestionInfo != null) {
            notificationEntry.remoteInputText = editedSuggestionInfo.originalText;
        }
    }

    public void focusAnimated() {
        if (getVisibility() != 0) {
            Animator animatorCreateCircularReveal = ViewAnimationUtils.createCircularReveal(this, this.mRevealCx, this.mRevealCy, 0.0f, this.mRevealR);
            animatorCreateCircularReveal.setDuration(360L);
            animatorCreateCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            animatorCreateCircularReveal.start();
        }
        focus();
    }

    private static UserHandle computeTextOperationUser(UserHandle userHandle) {
        return UserHandle.ALL.equals(userHandle) ? UserHandle.of(ActivityManager.getCurrentUser()) : userHandle;
    }

    public void focus() {
        MetricsLogger.action(((LinearLayout) this).mContext, 397, this.mEntry.getSbn().getPackageName());
        setVisibility(0);
        NotificationViewWrapper notificationViewWrapper = this.mWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setRemoteInputVisible(true);
        }
        this.mEditText.setInnerFocusable(true);
        RemoteEditText remoteEditText = this.mEditText;
        remoteEditText.mShowImeOnInputConnection = true;
        remoteEditText.setText(this.mEntry.remoteInputText);
        RemoteEditText remoteEditText2 = this.mEditText;
        remoteEditText2.setSelection(remoteEditText2.getText().length());
        this.mEditText.requestFocus();
        this.mController.addRemoteInput(this.mEntry, this.mToken);
        this.mRemoteInputQuickSettingsDisabler.setRemoteInputActive(true);
        updateSendButton();
    }

    public void onNotificationUpdateOrReset() {
        NotificationViewWrapper notificationViewWrapper;
        if (this.mProgressBar.getVisibility() == 0) {
            reset();
        }
        if (!isActive() || (notificationViewWrapper = this.mWrapper) == null) {
            return;
        }
        notificationViewWrapper.setRemoteInputVisible(true);
    }

    private void reset() {
        this.mResetting = true;
        this.mEntry.remoteInputTextWhenReset = SpannedString.valueOf(this.mEditText.getText());
        this.mEditText.getText().clear();
        this.mEditText.setEnabled(true);
        this.mSendButton.setVisibility(0);
        this.mProgressBar.setVisibility(4);
        this.mController.removeSpinning(this.mEntry.getKey(), this.mToken);
        updateSendButton();
        onDefocus(false);
        this.mResetting = false;
    }

    @Override // android.view.ViewGroup
    public boolean onRequestSendAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        if (this.mResetting && view == this.mEditText) {
            return false;
        }
        return super.onRequestSendAccessibilityEvent(view, accessibilityEvent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSendButton() {
        this.mSendButton.setEnabled(this.mEditText.getText().length() != 0);
    }

    public void close() {
        this.mEditText.defocusIfNeeded(false);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mController.requestDisallowLongPressAndDismiss();
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    public boolean requestScrollTo() {
        this.mController.lockScrollTo(this.mEntry);
        return true;
    }

    public boolean isActive() {
        return this.mEditText.isFocused() && this.mEditText.isEnabled();
    }

    public void stealFocusFrom(RemoteInputView remoteInputView) {
        remoteInputView.close();
        setPendingIntent(remoteInputView.mPendingIntent);
        setRemoteInput(remoteInputView.mRemoteInputs, remoteInputView.mRemoteInput, this.mEntry.editedSuggestionInfo);
        setRevealParameters(remoteInputView.mRevealCx, remoteInputView.mRevealCy, remoteInputView.mRevealR);
        focus();
    }

    public boolean updatePendingIntentFromActions(Notification.Action[] actionArr) {
        Intent intent;
        PendingIntent pendingIntent = this.mPendingIntent;
        if (pendingIntent == null || actionArr == null || (intent = pendingIntent.getIntent()) == null) {
            return false;
        }
        for (Notification.Action action : actionArr) {
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            PendingIntent pendingIntent2 = action.actionIntent;
            if (pendingIntent2 != null && remoteInputs != null && intent.filterEquals(pendingIntent2.getIntent())) {
                RemoteInput remoteInput = null;
                for (RemoteInput remoteInput2 : remoteInputs) {
                    if (remoteInput2.getAllowFreeFormInput()) {
                        remoteInput = remoteInput2;
                    }
                }
                if (remoteInput != null) {
                    setPendingIntent(action.actionIntent);
                    setRemoteInput(remoteInputs, remoteInput, null);
                    return true;
                }
            }
        }
        return false;
    }

    public PendingIntent getPendingIntent() {
        return this.mPendingIntent;
    }

    public void setRemoved() {
        this.mRemoved = true;
    }

    public void setRevealParameters(int i, int i2, int i3) {
        this.mRevealCx = i;
        this.mRevealCy = i2;
        this.mRevealR = i3;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        detachViewFromParent(this.mEditText);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchFinishTemporaryDetach() {
        if (isAttachedToWindow()) {
            RemoteEditText remoteEditText = this.mEditText;
            attachViewToParent(remoteEditText, 0, remoteEditText.getLayoutParams());
        } else {
            removeDetachedView(this.mEditText, false);
        }
        super.dispatchFinishTemporaryDetach();
    }

    public void setWrapper(NotificationViewWrapper notificationViewWrapper) {
        this.mWrapper = notificationViewWrapper;
    }

    public void setOnVisibilityChangedListener(Consumer<Boolean> consumer) {
        this.mOnVisibilityChangedListener = consumer;
    }

    @Override // android.view.View
    protected void onVisibilityChanged(View view, int i) {
        Consumer<Boolean> consumer;
        super.onVisibilityChanged(view, i);
        if (view != this || (consumer = this.mOnVisibilityChangedListener) == null) {
            return;
        }
        consumer.accept(Boolean.valueOf(i == 0));
    }

    public boolean isSending() {
        return getVisibility() == 0 && this.mController.isSpinning(this.mEntry.getKey(), this.mToken);
    }

    private class EditorActionHandler implements TextView.OnEditorActionListener {
        private EditorActionHandler() {
        }

        @Override // android.widget.TextView.OnEditorActionListener
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) throws PendingIntent.CanceledException {
            boolean z = keyEvent == null && (i == 6 || i == 5 || i == 4);
            boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
            if (!z && !z2) {
                return false;
            }
            if (RemoteInputView.this.mEditText.length() > 0) {
                RemoteInputView remoteInputView = RemoteInputView.this;
                remoteInputView.sendRemoteInput(remoteInputView.prepareRemoteInputFromText());
            }
            return true;
        }
    }

    private class SendButtonTextWatcher implements TextWatcher {
        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        private SendButtonTextWatcher() {
        }

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable editable) {
            RemoteInputView.this.updateSendButton();
        }
    }

    public static class RemoteEditText extends EditText {
        private final Drawable mBackground;
        private LightBarController mLightBarController;
        private RemoteInputView mRemoteInputView;
        boolean mShowImeOnInputConnection;
        UserHandle mUser;

        public RemoteEditText(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mBackground = getBackground();
            this.mLightBarController = (LightBarController) Dependency.get(LightBarController.class);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void defocusIfNeeded(boolean z) {
            RemoteInputView remoteInputView;
            RemoteInputView remoteInputView2 = this.mRemoteInputView;
            if ((remoteInputView2 != null && remoteInputView2.mEntry.getRow().isChangingPosition()) || isTemporarilyDetached()) {
                if (!isTemporarilyDetached() || (remoteInputView = this.mRemoteInputView) == null) {
                    return;
                }
                remoteInputView.mEntry.remoteInputText = getText();
                return;
            }
            if (isFocusable() && isEnabled()) {
                setInnerFocusable(false);
                RemoteInputView remoteInputView3 = this.mRemoteInputView;
                if (remoteInputView3 != null) {
                    remoteInputView3.onDefocus(z);
                }
                this.mShowImeOnInputConnection = false;
            }
        }

        @Override // android.widget.TextView, android.view.View
        protected void onVisibilityChanged(View view, int i) {
            super.onVisibilityChanged(view, i);
            if (isShown()) {
                return;
            }
            defocusIfNeeded(false);
        }

        @Override // android.widget.TextView, android.view.View
        protected void onFocusChanged(boolean z, int i, Rect rect) {
            super.onFocusChanged(z, i, rect);
            if (!z) {
                defocusIfNeeded(true);
            }
            RemoteInputView remoteInputView = this.mRemoteInputView;
            if (remoteInputView == null || remoteInputView.mRemoved) {
                return;
            }
            this.mLightBarController.setDirectReplying(z);
        }

        @Override // android.widget.TextView, android.view.View
        public void getFocusedRect(Rect rect) {
            super.getFocusedRect(rect);
            int i = ((EditText) this).mScrollY;
            rect.top = i;
            rect.bottom = i + (((EditText) this).mBottom - ((EditText) this).mTop);
        }

        @Override // android.view.View
        public boolean requestRectangleOnScreen(Rect rect) {
            return this.mRemoteInputView.requestScrollTo();
        }

        @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
        public boolean onKeyDown(int i, KeyEvent keyEvent) {
            if (i == 4) {
                return true;
            }
            return super.onKeyDown(i, keyEvent);
        }

        @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
        public boolean onKeyUp(int i, KeyEvent keyEvent) {
            if (i == 4) {
                defocusIfNeeded(true);
                return true;
            }
            return super.onKeyUp(i, keyEvent);
        }

        @Override // android.widget.TextView, android.view.View
        public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == 4 && keyEvent.getAction() == 1) {
                defocusIfNeeded(true);
            }
            return super.onKeyPreIme(i, keyEvent);
        }

        @Override // android.widget.TextView, android.view.View
        public boolean onCheckIsTextEditor() {
            RemoteInputView remoteInputView = this.mRemoteInputView;
            return !(remoteInputView != null && remoteInputView.mRemoved) && super.onCheckIsTextEditor();
        }

        @Override // android.widget.TextView, android.view.View
        public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
            InputConnection inputConnectionOnCreateInputConnection = super.onCreateInputConnection(editorInfo);
            Context context = null;
            InputConnection inputConnectionCreateWrapper = inputConnectionOnCreateInputConnection == null ? null : InputConnectionCompat.createWrapper(inputConnectionOnCreateInputConnection, editorInfo, new InputConnectionCompat.OnCommitContentListener() { // from class: com.android.systemui.statusbar.policy.RemoteInputView.RemoteEditText.1
                @Override // androidx.core.view.inputmethod.InputConnectionCompat.OnCommitContentListener
                public boolean onCommitContent(InputContentInfoCompat inputContentInfoCompat, int i, Bundle bundle) throws PendingIntent.CanceledException {
                    Uri contentUri = inputContentInfoCompat.getContentUri();
                    ClipDescription description = inputContentInfoCompat.getDescription();
                    String mimeType = (description == null || description.getMimeTypeCount() <= 0) ? null : description.getMimeType(0);
                    if (mimeType == null) {
                        return true;
                    }
                    RemoteEditText.this.mRemoteInputView.sendRemoteInput(RemoteEditText.this.mRemoteInputView.prepareRemoteInputFromData(mimeType, contentUri));
                    return true;
                }
            });
            try {
                Context context2 = ((EditText) this).mContext;
                context = context2.createPackageContextAsUser(context2.getPackageName(), 0, this.mUser);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("RemoteInput", "Unable to create user context:" + e.getMessage(), e);
            }
            if (this.mShowImeOnInputConnection && inputConnectionCreateWrapper != null) {
                if (context == null) {
                    context = getContext();
                }
                final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(InputMethodManager.class);
                if (inputMethodManager != null) {
                    post(new Runnable() { // from class: com.android.systemui.statusbar.policy.RemoteInputView.RemoteEditText.2
                        @Override // java.lang.Runnable
                        public void run() {
                            inputMethodManager.viewClicked(RemoteEditText.this);
                            inputMethodManager.showSoftInput(RemoteEditText.this, 0);
                        }
                    });
                }
            }
            return inputConnectionCreateWrapper;
        }

        @Override // android.widget.TextView
        public void onCommitCompletion(CompletionInfo completionInfo) {
            clearComposingText();
            setText(completionInfo.getText());
            setSelection(getText().length());
        }

        void setInnerFocusable(boolean z) {
            setFocusableInTouchMode(z);
            setFocusable(z);
            setCursorVisible(z);
            if (z) {
                requestFocus();
                setBackground(this.mBackground);
            } else {
                setBackground(null);
            }
        }
    }
}
