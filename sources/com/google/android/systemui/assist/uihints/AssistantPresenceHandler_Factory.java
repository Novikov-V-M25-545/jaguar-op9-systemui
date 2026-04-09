package com.google.android.systemui.assist.uihints;

import android.content.Context;
import com.android.internal.app.AssistUtils;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class AssistantPresenceHandler_Factory implements Factory<AssistantPresenceHandler> {
    private final Provider<AssistUtils> assistUtilsProvider;
    private final Provider<Context> contextProvider;

    public AssistantPresenceHandler_Factory(Provider<Context> provider, Provider<AssistUtils> provider2) {
        this.contextProvider = provider;
        this.assistUtilsProvider = provider2;
    }

    @Override // javax.inject.Provider
    public AssistantPresenceHandler get() {
        return provideInstance(this.contextProvider, this.assistUtilsProvider);
    }

    public static AssistantPresenceHandler provideInstance(Provider<Context> provider, Provider<AssistUtils> provider2) {
        return new AssistantPresenceHandler(provider.get(), provider2.get());
    }

    public static AssistantPresenceHandler_Factory create(Provider<Context> provider, Provider<AssistUtils> provider2) {
        return new AssistantPresenceHandler_Factory(provider, provider2);
    }
}
