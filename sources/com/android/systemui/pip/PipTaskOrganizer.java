package com.android.systemui.pip;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceControl;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowContainerTransactionCallback;
import android.window.WindowOrganizer;
import com.android.internal.os.SomeArgs;
import com.android.systemui.R;
import com.android.systemui.pip.PipAnimationController;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.phone.PipUpdateThread;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.wm.DisplayController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class PipTaskOrganizer extends TaskOrganizer implements DisplayController.OnDisplaysChangedListener {
    private static final String TAG = PipTaskOrganizer.class.getSimpleName();
    private final int mEnterExitAnimationDuration;
    private SurfaceControl mLeash;
    private final Handler mMainHandler;
    private int mOneShotAnimationType;
    private int mOverridableMinSize;
    private PictureInPictureParams mPictureInPictureParams;
    private final PipAnimationController mPipAnimationController;
    private final PipBoundsHandler mPipBoundsHandler;
    private final PipUiEventLogger mPipUiEventLoggerLogger;
    private int mRequestedOrientation;
    private boolean mShouldDeferEnteringPip;
    private final Divider mSplitDivider;
    private State mState;
    private PipSurfaceTransactionHelper.SurfaceControlTransactionFactory mSurfaceControlTransactionFactory;
    private final PipSurfaceTransactionHelper mSurfaceTransactionHelper;
    private ActivityManager.RunningTaskInfo mTaskInfo;
    private WindowContainerToken mToken;
    private final Handler.Callback mUpdateCallbacks;
    private final Handler mUpdateHandler;
    private final List<PipTransitionCallback> mPipTransitionCallbacks = new ArrayList();
    private final Rect mLastReportedBounds = new Rect();
    private final Map<IBinder, PipWindowConfigurationCompact> mCompactState = new HashMap();
    private final PipAnimationController.PipAnimationCallback mPipAnimationCallback = new PipAnimationController.PipAnimationCallback() { // from class: com.android.systemui.pip.PipTaskOrganizer.1
        @Override // com.android.systemui.pip.PipAnimationController.PipAnimationCallback
        public void onPipAnimationStart(PipAnimationController.PipTransitionAnimator pipTransitionAnimator) {
            PipTaskOrganizer.this.sendOnPipTransitionStarted(pipTransitionAnimator.getTransitionDirection());
        }

        @Override // com.android.systemui.pip.PipAnimationController.PipAnimationCallback
        public void onPipAnimationEnd(SurfaceControl.Transaction transaction, PipAnimationController.PipTransitionAnimator pipTransitionAnimator) {
            PipTaskOrganizer.this.finishResize(transaction, pipTransitionAnimator.getDestinationBounds(), pipTransitionAnimator.getTransitionDirection(), pipTransitionAnimator.getAnimationType());
            PipTaskOrganizer.this.sendOnPipTransitionFinished(pipTransitionAnimator.getTransitionDirection());
        }

        @Override // com.android.systemui.pip.PipAnimationController.PipAnimationCallback
        public void onPipAnimationCancel(PipAnimationController.PipTransitionAnimator pipTransitionAnimator) {
            PipTaskOrganizer.this.sendOnPipTransitionCancelled(pipTransitionAnimator.getTransitionDirection());
        }
    };

    public interface PipTransitionCallback {
        void onPipTransitionCanceled(ComponentName componentName, int i);

        void onPipTransitionFinished(ComponentName componentName, int i);

        void onPipTransitionStarted(ComponentName componentName, int i);
    }

    public int getOutPipWindowingMode() {
        return 0;
    }

    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) {
    }

    private enum State {
        UNDEFINED(0),
        TASK_APPEARED(1),
        ENTERING_PIP(2),
        EXITING_PIP(3);

        private final int mStateValue;

        State(int i) {
            this.mStateValue = i;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isInPip() {
            int i = this.mStateValue;
            return i >= TASK_APPEARED.mStateValue && i != EXITING_PIP.mStateValue;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean shouldBlockResizeRequest() {
            int i = this.mStateValue;
            return i < ENTERING_PIP.mStateValue || i == EXITING_PIP.mStateValue;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$new$0(Message message) {
        SomeArgs someArgs = (SomeArgs) message.obj;
        Consumer consumer = (Consumer) someArgs.arg1;
        int i = message.what;
        if (i == 1) {
            Rect rect = (Rect) someArgs.arg2;
            resizePip(rect);
            if (consumer != null) {
                consumer.accept(rect);
            }
        } else if (i == 2) {
            Rect rect2 = (Rect) someArgs.arg2;
            Rect rect3 = (Rect) someArgs.arg3;
            animateResizePip(rect2, rect3, (Rect) someArgs.arg4, someArgs.argi1, someArgs.argi2);
            if (consumer != null) {
                consumer.accept(rect3);
            }
        } else if (i == 3) {
            Rect rect4 = (Rect) someArgs.arg2;
            int i2 = someArgs.argi1;
            offsetPip(rect4, 0, i2, someArgs.argi2);
            Rect rect5 = new Rect(rect4);
            rect5.offset(0, i2);
            if (consumer != null) {
                consumer.accept(rect5);
            }
        } else if (i == 4) {
            SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) someArgs.arg2;
            Rect rect6 = (Rect) someArgs.arg3;
            finishResize(transaction, rect6, someArgs.argi1, -1);
            if (consumer != null) {
                consumer.accept(rect6);
            }
        } else if (i == 5) {
            userResizePip((Rect) someArgs.arg2, (Rect) someArgs.arg3);
        }
        someArgs.recycle();
        return true;
    }

    public PipTaskOrganizer(Context context, PipBoundsHandler pipBoundsHandler, PipSurfaceTransactionHelper pipSurfaceTransactionHelper, Divider divider, DisplayController displayController, PipAnimationController pipAnimationController, PipUiEventLogger pipUiEventLogger) {
        Handler.Callback callback = new Handler.Callback() { // from class: com.android.systemui.pip.PipTaskOrganizer$$ExternalSyntheticLambda0
            @Override // android.os.Handler.Callback
            public final boolean handleMessage(Message message) {
                return this.f$0.lambda$new$0(message);
            }
        };
        this.mUpdateCallbacks = callback;
        this.mState = State.UNDEFINED;
        this.mOneShotAnimationType = 0;
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mUpdateHandler = new Handler(PipUpdateThread.get().getLooper(), callback);
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mEnterExitAnimationDuration = context.getResources().getInteger(R.integer.config_pipResizeAnimationDuration);
        this.mOverridableMinSize = context.getResources().getDimensionPixelSize(android.R.dimen.kg_widget_pager_bottom_padding);
        this.mSurfaceTransactionHelper = pipSurfaceTransactionHelper;
        this.mPipAnimationController = pipAnimationController;
        this.mPipUiEventLoggerLogger = pipUiEventLogger;
        this.mSurfaceControlTransactionFactory = PipAnimationController$PipTransitionAnimator$$ExternalSyntheticLambda0.INSTANCE;
        this.mSplitDivider = divider;
        displayController.addDisplayWindowListener(this);
    }

    public Handler getUpdateHandler() {
        return this.mUpdateHandler;
    }

    public Rect getLastReportedBounds() {
        return new Rect(this.mLastReportedBounds);
    }

    public Rect getCurrentOrAnimatingBounds() {
        PipAnimationController.PipTransitionAnimator currentAnimator = this.mPipAnimationController.getCurrentAnimator();
        if (currentAnimator != null && currentAnimator.isRunning()) {
            return new Rect(currentAnimator.getDestinationBounds());
        }
        return getLastReportedBounds();
    }

    public boolean isInPip() {
        return this.mState.isInPip();
    }

    public boolean isDeferringEnterPipAnimation() {
        return this.mState.isInPip() && this.mShouldDeferEnteringPip;
    }

    public void registerPipTransitionCallback(PipTransitionCallback pipTransitionCallback) {
        this.mPipTransitionCallbacks.add(pipTransitionCallback);
    }

    public void setOneShotAnimationType(int i) {
        this.mOneShotAnimationType = i;
    }

    public void exitPip(final int i) {
        WindowContainerToken windowContainerToken;
        if (!this.mState.isInPip() || (windowContainerToken = this.mToken) == null) {
            Log.wtf(TAG, "Not allowed to exitPip in current state mState=" + this.mState + " mToken=" + this.mToken);
            return;
        }
        PipWindowConfigurationCompact pipWindowConfigurationCompactRemove = this.mCompactState.remove(windowContainerToken.asBinder());
        if (pipWindowConfigurationCompactRemove == null) {
            Log.wtf(TAG, "Token not in record, this should not happen mToken=" + this.mToken);
            return;
        }
        this.mPipUiEventLoggerLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_EXPAND_TO_FULLSCREEN);
        pipWindowConfigurationCompactRemove.syncWithScreenOrientation(this.mRequestedOrientation, this.mPipBoundsHandler.getDisplayRotation());
        boolean z = pipWindowConfigurationCompactRemove.getRotation() != this.mPipBoundsHandler.getDisplayRotation();
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        final Rect bounds = pipWindowConfigurationCompactRemove.getBounds();
        final int i2 = syncWithSplitScreenBounds(bounds) ? 4 : 3;
        if (z) {
            this.mState = State.EXITING_PIP;
            sendOnPipTransitionStarted(i2);
            applyWindowingModeChangeOnExit(windowContainerTransaction, i2);
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            sendOnPipTransitionFinished(i2);
            return;
        }
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        this.mSurfaceTransactionHelper.scale(transaction, this.mLeash, bounds, this.mLastReportedBounds);
        transaction.setWindowCrop(this.mLeash, bounds.width(), bounds.height());
        windowContainerTransaction.setActivityWindowingMode(this.mToken, i2 == 4 ? 4 : 1);
        windowContainerTransaction.setBounds(this.mToken, bounds);
        windowContainerTransaction.setBoundsChangeTransaction(this.mToken, transaction);
        applySyncTransaction(windowContainerTransaction, new WindowContainerTransactionCallback() { // from class: com.android.systemui.pip.PipTaskOrganizer.2
            public void onTransactionReady(int i3, SurfaceControl.Transaction transaction2) {
                transaction2.apply();
                PipTaskOrganizer pipTaskOrganizer = PipTaskOrganizer.this;
                pipTaskOrganizer.scheduleAnimateResizePip(pipTaskOrganizer.mLastReportedBounds, bounds, null, i2, i, null);
                PipTaskOrganizer.this.mState = State.EXITING_PIP;
            }
        });
    }

    private void applyWindowingModeChangeOnExit(WindowContainerTransaction windowContainerTransaction, int i) {
        windowContainerTransaction.setWindowingMode(this.mToken, getOutPipWindowingMode());
        windowContainerTransaction.setActivityWindowingMode(this.mToken, 0);
        Divider divider = this.mSplitDivider;
        if (divider == null || i != 4) {
            return;
        }
        windowContainerTransaction.reparent(this.mToken, divider.getSecondaryRoot(), true);
    }

    public void removePip() {
        if (!this.mState.isInPip() || this.mToken == null) {
            Log.wtf(TAG, "Not allowed to removePip in current state mState=" + this.mState + " mToken=" + this.mToken);
            return;
        }
        this.mUpdateHandler.post(new Runnable() { // from class: com.android.systemui.pip.PipTaskOrganizer$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$removePip$1();
            }
        });
        this.mCompactState.remove(this.mToken.asBinder());
        this.mState = State.EXITING_PIP;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$removePip$1() {
        this.mPipAnimationController.getAnimator(this.mLeash, this.mLastReportedBounds, 1.0f, 0.0f).setTransitionDirection(5).setPipAnimationCallback(this.mPipAnimationCallback).setDuration(this.mEnterExitAnimationDuration).start();
    }

    private void removePipImmediately() {
        try {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            windowContainerTransaction.setBounds(this.mToken, (Rect) null);
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            ActivityTaskManager.getService().removeStacksInWindowingModes(new int[]{2});
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to remove PiP", e);
        }
    }

    public void onTaskAppeared(ActivityManager.RunningTaskInfo runningTaskInfo, SurfaceControl surfaceControl) {
        Objects.requireNonNull(runningTaskInfo, "Requires RunningTaskInfo");
        this.mTaskInfo = runningTaskInfo;
        WindowContainerToken windowContainerToken = runningTaskInfo.token;
        this.mToken = windowContainerToken;
        this.mState = State.TASK_APPEARED;
        this.mLeash = surfaceControl;
        this.mCompactState.put(windowContainerToken.asBinder(), new PipWindowConfigurationCompact(this.mTaskInfo.configuration.windowConfiguration));
        ActivityManager.RunningTaskInfo runningTaskInfo2 = this.mTaskInfo;
        this.mPictureInPictureParams = runningTaskInfo2.pictureInPictureParams;
        this.mRequestedOrientation = runningTaskInfo.requestedOrientation;
        this.mPipUiEventLoggerLogger.setTaskInfo(runningTaskInfo2);
        this.mPipUiEventLoggerLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_ENTER);
        if (this.mShouldDeferEnteringPip) {
            SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
            transaction.setAlpha(this.mLeash, 0.0f);
            transaction.show(this.mLeash);
            transaction.apply();
            return;
        }
        Rect destinationBounds = this.mPipBoundsHandler.getDestinationBounds(this.mTaskInfo.topActivity, getAspectRatioOrDefault(this.mPictureInPictureParams), null, getMinimalSize(this.mTaskInfo.topActivityInfo));
        Objects.requireNonNull(destinationBounds, "Missing destination bounds");
        Rect bounds = this.mTaskInfo.configuration.windowConfiguration.getBounds();
        int i = this.mOneShotAnimationType;
        if (i == 0) {
            scheduleAnimateResizePip(bounds, destinationBounds, getValidSourceHintRect(runningTaskInfo, bounds, destinationBounds), 2, this.mEnterExitAnimationDuration, null);
            this.mState = State.ENTERING_PIP;
        } else if (i == 1) {
            enterPipWithAlphaAnimation(destinationBounds, this.mEnterExitAnimationDuration);
            this.mOneShotAnimationType = 0;
        } else {
            throw new RuntimeException("Unrecognized animation type: " + this.mOneShotAnimationType);
        }
    }

    private Rect getValidSourceHintRect(ActivityManager.RunningTaskInfo runningTaskInfo, Rect rect, Rect rect2) {
        PictureInPictureParams pictureInPictureParams = runningTaskInfo.pictureInPictureParams;
        Rect sourceRectHint = (pictureInPictureParams == null || !pictureInPictureParams.hasSourceBoundsHint()) ? null : runningTaskInfo.pictureInPictureParams.getSourceRectHint();
        if (sourceRectHint == null || !rect.contains(sourceRectHint) || sourceRectHint.width() <= rect2.width() || sourceRectHint.height() <= rect2.height()) {
            return null;
        }
        return sourceRectHint;
    }

    private void enterPipWithAlphaAnimation(Rect rect, long j) {
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        transaction.setAlpha(this.mLeash, 0.0f);
        transaction.apply();
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        windowContainerTransaction.setActivityWindowingMode(this.mToken, 0);
        windowContainerTransaction.setBounds(this.mToken, rect);
        windowContainerTransaction.scheduleFinishEnterPip(this.mToken, rect);
        applySyncTransaction(windowContainerTransaction, new AnonymousClass3(rect, j));
    }

    /* renamed from: com.android.systemui.pip.PipTaskOrganizer$3, reason: invalid class name */
    class AnonymousClass3 extends WindowContainerTransactionCallback {
        final /* synthetic */ Rect val$destinationBounds;
        final /* synthetic */ long val$durationMs;

        AnonymousClass3(Rect rect, long j) {
            this.val$destinationBounds = rect;
            this.val$durationMs = j;
        }

        public void onTransactionReady(int i, SurfaceControl.Transaction transaction) {
            transaction.apply();
            Handler handler = PipTaskOrganizer.this.mUpdateHandler;
            final Rect rect = this.val$destinationBounds;
            final long j = this.val$durationMs;
            handler.post(new Runnable() { // from class: com.android.systemui.pip.PipTaskOrganizer$3$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onTransactionReady$0(rect, j);
                }
            });
            PipTaskOrganizer.this.mState = State.ENTERING_PIP;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onTransactionReady$0(Rect rect, long j) {
            PipTaskOrganizer.this.mPipAnimationController.getAnimator(PipTaskOrganizer.this.mLeash, rect, 0.0f, 1.0f).setTransitionDirection(2).setPipAnimationCallback(PipTaskOrganizer.this.mPipAnimationCallback).setDuration(j).start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendOnPipTransitionStarted(final int i) {
        runOnMainHandler(new Runnable() { // from class: com.android.systemui.pip.PipTaskOrganizer$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$sendOnPipTransitionStarted$2(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$sendOnPipTransitionStarted$2(int i) {
        for (int size = this.mPipTransitionCallbacks.size() - 1; size >= 0; size--) {
            this.mPipTransitionCallbacks.get(size).onPipTransitionStarted(this.mTaskInfo.baseActivity, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendOnPipTransitionFinished(final int i) {
        runOnMainHandler(new Runnable() { // from class: com.android.systemui.pip.PipTaskOrganizer$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$sendOnPipTransitionFinished$3(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$sendOnPipTransitionFinished$3(int i) {
        for (int size = this.mPipTransitionCallbacks.size() - 1; size >= 0; size--) {
            this.mPipTransitionCallbacks.get(size).onPipTransitionFinished(this.mTaskInfo.baseActivity, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendOnPipTransitionCancelled(final int i) {
        runOnMainHandler(new Runnable() { // from class: com.android.systemui.pip.PipTaskOrganizer$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$sendOnPipTransitionCancelled$4(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$sendOnPipTransitionCancelled$4(int i) {
        for (int size = this.mPipTransitionCallbacks.size() - 1; size >= 0; size--) {
            this.mPipTransitionCallbacks.get(size).onPipTransitionCanceled(this.mTaskInfo.baseActivity, i);
        }
    }

    private void runOnMainHandler(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            this.mMainHandler.post(runnable);
        }
    }

    public void onTaskVanished(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (this.mState.isInPip()) {
            WindowContainerToken windowContainerToken = runningTaskInfo.token;
            Objects.requireNonNull(windowContainerToken, "Requires valid WindowContainerToken");
            if (windowContainerToken.asBinder() != this.mToken.asBinder()) {
                Log.wtf(TAG, "Unrecognized token: " + windowContainerToken);
                return;
            }
            this.mShouldDeferEnteringPip = false;
            this.mPictureInPictureParams = null;
            this.mState = State.UNDEFINED;
            this.mPipUiEventLoggerLogger.setTaskInfo(null);
        }
    }

    public void onTaskInfoChanged(ActivityManager.RunningTaskInfo runningTaskInfo) {
        Objects.requireNonNull(this.mToken, "onTaskInfoChanged requires valid existing mToken");
        this.mRequestedOrientation = runningTaskInfo.requestedOrientation;
        PictureInPictureParams pictureInPictureParams = runningTaskInfo.pictureInPictureParams;
        if (pictureInPictureParams == null || !applyPictureInPictureParams(pictureInPictureParams)) {
            Log.d(TAG, "Ignored onTaskInfoChanged with PiP param: " + pictureInPictureParams);
            return;
        }
        Rect destinationBounds = this.mPipBoundsHandler.getDestinationBounds(runningTaskInfo.topActivity, getAspectRatioOrDefault(pictureInPictureParams), this.mLastReportedBounds, getMinimalSize(runningTaskInfo.topActivityInfo), true);
        Objects.requireNonNull(destinationBounds, "Missing destination bounds");
        scheduleAnimateResizePip(destinationBounds, this.mEnterExitAnimationDuration, null);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onFixedRotationStarted(int i, int i2) {
        this.mShouldDeferEnteringPip = true;
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onFixedRotationFinished(int i) {
        if (this.mShouldDeferEnteringPip && this.mState.isInPip()) {
            enterPipWithAlphaAnimation(this.mPipBoundsHandler.getDestinationBounds(this.mTaskInfo.topActivity, getAspectRatioOrDefault(this.mPictureInPictureParams), null, getMinimalSize(this.mTaskInfo.topActivityInfo)), 0L);
        }
        this.mShouldDeferEnteringPip = false;
    }

    public void onMovementBoundsChanged(Rect rect, boolean z, boolean z2, boolean z3, WindowContainerTransaction windowContainerTransaction) {
        PipAnimationController.PipTransitionAnimator currentAnimator = this.mPipAnimationController.getCurrentAnimator();
        if (currentAnimator == null || !currentAnimator.isRunning() || currentAnimator.getTransitionDirection() != 2) {
            if (this.mState.isInPip() && z) {
                int transitionDirection = 0;
                if (currentAnimator != null) {
                    transitionDirection = currentAnimator.getTransitionDirection();
                    currentAnimator.removeAllUpdateListeners();
                    currentAnimator.removeAllListeners();
                    currentAnimator.cancel();
                    sendOnPipTransitionCancelled(transitionDirection);
                    sendOnPipTransitionFinished(transitionDirection);
                }
                this.mLastReportedBounds.set(rect);
                prepareFinishResizeTransaction(rect, transitionDirection, createFinishResizeSurfaceTransaction(rect), windowContainerTransaction);
                return;
            }
            if (currentAnimator != null && currentAnimator.isRunning()) {
                if (currentAnimator.getDestinationBounds().isEmpty()) {
                    return;
                }
                rect.set(currentAnimator.getDestinationBounds());
                return;
            } else {
                if (this.mLastReportedBounds.isEmpty()) {
                    return;
                }
                rect.set(this.mLastReportedBounds);
                return;
            }
        }
        Rect destinationBounds = currentAnimator.getDestinationBounds();
        rect.set(destinationBounds);
        if (z2 || z3 || !this.mPipBoundsHandler.getDisplayBounds().contains(destinationBounds)) {
            Rect destinationBounds2 = this.mPipBoundsHandler.getDestinationBounds(this.mTaskInfo.topActivity, getAspectRatioOrDefault(this.mPictureInPictureParams), null, getMinimalSize(this.mTaskInfo.topActivityInfo));
            if (destinationBounds2.equals(destinationBounds)) {
                return;
            }
            if (currentAnimator.getAnimationType() == 0) {
                currentAnimator.updateEndValue(destinationBounds2);
            }
            currentAnimator.setDestinationBounds(destinationBounds2);
            rect.set(destinationBounds2);
        }
    }

    private boolean applyPictureInPictureParams(PictureInPictureParams pictureInPictureParams) {
        PictureInPictureParams pictureInPictureParams2 = this.mPictureInPictureParams;
        boolean z = pictureInPictureParams2 == null || !Objects.equals(pictureInPictureParams2.getAspectRatioRational(), pictureInPictureParams.getAspectRatioRational());
        if (z) {
            this.mPictureInPictureParams = pictureInPictureParams;
            this.mPipBoundsHandler.onAspectRatioChanged(pictureInPictureParams.getAspectRatio());
        }
        return z;
    }

    public void scheduleAnimateResizePip(Rect rect, int i, Consumer<Rect> consumer) {
        if (this.mShouldDeferEnteringPip) {
            Log.d(TAG, "skip scheduleAnimateResizePip, entering pip deferred");
        } else {
            scheduleAnimateResizePip(this.mLastReportedBounds, rect, null, 0, i, consumer);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleAnimateResizePip(Rect rect, Rect rect2, Rect rect3, int i, int i2, Consumer<Rect> consumer) {
        if (this.mState.isInPip()) {
            SomeArgs someArgsObtain = SomeArgs.obtain();
            someArgsObtain.arg1 = consumer;
            someArgsObtain.arg2 = rect;
            someArgsObtain.arg3 = rect2;
            someArgsObtain.arg4 = rect3;
            someArgsObtain.argi1 = i;
            someArgsObtain.argi2 = i2;
            Handler handler = this.mUpdateHandler;
            handler.sendMessage(handler.obtainMessage(2, someArgsObtain));
        }
    }

    public void scheduleResizePip(Rect rect, Consumer<Rect> consumer) {
        SomeArgs someArgsObtain = SomeArgs.obtain();
        someArgsObtain.arg1 = consumer;
        someArgsObtain.arg2 = rect;
        Handler handler = this.mUpdateHandler;
        handler.sendMessage(handler.obtainMessage(1, someArgsObtain));
    }

    public void scheduleUserResizePip(Rect rect, Rect rect2, Consumer<Rect> consumer) {
        SomeArgs someArgsObtain = SomeArgs.obtain();
        someArgsObtain.arg1 = consumer;
        someArgsObtain.arg2 = rect;
        someArgsObtain.arg3 = rect2;
        Handler handler = this.mUpdateHandler;
        handler.sendMessage(handler.obtainMessage(5, someArgsObtain));
    }

    public void scheduleFinishResizePip(Rect rect) {
        scheduleFinishResizePip(rect, null);
    }

    public void scheduleFinishResizePip(Rect rect, Consumer<Rect> consumer) {
        scheduleFinishResizePip(rect, 0, consumer);
    }

    private void scheduleFinishResizePip(Rect rect, int i, Consumer<Rect> consumer) {
        if (this.mState.shouldBlockResizeRequest()) {
            return;
        }
        SomeArgs someArgsObtain = SomeArgs.obtain();
        someArgsObtain.arg1 = consumer;
        someArgsObtain.arg2 = createFinishResizeSurfaceTransaction(rect);
        someArgsObtain.arg3 = rect;
        someArgsObtain.argi1 = i;
        Handler handler = this.mUpdateHandler;
        handler.sendMessage(handler.obtainMessage(4, someArgsObtain));
    }

    private SurfaceControl.Transaction createFinishResizeSurfaceTransaction(Rect rect) {
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        this.mSurfaceTransactionHelper.crop(transaction, this.mLeash, rect).resetScale(transaction, this.mLeash, rect).round(transaction, this.mLeash, this.mState.isInPip());
        return transaction;
    }

    public void scheduleOffsetPip(Rect rect, int i, int i2, Consumer<Rect> consumer) {
        if (this.mState.shouldBlockResizeRequest()) {
            return;
        }
        if (this.mShouldDeferEnteringPip) {
            Log.d(TAG, "skip scheduleOffsetPip, entering pip deferred");
            return;
        }
        SomeArgs someArgsObtain = SomeArgs.obtain();
        someArgsObtain.arg1 = consumer;
        someArgsObtain.arg2 = rect;
        someArgsObtain.argi1 = i;
        someArgsObtain.argi2 = i2;
        Handler handler = this.mUpdateHandler;
        handler.sendMessage(handler.obtainMessage(3, someArgsObtain));
    }

    private void offsetPip(Rect rect, int i, int i2, int i3) {
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleOffsetPip() instead of this directly");
        }
        if (this.mTaskInfo == null) {
            Log.w(TAG, "mTaskInfo is not set");
            return;
        }
        Rect rect2 = new Rect(rect);
        rect2.offset(i, i2);
        animateResizePip(rect, rect2, null, 1, i3);
    }

    private void resizePip(Rect rect) {
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleResizePip() instead of this directly");
        }
        if (this.mToken == null || this.mLeash == null) {
            Log.w(TAG, "Abort animation, invalid leash");
            return;
        }
        this.mLastReportedBounds.set(rect);
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        this.mSurfaceTransactionHelper.crop(transaction, this.mLeash, rect).round(transaction, this.mLeash, this.mState.isInPip());
        transaction.apply();
    }

    private void userResizePip(Rect rect, Rect rect2) {
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleUserResizePip() instead of this directly");
        }
        if (this.mToken == null || this.mLeash == null) {
            Log.w(TAG, "Abort animation, invalid leash");
            return;
        }
        if (rect.isEmpty() || rect2.isEmpty()) {
            Log.w(TAG, "Attempted to user resize PIP to or from empty bounds, aborting.");
            return;
        }
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        this.mSurfaceTransactionHelper.scale(transaction, this.mLeash, rect, rect2);
        transaction.apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishResize(SurfaceControl.Transaction transaction, Rect rect, int i, int i2) {
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleResizePip() instead of this directly");
        }
        this.mLastReportedBounds.set(rect);
        if (i == 5) {
            removePipImmediately();
        } else {
            if (PipAnimationController.isInPipDirection(i) && i2 == 1) {
                return;
            }
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            prepareFinishResizeTransaction(rect, i, transaction, windowContainerTransaction);
            applyFinishBoundsResize(windowContainerTransaction, i);
        }
    }

    private void prepareFinishResizeTransaction(Rect rect, int i, SurfaceControl.Transaction transaction, WindowContainerTransaction windowContainerTransaction) {
        if (PipAnimationController.isInPipDirection(i)) {
            windowContainerTransaction.setActivityWindowingMode(this.mToken, 0);
            windowContainerTransaction.scheduleFinishEnterPip(this.mToken, rect);
        } else if (PipAnimationController.isOutPipDirection(i)) {
            if (i == 3) {
                rect = null;
            }
            applyWindowingModeChangeOnExit(windowContainerTransaction, i);
        }
        windowContainerTransaction.setBounds(this.mToken, rect);
        windowContainerTransaction.setBoundsChangeTransaction(this.mToken, transaction);
    }

    public void applyFinishBoundsResize(WindowContainerTransaction windowContainerTransaction, int i) {
        WindowOrganizer.applyTransaction(windowContainerTransaction);
    }

    private void animateResizePip(Rect rect, Rect rect2, Rect rect3, int i, int i2) {
        SurfaceControl surfaceControl;
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleAnimateResizePip() instead of this directly");
        }
        if (this.mToken == null || (surfaceControl = this.mLeash) == null) {
            Log.w(TAG, "Abort animation, invalid leash");
        } else {
            this.mPipAnimationController.getAnimator(surfaceControl, rect, rect2, rect3).setTransitionDirection(i).setPipAnimationCallback(this.mPipAnimationCallback).setDuration(i2).start();
        }
    }

    private Size getMinimalSize(ActivityInfo activityInfo) {
        ActivityInfo.WindowLayout windowLayout;
        int i;
        int i2;
        if (activityInfo == null || (windowLayout = activityInfo.windowLayout) == null || (i = windowLayout.minWidth) <= 0 || (i2 = windowLayout.minHeight) <= 0) {
            return null;
        }
        int i3 = this.mOverridableMinSize;
        if (i < i3 || i2 < i3) {
            EventLog.writeEvent(1397638484, "174302616", -1, "");
        }
        return new Size(Math.max(windowLayout.minWidth, this.mOverridableMinSize), Math.max(windowLayout.minHeight, this.mOverridableMinSize));
    }

    private float getAspectRatioOrDefault(PictureInPictureParams pictureInPictureParams) {
        if (pictureInPictureParams == null || !pictureInPictureParams.hasSetAspectRatio()) {
            return this.mPipBoundsHandler.getDefaultAspectRatio();
        }
        return pictureInPictureParams.getAspectRatio();
    }

    private boolean syncWithSplitScreenBounds(Rect rect) {
        Divider divider = this.mSplitDivider;
        if (divider == null || !divider.isDividerVisible()) {
            return false;
        }
        rect.set(this.mSplitDivider.getView().getNonMinimizedSplitScreenSecondaryBounds());
        return true;
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + TAG);
        printWriter.println(str2 + "mTaskInfo=" + this.mTaskInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append("mToken=");
        sb.append(this.mToken);
        sb.append(" binder=");
        WindowContainerToken windowContainerToken = this.mToken;
        sb.append(windowContainerToken != null ? windowContainerToken.asBinder() : null);
        printWriter.println(sb.toString());
        printWriter.println(str2 + "mLeash=" + this.mLeash);
        printWriter.println(str2 + "mState=" + this.mState);
        printWriter.println(str2 + "mOneShotAnimationType=" + this.mOneShotAnimationType);
        printWriter.println(str2 + "mPictureInPictureParams=" + this.mPictureInPictureParams);
        printWriter.println(str2 + "mLastReportedBounds=" + this.mLastReportedBounds);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(str2);
        sb2.append("mInitialState:");
        printWriter.println(sb2.toString());
        for (Map.Entry<IBinder, PipWindowConfigurationCompact> entry : this.mCompactState.entrySet()) {
            printWriter.println(str2 + "  binder=" + entry.getKey() + " config=" + entry.getValue());
        }
    }
}
