package com.android.systemui.util.concurrency;

import dagger.internal.Factory;

/* loaded from: classes.dex */
public final class ExecutionImpl_Factory implements Factory<ExecutionImpl> {
    private static final ExecutionImpl_Factory INSTANCE = new ExecutionImpl_Factory();

    @Override // javax.inject.Provider
    public ExecutionImpl get() {
        return provideInstance();
    }

    public static ExecutionImpl provideInstance() {
        return new ExecutionImpl();
    }

    public static ExecutionImpl_Factory create() {
        return INSTANCE;
    }
}
