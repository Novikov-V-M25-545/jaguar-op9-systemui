package com.android.systemui.statusbar.phone;

import android.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.content.res.ApkAssets;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.Dumpable;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class NavigationModeController implements Dumpable {
    private static final String TAG = "NavigationModeController";
    private final Context mContext;
    private Context mCurrentUserContext;
    private final DeviceProvisionedController.DeviceProvisionedListener mDeviceProvisionedCallback;
    private ArrayList<ModeChangedListener> mListeners = new ArrayList<>();
    private final IOverlayManager mOverlayManager;
    private BroadcastReceiver mReceiver;
    private SettingsObserver mSettingsObserver;
    private final Executor mUiBgExecutor;

    public interface ModeChangedListener {
        void onNavigationModeChanged(int i);

        default void onSettingsChanged() {
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            for (int i = 0; i < NavigationModeController.this.mListeners.size(); i++) {
                ((ModeChangedListener) NavigationModeController.this.mListeners.get(i)).onSettingsChanged();
            }
        }
    }

    public NavigationModeController(Context context, DeviceProvisionedController deviceProvisionedController, ConfigurationController configurationController, Executor executor) throws Resources.NotFoundException {
        DeviceProvisionedController.DeviceProvisionedListener deviceProvisionedListener = new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.statusbar.phone.NavigationModeController.1
            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSwitched() throws Resources.NotFoundException {
                Log.d(NavigationModeController.TAG, "onUserSwitched: " + ActivityManagerWrapper.getInstance().getCurrentUserId());
                NavigationModeController.this.updateCurrentInteractionMode(true, true);
            }
        };
        this.mDeviceProvisionedCallback = deviceProvisionedListener;
        this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.NavigationModeController.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) throws Resources.NotFoundException {
                Log.d(NavigationModeController.TAG, "ACTION_OVERLAY_CHANGED");
                NavigationModeController.this.updateCurrentInteractionMode(true, true);
            }
        };
        this.mContext = context;
        this.mCurrentUserContext = context;
        this.mOverlayManager = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
        this.mUiBgExecutor = executor;
        deviceProvisionedController.addCallback(deviceProvisionedListener);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.OVERLAY_CHANGED");
        intentFilter.addDataScheme("package");
        intentFilter.addDataSchemeSpecificPart("android", 0);
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        this.mSettingsObserver = new SettingsObserver(new Handler());
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("back_gesture_height"), false, this.mSettingsObserver, -1);
        configurationController.addCallback(new ConfigurationController.ConfigurationListener() { // from class: com.android.systemui.statusbar.phone.NavigationModeController.3
            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onOverlayChanged() throws Resources.NotFoundException {
                Log.d(NavigationModeController.TAG, "onOverlayChanged");
                NavigationModeController.this.updateCurrentInteractionMode(true, true);
            }
        });
        updateCurrentInteractionMode(false, false);
    }

    public void updateCurrentInteractionMode(boolean z, boolean z2) throws Resources.NotFoundException {
        Context currentUserContext = getCurrentUserContext();
        this.mCurrentUserContext = currentUserContext;
        final int currentInteractionMode = getCurrentInteractionMode(currentUserContext);
        if (currentInteractionMode == 2) {
            switchToDefaultGestureNavOverlayIfNecessary(Settings.Secure.getFloat(this.mCurrentUserContext.getContentResolver(), "gesture_navbar_length", 1.0f) == 0.0f);
        }
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationModeController$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateCurrentInteractionMode$0(currentInteractionMode);
            }
        });
        Log.e(TAG, "updateCurrentInteractionMode: mode=" + currentInteractionMode);
        dumpAssetPaths(this.mCurrentUserContext);
        if (z) {
            for (int i = 0; i < this.mListeners.size(); i++) {
                this.mListeners.get(i).onNavigationModeChanged(currentInteractionMode);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateCurrentInteractionMode$0(int i) {
        Settings.Secure.putString(this.mCurrentUserContext.getContentResolver(), "navigation_mode", String.valueOf(i));
    }

    public int addListener(ModeChangedListener modeChangedListener) {
        this.mListeners.add(modeChangedListener);
        return getCurrentInteractionMode(this.mCurrentUserContext);
    }

    public void removeListener(ModeChangedListener modeChangedListener) {
        this.mListeners.remove(modeChangedListener);
    }

    private int getCurrentInteractionMode(Context context) throws Resources.NotFoundException {
        int integer = context.getResources().getInteger(R.integer.config_dreamOverlayMaxReconnectAttempts);
        Log.d(TAG, "getCurrentInteractionMode: mode=" + integer + " contextUser=" + context.getUserId());
        return integer;
    }

    public Context getCurrentUserContext() {
        int currentUserId = ActivityManagerWrapper.getInstance().getCurrentUserId();
        Log.d(TAG, "getCurrentUserContext: contextUser=" + this.mContext.getUserId() + " currentUser=" + currentUserId);
        if (this.mContext.getUserId() == currentUserId) {
            return this.mContext;
        }
        try {
            Context context = this.mContext;
            return context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.of(currentUserId));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to create package context", e);
            return null;
        }
    }

    private void switchToDefaultGestureNavOverlayIfNecessary(boolean z) throws Resources.NotFoundException {
        int userId = this.mCurrentUserContext.getUserId();
        try {
            IOverlayManager iOverlayManagerAsInterface = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
            String str = "com.custom.overlay.systemui.gestural.hidden";
            OverlayInfo overlayInfo = iOverlayManagerAsInterface.getOverlayInfo(z ? "com.custom.overlay.systemui.gestural.hidden" : "com.android.internal.systemui.navbar.gestural", userId);
            if (overlayInfo == null || overlayInfo.isEnabled()) {
                return;
            }
            int dimensionPixelSize = this.mCurrentUserContext.getResources().getDimensionPixelSize(R.dimen.car_single_line_list_item_height);
            if (!z) {
                str = "com.android.internal.systemui.navbar.gestural";
            }
            iOverlayManagerAsInterface.setEnabledExclusiveInCategory(str, userId);
            int dimensionPixelSize2 = this.mCurrentUserContext.getResources().getDimensionPixelSize(R.dimen.car_single_line_list_item_height);
            float f = dimensionPixelSize2 == 0 ? 1.0f : dimensionPixelSize / dimensionPixelSize2;
            Settings.Secure.putFloat(this.mCurrentUserContext.getContentResolver(), "back_gesture_inset_scale_left", f);
            Settings.Secure.putFloat(this.mCurrentUserContext.getContentResolver(), "back_gesture_inset_scale_right", f);
            Log.v(TAG, "Moved back sensitivity for user " + userId + " to scale " + f);
        } catch (RemoteException | IllegalStateException | SecurityException unused) {
            Log.e(TAG, "Failed to switch to default gesture nav overlay for user " + userId);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String strJoin;
        printWriter.println("NavigationModeController:");
        printWriter.println("  mode=" + getCurrentInteractionMode(this.mCurrentUserContext));
        try {
            strJoin = String.join(", ", this.mOverlayManager.getDefaultOverlayPackages());
        } catch (RemoteException unused) {
            strJoin = "failed_to_fetch";
        }
        printWriter.println("  defaultOverlays=" + strJoin);
        dumpAssetPaths(this.mCurrentUserContext);
    }

    private void dumpAssetPaths(Context context) {
        String str = TAG;
        Log.d(str, "  contextUser=" + this.mCurrentUserContext.getUserId());
        Log.d(str, "  assetPaths=");
        for (ApkAssets apkAssets : context.getResources().getAssets().getApkAssets()) {
            Log.d(TAG, "    " + apkAssets.getAssetPath());
        }
    }
}
