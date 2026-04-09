package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.systemui.R;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PrivacyItem.kt */
/* loaded from: classes.dex */
public enum PrivacyType {
    TYPE_CAMERA(R.string.privacy_type_camera, android.R.drawable.list_selector_background_transition_holo_light),
    TYPE_MICROPHONE(R.string.privacy_type_microphone, android.R.drawable.list_selector_focused_holo_light),
    TYPE_LOCATION(R.string.privacy_type_location, android.R.drawable.list_selector_focused_holo_dark);

    private final int iconId;
    private final int nameId;

    PrivacyType(int i, int i2) {
        this.nameId = i;
        this.iconId = i2;
    }

    public final int getIconId() {
        return this.iconId;
    }

    public final int getNameId() {
        return this.nameId;
    }

    public final String getName(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        return context.getResources().getString(this.nameId);
    }

    public final Drawable getIcon(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        return context.getResources().getDrawable(this.iconId, context.getTheme());
    }
}
