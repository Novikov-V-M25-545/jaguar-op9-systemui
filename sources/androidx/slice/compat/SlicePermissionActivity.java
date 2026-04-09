package androidx.slice.compat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.BidiFormatter;
import androidx.slice.core.R$id;
import androidx.slice.core.R$layout;
import androidx.slice.core.R$string;

/* loaded from: classes.dex */
public class SlicePermissionActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    private String mCallingPkg;
    private AlertDialog mDialog;
    private String mProviderPkg;
    private Uri mUri;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mUri = (Uri) getIntent().getParcelableExtra("slice_uri");
        this.mCallingPkg = getIntent().getStringExtra("pkg");
        this.mProviderPkg = getIntent().getStringExtra("provider_pkg");
        try {
            PackageManager packageManager = getPackageManager();
            String strUnicodeWrap = BidiFormatter.getInstance().unicodeWrap(loadSafeLabel(packageManager, packageManager.getApplicationInfo(this.mCallingPkg, 0)).toString());
            String strUnicodeWrap2 = BidiFormatter.getInstance().unicodeWrap(loadSafeLabel(packageManager, packageManager.getApplicationInfo(this.mProviderPkg, 0)).toString());
            AlertDialog alertDialogShow = new AlertDialog.Builder(this).setTitle(getString(R$string.abc_slice_permission_title, new Object[]{strUnicodeWrap, strUnicodeWrap2})).setView(R$layout.abc_slice_permission_request).setNegativeButton(R$string.abc_slice_permission_deny, this).setPositiveButton(R$string.abc_slice_permission_allow, this).setOnDismissListener(this).show();
            this.mDialog = alertDialogShow;
            ((TextView) alertDialogShow.getWindow().getDecorView().findViewById(R$id.text1)).setText(getString(R$string.abc_slice_permission_text_1, new Object[]{strUnicodeWrap2}));
            ((TextView) this.mDialog.getWindow().getDecorView().findViewById(R$id.text2)).setText(getString(R$string.abc_slice_permission_text_2, new Object[]{strUnicodeWrap2}));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SlicePermissionActivity", "Couldn't find package", e);
            finish();
        }
    }

    private CharSequence loadSafeLabel(PackageManager packageManager, ApplicationInfo applicationInfo) {
        String string = Html.fromHtml(applicationInfo.loadLabel(packageManager).toString()).toString();
        int length = string.length();
        int iCharCount = 0;
        while (iCharCount < length) {
            int iCodePointAt = string.codePointAt(iCharCount);
            int type = Character.getType(iCodePointAt);
            if (type == 13 || type == 15 || type == 14) {
                string = string.substring(0, iCharCount);
                break;
            }
            if (type == 12) {
                string = string.substring(0, iCharCount) + " " + string.substring(Character.charCount(iCodePointAt) + iCharCount);
            }
            iCharCount += Character.charCount(iCodePointAt);
        }
        String strTrim = string.trim();
        if (strTrim.isEmpty()) {
            return applicationInfo.packageName;
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(42.0f);
        return TextUtils.ellipsize(strTrim, textPaint, 500.0f, TextUtils.TruncateAt.END);
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            SliceProviderCompat.grantSlicePermission(this, getPackageName(), this.mCallingPkg, this.mUri.buildUpon().path("").build());
        }
        finish();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog == null || !alertDialog.isShowing()) {
            return;
        }
        this.mDialog.cancel();
    }
}
