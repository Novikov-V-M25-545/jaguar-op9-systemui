package com.android.systemui.shortcut;

import android.content.Context;
import android.content.res.Resources;
import android.os.RemoteException;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.shortcut.ShortcutKeyServiceProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerView;

/* loaded from: classes.dex */
public class ShortcutKeyDispatcher extends SystemUI implements ShortcutKeyServiceProxy.Callbacks {
    protected final long ALT_MASK;
    protected final long CTRL_MASK;
    protected final long META_MASK;
    protected final long SC_DOCK_LEFT;
    protected final long SC_DOCK_RIGHT;
    protected final long SHIFT_MASK;
    private final Divider mDivider;
    private final Recents mRecents;
    private ShortcutKeyServiceProxy mShortcutKeyServiceProxy;
    private IWindowManager mWindowManagerService;

    public ShortcutKeyDispatcher(Context context, Divider divider, Recents recents) {
        super(context);
        this.mShortcutKeyServiceProxy = new ShortcutKeyServiceProxy(this);
        this.mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        this.META_MASK = 281474976710656L;
        this.ALT_MASK = 8589934592L;
        this.CTRL_MASK = 17592186044416L;
        this.SHIFT_MASK = 4294967296L;
        this.SC_DOCK_LEFT = 281474976710727L;
        this.SC_DOCK_RIGHT = 281474976710728L;
        this.mDivider = divider;
        this.mRecents = recents;
    }

    public void registerShortcutKey(long j) {
        try {
            this.mWindowManagerService.registerShortcutKey(j, this.mShortcutKeyServiceProxy);
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.shortcut.ShortcutKeyServiceProxy.Callbacks
    public void onShortcutKeyPressed(long j) throws Resources.NotFoundException {
        int i = this.mContext.getResources().getConfiguration().orientation;
        if ((j == 281474976710727L || j == 281474976710728L) && i == 2) {
            handleDockKey(j);
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        registerShortcutKey(281474976710727L);
        registerShortcutKey(281474976710728L);
    }

    private void handleDockKey(long j) throws Resources.NotFoundException {
        DividerSnapAlgorithm.SnapTarget nextTarget;
        Divider divider = this.mDivider;
        if (divider == null || !divider.isDividerVisible()) {
            this.mRecents.splitPrimaryTask(j != 281474976710727L ? 1 : 0, null, -1);
            return;
        }
        DividerView view = this.mDivider.getView();
        DividerSnapAlgorithm snapAlgorithm = view.getSnapAlgorithm();
        DividerSnapAlgorithm.SnapTarget snapTargetCalculateNonDismissingSnapTarget = snapAlgorithm.calculateNonDismissingSnapTarget(view.getCurrentPosition());
        if (j == 281474976710727L) {
            nextTarget = snapAlgorithm.getPreviousTarget(snapTargetCalculateNonDismissingSnapTarget);
        } else {
            nextTarget = snapAlgorithm.getNextTarget(snapTargetCalculateNonDismissingSnapTarget);
        }
        view.startDragging(true, false);
        view.stopDragging(nextTarget.position, 0.0f, false, true);
    }
}
