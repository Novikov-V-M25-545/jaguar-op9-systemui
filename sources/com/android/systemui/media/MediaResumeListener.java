package com.android.systemui.media;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaDescription;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.ResumeMediaBrowser;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaResumeListener.kt */
/* loaded from: classes.dex */
public final class MediaResumeListener implements MediaDataManager.Listener {
    private final Executor backgroundExecutor;
    private Set<String> blockedApps;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private int currentUserId;
    private ResumeMediaBrowser mediaBrowser;
    private final MediaResumeListener$mediaBrowserCallback$1 mediaBrowserCallback;
    private final ResumeMediaBrowserFactory mediaBrowserFactory;
    private MediaDataManager mediaDataManager;
    private final ConcurrentLinkedQueue<ComponentName> resumeComponents;
    private final TunerService tunerService;
    private boolean useMediaResumption;

    @NotNull
    private final BroadcastReceiver userChangeReceiver;

    @VisibleForTesting
    public static /* synthetic */ void userChangeReceiver$annotations() {
    }

    /* JADX WARN: Type inference failed for: r4v4, types: [com.android.systemui.media.MediaResumeListener$mediaBrowserCallback$1] */
    public MediaResumeListener(@NotNull Context context, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull Executor backgroundExecutor, @NotNull TunerService tunerService, @NotNull ResumeMediaBrowserFactory mediaBrowserFactory) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(tunerService, "tunerService");
        Intrinsics.checkParameterIsNotNull(mediaBrowserFactory, "mediaBrowserFactory");
        this.context = context;
        this.broadcastDispatcher = broadcastDispatcher;
        this.backgroundExecutor = backgroundExecutor;
        this.tunerService = tunerService;
        this.mediaBrowserFactory = mediaBrowserFactory;
        this.useMediaResumption = Utils.useMediaResumption(context);
        this.resumeComponents = new ConcurrentLinkedQueue<>();
        Set<String> blockedMediaApps = Utils.getBlockedMediaApps(context);
        Intrinsics.checkExpressionValueIsNotNull(blockedMediaApps, "Utils.getBlockedMediaApps(context)");
        this.blockedApps = blockedMediaApps;
        this.currentUserId = context.getUserId();
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.media.MediaResumeListener$userChangeReceiver$1
            @Override // android.content.BroadcastReceiver
            public void onReceive(@NotNull Context context2, @NotNull Intent intent) {
                Intrinsics.checkParameterIsNotNull(context2, "context");
                Intrinsics.checkParameterIsNotNull(intent, "intent");
                if (Intrinsics.areEqual("android.intent.action.USER_UNLOCKED", intent.getAction())) {
                    this.this$0.loadMediaResumptionControls();
                } else if (Intrinsics.areEqual("android.intent.action.USER_SWITCHED", intent.getAction())) {
                    this.this$0.currentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    this.this$0.loadSavedComponents();
                }
            }
        };
        this.userChangeReceiver = broadcastReceiver;
        this.mediaBrowserCallback = new ResumeMediaBrowser.Callback() { // from class: com.android.systemui.media.MediaResumeListener$mediaBrowserCallback$1
            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void addTrack(@NotNull MediaDescription desc, @NotNull ComponentName component, @NotNull ResumeMediaBrowser browser) {
                Intrinsics.checkParameterIsNotNull(desc, "desc");
                Intrinsics.checkParameterIsNotNull(component, "component");
                Intrinsics.checkParameterIsNotNull(browser, "browser");
                MediaSession.Token token = browser.getToken();
                PendingIntent appIntent = browser.getAppIntent();
                PackageManager packageManager = this.this$0.context.getPackageManager();
                Object packageName = component.getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName, "component.packageName");
                Runnable resumeAction = this.this$0.getResumeAction(component);
                try {
                    Object applicationLabel = packageManager.getApplicationLabel(packageManager.getApplicationInfo(component.getPackageName(), 0));
                    Intrinsics.checkExpressionValueIsNotNull(applicationLabel, "pm.getApplicationLabel(\n…omponent.packageName, 0))");
                    packageName = applicationLabel;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("MediaResumeListener", "Error getting package information", e);
                }
                Log.d("MediaResumeListener", "Adding resume controls for " + browser.getUserId() + ": " + desc);
                MediaDataManager mediaDataManagerAccess$getMediaDataManager$p = MediaResumeListener.access$getMediaDataManager$p(this.this$0);
                int userId = browser.getUserId();
                Intrinsics.checkExpressionValueIsNotNull(token, "token");
                String string = packageName.toString();
                Intrinsics.checkExpressionValueIsNotNull(appIntent, "appIntent");
                String packageName2 = component.getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName2, "component.packageName");
                mediaDataManagerAccess$getMediaDataManager$p.addResumptionControls(userId, desc, resumeAction, token, string, appIntent, packageName2);
            }
        };
        if (this.useMediaResumption) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            UserHandle userHandle = UserHandle.ALL;
            Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
            broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter, null, userHandle);
            loadSavedComponents();
        }
    }

    public static final /* synthetic */ MediaDataManager access$getMediaDataManager$p(MediaResumeListener mediaResumeListener) {
        MediaDataManager mediaDataManager = mediaResumeListener.mediaDataManager;
        if (mediaDataManager == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
        }
        return mediaDataManager;
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        MediaDataManager.Listener.DefaultImpls.onMediaDataRemoved(this, key);
    }

    public final void setManager(@NotNull MediaDataManager manager) {
        Intrinsics.checkParameterIsNotNull(manager, "manager");
        this.mediaDataManager = manager;
        this.tunerService.addTunable(new TunerService.Tunable() { // from class: com.android.systemui.media.MediaResumeListener.setManager.1
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(@Nullable String str, @Nullable String str2) {
                MediaResumeListener mediaResumeListener = MediaResumeListener.this;
                mediaResumeListener.useMediaResumption = Utils.useMediaResumption(mediaResumeListener.context);
                MediaResumeListener.access$getMediaDataManager$p(MediaResumeListener.this).setMediaResumptionEnabled(MediaResumeListener.this.useMediaResumption);
            }
        }, "qs_media_resumption");
        this.tunerService.addTunable(new TunerService.Tunable() { // from class: com.android.systemui.media.MediaResumeListener.setManager.2
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(@Nullable String str, @Nullable String str2) {
                MediaResumeListener mediaResumeListener = MediaResumeListener.this;
                Set<String> blockedMediaApps = Utils.getBlockedMediaApps(mediaResumeListener.context);
                Intrinsics.checkExpressionValueIsNotNull(blockedMediaApps, "Utils.getBlockedMediaApps(context)");
                mediaResumeListener.blockedApps = blockedMediaApps;
                MediaResumeListener.access$getMediaDataManager$p(MediaResumeListener.this).setAppsBlockedFromResume$frameworks__base__packages__SystemUI__android_common__SystemUI_core(MediaResumeListener.this.blockedApps);
            }
        }, "qs_media_resumption_blocked");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void loadSavedComponents() {
        List<String> listSplit;
        this.resumeComponents.clear();
        List listEmptyList = null;
        String string = this.context.getSharedPreferences("media_control_prefs", 0).getString("browser_components_" + this.currentUserId, null);
        if (string != null && (listSplit = new Regex(":").split(string, 0)) != null) {
            if (!listSplit.isEmpty()) {
                ListIterator<String> listIterator = listSplit.listIterator(listSplit.size());
                while (listIterator.hasPrevious()) {
                    if (!(listIterator.previous().length() == 0)) {
                        listEmptyList = CollectionsKt___CollectionsKt.take(listSplit, listIterator.nextIndex() + 1);
                        break;
                    }
                }
                listEmptyList = CollectionsKt__CollectionsKt.emptyList();
            } else {
                listEmptyList = CollectionsKt__CollectionsKt.emptyList();
            }
        }
        if (listEmptyList != null) {
            Iterator it = listEmptyList.iterator();
            while (it.hasNext()) {
                List listSplit$default = StringsKt__StringsKt.split$default((String) it.next(), new String[]{"/"}, false, 0, 6, null);
                this.resumeComponents.add(new ComponentName((String) listSplit$default.get(0), (String) listSplit$default.get(1)));
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("loaded resume components for ");
        sb.append(this.currentUserId);
        sb.append(": ");
        Object[] array = this.resumeComponents.toArray();
        Intrinsics.checkExpressionValueIsNotNull(array, "resumeComponents.toArray()");
        String string2 = Arrays.toString(array);
        Intrinsics.checkExpressionValueIsNotNull(string2, "java.util.Arrays.toString(this)");
        sb.append(string2);
        Log.d("MediaResumeListener", sb.toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void loadMediaResumptionControls() {
        if (this.useMediaResumption) {
            PackageManager packageManager = this.context.getPackageManager();
            for (ComponentName componentName : this.resumeComponents) {
                Intent intent = new Intent("android.media.browse.MediaBrowserService");
                intent.setComponent(componentName);
                if (packageManager.resolveServiceAsUser(intent, 0, this.currentUserId) != null) {
                    this.mediaBrowserFactory.create(this.mediaBrowserCallback, componentName, this.currentUserId).findRecentMedia();
                } else {
                    Log.d("MediaResumeListener", "User " + this.currentUserId + " does not have component " + componentName);
                }
            }
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull final String key, @Nullable String str, @NotNull MediaData data) {
        final ArrayList arrayList;
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        if (this.useMediaResumption) {
            ResumeMediaBrowser resumeMediaBrowser = this.mediaBrowser;
            if (resumeMediaBrowser != null) {
                resumeMediaBrowser.disconnect();
            }
            if (data.getResumeAction() != null || data.getHasCheckedForResume() || this.blockedApps.contains(data.getPackageName())) {
                return;
            }
            Log.d("MediaResumeListener", "Checking for service component for " + data.getPackageName());
            List listQueryIntentServicesAsUser = this.context.getPackageManager().queryIntentServicesAsUser(new Intent("android.media.browse.MediaBrowserService"), 0, this.currentUserId);
            if (listQueryIntentServicesAsUser != null) {
                arrayList = new ArrayList();
                for (Object obj : listQueryIntentServicesAsUser) {
                    if (Intrinsics.areEqual(((ResolveInfo) obj).serviceInfo.packageName, data.getPackageName())) {
                        arrayList.add(obj);
                    }
                }
            } else {
                arrayList = null;
            }
            if (arrayList != null && arrayList.size() > 0) {
                this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaResumeListener.onMediaDataLoaded.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        MediaResumeListener mediaResumeListener = MediaResumeListener.this;
                        String str2 = key;
                        List list = arrayList;
                        if (list == null) {
                            Intrinsics.throwNpe();
                        }
                        Object obj2 = list.get(0);
                        Intrinsics.checkExpressionValueIsNotNull(obj2, "inf!!.get(0)");
                        ComponentInfo componentInfo = ((ResolveInfo) obj2).getComponentInfo();
                        Intrinsics.checkExpressionValueIsNotNull(componentInfo, "inf!!.get(0).componentInfo");
                        ComponentName componentName = componentInfo.getComponentName();
                        Intrinsics.checkExpressionValueIsNotNull(componentName, "inf!!.get(0).componentInfo.componentName");
                        mediaResumeListener.tryUpdateResumptionList(str2, componentName);
                    }
                });
                return;
            }
            MediaDataManager mediaDataManager = this.mediaDataManager;
            if (mediaDataManager == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
            }
            mediaDataManager.setResumeAction(key, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void tryUpdateResumptionList(final String str, final ComponentName componentName) {
        Log.d("MediaResumeListener", "Testing if we can connect to " + componentName);
        ResumeMediaBrowser resumeMediaBrowser = this.mediaBrowser;
        if (resumeMediaBrowser != null) {
            resumeMediaBrowser.disconnect();
        }
        ResumeMediaBrowser resumeMediaBrowserCreate = this.mediaBrowserFactory.create(new ResumeMediaBrowser.Callback() { // from class: com.android.systemui.media.MediaResumeListener.tryUpdateResumptionList.1
            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void onConnected() {
                Log.d("MediaResumeListener", "Connected to " + componentName);
            }

            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void onError() {
                Log.e("MediaResumeListener", "Cannot resume with " + componentName);
                MediaResumeListener.access$getMediaDataManager$p(MediaResumeListener.this).setResumeAction(str, null);
                ResumeMediaBrowser resumeMediaBrowser2 = MediaResumeListener.this.mediaBrowser;
                if (resumeMediaBrowser2 != null) {
                    resumeMediaBrowser2.disconnect();
                }
                MediaResumeListener.this.mediaBrowser = null;
            }

            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void addTrack(@NotNull MediaDescription desc, @NotNull ComponentName component, @NotNull ResumeMediaBrowser browser) {
                Intrinsics.checkParameterIsNotNull(desc, "desc");
                Intrinsics.checkParameterIsNotNull(component, "component");
                Intrinsics.checkParameterIsNotNull(browser, "browser");
                Log.d("MediaResumeListener", "Can get resumable media from " + componentName);
                MediaResumeListener.access$getMediaDataManager$p(MediaResumeListener.this).setResumeAction(str, MediaResumeListener.this.getResumeAction(componentName));
                MediaResumeListener.this.updateResumptionList(componentName);
                ResumeMediaBrowser resumeMediaBrowser2 = MediaResumeListener.this.mediaBrowser;
                if (resumeMediaBrowser2 != null) {
                    resumeMediaBrowser2.disconnect();
                }
                MediaResumeListener.this.mediaBrowser = null;
            }
        }, componentName, this.currentUserId);
        this.mediaBrowser = resumeMediaBrowserCreate;
        if (resumeMediaBrowserCreate != null) {
            resumeMediaBrowserCreate.testConnection();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateResumptionList(ComponentName componentName) {
        this.resumeComponents.remove(componentName);
        this.resumeComponents.add(componentName);
        if (this.resumeComponents.size() > 5) {
            this.resumeComponents.remove();
        }
        StringBuilder sb = new StringBuilder();
        Iterator<T> it = this.resumeComponents.iterator();
        while (it.hasNext()) {
            sb.append(((ComponentName) it.next()).flattenToString());
            sb.append(":");
        }
        this.context.getSharedPreferences("media_control_prefs", 0).edit().putString("browser_components_" + this.currentUserId, sb.toString()).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Runnable getResumeAction(final ComponentName componentName) {
        return new Runnable() { // from class: com.android.systemui.media.MediaResumeListener.getResumeAction.1
            @Override // java.lang.Runnable
            public final void run() {
                ResumeMediaBrowser resumeMediaBrowser = MediaResumeListener.this.mediaBrowser;
                if (resumeMediaBrowser != null) {
                    resumeMediaBrowser.disconnect();
                }
                MediaResumeListener mediaResumeListener = MediaResumeListener.this;
                mediaResumeListener.mediaBrowser = mediaResumeListener.mediaBrowserFactory.create(new ResumeMediaBrowser.Callback() { // from class: com.android.systemui.media.MediaResumeListener.getResumeAction.1.1
                    @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
                    public void onConnected() {
                        ResumeMediaBrowser resumeMediaBrowser2 = MediaResumeListener.this.mediaBrowser;
                        if ((resumeMediaBrowser2 != null ? resumeMediaBrowser2.getToken() : null) == null) {
                            Log.e("MediaResumeListener", "Error after connect");
                            ResumeMediaBrowser resumeMediaBrowser3 = MediaResumeListener.this.mediaBrowser;
                            if (resumeMediaBrowser3 != null) {
                                resumeMediaBrowser3.disconnect();
                            }
                            MediaResumeListener.this.mediaBrowser = null;
                            return;
                        }
                        Log.d("MediaResumeListener", "Connected for restart " + componentName);
                        Context context = MediaResumeListener.this.context;
                        ResumeMediaBrowser resumeMediaBrowser4 = MediaResumeListener.this.mediaBrowser;
                        if (resumeMediaBrowser4 == null) {
                            Intrinsics.throwNpe();
                        }
                        MediaController.TransportControls transportControls = new MediaController(context, resumeMediaBrowser4.getToken()).getTransportControls();
                        transportControls.prepare();
                        transportControls.play();
                    }

                    @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
                    public void onError() {
                        Log.e("MediaResumeListener", "Resume failed for " + componentName);
                        ResumeMediaBrowser resumeMediaBrowser2 = MediaResumeListener.this.mediaBrowser;
                        if (resumeMediaBrowser2 != null) {
                            resumeMediaBrowser2.disconnect();
                        }
                        MediaResumeListener.this.mediaBrowser = null;
                    }
                }, componentName, MediaResumeListener.this.currentUserId);
                ResumeMediaBrowser resumeMediaBrowser2 = MediaResumeListener.this.mediaBrowser;
                if (resumeMediaBrowser2 != null) {
                    resumeMediaBrowser2.restart();
                }
            }
        };
    }
}
