package com.android.systemui.pip.phone;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.MagnificationSpec;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.systemui.R;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class PipAccessibilityInteractionConnection extends IAccessibilityInteractionConnection.Stub {
    private List<AccessibilityNodeInfo> mAccessibilityNodeInfoList;
    private AccessibilityCallbacks mCallbacks;
    private Context mContext;
    private Handler mHandler;
    private PipMotionHelper mMotionHelper;
    private PipSnapAlgorithm mSnapAlgorithm;
    private PipTaskOrganizer mTaskOrganizer;
    private Runnable mUpdateMovementBoundCallback;
    private final Rect mNormalBounds = new Rect();
    private final Rect mExpandedBounds = new Rect();
    private final Rect mNormalMovementBounds = new Rect();
    private final Rect mExpandedMovementBounds = new Rect();
    private Rect mTmpBounds = new Rect();

    public interface AccessibilityCallbacks {
        void onAccessibilityShowMenu();
    }

    public void clearAccessibilityFocus() {
    }

    public void notifyOutsideTouch() {
    }

    public PipAccessibilityInteractionConnection(Context context, PipMotionHelper pipMotionHelper, PipTaskOrganizer pipTaskOrganizer, PipSnapAlgorithm pipSnapAlgorithm, AccessibilityCallbacks accessibilityCallbacks, Runnable runnable, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mMotionHelper = pipMotionHelper;
        this.mTaskOrganizer = pipTaskOrganizer;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mUpdateMovementBoundCallback = runnable;
        this.mCallbacks = accessibilityCallbacks;
    }

    public void findAccessibilityNodeInfoByAccessibilityId(long j, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec, Bundle bundle) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfosResult(j == AccessibilityNodeInfo.ROOT_NODE_ID ? getNodeList() : null, i);
        } catch (RemoteException unused) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x0087  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void performAccessibilityAction(long r1, int r3, android.os.Bundle r4, int r5, android.view.accessibility.IAccessibilityInteractionConnectionCallback r6, int r7, int r8, long r9) {
        /*
            r0 = this;
            long r7 = android.view.accessibility.AccessibilityNodeInfo.ROOT_NODE_ID
            int r1 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1))
            r2 = 1
            if (r1 != 0) goto L87
            int r1 = com.android.systemui.R.id.action_pip_resize
            if (r3 != r1) goto L37
            com.android.systemui.pip.phone.PipMotionHelper r1 = r0.mMotionHelper
            android.graphics.Rect r1 = r1.getBounds()
            int r1 = r1.width()
            android.graphics.Rect r3 = r0.mNormalBounds
            int r3 = r3.width()
            if (r1 != r3) goto L33
            com.android.systemui.pip.phone.PipMotionHelper r1 = r0.mMotionHelper
            android.graphics.Rect r1 = r1.getBounds()
            int r1 = r1.height()
            android.graphics.Rect r3 = r0.mNormalBounds
            int r3 = r3.height()
            if (r1 != r3) goto L33
            r0.setToExpandedBounds()
            goto L88
        L33:
            r0.setToNormalBounds()
            goto L88
        L37:
            r1 = 16
            if (r3 == r1) goto L7c
            r1 = 262144(0x40000, float:3.67342E-40)
            if (r3 == r1) goto L76
            r1 = 1048576(0x100000, float:1.469368E-39)
            if (r3 == r1) goto L70
            r1 = 16908354(0x1020042, float:2.3877414E-38)
            if (r3 == r1) goto L49
            goto L87
        L49:
            java.lang.String r1 = "ACTION_ARGUMENT_MOVE_WINDOW_X"
            int r1 = r4.getInt(r1)
            java.lang.String r3 = "ACTION_ARGUMENT_MOVE_WINDOW_Y"
            int r3 = r4.getInt(r3)
            android.graphics.Rect r4 = new android.graphics.Rect
            r4.<init>()
            com.android.systemui.pip.phone.PipMotionHelper r7 = r0.mMotionHelper
            android.graphics.Rect r7 = r7.getBounds()
            r4.set(r7)
            android.graphics.Rect r4 = r0.mTmpBounds
            r4.offsetTo(r1, r3)
            com.android.systemui.pip.phone.PipMotionHelper r1 = r0.mMotionHelper
            android.graphics.Rect r0 = r0.mTmpBounds
            r1.movePip(r0)
            goto L88
        L70:
            com.android.systemui.pip.phone.PipMotionHelper r0 = r0.mMotionHelper
            r0.dismissPip()
            goto L88
        L76:
            com.android.systemui.pip.phone.PipMotionHelper r0 = r0.mMotionHelper
            r0.expandPipToFullscreen()
            goto L88
        L7c:
            android.os.Handler r1 = r0.mHandler
            com.android.systemui.pip.phone.PipAccessibilityInteractionConnection$$ExternalSyntheticLambda0 r3 = new com.android.systemui.pip.phone.PipAccessibilityInteractionConnection$$ExternalSyntheticLambda0
            r3.<init>()
            r1.post(r3)
            goto L88
        L87:
            r2 = 0
        L88:
            r6.setPerformAccessibilityActionResult(r2, r5)     // Catch: android.os.RemoteException -> L8b
        L8b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.pip.phone.PipAccessibilityInteractionConnection.performAccessibilityAction(long, int, android.os.Bundle, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performAccessibilityAction$0() {
        this.mCallbacks.onAccessibilityShowMenu();
    }

    private void setToExpandedBounds() {
        this.mSnapAlgorithm.applySnapFraction(this.mExpandedBounds, this.mExpandedMovementBounds, this.mSnapAlgorithm.getSnapFraction(new Rect(this.mTaskOrganizer.getLastReportedBounds()), this.mNormalMovementBounds));
        this.mTaskOrganizer.scheduleFinishResizePip(this.mExpandedBounds, new Consumer() { // from class: com.android.systemui.pip.phone.PipAccessibilityInteractionConnection$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$setToExpandedBounds$1((Rect) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setToExpandedBounds$1(Rect rect) {
        this.mMotionHelper.synchronizePinnedStackBounds();
        this.mUpdateMovementBoundCallback.run();
    }

    private void setToNormalBounds() {
        this.mSnapAlgorithm.applySnapFraction(this.mNormalBounds, this.mNormalMovementBounds, this.mSnapAlgorithm.getSnapFraction(new Rect(this.mTaskOrganizer.getLastReportedBounds()), this.mExpandedMovementBounds));
        this.mTaskOrganizer.scheduleFinishResizePip(this.mNormalBounds, new Consumer() { // from class: com.android.systemui.pip.phone.PipAccessibilityInteractionConnection$$ExternalSyntheticLambda2
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$setToNormalBounds$2((Rect) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setToNormalBounds$2(Rect rect) {
        this.mMotionHelper.synchronizePinnedStackBounds();
        this.mUpdateMovementBoundCallback.run();
    }

    public void findAccessibilityNodeInfosByViewId(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i);
        } catch (RemoteException unused) {
        }
    }

    public void findAccessibilityNodeInfosByText(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i);
        } catch (RemoteException unused) {
        }
    }

    public void findFocus(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i2);
        } catch (RemoteException unused) {
        }
    }

    public void focusSearch(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i2);
        } catch (RemoteException unused) {
        }
    }

    void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, Rect rect4) {
        this.mNormalBounds.set(rect);
        this.mExpandedBounds.set(rect2);
        this.mNormalMovementBounds.set(rect3);
        this.mExpandedMovementBounds.set(rect4);
    }

    public static AccessibilityNodeInfo obtainRootAccessibilityNodeInfo(Context context) {
        AccessibilityNodeInfo accessibilityNodeInfoObtain = AccessibilityNodeInfo.obtain();
        accessibilityNodeInfoObtain.setSourceNodeId(AccessibilityNodeInfo.ROOT_NODE_ID, -3);
        accessibilityNodeInfoObtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
        accessibilityNodeInfoObtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS);
        accessibilityNodeInfoObtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_MOVE_WINDOW);
        accessibilityNodeInfoObtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
        accessibilityNodeInfoObtain.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_pip_resize, context.getString(R.string.accessibility_action_pip_resize)));
        accessibilityNodeInfoObtain.setImportantForAccessibility(true);
        accessibilityNodeInfoObtain.setClickable(true);
        accessibilityNodeInfoObtain.setVisibleToUser(true);
        return accessibilityNodeInfoObtain;
    }

    private List<AccessibilityNodeInfo> getNodeList() {
        if (this.mAccessibilityNodeInfoList == null) {
            this.mAccessibilityNodeInfoList = new ArrayList(1);
        }
        AccessibilityNodeInfo accessibilityNodeInfoObtainRootAccessibilityNodeInfo = obtainRootAccessibilityNodeInfo(this.mContext);
        this.mAccessibilityNodeInfoList.clear();
        this.mAccessibilityNodeInfoList.add(accessibilityNodeInfoObtainRootAccessibilityNodeInfo);
        return this.mAccessibilityNodeInfoList;
    }
}
