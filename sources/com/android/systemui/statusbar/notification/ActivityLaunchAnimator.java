package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.fingerprint.IFingerprintService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.MathUtils;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;
import android.view.SyncRtSurfaceTransactionApplier;
import android.view.View;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.biometrics.FODCircleViewImpl;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class ActivityLaunchAnimator {
    private boolean mAnimationPending;
    private boolean mAnimationRunning;
    private Callback mCallback;
    private final NotificationShadeDepthController mDepthController;
    private FODCircleViewImpl mFODCircleViewImpl;
    private boolean mIsLaunchForActivity;
    private final Executor mMainExecutor;
    private final NotificationListContainer mNotificationContainer;
    private final NotificationPanelViewController mNotificationPanel;
    private final NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    private final float mWindowCornerRadius;
    private final Runnable mTimeoutRunnable = new Runnable() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };
    private IFingerprintService mFingerprintService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));

    public interface Callback {
        boolean areLaunchAnimationsEnabled();

        void onExpandAnimationFinished(boolean z);

        void onExpandAnimationTimedOut();

        void onLaunchAnimationCancelled();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        setAnimationPending(false);
        this.mCallback.onExpandAnimationTimedOut();
    }

    public ActivityLaunchAnimator(NotificationShadeWindowViewController notificationShadeWindowViewController, Callback callback, NotificationPanelViewController notificationPanelViewController, NotificationShadeDepthController notificationShadeDepthController, NotificationListContainer notificationListContainer, Executor executor, FODCircleViewImpl fODCircleViewImpl) {
        this.mNotificationPanel = notificationPanelViewController;
        this.mNotificationContainer = notificationListContainer;
        this.mDepthController = notificationShadeDepthController;
        this.mNotificationShadeWindowViewController = notificationShadeWindowViewController;
        this.mCallback = callback;
        this.mMainExecutor = executor;
        this.mWindowCornerRadius = ScreenDecorationsUtils.getWindowCornerRadius(notificationShadeWindowViewController.getView().getResources());
        this.mFODCircleViewImpl = fODCircleViewImpl;
    }

    public RemoteAnimationAdapter getLaunchAnimation(View view, boolean z) {
        if ((view instanceof ExpandableNotificationRow) && this.mCallback.areLaunchAnimationsEnabled() && !z) {
            return new RemoteAnimationAdapter(new AnimationRunner((ExpandableNotificationRow) view), 400L, 250L);
        }
        return null;
    }

    public boolean isAnimationPending() {
        return this.mAnimationPending;
    }

    public void setLaunchResult(int i, boolean z) {
        this.mIsLaunchForActivity = z;
        setAnimationPending((i == 2 || i == 0) && this.mCallback.areLaunchAnimationsEnabled());
    }

    public boolean isLaunchForActivity() {
        return this.mIsLaunchForActivity;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setAnimationPending(boolean z) {
        this.mAnimationPending = z;
        this.mNotificationShadeWindowViewController.setExpandAnimationPending(z);
        if (z) {
            this.mNotificationShadeWindowViewController.getView().postDelayed(this.mTimeoutRunnable, 500L);
        } else {
            this.mNotificationShadeWindowViewController.getView().removeCallbacks(this.mTimeoutRunnable);
        }
    }

    public boolean isAnimationRunning() {
        return this.mAnimationRunning;
    }

    class AnimationRunner extends IRemoteAnimationRunner.Stub {
        private float mCornerRadius;
        private final float mNotificationCornerRadius;
        private final ExpandableNotificationRow mSourceNotification;
        private final SyncRtSurfaceTransactionApplier mSyncRtTransactionApplier;
        private final Rect mWindowCrop = new Rect();
        private boolean mIsFullScreenLaunch = true;
        private final ExpandAnimationParameters mParams = new ExpandAnimationParameters();

        public AnimationRunner(ExpandableNotificationRow expandableNotificationRow) {
            this.mSourceNotification = expandableNotificationRow;
            this.mSyncRtTransactionApplier = new SyncRtSurfaceTransactionApplier(expandableNotificationRow);
            this.mNotificationCornerRadius = Math.max(expandableNotificationRow.getCurrentTopRoundness(), expandableNotificationRow.getCurrentBottomRoundness());
        }

        public void onAnimationStart(final RemoteAnimationTarget[] remoteAnimationTargetArr, RemoteAnimationTarget[] remoteAnimationTargetArr2, final IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) throws RemoteException {
            ActivityLaunchAnimator.this.mMainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator$AnimationRunner$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onAnimationStart$0(remoteAnimationTargetArr, iRemoteAnimationFinishedCallback);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onAnimationStart$0(RemoteAnimationTarget[] remoteAnimationTargetArr, final IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            final RemoteAnimationTarget primaryRemoteAnimationTarget = getPrimaryRemoteAnimationTarget(remoteAnimationTargetArr);
            if (primaryRemoteAnimationTarget == null) {
                ActivityLaunchAnimator.this.setAnimationPending(false);
                invokeCallback(iRemoteAnimationFinishedCallback);
                ActivityLaunchAnimator.this.mNotificationPanel.collapse(false, 1.0f);
                return;
            }
            setExpandAnimationRunning(true);
            boolean z = primaryRemoteAnimationTarget.position.y == 0 && primaryRemoteAnimationTarget.sourceContainerBounds.height() >= ActivityLaunchAnimator.this.mNotificationPanel.getHeight();
            this.mIsFullScreenLaunch = z;
            if (!z) {
                ActivityLaunchAnimator.this.mNotificationPanel.collapseWithDuration(400);
            }
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mParams.startPosition = this.mSourceNotification.getLocationOnScreen();
            this.mParams.startTranslationZ = this.mSourceNotification.getTranslationZ();
            this.mParams.startClipTopAmount = this.mSourceNotification.getClipTopAmount();
            if (this.mSourceNotification.isChildInGroup()) {
                int clipTopAmount = this.mSourceNotification.getNotificationParent().getClipTopAmount();
                this.mParams.parentStartClipTopAmount = clipTopAmount;
                if (clipTopAmount != 0) {
                    float translationY = clipTopAmount - this.mSourceNotification.getTranslationY();
                    if (translationY > 0.0f) {
                        this.mParams.startClipTopAmount = (int) Math.ceil(translationY);
                    }
                }
            }
            final int iWidth = primaryRemoteAnimationTarget.sourceContainerBounds.width();
            final int iMax = Math.max(this.mSourceNotification.getActualHeight() - this.mSourceNotification.getClipBottomAmount(), 0);
            final int width = this.mSourceNotification.getWidth();
            valueAnimatorOfFloat.setDuration(400L);
            valueAnimatorOfFloat.setInterpolator(Interpolators.LINEAR);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator.AnimationRunner.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimationRunner.this.mParams.linearProgress = valueAnimator.getAnimatedFraction();
                    float interpolation = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(AnimationRunner.this.mParams.linearProgress);
                    int iLerp = (int) MathUtils.lerp(width, iWidth, interpolation);
                    AnimationRunner.this.mParams.left = (int) ((iWidth - iLerp) / 2.0f);
                    AnimationRunner.this.mParams.right = AnimationRunner.this.mParams.left + iLerp;
                    AnimationRunner.this.mParams.top = (int) MathUtils.lerp(AnimationRunner.this.mParams.startPosition[1], primaryRemoteAnimationTarget.position.y, interpolation);
                    ExpandAnimationParameters expandAnimationParameters = AnimationRunner.this.mParams;
                    float f = AnimationRunner.this.mParams.startPosition[1] + iMax;
                    RemoteAnimationTarget remoteAnimationTarget = primaryRemoteAnimationTarget;
                    expandAnimationParameters.bottom = (int) MathUtils.lerp(f, remoteAnimationTarget.position.y + remoteAnimationTarget.sourceContainerBounds.bottom, interpolation);
                    AnimationRunner animationRunner = AnimationRunner.this;
                    animationRunner.mCornerRadius = MathUtils.lerp(animationRunner.mNotificationCornerRadius, ActivityLaunchAnimator.this.mWindowCornerRadius, interpolation);
                    AnimationRunner.this.applyParamsToWindow(primaryRemoteAnimationTarget);
                    AnimationRunner animationRunner2 = AnimationRunner.this;
                    animationRunner2.applyParamsToNotification(animationRunner2.mParams);
                    AnimationRunner animationRunner3 = AnimationRunner.this;
                    animationRunner3.applyParamsToNotificationShade(animationRunner3.mParams);
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator.AnimationRunner.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    AnimationRunner.this.setExpandAnimationRunning(false);
                    AnimationRunner.this.invokeCallback(iRemoteAnimationFinishedCallback);
                }
            });
            valueAnimatorOfFloat.start();
            ActivityLaunchAnimator.this.setAnimationPending(false);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void invokeCallback(IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            try {
                iRemoteAnimationFinishedCallback.onAnimationFinished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private RemoteAnimationTarget getPrimaryRemoteAnimationTarget(RemoteAnimationTarget[] remoteAnimationTargetArr) {
            for (RemoteAnimationTarget remoteAnimationTarget : remoteAnimationTargetArr) {
                if (remoteAnimationTarget.mode == 0) {
                    return remoteAnimationTarget;
                }
            }
            return null;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setExpandAnimationRunning(boolean z) {
            ActivityLaunchAnimator.this.mNotificationPanel.setLaunchingNotification(z);
            this.mSourceNotification.setExpandAnimationRunning(z);
            ActivityLaunchAnimator.this.mNotificationShadeWindowViewController.setExpandAnimationRunning(z);
            ActivityLaunchAnimator.this.mNotificationContainer.setExpandingNotification(z ? this.mSourceNotification : null);
            ActivityLaunchAnimator.this.mAnimationRunning = z;
            boolean zIsClientActive = false;
            try {
                zIsClientActive = ActivityLaunchAnimator.this.mFingerprintService.isClientActive();
            } catch (Exception unused) {
            }
            if (!zIsClientActive) {
                ActivityLaunchAnimator.this.mFODCircleViewImpl.hideInDisplayFingerprintView();
            }
            ActivityLaunchAnimator.this.mFODCircleViewImpl.hideInDisplayFingerprintView();
            if (z) {
                return;
            }
            ActivityLaunchAnimator.this.mCallback.onExpandAnimationFinished(this.mIsFullScreenLaunch);
            applyParamsToNotification(null);
            applyParamsToNotificationShade(null);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyParamsToNotificationShade(ExpandAnimationParameters expandAnimationParameters) {
            ActivityLaunchAnimator.this.mNotificationContainer.applyExpandAnimationParams(expandAnimationParameters);
            ActivityLaunchAnimator.this.mNotificationPanel.applyExpandAnimationParams(expandAnimationParameters);
            ActivityLaunchAnimator.this.mDepthController.setNotificationLaunchAnimationParams(expandAnimationParameters);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyParamsToNotification(ExpandAnimationParameters expandAnimationParameters) {
            this.mSourceNotification.applyExpandAnimationParams(expandAnimationParameters);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyParamsToWindow(RemoteAnimationTarget remoteAnimationTarget) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(0.0f, this.mParams.top - remoteAnimationTarget.position.y);
            Rect rect = this.mWindowCrop;
            ExpandAnimationParameters expandAnimationParameters = this.mParams;
            rect.set(expandAnimationParameters.left, 0, expandAnimationParameters.right, expandAnimationParameters.getHeight());
            this.mSyncRtTransactionApplier.scheduleApply(new SyncRtSurfaceTransactionApplier.SurfaceParams[]{new SyncRtSurfaceTransactionApplier.SurfaceParams.Builder(remoteAnimationTarget.leash).withAlpha(1.0f).withMatrix(matrix).withWindowCrop(this.mWindowCrop).withLayer(remoteAnimationTarget.prefixOrderIndex).withCornerRadius(this.mCornerRadius).withVisibility(true).build()});
        }

        public void onAnimationCancelled() throws RemoteException {
            ActivityLaunchAnimator.this.mMainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator$AnimationRunner$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onAnimationCancelled$1();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onAnimationCancelled$1() {
            ActivityLaunchAnimator.this.setAnimationPending(false);
            ActivityLaunchAnimator.this.mCallback.onLaunchAnimationCancelled();
        }
    }

    public static class ExpandAnimationParameters {
        int bottom;
        int left;
        public float linearProgress;
        int parentStartClipTopAmount;
        int right;
        int startClipTopAmount;
        int[] startPosition;
        float startTranslationZ;
        int top;

        public int getTop() {
            return this.top;
        }

        public int getBottom() {
            return this.bottom;
        }

        public int getWidth() {
            return this.right - this.left;
        }

        public int getHeight() {
            return this.bottom - this.top;
        }

        public int getTopChange() {
            int i = this.startClipTopAmount;
            return Math.min((this.top - this.startPosition[1]) - (((float) i) != 0.0f ? (int) MathUtils.lerp(0.0f, i, Interpolators.FAST_OUT_SLOW_IN.getInterpolation(this.linearProgress)) : 0), 0);
        }

        public float getProgress() {
            return this.linearProgress;
        }

        public float getProgress(long j, long j2) {
            return MathUtils.constrain(((this.linearProgress * 400.0f) - j) / j2, 0.0f, 1.0f);
        }

        public int getStartClipTopAmount() {
            return this.startClipTopAmount;
        }

        public int getParentStartClipTopAmount() {
            return this.parentStartClipTopAmount;
        }

        public float getStartTranslationZ() {
            return this.startTranslationZ;
        }
    }
}
