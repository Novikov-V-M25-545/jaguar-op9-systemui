package com.android.systemui.util;

import android.util.AttributeSet;
import com.android.systemui.util.InjectionInflationController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory implements Factory<AttributeSet> {
    public static AttributeSet proxyProvideAttributeSet(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
        return (AttributeSet) Preconditions.checkNotNull(viewAttributeProvider.provideAttributeSet(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
