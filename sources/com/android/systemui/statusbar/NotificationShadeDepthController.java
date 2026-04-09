package com.android.systemui.statusbar;

import android.animation.Animator;
import android.app.WallpaperManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.MathUtils;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewRootImpl;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.PanelExpansionListener;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationShadeDepthController.kt */
/* loaded from: classes.dex */
public final class NotificationShadeDepthController implements PanelExpansionListener, Dumpable {
    public static final Companion Companion = new Companion(null);
    private final BiometricUnlockController biometricUnlockController;
    private View blurRoot;
    private final BlurUtils blurUtils;

    @NotNull
    private DepthAnimation brightnessMirrorSpring;
    private boolean brightnessMirrorVisible;
    private final Choreographer choreographer;
    private final DozeParameters dozeParameters;

    @NotNull
    private DepthAnimation globalActionsSpring;
    private boolean ignoreShadeBlurUntilHidden;
    private boolean isBlurred;
    private boolean isClosed;
    private boolean isOpen;
    private Animator keyguardAnimator;
    private final NotificationShadeDepthController$keyguardStateCallback$1 keyguardStateCallback;
    private final KeyguardStateController keyguardStateController;
    private List<DepthListener> listeners;
    private Animator notificationAnimator;

    @Nullable
    private ActivityLaunchAnimator.ExpandAnimationParameters notificationLaunchAnimationParams;
    private final NotificationShadeWindowController notificationShadeWindowController;
    private int prevShadeDirection;
    private float prevShadeVelocity;
    private long prevTimestamp;
    private boolean prevTracking;

    @NotNull
    public View root;
    private boolean scrimsVisible;

    @NotNull
    private DepthAnimation shadeAnimation;
    private float shadeExpansion;

    @NotNull
    private DepthAnimation shadeSpring;
    private boolean showingHomeControls;
    private final NotificationShadeDepthController$statusBarStateCallback$1 statusBarStateCallback;
    private final StatusBarStateController statusBarStateController;

    @NotNull
    private final Choreographer.FrameCallback updateBlurCallback;
    private boolean updateScheduled;
    private int wakeAndUnlockBlurRadius;
    private final WallpaperManager wallpaperManager;

    /* compiled from: NotificationShadeDepthController.kt */
    public interface DepthListener {
        void onWallpaperZoomOutChanged(float f);
    }

    public static /* synthetic */ void brightnessMirrorSpring$annotations() {
    }

    public static /* synthetic */ void globalActionsSpring$annotations() {
    }

    public static /* synthetic */ void shadeSpring$annotations() {
    }

