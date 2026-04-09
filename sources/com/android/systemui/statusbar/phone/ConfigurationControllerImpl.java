package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConfigurationControllerImpl.kt */
/* loaded from: classes.dex */
public final class ConfigurationControllerImpl implements ConfigurationController {
    private final Context context;
    private int density;
    private float fontScale;
    private final boolean inCarMode;
    private final Configuration lastConfig;
    private final List<ConfigurationController.ConfigurationListener> listeners;
    private LocaleList localeList;
    private int uiMode;

    public ConfigurationControllerImpl(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.listeners = new ArrayList();
        this.lastConfig = new Configuration();
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration currentConfig = resources.getConfiguration();
        this.context = context;
        this.fontScale = currentConfig.fontScale;
        this.density = currentConfig.densityDpi;
        int i = currentConfig.uiMode;
        this.inCarMode = (i & 15) == 3;
        this.uiMode = i & 48;
        Intrinsics.checkExpressionValueIsNotNull(currentConfig, "currentConfig");
        this.localeList = currentConfig.getLocales();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController
    public void notifyThemeChanged() {
        for (ConfigurationController.ConfigurationListener configurationListener : new ArrayList(this.listeners)) {
            if (this.listeners.contains(configurationListener)) {
                configurationListener.onThemeChanged();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        Intrinsics.checkParameterIsNotNull(newConfig, "newConfig");
        ArrayList<ConfigurationController.ConfigurationListener> arrayList = new ArrayList(this.listeners);
        for (ConfigurationController.ConfigurationListener configurationListener : arrayList) {
            if (this.listeners.contains(configurationListener)) {
                configurationListener.onConfigChanged(newConfig);
            }
        }
        float f = newConfig.fontScale;
        int i = newConfig.densityDpi;
        int i2 = newConfig.uiMode & 48;
        boolean z = i2 != this.uiMode;
        if (i != this.density || f != this.fontScale || (this.inCarMode && z)) {
            for (ConfigurationController.ConfigurationListener configurationListener2 : arrayList) {
                if (this.listeners.contains(configurationListener2)) {
                    configurationListener2.onDensityOrFontScaleChanged();
                }
            }
            this.density = i;
            this.fontScale = f;
        }
        LocaleList locales = newConfig.getLocales();
        if (!Intrinsics.areEqual(locales, this.localeList)) {
            this.localeList = locales;
            for (ConfigurationController.ConfigurationListener configurationListener3 : arrayList) {
                if (this.listeners.contains(configurationListener3)) {
                    configurationListener3.onLocaleListChanged();
                }
            }
        }
        if (z) {
            this.context.getTheme().applyStyle(this.context.getThemeResId(), true);
            this.uiMode = i2;
            for (ConfigurationController.ConfigurationListener configurationListener4 : arrayList) {
                if (this.listeners.contains(configurationListener4)) {
                    configurationListener4.onUiModeChanged();
                }
            }
        }
        if ((this.lastConfig.updateFrom(newConfig) & Integer.MIN_VALUE) != 0) {
            for (ConfigurationController.ConfigurationListener configurationListener5 : arrayList) {
                if (this.listeners.contains(configurationListener5)) {
                    configurationListener5.onOverlayChanged();
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(@NotNull ConfigurationController.ConfigurationListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.listeners.add(listener);
        listener.onDensityOrFontScaleChanged();
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(@NotNull ConfigurationController.ConfigurationListener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.listeners.remove(listener);
    }
}
