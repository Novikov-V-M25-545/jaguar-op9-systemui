package com.android.systemui.shared.recents;

import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MotionEvent;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.shared.recents.model.Task$TaskKey;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public interface ISystemUiProxy extends IInterface {
    Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException;

    void handleImageAsScreenshot(Bitmap bitmap, Rect rect, Insets insets, int i) throws RemoteException;

    void handleImageBundleAsScreenshot(Bundle bundle, Rect rect, Insets insets, Task$TaskKey task$TaskKey) throws RemoteException;

    Bundle monitorGestureInput(String str, int i) throws RemoteException;

    void notifyAccessibilityButtonClicked(int i) throws RemoteException;

    void notifyAccessibilityButtonLongClicked() throws RemoteException;

    void notifySwipeToHomeFinished() throws RemoteException;

    void onAssistantGestureCompletion(float f) throws RemoteException;

    void onAssistantProgress(float f) throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onQuickSwitchToNewTask(int i) throws RemoteException;

    void onSplitScreenInvoked() throws RemoteException;

    void onStatusBarMotionEvent(MotionEvent motionEvent) throws RemoteException;

    void setBackButtonAlpha(float f, boolean z) throws RemoteException;

    void setNavBarButtonAlpha(float f, boolean z) throws RemoteException;

    void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) throws RemoteException;

    void setShelfHeight(boolean z, int i) throws RemoteException;

    void setSplitScreenMinimized(boolean z) throws RemoteException;

    void startAssistant(Bundle bundle) throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void stopScreenPinning() throws RemoteException;

    public static abstract class Stub extends Binder implements ISystemUiProxy {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.shared.recents.ISystemUiProxy");
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 2) {
                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                startScreenPinning(parcel.readInt());
                parcel2.writeNoException();
                return true;
            }
            if (i == 1598968902) {
                parcel2.writeString("com.android.systemui.shared.recents.ISystemUiProxy");
                return true;
            }
            switch (i) {
                case 6:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    onSplitScreenInvoked();
                    parcel2.writeNoException();
                    return true;
                case 7:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    onOverviewShown(parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case QS.VERSION /* 8 */:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    Rect nonMinimizedSplitScreenSecondaryBounds = getNonMinimizedSplitScreenSecondaryBounds();
                    parcel2.writeNoException();
                    if (nonMinimizedSplitScreenSecondaryBounds != null) {
                        parcel2.writeInt(1);
                        nonMinimizedSplitScreenSecondaryBounds.writeToParcel(parcel2, 1);
                    } else {
                        parcel2.writeInt(0);
                    }
                    return true;
                case 9:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    setBackButtonAlpha(parcel.readFloat(), parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case 10:
                    parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                    onStatusBarMotionEvent(parcel.readInt() != 0 ? (MotionEvent) MotionEvent.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    return true;
                default:
                    switch (i) {
                        case 13:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            onAssistantProgress(parcel.readFloat());
                            parcel2.writeNoException();
                            return true;
                        case 14:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            startAssistant(parcel.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(parcel) : null);
                            parcel2.writeNoException();
                            return true;
                        case 15:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            Bundle bundleMonitorGestureInput = monitorGestureInput(parcel.readString(), parcel.readInt());
                            parcel2.writeNoException();
                            if (bundleMonitorGestureInput != null) {
                                parcel2.writeInt(1);
                                bundleMonitorGestureInput.writeToParcel(parcel2, 1);
                            } else {
                                parcel2.writeInt(0);
                            }
                            return true;
                        case LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY /* 16 */:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            notifyAccessibilityButtonClicked(parcel.readInt());
                            parcel2.writeNoException();
                            return true;
                        case 17:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            notifyAccessibilityButtonLongClicked();
                            parcel2.writeNoException();
                            return true;
                        case 18:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            stopScreenPinning();
                            parcel2.writeNoException();
                            return true;
                        case 19:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            onAssistantGestureCompletion(parcel.readFloat());
                            parcel2.writeNoException();
                            return true;
                        case 20:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            setNavBarButtonAlpha(parcel.readFloat(), parcel.readInt() != 0);
                            parcel2.writeNoException();
                            return true;
                        case 21:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            setShelfHeight(parcel.readInt() != 0, parcel.readInt());
                            parcel2.writeNoException();
                            return true;
                        case 22:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            handleImageAsScreenshot(parcel.readInt() != 0 ? (Bitmap) Bitmap.CREATOR.createFromParcel(parcel) : null, parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null, parcel.readInt() != 0 ? (Insets) Insets.CREATOR.createFromParcel(parcel) : null, parcel.readInt());
                            parcel2.writeNoException();
                            return true;
                        case 23:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            setSplitScreenMinimized(parcel.readInt() != 0);
                            parcel2.writeNoException();
                            return true;
                        case 24:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            notifySwipeToHomeFinished();
                            parcel2.writeNoException();
                            return true;
                        case 25:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            setPinnedStackAnimationListener(IPinnedStackAnimationListener.Stub.asInterface(parcel.readStrongBinder()));
                            parcel2.writeNoException();
                            return true;
                        case 26:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            onQuickSwitchToNewTask(parcel.readInt());
                            parcel2.writeNoException();
                            return true;
                        case 27:
                            parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                            handleImageBundleAsScreenshot(parcel.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(parcel) : null, parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null, parcel.readInt() != 0 ? (Insets) Insets.CREATOR.createFromParcel(parcel) : null, parcel.readInt() != 0 ? Task$TaskKey.CREATOR.createFromParcel(parcel) : null);
                            parcel2.writeNoException();
                            return true;
                        default:
                            return super.onTransact(i, parcel, parcel2, i2);
                    }
            }
        }
    }
}
