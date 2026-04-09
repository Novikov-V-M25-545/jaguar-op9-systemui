package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.IPinnedStackController;
import android.window.WindowContainerTransaction;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.PipAnimationController;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.InputConsumerController;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class PipManager implements BasePipManager, PipTaskOrganizer.PipTransitionCallback {
    private PipAppOpsListener mAppOpsListener;
    private Context mContext;
    private InputConsumerController mInputConsumerController;
    private boolean mIsInFixedRotation;
    private PipMediaController mMediaController;
    protected PipMenuActivityController mMenuController;
    private IPinnedStackAnimationListener mPinnedStackAnimationRecentsListener;
    private PipBoundsHandler mPipBoundsHandler;
    private PipTaskOrganizer mPipTaskOrganizer;
    private PipTouchHandler mTouchHandler;
    private Handler mHandler = new Handler();
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final Rect mTmpInsetBounds = new Rect();
    private final Rect mTmpNormalBounds = new Rect();
    private final Rect mReentryBounds = new Rect();
    private final DisplayChangeController.OnDisplayChangingListener mRotationController = new DisplayChangeController.OnDisplayChangingListener() { // from class: com.android.systemui.pip.phone.PipManager$$ExternalSyntheticLambda0
        @Override // com.android.systemui.wm.DisplayChangeController.OnDisplayChangingListener
        public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
            this.f$0.lambda$new$0(i, i2, i3, windowContainerTransaction);
        }
    };
    private DisplayController.OnDisplaysChangedListener mFixedRotationListener = new DisplayController.OnDisplaysChangedListener() { // from class: com.android.systemui.pip.phone.PipManager.1
        @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
        public void onFixedRotationStarted(int i, int i2) {
            PipManager.this.mIsInFixedRotation = true;
        }

        @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
        public void onFixedRotationFinished(int i) {
            PipManager.this.mIsInFixedRotation = false;
        }
    };
    private final TaskStackChangeListener mTaskStackListener = new AnonymousClass2();
    private IActivityManager mActivityManager = ActivityManager.getService();

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        if (!this.mPipTaskOrganizer.isInPip() || this.mPipTaskOrganizer.isDeferringEnterPipAnimation()) {
            this.mPipBoundsHandler.onDisplayRotationChangedNotInPip(i3);
            return;
        }
        if (this.mPipBoundsHandler.onDisplayRotationChanged(this.mTmpNormalBounds, this.mPipTaskOrganizer.getCurrentOrAnimatingBounds(), this.mTmpInsetBounds, i, i2, i3, windowContainerTransaction)) {
            this.mTouchHandler.adjustBoundsForRotation(this.mTmpNormalBounds, this.mPipTaskOrganizer.getLastReportedBounds(), this.mTmpInsetBounds);
            if (!this.mIsInFixedRotation) {
                this.mPipBoundsHandler.setShelfHeight(false, 0);
                this.mPipBoundsHandler.onImeVisibilityChanged(false, 0);
                this.mTouchHandler.onShelfVisibilityChanged(false, 0);
                this.mTouchHandler.onImeVisibilityChanged(false, 0);
            }
            updateMovementBounds(this.mTmpNormalBounds, true, false, false, windowContainerTransaction);
        }
    }

    /* renamed from: com.android.systemui.pip.phone.PipManager$2, reason: invalid class name */
    class AnonymousClass2 extends TaskStackChangeListener {
        AnonymousClass2() {
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityPinned(String str, int i, int i2, int i3) {
            PipManager.this.mTouchHandler.onActivityPinned();
            PipManager.this.mMediaController.onActivityPinned();
            PipManager.this.mMenuController.onActivityPinned();
            PipManager.this.mAppOpsListener.onActivityPinned(str);
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$2$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    PipManager.AnonymousClass2.lambda$onActivityPinned$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$onActivityPinned$0() {
            WindowManagerWrapper.getInstance().setPipVisibility(true);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityUnpinned() throws RemoteException {
            final ComponentName componentName = (ComponentName) PipUtils.getTopPipActivity(PipManager.this.mContext, PipManager.this.mActivityManager).first;
            PipManager.this.mMenuController.onActivityUnpinned();
            PipManager.this.mTouchHandler.onActivityUnpinned(componentName);
            PipManager.this.mAppOpsListener.onActivityUnpinned();
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$2$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    PipManager.AnonymousClass2.lambda$onActivityUnpinned$1(componentName);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$onActivityUnpinned$1(ComponentName componentName) {
            WindowManagerWrapper.getInstance().setPipVisibility(componentName != null);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (runningTaskInfo.configuration.windowConfiguration.getWindowingMode() != 2) {
                return;
            }
            PipManager.this.mTouchHandler.getMotionHelper().expandPipToFullscreen(z2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class PipManagerPinnedStackListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private PipManagerPinnedStackListener() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onListenerRegistered$0(IPinnedStackController iPinnedStackController) {
            PipManager.this.mTouchHandler.setPinnedStackController(iPinnedStackController);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onListenerRegistered(final IPinnedStackController iPinnedStackController) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onListenerRegistered$0(iPinnedStackController);
                }
            });
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(final boolean z, final int i) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onImeVisibilityChanged$1(z, i);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onImeVisibilityChanged$1(boolean z, int i) {
            PipManager.this.mPipBoundsHandler.onImeVisibilityChanged(z, i);
            PipManager.this.mTouchHandler.onImeVisibilityChanged(z, i);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onMovementBoundsChanged$2(boolean z) {
            PipManager.this.updateMovementBounds(null, false, z, false, null);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onMovementBoundsChanged(final boolean z) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onMovementBoundsChanged$2(z);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActionsChanged$3(ParceledListSlice parceledListSlice) throws RemoteException {
            PipManager.this.mMenuController.setAppActions(parceledListSlice);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onActionsChanged(final ParceledListSlice parceledListSlice) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() throws RemoteException {
                    this.f$0.lambda$onActionsChanged$3(parceledListSlice);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onActivityHidden$4(ComponentName componentName) {
            PipManager.this.mPipBoundsHandler.onResetReentryBounds(componentName);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onActivityHidden(final ComponentName componentName) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onActivityHidden$4(componentName);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onDisplayInfoChanged$5(DisplayInfo displayInfo) {
            PipManager.this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onDisplayInfoChanged(final DisplayInfo displayInfo) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onDisplayInfoChanged$5(displayInfo);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onConfigurationChanged$6() throws Resources.NotFoundException {
            PipManager.this.mPipBoundsHandler.onConfigurationChanged();
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onConfigurationChanged() {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() throws Resources.NotFoundException {
                    this.f$0.lambda$onConfigurationChanged$6();
                }
            });
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onAspectRatioChanged(final float f) {
            PipManager.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$PipManagerPinnedStackListener$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onAspectRatioChanged$7(f);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onAspectRatioChanged$7(float f) {
            PipManager.this.mPipBoundsHandler.onAspectRatioChanged(f);
            PipManager.this.mTouchHandler.onAspectRatioChanged();
        }
    }

    public PipManager(Context context, BroadcastDispatcher broadcastDispatcher, DisplayController displayController, FloatingContentCoordinator floatingContentCoordinator, DeviceConfigProxy deviceConfigProxy, PipBoundsHandler pipBoundsHandler, PipSnapAlgorithm pipSnapAlgorithm, PipTaskOrganizer pipTaskOrganizer, SysUiState sysUiState, PipUiEventLogger pipUiEventLogger) {
        this.mContext = context;
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new PipManagerPinnedStackListener());
        } catch (RemoteException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        pipTaskOrganizer.registerPipTransitionCallback(this);
        this.mInputConsumerController = InputConsumerController.getPipInputConsumer();
        PipMediaController pipMediaController = new PipMediaController(context, this.mActivityManager, broadcastDispatcher);
        this.mMediaController = pipMediaController;
        PipMenuActivityController pipMenuActivityController = new PipMenuActivityController(context, pipMediaController, this.mInputConsumerController);
        this.mMenuController = pipMenuActivityController;
        this.mTouchHandler = new PipTouchHandler(context, this.mActivityManager, pipMenuActivityController, this.mInputConsumerController, this.mPipBoundsHandler, this.mPipTaskOrganizer, floatingContentCoordinator, deviceConfigProxy, pipSnapAlgorithm, sysUiState, pipUiEventLogger);
        this.mAppOpsListener = new PipAppOpsListener(context, this.mActivityManager, this.mTouchHandler.getMotionHelper());
        displayController.addDisplayChangingController(this.mRotationController);
        displayController.addDisplayWindowListener(this.mFixedRotationListener);
        DisplayInfo displayInfo = new DisplayInfo();
        context.getDisplay().getDisplayInfo(displayInfo);
        this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
        try {
            this.mPipTaskOrganizer.registerOrganizer(2);
            if (ActivityTaskManager.getService().getStackInfo(2, 0) != null) {
                this.mInputConsumerController.registerInputConsumer(true);
            }
        } catch (RemoteException | UnsupportedOperationException e2) {
            e2.printStackTrace();
        }
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        this.mTouchHandler.onConfigurationChanged();
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void showPictureInPictureMenu() throws RemoteException {
        this.mTouchHandler.showPictureInPictureMenu();
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void setShelfHeight(final boolean z, final int i) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setShelfHeight$1(z, i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setShelfHeight$1(boolean z, int i) {
        if (!z) {
            i = 0;
        }
        if (this.mPipBoundsHandler.setShelfHeight(z, i)) {
            this.mTouchHandler.onShelfVisibilityChanged(z, i);
            updateMovementBounds(this.mPipTaskOrganizer.getLastReportedBounds(), false, false, true, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setPinnedStackAnimationType$2(int i) {
        this.mPipTaskOrganizer.setOneShotAnimationType(i);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void setPinnedStackAnimationType(final int i) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setPinnedStackAnimationType$2(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setPinnedStackAnimationListener$3(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
        this.mPinnedStackAnimationRecentsListener = iPinnedStackAnimationListener;
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void setPinnedStackAnimationListener(final IPinnedStackAnimationListener iPinnedStackAnimationListener) {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipManager$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$setPinnedStackAnimationListener$3(iPinnedStackAnimationListener);
            }
        });
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionStarted(ComponentName componentName, int i) {
        if (PipAnimationController.isOutPipDirection(i)) {
            updateReentryBounds();
            this.mPipBoundsHandler.onSaveReentryBounds(componentName, this.mReentryBounds);
        }
        this.mTouchHandler.setTouchEnabled(false);
        IPinnedStackAnimationListener iPinnedStackAnimationListener = this.mPinnedStackAnimationRecentsListener;
        if (iPinnedStackAnimationListener != null) {
            try {
                iPinnedStackAnimationListener.onPinnedStackAnimationStarted();
            } catch (RemoteException e) {
                Log.e("PipManager", "Failed to callback recents", e);
            }
        }
    }

    public void updateReentryBounds() {
        Rect userResizeBounds = this.mTouchHandler.getUserResizeBounds();
        this.mPipBoundsHandler.applySnapFraction(userResizeBounds, this.mPipBoundsHandler.getSnapFraction(this.mPipTaskOrganizer.getLastReportedBounds()));
        this.mReentryBounds.set(userResizeBounds);
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionFinished(ComponentName componentName, int i) throws RemoteException {
        onPipTransitionFinishedOrCanceled(i);
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionCanceled(ComponentName componentName, int i) throws RemoteException {
        onPipTransitionFinishedOrCanceled(i);
    }

    private void onPipTransitionFinishedOrCanceled(int i) throws RemoteException {
        this.mTouchHandler.setTouchEnabled(true);
        this.mTouchHandler.onPinnedStackAnimationEnded(i);
        this.mMenuController.onPinnedStackAnimationEnded();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMovementBounds(Rect rect, boolean z, boolean z2, boolean z3, WindowContainerTransaction windowContainerTransaction) {
        Rect rect2 = new Rect(rect);
        this.mPipBoundsHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, this.mTmpDisplayInfo);
        this.mPipTaskOrganizer.onMovementBoundsChanged(rect2, z, z2, z3, windowContainerTransaction);
        this.mTouchHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, z2, z3, this.mTmpDisplayInfo.rotation);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void dump(PrintWriter printWriter) {
        printWriter.println("PipManager");
        this.mInputConsumerController.dump(printWriter, "  ");
        this.mMenuController.dump(printWriter, "  ");
        this.mTouchHandler.dump(printWriter, "  ");
        this.mPipBoundsHandler.dump(printWriter, "  ");
        this.mPipTaskOrganizer.dump(printWriter, "  ");
    }
}
