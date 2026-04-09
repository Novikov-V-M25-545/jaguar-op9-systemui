package com.android.systemui.statusbar.phone;

import android.view.ContextThemeWrapper;
import android.view.View;
import com.android.systemui.statusbar.phone.ContextualButton;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class RotationContextButton extends ContextualButton implements NavigationModeController.ModeChangedListener, RotationButton {
    private int mNavBarMode;
    private RotationButtonController mRotationButtonController;

    public RotationContextButton(int i, int i2) {
        super(i, i2);
        this.mNavBarMode = 0;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setRotationButtonController(RotationButtonController rotationButtonController) {
        this.mRotationButtonController = rotationButtonController;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setVisibilityChangedCallback(final Consumer<Boolean> consumer) {
        setListener(new ContextualButton.ContextButtonListener() { // from class: com.android.systemui.statusbar.phone.RotationContextButton.1
            @Override // com.android.systemui.statusbar.phone.ContextualButton.ContextButtonListener
            public void onVisibilityChanged(ContextualButton contextualButton, boolean z) {
                Consumer consumer2 = consumer;
                if (consumer2 != null) {
                    consumer2.accept(Boolean.valueOf(z));
                }
            }
        });
    }

    @Override // com.android.systemui.statusbar.phone.ContextualButton, com.android.systemui.statusbar.phone.ButtonDispatcher
    public void setVisibility(int i) {
        super.setVisibility(i);
        KeyButtonDrawable imageDrawable = getImageDrawable();
        if (i != 0 || imageDrawable == null) {
            return;
        }
        imageDrawable.resetAnimation();
        imageDrawable.startAnimation();
    }

    @Override // com.android.systemui.statusbar.phone.ContextualButton
    protected KeyButtonDrawable getNewDrawable() {
        return KeyButtonDrawable.create(new ContextThemeWrapper(getContext().getApplicationContext(), this.mRotationButtonController.getStyleRes()), this.mIconResId, false, null);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean acceptRotationProposal() {
        View currentView = getCurrentView();
        return currentView != null && currentView.isAttachedToWindow();
    }
}
