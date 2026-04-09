package com.android.systemui.user;

import android.content.Context;
import android.os.UserManager;
import dagger.internal.Factory;
import javax.inject.Provider;

/* loaded from: classes.dex */
public final class UserCreator_Factory implements Factory<UserCreator> {
    private final Provider<Context> contextProvider;
    private final Provider<UserManager> userManagerProvider;

    public UserCreator_Factory(Provider<Context> provider, Provider<UserManager> provider2) {
        this.contextProvider = provider;
        this.userManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public UserCreator get() {
        return provideInstance(this.contextProvider, this.userManagerProvider);
    }

    public static UserCreator provideInstance(Provider<Context> provider, Provider<UserManager> provider2) {
        return new UserCreator(provider.get(), provider2.get());
    }

    public static UserCreator_Factory create(Provider<Context> provider, Provider<UserManager> provider2) {
        return new UserCreator_Factory(provider, provider2);
    }
}
