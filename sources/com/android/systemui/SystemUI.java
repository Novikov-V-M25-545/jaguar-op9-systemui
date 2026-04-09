package com.android.systemui;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public abstract class SystemUI implements Dumpable {
    protected final Context mContext;

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    protected void onBootCompleted() {
    }

    protected void onConfigurationChanged(Configuration configuration) {
    }

    public abstract void start();

    public SystemUI(Context context) {
        this.mContext = context;
    }

    public static void overrideNotificationAppName(Context context, Notification.Builder builder, boolean z) {
        String string;
        Bundle bundle = new Bundle();
        if (z) {
            string = context.getString(android.R.string.lock_to_app_unlock_pattern);
        } else {
            string = context.getString(android.R.string.lock_to_app_unlock_password);
        }
        bundle.putString("android.substName", string);
        builder.addExtras(bundle);
    }
}
