package com.android.systemui.statusbar.notification;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.Pair;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.NotificationChannels;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class InstantAppNotifier extends SystemUI implements CommandQueue.Callbacks, KeyguardStateController.Callback {
    private final CommandQueue mCommandQueue;
    private final ArraySet<Pair<String, Integer>> mCurrentNotifs;
    private final Divider mDivider;
    private boolean mDockedStackExists;
    private final Handler mHandler;
    private KeyguardStateController mKeyguardStateController;
    private final Executor mUiBgExecutor;
    private final SynchronousUserSwitchObserver mUserSwitchListener;

    public InstantAppNotifier(Context context, CommandQueue commandQueue, Executor executor, Divider divider) {
        super(context);
        this.mHandler = new Handler();
        this.mCurrentNotifs = new ArraySet<>();
        this.mUserSwitchListener = new AnonymousClass1();
        this.mDivider = divider;
        this.mCommandQueue = commandQueue;
        this.mUiBgExecutor = executor;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        try {
            ActivityManager.getService().registerUserSwitchObserver(this.mUserSwitchListener, "InstantAppNotifier");
        } catch (RemoteException unused) {
        }
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mKeyguardStateController.addCallback(this);
        this.mDivider.registerInSplitScreenListener(new Consumer() { // from class: com.android.systemui.statusbar.notification.InstantAppNotifier$$ExternalSyntheticLambda1
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$start$0((Boolean) obj);
            }
        });
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        for (StatusBarNotification statusBarNotification : notificationManager.getActiveNotifications()) {
            if (statusBarNotification.getId() == 7) {
                notificationManager.cancel(statusBarNotification.getTag(), statusBarNotification.getId());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$start$0(Boolean bool) {
        this.mDockedStackExists = bool.booleanValue();
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        if (this.mContext.getDisplayId() == i) {
            updateForegroundInstantApps();
        }
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void preloadRecentApps() {
        updateForegroundInstantApps();
    }

    /* renamed from: com.android.systemui.statusbar.notification.InstantAppNotifier$1, reason: invalid class name */
    class AnonymousClass1 extends SynchronousUserSwitchObserver {
        public void onUserSwitching(int i) throws RemoteException {
        }

        AnonymousClass1() {
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            InstantAppNotifier.this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.notification.InstantAppNotifier$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$onUserSwitchComplete$0();
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onUserSwitchComplete$0() {
            InstantAppNotifier.this.updateForegroundInstantApps();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateForegroundInstantApps() {
        final NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        final IPackageManager packageManager = AppGlobals.getPackageManager();
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.InstantAppNotifier$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$updateForegroundInstantApps$2(notificationManager, packageManager);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateForegroundInstantApps$2(final NotificationManager notificationManager, IPackageManager iPackageManager) {
        int windowingMode;
        ArraySet<Pair<String, Integer>> arraySet = new ArraySet<>(this.mCurrentNotifs);
        try {
            ActivityManager.StackInfo focusedStackInfo = ActivityTaskManager.getService().getFocusedStackInfo();
            if (focusedStackInfo != null && ((windowingMode = focusedStackInfo.configuration.windowConfiguration.getWindowingMode()) == 1 || windowingMode == 4 || windowingMode == 5)) {
                checkAndPostForStack(focusedStackInfo, arraySet, notificationManager, iPackageManager);
            }
            if (this.mDockedStackExists) {
                checkAndPostForPrimaryScreen(arraySet, notificationManager, iPackageManager);
            }
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        arraySet.forEach(new Consumer() { // from class: com.android.systemui.statusbar.notification.InstantAppNotifier$$ExternalSyntheticLambda2
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.lambda$updateForegroundInstantApps$1(notificationManager, (Pair) obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateForegroundInstantApps$1(NotificationManager notificationManager, Pair pair) {
        this.mCurrentNotifs.remove(pair);
        notificationManager.cancelAsUser((String) pair.first, 7, new UserHandle(((Integer) pair.second).intValue()));
    }

    private void checkAndPostForPrimaryScreen(ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        try {
            checkAndPostForStack(ActivityTaskManager.getService().getStackInfo(3, 0), arraySet, notificationManager, iPackageManager);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    private void checkAndPostForStack(ActivityManager.StackInfo stackInfo, ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        if (stackInfo != null) {
            try {
                ComponentName componentName = stackInfo.topActivity;
                if (componentName == null) {
                    return;
                }
                String packageName = componentName.getPackageName();
                if (arraySet.remove(new Pair(packageName, Integer.valueOf(stackInfo.userId)))) {
                    return;
                }
                ApplicationInfo applicationInfo = iPackageManager.getApplicationInfo(packageName, LineageHardwareManager.FEATURE_DISPLAY_MODES, stackInfo.userId);
                if (applicationInfo.isInstantApp()) {
                    postInstantAppNotif(packageName, stackInfo.userId, applicationInfo, notificationManager, stackInfo.taskIds[r8.length - 1]);
                }
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    }

    private void postInstantAppNotif(String str, int i, ApplicationInfo applicationInfo, NotificationManager notificationManager, int i2) {
        int i3;
        String str2;
        Notification.Action action;
        int i4;
        PendingIntent activityAsUser;
        Notification.Builder builder;
        PendingIntent pendingIntent;
        ComponentName instantAppInstallerComponent;
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", this.mContext.getString(R.string.instant_apps));
        this.mCurrentNotifs.add(new Pair<>(str, Integer.valueOf(i)));
        String string = this.mContext.getString(R.string.instant_apps_help_url);
        boolean z = !string.isEmpty();
        Context context = this.mContext;
        if (z) {
            i3 = R.string.instant_apps_message_with_help;
        } else {
            i3 = R.string.instant_apps_message;
        }
        String string2 = context.getString(i3);
        UserHandle userHandleOf = UserHandle.of(i);
        Notification.Action actionBuild = new Notification.Action.Builder((Icon) null, this.mContext.getString(R.string.app_info), PendingIntent.getActivityAsUser(this.mContext, 0, new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", str, null)), 67108864, null, userHandleOf)).build();
        if (z) {
            str2 = "android.intent.action.VIEW";
            action = actionBuild;
            activityAsUser = PendingIntent.getActivityAsUser(this.mContext, 0, new Intent("android.intent.action.VIEW").setData(Uri.parse(string)), 67108864, null, userHandleOf);
            i4 = i2;
        } else {
            str2 = "android.intent.action.VIEW";
            action = actionBuild;
            i4 = i2;
            activityAsUser = null;
        }
        Intent taskIntent = getTaskIntent(i4, i);
        Notification.Builder builder2 = new Notification.Builder(this.mContext, NotificationChannels.GENERAL);
        if (taskIntent == null || !taskIntent.isWebIntent()) {
            builder = builder2;
            pendingIntent = activityAsUser;
        } else {
            taskIntent.setComponent(null).setPackage(null).addFlags(512).addFlags(268435456);
            pendingIntent = activityAsUser;
            PendingIntent activityAsUser2 = PendingIntent.getActivityAsUser(this.mContext, 0, taskIntent, 67108864, null, userHandleOf);
            try {
                instantAppInstallerComponent = AppGlobals.getPackageManager().getInstantAppInstallerComponent();
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
                instantAppInstallerComponent = null;
            }
            Notification.Action actionBuild2 = new Notification.Action.Builder((Icon) null, this.mContext.getString(R.string.go_to_web), PendingIntent.getActivityAsUser(this.mContext, 0, new Intent().setComponent(instantAppInstallerComponent).setAction(str2).addCategory("android.intent.category.BROWSABLE").addCategory("unique:" + System.currentTimeMillis()).putExtra("android.intent.extra.PACKAGE_NAME", applicationInfo.packageName).putExtra("android.intent.extra.VERSION_CODE", applicationInfo.versionCode & Integer.MAX_VALUE).putExtra("android.intent.extra.LONG_VERSION_CODE", applicationInfo.longVersionCode).putExtra("android.intent.extra.INSTANT_APP_FAILURE", activityAsUser2), 67108864, null, userHandleOf)).build();
            builder = builder2;
            builder.addAction(actionBuild2);
        }
        Notification.Builder color = builder.addExtras(bundle).addAction(action).setContentIntent(pendingIntent).setColor(this.mContext.getColor(R.color.instant_apps_color));
        Context context2 = this.mContext;
        notificationManager.notifyAsUser(str, 7, color.setContentTitle(context2.getString(R.string.instant_apps_title, applicationInfo.loadLabel(context2.getPackageManager()))).setLargeIcon(Icon.createWithResource(str, applicationInfo.icon)).setSmallIcon(Icon.createWithResource(this.mContext.getPackageName(), R.drawable.instant_icon)).setContentText(string2).setStyle(new Notification.BigTextStyle().bigText(string2)).setOngoing(true).build(), new UserHandle(i));
    }

    private Intent getTaskIntent(int i, int i2) {
        try {
            List list = ActivityTaskManager.getService().getRecentTasks(5, 0, i2).getList();
            for (int i3 = 0; i3 < list.size(); i3++) {
                if (((ActivityManager.RecentTaskInfo) list.get(i3)).id == i) {
                    return ((ActivityManager.RecentTaskInfo) list.get(i3)).baseIntent;
                }
            }
            return null;
        } catch (RemoteException unused) {
            return null;
        }
    }
}
