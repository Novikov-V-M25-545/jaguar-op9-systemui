package com.android.systemui.biometrics;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class FODCircleViewImpl_Factory implements Factory<FODCircleViewImpl> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public FODCircleViewImpl_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public FODCircleViewImpl get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static FODCircleViewImpl provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new FODCircleViewImpl(provider.get(), provider2.get());
    }

    public static FODCircleViewImpl_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new FODCircleViewImpl_Factory(provider, provider2);
    }
}
