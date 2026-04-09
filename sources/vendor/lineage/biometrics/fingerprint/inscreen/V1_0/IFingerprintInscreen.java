package vendor.lineage.biometrics.fingerprint.inscreen.V1_0;

import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/* loaded from: classes2.dex */
public interface IFingerprintInscreen extends IBase {
    IHwBinder asBinder();

    int getDimAmount(int i) throws RemoteException;

    int getPositionX() throws RemoteException;

    int getPositionY() throws RemoteException;

    int getSize() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    void onHideFODView() throws RemoteException;

    void onPress() throws RemoteException;

    void onRelease() throws RemoteException;

    void onShowFODView() throws RemoteException;

    void setCallback(IFingerprintInscreenCallback iFingerprintInscreenCallback) throws RemoteException;

    boolean shouldBoostBrightness() throws RemoteException;

    static IFingerprintInscreen asInterface(IHwBinder iHwBinder) {
        if (iHwBinder == null) {
            return null;
        }
        IHwInterface iHwInterfaceQueryLocalInterface = iHwBinder.queryLocalInterface("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
        if (iHwInterfaceQueryLocalInterface != null && (iHwInterfaceQueryLocalInterface instanceof IFingerprintInscreen)) {
            return (IFingerprintInscreen) iHwInterfaceQueryLocalInterface;
        }
        Proxy proxy = new Proxy(iHwBinder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen")) {
                    return proxy;
                }
            }
        } catch (RemoteException unused) {
        }
        return null;
    }

    static IFingerprintInscreen getService(String str) throws RemoteException {
        return asInterface(HwBinder.getService("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen", str));
    }

    static IFingerprintInscreen getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IFingerprintInscreen {
        private IHwBinder mRemote;

        public Proxy(IHwBinder iHwBinder) {
            Objects.requireNonNull(iHwBinder);
            this.mRemote = iHwBinder;
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException unused) {
                return "[class or subclass of vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen]@Proxy";
            }
        }

        public final boolean equals(Object obj) {
            return HidlSupport.interfacesEqual(this, obj);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public int getPositionX() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(1, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readInt32();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public int getPositionY() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(2, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readInt32();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public int getSize() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(3, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readInt32();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public void onPress() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(6, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public void onRelease() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(7, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public void onShowFODView() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(8, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public void onHideFODView() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(9, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public int getDimAmount(int i) throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            hwParcel.writeInt32(i);
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(13, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readInt32();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public boolean shouldBoostBrightness() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(14, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readBool();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public void setCallback(IFingerprintInscreenCallback iFingerprintInscreenCallback) throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.lineage.biometrics.fingerprint.inscreen@1.0::IFingerprintInscreen");
            hwParcel.writeStrongBinder(iFingerprintInscreenCallback == null ? null : iFingerprintInscreenCallback.asBinder());
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(15, hwParcel, hwParcel2, 1);
                hwParcel.releaseTemporaryStorage();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(256067662, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readStringVector();
            } finally {
                hwParcel2.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(256136003, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readString();
            } finally {
                hwParcel2.release();
            }
        }
    }
}
