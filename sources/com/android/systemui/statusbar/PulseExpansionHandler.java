package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.ShadeController;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PulseExpansionHandler.kt */
/* loaded from: classes.dex */
public final class PulseExpansionHandler implements Gefingerpoken {
    public static final Companion Companion = new Companion(null);
    private static final float RUBBERBAND_FACTOR_STATIC = 0.25f;
    private static final int SPRING_BACK_ANIMATION_LENGTH_MS = 375;
    private boolean bouncerShowing;
    private final KeyguardBypassController bypassController;
    private ExpansionCallback expansionCallback;
    private final FalsingManager falsingManager;
    private final HeadsUpManagerPhone headsUpManager;
    private boolean isExpanding;
    private boolean isWakingToShadeLocked;
    private boolean leavingLockscreen;
    private boolean mDraggedFarEnough;
    private float mEmptyDragAmount;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private final int mMinDragDistance;
    private final PowerManager mPowerManager;
    private boolean mPulsing;
    private boolean mReachedWakeUpHeight;
    private ExpandableView mStartingChild;
    private final int[] mTemp2;
    private final float mTouchSlop;
    private float mWakeUpHeight;

    @Nullable
    private Runnable pulseExpandAbortListener;
    private boolean qsExpanded;
    private final NotificationRoundnessManager roundnessManager;
    private ShadeController shadeController;
    private NotificationStackScrollLayout stackScroller;
    private final StatusBarStateController statusBarStateController;
    private VelocityTracker velocityTracker;
    private final NotificationWakeUpCoordinator wakeUpCoordinator;

    /* compiled from: PulseExpansionHandler.kt */
    public interface ExpansionCallback {
        void setEmptyDragAmount(float f);
    }

