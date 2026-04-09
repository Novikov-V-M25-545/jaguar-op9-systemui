package com.android.systemui.statusbar.notification.collection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.phone.StatusBar;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: TargetSdkResolver.kt */
/* loaded from: classes.dex */
public final class TargetSdkResolver {
    private final String TAG;
    private final Context context;

    public TargetSdkResolver(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
        this.TAG = "TargetSdkResolver";
    }

    public final void initialize(@NotNull CommonNotifCollection collection) {
        Intrinsics.checkParameterIsNotNull(collection, "collection");
        collection.addCollectionListener(new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.collection.TargetSdkResolver.initialize.1
            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryBind(@NotNull NotificationEntry entry, @NotNull StatusBarNotification sbn) {
                Intrinsics.checkParameterIsNotNull(entry, "entry");
                Intrinsics.checkParameterIsNotNull(sbn, "sbn");
                entry.targetSdk = TargetSdkResolver.this.resolveNotificationSdk(sbn);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int resolveNotificationSdk(StatusBarNotification statusBarNotification) {
        Context context = this.context;
        UserHandle user = statusBarNotification.getUser();
        Intrinsics.checkExpressionValueIsNotNull(user, "sbn.user");
        try {
            return StatusBar.getPackageManagerForUser(context, user.getIdentifier()).getApplicationInfo(statusBarNotification.getPackageName(), 0).targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(this.TAG, "Failed looking up ApplicationInfo for " + statusBarNotification.getPackageName(), e);
            return 0;
        }
    }
}
