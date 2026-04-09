package com.android.systemui.assist;

import androidx.slice.Clock;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class AssistModule_ProvideSystemClockFactory implements Factory<Clock> {
    private static final AssistModule_ProvideSystemClockFactory INSTANCE = new AssistModule_ProvideSystemClockFactory();

    @Override // javax.inject.Provider
    public Clock get() {
        return provideInstance();
    }

    public static Clock provideInstance() {
        return proxyProvideSystemClock();
    }

    public static AssistModule_ProvideSystemClockFactory create() {
        return INSTANCE;
    }

    public static Clock proxyProvideSystemClock() {
        return (Clock) Preconditions.checkNotNull(AssistModule.provideSystemClock(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
