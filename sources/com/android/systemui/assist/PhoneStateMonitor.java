package com.android.systemui.assist;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

/* loaded from: classes.dex */
public final class PhoneStateMonitor {
    private static final String[] DEFAULT_HOME_CHANGE_ACTIONS = {"android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED", "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED"};
    private final Context mContext;
    private ComponentName mDefaultHome;
    private boolean mLauncherShowing;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    private final StatusBarStateController mStatusBarStateController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);

    private boolean isLauncherInAllApps() {
        return false;
    }

    private boolean isLauncherInOverview() {
        return false;
    }

    PhoneStateMonitor(Context context, BroadcastDispatcher broadcastDispatcher, Optional<Lazy<StatusBar>> optional, BootCompleteCache bootCompleteCache) {
        this.mContext = context;
        this.mStatusBarOptionalLazy = optional;
        ActivityManagerWrapper activityManagerWrapper = ActivityManagerWrapper.getInstance();
        this.mDefaultHome = getCurrentDefaultHome();
        bootCompleteCache.addListener(new BootCompleteCache.BootCompleteListener() { // from class: com.android.systemui.assist.PhoneStateMonitor$$ExternalSyntheticLambda0
            @Override // com.android.systemui.BootCompleteCache.BootCompleteListener
            public final void onBootComplete() {
                this.f$0.lambda$new$0();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        for (String str : DEFAULT_HOME_CHANGE_ACTIONS) {
            intentFilter.addAction(str);
        }
        broadcastDispatcher.registerReceiver(new BroadcastReceiver() { // from class: com.android.systemui.assist.PhoneStateMonitor.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                PhoneStateMonitor.this.mDefaultHome = PhoneStateMonitor.getCurrentDefaultHome();
            }
        }, intentFilter);
        this.mLauncherShowing = isLauncherShowing(activityManagerWrapper.getRunningTask());
        activityManagerWrapper.registerTaskStackListener(new TaskStackChangeListener() { // from class: com.android.systemui.assist.PhoneStateMonitor.2
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
                PhoneStateMonitor phoneStateMonitor = PhoneStateMonitor.this;
                phoneStateMonitor.mLauncherShowing = phoneStateMonitor.isLauncherShowing(runningTaskInfo);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        this.mDefaultHome = getCurrentDefaultHome();
    }

    public int getPhoneState() {
        if (isShadeFullscreen()) {
            return getPhoneLockscreenState();
        }
        if (this.mLauncherShowing) {
            return getPhoneLauncherState();
        }
        return getPhoneAppState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ComponentName getCurrentDefaultHome() {
        ArrayList<ResolveInfo> arrayList = new ArrayList();
        ComponentName homeActivities = PackageManagerWrapper.getInstance().getHomeActivities(arrayList);
        if (homeActivities != null) {
            return homeActivities;
        }
        int i = Integer.MIN_VALUE;
        while (true) {
            ComponentName componentName = null;
            for (ResolveInfo resolveInfo : arrayList) {
                int i2 = resolveInfo.priority;
                if (i2 <= i) {
                    if (i2 == i) {
                        break;
                    }
                } else {
                    componentName = resolveInfo.activityInfo.getComponentName();
                    i = resolveInfo.priority;
                }
            }
            return componentName;
        }
    }

    private int getPhoneLockscreenState() {
        if (isDozing()) {
            return 1;
        }
        if (isBouncerShowing()) {
            return 3;
        }
        return isKeyguardLocked() ? 2 : 4;
    }

    private int getPhoneLauncherState() {
        if (isLauncherInOverview()) {
            return 6;
        }
        return isLauncherInAllApps() ? 7 : 5;
    }

    private int getPhoneAppState() {
        if (isAppImmersive()) {
            return 9;
        }
        return isAppFullscreen() ? 10 : 8;
    }

    private boolean isShadeFullscreen() {
        int state = this.mStatusBarStateController.getState();
        return state == 1 || state == 2;
    }

    private boolean isDozing() {
        return this.mStatusBarStateController.isDozing();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isLauncherShowing(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo == null) {
            return false;
        }
        return runningTaskInfo.topActivity.equals(this.mDefaultHome);
    }

    private boolean isAppImmersive() {
        return this.mStatusBarOptionalLazy.get().get().inImmersiveMode();
    }

    private boolean isAppFullscreen() {
        return this.mStatusBarOptionalLazy.get().get().inFullscreenMode();
    }

    private boolean isBouncerShowing() {
        return ((Boolean) this.mStatusBarOptionalLazy.map(new Function() { // from class: com.android.systemui.assist.PhoneStateMonitor$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return PhoneStateMonitor.lambda$isBouncerShowing$1((Lazy) obj);
            }
        }).orElse(Boolean.FALSE)).booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Boolean lambda$isBouncerShowing$1(Lazy lazy) {
        return Boolean.valueOf(((StatusBar) lazy.get()).isBouncerShowing());
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
        return keyguardManager != null && keyguardManager.isKeyguardLocked();
    }
}
