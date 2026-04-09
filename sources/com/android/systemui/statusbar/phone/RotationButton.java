package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import java.util.function.Consumer;

/* loaded from: classes.dex */
interface RotationButton {
    View getCurrentView();

    KeyButtonDrawable getImageDrawable();

    boolean hide();

    boolean isVisible();

    default void setCanShowRotationButton(boolean z) {
    }

    void setDarkIntensity(float f);

    void setOnClickListener(View.OnClickListener onClickListener);

    void setOnHoverListener(View.OnHoverListener onHoverListener);

    void setRotationButtonController(RotationButtonController rotationButtonController);

    void setVisibilityChangedCallback(Consumer<Boolean> consumer);

    boolean show();

    void updateIcon();

    default boolean acceptRotationProposal() {
        return getCurrentView() != null;
    }
}
