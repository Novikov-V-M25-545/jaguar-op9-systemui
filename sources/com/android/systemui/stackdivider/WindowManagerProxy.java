package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.WindowManagerGlobal;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.TransactionPool;
import com.android.systemui.stackdivider.SyncTransactionQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/* loaded from: classes.dex */
public class WindowManagerProxy {
    private static final int[] HOME_AND_RECENTS = {2, 3};
    private final SyncTransactionQueue mSyncTransactionQueue;

    @GuardedBy({"mDockedRect"})
    private final Rect mDockedRect = new Rect();
    private final Rect mTmpRect1 = new Rect();

    @GuardedBy({"mDockedRect"})
    private final Rect mTouchableRegion = new Rect();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Runnable mSetTouchableRegionRunnable = new Runnable() { // from class: com.android.systemui.stackdivider.WindowManagerProxy.1
        @Override // java.lang.Runnable
        public void run() {
            try {
                synchronized (WindowManagerProxy.this.mDockedRect) {
                    WindowManagerProxy.this.mTmpRect1.set(WindowManagerProxy.this.mTouchableRegion);
                }
                WindowManagerGlobal.getWindowManagerService().setDockedStackDividerTouchRegion(WindowManagerProxy.this.mTmpRect1);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to set touchable region: " + e);
            }
        }
    };

    WindowManagerProxy(TransactionPool transactionPool, Handler handler) {
        this.mSyncTransactionQueue = new SyncTransactionQueue(transactionPool, handler);
    }

