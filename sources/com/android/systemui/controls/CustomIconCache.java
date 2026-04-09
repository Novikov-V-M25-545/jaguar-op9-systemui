package com.android.systemui.controls;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CustomIconCache.kt */
/* loaded from: classes.dex */
public final class CustomIconCache {
    private final Map<String, Icon> cache = new LinkedHashMap();
    private ComponentName currentComponent;

    public final void store(@NotNull ComponentName component, @NotNull String controlId, @Nullable Icon icon) {
        Intrinsics.checkParameterIsNotNull(component, "component");
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        if (!Intrinsics.areEqual(component, this.currentComponent)) {
            clear();
            this.currentComponent = component;
        }
        synchronized (this.cache) {
            if (icon != null) {
                this.cache.put(controlId, icon);
            } else {
                this.cache.remove(controlId);
            }
        }
    }

    @Nullable
    public final Icon retrieve(@NotNull ComponentName component, @NotNull String controlId) {
        Icon icon;
        Intrinsics.checkParameterIsNotNull(component, "component");
        Intrinsics.checkParameterIsNotNull(controlId, "controlId");
        if (!Intrinsics.areEqual(component, this.currentComponent)) {
            return null;
        }
        synchronized (this.cache) {
            icon = this.cache.get(controlId);
        }
        return icon;
    }

    private final void clear() {
        synchronized (this.cache) {
            this.cache.clear();
            Unit unit = Unit.INSTANCE;
        }
    }
}
