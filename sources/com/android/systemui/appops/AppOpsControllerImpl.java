package com.android.systemui.appops;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class AppOpsControllerImpl extends BroadcastReceiver implements AppOpsController, AppOpsManager.OnOpActiveChangedInternalListener, AppOpsManager.OnOpNotedListener, Dumpable {
    protected static final int[] OPS = {26, 101, 24, 27, 100, 0, 1};
    private final AppOpsManager mAppOps;
    private final AudioManager mAudioManager;
    private H mBGHandler;
    private final BroadcastDispatcher mDispatcher;
    private final PermissionFlagsCache mFlagsCache;
    private long mLastLocationProviderPackageUpdate;
    private boolean mListening;
    private final LocationManager mLocationManager;
    private List<String> mLocationProviderPackages;
    private boolean mMicMuted;
    private final List<AppOpsController.Callback> mCallbacks = new ArrayList();
    private final ArrayMap<Integer, Set<AppOpsController.Callback>> mCallbacksByCode = new ArrayMap<>();

    @GuardedBy({"mActiveItems"})
    private final List<AppOpItem> mActiveItems = new ArrayList();

    @GuardedBy({"mNotedItems"})
    private final List<AppOpItem> mNotedItems = new ArrayList();

    @GuardedBy({"mActiveItems"})
    private final SparseArray<ArrayList<AudioRecordingConfiguration>> mRecordingsByUid = new SparseArray<>();
    private AudioManager.AudioRecordingCallback mAudioRecordingCallback = new AudioManager.AudioRecordingCallback() { // from class: com.android.systemui.appops.AppOpsControllerImpl.1
        @Override // android.media.AudioManager.AudioRecordingCallback
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> list) {
            synchronized (AppOpsControllerImpl.this.mActiveItems) {
                AppOpsControllerImpl.this.mRecordingsByUid.clear();
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    AudioRecordingConfiguration audioRecordingConfiguration = list.get(i);
                    ArrayList arrayList = (ArrayList) AppOpsControllerImpl.this.mRecordingsByUid.get(audioRecordingConfiguration.getClientUid());
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                        AppOpsControllerImpl.this.mRecordingsByUid.put(audioRecordingConfiguration.getClientUid(), arrayList);
                    }
                    arrayList.add(audioRecordingConfiguration);
                }
            }
            AppOpsControllerImpl.this.updateRecordingPausedStatus();
        }
    };

    public AppOpsControllerImpl(Context context, Looper looper, DumpManager dumpManager, PermissionFlagsCache permissionFlagsCache, AudioManager audioManager, BroadcastDispatcher broadcastDispatcher) {
        this.mDispatcher = broadcastDispatcher;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mFlagsCache = permissionFlagsCache;
        this.mBGHandler = new H(looper);
        int length = OPS.length;
        for (int i = 0; i < length; i++) {
            this.mCallbacksByCode.put(Integer.valueOf(OPS[i]), new ArraySet());
        }
        this.mAudioManager = audioManager;
        this.mMicMuted = audioManager.isMicrophoneMute();
        this.mLocationManager = (LocationManager) context.getSystemService(LocationManager.class);
        dumpManager.registerDumpable("AppOpsControllerImpl", this);
    }

    @VisibleForTesting
    protected void setBGHandler(H h) {
        this.mBGHandler = h;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @VisibleForTesting
    protected void setListening(boolean z) {
        this.mListening = z;
        if (z) {
            AppOpsManager appOpsManager = this.mAppOps;
            int[] iArr = OPS;
            appOpsManager.startWatchingActive(iArr, this);
            this.mAppOps.startWatchingNoted(iArr, this);
            this.mAudioManager.registerAudioRecordingCallback(this.mAudioRecordingCallback, this.mBGHandler);
            this.mBGHandler.post(new Runnable() { // from class: com.android.systemui.appops.AppOpsControllerImpl$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$setListening$0();
                }
            });
            this.mDispatcher.registerReceiverWithHandler(this, new IntentFilter("android.media.action.MICROPHONE_MUTE_CHANGED"), this.mBGHandler);
            return;
        }
        this.mAppOps.stopWatchingActive(this);
        this.mAppOps.stopWatchingNoted(this);
        this.mAudioManager.unregisterAudioRecordingCallback(this.mAudioRecordingCallback);
        this.mBGHandler.removeCallbacksAndMessages(null);
        this.mDispatcher.unregisterReceiver(this);
        synchronized (this.mActiveItems) {
            this.mActiveItems.clear();
            this.mRecordingsByUid.clear();
        }
        synchronized (this.mNotedItems) {
            this.mNotedItems.clear();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setListening$0() {
        this.mAudioRecordingCallback.onRecordingConfigChanged(this.mAudioManager.getActiveRecordingConfigurations());
    }

    @Override // com.android.systemui.appops.AppOpsController
    public void addCallback(int[] iArr, AppOpsController.Callback callback) {
        int length = iArr.length;
        boolean z = false;
        for (int i = 0; i < length; i++) {
            if (this.mCallbacksByCode.containsKey(Integer.valueOf(iArr[i]))) {
                this.mCallbacksByCode.get(Integer.valueOf(iArr[i])).add(callback);
                z = true;
            }
        }
        if (z) {
            this.mCallbacks.add(callback);
        }
        if (this.mCallbacks.isEmpty()) {
            return;
        }
        setListening(true);
    }

    @Override // com.android.systemui.appops.AppOpsController
    public void removeCallback(int[] iArr, AppOpsController.Callback callback) {
        int length = iArr.length;
        for (int i = 0; i < length; i++) {
            if (this.mCallbacksByCode.containsKey(Integer.valueOf(iArr[i]))) {
                this.mCallbacksByCode.get(Integer.valueOf(iArr[i])).remove(callback);
            }
        }
        this.mCallbacks.remove(callback);
        if (this.mCallbacks.isEmpty()) {
            setListening(false);
        }
    }

    private AppOpItem getAppOpItemLocked(List<AppOpItem> list, int i, int i2, String str) {
        int size = list.size();
        for (int i3 = 0; i3 < size; i3++) {
            AppOpItem appOpItem = list.get(i3);
            if (appOpItem.getCode() == i && appOpItem.getUid() == i2 && appOpItem.getPackageName().equals(str)) {
                return appOpItem;
            }
        }
        return null;
    }

    private boolean updateActives(int i, int i2, String str, boolean z) {
        synchronized (this.mActiveItems) {
            AppOpItem appOpItemLocked = getAppOpItemLocked(this.mActiveItems, i, i2, str);
            boolean z2 = true;
            if (appOpItemLocked != null || !z) {
                if (appOpItemLocked == null || z) {
                    return false;
                }
                this.mActiveItems.remove(appOpItemLocked);
                return true;
            }
            AppOpItem appOpItem = new AppOpItem(i, i2, str, System.currentTimeMillis());
            if (i == 27) {
                appOpItem.setSilenced(isAllRecordingPausedLocked(i2));
            }
            this.mActiveItems.add(appOpItem);
            if (appOpItem.isSilenced()) {
                z2 = false;
            }
            return z2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeNoted(int i, int i2, String str) {
        boolean z;
        synchronized (this.mNotedItems) {
            AppOpItem appOpItemLocked = getAppOpItemLocked(this.mNotedItems, i, i2, str);
            if (appOpItemLocked == null) {
                return;
            }
            this.mNotedItems.remove(appOpItemLocked);
            synchronized (this.mActiveItems) {
                z = getAppOpItemLocked(this.mActiveItems, i, i2, str) != null;
            }
            if (z) {
                return;
            }
            lambda$notifySuscribers$1(i, i2, str, false);
        }
    }

    private boolean addNoted(int i, int i2, String str) {
        AppOpItem appOpItemLocked;
        boolean z;
        synchronized (this.mNotedItems) {
            appOpItemLocked = getAppOpItemLocked(this.mNotedItems, i, i2, str);
            if (appOpItemLocked == null) {
                appOpItemLocked = new AppOpItem(i, i2, str, System.currentTimeMillis());
                this.mNotedItems.add(appOpItemLocked);
                z = true;
            } else {
                z = false;
            }
        }
        this.mBGHandler.removeCallbacksAndMessages(appOpItemLocked);
        this.mBGHandler.scheduleRemoval(appOpItemLocked, 5000L);
        return z;
    }

    private boolean isUserSensitive(int i, int i2, String str) {
        String strOpToPermission = AppOpsManager.opToPermission(i);
        return (strOpToPermission == null || (this.mFlagsCache.getPermissionFlags(strOpToPermission, str, i2) & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) == 0) ? false : true;
    }

    private boolean isUserVisible(AppOpItem appOpItem) {
        return isUserVisible(appOpItem.getCode(), appOpItem.getUid(), appOpItem.getPackageName());
    }

    private boolean isLocationProvider(String str) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (this.mLastLocationProviderPackageUpdate + 30000 < jCurrentTimeMillis) {
            this.mLastLocationProviderPackageUpdate = jCurrentTimeMillis;
            this.mLocationProviderPackages = this.mLocationManager.getProviderPackages("fused");
        }
        return this.mLocationProviderPackages.contains(str);
    }

    private boolean isUserVisible(int i, int i2, String str) {
        if (i == 24 || i == 42 || i == 101 || i == 100) {
            return true;
        }
        if (i == 26 && isLocationProvider(str)) {
            return true;
        }
        return isUserSensitive(i, i2, str);
    }

    @Override // com.android.systemui.appops.AppOpsController
    public List<AppOpItem> getActiveAppOpsForUser(int i) {
        int i2;
        ArrayList arrayList = new ArrayList();
        synchronized (this.mActiveItems) {
            int size = this.mActiveItems.size();
            for (int i3 = 0; i3 < size; i3++) {
                AppOpItem appOpItem = this.mActiveItems.get(i3);
                if ((i == -1 || UserHandle.getUserId(appOpItem.getUid()) == i) && isUserVisible(appOpItem) && !appOpItem.isSilenced()) {
                    arrayList.add(appOpItem);
                }
            }
        }
        synchronized (this.mNotedItems) {
            int size2 = this.mNotedItems.size();
            for (i2 = 0; i2 < size2; i2++) {
                AppOpItem appOpItem2 = this.mNotedItems.get(i2);
                if ((i == -1 || UserHandle.getUserId(appOpItem2.getUid()) == i) && isUserVisible(appOpItem2)) {
                    arrayList.add(appOpItem2);
                }
            }
        }
        return arrayList;
    }

    private void notifySuscribers(final int i, final int i2, final String str, final boolean z) {
        this.mBGHandler.post(new Runnable() { // from class: com.android.systemui.appops.AppOpsControllerImpl$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$notifySuscribers$1(i, i2, str, z);
            }
        });
    }

    public void onOpActiveChanged(int i, int i2, String str, boolean z) {
        boolean z2;
        if (updateActives(i, i2, str, z)) {
            synchronized (this.mNotedItems) {
                z2 = getAppOpItemLocked(this.mNotedItems, i, i2, str) != null;
            }
            if (z2) {
                return;
            }
            notifySuscribers(i, i2, str, z);
        }
    }

    public void onOpNoted(int i, int i2, String str, int i3) {
        boolean z;
        if (i3 == 0 && addNoted(i, i2, str)) {
            synchronized (this.mActiveItems) {
                z = getAppOpItemLocked(this.mActiveItems, i, i2, str) != null;
            }
            if (z) {
                return;
            }
            notifySuscribers(i, i2, str, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: notifySuscribersWorker, reason: merged with bridge method [inline-methods] */
    public void lambda$notifySuscribers$1(int i, int i2, String str, boolean z) {
        if (this.mCallbacksByCode.containsKey(Integer.valueOf(i)) && isUserVisible(i, i2, str)) {
            Iterator<AppOpsController.Callback> it = this.mCallbacksByCode.get(Integer.valueOf(i)).iterator();
            while (it.hasNext()) {
                it.next().onActiveStateChanged(i, i2, str, z);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("AppOpsController state:");
        printWriter.println("  Listening: " + this.mListening);
        printWriter.println("  Active Items:");
        for (int i = 0; i < this.mActiveItems.size(); i++) {
            AppOpItem appOpItem = this.mActiveItems.get(i);
            printWriter.print("    ");
            printWriter.println(appOpItem.toString());
        }
        printWriter.println("  Noted Items:");
        for (int i2 = 0; i2 < this.mNotedItems.size(); i2++) {
            AppOpItem appOpItem2 = this.mNotedItems.get(i2);
            printWriter.print("    ");
            printWriter.println(appOpItem2.toString());
        }
    }

    private boolean isAllRecordingPausedLocked(int i) {
        if (this.mMicMuted) {
            return true;
        }
        ArrayList<AudioRecordingConfiguration> arrayList = this.mRecordingsByUid.get(i);
        if (arrayList == null) {
            return false;
        }
        int size = arrayList.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (!arrayList.get(i2).isClientSilenced()) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRecordingPausedStatus() {
        boolean zIsAllRecordingPausedLocked;
        synchronized (this.mActiveItems) {
            int size = this.mActiveItems.size();
            for (int i = 0; i < size; i++) {
                AppOpItem appOpItem = this.mActiveItems.get(i);
                if (appOpItem.getCode() == 27 && appOpItem.isSilenced() != (zIsAllRecordingPausedLocked = isAllRecordingPausedLocked(appOpItem.getUid()))) {
                    appOpItem.setSilenced(zIsAllRecordingPausedLocked);
                    notifySuscribers(appOpItem.getCode(), appOpItem.getUid(), appOpItem.getPackageName(), !appOpItem.isSilenced());
                }
            }
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        this.mMicMuted = this.mAudioManager.isMicrophoneMute();
        updateRecordingPausedStatus();
    }

    protected class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        public void scheduleRemoval(final AppOpItem appOpItem, long j) {
            removeCallbacksAndMessages(appOpItem);
            postDelayed(new Runnable() { // from class: com.android.systemui.appops.AppOpsControllerImpl.H.1
                @Override // java.lang.Runnable
                public void run() {
                    AppOpsControllerImpl.this.removeNoted(appOpItem.getCode(), appOpItem.getUid(), appOpItem.getPackageName());
                }
            }, appOpItem, j);
        }
    }
}
