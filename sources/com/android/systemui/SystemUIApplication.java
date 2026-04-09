package com.android.systemui;

import android.app.ActivityThread;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.TimingsTraceLog;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.dagger.ContextComponentHelper;
import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.util.NotificationChannels;
import java.lang.reflect.InvocationTargetException;

/* loaded from: classes.dex */
public class SystemUIApplication extends Application implements SystemUIAppComponentFactory.ContextInitializer {
    private BootCompleteCacheImpl mBootCompleteCache;
    private ContextComponentHelper mComponentHelper;
    private SystemUIAppComponentFactory.ContextAvailableCallback mContextAvailableCallback;
    private SystemUIRootComponent mRootComponent;
    private SystemUI[] mServices;
    private boolean mServicesStarted;

    public SystemUIApplication() {
        Log.v("SystemUIService", "SystemUIApplication constructed.");
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        Log.v("SystemUIService", "SystemUIApplication created.");
        TimingsTraceLog timingsTraceLog = new TimingsTraceLog("SystemUIBootTiming", 4096L);
        timingsTraceLog.traceBegin("DependencyInjection");
        this.mContextAvailableCallback.onContextAvailable(this);
        SystemUIRootComponent rootComponent = SystemUIFactory.getInstance().getRootComponent();
        this.mRootComponent = rootComponent;
        this.mComponentHelper = rootComponent.getContextComponentHelper();
        this.mBootCompleteCache = this.mRootComponent.provideBootCacheImpl();
        timingsTraceLog.traceEnd();
        setTheme(R.style.Theme_SystemUI);
        if (Process.myUserHandle().equals(UserHandle.SYSTEM)) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            intentFilter.setPriority(1000);
            registerReceiver(new BroadcastReceiver() { // from class: com.android.systemui.SystemUIApplication.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (SystemUIApplication.this.mBootCompleteCache.isBootComplete()) {
                        return;
                    }
                    SystemUIApplication.this.unregisterReceiver(this);
                    SystemUIApplication.this.mBootCompleteCache.setBootComplete();
                    if (SystemUIApplication.this.mServicesStarted) {
                        int length = SystemUIApplication.this.mServices.length;
                        for (int i = 0; i < length; i++) {
                            SystemUIApplication.this.mServices[i].onBootCompleted();
                        }
                    }
                }
            }, intentFilter);
            registerReceiver(new BroadcastReceiver() { // from class: com.android.systemui.SystemUIApplication.2
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction()) && SystemUIApplication.this.mBootCompleteCache.isBootComplete()) {
                        NotificationChannels.createAll(context);
                    }
                }
            }, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
            return;
        }
        String strCurrentProcessName = ActivityThread.currentProcessName();
        ApplicationInfo applicationInfo = getApplicationInfo();
        if (strCurrentProcessName != null) {
            if (strCurrentProcessName.startsWith(applicationInfo.processName + ":")) {
                return;
            }
        }
        startSecondaryUserServicesIfNeeded();
    }

    public void startServicesIfNeeded() {
        startServicesIfNeeded("StartServices", SystemUIFactory.getInstance().getSystemUIServiceComponents(getResources()));
    }

    void startSecondaryUserServicesIfNeeded() {
        startServicesIfNeeded("StartSecondaryServices", SystemUIFactory.getInstance().getSystemUIServiceComponentsPerUser(getResources()));
    }

    private void startServicesIfNeeded(String str, String[] strArr) {
        if (this.mServicesStarted) {
            return;
        }
        this.mServices = new SystemUI[strArr.length];
        if (!this.mBootCompleteCache.isBootComplete() && "1".equals(SystemProperties.get("sys.boot_completed"))) {
            this.mBootCompleteCache.setBootComplete();
        }
        DumpManager dumpManagerCreateDumpManager = this.mRootComponent.createDumpManager();
        Log.v("SystemUIService", "Starting SystemUI services for user " + Process.myUserHandle().getIdentifier() + ".");
        TimingsTraceLog timingsTraceLog = new TimingsTraceLog("SystemUIBootTiming", 4096L);
        timingsTraceLog.traceBegin(str);
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            String str2 = strArr[i];
            timingsTraceLog.traceBegin(str + str2);
            long jCurrentTimeMillis = System.currentTimeMillis();
            try {
                SystemUI systemUIResolveSystemUI = this.mComponentHelper.resolveSystemUI(str2);
                if (systemUIResolveSystemUI == null) {
                    systemUIResolveSystemUI = (SystemUI) Class.forName(str2).getConstructor(Context.class).newInstance(this);
                }
                SystemUI[] systemUIArr = this.mServices;
                systemUIArr[i] = systemUIResolveSystemUI;
                systemUIArr[i].start();
                timingsTraceLog.traceEnd();
                long jCurrentTimeMillis2 = System.currentTimeMillis() - jCurrentTimeMillis;
                if (jCurrentTimeMillis2 > 1000) {
                    Log.w("SystemUIService", "Initialization of " + str2 + " took " + jCurrentTimeMillis2 + " ms");
                }
                if (this.mBootCompleteCache.isBootComplete()) {
                    this.mServices[i].onBootCompleted();
                }
                dumpManagerCreateDumpManager.registerDumpable(this.mServices[i].getClass().getName(), this.mServices[i]);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        this.mRootComponent.getInitController().executePostInitTasks();
        timingsTraceLog.traceEnd();
        this.mServicesStarted = true;
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mServicesStarted) {
            this.mRootComponent.getConfigurationController().onConfigurationChanged(configuration);
            int length = this.mServices.length;
            for (int i = 0; i < length; i++) {
                SystemUI[] systemUIArr = this.mServices;
                if (systemUIArr[i] != null) {
                    systemUIArr[i].onConfigurationChanged(configuration);
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUIAppComponentFactory.ContextInitializer
    public void setContextAvailableCallback(SystemUIAppComponentFactory.ContextAvailableCallback contextAvailableCallback) {
        this.mContextAvailableCallback = contextAvailableCallback;
    }
}