    public PulseExpansionHandler(@NotNull Context context, @NotNull NotificationWakeUpCoordinator wakeUpCoordinator, @NotNull KeyguardBypassController bypassController, @NotNull HeadsUpManagerPhone headsUpManager, @NotNull NotificationRoundnessManager roundnessManager, @NotNull StatusBarStateController statusBarStateController, @NotNull FalsingManager falsingManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(wakeUpCoordinator, "wakeUpCoordinator");
        Intrinsics.checkParameterIsNotNull(bypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(headsUpManager, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(roundnessManager, "roundnessManager");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
        this.wakeUpCoordinator = wakeUpCoordinator;
        this.bypassController = bypassController;
        this.headsUpManager = headsUpManager;
        this.roundnessManager = roundnessManager;
        this.statusBarStateController = statusBarStateController;
        this.falsingManager = falsingManager;
        this.mTemp2 = new int[2];
        this.mMinDragDistance = context.getResources().getDimensionPixelSize(R.dimen.keyguard_drag_down_min_distance);
        Intrinsics.checkExpressionValueIsNotNull(ViewConfiguration.get(context), "ViewConfiguration.get(context)");
        this.mTouchSlop = r3.getScaledTouchSlop();
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
    }

    /* compiled from: PulseExpansionHandler.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public final boolean isExpanding() {
        return this.isExpanding;
    }

    private final void setExpanding(boolean z) {
        boolean z2 = this.isExpanding != z;
        this.isExpanding = z;
        this.bypassController.setPulseExpanding(z);
        if (z2) {
            if (z) {
                NotificationEntry topEntry = this.headsUpManager.getTopEntry();
                if (topEntry != null) {
                    this.roundnessManager.setTrackingHeadsUp(topEntry.getRow());
                }
            } else {
                this.roundnessManager.setTrackingHeadsUp(null);
                if (!this.leavingLockscreen) {
                    this.bypassController.maybePerformPendingUnlock();
                    Runnable runnable = this.pulseExpandAbortListener;
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
            this.headsUpManager.unpinAll(true);
        }
    }

    public final boolean getLeavingLockscreen() {
        return this.leavingLockscreen;
    }

    public final boolean isWakingToShadeLocked() {
        return this.isWakingToShadeLocked;
    }

    private final boolean isFalseTouch() {
        return this.falsingManager.isFalseTouch();
    }

    public final void setQsExpanded(boolean z) {
        this.qsExpanded = z;
    }

    public final void setPulseExpandAbortListener(@Nullable Runnable runnable) {
        this.pulseExpandAbortListener = runnable;
    }

    public final void setBouncerShowing(boolean z) {
        this.bouncerShowing = z;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onInterceptTouchEvent(@NotNull MotionEvent event) {
        Intrinsics.checkParameterIsNotNull(event, "event");
        return canHandleMotionEvent() && startExpansion(event);
    }

    private final boolean canHandleMotionEvent() {
        return (!this.wakeUpCoordinator.getCanShowPulsingHuns() || this.qsExpanded || this.bouncerShowing) ? false : true;
    }

    private final boolean startExpansion(MotionEvent motionEvent) {
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker == null) {
            Intrinsics.throwNpe();
        }
        velocityTracker.addMovement(motionEvent);
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDraggedFarEnough = false;
            setExpanding(false);
            this.leavingLockscreen = false;
            this.mStartingChild = null;
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
        } else if (actionMasked == 1) {
            recycleVelocityTracker();
            setExpanding(false);
        } else if (actionMasked == 2) {
            float f = y - this.mInitialTouchY;
            if (f > this.mTouchSlop && f > Math.abs(x - this.mInitialTouchX)) {
                this.falsingManager.onStartExpandingFromPulse();
                setExpanding(true);
                captureStartingChild(this.mInitialTouchX, this.mInitialTouchY);
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                this.mWakeUpHeight = this.wakeUpCoordinator.getWakeUpHeight();
                this.mReachedWakeUpHeight = false;
                return true;
            }
        } else if (actionMasked == 3) {
            recycleVelocityTracker();
            setExpanding(false);
        }
        return false;
    }

    private final void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.velocityTracker = null;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        Intrinsics.checkParameterIsNotNull(event, "event");
        boolean z = false;
        if (!canHandleMotionEvent()) {
            return false;
        }
        if (this.velocityTracker == null || !this.isExpanding || event.getActionMasked() == 0) {
            return startExpansion(event);
        }
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker == null) {
            Intrinsics.throwNpe();
        }
        velocityTracker.addMovement(event);
        float y = event.getY() - this.mInitialTouchY;
        int actionMasked = event.getActionMasked();
        if (actionMasked == 1) {
            VelocityTracker velocityTracker2 = this.velocityTracker;
            if (velocityTracker2 == null) {
                Intrinsics.throwNpe();
            }
            velocityTracker2.computeCurrentVelocity(1000);
            if (y > 0) {
                VelocityTracker velocityTracker3 = this.velocityTracker;
                if (velocityTracker3 == null) {
                    Intrinsics.throwNpe();
                }
                if (velocityTracker3.getYVelocity() > -1000 && this.statusBarStateController.getState() != 0) {
                    z = true;
                }
            }
            if (!this.falsingManager.isUnlockingDisabled() && !isFalseTouch() && z) {
                finishExpansion();
            } else {
                cancelExpansion();
            }
            recycleVelocityTracker();
        } else if (actionMasked == 2) {
            updateExpansionHeight(y);
        } else if (actionMasked == 3) {
            cancelExpansion();
            recycleVelocityTracker();
        }
        return this.isExpanding;
    }

    private final void finishExpansion() {
        resetClock();
        ExpandableView expandableView = this.mStartingChild;
        if (expandableView != null) {
            if (expandableView == null) {
                Intrinsics.throwNpe();
            }
            setUserLocked(expandableView, false);
            this.mStartingChild = null;
        }
        if (this.statusBarStateController.isDozing()) {
            this.isWakingToShadeLocked = true;
            this.wakeUpCoordinator.setWillWakeUp(true);
            PowerManager powerManager = this.mPowerManager;
            if (powerManager == null) {
                Intrinsics.throwNpe();
            }
            powerManager.wakeUp(SystemClock.uptimeMillis(), 4, "com.android.systemui:PULSEDRAG");
        }
        ShadeController shadeController = this.shadeController;
        if (shadeController == null) {
            Intrinsics.throwUninitializedPropertyAccessException("shadeController");
        }
        shadeController.goToLockedShade(this.mStartingChild);
        this.leavingLockscreen = true;
        setExpanding(false);
        ExpandableView expandableView2 = this.mStartingChild;
        if (expandableView2 instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
            if (expandableNotificationRow == null) {
                Intrinsics.throwNpe();
            }
            expandableNotificationRow.onExpandedByGesture(true);
        }
    }

    private final void updateExpansionHeight(float f) {
        float fMax;
        float fMax2 = Math.max(f, 0.0f);
        if (!this.mReachedWakeUpHeight && f > this.mWakeUpHeight) {
            this.mReachedWakeUpHeight = true;
        }
        ExpandableView expandableView = this.mStartingChild;
        if (expandableView != null) {
            if (expandableView == null) {
                Intrinsics.throwNpe();
            }
            int iMin = Math.min((int) (expandableView.getCollapsedHeight() + fMax2), expandableView.getMaxContentHeight());
            expandableView.setActualHeight(iMin);
            fMax = Math.max(iMin, fMax2);
        } else {
            this.wakeUpCoordinator.setNotificationsVisibleForExpansion(f > (this.mReachedWakeUpHeight ? this.mWakeUpHeight : 0.0f), true, true);
            fMax = Math.max(this.mWakeUpHeight, fMax2);
        }
        setEmptyDragAmount(this.wakeUpCoordinator.setPulseHeight(fMax) * RUBBERBAND_FACTOR_STATIC);
    }

    private final void captureStartingChild(float f, float f2) {
        if (this.mStartingChild != null || this.bypassController.getBypassEnabled()) {
            return;
        }
        ExpandableView expandableViewFindView = findView(f, f2);
        this.mStartingChild = expandableViewFindView;
        if (expandableViewFindView != null) {
            if (expandableViewFindView == null) {
                Intrinsics.throwNpe();
            }
            setUserLocked(expandableViewFindView, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setEmptyDragAmount(float f) {
        this.mEmptyDragAmount = f;
        ExpansionCallback expansionCallback = this.expansionCallback;
        if (expansionCallback == null) {
            Intrinsics.throwUninitializedPropertyAccessException("expansionCallback");
        }
        expansionCallback.setEmptyDragAmount(f);
    }

    private final void reset(final ExpandableView expandableView) {
        if (expandableView.getActualHeight() == expandableView.getCollapsedHeight()) {
            setUserLocked(expandableView, false);
            return;
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(expandableView, "actualHeight", expandableView.getActualHeight(), expandableView.getCollapsedHeight());
        Intrinsics.checkExpressionValueIsNotNull(anim, "anim");
        anim.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        anim.setDuration(SPRING_BACK_ANIMATION_LENGTH_MS);
        anim.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.PulseExpansionHandler.reset.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@NotNull Animator animation) {
                Intrinsics.checkParameterIsNotNull(animation, "animation");
                PulseExpansionHandler.this.setUserLocked(expandableView, false);
            }
        });
        anim.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setUserLocked(ExpandableView expandableView, boolean z) {
        if (expandableView instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) expandableView).setUserLocked(z);
        }
    }

    private final void resetClock() {
        ValueAnimator anim = ValueAnimator.ofFloat(this.mEmptyDragAmount, 0.0f);
        Intrinsics.checkExpressionValueIsNotNull(anim, "anim");
        anim.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        anim.setDuration(SPRING_BACK_ANIMATION_LENGTH_MS);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.PulseExpansionHandler.resetClock.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator animation) {
                PulseExpansionHandler pulseExpansionHandler = PulseExpansionHandler.this;
                Intrinsics.checkExpressionValueIsNotNull(animation, "animation");
                Object animatedValue = animation.getAnimatedValue();
                if (animatedValue == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
                }
                pulseExpansionHandler.setEmptyDragAmount(((Float) animatedValue).floatValue());
            }
        });
        anim.start();
    }

    private final void cancelExpansion() {
        setExpanding(false);
        this.falsingManager.onExpansionFromPulseStopped();
        ExpandableView expandableView = this.mStartingChild;
        if (expandableView != null) {
            if (expandableView == null) {
                Intrinsics.throwNpe();
            }
            reset(expandableView);
            this.mStartingChild = null;
        } else {
            resetClock();
        }
        this.wakeUpCoordinator.setNotificationsVisibleForExpansion(false, true, false);
    }

    private final ExpandableView findView(float f, float f2) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.stackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("stackScroller");
        }
        notificationStackScrollLayout.getLocationOnScreen(this.mTemp2);
        int[] iArr = this.mTemp2;
        float f3 = f + iArr[0];
        float f4 = f2 + iArr[1];
        NotificationStackScrollLayout notificationStackScrollLayout2 = this.stackScroller;
        if (notificationStackScrollLayout2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("stackScroller");
        }
        ExpandableView childAtRawPosition = notificationStackScrollLayout2.getChildAtRawPosition(f3, f4);
        if (childAtRawPosition == null || !childAtRawPosition.isContentExpandable()) {
            return null;
        }
        return childAtRawPosition;
    }

    public final void setUp(@NotNull NotificationStackScrollLayout stackScroller, @NotNull ExpansionCallback expansionCallback, @NotNull ShadeController shadeController) {
        Intrinsics.checkParameterIsNotNull(stackScroller, "stackScroller");
        Intrinsics.checkParameterIsNotNull(expansionCallback, "expansionCallback");
        Intrinsics.checkParameterIsNotNull(shadeController, "shadeController");
        this.expansionCallback = expansionCallback;
        this.shadeController = shadeController;
        this.stackScroller = stackScroller;
    }

    public final void setPulsing(boolean z) {
        this.mPulsing = z;
    }

    public final void onStartedWakingUp() {
        this.isWakingToShadeLocked = false;
    }
}
