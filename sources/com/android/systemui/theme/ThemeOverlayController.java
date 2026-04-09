package com.android.systemui.theme;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.om.OverlayManager;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.google.android.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class ThemeOverlayController extends SystemUI {
    private final Handler mBgHandler;
    private BroadcastDispatcher mBroadcastDispatcher;
    private ThemeOverlayManager mThemeManager;
    private UserManager mUserManager;
    private CurrentUserTracker mUserTracker;

    public ThemeOverlayController(Context context, BroadcastDispatcher broadcastDispatcher, Handler handler) {
        super(context);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mBgHandler = handler;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mThemeManager = new ThemeOverlayManager((OverlayManager) this.mContext.getSystemService(OverlayManager.class), AsyncTask.THREAD_POOL_EXECUTOR, this.mContext.getString(R.string.launcher_overlayable_package), this.mContext.getString(R.string.themepicker_overlayable_package));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(new BroadcastReceiver() { // from class: com.android.systemui.theme.ThemeOverlayController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                ThemeOverlayController.this.updateThemeOverlays();
            }
        }, intentFilter, this.mBgHandler, UserHandle.ALL);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("theme_customization_overlay_packages"), false, new ContentObserver(this.mBgHandler) { // from class: com.android.systemui.theme.ThemeOverlayController.2
            public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
                if (ActivityManager.getCurrentUser() == i2) {
                    ThemeOverlayController.this.updateThemeOverlays();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("display_cutout_mode"), false, new ContentObserver(this.mBgHandler) { // from class: com.android.systemui.theme.ThemeOverlayController.3
            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri) {
                if (uri.equals(Settings.System.getUriFor("display_cutout_mode"))) {
                    reloadAssets("com.android.launcher3");
                    String defaultHomeApp = ThemeOverlayController.getDefaultHomeApp(((SystemUI) ThemeOverlayController.this).mContext);
                    if (defaultHomeApp.equals("com.android.launcher3")) {
                        return;
                    }
                    reloadAssets(defaultHomeApp);
                }
            }

            private void reloadAssets(String str) {
                try {
                    IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay")).reloadAssets(str, -2);
                } catch (RemoteException unused) {
                    Log.i("ThemeOverlayController", "Unable to reload resources for " + str);
                }
            }
        }, -1);
        CurrentUserTracker currentUserTracker = new CurrentUserTracker(this.mBroadcastDispatcher) { // from class: com.android.systemui.theme.ThemeOverlayController.4
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                ThemeOverlayController.this.updateThemeOverlays();
            }
        };
        this.mUserTracker = currentUserTracker;
        currentUserTracker.startTracking();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateThemeOverlays() {
        int currentUser = ActivityManager.getCurrentUser();
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "theme_customization_overlay_packages", currentUser);
        ArrayMap arrayMap = new ArrayMap();
        if (!TextUtils.isEmpty(stringForUser)) {
            try {
                JSONObject jSONObject = new JSONObject(stringForUser);
                for (String str : ThemeOverlayManager.THEME_CATEGORIES) {
                    if (jSONObject.has(str)) {
                        arrayMap.put(str, jSONObject.getString(str));
                    }
                }
            } catch (JSONException e) {
                Log.i("ThemeOverlayController", "Failed to parse THEME_CUSTOMIZATION_OVERLAY_PACKAGES.", e);
            }
        }
        HashSet hashSetNewHashSet = Sets.newHashSet(new UserHandle[]{UserHandle.of(currentUser)});
        for (UserInfo userInfo : this.mUserManager.getEnabledProfiles(currentUser)) {
            if (userInfo.isManagedProfile()) {
                hashSetNewHashSet.add(userInfo.getUserHandle());
            }
        }
        this.mThemeManager.applyCurrentUserOverlays(arrayMap, hashSetNewHashSet);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getDefaultHomeApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return packageManager.resolveActivity(intent, 65536).activityInfo.packageName;
    }
}
