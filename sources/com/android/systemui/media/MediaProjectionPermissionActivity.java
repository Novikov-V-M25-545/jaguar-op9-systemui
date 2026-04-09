package com.android.systemui.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.util.Utils;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class MediaProjectionPermissionActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private AlertDialog mDialog;
    private String mPackageName;
    private IMediaProjectionManager mService;
    private int mUid;

    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) throws PackageManager.NameNotFoundException {
        String string;
        String string2;
        super.onCreate(bundle);
        this.mPackageName = getCallingPackage();
        this.mService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        if (this.mPackageName == null) {
            finish();
            return;
        }
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mPackageName, 0);
            int i = applicationInfo.uid;
            this.mUid = i;
            try {
                if (this.mService.hasProjectionPermission(i, this.mPackageName)) {
                    setResult(-1, getMediaProjectionIntent(this.mUid, this.mPackageName));
                    finish();
                    return;
                }
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(42.0f);
                if (Utils.isHeadlessRemoteDisplayProvider(packageManager, this.mPackageName)) {
                    string2 = getString(R.string.media_projection_dialog_service_text);
                    string = getString(R.string.media_projection_dialog_service_title);
                } else {
                    String string3 = applicationInfo.loadLabel(packageManager).toString();
                    int length = string3.length();
                    int iCharCount = 0;
                    while (iCharCount < length) {
                        int iCodePointAt = string3.codePointAt(iCharCount);
                        int type = Character.getType(iCodePointAt);
                        if (type == 13 || type == 15 || type == 14) {
                            string3 = string3.substring(0, iCharCount) + "…";
                            break;
                        }
                        iCharCount += Character.charCount(iCodePointAt);
                    }
                    if (string3.isEmpty()) {
                        string3 = this.mPackageName;
                    }
                    String strUnicodeWrap = BidiFormatter.getInstance().unicodeWrap(TextUtils.ellipsize(string3, textPaint, 500.0f, TextUtils.TruncateAt.END).toString());
                    String string4 = getString(R.string.media_projection_dialog_text, new Object[]{strUnicodeWrap});
                    SpannableString spannableString = new SpannableString(string4);
                    int iIndexOf = string4.indexOf(strUnicodeWrap);
                    if (iIndexOf >= 0) {
                        spannableString.setSpan(new StyleSpan(1), iIndexOf, strUnicodeWrap.length() + iIndexOf, 0);
                    }
                    string = getString(R.string.media_projection_dialog_title, new Object[]{strUnicodeWrap});
                    string2 = spannableString;
                }
                View viewInflate = View.inflate(this, R.layout.media_projection_dialog_title, null);
                ((TextView) viewInflate.findViewById(R.id.dialog_title)).setText(string);
                AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setCustomTitle(viewInflate).setMessage(string2).setPositiveButton(R.string.media_projection_action_text, this).setNegativeButton(android.R.string.cancel, this).setOnCancelListener(this).create();
                this.mDialog = alertDialogCreate;
                alertDialogCreate.create();
                this.mDialog.getButton(-1).setFilterTouchesWhenObscured(true);
                Window window = this.mDialog.getWindow();
                window.setType(2003);
                window.addSystemFlags(LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES);
                this.mDialog.show();
            } catch (RemoteException e) {
                Log.e("MediaProjectionPermissionActivity", "Error checking projection permissions", e);
                finish();
            }
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("MediaProjectionPermissionActivity", "unable to look up package name", e2);
            finish();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0031 A[DONT_GENERATE, PHI: r2
  0x0031: PHI (r2v6 android.app.AlertDialog) = (r2v5 android.app.AlertDialog), (r2v7 android.app.AlertDialog) binds: [B:11:0x001f, B:19:0x002f] A[DONT_GENERATE, DONT_INLINE]] */
    @Override // android.content.DialogInterface.OnClickListener
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void onClick(android.content.DialogInterface r2, int r3) {
        /*
            r1 = this;
            r2 = -1
            if (r3 != r2) goto L2d
            int r3 = r1.mUid     // Catch: java.lang.Throwable -> Lf android.os.RemoteException -> L11
            java.lang.String r0 = r1.mPackageName     // Catch: java.lang.Throwable -> Lf android.os.RemoteException -> L11
            android.content.Intent r3 = r1.getMediaProjectionIntent(r3, r0)     // Catch: java.lang.Throwable -> Lf android.os.RemoteException -> L11
            r1.setResult(r2, r3)     // Catch: java.lang.Throwable -> Lf android.os.RemoteException -> L11
            goto L2d
        Lf:
            r2 = move-exception
            goto L22
        L11:
            r2 = move-exception
            java.lang.String r3 = "MediaProjectionPermissionActivity"
            java.lang.String r0 = "Error granting projection permission"
            android.util.Log.e(r3, r0, r2)     // Catch: java.lang.Throwable -> Lf
            r2 = 0
            r1.setResult(r2)     // Catch: java.lang.Throwable -> Lf
            android.app.AlertDialog r2 = r1.mDialog
            if (r2 == 0) goto L34
            goto L31
        L22:
            android.app.AlertDialog r3 = r1.mDialog
            if (r3 == 0) goto L29
            r3.dismiss()
        L29:
            r1.finish()
            throw r2
        L2d:
            android.app.AlertDialog r2 = r1.mDialog
            if (r2 == 0) goto L34
        L31:
            r2.dismiss()
        L34:
            r1.finish()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.MediaProjectionPermissionActivity.onClick(android.content.DialogInterface, int):void");
    }

    private Intent getMediaProjectionIntent(int i, String str) throws RemoteException {
        IMediaProjection iMediaProjectionCreateProjection = this.mService.createProjection(i, str, 0, false);
        Intent intent = new Intent();
        intent.putExtra("android.media.projection.extra.EXTRA_MEDIA_PROJECTION", iMediaProjectionCreateProjection.asBinder());
        return intent;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }
}
