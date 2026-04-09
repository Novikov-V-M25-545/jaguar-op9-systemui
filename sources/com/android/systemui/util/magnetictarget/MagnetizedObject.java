package com.android.systemui.util.magnetictarget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.PointF;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function5;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MagnetizedObject.kt */
/* loaded from: classes.dex */
public abstract class MagnetizedObject<T> {
    public static final Companion Companion = new Companion(null);
    private static boolean hapticSettingObserverInitialized;
    private static boolean systemHapticsEnabled;

    @NotNull
    private Function5<? super MagneticTarget, ? super Float, ? super Float, ? super Boolean, ? super Function0<Unit>, Unit> animateStuckToTarget;
    private final PhysicsAnimator<T> animator;
    private final ArrayList<MagneticTarget> associatedTargets;

    @NotNull
    private final Context context;
    private boolean flingToTargetEnabled;
    private float flingToTargetMinVelocity;
    private float flingToTargetWidthPercent;
    private float flingUnstuckFromTargetMinVelocity;

    @NotNull
    private PhysicsAnimator.SpringConfig flungIntoTargetSpringConfig;
    private boolean hapticsEnabled;

    @NotNull
    public MagnetListener magnetListener;
    private boolean movedBeyondSlop;
    private final int[] objectLocationOnScreen;

    @Nullable
    private PhysicsAnimator.EndListener<T> physicsAnimatorEndListener;

    @Nullable
    private PhysicsAnimator.UpdateListener<T> physicsAnimatorUpdateListener;

    @NotNull
    private PhysicsAnimator.SpringConfig springConfig;
    private float stickToTargetMaxXVelocity;
    private MagneticTarget targetObjectIsStuckTo;
    private PointF touchDown;
    private int touchSlop;

    @NotNull
    private final T underlyingObject;
    private final VelocityTracker velocityTracker;
    private final Vibrator vibrator;

    @NotNull
    private final FloatPropertyCompat<? super T> xProperty;

    @NotNull
    private final FloatPropertyCompat<? super T> yProperty;

    /* compiled from: MagnetizedObject.kt */
    public interface MagnetListener {
        void onReleasedInTarget(@NotNull MagneticTarget magneticTarget);

        void onStuckToTarget(@NotNull MagneticTarget magneticTarget);

        void onUnstuckFromTarget(@NotNull MagneticTarget magneticTarget, float f, float f2, boolean z);
    }

    public abstract float getHeight(@NotNull T t);

    public abstract void getLocationOnScreen(@NotNull T t, @NotNull int[] iArr);

    public abstract float getWidth(@NotNull T t);

