package com.android.settingslib.users;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;

/* loaded from: classes.dex */
public class UserCreatingDialog extends AlertDialog {
    public UserCreatingDialog(Context context) {
        this(context, false);
    }

    public UserCreatingDialog(Context context, boolean z) {
        super(context, R.style.Theme.DeviceDefault.Light.Dialog.Alert);
        inflateContent(z);
        getWindow().setType(2010);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.privateFlags = 272;
        getWindow().setAttributes(attributes);
    }

    private void inflateContent(boolean z) {
        int i;
        setCancelable(false);
        View viewInflate = LayoutInflater.from(getContext()).inflate(R$layout.user_creation_progress_dialog, (ViewGroup) null);
        Context context = getContext();
        if (z) {
            i = R$string.creating_new_guest_dialog_message;
        } else {
            i = R$string.creating_new_user_dialog_message;
        }
        String string = context.getString(i);
        viewInflate.setAccessibilityPaneTitle(string);
        ((TextView) viewInflate.findViewById(R$id.message)).setText(string);
        setView(viewInflate);
    }
}
