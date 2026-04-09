package com.android.systemui.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.UriGrantsManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import androidx.palette.graphics.Palette;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.MediaNotificationProcessor;
import com.android.systemui.statusbar.notification.row.HybridGroupManager;
import com.android.systemui.util.Assert;
import com.android.systemui.util.Utils;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import kotlin.text.StringsKt__StringsJVMKt;
import lineageos.hardware.LineageHardwareManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataManager.kt */
/* loaded from: classes.dex */
public final class MediaDataManager implements Dumpable {
    private final MediaDataManager$appChangeReceiver$1 appChangeReceiver;

    @NotNull
    private Set<String> appsBlockedFromResume;
    private final Executor backgroundExecutor;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private final DelayableExecutor foregroundExecutor;
    private final Set<Listener> internalListeners;
    private final MediaControllerFactory mediaControllerFactory;
    private final MediaDataFilter mediaDataFilter;
    private final LinkedHashMap<String, MediaData> mediaEntries;
    private boolean useMediaResumption;
    private final boolean useQsMediaPlayer;

    /* compiled from: MediaDataManager.kt */
    public interface Listener {

        /* compiled from: MediaDataManager.kt */
        public static final class DefaultImpls {
            public static void onMediaDataRemoved(Listener listener, @NotNull String key) {
                Intrinsics.checkParameterIsNotNull(key, "key");
            }
        }

        void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData);

