package com.android.systemui.screenshot;

import dagger.internal.Factory;

/* loaded from: classes.dex */
public final class ScreenshotSmartActions_Factory implements Factory<ScreenshotSmartActions> {
    private static final ScreenshotSmartActions_Factory INSTANCE = new ScreenshotSmartActions_Factory();

    @Override // javax.inject.Provider
    public ScreenshotSmartActions get() {
        return provideInstance();
    }

    public static ScreenshotSmartActions provideInstance() {
        return new ScreenshotSmartActions();
    }

    public static ScreenshotSmartActions_Factory create() {
        return INSTANCE;
    }
}
