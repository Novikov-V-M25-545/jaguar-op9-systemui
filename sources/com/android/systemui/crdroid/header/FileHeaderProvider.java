package com.android.systemui.crdroid.header;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.crdroid.header.StatusBarHeaderMachine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class FileHeaderProvider implements StatusBarHeaderMachine.IStatusBarHeaderProvider {
    private Context mContext;
    private Drawable mImage;

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void disableProvider() {
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public String getName() {
        return "file";
    }

    public FileHeaderProvider(Context context) {
        this.mContext = context;
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void settingsChanged(Uri uri) throws IOException {
        String stringForUser;
        boolean z = Settings.System.getIntForUser(this.mContext.getContentResolver(), "status_bar_custom_header", 0, -2) == 1;
        if (uri != null && uri.equals(Settings.System.getUriFor("status_bar_file_header_image")) && (stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "status_bar_file_header_image", -2)) != null) {
            saveHeaderImage(Uri.parse(stringForUser));
        }
        if (z) {
            loadHeaderImage();
        }
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public void enableProvider() throws IOException {
        settingsChanged(null);
    }

    private void saveHeaderImage(Uri uri) throws IOException {
        try {
            InputStream inputStreamOpenInputStream = this.mContext.getContentResolver().openInputStream(uri);
            File file = new File(this.mContext.getFilesDir(), "custom_file_header_image");
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[LineageHardwareManager.FEATURE_DISPLAY_MODES];
            while (true) {
                int i = inputStreamOpenInputStream.read(bArr);
                if (i != -1) {
                    fileOutputStream.write(bArr, 0, i);
                } else {
                    fileOutputStream.flush();
                    return;
                }
            }
        } catch (IOException unused) {
            Log.e("FileHeaderProvider", "Save header image failed  " + uri);
        }
    }

    private void loadHeaderImage() {
        this.mImage = null;
        File file = new File(this.mContext.getFilesDir(), "custom_file_header_image");
        if (file.exists()) {
            this.mImage = new BitmapDrawable(this.mContext.getResources(), BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
    }

    @Override // com.android.systemui.crdroid.header.StatusBarHeaderMachine.IStatusBarHeaderProvider
    public Drawable getCurrent(Calendar calendar) {
        return this.mImage;
    }
}
