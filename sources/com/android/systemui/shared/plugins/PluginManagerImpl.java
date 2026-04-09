package com.android.systemui.shared.plugins;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.PluginManager;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/* loaded from: classes.dex */
public class PluginManagerImpl extends BroadcastReceiver implements PluginManager {
    private static final String TAG = PluginManagerImpl.class.getSimpleName();
    private final boolean isDebuggable;
    private final Map<String, ClassLoader> mClassLoaders;
    private final Context mContext;
    private final PluginInstanceManagerFactory mFactory;
    private boolean mHasOneShot;
    private boolean mListening;
    private Looper mLooper;
    private final ArraySet<String> mOneShotPackages;
    private ClassLoaderFilter mParentClassLoader;
    private final PluginEnabler mPluginEnabler;
    private final PluginInitializer mPluginInitializer;
    private final ArrayMap<PluginListener<?>, PluginInstanceManager> mPluginMap;
    private final PluginPrefs mPluginPrefs;
    private final ArraySet<String> mWhitelistedPlugins;

    public PluginManagerImpl(Context context, PluginInitializer pluginInitializer) {
        this(context, new PluginInstanceManagerFactory(), Build.IS_DEBUGGABLE, Thread.getUncaughtExceptionPreHandler(), pluginInitializer);
    }

