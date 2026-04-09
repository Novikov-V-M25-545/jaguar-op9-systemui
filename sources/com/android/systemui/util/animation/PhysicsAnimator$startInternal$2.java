package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.SpringAnimation;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: PhysicsAnimator.kt */
/* loaded from: classes.dex */
final /* synthetic */ class PhysicsAnimator$startInternal$2 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$startInternal$2(SpringAnimation springAnimation) {
        super(0, springAnimation);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "start";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(SpringAnimation.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "start()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public /* bridge */ /* synthetic */ Unit invoke() {
        invoke2();
        return Unit.INSTANCE;
    }

    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final void invoke2() {
        ((SpringAnimation) this.receiver).start();
    }
}
