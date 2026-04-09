package com.android.systemui.pip.phone;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.DeviceConfig;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.internal.policy.TaskResizingAlgorithm;
import com.android.systemui.R;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.util.DeviceConfigProxy;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/* loaded from: classes.dex */
public class PipResizeGestureHandler {
    private boolean mAllowGesture;
    private final Context mContext;
    private int mCtrlType;
    private int mDelta;
    private final Rect mDisplayBounds;
    private final int mDisplayId;
    private final Rect mDragCornerSize;
    private boolean mEnableUserResize;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsAttached;
    private boolean mIsEnabled;
    private final Rect mLastDownBounds;
    private final Rect mLastResizeBounds;
    private final Executor mMainExecutor;
    private final Point mMaxSize;
    private final PipMenuActivityController mMenuController;
    private final Point mMinSize;
    private final PipMotionHelper mMotionHelper;
    private final Function<Rect, Rect> mMovementBoundsSupplier;
    private final PipBoundsHandler mPipBoundsHandler;
    private PipTaskOrganizer mPipTaskOrganizer;
    private PipUiEventLogger mPipUiEventLogger;
    private final SysUiState mSysUiState;
    private boolean mThresholdCrossed;
    private final Rect mTmpBottomLeftCorner;
    private final Rect mTmpBottomRightCorner;
    private final Rect mTmpTopLeftCorner;
    private final Rect mTmpTopRightCorner;
    private float mTouchSlop;
    private final Runnable mUpdateMovementBoundsRunnable;
    private final Rect mUserResizeBounds;
    private final Region mTmpRegion = new Region();
    private final PointF mDownPoint = new PointF();

