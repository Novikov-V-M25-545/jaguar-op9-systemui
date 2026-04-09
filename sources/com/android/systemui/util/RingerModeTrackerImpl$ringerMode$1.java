package com.android.systemui.util;

import android.media.AudioManager;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: RingerModeTrackerImpl.kt */
/* loaded from: classes.dex */
final /* synthetic */ class RingerModeTrackerImpl$ringerMode$1 extends FunctionReference implements Function0<Integer> {
    RingerModeTrackerImpl$ringerMode$1(AudioManager audioManager) {
        super(0, audioManager);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "getRingerMode";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(AudioManager.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "getRingerMode()I";
    }

    @Override // kotlin.jvm.functions.Function0
    public /* bridge */ /* synthetic */ Integer invoke() {
        return Integer.valueOf(invoke2());
    }

    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final int invoke2() {
        return ((AudioManager) this.receiver).getRingerMode();
    }
}
