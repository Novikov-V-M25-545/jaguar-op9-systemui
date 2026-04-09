package com.android.systemui.dagger;

import com.android.systemui.util.leak.LeakDetector;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class DependencyProvider_ProvideLeakDetectorFactory implements Factory<LeakDetector> {
    private final DependencyProvider module;

    public DependencyProvider_ProvideLeakDetectorFactory(DependencyProvider dependencyProvider) {
        this.module = dependencyProvider;
    }

    @Override // javax.inject.Provider
    public LeakDetector get() {
        return provideInstance(this.module);
    }

    public static LeakDetector provideInstance(DependencyProvider dependencyProvider) {
        return proxyProvideLeakDetector(dependencyProvider);
    }

    public static DependencyProvider_ProvideLeakDetectorFactory create(DependencyProvider dependencyProvider) {
        return new DependencyProvider_ProvideLeakDetectorFactory(dependencyProvider);
    }

    public static LeakDetector proxyProvideLeakDetector(DependencyProvider dependencyProvider) {
        return (LeakDetector) Preconditions.checkNotNull(dependencyProvider.provideLeakDetector(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
