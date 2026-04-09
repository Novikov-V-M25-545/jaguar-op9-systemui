package com.android.systemui.pip.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import android.util.Property;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class PipMenuActivity extends Activity {
    private AccessibilityManager mAccessibilityManager;
    private LinearLayout mActionsGroup;
    private Drawable mBackgroundDrawable;
    private int mBetweenActionPaddingLand;
    private View mDismissButton;
    private View mMenuContainer;
    private AnimatorSet mMenuContainerAnimator;
    private int mMenuState;
    private View mResizeHandle;
    private View mSettingsButton;
    private Messenger mToControllerMessenger;
    private View mViewRoot;
    private boolean mResize = true;
    private boolean mAllowMenuTimeout = true;
    private boolean mAllowTouches = true;
    private final List<RemoteAction> mActions = new ArrayList();
    private ValueAnimator.AnimatorUpdateListener mMenuBgUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity.1
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            PipMenuActivity.this.mBackgroundDrawable.setAlpha((int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * 0.3f * 255.0f));
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.systemui.pip.phone.PipMenuActivity.2
        @Override // android.os.Handler
        public void handleMessage(Message message) throws RemoteException {
            switch (message.what) {
                case 1:
                    Bundle bundle = (Bundle) message.obj;
                    PipMenuActivity.this.showMenu(bundle.getInt("menu_state"), (Rect) bundle.getParcelable("stack_bounds"), bundle.getBoolean("allow_timeout"), bundle.getBoolean("resize_menu_on_show"), bundle.getBoolean("show_menu_with_delay"), bundle.getBoolean("show_resize_handle"));
                    break;
                case 2:
                    PipMenuActivity.this.cancelDelayedFinish();
                    break;
                case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                    PipMenuActivity.this.hideMenu((Runnable) message.obj);
                    break;
                case 4:
                    Bundle bundle2 = (Bundle) message.obj;
                    ParceledListSlice parcelable = bundle2.getParcelable("actions");
                    PipMenuActivity.this.setActions((Rect) bundle2.getParcelable("stack_bounds"), parcelable != null ? parcelable.getList() : Collections.EMPTY_LIST);
                    break;
                case 5:
                    PipMenuActivity.this.updateDismissFraction(((Bundle) message.obj).getFloat("dismiss_fraction"));
                    break;
                case 6:
                    PipMenuActivity.this.mAllowTouches = true;
                    break;
                case 7:
                    PipMenuActivity.this.dispatchPointerEvent((MotionEvent) message.obj);
                    break;
                case QS.VERSION /* 8 */:
                    PipMenuActivity.this.mMenuContainerAnimator.setStartDelay(30L);
                    PipMenuActivity.this.mMenuContainerAnimator.start();
                    break;
                case 9:
                    PipMenuActivity.this.fadeOutMenu();
                    break;
            }
        }
    };
    private Messenger mMessenger = new Messenger(this.mHandler);
    private final Runnable mFinishRunnable = new Runnable() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda7
        @Override // java.lang.Runnable
        public final void run() throws RemoteException {
            this.f$0.hideMenu();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$updateActionViews$3(View view, MotionEvent motionEvent) {
        return true;
    }

    @Override // android.app.Activity
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) throws RemoteException {
        getWindow().addFlags(LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT);
        super.onCreate(bundle);
        setContentView(R.layout.pip_menu_activity);
        this.mAccessibilityManager = (AccessibilityManager) getSystemService(AccessibilityManager.class);
        ColorDrawable colorDrawable = new ColorDrawable(-16777216);
        this.mBackgroundDrawable = colorDrawable;
        colorDrawable.setAlpha(0);
        View viewFindViewById = findViewById(R.id.background);
        this.mViewRoot = viewFindViewById;
        viewFindViewById.setBackground(this.mBackgroundDrawable);
        View viewFindViewById2 = findViewById(R.id.menu_container);
        this.mMenuContainer = viewFindViewById2;
        viewFindViewById2.setAlpha(0.0f);
        View viewFindViewById3 = findViewById(R.id.settings);
        this.mSettingsButton = viewFindViewById3;
        viewFindViewById3.setAlpha(0.0f);
        this.mSettingsButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$onCreate$0(view);
            }
        });
        View viewFindViewById4 = findViewById(R.id.dismiss);
        this.mDismissButton = viewFindViewById4;
        viewFindViewById4.setAlpha(0.0f);
        this.mDismissButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws RemoteException {
                this.f$0.lambda$onCreate$1(view);
            }
        });
        findViewById(R.id.expand_button).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws RemoteException {
                this.f$0.lambda$onCreate$2(view);
            }
        });
        View viewFindViewById5 = findViewById(R.id.resize_handle);
        this.mResizeHandle = viewFindViewById5;
        viewFindViewById5.setAlpha(0.0f);
        this.mActionsGroup = (LinearLayout) findViewById(R.id.actions_group);
        this.mBetweenActionPaddingLand = getResources().getDimensionPixelSize(R.dimen.pip_between_action_padding_land);
        updateFromIntent(getIntent());
        setTitle(R.string.pip_menu_title);
        setDisablePreviewScreenshots(true);
        getWindow().setExitTransition(null);
        initAccessibility();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(View view) {
        if (view.getAlpha() != 0.0f) {
            showSettings();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(View view) throws RemoteException {
        dismissPip();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View view) throws RemoteException {
        if (this.mMenuContainer.getAlpha() != 0.0f) {
            expandPip();
        }
    }

    private void initAccessibility() {
        getWindow().getDecorView().setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.pip.phone.PipMenuActivity.3
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, PipMenuActivity.this.getResources().getString(R.string.pip_menu_title)));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i, Bundle bundle) throws RemoteException {
                if (i == 16 && PipMenuActivity.this.mMenuState == 1) {
                    Message messageObtain = Message.obtain();
                    messageObtain.what = 107;
                    PipMenuActivity.this.sendMessage(messageObtain, "Could not notify controller to show PIP menu");
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        });
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) throws RemoteException {
        if (i == 111) {
            hideMenu();
            return true;
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) throws RemoteException {
        super.onNewIntent(intent);
        updateFromIntent(intent);
    }

    @Override // android.app.Activity
    public void onUserInteraction() {
        if (this.mAllowMenuTimeout) {
            repostDelayedFinish(2000);
        }
    }

    @Override // android.app.Activity
    protected void onUserLeaveHint() throws RemoteException {
        super.onUserLeaveHint();
        hideMenu();
    }

    @Override // android.app.Activity
    public void onTopResumedActivityChanged(boolean z) throws RemoteException {
        super.onTopResumedActivityChanged(z);
        if (z || this.mMenuState == 0) {
            return;
        }
        hideMenu();
    }

    @Override // android.app.Activity
    protected void onStop() throws RemoteException {
        super.onStop();
        hideMenu();
        cancelDelayedFinish();
    }

    @Override // android.app.Activity
    protected void onDestroy() throws RemoteException {
        super.onDestroy();
        notifyActivityCallback(null);
    }

    @Override // android.app.Activity
    public void onPictureInPictureModeChanged(boolean z) throws RemoteException {
        if (z) {
            return;
        }
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchPointerEvent(MotionEvent motionEvent) throws RemoteException {
        if (motionEvent.isTouchEvent()) {
            dispatchTouchEvent(motionEvent);
        } else {
            dispatchGenericMotionEvent(motionEvent);
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) throws RemoteException {
        if (!this.mAllowTouches) {
            return false;
        }
        if (motionEvent.getAction() == 4) {
            hideMenu();
            return true;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override // android.app.Activity
    public void finish() throws RemoteException {
        notifyActivityCallback(null);
        super.finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showMenu(int i, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) throws RemoteException {
        this.mAllowMenuTimeout = z;
        int i2 = this.mMenuState;
        if (i2 == i) {
            if (z) {
                repostDelayedFinish(2000);
                return;
            }
            return;
        }
        this.mAllowTouches = !(z2 && (i2 == 2 || i == 2));
        cancelDelayedFinish();
        updateActionViews(rect);
        AnimatorSet animatorSet = this.mMenuContainerAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        this.mMenuContainerAnimator = new AnimatorSet();
        View view = this.mMenuContainer;
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.ALPHA, view.getAlpha(), 1.0f);
        objectAnimatorOfFloat.addUpdateListener(this.mMenuBgUpdateListener);
        View view2 = this.mSettingsButton;
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(view2, (Property<View, Float>) View.ALPHA, view2.getAlpha(), 1.0f);
        View view3 = this.mDismissButton;
        ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(view3, (Property<View, Float>) View.ALPHA, view3.getAlpha(), 1.0f);
        View view4 = this.mResizeHandle;
        ObjectAnimator objectAnimatorOfFloat4 = ObjectAnimator.ofFloat(view4, (Property<View, Float>) View.ALPHA, view4.getAlpha(), 0.0f);
        if (i == 2) {
            this.mMenuContainerAnimator.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2, objectAnimatorOfFloat3, objectAnimatorOfFloat4);
        } else {
            this.mMenuContainerAnimator.playTogether(objectAnimatorOfFloat3, objectAnimatorOfFloat4);
        }
        this.mMenuContainerAnimator.setInterpolator(Interpolators.ALPHA_IN);
        this.mMenuContainerAnimator.setDuration(i == 1 ? 125L : 175L);
        if (z) {
            this.mMenuContainerAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.pip.phone.PipMenuActivity.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    PipMenuActivity.this.repostDelayedFinish(3500);
                }
            });
        }
        if (z3) {
            notifyMenuStateChange(i, z2, 8);
        } else {
            notifyMenuStateChange(i, z2, -1);
            this.mMenuContainerAnimator.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fadeOutMenu() {
        this.mMenuContainer.setAlpha(0.0f);
        this.mSettingsButton.setAlpha(0.0f);
        this.mDismissButton.setAlpha(0.0f);
        this.mResizeHandle.setAlpha(0.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideMenu() throws RemoteException {
        hideMenu(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideMenu(Runnable runnable) throws RemoteException {
        hideMenu(runnable, true, false, true);
    }

    private void hideMenu(final Runnable runnable, boolean z, final boolean z2, boolean z3) throws RemoteException {
        if (this.mMenuState != 0) {
            cancelDelayedFinish();
            if (z) {
                notifyMenuStateChange(0, this.mResize, -1);
            }
            this.mMenuContainerAnimator = new AnimatorSet();
            View view = this.mMenuContainer;
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, (Property<View, Float>) View.ALPHA, view.getAlpha(), 0.0f);
            objectAnimatorOfFloat.addUpdateListener(this.mMenuBgUpdateListener);
            View view2 = this.mSettingsButton;
            ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(view2, (Property<View, Float>) View.ALPHA, view2.getAlpha(), 0.0f);
            View view3 = this.mDismissButton;
            ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(view3, (Property<View, Float>) View.ALPHA, view3.getAlpha(), 0.0f);
            View view4 = this.mResizeHandle;
            this.mMenuContainerAnimator.playTogether(objectAnimatorOfFloat, objectAnimatorOfFloat2, objectAnimatorOfFloat3, ObjectAnimator.ofFloat(view4, (Property<View, Float>) View.ALPHA, view4.getAlpha(), 0.0f));
            this.mMenuContainerAnimator.setInterpolator(Interpolators.ALPHA_OUT);
            this.mMenuContainerAnimator.setDuration(z3 ? 125L : 0L);
            this.mMenuContainerAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.pip.phone.PipMenuActivity.5
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) throws RemoteException {
                    Runnable runnable2 = runnable;
                    if (runnable2 != null) {
                        runnable2.run();
                    }
                    if (z2) {
                        return;
                    }
                    PipMenuActivity.this.finish();
                }
            });
            this.mMenuContainerAnimator.start();
            return;
        }
        finish();
    }

    private void updateFromIntent(Intent intent) throws RemoteException {
        Messenger messenger = (Messenger) intent.getParcelableExtra("messenger");
        this.mToControllerMessenger = messenger;
        if (messenger == null) {
            Log.w("PipMenuActivity", "Controller messenger is null. Stopping.");
            finish();
            return;
        }
        notifyActivityCallback(this.mMessenger);
        ParceledListSlice parcelableExtra = intent.getParcelableExtra("actions");
        if (parcelableExtra != null) {
            this.mActions.clear();
            this.mActions.addAll(parcelableExtra.getList());
        }
        int intExtra = intent.getIntExtra("menu_state", 0);
        if (intExtra != 0) {
            showMenu(intExtra, (Rect) intent.getParcelableExtra("stack_bounds"), intent.getBooleanExtra("allow_timeout", true), intent.getBooleanExtra("resize_menu_on_show", false), intent.getBooleanExtra("show_menu_with_delay", false), intent.getBooleanExtra("show_resize_handle", false));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setActions(Rect rect, List<RemoteAction> list) {
        this.mActions.clear();
        this.mActions.addAll(list);
        updateActionViews(rect);
    }

    private void updateActionViews(Rect rect) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.expand_container);
        ViewGroup viewGroup2 = (ViewGroup) findViewById(R.id.actions_container);
        viewGroup2.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return PipMenuActivity.lambda$updateActionViews$3(view, motionEvent);
            }
        });
        if (!this.mActions.isEmpty()) {
            if (this.mMenuState != 1) {
                viewGroup2.setVisibility(0);
                if (this.mActionsGroup != null) {
                    LayoutInflater layoutInflaterFrom = LayoutInflater.from(this);
                    while (this.mActionsGroup.getChildCount() < this.mActions.size()) {
                        this.mActionsGroup.addView((ImageButton) layoutInflaterFrom.inflate(R.layout.pip_menu_action, (ViewGroup) this.mActionsGroup, false));
                    }
                    int i = 0;
                    while (i < this.mActionsGroup.getChildCount()) {
                        this.mActionsGroup.getChildAt(i).setVisibility(i < this.mActions.size() ? 0 : 8);
                        i++;
                    }
                    boolean z = rect != null && rect.width() > rect.height();
                    int i2 = 0;
                    while (i2 < this.mActions.size()) {
                        final RemoteAction remoteAction = this.mActions.get(i2);
                        final ImageButton imageButton = (ImageButton) this.mActionsGroup.getChildAt(i2);
                        int type = remoteAction.getIcon().getType();
                        if (type == 4 || type == 6) {
                            imageButton.setImageDrawable(null);
                        } else {
                            remoteAction.getIcon().loadDrawableAsync(this, new Icon.OnDrawableLoadedListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda0
                                @Override // android.graphics.drawable.Icon.OnDrawableLoadedListener
                                public final void onDrawableLoaded(Drawable drawable) {
                                    PipMenuActivity.lambda$updateActionViews$4(imageButton, drawable);
                                }
                            }, this.mHandler);
                        }
                        imageButton.setContentDescription(remoteAction.getContentDescription());
                        if (remoteAction.isEnabled()) {
                            imageButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda4
                                @Override // android.view.View.OnClickListener
                                public final void onClick(View view) {
                                    this.f$0.lambda$updateActionViews$6(remoteAction, view);
                                }
                            });
                        }
                        imageButton.setEnabled(remoteAction.isEnabled());
                        imageButton.setAlpha(remoteAction.isEnabled() ? 1.0f : 0.54f);
                        ((LinearLayout.LayoutParams) imageButton.getLayoutParams()).leftMargin = (!z || i2 <= 0) ? 0 : this.mBetweenActionPaddingLand;
                        i2++;
                    }
                }
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.pip_action_padding);
                layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.pip_expand_container_edge_margin);
                viewGroup.requestLayout();
                return;
            }
        }
        viewGroup2.setVisibility(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateActionViews$4(ImageButton imageButton, Drawable drawable) {
        if (drawable != null) {
            drawable.setTint(-1);
            imageButton.setImageDrawable(drawable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateActionViews$6(final RemoteAction remoteAction, View view) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() throws PendingIntent.CanceledException {
                PipMenuActivity.lambda$updateActionViews$5(remoteAction);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateActionViews$5(RemoteAction remoteAction) throws PendingIntent.CanceledException {
        try {
            remoteAction.getActionIntent().send();
        } catch (PendingIntent.CanceledException e) {
            Log.w("PipMenuActivity", "Failed to send action", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDismissFraction(float f) {
        int i;
        float f2 = 1.0f - f;
        int i2 = this.mMenuState;
        if (i2 == 2) {
            this.mMenuContainer.setAlpha(f2);
            this.mSettingsButton.setAlpha(f2);
            this.mDismissButton.setAlpha(f2);
            i = (int) (((f2 * 0.3f) + (f * 0.6f)) * 255.0f);
        } else {
            if (i2 == 1) {
                this.mDismissButton.setAlpha(f2);
            }
            i = (int) (f * 0.6f * 255.0f);
        }
        this.mBackgroundDrawable.setAlpha(i);
    }

    private void notifyMenuStateChange(int i, boolean z, int i2) throws RemoteException {
        this.mMenuState = i;
        this.mResize = z;
        Message messageObtain = Message.obtain();
        messageObtain.what = 100;
        messageObtain.arg1 = i;
        messageObtain.arg2 = z ? 1 : 0;
        if (i2 != -1) {
            messageObtain.replyTo = this.mMessenger;
            Bundle bundle = new Bundle(1);
            bundle.putInt("message_callback_what", i2);
            messageObtain.obj = bundle;
        }
        sendMessage(messageObtain, "Could not notify controller of PIP menu visibility");
    }

    private void expandPip() throws RemoteException {
        hideMenu(new Runnable() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() throws RemoteException {
                this.f$0.lambda$expandPip$7();
            }
        }, false, false, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$expandPip$7() throws RemoteException {
        sendEmptyMessage(101, "Could not notify controller to expand PIP");
    }

    private void dismissPip() throws RemoteException {
        hideMenu(new Runnable() { // from class: com.android.systemui.pip.phone.PipMenuActivity$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() throws RemoteException {
                this.f$0.lambda$dismissPip$8();
            }
        }, false, true, this.mMenuState != 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$dismissPip$8() throws RemoteException {
        sendEmptyMessage(103, "Could not notify controller to dismiss PIP");
    }

    private void showSettings() {
        Pair<ComponentName, Integer> topPipActivity = PipUtils.getTopPipActivity(this, ActivityManager.getService());
        if (topPipActivity.first != null) {
            UserHandle userHandleOf = UserHandle.of(((Integer) topPipActivity.second).intValue());
            Intent intent = new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.fromParts("package", ((ComponentName) topPipActivity.first).getPackageName(), null));
            intent.putExtra("android.intent.extra.user_handle", userHandleOf);
            intent.setFlags(268468224);
            startActivity(intent);
        }
    }

    private void notifyActivityCallback(Messenger messenger) throws RemoteException {
        Message messageObtain = Message.obtain();
        messageObtain.what = 104;
        messageObtain.replyTo = messenger;
        messageObtain.arg1 = this.mResize ? 1 : 0;
        sendMessage(messageObtain, "Could not notify controller of activity finished");
    }

    private void sendEmptyMessage(int i, String str) throws RemoteException {
        Message messageObtain = Message.obtain();
        messageObtain.what = i;
        sendMessage(messageObtain, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendMessage(Message message, String str) throws RemoteException {
        Messenger messenger = this.mToControllerMessenger;
        if (messenger == null) {
            return;
        }
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            Log.e("PipMenuActivity", str, e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelDelayedFinish() {
        this.mHandler.removeCallbacks(this.mFinishRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void repostDelayedFinish(int i) {
        int recommendedTimeoutMillis = this.mAccessibilityManager.getRecommendedTimeoutMillis(i, 5);
        this.mHandler.removeCallbacks(this.mFinishRunnable);
        this.mHandler.postDelayed(this.mFinishRunnable, recommendedTimeoutMillis);
    }
}
