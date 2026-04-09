package com.android.systemui.util.animation;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: PhysicsAnimator.kt */
/* loaded from: classes.dex */
final /* synthetic */ class PhysicsAnimator$withEndActions$1$1 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$withEndActions$1$1(Runnable runnable) {
        super(0, runnable);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "run";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(Runnable.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "run()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public /* bridge */ /* synthetic */ Unit invoke() {
        invoke2();
        return Unit.INSTANCE;
    }

    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final void invoke2() {
        ((Runnable) this.receiver).run();
    }
}
