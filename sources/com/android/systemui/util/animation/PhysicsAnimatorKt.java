package com.android.systemui.util.animation;

import android.view.View;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.WeakHashMap;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PhysicsAnimator.kt */
/* loaded from: classes.dex */
public final class PhysicsAnimatorKt {
    private static final float UNSET;

    @NotNull
    private static final WeakHashMap<Object, PhysicsAnimator<?>> animators;
    private static final PhysicsAnimator.FlingConfig globalDefaultFling;
    private static final PhysicsAnimator.SpringConfig globalDefaultSpring;
    private static boolean verboseLogging;

    @NotNull
    public static final <T extends View> PhysicsAnimator<T> getPhysicsAnimator(@NotNull T physicsAnimator) {
        Intrinsics.checkParameterIsNotNull(physicsAnimator, "$this$physicsAnimator");
        return PhysicsAnimator.Companion.getInstance(physicsAnimator);
    }

    static {
        FloatCompanionObject floatCompanionObject = FloatCompanionObject.INSTANCE;
        UNSET = -floatCompanionObject.getMAX_VALUE();
        animators = new WeakHashMap<>();
        globalDefaultSpring = new PhysicsAnimator.SpringConfig(1500.0f, 0.5f);
        globalDefaultFling = new PhysicsAnimator.FlingConfig(1.0f, -floatCompanionObject.getMAX_VALUE(), floatCompanionObject.getMAX_VALUE());
    }

    @NotNull
    public static final WeakHashMap<Object, PhysicsAnimator<?>> getAnimators() {
        return animators;
    }
}
