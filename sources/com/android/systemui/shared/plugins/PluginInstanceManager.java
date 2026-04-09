package com.android.systemui.shared.plugins;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginFragment;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.VersionInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class PluginInstanceManager<T extends Plugin> {
    private final boolean isDebuggable;
    private final String mAction;
    private final boolean mAllowMultiple;
    private final Context mContext;
    private final PluginListener<T> mListener;

    @VisibleForTesting
    final PluginInstanceManager<T>.MainHandler mMainHandler;
    private final PluginManagerImpl mManager;

    @VisibleForTesting
    final PluginInstanceManager<T>.PluginHandler mPluginHandler;
    private final PackageManager mPm;
    private final VersionInfo mVersion;
    private final ArraySet<String> mWhitelistedPlugins;

    PluginInstanceManager(Context context, String str, PluginListener<T> pluginListener, boolean z, Looper looper, VersionInfo versionInfo, PluginManagerImpl pluginManagerImpl) {
        this(context, context.getPackageManager(), str, pluginListener, z, looper, versionInfo, pluginManagerImpl, Build.IS_DEBUGGABLE, pluginManagerImpl.getWhitelistedPlugins());
    }

    @VisibleForTesting
    PluginInstanceManager(Context context, PackageManager packageManager, String str, PluginListener<T> pluginListener, boolean z, Looper looper, VersionInfo versionInfo, PluginManagerImpl pluginManagerImpl, boolean z2, String[] strArr) {
        ArraySet<String> arraySet = new ArraySet<>();
        this.mWhitelistedPlugins = arraySet;
        this.mMainHandler = new MainHandler(Looper.getMainLooper());
        this.mPluginHandler = new PluginHandler(looper);
        this.mManager = pluginManagerImpl;
        this.mContext = context;
        this.mPm = packageManager;
        this.mAction = str;
        this.mListener = pluginListener;
        this.mAllowMultiple = z;
        this.mVersion = versionInfo;
        arraySet.addAll(Arrays.asList(strArr));
        this.isDebuggable = true;
    }

    public void loadAll() {
        this.mPluginHandler.sendEmptyMessage(1);
    }

    public void destroy() {
        Iterator it = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
        while (it.hasNext()) {
            this.mMainHandler.obtainMessage(2, ((PluginInfo) it.next()).mPlugin).sendToTarget();
        }
    }

    public void onPackageRemoved(String str) {
        this.mPluginHandler.obtainMessage(3, str).sendToTarget();
    }

    public void onPackageChange(String str) {
        this.mPluginHandler.obtainMessage(3, str).sendToTarget();
        this.mPluginHandler.obtainMessage(2, str).sendToTarget();
    }

    public boolean checkAndDisable(String str) {
        Iterator it = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
        boolean zDisable = false;
        while (it.hasNext()) {
            PluginInfo pluginInfo = (PluginInfo) it.next();
            if (str.startsWith(pluginInfo.mPackage)) {
                zDisable |= disable(pluginInfo, 2);
            }
        }
        return zDisable;
    }

    public boolean disableAll() {
        ArrayList arrayList = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins);
        boolean zDisable = false;
        for (int i = 0; i < arrayList.size(); i++) {
            zDisable |= disable((PluginInfo) arrayList.get(i), 3);
        }
        return zDisable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isPluginWhitelisted(ComponentName componentName) {
        Iterator<String> it = this.mWhitelistedPlugins.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(next);
            if (componentNameUnflattenFromString == null) {
                if (next.equals(componentName.getPackageName())) {
                    return true;
                }
            } else if (componentNameUnflattenFromString.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    private boolean disable(PluginInfo pluginInfo, int i) {
        ComponentName componentName = new ComponentName(pluginInfo.mPackage, pluginInfo.mClass);
        if (isPluginWhitelisted(componentName)) {
            return false;
        }
        Log.w("PluginInstanceManager", "Disabling plugin " + componentName.flattenToShortString());
        this.mManager.getPluginEnabler().setDisabled(componentName, i);
        return true;
    }

    public <T> boolean dependsOn(Plugin plugin, Class<T> cls) {
        Iterator it = new ArrayList(((PluginHandler) this.mPluginHandler).mPlugins).iterator();
        while (it.hasNext()) {
            PluginInfo pluginInfo = (PluginInfo) it.next();
            if (pluginInfo.mPlugin.getClass().getName().equals(plugin.getClass().getName())) {
                return pluginInfo.mVersion != null && pluginInfo.mVersion.hasClass(cls);
            }
        }
        return false;
    }

    public String toString() {
        return String.format("%s@%s (action=%s)", PluginInstanceManager.class.getSimpleName(), Integer.valueOf(hashCode()), this.mAction);
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                PluginPrefs.setHasPlugins(PluginInstanceManager.this.mContext);
                PluginInfo pluginInfo = (PluginInfo) message.obj;
                PluginInstanceManager.this.mManager.handleWtfs();
                if (!(message.obj instanceof PluginFragment)) {
                    ((Plugin) pluginInfo.mPlugin).onCreate(PluginInstanceManager.this.mContext, pluginInfo.mPluginContext);
                }
                PluginInstanceManager.this.mListener.onPluginConnected((Plugin) pluginInfo.mPlugin, pluginInfo.mPluginContext);
                return;
            }
            if (i == 2) {
                PluginInstanceManager.this.mListener.onPluginDisconnected((Plugin) message.obj);
                Object obj = message.obj;
                if (obj instanceof PluginFragment) {
                    return;
                }
                ((Plugin) obj).onDestroy();
                return;
            }
            super.handleMessage(message);
        }
    }

    private class PluginHandler extends Handler {
        private final ArrayList<PluginInfo<T>> mPlugins;

        public PluginHandler(Looper looper) {
            super(looper);
            this.mPlugins = new ArrayList<>();
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                for (int size = this.mPlugins.size() - 1; size >= 0; size--) {
                    PluginInstanceManager.this.mMainHandler.obtainMessage(2, this.mPlugins.get(size).mPlugin).sendToTarget();
                }
                this.mPlugins.clear();
                handleQueryPlugins(null);
                return;
            }
            if (i == 2) {
                String str = (String) message.obj;
                if (PluginInstanceManager.this.mAllowMultiple || this.mPlugins.size() == 0) {
                    handleQueryPlugins(str);
                    return;
                }
                return;
            }
            if (i == 3) {
                String str2 = (String) message.obj;
                for (int size2 = this.mPlugins.size() - 1; size2 >= 0; size2--) {
                    PluginInfo<T> pluginInfo = this.mPlugins.get(size2);
                    if (pluginInfo.mPackage.equals(str2)) {
                        PluginInstanceManager.this.mMainHandler.obtainMessage(2, pluginInfo.mPlugin).sendToTarget();
                        this.mPlugins.remove(size2);
                    }
                }
                return;
            }
            super.handleMessage(message);
        }

        private void handleQueryPlugins(String str) {
            Intent intent = new Intent(PluginInstanceManager.this.mAction);
            if (str != null) {
                intent.setPackage(str);
            }
            List<ResolveInfo> listQueryIntentServices = PluginInstanceManager.this.mPm.queryIntentServices(intent, 0);
            if (listQueryIntentServices.size() > 1 && !PluginInstanceManager.this.mAllowMultiple) {
                Log.w("PluginInstanceManager", "Multiple plugins found for " + PluginInstanceManager.this.mAction);
                return;
            }
            Iterator<ResolveInfo> it = listQueryIntentServices.iterator();
            while (it.hasNext()) {
                ServiceInfo serviceInfo = it.next().serviceInfo;
                PluginInfo<T> pluginInfoHandleLoadPlugin = handleLoadPlugin(new ComponentName(serviceInfo.packageName, serviceInfo.name));
                if (pluginInfoHandleLoadPlugin != null) {
                    this.mPlugins.add(pluginInfoHandleLoadPlugin);
                    PluginInstanceManager.this.mMainHandler.obtainMessage(1, pluginInfoHandleLoadPlugin).sendToTarget();
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        protected PluginInfo<T> handleLoadPlugin(ComponentName componentName) {
            Plugin plugin;
            if (PluginInstanceManager.this.isDebuggable || PluginInstanceManager.this.isPluginWhitelisted(componentName)) {
                if (!PluginInstanceManager.this.mManager.getPluginEnabler().isEnabled(componentName)) {
                    return null;
                }
                String packageName = componentName.getPackageName();
                String className = componentName.getClassName();
                try {
                    ApplicationInfo applicationInfo = PluginInstanceManager.this.mPm.getApplicationInfo(packageName, 0);
                    if (PluginInstanceManager.this.mPm.checkPermission("com.android.systemui.permission.PLUGIN", packageName) == 0) {
                        ClassLoader classLoader = PluginInstanceManager.this.mManager.getClassLoader(applicationInfo);
                        PluginContextWrapper pluginContextWrapper = new PluginContextWrapper(PluginInstanceManager.this.mContext.createApplicationContext(applicationInfo, 0), classLoader);
                        Class<?> cls = Class.forName(className, true, classLoader);
                        Plugin plugin2 = (Plugin) cls.newInstance();
                        try {
                            plugin = plugin2;
                            try {
                                return new PluginInfo<>(packageName, className, plugin2, pluginContextWrapper, checkVersion(cls, plugin2, PluginInstanceManager.this.mVersion));
                            } catch (VersionInfo.InvalidVersionException e) {
                                e = e;
                                Notification.Builder color = new Notification.Builder(PluginInstanceManager.this.mContext, "ALR").setStyle(new Notification.BigTextStyle()).setSmallIcon(Resources.getSystem().getIdentifier("stat_sys_warning", "drawable", "android")).setWhen(0L).setShowWhen(false).setVisibility(1).setColor(PluginInstanceManager.this.mContext.getColor(Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android")));
                                try {
                                    className = PluginInstanceManager.this.mPm.getServiceInfo(componentName, 0).loadLabel(PluginInstanceManager.this.mPm).toString();
                                } catch (PackageManager.NameNotFoundException unused) {
                                }
                                if (!e.isTooNew()) {
                                    Notification.Builder contentTitle = color.setContentTitle("Plugin \"" + className + "\" is too old");
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Contact plugin developer to get an updated version.\n");
                                    sb.append(e.getMessage());
                                    contentTitle.setContentText(sb.toString());
                                } else {
                                    Notification.Builder contentTitle2 = color.setContentTitle("Plugin \"" + className + "\" is too new");
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("Check to see if an OTA is available.\n");
                                    sb2.append(e.getMessage());
                                    contentTitle2.setContentText(sb2.toString());
                                }
                                color.addAction(new Notification.Action.Builder((Icon) null, "Disable plugin", PendingIntent.getBroadcast(PluginInstanceManager.this.mContext, 0, new Intent("com.android.systemui.action.DISABLE_PLUGIN").setData(Uri.parse("package://" + componentName.flattenToString())), 0)).build());
                                ((NotificationManager) PluginInstanceManager.this.mContext.getSystemService(NotificationManager.class)).notify(6, color.build());
                                Log.w("PluginInstanceManager", "Plugin has invalid interface version " + plugin.getVersion() + ", expected " + PluginInstanceManager.this.mVersion);
                                return null;
                            }
                        } catch (VersionInfo.InvalidVersionException e2) {
                            e = e2;
                            plugin = plugin2;
                        }
                    } else {
                        Log.d("PluginInstanceManager", "Plugin doesn't have permission: " + packageName);
                        return null;
                    }
                } catch (Throwable th) {
                    Log.w("PluginInstanceManager", "Couldn't load plugin: " + packageName, th);
                    return null;
                }
            } else {
                Log.w("PluginInstanceManager", "Plugin cannot be loaded on production build: " + componentName);
                return null;
            }
        }

        private VersionInfo checkVersion(Class<?> cls, T t, VersionInfo versionInfo) throws VersionInfo.InvalidVersionException {
            VersionInfo versionInfoAddClass = new VersionInfo().addClass(cls);
            if (versionInfoAddClass.hasVersionInfo()) {
                versionInfo.checkVersion(versionInfoAddClass);
                return versionInfoAddClass;
            }
            if (t.getVersion() == versionInfo.getDefaultVersion()) {
                return null;
            }
            throw new VersionInfo.InvalidVersionException("Invalid legacy version", false);
        }
    }

    public static class PluginContextWrapper extends ContextWrapper {
        private final ClassLoader mClassLoader;
        private LayoutInflater mInflater;

        public PluginContextWrapper(Context context, ClassLoader classLoader) {
            super(context);
            this.mClassLoader = classLoader;
        }

        @Override // android.content.ContextWrapper, android.content.Context
        public ClassLoader getClassLoader() {
            return this.mClassLoader;
        }

        @Override // android.content.ContextWrapper, android.content.Context
        public Object getSystemService(String str) {
            if ("layout_inflater".equals(str)) {
                if (this.mInflater == null) {
                    this.mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
                }
                return this.mInflater;
            }
            return getBaseContext().getSystemService(str);
        }
    }

    static class PluginInfo<T> {
        private String mClass;
        String mPackage;
        T mPlugin;
        private final Context mPluginContext;
        private final VersionInfo mVersion;

        public PluginInfo(String str, String str2, T t, Context context, VersionInfo versionInfo) {
            this.mPlugin = t;
            this.mClass = str2;
            this.mPackage = str;
            this.mPluginContext = context;
            this.mVersion = versionInfo;
        }
    }
}