    void dismissOrMaximizeDocked(final SplitScreenTaskOrganizer splitScreenTaskOrganizer, final SplitDisplayLayout splitDisplayLayout, final boolean z) {
        this.mExecutor.execute(new Runnable() { // from class: com.android.systemui.stackdivider.WindowManagerProxy$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$dismissOrMaximizeDocked$0(splitScreenTaskOrganizer, splitDisplayLayout, z);
            }
        });
    }

    public void setResizing(final boolean z) {
        this.mExecutor.execute(new Runnable() { // from class: com.android.systemui.stackdivider.WindowManagerProxy.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    ActivityTaskManager.getService().setSplitScreenResizing(z);
                } catch (RemoteException e) {
                    Log.w("WindowManagerProxy", "Error calling setDockedStackResizing: " + e);
                }
            }
        });
    }

    public void setTouchRegion(Rect rect) {
        synchronized (this.mDockedRect) {
            this.mTouchableRegion.set(rect);
        }
        this.mExecutor.execute(this.mSetTouchableRegionRunnable);
    }

    static void applyResizeSplits(int i, SplitDisplayLayout splitDisplayLayout) {
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        splitDisplayLayout.resizeSplits(i, windowContainerTransaction);
        WindowOrganizer.applyTransaction(windowContainerTransaction);
    }

    private static boolean getHomeAndRecentsTasks(List<ActivityManager.RunningTaskInfo> list, WindowContainerToken windowContainerToken) {
        List childTasks;
        if (windowContainerToken == null) {
            childTasks = TaskOrganizer.getRootTasks(0, HOME_AND_RECENTS);
        } else {
            childTasks = TaskOrganizer.getChildTasks(windowContainerToken, HOME_AND_RECENTS);
        }
        int size = childTasks.size();
        boolean z = false;
        for (int i = 0; i < size; i++) {
            ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) childTasks.get(i);
            list.add(runningTaskInfo);
            if (runningTaskInfo.topActivityType == 2) {
                z = runningTaskInfo.isResizeable;
            }
        }
        return z;
    }

    static boolean applyHomeTasksMinimized(SplitDisplayLayout splitDisplayLayout, WindowContainerToken windowContainerToken, WindowContainerTransaction windowContainerTransaction) {
        Rect rect;
        ArrayList arrayList = new ArrayList();
        boolean homeAndRecentsTasks = getHomeAndRecentsTasks(arrayList, windowContainerToken);
        if (homeAndRecentsTasks) {
            rect = splitDisplayLayout.calcResizableMinimizedHomeStackBounds();
        } else {
            boolean z = false;
            rect = new Rect(0, 0, 0, 0);
            int size = arrayList.size() - 1;
            while (true) {
                if (size < 0) {
                    break;
                }
                if (((ActivityManager.RunningTaskInfo) arrayList.get(size)).topActivityType == 2) {
                    int i = ((ActivityManager.RunningTaskInfo) arrayList.get(size)).configuration.orientation;
                    boolean zIsLandscape = splitDisplayLayout.mDisplayLayout.isLandscape();
                    if (i == 2 || (i == 0 && zIsLandscape)) {
                        z = true;
                    }
                    rect.right = z == zIsLandscape ? splitDisplayLayout.mDisplayLayout.width() : splitDisplayLayout.mDisplayLayout.height();
                    rect.bottom = z == zIsLandscape ? splitDisplayLayout.mDisplayLayout.height() : splitDisplayLayout.mDisplayLayout.width();
                } else {
                    size--;
                }
            }
        }
        for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
            if (!homeAndRecentsTasks) {
                if (((ActivityManager.RunningTaskInfo) arrayList.get(size2)).topActivityType != 3) {
                    windowContainerTransaction.setWindowingMode(((ActivityManager.RunningTaskInfo) arrayList.get(size2)).token, 1);
                    windowContainerTransaction.setBounds(((ActivityManager.RunningTaskInfo) arrayList.get(size2)).token, rect);
                }
            } else {
                windowContainerTransaction.setBounds(((ActivityManager.RunningTaskInfo) arrayList.get(size2)).token, rect);
            }
        }
        splitDisplayLayout.mTiles.mHomeBounds.set(rect);
        return homeAndRecentsTasks;
    }

    boolean applyEnterSplit(SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout) {
        TaskOrganizer.setLaunchRoot(0, splitScreenTaskOrganizer.mSecondary.token);
        List rootTasks = TaskOrganizer.getRootTasks(0, (int[]) null);
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        if (rootTasks.isEmpty()) {
            return false;
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = null;
        for (int size = rootTasks.size() - 1; size >= 0; size--) {
            ActivityManager.RunningTaskInfo runningTaskInfo2 = (ActivityManager.RunningTaskInfo) rootTasks.get(size);
            if ((runningTaskInfo2.isResizeable || runningTaskInfo2.topActivityType == 2) && runningTaskInfo2.configuration.windowConfiguration.getWindowingMode() == 1) {
                runningTaskInfo = isHomeOrRecentTask(runningTaskInfo2) ? runningTaskInfo2 : null;
                windowContainerTransaction.reparent(runningTaskInfo2.token, splitScreenTaskOrganizer.mSecondary.token, true);
            }
        }
        windowContainerTransaction.reorder(splitScreenTaskOrganizer.mSecondary.token, true);
        boolean zApplyHomeTasksMinimized = applyHomeTasksMinimized(splitDisplayLayout, null, windowContainerTransaction);
        if (runningTaskInfo != null) {
            windowContainerTransaction.setBoundsChangeTransaction(runningTaskInfo.token, splitScreenTaskOrganizer.mHomeBounds);
        }
        applySyncTransaction(windowContainerTransaction);
        return zApplyHomeTasksMinimized;
    }

    static boolean isHomeOrRecentTask(ActivityManager.RunningTaskInfo runningTaskInfo) {
        int activityType = runningTaskInfo.configuration.windowConfiguration.getActivityType();
        return activityType == 2 || activityType == 3;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: applyDismissSplit, reason: merged with bridge method [inline-methods] */
    public void lambda$dismissOrMaximizeDocked$0(final SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout, boolean z) {
        int i;
        int i2;
        TaskOrganizer.setLaunchRoot(0, (WindowContainerToken) null);
        List childTasks = TaskOrganizer.getChildTasks(splitScreenTaskOrganizer.mPrimary.token, (int[]) null);
        List childTasks2 = TaskOrganizer.getChildTasks(splitScreenTaskOrganizer.mSecondary.token, (int[]) null);
        List rootTasks = TaskOrganizer.getRootTasks(0, HOME_AND_RECENTS);
        rootTasks.removeIf(new Predicate() { // from class: com.android.systemui.stackdivider.WindowManagerProxy$$ExternalSyntheticLambda1
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return WindowManagerProxy.lambda$applyDismissSplit$1(splitScreenTaskOrganizer, (ActivityManager.RunningTaskInfo) obj);
            }
        });
        if (childTasks.isEmpty() && childTasks2.isEmpty() && rootTasks.isEmpty()) {
            return;
        }
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        if (z) {
            for (int size = childTasks.size() - 1; size >= 0; size--) {
                windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks.get(size)).token, (WindowContainerToken) null, true);
            }
            boolean z2 = false;
            for (int size2 = childTasks2.size() - 1; size2 >= 0; size2--) {
                ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) childTasks2.get(size2);
                windowContainerTransaction.reparent(runningTaskInfo.token, (WindowContainerToken) null, true);
                if (isHomeOrRecentTask(runningTaskInfo)) {
                    windowContainerTransaction.setBounds(runningTaskInfo.token, (Rect) null);
                    windowContainerTransaction.setWindowingMode(runningTaskInfo.token, 0);
                    if (size2 == 0) {
                        z2 = true;
                    }
                }
            }
            if (z2) {
                boolean zIsLandscape = splitDisplayLayout.mDisplayLayout.isLandscape();
                if (zIsLandscape) {
                    i = splitDisplayLayout.mSecondary.left - splitScreenTaskOrganizer.mHomeBounds.left;
                } else {
                    i = splitDisplayLayout.mSecondary.left;
                }
                if (zIsLandscape) {
                    i2 = splitDisplayLayout.mSecondary.top;
                } else {
                    i2 = splitDisplayLayout.mSecondary.top - splitScreenTaskOrganizer.mHomeBounds.top;
                }
                SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                transaction.setPosition(splitScreenTaskOrganizer.mSecondarySurface, i, i2);
                Rect rect = new Rect(0, 0, splitDisplayLayout.mDisplayLayout.width(), splitDisplayLayout.mDisplayLayout.height());
                rect.offset(-i, -i2);
                transaction.setWindowCrop(splitScreenTaskOrganizer.mSecondarySurface, rect);
                windowContainerTransaction.setBoundsChangeTransaction(splitScreenTaskOrganizer.mSecondary.token, transaction);
            }
        } else {
            for (int size3 = childTasks2.size() - 1; size3 >= 0; size3--) {
                if (!isHomeOrRecentTask((ActivityManager.RunningTaskInfo) childTasks2.get(size3))) {
                    windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks2.get(size3)).token, (WindowContainerToken) null, true);
                }
            }
            for (int size4 = childTasks2.size() - 1; size4 >= 0; size4--) {
                ActivityManager.RunningTaskInfo runningTaskInfo2 = (ActivityManager.RunningTaskInfo) childTasks2.get(size4);
                if (isHomeOrRecentTask(runningTaskInfo2)) {
                    windowContainerTransaction.reparent(runningTaskInfo2.token, (WindowContainerToken) null, true);
                    windowContainerTransaction.setBounds(runningTaskInfo2.token, (Rect) null);
                    windowContainerTransaction.setWindowingMode(runningTaskInfo2.token, 0);
                }
            }
            for (int size5 = childTasks.size() - 1; size5 >= 0; size5--) {
                windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks.get(size5)).token, (WindowContainerToken) null, true);
            }
        }
        for (int size6 = rootTasks.size() - 1; size6 >= 0; size6--) {
            windowContainerTransaction.setBounds(((ActivityManager.RunningTaskInfo) rootTasks.get(size6)).token, (Rect) null);
            windowContainerTransaction.setWindowingMode(((ActivityManager.RunningTaskInfo) rootTasks.get(size6)).token, 0);
        }
        windowContainerTransaction.setFocusable(splitScreenTaskOrganizer.mPrimary.token, true);
        applySyncTransaction(windowContainerTransaction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$applyDismissSplit$1(SplitScreenTaskOrganizer splitScreenTaskOrganizer, ActivityManager.RunningTaskInfo runningTaskInfo) {
        return runningTaskInfo.token.equals(splitScreenTaskOrganizer.mSecondary.token) || runningTaskInfo.token.equals(splitScreenTaskOrganizer.mPrimary.token);
    }

    void applySyncTransaction(WindowContainerTransaction windowContainerTransaction) {
        this.mSyncTransactionQueue.queue(windowContainerTransaction);
    }

    boolean queueSyncTransactionIfWaiting(WindowContainerTransaction windowContainerTransaction) {
        return this.mSyncTransactionQueue.queueIfWaiting(windowContainerTransaction);
    }

    void runInSync(SyncTransactionQueue.TransactionRunnable transactionRunnable) {
        this.mSyncTransactionQueue.runInSync(transactionRunnable);
    }
}
