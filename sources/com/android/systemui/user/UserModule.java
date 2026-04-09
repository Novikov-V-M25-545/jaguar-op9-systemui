package com.android.systemui.user;

import com.android.settingslib.users.EditUserInfoController;

/* loaded from: classes.dex */
public class UserModule {
    EditUserInfoController provideEditUserInfoController() {
        return new EditUserInfoController("com.android.systemui.fileprovider");
    }
}