    public static /* synthetic */ void updateBlurCallback$annotations() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v10, types: [com.android.systemui.plugins.statusbar.StatusBarStateController$StateListener, com.android.systemui.statusbar.NotificationShadeDepthController$statusBarStateCallback$1] */
    public NotificationShadeDepthController(@NotNull StatusBarStateController statusBarStateController, @NotNull BlurUtils blurUtils, @NotNull BiometricUnlockController biometricUnlockController, @NotNull KeyguardStateController keyguardStateController, @NotNull Choreographer choreographer, @NotNull WallpaperManager wallpaperManager, @NotNull NotificationShadeWindowController notificationShadeWindowController, @NotNull DozeParameters dozeParameters, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(blurUtils, "blurUtils");
        Intrinsics.checkParameterIsNotNull(biometricUnlockController, "biometricUnlockController");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(choreographer, "choreographer");
        Intrinsics.checkParameterIsNotNull(wallpaperManager, "wallpaperManager");
        Intrinsics.checkParameterIsNotNull(notificationShadeWindowController, "notificationShadeWindowController");
        Intrinsics.checkParameterIsNotNull(dozeParameters, "dozeParameters");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.statusBarStateController = statusBarStateController;
        this.blurUtils = blurUtils;
        this.biometricUnlockController = biometricUnlockController;
        this.keyguardStateController = keyguardStateController;
        this.choreographer = choreographer;
        this.wallpaperManager = wallpaperManager;
        this.notificationShadeWindowController = notificationShadeWindowController;
        this.dozeParameters = dozeParameters;
        this.isClosed = true;
        this.listeners = new ArrayList();
        this.prevTimestamp = -1L;
        this.shadeSpring = new DepthAnimation();
        this.shadeAnimation = new DepthAnimation();
        this.globalActionsSpring = new DepthAnimation();
        this.brightnessMirrorSpring = new DepthAnimation();
        this.updateBlurCallback = new Choreographer.FrameCallback() { // from class: com.android.systemui.statusbar.NotificationShadeDepthController$updateBlurCallback$1
            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j) {
                ViewRootImpl viewRootImpl;
                this.this$0.updateScheduled = false;
                float fMax = Math.max((int) ((this.this$0.getShadeSpring().getRadius() * 0.4f) + (MathUtils.constrain(this.this$0.getShadeAnimation().getRadius(), this.this$0.blurUtils.getMinBlurRadius(), this.this$0.blurUtils.getMaxBlurRadius()) * 0.6f)), this.this$0.wakeAndUnlockBlurRadius) * (1.0f - this.this$0.getBrightnessMirrorSpring().getRatio());
                ActivityLaunchAnimator.ExpandAnimationParameters notificationLaunchAnimationParams = this.this$0.getNotificationLaunchAnimationParams();
                float f = 0.0f;
                float f2 = 1.0f - (notificationLaunchAnimationParams != null ? notificationLaunchAnimationParams.linearProgress : 0.0f);
                float f3 = fMax * f2 * f2;
                if (!this.this$0.ignoreShadeBlurUntilHidden) {
                    f = f3;
                } else if (f3 == 0.0f) {
                    this.this$0.ignoreShadeBlurUntilHidden = false;
                    f = f3;
                }
                int iMax = this.this$0.scrimsVisible ? 0 : Math.max((int) f, this.this$0.getGlobalActionsSpring().getRadius());
                BlurUtils blurUtils2 = this.this$0.blurUtils;
                View view = this.this$0.blurRoot;
                if (view == null || (viewRootImpl = view.getViewRootImpl()) == null) {
                    viewRootImpl = this.this$0.getRoot().getViewRootImpl();
                }
                blurUtils2.applyBlur(viewRootImpl, iMax);
                float fRatioOfBlurRadius = this.this$0.blurUtils.ratioOfBlurRadius(iMax);
                try {
                    this.this$0.wallpaperManager.setWallpaperZoomOut(this.this$0.getRoot().getWindowToken(), fRatioOfBlurRadius);
                } catch (IllegalArgumentException e) {
                    Log.w("DepthController", "Can't set zoom. Window is gone: " + this.this$0.getRoot().getWindowToken(), e);
                }
                Iterator it = this.this$0.listeners.iterator();
                while (it.hasNext()) {
                    ((NotificationShadeDepthController.DepthListener) it.next()).onWallpaperZoomOutChanged(fRatioOfBlurRadius);
                }
                this.this$0.notificationShadeWindowController.setBackgroundBlurRadius(iMax);
            }
        };
        this.keyguardStateCallback = new NotificationShadeDepthController$keyguardStateCallback$1(this);
        ?? r3 = new StatusBarStateController.StateListener() { // from class: com.android.systemui.statusbar.NotificationShadeDepthController$statusBarStateCallback$1
            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                NotificationShadeDepthController notificationShadeDepthController = this.this$0;
                notificationShadeDepthController.updateShadeAnimationBlur(notificationShadeDepthController.shadeExpansion, this.this$0.prevTracking, this.this$0.prevShadeVelocity, this.this$0.prevShadeDirection);
                this.this$0.updateShadeBlur();
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozingChanged(boolean z) {
                if (z) {
                    this.this$0.getShadeSpring().finishIfRunning();
                    this.this$0.getShadeAnimation().finishIfRunning();
                    this.this$0.getGlobalActionsSpring().finishIfRunning();
                    this.this$0.getBrightnessMirrorSpring().finishIfRunning();
                }
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozeAmountChanged(float f, float f2) {
                NotificationShadeDepthController notificationShadeDepthController = this.this$0;
                notificationShadeDepthController.setWakeAndUnlockBlurRadius(notificationShadeDepthController.blurUtils.blurRadiusOfRatio(f2));
            }
        };
        this.statusBarStateCallback = r3;
        String name = NotificationShadeDepthController.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
        statusBarStateController.addCallback(r3);
        notificationShadeWindowController.setScrimsVisibilityListener(new Consumer<Integer>() { // from class: com.android.systemui.statusbar.NotificationShadeDepthController.1
            @Override // java.util.function.Consumer
            public final void accept(Integer num) {
                NotificationShadeDepthController.this.setScrimsVisible(num != null && num.intValue() == 2);
            }
        });
        this.shadeAnimation.setStiffness(200.0f);
        this.shadeAnimation.setDampingRatio(1.0f);
    }

