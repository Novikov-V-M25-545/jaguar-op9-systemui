package com.android.systemui.media;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.util.Log;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.statusbar.phone.NotificationListenerWithPlugins;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaSessionBasedFilter.kt */
/* loaded from: classes.dex */
public final class MediaSessionBasedFilter implements MediaDataManager.Listener {
    private final Executor backgroundExecutor;
    private final Executor foregroundExecutor;
    private final Map<String, Set<MediaSession.Token>> keyedTokens;
    private final Set<MediaDataManager.Listener> listeners;
    private final LinkedHashMap<String, List<MediaController>> packageControllers;
    private final MediaSessionBasedFilter$sessionListener$1 sessionListener;
    private final MediaSessionManager sessionManager;
    private final Set<MediaSession.Token> tokensWithNotifications;

    /* JADX WARN: Type inference failed for: r3v5, types: [com.android.systemui.media.MediaSessionBasedFilter$sessionListener$1] */
    public MediaSessionBasedFilter(@NotNull final Context context, @NotNull MediaSessionManager sessionManager, @NotNull Executor foregroundExecutor, @NotNull Executor backgroundExecutor) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(sessionManager, "sessionManager");
        Intrinsics.checkParameterIsNotNull(foregroundExecutor, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(backgroundExecutor, "backgroundExecutor");
        this.sessionManager = sessionManager;
        this.foregroundExecutor = foregroundExecutor;
        this.backgroundExecutor = backgroundExecutor;
        this.listeners = new LinkedHashSet();
        this.packageControllers = new LinkedHashMap<>();
        this.keyedTokens = new LinkedHashMap();
        this.tokensWithNotifications = new LinkedHashSet();
        this.sessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() { // from class: com.android.systemui.media.MediaSessionBasedFilter$sessionListener$1
            @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
            public void onActiveSessionsChanged(@NotNull List<MediaController> controllers) {
                Intrinsics.checkParameterIsNotNull(controllers, "controllers");
                this.this$0.handleControllersChanged(controllers);
            }
        };
        backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaSessionBasedFilter.1
            @Override // java.lang.Runnable
            public final void run() {
                ComponentName componentName = new ComponentName(context, (Class<?>) NotificationListenerWithPlugins.class);
                MediaSessionBasedFilter.this.sessionManager.addOnActiveSessionsChangedListener(MediaSessionBasedFilter.this.sessionListener, componentName);
                MediaSessionBasedFilter mediaSessionBasedFilter = MediaSessionBasedFilter.this;
                List<MediaController> activeSessions = mediaSessionBasedFilter.sessionManager.getActiveSessions(componentName);
                Intrinsics.checkExpressionValueIsNotNull(activeSessions, "sessionManager.getActiveSessions(name)");
                mediaSessionBasedFilter.handleControllersChanged(activeSessions);
            }
        });
    }

    public final boolean addListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull final String key, @Nullable final String str, @NotNull final MediaData info) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        Intrinsics.checkParameterIsNotNull(info, "info");
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaSessionBasedFilter.onMediaDataLoaded.1
            @Override // java.lang.Runnable
            public final void run() {
                ArrayList arrayList;
                MediaSession.Token token = info.getToken();
                if (token != null) {
                    MediaSessionBasedFilter.this.tokensWithNotifications.add(token);
                }
                String str2 = str;
                boolean z = str2 != null && (Intrinsics.areEqual(key, str2) ^ true);
                if (z) {
                    Map map = MediaSessionBasedFilter.this.keyedTokens;
                    String str3 = str;
                    if (map == null) {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.MutableMap<K, V>");
                    }
                    Set set = (Set) TypeIntrinsics.asMutableMap(map).remove(str3);
                    if (set != null) {
                    }
                }
                if (info.getToken() != null) {
                    Set set2 = (Set) MediaSessionBasedFilter.this.keyedTokens.get(key);
                    if (set2 == null) {
                    } else {
                        set2.add(info.getToken());
                    }
                }
                List list = (List) MediaSessionBasedFilter.this.packageControllers.get(info.getPackageName());
                MediaController mediaController = null;
                if (list != null) {
                    arrayList = new ArrayList();
                    for (Object obj : list) {
                        MediaController.PlaybackInfo playbackInfo = ((MediaController) obj).getPlaybackInfo();
                        if (playbackInfo != null && playbackInfo.getPlaybackType() == 2) {
                            arrayList.add(obj);
                        }
                    }
                } else {
                    arrayList = null;
                }
                if (arrayList != null && arrayList.size() == 1) {
                    mediaController = (MediaController) CollectionsKt.firstOrNull(arrayList);
                }
                if (z || mediaController == null || Intrinsics.areEqual(mediaController.getSessionToken(), info.getToken()) || !MediaSessionBasedFilter.this.tokensWithNotifications.contains(mediaController.getSessionToken())) {
                    MediaSessionBasedFilter.this.dispatchMediaDataLoaded(key, str, info);
                    return;
                }
                Log.d("MediaSessionBasedFilter", "filtering key=" + key + " local=" + info.getToken() + " remote=" + mediaController.getSessionToken());
                Object obj2 = MediaSessionBasedFilter.this.keyedTokens.get(key);
                if (obj2 == null) {
                    Intrinsics.throwNpe();
                }
                if (((Set) obj2).contains(mediaController.getSessionToken())) {
                    return;
                }
                MediaSessionBasedFilter.this.dispatchMediaDataRemoved(key);
            }
        });
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull final String key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        this.backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaSessionBasedFilter.onMediaDataRemoved.1
            @Override // java.lang.Runnable
            public final void run() {
                MediaSessionBasedFilter.this.keyedTokens.remove(key);
                MediaSessionBasedFilter.this.dispatchMediaDataRemoved(key);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void dispatchMediaDataLoaded(final String str, final String str2, final MediaData mediaData) {
        this.foregroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaSessionBasedFilter.dispatchMediaDataLoaded.1
            @Override // java.lang.Runnable
            public final void run() {
                Iterator it = CollectionsKt___CollectionsKt.toSet(MediaSessionBasedFilter.this.listeners).iterator();
                while (it.hasNext()) {
                    ((MediaDataManager.Listener) it.next()).onMediaDataLoaded(str, str2, mediaData);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void dispatchMediaDataRemoved(final String str) {
        this.foregroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaSessionBasedFilter.dispatchMediaDataRemoved.1
            @Override // java.lang.Runnable
            public final void run() {
                Iterator it = CollectionsKt___CollectionsKt.toSet(MediaSessionBasedFilter.this.listeners).iterator();
                while (it.hasNext()) {
                    ((MediaDataManager.Listener) it.next()).onMediaDataRemoved(str);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handleControllersChanged(List<MediaController> list) {
        this.packageControllers.clear();
        for (MediaController mediaController : list) {
            List<MediaController> list2 = this.packageControllers.get(mediaController.getPackageName());
            if (list2 != null) {
                list2.add(mediaController);
            } else {
                this.packageControllers.put(mediaController.getPackageName(), CollectionsKt__CollectionsKt.mutableListOf(mediaController));
            }
        }
        Set<MediaSession.Token> set = this.tokensWithNotifications;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(((MediaController) it.next()).getSessionToken());
        }
        set.retainAll(arrayList);
    }
}
