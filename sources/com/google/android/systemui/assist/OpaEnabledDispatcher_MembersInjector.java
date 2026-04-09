package com.google.android.systemui.assist;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;

/* loaded from: classes.dex */
public final class OpaEnabledDispatcher_MembersInjector {
    public static void injectMStatusBarLazy(OpaEnabledDispatcher opaEnabledDispatcher, Lazy<StatusBar> lazy) {
        opaEnabledDispatcher.mStatusBarLazy = lazy;
    }
}