    /* compiled from: NotificationShadeDepthController.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @NotNull
    public final View getRoot() {
        View view = this.root;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("root");
        }
        return view;
    }

    public final void setRoot(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "<set-?>");
        this.root = view;
    }

    @NotNull
    public final DepthAnimation getShadeSpring() {
        return this.shadeSpring;
    }

    @NotNull
    public final DepthAnimation getShadeAnimation() {
        return this.shadeAnimation;
    }

    @NotNull
    public final DepthAnimation getGlobalActionsSpring() {
        return this.globalActionsSpring;
    }

    public final void setShowingHomeControls(boolean z) {
        this.showingHomeControls = z;
    }

    @NotNull
    public final DepthAnimation getBrightnessMirrorSpring() {
        return this.brightnessMirrorSpring;
    }

    public final void setBrightnessMirrorVisible(boolean z) {
        this.brightnessMirrorVisible = z;
        DepthAnimation.animateTo$default(this.brightnessMirrorSpring, z ? this.blurUtils.blurRadiusOfRatio(1.0f) : 0, null, 2, null);
    }

    @Nullable
    public final ActivityLaunchAnimator.ExpandAnimationParameters getNotificationLaunchAnimationParams() {
        return this.notificationLaunchAnimationParams;
    }

    public final void setNotificationLaunchAnimationParams(@Nullable ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.notificationLaunchAnimationParams = expandAnimationParameters;
        if (expandAnimationParameters != null) {
            scheduleUpdate$default(this, null, 1, null);
            return;
        }
        if (this.shadeSpring.getRadius() == 0 && this.shadeAnimation.getRadius() == 0) {
            return;
        }
        this.ignoreShadeBlurUntilHidden = true;
        DepthAnimation.animateTo$default(this.shadeSpring, 0, null, 2, null);
        this.shadeSpring.finishIfRunning();
        DepthAnimation.animateTo$default(this.shadeAnimation, 0, null, 2, null);
        this.shadeAnimation.finishIfRunning();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setScrimsVisible(boolean z) {
        if (this.scrimsVisible == z) {
            return;
        }
        this.scrimsVisible = z;
        scheduleUpdate$default(this, null, 1, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setWakeAndUnlockBlurRadius(int i) {
        if (this.wakeAndUnlockBlurRadius == i) {
            return;
        }
        this.wakeAndUnlockBlurRadius = i;
        scheduleUpdate$default(this, null, 1, null);
    }

    public final void addListener(@NotNull DepthListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.listeners.add(listener);
    }

    @Override // com.android.systemui.statusbar.phone.PanelExpansionListener
    public void onPanelExpansionChanged(float f, boolean z) {
        long jElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        if (this.shadeExpansion == f && this.prevTracking == z) {
            this.prevTimestamp = jElapsedRealtimeNanos;
            return;
        }
        float fConstrain = 1.0f;
        if (this.prevTimestamp < 0) {
            this.prevTimestamp = jElapsedRealtimeNanos;
        } else {
            fConstrain = MathUtils.constrain((float) ((jElapsedRealtimeNanos - r2) / 1.0E9d), 1.0E-5f, 1.0f);
        }
        float f2 = f - this.shadeExpansion;
        int iSignum = (int) Math.signum(f2);
        float fConstrain2 = MathUtils.constrain((f2 * 100.0f) / fConstrain, -3000.0f, 3000.0f);
        updateShadeAnimationBlur(f, z, fConstrain2, iSignum);
        this.prevShadeDirection = iSignum;
        this.prevShadeVelocity = fConstrain2;
        this.shadeExpansion = f;
        this.prevTracking = z;
        this.prevTimestamp = jElapsedRealtimeNanos;
        updateShadeBlur();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateShadeAnimationBlur(float f, boolean z, float f2, int i) {
        if (!isOnKeyguardNotDismissing()) {
            animateBlur(false, 0.0f);
            this.isClosed = true;
            this.isOpen = false;
            return;
        }
        if (f > 0.0f) {
            if (this.isClosed) {
                animateBlur(true, f2);
                this.isClosed = false;
            }
            if (z && !this.isBlurred) {
                animateBlur(true, 0.0f);
            }
            if (!z && i < 0 && this.isBlurred) {
                animateBlur(false, f2);
            }
            if (f == 1.0f) {
                if (this.isOpen) {
                    return;
                }
                this.isOpen = true;
                if (this.isBlurred) {
                    return;
                }
                animateBlur(true, f2);
                return;
            }
            this.isOpen = false;
            return;
        }
        if (this.isClosed) {
            return;
        }
        this.isClosed = true;
        if (this.isBlurred) {
            animateBlur(false, f2);
        }
    }

    private final void animateBlur(boolean z, float f) {
        this.isBlurred = z;
        float f2 = (z && isOnKeyguardNotDismissing()) ? 1.0f : 0.0f;
        this.shadeAnimation.setStartVelocity(f);
        DepthAnimation.animateTo$default(this.shadeAnimation, this.blurUtils.blurRadiusOfRatio(f2), null, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateShadeBlur() {
        DepthAnimation.animateTo$default(this.shadeSpring, isOnKeyguardNotDismissing() ? this.blurUtils.blurRadiusOfRatio(this.shadeExpansion) : 0, null, 2, null);
    }

    static /* synthetic */ void scheduleUpdate$default(NotificationShadeDepthController notificationShadeDepthController, View view, int i, Object obj) {
        if ((i & 1) != 0) {
            view = null;
        }
        notificationShadeDepthController.scheduleUpdate(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void scheduleUpdate(View view) {
        if (this.updateScheduled) {
            return;
        }
        this.updateScheduled = true;
        this.blurRoot = view;
        this.choreographer.postFrameCallback(this.updateBlurCallback);
    }

    private final boolean isOnKeyguardNotDismissing() {
        int state = this.statusBarStateController.getState();
        return (state == 0 || state == 2) && !this.keyguardStateController.isKeyguardFadingAway();
    }

    public final void updateGlobalDialogVisibility(float f, @Nullable View view) {
        this.globalActionsSpring.animateTo(this.blurUtils.blurRadiusOfRatio(f), view);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(pw, "  ");
        indentingPrintWriter.println("StatusBarWindowBlurController:");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("shadeRadius: " + this.shadeSpring.getRadius());
        indentingPrintWriter.println("shadeAnimation: " + this.shadeAnimation.getRadius());
        indentingPrintWriter.println("globalActionsRadius: " + this.globalActionsSpring.getRadius());
        indentingPrintWriter.println("brightnessMirrorRadius: " + this.brightnessMirrorSpring.getRadius());
        indentingPrintWriter.println("wakeAndUnlockBlur: " + this.wakeAndUnlockBlurRadius);
        StringBuilder sb = new StringBuilder();
        sb.append("notificationLaunchAnimationProgress: ");
        ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters = this.notificationLaunchAnimationParams;
        sb.append(expandAnimationParameters != null ? Float.valueOf(expandAnimationParameters.linearProgress) : null);
        indentingPrintWriter.println(sb.toString());
        indentingPrintWriter.println("ignoreShadeBlurUntilHidden: " + this.ignoreShadeBlurUntilHidden);
    }

    /* compiled from: NotificationShadeDepthController.kt */
    public final class DepthAnimation {
        private int pendingRadius = -1;
        private int radius;
        private SpringAnimation springAnimation;
        private View view;

        public DepthAnimation() {
            final String str = "blurRadius";
            SpringAnimation springAnimation = new SpringAnimation(this, new FloatPropertyCompat<DepthAnimation>(str) { // from class: com.android.systemui.statusbar.NotificationShadeDepthController$DepthAnimation$springAnimation$1
                @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
                public void setValue(@Nullable NotificationShadeDepthController.DepthAnimation depthAnimation, float f) {
                    this.this$0.setRadius((int) f);
                    NotificationShadeDepthController.DepthAnimation depthAnimation2 = this.this$0;
                    NotificationShadeDepthController.this.scheduleUpdate(depthAnimation2.view);
                }

                @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
                public float getValue(@Nullable NotificationShadeDepthController.DepthAnimation depthAnimation) {
                    return this.this$0.getRadius();
                }
            });
            this.springAnimation = springAnimation;
            springAnimation.setSpring(new SpringForce(0.0f));
            SpringForce spring = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring, "springAnimation.spring");
            spring.setDampingRatio(1.0f);
            SpringForce spring2 = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring2, "springAnimation.spring");
            spring2.setStiffness(10000.0f);
            this.springAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() { // from class: com.android.systemui.statusbar.NotificationShadeDepthController.DepthAnimation.1
                @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                public final void onAnimationEnd(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation, boolean z, float f, float f2) {
                    DepthAnimation.this.pendingRadius = -1;
                }
            });
        }