    public MagnetizedObject(@NotNull Context context, @NotNull T underlyingObject, @NotNull FloatPropertyCompat<? super T> xProperty, @NotNull FloatPropertyCompat<? super T> yProperty) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(underlyingObject, "underlyingObject");
        Intrinsics.checkParameterIsNotNull(xProperty, "xProperty");
        Intrinsics.checkParameterIsNotNull(yProperty, "yProperty");
        this.context = context;
        this.underlyingObject = underlyingObject;
        this.xProperty = xProperty;
        this.yProperty = yProperty;
        this.animator = PhysicsAnimator.Companion.getInstance(underlyingObject);
        this.objectLocationOnScreen = new int[2];
        this.associatedTargets = new ArrayList<>();
        VelocityTracker velocityTrackerObtain = VelocityTracker.obtain();
        Intrinsics.checkExpressionValueIsNotNull(velocityTrackerObtain, "VelocityTracker.obtain()");
        this.velocityTracker = velocityTrackerObtain;
        Object systemService = context.getSystemService("vibrator");
        if (systemService == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.os.Vibrator");
        }
        this.vibrator = (Vibrator) systemService;
        this.touchDown = new PointF();
        this.animateStuckToTarget = new MagnetizedObject$animateStuckToTarget$1(this);
        this.flingToTargetEnabled = true;
        this.flingToTargetWidthPercent = 3.0f;
        this.flingToTargetMinVelocity = 4000.0f;
        this.flingUnstuckFromTargetMinVelocity = 4000.0f;
        this.stickToTargetMaxXVelocity = 2000.0f;
        this.hapticsEnabled = true;
        PhysicsAnimator.SpringConfig springConfig = new PhysicsAnimator.SpringConfig(1500.0f, 1.0f);
        this.springConfig = springConfig;
        this.flungIntoTargetSpringConfig = springConfig;
        Companion.initHapticSettingObserver(context);
    }

    @NotNull
    public final T getUnderlyingObject() {
        return this.underlyingObject;
    }

    public final boolean getObjectStuckToTarget() {
        return this.targetObjectIsStuckTo != null;
    }

    @NotNull
    public final MagnetListener getMagnetListener() {
        MagnetListener magnetListener = this.magnetListener;
        if (magnetListener == null) {
            Intrinsics.throwUninitializedPropertyAccessException("magnetListener");
        }
        return magnetListener;
    }

    public final void setMagnetListener(@NotNull MagnetListener magnetListener) {
        Intrinsics.checkParameterIsNotNull(magnetListener, "<set-?>");
        this.magnetListener = magnetListener;
    }

    public final void setAnimateStuckToTarget(@NotNull Function5<? super MagneticTarget, ? super Float, ? super Float, ? super Boolean, ? super Function0<Unit>, Unit> function5) {
        Intrinsics.checkParameterIsNotNull(function5, "<set-?>");
        this.animateStuckToTarget = function5;
    }

    public final float getFlingToTargetWidthPercent() {
        return this.flingToTargetWidthPercent;
    }

    public final void setFlingToTargetWidthPercent(float f) {
        this.flingToTargetWidthPercent = f;
    }

    public final float getFlingToTargetMinVelocity() {
        return this.flingToTargetMinVelocity;
    }

    public final void setFlingToTargetMinVelocity(float f) {
        this.flingToTargetMinVelocity = f;
    }

    public final float getStickToTargetMaxXVelocity() {
        return this.stickToTargetMaxXVelocity;
    }

    public final void setStickToTargetMaxXVelocity(float f) {
        this.stickToTargetMaxXVelocity = f;
    }

    public final void setHapticsEnabled(boolean z) {
        this.hapticsEnabled = z;
    }

    public final void addTarget(@NotNull MagneticTarget target) {
        Intrinsics.checkParameterIsNotNull(target, "target");
        this.associatedTargets.add(target);
        target.updateLocationOnScreen();
    }

    @NotNull
    public final MagneticTarget addTarget(@NotNull View target, int i) {
        Intrinsics.checkParameterIsNotNull(target, "target");
        MagneticTarget magneticTarget = new MagneticTarget(target, i);
        addTarget(magneticTarget);
        return magneticTarget;
    }

    public final boolean maybeConsumeMotionEvent(@NotNull MotionEvent ev) {
        T next;
        Intrinsics.checkParameterIsNotNull(ev, "ev");
        if (this.associatedTargets.size() == 0) {
            return false;
        }
        MagneticTarget magneticTarget = null;
        if (ev.getAction() == 0) {
            updateTargetViews$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
            this.velocityTracker.clear();
            this.targetObjectIsStuckTo = null;
            this.touchDown.set(ev.getRawX(), ev.getRawY());
            this.movedBeyondSlop = false;
        }
        addMovement(ev);
        if (!this.movedBeyondSlop) {
            if (((float) Math.hypot(ev.getRawX() - this.touchDown.x, ev.getRawY() - this.touchDown.y)) <= this.touchSlop) {
                return false;
            }
            this.movedBeyondSlop = true;
        }
        Iterator<T> it = this.associatedTargets.iterator();
        while (true) {
            if (!it.hasNext()) {
                next = (T) null;
                break;
            }
            next = it.next();
            MagneticTarget magneticTarget2 = (MagneticTarget) next;
            if (((float) Math.hypot((double) (ev.getRawX() - magneticTarget2.getCenterOnScreen().x), (double) (ev.getRawY() - magneticTarget2.getCenterOnScreen().y))) < ((float) magneticTarget2.getMagneticFieldRadiusPx())) {
                break;
            }
        }
        MagneticTarget magneticTarget3 = next;
        boolean z = (getObjectStuckToTarget() || magneticTarget3 == null) ? false : true;
        boolean z2 = getObjectStuckToTarget() && magneticTarget3 != null && (Intrinsics.areEqual(this.targetObjectIsStuckTo, magneticTarget3) ^ true);
        if (z || z2) {
            this.velocityTracker.computeCurrentVelocity(1000);
            float xVelocity = this.velocityTracker.getXVelocity();
            float yVelocity = this.velocityTracker.getYVelocity();
            if (z && Math.abs(xVelocity) > this.stickToTargetMaxXVelocity) {
                return false;
            }
            this.targetObjectIsStuckTo = magneticTarget3;
            cancelAnimations$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
            MagnetListener magnetListener = this.magnetListener;
            if (magnetListener == null) {
                Intrinsics.throwUninitializedPropertyAccessException("magnetListener");
            }
            if (magneticTarget3 == null) {
                Intrinsics.throwNpe();
            }
            magnetListener.onStuckToTarget(magneticTarget3);
            this.animateStuckToTarget.invoke(magneticTarget3, Float.valueOf(xVelocity), Float.valueOf(yVelocity), Boolean.FALSE, null);
            vibrateIfEnabled(5);
        } else if (magneticTarget3 == null && getObjectStuckToTarget()) {
            this.velocityTracker.computeCurrentVelocity(1000);
            cancelAnimations$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
            MagnetListener magnetListener2 = this.magnetListener;
            if (magnetListener2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("magnetListener");
            }
            MagneticTarget magneticTarget4 = this.targetObjectIsStuckTo;
            if (magneticTarget4 == null) {
                Intrinsics.throwNpe();
            }
            magnetListener2.onUnstuckFromTarget(magneticTarget4, this.velocityTracker.getXVelocity(), this.velocityTracker.getYVelocity(), false);
            this.targetObjectIsStuckTo = null;
            vibrateIfEnabled(2);
        }
        if (ev.getAction() == 1) {
            this.velocityTracker.computeCurrentVelocity(1000);
            float xVelocity2 = this.velocityTracker.getXVelocity();
            float yVelocity2 = this.velocityTracker.getYVelocity();
            cancelAnimations$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
            if (!getObjectStuckToTarget()) {
                Iterator<T> it2 = this.associatedTargets.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    T next2 = it2.next();
                    if (isForcefulFlingTowardsTarget((MagneticTarget) next2, ev.getRawX(), ev.getRawY(), xVelocity2, yVelocity2)) {
                        magneticTarget = next2;
                        break;
                    }
                }
                final MagneticTarget magneticTarget5 = magneticTarget;
                if (magneticTarget5 == null) {
                    return false;
                }
                MagnetListener magnetListener3 = this.magnetListener;
                if (magnetListener3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("magnetListener");
                }
                magnetListener3.onStuckToTarget(magneticTarget5);
                this.targetObjectIsStuckTo = magneticTarget5;
                this.animateStuckToTarget.invoke(magneticTarget5, Float.valueOf(xVelocity2), Float.valueOf(yVelocity2), Boolean.TRUE, new Function0<Unit>() { // from class: com.android.systemui.util.magnetictarget.MagnetizedObject.maybeConsumeMotionEvent.1
                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(0);
                    }

                    @Override // kotlin.jvm.functions.Function0
                    public /* bridge */ /* synthetic */ Unit invoke() {
                        invoke2();
                        return Unit.INSTANCE;
                    }

                    /* renamed from: invoke, reason: avoid collision after fix types in other method */
                    public final void invoke2() {
                        MagnetizedObject.this.getMagnetListener().onReleasedInTarget(magneticTarget5);
                        MagnetizedObject.this.targetObjectIsStuckTo = null;
                        MagnetizedObject.this.vibrateIfEnabled(5);
                    }
                });
                return true;
            }
            if ((-yVelocity2) > this.flingUnstuckFromTargetMinVelocity) {
                MagnetListener magnetListener4 = this.magnetListener;
                if (magnetListener4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("magnetListener");
                }
                MagneticTarget magneticTarget6 = this.targetObjectIsStuckTo;
                if (magneticTarget6 == null) {
                    Intrinsics.throwNpe();
                }
                magnetListener4.onUnstuckFromTarget(magneticTarget6, xVelocity2, yVelocity2, true);
            } else {
                MagnetListener magnetListener5 = this.magnetListener;
                if (magnetListener5 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("magnetListener");
                }
                MagneticTarget magneticTarget7 = this.targetObjectIsStuckTo;
                if (magneticTarget7 == null) {
                    Intrinsics.throwNpe();
                }
                magnetListener5.onReleasedInTarget(magneticTarget7);
                vibrateIfEnabled(5);
            }
            this.targetObjectIsStuckTo = null;
            return true;
        }
        return getObjectStuckToTarget();
    }

    /* JADX INFO: Access modifiers changed from: private */
    @SuppressLint({"MissingPermission"})
    public final void vibrateIfEnabled(int i) {
        if (this.hapticsEnabled && systemHapticsEnabled) {
            this.vibrator.vibrate(VibrationEffect.createPredefined(i));
        }
    }

    private final void addMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.velocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void animateStuckToTargetInternal(MagneticTarget magneticTarget, float f, float f2, boolean z, Function0<Unit> function0) {
        magneticTarget.updateLocationOnScreen();
        getLocationOnScreen(this.underlyingObject, this.objectLocationOnScreen);
        float width = (magneticTarget.getCenterOnScreen().x - (getWidth(this.underlyingObject) / 2.0f)) - this.objectLocationOnScreen[0];
        float height = (magneticTarget.getCenterOnScreen().y - (getHeight(this.underlyingObject) / 2.0f)) - this.objectLocationOnScreen[1];
        PhysicsAnimator.SpringConfig springConfig = z ? this.flungIntoTargetSpringConfig : this.springConfig;
        cancelAnimations$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
        PhysicsAnimator<T> physicsAnimator = this.animator;
        FloatPropertyCompat<? super T> floatPropertyCompat = this.xProperty;
        PhysicsAnimator<T> physicsAnimatorSpring = physicsAnimator.spring(floatPropertyCompat, floatPropertyCompat.getValue(this.underlyingObject) + width, f, springConfig);
        FloatPropertyCompat<? super T> floatPropertyCompat2 = this.yProperty;
        physicsAnimatorSpring.spring(floatPropertyCompat2, floatPropertyCompat2.getValue(this.underlyingObject) + height, f2, springConfig);
        PhysicsAnimator.UpdateListener<T> updateListener = this.physicsAnimatorUpdateListener;
        if (updateListener != null) {
            PhysicsAnimator<T> physicsAnimator2 = this.animator;
            if (updateListener == null) {
                Intrinsics.throwNpe();
            }
            physicsAnimator2.addUpdateListener(updateListener);
        }
        PhysicsAnimator.EndListener<T> endListener = this.physicsAnimatorEndListener;
        if (endListener != null) {
            PhysicsAnimator<T> physicsAnimator3 = this.animator;
            if (endListener == null) {
                Intrinsics.throwNpe();
            }
            physicsAnimator3.addEndListener(endListener);
        }
        if (function0 != null) {
            this.animator.withEndActions(function0);
        }
        this.animator.start();
    }

    private final boolean isForcefulFlingTowardsTarget(MagneticTarget magneticTarget, float f, float f2, float f3, float f4) {
        if (!this.flingToTargetEnabled) {
            return false;
        }
        if (!(f2 >= magneticTarget.getCenterOnScreen().y ? f4 < this.flingToTargetMinVelocity : f4 > this.flingToTargetMinVelocity)) {
            return false;
        }
        if (f3 != 0.0f) {
            float f5 = f4 / f3;
            f = (magneticTarget.getCenterOnScreen().y - (f2 - (f * f5))) / f5;
        }
        float width = (magneticTarget.getTargetView().getWidth() * this.flingToTargetWidthPercent) / 2;
        return f > magneticTarget.getCenterOnScreen().x - width && f < magneticTarget.getCenterOnScreen().x + width;
    }

    public final void cancelAnimations$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        this.animator.cancel(this.xProperty, this.yProperty);
    }

    public final void updateTargetViews$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        Iterator<T> it = this.associatedTargets.iterator();
        while (it.hasNext()) {
            ((MagneticTarget) it.next()).updateLocationOnScreen();
        }
        if (this.associatedTargets.size() > 0) {
            ViewConfiguration viewConfiguration = ViewConfiguration.get(this.associatedTargets.get(0).getTargetView().getContext());
            Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(as…ts[0].targetView.context)");
            this.touchSlop = viewConfiguration.getScaledTouchSlop();
        }
    }

    /* compiled from: MagnetizedObject.kt */
    public static final class MagneticTarget {

        @NotNull
        private final PointF centerOnScreen;
        private int magneticFieldRadiusPx;

        @NotNull
        private final View targetView;
        private final int[] tempLoc;

        public MagneticTarget(@NotNull View targetView, int i) {
            Intrinsics.checkParameterIsNotNull(targetView, "targetView");
            this.targetView = targetView;
            this.magneticFieldRadiusPx = i;
            this.centerOnScreen = new PointF();
            this.tempLoc = new int[2];
        }

        @NotNull
        public final View getTargetView() {
            return this.targetView;
        }

        public final int getMagneticFieldRadiusPx() {
            return this.magneticFieldRadiusPx;
        }

        public final void setMagneticFieldRadiusPx(int i) {
            this.magneticFieldRadiusPx = i;
        }

        @NotNull
        public final PointF getCenterOnScreen() {
            return this.centerOnScreen;
        }

        public final void updateLocationOnScreen() {
            this.targetView.post(new Runnable() { // from class: com.android.systemui.util.magnetictarget.MagnetizedObject$MagneticTarget$updateLocationOnScreen$1
                @Override // java.lang.Runnable
                public final void run() {
                    this.this$0.getTargetView().getLocationOnScreen(this.this$0.tempLoc);
                    this.this$0.getCenterOnScreen().set((this.this$0.tempLoc[0] + (this.this$0.getTargetView().getWidth() / 2.0f)) - this.this$0.getTargetView().getTranslationX(), (this.this$0.tempLoc[1] + (this.this$0.getTargetView().getHeight() / 2.0f)) - this.this$0.getTargetView().getTranslationY());
                }
            });
        }
    }

    /* compiled from: MagnetizedObject.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final void initHapticSettingObserver(final Context context) {
            if (MagnetizedObject.hapticSettingObserverInitialized) {
                return;
            }
            final Handler main = Handler.getMain();
            ContentObserver contentObserver = new ContentObserver(main) { // from class: com.android.systemui.util.magnetictarget.MagnetizedObject$Companion$initHapticSettingObserver$hapticSettingObserver$1
                @Override // android.database.ContentObserver
                public void onChange(boolean z) {
                    MagnetizedObject.systemHapticsEnabled = Settings.System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 0, -2) != 0;
                }
            };
            context.getContentResolver().registerContentObserver(Settings.System.getUriFor("haptic_feedback_enabled"), true, contentObserver);
            contentObserver.onChange(false);
            MagnetizedObject.hapticSettingObserverInitialized = true;
        }
    }
}
