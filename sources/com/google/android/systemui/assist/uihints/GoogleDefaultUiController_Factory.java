package com.google.android.systemui.assist.uihints;

import android.content.Context;
import com.android.systemui.assist.AssistLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class GoogleDefaultUiController_Factory implements Factory<GoogleDefaultUiController> {
    private final Provider<AssistLogger> assistLoggerProvider;
    private final Provider<Context> contextProvider;

    public GoogleDefaultUiController_Factory(Provider<Context> provider, Provider<AssistLogger> provider2) {
        this.contextProvider = provider;
        this.assistLoggerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public GoogleDefaultUiController get() {
        return provideInstance(this.contextProvider, this.assistLoggerProvider);
    }

    public static GoogleDefaultUiController provideInstance(Provider<Context> provider, Provider<AssistLogger> provider2) {
        return new GoogleDefaultUiController(provider.get(), provider2.get());
    }

    public static GoogleDefaultUiController_Factory create(Provider<Context> provider, Provider<AssistLogger> provider2) {
        return new GoogleDefaultUiController_Factory(provider, provider2);
    }
}
