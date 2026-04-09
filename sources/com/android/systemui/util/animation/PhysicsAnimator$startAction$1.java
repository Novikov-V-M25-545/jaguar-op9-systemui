package com.android.systemui.util.animation;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: PhysicsAnimator.kt */
/* loaded from: classes.dex */
final /* synthetic */ class PhysicsAnimator$startAction$1 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$startAction$1(PhysicsAnimator physicsAnimator) {
        super(0, physicsAnimator);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "startInternal";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(PhysicsAnimator.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "startInternal$frameworks__base__packages__SystemUI__android_common__SystemUI_core()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public /* bridge */ /* synthetic */ Unit invoke() {
        invoke2();
        return Unit.INSTANCE;
    }

    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final void invoke2() {
        ((PhysicsAnimator) this.receiver).startInternal$frameworks__base__packages__SystemUI__android_common__SystemUI_core();
    }
}
