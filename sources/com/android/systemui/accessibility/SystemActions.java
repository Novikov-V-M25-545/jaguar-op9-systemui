package com.android.systemui.accessibility;

import android.R;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.Locale;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class SystemActions extends SystemUI {
    private AccessibilityManager mA11yManager;
    private Locale mLocale;
    private SystemActionsBroadcastReceiver mReceiver;
    private Recents mRecents;
    private StatusBar mStatusBar;

    public SystemActions(Context context) {
        super(context);
        this.mRecents = (Recents) Dependency.get(Recents.class);
        this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
        this.mReceiver = new SystemActionsBroadcastReceiver();
        this.mLocale = this.mContext.getResources().getConfiguration().getLocales().get(0);
        this.mA11yManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        Context context = this.mContext;
        SystemActionsBroadcastReceiver systemActionsBroadcastReceiver = this.mReceiver;
        context.registerReceiverForAllUsers(systemActionsBroadcastReceiver, systemActionsBroadcastReceiver.createIntentFilter(), "com.android.systemui.permission.SELF", null);
        registerActions();
    }

    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Locale locale = this.mContext.getResources().getConfiguration().getLocales().get(0);
        if (locale.equals(this.mLocale)) {
            return;
        }
        this.mLocale = locale;
        registerActions();
    }

    private void registerActions() {
        RemoteAction remoteActionCreateRemoteAction = createRemoteAction(R.string.accessibility_button_instructional_text, "SYSTEM_ACTION_BACK");
        RemoteAction remoteActionCreateRemoteAction2 = createRemoteAction(R.string.accessibility_dialog_button_allow, "SYSTEM_ACTION_HOME");
        RemoteAction remoteActionCreateRemoteAction3 = createRemoteAction(R.string.accessibility_freeform_caption, "SYSTEM_ACTION_RECENTS");
        RemoteAction remoteActionCreateRemoteAction4 = createRemoteAction(R.string.accessibility_dialog_button_uninstall, "SYSTEM_ACTION_NOTIFICATIONS");
        RemoteAction remoteActionCreateRemoteAction5 = createRemoteAction(R.string.accessibility_enable_service_title, "SYSTEM_ACTION_QUICK_SETTINGS");
        RemoteAction remoteActionCreateRemoteAction6 = createRemoteAction(R.string.accessibility_edit_shortcut_menu_volume_title, "SYSTEM_ACTION_POWER_DIALOG");
        RemoteAction remoteActionCreateRemoteAction7 = createRemoteAction(R.string.accessibility_dialog_button_deny, "SYSTEM_ACTION_LOCK_SCREEN");
        RemoteAction remoteActionCreateRemoteAction8 = createRemoteAction(R.string.accessibility_gesture_3finger_instructional_text, "SYSTEM_ACTION_TAKE_SCREENSHOT");
        RemoteAction remoteActionCreateRemoteAction9 = createRemoteAction(R.string.accessibility_button_prompt_text, "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT");
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction, 1);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction2, 2);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction3, 3);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction4, 4);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction5, 5);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction6, 6);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction7, 8);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction8, 9);
        this.mA11yManager.registerSystemAction(remoteActionCreateRemoteAction9, 13);
    }

    public void register(int i) {
        int i2;
        String str;
        switch (i) {
            case 1:
                i2 = R.string.accessibility_button_instructional_text;
                str = "SYSTEM_ACTION_BACK";
                break;
            case 2:
                i2 = R.string.accessibility_dialog_button_allow;
                str = "SYSTEM_ACTION_HOME";
                break;
            case SensorManagerPlugin.Sensor.TYPE_SWIPE /* 3 */:
                i2 = R.string.accessibility_freeform_caption;
                str = "SYSTEM_ACTION_RECENTS";
                break;
            case 4:
                i2 = R.string.accessibility_dialog_button_uninstall;
                str = "SYSTEM_ACTION_NOTIFICATIONS";
                break;
            case 5:
                i2 = R.string.accessibility_enable_service_title;
                str = "SYSTEM_ACTION_QUICK_SETTINGS";
                break;
            case 6:
                i2 = R.string.accessibility_edit_shortcut_menu_volume_title;
                str = "SYSTEM_ACTION_POWER_DIALOG";
                break;
            case 7:
            case 10:
            default:
                return;
            case QS.VERSION /* 8 */:
                i2 = R.string.accessibility_dialog_button_deny;
                str = "SYSTEM_ACTION_LOCK_SCREEN";
                break;
            case 9:
                i2 = R.string.accessibility_gesture_3finger_instructional_text;
                str = "SYSTEM_ACTION_TAKE_SCREENSHOT";
                break;
            case 11:
                i2 = R.string.accessibility_edit_shortcut_menu_button_title;
                str = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON";
                break;
            case 12:
                i2 = R.string.accessibility_dialog_touch_filtered_warning;
                str = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU";
                break;
            case 13:
                i2 = R.string.accessibility_button_prompt_text;
                str = "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT";
                break;
        }
        this.mA11yManager.registerSystemAction(createRemoteAction(i2, str), i);
    }

    private RemoteAction createRemoteAction(int i, String str) {
        return new RemoteAction(Icon.createWithResource(this.mContext, R.drawable.ic_info), this.mContext.getString(i), this.mContext.getString(i), this.mReceiver.createPendingIntent(this.mContext, str));
    }

    public void unregister(int i) {
        this.mA11yManager.unregisterSystemAction(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleBack() {
        sendDownAndUpKeyEvents(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleHome() {
        sendDownAndUpKeyEvents(3);
    }

    private void sendDownAndUpKeyEvents(int i) {
        long jUptimeMillis = SystemClock.uptimeMillis();
        sendKeyEventIdentityCleared(i, 0, jUptimeMillis, jUptimeMillis);
        sendKeyEventIdentityCleared(i, 1, jUptimeMillis, SystemClock.uptimeMillis());
    }

    private void sendKeyEventIdentityCleared(int i, int i2, long j, long j2) {
        KeyEvent keyEventObtain = KeyEvent.obtain(j, j2, i2, i, 0, 0, -1, 0, 8, 257, null);
        InputManager.getInstance().injectInputEvent(keyEventObtain, 0);
        keyEventObtain.recycle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRecents() {
        this.mRecents.toggleRecentApps();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNotifications() {
        this.mStatusBar.animateExpandNotificationsPanel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQuickSettings() {
        this.mStatusBar.animateExpandSettingsPanel(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePowerDialog() {
        try {
            WindowManagerGlobal.getWindowManagerService().showGlobalActions();
        } catch (RemoteException unused) {
            Log.e("SystemActions", "failed to display power dialog.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleLockScreen() {
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        ((PowerManager) this.mContext.getSystemService(PowerManager.class)).goToSleep(SystemClock.uptimeMillis(), 7, 0);
        try {
            windowManagerService.lockNow((Bundle) null);
        } catch (RemoteException unused) {
            Log.e("SystemActions", "failed to lock screen.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTakeScreenshot() {
        new ScreenshotHelper(this.mContext).takeScreenshot(1, true, true, 0, new Handler(Looper.getMainLooper()), (Consumer) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAccessibilityButton() {
        AccessibilityManager.getInstance(this.mContext).notifyAccessibilityButtonClicked(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAccessibilityButtonChooser() {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAccessibilityShortcut() {
        this.mA11yManager.performAccessibilityShortcut();
    }

    private class SystemActionsBroadcastReceiver extends BroadcastReceiver {
        private SystemActionsBroadcastReceiver() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public PendingIntent createPendingIntent(Context context, String str) {
            str.hashCode();
            switch (str) {
                case "SYSTEM_ACTION_BACK":
                case "SYSTEM_ACTION_HOME":
                case "SYSTEM_ACTION_POWER_DIALOG":
                case "SYSTEM_ACTION_NOTIFICATIONS":
                case "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT":
                case "SYSTEM_ACTION_LOCK_SCREEN":
                case "SYSTEM_ACTION_RECENTS":
                case "SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU":
                case "SYSTEM_ACTION_ACCESSIBILITY_BUTTON":
                case "SYSTEM_ACTION_TAKE_SCREENSHOT":
                case "SYSTEM_ACTION_QUICK_SETTINGS":
                    Intent intent = new Intent(str);
                    intent.setPackage(context.getPackageName());
                    return PendingIntent.getBroadcast(context, 0, intent, 0);
                default:
                    return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public IntentFilter createIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("SYSTEM_ACTION_BACK");
            intentFilter.addAction("SYSTEM_ACTION_HOME");
            intentFilter.addAction("SYSTEM_ACTION_RECENTS");
            intentFilter.addAction("SYSTEM_ACTION_NOTIFICATIONS");
            intentFilter.addAction("SYSTEM_ACTION_QUICK_SETTINGS");
            intentFilter.addAction("SYSTEM_ACTION_POWER_DIALOG");
            intentFilter.addAction("SYSTEM_ACTION_LOCK_SCREEN");
            intentFilter.addAction("SYSTEM_ACTION_TAKE_SCREENSHOT");
            intentFilter.addAction("SYSTEM_ACTION_ACCESSIBILITY_BUTTON");
            intentFilter.addAction("SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU");
            intentFilter.addAction("SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT");
            return intentFilter;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            switch (action) {
                case "SYSTEM_ACTION_BACK":
                    SystemActions.this.handleBack();
                    break;
                case "SYSTEM_ACTION_HOME":
                    SystemActions.this.handleHome();
                    break;
                case "SYSTEM_ACTION_POWER_DIALOG":
                    SystemActions.this.handlePowerDialog();
                    break;
                case "SYSTEM_ACTION_NOTIFICATIONS":
                    SystemActions.this.handleNotifications();
                    break;
                case "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT":
                    SystemActions.this.handleAccessibilityShortcut();
                    break;
                case "SYSTEM_ACTION_LOCK_SCREEN":
                    SystemActions.this.handleLockScreen();
                    break;
                case "SYSTEM_ACTION_RECENTS":
                    SystemActions.this.handleRecents();
                    break;
                case "SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU":
                    SystemActions.this.handleAccessibilityButtonChooser();
                    break;
                case "SYSTEM_ACTION_ACCESSIBILITY_BUTTON":
                    SystemActions.this.handleAccessibilityButton();
                    break;
                case "SYSTEM_ACTION_TAKE_SCREENSHOT":
                    SystemActions.this.handleTakeScreenshot();
                    break;
                case "SYSTEM_ACTION_QUICK_SETTINGS":
                    SystemActions.this.handleQuickSettings();
                    break;
            }
        }
    }
}
