package com.android.systemui.user;

import com.android.settingslib.users.EditUserInfoController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

/* loaded from: classes.dex */
public final class UserModule_ProvideEditUserInfoControllerFactory implements Factory<EditUserInfoController> {
    private final UserModule module;

    public UserModule_ProvideEditUserInfoControllerFactory(UserModule userModule) {
        this.module = userModule;
    }

    @Override // javax.inject.Provider
    public EditUserInfoController get() {
        return provideInstance(this.module);
    }

    public static EditUserInfoController provideInstance(UserModule userModule) {
        return proxyProvideEditUserInfoController(userModule);
    }

    public static UserModule_ProvideEditUserInfoControllerFactory create(UserModule userModule) {
        return new UserModule_ProvideEditUserInfoControllerFactory(userModule);
    }

    public static EditUserInfoController proxyProvideEditUserInfoController(UserModule userModule) {
        return (EditUserInfoController) Preconditions.checkNotNull(userModule.provideEditUserInfoController(), "Cannot return null from a non-@Nullable @Provides method");
    }
}