        void onMediaDataRemoved(@NotNull String str);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v4, types: [android.content.BroadcastReceiver, com.android.systemui.media.MediaDataManager$appChangeReceiver$1] */
    public MediaDataManager(@NotNull Context context, @NotNull Executor backgroundExecutor, @NotNull DelayableExecutor foregroundExecutor, @NotNull MediaControllerFactory mediaControllerFactory, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull DumpManager dumpManager, @NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull MediaResumeListener mediaResumeListener, @NotNull MediaSessionBasedFilter mediaSessionBasedFilter, @NotNull MediaDeviceManager mediaDeviceManager, @NotNull MediaDataCombineLatest mediaDataCombineLatest, @NotNull MediaDataFilter mediaDataFilter, boolean z, boolean z2) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(foregroundExecutor, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
        Intrinsics.checkParameterIsNotNull(mediaSessionBasedFilter, "mediaSessionBasedFilter");
        Intrinsics.checkParameterIsNotNull(mediaDeviceManager, "mediaDeviceManager");
        Intrinsics.checkParameterIsNotNull(mediaDataCombineLatest, "mediaDataCombineLatest");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter, "mediaDataFilter");
        this.context = context;
        this.backgroundExecutor = backgroundExecutor;
        this.foregroundExecutor = foregroundExecutor;
        this.mediaControllerFactory = mediaControllerFactory;
        this.broadcastDispatcher = broadcastDispatcher;
        this.mediaDataFilter = mediaDataFilter;
        this.useMediaResumption = z;
        this.useQsMediaPlayer = z2;
        this.internalListeners = new LinkedHashSet();
        this.mediaEntries = new LinkedHashMap<>();
        Set<String> blockedMediaApps = Utils.getBlockedMediaApps(context);
        Intrinsics.checkExpressionValueIsNotNull(blockedMediaApps, "Utils.getBlockedMediaApps(context)");
        this.appsBlockedFromResume = blockedMediaApps;
        ?? r3 = new BroadcastReceiver() { // from class: com.android.systemui.media.MediaDataManager$appChangeReceiver$1
            @Override // android.content.BroadcastReceiver
            public void onReceive(@NotNull Context context2, @NotNull Intent intent) {
                String[] stringArrayExtra;
                String encodedSchemeSpecificPart;
                Intrinsics.checkParameterIsNotNull(context2, "context");
                Intrinsics.checkParameterIsNotNull(intent, "intent");
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                int iHashCode = action.hashCode();
                if (iHashCode == -1001645458) {
                    if (!action.equals("android.intent.action.PACKAGES_SUSPENDED") || (stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list")) == null) {
                        return;
                    }
                    for (String it : stringArrayExtra) {
                        MediaDataManager mediaDataManager = this.this$0;
                        Intrinsics.checkExpressionValueIsNotNull(it, "it");
                        mediaDataManager.removeAllForPackage(it);
                    }
                    return;
                }
                if (iHashCode != -757780528) {
                    if (iHashCode != 525384130 || !action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        return;
                    }
                } else if (!action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                    return;
                }
                Uri data = intent.getData();
                if (data == null || (encodedSchemeSpecificPart = data.getEncodedSchemeSpecificPart()) == null) {
                    return;
                }
                this.this$0.removeAllForPackage(encodedSchemeSpecificPart);
            }
        };
        this.appChangeReceiver = r3;
        dumpManager.registerDumpable("MediaDataManager", this);
        addInternalListener(mediaTimeoutListener);
        addInternalListener(mediaResumeListener);
        addInternalListener(mediaSessionBasedFilter);
        mediaSessionBasedFilter.addListener(mediaDeviceManager);
        mediaSessionBasedFilter.addListener(mediaDataCombineLatest);
        mediaDeviceManager.addListener(mediaDataCombineLatest);
        mediaDataCombineLatest.addListener(mediaDataFilter);
        mediaTimeoutListener.setTimeoutCallback(new Function2<String, Boolean, Unit>() { // from class: com.android.systemui.media.MediaDataManager.1
            {
                super(2);
            }

            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(String str, Boolean bool) {
                invoke(str, bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull String token, boolean z3) {
                Intrinsics.checkParameterIsNotNull(token, "token");
                MediaDataManager.this.setTimedOut$frameworks__base__packages__SystemUI__android_common__SystemUI_core(token, z3);
            }
        });
        mediaResumeListener.setManager(this);
        mediaDataFilter.setMediaDataManager$frameworks__base__packages__SystemUI__android_common__SystemUI_core(this);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGES_SUSPENDED");
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(r3, intentFilter, null, userHandle);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addAction("android.intent.action.PACKAGE_RESTARTED");
        intentFilter2.addDataScheme("package");
        context.registerReceiver(r3, intentFilter2);
    }

    public final void setAppsBlockedFromResume$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull Set<String> value) {
        Intrinsics.checkParameterIsNotNull(value, "value");
        this.appsBlockedFromResume.clear();
        this.appsBlockedFromResume.addAll(value);
        Iterator<T> it = this.appsBlockedFromResume.iterator();
        while (it.hasNext()) {
            removeAllForPackage((String) it.next());
        }
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public MediaDataManager(@NotNull Context context, @NotNull Executor backgroundExecutor, @NotNull DelayableExecutor foregroundExecutor, @NotNull MediaControllerFactory mediaControllerFactory, @NotNull DumpManager dumpManager, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull MediaResumeListener mediaResumeListener, @NotNull MediaSessionBasedFilter mediaSessionBasedFilter, @NotNull MediaDeviceManager mediaDeviceManager, @NotNull MediaDataCombineLatest mediaDataCombineLatest, @NotNull MediaDataFilter mediaDataFilter) {
        this(context, backgroundExecutor, foregroundExecutor, mediaControllerFactory, broadcastDispatcher, dumpManager, mediaTimeoutListener, mediaResumeListener, mediaSessionBasedFilter, mediaDeviceManager, mediaDataCombineLatest, mediaDataFilter, Utils.useMediaResumption(context), Utils.useQsMediaPlayer(context));
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(foregroundExecutor, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
        Intrinsics.checkParameterIsNotNull(mediaSessionBasedFilter, "mediaSessionBasedFilter");
        Intrinsics.checkParameterIsNotNull(mediaDeviceManager, "mediaDeviceManager");
        Intrinsics.checkParameterIsNotNull(mediaDataCombineLatest, "mediaDataCombineLatest");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter, "mediaDataFilter");
    }

    public final void onNotificationAdded(@NotNull String key, @NotNull StatusBarNotification sbn) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(sbn, "sbn");
        if (this.useQsMediaPlayer && MediaDataManagerKt.isMediaNotification(sbn)) {
            Assert.isMainThread();
            String packageName = sbn.getPackageName();
            Intrinsics.checkExpressionValueIsNotNull(packageName, "sbn.packageName");
            String strFindExistingEntry = findExistingEntry(key, packageName);
            if (strFindExistingEntry == null) {
                MediaData mediaData = MediaDataManagerKt.LOADING;
                String packageName2 = sbn.getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName2, "sbn.packageName");
                this.mediaEntries.put(key, mediaData.copy((3636223 & 1) != 0 ? mediaData.userId : 0, (3636223 & 2) != 0 ? mediaData.initialized : false, (3636223 & 4) != 0 ? mediaData.backgroundColor : 0, (3636223 & 8) != 0 ? mediaData.app : null, (3636223 & 16) != 0 ? mediaData.appIcon : null, (3636223 & 32) != 0 ? mediaData.artist : null, (3636223 & 64) != 0 ? mediaData.song : null, (3636223 & 128) != 0 ? mediaData.artwork : null, (3636223 & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) != 0 ? mediaData.actions : null, (3636223 & 512) != 0 ? mediaData.actionsToShowInCompact : null, (3636223 & LineageHardwareManager.FEATURE_VIBRATOR) != 0 ? mediaData.packageName : packageName2, (3636223 & LineageHardwareManager.FEATURE_TOUCH_HOVERING) != 0 ? mediaData.token : null, (3636223 & LineageHardwareManager.FEATURE_AUTO_CONTRAST) != 0 ? mediaData.clickIntent : null, (3636223 & LineageHardwareManager.FEATURE_DISPLAY_MODES) != 0 ? mediaData.device : null, (3636223 & LineageHardwareManager.FEATURE_READING_ENHANCEMENT) != 0 ? mediaData.active : false, (3636223 & 32768) != 0 ? mediaData.resumeAction : null, (3636223 & 65536) != 0 ? mediaData.isLocalSession : false, (3636223 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0 ? mediaData.resumption : false, (3636223 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0 ? mediaData.notificationKey : null, (3636223 & LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES) != 0 ? mediaData.hasCheckedForResume : false, (3636223 & 1048576) != 0 ? mediaData.isPlaying : null, (3636223 & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0 ? mediaData.isClearable : false));
            } else if (!Intrinsics.areEqual(strFindExistingEntry, key)) {
                MediaData mediaDataRemove = this.mediaEntries.remove(strFindExistingEntry);
                if (mediaDataRemove == null) {
                    Intrinsics.throwNpe();
                }
                Intrinsics.checkExpressionValueIsNotNull(mediaDataRemove, "mediaEntries.remove(oldKey)!!");
                this.mediaEntries.put(key, mediaDataRemove);
            }
            loadMediaData(key, sbn, strFindExistingEntry);
            return;
        }
        onNotificationRemoved(key);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void removeAllForPackage(String str) {
        Assert.isMainThread();
        LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        for (Map.Entry<String, MediaData> entry : linkedHashMap.entrySet()) {
            if (Intrinsics.areEqual(entry.getValue().getPackageName(), str)) {
                linkedHashMap2.put(entry.getKey(), entry.getValue());
            }
        }
        Iterator it = linkedHashMap2.entrySet().iterator();
        while (it.hasNext()) {
            removeEntry((String) ((Map.Entry) it.next()).getKey());
        }
    }

    public final void setResumeAction(@NotNull String key, @Nullable Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        MediaData mediaData = this.mediaEntries.get(key);
        if (mediaData != null) {
            mediaData.setResumeAction(runnable);
            mediaData.setHasCheckedForResume(true);
        }
    }

    public final void addResumptionControls(final int i, @NotNull final MediaDescription desc, @NotNull final Runnable action, @NotNull final MediaSession.Token token, @NotNull final String appName, @NotNull final PendingIntent appIntent, @NotNull final String packageName) {
        Intrinsics.checkParameterIsNotNull(desc, "desc");
        Intrinsics.checkParameterIsNotNull(action, "action");
        Intrinsics.checkParameterIsNotNull(token, "token");
        Intrinsics.checkParameterIsNotNull(appName, "appName");
        Intrinsics.checkParameterIsNotNull(appIntent, "appIntent");
        Intrinsics.checkParameterIsNotNull(packageName, "packageName");
        if (!this.mediaEntries.containsKey(packageName)) {
            MediaData mediaData = MediaDataManagerKt.LOADING;
            this.mediaEntries.put(packageName, mediaData.copy((3636223 & 1) != 0 ? mediaData.userId : 0, (3636223 & 2) != 0 ? mediaData.initialized : false, (3636223 & 4) != 0 ? mediaData.backgroundColor : 0, (3636223 & 8) != 0 ? mediaData.app : null, (3636223 & 16) != 0 ? mediaData.appIcon : null, (3636223 & 32) != 0 ? mediaData.artist : null, (3636223 & 64) != 0 ? mediaData.song : null, (3636223 & 128) != 0 ? mediaData.artwork : null, (3636223 & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) != 0 ? mediaData.actions : null, (3636223 & 512) != 0 ? mediaData.actionsToShowInCompact : null, (3636223 & LineageHardwareManager.FEATURE_VIBRATOR) != 0 ? mediaData.packageName : packageName, (3636223 & LineageHardwareManager.FEATURE_TOUCH_HOVERING) != 0 ? mediaData.token : null, (3636223 & LineageHardwareManager.FEATURE_AUTO_CONTRAST) != 0 ? mediaData.clickIntent : null, (3636223 & LineageHardwareManager.FEATURE_DISPLAY_MODES) != 0 ? mediaData.device : null, (3636223 & LineageHardwareManager.FEATURE_READING_ENHANCEMENT) != 0 ? mediaData.active : false, (3636223 & 32768) != 0 ? mediaData.resumeAction : action, (3636223 & 65536) != 0 ? mediaData.isLocalSession : false, (3636223 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0 ? mediaData.resumption : false, (3636223 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0 ? mediaData.notificationKey : null, (3636223 & LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES) != 0 ? mediaData.hasCheckedForResume : true, (3636223 & 1048576) != 0 ? mediaData.isPlaying : null, (3636223 & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0 ? mediaData.isClearable : false));
        }
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDataManager.addResumptionControls.1
            @Override // java.lang.Runnable
            public final void run() throws PackageManager.NameNotFoundException {
                MediaDataManager.this.loadMediaDataInBgForResumption(i, desc, action, token, appName, appIntent, packageName);
            }
        });
    }

    private final String findExistingEntry(String str, String str2) {
        if (this.mediaEntries.containsKey(str)) {
            return str;
        }
        if (this.mediaEntries.containsKey(str2)) {
            return str2;
        }
        return null;
    }

    private final void loadMediaData(final String str, final StatusBarNotification statusBarNotification, final String str2) {
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDataManager.loadMediaData.1
            @Override // java.lang.Runnable
            public final void run() {
                MediaDataManager.this.loadMediaDataInBg(str, statusBarNotification, str2);
            }
        });
    }

    public final void addListener(@NotNull Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.mediaDataFilter.addListener(listener);
    }

    public final void removeListener(@NotNull Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        this.mediaDataFilter.removeListener(listener);
    }

    private final boolean addInternalListener(Listener listener) {
        return this.internalListeners.add(listener);
    }

    private final void notifyMediaDataLoaded(String str, String str2, MediaData mediaData) {
        Iterator<T> it = this.internalListeners.iterator();
        while (it.hasNext()) {
            ((Listener) it.next()).onMediaDataLoaded(str, str2, mediaData);
        }
    }

    private final void notifyMediaDataRemoved(String str) {
        Iterator<T> it = this.internalListeners.iterator();
        while (it.hasNext()) {
            ((Listener) it.next()).onMediaDataRemoved(str);
        }
    }

    public final void setTimedOut$frameworks__base__packages__SystemUI__android_common__SystemUI_core(@NotNull String token, boolean z) {
        Intrinsics.checkParameterIsNotNull(token, "token");
        MediaData it = this.mediaEntries.get(token);
        if (it == null || it.getActive() == (!z)) {
            return;
        }
        it.setActive(!z);
        Log.d("MediaDataManager", "Updating " + token + " timedOut: " + z);
        Intrinsics.checkExpressionValueIsNotNull(it, "it");
        onMediaDataLoaded(token, token, it);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void removeEntry(String str) {
        this.mediaEntries.remove(str);
        notifyMediaDataRemoved(str);
    }

    public final void dismissMediaData(@NotNull final String key, long j) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDataManager.dismissMediaData.1
            @Override // java.lang.Runnable
            public final void run() {
                MediaSession.Token token;
                MediaData mediaData = (MediaData) MediaDataManager.this.mediaEntries.get(key);
                if (mediaData == null || !mediaData.isLocalSession() || (token = mediaData.getToken()) == null) {
                    return;
                }
                MediaController mediaController = MediaDataManager.this.mediaControllerFactory.create(token);
                Intrinsics.checkExpressionValueIsNotNull(mediaController, "mediaController");
                mediaController.getTransportControls().stop();
            }
        });
        this.foregroundExecutor.executeDelayed(new Runnable() { // from class: com.android.systemui.media.MediaDataManager.dismissMediaData.2
            @Override // java.lang.Runnable
            public final void run() {
                MediaDataManager.this.removeEntry(key);
            }
        }, j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void loadMediaDataInBgForResumption(final int i, final MediaDescription mediaDescription, final Runnable runnable, final MediaSession.Token token, final String str, final PendingIntent pendingIntent, final String str2) throws PackageManager.NameNotFoundException {
        int iIntValue;
        if (TextUtils.isEmpty(mediaDescription.getTitle())) {
            Log.e("MediaDataManager", "Description incomplete");
            this.mediaEntries.remove(str2);
            return;
        }
        Log.d("MediaDataManager", "adding track for " + i + " from browser: " + mediaDescription);
        Bitmap iconBitmap = mediaDescription.getIconBitmap();
        if (iconBitmap == null && mediaDescription.getIconUri() != null) {
            try {
                ApplicationInfo applicationInfo = this.context.getPackageManager().getApplicationInfo(str2, 0);
                Integer numValueOf = applicationInfo != null ? Integer.valueOf(applicationInfo.uid) : null;
                if (numValueOf == null) {
                    Intrinsics.throwNpe();
                }
                iIntValue = numValueOf.intValue();
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("MediaDataManager", "Could not get app UID for " + str2, e);
                iIntValue = -1;
            }
            Uri iconUri = mediaDescription.getIconUri();
            if (iconUri == null) {
                Intrinsics.throwNpe();
            }
            iconBitmap = loadBitmapFromUriForUser(iconUri, i, iIntValue, str2);
        }
        final Icon iconCreateWithBitmap = iconBitmap != null ? Icon.createWithBitmap(iconBitmap) : null;
        final int iComputeBackgroundColor = iconBitmap != null ? computeBackgroundColor(iconBitmap) : -12303292;
        final MediaAction resumeMediaAction = getResumeMediaAction(runnable);
        this.foregroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDataManager.loadMediaDataInBgForResumption.1
            @Override // java.lang.Runnable
            public final void run() {
                MediaDataManager mediaDataManager = MediaDataManager.this;
                String str3 = str2;
                int i2 = i;
                int i3 = iComputeBackgroundColor;
                String str4 = str;
                CharSequence subtitle = mediaDescription.getSubtitle();
                CharSequence title = mediaDescription.getTitle();
                Icon icon = iconCreateWithBitmap;
                List listListOf = CollectionsKt__CollectionsJVMKt.listOf(resumeMediaAction);
                List listListOf2 = CollectionsKt__CollectionsJVMKt.listOf(0);
                String str5 = str2;
                mediaDataManager.onMediaDataLoaded(str3, null, new MediaData(i2, true, i3, str4, null, subtitle, title, icon, listListOf, listListOf2, str5, token, pendingIntent, null, false, runnable, false, true, str5, true, null, false, 3211264, null));
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v22, types: [T, java.lang.CharSequence] */
    /* JADX WARN: Type inference failed for: r3v28, types: [T, java.lang.String] */
    /* JADX WARN: Type inference failed for: r3v31, types: [T, java.lang.CharSequence] */
    public final void loadMediaDataInBg(final String str, final StatusBarNotification statusBarNotification, final String str2) {
        Icon iconCreateWithBitmap;
        List arrayList;
        Notification.Action[] actionArr;
        Notification notification;
        List list;
        final MediaSession.Token token = (MediaSession.Token) statusBarNotification.getNotification().extras.getParcelable("android.mediaSession");
        MediaController mediaController = this.mediaControllerFactory.create(token);
        Intrinsics.checkExpressionValueIsNotNull(mediaController, "mediaController");
        MediaMetadata metadata = mediaController.getMetadata();
        Notification notification2 = statusBarNotification.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification2, "sbn.notification");
        Bitmap bitmap = metadata != null ? metadata.getBitmap("android.media.metadata.ART") : null;
        if (bitmap == null) {
            bitmap = metadata != null ? metadata.getBitmap("android.media.metadata.ALBUM_ART") : null;
        }
        if (bitmap == null && metadata != null) {
            bitmap = loadBitmapFromUri(metadata);
        }
        if (bitmap == null) {
            iconCreateWithBitmap = notification2.getLargeIcon();
        } else {
            iconCreateWithBitmap = Icon.createWithBitmap(bitmap);
        }
        final Icon icon = iconCreateWithBitmap;
        if (icon != null && bitmap == null) {
            if (icon.getType() == 1 || icon.getType() == 5) {
                bitmap = icon.getBitmap();
            } else {
                Context context = this.context;
                UserHandle user = statusBarNotification.getUser();
                Intrinsics.checkExpressionValueIsNotNull(user, "sbn.user");
                Drawable drawableLoadDrawableAsUser = icon.loadDrawableAsUser(context, user.getIdentifier());
                Intrinsics.checkExpressionValueIsNotNull(drawableLoadDrawableAsUser, "artWorkIcon.loadDrawable…     sbn.user.identifier)");
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(drawableLoadDrawableAsUser.getIntrinsicWidth(), drawableLoadDrawableAsUser.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapCreateBitmap);
                drawableLoadDrawableAsUser.setBounds(0, 0, drawableLoadDrawableAsUser.getIntrinsicWidth(), drawableLoadDrawableAsUser.getIntrinsicHeight());
                drawableLoadDrawableAsUser.draw(canvas);
                bitmap = bitmapCreateBitmap;
            }
        }
        final int iComputeBackgroundColor = computeBackgroundColor(bitmap);
        final String strLoadHeaderAppName = Notification.Builder.recoverBuilder(this.context, notification2).loadHeaderAppName();
        Notification notification3 = statusBarNotification.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification3, "sbn.notification");
        Icon smallIcon = notification3.getSmallIcon();
        Context context2 = this.context;
        UserHandle user2 = statusBarNotification.getUser();
        Intrinsics.checkExpressionValueIsNotNull(user2, "sbn.user");
        final Drawable drawableLoadDrawableAsUser2 = smallIcon.loadDrawableAsUser(context2, user2.getIdentifier());
        Intrinsics.checkExpressionValueIsNotNull(drawableLoadDrawableAsUser2, "sbn.notification.smallIc…     sbn.user.identifier)");
        final Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        T string = metadata != null ? metadata.getString("android.media.metadata.DISPLAY_TITLE") : 0;
        ref$ObjectRef.element = string;
        CharSequence charSequence = (CharSequence) string;
        if (charSequence == null || StringsKt__StringsJVMKt.isBlank(charSequence)) {
            ref$ObjectRef.element = metadata != null ? metadata.getString("android.media.metadata.TITLE") : 0;
        }
        CharSequence charSequence2 = (CharSequence) ref$ObjectRef.element;
        if (charSequence2 == null || StringsKt__StringsJVMKt.isBlank(charSequence2)) {
            ref$ObjectRef.element = HybridGroupManager.resolveTitle(notification2);
        }
        CharSequence charSequence3 = (CharSequence) ref$ObjectRef.element;
        if (charSequence3 == null || StringsKt__StringsJVMKt.isBlank(charSequence3)) {
            ref$ObjectRef.element = this.context.getString(R.string.controls_media_empty_title, strLoadHeaderAppName);
        }
        final Ref$ObjectRef ref$ObjectRef2 = new Ref$ObjectRef();
        T string2 = metadata != null ? metadata.getString("android.media.metadata.ARTIST") : 0;
        ref$ObjectRef2.element = string2;
        if (((CharSequence) string2) == null) {
            ref$ObjectRef2.element = HybridGroupManager.resolveText(notification2);
        }
        final ArrayList arrayList2 = new ArrayList();
        Notification.Action[] actionArr2 = notification2.actions;
        int[] intArray = notification2.extras.getIntArray("android.compactActions");
        if (intArray == null || (arrayList = ArraysKt___ArraysKt.toMutableList(intArray)) == null) {
            arrayList = new ArrayList();
        }
        List list2 = arrayList;
        Context packageContext = statusBarNotification.getPackageContext(this.context);
        Intrinsics.checkExpressionValueIsNotNull(packageContext, "sbn.getPackageContext(context)");
        if (actionArr2 != null) {
            int length = actionArr2.length;
            int i = 0;
            while (i < length) {
                int i2 = length;
                final Notification.Action action = actionArr2[i];
                if (action.getIcon() == null) {
                    actionArr = actionArr2;
                    StringBuilder sb = new StringBuilder();
                    notification = notification2;
                    sb.append("No icon for action ");
                    sb.append(i);
                    sb.append(' ');
                    sb.append(action.title);
                    Log.i("MediaDataManager", sb.toString());
                    list2.remove(Integer.valueOf(i));
                    list = list2;
                } else {
                    actionArr = actionArr2;
                    notification = notification2;
                    list = list2;
                    arrayList2.add(new MediaAction(action.getIcon().loadDrawable(packageContext), action.actionIntent != null ? new Runnable() { // from class: com.android.systemui.media.MediaDataManager$loadMediaDataInBg$runnable$1
                        @Override // java.lang.Runnable
                        public final void run() throws PendingIntent.CanceledException {
                            try {
                                action.actionIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                Log.d("MediaDataManager", "Intent canceled", e);
                            }
                        }
                    } : null, action.title));
                }
                i++;
                length = i2;
                actionArr2 = actionArr;
                notification2 = notification;
                list2 = list;
            }
        }
        final List list3 = list2;
        final Notification notification4 = notification2;
        MediaController.PlaybackInfo playbackInfo = mediaController.getPlaybackInfo();
        final boolean zAreEqual = Intrinsics.areEqual((Object) (playbackInfo != null ? Integer.valueOf(playbackInfo.getPlaybackType()) : null), (Object) 1);
        PlaybackState playbackState = mediaController.getPlaybackState();
        final Boolean boolValueOf = playbackState != null ? Boolean.valueOf(NotificationMediaManager.isPlayingState(playbackState.getState())) : null;
        this.foregroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaDataManager.loadMediaDataInBg.1
            @Override // java.lang.Runnable
            public final void run() {
                MediaData mediaData = (MediaData) MediaDataManager.this.mediaEntries.get(str);
                Runnable resumeAction = mediaData != null ? mediaData.getResumeAction() : null;
                MediaData mediaData2 = (MediaData) MediaDataManager.this.mediaEntries.get(str);
                boolean z = mediaData2 != null && mediaData2.getHasCheckedForResume();
                MediaData mediaData3 = (MediaData) MediaDataManager.this.mediaEntries.get(str);
                boolean active = mediaData3 != null ? mediaData3.getActive() : true;
                MediaDataManager mediaDataManager = MediaDataManager.this;
                String str3 = str;
                String str4 = str2;
                int normalizedUserId = statusBarNotification.getNormalizedUserId();
                int i3 = iComputeBackgroundColor;
                String str5 = strLoadHeaderAppName;
                Drawable drawable = drawableLoadDrawableAsUser2;
                CharSequence charSequence4 = (CharSequence) ref$ObjectRef2.element;
                CharSequence charSequence5 = (CharSequence) ref$ObjectRef.element;
                Icon icon2 = icon;
                List list4 = arrayList2;
                List list5 = list3;
                String packageName = statusBarNotification.getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName, "sbn.packageName");
                mediaDataManager.onMediaDataLoaded(str3, str4, new MediaData(normalizedUserId, true, i3, str5, drawable, charSequence4, charSequence5, icon2, list4, list5, packageName, token, notification4.contentIntent, null, active, resumeAction, zAreEqual, false, str, z, boolValueOf, statusBarNotification.isClearable(), LineageHardwareManager.FEATURE_COLOR_BALANCE, null));
            }
        });
    }

    private final Bitmap loadBitmapFromUri(MediaMetadata mediaMetadata) {
        for (String str : MediaDataManagerKt.ART_URIS) {
            String string = mediaMetadata.getString(str);
            if (!TextUtils.isEmpty(string)) {
                Uri uri = Uri.parse(string);
                Intrinsics.checkExpressionValueIsNotNull(uri, "Uri.parse(uriString)");
                Bitmap bitmapLoadBitmapFromUri = loadBitmapFromUri(uri);
                if (bitmapLoadBitmapFromUri != null) {
                    Log.d("MediaDataManager", "loaded art from " + str);
                    return bitmapLoadBitmapFromUri;
                }
            }
        }
        return null;
    }

    private final Bitmap loadBitmapFromUriForUser(Uri uri, int i, int i2, String str) {
        try {
            UriGrantsManager.getService().checkGrantUriPermission_ignoreNonSystem(i2, str, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, i));
            return loadBitmapFromUri(uri);
        } catch (SecurityException e) {
            Log.e("MediaDataManager", "Failed to get URI permission: " + e);
            return null;
        }
    }

    private final Bitmap loadBitmapFromUri(Uri uri) {
        if (uri.getScheme() == null) {
            return null;
        }
        if (!uri.getScheme().equals("content") && !uri.getScheme().equals("android.resource") && !uri.getScheme().equals("file")) {
            return null;
        }
        try {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.context.getContentResolver(), uri), new ImageDecoder.OnHeaderDecodedListener() { // from class: com.android.systemui.media.MediaDataManager.loadBitmapFromUri.1
                @Override // android.graphics.ImageDecoder.OnHeaderDecodedListener
                public final void onHeaderDecoded(ImageDecoder decoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
                    Intrinsics.checkExpressionValueIsNotNull(decoder, "decoder");
                    decoder.setMutableRequired(true);
                }
            });
        } catch (IOException e) {
            Log.e("MediaDataManager", "Unable to load bitmap", e);
            return null;
        } catch (RuntimeException e2) {
            Log.e("MediaDataManager", "Unable to load bitmap", e2);
            return null;
        }
    }

    private final int computeBackgroundColor(Bitmap bitmap) {
        int rgb;
        if (bitmap != null) {
            Palette paletteGenerate = MediaNotificationProcessor.generateArtworkPaletteBuilder(bitmap).generate();
            Intrinsics.checkExpressionValueIsNotNull(paletteGenerate, "MediaNotificationProcess…              .generate()");
            Palette.Swatch swatch = MediaNotificationProcessor.findBackgroundSwatch(paletteGenerate);
            Intrinsics.checkExpressionValueIsNotNull(swatch, "swatch");
            rgb = swatch.getRgb();
        } else {
            rgb = -1;
        }
        float[] fArr = {0.0f, 0.0f, 0.0f};
        ColorUtils.colorToHSL(rgb, fArr);
        float f = fArr[2];
        if (f < 0.05f || f > 0.95f) {
            fArr[1] = 0.0f;
        }
        fArr[1] = fArr[1] * 0.8f;
        fArr[2] = 0.25f;
        return ColorUtils.HSLToColor(fArr);
    }

    private final MediaAction getResumeMediaAction(Runnable runnable) {
        return new MediaAction(this.context.getDrawable(R.drawable.lb_ic_play), runnable, this.context.getString(R.string.controls_media_resume));
    }

    public final void onMediaDataLoaded(@NotNull String key, @Nullable String str, @NotNull MediaData data) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(data, "data");
        Assert.isMainThread();
        if (this.mediaEntries.containsKey(key)) {
            this.mediaEntries.put(key, data);
            notifyMediaDataLoaded(key, str, data);
        }
    }

    public final void onNotificationRemoved(@NotNull String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Assert.isMainThread();
        MediaData mediaDataRemove = this.mediaEntries.remove(key);
        if (this.useMediaResumption) {
            if ((mediaDataRemove != null ? mediaDataRemove.getResumeAction() : null) != null) {
                if (!isBlockedFromResume(mediaDataRemove != null ? mediaDataRemove.getPackageName() : null)) {
                    Log.d("MediaDataManager", "Not removing " + key + " because resumable");
                    Runnable resumeAction = mediaDataRemove.getResumeAction();
                    if (resumeAction == null) {
                        Intrinsics.throwNpe();
                    }
                    MediaData mediaDataCopy = mediaDataRemove.copy((3636223 & 1) != 0 ? mediaDataRemove.userId : 0, (3636223 & 2) != 0 ? mediaDataRemove.initialized : false, (3636223 & 4) != 0 ? mediaDataRemove.backgroundColor : 0, (3636223 & 8) != 0 ? mediaDataRemove.app : null, (3636223 & 16) != 0 ? mediaDataRemove.appIcon : null, (3636223 & 32) != 0 ? mediaDataRemove.artist : null, (3636223 & 64) != 0 ? mediaDataRemove.song : null, (3636223 & 128) != 0 ? mediaDataRemove.artwork : null, (3636223 & LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT) != 0 ? mediaDataRemove.actions : CollectionsKt__CollectionsJVMKt.listOf(getResumeMediaAction(resumeAction)), (3636223 & 512) != 0 ? mediaDataRemove.actionsToShowInCompact : CollectionsKt__CollectionsJVMKt.listOf(0), (3636223 & LineageHardwareManager.FEATURE_VIBRATOR) != 0 ? mediaDataRemove.packageName : null, (3636223 & LineageHardwareManager.FEATURE_TOUCH_HOVERING) != 0 ? mediaDataRemove.token : null, (3636223 & LineageHardwareManager.FEATURE_AUTO_CONTRAST) != 0 ? mediaDataRemove.clickIntent : null, (3636223 & LineageHardwareManager.FEATURE_DISPLAY_MODES) != 0 ? mediaDataRemove.device : null, (3636223 & LineageHardwareManager.FEATURE_READING_ENHANCEMENT) != 0 ? mediaDataRemove.active : false, (3636223 & 32768) != 0 ? mediaDataRemove.resumeAction : null, (3636223 & 65536) != 0 ? mediaDataRemove.isLocalSession : false, (3636223 & LineageHardwareManager.FEATURE_COLOR_BALANCE) != 0 ? mediaDataRemove.resumption : true, (3636223 & LineageHardwareManager.FEATURE_PICTURE_ADJUSTMENT) != 0 ? mediaDataRemove.notificationKey : null, (3636223 & LineageHardwareManager.FEATURE_TOUCHSCREEN_GESTURES) != 0 ? mediaDataRemove.hasCheckedForResume : false, (3636223 & 1048576) != 0 ? mediaDataRemove.isPlaying : null, (3636223 & LineageHardwareManager.FEATURE_ANTI_FLICKER) != 0 ? mediaDataRemove.isClearable : true);
                    String packageName = mediaDataRemove.getPackageName();
                    if (this.mediaEntries.put(packageName, mediaDataCopy) == null) {
                        notifyMediaDataLoaded(packageName, key, mediaDataCopy);
                        return;
                    } else {
                        notifyMediaDataRemoved(key);
                        notifyMediaDataLoaded(packageName, packageName, mediaDataCopy);
                        return;
                    }
                }
            }
        }
        if (mediaDataRemove != null) {
            notifyMediaDataRemoved(key);
        }
    }

    private final boolean isBlockedFromResume(String str) {
        if (str == null) {
            return true;
        }
        return this.appsBlockedFromResume.contains(str);
    }

    public final void setMediaResumptionEnabled(boolean z) {
        if (this.useMediaResumption == z) {
            return;
        }
        this.useMediaResumption = z;
        if (z) {
            return;
        }
        LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        for (Map.Entry<String, MediaData> entry : linkedHashMap.entrySet()) {
            if (!entry.getValue().getActive()) {
                linkedHashMap2.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry entry2 : linkedHashMap2.entrySet()) {
            this.mediaEntries.remove(entry2.getKey());
            notifyMediaDataRemoved((String) entry2.getKey());
        }
    }

    public final void onSwipeToDismiss() {
        this.mediaDataFilter.onSwipeToDismiss();
    }

    public final boolean hasActiveMedia() {
        return this.mediaDataFilter.hasActiveMedia();
    }

    public final boolean hasAnyMedia() {
        return this.mediaDataFilter.hasAnyMedia();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fd, @NotNull PrintWriter pw, @NotNull String[] args) {
        Intrinsics.checkParameterIsNotNull(fd, "fd");
        Intrinsics.checkParameterIsNotNull(pw, "pw");
        Intrinsics.checkParameterIsNotNull(args, "args");
        pw.println("internalListeners: " + this.internalListeners);
        pw.println("externalListeners: " + this.mediaDataFilter.getListeners$frameworks__base__packages__SystemUI__android_common__SystemUI_core());
        pw.println("mediaEntries: " + this.mediaEntries);
        pw.println("useMediaResumption: " + this.useMediaResumption);
        pw.println("appsBlockedFromResume: " + this.appsBlockedFromResume);
    }
}