    public PipResizeGestureHandler(Context context, PipBoundsHandler pipBoundsHandler, PipMotionHelper pipMotionHelper, DeviceConfigProxy deviceConfigProxy, PipTaskOrganizer pipTaskOrganizer, PipMenuActivityController pipMenuActivityController, Function<Rect, Rect> function, Runnable runnable, SysUiState sysUiState, PipUiEventLogger pipUiEventLogger) {
        Point point = new Point();
        this.mMaxSize = point;
        this.mMinSize = new Point();
        this.mLastResizeBounds = new Rect();
        this.mUserResizeBounds = new Rect();
        this.mLastDownBounds = new Rect();
        this.mDragCornerSize = new Rect();
        this.mTmpTopLeftCorner = new Rect();
        this.mTmpTopRightCorner = new Rect();
        this.mTmpBottomLeftCorner = new Rect();
        this.mTmpBottomRightCorner = new Rect();
        this.mDisplayBounds = new Rect();
        this.mContext = context;
        this.mDisplayId = context.getDisplayId();
        Executor mainExecutor = context.getMainExecutor();
        this.mMainExecutor = mainExecutor;
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mMenuController = pipMenuActivityController;
        this.mMotionHelper = pipMotionHelper;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        this.mMovementBoundsSupplier = function;
        this.mUpdateMovementBoundsRunnable = runnable;
        this.mSysUiState = sysUiState;
        this.mPipUiEventLogger = pipUiEventLogger;
        context.getDisplay().getRealSize(point);
        reloadResources();
        this.mEnableUserResize = DeviceConfig.getBoolean("systemui", "pip_user_resize", true);
        deviceConfigProxy.addOnPropertiesChangedListener("systemui", mainExecutor, new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.pip.phone.PipResizeGestureHandler.1
            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                if (properties.getKeyset().contains("pip_user_resize")) {
                    PipResizeGestureHandler.this.mEnableUserResize = properties.getBoolean("pip_user_resize", true);
                }
            }
        });
    }

    public void onConfigurationChanged() {
        reloadResources();
    }

    private void reloadResources() {
        this.mDelta = this.mContext.getResources().getDimensionPixelSize(R.dimen.pip_resize_edge_size);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
    }

    private void resetDragCorners() {
        Rect rect = this.mDragCornerSize;
        int i = this.mDelta;
        rect.set(0, 0, i, i);
        this.mTmpTopLeftCorner.set(this.mDragCornerSize);
        this.mTmpTopRightCorner.set(this.mDragCornerSize);
        this.mTmpBottomLeftCorner.set(this.mDragCornerSize);
        this.mTmpBottomRightCorner.set(this.mDragCornerSize);
    }

    private void disposeInputChannel() {
        InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
        if (inputEventReceiver != null) {
            inputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.dispose();
            this.mInputMonitor = null;
        }
    }

    void onActivityPinned() {
        this.mIsAttached = true;
        updateIsEnabled();
    }

    void onActivityUnpinned() {
        this.mIsAttached = false;
        this.mUserResizeBounds.setEmpty();
        updateIsEnabled();
    }

    private void updateIsEnabled() {
        boolean z = this.mIsAttached && this.mEnableUserResize;
        if (z == this.mIsEnabled) {
            return;
        }
        this.mIsEnabled = z;
        disposeInputChannel();
        if (this.mIsEnabled) {
            this.mInputMonitor = InputManager.getInstance().monitorGestureInput("pip-resize", this.mDisplayId);
            this.mInputEventReceiver = new SysUiInputEventReceiver(this.mInputMonitor.getInputChannel(), Looper.getMainLooper());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onInputEvent(InputEvent inputEvent) throws RemoteException {
        if (inputEvent instanceof MotionEvent) {
            onMotionEvent((MotionEvent) inputEvent);
        }
    }

    public boolean isWithinTouchRegion(int i, int i2) {
        Rect bounds = this.mMotionHelper.getBounds();
        if (bounds == null) {
            return false;
        }
        resetDragCorners();
        Rect rect = this.mTmpTopLeftCorner;
        int i3 = bounds.left;
        int i4 = this.mDelta;
        rect.offset(i3 - (i4 / 2), bounds.top - (i4 / 2));
        Rect rect2 = this.mTmpTopRightCorner;
        int i5 = bounds.right;
        int i6 = this.mDelta;
        rect2.offset(i5 - (i6 / 2), bounds.top - (i6 / 2));
        Rect rect3 = this.mTmpBottomLeftCorner;
        int i7 = bounds.left;
        int i8 = this.mDelta;
        rect3.offset(i7 - (i8 / 2), bounds.bottom - (i8 / 2));
        Rect rect4 = this.mTmpBottomRightCorner;
        int i9 = bounds.right;
        int i10 = this.mDelta;
        rect4.offset(i9 - (i10 / 2), bounds.bottom - (i10 / 2));
        this.mTmpRegion.setEmpty();
        this.mTmpRegion.op(this.mTmpTopLeftCorner, Region.Op.UNION);
        this.mTmpRegion.op(this.mTmpTopRightCorner, Region.Op.UNION);
        this.mTmpRegion.op(this.mTmpBottomLeftCorner, Region.Op.UNION);
        this.mTmpRegion.op(this.mTmpBottomRightCorner, Region.Op.UNION);
        return this.mTmpRegion.contains(i, i2);
    }

    public boolean willStartResizeGesture(MotionEvent motionEvent) {
        return this.mEnableUserResize && isInValidSysUiState() && isWithinTouchRegion((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    private void setCtrlType(int i, int i2) {
        Rect bounds = this.mMotionHelper.getBounds();
        Rect rectApply = this.mMovementBoundsSupplier.apply(bounds);
        this.mDisplayBounds.set(rectApply.left, rectApply.top, rectApply.right + bounds.width(), rectApply.bottom + bounds.height());
        if (this.mTmpTopLeftCorner.contains(i, i2)) {
            int i3 = bounds.top;
            Rect rect = this.mDisplayBounds;
            if (i3 != rect.top && bounds.left != rect.left) {
                int i4 = this.mCtrlType | 1;
                this.mCtrlType = i4;
                this.mCtrlType = i4 | 4;
            }
        }
        if (this.mTmpTopRightCorner.contains(i, i2)) {
            int i5 = bounds.top;
            Rect rect2 = this.mDisplayBounds;
            if (i5 != rect2.top && bounds.right != rect2.right) {
                int i6 = this.mCtrlType | 2;
                this.mCtrlType = i6;
                this.mCtrlType = i6 | 4;
            }
        }
        if (this.mTmpBottomRightCorner.contains(i, i2)) {
            int i7 = bounds.bottom;
            Rect rect3 = this.mDisplayBounds;
            if (i7 != rect3.bottom && bounds.right != rect3.right) {
                int i8 = this.mCtrlType | 2;
                this.mCtrlType = i8;
                this.mCtrlType = i8 | 8;
            }
        }
        if (this.mTmpBottomLeftCorner.contains(i, i2)) {
            int i9 = bounds.bottom;
            Rect rect4 = this.mDisplayBounds;
            if (i9 == rect4.bottom || bounds.left == rect4.left) {
                return;
            }
            int i10 = this.mCtrlType | 1;
            this.mCtrlType = i10;
            this.mCtrlType = i10 | 8;
        }
    }

    private boolean isInValidSysUiState() {
        return (this.mSysUiState.getFlags() & 51788) == 0;
    }

    private void onMotionEvent(MotionEvent motionEvent) throws RemoteException {
        int actionMasked = motionEvent.getActionMasked();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (actionMasked == 0) {
            this.mLastResizeBounds.setEmpty();
            if (isInValidSysUiState() && isWithinTouchRegion((int) x, (int) y)) {
                z = true;
            }
            this.mAllowGesture = z;
            if (z) {
                setCtrlType((int) x, (int) y);
                this.mDownPoint.set(x, y);
                this.mLastDownBounds.set(this.mMotionHelper.getBounds());
                return;
            }
            return;
        }
        if (this.mAllowGesture) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!this.mThresholdCrossed) {
                        PointF pointF = this.mDownPoint;
                        if (Math.hypot(x - pointF.x, y - pointF.y) > this.mTouchSlop) {
                            this.mThresholdCrossed = true;
                            this.mDownPoint.set(x, y);
                            this.mInputMonitor.pilferPointers();
                        }
                    }
                    if (this.mThresholdCrossed) {
                        if (this.mMenuController.isMenuActivityVisible()) {
                            this.mMenuController.hideMenuWithoutResize();
                            this.mMenuController.hideMenu();
                        }
                        Rect bounds = this.mMotionHelper.getBounds();
                        Rect rect = this.mLastResizeBounds;
                        PointF pointF2 = this.mDownPoint;
                        float f = pointF2.x;
                        float f2 = pointF2.y;
                        int i = this.mCtrlType;
                        Point point = this.mMinSize;
                        rect.set(TaskResizingAlgorithm.resizeDrag(x, y, f, f2, bounds, i, point.x, point.y, this.mMaxSize, true, this.mLastDownBounds.width() > this.mLastDownBounds.height()));
                        this.mPipBoundsHandler.transformBoundsToAspectRatio(this.mLastResizeBounds);
                        this.mPipTaskOrganizer.scheduleUserResizePip(this.mLastDownBounds, this.mLastResizeBounds, null);
                        return;
                    }
                    return;
                }
                if (actionMasked != 3) {
                    if (actionMasked != 5) {
                        return;
                    }
                    this.mAllowGesture = false;
                    return;
                }
            }
            if (!this.mLastResizeBounds.isEmpty()) {
                this.mUserResizeBounds.set(this.mLastResizeBounds);
                this.mPipTaskOrganizer.scheduleFinishResizePip(this.mLastResizeBounds, new Consumer() { // from class: com.android.systemui.pip.phone.PipResizeGestureHandler$$ExternalSyntheticLambda1
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.lambda$onMotionEvent$1((Rect) obj);
                    }
                });
                this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_RESIZE);
                return;
            }
            resetState();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onMotionEvent$1(Rect rect) {
        new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.android.systemui.pip.phone.PipResizeGestureHandler$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$onMotionEvent$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onMotionEvent$0() {
        this.mMotionHelper.synchronizePinnedStackBounds();
        this.mUpdateMovementBoundsRunnable.run();
        resetState();
    }

    private void resetState() {
        this.mCtrlType = 0;
        this.mAllowGesture = false;
        this.mThresholdCrossed = false;
    }

    void setUserResizeBounds(Rect rect) {
        this.mUserResizeBounds.set(rect);
    }

    void invalidateUserResizeBounds() {
        this.mUserResizeBounds.setEmpty();
    }

    Rect getUserResizeBounds() {
        return this.mUserResizeBounds;
    }

    void updateMaxSize(int i, int i2) {
        this.mMaxSize.set(i, i2);
    }

    void updateMinSize(int i, int i2) {
        this.mMinSize.set(i, i2);
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipResizeGestureHandler");
        printWriter.println(str2 + "mAllowGesture=" + this.mAllowGesture);
        printWriter.println(str2 + "mIsAttached=" + this.mIsAttached);
        printWriter.println(str2 + "mIsEnabled=" + this.mIsEnabled);
        printWriter.println(str2 + "mEnableUserResize=" + this.mEnableUserResize);
        printWriter.println(str2 + "mThresholdCrossed=" + this.mThresholdCrossed);
    }

    class SysUiInputEventReceiver extends BatchedInputEventReceiver {
        SysUiInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper, Choreographer.getSfInstance());
        }

        public void onInputEvent(InputEvent inputEvent) throws RemoteException {
            PipResizeGestureHandler.this.onInputEvent(inputEvent);
            finishInputEvent(inputEvent, true);
        }
    }
}
