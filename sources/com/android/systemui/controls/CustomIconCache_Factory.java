package com.android.systemui.controls;

import dagger.internal.Factory;

/* loaded from: classes.dex */
public final class CustomIconCache_Factory implements Factory<CustomIconCache> {
    private static final CustomIconCache_Factory INSTANCE = new CustomIconCache_Factory();

    @Override // javax.inject.Provider
    public CustomIconCache get() {
        return provideInstance();
    }

    public static CustomIconCache provideInstance() {
        return new CustomIconCache();
    }

    public static CustomIconCache_Factory create() {
        return INSTANCE;
    }
}
