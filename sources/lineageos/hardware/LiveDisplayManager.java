package lineageos.hardware;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import lineageos.hardware.ILiveDisplayService;

/* loaded from: classes2.dex */
public class LiveDisplayManager {
    private static LiveDisplayManager sInstance;
    private static ILiveDisplayService sService;
    private LiveDisplayConfig mConfig;
    private final Context mContext;

    private LiveDisplayManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext != null) {
            this.mContext = applicationContext;
        } else {
            this.mContext = context;
        }
        sService = getService();
        if (context.getPackageManager().hasSystemFeature("org.lineageos.livedisplay") && checkService()) {
            return;
        }
        Log.wtf("LiveDisplay", "Unable to get LiveDisplayService. The service either crashed, was not started, or the interface has been called to early in SystemServer init");
    }

    public static synchronized LiveDisplayManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LiveDisplayManager(context);
        }
        return sInstance;
    }

    public static ILiveDisplayService getService() {
        ILiveDisplayService iLiveDisplayService = sService;
        if (iLiveDisplayService != null) {
            return iLiveDisplayService;
        }
        IBinder service = ServiceManager.getService("lineagelivedisplay");
        if (service == null) {
            return null;
        }
        ILiveDisplayService iLiveDisplayServiceAsInterface = ILiveDisplayService.Stub.asInterface(service);
        sService = iLiveDisplayServiceAsInterface;
        return iLiveDisplayServiceAsInterface;
    }

    private boolean checkService() {
        if (sService != null) {
            return true;
        }
        Log.w("LiveDisplay", "not connected to LineageHardwareManagerService");
        return false;
    }

    public LiveDisplayConfig getConfig() {
        try {
            if (this.mConfig == null) {
                this.mConfig = checkService() ? sService.getConfig() : null;
            }
            return this.mConfig;
        } catch (RemoteException unused) {
            return null;
        }
    }

    public int getMode() {
        try {
            if (checkService()) {
                return sService.getMode();
            }
            return 0;
        } catch (RemoteException unused) {
            return 0;
        }
    }

    public int getDayColorTemperature() {
        try {
            if (checkService()) {
                return sService.getDayColorTemperature();
            }
            return -1;
        } catch (RemoteException unused) {
            return -1;
        }
    }

    public boolean isAntiFlickerEnabled() {
        try {
            if (checkService()) {
                return sService.isAntiFlickerEnabled();
            }
            return false;
        } catch (RemoteException unused) {
            return false;
        }
    }
}