    @VisibleForTesting
    PluginManagerImpl(Context context, PluginInstanceManagerFactory pluginInstanceManagerFactory, boolean z, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, final PluginInitializer pluginInitializer) {
        this.mPluginMap = new ArrayMap<>();
        this.mClassLoaders = new ArrayMap();
        this.mOneShotPackages = new ArraySet<>();
        ArraySet<String> arraySet = new ArraySet<>();
        this.mWhitelistedPlugins = arraySet;
        this.mContext = context;
        this.mFactory = pluginInstanceManagerFactory;
        this.mLooper = pluginInitializer.getBgLooper();
        this.isDebuggable = true;
        arraySet.addAll(Arrays.asList(pluginInitializer.getWhitelistedPlugins(context)));
        this.mPluginPrefs = new PluginPrefs(context);
        this.mPluginEnabler = pluginInitializer.getPluginEnabler(context);
        this.mPluginInitializer = pluginInitializer;
        Thread.setUncaughtExceptionPreHandler(new PluginExceptionHandler(uncaughtExceptionHandler));
        new Handler(this.mLooper).post(new Runnable() { // from class: com.android.systemui.shared.plugins.PluginManagerImpl.1
            @Override // java.lang.Runnable
            public void run() {
                pluginInitializer.onPluginManagerInit();
            }
        });
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public String[] getWhitelistedPlugins() {
        return (String[]) this.mWhitelistedPlugins.toArray(new String[0]);
    }

    public PluginEnabler getPluginEnabler() {
        return this.mPluginEnabler;
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(PluginListener<T> pluginListener, Class<?> cls) {
        addPluginListener((PluginListener) pluginListener, cls, false);
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(PluginListener<T> pluginListener, Class<?> cls, boolean z) {
        addPluginListener(PluginManager.Helper.getAction(cls), pluginListener, cls, z);
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T extends Plugin> void addPluginListener(String str, PluginListener<T> pluginListener, Class<?> cls) {
        addPluginListener(str, pluginListener, cls, false);
    }

    public <T extends Plugin> void addPluginListener(String str, PluginListener<T> pluginListener, Class cls, boolean z) {
        this.mPluginPrefs.addAction(str);
        PluginInstanceManager pluginInstanceManagerCreatePluginInstanceManager = this.mFactory.createPluginInstanceManager(this.mContext, str, pluginListener, z, this.mLooper, cls, this);
        pluginInstanceManagerCreatePluginInstanceManager.loadAll();
        synchronized (this) {
            this.mPluginMap.put(pluginListener, pluginInstanceManagerCreatePluginInstanceManager);
        }
        startListening();
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public void removePluginListener(PluginListener<?> pluginListener) {
        synchronized (this) {
            if (this.mPluginMap.containsKey(pluginListener)) {
                this.mPluginMap.remove(pluginListener).destroy();
                if (this.mPluginMap.size() == 0) {
                    stopListening();
                }
            }
        }
    }

    private void startListening() {
        if (this.mListening) {
            return;
        }
        this.mListening = true;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this, intentFilter);
        intentFilter.addAction("com.android.systemui.action.PLUGIN_CHANGED");
        intentFilter.addAction("com.android.systemui.action.DISABLE_PLUGIN");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this, intentFilter, "com.android.systemui.permission.PLUGIN", null);
        this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    private void stopListening() {
        if (!this.mListening || this.mHasOneShot) {
            return;
        }
        this.mListening = false;
        this.mContext.unregisterReceiver(this);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        int disableReason;
        String string;
        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
            synchronized (this) {
                Iterator<PluginInstanceManager> it = this.mPluginMap.values().iterator();
                while (it.hasNext()) {
                    it.next().loadAll();
                }
            }
            return;
        }
        if ("com.android.systemui.action.DISABLE_PLUGIN".equals(intent.getAction())) {
            ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(intent.getData().toString().substring(10));
            if (isPluginWhitelisted(componentNameUnflattenFromString)) {
                return;
            }
            getPluginEnabler().setDisabled(componentNameUnflattenFromString, 1);
            ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).cancel(componentNameUnflattenFromString.getClassName(), 6);
            return;
        }
        String encodedSchemeSpecificPart = intent.getData().getEncodedSchemeSpecificPart();
        ComponentName componentNameUnflattenFromString2 = ComponentName.unflattenFromString(encodedSchemeSpecificPart);
        if (this.mOneShotPackages.contains(encodedSchemeSpecificPart)) {
            int identifier = Resources.getSystem().getIdentifier("stat_sys_warning", "drawable", "android");
            int identifier2 = Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android");
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                string = packageManager.getApplicationInfo(encodedSchemeSpecificPart, 0).loadLabel(packageManager).toString();
            } catch (PackageManager.NameNotFoundException unused) {
                string = encodedSchemeSpecificPart;
            }
            Notification.Builder contentText = new Notification.Builder(this.mContext, "ALR").setSmallIcon(identifier).setWhen(0L).setShowWhen(false).setPriority(2).setVisibility(1).setColor(this.mContext.getColor(identifier2)).setContentTitle("Plugin \"" + string + "\" has updated").setContentText("Restart SysUI for changes to take effect.");
            contentText.addAction(new Notification.Action.Builder((Icon) null, "Restart SysUI", PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.action.RESTART").setData(Uri.parse("package://" + encodedSchemeSpecificPart)), 0)).build());
            ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).notify(6, contentText.build());
        }
        if (clearClassLoader(encodedSchemeSpecificPart)) {
            if (Build.IS_ENG) {
                Toast.makeText(this.mContext, "Reloading " + encodedSchemeSpecificPart, 1).show();
            } else {
                Log.v(TAG, "Reloading " + encodedSchemeSpecificPart);
            }
        }
        if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction()) && componentNameUnflattenFromString2 != null && ((disableReason = getPluginEnabler().getDisableReason(componentNameUnflattenFromString2)) == 2 || disableReason == 3 || disableReason == 1)) {
            Log.i(TAG, "Re-enabling previously disabled plugin that has been updated: " + componentNameUnflattenFromString2.flattenToShortString());
            getPluginEnabler().setEnabled(componentNameUnflattenFromString2);
        }
        synchronized (this) {
            if (!"android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                Iterator<PluginInstanceManager> it2 = this.mPluginMap.values().iterator();
                while (it2.hasNext()) {
                    it2.next().onPackageChange(encodedSchemeSpecificPart);
                }
            } else {
                Iterator<PluginInstanceManager> it3 = this.mPluginMap.values().iterator();
                while (it3.hasNext()) {
                    it3.next().onPackageRemoved(encodedSchemeSpecificPart);
                }
            }
        }
    }

