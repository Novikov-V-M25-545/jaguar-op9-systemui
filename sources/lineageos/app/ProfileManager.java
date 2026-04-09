package lineageos.app;

import android.content.Context;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.UUID;
import lineageos.app.IProfileManager;

/* loaded from: classes2.dex */
public class ProfileManager {
    public static final UUID NO_PROFILE = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static ProfileManager sProfileManagerInstance;
    private static IProfileManager sService;
    private Context mContext;

    private ProfileManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext != null) {
            this.mContext = applicationContext;
        } else {
            this.mContext = context;
        }
        try {
            sService = getService();
        } catch (RemoteException unused) {
            sService = null;
        }
        if (context.getPackageManager().hasSystemFeature("org.lineageos.profiles") && sService == null) {
            Log.wtf("ProfileManager", "Unable to get ProfileManagerService. The service either crashed, was not started, or the interface has been called to early in SystemServer init");
        }
    }

    public static ProfileManager getInstance(Context context) {
        if (sProfileManagerInstance == null) {
            sProfileManagerInstance = new ProfileManager(context);
        }
        return sProfileManagerInstance;
    }

    public static IProfileManager getService() throws RemoteException {
        IProfileManager iProfileManager = sService;
        if (iProfileManager != null) {
            return iProfileManager;
        }
        IProfileManager iProfileManagerAsInterface = IProfileManager.Stub.asInterface(ServiceManager.getService("profile"));
        sService = iProfileManagerAsInterface;
        if (iProfileManagerAsInterface != null) {
            return iProfileManagerAsInterface;
        }
        throw new RemoteException("Couldn't get profile on binder");
    }

    public void setActiveProfile(UUID uuid) {
        try {
            getService().setActiveProfile(new ParcelUuid(uuid));
        } catch (RemoteException e) {
            Log.e("ProfileManager", e.getLocalizedMessage(), e);
        }
    }

    public Profile getActiveProfile() {
        try {
            return getService().getActiveProfile();
        } catch (RemoteException e) {
            Log.e("ProfileManager", e.getLocalizedMessage(), e);
            return null;
        }
    }

    public Profile[] getProfiles() {
        try {
            return getService().getProfiles();
        } catch (RemoteException e) {
            Log.e("ProfileManager", e.getLocalizedMessage(), e);
            return null;
        }
    }
}
