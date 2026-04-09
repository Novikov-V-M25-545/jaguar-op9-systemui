package com.android.systemui.globalactions;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListAdapter;
import com.android.systemui.R;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class GlobalActionsPowerDialog {
    public static int mNotificationBackgroundAlpha;

    public static Dialog create(Context context, ListAdapter listAdapter) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.global_actions_power_dialog, (ViewGroup) null);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            viewGroup.addView(listAdapter.getView(i, null, viewGroup));
        }
        Resources resources = context.getResources();
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(1);
        dialog.setContentView(viewGroup);
        Window window = dialog.getWindow();
        window.setType(2020);
        window.setTitle("");
        Drawable drawable = resources.getDrawable(R.drawable.control_background);
        drawable.setAlpha(mNotificationBackgroundAlpha);
        window.setBackgroundDrawable(drawable);
        window.addFlags(LineageHardwareManager.FEATURE_COLOR_BALANCE);
        return dialog;
    }
}