    public ClassLoader getClassLoader(ApplicationInfo applicationInfo) {
        if (!this.isDebuggable && !isPluginPackageWhitelisted(applicationInfo.packageName)) {
            Log.w(TAG, "Cannot get class loader for non-whitelisted plugin. Src:" + applicationInfo.sourceDir + ", pkg: " + applicationInfo.packageName);
            return null;
        }
        if (this.mClassLoaders.containsKey(applicationInfo.packageName)) {
            return this.mClassLoaders.get(applicationInfo.packageName);
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        LoadedApk.makePaths((ActivityThread) null, true, applicationInfo, arrayList, arrayList2);
        String str = File.pathSeparator;
        PathClassLoader pathClassLoader = new PathClassLoader(TextUtils.join(str, arrayList), TextUtils.join(str, arrayList2), getParentClassLoader());
        this.mClassLoaders.put(applicationInfo.packageName, pathClassLoader);
        return pathClassLoader;
    }

    private boolean clearClassLoader(String str) {
        return this.mClassLoaders.remove(str) != null;
    }

    ClassLoader getParentClassLoader() {
        if (this.mParentClassLoader == null) {
            this.mParentClassLoader = new ClassLoaderFilter(PluginManagerImpl.class.getClassLoader(), "com.android.systemui.plugin");
        }
        return this.mParentClassLoader;
    }

    @Override // com.android.systemui.shared.plugins.PluginManager
    public <T> boolean dependsOn(Plugin plugin, Class<T> cls) {
        synchronized (this) {
            for (int i = 0; i < this.mPluginMap.size(); i++) {
                if (this.mPluginMap.valueAt(i).dependsOn(plugin, cls)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void handleWtfs() {
        this.mPluginInitializer.handleWtfs();
    }

    @VisibleForTesting
    public static class PluginInstanceManagerFactory {
        public <T extends Plugin> PluginInstanceManager createPluginInstanceManager(Context context, String str, PluginListener<T> pluginListener, boolean z, Looper looper, Class<?> cls, PluginManagerImpl pluginManagerImpl) {
            return new PluginInstanceManager(context, str, pluginListener, z, looper, new VersionInfo().addClass(cls), pluginManagerImpl);
        }
    }

    private boolean isPluginPackageWhitelisted(String str) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(next);
            if (componentNameUnflattenFromString != null) {
                if (componentNameUnflattenFromString.getPackageName().equals(str)) {
                    return true;
                }
            } else if (next.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPluginWhitelisted(ComponentName componentName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(next);
            if (componentNameUnflattenFromString != null) {
                if (componentNameUnflattenFromString.equals(componentName)) {
                    return true;
                }
            } else if (next.equals(componentName.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private static class ClassLoaderFilter extends ClassLoader {
        private final ClassLoader mBase;
        private final String mPackage;

        public ClassLoaderFilter(ClassLoader classLoader, String str) {
            super(ClassLoader.getSystemClassLoader());
            this.mBase = classLoader;
            this.mPackage = str;
        }

        @Override // java.lang.ClassLoader
        protected Class<?> loadClass(String str, boolean z) throws ClassNotFoundException {
            if (!str.startsWith(this.mPackage)) {
                super.loadClass(str, z);
            }
            return this.mBase.loadClass(str);
        }
    }

    private class PluginExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler mHandler;

        private PluginExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.mHandler = uncaughtExceptionHandler;
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            if (SystemProperties.getBoolean("plugin.debugging", false)) {
                this.mHandler.uncaughtException(thread, th);
                return;
            }
            boolean zCheckStack = checkStack(th);
            if (!zCheckStack) {
                synchronized (this) {
                    Iterator it = PluginManagerImpl.this.mPluginMap.values().iterator();
                    while (it.hasNext()) {
                        zCheckStack |= ((PluginInstanceManager) it.next()).disableAll();
                    }
                }
            }
            if (zCheckStack) {
                th = new CrashWhilePluginActiveException(th);
            }
            this.mHandler.uncaughtException(thread, th);
        }

        private boolean checkStack(Throwable th) {
            boolean zCheckAndDisable;
            if (th == null) {
                return false;
            }
            synchronized (this) {
                zCheckAndDisable = false;
                for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                    Iterator it = PluginManagerImpl.this.mPluginMap.values().iterator();
                    while (it.hasNext()) {
                        zCheckAndDisable |= ((PluginInstanceManager) it.next()).checkAndDisable(stackTraceElement.getClassName());
                    }
                }
            }
            return checkStack(th.getCause()) | zCheckAndDisable;
        }
    }

    public static class CrashWhilePluginActiveException extends RuntimeException {
        public CrashWhilePluginActiveException(Throwable th) {
            super(th);
        }
    }
}
