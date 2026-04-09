package com.android.systemui.controls.ui;

import android.os.VibrationEffect;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Vibrations.kt */
/* loaded from: classes.dex */
public final class Vibrations {
    public static final Vibrations INSTANCE;

    @NotNull
    private static final VibrationEffect rangeEdgeEffect;

    @NotNull
    private static final VibrationEffect rangeMiddleEffect;

    static {
        Vibrations vibrations = new Vibrations();
        INSTANCE = vibrations;
        rangeEdgeEffect = vibrations.initRangeEdgeEffect();
        rangeMiddleEffect = vibrations.initRangeMiddleEffect();
    }

    private Vibrations() {
    }

    @NotNull
    public final VibrationEffect getRangeEdgeEffect() {
        return rangeEdgeEffect;
    }

    @NotNull
    public final VibrationEffect getRangeMiddleEffect() {
        return rangeMiddleEffect;
    }

    private final VibrationEffect initRangeEdgeEffect() {
        VibrationEffect.Composition compositionStartComposition = VibrationEffect.startComposition();
        compositionStartComposition.addPrimitive(7, 0.5f);
        VibrationEffect vibrationEffectCompose = compositionStartComposition.compose();
        Intrinsics.checkExpressionValueIsNotNull(vibrationEffectCompose, "composition.compose()");
        return vibrationEffectCompose;
    }

    private final VibrationEffect initRangeMiddleEffect() {
        VibrationEffect.Composition compositionStartComposition = VibrationEffect.startComposition();
        compositionStartComposition.addPrimitive(7, 0.1f);
        VibrationEffect vibrationEffectCompose = compositionStartComposition.compose();
        Intrinsics.checkExpressionValueIsNotNull(vibrationEffectCompose, "composition.compose()");
        return vibrationEffectCompose;
    }
}
