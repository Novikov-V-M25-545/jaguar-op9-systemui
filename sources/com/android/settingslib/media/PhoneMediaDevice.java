package com.android.settingslib.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2Manager;
import com.android.settingslib.R$drawable;
import com.android.settingslib.bluetooth.BluetoothUtils;

/* loaded from: classes.dex */
public class PhoneMediaDevice extends MediaDevice {
    private String mSummary;

    @Override // com.android.settingslib.media.MediaDevice
    public boolean isConnected() {
        return true;
    }

    PhoneMediaDevice(Context context, MediaRouter2Manager mediaRouter2Manager, MediaRoute2Info mediaRoute2Info, String str) {
        super(context, mediaRouter2Manager, mediaRoute2Info, str);
        this.mSummary = "";
        initDeviceRecord();
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0020  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0027  */
    @Override // com.android.settingslib.media.MediaDevice
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.String getName() {
        /*
            r2 = this;
            android.media.MediaRoute2Info r0 = r2.mRouteInfo
            int r0 = r0.getType()
            r1 = 3
            if (r0 == r1) goto L27
            r1 = 4
            if (r0 == r1) goto L27
            r1 = 9
            if (r0 == r1) goto L20
            r1 = 22
            if (r0 == r1) goto L27
            switch(r0) {
                case 11: goto L27;
                case 12: goto L27;
                case 13: goto L20;
                default: goto L17;
            }
        L17:
            android.content.Context r2 = r2.mContext
            int r0 = com.android.settingslib.R$string.media_transfer_this_device_name
            java.lang.String r2 = r2.getString(r0)
            goto L2f
        L20:
            android.media.MediaRoute2Info r2 = r2.mRouteInfo
            java.lang.CharSequence r2 = r2.getName()
            goto L2f
        L27:
            android.content.Context r2 = r2.mContext
            int r0 = com.android.settingslib.R$string.media_transfer_wired_usb_device_name
            java.lang.String r2 = r2.getString(r0)
        L2f:
            java.lang.String r2 = r2.toString()
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.media.PhoneMediaDevice.getName():java.lang.String");
    }

    @Override // com.android.settingslib.media.MediaDevice
    public Drawable getIcon() {
        Drawable iconWithoutBackground = getIconWithoutBackground();
        setColorFilter(iconWithoutBackground);
        return BluetoothUtils.buildAdvancedDrawable(this.mContext, iconWithoutBackground);
    }

    @Override // com.android.settingslib.media.MediaDevice
    public Drawable getIconWithoutBackground() {
        return this.mContext.getDrawable(getDrawableResId());
    }

    int getDrawableResId() {
        int type = this.mRouteInfo.getType();
        if (type != 3 && type != 4 && type != 9 && type != 22) {
            switch (type) {
                case 11:
                case 12:
                case 13:
                    break;
                default:
                    return R$drawable.ic_smartphone;
            }
        }
        return R$drawable.ic_headphone;
    }

    @Override // com.android.settingslib.media.MediaDevice
    public String getId() {
        int type = this.mRouteInfo.getType();
        if (type == 3 || type == 4) {
            return "wired_headset_media_device_id";
        }
        if (type != 9 && type != 22) {
            switch (type) {
                case 11:
                case 12:
                case 13:
                    break;
                default:
                    return "phone_media_device_id";
            }
        }
        return "usb_headset_media_device_id";
    }
}
