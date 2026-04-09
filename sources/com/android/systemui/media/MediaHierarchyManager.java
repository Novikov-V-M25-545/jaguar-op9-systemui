package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewOverlay;
import android.view.ViewRootImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.animation.UniqueObjectHostView;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHierarchyManager.kt */
/* loaded from: classes.dex */
public final class MediaHierarchyManager {
    public static final Companion Companion = new Companion(null);
    private boolean animationPending;
    private Rect animationStartBounds;
    private ValueAnimator animator;
    private final KeyguardBypassController bypassController;
    private boolean collapsingShadeFromQS;
    private final Context context;
    private int currentAttachmentLocation;
    private Rect currentBounds;
    private int desiredLocation;
    private boolean dozeAnimationRunning;
    private boolean fullyAwake;
    private boolean goingToSleep;
    private final KeyguardStateController keyguardStateController;
    private final MediaCarouselController mediaCarouselController;
    private final MediaHost[] mediaHosts;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private int previousLocation;
    private float qsExpansion;
    private ViewGroupOverlay rootOverlay;
    private View rootView;
    private final Runnable startAnimation;
    private final SysuiStatusBarStateController statusBarStateController;
    private int statusbarState;
    private Rect targetBounds;

    public MediaHierarchyManager(@NotNull Context context, @NotNull SysuiStatusBarStateController statusBarStateController, @NotNull KeyguardStateController keyguardStateController, @NotNull KeyguardBypassController bypassController, @NotNull MediaCarouselController mediaCarouselController, @NotNull NotificationLockscreenUserManager notifLockscreenUserManager, @NotNull WakefulnessLifecycle wakefulnessLifecycle) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(bypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(mediaCarouselController, "mediaCarouselController");
        Intrinsics.checkParameterIsNotNull(notifLockscreenUserManager, "notifLockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(wakefulnessLifecycle, "wakefulnessLifecycle");
        this.context = context;
        this.statusBarStateController = statusBarStateController;
        this.keyguardStateController = keyguardStateController;
        this.bypassController = bypassController;
        this.mediaCarouselController = mediaCarouselController;
        this.notifLockscreenUserManager = notifLockscreenUserManager;
        this.currentBounds = new Rect();
        this.animationStartBounds = new Rect();
        this.targetBounds = new Rect();
        this.statusbarState = statusBarStateController.getState();
        final ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.media.MediaHierarchyManager$$special$$inlined$apply$lambda$1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.updateTargetState();
                MediaHierarchyManager mediaHierarchyManager = this;
                mediaHierarchyManager.interpolateBounds(mediaHierarchyManager.animationStartBounds, this.targetBounds, valueAnimatorOfFloat.getAnimatedFraction(), this.currentBounds);
                MediaHierarchyManager mediaHierarchyManager2 = this;
                MediaHierarchyManager.applyState$default(mediaHierarchyManager2, mediaHierarchyManager2.currentBounds, false, 2, null);
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.media.MediaHierarchyManager$$special$$inlined$apply$lambda$2
            private boolean cancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(@Nullable Animator animator) {
                this.cancelled = true;
                this.this$0.animationPending = false;
                View view = this.this$0.rootView;
                if (view != null) {
                    view.removeCallbacks(this.this$0.startAnimation);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator) {
                if (this.cancelled) {
                    return;
                }
                this.this$0.applyTargetStateIfNotAnimating();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(@Nullable Animator animator) {
                this.cancelled = false;
                this.this$0.animationPending = false;
            }
        });
        this.animator = valueAnimatorOfFloat;
        this.mediaHosts = new MediaHost[3];
        this.previousLocation = -1;
        this.desiredLocation = -1;
        this.currentAttachmentLocation = -1;
        this.startAnimation = new Runnable() { // from class: com.android.systemui.media.MediaHierarchyManager$startAnimation$1
            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.animator.start();
            }
        };
        statusBarStateController.addCallback(new StatusBarStateController.StateListener() { // from class: com.android.systemui.media.MediaHierarchyManager.1
            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStatePreChange(int i, int i2) {
                MediaHierarchyManager.this.statusbarState = i2;
                MediaHierarchyManager.updateDesiredLocation$default(MediaHierarchyManager.this, false, 1, null);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                MediaHierarchyManager.this.updateTargetState();
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozeAmountChanged(float f, float f2) {
                MediaHierarchyManager.this.setDozeAnimationRunning((f == 0.0f || f == 1.0f) ? false : true);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozingChanged(boolean z) {
                if (!z) {
                    MediaHierarchyManager.this.setDozeAnimationRunning(false);
                } else {
                    MediaHierarchyManager.updateDesiredLocation$default(MediaHierarchyManager.this, false, 1, null);
                }
            }
        });
        wakefulnessLifecycle.addObserver(new WakefulnessLifecycle.Observer() { // from class: com.android.systemui.media.MediaHierarchyManager.2
            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onFinishedGoingToSleep() {
                MediaHierarchyManager.this.setGoingToSleep(false);
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onStartedGoingToSleep() {
                MediaHierarchyManager.this.setGoingToSleep(true);
                MediaHierarchyManager.this.setFullyAwake(false);
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onFinishedWakingUp() {
                MediaHierarchyManager.this.setGoingToSleep(false);
                MediaHierarchyManager.this.setFullyAwake(true);
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onStartedWakingUp() {
                MediaHierarchyManager.this.setGoingToSleep(false);
            }
        });
    }

    private final ViewGroup getMediaFrame() {
        return this.mediaCarouselController.getMediaFrame();
    }

    public final void setQsExpansion(float f) {
        if (this.qsExpansion != f) {
            this.qsExpansion = f;
            updateDesiredLocation$default(this, false, 1, null);
            if (getQSTransformationProgress() >= 0) {
                updateTargetState();
                applyTargetStateIfNotAnimating();
            }
        }
    }

    public final void setCollapsingShadeFromQS(boolean z) {
        if (this.collapsingShadeFromQS != z) {
            this.collapsingShadeFromQS = z;
            updateDesiredLocation(true);
        }
    }

    private final boolean getBlockLocationChanges() {
        return this.goingToSleep || this.dozeAnimationRunning;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setGoingToSleep(boolean z) {
        if (this.goingToSleep != z) {
            this.goingToSleep = z;
            if (z) {
                return;
            }
            updateDesiredLocation$default(this, false, 1, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setFullyAwake(boolean z) {
        if (this.fullyAwake != z) {
            this.fullyAwake = z;
            if (z) {
                updateDesiredLocation(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setDozeAnimationRunning(boolean z) {
        if (this.dozeAnimationRunning != z) {
            this.dozeAnimationRunning = z;
            if (z) {
                return;
            }
            updateDesiredLocation$default(this, false, 1, null);
        }
    }

    @NotNull
    public final UniqueObjectHostView register(@NotNull MediaHost mediaObject) {
        Intrinsics.checkParameterIsNotNull(mediaObject, "mediaObject");
        UniqueObjectHostView uniqueObjectHostViewCreateUniqueObjectHost = createUniqueObjectHost();
        mediaObject.setHostView(uniqueObjectHostViewCreateUniqueObjectHost);
        mediaObject.addVisibilityChangeListener(new Function1<Boolean, Unit>() { // from class: com.android.systemui.media.MediaHierarchyManager.register.1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
                invoke(bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(boolean z) {
                MediaHierarchyManager.this.updateDesiredLocation(true);
            }
        });
        this.mediaHosts[mediaObject.getLocation()] = mediaObject;
        if (mediaObject.getLocation() == this.desiredLocation) {
            this.desiredLocation = -1;
        }
        if (mediaObject.getLocation() == this.currentAttachmentLocation) {
            this.currentAttachmentLocation = -1;
        }
        updateDesiredLocation$default(this, false, 1, null);
        return uniqueObjectHostViewCreateUniqueObjectHost;
    }

    public final void closeGuts() {
        this.mediaCarouselController.closeGuts();
    }

    private final UniqueObjectHostView createUniqueObjectHost() {
        final UniqueObjectHostView uniqueObjectHostView = new UniqueObjectHostView(this.context);
        uniqueObjectHostView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.media.MediaHierarchyManager.createUniqueObjectHost.1
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(@Nullable View view) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(@Nullable View view) {
                if (MediaHierarchyManager.this.rootOverlay == null) {
                    MediaHierarchyManager mediaHierarchyManager = MediaHierarchyManager.this;
                    ViewRootImpl viewRootImpl = uniqueObjectHostView.getViewRootImpl();
                    Intrinsics.checkExpressionValueIsNotNull(viewRootImpl, "viewHost.viewRootImpl");
                    mediaHierarchyManager.rootView = viewRootImpl.getView();
                    MediaHierarchyManager mediaHierarchyManager2 = MediaHierarchyManager.this;
                    View view2 = mediaHierarchyManager2.rootView;
                    if (view2 == null) {
                        Intrinsics.throwNpe();
                    }
                    ViewOverlay overlay = view2.getOverlay();
                    if (overlay == null) {
                        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroupOverlay");
                    }
                    mediaHierarchyManager2.rootOverlay = (ViewGroupOverlay) overlay;
                }
                uniqueObjectHostView.removeOnAttachStateChangeListener(this);
            }
        });
        return uniqueObjectHostView;
    }

    static /* synthetic */ void updateDesiredLocation$default(MediaHierarchyManager mediaHierarchyManager, boolean z, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        mediaHierarchyManager.updateDesiredLocation(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateDesiredLocation(boolean z) {
        int iCalculateLocation = calculateLocation();
        int i = this.desiredLocation;
        if (iCalculateLocation != i) {
            if (i >= 0) {
                this.previousLocation = i;
            }
            boolean z2 = i == -1;
            this.desiredLocation = iCalculateLocation;
            boolean z3 = !z && shouldAnimateTransition(iCalculateLocation, this.previousLocation);
            Pair<Long, Long> animationParams = getAnimationParams(this.previousLocation, iCalculateLocation);
            this.mediaCarouselController.onDesiredLocationChanged(iCalculateLocation, getHost(iCalculateLocation), z3, animationParams.component1().longValue(), animationParams.component2().longValue());
            performTransitionToNewLocation(z2, z3);
        }
    }

    private final void performTransitionToNewLocation(boolean z, boolean z2) {
        View view;
        if (this.previousLocation < 0 || z) {
            cancelAnimationAndApplyDesiredState();
            return;
        }
        MediaHost host = getHost(this.desiredLocation);
        MediaHost host2 = getHost(this.previousLocation);
        if (host == null || host2 == null) {
            cancelAnimationAndApplyDesiredState();
            return;
        }
        updateTargetState();
        if (isCurrentlyInGuidedTransformation()) {
            applyTargetStateIfNotAnimating();
            return;
        }
        if (z2) {
            this.animator.cancel();
            if (this.currentAttachmentLocation != this.previousLocation || !host2.getHostView().isAttachedToWindow()) {
                this.animationStartBounds.set(this.currentBounds);
            } else {
                this.animationStartBounds.set(host2.getCurrentBounds());
            }
            adjustAnimatorForTransition(this.desiredLocation, this.previousLocation);
            if (this.animationPending || (view = this.rootView) == null) {
                return;
            }
            this.animationPending = true;
            view.postOnAnimation(this.startAnimation);
            return;
        }
        cancelAnimationAndApplyDesiredState();
    }

    private final boolean shouldAnimateTransition(int i, int i2) {
        if (isCurrentlyInGuidedTransformation()) {
            return false;
        }
        if (i2 == 2 && this.desiredLocation == 1 && this.statusbarState == 0) {
            return false;
        }
        if (i == 1 && i2 == 2 && (this.statusBarStateController.leaveOpenOnKeyguardHide() || this.statusbarState == 2)) {
            return true;
        }
        if (!MediaHierarchyManagerKt.isShownNotFaded(getMediaFrame())) {
            ValueAnimator animator = this.animator;
            Intrinsics.checkExpressionValueIsNotNull(animator, "animator");
            if (!animator.isRunning() && !this.animationPending) {
                return false;
            }
        }
        return true;
    }

    private final void adjustAnimatorForTransition(int i, int i2) {
        Pair<Long, Long> animationParams = getAnimationParams(i2, i);
        long jLongValue = animationParams.component1().longValue();
        long jLongValue2 = animationParams.component2().longValue();
        ValueAnimator valueAnimator = this.animator;
        valueAnimator.setDuration(jLongValue);
        valueAnimator.setStartDelay(jLongValue2);
    }

    private final Pair<Long, Long> getAnimationParams(int i, int i2) {
        long j;
        int i3;
        long keyguardFadingAwayDelay = 0;
        if (i == 2 && i2 == 1) {
            if (this.statusbarState == 0 && this.keyguardStateController.isKeyguardFadingAway()) {
                keyguardFadingAwayDelay = this.keyguardStateController.getKeyguardFadingAwayDelay();
            }
            i3 = 448;
        } else {
            if (i != 1 || i2 != 2) {
                j = 200;
                return TuplesKt.to(Long.valueOf(j), Long.valueOf(keyguardFadingAwayDelay));
            }
            i3 = 464;
        }
        j = i3;
        return TuplesKt.to(Long.valueOf(j), Long.valueOf(keyguardFadingAwayDelay));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void applyTargetStateIfNotAnimating() {
        ValueAnimator animator = this.animator;
        Intrinsics.checkExpressionValueIsNotNull(animator, "animator");
        if (animator.isRunning()) {
            return;
        }
        applyState$default(this, this.targetBounds, false, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateTargetState() {
        Rect currentBounds;
        if (isCurrentlyInGuidedTransformation()) {
            float transformationProgress = getTransformationProgress();
            MediaHost host = getHost(this.desiredLocation);
            if (host == null) {
                Intrinsics.throwNpe();
            }
            MediaHost host2 = getHost(this.previousLocation);
            if (host2 == null) {
                Intrinsics.throwNpe();
            }
            if (!host.getVisible()) {
                host = host2;
            } else if (!host2.getVisible()) {
                host2 = host;
            }
            this.targetBounds = interpolateBounds$default(this, host2.getCurrentBounds(), host.getCurrentBounds(), transformationProgress, null, 8, null);
            return;
        }
        MediaHost host3 = getHost(this.desiredLocation);
        if (host3 == null || (currentBounds = host3.getCurrentBounds()) == null) {
            return;
        }
        this.targetBounds.set(currentBounds);
    }

    static /* synthetic */ Rect interpolateBounds$default(MediaHierarchyManager mediaHierarchyManager, Rect rect, Rect rect2, float f, Rect rect3, int i, Object obj) {
        if ((i & 8) != 0) {
            rect3 = null;
        }
        return mediaHierarchyManager.interpolateBounds(rect, rect2, f, rect3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Rect interpolateBounds(Rect rect, Rect rect2, float f, Rect rect3) {
        int iLerp = (int) MathUtils.lerp(rect.left, rect2.left, f);
        int iLerp2 = (int) MathUtils.lerp(rect.top, rect2.top, f);
        int iLerp3 = (int) MathUtils.lerp(rect.right, rect2.right, f);
        int iLerp4 = (int) MathUtils.lerp(rect.bottom, rect2.bottom, f);
        if (rect3 == null) {
            rect3 = new Rect();
        }
        rect3.set(iLerp, iLerp2, iLerp3, iLerp4);
        return rect3;
    }

    private final boolean isCurrentlyInGuidedTransformation() {
        return getTransformationProgress() >= ((float) 0);
    }

    private final float getTransformationProgress() {
        float qSTransformationProgress = getQSTransformationProgress();
        if (qSTransformationProgress >= 0) {
            return qSTransformationProgress;
        }
        return -1.0f;
    }

    private final float getQSTransformationProgress() {
        MediaHost host = getHost(this.desiredLocation);
        MediaHost host2 = getHost(this.previousLocation);
        if (host == null || host.getLocation() != 0 || host2 == null || host2.getLocation() != 1) {
            return -1.0f;
        }
        if (host2.getVisible() || this.statusbarState != 1) {
            return this.qsExpansion;
        }
        return -1.0f;
    }

    private final MediaHost getHost(int i) {
        if (i < 0) {
            return null;
        }
        return this.mediaHosts[i];
    }

    private final void cancelAnimationAndApplyDesiredState() {
        this.animator.cancel();
        MediaHost host = getHost(this.desiredLocation);
        if (host != null) {
            applyState(host.getCurrentBounds(), true);
        }
    }

    static /* synthetic */ void applyState$default(MediaHierarchyManager mediaHierarchyManager, Rect rect, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        mediaHierarchyManager.applyState(rect, z);
    }

    private final void applyState(Rect rect, boolean z) {
        this.currentBounds.set(rect);
        boolean zIsCurrentlyInGuidedTransformation = isCurrentlyInGuidedTransformation();
        this.mediaCarouselController.setCurrentState(zIsCurrentlyInGuidedTransformation ? this.previousLocation : -1, this.desiredLocation, zIsCurrentlyInGuidedTransformation ? getTransformationProgress() : 1.0f, z);
        updateHostAttachment();
        if (this.currentAttachmentLocation == -1000) {
            ViewGroup mediaFrame = getMediaFrame();
            Rect rect2 = this.currentBounds;
            mediaFrame.setLeftTopRightBottom(rect2.left, rect2.top, rect2.right, rect2.bottom);
        }
    }

    private final void updateHostAttachment() {
        boolean z = isTransitionRunning() && this.rootOverlay != null;
        int i = z ? -1000 : this.desiredLocation;
        if (this.currentAttachmentLocation != i) {
            this.currentAttachmentLocation = i;
            ViewGroup viewGroup = (ViewGroup) getMediaFrame().getParent();
            if (viewGroup != null) {
                viewGroup.removeView(getMediaFrame());
            }
            MediaHost host = getHost(this.desiredLocation);
            if (host == null) {
                Intrinsics.throwNpe();
            }
            UniqueObjectHostView hostView = host.getHostView();
            if (z) {
                ViewGroupOverlay viewGroupOverlay = this.rootOverlay;
                if (viewGroupOverlay == null) {
                    Intrinsics.throwNpe();
                }
                viewGroupOverlay.add(getMediaFrame());
                return;
            }
            hostView.addView(getMediaFrame());
            int paddingLeft = hostView.getPaddingLeft();
            int paddingTop = hostView.getPaddingTop();
            getMediaFrame().setLeftTopRightBottom(paddingLeft, paddingTop, this.currentBounds.width() + paddingLeft, this.currentBounds.height() + paddingTop);
        }
    }

    private final boolean isTransitionRunning() {
        if (!isCurrentlyInGuidedTransformation() || getTransformationProgress() == 1.0f) {
            ValueAnimator animator = this.animator;
            Intrinsics.checkExpressionValueIsNotNull(animator, "animator");
            if (!animator.isRunning() && !this.animationPending) {
                return false;
            }
        }
        return true;
    }

    private final int calculateLocation() {
        MediaHost host;
        int i;
        if (getBlockLocationChanges()) {
            return this.desiredLocation;
        }
        boolean z = !this.bypassController.getBypassEnabled() && ((i = this.statusbarState) == 1 || i == 3);
        boolean zShouldShowLockscreenNotifications = this.notifLockscreenUserManager.shouldShowLockscreenNotifications();
        float f = this.qsExpansion;
        int i2 = ((f <= 0.0f || z) && (f <= 0.4f || !z)) ? (z && zShouldShowLockscreenNotifications) ? 2 : 1 : 0;
        if (i2 == 2 && (((host = getHost(i2)) == null || !host.getVisible()) && !this.statusBarStateController.isDozing())) {
            return 0;
        }
        if (i2 == 2 && this.desiredLocation == 0 && this.collapsingShadeFromQS) {
            return 0;
        }
        if (i2 == 2 || this.desiredLocation != 2 || this.fullyAwake) {
            return i2;
        }
        return 2;
    }

    /* compiled from: MediaHierarchyManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }
}
