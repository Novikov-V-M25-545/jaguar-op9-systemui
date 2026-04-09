package com.android.systemui.assist;

import android.content.Context;
import com.android.internal.app.AssistUtils;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AssistModule_ProvideAssistUtilsFactory implements Factory<AssistUtils> {
    private final Provider<Context> contextProvider;

    public AssistModule_ProvideAssistUtilsFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AssistUtils get() {
        return provideInstance(this.contextProvider);
    }

    public static AssistUtils provideInstance(Provider<Context> provider) {
        return proxyProvideAssistUtils(provider.get());
    }

    public static AssistModule_ProvideAssistUtilsFactory create(Provider<Context> provider) {
        return new AssistModule_ProvideAssistUtilsFactory(provider);
    }

    public static AssistUtils proxyProvideAssistUtils(Context context) {
        return (AssistUtils) Preconditions.checkNotNull(AssistModule.provideAssistUtils(context), "Cannot return null from a non-@Nullable @Provides method");
    }
}
