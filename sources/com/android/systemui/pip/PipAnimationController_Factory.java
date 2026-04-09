package com.android.systemui.pip;

import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class PipAnimationController_Factory implements Factory<PipAnimationController> {
    private final Provider<PipSurfaceTransactionHelper> helperProvider;

    public PipAnimationController_Factory(Provider<PipSurfaceTransactionHelper> provider) {
        this.helperProvider = provider;
    }

    @Override // javax.inject.Provider
    public PipAnimationController get() {
        return provideInstance(this.helperProvider);
    }

    public static PipAnimationController provideInstance(Provider<PipSurfaceTransactionHelper> provider) {
        return new PipAnimationController(provider.get());
    }

    public static PipAnimationController_Factory create(Provider<PipSurfaceTransactionHelper> provider) {
        return new PipAnimationController_Factory(provider);
    }
}
