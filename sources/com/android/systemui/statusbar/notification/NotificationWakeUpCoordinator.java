package com.android.systemui.statusbar.notification;

import android.animation.ObjectAnimator;
import android.util.FloatProperty;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.PanelExpansionListener;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationWakeUpCoordinator.kt */
/* loaded from: classes.dex */
public final class NotificationWakeUpCoordinator implements OnHeadsUpChangedListener, StatusBarStateController.StateListener, PanelExpansionListener {
    private final KeyguardBypassController bypassController;
    private boolean collapsedEnoughToHide;
    private final DozeParameters dozeParameters;
    private boolean fullyAwake;

    @NotNull
    public NotificationIconAreaController iconAreaController;
    private float mDozeAmount;
    private final Set<NotificationEntry> mEntrySetToClearWhenFinished;
    private final HeadsUpManager mHeadsUpManager;
    private float mLinearDozeAmount;
    private float mLinearVisibilityAmount;
    private final NotificationWakeUpCoordinator$mNotificationVisibility$1 mNotificationVisibility;
    private float mNotificationVisibleAmount;
    private boolean mNotificationsVisible;
    private boolean mNotificationsVisibleForExpansion;
    private NotificationStackScrollLayout mStackScroller;
    private float mVisibilityAmount;
    private ObjectAnimator mVisibilityAnimator;
    private Interpolator mVisibilityInterpolator;
    private boolean notificationsFullyHidden;
    private boolean pulseExpanding;
    private boolean pulsing;
    private int state;
    private final StatusBarStateController statusBarStateController;
    private final ArrayList<WakeUpListener> wakeUpListeners;
    private boolean wakingUp;
    private boolean willWakeUp;

    /* compiled from: NotificationWakeUpCoordinator.kt */
    public interface WakeUpListener {
        default void onFullyHiddenChanged(boolean z) {
        }

        default void onPulseExpansionChanged(boolean z) {
        }
    }

