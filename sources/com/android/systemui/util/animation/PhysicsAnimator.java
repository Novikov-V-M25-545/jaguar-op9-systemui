package com.android.systemui.util.animation;

import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import androidx.dynamicanimation.animation.AnimationHandler;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PhysicsAnimator.kt */
/* loaded from: classes.dex */
public final class PhysicsAnimator<T> {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static Function1<Object, ? extends PhysicsAnimator<?>> instanceConstructor = PhysicsAnimator$Companion$instanceConstructor$1.INSTANCE;

    @NotNull
    private Function1<? super Set<? extends FloatPropertyCompat<? super T>>, Unit> cancelAction;
    private AnimationHandler customAnimationHandler;
    private FlingConfig defaultFling;
    private SpringConfig defaultSpring;
    private final ArrayList<Function0<Unit>> endActions;
    private final ArrayList<EndListener<T>> endListeners;
    private final ArrayMap<FloatPropertyCompat<? super T>, FlingAnimation> flingAnimations;
    private final ArrayMap<FloatPropertyCompat<? super T>, FlingConfig> flingConfigs;

    @NotNull
    private ArrayList<PhysicsAnimator<T>.InternalListener> internalListeners;
    private final ArrayMap<FloatPropertyCompat<? super T>, SpringAnimation> springAnimations;
    private final ArrayMap<FloatPropertyCompat<? super T>, SpringConfig> springConfigs;

    @NotNull
    private Function0<Unit> startAction;
    private final ArrayList<UpdateListener<T>> updateListeners;

    @NotNull
    private final WeakReference<T> weakTarget;

    /* compiled from: PhysicsAnimator.kt */
    public interface EndListener<T> {
        void onAnimationEnd(T t, @NotNull FloatPropertyCompat<? super T> floatPropertyCompat, boolean z, boolean z2, float f, float f2, boolean z3);
    }

    /* compiled from: PhysicsAnimator.kt */
    public interface UpdateListener<T> {
        void onAnimationUpdateForProperty(T t, @NotNull ArrayMap<FloatPropertyCompat<? super T>, AnimationUpdate> arrayMap);
    }

    public static final float estimateFlingEndValue(float f, float f2, @NotNull FlingConfig flingConfig) {
        return Companion.estimateFlingEndValue(f, f2, flingConfig);
    }

    @NotNull
    public static final <T> PhysicsAnimator<T> getInstance(@NotNull T t) {
        return Companion.getInstance(t);
    }

