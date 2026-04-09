package com.android.systemui.util;

import android.content.Context;
import com.android.systemui.util.InjectionInflationController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class InjectionInflationController_ViewAttributeProvider_ProvideContextFactory implements Factory<Context> {
    public static Context proxyProvideContext(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
        return (Context) Preconditions.checkNotNull(viewAttributeProvider.provideContext(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