    /* JADX WARN: Type inference failed for: r4v1, types: [com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator$mNotificationVisibility$1] */
    public NotificationWakeUpCoordinator(@NotNull HeadsUpManager mHeadsUpManager, @NotNull StatusBarStateController statusBarStateController, @NotNull KeyguardBypassController bypassController, @NotNull DozeParameters dozeParameters) {
        Intrinsics.checkParameterIsNotNull(mHeadsUpManager, "mHeadsUpManager");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(bypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(dozeParameters, "dozeParameters");
        this.mHeadsUpManager = mHeadsUpManager;
        this.statusBarStateController = statusBarStateController;
        this.bypassController = bypassController;
        this.dozeParameters = dozeParameters;
        final String str = "notificationVisibility";
        this.mNotificationVisibility = new FloatProperty<NotificationWakeUpCoordinator>(str) { // from class: com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator$mNotificationVisibility$1
            @Override // android.util.FloatProperty
            public void setValue(@NotNull NotificationWakeUpCoordinator coordinator, float f) {
                Intrinsics.checkParameterIsNotNull(coordinator, "coordinator");
                coordinator.setVisibilityAmount(f);
            }

            @Override // android.util.Property
            @Nullable
            public Float get(@NotNull NotificationWakeUpCoordinator coordinator) {
                Intrinsics.checkParameterIsNotNull(coordinator, "coordinator");
                return Float.valueOf(coordinator.mLinearVisibilityAmount);
            }
        };
        this.mVisibilityInterpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
        this.mEntrySetToClearWhenFinished = new LinkedHashSet();
        this.wakeUpListeners = new ArrayList<>();
        this.state = 1;
        mHeadsUpManager.addListener(this);
        statusBarStateController.addCallback(this);
        addListener(new WakeUpListener() { // from class: com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.1
            @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
            public void onFullyHiddenChanged(boolean z) {
                if (z && NotificationWakeUpCoordinator.this.mNotificationsVisibleForExpansion) {
                    NotificationWakeUpCoordinator.this.setNotificationsVisibleForExpansion(false, false, false);
                }
            }
        });
    }

    public final void setFullyAwake(boolean z) {
        this.fullyAwake = z;
    }

    public final void setWakingUp(boolean z) {
        this.wakingUp = z;
        setWillWakeUp(false);
        if (z) {
            if (this.mNotificationsVisible && !this.mNotificationsVisibleForExpansion && !this.bypassController.getBypassEnabled()) {
                NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
                if (notificationStackScrollLayout == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
                }
                notificationStackScrollLayout.wakeUpFromPulse();
            }
            if (!this.bypassController.getBypassEnabled() || this.mNotificationsVisible) {
                return;
            }
            updateNotificationVisibility(shouldAnimateVisibility(), false);
        }
    }

    public final void setWillWakeUp(boolean z) {
        if (z && this.mDozeAmount == 0.0f) {
            return;
        }
        this.willWakeUp = z;
    }

    public final void setIconAreaController(@NotNull NotificationIconAreaController notificationIconAreaController) {
        Intrinsics.checkParameterIsNotNull(notificationIconAreaController, "<set-?>");
        this.iconAreaController = notificationIconAreaController;
    }

    public final void setPulsing(boolean z) {
        this.pulsing = z;
        if (z) {
            updateNotificationVisibility(shouldAnimateVisibility(), false);
        }
    }

    public final boolean getNotificationsFullyHidden() {
        return this.notificationsFullyHidden;
    }

    private final void setNotificationsFullyHidden(boolean z) {
        if (this.notificationsFullyHidden != z) {
            this.notificationsFullyHidden = z;
            Iterator<WakeUpListener> it = this.wakeUpListeners.iterator();
            while (it.hasNext()) {
                it.next().onFullyHiddenChanged(z);
            }
        }
    }

    public final boolean getCanShowPulsingHuns() {
        boolean z = this.pulsing;
        if (!this.bypassController.getBypassEnabled()) {
            return z;
        }
        boolean z2 = z || ((this.wakingUp || this.willWakeUp || this.fullyAwake) && this.statusBarStateController.getState() == 1);
        if (this.collapsedEnoughToHide) {
            return false;
        }
        return z2;
    }

    public final void setStackScroller(@NotNull NotificationStackScrollLayout stackScroller) {
        Intrinsics.checkParameterIsNotNull(stackScroller, "stackScroller");
        this.mStackScroller = stackScroller;
        this.pulseExpanding = stackScroller.isPulseExpanding();
        stackScroller.setOnPulseHeightChangedListener(new Runnable() { // from class: com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.setStackScroller.1
            @Override // java.lang.Runnable
            public final void run() {
                boolean zIsPulseExpanding = NotificationWakeUpCoordinator.this.isPulseExpanding();
                boolean z = zIsPulseExpanding != NotificationWakeUpCoordinator.this.pulseExpanding;
                NotificationWakeUpCoordinator.this.pulseExpanding = zIsPulseExpanding;
                Iterator it = NotificationWakeUpCoordinator.this.wakeUpListeners.iterator();
                while (it.hasNext()) {
                    ((WakeUpListener) it.next()).onPulseExpansionChanged(z);
                }
            }
        });
    }

    public final boolean isPulseExpanding() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        }
        return notificationStackScrollLayout.isPulseExpanding();
    }

    public final void setNotificationsVisibleForExpansion(boolean z, boolean z2, boolean z3) {
        this.mNotificationsVisibleForExpansion = z;
        updateNotificationVisibility(z2, z3);
        if (z || !this.mNotificationsVisible) {
            return;
        }
        this.mHeadsUpManager.releaseAllImmediately();
    }

    public final void addListener(@NotNull WakeUpListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.wakeUpListeners.add(listener);
    }

    public final void removeListener(@NotNull WakeUpListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.wakeUpListeners.remove(listener);
    }

    private final void updateNotificationVisibility(boolean z, boolean z2) {
        boolean z3 = false;
        if ((this.mNotificationsVisibleForExpansion || this.mHeadsUpManager.hasNotifications()) && getCanShowPulsingHuns()) {
            z3 = true;
        }
        if (z3 || !this.mNotificationsVisible || (!(this.wakingUp || this.willWakeUp) || this.mDozeAmount == 0.0f)) {
            setNotificationsVisible(z3, z, z2);
        }
    }

    private final void setNotificationsVisible(boolean z, boolean z2, boolean z3) {
        if (this.mNotificationsVisible == z) {
            return;
        }
        this.mNotificationsVisible = z;
        ObjectAnimator objectAnimator = this.mVisibilityAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        if (z2) {
            notifyAnimationStart(z);
            startVisibilityAnimation(z3);
        } else {
            setVisibilityAmount(z ? 1.0f : 0.0f);
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozeAmountChanged(float f, float f2) {
        if (updateDozeAmountIfBypass()) {
            return;
        }
        if (f != 1.0f && f != 0.0f) {
            float f3 = this.mLinearDozeAmount;
            if (f3 == 0.0f || f3 == 1.0f) {
                notifyAnimationStart(f3 == 1.0f);
            }
        }
        setDozeAmount(f, f2);
    }

    public final void setDozeAmount(float f, float f2) {
        boolean z = f != this.mLinearDozeAmount;
        this.mLinearDozeAmount = f;
        this.mDozeAmount = f2;
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        }
        notificationStackScrollLayout.setDozeAmount(this.mDozeAmount);
        updateHideAmount();
        if (z && f == 0.0f) {
            setNotificationsVisible(false, false, false);
            setNotificationsVisibleForExpansion(false, false, false);
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        updateDozeAmountIfBypass();
        if (this.bypassController.getBypassEnabled() && i == 1 && this.state == 2 && (!this.statusBarStateController.isDozing() || shouldAnimateVisibility())) {
            setNotificationsVisible(true, false, false);
            setNotificationsVisible(false, true, false);
        }
        this.state = i;
    }

    @Override // com.android.systemui.statusbar.phone.PanelExpansionListener
    public void onPanelExpansionChanged(float f, boolean z) {
        boolean z2 = f <= 0.9f;
        if (z2 != this.collapsedEnoughToHide) {
            boolean canShowPulsingHuns = getCanShowPulsingHuns();
            this.collapsedEnoughToHide = z2;
            if (!canShowPulsingHuns || getCanShowPulsingHuns()) {
                return;
            }
            updateNotificationVisibility(true, true);
            this.mHeadsUpManager.releaseAllImmediately();
        }
    }

    private final boolean updateDozeAmountIfBypass() {
        if (!this.bypassController.getBypassEnabled()) {
            return false;
        }
        float f = (this.statusBarStateController.getState() == 0 || this.statusBarStateController.getState() == 2) ? 0.0f : 1.0f;
        setDozeAmount(f, f);
        return true;
    }

    private final void startVisibilityAnimation(boolean z) {
        Interpolator interpolator;
        float f = this.mNotificationVisibleAmount;
        if (f == 0.0f || f == 1.0f) {
            if (this.mNotificationsVisible) {
                interpolator = Interpolators.TOUCH_RESPONSE;
            } else {
                interpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
            }
            this.mVisibilityInterpolator = interpolator;
        }
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this, this.mNotificationVisibility, this.mNotificationsVisible ? 1.0f : 0.0f);
        objectAnimatorOfFloat.setInterpolator(Interpolators.LINEAR);
        long j = 500;
        if (z) {
            j = (long) (j / 1.5f);
        }
        objectAnimatorOfFloat.setDuration(j);
        objectAnimatorOfFloat.start();
        this.mVisibilityAnimator = objectAnimatorOfFloat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setVisibilityAmount(float f) {
        this.mLinearVisibilityAmount = f;
        this.mVisibilityAmount = this.mVisibilityInterpolator.getInterpolation(f);
        handleAnimationFinished();
        updateHideAmount();
    }

    private final void handleAnimationFinished() {
        if (this.mLinearDozeAmount == 0.0f || this.mLinearVisibilityAmount == 0.0f) {
            Iterator<T> it = this.mEntrySetToClearWhenFinished.iterator();
            while (it.hasNext()) {
                ((NotificationEntry) it.next()).setHeadsUpAnimatingAway(false);
            }
            this.mEntrySetToClearWhenFinished.clear();
        }
    }

    public final float getWakeUpHeight() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        }
        return notificationStackScrollLayout.getWakeUpHeight();
    }

    private final void updateHideAmount() {
        float fMin = Math.min(1.0f - this.mLinearVisibilityAmount, this.mLinearDozeAmount);
        float fMin2 = Math.min(1.0f - this.mVisibilityAmount, this.mDozeAmount);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        }
        notificationStackScrollLayout.setHideAmount(fMin, fMin2);
        setNotificationsFullyHidden(fMin == 1.0f);
    }

    private final void notifyAnimationStart(boolean z) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        }
        notificationStackScrollLayout.notifyHideAnimationStart(!z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        if (z) {
            setNotificationsVisible(false, false, false);
        }
    }

    public final float setPulseHeight(float f) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        }
        float pulseHeight = notificationStackScrollLayout.setPulseHeight(f);
        if (this.bypassController.getBypassEnabled()) {
            return 0.0f;
        }
        return pulseHeight;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(@NotNull NotificationEntry entry, boolean z) {
        Intrinsics.checkParameterIsNotNull(entry, "entry");
        boolean zShouldAnimateVisibility = shouldAnimateVisibility();
        if (z) {
            if (this.mEntrySetToClearWhenFinished.contains(entry)) {
                this.mEntrySetToClearWhenFinished.remove(entry);
                entry.setHeadsUpAnimatingAway(false);
            }
        } else if (this.mLinearDozeAmount != 0.0f && this.mLinearVisibilityAmount != 0.0f) {
            if (entry.isRowDismissed()) {
                zShouldAnimateVisibility = false;
            } else if (!this.wakingUp && !this.willWakeUp) {
                entry.setHeadsUpAnimatingAway(true);
                this.mEntrySetToClearWhenFinished.add(entry);
            }
        }
        updateNotificationVisibility(zShouldAnimateVisibility, false);
    }

    private final boolean shouldAnimateVisibility() {
        return this.dozeParameters.getAlwaysOn() && !this.dozeParameters.getDisplayNeedsBlanking();
    }
}
