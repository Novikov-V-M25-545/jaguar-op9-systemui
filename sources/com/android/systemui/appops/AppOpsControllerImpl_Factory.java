package com.android.systemui.appops;

import android.content.Context;
import android.media.AudioManager;
import android.os.Looper;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AppOpsControllerImpl_Factory implements Factory<AppOpsControllerImpl> {
    private final Provider<AudioManager> audioManagerProvider;
    private final Provider<Looper> bgLooperProvider;
    private final Provider<PermissionFlagsCache> cacheProvider;
    private final Provider<Context> contextProvider;
    private final Provider<BroadcastDispatcher> dispatcherProvider;
    private final Provider<DumpManager> dumpManagerProvider;

    public AppOpsControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<DumpManager> provider3, Provider<PermissionFlagsCache> provider4, Provider<AudioManager> provider5, Provider<BroadcastDispatcher> provider6) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
        this.dumpManagerProvider = provider3;
        this.cacheProvider = provider4;
        this.audioManagerProvider = provider5;
        this.dispatcherProvider = provider6;
    }

    @Override // javax.inject.Provider
    public AppOpsControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider, this.dumpManagerProvider, this.cacheProvider, this.audioManagerProvider, this.dispatcherProvider);
    }

    public static AppOpsControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<DumpManager> provider3, Provider<PermissionFlagsCache> provider4, Provider<AudioManager> provider5, Provider<BroadcastDispatcher> provider6) {
        return new AppOpsControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static AppOpsControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<DumpManager> provider3, Provider<PermissionFlagsCache> provider4, Provider<AudioManager> provider5, Provider<BroadcastDispatcher> provider6) {
        return new AppOpsControllerImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
