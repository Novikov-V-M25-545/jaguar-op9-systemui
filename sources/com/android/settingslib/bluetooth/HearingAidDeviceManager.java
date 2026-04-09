package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import com.android.internal.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class HearingAidDeviceManager {
    private final LocalBluetoothManager mBtManager;
    private final List<CachedBluetoothDevice> mCachedDevices;

    private boolean isValidHiSyncId(long j) {
        return j != 0;
    }

    private void log(String str) {
    }

    HearingAidDeviceManager(LocalBluetoothManager localBluetoothManager, List<CachedBluetoothDevice> list) {
        this.mBtManager = localBluetoothManager;
        this.mCachedDevices = list;
    }

    void initHearingAidDeviceIfNeeded(CachedBluetoothDevice cachedBluetoothDevice) {
        long hiSyncId = getHiSyncId(cachedBluetoothDevice.getDevice());
        if (isValidHiSyncId(hiSyncId)) {
            cachedBluetoothDevice.setHiSyncId(hiSyncId);
        }
    }

    private long getHiSyncId(BluetoothDevice bluetoothDevice) {
        HearingAidProfile hearingAidProfile = this.mBtManager.getProfileManager().getHearingAidProfile();
        if (hearingAidProfile != null) {
            return hearingAidProfile.getHiSyncId(bluetoothDevice);
        }
        return 0L;
    }

    boolean setSubDeviceIfNeeded(CachedBluetoothDevice cachedBluetoothDevice) {
        CachedBluetoothDevice cachedDevice;
        long hiSyncId = cachedBluetoothDevice.getHiSyncId();
        if (!isValidHiSyncId(hiSyncId) || (cachedDevice = getCachedDevice(hiSyncId)) == null) {
            return false;
        }
        cachedDevice.setSubDevice(cachedBluetoothDevice);
        return true;
    }

    private CachedBluetoothDevice getCachedDevice(long j) {
        for (int size = this.mCachedDevices.size() - 1; size >= 0; size--) {
            CachedBluetoothDevice cachedBluetoothDevice = this.mCachedDevices.get(size);
            if (cachedBluetoothDevice.getHiSyncId() == j) {
                return cachedBluetoothDevice;
            }
        }
        return null;
    }

    void updateHearingAidsDevices() {
        HashSet hashSet = new HashSet();
        for (CachedBluetoothDevice cachedBluetoothDevice : this.mCachedDevices) {
            if (!isValidHiSyncId(cachedBluetoothDevice.getHiSyncId())) {
                long hiSyncId = getHiSyncId(cachedBluetoothDevice.getDevice());
                if (isValidHiSyncId(hiSyncId)) {
                    cachedBluetoothDevice.setHiSyncId(hiSyncId);
                    hashSet.add(Long.valueOf(hiSyncId));
                }
            }
        }
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            onHiSyncIdChanged(((Long) it.next()).longValue());
        }
    }

    @VisibleForTesting
    void onHiSyncIdChanged(long j) {
        CachedBluetoothDevice cachedBluetoothDevice;
        int size = this.mCachedDevices.size() - 1;
        int i = -1;
        while (size >= 0) {
            CachedBluetoothDevice cachedBluetoothDevice2 = this.mCachedDevices.get(size);
            if (cachedBluetoothDevice2.getHiSyncId() == j) {
                if (i != -1) {
                    if (cachedBluetoothDevice2.isConnected()) {
                        cachedBluetoothDevice = this.mCachedDevices.get(i);
                        size = i;
                    } else {
                        cachedBluetoothDevice2 = this.mCachedDevices.get(i);
                        cachedBluetoothDevice = cachedBluetoothDevice2;
                    }
                    cachedBluetoothDevice2.setSubDevice(cachedBluetoothDevice);
                    this.mCachedDevices.remove(size);
                    log("onHiSyncIdChanged: removed from UI device =" + cachedBluetoothDevice + ", with hiSyncId=" + j);
                    this.mBtManager.getEventManager().dispatchDeviceRemoved(cachedBluetoothDevice);
                    return;
                }
                i = size;
            }
            size--;
        }
    }

    boolean onProfileConnectionStateChangedIfProcessed(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (i == 0) {
            CachedBluetoothDevice cachedBluetoothDeviceFindMainDevice = findMainDevice(cachedBluetoothDevice);
            if (cachedBluetoothDeviceFindMainDevice != null) {
                cachedBluetoothDeviceFindMainDevice.refresh();
                return true;
            }
            CachedBluetoothDevice subDevice = cachedBluetoothDevice.getSubDevice();
            if (subDevice == null || !subDevice.isConnected()) {
                return false;
            }
            this.mBtManager.getEventManager().dispatchDeviceRemoved(cachedBluetoothDevice);
            cachedBluetoothDevice.switchSubDeviceContent();
            cachedBluetoothDevice.refresh();
            this.mBtManager.getEventManager().dispatchDeviceAdded(cachedBluetoothDevice);
            return true;
        }
        if (i != 2) {
            return false;
        }
        onHiSyncIdChanged(cachedBluetoothDevice.getHiSyncId());
        CachedBluetoothDevice cachedBluetoothDeviceFindMainDevice2 = findMainDevice(cachedBluetoothDevice);
        if (cachedBluetoothDeviceFindMainDevice2 == null) {
            return false;
        }
        if (cachedBluetoothDeviceFindMainDevice2.isConnected()) {
            cachedBluetoothDeviceFindMainDevice2.refresh();
            return true;
        }
        this.mBtManager.getEventManager().dispatchDeviceRemoved(cachedBluetoothDeviceFindMainDevice2);
        cachedBluetoothDeviceFindMainDevice2.switchSubDeviceContent();
        cachedBluetoothDeviceFindMainDevice2.refresh();
        this.mBtManager.getEventManager().dispatchDeviceAdded(cachedBluetoothDeviceFindMainDevice2);
        return true;
    }

    CachedBluetoothDevice findMainDevice(CachedBluetoothDevice cachedBluetoothDevice) {
        CachedBluetoothDevice subDevice;
        for (CachedBluetoothDevice cachedBluetoothDevice2 : this.mCachedDevices) {
            if (isValidHiSyncId(cachedBluetoothDevice2.getHiSyncId()) && (subDevice = cachedBluetoothDevice2.getSubDevice()) != null && subDevice.equals(cachedBluetoothDevice)) {
                return cachedBluetoothDevice2;
            }
        }
        return null;
    }
}
