package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import java.util.Set;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: PhysicsAnimator.kt */
/* loaded from: classes.dex */
final /* synthetic */ class PhysicsAnimator$cancelAction$1<T> extends FunctionReference implements Function1<Set<? extends FloatPropertyCompat<? super T>>, Unit> {
    PhysicsAnimator$cancelAction$1(PhysicsAnimator physicsAnimator) {
        super(1, physicsAnimator);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "cancelInternal";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(PhysicsAnimator.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "cancelInternal$frameworks__base__packages__SystemUI__android_common__SystemUI_core(Ljava/util/Set;)V";
    }

    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Object obj) {
        invoke((Set) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull Set<? extends FloatPropertyCompat<? super T>> p1) {
        Intrinsics.checkParameterIsNotNull(p1, "p1");
        ((PhysicsAnimator) this.receiver).cancelInternal$frameworks__base__packages__SystemUI__android_common__SystemUI_core(p1);
    }
}
