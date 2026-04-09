package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.view.RotationPolicy;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.RotationLockController;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/* loaded from: classes.dex */
public class RotationButtonController {
    private AccessibilityManagerWrapper mAccessibilityManagerWrapper;
    private final Context mContext;
    private boolean mHoveringRotationSuggestion;
    private boolean mIsNavigationBarShowing;
    private int mLastRotationSuggestion;
    private boolean mPendingRotationSuggestion;
    private Consumer<Integer> mRotWatcherListener;
    private Animator mRotateHideAnimator;
    private final RotationButton mRotationButton;
    private RotationLockController mRotationLockController;
    private boolean mSkipOverrideUserLockPrefsOnce;
    private int mStyleRes;
    private TaskStackListenerImpl mTaskStackListener;
    private final ViewRippler mViewRippler;
    private final UiEventLogger mUiEventLogger = new UiEventLoggerImpl();
    private boolean mListenersRegistered = false;
    private final Runnable mRemoveRotationProposal = new Runnable() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$$ExternalSyntheticLambda2
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };
    private final Runnable mCancelPendingRotationProposal = new Runnable() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$$ExternalSyntheticLambda3
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$1();
        }
    };
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final IRotationWatcher.Stub mRotationWatcher = new AnonymousClass1();

    static boolean hasDisable2RotateSuggestionFlag(int i) {
        return (i & 16) != 0;
    }

    private boolean isRotationAnimationCCW(int i, int i2) {
        if (i == 0 && i2 == 1) {
            return false;
        }
        if (i == 0 && i2 == 2) {
            return true;
        }
        if (i == 0 && i2 == 3) {
            return true;
        }
        if (i == 1 && i2 == 0) {
            return true;
        }
        if (i == 1 && i2 == 2) {
            return false;
        }
        if (i == 1 && i2 == 3) {
            return true;
        }
        if (i == 2 && i2 == 0) {
            return true;
        }
        if (i == 2 && i2 == 1) {
            return true;
        }
        if (i == 2 && i2 == 3) {
            return false;
        }
        if (i == 3 && i2 == 0) {
            return false;
        }
        if (i == 3 && i2 == 1) {
            return true;
        }
        return i == 3 && i2 == 2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        setRotateSuggestionButtonState(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1() {
        this.mPendingRotationSuggestion = false;
    }

    /* renamed from: com.android.systemui.statusbar.phone.RotationButtonController$1, reason: invalid class name */
    class AnonymousClass1 extends IRotationWatcher.Stub {
        AnonymousClass1() {
        }

        public void onRotationChanged(final int i) throws RemoteException {
            RotationButtonController.this.mMainThreadHandler.postAtFrontOfQueue(new Runnable() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onRotationChanged$0(i);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onRotationChanged$0(int i) {
            if (RotationButtonController.this.mRotationLockController.isRotationLocked()) {
                if (RotationButtonController.this.shouldOverrideUserLockPrefs(i)) {
                    RotationButtonController.this.setRotationLockedAtAngle(i);
                }
                RotationButtonController.this.setRotateSuggestionButtonState(false, true);
            }
            if (RotationButtonController.this.mRotWatcherListener != null) {
                RotationButtonController.this.mRotWatcherListener.accept(Integer.valueOf(i));
            }
        }
    }

    RotationButtonController(Context context, int i, RotationButton rotationButton, Consumer<Boolean> consumer) {
        AnonymousClass1 anonymousClass1 = null;
        this.mViewRippler = new ViewRippler(this, anonymousClass1);
        this.mContext = context;
        this.mRotationButton = rotationButton;
        rotationButton.setRotationButtonController(this);
        this.mStyleRes = i;
        this.mIsNavigationBarShowing = true;
        this.mRotationLockController = (RotationLockController) Dependency.get(RotationLockController.class);
        this.mAccessibilityManagerWrapper = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);
        this.mTaskStackListener = new TaskStackListenerImpl(this, anonymousClass1);
        rotationButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.onRotateSuggestionClick(view);
            }
        });
        rotationButton.setOnHoverListener(new View.OnHoverListener() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$$ExternalSyntheticLambda1
            @Override // android.view.View.OnHoverListener
            public final boolean onHover(View view, MotionEvent motionEvent) {
                return this.f$0.onRotateSuggestionHover(view, motionEvent);
            }
        });
        rotationButton.setVisibilityChangedCallback(consumer);
    }

    void registerListeners() {
        if (this.mListenersRegistered) {
            return;
        }
        this.mListenersRegistered = true;
        try {
            WindowManagerGlobal.getWindowManagerService().watchRotation(this.mRotationWatcher, this.mContext.getDisplay().getDisplayId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (IllegalArgumentException unused) {
            this.mListenersRegistered = false;
            Log.w("StatusBar/RotationButtonController", "RegisterListeners for the display failed");
        }
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
    }

    void unregisterListeners() {
        if (this.mListenersRegistered) {
            this.mListenersRegistered = false;
            try {
                WindowManagerGlobal.getWindowManagerService().removeRotationWatcher(this.mRotationWatcher);
                ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    void setRotationCallback(Consumer<Integer> consumer) {
        this.mRotWatcherListener = consumer;
    }

    void setRotationLockedAtAngle(int i) {
        this.mRotationLockController.setRotationLockedAtAngle(true, i);
    }

    public boolean isRotationLocked() {
        return this.mRotationLockController.isRotationLocked();
    }

    void setRotateSuggestionButtonState(boolean z) {
        setRotateSuggestionButtonState(z, false);
    }

    void setRotateSuggestionButtonState(boolean z, boolean z2) {
        View currentView;
        KeyButtonDrawable imageDrawable;
        if ((!z && !this.mRotationButton.isVisible()) || (currentView = this.mRotationButton.getCurrentView()) == null || (imageDrawable = this.mRotationButton.getImageDrawable()) == null) {
            return;
        }
        this.mPendingRotationSuggestion = false;
        this.mMainThreadHandler.removeCallbacks(this.mCancelPendingRotationProposal);
        if (z) {
            Animator animator = this.mRotateHideAnimator;
            if (animator != null && animator.isRunning()) {
                this.mRotateHideAnimator.cancel();
            }
            this.mRotateHideAnimator = null;
            currentView.setAlpha(1.0f);
            if (imageDrawable.canAnimate()) {
                imageDrawable.resetAnimation();
                imageDrawable.startAnimation();
            }
            if (!isRotateSuggestionIntroduced()) {
                this.mViewRippler.start(currentView);
            }
            this.mRotationButton.show();
            return;
        }
        this.mViewRippler.stop();
        if (z2) {
            Animator animator2 = this.mRotateHideAnimator;
            if (animator2 != null && animator2.isRunning()) {
                this.mRotateHideAnimator.pause();
            }
            this.mRotationButton.hide();
            return;
        }
        Animator animator3 = this.mRotateHideAnimator;
        if (animator3 == null || !animator3.isRunning()) {
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(currentView, "alpha", 0.0f);
            objectAnimatorOfFloat.setDuration(100L);
            objectAnimatorOfFloat.setInterpolator(Interpolators.LINEAR);
            objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.RotationButtonController.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator4) {
                    RotationButtonController.this.mRotationButton.hide();
                }
            });
            this.mRotateHideAnimator = objectAnimatorOfFloat;
            objectAnimatorOfFloat.start();
        }
    }

    void setDarkIntensity(float f) {
        this.mRotationButton.setDarkIntensity(f);
    }

    void onRotationProposal(int i, int i2, boolean z) {
        int i3;
        if (this.mRotationButton.acceptRotationProposal()) {
            if (!z) {
                setRotateSuggestionButtonState(false);
                return;
            }
            if (i == i2) {
                this.mMainThreadHandler.removeCallbacks(this.mRemoveRotationProposal);
                setRotateSuggestionButtonState(false);
                return;
            }
            this.mLastRotationSuggestion = i;
            boolean zIsRotationAnimationCCW = isRotationAnimationCCW(i2, i);
            if (i2 == 0 || i2 == 2) {
                i3 = zIsRotationAnimationCCW ? R.style.RotateButtonCCWStart90 : R.style.RotateButtonCWStart90;
            } else {
                i3 = zIsRotationAnimationCCW ? R.style.RotateButtonCCWStart0 : R.style.RotateButtonCWStart0;
            }
            this.mStyleRes = i3;
            this.mRotationButton.updateIcon();
            if (this.mIsNavigationBarShowing) {
                showAndLogRotationSuggestion();
                return;
            }
            this.mPendingRotationSuggestion = true;
            this.mMainThreadHandler.removeCallbacks(this.mCancelPendingRotationProposal);
            this.mMainThreadHandler.postDelayed(this.mCancelPendingRotationProposal, 20000L);
        }
    }

    void onDisable2FlagChanged(int i) {
        if (hasDisable2RotateSuggestionFlag(i)) {
            onRotationSuggestionsDisabled();
        }
    }

    void onNavigationBarWindowVisibilityChange(boolean z) {
        if (this.mIsNavigationBarShowing != z) {
            this.mIsNavigationBarShowing = z;
            if (z && this.mPendingRotationSuggestion) {
                showAndLogRotationSuggestion();
            }
        }
    }

    int getStyleRes() {
        return this.mStyleRes;
    }

    RotationButton getRotationButton() {
        return this.mRotationButton;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRotateSuggestionClick(View view) {
        this.mUiEventLogger.log(RotationButtonEvent.ROTATION_SUGGESTION_ACCEPTED);
        incrementNumAcceptedRotationSuggestionsIfNeeded();
        setRotationLockedAtAngle(this.mLastRotationSuggestion);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onRotateSuggestionHover(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        this.mHoveringRotationSuggestion = actionMasked == 9 || actionMasked == 7;
        rescheduleRotationTimeout(true);
        return false;
    }

    private void onRotationSuggestionsDisabled() {
        setRotateSuggestionButtonState(false, true);
        this.mMainThreadHandler.removeCallbacks(this.mRemoveRotationProposal);
    }

    private void showAndLogRotationSuggestion() {
        setRotateSuggestionButtonState(true);
        rescheduleRotationTimeout(false);
        this.mUiEventLogger.log(RotationButtonEvent.ROTATION_SUGGESTION_SHOWN);
    }

    void setSkipOverrideUserLockPrefsOnce() {
        this.mSkipOverrideUserLockPrefsOnce = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean shouldOverrideUserLockPrefs(int i) {
        if (!this.mSkipOverrideUserLockPrefsOnce) {
            return i == RotationPolicy.getNaturalRotation();
        }
        this.mSkipOverrideUserLockPrefsOnce = false;
        return false;
    }

    private void rescheduleRotationTimeout(boolean z) {
        Animator animator;
        if (!z || (((animator = this.mRotateHideAnimator) == null || !animator.isRunning()) && this.mRotationButton.isVisible())) {
            this.mMainThreadHandler.removeCallbacks(this.mRemoveRotationProposal);
            this.mMainThreadHandler.postDelayed(this.mRemoveRotationProposal, computeRotationProposalTimeout());
        }
    }

    private int computeRotationProposalTimeout() {
        return this.mAccessibilityManagerWrapper.getRecommendedTimeoutMillis(this.mHoveringRotationSuggestion ? 16000 : 5000, 4);
    }

    private boolean isRotateSuggestionIntroduced() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "num_rotation_suggestions_accepted", 0) >= 3;
    }

    private void incrementNumAcceptedRotationSuggestionsIfNeeded() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int i = Settings.Secure.getInt(contentResolver, "num_rotation_suggestions_accepted", 0);
        if (i < 3) {
            Settings.Secure.putInt(contentResolver, "num_rotation_suggestions_accepted", i + 1);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class TaskStackListenerImpl extends TaskStackChangeListener {
        private TaskStackListenerImpl() {
        }

        /* synthetic */ TaskStackListenerImpl(RotationButtonController rotationButtonController, AnonymousClass1 anonymousClass1) {
            this();
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            RotationButtonController.this.setRotateSuggestionButtonState(false);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskRemoved(int i) {
            RotationButtonController.this.setRotateSuggestionButtonState(false);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(int i) {
            RotationButtonController.this.setRotateSuggestionButtonState(false);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRequestedOrientationChanged(final int i, int i2) {
            Optional.ofNullable(ActivityManagerWrapper.getInstance()).map(new Function() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$TaskStackListenerImpl$$ExternalSyntheticLambda1
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return ((ActivityManagerWrapper) obj).getRunningTask();
                }
            }).ifPresent(new Consumer() { // from class: com.android.systemui.statusbar.phone.RotationButtonController$TaskStackListenerImpl$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.lambda$onActivityRequestedOrientationChanged$0(i, (ActivityManager.RunningTaskInfo) obj);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActivityRequestedOrientationChanged$0(int i, ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (runningTaskInfo.id == i) {
                RotationButtonController.this.setRotateSuggestionButtonState(false);
            }
        }
    }

    private class ViewRippler {
        private final Runnable mRipple;
        private View mRoot;

        private ViewRippler() {
            this.mRipple = new Runnable() { // from class: com.android.systemui.statusbar.phone.RotationButtonController.ViewRippler.1
                @Override // java.lang.Runnable
                public void run() {
                    if (ViewRippler.this.mRoot.isAttachedToWindow()) {
                        ViewRippler.this.mRoot.setPressed(true);
                        ViewRippler.this.mRoot.setPressed(false);
                    }
                }
            };
        }

        /* synthetic */ ViewRippler(RotationButtonController rotationButtonController, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void start(View view) {
            stop();
            this.mRoot = view;
            view.postOnAnimationDelayed(this.mRipple, 50L);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 2000L);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 4000L);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 6000L);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 8000L);
        }

        public void stop() {
            View view = this.mRoot;
            if (view != null) {
                view.removeCallbacks(this.mRipple);
            }
        }
    }

    enum RotationButtonEvent implements UiEventLogger.UiEventEnum {
        ROTATION_SUGGESTION_SHOWN(206),
        ROTATION_SUGGESTION_ACCEPTED(207);

        private final int mId;

        RotationButtonEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }
}