    @NotNull
    public final PhysicsAnimator<T> flingThenSpring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, @NotNull FlingConfig flingConfig, @NotNull SpringConfig springConfig) {
        return flingThenSpring$default(this, floatPropertyCompat, f, flingConfig, springConfig, false, 16, null);
    }

    private PhysicsAnimator(T t) {
        this.weakTarget = new WeakReference<>(t);
        this.springAnimations = new ArrayMap<>();
        this.flingAnimations = new ArrayMap<>();
        this.springConfigs = new ArrayMap<>();
        this.flingConfigs = new ArrayMap<>();
        this.updateListeners = new ArrayList<>();
        this.endListeners = new ArrayList<>();
        this.endActions = new ArrayList<>();
        this.defaultSpring = PhysicsAnimatorKt.globalDefaultSpring;
        this.defaultFling = PhysicsAnimatorKt.globalDefaultFling;
        this.internalListeners = new ArrayList<>();
        this.startAction = new PhysicsAnimator$startAction$1(this);
        this.cancelAction = new PhysicsAnimator$cancelAction$1(this);
    }

    public /* synthetic */ PhysicsAnimator(Object obj, DefaultConstructorMarker defaultConstructorMarker) {
        this(obj);
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class AnimationUpdate {
        private final float value;
        private final float velocity;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AnimationUpdate)) {
                return false;
            }
            AnimationUpdate animationUpdate = (AnimationUpdate) obj;
            return Float.compare(this.value, animationUpdate.value) == 0 && Float.compare(this.velocity, animationUpdate.velocity) == 0;
        }

        public int hashCode() {
            return (Float.hashCode(this.value) * 31) + Float.hashCode(this.velocity);
        }

        @NotNull
        public String toString() {
            return "AnimationUpdate(value=" + this.value + ", velocity=" + this.velocity + ")";
        }

        public AnimationUpdate(float f, float f2) {
            this.value = f;
            this.velocity = f2;
        }
    }

    @NotNull
    public final ArrayList<PhysicsAnimator<T>.InternalListener> getInternalListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        return this.internalListeners;
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> property, float f, float f2, float f3, float f4) {
        Intrinsics.checkParameterIsNotNull(property, "property");
        if (PhysicsAnimatorKt.verboseLogging) {
            Log.d("PhysicsAnimator", "Springing " + Companion.getReadablePropertyName(property) + " to " + f + '.');
        }
        this.springConfigs.put(property, new SpringConfig(f3, f4, f2, f));
        return this;
    }

    public static /* synthetic */ PhysicsAnimator spring$default(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, float f, float f2, SpringConfig springConfig, int i, Object obj) {
        if ((i & 8) != 0) {
            springConfig = physicsAnimator.defaultSpring;
        }
        return physicsAnimator.spring(floatPropertyCompat, f, f2, springConfig);
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> property, float f, float f2, @NotNull SpringConfig config) {
        Intrinsics.checkParameterIsNotNull(property, "property");
        Intrinsics.checkParameterIsNotNull(config, "config");
        return spring(property, f, f2, config.getStiffness$frameworks__base__packages__SystemUI__android_common__SystemUI_core(), config.getDampingRatio$frameworks__base__packages__SystemUI__android_common__SystemUI_core());
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> property, float f, @NotNull SpringConfig config) {
        Intrinsics.checkParameterIsNotNull(property, "property");
        Intrinsics.checkParameterIsNotNull(config, "config");
        return spring(property, f, 0.0f, config);
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> property, float f) {
        Intrinsics.checkParameterIsNotNull(property, "property");
        return spring$default(this, property, f, 0.0f, null, 8, null);
    }

    public static /* synthetic */ PhysicsAnimator flingThenSpring$default(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, float f, FlingConfig flingConfig, SpringConfig springConfig, boolean z, int i, Object obj) {
        if ((i & 16) != 0) {
            z = false;
        }
        return physicsAnimator.flingThenSpring(floatPropertyCompat, f, flingConfig, springConfig, z);
    }

    @NotNull
    public final PhysicsAnimator<T> flingThenSpring(@NotNull FloatPropertyCompat<? super T> property, float f, @NotNull FlingConfig flingConfig, @NotNull SpringConfig springConfig, boolean z) {
        Intrinsics.checkParameterIsNotNull(property, "property");
        Intrinsics.checkParameterIsNotNull(flingConfig, "flingConfig");
        Intrinsics.checkParameterIsNotNull(springConfig, "springConfig");
        T t = this.weakTarget.get();
        if (t == null) {
            Log.w("PhysicsAnimator", "Trying to animate a GC-ed target.");
            return this;
        }
        FlingConfig flingConfigCopy$default = FlingConfig.copy$default(flingConfig, 0.0f, 0.0f, 0.0f, 0.0f, 15, null);
        SpringConfig springConfigCopy$default = SpringConfig.copy$default(springConfig, 0.0f, 0.0f, 0.0f, 0.0f, 15, null);
        float f2 = 0;
        float min$frameworks__base__packages__SystemUI__android_common__SystemUI_core = f < f2 ? flingConfig.getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core() : flingConfig.getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
        if (z && isValidValue(min$frameworks__base__packages__SystemUI__android_common__SystemUI_core)) {
            float value = property.getValue(t) + (f / (flingConfig.getFriction$frameworks__base__packages__SystemUI__android_common__SystemUI_core() * 4.2f));
            float min$frameworks__base__packages__SystemUI__android_common__SystemUI_core2 = (flingConfig.getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core() + flingConfig.getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core()) / 2;
            if ((f < f2 && value > min$frameworks__base__packages__SystemUI__android_common__SystemUI_core2) || (f > f2 && value < min$frameworks__base__packages__SystemUI__android_common__SystemUI_core2)) {
                float min$frameworks__base__packages__SystemUI__android_common__SystemUI_core3 = value < min$frameworks__base__packages__SystemUI__android_common__SystemUI_core2 ? flingConfig.getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core() : flingConfig.getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
                if (isValidValue(min$frameworks__base__packages__SystemUI__android_common__SystemUI_core3)) {
                    return spring(property, min$frameworks__base__packages__SystemUI__android_common__SystemUI_core3, f, springConfig);
                }
            }
            float value2 = min$frameworks__base__packages__SystemUI__android_common__SystemUI_core - property.getValue(t);
            float friction$frameworks__base__packages__SystemUI__android_common__SystemUI_core = flingConfig.getFriction$frameworks__base__packages__SystemUI__android_common__SystemUI_core() * 4.2f * value2;
            if (value2 > 0.0f && f >= 0.0f) {
                f = Math.max(friction$frameworks__base__packages__SystemUI__android_common__SystemUI_core, f);
            } else if (value2 < 0.0f && f <= 0.0f) {
                f = Math.min(friction$frameworks__base__packages__SystemUI__android_common__SystemUI_core, f);
            }
            flingConfigCopy$default.setStartVelocity$frameworks__base__packages__SystemUI__android_common__SystemUI_core(f);
            springConfigCopy$default.setFinalPosition$frameworks__base__packages__SystemUI__android_common__SystemUI_core(min$frameworks__base__packages__SystemUI__android_common__SystemUI_core);
        } else {
            flingConfigCopy$default.setStartVelocity$frameworks__base__packages__SystemUI__android_common__SystemUI_core(f);
        }
        this.flingConfigs.put(property, flingConfigCopy$default);
        this.springConfigs.put(property, springConfigCopy$default);
        return this;
    }

    private final boolean isValidValue(float f) {
        FloatCompanionObject floatCompanionObject = FloatCompanionObject.INSTANCE;
        return f < floatCompanionObject.getMAX_VALUE() && f > (-floatCompanionObject.getMAX_VALUE());
    }

    @NotNull
    public final PhysicsAnimator<T> addUpdateListener(@NotNull UpdateListener<T> listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.updateListeners.add(listener);
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> addEndListener(@NotNull EndListener<T> listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.endListeners.add(listener);
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> withEndActions(@NotNull Function0<Unit>... endActions) {
        Intrinsics.checkParameterIsNotNull(endActions, "endActions");
        this.endActions.addAll(ArraysKt___ArraysKt.filterNotNull(endActions));
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> withEndActions(@NotNull Runnable... endActions) {
        Intrinsics.checkParameterIsNotNull(endActions, "endActions");
        ArrayList<Function0<Unit>> arrayList = this.endActions;
        List listFilterNotNull = ArraysKt___ArraysKt.filterNotNull(endActions);
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(listFilterNotNull, 10));
        Iterator<T> it = listFilterNotNull.iterator();
        while (it.hasNext()) {
            arrayList2.add(new PhysicsAnimator$withEndActions$1$1((Runnable) it.next()));
        }
        arrayList.addAll(arrayList2);
        return this;
    }

    public final void setDefaultSpringConfig(@NotNull SpringConfig defaultSpring) {
        Intrinsics.checkParameterIsNotNull(defaultSpring, "defaultSpring");
        this.defaultSpring = defaultSpring;
    }

    public final void setCustomAnimationHandler(@NotNull AnimationHandler handler) {
        Intrinsics.checkParameterIsNotNull(handler, "handler");
        this.customAnimationHandler = handler;
    }

    public final void start() {
        this.startAction.invoke();
    }

    public final void startInternal$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        Looper mainLooper = Looper.getMainLooper();
        Intrinsics.checkExpressionValueIsNotNull(mainLooper, "Looper.getMainLooper()");
        if (!mainLooper.isCurrentThread()) {
            Log.e("PhysicsAnimator", "Animations can only be started on the main thread. If you are seeing this message in a test, call PhysicsAnimatorTestUtils#prepareForTest in your test setup.");
        }
        final T t = this.weakTarget.get();
        if (t == null) {
            Log.w("PhysicsAnimator", "Trying to animate a GC-ed object.");
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (final FloatPropertyCompat<? super T> floatPropertyCompat : getAnimatedProperties$frameworks__base__packages__SystemUI__android_common__SystemUI_core()) {
            final FlingConfig flingConfig = this.flingConfigs.get(floatPropertyCompat);
            final SpringConfig springConfig = this.springConfigs.get(floatPropertyCompat);
            final float value = floatPropertyCompat.getValue(t);
            if (flingConfig != null) {
                arrayList.add(new Function0<Unit>() { // from class: com.android.systemui.util.animation.PhysicsAnimator$startInternal$1
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
                        PhysicsAnimator.FlingConfig flingConfig2 = flingConfig;
                        flingConfig2.setMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core(Math.min(value, flingConfig2.getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core()));
                        flingConfig2.setMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core(Math.max(value, flingConfig2.getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core()));
                        this.this$0.cancel(floatPropertyCompat);
                        FlingAnimation flingAnimation = this.this$0.getFlingAnimation(floatPropertyCompat, t);
                        AnimationHandler animationHandler = this.this$0.customAnimationHandler;
                        if (animationHandler == null) {
                            animationHandler = flingAnimation.getAnimationHandler();
                        }
                        flingAnimation.setAnimationHandler(animationHandler);
                        flingConfig.applyToAnimation$frameworks__base__packages__SystemUI__android_common__SystemUI_core(flingAnimation);
                        flingAnimation.start();
                    }
                });
            }
            if (springConfig != null) {
                if (flingConfig == null) {
                    SpringAnimation springAnimation = getSpringAnimation(floatPropertyCompat, t);
                    if (this.customAnimationHandler != null && (!Intrinsics.areEqual(springAnimation.getAnimationHandler(), this.customAnimationHandler))) {
                        if (springAnimation.isRunning()) {
                            cancel(floatPropertyCompat);
                        }
                        AnimationHandler animationHandler = this.customAnimationHandler;
                        if (animationHandler == null) {
                            animationHandler = springAnimation.getAnimationHandler();
                        }
                        springAnimation.setAnimationHandler(animationHandler);
                    }
                    springConfig.applyToAnimation$frameworks__base__packages__SystemUI__android_common__SystemUI_core(springAnimation);
                    arrayList.add(new PhysicsAnimator$startInternal$2(springAnimation));
                } else {
                    final float min$frameworks__base__packages__SystemUI__android_common__SystemUI_core = flingConfig.getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
                    final float max$frameworks__base__packages__SystemUI__android_common__SystemUI_core = flingConfig.getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
                    this.endListeners.add(0, new EndListener<T>() { // from class: com.android.systemui.util.animation.PhysicsAnimator$startInternal$3
                        @Override // com.android.systemui.util.animation.PhysicsAnimator.EndListener
                        public void onAnimationEnd(T t2, @NotNull FloatPropertyCompat<? super T> property, boolean z, boolean z2, float f, float f2, boolean z3) {
                            Intrinsics.checkParameterIsNotNull(property, "property");
                            boolean z4 = true;
                            if ((!Intrinsics.areEqual(property, floatPropertyCompat)) || !z || z2) {
                                return;
                            }
                            float f3 = 0;
                            boolean z5 = Math.abs(f2) > f3;
                            if (f >= min$frameworks__base__packages__SystemUI__android_common__SystemUI_core && f <= max$frameworks__base__packages__SystemUI__android_common__SystemUI_core) {
                                z4 = false;
                            }
                            if (z5 || z4) {
                                springConfig.setStartVelocity$frameworks__base__packages__SystemUI__android_common__SystemUI_core(f2);
                                if (springConfig.getFinalPosition$frameworks__base__packages__SystemUI__android_common__SystemUI_core() == PhysicsAnimatorKt.UNSET) {
                                    if (z5) {
                                        springConfig.setFinalPosition$frameworks__base__packages__SystemUI__android_common__SystemUI_core(f2 < f3 ? min$frameworks__base__packages__SystemUI__android_common__SystemUI_core : max$frameworks__base__packages__SystemUI__android_common__SystemUI_core);
                                    } else if (z4) {
                                        PhysicsAnimator.SpringConfig springConfig2 = springConfig;
                                        float f4 = min$frameworks__base__packages__SystemUI__android_common__SystemUI_core;
                                        if (f >= f4) {
                                            f4 = max$frameworks__base__packages__SystemUI__android_common__SystemUI_core;
                                        }
                                        springConfig2.setFinalPosition$frameworks__base__packages__SystemUI__android_common__SystemUI_core(f4);
                                    }
                                }
                                SpringAnimation springAnimation2 = this.this$0.getSpringAnimation(floatPropertyCompat, t2);
                                AnimationHandler animationHandler2 = this.this$0.customAnimationHandler;
                                if (animationHandler2 == null) {
                                    animationHandler2 = springAnimation2.getAnimationHandler();
                                }
                                springAnimation2.setAnimationHandler(animationHandler2);
                                springConfig.applyToAnimation$frameworks__base__packages__SystemUI__android_common__SystemUI_core(springAnimation2);
                                springAnimation2.start();
                            }
                        }
                    });
                }
            }
        }
        this.internalListeners.add(new InternalListener(this, t, getAnimatedProperties$frameworks__base__packages__SystemUI__android_common__SystemUI_core(), new ArrayList(this.updateListeners), new ArrayList(this.endListeners), new ArrayList(this.endActions)));
        Iterator<T> it = arrayList.iterator();
        while (it.hasNext()) {
            ((Function0) it.next()).invoke();
        }
        clearAnimator();
    }

    private final void clearAnimator() {
        this.springConfigs.clear();
        this.flingConfigs.clear();
        this.updateListeners.clear();
        this.endListeners.clear();
        this.endActions.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final SpringAnimation getSpringAnimation(FloatPropertyCompat<? super T> floatPropertyCompat, T t) {
        ArrayMap<FloatPropertyCompat<? super T>, SpringAnimation> arrayMap = this.springAnimations;
        SpringAnimation springAnimation = arrayMap.get(floatPropertyCompat);
        if (springAnimation == null) {
            DynamicAnimation<?> dynamicAnimationConfigureDynamicAnimation = configureDynamicAnimation(new SpringAnimation(t, floatPropertyCompat), floatPropertyCompat);
            if (dynamicAnimationConfigureDynamicAnimation != null) {
                springAnimation = (SpringAnimation) dynamicAnimationConfigureDynamicAnimation;
                arrayMap.put(floatPropertyCompat, springAnimation);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type androidx.dynamicanimation.animation.SpringAnimation");
            }
        }
        Intrinsics.checkExpressionValueIsNotNull(springAnimation, "springAnimations.getOrPu…    as SpringAnimation })");
        return springAnimation;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final FlingAnimation getFlingAnimation(FloatPropertyCompat<? super T> floatPropertyCompat, T t) {
        ArrayMap<FloatPropertyCompat<? super T>, FlingAnimation> arrayMap = this.flingAnimations;
        FlingAnimation flingAnimation = arrayMap.get(floatPropertyCompat);
        if (flingAnimation == null) {
            DynamicAnimation<?> dynamicAnimationConfigureDynamicAnimation = configureDynamicAnimation(new FlingAnimation(t, floatPropertyCompat), floatPropertyCompat);
            if (dynamicAnimationConfigureDynamicAnimation != null) {
                flingAnimation = (FlingAnimation) dynamicAnimationConfigureDynamicAnimation;
                arrayMap.put(floatPropertyCompat, flingAnimation);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type androidx.dynamicanimation.animation.FlingAnimation");
            }
        }
        Intrinsics.checkExpressionValueIsNotNull(flingAnimation, "flingAnimations.getOrPut…     as FlingAnimation })");
        return flingAnimation;
    }

    private final DynamicAnimation<?> configureDynamicAnimation(final DynamicAnimation<?> dynamicAnimation, final FloatPropertyCompat<? super T> floatPropertyCompat) {
        dynamicAnimation.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() { // from class: com.android.systemui.util.animation.PhysicsAnimator.configureDynamicAnimation.1
            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
            public final void onAnimationUpdate(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation2, float f, float f2) {
                int size = PhysicsAnimator.this.getInternalListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core().size();
                for (int i = 0; i < size; i++) {
                    PhysicsAnimator.this.getInternalListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core().get(i).onInternalAnimationUpdate$frameworks__base__packages__SystemUI__android_common__SystemUI_core(floatPropertyCompat, f, f2);
                }
            }
        });
        dynamicAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() { // from class: com.android.systemui.util.animation.PhysicsAnimator.configureDynamicAnimation.2
            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
            public final void onAnimationEnd(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation2, final boolean z, final float f, final float f2) {
                CollectionsKt__MutableCollectionsKt.removeAll((List) PhysicsAnimator.this.getInternalListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core(), (Function1) new Function1<PhysicsAnimator<T>.InternalListener, Boolean>() { // from class: com.android.systemui.util.animation.PhysicsAnimator.configureDynamicAnimation.2.1
                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(1);
                    }

                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Boolean invoke(Object obj) {
                        return Boolean.valueOf(invoke((InternalListener) obj));
                    }

                    public final boolean invoke(@NotNull PhysicsAnimator<T>.InternalListener it) {
                        Intrinsics.checkParameterIsNotNull(it, "it");
                        AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                        return it.onInternalAnimationEnd$frameworks__base__packages__SystemUI__android_common__SystemUI_core(floatPropertyCompat, z, f, f2, dynamicAnimation instanceof FlingAnimation);
                    }
                });
                if (Intrinsics.areEqual((SpringAnimation) PhysicsAnimator.this.springAnimations.get(floatPropertyCompat), dynamicAnimation)) {
                    PhysicsAnimator.this.springAnimations.remove(floatPropertyCompat);
                }
                if (Intrinsics.areEqual((FlingAnimation) PhysicsAnimator.this.flingAnimations.get(floatPropertyCompat), dynamicAnimation)) {
                    PhysicsAnimator.this.flingAnimations.remove(floatPropertyCompat);
                }
            }
        });
        return dynamicAnimation;
    }

    /* compiled from: PhysicsAnimator.kt */
    public final class InternalListener {
        private List<? extends Function0<Unit>> endActions;
        private List<? extends EndListener<T>> endListeners;
        private int numPropertiesAnimating;
        private Set<? extends FloatPropertyCompat<? super T>> properties;
        private final T target;
        final /* synthetic */ PhysicsAnimator this$0;
        private final ArrayMap<FloatPropertyCompat<? super T>, AnimationUpdate> undispatchedUpdates;
        private List<? extends UpdateListener<T>> updateListeners;

        public InternalListener(PhysicsAnimator physicsAnimator, @NotNull T t, @NotNull Set<? extends FloatPropertyCompat<? super T>> properties, @NotNull List<? extends UpdateListener<T>> updateListeners, @NotNull List<? extends EndListener<T>> endListeners, List<? extends Function0<Unit>> endActions) {
            Intrinsics.checkParameterIsNotNull(properties, "properties");
            Intrinsics.checkParameterIsNotNull(updateListeners, "updateListeners");
            Intrinsics.checkParameterIsNotNull(endListeners, "endListeners");
            Intrinsics.checkParameterIsNotNull(endActions, "endActions");
            this.this$0 = physicsAnimator;
            this.target = t;
            this.properties = properties;
            this.updateListeners = updateListeners;
            this.endListeners = endListeners;
            this.endActions = endActions;
            this.numPropertiesAnimating = properties.size();
            this.undispatchedUpdates = new ArrayMap<>();
        }

        public final void onInternalAnimationUpdate$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull FloatPropertyCompat<? super T> property, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(property, "property");
            if (this.properties.contains(property)) {
                this.undispatchedUpdates.put(property, new AnimationUpdate(f, f2));
                maybeDispatchUpdates();
            }
        }

        public final boolean onInternalAnimationEnd$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull FloatPropertyCompat<? super T> property, boolean z, float f, float f2, boolean z2) {
            Intrinsics.checkParameterIsNotNull(property, "property");
            if (!this.properties.contains(property)) {
                return false;
            }
            this.numPropertiesAnimating--;
            maybeDispatchUpdates();
            if (this.undispatchedUpdates.containsKey(property)) {
                Iterator<T> it = this.updateListeners.iterator();
                while (it.hasNext()) {
                    UpdateListener updateListener = (UpdateListener) it.next();
                    T t = this.target;
                    ArrayMap<FloatPropertyCompat<? super T>, AnimationUpdate> arrayMap = new ArrayMap<>();
                    arrayMap.put(property, this.undispatchedUpdates.get(property));
                    updateListener.onAnimationUpdateForProperty(t, arrayMap);
                }
                this.undispatchedUpdates.remove(property);
            }
            boolean z3 = !this.this$0.arePropertiesAnimating(this.properties);
            Iterator<T> it2 = this.endListeners.iterator();
            while (it2.hasNext()) {
                ((EndListener) it2.next()).onAnimationEnd(this.target, property, z2, z, f, f2, z3);
                if (this.this$0.isPropertyAnimating(property)) {
                    return false;
                }
            }
            if (z3 && !z) {
                Iterator<T> it3 = this.endActions.iterator();
                while (it3.hasNext()) {
                    ((Function0) it3.next()).invoke();
                }
            }
            return z3;
        }

        private final void maybeDispatchUpdates() {
            if (this.undispatchedUpdates.size() < this.numPropertiesAnimating || this.undispatchedUpdates.size() <= 0) {
                return;
            }
            Iterator<T> it = this.updateListeners.iterator();
            while (it.hasNext()) {
                ((UpdateListener) it.next()).onAnimationUpdateForProperty(this.target, new ArrayMap<>(this.undispatchedUpdates));
            }
            this.undispatchedUpdates.clear();
        }
    }

    public final boolean isRunning() {
        Set<FloatPropertyCompat<? super T>> setKeySet = this.springAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet, "springAnimations.keys");
        Set<FloatPropertyCompat<? super T>> setKeySet2 = this.flingAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet2, "flingAnimations.keys");
        return arePropertiesAnimating(CollectionsKt___CollectionsKt.union(setKeySet, setKeySet2));
    }

    public final boolean isPropertyAnimating(@NotNull FloatPropertyCompat<? super T> property) {
        Intrinsics.checkParameterIsNotNull(property, "property");
        SpringAnimation springAnimation = this.springAnimations.get(property);
        if (!(springAnimation != null ? springAnimation.isRunning() : false)) {
            FlingAnimation flingAnimation = this.flingAnimations.get(property);
            if (!(flingAnimation != null ? flingAnimation.isRunning() : false)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    public final Set<FloatPropertyCompat<? super T>> getAnimatedProperties$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
        Set<FloatPropertyCompat<? super T>> setKeySet = this.springConfigs.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet, "springConfigs.keys");
        Set<FloatPropertyCompat<? super T>> setKeySet2 = this.flingConfigs.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet2, "flingConfigs.keys");
        return CollectionsKt___CollectionsKt.union(setKeySet, setKeySet2);
    }

    public final void cancelInternal$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull Set<? extends FloatPropertyCompat<? super T>> properties) {
        Intrinsics.checkParameterIsNotNull(properties, "properties");
        for (FloatPropertyCompat<? super T> floatPropertyCompat : properties) {
            FlingAnimation flingAnimation = this.flingAnimations.get(floatPropertyCompat);
            if (flingAnimation != null) {
                flingAnimation.cancel();
            }
            SpringAnimation springAnimation = this.springAnimations.get(floatPropertyCompat);
            if (springAnimation != null) {
                springAnimation.cancel();
            }
        }
    }

    public final void cancel() {
        Function1<? super Set<? extends FloatPropertyCompat<? super T>>, Unit> function1 = this.cancelAction;
        Set<FloatPropertyCompat<? super T>> setKeySet = this.flingAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet, "flingAnimations.keys");
        function1.invoke(setKeySet);
        Function1<? super Set<? extends FloatPropertyCompat<? super T>>, Unit> function12 = this.cancelAction;
        Set<FloatPropertyCompat<? super T>> setKeySet2 = this.springAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(setKeySet2, "springAnimations.keys");
        function12.invoke(setKeySet2);
    }

    public final void cancel(@NotNull FloatPropertyCompat<? super T>... properties) {
        Intrinsics.checkParameterIsNotNull(properties, "properties");
        this.cancelAction.invoke(ArraysKt___ArraysKt.toSet(properties));
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class SpringConfig {
        private float dampingRatio;
        private float finalPosition;
        private float startVelocity;
        private float stiffness;

        public static /* synthetic */ SpringConfig copy$default(SpringConfig springConfig, float f, float f2, float f3, float f4, int i, Object obj) {
            if ((i & 1) != 0) {
                f = springConfig.stiffness;
            }
            if ((i & 2) != 0) {
                f2 = springConfig.dampingRatio;
            }
            if ((i & 4) != 0) {
                f3 = springConfig.startVelocity;
            }
            if ((i & 8) != 0) {
                f4 = springConfig.finalPosition;
            }
            return springConfig.copy(f, f2, f3, f4);
        }

        @NotNull
        public final SpringConfig copy(float f, float f2, float f3, float f4) {
            return new SpringConfig(f, f2, f3, f4);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SpringConfig)) {
                return false;
            }
            SpringConfig springConfig = (SpringConfig) obj;
            return Float.compare(this.stiffness, springConfig.stiffness) == 0 && Float.compare(this.dampingRatio, springConfig.dampingRatio) == 0 && Float.compare(this.startVelocity, springConfig.startVelocity) == 0 && Float.compare(this.finalPosition, springConfig.finalPosition) == 0;
        }

        public int hashCode() {
            return (((((Float.hashCode(this.stiffness) * 31) + Float.hashCode(this.dampingRatio)) * 31) + Float.hashCode(this.startVelocity)) * 31) + Float.hashCode(this.finalPosition);
        }

        @NotNull
        public String toString() {
            return "SpringConfig(stiffness=" + this.stiffness + ", dampingRatio=" + this.dampingRatio + ", startVelocity=" + this.startVelocity + ", finalPosition=" + this.finalPosition + ")";
        }

        public SpringConfig(float f, float f2, float f3, float f4) {
            this.stiffness = f;
            this.dampingRatio = f2;
            this.startVelocity = f3;
            this.finalPosition = f4;
        }

        public final float getStiffness$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return this.stiffness;
        }

        public final float getDampingRatio$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return this.dampingRatio;
        }

        public final void setStartVelocity$frameworks__base__packages__SystemUI__android_common__SystemUI_core(float f) {
            this.startVelocity = f;
        }

        public /* synthetic */ SpringConfig(float f, float f2, float f3, float f4, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(f, f2, (i & 4) != 0 ? 0.0f : f3, (i & 8) != 0 ? PhysicsAnimatorKt.UNSET : f4);
        }

        public final float getFinalPosition$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return this.finalPosition;
        }

        public final void setFinalPosition$frameworks__base__packages__SystemUI__android_common__SystemUI_core(float f) {
            this.finalPosition = f;
        }

        public SpringConfig() {
            this(PhysicsAnimatorKt.globalDefaultSpring.stiffness, PhysicsAnimatorKt.globalDefaultSpring.dampingRatio);
        }

        public SpringConfig(float f, float f2) {
            this(f, f2, 0.0f, 0.0f, 8, null);
        }

        public final void applyToAnimation$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull SpringAnimation anim) {
            Intrinsics.checkParameterIsNotNull(anim, "anim");
            SpringForce spring = anim.getSpring();
            if (spring == null) {
                spring = new SpringForce();
            }
            spring.setStiffness(this.stiffness);
            spring.setDampingRatio(this.dampingRatio);
            spring.setFinalPosition(this.finalPosition);
            anim.setSpring(spring);
            float f = this.startVelocity;
            if (f != 0.0f) {
                anim.setStartVelocity(f);
            }
        }
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class FlingConfig {
        private float friction;
        private float max;
        private float min;
        private float startVelocity;

        public static /* synthetic */ FlingConfig copy$default(FlingConfig flingConfig, float f, float f2, float f3, float f4, int i, Object obj) {
            if ((i & 1) != 0) {
                f = flingConfig.friction;
            }
            if ((i & 2) != 0) {
                f2 = flingConfig.min;
            }
            if ((i & 4) != 0) {
                f3 = flingConfig.max;
            }
            if ((i & 8) != 0) {
                f4 = flingConfig.startVelocity;
            }
            return flingConfig.copy(f, f2, f3, f4);
        }

        @NotNull
        public final FlingConfig copy(float f, float f2, float f3, float f4) {
            return new FlingConfig(f, f2, f3, f4);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FlingConfig)) {
                return false;
            }
            FlingConfig flingConfig = (FlingConfig) obj;
            return Float.compare(this.friction, flingConfig.friction) == 0 && Float.compare(this.min, flingConfig.min) == 0 && Float.compare(this.max, flingConfig.max) == 0 && Float.compare(this.startVelocity, flingConfig.startVelocity) == 0;
        }

        public int hashCode() {
            return (((((Float.hashCode(this.friction) * 31) + Float.hashCode(this.min)) * 31) + Float.hashCode(this.max)) * 31) + Float.hashCode(this.startVelocity);
        }

        @NotNull
        public String toString() {
            return "FlingConfig(friction=" + this.friction + ", min=" + this.min + ", max=" + this.max + ", startVelocity=" + this.startVelocity + ")";
        }

        public FlingConfig(float f, float f2, float f3, float f4) {
            this.friction = f;
            this.min = f2;
            this.max = f3;
            this.startVelocity = f4;
        }

        public final float getFriction$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return this.friction;
        }

        public final float getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return this.min;
        }

        public final void setMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core(float f) {
            this.min = f;
        }

        public final float getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return this.max;
        }

        public final void setMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core(float f) {
            this.max = f;
        }

        public final void setStartVelocity$frameworks__base__packages__SystemUI__android_common__SystemUI_core(float f) {
            this.startVelocity = f;
        }

        public FlingConfig() {
            this(PhysicsAnimatorKt.globalDefaultFling.friction);
        }

        public FlingConfig(float f) {
            this(f, PhysicsAnimatorKt.globalDefaultFling.min, PhysicsAnimatorKt.globalDefaultFling.max);
        }

        public FlingConfig(float f, float f2, float f3) {
            this(f, f2, f3, 0.0f);
        }

        public final void applyToAnimation$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull FlingAnimation anim) {
            Intrinsics.checkParameterIsNotNull(anim, "anim");
            anim.setFriction(this.friction);
            anim.setMinValue(this.min);
            anim.setMaxValue(this.max);
            anim.setStartVelocity(this.startVelocity);
        }
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final Function1<Object, PhysicsAnimator<?>> getInstanceConstructor$frameworks__base__packages__SystemUI__android_common__SystemUI_core() {
            return PhysicsAnimator.instanceConstructor;
        }

        @NotNull
        public final <T> PhysicsAnimator<T> getInstance(@NotNull T target) {
            Intrinsics.checkParameterIsNotNull(target, "target");
            if (!PhysicsAnimatorKt.getAnimators().containsKey(target)) {
                PhysicsAnimatorKt.getAnimators().put(target, getInstanceConstructor$frameworks__base__packages__SystemUI__android_common__SystemUI_core().invoke(target));
            }
            Object obj = PhysicsAnimatorKt.getAnimators().get(target);
            if (obj != null) {
                return (PhysicsAnimator) obj;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.PhysicsAnimator<T>");
        }

        public final float estimateFlingEndValue(float f, float f2, @NotNull FlingConfig flingConfig) {
            Intrinsics.checkParameterIsNotNull(flingConfig, "flingConfig");
            return Math.min(flingConfig.getMax$frameworks__base__packages__SystemUI__android_common__SystemUI_core(), Math.max(flingConfig.getMin$frameworks__base__packages__SystemUI__android_common__SystemUI_core(), f + (f2 / (flingConfig.getFriction$frameworks__base__packages__SystemUI__android_common__SystemUI_core() * 4.2f))));
        }

        @NotNull
        public final String getReadablePropertyName(@NotNull FloatPropertyCompat<?> property) {
            Intrinsics.checkParameterIsNotNull(property, "property");
            return Intrinsics.areEqual(property, DynamicAnimation.TRANSLATION_X) ? "translationX" : Intrinsics.areEqual(property, DynamicAnimation.TRANSLATION_Y) ? "translationY" : Intrinsics.areEqual(property, DynamicAnimation.TRANSLATION_Z) ? "translationZ" : Intrinsics.areEqual(property, DynamicAnimation.SCALE_X) ? "scaleX" : Intrinsics.areEqual(property, DynamicAnimation.SCALE_Y) ? "scaleY" : Intrinsics.areEqual(property, DynamicAnimation.ROTATION) ? "rotation" : Intrinsics.areEqual(property, DynamicAnimation.ROTATION_X) ? "rotationX" : Intrinsics.areEqual(property, DynamicAnimation.ROTATION_Y) ? "rotationY" : Intrinsics.areEqual(property, DynamicAnimation.SCROLL_X) ? "scrollX" : Intrinsics.areEqual(property, DynamicAnimation.SCROLL_Y) ? "scrollY" : Intrinsics.areEqual(property, DynamicAnimation.ALPHA) ? "alpha" : "Custom FloatPropertyCompat instance";
        }
    }

    public final boolean arePropertiesAnimating(@NotNull Set<? extends FloatPropertyCompat<? super T>> properties) {
        Intrinsics.checkParameterIsNotNull(properties, "properties");
        if ((properties instanceof Collection) && properties.isEmpty()) {
            return false;
        }
        Iterator<T> it = properties.iterator();
        while (it.hasNext()) {
            if (isPropertyAnimating((FloatPropertyCompat) it.next())) {
                return true;
            }
        }
        return false;
    }
}
