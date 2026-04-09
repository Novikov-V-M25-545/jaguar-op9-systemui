package com.android.systemui.stackdivider;

import android.graphics.Region;
import android.os.Binder;
import android.view.View;
import android.view.WindowManager;
import com.android.systemui.wm.SystemWindows;

/* loaded from: classes.dex */
public class DividerWindowManager {
    private WindowManager.LayoutParams mLp;
    final SystemWindows mSystemWindows;
    private View mView;

    public DividerWindowManager(SystemWindows systemWindows) {
        this.mSystemWindows = systemWindows;
    }

    public void add(View view, int i, int i2, int i3) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i, i2, 2034, 545521704, -3);
        this.mLp = layoutParams;
        layoutParams.token = new Binder();
        this.mLp.setTitle("DockedStackDivider");
        WindowManager.LayoutParams layoutParams2 = this.mLp;
        layoutParams2.privateFlags |= 64;
        layoutParams2.layoutInDisplayCutoutMode = 3;
        view.setSystemUiVisibility(1792);
        this.mSystemWindows.addView(view, this.mLp, i3, 2034);
        this.mView = view;
    }

    public void remove() {
        View view = this.mView;
        if (view != null) {
            this.mSystemWindows.removeView(view);
        }
        this.mView = null;
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0022  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0012  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void setSlippery(boolean r6) {
        /*
            r5 = this;
            r0 = 1
            r1 = 536870912(0x20000000, float:1.0842022E-19)
            if (r6 == 0) goto L12
            android.view.WindowManager$LayoutParams r2 = r5.mLp
            int r3 = r2.flags
            r4 = r3 & r1
            if (r4 != 0) goto L12
            r6 = r3 | r1
            r2.flags = r6
            goto L23
        L12:
            if (r6 != 0) goto L22
            android.view.WindowManager$LayoutParams r6 = r5.mLp
            int r2 = r6.flags
            r1 = r1 & r2
            if (r1 == 0) goto L22
            r1 = -536870913(0xffffffffdfffffff, float:-3.6893486E19)
            r1 = r1 & r2
            r6.flags = r1
            goto L23
        L22:
            r0 = 0
        L23:
            if (r0 == 0) goto L2e
            com.android.systemui.wm.SystemWindows r6 = r5.mSystemWindows
            android.view.View r0 = r5.mView
            android.view.WindowManager$LayoutParams r5 = r5.mLp
            r6.updateViewLayout(r0, r5)
        L2e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.DividerWindowManager.setSlippery(boolean):void");
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0017  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void setTouchable(boolean r7) {
        /*
            r6 = this;
            android.view.View r0 = r6.mView
            if (r0 != 0) goto L5
            return
        L5:
            r1 = 0
            r2 = 1
            if (r7 != 0) goto L17
            android.view.WindowManager$LayoutParams r3 = r6.mLp
            int r4 = r3.flags
            r5 = r4 & 16
            if (r5 != 0) goto L17
            r7 = r4 | 16
            r3.flags = r7
        L15:
            r1 = r2
            goto L26
        L17:
            if (r7 == 0) goto L26
            android.view.WindowManager$LayoutParams r7 = r6.mLp
            int r3 = r7.flags
            r4 = r3 & 16
            if (r4 == 0) goto L26
            r1 = r3 & (-17)
            r7.flags = r1
            goto L15
        L26:
            if (r1 == 0) goto L2f
            com.android.systemui.wm.SystemWindows r7 = r6.mSystemWindows
            android.view.WindowManager$LayoutParams r6 = r6.mLp
            r7.updateViewLayout(r0, r6)
        L2f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.DividerWindowManager.setTouchable(boolean):void");
    }

    public void setTouchRegion(Region region) {
        View view = this.mView;
        if (view == null) {
            return;
        }
        this.mSystemWindows.setTouchableRegion(view, region);
    }
}
