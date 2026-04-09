package com.android.systemui.crdroid.header;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.util.crdroid.Utils;
import com.android.systemui.crdroid.header.StatusBarHeaderMachine;
import java.util.Calendar;

/* loaded from: classes.dex */
public class StaticHeaderProvider implements StatusBarHeaderMachine.IStatusBarHeaderProvider {
    private Context mContext;
    private String mImage;
    private String mPackageName;
    private Resources mRes;

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void disableProvider() {
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public String getName() {
        return "static";
    }

    public StaticHeaderProvider(Context context) {
        this.mContext = context;
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void settingsChanged(Uri uri) {
        boolean z = Settings.System.getIntForUser(this.mContext.getContentResolver(), "status_bar_custom_header", 0, -2) == 1;
        String stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "status_bar_custom_header_image", -2);
        if (stringForUser == null || !z || stringForUser.indexOf("/") == -1) {
            return;
        }
        String[] strArrSplit = stringForUser.split("/");
        this.mPackageName = strArrSplit[0];
        this.mImage = strArrSplit[1];
        loadHeaderImage();
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void enableProvider() {
        settingsChanged(null);
    }

    private void loadHeaderImage() {
        try {
            this.mRes = this.mContext.getPackageManager().getResourcesForApplication(this.mPackageName);
        } catch (Exception e) {
            Log.e("StaticHeaderProvider", "Failed to load icon pack " + this.mPackageName, e);
            this.mRes = null;
        }
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public Drawable getCurrent(Calendar calendar) {
        if (this.mRes == null) {
            return null;
        }
        if (!Utils.isPackageInstalled(this.mContext, this.mPackageName)) {
            Log.w("StaticHeaderProvider", "Header pack image " + this.mImage + " no longer available");
            return null;
        }
        try {
            Resources resources = this.mRes;
            return resources.getDrawable(resources.getIdentifier(this.mImage, "drawable", this.mPackageName), null);
        } catch (Resources.NotFoundException unused) {
            Log.w("StaticHeaderProvider", "No drawable found for " + calendar + " in " + this.mPackageName);
            return null;
        }
    }
}
