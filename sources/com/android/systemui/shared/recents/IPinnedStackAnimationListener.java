package com.android.systemui.shared.recents;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* loaded from: classes.dex */
public interface IPinnedStackAnimationListener extends IInterface {
    void onPinnedStackAnimationStarted() throws RemoteException;

    public static abstract class Stub extends Binder implements IPinnedStackAnimationListener {
        public static IPinnedStackAnimationListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.shared.recents.IPinnedStackAnimationListener");
            if (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IPinnedStackAnimationListener)) {
                return (IPinnedStackAnimationListener) iInterfaceQueryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        private static class Proxy implements IPinnedStackAnimationListener {
            public static IPinnedStackAnimationListener sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.android.systemui.shared.recents.IPinnedStackAnimationListener
            public void onPinnedStackAnimationStarted() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.android.systemui.shared.recents.IPinnedStackAnimationListener");
                    if (this.mRemote.transact(1, parcelObtain, null, 1) || Stub.getDefaultImpl() == null) {
                        return;
                    }
                    Stub.getDefaultImpl().onPinnedStackAnimationStarted();
                } finally {
                    parcelObtain.recycle();
                }
            }
        }

        public static IPinnedStackAnimationListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