        public final int getRadius() {
            return this.radius;
        }

        public final void setRadius(int i) {
            this.radius = i;
        }

        public final float getRatio() {
            return NotificationShadeDepthController.this.blurUtils.ratioOfBlurRadius(this.radius);
        }

        public static /* synthetic */ void animateTo$default(DepthAnimation depthAnimation, int i, View view, int i2, Object obj) {
            if ((i2 & 2) != 0) {
                view = null;
            }
            depthAnimation.animateTo(i, view);
        }

        public final void animateTo(int i, @Nullable View view) {
            if (this.pendingRadius == i && Intrinsics.areEqual(this.view, view)) {
                return;
            }
            this.view = view;
            this.pendingRadius = i;
            this.springAnimation.animateToFinalPosition(i);
        }

        public final void finishIfRunning() {
            if (this.springAnimation.isRunning()) {
                this.springAnimation.skipToEnd();
            }
        }

        public final void setStiffness(float f) {
            SpringForce spring = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring, "springAnimation.spring");
            spring.setStiffness(f);
        }

        public final void setDampingRatio(float f) {
            SpringForce spring = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring, "springAnimation.spring");
            spring.setDampingRatio(f);
        }

        public final void setStartVelocity(float f) {
            this.springAnimation.setStartVelocity(f);
        }
    }
}
