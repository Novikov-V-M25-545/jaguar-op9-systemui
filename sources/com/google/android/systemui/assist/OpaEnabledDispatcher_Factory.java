package com.google.android.systemui.assist;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class OpaEnabledDispatcher_Factory implements Factory<OpaEnabledDispatcher> {
    private final Provider<StatusBar> mStatusBarLazyAndLazyProvider;

    public OpaEnabledDispatcher_Factory(Provider<StatusBar> provider) {
        this.mStatusBarLazyAndLazyProvider = provider;
    }

    @Override // javax.inject.Provider
    public OpaEnabledDispatcher get() {
        return provideInstance(this.mStatusBarLazyAndLazyProvider);
    }

    public static OpaEnabledDispatcher provideInstance(Provider<StatusBar> provider) {
        OpaEnabledDispatcher opaEnabledDispatcher = new OpaEnabledDispatcher(DoubleCheck.lazy(provider));
        OpaEnabledDispatcher_MembersInjector.injectMStatusBarLazy(opaEnabledDispatcher, DoubleCheck.lazy(provider));
        return opaEnabledDispatcher;
    }

    public static OpaEnabledDispatcher_Factory create(Provider<StatusBar> provider) {
        return new OpaEnabledDispatcher_Factory(provider);
    }
}
