package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.KeyButtonView;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class FloatingRotationButton implements RotationButton {
    private boolean mCanShow = true;
    private final Context mContext;
    private final int mDiameter;
    private boolean mIsShowing;
    private KeyButtonDrawable mKeyButtonDrawable;
    private final KeyButtonView mKeyButtonView;
    private final int mMargin;
    private RotationButtonController mRotationButtonController;
    private Consumer<Boolean> mVisibilityChangedCallback;
    private final WindowManager mWindowManager;

    FloatingRotationButton(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        KeyButtonView keyButtonView = (KeyButtonView) LayoutInflater.from(context).inflate(R.layout.rotate_suggestion, (ViewGroup) null);
        this.mKeyButtonView = keyButtonView;
        keyButtonView.setVisibility(0);
        Resources resources = context.getResources();
        this.mDiameter = resources.getDimensionPixelSize(R.dimen.floating_rotation_button_diameter);
        this.mMargin = Math.max(resources.getDimensionPixelSize(R.dimen.floating_rotation_button_min_margin), resources.getDimensionPixelSize(R.dimen.rounded_corner_content_padding));
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setRotationButtonController(RotationButtonController rotationButtonController) {
        this.mRotationButtonController = rotationButtonController;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setVisibilityChangedCallback(Consumer<Boolean> consumer) {
        this.mVisibilityChangedCallback = consumer;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public View getCurrentView() {
        return this.mKeyButtonView;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean show() {
        if (!this.mCanShow || this.mIsShowing) {
            return false;
        }
        this.mIsShowing = true;
        int i = this.mDiameter;
        int i2 = this.mMargin;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i, i, i2, i2, 2024, 8, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("FloatingRotationButton");
        layoutParams.setFitInsetsTypes(0);
        int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
        if (rotation == 0) {
            layoutParams.gravity = 83;
        } else if (rotation == 1) {
            layoutParams.gravity = 85;
        } else if (rotation == 2) {
            layoutParams.gravity = 53;
        } else if (rotation == 3) {
            layoutParams.gravity = 51;
        }
        updateIcon();
        this.mWindowManager.addView(this.mKeyButtonView, layoutParams);
        KeyButtonDrawable keyButtonDrawable = this.mKeyButtonDrawable;
        if (keyButtonDrawable != null && keyButtonDrawable.canAnimate()) {
            this.mKeyButtonDrawable.resetAnimation();
            this.mKeyButtonDrawable.startAnimation();
        }
        this.mKeyButtonView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.statusbar.phone.FloatingRotationButton.1
            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
                if (FloatingRotationButton.this.mIsShowing && FloatingRotationButton.this.mVisibilityChangedCallback != null) {
                    FloatingRotationButton.this.mVisibilityChangedCallback.accept(Boolean.TRUE);
                }
                FloatingRotationButton.this.mKeyButtonView.removeOnLayoutChangeListener(this);
            }
        });
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean hide() {
        if (!this.mIsShowing) {
            return false;
        }
        this.mWindowManager.removeViewImmediate(this.mKeyButtonView);
        this.mIsShowing = false;
        Consumer<Boolean> consumer = this.mVisibilityChangedCallback;
        if (consumer == null) {
            return true;
        }
        consumer.accept(Boolean.FALSE);
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean isVisible() {
        return this.mIsShowing;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void updateIcon() {
        if (this.mIsShowing) {
            KeyButtonDrawable imageDrawable = getImageDrawable();
            this.mKeyButtonDrawable = imageDrawable;
            this.mKeyButtonView.setImageDrawable(imageDrawable);
            this.mKeyButtonDrawable.setCallback(this.mKeyButtonView);
            KeyButtonDrawable keyButtonDrawable = this.mKeyButtonDrawable;
            if (keyButtonDrawable == null || !keyButtonDrawable.canAnimate()) {
                return;
            }
            this.mKeyButtonDrawable.resetAnimation();
            this.mKeyButtonDrawable.startAnimation();
        }
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mKeyButtonView.setOnClickListener(onClickListener);
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setOnHoverListener(View.OnHoverListener onHoverListener) {
        this.mKeyButtonView.setOnHoverListener(onHoverListener);
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public KeyButtonDrawable getImageDrawable() {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.mContext.getApplicationContext(), this.mRotationButtonController.getStyleRes());
        int themeAttr = Utils.getThemeAttr(contextThemeWrapper, R.attr.darkIconTheme);
        ContextThemeWrapper contextThemeWrapper2 = new ContextThemeWrapper(contextThemeWrapper, Utils.getThemeAttr(contextThemeWrapper, R.attr.lightIconTheme));
        ContextThemeWrapper contextThemeWrapper3 = new ContextThemeWrapper(contextThemeWrapper, themeAttr);
        int i = R.attr.singleToneColor;
        return KeyButtonDrawable.create(contextThemeWrapper2, Utils.getColorAttrDefaultColor(contextThemeWrapper2, i), Utils.getColorAttrDefaultColor(contextThemeWrapper3, i), R.drawable.ic_sysbar_rotate_button, false, Color.valueOf(Color.red(r4), Color.green(r4), Color.blue(r4), 0.92f));
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setDarkIntensity(float f) {
        this.mKeyButtonView.setDarkIntensity(f);
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setCanShowRotationButton(boolean z) {
        this.mCanShow = z;
        if (z) {
            return;
        }
        hide();
    }
}
