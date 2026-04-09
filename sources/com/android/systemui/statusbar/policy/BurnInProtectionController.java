package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.phone.StatusBar;

/* loaded from: classes.dex */
public class BurnInProtectionController {
    private Context mContext;
    private int mHorizontalMaxShift;
    private PhoneStatusBarView mPhoneStatusBarView;
    private StatusBar mStatusBar;
    private int mVerticalMaxShift;
    private int mHorizontalShift = 0;
    private int mVerticalShift = 0;
    private int mHorizontalDirection = 1;
    private int mVerticalDirection = 1;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() { // from class: com.android.systemui.statusbar.policy.BurnInProtectionController$$ExternalSyntheticLambda0
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        shiftItems();
        this.mHandler.postDelayed(this.mRunnable, 40000L);
    }

    public BurnInProtectionController(Context context, StatusBar statusBar, PhoneStatusBarView phoneStatusBarView) {
        this.mContext = context;
        this.mPhoneStatusBarView = phoneStatusBarView;
        this.mStatusBar = statusBar;
        this.mHorizontalMaxShift = context.getResources().getDimensionPixelSize(R.dimen.burnin_protection_horizontal_shift);
        this.mVerticalMaxShift = this.mContext.getResources().getDimensionPixelSize(R.dimen.burnin_protection_vertical_shift) - 1;
    }

    public void startShiftTimer(boolean z) {
        if (z) {
            this.mHandler.removeCallbacks(this.mRunnable);
            this.mHandler.postDelayed(this.mRunnable, 40000L);
        }
    }

    public void stopShiftTimer(boolean z) {
        if (z) {
            this.mHandler.removeCallbacks(this.mRunnable);
            resetShiftItems();
        }
    }

    private void shiftItems() {
        int i = this.mHorizontalShift;
        int i2 = this.mHorizontalDirection;
        int i3 = i + i2;
        this.mHorizontalShift = i3;
        int i4 = this.mHorizontalMaxShift;
        if (i3 >= i4 || i3 <= (-i4)) {
            this.mHorizontalDirection = i2 * (-1);
        }
        int i5 = this.mVerticalShift;
        int i6 = this.mVerticalDirection;
        int i7 = i5 + i6;
        this.mVerticalShift = i7;
        int i8 = this.mVerticalMaxShift;
        if (i7 >= i8 || i7 <= (-i8)) {
            this.mVerticalDirection = i6 * (-1);
        }
        this.mPhoneStatusBarView.shiftStatusBarItems(i3, i7);
        NavigationBarView navigationBarView = this.mStatusBar.getNavigationBarView();
        if (navigationBarView != null) {
            navigationBarView.shiftNavigationBarItems(this.mHorizontalShift, this.mVerticalShift);
        }
    }

    private void resetShiftItems() {
        this.mPhoneStatusBarView.updateResources();
    }
}
