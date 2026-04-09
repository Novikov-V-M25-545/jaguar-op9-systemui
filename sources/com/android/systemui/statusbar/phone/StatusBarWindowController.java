package com.android.systemui.statusbar.phone;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.os.Binder;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class StatusBarWindowController {
    private int mBarHeight;
    private final Context mContext;
    private WindowManager.LayoutParams mLp;
    private final Resources mResources;
    private ViewGroup mStatusBarView;
    private final SuperStatusBarViewFactory mSuperStatusBarViewFactory;
    private final WindowManager mWindowManager;
    private final State mCurrentState = new State();
    private final WindowManager.LayoutParams mLpChanged = new WindowManager.LayoutParams();

    public StatusBarWindowController(Context context, WindowManager windowManager, SuperStatusBarViewFactory superStatusBarViewFactory, Resources resources) {
        this.mBarHeight = -1;
        this.mContext = context;
        this.mWindowManager = windowManager;
        this.mSuperStatusBarViewFactory = superStatusBarViewFactory;
        this.mStatusBarView = superStatusBarViewFactory.getStatusBarWindowView();
        this.mResources = resources;
        if (this.mBarHeight < 0) {
            this.mBarHeight = resources.getDimensionPixelSize(R.dimen.notification_custom_view_max_image_height);
        }
    }

    public int getStatusBarHeight() {
        return this.mBarHeight;
    }

    public void refreshStatusBarHeight() {
        int dimensionPixelSize = this.mResources.getDimensionPixelSize(R.dimen.notification_custom_view_max_image_height);
        if (this.mBarHeight != dimensionPixelSize) {
            this.mBarHeight = dimensionPixelSize;
            apply(this.mCurrentState);
        }
    }

    public void attach() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, this.mBarHeight, 2000, -2139095032, -3);
        this.mLp = layoutParams;
        layoutParams.privateFlags |= 16777216;
        layoutParams.token = new Binder();
        WindowManager.LayoutParams layoutParams2 = this.mLp;
        layoutParams2.gravity = 48;
        layoutParams2.setFitInsetsTypes(0);
        this.mLp.setTitle("StatusBar");
        this.mLp.packageName = this.mContext.getPackageName();
        WindowManager.LayoutParams layoutParams3 = this.mLp;
        layoutParams3.layoutInDisplayCutoutMode = 3;
        this.mWindowManager.addView(this.mStatusBarView, layoutParams3);
        this.mLpChanged.copyFrom(this.mLp);
    }

    public void setForceStatusBarVisible(boolean z) {
        State state = this.mCurrentState;
        state.mForceStatusBarVisible = z;
        apply(state);
    }

    private void applyHeight() {
        this.mLpChanged.height = this.mBarHeight;
    }

    private void apply(State state) {
        applyForceStatusBarVisibleFlag(state);
        applyHeight();
        WindowManager.LayoutParams layoutParams = this.mLp;
        if (layoutParams == null || layoutParams.copyFrom(this.mLpChanged) == 0) {
            return;
        }
        this.mWindowManager.updateViewLayout(this.mStatusBarView, this.mLp);
    }

    private static class State {
        boolean mForceStatusBarVisible;

        private State() {
        }
    }

    private void applyForceStatusBarVisibleFlag(State state) {
        if (state.mForceStatusBarVisible) {
            this.mLpChanged.privateFlags |= LineageHardwareManager.FEATURE_AUTO_CONTRAST;
        } else {
            this.mLpChanged.privateFlags &= -4097;
        }
    }
}
