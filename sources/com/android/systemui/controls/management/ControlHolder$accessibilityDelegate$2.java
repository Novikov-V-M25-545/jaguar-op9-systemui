package com.android.systemui.controls.management;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: ControlAdapter.kt */
/* loaded from: classes.dex */
final /* synthetic */ class ControlHolder$accessibilityDelegate$2 extends FunctionReference implements Function0<Integer> {
    ControlHolder$accessibilityDelegate$2(ControlHolder controlHolder) {
        super(0, controlHolder);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "getLayoutPosition";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlHolder.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "getLayoutPosition()I";
    }

    @Override // kotlin.jvm.functions.Function0
    public /* bridge */ /* synthetic */ Integer invoke() {
        return Integer.valueOf(invoke2());
    }

    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final int invoke2() {
        return ((ControlHolder) this.receiver).getLayoutPosition();
    }
}
