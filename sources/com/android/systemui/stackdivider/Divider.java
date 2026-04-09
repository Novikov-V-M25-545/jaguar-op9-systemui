package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.ViewGroup;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.TransactionPool;
import com.android.systemui.recents.Recents;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.stackdivider.DividerView;
import com.android.systemui.stackdivider.SyncTransactionQueue;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.DisplayLayout;
import com.android.systemui.wm.SystemWindows;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/* loaded from: classes.dex */
public class Divider extends SystemUI implements DividerView.DividerCallbacks, DisplayController.OnDisplaysChangedListener {
    private TaskStackChangeListener mActivityRestartListener;
    private boolean mAdjustedForIme;
    private DisplayController mDisplayController;
    private final DividerState mDividerState;
    private final ArrayList<WeakReference<Consumer<Boolean>>> mDockedStackExistsListeners;
    private ForcedResizableInfoActivityController mForcedResizableController;
    private Handler mHandler;
    private boolean mHomeStackResizable;
    private DisplayImeController mImeController;
    private final DividerImeController mImePositionProcessor;
    private KeyguardStateController mKeyguardStateController;
    private boolean mMinimized;
    private final Optional<Lazy<Recents>> mRecentsOptionalLazy;
    private SplitDisplayLayout mRotateSplitLayout;
    private DisplayChangeController.OnDisplayChangingListener mRotationController;
    private SplitDisplayLayout mSplitLayout;
    private SplitScreenTaskOrganizer mSplits;
    private SystemWindows mSystemWindows;
    final TransactionPool mTransactionPool;
    private DividerView mView;
    private boolean mVisible;
    private DividerWindowManager mWindowManager;
    private WindowManagerProxy mWindowManagerProxy;

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        int currentPosition;
        if (!this.mSplits.isSplitScreenSupported() || this.mWindowManagerProxy == null) {
            return;
        }
        WindowContainerTransaction windowContainerTransaction2 = new WindowContainerTransaction();
        SplitDisplayLayout splitDisplayLayout = new SplitDisplayLayout(this.mContext, new DisplayLayout(this.mDisplayController.getDisplayLayout(i)), this.mSplits);
        splitDisplayLayout.rotateTo(i3);
        this.mRotateSplitLayout = splitDisplayLayout;
        if (isDividerVisible()) {
            currentPosition = this.mMinimized ? this.mView.mSnapTargetBeforeMinimized.position : this.mView.getCurrentPosition();
        } else {
            currentPosition = splitDisplayLayout.getSnapAlgorithm().getMiddleTarget().position;
        }
        splitDisplayLayout.resizeSplits(splitDisplayLayout.getSnapAlgorithm().calculateNonDismissingSnapTarget(currentPosition).position, windowContainerTransaction2);
        if (isSplitActive() && this.mHomeStackResizable) {
            WindowManagerProxy.applyHomeTasksMinimized(splitDisplayLayout, this.mSplits.mSecondary.token, windowContainerTransaction2);
        }
        if (this.mWindowManagerProxy.queueSyncTransactionIfWaiting(windowContainerTransaction2)) {
            Slog.w("Divider", "Screen rotated while other operations were pending, this may result in some graphical artifacts.");
        } else {
            windowContainerTransaction.merge(windowContainerTransaction2, true);
        }
    }

    public Divider(Context context, Optional<Lazy<Recents>> optional, DisplayController displayController, SystemWindows systemWindows, DisplayImeController displayImeController, Handler handler, KeyguardStateController keyguardStateController, TransactionPool transactionPool) {
        super(context);
        this.mDividerState = new DividerState();
        this.mVisible = false;
        this.mMinimized = false;
        this.mAdjustedForIme = false;
        this.mHomeStackResizable = false;
        this.mDockedStackExistsListeners = new ArrayList<>();
        this.mSplits = new SplitScreenTaskOrganizer(this);
        this.mRotationController = new DisplayChangeController.OnDisplayChangingListener() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda2
            @Override // com.android.systemui.wm.DisplayChangeController.OnDisplayChangingListener
            public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
                this.f$0.lambda$new$0(i, i2, i3, windowContainerTransaction);
            }
        };
        this.mActivityRestartListener = new TaskStackChangeListener() { // from class: com.android.systemui.stackdivider.Divider.1
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
                if (z3 && runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 3 && Divider.this.mSplits.isSplitScreenSupported() && Divider.this.isMinimized()) {
                    Divider.this.onUndockingTask();
                }
            }
        };
        this.mDisplayController = displayController;
        this.mSystemWindows = systemWindows;
        this.mImeController = displayImeController;
        this.mHandler = handler;
        this.mKeyguardStateController = keyguardStateController;
        this.mRecentsOptionalLazy = optional;
        this.mForcedResizableController = new ForcedResizableInfoActivityController(context, this);
        this.mTransactionPool = transactionPool;
        this.mWindowManagerProxy = new WindowManagerProxy(transactionPool, this.mHandler);
        this.mImePositionProcessor = new DividerImeController(this.mSplits, transactionPool, this.mHandler);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mWindowManager = new DividerWindowManager(this.mSystemWindows);
        this.mDisplayController.addDisplayWindowListener(this);
        this.mKeyguardStateController.addCallback(new KeyguardStateController.Callback() { // from class: com.android.systemui.stackdivider.Divider.2
            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardFadingAwayChanged() {
            }

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onUnlockedChanged() {
            }

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardShowingChanged() {
                if (!Divider.this.isSplitActive() || Divider.this.mView == null) {
                    return;
                }
                Divider.this.mView.setHidden(Divider.this.mKeyguardStateController.isShowing());
                if (Divider.this.mKeyguardStateController.isShowing()) {
                    return;
                }
                Divider.this.mImePositionProcessor.updateAdjustForIme();
            }
        });
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayAdded(int i) {
        if (i != 0) {
            return;
        }
        this.mSplitLayout = new SplitDisplayLayout(this.mDisplayController.getDisplayContext(i), this.mDisplayController.getDisplayLayout(i), this.mSplits);
        this.mImeController.addPositionProcessor(this.mImePositionProcessor);
        this.mDisplayController.addDisplayChangingController(this.mRotationController);
        if (!ActivityTaskManager.supportsSplitScreenMultiWindow(this.mContext)) {
            removeDivider();
            return;
        }
        try {
            this.mSplits.init();
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            this.mSplitLayout.resizeSplits(this.mSplitLayout.getSnapAlgorithm().getMiddleTarget().position, windowContainerTransaction);
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mActivityRestartListener);
        } catch (Exception e) {
            Slog.e("Divider", "Failed to register docked stack listener", e);
            removeDivider();
        }
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayConfigurationChanged(int i, Configuration configuration) throws Resources.NotFoundException {
        if (i == 0 && this.mSplits.isSplitScreenSupported()) {
            SplitDisplayLayout splitDisplayLayout = new SplitDisplayLayout(this.mDisplayController.getDisplayContext(i), this.mDisplayController.getDisplayLayout(i), this.mSplits);
            this.mSplitLayout = splitDisplayLayout;
            if (this.mRotateSplitLayout == null) {
                int i2 = splitDisplayLayout.getSnapAlgorithm().getMiddleTarget().position;
                WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
                this.mSplitLayout.resizeSplits(i2, windowContainerTransaction);
                WindowOrganizer.applyTransaction(windowContainerTransaction);
            } else if (splitDisplayLayout.mDisplayLayout.rotation() == this.mRotateSplitLayout.mDisplayLayout.rotation()) {
                this.mSplitLayout.mPrimary = new Rect(this.mRotateSplitLayout.mPrimary);
                this.mSplitLayout.mSecondary = new Rect(this.mRotateSplitLayout.mSecondary);
                this.mRotateSplitLayout = null;
            }
            if (isSplitActive()) {
                update(configuration);
            }
        }
    }

    Handler getHandler() {
        return this.mHandler;
    }

    public DividerView getView() {
        return this.mView;
    }

    public boolean isMinimized() {
        return this.mMinimized;
    }

    public boolean isHomeStackResizable() {
        return this.mHomeStackResizable;
    }

    public boolean isDividerVisible() {
        DividerView dividerView = this.mView;
        return dividerView != null && dividerView.getVisibility() == 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSplitActive() {
        ActivityManager.RunningTaskInfo runningTaskInfo;
        SplitScreenTaskOrganizer splitScreenTaskOrganizer = this.mSplits;
        ActivityManager.RunningTaskInfo runningTaskInfo2 = splitScreenTaskOrganizer.mPrimary;
        return (runningTaskInfo2 == null || (runningTaskInfo = splitScreenTaskOrganizer.mSecondary) == null || (runningTaskInfo2.topActivityType == 0 && runningTaskInfo.topActivityType == 0)) ? false : true;
    }

    private void addDivider(Configuration configuration) throws Resources.NotFoundException {
        Context displayContext = this.mDisplayController.getDisplayContext(this.mContext.getDisplayId());
        this.mView = (DividerView) LayoutInflater.from(displayContext).inflate(R.layout.docked_stack_divider, (ViewGroup) null);
        DisplayLayout displayLayout = this.mDisplayController.getDisplayLayout(this.mContext.getDisplayId());
        this.mView.injectDependencies(this.mWindowManager, this.mDividerState, this, this.mSplits, this.mSplitLayout, this.mImePositionProcessor, this.mWindowManagerProxy);
        this.mView.setVisibility(this.mVisible ? 0 : 4);
        this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable, (SurfaceControl.Transaction) null);
        int dimensionPixelSize = displayContext.getResources().getDimensionPixelSize(android.R.dimen.date_picker_date_label_size);
        boolean z = configuration.orientation == 2;
        int iWidth = z ? dimensionPixelSize : displayLayout.width();
        if (z) {
            dimensionPixelSize = displayLayout.height();
        }
        this.mWindowManager.add(this.mView, iWidth, dimensionPixelSize, this.mContext.getDisplayId());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeDivider() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDividerRemoved();
        }
        this.mWindowManager.remove();
    }

    private void update(Configuration configuration) throws Resources.NotFoundException {
        boolean z = this.mView != null && this.mKeyguardStateController.isShowing();
        removeDivider();
        addDivider(configuration);
        if (this.mMinimized) {
            this.mView.setMinimizedDockStack(true, this.mHomeStackResizable, (SurfaceControl.Transaction) null);
            updateTouchable();
        }
        this.mView.setHidden(z);
    }

    void onTaskVanished() {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.removeDivider();
            }
        });
    }

    private void updateVisibility(final boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            this.mView.setVisibility(z ? 0 : 4);
            if (z) {
                this.mView.enterSplitMode(this.mHomeStackResizable);
                this.mWindowManagerProxy.runInSync(new SyncTransactionQueue.TransactionRunnable() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda0
                    @Override // com.android.systemui.stackdivider.SyncTransactionQueue.TransactionRunnable
                    public final void runWithTransaction(SurfaceControl.Transaction transaction) {
                        this.f$0.lambda$updateVisibility$1(transaction);
                    }
                });
            } else {
                this.mView.exitSplitMode();
                this.mWindowManagerProxy.runInSync(new SyncTransactionQueue.TransactionRunnable() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda1
                    @Override // com.android.systemui.stackdivider.SyncTransactionQueue.TransactionRunnable
                    public final void runWithTransaction(SurfaceControl.Transaction transaction) {
                        this.f$0.lambda$updateVisibility$2(transaction);
                    }
                });
            }
            synchronized (this.mDockedStackExistsListeners) {
                this.mDockedStackExistsListeners.removeIf(new Predicate() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda6
                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return Divider.lambda$updateVisibility$3(z, (WeakReference) obj);
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateVisibility$1(SurfaceControl.Transaction transaction) {
        this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable, transaction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateVisibility$2(SurfaceControl.Transaction transaction) {
        this.mView.setMinimizedDockStack(false, this.mHomeStackResizable, transaction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$updateVisibility$3(boolean z, WeakReference weakReference) {
        Consumer consumer = (Consumer) weakReference.get();
        if (consumer != null) {
            consumer.accept(Boolean.valueOf(z));
        }
        return consumer == null;
    }

    public void setMinimized(final boolean z) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setMinimized$4(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setMinimized$4(boolean z) {
        if (this.mVisible) {
            setHomeMinimized(z, this.mHomeStackResizable);
        }
    }

    private void setHomeMinimized(boolean z, boolean z2) {
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        boolean z3 = true;
        boolean z4 = this.mMinimized != z;
        if (z4) {
            this.mMinimized = z;
        }
        windowContainerTransaction.setFocusable(this.mSplits.mPrimary.token, !this.mMinimized);
        boolean z5 = this.mHomeStackResizable != z2;
        if (z5) {
            this.mHomeStackResizable = z2;
            if (isDividerVisible()) {
                WindowManagerProxy.applyHomeTasksMinimized(this.mSplitLayout, this.mSplits.mSecondary.token, windowContainerTransaction);
                z3 = false;
            }
        }
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            int displayId = dividerView.getDisplay() != null ? this.mView.getDisplay().getDisplayId() : 0;
            if (this.mMinimized) {
                this.mImePositionProcessor.pause(displayId);
            }
            if (z4 || z5) {
                this.mView.setMinimizedDockStack(z, getAnimDuration(), z2);
            }
            if (!this.mMinimized) {
                this.mImePositionProcessor.resume(displayId);
            }
        }
        updateTouchable();
        if (z3) {
            if (this.mSplits.mDivider.getWmProxy().queueSyncTransactionIfWaiting(windowContainerTransaction)) {
                return;
            }
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            return;
        }
        this.mWindowManagerProxy.applySyncTransaction(windowContainerTransaction);
    }

    void setAdjustedForIme(boolean z) {
        if (this.mAdjustedForIme == z) {
            return;
        }
        this.mAdjustedForIme = z;
        updateTouchable();
    }

    private void updateTouchable() {
        this.mWindowManager.setTouchable(!this.mAdjustedForIme);
    }

    public void onRecentsDrawn() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onRecentsDrawn();
        }
    }

    public void onUndockingTask() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onUndockingTask();
        }
    }

    public void onDockedFirstAnimationFrame() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedFirstAnimationFrame();
        }
    }

    public void onDockedTopTask() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedTopTask();
        }
    }

    public void onAppTransitionFinished() {
        if (this.mView == null) {
            return;
        }
        this.mForcedResizableController.onAppTransitionFinished();
    }

    @Override // com.android.systemui.stackdivider.DividerView.DividerCallbacks
    public void onDraggingStart() {
        this.mForcedResizableController.onDraggingStart();
    }

    @Override // com.android.systemui.stackdivider.DividerView.DividerCallbacks
    public void onDraggingEnd() {
        this.mForcedResizableController.onDraggingEnd();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$growRecents$5(Lazy lazy) {
        ((Recents) lazy.get()).growRecents();
    }

    @Override // com.android.systemui.stackdivider.DividerView.DividerCallbacks
    public void growRecents() {
        this.mRecentsOptionalLazy.ifPresent(new Consumer() { // from class: com.android.systemui.stackdivider.Divider$$ExternalSyntheticLambda5
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Divider.lambda$growRecents$5((Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mVisible=");
        printWriter.println(this.mVisible);
        printWriter.print("  mMinimized=");
        printWriter.println(this.mMinimized);
        printWriter.print("  mAdjustedForIme=");
        printWriter.println(this.mAdjustedForIme);
    }

    long getAnimDuration() {
        return (long) (Settings.Global.getFloat(this.mContext.getContentResolver(), "transition_animation_scale", this.mContext.getResources().getFloat(android.R.dimen.car_seekbar_thumb_stroke)) * 336.0f);
    }

    public void registerInSplitScreenListener(Consumer<Boolean> consumer) {
        consumer.accept(Boolean.valueOf(isDividerVisible()));
        synchronized (this.mDockedStackExistsListeners) {
            this.mDockedStackExistsListeners.add(new WeakReference<>(consumer));
        }
    }

    void startEnterSplit() throws Resources.NotFoundException {
        update(this.mDisplayController.getDisplayContext(this.mContext.getDisplayId()).getResources().getConfiguration());
        this.mHomeStackResizable = this.mWindowManagerProxy.applyEnterSplit(this.mSplits, this.mSplitLayout);
    }

    void startDismissSplit() {
        this.mWindowManagerProxy.lambda$dismissOrMaximizeDocked$0(this.mSplits, this.mSplitLayout, true);
        updateVisibility(false);
        this.mMinimized = false;
        removeDivider();
        this.mImePositionProcessor.reset();
    }

    void ensureMinimizedSplit() {
        setHomeMinimized(true, this.mHomeStackResizable);
        if (this.mView == null || isDividerVisible()) {
            return;
        }
        updateVisibility(true);
    }

    void ensureNormalSplit() {
        setHomeMinimized(false, this.mHomeStackResizable);
        if (this.mView == null || isDividerVisible()) {
            return;
        }
        updateVisibility(true);
    }

    SplitDisplayLayout getSplitLayout() {
        return this.mSplitLayout;
    }

    WindowManagerProxy getWmProxy() {
        return this.mWindowManagerProxy;
    }

    public WindowContainerToken getSecondaryRoot() {
        ActivityManager.RunningTaskInfo runningTaskInfo;
        SplitScreenTaskOrganizer splitScreenTaskOrganizer = this.mSplits;
        if (splitScreenTaskOrganizer == null || (runningTaskInfo = splitScreenTaskOrganizer.mSecondary) == null) {
            return null;
        }
        return runningTaskInfo.token;
    }
}
